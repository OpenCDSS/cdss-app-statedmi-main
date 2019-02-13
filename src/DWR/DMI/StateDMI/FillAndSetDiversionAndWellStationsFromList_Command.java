// FillAndSetDiversionAndWellStationsFromList_Command - This class initializes, checks,
// and runs the FillDiversionStationsFromList(), SetDiversionStationsFromList(), FillWellStationsFromList(), and
// SetWellStationsFromList() commands.

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

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.YearType;
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
This class initializes, checks, and runs the FillDiversionStationsFromList(),
SetDiversionStationsFromList(), FillWellStationsFromList(), and
SetWellStationsFromList() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetDiversionStationsFromList()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
*/
public abstract class FillAndSetDiversionAndWellStationsFromList_Command extends AbstractCommand implements Command
{

/**
Values for boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Values for Delim parameter.
*/
protected final String _Space = "Space";
protected final String _Tab = "Tab";
protected final String _Whitespace = "Whitespace";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetDiversionAndWellStationsFromList_Command ()
{	super();
	setCommandName ( "?Fill?Set?Diversion?Well?StationsFromList" );
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
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	//String Delim = parameters.getValue ( "Delim" );
	//String MergeDelim = parameters.getValue ( "MergeDelim" );
	// Diversion stations...
	String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
	String OnOffCol = parameters.getValue ( "OnOffCol" );
	String CapacityCol = parameters.getValue ( "CapacityCol" );
	String ReplaceResOptionCol = parameters.getValue ( "ReplaceResOptionCol" );
	String DailyIDCol = parameters.getValue ( "DailyIDCol" );
	String UserNameCol = parameters.getValue ( "UserNameCol" );
	String DemandTypeCol = parameters.getValue ( "DemandTypeCol" );
	String IrrigatedAcresCol = parameters.getValue ( "IrrigatedAcresCol" );
	String UseTypeCol = parameters.getValue ( "UseTypeCol" );
	String DemandSourceCol = parameters.getValue ( "DemandSourceCol" );
	String EffAnnualCol = parameters.getValue ( "EffAnnualCol" );
	String EffMonthlyCol = parameters.getValue ( "EffMonthlyCol" );
	// StateMod wells...
	String DiversionIDCol = parameters.getValue ( "DiversionIDCol" );
	String AdminNumShiftCol = parameters.getValue ( "AdminNumShiftCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
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
	
	if ( (RiverNodeIDCol != null) && (RiverNodeIDCol.length() != 0) && !StringUtil.isInteger(RiverNodeIDCol) ) {
        message = "The river node ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the river node ID column as a number >= 1." ) );
	}
	
