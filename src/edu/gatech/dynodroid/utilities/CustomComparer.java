package edu.gatech.dynodroid.utilities;

import java.util.Comparator;

public class CustomComparer implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {
		if(arg0.split("_").length != 2) {
			return 1;
		}
		if(arg1.split("_").length != 2) {
			return -1;
		}
		int arg0Num = Integer.parseInt(arg0.split("_")[1].split("\\.")[0]);
		
		int arg1Num = Integer.parseInt(arg1.split("_")[1].split("\\.")[0]);
		
		return arg0Num > arg1Num ? 1:(arg0Num == arg1Num ? 0 : -1);
		
	}

}