package db.couchbase;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;


public class CustomCouchbaseClient 
{
	// --- Fields ---
	private CouchbaseClient couchbase;
	private String host_url;
	private String port = "8091";
	
	// --- Constructors ---
	public CustomCouchbaseClient() {}
	
	// --- Methods ---
	/**
	 * Connect to CouchBase
	 * @return
	 */
	public boolean connect(String host, String bucket) 
	{
		try {
			List<URI> uri_list = new LinkedList<URI>();
			CouchbaseConnectionFactory cf;
			
			uri_list.add(URI.create(host + ":" + this.port + "/pools"));
			cf = new CouchbaseConnectionFactory(uri_list, bucket, "");
			this.couchbase = new CouchbaseClient(cf);
			this.host_url = host;
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
	 * Query view
	 * @param view
	 * @param query
	 * @return
	 */
	public boolean queryView(
			String doc_name, 
			String view_name, 
			Query query) 
	{
		View view = this.couchbase.getView(doc_name, view_name);
		if (view == null) return false;
		
		ViewResponse result = this.couchbase.query(view, query);
		
		Iterator<ViewRow> itr = result.iterator();
		ViewRow row;
		row = itr.next();
		if (row != null) {
			System.out.println(String.format("ID is: %s", row.getId()));
			System.out.println(String.format("Key is: %s", row.getKey()));          
		}
		
		return true;
	}
	
	
	public boolean loadDesignDoc(
			String doc, 
			String bucketname, 
			String view_name) throws IOException
	{
		HttpClient httpclient = new DefaultHttpClient();
		String put_url = this.host_url + ":" + "8092" + "/" 
	    		+ bucketname
	    		+ "/_design/" 
	    		+ view_name; // use any one node in your cluster
		System.out.println(put_url);
	    HttpPut httpput = new HttpPut(put_url);

	    StringEntity reqEntity = new StringEntity(doc);
	    httpput.setEntity(reqEntity);
	    HttpResponse response = httpclient.execute(httpput);
	    System.out.println("View loading result:"  + response.getStatusLine());

	    if (response.getStatusLine().getStatusCode() < 300) return true;
	    else return false;
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
