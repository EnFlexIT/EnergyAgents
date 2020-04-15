package de.enflexit.ea.core.dataModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import jade.core.AID;

/**
 * The Class DirectoryHelper can be used to determine/receive standard directory
 * entries (e.g. a working directory for the agent, the logging directory  or the
 * file reference to an Energy Agents phone book).
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DirectoryHelper {

	public enum DirectoryType {
		WorkingDirectory,
		LoggingDirectory,
		PhoneBookFile
	}
	
	public static final String LOGING_SUB_DIR = "log";
	public static final String PHONEBOOK_FILE_NAME = "PhoneBook.xml";
	
	/**
	 * Return the specified file or directory for the correspo0nding agent.
	 *
	 * @param type the {@link DirectoryType}
	 * @param agentAID the agent AID
	 * @return the file or directory as {@link File} instance
	 */
	public static File getFileOrDirectory(DirectoryType type, AID agentAID) {
		// --- AID must be available ----------------------
		if (agentAID==null) {
			throw new NullPointerException("The agents AID is not allowed to be Null!");
		}
		return getFileOrDirectory(type, agentAID.getLocalName());
	}
	
	/**
	 * Return the specified file or directory for the correspo0nding agent.
	 *
	 * @param type the {@link DirectoryType}
	 * @param localAgentName the local agent name
	 * @return the file or directory as {@link File} instance
	 */
	public static File getFileOrDirectory(DirectoryType type, String localAgentName) {
		
		// --- DirectoryType must be specified ------------
		if (type==null) {
			throw new NullPointerException("The DirectoryType is not allowed to be Null!");
		}
		// --- AID must be available ----------------------
		if (localAgentName==null) {
			throw new NullPointerException("The local agent name is not allowed to be Null!");
		}

		
		// --- Get current project ------------------------
		Project currProject = Application.getProjectFocused();
		String eaWorkingDirectory = checkPathEndForFileSeparator(currProject.getProjectAgentWorkingFolderFullPath(true));
		
		// --- Define the directory paths -----------------
		String workingDir = checkPathEndForFileSeparator(eaWorkingDirectory + localAgentName);
		String phoneBookFile = workingDir + PHONEBOOK_FILE_NAME;
		String LogginDir = checkPathEndForFileSeparator(workingDir + LOGING_SUB_DIR); 
		
		
		// --- Initiate the file instance -----------------
		File file = null;
		switch (type) {
		case WorkingDirectory:
			file = new File(workingDir);
			break;
			
		case LoggingDirectory:
			file = new File(LogginDir);
			break;
			
		case PhoneBookFile:
			file = new File(phoneBookFile);
			break;
			
		}
		return file;
	}
	
	/**
	 * Check a path end for a required file separator.
	 *
	 * @param path the path
	 * @return the path string with an file separator at the end
	 */
	public static String checkPathEndForFileSeparator(String path) {
		if (path.endsWith(File.separator)==false) {
			path += File.separator;
		}
		return path;
	}
	
	/**
	 * Returns a sub path element that corresponds to the month of the specified timestamp.
	 * @param timeStamp the timestamp
	 * @return the sub path element
	 */
	public static String getSubPathForMonth(long timeStamp) {
		Date date = new Date(timeStamp);
		String monthDescriptor = new SimpleDateFormat("MM").format(date) + "_" + new SimpleDateFormat("MMM").format(date);
		return monthDescriptor;
	}
	
	/**
	 * Returns the day prefix for files using the specified timestamp (e.g. DAY_18).
	 * @param timeStamp the time stamp
	 * @return the time stamp prefix
	 */
	public static String getDayFilePrefix(long timeStamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		return "DAY_" + sdf.format(new Date(timeStamp));
	}
	
	/**
	 * Checks if the directory is available for the specified destination file. 
	 * If the directory was not created yet, the method tries to create the directory.
	 *
	 * @param destinationFile the destination file
	 * @return true, if the base directory for the file is available
	 */
	public static boolean isAvailableDirectory(File destinationFile) {
		
		File destinDirectory = destinationFile.getParentFile();
		boolean destinDirectoryExists = destinDirectory.exists(); 
		if (destinDirectoryExists==false) {
			destinDirectoryExists = destinDirectory.mkdirs();
		}
		return destinDirectoryExists;
	}
}
