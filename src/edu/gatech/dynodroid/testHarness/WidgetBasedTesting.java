/**
 * 
 */
package edu.gatech.dynodroid.testHarness;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.ddmlib.Client;

import edu.gatech.dynodroid.appHandler.AndroidAppHandler;
import edu.gatech.dynodroid.appHandler.ApkHandler;
import edu.gatech.dynodroid.appHandler.AppSrcHandler;
import edu.gatech.dynodroid.covHandler.CoverageHandler;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformerFactory;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.KeyPressAction;
import edu.gatech.dynodroid.hierarchyHelper.LayoutExtractor;
import edu.gatech.dynodroid.hierarchyHelper.LayoutExtractorFactory;
import edu.gatech.dynodroid.hierarchyHelper.MonkeyBasedActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.hierarchyHelper.ViewServerLayoutExtractor;
import edu.gatech.dynodroid.logMonitoring.LogMonitoring;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.testHarness.WidgetRandomBiasSelection.WidgetRandomBiasBasedSelectionStrategy;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.MonkeyTraceLogger;
import edu.gatech.dynodroid.utilities.Pair;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author machiry, rohan
 * 
 */
public class WidgetBasedTesting extends TestStrategy {
	private ADevice testDevice;
	private CoverageHandler coverageHandler;
	private AndroidAppHandler androidAppHandler;
	private String appSrcDir;
	private String workingDir;
	private int maxNoOfWidgets = 1000;
	private int maxAppStarts = 100;
	private long appStartWaitTime = 2000;
	private long coverageDumpWaitTime = 4000;
	private long screenRenderingTime = 3000;
	private int targetAppId = -1;
	private int coverageSamplingInterval = 100;
	private WidgetSelectionStrategy selectionStrategy = null;
	private String runStatsBaseDir = null;
	private int targetHostPortNumber = 0;
	private LayoutExtractor targetLayoutExtractor = null;
	private DeviceActionPerformer targetDeviceActionPerformer = null;
	private static final int logCleaningInterval = 25;
	private WidgetPictureManager screenshotCapturer = null;
	private Client targetClient = null;
	private String traceFilePath = null;
	private String odexFilePath = null;
	public static final String widgetBasedTestingStrategy = "WidgetBasedTesting";
	public static final String maxNoOfWidgetsProperty = "max_widgets";
	public static final String screenRenderingTimeProperty = "appres_time";
	public static final String selectionStrategyProperty = "sel_stra";
	private String logTag = "WBT_";

	HashMap<Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>, Integer> widgetBlackList;

	public WidgetBasedTesting(ADevice tDev, CoverageHandler handler,
			AndroidAppHandler appH, HashMap<String, String> properties,
			int cov_sam) throws Exception {
		this.coverageSamplingInterval = cov_sam;
		getProperties(properties);
		this.testDevice = tDev;
		this.coverageHandler = handler;
		this.androidAppHandler = appH;
		widgetBlackList = new HashMap<Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>, Integer>();
		this.targetDeviceActionPerformer = DeviceActionPerformerFactory
				.getDeviceActionPerformer(
						MonkeyBasedActionPerformer.monkeyActionPerformer,
						this.workingDir + "/ResultMonkeyScript.txt",
						this.workingDir, false, this.testDevice);
	}

