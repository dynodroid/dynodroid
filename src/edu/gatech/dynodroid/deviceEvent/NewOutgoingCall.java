package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class NewOutgoingCall extends BroadCastAction implements IDeviceAction {
	
	public NewOutgoingCall(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating NewOutgoingCall receiver");
		}
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new NewOutgoingCall(completeComponentName, intFilter);
	}

	@Override
	public String getBroadCastAction(){
		return "android.intent.action.NEW_OUTGOING_CALL";
	}

}
