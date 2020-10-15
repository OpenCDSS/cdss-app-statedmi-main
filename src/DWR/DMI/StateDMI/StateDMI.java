// StateDMI -  Main program class that runs batch or UI.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataDimension;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreConnectionUIProvider;
import riverside.datastore.DataStoreFactory; 

/**
The StateDMI class is the entry point into the StateDMI application.  The
program can run either in batch mode or UI application.
*/
public class StateDMI
{

public static final String PROGRAM_NAME = "StateDMI";
public static final String PROGRAM_VERSION = "5.00.01dev (2020-10-14)";

/**
Interface for StateCU commands.
*/
public static final int APP_TYPE_STATECU = 0;
/**
Interface for StateMod commands.
*/
public static final int APP_TYPE_STATEMOD = 1;
/**
Interface for StateGW commands (not yet implemented).
*/
public static final int APP_TYPE_STATEGW = 2;

/**
Path to the configuration file.  This cannot be defaulted until the -home command line parameter is processed.
*/
private static String __configFile = "";

/**
Main GUI instance.
*/
private static StateDMI_JFrame __stateDMI_JFrame;

/**
List of properties to control the software from the configuration file and passed in on the command line.
*/
private static PropList __statedmi_props = null;

/**
Home directory for system install (e.g., "\CDSS").
*/
private static String __statedmiInstallHome = null;

/**
Default model application type for StateDMI GUI "look" (until instructed otherwise by customer).
*/
private static int __app_type = APP_TYPE_STATECU;
							

public static boolean runGUIWithSelectedCommandFile = false;
private static String[] __args;

/**
Commands file being processed when run in batch mode with -commands File.
*/
private static String __commandFile = null;

/*
Log file from the command line.  Parent folder must exist to create.
*/
private static String __logFileFromCommandLine = null;

/**
Helper method to return the arguments this program was run with.
@return List of arguments the program was run with, as a string with arguments separated by spaces.
*/
public static String getArgs()
{
	String arguments = "";
	for ( int i = 0; i < __args.length; i++ ) {
		arguments += (" " + __args[i]);
	}
	return arguments;
}


/**
Return the command file that is being processed, or null if not being run in batch mode.
@return the path to the commands file to run.
*/
private static String getCommandFile ()
{
	return __commandFile;
}

/**
Return the name of the configuration file for the session.  This file will be determined
from the -home command line parameter during command line parsing (default) and can be
specified with -config File on the command line (typically used to test different configurations).
@return the full path to the configuration file.
*/
public static String getConfigFile ()
{
    return __configFile;
}

/**
Return the JFrame instance for the main application.
@return the JFrame instance for use with low-level code that needs to pop up dialogs, etc.
*/
public static StateDMI_JFrame getJFrame ()
{	return __stateDMI_JFrame;
}

/**
Return a StateDMI property.  The properties are defined in the StateDMI configuration file.
@param propertyExp name of property to look up as a Java regular expression.
@return the value(s) for a StateDMI configuration property, or null if a properties file does not exist.
Return null if the property is not found (or if no configuration file exists for StateDMI).
*/
public static List<Prop> getProps ( String propertyExp )
{   if ( __statedmi_props == null ) {
        return null;
    }
    return __statedmi_props.getPropsMatchingRegExp(propertyExp);
}

/**
Return the value of a StateDMI property.  The properties are defined in the StateDMI configuration file.
@param property name of property to look up.
@return the value for a StateDMI configuration property, or null if a properties file does not exist.
Return null if the property is not found (or if no configuration file exists for StateDMI).
*/
public static String getPropValue ( String property )
{	if ( __statedmi_props == null ) {
		return null;
	}
	return __statedmi_props.getValue ( property );
}

/**
Initialize default message levels.  See initialize2() for initialization after
command line arguments have been parsed.
*/
private static void initialize1 ()
{	// Initialize message levels...

	Message.setDebugLevel( Message.TERM_OUTPUT, 0 );
	Message.setDebugLevel( Message.LOG_OUTPUT, 0 );
	Message.setStatusLevel( Message.TERM_OUTPUT, 0 );
	Message.setStatusLevel( Message.LOG_OUTPUT, 2 );
	Message.setWarningLevel( Message.TERM_OUTPUT, 0 );
	Message.setWarningLevel( Message.LOG_OUTPUT, 3 );
	// Will capture FYI warnings as well as command-level (2)
	// warnings and interface level warnings (1).

	// Indicate that message levels should be shown in messages, to allow
	// for a filter when displaying messages...

	Message.setPropValue ( "ShowMessageLevel=true" );
	Message.setPropValue ( "ShowMessageTag=true" );
}

/**
Initialize private data after arguments are processed (mainly to know the home directory).
*/
private static void initialize2 ()
{	String routine = "StateDMI.initialize2";

	// Check the home directory...

	if ( __statedmiInstallHome == null ) {
		__statedmiInstallHome = "\\CDSS";
		Message.printWarning ( 1, routine,
		"-home was not specified on the command line.  Assuming \"" + __statedmiInstallHome + "\"" );
	}
	
	// Set up the units conversion data.  For now read from the DATAUNIT
	// file but in the future may read from HydroBase...

	String units_file = __statedmiInstallHome + File.separator + "system" + File.separator + "DATAUNIT";

	try {
		DataUnits.readUnitsFile( units_file );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error reading units file \"" + units_file + "\"\n" +
		"Some conversions will not be supported.\n" +
		"Default output precisions may not be appropriate." );
		try {
			DataDimension dim = DataDimension.lookupDimension ( "L" );
			String dim_abbrev = dim.getAbbreviation();
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 1, "MM", "Millimeter", 1, 1, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 0, "CM", "Centimeter", 2, 10, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 0, "M", "Meter", 2, 1000, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 0, "KM", "Kilometer", 1, 1000000, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 0, "IN", "Inch", 2, 25.4, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev,	0, "FT", "Feet", 2, 304.8, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 0, "MI", "Mile", 1, 1609344, 0 ) );
		
