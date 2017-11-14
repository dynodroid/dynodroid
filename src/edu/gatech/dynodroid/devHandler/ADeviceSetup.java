/**
 * 
 */
package edu.gatech.dynodroid.devHandler;

import java.io.File;

/**
 * @author machiry
 * 
 */
public class ADeviceSetup {

	private static boolean isInitialized = false;
	public static String adbPath = null;
	public static long timeForAdbInitialization = 1500;

	private static Object sSync = new Object();

	/***
	 * This is initialization method that initializes the setup required for ADB
	 * connection
	 * 
	 * @param adbLocalPath absolute path on disc where adb binary is present
	 * @return true on success
	 */
	public static boolean initializeDeviceSetup(String adbLocalPath,
			long timeForAdb) {
		
		synchronized (sSync) {
			if (!isInitialized) {
				if ((new File(adbLocalPath)).exists() && timeForAdb >= 1500) {
					adbPath = adbLocalPath;
					timeForAdbInitialization = timeForAdb;
					isInitialized = true;
					return true;
				}
				return false;
			}
			return false;
		}
		
	}
	
	/***
	 * This method returns the initialization status for the setup 
	 * @return true/false depending on initialization being sucessful or not
	 */
	public static boolean isInitialized(){
		synchronized (sSync) {
			return isInitialized;
		}
	}

	/***
	 * This is overloaded initialization method that use the default timeout
	 * for adb initialization 
	 * @param adbLocalPath absolute path on disc where adb binary is present
	 * @return true on success
	 */
	public static boolean initializeDeviceSetup(String adbLocalPath) {
		return initializeDeviceSetup(adbLocalPath, timeForAdbInitialization);
	}

}
