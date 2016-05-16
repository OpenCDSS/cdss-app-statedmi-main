package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_StationView;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_Util;

import RTi.DMI.DMIUtil;
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
This class initializes, checks, and runs the FillClimateStationsFromHydroBase() command.
</p>
*/
public class FillClimateStationsFromHydroBase_Command 
extends AbstractCommand implements Command
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
public FillClimateStationsFromHydroBase_Command ()
{	super();
	setCommandName ( "FillClimateStationsFromHydroBase" );
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
{	String routine = "FillClimateStationsFromHydroBase_Command.checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
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
{	return (new FillClimateStationsFromHydroBase_JDialog ( parent, this )).ok();
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
	
	int matchCount = 0;
	try {
		// Query climate stations in HydroBase.  Then use the resulting
		// geoloc.latitude, geoloc.county, geoloc.huc, and station.station_name
		// to fill missing data.

		// Read the stations from HydroBase, which will be a join of stations and Geoloc data...

		StateCU_ClimateStation cli = null;
		HydroBase_StationView hcli = null;
		String id;
		for ( int i = 0; i < stationListSize; i++ ) {
			cli = (StateCU_ClimateStation)stationList.get(i);
			id = cli.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			try {
				hcli = hbdmi.readStationViewForStation_id(id);
				if ( hcli == null ) {
					message = "No station data for \"" + id + "\" available in HydroBase.  Not filling station.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the station is a valid ID in HydroBase." ) );
					continue;
				}
			}
			catch ( Exception e ) {
				message = "Error getting station data for \"" + id + "\" from HydroBase.  Not filling station.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the station is a valid ID in HydroBase." ) );
				Message.printWarning ( 3, routine, "Last query: \"" + hbdmi.getLastQueryString() + "\"" );
				Message.printWarning ( 3, routine, e );
				continue;
			}

			// Reset the data with the specified filled values.  Currently region1 is hard-coded
			// to be county, and region2 is HUC.  If StateDMI is made more flexible,
			// some additional checks will need to be put in here to account for other options.

			if ( StateCU_Util.isMissing(cli.getRegion1()) ) {
				Message.printStatus ( 2, routine, "Filling " +id+" Region1 -> County: "+hcli.getCounty());
				cli.setRegion1 ( hcli.getCounty() );
			}
			if ( StateCU_Util.isMissing(cli.getRegion2()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " Region2 -> HUC: " + hcli.getHUC() );
				cli.setRegion2 ( hcli.getHUC() );
			}
			if ( StateCU_Util.isMissing(cli.getLatitude()) && !DMIUtil.isMissing(hcli.getLatdecdeg()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " Latitude -> " +
				StringUtil.formatString( hcli.getLatdecdeg(),"%11.6f"));
				cli.setLatitude ( hcli.getLatdecdeg() );
			}
			if ( StateCU_Util.isMissing(cli.getElevation()) && !DMIUtil.isMissing(hcli.getElev()) ) {
				Message.printStatus ( 2, routine,
				"Filling " + id + " Elevation -> " + StringUtil.formatString( hcli.getElev(),"%11.6f") );
				cli.setElevation ( hcli.getElev() );
			}
			if ( StateCU_Util.isMissing(cli.getName()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " Name -> " + hcli.getStation_name());
				cli.setName ( hcli.getStation_name() );
			}
		}

		// If nothing was matched, take additional actions...

		if ( matchCount <= 0) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Do nothing
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Climate station \"" + ID +	"\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Climate station \"" + ID +	"\" was not matched: failing and not filling.";
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
        message = "Unexpected error filling climate station data (" + e + ").";
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