			dim = DataDimension.lookupDimension ( "L3" );
			DataUnits.addUnits( new DataUnits ( dim_abbrev, 1, "ACFT", "Acre-feet", 1, 1, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev,	0, "AF", "Acre-feet", 1, 1, 0 ) );
			DataUnits.addUnits( new DataUnits ( dim_abbrev,	0, "AF/M", "AF/Month", 1, 1, 0 ) );
		}
		catch ( Exception e2 ) {
			// Trouble configuring units.
			Message.printWarning ( 2, routine, "Trouble setting default units conversions." );
			Message.printWarning ( 2, routine, e );
		}
	}
}

/**
Instantiates the main application instance when run stand-alone.
@param args Command line arguments.
*/
public static void main ( String args[] )
{	String routine = "StateDMI.main";

	try {
		// Main try...
	
		// Initial set...
		IOUtil.setProgramData ( PROGRAM_NAME, PROGRAM_VERSION, args );
		JGUIUtil.setAppNameForWindows("StateDMI");
		StateDMISession session = StateDMISession.getInstance(getMajorVersion());
		
	    // Set properties for testing.
	    // TODO SAM 2016-05-15 This needs to go into a StateDMI.cfg file
	    String diffProg = "C:\\Program Files\\KDiff3\\kdiff3.exe";
	    File f = new File ( diffProg );
	    if ( f.exists() && f.canExecute() ) {
	    	IOUtil.setProp("DiffProgram", diffProg);
	    }
	
		// Note that messages will not be printed to the log file until the log file is opened below.
	
		initialize1();
	
		try {
			parseArgs ( session, args );
		}
		catch ( Exception e ) {
	        Message.printWarning ( 1, routine, "Error parsing command line arguments.  Using default behavior." );
	        Message.printWarning( 1, "StateDMI", e );
	    }
	
		initialize2();
	
		setIcon();
	
		if ( IOUtil.isBatch() ) {
			// Running like "statedmi -commands file"
			StateDMICommandFileRunner runner = new StateDMICommandFileRunner();
		    // Open the HydroBase connection if the configuration file specifies the information.  Do this before
			// reading the command file because commands may try to run discovery during load.
	        openHydroBase ( runner.getProcessor() );
			try {
			    String command_file_full = getCommandFile();
			    Message.printStatus( 1, routine, "Running command file in batch mode:  \"" + command_file_full + "\"" );
				runner.readCommandFile ( command_file_full );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine, "Error reading command file \"" +
						getCommandFile() + "\".  Unable to run commands." );
				Message.printWarning ( 1, routine, e );
				quitProgram ( 1 );
			}
			try {
			    // The following will throw an exception if there are any errors running.
	            runner.runCommands();
                quitProgram ( 0 );
			}
			catch ( Exception e ) {
				// Some type of error
				Message.printWarning ( 1, routine, "Error running command file \"" + getCommandFile() + "\"." );
				Message.printWarning ( 1, routine, e );
				quitProgram ( 1 );
			}
		}
		/* FIXME SAM 2007-10-22 Need to enable
		else if ( __is_server ) {
			// Run in server mode via the GUI object.  This goes into a loop...
			__tstool_JFrame.runServer();
		}
		*/
		else {
			// Start the interface...
	
			Message.printStatus ( 2, routine, "Starting StateDMI GUI..." );
			try {
				__stateDMI_JFrame = new StateDMI_JFrame (session, __app_type );
			}
			catch ( Exception e2 ) {
				Message.printWarning ( 1, routine, "Error starting StateDMI." );
				Message.printWarning ( 1, routine, e2 );
				quitProgram ( 1 );
			}
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine, "Error starting StateDMI." );
		Message.printWarning ( 2, routine, e );
		e.printStackTrace();
		quitProgram ( 1 );
	}
}

/**
 * Return the StateDMI major version or zero if an issue (should not happen).
 * @return the major StateDMI version
 */
private static int getMajorVersion () {
    int majorVersion = 0;
    try {
    	System.err.println("program version: " + IOUtil.getProgramVersion());
    	majorVersion = Integer.parseInt(IOUtil.getProgramVersion().split("\\.")[0].trim());
    	System.err.println("Major version: " + majorVersion);
    }
    catch ( Exception e ) {
    	Message.printWarning(1, "StateDMI", "Error getting StateDMI major version number (" + e + ")." );
    }
    return majorVersion;
}

