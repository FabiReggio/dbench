package db.solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
	public SolrClient(String db_host, int db_port)
	{
        this.connect(db_host, db_port);
    }
	
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
	    long bad_tweets = 0; // number of bad tweets
	    ArrayList<Long> bad_tweets_record = new ArrayList<Long>();
	    Collection<SolrInputDocument> doc_list = new ArrayList<SolrInputDocument>();
		long limit = tweets;
		long count = 0;

		try {
			LineIterator line_iter = FileUtils.lineIterator(new File(file));

			while (line_iter.hasNext()) {
				// check limit
				if (count == limit) break;
				count++;

				// raw json to object
				json_string = line_iter.next();

				try {
					Status tweet = DataObjectFactory.createStatus(json_string);

					// create solr document
				    SolrInputDocument doc = new SolrInputDocument();
				    doc.addField("id", tweet.getId());
				    doc.addField("text", tweet.getText());
				    doc_list.add(doc);

				    // add to server every 10000 docs
				    if ((doc_list.size() % 10000) == 0) {
				    	System.out.println("Adding 10000 docs to solr");
					    this.server.add(doc_list);
					    doc_list.clear();
				    }
				} catch (TwitterException e) {
					System.out.println("error! bad tweet on line: " + count);
					bad_tweets_record.add(count);
					bad_tweets++;
				}
			}
			this.server.add(doc_list); // flush out remaining docs in doc_list
			this.server.commit(); // commit!
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
		} finally {
			System.out.println("Inserted: " + (count - bad_tweets));
			System.out.println("Number of Bad Tweets: " + bad_tweets);
			System.out.println("On line numbers:");
			for (int i = 0; i < bad_tweets_record.size(); i++)
				System.out.println((i + 1) + ": " + bad_tweets_record.get(i));
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
	 * Just a dummy query to see if Solr is working
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

	/**
	 * Deletes all data in solr collection
	 */
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
