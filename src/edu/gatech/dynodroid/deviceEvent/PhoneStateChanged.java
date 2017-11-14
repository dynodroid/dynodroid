package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.utilities.Logger;

public class PhoneStateChanged extends BroadCastAction implements IDeviceAction {

	public PhoneStateChanged(String completeComponentName,
			ArrayList<String> intFilter) throws Exception {
		if (completeComponentName != null) {
			this.targetComponentName = completeComponentName;
			if (intFilter != null && intFilter.size() > 0) {
				this.intentCategories.addAll(intFilter);
			}
		} else {
			throw new Exception(
					"Problem occured while creating PhoneStateChanged Action receiver");
		}
	}

	@Override
	public String getBroadCastAction() {
		return "android.intent.action.PHONE_STATE";
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new PhoneStateChanged(completeComponentName, intFilter);
	}

	@Override
	public boolean triggerAction(ADevice targetDevice,DeviceActionPerformer performer) {
		String targetNumber = "6789077112";
		if (targetDevice != null) {
			try {
				if (targetDevice.executeDeviceCommand("gsm call "
						+ targetNumber)) {
					Logger.logInfo("Generating Call on device:"+targetDevice.toString());
					Thread.sleep(6000);
					if (targetDevice.executeDeviceCommand("gsm cancel "
							+ targetNumber)) {
						Logger.logInfo("Cancelling call on device:"+targetDevice.toString());
						return true;
					} else{
						Logger.logError("Problem occured while trying to cancel the call on device:"+targetDevice.toString());
					}
				} else{
					Logger.logError("Problem occured while trying to make call on device:"+targetDevice.toString());
				}
			} catch (Exception e) {
				Logger.logException(e);
			}

		}
		return false;
	}

}
