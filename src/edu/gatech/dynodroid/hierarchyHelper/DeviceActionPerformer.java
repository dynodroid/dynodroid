/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.TraceLogger;

/**
 * This class encapsulates the Action performer
 * @author machiry
 * 
 */
public abstract class DeviceActionPerformer {
	
	/***
	 * This methid is used to perform the given action on the given device
	 * @param action target action to be performed
	 * @param targetDevice device on which the action needs to be performed
	 * @return true/false depending on whether performing action is sucessfull or not
	 */
	public abstract boolean performAction(IDeviceAction action,
			ADevice targetDevice);
	
	/***
	 * used to set the working directory for the action performer
	 * (this is not mandatory , performers need not have any working directory)
	 * @param wd the resulting directory 
	 */
	public void setWorkingDir(String wd){
	}
	
	/***
	 * End the tracing (if this performer is doing any tracing)
	 * @return true if tracing is ended
	 */
	
	public boolean endTracing(){
		return false;
	}
	
	public TraceLogger getTraceLogger(){
		return null;
	}
	
}
