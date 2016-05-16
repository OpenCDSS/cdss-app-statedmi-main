package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_ClimateStation;

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
This class initializes, checks, and runs the SetClimateStation() command.
*/
public class SetClimateStation_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";
	
/**
Constructor.
*/
public SetClimateStation_Command ()
{	super();
	setCommandName ( "SetClimateStation" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters (	PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String routine = "SetClimateStation_Command.checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String Latitude = parameters.getValue ( "Latitude" );
	String Elevation = parameters.getValue ( "Elevation" );
	String HeightHumidityMeas = parameters.getValue ( "HeightHumidityMeas" );
	String HeightWindMeas = parameters.getValue ( "HeightWindMeas" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "\nAn identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the identifier pattern to match." ) );
	}

	if ( (Latitude != null) && (Latitude.length() > 0) && !StringUtil.isDouble(Latitude) ) {
		message = "The latitude value (" + Latitude + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the latitude as a number." ) );
	}
	
	if ( (Elevation != null) && (Elevation.length() > 0) && !StringUtil.isDouble(Elevation) ) {
		message = "The elevation value (" + Elevation + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the elevation as a number." ) );
	}
	
	if ( (HeightHumidityMeas != null) && (HeightHumidityMeas.length() > 0) && !StringUtil.isDouble(HeightHumidityMeas) ) {
		message = "The HeightHumidityMeas value (" + HeightHumidityMeas + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the HeightHumidityMeas as a number." ) );
	}
	
	if ( (HeightWindMeas != null) && (HeightWindMeas.length() > 0) && !StringUtil.isDouble(HeightWindMeas) ) {
		message = "The HeightWindMeas value (" + HeightWindMeas + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the HeightWindMeas as a number." ) );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Add) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
						", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
    valid_Vector.add ( "Latitude" );
	valid_Vector.add ( "Elevation" );
    valid_Vector.add ( "Region1" );
	valid_Vector.add ( "Region2" );
	valid_Vector.add ( "HeightHumidityMeas" );
	valid_Vector.add ( "HeightWindMeas" );
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
{	String routine = "SetClimateStation_Command.editCommand";
	// The command will be modified if changed
	CommandProcessor processor = getCommandProcessor();
	List Region1_List = new Vector();
	List Region2_List = new Vector();
	try {
		Region1_List = (List)processor.getPropContents("CountyList");
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error getting county list - will not be listed in Region1." );
	}
	try {
		Region2_List = (List)processor.getPropContents("HUCList");
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error getting HUC list - will not be listed in Region2." );
	}
	return (new FillAndSetClimateStation_JDialog ( parent, this, Region1_List, Region2_List )).ok();
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
	String Latitude = parameters.getValue ( "Latitude" );
	String Elevation = parameters.getValue ( "Elevation" );
	String Region1 = parameters.getValue ( "Region1" );
	String Region2 = parameters.getValue ( "Region2" );
	String Name = parameters.getValue ( "Name" );
	String HeightHumidityMeas = parameters.getValue ( "HeightHumidityMeas" );
	String HeightWindMeas = parameters.getValue ( "HeightWindMeas" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of climate stations...
	
	List stationList = null;
	int stationListSize = 0;
	try {
		Object o = processor.getPropContents( "StateCU_ClimateStation_List");
		stationList = (List)o;
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_ClimateStation_List from processor.";
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
		boolean fill_latitude = false;
		double latitude_double = 0.0;
		if ( (Latitude != null) && (Latitude.length() > 0) ) {
			fill_latitude = true;
			latitude_double = StringUtil.atod ( Latitude );
		}
		boolean fill_elevation = false;
		double elevation_double = 0.0;
		if ( (Elevation != null) && (Elevation.length() > 0) ) {
			fill_elevation = true;
			elevation_double = StringUtil.atod ( Elevation );
		}
		boolean fill_region1 = false;
		if ( (Region1 != null) && (Region1.length() > 0) ) {
			fill_region1 = true;
		}
		boolean fill_region2 = false;
		if ( (Region2 != null) && (Region2.length() > 0)) {
			fill_region2 = true;
		}
		boolean fill_name = false;
		if ( (Name != null) && (Name.length() > 0) ) {
			fill_name = true;
		}
		boolean fill_zh = false;
		double zh_double = 0.0;
		if ( (HeightHumidityMeas != null) && (HeightHumidityMeas.length() > 0) ) {
			fill_zh = true;
			zh_double = StringUtil.atod ( HeightHumidityMeas );
		}
		boolean fill_zm = false;
		double zm_double = 0.0;
		if ( (HeightWindMeas != null) && (HeightWindMeas.length() > 0) ) {
			fill_zm = true;
			zm_double = StringUtil.atod ( HeightWindMeas );
		}

		StateCU_ClimateStation cli = null;
		String id;
		int match_count = 0;
		for (int i = 0; i < stationListSize; i++) {
			cli = (StateCU_ClimateStation)stationList.get(i);
			id = cli.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++match_count;
			// Have a match so reset the data...
			if ( fill_latitude ) {
				Message.printStatus ( 2, routine, "Setting " + id + " latitude -> " + Latitude );
				cli.setLatitude ( latitude_double );
			}
			if ( fill_elevation ) {
				Message.printStatus ( 2, routine, "Setting " + id + " elevation -> " + Elevation );
				cli.setElevation ( elevation_double );
			}
			if ( fill_region1 ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Region1 -> " + Region1 );
				cli.setRegion1 ( Region1 );
			}
			if ( fill_region2 ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Region2 -> " + Region2 );
				cli.setRegion2 ( Region2 );
			}
			if ( fill_name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				cli.setName ( Name );
			}
			if ( fill_zh ) {
				Message.printStatus ( 2, routine, "Setting " + id +
					" height of humidity/temperature measurements -> " + HeightHumidityMeas );
				cli.setZh ( zh_double );
			}
			if ( fill_zm ) {
				Message.printStatus ( 2, routine, "Setting " + id +
					" height of wind measurements -> " + HeightWindMeas );
				cli.setZm ( zm_double );
			}
		}

		// If nothing was matched and the idpattern does not contain a
		// wildcard, add a climate station at the end...

		if ( (match_count == 0) && (ID.indexOf("*") < 0) ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
			}
			else if ( IfNotFound.equalsIgnoreCase(_Add) ) {
				cli = new StateCU_ClimateStation();
				id = ID;
				cli.setID ( id );
				stationList.add ( cli );
				Message.printStatus ( 2, routine, "Adding climate station " + id );
				if ( fill_latitude ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Latitude -> " + Latitude );
					cli.setLatitude ( latitude_double );
				}
				if ( fill_elevation ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Elevation -> " + Elevation);
					cli.setElevation ( elevation_double );
				}
				if ( fill_region1 ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Region1 -> " + Region1 );
					cli.setRegion1 ( Region1 );
				}
				if ( fill_region2 ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Region2 -> " + Region2 );
					cli.setRegion2 ( Region2 );
				}
				if ( fill_name ) {
					Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
					cli.setName ( Name );
				}
				if ( fill_zh ) {
					Message.printStatus ( 2, routine, "Setting " + id +
						" height of humidity/temperature measurements -> " + HeightHumidityMeas );
					cli.setZh ( zh_double );
				}
				if ( fill_zm ) {
					Message.printStatus ( 2, routine, "Setting " + id +
						" height of wind measurements -> " + HeightWindMeas );
					cli.setZm ( zm_double );
				}
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Climate station \"" + ID +	"\" was not matched: warning and not adding.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Climate station \"" + ID +	"\" was not matched: failing and not adding.";
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
        message = "Unexpected error setting climate station data (" + e + ").";
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
	
	String ID = parameters.getValue ( "ID" );
	String Name = parameters.getValue ( "Name" );
	String Latitude = parameters.getValue ( "Latitude" );
	String Elevation = parameters.getValue ( "Elevation" );
	String Region1 = parameters.getValue ( "Region1" );
	String Region2 = parameters.getValue ( "Region2" );
	String HeightHumidityMeas = parameters.getValue ( "HeightHumidityMeas" );
	String HeightWindMeas = parameters.getValue ( "HeightWindMeas" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( Name != null && Name.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Name=\"" + Name + "\"" );
	}
	if ( Latitude != null && Latitude.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Latitude=" + Latitude );
	}
	if ( Elevation != null && Elevation.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Elevation=" + Elevation );
	}
	if ( Region1 != null && Region1.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region1=\"" + Region1 + "\"" );
	}
	if ( Region2 != null && Region2.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region2=\"" + Region2 + "\"" );
	}
	if ( HeightHumidityMeas != null && HeightHumidityMeas.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "HeightHumidityMeas=" + HeightHumidityMeas );
	}
	if ( HeightWindMeas != null && HeightWindMeas.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "HeightWindMeas=" + HeightWindMeas );
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