package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class BroadCastActionTriggerMonitoring extends MonitoringClient {
	
	public BroadCastActionTriggerMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3BroadCastDelivary";
			Logger.logInfo("BroadCastActionTriggerMonitoring Initialized for:"+targetDevice.toString());
		}
		else{
			throw new Exception("Unable to initialize BroadCastActionTriggerMonitoring");
		}
	}	
}
