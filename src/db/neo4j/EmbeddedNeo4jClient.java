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

import db.neo4j.TweetRelationship;
import db.neo4j.TweetProperty;

public class EmbeddedNeo4jClient 
{
    // --- Fields ---
	public GraphDatabaseService graph_db;
	private String db_path;
	private GlobalGraphOperations global_db_op;
	public HashMap<String, Long> node_list = new HashMap<String, Long>();
	public HashMap<String, Relationship> rel_list = new HashMap<String, Relationship>();
	
    // --- Constructor ---
    public EmbeddedNeo4jClient(String db_path) 
    {
    	this.db_path = db_path;
    }

    // --- Methods ---
    /**
     * Connect to embedded database
     */
    public boolean connect() 
    {
    	String key = null;
    	String value = null;
    	Long p_id = null;
    	Node start_node = null;
    	Node end_node = null;
    	RelationshipType rel_type = null;
    	
    	// create db object
    	this.graph_db = new EmbeddedGraphDatabase(this.db_path);
        registerShutdownHook(this.graph_db);
        
        // clear all book keepers
        node_list.clear();
        rel_list.clear();
        
        this.global_db_op = GlobalGraphOperations.at(this.graph_db);
        for (Node node : global_db_op.getAllNodes()) {
	        // load all nodes to a node_list
        	for (String property: node.getPropertyKeys()) {
        		if (property.equals("weight") == false) {
        			value = (String) node.getProperty(property);
        			p_id = node.getId();
		        	this.node_list.put(value, p_id);
        		}
        	}
        	
        	// load all relationships to rel_list
        	Iterable<Relationship> rels = node.getRelationships();
        	for (Relationship rel: rels) {
        		if ((end_node = rel.getEndNode()) != node) {
        			rel_type = rel.getType();
        			start_node = rel.getStartNode();
        			
        			if (rel_type.equals(TweetRelationship.Type.MENTIONS)) {
	        	    	key = "(" + start_node.getProperty(TweetProperty.USER) 
	        	    			+ ")--[" + rel_type + "]-->("
	        	    			+ end_node.getProperty(TweetProperty.USER) 
	        	    			+ ")";
        			} else if (rel_type.equals(TweetRelationship.Type.HASH_TAGS)) {
	        	    	key = "(" + start_node.getProperty(TweetProperty.USER) 
	        	    			+ ")--[" + rel_type + "]-->("
	        	    			+ end_node.getProperty(TweetProperty.HASH_TAG) 
	        	    			+ ")";
        			} else if (rel_type.equals(TweetRelationship.Type.SHARES_URL)) {
	        	    	key = "(" + start_node.getProperty(TweetProperty.USER) 
	        	    			+ ")--[" + rel_type + "]-->("
	        	    			+ end_node.getProperty(TweetProperty.URL) 
	        	    			+ ")";
	        		} else {
	        			throw new RuntimeException();
	        		}
        			
        			this.rel_list.put(key, rel);
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
        	addNewNode(TweetProperty.USER, tweet_author);
    	
    	// iterate through user_mentions, hash tags and urls and create
    	// relationships with the author node
    	// USER MENTIONED
    	String user_mentioned;
    	for (UserMentionEntity user_mention : user_mentions) {
    		user_mentioned = user_mention.getScreenName();
    		if (user_mentioned != null) {
	    		if (nodeExists(user_mentioned)) {
	    			incrementNodeWeight(user_mentioned);
	    		} else {
	        		addNewNode(TweetProperty.USER, user_mentioned);
	        		createRelationship(
	        				tweet_author, 
	        				user_mentioned, 
	        				TweetRelationship.Type.MENTIONS);
	    		}
    		}
    	}
    	
    	// HASH TAGGED
    	String tag;
    	for (HashtagEntity hash_tag: hash_tags) {
    		tag = hash_tag.getText();
    		if (tag != null) {
	    		if (nodeExists(tag)) {
	    			incrementNodeWeight(tag);
	    		} else {
	        		addNewNode(TweetProperty.HASH_TAG, tag);
	        		createRelationship(
	        				tweet_author, 
	        				tag, 
	        				TweetRelationship.Type.HASH_TAGS);
	    		}
    		}
    	}
    	
    	// SHARED URLS 
    	String display_url;
    	for (URLEntity url: urls) {
    		display_url = url.getDisplayURL();
    		if (display_url != null) {
	    		if (nodeExists(display_url)) {
	    			incrementNodeWeight(display_url);
	    		} else {
	        		addNewNode(TweetProperty.URL, display_url);
	        		createRelationship(
	        				tweet_author, 
	        				display_url, 
	        				TweetRelationship.Type.SHARES_URL);
	    		}
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
    public boolean addNewNode(String property, String value) 
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
    public boolean rmNode(String node_name) 
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
        	
        	// commit
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
    	if (this.node_list.containsKey((String) node_name)) return true;
    	else return false;
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
        Transaction tx = this.graph_db.beginTx();
        
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
    		RelationshipType rel_type) 
    {
    	String key = "(" + node_1 + ")--[" + rel_type + "]-->(" + node_2 + ")";
    	return  rel_list.get(key);
    }
  
    /**
     * Create relationship between two nodes
     * @param node_1
     * @param node_2
     * @param rel_type
     * @return
     */
    public boolean addRelationship(
    			Node node_1, 
    			Node node_2, 
    			RelationshipType rel_type) throws RuntimeException
    {
    	Relationship rel = null;
    	String n_1 = "";
    	String n_2 = "";
    	
    	Transaction tx = this.graph_db.beginTx();
		try {
	    	// create corresponding relationship
	    	if (rel_type.equals(TweetRelationship.Type.MENTIONS)) {
		    	rel = node_1.createRelationshipTo(node_2, rel_type);
		    	n_1 = (String) node_1.getProperty(TweetProperty.USER);
		    	n_2 = (String) node_2.getProperty(TweetProperty.USER);
	    	} else if (rel_type.equals(TweetRelationship.Type.HASH_TAGS)) {
		    	rel = node_1.createRelationshipTo(node_2, rel_type);
		    	n_1 = (String) node_1.getProperty(TweetProperty.USER);
		    	n_2 = (String) node_2.getProperty(TweetProperty.HASH_TAG);
	    	} else if (rel_type.equals(TweetRelationship.Type.SHARES_URL)) {
		    	rel = node_1.createRelationshipTo(node_2, rel_type);
		    	n_1 = (String) node_1.getProperty(TweetProperty.USER);
		    	n_2 = (String) node_2.getProperty(TweetProperty.URL);
	    	} else {
	    		throw new RuntimeException();
	    	}
	    	
	    	// commit
		    tx.success();
		} finally {
			tx.finish();
		
	    	// add relationship to rel_list
	    	String key = "(" + n_1 + ")--[" + rel_type + "]-->(" + n_2 + ")";
	    	rel_list.put(key, rel);
		}
    	
    	return false;
    }
    
    /**
     * Create relationship between two nodes
     * @param node_1
     * @param node_2
     * @param rel_type
     * @return
     */
    public boolean createRelationship(
    		String node_1,
    		String node_2,
    		RelationshipType rel_type) 
    {
    	Node first_node = this.graph_db.getNodeById(node_list.get(node_1));
    	Node second_node = this.graph_db.getNodeById(node_list.get(node_2));
    	
    	return addRelationship(first_node, second_node, rel_type);
    }
    
    /**
     * Remove relationship between two nodes
     * @param node_1
     * @param node_2
     * @param rel_type
     */
    public void removeRelationship(
    		String node_1,
    		String node_2,
    		RelationshipType rel_type)
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