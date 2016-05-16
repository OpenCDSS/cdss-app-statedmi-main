package rti.tscommandprocessor.commands.util;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
<p>
This class initializes, checks, and runs the SetWorkingDir() command.
</p>
*/
public class SetWorkingDir_Command extends AbstractCommand implements Command
{

/**
Protected data members shared with the dialog and other related classes.
*/
protected final String _BatchOnly = "BatchOnly";
protected final String _GUIOnly = "GUIOnly";
protected final String _GUIAndBatch = "GUIAndBatch";

/**
Flags for RunOnOS
*/
protected final String _All = "All";
protected final String _Windows = "Windows";
protected final String _UNIX = "UNIX";

/**
Constructor.
*/
public SetWorkingDir_Command ()
{	super();
	setCommandName ( "SetWorkingDir" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String WorkingDir = parameters.getValue ( "WorkingDir" );
	String RunMode = parameters.getValue ( "RunMode" );
	String RunOnOS = parameters.getValue ( "RunOnOS" );
	String warning = "";
    String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    if ( (WorkingDir == null) || WorkingDir.equals("") ) {
        message = "The working directory is blank.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message,
                "Specify the working directory." ) );
    }
    
    String working_dir = null;
    try { Object o = processor.getPropContents ( "WorkingDir" );
        // Working directory is available so use it...
        if ( o != null ) {
            working_dir = (String)o;
        }
    }
    catch ( Exception e ) {
        // Not fatal, but of use to developers.
        message = "Error requesting WorkingDir from processor.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Software error - report problem to support." ) );

    }
    try {
        String adjusted_path = IOUtil.verifyPathForOS (
            IOUtil.adjustPath ( working_dir, WorkingDir) );
        File f = new File ( adjusted_path );
        if ( !f.exists() ) {
            message = "The working directory does not exist for: \"" + adjusted_path + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that the directory exists." ) );
        }
        f = null;
    }
    catch ( Exception e ) {
        message = "The working directory \"" + WorkingDir + "\" cannot be adjusted by the working directory \""
        + working_dir + "\".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message,
                        "Verify that the path to the working directory and previous working directory are compatible." ) );
    }

	if ( (RunMode != null) && !RunMode.equals("") &&
		!RunMode.equalsIgnoreCase(_BatchOnly) &&
		!RunMode.equalsIgnoreCase(_GUIOnly) &&
		!RunMode.equalsIgnoreCase(_GUIAndBatch) ) {
        message = "The run mode \"" + RunMode + "\" is not valid.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message,
                        "Correct the run mode to be blank, " + _BatchOnly + ", " + _GUIOnly + ", or " +
                        _GUIAndBatch + "." ) );
	}
	
    if ( (RunOnOS != null) && !RunOnOS.equals("") &&
           !RunOnOS.equalsIgnoreCase(_All) &&
           !RunOnOS.equalsIgnoreCase(_UNIX) &&
           !RunOnOS.equalsIgnoreCase(_Windows) ) {
           message = "The RunOnOS \"" + RunOnOS + "\" parameter value is not valid.";
           warning += "\n" + message;
           status.addToLog ( CommandPhaseType.INITIALIZATION,
                   new CommandLogRecord(CommandStatusType.FAILURE,
                           message,
                           "Correct the RunOnOS parameter to be blank, " + _All + ", " + _UNIX + ", or " +
                           _Windows + "." ) );
    }

    // Check for invalid parameters...
    List valid_Vector = new Vector();
    valid_Vector.add ( "WorkingDir" );
    valid_Vector.add ( "RunMode" );
    valid_Vector.add ( "RunOnOS" );
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
	return (new SetWorkingDir_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	String routine = "SetWorkingDir_Command.parseCommand", message;
	int warning_level = 2;
	if ( (command_string.indexOf("=") > 0) || command_string.endsWith("()") ) {
		// New syntax...
		super.parseCommand ( command_string );
	}
	else {
	    // Parse the old command...
		List tokens = StringUtil.breakStringList ( command_string,
			"(,)", StringUtil.DELIM_ALLOW_STRINGS );
		if ( tokens.size() != 3 ) {
			message =
			"Invalid syntax for command.  Expecting SetWorkingDir(WorkingDir,RunMode).";
			Message.printWarning ( warning_level, routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
		String WorkingDir = ((String)tokens.get(1)).trim();
		String RunMode = ((String)tokens.get(2)).trim();
		PropList parameters = new PropList ( getCommandName() );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		if ( WorkingDir.length() > 0 ) {
			parameters.set ( "WorkingDir", WorkingDir );
		}
		if ( RunMode.length() > 0 ) {
			parameters.set ( "RunMode", RunMode );
		}
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
}

/**
Run the command.
Time series are taken from the
available list in memory, if available.  Otherwise the time series are re-read.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "SetWorkingDir_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
    
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);

	PropList parameters = getCommandParameters();
	String WorkingDir = parameters.getValue ( "WorkingDir" );
	String RunMode = parameters.getValue ( "RunMode" );
	if ( (RunMode == null) || RunMode.equals("") ) {
		RunMode = _GUIAndBatch;   // Default
	}
    String RunOnOS = parameters.getValue ( "RunOnOS" );
    if ( (RunOnOS == null) || RunOnOS.equals("") ) {
        RunOnOS = _All;   // Default
    }
    
    boolean doRun = true;   // Should the command be run?
    if ( IOUtil.isUNIXMachine() ) {
        if ( !RunOnOS.equalsIgnoreCase(_All) && !RunOnOS.equalsIgnoreCase(_UNIX) ) {
            // Not running on this platform.
            doRun = false;
        }
    }
    else {
        // Windows
        if ( !RunOnOS.equalsIgnoreCase(_All) && !RunOnOS.equalsIgnoreCase(_Windows) ) {
            // Not running on this platform.
            doRun = false;
        }
    }
    
    if ( (IOUtil.isBatch() && (!RunMode.equalsIgnoreCase("GUIAndBatch") &&(!RunMode.equalsIgnoreCase("Batch")))) ||
         (!IOUtil.isBatch() && (!RunMode.equalsIgnoreCase("GUIAndBatch") &&(!RunMode.equalsIgnoreCase("GUIOnly")))) ) {
        doRun = false;
    }

	// Now try to process.  The working directory is relative to the existing working directory
	// for the processor.

    String WorkingDir_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),WorkingDir ) );
	try {
	    if ( doRun ) {
		    // Only run the command for the requested run mode...
	        if ( IOUtil.fileExists(WorkingDir_full) ) {
	            // TODO SAM 2007-08-22 Evaluate the ramifications of this
	            // being application-wide vs. just the processor instance - for now processor only
	            // Some code that works with paths may be relying on the global value - hopefully not.
	            //IOUtil.setProgramWorkingDir(dir);
	            processor.setPropContents("WorkingDir",WorkingDir_full);
	            Message.printStatus ( 1, routine, "Setting working directory to \"" + WorkingDir +
	                    "\", full \"" + WorkingDir_full + "\"" );
	        }
	        else {
	            message = "Working directory \"" + WorkingDir + "\", full \"" +
	            WorkingDir_full + "\" does not exist.  Not setting.";
	            Message.printWarning ( 2, routine, message );
	            throw new Exception ( message );
	        }
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error setting working direcectory to \"" + WorkingDir + "\", full \"" + WorkingDir_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Check the log file." ) );
		throw new CommandException ( message );
	}
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String WorkingDir = props.getValue("WorkingDir");
	String RunMode = props.getValue("RunMode");
	String RunOnOS = props.getValue("RunOnOS");
	StringBuffer b = new StringBuffer ();
	if ( (WorkingDir != null) && (WorkingDir.length() > 0) ) {
		b.append ( "WorkingDir=\"" + WorkingDir + "\"" );
	}
	if ( (RunMode != null) && (RunMode.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RunMode=" + RunMode );
	}
    if ( (RunOnOS != null) && (RunOnOS.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RunOnOS=" + RunOnOS );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
