package edu.gatech.dynodroid.master;

import java.util.UUID;

public class TestProfile {
	public String baseWorkingDir = "";
	public int touchPercentage = 50;
	public int smallNavigationPercentage = 20;
	public int majorNavigationPercentage = 0;
	public int trackballPercentage = 30;
	public long delayBetweenEvents = 500;
	public int eventCount = 100;
	public int verboseLevel = 3;
	public long responseDelay = 3000;
        public long appStartUpDelay = 8000;
	public String sdkInstallPath = null;
	public int coverageDumpTime = 4000;
	public String baseAppDir = "";
	public int maxNoOfWidgets = 10000;
	public String emmaLibPath = null;
	public String testStrategy = "";
	public String appName = "";
	public int coverageSamplingInterval = 100;
	public String instrumetationSetupDir = null;
	public String widgetSelectionStrategy = "GraphBased";
	public boolean isApk = false;
	public String targetEmailAlias = null;
	public UUID requestUUID = null;
	
	public String baseLogDir = null;

	@Override
	public String toString() {
		return this.appName + ":" + this.testStrategy + ":"
				+ this.widgetSelectionStrategy + ":" + this.appName + ":"
				+ this.baseWorkingDir + ":" + this.eventCount;
	}

}
