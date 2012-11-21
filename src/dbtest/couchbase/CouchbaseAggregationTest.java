package dbtest.couchbase;

import io.FileManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import db.couchbase.CouchbaseTweetAggregation;
import db.couchbase.CustomCouchbaseClient;

public class CouchbaseAggregationTest
{
    // --- Fields ---
	private CouchbaseTweetAggregation couchbase;
	private CustomCouchbaseClient couchbase_client;
	private String[] results_header = {
				"user mentions (ms)",
				"hash tags (ms)",
				"shared urls (ms)",
	};

    // --- Constructors ---
    public CouchbaseAggregationTest(String host, String bucket) 
    {
    	this.couchbase_client = new CustomCouchbaseClient(host, bucket);
    	this.couchbase = new CouchbaseTweetAggregation(this.couchbase_client);
    }

    // --- Methods ---
	/**
	 * Display query results 
	 * 
	 * @param result
	 */
	public void printViewResponse(LinkedHashMap<String, Integer> sorted_map) 
	{
		System.out.println("----------------------------------------");
        int count = 0;
        for (Map.Entry<String, Integer> entry : sorted_map.entrySet()) {
            if (count == 5) break;
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + ":" + value);
            count++;
        }
	}

	/**
	 * Displays summary of query test results
	 */
	public void displaySummaryResults(
			long user_mentioned,
			long hashed_tags,
			long shared_urls)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("user mentions: %d ms \n", user_mentioned);
		System.out.printf("hash tags: %d ms \n", hashed_tags);
		System.out.printf("shared urls: %d ms \n", shared_urls);
		System.out.println("----------------------------------------");
	}

	/**
	 * Performs the aggregation, it performs both Map-Reduce queries and
	 * Aggregate Framework queries, on the following (We assume you have 
	 * twitter data in couchbase already).
	 * 
	 * - Most User Mentioned
	 * - Most Hashed Tags
	 * - Most Shared URLs
	 * 
	 * @return
	 * 		ArrayList of execution time.
	 */
	public ArrayList<Long> executeAggregation() 
	{
		ArrayList<Long> results = new ArrayList<Long>();
		long start = 0; // start time
		long time = 0; // total execution time
		
        // most user mentions
        System.out.println("Most User Mentioned");
        start = System.currentTimeMillis();
        printViewResponse(this.couchbase.mostUserMentioned());
        time = System.currentTimeMillis() - start;
        results.add(time);
        
        // most hash-tags
        System.out.println("Most Hashed Tags");
        start = System.currentTimeMillis();
        printViewResponse(this.couchbase.mostHashedTags());
        time = System.currentTimeMillis() - start;
        results.add(time);
        
        // most shared urls 
        System.out.println("Most Shared URLs");
        start = System.currentTimeMillis();
        printViewResponse(this.couchbase.mostSharedUrls());
        time = System.currentTimeMillis() - start;
        results.add(time);
        
		return results;
	}
	
	/**
	 * Performs a series of query and records how long does it take for 
	 * Couchbase to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void test(String res_path) 
	{
		FileManager file_manager = new FileManager();

		ArrayList<Long> results = new ArrayList<Long>();
		
		// prepare 
		file_manager.prepFileWriter(res_path);
		file_manager.csvLogEvent(this.results_header);
		
		// run tests
		results = executeAggregation();
		
		// display results
		displaySummaryResults(
				results.get(0), // user mentions
				results.get(1), // hash tags
				results.get(2)); // shared urls 
				
		System.out.printf("\n\n");
		
		// log results
		String[] csv_line = {
				// user mentions
				Long.toString(results.get(0)), 
				// hash tags
				Long.toString(results.get(1)), 
				// shared urls 
				Long.toString(results.get(2)), 
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
		couchbase_client.disconnect();
	}
}
