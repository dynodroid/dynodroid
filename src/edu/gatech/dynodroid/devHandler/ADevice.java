/**
 * 
 */
package edu.gatech.dynodroid.devHandler;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * This class is the abstract class for Android Device different implementations
 * (a.k.a for real device or for emulator) should use this base class to
 * implement the functionality
 * 
 * @author machiry
 * 
 */
public abstract class ADevice {

	//private static List<String> assignedDevices = new ArrayList<String>();
	private static HashMap<String, EmulatorInstance> knownEmulators = new HashMap<String, EmulatorInstance>();

	/**
	 * This is the lock object to access all the static objects Access to all
	 * static objects should be guarded by this object
	 */
	private static Object sSync = new Object();
	protected static boolean adbInitialized = false;
	protected static AndroidDebugBridge adbBridge = null;

	/***
	 * This method gets the name of the free device that can be used for testing
	 * 
	 * @return name of the free device
	 */
	public static String getFreeDevice() {
		ADeviceSetup.isInitialized();
		initializeBridge();
		synchronized (sSync) {

			String freeDevice = null;

			// get the current devices and return the device that is not
			// assigned
			IDevice[] recognizedDevices = adbBridge.getDevices();
			/*if (recognizedDevices != null) {
				for (IDevice currDev : recognizedDevices) {
					if (currDev.isOnline()
							&& !assignedDevices.contains(currDev.toString())) {
						assignedDevices.add(currDev.toString());
						freeDevice = currDev.toString();
						break;
					}
				}
			}*/

			if (recognizedDevices != null
					&& recognizedDevices.length < PropertyParser.maxNoOfEmulators) {
				synchronized (knownEmulators) {
					EmulatorInstance temp = EmulatorInstance.getNewEmulator();
					if (temp == null) {
						Logger.logError("Unable to create new emulators");
					} else {
						if (temp.getEmuName() != null) {
							knownEmulators.put(temp.getEmuName(), temp);
							//assignedDevices.add(temp.getEmuName());

							freeDevice = temp.getEmuName();
						} else {
							Logger.logInfo("Problem occured while trying to get the emulator name");
						}
					}
				}
			}

			return freeDevice;

		}
	}
	
	public static synchronized boolean hasFreeDevices(){
		IDevice[] recognizedDevices = adbBridge.getDevices();
		return recognizedDevices != null
				&& recognizedDevices.length < PropertyParser.maxNoOfEmulators;
	}

	public static LogCatObserver getLogCatObserver(String devName) {
		if (devName != null) {
			synchronized (knownEmulators) {
				if (knownEmulators.containsKey(devName)) {
					return knownEmulators.get(devName).getLogCatObserver();
				}
			}

		}
		return null;
	}

	public static AndroidDebugBridge getAbdBridge() {
		return adbBridge;
	}

