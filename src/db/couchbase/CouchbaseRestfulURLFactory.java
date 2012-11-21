package db.couchbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CouchbaseRestfulURLFactory 
{
	// --- Fields ---
	private String host;
	private String port;
	private String restful_port;
	
	// --- Constructors ---
	public CouchbaseRestfulURLFactory(
			String host, 
			String port, 
			String restful_port) 
	{
		this.host = host;
		this.port = port;
		this.restful_port = restful_port;
	}
	
	// --- Methods ---
	/**
	 * Builds a full url based on the array list of elements provided
	 * @return
	 * 		url string
	 */
	private String buildUrl(ArrayList<String> url_elements)
	{
		String url = "";
		
		for (String element : url_elements) 
			url += element + "/";
		
		url_elements.clear();
		
		return url;
	}
	
	/**
	 * Builds a single string containing the settings for restful use
	 * @param settings
	 * @return
	 */
	private String settingsBuilder(HashMap<String, String> settings)
	{
		String set = "";
		
		Iterator<String> iter = settings.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = settings.get(key);
			set +=  key + "=" + value + "&";
		}
		
		return set;
	}
	
	/**
	 * Bucket Url
	 * @param bucket
	 * @return
	 */
	public String getBucketsUrl(String bucket)
	{
		ArrayList<String> url_elements = new ArrayList<String>();
		url_elements.add(this.host + ":" + this.port);
		url_elements.add("pools");
		url_elements.add("default");
		url_elements.add("buckets");
		url_elements.add(bucket);
		
		return buildUrl(url_elements); 
	}
	
	/**
	 * Views Url
	 * @return
	 * 		view url
	 */
	public String getViewUrl(
			String bucket, 
			String doc_name, 
			String view_name,
			HashMap<String, String> view_settings)
	{
		ArrayList<String> url_elements = new ArrayList<String>();
		url_elements.add(this.host + ":" + this.restful_port);
		url_elements.add(bucket);
		url_elements.add("_design");
		url_elements.add(doc_name);
		url_elements.add("_view");
		String url = buildUrl(url_elements) 
				+ view_name + "?" 
				+ settingsBuilder(view_settings);
		
		return url;
	}

}
