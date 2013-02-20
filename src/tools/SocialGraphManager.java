package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.Graph;

public class SocialGraphManager 
{
	// --- Fields ---
	Graph graph = new TinkerGraph();
	
	// --- Constructor ---
	public SocialGraphManager() 
	{
		
	}
	
	// ---- Methods ---
	public boolean outputTweetSocialGraph(String fp)
	{
		boolean result = false;
		try {
			File output_file = new File(fp);
			OutputStream output_stream = new FileOutputStream(output_file);
			GraphMLWriter.outputGraph(graph, output_stream);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	

}
