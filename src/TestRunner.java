
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bson.BasicBSONObject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import unittests.MongoDBUnitTests;

import io.FileManager;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import db.MongoDBClient;

public class TestRunner 
{
	// --- Fields ---
	private MongoDBClient mongodb;
	private String db_name = "test";
	private String db_host = "project06.cs.st-andrews.ac.uk";
	private int db_port = 27017;
	
	private String local_db_name = "twitter";
	private String local_db_host = "localhost";
	private int local_db_port = 27017;
	
	private String io_collection = "io_test_collection";
	private String query_collection = "query_test_collection";
	
	// --- Constructors ---
	public TestRunner() {}
	
	// --- Methods ---
	/**
	 * Not to be confused with the database performance test, the unit 
	 * tests serve as a confirmation that an interface in communicating 
	 * with MongoDB does indeed work.
	 */
	public void runUnitTests() 
    {
        System.out.println("running Unit Tests!");
        Result result = JUnitCore.runClasses(MongoDBUnitTests.class);
        for (Failure failure: result.getFailures()) {
            System.out.println(failure.toString());
        }
    }
	
	/**
	 * Prepare database for tests
	 */
	public void prepDB(String collection, String mode)
	{
		if (mode.equals("remote")) {
			this.mongodb = new MongoDBClient();
			this.mongodb.connect(db_host, db_port, db_name);
			this.mongodb.setCollection(collection);
		} else if (mode.equals("local")) {
			this.mongodb = new MongoDBClient();
			this.mongodb.connect(local_db_host, local_db_port, local_db_name);
			this.mongodb.setCollection(collection);
		}
		
		this.mongodb.dropCollection("user_mentions");
		this.mongodb.dropCollection("hash_tags");
		this.mongodb.dropCollection("shared_urls");
	}
	
	/**
	 * Prepare results file for test
	 */
	public void prepResultsFile(FileManager fm, String res_path, String mode)
	{
		fm.prepFileWriter(res_path);
		String[] io_test_header = {
				"objects",
				"insert", 
				"remove", 
				"insert per msec",
				"remove per msec",
		};
		String[] query_test_header = {
				"objects",
				"time bucket",
				"total tweets in bucket",
				"user mentions",
				"hash tags",
				"shared urls"
		};
		
		if (mode.equals("IO TEST")) fm.csvLogEvent(io_test_header);
		else if (mode.equals("QUERY TEST")) fm.csvLogEvent(query_test_header);
	}
	
	/**
	 * Disconnect database
	 */
	public void closeDB()
	{
		this.mongodb.disconnect();
	}
	
	public ArrayList<Float> timeIO(
			String fp, 
			int lines_limit,
			String mode) 
	{
		File data_file = new File(fp);
		int line_number = 0;
		float objects = 0; // objects inserted
		String line = "";
		long start_time = 0;
		float execution_time = 0;
		
		LineIterator line_iter;
		try {
			line_iter = FileUtils.lineIterator(data_file);		
			start_time = System.currentTimeMillis();
			while (line_iter.hasNext()) {
				line = line_iter.next();
				boolean test = false;
				
				// check first char of line
				try { if (line.charAt(0) == '{') test = true;
				} catch (IndexOutOfBoundsException e) {}
				
				if ((line_number == lines_limit)) {
					break;
				} else if (test) { 
					if (mode.equals("insert"))
						this.mongodb.insert(line);
					else if (mode.equals("remove"))
//						this.mongodb.remove(line);
						this.mongodb.removeAll();
					objects += 1;
				}
				line_number += 1;
			}
			line_iter.close(); // close to reset the iterator  
			execution_time = System.currentTimeMillis() - start_time;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<Float>(Arrays.asList(execution_time, objects));
	}
	
	/**
	 * Displays IOtest Results
	 */
	public void displayIOResults(
			int line_limit,
			ArrayList<Float> insert_res,
			ArrayList<Float> remove_res,
			Float inserted_per_msec,
			Float removed_per_msec)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("tested: %f \n", insert_res.get(1));
		System.out.printf("lines tested: %d \n", line_limit);
		System.out.printf("insert time: %f ms\n", insert_res.get(0));
		System.out.printf("remove time: %f ms\n", remove_res.get(0));
		System.out.printf("insert/msec: %f \n", inserted_per_msec);
		System.out.printf("remove/msec: %f \n", removed_per_msec);
		System.out.println("----------------------------------------");
	}
	
