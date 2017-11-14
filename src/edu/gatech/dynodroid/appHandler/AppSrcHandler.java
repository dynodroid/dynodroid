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
 * This class is the handler for apps for which we have sources This might
 * provide several additional features
 * 
 * @author machiry
 * 
 */
public class AppSrcHandler extends AndroidAppHandler {

	private File appFolderPath = null;
	private ADevice targetTestDevice = null;
	private AppInstrumenter appInstrumenter = null;
	private String workingDir = null;

	public static final String debugInstall = "installd";
	public static final String instrumentInstall = "installi";

	public static final String debugBuild = "debug";
	public static final String instrumentBuild = "instrument";
	private static final Object sSync = new Object();

	public AppSrcHandler(String appSrcPath, ADevice targetDevice,
			String setupDir, String workDir) throws FileNotFoundException,
			InvalidAttributesException {
		assert (appSrcPath != null && (new File(appSrcPath)).exists());
		assert (targetDevice != null);
		assert (setupDir != null);
		assert (workDir != null);

		this.appInstrumenter = new AppInstrumenter(appSrcPath, setupDir);
		this.appFolderPath = new File(appSrcPath);
		this.targetTestDevice = targetDevice;
		FileUtilities.createDirectory(workDir);
		this.workingDir = workDir;

	}

