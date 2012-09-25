package db;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;


public class MongoDBAdaptor implements IMongoDBAdaptor 
{
	// --- Fields ---
	private Mongo mongodb;
	private DB db;
    private DBCollection collection;

	// --- Constructor ---
	public MongoDBAdaptor() {}

	// --- Methods ---
    /**
     * Connect to MongoDB 
     * @param db_host Hostname of MongoDB
     * @param db_port Port of MongoDB
     * @param db_name Database name to access in MongoDB
     */
	public boolean connect(String db_host, 
			int db_port, 
			String db_name) 
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
     * @param db_host Hostname of MongoDB
     * @param db_port Port of MongoDB
     * @param db_username Username to access MongoDB
     * @param db_password Password to access MongoDB
     * @param db_name Database name to access in MongoDB
     */
	public boolean connect(String db_host, 
			int db_port,
			String db_username, 
			char[] db_password, 
			String db_name) 
	{
		try {
			this.mongodb = new Mongo(db_host, db_port);
			this.db = mongodb.getDB(db_name);

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
     * @param data JSON Data to be inserted
     * @param collection Collection name to insert document 
     */
	public boolean insert(String data)
	{
	    try {
	    	if (isCollectionSet()) {
	            this.collection.insert((DBObject)JSON.parse(data));
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
     * @param data ArrayList of String which contains JSON Data to be inserted
     * @param collection Collection name to insert document 
     */
	public boolean insertBulk(ArrayList<String> data)
	{
	    try {
	    	if (isCollectionSet()) {
	    		for (String d: data) 
		            this.collection.insert((DBObject)JSON.parse(d));
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
     * @param data JSON Data to be inserted
     * @param collection Collection name to remove document 
     */
	public boolean remove(String data)
    {
	    try {
	    	if (isCollectionSet()) {
	            this.collection.remove((DBObject)JSON.parse(data));
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
	 * Builds a query to be used to find documents in MongoDB
	 * @param keys
	 * @param vals
	 * @param iconds
	 * @param oconds
	 * @return A query object
	 */
	public Object queryBuilder(
			ArrayList<String> keys,
			ArrayList<String> vals,
			ArrayList<String> iconds,
			ArrayList<String> oconds)
	{
    	BasicDBObject query = new BasicDBObject();
    	int size = keys.size();
    	
    	
    	for (int i = 0; i <= size; i++) {
    		if (iconds.get(i + 1).equals("=="))
    			query.put(keys.get(i), vals.get(i));
    		else if (iconds.get(i + 1).equals("<"))
    			query.put(keys.get(i), new BasicDBObject("$lt", vals.get(i)));
    		else if (iconds.get(i + 1).equals("<="))
    			query.put(keys.get(i), new BasicDBObject("$lte", vals.get(i)));
    		else if (iconds.get(i + 1).equals(">"))
    			query.put(keys.get(i), new BasicDBObject("$gt", vals.get(i)));
    		else if (iconds.get(i + 1).equals(">="))
    			query.put(keys.get(i), new BasicDBObject("$gte", vals.get(i)));
    		else if (iconds.get(i + 1).equals("!=")) 
    			query.put(keys.get(i), new BasicDBObject("$ne", vals.get(i)));
    		else if (iconds.get(i + 1).equals("&&")) 
    			query.put(keys.get(i), new BasicDBObject("$in", vals.get(i)));
    		else if (iconds.get(i + 1).equals("||")) 
    			query.put(keys.get(i), new BasicDBObject("$or", vals.get(i)));
    	}
    	
    	
    	
    	return query;
	}

	
    /**
     * Find documents
     * @param query Query string
     * @param keys ArrayList of keys
     * @param values ArrayList of values
     * @param iconds ArrayList of inner conditions between key-value 
     * @param oconds ArrayList of outer conditions between key-value **pairs**
     * @return
     */
	public ArrayList<String> find(
			ArrayList<String> keys,
			ArrayList<String> values,
			ArrayList<String> iconds,
			ArrayList<String> oconds)
    {
		ArrayList<String> results = new ArrayList<String>();
	    try {
	    	BasicDBObject query;
	    	query = (BasicDBObject) queryBuilder(keys, values, iconds, oconds);
	    	DBCursor cursor = this.collection.find(query);
	    	while(cursor.hasNext()) {
	    	    results.add(cursor.next().toString());
	    	}
        } catch (MongoException e) {
            System.out.println("error: " + e.toString());
        }
		return results;
    }

    /**
     * Find a single document 
     *
     */
	public String findOne()
    {
		String result = "";
	    try {
            result = this.collection.findOne().toString();
        } catch (MongoException e) {
            System.out.println("error: " + e.toString());
        }
		return result;
    }

	/**
	 * Checks to see if a MongoDB collection has been selected
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
     * @param db_user
     * @param db_pass
     */
    public boolean addUser(String db_user, char[] db_pass)
    {
    	try {
	    	this.db.addUser(db_user, db_pass);
    	} catch (MongoException e){
    		System.out.println("error: " + e);
    		return false;
    	}
    	return true;
    }
    
    /**
     * Remove user from MongoDB
     * @param db_user
     */
    public boolean removeUser(String db_user)
    {
    	try {
	    	this.db.removeUser(db_user);
    	} catch (MongoException e){
    		System.out.println("error: " + e);
    		return false;
    	}
    	return true;
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
