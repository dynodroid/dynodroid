/**
 * 
 */
package edu.gatech.dynodroid.testHarness.ScreenBasedGraphAbstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreenFeatures;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.Pair;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author machiry
 * 
 */
public class GraphBasedSelectionStrategy extends WidgetSelectionStrategy {

	private Logger textLogger;
	private String currWorkingDir;
	private ScreenGraph targetScreenGraph;
	private static final String LogPrefix = "GraphBasedSelectionStrategy";
	private ViewScreen startScreen = null;

	private HashMap<ViewScreen, HashSet<Pair<ViewElement, IDeviceAction>>> screenVisitedNodes = new HashMap<ViewScreen, HashSet<Pair<ViewElement, IDeviceAction>>>();
	private ArrayList<ViewScreen> completlyVisitedNodes = new ArrayList<ViewScreen>();
	private ArrayList<Pair<ViewElement, IDeviceAction>> currentTraversedPath = new ArrayList<Pair<ViewElement, IDeviceAction>>();

	// These are required to maintain the traversing of long path
	private boolean isPathTraversing = false;
	private ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> totalPathToUnvisitedScreen = new ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>>();
	private int currIndUnVisScreen = 0;
	private ArrayList<Pair<ViewElement, IDeviceAction>> totalPathToBeTraversed = new ArrayList<Pair<ViewElement, IDeviceAction>>();
	private ArrayList<Pair<ViewElement, IDeviceAction>> currentSelectedPath = new ArrayList<Pair<ViewElement, IDeviceAction>>();
	private int eleIndexInCurrentSelectedPath = 0;
	private ViewScreen nextExpectedScreen = null;
	private ViewScreen finalTargetExpectedScreen = null;
	// These are the nodes/screens that doesn't obey the previously known
	// traversed path
	private HashSet<ViewScreen> blackListedNodes = new HashSet<ViewScreen>();
	private ViewScreen currentFocusedScreen = null;
	private int totalNumberOfActions = 0;
	private int coverageGranularity = 100;

