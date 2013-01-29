import db.DBDetails;
import db.neo4j.EmbeddedNeo4jClient;
import db.neo4j.Neo4jTweetImporter;
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
        neo4j.dropDatabase();
        neo4j.connect();
        Neo4jTweetImporter neo4j_importer = new Neo4jTweetImporter(neo4j);
        neo4j_importer.importTweets(test_data);
	}
}
