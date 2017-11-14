package edu.gatech.dynodroid.appHandler;

import java.util.ArrayList;

public class IntentFilter {
	public ArrayList<String> intentActions = new ArrayList<String>();
	public ArrayList<String> intentCategory = new ArrayList<String>();
	
	@Override
	public String toString(){
		String retVal = null;
		for(String s:intentActions){
			if(retVal == null){
				retVal = "Actions:";
			}
			retVal += ","+s;
		}
		if(retVal == null){
			retVal = "No Actions";
		}
		retVal += ";";
		if(intentCategory.size() == 0){
			retVal += "No Categories";
		} else{
			retVal += "Categories:";
		}
		for(String s:intentCategory){
			retVal += ","+s;
		}
		return retVal;
		
	}
}
