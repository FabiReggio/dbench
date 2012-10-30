package dbtest;

import io.FileManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import db.DBDetails;
import db.mongodb.MongoDBClient;

public class MongoDBTest
{
	// --- Fields ---
	private DBDetails db_details;
	
	// --- Constructors ---
	public MongoDBTest(DBDetails db_details) 
	{
		this.db_details = db_details;
	}
	
	// --- Methods ---	
	public MongoDBClient prepDB() {
		MongoDBClient mongodb = new MongoDBClient();
		mongodb.connect(
				this.db_details.getDBHost(), 
				this.db_details.getDBPort(), 
				this.db_details.getDBName());
		mongodb.setCollection(this.db_details.getDBCollection());
		return mongodb;
	}
	
	/**
	 * Prepare results file for test
	 */
	public void prepResultsFile(
			FileManager fm, 
			String res_path, 
			String[] header)
	{
		fm.prepFileWriter(res_path);
		fm.csvLogEvent(header);
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
	
}
