import java.io.File;
import java.util.Map;
import java.lang.Thread;

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

	public static void usage()
    {
        System.out.println("dbench.jar");
        System.out.println("usage:");
        System.out.println(" --host");
        System.out.println(" --database");
        System.out.println(" --collection");
        System.out.println(" --outputDir");
        System.out.println(" --numberOfShards");
        System.out.println(" --shardsPerNode");
    }

    public static void mkDir(String path)
    {
		File dir = new File(path);
		if (dir.exists() == false) {
			System.out.println("creating directory: " + path);
			boolean result = dir.mkdir();
			if (result) {
				System.out.println("[" + path + "] created");
			} else {
				System.out.println("failed to create dir (check permissions?)");
				System.exit(-1);
			}
		}

    }

	// --- Main ---
	public static void main(String[] argv)
	{
		String dbhost = "";
		String dbname = "";
		String dbcollection = "";
		String output_dir = "";
		String number_of_shards = "";
		String shards_per_node = "";

		// parse command line arguments
		if (argv.length < 10) {
            TestRunner.usage();
        } else {
            if (argv[0].equals("--host")) dbhost = argv[1];
            else System.out.println("argument 1 should be --host");

            if (argv[2].equals("--database")) dbname = argv[3];
            else System.out.println("arugment 3 should be --database");

            if (argv[4].equals("--collection")) dbcollection = argv[5];
            else System.out.println("arugment 5 should be --collection");

            if (argv[6].equals("--outputDir")) output_dir = argv[7];
            else System.out.println("arugment 7 should be --outputDir");

            if (argv[8].equals("--numberOfShards")) number_of_shards = argv[9];
            else System.out.println("arugment 9 should be --numberOfShards");

            if (argv[10].equals("--shardsPerNode"))  shards_per_node = argv[11];
            else System.out.println("arugment 1 should be --shardsPerNode");
        }

        System.out.println("--- Benchmarking settings ---");
        System.out.println("dbhost: " + dbhost);
        System.out.println("dbname: " + dbname);
        System.out.println("dbcollection: " + dbcollection);
        System.out.println("output_dir: " + output_dir);
        System.out.println("number_of_shards: " + number_of_shards);
        System.out.println("shards_per_node: " + shards_per_node);

		// create results folder if doesn't exist
		String shards_dir = output_dir + number_of_shards + "_shards/";
		String shards_per_node_dir = shards_dir + shards_per_node +"_shards_per_node/";
        mkDir(output_dir);
        mkDir(shards_dir);
        mkDir(shards_per_node_dir);

        // sleep for 2 minutes
        try {
			Thread.currentThread();
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// test settings
		TestRunner.aggregationTest(
				"mongodb",
				dbhost,
				dbname,
				dbcollection,
				shards_per_node_dir,
				"map-reduce"
		);

		// social graph
		/* MongoDBClient client = new MongoDBClient(); */
		/* client.connect(dbhost, 27017, dbname); */
		/* MongoDBTweetSocialGraph graph = */
		/* 		new MongoDBTweetSocialGraph(client, collection); */
		/* graph.createSocialGraph("London2012", 2); */

		System.exit(0);

	}
}