	public GraphBasedSelectionStrategy(String workingDir,int covSamInterval) throws Exception {
		this.currWorkingDir = workingDir;
		FileUtilities.createDirectory(currWorkingDir);
		this.textLogger = new TextLogger(this.currWorkingDir
				+ "/GraphBasedSelection.log");
		this.targetScreenGraph = new ScreenGraph();
		if(covSamInterval > 0){
			this.coverageGranularity = covSamInterval;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy#getNextElementAction
	 * (edu.gatech.dynodroid.hierarchyHelper.ViewScreen,
	 * edu.gatech.dynodroid.hierarchyHelper.ViewElement,
	 * edu.gatech.dynodroid.hierarchyHelper.ViewElementAction, boolean)
	 */
	@Override
	public synchronized Pair<ViewElement, IDeviceAction> getNextElementAction(
			ViewScreen currScreen,
			Pair<ViewElement, IDeviceAction> lastActionPerformed,
			boolean resultOfLastOperation) {

		if (isPathTraversing) {
			if (eleIndexInCurrentSelectedPath < (this.currentSelectedPath
					.size() - 1)) {
				this.eleIndexInCurrentSelectedPath++;
				this.textLogger.logInfo(
						LogPrefix,
						"Trying to click the next element in the widget path:"
								+ this.currentSelectedPath.get(
										eleIndexInCurrentSelectedPath)
										.toString());
				return this.currentSelectedPath
						.get(eleIndexInCurrentSelectedPath);
			}
			if (setupNextShorestPath()) {
				// If we are able to find the next shortest path continue with
				// it
				this.eleIndexInCurrentSelectedPath++;
				return this.currentSelectedPath
						.get(eleIndexInCurrentSelectedPath);
			}
			// The App remains in the current screen after traversing all known
			// paths
			this.blackListedNodes.add(nextExpectedScreen);
			this.textLogger
					.logWarning(LogPrefix,
							"Added Screen:" + nextExpectedScreen.toString()
									+ " to blacklisted screens");
			if (this.targetScreenGraph.removePath(currScreen,
					this.totalPathToUnvisitedScreen.get(currIndUnVisScreen))) {
				this.textLogger
						.logWarning(LogPrefix,
								"Path is deleted from graph as it didn't give the target window");
			} else {
				this.textLogger.logError(LogPrefix,
						"Problem occured when trying to delete an edge from Node:"
								+ currScreen.toString());
			}
			if (setupNextUnVisitedScreen(currScreen)) {
				this.eleIndexInCurrentSelectedPath++;
				return this.currentSelectedPath
						.get(eleIndexInCurrentSelectedPath);
			}

			return null;

		} else {
			this.totalNumberOfActions++;
			if (!this.screenVisitedNodes.containsKey(currScreen)) {
				this.screenVisitedNodes.put(currScreen,
						new HashSet<Pair<ViewElement, IDeviceAction>>());
			}
			HashSet<Pair<ViewElement, IDeviceAction>> visitedElementsInScreen = this.screenVisitedNodes
					.get(currScreen);
			ArrayList<Pair<ViewElement, IDeviceAction>> currentElementsInScreen = currScreen
					.getAllPossibleActions();
			if (lastActionPerformed != null) {
				visitedElementsInScreen.add(lastActionPerformed);
			}

			if (visitedElementsInScreen.containsAll(currentElementsInScreen)) {
				this.textLogger
						.logInfo(
								LogPrefix,
								"Current Screen:"
										+ currScreen.toString()
										+ " completly visited, will try to find next unvisited window");
				this.completlyVisitedNodes.add(currScreen);
				if (setupNextUnVisitedScreen(currScreen)) {
					this.eleIndexInCurrentSelectedPath++;
					return this.currentSelectedPath
							.get(eleIndexInCurrentSelectedPath);
				}
				return null;
			} else {
				HashSet<Pair<ViewElement, IDeviceAction>> tempElements = new HashSet<Pair<ViewElement, IDeviceAction>>();
				tempElements.addAll(currentElementsInScreen);
				tempElements.removeAll(visitedElementsInScreen);
				Pair<ViewElement, IDeviceAction> nextElementToBeVisited = null;
				for (Pair<ViewElement, IDeviceAction> v : tempElements) {
					nextElementToBeVisited = v;
				}
				if (nextElementToBeVisited == null) {
					this.textLogger
							.logInfo(
									LogPrefix,
									"Current Screen:"
											+ currScreen.toString()
											+ " completly visited, will try to find next unvisited window");
					this.completlyVisitedNodes.add(currScreen);
					if (setupNextUnVisitedScreen(currScreen)) {
						this.eleIndexInCurrentSelectedPath++;
						return this.currentSelectedPath
								.get(eleIndexInCurrentSelectedPath);
					}
					return null;
				}
				visitedElementsInScreen.add(nextElementToBeVisited);
				currentTraversedPath.add(nextElementToBeVisited);
				this.textLogger.logInfo(LogPrefix,
						"Next Widget to Take action on:"
								+ nextElementToBeVisited.toString());
				return nextElementToBeVisited;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy#notifyNewScreen(edu
	 * .gatech.dynodroid.hierarchyHelper.ViewScreen,
	 * edu.gatech.dynodroid.hierarchyHelper.ViewScreen)
	 */
	@Override
	public synchronized ViewScreen notifyNewScreen(ViewScreen oldScreen,
			ViewScreen newScreen) {
		if (!isPathTraversing) {

			targetScreenGraph.AddEdge(oldScreen, newScreen,
					this.currentTraversedPath);
			ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> existingPath = targetScreenGraph
					.getPath(newScreen, oldScreen);
			//Add back edge only if there is no existing path form newscreen to oldscreen
			if (existingPath == null || existingPath.size() == 0) {
				ArrayList<Pair<ViewElement, IDeviceAction>> backPath = new ArrayList<Pair<ViewElement, IDeviceAction>>();
				backPath.add(newScreen.getBackWidget());
				targetScreenGraph.AddEdge(newScreen, oldScreen, backPath);
			}
			this.currentTraversedPath.clear();
			this.currentFocusedScreen = newScreen;
			return newScreen;
		} else {
			if (this.areScreensSame(nextExpectedScreen, newScreen)) {
				this.textLogger.logInfo(LogPrefix,
						"Got Same Window as expected:" + newScreen.toString());
				if (this.eleIndexInCurrentSelectedPath == (this.currentSelectedPath
						.size() - 1)) {
					this.textLogger.logInfo(LogPrefix,
							"Transition happned at same path as expected,Longest Path Length:"
									+ this.totalPathToBeTraversed.size()
									+ ",Current Path Length:"
									+ this.currentSelectedPath.size());
				} else {
					this.textLogger.logInfo(LogPrefix,
							"Transition happned at different path than expected,Longest Path Length:"
									+ this.totalPathToBeTraversed.size()
									+ ",Current Path Length:"
									+ this.currentSelectedPath.size()
									+ ",Target Transition Index:"
									+ this.eleIndexInCurrentSelectedPath);
				}
				if (this.areScreensSame(finalTargetExpectedScreen, newScreen)) {
					this.textLogger.logInfo(LogPrefix,
							"Transition Happned to the final target Window"
									+ newScreen.toString());
					cleanUpGraphPathCache();
					this.currentFocusedScreen = newScreen;
					return newScreen;
				}
				setupNextScreenInUnVisitedPath();

			} else {
				this.textLogger.logWarning(LogPrefix,
						"Got Different Window:" + newScreen.toString()
								+ " Other than the one expected:"
								+ nextExpectedScreen.toString());
				this.textLogger.logInfo(LogPrefix,
						"Continuing with this screen ignoring path divergence");
				cleanUpGraphPathCache();

			}
			this.currentFocusedScreen = newScreen;
			return newScreen;

		}
	}

	private void cleanUpPathCache() {
		this.currentSelectedPath.clear();
		this.currentTraversedPath.clear();
		this.nextExpectedScreen = null;
		this.eleIndexInCurrentSelectedPath = -1;
	}

	private void cleanUpGraphPathCache() {
		isPathTraversing = false;
		this.currIndUnVisScreen = -1;
		this.totalPathToUnvisitedScreen.clear();
		this.finalTargetExpectedScreen = null;
		cleanUpPathCache();
	}

	private boolean setupNextShorestPath() {
		this.currentTraversedPath.clear();
		if (this.totalPathToBeTraversed.size() == this.currentSelectedPath
				.size()) {
			return false;
		}
		int targetSize = this.currentSelectedPath.size() + 1;
		int startIndex = this.totalPathToBeTraversed.size() - targetSize;
		this.currentSelectedPath.clear();
		for (int i = startIndex; i < this.totalPathToBeTraversed.size(); i++) {
			this.currentSelectedPath.add(this.totalPathToBeTraversed.get(i));
		}
		this.textLogger.logInfo(LogPrefix, "Trying path of length:"
				+ this.currentSelectedPath.size());
		this.eleIndexInCurrentSelectedPath = -1;
		return true;
	}

	private void setupNextScreenInUnVisitedPath() {
		cleanUpPathCache();
		this.isPathTraversing = true;
		this.currIndUnVisScreen++;
		Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> nextScreenInPath = this.totalPathToUnvisitedScreen
				.get(currIndUnVisScreen);
		this.nextExpectedScreen = nextScreenInPath.getFirst();
		this.textLogger.logInfo(LogPrefix, "Next screen in the path found:"
				+ this.nextExpectedScreen.toString());
		this.totalPathToBeTraversed.addAll(nextScreenInPath.getSecond());
		setupNextShorestPath();
	}

	private boolean setupNextUnVisitedScreen(ViewScreen currentScreen) {
		boolean retVal = false;
		this.currentTraversedPath.clear();
		cleanUpGraphPathCache();
		ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> p2UnvisitedScreen = this.targetScreenGraph
				.getUnFilteredWindow(currentScreen, this.completlyVisitedNodes);
		if (p2UnvisitedScreen == null || p2UnvisitedScreen.size() == 0) {
			return false;
		}
		this.finalTargetExpectedScreen = p2UnvisitedScreen.get(
				p2UnvisitedScreen.size() - 1).getFirst();
		this.textLogger.logInfo(LogPrefix, "Found a path to:"
				+ this.finalTargetExpectedScreen.toString() + " from src Node:"
				+ currentScreen.toString());
		if (p2UnvisitedScreen != null) {
			this.totalPathToUnvisitedScreen.addAll(p2UnvisitedScreen);
			setupNextScreenInUnVisitedPath();
			retVal = true;
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy#initializeNewScreen
	 * (edu.gatech.dynodroid.hierarchyHelper.ViewScreen)
	 */
	@Override
	public synchronized boolean initializeNewScreen(ViewScreen firstScreen) {
		try {
			ViewScreenFeatures startScreenFeatures = new ViewScreenFeatures(0,
					0, 0, 0, "M3@AppStartScreen");
			this.startScreen = new ViewScreen(startScreenFeatures, null);
			this.targetScreenGraph.AddEdge(startScreen, firstScreen,
					new ArrayList<Pair<ViewElement, IDeviceAction>>());
			this.currentFocusedScreen = firstScreen;
			this.screenVisitedNodes.put(currentFocusedScreen,
					new HashSet<Pair<ViewElement, IDeviceAction>>());
			this.textLogger.logInfo(LogPrefix, "Initialization Sucessfull");
			cleanUpGraphPathCache();
			return true;
		} catch (Exception e) {
			this.textLogger.logException(LogPrefix, e);
		}
		return false;
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
		this.textLogger.logInfo(LogPrefix, "Completly Visited Screens:");
		String visitedScreens = "";
		for (ViewScreen s : this.completlyVisitedNodes) {
			visitedScreens += s.toString() + "\n";
		}
		this.textLogger.logInfo(LogPrefix, visitedScreens);
		this.textLogger.logInfo(LogPrefix, "Black Listed Screens:");
		visitedScreens = "";
		for (ViewScreen s : this.blackListedNodes) {
			visitedScreens += s.toString() + "\n";
		}
		this.textLogger.logInfo(LogPrefix, visitedScreens);
		this.targetScreenGraph.dumpGraphStats(this.textLogger);

	}

	@Override
	public void addNonUiDeviceAction(Pair<ViewElement, IDeviceAction> action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNonUiDeviceAction(Pair<ViewElement, IDeviceAction> action) {
		// TODO Auto-generated method stub
		
	}

}
