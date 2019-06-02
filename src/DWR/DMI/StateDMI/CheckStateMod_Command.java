// CheckStateMod_Command - This class initializes, checks, and runs the Check*() commands for StateMod data.

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

import DWR.StateMod.StateMod_ComponentValidation;
import DWR.StateMod.StateMod_ComponentValidator;
import DWR.StateMod.StateMod_Data;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;

import RTi.TS.TS;
import RTi.TS.MonthTS;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
<p>
This class initializes, checks, and runs the Check*() commands for StateMod data.
It should be extended and the command name set in derived classes.
</p>
*/
public abstract class CheckStateMod_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public CheckStateMod_Command ()
{	super();
	setCommandName ( "Check?" );
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
{	String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}

	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail + ", or " + _Warn +
				" (default).") );
	}

	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(2);
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
		throw new InvalidCommandParameterException ( warning );
	}
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	return (new CheckStateMod_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
@SuppressWarnings("unchecked")
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of data to check (can be either a list of StateMod data or a list of time series)...
	// DON'T USE ELSE BELOW because multiple blocks of code need to be executed.
	
	List<? extends StateMod_Data> dataList = null;
	List<MonthTS> tsList = null;
	if ( this instanceof CheckStreamGageStations_Command ) {
		try {
			dataList = (List<StateMod_StreamGage>)processor.getPropContents ( "StateMod_StreamGageStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting stream gage stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckDiversionStations_Command ||
			this instanceof CheckDiversionHistoricalTSMonthly_Command ||
			this instanceof CheckDiversionDemandTSMonthly_Command ) {
		try {
			dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting diversion stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckDiversionRights_Command ) {
		try {
			dataList = (List<StateMod_DiversionRight>)processor.getPropContents ( "StateMod_DiversionRight_List");
		}
		catch ( Exception e ) {
			message = "Error requesting diversion rights from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckDiversionHistoricalTSMonthly_Command ) {
		try {
			tsList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List");
		}
		catch ( Exception e ) {
			message = "Error requesting diversion historical time series (monthly) from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckDiversionDemandTSMonthly_Command ) {
		try {
			tsList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List");
		}
		catch ( Exception e ) {
			message = "Error requesting diversion demand time series (monthly) from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckInstreamFlowStations_Command ||
			this instanceof CheckInstreamFlowDemandTSAverageMonthly_Command ) {
		try {
			dataList = (List<StateMod_InstreamFlow>)processor.getPropContents ( "StateMod_InstreamFlowStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting instream flow stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckInstreamFlowRights_Command ) {
		try {
			dataList = (List<StateMod_InstreamFlowRight>)processor.getPropContents ( "StateMod_InstreamFlowRight_List");
		}
		catch ( Exception e ) {
			message = "Error requesting instream flow rights from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckInstreamFlowDemandTSAverageMonthly_Command ) {
		try {
			tsList = (List<MonthTS>)processor.getPropContents ( "StateMod_InstreamFlowDemandTSAverageMonthly_List");
		}
		catch ( Exception e ) {
			message = "Error requesting instream flow demand time series (averge monthly) from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckReservoirStations_Command ) {
		try {
			dataList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting reservoir stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckReservoirRights_Command ) {
		try {
			dataList = (List<StateMod_ReservoirRight>)processor.getPropContents ( "StateMod_ReservoirRight_List");
		}
		catch ( Exception e ) {
			message = "Error requesting reservoir rights from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckWellStations_Command ||
			this instanceof CheckWellHistoricalPumpingTSMonthly_Command ||
			this instanceof CheckWellDemandTSMonthly_Command ) {
		try {
			dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting well stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckWellRights_Command ) {
		try {
			dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List");
		}
		catch ( Exception e ) {
			message = "Error requesting well rights from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckWellHistoricalPumpingTSMonthly_Command ) {
		try {
			tsList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List");
		}
		catch ( Exception e ) {
			message = "Error requesting well historical pumping time series (monthly) from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckWellDemandTSMonthly_Command ) {
		try {
			tsList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List");
		}
		catch ( Exception e ) {
			message = "Error requesting well demand time series (monthly) from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckStreamEstimateStations_Command ) {
		try {
			dataList = (List<StateMod_StreamEstimate>)processor.getPropContents ( "StateMod_StreamEstimateStation_List");
		}
		catch ( Exception e ) {
			message = "Error requesting stream estimate stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckStreamEstimateCoefficients_Command ) {
		try {
			dataList = (List<StateMod_StreamEstimate_Coefficients>)processor.getPropContents ( "StateMod_StreamEstimateCoefficients_List");
		}
		catch ( Exception e ) {
			message = "Error requesting stream estimate coefficients from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	if ( this instanceof CheckRiverNetwork_Command ) {
		try {
			dataList = (List<StateMod_RiverNetworkNode>)processor.getPropContents ( "StateMod_RiverNetworkNode_List");
		}
		catch ( Exception e ) {
			message = "Error requesting river network nodes from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	
	// Get the data set for cross-checks between components...
	
	StateMod_DataSet dataset = null;
	try {
		dataset = (StateMod_DataSet)processor.getPropContents ( "StateMod_DataSet");
	}
	catch ( Exception e ) {
		message = "Error requesting StateMod data set from processor for cross checks.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Report problem to software support." ) );
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
		int dataListSize = 0;
		if ( dataList != null ) {
			dataListSize = dataList.size();
		}
		int matchCount = 0;
		StateMod_Data data = null;
		TS ts = null;
		String id; // Identifier for object, to use for wildcard matching
		Message.printStatus(2, routine, "Checking " + dataListSize + " objects.");
		StateMod_ComponentValidation problems; // Problems for an object
		int listSize = 0;
		if ( tsList != null ) {
			listSize = tsList.size();
		}
		else {
			listSize = dataListSize;
		}
		for ( int i = 0; i < listSize; i++ ) {
			if ( tsList != null ) {
				// Get a time series to check
				ts = tsList.get(i);
				id = ts.getLocation(); // Just use the location for matches
			}
			else {
				// Get a StateMod object to check
				data = dataList.get(i);
				id = data.getID();
			}
			if ( !id.matches(idpattern_Java) ) {
				continue;
			}
			++matchCount;
			// Check the object
			problems = null;
			if ( tsList != null ) {
				// Basic data check...
				problems = StateMod_Util.validateTimeSeries ( null, true, true, ts, dataList );
			}
			else if ( data instanceof StateMod_ComponentValidator ) {
				// Use the generic validator interface
				StateMod_ComponentValidator validator = (StateMod_ComponentValidator)data;
				problems = validator.validateComponent(dataset);
			}
			int problemsSize = 0;
			if ( problems != null ) {
				problemsSize = problems.size();
			}
			// Log all the problems at the command level
			for ( int iprob = 0; iprob < problemsSize; ++iprob ) {
				String problem = problems.get(iprob).getProblem();
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, problem );
				status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
					problem, problems.get(iprob).getRecommendation() ) );
			}
		}
		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not checking.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID + "\" was not matched: failing and not checking.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the identifier is correct." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error checking data (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Check log file for details." ) );
        throw new CommandException ( message );
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
	
	String ID = parameters.getValue ( "ID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
