package rti.tscommandprocessor.commands.time;

import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.YearType;

/**
This class initializes, checks, and runs the SetOutputYearType() command.
*/
public class SetOutputYearType_Command extends AbstractCommand
implements Command
{

/**
Constructor.
*/
public SetOutputYearType_Command ()
{	super();
	setCommandName ( "SetOutputYearType" );
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
{	String OutputYearType = parameters.getValue ( "OutputYearType" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	YearType outputYearType = null;
	if ( (OutputYearType != null) && !OutputYearType.equals("") ) {
        try {
            outputYearType = YearType.valueOfIgnoreCase(OutputYearType);
        }
        catch ( Exception e ) {
        	outputYearType = null;
        }
        if ( outputYearType == null ) {
            message = "The output year type (" + OutputYearType + ") is invalid.";
            warning += "\n" + message;
            StringBuffer b = new StringBuffer();
            List<YearType> values = YearType.getYearTypeChoices();
            for ( YearType t : values ) {
                if ( b.length() > 0 ) {
                    b.append ( ", " );
                }
                b.append ( t.toString() );
            }
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message, "Valid values are:  " + b.toString() + "."));
        }
	}
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "OutputYearType" );
	warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new SetOutputYearType_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.  This method currently
supports old syntax and new parameter-based syntax.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{
    if ( (command_string.indexOf('=') > 0) || command_string.endsWith("()") ) {
        // Current syntax...
        super.parseCommand( command_string);
    }
    else {
        // TODO SAM 2008-07-08 This whole block of code needs to be
        // removed as soon as commands have been migrated to the new syntax.
    	List<String> v = StringUtil.breakStringList(command_string, "(),", StringUtil.DELIM_ALLOW_STRINGS );
        int ntokens = 0;
        if ( v != null ) {
            ntokens = v.size();
        }
        String OutputYearType = "";
        if ( ntokens >= 2 ) {
            // Output year type...
            OutputYearType = v.get(1).trim();
        }

        // Set parameters and new defaults...

        PropList parameters = new PropList ( getCommandName() );
        parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
        if ( OutputYearType.length() > 0 ) {
            parameters.set ( "OutputYearType", OutputYearType );
        }
        parameters.setHowSet ( Prop.SET_UNKNOWN );
        setCommandParameters ( parameters );
    }
}

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "SetOutputYearType_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	String OutputYearType = parameters.getValue ( "OutputYearType" );
	YearType outputYearType = YearType.valueOfIgnoreCase(OutputYearType);
	
	try {
        // Set the output year type...
	    processor.setPropContents ( "OutputYearType", outputYearType );
		Message.printStatus ( 2, routine, "Set output year type to \"" + OutputYearType + "\".");
	}
	catch ( Exception e ) {
		message = "Unexpected error setting output year type to \"" + OutputYearType + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "See the log file for details."));
		throw new CommandException ( message );
	}

	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String OutputYearType = parameters.getValue("OutputYearType");
	StringBuffer b = new StringBuffer ();
	if ( (OutputYearType != null) && (OutputYearType.length() > 0) ) {
		b.append ( "OutputYearType=" + OutputYearType );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}