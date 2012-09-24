package db;

import db.IMySQLAdaptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLAdaptor implements IMySQLAdaptor 
{
    // --- Fields --- 
    private Connection db_conn = null;

    // --- Constructors --- 
    public MySQLAdaptor() {}

    // --- Methods --- 
    public boolean dbConnect(String db_host,
            String db_username,
            String db_password,
            String db_name)
    {
        try {
            db_conn = DriverManager.getConnection(db_host + db_name, 
                    db_username, 
                    db_password);
            System.out.println("Connected to the database");
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
            return false; 
        }
       return true; 
    }

    public boolean dbDisconnect()
    {
		try {
			this.db_conn.close();
			System.out.println("Disconnected from database");
		} catch (SQLException e) {
			System.out.println("Error: " + e.toString());
            return false; 
		}
        return true;
    }

    public Object dbQuery(String query)
    {
        Object result = "";
        Statement dbs = null;
        boolean obtain_results = false;

        try {
            dbs = this.db_conn.createStatement();
           	if (dbs.executeUpdate(query) != 0) {
				System.out.println("Query Executed Successfully!");
				
                // check if query should return something
                for (String op: mod_operations) {
                    if (query.startsWith(op)) {
                        obtain_results = true;
                        break;
                    }
                }
                
                // return results or true to indicate success
                if (obtain_results) result = dbs.getResultSet();
                else result = true;

			} else {
				System.out.println("Query Executed With Errors!");
				result = false;
			}
            dbs.close(); // close statement
		} catch (SQLException e) {
            System.out.println("Query String Error!");
            result = false; 
		}
		return result;
    }
}
