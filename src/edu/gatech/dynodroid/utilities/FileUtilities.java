/**
 * 
 */
package edu.gatech.dynodroid.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class contains collection of common methods that help in performing
 * various file operations like: File Copy, appending lines to a file etc.
 * 
 * @author machiry
 * 
 */
public final class FileUtilities {

	/***
	 * This method copies the srcFile to dstFile if append is requested than the
	 * srcFile contents will be appended to the dst file
	 * 
	 * @param srcFile
	 *            Source File that needs to be copied
	 * @param dstFile
	 *            Destination location where the provided source file needs to
	 *            be copied
	 * @param append
	 *            if true the the contents of source file will be appended to
	 *            the target file, else dstFile will be replaced
	 * @return true on success , false on failure
	 */
	public static boolean copyfile(File srcFile, File dstFile, boolean append) {
		boolean fileCopySuccesfull = false;
		try {
			File f1 = srcFile;
			InputStream in = new FileInputStream(f1);

			OutputStream out = new FileOutputStream(dstFile, append);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			fileCopySuccesfull = true;
		} catch (Exception ex) {
			Logger.logException(ex);
		}
		return fileCopySuccesfull;
	}

	/***
	 * This method finds the non existing directory by splitting up the provided
	 * java package name if the directory exists it will increment a count
	 * (which is initially 2) and appends if to the path will it finds a
	 * non-existing directory
	 * 
	 * @param dirName
	 *            root directory under which we need to find the non-existing
	 *            directory
	 * @param packageName
	 *            initial guess of non-existing package (ie non existing
	 *            directory)
	 * @return the java package name that points to a non-existing directory
	 */
	public static String findNonExistingDirectory(String dirName,
			String packageName) {
		String[] subParts = packageName.split("\\.");
		String rootDirName = dirName;
		String targetPackageName = packageName;
		int i = 0;
		for (i = 0; i < subParts.length - 1; i++) {
			rootDirName = rootDirName + "/" + subParts[i];
		}
		String relativePath = subParts[i];
		int j = 2;
		while ((new File(rootDirName + "/" + relativePath)).exists()) {
			targetPackageName = packageName + Integer.toString(j);
			relativePath = subParts[i] + Integer.toString(j);
			j++;
		}
		return targetPackageName;
	}

	/***
	 * This method converts the provided java package name to file path by
	 * splitting at '.' and appending the resulting strings
	 * 
	 * @param rootFolder
	 *            root folder which needs to be appended to the path got by
	 *            appending the splited parts
	 * @param packageName
	 *            target package name
	 * @return converted file path
	 */
	public static String convertJavaPackageNameToPath(String rootFolder,
			String packageName) {
		String targetFolderName = rootFolder;
		String[] subParts = packageName.split("\\.");
		int i = 0;
		for (i = 0; i < subParts.length; i++) {
			targetFolderName = targetFolderName + "/" + subParts[i];
		}
		return targetFolderName;
	}

