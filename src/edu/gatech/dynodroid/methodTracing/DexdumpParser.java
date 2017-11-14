package edu.gatech.dynodroid.methodTracing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

import edu.gatech.dynodroid.utilities.ExecHelper;
import edu.gatech.dynodroid.utilities.FileUtilities;
import edu.gatech.dynodroid.utilities.Logger;
import edu.gatech.dynodroid.utilities.TextLogger;

public class DexdumpParser {
	private String dexdumpbin = null;
	private Logger targetLogger = null;
	private static final String TAG = "DexDumpParser";

	public DexdumpParser(String dexDumpBinaryPath, TextLogger logger)
			throws IOException {
		if (dexDumpBinaryPath != null && (new File(dexDumpBinaryPath)).exists()
				&& logger != null) {
			Logger.logInfo("dexDumpbin:"+dexDumpBinaryPath);
			this.dexdumpbin = dexDumpBinaryPath;
			targetLogger = logger;
		} else {
			throw new IOException("Provided dexDumpBinary doesn't exist");
		}
	}

	public ArrayList<String> getPackageInfo(String dexFile,
			String mainAppPackage) {
		ArrayList<String> packageInfo = new ArrayList<String>();
		try {
			if (dexFile != null && ((new File(dexFile)).exists())) {
				File tmpFile = File.createTempFile("parsed", ".xml");
				Logger.logInfo("Executing Command:"+dexdumpbin + " -l xml " + dexFile);
				String output = ExecHelper.RunProgram(dexdumpbin + " -l xml " + dexFile, true);
				FileUtilities.appendLineToFile(tmpFile.getAbsolutePath(), output);
				if (tmpFile.exists()) {
					targetLogger.logInfo(TAG, "Parsed Dex file to xml file:"
							+ tmpFile.getAbsolutePath());
					DocumentBuilder builder = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					Document doc = builder.parse(tmpFile);
					Element root = doc.getDocumentElement();
					NodeList classElems = root.getElementsByTagName("package");
					for (int i = 0; i < classElems.getLength(); i++) {
						Element pkgele = (org.w3c.dom.Element) classElems
								.item(i);
						String pkgName = pkgele.getAttribute("name");
						if (pkgName != null) {
							if (pkgName.startsWith(".")) {
								pkgName = mainAppPackage + pkgName;
							} else if (!pkgName.contains(".")) {
								pkgName = mainAppPackage + "." + pkgName;
							}
							pkgName = pkgName.replace('.', '/');
							packageInfo.add("L"+pkgName);
						}
					}

				}
			}
		} catch (Exception e) {
			targetLogger.logException(TAG, e);
		}
		return packageInfo;
	}

}
