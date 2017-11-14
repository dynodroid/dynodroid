package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;
import java.util.Random;

/***
 * Gesture Action
 * @author machiry
 *
 */
public class GestureAction implements IDeviceAction {
	
    public int x1_cord=0;
    public int y1_cord=0;
    public int x2_cord=0;
    public int y2_cord=0;
    public int width = 0;
    public int height = 0;
    public String callBackName=null;
    private static final String actionNamePrefix = "GESTURE_";
    private static final Random random = new Random();
	
    public GestureAction(int x1,int y1, int x2,int y2){
        assert(x1>0 && y1>0 && x2>0 && y2>0);
        this.x1_cord = x1;
        this.y1_cord = y1;
        this.x2_cord = x2;
        this.width = x2-x1 <= 0?5:x2-x1;
        this.y2_cord = y2;
        this.height = y2-y1 <=0?5:y2-y1;
    }
	
    public GestureAction(int x1,int y1,int x2,int y2,String callB){
        assert(x1>0 && y1>0 && x2>0 && y2>0);
        this.x1_cord = x1;
        this.y1_cord = y1;
        this.x2_cord = x2;
        this.y2_cord = y2;
        this.callBackName = callB;
    }

    @Override
	public ArrayList<String> getMonkeyCommand() {
        ArrayList<String> retVal = new ArrayList<String>();
        int x1, y1, x2, y2;
        switch(random.nextInt(4)) {
        case 0:
            x1 = this.x1_cord + (width/2);
            y1 = this.y1_cord + 5;
            x2 = this.x1_cord + (width/2);
            y2 = this.y2_cord - 5;
            break;
        case 1:
            x1 = this.x1_cord + (width/2);
            y1 = this.y2_cord - 5;
            x2 = this.x1_cord + (width/2);
            y2 = this.y1_cord + 5;
            break;
        case 2:
            x1 = this.x1_cord + 5;
            y1 = this.y1_cord + (height/2);
            x2 = this.x2_cord - 5;
            y2 = this.y1_cord + (height/2);
            break;
        case 3:
        default:
            x1 = this.x2_cord - 5;
            y1 = this.y1_cord + (height/2);
            x2 = this.x1_cord + 5;
            y2 = this.y1_cord + (height/2);            
            break;
        }
        retVal.add("Slide("+x1+","+y1+","+x2+","+y2+")");
        return retVal;
    }

    @Override
	public String actionName() {
        // TODO Auto-generated method stub
        return actionNamePrefix+x1_cord+"_"+y1_cord+"_"+x2_cord+"_"+y2_cord;
    }

    @Override
	public String getCallBackName() {
        return this.callBackName;
    }
	
    @Override
	public int hashCode(){
        int retVal = 0;
        if(this.callBackName != null){
            retVal = this.callBackName.hashCode();
        }
        return retVal ^ x1_cord ^ y1_cord ^ x2_cord ^ y2_cord ^ "GESTURE".hashCode();
    }
	
    @Override
	public boolean equals(Object obj){
        if(obj instanceof GestureAction){
            GestureAction that = (GestureAction)obj;
            if(this.callBackName == null && that.callBackName==null){
                return this.x1_cord == that.x1_cord && this.y1_cord == that.y1_cord &&
                    this.x2_cord == that.x2_cord && this.y2_cord == that.y2_cord;
            }
            if(this.callBackName != null && that.callBackName != null){
                return this.x1_cord == that.x1_cord && this.y1_cord == that.y1_cord &&
                    this.x2_cord == that.x2_cord && this.y2_cord == that.y2_cord &&
                    this.callBackName.equals(that.callBackName);
            }
        }
        return false;
    }
    @Override
	public String toString(){
        return actionName();
    }

}