	private boolean getProperties(HashMap<String, String> properties) {
		boolean retVal = true;
		retVal = retVal
				&& properties.containsKey(TestStrategy.workDirPropertyName);
		retVal = retVal
				&& properties.containsKey(TestStrategy.appSrcPropertyName);
		retVal = retVal && properties.containsKey(selectionStrategyProperty);
		if (retVal) {
			this.workingDir = properties.get(TestStrategy.workDirPropertyName);
			this.appSrcDir = properties.get(TestStrategy.appSrcPropertyName);
			this.runStatsBaseDir = this.workingDir + "/RunStats";
			retVal = FileUtilities.createDirectory(this.runStatsBaseDir);
			String selStraLog = this.workingDir + "/SelectionStrategyLog.log";
			FileUtilities.createDirectory(selStraLog);
			this.selectionStrategy = StrategyFactory
					.getWidgetSelectionStrategy(
							properties.get(selectionStrategyProperty),
							selStraLog, this.coverageSamplingInterval);
		}

		try {
			this.appStartWaitTime = Long.parseLong(properties
					.get(TestStrategy.appStartUpTimeProperty));
			this.coverageDumpWaitTime = Long.parseLong(properties
					.get(TestStrategy.appCoverageDumpTimeProperty));
			this.maxNoOfWidgets = Integer.parseInt(properties
					.get(WidgetBasedTesting.maxNoOfWidgetsProperty));
			this.screenRenderingTime = Long.parseLong(properties
					.get(WidgetBasedTesting.screenRenderingTimeProperty));
		} catch (Exception e) {
			Logger.logException("RMT:Problem occured while parsing the provided properties,"
					+ e.getMessage());
		}

		try {
			if (retVal) {
				this.targetHostPortNumber = Integer
						.parseInt(properties
								.get(ViewServerLayoutExtractor.targetHostPortNumberProperty));
				retVal = false;
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.testHarness.TestStrategy#prepare()
	 */
	@Override
	public boolean prepare() {
		try {
			this.textLogger = new TextLogger(workingDir + "/WBT.log");
			FileUtilities.createDirectory(workingDir + "/PreviousLogs");
			this.traceFilePath = workingDir + "/MethodTraceFiles";
			FileUtilities.createDirectory(this.traceFilePath);
			LogMonitoring.cleanMonitoring(testDevice, workingDir
					+ "/PreviousLogs");
			this.androidAppHandler.setDevice(testDevice);
			if (!LogMonitoring.initializeMonitoring(testDevice, workingDir
					+ "/MonitoringLogs", PropertyParser.kernelModulesLocation,
					this.selectionStrategy)) {
				Logger.logError("Problem occured while trying to initalize log monitoring");
			} else {
				Logger.logInfo("Sucessfully Initialized Log Monitoring");
				if (!LogMonitoring.startMonitoring(testDevice)) {
					Logger.logError("Problem occured while trying to start log monitoring");
				} else {
					Logger.logInfo("Sucessfully started Log Monitoring");
				}
			}

			//Set up phone contacts
			this.testDevice
					.executeShellCommand("am start -a android.intent.action.INSERT -e name \"Machiry AsLee\" -e phone 6789077112 -e email \"m3reporting@gmail.com\" -t vnd.android.cursor.dir/contact");
			Thread.sleep(4000);
			this.targetDeviceActionPerformer.performAction(new KeyPressAction(4), this.testDevice);
			this.targetDeviceActionPerformer.performAction(new KeyPressAction(4), this.testDevice);
			Thread.sleep(3000);
			this.testDevice
					.executeShellCommand("am start -a android.intent.action.INSERT -e name \"Rohan Tahil\" -e phone 6789077113 -e email \"dynodroidgatech@gmail.com\" -t vnd.android.cursor.dir/contact");
			Thread.sleep(4000);
			this.targetDeviceActionPerformer.performAction(new KeyPressAction(4), this.testDevice);
			this.targetDeviceActionPerformer.performAction(new KeyPressAction(4), this.testDevice);
			Thread.sleep(3000);

			LogMonitoring.addTag(testDevice, "Installing Application:", 0);
			ArrayList<String> odexBeforeInstall = new ArrayList<String>();
			odexBeforeInstall = this.testDevice
					.executeShellCommand("ls /data/dalvik-cache");
			this.androidAppHandler.uninstallApp();
			boolean retVal = this.testDevice.cleanSDCard()
					&& this.androidAppHandler
							.installApp(AppSrcHandler.instrumentInstall);
			if (retVal) {
				this.textLogger.logInfo(this.testDevice.getDeviceName(),
						"Device Prepare Complete");
			} else {
				this.textLogger.logInfo(this.testDevice.getDeviceName(),
						"Problem occured during Device Prepare");
			}
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
			this.targetLayoutExtractor = LayoutExtractorFactory
					.getLayoutExtractor(
							LayoutExtractorFactory.hierarchyLayoutExtractorType,
							this.testDevice, this.targetHostPortNumber);
			this.logTag = this.logTag + this.testDevice.getDeviceName();
			retVal = retVal && this.targetLayoutExtractor.setupDevice();
			try {
				this.screenshotCapturer = new WidgetPictureManager(workingDir
						+ "/MonitoringLogs/screenshots",
						this.targetDeviceActionPerformer);
			} catch (Exception e) {
				Logger.logException(e);
			}
			return retVal;
		} catch (Exception e) {
			Logger.logException("WBT:Exception occured during prepare Step"
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
		boolean retVal = false;
		this.textLogger.logInfo("AppPackage",
				this.androidAppHandler.getAppPackage());
		if (this.testDevice.getFileFromDevice("/data/system/packages.list",
				workingDir + "/packages.list")) {
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
			Logger.logError("Problem occured while trying to get the packages.list from device");
		}
		FileUtilities.appendLineToFile(workingDir + "/ManifestInfo.txt",
				this.androidAppHandler.getManifestInfo());

		try {
			int noOfAppStarts = 0;
			int i = 0;
			boolean ableToStartTheApp = true;
			ViewScreen startScreen = null;
			String currBaseDir = null;
			String coverageFile = null;
			int noOfCoverages = 1;
			String textBoxInputPath = (this.androidAppHandler instanceof ApkHandler) ? this.appSrcDir
					+ ".textBoxInput"
					: this.appSrcDir + "/textBoxInput";
			ViewScreen oldScreen = null;
			Pair<ViewElement, IDeviceAction> currentEleExecised = null;

			while (i < this.maxNoOfWidgets && ableToStartTheApp
					&& noOfAppStarts < this.maxAppStarts) {

				if (!this.selectionStrategy.reStartStrategy()) {
					this.textLogger
							.logError(logTag,
									"Problem occured while trying to re-start the app strategy");
				}
				noOfAppStarts++;

				// Start Method Profiling
				if (!enableAppMethodProfiling(this.androidAppHandler
						.getAppPackage())) {
					this.textLogger.logError(logTag,
							"Error Occured while enabling Method Profiling for app:"
									+ this.androidAppHandler.getAppPackage());
				}

				addBlacklistWidget(oldScreen, currentEleExecised);

				addReceiversToSelectionStrategy(
						this.androidAppHandler.getAndroidManifestParser(),
						this.selectionStrategy);
				LogMonitoring.addTag(testDevice, "Starting Application ("
						+ noOfAppStarts + "):", 0);
				if (this.androidAppHandler.startAppInstrument()) {
					Thread.sleep(this.appStartWaitTime);

					oldScreen = targetLayoutExtractor.getCurrentScreen(true,
							textBoxInputPath);
					startScreen = oldScreen;
					// First Initialize the strategy
					selectionStrategy.initializeNewScreen(oldScreen);
					ViewScreen newScreen = oldScreen;

					if (selectionStrategy instanceof WidgetRandomBiasBasedSelectionStrategy) {
						i += ((WidgetRandomBiasBasedSelectionStrategy) selectionStrategy)
								.populateTextFields(newScreen, this);
					}

					currBaseDir = this.runStatsBaseDir + "/Run_"
							+ noOfAppStarts;
					retVal = true;
					FileUtilities.createDirectory(currBaseDir);

					while (oldScreen != null && i < this.maxNoOfWidgets) {
						i++;
						/*if (selectionStrategy.needFreshDirectory()) {
							currBaseDir = this.runStatsBaseDir + "/Run_"
									+ noOfAppStarts + "_Coverage_" + i;
							FileUtilities.createDirectory(currBaseDir);
						}*/
						//coverageFile = currBaseDir + "/coverage.ec";
						newScreen = targetLayoutExtractor.getCurrentScreen(
								true, textBoxInputPath);
						if(i % 5 == 0){
							this.testDevice
							.executeShellCommand("am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE");
							Thread.sleep(2000);
						}
						if (!selectionStrategy.areScreensSame(oldScreen,
								newScreen)) {
							int noOfBackPress = normalizeApp(newScreen);
							if (noOfBackPress != 0) {
								// This means normalize happened and we need to
								// account
								// for that

								addBlacklistWidget(oldScreen,
										currentEleExecised);

								if (noOfBackPress < 0) {
									this.textLogger
											.logError(
													logTag,
													"Problem occured while normalizing the app from:"
															+ newScreen.features.screenName
															+ "..App Exited");
									break;
								} else {
									this.textLogger
											.logWarning(logTag,
													"Normalization Done on the current screen");
									newScreen = targetLayoutExtractor
											.getCurrentScreen(true,
													textBoxInputPath);
								}
							}
						}

						// if the current screen is not start screen then add
						// backwidget to the possible widgets
						if (!selectionStrategy.areScreensSame(startScreen,
								newScreen)
								&& !newScreen.childWidgets.contains(newScreen
										.getBackWidget().getFirst())) {
							this.textLogger.logInfo(logTag, "Added BackButton");
							newScreen.childWidgets.add(newScreen
									.getBackWidget().getFirst());
						}
						if (!selectionStrategy.areScreensSame(oldScreen,
								newScreen)) {
							this.textLogger.logInfo(
									logTag,
									"New Screen appeared..old screen:"
											+ oldScreen.toString()
											+ ",new screen:"
											+ newScreen.toString());
							newScreen = selectionStrategy.notifyNewScreen(
									oldScreen, newScreen);
							if (selectionStrategy instanceof WidgetRandomBiasBasedSelectionStrategy) {
								i += ((WidgetRandomBiasBasedSelectionStrategy) selectionStrategy)
										.populateTextFields(newScreen, this);
							}
							oldScreen = newScreen;
						}
						if (newScreen == null) {
							this.textLogger
									.logWarning(logTag,
											"Null Screen returned....Ending Testing cycle");
							break;
						}

						Pair<ViewElement, IDeviceAction> currElement = selectionStrategy
								.getNextElementAction(newScreen,
										currentEleExecised, true);
						if (currElement == null) {
							this.textLogger
									.logWarning(logTag,
											"No View Element to Execise..Ending Testing cycle");
							break;
						}

						// This is to add tag for the logging
						// so that any events that occur after this will be
						// notified
						LogMonitoring.addTag(testDevice,
								currElement.toString(), i);

						currentEleExecised = currElement;
						
						if (i % logCleaningInterval == 0) {
							// This is just to save on in-memory foot
							// print
							// of the Monitoring Objects.
							if (!LogMonitoring.cleanMonitoring(
									testDevice, workingDir)) {
								Logger.logError("Problem occured while trying to clean log monitoring");
							} else {
								Logger.logInfo("Sucessfully cleaned Log Monitoring");
							}
						}

						if (selectionStrategy.needDumpCoverage()) {
							String currCovDir = this.runStatsBaseDir +"/Coverage" + noOfCoverages;
							noOfCoverages++;
							FileUtilities.createDirectory(currCovDir);
							coverageFile = currCovDir +"/coverage.ec";
							
							this.textLogger
									.logInfo(
											logTag,
											i
													+ " Trying to get intermediate coverage");
							this.androidAppHandler
									.getIntermediateCoverage(
											coverageFile,
											this.coverageDumpWaitTime);
							
							this.coverageHandler
									.setReportDir(currCovDir);

							this.coverageHandler.computeCoverageReport(
									coverageFile,
									CoverageHandler.coverageTypeAll,
									this.appSrcDir + "/src");
						}
						if (!widgetBlackList
								.containsKey(new Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>(
										newScreen, currentEleExecised))) {
							this.targetDeviceActionPerformer
									.setWorkingDir(currBaseDir);
							if (performAction(currentEleExecised)) {
								this.textLogger.logInfo(logTag, i
										+ " Execised ViewElement:"
										+ currentEleExecised.toString());

								if (this.screenshotCapturer != null) {
									if (!this.screenshotCapturer.takePicture(
											currElement.getFirst(),
											this.testDevice, i)
											&& currElement.getFirst() != null) {
										this.textLogger.logError(
												"ScreenshotError",
												"Problem Occured while taking screen shot for widget:"
														+ currElement
																.getFirst()
																.toString());
									}
								}
							} else {
								this.textLogger
										.logError(
												logTag,
												"Problem occured while trying to execise View Element:"
														+ currentEleExecised
																.toString());
							}
						} else {
							increaseBlacklistCount(newScreen,
									currentEleExecised);
							this.textLogger
									.logError(
											logTag,
											"Ignoring Blacklisted element: "
													+ new Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>(
															newScreen,
															currentEleExecised));
						}
					}

				} else {
					ableToStartTheApp = false;
					this.textLogger
							.logError(logTag,
									"Problem occured while trying to start the app enabling instrumentation");
				}

			}
			
			LogMonitoring.cleanMonitoring(
					testDevice, workingDir);

			// End Method Profiling
			if (!disableAppMethodProfiling()) {
				this.textLogger.logInfo(logTag,
						"Problem occured while disabling method profiling on this device:"
								+ this.testDevice.getDeviceName());
			}

			currBaseDir = this.runStatsBaseDir + "/FinalCoverageStats";
			FileUtilities.createDirectory(currBaseDir);
			coverageFile = currBaseDir + "/coverage.ec";
			if (!this.androidAppHandler.exitFromApp()) {
				this.textLogger.logError(logTag,
						"Problem occured while trying to exit form the App");
			} else {
				this.textLogger.logError(logTag, "App Exited Sucessfully");
			}
			if (!this.androidAppHandler.getFinalCoverage(coverageFile,
					this.coverageDumpWaitTime)) {
				this.textLogger
						.logError(logTag,
								"Problem occured while trying to get the coverage file from the device");
			} else {
				this.textLogger.logError(logTag, "Got final coverage dump");
			}
			this.coverageHandler.setReportDir(currBaseDir);
			this.coverageHandler.computeCoverageReport(coverageFile,
					CoverageHandler.coverageTypeAll, this.appSrcDir + "/src");

			// Get the stats from trace log
			if (!computeStatsFromTraceLog()) {
				this.textLogger
						.logError(logTag,
								"Problem occured while trying to compute covered methods from trace logs");
			}

		} catch (Exception e) {
			this.textLogger.logException(logTag, e);
		}
		return retVal;
	}

	private boolean computeStatsFromTraceLog() {
		boolean retVal = false;
		try {
			if (this.androidAppHandler instanceof ApkHandler) {
				/*
				 * String coveredMethodFile = this.workingDir +
				 * "/CoveredMethods.txt";
				 * 
				 * // Get the .trace files from the trace directory FileFilter f
				 * = new FileFilter() {
				 * 
				 * @Override public boolean accept(File arg0) { if (arg0 != null
				 * && arg0.isFile() && arg0.getName().endsWith(".trace")) {
				 * return true; } return false; } };
				 * 
				 * File[] traceFiles = (new
				 * File(this.traceFilePath)).listFiles(f); for (File fil :
				 * traceFiles) { ArrayList<String> meths = TracefileParser
				 * .getCoveredMethods(fil.getAbsolutePath());
				 * FileUtilities.appendLinesToFile(coveredMethodFile, meths); }
				 */

				retVal = true;

			} else {
				retVal = true;
			}

		} catch (Exception e) {
			this.textLogger.logException(logTag, e);
		}
		return retVal;
	}

	private boolean enableAppMethodProfiling(String appPackageName) {
		boolean retVal = false;
		try {
			if (this.odexFilePath != null
					&& (new File(this.odexFilePath)).exists()) {

				String checkSumInfo = ExecHelper.RunProgram(
						PropertyParser.toolLoc + "/getHeaderInfo.sh "
								+ PropertyParser.toolLoc
								+ "/dexdump "
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

				// IDevice dev = ADevice.getIDevice(testDevice.getDeviceName());

				/*
				 * DexdumpParser ddparser = new DexdumpParser(
				 * PropertyParser.sdkInstallPath + "/platform-tools/dexdump",
				 * (TextLogger) this.textLogger); ArrayList<String>
				 * packagesInApk = ddparser.getPackageInfo(
				 * this.androidAppHandler.getAppExtractDir() + "/classes.dex",
				 * this.androidAppHandler.getAppPackage());
				 * 
				 * //White Listing of the Packages that needs to be monitored
				 * FileUtilities
				 * .appendLinesToFile(this.workingDir+"/apkPackages.txt",
				 * packagesInApk);
				 * testDevice.putFileInToDevice(this.workingDir+"/apkPackages.txt"
				 * , "/sdcard/apkPackages.txt");
				 */

				// targetClient = dev.getClient(appPackageName);
				/*
				 * MethodProfileHandler.addTracingSupport(targetClient,
				 * this.traceFilePath);
				 */
				retVal = true;
			} else {
				retVal = true;
			}
		} catch (Exception e) {
			this.textLogger.logException(logTag, e);
		}
		return retVal;
	}

	private boolean disableAppMethodProfiling() {
		boolean retVal = false;
		try {
			if (this.androidAppHandler instanceof ApkHandler) {
				if (targetClient != null) {
					/*
					 * this.textLogger.logInfo(logTag,
					 * "Disabling Method profiling for client:" + targetClient);
					 * MethodProfileHandler.stopTacing(targetClient); int reTry
					 * = 10; while (reTry > 0) { if
					 * (MethodProfileHandler.isTraceCompleted(targetClient)) {
					 * break; } reTry--; Thread.sleep(1000); }
					 */
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

	private boolean isCurrentScreenInApp(ViewScreen currWindow) {
		if (currWindow != null) {
			String currWindowName = currWindow.features.screenName;
			if (currWindowName.contains("/")) {
				String pkgName = currWindowName.split("/")[0];
				if (pkgName.contains(".")) {
					return pkgName.contains(this.androidAppHandler
							.getAppPackage());
				}
			}
			return true;
		}
		return false;
	}

	public boolean performAction(
			Pair<ViewElement, IDeviceAction> currentEleExecised)
			throws InterruptedException {

		if (this.targetDeviceActionPerformer.performAction(
				currentEleExecised.getSecond(), this.testDevice)) {
			if (this.targetDeviceActionPerformer.getTraceLogger() != null) {
				this.targetDeviceActionPerformer.getTraceLogger().addTraceData(
						MonkeyTraceLogger.sleepCategory,
						Long.toString(this.screenRenderingTime));
			}
			return true;
		} else {
			return false;
		}

	}

	private void addBlacklistWidget(ViewScreen screen,
			Pair<ViewElement, IDeviceAction> widget) {
		if (screen != null && widget != null && (this.selectionStrategy instanceof WidgetRandomBiasBasedSelectionStrategy)) {
			Pair<ViewScreen, Pair<ViewElement, IDeviceAction>> blacklisted = new Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>(
					screen, widget);
			if (!widgetBlackList.containsKey(blacklisted)) {
				widgetBlackList.put(blacklisted, 0);
				this.textLogger.logInfo(logTag, "Added blacklist element: "
						+ blacklisted);
			}

		}
	}

	private void increaseBlacklistCount(ViewScreen screen,
			Pair<ViewElement, IDeviceAction> widget) {
		Pair<ViewScreen, Pair<ViewElement, IDeviceAction>> pair = new Pair<ViewScreen, Pair<ViewElement, IDeviceAction>>(
				screen, widget);
		Integer count = widgetBlackList.get(pair);
		if (count != null) {
			widgetBlackList.put(pair, count + 1);
		}
	}

	/***
	 * 
	 * @param currentWindow
	 * @param workingDir
	 * @return
	 */
	private int normalizeApp(ViewScreen currentWindow) {
		int noOfBackButtonPress = 0;
		int maxNoOfExceptions = 3;
		int maxNoOfBackButtonPress = 15;
		try {
			if (currentWindow == null || currentWindow.features == null
					|| currentWindow.features.screenName == null) {
				// Work around to make sure that we dont get any null reference
				// exception
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					Logger.logException(e);
				}
				currentWindow = targetLayoutExtractor.getCurrentScreen(true);
			}
			String currWindowName = currentWindow.features.screenName;
			if (!isCurrentScreenInApp(currentWindow)) {
				while (maxNoOfExceptions > 0
						&& !isCurrentScreenInApp(currentWindow)
						&& noOfBackButtonPress <= maxNoOfBackButtonPress) {
					if (currWindowName.startsWith("com.android.launcher")) {
						return -1;
					}
					try {
						this.textLogger
								.logWarning(
										logTag,
										"Current Window:"
												+ currWindowName
												+ " doesn't belong to the Application..back button will be pressed till we navigate back");
						this.targetDeviceActionPerformer.performAction(
								new KeyPressAction(4), testDevice);
						// This is to invalidate the current widget
						// as this lead to new Application

						currentWindow = targetLayoutExtractor
								.getCurrentScreen(false);
						currWindowName = currentWindow.features.screenName;
						noOfBackButtonPress++;
					} catch (Exception e) {
						this.textLogger.logException(logTag, e);
						maxNoOfExceptions--;
					}
				}
				if (maxNoOfExceptions <= 0
						&& !isCurrentScreenInApp(currentWindow)) {
					this.textLogger.logError(logTag,
							"Problem occured while trying to navigate back to the source application:"
									+ this.androidAppHandler.getAppPackage());
					noOfBackButtonPress = -1;
				}
				if (noOfBackButtonPress > maxNoOfBackButtonPress) {
					noOfBackButtonPress = -1;
				}

			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return noOfBackButtonPress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.testHarness.TestStrategy#cleanup()
	 */
	@Override
	public boolean cleanup() {

		this.textLogger.logInfo(logTag, "Blacklisted element count:");
		for (Pair<ViewScreen, Pair<ViewElement, IDeviceAction>> key : widgetBlackList
				.keySet()) {
			this.textLogger.logInfo(logTag,
					key + ": " + widgetBlackList.get(key));
		}
		this.textLogger.logInfo(logTag, "End Blacklist");

		this.textLogger.endLog();
		this.targetDeviceActionPerformer.endTracing();
		this.selectionStrategy.cleanUp();
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
