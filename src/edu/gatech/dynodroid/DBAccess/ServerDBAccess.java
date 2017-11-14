package edu.gatech.dynodroid.DBAccess;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.UUID;

public class ServerDBAccess implements IDBFacade, Serializable {

	private static final long serialVersionUID = -8624788814370606850L;
	private IDBHelper dbHelper = null;

	public ServerDBAccess(IDBHelper dbHelp) throws Exception {
		if (dbHelp != null) {
			this.dbHelper = dbHelp;
		} else {
			throw new Exception("Unable to initialize ServerDBAccess");
		}
	}

	@Override
	public boolean insertRequest(UUID reqID, String fileServer,String payloadFileName,
			String mailID, String type) {
		String queryFormat = "insert into requests(requestid,typeid,emailid,fileserver,filepath) values(\'%s\',%d,\'%s\',\'%s\',\'%s\')";
		if (reqID != null && payloadFileName != null && mailID != null) {
			// TODO: check for sql injection
			int typeID = getRequestTypeID(type);
			if (typeID != -1) {
				String actualQuery = String.format(queryFormat,
						reqID.toString(), typeID, mailID, fileServer,payloadFileName);
				int colAffected = this.dbHelper.executeUpdateQuery(actualQuery);
				return colAffected > 0;
			}

		}
		return false;
	}

	@Override
	public boolean updateRequestStatus(UUID reqID, String statusID) {
		String queryFormat = "insert into request_status_log(requestid,statusid) values(\'%s\',%d)";
		if (reqID != null) {
			// TODO: check for sql injection
			int typeID = getRequestStatusID(statusID);
			if (typeID != -1) {
				String actualQuery = String.format(queryFormat,
						reqID.toString(), typeID);
				int colAffected = this.dbHelper.executeUpdateQuery(actualQuery);
				return colAffected > 0;
			}
		}
		return false;
	}

	@Override
	public String getRequestStatus(UUID reqID) {
		String queryFormat = "select rs.name from request_status as rs, request_status_log as rsl where rsl.requestid = \'%s\' and rsl.statusid = rs.id order by rsl.data desc limit 1";
		String defStatus = "NOT_FOUND";
		if (reqID != null) {
			// TODO: check for sql injection
			String actualQuery = String.format(queryFormat, reqID.toString());
			ResultSet rs = this.dbHelper.executeResultSetQuery(actualQuery);
			if (rs != null) {
				try {
					defStatus = rs.getString(1);
				} catch (Exception e) {
					defStatus = "NOT_FOUND";
					// TODO: Log exception
				}
			}
		}
		return defStatus;
	}

	@Override
	public String getRequestResult(UUID reqID) {
		String queryFormat = "select rr.result_file from request_results as rr where rr.requestid=\'%s\'";
		String defLocation = "NOT_FOUND";
		if (reqID != null) {
			// TODO: check for sql injection
			String actualQuery = String.format(queryFormat, reqID.toString());
			ResultSet rs = this.dbHelper.executeResultSetQuery(actualQuery);
			if (rs != null) {
				try {
					defLocation = rs.getString(1);
				} catch (Exception e) {
					defLocation = "NOT_FOUND";
					// TODO: Log exception
				}
			}
		}
		return defLocation;
	}

	@Override
	public boolean updateRequestResult(UUID reqID, String privateServer,String privatePath,String downloadFileServer,String downloadFileName) {
		String queryFormat = "insert into request_results(requestid,public_result_server,public_result_file,private_result_server,private_result_file) values(\'%s\',\'%s\',\'%s\',\'%s\',\'%s\')";
		if (reqID != null) {
			// TODO: check for sql injection
			String actualQuery = String.format(queryFormat, reqID.toString(),
					downloadFileServer,downloadFileName,privateServer,privatePath);
			int colAffected = this.dbHelper.executeUpdateQuery(actualQuery);
			return colAffected > 0;
		}
		return false;
	}

	public int getRequestTypeID(String typeName) {
		int retVal = -1;
		String queryFormat = "select id from request_type where name = \'%s\'";
		if (typeName != null) {
			// TODO: check for sql injection
			String actualQuery = String.format(queryFormat, typeName);
			ResultSet rs = this.dbHelper.executeResultSetQuery(actualQuery);
			if (rs != null) {
				try {
					retVal = rs.getInt(1);
				} catch (Exception e) {
					// TODO: Log exception
					retVal = -1;
				}
			}
		}
		return retVal;
	}
	
	public int getRequestStatusID(String statusName) {
		int retVal = -1;
		String queryFormat = "select id from request_status where name = \'%s\'";
		if (statusName != null) {
			// TODO: check for sql injection
			String actualQuery = String.format(queryFormat, statusName);
			ResultSet rs = this.dbHelper.executeResultSetQuery(actualQuery);
			if (rs != null) {
				try {
					retVal = rs.getInt(1);
				} catch (Exception e) {
					// TODO: Log exception
					retVal = -1;
				}
			}
		}
		return retVal;
	}

}
