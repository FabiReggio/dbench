package io;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

public class DataManager implements IDataManager 
{
	// --- Fields ---
	
	
	// --- Constructors ---
	public DataManager() {}
		
	// --- Methods ---
	public void loadData(String fp) 
	{
		
		
	}
	
	public void parseJson(String file) 
	{
		try {
			JsonFactory json_factory = new JsonFactory();
			JsonParser json_parser = json_factory.createJsonParser(file);
		} catch (JsonParseException e) {
			System.out.println("error: " + e);
		} catch (IOException e) {
			System.out.println("error: " + e);
		}
	}
	
	
}
