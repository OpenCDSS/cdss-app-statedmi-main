package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateMod.StateMod_NodeNetwork;

import RTi.GR.GRLimits;
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

/**
<p>
This class initializes, checks, and runs the FillNetworkFromHydroBase() command.
</p>
*/
public class FillNetworkFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Used with LocationEstimate parameter.
*/
protected final String _Interpolate = "Interpolate";

/**
Constructor.
*/
public FillNetworkFromHydroBase_Command ()
{	super();
	setCommandName ( "FillNetworkFromHydroBase" );
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
	//String ID = parameters.getValue ( "ID" );
	//String NameFormat = parameters.getValue ( "NameFormat" );
	String LocationEstimate = parameters.getValue ( "LocationEstimate" );
	//String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	/*
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the identifier pattern to match." ) );
	}

	if ( (this instanceof FillStreamGageStationsFromHydroBase_Command) ||
			(this instanceof FillStreamEstimateStationsFromHydroBase_Command)||
			(this instanceof FillRiverNetworkFromHydroBase_Command)){
		if ( (NameFormat != null) && (NameFormat.length() > 0) &&
			!NameFormat.equalsIgnoreCase(__StationName) &&
			!NameFormat.equalsIgnoreCase(__StationName_NodeType)) {
			message = "The NameFormat value (" + NameFormat + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify NameFormat as " + __StationName + " (default) or " +
					__StationName_NodeType + ".") );
		}
	}
	*/
	
	if ( (LocationEstimate != null) && (LocationEstimate.length() > 0) && !LocationEstimate.equalsIgnoreCase(_Interpolate) ) {
		message = "The LocationEstimate value (" + LocationEstimate + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify LocationEstimate as " + _Interpolate + " (default).") );
		}
	
	/*
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail + ", or " + _Warn +
				" (default).") );
	}
	*/

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "LocationEstimate" );
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
{	return (new FillNetworkFromHydroBase_JDialog ( parent, this )).ok();
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
	int log_level = 3;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	//String ID = parameters.getValue ( "ID" );
	//if ( ID == null ) {
	//	ID = "*"; // Default
	//}
	//String NameFormat = parameters.getValue ( "NameFormat" );
	//if ( NameFormat == null ) {
	//	NameFormat = __StationName; // Default
	//}
	String LocationEstimate = parameters.getValue ( "LocationEstimate" );
	if ( LocationEstimate == null ) {
		LocationEstimate = _Interpolate;
	}
	//if ( IfNotFound == null ) {
	//	IfNotFound = _Warn; // Default
	//}
	// FIXME SAM 2009-01-21 Enable ID and other parameters to make command more flexible
	//String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
    // Get the network.
    
    StateMod_NodeNetwork net = null;
    try {
    	net = (StateMod_NodeNetwork)processor.getPropContents ( "StateMod_Network" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting StateMod network to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to sotware support." ) );
    }
    if ( net == null ) {
        message = "StateMod network is not available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that a network has been read or defined prior to this command." ) );
    }
	
	// Get the HydroBase DMI...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		// TODO SAM 2004-06-25 - need to pass extents if not interpolating.

		GRLimits limits = null;
		boolean interpolate = false;
		if ( LocationEstimate.equalsIgnoreCase(_Interpolate) ) {
			interpolate = true;
		}
		net.fillLocations ( new StateDMI_NodeDataProvider(hbdmi), interpolate, limits );
	}
    catch ( Exception e ) {
        message = "Unexpected error filling network from HydroBase (" + e + ").";
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
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
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
	
	//String ID = parameters.getValue ( "ID" );
	//String NameFormat = parameters.getValue ( "NameFormat" );
	String LocationEstimate = parameters.getValue ( "LocationEstimate" );
	//String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	/*
	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (this instanceof FillStreamGageStationsFromHydroBase_Command) ||
		(this instanceof FillStreamEstimateStationsFromHydroBase_Command)||
		(this instanceof FillRiverNetworkFromHydroBase_Command)){
		if ( NameFormat != null && NameFormat.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "NameFormat=" + NameFormat );
		}
	}
	*/
	if ( LocationEstimate != null && LocationEstimate.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LocationEstimate=" + LocationEstimate );
	}
	/*
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	*/
	
	return getCommandName() + "(" + b.toString() + ")";
}

}