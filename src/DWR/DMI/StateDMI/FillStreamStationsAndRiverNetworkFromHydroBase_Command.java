package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_StationView;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_Data;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
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
This class initializes, checks, and runs the FillStreamGageStationsFromHydroBase(),
FillStreamEstimateStationsFromHydroBase, and FillRiverNetworkFromHydroBase() commands.
</p>
*/
public class FillStreamStationsAndRiverNetworkFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Used with NameFormat
*/
private final String __StationName = "StationName";
private final String __StationName_NodeType = "StationName_NodeType";

/**
Used with CheckStructures
*/
private final String __False = "False";
private final String __True = "True";

/**
Constructor.
*/
public FillStreamStationsAndRiverNetworkFromHydroBase_Command ()
{	super();
	setCommandName ( "Fill?FromHydroBase" );
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
	String NameFormat = parameters.getValue ( "NameFormat" );
	String CheckStructures = parameters.getValue ( "CheckStructures" );
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
	
	if ( this instanceof FillStreamGageStationsFromHydroBase_Command ||
		(this instanceof FillStreamEstimateStationsFromHydroBase_Command)){
		if ( (CheckStructures != null) && (CheckStructures.length() > 0) &&
			!CheckStructures.equalsIgnoreCase(__False) &&
			!CheckStructures.equalsIgnoreCase(__True)) {
			message = "The CheckStructures value (" + NameFormat + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify CheckStructures as " + __False + " (default) or " +
					__True + ".") );
		}
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
    if ( (this instanceof FillStreamGageStationsFromHydroBase_Command) ||
		(this instanceof FillStreamEstimateStationsFromHydroBase_Command)||
		(this instanceof FillRiverNetworkFromHydroBase_Command)){
    	valid_Vector.add ( "NameFormat" );
    }
    if ( this instanceof FillStreamGageStationsFromHydroBase_Command ||
    	(this instanceof FillStreamEstimateStationsFromHydroBase_Command)) {
    	valid_Vector.add ( "CheckStructures" );
    }
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
	String NameFormat = parameters.getValue ( "NameFormat" );
	if ( NameFormat == null ) {
		NameFormat = __StationName; // Default
	}
	String CheckStructures = parameters.getValue ( "CheckStructures" );
	if ( (this instanceof FillStreamEstimateStationsFromHydroBase_Command) ||
		(this instanceof FillRiverNetworkFromHydroBase_Command) ) {
		CheckStructures = __True; // Default for network and stream estimate because stations and structures are mixed
	}
	else if ( CheckStructures == null ) {
		CheckStructures = __False; // Default for gages since should be stations
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of climate stations...
	
	List stationList = null;
	int stationListSize = 0;
	int compType = StateMod_DataSet.COMP_UNKNOWN; // Use integers to increase performance below
	try {
		if ( this instanceof FillStreamGageStationsFromHydroBase_Command ) {
			stationList = (List)processor.getPropContents ( "StateMod_StreamGageStation_List");
			compType = StateMod_DataSet.COMP_STREAMGAGE_STATIONS;
		}
		else if ( this instanceof FillStreamEstimateStationsFromHydroBase_Command ) {
			stationList = (List)processor.getPropContents ( "StateMod_StreamEstimateStation_List");
			compType = StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS;
		}
		else if ( this instanceof FillRiverNetworkFromHydroBase_Command ) {
			stationList = (List)processor.getPropContents ( "StateMod_RiverNetworkNode_List");
			compType = StateMod_DataSet.COMP_RIVER_NETWORK;
		}
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting data from processor (" + e + ").";
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
		// Query stations in HydroBase, assuming that the identifiers are station IDs.
		// Then use the resulting station.station_name to fill missing data.
		// Do separate queries to simplify the use of stored procedures to a single item read.

		HydroBase_StationView sg = null;
		HydroBase_StructureView str = null;
		StateMod_StreamGage gage = null;
		StateMod_StreamEstimate estimate = null;
		StateMod_RiverNetworkNode rin = null;
		StateMod_Data smdata = null; // To allow polymorphism
		String id = null, name;
		String hb_name = null; // Name returned from HydroBase
		String node_type = null; // Node type used in formatting the name.
		int [] wdid_parts = new int[2];	// WDID parts for structure query
		boolean isWDID = false; // Indicates if an ID is for a structure with a WDID
		for ( int i = 0; i < stationListSize; i++ ) {
			if ( compType == StateMod_DataSet.COMP_STREAMGAGE_STATIONS ) {
				gage = (StateMod_StreamGage)stationList.get(i);
				smdata = gage;
			}
			else if ( compType == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS ) {
				estimate = (StateMod_StreamEstimate)stationList.get(i);
				smdata = estimate;
			}
			else if ( compType == StateMod_DataSet.COMP_RIVER_NETWORK ) {
				rin = (StateMod_RiverNetworkNode)stationList.get(i);
				smdata = rin;
			}
			id = smdata.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}

			// Read the station from HydroBase, which will be a join of Station and Geoloc data...

			try {
				sg = hbdmi.readStationViewForStation_id(id);
				hb_name = sg.getStation_name();
				node_type = "FLO";
			}
			catch ( Exception e ) {
				// Mainly for debugging right now since it may not be a station ID.
				// This can be removed later...
				if ( !CheckStructures.equalsIgnoreCase("True") ) {
					Message.printStatus ( 2, routine,
					"Error getting station data from HydroBase for \"" + id +
					"\" - not checking structures because CheckStructures=False" );
					if ( Message.isDebugOn ) {
						Message.printDebug ( 3, routine, e );
					}
					continue;
				}
			}
			if ( sg == null ) {
				// Invalid ID...
				if ( !CheckStructures.equalsIgnoreCase(__True) ) {
					Message.printStatus ( 2, routine, "No station data in HydroBase for \"" + id +
						"\" - not checking structures because CheckStructures=False" );
					continue;
				}
				// Read from HydroBase as a structure...

				isWDID = true;
				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - check below if no data have been found...
					isWDID = false;
					str = null;
				}
				if ( isWDID ) {
					try {
						str = hbdmi.readStructureViewForWDID ( wdid_parts[0], wdid_parts[1] );
					}
					catch ( Exception e ) {
						// Mainly for debugging right now.  This can be removed later...
						Message.printStatus ( 2, routine,
						"Error getting structure data from HydroBase for \"" + id + "\"" );
						if ( Message.isDebugOn ) {
							Message.printDebug ( 2, routine, e );
						}
						continue;
					}
				}
				if ( str == null ) {
					// Invalid ID...
					Message.printStatus ( 2, routine, "No station or structure data in HydroBase for \"" + id + "\"" );
					continue;
				}
				// Set the data to use below...
				hb_name = str.getStr_name();
				// The node type may be available if the original list
				// of stations was read from a network file...
				int related_smdata_type = StateMod_DataSet.COMP_UNKNOWN;
				if ( compType == StateMod_DataSet.COMP_STREAMGAGE_STATIONS ) {
					related_smdata_type = gage.getRelatedSMDataType();
				}
				else if ( compType == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS ) {
					related_smdata_type = estimate.getRelatedSMDataType();
				}
				else if ( compType == StateMod_DataSet.COMP_RIVER_NETWORK ) {
					related_smdata_type = rin.getRelatedSMDataType();
				}
				node_type = "";
				if ( related_smdata_type != StateMod_DataSet.COMP_UNKNOWN ) {
					// Use this data type...
					if ( related_smdata_type == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
						if ( compType == StateMod_DataSet.COMP_STREAMGAGE_STATIONS ) {
							if (gage.getRelatedSMDataType2() == StateMod_DataSet.COMP_WELL_STATIONS ) {
								node_type = "D&W";
							}
							else {
								node_type = "DIV";
							}
						}
						else if ( compType == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS ) {
							if ( estimate.getRelatedSMDataType2() == StateMod_DataSet.COMP_WELL_STATIONS ) {
								node_type = "D&W";
							}
							else {
								node_type = "DIV";
							}
						}
						else if ( compType == StateMod_DataSet.COMP_RIVER_NETWORK ) {
							if ( rin.getRelatedSMDataType2() == StateMod_DataSet.COMP_WELL_STATIONS ) {
								node_type = "D&W";
							}
							else {
								node_type = "DIV";
							}
						}
					}
					else if ( related_smdata_type == StateMod_DataSet.COMP_RESERVOIR_STATIONS ) {
						node_type = "RES";
					}
					else if ( related_smdata_type == StateMod_DataSet.COMP_INSTREAM_STATIONS ) {
						node_type = "ISF";
					}
					else if ( related_smdata_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
						node_type = "WEL";
					}
				}
				else {
					// Use the type from HydroBase.  It is possible that the user has assigned
					// a node type to be different from HydroBase.  HydroBase also cannot
					// indicate whether a D&W node...
					String str_type = str.getStr_type();
					if ( str_type.equalsIgnoreCase("H") ) {
						node_type = "DIV";
					}
					else if ( str_type.equalsIgnoreCase("MFR") ) {
						node_type = "ISF";
					}
					else if ( str_type.equalsIgnoreCase("R") ) {
						node_type = "RES";
					}
					else if(str_type.equalsIgnoreCase("W")){
						node_type = "WEL";
					}
				}
			}

			// Reset the data with the specified filled values.

			if ( StateMod_Util.isMissing(smdata.getName()) ) {
				if ( NameFormat.equals(__StationName_NodeType) ) {
					name = StringUtil.formatString(hb_name,"%-20.20s") + "_" + node_type;
				}
				else {
					// "StationName" - default
					name = hb_name;
				}
				Message.printStatus ( 2, routine, "Filling " + id + " Name -> " + name );
				smdata.setName ( name );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error filling data from HydroBase (" + e + ").";
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
	String NameFormat = parameters.getValue ( "NameFormat" );
	String CheckStructures = parameters.getValue ( "CheckStructures" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

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
	if ( this instanceof FillStreamGageStationsFromHydroBase_Command ||
		(this instanceof FillStreamEstimateStationsFromHydroBase_Command)){
		if ( CheckStructures != null && CheckStructures.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "CheckStructures=" + CheckStructures );
		}
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