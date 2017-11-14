package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

public class OutBoundUrlMonitoring extends MonitoringClient {

	public OutBoundUrlMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3_NET:";
			Logger.logInfo("OutBoundUrlMonitoring Initialized for:"
					+ targetDevice.toString());
		} else {
			throw new Exception("Unable to initialize OutBoundUrlMonitoring");
		}
	}

	@Override
	public boolean consume(String entry) {
		if (this.toMonitor && entry != null) {
			boolean add = false;
			if (entry.contains(filterString)) {
				if (entry.contains("Accept;")) {
					add = !entry.contains("Accept;Server=localhost")
							|| !entry.contains("To::Server=localhost;");
				}
				add = entry.contains("Connect;");

				if (add) {
					synchronized (logEntries) {
						logEntries.add(entry);
					}
					return true;
				}
			}
		}
		return false;
	}
}
