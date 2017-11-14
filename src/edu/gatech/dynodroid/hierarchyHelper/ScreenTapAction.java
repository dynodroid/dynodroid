package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

/***
 * Screen Tap Action
 * @author machiry
 *
 */
public class ScreenTapAction implements IDeviceAction {
	
	public int x_cord=0;
	public int y_cord=0;
	public String callBackName=null;
	private static final String actionNamePrefix = "TAPSCREEN_";
	
	public ScreenTapAction(int x,int y){
		assert(x>0 && y>0);
		this.x_cord = x;
		this.y_cord = y;
	}
	
	public ScreenTapAction(int x,int y,String callB){
		assert(x>0 && y>0);
		this.x_cord = x;
		this.y_cord = y;
		this.callBackName = callB;
	}

	@Override
	public ArrayList<String> getMonkeyCommand() {
		ArrayList<String> retVal = new ArrayList<String>();
		retVal.add("Tap("+x_cord+","+y_cord+")");
		return retVal;
	}

	@Override
	public String actionName() {
		// TODO Auto-generated method stub
		return actionNamePrefix+x_cord+"_"+y_cord;
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
		return retVal ^ x_cord ^ y_cord ^ "TAP".hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof ScreenTapAction){
			ScreenTapAction that = (ScreenTapAction)obj;
			if(this.callBackName == null && that.callBackName==null){
				return this.x_cord == that.x_cord && this.y_cord == that.y_cord;
			}
			if(this.callBackName != null && that.callBackName != null){
				return this.x_cord == that.x_cord && this.y_cord == that.y_cord && this.callBackName.equals(that.callBackName);
			}
		}
		return false;
	}
	@Override
	public String toString(){
		return actionName();
	}

}
