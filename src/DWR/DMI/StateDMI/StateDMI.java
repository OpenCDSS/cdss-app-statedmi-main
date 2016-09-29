//------------------------------------------------------------------------------
// StateDMI -  Main program class which is responsible for starting either an
//		applet or stand alone GUIs.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 10 Sep 2002	J. Thomas Sapienza, RTi	Initial version from older class.
// 11 Sep 2002	JTS, RTi		Convert AWT to Swing.
// 03 Oct 2002 	JTS, RTi		Set up proper program name and version
//					information.  Message logging works.
// 2002-10-04	JTS, RTi		DBHost information pulled out of command
//					line for the login JDialog.
// 2002-10-14	Steven A. Malers, RTi	Review code for initial release.
//					Specifically:
//					* Open log file with user name.
//					* Default debug to off and set warning
//					  and status levels lower.
// 2002-11-14	SAM, RTi		Update to version 01.02.00.  See
//					StateDMIMainGUI for details.
// 2003-04-22	SAM, RTi		Update to version 01.03.00.  See
//					StateDMIMainGUI for details.  This
//					version now will be used for StateCU
//					files or StateMod files, but not both.
//					* Remove batch log file from here.
//					* Check for a command line argument
//					  -statecu or -statemod to indicate how
//					  the interface should appear.
//					* Remove call to HBParse() since it is
//					  old-style.
// 2003-06-04	SAM, RTi		Update to version 01.04.00.
//					* Use the new StateCU package where
//					  classes are named StateCU_XXX.
//					* Use the new TS package (uses DateTime,
//					  etc.).
//					* Change the name of the GUI class to
//					  StateDMI_JFrame.
//					* Change to use JApplet instead of
//					  Applet.
// 2003-08-10	SAM, RTi		* Split initialize() into initialize1()
//					  and initialize2() to handle setup
//					  before and after arguments are
//					  parsed.  Previously the home directory
//					  was not being detected correctly.
//					* Enable -h and -v command line options.
// 2004-01-13	SAM, RTi		Update to version 01.05.00 Beta to
//					prepare for a beta release to the State.
// 2004-03-02	SAM, RTi		Update to version 01.06.00 Beta to
//					prepare for a beta release to the State.
// 2004-03-03	SAM, RTi		Update to version 01.07.00 Beta to
//					prepare for a beta release to the State.
// 2004-04-01	SAM, RTi		Update to version 01.08.00 Beta to
//					prepare hopefully the final test version
//					* Add a couple more commands for StateCU
// 2004-04-08	SAM, RTi		Update to version 01.09.00 to
//					release to the State.
// 2004-04-10	SAM, RTi		Update to version 01.10.00.
//					* Enable setCropPatternTSFromList() for
//					  Rio Grande work.
//					* Begin enabling StateMod commands.
// 2004-05-28	SAM, RTi		Update to version 01.11.00.
//					* Add fillCropPatternTSConstant().
// 2004-06-01	SAM, RTi		Update to version 01.12.00.
//					* Start cranking through StateMod
//					  commands.
// 2004-07-12	SAM, RTi		Update to version 01.13.00.
//					* Release recent StateCU changes to
//					  Erin Wilson and include some initial
//					  StateMod commands.
// 2004-08-22	SAM, RTi		Update to version 01.14.00.
//					* Release additional StateMod features.
// 2004-09-16	SAM, RTi		Update to version 01.15.00.
//					* Previous release had all but wells so
//					  enable wells.
// 2004-09-30	SAM, RTi		Update to version 01.15.01.
//					* Version sent to Leondard Rice for
//					  Rio Grande testing.
// 2004-10-01	SAM, RTi		Update to version 1.15.02.
//					* Official version for full
//					  functionality and documentation.
//					* Add -release option to test image
//					  file packaging in the Jar file.
// 2004-10-07	SAM, RTi		Update to version 1.15.03.
//					* Add type 6 for well station
//					  DemandType.
// 2004-10-11	SAM, RTi		Update to version 1.15.04.
//					* Follow up to previous item.  Cleaned
//					  up some messages in HydroBaseDMI.
// 2004-10-21	SAM, RTi		Update to version 1.15.05.
//					* Minor bug fix with crop pattern TS.
// 2004-11-01	SAM, RTi		Update to version 1.16.00.
//					* Add readIrrigationPracticeWellData
//					  FromHydroBase().
//					* Minor fixes in reservoir station and
//					  account dialog based on feedback from
//					  Rick Parsons.
// 2004-12-17	SAM, RTi		Update to version 1.16.01.
//					* Fix bug where diversion station ID
//					  that looks like a WDID but is not in
//					  HydroBase was causing null exceptions.
// 2005-01-12	SAM, RTi		Update to version 1.16.02.
//					* Fix bug where
//					  setDiversionStationDelayTablesFromRTN
//					  command name was not being set
//					  properly by the editor.
//					* Fix bug where name in list file was
//					  not getting set for collections.
// 2005-01-13	SAM, RTi		Update to version 1.16.03.
//					* Change default for file writes to
//					  OverwriteFile (instead of UpdateFile).
//					* Fix bug where setting output year type
//					  was not having an effect.
//					* Add setDiversionHistoricalTSMonthly
//					  Constant().
// 2005-01-18	SAM, RTi		Update to version 1.17.00.
//					* Implement output component displays.
//					* Add sortDiversionHistoricalTSMonthly()
//					  command.
//					* Rework the default sizing of display
//					  panels.
// 2005-01-26	SAM, RTi		Update to version 1.17.01.
//					* Resolve issue filling diversion
//					  historical time series with a
//					  constant.
// 2005-01-27	SAM, RTi		Update to version 1.17.02.
//					* Rework readSprinklerParcelsFromList()
//					  to only read the parcel identifiers
//					  from the list.
// 2005-01-31	SAM, RTi		Update to version 1.17.03.
//					* The above was not finished.  Continue
//					  here as time allows.
//					* Instream flow monthly needed
//					  setOutputYearType(), not
//					  setOutputPeriod().
//					* Fix bug where some reservoir station
//					  data does not seem to be coming out
//					  of HydroBase.
// 2005-02-01	SAM, RTi		Update to version 1.17.04.
//					* Do not automatically manipulate dead
//					  storage when writing StateMod
//					  reservoir account information.
// 2005-02-02	SAM, RTi		Update to version 1.17.05.
//					* Resolve issue where limiting
//					  diversions to rights is introducing
//					  to many zeros.
// 2005-02-07	SAM, RTi		Update to version 1.17.06.
//					* Fix problem limiting diversions to
//					  rights (ignores not being handled).
// 2005-02-09	SAM, RTi		Update to version 1.17.07.
//					* Add limitDiversionDemandTSMonthly
//					  ToRights().
// 2005-02-10	SAM, RTi		Update to version 1.17.08.
//					* Provide more options for limiting
//					  demands to rights.
//					* Handle the rights switch when limiting
//					  diversions and demands to rights.
// 2005-02-14	SAM, RTi		Update to version 1.17.09.
//					* Change default sort order for stream
//					  gage stations to be the network.
// 2005-02-24	SAM, RTi		Update to version 1.17.10.
//					* Try to finish implementing IPY
//					  commands for use on the Rio Grande.
// 2005-03-11	SAM, RTi		* Turn on message levels in the output,
//					  to evaluate better user feedback.
// 2005-03-21	SAM, RTi		Update to version 1.17.11.
//					* Get the IPY working.
// 2005-03-25	SAM, RTi		Update to version 1.17.12.
//					* Add warnings for obsolete commands.
//					* Change default console output to zero
//					  for warnings and status messages.
// 2005-04-05	SAM, RTi		Update to version 1.17.13.
//					* Finalize results displays for ISF,
//					  Reservoir, Well, and network data.
// 2005-05-02	SAM, RTi		* Change the log file warning level to
//					  3.
// 2005-06-01	SAM, RTi		Change the version to 1.17.13 BETA for
//					the initial release with stored
//					procedures.
// 2005-06-03	SAM, RTi		Change the version to 1.17.14 BETA.
//					* Change writeCropPatternTSToStateCU()
//					  to allow writing only the total acres,
//					  to facilitate checks.
// 2005-06-08	J. Thomas Sapienza, RTi	Added call to setApplicationHomeDir().
// 2005-06-28	SAM, RTi		Change to version 1.17.15.
// 2005-07-08	SAM, RTi		Change to version 1.17.16.
// 2005-07-13	SAM, RTi		Change to version 1.17.17.
// 2005-07-27	SAM, RTi		Change to version 1.17.18.
// 2005-08-11	SAM, RTi		Change to version 1.17.19.
// 2005-08-18	SAM, RTi		Change to version 1.17.20.
// 2005-10-03	SAM, RTi		Update to version 1.18.00.
//					* Begin finalizing list commands.
//					* Add commands for well pumping time
//					  series.
// 2005-10-10	SAM, RTi		Update to version 1.18.01.
//					* Continue finalizing list commands.
//					* Do a sweep and convert command dialog
//					  command JTextFields to JTextAreas.
// 2005-10-12	SAM, RTi		Update to version 1.18.02.
//					* For well stations default the
//					  diversion station ID to "N/A".
// 2005-10-13	SAM, RTi		Update to version 1.18.03.
//					* Implement a few changes to support the
//					  separate commands files like Ray
//					  Bennett requested.
// 2005-10-18	SAM, RTi		Update to version 1.18.04.
//					* Additional minor changes for well
//					  processing based on Ray Bennett
//					  feedback.
// 2005-11-02	JTS, RTi		Update to version 1.18.05:
//					* Changed the path string for loading
//					  the application's Icon.
// 2005-11-14	SAM, RTi		* Implement
//					  setDiversionStationsFromList() and
//					  setWellStationsFromList() commands.
// 2005-11-21	SAM, RTi		Update to version 1.18.06:
// 		JTS, RTi		* Reworked Icon-loading code to be more
//					  verbose about the process so that 
//					  debugging it is easier.  
//					* Add readDiversionDemandTSMonthlyFrom
//					  StateMod().
// 2005-11-22	SAM, RTi		Update to version 1.18.07:
//					* Add AWC to readCULocationsFromList().
// 2005-12-06	SAM, RTi		Update to version 1.18.08:
//					* Add
//					  fillDiversionStationsFromNetwork(),
//					  fillInstreamFlowStationsFromNetwork(),
//					  fillReservoirStationsFromNetwork(),
//					  fillWellStationsFromNetwork().
// 2005-12-23	SAM, RTi		Update to version 1.18.09:
//					* Some fixes in the network editor were
//					  implemented.
// 2006-01-30	SAM, RTi		Update to version 1.18.10:
//					* Update
//					  readWellDemandTSMonthlyFromStateMod()
//					  to include IgnoreWells and IgnoreDWs
//					  parameters.
//					* Set the startup status message level
//					  for the terminal to 0.
// 2006-01-31	SAM, RTi		Update to version 1.19.00:
// 2006-04-10	SAM, RTi		Update to version 1.20.00:
//					* Respond to feedback from Ray Bennett.
//					  Mainly revise messaging to make
//					  processing of wells easier.
// 2006-04-24	SAM, RTi		Update to version 1.20.01:
//					* Continue troubleshooting well data
//					  processing.
// 2006-04-30	SAM, RTi		Update to version 1.20.02:
//					* Always create the check file.  If no
//					  check messages are available, print a
//					  general message.
//					* Change well rights to use receipt for
//					  their identifiers.
//					* Add translateBlaneyCriddle().
//					* Add translageCropCharacteristics()
// 2006-06-12	SAM, RTi		Update to version 1.20.03:
//					* Add read*FromList() to all menus that
//					  through oversight did not have them
//					  added.
//					* Fix problem where "see check file"
//					  dialog was being shown before editing
//					  commands, when processing the working
//					  directory.
// 2006-06-13	SAM, RTi		Update to version 1.20.04:
//					* When writing list files, make sure to
//					  add to the list of output files.
// 2006-06-27	SAM, RTi		Update to version 1.20.05:
//					* Several sort commands were not
//					  properly hooked into menus - command
//					  dialogs were not showing.
// 2006-07-07	SAM, RTi		* In synchronizeIrrigationPracticeAnd
//					  CropPatternTS(), remove the code that
//					  resets groundwater acreage to
//					  sprinkler acreage if groundwater is
//					  less - it is unneeded.
// 2006-10-09	SAM, RTi		Update to version 1.21.00:
//					* Adjust reading well rights to reread
//					  from the database rather than relying
//					  on the "wells" table and add the APEX
//					  amounts to the absolute decrees.
// 2006-10-24	SAM, RTi		Update to version 1.22.00:
//					* Change readWellRightsFromHydroBase
//					  IDFormat dialog note and fix to make
//					  sure IDs are being formatted for
//					  previous release.
// 2006-11-03	SAM, RTi		Update to version 2.00.00:
//					* First release using the NSIS
//					  installer.
//					* First release using the new
//					  development environment.
// 2006-11-08	KAT, RTi		Changed how the -home argument
//					  is used so that relative paths work.
// 2007-01-04	SAM, RTi		Update version to 2.01.00.
//					* Add command to set river network node.
//					* Update for new StateCU file crop name length.
//					* Clean up this class based on Eclipse warnings.
// 2007-03-04	SAM, RTi		Change version date to 2007-03-02 to be
//					consistent with other CDSS software.
// 2007-03-23	SAM, RTi		Update to version 2.03.00 to include
//					some StateCU file format cleanup and IPY fixes.
// 2007-03-27	SAM, RTi		Update to version 2.04.00 to include
//					updates to help with Rio Grande 2002 parcel data.
// 2007-04-16	SAM, RTi		Update to version 2.05.00 to include
//					more updates for the Rio Grande.
// 2007-04-22	SAM, RTi		Update to version 2.06.00 to include more
//					updates for the Rio Grande.
// 2007-05-01	SAM, RTi		Update to version 2.07.00 to fix problem
//					writing *wer and editing readWellRightsFromHydroBase().
// 2007-05-11	SAM, RTi		Update to version 2.08.00 to enable multiple
//					snapshots of irrigated lands in the CDS file.
//------------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JApplet;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataDimension;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The StateDMI class is the entry point into the StateDMI application.  The
program can run either as an applet or a stand-alone application.  The main
program performs basic initialization and then starts up the graphical user interface.
*/
public class StateDMI extends JApplet
{

public static final String PROGRAM_NAME = "StateDMI";
public static final String PROGRAM_VERSION = "4.03.00beta (2016-09-28)";

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
Main GUI instance.
*/
private static StateDMI_JFrame __stateDMI_JFrame;

/**
Home directory for CDSS install (e.g., "\CDSS").
*/
private static String __home = null;

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
Return the JFrame instance for the main application.
@return the JFrame instance for use with low-level code that needs to pop up dialogs, etc.
*/
public static StateDMI_JFrame getJFrame ()
{	return __stateDMI_JFrame;
}

/**
Instantiates the application instance from an applet.
*/
public void init()
{
	IOUtil.setApplet ( this );
	IOUtil.setProgramData ( PROGRAM_NAME, PROGRAM_VERSION, null );
	initialize1();
    try {
      	parseArgs( this );
	}
	catch ( Exception e ) {
        Message.printWarning( 1, "StateDMI", "Error parsing command line arguments.  Using default behavior." );
        Message.printWarning( 1, "StateDMI", e );
    }

    // Instantiate main GUI

	initialize2();
	setIcon();
	__stateDMI_JFrame = new StateDMI_JFrame ( __app_type );
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

	if ( !IOUtil.isApplet() && (__home == null) ) {
		__home = "\\CDSS";
		Message.printWarning ( 1, routine,
		"-home was not specified on the command line.  Assuming \"" + __home + "\"" );
	}
	
	// Set up the units conversion data.  For now read from the DATAUNIT
	// file but in the future may read from HydroBase...

	String units_file = __home + File.separator + "system" + File.separator + "DATAUNIT";

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
			parseArgs ( args );
		}
		catch ( Exception e ) {
	        Message.printWarning ( 1, routine, "Error parsing command line arguments.  Using default behavior." );
	        Message.printWarning( 1, "StateDMI", e );
	    }
	
