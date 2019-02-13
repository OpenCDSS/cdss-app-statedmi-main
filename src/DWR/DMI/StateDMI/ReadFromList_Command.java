// ReadFromList_Command - This class initializes, checks, and runs the Read*FromList() commands, which are used to read StateMod components.

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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Well;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the Read*FromList() commands, which are used to read
StateMod components.  Derived classes should be defined to allow for unique identification of the
commands, but most work is done in this base class.
*/
public class ReadFromList_Command 
extends AbstractCommand implements Command
{
	
/**
Constructor.
*/
public ReadFromList_Command ()
{	super();
	setCommandName ( "Read?FromList" );
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
	//String ID = parameters.getValue ( "ID" );
	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" ); // Stream gage
	String DailyIDCol = parameters.getValue ( "DailyIDCol" ); // Stream gage
	String DiversionIDCol = parameters.getValue ( "DiversionIDCol" ); // Well
	String Top = parameters.getValue ( "Top" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
    if ( (ListFile == null) || (ListFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
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
            if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
            }
        }
        catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + ListFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
        }
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
	
	if ( (this instanceof ReadStreamGageStationsFromList_Command) ||
		(this instanceof ReadStreamEstimateStationsFromList_Command) ) {
		if ( (RiverNodeIDCol != null) && (RiverNodeIDCol.length() != 0) && !StringUtil.isInteger(RiverNodeIDCol) ) {
	        message = "The river node ID column is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the river node ID column as a number >= 1." ) );
		}
		if ( (DailyIDCol != null) && (DailyIDCol.length() != 0) && !StringUtil.isInteger(DailyIDCol) ) {
	        message = "The daily ID column is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the daily ID column as a number >= 1." ) );
		}
	}
	
	if ( this instanceof ReadWellStationsFromList_Command ) {
		if ( (DiversionIDCol != null) && (DiversionIDCol.length() != 0) && !StringUtil.isInteger(DiversionIDCol) ) {
	        message = "The diversion ID column is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the diversion ID column as a number >= 1." ) );
		}
	}
	
	if ( (Top != null) && (Top.length() != 0) && !StringUtil.isInteger(Top) ) {
        message = "The Top parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the Top parameter as a number >= 1." ) );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(7);
	validList.add ( "ListFile" );
	validList.add ( "IDCol" );
	validList.add ( "NameCol" );
	if ( (this instanceof ReadStreamGageStationsFromList_Command) ||
		(this instanceof ReadStreamEstimateStationsFromList_Command) ) {
		validList.add ( "RiverNodeIDCol" );
		validList.add ( "DailyIDCol" );
	}
	if ( this instanceof ReadWellStationsFromList_Command ) {
		validList.add ( "DiversionIDCol" );
	}
	validList.add ( "Top" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Check the default WDID length in the processor, used to generate WDIDs that are consistent with input.
@param defaultWdidLength starting value for default WDID length.
@param id identifier of location to check.
@return updated value of defaultWdidLength.
*/
private int checkDefaultWdidLength ( int defaultWdidLength, String id )
{	String routine = "ReadFromList_Command.checkDefaultWdidLength";
	// Assume numeric fields are WDIDs and reset the default width for WDIDs in output.
	// This is used to make identifiers consistent in the data sets.
	if ( HydroBase_WaterDistrict.isWDID(id) && (id.length() == 6) || (id.length() == 7) ) {
		int defaultWdidLengthPrev = defaultWdidLength;
		defaultWdidLength = id.length();
		if ( defaultWdidLength != defaultWdidLengthPrev ) {
			// Only print the message when the value changes.
			Message.printStatus ( 2, routine,
			"Reading well list - assuming WDID length should be " + defaultWdidLength +
			" as default for other processing.");
		}
	}
	return defaultWdidLength;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ReadFromList_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Process the diversion station list file.
*/
private int processDiversionStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processDiversionStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source.  This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionStationMatchList() );

	// Loop through the records in the table.

	StateMod_Diversion div;
	String id, name;
	TableRecord rec = null;
	int defaultWdidLength = ((Integer)processor.getPropContents("DefaultWdidLength")).intValue();
	for (int i = 0; i < size; i++) {
		div = new StateMod_Diversion ( false );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) { // Sometimes blank lines
			continue;
		}
		div.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			div.setName ( name );
		}
		// Replace or add in the diversion list...
		processor.findAndAddSMDiversion ( div, true );
		// Check the default WDID length in the processor
		defaultWdidLength = checkDefaultWdidLength ( defaultWdidLength, div.getID() );
	}
	
	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModDiversionStationMatchList(),
			"Diversion Stations" );
	
	// Set the default identifier length in the processor.
	if ( defaultWdidLength > 0 ) {
		getCommandProcessor().setPropContents("WDIDLength", new Integer(defaultWdidLength) );
	}
	
	return warning_count;
}