/**
Open a datastore given its configuration properties.  The datastore is also added to the processor
(if the open fails then the datastore should set status=1).
The StateDMI configuration file properties are checked here to ensure that the datastore type is enabled.
Otherwise, opening datastores takes time and impacts performance.
@param session StateDMI session, which provides user and environment information
@param dataStoreProps datastore configuration properties recognized by the datastore factory "create" method.
@param processor time series command processor that will use/manage the datastore
@param isBatch indicate whether running in batch mode - if true, do not open datastores with login of "prompt"
*/
protected static DataStore openDataStore ( StateDMISession session, PropList dataStoreProps,
	StateDMI_Processor processor, boolean isBatch )
throws ClassNotFoundException, IllegalAccessException, InstantiationException, Exception
{   String routine = "StateDMI.openDataStore";
    // Open the datastore depending on the type
    String dataStoreType = dataStoreProps.getValue("Type");
    String dataStoreConfigFile = dataStoreProps.getValue("DataStoreConfigFile");
    Message.printStatus(2,routine,"DataStoreConfigFile="+dataStoreConfigFile);
    // For now hard-code this here
    // TODO SAM 2010-09-01 Make this more elegant
    String packagePath = ""; // Make sure to include trailing period below
    // TODO SAM 2016-03-25 Need to figure out how software feature set can be generically disabled without coding name here
    // Similar checks are done in the StateDMI UI to enable/disable UI features
    String propValue = null; // From software installation configuration
    String userPropValue = null; // From user configuration
    if ( dataStoreType.equalsIgnoreCase("ColoradoHydroBaseRestDataStore") ) {
        propValue = getPropValue("StateDMI.ColoradoHydroBaseRestEnabled");
    	userPropValue = session.getUserConfigPropValue ( "ColoradoHydroBaseRestEnabled" );
    	if ( (userPropValue != null) && !userPropValue.isEmpty() ) {
    		propValue = userPropValue;
    	}
    	// Default if not specified is true
        if ( (propValue == null) || propValue.equalsIgnoreCase("True") ) {
            packagePath = "cdss.dmi.hydrobase.rest.";
        }
    }
    // TODO @jurentie: this class is not implemented yet 08-01-2018
    else if ( dataStoreType.equalsIgnoreCase("GenericDatabaseDataStore") ) {
        // No need to check whether enabled or not since a generic connection
        // Specific configuration files will indicate if enabled
    	// TODO SAM 2016-02-19 Need to move to more appropriate path - don't confuse with RiversideDBDataStore
        packagePath = "riverside.datastore.";
    }
    else if ( dataStoreType.equalsIgnoreCase("HydroBaseDataStore") ) {
        propValue = getPropValue("StateDMI.HydroBaseEnabled");
    	userPropValue = session.getUserConfigPropValue ( "HydroBaseEnabled" );
    	// Default if not specified is true
    	if ( (userPropValue != null) && !userPropValue.isEmpty() ) {
    		propValue = userPropValue;
    	}
        if ( (propValue == null) || propValue.equalsIgnoreCase("True") ) {
            packagePath = "DWR.DMI.HydroBaseDMI.";
        }
    }
    if ( !packagePath.equals("") ) {
        propValue = dataStoreProps.getValue("Enabled");
        if ( (propValue != null) && propValue.equalsIgnoreCase("False") ) {
            // Datastore is disabled.  Do not even attempt to load.  This will minimize in-memory resource use.
            Message.printStatus(2, routine, "Created datastore \"" + dataStoreType + "\", name \"" +
                dataStoreProps.getValue("Name") + "\" is disabled.  Not opening." );
            return null;
        }
        else{
        	// Datastore is enabled
            StopWatch sw = new StopWatch();
            sw.start();
            String className = packagePath + dataStoreType + "Factory";
            DataStoreFactory factory = null;
            
            Message.printStatus(2, routine, "Getting class for name \"" + className + "\"" );
            Class clazz = Class.forName( className );
            Message.printStatus(2, routine, "Creating instance of class \"" + className + "\"" );
        	factory = (DataStoreFactory)clazz.newInstance();
        	
        	// Check for a login of "prompt"
        	String systemLogin = dataStoreProps.getValue("SystemLogin");
        	String systemPassword = dataStoreProps.getValue("SystemPassword");
            if ( ((systemLogin != null) && systemLogin.equalsIgnoreCase("prompt")) ||
            	((systemPassword != null) && systemPassword.equalsIgnoreCase("prompt"))	) {
                // If in batch mode, skip
            	if ( isBatch ) {
                    Message.printStatus(2, routine, "Skipping datastore \"" + dataStoreType + "\", name \"" +
                        dataStoreProps.getValue("Name") + "\" because in batch mode.  Will prompt for login when not in batch mode." );
                    return null;
            	}
            	else {
            		// Not in batch mode - open the datastore using a prompt initiated from the StateDMI UI
            		if ( factory instanceof DataStoreConnectionUIProvider ) {
        	            // Create the datastore instance using the properties in the configuration file
            			// supplemented by login/password from interactive input
        	        	// Add to the processor even if it does not successfully open so that UI can show
        	        	// TODO SAM 2015-02-15 Need to update each factory to handle partial opens
            			Message.printStatus(2, routine, "Opening datastore \"" + dataStoreType + "\", name \"" +
                            dataStoreProps.getValue("Name") + "\" via prompt from StateDMI UI." );
            			DataStoreConnectionUIProvider uip = (DataStoreConnectionUIProvider)factory;
        	            DataStore dataStore = uip.openDataStoreConnectionUI(dataStoreProps,getJFrame());
        	            sw.stop();
        	            if ( dataStore == null ) {
        	            	Message.printStatus(2, routine, "Datastore \"" + dataStoreProps.getValue("Name") +
        	            		"\" is null after opening from StateDMI UI - this is unexpected." );
        	            }
        	            else {
	        	            // Add the datastore to the processor
	        	            processor.setPropContents ( "DataStore", dataStore );
	        	            Message.printStatus(2, routine, "Opening datastore type \"" + dataStoreType + "\", name \"" +
	        	                dataStore.getName() + "\" took " + sw.getMilliseconds() + " ms" );
        	            }
        	            return dataStore;
            		}
                	else {
        	            Message.printStatus(2, routine, "Not opening datastore type \"" + dataStoreType + "\", name \"" +
        	            	dataStoreProps.getValue("Name") + "\" because prompt is requested but datastore does not offer interface." );
                		return null;
                	}
            	}
            }
            else {
	            // Create the datastore instance using the properties in the configuration file
	        	// Add to the processor even if it does not successfully open so that UI can show
	        	// TODO SAM 2015-02-15 Need to update each factory to handle partial opens
	            DataStore dataStore = factory.create(dataStoreProps);
	            // Add the datastore to the processor
	            processor.setPropContents ( "DataStore", dataStore );
	            Message.printStatus(2, routine, "DataStore properties are: " + dataStore.getProperties().toString(","));
	            sw.stop();
	            Message.printStatus(2, routine, "Opening datastore type \"" + dataStoreType + "\", name \"" +
	                dataStore.getName() + "\" took " + sw.getMilliseconds() + " ms" );
	            return dataStore;
            }
        }
    }
    else {
        return null;
    }
}

