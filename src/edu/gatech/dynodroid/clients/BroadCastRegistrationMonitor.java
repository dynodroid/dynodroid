package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.deviceEvent.BroadCastAction;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.Pair;

public class BroadCastRegistrationMonitor extends MonitoringClient {
	
	public BroadCastRegistrationMonitor(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3RegReceivers";
			Logger.logInfo("BroadCastRegistrationMonitor Initialized for:"
					+ targetDevice.toString());
		} else {
			throw new Exception(
					"Unable to initialize BroadCastRegistrationMonitor");
		}
	}
	
	@Override
	public boolean consume(String entry) {
		if (this.toMonitor && entry != null) {
			if (entry.contains(filterString) && entry.contains(Integer.toString(targetAppUid))) {
				synchronized (logEntries) {
					logEntries.add(entry);
				}
				//its ok for the feedback to be null
				if(this.feedBack != null){
					BroadCastReceiverInfo receiveInfo = BroadCastReceiverInfo.getReceiverInfo(entry);
					if(receiveInfo != null){
						for(String actionName:receiveInfo.bcActions){
							BroadCastAction targetAction = BroadCastAction.getBroadCastEvent(receiveInfo.receiver, actionName, receiveInfo.bcCategories);
							if(targetAction != null && (targetAction instanceof IDeviceAction)){
								this.feedBack.addNonUiDeviceAction(new Pair<ViewElement, IDeviceAction>(null, (IDeviceAction)targetAction));
							} else{
								Logger.logError("Unable to parse the receiveInfo to listner");
							}
						}
					} else{
						Logger.logError("Problem occured parsing the register receiver entry:"+entry);
					}
				}
				return true;
			}
		}
		return false;
	}
	
	
}