		initialize2();
	
		// Open the log file...
	
		String __logfile = "";
		String user = IOUtil.getProgramUser();
	
		if ( (__home == null) || (__home.length() == 0) || (__home.charAt(0) == '.')) {
			Message.printWarning ( 2, routine, "Home directory is not defined.  Not opening log file.");
		}
		else {
			if ( (user == null) || user.trim().equals("")) {
				__logfile = __home + File.separator + "logs" + File.separator + "StateDMI.log";
			}
			else {
				__logfile = __home + File.separator + "logs" + File.separator + "StateDMI_" + user + ".log";
			}
			Message.printStatus ( 1, routine, "Log file name: " + __logfile );
			try {
				Message.openLogFile ( __logfile );
			}
			catch (Exception e) {
				Message.printWarning ( 1, routine, "Error opening log file \"" + __logfile + "\"");
			}
		}
	
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
				__stateDMI_JFrame = new StateDMI_JFrame ( __app_type );
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
	        List<HydroBaseDMI> hbdmi_Vector = new Vector(1);
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
Parse command line arguments.
@param args Command line arguments.
*/
public static void parseArgs ( String[] args )
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
			//__home = args[i];
			__home = (new File(args[i])).getCanonicalPath().toString();
			
			Message.printStatus ( 1, routine, "Home directory for StateDMI is \"" + __home + "\"" );
			IOUtil.setProgramWorkingDir(__home);
			IOUtil.setApplicationHomeDir(__home);
			JGUIUtil.setLastFileDialogDirectory(__home);
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
		else if (args[i].equalsIgnoreCase("-v")) {
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
Parse the command-line arguments for the applet, determined from the applet data.
@param a JApplet for this application.
*/
public static void parseArgs ( JApplet a )
throws Exception
{	String home = a.getParameter("-home");
	String test = a.getParameter("-test");
	String statecu = a.getParameter("-statecu");
	String statemod = a.getParameter("-statemod");

	if ( home != null ) {
		//__home = home;
		__home = (new File(home)).getCanonicalPath().toString();
		
		IOUtil.setProgramWorkingDir(__home);
		JGUIUtil.setLastFileDialogDirectory(__home);
		IOUtil.setApplicationHomeDir(__home);
	}

	if ( statecu != null ) {
		__app_type = APP_TYPE_STATECU;
	}

	if ( statemod != null ) {
		__app_type = APP_TYPE_STATEMOD;
	}

	if ( test != null ) {
		IOUtil.testing(true);
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
	//System.out.println ( usage );
	Message.printStatus ( 1, routine, usage );
	quitProgram(0);
}

/**
Print the program version and exit the program.
*/
public static void printVersion ( )
{	String nl = System.getProperty ( "line.separator" );
	System.out.println (  nl + PROGRAM_NAME + " version: " + PROGRAM_VERSION + nl + nl );
	quitProgram (0);
}

/**
Clean up and quit the program.
@param status Exit status.
*/
static void quitProgram ( int status )
{	String	routine="StateDMI.quitProgram";

	Message.printStatus ( 1, routine, "Exiting with status " + status + "." );
	System.out.print( "STOP " + status + "\n" );
	Message.closeLogFile();
	System.exit ( status ); 
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
        System.out.println ( message );
                
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
        System.out.println ( message );
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
    System.out.println(message);
}

}