package edu.gatech.dynodroid.appHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.naming.directory.InvalidAttributesException;

import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

public class AppInstrumenter {
	// Private Fields
	private String appFolder;
	private String setupFolder;
	private File manifestFile;
	public AndroidManifestParser manifestParser;
	private boolean takeBackup = true;

	private String targetInstrumentPackageName;
	private String finalInstrumentationClass;
	private String appPackage;
	private String appLauncherActivity;
	private String targetCoverageTriggerReceiver;

	private File emmaInstrumentationTemplateFile;
	private File finishListnerTemplateFile;
	private File instrumentedActivityTemplateFile1;
	private File instrumentedActivityTemplateFile2;
	private File smsInstrumentedReceiverFile;

	// Private static fields
	private final static String manifestTagName = "manifest";
	private final static String androidManifest = "AndroidManifest.xml";
	private final static String applicationTagName = "application";
	private final static String backupSuffix = ".backup";
	private final static String instrumentationTagFormat = "<instrumentation android:handleProfiling=\"true\""
			+ " android:label=\"EmmaInstrumentation\""
			+ " android:name=\"%s\""
			+ " android:targetPackage=\"%s\" />";
	private final static String instrumentationActivityFormat = "<activity android:name=\"%s\""
			+ " android:label=\"EmmaInstrumentationActivity\"/>";

	private final static String smsReceiverFormat = "<receiver android:name=\"%s\">\n"
			+ "<intent-filter>\n"
			+ "<action android:name=\"edu.gatech.m3.emma.COLLECT_COVERAGE\"/>\n"
			+ "</intent-filter>\n" + "</receiver>";

	private final static String[] requiredPermissions = new String[] {
			"<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/>" };

	private final static String emmaInstrumentationTemplateFileName = "EmmaInstrumentation.java";
	private final static String finishListnerTemplateFileName = "FinishListener.java";
	private final static String instrumentedActivityTemplateFileName1 = "InstrumentedActivity.java1";
	private final static String instrumentedActivityTemplateFileName2 = "InstrumentedActivity.java2";
	private final static String emmaSMSReceiverTemplateFile = "SMSInstrumentedReceiver.java";

	// Public static fields
	public final static String coverageFilePath = "/mnt/sdcard/coverage.ec";

	// Constructors
	public AppInstrumenter(String appf, String setupFol)
			throws FileNotFoundException, InvalidAttributesException {
		commonSetup(appf, setupFol);
		if (!this.validateParameters()) {
			throw new InvalidAttributesException(
					"Provided Data for App Instrumentation is invalid..refer log for more details");
		}
	}

	public AppInstrumenter(String appf, String setupFol, boolean backup)
			throws FileNotFoundException, InvalidAttributesException {
		this.takeBackup = backup;
		commonSetup(appf, setupFol);
		if (!this.validateParameters()) {
			throw new InvalidAttributesException(
					"Provided Data for App Instrumentation is invalid..refer log for more details");
		}
	}

	// Public Access Methods
	public String getAppPackage() {
		return this.appPackage;
	}

	public String getInstrumentationClass() {
		return this.finalInstrumentationClass;
	}

	public String getLauncherActivity() {
		return this.appLauncherActivity;
	}

	public boolean isInstrumentationDone() {
		return this.finalInstrumentationClass != null;
	}
	
	public String getCoverageTriggerReceiver(){
		return this.targetCoverageTriggerReceiver;
	}

	// Private Methods
	private boolean validateParameters() {
		boolean retVal = true;
		if (!(new File(setupFolder)).exists()) {
			Logger.logError("The provided setup folder:" + setupFolder
					+ " is not present");
			retVal = false;
		}

		// Check all the required files exists
		if (!this.emmaInstrumentationTemplateFile.exists()) {
			Logger.logError("File:"
					+ this.emmaInstrumentationTemplateFile.getAbsolutePath()
					+ " is not present");
			retVal = false;
		}

		if (!this.finishListnerTemplateFile.exists()) {
			Logger.logError("File:"
					+ this.finishListnerTemplateFile.getAbsolutePath()
					+ " is not present");
			retVal = false;
		}

		if (!this.instrumentedActivityTemplateFile1.exists()) {
			Logger.logError("File:"
					+ this.instrumentedActivityTemplateFile1.getAbsolutePath()
					+ " is not present");
			retVal = false;
		}

		if (!this.instrumentedActivityTemplateFile2.exists()) {
			Logger.logError("File:"
					+ this.instrumentedActivityTemplateFile2.getAbsolutePath()
					+ " is not present");
			retVal = false;
		}

		if (!this.smsInstrumentedReceiverFile.exists()) {
			Logger.logError("File:"
					+ this.smsInstrumentedReceiverFile.getAbsolutePath()
					+ " is not present");
			retVal = false;
		}

		if ((this.appPackage = this.manifestParser.getAppPackage()) == null) {
			Logger.logError("Unable to get app package from AndroidManifest.xml");
			retVal = false;
		}

		if ((this.appLauncherActivity = this.manifestParser
				.getLauncherActivity()) == null) {
			Logger.logError("Unable to get Main Activity from AndroidManifest.xml");
			retVal = false;
		}
		this.finalInstrumentationClass = this.manifestParser
				.getInstrumentation();
		return retVal;
	}

