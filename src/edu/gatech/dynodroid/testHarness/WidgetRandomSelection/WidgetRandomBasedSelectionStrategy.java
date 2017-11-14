/**
 * 
 */
package edu.gatech.dynodroid.testHarness.WidgetRandomSelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Pair;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author machiry
 * 
 */
public class WidgetRandomBasedSelectionStrategy extends WidgetSelectionStrategy {

	private String currWorkingDir;
	private TextLogger textLogger;
	HashMap<Pair<ViewElement, IDeviceAction>, Integer> costMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
	HashMap<Pair<ViewElement, IDeviceAction>, Integer> frequencyMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
	HashSet<Pair<ViewElement, IDeviceAction>> nonUIEvents = new HashSet<Pair<ViewElement, IDeviceAction>>();
	private static final Integer inScreenCost = 0;
	private static final Integer offScreenCost = -1;
	private static final String logPrefix = "RandomBased";
	private Random randomNumber;
	private int totalNumberOfActions = 0;
	private int coverageGranularity = 100;

	public WidgetRandomBasedSelectionStrategy(String workingDir,
			int samplingInterval) throws Exception {
		this.currWorkingDir = workingDir;
		FileUtilities.createDirectory(currWorkingDir);
		this.textLogger = new TextLogger(this.currWorkingDir
				+ "/WidgetRandomBasedSelection.log");
		this.randomNumber = new Random();
		if (samplingInterval > 0) {
			this.coverageGranularity = samplingInterval;
		}
	}

	@Override
	public Pair<ViewElement, IDeviceAction> getNextElementAction(
			ViewScreen currScreen,
			Pair<ViewElement, IDeviceAction> lastPerformedAction,
			boolean resultOfLastOperation) {
		if (resultOfLastOperation && lastPerformedAction != null) {
			incrementFrequency(lastPerformedAction);
			totalNumberOfActions++;
		}
		Pair<ViewElement, IDeviceAction> retEle = getRandomAction(getAllActionsOfCost(inScreenCost));
		this.textLogger.logInfo(logPrefix, "Returning Next Device Action as:"
				+ (retEle == null ? "NULL" : retEle.toString()));
		return retEle;
	}

	@Override
	public ViewScreen notifyNewScreen(ViewScreen oldScreen, ViewScreen newScreen) {
		setCost(oldScreen, offScreenCost);
		setCost(newScreen, inScreenCost);
		this.textLogger.logInfo(logPrefix, "New Screen Notified."
				+ " Old Screen=" + oldScreen.toString() + ", New Screen="
				+ newScreen.toString());
		return newScreen;
	}

	private synchronized ArrayList<Pair<ViewElement, IDeviceAction>> getAllActionsOfCost(
			Integer targetCost) {
		ArrayList<Pair<ViewElement, IDeviceAction>> retActions = new ArrayList<Pair<ViewElement, IDeviceAction>>();
		for (Pair<ViewElement, IDeviceAction> p : costMap.keySet()) {
			if (costMap.get(p).equals(targetCost)) {
				retActions.add(p);
			}
		}
		// These nonUIevents can be triggered any time
		synchronized (nonUIEvents) {
			retActions.addAll(nonUIEvents);
		}
		return retActions;
	}

	private synchronized Pair<ViewElement, IDeviceAction> getRandomAction(
			ArrayList<Pair<ViewElement, IDeviceAction>> candidateActions) {
		Pair<ViewElement, IDeviceAction> retVal = null;
		if (candidateActions != null && candidateActions.size() > 0) {
			int rand = this.randomNumber.nextInt(candidateActions.size());
			if (rand >= candidateActions.size()) {
				rand = candidateActions.size() - 1;
			}
			retVal = candidateActions.get(rand);
		}
		return retVal;
	}

	private synchronized void setCost(ViewScreen targetScreen, Integer newCost) {
		if (targetScreen != null) {
			ArrayList<Pair<ViewElement, IDeviceAction>> allAction = targetScreen
					.getAllPossibleActions();
			for (Pair<ViewElement, IDeviceAction> p : allAction) {
				if (costMap.containsKey(p)) {
					costMap.remove(p);
				}
				costMap.put(p, newCost);
			}
		}
	}

	private synchronized void incrementFrequency(
			Pair<ViewElement, IDeviceAction> targetEle) {
		if (targetEle != null) {
			Integer oldFrequency = 0;
			if (frequencyMap.containsKey(targetEle)) {
				oldFrequency = frequencyMap.get(targetEle);
				frequencyMap.remove(targetEle);
				oldFrequency++;
			}
			frequencyMap.put(targetEle, oldFrequency);
		}
	}

	@Override
	public boolean initializeNewScreen(ViewScreen firstScreen) {
		setCost(firstScreen, inScreenCost);
		return true;
	}

	@Override
	public boolean areScreensSame(ViewScreen scr1, ViewScreen scr2) {
		if (scr1 != null && scr2 != null) {
			return scr1.equals(scr2);
		}
		return scr1 == null && scr2 == null;
	}

	@Override
	public boolean needDumpCoverage() {
		return ((this.totalNumberOfActions % this.coverageGranularity) == 0);
	}

	@Override
	public void cleanUp() {
		this.textLogger.logInfo(logPrefix, "WIDGET RANDOM STRATEGY DUMP");
		this.textLogger.logInfo(logPrefix, "WIDGET COUNT DUMP");
		for (Pair<ViewElement, IDeviceAction> p : frequencyMap.keySet()) {
			this.textLogger.logInfo(logPrefix, "Element:" + p.toString()
					+ " Count:" + frequencyMap.get(p));
		}
		this.textLogger.logInfo(logPrefix, "WIDGET COST DUMP");
		for (Pair<ViewElement, IDeviceAction> p : costMap.keySet()) {
			this.textLogger.logInfo(logPrefix, "Element:" + p.toString()
					+ " Cost:" + costMap.get(p));
		}
		this.textLogger.endLog();
	}

	@Override
	public boolean needFreshDirectory() {
		return needDumpCoverage();
	}

	@Override
	public void addNonUiDeviceAction(Pair<ViewElement, IDeviceAction> action) {
		if (action != null) {
			synchronized (nonUIEvents) {
				this.nonUIEvents.add(action);
			}
		}
	}

	@Override
	public void removeNonUiDeviceAction(Pair<ViewElement, IDeviceAction> action) {
		if (action != null) {
			synchronized (nonUIEvents) {
				this.nonUIEvents.remove(action);
			}
		}
	}

	public boolean reStartStrategy() {
		try {
			synchronized (nonUIEvents) {
				nonUIEvents.clear();
			}
			return true;
		} catch (Exception e) {
			this.textLogger.logException(logPrefix, e);
		}
		return false;
	}

}
