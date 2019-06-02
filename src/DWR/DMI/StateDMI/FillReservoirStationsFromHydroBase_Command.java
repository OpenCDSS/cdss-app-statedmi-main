// FillReservoirStationsFromHydroBase_Command - This class initializes, checks, and runs the FillReservoirStationsFromHydroBase() command.

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
import DWR.DMI.HydroBaseDMI.HydroBase_AreaCap;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureReservoir;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirAreaCap;
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
This class initializes, checks, and runs the FillReservoirStationsFromHydroBase() command.
</p>
*/
public class FillReservoirStationsFromHydroBase_Command 
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
public FillReservoirStationsFromHydroBase_Command ()
{	super();
	setCommandName ( "FillReservoirStationsFromHydroBase" );
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
		message = "An identifier or pattern must be specified.";
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
	List<String> valid_Vector = new Vector<String>(2);
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
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of reservoir stations...
	
	List<StateMod_Reservoir> stationList = null;
	int stationListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List");
		stationList = dataList;
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting reservoir station data from processor.";
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
		StateMod_Reservoir res = null;
		HydroBase_StructureReservoir hbres = null;

		StateMod_ReservoirAreaCap smareacap = null, smareacap2; // Area cap data for StateMod.
		List<StateMod_ReservoirAreaCap> smareacapList; // List of StateMod_ReservoirAreaCap to set in a reservoir.
		int nsmareacap = 0; // Number of StateMod area capacities.
		HydroBase_AreaCap hbareacap; // Area cap data from HydroBase.
		List<HydroBase_AreaCap> hbareacapList = null; // Vector of HydroBase_AreaCap.
		int nhbareacap = 0; // Number of HydroBase area capacities.

		List<String> parts = null;
		int psize = 0; // Number of parts in a collection
		int iparts = 0; // Index for iterating through parts
		String part_id = ""; // Identifier for a part in a collection
		String name = "";
		int [] wdid_parts = new int[2];	// Parts when a WDID is parsed
		double volmax = 0.0;
		String collection_type = null;
		String id; // Reservoir ID.
		int matchCount = 0;
		for ( int i = 0; i < stationListSize; i++ ) {
			res = stationList.get(i);
			id = res.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Defaults for each structure...

			name = StateMod_Util.MISSING_STRING;
			volmax = StateMod_Util.MISSING_DOUBLE;

			// Read the structure from HydroBase.  If an aggregate, read each structure and aggregate...

			if ( res.isCollection() ) {
				// Aggregate or system...
				psize = 0;
				parts = res.getCollectionPartIDs(0);
				if ( parts != null ) {
					psize = parts.size();
				}
				// Defaults...
				collection_type = res.getCollectionType();
				name = "Reservoir " + collection_type;
				volmax = 0.0;
				//area = 0.0;
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
								message, "Verify that the reservoir aggregate part identifier is a WDID." ) );
						continue;
					}
					try {
						hbres = hbdmi.readStructureReservoirForWDID ( wdid_parts[0], wdid_parts[1] );
					}
					catch ( Exception e ) {
						message = "Error getting structure data from HydroBase for " + id +
						" (part " + part_id + ")";
						Message.printWarning ( 3, routine, e );
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the reservoir aggregate part " +
									"identifier is a WDID and has data in HydroBase." ) );
						continue;
					}
					if ( hbres == null ) {
						message = "No structure/reservoir data from " +
						"HydroBase for " + id + " (part " + part_id + ")";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify that the reservoir has data in HydroBase." ) );
						continue;
					}
					// Accumulate the data.  This logic follows the old watright logic.

					// Max volumne...
					if ( hbres.getMax_storage() > 0.0 ) {
						volmax += hbres.getMax_storage();
						Message.printStatus ( 2, routine, "Using " + id + " reservoir " +
						collection_type + " " + part_id + " maximum volume -> " + hbres.getMax_storage() );
					}
					else {
						Message.printStatus ( 2, routine, "Missing " + id + " reservoir " +
						collection_type + " " + part_id +
						" maximum volume -> might need to set manually." );
						// Not a a fatal warning
						// FIXME SAM 2009-01-18 Make sure to catch in data check
					}
				}
				// TODO SAM 2004-09-13 - collections do not get their
				// content/area/seepage curves assigned.
			}
			else {
				// Single reservoir...
				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - non-fatal, just ignore the diversion...
					Message.printStatus ( 2, routine,
					"Skipping reservoir \"" + id + "\" - does not appear to be a WDID." );
					// FIXME SAM 2009-01-18 Evaluate whether this should be an error or catch
					// in the data check code.
					continue;
				}
				try {
					hbres = hbdmi.readStructureReservoirForWDID ( wdid_parts[0], wdid_parts[1] );
				}
				catch ( Exception e ) {
					Message.printStatus ( 2, routine,
					"Error getting structure data from HydroBase for " + id );
					Message.printWarning ( 3, routine, e );
					// Not fatal because aggregates and other nodes
					// may not be in HydroBase...
					//++warning_count;
					// FIXME SAM 2009-01-18 Evaluate whether this should be an error or catch
					// in the data check code.
					continue;
				}
				if ( hbres == null ) {
					Message.printStatus ( 2, routine,
					"No structure/reservoir data from HydroBase for " + id );
					// Not fatal because aggregates and other nodes
					// may not be in HydroBase...
					//++warning_count;
					// FIXME SAM 2009-01-18 put in data check code
					continue;
				}
				name = hbres.getStr_name();
				// Max volume, similar to aggregate code...
				if ( hbres.getMax_storage() > 0.0 ) {
					volmax = hbres.getMax_storage();
					Message.printStatus ( 2, routine,
					"Using " + id + " reservoir maximum volume -> " + volmax );
				}
				else {
					Message.printWarning ( 3, routine,
					"Missing " + id + " reservoir maximum volume -> might need to set manually." );
					// Not a a fatal warning
					// FIXME SAM 2009-01-18 put in data check code
				}

				// Get the area capacity information...

				try {
					hbareacapList = hbdmi.readAreaCapListForStructure_num ( hbres.getStructure_num() );
				}
				catch ( Exception e ) {
					Message.printStatus ( 2, routine, "Missing " + id +
						" reservoir content/area/seepage in HydroBase -> need to set manually." );
					hbareacapList = null;
					// FIXME SAM 2009-01-18 put in data check code
				}
			}

			// Reset the data with the specified filled values.

			if ( StateMod_Util.isMissing(res.getName()) ) {
				Message.printStatus ( 2, routine, "Filling " + id + " Name -> " + name );
				res.setName ( name );
			}
			if ( StateMod_Util.isMissing(res.getVolmax()) ) {
				Message.printStatus ( 2, routine,
				"Filling " + id + " VolMax -> " + StringUtil.formatString( volmax,"%.2f") );
				res.setVolmax ( volmax );
			}
			nsmareacap = 0;
			if ( res.getAreaCaps() != null ) {
				nsmareacap = res.getAreaCaps().size();
			}
			nhbareacap = 0;
			if ( hbareacapList != null ) {
				nhbareacap = hbareacapList.size();
			}
			if ( (nsmareacap == 0) && (nhbareacap != 0) ) {
				// No data so fill from HydroBase...
				Message.printStatus ( 2, routine,
				"Filling " + id + " ContentAreaSeepage -> " + nhbareacap + " values found in HydroBase." );
				smareacapList = new Vector<StateMod_ReservoirAreaCap>(nhbareacap);
				for ( int iac = 0; iac < nhbareacap; iac++ ) {
					hbareacap = hbareacapList.get(iac);
					smareacap = new StateMod_ReservoirAreaCap();
					smareacap.setConten ( hbareacap.getVolume() );
					smareacap.setSurarea ( hbareacap.getSurface_area() );
					smareacap.setSeepage ( 0.0 );
					smareacapList.add ( smareacap );
				}
				// Automatically add  bounding values...
				smareacap2 = new StateMod_ReservoirAreaCap();
				smareacap2.setConten ( 9999999.0 );
				smareacap2.setSurarea ( smareacap.getSurarea() );
				smareacap2.setSeepage ( smareacap.getSeepage() );
				Message.printStatus ( 2, routine,
				"Filling " + id + " ContentAreaSeepage -> automatically adding high bound." );
				smareacapList.add ( smareacap2 );
				// Minimum
				smareacap = (StateMod_ReservoirAreaCap)smareacapList.get(0);
				if ( smareacap.getConten() != 0.0 ) {
					smareacap2 = new StateMod_ReservoirAreaCap();
					smareacap2.setConten ( 0.0 );
					smareacap2.setSurarea ( 0.0 );
					smareacap2.setSeepage ( 0.0 );
					smareacapList.add (0,smareacap2);
					Message.printStatus ( 2, routine,
					"Filling " + id + " ContentAreaSeepage -> automatically adding low zero bound." );
				}
				res.setAreaCaps ( smareacapList );
			}
		}
		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Reservoir \"" + ID + "\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Reservoir \"" + ID +	"\" was not matched: failing and not filling.";
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
        message = "Unexpected error filling reservoir stations from HydroBase (" + e + ").";
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
