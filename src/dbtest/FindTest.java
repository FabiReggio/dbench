package dbtest;

import io.FileManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetFind;

public class FindTest extends DBTest {
	// --- Fields ---
	private MongoDBTweetFind mongodb;
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
	public FindTest(DBDetails db_details) {
		super(db_details);
		MongoDBClient client = prepDB();
		this.mongodb = new MongoDBTweetFind(client);
	}
	
	// --- Methods ---
	/**
	 * Displays summary of query test results
	 */
	public void displayResultsSummary(
			long objects)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.println("----------------------------------------");
	}	
	
	/**
	 * Performs a series of find query and records how long does it take for 
	 * MongoDB to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void test(String res_path) 
	{
		FileManager file_manager = new FileManager();

		long objects; // number of objects in collection
		
		// prepare 
		prepResultsFile(file_manager, res_path, this.results_header);
		objects = this.mongodb.getCollectionCount();
		
		// run tests
		// SHARED URLS
		System.out.println("SHARED URLS");
		
		// display results
		displayResultsSummary(objects);
		System.out.printf("\n\n");
		
		// log results
		String[] csv_line = {
				Long.toString(objects), // number of objects in collection
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
	
}
