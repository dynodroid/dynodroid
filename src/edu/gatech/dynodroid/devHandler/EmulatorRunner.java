package edu.gatech.dynodroid.devHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

public class EmulatorRunner implements Runnable {

	public String emuName = null;
	private LogCatObserver targetLogCatObserver = null;
	private ArrayList<String> kernelMessages = new ArrayList<String>();
	private Object sync = new Object();
	private boolean stopEmulator = false;
	public boolean monitor = false;
	public static final String emulatorReadyMessage = "done scanning volume external";
	public ArrayList<MonitoringClient> kernelLogFilters = new ArrayList<MonitoringClient>();
	private boolean isEmulatorRunning = true;

	public EmulatorRunner(String emulatorName) throws Exception {
		if (emulatorName != null && emulatorName.length() > 0) {
			this.emuName = emulatorName;
		} else {
			throw new Exception("Invalid Emulator Name Provided");
		}
	}

	public LogCatObserver getLogCatObserver() {
		return targetLogCatObserver;
	}

	public boolean startLogCatObserving(String devName) {
		try {
			if (devName != null && this.targetLogCatObserver == null) {
				this.targetLogCatObserver = new LogCatObserver(devName);
			}
			this.targetLogCatObserver.startMonitoring();
			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	public boolean isEmulatorReady() {
		if (targetLogCatObserver != null && this.isEmulatorRunning) {
			ArrayList<String> entries = this.targetLogCatObserver
					.getLogEntries();
			if (entries != null) {
				for (String s : entries) {
					if (s.contains(emulatorReadyMessage)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void stopEmulator() {
		try {
			this.stopEmulator = true;
			// Wait till emulator dies
			if (this.targetLogCatObserver != null) {
				this.targetLogCatObserver.quitMonitoring();
			}
			Thread.sleep(2000);
		} catch (Exception e) {
			Logger.logException(e);
		}
	}

	@Override
	public void run() {
		String emulatorRunCommand = "emulator -avd " + emuName
				+ " -verbose -scale 0.70 -show-kernel" +(PropertyParser.isManualMode?"":" -no-window") +" -kernel "
				+ PropertyParser.kernelModulesLocation + "/zImage -system "
				+ PropertyParser.customSystemImg + " -ramdisk "
				+ PropertyParser.customRamDiskImg;
		Process theProcess = null;
		BufferedReader inStream = null;
		String tempStr = null;
		try {
			Logger.logInfo("Starting to Run Emulator:" + emulatorRunCommand);
			theProcess = Runtime.getRuntime().exec(emulatorRunCommand);
		} catch (IOException e) {
			Logger.logException(e);
		}

		try {
			if (theProcess != null) {
				inStream = new BufferedReader(new InputStreamReader(
						theProcess.getInputStream()));
				while (!(this.stopEmulator)
						&& (tempStr = inStream.readLine()) != null) {
					// Logger.logInfo(tempStr);
					if (this.monitor) {
						if (!isLineConsumed(tempStr)) {
							synchronized (sync) {
								kernelMessages.add(tempStr);
							}
						}
					}
				}
				this.isEmulatorRunning = false;

				// Here even if we get exception we don't care and we continue
				theProcess.destroy();
			} else {
				Logger.logError("Problem ocured while trying to start the emulator");
			}

		} catch (IOException e) {
			Logger.logException(e);
		}
	}

	private boolean isLineConsumed(String targetLine) {
		boolean hasConsumed = false;
		for (MonitoringClient m : kernelLogFilters) {
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

	public ArrayList<String> getKernelLogEntries() {
		synchronized (sync) {
			ArrayList<String> logEn = new ArrayList<String>();
			logEn.addAll(kernelMessages);
			return logEn;
		}
	}

	public boolean cleanKernelLogEntries(String fileName) {
		try {
			for (MonitoringClient m : kernelLogFilters) {
				m.cleanMonitoringInfo();
			}
			synchronized (kernelMessages) {
				if (fileName != null) {
					if (!FileUtilities.appendLinesToFile(fileName,
							kernelMessages)) {
						Logger.logError("Problem occured while writing entries to the provied file, provided file:"
								+ fileName == null ? "NULL" : fileName);
					}
				}
				kernelMessages.clear();
			}

			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	public void startMonitoring() {
		synchronized (sync) {
			this.monitor = true;
		}
		for (MonitoringClient m : kernelLogFilters) {
			m.startMonitoring();
		}
	}

	public void stopMonitoring() {
		for (MonitoringClient m : kernelLogFilters) {
			m.stopMonitoring();
		}
		synchronized (sync) {
			this.monitor = false;
		}
	}

}
