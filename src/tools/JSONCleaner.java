package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import twitter4j.Status;

public class JSONCleaner {
	// --- Fields ---
	private String current_line = "";
	
	// --- Constructors ---
	public JSONCleaner() {}
	
	// --- Methods ---
	public String escapeNewLine(String line)
	{
		String cleaned_line = "";
		cleaned_line = line.replaceAll("\n", "\\n");
		return cleaned_line;
	}
	
	public String parseLine(String line) 
	{
		String parsed_line = "";
		
		if (line.substring(0).equals("[") || line.substring(0).equals("]")){}
		
		return parsed_line;
	}
	
	public void cleanJSONFile(String fp)
	{
		String json_string = "";
		Status tweet = null;
		
		try {
			File json_file = new File(fp);
			JsonFactory json_factory = new JsonFactory();
//			ObjectMapper mapper = ObjectMapper(json_factory);
			JsonParser json_parser = json_factory.createJsonParser(json_file);
			
			while (json_parser.nextToken() != JsonToken.END_ARRAY) {
				
				
				
			}
			
//			FileWriter fstream = new FileWriter(fp + ".cleaned");
//			BufferedWriter out = new BufferedWriter(fstream);
			
			
			
			
		
//			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
