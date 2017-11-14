package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.utilities.Logger;


/***
 * 
 * @author machiry
 *
 */
public class ViewScreenFeatures {
	public int absLeft;
	public int absTop;
	public int absHeight;
	public int absWidth;
	public String screenName;
	
	public ViewScreenFeatures(int top,int left,int height,int width,String screenName){
		//assert(screenName != null && screenName.length()>0);
		this.absLeft = left;
		this.absTop = top;
		this.absHeight = height;
		this.absWidth = width;
		this.screenName = screenName;
		if(screenName == null)
		{
			Logger.logError("Some Thing Really Really Wrong");
		}
	}
	
	@Override
	public int hashCode(){
		int tempV = absLeft*3+absHeight*5+absHeight*7+absWidth*11;
		tempV = tempV ^ this.screenName.hashCode();
		return tempV;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof ViewScreenFeatures){
			ViewScreenFeatures that = (ViewScreenFeatures)obj;
			return this.absHeight == that.absHeight && this.absWidth == that.absWidth && this.absTop == that.absTop && this.absLeft == that.absLeft && this.screenName.equals(that.screenName);
		}
		return false;
	}
	
	@Override
	public String toString(){
		String retVal = this.screenName+",T:"+this.absTop+",L:"+this.absLeft+",H:"+this.absHeight+",W:"+this.absWidth+"\n";
		return retVal;
	}
}
