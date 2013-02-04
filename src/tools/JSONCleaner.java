package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

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
	
	public void cleanJSONFile(String fp)
	{
		try {
			File file = new File(fp);
			
			FileWriter fstream = new FileWriter(fp + ".cleaned");
			BufferedWriter out = new BufferedWriter(fstream);
		
			LineIterator line_iter = FileUtils.lineIterator(file);
			while (line_iter.hasNext()) {
				this.current_line = line_iter.next();
				out.write(escapeNewLine(this.current_line));
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
