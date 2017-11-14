/**
 * 
 */
package edu.gatech.dynodroid.covHandler;

import java.io.File;

import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;

/**
 * @author machiry
 * 
 */
public class EmmaCoverageHandler extends CoverageHandler {
	private String coverageEM;
	private String coverageReportDir;
	private String emmaLib;
	private String htmlReportFileName = "coverage.html";
	private String txtReportFileName = "coverage.txt";

	public EmmaCoverageHandler(String covEM,
			String reportDir, String emLib) {
		assert (covEM != null && covEM.length() > 0 && (new File(covEM))
				.exists());
		assert (reportDir != null && reportDir.length() > 0);
		assert (emLib != null && emLib.length() > 0 && ((new File(emLib))
				.exists()));
		FileUtilities.createDirectory(reportDir);
		this.coverageEM = covEM;
		this.coverageReportDir = reportDir;
		this.emmaLib = emLib;
	}

	public EmmaCoverageHandler(String covEM, String coverageEC,
			String reportDir, String emLib, String htmlReportFile,
			String txtReportFile) {
		assert (covEM != null && covEM.length() > 0 && (new File(covEM))
				.exists());
		assert (reportDir != null && reportDir.length() > 0);
		assert (emLib != null && emLib.length() > 0 && ((new File(emLib))
				.exists()));
		if (!(new File(reportDir)).exists()) {
			ExecHelper.RunProgram("mkdir -p " + reportDir, false);
		}
		this.coverageEM = covEM;
		this.coverageReportDir = reportDir;
		this.emmaLib = emLib;
		this.htmlReportFileName = htmlReportFile;
		this.txtReportFileName = txtReportFile;

	}

	@Override
	public boolean setReportDir(String targetDir) {
		assert (targetDir != null && targetDir.length() > 0);
		try {
			ExecHelper.RunProgram("mkdir -p " + targetDir, false);
			this.coverageReportDir = targetDir;
			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.covHandler.CoverageHandler#computeCoverageReport(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public String computeCoverageReport(String coverageFile, String type) {
		return getCoverageReport(coverageFile, type, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gatech.m3.covHandler.CoverageHandler#computeCoverageReport(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public String computeCoverageReport(String coverageFile, String type,
			String srcPath) {
		assert (srcPath != null && srcPath.length() > 0 && (new File(srcPath))
				.exists());
		return getCoverageReport(coverageFile, type, srcPath);
	}

	private String getCoverageReport(String coverageFile, String type,
			String srcPath) {
		String pathOfCovTxt = null;
		ExecHelper.RunProgram("java -cp " + emmaLib
				+ " emma report -Dreport.html.out.file=" + coverageReportDir
				+ "/" + this.htmlReportFileName + " -r html -in "
				+ this.coverageEM + "," + coverageFile
				+ (srcPath != null ? (" -sourcepath " + srcPath) : "")
				+ " --verbose", true);
		ExecHelper.RunProgram("java -cp " + emmaLib
				+ " emma report -Dreport.txt.out.file=" + coverageReportDir
				+ "/" + this.txtReportFileName + " -r txt -in "
				+ this.coverageEM + "," + coverageFile
				+ (srcPath != null ? (" -sourcepath " + srcPath) : "")
				+ " --verbose", true);
		pathOfCovTxt = coverageReportDir + "/" + this.txtReportFileName;
		if (!(new File(pathOfCovTxt)).exists()) {
			pathOfCovTxt = null;
		}
		return pathOfCovTxt;
	}

}
