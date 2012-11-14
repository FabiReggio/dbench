package db.couchbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

public class CouchbaseTweetAggregation 
{
	// --- Fields ---
	private CustomCouchbaseClient couchbase;
	private Query query;
	
	// --- Constructors ---
	public CouchbaseTweetAggregation(CustomCouchbaseClient couchbase) {
		this.couchbase = couchbase;
		
		this.query = new Query();
		this.query.setStale(Stale.FALSE);
		this.query.setIncludeDocs(true);
		this.query.setDescending(false);
		this.query.setGroup(true);
	}
	
	// --- Methods ---
	/**
	 * Prints the view response
	 * @param result
	 */
	public void printViewResponse(ViewResponse result) 
	{
		Iterator<ViewRow> itr = result.iterator();
		ViewRow row;
		row = itr.next();
		if (row != null) {
			System.out.println(String.format("ID is: %s", row.getId()));
			System.out.println(String.format("Key is: %s", row.getKey()));          
		}
	}

	/**
	 * Queries the most user mentioned
	 * @return
	 */
//	public LinkedHashMap<String, Integer> mostUserMentioned(
//			String doc_name, 
//			String view_name)
//	{
//		ViewResponse result = this.couchbase.queryView(
//				"_design", 
//				"most_user_mentioned", 
//				query);
//		
//		return sortHashMapByValues(covertHashMapValuesToInt(result.getMap()));
//	}
	
	/**
	 * Queries the most shared urls
	 * @return
	 */
//	public LinkedHashMap<String, Integer> mostSharedUrls(
//			String doc_name, 
//			String view_name)
//	{
//		ViewResponse result = this.couchbase.queryView(
//				"_design", 
//				"most_shared_urls", 
//				query);
//		
//		return sortHashMapByValues(covertHashMapValuesToInt(result.getMap()));
//	}
	
//	/**
//	 * Queries the most hashed tags 
//	 * @return
//	 */
//	public LinkedHashMap<String, Integer> mostHashedTags(
//			String doc_name, 
//			String view_name)
//	{
//		ViewResponse result = this.couchbase.queryView(
//				"_design", 
//				"most_hashed_tags", 
//				query);
//		
//		return sortHashMapByValues(covertHashMapValuesToInt(result.getMap()));
//	}
	
	/**
	 * Converts the map values from type object to int
	 * It has been deemed safer to convert map types this way
	 * @param map
	 * @return
	 */
	public Map<String, Integer> covertHashMapValuesToInt(
			Map<String, Object> map)
	{
		Map<String, Integer> converted_hashmap;
		converted_hashmap = new HashMap<String, Integer>();
		
		Iterator<String> iter = map.keySet().iterator();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			Integer value = (Integer) map.get(key);
			converted_hashmap.put(key, value);
		}
		
		return converted_hashmap;
	}
	
	/**
	 * Sort a hash map by values, keeps duplicates
	 * Note: function assumes the input hash-map is <String, Integer>
	 * @param map
	 * @return
	 */
	public LinkedHashMap<String, Integer> sortHashMapByValues(
			Map<String, Integer> map) 
	{
	    List<String> mapKeys = new ArrayList<String>(map.keySet());
	    List<Integer> mapValues = new ArrayList<Integer>(map.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap<String, Integer> sortedMap;
	    sortedMap = new LinkedHashMap<String, Integer>();
	    
	    Iterator<Integer> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = map.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                map.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Integer)val);
	                break;
	            }

	        }

	    }
	    
	    return sortedMap;
	}
	
 }