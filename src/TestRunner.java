import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import unittests.MongoDBUnitTests;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.solr.SolrClient;
import dbtest.MongoDBFindTest;
import dbtest.MongoDBIOTest;
import dbtest.MongoDBAggregationTest;

/**
 * TestRunner as the name suggests is where the tests are executed from
 * @author Chris Choi 
 */
public class TestRunner 
{
	// --- Fields ---
	
	// --- Constructors ---
	public TestRunner() {}
	
	// --- Methods ---
	/**
	 * Not to be confused with the database performance test, the unit 
	 * tests serve as a confirmation that an interface in communicating 
	 * with different databases do indeed work.
	 */
	public void runUnitTests() 
    {
        System.out.println("running Unit Tests!");
        Result result = JUnitCore.runClasses(MongoDBUnitTests.class);
        for (Failure failure: result.getFailures()) {
            System.out.println(failure.toString());
        }
    }	

	// --- Main ---
	public static void main(String[] argv) 
	{
		String db_name = "db_tests";
		String p6 = "project06.cs.st-andrews.ac.uk";
		String p7 = "project07.cs.st-andrews.ac.uk";
		String local_host = "http://localhost";
		int db_port = 27017;
		
		String io_col = "io_test_collection";
		String q_col = "query_test_collection";
		String test_col = "test";
		String x = "/datadisk1/userContent/olympics3.jsonl";
		String y = "/datadisk1/home/chris/twitter_data/100meters.json.test";
		
		DBDetails proj06 = new DBDetails(p6, db_port, db_name, io_col);
		MongoDBIOTest io_test = new MongoDBIOTest(proj06);
		io_test.run(y, "./", 5);

		/* MongoDBAggregationTest aggre_test = new MongoDBAggregationTest(proj07); */
		/* MongoDBFindTest find_test = new MongoDBFindTest(host_proj07); */
		
		/* find_test.run("./", 5); */
		/* aggre_test.run("./", 5); */
		
//		SolrClient solr = new SolrClient();
//		solr.connect(local_host, 8983);
//		solr.deleteAll();
//		solr.addTweets(t3);
//		solr.testQuery();
//		solr.tweetCount("text", "olympics");
	}
}
