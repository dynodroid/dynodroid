package edu.gatech.dynodroid.clients;

import java.util.ArrayList;

import edu.gatech.dynodroid.utilities.Logger;

public class BroadCastReceiverInfo {
	public ArrayList<String> bcActions = new ArrayList<String>();
	public ArrayList<String> bcCategories = new ArrayList<String>();
	public int appId = -1;
	public String receiver = null;

	public static BroadCastReceiverInfo getReceiverInfo(String line) {
		BroadCastReceiverInfo retVal = null;
		if (line != null) {
			try {
				retVal = new BroadCastReceiverInfo();
				// Format
				// V/M3RegReceivers( 68):
				// From:10008;Action:android.intent.action.PROXY_CHANGE;Category:
				// ;Receiver:android.content.IIntentReceiver$Stub$Proxy@4050bf00
				// OR
				// V/M3UnRegReceivers( 68):
				// From:10008;Action:android.net.conn.CONNECTIVITY_CHANGE;Category:;Receiver:android.content.IIntentReceiver$Stub$Proxy@4051efa0
				String[] parts = line.split(";");
				// Get the Appid
				String[] appIdParts = parts[0].split(":");
				retVal.appId = Integer.parseInt(appIdParts[appIdParts.length-1]);
				String[] actionsParts = parts[1].split(":")[1].split(",");
				for(int i=0;i<actionsParts.length;i++){
					retVal.bcActions.add(actionsParts[i]);
				}
				
				//May or may not be present.
				if(parts[2].split(":").length > 1){
					String[] categoryParts = parts[2].split(":")[1].split(",");
					for(int i=0;i<categoryParts.length;i++){
						retVal.bcCategories.add(categoryParts[i]);
					}
				}
				
				retVal.receiver = parts[3].split(":")[1];
			} catch (Exception e) {
				Logger.logException(e);
				retVal = null;
			}

		}
		return retVal;
	}

}
