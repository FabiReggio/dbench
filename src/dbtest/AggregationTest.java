package dbtest;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import io.FileManager;
import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetAggregation;

public class AggregationTest extends DBTest 
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
	public AggregationTest(DBDetails db_details)
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
		System.out.printf("user mentions time: %f mins \n", user_mentions_mptime);
		System.out.printf("hash tags time: %f mins \n", hash_tags_mptime);
		System.out.printf("shared urls time: %f mins \n", shared_urls_mptime);
		System.out.println("----------- Aggregate Framework ---------");
		System.out.printf("user mentions time: %f mins \n", user_mentions_atime);
		System.out.printf("hash tags time: %f mins \n", hash_tags_atime);
		System.out.printf("shared urls time: %f mins \n", shared_urls_atime);
		System.out.println("----------------------------------------");
	}
	
	/**
	 * Performs the aggregation, it performs both Map-Reduce queries and
	 * Aggregate Framework queries.
	 * @param mode
	 * 		mode = 1: queries most user mentioned
	 * 		mode = 2: queries most hash tag used
	 * 		mode = 3: queries most shared url 
	 * @return
	 * 		ArrayList of Long, containing both Map-Reduce and Aggregate
	 * 		Framework execution time.
	 */
	public ArrayList<Long> executeAggregation(int mode) 
	{
		ArrayList<Long> results = new ArrayList<Long>();
		long start_time = 0;
		
		// map-reduce method
		System.out.println("map-reduce method");
		
		start_time = System.currentTimeMillis();
		switch(mode) {
			case 1:
				displayMPQueryResults(this.mongodb.mapReduceUserMentions());
				break;
			case 2:
				displayMPQueryResults(this.mongodb.mapReduceHashTags());
				break;
			case 3:
				displayMPQueryResults(this.mongodb.mapReduceSharedUrls());
				break;
		}
		results.add((System.currentTimeMillis() - start_time) / 60000);
		
		// aggregate method
		System.out.println("aggregate framework method");
		start_time = System.currentTimeMillis();
		switch(mode) {
			case 1:
				displayAggreQueryResults(this.mongodb.aggregateUserMentions());
				break;
			case 2:
				displayAggreQueryResults(this.mongodb.aggregateHashTags());
				break;
			case 3:
				displayAggreQueryResults(this.mongodb.aggregateSharedUrls());
				break;
		}
		results.add((System.currentTimeMillis() - start_time) / 60000);
		System.out.printf("\n\n");
		
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
		ArrayList<Long> user_mentions_results = new ArrayList<Long>();
		ArrayList<Long> hash_tags_results = new ArrayList<Long>();
		ArrayList<Long> shared_urls_results = new ArrayList<Long>();
		
		// prepare 
		prepResultsFile(file_manager, res_path, this.results_header);
		objects = this.mongodb.getCollectionCount();
		
		// run tests
		// USER MENTIONS
		System.out.println("USER MENTIONS");
		user_mentions_results = executeAggregation(1);
		
		// HASH TAGS
		System.out.println("HASH TAGS");
		hash_tags_results = executeAggregation(2);
		
		// SHARED URLS
		System.out.println("SHARED URLS");
		shared_urls_results = executeAggregation(3);
		
		// display results
		displaySummaryResults(
				objects,
				user_mentions_results.get(0),
				hash_tags_results.get(0),
				shared_urls_results.get(0),
				user_mentions_results.get(1),
				hash_tags_results.get(1),
				shared_urls_results.get(1)
				);
		System.out.printf("\n\n");
		
		// log results
		String[] csv_line = {
				Long.toString(objects), // number of objects in collection
				// map reduce
				Float.toString(user_mentions_results.get(0)),
				Float.toString(hash_tags_results.get(0)),
				Float.toString(shared_urls_results.get(0)),
				// aggregate framework
				Float.toString(user_mentions_results.get(1)),
				Float.toString(hash_tags_results.get(1)),
				Float.toString(shared_urls_results.get(1))
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
		for (int i = 0; i <= repeat; i++) {
			System.out.println("Run number: " + Integer.toString(i));
			this.test(res_path + "aggre_results_" + (i + 1) + ".csv");
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