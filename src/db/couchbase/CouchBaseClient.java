package db.couchbase;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.internal.OperationFuture;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import com.couchbase.client.CouchbaseClient;



public class CouchBaseClient 
{
	// --- Fields ---
	private CouchbaseClient couchbase;
	
	// --- Constructors ---
	public CouchBaseClient() {}
	
	// --- Methods ---
	/**
	 * Connect to CouchBase
	 * @return
	 */
	public boolean connect(String host) 
	{
		try {
			List<URI> uri_list = new LinkedList<URI>();
			uri_list.add(URI.create(host + ":8091/pools"));
			couchbase = new CouchbaseClient(uri_list, "db_tests", "");
		} catch (IOException e) {
			System.err.println("error! cannot connect to couchbase!");
			return false;
		}
		return true;
	}
	
	/**
	 * Insert Tweet
	 * @param tweet_json
	 * 		Tweet as a json String
	 * @return
	 */
	public boolean insert(String tweet_json)
	{
		if (couchbase == null) return false;
		
		try {
			Status tweet = DataObjectFactory.createStatus(tweet_json);
			String t_id = String.valueOf(tweet.getId());
			return this.couchbase.set(t_id, 0, tweet_json).get();
		} catch (TwitterException e) {
			System.out.println("error! bad input tweet!");
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Remove Tweet
	 * @param tweet_json
	 * @return
	 */
	public boolean remove(String tweet_json)
	{
		if (couchbase == null) return false;
		
		try {
			Status tweet = DataObjectFactory.createStatus(tweet_json);
			String t_id = String.valueOf(tweet.getId());
			return this.couchbase.delete(t_id).get();
		} catch (TwitterException e) {
			System.out.println("error! bad input tweet!");
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Disconnect from CouchBase
	 * @return
	 */
	public boolean disconnect() 
	{
		return couchbase.shutdown(5, TimeUnit.SECONDS);
	}
	

}
