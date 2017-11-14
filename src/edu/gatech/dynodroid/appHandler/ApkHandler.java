/**
 * 
 */
package edu.gatech.dynodroid.appHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.naming.directory.InvalidAttributesException;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * @author machiry
 * 
 */
public class ApkHandler extends AndroidAppHandler {

	private File apkFilePath = null;
	private ADevice targetTestDevice = null;
	private String workingDir = null;
	private String apkExtractDir = null;
	private AndroidManifestParser manParser = null;

	public ApkHandler(String apkPath, ADevice targetDevice, String setupDir,
			String workDir) throws FileNotFoundException,
			InvalidAttributesException {
		assert (apkPath != null && (new File(apkPath)).exists());
		assert (targetDevice != null);
		assert (setupDir != null);
		assert (workDir != null);

		if (apkPath == null || !(new File(apkPath)).exists()) {
			throw new FileNotFoundException("Provided apk not found");
		}
		if (PropertyParser.apkToolLocation == null
				|| !(new File(PropertyParser.apkToolLocation)).exists()) {
			throw new FileNotFoundException("Provided apktool.jar not found");
		}
		FileUtilities.createDirectory(workDir);

		this.apkFilePath = new File(apkPath);
		this.apkExtractDir = workDir + "/ApkExtractDir";
		
		if(!ApkHandler.extractApk(this.apkFilePath, this.apkExtractDir)) {
			throw new FileNotFoundException(
					"Unable to find AndroidManifest.xml in the apk extracted location");
		}
		
		this.manParser = new AndroidManifestParser(this.apkExtractDir
				+ "/AndroidManifest.xml");

		this.targetTestDevice = targetDevice;

		this.workingDir = workDir;

	}
	
