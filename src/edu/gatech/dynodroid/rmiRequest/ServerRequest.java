package edu.gatech.dynodroid.rmiRequest;

import java.util.UUID;

import edu.gatech.dynodroid.DBAccess.IDBFacade;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.master.TestProfile;
import edu.gatech.dynodroid.testHarness.TestStrategy;
import edu.gatech.dynodroid.utilities.TextLogger;

public class ServerRequest implements IServerRequest {
	public ServerRequestStatus currentStatus = ServerRequestStatus.RECEIVED; 
	public TestProfile targetTestProfile = null;
	public ADevice targetDevice=null;
	private TestStrategy targetTestStrategy = null;
	public UUID requestUUID=null;
	private TextLogger targetLogger = null;
	private IDBFacade targetDBFacade = null;
	private static final String TAG = "ServerRequest";
	private ServerRequestType requestType = ServerRequestType.APK;
	
	public ServerRequest(TestProfile tprof,ADevice tDev,TestStrategy tStra,TextLogger profLog,IDBFacade dbFac,ServerRequestType targetType) throws Exception{
		if(tprof != null && tDev != null && tStra != null && profLog != null && dbFac != null){
			
			this.targetDevice = tDev;
			this.targetTestProfile = tprof;
			this.targetTestStrategy = tStra;
			this.requestUUID = this.targetTestProfile.requestUUID;
			this.targetLogger = profLog;
			this.targetDBFacade = dbFac;
			this.requestType = targetType;
		} else{
			throw new Exception("Unable to create Server Request,one or more parameters are null");
		}
	}

	@Override
	public boolean processRequest() {
		boolean isSuccess = true;		
		if (targetTestStrategy.prepare()) {
			this.targetLogger.logInfo(TAG, "Preparation Sucessfull..");
			targetTestStrategy.perform();
			this.targetLogger.logInfo(TAG, "Perform Done..");
			targetTestStrategy.cleanup();
			this.targetLogger.logInfo(TAG, "Cleanup Done..");
		} else {
			this.targetLogger.logInfo(TAG, "Preparation UnSucessfull..");
		}		
		
		return isSuccess;
	}

	@Override
	public synchronized void updateStatus(ServerRequestStatus newStatus) {
		this.currentStatus = newStatus;
		this.targetDBFacade.updateRequestStatus(requestUUID, newStatus.name());
	}

	@Override
	public ServerRequestStatus getCurrentStatus() {
		return this.currentStatus;
	}

	@Override
	public ServerRequestType getRequestType() {
		return requestType;
	}

}
