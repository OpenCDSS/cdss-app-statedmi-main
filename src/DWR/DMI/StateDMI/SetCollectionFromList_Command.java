// SetCollectionFromList_Command - This class initializes, checks, and runs the
// Set*Aggregate/SystemFromList() commands (set collection information).

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

import javax.swing.JFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Location_CollectionPartType;
import DWR.StateCU.StateCU_Location_CollectionType;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Diversion_CollectionType;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_Reservoir_CollectionType;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_Well_CollectionPartIdType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import DWR.StateMod.StateMod_Well_CollectionType;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the Set*Aggregate/SystemFromList() commands (set
collection information).
It is an abstract base class that must be controlled via a derived class.  For example, the
SetDiversionAggregateFromList() command extends this class in order to uniquely represent the command,
but much of the functionality is in this base class.
The collection information is applied to StateCU_Location, StateMod_Diversion, StateMod_Reservoir,
and StateMod_Well, with specific enumerations for types handled for each.
*/
public abstract class SetCollectionFromList_Command extends AbstractCommand implements CommandDiscoverable, ObjectListProvider
{
	
/**
Possible values for the PartType parameter.
*/
protected final String _Ditch = "Ditch";
protected final String _Parcel = "Parcel";
//protected final String _Well = "Well"; // Conflict below - editor uses StateMod_Well.COLLECTION_PART_TYPE_WELL

/**
Possible values for node type, used by the editor and internally for messages.
*/
protected final String _Well = "Well";
protected final String _Diversion = "Diversion";
protected final String _Reservoir = "Reservoir";

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for PartsListedHow parameter.
*/
protected final String _InRow = "InRow";
protected final String _InColumn = "InColumn";

/**
The table that is created.
*/
private DataTable __table = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetCollectionFromList_Command ()
{	super();
	setCommandName ( "Set?Collection?" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String routine = getClass().getName() + ".checkCommandParameters";
	String ListFile = parameters.getValue ( "ListFile" );
	String Year = parameters.getValue ( "Year" );
	String Div = parameters.getValue ( "Div" );
	String PartType = parameters.getValue ( "PartType" );
	String IDCol = parameters.getValue ( "IDCol" );
	String PartIDsCol = parameters.getValue ( "PartIDsCol" );
	String PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
	String PartsListedHow = parameters.getValue ( "PartsListedHow" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    if ( (ListFile == null) || (ListFile.length() == 0) ) {
        message = "The list file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify an existing list file." ) );
    }
    else {
    	String working_dir = null;
        try {
        	Object o = processor.getPropContents ( "WorkingDir" );
            // Working directory is available so use it...
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            Message.printWarning(3, routine, message );
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify an existing input file." ) );
        }
    
        try {
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, ListFile));
			File f = new File ( adjustedPath );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The list file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the input directory." ) );
			}
			else if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The list file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing list file (may be OK if created during processing)." ) );
			}
			f = null;
			f2 = null;
        }
        catch ( Exception e ) {
            message = "The list file:\n" +
            "    \"" + ListFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that list file and working directory paths are compatible." ) );
        }
    }
    
	if ( (StringUtil.indexOfIgnoreCase(getCommandName(),_Well,0) >= 0) ) {
		// Node type is well
		if ( (PartType == null) || PartType.equals("") ) {
			message = "The part type must specified with well data.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the part type as " + _Ditch + ", " + _Parcel + ", or " + _Well + ".") );	
		}
		else if ( (PartType != null) && PartType.equalsIgnoreCase(_Parcel) ) {
			if ( (Year == null) || (Year.length() == 0) ) {
				message = "The year must be specified with part type of " + _Parcel;
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			else if ( !StringUtil.isInteger(Year)) {
				message = "The year (" + Year + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			if ( (Div == null) || (Div.length() == 0) ) {
				message = "The division must be specified with part type of " + _Parcel;
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			else if ( !StringUtil.isInteger(Div)) {
				message = "The division (" + Div + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the division as an integer.") );
			}
		}
		else if ( (PartType != null) && PartType.equalsIgnoreCase(_Well) ) {
			// Part ID type column must be specified
			if ( (PartIDTypeColumn == null) || PartIDTypeColumn.isEmpty() ) {
				message = "The well aggregate part ID type column must be specified";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the part ID type column number or name.") );
			}
		}
	}
	else if ( (PartType != null) && !PartType.equals("") ) {
		// DO NOT specify the year or division
		if ( (Year != null) && (Year.length() > 0) ) {
			message = "The year should only be specified with part type of " + _Parcel;
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify the year with part type " + PartType) );
		}
		if ( (Div != null) && (Div.length() > 0) ) {
			message = "The division should only be specified with part type of " + _Parcel;
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify the division with part type " + PartType) );
		}
	}
	
	// TODO SAM 2009-03-23 Need to check the part types more specifically; however, this is difficult
	// because this command may be applied to StateCU locations or StateMod station types.
	if ( (PartType != null) && (PartType.length() != 0) && !PartType.equalsIgnoreCase(_Ditch) &&
			!PartType.equalsIgnoreCase(_Parcel) && !PartType.equalsIgnoreCase(_Well)) {
		message = "The part type (" + PartType + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the part type as " + _Ditch + ", " + _Parcel + ", or " + _Well + ".") );
	}
	
	if ( (IDCol == null) ||	IDCol.isEmpty() ) {
        message = "The ID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column name or number (1+) to read." ) );
	}
	
	if ( (PartIDsCol == null) || PartIDsCol.isEmpty() ) {
        message = "The part ID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the part ID column (1+) to read." ) );
	}
	
	if ( (PartsListedHow == null) || (PartsListedHow.length() == 0) ) {
		message = "The PartsListedHow parameter must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the PartsListedHow parameter as " + _InRow + " or " + _InColumn + ".") );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(12);
	validList.add ( "ListFile" );
	validList.add ( "TableID" );
	validList.add ( "Year" );
	validList.add ( "Div" );
	validList.add ( "PartType" );
	validList.add ( "IDCol" );
	validList.add ( "NameCol" );
	validList.add ( "PartIDsCol" );
	validList.add ( "PartIDTypeColumn" );
	validList.add ( "PartsListedHow" );
	validList.add ( "PartIDsColMax" );
	validList.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new SetCollectionFromList_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
The following classes can be requested:  DataTable
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{   DataTable table = getDiscoveryTable();
    List<T> v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new ArrayList<T>();
        v.add ( (T)table );
    }
    return v;
}

