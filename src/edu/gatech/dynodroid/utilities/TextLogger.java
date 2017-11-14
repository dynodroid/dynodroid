package edu.gatech.dynodroid.utilities;

import java.io.File;
import java.io.PrintStream;

public class TextLogger extends Logger {

	private PrintStream logobject = null;

	private static final String infoPrefix = "INFO:";
	private static final String errorPrefix = "ERROR:";
	private static final String exceptionPrefix = "EXCEPTION:";
	private static final String warningPrefix = "WARNING:";

	public TextLogger(String fileName) throws Exception {
		if (fileName != null) {
			try {
				logobject = new PrintStream(new File(fileName));
			} catch (Exception e) {
				super.logException(e);
				throw e;
			}
		}
	}

	@Override
	public void endLog() {
		try {
			logobject.close();
		} catch (Exception e) {
			Logger.logException(e);
		}
	}

	@Override
	public void logInfo(String prefix, String msg) {
		writeMsg(prefix + ":" + infoPrefix + msg);
		Logger.logInfo(prefix+":"+msg);

	}

	@Override
	public void logError(String prefix, String msg) {
		writeMsg(prefix + ":" + errorPrefix + msg);
		Logger.logInfo(prefix+":"+msg);
	}

	@Override
	public void logException(String prefix, String msg) {
		writeMsg(prefix + ":" + exceptionPrefix + msg);
		Logger.logException(prefix+":"+msg);
	}

	@Override
	public synchronized void logException(String prefix, Exception e) {
		logobject
				.println(prefix + ":" + exceptionPrefix + ":" + e.getMessage());
		e.printStackTrace(logobject);
		logobject.flush();
		Logger.logException(e);

	}

	@Override
	public void logWarning(String prefix, String msg) {
		writeMsg(prefix + ":" + warningPrefix + msg);
		Logger.logWarning(prefix+":"+msg);
	}

	private synchronized void writeMsg(String msg) {
		logobject.printf("%s\n", msg);
		logobject.flush();
	}

	@Override
	public void finalize() {
		try {
			logobject.flush();
			logobject.close();
		} catch (Exception e) {
			Logger.logException(e);
		}
	}
}
