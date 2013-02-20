package db;

/**
 * DBDetails essentially encapsulates all the details needed to
 * connect to a database
 * @author chris choi
 */
public class DBDetails {
	// --- Fields ---
	private String db_host;
	private int db_port;
	private String db_name;
	private String db_collection;

	// --- Constructors ---
	public DBDetails(
			String db_host,
			int db_port,
			String db_name,
			String db_collection)
	{
		this.db_host = db_host;
		this.db_port = db_port;
		this.db_name = db_name;
		this.db_collection = db_collection;
	}

	// --- Getters and Setters ---
	public String getDBHost() {
		return db_host;
	}

	public void setDBHost(String db_host) {
		this.db_host = db_host;
	}

	public int getDBPort() {
		return db_port;
	}

	public void setDBPort(int db_port) {
		this.db_port = db_port;
	}

	public String getDBName() {
		return db_name;
	}

	public void setDBName(String db_name) {
		this.db_name = db_name;
	}

	public String getDBCollection() {
		return db_collection;
	}

	public void setDBCollection(String db_collection) {
		this.db_collection = db_collection;
	}

}
