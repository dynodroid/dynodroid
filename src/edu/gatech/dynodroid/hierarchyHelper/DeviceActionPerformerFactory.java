package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.MonkeyTraceLogger;

public class DeviceActionPerformerFactory {
	public static DeviceActionPerformer getDeviceActionPerformer(String type,
			String logFile,String workingDir,boolean saveTempFiles, ADevice device) {
		DeviceActionPerformer retVal = null;
		try {
			if (type.equals(MonkeyBasedActionPerformer.monkeyActionPerformer)) {
                            retVal = new MonkeyBasedActionPerformer(new MonkeyTraceLogger(logFile),workingDir,saveTempFiles, device);
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}
}
