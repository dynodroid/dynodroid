/**
 * 
 */
package edu.gatech.dynodroid.devHandler;

import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InvalidNameException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;

import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * @author machiry
 * 
 */
public class DeviceEmulator extends ADevice {

	private IDevice androidDevice = null;
	private static final int defaultShellCommandTimeOut = 10000;
	private String deviceName;
	
	//Just to be on safer side we limit defaults to more than maximum values
	public static final int maxEmulatorWidth = 500;
	public static final int maxEmulatorHeight = 1000;

	private static HashMap<String, LogCatObserver> logCatObserverMap = new HashMap<String, LogCatObserver>();
	//private static HashMap<String, DeviceEmulator> deviceEmulatorCache = new HashMap<String, DeviceEmulator>();

	private DeviceEmulator(String emulatorName) throws InvalidNameException {
		androidDevice = super.getIDevice(emulatorName);
		this.deviceName = emulatorName;
		if (androidDevice == null) {
			throw new InvalidNameException(
					"The provided device name doesn't exist or is busy handling other testing");
		}
		synchronized (logCatObserverMap) {
			if (!logCatObserverMap.containsKey(this.deviceName)) {
				LogCatObserver logCatObser = ADevice
						.getLogCatObserver(deviceName);
				if (logCatObser == null) {
					logCatObser = new LogCatObserver(this.deviceName);
				}
				logCatObserverMap.put(this.deviceName, logCatObser);
			}
		}
	}

