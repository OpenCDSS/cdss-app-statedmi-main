// SortTable_Command - This class initializes, checks, and runs the SortTable() command.

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringDictionary;
import RTi.Util.Table.DataTable;

/**
This class initializes, checks, and runs the SortTable() command.
*/
public class SortTable_Command extends AbstractCommand implements Command
{

/**
Values for SortOrder parameter.
*/
protected final String _Ascending = "Ascending";
protected final String _Descending = "Descending";

/**
Columns to sort, initialized in checkCommandParameters().
*/
private String [] sortColumns = new String[0];

/**
Constructor.
*/
public SortTable_Command ()
{	super();
	setCommandName ( "SortTable" );
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
{	String TableID = parameters.getValue ( "TableID" );
    String SortColumns = parameters.getValue ( "SortColumns" );
    String SortOrder = parameters.getValue ( "SortOrder" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || TableID.isEmpty() ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }

    if ( (SortColumns == null) || SortColumns.isEmpty() ) {
        message = "The column to sort must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the name of the column to sort." ) );
    }
    else {
    	this.sortColumns = SortColumns.split(",");
    	for ( int i = 0; i < this.sortColumns.length; i++ ) {
    		this.sortColumns[i] = this.sortColumns[i].trim();
    	}
    }
    
    if ( (SortOrder != null) && (SortOrder.length() != 0) ) {
    	StringDictionary sortOrder = new StringDictionary(SortOrder,":",",");
    	LinkedHashMap<String,String> map = sortOrder.getLinkedHashMap();
    	Set<String> set = map.keySet();
    	for ( String s : set ) {
    		// Look for column in the sort columns list
    		boolean found = false;
    		for ( int i = 0; i < this.sortColumns.length; i++ ) {
    			if ( s.equalsIgnoreCase(sortColumns[i]) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			message = "Column \"" + s + "\" specified with sort order is not included in SortColumns.";
    	        warning += "\n" + message;
    	        status.addToLog ( CommandPhaseType.INITIALIZATION,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Only specify sort order for columns that are being sorted." ) );
    		}
    		String order = map.get(s);
    		if ( !order.equalsIgnoreCase(_Ascending) && !order.equalsIgnoreCase(_Descending)) {
    	        message = "The sort order (" + order + ") for sort column (" + s + ") is invalid.";
    	        warning += "\n" + message;
    	        status.addToLog ( CommandPhaseType.INITIALIZATION,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Specify the sort order as " + _Ascending + " or " + _Descending + ".") );
    		}
    	}
    }
 
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(3);
    validList.add ( "TableID" );
    validList.add ( "SortColumns" );
    validList.add ( "SortOrder" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new SortTable_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Parse the command.  Need to handle legacy SortOrder that does not use a dictionary.
@param commandString the string representation of the command
*/
public void parseCommand ( String commandString )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{
	super.parseCommand(commandString);
	// Check for SortOrder that does not have the dictionary ":" delimiter
	// If found, replace with the new syntax on the single column to be sorted
	PropList props = getCommandParameters();
	String propValue = props.getValue("SortOrder");
	if ( propValue != null ) {
		if ( propValue.indexOf(":") < 0 ) {
			// Does not use the dictionary notation so set to new syntax
			String col = props.getValue("SortColumns");
			props.set("SortOrder",col + ":" + propValue);
		}
	}
}

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

	CommandProcessor processor = getCommandProcessor();
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
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

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) ) {
    	// In discovery mode want lists of tables to include ${Property}
    	if ( TableID.indexOf("${") >= 0 ) {
    		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    	}
    }
    String SortOrder = parameters.getValue ( "SortOrder" );
    StringDictionary sortOrder = new StringDictionary(SortOrder,":",",");
	int [] sortOrderArray = new int[sortColumns.length];
	for ( int i = 0; i < sortColumns.length; i++ ) {
		sortOrderArray[i] = 1; // Default
		Object o = sortOrder.get(sortColumns[i]);
		if ( o != null ) {
			String s = (String)o;
			if ( s.equalsIgnoreCase(_Descending) ) {
				sortOrderArray[i] = -1;
			}
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
    	// Sort the table...
        table.sortTable ( sortColumns, sortOrderArray );
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error sorting the table (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
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
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
	String SortColumns = props.getValue( "SortColumns" );
	String SortOrder = props.getValue( "SortOrder" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
	if ( (SortColumns != null) && (SortColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SortColumns=\"" + SortColumns + "\"" );
	}
    if ( (SortOrder != null) && (SortOrder.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "SortOrder=\"" + SortOrder + "\"");
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
