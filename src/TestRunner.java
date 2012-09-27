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

import unittests.MongoDBUnitTests;
import db.MongoDBAdaptor;
import io.FileManager;

public class TestRunner 
{
	// --- Fields ---
	private MongoDBAdaptor mongodb;
	private String db_host = "localhost";
	private int db_port = 27017;
	private String db_name = "test";
	private String collection = "test_collection";
	
	// --- Constructors ---
	public TestRunner() {}
	
	// --- Methods ---
	public void runUnitTests() 
    {
        System.out.println("running Unit Tests!");
        Result result = JUnitCore.runClasses(MongoDBUnitTests.class);
        for (Failure failure: result.getFailures()) {
            System.out.println(failure.toString());
        }
    }
	
	public void prepDB()
	{
		this.mongodb = new MongoDBAdaptor();
		this.mongodb.connect(db_host, db_port, db_name);
		this.mongodb.setCollection(collection);
		
		// remove everything in the test collection
		this.mongodb.removeAll();
	}
	
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
				
				// check line number and line
				if ((line_number == lines_limit)) {
					break;
				} else if (line.charAt(0) == '{') { 
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
	
	public void singleFileTest(String file_path) 
	{
		FileManager file_manager = new FileManager();
		ArrayList<Float> insert_res = new ArrayList<Float>();
		ArrayList<Float> remove_res = new ArrayList<Float>();
		try {
			
			// prepare the database for testing first
			prepDB();
			
			// prepare results file
			file_manager.prepFileWriter("results.dat");
			String[] header_line = {
					"objects",
					"insert", 
					"remove", 
					"insert per sec",
					"remove per sec"
			};
			file_manager.csvLogEvent(header_line);
			
			// processing file
			int num_lines = lineCount(file_path);
			System.out.println("processing file: " + file_path);
			System.out.println("number of lines: " + num_lines);
			
			// perform test at different percentile grades
			for (int i: range(1, 21)) { // go from 1 to 20
				float percentage = (float) (0.05 * i);
				int line_limit = (int) (num_lines * percentage);
				
				insert_res = testIO(new File(file_path), line_limit, "insert");
				remove_res = testIO(new File(file_path), line_limit, "remove");
				
				// calculate insert and remove per second
				float objects = insert_res.get(1);
				float obj_inserted_per_sec = objects / insert_res.get(0);
				float obj_removed_per_sec = objects / remove_res.get(0);
				
				// display results
				System.out.println("-------------- Results -----------------");
				System.out.printf("tested: %f \n", insert_res.get(1));
				System.out.printf("lines tested: %d \n", line_limit);
				System.out.printf("insert time: %f ms\n", insert_res.get(0));
				System.out.printf("remove time: %f ms\n", remove_res.get(0));
				System.out.printf("insert/sec: %f ms\n", obj_inserted_per_sec);
				System.out.printf("remove/sec: %f ms\n", obj_removed_per_sec);
				System.out.println("----------------------------------------");
				
				// log results 
				if (Float.isInfinite(obj_removed_per_sec)) 
					obj_removed_per_sec = 0;
				String[] csv_line = {
						Float.toString(insert_res.get(1)), // objects tested
						Float.toString(insert_res.get(0)), // insert time
						Float.toString(remove_res.get(0)), // remove time
						Float.toString(obj_inserted_per_sec), // obj per sec
						Float.toString(obj_removed_per_sec), // obj per sec
				};
				file_manager.csvLogEvent(csv_line);
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
		} catch (IOException e) {
			System.out.println("error: " + e);
		} finally {
			file_manager.closeFileWriter();
		}
	}
	
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
	
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;

	   return result;
	}
	
	// --- Main ---
	public static void main(String[] argv) {
		String test = "/datadisk1/home/chris/twitter_data/100meters.json";
		String test2 = "/datadisk1/home/chris/twitter_data/100meters.json.test";
		String olympicsraw = "/home/jenkins/userContent/olympics3.raw";

		TestRunner tr = new TestRunner();
		
//		tr.singleFileTest(test);
		tr.singleFileTest(test2);
	}
}
