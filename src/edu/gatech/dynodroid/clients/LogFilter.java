package edu.gatech.dynodroid.clients;

import java.util.ArrayList;

public class LogFilter implements Runnable {
	
	private ArrayList<String> entries = null;
	private String filter =null;
	private ArrayList<String> targetOutput=null;
	
	public LogFilter(ArrayList<String> targetLocation,ArrayList<String> toBeFilteredEntries,String filterString){
		this.entries = toBeFilteredEntries;
		this.filter = filterString;
		this.targetOutput = targetLocation;
	}

	@Override
	public void run() {
		ArrayList<String> targetEntries=new ArrayList<String>();
		if(entries != null && filter != null){
			for(String s:entries){
				if(s.contains(filter)){
					targetEntries.add(s);
				}
			}
		} else{
			if(entries != null){
				targetEntries.addAll(entries);
			}
		}
		
		if(targetOutput != null){
			synchronized (targetOutput) {
				targetOutput.addAll(targetEntries);
			}
		}

	}

}