/**
Open the datastores (e.g., database and web service connection(s)) using datastore configuration files.
This method can be called from the UI code to automatically establish database startup database connections.
@param session StateDMI session, which provides user and environment information
@param processor command processor that will have datastores opened
@param isBatch is the software running in batch mode?  If in batch mode do not open up datastores
that have a login of "prompt".
*/
protected static void openDataStoresAtStartup ( StateDMISession session, StateDMI_Processor processor,
	boolean isBatch ) {
	String routine = "StateDMI.openDataStoresAtStartup";
	
    Message.printStatus(2, routine, "Searching for StateDMI datastore configuration files in folder:  \"" +
    	session.getInstallDatastoresFolder() + "\"." );
    
    // Allow multiple database connections via the new convention using datastore configuration files.
    // List configuration files in the 'datastores' folder of the software installation.
    List<String> dataStoreConfigFiles = new ArrayList<String>();
    // First list the cfg files
    String installDatastoresFolder = session.getInstallDatastoresFolder();
    List<File> installConfigFiles = IOUtil.getFilesMatchingPattern(installDatastoresFolder, "cfg", true);
    Message.printStatus(2, routine, "Found " + installConfigFiles.size() + " *.cfg files in \"" + installDatastoresFolder );
    // Convert to String
    for ( File f : installConfigFiles ) {
    	dataStoreConfigFiles.add(f.getAbsolutePath());
    }
    // Also get names of datastore configuration files from user's datastores folder .statedmi/NN/datastores
    if ( session.createUserDatastoresFolder() ) {
	    String datastoreFolder = session.getUserDatastoresFolder();
	    File f = new File(datastoreFolder);
	    FilenameFilter ff = new FilenameFilter() {
	    	public boolean accept(File dir, String name) {
	    		if ( name.toLowerCase().endsWith(".cfg") ) {
	    			return true;
	    		}
	    		else {
	    			return false;
	    		}
	    	}
	    };
	    String [] dfs = f.list(ff); // Returns files without leading path
	    if ( dfs != null ) {
	    	for ( int i = 0; i < dfs.length; i++ ) {
	    		String datastoreFile = datastoreFolder + File.separator + dfs[i];
	    		dataStoreConfigFiles.add(datastoreFile);
    			Message.printStatus(2, routine, "Found user datastore configuration file: " + datastoreFile );
	    	}
	    }
    }
    // Now open the datastores for found configuration files
    // - loop backwards since user files were added last
    // - if a duplicate is found, use the user version first
    int nDataStores = dataStoreConfigFiles.size();
    // Datastore names that have been opened, so as to avoid reopening.  User datastores are opened first.
    List<String> openDataStoreNameList = new ArrayList<String>();
	Message.printStatus(2, routine, "Trying to open " + dataStoreConfigFiles.size() + " data stores (first user, then installation)." );
    for ( int iDataStore = nDataStores - 1; iDataStore >= 0; iDataStore-- ) {
    	String dataStoreFile = dataStoreConfigFiles.get(iDataStore);
        Message.printStatus ( 2, routine, "Opening datastore using properties in \"" + dataStoreFile + "\".");
        // Read the properties from the configuration file
        PropList dataStoreProps = new PropList("");
        String dataStoreFileFull = dataStoreFile;
        if ( !IOUtil.fileExists(dataStoreFileFull) ) {
            Message.printWarning(3, routine, "Datastore configuration file \"" + dataStoreFileFull +
                "\" does not exist - not opening datastore." );
        }
        else {
            dataStoreProps.setPersistentName(dataStoreFileFull);
            String dataStoreClassName = "";
            try {
                // Get the properties from the file
                dataStoreProps.readPersistent();
                String dataStoreName = dataStoreProps.getValue("Name");
                // See if the datastore name matches one that is already open, if so, ignore it
                boolean dataStoreAlreadyOpened = false;
                for ( String openDataStoreName : openDataStoreNameList ) {
                	if ( openDataStoreName.equalsIgnoreCase(dataStoreName)) {
                		// Found a matching datastore
                		Message.printStatus(2,routine,"Datastore \"" + dataStoreName +
                			"\" is already open (user datastores are used before system datastores).  Skipping.");
                		dataStoreAlreadyOpened = true;
                		break;
                	}
                }
                if ( dataStoreAlreadyOpened ) {
                	continue;
                }
                // Also assign the configuration file path property to facilitate file processing later
                // (e.g., to locate related files referenced in the configuration file, such as lists of data
                // that are not available from web services)
                dataStoreProps.set("DataStoreConfigFile",dataStoreFileFull);
                openDataStore ( session, dataStoreProps, processor, isBatch );
                // Save the datastore name so duplicates are not opened
                openDataStoreNameList.add(dataStoreName);
            }
            catch ( ClassNotFoundException e ) {
                Message.printWarning (2,routine, "Datastore class \"" + dataStoreClassName +
                    "\" is not in the class path - report to software support (" + e + ")." );
                Message.printWarning(2, routine, e);
            }
            catch( InstantiationException e ) {
                Message.printWarning (2,routine, "Error instantiating datastore for class \"" + dataStoreClassName +
                    "\" - report to software support (" + e + ")." );
                Message.printWarning(2, routine, e);
            }
            catch( IllegalAccessException e ) {
                Message.printWarning (2,routine, "Datastore for class \"" + dataStoreClassName +
                    "\" needs a no-argument constructor - report to software support (" + e + ")." );
                Message.printWarning(2, routine, e);
            }
            catch ( Exception e ) {
                Message.printWarning (2,routine,"Error reading datastore configuration file \"" +
                    dataStoreFileFull + "\" - not opening datastore (" + e + ")." );
                Message.printWarning(2, routine, e);
            }
        }
    }
}

