/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.awt.image.BufferedImage;

import edu.gatech.dynodroid.utilities.PsdFile;


/**
 * @author machiry
 *
 */
public abstract class LayoutExtractor {
	
	/***
	 * This method is used to setup Device and other data structures that will be required by 
	 * underlying layout extractor
	 *  
	 * @return true/false on success and failure respectively
	 */
	public abstract boolean setupDevice();
	
	/***
	 * This method is used to get the current focused screen from the device
	 * @param getFullDump this is used to specify whether a full dump of the screen is required or not
	 * full dump includes all properties of child view elements , their call backs and all the events that are registered by this application
	 * @return ViewScreen object that contains the current screen
	 */
	public abstract ViewScreen getCurrentScreen(boolean getFullDump); 
	
	public abstract BufferedImage captureScreenShot(ViewElement targetWidget);
	
	public abstract PsdFile captureCompleteScreenShot(ViewScreen targetScreen);
	
	/***
	 * This method is similar to getCurrentScreen but only addition is that this accepts a config file which 
	 * could contain directions for how to handle the known widgets
	 * @param getFullDump this is used to specify whether a full dump of the screen is required or not
	 * @param viewHandlingConfig the path to the file which contains the directions on how to
	 * Exercise each view
	 * @return ViewScreen object that contains the current screen
	 */
	public abstract ViewScreen getCurrentScreen(boolean getFullDump,String viewHandlingConfig);
	
	
}
