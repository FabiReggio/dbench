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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;


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
	public boolean addTweet(String file)
	{
		JsonFactory factory = new JsonFactory(); 
	    ObjectMapper mapper = new ObjectMapper(factory);
	    JsonParser json_parser;
	    TweetForSolr tweet = new TweetForSolr();
	    String json_string = "";
	    
		try {
			LineIterator line_iter = FileUtils.lineIterator(new File(file));
			
			while (line_iter.hasNext()) {
				json_string = line_iter.next();
				json_parser = new JsonFactory().createJsonParser(json_string);
			    tweet = mapper.readValue(json_parser, TweetForSolr.class);
			    
			    SolrInputDocument doc = new SolrInputDocument();
			    doc.addField("MongoDB Object ID", tweet.getObjId());
			    doc.addField("Tweet ID", tweet.getTweetId());
			    doc.addField("Tweet Text", tweet.getTweetText());
			    
			    this.server.add(doc);
			    this.server.commit();
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
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
		params.set("count", key + ":" + value);
		
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
}
