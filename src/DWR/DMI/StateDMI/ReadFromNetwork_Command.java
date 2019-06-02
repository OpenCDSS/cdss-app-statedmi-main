// ReadFromNetwork_Command - This class initializes, checks, and runs the Read*FromNetwork() commands.

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

import cdss.domain.hydrology.network.HydrologyNode;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Well;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the Read*FromNetwork() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the ReadDiversionStationsFromNetwork()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class ReadFromNetwork_Command extends AbstractCommand implements Command
{
	
/**
Flags for Ignore* and Append fields:
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Input file that is read by this command.
*/
private File __InputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public ReadFromNetwork_Command ()
{	super();
	setCommandName ( "Read?FromNetwork" );
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
{	String routine = "ReadFromStateMod_Command.checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String IncludeStreamEstimateStations = parameters.getValue ( "IncludeStreamEstimateStations" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
    // The input file is not required (network can be taken from editor in memory).
	if ( (InputFile != null) && (InputFile.length() != 0) ) {
		String working_dir = null;		
			try {
				Object o = processor.getPropContents ( "WorkingDir" );
				// Working directory is available so use it...
				if ( o != null ) {
					working_dir = (String)o;
				}
			}
			catch ( Exception e ) {
				message = "Error requesting WorkingDir from processor.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Software error - report to support." ) );
			}
	
		try {
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile));
			File f = new File ( adjustedPath );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The input file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the input directory." ) );
			}
			else if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The input file:\n" +
			"    \"" + InputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that input file and working directory paths are compatible." ) );
		}
	}
	
	if ( this instanceof ReadStreamGageStationsFromNetwork_Command ) {
		if ( (IncludeStreamEstimateStations != null) && (IncludeStreamEstimateStations.length() > 0) &&
			!IncludeStreamEstimateStations.equalsIgnoreCase(_False) && !IncludeStreamEstimateStations.equalsIgnoreCase(_True) ) {
			message = "The IncludeStreamEstimateStations value (" + IncludeStreamEstimateStations + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IncludeStreamEstimateStations as " + _False + " (default), or " + _True + ".") );
		}
	}
    
	// Check for invalid parameters...
	Vector<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "InputFile" );
	if ( this instanceof ReadStreamGageStationsFromNetwork_Command ){
		valid_Vector.add ( "IncludeStreamEstimateStations" );
	}
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
	return (new ReadFromNetwork_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getInputFile ()
{
	return __InputFile_File;
}

/**
Read StateMod diversion stations.
*/
private int readDiversionStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net )
throws Exception
{	//String routine = "ReadFromNetwork_Command.readDiversionStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionStationMatchList() );

	// Now process the data from the network.  First get the diversion stations...

	List<HydrologyNode> diversionStationList = net.getNodesForType(HydrologyNode.NODE_TYPE_DIV);
	int diversionStationListSize = 0;
	if ( diversionStationList != null ) {
		diversionStationListSize = diversionStationList.size();
	}

	// Loop through the data and add new StateMod_Diversion instances for each entry...

	StateMod_Diversion div;
	HydrologyNode node = null;
	for (int i = 0; i < diversionStationListSize; i++) {
		node = diversionStationList.get(i);
		div = new StateMod_Diversion ( false );
		div.setID ( node.getCommonID() );
		div.setCgoto ( node.getCommonID() );
		// TODO SAM 2004-04-12 - need to assign available data.  Maybe have options of what to
		// assign (name).  Replace or add in the diversion list.
		processor.findAndAddSMDiversion ( div, true );
	}
	
	// Now get the diversion/wells...

	diversionStationList = net.getNodesForType(HydrologyNode.NODE_TYPE_DIV_AND_WELL);
	diversionStationListSize = 0;
	if ( diversionStationList != null ) {
		diversionStationListSize = diversionStationList.size();
	}

	// Loop through the data and add new StateMod_Diversion instances for each entry...

	for (int i = 0; i < diversionStationListSize; i++) {
		node = (HydrologyNode)diversionStationList.get(i);
		div = new StateMod_Diversion( false );
		div.setID ( node.getCommonID() );
		div.setCgoto ( node.getCommonID() );
		// TODO SAM 2004-04-12 - need to assign available
		// data.  Maybe have options of what to assign (name).
		// Replace or add in the __SMDiversion_Vector...
		processor.findAndAddSMDiversion ( div, true );
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModDiversionStationMatchList(), "Diversion Stations" );
	return warning_count;
}

