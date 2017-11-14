package edu.gatech.dynodroid.hierarchyHelper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ViewConfigurationHandler {

	/***
	 * This method parses the config file for handling different views and
	 * returns the string corresponding to the given View element
	 * 
	 * @param v
	 *            the target view element for which we want input text
	 * @param configFile
	 *            config file containing information about view handling
	 * @return target string of the text we need to input to the view
	 */
	public static String getInputText(ViewNode v,String viewHandlingConfig){
		String targetText = "";
		try{			
			FileInputStream fstream = new FileInputStream(viewHandlingConfig);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] configLineParts = strLine.split(":");
				//This is just to ensure that the config file has entries in correct format
				if(configLineParts.length > 1){
					int targetViewID = Integer.parseInt(configLineParts[0]);
					if(targetViewID == 0){
						targetText = joinString(configLineParts, ':',1);
					}
					if(targetViewID == v.uniqueViewID){
						targetText = joinString(configLineParts, ':',1);
						break;
					}
				}
			}
			in.close();
		} catch(Exception e){
			//Ignore Any exceptions while checking for existance of the file
		}
		
		return targetText;
	}

	private static String joinString(String[] parts, char joinChar,int startIndex) {
		try {
			String targetString = parts[startIndex];
			for (int i = startIndex+1; i < parts.length; i++) {
				targetString = targetString + joinChar + parts[i];
			}
			return targetString;
		} catch (Exception e) {
			//Ignore the exception here we dont care
		}
		return null;
	}

}
