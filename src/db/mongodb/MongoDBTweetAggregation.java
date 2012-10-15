package db.mongodb;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.DB;

public class MongoDBTweetAggregation extends MongoDBClient 
{
	// --- Fields ---
	private DB db;
	private DBCollection collection;
	
	// --- Constructors ---
	public MongoDBTweetAggregation() {}

	// --- Methods ---
	/**
	 * Find documents - Object Method Test
	 * 
	 * @param query
	 *            Query string
	 * @return
	 */
	public DBObject objectTimeBucket() 
	{
		// Key
		DBObject key = new BasicDBObject();
		
		// Conditions
		DBObject regex = new BasicDBObject("$regex", "Tue Jul 24 13:35:*");
		DBObject cond = new BasicDBObject("created_at", regex);
		
		// Initial
		DBObject initial = new BasicDBObject("sum", 0);
		
		// Reduce
		String reduce_func = "function(doc, prev) {prev.sum += 1}";
		
		// Group Command
		DBObject result = this.collection.group(
				key,
				cond,
				initial,
				reduce_func);
		return result;
	}

	/**
	 * Find Most User Mentioned by using Map Reduce 
	 * @return
	 */
	public DBCursor mapReduceUserMentions() 
	{
		String map = ""
				+ "function() {"
				+ "		if (!this.entities) { return; }"
				+ "		this.entities.user_mentions.forEach("
				+ "			function(mention) {"
				+ "				emit(mention.screen_name, { count: 1 });" 
				+ "			}"
				+ "		)" 
				+ "};";
		String reduce = "" 
				+ "function(key, values) {" 
				+ "		var result = { count : 0 };" 
				+ "		values.forEach(function(value) {" 
				+ "				result.count += value.count;"
				+ "		});"
				+ "		return result;"
				+ "};";
		
		this.collection.mapReduce(
				map, 
				reduce, 
				"user_mentions",
				null);
		
		DBCollection user_mentions = this.db.getCollection("user_mentions");
		BasicDBObject sort_by = new BasicDBObject("value.count", -1);
		return user_mentions.find().sort(sort_by).limit(1000);
	}
	
	/**
	 * Finds the Most User Mentioned using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateUserMentions() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", 0);
		keys.put("entities.user_mentions", 1);
		DBObject project = new BasicDBObject("$project", keys);
		
		// $unwind
		DBObject unwind = new BasicDBObject(
				"$unwind", 
				"$entities.user_mentions");
		
		// $group
		DBObject keys_2 = new BasicDBObject();
		keys_2.put("_id", "$entities.user_mentions.screen_name");
		keys_2.put("count", new BasicDBObject("$sum", 1));
		DBObject group = new BasicDBObject("$group", keys_2);
		
		// $sort
		DBObject sort_by = new BasicDBObject("count", -1);
		DBObject sort = new BasicDBObject("$sort", sort_by); 
		
		// $limit 
		DBObject limit = new BasicDBObject("$limit", 1000);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort,
				limit);
		return out.results();
	}
	
	/**
	 * Find Most Hashed Tags by using Map Reduce 
	 * @return
	 */
	public DBCursor mapReduceHashTags() 
	{
		String map = ""
				+ "function() {"
				+ "		if (!this.entities) { return; }"
				+ "		this.entities.hashtags.forEach("
				+ "			function(tag) {"
				+ "				emit(tag.text, { count: 1 });" 
				+ "			}"
				+ "		)" 
				+ "};";
		String reduce = "" 
				+ "function(key, values) {" 
				+ "		var result = { count : 0 };" 
				+ "		values.forEach(function(value) {" 
				+ "				result.count += value.count;"
				+ "		});"
				+ "		return result;"
				+ "};";
		
		this.collection.mapReduce(
				map, 
				reduce, 
				"hash_tags",
				null);
		
		DBCollection hash_tags = this.db.getCollection("hash_tags");
		BasicDBObject sort_by = new BasicDBObject("value.count", -1);
		return hash_tags.find().sort(sort_by).limit(1000);
	}
	
	/**
	 * Finds the Most Hash Tag using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateHashTags() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", 0);
		keys.put("entities.hashtags", 1);
		DBObject project = new BasicDBObject("$project", keys);
		
		// $unwind
		DBObject unwind = new BasicDBObject(
				"$unwind", 
				"$entities.hashtags");
		
		// $group
		DBObject keys_2 = new BasicDBObject();
		keys_2.put("_id", "$entities.hashtags.text");
		keys_2.put("count", new BasicDBObject("$sum", 1));
		DBObject group = new BasicDBObject("$group", keys_2);
		
		// $sort
		DBObject sort_by = new BasicDBObject("count", -1);
		DBObject sort = new BasicDBObject("$sort", sort_by); 
		
		// $limit 
		DBObject limit = new BasicDBObject("$limit", 1000);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort,
				limit);
		return out.results();
	}
	
	/**
	 * Find Most Shared URL by using Map Reduce
	 */
	public DBCursor mapReduceSharedUrls() 
	{
		String map = ""
				+ "function() {"
				+ "		if (!this.entities) { return; }"
				+ "		this.entities.urls.forEach("
				+ "			function(urls) {"
				+ "				emit(urls.expanded_url, { count: 1 });" 
				+ "			}"
				+ "		)" 
				+ "};";
		String reduce = "" 
				+ "function(key, values) {" 
				+ "		var result = { count : 0 };" 
				+ "		values.forEach(function(value) {" 
				+ "				result.count += value.count;"
				+ "		});"
				+ "		return result;"
				+ "};";
		
		this.collection.mapReduce(
				map, 
				reduce, 
				"shared_urls",
				null);
		
		DBCollection shared_urls = this.db.getCollection("shared_urls");
		BasicDBObject sort_by = new BasicDBObject("value.count", -1);
		return shared_urls.find().sort(sort_by).limit(1000);
	}
	
	/**
	 * Finds the Most Share URL using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateSharedUrls() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", 0);
		keys.put("entities.urls", 1);
		DBObject project = new BasicDBObject("$project", keys);
		
		// $unwind
		DBObject unwind = new BasicDBObject(
				"$unwind", 
				"$entities.urls");
		
		// $group
		DBObject keys_2 = new BasicDBObject();
		keys_2.put("_id", "$entities.urls.expanded_url");
		keys_2.put("count", new BasicDBObject("$sum", 1));
		DBObject group = new BasicDBObject("$group", keys_2);
		
		// $sort
		DBObject sort_by = new BasicDBObject("count", -1);
		DBObject sort = new BasicDBObject("$sort", sort_by); 
		
		// $limit 
		DBObject limit = new BasicDBObject("$limit", 1000);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort,
				limit);
		return out.results();
	}
}
