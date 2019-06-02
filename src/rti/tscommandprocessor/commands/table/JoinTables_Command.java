// JoinTables_Command - This class initializes, checks, and runs the JoinTables() command.

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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
//import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableJoinMethodType;
import RTi.Util.Table.HandleMultipleJoinMatchesHowType;

// TODO SAM 2013-08-19 Don't make discoverable because new table is not created.  In the future may allow the
// joined table to be a copy
/**
This class initializes, checks, and runs the JoinTables() command.
*/
public class JoinTables_Command extends AbstractCommand implements Command //, CommandDiscoverable, ObjectListProvider
{

/**
The table that is created.
*/
private DataTable __table = null;

/**
Constructor.
*/
public JoinTables_Command ()
{	super();
	setCommandName ( "JoinTables" );
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
    String TableToJoinID = parameters.getValue ( "TableToJoinID" );
    String JoinMethod = parameters.getValue ( "JoinMethod" );
    String JoinColumns = parameters.getValue ( "JoinColumns" );
    String HandleMultipleJoinMatchesHow = parameters.getValue ( "HandleMultipleJoinMatchesHow" );
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
    
    if ( (TableToJoinID == null) || (TableToJoinID.length() == 0) ) {
        message = "The table to join identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table to join identifier." ) );
    }
    
    if ( (TableID != null) && (TableID.length() != 0) && (TableToJoinID != null) && (TableToJoinID.length() != 0) &&
        TableID.equalsIgnoreCase(TableToJoinID) ) {
        message = "The original and table to join identifiers are the same.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table to join identifier different from the original table identifier." ) );
    }
    
    if ( (JoinMethod != null) && (JoinMethod.length() != 0) && !JoinMethod.equalsIgnoreCase("" + DataTableJoinMethodType.JOIN_ALWAYS) &&
        !JoinMethod.equalsIgnoreCase("" + DataTableJoinMethodType.JOIN_IF_IN_BOTH)) {
        message = "The join method (" + JoinMethod + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the join method as " + DataTableJoinMethodType.JOIN_ALWAYS + " or " +
                DataTableJoinMethodType.JOIN_IF_IN_BOTH) );
    }
    
    if ( (JoinColumns != null) && !JoinColumns.isEmpty() && (JoinColumns.indexOf(":") < 0) ) {
    	// Probably specified only one column
        message = "The join column names must be specified for each table.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table column names for both tables, even if the same." ) );
    }
    
    if ( (HandleMultipleJoinMatchesHow != null) && !HandleMultipleJoinMatchesHow.isEmpty() &&
    	!HandleMultipleJoinMatchesHow.equalsIgnoreCase(""+HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS) &&
        !HandleMultipleJoinMatchesHow.equalsIgnoreCase(""+HandleMultipleJoinMatchesHowType.USE_LAST_MATCH)) {
        message = "The HandleMultipleJoinMatchesHow parameter (" + HandleMultipleJoinMatchesHow + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify HandleMultipleJoinMatchesHow as " + HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS +
                	" or " + HandleMultipleJoinMatchesHowType.USE_LAST_MATCH + " (default).") );
    }
 
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(8);
    validList.add ( "TableID" );
    validList.add ( "TableToJoinID" );
    validList.add ( "JoinColumns" );
    validList.add ( "IncludeColumns" );
    validList.add ( "ColumnMap" );
    validList.add ( "ColumnFilters" );
    validList.add ( "JoinMethod" );
    validList.add ( "HandleMultipleJoinMatchesHow" );
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
	return (new JoinTables_JDialog ( parent, this, tableIDChoices )).ok();
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
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{   DataTable table = getDiscoveryTable();
    List<T> v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new Vector<T>();
        v.add ( (T)table );
    }
    return v;
}

