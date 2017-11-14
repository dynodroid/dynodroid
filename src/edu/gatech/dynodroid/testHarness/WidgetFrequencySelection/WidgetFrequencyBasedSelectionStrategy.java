package edu.gatech.dynodroid.testHarness.WidgetFrequencySelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Pair;
import edu.gatech.dynodroid.utilities.TextLogger;

public class WidgetFrequencyBasedSelectionStrategy extends
		WidgetSelectionStrategy {

	private String currWorkingDir;
	private TextLogger textLogger;
	HashMap<Pair<ViewElement, IDeviceAction>, Integer> costMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
	HashMap<Pair<ViewElement, IDeviceAction>, Integer> frequencyMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
	HashSet<Pair<ViewElement, IDeviceAction>> nonUIActions = new HashSet<Pair<ViewElement, IDeviceAction>>();
	private static final Integer inScreenCost = 0;
	private static final Integer offScreenCost = -1;
	private static final String logPrefix = "FrequencyBased";
	private int totalNumberOfActions = 0;
	private int coverageGranularity = 100;

	public WidgetFrequencyBasedSelectionStrategy(String workingDir,
			int covSamInterval) throws Exception {
		this.currWorkingDir = workingDir;
		FileUtilities.createDirectory(currWorkingDir);
		this.textLogger = new TextLogger(this.currWorkingDir
				+ "/WidgetFrequencyBasedSelection.log");
		if (covSamInterval > 0) {
			this.coverageGranularity = covSamInterval;
		}
	}

	@Override
	public Pair<ViewElement, IDeviceAction> getNextElementAction(
			ViewScreen currScreen,
			Pair<ViewElement, IDeviceAction> lastPerformedAction,
			boolean resultOfLastOperation) {
		if (resultOfLastOperation && lastPerformedAction != null) {
			this.totalNumberOfActions++;
			incrementFrequency(lastPerformedAction);
		}
		Pair<ViewElement, IDeviceAction> retEle = getActionWithMinFrequency(getAllActionsOfCost(inScreenCost));
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
		retActions.addAll(nonUIActions);
		return retActions;
	}

	private synchronized Pair<ViewElement, IDeviceAction> getActionWithMinFrequency(
			ArrayList<Pair<ViewElement, IDeviceAction>> candidateActions) {
		Pair<ViewElement, IDeviceAction> retVal = null;
		synchronized (frequencyMap) {
			if (candidateActions != null && candidateActions.size() > 0) {
				Integer knownMinFreq = Integer.MAX_VALUE;
				for (Pair<ViewElement, IDeviceAction> p : candidateActions) {
					if (!frequencyMap.containsKey(p)) {
						// This is the first widget that we found..
						frequencyMap.put(p, 0);
						retVal = p;
						break;
					}
					if (knownMinFreq > frequencyMap.get(p)) {
						knownMinFreq = frequencyMap.get(p);
						retVal = p;
					}
				}
			}
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
		this.textLogger.logInfo(logPrefix, "WIDGET FREQUENCY STRATEGY DUMP");
		this.textLogger.logInfo(logPrefix, "WIDGET FREQUENCY DUMP");
		for (Pair<ViewElement, IDeviceAction> p : frequencyMap.keySet()) {
			this.textLogger.logInfo(logPrefix, "Element:" + p.toString()
					+ " Frequency:" + frequencyMap.get(p));
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
			synchronized (nonUIActions) {
				nonUIActions.add(action);
			}
		}

	}

	@Override
	public void removeNonUiDeviceAction(Pair<ViewElement, IDeviceAction> action) {
		if (action != null) {
			synchronized (nonUIActions) {
				nonUIActions.remove(action);
			}
		}
	}

}
