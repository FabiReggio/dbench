package db.mongodb;

public class TweetFindQueries extends MongoDBClient 
{
	// --- Fields ---
	
	// --- Constructors ---
	public TweetFindQueries() {}

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
}
