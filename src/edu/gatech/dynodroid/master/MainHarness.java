/**
 * 
 */
package edu.gatech.dynodroid.master;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;

import edu.gatech.dynodroid.RMIHelper.RMIBackEndServer;
import edu.gatech.dynodroid.RMIHelper.RMIBackEndServerImpl;
import edu.gatech.dynodroid.RMIHelper.RMIHelper;
import edu.gatech.dynodroid.appHandler.AndroidAppHandler;
import edu.gatech.dynodroid.appHandler.ApkHandler;
import edu.gatech.dynodroid.appHandler.AppSrcHandler;
import edu.gatech.dynodroid.covHandler.CoverageHandler;
import edu.gatech.dynodroid.covHandler.DummyCovHandler;
import edu.gatech.dynodroid.covHandler.EmmaCoverageHandler;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.devHandler.ADeviceSetup;
import edu.gatech.dynodroid.devHandler.DeviceEmulator;
import edu.gatech.dynodroid.hierarchyHelper.LayoutExtractorFactory;
import edu.gatech.dynodroid.hierarchyHelper.ViewServerLayoutExtractor;
import edu.gatech.dynodroid.logMonitoring.LogMonitoring;
import edu.gatech.dynodroid.reporting.TestProfileReporting;
import edu.gatech.dynodroid.testHarness.RandomMonkeyTesting;
import edu.gatech.dynodroid.testHarness.TestStrategy;
import edu.gatech.dynodroid.testHarness.WidgetBasedTesting;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author machiry
 * 
 */
public class MainHarness {

	public static ArrayList<String> builtApps = new ArrayList<String>();
	public static ArrayList<String> buildingApps = new ArrayList<String>();
	public static ArrayList<String> results = new ArrayList<String>();
	public static ArrayList<String> currentBusyApps = new ArrayList<String>();
	private static int currPortNumber = 6000;

	private static void printUsage() {
		System.out
				.println("Please provide properties file(absoulte path) as second argument\n");
		System.out.println("Properties File Format:");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			printUsage();
			return;
		}

		if (args[0].equals("ser")) {
			try {
				// 1.Setup
				if (!PropertyParser.parsePropertiesFile(args[1])) {
					System.out
							.println("Problem occured while trying to do automation setup");
					return;
				}

				Logger.initalize(null);

				ADeviceSetup
						.initializeDeviceSetup(PropertyParser.sdkInstallPath
								+ "/platform-tools/adb");

				// 2. get all Resultant Test Profiles
				ArrayList<TestProfile> allTestProfiles = getAllTestProfiles();
				Logger.logInfo("Total Number of Test Profiles:"
						+ allTestProfiles.size());

				int totalRuns = 0;
				for (TestProfile t : allTestProfiles) {
					try {
						Thread th = new Thread(new TestProfileHandler(t));
						th.start();
						totalRuns++;
					} catch (Exception e) {
						Logger.logException(e);
					}
				}
				Logger.logInfo("Total Number of Threads scheduled:" + totalRuns);
				while (results.size() < totalRuns) {
					Thread.sleep(5000);
				}
				Logger.logInfo("-----START RUN SUMMARY-----");
				for (String s : results) {
					Logger.logInfo(s);
				}
				Logger.logInfo("-----END RUN SUMMARY-----");
				Logger.logInfo("All Threads Completed Execution..Exiting");
				if (PropertyParser.mailNotificationRecipents != null) {
					TestProfileReporting.sendCompletionMail(
							PropertyParser.baseWorkingDir,
							PropertyParser.mailNotificationRecipents);
				}
				System.exit(0);
			} catch (Exception e) {
				Logger.logException(e);
			} finally {
				Logger.endLogging();
			}
		}

