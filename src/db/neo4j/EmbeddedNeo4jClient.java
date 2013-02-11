package db.neo4j;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import db.neo4j.TweetRelationship;
import db.neo4j.NodeType;

public class EmbeddedNeo4jClient
{
	// --- Fields ---
	public GraphDatabaseService graph_db;
	private String db_path;
	private IndexManager index_manager;
	private Index<Node> user_index;
	private Index<Node> hashtag_index;
	private Index<Node> url_index;
	private RelationshipIndex mentions_rel_index;
	private RelationshipIndex hashtags_rel_index;
	private RelationshipIndex shares_rel_index;

	// --- Constructor ---
	public EmbeddedNeo4jClient(String db_path)
	{
		this.db_path = db_path;
	}

	// --- Methods ---
	/**
	 * Connect to embedded database
	 * @return
	 * 		True or false for success or failure
	 */
	public boolean connect()
	{
		// create db object
		this.graph_db = new EmbeddedGraphDatabase(this.db_path);
		registerShutdownHook(this.graph_db);

		initIndexes();

		if (this.graph_db != null)
			return true;
		else
			return false;
	}

	/**
	 * Initializes indexes
	 * @return
	 * 		True or false for success or failure
	 */
	private boolean initIndexes()
	{
		try {
			index_manager = this.graph_db.index();

			user_index = index_manager.forNodes("users");
			hashtag_index = index_manager.forNodes("hashtags");
			url_index = index_manager.forNodes("urls");
			mentions_rel_index = index_manager.forRelationships("rel_mentions");
			hashtags_rel_index = index_manager.forRelationships("rel_hashtags");
			shares_rel_index = index_manager.forRelationships("rel_shares");

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}

		return true;
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
	 *
	 * @param graphDb
	 */
	private static void registerShutdownHook(final GraphDatabaseService graphDb)
	{
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
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
	 * Checks to see if node with specific property and value exists
	 *
	 * @param node_name
	 * @return Node or null for not exist
	 */
	public Node nodeExists(String node_name, String node_type)
	{
		IndexHits<Node> hits;
		Node node = null;

		if (node_type.equals(NodeType.USER))
			hits = user_index.get("value", node_name);
		else if (node_type.equals(NodeType.HASH_TAG))
			hits = hashtag_index.get("value", node_name);
		else if (node_type.equals(NodeType.URL))
			hits = url_index.get("value", node_name);
		else
			throw new RuntimeException();

		if (hits.size() == 1)
			node = hits.getSingle();
		else if (hits.size() > 1)
			throw new RuntimeException();

		hits.close(); // very important!
		return node;
	}

	/**
	 * Create a new node
	 *
	 * @param node_name
	 * @return
	 */
	public boolean addNode(String node_type, String value)
	{
		boolean outcome = false;

		if (nodeExists(value, node_type) != null)
			return outcome;

		Transaction tx = this.graph_db.beginTx();

		try {
			Node node = this.graph_db.createNode();

			// set node properties
			node.setProperty("node_type", node_type);
			node.setProperty("value", value);
			node.setProperty("weight", 1);

			// add node to corresponding index
			if (node_type.equals(NodeType.USER))
				user_index.add(node, "value", value);
			else if (node_type.equals(NodeType.HASH_TAG))
				hashtag_index.add(node, "value", value);
			else if (node_type.equals(NodeType.URL))
				url_index.add(node, "value", value);

			tx.success();
			outcome = true;
		} finally {
			tx.finish();
		}

		return outcome;
	}

	/**
	 * Delete the Node and its relationship to other nodes
	 *
	 * @param node_name
	 * @return True or False
	 */
	public boolean deleteNode(String node_name, String node_type)
	{
		boolean outcome = false;
		Transaction tx = this.graph_db.beginTx();

		try {
			Node node = nodeExists(node_name, node_type);

			// Note: MUST DO THIS BEFORE node.delete()
			// delete all relationships to and from this node
			for (Relationship relationship : node.getRelationships())
				relationship.delete();

			// delete the actual node
			node.delete();

			// rm node from corresponding index
			if (node_type.equals(NodeType.USER))
				user_index.remove(node, "value", node_name);
			else if (node_type.equals(NodeType.HASH_TAG))
				hashtag_index.remove(node, "value", node_name);
			else if (node_type.equals(NodeType.URL))
				url_index.remove(node, "value", node_name);

			// commit
			tx.success();
			outcome = true;
		} finally {
			tx.finish();
		}

		return outcome;
	}

	/**
	 * Increment the node's weight
	 *
	 * @param node_name
	 * @return True or False
	 */
	public boolean incrementNodeWeight(Node node)
	{
		boolean outcome = false;
		Transaction tx = this.graph_db.beginTx();

		try {
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
	 *
	 * @param node_1
	 * @param node_2
	 * @param rel_type
	 * @return
	 * 		Relationship between node 1 and 2 or else returns null
	 */
	public Relationship getRelationship(
			Node node_1,
			Node node_2,
			RelationshipType rel_type)
	{
		IndexHits<Relationship> hits = null;
		Relationship rel = null;

		// check to see if relationship already exists in index
		if (rel_type.equals(TweetRelationship.Type.MENTIONS)) {
			hits = mentions_rel_index.get(
					"target",
					node_2.getProperty("value"),
					node_1,
					node_2);
		} else if (rel_type.equals(TweetRelationship.Type.HASH_TAGS)) {
			hits = hashtags_rel_index.get(
					"target",
					node_2.getProperty("value"),
					node_1,
					node_2);
		} else if (rel_type.equals(TweetRelationship.Type.SHARES_URL)) {
			hits = shares_rel_index.get(
					"target",
					node_2.getProperty("value"),
					node_1,
					node_2);
		} else {
			throw new RuntimeException();
		}

		if (hits != null && hits.size() == 1) rel = hits.getSingle();
		else if (hits.size() > 1) throw new RuntimeException();

		hits.close(); // very important!

		return rel;
	}

	/**
	 * Create relationship between two nodes
	 *
	 * @param node_1
	 * @param node_2
	 * @param rel_type
	 * @return
	 */
	public boolean createRelationship(
			Node node_1,
			Node node_2,
			RelationshipType rel_type) throws RuntimeException
	{
		boolean outcome = false;
		Relationship rel = null;

		Transaction tx = this.graph_db.beginTx();
		try {
			// check if relationship exists
			if (getRelationship(node_1, node_2, rel_type) == null) {
				rel = node_1.createRelationshipTo(node_2, rel_type);
				rel.setProperty("from", node_1.getProperty("value"));
				rel.setProperty("to", node_2.getProperty("value"));
			} else {
				return false;
			}

			// add node to corresponding index
			if (rel_type.equals(TweetRelationship.Type.MENTIONS)) {
				mentions_rel_index.add(
						rel,
						"target",
						node_2.getProperty("value"));
			} else if (rel_type.equals(TweetRelationship.Type.HASH_TAGS)) {
				hashtags_rel_index.add(
						rel,
						"target",
						node_2.getProperty("value"));
			} else if (rel_type.equals(TweetRelationship.Type.SHARES_URL)) {
				shares_rel_index.add(
						rel,
						"target",
						node_2.getProperty("value"));
			}

			// commit
			tx.success();
		} finally {
			tx.finish();
			outcome = true;
		}

		return outcome;
	}

	/**
	 * Remove relationship between two nodes
	 *
	 * @param node_1
	 * @param node_2
	 * @param rel_type
	 */
	public void removeRelationship(
			Node node_1,
			Node node_2,
			RelationshipType rel_type)
	{
		getRelationship(node_1, node_2, rel_type).delete();
	}

	/**
	 * Checks to see if connected
	 *
	 * @return
	 */
	public boolean connected()
	{
		if (this.graph_db != null)
			return true;
		else
			return false;
	}
}
