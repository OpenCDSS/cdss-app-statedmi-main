// CheckStateCU_Command - This class initializes, checks, and runs the Check*() commands for StateCU data.

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

import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_ComponentValidation;
import DWR.StateCU.StateCU_ComponentValidator;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Data;
import DWR.StateCU.StateCU_DataSet;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Location_ParcelValidator;
import DWR.StateCU.StateCU_PenmanMonteith;
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
This class initializes, checks, and runs the Check*() commands for StateCU data.
It should be extended and the command name set in derived classes.
*/
public abstract class CheckStateCU_Command extends AbstractCommand implements Command
{
	
/**
Values for DeepCheck parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public CheckStateCU_Command ()
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
{	String routine = getClass().getSimpleName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String DeepCheck = parameters.getValue ( "DeepCheck" );
	String AreaPrecision = parameters.getValue ( "AreaPrecision" );
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

	if ( (AreaPrecision != null) && !AreaPrecision.isEmpty() && !StringUtil.isInteger(AreaPrecision) ) {
		message = "The AreaPrecision value (" + AreaPrecision + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify AreaPrecision as an integer.") );
	}

	if ( (DeepCheck != null) && (DeepCheck.length() > 0) && !DeepCheck.equalsIgnoreCase(_False) &&
		!DeepCheck.equalsIgnoreCase(_True) ) {
		message = "The DeepCheck value (" + DeepCheck + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify DeepCheck as " + _False + " (default), or " + _True + ".") );
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
	List<String> validList = new ArrayList<>(4);
    validList.add ( "ID" );
    validList.add ( "AreaPrecision" );
    validList.add ( "DeepCheck" );
    validList.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
{	return (new CheckStateCU_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
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
	int areaPrecision = 3; // Default
	String AreaPrecision = parameters.getValue ( "AreaPrecision" );
	if ( (AreaPrecision != null) && !AreaPrecision.isEmpty() ) {
		areaPrecision = Integer.parseInt(AreaPrecision);
	}
	String DeepCheck = parameters.getValue ( "DeepCheck" );
	boolean deepCheck = false; // Default
	if ( (DeepCheck != null) && DeepCheck.equalsIgnoreCase("true") ) {
		deepCheck = true;
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of data to check...
	
	List<? extends StateCU_Data> dataList = null; // TODO SAM 2009-05-05 evaluate whether specific lists are needed
	
	if ( this instanceof CheckBlaneyCriddle_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_BlaneyCriddle> objectList = (List<StateCU_BlaneyCriddle>)processor.getPropContents("StateCU_BlaneyCriddle_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting Blaney-Criddle crop coefficients from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}

	if ( this instanceof CheckClimateStations_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_ClimateStation> objectList = (List<StateCU_ClimateStation>)processor.getPropContents("StateCU_ClimateStation_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting climate stations from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	if ( this instanceof CheckCropCharacteristics_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_CropCharacteristics> objectList = (List<StateCU_CropCharacteristics>)processor.getPropContents("StateCU_CropCharacteristics_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting crop characteristics from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	if ( this instanceof CheckCropPatternTS_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_CropPatternTS> objectList = (List<StateCU_CropPatternTS>)processor.getPropContents("StateCU_CropPatternTS_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting crop pattern time series from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	if ( (this instanceof CheckCULocations_Command) || (this instanceof CheckParcels_Command) ) {
		// Parcels are accessed via CU Locations
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_Location> objectList = (List<StateCU_Location>)processor.getPropContents("StateCU_Location_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting CU location list from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}

	if ( this instanceof CheckIrrigationPracticeTS_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_IrrigationPracticeTS> objectList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents("StateCU_IrrigationPracticeTS_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting irrigation practice time series from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}

	if ( this instanceof CheckPenmanMonteith_Command ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateCU_PenmanMonteith> objectList = (List<StateCU_PenmanMonteith>)processor.getPropContents("StateCU_PenmanMonteith_List");
			dataList = objectList;
		}
		catch ( Exception e ) {
			message = "Error requesting Penman-Monteith crop coefficients from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}

	// Get the data set for cross-checks between components...
	
	StateCU_DataSet dataset = null;
	try {
		dataset = (StateCU_DataSet)processor.getPropContents("StateCU_DataSet");
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU data set from processor for cross checks.";
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
		StateCU_Data data = null;
		String id; // Identifier for object, to use for wildcard matching
		Message.printStatus(2, routine, "Checking " + dataListSize + " objects.");
		for ( int i = 0; i < dataListSize; i++ ) {
			notifyCommandProgressListeners ( i, dataListSize, (float)((i + 1)/((float)dataListSize)*100.0),
				"Checking data object " + i + " of " + dataListSize );
			data = dataList.get(i);
			id = data.getID();
			if ( this instanceof CheckCropCharacteristics_Command ||
				this instanceof CheckBlaneyCriddle_Command || this instanceof CheckPenmanMonteith_Command) {
				// Use the name rather than ID
				id = data.getName();
			}
			if ( !id.matches(idpattern_Java) ) {
				continue;
			}
			++matchCount;
			// Check the object
			if ( data instanceof StateCU_ComponentValidator ) {
				StateCU_ComponentValidator validator = null;
				if ( this instanceof CheckParcels_Command ) {
					// Need to evaluate all parcels for a StateCU_Location, not just the individual parcel's data.
					validator = ((StateCU_Location)data).getParcelValidator( (List<StateCU_Location>)dataList, deepCheck, areaPrecision );
				}
				else {
					validator = (StateCU_ComponentValidator)data;
				}
				if ( i == 0 ) {
					// Extra check on full dataset for parcels - only need to do once so do for the first item and put at the top of output.
					if ( validator instanceof StateCU_Location_ParcelValidator ) {
						StateCU_ComponentValidation problems = ((StateCU_Location_ParcelValidator)validator).validateAllComponentData(dataset);
						int problemsSize = problems.size();
						if ( problemsSize > 0 ) {
							// Need to log all the problems at the command level
							for ( int iprob = 0; iprob < problemsSize; ++iprob ) {
								String problem = problems.get(iprob).getProblem();
								Message.printWarning(warning_level,
									MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, problem );
								status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
									problem, problems.get(iprob).getRecommendation() ) );
							}
						}
					}
				}
				StateCU_ComponentValidation problems = validator.validateComponent(dataset);
				int problemsSize = problems.size();
				if ( problemsSize > 0 ) {
					// Need to log all the problems at the command level
					for ( int iprob = 0; iprob < problemsSize; ++iprob ) {
						String problem = problems.get(iprob).getProblem();
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, problem );
						status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
							problem, problems.get(iprob).getRecommendation() ) );
					}
				}
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
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
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
	String AreaPrecision = parameters.getValue ( "AreaPrecision" );
	String DeepCheck = parameters.getValue ( "DeepCheck" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( AreaPrecision != null && AreaPrecision.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AreaPrecision=" + AreaPrecision );
	}
	if ( DeepCheck != null && DeepCheck.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DeepCheck=" + DeepCheck );
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
