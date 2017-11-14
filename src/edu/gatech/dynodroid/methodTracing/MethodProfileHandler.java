package edu.gatech.dynodroid.methodTracing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData.IMethodProfilingHandler;
import com.android.ddmlib.ClientData.MethodProfilingStatus;

import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

/***
 * This method listens for the profile complete statistics and writes to a file
 * 
 * @author machiry
 * 
 */
public class MethodProfileHandler /*implements IMethodProfilingHandler*/ {

	/*private static Logger targetLogger = null;
	private static final String TAG = "MethodProfileHandler";
	private static HashMap<Client, String> traceFileDump = new HashMap<Client, String>();
	private static HashMap<Client, Boolean> tracingStatus = new HashMap<Client, Boolean>();
	private static ArrayList<Client> traceComplete = new ArrayList<Client>();

	public MethodProfileHandler(TextLogger logger) throws Exception {
		if (logger != null) {
			targetLogger = logger;
		} else {
			throw new Exception("Provided target is not valid");
		}
	}

	public static void addTracingSupport(Client targetClient,
			String pathforTraceFiles) {
		if (targetClient != null && pathforTraceFiles != null) {
			ClientChangeListener.enableClientChangeListener(targetClient);
			FileUtilities.createDirectory(pathforTraceFiles);
			synchronized (traceFileDump) {
				if (traceFileDump.containsKey(targetClient)) {
					traceFileDump.remove(targetClient);
				} else {
					targetLogger.logInfo(TAG,
							"Added Tracing support for client:" + targetClient);
					;
				}
				traceFileDump.put(targetClient, pathforTraceFiles);
			}
			synchronized (tracingStatus) {
				if (tracingStatus.containsKey(targetClient)) {
					tracingStatus.remove(targetClient);
				} else {
					targetLogger.logInfo(TAG,
							"Added_1 Tracing support for client:"
									+ targetClient);
					;
				}
				tracingStatus.put(targetClient, true);
			}
			getProfilingStatus(targetClient);
			if (targetClient.getClientData().getMethodProfilingStatus() == MethodProfilingStatus.OFF) {
				targetClient.toggleMethodProfiling();
				targetLogger.logInfo(TAG, "Profiling Turned ON for Client:"
						+ targetClient + ", trace data stored at:"
						+ pathforTraceFiles);
			}
		}
	}

	private static void getProfilingStatus(Client targetClient) {
		ClientChangeListener.clearClientState(targetClient);
		targetClient.requestMethodProfilingStatus();
		int maxPoll = 3;
		while (maxPoll > 0) {
			if ((ClientChangeListener.getLatestState(targetClient) & Client.CHANGE_HEAP_ALLOCATION_STATUS) != 0) {
				break;
			}
			maxPoll--;
			try {
				Thread.sleep(500);
			} catch (Exception e) {

			}
		}
	}

	public static void stopTacing(Client targetClient) {
		if (targetClient != null) {
			
			synchronized (tracingStatus) {
				if (tracingStatus.containsKey(targetClient)) {
					tracingStatus.remove(targetClient);
				}
				tracingStatus.put(targetClient, false);
			}
			getProfilingStatus(targetClient);
			if (targetClient.getClientData().getMethodProfilingStatus() == MethodProfilingStatus.ON) {
				targetClient.toggleMethodProfiling();
				targetLogger.logInfo(TAG, "Profiling Turned OFF for Client:"
						+ targetClient);
			}
		}
	}

	@Override
	public void onEndFailure(Client arg0, String arg1) {
		targetLogger.logInfo(TAG, "OnEndFailure for client:" + arg0.toString());

	}

	@Override
	public void onStartFailure(Client arg0, String arg1) {
		targetLogger.logInfo(TAG,
				"onStartFailure for client:" + arg0.toString());

	}

	@Override
	public void onSuccess(String arg0, Client arg1) {
		targetLogger.logInfo(TAG, "OnSuccess for client:" + arg0.toString());

	}
	
	public static boolean isTraceCompleted(Client targetClient){
		boolean retVal = false;
		if(targetClient != null){
			synchronized (traceComplete) {
				retVal = traceComplete.contains(targetClient);
			}
		}
		return retVal;
	}

	@Override
	public void onSuccess(byte[] arg0, Client arg1) {
		boolean isTracingRequired = false;
		String tracingFileLoc = null;
		synchronized (traceFileDump) {
			tracingFileLoc = traceFileDump.get(arg1);
		}
		synchronized (tracingStatus) {
			isTracingRequired = tracingStatus.get(arg1);
		}
		if (tracingFileLoc != null) {
			synchronized (traceComplete) {
				traceComplete.add(arg1);
			}
			arg1.requestMethodProfilingStatus();
			FileOutputStream fos = null;
			File opFile = null;
			try {
				opFile = File.createTempFile(arg1.toString(), ".trace",
						new File(tracingFileLoc));
				targetLogger.logInfo(TAG,
						"Trace Written to file:" + opFile.getAbsolutePath());
				fos = new FileOutputStream(opFile);
				fos.write(arg0);
			} catch (Exception e) {
				targetLogger.logException(TAG, e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						targetLogger.logException(TAG, e);
					}
				}
				getProfilingStatus(arg1);
				if (isTracingRequired) {
					if (arg1.getClientData().getMethodProfilingStatus() == MethodProfilingStatus.OFF) {
						arg1.toggleMethodProfiling();
						targetLogger.logInfo(TAG,
								"Profiling Turned ON Again for Client:" + arg1
										+ ", trace data stored at:"
										+ tracingFileLoc);
					}
				} else {
					if (arg1.getClientData().getMethodProfilingStatus() == MethodProfilingStatus.ON) {
						arg1.toggleMethodProfiling();
						targetLogger.logInfo(TAG,
								"Profiling Turned OFF for Client:" + arg1
										+ ", trace data stored at:"
										+ tracingFileLoc);
					}
				}
			}
		}

	}

	public String getTraceFileLoc(Client target) {
		if (target != null) {
			synchronized (traceFileDump) {
				if (traceFileDump.containsKey(target)) {
					return traceFileDump.get(target);
				}
			}
		}
		return null;
	}*/
}
