// TableTimeSeriesMath_Command - This class initializes, checks, and runs the TableTimeSeriesMath() command.

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
import rti.tscommandprocessor.core.TSListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSUtil;
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
import RTi.Util.Table.DataTableMathOperatorType;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the TableTimeSeriesMath() command.
*/
public class TableTimeSeriesMath_Command extends AbstractCommand implements Command
{
    
/**
Values for NonValue parameter.
*/
protected final String _NaN = "NaN";
protected final String _Null = "Null";

/**
Values for IfTableInputIsBlank and IfTSListIsEmpty parameter.
*/
protected final String _Fail = "Fail";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";

/**
Constructor.
*/
public TableTimeSeriesMath_Command ()
{   super();
    setCommandName ( "TableTimeSeriesMath" );
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
{   String Operator = parameters.getValue ( "Operator" );
    String TableID = parameters.getValue ( "TableID" );
    String TableTSIDColumn = parameters.getValue ( "TableTSIDColumn" );
    String TableInputColumn = parameters.getValue ( "TableInputColumn" );
    String IfTableInputIsBlank = parameters.getValue ( "IfTableInputIsBlank" );
    String IfTSListIsEmpty = parameters.getValue ( "IfTSListIsEmpty" );
    String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (Operator == null) || Operator.equals("") ) {
        message = "The operator must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the operator to process input." ) );
    }
    else {
        // Make sure that the operator is known in general
        boolean supported = false;
        DataTableMathOperatorType operatorType = null;
        try {
            operatorType = DataTableMathOperatorType.valueOfIgnoreCase(Operator);
            supported = true;
        }
        catch ( Exception e ) {
            message = "The operator (" + Operator + ") is not recognized.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Select a supported operator using the command editor." ) );
        }
        
        // Make sure that it is in the supported list
        
        if ( supported ) {
            supported = false;
            List<DataTableMathOperatorType> operators = getOperatorChoices();
            for ( int i = 0; i < operators.size(); i++ ) {
                if ( operatorType == operators.get(i) ) {
                    supported = true;
                }
            }
            if ( !supported ) {
                message = "The operator (" + Operator + ") is not supported by this command.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Select a supported operator using the command editor." ) );
            }
        }
    }
    
    if ( (TableID == null) || TableID.equals("") ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the identifier for the table to process." ) );
    }
    
    if ( (TableTSIDColumn == null) || TableTSIDColumn.equals("") ) {
        message = "The TableTSID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a table column name for the TSID." ) );
    }
    
    if ( (TableInputColumn == null) || TableInputColumn.equals("") ) {
        message = "The table input column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a table column name for the input value." ) );
    }
    
    if ( (IfTableInputIsBlank != null) && !IfTableInputIsBlank.equals("") &&
        !IfTableInputIsBlank.equalsIgnoreCase(_Ignore) &&  !IfTableInputIsBlank.equalsIgnoreCase(_Fail) &&
        !IfTableInputIsBlank.equalsIgnoreCase(_Warn) ) {
        message = "The IfTableInputIsBlank value (" + IfTableInputIsBlank + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _Ignore + ", " + _Warn + ", or " + _Fail + "." ) );   
    }
    
    if ( (IfTSListIsEmpty != null) && !IfTSListIsEmpty.equals("") &&
        !IfTSListIsEmpty.equalsIgnoreCase(_Ignore) &&  !IfTSListIsEmpty.equalsIgnoreCase(_Fail) &&
        !IfTSListIsEmpty.equalsIgnoreCase(_Warn) ) {
        message = "The IfTSListIsEmpty value (" + IfTSListIsEmpty + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _Ignore + ", " + _Warn + ", or " + _Fail + "." ) );   
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(10);
    validList.add ( "TSList" );
    validList.add ( "TSID" );
    validList.add ( "EnsembleID" );
    validList.add ( "Operator" );
    validList.add ( "TableID" );
    validList.add ( "TableTSIDColumn" );
    validList.add ( "TableTSIDFormat" );
    validList.add ( "TableInputColumn" );
    validList.add ( "IfTableInputIsBlank" );
    validList.add ( "IfTSListIsEmpty" );
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
    return (new TableTimeSeriesMath_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Get the list of operators that can be used.
*/
public List<DataTableMathOperatorType> getOperatorChoices()
{
    List<DataTableMathOperatorType> choices = new Vector<DataTableMathOperatorType>();
    choices.add ( DataTableMathOperatorType.ASSIGN );
    choices.add ( DataTableMathOperatorType.ADD );
    choices.add ( DataTableMathOperatorType.SUBTRACT );
    choices.add ( DataTableMathOperatorType.MULTIPLY );
    choices.add ( DataTableMathOperatorType.DIVIDE );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public List<String> getOperatorChoicesAsStrings()
{
    List<DataTableMathOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new Vector<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

// Parse command is in the base class

/**
Method to execute the command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   String message, routine = getClass().getSimpleName() + ".runCommand";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;
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

    String TSList = parameters.getValue ( "TSList" );
    if ( (TSList == null) || TSList.equals("") ) {
        TSList = TSListType.ALL_TS.toString();
    }
    String TSID = parameters.getValue ( "TSID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TSID != null) && (TSID.indexOf("${") >= 0) ) {
		TSID = TSCommandProcessorUtil.expandParameterValue(processor, this, TSID);
	}
    String EnsembleID = parameters.getValue ( "EnsembleID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (EnsembleID != null) && (EnsembleID.indexOf("${") >= 0) ) {
		EnsembleID = TSCommandProcessorUtil.expandParameterValue(processor, this, EnsembleID);
	}
    String Operator = parameters.getValue ( "Operator" );
    DataTableMathOperatorType operator = DataTableMathOperatorType.valueOfIgnoreCase(Operator);
    String TableID = parameters.getValue ( "TableID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
    String TableTSIDColumn = parameters.getValue ( "TableTSIDColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableTSIDColumn != null) && (TableTSIDColumn.indexOf("${") >= 0) ) {
		TableTSIDColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, TableTSIDColumn);
	}
    String TableTSIDFormat = parameters.getValue ( "TableTSIDFormat" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableTSIDFormat != null) && (TableTSIDFormat.indexOf("${") >= 0) ) {
		TableTSIDFormat = TSCommandProcessorUtil.expandParameterValue(processor, this, TableTSIDFormat);
	}
    String TableInputColumn = parameters.getValue ( "TableInputColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableInputColumn != null) && (TableInputColumn.indexOf("${") >= 0) ) {
		TableInputColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, TableInputColumn);
	}
    String IfTableInputIsBlank = parameters.getValue ( "IfTableInputIsBlank" );
    if ( (IfTableInputIsBlank == null) || IfTableInputIsBlank.equals("") ) {
        IfTableInputIsBlank = _Warn;
    }
    String IfTSListIsEmpty = parameters.getValue ( "IfTSListIsEmpty" );
    if ( (IfTSListIsEmpty == null) || IfTSListIsEmpty.equals("") ) {
        IfTSListIsEmpty = _Warn;
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
    
    // Get the column from the table to be used as input...
    
    int tableInputColumn = -1;
    try {
        tableInputColumn = table.getFieldIndex(TableInputColumn);
    }
    catch ( Exception e2 ) {
        message = "Table \"" + TableID + "\" does not have column \"" + TableInputColumn + "\".";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that a table exists with the requested input column." ) );
    }
    
    // Get the time series to process.  Allow TSID to be a pattern or specific time series...

    request_params = new PropList ( "" );
    request_params.set ( "TSList", TSList );
    request_params.set ( "TSID", TSID );
    request_params.set ( "EnsembleID", EnsembleID );
    try {
        bean = processor.processRequest( "GetTimeSeriesToProcess", request_params);
    }
    catch ( Exception e ) {
        message = "Error requesting GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\", TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\") from processor.";
        Message.printWarning(log_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report the problem to software support." ) );
    }
    if ( bean == null ) {
        Message.printStatus ( 2, routine, "Bean is null.");
    }
    PropList bean_PropList = bean.getResultsPropList();
    Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
    List<TS> tslist = null;
    int nts = 0;
    if ( o_TSList == null ) {
        message = "Null TSToProcessList returned from processor for GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
        Message.printWarning ( log_level, MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
            "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
    }
    else {
    	@SuppressWarnings("unchecked")
		List<TS> tslist0 = (List<TS>)o_TSList;
        tslist = tslist0;
        nts = tslist.size();
    }
    
    if ( nts == 0 ) {
        message = "Unable to find time series to process using TSList=\"" + TSList + "\" TSID=\"" + TSID +
            "\", EnsembleID=\"" + EnsembleID + "\".";
        if ( IfTSListIsEmpty.equalsIgnoreCase(_Warn) ) {
            Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING, message,
                "Verify that the TSID parameter matches one or more time series - " +
                "may be OK for partial run or special case." ) );
        }
        else if ( IfTSListIsEmpty.equalsIgnoreCase(_Fail) ) {
            Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the TSID parameter matches one or more time series - " +
                "may be OK for partial run or special case." ) );
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

    try {
        TS ts = null;
        Object o_ts = null;
        Object tableObject; // The table value as a generic object
        Double tableValue; // The value used to perform math
        for ( int its = 0; its < nts; its++ ) {
            // The the time series to process, from the list that was returned above.
            o_ts = tslist.get(its);
            if ( o_ts == null ) {
                message = "Time series to process is null.";
                Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                    "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
                // Go to next time series.
                continue;
            }
            ts = (TS)o_ts;
            
            try {
                // Get the value from the table
                // See if a matching row exists using the specified TSID column...
                String tsid = null;
                if ( (TableTSIDFormat != null) && !TableTSIDFormat.equals("") ) {
                    // Format the TSID using the specified format
                    tsid = ts.formatLegend ( TableTSIDFormat );
                }
                else {
                    // Use the alias if available and then the TSID
                    tsid = ts.getAlias();
                    if ( (tsid == null) || tsid.equals("") ) {
                        tsid = ts.getIdentifierString();
                    }
                }
                TableRecord rec = table.getRecord ( TableTSIDColumn, tsid );
                if ( rec == null ) {
                    message = "Cannot find table cell in column \"" + TableTSIDColumn +
                    "\" matching TSID formatted as \"" + tsid + "\" - skipping time series \"" +
                    ts.getIdentifierString() + "\".";
                    if ( IfTableInputIsBlank.equalsIgnoreCase(_Warn) ) {
                        Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING, message,
                        "Verify that the column TSID matches one or more time series." ) );
                    }
                    else if ( IfTableInputIsBlank.equalsIgnoreCase(_Fail) ) {
                        Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                        "Verify that the column TSID matches one or more time series." ) );
                    }
                    // Go to next time series.
                    continue;
                }
                // Get the value from the table...
                tableObject = rec.getFieldValue(tableInputColumn);
                // Allow the value to be any number
                if ( tableObject == null ) {
                    message = "Table value in column \"" + TableInputColumn +
                    "\" matching TSID \"" + tsid + "\" is null - skipping time series \"" +
                    ts.getIdentifierString() + "\".";
                    Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                    status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                        "Verify that the proper table input column is specified and that column values are numbers." ) );
                    // Go to next time series.
                    continue;
                }
                else if ( !StringUtil.isDouble("" + tableObject) ) {
                    message = "Table value in column \"" + TableInputColumn +
                    "\" matching TSID \"" + tsid + "\" (" + tableObject +
                    ") is not a number - skipping time series \"" + ts.getIdentifierString() + "\".";
                    Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                    status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                        "Verify that the proper table input column is specified and that column values are numbers." ) );
                    // Go to next time series.
                    continue;
                }
                else {
                    // Have a numerical value, but re-parse in case the original is an Integer, etc.
                    tableValue = Double.parseDouble("" + tableObject );
                    if ( tableValue.isNaN() ) {
                        message = "Table value in column \"" + TableInputColumn +
                        "\" matching TSID \"" + tsid + "\" is NaN - skipping time series \"" +
                        ts.getIdentifierString() + "\".";
                        Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                            "Verify that the proper table input column is specified and that column values are numbers." ) );
                        // Go to next time series.
                        continue;
                    }
                }
                // TODO SAM 2012-01-13 add analysis start and end to command parameters 
                // Do the calculation...
                if ( operator == DataTableMathOperatorType.ADD ) {
                    ts.addToGenesis("Table \"" + TableID + "\" column \"" + TableInputColumn +
                        "\" value " + tableValue + " used for add." );
                    TSUtil.addConstant(ts, null, null, tableValue);
                }
                else if ( (operator == DataTableMathOperatorType.ASSIGN) && (tableValue != 0.0) ) {
                    ts.addToGenesis("Table \"" + TableID + "\" column \"" + TableInputColumn +
                        "\" value " + tableValue + " assigned to time series." );
                    TSUtil.setConstant(ts, null, null, tableValue);
                }
                else if ( (operator == DataTableMathOperatorType.DIVIDE) && (tableValue != 0.0) ) {
                    ts.addToGenesis("Table \"" + TableID + "\" column \"" + TableInputColumn +
                        "\" value " + tableValue + " used for divide (multiply 1/" + tableValue + "." );
                    TSUtil.scale(ts, null, null, 1.0/tableValue);
                }
                else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
                    ts.addToGenesis("Table \"" + TableID + "\" column \"" + TableInputColumn +
                        "\" value " + tableValue + " used for multiply." );
                    TSUtil.scale(ts, null, null, tableValue);
                }
                else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
                    ts.addToGenesis("Table \"" + TableID + "\" column \"" + TableInputColumn +
                        "\" value " + tableValue + " used for subtract." );
                    TSUtil.addConstant(ts, null, null, -tableValue);
                }
            }
            catch ( Exception e ) {
                message = "Unexpected error processing time series \""+ ts.getIdentifier() + " (" + e + ").";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                Message.printWarning(3,routine,e);
                status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details - report the problem to software support." ) );
            }
        }
    }
    catch ( Exception e ) {
        message = "Unexpected error processing time series (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check log file for details." ) );
        throw new CommandException ( message );
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
    
    String TSList = parameters.getValue( "TSList" );
    String TSID = parameters.getValue( "TSID" );
    String EnsembleID = parameters.getValue( "EnsembleID" );
    String Operator = parameters.getValue( "Operator" );
    String TableID = parameters.getValue( "TableID" );
    String TableTSIDColumn = parameters.getValue ( "TableTSIDColumn" );
    String TableTSIDFormat = parameters.getValue ( "TableTSIDFormat" );
    String TableInputColumn = parameters.getValue ( "TableInputColumn" );
    String IfTableInputIsBlank = parameters.getValue ( "IfTableInputIsBlank" );
    String IfTSListIsEmpty = parameters.getValue ( "IfTSListIsEmpty" );
        
    StringBuffer b = new StringBuffer ();

    if ( (TSList != null) && (TSList.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TSList=" + TSList );
    }
    if ( (TSID != null) && (TSID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TSID=\"" + TSID + "\"" );
    }
    if ( (EnsembleID != null) && (EnsembleID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "EnsembleID=\"" + EnsembleID + "\"" );
    }
    if ( (Operator != null) && (Operator.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Operator=\"" + Operator + "\"" );
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (TableTSIDColumn != null) && (TableTSIDColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableTSIDColumn=\"" + TableTSIDColumn + "\"" );
    }
    if ( (TableTSIDFormat != null) && (TableTSIDFormat.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableTSIDFormat=\"" + TableTSIDFormat + "\"" );
    }
    if ( (TableInputColumn != null) && (TableInputColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableInputColumn=\"" + TableInputColumn + "\"" );
    }
    if ( (IfTableInputIsBlank != null) && (IfTableInputIsBlank.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IfTableInputIsBlank=" + IfTableInputIsBlank );
    }
    if ( (IfTSListIsEmpty != null) && (IfTSListIsEmpty.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IfTSListIsEmpty=" + IfTSListIsEmpty );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

}
