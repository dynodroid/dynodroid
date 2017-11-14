package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

import edu.gatech.dynodroid.utilities.Pair;

/**
 * This represents the complete screen..
 * this also includes all teh ViewElements which are children
 * @author machiry
 * 
 */
public class ViewScreen {
	public ViewScreenFeatures features;
	public ArrayList<ViewElement> childWidgets = new ArrayList<ViewElement>();
	public Window nativeObject;

	/***
	 * 
	 * @param attrib
	 */

	public ViewScreen(ViewScreenFeatures attrib, Window no) {
		this.features = attrib;
		this.nativeObject = no;
		this.childWidgets.add(getMenuWidget().getFirst());
	}

	public Pair<ViewElement, IDeviceAction> getBackWidget() {
		ViewElementFeatures featu = new ViewElementFeatures(-1, true, -1, -1,
				-1, -1, "BackButton");
		featu.possibleActions.add(new KeyPressAction(4));
		Pair<ViewElement, IDeviceAction> retVal = new Pair<ViewElement, IDeviceAction>(
				new ViewElement(featu, null, new ViewNode(nativeObject,
						"BackButton", false)), featu.possibleActions.get(0));
		return retVal;
	}

	private Pair<ViewElement, IDeviceAction> getMenuWidget() {
		ViewElementFeatures featu = new ViewElementFeatures(-1, true, -1, -1,
				-1, -1, "MenuButton");
		featu.possibleActions.add(new KeyPressAction(82));
		Pair<ViewElement, IDeviceAction> retVal = new Pair<ViewElement, IDeviceAction>(
				new ViewElement(featu, null, new ViewNode(nativeObject,
						"MenuButton", true)), featu.possibleActions.get(0));
		return retVal;
	}

	public ArrayList<Pair<ViewElement, IDeviceAction>> getAllPossibleActions() {
		ArrayList<Pair<ViewElement, IDeviceAction>> retVal = new ArrayList<Pair<ViewElement, IDeviceAction>>();
		for (ViewElement v : this.childWidgets) {
			for (IDeviceAction a : v.features.possibleActions) {
				retVal.add(new Pair<ViewElement, IDeviceAction>(v, a));
			}
		}
		return retVal;
	}

	@Override
	public int hashCode() {
		int tempV = this.features.hashCode();
		int childHashCode = 0;
		for (ViewElement e : childWidgets) {
			childHashCode = childHashCode ^ e.hashCode();
		}
		tempV = tempV ^ childHashCode;
		return tempV;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewScreen) {
			ViewScreen that = (ViewScreen) obj;
			return this.features.equals(that.features) && this.childWidgets.containsAll(that.childWidgets) && that.childWidgets.containsAll(this.childWidgets);
		}
		return false;
	}

	@Override
	public String toString() {
		return features.toString() + ":NoOfChildren=" + childWidgets.size();
	}

}
