package db.mongodb;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDBTweetFind 
{
	// --- Fields ---
	private MongoDBClient mongodb;
	private DBCollection collection;
	
	// --- Constructors ---
	public MongoDBTweetFind(MongoDBClient mongodb){
		this.mongodb = mongodb;
		this.collection = mongodb.getCollection();
	}

	// --- Methods ---
//	/**
//	 * Find documents - Object Method Test
//	 * @param query
//	 *            Query string
//	 * @return
//	 */
//	public DBObject timeBucket(String pattern) 
//	{
//		// Key
//		DBObject key = new BasicDBObject();
//		
//		// Conditions
//		DBObject regex = new BasicDBObject("$regex", pattern);
//		DBObject cond = new BasicDBObject("created_at", regex);
//		
//		// Initial
//		DBObject initial = new BasicDBObject("sum", 0);
//		
//		// Reduce
//		String reduce_func = "function(doc, prev) {prev.sum += 1}";
//		
//		// Group Command
//		DBObject result = this.collection.group(
//				key,
//				cond,
//				initial,
//				reduce_func);
//		return result;
//	}
	
	/**
	 * Finds the number of tweets that matches the keyword in the text field 
	 * by using REGEX method 
	 * @param Keyword
	 * 		String to match 
	 * @return
	 */
	public int regexFindTweetCount(String keyword)
	{
		// Key
		DBObject regex = new BasicDBObject("$regex", keyword);
		DBObject key = new BasicDBObject("text", regex);
		
		// Find
		return this.collection.find(key).count();
	}
	
	/**
	 * Finds the number of tweets that matches the keyword in the text field
	 * by using _keyword field (which is essentially an array of strings).
	 * What we are interested is to see if searching through an array of string
	 * is faster than REGEX
	 * 
	 * Example query in mongo shell:
	 * 		db.collection.find({"_keywords" : "olympics"}).count()
	 * 
	 * @return
	 */
	public int matchFindTweetCount(String keyword)
	{
		// Key
		DBObject key = new BasicDBObject("_keywords", keyword);
		
		// Find
		return this.collection.find(key).count();
	}
	
	/**
	 * Finds the number of tweets that matches the keyword in the text field
	 * by using _keyword field (which is essentially an array of strings).
	 * What we are interested is to see if searching through an array of string
	 * is faster than REGEX
	 * 
	 * Example query in mongo shell:
	 * 		db.collection.find({"_keywords" : {$all : ["olympics"]}}).count()
	 * 
	 * @return
	 */
	public int arrayFindTweetCount(String keyword)
	{
		// Key
		DBObject all = new BasicDBObject("$all", keyword.split(" "));
		DBObject key = new BasicDBObject("_keywords", all);
		
		// Find
		return this.collection.find(key).count();
	}
	
	/**
	 * Finds the number of tweets that matches the keyword in the text field
	 * by using the aggregate framework
	 * 
	 * Example query in mongo shell:
	 * 		db.query_test_collection.aggregate( 
	 * 			{ $project : { "_keywords" : 1}}, 
	 * 			{ $unwind : "$_keywords"}, 
	 * 			{ $match : {"_keywords" : "olympics"}}, 
	 * 			{ $group : { _id: "$_keywords", count : {$sum : 1}}})
	 * 
	 * @return
	 */
	public int aggregateFindTweetCount(String keyword)
	{
		// $project
		DBObject elements = new BasicDBObject("_keywords", 1);
		DBObject project = new BasicDBObject("$project", elements);
		
		// $unwind
		DBObject unwind = new BasicDBObject("$unwind", "$_keywords");
		
		// $match
		DBObject pattern = new BasicDBObject("_keywords", keyword);
		DBObject match = new BasicDBObject("$match", pattern);
		
		// $group
		DBObject group_by = new BasicDBObject();
		group_by.put("_id", "$_keywords");
		group_by.put("count", new BasicDBObject("$sum", 1));
		DBObject group = new BasicDBObject("$group", group_by);
		
		AggregationOutput out = this.collection.aggregate(
				project, 
				unwind,
				match,
				group);
		return (Integer) out.getCommandResult().get("count");
	}
	
	public long getCollectionCount() 
	{
		return this.collection.getCount();
	}
}