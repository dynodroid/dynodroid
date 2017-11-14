package edu.gatech.dynodroid.appHandler;

import edu.gatech.dynodroid.devHandler.ADevice;

public abstract class AndroidAppHandler {
	
	/***
	 * This method sets the device on which the app handler needs to
	 * perform all its default activities
	 * @param dev
	 */
	public abstract void setDevice(ADevice dev);
	
	/***
	 * Install the application on the provided device
	 * @param targetDevice The device on which the app needs to be installed
	 * @param type This is the type of application package that needs to be installed
	 * for handlers that handle directly APK..type is irrelevant 
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean installApp(ADevice targetDevice,String type);
	
	/***
	 * Install the application on the default device
	 * @param type This is the type of application package that needs to be installed
	 * for handlers that handle directly APK..type is irrelevant 
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean installApp(String type);
	
	/***
	 * Instrument The application 
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean instrumentApp();
	
	/***
	 * Start the Application on default device
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean startApp();
	
	/***
	 * Start the Application on the provided device
	 * @param targetDevice The device on which the app needs to be started
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean startApp(ADevice targetDevice);
	
	/***
	 * Start the Application with instrumentation on, this might be calling different wrapper
	 * activity which calls the main app activity
	 * @param targetDevice he device on which the app needs to be started with instrumentation
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean startAppInstrument(ADevice targetDevice);
	
	/***
	 * Start the Application with instrumentation on, this might be calling different wrapper
	 * activity which calls the main app activity
	 * 
	 * @return true/false on success or failure respectively 
	 */
	public abstract boolean startAppInstrument();
	
	/***
	 * This method builds the application depending on provided type different types of APKs will might be built
	 * (This might not be applicable for all app handlers. Eg: if the handler directly uses application as apk) 
	 * @param type This is the type of build that needs to be done , might be debug,instrument or release
	 * for handlers that handle directly APK..type is irrelevant 
	 * @return true/false on success or failure respectively 
	 */
	public boolean buildApp(String type){
		return true;
	}
	
	/***
	 * This method gets the intermediate coverage file (coverage.em) for the App
	 * Here the App Handler can use different mechanism to get teh coverage file
	 * one simple way if to send SMS and have the handler dump the coverage file 
	 * @param targetPath the path under which the coverage file needs to be stored
	 * @param coverageDumpWaitTime time in milliseconds that needs to wait for coverage to complete
	 * @return true/false on success or failure respectively
	 */
	public boolean getIntermediateCoverage(String targetPath,long coverageDumpWaitTime){
		return true;
	}
	
	/***
	 * This method gets the final coverage file (coverage.em) for the app
	 * App handler can use different mechanism to collect the final coverage file
	 * Eg: by using activity manager (i.e am ) or just exiting the app and having finish listener dump the coverage file 
	 * @param targetPath the path under which the coverage file needs to be stored
	 * @param coverageDumpWaitTime time in milliseconds that needs to wait for coverage to complete
	 * @return true/false on success or failure respectively 
	 */
	public boolean getFinalCoverage(String targetPath,long coverageDumpWaitTime){
		return true;
	}
	
	/***
	 * This method is used to uninstall the provided app package from the device
	 * @return true on success / false on failure 
	 */
	public abstract boolean uninstallApp();
	
	/***
	 * This method is used to uninstall the provided app package from the specified device
	 * @param targetDevice the device from which the app needs to be un installed.
	 * @return true on success / false on failure 
	 */
	public abstract boolean uninstallApp(ADevice targetDevice);
	
	/***
	 * This method is used to exit from the application
	 * @return true on success / false on failure 
	 */
	public abstract boolean exitFromApp();
	
	/***
	 * This method is used to exit from the running app from the specified device
	 * @param targetDevice the device on which the operation needs to be performed.
	 * @return true on success / false on failure 
	 */
	public abstract boolean exitFromApp(ADevice targetDevice);
	
	/***
	 * This method gives the Java package to which the current package belongs to
	 * @return java fully qualified name of the package to which the app belongs to
	 */
	public abstract String getAppPackage();
	
	/***
	 * This method returns the information present in the app's AndroidManifest.xml in a human readable form
	 * @return string containing manifest info in human readable form
	 */
	public abstract String getManifestInfo();
	
	/***
	 * Thie method returns the ManifestParser object for the AndroidManifest.xml of the app
	 * @return AndroidManifestParser object
	 */
	public abstract AndroidManifestParser getAndroidManifestParser();
	
	/***
	 * This method returns the directory in which the app if extracted , if app is provided with sources,
	 * then its the app folder that will be returned
	 * @return absolute path of the folder in which app is extracted.
	 */
	public abstract String getAppExtractDir();
}
