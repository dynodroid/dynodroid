/**
 * 
 */
package edu.gatech.dynodroid.testHarness;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.utilities.Pair;

/**
 * @author machiry
 *
 */
public abstract class WidgetSelectionStrategy {
	
	/***
	 * This method gets the next widget to be excised and the corresponding action to be performed
	 * @param currScreen The current screen which the app is in according to the caller
	 * @param lastPerformedAction The pair of ViewElement and the corresponding action that was performed on the widget
	 * @param resultOfLastOperation Result of last operation performed (true/false)
	 * @return Pair of Widget and the action to be performed on it
	 */
	public abstract Pair<ViewElement,IDeviceAction> getNextElementAction(ViewScreen currScreen,Pair<ViewElement,IDeviceAction> lastPerformedAction,boolean resultOfLastOperation);
	
	/***
	 * This method is used to notify the strategy about the new screen that popped up and which 
	 * according to the caller is not same as old screen
	 * @param oldScreen the old screen which the app was in 
	 * @param newScreen the new screen which is the current screen of the App
	 * @return the screen which should be considered as current screen.
	 * Note: the return value can be same as old screen/new screen or any screen object
	 * which the strategy thinks is same as new screen
	 */
	public abstract ViewScreen notifyNewScreen(ViewScreen oldScreen,ViewScreen newScreen);
	
	/***
	 * The method is used to notify the strategy about the initialization of the App and its first screen
	 * @param firstScreen the first screen of the app
	 * @return true/false on sucess and failure respectively
	 */
	public abstract boolean initializeNewScreen(ViewScreen firstScreen);
	
	/**
	 * This method will compare the two screens provided
	 * @param scr1  The first screen that needs to be compared
	 * @param scr2 The 2nd screen that needs to be compared
	 * @return true/false depending on whether they are same or not.
	 */
	public abstract boolean areScreensSame(ViewScreen scr1,ViewScreen scr2);
	
	/***
	 * This method is used to check if coverage need to be collected or not
	 * @return true/false depending on whether the coverage is required or not
	 */
	public abstract boolean needDumpCoverage();
	
	/***
	 * This will do cleanup tasks
	 */
	public abstract void cleanUp();
	
	
	public abstract void addNonUiDeviceAction(Pair<ViewElement,IDeviceAction> action);
	
	public abstract void removeNonUiDeviceAction(Pair<ViewElement,IDeviceAction> action);
	
	/***
	 * This method indicates whether a new folder needs to be created for each
	 * widget that needs to be excised
	 * 	 * 
	 * @return true/false depending on whether a fresh directory is required for each widget
	 */
	public boolean needFreshDirectory() {
		return false;
	}
	
	public boolean reStartStrategy(){
		return true;
	}

}
