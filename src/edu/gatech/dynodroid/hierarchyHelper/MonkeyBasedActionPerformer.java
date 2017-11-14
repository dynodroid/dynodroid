/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.deviceEvent.NonMonkeyEvent;
import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.MonkeyTraceLogger;
import edu.gatech.dynodroid.utilities.TraceLogger;

/**
 * @author machiry
 * 
 */
public class MonkeyBasedActionPerformer extends DeviceActionPerformer {

    public static final String monkeyActionPerformer = "MonkeyBasedActionPerformer";

    private PrintWriter monkeyStream;
    private ServerSocket serverSocket = null;
    private Socket monkeySocket = null;
    private int MONKEY_SOCKET;

    public TraceLogger monkeyTrace;
    public String workingDir;
    public boolean storeScript = false;
    public long sleepTime;
    public Process cmd;
    public final String pyCommand;
    private Thread monkeyOutputReader = null;
    public String monkeyRunnerOutputFile = null;
    public String monkeyRunnerErrorFile = null;
    
    public MonkeyBasedActionPerformer(TraceLogger traceLog, String workDir,
                                      boolean saveScript, ADevice device) {
        this.monkeyTrace = traceLog;
        FileUtilities.createDirectory(workDir);
        this.workingDir = workDir;
        this.monkeyRunnerOutputFile = this.workingDir +"/MonkeyRunnerOutput.txt";
        this.monkeyRunnerErrorFile = this.workingDir + "/MonkeyRunnerError.txt";
        this.storeScript = saveScript;
        sleepTime = PropertyParser.responseDelay;
        String deviceName = device.getDeviceName();
        pyCommand = "monkeyrunner " + 
            PropertyParser.MonkeyRunnerScript +
            " " +
            deviceName;
        MONKEY_SOCKET = 9000 + Integer.parseInt(deviceName.substring(deviceName.length() - 3, deviceName.length()));
        startMonkeyRunner();
    }

    @Override
    public void setWorkingDir(String wd) {
        FileUtilities.createDirectory(wd);
        this.workingDir = wd;
    }

    private synchronized void startMonkeyRunner() {
        try {
            if(cmd != null) {
                cmd.destroy();
            }
            if(monkeySocket != null && !monkeySocket.isClosed()) {
                monkeySocket.close();
            }
            if(serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch(Exception ex) {
            Logger.logException(ex);
        }
        try {
            System.out.println("Waiting for socket connection on port: " + MONKEY_SOCKET);
            serverSocket = new ServerSocket(MONKEY_SOCKET);
            cmd = Runtime.getRuntime().exec(pyCommand);
            monkeySocket = serverSocket.accept();
            monkeyStream = new PrintWriter(monkeySocket.getOutputStream());
            System.out.println("Socket connected on port: " + MONKEY_SOCKET);

            try{
            	if(monkeyOutputReader != null && monkeyOutputReader.isAlive()){
                    monkeyOutputReader.interrupt();
            	}
            } catch(Exception e){
            	
            }
            monkeyOutputReader = new Thread(new ProcessStreamReader(monkeySocket.getInputStream(), monkeyRunnerOutputFile));
            monkeyOutputReader.setName("Monkey Runner OutputReader for:");
            monkeyOutputReader.start();
            
            /*try{
            	if(monkeyErrorReader != null && monkeyErrorReader.isAlive()){
                    monkeyErrorReader.interrupt();
            	}
            } catch(Exception e){
            	
            }*/
            //monkeyErrorReader = new Thread(new ProcessStreamReader(cmd.getErrorStream(), monkeyRunnerErrorFile));
            //monkeyErrorReader.setName("Monkey Runner ErrorReader for:"+cmd.toString());
            //monkeyErrorReader.start();
            
        } catch(Exception ex) {
            Logger.logException(ex);
        }
    }
    
  
    private void performMonkeyCommand(String command) {
        try {
            if(monkeyStream.checkError()) {
                startMonkeyRunner();
            }
            monkeyStream.println(command);
            monkeyStream.flush();
            Thread.sleep(this.sleepTime);
        } catch(Exception ex) {
            Logger.logException(ex);
            startMonkeyRunner();
            try{
                monkeyStream.println(command);
                monkeyStream.flush();
                Thread.sleep(this.sleepTime);
            } catch(Exception ex1){
            	Logger.logException(ex1);
            }
            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.gatech.m3.hierarchyHelper.DeviceActionPerformer#performAction(edu
     * .gatech.m3.hierarchyHelper.DeviceAction,
     * edu.gatech.m3.devHandler.ADevice)
     */
    @Override
	public boolean performAction(IDeviceAction action, ADevice targetDevice) {
        if (action instanceof NonMonkeyEvent) {
            try {
                // TODO: add code to handle broadcast actions
                this.monkeyTrace.addTraceData(
                                              MonkeyTraceLogger.commentCategory, action.toString());
                NonMonkeyEvent targetAction = (NonMonkeyEvent) action;
                if (!targetAction.triggerAction(targetDevice,this)) {
                    Logger.logError("Problem occured while triggering the broadcast event:"
                                    + targetAction.toString());
                } else {
                    return true;
                }
            } catch (Exception e) {
                Logger.logException(e);
            }

        } else {
            ArrayList<String> monkeyActionStrings = action.getMonkeyCommand();
            try {
                if (monkeyActionStrings != null
                    && !monkeyActionStrings.isEmpty()) { 
                    for(String command: monkeyActionStrings) {
                        performMonkeyCommand(command);
                    }
                    addDataToTracefile(monkeyActionStrings);
                    return true;
                }
            } catch (Exception e) {
                Logger.logException(e);
            }
        }

        // TODO Auto-generated method stub
        return false;
    }

    private void addDataToTracefile(ArrayList<String> traceCommands) {
        for (String s : traceCommands) {
            this.monkeyTrace.addTraceLine(s);
        }
    }

    @Override
    public boolean endTracing() {
        performMonkeyCommand("End Data");
        try {
            monkeyStream.close();
        } catch(Exception ex) {
        }

        if (this.monkeyTrace != null) {
            this.monkeyTrace.addTraceData(MonkeyTraceLogger.commentCategory,
                                          "Ending Tracing Data");
            this.monkeyTrace.endTraceLog();
            return true;
        }
        return false;
    }

    @Override
	public TraceLogger getTraceLogger() {
        return this.monkeyTrace;
    }
	
    class ProcessStreamReader implements Runnable{
		
        private BufferedReader inStream = null;
        private String outputFile = null;
		
        public ProcessStreamReader(InputStream targetStream,String targetOutputFile) throws Exception{
            if(targetStream != null && targetOutputFile != null){
                try{
                    inStream = new BufferedReader(new InputStreamReader(
                                                                        targetStream));
                    outputFile = targetOutputFile;
                    FileUtilities.appendLineToFile(outputFile, "Start Of Log");
                } catch(Exception e){
                    throw e;
                }
            }
            else{
                throw new Exception("Input Parameters not Valid for ProcessStreamReader");
            }
        }

        @Override
            public void run() {
            String tempStr = null;
            try{
                while ((tempStr = inStream.readLine()) != null) {
                    FileUtilities.appendLineToFile(outputFile, tempStr);
                }
            } catch(Exception e){
                FileUtilities.appendLineToFile(outputFile, "Problem occured while reading from given stream"+e.getMessage());
            }
        }
		
    }

}
