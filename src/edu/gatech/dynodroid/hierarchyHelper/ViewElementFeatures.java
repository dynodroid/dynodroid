package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

/**
 * 
 * @author machiry
 *
 */
public class ViewElementFeatures {
	public int id;
	public ArrayList<IDeviceAction> possibleActions = new ArrayList<IDeviceAction>();
	public boolean isEnabled;
	public int absLeft;
	public int absTop;
	public int absHeight;
	public int absWidth;
	public int centerX;
	public int centerY;
	public String widgetName;
	public float scaling;
	
	public ViewElementFeatures(int id,boolean isEnabled,int top,int left,int height,int width,String name){
		this.id = id;
		this.isEnabled = isEnabled;
		this.absLeft = left;
		this.absTop = top;
		this.absWidth = width;
		this.absHeight = height;
		this.centerY = (this.absTop+(this.absHeight/2));
		this.centerX = (this.absLeft+(this.absWidth/2));
		this.widgetName = name;

	}
	
	@Override
	public int hashCode(){
		int tempV = absLeft*3+absHeight*5+absHeight*7+absWidth*11;
		tempV = tempV ^ this.widgetName.hashCode();
		int actionHashCode = 0;
		for(IDeviceAction c:possibleActions){
			actionHashCode = actionHashCode^c.hashCode();
		}
		return tempV^actionHashCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof ViewElementFeatures){
			ViewElementFeatures that = (ViewElementFeatures)obj;
			return this.widgetName.equals(that.widgetName) && this.scaling == that.scaling && this.possibleActions.containsAll(that.possibleActions) && that.possibleActions.containsAll(this.possibleActions) && this.isEnabled == that.isEnabled && this.absLeft == that.absLeft && this.absTop == that.absTop && this.absHeight == that.absHeight && this.absWidth == that.absWidth;
		}
		return false;
	}
	@Override
	public String toString(){
		String retVal = this.widgetName+",T:"+this.absTop+",L:"+this.absLeft+",H:"+this.absHeight+",W:"+this.absWidth;
		for(int i=0;i<this.possibleActions.size();i++){
			if(i==0){
				retVal += ",";
			}
			if(i == this.possibleActions.size() -1){
				retVal += this.possibleActions.get(i).toString();
			} else{
				retVal += this.possibleActions.get(i).toString()+",";
			}
		}
		return retVal;
	}

}