/**
Process the instream flow station list file.
*/
private int processInstreamFlowStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processInstreamFlowStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source.  This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModInstreamFlowStationMatchList() );

	// Loop through the records in the table.

	StateMod_InstreamFlow ifs;
	String id, name;
	TableRecord rec = null;
	int defaultWdidLength = ((Integer)processor.getPropContents("DefaultWdidLength")).intValue();
	for (int i = 0; i < size; i++) {
		ifs = new StateMod_InstreamFlow ( false );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) { // Sometimes blank lines
			continue;
		}
		ifs.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			ifs.setName ( name );
		}
		// Replace or add in the __SMStreamGage_Vector...
		processor.findAndAddSMInstreamFlow ( ifs, true );
		// Check the default WDID length in the processor
		defaultWdidLength = checkDefaultWdidLength ( defaultWdidLength, ifs.getID() );
	}
	
	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModInstreamFlowStationMatchList(),
			"Instream Flow Stations" );
	
	// Set the default identifier length in the processor.
	if ( defaultWdidLength > 0 ) {
		getCommandProcessor().setPropContents("WDIDLength", new Integer(defaultWdidLength) );
	}
	
	return warning_count;
}

/**
Process the reservoir station list file.
*/
private int processReservoirStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processReservoirStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source.  This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModReservoirStationMatchList() );

	// Loop through the records in the table.

	StateMod_Reservoir res;
	String id, name;
	TableRecord rec = null;
	int defaultWdidLength = ((Integer)processor.getPropContents("DefaultWdidLength")).intValue();
	for (int i = 0; i < size; i++) {
		res = new StateMod_Reservoir ( false );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) { // Sometimes blank lines
			continue;
		}
		res.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			res.setName ( name );
		}
		// Replace or add in the reservoir list...
		processor.findAndAddSMReservoir ( res, true );
		// Check the default WDID length in the processor
		defaultWdidLength = checkDefaultWdidLength ( defaultWdidLength, res.getID() );
	}
	
	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModReservoirStationMatchList(),
			"Reservoir Stations" );
	
	// Set the default identifier length in the processor.
	if ( defaultWdidLength > 0 ) {
		getCommandProcessor().setPropContents("WDIDLength", new Integer(defaultWdidLength) );
	}
	
	return warning_count;
}

/**
Process the well station list file.
*/
private int processStreamEstimateStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int, int RiverNodeIDCol_int, int DailyIDCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processStreamEstimateStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source.  This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamEstimateStationMatchList() );

	// Loop through the records in the table.

	StateMod_StreamEstimate estimate;
	String id, name, riverid, dailyid;
	TableRecord rec = null;
	for (int i = 0; i < size; i++) {
		estimate = new StateMod_StreamEstimate ( false );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) {	// Sometimes blank lines
			continue;
		}
		estimate.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			estimate.setName ( name );
		}
		if ( RiverNodeIDCol_int >= 0 ) {
			riverid = (String)rec.getFieldValue(RiverNodeIDCol_int);
			estimate.setCgoto ( riverid );
		}
		if ( DailyIDCol_int >= 0 ) {
			dailyid = (String)rec.getFieldValue(DailyIDCol_int);
			estimate.setCrunidy ( dailyid );
		}
		// Replace or add in the __SMStreamEstimate_Vector...
		processor.findAndAddSMStreamEstimate ( estimate, true );
	}
	
	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModStreamEstimateStationMatchList(),
			"Stream Estimate Stations" );
	return warning_count;
}