/**
Read StateMod instream flow stations.
*/
private int readInstreamFlowStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net )
throws Exception
{	//String routine = "ReadFromNetwork_Command.readInstreamFlowStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModInstreamFlowStationMatchList() );

	// Now process the data from the network.  First get the instream flow stations...

	List<HydrologyNode> instreamFlowStationList = net.getNodesForType(HydrologyNode.NODE_TYPE_ISF);
	int instreamFlowStationSize = 0;
	if ( instreamFlowStationList != null ) {
		instreamFlowStationSize = instreamFlowStationList.size();
	}

	// Loop through the data and add new StateMod_InstreamFlow instances for each entry...

	StateMod_InstreamFlow ifs;
	HydrologyNode node = null;
	for (int i = 0; i < instreamFlowStationSize; i++) {
		node = instreamFlowStationList.get(i);
		ifs = new StateMod_InstreamFlow ( false );
		ifs.setID ( node.getCommonID() );
		ifs.setCgoto ( node.getCommonID() );
		// State standard...
		ifs.setIfrrdn ( node.getCommonID() + "_Dwn" );
		// TODO SAM 2004-04-12 - need to assign available data.
		// Maybe have options of what to assign (name).
		// Replace or add in the __SMInstreamFlow_Vector...
		processor.findAndAddSMInstreamFlow ( ifs, true );
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModInstreamFlowStationMatchList(), "Instream Flow Stations" );
	return warning_count;
}

/**
Read StateMod reservoir stations.
*/
private int readReservoirStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net )
throws Exception
{	//String routine = "ReadFromNetwork_Command.readReservoirStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModReservoirStationMatchList() );

	// Now process the data from the network.  First get the reservoir stations...

	List<HydrologyNode> reservoirStationList = net.getNodesForType(HydrologyNode.NODE_TYPE_RES);
	int reservoirStationSize = 0;
	if ( reservoirStationList != null ) {
		reservoirStationSize = reservoirStationList.size();
	}

	// Loop through the data and add new StateMod_Reservoir instances for each entry...

	StateMod_Reservoir res;
	HydrologyNode node = null;
	for (int i = 0; i < reservoirStationSize; i++) {
		node = reservoirStationList.get(i);
		res = new StateMod_Reservoir ( false );
		res.setID ( node.getCommonID() );
		res.setCgoto ( node.getCommonID() );
		// TODO SAM 2004-04-12 - need to assign available data.  Maybe have options of what to
		// assign (name).  Replace or add in the reservoir list.
		processor.findAndAddSMReservoir ( res, true );
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModReservoirStationMatchList(), "Reservoir Stations" );
	return warning_count;
}

/**
Read StateMod stream estimate stations.
*/
private int readStreamEstimateStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net )
throws Exception
{	String routine = "ReadFromNetwork_Command.readStreamEstimateStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamEstimateStationMatchList() );

	// Now process the data from the network...

	// Now process the data from the network.  The nodes are the baseflow
	// nodes that are not flow nodes...

	List<HydrologyNode> estimateList = net.getBaseflowNodes();
	int size = 0;
	if ( estimateList != null ) {
		size = estimateList.size();
	}

	// Loop through the data and add new StateMod_StreamEstimate instances for each entry...

	HydrologyNode node = null;
	StateMod_StreamEstimate estimate;
	int countAdded = 0;
	for (int i = 0; i < size; i++) {
		node = estimateList.get(i);
		if ( node.getType() == HydrologyNode.NODE_TYPE_FLOW ) {
			// Do not include because the station is listed in the RIS file...
			String message = "Not including flow node \"" + node.getCommonID() +
			"\" as stream estimate station.  Use Other (OTH) node type " +
			"if this is not a gage with historical data.";
			Message.printStatus ( 2, routine, message );
			continue;
		}
		estimate = new StateMod_StreamEstimate();
		estimate.setID ( node.getCommonID() );
		estimate.setCgoto ( node.getCommonID() );
		// TODO SAM 2004-04-12 - need to assign available
		// data.  Maybe have options of what to assign (name and daily ID).
		// Replace or add in the __SMStreamEstimate_Vector...
		processor.findAndAddSMStreamEstimate ( estimate, true );
		++countAdded;
	}

	Message.printStatus ( 2, routine, "Read " + countAdded + " stream estimate stations." );

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModStreamEstimateStationMatchList(), "Stream Estimate Stations" );
	return warning_count;
}

/**
Read StateMod stream gage stations.
*/
private int readStreamGageStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net,
		boolean IncludeStreamEstimateStations_boolean )
