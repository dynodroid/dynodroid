package edu.gatech.dynodroid.DBAccess;

import java.util.UUID;

public interface IDBFacade {	
	public abstract boolean insertRequest(UUID reqID, String fileServer,String payloadFileName,String mailID,String type);
	public abstract boolean updateRequestStatus(UUID reqID,String statusID);
	public abstract String getRequestStatus(UUID reqID);
	public abstract String getRequestResult(UUID reqID);
	public abstract boolean updateRequestResult(UUID reqID, String privateServer,String privatePath,String downloadFileServer,String downloadFileName);
	
}
