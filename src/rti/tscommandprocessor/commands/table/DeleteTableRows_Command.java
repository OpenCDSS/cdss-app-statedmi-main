// DeleteTableRows_Command - This class initializes, checks, and runs the DeleteTableRows() command.

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

package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
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

/**
This class initializes, checks, and runs the DeleteTableRows() command.
*/
public class DeleteTableRows_Command extends AbstractCommand
{
    
/**
Constructor.
*/
public DeleteTableRows_Command ()
{	super();
	setCommandName ( "DeleteTableRows" );
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
    String DeleteRowNumbers = parameters.getValue ( "DeleteRowNumbers" );
    //String DeleteCountProperty = parameters.getValue ( "DeleteCountProperty" );
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
    if ( (DeleteRowNumbers == null) || (DeleteRowNumbers.length() == 0) ) {
        message = "The row numbers to delete must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the row numbers to delete." ) );
    }
 
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(2);
    validList.add ( "TableID" );
    validList.add ( "DeleteRowNumbers" );
    //validList.add ( "DeleteCountProperty" );
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
	return (new DeleteTableRows_JDialog ( parent, this, tableIDChoices )).ok();
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
		status.clearLog(CommandPhaseType.RUN);
	}

    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String DeleteRowNumbers = parameters.getValue ( "DeleteRowNumbers" );
    if ( (DeleteRowNumbers != null) && !DeleteRowNumbers.isEmpty() && (commandPhase == CommandPhaseType.RUN) && DeleteRowNumbers.indexOf("${") >= 0 ) {
    	DeleteRowNumbers = TSCommandProcessorUtil.expandParameterValue(processor, this, DeleteRowNumbers);
    }
    String [] deleteRowNumbers = new String[0];
    if ( (DeleteRowNumbers != null) && !DeleteRowNumbers.isEmpty() ) {
    	deleteRowNumbers = DeleteRowNumbers.split(",");
	    for ( int i = 0; i < deleteRowNumbers.length; i++ ) {
	    	deleteRowNumbers[i] = deleteRowNumbers[i].trim();
	    }
    }
    //String DeleteCountProperty = parameters.getValue ( "DeleteCountProperty" );
    
    // Get the table to process.
    DataTable table = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
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
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	try {
    	// Delete rows...
		for ( int i = 0; i < deleteRowNumbers.length; i++ ) {
			// Determine the row(s) to delete, number may change as the rows are deleted so user must be aware
			int numRowsInTable = table.getNumberOfRecords();
			String rowToDeleteString = deleteRowNumbers[i];
			int rowToDelete1 = -1; // 1-index
			if ( rowToDeleteString.equalsIgnoreCase("last") ) {
				rowToDelete1 = numRowsInTable;
			}
			else {
				rowToDelete1 = Integer.parseInt(rowToDeleteString);
			}
			if ( rowToDelete1 > numRowsInTable ) {
				message = "Row to delete (" + rowToDelete1 + ") is > number of rows in table.  Not deleting.";
				Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
		        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
		            message, "Verify that the table contains row " + rowToDelete1 ) );
				continue;
			}
			if ( rowToDelete1 < 1 ) {
				message = "Row to delete (" + rowToDelete1 + ") is < 1.  Not deleting.";
				Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
		        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
		            message, "Specify a row > 0" ) );
				continue;
			}
			int rowToDelete0 = rowToDelete1 - 1; // 0-index
			try {
				table.deleteRecord(rowToDelete0);
			}
			catch ( Exception e ) {
				message = "Exception deleting row \"" + rowToDelete1 + "\" from table \"" + table.getTableID() + "\" (" + e + ").";
				Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
		        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
		            message, "Check the log file for errors." ) );
			}
        }
 	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error deleting table row (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check the log file for errors." ) );
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
    String DeleteRowNumbers = props.getValue( "DeleteRowNumbers" );
    //String DeleteCountProperty = props.getValue( "DeleteCountProperty" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (DeleteRowNumbers != null) && (DeleteRowNumbers.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DeleteRowNumbers=\"" + DeleteRowNumbers + "\"" );
    }
    /*
    if ( (DeleteCountProperty != null) && (DeleteCountProperty.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DeleteCountProperty=" + DeleteCountProperty );
    }
    */
	return getCommandName() + "(" + b.toString() + ")";
}

}
