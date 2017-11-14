/**
 * 
 */
package edu.gatech.dynodroid.testHarness;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.gatech.dynodroid.appHandler.AndroidAppHandler;
import edu.gatech.dynodroid.appHandler.AppSrcHandler;
import edu.gatech.dynodroid.covHandler.CoverageHandler;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.logMonitoring.LogMonitoring;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.CustomComparer;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author machiry
 * 
 */
public class RandomMonkeyTesting extends TestStrategy {

	private int touchPercentage = 50;
	private int smallNavigationPercentage = 20;
	private int majorNavigationPercentage = 0;
	private int trackballPercentage = 30;
	private long delayBetweenEvents = 200;
	private long numberOfEvents = 1000;
	private int verboseL = 3;
	private long appStartWaitTime = 2000;
	private int targetAppId = -1;
	private long coverageDumpWaitTime = 4000;
	private String coverageStoreDir = "";
	private String odexFilePath = null;
	private Logger textLogger;
	private ADevice testDevice;
	private CoverageHandler coverageHandler;
	private AndroidAppHandler androidAppHandler;
	private String appSrcDir;
	private String workingDir;
	private String logTag = "RMT_";
	public static final String randomTestingStrategy = "RandomMonkeyTesting";
	public static final String TouchPercentageProperty = "tch_pct";
	public static final String SmallNavigationPercentageProperty = "sml_pct";
	public static final String MajorNavigationPercentageProperty = "mjr_pct";
	public static final String TrackBallPercentageProperty = "trk_pct";
	public static final String DelayBetweenEvents = "delay";
	public static final String VerboseLevelProperty = "verbose_level";
	public static final String NumberOfEventsProperty = "max_events";
	public int coverageSamplingIntervel = 0;

	public RandomMonkeyTesting(ADevice tDev, CoverageHandler handler,
			AndroidAppHandler appH, HashMap<String, String> properties) {
		this.testDevice = tDev;
		this.coverageHandler = handler;
		this.androidAppHandler = appH;
		getProperties(properties);
	}
	
	public RandomMonkeyTesting(ADevice tDev, CoverageHandler handler,
			AndroidAppHandler appH, HashMap<String, String> properties,int cov_sample) {
		this(tDev,handler,appH,properties);
		this.coverageSamplingIntervel = cov_sample;
	}

