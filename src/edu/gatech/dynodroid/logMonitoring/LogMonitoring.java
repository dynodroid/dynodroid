/**
 * 
 */
package edu.gatech.dynodroid.logMonitoring;

import java.util.ArrayList;
import java.util.HashMap;

import edu.gatech.dynodroid.clients.AndroidServicesMonitoring;
import edu.gatech.dynodroid.clients.AppServiceMonitoring;
import edu.gatech.dynodroid.clients.AudioServiceMonitoring;
import edu.gatech.dynodroid.clients.BroadCastActionTriggerMonitoring;
import edu.gatech.dynodroid.clients.BroadCastRegistrationMonitor;
import edu.gatech.dynodroid.clients.BroadCastStartMonitoring;
import edu.gatech.dynodroid.clients.BroadCastUnRegistrationMonitor;
import edu.gatech.dynodroid.clients.DalvikMethodCoverageMonitoring;
import edu.gatech.dynodroid.clients.DexLoadingMonitoring;
import edu.gatech.dynodroid.clients.IntentStartMonitoring;
import edu.gatech.dynodroid.clients.KernelFileMonitoring;
import edu.gatech.dynodroid.clients.KernelNetworkMonitoring;
import edu.gatech.dynodroid.clients.LoadLibraryMonitoring;
import edu.gatech.dynodroid.clients.MonitoringClient;
import edu.gatech.dynodroid.clients.OutBoundUrlMonitoring;
import edu.gatech.dynodroid.clients.SMSMonitoring;
import edu.gatech.dynodroid.clients.StartProcessMonitoring;
import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.testHarness.WidgetSelectionStrategy;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * @author machiry
 * 
 */
public class LogMonitoring {

	private static HashMap<ADevice, HashMap<String, MonitoringClient>> deviceMonitors = new HashMap<ADevice, HashMap<String, MonitoringClient>>();

	private static final String smsMonitorName = "SMSSend";
	private static final String kerNetworkMonitorName = "KernelNetwork";
	private static final String kerFileNetworkMonitorName = "KernelFile";
	private static final String loadLibraryMonitorName = "LOADLIB";
	private static final String dexLoadMonitorName = "DEXLOAD";
	private static final String broadCastRegistrationMonitoring = "BroadCastRegister";
	private static final String broadCastUnRegistrationMonitoring = "BroadCastUnRegister";
	private static final String broadCastActionMonitoring = "BroadCastAction";
	private static final String audioServiceMonitoring = "AudioServiceInteraction";
	private static final String androidServicesMonitoring = "AndroidServicesInteraction";
	private static final String startProcessMonitoring = "StartAppProcess";
	private static final String intentMonitoring = "StartIntentMonitoring";
	private static final String appServiceMonitoring = "StartAppService";
	private static final String broadCastToApkReceiver = "BroadCastingToPackageReceivers";
	private static final String outboundUrlMonitoring = "OutboundUrl";
	private static final String apkMethodCoverageMonitoring = "DalvikMethodCoverage";

