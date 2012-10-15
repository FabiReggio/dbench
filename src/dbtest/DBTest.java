package dbtest;

import io.FileManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import db.DBDetails;
import db.mongodb.MongoDBClient;
import db.mongodb.MongoDBTweetAggregation;
import db.mongodb.MongoDBTweetFind;

public class DBTest
{
	// --- Fields ---
	private Object mongodb;
	private String db_host = "";
	private int db_port = 0;
	private String db_name = "";
	private String db_collection = "";

	// --- Constructors ---
	public DBTest(DBDetails db_details)
	{
		this.db_host = db_details.getDBHost();
		this.db_port = db_details.getDBPort();
		this.db_name = db_details.getDBName();
		this.db_collection = db_details.getDBCollection();
	}
	
	// --- Methods ---	
	/**
	 * Prepare database for tests
	 */
	public Object prepDB(String type) {
        if (type.equals("NORMAL"))
            this.mongodb = new MongoDBClient();
			this.mongodb.connect(this.db_host, this.db_port, this.db_name);
			this.mongodb.setCollection(this.db_collection);
        else if (type.equals("AGGREGATION")) 
            this.mongodb = new MongoDBTweetAggregation();
			this.mongodb.connect(this.db_host, this.db_port, this.db_name);
			this.mongodb.setCollection(this.db_collection);
        else if (type.equals("FIND")) 
            this.mongodb = new MongoDBTweetFind();
			this.mongodb.connect(this.db_host, this.db_port, this.db_name);
			this.mongodb.setCollection(this.db_collection);
		return this.mongodb;
	}
	
	/**
	 * Disconnect database
	 */
	public void closeDB()
	{
		this.mongodb.disconnect();
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