/**
Process the stream gage station list file.
*/
private int processStreamGageStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int, int RiverNodeIDCol_int, int DailyIDCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processStreamGageStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source.  This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamGageStationMatchList() );

	// Loop through the records in the table.

	StateMod_StreamGage gage;
	String id, name, riverid, dailyid;
	TableRecord rec = null;
	for (int i = 0; i < size; i++) {
		gage = new StateMod_StreamGage ( false );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) { // Sometimes blank lines
			continue;
		}
		gage.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			gage.setName ( name );
		}
		if ( RiverNodeIDCol_int >= 0 ) {
			riverid = (String)rec.getFieldValue(RiverNodeIDCol_int);
			gage.setCgoto ( riverid );
		}
		if ( DailyIDCol_int >= 0 ) {
			dailyid = (String)rec.getFieldValue(DailyIDCol_int);
			gage.setCrunidy ( dailyid );
		}
		// Replace or add in the __SMStreamGage_Vector...
		processor.findAndAddSMStreamGage ( gage, true );
	}
	
	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModStreamGageStationMatchList(),
			"Stream Gage Stations" );
	return warning_count;
}

/**
Process the well station list file.
*/
private int processWellStations ( StateDMI_Processor processor, DataTable table,
		int warning_count,
		int IDCol_int, int NameCol_int, int DiversionIDCol_int )
