package dbtest;

import io.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import db.DBDetails;
import db.mongodb.MongoDBClient;

public class MongoDBIOTest extends MongoDBTest
{
	// --- Fields ---
	private MongoDBClient mongodb;
	private String[] io_test_header = {
			"objects",
			"insert", 
			"remove", 
			"insert per min",
			"remove per min"
	};
	
	// --- Constructors ---
	public MongoDBIOTest(DBDetails db_details)
	{
		super(db_details);
		this.mongodb = prepDB();
	}
	
	// --- Methods ---
	/**
	 * Displays IOtest Results
	 */
	public void displayIOResults(
			int line_limit,
			ArrayList<Float> insert_res,
			ArrayList<Float> remove_res,
			Float insert_rate,
			Float remove_rate)
	{
		System.out.println("-------------- Results -----------------");
		System.out.printf("tested: %f \n", insert_res.get(1));
		System.out.printf("lines tested: %d \n", line_limit);
		System.out.printf("insert time: %f ms\n", insert_res.get(0));
		System.out.printf("remove time: %f ms\n", remove_res.get(0));
		System.out.printf("insert/ms: %f \n", insert_rate);
		System.out.printf("remove/ms: %f \n", remove_rate);
	}
	
	/**
	 * 
	 * @param fp
	 * @param lines_limit
	 * @param mode
	 * @return
	 */
	public ArrayList<Float> executeIO(
			String fp, 
			int lines_limit,
			String mode) 
	{
		File data_file = new File(fp);
		int line_number = 0;
		float objects = 0; // objects inserted
		String line = "";
		long start = 0;
		float time = 0;
		
		LineIterator line_iter;
		try {
			line_iter = FileUtils.lineIterator(data_file);		
			start = System.currentTimeMillis();
			while (line_iter.hasNext()) {
				line = line_iter.next();
				boolean test = false;

				// check first char of line
				try { if (line.charAt(0) == '{') test = true;
				} catch (IndexOutOfBoundsException e) {}
				
				if (line_number == lines_limit) {
                    break;
                } else if (test) { 
					if (mode.equals("insert")) 
						this.mongodb.insert(line);
					else if (mode.equals("remove")) 
						this.mongodb.removeAll();
					objects += 1;
				}

				line_number += 1;
			}
			line_iter.close(); // reset the iterator by closing 
			time = System.currentTimeMillis() - start;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<Float>(Arrays.asList(time, objects));
	}
	
	/**
	 * Uses a single data file containing the JSON objects as the source 
	 * for the database tests. The test investigates:
	 * - insert time per JSON object
	 * - remove time per JSON object 
	 * 
	 * @param data_file
	 * 			Data file path
	 * @param res_path 
	 * 			Results file path
	 */
	public void test(String data_file, String res_path) 
	{
		FileManager file_manager = new FileManager();
		ArrayList<Float> insert_res = new ArrayList<Float>();
		ArrayList<Float> remove_res = new ArrayList<Float>();
		
		try {
			// prepare 
			prepResultsFile(file_manager, res_path, this.io_test_header);
			
			// process data file
			int num_lines = lineCount(data_file);
			System.out.println("processing file: " + data_file);
			System.out.println("number of lines: " + num_lines);
			
			// perform test at different percentile grades
			for (double percent = 0.1; percent <= 1;  percent += 0.1) {
				int line_limit = (int) (num_lines * percent);

				System.out.println("");
				System.out.println("");
				System.out.println("");
				
				// INSERT
				System.out.println("performing individual insert");
				insert_res = executeIO(data_file, line_limit, "insert");
				
				// FSYNC (by sleeping for 2 minutes for good measure)
				/* System.out.println("sleep for 2 minutes"); */
				/* sleep(2); // sleep 2 minutes */
				
				// REMOVE
				System.out.println("performing remove all");
				remove_res = executeIO(data_file, line_limit, "remove");
				
				// calculate insert and remove per second
				float objects = insert_res.get(1);
				float insert_rate = objects / insert_res.get(0);
				float remove_rate = objects / remove_res.get(0);
				
				// display results
				displayIOResults(line_limit, 
						insert_res, 
						remove_res, 
						insert_rate,
						remove_rate);
				
				// log results 
				String[] csv_line = {
						Float.toString(insert_res.get(1)), // objects tested
						Float.toString(insert_res.get(0)), // insert time
						Float.toString(remove_res.get(0)), // remove time
						Float.toString(insert_rate), // obj per sec
						Float.toString(remove_rate), // obj per sec
				};
				file_manager.csvLogEvent(csv_line);
			}
		} catch (NullPointerException e) {
			System.out.println("error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("error: " + e);
		} finally {
			file_manager.closeFileWriter();
		}
	}
	
	/**
	 * Execute the test
	 * @param res_path
	 * @param repeat
	 */
	public void run(String data_file, String res_path, int repeat) 
	{
		for (int i = 1; i <= repeat; i++) {
			System.out.println("Run number: " + Integer.toString(i));
			this.test(data_file, res_path + "io_results_" + i + ".csv");
		}
        this.mongodb.disconnect();
	}
}
