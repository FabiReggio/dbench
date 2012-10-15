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

public class IOTest extends DBTest
{
	// --- Fields ---
	private MongoDBClient mongodb;
	private String[] io_test_header = {
			"objects",
			"insert", 
			"remove", 
			"insert per msec",
			"remove per msec",
	};
	
	// --- Constructors ---
	public IOTest (DBDetails db_details)
	{
		super(db_details);
	}
	
	// --- Methods ---
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
	}
	
	/**
	 * 
	 * @param fp
	 * @param lines_limit
	 * @param mode
	 * @return
	 */
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
	public void run(String fp, String res_path, int slice) 
	{
		FileManager file_manager = new FileManager();
		ArrayList<Float> insert_res = new ArrayList<Float>();
		ArrayList<Float> remove_res = new ArrayList<Float>();
		
		try {
			// prepare 
			this.mongodb = prepDB("NORMAL");
			prepResultsFile(file_manager, res_path, this.io_test_header);
			
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
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("error: " + e);
		} finally {
			file_manager.closeFileWriter();
			closeDB();
		}
	}
}
