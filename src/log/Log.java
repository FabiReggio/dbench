/*
 * Log logs messages to a specified file to be viewed later on. This class 
 * should be used very sparingly as the write method is not necessarily very 
 * efficient. The write method opens and closes the log file every time, taking 
 * considerable computing resources if used intensively.
 * @author Chris Choi
 */
package log;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.IOException;

public class Log {
	// --- Attributes ---
	private String fp; // File 
	private boolean append;
	private String x = "[Log] ";

	// --- Constructor ---
	public Log(String file_path, boolean append) {
		this.fp = file_path;
		this.append = append;
	}
	
	public Log(String file_path) {
		this.fp = file_path;
		this.append = true;
	}
	
	public Log() {
		this.fp = "debug.log";
		this.append = false;
	}
	
	// --- Methods ---
	public void write(String msg) {
		try {
		FileWriter fw = new FileWriter(fp, append);
		PrintWriter pw = new PrintWriter(fw);
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = null;
		df = new SimpleDateFormat("EEE, dd MMM yyyy H:mm:ss z");
		
		pw.printf("%s> %s\n", df.format(cal.getTime()), msg);
		pw.close();
		} catch (IOException e) {
			System.out.println(x + "ERROR! " + e);
		}
	}
}
