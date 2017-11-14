/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

/**
 * All Key Press Actions
 * @author machiry
 *
 */
public class KeyPressAction implements IDeviceAction {
	
	public int keyCode=0;
	private static final String actionNamePrefix = "KEYPRESS_";
	public String callBackName;

	public KeyPressAction(int keyC){
		assert(keyC > 0);
		this.keyCode = keyC;
	}
	
	public KeyPressAction(int keyC,String callB){
		assert(keyC > 0);
		this.keyCode = keyC;
		this.callBackName = callB;
	}
	
	/* (non-Javadoc)
	 * @see edu.gatech.m3.hierarchyHelper.DeviceAction#getMonkeyCommand()
	 */
	@Override
	public ArrayList<String> getMonkeyCommand() {
		ArrayList<String> retVal = new ArrayList<String>();
		retVal.add("DispatchKey(5000,5000,"+keyCode+",0,0,0,0)"); 
		return retVal;
	}

	/* (non-Javadoc)
	 * @see edu.gatech.m3.hierarchyHelper.DeviceAction#actionName()
	 */
	@Override
	public String actionName() {
		return actionNamePrefix+keyCode;
	}

	@Override
	public String getCallBackName() {
		return this.callBackName;
	}
	
	@Override
	public int hashCode(){
		int retVal = 0;
		if(this.callBackName != null){
			retVal = this.callBackName.hashCode();
		}
		return retVal ^ keyCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof KeyPressAction){
			KeyPressAction that = (KeyPressAction)obj;
			if(this.callBackName == null && that.callBackName==null){
				return this.keyCode == that.keyCode;
			}
			if(this.callBackName != null && that.callBackName != null){
				return this.keyCode==that.keyCode && this.callBackName.equals(that.callBackName);
			}
			return false;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return actionName();
	}

}