	private boolean getProperties(HashMap<String, String> properties) {
		boolean retVal = true;
		retVal = retVal
				&& properties.containsKey(TestStrategy.workDirPropertyName);
		retVal = retVal
				&& properties.containsKey(TestStrategy.appSrcPropertyName);
		if (retVal) {
			this.workingDir = properties.get(TestStrategy.workDirPropertyName);
			FileUtilities.createDirectory(this.workingDir);
			this.appSrcDir = properties.get(TestStrategy.appSrcPropertyName);
			this.coverageStoreDir = this.workingDir + "/RunStats";
			this.coverageHandler.setReportDir(this.coverageStoreDir);
		}

		try {
			this.touchPercentage = Integer.parseInt(properties
					.get(TouchPercentageProperty));
			this.smallNavigationPercentage = Integer.parseInt(properties
					.get(SmallNavigationPercentageProperty));
			this.majorNavigationPercentage = Integer.parseInt(properties
					.get(MajorNavigationPercentageProperty));
			this.trackballPercentage = Integer.parseInt(properties
					.get(TrackBallPercentageProperty));
			this.delayBetweenEvents = Long.parseLong(properties
					.get(DelayBetweenEvents));
			this.numberOfEvents = Long.parseLong(properties
					.get(NumberOfEventsProperty));
			this.verboseL = Integer.parseInt(properties
					.get(VerboseLevelProperty));
			this.appStartWaitTime = Long.parseLong(properties
					.get(TestStrategy.appStartUpTimeProperty));
			this.coverageDumpWaitTime = Long.parseLong(properties
					.get(TestStrategy.appCoverageDumpTimeProperty));
		} catch (Exception e) {
			Logger.logException("RMT:Problem occured while parsing the provided properties,"
					+ e.getMessage());
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.testHarness.TestStrategy#prepare()
	 */

	// We expect the app to be built before this method is called
	// As this method directly installs the app on the device
	@Override
	public boolean prepare() {
		try {
			this.textLogger = new TextLogger(workingDir + "/RMT.log");
			this.androidAppHandler.setDevice(testDevice);

			FileUtilities.createDirectory(workingDir + "/PreviousLogs");
			LogMonitoring.cleanMonitoring(testDevice, workingDir
					+ "/PreviousLogs");
			if (!LogMonitoring.initializeMonitoring(testDevice, workingDir+"/MonitoringLogs",
					PropertyParser.kernelModulesLocation, null)) {
				Logger.logError("Problem occured while trying to initalize log monitoring");
			} else {
				Logger.logInfo("Sucessfully Initialized Log Monitoring");
				if (!LogMonitoring.startMonitoring(testDevice)) {
					Logger.logError("Problem occured while trying to start log monitoring");
				} else {
					Logger.logInfo("Sucessfully started Log Monitoring");
				}
			}

			ArrayList<String> odexBeforeInstall = new ArrayList<String>();
			odexBeforeInstall = this.testDevice
					.executeShellCommand("ls /data/dalvik-cache");

			this.androidAppHandler.uninstallApp();
			boolean retVal = this.testDevice.cleanSDCard()
					&& this.androidAppHandler
							.installApp(AppSrcHandler.instrumentInstall);
			ArrayList<String> odexAfterInstall = this.testDevice
					.executeShellCommand("ls /data/dalvik-cache");

			odexAfterInstall.removeAll(odexBeforeInstall);

			if (odexAfterInstall.size() > 0) {
				for (String target : odexAfterInstall) {
					if (target.contains(this.androidAppHandler
							.getAndroidManifestParser().getAppPackage())) {
						odexFilePath = this.workingDir + "/ODEX_Apk.dex";
						Logger.logInfo("Copied ODEX from:"
								+ "/data/dalvik-cache/" + target + " to "
								+ odexFilePath);
						this.testDevice.getFileFromDevice("/data/dalvik-cache/"
								+ target, odexFilePath);
						break;
					}
				}
			}

			if (retVal) {
				// This is a check to know the app installation status on the
				// device
				// To ensure the state of the device and also to get the app id
				if (this.testDevice.getFileFromDevice(
						"/data/system/packages.list", workingDir
								+ "/packages.list")) {
					Logger.logInfo("Got Packages.list from the device");
					ArrayList<String> packagesInstalled = FileUtilities
							.readFileLineByLine(workingDir + "/packages.list");
					for (String s : packagesInstalled) {
						if (s.startsWith(this.androidAppHandler.getAppPackage())) {
							targetAppId = Integer.parseInt(s.split(" ")[1]);
							break;
						}
					}
					LogMonitoring.addAppId(testDevice, targetAppId);
					this.textLogger.logInfo("TargetAppID",
							Integer.toString(targetAppId));
				} else {
					Logger.logError("Problem occured while trying to get the packages.lst from device");
				}
				this.textLogger.logInfo(this.testDevice.getDeviceName(),
						"Device Prepare Complete");
			} else {
				this.textLogger.logInfo(this.testDevice.getDeviceName(),
						"Problem occured during Device Prepare");
			}
			return retVal;
		} catch (Exception e) {
			Logger.logException("RMP:Exception occured during prepare Step"
					+ e.getMessage());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.testHarness.TestStrategy#perform()
	 */
	@Override
	public boolean perform() {
		boolean isSucess = false;
		try {
			FileUtilities.appendLineToFile(workingDir + "/ManifestInfo.txt",
					this.androidAppHandler.getManifestInfo());
			if (!enableAppMethodProfiling(this.androidAppHandler
					.getAppPackage())) {
				this.textLogger.logError(logTag,
						"Error Occured while enabling Method Profiling for app:"
								+ this.androidAppHandler.getAppPackage());
			}
			if (this.androidAppHandler.startAppInstrument()) {
				Thread.sleep(this.appStartWaitTime);
				textLogger.logInfo(this.testDevice.getDeviceName(),
						"App Started Sucessfully");
				// Compute the shell command line based on percentage numbers
				// and
				// event count
				String options = " --throttle "
						+ Long.toString(this.delayBetweenEvents);
				options += " --pct-touch "
						+ Integer.toString(this.touchPercentage);
				options += " --pct-nav "
						+ Integer.toString(this.smallNavigationPercentage);
				options += " --pct-majornav "
						+ Integer.toString(this.majorNavigationPercentage);
				options += " --pct-trackball "
						+ Integer.toString(this.trackballPercentage);

				for (int i = 0; i < verboseL; i++) {
					options += " -v";
				}

				options += " -p "
						+ this.androidAppHandler.getAndroidManifestParser()
								.getAppPackage();
				
				if(this.androidAppHandler instanceof AppSrcHandler){
					options += " -m "
							+ this.androidAppHandler.getAndroidManifestParser()
									.getAppPackage()
							+ "/"
							+ this.androidAppHandler.getAndroidManifestParser()
									.getInstrumentation();
					options += " --getcover " 
							+this.coverageSamplingIntervel;
				
				}
						
				options += " " + Long.toString(this.numberOfEvents);
				// invoke the monkey based on the above percentages

				textLogger.logInfo(this.testDevice.getDeviceName(),
						"Trying to Run Monkey in Random mode with:"
								+ this.numberOfEvents + " events");
				ArrayList<String> outPut = this.testDevice.executeShellCommand(
						"monkey" + options, 66000000);

				textLogger.logInfo(this.testDevice.getDeviceName(),
						"----Monkey Run Ended----");
				// Get the log and write to the log
				for (String s : outPut) {
					textLogger.logInfo(this.testDevice.getDeviceName()
							+ ":monkeyLog:", s);
				}
				LogMonitoring.cleanMonitoring(
						testDevice, workingDir);
				ArrayList<String> coverageECFiles = this.testDevice
						.executeShellCommand("ls /mnt/sdcard/coverage*.ec");
				if (coverageECFiles != null) {
					textLogger.logInfo(this.testDevice.getDeviceName(), "No of coverage file on device:"+coverageECFiles.size());
					Collections.sort(coverageECFiles,new CustomComparer());
					int i = 1;
					for (String coverageFile : coverageECFiles) {
						if(coverageFile != null && coverageFile.trim().length() > 0){
							String currentCoverageDir = this.coverageStoreDir
									+ "/Coverage" + i;
							FileUtilities.createDirectory(currentCoverageDir);
							String targetFile = currentCoverageDir + "/coverage.ec";
							textLogger.logInfo(this.testDevice.getDeviceName(), "copying file:"+coverageFile+" to "+targetFile);
							this.testDevice.getFileFromDevice(coverageFile,
									targetFile);
							this.coverageHandler.setReportDir(currentCoverageDir);
							isSucess = (this.coverageHandler.computeCoverageReport(
									targetFile, CoverageHandler.coverageTypeAll,
									this.appSrcDir + "/src") != null);
							if (isSucess) {
								textLogger.logInfo(this.testDevice.getDeviceName(),
										"Sucessfully Computed Coverage Report Number:"
												+ i + " On device file:"
												+ coverageFile
												+ " on machine file:" + targetFile);
							} else {
								textLogger
										.logError(
												this.testDevice.getDeviceName(),
												"Problem occured while trying to get coverage numbers for Coverage Report Number:"
														+ i
														+ " On device file:"
														+ coverageFile
														+ " on machine file:"
														+ targetFile);
							}
							i++;
						}
					}
				}
				else{
					textLogger.logError(this.testDevice.getDeviceName(), "No intermediate coverag report files found on the emulator");
				}

				textLogger.logInfo(this.testDevice.getDeviceName(),
						"Trying to get final coverage file from device");
				this.coverageStoreDir = this.coverageStoreDir
						+ "/FinalCoverageStats";
				FileUtilities.createDirectory(this.coverageStoreDir);
				String coverageECFile = this.coverageStoreDir + "/coverage.ec";

				if (this.androidAppHandler.getFinalCoverage(coverageECFile,
						this.coverageDumpWaitTime)) {
					textLogger.logInfo(this.testDevice.getDeviceName(),
							"Got Coverage file from device");
					this.coverageHandler.setReportDir(this.coverageStoreDir);
					isSucess = (this.coverageHandler.computeCoverageReport(
							coverageECFile, CoverageHandler.coverageTypeAll,
							this.appSrcDir + "/src") != null);
				}
				if (isSucess) {
					textLogger.logInfo(this.testDevice.getDeviceName(),
							"Sucessfully Computed Coverage Report");
				} else {
					textLogger.logInfo(this.testDevice.getDeviceName(),
							"Unable to compute Coverage Report");
				}
			}
		} catch (Exception e) {
			this.textLogger.logException(this.testDevice.getDeviceName(), e);
		}

		return isSucess;
	}

	private boolean enableAppMethodProfiling(String appPackageName) {
		boolean retVal = false;
		try {
			if (this.odexFilePath != null
					&& (new File(this.odexFilePath)).exists()) {

				String checkSumInfo = ExecHelper.RunProgram(
						PropertyParser.toolLoc + "/getHeaderInfo.sh "
								+ PropertyParser.sdkInstallPath
								+ "/platform-tools/dexdump "
								+ this.odexFilePath, true);

				Logger.logInfo("Got Check Sum:" + checkSumInfo);
				if (checkSumInfo != null) {
					String checkSum = checkSumInfo.split(":")[1];
					checkSum = checkSum.trim();
					FileUtilities.appendLineToFile(this.workingDir
							+ "/apkPackages.txt", checkSum);
					testDevice
							.executeShellCommand("rm /sdcard/apkPackages.txt");
					testDevice.putFileInToDevice(this.workingDir
							+ "/apkPackages.txt", "/sdcard/apkPackages.txt");
				}
				retVal = true;
			} else {
				retVal = true;
			}
		} catch (Exception e) {
			this.textLogger.logException(logTag, e);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.testHarness.TestStrategy#cleanup()
	 */
	@Override
	public boolean cleanup() {
		this.textLogger.endLog();
		LogMonitoring.stopMonitoring(testDevice, true);
		if (!LogMonitoring.cleanMonitoring(testDevice, workingDir)) {
			Logger.logError("Problem occured while trying to clean log monitoring");
		} else {
			Logger.logInfo("Sucessfully cleaned Log Monitoring");
		}
		return this.testDevice.cleanSDCard()
				&& this.androidAppHandler.uninstallApp();
	}

}
