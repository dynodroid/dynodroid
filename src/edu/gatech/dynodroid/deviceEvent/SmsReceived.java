package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class SmsReceived extends BroadCastAction implements IDeviceAction {	
	
	public SmsReceived(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating SmsReceived Action receiver");
		}
	}

	@Override
	public boolean triggerAction(ADevice targetDevice,DeviceActionPerformer performer) {
		if(targetDevice != null){
			return targetDevice.sendSMS("6789077112", "Come over! lets have a coffe");
		}
		return false;
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new SmsReceived(completeComponentName, intFilter);
	}
	
	@Override
	public String getBroadCastAction(){
		return "android.provider.Telephony.SMS_RECEIVED";
	}

}
