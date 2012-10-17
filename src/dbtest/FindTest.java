package dbtest;

import java.util.ArrayList;

import io.FileManager;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetFind;

public class FindTest extends DBTest {
	// --- Fields ---
	private MongoDBTweetFind mongodb;
	private String[] single_results_header = {
				"objects",
				"keyword",
				"regex",
				"match",
				"array",
				"aggregate",
	};
	private String[] double_results_header = {
				"objects",
				"keyword",
				"regex",
				"array",
	};
	private String[] single_keywords = {
				"Olympics",
				"London2012",
				"TeamGB",
				"TeamUSA",
				"Chris",
				"Hoy",
	};
	private String[] double_keywords = {
				"Chris Hoy",
				"Tom Daley",
				"Michael Phelps"
	};
	
	// --- Constructors ---
	public FindTest(DBDetails db_details) {
		super(db_details);
		MongoDBClient client = prepDB();
		this.mongodb = new MongoDBTweetFind(client);
	}
	
	// --- Methods ---
	/**
	 * Displays summary of query test results
	 */
	public void displaySingleKeywordResultsSummary(
			long objects,
			String keyword,
			float regex,
			float match,
			float array,
			float aggregate)
	{
		System.out.println("----------- Single Keyword Results --------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.printf("keyword: %s \n", keyword);
		System.out.printf("regex find: %f \n", regex);
		System.out.printf("match find: %f \n", match);
		System.out.printf("array find: %f \n", array);
		System.out.printf("aggregate find: %f \n", aggregate);
		System.out.println("-------------------------------------------------");
	}	
	
	/**
	 * Displays summary of query test results
	 */
	public void displayDoubleKeywordResultsSummary(
			long objects,
			String keyword,
			float regex,
			float array)
	{
		System.out.println("----------- Single Keyword Results --------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.printf("keyword: %s \n", keyword);
		System.out.printf("regex find: %f \n", regex);
		System.out.printf("array find: %f \n", array);
		System.out.println("-------------------------------------------------");
	}	
	
	/**
	 * Performs find queries, it performs both simple find queries and find 
	 * using the Aggregate Framework.
	 * @param mode
	 * 		mode = 1: REGEX find
	 * 		mode = 2: Match find 
	 * 		mode = 3: Array $all find
	 * 		mode = 4: Aggregate Framework find
	 * @return
	 * 		ArrayList of Long, containing both Map-Reduce and Aggregate
	 * 		Framework execution time.
	 */
	public Float executeFind(int mode, String keyword) 
	{
		long start_time = 0;
		start_time = System.currentTimeMillis();
		switch(mode) {
			case 1:
				this.mongodb.regexFindTweetCount(keyword);
				break;
			case 2:
				this.mongodb.matchFindTweetCount(keyword);
				break;
			case 3:
				this.mongodb.arrayFindTweetCount(keyword);
				break;
			case 4:
				this.mongodb.aggregateFindTweetCount(keyword);
				break;
		}
		return (float) ((System.currentTimeMillis() - start_time) / 60000.0);
	}
	
	/**
	 * Performs a series of find query and records how long does it take for 
	 * MongoDB to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void testSingleKeyword(String res_path) 
	{
		FileManager file_manager = new FileManager();

		long objects = 0; // number of objects in collection
		float regex_time = 0;
		float match_time = 0;
		float array_time = 0;
		float aggregate_time = 0;
		
		// prepare 
		prepResultsFile(file_manager, res_path, this.single_results_header);
		objects = this.mongodb.getCollectionCount();
		
		// run test
		// SINGLE KEYWORD
		for (String s : single_keywords) {
			regex_time = executeFind(1, s);
			match_time = executeFind(2, s);
			array_time = executeFind(3, s);
			aggregate_time = executeFind(4, s);
			
			// display results
			displaySingleKeywordResultsSummary(
					objects, 
					s, // keyword
					regex_time, 
					match_time, 
					array_time,
					aggregate_time);
			
			// log results
			String[] csv_line = {
					Long.toString(objects), // number of objects in collection
					s, // keyword
					Float.toString(regex_time),
					Float.toString(match_time),
					Float.toString(array_time),
					Float.toString(aggregate_time)
			};
			file_manager.csvLogEvent(csv_line);
		}
		
		// close
		file_manager.closeFileWriter();
	}
	
	/**
	 * Performs a series of find query and records how long does it take for 
	 * MongoDB to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void testDoubleKeyword(String res_path) {
		FileManager file_manager = new FileManager();

		long objects = 0; // number of objects in collection
		float regex_time = 0;
		float array_time = 0;
		
		// prepare 
		prepResultsFile(file_manager, res_path, this.double_results_header);
		objects = this.mongodb.getCollectionCount();
		
		// run test
		// DOUBLE KEYWORD
		for (String s : double_keywords) {
			regex_time = executeFind(1, s);
			array_time = executeFind(3, s);
			
			displayDoubleKeywordResultsSummary(
					objects, 
					s, // keyword
					regex_time, 
					array_time);
			
			// log results
			String[] csv_line = {
					Long.toString(objects), // number of objects in collection
					s, // keyword
					Float.toString(regex_time),
					Float.toString(array_time),
			};
			file_manager.csvLogEvent(csv_line);
		}
		
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
			this.testSingleKeyword(
					res_path + "single_find_results_" + (i + 1) + ".csv");
			this.testDoubleKeyword(
					res_path + "double_find_results_" + (i + 1) + ".csv");
		}
	}
	
}