		if (args[0].equals("rmi")) {
			if (!PropertyParser.parsePropertiesFile(args[1])) {
				System.out
						.println("Problem occured while trying to do automation setup");
				return;
			}

			Logger.initalize(null);

			ADeviceSetup.initializeDeviceSetup(PropertyParser.sdkInstallPath
					+ "/platform-tools/adb");
			try {
				RMIBackEndServer server = new RMIBackEndServerImpl(
						PropertyParser.rmiDBConnectionString);
				RMIHelper.bindRMIBackEndServer(server);
				Logger.logInfo("Started RMI BackEnd Service, Waiting for Connections");
			} catch (Exception e) {
				Logger.logException(e);
				Logger.logError("Problem Occured while setting up RMIBackEndService");
			}
		}
	}

	public static synchronized int getNextPortNumber() {
		if (currPortNumber > 60000) {
			currPortNumber = 6000;
		}
		currPortNumber += 2;
		return currPortNumber;
	}

	private static ArrayList<TestProfile> getAllTestProfiles() {
		ArrayList<TestProfile> resultTestProfiles = new ArrayList<TestProfile>();
		File appBaseDir = new File(PropertyParser.baseAppDir);
		File[] appDirs = FileUtilities.getAllDirectories(appBaseDir);

		FileFilter apkFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().endsWith(".apk");
			}
		};
		File[] apkFiles = appBaseDir.listFiles(apkFilter);

		ArrayList<File> apkFileList = new ArrayList<File>();

		ArrayList<File> totalApps = new ArrayList<File>();
		for (int j = 0; j < appDirs.length; j++) {
			totalApps.add(appDirs[j]);
		}

		if (apkFiles != null) {
			for (int i = 0; i < apkFiles.length; i++) {
				apkFileList.add(apkFiles[i]);
			}
			totalApps.addAll(apkFileList);
		}
		for (String s : PropertyParser.testStrategy) {
			if (s.equals(RandomMonkeyTesting.randomTestingStrategy)) {
				for (int i = 0; i < PropertyParser.eventCountArray.length; i++) {
					for (File app : totalApps) {
						TestProfile newTestProfile = new TestProfile();
						newTestProfile.appName = app.getName();
						newTestProfile.baseAppDir = app.getAbsolutePath();
						newTestProfile.eventCount = PropertyParser.eventCountArray[i];
						newTestProfile.sdkInstallPath = PropertyParser.sdkInstallPath;
						newTestProfile.emmaLibPath = PropertyParser.emmaLibPath;
						newTestProfile.testStrategy = RandomMonkeyTesting.randomTestingStrategy;
						newTestProfile.baseWorkingDir = PropertyParser.baseWorkingDir
								+ "/"
								+ newTestProfile.appName
								+ "_RMT_"
								+ newTestProfile.eventCount;
						FileUtilities
								.createDirectory(newTestProfile.baseWorkingDir);
						newTestProfile.touchPercentage = PropertyParser.touchPercentage;
						newTestProfile.smallNavigationPercentage = PropertyParser.smallNavigationPercentage;
						newTestProfile.majorNavigationPercentage = PropertyParser.majorNavigationPercentage;
						newTestProfile.trackballPercentage = PropertyParser.trackballPercentage;
						newTestProfile.responseDelay = PropertyParser.responseDelay;
                                                newTestProfile.appStartUpDelay = PropertyParser.appStartUpDelay;
						newTestProfile.delayBetweenEvents = PropertyParser.delayBetweenEvents;
						newTestProfile.verboseLevel = PropertyParser.verboseLevel;
						newTestProfile.coverageDumpTime = PropertyParser.coverageDumpTime;
						newTestProfile.instrumetationSetupDir = PropertyParser.instrumentationHelperDir;
						newTestProfile.coverageSamplingInterval = PropertyParser.coverageSamplingInterval;
						newTestProfile.isApk = apkFileList.contains(app);
						resultTestProfiles.add(newTestProfile);
					}
				}
			}
			if (s.equals(WidgetBasedTesting.widgetBasedTestingStrategy)) {
				for (File app : totalApps) {
					for (String childStrategy : PropertyParser.widgetSelectionStrategy) {
						for (int i = 0; i < PropertyParser.maxNoOfWidgets.length; i++) {
							TestProfile newTestProfile = new TestProfile();
							newTestProfile.appName = app.getName();
							newTestProfile.baseAppDir = app.getAbsolutePath();
							newTestProfile.sdkInstallPath = PropertyParser.sdkInstallPath;
							newTestProfile.emmaLibPath = PropertyParser.emmaLibPath;
							newTestProfile.testStrategy = WidgetBasedTesting.widgetBasedTestingStrategy;
							newTestProfile.maxNoOfWidgets = PropertyParser.maxNoOfWidgets[i];
							newTestProfile.baseWorkingDir = PropertyParser.baseWorkingDir
									+ "/"
									+ newTestProfile.appName
									+ "_WBT_"
									+ childStrategy
									+ "_"
									+ newTestProfile.maxNoOfWidgets;
							FileUtilities
									.createDirectory(newTestProfile.baseWorkingDir);
							newTestProfile.touchPercentage = PropertyParser.touchPercentage;
							newTestProfile.smallNavigationPercentage = PropertyParser.smallNavigationPercentage;
							newTestProfile.majorNavigationPercentage = PropertyParser.majorNavigationPercentage;
							newTestProfile.trackballPercentage = PropertyParser.trackballPercentage;
							newTestProfile.responseDelay = PropertyParser.responseDelay;
							newTestProfile.delayBetweenEvents = PropertyParser.delayBetweenEvents;
							newTestProfile.verboseLevel = PropertyParser.verboseLevel;
							newTestProfile.coverageDumpTime = PropertyParser.coverageDumpTime;
							newTestProfile.instrumetationSetupDir = PropertyParser.instrumentationHelperDir;
							newTestProfile.widgetSelectionStrategy = childStrategy;
							newTestProfile.coverageSamplingInterval = PropertyParser.coverageSamplingInterval;
							newTestProfile.isApk = apkFileList.contains(app);
							resultTestProfiles.add(newTestProfile);
						}
					}
				}
			}
		}

		return resultTestProfiles;
	}

}

