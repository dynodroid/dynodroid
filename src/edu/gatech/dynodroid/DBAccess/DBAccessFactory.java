package edu.gatech.dynodroid.DBAccess;

import java.io.Serializable;
import java.util.HashMap;

public class DBAccessFactory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8541099906491236607L;
	//The Main Cache
		private static HashMap<String, IDBFacade> dbFacadeCache = new HashMap<String, IDBFacade>();

		
		public static IDBFacade getDBAccess(String serverName, String dbName,
				String userName, String password) {
			
			if (serverName != null && dbName != null && userName != null
					&& password != null) {
				String cacheKey = ":" + serverName + ":" + dbName + ":" + userName
						+ ":" + password + ":";
				synchronized (dbFacadeCache) {
					if (dbFacadeCache.containsKey(cacheKey)) {
						return dbFacadeCache.get(cacheKey);
					}

					try {
						IDBFacade newDBFacade = new ServerDBAccess(DBHelperFactory.getDBHelper(serverName, dbName, userName, password));
						if (newDBFacade != null) {
							dbFacadeCache.put(cacheKey, newDBFacade);
						}
						return newDBFacade;
					} catch (Exception e) {
						// Ignore
					}

				}
			}

			return null;

		}
}
