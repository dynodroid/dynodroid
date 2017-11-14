package edu.gatech.dynodroid.clients;

import java.util.ArrayList;
import java.util.Random;

import edu.gatech.dynodroid.devHandler.ADevice;
import edu.gatech.dynodroid.deviceEvent.KeyEvent;
import edu.gatech.dynodroid.deviceEvent.PhoneStateChanged;
import edu.gatech.dynodroid.hierarchyHelper.IDeviceAction;
import edu.gatech.dynodroid.hierarchyHelper.ViewElement;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.Pair;

public class AudioServiceMonitoring extends MonitoringClient {
	private static final String getAudioFocusMethod = "requestAudioFocus";
	private static final String abandonAudioFocusMethod = "abandonAudioFocus";
	private static final String registerMediaButtonEventReceiverMethod = "registerMediaButtonEventReceiver";
	private static final String unregisterMediaButtonEventReceiverMethod = "unregisterMediaButtonEventReceiver";

	public AudioServiceMonitoring(ADevice targetDevice) throws Exception {
		if (targetDevice != null) {
			this.finalTargetDevice = targetDevice;
			this.filterString = "M3AudioManager";
			Logger.logInfo("AudioServiceMonitoring Initialized for:"
					+ targetDevice.toString());
		} else {
			throw new Exception("Unable to initialize AudioServiceMonitoring");
		}
	}

	@Override
	public boolean consume(String entry) {
		if (this.toMonitor && entry != null) {
			if (entry.contains(filterString)) {
				if (entry.contains(Integer.toString(this.targetAppUid))) {
					synchronized (logEntries) {
						logEntries.add(entry);
					}

					if (feedBack != null) {
						OperationDesc targetDesc = OperationDesc
								.parseLogLine(entry);
						if (targetDesc != null) {
							if (targetDesc.method.equals(getAudioFocusMethod)) {
								try {
									PhoneStateChanged targetEvent = new PhoneStateChanged(
											targetDesc.data,
											new ArrayList<String>());
									this.feedBack
											.addNonUiDeviceAction(new Pair<ViewElement, IDeviceAction>(
													null, targetEvent));

								} catch (Exception e) {
									Logger.logError("Problem occured while trying to add broadcast event to the list:"
											+ e.getMessage());
								}

							}
							if (targetDesc.method
									.equals(abandonAudioFocusMethod)) {
								try {
									PhoneStateChanged targetEvent = new PhoneStateChanged(
											targetDesc.data,
											new ArrayList<String>());
									this.feedBack
											.removeNonUiDeviceAction(new Pair<ViewElement, IDeviceAction>(
													null, targetEvent));

								} catch (Exception e) {
									Logger.logError("Problem occured while trying to remove broadcast event form list:"
											+ e.getMessage());
								}
							}
							if (targetDesc.method
									.equals(registerMediaButtonEventReceiverMethod)) {
								try {
									// We pick a random media key
									Random rand = new Random();
									KeyEvent targetEvent = new KeyEvent(
											targetDesc.data,
											KeyEvent.mediaButtons.get(rand
													.nextInt(KeyEvent.mediaButtons
															.size())));
									this.feedBack
											.addNonUiDeviceAction(new Pair<ViewElement, IDeviceAction>(
													null, targetEvent));
								} catch (Exception e) {
									Logger.logError("Problem occured while trying to add KeyEvent to the list:"
											+ e.getMessage());
								}
							}
							if (targetDesc.method
									.equals(unregisterMediaButtonEventReceiverMethod)) {
								try {
									// KeyCode doesn't matter as we have
									// overridden equals and hashcode in
									// KeyEvent to equate
									// all media buttons
									KeyEvent targetEvent = new KeyEvent(
											targetDesc.data,
											KeyEvent.mediaButtons.get(0));
									this.feedBack
											.removeNonUiDeviceAction((new Pair<ViewElement, IDeviceAction>(
													null, targetEvent)));
								} catch (Exception e) {
									Logger.logError("Problem occured while trying to remove KeyEvent form list:"
											+ e.getMessage());
								}
							}
						} else {
							Logger.logError("Log Line not in proper format:"
									+ entry);
						}
					}
					return true;
				}
			}

		}

		return false;
	}

	public static class OperationDesc {
		String method;
		int appId;
		String data;

		public static OperationDesc parseLogLine(String line) {
			// call format
			// D/M3AudioManager( 448):
			// Method:requestAudioFocus,Appid:10029,Listener:com.example.android.musicplayer.AudioFocusHelper@40548ee0
			OperationDesc targetRet = null;
			if (line != null) {
				try {
					targetRet = new OperationDesc();
					String[] parts = line.split(",");
					String[] methodParts = parts[0].split("\\)")[1].split(":");
					targetRet.method = methodParts[methodParts.length - 1];
					targetRet.appId = Integer.parseInt(parts[1].split(":")[1]);
					targetRet.data = parts[2].split(":")[1];
				} catch (Exception e) {
					Logger.logException(e);
					targetRet = null;
				}
			}

			return targetRet;

		}
	}

}
