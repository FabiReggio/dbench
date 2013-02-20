import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetSocialGraph;
import db.neo4j.CypherQueryController;
import db.neo4j.EmbeddedNeo4jClient;
import db.neo4j.Neo4jTweetImporter;
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
		
		String neo4j_server_uri = "http://localhost:7474/db/data/";
        String db_host = "http://avoss-cloud.cs.st-andrews.ac.uk";
        int db_port = 1234;
        String neo4j_dbpath = "/home/chris/neo4j_test/";
//        String test_data = "/home/chris/olympics3.jsonl";
//        String test_data = "/home/chris/test.jsonl";

//        SolrClient solr = new SolrClient(db_host, db_port);
//        solr.deleteAll();
//        solr.addTweets("/ssd/chris/twitter_data/olympics3.jsonl");
        
//        EmbeddedNeo4jClient neo4j = new EmbeddedNeo4jClient(neo4j_dbpath);
//        Neo4jTweetImporter neo4j_importer = new Neo4jTweetImporter(neo4j);
//        
//        neo4j.dropDatabase();
//        neo4j.connect();
//        neo4j_importer.importTweets(test_data, true);
        
       MongoDBClient client = new MongoDBClient();
       client.connect("project07.cs.st-andrews.ac.uk",
        		27017,
        		"db_tests");
       client.setCollection("query_test_collection");
       MongoDBTweetSocialGraph graph = new MongoDBTweetSocialGraph(client); 
       graph.createSocialGraph("JessCalandra", 2);
       
	}
}
