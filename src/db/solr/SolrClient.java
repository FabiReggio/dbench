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

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;


/**
 * Solr Client
 * @author Chris Choi
 */
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
	public void connect(String db_host, int db_port) 
	{
		this.server = new HttpSolrServer(db_host + ":" +  db_port + "/solr/");
	}

	/**
	 * Disconnect from Solr server
	 * @return
	 */
	public void disconnect() 
	{
		this.server.shutdown();
	}
	
	/**
	 * Add Tweet to Solr
	 * @param file
	 * @param tweets
	 * 		Number of tweets to add 
	 * @return
	 * 		True or False
	 */
	public boolean addTweets(String file, int tweets)
	{
	    String json_string = "";
	    
		try {
			LineIterator line_iter = FileUtils.lineIterator(new File(file));
			
			int limit = tweets;
			int count = 0;
			while (line_iter.hasNext()) {
				// check limit
				if (count == limit) break;
				count++;
				
				// raw json to object
				json_string = line_iter.next();
				Status tweet = DataObjectFactory.createStatus(json_string);
			    
				// create solr document
			    SolrInputDocument doc = new SolrInputDocument();
			    doc.addField("id", tweet.getId());
			    doc.addField("text", tweet.getText());
			    
			    // add to server
			    this.server.add(doc);
			    this.server.commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Add Tweet to Solr
	 * @param file
	 * @return
	 * 		True or False
	 */
	public boolean addTweets(String file)
	{
		return addTweets(file, -1);
	}
	
	/**
	 * Counts the number of tweets containing specified value from the 
	 * key field in question
	 * @param key
	 * @param value
	 */
	public int tweetCount(String key, String value)
	{
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", key + ":" + value);
			
			QueryResponse response = this.server.query(params);
			SolrDocumentList results = response.getResults();
			
			System.out.println("hits: " + results.getNumFound());
			System.out.println("query time (ms): " + response.getElapsedTime());
			System.out.println("tweets: " + results.size());
			
			int limit = 10;
			int count = 0;
			for (SolrDocument doc : results) {
				if (limit == count) break;
				count++;
				
				System.out.println("[DOC]: " + doc.getFieldValue("text"));
			}
			
			return results.size();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * 
	 */
	public void testQuery() 
	{
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", "*:*");
			
			QueryResponse response = this.server.query(params);
			SolrDocumentList results = response.getResults();
			
			System.out.println("hits: " + results.getNumFound());
			System.out.println("query time (ms): " + response.getElapsedTime());
			
			for (SolrDocument doc : results) {
				System.out.println("DOC: " + doc.getFieldValue("text"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteAll() 
	{
		try {
			this.server.deleteByQuery("*:*");
			this.server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
