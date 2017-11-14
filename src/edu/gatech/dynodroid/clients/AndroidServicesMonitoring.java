package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class AndroidServicesMonitoring extends MonitoringClient{
	
	public AndroidServicesMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3GetSystemService";
			Logger.logInfo("AndroidServiceMonitoring Initialized for:"
					+ targetDevice.toString());
		} else {
			throw new Exception("Unable to initialize AndroidServiceMonitoring");
		}
	}

}
