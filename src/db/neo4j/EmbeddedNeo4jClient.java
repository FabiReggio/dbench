package db.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.tooling.GlobalGraphOperations;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class EmbeddedNeo4jClient 
{
    // --- Fields ---
	public GraphDatabaseService graph_db;
	private String db_path;
	private GlobalGraphOperations global_db_op;
	public HashMap<String, Long> node_list = new HashMap<String, Long>();
	public HashMap<String, Relationship> rel_list = new HashMap<String, Relationship>();
    private static enum RelTypes implements RelationshipType
    {
        MENTIONS,
        SHARES_URL, 
        HASH_TAGS
    }
	
    // --- Constructor ---
    public EmbeddedNeo4jClient(String db_path) {
    	this.db_path = db_path;
    	this.connect();
    }

    // --- Methods ---
    /**
     * Connect to embedded database
     */
    public boolean connect() 
    {
    	// create db object
    	this.graph_db = new EmbeddedGraphDatabase(this.db_path);
        registerShutdownHook(this.graph_db);
        
        // clear all book keepers
        node_list.clear();
        rel_list.clear();
        
        // load all nodes to a node_name_list
        this.global_db_op = GlobalGraphOperations.at(this.graph_db);
        for (Node n : global_db_op.getAllNodes()) {
        	for (String property: n.getPropertyKeys()) {
        		if (property.equals("weight") == false) {
        			String value = (String) n.getProperty(property);
        			Long p_id = n.getId();
		        	this.node_list.put(value, p_id);
        		}
        	}
        }
        
    	if (this.graph_db != null) return true;
    	else return false;
    }

    /**
     * Disconnect from embedded database
     */
    public void disconnect() 
    {
        System.out.println("shutting down graph database [neo4j]");
    	this.graph_db.shutdown();
    }    
    
    /**
     * Registers the graph database for shutdown
     * @param graphDb
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb)
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        });
    }

    /**
     * Insert tweet
     * @param tweet
     */
    public void addTweet(Status tweet)
    {
    	String tweet_author = tweet.getUser().getScreenName();
    	UserMentionEntity[] user_mentions = tweet.getUserMentionEntities();
    	HashtagEntity[] hash_tags = tweet.getHashtagEntities();
    	URLEntity[] urls = tweet.getURLEntities();
    	
    	// create a node for the author
    	if (nodeExists(tweet_author) == false) 
        	createNewNode("user", tweet_author);
    	
    	// iterate through user_mentions, hash tags and urls and create
    	// relationships with the author node
    	// USER MENTIONED
    	String user_mentioned;
    	for (UserMentionEntity user_mention : user_mentions) {
    		user_mentioned = user_mention.getScreenName();
    		if (user_mentioned != null) {
	    		if (nodeExists(user_mentioned))
	    			incrementNodeWeight(user_mentioned);
	    		else 
	        		createNewNode("user", user_mentioned);
    		}
    	}
    	
    	// HASH TAGGED
    	String tag;
    	for (HashtagEntity hash_tag: hash_tags) {
    		tag = hash_tag.getText();
    		if (tag != null) {
	    		if (nodeExists(tag)) 
	    			incrementNodeWeight(tag);
	    		else
	        		createNewNode("hash_tag", tag);
    		}
    	}
    	
    	// SHARED URLS 
    	String display_url;
    	for (URLEntity url: urls) {
    		display_url = url.getDisplayURL();
    		if (display_url != null) {
	    		if (nodeExists(display_url)) 
	    			incrementNodeWeight(display_url);
	    		else
	        		createNewNode("url", display_url);
    		}
    	}
    }

    /**
     * Drops the graph database
     */
    public void dropDatabase()
    {
        try {
            FileUtils.deleteRecursively(new File(this.db_path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new node
     * @param node_name
     * @return
     */
    public boolean createNewNode(String property, String value) 
    {
    	boolean outcome = false;
    	
    	if (nodeExists(value)) return outcome;
    	
        Transaction tx = this.graph_db.beginTx();
        
        try {
        	 Node node = this.graph_db.createNode();
        	 
             node.setProperty(property, value);
             node.setProperty("weight", 1);
             
             outcome = true;
             // record new node in node_list
             node_list.put(value, node.getId()); 
             
             tx.success();
        } finally {
        	tx.finish();
        }
        
        return outcome;
    }
    
    /**
     * Delete the Node and its relationship to other nodes
     * @param node_name
     * @return
     * 		True or False
     */
    public boolean deleteNode(String node_name) 
    {
    	boolean outcome = false;
        Transaction tx = this.graph_db.beginTx();
        
        try {
			long node_id = this.node_list.get((String) node_name);
			Node node = this.graph_db.getNodeById(node_id);
			
			// Note: MUST DO THIS BEFORE node.delete()
			// delete all relationships to and from this node
			for (Relationship relationship : node.getRelationships())
				relationship.delete();
			
			// delete the actual node
			node.delete();
			
        	outcome = true;
        	// remove node in node_list
        	node_list.remove(node_name);
        	
            tx.success();
        } finally {
        	tx.finish();
        }
    	
        return outcome;
    }
    
    /**
     * Checks to see if node with specific property and value exists
     * @param node_name 
     * @return
     * 		Boolean
     */
    public boolean nodeExists(String node_name)
    {
    	if (this.node_list.containsKey((String) node_name)) 
    		return true;
    	else 
    		return false;
    }
    
    /**
     * Increment the node's weight
     * @param node_name
     * @return
     * 		True or False
     */
    public boolean incrementNodeWeight(String node_name) 
    {
    	boolean outcome = false;
        Transaction tx = graph_db.beginTx();
        
        try {
	    	// obtain node
			long node_id = this.node_list.get((String) node_name);
			Node node = this.graph_db.getNodeById(node_id);
			
			// get current weight and increment
			int weight = (Integer) node.removeProperty("weight");
			weight += 1;
			
			// update new weight
			node.setProperty("weight", weight);
			
			outcome = true;
            tx.success();
        } finally {
        	tx.finish();
        }
		
		return outcome;
    }
    
    /**
     * Obtain relationship between two nodes
     * @param node_1
     * @param node_2
     * @param rel_type
     * @return
     */
    public Relationship getRelationship(
    		String node_1, 
    		String node_2,
    		String rel_type) 
    {
    	String key = node_1 + "-" + node_2 + ":[" + rel_type + "]";
    	return  rel_list.get(key);
    }
  
    /**
     * Create relationship between node 1 and node 2
     * @param node_1
     * @param node_2
     * @param rel_type
     * @return
     */
    public boolean createRelationship(
    			Node node_1, 
    			Node node_2, 
    			String rel_type) 
    {
    	Relationship rel = null;
    	String first_node = "";
    	String second_node = "";
    	
    	// create corresponding relationship
    	if (rel_type.equals("mentions")) {
	    	rel = node_1.createRelationshipTo(node_2, RelTypes.MENTIONS);
	    	first_node = (String) node_1.getProperty("user");
	    	second_node = (String) node_2.getProperty("user");
    	} else if (rel_type.equals("hash_tagged")) {
	    	rel = node_1.createRelationshipTo(node_2, RelTypes.HASH_TAGS);
	    	first_node = (String) node_1.getProperty("user");
	    	second_node = (String) node_2.getProperty("hashtag");
    	} else if (rel_type.equals("shared_url")) {
	    	rel = node_1.createRelationshipTo(node_2, RelTypes.SHARES_URL);
	    	first_node = (String) node_1.getProperty("user");
	    	second_node = (String) node_2.getProperty("url");
    	}
    	
    	// add relationship to rel_list
    	String key = first_node + "-" + second_node + ":[" + rel_type + "]";
    	rel_list.put(key, rel);
    	
    	return false;
    }
    
    public void removeRelationship(
    		String node_1,
    		String node_2,
    		String rel_type)
    {
    	getRelationship(node_1, node_2, rel_type).delete();
    }
    		
    /**
     * Checks to see if connected
     * @return
     */
    public boolean connected() 
    {
    	if (this.graph_db != null) return true;
    	else return false;
    }
}