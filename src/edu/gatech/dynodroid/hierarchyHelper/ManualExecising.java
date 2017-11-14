/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.deviceEvent.NonMonkeyEvent;

/**
 * @author machiry
 * 
 */
public class ManualExecising extends NonMonkeyEvent {

	private ViewNode targetViewElement;

	public ManualExecising(ViewNode v) throws Exception {
		if (v != null) {
			targetViewElement = v;
		} else {
			throw new Exception("Null View");
		}
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
		return null;
	}

	@Override
	public boolean triggerAction(ADevice targetDevice,
			DeviceActionPerformer performer) {
		try {
			System.out
					.println("Manual Mode Started,Please Play with the app on the emulator:");
			System.out.println("Press Enter Key to Continue");
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			br.readLine();
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	@Override
	public int hashCode() {
		return targetViewElement.hashCode() ^ "ManualExecising".hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ManualExecising) {
			ManualExecising that = (ManualExecising) o;
			return this.targetViewElement.equals(that.targetViewElement);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "Manually Execising :" + targetViewElement.toString();
	}

}
