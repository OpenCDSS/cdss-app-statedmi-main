package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_Location;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_Well;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the Set*Aggregate/SystemFromList() commands (set
collection information).
It is an abstract base class that must be controlled via a derived class.  For example, the
SetDiversionAggregateFromList() command extends this class in order to uniquely represent the command,
but much of the functionality is in the base class.
*/
public abstract class SetCollectionFromList_Command extends AbstractCommand implements Command
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
	String NameCol = parameters.getValue ( "NameCol" );
	String PartIDsCol = parameters.getValue ( "PartIDsCol" );
	String PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
	String PartsListedHow = parameters.getValue ( "PartsListedHow" );
	String PartIDsColMax = parameters.getValue ( "PartIDsColMax" );
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
	
	if ( (IDCol == null) ||	(IDCol.length() == 0) ) {
        message = "The ID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column (1+) to read." ) );
	}
	else if ( !StringUtil.isInteger(IDCol) ) {
        message = "The ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column as a number >= 1." ) );
	}
	
	if ( (NameCol != null) && (NameCol.length() != 0) && !StringUtil.isInteger(NameCol) ) {
        message = "The name column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the name column as a number >= 1." ) );
	}
	
	if ( (PartIDsCol == null) || (PartIDsCol.length() == 0) ) {
        message = "The part ID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the part ID column (1+) to read." ) );
	}
	else if ( !StringUtil.isInteger(PartIDsCol) ) {
        message = "The part ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the part ID column as a number >= 1." ) );
	}
	
	if ( (PartsListedHow == null) || (PartsListedHow.length() == 0) ) {
		message = "The PartsListedHow parameter must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the PartsListedHow parameter as " + _InRow + " or " + _InColumn + ".") );
	}
	
	if ( (PartIDsColMax != null) && (PartIDsColMax.length() != 0) && !StringUtil.isInteger(PartIDsColMax) ) {
        message = "The part IDs column (maximum) is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the part IDs column (maximum) as a number >= 1." ) );
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
	List<String> validList = new ArrayList<String>(11);
	validList.add ( "ListFile" );
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
@param command_phase Command phase being executed (RUN or DISCOVERY).
*/
private void runCommandInternal ( int command_number, CommandPhaseType command_phase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
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
	
	String collectionType = null; // Aggregate, System, MultiStruct
	if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_AGGREGATE,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_AGGREGATE;
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_SYSTEM,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_SYSTEM;
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT;
	}

    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
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
    int IDCol_int = Integer.parseInt(IDCol) - 1; // zero reference
    String NameCol = parameters.getValue ( "NameCol" );
    int NameCol_int = -1;
    if ( (NameCol != null) && !NameCol.equals("") ) {
    	NameCol_int = Integer.parseInt(NameCol) - 1; // zero reference
    }
    String PartIDsCol = parameters.getValue ( "PartIDsCol" );
    int PartIDsCol_int = Integer.parseInt(PartIDsCol) - 1; // zero reference
    String PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
    int PartIDTypeColumn_int = -1;
    String PartsListedHow = parameters.getValue ( "PartsListedHow" );
    if ( (PartsListedHow == null) || PartsListedHow.equals("") ) {
    	PartsListedHow = _InRow; // Default
    }
    String PartIDsColMax = parameters.getValue ( "PartIDsColMax" );
    int PartIDsColMax_int = -1;
    if ( (PartIDsColMax != null) && !PartIDsColMax.equals("") ) {
    	PartIDsColMax_int = Integer.parseInt(PartIDsColMax) -1; // zero reference
    }
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
				culocList = (List)o;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting CU Location data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Diversion) ) {
			Object o = processor.getPropContents ( "StateMod_DiversionStation_List" );
			if ( o != null ) {
				divList = (List)o;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting diversion station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Reservoir) ) {
			Object o = processor.getPropContents ( "StateMod_ReservoirStation_List" );
			if ( o != null ) {
				resList = (List)o;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting reservoir station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    try {
		if ( nodeTypeFromCommand.equals(_Well) ) {
			Object o = processor.getPropContents ( "StateMod_WellStation_List" );
			if ( o != null ) {
				wellList = (List)o;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting well station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Try to warn in case where SetDiversion*FromList() is being used with well stations.
    // If no CU Locations and no well stations are found, then assume that the command is being used in error.
    if ( nodeTypeFromCommand.equals(_Diversion) &&
    	((culocList == null) || (culocList.size() == 0)) && ((divList == null) || (divList.size() == 0))) {
        message = "The " + getCommandName() + "() command is being used but no diversion stations have been read.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Use a SetWell" + collectionType +
                "FromList() command instead if D&W nodes are being processed, " +
                "or make sure to read diversion stations before this command." ) );
    }
    
    String ListFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
	if ( !IOUtil.fileExists(ListFile_full) ) {
        message = "File \"" + ListFile_full + "\" does not exist.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the list file name is correct." ) );
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Internal column numbers are zero-referenced, parameters are 1-referenced.  The parameters
    	// were adjusted to zero index above

    	// Read using the table...

    	PropList props = new PropList ("");
    	props.set ( "Delimiter=," ); // see existing prototype
    	props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
    	props.set ( "TrimStrings=True" ); // If true, trim strings after reading.
    	DataTable table = DataTable.parseFile ( ListFile_full, props );

    	int tsize = 0;
    	if ( table != null ) {
    		tsize = table.getNumberOfRecords();
    	}
    	Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
    			table.getNumberOfFields() + " fields" );
    	
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
                    status.addToLog ( command_phase,
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
            status.addToLog ( command_phase,
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
	        status.addToLog ( command_phase,
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
	        status.addToLog ( command_phase,
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
	        status.addToLog ( command_phase,
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
	        status.addToLog ( command_phase,
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
    	List<String> partIds = new ArrayList<String>();
    	List<String> partIdTypes = new ArrayList<String>();
    	boolean foundMatch = false;
    	for (int i = 0; i < size; i++) {
    		matchCount = 0; // Number of matching CU Location ID
    		culoc = (StateCU_Location)culocList.get(i);
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
    				partIds = new ArrayList<String>();
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
    				break; // First match will be used if duplicates.
    			}
    		}
    		else {
    			// The part IDs are in the multiple rows using the indicated column.
    			// Read data until there are no more items in the table...
    			partIds = new ArrayList<String>();
    			partIdTypes = new ArrayList<String>();
    			for ( int j = 0; j < tsize; j++ ) {
    				rec = table.getRecord(j);
    				id=(String)rec.getFieldValue(IDCol_int);
    				if ( id.equalsIgnoreCase( culoc_id) ) {
    					foundMatch = true;
    					partId = (String)rec.getFieldValue(PartIDsCol_int);
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    					}
    					if ( PartIDTypeColumn_int >= 0 ) {
    						// Also save the part id type
    						partIdType = (String)rec.getFieldValue(PartIDTypeColumn_int);
    						partIdTypes.add ( partIdType );
    					}
    				}
    			}
    		}
    		if ( foundMatch ) {
    			++matchCount;
    			culoc.setCollectionType ( collectionType );
    			culoc.setCollectionPartType (PartType);
    			if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Parcel) ) {
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
    				culoc.setCollectionPartIDs( partIds, partIdTypes );
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
    				culoc.setCollectionPartIDs ( partIds );
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
    	} // End loop on locations

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
    				partIds = new ArrayList<String>();
    				partIdTypes = new ArrayList<String>();
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
    			// Read data until the sm_id changes or there are no more items in the table...
    			partIds = new ArrayList<String>();
    			partIdTypes = new ArrayList<String>();
    			name = null;
    			for ( int j = 0; j < tsize; j++ ) {
    				rec = table.getRecord(j);
    				id = (String)rec.getFieldValue(IDCol_int);
    				if ( id.equalsIgnoreCase( sm_id) ) {
    					foundMatch = true;
    					partId = (String) rec.getFieldValue(PartIDsCol_int);
    					if ( partId.length() > 0 ) {
    						partIds.add ( partId );
    					}
    					if ( PartIDTypeColumn_int >= 0 ) {
    						partIdType = (String)rec.getFieldValue(PartIDTypeColumn_int);
    						partIdTypes.add ( partIdType );
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
    			}
    		}
    		if ( foundMatch ) {
    			++matchCount;
    			if ( nodeTypeFromCommand.equalsIgnoreCase( _Diversion) ) {
    				div.setCollectionType ( collectionType );
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
    				res.setCollectionType ( collectionType );
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
    				well.setCollectionType ( collectionType );
    				well.setCollectionPartType (PartType);
    				if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Parcel) ) {
        				// Need the division with wells that are collected by parcels...
	    				well.setCollectionDiv ( Div_int );
	    				well.setCollectionPartIDsForYear( Year_int, partIds);
	    				Message.printStatus ( 2, routine, "Setting StateMod " + sm_id + " " + nodeTypeFromCommand
	    				+ " " + collectionType + " Part IDs (" + PartType + ") " + Year_int + " -> " + partIds );
    				}
    				else if ( nodeTypeFromCommand.equalsIgnoreCase(_Well) && PartType.equalsIgnoreCase(_Well) ) {
        				// Set the well collection as a list of part IDs and their types - same over the full period
	    				well.setCollectionPartIDs( partIds, partIdTypes );
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
    					b.append ( (listfile_ids.get(j)).toString() );
    				}
    				else {
    					b.append ( ", " + (listfile_ids.get(j)).toString() );
    				}
    			}
    		}
    		String yearString = " ";
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
        	        status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
    	                message, "Verify that the list file contents are correct and that the indicated identifiers are included " +
	                		"as " + nodeTypeFromCommand + " stations in the data set as intended." + message2 ) ); 
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
	            Message.printWarning ( warning_level, 
        	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        	        routine, message );
        	        status.addToLog ( command_phase,
        	            new CommandLogRecord(CommandStatusType.FAILURE,
        	                message, "Verify that the list file contents are correct." + message2 ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting " + collectionType + " information (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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
		b.append ( "IDCol=" + IDCol );
	}
	if ( (NameCol != null) && (NameCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NameCol=" + NameCol );
	}
	if ( (PartIDsCol != null) && (PartIDsCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartIDsCol=" + PartIDsCol );
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
		b.append ( "PartIDsColMax=" + PartIDsColMax );
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