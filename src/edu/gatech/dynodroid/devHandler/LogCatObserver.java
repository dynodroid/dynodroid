package edu.gatech.dynodroid.devHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

public class LogCatObserver {
	public String deviceName;
	public boolean monitor = false;
	public LogCatObserverThread targetThread;
	public ArrayList<MonitoringClient> logFileFilters = new ArrayList<MonitoringClient>();

	public LogCatObserver(String devName) {
		this.deviceName = devName;
		this.targetThread = new LogCatObserverThread(deviceName);
		this.logFileFilters = this.targetThread.logFileFilters;
		Thread newThread = new Thread(this.targetThread);
		newThread.start();
	}

	public void startMonitoring() {
		this.targetThread.startMonitoring();
	}

	public void stopMonitoring() {
		this.targetThread.stopMonitoring();
	}

	public ArrayList<String> getLogEntries() {
		return this.targetThread.getLogEntries();
	}

	public void cleanLogEntries(String fileName) {
		this.targetThread.cleanLogEntries(fileName);
	}

	public String getDeviceName() {
		return this.deviceName;
	}

	public void quitMonitoring() {
		this.targetThread.quitMonitoring = true;
	}

}

class LogCatObserverThread implements Runnable {

	public boolean monitor = false;
	private Object sync = new Object();
	private ArrayList<String> logEntries = new ArrayList<String>();
	public ArrayList<MonitoringClient> logFileFilters = new ArrayList<MonitoringClient>();
	public boolean isMontoringPossible = false;
	public boolean quitMonitoring = false;

	public String deviceName;

	// public ADevice targetDevice;

	public LogCatObserverThread(String devName) {
		this.deviceName = devName;
	}

	@Override
	public void run() {
		Process theProcess = null;
		BufferedReader inStream = null;
		String tempStr = null;
		String javaCommandLine = "adb -s " + this.deviceName + " logcat";
		boolean allFine = true;
		try {
			theProcess = Runtime.getRuntime().exec(javaCommandLine);
			Logger.logInfo("Running_M3:"+javaCommandLine);
		} catch (IOException e) {
			Logger.logException(e);
			allFine = false;
		}

		// read from the called program's standard output stream
		try {
			if (allFine) {
				this.isMontoringPossible = true;
				inStream = new BufferedReader(new InputStreamReader(
						theProcess.getInputStream()));
				while (!(this.quitMonitoring)
						&& ((tempStr = inStream.readLine()) != null)) {
					// Logger.logInfo(tempStr);
					if (!isLineConsumed(tempStr)) {
						synchronized (sync) {
							if (this.monitor) {
								logEntries.add(tempStr);
							}
						}
					}
				}
				// theProcess.destroy();
			} else {
				Logger.logError("Error occured while trying to monitor device output:"
						+ this.deviceName);
			}
		} catch (IOException e) {
			Logger.logException(e);
		}

	}

	private boolean isLineConsumed(String targetLine) {
		boolean hasConsumed = false;
		for (MonitoringClient m : logFileFilters) {
			try {
				// Yes, we don't discriminate between monitors
				// consumption of log entry
				// Consumption by one client doesn't affect other clients
				// say : EqualOppurtunityEmployer Pattern :P
				hasConsumed = m.consume(targetLine) || hasConsumed;
			} catch (Exception e) {
				Logger.logException(e);
			}
		}

		return hasConsumed;
	}

	public ArrayList<String> getLogEntries() {
		synchronized (sync) {
			ArrayList<String> logEn = new ArrayList<String>();
			logEn.addAll(logEntries);
			return logEn;
		}
	}

	public boolean cleanLogEntries(String fileName) {
		for (MonitoringClient m : logFileFilters) {
			m.cleanMonitoringInfo();
		}
		synchronized (sync) {
			try {
				if (fileName != null) {
					if (!FileUtilities.appendLinesToFile(fileName, logEntries)) {
						Logger.logError("Problem occured while writing entries to the provied file, provided file:"
								+ fileName == null ? "NULL" : fileName);
					}
				}
				logEntries.clear();
				return true;
			} catch (Exception e) {
				Logger.logException(e);
			}
			return false;
		}
	}

	public void startMonitoring() {
		synchronized (sync) {
			this.monitor = true;
		}
		for (MonitoringClient m : logFileFilters) {
			m.startMonitoring();
		}
	}

	public void stopMonitoring() {
		for (MonitoringClient m : logFileFilters) {
			m.stopMonitoring();
		}
		synchronized (sync) {
			this.monitor = false;
		}
	}

}