//TODO SAM 2010-02-03 Evaluate whether non-null HydroBaseDMI return is OK or whether
//should rely on exceptions.
/**
Open the HydroBase connection using the CDSS configuration file information, when running
in batch mode or when auto-connecting in the GUI.  The CDSS configuration file is used to determine
HydroBase server and database name properties to use for the initial connection.  If no configuration file
exists, then a default connection is attempted.
@param processor the command processor that needs the (default) HydroBase connection.
@return opened HydroBaseDMI if the connection was made, or null if a problem.
*/
public static HydroBaseDMI openHydroBase ( StateDMI_Processor processor )
{   String routine = "StateDMI.openHydroBase";
	if ( IOUtil.isBatch() ) {//|| autoConnect ) {
	    // Running in batch mode or without a main GUI so automatically
	    // open HydroBase from the CDSS.cfg file information...
	    // Get the input needed to process the file...
	    String hbcfg = HydroBase_Util.getConfigurationFile();
	    PropList props = null;
	     
	    if ( IOUtil.fileExists(hbcfg) ) {
	        // Use the configuration file to get HydroBase properties...
	        Message.printStatus(2, routine, "HydroBase configuration file \"" + hbcfg +
	        "\" is being used to open HydroBase connection at startup." );
	        try {
	            props = HydroBase_Util.readConfiguration(hbcfg);
	        }
	        catch ( Exception e ) {
	            Message.printWarning ( 1, routine,
	            "Error reading CDSS configuration file \""+ hbcfg + "\".  Using defaults for HydroBase." );
	            Message.printWarning ( 3, routine, e );
	            props = null;
	        }
	    }
	    else {
	        Message.printStatus(2, routine, "HydroBase configuration file \"" + hbcfg +
	            "\" does not exist - not opening HydroBase connection at startup." );
	    }
	     
	    try {
	        // Now open the database...
	        // This uses the guest login.  If properties were not found,
	        // then default HydroBase information will be used.
	        HydroBaseDMI hbdmi = new HydroBaseDMI ( props );
	        hbdmi.open();
	        List<HydroBaseDMI> hbdmi_Vector = new Vector<HydroBaseDMI>(1);
	        hbdmi_Vector.add ( hbdmi );
	        processor.setPropContents ( "HydroBaseDMIList", hbdmi_Vector );
	        Message.printStatus(2, routine, "Successfully opened HydroBase connection." );
	        return hbdmi;
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( 1, routine, "Error opening HydroBase.  HydroBase features will be disabled." );
	        Message.printWarning ( 3, routine, e );
	        return null;
	    }
	}
	return null; // Probably will not get here
}