	@Override
	public void setDevice(ADevice dev) {
		assert (dev != null);
		this.targetTestDevice = dev;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#installApp(edu.gatech.m3.
	 * devHandler.ADevice)
	 */
	@Override
	public boolean installApp(ADevice targetDevice, String type) {
		assert (targetDevice != null);
		boolean installSucessfull = false;

		if (isAppInstallTypeValid(type)) {
			// install the app
			String output = ExecHelper
					.RunProgram(
							"ant -f " + this.appFolderPath.getAbsolutePath()
									+ "/build.xml -Dadb.device.arg=\"-s "
									+ targetDevice.getDeviceName() + "\" "
									+ type, true);
			installSucessfull = output.contains("Success");
		}

		return installSucessfull;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#installApp()
	 */
	@Override
	public boolean installApp(String type) {
		boolean installSucessfull = false;
		if (isAppInstallTypeValid(type)) {
			if ((new File(this.appFolderPath.getAbsolutePath() + "/m3_setup"))
					.exists()) {
				File setupContents = new File(
						this.appFolderPath.getAbsolutePath() + "/m3_setup");
				for (File f : setupContents.listFiles()) {
					this.targetTestDevice.putFileInToDevice(
							f.getAbsolutePath(), "/mnt/sdcard/" + f.getName());
				}
			}
			// install the app
			String output = ExecHelper.RunProgram("ant -f "
					+ this.appFolderPath.getAbsolutePath()
					+ "/build.xml -Dadb.device.arg=\"-s "
					+ this.targetTestDevice.getDeviceName() + "\" " + type,
					true);
			installSucessfull = output.contains("Success");
		}

		return installSucessfull;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#instrumentApp()
	 */
	@Override
	public boolean instrumentApp() {
		return this.appInstrumenter.doInstrumentation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#startApp()
	 */
	@Override
	public boolean startApp() {
		try {
			ArrayList<String> output = this.targetTestDevice
					.executeShellCommand("am start -n "
							+ this.appInstrumenter.getAppPackage() + "/"
							+ this.appInstrumenter.getLauncherActivity());
			if (output != null) {
				String op = "";
				for (String s : output) {
					op += s + "\n";
				}
				Logger.logInfo(this.targetTestDevice.getDeviceName()
						+ ":AppRunResult:" + op);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName() + ":"
					+ e.getMessage());
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
		assert (targetDevice != null);
		try {
			ArrayList<String> output = targetDevice
					.executeShellCommand("am start -n "
							+ this.appInstrumenter.getAppPackage() + "/"
							+ this.appInstrumenter.getLauncherActivity());
			if (output != null) {
				String op = "";
				for (String s : output) {
					op += s + "\n";
				}
				Logger.logInfo(targetDevice.getDeviceName() + ":AppRunResult:"
						+ op);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.logException(targetDevice.getDeviceName() + ":"
					+ e.getMessage());
		}
		return false;
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
		assert (targetDevice != null);
		if (this.appInstrumenter.isInstrumentationDone()
				&& unLockDevice(targetDevice)) {
			ArrayList<String> output = targetDevice
					.executeShellCommand("am instrument -e coverage true "
							+ this.appInstrumenter.getAppPackage() + "/"
							+ this.appInstrumenter.getInstrumentationClass());
			return (output != null) && (output.size() == 0);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.m3.appHandler.AndroidAppHandler#startAppInstrument()
	 */
	@Override
	public boolean startAppInstrument() {
		if (this.appInstrumenter.isInstrumentationDone()
				&& unLockDevice(targetTestDevice)) {
			this.targetTestDevice
					.executeShellCommand("am instrument -e coverage true "
							+ this.appInstrumenter.getAppPackage() + "/"
							+ this.appInstrumenter.getInstrumentationClass());
			return true;
		}
		return false;
	}

	@Override
	public boolean buildApp(String type) {
		if (isBuildTypeValid(type)) {
			synchronized (sSync) {
				ExecHelper.RunProgram("android update project --target "
						+ PropertyParser.androidTarget + " --path "
						+ this.appFolderPath.getAbsolutePath(), true);
				String output = ExecHelper.RunProgram("ant -f "
						+ this.appFolderPath.getAbsolutePath()
						+ "/build.xml clean", true);
				output = ExecHelper.RunProgram(
						"ant -f " + this.appFolderPath.getAbsolutePath()
								+ "/build.xml " + type, true);
				ExecHelper.RunProgram(
						"cp coverage.em "
								+ this.appFolderPath.getAbsolutePath()
								+ "/coverage.em", true);
				ExecHelper.RunProgram("rm coverage.em", true);
				return output.contains("BUILD SUCCESSFUL");
			}
		}
		return false;
	}

	@Override
	public boolean getIntermediateCoverage(String targetPath,
			long coverageDumpWaitTime) {
		assert (targetPath != null);
		try {
			cleanupFile(targetPath);
			String targetCoverageFile = "/mnt/sdcard/coverage.ec";
			this.targetTestDevice
					.executeShellCommand("am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE");
			/*
			 * if (this.targetTestDevice.sendSMS("6782345628",
			 * targetCoverageFile)) { Thread.sleep(coverageDumpWaitTime);
			 * boolean retVal = this.targetTestDevice.getFileFromDevice(
			 * targetCoverageFile, targetPath); return retVal; }
			 */
			return this.targetTestDevice.getFileFromDevice(targetCoverageFile,
					targetPath);
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName() + ":"
					+ e.getMessage());
		}
		return false;
	}

	@Override
	public boolean getFinalCoverage(String targetPath, long coverageDumpWaitTime) {
		assert (targetPath != null);
		try {
			cleanupFile(targetPath);
			String targetCoverageFile = "/mnt/sdcard/coverage.ec";
			this.targetTestDevice
					.executeShellCommand("am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE");
			/*
			 * if (this.targetTestDevice.sendSMS("6782345628",
			 * targetCoverageFile)) { Thread.sleep(coverageDumpWaitTime);
			 * boolean retVal = this.targetTestDevice.getFileFromDevice(
			 * targetCoverageFile, targetPath); return retVal; }
			 */
			return this.targetTestDevice.getFileFromDevice(targetCoverageFile,
					targetPath);
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName() + ":"
					+ e.getMessage());
		}
		return false;
	}

	// Private Methods
	private boolean isBuildTypeValid(String buildType) {
		return buildType != null
				&& (buildType.equals(debugBuild) || buildType
						.equals(instrumentBuild));
	}

	private boolean isAppInstallTypeValid(String installType) {
		return installType != null
				&& (installType.equals(debugInstall) || installType
						.equals(instrumentInstall));
	}

	// This method checks whether the provided file path has a file
	// if it has file this method deletes it
	private boolean cleanupFile(String filePath) throws Exception {
		File temp = new File(filePath);
		if (!temp.exists()) {
			temp.createNewFile();
		}
		return temp.delete();
	}

	// Here we just press the back button multiple times
	// assuming that app will be exit after this
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

	@Override
	public boolean uninstallApp() {
		boolean uninstallSucessfull = false;
		try {
			String output = this.targetTestDevice
					.uninstallAppPackage(this.appInstrumenter.getAppPackage());
			output = output.trim();
			uninstallSucessfull = output.equalsIgnoreCase("Success");
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName()
					+ "::UninstallFailure::" + e.getMessage());
		}
		return uninstallSucessfull;
	}

	@Override
	public boolean uninstallApp(ADevice targetDevice) {
		boolean uninstallSucessfull = false;
		assert (targetDevice != null);
		try {
			String output = targetDevice
					.uninstallAppPackage(this.appInstrumenter.getAppPackage());
			output = output.trim();
			uninstallSucessfull = true;// output.equalsIgnoreCase("Success");
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName()
					+ "::UninstallFailure::" + e.getMessage());
		}
		return uninstallSucessfull;
	}

	@Override
	public boolean exitFromApp() {
		return exitFromTheApp(this.targetTestDevice);
	}

	@Override
	public boolean exitFromApp(ADevice targetDevice) {
		return exitFromTheApp(targetDevice);
	}

	@Override
	public String getAppPackage() {
		return this.appInstrumenter.getAppPackage();
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
		return this.appInstrumenter.manifestParser.toString();
	}

	@Override
	public AndroidManifestParser getAndroidManifestParser() {
		return this.appInstrumenter.manifestParser;
	}

	@Override
	public String getAppExtractDir() {
		// TODO Auto-generated method stub
		return this.appFolderPath.getAbsolutePath();
	}

}
