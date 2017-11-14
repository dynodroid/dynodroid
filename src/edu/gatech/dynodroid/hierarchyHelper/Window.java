package edu.gatech.dynodroid.hierarchyHelper;

import edu.gatech.dynodroid.devHandler.ADevice;

/***
 * The Code for This class has been copied form Android Open Source Location
 * 
 * @author Android Open Source Community
 * 
 */
public class Window {
	private String mTitle = "NoTitle";
	private int mHashCode;
	private ADevice targetDevice;

	public Window(ADevice paramIDevice, String paramString, int paramInt) {
		this.targetDevice = paramIDevice;
		if (paramString != null && !paramString.isEmpty()) {
			this.mTitle = paramString;
		}
		this.mHashCode = paramInt;
	}

	public String getTitle() {
		return this.mTitle;
	}

	public int getHashCode() {
		return this.mHashCode;
	}

	public String encode() {
		return Integer.toHexString(this.mHashCode);
	}

	@Override
	public String toString() {
		return this.mTitle;
	}

	public ADevice getDevice() {
		return this.targetDevice;
	}

	public static Window getFocusedWindow(ADevice paramIDevice) {
		return new Window(paramIDevice, "<Focused Window>", -1);
	}

	@Override
	public int hashCode() {
		return this.mHashCode;
	}

	@Override
	public boolean equals(Object paramObject) {
		if ((paramObject instanceof Window)) {
			return (this.mHashCode == ((Window) paramObject).mHashCode)
					&& (this.targetDevice.getDeviceName()
							.equals(((Window) paramObject).targetDevice
									.getDeviceName()));
		}

		return false;
	}
}
