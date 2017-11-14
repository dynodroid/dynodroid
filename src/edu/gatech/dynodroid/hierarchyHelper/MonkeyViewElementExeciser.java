/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.MonkeyTraceLogger;
import edu.gatech.dynodroid.utilities.TraceLogger;

/**
 * @author machiry
 * 
 */
public class MonkeyViewElementExeciser extends ViewElementExeciser {

	private TraceLogger monkeyTrace;
	public static final String monkeyElementExeciser = "monkey_elem_execiser";
	
	public MonkeyViewElementExeciser(TraceLogger log){
		assert(log!=null);
		this.monkeyTrace = log;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.hierarchyHelper.ViewElementExeciser#execiseElement(edu.
	 * gatech.m3.hierarchyHelper.ViewElement,
	 * edu.gatech.m3.hierarchyHelper.ViewElementAction)
	 */
	@Override
	public boolean execiseElement(ADevice targetDevice,ViewElement targetElement,
			ViewElementAction actionTobePerformed) {
		boolean handlingSucessfull = true;
		switch (actionTobePerformed) {
		case OnClick:
			break;
		case OnLongClick:
			break;
		case OnSlide:
			break;
		case BackButton:
			break;
		case MenuButton:
			break;
		}
		return handlingSucessfull;
	}
	
	@Override
	public boolean endTracing() {
		if(this.monkeyTrace != null){
			this.monkeyTrace.addTraceData(MonkeyTraceLogger.commentCategory, "Ending Tracing Data");
			this.monkeyTrace.endTraceLog();
			return true;
		}
		return false;
	}

}
