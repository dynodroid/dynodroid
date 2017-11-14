package edu.gatech.dynodroid.clients;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.Logger;

public class KernelNetworkMonitoring extends MonitoringClient {

	public static final String kernelModuleName = "m3netmod";
	public String onHostModuleLoc = null;

	public KernelNetworkMonitoring(ADevice targetDevice,
			String onHostModuleLocation) throws Exception {
		if (targetDevice != null && onHostModuleLocation != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3_NET_Ker";
			this.onHostModuleLoc = onHostModuleLocation;
			Logger.logInfo("KernelNetworkMonitoring Monitoring Initialized for:"+targetDevice.toString());
		} else {
			throw new Exception(
					"Problem occured while creating KernelNetworkMonitoring module");
		}
	}
	
	//Dude!! we need to install the kernel module here
		@Override
		public boolean initializeMonitoring(String logFile,WidgetSelectionStrategy feeB) {
			String onDevLocation = "/sdcard/"+kernelModuleName+".ko";
			this.finalTargetDevice.putFileInToDevice(onHostModuleLoc+"/"+kernelModuleName+".ko", onDevLocation);
			this.finalTargetDevice.executeShellCommand("insmod "+onDevLocation);		
			return super.initializeMonitoring(logFile,feeB);
		}
		
		//Try to remove the module
		@Override
		public void cleanMonitoringInfo() {
			//Need to fix this
			//this.finalTargetDevice.executeShellCommand("rmmod "+kernelModuleName);
			super.cleanMonitoringInfo();
		}

}
