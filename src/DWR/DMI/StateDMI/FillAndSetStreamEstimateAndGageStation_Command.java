package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
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

// TODO SAM 2009-02-18 This code could really be condensed - see newer classes for logic to combine
// fill and set.

/**
<p>
This class initializes, checks, and runs the FillDiversionStation(), SetDiversionStation(),
FillWellStation(), and SetWellStation() commands.  The functionality is handled in one class due to the
close similarity between the commands and of diversions and wells in StateMod.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetDiversionStation()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
</p>
*/
public abstract class FillAndSetStreamEstimateAndGageStation_Command extends AbstractCommand implements Command
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
public FillAndSetStreamEstimateAndGageStation_Command ()
{	super();
	setCommandName ( "?Fill/Set?Stream?Estimate/Gage?Station" );
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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	String DailyID = parameters.getValue ( "DailyID" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
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
	
	if ( (RiverNodeID != null) && (RiverNodeID.length() > 0) &&
		((RiverNodeID.indexOf(" ") >= 0) || (RiverNodeID.indexOf("-") >= 0))) {
        message = "The RiverNodeID (" + RiverNodeID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the RiverNodeID without spaces or dashes." ) );
	}
	
	if ( (DailyID != null) && (DailyID.length() > 0) &&
		((DailyID.indexOf(" ") >= 0) || (DailyID.indexOf("-") >= 0))) {
        message = "The DailyID (" + DailyID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the DailyID without spaces or dashes." ) );
	}

	if ( (this instanceof SetStreamEstimateStation_Command) || (this instanceof SetStreamGageStation_Command) ) {
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
	List<String> valid_Vector = new Vector<String>(5);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "RiverNodeID" );
	valid_Vector.add ( "DailyID" );
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
	return (new FillAndSetStreamEstimateAndGageStation_JDialog ( parent, this )).ok();
}

/**
Process FillStreamEstimateStation() and FillStreamGageStation() commands.
*/
private int processFillCommands ( List<StateMod_StreamEstimate> streamEstimateStationList,
		List<StateMod_StreamGage> streamGageStationList,
		int warningCount, int warningLevel, String commandTag, CommandStatus status,
		String ID, String idpattern_Java,
		boolean fill_Name, String Name,
		boolean fill_RiverNodeID, String RiverNodeID,
		boolean fill_DailyID, String DailyID,
		String IfNotFound )
{	String routine = getClass().getName() + ".processFillCommands";
	int size = 0;
	if ( this instanceof FillStreamGageStation_Command ) {
		if ( streamGageStationList != null ) {
			size = streamGageStationList.size();
		}
	}
	else if ( this instanceof FillStreamEstimateStation_Command ) {
		if ( streamEstimateStationList != null ) {
			size = streamEstimateStationList.size();
		}
	}
	StateMod_StreamGage gage = null;
	StateMod_StreamEstimate estimate = null;
	String id;
	int matchCount = 0;
	for (int i = 0; i < size; i++) {
		if ( this instanceof FillStreamGageStation_Command ) {
			gage = streamGageStationList.get(i);
			id = gage.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Have a match so reset the data...
			if ( fill_Name && StateMod_Util.isMissing(gage.getName()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " name -> " + Name );
				gage.setName ( Name );
			}
			if ( fill_RiverNodeID && StateMod_Util.isMissing(gage.getCgoto()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " RiverNodeID -> " + RiverNodeID );
				gage.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID && StateMod_Util.isMissing(gage.getCrunidy()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " DailyID -> " + DailyID );
				gage.setCrunidy ( DailyID );
			}
		}
		else if ( this instanceof FillStreamEstimateStation_Command ) {
			estimate = streamEstimateStationList.get(i);
			id = estimate.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Have a match so reset the data...
			if ( fill_Name && StateMod_Util.isMissing(estimate.getName()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " name -> " + Name );
				estimate.setName ( Name );
			}
			if ( fill_RiverNodeID && StateMod_Util.isMissing(estimate.getCgoto()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " RiverNodeID -> " + RiverNodeID );
				estimate.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID && StateMod_Util.isMissing(estimate.getCrunidy()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " DailyID -> " + DailyID );
				estimate.setCrunidy ( DailyID );
			}
		}
		++matchCount;
	}
	if ( matchCount == 0 ) {
		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
			if ( this instanceof FillStreamGageStation_Command ) {
				String message = "Stream gage station \"" + ID + "\" was not matched: warning and not filling.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( this instanceof FillStreamEstimateStation_Command ){
				String message = "Stream estimate station \"" + ID + "\" was not matched: warning and not filling.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
		}
		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
			if ( this instanceof FillStreamGageStation_Command ) {
				String message = "Stream gage station \"" + ID + "\" was not matched: failing and not filling.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( this instanceof FillStreamEstimateStation_Command ){
				String message = "Stream estimate station \"" + ID + "\" was not matched: failing and not filling.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
		}
	}
	return warningCount;
}

/**
Process SetStreamEstimateStation() and SetStreamGageStation() commands.
*/
private int processSetCommands ( List<StateMod_StreamEstimate> streamEstimateStationList,
		List<StateMod_StreamGage> streamGageStationList,
		int warningCount, int warningLevel, String commandTag, CommandStatus status,
		String ID, String idpattern_Java,
		boolean fill_Name, String Name,
		boolean fill_RiverNodeID, String RiverNodeID,
		boolean fill_DailyID, String DailyID,
		String IfNotFound )
{	String routine = getClass().getName() + ".processSetCommands";
	int size = 0;
	if ( this instanceof SetStreamGageStation_Command ) {
		if ( streamGageStationList != null ) {
			size = streamGageStationList.size();
		}
	}
	else if ( this instanceof SetStreamEstimateStation_Command ) {
		if ( streamEstimateStationList != null ) {
			size = streamEstimateStationList.size();
		}
	}
	StateMod_StreamGage gage = null;
	StateMod_StreamEstimate estimate = null;
	String id;
	int matchCount = 0;
	for (int i = 0; i < size; i++) {
		if ( this instanceof SetStreamGageStation_Command ) {
			gage = (StateMod_StreamGage)streamGageStationList.get(i);
			id = gage.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				gage.setName ( Name );
			}
			if ( fill_RiverNodeID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
				gage.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				gage.setCrunidy ( DailyID );
			}
		}
		else if ( this instanceof SetStreamEstimateStation_Command ) {
			estimate = (StateMod_StreamEstimate)streamEstimateStationList.get(i);
			id = estimate.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				estimate.setName ( Name );
			}
			if ( fill_RiverNodeID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
				estimate.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				estimate.setCrunidy ( DailyID );
			}
		}
	}

	// If nothing was matched and the idpattern does not contain a
	// wildcard, add a stream gage at the end...

	if ( (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase(_Add) ) {
		if ( this instanceof SetStreamGageStation_Command ) {
			gage = new StateMod_StreamGage();
			id = ID;
			gage.setID ( id );
			streamGageStationList.add ( gage );
			Message.printStatus ( 2, routine, "Adding stream gage " + id );
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				gage.setName ( Name );
			}
			if ( fill_RiverNodeID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
				gage.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				gage.setCrunidy ( DailyID );
			}
			++matchCount;
		}
		else if ( this instanceof SetStreamEstimateStation_Command ){
			estimate = new StateMod_StreamEstimate();
			id = ID;
			estimate.setID ( id );
			streamEstimateStationList.add ( estimate );
			Message.printStatus ( 2, routine, "Adding stream estimate " + id );
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				estimate.setName ( Name );
			}
			if ( fill_RiverNodeID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
				estimate.setCgoto ( RiverNodeID );
			}
			if ( fill_DailyID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				estimate.setCrunidy ( DailyID );
			}
			++matchCount;
		}
	}
	if ( matchCount == 0 ) {
		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
			if ( this instanceof SetStreamGageStation_Command ) {
				String message = "Stream gage station \"" + ID + "\" was not matched: warning and not setting.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( this instanceof SetStreamEstimateStation_Command ){
				String message = "Stream estimate station \"" + ID + "\" was not matched: warning and not setting.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
		}
		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
			if ( this instanceof SetStreamGageStation_Command ) {
				String message = "Stream gage station \"" + ID + "\" was not matched: failing and not setting.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( this instanceof SetStreamEstimateStation_Command ){
				String message = "Stream estimate station \"" + ID + "\" was not matched: failing and not setting.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
		}
	}
	return warningCount;
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
    // Trim the input if not null because Ray Bennett sometimes generates commands with FORTRAN that
    // has extra spaces.

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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	if ( RiverNodeID != null ) {
		RiverNodeID = RiverNodeID.trim();
	}
	String DailyID = parameters.getValue ( "DailyID" );
	if ( DailyID != null ) {
		DailyID = DailyID.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List<StateMod_StreamGage> streamGageStationList = null;
    List<StateMod_StreamEstimate> streamEstimateStationList = null;
    try {
    	if ( (this instanceof FillStreamGageStation_Command) ||
    		(this instanceof SetStreamGageStation_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamGage>dataList = (List<StateMod_StreamGage>)processor.getPropContents ( "StateMod_StreamGageStation_List" );
    		streamGageStationList = dataList;
    	}
    	else if ( (this instanceof FillStreamEstimateStation_Command) ||
        	(this instanceof SetStreamEstimateStation_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate> dataList =
				(List<StateMod_StreamEstimate>)processor.getPropContents ( "StateMod_StreamEstimateStation_List" );
    		streamEstimateStationList = dataList;
    	}
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
    	boolean fill_RiverNodeID = false;
    	if ( RiverNodeID != null ) {
    		fill_RiverNodeID = true;
    	}
    	boolean fill_DailyID = false;
    	if ( DailyID != null ) {
    		fill_DailyID = true;
    	}
    	
    	// Use logic as per code pulled out of StateDMI_Processor
    	
    	if ( (this instanceof SetStreamEstimateStation_Command) ||
        	(this instanceof SetStreamGageStation_Command) ) {
    		warning_count = processSetCommands ( streamEstimateStationList, streamGageStationList,
    				warning_count, warning_level, command_tag, status,
    				ID, idpattern_Java,
    				fill_Name, Name,
    				fill_RiverNodeID, RiverNodeID,
    				fill_DailyID, DailyID,
    				IfNotFound );
    	}
    	else if ( (this instanceof FillStreamEstimateStation_Command) ||
        	(this instanceof FillStreamGageStation_Command) ) {
    		warning_count = processFillCommands ( streamEstimateStationList, streamGageStationList,
    				warning_count, warning_level, command_tag, status,
    				ID, idpattern_Java,
    				fill_Name, Name,
    				fill_RiverNodeID, RiverNodeID,
    				fill_DailyID, DailyID,
    				IfNotFound );
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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	String DailyID = parameters.getValue ( "DailyID" );
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
	if ( (RiverNodeID != null) && (RiverNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RiverNodeID=\"" + RiverNodeID + "\"" );
	}
	if ( (DailyID != null) && (DailyID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DailyID=\"" + DailyID + "\"" );
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