class TestProfileHandler implements Runnable {
	TestProfile targetTestProfile = null;
	Logger profileLogger = null;
	ADevice targetDevice;
	TestStrategy targetTestStrategy = null;
	AndroidAppHandler targetAppHandler = null;
	CoverageHandler targetCoverageHandler = null;
	private String resultString = "";

	private static synchronized int getNextPortNumber() {
		return MainHarness.getNextPortNumber();
	}

	public TestProfileHandler(TestProfile profile) throws Exception {
		this.targetTestProfile = profile;
		this.profileLogger = new TextLogger(profile.baseWorkingDir
				+ "/CompleteTestProfile.log");

	}

	private HashMap<String, String> getPropertiesMap(String stra) {
		HashMap<String, String> retVal = new HashMap<String, String>();
		if (stra.equals(RandomMonkeyTesting.randomTestingStrategy)) {
			retVal.put(TestStrategy.appCoverageDumpTimeProperty,
					Integer.toString(targetTestProfile.coverageDumpTime));
			retVal.put(TestStrategy.appStartUpTimeProperty,
					Long.toString(targetTestProfile.appStartUpDelay));
			retVal.put(TestStrategy.workDirPropertyName,
					targetTestProfile.baseWorkingDir + "/TestStrategy");
			retVal.put(TestStrategy.appSrcPropertyName,
					targetTestProfile.baseAppDir);
			retVal.put(
					RandomMonkeyTesting.MajorNavigationPercentageProperty,
					Integer.toString(targetTestProfile.majorNavigationPercentage));
			retVal.put(
					RandomMonkeyTesting.SmallNavigationPercentageProperty,
					Integer.toString(targetTestProfile.smallNavigationPercentage));
			retVal.put(RandomMonkeyTesting.TrackBallPercentageProperty,
					Integer.toString(targetTestProfile.trackballPercentage));
			retVal.put(RandomMonkeyTesting.TouchPercentageProperty,
					Integer.toString(targetTestProfile.touchPercentage));
			retVal.put(RandomMonkeyTesting.NumberOfEventsProperty,
					Integer.toString(targetTestProfile.eventCount));
			retVal.put(RandomMonkeyTesting.VerboseLevelProperty,
					Integer.toString(targetTestProfile.verboseLevel));
			retVal.put(RandomMonkeyTesting.DelayBetweenEvents,
					Long.toString(targetTestProfile.delayBetweenEvents));
		}
		if (stra.equals(WidgetBasedTesting.widgetBasedTestingStrategy)) {
			retVal.put(TestStrategy.appCoverageDumpTimeProperty,
					Integer.toString(targetTestProfile.coverageDumpTime));
			retVal.put(TestStrategy.appStartUpTimeProperty,
					Long.toString(targetTestProfile.appStartUpDelay));
			retVal.put(TestStrategy.workDirPropertyName,
					targetTestProfile.baseWorkingDir + "/TestStrategy");
			retVal.put(TestStrategy.appSrcPropertyName,
					targetTestProfile.baseAppDir);
			retVal.put(WidgetBasedTesting.maxNoOfWidgetsProperty,
					Integer.toString(targetTestProfile.maxNoOfWidgets));
			retVal.put(WidgetBasedTesting.screenRenderingTimeProperty,
					Long.toString(targetTestProfile.responseDelay));
			retVal.put(WidgetBasedTesting.selectionStrategyProperty,
					targetTestProfile.widgetSelectionStrategy);
			retVal.put(ViewServerLayoutExtractor.targetHostPortNumberProperty,
					Integer.toString(getNextPortNumber()));
		}
		return retVal;
	}

