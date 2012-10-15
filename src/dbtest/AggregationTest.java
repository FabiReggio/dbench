package dbtest;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import io.FileManager;
import db.DBDetails;
import db.mongodb.MongoDBTweetAggregation;

public class AggregationTest extends DBTest 
{
	// --- Fields ---
	private MongoDBTweetAggregation mongodb;
	private String[] query_test_header = {
				"objects",
				"time bucket",
				"total tweets in bucket",
				"user mentions (mp)",
				"hash tags (mp)",
				"shared urls (mp)",
				"user mentions (aggre)",
				"hash tags (aggre)",
				"shared urls (aggre)"
	};
	
	// --- Constructors ---
	public QueryTest(DBDetails db_details)
	{
		super(db_details);
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
	public void displaySummaryQueryResults(
			long objects,
			float time_bucket,
			String total_tweets,
			float user_mentions_mptime,
			float hash_tags_mptime,
			float shared_urls_mptime,
			float user_mentions_atime,
			float hash_tags_atime,
			float shared_urls_atime)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.printf("time bucket execution time: %f mins \n", time_bucket);
		System.out.printf("total tweets in bucket: %s \n", total_tweets);
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
	 * Executes the query tests, it performs both Map-Reduce queries and
	 * Aggregate Framework queries.
	 * @param mode
	 * 		mode = 1: queries most user mentioned
	 * 		mode = 2: queries most hash tag used
	 * 		mode = 3: queries most shared url 
	 * @return
	 * 		ArrayList of Long, containing both Map-Reduce and Aggregate
	 * 		Framework execution time.
	 */
	public ArrayList<Long> executeTest(int mode) 
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
	public void run(String res_path) 
	{
		FileManager file_manager = new FileManager();

		float start_time = 0;
		long objects = 0; // number of objects in collection
		float tb_time = 0;
		String total_tweets = ""; // sum of tweets 
		ArrayList<Long> user_mentions_results = new ArrayList<Long>();
		ArrayList<Long> hash_tags_results = new ArrayList<Long>();
		ArrayList<Long> shared_urls_results = new ArrayList<Long>();
		
		// prepare 
		this.mongodb = prepDB("AGGREGATION");
		prepResultsFile(file_manager, res_path, this.query_test_header);
		objects = this.mongodb.getCollectionCount();
		
		// run tests
		// TIME BUCKET
		System.out.println("querying time bucket");
		start_time = System.currentTimeMillis();
		DBObject obj = this.mongodb.objectTimeBucket();
		tb_time = System.currentTimeMillis() - start_time;
		total_tweets = ((BasicBSONObject) obj.get("0")).get("sum").toString();
		
		// USER MENTIONS
		System.out.println("USER MENTIONS");
		user_mentions_results = executeTest(1);
		
		// HASH TAGS
		System.out.println("HASH TAGS");
		hash_tags_results = executeTest(2);
		
		// SHARED URLS
		System.out.println("SHARED URLS");
		shared_urls_results = executeTest(3);
		
		// display results
		displaySummaryQueryResults(
				objects,
				tb_time,
				total_tweets,
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
				Float.toString(tb_time), // time bucket execution time 
				total_tweets, // sum of time_bucket
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
	 * 
	 * 
	 */
	public void addKeywordField()
	{
		this.mongodb = this.prepDB();
		this.mongodb.addKeywordField("text");
	}

}
