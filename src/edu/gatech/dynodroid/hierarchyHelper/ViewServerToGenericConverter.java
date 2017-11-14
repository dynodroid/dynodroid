package edu.gatech.dynodroid.hierarchyHelper;

import java.io.File;
import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.DeviceEmulator;
import edu.gatech.dynodroid.master.PropertyParser;

/***
 * This class converts the objects returned by ViewServerInteraction layer to a
 * generic format
 * 
 * @author machiry
 * 
 */
public class ViewServerToGenericConverter {

	public static ViewScreen convertWindowToViewScreen(Window srcWindow) {
		ViewScreenFeatures features = new ViewScreenFeatures(0, 0, 800, 480,
				srcWindow.getTitle());
		return new ViewScreen(features, srcWindow);
	}

	public static ArrayList<ViewElement> convertViewNodeToViewElementList(
			ViewNode srcNode, ViewScreen parentScreen) {
		ArrayList<ViewNode> allChildViewNodes = new ArrayList<ViewNode>();
		getAllChildren(srcNode, allChildViewNodes);
		ArrayList<ViewElement> childViewElements = new ArrayList<ViewElement>();
		for (ViewNode v : allChildViewNodes) {
			ViewElementFeatures features = new ViewElementFeatures(v.index,
					v.isClickable, v.realTop, v.realLeft, v.height, v.width,
					v.id);
			features.scaling = v.appScale;
			if (v.isEnabled) {
				boolean isTextBox = false;
				if (isViewTextInput(v)) {
					IDeviceAction action = new TextInputEvent(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale),
							getInputText(v));
					features.possibleActions.add(action);
					isTextBox = true;
				}
				if (!isTextBox && isViewTappable(v)) {
					IDeviceAction action = new ScreenTapAction(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale));
					features.possibleActions.add(action);
				}
				if (isViewLongClikable(v)) {
					IDeviceAction action = new ScreenLongTapAction(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale));
					features.possibleActions.add(action);
				}
				if (isGestureAccepted(v)) {
					IDeviceAction action = new GestureAction(v.realLeft,
							v.realTop, v.realLeft + v.width, v.realTop
									+ v.height);
					features.possibleActions.add(action);
				}
				if (needManualInteraction(v)) {
					try {
						features.possibleActions.add(new ManualExecising(v));
					} catch (Exception e) {

					}
				}
			}
			ViewElement targetElement = new ViewElement(features, parentScreen,
					v);
			childViewElements.add(targetElement);
			parentScreen.childWidgets.add(targetElement);
		}
		return childViewElements;
	}

	// TODO: need to change this and add more flesh here to handle the text
	// boxes
	public static ArrayList<ViewElement> convertViewNodeToViewElementList(
			ViewNode srcNode, ViewScreen parentScreen, String viewHandlingConfig) {
		boolean isconfigFilePresent = false;
		// Logger.logInfo("Got Text File:"+viewHandlingConfig);
		try {
			File configFile = new File(viewHandlingConfig);
			isconfigFilePresent = configFile.exists();
		} catch (Exception e) {
			// Ignore Any exceptions while checking for existance of the file
		}

		// if view handling file is not present then continue with the old logic
		if (!isconfigFilePresent) {
			return convertViewNodeToViewElementList(srcNode, parentScreen);
		}

		ArrayList<ViewNode> allChildViewNodes = new ArrayList<ViewNode>();
		getAllChildren(srcNode, allChildViewNodes);
		ArrayList<ViewElement> childViewElements = new ArrayList<ViewElement>();
		for (ViewNode v : allChildViewNodes) {
			ViewElementFeatures features = new ViewElementFeatures(v.index,
					v.isClickable, v.realTop, v.realLeft, v.height, v.width,
					v.id);
			features.scaling = v.appScale;
			if (v.isEnabled) {
				boolean isTextBox = false;
				if (isViewTextInput(v)) {
					IDeviceAction action = new TextInputEvent(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale),
							getInputText(v, viewHandlingConfig));
					features.possibleActions.add(action);
					isTextBox = true;
				}
				if (!isTextBox && isViewTappable(v)) {
					IDeviceAction action = new ScreenTapAction(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale));
					features.possibleActions.add(action);
				}
				if (isViewLongClikable(v)) {
					IDeviceAction action = new ScreenLongTapAction(
							((int) ((v.realLeft + (v.width / 2)) * v.appScale)),
							(int) ((v.realTop + (v.height / 2)) * v.appScale));
					features.possibleActions.add(action);
				}
				if (isGestureAccepted(v)) {
					IDeviceAction action = new GestureAction(v.realLeft,
							v.realTop, v.realLeft + v.width, v.realTop
									+ v.height);
					features.possibleActions.add(action);
				}
				if (needManualInteraction(v)) {
					try {
						features.possibleActions.add(new ManualExecising(v));
					} catch (Exception e) {

					}
				}
			}
			ViewElement targetElement = new ViewElement(features, parentScreen,
					v);
			childViewElements.add(targetElement);
			parentScreen.childWidgets.add(targetElement);
		}
		return childViewElements;
	}

	private static boolean isViewTappable(ViewNode v) {
		if (v != null) {
			return (v.onClickCallBackFound || v.m3Clickable) && v.width != 0
					&& v.height != 0;
		}
		return false;
	}

	private static boolean needManualInteraction(ViewNode v) {
		/*if (v != null) {
			return PropertyParser.isManualMode && v.needManualInteraction;
		}*/
		return false;
	}

	private static boolean isGestureAccepted(ViewNode v) {
		if (v != null) {
			return (v.width >= 70 || v.height >= 100) && v.canAcceptGestures
					&& !isViewTextInput(v);
		}
		return false;
	}

	private static String getInputText(ViewNode v) {
		return "randomText";
	}

	private static String getInputText(ViewNode v, String viewHandlingConfig) {
		String text = ViewConfigurationHandler.getInputText(v,
				viewHandlingConfig);
		// Logger.logInfo("Returning Text:"+text);
		if (text.equals("")) {
			text = "RandomText";
		}
		return text;
	}

	private static boolean isViewLongClikable(ViewNode v) {
		if (v != null) {
			return (v.isLongClickable && v.width != 0 && v.height != 0);
		}
		return false;
	}

	private static boolean isViewTextInput(ViewNode v) {
		if (v != null) {
			return v.textBoxText != null && v.isClickable;
		}
		return false;
	}

	private static ArrayList<ViewNode> getAllChildren(ViewNode topParent,
			ArrayList<ViewNode> children) {
		if (topParent != null) {
			if (topParent.children == null || topParent.children.size() == 0) {
				if (isStartValid(topParent) && isDiemensionValid(topParent)) {
					children.add(topParent);
				}
			} else {
				for (ViewNode child : topParent.children) {
					getAllChildren(child, children);
				}
			}
			return children;
		}
		return children;
	}

	private static boolean isDiemensionValid(ViewNode v) {
		if (v.width > 0 && v.height > 0) {
			return (v.realLeft + v.width) <= DeviceEmulator.maxEmulatorWidth
					&& (v.realTop + v.height) <= DeviceEmulator.maxEmulatorHeight;
		}
		return false;
	}

	private static boolean isStartValid(ViewNode v) {
		return v.realLeft >= 0 && v.realLeft < DeviceEmulator.maxEmulatorWidth
				&& v.realTop >= 0
				&& v.realTop < DeviceEmulator.maxEmulatorHeight;
	}

}
