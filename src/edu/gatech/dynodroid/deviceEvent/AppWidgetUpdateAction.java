package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;

public class AppWidgetUpdateAction extends BroadCastAction implements IDeviceAction {
	
	public AppWidgetUpdateAction(String completeComponentName,ArrayList<String> intFilter) throws Exception{
		if(completeComponentName != null){
			this.targetComponentName = completeComponentName;
			if(intFilter != null && intFilter.size() > 0){
				this.intentCategories.addAll(intFilter);
			}
		} else{
			throw new Exception("Problem occured while creating BaterryLow Action receiver");
		}
	}
	
	@Override
	public BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception {
		return new AppWidgetUpdateAction(completeComponentName, intFilter);
	}

	@Override
	public String getBroadCastAction(){
		return "android.appwidget.action.APPWIDGET_UPDATE";
	}

}
