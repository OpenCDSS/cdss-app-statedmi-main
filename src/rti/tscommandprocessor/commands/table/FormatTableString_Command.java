package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableStringFormatter;

/**
This class initializes, checks, and runs the FormatTableString() command.
*/
public class FormatTableString_Command extends AbstractCommand implements Command
{
    
/**
Constructor.
*/
public FormatTableString_Command ()
{   super();
    setCommandName ( "FormatTableString" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{   String TableID = parameters.getValue ( "TableID" );
    String Format = parameters.getValue ( "Format" );
    String OutputColumn = parameters.getValue ( "OutputColumn" );
    String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || TableID.isEmpty() ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the identifier for the table to process." ) );
    }
    
    if ( (Format == null) || Format.equals("") ) {
        message = "The format must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a format to process input." ) );
    }

    if ( (OutputColumn == null) || OutputColumn.equals("") ) {
        message = "The output column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a column name for output." ) );
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(5);
    validList.add ( "TableID" );
    validList.add ( "InputColumns" );
    validList.add ( "Format" );
    validList.add ( "OutputColumn" );
    validList.add ( "InsertBeforeColumn" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
    
    if ( warning.length() > 0 ) {
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag(command_tag,warning_level), warning );
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
{   List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    return (new FormatTableString_JDialog ( parent, this, tableIDChoices )).ok();
}

// Parse command is in the base class

/**
Method to execute the command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{   String message, routine = getClass().getSimpleName() + ".runCommand";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    //int log_level = 3;  // Level for non-use messages for log file.
    
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    Boolean clearStatus = new Boolean(true); // default
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}
    PropList parameters = getCommandParameters();
    
    // Get the input parameters...
    
    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) ) {
    	// In discovery mode want lists of tables to include ${Property}
    	if ( TableID.indexOf("${") >= 0 ) {
    		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    	}
    }
    String InputColumns = parameters.getValue ( "InputColumns" );
    String [] inputColumnNames = new String[0];
    if ( (InputColumns != null) && !InputColumns.equals("") ) {
        if ( InputColumns.indexOf(",") < 0 ) {
            inputColumnNames = new String[1];
            inputColumnNames[0] = InputColumns.trim();
        }
        else {
            inputColumnNames = InputColumns.split(",");
            for ( int i = 0; i < inputColumnNames.length; i++ ) {
                inputColumnNames[i] = inputColumnNames[i].trim();
            }
        }
    }
    String Format = parameters.getValue ( "Format" );
    String OutputColumn = parameters.getValue ( "OutputColumn" );
    String InsertBeforeColumn = parameters.getValue ( "InsertBeforeColumn" );

    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( (TableID != null) && !TableID.equals("") ) {
        // Get the table to be updated
        request_params = new PropList ( "" );
        request_params.set ( "TableID", TableID );
        try {
            bean = processor.processRequest( "GetTable", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }
        PropList bean_PropList = bean.getResultsPropList();
        Object o_Table = bean_PropList.getContents ( "Table" );
        if ( o_Table == null ) {
            message = "Unable to find table to process using TableID=\"" + TableID + "\".";
            Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that a table exists with the requested ID." ) );
        }
        else {
            table = (DataTable)o_Table;
        }
    }
    
    if ( warning_count > 0 ) {
        // Input error...
        message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
        Message.printWarning(3, routine, message );
        throw new CommandException ( message );
    }
    
    // Now process...

    List<String> problems = new ArrayList<String>();
    try {
        DataTableStringFormatter dtm = new DataTableStringFormatter ( table );
        dtm.format ( inputColumnNames, Format, OutputColumn, InsertBeforeColumn, problems );
    }
    catch ( Exception e ) {
        message = "Unexpected error formatting string (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
    finally {
	    int MaxWarnings_int = 500; // Limit the problems to 500 to prevent command overload
	    int problemsSize = problems.size();
	    int problemsSizeOutput = problemsSize;
	    String ProblemType = "FormatTableString";
	    if ( (MaxWarnings_int > 0) && (problemsSize > MaxWarnings_int) ) {
	        // Limit the warnings to the maximum
	        problemsSizeOutput = MaxWarnings_int;
	    }
	    if ( problemsSizeOutput < problemsSize ) {
	        message = "Performing string formatting had " + problemsSize + " warnings - only " + problemsSizeOutput + " are listed.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
	        // No recommendation since it is a user-defined check
	        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
	        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
	    }
	    for ( int iprob = 0; iprob < problemsSizeOutput; iprob++ ) {
	        message = problems.get(iprob);
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
	        // No recommendation since it is a user-defined check
	        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
	        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
	    }
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
        throw new CommandWarningException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
    
    String TableID = parameters.getValue( "TableID" );
    String InputColumns = parameters.getValue( "InputColumns" );
    String Format = parameters.getValue( "Format" );
    String OutputColumn = parameters.getValue( "OutputColumn" );
    String InsertBeforeColumn = parameters.getValue( "InsertBeforeColumn" );
        
    StringBuffer b = new StringBuffer ();

    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (InputColumns != null) && (InputColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InputColumns=\"" + InputColumns + "\"" );
    }
    if ( (Format != null) && (Format.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Format=\"" + Format + "\"" );
    }
    if ( (OutputColumn != null) && (OutputColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputColumn=\"" + OutputColumn + "\"" );
    }
    if ( (InsertBeforeColumn != null) && (InsertBeforeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InsertBeforeColumn=\"" + InsertBeforeColumn + "\"" );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

}