	if ( (OnOffCol != null) && (OnOffCol.length() != 0) && !StringUtil.isInteger(OnOffCol) ) {
        message = "The on/off column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the on/off column as a number >= 1." ) );
	}
	
	if ( (CapacityCol != null) && (CapacityCol.length() != 0) && !StringUtil.isInteger(CapacityCol) ) {
        message = "The capacity column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the capacity column as a number >= 1." ) );
	}
	
	if ( (ReplaceResOptionCol != null) && (ReplaceResOptionCol.length() != 0) &&
		!StringUtil.isInteger(ReplaceResOptionCol) ) {
        message = "The replacement reservoir option column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the replacement reservoir option column as a number >= 1." ) );
	}
	
	if ( (DailyIDCol != null) && (DailyIDCol.length() != 0) && !StringUtil.isInteger(DailyIDCol) ) {
        message = "The daily ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the daily ID column as a number >= 1." ) );
	}
	
	if ( (UserNameCol != null) && (UserNameCol.length() != 0) && !StringUtil.isInteger(UserNameCol) ) {
        message = "The user name column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the user name column as a number >= 1." ) );
	}
	
	if ( (DemandTypeCol != null) && (DemandTypeCol.length() != 0) && !StringUtil.isInteger(DemandTypeCol) ) {
        message = "The demand type column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the demand type column as a number >= 1." ) );
	}
	
	if ( (IrrigatedAcresCol != null) && (IrrigatedAcresCol.length() != 0) && !StringUtil.isInteger(IrrigatedAcresCol) ) {
        message = "The irrigated acres column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the irrigated acres column as a number >= 1." ) );
	}
	
	if ( (UseTypeCol != null) && (UseTypeCol.length() != 0) && !StringUtil.isInteger(UseTypeCol) ) {
        message = "The use type column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the use type column as a number >= 1." ) );
	}
	
	if ( (DemandSourceCol != null) && (DemandSourceCol.length() != 0) && !StringUtil.isInteger(DemandSourceCol) ) {
        message = "The demand source column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the demand source column as a number >= 1." ) );
	}
	
	if ( (EffAnnualCol != null) && (EffAnnualCol.length() != 0) && !StringUtil.isInteger(EffAnnualCol) ) {
        message = "The efficiency (annual) column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the efficiency (annual) column as a number >= 1." ) );
	}
	
	if ( (EffMonthlyCol != null) && (EffMonthlyCol.length() != 0) && !StringUtil.isInteger(EffMonthlyCol) ) {
        message = "The efficiency (monthly) column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the efficiency (monthly) column as a number >= 1." ) );
	}
	
	if ( (DiversionIDCol != null) && (DiversionIDCol.length() != 0) && !StringUtil.isInteger(DiversionIDCol) ) {
        message = "The diversion ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the diversion ID column as a number >= 1." ) );
	}
	
	if ( (AdminNumShiftCol != null) && (AdminNumShiftCol.length() != 0) && !StringUtil.isInteger(AdminNumShiftCol) ) {
        message = "The administration number shift column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the administration number shift column as a number >= 1." ) );
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
	List valid_Vector = new Vector();
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "NameCol" );
	valid_Vector.add ( "Delim" );
	valid_Vector.add ( "MergeDelim" );
	valid_Vector.add ( "RiverNodeIDCol" );
	valid_Vector.add ( "OnOffCol" );
	valid_Vector.add ( "CapacityCol" );
	if ( (this instanceof FillDiversionStationsFromList_Command) ||
		(this instanceof SetDiversionStationsFromList_Command) ) {
		valid_Vector.add ( "ReplaceResOptionCol" );
	}
	valid_Vector.add ( "DailyIDCol" );
	valid_Vector.add ( "UserNameCol" );
	valid_Vector.add ( "DemandTypeCol" );
	valid_Vector.add ( "IrrigatedAcresCol" );
	valid_Vector.add ( "UseTypeCol" );
	valid_Vector.add ( "DemandSourceCol" );
	valid_Vector.add ( "EffAnnualCol" );
	valid_Vector.add ( "EffMonthlyCol" );
	if ( (this instanceof FillWellStationsFromList_Command) ||
		(this instanceof SetWellStationsFromList_Command) ) {
		valid_Vector.add ( "DiversionIDCol" );
		valid_Vector.add ( "AdminNumShiftCol" );
	}
	valid_Vector.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ), warning );
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
	return (new FillAndSetDiversionAndWellStationsFromList_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    boolean do_fill = true;
    if ( (this instanceof SetDiversionStationsFromList_Command) ||
    	(this instanceof SetWellStationsFromList_Command) ) {
    	do_fill = false;
    }
 
    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String Delim = parameters.getValue ( "Delim" );
    if ( Delim == null ) {
        Delim = ",";    // Default
	}
	else if ( Delim.equalsIgnoreCase("Space") ) {
	        Delim = " ";
	}
	else if ( Delim.equalsIgnoreCase("Tab") ) {
	        Delim = "\t";
	}
	else if ( Delim.equalsIgnoreCase("Whitespace") ) {
	        Delim = " \t";
	}
	String MergeDelim = parameters.getValue ( "MergeDelim" );
	if ( MergeDelim == null ) {
		MergeDelim = _False; // Default
	}
	// Diversion stations...
	String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
	String OnOffCol = parameters.getValue ( "OnOffCol" );
	String CapacityCol = parameters.getValue ( "CapacityCol" );
	String ReplaceResOptionCol = parameters.getValue ( "ReplaceResOptionCol" );
	String DailyIDCol = parameters.getValue ( "DailyIDCol" );
	String UserNameCol = parameters.getValue ( "UserNameCol" );
	String DemandTypeCol = parameters.getValue ( "DemandTypeCol" );
	String IrrigatedAcresCol = parameters.getValue ( "IrrigatedAcresCol" );
	String UseTypeCol = parameters.getValue ( "UseTypeCol" );
	String DemandSourceCol = parameters.getValue ( "DemandSourceCol" );
	String EffAnnualCol = parameters.getValue ( "EffAnnualCol" );
	String EffMonthlyCol = parameters.getValue ( "EffMonthlyCol" );
	// StateMod wells...
	String DiversionIDCol = parameters.getValue ( "DiversionIDCol" );
	String AdminNumShiftCol = parameters.getValue ( "AdminNumShiftCol" );
	
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
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
    int OnOffCol_int = -1;
    if ( OnOffCol != null ) {
    	OnOffCol_int = Integer.parseInt(OnOffCol) - 1;
    }
    int CapacityCol_int = -1;
    if ( CapacityCol != null ) {
    	CapacityCol_int = Integer.parseInt(CapacityCol) - 1;
    }
    int ReplaceResOptionCol_int = -1;
    if ( ReplaceResOptionCol != null ) {
    	ReplaceResOptionCol_int = Integer.parseInt(ReplaceResOptionCol) - 1;
    }
    int DailyIDCol_int = -1;
    if ( DailyIDCol != null ) {
    	DailyIDCol_int = Integer.parseInt(DailyIDCol) - 1;
    }
    int UserNameCol_int = -1;
    if ( UserNameCol != null ) {
    	UserNameCol_int = Integer.parseInt(UserNameCol) - 1;
    }
    int DemandTypeCol_int = -1;
    if ( DemandTypeCol != null ) {
    	DemandTypeCol_int = Integer.parseInt(DemandTypeCol) - 1;
    }
    int IrrigatedAcresCol_int = -1;
    if ( IrrigatedAcresCol != null ) {
    	IrrigatedAcresCol_int = Integer.parseInt(IrrigatedAcresCol) - 1;
    }
    int UseTypeCol_int = -1;
    if ( UseTypeCol != null ) {
    	UseTypeCol_int = Integer.parseInt(UseTypeCol) - 1;
    }
    int DemandSourceCol_int = -1;
    if ( DemandSourceCol != null ) {
    	DemandSourceCol_int = Integer.parseInt(DemandSourceCol) - 1;
    }
    int EffAnnualCol_int = -1;
    if ( EffAnnualCol != null ) {
    	EffAnnualCol_int = Integer.parseInt(EffAnnualCol) - 1;
    }
    int EffMonthlyCol_int = -1;
    if ( EffMonthlyCol != null ) {
    	EffMonthlyCol_int = Integer.parseInt(EffMonthlyCol) - 1;
    }
    int DiversionIDCol_int = -1;
    if ( DiversionIDCol != null ) {
    	DiversionIDCol_int = Integer.parseInt(DiversionIDCol) - 1;
    }
    int AdminNumShiftCol_int = -1;
    if ( AdminNumShiftCol != null ) {
    	AdminNumShiftCol_int = Integer.parseInt(AdminNumShiftCol) - 1;
    }
    
    // Get the data needed for the command
    
    boolean do_diversions = false;
    boolean do_wells = false;
    List divList = null;
    int listSize = 0;
    List wellList = null;
    try {
    	if ( (this instanceof FillDiversionStationsFromList_Command) ||
    		(this instanceof SetDiversionStationsFromList_Command)	) {
    		divList = (List)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		if ( divList != null ) {
    			listSize = divList.size();
    		}
    		do_diversions = true;
    	}
    	else if ( (this instanceof FillWellStationsFromList_Command) ||
    		(this instanceof SetWellStationsFromList_Command) ) {
    		wellList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
    		if ( wellList != null ) {
    			listSize = wellList.size();
    		}
    		do_wells = true;
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    YearType outputYearType = null;
    try {
    	outputYearType = (YearType)processor.getPropContents ( "OutputYearType" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputYearType (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    String ListFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
    Message.printStatus ( 2, routine, "Reading CU locations list file \"" + ListFile_full + "\"" );

	if ( !IOUtil.fileExists(ListFile_full) ) {
		message = "List file \"" + ListFile_full + "\" does not exist.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
	}
	if ( !IOUtil.fileReadable(ListFile_full) ) {
		message = "List file \"" + ListFile_full + "\" is not readable.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

	String action = "filling"; // Reset below, for messages
    try {
    	// Read using the table...

    	PropList props = new PropList ("");
    	props.set ( "Delimiter",Delim ); // see existing prototype
    	props.set ( "CommentLineIndicator=#" ); // New - skip lines that start with this
    	props.set ( "TrimStrings=True" ); // Trim strings after reading.
    	props.set ( "TrimInput=True" ); // Trim input line before reading.
    	if ( MergeDelim != null ) {
    		props.set ( "MergeDelimiters", MergeDelim ); // If true merge delimiters
    	}
    	DataTable table = null;
    	int tsize = 0;
    	try {
    		table = DataTable.parseFile ( ListFile_full, props );
    		tsize = table.getNumberOfRecords();
    	}
    	catch ( Exception e ) {
    		message = "Unexpected error parsing list file \"" + ListFile_full + "\"(" + e + ").";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify the format of the file."));
            Message.printWarning ( 3, routine, e );
            throw new CommandException ( message );
    	}
    	
    	// Make sure that requested columns are available
    	
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"NameCol", NameCol, NameCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"RiverNodeIDCol", RiverNodeIDCol, RiverNodeIDCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"OnOffCol", OnOffCol, OnOffCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"CapacityCol", CapacityCol, CapacityCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"ReplaceResOptionCol", ReplaceResOptionCol, ReplaceResOptionCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    		"DailyIDCol", DailyIDCol, DailyIDCol_int, table.getNumberOfFields(),
    		status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"UserNameCol", UserNameCol, UserNameCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"DemandTypeCol", DemandTypeCol, DemandTypeCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"IrrigatedAcresCol", IrrigatedAcresCol, IrrigatedAcresCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"UseTypeCol", UseTypeCol, UseTypeCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"DemandSourceCol", DemandSourceCol, DemandSourceCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"EffAnnualCol", EffAnnualCol, EffAnnualCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"EffMonthlyCol", EffMonthlyCol, EffMonthlyCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"DiversionIDCol", DiversionIDCol, DiversionIDCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
        	"AdminNumShiftCol", AdminNumShiftCol, AdminNumShiftCol_int, table.getNumberOfFields(),
        	status, command_phase, routine, command_tag, warning_level, warning_count );
    	
    	// Remove all the elements for the Vector that tracks when identifiers
    	// are read from more than one main source (e.g., DDS, and STR).
    	// This is used to print a warning.
    	if ( do_diversions ) {
    		processor.resetDataMatches ( processor.getStateModDiversionStationMatchList() );
    	}
    	else if ( do_wells ) {
    		processor.resetDataMatches ( processor.getStateModWellStationMatchList() );
    	}
    	
    	// Do this the brute force way because there are not that many records.

    	// Loop through the stations that are in memory...  For each one,
    	// search the table and modify information...

    	StateMod_Diversion dds = null;	// Instance to edit
    	StateMod_Well wes = null;	// Instance to edit
    	// Diversions and wells...
    	String id = null, id_rec;
    	String Name;
    	String RiverNodeID;
    	String OnOff;
    	String Capacity;
    	String ReplaceResOption;
    	String DailyID;
    	String UserName;
    	String DemandType;
    	String IrrigatedAcres;
    	String UseType;
    	String DemandSource;
    	String EffAnnual;
    	String EffMonthly = null;
    	// Wells...
    	String AdminNumShift;
    	String DiversionID;

    	TableRecord rec = null;
    	int matchCount = 0;
    	double [] EffMonthly_double = new double[12]; // Monthly efficiencies (always Jan-Dec)
    	double total; // Sum of efficiencies used to calculate an average.
    	boolean ismissing = false;	// Used for result of isDataMissing().
    	String Action = "Filling ";	// For messages
    	action = "filling";	// For messages
    	if ( !do_fill ) {
    		Action = "Setting ";
    		action = "setting";
    	}
    	for ( int i = 0; i < listSize; i++ ) {
    		if ( do_diversions ) {
    			dds = (StateMod_Diversion)divList.get(i);
    			id = dds.getID();
    		}
    		else if ( do_wells ) {
    			wes = (StateMod_Well)wellList.get(i);
    			id = wes.getID();
    		}
    		// First check to see if it desired to update the location...
    		/* TODO SAM 2005=11-15 - this may be used later
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		*/
    		// Now loop through the table and see if there are any matches for the ID...
    		for ( int j = 0; j < tsize; j++ ) {
    			rec = table.getRecord(j);
    			id_rec = (String)rec.getFieldValue(IDCol_int);
    			Message.printStatus(2, routine, "Checking DSS ID \"" + id + "\" against list id \"" +
    				id_rec + "\"");
    			if ( !id.equalsIgnoreCase(id_rec) ) {
    				continue;
    			}
    			// Have a match...
    			if ( do_diversions && (NameCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getName()))) {
    				Name = (String)rec.getFieldValue(NameCol_int);
    				Message.printStatus ( 2, routine, Action + id + " Name -> " + Name );
    				dds.setName( Name );
    			}
    			else if(do_wells && (NameCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getName()))) {
    				Name = (String)rec.getFieldValue(NameCol_int);
    				Message.printStatus ( 2, routine, Action + id + " Name -> " + Name );
    				wes.setName( Name );
    			}
    			if ( do_diversions && (RiverNodeIDCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getCgoto()))) {
    				RiverNodeID = (String)rec.getFieldValue( RiverNodeIDCol_int);
    				Message.printStatus ( 2, routine, Action + id + " RiverNodeID -> " + RiverNodeID );
    				dds.setCgoto( RiverNodeID );
    			}
    			else if(do_wells && (RiverNodeIDCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getCgoto()))) {
    				RiverNodeID = (String)rec.getFieldValue(RiverNodeIDCol_int);
    				Message.printStatus ( 2, routine, Action + id + " RiverNodeID -> " + RiverNodeID );
    				wes.setCgoto( RiverNodeID );
    			}
    			if ( do_diversions && (OnOffCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getSwitch()))) {
    				OnOff = (String)rec.getFieldValue(OnOffCol_int);
    				if ( !StringUtil.isInteger(OnOff) ) {
    					message = "In list file for location \"" + id + "\" OnOff (" + OnOff +
    					") is not an integer.  Not " + action + ".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " OnOff -> " + OnOff );
    					dds.setSwitch( StringUtil.atoi(OnOff) );
    				}
    			}
    			else if(do_wells && (OnOffCol_int > 0) &&
    				(!do_fill ||StateMod_Util.isMissing(wes.getSwitch()))) {
    				OnOff = (String)rec.getFieldValue(OnOffCol_int);
    				if ( !StringUtil.isInteger(OnOff) ) {
    					message = "In list file for location \"" + id + "\" OnOff (" + OnOff +
    					") is not an integer.  Not " + action + ".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " OnOff -> " + OnOff );
    					wes.setSwitch ( StringUtil.atoi(OnOff) );
    				}
    			}
    			if ( do_diversions && (CapacityCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getDivcap()))) {
    				Capacity = (String)rec.getFieldValue(CapacityCol_int);
    				if ( !StringUtil.isDouble(Capacity) ) {
    					message = "In list file for location \"" + id + "\" Capacity (" +
    					Capacity + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " Capacity -> " + Capacity );
    					dds.setDivcap( StringUtil.atod(Capacity) );
    				}
    			}
    			else if(do_wells && (CapacityCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getDivcapw()))) {
    				Capacity = (String)rec.getFieldValue(CapacityCol_int);
    				if ( !StringUtil.isDouble(Capacity) ) {
    					message = "In list file for location \"" + id + "\" Capacity (" +
    					Capacity + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " Capacity -> " + Capacity );
    					wes.setDivcapw(	StringUtil.atod(Capacity) );
    				}
    			}
    			if ( do_diversions && (ReplaceResOptionCol_int > 0)&&
    				(!do_fill || StateMod_Util.isMissing(dds.getIreptype()))) {
    				ReplaceResOption = (String)rec.getFieldValue(ReplaceResOptionCol_int);
    				if ( !StringUtil.isInteger(ReplaceResOption) ) {
    					message = "In list file for location \"" + id + "\" ReplaceResOption (" +
    					ReplaceResOption + ") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " ReplaceResOption -> " + ReplaceResOption );
    					dds.setIreptype( StringUtil.atoi(ReplaceResOption) );
    				}
    			}
    			if ( do_diversions && (DailyIDCol_int > 0) && (!do_fill ||
    				StateMod_Util.isMissing(dds.getCdividy()))) {
    				DailyID = (String)rec.getFieldValue(DailyIDCol_int);
    				Message.printStatus ( 2, routine, Action + id + " DailyID -> " + DailyID );
    				dds.setCdividy( DailyID );
    			}
    			else if(do_wells && (DailyIDCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getCdividyw()))) {
    				DailyID = (String)rec.getFieldValue(DailyIDCol_int);
    				Message.printStatus ( 2, routine, Action + id + " DailyID -> " + DailyID );
    				wes.setCdividyw( DailyID );
    			}
    			if ( do_diversions && (UserNameCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getUsername()))) {
    				UserName = (String)rec.getFieldValue(UserNameCol_int);
    				Message.printStatus ( 2, routine, Action + id + " UserName -> " + UserName );
    				dds.setUsername( UserName );
    			}
    			if ( do_wells && (AdminNumShiftCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getPrimary()))) {
    				AdminNumShift = (String)rec.getFieldValue(AdminNumShiftCol_int);
    				if ( !StringUtil.isDouble(AdminNumShift) ) {
    					message = "In list file for location \"" + id + "\" AdminNumShift (" +
    					AdminNumShift + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " AdminNumShift -> " + AdminNumShift );
    					wes.setPrimary(	StringUtil.atod(AdminNumShift) );
    				}
    			}
    			if ( do_wells && (DiversionIDCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getIdvcow2()))) {
    				DiversionID = (String)rec.getFieldValue(DiversionIDCol_int);
    				Message.printStatus ( 2, routine, Action + id + " DiversionID -> " + DiversionID );
    				wes.setIdvcow2( DiversionID );
    			}
    			if ( do_diversions && (DemandTypeCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getIdvcom()))) {
    				DemandType = (String)rec.getFieldValue(DemandTypeCol_int);
    				if ( !StringUtil.isInteger(DemandType) ) {
    					message = "In list file for location \"" + id + "\" DemandType (" + DemandType +
    					") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " DemandType -> " + DemandType );
    					dds.setIdvcom(StringUtil.atoi(DemandType) );
    				}
    			}
    			else if(do_wells && (DemandTypeCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getIdvcomw()))) {
    				DemandType = (String)rec.getFieldValue(DemandTypeCol_int);
    				if ( !StringUtil.isDouble(DemandType) ) {
    					message = "In list file for location \"" + id + "\" DemandType (" + DemandType +
    					") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " DemandType -> " + DemandType );
    					wes.setIdvcomw ( StringUtil.atoi(DemandType) );
    				}
    			}
    			if ( do_diversions && (IrrigatedAcresCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getArea()))) {
    				IrrigatedAcres = (String)rec.getFieldValue(IrrigatedAcresCol_int);
    				if ( !StringUtil.isDouble(IrrigatedAcres) ) {
    					message = "In list file for location \"" + id + "\" IrrigatedAcres (" +
    					IrrigatedAcres + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " IrrigatedAcres -> " + IrrigatedAcres);
    					dds.setArea( StringUtil.atod(IrrigatedAcres) );
    				}
    			}
    			else if(do_wells && (IrrigatedAcresCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getAreaw()))) {
    				IrrigatedAcres = (String)rec.getFieldValue(IrrigatedAcresCol_int);
    				if ( !StringUtil.isDouble(IrrigatedAcres) ) {
    					message = "In list file for location \"" + id + "\" IrrigatedAcres (" +
    					IrrigatedAcres + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " IrrigatedAcres -> " + IrrigatedAcres);
    					wes.setAreaw( StringUtil.atod(IrrigatedAcres) );
    				}
    			}
    			if ( do_diversions && (UseTypeCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getIrturn()))) {
    				UseType = (String)rec.getFieldValue(UseTypeCol_int);
    				if ( !StringUtil.isInteger(UseType) ) {
    					message = "In list file for location \"" + id + "\" UseType (" + UseType +
    					") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " UseType -> " + UseType );
    					dds.setIrturn( StringUtil.atoi(UseType) );
    				}
    			}
    			else if(do_wells && (UseTypeCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getIrturnw()))) {
    				UseType = (String)rec.getFieldValue(UseTypeCol_int);
    				if ( !StringUtil.isDouble(UseType) ) {
    					message = "In list file for location \"" + id + "\" UseType (" + UseType +
    					") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " UseType -> " + UseType );
    					wes.setIrturnw ( StringUtil.atoi(UseType) );
    				}
    			}
    			if ( do_diversions && (DemandSourceCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getDemsrc()))) {
    				DemandSource = (String)rec.getFieldValue(DemandSourceCol_int);
    				if ( !StringUtil.isInteger(DemandSource) ) {
    					message = "In list file for location \"" + id + "\" DemandSource (" + DemandSource+
    					") is not an integer.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " DemandSource -> " + DemandSource );
    					dds.setDemsrc( StringUtil.atoi(DemandSource) );
    				}
    			}
    			else if(do_wells && (DemandSourceCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(wes.getDemsrcw()))) {
    				DemandSource = (String)rec.getFieldValue(DemandSourceCol_int);
    				if ( !StringUtil.isDouble(DemandSource) ) {
    					message = "In list file for location \"" + id + "\" DemandSource (" + DemandSource+
    					") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " DemandSource -> " + DemandSource );
    					wes.setDemsrcw ( StringUtil.atoi(DemandSource) );
    				}
    			}
    			// Set the annual value to a positive so that it will be used,
    			// and set the monthly values to the same value.
    			if ( do_diversions && (EffAnnualCol_int > 0) &&
    				(!do_fill || StateMod_Util.isMissing(dds.getDivefc()))) {
    				EffAnnual = (String)rec.getFieldValue(EffAnnualCol_int);
    				if ( !StringUtil.isDouble(EffAnnual) ) {
    					message = "In list file for location \"" + id + "\" EffAnnual (" +
    					EffAnnual + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " EffAnnual -> " + EffAnnual );
    					dds.setDivefc( StringUtil.atod(EffAnnual) );
    					for ( int ieff = 0; ieff < 12; ieff++ ) {
    						dds.setDiveff ( ieff, StringUtil.atod(EffAnnual) );
    					}
    				}
    			}
    			else if(do_wells && (EffAnnualCol_int > 0) &&
    				(!do_fill ||StateMod_Util.isMissing(wes.getDivefcw()))) {
    				EffAnnual = (String)rec.getFieldValue(EffAnnualCol_int);
    				if ( !StringUtil.isDouble(EffAnnual) ) {
    					message = "In list file for location \"" + id + "\" EffAnnual (" +
    					EffAnnual + ") is not a number.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + id + " EffAnnual -> " + EffAnnual );
    					wes.setDivefcw(	StringUtil.atod(EffAnnual) );
    					for ( int ieff = 0; ieff < 12; ieff++ ) {
    						wes.setDiveff ( ieff, StringUtil.atod(EffAnnual) );
    					}
    				}
    			}
    			// Set the monthly values as specified and set the annual efficiency to the average
    			// (as a negative so that monthly values will be used).
    			if ( (do_diversions || do_wells) && (EffMonthlyCol_int > 0) ) {
    				total = 0.0;
    				// List file values are always Jan - Dec but DDS and WES could be different.
    				int warningCount2 = 0;
    				for ( int ieff = 0; ieff < 12; ieff++){
    					if ( do_diversions ) {
    						ismissing = StateMod_Util.isMissing ( dds.getDiveff( ieff, outputYearType) );
    					}
    					else if ( do_wells ) {
    						ismissing = StateMod_Util.isMissing ( wes.getDiveff( ieff, outputYearType) );
    					}
    					if ( !do_fill || ismissing ) {
	    					try {
	    						EffMonthly = (String)rec.getFieldValue(EffMonthlyCol_int + ieff);
	    						if ( !StringUtil.isDouble(EffMonthly) ) {
	    	    					message = "In list file for location \"" + id + "\" EffMonthly (" +
	    							EffMonthly + ") is not a number.  Not "+action+".";
	    	    					Message.printWarning ( warning_level,
	    				                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
	    			                status.addToLog(command_phase,
	    			                    new CommandLogRecord( CommandStatusType.FAILURE, message,
	    			                        "Verify the format of the file."));
	    			                ++warningCount2;
	    						}
	    						else {
	    							EffMonthly_double[ieff]=StringUtil.atod(EffMonthly );
	    						}
	    					}
	    					catch ( Exception e ) {
    	    					message = "In list file for location \"" + id + "\" error processing efficiency (" +
    	    					(ieff + 1) + "), " + EffMonthly + ".  Not "+action+". (" + e + ")";
    	    					Message.printWarning ( warning_level,
    				                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
    			                status.addToLog(command_phase,
    			                    new CommandLogRecord( CommandStatusType.FAILURE, message,
    			                        "Verify the format of the file."));
    			                ++warningCount2;
	    					}
    					}
    				}
    				// Set the individual values and the total...
    				if ( warningCount2 > 0 ) {
    					message = "In list file for location \"" + id + "\", warnings occurred.  Not "+action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					for ( int ieff = 0; ieff < 12; ieff++ ) {
    						Message.printStatus ( 2,
    						routine, Action + id + " EffMonthly (" + (ieff + 1) + ") -> " +
    						StringUtil.formatString(EffMonthly_double[ieff],"%.6f"));
    						if ( do_diversions ) {
    							dds.setDiveff ( ieff,EffMonthly_double[ieff],outputYearType );
    						}
    						else if ( do_wells ) {
    							wes.setDiveff ( ieff,EffMonthly_double[ieff],outputYearType );
    						}
    						total += EffMonthly_double[ieff];
    					}
    					// Set total to a negative...
    					if ( do_diversions ) {
    						dds.setDivefc(-total/12.0);
    					}
    					else if ( do_wells ) {
    						wes.setDivefcw(-total/12.0);
    					}
    				}
    			}
    			++matchCount;
    		}
    	}

    	// If nothing was matched, take other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No identifiers were matched: warning and not " + action + ".";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No identifiers were matched: failing and not " + action + ".";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifiers are correct." ) );
			}
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error " + action + " data from list file (" + e + ").";
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
	String Delim = parameters.getValue ( "Delim" );
	String MergeDelim = parameters.getValue ( "MergeDelim" );
	// Diversion stations...
	String NameCol = parameters.getValue ( "NameCol" );
	String RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
	String OnOffCol = parameters.getValue ( "OnOffCol" );
	String CapacityCol = parameters.getValue ( "CapacityCol" );
	String ReplaceResOptionCol = parameters.getValue ( "ReplaceResOptionCol" );
	String DailyIDCol = parameters.getValue ( "DailyIDCol" );
	String UserNameCol = parameters.getValue ( "UserNameCol" );
	String DemandTypeCol = parameters.getValue ( "DemandTypeCol" );
	String IrrigatedAcresCol = parameters.getValue ( "IrrigatedAcresCol" );
	String UseTypeCol = parameters.getValue ( "UseTypeCol" );
	String DemandSourceCol = parameters.getValue ( "DemandSourceCol" );
	String EffAnnualCol = parameters.getValue ( "EffAnnualCol" );
	String EffMonthlyCol = parameters.getValue ( "EffMonthlyCol" );
	// StateMod wells...
	String DiversionIDCol = parameters.getValue ( "DiversionIDCol" );
	String AdminNumShiftCol = parameters.getValue ( "AdminNumShiftCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
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
	if ( (RiverNodeIDCol != null) && (RiverNodeIDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RiverNodeIDCol=\"" + RiverNodeIDCol + "\"" );
	}
	if ( (OnOffCol != null) && (OnOffCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffCol=\"" + OnOffCol + "\"" );
	}
	if ( (CapacityCol != null) && (CapacityCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CapacityCol=\"" + CapacityCol + "\"" );
	}
	if ((ReplaceResOptionCol != null)&&(ReplaceResOptionCol.length() > 0) ){
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReplaceResOptionCol=\"" + ReplaceResOptionCol+"\"");
	}
	if ( (DailyIDCol != null) && (DailyIDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DailyIDCol=\"" + DailyIDCol + "\"" );
	}
	// StateMod wells...
	if ( (AdminNumShiftCol != null) && (AdminNumShiftCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AdminNumShiftCol=\"" + AdminNumShiftCol + "\"" );
	}
	// StateMod wells...
	if ( (DiversionIDCol != null) && (DiversionIDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DiversionIDCol=\"" + DiversionIDCol + "\"" );
	}
	if ( (UserNameCol != null) && (UserNameCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UserNameCol=\"" + UserNameCol + "\"" );
	}
	if ( (DemandTypeCol != null) && (DemandTypeCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DemandTypeCol=\"" + DemandTypeCol + "\"" );
	}
	if ( (IrrigatedAcresCol != null) && (IrrigatedAcresCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigatedAcresCol=\"" + IrrigatedAcresCol + "\"" );
	}
	if ( (UseTypeCol != null) && (UseTypeCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseTypeCol=\"" + UseTypeCol + "\"" );
	}
	if ( (DemandSourceCol != null) && (DemandSourceCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DemandSourceCol=\"" + DemandSourceCol + "\"" );
	}
	if ( (EffAnnualCol != null) && (EffAnnualCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffAnnualCol=\"" + EffAnnualCol + "\"" );
	}
	if ( (EffMonthlyCol != null) && (EffMonthlyCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffMonthlyCol=\"" + EffMonthlyCol + "\"" );
	}
	// General at end...
	if ( (Delim != null) && (Delim.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Delim=\"" + Delim + "\"" );
	}
	if ( (MergeDelim != null) && (MergeDelim.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MergeDelim=" + MergeDelim );
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