	/***
	 * This method appends the provided line to the file pointed by
	 * targetAbsPath if file doesn't exist it will be created
	 * 
	 * @param targetAbsPath
	 *            the target file to which the provided line needs to be
	 *            appended
	 * @param line
	 *            the target line that needs to be appended
	 * @return true (on success ) else false (on failure)
	 */
	public static boolean appendLineToFile(String targetAbsPath, String line) {
		boolean isSucess = false;
		FileWriter writer = null;
		try {
			File targetFile = new File(targetAbsPath);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			writer = new FileWriter(targetFile, true);
			writer.write(line);
			writer.write("\n");
			isSucess = true;
		} catch (Exception e) {
			Logger.logException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// Save ME!!! LOL..
					Logger.logException(e);
				}
			}
		}
		return isSucess;
	}

	/***
	 * This method appends multiple lines to the provided file if file doesn't
	 * exist it will be created
	 * 
	 * @param targetAbsPath
	 *            the target file to which the provided line needs to be
	 *            appended
	 * @param contents
	 *            the list of strings that needs to be appended
	 * @return true (on success ) else false (on failure)
	 */
	public static boolean appendLinesToFile(String targetAbsPath,
			ArrayList<String> contents) {
		boolean isSucess = false;
		FileWriter writer = null;
		try {
			File targetFile = new File(targetAbsPath);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			writer = new FileWriter(targetFile, true);
			for (String s : contents) {
				writer.write(s);
				writer.write("\n");
			}
			writer.flush();
			isSucess = true;
		} catch (Exception e) {
			Logger.logException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// Save ME!!! hmmm
					// here we might return true...which is okay as the file is
					// already written
					Logger.logException(e);
				}
			}
		}
		return isSucess;
	}

	/***
	 * This method is used to create a directory recursively
	 * 
	 * @param dirName
	 *            The target directory name that needs to be created
	 * @return true/false depending on whether the operation is sucessfull or
	 *         not
	 */
	public static boolean createDirectory(String dirName) {
		try {
			(new File(dirName)).mkdirs();
			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	/***
	 * This method is used to get all the directories present under the given
	 * srcDirectory
	 * 
	 * @param srcFile
	 *            the src folder under which the directories need to be fetched
	 * @return List of all the directories
	 */
	public static File[] getAllDirectories(File srcFile) {
		FileFilter dirFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		return srcFile.listFiles(dirFilter);
	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				createDirectory(dest.getAbsolutePath());
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			copyfile(src, dest, false);
		}
	}

	public static ArrayList<String> readFileLineByLine(String filePath) {
		ArrayList<String> targetLines = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				targetLines.add(strLine);
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			Logger.logException(e);
		}

		return targetLines;
	}

	public static boolean scpTo(String localFilePath, String remoteServerName,
			String remotePath, String userName) {
		boolean retVal = false;
		try {
			Logger.logInfo("Trying to Copy Files from local box:"+localFilePath +" to remote server:"+remoteServerName+ " at path:"+remotePath);
			ExecHelper.RunProgram("scp -r " + localFilePath
					+ " " + userName + "@" + remoteServerName + ":"
					+ remotePath, true);
			//Logger.logInfo("Output:" + output);
			Logger.logInfo("DONE:Trying to Copy Files from local box:"+localFilePath +" to remote server:"+remoteServerName+ " at path:"+remotePath);
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	public static boolean scpFrom(String remoteServerName, String remotePath,
			String localFilePath, String userName) {
		boolean retVal = false;
		try {
			Logger.logInfo("Trying to Copy Files from remote server:"+remoteServerName+ " at path:"+remotePath + " to local path:"+localFilePath);
			ExecHelper.RunProgram(
					"scp -r " + userName + "@" + remoteServerName + ":"
							+ remotePath + " " + localFilePath, true);
			//Logger.logInfo("Output:" + output);
			Logger.logInfo("DONE:Trying to Copy Files from remote server:"+remoteServerName+ " at path:"+remotePath + " to local path:"+localFilePath);
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

	public static boolean zipComplete(String srcFolder, String destFile) {
		boolean retVal = false;
		try {
			File outFolder = new File(destFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(outFolder)));
			zipFolderRecursive(srcFolder, out);
			out.flush();
			out.close();
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}

		return retVal;
	}

	private static boolean zipFolderRecursive(String srcFolder,
			ZipOutputStream targetStream) {
		boolean retVal = false;
		try {
			File inFolder = new File(srcFolder);
			BufferedInputStream in = null;
			byte[] data = new byte[1000];
			String files[] = inFolder.list();
			for (int i = 0; i < files.length; i++) {
				if ((new File(inFolder.getPath() + "/" + files[i])).isFile()) {
					targetStream.putNextEntry(new ZipEntry(files[i]));
					in = new BufferedInputStream(new FileInputStream(
							inFolder.getPath() + "/" + files[i]), 1000);
					int count;
					while ((count = in.read(data, 0, 1000)) != -1) {
						targetStream.write(data, 0, count);
					}
					targetStream.closeEntry();
				} else{
					zipFolderRecursive(inFolder.getPath() + "/" + files[i], targetStream);
				}
			}
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}

		return retVal;
	}
}
