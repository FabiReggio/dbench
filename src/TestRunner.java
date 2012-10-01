import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.mongodb.DBCursor;

import twitter4j.Tweet;
import unittests.MongoDBUnitTests;
import db.MongoDBAdaptor;
import io.FileManager;

public class TestRunner 
{
	// --- Fields ---
	private MongoDBAdaptor mongodb;
	private String db_host = "project06.cs.st-andrews.ac.uk";
	private int db_port = 27017;
	private String db_name = "testDB";
	private String collection = "test_collection";
	
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
	public void prepDB()
	{
		this.mongodb = new MongoDBAdaptor();
		this.mongodb.connect(db_host, db_port, db_name);
		this.mongodb.setCollection(collection);
		
		// remove everything in the test collection
		this.mongodb.removeAll();
	}
	
	/**
	 * Clean up database
	 */
	public void cleanUpDB()
	{
		this.mongodb.removeAll();
		this.mongodb.disconnect();
	}
	
	public ArrayList<Float> testIO(
			File data_file, 
			int lines_limit,
			String mode) 
	{
		int line_number = 0;
		float objects = 0; // objects inserted
		String line = "";
		float execution_time = 0;
		
		LineIterator line_iter;
		try {
			line_iter = FileUtils.lineIterator(data_file);		
			long start_time = System.currentTimeMillis();
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
						this.mongodb.remove(line);
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
	
	public ArrayList<Float> testQuery(File data_file) 
	{
		ArrayList<Float> find_res = new ArrayList<Float>();
//		this.mongodb.objectTimeBucket();
		
		displayQueryResults(this.mongodb.mapReduceUserMentions());
		displayQueryResults(this.mongodb.mapReduceHashTags());
		displayQueryResults(this.mongodb.mapReduceSharedUrls()); 
		
		return find_res;
	}
	
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
		System.out.printf("insert/sec: %f ms\n", inserted_per_msec);
		System.out.printf("remove/sec: %f ms\n", removed_per_msec);
		System.out.println("----------------------------------------");
	}
	
	/**
	 * Uses a single data file containing the JSON objects as the source 
	 * for the database tests. The test investigates:
	 * - insert time per JSON object
	 * - remove time per JSON object 
	 * 
	 * @param file_path
	 * @param res_path 
	 */
	public void singleFileTest(String file_path, String res_path) 
	{
		FileManager file_manager = new FileManager();
		ArrayList<Float> insert_res = new ArrayList<Float>();
		ArrayList<Float> remove_res = new ArrayList<Float>();
		ArrayList<Float> find_res = new ArrayList<Float>();
		
		try {
			// prepare the database for testing first
			prepDB();
			
			// prepare results file
			file_manager.prepFileWriter(res_path);
			String[] header_line = {
					"objects",
					"insert", 
					"remove", 
					"insert per msec",
					"remove per msec"
			};
			file_manager.csvLogEvent(header_line);
			
			// sleep for a bit before starting test
			// 60000 ms -> 1 mins * 60 seconds * 1000ms
//			System.out.println("Sleeping for 1 mins!");
//			Thread.currentThread().sleep(60000); 
			
			// processing file
			int num_lines = lineCount(file_path);
			System.out.println("processing file: " + file_path);
			System.out.println("number of lines: " + num_lines);
			
			// perform test at different percentile grades
			for (int i: range(1, 11)) { // go from 1 to 10
				float percentage = (float) (0.1 * i);
				int line_limit = (int) (num_lines * percentage);
				
				System.out.println("Start test!");
				insert_res = testIO(new File(file_path), line_limit, "insert");
				find_res = testQuery(new File(file_path));
				remove_res = testIO(new File(file_path), line_limit, "remove");
				
				
				
				
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
				
				// sleep for a bit before running the next test
				// 300000 ms -> 5 mins * 60 seconds * 1000ms
//				System.out.println("Sleeping for 5 mins!");
//				Thread.currentThread().sleep(300000); 
//				System.out.println("Right time to wake up!");
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
		} catch (IOException e) {
			System.out.println("error: " + e);
//		} catch (InterruptedException e) {
//			System.out.println("error: " + e);
		} finally {
			file_manager.closeFileWriter();
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
		String olympicsraw = "/home/jenkins/userContent/olympics3.raw";

		TestRunner tr = new TestRunner();
		
		for (int i: tr.range(1, 6)) { // repeat 5 times
			System.out.println("Run number: " + Integer.toString(i));
			tr.singleFileTest(t2, "results_" + i + ".dat");
		}
//		tr.singleFileTest(test, 5);
	}
}
