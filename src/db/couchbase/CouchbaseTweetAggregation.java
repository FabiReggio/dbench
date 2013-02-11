package db.couchbase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import tools.MapUtil;

public class CouchbaseTweetAggregation {
	// --- Fields ---
	private CustomCouchbaseClient couchbase;
	private HashMap<String, String> query;

	// --- Constructors ---
	public CouchbaseTweetAggregation(CustomCouchbaseClient couchbase) {
		this.couchbase = couchbase;

		this.query = new HashMap<String, String>();
		this.query.put("group", "true");
		this.query.put("reduce", "true");
		this.query.put("stale", "update_after");
		this.query.put("connection_timeout", "600000");
	}

	// --- Methods ---
	/**
	 * Prints the view response
	 *
	 * @param result
	 */
	public void printViewResponse(LinkedHashMap<String, Integer> sorted_map) {
        System.out.println("View response: ");
        int count = 0;
        for (Map.Entry<String, Integer> entry : sorted_map.entrySet()) {
            if (count == 5) break;
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + ":" + value);
            count++;
        }
	}

	/**
	 * Queries the most user mentioned
	 *
	 * @return
	 */
	public LinkedHashMap<String, Integer> mostUserMentioned()
	{
		JSONArray json_array = this.couchbase.queryView(
              "most_frequent",
              "most_user_mentioned",
              this.query);

		HashMap<String, Integer> hash = jsonArrayToHashMap(json_array);
        Map<String, Integer> sorted_map = MapUtil.sortByValue(hash, false);

		return (LinkedHashMap<String, Integer>) sorted_map;
	}

    /**
     * Queries the most hashed tags
     * @return
     */
    public LinkedHashMap<String, Integer> mostHashedTags()
    {
		JSONArray json_array = this.couchbase.queryView(
              "most_frequent",
              "most_hashed_tags",
              this.query);

		HashMap<String, Integer> hash = jsonArrayToHashMap(json_array);
        Map<String, Integer> sorted_map = MapUtil.sortByValue(hash, false);

		return (LinkedHashMap<String, Integer>) sorted_map;
    }

    /**
     * Queries the most shared urls
     * @return
     */
    public LinkedHashMap<String, Integer> mostSharedUrls()
    {
		JSONArray json_array = this.couchbase.queryView(
              "most_frequent",
              "most_shared_urls",
              this.query);

		HashMap<String, Integer> hash = jsonArrayToHashMap(json_array);
        Map<String, Integer> sorted_map = MapUtil.sortByValue(hash, false);

		return (LinkedHashMap<String, Integer>) sorted_map;
    }


	/**
	 * Converts JSONArray to HashMap
	 */
	private HashMap<String, Integer> jsonArrayToHashMap(JSONArray json_array)
	{
		HashMap<String, Integer> hash_map = new HashMap<String, Integer>();

		JSONObject json;
		String key;
		int value;
		for(int i = 0; i < json_array.length(); i++) {
			try {
				json = json_array.getJSONObject(i);
				key = (String) json.get("key");
				value = (Integer) json.get("value");

				hash_map.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return hash_map;
	}

	/**
	 * Sort a JSONArray by values, keeps duplicates Note: function assumes the
	 * input hash-map is <String, Integer>
	 *
	 * @param map
	 * @return
	 */
	public LinkedHashMap<String, Integer> sortViewResultsByValues (
			LinkedHashMap<String, Integer> map)
	{
		List<String> map_keys= new ArrayList<String>(map.keySet());
		List<Integer> map_values = new ArrayList<Integer>(map.values());
	    LinkedHashMap<String, Integer> sorted_map;
	    sorted_map = new LinkedHashMap<String, Integer>();

	    // sort the values
	    Collections.sort(map_values);

	    String key = "";
	    int value = 0;
	    for (int i = 0; i < map_values.size(); i++) {
	    	key = map_keys.get(i);
	    	value = map_values.get(i);
	        sorted_map.put(key, value);
	    }

	    return sorted_map;
	}
}
