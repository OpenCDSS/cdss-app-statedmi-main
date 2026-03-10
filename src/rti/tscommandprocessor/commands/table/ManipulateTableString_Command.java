// ManipulateTableString_Command - This class initializes, checks, and runs the ManipulateTableString() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import DWR.DMI.StateDMI.StateDMI_Processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import RTi.Util.String.StringDictionary;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableStringManipulator;
import RTi.Util.Table.DataTableStringOperatorType;

/**
This class initializes, checks, and runs the ManipulateTableString() command.
*/
public class ManipulateTableString_Command extends AbstractCommand implements Command
{
	
	/**
	 * Values for UseEmptyStringForNullInput.
	 */
	protected final String _False = "False";
	protected final String _True = "True";

/**
Constructor.
*/
public ManipulateTableString_Command () {
    super();
    setCommandName ( "ManipulateTableString" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
    String TableID = parameters.getValue ( "TableID" );
    String InputColumn1 = parameters.getValue ( "InputColumn1" );
    String Operator = parameters.getValue ( "Operator" );
    String InputColumn2 = parameters.getValue ( "InputColumn2" );
    String InputValue2 = parameters.getValue ( "InputValue2" );
    String InputValue3 = parameters.getValue ( "InputValue3" );
    String UseEmptyStringForNullInput = parameters.getValue ( "UseEmptyStringForNullInput" );
    String OutputColumn = parameters.getValue ( "OutputColumn" );
    String warning = "";
    String message;

    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || TableID.equals("") ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the identifier for the table to process." ) );
    }

    if ( (InputColumn1 == null) || InputColumn1.equals("") ) {
        message = "Input column 1 must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide an input column 1 name." ) );
    }

    DataTableStringOperatorType operatorType = null;
    if ( (Operator == null) || Operator.equals("") ) {
        message = "The operator must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the operator to process input." ) );
    }
    else {
        // Make sure that the operator is known in general.
        boolean supported = false;
        try {
            operatorType = DataTableStringOperatorType.valueOfIgnoreCase(Operator);
            supported = true;
        }
        catch ( Exception e ) {
            message = "The operator (" + Operator + ") is not recognized.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Select a supported operator using the command editor." ) );
        }

        // Make sure that it is in the supported list.

        if ( supported ) {
            supported = false;
            List<DataTableStringOperatorType> operators = DataTableStringManipulator.getOperatorChoices();
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

        // Additional checks that depend on the operator.
        /* TODO SAM 2010-09-13 Add this later.
        if ( supported ) {
            int nRequiredValues = -1;
            try {
                TSUtil_CalculateTimeSeriesStatistic.getRequiredNumberOfValuesForStatistic ( operatorType );
            }
            catch ( Exception e ) {
                message = "Statistic \"" + operatorType + "\" is not recognized.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Contact software support." ) );
            }

            if ( nRequiredValues >= 1 ) {
                if ( (Input1 == null) || Input1.equals("") ) {
                    message = "Value1 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value1." ) );
                }
                else if ( !StringUtil.isDouble(Input1) ) {
                    message = "Value1 (" + Input1 + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value1 as a number." ) );
                }
            }

            if ( nRequiredValues >= 2 ) {
                if ( (Input2 == null) || Input2.equals("") ) {
                    message = "Value2 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value2." ) );
                }
                else if ( !StringUtil.isDouble(Input2) ) {
                    message = "Value2 (" + Input2 + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value2 as a number." ) );
                }
            }

            if ( nRequiredValues == 3 ) {
                if ( (Output == null) || Output.equals("") ) {
                    message = "Value3 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value3." ) );
                }
                else if ( !StringUtil.isDouble(Input2) ) {
                    message = "Value3 (" + Output + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value3 as a number." ) );
                }
            }

            if ( nRequiredValues > 3 ) {
                message = "A maximum of 3 values are supported as input to statistic computation.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Refer to documentation for statistic.  Contact software support if necessary." ) );
            }
        }*/
    }

    if ( (operatorType != null) &&
    	(operatorType != DataTableStringOperatorType.COPY) &&
    	(operatorType != DataTableStringOperatorType.TO_BOOLEAN) &&
    	(operatorType != DataTableStringOperatorType.TO_DOUBLE) &&
        (operatorType != DataTableStringOperatorType.TO_INTEGER) &&
        (operatorType != DataTableStringOperatorType.TO_LONG) &&
        (operatorType != DataTableStringOperatorType.TO_DATE) &&
        (operatorType != DataTableStringOperatorType.TO_DATE_TIME) &&
        (operatorType != DataTableStringOperatorType.TO_LOWERCASE) &&
        (operatorType != DataTableStringOperatorType.TO_MIXEDCASE) &&
        (operatorType != DataTableStringOperatorType.TO_UPPERCASE) ) {
        if ( ((InputColumn2 == null) || InputColumn2.equals("")) &&
            ((InputValue2 == null) || InputValue2.equals(""))) {
            message = "Either InputColumn2 or InputValue2 MUST be specified.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Provide a column name or string constant as Input2." ) );
        }
    }

    if ( (operatorType != null) && (operatorType == DataTableStringOperatorType.REPLACE)) {
        // Must have input column 2 or value 2, and value 3.
        if ( ((InputColumn2 == null) || InputColumn2.equals("")) &&
            ((InputValue2 == null) || InputValue2.equals(""))) {
            message = "Either InputColumn2 or InputValue2 MUST be specified.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Provide a column name or string constant as Input2." ) );
        }
        if ( (InputValue3 == null) || InputValue3.equals("") ) {
            message = "InputValue3 MUST be specified.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Provide a string constant as InputValue3." ) );
        }
    }

    if ( ((InputColumn2 != null) && !InputColumn2.isEmpty()) &&
        ((InputValue2 != null) && !InputValue2.equals(""))) {
        message = "Either InputColumn2 or InputValue2 MUST be specified (but not both).";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a column name or string constant as Input2." ) );
    }

    if ( (UseEmptyStringForNullInput != null) && !UseEmptyStringForNullInput.isEmpty() ) {
    	if ( !UseEmptyStringForNullInput.equalsIgnoreCase(this._False) &&
    		!UseEmptyStringForNullInput.equalsIgnoreCase(this._True)  ) {
    		message = "The value for UseEmptyStringForNullInput is invalid.";
    		warning += "\n" + message;
    		status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
    			message, "Specify as " + this._False + " (default) or " + this._True + ".") );
    	}
    }

    // Check for invalid parameters.
    List<String> validList = new ArrayList<>(10);
    validList.add ( "TableID" );
    validList.add ( "ColumnIncludeFilters" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "InputColumn1" );
    validList.add ( "Operator" );
    validList.add ( "InputColumn2" );
    validList.add ( "InputValue2" );
    validList.add ( "InputValue3" );
    validList.add ( "UseEmptyStringForNullInput" );
    validList.add ( "OutputColumn" );
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
public boolean editCommand ( JFrame parent ) {
    List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    return (new ManipulateTableString_JDialog ( parent, this, tableIDChoices )).ok();
}

