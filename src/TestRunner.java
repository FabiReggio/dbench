import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.solr.common.SolrInputDocument;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.ViewResponse;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import unittests.MongoDBUnitTests;

import db.DBDetails;
import db.couchbase.CouchbaseTweetAggregation;
import db.couchbase.CustomCouchbaseClient;
import db.couchbase.CustomCouchbaseClient;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBUtils;
import db.solr.SolrClient;
import dbtest.couchbase.CouchbaseAggregationTest;
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

	// --- Main ---
	public static void main(String[] argv)
	{
		String db_name = "db_tests";
		String avoss = "avoss-cloud.cs.st-andrews.ac.uk";
		String p6 = "project06.cs.st-andrews.ac.uk";
		String p7 = "project07.cs.st-andrews.ac.uk";
		String local_host = "http://localhost";
		int db_port = 27017;

		String io_col = "io_test_collection";
		String q_col = "query_test_collection";
		String test_col = "test";
		String data_file = "../data/olympics3.jsonl";

        aggregationTest(
                "mongodb", 
				"localhost",
                "db_tests",
                "query_test_collection",
                "results/mongodb/raw_results/aggre_test/single_gup2/mp/",
                "map-reduce");

        aggregationTest(
                "mongodb", 
				"localhost",
                "db_tests",
                "query_test_collection",
                "results/mongodb/raw_results/aggre_test/single_gup2/aggre/",
                "aggregation framework");
	    
//		MongoDBClient mongo = new MongoDBClient();
//		mongo.connect("e-research.cs.st-andrews.ac.uk", db_port, db_name);
//		mongo.setCollection(test_col);
//		MongoDBUtils mongo_utils = new MongoDBUtils(mongo);
//		mongo_utils.createGroupField(2);
//		mongo.disconnect();
//

//        String host = "http://avoss-cloud.cs.st-andrews.ac.uk";
//        String bucket = "db_tests";
//        CustomCouchbaseClient couchbase = new CustomCouchbaseClient(host, bucket);
//		String line = "";
//		LineIterator line_iter;
//		try {
//			line_iter = FileUtils.lineIterator(new File(data_file));
//			while (line_iter.hasNext()) {
//				line = line_iter.next();
//				boolean test = false;
//
//				// check first char of line
//				try { if (line.charAt(0) == '{') test = true;
//				} catch (IndexOutOfBoundsException e) {}
//
//				if (test) {
//						couchbase.insert(line);
//				}
//			}
//			line_iter.close(); // reset the iterator by closing
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		couchbase.disconnect();

//		String doc_path = "./config/couchbase/most_user_mentioned.json";
//		String doc;
//		try {
//			doc = readFileToString(new File(doc_path)).replace("\n", "");
//			System.out.println(doc);
//			couchbase.loadDesignDoc(doc, "db_tests", "dev_test");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		SolrClient solr = new SolrClient();
//		solr.connect(local_host, 8983);
//		solr.deleteAll();
//		solr.addTweets(t3);
//		solr.testQuery();
//		solr.tweetCount("text", "olympics");
	}
}
