package db;

public interface IMongoDBAdaptor 
{
    // --- Fields --- 
    public int status = 0; 

    // --- Methods ---
    public boolean dbConnectSingleDB(String db_host,
            int db_port,
            String db_name);
    public boolean dbConnectSingleDB(String db_host,
            int db_port,
            String db_username,
            char[] db_password,
            String db_name);
    public boolean dbDisconnect();
    public Object dbQuery(String query);
}
