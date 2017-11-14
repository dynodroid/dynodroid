package edu.gatech.dynodroid.rmiRequest;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import edu.gatech.dynodroid.DBAccess.IDBFacade;
import edu.gatech.dynodroid.appHandler.AndroidAppHandler;
import edu.gatech.dynodroid.appHandler.ApkHandler;
import edu.gatech.dynodroid.appHandler.AppInstrumenter;
import edu.gatech.dynodroid.appHandler.AppSrcHandler;
import edu.gatech.dynodroid.covHandler.CoverageHandler;
import edu.gatech.dynodroid.covHandler.DummyCovHandler;
import edu.gatech.dynodroid.covHandler.EmmaCoverageHandler;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.devHandler.DeviceEmulator;
import edu.gatech.dynodroid.hierarchyHelper.LayoutExtractorFactory;
import edu.gatech.dynodroid.hierarchyHelper.ViewServerLayoutExtractor;
import edu.gatech.dynodroid.logMonitoring.LogMonitoring;
import edu.gatech.dynodroid.master.MainHarness;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.master.TestProfile;
import edu.gatech.dynodroid.reporting.TestProfileReporting;
import edu.gatech.dynodroid.testHarness.RandomMonkeyTesting;
import edu.gatech.dynodroid.testHarness.TestStrategy;
import edu.gatech.dynodroid.testHarness.WidgetBasedTesting;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

public class ServerRequestHandler implements Runnable {

	private TestProfile targetTestProfile = null;
	private IDBFacade dbFacade = null;
	private TextLogger targetLogger = null;
	private static final String TAG = "RequestHandler";
	private ServerRequestType targetType = ServerRequestType.APK;

	public ServerRequestHandler(TestProfile tarProf, IDBFacade tarDBF,
			TextLogger logger, ServerRequestType type) throws Exception {
		if (tarProf != null && tarDBF != null && logger != null) {
			this.targetTestProfile = tarProf;
			this.dbFacade = tarDBF;
			this.targetLogger = logger;
			this.targetType = type;
		} else {
			throw new Exception(
					"Unable to create ServerRequestHandler, one or more parameters are invalid");
		}

	}