throws Exception
{	//String routine = "ReadFromNetwork_Command.readStreamGageStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamGageStationMatchList() );

	// Now process the data from the network...

	// Get the list of baseflow nodes.  Stream gage stations must also be
	// indicated as baseflow nodes.  Those that are not indicated as
	// baseflow are treated as "other" nodes in modeling.

	List<HydrologyNode> streamGageStationList = net.getBaseflowNodes();
	int streamGageStationListSize = 0;
	if ( streamGageStationList != null ) {
		streamGageStationListSize = streamGageStationList.size();
	}

	// Loop through the data and add new StateMod_StreamEstimate
	// instances for each entry.  Only add if not already in the list.

	HydrologyNode node, downstream_flow_node;
	StateMod_StreamGage gage;
	for (int i = 0; i < streamGageStationListSize; i++) {
		node = streamGageStationList.get(i);
		if ( node.getType() == HydrologyNode.NODE_TYPE_FLOW ) {
			gage = new StateMod_StreamGage();
			gage.setID ( node.getCommonID() );
			gage.setCgoto ( node.getCommonID() );
			// As per Makenet code, set the daily identifier to the same as the gage identifier...
			gage.setCrunidy ( gage.getID() );
			// TODO SAM 2004-04-12 - need to assign available data.
			// Maybe have options of what to assign (name and daily ID).

			// Replace or add in the station list...
			processor.findAndAddSMStreamGage ( gage, true );
		}
		else if ( IncludeStreamEstimateStations_boolean ) {
			// Not streamflow so treat differently.  Only process if
			// stream estimate stations are included...
			gage = new StateMod_StreamGage();
			gage.setID ( node.getCommonID() );
			gage.setCgoto ( node.getCommonID() );
			// Set the node type.  This can be used later when
			// filling with HydroBase, to format the name (remove
			// this code if that convention is phased out).
			//
			// This code is the same when reading the stream estimate stations command.
			if ( node.getType() == HydrologyNode.NODE_TYPE_DIV ) {
				gage.setRelatedSMDataType (	StateMod_DataSet.COMP_DIVERSION_STATIONS );
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_DIV_AND_WELL ) {
				gage.setRelatedSMDataType (	StateMod_DataSet.COMP_DIVERSION_STATIONS );
				gage.setRelatedSMDataType2 ( StateMod_DataSet.COMP_WELL_STATIONS );
			}
			else if (node.getType() ==HydrologyNode.NODE_TYPE_ISF){
				gage.setRelatedSMDataType ( StateMod_DataSet.COMP_INSTREAM_STATIONS );
			}
			else if (node.getType() ==HydrologyNode.NODE_TYPE_RES){
				gage.setRelatedSMDataType (	StateMod_DataSet.COMP_RESERVOIR_STATIONS );
			}
			else if (node.getType()==HydrologyNode.NODE_TYPE_WELL){
				gage.setRelatedSMDataType (	StateMod_DataSet.COMP_WELL_STATIONS );
			}
			// As per Makenet code, set the daily identifier to the
			// ID of the downstream stream gage...
			downstream_flow_node = net.findDownstreamFlowNode ( node );
			if ( downstream_flow_node != null ) {
				gage.setCrunidy ( downstream_flow_node.getCommonID() );
			}
			// else {
			// TODO SAM 2004-07-10 - need a warning or would that
			// be a nuisance?  The makenet code did not print a warning.
			// }

			// Replace or add in the __SMStreamGage_Vector...
			processor.findAndAddSMStreamGage ( gage, true );
		}
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModStreamGageStationMatchList(), "Stream Gage Stations" );
	return warning_count;
}

