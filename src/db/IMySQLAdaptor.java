package db;

import java.util.ArrayList;

public interface IMySQLAdaptor 
{
    // --- Fields --- 
    public int state = 0;
    public ArrayList<String> mod_operations = new ArrayList<String>() {{
        add("CREATE"); 
        add("INSERT");
        add("UPDATE"); 
        add("DELETE"); 
        add("DROP");
        add("ALTER");
        add("TRUNCATE");
        add("LOAD");
    }};

    // --- Methods ---
    public boolean dbConnect(String db_host,
            String db_username,
            String db_password,
            String db_name);
    public boolean dbDisconnect();
    public Object dbQuery(String query);
}
