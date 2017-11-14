package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;

/***
 * 
 * @author machiry
 *
 */
public abstract class ViewElementExeciser {
	
	/***
	 * 
	 * @param targetDevice
	 * @param targetElement
	 * @param actionTobePerformed
	 * @return
	 */
	public abstract boolean execiseElement(ADevice targetDevice,ViewElement targetElement,ViewElementAction actionTobePerformed);
	
	public abstract boolean endTracing();

}
