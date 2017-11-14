package edu.gatech.dynodroid.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import edu.gatech.dynodroid.testHarness.RandomMonkeyTesting;
import edu.gatech.dynodroid.testHarness.StrategyFactory;
import edu.gatech.dynodroid.testHarness.WidgetBasedTesting;

public class PropertyParser {

	public static String baseWorkingDir = "";
	public static int touchPercentage = 15;
	public static int smallNavigationPercentage = 25;
	public static int majorNavigationPercentage = 15;
	public static int trackballPercentage = 15;
	public static long delayBetweenEvents = 1000;
	public static int[] eventCountArray = new int[1];
	public static int verboseLevel = 1;
	public static long responseDelay = 3000;
	public static long appStartUpDelay = 5000;
	public static String sdkInstallPath = null;
	public static int coverageDumpTime = 4000;
	public static String baseAppDir = "";
	public static int[] maxNoOfWidgets = new int[1];
	public static String emmaLibPath = null;
	public static int coverageSamplingInterval = 100;
	public static ArrayList<String> testStrategy = new ArrayList<String>();
	public static ArrayList<String> widgetSelectionStrategy = new ArrayList<String>();
	public static String instrumentationHelperDir = null;
	public static String kernelModulesLocation = "";
	public static String mailNotificationRecipents = null;
	public static String createEmuScript = null;
	public static String avdLocation = null;
	public static String apkToolLocation = null;
	public static int maxNoOfEmulators = 16;
	public static String customSystemImg = null;
	public static String customRamDiskImg = null;
	public static String remoteServerApkSrcPath = "";
	public static String remoteServerResultPath = "";
	public static String apkServerName = "";
	public static String reportEmailUserName = "example@gmail.com";
	public static String reportEmailPassword = "emailPassword";
	public static String resultServerName = "";
	public static String resultPublicServer = "";
	public static String resultDownloadablePath = "";
	public static String scpAccessUserName = "";
	public static String resultEmailAlias = null;
	public static String rmiDBConnectionString = null;
	public static String postProcessingScript = null;
	public static String webServerForResults = null;
	public static String MonkeyRunnerScript = null;
	public static int androidTarget = 3;
	public static boolean isManualMode = false;
	public static String toolLoc = null;

