package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;

/***
 * 
 * @author machiry
 *
 */
public abstract class ViewScreenExeciser {
	
	/***
	 * 
	 * @param targetTestDevice
	 * @param targetScreen
	 * @param targetAction
	 * @return
	 */
	public abstract boolean execiseScreen(ADevice targetTestDevice,ViewScreen targetScreen,ViewScreenAction targetAction);
	
	public abstract boolean endTracing();
}
