package dbtest.mongodb;

import io.FileManager;
import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetFind;

public class MongoDBFindTest extends MongoDBTest {
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
	public MongoDBFindTest(DBDetails db_details) {
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
		System.out.println("----------- Double Keyword Results --------------");
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
		float time = 0;
		long count = 0;
		float start_time = (float) System.currentTimeMillis();
		switch(mode) {
			case 1:
				count = this.mongodb.regexFindTweetCount(keyword);
				System.out.println("REGEX Found: " + count);
				break;
			case 2:
				count = this.mongodb.matchFindTweetCount(keyword);
				System.out.println("Match Found: " + count);
				break;
			case 3:
				count = this.mongodb.arrayFindTweetCount(keyword);
				System.out.println("Array Found: " + count);
				break;
			case 4:
				count = this.mongodb.aggregateFindTweetCount(keyword);
				System.out.println("Aggregate Found: " + count);
				break;
		}
		time = ((float) System.currentTimeMillis() - start_time) / 60000;
		System.out.println("Time: " + time + " mins");
		return time;
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
		for (String keyword : single_keywords) {
			System.out.println("\n");
			System.out.println("Testing Keyword: " + keyword);
			
			// REAL RUN 
			regex_time = executeFind(1, keyword);
			match_time = executeFind(2, keyword);
			array_time = executeFind(3, keyword);
			aggregate_time = executeFind(4, keyword);
			
			// display results
			displaySingleKeywordResultsSummary(
					objects, 
					keyword, 
					regex_time, 
					match_time, 
					array_time,
					aggregate_time);
			System.out.println("\n");
			
			// log results
			String[] csv_line = {
					Long.toString(objects), // number of objects in collection
					keyword, // keyword
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
		for (String keyword : double_keywords) {
			System.out.println("Testing Keyword: " + keyword);
			// DUMMY RUN
			executeFind(1, keyword);
			
			// REAL RUN
			regex_time = executeFind(1, keyword);
			array_time = executeFind(3, keyword);
			
			displayDoubleKeywordResultsSummary(
					objects, 
					keyword, 
					regex_time, 
					array_time);
			
			// log results
			String[] csv_line = {
					Long.toString(objects), // number of objects in collection
					keyword, // keyword
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
		for (int i = 1; i <= repeat; i++) {
			System.out.println("Run number: " + Integer.toString(i));
			this.testSingleKeyword(
					res_path + "single_find_results_" + i + ".csv");
			this.testDoubleKeyword(
					res_path + "double_find_results_" + i + ".csv");
		}
	}
	
}
