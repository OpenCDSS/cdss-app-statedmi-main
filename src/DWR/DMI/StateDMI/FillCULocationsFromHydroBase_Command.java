// FillCULocationsFromHydroBase_Command - This class initializes, checks, and runs the FillCULocationsFromHydroBase() command.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;

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
This class initializes, checks, and runs the FillCULocationsFromHydroBase() command.
</p>
*/
public class FillCULocationsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for CULocType.
*/
protected final String _Structure = "Structure";

/**
Values for Region1Type.
*/
protected final String _County = "County";

/**
Values for Region2Type.
*/
protected final String _HUC = "HUC";
	
/**
Constructor.
*/
public FillCULocationsFromHydroBase_Command ()
{	super();
	setCommandName ( "FillCULocationsFromHydroBase" );
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
	String CULocType = parameters.getValue ( "CULocType" );
	String Region1Type = parameters.getValue ( "Region1Type" );
	String Region2Type = parameters.getValue ( "Region2Type" );
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

	if ( (CULocType != null) && (CULocType.length() > 0) && !CULocType.equalsIgnoreCase(_Structure) ) {
		message = "The CULocType value (" + CULocType + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify CULocType as " + _Structure + " (default).") );
	}
	
	if ( (Region1Type != null) && (Region1Type.length() > 0) && !Region1Type.equalsIgnoreCase(_County) ) {
		message = "The Region1Type value (" + Region1Type + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify Region1Type as " + _County + " (default).") );
	}
	
	if ( (Region2Type != null) && (Region2Type.length() > 0) && !Region2Type.equalsIgnoreCase(_HUC) ) {
		message = "The Region2Type value (" + Region1Type + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify Region2Type as " + _HUC + " or blank (default).") );
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
	List<String> valid_Vector = new Vector<String>(5);
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "CULocType" );
    valid_Vector.add ( "Region1Type" );
    valid_Vector.add ( "Region2Type" );
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
{	return (new FillCULocationsFromHydroBase_JDialog ( parent, this )).ok();
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
	String CULocType = parameters.getValue ( "CULocType" );
	if ( CULocType == null ) {
		CULocType = _Structure; // Default
	}
	String Region1Type = parameters.getValue ( "Region1Type" );
	if ( Region1Type == null ) {
		Region1Type = _County; // Default
	}
	//String Region2Type = parameters.getValue ( "Region2Type" ); // OK if null (don't fill)
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of climate stations...
	
	List<StateCU_Location> culocList = null;
	int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
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
		// Loop through the structures and process each matching structure...

		StateCU_Location culoc = null;
		HydroBase_StructureView str;
		String id;
		int [] wdid_parts = new int[2];
		for ( int i = 0; i < culocListSize; i++ ) {
			culoc = (StateCU_Location)culocList.get(i);
			id = culoc.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			if ( CULocType.equalsIgnoreCase(_Structure) ) {
				// Query structures in HydroBase, assuming that the
				// numeric identifiers are WDIDs.  Then use the
				// resulting geoloc.latitude, geoloc.county, geoloc.huc,
				// and structure.str_name to fill missing data.  Later
				// will add other location types like parcel.

				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - this is an error for a numeric
					// ID because valid structures are expected to have WDIDs...
					if ( StringUtil.isInteger(id) ) {
						message = "Numeric ID \"" + id + "\" is not a " +
						"WDID.  Location can't be filled from Hydrobase.  Check the ID.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Check the identifier for the location being filled." ) );
						continue;
					}
					else {
						// Let it pass without incrementing the warning count...
						Message.printStatus( 2, routine,
						"Non-WDID location \"" + id + "\" cannot be filled from HydroBase.  Skipping..." );
						continue;
					}
				}
				// Now do the query...
				str = null;
				try {
					str = hbdmi.readStructureViewForWDID ( wdid_parts[0], wdid_parts[1] );
				}
				catch ( Exception e ) {
					message = "Error getting structure \"" + id + "\" data from HydroBase (" + e + ") " +
					"Last query: \"" + hbdmi.getLastQueryString() + "\"";
					Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
					Message.printWarning ( 3, routine, e );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Check the identifier for the location being filled." ) );
					continue;
				}
				if ( str == null ) {
					if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
						// Do nothing
						message = "No HydroBase data found for location \"" + id +
						"\": ignoring and not filling.";
						Message.printStatus(2,routine, message );
					}
					else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
						message = "No HydroBase data found for location \"" + id +
						"\": warning and not filling.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify that the identifier is correct." ) );
					}
					else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
						message = "No HydroBase data found for location \"" + id +
						"\": failing and not filling.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the identifier is correct." ) );
					}
					continue;
				}

				// Reset the data with the specified filled values.
				// Currently region1 is hard-coded to be county, and region2 is HUC.
				// If StateDMI is made more flexible, some additional checks will need to be put in here
				// to account for other options.

				if ( StateCU_Util.isMissing(culoc.getRegion1()) ) {
					Message.printStatus ( 2, routine,
					"Filling " + id + " Region1 -> County: " + str.getCounty() );
					culoc.setRegion1 ( str.getCounty() );
				}
				if ( StateCU_Util.isMissing(culoc.getRegion2()) ) {
					culoc.setRegion2 ( str.getHUC() );
					Message.printStatus ( 2, routine,
					"Filling " + id + " Region2 -> HUC: " + str.getHUC() );
				}
				if ( StateCU_Util.isMissing(culoc.getLatitude()) ) {
					culoc.setLatitude ( str.getLatdecdeg() );
					Message.printStatus ( 2, routine, "Filling " + id + " Latitude -> " +
					StringUtil.formatString(str.getLatdecdeg(),"%11.6f") );
				}
				if ( StateCU_Util.isMissing(culoc.getName()) ) {
					culoc.setName ( str.getStr_name() );
					Message.printStatus ( 2, routine,
					"Filling " + id + " Name -> " +	str.getStr_name() );
				}
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error filling CU location data (" + e + ").";
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
	String CULocType = parameters.getValue ( "CULocType" );
	String Region1Type = parameters.getValue ( "Region1Type" );
	String Region2Type = parameters.getValue ( "Region2Type" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( CULocType != null && CULocType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CULocType=" + CULocType );
	}
	if ( Region1Type != null && Region1Type.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region1Type=" + Region1Type );
	}
	if ( Region2Type != null && Region2Type.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region2Type=" + Region2Type );
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
