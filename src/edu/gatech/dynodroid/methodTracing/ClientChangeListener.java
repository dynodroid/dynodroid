package edu.gatech.dynodroid.methodTracing;

import java.util.HashMap;

import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.Client;

import edu.gatech.dynodroid.utilities.Logger;

public class ClientChangeListener implements IClientChangeListener {

	private static HashMap<Client, Integer> recentMessageReceived = new HashMap<Client, Integer>();

	public static void enableClientChangeListener(Client target) {
		if (target != null) {
			synchronized (recentMessageReceived) {
				if(!recentMessageReceived.containsKey(target)){
					recentMessageReceived.put(target, -1);
				}
			}
		}
	}
	
	public static void clearClientState(Client target){
		if(target != null){
			synchronized (recentMessageReceived) {
				if(recentMessageReceived.containsKey(target)){
					recentMessageReceived.remove(target);
					recentMessageReceived.put(target, 0);
					Logger.logInfo("Client State Cleared:"+target);
				}
			}
		}
	}
	
	public static int getLatestState(Client target){
		int latestChange = -1;
		if(target != null && recentMessageReceived.containsKey(target)){
			synchronized (recentMessageReceived) {
				latestChange = recentMessageReceived.get(target);
			}
		}
		return latestChange;
	}

	@Override
	public void clientChanged(Client arg0, int arg1) {
		// Logger.logInfo("For Client:"+arg0+" got Message:"+arg1);
		if(recentMessageReceived.containsKey(arg0)){
			Logger.logInfo("For Client:"+arg0+" got Message:"+arg1);
			synchronized (recentMessageReceived) {
				recentMessageReceived.remove(arg0);
				recentMessageReceived.put(arg0, arg1);
			}
		}

	}

}
