import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import unittests.MongoDBUnitTests;
import db.MongoDBAdaptor;
import db.IMongoDBAdaptor;
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
	public TestRunner() 
	{
//	    runUnitTests();
	}
	
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
	}
	
	public long testInsert(String document) 
	{
		long start_time = System.currentTimeMillis();
		this.mongodb.insert(document);
		long execution_time = System.currentTimeMillis() - start_time;
		return execution_time;
	}
	
	public long testRemove()
	{
		long start_time = System.currentTimeMillis();
		this.mongodb.removeAll();
		long execution_time = System.currentTimeMillis() - start_time;
		return execution_time;
	}
	
	public void singleFileTest(String file_path) 
	{
		try {
			File data_file = new File(file_path);
			
			// prepare the database for testing first
			prepDB();
			
			// processing file
			System.out.println("processing file: " + file_path);
			int num_lines = lineCount(file_path);
			System.out.println("number of lines: " + num_lines);
			
			// perform test at different percentile grades
			for (int i: range(1, 11)) { // go from 1 to 10
				long insert_time = 0;
				long remove_time = 0;
				long objects = 0; // number of objs inserted
				float percentage = (float) (0.1 * i);
				int lines_to_process = (int) (num_lines * percentage);
				
				// insert test 
				int line_number = 0;
				String line = "";
				LineIterator line_iter = FileUtils.lineIterator(data_file);
				while (line_iter.hasNext()) {
					line = line_iter.next();
					
					// check line number and line
					if ((line_number == lines_to_process)) {
						break;
					} else if (line.charAt(0) == '{') { 
						insert_time += testInsert(line); // perform test
						objects += 1;
					}
					line_number += 1;
				}
				line_iter.close(); // close to reset the iterator  
				
				// remove test 
				remove_time = testRemove();
				
				// display results
				System.out.println("--------------- Results -----------------");
				System.out.printf("tested: %d \n", objects);
				System.out.printf("lines tested: %d \n", line_number);
				System.out.printf("insert time: %d ms\n", insert_time);
				System.out.printf("remove time: %d ms\n", remove_time);
				System.out.println("-----------------------------------------");
				System.out.printf("collection count: %d \n", this.mongodb.getCollectionCount());
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
		} catch (IOException e) {
			System.out.println("error: " + e);
		}
	}
	
	public void testRunnerMultipleFiles(String dir_path) 
	{
		try {
			File dir = new File(dir_path);
			File[] file_list = dir.listFiles(); 
			
			for (File file: file_list) {
				System.out.println("processing file: " + file.getName());
				if (file.isFile()) {
					singleFileTest(file.getAbsolutePath());
				}
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
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
		TestRunner tr = new TestRunner();
		
		String bbc = "/datadisk1/home/chris/twitter_data/bbc/";
		String olympics = "/datadisk1/home/chris/twitter_data/100meters.json.test";
		
//		tr.testRunnerMultipleFiles(bbc);
		tr.singleFileTest(olympics);
	}
}
