package db;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDBAdaptor implements IMongoDBAdaptor {
	// --- Fields ---
	private Mongo mongodb;
	private DB db;

	// --- Constructor ---
	public MongoDBAdaptor() {
	}

	// --- Methods ---
	public boolean dbConnectSingleDB(String db_host, 
			int db_port, 
			String db_name) {
		try {
			this.mongodb = new Mongo(db_host, db_port);
			this.db = mongodb.getDB(db_name);
			System.out.println("Connected to the MongoDB");
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			return false;
		}
		return true;
	}

	public boolean dbConnectSingleDB(String db_host, 
			int db_port,
			String db_username, 
			char[] db_password, 
			String db_name) {
		try {
			this.mongodb = new Mongo(db_host, db_port);
			this.db = mongodb.getDB(db_name);

			// authenticate username and password
			if (this.db.authenticate(db_username, db_password))
				this.db = mongodb.getDB(db_name);

			System.out.println("Connected to the MongoDB");
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			return false;
		}
		return true;
	}

	public boolean dbDisconnect() {
		return true;
	}

	public Object dbQuery(String query) {
		return new Object();
	}

}
