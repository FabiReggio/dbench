import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import unittests.MongoDBUnitTests;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import dbtest.IOTest;
import dbtest.AggregationTest;

/**
 * TestRunner as the name suggests is where the tests are executed from
 * @author chris choi 
 *
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
	 * with MongoDB does indeed work.
	 */
	public void runUnitTests() 
    {
        System.out.println("running Unit Tests!");
        Result result = JUnitCore.runClasses(MongoDBUnitTests.class);
        for (Failure failure: result.getFailures()) {
            System.out.println(failure.toString());
        }
    }	
	
	/**
	 * Similar to Python's range function
	 * @param start
	 * @param stop
	 * @return An array of integers starting from start to stop defined
	 */
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];
	   
	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;
	   
	   return result;
	}

	// --- Main ---
	public static void main(String[] argv) {
		String db_name = "db_tests";
		String db_host1 = "project06.cs.st-andrews.ac.uk";
		String db_host2 = "project07.cs.st-andrews.ac.uk";
		int db_port = 27017;
		
		String io_col = "io_test_collection";
		String q_col = "query_test_collection";
		String t = "/datadisk1/home/chris/twitter_data/100meters.json";
		String t2 = "/datadisk1/home/chris/twitter_data/100meters.json.test";
		String t3 = "/datadisk1/userContent/olympics3.jsonl";
		
//		DBDetails host_1 = new DBDetails(db_host1, db_port, db_name, q_col);
		DBDetails host = new DBDetails(db_host2, db_port, db_name, q_col);

		TestRunner tr = new TestRunner();
//		IOTests io_tests = new IOTests(io_test);
		AggregationTest aggregation_test = new AggregationTest(host);
		
		
	}
}
