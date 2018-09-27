package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableValueStringProvider;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the InsertTableRow() command.
*/
public class InsertTableRow_Command extends AbstractCommand
{
    
/**
Constructor.
*/
public InsertTableRow_Command ()
{	super();
	setCommandName ( "InsertTableRow" );
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
{	String TableID = parameters.getValue ( "TableID" );
    String InsertRow = parameters.getValue ( "InsertRow" );
    String InsertCount = parameters.getValue ( "InsertCount" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }
    if ( (InsertRow != null) && !InsertRow.equals("") ) {
        if ( !StringUtil.isInteger(InsertRow.trim()) ) {
            message = "The InsertRow value (" + InsertRow + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the insert row as an integer 1+." ) );
        }
        else {
            int i = Integer.parseInt(InsertRow.trim());
            if ( i <= 0 ) {
                message = "The InsertRow value (" + InsertRow + ") is invalid.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the insert row as an integer 1+." ) );
            }
        }
    }
    if ( (InsertCount != null) && !InsertCount.equals("") ) {
        if ( !StringUtil.isInteger(InsertCount.trim()) ) {
            message = "The InsertCount value (" + InsertCount + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the insert count as an integer 1+." ) );
        }
        else {
            int i = Integer.parseInt(InsertCount.trim());
            if ( i <= 0 ) {
                message = "The InsertCount value (" + InsertCount + ") is invalid.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the insert count as an integer 1+." ) );
            }
        }
    }
 
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(4);
    validList.add ( "TableID" );
    validList.add ( "InsertRow" );
    validList.add ( "InsertCount" );
    validList.add ( "ColumnValues" );
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
public boolean editCommand ( JFrame parent )
{	List<String> tableIDChoices = TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
        (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new InsertTableRow_JDialog ( parent, this, tableIDChoices )).ok();
}

// Use base class parseCommand()

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String TableID = parameters.getValue ( "TableID" );
    String InsertRow = parameters.getValue ( "InsertRow" );
    Integer insertRow = null;
    if ( (InsertRow != null) && !InsertRow.equals("") ) {
        insertRow = new Integer(InsertRow);
    }
    String InsertCount = parameters.getValue ( "InsertCount" );
    Integer insertCount = null;
    if ( (InsertCount != null) && !InsertCount.equals("") ) {
        insertCount = new Integer(InsertCount);
    }
    String ColumnValues = parameters.getValue ( "ColumnValues" );
    // Used LinkedHashMap because want insert order to be retained in new columns, if columns are created
    LinkedHashMap<String,String> columnValues = new LinkedHashMap<String,String>();
    if ( (ColumnValues != null) && (ColumnValues.length() > 0) && (ColumnValues.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnValues, ",", 0 );
        // Now break pairs and put in hashmap
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            columnValues.put(parts[0].trim(), parts[1].trim() );
        }
    }
    
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
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	try {
    	// Insert rows...
	    if ( insertCount == null ) {
	        insertCount = 1;
	    }
	    int nRows = table.getNumberOfRecords();
        List<TableRecord> addedRecords = new ArrayList<TableRecord>();
        if ( insertRow == null ) {
            // Add the end of the table
            for ( int i = 0; i < insertCount; i++ ) {
                TableRecord addedRecord = table.addRecord(table.emptyRecord());
                if ( columnValues != null ) {
                    addedRecords.add(addedRecord);
                }
            }
        }
        else if ( insertRow < nRows ) {
            int row0 = insertRow - 1; // convert from 1-index to 0-index
            for ( int i = 0; i < insertCount; i++ ) {
                // OK to keep inserting at the same point
                TableRecord addedRecord = table.emptyRecord();
                table.insertRecord(row0,addedRecord,false);
                if ( columnValues != null ) {
                    addedRecords.add(addedRecord);
                }
            }
        }
        else {
            message = "Trying to insert rows beyond the end of the table.";
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Omit the InsertRow parameter to append to the end of the table." ) );
            throw new CommandWarningException ( message );
        }
        if ( columnValues != null ) {
        	// Set values in the table records that were added.
        	// At this point the records will have been set in the table, and table record references should be consistent.
    		final Command thisCommand = this; // for anonymous class below
    		// TODO SAM 2016-08-25 Why is the DataTableValueStringProvider needed?  The ColumnValues parameter
    		// is expanded above before parsing the parameter to allow ${xx:Property} to be expanded
    		DataTableValueStringProvider tableValueGetter = new DataTableValueStringProvider () {
    			public String getTableCellValueAsString ( String valueFormat ) {
    				// The value in the table can actually contain ${Property}
    				if ( (valueFormat == null) || valueFormat.isEmpty() || valueFormat.indexOf("${") < 0 ) {
    					return valueFormat;
    				}
    				else {
    					return TSCommandProcessorUtil.expandParameterValue(processor, thisCommand, valueFormat);
    				}
    			}
    		};
    	    table.setTableRecordValues ( addedRecords, columnValues, tableValueGetter, false );
        }
 	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error setting values in table (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
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
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
    String InsertRow = props.getValue( "InsertRow" );
    String InsertCount = props.getValue( "InsertCount" );
	String ColumnValues = props.getValue( "ColumnValues" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (InsertRow != null) && (InsertRow.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InsertRow=" + InsertRow );
    }
    if ( (InsertCount != null) && (InsertCount.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InsertCount=" + InsertCount );
    }
    if ( (ColumnValues != null) && (ColumnValues.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnValues=\"" + ColumnValues + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}