	@Override
	public void run() {
		IServerRequest req = null;
		ADevice targetDevice = null;
		if (isInputValid()) {
			try {
				targetDevice = DeviceEmulator.getFreeEmulator();
				if (targetDevice != null) {
					dbFacade.updateRequestStatus(targetTestProfile.requestUUID,
							ServerRequestStatus.EMULATOR_CREATED.name());

					this.targetLogger.logInfo("RequestHandler",
							"Emulator Created!!");
					AndroidAppHandler appHandler = null;
					CoverageHandler covHandler = null;
					if (this.targetType == ServerRequestType.APK) {
						appHandler = new ApkHandler(
								targetTestProfile.baseAppDir, targetDevice,
								targetTestProfile.instrumetationSetupDir,
								targetTestProfile.baseWorkingDir
										+ "/AppHandler");
						covHandler = new DummyCovHandler();
					} else {

						// Here baseAppDir is the zip file containing the entire
						// app
						// 1.Unzip the directory
						// 2.run android update project
						// 3.build the app
						// 4.Use AppSrc Handler
						appHandler = new AppSrcHandler(
								targetTestProfile.baseAppDir, targetDevice,
								targetTestProfile.instrumetationSetupDir,
								targetTestProfile.baseWorkingDir
										+ "/AppHandler");
						
						covHandler = new EmmaCoverageHandler(
								targetTestProfile.baseAppDir + "/coverage.em",
								targetTestProfile.baseWorkingDir + "/coverageHandler",
								targetTestProfile.emmaLibPath);
					}


					WidgetBasedTesting testStr = new WidgetBasedTesting(
							targetDevice,
							covHandler,
							appHandler,
							getPropertiesMap(
									targetTestProfile,
									WidgetBasedTesting.widgetBasedTestingStrategy),
							targetTestProfile.coverageSamplingInterval);

					req = new ServerRequest(targetTestProfile, targetDevice,
							testStr, this.targetLogger, this.dbFacade,
							this.targetType);

					req.updateStatus(ServerRequestStatus.TEST_SCHEDULED);

					this.targetLogger.logInfo(TAG, "Trying to Schedule Tests");

					if (req.processRequest()) {
						this.targetLogger.logInfo(TAG,
								"Successfully Completed Testing");
						req.updateStatus(ServerRequestStatus.COMPLETED);
					} else {
						this.targetLogger
								.logError(TAG,
										"Problem Occured while trying to run the tests");
						req.updateStatus(ServerRequestStatus.PROBLEM_OCCURED);
					}

				} else {

					this.targetLogger.logError("RequestHandler",
							"Unable to Create Emulator");
					dbFacade.updateRequestStatus(targetTestProfile.requestUUID,
							ServerRequestStatus.EMULATOR_BUSY.name());
				}
			} catch (Exception e) {
				if (req != null) {
					req.updateStatus(ServerRequestStatus.PROBLEM_OCCURED);
				} else {
					dbFacade.updateRequestStatus(targetTestProfile.requestUUID,
							ServerRequestStatus.PROBLEM_OCCURED.name());
				}
				this.targetLogger.logException(
						targetTestProfile.requestUUID.toString(), e);
			} finally {
				if (targetDevice != null) {

					// Clean up

					LogMonitoring.releaseLogs(targetDevice);

					LayoutExtractorFactory.deleteCache(targetDevice);

					// Here we destroy the device so that we run a fresh
					// emulator
					// next time
					targetDevice.destroyDevice();

					// this.targetDevice.freeDevice();
				}

				this.targetLogger.endLog();

				String monitoringLogsBaseFolder = targetTestProfile.baseWorkingDir
						+ "/TestStrategy/MonitoringLogs/";

				// TODO: post processing the logs to create a xml file
				postProcessing(monitoringLogsBaseFolder);

				// Copy the logs to the target result server
				if (!FileUtilities.scpTo(targetTestProfile.baseLogDir,
						PropertyParser.resultServerName,
						PropertyParser.remoteServerResultPath,
						PropertyParser.scpAccessUserName)) {
					Logger.logError("Problem Occured while copying the logs to the result server");
				}

				// Zip the monitoring logs
				String zipFile = "/tmp/"
						+ targetTestProfile.requestUUID.toString()
						+ "_results.zip";
				if (!FileUtilities.zipComplete(targetTestProfile.baseWorkingDir
						+ "/TestStrategy/MonitoringLogs", zipFile)) {
					Logger.logError("Problem Occured while Trying to create Zip folder for results");
				}

				// Place then in to downlodable location
				if (!FileUtilities.scpTo(zipFile,
						PropertyParser.resultPublicServer,
						PropertyParser.resultDownloadablePath,
						PropertyParser.scpAccessUserName)) {
					Logger.logError("Problem Occured while Trying to copy the result Zip folder to public server");
				}

				Logger.logInfo("Request :"
						+ targetTestProfile.requestUUID.toString()
						+ " Processing Complete!!");
				// Update the db with the path of the zip file
				if (req != null
						&& req.getCurrentStatus() == ServerRequestStatus.COMPLETED) {
					dbFacade.updateRequestResult(
							targetTestProfile.requestUUID,
							PropertyParser.resultServerName,
							PropertyParser.remoteServerResultPath
									+ "/"
									+ (new File(targetTestProfile.baseLogDir))
											.getName()
									+ "/"
									+ (new File(
											targetTestProfile.baseWorkingDir))
											.getName(),
							PropertyParser.resultPublicServer,
							PropertyParser.resultDownloadablePath + "/"
									+ (new File(zipFile)).getName());
				}

				if (!sendEmail(targetTestProfile, req)) {
					Logger.logError("Problem Occured while sending email");
				} else {
					Logger.logInfo("Notification Mail Sent to the requester");
				}

			}
		} else {
			Logger.logInfo("Request :"
					+ targetTestProfile.requestUUID.toString()
					+ " Processing Complete!!");
			this.targetLogger.endLog();
			dbFacade.updateRequestStatus(targetTestProfile.requestUUID,
					ServerRequestStatus.INPUT_INVALID.name());
		}
	}

