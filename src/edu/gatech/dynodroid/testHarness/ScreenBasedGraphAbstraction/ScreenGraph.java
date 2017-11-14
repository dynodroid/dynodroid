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
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.Pair;

/**
 * This is the abstraction of Graph structure based on App Screens 
 * @author machiry
 * 
 */
public class ScreenGraph {
	private HashSet<ViewScreen> nodes = new HashSet<ViewScreen>();
	private HashMap<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>, ViewScreen> edges = new HashMap<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>, ViewScreen>();

	public ScreenGraph() {

	}

	public boolean AddNode(ViewScreen newWin) {
		if (!nodes.contains(newWin)) {
			nodes.add(newWin);
			return true;
		}
		return false;
	}

	
	public boolean AddEdge(ViewScreen srcNode, ViewScreen dstNode,
			ArrayList<Pair<ViewElement, IDeviceAction>> viewActions) {
		assert (viewActions != null && viewActions.size() > 0);
		ArrayList<Pair<ViewElement, IDeviceAction>> viewList = new ArrayList<Pair<ViewElement, IDeviceAction>>();
		viewList.addAll(viewActions);
		Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> nodeP = new Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>(
				srcNode, viewList);
		AddNode(srcNode);
		AddNode(dstNode);
		if (!edges.containsKey(nodeP)) {
			edges.put(nodeP, dstNode);
		}
		return false;
	}