	private void commonSetup(String appf, String setupFol)
			throws FileNotFoundException {
		this.appFolder = appf;
		this.setupFolder = setupFol;
		this.manifestFile = new File(this.appFolder + "/" + androidManifest);
		this.manifestParser = new AndroidManifestParser(
				this.manifestFile.getAbsolutePath());
		this.emmaInstrumentationTemplateFile = new File(this.setupFolder + "/"
				+ emmaInstrumentationTemplateFileName);
		this.finishListnerTemplateFile = new File(this.setupFolder + "/"
				+ finishListnerTemplateFileName);
		this.instrumentedActivityTemplateFile1 = new File(this.setupFolder
				+ "/" + instrumentedActivityTemplateFileName1);
		this.instrumentedActivityTemplateFile2 = new File(this.setupFolder
				+ "/" + instrumentedActivityTemplateFileName2);
		this.smsInstrumentedReceiverFile = new File(this.setupFolder + "/"
				+ emmaSMSReceiverTemplateFile);
	}

	public boolean doInstrumentation() {
		return doInstrumentation(false);
	}

	// Public Methods
	public boolean doInstrumentation(boolean force) {
		boolean retVal = false;

		if (!force && (this.finalInstrumentationClass != null)) {
			return true;
		}

		try {

			// Take the backup if required
			if (this.takeBackup
					&& !FileUtilities.copyfile(manifestFile, new File(
							manifestFile.getAbsolutePath() + backupSuffix),
							false)) {

				Logger.logError("Unable to create backup of the Manifest File");
				return false;
			}

			// Add the required files to the project
			if (!addTheInstrumentationFiles()) {
				Logger.logError("Problem occured while adding the instrumentation files to the application directory");
				return false;
			}

			// Change the Android Manifest file to consume the changes
			if (!changeManifestFile()) {
				Logger.logError("Problem occured while modifying manifest.xml");
				return false;
			}

			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	// Private Methods
	private boolean addTheInstrumentationFiles() {
		boolean retVal = false;
		try {
			String mainPackageName = this.manifestParser.getAppPackage();
			String appSrcFolder = this.appFolder + "/src";
			String targetPackageName = FileUtilities.findNonExistingDirectory(
					appSrcFolder, mainPackageName + ".EmmaInstrument");
			this.targetInstrumentPackageName = targetPackageName;
			String targetFolderPath = FileUtilities
					.convertJavaPackageNameToPath(appSrcFolder,
							targetPackageName);
			// We found the target package path.
			// Create the folder in the src path
			if (!(new File(targetFolderPath)).mkdirs()) {
				Logger.logError("Unable to create the target directory:"
						+ targetFolderPath);
				// Failed to create the directory
				return false;
			}

			String importPackageHeader = "package " + targetPackageName + ";";
			// Create EmmaInstrumentation.java
			File emmaInstrumentationsFile = new File(targetFolderPath + "/"
					+ "EmmaInstrumentation.java");
			FileUtilities.appendLineToFile(
					emmaInstrumentationsFile.getAbsolutePath(),
					importPackageHeader);
			FileUtilities.copyfile(this.emmaInstrumentationTemplateFile,
					emmaInstrumentationsFile, true);

			// Create FinishListner.java
			File finishListnerFile = new File(targetFolderPath + "/"
					+ "FinishListener.java");
			FileUtilities.appendLineToFile(finishListnerFile.getAbsolutePath(),
					importPackageHeader);
			FileUtilities.copyfile(this.finishListnerTemplateFile,
					finishListnerFile, true);

			// Create InstrumentedActivity.java
			File instrumentedActivityFile = new File(targetFolderPath + "/"
					+ "InstrumentedActivity.java");

			FileUtilities.appendLineToFile(
					instrumentedActivityFile.getAbsolutePath(),
					importPackageHeader);

			FileUtilities.copyfile(this.instrumentedActivityTemplateFile1,
					instrumentedActivityFile, true);

			FileUtilities.appendLineToFile(
					instrumentedActivityFile.getAbsolutePath(),
					"public class InstrumentedActivity extends "
							+ this.manifestParser.getLauncherActivity() + " {");

			FileUtilities.copyfile(this.instrumentedActivityTemplateFile2,
					instrumentedActivityFile, true);

			// Create SMSInstrumentedReceiver.java
			File smsReceiverFile = new File(targetFolderPath + "/"
					+ "SMSInstrumentedReceiver.java");
			FileUtilities.appendLineToFile(smsReceiverFile.getAbsolutePath(),
					importPackageHeader);
			FileUtilities.copyfile(this.smsInstrumentedReceiverFile,
					smsReceiverFile, true);
			this.targetCoverageTriggerReceiver = targetInstrumentPackageName+".SMSInstrumentedReceiver";

			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	private boolean changeManifestFile() {
		boolean retVal = false;
		try {
			ArrayList<String> xmlStringsToBeAdded = new ArrayList<String>();

			String modifiedManifestFile = this.appFolder
					+ "/AndroidManifest.xml.modified";

			AndroidManifestParser manParser = new AndroidManifestParser(
					manifestFile.getAbsolutePath());

			String targetInstrumentationString = String.format(
					instrumentationTagFormat, this.targetInstrumentPackageName
							+ ".EmmaInstrumentation",
					this.manifestParser.getAppPackage());
			this.finalInstrumentationClass = this.targetInstrumentPackageName
					+ ".EmmaInstrumentation";
			xmlStringsToBeAdded.add(targetInstrumentationString);
			for (int i = 0; i < requiredPermissions.length; i++) {
				xmlStringsToBeAdded.add(requiredPermissions[i]);
			}
			manParser.addInsideTag(xmlStringsToBeAdded, manifestTagName,
					modifiedManifestFile);

			manParser = new AndroidManifestParser(modifiedManifestFile);

			String targetInstrumentationActivityString = String.format(
					instrumentationActivityFormat,
					this.targetInstrumentPackageName + ".InstrumentedActivity");

			String targetSMSReceiverString = String.format(smsReceiverFormat,
					this.targetInstrumentPackageName
							+ ".SMSInstrumentedReceiver");
			
			xmlStringsToBeAdded.clear();
			xmlStringsToBeAdded.add(targetSMSReceiverString);
			xmlStringsToBeAdded.add(targetInstrumentationActivityString);
			manParser.addInsideTag(xmlStringsToBeAdded, applicationTagName,
					modifiedManifestFile);
			retVal = FileUtilities.copyfile(new File(modifiedManifestFile),
					this.manifestFile, false);
			if (!retVal) {
				Logger.logError("Problem occured while trying to copy modfied file to original location");
			}

		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	// Static Methods
	// These are basic validation methods

	/*
	 * This method validates the provided app folder and checks for valid
	 * manifest.xml
	 */
	public static boolean isAppFolderGood(String appFolder) {
		boolean isGood = false;
		File tempFile = new File(appFolder);
		if (tempFile.exists()) {
			File srcFolder = new File(tempFile.getAbsolutePath() + "/src");
			if (srcFolder.exists()) {
				try {
					AndroidManifestParser parser = new AndroidManifestParser(
							tempFile.getAbsolutePath() + "/" + androidManifest);
					if (parser.getAppPackage() != null
							&& parser.getLauncherActivity() != null) {
						isGood = true;
					}
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		}

		return isGood;
	}

	/*
	 * This method checks the provided setup folder for various required files
	 */
	public static boolean isSetupDirGood(String setupDir) {
		ArrayList<String> filesToBeChecked = new ArrayList<String>();
		filesToBeChecked.add(emmaInstrumentationTemplateFileName);
		filesToBeChecked.add(finishListnerTemplateFileName);
		filesToBeChecked.add(instrumentedActivityTemplateFileName1);
		filesToBeChecked.add(instrumentedActivityTemplateFileName2);

		for (String file : filesToBeChecked) {
			File tempFile = new File(setupDir + "/" + file);
			if (!tempFile.exists()) {
				return false;
			}
		}

		return true;
	}
}
