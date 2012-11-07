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
				"objects",
				"user mentions (mp)",
				"hash tags (mp)",
				"shared urls (mp)",
				"user mentions (aggre)",
				"hash tags (aggre)",
				"shared urls (aggre)"
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
			float user_mentions_mptime,
			float hash_tags_mptime,
			float shared_urls_mptime,
			float user_mentions_atime,
			float hash_tags_atime,
			float shared_urls_atime)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.println("-------------- Map-Reduce ---------------");
		System.out.printf("user mentions: %f mins \n", user_mentions_mptime);
		System.out.printf("hash tags: %f mins \n", hash_tags_mptime);
		System.out.printf("shared urls: %f mins \n", shared_urls_mptime);
		System.out.println("----------- Aggregate Framework ---------");
		System.out.printf("user mentions: %f mins \n", user_mentions_atime);
		System.out.printf("hash tags: %f mins \n", hash_tags_atime);
		System.out.printf("shared urls: %f mins \n", shared_urls_atime);
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
	public ArrayList<Float> executeAggregation(int mode) 
	{
		ArrayList<Float> results = new ArrayList<Float>();
		float start = 0; // start time
		float time = 0;
		
		switch(mode) {
			case 1:
				System.out.println("map-reduce method");
				
				// most user mentions
				System.out.println("Most User Mentions");
				start = (float) System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceUserMentions());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				// most hash-tags
				System.out.println("Most Hash Tags");
				start = (float) System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceHashTags());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				// most shared urls 
				System.out.println("Most Shared URLs");
				start = (float) System.currentTimeMillis();
				displayMPQueryResults(this.mongodb.mapReduceSharedUrls());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				break;
			case 2:
				System.out.println("aggregate framework method");
				
				// most user mentions
				System.out.println("Most User Mentions");
				start = (float) System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateUserMentions());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				// most hash-tags
				System.out.println("Most Hash Tags");
				start = (float) System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateHashTags());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				// most shared urls
				System.out.println("Most Shared URLs");
				start = (float) System.currentTimeMillis();
				displayAggreQueryResults(this.mongodb.aggregateSharedUrls());
				time = ((float) System.currentTimeMillis() - start) / 60000;
				results.add(time);
				
				break;
		}
		
		return results;
	}
	
	/**
	 * Performs a series of query and records how long does it take for 
	 * MongoDB to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void test(String res_path) 
	{
		FileManager file_manager = new FileManager();

		long objects = 0; // number of objects in collection
		ArrayList<Float> map_reduce_results = new ArrayList<Float>();
		ArrayList<Float> aggregate_framework_results = new ArrayList<Float>();
		
		// prepare 
		prepResultsFile(file_manager, res_path, this.results_header);
		objects = this.mongodb.getCollectionCount();
		
		// run tests
		map_reduce_results = executeAggregation(1);
		aggregate_framework_results = executeAggregation(2);
		
		// display results
		displaySummaryResults(
				objects,
				map_reduce_results.get(0), // user mentions
				map_reduce_results.get(1), // hash tags
				map_reduce_results.get(2), // shared urls 
				aggregate_framework_results.get(0), // user mentions
				aggregate_framework_results.get(1), // hash tags
				aggregate_framework_results.get(2) // shared urls
				);
		System.out.printf("\n\n");
		
		// log results
		String[] csv_line = {
				Long.toString(objects), // number of objects in collection
				// MAP-REDUCE
				// user mentions
				Float.toString(map_reduce_results.get(0)), 
				// hash tags
				Float.toString(map_reduce_results.get(1)), 
				// shared urls 
				Float.toString(map_reduce_results.get(2)), 
				// AGGREGATE FRAMEWORK
				// user mentions
				Float.toString(aggregate_framework_results.get(0)),
				// hash tags
				Float.toString(aggregate_framework_results.get(1)),
				// shared urls 
				Float.toString(aggregate_framework_results.get(2)) 
		};
		file_manager.csvLogEvent(csv_line);
		
		// close
		file_manager.closeFileWriter();
	}
	
	/**
	 * Execute the test
	 * @param res_path
	 * @param repeat
	 */
	public void run(String res_path, int repeat) 
	{
		for (int i = 1; i <= repeat; i++) {
			System.out.println("Run number: " + Integer.toString(i));
			this.test(res_path + "aggre_results_" + i + ".csv");
		}
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