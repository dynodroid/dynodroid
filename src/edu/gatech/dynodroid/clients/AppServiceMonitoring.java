package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class AppServiceMonitoring extends MonitoringClient {	
	
	public AppServiceMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3AppService";
			Logger.logInfo("AppServiceMonitoring Initialized for:"+targetDevice.toString());
		}
		else{
			throw new Exception("Unable to initialize AppServiceMonitoring");
		}
	}	

}
