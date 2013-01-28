package dbtest.solr;

import io.FileManager;
import db.DBDetails;
import db.solr.SolrClient;


public class SolrFindTest 
{
	// --- Fields ---
	SolrClient solr = new SolrClient();
	private String[] results_header = {
				"keyword",
				"query time(ms)"
	};
	private String[] keywords = {
				"Olympics",
				"London2012",
				"TeamGB",
				"TeamUSA",
				"Chris",
				"Hoy",
				"Chris Hoy",
				"Tom Daley",
				"Michael Phelps"
	};
	
	// --- Constructors ---
	public SolrFindTest(DBDetails db_details) 
	{
		this.solr.connect(db_details.getDBHost(), db_details.getDBPort());
	}
	
	// --- Methods ---
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
	 * Execute a find query
	 */
	public long executeFind(String field, String keyword)
	{
		long time = 0;
		long start_time = System.currentTimeMillis();
		this.solr.tweetCount(field, keyword);
		time = System.currentTimeMillis() - start_time;
		return time;
	}
	
	/**
	 * Run test
	 * @param res_path
	 * 		Results Path
	 */
	public void run(String res_path) 
	{
		FileManager file_manager = new FileManager();
		long time = 0;
		
		// prepare
		prepResultsFile(file_manager, res_path, this.results_header);
		
		for (String keyword : keywords) {
			// test
			time = executeFind("text", keyword);
			
			// log results
			String [] csv_line = {
					keyword, // keyword
					Long.toString(time),
			};
			file_manager.csvLogEvent(csv_line);
		}
		
		// close
		file_manager.closeFileWriter();
	}

}
