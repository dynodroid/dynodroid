package edu.gatech.dynodroid.deviceEvent;

import java.util.ArrayList;
import java.util.Arrays;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;

public class KeyEvent extends NonMonkeyEvent {
	
	int targetKeyCode = 0;	
	String callBackName = null;
	private static final Integer[] mediaButtonsI = new Integer[]{128,129,90,87,127,126,85,88,130,89,86};
	
	public static final ArrayList<Integer> mediaButtons = new ArrayList<Integer>(Arrays.asList(mediaButtonsI));
	
	public KeyEvent(String callBackName,int keyCode){
		this.targetKeyCode = keyCode;
		this.callBackName = callBackName;
	}

	@Override
	public ArrayList<String> getMonkeyCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String actionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCallBackName() {
		// TODO Auto-generated method stub
		return this.callBackName;
	}

	@Override
	public boolean triggerAction(ADevice targetDevice,
			DeviceActionPerformer performer) {	
		if (targetDevice != null && performer != null) {			
			ArrayList<String> output = targetDevice.executeShellCommand("input keyevent "+this.targetKeyCode);
			if(output == null || output.size() == 0){
				return true;
			}
			
		}		
		return false;
		
	}
	
	@Override
	public int hashCode(){
		if(mediaButtons.contains(targetKeyCode)){
			return "MediaButton".hashCode() ^ this.callBackName.hashCode();
		}
		return Integer.valueOf(targetKeyCode).hashCode() ^ this.callBackName.hashCode();		
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof KeyEvent){
			KeyEvent that = (KeyEvent)o;
			if(mediaButtons.contains(this.targetKeyCode) || mediaButtons.contains(that.targetKeyCode)){
				return mediaButtons.contains(this.targetKeyCode) && mediaButtons.contains(that.targetKeyCode) && this.callBackName.equals(that.callBackName);
			}			
			return this.targetKeyCode == that.targetKeyCode && this.callBackName.equals(that.callBackName);
		}
		return false;
	}
	

}
