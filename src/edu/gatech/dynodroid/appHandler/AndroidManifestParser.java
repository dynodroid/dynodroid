package edu.gatech.dynodroid.appHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.gatech.dynodroid.utilities.Logger;

/***
 * This the class that takes care of parsing Android Manifest file to get
 * information such as main activity, app package, permissions etc
 * 
 * @author machiry
 * 
 */
public class AndroidManifestParser {

	// Private fields
	private File manifestFile;
	private String appPackage;
	private String launcherActivity;
	private ArrayList<String> permissions;
	private String instrumentationInfo;
	private ArrayList<String> allActivities = null;
	private ArrayList<String> allServices = null;
	private ArrayList<BroadCastReceiver> allReceivers = null;

	// Private Static Fields
	private final static String manifestTagName = "manifest";
	private final static String mainPackageAttributeName = "package";
	private final static String activityTagName = "activity";
	private final static String intentFilterTagName = "intent-filter";
	private final static String actionTagName = "action";
	private final static String categoryTagName = "category";
	private final static String serviceTagName = "service";
	private final static String receiverTagName = "receiver";
	private final static String androidNameAttribute = "android:name";
	private final static String androidMainActionValue = "android.intent.action.MAIN";
	private final static String androidLauncherValue = "android.intent.category.LAUNCHER";
	private final static String usesPermissionTagName = "uses-permission";
	private final static String instrumentationTagName = "instrumentation";

	/***
	 * Public Constructor
	 * 
	 * @param file
	 *            The target AndoridManifest.xml that needs to be parsed
	 * @throws FileNotFoundException
	 *             if the file is not present
	 */
	public AndroidManifestParser(String file) throws FileNotFoundException {
		manifestFile = new File(file);
		if (!manifestFile.exists()) {
			throw new FileNotFoundException(
					"Provided AndroidManifest.xml not present");
		}
		this.appPackage = null;
		this.launcherActivity = null;
		this.permissions = null;
	}

	/***
	 * This method returns the instrumentation if present in the provided
	 * Manifest file
	 * 
	 * @return android:name attribute of the instrumentation present in the
	 *         Manifest.xml
	 */
	public String getInstrumentation() {
		if (this.instrumentationInfo == null) {
			try {
				// Get the instrumented activity name
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc
						.getElementsByTagName(instrumentationTagName);
				if (nodes != null && nodes.getLength() == 1) {
					Element instrumentationElement = (Element) nodes.item(0);
					this.instrumentationInfo = instrumentationElement
							.getAttribute(androidNameAttribute);
				}
			} catch (Exception e) {
				Logger.logException(e);
			}

		}
		return this.instrumentationInfo;
	}

