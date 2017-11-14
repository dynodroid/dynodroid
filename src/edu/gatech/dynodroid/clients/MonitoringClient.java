/**
 * 
 */
package edu.gatech.dynodroid.clients;

import java.io.File;
import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;


/**
 * @author machiry
 *
 */
public abstract class MonitoringClient {	
	
	public ADevice finalTargetDevice = null;
	public String filterString = "";
	public ArrayList<String> logEntries = new ArrayList<String>();
	private String logFile = null;
	protected boolean toMonitor = false;
	protected WidgetSelectionStrategy feedBack=null;
	protected int targetAppUid = -1;
	

	
	/***
	 * 
	 * This method is used to initialize this monitoring client 
	 * all the initialization stuff of the module should be taken care here.
	 * 
	 * @param logFile The file to which the entries to be written , when stopMonitoring is called
	 * @return true/false depending on the success or failure of the method
	 */
	public boolean initializeMonitoring(String logFile,WidgetSelectionStrategy feedBa) {
		if (logFile != null) {
			File newFile = new File(logFile);
			if (!newFile.getParentFile().exists()) {
				FileUtilities.createDirectory(newFile.getParentFile()
						.getAbsolutePath());
			}
			this.logFile = newFile.getAbsolutePath();
			this.feedBack = feedBa;
			return true;
		}
		return false;
	}

	public void addTargetAppId(int targetAppid){
		this.targetAppUid = targetAppid;
	}
	
	/***
	 * This method initialized the monitoring and the monitoring client should initialize all its structures.
	 * @return true/false depending on the success or failure of the tasks
	 */
	public boolean startMonitoring() {
		this.toMonitor = true;
		return true;
	}

	/***This method is used to indicate to stop monitoring 
	 * this should make all monitoring clients write their contents 
	 * to the log file if necessary
	 * @return  true/false depending on the success or failure of the method
	 */
	public boolean stopMonitoring() {
		this.toMonitor = false;
		return true;
	}

	/***
	 * Consumes the given log entry
	 * @param entry
	 * @return true/false depending on whether it is able to consume the log or not
	 */
	public boolean consume(String entry) {
		if (this.toMonitor && entry != null) {
			if (entry.contains(filterString)) {
				synchronized (logEntries) {
					logEntries.add(entry);
				}
				return true;
			}
		}
		return false;
	}

	/***
	 * This method is used to add tag to the monitoring
	 * basically to add tags like button pressed 
	 * so that we know what events occurred after what button pressed.
	 * @param tag
	 */
	public void addTag(String tag) {
		if (tag != null) {
			synchronized (logEntries) {
				logEntries.add(tag);
			}
		}

	}

	public void cleanMonitoringInfo() {
		synchronized (logEntries) {
			if (logFile != null) {
				if(logEntries.size() == 0){
					logEntries.add("No Logs Recorded");
				}
				FileUtilities.appendLinesToFile(logFile, logEntries);
			}
			logEntries.clear();
		}
	}
}
