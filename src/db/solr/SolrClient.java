package db.solr;

import java.io.IOException;

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
		this.server = new HttpSolrServer(db_host + db_port);
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
	 * @param json_data
	 * @return
	 */
	public boolean addTweet(String json_data)
	{
		JsonFactory factory = new JsonFactory(); 
	    ObjectMapper mapper = new ObjectMapper(factory);
	    TweetForSolr tweet = new TweetForSolr();
	    JsonParser json_parser;
	    SolrInputDocument doc = new SolrInputDocument();
	    
		try {
			json_parser = new JsonFactory().createJsonParser(json_data);
		    tweet = mapper.readValue(json_parser, TweetForSolr.class);
		    doc.addField("MongoDB Object ID", tweet.getObjId());
		    doc.addField("Tweet ID", tweet.getTweetId());
		    doc.addField("Tweet Text", tweet.getTweetText());
		    this.server.add(doc);
		    this.server.commit();
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
	
	public void countTweets(String key, String value)
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
