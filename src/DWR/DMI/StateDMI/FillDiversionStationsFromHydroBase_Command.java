// FillDiversionStationsFromHydroBase_Command - This class initializes, checks, and runs the FillDiversionStationsFromHydroBase() command.

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
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Util;

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
This class initializes, checks, and runs the FillDiversionStationsFromHydroBase() command.
</p>
*/
public class FillDiversionStationsFromHydroBase_Command 
extends AbstractCommand implements Command
{
/**
Values for UserNameFrom parameter.
*/
protected final String _StructureName = "StructureName";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public FillDiversionStationsFromHydroBase_Command ()
{	super();
	setCommandName ( "FillDiversionStationsFromHydroBase" );
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
	String UserNameFrom = parameters.getValue ( "UserNameFrom" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}
	
	if ( (UserNameFrom != null) && (UserNameFrom.length() > 0) && !UserNameFrom.equalsIgnoreCase(_StructureName) ) {
		message = "The UserNameFrom value (" + UserNameFrom + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify UserNameFrom as " + _StructureName + " (default).") );
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
    valid_Vector.add ( "UserNameFrom" );
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
{	return (new FillStationsFromHydroBase_JDialog ( parent, this )).ok();
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
	String UserNameFrom = parameters.getValue ( "UserNameFrom" );
	if ( UserNameFrom == null ) {
		UserNameFrom = _StructureName; // Default and currently only choice
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of diversion stations...
	
	List stationList = null;
	int stationListSize = 0;
	try {
		stationList = (List)processor.getPropContents ( "StateMod_DiversionStation_List");
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting diversion station data from processor.";
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
		StateMod_Diversion div = null;
		HydroBase_StructureView hbdiv = null;
		List parts = null;
		int psize = 0; // Number of parts in a collection
		int iparts = 0; // Index for iterating through parts
		String part_id = ""; // Identifier for a part in a collection
		String name = "", user = "";
		int [] wdid_parts = new int[2]; // Parts when a WDID is parsed
		double capacity = 0.0;
		double area = 0.0;
		int demsrc = 0;
		String collectionType = null;
		String id = "";	// Diversion ID.
		int matchCount = 0;
		for ( int i = 0; i < stationListSize; i++ ) {
			try {
				div = (StateMod_Diversion)stationList.get(i);
				id = div.getID();
				if ( !id.matches(idpattern_Java) ) {
					// Identifier does not match...
					continue;
				}
				++matchCount;
				// Defaults for each structure...
		
				name = StateMod_Util.MISSING_STRING;
				user = StateMod_Util.MISSING_STRING;
				capacity = StateMod_Util.MISSING_DOUBLE;
				area = StateMod_Util.MISSING_DOUBLE;
				demsrc = StateMod_Util.MISSING_INT;
		
				// Read the structure from HydroBase.  If an aggregate, read
				// each structure and aggregate...
		
				if ( div.isCollection() ) {
					// Aggregate or system...
					psize = 0;
					parts = div.getCollectionPartIDs(0);
					if ( parts != null ) {
						psize = parts.size();
					}
					// Defaults...
					collectionType = div.getCollectionType();
					name = "Diversion " + collectionType;
					user = name; // Default to same as diversion
					capacity = 0.0;
					area = 0.0;
					for ( iparts = 0; iparts < psize; iparts++ ) {
						part_id = (String)parts.get(iparts);
						try {
							// Parse out the WDID...
							HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
						}
						catch ( Exception e ) {
							// Not a WDID - this is an error because valid structures are expected as
							// parts of an aggregate...
							message = "Location \"" + id + "\" (" + part_id + ") is not a WDID.";
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Confirm that only WDIDs are used for " +
									collectionType + " parts." ) );
							continue;
						}
						try {
							hbdiv = hbdmi.readStructureViewForWDID( wdid_parts[0], wdid_parts[1] );
						}
						catch ( Exception e ) {
							message = "Error getting structure data from HydroBase for " + id + " (part " + part_id +
							") (" + e + ")";
							Message.printWarning ( 3, routine, e );
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Verify that " + part_id +
									" has data in HydroBase - report to support." ) );
							continue;
						}
						if ( hbdiv == null ) {
							message = "No structure data from HydroBase for " + id + " (part " + part_id + ").";
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.WARNING,
									message, "Verify that " + part_id +
									" has data in HydroBase - not incrementing capacity and area with part." ) );
							// TODO SAM 2009-01-24 Evaluate whether to change error handling.
							continue;
						}
						// Accumulate the data.  This logic follows the old watright logic.
		
						// Capacity...
						if ( hbdiv.getEst_capacity() > 0.0 ) {
							capacity += hbdiv.getEst_capacity();
							Message.printStatus ( 2, routine, "Using " + id + " diversion " + collectionType +
								" " + part_id + " estimated capacity -> " + hbdiv.getEst_capacity() );
						}
						else if ( hbdiv.getDcr_capacity() > 0.0 ) {
							capacity += hbdiv.getDcr_capacity();
							Message.printStatus ( 2, routine, "Using " + id + " diversion " + collectionType +
								" " + part_id + " decreed capacity -> " + hbdiv.getDcr_capacity() );
						}
						else {
							// Most recent watright ignores missing capacity since the capacity will
							// most likely be increased to the historical diversion...
							Message.printStatus ( 2, routine, "Using " + id + " diversion " +
							collectionType + " " + part_id + " capacity -> missing (not added)." );
						}
		
						// TODO SAM 2009-01-22 Clean up this old code - some old choices are not needed.
						// Set the demand source as per watright using the "BEST3" option.
		
						if ( hbdiv.getTia_gis() > 0.0 ) {
							area += hbdiv.getTia_gis();
							Message.printStatus ( 2, routine, "Using " + id + " diversion " +
							collectionType + " " + part_id + " area (GIS " + hbdiv.getTia_gis_calyear() +
							") -> " + StringUtil.formatString(hbdiv.getTia_gis(),"%.3f") );
							demsrc = StateMod_Diversion.DEMSRC_GIS;
						}
						else if ( hbdiv.getTia_div() > 0.0 ) {
							area += hbdiv.getTia_div();
							Message.printStatus ( 2, routine, "Using " + id + " diversion " + collectionType +
							" " + part_id + " area (div comments " + hbdiv.getTia_div_calyear() + ") -> " +
							StringUtil.formatString(hbdiv.getTia_div(), "%.3f") );
							// Only reset if the demsrc is not GIS..
							if ( demsrc != StateMod_Diversion.DEMSRC_GIS ){
								demsrc = StateMod_Diversion.DEMSRC_TIA;
							}
						}
						else if ( hbdiv.getTia_struct() > 0.0 ) {
							area += hbdiv.getTia_struct();
							Message.printStatus ( 2, routine, "Using " + id + " diversion " + collectionType +
							" " + part_id + " area (TIA " + hbdiv.getTia_struct_calyear() +") -> " +
							StringUtil.formatString(hbdiv.getTia_struct(),"%.3f") );
							// Only reset if the demsrc is not GIS..
							if ( demsrc != StateMod_Diversion.DEMSRC_GIS ){
								demsrc = StateMod_Diversion.DEMSRC_TIA;
							}
						}
						else if ( hbdiv.getTia_struct() > 0.0 ) {
							Message.printStatus ( 2, routine, "Using " + id + " diversion " +
							collectionType + " " + part_id + " area (unknown) -> 0.0" );
						}
					}
				}
				else {
					// Single ditch...
					try {
						// Parse out the WDID...
						HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
					}
					catch ( Exception e ) {
						// Not a WDID - non-fatal, just ignore the diversion...
						Message.printStatus ( 2, routine, "Skipping diversion \"" + id +
						"\" - does not appear to be a WDID." );
						continue;
					}
					try {
						hbdiv = hbdmi.readStructureViewForWDID( wdid_parts[0], wdid_parts[1] );
					}
					catch ( Exception e ) {
						Message.printWarning ( 3, routine,
						"Error getting structure data from HydroBase for " + id );
						Message.printWarning ( 3, routine, e );
						// Not fatal because aggregates and other nodes may not be in HydroBase...
						//++warning_count;
						// TODO SAM 2009-01-24 Evaluate whether to change error handling.
						continue;
					}
					if ( hbdiv == null ) {
						Message.printStatus ( 2, routine, "No structure data from HydroBase for " + id );
						// Not fatal because aggregates and other nodes may not be in HydroBase...
						//++warning_count;
						// TODO SAM 2009-01-24 Evaluate whether to change error handling.
						continue;
					}
					name = hbdiv.getStr_name();
					if ( UserNameFrom.equalsIgnoreCase("StructureName") ) {
						user = hbdiv.getStr_name();
					}
					else {
						// Query the rolodex, etc.
						// TODO SAM 2004-06-08 currently not implemented.
					}
					// Capacity, similar to aggregate code...
					if ( hbdiv.getEst_capacity() > 0.0 ) {
						capacity = hbdiv.getEst_capacity();
						Message.printStatus ( 2, routine, "Using " + id + " estimated capacity -> " +
						hbdiv.getEst_capacity() );
					}
					else if ( hbdiv.getDcr_capacity() > 0.0 ) {
						capacity = hbdiv.getDcr_capacity();
						Message.printStatus ( 2, routine, "Using " + id + " decreed capacity -> " +
						hbdiv.getDcr_capacity() );
					}
					else {
						// Default 999 value as per watright code
						// TODO SAM 2004-06-08 may need a better way to handle but often gets
						// reset to the maximum diversion value.
						capacity = 999.0;
						Message.printStatus ( 2, routine, "Using " + id + " default capacity -> 999" );
					}
					// Demsrc, similar to the aggregate code above...
					if ( hbdiv.getTia_gis() > 0.0 ) {
						area = hbdiv.getTia_gis();
						Message.printStatus ( 2, routine, "Using " + id + " area (GIS " +
						hbdiv.getTia_gis_calyear() + ") -> " + StringUtil.formatString(hbdiv.getTia_gis(),"%.3f") );
						demsrc = StateMod_Diversion.DEMSRC_GIS;
					}
					else if ( hbdiv.getTia_div() > 0.0 ) {
						area = hbdiv.getTia_div();
						Message.printStatus ( 2, routine, "Using " + id + " area (div comments " +
						hbdiv.getTia_div_calyear() + ") -> " + StringUtil.formatString(hbdiv.getTia_div(),"%.3f") );
						// Only reset if the demsrc is not GIS..
						if ( demsrc != StateMod_Diversion.DEMSRC_GIS ){
							demsrc = StateMod_Diversion.DEMSRC_TIA;
						}
					}
					else if ( hbdiv.getTia_struct() > 0.0 ) {
						area = hbdiv.getTia_struct();
						Message.printStatus ( 2, routine, "Using " + id + " area (TIA " +
						hbdiv.getTia_struct_calyear() + ") -> " +
						StringUtil.formatString(hbdiv.getTia_struct(),"%.3f") );
						// Only reset if the demsrc is not GIS..
						if ( demsrc != StateMod_Diversion.DEMSRC_GIS ){
							demsrc = StateMod_Diversion.DEMSRC_TIA;
						}
					}
					else {
						area = 0.0;
						Message.printStatus ( 2, routine, "Using " + id + " area (unknown) -> 0.0" );
						demsrc = StateMod_Diversion.DEMSRC_UNKNOWN;
					}
				}
		
				// Reset the data with the specified filled values.
		
				if ( StateMod_Util.isMissing(div.getName()) ) {
					Message.printStatus ( 2, routine, "Filling " + id + " Name -> " + name );
					div.setName ( name );
				}
				if ( StateMod_Util.isMissing(div.getUsername()) ) {
					Message.printStatus ( 2, routine, "Filling " + id + " UserName -> " + user );
					div.setUsername ( user );
				}
				if ( StateMod_Util.isMissing(div.getDivcap()) ) {
					Message.printStatus ( 2, routine,
					"Filling " + id + " Capacity -> " + StringUtil.formatString( capacity,"%.2f") );
					div.setDivcap ( capacity );
				}
				if ( StateMod_Util.isMissing(div.getArea()) ) {
					Message.printStatus ( 2, routine,
					"Filling " + id + " Area -> " + StringUtil.formatString( area,"%.2f") );
					div.setArea ( area );
				}
				if ( StateMod_Util.isMissing(div.getDemsrc()) ) {
					Message.printStatus ( 2, routine, "Filling " + id + " Demsrc -> " + demsrc );
					div.setDemsrc ( demsrc );
				}
			}
			catch ( Exception e2 ) {
		        Message.printWarning ( 3, routine, e2 );
		        message = "Unexpected error filling reservoir station " + id + " from HydroBase (" + e2 + ").";
		        Message.printWarning ( warning_level, 
		            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		        status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Check log file for details." ) );
			}
		}
		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Diversion \"" + ID + "\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Diversion \"" + ID + "\" was not matched: failing and not filling.";
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
        message = "Unexpected error filling diversion stations from HydroBase (" + e + ").";
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
	String UserNameFrom = parameters.getValue ( "UserNameFrom" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( UserNameFrom != null && UserNameFrom.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UserNameFrom=" + UserNameFrom );
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