	private TestStrategy getTestStrategy() {
		TestStrategy retVal = null;
		try {
			if (this.targetTestProfile.testStrategy
					.equals(RandomMonkeyTesting.randomTestingStrategy)) {
				retVal = new RandomMonkeyTesting(
						targetDevice,
						targetCoverageHandler,
						targetAppHandler,
						getPropertiesMap(RandomMonkeyTesting.randomTestingStrategy),
						this.targetTestProfile.coverageSamplingInterval);
			}
			if (this.targetTestProfile.testStrategy
					.equals(WidgetBasedTesting.widgetBasedTestingStrategy)) {
				retVal = new WidgetBasedTesting(
						targetDevice,
						targetCoverageHandler,
						targetAppHandler,
						getPropertiesMap(WidgetBasedTesting.widgetBasedTestingStrategy),
						this.targetTestProfile.coverageSamplingInterval);
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	public void run() {
		boolean isAppAlreadyBuilt = false;
		boolean isAppBuilding = true;
		boolean buildSucessfull = true;
		boolean appBusy = true;
		try {
			while (appBusy) {
				synchronized (MainHarness.currentBusyApps) {
					appBusy = MainHarness.currentBusyApps
							.contains(this.targetTestProfile.baseAppDir);
					if (!appBusy) {
						MainHarness.currentBusyApps
								.add(this.targetTestProfile.baseAppDir);
					}
				}
				Thread.sleep(2000);
			}

			while ((targetDevice = DeviceEmulator.getFreeEmulator()) == null) {
				this.profileLogger.logInfo(this.targetTestProfile.appName,
						this.targetTestProfile.appName
								+ " waiting for free device..");
				Thread.sleep(10000);
			}
			this.profileLogger.logInfo(this.targetTestProfile.appName,
					"Got Device:" + targetDevice.getDeviceName());
			if (!this.targetTestProfile.isApk) {
				this.targetAppHandler = new AppSrcHandler(
						this.targetTestProfile.baseAppDir, targetDevice,
						targetTestProfile.instrumetationSetupDir,
						targetTestProfile.baseWorkingDir + "/AppHandler");
			} else {
				this.targetAppHandler = new ApkHandler(
						this.targetTestProfile.baseAppDir, targetDevice,
						targetTestProfile.instrumetationSetupDir,
						targetTestProfile.baseWorkingDir + "/AppHandler");
			}
			synchronized (MainHarness.builtApps) {
				isAppAlreadyBuilt = MainHarness.builtApps
						.contains(this.targetTestProfile.appName);
			}
			if (!isAppAlreadyBuilt) {
				while (isAppBuilding) {
					synchronized (MainHarness.buildingApps) {
						isAppBuilding = MainHarness.buildingApps
								.contains(targetTestProfile.appName);
					}
					Thread.sleep(2000);
				}
				synchronized (MainHarness.builtApps) {
					isAppAlreadyBuilt = MainHarness.builtApps
							.contains(this.targetTestProfile.appName);
				}
			}
			if (!isAppAlreadyBuilt && !isAppBuilding) {
				synchronized (MainHarness.buildingApps) {
					MainHarness.buildingApps
							.add(this.targetTestProfile.appName);
				}
				buildSucessfull = targetAppHandler.instrumentApp()
						&& targetAppHandler
								.buildApp(AppSrcHandler.instrumentBuild);
				synchronized (MainHarness.builtApps) {
					MainHarness.builtApps.add(this.targetTestProfile.appName);
				}
				synchronized (MainHarness.buildingApps) {
					MainHarness.buildingApps.remove(targetTestProfile.appName);
				}

			}
			synchronized (MainHarness.currentBusyApps) {
				MainHarness.currentBusyApps
						.remove(this.targetTestProfile.baseAppDir);
			}
			if (!buildSucessfull) {
				this.profileLogger.logError("", "Build Ussucessfull..Exiting");
				return;
			}
			if (!targetTestProfile.isApk) {
				this.targetCoverageHandler = new EmmaCoverageHandler(
						targetTestProfile.baseAppDir + "/coverage.em",
						targetTestProfile.baseWorkingDir + "/coverageHandler",
						targetTestProfile.emmaLibPath);
			} else {
				this.targetCoverageHandler = new DummyCovHandler();
			}

			this.targetTestStrategy = getTestStrategy();
			if (targetTestStrategy == null) {
				this.profileLogger.logError("",
						"Unable to get Appropriate Strategy..Exiting");
				return;
			}

			this.profileLogger.logInfo("",
					"Got App Strategy..Trying to run the appropriate strategy");
			long beforeTime = System.currentTimeMillis();
			if (targetTestStrategy.prepare()) {
				this.profileLogger.logInfo("", "Preparation Sucessfull..");
				targetTestStrategy.perform();
				this.profileLogger.logInfo("", "Perform Done..");
				targetTestStrategy.cleanup();
				this.profileLogger.logInfo("", "Cleanup Done..");
				resultString = resultString + ",Ran Sucessfully";
			} else {
				resultString = resultString + ",Preparation UnSucessfull";
				this.profileLogger.logInfo("", "Preparation UnSucessfull..");
			}
			long afterTime = System.currentTimeMillis();
			
			this.profileLogger.logInfo("","Total Execution Time:"+(afterTime-beforeTime)/1000 + " seconds");

		} catch (Exception e) {
			resultString = resultString + ",Problem Occured..";
			this.profileLogger.logException("ProfileHandler", e);
		} finally {
			if (this.targetDevice != null) {

				// Clean up

				LogMonitoring.releaseLogs(this.targetDevice);

				LayoutExtractorFactory.deleteCache(targetDevice);

				// Here we destroy the device so that we run a fresh emulator
				// next time
				this.targetDevice.destroyDevice();

				// this.targetDevice.freeDevice();
			}
			if (this.profileLogger != null) {
				this.profileLogger.endLog();
			}
			synchronized (MainHarness.results) {
				MainHarness.results.add(this.targetTestProfile.toString()
						+ "::Result:" + resultString);
				String targetDirName = (new File(
						this.targetTestProfile.baseWorkingDir)).getName();
				try {
					FileUtilities.copyFolder(new File(
							this.targetTestProfile.baseWorkingDir), new File(
							PropertyParser.baseWorkingDir + "/CompletedRuns/"
									+ targetDirName));
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		}

		return;
	}

}
