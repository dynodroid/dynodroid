package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class IntentStartMonitoring extends MonitoringClient {
	public IntentStartMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "Starting: Intent";
			Logger.logInfo("IntentStartMonitoring Initialized for:"+targetDevice.toString());
		}
		else{
			throw new Exception("Unable to initialize IntentStartMonitoring");
		}
	}	
}
