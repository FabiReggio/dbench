import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.solr.common.SolrInputDocument;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import unittests.MongoDBUnitTests;

import db.DBDetails;
import db.couchbase.CouchBaseClient;
import db.mongodb.MongoDBClient;
import db.solr.SolrClient;
import dbtest.mongodb.MongoDBAggregationTest;
import dbtest.mongodb.MongoDBFindTest;
import dbtest.mongodb.MongoDBIOTest;

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
		
		CouchBaseClient couchbase = new CouchBaseClient();
		couchbase.connect("http://" + p6);
  	
		LineIterator line_iter;
		boolean test = false;
		try {
			line_iter = FileUtils.lineIterator(new File(x));
			while (line_iter.hasNext()) {
				String line = line_iter.next();
				
				// check first char of line
				try { if (line.charAt(0) == '{') test = true;
				} catch (IndexOutOfBoundsException e) {}
				
				if (test) 
					couchbase.insert(line);
			}
		} catch (IOException e) {}
		couchbase.disconnect();
		
//		SolrClient solr = new SolrClient();
//		solr.connect(local_host, 8983);
//		solr.deleteAll();
//		solr.addTweets(t3);
//		solr.testQuery();
//		solr.tweetCount("text", "olympics");
	}
}
