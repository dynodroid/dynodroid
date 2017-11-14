package edu.gatech.dynodroid.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * This is ExecHelper that contains methods which help in running a command line
 * @author machiry
 *
 */
public final class ExecHelper {
	
	/***
	 * This method runs the provided command line and returns the output (if getOutput is true) else null
	 * @param javaCommandLine the main command line the t needs to be executed..make sure that all the arguments are properly
	 * formatted and appended as this method doesn't take care of handling arguments separetly 
	 * @param getOutput if this arguemnt is true then the calling method is expecting output,
	 * this will cause this method to be blocked untill the completion of provided command line 
	 * @return Standard output of provided command line 
	 */
	public static String RunProgram(String javaCommandLine, boolean getOutput) {
		Process theProcess = null;
		BufferedReader inStream = null;
		StringBuilder outPut = new StringBuilder();
		String tempStr = null;
		try {
			theProcess = Runtime.getRuntime().exec(javaCommandLine);
		} catch (IOException e) {
			Logger.logException(e);
			return null;
		}

		// read from the called program's standard output stream
		try {
			//if (getOutput) {
				inStream = new BufferedReader(new InputStreamReader(
						theProcess.getInputStream()));
				while ((tempStr = inStream.readLine()) != null) {
					outPut.append(tempStr);
				}
			//}
			try {
				/*theProcess.waitFor();
			} catch (InterruptedException e) {
				
			} finally{*/
				theProcess.destroy();
			} catch(Exception e){
				
			}
		} catch (IOException e) {
			Logger.logException(e);
			return null;
		}
		return outPut.toString();
	}
}
