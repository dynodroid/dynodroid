package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class DexLoadingMonitoring extends MonitoringClient {

	public DexLoadingMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3_SYS:DexClassLoader";
			Logger.logInfo("Dex Load Monitoring Initialized for:"+targetDevice.toString());
		}
		else{
			throw new Exception("Unable to initialize DexLoadingMonitoring");
		}
	}	
}