	private static boolean initializeBridge() {
		synchronized (sSync) {
			if (!adbInitialized) {
				try {
					AndroidDebugBridge.init(false);
					//AndroidDebugBridge.init(true);
					adbBridge = AndroidDebugBridge.createBridge(
							ADeviceSetup.adbPath, true);
					Thread.sleep(ADeviceSetup.timeForAdbInitialization);
					
					//New Changes
					//AndroidDebugBridge.addClientChangeListener(new ClientChangeListener());
					/*try{
					ClientData.setMethodProfilingHandler(new MethodProfileHandler(new TextLogger(PropertyParser.baseWorkingDir+"/MethodProfilerLog.txt")));
					} catch(Exception e){
						Logger.logError("Problem occured while setting the profile handler");
						Logger.logException(e);
					}	*/				
					adbInitialized = true;
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
			return adbInitialized;
		}
	}

	/*private static boolean isDeviceBusy(String deviceName) {
		assert (initializeBridge());
		synchronized (sSync) {
			return assignedDevices.contains(deviceName);
		}
	}*/

	public static IDevice getIDevice(String deviceName) {
		assert (initializeBridge());
		//assert (!isDeviceBusy(deviceName));
		synchronized (sSync) {
			IDevice targetDevice = null;
			IDevice[] recognizedDevices = adbBridge.getDevices();
			for (IDevice currDev : recognizedDevices) {
				if (currDev.isOnline()
						&& currDev.toString().equalsIgnoreCase(deviceName)) {
					targetDevice = currDev;
					break;
				}
			}
			return targetDevice;
		}
	}

	protected static void releaseDevice(String deviceName) {
		/*synchronized (sSync) {
			assignedDevices.remove(deviceName);
		}*/
	}

	protected static void destroyDevice(String devName) {
		if (devName != null) {
			Logger.logInfo("In destroyDevice");
			synchronized (knownEmulators) {
				Logger.logInfo("Got Lock");
				if (knownEmulators.containsKey(devName)) {
					ExecHelper.RunProgram("adb -s "+devName+" emu kill", true);
					if (knownEmulators.get(devName).destroyEmulator()) {
						Logger.logInfo("Emulator :" + devName
								+ " destroyed sucessfully");
					} else {
						Logger.logInfo("problem occured while destroying the Emulator :"
								+ devName);
					}
					knownEmulators.remove(devName);
				} else {
					Logger.logInfo("Unable to find emulator in the knownemulators list");
				}
			}
			releaseDevice(devName);
		}
	}

	protected static void startKernelLogMonitoring(String devName) {
		synchronized (knownEmulators) {
			if (knownEmulators.containsKey(devName)) {
				knownEmulators.get(devName).startMonitoring();
			}
		}
	}

	protected static void stopKernelLogMonitoring(String devName) {
		synchronized (knownEmulators) {
			if (knownEmulators.containsKey(devName)) {
				knownEmulators.get(devName).stopMonitoring();
			}
		}
	}

	protected static void cleanKernelLogEntriesS(String devName, String fileName) {
		synchronized (knownEmulators) {
			if (knownEmulators.containsKey(devName)) {
				knownEmulators.get(devName).cleanKernelLogEntries(fileName);
			}
		}
	}

	protected static ArrayList<String> getKernelLogEntries(String devName) {
		synchronized (knownEmulators) {
			if (knownEmulators.containsKey(devName)) {
				knownEmulators.get(devName).getKernelLogEntries();
			}
		}
		return null;
	}

	protected static boolean addLogCatMonitors(String devName,
			ArrayList<MonitoringClient> logCatMonitors) {
		EmulatorInstance emuInstance = null;
		if (logCatMonitors != null && devName != null) {
			synchronized (knownEmulators) {
				if (knownEmulators.containsKey(devName)) {
					emuInstance = knownEmulators.get(devName);
				}
			}

			if (emuInstance != null && emuInstance.getLogCatObserver() != null) {
				emuInstance.getLogCatObserver().logFileFilters
						.addAll(logCatMonitors);
				return true;
			}
		}
		return false;
	}

	protected static boolean addkernelLogMonitors(String devName,
			ArrayList<MonitoringClient> kernelMonitors) {
		EmulatorInstance emuInstance = null;
		if (kernelMonitors != null && devName != null) {
			synchronized (knownEmulators) {
				if (knownEmulators.containsKey(devName)) {
					emuInstance = knownEmulators.get(devName);
				}
			}

			if (emuInstance != null) {
				emuInstance.addMonitors(kernelMonitors);
				return true;
			}
		}
		return false;
	}

	/***
	 * This methods gets the file from the device and saves it to the provided
	 * locationOnDisc
	 * 
	 * @param locationOnDevice
	 *            on device absolute location
	 * @param locationOnDisc
	 *            on disc absolute location
	 * @return true/false depending on whether the pull is sucessfull or not
	 *         respectively
	 */
	public abstract boolean getFileFromDevice(String locationOnDevice,
			String locationOnDisc);

	/***
	 * This methods puts the file from the disc and saves it to the provided
	 * locationOnDevice
	 * 
	 * @param locationOnDisc
	 *            on disc absolute location of the file to be put
	 * @param locationOnDevice
	 *            on device absolute location
	 * @return true/false depending on whether the push is sucessfull or not
	 *         respectively
	 */
	public abstract boolean putFileInToDevice(String locationOnDisc,
			String locationOnDevice);

	/***
	 * This method executes the provided command on device using shell
	 * 
	 * @param command
	 *            command to be executed on device
	 * @param timeOutInMilliSec
	 *            timeout for command to respond
	 * @return response of the shell command executed
	 */
	public abstract ArrayList<String> executeShellCommand(String command,
			int timeOutInMilliSec);

	/***
	 * This method executes the provided command on device using shell and with
	 * default timeout value
	 * 
	 * @param command
	 *            command to be executed on device
	 * @return response of the shell command executed
	 */
	public abstract ArrayList<String> executeShellCommand(String command);

	/***
	 * This method returns the device name that this object is handling
	 * 
	 * @return human readable device name
	 */
	public abstract String getDeviceName();

	/***
	 * This method sends SMS to the device with the given number and message
	 * 
	 * @param number
	 *            The number from which the SMS needs to be sent
	 * @param message
	 *            The target message that needs to be sent
	 * @return true on success or false on failure
	 */
	public abstract boolean sendSMS(String number, String message);

	/***
	 * This method is used to clean the location /sdcard this is achieved by
	 * deleting all the files in the directory
	 * 
	 * @return true on success / false on failure
	 */
	public abstract boolean cleanSDCard();

	/***
	 * This method is used to un install the provided app package form the
	 * device
	 * 
	 * @param targetPackageName
	 *            that target package that needs to be un installed
	 * @return output after un installing the app package from the device
	 */
	public abstract String uninstallAppPackage(String targetPackageName);

	/***
	 * This method is used to create TCP forward from host machine to the device
	 * 
	 * @param srcPortNumber
	 *            src port number of the host
	 * @param destPortNumber
	 *            dst port number of the device
	 * @return true on success or false on failure
	 */
	public abstract boolean createForward(int srcPortNumber, int destPortNumber);

	/***
	 * This method free the current device
	 * 
	 * @return true/ false depending on whether the operation is sucessfull or
	 *         not
	 */
	public abstract boolean freeDevice();

	public abstract boolean destroyDevice();

	public abstract void startLogMonitoring();

	public abstract void stopLogMonitoring();

	public abstract void cleanLogEntries(String targetFile);

	public abstract void startKernelLogMonitoring();

	public abstract void stopKernelLogMonitoring();

	public abstract void cleanKernelLogEntries(String targetFile);

	public abstract boolean installApk(String apkFile, boolean reinstall);

	public abstract boolean addLogCatMonitors(
			ArrayList<MonitoringClient> logCatMonitors);

	public abstract boolean addKerenelMonitots(
			ArrayList<MonitoringClient> kernelMonitors);
	
	public abstract DeviceConnection getDeviceConnection();
	
	public abstract boolean executeDeviceCommand(String command);

}
