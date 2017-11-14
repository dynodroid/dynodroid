package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class DalvikMethodCoverageMonitoring extends MonitoringClient {
	public DalvikMethodCoverageMonitoring(ADevice targetDevice)
			throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "DVM_M3_Method:";
			Logger.logInfo("OutBoundUrlMonitoring Initialized for:"
					+ targetDevice.toString());
		} else {
			throw new Exception("Unable to initialize OutBoundUrlMonitoring");
		}
	}

	@Override
	public boolean consume(String entry) {
		if (this.toMonitor && entry != null) {
			if (entry.contains(filterString)) {
				String methodCovered = entry.split(":")[entry.split(":").length - 1];
				synchronized (logEntries) {
					logEntries.add(methodCovered);
				}
				return true;
			}
		}
		return false;
	}
}
