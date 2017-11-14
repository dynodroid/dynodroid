/**
 * 
 */
package edu.gatech.dynodroid.covHandler;

/**
 * @author machiry
 *
 */
public abstract class CoverageHandler {
	
	public static final String coverageTypeAll = "all";
	/***
	 * This method generates the requested type of coverage report from the provided coverage file
	 * and returns the path to the report
	 * @param coverageFile The coverage file collected after testing the app
	 * @type the type of coverage file needs to be generated 
	 * @return Absolute path of the location where coverage report is stored 
	 */
	public abstract String computeCoverageReport(String coverageFile,String type);
	
	/***
	 * This method generates the requested type of coverage report from the provided coverage file
	 * links the report with the sources and returns the path to the report
	 *  
	 * @param coverageFile The coverage file collected after testing the app
	 * @param type the type of coverage file needs to be generated 
	 * @param srcPath The path where app sources are present
	 * @return Absolute path of the location where coverage report is stored 
	 */
	public abstract String computeCoverageReport(String coverageFile,String type,String srcPath);
	
	/***
	 * This method changes the default report directory to the given value
	 * @param targetDir The target directory to be set as report directory
	 * @return true on success / false on failure
	 */
	public abstract boolean setReportDir(String targetDir);
}
