package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class UMSDisconnected extends BroadCastAction implements IDeviceAction {
	
	public UMSDisconnected(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating UMSDisconnected receiver");
		}
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new UMSDisconnected(completeComponentName, intFilter);
	}

	@Override
	public String getBroadCastAction(){
		return "android.intent.action.UMS_DISCONNECTED";
	}

}
