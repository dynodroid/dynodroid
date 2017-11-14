package edu.gatech.dynodroid.DBAccess;

import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import edu.gatech.dynodroid.utilities.Logger;

public class DBHelper implements IDBHelper, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1222465311006944949L;
	private Connection dbConn = null;
	private String serverN = "";
	private String passwd = "";
	private String dbN = "";
	private String uname = "";
	private int reTryCount = 2;

	public DBHelper(String serverName, String dbName, String userName,
			String password) throws Exception {
		try {			
			this.serverN = serverName;
			this.passwd = password;
			this.dbN = dbName;
			this.uname = userName;
			reconnect();
		} catch (Exception e) {
			Logger.logException(e);
			throw e;
		}
	}

	private synchronized void reconnect() {
		try {
			String url = "jdbc:mysql://" + this.serverN + "/" + this.dbN
					+ "?autoReconnect=true";
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			dbConn = (Connection) DriverManager.getConnection(url, this.uname,
					this.passwd);
			Logger.logInfo("Connection to Database:" + this.dbN
					+ " Sucessfully Established");
		} catch (Exception e) {
			Logger.logException(e);
		}
	}

	@Override
	public synchronized ResultSet executeResultSetQuery(String query) {
		ResultSet retVal = null;
		Statement s = null;
		int cou = reTryCount;
		while (cou > 0) {
			cou--;
			try {
				s = (Statement) dbConn.createStatement();
				s.executeQuery(query);
				retVal = s.getResultSet();
				retVal.next();
				break;
			} catch (Exception e) {
				retVal = null;
				Logger.logException(e);
				reconnect();
			}
		}
		return retVal;
	}

	@Override
	public synchronized int executeUpdateQuery(String query) {
		int retVal = -1;
		Statement s = null;
		int cou = reTryCount;
		while (cou > 0) {
			cou--;
			try {
				s = (Statement) dbConn.createStatement();
				retVal = s.executeUpdate(query);
				break;
			} catch (Exception e) {
				Logger.logException(e);
				reconnect();
			}
		}
		return retVal;
	}

	@Override
	public synchronized int executeScalarQuery(String query) {
		int retVal = -1;
		Statement s = null;
		int cou = reTryCount;
		while (cou > 0) {
			cou--;
			try {
				s = (Statement) dbConn.createStatement();
				ResultSet rs = s.executeQuery(query);
				rs.next();
				retVal = rs.getInt(1);
				break;
			} catch (Exception e) {
				Logger.logException(e);
				reconnect();
			}
		}
		return retVal;
	}

}
