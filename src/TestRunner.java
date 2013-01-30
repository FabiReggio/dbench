import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import db.DBDetails;
import db.neo4j.CypherQueryController;
import db.neo4j.EmbeddedNeo4jClient;
import db.neo4j.Neo4jTweetImporter;
import db.neo4j.TweetRelationship;
import db.solr.SolrClient;
import dbtest.couchbase.CouchbaseAggregationTest;
import dbtest.mongodb.MongoDBAggregationTest;

/**
 * TestRunner as the name suggests is where the tests are executed from
 * @author Chris Choi
 */
public class TestRunner
{
	// --- Constructors ---
	public TestRunner() {}

	// --- Methods ---
	public static void aggregationTest(
			String type,
			String host,
			String db_name,
			String db_table,
			String res_path,
			String test_mode)
	{
		if (type.equals("couchbase")) {
			CouchbaseAggregationTest couchbase = new CouchbaseAggregationTest(
					"http://" + host,
					db_name);
			couchbase.run(res_path, 5);

		} else if (type.equals("mongodb")) {
			DBDetails details = new DBDetails(host, 27017, db_name, db_table);
			MongoDBAggregationTest mongo = new MongoDBAggregationTest(details);
			mongo.run(res_path, 5, test_mode);
		}
	}

	public static void fullTextSearchTest(
			String type,
			String host,
			String db_name,
			String db_table,
			String res_path,
			String test_mode)
	{
		if (type.equals("mongodb")) {
		}
	}

	// --- Main ---
	public static void main(String[] argv)
	{
		TestRunner test = new TestRunner();
		
        String db_host = "http://avoss-cloud.cs.st-andrews.ac.uk";
        int db_port = 1234;
        String neo4j_dbpath = "/Users/chutsu/neo4j_test/";
        String test_data = "/Users/chutsu/sandbox/twitter_data/test.jsonl";

//        SolrClient solr = new SolrClient(db_host, db_port);
//        solr.deleteAll();
//        solr.addTweets("/ssd/chris/twitter_data/olympics3.jsonl");
        
        EmbeddedNeo4jClient neo4j = new EmbeddedNeo4jClient(neo4j_dbpath);
        Neo4jTweetImporter neo4j_importer = new Neo4jTweetImporter(neo4j);
        
//        neo4j.dropDatabase();
        neo4j.connect();
//        neo4j_importer.importTweets(test_data);
        
        long node_id = neo4j.node_list.get("CGreentown");
        
//        for (Map.Entry<String, Long> entry: neo4j.node_list.entrySet()) {
//        	Node node = (Node) neo4j.graph_db.getNodeById(entry.getValue());
//        	if (node.hasProperty("screen_name")) {
////	        	System.out.println(entry.getKey() + ":" + entry.getValue());
//	        	System.out.println(node.getProperty("screen_name") + " : " + node.getProperty("weight"));
////	        	System.out.println();
//        	}
//        }
        
//        for (Map.Entry<String, Relationship> entry: neo4j.rel_list.entrySet()) {
//        	if (entry.getValue().getType().equals(TweetRelationship.Type.SHARES_URL)) {
//        		Relationship rel = entry.getValue();
//        		Node start_node = rel.getStartNode();
//        		Node end_node = rel.getEndNode();
//        		System.out.println(start_node.getProperty("screen_name") + " ---> " + end_node.getProperty("display_url"));
//        	}
//        }
        
        for (Map.Entry<String, Relationship> entry: neo4j.rel_list.entrySet()) {
        	if (entry.getValue().getType().equals(TweetRelationship.Type.HASH_TAGS)) {
        		Relationship rel = entry.getValue();
        		Node start_node = rel.getStartNode();
        		Node end_node = rel.getEndNode();
        		System.out.println(start_node.getProperty("screen_name") + " ---> " + end_node.getProperty("hash_tag"));
        	}
        }
        
        CypherQueryController query_engine = new CypherQueryController(neo4j);
        System.out.println("node_id: " + node_id);
        String q = "START n = node(" + node_id + ") " +
        		"MATCH n-[:MENTIONS]->user " +
        		"RETURN n.screen_name?, user.screen_name?";
        query_engine.query(q);
        
        q = "START n = node(" + node_id + ") " +
        		"MATCH n-[:HASH_TAGS]->tag " +
        		"RETURN n.screen_name?, tag.hash_tag?";
        query_engine.query(q);
        
//        q = "START n = node(" + node_id + ") " +
//        		"MATCH n-[:SHARES_URL]->url " +
//        		"RETURN n.screen_name?, url.display_url?";
//        query_engine.query(q);
        
	}
}