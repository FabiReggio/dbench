package db.couchbase;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.couchbase.client.CouchbaseClient;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class CustomCouchbaseClient 
{
	// --- Fields ---
	private HttpClient http_client;
	private CouchbaseClient couchbase_client;
	private CouchbaseRestfulURLFactory url_factory;
	private String host;
	private String bucket;
	private String port = "8091";
	private String restful_port = "8092";
	private String user = "";
	private String pass = "";
//	private ArrayList<String> url_elements = new ArrayList<String>();
	
	// --- Constructors ---
	public CustomCouchbaseClient(String host, String bucket) 
	{
		this.host = host;
		this.bucket = bucket;
		url_factory = new CouchbaseRestfulURLFactory(host, 
				this.port, 
				this.restful_port);
		
		if (connect() == false)
			throw new RuntimeErrorException(new Error("failed to connect!"));
		
	}
	
	// --- Methods ---
	/**
	 * Attempts to establish a connection to couchbase
	 * @return
	 */
	private boolean connect() 
	{
		if (restful_connect() == false) {
			System.out.println("error! cannot create restful connection!");
			return false;
		} 
		
		if (native_connect() == false) { 
			System.out.println("error! cannot create native connection!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Connect to CouchBase via RestFul services
	 * @return
	 */
	private boolean restful_connect() 
	{
		// instanciate http_client (RESTFUL) and couchbase_client (NATIVE)
		this.http_client = new DefaultHttpClient();
		
		// get response
		try {
			String url = url_factory.getBucketsUrl(this.bucket);
			System.out.println(url);
			HttpGet get_req = RequestFactory.jsonGetReq(url);
			HttpResponse response = this.http_client.execute(get_req);
			HttpEntity entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() != 200) {
				String error = "Failed : HTTP error code : " 
					+ response.getStatusLine().getStatusCode();
				throw new RuntimeException(error);
			}
	 
			if (entity != null) {
				// load json object
//				InputStream in = entity.getContent();
//				String json_string = convertInputStreamToString(in);
//				JSONObject json = new JSONObject(json_string);
			}
			
			return true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean native_connect() 
	{
        try {
            List<URI> uri_list = new LinkedList<URI>();
            uri_list.add(URI.create(this.host + ":" + this.port + "/pools"));
            this.couchbase_client = new CouchbaseClient(uri_list, this.bucket, "");
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
		
		try {
			Status tweet = DataObjectFactory.createStatus(tweet_json);
			String t_id = String.valueOf(tweet.getId());
			return this.couchbase_client.set(t_id, 0, tweet_json).get();
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
		if (http_client == null) return false;
		
		try {
			Status tweet = DataObjectFactory.createStatus(tweet_json);
			String t_id = String.valueOf(tweet.getId());
			return this.couchbase_client.delete(t_id).get();
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
	public JSONArray queryView(
			String doc_name, 
			String view_name,
			HashMap<String, String> view_settings) 
	{
		// send and get response
		try {
			String url = "";
			url = url_factory.getViewUrl(
					this.bucket, 
					doc_name, 
					view_name, 
					view_settings);
			HttpGet get_req = RequestFactory.jsonGetReq(url);
			HttpResponse response = this.http_client.execute(get_req);
			HttpEntity entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() != 200) {
				String error = "Failed : HTTP error code : " 
					+ response.getStatusLine().getStatusCode();
				throw new RuntimeException(error);
			}
	 
			if (entity != null) {
				// load json object
				InputStream in = entity.getContent();
				String json_string = convertInputStreamToString(in);
				JSONObject json = new JSONObject(json_string);
				return json.getJSONArray("rows");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public boolean loadDesignDoc(
			String doc, 
			String bucketname, 
			String view_name) throws IOException
	{
		return false;
	}

	/**
	 * Converts input stream to string
	 * @param in
	 * @return
	 * 		String of input stream
	 * @throws IOException
	 */
	private String convertInputStreamToString(InputStream in) 
			throws IOException 
	{
		String s = null;
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer);
		s = writer.toString();
		
		return s;
	}
	
	/**
	 * Adds Basic HTTP authentication to connection
	 * @param conn
	 */
	public void addBasicHttpAuth(HttpURLConnection conn)
	{
		String auth = RequestFactory.encodeAuth(this.user, this.pass);
		conn.setRequestProperty("Authorization", "Basic " + auth);
	}
	
	/**
	 * Disconnect from CouchBase
	 * @return
	 */
	public boolean disconnect() 
	{
		return this.couchbase_client.shutdown(5, TimeUnit.SECONDS);
	}
	

}