/**
Open the log file.  This should be done as soon as the application home
directory is known so that remaining information can be captured in the log file.
*/
private static void openLogFile ( StateDMISession session )
{	String routine = "StateDMI.openLogFile";

	String logFile = null;
	// Default as of StateDMI 5 is to open the log file as $home/.tstool/NN/logs/StateDMI_user.log file unless specified on command line
	if ( __logFileFromCommandLine != null ) {
		File f = new File(__logFileFromCommandLine);
		if ( !f.getParentFile().exists() ) {
			Message.printWarning ( 1, routine, "Error opening log file \"" + __logFileFromCommandLine +
				"\" - log file parent folder does not exist.");
		}
		else {
			logFile = __logFileFromCommandLine;
			Message.printStatus ( 1, routine, "Log file name from -logFile: " + logFile );
			try {
                Message.openLogFile ( logFile );
                // Do it again so it goes into the log file
                Message.printStatus ( 1, routine, "Log file name from -logFile: " + logFile );
			}
			catch (Exception e) {
				Message.printWarning ( 1, routine, "Error opening log file \"" + logFile + "\"");
			}
		}
	}
	else {
		// Get the log file name from the session object...under user home folder
		if ( session.createUserLogsFolder() ) {
			// Log folder already exists or was created, so OK to use
			logFile = session.getUserLogFile();
			Message.printStatus ( 1, routine, "Log file name from StateDMI default: " + logFile );
			try {
                Message.openLogFile ( logFile );
                // Also log for troubleshooting
                Message.printStatus ( 1, routine, "Log file name from StateDMI default: " + logFile );
			}
			catch (Exception e) {
				Message.printWarning ( 1, routine, "Error opening log file \"" + logFile + "\"");
			}
		}
		else {
			Message.printWarning ( 2, routine, "Unable to create/open StateDMI log folder \"" + session.getUserLogsFolder() + "\".  Not opening log file.");
		}
	}
}

/**
Parse command line arguments.
@param args Command line arguments.
*/
public static void parseArgs ( StateDMISession session, String[] args )
throws Exception
{	String routine = "StateDMI.parseArgs";

    // Allow setting of -home via system property "statedmi.home". This
    // can be supplied by passing the -Dstatedmi.home=HOME option to the java vm.
    // this little block of code copies the passed values into the front of the args array.
    if (System.getProperty("statedmi.home") != null) {
        String[] extArgs = new String[args.length + 2];
        System.arraycopy(args, 0, extArgs, 2, args.length);
        extArgs[0] = "-home";
        extArgs[1] = System.getProperty("statedmi.home");
        args = extArgs;
    }

	__args = args;
	for (int i = 0; i < args.length; i++) {
		// List alphabetically...
		if ( args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help") ||
			args[i].equalsIgnoreCase("/h") || args[i].equals("/?") ) {
			printUsage ();
		}
		else if (args[i].equalsIgnoreCase("-home")) {
			if ( i == (args.length - 1) ) {
				Message.printWarning ( 1, routine,"No argument provided to '-home'");
				continue;
			}
			i++;
			//__statedmiInstallHome = args[i];
			__statedmiInstallHome = (new File(args[i])).getCanonicalPath().toString();

			// Open the log file so that remaining messages will be seen in the log file...
			openLogFile(session);
			
			Message.printStatus ( 1, routine, "StateDMI install folder from -home command line parameter is \"" + __statedmiInstallHome + "\"" );
			// The default configuration file location is relative to the install home.  This works
			// as long as the -home argument is first in the command line.
			setConfigFile ( __statedmiInstallHome + File.separator + "system" + File.separator + "StateDMI.cfg" );
			IOUtil.setProgramWorkingDir(__statedmiInstallHome);
			IOUtil.setApplicationHomeDir(__statedmiInstallHome);
			JGUIUtil.setLastFileDialogDirectory(__statedmiInstallHome);
			
			readConfigFile(getConfigFile());
		}
		else if (args[i].equalsIgnoreCase("-logFile")) {
		    // Specify the log file.
			if ((i + 1)== args.length) {
				String message = "No argument provided to '-logFile'";
				Message.printWarning(1,routine,message);
				throw new Exception(message);
			}
			i++;
			__logFileFromCommandLine = args[i];
		}
		else if (args[i].equalsIgnoreCase("-statecu")) {
			Message.printStatus ( 1, routine, "Running StateDMI for StateCU files." );
			__app_type = APP_TYPE_STATECU;
		}
		else if (args[i].equalsIgnoreCase("-statemod")) {
			Message.printStatus ( 1, routine, "Running StateDMI for StateMod files." );
			__app_type = APP_TYPE_STATEMOD;
		}
		else if (args[i].equalsIgnoreCase("-test")) {
			IOUtil.testing(true);
		}
		else if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("--version")) {
			printVersion ();
		}
		else if ( args[i].regionMatches(true,0,"-d",0,2)) {
			// Set debug information...
			if ((i + 1)== args.length) {
				// No argument.  Turn terminal and log file debug on to level 1...
				Message.isDebugOn = true;
				Message.setDebugLevel ( Message.TERM_OUTPUT, 1);
				Message.setDebugLevel ( Message.LOG_OUTPUT, 1);
			}
			i++;
			if ( args[i].indexOf(",") >= 0 ) {
				// Comma, set screen and file debug to different levels...
				String token = StringUtil.getToken(args[i],",",0,0);
				if ( StringUtil.isInteger(token) ) {
					Message.isDebugOn = true;
					Message.setDebugLevel ( Message.TERM_OUTPUT, StringUtil.atoi(token) );
				}
				token=StringUtil.getToken(args[i],",",0,1);
				if ( StringUtil.isInteger(token) ) {
					Message.isDebugOn = true;
					Message.setDebugLevel ( Message.LOG_OUTPUT, StringUtil.atoi(token) );
				}
			}
			else {
				// No comma.  Turn screen and log file debug on to the requested level...
				if ( StringUtil.isInteger(args[i]) ) {
					Message.isDebugOn = true;
					Message.setDebugLevel (	Message.TERM_OUTPUT, StringUtil.atoi(args[i]) );
					Message.setDebugLevel ( Message.LOG_OUTPUT, StringUtil.atoi(args[i]) );
				}
			}
		}
		else if (args[i].equalsIgnoreCase("-commands")) {
		    // Command file name
			if ((i + 1)== args.length) {
				Message.printWarning(1,routine, "No argument provided to '-commands'");
				throw new Exception("No argument provided to '-commands'");
			}
			i++;
			setupUsingCommandFile ( args[i], true );
		}

		// User specified or specified by a script/system call to the normal StateDMI script/launcher.
		else {
		    // Assume that a command file has been specified on the command line
		    setupUsingCommandFile ( args[i], false );
		}
	}
}