	/***
	 * This method will return all the possible paths from the current screen to any screen 
	 * @param srcNode the src screen from which the paths were required
	 * @return all possible paths from the given screen
	 */
	public HashMap<ArrayList<Pair<ViewElement, IDeviceAction>>, ViewScreen> GetAllTransitions(
			ViewScreen srcNode) {
		HashMap<ArrayList<Pair<ViewElement, IDeviceAction>>, ViewScreen> transitions = new HashMap<ArrayList<Pair<ViewElement, IDeviceAction>>, ViewScreen>();
		if (nodes.contains(srcNode)) {
			for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
					.keySet()) {
				if (p.getFirst().equals(srcNode)) {
					transitions.put(
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond()), edges.get(p));
				}
			}
		}
		return transitions;
	}

	public boolean removePath(
			ViewScreen parent,
			Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> targetPath) {
		boolean removeSucessfull = false;
		if (nodes.contains(parent) && targetPath != null) {
			Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> targetEdge = new Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>(
					parent, targetPath.getSecond());
			if (edges.containsKey(targetEdge)
					&& edges.get(targetEdge).equals(targetPath.getFirst())) {
				edges.remove(targetEdge);
				removeSucessfull = true;
			}
		}
		return removeSucessfull;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> getUnFilteredWindow(
			ViewScreen startWin, ArrayList<ViewScreen> filterList,
			ArrayList<ViewScreen> visitedWindows) {
		ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> path = new ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>>();
		if (visitedWindows.contains(startWin)) {
			return path;
		}
		visitedWindows.add(startWin);

		// We search for non explored or non filtered window in dfs format
		// Check all the children recursively by giving preference to the
		// immediate children
		// else traverse backwards from parent

		// 1.This is to check if any of the immediate child is not traversed
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
				.keySet()) {
			ViewScreen first = p.getFirst();
			ViewScreen second = edges.get(p);
			if (first.equals(startWin)
					&& !ViewElement.isBackButton(p.getSecond().get(0).getSecond())) {
				if (!filterList.contains(second)) {
					path.add(new Pair(second,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					return path;
				}
			}
		}

		// 2.This is to check if any of the forward child is not traversed
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
				.keySet()) {
			ViewScreen first = p.getFirst();
			ViewScreen second = edges.get(p);
			if (first.equals(startWin)
					&& !ViewElement.isBackButton(p.getSecond().get(0).getSecond())) {
				ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> tempPath = getUnFilteredWindow(
						second, filterList, visitedWindows);
				if (!tempPath.isEmpty()) {
					path.add(new Pair(second,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					path.addAll(tempPath);
					return path;
				}
			}
		}

		// 3.Check same recursively for parent
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
				.keySet()) {
			ViewScreen first = p.getFirst();
			ViewScreen second = edges.get(p);
			if (first.equals(startWin)
					&& ViewElement.isBackButton(p.getSecond().get(0).getSecond())) {
				if (!filterList.contains(second)) {
					path.add(new Pair(second,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					return path;
				}
				ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> tempPath = getUnFilteredWindow(
						second, filterList, visitedWindows);
				if (!tempPath.isEmpty()) {
					path.add(new Pair(second,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					path.addAll(tempPath);
					return path;
				}
			}
		}

		return path;
	}

	/***
	 * This will return a screen reachable from the provided window which is unfiltered
	 * @param startWin the srcScreen
	 * @param filterList the filter list
	 * @return Path to the target screen
	 */
	public ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> getUnFilteredWindow(
			ViewScreen startWin, ArrayList<ViewScreen> filterList) {
		return getUnFilteredWindow(startWin, filterList,
				new ArrayList<ViewScreen>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> getPath(
			ViewScreen startWin, ViewScreen targetWin,
			ArrayList<ViewScreen> visitedWindows) {
		ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> path = new ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>>();
		if (visitedWindows.contains(startWin)) {
			return path;
		}
		visitedWindows.add(startWin);
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
				.keySet()) {
			ViewScreen first = p.getFirst();
			ViewScreen second = edges.get(p);
			if (first.equals(startWin)) {
				if (second.equals(targetWin)) {
					path.add(new Pair(targetWin,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					break;
				}
				ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> tempPath = getPath(
						second, targetWin,visitedWindows);
				if (tempPath != null && !tempPath.isEmpty()) {
					path.add(new Pair(second,
							new ArrayList<Pair<ViewElement, IDeviceAction>>(p
									.getSecond())));
					path.addAll(tempPath);
					break;
				}
			}
		}
		return path;
	}

	public ArrayList<Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>>> getPath(
			ViewScreen startWin, ViewScreen targetWin) {
		return getPath(startWin, targetWin, new ArrayList<ViewScreen>());
	}

	public ArrayList<Pair<ViewElement, IDeviceAction>> GetPath(
			ViewScreen startWin, ViewScreen targetWin) {
		ArrayList<Pair<ViewElement, IDeviceAction>> pathEdges = new ArrayList<Pair<ViewElement, IDeviceAction>>();
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : edges
				.keySet()) {
			ViewScreen first = p.getFirst();
			ViewScreen second = edges.get(p);
			if (first.equals(startWin)) {
				if (second.equals(targetWin)) {
					pathEdges.addAll(p.getSecond());
					break;
				}
				ArrayList<Pair<ViewElement, IDeviceAction>> tempPath = GetPath(
						second, targetWin);
				if (tempPath != null && !tempPath.isEmpty()) {
					pathEdges.addAll(p.getSecond());
					pathEdges.addAll(tempPath);
					break;
				}
			}
		}
		return pathEdges;
	}

	
	public HashSet<ViewScreen> getNodes() {
		return this.nodes;
	}

	/***
	 * This will dump the current graph structure to the provided logwriter
	 * @param logWriter the logwriter to which the dumping to be made
	 */
	public void dumpGraphStats(Logger logWriter) {
		String dump = "START GRAPH DUMP:";
		dump = "\nNodes:\n";
		for (ViewScreen s : this.nodes) {
			dump = dump + "\t\t"+ s.toString() + "\n";
		}
		dump += "Edges:\n";
		int edgeCount = 1;
		for (Pair<ViewScreen, ArrayList<Pair<ViewElement, IDeviceAction>>> p : this.edges
				.keySet()) {
			dump += "\t\t Edge " + edgeCount + " From:" + p.getFirst().toString()
					+ " with Widget,Actions:\n";
			for (Pair<ViewElement, IDeviceAction> v : p.getSecond()) {
				dump += "\t\t\t"+v.toString() + "\n";
			}
			dump += "\t\tTo:" + this.edges.get(p).toString() + "\n\n";
			edgeCount++;
		}

		dump += "END GRAPH DUMP";
		logWriter.logInfo("GRAPH_DUMP", dump);
	}
}
