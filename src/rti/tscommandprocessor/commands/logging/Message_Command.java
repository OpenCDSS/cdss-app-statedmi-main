package rti.tscommandprocessor.commands.logging;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
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
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the Message() command.
*/
public class Message_Command extends AbstractCommand implements Command
{

/**
Constructor.
*/
public Message_Command ()
{	super();
	setCommandName ( "Message" );
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
{	String routine = getCommandName() + "_checkCommandParameters";
	String ScreenLevel = parameters.getValue ( "ScreenLevel" );
	String LogFileLevel = parameters.getValue ( "LogFileLevel" );
	String warning = "";
	String message;
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (ScreenLevel != null) && !ScreenLevel.isEmpty() && !StringUtil.isInteger(ScreenLevel) ) {
        message = "The screen warning level \"" + ScreenLevel + "\" is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Change the screen level to an integer." ) );
    }
    if ( (LogFileLevel != null) && !LogFileLevel.isEmpty() && !StringUtil.isInteger(LogFileLevel) ) {
        message = "The log file warning level \"" + LogFileLevel + "\" is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Change the log file level to an integer." ) );
    }

	// Check for invalid parameters...
    List<String> validList = new ArrayList<String>(2);
	validList.add ( "Message" );
	validList.add ( "CommandStatus" );
	warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
	
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine,
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
	return (new Message_JDialog ( parent, this )).ok();
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
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
		status.clearLog(CommandPhaseType.RUN);
	}
	
	String Message2 = parameters.getValue ( "Message" );
	String CommandStatus = parameters.getValue ( "CommandStatus" );
	CommandStatusType cst = null;
	if ( (CommandStatus != null) && !CommandStatus.equals("") ) {
	    cst = CommandStatusType.parse(CommandStatus);
	}

	try {
	    // TODO SAM 2013-12-07 Evaluate whether to allow users to control debug/status/logging,
	    // but perhaps move to standard logging first
	    String messageExpanded = TSCommandProcessorUtil.expandParameterValue(
	            this.getCommandProcessor(),this,Message2);
	    Message.printStatus(2, routine, messageExpanded);
	    if ( cst != null ) {
	        // Add a command record message to trigger the status level
	        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(cst, messageExpanded, "Check the log file or command window for details." ) );
	    }
	}
	catch ( Exception e ) {
		message = "Unexpected error printing message.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Check the log file or command window for details." ) );
		throw new CommandException ( message );
	}
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{   if ( props == null ) {
        return getCommandName() + "()";
    }
    String Message = props.getValue( "Message" );
    String CommandStatus = props.getValue( "CommandStatus" );
    StringBuffer b = new StringBuffer ();
    if ( (Message != null) && (Message.length() > 0) ) {
        b.append ( "Message=\"" + Message + "\"" );
    }
    if ( (CommandStatus != null) && (CommandStatus.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CommandStatus=" + CommandStatus );
    }
    return getCommandName() + "(" + b.toString() + ")";
}

}