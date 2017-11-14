/**
 * 
 */
package edu.gatech.dynodroid.testHarness;

import java.util.HashMap;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.hierarchyHelper.CaptureAction;
import edu.gatech.dynodroid.hierarchyHelper.DeviceActionPerformer;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;

/**
 * This class manages the picture or snapshot of the views of the current screen.
 *   it maintains the cache of the pictures that were taken.
 *   takes snapshot only when its necessary
 * @author machiry, rohan
 *
 */
public class WidgetPictureManager {
    private String widgetPictureLocation;
    private String screenLocation;
    private HashMap<Integer, String> processedWidgets = new HashMap<Integer, String>();
    private static final int ignoreUniqueID = -1;
    private DeviceActionPerformer performer = null;
	
    public WidgetPictureManager(String widgetLocation, DeviceActionPerformer performer) throws Exception{
        if(widgetLocation != null && !widgetLocation.isEmpty()){
            this.widgetPictureLocation = widgetLocation;
            screenLocation = this.widgetPictureLocation +"/screens";
            FileUtilities.createDirectory(widgetPictureLocation);
            FileUtilities.createDirectory(screenLocation);
            this.performer = performer;
        } else{
            throw new Exception("Invalid Path provided for image storage");
        }
		
    }
	
    public boolean takePicture(ViewElement targetView, ADevice device, int runs){
        boolean retVal = false;
        if(targetView != null){
            retVal = true;
            if(targetView.inScreen != null) {
                takeCompletePicture(targetView.inScreen, device, runs);
            }
            if(targetView.nativeObject.uniqueViewID != ignoreUniqueID && !processedWidgets.containsKey(targetView.nativeObject.uniqueViewID)){
                String fileName = 
                    this.widgetPictureLocation + "/" +
                    Integer.toString(targetView.nativeObject.uniqueViewID) + 
                    (targetView.inScreen == null ? "" : 
                     "_" + runs + targetView.inScreen.hashCode()) + ".png";

                performer.performAction(new CaptureAction(targetView.features.absLeft, 
                                                          targetView.features.absTop,
                                                          targetView.features.absLeft+targetView.features.absWidth,
                                                          targetView.features.absTop+targetView.features.absHeight,
                                                          fileName),
                                        null);
                processedWidgets.put(targetView.nativeObject.uniqueViewID, fileName);
                retVal = true;

            } else if(processedWidgets.containsKey(targetView.nativeObject.uniqueViewID) && targetView.inScreen != null) {
                String origFile = processedWidgets.get(targetView.nativeObject.uniqueViewID);
                String destFile = this.widgetPictureLocation + "/" +
                    Integer.toString(targetView.nativeObject.uniqueViewID) + 
                    (targetView.inScreen == null ? "" : 
                     "_" + runs + targetView.inScreen.hashCode()) + ".png";
                String command = "cp " + origFile + " " + destFile;
                ExecHelper.RunProgram(command, false);
            }
        }
        return retVal;
    }

    private boolean takeCompletePicture(ViewScreen targetScreen, ADevice device, int runs){
        boolean retVal = false;
        if(device != null && targetScreen != null) {
            String fileName = screenLocation + 
                "/" + runs + 
                Integer.toString(targetScreen.hashCode())+".png";
            performer.performAction(new CaptureAction(fileName), null);
            retVal = true;                            
        }
        return retVal;
    }
}