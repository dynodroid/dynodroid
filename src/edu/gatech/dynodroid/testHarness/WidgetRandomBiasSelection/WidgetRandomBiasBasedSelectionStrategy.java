/**
 * 
 */
package edu.gatech.dynodroid.testHarness.WidgetRandomBiasSelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import edu.gatech.dynodroid.deviceEvent.BroadCastAction;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.TextInputEvent;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.hierarchyHelper.ViewScreen;
import edu.gatech.dynodroid.testHarness.WidgetBasedTesting;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Pair;
import edu.gatech.dynodroid.utilities.TextLogger;

/**
 * @author rohant
 * 
 */
public class WidgetRandomBiasBasedSelectionStrategy extends WidgetSelectionStrategy {

    private String currWorkingDir;
    private TextLogger textLogger;
    private TextLogger textBoxLogger;;
    private ViewScreen currentScreen;
    private static final Integer inScreenCost = 0;
    private static final Integer offScreenCost = -1;
    private static final String logPrefix = "RandomBiasBased";
    private Random randomNumber;
    private int totalNumberOfActions = 0;
    private int coverageGranularity = 100;

    HashMap<Pair<ViewElement, IDeviceAction>, Integer> costMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
    HashMap<ViewScreen, HashMap<Pair<ViewElement, IDeviceAction>, Integer>> screenBiasMap = 
        new HashMap<ViewScreen, HashMap<Pair<ViewElement, IDeviceAction>, Integer>>();
    HashMap<ViewScreen, HashMap<Pair<ViewElement, IDeviceAction>, Integer>> widgetCountMap = 
        new HashMap<ViewScreen, HashMap<Pair<ViewElement, IDeviceAction>, Integer>>();
    HashMap<Pair<ViewElement, IDeviceAction>, Integer> currentBiasMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();
    HashSet<Pair<ViewElement, IDeviceAction>> nonUIEvents = new HashSet<Pair<ViewElement, IDeviceAction>>();
    HashSet<Pair<ViewElement, IDeviceAction>> populatedTextFields = new HashSet<Pair<ViewElement, IDeviceAction>>();

    public WidgetRandomBiasBasedSelectionStrategy
        (String workingDir,
         int samplingInterval) throws Exception {

        this.currWorkingDir = workingDir;
        FileUtilities.createDirectory(currWorkingDir);
        this.textLogger = new TextLogger(this.currWorkingDir
                                         + "/WidgetRandomBiasBasedSelection.log");
        this.textBoxLogger = new TextLogger(this.currWorkingDir
                                         + "/textBoxOutput.txt");
        this.randomNumber = new Random();
        if (samplingInterval > 0) {
            this.coverageGranularity = samplingInterval;
        }
    }