	/**
	 * This method initializes the monitoring of the log for each device This
	 * method could be called multiple times
	 * 
	 * @param targetDevice
	 *            device on which monitoring clients need to be initialized
	 * @param baseWorkDir
	 *            base working directory
	 * @param kernelModules
	 *            folder in which all the required kernel modules are present
	 * @return true/false This depends on whether the monitoring is successful
	 *         or not
	 */
	public static boolean initializeMonitoring(ADevice targetDevice,
			String baseWorkDir, String kernelModules,
			WidgetSelectionStrategy feedBack) {
		boolean allSuccess = true;
		if (targetDevice != null && baseWorkDir != null
				&& kernelModules != null) {
			allSuccess = true;
			FileUtilities.createDirectory(baseWorkDir);
			synchronized (deviceMonitors) {
				try {
					HashMap<String, MonitoringClient> targetMonitors = new HashMap<String, MonitoringClient>();

					ArrayList<MonitoringClient> newLogCatMonitors = new ArrayList<MonitoringClient>();
					ArrayList<MonitoringClient> newkernelLogMonitors = new ArrayList<MonitoringClient>();

					if (deviceMonitors.containsKey(targetDevice)) {
						targetMonitors = deviceMonitors.get(targetDevice);
					} else {
						deviceMonitors.put(targetDevice, targetMonitors);
					}

					if (targetMonitors.containsKey(smsMonitorName)) {
						allSuccess = allSuccess
								&& targetMonitors.get(smsMonitorName)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ smsMonitorName
														+ "_monitoring.log",
												feedBack);
					} else {
						SMSMonitoring monitor = new SMSMonitoring(targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(smsMonitorName, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + smsMonitorName
										+ "_monitoring.log", feedBack);

					}

					if (targetMonitors.containsKey(loadLibraryMonitorName)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(loadLibraryMonitorName)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ loadLibraryMonitorName
														+ "_monitoring.log",
												feedBack);
					} else {
						LoadLibraryMonitoring monitor = new LoadLibraryMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(loadLibraryMonitorName, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + loadLibraryMonitorName
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(dexLoadMonitorName)) {
						allSuccess = allSuccess
								&& targetMonitors.get(dexLoadMonitorName)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ dexLoadMonitorName
														+ "_monitoring.log",
												feedBack);
					} else {
						DexLoadingMonitoring monitor = new DexLoadingMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(dexLoadMonitorName, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + dexLoadMonitorName
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(broadCastActionMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(broadCastActionMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ broadCastActionMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						BroadCastActionTriggerMonitoring monitor = new BroadCastActionTriggerMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(broadCastActionMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + broadCastActionMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors
							.containsKey(broadCastRegistrationMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(broadCastRegistrationMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ broadCastRegistrationMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						BroadCastRegistrationMonitor monitor = new BroadCastRegistrationMonitor(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(broadCastRegistrationMonitoring,
								monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + broadCastRegistrationMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors
							.containsKey(broadCastUnRegistrationMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(broadCastUnRegistrationMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ broadCastUnRegistrationMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						BroadCastUnRegistrationMonitor monitor = new BroadCastUnRegistrationMonitor(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(broadCastUnRegistrationMonitoring,
								monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/"
										+ broadCastUnRegistrationMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(audioServiceMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(audioServiceMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ audioServiceMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						AudioServiceMonitoring monitor = new AudioServiceMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(audioServiceMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + audioServiceMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(androidServicesMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(androidServicesMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ androidServicesMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						AndroidServicesMonitoring monitor = new AndroidServicesMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(androidServicesMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + androidServicesMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(startProcessMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(startProcessMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ startProcessMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						StartProcessMonitoring monitor = new StartProcessMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(startProcessMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + startProcessMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(appServiceMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors.get(appServiceMonitoring)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ appServiceMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						AppServiceMonitoring monitor = new AppServiceMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(appServiceMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + appServiceMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(intentMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors.get(intentMonitoring)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ intentMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						IntentStartMonitoring monitor = new IntentStartMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(intentMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + intentMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(broadCastToApkReceiver)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(broadCastToApkReceiver)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ broadCastToApkReceiver
														+ "_monitoring.log",
												feedBack);
					} else {
						BroadCastStartMonitoring monitor = new BroadCastStartMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(broadCastToApkReceiver, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + broadCastToApkReceiver
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(outboundUrlMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors.get(outboundUrlMonitoring)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ outboundUrlMonitoring
														+ "_monitoring.log",
												feedBack);
					} else {
						OutBoundUrlMonitoring monitor = new OutBoundUrlMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors.put(outboundUrlMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + outboundUrlMonitoring
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(apkMethodCoverageMonitoring)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(apkMethodCoverageMonitoring)
										.initializeMonitoring(
												baseWorkDir
														+ "/MethodsTrigerred.txt",
												feedBack);
					} else {
						DalvikMethodCoverageMonitoring monitor = new DalvikMethodCoverageMonitoring(
								targetDevice);
						newLogCatMonitors.add(monitor);
						targetMonitors
								.put(apkMethodCoverageMonitoring, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/MethodsTrigerred.txt", feedBack);
					}

					if (targetMonitors.containsKey(kerNetworkMonitorName)) {
						allSuccess = allSuccess
								&& targetMonitors.get(kerNetworkMonitorName)
										.initializeMonitoring(
												baseWorkDir + "/"
														+ kerNetworkMonitorName
														+ "_monitoring.log",
												feedBack);
					} else {
						KernelNetworkMonitoring monitor = new KernelNetworkMonitoring(
								targetDevice, kernelModules);
						newkernelLogMonitors.add(monitor);
						targetMonitors.put(kerNetworkMonitorName, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + kerNetworkMonitorName
										+ "_monitoring.log", feedBack);
					}

					if (targetMonitors.containsKey(kerFileNetworkMonitorName)) {
						allSuccess = allSuccess
								&& targetMonitors
										.get(kerFileNetworkMonitorName)
										.initializeMonitoring(
												baseWorkDir
														+ "/"
														+ kerFileNetworkMonitorName
														+ "_monitoring.log",
												feedBack);
					} else {
						KernelFileMonitoring monitor = new KernelFileMonitoring(
								targetDevice, kernelModules);
						newkernelLogMonitors.add(monitor);
						targetMonitors.put(kerFileNetworkMonitorName, monitor);
						allSuccess = allSuccess
								&& monitor.initializeMonitoring(baseWorkDir
										+ "/" + kerFileNetworkMonitorName
										+ "_monitoring.log", feedBack);
					}
					if (newkernelLogMonitors.size() > 0) {
						allSuccess = allSuccess
								&& targetDevice
										.addKerenelMonitots(newkernelLogMonitors);
					}
					if (newLogCatMonitors.size() > 0) {
						allSuccess = allSuccess
								&& targetDevice
										.addLogCatMonitors(newLogCatMonitors);
					}

				} catch (Exception e) {
					Logger.logException(e);
					allSuccess = false;
				}

			}
		}
		return allSuccess;
	}

	/***
	 * 
	 * @param cleanTheLogs
	 * @return
	 */
	public static boolean stopMonitoring(ADevice targetDevice,
			boolean cleanTheLogs) {
		boolean allSucess = true;
		try {
			if (targetDevice != null) {
				allSucess = true;
				targetDevice.stopLogMonitoring();
				targetDevice.stopKernelLogMonitoring();
			}
		} catch (Exception e) {
			Logger.logException(e);
			allSucess = false;
		}
		return allSucess;
	}

	public static boolean startMonitoring(ADevice targetDevice) {
		boolean allSucess = true;// false;
		try {
			if (targetDevice != null) {
				allSucess = true;
				targetDevice.startLogMonitoring();
				targetDevice.startKernelLogMonitoring();
			}
		} catch (Exception e) {
			Logger.logException(e);
			allSucess = false;
		}

		return allSucess;
	}

	public static boolean cleanMonitoring(ADevice targetDevice, String workDir) {
		boolean allSucess = true;// false;
		try {
			if (targetDevice != null && workDir != null) {
				allSucess = true;
				targetDevice
						.cleanLogEntries(workDir + "/GenerelLogCatLogs.log");
				targetDevice.cleanKernelLogEntries(workDir
						+ "/GenerelKernelLogs.log");
			}
		} catch (Exception e) {
			Logger.logException(e);
			allSucess = false;
		}

		return allSucess;
	}

	public static void addTag(ADevice targetDevice, String tag, int runs) {

		HashMap<String, MonitoringClient> targetMonitors = null;
		long currTime = System.currentTimeMillis();

		synchronized (deviceMonitors) {
			if (deviceMonitors.containsKey(targetDevice)) {
				targetMonitors = deviceMonitors.get(targetDevice);
			}
		}

		tag = tag.replaceFirst("_0", "_" + runs);

		if (targetMonitors != null) {
			for (MonitoringClient m : targetMonitors.values()) {
				m.addTag(currTime + ":" + tag);
			}
		}
	}

	public static void releaseLogs(ADevice targetDevice) {
		synchronized (deviceMonitors) {
			if (deviceMonitors.containsKey(targetDevice)) {
				deviceMonitors.remove(targetDevice);
			}
		}
	}

	public static void addAppId(ADevice targetDevice, int targetAppId) {
		HashMap<String, MonitoringClient> targetMonitors = null;
		synchronized (deviceMonitors) {
			if (deviceMonitors.containsKey(targetDevice)) {
				targetMonitors = deviceMonitors.get(targetDevice);
			}
		}

		if (targetMonitors != null) {
			for (MonitoringClient m : targetMonitors.values()) {
				m.addTargetAppId(targetAppId);
			}
		}
	}

}
