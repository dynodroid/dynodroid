package edu.gatech.dynodroid.RMIHelper;

import java.rmi.Naming;

import edu.gatech.dynodroid.utilities.Logger;

public class RMIHelper {
	
	private static String m3RMISubmitServerName = "M3SubmitServer";
	
	public static boolean bindRMIBackEndServer(RMIBackEndServer server){
		boolean retVal = false;
		if(server != null){
			try{
				Naming.rebind(m3RMISubmitServerName, server);
				Logger.logInfo("RMIBackEndServer bind sucessfull..");
				retVal = true;
			} catch(Exception e){
				Logger.logException(e);
			}
		}
		return retVal;		
	}
}
