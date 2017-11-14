/**
 * 
 */
package edu.gatech.dynodroid.utilities;

/**
 * @author machiry
 *
 */
public abstract class TraceLogger {
	/***
	 * This method is used to log the provided data according to the provided category
	 * @param category the category to which the provided data belongs to
	 * @param data the data the needs to be logged
	 */
	public abstract void addTraceData(String category,String data);
	
	/***
	 * This method will return the trace file that is currently used by the Tracer/Logger for logging
	 * @return filename the file in which the trace is generated
	 */
	public abstract String getTraceFile();
	
	/***
	 * This method will end the tracing data..
	 * this might add some footers if required
	 */
	public abstract void endTraceLog();
	
	/**
	 * This will add the provided line as it is to the trace file
	 * @param line the target line that needs to be added.
	 */
	public abstract void addTraceLine(String line);
}