// Use base class parseCommand()

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
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
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
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommandInteral",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
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
		status.clearLog(commandPhase);
	}
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String TableToJoinID = parameters.getValue ( "TableToJoinID" );
    if ( (TableToJoinID != null) && !TableToJoinID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableToJoinID.indexOf("${") >= 0 ) {
    	TableToJoinID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableToJoinID);
    }
    String JoinColumns = parameters.getValue ( "JoinColumns" );
    if ( (JoinColumns != null) && !JoinColumns.isEmpty() && (commandPhase == CommandPhaseType.RUN) && JoinColumns.indexOf("${") >= 0 ) {
    	JoinColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, JoinColumns);
    }
    Hashtable<String,String> joinColumnsMap = new Hashtable<String,String>();
    if ( (JoinColumns != null) && (JoinColumns.length() > 0) && (JoinColumns.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(JoinColumns, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            joinColumnsMap.put(parts[0].trim(),parts[1].trim() );
        }
    }
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    if ( (IncludeColumns != null) && !IncludeColumns.isEmpty() && (commandPhase == CommandPhaseType.RUN) && IncludeColumns.indexOf("${") >= 0 ) {
    	IncludeColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, IncludeColumns);
    }
    String [] includeColumns = null;
    if ( (IncludeColumns != null) && !IncludeColumns.equals("") ) {
        includeColumns = IncludeColumns.split(",");
        for ( int i = 0; i < includeColumns.length; i++ ) {
            includeColumns[i] = includeColumns[i].trim();
        }
    }
    String ColumnMap = parameters.getValue ( "ColumnMap" );
    if ( (ColumnMap != null) && !ColumnMap.isEmpty() && (commandPhase == CommandPhaseType.RUN) && ColumnMap.indexOf("${") >= 0 ) {
    	ColumnMap = TSCommandProcessorUtil.expandParameterValue(processor, this, ColumnMap);
    }
    Hashtable<String,String> columnMap = new Hashtable<String,String>();
    if ( (ColumnMap != null) && (ColumnMap.length() > 0) && (ColumnMap.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnMap, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            columnMap.put(parts[0].trim(),parts[1].trim());
        }
    }
    String ColumnFilters = parameters.getValue ( "ColumnFilters" );
    if ( (ColumnFilters != null) && !ColumnFilters.isEmpty() && (commandPhase == CommandPhaseType.RUN) && ColumnFilters.indexOf("${") >= 0 ) {
    	ColumnFilters = TSCommandProcessorUtil.expandParameterValue(processor, this, ColumnFilters);
    }
    Hashtable<String,String> columnFilters = new Hashtable<String,String>();
    if ( (ColumnFilters != null) && (ColumnFilters.length() > 0) && (ColumnFilters.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnFilters, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            columnFilters.put(parts[0].trim(),parts[1].trim() );
        }
    }
    String JoinMethod = parameters.getValue ( "JoinMethod" );
    DataTableJoinMethodType joinMethodType = DataTableJoinMethodType.valueOfIgnoreCase(JoinMethod);
    if ( joinMethodType == null ) {
        joinMethodType = DataTableJoinMethodType.JOIN_IF_IN_BOTH;
    }
    String HandleMultipleJoinMatchesHow0 = parameters.getValue ( "HandleMultipleJoinMatchesHow" );
    HandleMultipleJoinMatchesHowType handleMultipleJoinMatchesHow =
    	HandleMultipleJoinMatchesHowType.valueOfIgnoreCase(HandleMultipleJoinMatchesHow0);
    if ( handleMultipleJoinMatchesHow == null ) {
    	handleMultipleJoinMatchesHow = HandleMultipleJoinMatchesHowType.USE_LAST_MATCH; // Default
    }
    
    // Get the tables to process.

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
    
    DataTable tableToJoin = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        String tableToJoinID = TSCommandProcessorUtil.expandParameterValue(processor,this,TableToJoinID);
        if ( (TableID != null) && !TableID.equals("") ) {
            // Get the table to be updated
            request_params = new PropList ( "" );
            request_params.set ( "TableID", tableToJoinID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + tableToJoinID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + tableToJoinID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
                tableToJoin = (DataTable)o_Table;
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

    List<String> problems = new ArrayList<String>();
	try {
    	// Join the tables...
	    if ( commandPhase == CommandPhaseType.RUN ) {
	        table.joinTable ( table, tableToJoin, joinColumnsMap, includeColumns, columnMap, columnFilters,
	        	joinMethodType, handleMultipleJoinMatchesHow, problems );
	        // Table is already in the processor so no need to resubmit
	        // TODO SAM 2013-07-31 at some point may need to refresh discovery on table column names
	        for ( String p : problems ) {
	            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, p );
	            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING, p, "Check input." ) );
	        }
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error joining tables (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
        int i = -1;
        for ( String p : problems ) {
            ++i;
            if ( i < 1000 ) {
                // TODO SAM 2014-06-26 Cap warnings without hard-coding
                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
                    p, "Check input." ) );
            }
        }
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
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
    String TableToJoinID = props.getValue( "TableToJoinID" );
    String JoinColumns = props.getValue( "JoinColumns" );
	String IncludeColumns = props.getValue( "IncludeColumns" );
	String ColumnMap = props.getValue( "ColumnMap" );
	String ColumnFilters = props.getValue( "ColumnFilters" );
	String JoinMethod = props.getValue( "JoinMethod" );
	String HandleMultipleJoinMatchesHow = props.getValue( "HandleMultipleJoinMatchesHow" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (TableToJoinID != null) && (TableToJoinID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableToJoinID=\"" + TableToJoinID + "\"" );
    }
    if ( (JoinColumns != null) && (JoinColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "JoinColumns=\"" + JoinColumns + "\"" );
    }
	if ( (IncludeColumns != null) && (IncludeColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeColumns=\"" + IncludeColumns + "\"" );
	}
    if ( (ColumnMap != null) && (ColumnMap.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnMap=\"" + ColumnMap + "\"" );
    }
    if ( (ColumnFilters != null) && (ColumnFilters.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnFilters=\"" + ColumnFilters + "\"" );
    }
    if ( (JoinMethod != null) && (JoinMethod.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "JoinMethod=" + JoinMethod );
    }
    if ( (HandleMultipleJoinMatchesHow != null) && (HandleMultipleJoinMatchesHow.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "HandleMultipleJoinMatchesHow=" + HandleMultipleJoinMatchesHow );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