/**
Print the program usage to the log file.
*/
public static void printUsage ( )
{	String nl = System.getProperty ( "line.separator" );
	String routine = "StateDMI.printUsage";
	String usage =  nl +
"Usage:  " + PROGRAM_NAME + " [options]" + nl + nl +
"StateDMI creates the input files for the StateCU and StateMod models." + nl+
"The following command line options are recognized (-statecu is assumed and"+nl+
"either -statecu OR -statemod can be specified):"+ nl + nl +
"-h OR /h OR /? OR -help" + nl +
"                     Print this help information." + nl +
"-home Dir            Specify the home directory for CDSS (e.g., \\CDSS)." +nl+
"                     The default is \"\\CDSS\" on the current drive." + nl +
"                     Supporting files are located using this directory." + nl +
"-statecu             Run StateDMI for StateCU files." + nl +
"-statemod            Run StateDMI for StateMod files." + nl +
//"-d#[,#]            Sets the debug level.  The first number applies to screen"+nl+
//"                   output.  The second to the log file.  If only one value"+nl+
//"                   is given, it will be applied to screen and file output."+nl+
"-v                   Print the program version."+nl+
//"-w#[,#]            Sets the warning level.  The first number applies to" +nl+
//"                   screen output.  The second to the log file.  If only one"+nl+
//"                   value is given, it will be applied to screen and file" +nl+
//"                   output."
		nl;
	//System.err.println ( usage );
	Message.printStatus ( 1, routine, usage );
	quitProgram(0);
}

/**
Print the program version and exit the program.
*/
public static void printVersion ( )
{	String nl = System.getProperty ( "line.separator" );
	System.err.println (  nl + PROGRAM_NAME + " version: " + PROGRAM_VERSION + nl + nl +
	"StateDMI is a part of Colorado's Decision Support Systems (CDSS)\n" +
	"Copyright (C) 1997-2019 Colorado Department of Natural Resources\n" +
	"\n" +
	"StateDMI is free software:  you can redistribute it and/or modify\n" +
	"    it under the terms of the GNU General Public License as published by\n" +
	"    the Free Software Foundation, either version 3 of the License, or\n" +
	"    (at your option) any later version.\n" +
	"\n" +
	"StateDMI is distributed in the hope that it will be useful,\n" +
	"    but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
	"    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
	"    GNU General Public License for more details.\n" +
	"\n" +
	"You should have received a copy of the GNU General Public License\n" +
	"    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.\n" );

	quitProgram (0);
}

/**
Clean up and quit the program.
@param status Exit status.
*/
static void quitProgram ( int status )
{	String	routine="StateDMI.quitProgram";

	Message.printStatus ( 1, routine, "Exiting with status " + status + "." );
	System.err.print( "STOP " + status + "\n" );
	Message.closeLogFile();
	System.exit ( status ); 
}

/**
Read the configuration file.  This should be done as soon as the application home is known.
TODO SAM 2015-01-07 need to store configuration information in a generic "session" object to be developed.
@param configFile Name of the configuration file.
*/
private static void readConfigFile ( String configFile )
{	String routine = "StateDMI.readConfigFile";
    Message.printStatus ( 2, routine, "Reading StateDMI configuration information from \"" + configFile + "\"." );
	if ( IOUtil.fileReadable(configFile) ) {
		__statedmi_props = new PropList ( configFile );
		__statedmi_props.setPersistentName ( configFile );
		try {
            __statedmi_props.readPersistent ();
            // Print out the configuration information since it is useful in troubleshooting.
            int size = __statedmi_props.size();
            for ( int i = 0; i < size; i++ ) {
                Prop prop = __statedmi_props.elementAt(i);
                Message.printStatus( 2, routine, prop.getKey() + "=" + prop.getValue() );
                if ( prop.getKey().equalsIgnoreCase("StateDMI.DiffProgram") ) {
                	// Also set global properties that are used more generically
                	IOUtil.setProp("DiffProgram", prop.getValue());
                }
            }
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error reading StateDMI configuration file \"" + configFile + "\".  StateDMI may not start (" + e + ")." );
			Message.printWarning ( 1, routine, e );
		}
	}
	else {
	    Message.printWarning ( 1, routine,
	        "StateDMI configuration file \"" + configFile + "\" is not readable.  StateDMI may not start." );
	}
}