	/*
	 * Extracts the APK file and checks for AndroidManifest.xml.
	 * 
	 * return true if success else false
	 */
	private static boolean extractApk(File apkFile,String apkExtractDir){
		boolean is_success = false;
		String output = ExecHelper.RunProgram("java -jar "
				+ PropertyParser.apkToolLocation + " -q d -s -f -o " + apkExtractDir + " "
				+ apkFile.getAbsolutePath(), true);
		is_success = (new File(apkExtractDir + "/AndroidManifest.xml")).exists();
		if (!is_success) {
			Logger.logWarning("apk tool failed to extract complete APK:" + output);
			Logger.logInfo("Trying to extract apk without resources");
			ExecHelper.RunProgram("java -jar "
					+ PropertyParser.apkToolLocation + " -q d -s -f -r -o " + apkExtractDir + " "
					+ apkFile.getAbsolutePath(), true);
			is_success = (new File(apkExtractDir + "/AndroidManifest.xml")).exists();
			try {
				//Make sure that Manifest is readable and nothing happened while extraction.
				AndroidManifestParser tempMan = new AndroidManifestParser(apkExtractDir + "/AndroidManifest.xml");
				is_success = tempMan.getAppPackage() != null;
			} catch(Exception e){
				is_success = false;
			}
		}
		if(!is_success) {
			Logger.logError("Apktool Failed to extract APK. Try to run APK tool manually to see the error");
		}
		return is_success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#setDevice(edu.gatech.m3.devHandler
	 * .ADevice)
	 */
	@Override
	public void setDevice(ADevice dev) {
		if (dev != null) {
			this.targetTestDevice = dev;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#installApp(edu.gatech.m3.
	 * devHandler.ADevice, java.lang.String)
	 */
	@Override
	public boolean installApp(ADevice targetDevice, String type) {
		if (targetDevice != null) {
			return targetDevice.installApk(this.apkFilePath.getAbsolutePath(),
					true);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#installApp(java.lang.String)
	 */
	@Override
	public boolean installApp(String type) {
		return this.targetTestDevice.installApk(
				this.apkFilePath.getAbsolutePath(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#instrumentApp()
	 */
	@Override
	public boolean instrumentApp() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#startApp()
	 */
	@Override
	public boolean startApp() {
		return startA(targetTestDevice);
	}

	private boolean startA(ADevice tdev) {
		if (unLockDevice(tdev)) {
			if (this.manParser.getInstrumentation() != null) {
				tdev.executeShellCommand("am instrument -e coverage true "
						+ this.manParser.getAppPackage() + "/"
						+ this.manParser.getInstrumentation());
				return true;
			} else {
				/*Logger.logInfo("am start -n "
						+ this.manParser.getAppPackage() + "/"
						+ this.manParser.getLauncherActivity());*/
				tdev.executeShellCommand("am start -n "
						+ this.manParser.getAppPackage() + "/"
						+ this.manParser.getLauncherActivity());
				
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#startApp(edu.gatech.m3.devHandler
	 * .ADevice)
	 */
	@Override
	public boolean startApp(ADevice targetDevice) {
		return startA(targetDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#startAppInstrument(edu.gatech
	 * .m3.devHandler.ADevice)
	 */
	@Override
	public boolean startAppInstrument(ADevice targetDevice) {
		return startApp(targetDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#startAppInstrument()
	 */
	@Override
	public boolean startAppInstrument() {
		// TODO Auto-generated method stub
		return startApp();
	}

	private boolean uninstallAppP(ADevice tdev) {
		boolean uninstallSucessfull = false;
		try {
			String output = tdev.uninstallAppPackage(this.manParser
					.getAppPackage());
			output = output.trim();
			uninstallSucessfull = output.equalsIgnoreCase("Success");
		} catch (Exception e) {
			Logger.logException(tdev.getDeviceName() + "::UninstallFailure::"
					+ e.getMessage());
		}
		return uninstallSucessfull;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#uninstallApp()
	 */
	@Override
	public boolean uninstallApp() {
		return uninstallAppP(this.targetTestDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#uninstallApp(edu.gatech.m3
	 * .devHandler.ADevice)
	 */
	@Override
	public boolean uninstallApp(ADevice targetDevice) {
		return uninstallAppP(targetDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#exitFromApp()
	 */
	@Override
	public boolean exitFromApp() {
		// TODO Auto-generated method stub
		return exitFromTheApp(targetTestDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.appHandler.AndroidAppHandler#exitFromApp(edu.gatech.m3.
	 * devHandler.ADevice)
	 */
	@Override
	public boolean exitFromApp(ADevice targetDevice) {
		// TODO Auto-generated method stub
		return exitFromTheApp(targetDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#getAppPackage()
	 */
	@Override
	public String getAppPackage() {
		return this.manParser.getAppPackage();
	}

	private boolean exitFromTheApp(ADevice tdev) {
		boolean retVal = false;
		try {

			File monkeyScriptFile = File.createTempFile("monkey_exit", ".txt",
					new File(this.workingDir));
			ArrayList<String> monkeyScript = new ArrayList<String>();
			monkeyScript.add("count = " + 3);
			monkeyScript.add("speed = 1000");
			monkeyScript.add("start data >>");

			monkeyScript.add("UserWait(" + 100 + ")");

			// TODO: don't use hard coded value 15..instead parameterize this
			for (int i = 0; i < 15; i++) {
				monkeyScript.add("DispatchKey(5000,5000,0,4,0,0,0,0)");
				monkeyScript.add("DispatchKey(5050,5050,1,4,0,0,0,0)");
			}
			FileUtilities.appendLinesToFile(monkeyScriptFile.getAbsolutePath(),
					monkeyScript);
			String onDeviceFile = "/mnt/sdcard/" + monkeyScriptFile.getName();
			tdev.putFileInToDevice(monkeyScriptFile.getAbsolutePath(),
					onDeviceFile);

			tdev.executeShellCommand("monkey -v -v -v -f " + onDeviceFile
					+ " 1");
			tdev.executeShellCommand("rm " + onDeviceFile);
			retVal = true;
		} catch (Exception e) {
			Logger.logException(tdev.getDeviceName() + ":ExitingApp:"
					+ e.getMessage());
		}
		return retVal;
	}

	private boolean unLockDevice(ADevice tdev) {
		boolean retVal = false;
		try {
			File tempFile = File.createTempFile("monkey_unlock", ".txt",
					new File(this.workingDir));
			PrintStream printStream = new PrintStream(new FileOutputStream(
					tempFile));
			printStream.println("count = " + 3);
			printStream.println("speed = 1000");
			printStream.println("start data >>");
			printStream.println("UserWait(" + 100 + ")");
			printStream.println("DispatchKey(5000,5000,0,82,0,0,0,0)");
			printStream.close();
			String onDevFile = (new File("/mnt/sdcard/" + tempFile.getName()))
					.getAbsolutePath();
			tdev.putFileInToDevice(tempFile.getAbsolutePath(), onDevFile);

			tdev.executeShellCommand("monkey -v -v -v -f " + onDevFile + " 1");
			tdev.executeShellCommand("rm " + onDevFile);
			retVal = true;
		} catch (Exception e) {
			Logger.logException(tdev.getDeviceName() + ":Unlocking the device:"
					+ e.getMessage());
		}
		return retVal;
	}

	@Override
	public String getManifestInfo() {
		return this.manParser.toString();
	}

	@Override
	public AndroidManifestParser getAndroidManifestParser() {
		return this.manParser;
	}

	@Override
	public String getAppExtractDir() {
		// TODO Auto-generated method stub
		return this.apkExtractDir;
	}

}
