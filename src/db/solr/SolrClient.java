package db.solr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import twitter4j.Tweet;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;


public class SolrClient 
{
	// --- Fields ---
	private HttpSolrServer server;
	
	// --- Constructors ---
	public SolrClient() {}

	// --- Methods ---
	/**
	 * Connect to Solr server
	 * @param db_host
	 * @param db_port
	 * @return
	 */
	public boolean connect(String db_host, int db_port) 
	{
		this.server = new HttpSolrServer(db_host + ":" +  db_port + "/solr/");
		return false; 
	}

	/**
	 * Disconnect from Solr server
	 * @return
	 */
	public boolean disconnect() 
	{
		this.server.shutdown();
		return false;
	}
	
	/**
	 * Add Tweet to Solr
	 * @param file
	 * @return
	 */
	public boolean addTweets(String file)
	{
	    String json_string = "";
	    
		try {
			LineIterator line_iter = FileUtils.lineIterator(new File(file));
			
			int limit = 1;
			int count = 0;
			while (line_iter.hasNext()) {
				if (count == limit) break;
				count++;
				
				json_string = line_iter.next();
//				System.out.println(json_string);
				
				Object tweet = DataObjectFactory.createTweet(json_string);
			    System.out.println(tweet.toString());
			    
//			    SolrInputDocument doc = new SolrInputDocument();
//			    doc.addField("MongoDB Object ID", tweet.getObjId());
//			    doc.addField("Tweet ID", tweet.getTweetId());
//			    doc.addField("Tweet Text", tweet.getTweetText());
//			    this.server.add(doc);
//			    this.server.commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
//		} catch (SolrServerException e) {
//			e.printStackTrace();
//			return false;
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Counts the number of tweets containing specified value from the 
	 * key field in question
	 * @param key
	 * @param value
	 */
	public void tweetCount(String key, String value)
	{
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", key + ":" + value);
		
		try {
			QueryResponse response = this.server.query(params);
			SolrDocumentList results = response.getResults();
			for (SolrDocument doc : results) {
				System.out.println(doc.toString());
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	public void testQuery() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		
		try {
			QueryResponse response = this.server.query(params);
			SolrDocumentList results = response.getResults();
			long query_time = response.getElapsedTime();
			
			System.out.println("hits: " + results.getNumFound());
			System.out.println("query time (ms): " + query_time);
			
			for (SolrDocument doc : results) {
				System.out.println("DOC: " + doc.toString());
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
}
