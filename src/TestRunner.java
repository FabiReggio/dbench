import db.DBDetails;
import db.mongodb.MongoDBClient;
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
		MongoDBClient mongo = new MongoDBClient();
		mongo.connect("localhost", 27017, "query_test_collection");
		mongo.setCollection("query_test_collection");
		if (mongo.addKeywordField("text") == false)
			System.out.println("Opps! couldn't add a keyword field");
		
//		aggregationTest(
//                "mongodb", 
//				"localhost",
//                "db_tests",
//                "query_test_collection",
//                "results/test",
//                "map-reduce");
	}
}
