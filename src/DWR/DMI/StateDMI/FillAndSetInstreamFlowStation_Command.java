package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_Util;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
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
This class initializes, checks, and runs the FillInstreamFlowStation() and SetInstreamFlowStation(),
commands.  It is an abstract base class that must be controlled via a derived class.  For example,
the SetInstreamFlowStation() command extends this class in order to uniquely represent the command,
but much of the functionality is in this base class.
</p>
*/
public abstract class FillAndSetInstreamFlowStation_Command extends AbstractCommand implements Command
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
public FillAndSetInstreamFlowStation_Command ()
{	super();
	setCommandName ( "?Fill/Set?InstreamFlowStation" );
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
	String UpstreamRiverNodeID = parameters.getValue ( "UpstreamRiverNodeID" );
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	String DailyID = parameters.getValue ( "DailyID" );
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
	
	if ( (UpstreamRiverNodeID != null) && (UpstreamRiverNodeID.length() > 0) &&
		((UpstreamRiverNodeID.indexOf(" ") >= 0) || (UpstreamRiverNodeID.indexOf("-") >= 0))) {
        message = "The UpstreamRiverNodeID (" + UpstreamRiverNodeID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the UpstreamRiverNodeID without spaces or dashes." ) );
	}
	
	if ( (DownstreamRiverNodeID != null) && (DownstreamRiverNodeID.length() > 0) &&
		((DownstreamRiverNodeID.indexOf(" ") >= 0) || (DownstreamRiverNodeID.indexOf("-") >= 0))) {
        message = "The DownstreamRiverNodeID (" + DownstreamRiverNodeID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the DownstreamRiverNodeID without spaces or dashes." ) );
	}
	
	if ( (DailyID != null) && (DailyID.length() > 0) &&
		((DailyID.indexOf(" ") >= 0) || (DailyID.indexOf("-") >= 0))) {
        message = "The DailyID (" + DailyID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the DailyID without spaces or dashes." ) );
	}

	if ( this instanceof SetInstreamFlowStation_Command ) {
		// Include the Add option
		if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
			!IfNotFound.equalsIgnoreCase(_Add) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
			message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
							", or " + _Warn + " (default).") );
		}
	}
	else {
		if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
			message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
							", or " + _Warn + " (default).") );
		}
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "UpstreamRiverNodeID" );
	valid_Vector.add ( "DownstreamRiverNodeID" );
	valid_Vector.add ( "OnOff" );
	valid_Vector.add ( "DailyID" );
	valid_Vector.add ( "DemandType" );
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
	return (new FillAndSetInstreamFlowStation_JDialog ( parent, this )).ok();
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
    // Trim strings because Ray Bennett has written FORTRAN programs to process command files, and there
    // are sometimes extra spaces in the parameter values - this causes IDs to not be matched, etc.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	ID = ID.trim();
	String Name = parameters.getValue ( "Name" );
	if ( Name != null ) {
		Name = Name.trim();
	}
	String UpstreamRiverNodeID = parameters.getValue ( "UpstreamRiverNodeID" );
	if ( UpstreamRiverNodeID != null ) {
		UpstreamRiverNodeID = UpstreamRiverNodeID.trim();
	}
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	if ( DownstreamRiverNodeID != null ) {
		DownstreamRiverNodeID = DownstreamRiverNodeID.trim();
	}
	String OnOff = parameters.getValue ( "OnOff" );
	if ( OnOff != null ) {
		OnOff = OnOff.trim();
	}
	String DailyID = parameters.getValue ( "DailyID" );
	if ( DailyID != null ) {
		DailyID = DailyID.trim();
	}
	String DemandType = parameters.getValue ( "DemandType" );
	if ( DemandType != null ) {
		DemandType = DemandType.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List instreamFlowStationList = null;
    int instreamFlowStationListSize = 0;
    try {
    	instreamFlowStationList = (List)processor.getPropContents ( "StateMod_InstreamFlowStation_List" );
    	instreamFlowStationListSize = instreamFlowStationList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	boolean fill_Name = false;
    	if ( Name != null ) {
    		fill_Name = true;
    	}
    	boolean fill_UpstreamRiverNodeID = false;
    	if ( UpstreamRiverNodeID != null ) {
    		fill_UpstreamRiverNodeID = true;
    	}
    	boolean fill_DownstreamRiverNodeID = false;
    	if ( DownstreamRiverNodeID != null ) {
    		fill_DownstreamRiverNodeID = true;
    	}
    	boolean fill_OnOff = false;
    	int OnOff_int = 0;
    	if ( (OnOff != null) && (OnOff.length() > 0) ) {
    		fill_OnOff = true;
    		OnOff_int = Integer.parseInt ( OnOff );
    	}

    	boolean fill_DailyID = false;
    	if ( DailyID != null ) {
    		fill_DailyID = true;
    	}

    	boolean fill_DemandType = false;
    	int DemandType_int = 0;
    	if ( (DemandType != null) && (DemandType.length() > 0) ) {
    		fill_DemandType = true;
    		DemandType_int = Integer.parseInt ( DemandType );
    	}
    	
    	// Use logic as per code pulled out of StateDMI_Processor
    	
    	StateMod_InstreamFlow ifs = null;
    	String id;
    	int matchCount = 0;
    	String action = "Setting ";
    	boolean fill = false;
    	if ( this instanceof FillInstreamFlowStation_Command ) {
    		action = "Filling ";
    		fill = true;
    	}
    	for (int i = 0; i < instreamFlowStationListSize; i++) {
    		ifs=(StateMod_InstreamFlow)instreamFlowStationList.get(i);
    		id = ifs.getID();
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Have a match so reset the data...
    		if ( fill_Name && (!fill || StateMod_Util.isMissing(ifs.getName())) ) {
    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
    			ifs.setName ( Name );
    		}
    		if ( fill_UpstreamRiverNodeID && (!fill || StateMod_Util.isMissing(ifs.getCgoto())) ) {
    			Message.printStatus ( 2, routine, action + id + " UpstreamRiverNodeID -> " +
    			UpstreamRiverNodeID );
    			ifs.setCgoto ( UpstreamRiverNodeID );
    		}
    		if ( fill_DownstreamRiverNodeID && (!fill || StateMod_Util.isMissing(ifs.getIfrrdn())) ) {
    			Message.printStatus ( 2, routine,
    			action + id + " DownstreamRiverNodeID -> " + DownstreamRiverNodeID );
    			ifs.setIfrrdn ( DownstreamRiverNodeID );
    		}
    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(ifs.getSwitch())) ) {
    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
    			ifs.setSwitch ( OnOff_int );
    		}
    		if ( fill_DailyID && (!fill || StateMod_Util.isMissing(ifs.getCifridy())) ){
    			if ( DailyID.equalsIgnoreCase("ID") ) {
    				// Set the daily ID to the same as the normal identifier...
    				Message.printStatus ( 2, routine, action + id + " DailyID -> " + ifs.getID() );
    				ifs.setCifridy ( ifs.getID() );
    			}
    			else {
    				// Set to the given value...
    				Message.printStatus ( 2, routine, action + id + " DailyID -> " + DailyID );
    				ifs.setCifridy ( DailyID );
    			}
    		}
    		if ( fill_DemandType && (!fill || StateMod_Util.isMissing(ifs.getIifcom())) ){
    			Message.printStatus ( 2, routine, action + id + " DemandType -> " + DemandType );
    			ifs.setIifcom ( DemandType_int );
    		}
    	}

		if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
	    	// If nothing was matched and the idpattern does not contain a
	    	// wildcard, add a StateMod_InstreamFlow at the end...
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
				Message.printStatus(2 , routine, "Did not match location \"" + ID + "\" - ignoring.");
			}
			else if ( IfNotFound.equalsIgnoreCase("Add") ) {
				ifs = new StateMod_InstreamFlow ( false );
				id = ID;
				ifs.setID ( id );
				instreamFlowStationList.add ( ifs );
				Message.printStatus ( 2, routine, "Adding instream flow station " + id );
				if ( fill_Name ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
					ifs.setName ( Name );
				}
				if ( fill_UpstreamRiverNodeID ) {
					Message.printStatus ( 2, routine,
					"Setting " + id + " UpstreamRiverNodeID -> " + UpstreamRiverNodeID );
					ifs.setCgoto ( UpstreamRiverNodeID );
				}
				if ( fill_DownstreamRiverNodeID ) {
					Message.printStatus ( 2, routine,
					"Setting " + id + " DownstreamRiverNodeID -> " + DownstreamRiverNodeID );
					ifs.setIfrrdn ( DownstreamRiverNodeID );
				}
				if ( fill_OnOff ) {
					Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
					ifs.setSwitch ( OnOff_int );
				}
				if ( fill_DailyID ) {
					if ( DailyID.equalsIgnoreCase("ID") ) {
						// Set the daily ID to the same as the normal identifier...
						Message.printStatus ( 2, routine, action + id + " DailyID -> " + ifs.getID() );
						ifs.setCifridy ( ifs.getID() );
					}
					else {
						Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
						ifs.setCifridy ( DailyID );
					}
				}
				if ( fill_DemandType ) {
					Message.printStatus ( 2, routine, "Setting " + id + " DemandType -> "+DemandType);
					ifs.setIifcom ( DemandType_int );
				}
    		}
			// Increment the count so that the following warnings are not printed
			++matchCount;
		}
		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Instream flow \"" + ID + "\" was not matched: warning and not " +
				action.toLowerCase();
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Instream flow \"" + ID +	"\" was not matched: failing and not " +
				action.toLowerCase();
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
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
	String Name = parameters.getValue ( "Name" );
	String UpstreamRiverNodeID = parameters.getValue ( "UpstreamRiverNodeID" );
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	String OnOff = parameters.getValue ( "OnOff" );
	String DailyID = parameters.getValue ( "DailyID" );
	String DemandType = parameters.getValue ( "DemandType" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (Name != null) && (Name.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Name=\"" + Name + "\"" );
	}
	if ( (UpstreamRiverNodeID != null) && (UpstreamRiverNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UpstreamRiverNodeID=\"" + UpstreamRiverNodeID + "\"" );
	}
	if ( (DownstreamRiverNodeID != null) && (DownstreamRiverNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DownstreamRiverNodeID=\"" + DownstreamRiverNodeID + "\"" );
	}
	if ( (OnOff != null) && (OnOff.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOff=" + OnOff );
	}
	if ( (DailyID != null) && (DailyID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DailyID=\"" + DailyID + "\"" );
	}
	if ( (DemandType != null) && (DemandType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DemandType=" + DemandType );
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