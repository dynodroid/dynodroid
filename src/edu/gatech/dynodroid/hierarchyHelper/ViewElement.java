package edu.gatech.dynodroid.hierarchyHelper;


/***
 * This represents a widget on the screen
 * 
 * @author machiry
 * 
 */
public class ViewElement {
	public ViewElementFeatures features;
	public ViewScreen inScreen;
	public ViewNode nativeObject;

	public ViewElement(ViewElementFeatures attributes, ViewScreen parent,
			ViewNode no) {
		assert (parent != null);
		this.features = attributes;
		this.inScreen = parent;
		if (this.inScreen != null) {
			this.inScreen.childWidgets.add(this);
		}
		this.nativeObject = no;
	}

	@Override
	public int hashCode() {
		int val = this.features.hashCode();
		return val;
	}

	public static boolean isBackButton(IDeviceAction action){
		if(action instanceof KeyPressAction){
			KeyPressAction keyAc = (KeyPressAction)action;
			return keyAc.keyCode == 4;
		}
		return false;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewElement) {
			ViewElement that = (ViewElement) obj;
			return this.features.equals(that.features);
		}
		return false;
	}

	@Override
	public String toString() {
		if(nativeObject != null){
			return nativeObject.uniqueViewID+(inScreen == null ? "":"_0"+inScreen.hashCode()) +":"+features.toString();
		}
		return "-1"+":"+features.toString();
	}

}
