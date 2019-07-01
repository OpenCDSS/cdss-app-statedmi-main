// StateDMISession - Class to maintain StateDMI session information such as the history of command files opened.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.DMI.StateDMI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Class to maintain StateDMI session information such as the history of command files opened.
A singleton instance should be retrieve using the getInstance() method.
*/
public class StateDMISession
{

/**
Global value that indicates if the command file history is being written.
Need to handle because if the file is being modified at the same time exceptions will be thrown.
*/
private boolean historyBeingWritten = false;

/**
Global value that indicates if the UI state file is being written.
Need to handle because if the file is being modified at the same time exceptions will be thrown.
*/
private boolean uiStateBeingWritten = false;

/**
 * List of properties for the UI state, such as last selections in wizards, choices, etc.
 */
private PropList uiStateProps = new PropList("ui-state");

/**
 * Private singleton instance.
 * Instance is created in getInstance().
 */
private static StateDMISession instance = null;

/**
 * Major software version, used for folder below .statedmi/.
 * This is initialized as a parameter to the constructor.
 */
private int majorVersion = 0; // 0 will be an obvious error if a folder is created

/**
Private constructor for the session instance.
@param majorVersion the major version of StateDMI, necessary because user files are organized by StateDMI version.
*/
private StateDMISession ( int majorVersion )
{	// Read UI state properties so they are available for interaction.
	// They will be written when StateDMI closes, and at other intermediate points, as appropriate,
	// by calling writeUIState().
	this.majorVersion = majorVersion;
	readUIState();
}

/**
 * Check that the history file exists.
 * If it does not, copy from the previous version(s) of StateDMI if it exists.
 * @return true if the history file exists, false if an issue creating/finding the file.
 */
private boolean checkHistoryFile() {
	String historyFile = getHistoryFile();
	File f = new File(historyFile);
	File folder = f.getParentFile();
	if ( !folder.exists() ) {
		// Create all the folders for the history file
		if ( !folder.mkdirs() ) {
			// Unable to make folder
			return false;
		}
	}
	// If here the folder for the history file exists so can check for the file.
	// If the file does not exist, see if one exists in a previous version of the software and copy it.
	// - this is OK because currently the format has not changed since the original
	// - go back two versions to allow for a year or two of gap for the user
	for ( int i = 1; i <= 2; i++ ) {
		if ( !f.exists() ) {
			// The history file does not exist so check for an old version
			String historyFileOld = getHistoryFile(getMajorVersion() - i);
			File f2 = new File(historyFileOld);
			if ( f2.exists() ) {
				Path original = Paths.get(historyFileOld);
				Path copy = Paths.get(historyFile);
				try {
					Files.copy(original, copy);
					break;
				}
				catch ( IOException e ) {
					String routine = getClass().getSimpleName() + ".checkHistoryFile";
					Message.printWarning(2,routine,"Error copying old history file \"" + historyFileOld +
						"\" history file to \"" + historyFile + "\"" );
				}
			}
		}
	}
	// If the file still does not exist, create an empty file
	if ( !f.exists() ) {
		// The following will get overwritten by writeHistory()
		StringBuilder sb = new StringBuilder ( "# StateDMI command file history, most recent at top, shared between similar StateDMI major version" );
		try {
			IOUtil.writeFile ( f.getPath(), sb.toString() );
		}
		catch ( IOException e ) {
			// For now absorb
			return false;
		}
	}
	return true;
}

/**
 * Check that the UI state file exists.
 * If it does not, copy from the previous version(s) of StateDMI if it exists.
 * @return true if the UI state file exists, false if an issue creating/finding the file.
 */
private boolean checkUIStateFile() {
	String uiStateFile = getUIStateFile();
	File f = new File(uiStateFile);
	File folder = f.getParentFile();
	if ( !folder.exists() ) {
		// Create all the folders for the UI state file
		if ( !folder.mkdirs() ) {
			// Unable to make folder
			return false;
		}
	}
	// If here the folder for the UI state file exists so can check for the file.
	// If the file does not exist, see if one exists in a previous version of the software and copy it.
	// - this is OK because currently the format has not changed since the original
	// - go back two versions to allow for a year or two of gap for the user
	for ( int i = 1; i <= 2; i++ ) {
		if ( !f.exists() ) {
			// The UI state file does not exist so check for an old version
			String uiStateFileOld = getUIStateFile(getMajorVersion() - i);
			File f2 = new File(uiStateFileOld);
			if ( f2.exists() ) {
				Path original = Paths.get(uiStateFileOld);
				Path copy = Paths.get(uiStateFile);
				try {
					Files.copy(original, copy);
					break;
				}
				catch ( IOException e ) {
					String routine = getClass().getSimpleName() + ".checkUIStateFile";
					Message.printWarning(2,routine,"Error copying old UI state file \"" + uiStateFileOld +
						"\" UI state file to \"" + uiStateFile + "\"" );
				}
			}
		}
	}
	// If the file still does not exist, create an empty file
	if ( !f.exists() ) {
		// The following will get overwritten by writeHistory()
		StringBuilder sb = new StringBuilder ( "# StateDMI UI state" );
		try {
			IOUtil.writeFile ( f.getPath(), sb.toString() );
		}
		catch ( IOException e ) {
			// For now absorb
			return false;
		}
	}
	return true;
}

/**
Create a new system configuration file in user files.
This is used when transitioning from StateDMI earlier than 5.00.00 to version later.
@return true if the file was created, false for all other cases.
*/
public boolean createUserConfigFile ( )
{
	if ( getUserFolder().equals("/") ) {
		// Don't allow files to be created under root on Linux
		return false;
	}

	// Create the configuration folder if necessary
	File f = new File(getUserConfigFile());
	File folder = f.getParentFile();
	if ( !folder.exists() ) {
		if ( !folder.mkdirs() ) {
			// Unable to make folder
			return false;
		}
	}
	try {
		String nl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder ( "# StateDMI configuration file containing user settings, shared between StateDMI versions" + nl );
		sb.append("# This file indicates application properties." + nl );
		sb.append("# Refer to the StateDMI.cfg file under the software installation folder for global configuration properties." + nl );
		sb.append("# User settings in this file will override the installation settings." + nl );
		sb.append(nl);
		// Include a line for HydroBase since it often needs to be disabled on computers where HydroBase is not used
		// - for now HydroBase is always required so don't provide a configuration property
		//sb.append("HydroBaseEnabled = true" + nl );
		IOUtil.writeFile ( f.getPath(), sb.toString() );
		return true;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Create the user's datastores folder if necessary.
@return true if datastores folder exists and is writable, false otherwise.
*/
public boolean createUserDatastoresFolder () {
	String datastoreFolder = getUserDatastoresFolder();
	// Do not allow datastore folder to be created under Linux root but allow StateDMI to run
	if ( datastoreFolder.equals("/") ) {
		return false;
	}
	File f = new File(datastoreFolder);
	if ( !f.exists() ) {
		try {
			f.mkdirs();
		}
		catch ( SecurityException e ) {
			return false;
		}
	}
	else {
		// Make sure it is writable
		if ( !f.canWrite() ) {
			return false;
		}
	}
	return true;
}

/**
Create the user "logs" folder if necessary.
@return true if "logs" folder exists and is writable, false otherwise.
*/
public boolean createUserLogsFolder () {
	String logsFolder = getUserLogsFolder();
	// Do not allow log file to be created under Linux root but allow StateDMI to run
	if ( logsFolder.equals("/") ) {
		return false;
	}
	File f = new File(logsFolder);
	if ( !f.exists() ) {
		try {
			f.mkdirs();
		}
		catch ( SecurityException e ) {
			return false;
		}
	}
	else {
		// Make sure it is writable
		if ( !f.canWrite() ) {
			return false;
		}
	}
	return true;
}

/**
Create the user "plugins" folder if necessary.
THIS IS CURRENTLY NOT USED FOR StateDMI.
@return true if "plugins" folder exists and is writable, false otherwise.
*/
public boolean createUserPluginsFolder () {
	/*
	String pluginsFolder = getPluginsFolder();
	// Do not allow log file to be created under Linux root but allow StateDMI to run
	if ( pluginsFolder.equals("/") ) {
		return false;
	}
	File f = new File(pluginsFolder);
	if ( !f.exists() ) {
		try {
			f.mkdirs();
		}
		catch ( SecurityException e ) {
			return false;
		}
	}
	else {
		// Make sure it is writable
		if ( !f.canWrite() ) {
			return false;
		}
	}
	*/
	return true;
}

/**
Create the user's system folder if necessary.
@return true if system folder exists and is writable, false otherwise.
*/
public boolean createUserSystemFolder () {
	String systemFolder = getUserSystemFolder();
	// Do not allow system folder to be created under Linux root but allow StateDMI to run
	if ( systemFolder.equals("/") ) {
		return false;
	}
	File f = new File(systemFolder);
	if ( !f.exists() ) {
		try {
			f.mkdirs();
		}
		catch ( SecurityException e ) {
			return false;
		}
	}
	else {
		// Make sure it is writable
		if ( !f.canWrite() ) {
			return false;
		}
	}
	return true;
}

/**
Return the name of the StateDMI history file.
This exists under the StateDMI version folder, used with StateDMI 5.x and later.
*/
public String getHistoryFile ()
{
	return getHistoryFile ( getMajorVersion() );
}

/**
Return the name of the StateDMI history file for a StateDMI major version.
*/
public String getHistoryFile ( int majorVersion )
{
	String historyFile = "";
	// History file exists in, for example:  ./statedmi/5/
	historyFile = getMajorVersionFolder() + File.separator + "command-file-history.txt";
	//Message.printStatus(1,"","History file \"" + historyFile + "\"");
	return historyFile;
}

/**
Return the folder where StateDMI is installed, for example:
<ul>
<li>	Windows:  C:\CDSS\StateDMI-Version</li>
<li>	Linux: /opt/StateDMI-Version</li>
</ul>
*/
public String getInstallFolder ()
{
	String installFolder = IOUtil.getApplicationHomeDir();
	return installFolder;
}

/**
Return the folder for StateDMI datastore configuration files, in software install folder:
*/
public String getInstallDatastoresFolder ()
{
	String installFolder = getInstallFolder();
	return installFolder + File.separator + "datastores";
}

/**
 * Return the singleton instance of the StateDMISession.
 * This version must be called after the overloaded version that specifies the major version.
 * Otherwise, 0 is set as the major version.
 */
public static StateDMISession getInstance() {
	if ( instance == null ) {
		instance = new StateDMISession( 0 );
	}
	// Else instance is non-null and will be returned
	instance.initializeUserFiles(instance.getMajorVersion()); // Won't do anything if already initialized
	return instance;
}

/**
 * Return the singleton instance of the StateDMISession.
 * @param majorVersion the major version of StateDMI, necessary because user files are organized by StateDMI version.
 */
public static StateDMISession getInstance( int majorVersion ) {
	if ( instance == null ) {
		instance = new StateDMISession( majorVersion );
	}
	// Else instance is non-null and will be returned
	instance.initializeUserFiles(instance.getMajorVersion()); // Won't do anything if already initialized
	return instance;
}

/**
 * Return the major software version, used for top-level user files.
 * @return the software major version, used for top-level user files
 */
public int getMajorVersion () {
	return this.majorVersion;
}

/**
Return the folder to the major version:
<ul>
<li>	Windows:  C:\Users\UserName\.statedmi\5</li>
<li>	Linux: /home/UserName/.statedmi/5</li>
</ul>
*/
public String getMajorVersionFolder ()
{
	String majorVersionFolder = getUserFolder() + File.separator + getMajorVersion();
	//Message.printStatus(1,"","Major version folder is \"" + majorVersionFolder + "\"");
	return majorVersionFolder;
}

/**
Return the name of the StateDMI UI state file.
*/
public String getUIStateFile ()
{
	return getUIStateFile(getMajorVersion());
}

/**
Return the name of the StateDMI UI state file for a StateDMI major version.
@param majorVersion the major StateDMI version of interest.
*/
public String getUIStateFile ( int majorVersion )
{
	String uiStateFile = "";
	// UI State file exists in, for example:  ./statedmi/5/
	uiStateFile = getMajorVersionFolder() + File.separator + "ui-state.txt";
	//Message.printStatus(1,"","UI state file \"" + uiStateFile + "\"");
	return uiStateFile;
}

/**
 * Return a UI state property, as a string.
 * @param propertyName name of property being requested.
 */
public String getUIStateProperty ( String propertyName ) {
	return this.uiStateProps.getValue(propertyName);
}

/**
Return the name of the user's StateDMI configuration file.
*/
public String getUserConfigFile ()
{
	String logFile = getUserSystemFolder() + File.separator + "StateDMI.cfg";
	//Message.printStatus(1,"","Config file is \"" + logFolder + "\"");
	return logFile;
}

/**
Return the value of the requested property from the user's StateDMI configuration file.
This reads the configuration file each time to ensure synchronization.
@param propName property name
@return the value of the property or null if file or property is not found
*/
public String getUserConfigPropValue ( String propName )
{
	String configFile = getUserConfigFile();
	File f = new File(configFile);
	if ( !f.exists() || !f.canRead() ) {
		return null;
	}
	PropList props = new PropList("StateDMIUserConfig");
	props.setPersistentName(configFile);
	try {
		props.readPersistent();
		return props.getValue(propName);
	}
	catch ( Exception e ) {
		return null;
	}
}

/**
Return the name of the user's datastore configuration folder.
@return the "datastores" folder path (no trailing /).
*/
public String getUserDatastoresFolder ()
{
	// 12.06.00 and earlier (not under version folder and singular)...
	//String datastoreFolder = getUserFolder() + File.separator + "datastore";
	// 12.07.00 and later (under version folder and plural, which seems more appropriate)
	String datastoresFolder = getMajorVersionFolder() + File.separator + "datastores";
	//Message.printStatus(1,"","Datastores folder is \"" + datastoreFolder + "\"");
	return datastoresFolder;
}

/**
Return the name of the StateDMI user folder for the operating system, for example:
<ul>
<li>	Windows:  C:\Users\UserName\.statedmi</li>
<li>	Linux: /home/UserName/.statedmi</li>
</ul>
*/
public String getUserFolder ()
{
	String userFolder = System.getProperty("user.home") + File.separator + ".statedmi";
	//Message.printStatus(1,"","User folder is \"" + userFolder + "\"");
	return userFolder;
}

/**
Return the name of the log file for the user.
*/
public String getUserLogFile ()
{
	String logFile = getUserLogsFolder() + File.separator + "StateDMI_" + System.getProperty("user.name") + ".log";
	//Message.printStatus(1,"","Log folder is \"" + logFolder + "\"");
	return logFile;
}

/**
Return the name of the user's log file folder.
@return the "logs" folder path (no trailing /).
*/
public String getUserLogsFolder ()
{
	String logsFolder = "";
	logsFolder = getMajorVersionFolder() + File.separator + "logs";
	//Message.printStatus(1,"","Log folder is \"" + logFolder + "\"");
	return logsFolder;
}

/**
Return the name of the plugins folder for user files.
THIS IS CURRENTLY NOT USED FOR StateDMI.
@return the "plugins" folder path (no trailing /).
*/
public String getUserPluginsFolder ()
{
	// 12.06.00 and earlier was split into plugin-command and plugin-datastore
	// 12.07.00 and later (under version folder and plural, which seems more appropriate)
	String pluginsFolder = getMajorVersionFolder() + File.separator + "plugins";
	//Message.printStatus(1,"","Plugins folder is \"" + pluginsFolder + "\"");
	return pluginsFolder;
}

/**
Return the name of the user system folder.
<pre>
Windows:  C:/Users/user/.tstool/MajorVersion/system
Linux:    $HOME/.tstool/MajorVersion/system
</pre>
@return the user "system" folder path (no trailing /).
*/
public String getUserSystemFolder ()
{

	// 12.06.00 and earlier (not under version folder)...
	//String systemFolder = getUserFolder() + File.separator + "system";
	// 12.07.00 and later (under version folder)
	String systemFolder = getMajorVersionFolder() + File.separator + "system";
	//Message.printStatus(1,"","System folder is \"" + systemFolder + "\"");
	return systemFolder;
}

/**
 * Initialize user files.
 * This method should be called at application startup to make sure that user files are created.
 * 
 * <pre>
 * C:/Users/user/
 *   .statedmi/
 *      5/
 *        datastores/
 *          somedatastore/
 *            somedatastore.cfg
 *        logs/
 *          StateDMI-user.log
 *      6/
 *        ...
 * </pre>
 * 
 * Conventions will be used to manage files but users will be able to organize as they prefer.
 * The jar files can contain datastores and commands in the same jar file so as to
 * minimize duplicate deployment of code.
 * The use of a version folder is a compromise: users will need to use migration tools
 * to import previous version datastore configurations, etc., but the version folder
 * allows different major versions of StateDMI to remain functional if major design changes occur.
 * @param majorVersion the major StateDMI version, a parameter to allow calling multiple times if necessary
 * @return true if the files were initialized, false for all other cases.
 */
public boolean initializeUserFiles ( int version ) {
	String routine = getClass().getSimpleName() + ".initializeUserFiles";
	String userFolder = getUserFolder();
	if ( userFolder.equals("/") ) {
		// Don't allow files to be created under root on Linux
		Message.printWarning(3, routine, "Unable to create user files in / (root) folder - need to run as normal user.");
		return false;
	}
	// Create the version folder if it does not exist
	String versionFolder = userFolder + File.separator + version;
	File f = new File(versionFolder);
	if ( !f.exists() ) {
		try {
			f.mkdirs();
		}
		catch ( SecurityException e ) {
			Message.printWarning(3, routine, "Could not create StateDMI user files version folder \"" + versionFolder + "\" (" + e + ").");
			return false;
		}
	}
	else {
		// Make sure it is writeable
		if ( !f.canWrite() ) {
			Message.printWarning(3, routine, "StateDMI user files version folder \"" + versionFolder + "\" is not writeable.");
			return false;
		}
	}
	// Create main folders under the version folder
	createUserDatastoresFolder();
	createUserLogsFolder();
	// Plugins are not implemented
	//createUserPluginsFolder();
	createUserSystemFolder();
	return true;
}

/**
Push a new command file onto the history.  This reads the history, updates it, and writes it.
This is done because if multiple StateDMI sessions are running they, will share the history.
@param commandFile full path to command file that has been opened
*/
public void pushHistory ( String commandFile )
{
	// Read the history file from the .statedmi-history file
	List<String> history = readHistory();
	// Add in the first position so it will show up first in the File...Open... menu
	history.add(0, commandFile);
	// Process from the back so that old duplicates are removed and recent access is always at the top of the list
	// TODO SAM 2014-12-17 use a StateDMI configuration file property to set cap
	int max = 100;
	String old;
	for ( int i = history.size() - 1; i >= 1; i-- ) {
		old = history.get(i);
		if ( i >= max ) {
			// Trim the history to the maximum
			history.remove(i);
		}
		else if ( old.equals(commandFile) || old.equals("") || old.startsWith("#")) {
			// Ignore comments, blank lines and duplicate to most recent access
			history.remove(i--);
		}
	}
	//Message.printStatus(2,"", "History length is " + history.size());
	// Write the updated history
	writeHistory(history);
}

/**
Read the history of command files that have been opened.
@return list of command files recently opened, newest first
*/
public List<String> readHistory()
{	//String routine = getClass().getSimpleName() + ".readHistory";
	try {
		// If the history file does not exist or is zero size, automatically copy from the previous
		// version of StateDMI if it exists.
		if ( !checkHistoryFile() ) {
			// History file could not be verified/created so return empty list.
			return new ArrayList<String>();
		}
		List<String> history = IOUtil.fileToStringList(getHistoryFile());
		// Remove comment lines
		for ( int i = (history.size() - 1); i >= 0; i-- ) {
			String f = history.get(i);
			if ( f.startsWith("#") ) {
				history.remove(i);
			}
		}
		return history;
	}
	catch ( Exception e ) {
		// For now just swallow exception - may be because the history folder does not exist
		//Message.printWarning(3,routine,e);
		return new ArrayList<String>();
	}
}

/**
Read the UI state.  The UI state is saved as simple property=value text file in
the .statedmi/ui-state.txt file.
Properties are saved in the uiStateProps PropList internally.
*/
public void readUIState()
{	//String routine = getClass().getSimpleName() + ".readUIState";
	// Check that the UI state file exists in the expected location
	checkUIStateFile();
	try {
		this.uiStateProps = new PropList("ui-state");
		this.uiStateProps.setPersistentName(getUIStateFile());
		this.uiStateProps.readPersistent();
	}
	catch ( Exception e ) {
		// For now just swallow exception - may be because the UI state file has not
		// been created the first time.
		//Message.printWarning(3,routine,e);
	}
}

/**
 * Set a UI state property.
 * @propertyName name of the state property.
 * @propertyValue value of the property as a string.
 */
public void setUIStateProperty ( String propertyName, String propertyValue ) {
	this.uiStateProps.set(propertyName,propertyValue);
}

/**
Write the history of commands files that have been opened.
*/
private void writeHistory ( List<String> history )
{
	String nl = System.getProperty("line.separator");
	StringBuilder sb = new StringBuilder ( "# StateDMI command file history, most recent at top, shared between similar StateDMI major version" );
	
	if ( getUserFolder().equals("/") ) {
		// Don't allow files to be created under root on Linux
		return;
	}

	long ms = System.currentTimeMillis();
	while ( this.historyBeingWritten ) {
		// Need to wait until another operation finishes writing
		// But don't wait longer than a couple of seconds before moving on
		if ( (System.currentTimeMillis() - ms) > 2000 ) {
			break;
		}
	}
	// Now can continue
	try {
	
		for ( String s : history ) {
			sb.append(nl + s);
		}
		// If the history file does not exist or is zero size, automatically copy from the previous
		// version of StateDMI if it exists.
		if ( !checkHistoryFile() ) {
			return;
		}
		try {
			//Message.printStatus(1, "", "Writing history: " + sb );
			File f = new File(getHistoryFile());
			IOUtil.writeFile ( f.getPath(), sb.toString() );
		}
		catch ( Exception e ) {
			// Absorb exception for now
		}
	}
	finally {
		// Make sure to do the following so don't lock up
		this.historyBeingWritten = false;
	}
}

/**
Write the UI state properties.
*/
public void writeUIState ()
{
	if ( getUserFolder().equals("/") ) {
		// Don't allow files to be created under root on Linux
		return;
	}

	// Make sure the UI state file exists.
	checkUIStateFile();

	long ms = System.currentTimeMillis();
	while ( this.uiStateBeingWritten ) {
		// Need to wait until another operation finishes writing
		// But don't wait longer than a couple of seconds before moving on
		if ( (System.currentTimeMillis() - ms) > 2000 ) {
			break;
		}
	}
	// Now can continue
	this.uiStateBeingWritten = true;
	try {
		try {
			//Message.printStatus(1, "", "Writing UI state" );
			this.uiStateProps.writePersistent();
		}
		catch ( Exception e ) {
			// Absorb exception for now
		}
	}
	finally {
		// Make sure to do the following so don't lock up
		this.uiStateBeingWritten = false;
	}
}

}