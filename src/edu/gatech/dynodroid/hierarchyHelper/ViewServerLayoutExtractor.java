/**
 * 
 */
package edu.gatech.dynodroid.hierarchyHelper;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.android.ddmlib.MultiLineReceiver;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.utilities.LocalHostConnection;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.PsdFile;

/**
 * @author machiry
 * 
 * (Contains some code which is part of hierarchyviewer
 */
public class ViewServerLayoutExtractor extends LayoutExtractor {

	private ADevice targetTestDevice;
	private int hostPortNumber;
	private ViewServerInfo localViewServerInfo;
	private boolean isViewServerSetup = false;
	public static final int viewServerPortNumber = 4939;
	public static final String targetHostPortNumberProperty = "host_port";

	public ViewServerLayoutExtractor(ADevice targetDevice, int hostPortNumber) {
		assert (targetDevice != null && (hostPortNumber - 1000) > 0);
		this.targetTestDevice = targetDevice;
		this.hostPortNumber = hostPortNumber;
		//assert (localViewServerInfo != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gatech.dynodroid.hierarchyHelper.LayoutExtractor#setupDevice()
	 */
	@Override
	public boolean setupDevice() {
		if (!this.isViewServerSetup) {
			this.isViewServerSetup = true;
			Logger.logInfo("Setting up fresh view server");
			stopViewServer();
			if (startViewServer(viewServerPortNumber)
					&& this.targetTestDevice.createForward(hostPortNumber,
							viewServerPortNumber)) {
				this.localViewServerInfo = loadViewServerInfo();
				Logger.logInfo("View Server Created fresh");
				return true;
			}
			return false;
		} 
		return true;
	}

	@Override
	public ViewScreen getCurrentScreen(boolean getFullDump) {
		Window currentWindow = getCurrentWindow();
		ViewScreen currentScreen = ViewServerToGenericConverter
				.convertWindowToViewScreen(currentWindow);
		if (getFullDump) {
			ViewNode parentNode = loadWindowData(currentWindow);
			ViewServerToGenericConverter.convertViewNodeToViewElementList(
					parentNode, currentScreen);
		}
		return currentScreen;
	}
	
	private Window getCurrentWindow(){
		Window currentWindow = getFocusedWindow();
		int reTry = 10;
		while (currentWindow == null && reTry > 0) {
			reTry--;
			currentWindow = getFocusedWindow();
			try{
			Thread.sleep((10-reTry)*1000);
			} catch(Exception e){
				//Logger.logException(e);
			}
			Logger.logInfo("IGNORE:Retry.."+reTry);
		}
		return currentWindow;
	}
	
	@Override
	public ViewScreen getCurrentScreen(boolean getFullDump,
			String viewHandlingConfig) {
		Window currentWindow = getCurrentWindow();
		ViewScreen currentScreen = ViewServerToGenericConverter
				.convertWindowToViewScreen(currentWindow);
		if (getFullDump) {
			ViewNode parentNode = loadWindowData(currentWindow);
			ViewServerToGenericConverter.convertViewNodeToViewElementList(
					parentNode, currentScreen,viewHandlingConfig);
		}
		return currentScreen;
	}


	@Override
	public BufferedImage captureScreenShot(ViewElement targetWidget) {
		BufferedImage targetImage = null;
		if (targetWidget != null && targetWidget.inScreen != null) {
			targetImage = captureviewNode(targetWidget.inScreen.nativeObject,
					targetWidget.nativeObject);
		}
		return targetImage;
	}
	
	@Override
	public PsdFile captureCompleteScreenShot(ViewScreen targetScreen) {
		PsdFile targetImage = null;
		if (targetScreen != null) {
			targetImage = captureScreen(targetScreen.nativeObject);
		}
		return targetImage;
	}

	// Private Helper functions

	private BufferedImage captureviewNode(Window win, ViewNode widget) {
		LocalHostConnection localDeviceConnection = null;
		try {

			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.getSocket().setSoTimeout(5000);
			localDeviceConnection.sendCommand("CAPTURE " + win.encode() + " "
					+ widget.toString());
			BufferedImage image = ImageIO.read(localDeviceConnection
					.getSocket().getInputStream());
			return image;
		} catch (Exception e) {
			Logger.logException("bitDroid"
					+ "::Unable to capture data for node " + win
					+ " in window " + win.getTitle() + " on device "
					+ this.targetTestDevice.getDeviceName()
					+ " Exception Message:" + e.getMessage());
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		return null;
	}
	
	private PsdFile captureScreen(Window win) {
		LocalHostConnection localDeviceConnection = null;
		try {

			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.getSocket().setSoTimeout(5000);
			localDeviceConnection.sendCommand("CAPTURE_LAYERS " + win.encode());
			DataInputStream in = new DataInputStream(localDeviceConnection.getSocket().getInputStream());

            int width = in.readInt();
            int height = in.readInt();

            PsdFile psd = new PsdFile(width, height);

            while (readLayer(in, psd)) {
            }           

			return psd;
		} catch (Exception e) {
			Logger.logException("bitDroid"
					+ "::Unable to capture data for node " + win
					+ " in window " + win.getTitle() + " on device "
					+ this.targetTestDevice.getDeviceName()
					+ " Exception Message:" + e.getMessage());
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		return null;
	}
	
	private boolean readLayer(DataInputStream in, PsdFile psd) {
        try {
            if (in.read() == 2) {
                //System.out.println("Found end of layers list");
                return false;
            }
            String name = in.readUTF();
            //System.out.println("name = " + name);
            boolean visible = in.read() == 1;
            int x = in.readInt();
            int y = in.readInt();
            int dataSize = in.readInt();

            byte[] data = new byte[dataSize];
            int read = 0;
            while (read < dataSize) {
                read += in.read(data, read, dataSize - read);
            }

            ByteArrayInputStream arrayIn = new ByteArrayInputStream(data);
            BufferedImage chunk = ImageIO.read(arrayIn);

            // Ensure the image is in the right format
            BufferedImage image = new BufferedImage(chunk.getWidth(), chunk.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(chunk, null, 0, 0);
            g.dispose();

            psd.addLayer(name, image, new Point(x, y), visible);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	private ViewServerInfo loadViewServerInfo() {
		int i = -1;
		int j = -1;
		LocalHostConnection localDeviceConnection = null;
		try {
			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.sendCommand("SERVER");
			String str1 = localDeviceConnection.getInputStream().readLine();
			if (str1 != null)
				i = Integer.parseInt(str1);
		} catch (Exception localException1) {
			// TODO:
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		localDeviceConnection = null;
		try {
			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.sendCommand("PROTOCOL");
			String str2 = localDeviceConnection.getInputStream().readLine();
			if (str2 != null)
				j = Integer.parseInt(str2);
		} catch (Exception localException2) {
			// TODO:
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		if ((i == -1) || (j == -1)) {
			return null;
		}
		ViewServerInfo localViewServerInfo = new ViewServerInfo(i, j);
		return localViewServerInfo;
	}

	private Window getFocusedWindow() {
		LocalHostConnection localDeviceConnection = null;
		try {
			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.sendCommand("GET_FOCUS");
			String str = localDeviceConnection.getInputStream().readLine();
			if ((str == null) || (str.length() == 0)) {
				return null;
			}
			int i = (int) Long
					.parseLong(str.substring(0, str.indexOf(' ')), 16);
			return new Window(this.targetTestDevice, str.substring(str
					.indexOf(' ') + 1), i);
		} catch (Exception localException) {
			Logger.logException("Problem while trying to get the current focused window:"
					+ localException.getMessage());
			Logger.logException(localException);
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		return null;
	}

	private ViewNode loadWindowData(Window paramWindow) {
		LocalHostConnection localDeviceConnection = null;
		try {
			localDeviceConnection = new LocalHostConnection(hostPortNumber);
			localDeviceConnection.sendCommand("DUMP " + paramWindow.encode());
			BufferedReader localBufferedReader = localDeviceConnection
					.getInputStream();
			ViewNode localViewNode1 = null;
			int i = -1;
			String str;
			while (((str = localBufferedReader.readLine()) != null)
					&& (!"DONE.".equalsIgnoreCase(str))) {
				int j = 0;
				while (str.charAt(j) == ' ') {
					j++;
				}
				while (j <= i) {
					localViewNode1 = localViewNode1.parent;
					i--;
				}
				localViewNode1 = new ViewNode(paramWindow, localViewNode1,
						str.substring(j));
				i = j;
			}
			if (localViewNode1 == null) {
				return null;
			}
			while (localViewNode1.parent != null) {
				localViewNode1 = localViewNode1.parent;
			}
			if (localViewServerInfo != null) {
				localViewNode1.protocolVersion = localViewServerInfo.protocolVersion;
			}
			ViewNode localViewNode2 = localViewNode1;
			return localViewNode2;
		} catch (Exception localException) {
			Logger.logException("bitDroid"
					+ "Unable to load window data for window "
					+ paramWindow.getTitle() + " on device "
					+ paramWindow.getDevice());
		} finally {
			if (localDeviceConnection != null) {
				localDeviceConnection.close();
			}
		}
		return null;
	}

	public HashSet<ViewNode> getAllChildren(ViewNode topParent,
			HashSet<ViewNode> children) {
		if (topParent != null) {
			if (topParent.children == null || topParent.children.size() == 0) {
				children.add(topParent);
			} else {
				for (ViewNode child : topParent.children) {
					getAllChildren(child, children);
				}
			}
			return children;
		}
		return children;
	}

	private boolean stopViewServer() {
		boolean[] arrayOfBoolean = new boolean[1];
		try {
			ArrayList<String> shellOutput = this.targetTestDevice
					.executeShellCommand(buildStopServerShellCommand());
			BooleanResultReader reader = new BooleanResultReader(arrayOfBoolean);
			String[] outputLines = new String[shellOutput.size()];
			reader.processNewLines(shellOutput.toArray(outputLines));
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName()
					+ ":Exception while stoping the view server:"
					+ e.getMessage());
		}
		return arrayOfBoolean[0];
	}

	private boolean startViewServer(int paramInt) {
		boolean[] arrayOfBoolean = new boolean[1];
		try {
			ArrayList<String> shellOutput = this.targetTestDevice
					.executeShellCommand(buildStartServerShellCommand(paramInt));
			BooleanResultReader reader = new BooleanResultReader(arrayOfBoolean);
			String[] outputLines = new String[shellOutput.size()];
			reader.processNewLines(shellOutput.toArray(outputLines));
		} catch (Exception e) {
			Logger.logException(this.targetTestDevice.getDeviceName()
					+ ":Exception while starting the view server:"
					+ e.getMessage());
		}
		return arrayOfBoolean[0];
	}

	private String buildStartServerShellCommand(int paramInt) {
		return String.format("service call window %d i32 %d", new Object[] {
				Integer.valueOf(1), Integer.valueOf(paramInt) });
	}

	private String buildStopServerShellCommand() {
		return String.format("service call window %d",
				new Object[] { Integer.valueOf(2) });
	}

	private static class BooleanResultReader extends MultiLineReceiver {
		private final boolean[] mResult;

		public BooleanResultReader(boolean[] paramArrayOfBoolean) {
			this.mResult = paramArrayOfBoolean;
		}

		@Override
		public void processNewLines(String[] paramArrayOfString) {
			if (paramArrayOfString.length > 0) {
				Pattern localPattern = Pattern
						.compile(".*?\\([0-9]{8} ([0-9]{8}).*");
				Matcher localMatcher = localPattern
						.matcher(paramArrayOfString[0]);
				if ((localMatcher.matches())
						&& (Integer.parseInt(localMatcher.group(1)) == 1))
					this.mResult[0] = true;
			}
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
	}

	public static class ViewServerInfo {
		public final int protocolVersion;
		public final int serverVersion;

		ViewServerInfo(int paramInt1, int paramInt2) {
			this.protocolVersion = paramInt2;
			this.serverVersion = paramInt1;
		}
	}
}
