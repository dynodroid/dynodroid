package edu.gatech.dynodroid.RMIHelper;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import edu.gatech.dynodroid.DBAccess.DBAccessFactory;
import edu.gatech.dynodroid.DBAccess.IDBFacade;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.master.TestProfile;
import edu.gatech.dynodroid.rmiRequest.ServerRequestHandler;
import edu.gatech.dynodroid.rmiRequest.ServerRequestStatus;
import edu.gatech.dynodroid.rmiRequest.ServerRequestType;
import edu.gatech.dynodroid.testHarness.WidgetBasedTesting;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

public class RMIBackEndServerImpl extends UnicastRemoteObject implements
		RMIBackEndServer, Serializable {

	private static IDBFacade dbFacade = null;
	private static final int defaultMaxEvents = 10000;

	public RMIBackEndServerImpl(String dbConnectionsString)
			throws RemoteException {
		super();
		try {
			String[] parts = dbConnectionsString.split(";");
			dbFacade = DBAccessFactory.getDBAccess(parts[0], parts[1],
					parts[2], parts[3]);
			Logger.logInfo("RMI Back End Server Created");
		} catch (Exception e) {
			Logger.logException(e);
			throw new RemoteException(e.getMessage());
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2979994852109865472L;

	@Override
	public UUID submitRequest(String filePath, String emailID, String fileServer,String requestType,int noOfEvents)
			throws RemoteException {

		UUID targetUUID = null;
		try {
			targetUUID = UUID.randomUUID();
			Logger.logInfo("Received Request:"+targetUUID.toString());
			Logger.logInfo("FilePath="+filePath+" emailID="+emailID+" fileServer="+fileServer+" requestType="+requestType+" noOfEvents="+noOfEvents);
			ServerRequestType reqType = ServerRequestType.valueOf(requestType);
			dbFacade.insertRequest(targetUUID, fileServer,filePath, emailID,
					reqType.name());
			dbFacade.updateRequestStatus(targetUUID, ServerRequestStatus.RECEIVED.name());
			String appWorkingDir = "/tmp/m3workdir/" + targetUUID.toString();
			FileUtilities.createDirectory(appWorkingDir);
			String apkFolder = appWorkingDir + "/app";
			FileUtilities.createDirectory(apkFolder);
			String logsDir = appWorkingDir + "/"+targetUUID.toString()+"_logs";
			
			String apkFilePath = apkFolder + "/"
					+ (new File(filePath)).getName();
			TestProfile targetTestProfile = getDefaultProfile(logsDir,
					apkFilePath,emailID);
			if(noOfEvents > 0 && noOfEvents <= defaultMaxEvents){
				targetTestProfile.maxNoOfWidgets = noOfEvents;
			}
			targetTestProfile.requestUUID = targetUUID;
			if (FileUtilities.scpFrom(fileServer, filePath, apkFolder,
					PropertyParser.scpAccessUserName)) {
				TextLogger newLogger = new TextLogger(logsDir
						+ "/TotalExecution.log");
				ServerRequestHandler handler = new ServerRequestHandler(
						targetTestProfile, dbFacade,newLogger,reqType);
				Thread th = new Thread(handler);
				th.start();			

			} else {
				Logger.logError("Problem occured while trying to copy the provided file:"
						+ filePath + " from remote server:" + fileServer);
			}
		} catch (Exception e) {
			Logger.logException(e);
			dbFacade.updateRequestStatus(targetUUID,
					ServerRequestStatus.PROBLEM_OCCURED.name());
		}
		// TODO Auto-generated method stub
		return targetUUID;
	}

	private TestProfile getDefaultProfile(String baseWorkingDir,
			String apkFileLoc,String emailID) {
		TestProfile newTestProfile = new TestProfile();
		File apkFile = new File(apkFileLoc);
		newTestProfile.appName = apkFile.getName();
		newTestProfile.baseAppDir = apkFile.getAbsolutePath();
		newTestProfile.sdkInstallPath = PropertyParser.sdkInstallPath;
		newTestProfile.emmaLibPath = PropertyParser.emmaLibPath;
		newTestProfile.testStrategy = WidgetBasedTesting.widgetBasedTestingStrategy;
		newTestProfile.maxNoOfWidgets = PropertyParser.maxNoOfWidgets[0];
		newTestProfile.widgetSelectionStrategy = PropertyParser.widgetSelectionStrategy
				.get(0);
		newTestProfile.baseWorkingDir = baseWorkingDir + "/"
				+ newTestProfile.appName + "_WBT_"
				+ newTestProfile.widgetSelectionStrategy + "_"
				+ newTestProfile.maxNoOfWidgets;
		newTestProfile.baseLogDir = baseWorkingDir;
		FileUtilities.createDirectory(newTestProfile.baseWorkingDir);
		newTestProfile.touchPercentage = PropertyParser.touchPercentage;
		newTestProfile.smallNavigationPercentage = PropertyParser.smallNavigationPercentage;
		newTestProfile.majorNavigationPercentage = PropertyParser.majorNavigationPercentage;
		newTestProfile.trackballPercentage = PropertyParser.trackballPercentage;
		newTestProfile.responseDelay = PropertyParser.responseDelay;
		newTestProfile.delayBetweenEvents = PropertyParser.delayBetweenEvents;
		newTestProfile.verboseLevel = PropertyParser.verboseLevel;
		newTestProfile.coverageDumpTime = PropertyParser.coverageDumpTime;
		newTestProfile.instrumetationSetupDir = PropertyParser.instrumentationHelperDir;
		newTestProfile.coverageSamplingInterval = PropertyParser.coverageSamplingInterval;
		newTestProfile.isApk = true;
		newTestProfile.targetEmailAlias = emailID;

		return newTestProfile;
	}
	

}
