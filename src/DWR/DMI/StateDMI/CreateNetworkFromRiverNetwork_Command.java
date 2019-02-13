// CreateNetworkFromRiverNetwork_Command - This class initializes, checks, and runs the CreateNetworkFromRiverNetwork() command.

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

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_Plan;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;

/**
This class initializes, checks, and runs the CreateNetworkFromRiverNetwork() command.
*/
public class CreateNetworkFromRiverNetwork_Command 
extends AbstractCommand implements Command
{

/**
Constructor.
*/
public CreateNetworkFromRiverNetwork_Command ()
{	super();
	setCommandName ( "CreateNetworkFromRiverNetwork" );
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
{	String routine = "CreateNetworkFromRiverNetwork.checkCommandParameters";
	//String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	// Check for invalid parameters...
	List valid_Vector = new Vector();

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
{	// The command will be modified if changed...
	return (new CreateNetworkFromRiverNetwork_JDialog ( parent, this )).ok();
}

// Use the base class parseCommand()

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters (none)...

	// Get the list of river network nodes...
	
	List<StateMod_RiverNetworkNode> riverNetworkNodeList = null;
	int riverNetworkNodeListSize = 0;
	try {
		riverNetworkNodeList = (List<StateMod_RiverNetworkNode>)processor.getPropContents ( "StateMod_RiverNetworkNode_List");
		riverNetworkNodeListSize = riverNetworkNodeList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting river network node data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( riverNetworkNodeListSize == 0 ) {
		message = "No river network nodes are available to process.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Make sure that the StateMod river network was read/set with previous command(s)." ) );
	}
	
	// Get the stream gage stations...
	
    List<StateMod_StreamGage> streamGageStationList = null;
    try {
    	streamGageStationList = (List<StateMod_StreamGage>)processor.getPropContents ( "StateMod_StreamGageStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting stream gage station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
	// Get the stream estimate stations...
	
    List<StateMod_StreamEstimate> streamEstimateStationList = null;
    try {
    	streamEstimateStationList = (List<StateMod_StreamEstimate>)processor.getPropContents ( "StateMod_StreamEstimateStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting stream estimate station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the diversion stations.
    
    List<StateMod_Diversion> diversionStationList = null;
    try {
    	diversionStationList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting diversion station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the reservoir stations.
    
    List<StateMod_Reservoir> reservoirStationList = null;
    try {
    	reservoirStationList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting reservoir station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
   // Get the instream flow stations.
    
    List<StateMod_InstreamFlow> instreamFlowStationList = null;
    try {
    	instreamFlowStationList = (List<StateMod_InstreamFlow>)processor.getPropContents ( "StateMod_InstreamFlowStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting instream flow station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the well stations.
    
    List<StateMod_Well> wellStationList = null;
    try {
    	wellStationList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting well station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the plan stations.
    
    List<StateMod_Plan> planStationList = null;
    try {
    	planStationList = (List<StateMod_Plan>)processor.getPropContents ( "StateMod_PlanStation_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting well station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
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
		// Create the network...

		StateMod_NodeNetwork net = StateMod_NodeNetwork.createFromStateModVector ( riverNetworkNodeList );

		// TODO SAM 2004-06-25 Need to figure out how this is kept consistent with the GUI network.

		// For each node, check for its occurrence in the other lists in order
		// to set the node type.  Start at the top of the network and go to the bottom...

		HydrologyNode node = net.getMostUpstreamNode();
		HydrologyNode hold = null;
		int pos = 0;
		String id;
		int found_count = 0;
		int notFoundCount = 0;
		while ( true ) {
			if (node == null) {
				break;
			}
			if (node.getType() == HydrologyNode.NODE_TYPE_END) {
				++found_count;
				break;
			}
			id = node.getCommonID();

			// Try to find in stream gages...
			pos = StateMod_Util.indexOfRiverNodeID(	streamGageStationList,id);
			if ( pos >= 0 ) {
				// Assume that it is a stream gage...
				Message.printStatus ( 2, routine, id + ": setting node type to stream gage" );
				node.setType ( HydrologyNode.NODE_TYPE_FLOW );
				// Always a base flow...
				node.setIsNaturalFlow ( true );
			}
			if ( pos < 0 ) {
				// Try to find in diversions...
				pos = StateMod_Util.indexOfRiverNodeID ( diversionStationList, id );
				if ( pos >= 0 ) {
					// Assume that it is a diversion...
					Message.printStatus ( 2, routine, id + ": setting node type to diversion" );
					node.setType ( HydrologyNode.NODE_TYPE_DIV );
				}
			}
			if ( pos < 0 ) {
				// Try to find in reservoirs..
				pos = StateMod_Util.indexOfRiverNodeID ( reservoirStationList, id );
				if ( pos >= 0 ) {
					// Assume that it is a reservoir...
					Message.printStatus ( 2, routine, id + ": setting node type to reservoir" );
					node.setType ( HydrologyNode.NODE_TYPE_RES );
				}
			}
			if ( pos < 0 ) {
				// Try to find in instream flow stations..
				pos = StateMod_Util.indexOfRiverNodeID ( instreamFlowStationList, id );
				if ( pos >= 0 ) {
					// Assume that it is an instream flow station...
					Message.printStatus ( 2, routine, id + ": setting node type to instream flow" );
					node.setType ( HydrologyNode.NODE_TYPE_ISF );
				}
			}
			if ( pos < 0 ) {
				// Try to find in well stations..
				pos = StateMod_Util.indexOfRiverNodeID ( wellStationList, id );
				if ( pos >= 0 ) {
					// Assume that it is a well station...
					Message.printStatus ( 2, routine, id + ": setting node type to well" );
					node.setType ( HydrologyNode.NODE_TYPE_WELL );
				}
			}
			if ( pos < 0 ) {
				// Try to find in plan stations...
				pos = StateMod_Util.indexOfRiverNodeID ( planStationList, id );
				if ( pos >= 0 ) {
					// Assume that it is a plan station...
					Message.printStatus ( 2, routine, id + ": setting node type to plan" );
					node.setType ( HydrologyNode.NODE_TYPE_PLAN );
				}
			}
			// Check if not found...
			if ( pos < 0 ) {
				Message.printStatus ( 2, routine,
				id + ": unable to determine node type from other data.  Setting the type to OTHER." );
				node.setType ( HydrologyNode.NODE_TYPE_OTHER );
				++notFoundCount;
			}
			else {
				++found_count;
			}
			// Try to determine whether a base flow location.  Stream estimate stations use
			// the same file format as stream gages and therefore the river node ID can be
			// checked.  Only need to check nodes that are not stream gages...
			if ( node.getType() != HydrologyNode.NODE_TYPE_FLOW ) {
				pos = StateMod_Util.indexOfRiverNodeID ( streamEstimateStationList, id );
				if ( pos >= 0 ) {
					node.setIsNaturalFlow ( true );
				}
			}

			// Go to the next node...

			hold = node;
			node = StateMod_NodeNetwork.getDownstreamNode(node,	StateMod_NodeNetwork.POSITION_COMPUTATIONAL);
			// Check for self-reference error?? TODO SAM 2004-07-01
			if (hold == node) {
				break;
			}
		}
		// TODO SAM 2009-01-21 Evaluate whether a warning can be generated - probably just need to
		// assume OTHER node type.
		message = "Unable to determine node type for " +
			notFoundCount + " nodes out of " + (notFoundCount + found_count)+" - used OTHER.";
        Message.printStatus ( 2, routine, message );
        
    	// Set the network in the processor.
    	
    	processor.setPropContents ( "StateMod_Network", net );
        
	}
    catch ( Exception e ) {
        message = "Unexpected error creating network (" + e + ").";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
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
	
	StringBuffer b = new StringBuffer();
	return getCommandName() + "(" + b.toString() + ")";
}

}