/**
Set the commands file that is being used with StateDMI.
@param commandFile Commands file being processed, when started with
-commands File parameter.  This indicates that a batch run should be done, with
no main StateDMI GUI, although windows may display for graphical products.
*/
private static void setCommandFile ( String commandFile )
{
	__commandFile = commandFile;
}

/**
Set the configuration file that is being used with StateDMI.  If a relative path is
given, then the file is made into an absolute path by using the working directory.
Typically an absolute path is provided when the -home command line parameter is parsed
at startup, and a relative path may be provided if -config is specified on the command line.
@param configFile Configuration file.
*/
private static void setConfigFile ( String configFile )
{
    __configFile = configFile;
}

/**
Set the icon for the application.  This will be used for all windows.
*/
public static void setIcon ( )
{	// First try loading the icon from the JAR file or class path...
	String icon_file = "StateDMICDSSIcon32.gif";
	String icon_path ="";
	try {
        // The icon files live in the main application folder in the classpath.
        icon_path = "DWR/DMI/StateDMI/" + icon_file;
		JGUIUtil.setIconImage( icon_path );
	}
	catch ( Exception e ) {
		Message.printStatus ( 2, "", "StateDMI icon \"" + icon_path + "\" does not exist in classpath." );
	}
}

/**
Setup the application using the specific command file, which either came in on the
command line with "-commands CommandFile" or simply as an argument.
@param command_file_arg Command file from the command line argument (no processing on the argument
before this call).
@param is_batch Indicates if the command file was specified with "-commands CommandFile",
indicating that a batch run is requested.
*/
private static void setupUsingCommandFile ( String command_file_arg, boolean is_batch )
{   String routine = "StateDMI.setupUsingCommandFile";

    // Make sure that the command file is an absolute path because it indicates the working
    // directory for all other processing.
    String user_dir = System.getProperty("user.dir");
    Message.printStatus (1, routine, "Startup (user.dir) directory is \"" + user_dir + "\"");
    String command_file_canonical = null;   // Does not need to be absolute
    File command_file_File = new File(command_file_arg);
    File command_file_full_File = null;
    String command_file_full = null;
    try {
        command_file_canonical = command_file_File.getCanonicalPath();
        Message.printStatus( 1, routine, "Canonical path for command file is \"" + command_file_canonical + "\"" );
    }
    catch ( Exception e ) {
        String message = "Unable to determine canonical path for \"" + command_file_arg + "\"." +
        "Check that the file exists and read permissions are granted.  Not using command file.";
        Message.printWarning ( 1, routine, message );
        System.err.println ( message );
                
        return;
    }
    
    // Get the absolute path to the command file.
    // TODO SAM 2008-01-11 Shouldn't a canonical path always be absolute?
    File command_file_canonical_File = new File ( command_file_canonical );
    if ( command_file_canonical_File.isAbsolute() ) {
        command_file_full = command_file_canonical;
    }
    else {
        // Append the command file to the user directory and set the working directory to
        // the resulting directory.
        command_file_full = user_dir + File.separator + command_file_full;
    }
    
    // Save the full path to the command file so it can be processed when the GUI initializes.
    // TODO SAM 2007-09-09 Evaluate phasing global command file out - needs to be handled in
    // the command processor.
    Message.printStatus ( 1, routine, "Command file is \"" + command_file_full + "\"" );
    // FIXME SAM 2008-09-04 Confirm no negative effects from taking this out
    //IOUtil.setProgramCommandFile ( command_file_full );
    setCommandFile ( command_file_full );
    
    setWorkingDirUsingCommandFile ( command_file_full );
    
    command_file_full_File = new File ( command_file_full );
    if ( !command_file_full_File.exists() ) {
        String message = "Command file \"" + command_file_full + "\" does not exist.";
        Message.printWarning(1, routine, message );
        System.err.println ( message );
        if ( is_batch ) {
             // Exit because there is nothing to do...
            quitProgram ( 1 );
        }
        else {
            // In GUI mode, go ahead and start up but there will be more warnings about no file to read.
        }
    }
    
    // Indicate whether running in batch mode...
    
    IOUtil.isBatch ( is_batch );
}

/**
Set the working directory as the parent of the command file.
@param commandFileFull the full (absolute) path to the command file
*/
private static void setWorkingDirUsingCommandFile ( String commandFileFull )
{   File commandFileFull_File = new File ( commandFileFull );
    String workingDir = commandFileFull_File.getParent();
    IOUtil.setProgramWorkingDir ( workingDir );
    // Set the dialog because if the running in batch mode and interaction with the graph
    // occurs, this default for dialogs should be the home of the command file.
    JGUIUtil.setLastFileDialogDirectory( workingDir );
    // Print at level 1 because the log file is not yet initialized.
    String message = "Setting working directory to command file folder \"" + workingDir + ".\"";
    //Message.printStatus ( 1, routine, message );
    System.err.println(message);
}

}