	public static DeviceEmulator getFreeEmulator() {
		String freeDevice = ADevice.getFreeDevice();
		if (freeDevice == null) {
			return null;
		}
		try {
			/*synchronized (deviceEmulatorCache) {
				if (deviceEmulatorCache.containsKey(freeDevice)) {
					Logger.logInfo("DeviceEmulator Object found in cache, will be returned");
					return deviceEmulatorCache.get(freeDevice);
				} else {*/
					DeviceEmulator newDev = new DeviceEmulator(freeDevice);
					Logger.logInfo("Created New DeviceEmulator Object");
					//deviceEmulatorCache.put(freeDevice, newDev);
					return newDev;
				//}
			} catch (Exception e) {
			// Log Exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.devHandler.ADevice#getFileFromDevice(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean getFileFromDevice(String locationOnDevice,
			String locationOnDisc) {
		boolean gotFile = false;
		try {
			this.androidDevice.pullFile(locationOnDevice, locationOnDisc);
			gotFile = true;
		} catch (Exception e) {

		}

		return gotFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.devHandler.ADevice#putFileInToDevice(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean putFileInToDevice(String locationOnDisc,
			String locationOnDevice) {
		boolean placedFile = false;
		try {
			this.androidDevice.pushFile(locationOnDisc, locationOnDevice);
			placedFile = true;
		} catch (Exception e) {

		}
		return placedFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.devHandler.ADevice#executeShellCommand(java.lang.String,
	 * int)
	 */
	@Override
	public ArrayList<String> executeShellCommand(String command,
			int timeOutInMilliSec) {
		ArrayList<String> output = null;
		int retryCount = 4;
		boolean shellResponsive = true;
		StringOutputReceiver outputReceiver = new StringOutputReceiver();
		while (shellResponsive && (retryCount > 0)) {
			try {
				this.androidDevice.executeShellCommand(command, outputReceiver,
						timeOutInMilliSec);
				break;
			} catch (ShellCommandUnresponsiveException e) {
				Logger.logException("ShellCommandUnRespeonsive..will be retried");
				retryCount--;
			} catch (Exception e) {
				// shellResponsive = false;
				Logger.logException(e);
				retryCount--;
			}
		}
		output = outputReceiver.output;
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.devHandler.ADevice#executeShellCommand(java.lang.String)
	 */
	@Override
	public ArrayList<String> executeShellCommand(String command) {
		return executeShellCommand(command, defaultShellCommandTimeOut);
	}

	@Override
	public String getDeviceName() {
		return this.deviceName;
	}

	@Override
	public boolean sendSMS(String number, String message) {
		boolean retValue = false;
		try {
			int portNo = Integer.parseInt(getDeviceName().substring(
					getDeviceName().indexOf('-') + 1));
			DeviceConnection devC = new DeviceConnection(androidDevice, portNo);
			devC.sendCommand("sms send " + number + " " + message);
			retValue = true;
			devC.close();
		} catch (Exception e) {
			Logger.logException(getDeviceName() + ":SendSMS:" + e.getMessage());
		}
		return retValue;
	}

	@Override
	public boolean cleanSDCard() {
		// TODO: we can do this..but has problems
		// Prob1: MEDIA_SCANNER finished
		// this.executeShellCommand("rm -r /sdcard/*");
		return true;
	}

	@Override
	public String uninstallAppPackage(String targetPackageName) {
		String output = "";
		int retryCount = 4;
		boolean shellResponsive = true;
		while (shellResponsive && (retryCount > 0)) {
			try {
				output = this.androidDevice.uninstallPackage(targetPackageName);
				output = output == null ? "Success" : output;
				break;
			} catch (InstallException e) {
				Logger.logException("InstallException..will be retried");
				retryCount--;
			} catch (Exception e) {
				shellResponsive = false;
				Logger.logException(e);
			}
		}

		return output;
	}

	@Override
	public boolean createForward(int srcPortNumber, int dstPortNumber) {
		try {
			this.androidDevice.createForward(srcPortNumber, dstPortNumber);
			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	@Override
	public boolean freeDevice() {
		//releaseDevice(getDeviceName());
		return true;
	}

	private LogCatObserver getTargetObserver() {
		synchronized (logCatObserverMap) {
			if (logCatObserverMap.containsKey(deviceName)) {
				return logCatObserverMap.get(deviceName);
			}
		}
		return null;
	}

	@Override
	public void startLogMonitoring() {
		LogCatObserver targetObserver = getTargetObserver();
		if (targetObserver != null) {
			targetObserver.startMonitoring();
		}

	}

	@Override
	public void stopLogMonitoring() {
		LogCatObserver targetObserver = getTargetObserver();
		if (targetObserver != null) {
			targetObserver.stopMonitoring();
		}
	}

	@Override
	public void cleanLogEntries(String fileName) {
		LogCatObserver targetObserver = getTargetObserver();
		if (targetObserver != null) {
			targetObserver.cleanLogEntries(fileName);
		}
	}

	@Override
	public void startKernelLogMonitoring() {
		ADevice.startKernelLogMonitoring(deviceName);

	}

	@Override
	public void stopKernelLogMonitoring() {
		ADevice.stopKernelLogMonitoring(deviceName);
	}

	@Override
	public void cleanKernelLogEntries(String targetFile) {
		ADevice.cleanKernelLogEntriesS(deviceName, targetFile);
	}

	@Override
	public int hashCode() {
		return this.deviceName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ADevice) {
			ADevice that = (ADevice) o;
			return this.deviceName.equals(that.getDeviceName());
		}
		return false;
	}

	@Override
	public boolean addLogCatMonitors(ArrayList<MonitoringClient> logCatMonitors) {
		// return ADevice.addLogCatMonitors(this.deviceName, logCatMonitors);
		boolean retVal = false;
		LogCatObserver targetObserver = getTargetObserver();
		if (targetObserver != null && logCatMonitors != null) {
			try {
				targetObserver.logFileFilters.addAll(logCatMonitors);
				retVal = true;
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return retVal;
	}

	@Override
	public boolean addKerenelMonitots(ArrayList<MonitoringClient> kernelMonitors) {
		return ADevice.addkernelLogMonitors(this.deviceName, kernelMonitors);
	}

	@Override
	public boolean installApk(String apkFile, boolean reinstall) {
		int retryCount = 4;
		boolean shellResponsive = true;
		boolean retVal = false;
		while (shellResponsive && (retryCount > 0)) {
			try {
				retryCount--;
				String output = this.androidDevice.installPackage(apkFile,
						reinstall);
				output = output == null ? "Success" : output;
				retVal = true;
				break;
			} catch (InstallException e) {
				Logger.logException("InstallException..will be retried");
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return retVal;
	}

	@Override
	public boolean destroyDevice() {
		/*synchronized (deviceEmulatorCache) {
			deviceEmulatorCache.remove(deviceName);
		}*/
		synchronized (logCatObserverMap) {
			logCatObserverMap.remove(deviceName);
		}
		ADevice.destroyDevice(deviceName);
		freeDevice();
		return true;
	}

	@Override
	public DeviceConnection getDeviceConnection() {
		DeviceConnection retVal = null;
		try {
			int portNo = Integer.parseInt(getDeviceName().substring(
					getDeviceName().indexOf('-') + 1));
			retVal = new DeviceConnection(androidDevice, portNo);
		} catch (Exception e) {
			Logger.logException(getDeviceName() + ":GetDeviceConnection:"
					+ e.getMessage());
		}
		return retVal;
	}

	@Override
	public boolean executeDeviceCommand(String command) {
		boolean retValue = false;
		try {
			int portNo = Integer.parseInt(getDeviceName().substring(
					getDeviceName().indexOf('-') + 1));
			DeviceConnection devC = new DeviceConnection(androidDevice, portNo);
			devC.sendCommand(command);
			retValue = true;
			devC.close();
		} catch (Exception e) {
			Logger.logException(getDeviceName() + ":executeDeviceCommand:" + e.getMessage());
		}
		return retValue;
	}

}

class StringOutputReceiver extends MultiLineReceiver {

	public ArrayList<String> output = null;

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processNewLines(String[] arg0) {
		if (output == null) {
			output = new ArrayList<String>();
		}
		for (String s : arg0) {
			output.add(s);
		}

	}
}
