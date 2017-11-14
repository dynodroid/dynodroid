package edu.gatech.dynodroid.methodTracing;

import java.util.ArrayList;

import com.android.traceview.DmTraceReader;
import com.android.traceview.MethodData;

import edu.gatech.dynodroid.utilities.Logger;

public class TracefileParser {
	
	public static ArrayList<String> getCoveredMethods(String filePath) {
		ArrayList<String> coveredMethods = new ArrayList<String>();
		if (filePath != null) {
			try {
				DmTraceReader read = new DmTraceReader(filePath, false);
				MethodData[] methods = read.getMethods();
				for (MethodData m : methods) {					
					m.computeProfileName();
					coveredMethods.add(m.getName());
				}
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return coveredMethods;
	}
}
