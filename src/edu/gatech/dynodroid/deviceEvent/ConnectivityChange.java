package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class ConnectivityChange extends BroadCastAction implements IDeviceAction {
	
	public ConnectivityChange(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating PowerConnected Action receiver");
		}
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new ConnectivityChange(completeComponentName, intFilter);
	}
	
	@Override
	public String getBroadCastAction(){
		return "android.net.conn.CONNECTIVITY_CHANGE";
	}

}
