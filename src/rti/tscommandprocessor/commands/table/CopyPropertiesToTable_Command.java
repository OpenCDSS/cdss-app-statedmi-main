// CopyPropertiesToTable_Command - This class initializes, checks, and runs the CopyPropertiesToTable_Command() command.

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
import java.util.HashMap;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the CopyPropertiesToTable_Command() command.
*/
public class CopyPropertiesToTable_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Used with AllowDuplicates.
*/
protected final String _False = "False";
protected final String _True = "True";
    
/**
The table that is created (when not operating on an existing table).
*/
private DataTable __table = null;

/**
Constructor.
*/
public CopyPropertiesToTable_Command ()
{   super();
    setCommandName ( "CopyPropertiesToTable" );
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
{   String IncludeProperties = parameters.getValue ( "IncludeProperties" );
    String TableID = parameters.getValue ( "TableID" );
    String TableLookupColumn = parameters.getValue ( "TableLookupColumn" );
    String AllowDuplicates = parameters.getValue ( "AllowDuplicates" );
    String TableOutputColumns = parameters.getValue ( "TableOutputColumns" );
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
    
    if ( (TableLookupColumn == null) || TableLookupColumn.equals("") ) {
        message = "The table lookup column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a table lookup column name." ) );
    }
    
    if ( (IncludeProperties != null) && !IncludeProperties.equals("") &&
        (TableOutputColumns != null) && !TableOutputColumns.equals("") ) {
        String[] includeProperties = IncludeProperties.split(",");
        String[] tableOutputColumns = TableOutputColumns.split(",");
        if ( includeProperties.length != tableOutputColumns.length ) {
            message = "The number of include properties (" +
                ") and the number of specified table output columns (" + ") is different.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the same number of include properties names as output columns." ) );
        }
    }
    
    if ( (AllowDuplicates != null) && !AllowDuplicates.equals("") && !AllowDuplicates.equalsIgnoreCase(_False) &&
        !AllowDuplicates.equalsIgnoreCase(_True) ) {
        message = "The AllowDuplicates value (" + AllowDuplicates + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specity the value as " + _False + " or " + _True + " (default)." ) );
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(6);
    validList.add ( "IncludeProperties" );
    validList.add ( "TableID" );
    validList.add ( "TableLookupColumn" );
    validList.add ( "TableLookupValue" );
    validList.add ( "AllowDuplicates" );
    validList.add ( "TableOutputColumns" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
    
    if ( warning.length() > 0 ) {
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag(command_tag,warning_level),
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
{   List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    return (new CopyPropertiesToTable_JDialog ( parent, this, tableIDChoices )).ok();
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
        v = new ArrayList<T>();
        v.add ( (T)table );
    }
    return v;
}

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
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{   String message, routine = getClass().getSimpleName() + ".runCommand";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    //int log_level = 3;
    //int log_level = 3;  // Level for non-use messages for log file.
    
    CommandProcessor processor = getCommandProcessor();
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
		status.clearLog(commandPhase);
	}
    PropList parameters = getCommandParameters();
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }
    
    // Get the input parameters...

    String IncludeProperties = parameters.getValue ( "IncludeProperties" );
    String [] includeProperties = null;
    if ( (IncludeProperties != null) && !IncludeProperties.equals("") ) {
        includeProperties = IncludeProperties.trim().split(",");
    }
    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) ) {
    	// In discovery mode want lists of tables to include ${Property}
    	if ( TableID.indexOf("${") >= 0 ) {
    		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    	}
    }
    String TableLookupColumn = parameters.getValue ( "TableLookupColumn" );
    String TableLookupValue = parameters.getValue ( "TableLookupValue" );
    String AllowDuplicates = parameters.getValue ( "AllowDuplicates" );
    boolean allowDuplicates = false; // Default
    if ( (AllowDuplicates != null) && AllowDuplicates.equalsIgnoreCase(_True) ) {
        allowDuplicates = true;
    }
    String TableOutputColumns = parameters.getValue ( "TableOutputColumns" );
    String [] tableOutputColumnNames = null;
    if ( (TableOutputColumns != null) && !TableOutputColumns.equals("") ) {
        tableOutputColumnNames = TableOutputColumns.split(",");
        // These are expanded below based on dynamic time series properties
    }

    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( (TableID != null) && !TableID.equals("") ) {
        // Get the table to be updated/created
        request_params = new PropList ( "" );
        request_params.set ( "TableID", TableID );
        try {
            bean = processor.processRequest( "GetTable", request_params);
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table != null ) {
                // Found the table so no need to create it
                table = (DataTable)o_Table;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }
    }
  
    if ( warning_count > 0 ) {
        // Input error...
        message = "Insufficient data to run command.";
        status.addToLog ( commandPhase,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
        Message.printWarning(3, routine, message );
        throw new CommandException ( message );
    }
    
    // Now process...

    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        if ( table == null ) {
            // Did not find table so is being created in this command
            // Create an empty table and set the ID
            table = new DataTable();
            table.setTableID ( TableID );
            setDiscoveryTable ( table );
        }
    }
    else if ( commandPhase == CommandPhaseType.RUN ) {
        if ( table == null ) {
            // Did not find the table above so create it
            table = new DataTable( /*columnList*/ );
            table.setTableID ( TableID );
            Message.printStatus(2, routine, "Was not able to match existing table \"" + TableID + "\" so created new table.");
            
            // Set the table in the processor...
            
            request_params = new PropList ( "" );
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
        }
        // TODO SAM 2015-06-07 Need to enable logic for properties
        try {
            int TableLookupColumnNumber = -1;
            Message.printStatus(2, routine, "Copying properties to table \"" + TableID + "\"");
                           
            // Get the properties to process
            if ( IncludeProperties == null ) {
                // Get all the properties by forming a list of property names from the hashtable
                HashMap<String, Object> propertyHash = null; // TODO SAM Enable ts.getProperties();
                ArrayList<String> keyList = new ArrayList<String>(propertyHash.keySet());
                // Don't sort because original order has meaning
                //Collections.sort(keyList);
                includeProperties = StringUtil.toArray(keyList);
            }
            // Set the column names from the properties
            if ( tableOutputColumnNames == null ) {
                tableOutputColumnNames = includeProperties;
            }
            else {
                // Check for wildcards
                for ( int icolumn = 0; icolumn < includeProperties.length; icolumn++ ) {
                    if ( tableOutputColumnNames[icolumn].equals("*") ) {
                        // Output column name is the same as the property name
                        tableOutputColumnNames[icolumn] = includeProperties[icolumn];
                    }
                }
            }
                
            // Make sure that the output table includes the columns to receive property values, including the lookup column.
            try {
                TableLookupColumnNumber = table.getFieldIndex(TableLookupColumn);
            }
            catch ( Exception e2 ) {
                TableLookupColumnNumber =
                    table.addField(new TableField(TableField.DATA_TYPE_STRING, TableLookupColumn, -1, -1), null);
                Message.printStatus(2, routine, "Did not match TableLookupColumn \"" + TableLookupColumn +
                    "\" as column table so added to table." );
            }
            // Other output column types depend on the properties
            for ( int i = 0; i < tableOutputColumnNames.length; i++ ) {
                String tableOutputColumnName = tableOutputColumnNames[i];
                try {
                    // Column names are allowed to use time series properties
                    tableOutputColumnName = ""; // TODO SAM EnableTSCommandProcessorUtil.expandTimeSeriesMetadataString(
                        //processor, ts, tableOutputColumnName, status, commandPhase);
                    table.getFieldIndex(tableOutputColumnName);
                }
                catch ( Exception e2 ) {
                    message = "Table \"" + TableID + "\" does not have column \"" + tableOutputColumnName + "\" - creating.";
                    Message.printStatus(2,routine,message);
                    //Message.printWarning ( warning_level,
                    //MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                    //status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    //    message, "Verify that a table exists with the requested output column." ) );
                    // Skip the time series...
                    //continue;
                    //
                    // Create the column in the table - do this before any attempt to match the record based on TSID below
                    // For now don't set any width or precision on the column.
                    // First find the matching property in the time series to determine the property type.
                    // The order of IncludeProperties is the same as tableOutputColumnNames.
                    Object propertyValue = null; // TODO SAM Enable ts.getProperty(includeProperties[i] );
                    if ( propertyValue == null ) {
                        // If null just let the property be set by a later record where a non-null value is found.
                        // TODO SAM 2012-09-30 Is it possible to check the type even if null?
                        continue;
                    }
                    /* TODO Dead code that needs to be evaluated
                    else if ( propertyValue instanceof String ) {
                        table.addField(new TableField(TableField.DATA_TYPE_STRING, tableOutputColumnName, -1, -1), null);
                    }
                    else if ( propertyValue instanceof Integer ) {
                        table.addField(new TableField(TableField.DATA_TYPE_INT, tableOutputColumnName, -1, -1), null);
                    }
                    else if ( propertyValue instanceof Long ) {
                        table.addField(new TableField(TableField.DATA_TYPE_LONG, tableOutputColumnName, -1, -1), null);
                    }
                    else if ( propertyValue instanceof Short ) {
                        table.addField(new TableField(TableField.DATA_TYPE_SHORT, tableOutputColumnName, -1, -1), null);
                    }
                    else if ( propertyValue instanceof Double ) {
                        table.addField(new TableField(TableField.DATA_TYPE_DOUBLE, tableOutputColumnName,15, 6), null);
                    }
                    else if ( propertyValue instanceof Float ) {
                        table.addField(new TableField(TableField.DATA_TYPE_FLOAT, tableOutputColumnName,15, 6), null);
                    }
                    else if ( propertyValue instanceof Date ) {
                        table.addField(new TableField(TableField.DATA_TYPE_DATE, tableOutputColumnName, -1, -1), null);
                    }
                    else if ( propertyValue instanceof DateTime ) {
                        table.addField(new TableField(TableField.DATA_TYPE_DATETIME, tableOutputColumnName, -1, -1), null);
                    }
                    else {
                        message = "Time property type for \"" + tableOutputColumnNames[i] +
                            "\" (" + propertyValue + ") is not handled - cannot add column to table.";
                        Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Contact software support." ) );
                        // Skip the time series...
                        continue;
                    }
                    Message.printStatus(2, routine, "Did not match property name \"" + tableOutputColumnNames[i] +
                        "\" as column table so added to table." );
                    */
                }
            }
                
            // Get the table column numbers corresponding to the column names...
            
            // Get the columns from the table to be used as output...
            // TODO SAM 2014-06-09 Why is this done here and not above?
            int [] tableOutputColumns = new int[tableOutputColumnNames.length];
            String [] tableOutputColumnNamesExpanded = new String[tableOutputColumnNames.length];
            for ( int i = 0; i < tableOutputColumns.length; i++ ) {
                tableOutputColumnNamesExpanded[i] = ""; // TODO SAM Enable TSCommandProcessorUtil.expandTimeSeriesMetadataString(
                    //processor, ts, tableOutputColumnNames[i], status, commandPhase);
                try {
                    tableOutputColumns[i] = table.getFieldIndex(tableOutputColumnNamesExpanded[i]);
                }
                catch ( Exception e2 ) {
                    // This should not happen since columns created above, but possible that a value had all nulls
                    // above and therefore column was not added because type was unknown
                    // FIXME SAM 2012-09-30 Need to add column as string if all values were null?
                    //message = "Table \"" + TableID + "\" does not have column \"" + tableOutputColumnNames[i] + "\".";
                    //Message.printWarning ( warning_level,
                    //MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                    //status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    //    message, "Verify that a table exists with the requested output column." ) );
                    // Skip the time series...
                    //continue;
                }
            }
                
            // See if a matching row exists using the specified TSID column...
            String tsid = null;
            if ( (TableLookupValue != null) && !TableLookupValue.equals("") ) {
                // Format the TSID using the specified format
                // TODO SAM Enable tsid = ts.formatLegend ( TableLookupValue );
            }
            TableRecord rec = null;
            if ( !allowDuplicates ) {
                // Try to match the TSID 
                rec = table.getRecord ( TableLookupColumn, tsid );
            }
            if ( rec == null ) {
                //message = "Cannot find table \"" + TableID + "\" cell in column \"" + TableLookupColumn +
                //    "\" matching TSID formatted as \"" + tsid + "\" - skipping time series \"" +
                //    ts.getIdentifierString() + "\".";
                //Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                //    routine, message );
                //status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE, message,
                //    "Verify that table \"" + TableID + "\" column TSID matches one or more time series." ) );
                // Go to next time series.
                //continue;
                
                // Add a new record to the table that matches the formatted TSID
                int recNum = table.getTableRecords().size();
                table.setFieldValue(recNum, TableLookupColumnNumber, tsid, true);
                // Get the new record for use below
                rec = table.getRecord(recNum);
            }
            else {
                Message.printStatus(2, routine, "Matched table \"" + TableID + "\" row for TSID \"" + tsid );
            }
            
            // Loop through the property names...
            
            //for ( int icolumn = 0; icolumn < IncludeProperties.length; icolumn++ ) {
            //    String propertyName = IncludeProperties[icolumn];
            //    Object propertyValue = ts.getProperty(propertyName);
            for ( int icolumn = 0; icolumn < tableOutputColumnNames.length; icolumn++ ) {
                String propertyName = includeProperties[icolumn];
                Object propertyValue = ""; // TODO SAM Enable ts.getProperty(propertyName);
                // If the property value is null, just skip setting it - default value for columns is null
                // TODO SAM 2011-04-27 Should this be a warning?
                /* TODO dead code that needs to be evaluated
                if ( propertyValue == null ) {
                    Message.printStatus(2,routine,"Time series property \"" + propertyName + "\" is null, not copying");
                    continue;
                }
                */
                // Get the matching table column
                try {
                    // Get the value from the table
                    // Make sure that the table has the specified column...
                    int colNumber = tableOutputColumns[icolumn];
                    if ( colNumber < 0 ) {
                        // TODO SAM 2012-09-30 Should not happen?
                        message = "Table \"" + TableID +
                        "\" does not have column \"" + tableOutputColumnNamesExpanded[icolumn] + "\".";
                        Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
                            message, "Verify that the proper table output column is specified and has been defined." ) );
                        continue;
                    }
                    // Set the value in the table...
                    try {
                        rec.setFieldValue(colNumber,propertyValue);
                        if ( Message.isDebugOn ) {
                            Message.printDebug(1, routine, "Setting table column " + tableOutputColumnNamesExpanded[icolumn] + "=\"" +
                                propertyValue + "\"" );
                        }
                        Message.printStatus(2, routine, "Setting table column " + tableOutputColumnNamesExpanded[icolumn] + "=\"" +
                             propertyValue + "\"" );
                        // TODO SAM 2011-04-27 Evaluate why the column width is necessary in the data table
                        // Reset the column width if necessary
                        if ( propertyValue instanceof String ) {
                            // If the incoming string is longer than the column width, reset the column width
                            int width = table.getFieldWidth(tableOutputColumns[icolumn]);
                            if ( width > 0 ) {
                                table.setFieldWidth(tableOutputColumns[icolumn],
                                    Math.max(width,((String)propertyValue).length()));
                            }
                        }
                    }
                    catch ( Exception e ) {
                        // Blank cell values are allowed - just don't set the property
                        message = "Unable to set " + propertyName + "=" + propertyValue + " in table \"" + TableID +
                            "\" column \"" + tableOutputColumnNamesExpanded[icolumn] +
                            "\" matching lookup value \""; // TODO SAM Enable + tsid + " (" + ts.getIdentifier().toStringAliasAndTSID() + "\") (" + e + ").";
                        Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING, message,
                            "Verify that the proper table output column is specified and has been defined." ) );
                    }
                }
                catch ( Exception e ) {
                	// TODO SAM Enable
                    //message = "Unexpected error processing ... \""+ ts.getIdentifier() + " (" + e + ").";
                    //Message.printWarning ( warning_level,
                    //    MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                    //Message.printWarning(3,routine,e);
                    //status.addToLog ( commandPhase,new CommandLogRecord(CommandStatusType.FAILURE,
                    //    message, "See the log file for details - report the problem to software support." ) );
                }
            }
            Message.printStatus(2, routine, "Table \"" + TableID +
                "\" after copying properties has " + table.getNumberOfRecords() + " records." );
        }
        catch ( Exception e ) {
            message = "Unexpected error processing properties (" + e + ").";
            Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            Message.printWarning ( 3, routine, e );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Check log file for details." ) );
            throw new CommandException ( message );
        }
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
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{   
    if ( parameters == null ) {
        return getCommandName() + "()";
    }
    
    String IncludeProperties = parameters.getValue( "IncludeProperties" );
    String TableID = parameters.getValue( "TableID" );
    String TableLookupColumn = parameters.getValue ( "TableLookupColumn" );
    String TableLookupValue = parameters.getValue ( "TableLookupValue" );
    String AllowDuplicates = parameters.getValue ( "AllowDuplicates" );
    String TableOutputColumns = parameters.getValue ( "TableOutputColumns" );
        
    StringBuffer b = new StringBuffer ();

    if ( (IncludeProperties != null) && (IncludeProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeProperties=\"" + IncludeProperties + "\"" );
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (TableLookupColumn != null) && (TableLookupColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableLookupColumn=\"" + TableLookupColumn + "\"" );
    }
    if ( (TableLookupValue != null) && (TableLookupValue.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableLookupValue=\"" + TableLookupValue + "\"" );
    }
    if ( (AllowDuplicates != null) && (AllowDuplicates.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "AllowDuplicates=\"" + AllowDuplicates + "\"" );
    }
    if ( (TableOutputColumns != null) && (TableOutputColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableOutputColumns=\"" + TableOutputColumns + "\"" );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

}
