/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

/**
 * This is the class that encapsulates an action that needs to be performed on the android device
 * @author machiry
 *
 */
public interface IDeviceAction {
	
	/***
	 * This method is used to return the monkey command corresponding to the underlying action
	 * @return command strings
	 */
	
	public abstract ArrayList<String> getMonkeyCommand();
	
	/***
	 * The action name of the current action
	 * @return Name of the action
	 */
	public abstract String actionName();
	
	/***
	 * This method returns the call back (if any) registered for this particulat device action
	 * 
	 * @return method signature of the registered call back
	 */
	public abstract String getCallBackName();

}
