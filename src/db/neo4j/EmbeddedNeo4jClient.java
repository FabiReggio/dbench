package db.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
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
	private GraphDatabaseService graph_db;
	private String db_path;
	private GlobalGraphOperations global_db_op;
	private HashMap<String, Long> node_list = new HashMap<String, Long>();
	
    private String greeting;
    private Node node_1;
    private Node node_2;
    private Relationship relationship;


    // --- Constructor ---
    public EmbeddedNeo4jClient(String db_path) {
    	this.db_path = db_path;
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
        
        // load all nodes to a node_name_list
        this.global_db_op = GlobalGraphOperations.at(this.graph_db);
        for (Node n : global_db_op.getAllNodes()) 
        	this.node_list.put((String) n.getProperty("value"), n.getId());
        
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
     * Defined relationship types between nodes
     * @author chris
     */
    private static enum RelTypes implements RelationshipType
    {
        KNOWS,
        MENTIONS,
        SHARES_URL, 
        HASH_TAGGED
    }

    /**
     * 
     */
    public void createDb()
    {
        Transaction tx = graph_db.beginTx();
        try
        {
            // Updating operations go here
            node_1 = graph_db.createNode();
            node_1.setProperty( "message", "Hello, " );
            node_2 = graph_db.createNode();
            node_2.setProperty( "message", "World!" );

            relationship = node_1.createRelationshipTo(node_2, RelTypes.KNOWS);
            relationship.setProperty( "message", "brave Neo4j " );

            System.out.print( node_1.getProperty( "message" ) );
            System.out.print( relationship.getProperty( "message" ) );
            System.out.print( node_2.getProperty( "message" ) );

            greeting = ( (String) node_1.getProperty( "message" ) )
                       + ( (String) relationship.getProperty( "message" ) )
                       + ( (String) node_2.getProperty( "message" ) );

            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    /**
     * Insert tweet
     * @param tweet
     */
    public void addTweet(Status tweet)
    {
        Transaction tx = this.graph_db.beginTx();
        try
        {
        	String tweet_author = tweet.getUser().getScreenName();
        	UserMentionEntity[] user_mentions = tweet.getUserMentionEntities();
        	HashtagEntity[] hash_tags = tweet.getHashtagEntities();
        	URLEntity[] urls = tweet.getURLEntities();
        	
        	String user_mentioned;
        	for (UserMentionEntity user_mention : user_mentions) {
        		user_mentioned = user_mention.getScreenName();
        		if (nodeExists(user_mentioned)) {
        			incrementNodeWeight(user_mentioned);
        		} else {
        			
        		}
        		
        	}
        	
            tx.success();
        } finally {
            tx.finish();
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
     * Remove data from graph database
     */
    public void removeData()
    {
        Transaction tx = graph_db.beginTx();
        try {
            // let's remove the data
            node_1.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING)
            		.delete();
            node_1.delete();
            node_2.delete();

            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    public boolean createNewNode(String node_name) 
    {
    	boolean outcome = false;
        Transaction tx = this.graph_db.beginTx();
        
        try {
        	 Node node = this.graph_db.createNode();
        	 
             node.setProperty("value", node_name);
             node.setProperty("weight", 0);
             
             outcome = true;
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
        } finally {
        	tx.finish();
        }
    	
        return outcome;
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
    	return this.node_list.containsKey((String) node_name);
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