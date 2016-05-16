package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_PrfGageData;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
<p>
This class initializes, checks, and runs the SetStreamEstimateCoefficientsPFGage() command.
</p>
*/
public class SetStreamEstimateCoefficientsPFGage_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add"; // Hidden default
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for IfFound parameter, also Ignore, Warn, and Fail about.
*/
protected final String _Set = "Set";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetStreamEstimateCoefficientsPFGage_Command ()
{	super();
	setCommandName ( "SetStreamEstimateCoefficientsPFGage" );
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
	String GageID = parameters.getValue ( "GageID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String IfFound = parameters.getValue ( "IfFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID to process." ) );
	}
	
	if ( (GageID == null) || (GageID.length() == 0) ) {
        message = "The substitute gage ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the substitute gage ID to process." ) );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " or blank (default is to add).") );
	}
	
	if ( (IfFound != null) && (IfFound.length() > 0) && !IfFound.equalsIgnoreCase(_Set) &&
		!IfFound.equalsIgnoreCase(_Ignore) && !IfFound.equalsIgnoreCase(_Fail) &&
		!IfFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfFound value (" + IfFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfFound as " + _Set + ", " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "GageID" );
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
	return (new SetStreamEstimateCoefficientsPFGage_JDialog ( parent, this )).ok();
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
	String GageID = parameters.getValue ( "GageID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
  
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	StateMod_PrfGageData prf = new StateMod_PrfGageData ( ID, GageID );
    	boolean found = processor.findAndAddSMPrfGageData ( prf, true );

    	// If nothing was matched take additional actions...

    	/* FIXME SAM 2009-01-11 Fix this later - was not in the previous software
    	if ( !found ) {
    		if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
    			// Do nothing
    		}
     		else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Stream estimate coefficients station \"" + ID +
				"\" was not matched: warning and not setting proration factor gage.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
    		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Stream estimate coefficients station \"" + ID +
				"\" was not matched: failing and not setting proration factor gage.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
			}
    	}
    	*/
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing stream estimate coefficients proration factor gage data (" + e + ").";
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
	String GageID = parameters.getValue ( "GageID" );
	String IfFound = parameters.getValue ( "IfFound" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (GageID != null) && (GageID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GageID=\"" + GageID + "\"" );
	}
	if ( (IfFound != null) && (IfFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfFound=" + IfFound );
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