	/***
	 * This method returns the fully qualified names of all the activity components present in apps Manifest.
	 * @return An ArrayList of all activities names
	 */
	public ArrayList<String> getAllActivities() {
		if (this.allActivities == null) {
			this.allActivities = new ArrayList<String>();
			getAppPackage();
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc.getElementsByTagName(activityTagName);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element activityElement = (Element) nodes.item(i);
					String activityName = activityElement
							.getAttribute(androidNameAttribute);
					if (activityName.startsWith(".")) {
						activityName = this.appPackage + activityName;
					} else if (!activityName.contains(".")) {
						activityName = this.appPackage + "." + activityName;
					}
					this.allActivities.add(activityName);
				}

			} catch (Exception e) {
				Logger.logException(e);
			}

		}
		return this.allActivities;
	}

	/***
	 * This method returns the fully qualified names of all the service components present in apps Manifest.
	 * @return An ArrayList of all service names
	 */
	public ArrayList<String> getAllServices() {
		if (this.allServices == null) {
			this.allServices = new ArrayList<String>();
			getAppPackage();
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc.getElementsByTagName(serviceTagName);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element serviceElement = (Element) nodes.item(i);
					String serviceName = serviceElement
							.getAttribute(androidNameAttribute);
					if (serviceName.startsWith(".")) {
						serviceName = this.appPackage + serviceName;
					} else if (!serviceName.contains(".")) {
						serviceName = this.appPackage + "." + serviceName;
					}
					this.allServices.add(serviceName);
				}

			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return this.allServices;
	}

	
	/***
	 * This method returns all the receiver components present in apps Manifest.
	 * @return An ArrayList of all receiver components in form of BroadCastReceiver  objects
	 * 
	 *  @see edu.gatech.dynodroid.appHandler.BroadCastReceiver
	 */
	public ArrayList<BroadCastReceiver> getAllBroadcastReceivers() {
		if (this.allReceivers == null) {
			this.allReceivers = new ArrayList<BroadCastReceiver>();
			getAppPackage();
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc.getElementsByTagName(receiverTagName);
				for (int i = 0; i < nodes.getLength(); i++) {
					BroadCastReceiver currentReceiver = new BroadCastReceiver();
					Element receiverElement = (Element) nodes.item(i);
					Node currentReceiverNode = nodes.item(i);
					String receiverName = receiverElement
							.getAttribute(androidNameAttribute);
					if (receiverName.startsWith(".")) {
						receiverName = this.appPackage + receiverName;
					} else if (!receiverName.contains(".")) {
						receiverName = this.appPackage + "." + receiverName;
					}

					currentReceiver.receiverComponentName = receiverName;

					for (int j = 0; j < currentReceiverNode.getChildNodes()
							.getLength(); j++) {

						Node intentFilterNode = currentReceiverNode
								.getChildNodes().item(j);
						if (isNodeOfType(intentFilterNode, intentFilterTagName)) {
							IntentFilter currentIntentFilter = new IntentFilter();
							for (int k = 0; k < intentFilterNode
									.getChildNodes().getLength(); k++) {
								Node currentIntentChildNode = intentFilterNode
										.getChildNodes().item(k);
								if (isNodeOfType(currentIntentChildNode,
										actionTagName)) {
									Element currentIntentChildElement = (Element) currentIntentChildNode;
									currentIntentFilter.intentActions
											.add(currentIntentChildElement
													.getAttribute(androidNameAttribute));
								}
								if (isNodeOfType(currentIntentChildNode,
										categoryTagName)) {
									Element currentIntentChildElement = (Element) currentIntentChildNode;
									currentIntentFilter.intentCategory
											.add(currentIntentChildElement
													.getAttribute(androidNameAttribute));
								}

							}
							currentReceiver.intentFilters
									.add(currentIntentFilter);

						}
					}

					this.allReceivers.add(currentReceiver);
				}
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return this.allReceivers;
	}

	/***
	 * This method returns the main app package that this Application belong to:
	 * This reads the 'package' attribute of the Manifest tag in the
	 * AndroidManifest.xml
	 * 
	 * @return target App package in string
	 */
	public String getAppPackage() {
		if (this.appPackage == null) {
			try {
				// Get the main app package name
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc.getElementsByTagName(manifestTagName);
				if (nodes != null && nodes.getLength() == 1) {
					Element manifestElement = (Element) nodes.item(0);
					this.appPackage = manifestElement
							.getAttribute(mainPackageAttributeName);
				}
			} catch (Exception e) {
				Logger.logException(e);
			}
		}
		return this.appPackage;
	}

	/***
	 * This returns the main activity(Activity that will be lauched when the app
	 * starts) of the app.
	 * 
	 * @return fully qualified name of the main activity
	 */
	public String getLauncherActivity() {
		if (this.launcherActivity == null) {
			// Get the main launcher activity
			try {
				// Get the main app package name
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc.getElementsByTagName(activityTagName);
				boolean mainActivity = false;
				boolean launcherActivity = false;
				if (nodes != null) {
					for (int i = 0; i < nodes.getLength(); i++) {
						mainActivity = false;
						launcherActivity = false;
						Element activityElement = (Element) nodes.item(i);
						Node currentActivityNode = nodes.item(i);
						for (int j = 0; j < currentActivityNode.getChildNodes()
								.getLength(); j++) {
							Node intentFilterNode = currentActivityNode
									.getChildNodes().item(j);
							if (isNodeOfType(intentFilterNode,
									intentFilterTagName)) {
								for (int k = 0; k < intentFilterNode
										.getChildNodes().getLength(); k++) {
									Node currentChild = intentFilterNode
											.getChildNodes().item(k);
									if (isNodeOfType(currentChild,
											actionTagName)
											&& hasAttribute(currentChild,
													androidNameAttribute,
													androidMainActionValue)) {
										mainActivity = true;
									}
									if (isNodeOfType(currentChild,
											categoryTagName)
											&& hasAttribute(currentChild,
													androidNameAttribute,
													androidLauncherValue)) {
										launcherActivity = true;
									}

								}

							}
						}
						if (mainActivity && launcherActivity) {
							this.launcherActivity = activityElement
									.getAttribute(androidNameAttribute);
							if (this.launcherActivity.startsWith(".")) {
								this.launcherActivity = getAppPackage()
										+ this.launcherActivity;
							} else if (!this.launcherActivity.contains(".")) {
								this.launcherActivity = getAppPackage() + "."
										+ this.launcherActivity;
							}
							break;
						}

					}
				}
			} catch (Exception e) {
				Logger.logException(e);

			}
		}
		return this.launcherActivity;
	}

	private boolean hasAttribute(Node targetNode, String attributeName,
			String attributeValue) {
		if (targetNode != null) {
			Element ele = (Element) targetNode;
			return ele.hasAttribute(attributeName)
					&& ele.getAttribute(attributeName).equalsIgnoreCase(
							attributeValue);
		}
		return false;
	}

	private boolean isNodeOfType(Node targetNode, String type) {
		if (targetNode != null && (targetNode instanceof Element)) {
			Element ele = (Element) targetNode;
			if (ele.getTagName().equalsIgnoreCase(type)) {
				return true;
			}
		}
		return false;
	}

	/***
	 * This method gets all the permissions required by the App by parsing all
	 * the <uses-permission> tags..
	 * 
	 * @return List of Strings that contain the target permissions that will be
	 *         used
	 */
	public ArrayList<String> getPermissions() {
		try {
			if (this.permissions == null) {
				// Get the permissions from the Manifest File
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(manifestFile);
				NodeList nodes = doc
						.getElementsByTagName(usesPermissionTagName);
				if (nodes != null) {
					this.permissions = new ArrayList<String>();
					for (int i = 0; i < nodes.getLength(); i++) {
						Element manifestElement = (Element) nodes.item(i);
						this.permissions.add(manifestElement
								.getAttribute(androidNameAttribute));
					}
				}
				if (this.permissions == null) {
					this.permissions = new ArrayList<String>();
				}
			}
		} catch (Exception e) {
			Logger.logException(e);
		}

		return new ArrayList<String>(this.permissions);
	}

	/***
	 * This method adds the provided lines under the given tag name in the
	 * provided manifest file and saves the file to the provided location
	 * 
	 * @param linesToBeAdded
	 *            the lines that need to be added
	 * @param tagName
	 *            the tag name under which the provided lines to be added
	 * @param targetManifestFile
	 *            target manifest file (after adding the lines) to the src file
	 * @return true/false on success or failure respectively
	 */
	public boolean addInsideTag(ArrayList<String> linesToBeAdded,
			String tagName, String targetManifestFile) {
		boolean retVal = false;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(manifestFile);
			NodeList nodes = doc.getElementsByTagName(tagName);
			if (nodes != null) {
				Element elm = (Element) nodes.item(0);

				DocumentBuilder targetBuilder = DocumentBuilderFactory
						.newInstance().newDocumentBuilder();
				String targetXmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><dummyRoot>";
				for (String s : linesToBeAdded) {
					targetXmlString += s;
				}
				targetXmlString += "</dummyRoot>";
				Document targetDoc = targetBuilder.parse(new InputSource(
						new StringReader(targetXmlString)));

				Element parentElement = targetDoc.getDocumentElement();
				NodeList destNodes = parentElement.getChildNodes();
				for (int i = 0; i < destNodes.getLength(); i++) {

					elm.appendChild(doc.importNode(destNodes.item(i), true));
				}

				retVal = writexmlDocumentToFile(doc, targetManifestFile);
			}
		} catch (Exception e) {
			retVal = false;
			Logger.logException(e);
		}

		return retVal;
	}

	private boolean writexmlDocumentToFile(Document doc, String targetFile) {
		boolean isSucess = false;
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			String xmlString = result.getWriter().toString();

			File xmlfile = new File(targetFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(xmlfile));
			writer.write(xmlString);
			writer.close();
			isSucess = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return isSucess;
	}

	@Override
	public String toString() {
		String retVal = null;
		getPermissions();
		getLauncherActivity();
		getAppPackage();
		getAllActivities();
		getAllServices();
		getAllBroadcastReceivers();
		getInstrumentation();
		retVal = "App Package:" + this.appPackage + "\n";
		retVal += "Launcher Activity:" + this.launcherActivity + "\n";
		retVal += "Instrumentation :"
				+ (this.instrumentationInfo == null ? "No Instrumentation(Expected)"
						: this.instrumentationInfo) + "\n";
		retVal += "Services :"
				+ ((this.allServices == null || this.allServices.size() == 0) ? "No Services\n"
						: "\n");
		if (this.allServices != null) {
			for (String s : this.allServices) {
				retVal += "\t" + s + "\n";
			}
		}
		retVal += "Receivers :"
				+ ((this.allReceivers == null || this.allReceivers.size() == 0) ? "No Receivers\n"
						: "\n");
		if (this.allReceivers != null) {
			for (BroadCastReceiver s : this.allReceivers) {
				retVal += "\t" + s.toString() + "\n";
			}
		}
		retVal += "Activities :"
				+ ((this.allActivities == null || this.allActivities.size() == 0) ? "No Activites, Very Strange\n"
						: "\n");
		if (this.allActivities != null) {
			for (String s : this.allActivities) {
				retVal += "\t" + s + "\n";
			}
		}
		retVal += "Permissions :"
				+ ((this.permissions == null || this.permissions.size() == 0) ? "No Permissions, Very Strange\n"
						: "\n");
		if (this.permissions != null) {
			for (String s : this.permissions) {
				retVal += "\t" + s + "\n";
			}
		}
		return retVal;

	}
}
