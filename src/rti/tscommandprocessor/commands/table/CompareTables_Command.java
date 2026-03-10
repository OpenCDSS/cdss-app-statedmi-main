// CompareTables_Command - This class initializes, checks, and runs the CompareTables() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

//import rti.tscommandprocessor.core.TSCommandProcessor;
//import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableComparer;
import RTi.Util.Table.DataTableComparerAnalysisType;

/**
This class initializes, checks, and runs the CompareTables() command.
*/
public class CompareTables_Command extends AbstractCommand
implements CommandDiscoverable, ObjectListProvider, FileGenerator
{

/**
Possible values for IfDifferent and IfSame parameters.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Possible values for MatchColumnsHow parameters.
*/
protected final String _Name = "Name";
protected final String _Order = "Order";

/**
Possible values for OutputRows parameter.
*/
protected final String _All = "All";
protected final String _Different = "Different";
protected final String _Same = "Same";

/**
The first difference table that is created.
*/
private DataTable __diffTable1 = null;

/**
The second difference table that is created.
*/
private DataTable __diffTable2 = null;

/**
The final difference table that is created.
*/
private DataTable __diffTable = null;

/**
The first difference table file that is created by this command.
*/
private File __DiffFile1_File = null;

/**
The second difference table file that is created by this command.
*/
private File __DiffFile2_File = null;

/**
The final difference table file that is created by this command.
*/
private File __DiffFile_File = null;

/**
Constructor.
*/
public CompareTables_Command () {
	super();
	setCommandName ( "CompareTables" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages,
to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String Table1ID = parameters.getValue ( "Table1ID" );
    String Table2ID = parameters.getValue ( "Table2ID" );
    String Precision = parameters.getValue ( "Precision" );
    String MatchColumnsHow = parameters.getValue ( "MatchColumnsHow" );
    String AnalysisMethod = parameters.getValue ( "AnalysisMethod" );
    String Tolerance = parameters.getValue ( "Tolerance" );
    String AllowedDiff = parameters.getValue ( "AllowedDiff" );
    String IfDifferent = parameters.getValue ( "IfDifferent" );
    String IfSame = parameters.getValue ( "IfSame" );
    // Changed as of TSTool 14.0.3.
    //String NewTableID = parameters.getValue ( "NewTableID" );
    //String NewTable2ID = parameters.getValue ( "NewTable2ID" );
    String DiffTable1ID = parameters.getValue ( "DiffTable1ID" );
    String DiffTable2ID = parameters.getValue ( "DiffTable2ID" );
    String DiffTableID = parameters.getValue ( "DiffTableID" );
    // Changed as of TSTool 14.0.3.
    //String OutputFile = parameters.getValue ( "OutputFile" );
    //String OutputFile2 = parameters.getValue ( "OutputFile2" );
    String DiffFile1 = parameters.getValue ( "DiffFile1" );
    String DiffFile2 = parameters.getValue ( "DiffFile2" );
    String DiffFile = parameters.getValue ( "DiffFile" );
    String OutputRows = parameters.getValue ( "OutputRows" );
	String warning = "";
    String message;

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (Table1ID == null) || (Table1ID.length() == 0) ) {
        message = "The first table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the first table identifier." ) );
    }

    if ( (Table2ID == null) || (Table2ID.length() == 0) ) {
        message = "The second table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the second table identifier." ) );
    }

    if ( (Table1ID != null) && (Table1ID.length() != 0) && (DiffTable1ID != null) && (DiffTable1ID.length() != 0) &&
        Table1ID.equalsIgnoreCase(DiffTable1ID) ) {
        message = "The first and first difference table identifiers are the same.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the first difference table identifier different from the first table identifier." ) );
    }

    if ( (Table2ID != null) && (Table2ID.length() != 0) && (DiffTable1ID != null) && (DiffTable1ID.length() != 0) &&
        Table2ID.equalsIgnoreCase(DiffTable1ID) ) {
        message = "The second and first difference table identifiers are the same.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the first difference table identifier different from the second table identifier." ) );
    }

    if ( (MatchColumnsHow != null) && !MatchColumnsHow.isEmpty() && !MatchColumnsHow.equalsIgnoreCase(_Name) &&
        !MatchColumnsHow.equalsIgnoreCase(_Order) ) {
            message = "The MatchColumnsHow parameter \"" + MatchColumnsHow + "\" is not a valid value.";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the parameter as " + _Name + " (default) or " + _Order + "."));
    }

   	DataTableComparerAnalysisType analysisType = DataTableComparerAnalysisType.SIMPLE; // Default.
    if ( (AnalysisMethod != null) && !AnalysisMethod.isEmpty() ) {
    	analysisType = DataTableComparerAnalysisType.valueOfIgnoreCase(AnalysisMethod);
    	if ( analysisType == null ) {
    		message = "The AnalysisMethod parameter \"" + AnalysisMethod + "\" is not a valid value.";
    		warning += "\n" + message;
    		status.addToLog(CommandPhaseType.INITIALIZATION,
   				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the analysis method as " + DataTableComparerAnalysisType.ADVANCED + " or " +
						DataTableComparerAnalysisType.SIMPLE + " (default)."));
    	}
    }

    if ( analysisType == DataTableComparerAnalysisType.SIMPLE ) {
   		// Analysis method was not specified:
   		// - NewTable2ID should not be specified
   		// - DiffTableID should not be specified
   		if ( (DiffTable2ID != null) && !DiffTable2ID.isEmpty() ) {
   			message = "The DiffTable2ID parameter should not be specified with AnalysisMethod=" +
   				DataTableComparerAnalysisType.SIMPLE + ".";
   			warning += "\n" + message;
   			status.addToLog(CommandPhaseType.INITIALIZATION,
  					new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify DiffTable2ID or change the analysis method to " +
						DataTableComparerAnalysisType.ADVANCED + "." ));
   		}
   		if ( (DiffFile2 != null) && !DiffFile2.isEmpty() ) {
   			message = "The DiffFile2 parameter should not be specified with AnalysisMethod=" +
   				DataTableComparerAnalysisType.SIMPLE + ".";
   			warning += "\n" + message;
   			status.addToLog(CommandPhaseType.INITIALIZATION,
  					new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify DiffFile2 or change the analysis method to " +
						DataTableComparerAnalysisType.ADVANCED + "." ));
   		}
   		if ( (DiffTableID != null) && !DiffTableID.isEmpty() ) {
   			message = "The DiffTableID parameter should not be specified with AnalysisMethod=" +
   				DataTableComparerAnalysisType.SIMPLE + ".";
   			warning += "\n" + message;
   			status.addToLog(CommandPhaseType.INITIALIZATION,
  					new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify DiffTableID or change the analysis method to " +
						DataTableComparerAnalysisType.ADVANCED + "." ));
   		}
   		if ( (DiffFile != null) && !DiffFile.isEmpty() ) {
   			message = "The DiffFile parameter should not be specified with AnalysisMethod=" +
   				DataTableComparerAnalysisType.SIMPLE + ".";
   			warning += "\n" + message;
   			status.addToLog(CommandPhaseType.INITIALIZATION,
  					new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify DiffFile or change the analysis method to " +
						DataTableComparerAnalysisType.ADVANCED + "." ));
   		}
   	}

    if ( (Precision != null) && !Precision.equals("") ) {
        if ( !StringUtil.isInteger(Precision) ) {
            message = "The precision: \"" + Precision + "\" is not an integer.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the precision as an integer (or blank to not round)." ) );
        }
        if ( StringUtil.atoi(Precision) < 0 ) {
            message = "The precision: \"" + Precision + "\" must be >= 0.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the precision as an integer >= 0 (or blank to not round)." ) );
        }
    }

    if ( (Tolerance != null) && !Tolerance.equals("") ) {
        if ( !StringUtil.isDouble(Tolerance) ) {
            message = "The tolerance: \"" + Tolerance + "\" is not a number.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the tolerance as a number." ) );
        }
        if ( StringUtil.atod(Tolerance) < 0.0 ) {
            message = "The tolerance: \"" + Tolerance + "\" must be >= 0.0.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the tolerance as a number >= 0.0." ) );
        }
    }

    if ( (AllowedDiff != null) && !AllowedDiff.equals("") && !StringUtil.isInteger(AllowedDiff) ) {
        message = "The number of allowed differences \"" + AllowedDiff + "\" is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                 message, "Specify the parameter as an integer."));
    }

    if ( (DiffFile1 != null) && (DiffFile1.length() > 0) && (DiffFile1.indexOf("${") < 0) ) {
        try {
        	processor.getPropContents ( "WorkingDir" );
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Software error - report problem to support." ) );
        }
    }
    if ( (DiffFile2 != null) && (DiffFile2.length() > 0) && (DiffFile2.indexOf("${") < 0) ) {
        try {
        	processor.getPropContents ( "WorkingDir" );
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Software error - report problem to support." ) );
        }
    }

    if ( (DiffFile != null) && (DiffFile.length() > 0) && (DiffFile.indexOf("${") < 0) ) {
        try {
        	processor.getPropContents ( "WorkingDir" );
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Software error - report problem to support." ) );
        }

        // The DiffTable must be specified because no default is created.

   		if ( (DiffTableID == null) || DiffTableID.isEmpty() ) {
   			message = "The DiffTableID must be specified if the difference file (DiffFile) parameter is specified.";
   			warning += "\n" + message;
   			status.addToLog(CommandPhaseType.INITIALIZATION,
  					new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify DiffTableID or remove the difference file parameter." ));
   		}
    }

    if ( (IfDifferent != null) && !IfDifferent.equals("") && !IfDifferent.equalsIgnoreCase(_Ignore) &&
        !IfDifferent.equalsIgnoreCase(_Warn) && !IfDifferent.equalsIgnoreCase(_Fail) ) {
        message = "The IfDifferent parameter \"" + IfDifferent + "\" is not a valid value.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _Ignore + " (default), " +
                _Warn + ", or " + _Fail + "."));
    }
    if ( (IfSame != null) && !IfSame.equals("") && !IfSame.equalsIgnoreCase(_Ignore) &&
        !IfSame.equalsIgnoreCase(_Warn) && !IfSame.equalsIgnoreCase(_Fail) ) {
        message = "The IfSame parameter \"" + IfSame + "\" is not a valid value.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _Ignore + " (default), " +
                _Warn + ", or " + _Fail + "."));
    }

    if ( (OutputRows != null) && !OutputRows.equals("") && !OutputRows.equalsIgnoreCase(_All) &&
        !OutputRows.equalsIgnoreCase(_Different) && !OutputRows.equalsIgnoreCase(_Same) ) {
        message = "The OutputRows parameter \"" + OutputRows + "\" is not a valid value.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _All + " (default), " +
                _Different + ", or " + _Same + "."));
    }

	// Check for invalid parameters.
	List<String> validList = new ArrayList<>(27);
    validList.add ( "Table1ID" );
    validList.add ( "Table2ID" );
    validList.add ( "CompareColumns1" );
    validList.add ( "ExcludeColumns1" );
    validList.add ( "MatchColumns1" );
    validList.add ( "CompareColumns2" );
    validList.add ( "ExcludeColumns2" );
    validList.add ( "MatchColumns2" );
    validList.add ( "MatchColumnsHow" );
    validList.add ( "AnalysisMethod" );
    validList.add ( "Precision" );
    validList.add ( "Tolerance" );
    validList.add ( "AllowedDiff" );
    validList.add ( "IfDifferent" );
    validList.add ( "IfSame" );
    validList.add ( "DiffTable1ID" );
    validList.add ( "DiffTable2ID" );
    validList.add ( "DiffTableID" );
    validList.add ( "RowNumberColumn" );
    validList.add ( "DiffFile1" );
    validList.add ( "DiffFile2" );
    validList.add ( "DiffFile" );
    validList.add ( "OutputRows" );
    validList.add ( "DiffRowCountProperty" );
    validList.add ( "DiffCellCountProperty" );
    validList.add ( "SameRowCountProperty" );
    validList.add ( "SameCellCountProperty" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
		throw new InvalidCommandParameterException ( warning );
	}

    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent ) {
	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed.
	return (new CompareTables_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Return the first difference table that is created by this class when run in discovery mode.
@return the first difference table that is created by this class when run in discovery mode
*/
private DataTable getDiscoveryTable1() {
    return __diffTable1;
}

/**
Return the second difference table that is created by this class when run in discovery mode.
@return the second difference table that is created by this class when run in discovery mode
*/
private DataTable getDiscoveryTable2() {
    return __diffTable2;
}

/**
Return the final difference table that is created by this class when run in discovery mode.
@return the final difference table that is created by this class when run in discovery mode
*/
private DataTable getDiscoveryTable3() {
    return __diffTable;
}

/**
Return the list of files that were created by this command.
@return the list of files that were created by this command
*/
public List<File> getGeneratedFileList () {
    List<File> list = new ArrayList<>();
    if ( getDiffFile1() != null ) {
        list.add ( getDiffFile1() );
    }
    if ( getDiffFile2() != null ) {
        list.add ( getDiffFile2() );
    }
    if ( getDiffFile() != null ) {
        list.add ( getDiffFile() );
    }
    return list;
}

/**
Return the first difference file generated by this file.  This method is used internally.
@return the first difference file generated by this file.
*/
private File getDiffFile1 () {
    return __DiffFile1_File;
}

/**
Return the second difference file generated by this file.  This method is used internally.
@return the second difference file generated by this file.
*/
private File getDiffFile2 () {
    return __DiffFile2_File;
}

/**
Return the final difference output file generated by this file.  This method is used internally.
@return the final difference output file generated by this file.
*/
private File getDiffFile () {
    return __DiffFile_File;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
@param class to match (DataTable).
@return a list of objects of the requested type.
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c ) {
    List<T> v = null;
    DataTable table = getDiscoveryTable1();
    if ( (table != null) && (c == table.getClass()) ) {
        v = new ArrayList<>();
        v.add ( (T)table );
    }
    table = getDiscoveryTable2();
    if ( (table != null) && (c == table.getClass()) ) {
        v = new ArrayList<>();
        v.add ( (T)table );
    }
    table = getDiscoveryTable3();
    if ( (table != null) && (c == table.getClass()) ) {
        v = new ArrayList<>();
        v.add ( (T)table );
    }
    return v;
}

/**
Parse the command string into a PropList of parameters, used to translate old parameters to new.
@param commandString A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void parseCommand ( String commandString )
throws InvalidCommandSyntaxException, InvalidCommandParameterException {
	// Parse the string in the parent code.
	super.parseCommand(commandString);
	String routine = getClass().getSimpleName() + ".parseCommand";

	//int warning_level = 2;
	//String routine = getClass().getSimpleName() + ".parseCommand";
	String message;

    // Translate old parameters to new equivalents.

	CommandStatus status = getCommandStatus();
	PropList parameters = getCommandParameters();
	String propValue = parameters.getValue("NewTableID");
	if ( (propValue != null) && !propValue.isEmpty() ) {
		// Convert NewTableID to DiffTable1ID.
		parameters.unSet("NewTableID");
		parameters.set("DiffTable1ID", propValue);
		parameters.setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
		message = "Automatically updated old parameter NewTableID to current syntax DiffTable1ID: " + commandString;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.INFO, message,
				"The new syntax should be used with TSTool 14.9.3 and later." ) );
		Message.printStatus(2, routine, message);
	}
	propValue = parameters.getValue("NewTable2ID");
	if ( (propValue != null) && !propValue.isEmpty() ) {
		// Convert NewTable2ID to DiffTable2ID.
		parameters.unSet("NewTable2ID");
		parameters.set("DiffTable2ID", propValue);
		parameters.setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
		message = "Automatically updated old parameter NewTable2ID to current syntax DiffTable2ID: " + commandString;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.INFO, message,
				"The new syntax should be used with TSTool 14.9.3 and later." ) );
		Message.printStatus(2, routine, message);
	}
	propValue = parameters.getValue("OutputFile");
	if ( (propValue != null) && !propValue.isEmpty() ) {
		// Convert OutputFile to DiffFile1.
		parameters.unSet("OutputFile");
		parameters.set("DiffFile1", propValue);
		parameters.setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
		message = "Automatically updated old parameter OutputFile to current syntax DiffFile1: " + commandString;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.INFO, message,
				"The new syntax should be used with TSTool 14.9.3 and later." ) );
		Message.printStatus(2, routine, message);
	}
	propValue = parameters.getValue("OutputFile2");
	if ( (propValue != null) && !propValue.isEmpty() ) {
		// Convert OutputFile2 to DiffFile2.
		parameters.unSet("OutputFile2");
		parameters.set("DiffFile2", propValue);
		parameters.setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
		message = "Automatically updated old parameter OutputFile2 to current syntax DiffFile2: " + commandString;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.INFO, message,
				"The new syntax should be used with TSTool 14.9.3 and later." ) );
		Message.printStatus(2, routine, message);
	}
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String routine = getClass().getSimpleName() + ".runCommandInternal", message = "";
	int warning_level = 2;
	int log_level = 3; // Level for non-user messages for log file.
	String command_tag = "" + command_number;
	int warning_count = 0;

    // Clear the output files.
    setDiffFile1 ( null );
    setDiffFile2 ( null );
    setDiffFile ( null );

    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable1 ( null );
        setDiscoveryTable2 ( null );
        setDiscoveryTable3 ( null );
    }

	// Make sure there are time series available to operate on.

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String Table1ID = parameters.getValue ( "Table1ID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		Table1ID = TSCommandProcessorUtil.expandParameterValue(processor, this, Table1ID);
    }
    String Table2ID = parameters.getValue ( "Table2ID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		Table2ID = TSCommandProcessorUtil.expandParameterValue(processor, this, Table2ID);
    }
    String CompareColumns1 = parameters.getValue ( "CompareColumns1" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		CompareColumns1 = TSCommandProcessorUtil.expandParameterValue(processor, this, CompareColumns1);
    }
    String ExcludeColumns1 = parameters.getValue ( "ExcludeColumns1" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		ExcludeColumns1 = TSCommandProcessorUtil.expandParameterValue(processor, this, ExcludeColumns1);
    }
    String MatchColumns1 = parameters.getValue ( "MatchColumns1" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		MatchColumns1 = TSCommandProcessorUtil.expandParameterValue(processor, this, MatchColumns1);
    }
    if ( ((MatchColumns1 == null) || MatchColumns1.isEmpty()) && ((CompareColumns1 != null) && !CompareColumns1.isEmpty()) ) {
    	// Only used with advanced analysis:
    	// - also need to set at runtime because CompareColumns1 defaults to table columns
    	MatchColumns1 = CompareColumns1;
    }
    String CompareColumns2 = parameters.getValue ( "CompareColumns2" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		CompareColumns2 = TSCommandProcessorUtil.expandParameterValue(processor, this, CompareColumns2);
    }
    String ExcludeColumns2 = parameters.getValue ( "ExcludeColumns2" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		ExcludeColumns2 = TSCommandProcessorUtil.expandParameterValue(processor, this, ExcludeColumns2);
    }
    String MatchColumns2 = parameters.getValue ( "MatchColumns2" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		MatchColumns2 = TSCommandProcessorUtil.expandParameterValue(processor, this, MatchColumns2);
    }
    if ( (MatchColumns2 == null) || (MatchColumns2.isEmpty()) ) {
    	// Set to the same as MatchColumns1.
    	MatchColumns2 = MatchColumns1;
    }
    String MatchColumnsHow = parameters.getValue ( "MatchColumnsHow" );
    boolean matchColumnsByName = true;
    if ( (MatchColumnsHow != null) && MatchColumnsHow.equalsIgnoreCase(_Order) ) {
        matchColumnsByName = false;
    }
    String AnalysisMethod = parameters.getValue ( "AnalysisMethod" );
    DataTableComparerAnalysisType analysisType = DataTableComparerAnalysisType.SIMPLE; // Default.
    if ( (AnalysisMethod != null) && !AnalysisMethod.isEmpty() ) {
    	analysisType = DataTableComparerAnalysisType.valueOfIgnoreCase(AnalysisMethod);
    }
    String Precision = parameters.getValue ( "Precision" );
    Integer Precision_Integer = null;
    if ( (Precision != null) && !Precision.equals("") ) {
        Precision_Integer = Integer.valueOf ( Precision );
    }
    String Tolerance = parameters.getValue ( "Tolerance" );
    Double Tolerance_Double = null;
    if ( (Tolerance != null) && !Tolerance.equals("") ) {
        Tolerance_Double = Double.valueOf ( Tolerance );
    }
    String AllowedDiff = parameters.getValue ( "AllowedDiff" );
    int AllowedDiff_int = 0;
    if ( StringUtil.isInteger(AllowedDiff) ) {
        AllowedDiff_int = Integer.parseInt(AllowedDiff);
    }
    String IfDifferent = parameters.getValue ( "IfDifferent" );
    CommandStatusType IfDifferent_CommandStatusType = CommandStatusType.UNKNOWN;
    if ( (IfDifferent == null) || IfDifferent.equals("") ) {
        IfDifferent = _Ignore; // Default.
    }
    else {
        if ( !IfDifferent.equalsIgnoreCase(_Ignore) ) {
            IfDifferent_CommandStatusType = CommandStatusType.parse(IfDifferent);
        }
    }
    String IfSame = parameters.getValue ( "IfSame" );
    CommandStatusType IfSame_CommandStatusType = CommandStatusType.UNKNOWN;
    if ( (IfSame == null) || IfSame.equals("") ) {
        IfSame = _Ignore; // Default.
    }
    else {
        if ( !IfSame.equalsIgnoreCase(_Ignore) ) {
            IfSame_CommandStatusType = CommandStatusType.parse(IfSame);
        }
    }
    // Number of comparison tables for output to the processor:
    // - always default to 1
    // - if a second table is specified by the user, then two tables are output to the processor
    // - two comparison output tables allows reviewing the differences from each table's perspective
    // - one comparison output table merges the differences into one difference table
    String DiffTable1ID = parameters.getValue ( "DiffTable1ID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	DiffTable1ID = TSCommandProcessorUtil.expandParameterValue(processor, this, DiffTable1ID);
    }
    String diffTable1ID = null;
    if ( analysisType == DataTableComparerAnalysisType.ADVANCED ) {
    	diffTable1ID = Table1ID + "-" + Table2ID + "-diff1"; // Default for Advanced method.
    }
    else {
    	diffTable1ID = Table1ID + "-" + Table2ID + "-diff1"; // Default for Simple method.
    }
    if ( (DiffTable1ID != null) && !DiffTable1ID.isEmpty() ) {
        diffTable1ID = DiffTable1ID;
    }
    String DiffTable2ID = parameters.getValue ( "DiffTable2ID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	DiffTable2ID = TSCommandProcessorUtil.expandParameterValue(processor, this, DiffTable2ID);
    }
    String diffTable2ID = Table1ID + "-" + Table2ID + "-diff2"; // Default.
    if ( (DiffTable2ID != null) && !DiffTable2ID.isEmpty() ) {
        diffTable2ID = DiffTable2ID;
    }
    String DiffTableID = parameters.getValue ( "DiffTableID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	DiffTableID = TSCommandProcessorUtil.expandParameterValue(processor, this, DiffTableID);
    }
    String RowNumberColumn = parameters.getValue ( "RowNumberColumn" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	RowNumberColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, RowNumberColumn);
    }
    String DiffFile1 = parameters.getValue ( "DiffFile1" ); // Expanded below.
    if ( (DiffFile1 != null) && DiffFile1.isEmpty() ) {
        DiffFile1 = null; // Easier for checks below.
    }
    String DiffFile2 = parameters.getValue ( "DiffFile2" ); // Expanded below.
    if ( (DiffFile2 != null) && DiffFile2.isEmpty() ) {
        DiffFile2 = null; // Easier for checks below.
    }
    String DiffFile = parameters.getValue ( "DiffFile" ); // Expanded below.
    if ( (DiffFile != null) && DiffFile.isEmpty() ) {
        DiffFile = null; // Easier for checks below.
    }
    String OutputRows = parameters.getValue ( "OutputRows" );
    if ( (OutputRows == null) || OutputRows.isEmpty() ) {
        OutputRows = this._All; // Default.
    }
    String DiffRowCountProperty = parameters.getValue ( "DiffRowCountProperty" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	DiffRowCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, DiffRowCountProperty);
    }
    String DiffCellCountProperty = parameters.getValue ( "DiffCellCountProperty" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	DiffCellCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, DiffCellCountProperty);
    }
    String SameRowCountProperty = parameters.getValue ( "SameRowCountProperty" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	SameRowCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, SameRowCountProperty);
    }
    String SameCellCountProperty = parameters.getValue ( "SameCellCountProperty" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	SameCellCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, SameCellCountProperty);
    }

    String [] compareColumns1 = null;
    if ( (CompareColumns1 != null) && !CompareColumns1.equals("") ) {
        compareColumns1 = CompareColumns1.split(",");
        for ( int i = 0; i < compareColumns1.length; i++ ) {
            compareColumns1[i] = compareColumns1[i].trim();
        }
    }
    String [] excludeColumns1 = null;
    if ( (ExcludeColumns1 != null) && !ExcludeColumns1.equals("") ) {
        excludeColumns1 = ExcludeColumns1.split(",");
        for ( int i = 0; i < excludeColumns1.length; i++ ) {
            excludeColumns1[i] = excludeColumns1[i].trim();
        }
    }
    String [] matchColumns1 = null;
    if ( (MatchColumns1 != null) && !MatchColumns1.equals("") ) {
        matchColumns1 = MatchColumns1.split(",");
        for ( int i = 0; i < matchColumns1.length; i++ ) {
            matchColumns1[i] = matchColumns1[i].trim();
        }
    }
    String [] compareColumns2 = null;
    if ( (CompareColumns2 != null) && !CompareColumns2.equals("") ) {
        compareColumns2 = CompareColumns2.split(",");
        for ( int i = 0; i < compareColumns2.length; i++ ) {
            compareColumns2[i] = compareColumns2[i].trim();
        }
    }
    String [] excludeColumns2 = null;
    if ( (ExcludeColumns2 != null) && !ExcludeColumns2.equals("") ) {
        excludeColumns2 = ExcludeColumns2.split(",");
        for ( int i = 0; i < excludeColumns2.length; i++ ) {
            excludeColumns2[i] = excludeColumns2[i].trim();
        }
    }
    String [] matchColumns2 = null;
    if ( (MatchColumns2 != null) && !MatchColumns2.equals("") ) {
        matchColumns2 = MatchColumns2.split(",");
        for ( int i = 0; i < matchColumns2.length; i++ ) {
            matchColumns2[i] = matchColumns2[i].trim();
        }
    }

    // Get the table to process.

    DataTable table1 = null;
    int table1RowCount = 0;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (Table1ID != null) && !Table1ID.isEmpty() ) {
            // Get the table to be updated.
            request_params = new PropList ( "" );
            request_params.set ( "TableID", Table1ID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + Table1ID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + Table1ID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
                table1 = (DataTable)o_Table;
                table1RowCount = table1.getNumberOfRecords();
            }
        }
    }

    DataTable table2 = null;
    int table2RowCount = 0;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (Table2ID != null) && !Table2ID.equals("") ) {
            // Get the table to be updated.
            request_params = new PropList ( "" );
            request_params.set ( "TableID", Table2ID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + Table2ID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + Table2ID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
                table2 = (DataTable)o_Table;
                table2RowCount = table2.getNumberOfRecords();
            }
        }
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

    int diffCount = 0; // For final warning check.
    int diffRowCount = 0; // For setting properties.
    int diffCellCount = 0; // For setting properties.
    int sameRowCount = 0; // For setting properties.
    int sameCellCount = 0; // For setting properties.
    int errorCount = 0; // Count of errors doing the comparison.
    int table1CellCount = 0;
	try {
    	// Create the table.
	    String DiffFile1_full = DiffFile1;
	    String DiffFile2_full = DiffFile2;
	    String DiffFile_full = DiffFile;
	    if ( commandPhase == CommandPhaseType.RUN ) {
	        if ( DiffFile1 != null ) {
    	        DiffFile1_full = IOUtil.verifyPathForOS(
    	            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
    	            	TSCommandProcessorUtil.expandParameterValue(processor, this, DiffFile1)));
	        }
	        if ( DiffFile2 != null ) {
    	        DiffFile2_full = IOUtil.verifyPathForOS(
    	            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
    	            	TSCommandProcessorUtil.expandParameterValue(processor, this, DiffFile2)));
	        }
	        if ( DiffFile != null ) {
    	        DiffFile_full = IOUtil.verifyPathForOS(
    	            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
    	            	TSCommandProcessorUtil.expandParameterValue(processor, this, DiffFile)));
	        }
	        DataTableComparer comparer = new DataTableComparer (
	        	table1,
	            StringUtil.toList(compareColumns1), StringUtil.toList(excludeColumns1),
	            StringUtil.toList(matchColumns1),
	            table2,
	            StringUtil.toList(compareColumns2), StringUtil.toList(excludeColumns2),
	            StringUtil.toList(matchColumns2),
	            matchColumnsByName,
	            analysisType,
	            Precision_Integer, Tolerance_Double, diffTable1ID, diffTable2ID,
	            DiffTableID,
	            RowNumberColumn,
	            OutputRows );
	        comparer.compare ();
	        DataTable diffTable1 = comparer.getDiffTable1();
	        DataTable diffTable2 = comparer.getDiffTable2();
	        DataTable diffTable = comparer.getDiffTable();
	        table1CellCount = table1.getNumberOfRecords()*table1.getNumberOfFields();
           	if ( (compareColumns1 != null) && compareColumns1.length > 0 ) {
           		table1CellCount = table1.getNumberOfRecords()*compareColumns1.length;
           	}
	        diffCount = comparer.getDifferenceCount();
	        diffRowCount = comparer.getDifferentRowCount();
	        diffCellCount = comparer.getDifferentCellCount();
	        sameRowCount = comparer.getSameRowCount();
	        sameCellCount = comparer.getSameCellCount();
	        errorCount = comparer.getErrorCount();

	        // If an output file is desired, write to it and save the name.
	        if ( (DiffFile1 != null) && (diffTable1 != null) ) {
	            comparer.writeHtmlDiffFile1 ( DiffFile1_full );
	            setDiffFile1 ( new File(DiffFile1_full) );
	        }

	        // If a second output file is desired, write to it and save the name.
	        if ( (DiffFile2 != null) && (diffTable2 != null) ) {
	            comparer.writeHtmlDiffFile2 ( DiffFile2_full );
	            setDiffFile2 ( new File(DiffFile2_full) );
	        }

	        // If a final output file is desired, write to it and save the name.
	        if ( (DiffFile != null) && (diffTable != null) ) {
	            comparer.writeHtmlDiffFile ( DiffFile_full );
	            setDiffFile ( new File(DiffFile_full) );
	        }

            // Set the comparison table(s) in the processor if the user has specific a name
	        // (otherwise the table is used internally, for example to create the HTML file).

	        if ( (diffTable1 != null) && (DiffTable1ID != null) && !DiffTable1ID.isEmpty() ) {
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", diffTable1 );
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
	        }

	        if ( (diffTable2 != null) && (DiffTable2ID != null) && !DiffTable2ID.isEmpty() ) {
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", diffTable2 );
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
	        }

	        if ( (diffTable != null) && (DiffTableID != null) && !DiffTableID.isEmpty() ) {
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", diffTable );
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
	        }

	        // Add problems to the status:
	        // - show as warnings since an exception would have been generated if a major problem
	        List<String> problems = comparer.getProblems();
	        int problemCount = 0;
	        for ( String problem : problems ) {
	        	++problemCount;
                status.addToLog ( commandPhase,
                    new CommandLogRecord(CommandStatusType.WARNING,
                         problem, "See the log file." ) );
                if ( problemCount > 200 ) {
                	status.addToLog ( commandPhase,
                    	new CommandLogRecord(CommandStatusType.WARNING,
                         	"Limiting problem output to 200 messages.", "See the log file." ) );
                	break;
                }
	        }
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            if ( (DiffTable1ID != null) && !DiffTable1ID.isEmpty() ) {
                // Create an empty table and set the ID.
                DataTable diffTable1 = new DataTable();
                diffTable1.setTableID ( DiffTable1ID );
                setDiscoveryTable1 ( diffTable1 );
                Message.printStatus(2,routine,"Setting discovery table 1" + diffTable1.getTableID() );
            }
            if ( (DiffTable2ID != null) && !DiffTable2ID.isEmpty() ) {
                // Create an empty table and set the ID.
                DataTable diffTable2 = new DataTable();
                diffTable2.setTableID ( DiffTable2ID );
                setDiscoveryTable2 ( diffTable2 );
                Message.printStatus(2,routine,"Setting discovery table 2 " + diffTable2.getTableID() );
            }
            if ( (DiffTableID != null) && !DiffTableID.isEmpty() ) {
                // Create an empty table and set the ID.
                DataTable diffTable = new DataTable();
                diffTable.setTableID ( DiffTableID );
                setDiscoveryTable3 ( diffTable );
                Message.printStatus(2,routine,"Setting discovery table " + diffTable.getTableID() );
            }
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error comparing tables (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
	}
	int allowedDiffPositive = AllowedDiff_int;
	if ( AllowedDiff_int < 0 ) {
	    allowedDiffPositive = AllowedDiff_int;
	}
	if ( diffCount > 0 ) {
	    // Have some differences - action is as per user request.
        message = "" + diffCount + " (" +
        	StringUtil.formatString(100.0*(double)diffCount/(double)table1CellCount, "%.2f")
        	+ " %) out of " + table1CellCount + " table 1 values had differences.";
        Message.printStatus ( 2, routine, message );
	    boolean needToNotify = false;
	    if ( (AllowedDiff_int < 0) && (diffCount > allowedDiffPositive) ) {
	        needToNotify = true; // Too many differences (not expected).
	    }
	    else if ( (AllowedDiff_int >= 0) && (diffCount != AllowedDiff_int) ) {
	        needToNotify = true; // Does not match exact number of differences.
	    }
        if ( needToNotify &&
           ((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
           (IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
            // Have differences and need to warn.
            Message.printWarning ( warning_level,
            	MessageUtil.formatMessageTag( command_tag,++warning_count),
            	routine, message );
            status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(IfDifferent_CommandStatusType,
                    message, "Check tables because difference is not expected.") );

            // Also show the row and cell differences, which may make it easier to understand.
            if ( diffRowCount > 0 ) {
            	message = "" + diffRowCount + " (" +
            	StringUtil.formatString(100.0*(double)diffRowCount/(double)table1.getNumberOfRecords(), "%.2f")
            	+ "%) out of " + table1.getNumberOfRecords() + " table 1 rows had differences.";
            	Message.printWarning ( warning_level,
            		MessageUtil.formatMessageTag( command_tag,++warning_count),
            		routine, message );
            	status.addToLog(CommandPhaseType.RUN,
                	new CommandLogRecord(IfDifferent_CommandStatusType,
                    	message, "Check tables because difference is not expected.") );
            }

            if ( diffCellCount > 0 ) {
            	message = "" + diffCellCount + " (" +
            	StringUtil.formatString(100.0*(double)diffCellCount/(double)table1CellCount, "%.2f")
            	+ "%) out of " + table1CellCount + " table 1 cells had differences.";
            	Message.printWarning ( warning_level,
            		MessageUtil.formatMessageTag( command_tag,++warning_count),
            		routine, message );
            	status.addToLog(CommandPhaseType.RUN,
                	new CommandLogRecord(IfDifferent_CommandStatusType,
                    	message, "Check tables because difference is not expected.") );
            }
        }
    }

    // Set the property indicating the number of different rows.
    if ( (DiffRowCountProperty != null) && !DiffRowCountProperty.isEmpty() ) {
        PropList request_params = new PropList ( "" );
        request_params.setUsingObject ( "PropertyName", DiffRowCountProperty );
        request_params.setUsingObject ( "PropertyValue", Integer.valueOf(diffRowCount) );
        try {
            processor.processRequest( "SetProperty", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetProperty(Property=\"" + DiffRowCountProperty + "\") from processor.";
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
        }
    }

    // Set the property indicating the number of different cells.
    if ( (DiffCellCountProperty != null) && !DiffCellCountProperty.isEmpty() ) {
        PropList request_params = new PropList ( "" );
        request_params.setUsingObject ( "PropertyName", DiffCellCountProperty );
        request_params.setUsingObject ( "PropertyValue", Integer.valueOf(diffCellCount) );
        try {
            processor.processRequest( "SetProperty", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetProperty(Property=\"" + DiffCellCountProperty + "\") from processor.";
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
        }
    }

    // Set the property indicating the number of same rows.
    if ( (SameRowCountProperty != null) && !SameRowCountProperty.isEmpty() ) {
        PropList request_params = new PropList ( "" );
        request_params.setUsingObject ( "PropertyName", SameRowCountProperty );
        request_params.setUsingObject ( "PropertyValue", Integer.valueOf(sameRowCount) );
        try {
            processor.processRequest( "SetProperty", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetProperty(Property=\"" + SameRowCountProperty + "\") from processor.";
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
        }
    }

    // Set the property indicating the number of same cells.
    if ( (SameCellCountProperty != null) && !SameCellCountProperty.isEmpty() ) {
        PropList request_params = new PropList ( "" );
        request_params.setUsingObject ( "PropertyName", SameCellCountProperty );
        request_params.setUsingObject ( "PropertyValue", Integer.valueOf(sameCellCount) );
        try {
            processor.processRequest( "SetProperty", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetProperty(Property=\"" + SameCellCountProperty + "\") from processor.";
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
        }
    }

	boolean tablesDiffSize = false;
	// Also check for different table sizes, which may not be fully handled in the above comparison.
	if ( (table1RowCount == 0) && (table2RowCount != 0) ) {
		tablesDiffSize = true;
		if (
			((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
			(IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
			// Have differences and need to warn:
			// - do not allow any differences
			message = "First table is empty and second is not.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag( command_tag,++warning_count),
				routine, message );
			status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfDifferent_CommandStatusType,
					message, "Check tables because difference is not expected.") );
		}
	}
	else if ( (table2RowCount == 0) && (table1RowCount != 0) ) {
		tablesDiffSize = true;
		if (
			((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
			(IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
			// Have differences and need to warn:
			// - do not allow any differences
			message = "Second table is empty and first is not.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag( command_tag,++warning_count),
				routine, message );
			status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfDifferent_CommandStatusType,
					message, "Check tables because difference is not expected.") );
		}
	}
	else if ( (analysisType == DataTableComparerAnalysisType.SIMPLE) && (table2RowCount != table1RowCount) ) {
		tablesDiffSize = true;
		if (
			((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
			(IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
			// Have differences and need to warn:
			// - do not allow any differences
			message = "First table has " + table1RowCount + " rows and second table has " + table2RowCount + " rows.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag( command_tag,++warning_count),
				routine, message );
			status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfDifferent_CommandStatusType,
					message, "Check tables because difference is not expected for simple analysis.") );
		}
	}
	if ( (diffCount == 0) && !tablesDiffSize ) {
		// No differences were reported.
		message = "No table values were different (the tables are the same).";
		Message.printStatus ( 2, routine, message );
		if ( (IfSame_CommandStatusType == CommandStatusType.WARNING) ||
			(IfSame_CommandStatusType == CommandStatusType.FAILURE)) {
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag( command_tag,++warning_count),
				routine, message );
			status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfSame_CommandStatusType,
					message, "Check tables because match is not expected.") );
		}
	}
	if ( errorCount > 0 ) {
		// Error comparing the tables, usually due to a software problem.
		message = "There were " + errorCount + " errors performaing the comparison.";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag( command_tag,++warning_count),
        routine, message );
        status.addToLog(CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Contact software support.") );
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the first difference table that is created by this class in discovery mode.
@param table data table for discovery mode
*/
private void setDiscoveryTable1 ( DataTable table ) {
    this.__diffTable1 = table;
}

/**
Set the second difference table that is created by this class in discovery mode.
@param table data table for discovery mode
*/
private void setDiscoveryTable2 ( DataTable table ) {
    this.__diffTable2 = table;
}

/**
Set the final difference table that is created by this class in discovery mode.
@param table data table for discovery mode
*/
private void setDiscoveryTable3 ( DataTable table ) {
    this.__diffTable = table;
}

/**
Set the first difference file that is created by this command.
@param file output file for discovery mode
*/
private void setDiffFile1 ( File file ) {
    this.__DiffFile1_File = file;
}

/**
Set the first difference output file that is created by this command.
@param file output file for discovery mode
*/
private void setDiffFile2 ( File file ) {
    this.__DiffFile2_File = file;
}

/**
Set the final difference output file that is created by this command.
@param file output file for discovery mode
*/
private void setDiffFile ( File file ) {
    this.__DiffFile_File = file;
}

/**
Return the string representation of the command.
@param parameters to include in the command
@return the string representation of the command
*/
public String toString ( PropList parameters ) {
	String [] parameterOrder = {
    	"Table1ID",
		"CompareColumns1",
		"ExcludeColumns1",
		"MatchColumns1",
    	"Table2ID",
		"CompareColumns2",
		"ExcludeColumns2",
		"MatchColumns2",
		"MatchColumnsHow",
    	"AnalysisMethod",
    	"Precision",
    	"Tolerance",
    	"AllowedDiff",
    	"IfDifferent",
    	"IfSame",
    	// Changed in TSTool 14.9.3.
    	//"NewTableID",
    	//"NewTable2ID",
    	"DiffTable1ID",
    	"DiffTable2ID",
    	"DiffTableID",
    	"RowNumberColumn",
    	// Changed in TSTool 14.9.3.
    	//"OutputFile",
    	//"OutputFile2",
    	"DiffFile1",
    	"DiffFile2",
    	"DiffFile",
    	"OutputRows",
    	"DiffRowCountProperty",
    	"DiffCellCountProperty",
    	"SameRowCountProperty",
    	"SameCellCountProperty"
	};
	return this.toString(parameters, parameterOrder);
}

}