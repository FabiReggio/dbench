package db.mongodb;



import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoDBUtils 
{
	// --- Fields ---
	private MongoDBClient mongodb;
	private DBCollection collection;
	private DB db;
	
	// --- Constructors ---
	public MongoDBUtils(MongoDBClient mongodb) 
	{
		this.mongodb = mongodb;
		this.collection = this.mongodb.getCollection();
		this.db = this.mongodb.getDB();
	}
	
	// --- Methods ---
	/**
	 * Creates a group field, later used for manual pre-splitting
	 * @param groupings
	 * 		The number of shard groups you wish to distinguish 
	 */
	public boolean createGroupField(int groupings)
	{
		DBCursor cursor = this.collection.find().limit(10);
		
		Iterator<DBObject> iter = cursor.iterator();
		int counter = 1;
		while (iter.hasNext()) {
			BasicDBObject old_doc = (BasicDBObject) iter.next();
			BasicDBObject old_doc_id = new BasicDBObject();
			BasicDBObject set = new BasicDBObject();
			
			// get the doc id and create set object 
			if (counter > groupings) counter = 1;
			old_doc_id.append("_id", old_doc.get("_id"));
			set.put("$set", new BasicDBObject("group", counter));
			
			this.collection.update(old_doc_id, set);
			counter++;
		}
		
		return false;
	}
	

}
