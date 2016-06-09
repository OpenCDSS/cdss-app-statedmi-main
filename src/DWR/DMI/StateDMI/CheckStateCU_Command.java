package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_ComponentValidation;
import DWR.StateCU.StateCU_ComponentValidator;
import DWR.StateCU.StateCU_Data;
import DWR.StateCU.StateCU_DataSet;

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
This class initializes, checks, and runs the Check*() commands for StateCU data.
It should be extended and the command name set in derived classes.
</p>
*/
public abstract class CheckStateCU_Command extends AbstractCommand implements Command
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
	List valid_Vector = new Vector();
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
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of data to check...
	
	List<? extends StateCU_Data> dataList = null; // TODO SAM 2009-05-05 evaluate whether specific lists are needed
	
	if ( this instanceof CheckBlaneyCriddle_Command ) {
		try {
			dataList = (List)processor.getPropContents ( "StateCU_BlaneyCriddle_List");
		}
		catch ( Exception e ) {
			message = "Error requesting Blaney-Criddle crop coefficients from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}

	if ( this instanceof CheckPenmanMonteith_Command ) {
		try {
			dataList = (List)processor.getPropContents ( "StateCU_PenmanMonteith_List");
		}
		catch ( Exception e ) {
			message = "Error requesting Penman-Monteith crop coefficients from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	if ( this instanceof CheckClimateStations_Command ) {
		try {
			dataList = (List)processor.getPropContents ( "StateCU_ClimateStation_List");
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
			dataList = (List)processor.getPropContents ( "StateCU_CropCharacteristics_List");
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
			dataList = (List)processor.getPropContents ( "StateCU_CropPatternTS_List");
		}
		catch ( Exception e ) {
			message = "Error requesting crop pattern time series from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	if ( this instanceof CheckCULocations_Command ) {
		try {
			dataList = (List)processor.getPropContents ( "StateCU_Location_List");
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
			dataList = (List)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
		}
		catch ( Exception e ) {
			message = "Error requesting irrigation practice time series from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		}
	}
	
	// Get the data set for cross-checks between components...
	
	StateCU_DataSet dataset = null;
	try {
		dataset = (StateCU_DataSet)processor.getPropContents ( "StateCU_DataSet");
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
			data = dataList.get(i);
			id = data.getID();
			if ( this instanceof CheckCropCharacteristics_Command ||
				this instanceof CheckBlaneyCriddle_Command || this instanceof CheckPenmanMonteith_Command) {
				// Use the name
				id = data.getName();
			}
			if ( !id.matches(idpattern_Java) ) {
				continue;
			}
			++matchCount;
			// Check the object
			if ( data instanceof StateCU_ComponentValidator ) {
				StateCU_ComponentValidator validator = (StateCU_ComponentValidator)data;
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