    @Override
	public Pair<ViewElement, IDeviceAction> getNextElementAction
        (ViewScreen currScreen,
         Pair<ViewElement, IDeviceAction> lastPerformedAction,
         boolean resultOfLastOperation) {

        if (resultOfLastOperation && lastPerformedAction != null) {
            totalNumberOfActions++;
        }
        this.currentScreen = currScreen;
        if(currScreen!=null && screenBiasMap.get(currScreen) == null) {
            screenBiasMap.put(currScreen, new HashMap<Pair<ViewElement, IDeviceAction>, Integer>());
        }
        if(currScreen!=null && widgetCountMap.get(this.currentScreen) == null) {
            widgetCountMap.put(this.currentScreen, new HashMap<Pair<ViewElement, IDeviceAction>, Integer>());
        }
        ArrayList<Pair<ViewElement, IDeviceAction>> actions = getAllActionsOfCost(inScreenCost);
        setBias(actions, lastPerformedAction);
        Pair<ViewElement, IDeviceAction> retEle = getNextAction(actions);

        increaseWidgetCount(retEle);

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

    private synchronized ArrayList<Pair<ViewElement, IDeviceAction>> getAllActionsOfCost
        (Integer targetCost) {

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

    private synchronized Pair<ViewElement, IDeviceAction> getNextAction
        (ArrayList<Pair<ViewElement, IDeviceAction>> candidateActions) {

        Pair<ViewElement, IDeviceAction> retVal = null;
        if (candidateActions != null && candidateActions.size() > 0) {
            while(retVal == null) {
                int rand = this.randomNumber.nextInt(candidateActions.size());
                if (rand >= candidateActions.size()) {
                    rand = candidateActions.size() - 1;
                }
                retVal = candidateActions.get(rand);
                if(!getCurrentBias(retVal)) {
                    retVal = null;
                }
            }
        }
        return retVal;
    }

    private void increaseWidgetCount(Pair<ViewElement, IDeviceAction> nextAction) {
        HashMap<Pair<ViewElement, IDeviceAction>, Integer> countMap = widgetCountMap.get(this.currentScreen);
        HashMap<Pair<ViewElement, IDeviceAction>, Integer> biasMap = screenBiasMap.get(this.currentScreen);

        if(countMap.get(nextAction) != null) {
            int currentCount = countMap.get(nextAction);
            countMap.put(nextAction, currentCount + 1);
        }

        if(biasMap.get(nextAction) != null) {
            int cost = biasMap.get(nextAction);
            biasMap.put(nextAction, cost + 1);
            this.textLogger.logInfo(logPrefix,"Updated biasmap: " + biasMap.get(nextAction) + " : " + nextAction);
        }
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

    private synchronized void setBias
        (ArrayList<Pair<ViewElement, IDeviceAction>> actions,
         Pair<ViewElement, IDeviceAction> lastAction) {
    	
    	HashMap<Pair<ViewElement, IDeviceAction>, Integer> biasMap = null;
        HashMap<Pair<ViewElement, IDeviceAction>, Integer> countMap = null;
    	
    	 if(this.currentScreen == null) {
             return; 
         }
    	 biasMap = screenBiasMap.get(this.currentScreen);
    	 countMap = widgetCountMap.get(this.currentScreen);
    	 if(biasMap == null || countMap == null){
    		 return;
    	 }

        currentBiasMap = new HashMap<Pair<ViewElement, IDeviceAction>, Integer>();

        for(Pair<ViewElement, IDeviceAction> action: actions) {
            if(biasMap.get(action) == null) {
                IDeviceAction devAction = action.getSecond();
                if(devAction instanceof TextInputEvent) { 
                    biasMap.put(action, -1);
                } else if(devAction instanceof BroadCastAction) {
                    biasMap.put(action, 2);
                } else {
                    biasMap.put(action, 1);
                }
            }
            currentBiasMap.put(action, 0);
            if(countMap.get(action) == null) {
                countMap.put(action, 0);
            }
        }
    }

    private synchronized boolean getCurrentBias(Pair<ViewElement, IDeviceAction> action) {
        if(currentBiasMap.size() == 1 || screenBiasMap.get(currentScreen) == null) {
            return true;
        }

        boolean retVal = true;
        int currentBias = currentBiasMap.get(action) + 1;
        int baseBias = screenBiasMap.get(currentScreen).get(action);
        
        if(baseBias < 0 || currentBias < baseBias) {
            retVal = false;
        }

        currentBiasMap.put(action, currentBias);

        return retVal;
    }

    public int populateTextFields(ViewScreen screen, WidgetBasedTesting widgetBasedTesting) {
        if(screen != null) {
            this.currentScreen = screen;
            if(this.currentScreen!=null && screenBiasMap.get(this.currentScreen) == null) {
                screenBiasMap.put(this.currentScreen, new HashMap<Pair<ViewElement, IDeviceAction>, Integer>());            
            }
            if(this.currentScreen!=null && widgetCountMap.get(this.currentScreen) == null) {
                widgetCountMap.put(this.currentScreen, new HashMap<Pair<ViewElement, IDeviceAction>, Integer>());
            }

            ArrayList<Pair<ViewElement, IDeviceAction>> allAction = screen.getAllPossibleActions();
            int newTextFields = 0;
            for (Pair<ViewElement, IDeviceAction> p : allAction) {
                IDeviceAction devAction = p.getSecond();
                if(devAction instanceof TextInputEvent && !populatedTextFields.contains(p)) {
                    try {
                        widgetBasedTesting.performAction(p);
                        populatedTextFields.add(p);
                        increaseWidgetCount(p);
                        this.textLogger.logInfo(logPrefix,
                                                " Execised ViewElement:"
                                                + p.toString());
                        addFieldToOutputFile(p, screen);
                        newTextFields++;
                    } catch(InterruptedException ex) {
                    }
                }
            }
            return newTextFields;
        }
        return 0;
    }

    private void addFieldToOutputFile(Pair<ViewElement, IDeviceAction> pair, ViewScreen screen) {
        this.textBoxLogger.logInfo("", screen.toString());
        this.textBoxLogger.logInfo("", pair.getFirst().toString());
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
        this.textLogger.logInfo(logPrefix, "WIDGET RANDOM BIAS STRATEGY STATISTICS");

        this.textLogger.logInfo(logPrefix, "WIDGET COUNT");
        for(ViewScreen key: widgetCountMap.keySet()) {

            this.textLogger.logInfo(logPrefix, key.toString());

            HashMap<Pair<ViewElement, IDeviceAction>, Integer> countMap = widgetCountMap.get(key);
            for (Pair<ViewElement, IDeviceAction> p : countMap.keySet()) {
                this.textLogger.logInfo(logPrefix, "Element:" + p.toString()
                                        + " Count:" + countMap.get(p));
            }
        }
        this.textLogger.logInfo(logPrefix, "END WIDGET COUNT");

        this.textLogger.logInfo(logPrefix, "WIDGET COST");
        for (Pair<ViewElement, IDeviceAction> p : costMap.keySet()) {
            this.textLogger.logInfo(logPrefix, "Element:" + p.toString()
                                    + " Cost:" + costMap.get(p));
        }
        this.textLogger.logInfo(logPrefix, "END WIDGET COST");

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