	public static boolean parsePropertiesFile(String propertiesFile) {
		boolean retVal = true;
		try {
			InputStream is = new FileInputStream(propertiesFile);
			Properties prop = new Properties();
			prop.load(is);
			eventCountArray[0] = 100;
			maxNoOfWidgets[0] = 100;
			testStrategy.add(RandomMonkeyTesting.randomTestingStrategy);
			widgetSelectionStrategy.add("RandomBiasBased");
			baseAppDir = prop.getProperty("app_dir");
			if (baseAppDir == null) {
				displayPropertiesUsage();
				return false;
			}
			baseWorkingDir = prop.getProperty("work_dir");
			if (baseWorkingDir == null) {
				displayPropertiesUsage();
				return false;
			}

			sdkInstallPath = prop.getProperty("sdk_install");
			if (sdkInstallPath == null) {
				displayPropertiesUsage();
				return false;
			}

			instrumentationHelperDir = prop.getProperty("instru_setup");
			if (instrumentationHelperDir == null) {
				displayPropertiesUsage();
				return false;
			}

			kernelModulesLocation = prop.getProperty("ker_mod");
			if (kernelModulesLocation == null) {
				displayPropertiesUsage();
				return false;
			}

			rmiDBConnectionString = prop.getProperty("rmi_db");
			if (rmiDBConnectionString == null) {
				displayPropertiesUsage();
				return false;
			}

			apkServerName = prop.getProperty("apk_srv");
			if (apkServerName == null) {
				displayPropertiesUsage();
				return false;
			}

			scpAccessUserName = prop.getProperty("scp_user_name");
			if (scpAccessUserName == null) {
				displayPropertiesUsage();
				return false;
			}

			resultServerName = prop.getProperty("res_srv");
			if (resultServerName == null) {
				displayPropertiesUsage();
				return false;
			}

			resultDownloadablePath = prop.getProperty("res_dwn");
			if (resultDownloadablePath == null) {
				displayPropertiesUsage();
				return false;
			}

			toolLoc = prop.getProperty("tools_dir");
			if (toolLoc == null) {
				displayPropertiesUsage();
				return false;
			}

			postProcessingScript = prop.getProperty("post_proc_scr");

			webServerForResults = prop.getProperty("web_srv_results");

			resultPublicServer = prop.getProperty("res_pub_srv");
			if (resultPublicServer == null) {
				displayPropertiesUsage();
				return false;
			}

			remoteServerApkSrcPath = prop.getProperty("apk_rem_path");
			if (remoteServerApkSrcPath == null) {
				displayPropertiesUsage();
				return false;
			}
			remoteServerResultPath = prop.getProperty("res_rem_path");
			if (remoteServerResultPath == null) {
				displayPropertiesUsage();
				return false;
			}

			createEmuScript = prop.getProperty("create_emu");
			if (createEmuScript == null) {
				displayPropertiesUsage();
				return false;
			}

			avdLocation = prop.getProperty("avd_store");
			if (avdLocation == null) {
				displayPropertiesUsage();
				return false;
			}

			apkToolLocation = prop.getProperty("apktool_loc");
			if (apkToolLocation == null) {
				displayPropertiesUsage();
				return false;
			}
			 
			reportEmailUserName = prop.getProperty("report_email_user");
			if(reportEmailUserName == null){
				displayPropertiesUsage();
				return false;
			}
			
			reportEmailPassword = prop.getProperty("report_email_pass");
			if(reportEmailPassword == null){
				displayPropertiesUsage();
				return false;
			}

			customSystemImg = prop.getProperty("system_image");
			if (customSystemImg == null) {
				displayPropertiesUsage();
				return false;
			}

			customRamDiskImg = prop.getProperty("ramdisk_image");
			if (customRamDiskImg == null) {
				displayPropertiesUsage();
				return false;
			}

			emmaLibPath = (new File(sdkInstallPath, "tools/lib/emma.jar"))
					.getAbsolutePath();
			String temp;

			if (prop.getProperty("max_widgets") != null) {

				String[] maxWidgets = prop.getProperty("max_widgets")
						.split(",");
				maxNoOfWidgets = new int[maxWidgets.length];
				int i = 0;
				for (String s : maxWidgets) {
					maxNoOfWidgets[i++] = Integer.parseInt(s);
				}

			}
			temp = prop.getProperty("res_delay");
			if (temp != null) {
				responseDelay = Integer.parseInt(temp);
			}

			temp = prop.getProperty("manual_mode");
			if (temp != null) {
				try {
					isManualMode = Integer.parseInt(temp) == 1?true:false;
				} catch (Exception e) {
				}
			}

			temp = prop.getProperty("verbose_level");
			if (temp != null) {
				verboseLevel = Integer.parseInt(temp);
			}

			temp = prop.getProperty("tch_pct");
			if (temp != null) {
				touchPercentage = Integer.parseInt(temp);
			}

			temp = prop.getProperty("android_target");
			if (temp != null) {
				androidTarget = Integer.parseInt(temp);
			}

			temp = prop.getProperty("max_emu");
			if (temp != null) {
				maxNoOfEmulators = Integer.parseInt(temp);
			}

			temp = prop.getProperty("cov_sam");
			if (temp != null) {
				coverageSamplingInterval = Integer.parseInt(temp);
			}

			temp = prop.getProperty("sml_pct");
			if (temp != null) {
				smallNavigationPercentage = Integer.parseInt(temp);
			}

			temp = prop.getProperty("mjr_pct");
			if (temp != null) {
				majorNavigationPercentage = Integer.parseInt(temp);
			}

			temp = prop.getProperty("trk_pct");
			if (temp != null) {
				trackballPercentage = Integer.parseInt(temp);
			}

			temp = prop.getProperty("delay");
			if (temp != null) {
				delayBetweenEvents = Long.parseLong(temp);
			}
			if (prop.getProperty("event_count") != null) {

				String[] maxEvents = prop.getProperty("event_count").split(",");
				eventCountArray = new int[maxEvents.length];
				int i = 0;
				for (String s : maxEvents) {
					eventCountArray[i++] = Integer.parseInt(s);
				}

			}

			if (prop.getProperty("test_strategy") != null) {

				String[] allStrategies = prop.getProperty("test_strategy")
						.split(",");
				testStrategy.clear();
				for (String s : allStrategies) {
					testStrategy.add(s);
				}

			}
			if (prop.getProperty("sel_stra") != null) {
				String[] allStrategies = prop.getProperty("sel_stra")
						.split(",");
				widgetSelectionStrategy.clear();
				for (String s : allStrategies) {
					widgetSelectionStrategy.add(s);
				}
			}

			if (prop.getProperty("complete_notify") != null) {
				mailNotificationRecipents = prop.getProperty("complete_notify");
			}

			MonkeyRunnerScript = prop.getProperty("monkeyrunner_script");
			if (MonkeyRunnerScript == null) {
				displayPropertiesUsage();
				return false;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private static void displayPropertiesUsage() {
		System.out.println("\nRequired Properties:\n");
		System.out.println("\nMost of These Properties will be filled by the deploy script and will be right, don't modify unless you really know what you are doing\n");
		String mandatoryOptions = "\tapp_dir=<Absolute path to the directory containing all the Applications that need to be tested..>\n"
								+ "\t\tNOTE:\n" 
									+ "\t\t\tIF SOURCES ARE PRESNT THEN EACH APPLICATION SHOULD BE PRESENT IN SEPERATE FOLDER UNDER THE PROVIDED DIRECTORY\n"
									+ "\t\t\tIF THE APPLICATION IS APK THEN APK NEEDS TO BE PRESENT\n" 
								+ "\t[s]instru_setup=<Absolute path to the directory where all the necessary files required for instrumentation are present(will be autoconfigured by deployment script)>\n"
								+ "\twork_dir=<Absolute path to the directory where all the logs and run time stats of the application(s) will be stored>\n"
								+ "\tsdk_install=<The directory in which android sdk is installed>\n"
								+ "\tker_mod=<The directory containing the kernel module that needs to be installed before testing (an ideal place for any kernel level monitoring)>\n"
								+ "\tcreate_emu=<The absolute path to the autogenerated script for creating emulator for testing>\n"
								+ "\tavd_store=<Absolute oath to the directory where all the avds created for testing will be stored>\n"
								+ "\ttools_dir=<The directory containing all the tools required by dynodroid>\n"
								+ "\tsystem_image=<Absolute path to the system.img that needs to be used for emulator creation>\n"
								+ "\tramdisk_image=<Absolute path to the ramdisk.img that needs to be used for emulator creation>\n"
								+ "\tmonkeyrunner_script=<Absolute Path to the monkey runner script that is used to generate the input events>\n"
								+ "\t[a]apktool_loc=<Absoulte Path to the apktool.jar file which will be used to extract the provided apk for AndroidManifest.xml>\n"								
								+ "\tcomplete_notify=<Email-id to be notified after the test run is complete>\n"
								+ "\treport_email_user=<gmail id which is used to send notification mail>\n"
								+ "\treport_email_pass=<password of the notification mail id>\n";
								

		System.out.println(mandatoryOptions);
		System.out.println("Optional Properties:");
		
		System.out.println("\t[s][defVal=100]cov_sam=<Number of events after which the coverage report needs to be generated>");
		System.out.println("\t[defVal=0]manual_mode=<If you want the emulator to run in foreground (allowed values 0 or 1)>");
		System.out.println("\t[defVal=16]max_emu=<Maximum number of emulatores that can be created simultaneosly>");
		System.out.println("\t[s][defVal=3]android_target=<The target number for Gingerbread (run command: android list targets to know the target number)>");
		System.out
		.println("\t[defVal=RandomMonkeyTesting]test_strategy=<Comma Separated Values of Strategies to be used for Testing the provided Apps..all strategies will be applied to all apps>");
		System.out.println("\t\tAvailble Strategies:"+RandomMonkeyTesting.randomTestingStrategy+" (Plain monkey will be used) ,"+WidgetBasedTesting.widgetBasedTestingStrategy+ " (Event abstracton will be used)");
		System.out.println("\t\tOptional Properties For WidgetBasedTesting...");

		System.out
				.println("\t\t [defVal=100]max_widgets=<Maximum Number of Events The need to be used for testing>");
		System.out
				.println("\t\t [defVal=RandomBiasBased]sel_stra=<Comma Separated Values of Widget Selection Strategies to be used for Testing the provided Apps..all strategies will be applied to all apps>");
		
		System.out.println("\t\t  Available Strategies are:"+StrategyFactory.frequencyBased+","+StrategyFactory.randomBased+","+StrategyFactory.randomBiasBased);

		System.out.println("Optional Properties For RandomMonkeyTesting...");
		String monkeyOnlyOptions = "\t[default Value=1]verbose_level=<1-3 where 1 is minimum verbosity>\n"
				+ "\t[defVal=100]event_count=<Comma Separated Values of Number of events to be triggered>\n"
				+ "\t[default Value=15]tch_pct=<Integer indicating the percentage of touch events in the random event generator>\n"
				+ "\t[default Value=25]sml_pct=<Integer indicating the percentage of small navigation events in the random event generator>\n"
				+ "\t[default Value=15]mjr_pct=<Integer indicating the percentage of major navigation  in the random event generator>\n"
				+ "\t[default Value=15]trk_pct=<Integer indicating the percentage of track events in the random event generator>\n"
				+ "\t  For More details on this refer Android Monkey Documentation\n";
		System.out.println(monkeyOnlyOptions);
		
		System.out.println("\nPrefix [a] and [s] indicate that the corresponding properties will be used only for apks and apps with sources respectively");
	}

}
