// SetInstreamFlowDemandTSFromRights_Command - This class initializes, checks, and runs the SetInstreamFlowDemandTS*FromRights() commands.

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

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_InstreamFlowRight;

import RTi.TS.MonthTS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.YearType;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the SetInstreamFlowDemandTS*FromRights() commands.
It is an abstract base class that must be controlled via a derived class in anticipation of additional
time series being added.  For example,
the SetInstreamFlowDemandTSAverageMonthlyFromRights() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
*/
public abstract class SetInstreamFlowDemandTSFromRights_Command extends AbstractCommand implements Command
{
	
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
public SetInstreamFlowDemandTSFromRights_Command ()
{	super();
	setCommandName ( "SetInstreamFlowDemandTS?FromRights" );
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
{	//String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The instream flow station ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the instream flow station ID to process." ) );
	}

	// Include the Add option
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Add) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
						", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(2);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
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
{	// The command will be modified if changed...
	return (new SetInstreamFlowDemandTSFromRights_JDialog ( parent, this )).ok();
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
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	// SetStart and SetEnd processed in checkCommandParameters()
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List<MonthTS> tsList = null;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // Used to look up time series metadata
    try {
		if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyFromRights_Command ) {
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_InstreamFlowDemandTSAverageMonthly_List" );
			tsList = dataList;
			compType = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY;
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting time series data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    List<StateMod_InstreamFlowRight> rightList = null;
    int rightListSize = 0;
    try {
		@SuppressWarnings("unchecked")
		List<StateMod_InstreamFlowRight> dataList = (List<StateMod_InstreamFlowRight>)processor.getPropContents ( "StateMod_InstreamFlowRight_List" );
		rightList = dataList;
    	rightListSize = rightList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting water right data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Output period is year 0 specified with SetStart and SetEnd
    
    DateTime OutputStart_DateTime = null;
    DateTime OutputEnd_DateTime = null;
	
	// Output year type is needed to make sure that the output period is OK.
	
	YearType outputYearType = YearType.CALENDAR;
    try {
	   outputYearType = (YearType)processor.getPropContents ( "OutputYearType");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputYearType (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyFromRights_Command ) {
		// Dates are year zero because average values, not a full time series
		OutputStart_DateTime = new DateTime(DateTime.PRECISION_MONTH|DateTime.DATE_ZERO);
		OutputEnd_DateTime = new DateTime(DateTime.PRECISION_MONTH|DateTime.DATE_ZERO);
		OutputEnd_DateTime.setMonth(12);
		if (outputYearType == YearType.WATER ){
			OutputStart_DateTime.setMonth ( 10 );
			OutputEnd_DateTime.setYear ( 1 );
			OutputEnd_DateTime.setMonth ( 9 );
		}
		else if (outputYearType == YearType.NOV_TO_OCT ){
			OutputStart_DateTime.setMonth ( 11 );
			OutputEnd_DateTime.setYear ( 1 );
			OutputEnd_DateTime.setMonth ( 10 );
		}
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Get the set start and end.

    	DateTime SetStart_DateTime = null;
    	if ( OutputStart_DateTime != null ) {
    		SetStart_DateTime = new DateTime(OutputStart_DateTime);
    	}
    	if ( SetStart_DateTime != null ) {
    		SetStart_DateTime.setPrecision ( DateTime.PRECISION_MONTH );
    	}
    	DateTime SetEnd_DateTime = null;
    	if ( OutputEnd_DateTime != null ) {
    		SetEnd_DateTime = new DateTime(OutputEnd_DateTime);
    	}
    	if ( SetEnd_DateTime != null ) {
    		SetEnd_DateTime.setPrecision ( DateTime.PRECISION_MONTH );
    	}

    	StateMod_InstreamFlowRight ifr = null;
    	String id = "";
    	int matchCount = 0;
    	MonthTS ts = null;	// Time series to add
    	TSIdent tsident = null;	// Time series identifier

    	// In the following, make sure that if there are more than one water
    	// right per location that only one time series is created.  Rather than
    	// do a bunch of book-keeping, just add the 2nd, 3rd, etc. right to the
    	// time series as a constant.

    	int pos = 0;
    	Message.printStatus(2, routine,
    		"Checking " + rightListSize + " instream flow water rights against ID pattern.");
    	for (int i = 0; i < rightListSize; i++) {
    		ifr = (StateMod_InstreamFlowRight)rightList.get(i);
    		// Get the instream flow station corresponding to the right...
    		id = ifr.getCgoto();
    		if ( !id.matches(idpattern_Java) ) {
    			// Station identifier does not match that requested...
    			continue;
    		}
    		// Have a match so create (or add to) a demand time series...
    		if ( compType == StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY ) {
    			// See if the time series already exists...
    			pos = TSUtil.indexOf ( tsList, id, "Location", 0 );
    			if ( pos >= 0 ) {
    				// Found the time series - just increment the time series value...
    				Message.printStatus ( 2, routine,
    					"Setting " + id + " demand TS -> adding " + ifr.getIrtem() );
    				ts = (MonthTS)tsList.get(pos);
    				// Set for the entire period since it is an average monthly time series.
    				TSUtil.addConstant ( ts, null, null, ifr.getDcrifr() );
    				++matchCount;
    			}
    			else if ( IfNotFound.equals(_Add) ) {
    				// Create a new time series...
    				Message.printStatus ( 2, routine,
    				"Adding new time instream flow (average monthly) time series for \"" + id + "\"" );
    				ts = new MonthTS();
    				tsident = new TSIdent (
    					ifr.getCgoto(),
    					"DWR",
    					StateMod_DataSet.
    					lookupTimeSeriesDataType (
    					compType ),
    					"Month",
    					"" );	// No scenario
    				ts.setIdentifier ( tsident );
    				// Average time series can use year zero but the
    				// water year must be reflected in the output...
    				DateTime date1=new DateTime();
    				date1.setYear ( 0 );
    				date1.setMonth ( 1 );
    				DateTime date2=new DateTime();
    				date2.setYear ( 0 );
    				date2.setMonth ( 12 );
    				if (outputYearType == YearType.WATER){
    					date1.setMonth ( 10 );
    					date2.setYear ( 1 );
    					date2.setMonth ( 9 );
    				}
    				else if (outputYearType == YearType.NOV_TO_OCT){
    					date1.setMonth ( 11 );
    					date2.setYear ( 1 );
    					date2.setMonth ( 10 );
    				}
    				ts.setDate1 ( date1 );
    				ts.setDate2 ( date2 );
    				ts.setDataUnits ( StateMod_DataSet.lookupTimeSeriesDataUnits ( compType));
    				ts.allocateDataSpace ( ifr.getDcrifr() );
    				tsList.add ( ts );
    				Message.printStatus ( 2, routine, "Setting " + id + " demand TS -> " + ifr.getDcrifr() );
    				// Increment the counter so the warning below is not generated
    				++matchCount;
    			}
    		}
    	}
    	if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct and if appropriate use IfNotFound=" + _Add) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID +	"\" was not matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct and if appropriate use IfNotFound=" + _Add) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
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

	String ID = parameters.getValue ( "ID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
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