/**
Read StateMod well stations.
*/
private int readWellStationsFromNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, HydroBaseDMI hbdmi, StateMod_NodeNetwork net )
throws Exception
{	//String routine = "ReadFromNetwork_Command.readWellStationsFromNetwork";
	//String message = null;
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModWellStationMatchList() );

	List<HydrologyNode> wellList = null;
	StateMod_Well well;

	// Now process the data from the network.  First get the well stations...

	wellList = net.getNodesForType(HydrologyNode.NODE_TYPE_WELL);
	int size = 0;
	if ( wellList != null ) {
		size = wellList.size();
	}

	// Loop through the data and add new StateMod_Well instances for each entry...

	HydrologyNode node = null;
	for (int i = 0; i < size; i++) {
		node = wellList.get(i);
		well = new StateMod_Well ( false );
		well.setID ( node.getCommonID() );
		well.setCgoto ( node.getCommonID() );
		// TODO SAM 2005-10-12 This could be filled with a command but default here
		// at the request of Ray Bennett.
		well.setIdvcow2 ( "NA" );
		// TODO SAM 2004-04-12 - need to assign available data.  Maybe have options of what to
		// assign (name).  Replace or add in the well list...
		processor.findAndAddSMWell ( well, true );
	}

	// Next get the D&W stations...

	wellList =net.getNodesForType(HydrologyNode.NODE_TYPE_DIV_AND_WELL);
	if ( wellList != null ) {
		size = wellList.size();
	}

	// Loop through the data and add new StateMod_Well instances for each entry...

	for (int i = 0; i < size; i++) {
		node = (HydrologyNode)wellList.get(i);
		well = new StateMod_Well ( false );
		well.setID ( node.getCommonID() );
		// TODO SAM 2010-01-14 Why is the cgoto (river network node) not set?
		// Set the diversion ID to the same as the ID...
		well.setIdvcow2( node.getCommonID() );
		// TODO SAM 2004-04-12 - need to assign available
		// data.  Maybe have options of what to assign (name).
		// Replace or add in the __SMWell_Vector...
		processor.findAndAddSMWell ( well, true );
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true, processor.getStateModWellStationMatchList(), "Well Stations" );
	return warning_count;
}

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
    
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" ); // Can be null
    boolean IncludeStreamEstimateStations_boolean = false; // Default
    String IncludeStreamEstimateStations = parameters.getValue ( "IncludeStreamEstimateStations" );
    if ( (IncludeStreamEstimateStations != null) && IncludeStreamEstimateStations.equalsIgnoreCase(_True) ) {
    	IncludeStreamEstimateStations_boolean = true;
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
				message, "Verify that HydroBase is accessible." ) );
	}

    String InputFile_full = InputFile;
	if ( InputFile != null ) {
    	InputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
    	
       	if ( !IOUtil.fileExists(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
    	}
    	if ( !IOUtil.fileReadable(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
    	}
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
     	// Clear the filename
    	setInputFile ( null );
    	
    	// Read the network file
    	
    	StateMod_NodeNetwork net = null;
    	if ( InputFile_full != null ) {
    		// Read the network.  For now only support old Makenet format...
    		Message.printStatus(2, routine, "Reading network file \"" + InputFile_full + "\"" );
    		net = StateMod_NodeNetwork.readStateModNetworkFile(	InputFile_full,
    			new StateDMI_NodeDataProvider(hbdmi), true);
    	}
    	else {
    		// Get the network from memory and use it.
    		// TODO SAM 2004-04-12 For now try to get from the JFrame.  Later, may try to get from a data set.
    		net = (StateMod_NodeNetwork)processor.getPropContents ( "StateMod_Network" );
    		if ( net == null ) {
    			message = "No network file is given and no network appears to have been read previously.";
    			Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
    			throw new Exception ( message );
    		}
    	}
    	// Set back in the processor for use by later commands
    	processor.setPropContents ( "StateMod_Network", net );
    	
    	// Extract the data from the network...
    	
    	if ( this instanceof ReadStreamGageStationsFromNetwork_Command ) {
    		warning_count = readStreamGageStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net, IncludeStreamEstimateStations_boolean );
    	}
    	else if ( this instanceof ReadDiversionStationsFromNetwork_Command ) {
    		warning_count = readDiversionStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net );
    	}
    	else if ( this instanceof ReadReservoirStationsFromNetwork_Command ) {
    		warning_count = readReservoirStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net );
    	}
    	else if ( this instanceof ReadInstreamFlowStationsFromNetwork_Command ) {
    		warning_count = readInstreamFlowStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net );
    	}
    	else if ( this instanceof ReadStreamEstimateStationsFromNetwork_Command ) {
    		warning_count = readStreamEstimateStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net );
    	}
    	else if ( this instanceof ReadWellStationsFromNetwork_Command ) {
    		warning_count = readWellStationsFromNetwork ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, hbdmi, net );
    	}
    	// Set the filename
    	if ( InputFile_full != null ) {
    		setInputFile ( new File(InputFile_full) );
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading file \"" + InputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the input file that is read by this command.  This is only used internally.
*/
protected void setInputFile ( File file )
{
	__InputFile_File = file;
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

	String InputFile = parameters.getValue ( "InputFile" );
	String IncludeStreamEstimateStations = parameters.getValue ( "IncludeStreamEstimateStations" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}

	if ( this instanceof ReadStreamGageStationsFromNetwork_Command ) {
		if ( (IncludeStreamEstimateStations != null) && (IncludeStreamEstimateStations.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IncludeStreamEstimateStations=\"" + IncludeStreamEstimateStations + "\"" );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