	/**
	 * Uses a single data file containing the JSON objects as the source 
	 * for the database tests. The test investigates:
	 * - insert time per JSON object
	 * - remove time per JSON object 
	 * 
	 * @param file_path
	 * 			Data file path
	 * @param res_path 
	 * 			Results file path
	 * @param slice 
	 * 			Number of increments you wish to perform on the data file
	 * 			e.g. when slice is 5, that means there will be 5 increments
	 * 			starting initially at 20%, 40%, 60%... and finishing at 100%	
	 */
	public void runIOTest(String fp, String res_path, int slice, String mode) 
	{
		FileManager file_manager = new FileManager();
		ArrayList<Float> insert_res = new ArrayList<Float>();
		ArrayList<Float> remove_res = new ArrayList<Float>();
		
		try {
			// prepare 
			prepDB(this.io_collection, mode);
			prepResultsFile(file_manager, res_path, "IO TEST");
			
			// process data file
			int num_lines = lineCount(fp);
			System.out.println("processing file: " + fp);
			System.out.println("number of lines: " + num_lines);
			
			// perform test at different percentile grades
			float incre = 100 / (float) slice;
			for (float percent = incre; percent <= 100;  percent += incre) {
				int line_limit = (int) (num_lines * percent);
				
				System.out.println("Start test!");
				
				// INSERT
				System.out.println("performing insert");
				insert_res = timeIO(fp, line_limit, "insert");
				
				// FSYNC (by sleeping for 2 minutes for good measure)
				System.out.println("sleep for 2 minutes");
				sleep(2); // sleep 2 minutes
				
				// REMOVE
				System.out.println("performing remove all");
				remove_res = timeIO(fp, line_limit, "remove");
				
				// calculate insert and remove per second
				float objects = insert_res.get(1);
				float inserted_per_msec = objects / insert_res.get(0);
				float removed_per_msec = objects / remove_res.get(0);
				
				// display results
				displayIOResults(
						line_limit, 
						insert_res, 
						remove_res, 
						inserted_per_msec,
						removed_per_msec);
				
				// log results 
				String[] csv_line = {
						Float.toString(insert_res.get(1)), // objects tested
						Float.toString(insert_res.get(0)), // insert time
						Float.toString(remove_res.get(0)), // remove time
						Float.toString(inserted_per_msec), // obj per sec
						Float.toString(removed_per_msec), // obj per sec
				};
				file_manager.csvLogEvent(csv_line);
				
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
		} catch (IOException e) {
			System.out.println("error: " + e);
		} finally {
			file_manager.closeFileWriter();
			closeDB();
		}
	}
	
	/**
	 * Displays summary of query test results
	 */
	public void displaySummaryQueryResults(
			long objects,
			float time_bucket,
			String total_tweets,
			float user_mentions_time,
			float hash_tags_time,
			float shared_urls_time)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("objects queried: %d \n", objects);
		System.out.printf("time bucket execution time: %f ms \n", time_bucket);
		System.out.printf("total tweets in bucket: %s \n", total_tweets);
		System.out.printf("user mentions time: %f ms \n", user_mentions_time);
		System.out.printf("hash tags time: %f ms \n", hash_tags_time);
		System.out.printf("shared urls time: %f ms \n", shared_urls_time);
		System.out.println("----------------------------------------");
	}
	
	
	/**
	 * Displays the results returned from query
	 * @param cur
	 */
	public void displayQueryResults(DBCursor cur)
	{
		int count = 0;
		int limit = 5;
		while (cur.hasNext()) {
			if (count != limit) 
				System.out.println(cur.next().toString());
			else
				break;
			count++;
		}
	}
	

		
	/**
	 * Performs a series of query and records how long does it take for 
	 * MongoDB to return the results. 
	 * 
	 * WARNING: We assume there are already data in the database to query
	 * @return
	 */
	public void runQueryTest(String res_path, String mode) 
	{
		FileManager file_manager = new FileManager();

		float start_time = 0;
		long objects = 0; // number of objects in collection
		float tb_time = 0;
		String total_tweets = ""; // sum of tweets
		float user_mentions_time = 0;
		float hash_tags_time = 0;
		float shared_urls_time = 0;
		
		// prepare 
		prepDB(this.query_collection, mode);
		prepResultsFile(file_manager, res_path, "QUERY TEST");
		objects = this.mongodb.getCollectionCount();
			
		// run tests
		// TIME BUCKET
		System.out.println("querying time bucket");
		start_time = System.currentTimeMillis();
		DBObject obj = this.mongodb.objectTimeBucket();
		tb_time = System.currentTimeMillis() - start_time;
		total_tweets = ((BasicBSONObject) obj.get("0")).get("sum").toString();
		System.out.println(total_tweets);
		System.out.println("here");
	
		// USER MENTIONS
		System.out.println("here");
		System.out.println("USER MENTIONS");
		start_time = System.currentTimeMillis();
		displayQueryResults(this.mongodb.mapReduceUserMentions());
		user_mentions_time = System.currentTimeMillis() - start_time;
		System.out.println("here");
//		
//		// HASH TAGS
//		System.out.println("HASH TAGS");
//		start_time = System.currentTimeMillis();
//		displayQueryResults(this.mongodb.mapReduceHashTags());
//		hash_tags_time = System.currentTimeMillis() - start_time;
//		
//		// SHARED URLS
//		System.out.println("SHARED URLS");
//		start_time = System.currentTimeMillis();
//		displayQueryResults(this.mongodb.mapReduceSharedUrls()); 
//		shared_urls_time = System.currentTimeMillis() - start_time; 
			
		// display results
		displaySummaryQueryResults(
				objects,
				tb_time,
				total_tweets,
				user_mentions_time,
				hash_tags_time,
				shared_urls_time
				);
		
		// log results
		String[] csv_line = {
				Long.toString(objects), // number of objects in collection
				Float.toString(tb_time), // time bucket execution time 
				total_tweets, // sum of time_bucket
				Float.toString(user_mentions_time),
				Float.toString(hash_tags_time),
				Float.toString(shared_urls_time)
		};
		file_manager.csvLogEvent(csv_line);
		
		// close
		file_manager.closeFileWriter();
	}

	
	/**
	 * Sleep
	 * @param minutes
	 */
	public void sleep(int minutes) 
	{
		try {
			Thread.currentThread();
			Thread.sleep(60000 * minutes);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Returns the number of lines the file may have
	 * @param filename
	 * @return An integer of number of lines
	 * @throws IOException
	 */
	public int lineCount(String filename) throws IOException {
		FileInputStream fs = new FileInputStream(filename);
	    InputStream is = new BufferedInputStream(fs);
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	/**
	 * Similar to Python's range function
	 * @param start
	 * @param stop
	 * @return An array of integers starting from start to stop defined
	 */
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];
	   
	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;
	   
	   return result;
	}
	
	// --- Main ---
	public static void main(String[] argv) {
		String t = "/datadisk1/home/chris/twitter_data/100meters.json";
		String t2 = "/datadisk1/home/chris/twitter_data/100meters.json.test";
		String t3 = "/datadisk1/userContent/olympics3.jsonl";

		TestRunner tr = new TestRunner();
		
		for (int i: tr.range(1, 6)) { // repeat 5 times
			System.out.println("Run number: " + Integer.toString(i));
//			tr.runIOTest(t3, "io_results_" + i + ".dat", 1, "remote");
			tr.runQueryTest("query_results_" + i + ".dat", "remote");
//			tr.runQueryTest("query_results_" + i + ".dat", "local");
		}
	}
}
