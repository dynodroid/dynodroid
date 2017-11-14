/**
 * 
 */
package edu.gatech.dynodroid.utilities;

import java.io.File;
import java.io.PrintStream;

/**
 * @author machiry
 * 
 */
public class MonkeyTraceLogger extends TraceLogger {

	public static final String sleepCategory = "UserWait(%s)";
	public static final String tapCategory = "Tap(%s)";
	public static final String keyPressCategory = "DispatchKey(5050,5050,%s,%s,0,0,0,0)";
	public static final String keySinglePressCategory = "DispatchKey(5000,5000,0,%s,0,0,0,0)";
	public static final String commentCategory = "#";

	private String monkeyTraceFile = "";
	private PrintStream logobject = null;

	public MonkeyTraceLogger(String logFileName) throws Exception {
		try {
			logobject = new PrintStream(new File(logFileName));
			logobject.println("count = 3");
			logobject.println("speed = 1000");
			logobject.println("start data >>");
			logobject.flush();
			monkeyTraceFile = logFileName;
		} catch (Exception e) {
			Logger.logException(e);
			throw e;
		}
	}
	
	@Override
	public String getTraceFile(){
		return monkeyTraceFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.utilities.Logger#addTraceData(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public synchronized void addTraceData(String category, String data) {
		try{
			if(category.equalsIgnoreCase(sleepCategory)){
				logLine(String.format(sleepCategory, data));
			}
			else if(category.equalsIgnoreCase(tapCategory)){
				logLine(String.format(tapCategory, data));
			}
			else if(category.equalsIgnoreCase(keyPressCategory)){
				logLine(String.format(keyPressCategory, "0",data));
				logLine(String.format(keyPressCategory, "1",data));
			}
			else if(category.equalsIgnoreCase(keySinglePressCategory)){
				logLine(String.format(keySinglePressCategory,data));
			}
			else if(category.equalsIgnoreCase(commentCategory)){
				logLine(commentCategory+" "+data);
			}
		} catch(Exception e){
			Logger.logException(e);
		}

	}
	
	private void logLine(String message){
		synchronized (logobject) {
			logobject.println(message);
		}
	}
	
	@Override
	public void finalize(){
		try{
			logobject.flush();
			logobject.close();
		} catch(Exception e){
			Logger.logException(e);
		}
	}

	@Override
	public void endTraceLog() {
		try{
			this.logobject.close();
		} catch(Exception e){
			
		}
		
	}

	@Override
	public void addTraceLine(String line) {
		logLine(line);		
	}

}
