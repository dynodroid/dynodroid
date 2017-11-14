/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.util.HashMap;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * @author machiry
 * 
 */
public class LayoutExtractorFactory {

	public static final String hierarchyLayoutExtractorType = "hvLayout";
	private static HashMap<ADevice, LayoutExtractor> layoutExtractorCache = new HashMap<ADevice, LayoutExtractor>();
	
	public static synchronized LayoutExtractor getLayoutExtractor(String type,ADevice targetDevice,int hostPortNumber) {
		LayoutExtractor retVal = null;
		assert(type != null);
		if(type.equals(hierarchyLayoutExtractorType)){
			if(!layoutExtractorCache.containsKey(targetDevice)){
				retVal = new ViewServerLayoutExtractor(targetDevice,hostPortNumber);
				layoutExtractorCache.put(targetDevice, retVal);
				Logger.logInfo("View Server Object Newly Created");
			} else{
				retVal = layoutExtractorCache.get(targetDevice);
				Logger.logInfo("Old View Server Object Found");
			}
			
		}
		return retVal;
	}
	
	public static void  deleteCache(ADevice targetDevice) {
		if(targetDevice != null){
			synchronized (layoutExtractorCache) {
				if(layoutExtractorCache.containsKey(targetDevice)){
					layoutExtractorCache.remove(targetDevice);
				}
			}
		}
	}
}
