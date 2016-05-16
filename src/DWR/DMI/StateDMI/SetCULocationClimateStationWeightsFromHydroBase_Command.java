package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_StationGeolocCUClimWts;
import DWR.StateCU.StateCU_Location;

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
This class initializes, checks, and runs the SetCULocationClimateStationWeightsFromHydroBase() command.
</p>
*/
public class SetCULocationClimateStationWeightsFromHydroBase_Command 
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
public SetCULocationClimateStationWeightsFromHydroBase_Command ()
{	super();
	setCommandName ( "SetCULocationClimateStationWeightsFromHydroBase" );
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
{	return (new SetCULocationClimateStationWeightsFromHydroBase_JDialog ( parent, this )).ok();
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
	
	List culocList = null;
	int culocListSize = 0;
	try {
		Object o = processor.getPropContents( "StateCU_Location_List");
		culocList = (List)o;
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
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
	
	try {
		StateCU_Location culoc;
		String id;
		HydroBase_StationGeolocCUClimWts hcli;
		String stationid;
		double precwt, tempwt;
		String culocRegion1;
		String culocRegion2;
		List hcliList;
		int matchCount = 0;	// Number of matching IDs that have data in HydroBase
		for (int i = 0; i < culocListSize; i++) {
			culoc = (StateCU_Location)culocList.get(i);
			id = culoc.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			culocRegion1 = culoc.getRegion1();
			culocRegion2 = culoc.getRegion2();
			// Now read the data from HydroBase (query each time because there are not many records)...
			hcliList = hbdmi.readStationGeolocCUClimWtsListForCountyAndHydrounit(
				culocRegion1, culocRegion2 );
			int hclListSize = 0;
			if ( hcliList != null ) {
				hclListSize = hcliList.size();
			}
			if ( hclListSize == 0 ) {
				// No data in HydroBase
				Message.printStatus(2, routine, "No climate station weights data in HydroBase for county \"" +
					culocRegion1 + "\" and HUC \"" + culocRegion2 + "\".");
				continue;
			}
			// Have data.  Need to add this combination to the cu location...
			// Zero out what is defined for the location...
			++matchCount;
			culoc.setNumClimateStations ( hclListSize );
			for ( int j = 0; j < hclListSize; j++ ) {
				hcli = (HydroBase_StationGeolocCUClimWts)hcliList.get(j);
				culoc.setClimateStationID ( stationid = hcli.getStation_id(), j );
				culoc.setTemperatureStationWeight ( tempwt = hcli.getTemp_wt(), j );
				culoc.setPrecipitationStationWeight ( precwt = hcli.getPcpn_wt(), j );
				Message.printStatus ( 2, routine, "Setting " + id +
					" climate station -> " + stationid +
					" tempwt=" + StringUtil.formatString(tempwt,"%.2f") +
					" precwt=" + StringUtil.formatString(precwt,"%.2f") );
			}
		}
		
		// If nothing was matched, perform additional actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "CU location \"" + ID + "\" was not matched: warning and not setting data.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct and that the " +
							"location has county and HUC." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "CU location \"" + ID +	"\" was not matched: failing and not setting data.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct and that the " +
							"location has county and HUC." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting CU location climate station data (" + e + ").";
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