import java.io.File;
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
 * 
 * @author Chris Choi
 */
public class TestRunner {
	// --- Constructors ---
	public TestRunner() {
	}

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
					"http://" + host, db_name);
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
	public static void main(String[] argv) {
		TestRunner test = new TestRunner();

		String neo4j_server_uri = "http://localhost:7474/db/data/";
		String db_host = "http://avoss-cloud.cs.st-andrews.ac.uk";
		int db_port = 1234;
		String neo4j_dbpath = "/home/chris/neo4j_test/";

		// results folder
		String number_of_shards = "1";
		String shards_per_node = "4";
		String results_folder = number_of_shards 
				+ "shards-" 
				+ shards_per_node + "per_shard";

		// create results folder if doesn't exist
		File results_dir = new File(results_folder);
		if (results_dir.exists() == false) {
			System.out.println("creating directory: " + results_folder);
			boolean result = results_dir.mkdir();
			if (result) {
				System.out.println("[" + results_folder + "] created");
			} else {
				System.out.println("failed to create dir (check permissions?)");
				System.exit(-1);
			}
		}
		
		// test settings
//		TestRunner.aggregationTest(
//				"mongodb", 
//				"e-research.cs.st-andrews.ac.uk",
//				"db_tests",
//				"sample_data",
//				results_folder + "/", 
//				"map-reduce"
//		);

		// social graph
		MongoDBClient client = new MongoDBClient();
		client.connect("e-research.cs.st-andrews.ac.uk", 27017, "db_tests");
		client.setCollection("sample_data");
		MongoDBTweetSocialGraph graph = new MongoDBTweetSocialGraph(client);
		graph.createSocialGraph("London2012", 2);

	}
}
