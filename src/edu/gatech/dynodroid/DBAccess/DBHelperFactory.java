package edu.gatech.dynodroid.DBAccess;

import java.io.Serializable;
import java.util.HashMap;


/***
 * We don't want to create unnecessary objects so we cache dbHelper objects so
 * that they can be reused across requests
 * 
 * @author machiry
 * 
 */
public class DBHelperFactory implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2583219812577536171L;
	//The Main Cache
	private static HashMap<String, IDBHelper> dbHelperCache = new HashMap<String, IDBHelper>();

	
	/***
	 * This method returns the DBHelper object for the required server with the provided credentials.
	 * 
	 * @param serverName target server name on which mysql is hosted
	 * @param dbName target database name
	 * @param userName user name using which we need to connect to the database
	 * @param password password for the database
	 * @return IDBHelper the target DBHelper object
	 */
	public static IDBHelper getDBHelper(String serverName, String dbName,
			String userName, String password) {
		if (serverName != null && dbName != null && userName != null
				&& password != null) {
			String cacheKey = ":" + serverName + ":" + dbName + ":" + userName
					+ ":" + password + ":";
			synchronized (dbHelperCache) {
				if (dbHelperCache.containsKey(cacheKey)) {
					return dbHelperCache.get(cacheKey);
				}

				try {
					IDBHelper newDBHelper = new DBHelper(serverName, dbName,
							userName, password);
					if (newDBHelper != null) {
						dbHelperCache.put(cacheKey, newDBHelper);
					}
					return newDBHelper;
				} catch (Exception e) {
					// Ignore
				}

			}
		}

		return null;

	}
}
