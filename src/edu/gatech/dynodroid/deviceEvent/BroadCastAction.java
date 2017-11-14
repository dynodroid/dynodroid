package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.utilities.Logger;

public abstract class BroadCastAction extends NonMonkeyEvent{

	protected String targetComponentName = null;
	protected ArrayList<String> intentCategories = new ArrayList<String>();
	
	// These are just static objects to create correct object when asked for
	/*private static BaterryChanged batteryChanged = null;
	private static BaterryLow batteryLow = null;
	private static BaterryOkay batteryOkay = null;
	private static BootCompleted bootCompleted = null;
	private static PhoneStateChanged phoneStateChanged = null;
	private static PowerConnected powerConnected = null;
	private static PowerDisconnected powerDisconnected = null;
	private static SmsReceived smsReceived = null;
	private static AudioBecomingNoisy audioBecomingNoisy = null;
	private static ConnectivityChange connectivtyChange = null;
	private static MediaMounted mediaMounted = null;*/
	
	private static ArrayList<BroadCastAction> supportedBroadCastActions = new ArrayList<BroadCastAction>();

	static {
		try {
			String dummy = "dummy";
			/*batteryChanged = new BaterryChanged(dummy,null);
			batteryLow = new BaterryLow(dummy,null);
			batteryOkay = new BaterryOkay(dummy,null);
			bootCompleted = new BootCompleted(dummy,null);
			phoneStateChanged = new PhoneStateChanged(dummy,null);
			powerConnected = new PowerConnected(dummy,null);
			powerDisconnected = new PowerDisconnected(dummy,null);
			smsReceived = new SmsReceived(dummy,null);
			audioBecomingNoisy = new AudioBecomingNoisy(dummy, null);
			connectivtyChange = new ConnectivityChange(dummy, null);
			mediaMounted = new MediaMounted(dummy, null);*/
			
			supportedBroadCastActions.add(new AppWidgetUpdateAction(dummy, null));
			supportedBroadCastActions.add(new AudioBecomingNoisy(dummy, null));
			supportedBroadCastActions.add(new BaterryChanged(dummy,null));
			supportedBroadCastActions.add(new BaterryLow(dummy,null));
			supportedBroadCastActions.add(new BaterryOkay(dummy,null));
			supportedBroadCastActions.add(new BootCompleted(dummy,null));
			supportedBroadCastActions.add(new ConnectivityChange(dummy, null));
			supportedBroadCastActions.add(new DateChanged(dummy, null));
			supportedBroadCastActions.add(new InputMethodChanged(dummy,null));
			supportedBroadCastActions.add(new MediaEject(dummy, null));
			supportedBroadCastActions.add(new MediaMounted(dummy, null));
			supportedBroadCastActions.add(new MediaScannerFinished(dummy, null));
			supportedBroadCastActions.add(new MediaUnmounted(dummy, null));
			supportedBroadCastActions.add(new NewOutgoingCall(dummy, null));
			supportedBroadCastActions.add(new PackageAdded(dummy,null));
			supportedBroadCastActions.add(new PackageRemoved(dummy,null));
			supportedBroadCastActions.add(new PackageReplaced(dummy,null));
			supportedBroadCastActions.add(new PhoneStateChanged(dummy,null));
			supportedBroadCastActions.add(new PowerConnected(dummy,null));
			supportedBroadCastActions.add(new PowerDisconnected(dummy,null));
			supportedBroadCastActions.add(new ShutdownAction(dummy,null));
			supportedBroadCastActions.add(new SmsReceived(dummy,null));
			supportedBroadCastActions.add(new TimeSetAction(dummy, null));
			supportedBroadCastActions.add(new TimeZoneChanged(dummy, null));
			supportedBroadCastActions.add(new UMSConnected(dummy, null));
			supportedBroadCastActions.add(new UMSDisconnected(dummy, null));
			supportedBroadCastActions.add(new UserPresentAction(dummy, null));
			
		} catch (Exception e) {
			Logger.logException(e);
		}
	}

	public ArrayList<String> getMonkeyCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public String actionName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCallBackName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBroadCastAction() {
		return "NULL_BROADCAST";
	}
	
	public abstract BroadCastAction getInstance(String completeComponentName,ArrayList<String> intFilter) throws Exception;

	public boolean triggerAction(ADevice targetDevice,DeviceActionPerformer performer) {
		if (targetDevice != null) {
			ArrayList<String> output = new ArrayList<String>();
			String categoryString=null;
			if(this.intentCategories != null && this.intentCategories.size() > 0){
				for(String s:this.intentCategories){
					if(categoryString == null){
						categoryString = " -c " +s;
					} else{
						categoryString += " -c " + s;
					}
				}
			}
			String targetBroadCast = null;
			if(targetComponentName == null || !targetComponentName.contains("/")){
				targetBroadCast = "am broadcast -a "
						+ getBroadCastAction() +(categoryString !=null?(categoryString+" "):" ")+"--receiver-registered-only";
			} else{
				targetBroadCast ="am broadcast -a "
						+ getBroadCastAction()+(categoryString !=null?(categoryString+" "):" ") +" -n "+this.targetComponentName;
			}
			
			Logger.logInfo("Trying to BroadCast:"+targetBroadCast);
			output = targetDevice.executeShellCommand(targetBroadCast);
			for (String s : output) {
				if (s.contains("Broadcast completed")) {
					Logger.logInfo(s);
					return true;
				}
			}
			//Use this if you want to simulate
			//Logger.logInfo(getBroadCastAction()+" will be fired here");
		}
		return false;
	}

	@Override
	public String toString() {
		return "Event:" + getBroadCastAction() + "~->To~->"
				+ this.targetComponentName;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BroadCastAction) {
			BroadCastAction that = (BroadCastAction) o;
			return getBroadCastAction().equals(that.getBroadCastAction())
					&& this.targetComponentName
							.equals(that.targetComponentName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getBroadCastAction().hashCode()
				^ this.targetComponentName.hashCode();
	}

	public static BroadCastAction getBroadCastEvent(String receiverName,
			String broadcastAction,ArrayList<String> intFilter) {
		BroadCastAction retVal = null;
		try {
			if (broadcastAction != null) {
				for(BroadCastAction a:supportedBroadCastActions){
					if(a.getBroadCastAction().equals(broadcastAction)){
						retVal = a.getInstance(receiverName, intFilter);
						break;
					}
				}				
				if (retVal == null) {
					Logger.logError("Ignoring:" + broadcastAction
							+ " as we don't have handlers coded for this :(");
				}
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}
}
