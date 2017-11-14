package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class StartProcessMonitoring extends MonitoringClient {
	public StartProcessMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3StartAppProcess";
			Logger.logInfo("StartProcessMonitoring Initialized for:"+targetDevice.toString());
		}
		else{
			throw new Exception("Unable to initialize StartProcessMonitoring");
		}
	}	
}
