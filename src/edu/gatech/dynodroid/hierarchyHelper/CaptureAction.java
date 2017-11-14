package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

/***
 * Widget/Screen Capture Action
 * @author rohan
 *
 */
public class CaptureAction implements IDeviceAction {
	
    public int x1=0;
    public int y1=0;
    public int x2=0;
    public int y2=0;
    public String fileName=null;   
    private static final String actionNamePrefix = "Snap_";
	
    public CaptureAction(String fileName){
        x1 = -1;
        x2 = -1;
        y1 = -1;
        y2 = -1;
        this.fileName = fileName;
    }
	
    public CaptureAction(int x1,int y1, int x2, int y2, String fileName){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.fileName = fileName;
    }

    @Override
    public ArrayList<String> getMonkeyCommand() {
        ArrayList<String> retVal = new ArrayList<String>();
        if(x1 > -1 && x2 > -1 && y1 > -1 && y2 > -1) {
            retVal.add("Snap("+x1+","+y1+","+x2+","+y2+"):"+this.fileName);
        } else {
            retVal.add("Snap():"+this.fileName);
        }
        return retVal;
    }

    @Override
    public String actionName() {
        // TODO Auto-generated method stub
        return actionNamePrefix+this.fileName;
    }

    @Override
    public String getCallBackName() {
        return null;
    }
	
    @Override
    public int hashCode(){
        int retVal = 0;
        return retVal ^ x1 ^ y1 ^ x2 ^ y2 ^ ("TAP" + this.fileName).hashCode();
    }
	
    @Override
    public boolean equals(Object obj){
        if(obj instanceof CaptureAction){
            CaptureAction that = (CaptureAction)obj;
            return this.x1 == that.x1 && this.y1 == that.y1 && this.x2 == that.x2 && this.y2 == that.y2 && this.fileName.equals(that.fileName);
        }
        return false;
    }
    @Override
    public String toString(){
        return actionName();
    }

}
