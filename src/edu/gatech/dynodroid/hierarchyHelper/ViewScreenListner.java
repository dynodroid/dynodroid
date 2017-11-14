package edu.gatech.dynodroid.hierarchyHelper;

/**
 * @author machiry
 *
 */
public class ViewScreenListner {
	public ViewScreenAction event;
	public String registeredCallBack;
	
	public ViewScreenListner(String targetEvent,String callbackName){
		this.event = ViewScreenAction.formString(targetEvent);
		this.registeredCallBack = callbackName;
	}
	
	@Override
	public int hashCode(){
		return registeredCallBack.hashCode() ^ event.toString().hashCode();
	}
	
	@Override
	public String toString(){
		return event.toString()+"->" +registeredCallBack;
	}
}
