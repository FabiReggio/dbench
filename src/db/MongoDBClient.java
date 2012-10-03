package db;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import twitter4j.Tweet;

import com.mongodb.AggregationOutput;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

public class MongoDBClient implements IDBAdaptor {
	// --- Fields ---
	private Mongo mongodb;
	private DB db;
	private DBCollection collection;
	private String db_name = "";

	// --- Constructor ---
	public MongoDBClient() {}

	// --- Methods ---
	/**
	 * Connect to MongoDBF
	 * 
	 * @param db_host
	 *            Hostname of MongoDB
	 * @param db_port
	 *            Port of MongoDB
	 * @param db_name
	 *            Database name to access in MongoDB
	 */
	public boolean connect(String db_host, int db_port, String db_name) 
	{
		try {
			this.mongodb = new Mongo(db_host, db_port);
			this.db = mongodb.getDB(db_name);
			System.out.println("connected to the MongoDB");
		} catch (MongoException e) {
			System.out.println("error: " + e.toString());
			return false;
		} catch (UnknownHostException e) {
			System.out.println("error: " + e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Connect to MongoDB
	 * 
	 * @param db_host
	 *            Hostname of MongoDB
	 * @param db_port
	 *            Port of MongoDB
	 * @param db_username
	 *            Username to access MongoDB
	 * @param db_password
	 *            Password to access MongoDB
	 * @param db_name
	 *            Database name to access in MongoDB
	 */
	public boolean connect(String db_host, int db_port, String db_username,
			char[] db_password, String db_name) 
	{
		try {
			this.mongodb = new Mongo(db_host, db_port);
			this.db = mongodb.getDB(db_name);
			this.db_name = db_name;

			// authenticate username and password
			if (this.db.authenticate(db_username, db_password))
				this.db = mongodb.getDB(db_name);

			System.out.println("connected to the MongoDB");
		} catch (Exception e) {
			System.out.println("error: " + e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Disconnect from MongoDB
	 */
	public boolean disconnect() 
	{
		this.mongodb.close();

		// double check connection is indeed closed
		try {
			this.mongodb.getDatabaseNames();
		} catch (MongoException e) {
			return true;
		}
		return false;
	}

	/**
	 * Insert document
	 * 
	 * @param data
	 *            JSON Data to be inserted
	 * @param collection
	 *            Collection name to insert document
	 */
	public boolean insert(String data) 
	{
		try {
			if (isCollectionSet()) {
				this.collection.insert((DBObject) JSON.parse(data));
				return true;
			} else {
				System.out.println("error: collection hasn't been set yet");
				return false;
			}
		} catch (MongoException e) {
			System.out.println("error: " + e.toString());
			return false;
		}
	}

	/**
	 * Insert a list of documents
	 * 
	 * @param data
	 *            ArrayList of String which contains JSON Data
	 * @param collection
	 *            Collection name to insert document
	 */
	public boolean insertBulk(ArrayList<String> data) 
	{
		try {
			if (isCollectionSet()) {
				for (String d : data)
					this.collection.insert((DBObject) JSON.parse(d));
				return true;
			} else {
				System.out.println("error: collection hasn't been set yet");
				return false;
			}
		} catch (MongoException e) {
			System.out.println("error: " + e.toString());
			return false;
		}
	}

	/**
	 * Remove document
	 * 
	 * @param data
	 *            JSON Data to be inserted
	 * @param collection
	 *            Collection name to remove document
	 */
	public boolean remove(String data) 
	{
		try {
			if (isCollectionSet()) {
				this.collection.remove((DBObject) JSON.parse(data));
				return true;
			} else {
				System.out.println("error: collection hasn't been set yet");
				return false;
			}
		} catch (MongoException e) {
			System.out.println("error: " + e.toString());
			return false;
		}
	}

	/**
	 * Remove all documents in collection
	 */
	public boolean removeAll() 
	{
		try {
			if (isCollectionSet()) {
				this.collection.remove(new BasicDBObject());
				return true;
			} else {
				System.out.println("error: collection hasn't been set yet");
				return false;
			}
		} catch (MongoException e) {
			System.out.println("error: " + e.toString());
			return false;
		}
	}

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
		
		MapReduceOutput output = this.collection.mapReduce(
				map, 
				reduce, 
				"user_mentions",
				null);
		
		DBCollection user_mentions = this.db.getCollection("user_mentions");
		BasicDBObject sort_by = new BasicDBObject("value.count", -1);
		return user_mentions.find(new BasicDBObject()).sort(sort_by);
	}
	
	/**
	 * Finds the Most User Mentioned using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateUserMentions() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", "0");
		keys.put("entities.user_mentions", "1");
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
		DBObject sort = new BasicDBObject("count", -1);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort);
		return out.results();
	}
	
	/**
	 * Find Most Hased Tags by using Map Reduce 
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
		
		MapReduceOutput output = this.collection.mapReduce(
				map, 
				reduce, 
				"hash_tags",
				null);
		
		DBCollection hash_tags = this.db.getCollection("hash_tags");
		BasicDBObject sort_by = new BasicDBObject("value.count", -1);
		return hash_tags.find().sort(sort_by);
	}
	
	/**
	 * Finds the Most Hash Tag using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateHashTags() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", "0");
		keys.put("entities.hashtags", "1");
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
		DBObject sort = new BasicDBObject("count", -1);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort);
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
				+ "				emit(urls.url, { count: 1 });" 
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
		
		MapReduceOutput output = this.collection.mapReduce(
				map, 
				reduce, 
				"shared_urls",
				null);
		
		DBCollection shared_urls = this.db.getCollection("shared_urls");
		BasicDBObject sort_by = new BasicDBObject("value.count", "-1");
		return shared_urls.find(new BasicDBObject()).sort(sort_by);
	}
	
	/**
	 * Finds the Most Share URL using the Aggregate Framework
	 * @return Iterator of DBObject
	 */
	public Iterable<DBObject> aggregateSharedURL() 
	{
		// $project
		DBObject keys = new BasicDBObject();
		keys.put("_id", "0");
		keys.put("entities.url", "1");
		DBObject project = new BasicDBObject("$project", keys);
		
		// $unwind
		DBObject unwind = new BasicDBObject(
				"$unwind", 
				"$entities.url");
		
		// $group
		DBObject keys_2 = new BasicDBObject();
		keys_2.put("_id", "$entities.url.url");
		keys_2.put("count", new BasicDBObject("$sum", 1));
		DBObject group = new BasicDBObject("$group", keys_2);
		
		// $sort
		DBObject sort = new BasicDBObject("count", -1);
		
		AggregationOutput out = this.collection.aggregate(
				project,
				unwind,
				group,
				sort);
		return out.results();
	}
	
	/**
	 * Checks to see if a MongoDB collection has been selected
	 * 
	 * @return True or False
	 */
	public boolean isCollectionSet() 
	{
		if (this.collection != null
				&& this.collection.getFullName().length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add user to MongoDB
	 * 
	 * @param db_user
	 * @param db_pass
	 */
	public boolean addUser(String db_user, char[] db_pass) 
	{
		try {
			this.db.addUser(db_user, db_pass);
		} catch (MongoException e) {
			System.out.println("error: " + e);
			return false;
		}
		return true;
	}

	/**
	 * Remove user from MongoDB
	 * 
	 * @param db_user
	 */
	public boolean removeUser(String db_user) 
	{
		try {
			this.db.removeUser(db_user);
		} catch (MongoException e) {
			System.out.println("error: " + e);
			return false;
		}
		return true;
	}
	
	public void dropCollection(String collection) 
	{
		this.collection.drop();
	}

	// --- Setters and Getters ---
	public void setCollection(String collection_name) 
	{
		if (this.db != null)
			this.collection = this.db.getCollection(collection_name);
	}

	public String getCollectionName() 
	{
		return this.collection.getFullName();
	}

	public long getCollectionCount() 
	{
		return this.collection.count();
	}

}
