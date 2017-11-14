package edu.gatech.dynodroid.deviceEvent;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public abstract class NonMonkeyEvent implements IDeviceAction{
	public abstract boolean triggerAction(ADevice targetDevice,DeviceActionPerformer performer);
}