	private String extractAppZip(String zipPath, String destDir) {
		String targetDir = null;
		try{
		if (zipPath != null && destDir != null) {
			if (zipPath.toLowerCase().endsWith(".zip")
					&& (new File(zipPath)).exists()) {
				
				ExecHelper.RunProgram("unzip " + zipPath + " -d " + destDir,
						true);				

			} else if (zipPath.toLowerCase().endsWith(".tar.gz")) {
				FileUtilities.createDirectory(destDir);
				ExecHelper.RunProgram(
						"tar -xvzf " + zipPath + " -C " + destDir, true);
			}
			
			boolean dirFound = false;
			if ((new File(destDir)).exists()) {
				targetDir = destDir;
				while ((new File(targetDir)).listFiles().length == 1) {
					dirFound = false;
					for (File f : (new File(targetDir)).listFiles()) {
						if (f.isDirectory()) {
							targetDir = targetDir + "/" + f.getName();
							dirFound = true;
							break;
						}
					}
					if (!dirFound) {
						break;
					}
				}
			}
		}
		} catch(Exception e){
			Logger.logError("Problem occured while trying to extract file:"+zipPath +" to "+destDir);
			Logger.logException(e);
		}

		return targetDir;
	}

	private void postProcessing(String monitoringLogsBaseDir) {
		if (monitoringLogsBaseDir != null
				&& PropertyParser.postProcessingScript != null
				&& PropertyParser.webServerForResults != null) {
			try {
				String command = "python "
						+ PropertyParser.postProcessingScript + " "
						+ monitoringLogsBaseDir + " "
						+ PropertyParser.webServerForResults;
				Logger.logInfo("Running Post Processing Command:" + command);
				ExecHelper.RunProgram(command, true);
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
	}

	private HashMap<String, String> getPropertiesMap(
			TestProfile targetTestProfile, String stra) {
		HashMap<String, String> retVal = new HashMap<String, String>();
		if (stra.equals(RandomMonkeyTesting.randomTestingStrategy)) {
			retVal.put(TestStrategy.appCoverageDumpTimeProperty,
					Integer.toString(targetTestProfile.coverageDumpTime));
			retVal.put(TestStrategy.appStartUpTimeProperty,
					Long.toString(targetTestProfile.responseDelay));
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
					Long.toString(targetTestProfile.responseDelay));
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
					Integer.toString(MainHarness.getNextPortNumber()));
		}
		return retVal;
	}

	private boolean isInputValid() {
		boolean retVal = false;
		try {
			if (this.targetType == ServerRequestType.SOURCES) {
				
				String destDir = targetTestProfile.baseAppDir +"_extracted";				
				String targetExtractedAppDir = extractAppZip(targetTestProfile.baseAppDir, destDir);
				if(targetExtractedAppDir != null){
					targetTestProfile.baseAppDir = targetExtractedAppDir;
					ExecHelper.RunProgram("android update project --target "+PropertyParser.androidTarget+" --path "+targetExtractedAppDir, true);
					try{
						AppInstrumenter appInstrumenter = new AppInstrumenter(targetExtractedAppDir, PropertyParser.instrumentationHelperDir);
						if(appInstrumenter.doInstrumentation()){
							String output = ExecHelper.RunProgram("ant -f "
									+ targetExtractedAppDir + "/build.xml clean", true);
							output = ExecHelper.RunProgram("ant -f "
									+ targetExtractedAppDir + "/build.xml "
									+ "instrument", true);
							retVal = output.contains("BUILD SUCCESSFUL");
						}
					} catch(Exception e){
						Logger.logException(e);
					}
				}
			}
			if (this.targetType == ServerRequestType.APK) {
				File dummy = FileUtils.getTempDirectory();
				String actualDummy = dummy + "/" + System.nanoTime();
				FileUtilities.createDirectory(actualDummy);
				ExecHelper.RunProgram("java -jar "
						+ PropertyParser.apkToolLocation + " -q d -f "
						+ targetTestProfile.baseAppDir + " " + actualDummy,
						true);

				retVal = (new File(actualDummy + "/AndroidManifest.xml"))
						.exists();
				FileUtils.deleteDirectory(new File(actualDummy));
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	private static boolean sendEmail(TestProfile targetTestProfile,
			IServerRequest req) {
		boolean retVal = false;
		try {
			String subject = "Request :"
					+ targetTestProfile.requestUUID.toString() + " Completed!!";
			ServerRequestStatus currStatus = ServerRequestStatus.PROBLEM_OCCURED;
			if (req != null) {
				currStatus = req.getCurrentStatus();
			}

			String body = "hi,\n\n The Request you submitted is Completed.\n\nResult:\n";
			body += currStatus.name() + "\nDescription:"
					+ currStatus.toString() + "\n\n";

			body += " \n\n Have a Nice Day\n\n";
			body += "Thanks,\nm3 Reporting";
			retVal = TestProfileReporting.sendMail(subject,
					targetTestProfile.targetEmailAlias, body);
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}
}