// Parse command is in the base class.

/**
Method to execute the command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    String message, routine = getClass().getSimpleName() + ".runCommand";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    //int log_level = 3;  // Level for non-use messages for log file.

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    Boolean clearStatus = Boolean.TRUE; // Default.
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen.
    }
    if ( clearStatus ) {
		status.clearLog(CommandPhaseType.RUN);
	}
    PropList parameters = getCommandParameters();

    // Get the input parameters.

    String TableID = parameters.getValue ( "TableID" );
    if ( commandPhase == CommandPhaseType.RUN ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    StringDictionary columnIncludeFilters = new StringDictionary(ColumnIncludeFilters,":",",");
    // Expand the filter information.
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnIncludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            map.remove(key);
        }
    }
    String ColumnExcludeFilters = parameters.getValue ( "ColumnExcludeFilters" );
    StringDictionary columnExcludeFilters = new StringDictionary(ColumnExcludeFilters,":",",");
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            map.remove(key);
        }
    }
    String InputColumn1 = parameters.getValue ( "InputColumn1" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	InputColumn1 = TSCommandProcessorUtil.expandParameterValue(processor, this, InputColumn1);
    }
    String Operator = parameters.getValue ( "Operator" );
    DataTableStringOperatorType operator = DataTableStringOperatorType.valueOfIgnoreCase(Operator);
    String InputColumn2 = parameters.getValue ( "InputColumn2" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	InputColumn2 = TSCommandProcessorUtil.expandParameterValue(processor, this, InputColumn2);
    }
    String InputValue2 = parameters.getValue ( "InputValue2" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	InputValue2 = TSCommandProcessorUtil.expandParameterValue(processor, this, InputValue2);
    }
    String InputValue3 = parameters.getValue ( "InputValue3" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	InputValue3 = TSCommandProcessorUtil.expandParameterValue(processor, this, InputValue3);
    }
    String UseEmptyStringForNullInput = parameters.getValue ( "UseEmptyStringForNullInput" );
    boolean useEmptyStringForNullInput = false; // Default.
    if ( (UseEmptyStringForNullInput != null) && UseEmptyStringForNullInput.equalsIgnoreCase(this._True) ) {
    	useEmptyStringForNullInput = true;
    }
    String OutputColumn = parameters.getValue ( "OutputColumn" );
    if ( commandPhase == CommandPhaseType.RUN ) {
    	OutputColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, OutputColumn);
    }

    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( (TableID != null) && !TableID.equals("") ) {
        // Get the table to be updated.
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
        // Input error.
        message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
        Message.printWarning(3, routine, message );
        throw new CommandException ( message );
    }

    // Now process.

    List<String> problems = new ArrayList<>();
    try {
        DataTableStringManipulator dtm = new DataTableStringManipulator ( table, columnIncludeFilters, columnExcludeFilters );
        dtm.manipulate ( InputColumn1, operator, InputColumn2, InputValue2, InputValue3, useEmptyStringForNullInput, OutputColumn, problems );
    }
    catch ( Exception e ) {
        message = "Unexpected error performing table string manipulation (" + e + ").";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check log file for details." ) );
        throw new CommandException ( message );
    }

    int MaxWarnings_int = 500; // Limit the problems to 500 to prevent command overload.
    int problemsSize = problems.size();
    int problemsSizeOutput = problemsSize;
    String ProblemType = "ManipulateTableString";
    if ( (MaxWarnings_int > 0) && (problemsSize > MaxWarnings_int) ) {
        // Limit the warnings to the maximum.
        problemsSizeOutput = MaxWarnings_int;
    }
    if ( problemsSizeOutput < problemsSize ) {
        message = "Performing table string manipulation had " + problemsSize + " warnings - only " + problemsSizeOutput + " are listed.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
        // No recommendation since it is a user-defined check.
        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
    }
    for ( int iprob = 0; iprob < problemsSizeOutput; iprob++ ) {
        message = problems.get(iprob);
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
        // No recommendation since it is a user-defined check.
        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
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
@param parameters to include in the command
@return the string representation of the command
*/
public String toString ( PropList parameters ) {
	String [] parameterOrder = {
    	"TableID",
		"ColumnIncludeFilters",
		"ColumnExcludeFilters",
    	"InputColumn1",
    	"Operator",
    	"InputColumn2",
    	"InputValue2",
    	"InputValue3",
    	"UseEmptyStringForNullInput",
    	"OutputColumn"
	};
	return this.toString(parameters, parameterOrder);
}

}