// Use base class parse method

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param commandPhase Command phase being executed (RUN or DISCOVERY).
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings

    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    String nodeTypeFromCommand = null; // Diversion, Well, Reservoir
	if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Diversion,0) >= 0 ) {
		nodeTypeFromCommand = _Diversion;
	}
	else if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Well,0) >= 0 ) {
		nodeTypeFromCommand = _Well;
	}
	else if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Reservoir,0) >= 0 ) {
		nodeTypeFromCommand = _Reservoir;
	}
	
	// Use a string for collectionType because it will be converted to each station type enumeration below.
	// - the diversion collection type overlaps well and reservoir
	String collectionType = null; // Aggregate, System, MultiStruct
	if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.AGGREGATE.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.AGGREGATE.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.SYSTEM.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.SYSTEM.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.MULTISTRUCT.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.MULTISTRUCT.toString();
	}

    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
	if ( (ListFile != null) && (ListFile.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		ListFile = StateDMICommandProcessorUtil.expandParameterValue(processor, this, ListFile);
	}
    String TableID = parameters.getValue ( "TableID" );
	if ( (TableID != null) && (TableID.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		TableID = StateDMICommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
	String Delim = parameters.getValue ( "Delim" );
	if ( (Delim == null) || Delim.equals("") ) {
		Delim = ","; // Default
	}
    String Year = parameters.getValue ( "Year" );
    int Year_int = 0; // Only used by wells
    if ( (Year != null) && !Year.equals("") ) {
    	Year_int = Integer.parseInt(Year);
    }
    String Div = parameters.getValue ( "Div" );
    int Div_int = 0;
    if ( (Div != null) && !Div.equals("") ) {
    	Div_int = Integer.parseInt(Div);
    }
    String PartType = parameters.getValue ( "PartType" );
	if ( nodeTypeFromCommand.equalsIgnoreCase(_Diversion) ) {
		PartType = _Ditch;	// Default for diversions.
	}
	else if ( nodeTypeFromCommand.equalsIgnoreCase(_Reservoir) ) {
		PartType = _Reservoir;	// Default for reservoirs.
	}
	else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) ) {
		// Part type will have been set above from the parameter
	}
    String IDCol = parameters.getValue ( "IDCol" );
    String NameCol = parameters.getValue ( "NameCol" );
    String PartIDsCol = parameters.getValue ( "PartIDsCol" );
    String PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
    String PartsListedHow = parameters.getValue ( "PartsListedHow" );
    if ( (PartsListedHow == null) || PartsListedHow.equals("") ) {
    	PartsListedHow = _InRow; // Default
    }
    String PartIDsColMax = parameters.getValue ( "PartIDsColMax" );
	boolean partsInRow = true; // To simplify code below
	if ( PartsListedHow.equalsIgnoreCase("InColumn") ) {
		partsInRow = false;
	}
    String IfNotFound = parameters.getValue ( "IfNotFound" );
    if ( (IfNotFound == null) || IfNotFound.equals("") ) {
    	IfNotFound = _Warn;
    }
    
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source (e.g., DDS, and STR).
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateCULocationMatchList() );
	if ( nodeTypeFromCommand.equals("Diversion") ) {
		processor.resetDataMatches ( processor.getStateModDiversionStationMatchList() );
	}
	if ( nodeTypeFromCommand.equals("Reservoir") ) {
		processor.resetDataMatches ( processor.getStateModReservoirStationMatchList() );
	}
	if ( nodeTypeFromCommand.equals("Well") ) {
		processor.resetDataMatches ( processor.getStateModWellStationMatchList() );
	}
    
    // Get the data needed for the command
    
    List<StateCU_Location> culocList = null;
    List<StateMod_Diversion> divList = null;
    List<StateMod_Reservoir> resList = null;
    List<StateMod_Well> wellList = null;
    try {
		// Don't know for sure what is being processed so have to get StateCU and StateMod data lists
		if ( nodeTypeFromCommand.equals(_Diversion) || nodeTypeFromCommand.equals(_Well) ) {
			Object o = processor.getPropContents ( "StateCU_Location_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateCU_Location> dataList = (List<StateCU_Location>)o;
				culocList = dataList;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting CU Location data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Diversion) ) {
			Object o = processor.getPropContents ( "StateMod_DiversionStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)o;
				divList = dataList;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting diversion station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Reservoir) ) {
			Object o = processor.getPropContents ( "StateMod_ReservoirStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)o;
				resList = dataList;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting reservoir station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Well) ) {
			Object o = processor.getPropContents ( "StateMod_WellStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Well> dataList = (List<StateMod_Well>)o;
				wellList = dataList;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting well station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Try to warn in case where SetDiversion*FromList() is being used with well stations.
    // If no CU Locations and no well stations are found, then assume that the command is being used in error.
    if ( commandPhase == CommandPhaseType.RUN ) {
    	if ( nodeTypeFromCommand.equals(_Diversion) &&
    		((culocList == null) || (culocList.size() == 0)) && ((divList == null) || (divList.size() == 0))) {
        	message = "The " + getCommandName() + "() command is being used but no diversion stations have been read.";
        	Message.printWarning ( warning_level, 
        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        	status.addToLog ( commandPhase,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Use a SetWell" + collectionType +
                	"FromList() command instead if D&W nodes are being processed, " +
                	"or make sure to read diversion stations before this command." ) );
    	}
    }
    
    String ListFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
	if ( !IOUtil.fileExists(ListFile_full) ) {
        message = "File \"" + ListFile_full + "\" does not exist.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the list file name is correct." ) );
	}

	// Get the HydroBase DMI...
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }
    
    // If here, OK to continue processing the command

    try {
    	// If specified, try reading the aggregation information into a table.
    	if ( commandPhase == CommandPhaseType.RUN ) {
    	// DO NOT indent, in order to allow comparison with previous code versions
     	// Internal column numbers are zero-referenced, parameters are 1-referenced.  The parameters
     	// were adjusted to zero index above

    	// First read the table using a table...

    	PropList props = new PropList ("");
    	props.set ( "Delimiter=," ); // see existing prototype
    	props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
    	props.set ( "TrimStrings=True" ); // If true, trim strings after reading.
    	DataTable table = DataTable.parseFile ( ListFile_full, props );
    	if ( (TableID != null) && !TableID.isEmpty() ) {
            table.setTableID ( TableID );
    	}

    	int tsize = 0;
    	if ( table != null ) {
    		tsize = table.getNumberOfRecords();
    	}
    	Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
    		table.getNumberOfFields() + " fields" );
    	
    	// Determine the column numbers from parameters
        int indexErrors = 0;
        int IDCol_int = -1;
        if ( (IDCol != null) && !IDCol.isEmpty() ) {
        	if ( StringUtil.isInteger(IDCol) ) {
        		// Assume that integer is a column number
        		IDCol_int = Integer.parseInt(IDCol) - 1; // zero reference
        	}
        	else {
        		// Assume that a column name is specified
	        	IDCol_int = table.getFieldIndex(IDCol);
        	}
	        if ( IDCol_int < 0 ) {
		       	message = "Table with TableID=\"" + TableID + "\" does not contain IDCol Column=\"" +
		       		IDCol + "\". Can't process input.";
	            Message.printWarning(warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column exists in the table." ) );
	            ++indexErrors;
        	}
        }
        int NameCol_int = -1;
        if ( (NameCol != null) && !NameCol.isEmpty() ) {
        	if ( StringUtil.isInteger(NameCol) ) {
        		// Assume that integer is a column number
        		NameCol_int = Integer.parseInt(NameCol) - 1; // zero reference
        	}
        	else {
        		// Assume that a column name is specified
	        	NameCol_int = table.getFieldIndex(NameCol);
        	}
	        if ( NameCol_int < 0 ) {
		       	message = "Table with TableID=\"" + TableID + "\" does not contain NameCol Column=\"" +
		       		NameCol + "\". Can't process input.";
	            Message.printWarning(warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column exists in the table." ) );
	            ++indexErrors;
        	}
        }
        int PartIDsCol_int = -1;
        if ( (PartIDsCol != null) && !PartIDsCol.isEmpty() ) {
        	if ( StringUtil.isInteger(PartIDsCol) ) {
        		// Assume that integer is a column number
        		PartIDsCol_int = Integer.parseInt(PartIDsCol) - 1; // zero reference
        	}
        	else {
        		// Assume that a column name is specified
	        	PartIDsCol_int = table.getFieldIndex(PartIDsCol);
        	}
	        if ( PartIDsCol_int < 0 ) {
		       	message = "Table with TableID=\"" + TableID + "\" does not contain PartIDsCol Column=\"" +
		       		PartIDsCol + "\". Can't process input.";
	            Message.printWarning(warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column exists in the table." ) );
	            ++indexErrors;
        	}
        }
        int PartIDTypeColumn_int = -1;
        if ( (PartIDTypeColumn != null) && !PartIDTypeColumn.isEmpty() ) {
        	if ( StringUtil.isInteger(PartIDTypeColumn) ) {
        		// Assume that integer is a column number
        		PartIDTypeColumn_int = Integer.parseInt(PartIDTypeColumn) - 1; // zero reference
        	}
        	else {
        		// Assume that a column name is specified
	        	PartIDTypeColumn_int = table.getFieldIndex(PartIDTypeColumn);
        	}
	        if ( PartIDTypeColumn_int < 0 ) {
		       	message = "Table with TableID=\"" + TableID + "\" does not contain PartIDTypeColumn Column=\"" +
		       		PartIDTypeColumn + "\". Can't process input.";
	            Message.printWarning(warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column exists in the table." ) );
	            ++indexErrors;
        	}
        }
        int PartIDsColMax_int = -1;
        if ( (PartIDsColMax != null) && !PartIDsColMax.isEmpty() ) {
        	if ( StringUtil.isInteger(PartIDsColMax) ) {
        		// Assume that integer is a column number
        		PartIDsColMax_int = Integer.parseInt(PartIDsColMax) - 1; // zero reference
        	}
        	else {
        		// Assume that a column name is specified
	        	PartIDsColMax_int = table.getFieldIndex(PartIDsColMax);
        	}
	        if ( PartIDsColMax_int < 0 ) {
		       	message = "Table with TableID=\"" + TableID + "\" does not contain PartIDsColMax Column=\"" +
		       		PartIDsColMax + "\". Can't process input.";
	            Message.printWarning(warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column exists in the table." ) );
	            ++indexErrors;
        	}
        }
    	
        if ( indexErrors != 0 ) {
            // There were errors determining column 	
	       	message = "There were errors determining table column numbers.  Cannot continue.";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the column(s) from previous warnings exist in the table." ) );
        }
        else {
    	// Allocate an array for all locations in the list file to indicate when an
    	// aggregate/system was not matched...
    	
    	List<String> listfile_ids = new ArrayList<String>();  // Unique list of IDs from the list file
    	TableRecord rec = null;
    	String id;
    	boolean found;
    	for (int j = 0; j < tsize; j++) {
    		rec = table.getRecord(j);
    		id = (String)rec.getFieldValue(IDCol_int);
    		if ( id.equals("") ) {
    			// Blank line in file so skip the ID
    			continue;
    		}
    		found = false;
    		for ( int iv = 0; iv < listfile_ids.size(); iv++ ) {
    			if ( id.equalsIgnoreCase(listfile_ids.get(iv))) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			// Not found in the vector so add it...
    			listfile_ids.add ( id );
    		}
    	}
    	boolean [] listrecord_matched = new boolean[listfile_ids.size()];
    	for ( int i = 0; i < listrecord_matched.length; i++ ) {
    		Message.printStatus( 2, routine, "List file ID = \"" + listfile_ids.get(i) + "\"");
    		listrecord_matched[i] = false;
    	}

    	// Default to the maximum available...
    	if ( PartsListedHow.equalsIgnoreCase(_InRow) ) {
    		if ( PartIDsColMax == null ) {
    			// Default to use the size of the table...
    			PartIDsColMax_int = table.getNumberOfFields() - 1;
    		}
    	}
    	
        if ( (PartIDTypeColumn != null) && !PartIDTypeColumn.isEmpty() ) {
        	// Get the column number - first try number
        	try {
        		PartIDTypeColumn_int = Integer.parseInt(PartIDTypeColumn);
        		PartIDTypeColumn_int = PartIDTypeColumn_int - 1; // 0-index
        	}
        	catch ( NumberFormatException e ) {
        		// Was specified as column name, not integer
        		// Try getting the column name from the table
        		try {
        			PartIDTypeColumn_int = table.getFieldIndex(PartIDTypeColumn);
        		}
        		catch ( Exception e2 ) {
                    message = "PartIDTypeColumn \"" + PartIDTypeColumn + "\" column is not found in the list file.";
                    Message.printWarning ( warning_level, 
	                    MessageUtil.formatMessageTag(command_tag, ++warning_count),
	                    routine, message );
                    status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that column \"" + PartIDTypeColumn + "\" exists in the list file." ) );  		
            		throw new CommandException ( message );
        		}
        	}
        }

    	// Make sure that specified columns are within the table...

    	if ( (IDCol_int + 1) > table.getNumberOfFields() ) {
            message = "IDCol \"" + IDCol + "\" is > than the number of columns in the file: " +
				table.getNumberOfFields();
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a column <= the number of columns in the file." ) );  		
    		throw new CommandException ( message );
    	}
    	if ( (NameCol_int + 1) > table.getNumberOfFields() ) {
            message = "NameCol \"" + NameCol + "\" is > than the number of columns in the file: " +
			table.getNumberOfFields();
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify a column <= the number of columns in the file." ) );  		
			throw new CommandException ( message );
    	}
    	if ( (PartIDsCol_int + 1) > table.getNumberOfFields() ) {
            message = "PartIDsCol \"" + PartIDsCol + "\" is > than the number of columns in the file: " +
			table.getNumberOfFields();
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify a column <= the number of columns in the file." ) );  		
			throw new CommandException ( message );
    	}
    	if ( (PartIDTypeColumn_int + 1) > table.getNumberOfFields() ) {
            message = "PartIDTypeColumn \"" + PartIDTypeColumn + "\" is > than the number of columns in the file: " +
			table.getNumberOfFields();
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify a column <= the number of columns in the file." ) );  		
			throw new CommandException ( message );
    	}
    	if ( (PartIDsColMax_int + 1) > table.getNumberOfFields() ) {
            message = "PartIDsColMax \"" + PartIDsColMax + "\" is > than the number of columns in the file: " +
			table.getNumberOfFields();
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify a column <= the number of columns in the file." ) );  		
			throw new CommandException ( message );
    	}

    	// Do this the brute force way because there are not that many records.

    	// Locations may be for StateCU or StateMod data...

    	// Loop through the CU locations that are in memory...  For each one,
    	// search the table and set collection information to the location

    	StateCU_Location culoc;
    	int size = 0;
    	if ( culocList != null ) {
    		size = culocList.size();
    	}
    	String culoc_id;
    	String name; // Name to assign if specified in the list file.
    	String partId;
    	String partIdType;
    	int matchCount = 0; // TODO SAM 2016-05-17 not sure how this is supposed to be used
    	if ( matchCount < 0 ) {
    		// TODO put in to prevent compiler warning about not being used
    	}
    	List<String> partIds = new ArrayList<String>();
    	List<String> partIdTypes = new ArrayList<String>();
    	boolean foundMatch = false;
    	for (int i = 0; i < size; i++) {
    		matchCount = 0; // Number of matching CU Location ID
    		culoc = culocList.get(i);
    		culoc_id = culoc.getID();
    		name = null; // If set below in the list file it will be used
    		// Now loop through the table and see if there are any matches for the ID.
    		foundMatch = false;
    		if ( partsInRow ) {
    			for (int j = 0; j < tsize; j++) {
    				rec = table.getRecord(j);
    				id = (String)rec.getFieldValue(IDCol_int);
    				if ( !id.equalsIgnoreCase(culoc_id) ) {
    					// No match...
    					continue;
    				}
    				// Else have a match.  Need to set the data.  Do not reuse the list!
    				foundMatch = true;
    				partIds = new ArrayList<>();
    				partIdTypes = new ArrayList<>();
    				// The part IDs are in the same row starting at the indicated column and
    				// continue until the part IDs are blank.
    				// - this is ONLY used with WDIDs (not RECEIPT) so set the part ID type for output later
    				for ( int ic = PartIDsCol_int; ic <= PartIDsColMax_int ;ic++ ){
    					partId = (String)rec.getFieldValue(ic);
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    						partIdTypes.add ( "WDID" );
    					}
    				}
    				if ( NameCol_int >= 0 ) {
    					name = (String)rec.getFieldValue(NameCol_int);
    				}
    				break; // First match will be used if duplicates.
    			}
    		}
    		else {
    			// The part IDs are in the multiple rows using the indicated column.
    			// Read data until there are no more items in the table...
    			partIds = new ArrayList<>();
    			partIdTypes = new ArrayList<>();
    			for ( int j = 0; j < tsize; j++ ) {
    				rec = table.getRecord(j);
    				id=(String)rec.getFieldValue(IDCol_int);
    				if ( id.equalsIgnoreCase( culoc_id) ) {
    					foundMatch = true;
    					partId = (String)rec.getFieldValue(PartIDsCol_int);
    					partId = partId.trim();
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    						if ( PartIDTypeColumn_int >= 0 ) {
    							// Also save the part id type
    							partIdType = (String)rec.getFieldValue(PartIDTypeColumn_int);
    							partIdType = partIdType.trim();
    							partIdTypes.add ( partIdType );
    						}
    						else {
    							// Assume WDID
    							// - TODO smalers 2019-07-10 could allow P: in id for receipt
    							partIdTypes.add ( "WDID" );
    						}
    					}
    					else {
    						message = "CU location " + nodeTypeFromCommand + " collection \"" + culoc_id +
    							"\" has blank part ID for part " + partIds.size() + 1;
							Message.printWarning ( warning_level, 
								MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
							status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify that the part ID is specified." ) );
    					}
    				}
    			}
    		}
    		if ( foundMatch ) {
    			++matchCount;
    			StateCU_Location_CollectionType collectionTypeForCuloc = StateCU_Location_CollectionType.valueOfIgnoreCase(collectionType);
 				if ( collectionTypeForCuloc == null ) {
 					message = "CU Location collection \"" + culoc_id + "\" type \"" + collectionType + "\" is invalid.";
 					Message.printWarning ( warning_level, 
 						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
 					status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
 						message, "Verify that the collection type is valid." ) );
 				}
    			culoc.setCollectionType ( collectionTypeForCuloc );
    			StateCU_Location_CollectionPartType collectionPartTypeForCuloc = StateCU_Location_CollectionPartType.valueOfIgnoreCase(PartType);
 				if ( collectionPartTypeForCuloc == null ) {
    				message = "CU Location collection \"" + culoc_id + "\" part type \"" + PartType + "\" is invalid.";
    				Message.printWarning ( warning_level, 
    					MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    				status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
    					message, "Verify that part type is valid." ) );
 				}
    			culoc.setCollectionPartType ( collectionPartTypeForCuloc );
    			if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Parcel) ) {
    				// Aggregation is by parcel list. This is being phased out.
    				// Need the division with wells that are collected by parcels...
    				culoc.setCollectionDiv ( Div_int );
    				culoc.setCollectionPartIDsForYear( Year_int, partIds );
    				Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand + " " +
    					collectionType + " Part (" + PartType + ") IDs " + Year_int + " -> " + partIds );
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					culoc.setName ( name );
    				}
    			}
    			else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Well) ) {
    				List<StateCU_Location_CollectionPartIdType> partIdTypesForCuloc = new ArrayList<>();
    				List<Integer> partIdWDsForCuloc = new ArrayList<Integer>(); // Used to store WD for receipt, for cached data lookups
    				String partIdTypeForLoop;
    				for ( int iPart = 0; iPart < partIdTypes.size(); iPart++ ) {
    					partIdTypeForLoop = partIdTypes.get(iPart);
    					StateCU_Location_CollectionPartIdType partIdTypeForCuloc = StateCU_Location_CollectionPartIdType.valueOfIgnoreCase(partIdTypeForLoop);
   						partIdTypesForCuloc.add(partIdTypeForCuloc); // Add in any case so ID and type lists align
   						if ( partIdTypeForCuloc == StateCU_Location_CollectionPartIdType.RECEIPT) { 
   							// Set the WD to -1, will be filled later
    						partIdWDsForCuloc.add(new Integer(-1));
   						}
   						else {
   							// Set the WD, mostly for information since used for Well part ID of receipt
    						String wd = partIds.get(iPart).substring(0,2);
    						partIdWDsForCuloc.add(new Integer(wd));
   						}
    					if ( partIdTypeForCuloc == null ) {
    						message = "CU Location collection \"" + culoc_id + "\" part ID \"" + partIds.get(iPart) +
    							"\" has invalid part ID type \"" + partIdTypeForLoop + "\".";
    						Message.printWarning ( warning_level, 
    							MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    						status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
    							message, "Verify that part ID type is valid." ) );
    					}
    				}
    				culoc.setCollectionPartIDs( partIds, partIdTypesForCuloc, partIdWDsForCuloc );
    				// Print to log confirming the collection information
    				StringBuilder b = new StringBuilder("[");
    				for ( int iPart = 0; iPart < partIds.size(); iPart++ ) {
    					if ( iPart > 0 ) {
    						b.append(", ");
    					}
    					b.append(partIdTypes.get(iPart) + " " + partIds.get(iPart));
    				}
    				b.append("]");
    				Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand + " " +
    					collectionType + " Part (" + PartType + ") IDs -> " + b );
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					culoc.setName ( name );
    				}
    			}
    			else {
    				// Ditch or reservoir expects part to be a WDID
    				List<StateCU_Location_CollectionPartIdType> partIdTypesForCuloc = new ArrayList<>();
    				List<Integer> partIdWDsForCuloc = new ArrayList<Integer>(); // Used to store WD for receipt, for cached data lookups
    				for ( int iPart = 0; iPart < partIds.size(); iPart++ ) {
   						partIdTypesForCuloc.add( StateCU_Location_CollectionPartIdType.WDID );
   						// Set the WD, mostly for information since only used in processing for Well part ID of receipt
    					String wd = partIds.get(iPart).substring(0,2);
    					partIdWDsForCuloc.add(new Integer(wd));
    				}
    				culoc.setCollectionPartIDs ( partIds, partIdTypesForCuloc, partIdWDsForCuloc );
    				// All part types are WDID.  Set to help with visualization and data checks.
    				Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand
    				+ " " + collectionType + " Part (" + PartType + ") IDs -> " + partIds );
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting CU location " + culoc_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					culoc.setName ( name );
    				}
    			}
    			// Indicate that a list file record match was found...
    			for ( int iv = 0; iv < listrecord_matched.length; iv++ ) {
    				if ( culoc_id.equalsIgnoreCase(listfile_ids.get(iv)) ) {
    					listrecord_matched[iv] = true;
    				}
    			}
    		} // End match found
    	} // End loop on CU locations

    	// Process StateMod data.

    	StateMod_Diversion div = null;
    	StateMod_Reservoir res = null;
    	StateMod_Well well = null;

    	size = 0;
    	if ( nodeTypeFromCommand.equalsIgnoreCase(_Diversion) ) {
    		if ( divList != null ) {
    			size = divList.size();
    		}
    	}
    	else if ( nodeTypeFromCommand.equalsIgnoreCase(_Reservoir) ) {
    		if ( resList != null ) {
    			size = resList.size();
    		}
    	}
    	else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) ) {
    		if ( wellList != null ) {
    			size = wellList.size();
    		}
    	}
    	String sm_id = null;
    	for ( int i = 0; i < size; i++ ) {
    		matchCount = 0; // Number of matching StateMod data ID
    		if ( nodeTypeFromCommand.equalsIgnoreCase(_Diversion) ) {
    			div = divList.get(i);
    			sm_id = div.getID();
    		}
    		else if ( nodeTypeFromCommand.equalsIgnoreCase(_Reservoir) ) {
    			res = resList.get(i);
    			sm_id = res.getID();
    		}
    		else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) ) {
    			well = wellList.get(i);
    			sm_id = well.getID();
    		}
    		// Now loop through the table and see if there are any matches for the ID...
    		name = null; // if set below it will be used
    		foundMatch = false;
    		if ( partsInRow ) {
    			for ( int j = 0; j < tsize; j++ ) {
    				rec = table.getRecord(j);
    				id = (String)rec.getFieldValue(IDCol_int);
    				if ( !id.equalsIgnoreCase(sm_id) ) {
    					// No match...
    					continue;
    				}
    				// Else have a match.  Need to set the data.  Do not reuse the Vector!
    				foundMatch = true;
    				partIds = new ArrayList<>();
    				partIdTypes = new ArrayList<>();
    				// The part IDs are in the same row starting at the indicated column and
    				// continue until the part IDs are blank...
    				for ( int ic = PartIDsCol_int; ic <= PartIDsColMax_int ;ic++ ){
    					partId = (String)rec.getFieldValue(ic);
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    					}
    				}
    				if ( NameCol_int >= 0 ) {
    					name = (String)rec.getFieldValue(NameCol_int);
    				}
    				break;	// First match will be used if duplicates
    			}
    		}
    		else {
    			// The part IDs are in the multiple rows using the indicated column.
    			// Read all the data because the well ID may not be sorted
    			partIds = new ArrayList<>();
    			partIdTypes = new ArrayList<>();
    			name = null;
    			for ( int j = 0; j < tsize; j++ ) {
    				rec = table.getRecord(j);
    				id = (String)rec.getFieldValue(IDCol_int);
    				if ( id.equalsIgnoreCase( sm_id) ) {
    					foundMatch = true;
    					partId = (String)rec.getFieldValue(PartIDsCol_int);
    					partId = partId.trim();
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    						if ( PartIDTypeColumn_int >= 0 ) {
    							partIdType = (String)rec.getFieldValue(PartIDTypeColumn_int);
    							partIdType = partIdType.trim();
    							partIdTypes.add ( partIdType );
    						}
    						else {
    							// Assume WDID
    							// - TODO smalers 2019-07-10 could allow P: in id for receipt
    							partIdTypes.add ( "WDID" );
    						}
    						// Take the first name in the desired column
        					if ( (NameCol_int >= 0) && (name == null) ) {
        						// Name has not been found so try to get it from the row
        						String name0 = (String)rec.getFieldValue(NameCol_int);
        						if ( name0.length() > 0 ) {
        							name = name0;
        						}
        					}
    					}
    					else {
    						message = "StateMod " + nodeTypeFromCommand + " collection \"" + sm_id +
    							"\" has blank part ID for part " + partIds.size() + 1;
							Message.printWarning ( warning_level, 
								MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
							status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify that the part ID is specified." ) );
    					}
    				}
    			}
    		}
    		if ( foundMatch ) {
    			++matchCount;
    			if ( nodeTypeFromCommand.equalsIgnoreCase( _Diversion) ) {
    				StateMod_Diversion_CollectionType collectionTypeForDiv = StateMod_Diversion_CollectionType.valueOfIgnoreCase(collectionType);
 					if ( collectionTypeForDiv == null ) {
 						message = "StateMod diversion collection \"" + sm_id + "\" type \"" + collectionType + "\" is invalid.";
 						Message.printWarning ( warning_level, 
 							MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
 						status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
 							message, "Verify that the collection type is valid." ) );
 					}
    				div.setCollectionType ( collectionTypeForDiv );
    				div.setCollectionPartIDs ( partIds );
    				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
    				+ " " + collectionType + " Part IDs (" + PartType + ") -> " + partIds );
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting " + sm_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					div.setName ( name );
    				}
    			}
    			else if ( nodeTypeFromCommand.equalsIgnoreCase(_Reservoir) ) {
    				StateMod_Reservoir_CollectionType collectionTypeForRes = StateMod_Reservoir_CollectionType.valueOfIgnoreCase(collectionType);
 					if ( collectionTypeForRes == null ) {
 						message = "StateMod reservoir collection \"" + sm_id + "\" type \"" + collectionType + "\" is invalid.";
 						Message.printWarning ( warning_level, 
 							MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
 						status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
 							message, "Verify that the collection type is valid." ) );
 					}
    				res.setCollectionType ( collectionTypeForRes );
    				res.setCollectionPartIDs ( partIds );
    				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
    				+ " " + collectionType + " Part IDs (" + PartType + ") -> " + partIds );
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting " + sm_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					res.setName ( name );
    				}
    			}
    			else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) ) {
    				StateMod_Well_CollectionType collectionTypeForWell = StateMod_Well_CollectionType.valueOfIgnoreCase(collectionType);
 					if ( collectionTypeForWell == null ) {
 						message = "StateMod well collection \"" + sm_id + "\" type \"" + collectionType + "\" is invalid.";
 						Message.printWarning ( warning_level, 
 							MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
 						status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
 							message, "Verify that the collection type is valid." ) );
 					}
    				well.setCollectionType ( collectionTypeForWell );
    				StateMod_Well_CollectionPartType collectionPartTypeForWell = StateMod_Well_CollectionPartType.valueOfIgnoreCase(PartType);
 					if ( collectionPartTypeForWell == null ) {
    					message = "StateMod well collection \"" + sm_id + "\" part type \"" + PartType + "\" is invalid.";
    					Message.printWarning ( warning_level, 
    						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    					status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
    						message, "Verify that part type is valid." ) );
 					}
    				well.setCollectionPartType (collectionPartTypeForWell);
    				if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Parcel) ) {
        				// Need the division with wells that are collected by parcels...
	    				well.setCollectionDiv ( Div_int );
	    				well.setCollectionPartIDsForYear( Year_int, partIds);
	    				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
	    				+ " " + collectionType + " Part IDs (" + PartType + ") " + Year_int + " -> " + partIds );
    				}
    				else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Well) ) {
        				// Set the well collection as a list of part IDs and their types - same over the full period
    					String partIdTypeForLoop;
    					List<StateMod_Well_CollectionPartIdType> partIdTypesForWell = new ArrayList<StateMod_Well_CollectionPartIdType>();
    					List<Integer> partIdWDsForWell = new ArrayList<Integer>(); // Used to store WD for receipt, for cached data lookups
    					for ( int iPart = 0; iPart < partIdTypes.size(); iPart++ ) {
    						partIdTypeForLoop = partIdTypes.get(iPart);
    						StateMod_Well_CollectionPartIdType partIdTypeForWell = StateMod_Well_CollectionPartIdType.valueOfIgnoreCase(partIdTypeForLoop);
    						partIdTypesForWell.add(partIdTypeForWell); // Add in any case so list is aligned with the ID list
    						if ( partIdTypeForWell == StateMod_Well_CollectionPartIdType.RECEIPT) { 
    							// Set the WD to -1, will be filled later
    							partIdWDsForWell.add(new Integer(-1));
   							}
   							else {
   								// Set the WD, mostly for information since used for Well part ID of receipt
    							String wd = partIds.get(iPart).substring(0,2);
    							partIdWDsForWell.add(new Integer(wd));
   							}
    						if ( partIdTypeForWell == null ) {
    							message = "StateMod well collection \"" + sm_id + "\" part ID \"" + partIds.get(iPart) +
    								"\" has invalid part ID type \"" + partIdTypeForLoop + "\".";
    							Message.printWarning ( warning_level, 
    								MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    							status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
    								message, "Verify that part ID type is valid." ) );
    						}
    					}
	    				well.setCollectionPartIDs( partIds, partIdTypesForWell, partIdWDsForWell );
	    				StringBuilder b = new StringBuilder ( "[" );
	    				for ( int iPart = 0; iPart < partIds.size(); iPart++ ) {
	    					if ( iPart > 0 ) {
	    						b.append(", ");
	    					}
	    					b.append(partIdTypes.get(iPart) + " " + partIds.get(iPart));
	    				}
	    				b.append("]");
	    				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
	    					+ " " + collectionType + " Part IDs (" + PartType + ") -> " + b );
    				}
    				else {
    					// No need to set the division and year...
    					well.setCollectionPartIDsForYear( 0, partIds);
        				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
	    				+ " " + collectionType + " Part IDs (" + PartType + ") -> " + partIds );
	    				
    				}
    				if ( (name != null) && (name.length() != 0) ) {
    					Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
    					+ " " + collectionType + " Name -> " + name );
    					well.setName ( name );
    				}
    			}
    			// Indicate that a list file record match was found...
    			for ( int iv = 0; iv < listrecord_matched.length; iv++ ) {
    				if ( sm_id.equalsIgnoreCase(listfile_ids.get(iv)) ) {
    					listrecord_matched[iv] = true;
    				}
    			}
    		} // End if match
    	} // End locations
    	
    	// Get the count of the list file records that were matched and see if
    	// any were not matched.  This is OK if using a subset of the data.
    	int listrecord_matched_count = 0;
    	for ( int j = 0; j < listrecord_matched.length; j++ ) {
    		if ( listrecord_matched[j]) {
    			++listrecord_matched_count;
    		}
    	}
    	if ( listrecord_matched_count != listrecord_matched.length ) {
    		// Put together a specific warning...
    		StringBuffer b = new StringBuffer();
    		for ( int j = 0; j < listrecord_matched.length; j++ ) {
    			if ( !listrecord_matched[j]) {
    				if ( b.length() == 0 ) {
    					b.append ( listfile_ids.get(j) );
    				}
    				else {
    					b.append ( ", " + listfile_ids.get(j) );
    				}
    			}
    		}
    		String yearString = "";
    		if ( Year != null ) {
    			yearString = " (year " + Year + ")";
    		}
			message = "The following " + nodeTypeFromCommand + " station(s) in the " + collectionType +
				" list file" + yearString + " do not match any " + nodeTypeFromCommand +
				" station IDs in the data set (may be OK if processing an incomplete data set): " + b.toString();
			String message2 = "";
		    if ( nodeTypeFromCommand.equals(_Diversion) &&
	        	((culocList == null) || (culocList.size() == 0)) && ((divList == null) || (divList.size() == 0)) ) {
				message2 = "  Do not use SetDiversion" + collectionType + "FromList() when processing well data.";
			}
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
	            Message.printWarning ( warning_level, 
        	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        	        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
    	                message, "Verify that the list file contents are correct and that the indicated identifiers are included " +
	                		"as " + nodeTypeFromCommand + " stations in the data set as intended." + message2 ) ); 
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
	            Message.printWarning ( warning_level, 
        	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        	        routine, message );
        	        status.addToLog ( commandPhase,
        	            new CommandLogRecord(CommandStatusType.FAILURE,
        	                message, "Verify that the list file contents are correct." + message2 ) );
			}
    	}
    	if ( (TableID != null) && !TableID.isEmpty() ) {
    		// Also set the table in the processor
            PropList request_params = new PropList ( "" );
            request_params.setUsingObject ( "Table", table );
            try {
                processor.processRequest( "SetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetTable(Table=...) from processor.";
                Message.printWarning(warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                           message, "Report problem to software support." ) );
            }
    	} // End TableID set in processor
        } // End no index errors for table

        	// Additional processing to set the WD for collection that are Well RECEIPT:
        	// - TODO smalers 2020-10-10 also need to do for StateMod wells
        	List<String> problems = new ArrayList<>();
        	setCollectionPartReceiptWDForCULocation ( hbdmi, culocList, problems );
        	for ( String problem : problems ) {
        		status.addToLog ( commandPhase,
                	new CommandLogRecord(CommandStatusType.WARNING,
                        problem, "See log file for details." ) );
        	}

        	problems = new ArrayList<>();
        	setCollectionPartReceiptWDForStateModWell ( hbdmi, wellList, problems );
        	for ( String problem : problems ) {
        		status.addToLog ( commandPhase,
                	new CommandLogRecord(CommandStatusType.WARNING,
                        problem, "See log file for details." ) );
        	}

    	} // End run mode
    	else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
    	    // Create an empty table and set the ID
    	    DataTable table = new DataTable();
            table.setTableID ( TableID );
            setDiscoveryTable ( table );
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting " + collectionType + " information (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Additional processing to set the WD for collection parts that are well RECEIPT:
- this code should be kept consistent with the overloaded version for StateMod_Well
- necessary because the WD is used in cached data lookup later
- email from Doug Stenzel 2020-08 (?) indicates that all wells with receipt also have WD
  (but does not need to have ID)
- WDID parts can be split to determine WD but receipt must query the database and match
@param hbdmi HydroBaseDMI for database queries.
@param culocList list of StateCU_Location to process.
*/
private void setCollectionPartReceiptWDForCULocation ( HydroBaseDMI hbdmi, List<StateCU_Location> culocList, List<String> problems )
throws Exception {
	String routine = getClass().getSimpleName() + ".setCollectionPartReceiptWD";
   	// Get the HydroBase_Well instances for the water districts that are candidates to be matched
   	// so that and provide WD.

   	// Get the list of water districts from location IDs.
   	// - example is 17_GW...
   	// - example is 1234567
   	// - example is 1234567I
   	// - example is 1234567D
   	List<Integer> wdList = new ArrayList<>();
   	String culocid;
   	for ( StateCU_Location culoc : culocList ) {
   		culocid = culoc.getID();
   		// Get the first 2 characters
   		// - if an integer, assume the ID starts with a WDID that can be used to extract WD
   		if ( StringUtil.isInteger(culocid.substring(0,2)) ) {
   			wdList.add(new Integer(culocid.substring(0,2)));
   		}
   	}
   	int [] wdArray = new int[wdList.size()];
   	for ( int i = 0; i < wdArray.length; i++ ) {
   		wdArray[i] = wdList.get(i).intValue();
   	}
       
   	// Get the wells for divisions of interest that have non-empty receipt
   	//int [] divArray = HydroBase_WaterDistrict.lookupWaterDivisionIdsForDistricts(hbdmi.getWaterDistricts(), wdArray );
   	int [] divArray = null;
   	String receiptReq = "*";
   	List<HydroBase_Wells> wellsForDivisions = hbdmi.readWellsList( divArray, wdArray, receiptReq );
   	Message.printStatus(2, routine, "Found " + wellsForDivisions.size() +
   		" wells for non-empty receipt in districts from CU Location identifiers." );

   	// Loop through the CULocation and well receipt parts and look up the matching HydroBase well to get WD.
	int year = -1;
	boolean found = false;
   	for ( StateCU_Location culoc : culocList ) {
   		StateCU_Location_CollectionPartType partType = culoc.getCollectionPartType();
   		String receipt = null;
   		if ( partType == StateCU_Location_CollectionPartType.WELL ) {
   			// Have a well.  Loop through the ID list.  The year is irrelevant (only used with parcel ID types).
   			List<String> partIds = culoc.getCollectionPartIDsForYear(year);
   			List<StateCU_Location_CollectionPartIdType> partIdTypes = culoc.getCollectionPartIDTypes();
			String partId;
   			StateCU_Location_CollectionPartIdType partIdType;
   			for ( int i = 0; i < partIds.size(); i++ ) {
   				partId = partIds.get(i);
   				partIdType = partIdTypes.get(i);
   				if ( partIdType == StateCU_Location_CollectionPartIdType.RECEIPT ) {
   					found = false;
   					for ( HydroBase_Wells wellForReceipt : wellsForDivisions ) {
   						receipt = wellForReceipt.getReceipt();
   						if ( (receipt != null) && (receipt.equals(partId)) ) {
   							// Found a receipt.  Set the corresponding WD in the collection part.
   							culoc.getCollectionPartIDWDs().set(i, new Integer(wellForReceipt.getWD()));
   							found = true;
   							break;
   						}
   					}
   					if ( !found ) {
   						String message = "Did not find well \"" + culoc.getID() + "\" part well receipt \"" + partId +
   							"\" in HydroBase wells querying data in well identifier water districts.  "
   							+ "Unable to set WD used to retrieve data from cache."
   							+ "  Well will be omitted from processing.";
   						Message.printWarning(2, routine, message);
   						problems.add(message);
   					}
   				}
   			}
   		}
   	}
}

/**
Additional processing to set the WD for collection parts that are well RECEIPT:
- this code should be kept consistent with the overloaded version for StateCU_Location
- necessary because the WD is used in cached data lookup later
- email from Doug Stenzel 2020-08 (?) indicates that all wells with receipt also have WD
  (but does not need to have ID)
- WDID parts can be split to determine WD but receipt must query the database and match
@param hbdmi HydroBaseDMI for database queries.
@param wellList list of StateMod_Well to process.
*/
private void setCollectionPartReceiptWDForStateModWell ( HydroBaseDMI hbdmi, List<StateMod_Well> wellList, List<String> problems )
throws Exception {
	String routine = getClass().getSimpleName() + ".setCollectionPartReceiptWD";
	if ( (wellList == null) || (wellList.size() == 0) ) {
		// No StateMod data
		return;
	}
   	// Get the HydroBase_Well instances for the water districts that are candidates to be matched
   	// so that and provide WD.

   	// Get the list of water districts from location IDs.
   	// - example is 17_GW...
   	// - example is 1234567
   	// - example is 1234567I
   	// - example is 1234567D
   	List<Integer> wdList = new ArrayList<>();
   	String smwellid;
   	for ( StateMod_Well smwell : wellList ) {
   		smwellid = smwell.getID();
   		// Get the first 2 characters
   		// - if an integer, assume the ID starts with a WDID that can be used to extract WD
   		if ( StringUtil.isInteger(smwellid.substring(0,2)) ) {
   			wdList.add(new Integer(smwellid.substring(0,2)));
   		}
   	}
   	int [] wdArray = new int[wdList.size()];
   	for ( int i = 0; i < wdArray.length; i++ ) {
   		wdArray[i] = wdList.get(i).intValue();
   	}
       
   	// Get the wells for divisions of interest that have non-empty receipt
   	// int [] divArray = HydroBase_WaterDistrict.lookupWaterDivisionIdsForDistricts(hbdmi.getWaterDistricts(), wdArray );
   	int [] divArray = null;
   	String receiptReq = "*";
   	List<HydroBase_Wells> wellsForDivisions = hbdmi.readWellsList( divArray, wdArray, receiptReq );
   	Message.printStatus(2, routine, "Found " + wellsForDivisions.size() +
   		" wells for non-empty receipt in districts from CU Location identifiers." );

   	// Loop through the CULocation and well receipt parts and look up the matching HydroBase well to get WD.
	int year = -1;
	boolean found = false;
   	for ( StateMod_Well smwell : wellList ) {
   		StateMod_Well_CollectionPartType partType = smwell.getCollectionPartType();
   		String receipt = null;
   		if ( partType == StateMod_Well_CollectionPartType.WELL ) {
   			// Have a well.  Loop through the ID list.  The year is irrelevant (only used with parcel ID types).
   			List<String> partIds = smwell.getCollectionPartIDsForYear(year);
   			List<StateMod_Well_CollectionPartIdType> partIdTypes = smwell.getCollectionPartIDTypes();
			String partId;
   			StateMod_Well_CollectionPartIdType partIdType;
   			for ( int i = 0; i < partIds.size(); i++ ) {
   				partId = partIds.get(i);
   				partIdType = partIdTypes.get(i);
   				if ( partIdType == StateMod_Well_CollectionPartIdType.RECEIPT ) {
   					found = false;
   					for ( HydroBase_Wells wellForReceipt : wellsForDivisions ) {
   						receipt = wellForReceipt.getReceipt();
   						if ( (receipt != null) && (receipt.equals(partId)) ) {
   							// Found a receipt.  Set the corresponding WD in the collection part.
   							smwell.getCollectionPartIDWDs().set(i, new Integer(wellForReceipt.getWD()));
   							found = true;
   							break;
   						}
   					}
   					if ( !found ) {
   						String message = "Did not find well \"" + smwell.getID() + "\" part well receipt \"" + partId +
   							"\" in HydroBase wells querying data in well identifier water districts.  "
   							+ "Unable to set WD used to retrieve data from cache."
   							+ "  Well will be omitted from processing.";
   						Message.printWarning(2, routine, message);
   						problems.add(message);
   					}
   				}
   			}
   		}
   	}
}

/**
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String ListFile = parameters.getValue ( "ListFile" );
	String TableID = parameters.getValue ( "TableID" );
	String Year = parameters.getValue ( "Year" );
	String Div = parameters.getValue ( "Div" );
	String PartType = parameters.getValue ( "PartType" );
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String PartIDsCol = parameters.getValue ( "PartIDsCol" );
	String PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
	String PartIDsColMax = parameters.getValue ( "PartIDsColMax" );
	String PartsListedHow = parameters.getValue ( "PartsListedHow" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );

	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( (TableID != null) && (TableID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TableID=\"" + TableID + "\"" );
	}
	if ( (this instanceof SetWellAggregateFromList_Command) ||
		(this instanceof SetWellSystemFromList_Command) ) {
		if ( (Year != null) && (Year.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Year=" + Year );
		}
		if ( (Div != null) && (Div.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Div=" + Div );
		}
		if ( (PartType != null) && (PartType.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "PartType=" + PartType );
		}
	}
	if ( (IDCol != null) && (IDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDCol=\"" + IDCol + "\"" );
	}
	if ( (NameCol != null) && (NameCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NameCol=\"" + NameCol + "\"" );
	}
	if ( (PartIDsCol != null) && (PartIDsCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartIDsCol=\"" + PartIDsCol + "\"" );
	}
	if ( (PartIDTypeColumn != null) && (PartIDTypeColumn.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartIDTypeColumn=\"" + PartIDTypeColumn + "\"");
	}
	if ( (PartsListedHow != null) && (PartsListedHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartsListedHow=" + PartsListedHow );
	}
	if ( (PartIDsColMax != null) && (PartIDsColMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartIDsColMax=\"" + PartIDsColMax + "\"" );
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}