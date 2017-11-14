package edu.gatech.dynodroid.devHandler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.android.ddmlib.IDevice;

import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

public class EmulatorInstance {
	private static String androidDir = null;
	private static String createAvdScript = null;
	private EmulatorRunner emuRunner = null;
	private String targetAvdName = null;

	// Maximum time to be slept before giving up on the emulator
	private static int maxEmulatorStartTime = 60 * 5;

	private EmulatorInstance() {
		if (androidDir == null) {
			androidDir = PropertyParser.avdLocation;
		}
		if (createAvdScript == null) {
			createAvdScript = PropertyParser.createEmuScript;
		}
	}

	public String getEmuName() {
		if (this.emuRunner.getLogCatObserver() != null) {
			return this.emuRunner.getLogCatObserver().getDeviceName();
		}
		return null;
	}

	public LogCatObserver getLogCatObserver() {
		return this.emuRunner.getLogCatObserver();
	}

	public ArrayList<String> getKernelLogEntries() {
		return this.emuRunner.getKernelLogEntries();
	}

	public boolean cleanKernelLogEntries(String fileName) {
		return this.emuRunner.cleanKernelLogEntries(fileName);
	}

	public boolean addMonitors(ArrayList<MonitoringClient> kernelMonitors) {
		if (kernelMonitors != null) {
			emuRunner.kernelLogFilters.addAll(kernelMonitors);
			return true;
		}
		return false;
	}

	public void startMonitoring() {
		this.emuRunner.startMonitoring();
	}

	public void stopMonitoring() {
		this.emuRunner.stopMonitoring();
	}

	public boolean isReady() {
		return this.emuRunner.isEmulatorReady();
	}

	public boolean destroyEmulator() {
		if (this.emuRunner != null) {
			try {
				
				this.emuRunner.stopEmulator();
				File iniFile = new File(PropertyParser.avdLocation + "/"
						+ this.targetAvdName + ".ini");
				Logger.logInfo("Before Trying to delete");
				iniFile.delete();
				Logger.logInfo("Trying to delete:"+ PropertyParser.avdLocation + "/"+this.targetAvdName+".avd");
				Logger.logInfo("Trying to delete:"+PropertyParser.avdLocation + "/"
						+ this.targetAvdName + ".ini");
				FileUtils.deleteDirectory(new File(PropertyParser.avdLocation + "/"+this.targetAvdName+".avd"));
				
			} catch (Exception e) {
				Logger.logException(e);
			}
			return true;
		}
		return false;
	}

	// this method is to get new emulator instance
	// for android
	public synchronized static EmulatorInstance getNewEmulator() {
		EmulatorInstance retVal = null;
		try {
			File[] existingAvds = FileUtilities.getAllDirectories(new File(
					PropertyParser.avdLocation));

			FileFilter freeAvdsFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith("lock");
				}
			};

			File targetFreeEmulator = null;
			if (existingAvds != null) {
				for (File f : existingAvds) {
					File[] lockFiles = f.listFiles(freeAvdsFilter);
					if (lockFiles == null || lockFiles.length == 0) {
						Logger.logInfo("Found a free avd:"
								+ f.getAbsolutePath());
						targetFreeEmulator = f;
						break;
					}
				}
			}

			String nonExistingAvd = null;

			// All the existing avds are busy
			if (targetFreeEmulator == null) {
				ArrayList<String> avdNames = new ArrayList<String>();
				if (existingAvds != null) {
					for (File f : existingAvds) {
						avdNames.add(f.getName().substring(0,
								f.getName().length() - 4));
					}
				}
				for (int i = 1; i < 100; i++) {
					String avdName = "emu" + i;
					if (!avdNames.contains(avdName)) {
						nonExistingAvd = avdName;

						break;
					}
				}
				if (nonExistingAvd != null) {
					ExecHelper.RunProgram(PropertyParser.createEmuScript + " "
							+ nonExistingAvd, true);
					targetFreeEmulator = new File(androidDir + "/"
							+ nonExistingAvd + ".avd");
				}

			}

			if (targetFreeEmulator != null) {
				String targetFreeEmulatorName = targetFreeEmulator
						.getName()
						.substring(0, targetFreeEmulator.getName().length() - 4);
				try {

					// This is very bad hack to determine the mapping between
					// avd and emulator instance
					IDevice[] currentDevices = ADevice.adbBridge.getDevices();
					ArrayList<String> prevDevices = new ArrayList<String>();
					if (currentDevices != null) {
						for (IDevice dev : currentDevices) {
							prevDevices.add(dev.toString());
						}
					}
					EmulatorRunner temp = new EmulatorRunner(
							targetFreeEmulatorName);
					Thread t = new Thread(temp);
					t.start();
					// Sleep for 10 seconds for the emulator to fire up
					Thread.sleep(10 * 1000);

					// Get the emulator name corresponding to the avd we just
					// ran.
					currentDevices = ADevice.adbBridge.getDevices();
					for (IDevice dev : currentDevices) {
						if (!prevDevices.contains(dev.toString())) {
							if (!temp.startLogCatObserving(dev.toString())) {
								Logger.logError("Problem Occured while Trying to run logcat on the emulator");
								t.interrupt();
								temp = null;
							}
							break;
						}
					}
					if (temp != null) {
						EmulatorInstance newEmu = new EmulatorInstance();
						newEmu.emuRunner = temp;
						retVal = newEmu;
						newEmu.targetAvdName = targetFreeEmulatorName;

						int timeSlept = 0;
						// Sleep till the emulator is ready
						while (!temp.isEmulatorReady()
								&& timeSlept < maxEmulatorStartTime) {
							Thread.sleep(2 * 1000);
							timeSlept += 2;
						}

						if (!temp.isEmulatorReady()) {
							retVal = null;
						}
					}
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		} catch (Exception e) {
			Logger.logException(e);
		}

		return retVal;

	}

}
