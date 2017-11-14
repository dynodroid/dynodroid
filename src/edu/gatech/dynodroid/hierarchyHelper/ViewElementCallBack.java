package edu.gatech.dynodroid.hierarchyHelper;

public class ViewElementCallBack {
	public ViewElementAction action;
	public String callBackInfo;
	
	public ViewElementCallBack(String actionString,String callBackInfo){
		assert((this.action=ViewElementAction.fromString(actionString))!=null);
		this.callBackInfo = callBackInfo;		
	}
	
	@Override
	public int hashCode(){
		return callBackInfo.hashCode() ^ action.toString().hashCode();
	}
	
	@Override
	public String toString(){
		return action.toString()+"->" +callBackInfo;
	}
}
