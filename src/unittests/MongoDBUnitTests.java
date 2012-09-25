package unittests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import db.MongoDBAdaptor;

public class MongoDBUnitTests 
{
	// --- Fields ---
	private MongoDBAdaptor mongodb;
	private boolean bool;
	private String db_host = "localhost";
	private int db_port = 27017;
    private String db_user = "test_user";
    private char[] db_pass = "1234".toCharArray();
    private String db_name = "testDB";
    private String collection = "test_collection";

	// --- Test Methods ---
	@Before
    public void initMongoDB()
    {
        this.mongodb = new MongoDBAdaptor();
        this.bool = this.mongodb.connect(db_host, db_port, db_name);
        this.mongodb.setCollection(collection);
    }
	
	@After 
    public void closeMongoDB()
    {
//        this.mongodb.disconnect();
    }

    @Test
    public void testConnect() 
    {
    	System.out.println("@Test - Connect");
    	
    	// simple connect test
    	assertTrue(this.bool);
    	
    	// connect with user details 
    	this.mongodb.addUser(db_user, db_pass);
        boolean result = this.mongodb.connect(
                db_host, 
                db_port, 
                db_user,
                db_pass,
                db_name);
        assertTrue(result);
    }

    @Test
    public void testDisconnect() 
    {
    	System.out.println("@Test - Disconnect");
        assertTrue(this.bool);
    }
    
    @Test
    public void testInsert()
    {
    	System.out.println("@Test - Insert");
    	
    	// simple insert test 
    	boolean result = this.mongodb.insert("{ 'name': 'test'}");
    	assertTrue(result);
    }
    
    @Test public void testRemove() 
    {
    	System.out.println("@Test - Remove");
    	
    	// simple remove test 
    	boolean result = this.mongodb.remove("{ 'name': 'test'}");
    	assertTrue(result);
    	
    	// remove all test
    	this.mongodb.insert("{ 'name': 'test'}");
    	this.mongodb.insert("{ 'name': 'test1'}");
    	this.mongodb.insert("{ 'name': 'test2'}");
    	result = this.mongodb.removeAll();
    	assertTrue(result);
    }
    
    


}
