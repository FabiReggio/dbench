package dbtest.mongodb;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import io.FileManager;
import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetAggregation;

public class MongoDBAggregationTest extends MongoDBTest 
{
	// --- Fields ---
	private MongoDBTweetAggregation mongodb;
	private String[] results_header = {
				"run",
				"user mentions (ms)",
				"hash tags (ms)",
				"shared urls (ms)"
	};
	
	// --- Constructors ---
	public MongoDBAggregationTest(DBDetails db_details)
	{
		super(db_details);
		MongoDBClient client = prepDB();
		this.mongodb = new MongoDBTweetAggregation(client);
	}
	
	// --- Methods ---
	/**
	 * Displays the results returned from map-reduce queries
	 * @param cur
	 */
	public void displayMPQueryResults(DBCursor cur)
	{
		int count = 0;
		int limit = 6;
		while (cur.hasNext()) {
			if (count == limit) break;
			else System.out.println(cur.next().toString());
			count++;
		}
	}
	
	/**
	 * Displays the results returned from aggregate queries
	 * @param cur
	 */
	public void displayAggreQueryResults(Iterable<DBObject> iter)
	{
		int count = 0;
		int limit = 6;
		for (DBObject obj : iter) {
			if (count == limit) break;
			else System.out.println(obj.toString());
			count++;
		}
	}
	
	/**
	 * Displays summary of query test results
	 */
	public void displaySummaryResults(
			long objects,
			long user_mentions_time,
			long hash_tags_time,
			long shared_urls_time)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("objects aggregated: %d \n", objects);
		System.out.printf("user mentions: %d ms \n", user_mentions_time);
		System.out.printf("hash tags: %d ms \n", hash_tags_time);
		System.out.printf("shared urls: %d ms \n", shared_urls_time);
		System.out.println("----------------------------------------");
	}
	
	/**
	 * Performs the aggregation, it performs both Map-Reduce queries and
	 * Aggregate Framework queries, on the following (We assume you have 
	 * twitter data in MongoDB already).
	 * 
	 * - Most User Mentioned
	 * - Most Hashed Tags
	 * - Most Shared URLs
	 * 
	 * @param mode
	 * 		mode = 1: Map-Reduce
	 * 		mode = 2: Aggregate Framework
	 * @return
	 * 		ArrayList of Float, containing both Map-Reduce and Aggregate
	 * 		Framework execution time.
	 */
	public ArrayList<Long> executeAggregation(int mode) 
	{
		ArrayList<Long> results = new ArrayList<Long>();
		long start = 0; // start time
		long time = 0;
		
		switch(mode) {
			case 1:
				System.out.println("map-reduce method");
				
				// most user mentions
				System.out.println("Most User Mentions");
				start = System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceUserMentions());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				// most hash-tags
				System.out.println("Most Hash Tags");
				start = System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceHashTags());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				// most shared urls 
				System.out.println("Most Shared URLs");
				start = System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceSharedUrls());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				break;
			case 2:
				System.out.println("aggregate framework method");
				
				// most user mentions
				System.out.println("Most User Mentions");
				start = System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateUserMentions());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				// most hash-tags
				System.out.println("Most Hash Tags");
				start = System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateHashTags());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				// most shared urls
				System.out.println("Most Shared URLs");
				start = System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateSharedUrls());
				time = System.currentTimeMillis() - start;
				results.add(time);
				
				break;
		}
		
		return results;
	}
	
	/**
	 * Performs a series of query and records how long does it take for 
	 * MongoDB to return the results. 
	 * @param res_path
	 * 		Path to save results
	 * @param mode
	 * 		Mode can be "map-reduce" or "aggregation framework" 
	 * @param fm
	 * 		FileManager to which to write results to
	 * @param run
	 * 		Run number
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	private void test(String mode, FileManager fm, int run) 
	{
		long objects = 0; // number of objects in collection
		ArrayList<Long> test_results = new ArrayList<Long>();
		
		// run tests
		if (mode.equals("map-reduce")) {
			test_results = executeAggregation(1);
		} else if (mode.equals("aggregation framework")){
			test_results = executeAggregation(2);
		}
		
		// display results
		displaySummaryResults(
				objects,
				test_results.get(0), // user mentions
				test_results.get(1), // hash tags
				test_results.get(2)); // shared urls 
		System.out.printf("\n\n");
		
		// log results
		String[] csv_line = {
				Integer.toString(run), // number of objects in collection
				Long.toString(test_results.get(0)), // user mentions
				Long.toString(test_results.get(1)), // hash tags
				Long.toString(test_results.get(2)), // shared urls 
		};
		fm.csvLogEvent(csv_line);
		
	}
	
	/**
	 * Execute the test
	 * @param res_path
	 * 		Results path
	 * @param repeat
	 * 		Number of times to repeat
	 * @param mode
	 * 		Mode can be "map-reduce" or "aggregation framework" 
	 */
	public void run(String res_path, int repeat, String mode) 
	{
		String fpath = "";
		
		if (mode.equals("map-reduce")) {
			fpath = res_path + "aggre_results_map-reduce.csv";
		} else if (mode.equals("aggregation framework")) {
			fpath = res_path + "aggre_results_aggre_framework).csv";
		} else {
			System.out.println("Error! invalid mode chosen!");
			System.exit(-1);
		}
		
		// prep results file
		FileManager file_manager = new FileManager();
		prepResultsFile(file_manager, fpath, this.results_header);
		
		for (int i = 1; i <= repeat; i++) {
			System.out.println("Run number: " + Integer.toString(i));
			this.test(mode, file_manager, i);
		}
		
		// close results file
		file_manager.closeFileWriter();
	}
	
	/**
	 * Adds a new keyword field called "_keywords" to all documents in 
	 * collection
	 */
	public void addKeywordField()
	{
		this.mongodb.addKeywordField("text");
	}
}
