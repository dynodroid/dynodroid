package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class MediaMounted extends BroadCastAction implements IDeviceAction {
	public MediaMounted(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating MediaMounted Action receiver");
		}
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new MediaMounted(completeComponentName, intFilter);
	}
	
	@Override
	public String getBroadCastAction(){
		return "android.intent.action.MEDIA_MOUNTED";
	}
}
