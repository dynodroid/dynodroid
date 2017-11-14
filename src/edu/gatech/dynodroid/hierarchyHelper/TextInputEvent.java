package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;

import edu.gatech.dynodroid.utilities.Logger;

public class TextInputEvent implements IDeviceAction {

	int x_cord = 0;
	int y_cord = 0;
	String textToEnter = "randomText";
        String actionNamePrefix = "Input:";

	public TextInputEvent(int x, int y, String inputText) {
		assert (x > 0 && y > 0);
		this.x_cord = x;
		this.y_cord = y;
		this.textToEnter = inputText;
	}

	@Override
	public ArrayList<String> getMonkeyCommand() {
            ArrayList<String> retVal = new ArrayList<String>();
            if (this.textToEnter != null && !this.textToEnter.isEmpty()) {
                retVal.add("Tap("+this.x_cord+","+this.y_cord+")");
                retVal.add("Input:"+this.textToEnter);
            } else {
                Logger.logError("Invalid parameters , TextInputAction will not be performed");
            }
            return retVal;
	}

	@Override
	public String actionName() {
		// TODO Auto-generated method stub
		return actionNamePrefix+x_cord+"_"+y_cord;
	}

	@Override
	public String toString() {
		return "TextInput_X:" + this.x_cord + ",Y:" + this.y_cord + ",Input:"
				+ textToEnter;
	}

	@Override
	public String getCallBackName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		return this.x_cord ^ this.y_cord ^ this.textToEnter.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TextInputEvent) {
			TextInputEvent that = (TextInputEvent) o;
			return this.x_cord == that.x_cord && this.y_cord == that.y_cord
					&& this.textToEnter.equals(that.textToEnter);
		}
		return false;
	}

}
