package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;


public class FileManager 
{
	// --- Fields ---
	private FileWriter file_writer;
	
	// --- Constructors ---
	public FileManager() {}
	
	// --- Methods ---
	public String[] readFile(String file_path)
	{
		String[] lines;
		try {
			String raw_data = FileUtils.readFileToString(new File(file_path));
			lines = StringUtils.split(raw_data, '\n');
			return lines;
		} catch (IOException e) {
			System.out.println("error: " + e);
			return null;
		}
	}
	
	public boolean prepFileWriter(String file_name)
	{
		try {
			this.file_writer = new FileWriter(file_name);
		} catch (IOException e) {
			System.out.println("error: " + e);
			return false;
		}
		return true;
	}
	
	public boolean csvLogEvent(String[] elements) 
	{
		try {
			for (String element : elements) {
				this.file_writer.append(element);
				this.file_writer.append(",");
			}
			this.file_writer.append("\n");
		} catch (IOException e) {
			System.out.println("error: " + e);
			return false;
		}
		return true;
	}
	
	public boolean closeFileWriter() 
	{
		try {
			this.file_writer.close();
		} catch (IOException e) {
			System.out.println("error: " + e);
			return false;
		}
		return true;
	}
}