throws Exception
{	//String routine = "ReadFromList_Command.processWellStations";
	int size = table.getNumberOfRecords();
	
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source (e.g., DDS, and STR).
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModWellStationMatchList() );

	// Loop through the records in the table.

	StateMod_Well well;
	String id, name, diversion_id;
	int defaultWdidLength = ((Integer)processor.getPropContents("DefaultWdidLength")).intValue();
	TableRecord rec = null;
	for (int i = 0; i < size; i++) {
		well = new StateMod_Well ( false );
		// TODO SAM 2005-10-12
		// This could be filled with a command but default here at the request of Ray Bennett.
		well.setIdvcow2 ( "NA" );
		rec = table.getRecord(i);
		id = (String)rec.getFieldValue(IDCol_int);
		if ( id.length() == 0 ) {	// Sometimes blank lines
			continue;
		}
		well.setID ( id );
		if ( NameCol_int >= 0 ) {
			name = (String)rec.getFieldValue(NameCol_int);
			well.setName ( name );
		}
		if ( DiversionIDCol_int >= 0 ) {
			diversion_id = (String)rec.getFieldValue(DiversionIDCol_int);
			well.setIdvcow2 ( diversion_id );
		}
		// Replace or add in the __SMWell_Vector...
		processor.findAndAddSMWell ( well, true );
		// Check the default WDID length in the processor
		defaultWdidLength = checkDefaultWdidLength ( defaultWdidLength, well.getID() );
	}
	
	// Set the default identifier length in the processor.
	if ( defaultWdidLength > 0 ) {
		getCommandProcessor().setPropContents("WDIDLength", new Integer(defaultWdidLength) );
	}

	// Warn about identifiers that have been replaced in the well station list.

	processor.warnAboutDataMatches ( this, true, processor.getStateModWellStationMatchList(), "Well Stations" );
	return warning_count;
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
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
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
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
    status.clearLog(command_phase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
    String IDCol = parameters.getValue ( "IDCol" );
    String NameCol = parameters.getValue ( "NameCol" );
    String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
    String DailyIDCol = parameters.getValue ( "DailyIDCol" );
    String DiversionIDCol = parameters.getValue ( "DiversionIDCol" ); // Wells
    String Delim = parameters.getValue ( "Delim" );
    if ( (Delim == null) || (Delim.length() == 0) ) {
    	Delim = ","; // Default
    }
    String Top = parameters.getValue ( "Top" );
    int top = -1;
    if ( (Top != null) && !Top.isEmpty() ) {
    	top = Integer.parseInt(Top);
    }

    // Get columns, all zero offset
    
    int IDCol_int = -1;
    if ( IDCol != null ) {
        IDCol_int = Integer.parseInt(IDCol) - 1;
    }
    int NameCol_int = -1;
    if ( NameCol != null ) {
    	NameCol_int = Integer.parseInt(NameCol) - 1;
    }
    int RiverNodeIDCol_int = -1;
    if ( RiverNodeIDCol != null ) {
    	RiverNodeIDCol_int = Integer.parseInt(RiverNodeIDCol) - 1;
    }
    int DailyIDCol_int = -1;
    if ( DailyIDCol != null ) {
    	DailyIDCol_int = Integer.parseInt(DailyIDCol) - 1;
    }
    int DiversionIDCol_int = -1;
    if ( DiversionIDCol != null ) {
    	DiversionIDCol_int = Integer.parseInt(DiversionIDCol) - 1;
    }

    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    // Now try to read...

    String ListFile_full = ListFile;
    try {
    	boolean foundWdidLength = false;	// Used to determine a default WDID length for processing
        boolean readData = true;
        if ( command_phase == CommandPhaseType.DISCOVERY ){
            readData = false;
        }
        ListFile_full = IOUtil.verifyPathForOS(
                IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
        Message.printStatus ( 2, routine, "Reading list file \"" + ListFile_full + "\"" );
    
    	if ( !IOUtil.fileExists(ListFile_full) ) {
    		message = "List file \"" + ListFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(ListFile_full) ) {
    		message = "List file \"" + ListFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}

    	// Read using the table...

    	PropList props = new PropList ("");
    	props.set ( "Delimiter=" + Delim );
    	props.set ( "CommentLineIndicator=#" );
    	props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
    	if ( top > 0 ) {
    		props.set ( "Top=" + top );
    	}
    	DataTable table = null;
    	try {
    		table = DataTable.parseFile ( ListFile_full, props );
    	}
    	catch ( Exception e ) {
    		message = "Unexpected error parsing list file \"" + ListFile_full + "\".";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify the format of the file."));
            Message.printWarning ( 3, routine, e );
            throw new CommandException ( message );
    	}
    	
    	// Make sure that requested columns are available
    	
    	// Basic columns used by all commands...
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"NameCol", NameCol, NameCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	// Additional columns used by some commands...
    	if ( this instanceof ReadStreamGageStationsFromList_Command  ||
    		this instanceof ReadStreamEstimateStationsFromList_Command) {
    		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        			"RiverNodeIDCol", RiverNodeIDCol, RiverNodeIDCol_int, table.getNumberOfFields(),
        			status, command_phase, routine, command_tag, warning_level, warning_count );
    		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        			"DailyIDCol", DailyIDCol, DailyIDCol_int, table.getNumberOfFields(),
        			status, command_phase, routine, command_tag, warning_level, warning_count );
    	}
    	if ( this instanceof ReadWellStationsFromList_Command ) {
	    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
	    			"DiversionIDCol", DiversionIDCol, DiversionIDCol_int, table.getNumberOfFields(),
	    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	}
    	
    	// Process the table into model objects.
    	
       	if ( this instanceof ReadStreamEstimateStationsFromList_Command ) {
    		warning_count = processStreamEstimateStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int, RiverNodeIDCol_int, DailyIDCol_int );
    	}
       	else if ( this instanceof ReadStreamGageStationsFromList_Command ) {
    		warning_count = processStreamGageStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int, RiverNodeIDCol_int, DailyIDCol_int );
    	}
       	else if ( this instanceof ReadDiversionStationsFromList_Command ) {
    		warning_count = processDiversionStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int );
    	}
       	else if ( this instanceof ReadReservoirStationsFromList_Command ) {
    		warning_count = processReservoirStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int );
    	}
       	else if ( this instanceof ReadInstreamFlowStationsFromList_Command ) {
    		warning_count = processInstreamFlowStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int );
    	}
       	else if ( this instanceof ReadWellStationsFromList_Command ) {
    		warning_count = processWellStations ( processor, table,
    			warning_count, IDCol_int, NameCol_int, DiversionIDCol_int );
    	}
       	else {
       		throw new RuntimeException ( "Don't know how to process: " + this );
       	}
    }
    catch ( CommandException e ) {
    	// Just pass through
    	throw e;
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading list file \"" + ListFile_full + "\" (" + e + ").";
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
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
	String DailyIDCol = parameters.getValue ( "DailyIDCol" );
	String DiversionIDCol = parameters.getValue ( "DiversionIDCol" );
	String Top = parameters.getValue ( "Top" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
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
	if ( this instanceof ReadStreamGageStationsFromList_Command ||
		this instanceof ReadStreamEstimateStationsFromList_Command ) {
		if ( (RiverNodeIDCol != null) && (RiverNodeIDCol.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "RiverNodeIDCol=" + RiverNodeIDCol );
		}
		if ( (DailyIDCol != null) && (DailyIDCol.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "DailyIDCol=" + DailyIDCol );
		}
	}
	if ( this instanceof ReadWellStationsFromList_Command ) {
		if ( (DiversionIDCol != null) && (DiversionIDCol.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "DiversionIDCol=" + DiversionIDCol  );
		}
	}
	if ( (Top != null) && (Top.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Top=" + Top );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
