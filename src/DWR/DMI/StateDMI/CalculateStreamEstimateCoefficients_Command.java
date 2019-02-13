// CalculateStreamEstimateCoefficients_Command - This class initializes, checks, and runs the CalculateStreamEstimateCoefficients() command.

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

import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_PrfGageData;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.UpstreamFlowNodeA;

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

/**
<p>
This class initializes, checks, and runs the CalculateStreamEstimateCoefficients() command.
</p>
*/
public class CalculateStreamEstimateCoefficients_Command extends AbstractCommand implements Command
{
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public CalculateStreamEstimateCoefficients_Command ()
{	super();
	setCommandName ( "CalculateStreamEstimateCoefficients" );
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
	//String ID = parameters.getValue ( "ID" );
	String warning = "";
	//String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    /*
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID to process." ) );
	}
	*/

    // Check for invalid parameters...
	List valid_Vector = new Vector();
	//valid_Vector.add ( "ID" );
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
	return (new CalculateStreamEstimateCoefficients_JDialog ( parent, this )).ok();
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
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    //PropList parameters = getCommandParameters();
    //String ID = parameters.getValue ( "ID" );
    //if ( ID == null ) {
    //	ID = "*";
    //}
	
	//String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    
    // Get the data needed for the command
	
    List prfGageDataList = null;
    int prfGageDataListSize = 0;
    try {
    	prfGageDataList = (List)processor.getPropContents ( "StateMod_PrfGageData_List" );
    	prfGageDataListSize = prfGageDataList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting proration factor gage data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    List streamEstimateStationList = null;
    int streamEstimateStationListSize = 0;
    try {
    	streamEstimateStationList = (List)processor.getPropContents ( "StateMod_StreamEstimateStation_List" );
    	streamEstimateStationListSize = streamEstimateStationList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting stream estimate stations to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( streamEstimateStationListSize == 0 ) {
        message = "No stream estimate stations have been read/set - cannot create coefficients.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Read or define (set) the stream estimate stations prior to this command." ) );
    }
    
    List streamEstimateCoefficientsList = null;
    try {
    	streamEstimateCoefficientsList =
    		(List)processor.getPropContents ( "StateMod_StreamEstimateCoefficients_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting stream estimate coefficients to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    StateMod_NodeNetwork net = null;
    try {
		net = (StateMod_NodeNetwork)processor.getPropContents ( "StateMod_Network" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting node network (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( net == null ) {
        message = "No StateMod network has been read - cannot use for filling.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Read the StateMod network with a ReadNetworkFromStateMod() command " +
                	"prior to using this command." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Make sure that the gage substitute data point to valid network nodes...
    	
    	// Echo for troubleshooting
    	StateMod_PrfGageData prf = null;
    	String id;
		Message.printStatus ( 2, routine, "PFGage substitution data:");
    	for ( int i = 0; i < prfGageDataListSize; i++ ) {
    		prf = (StateMod_PrfGageData)prfGageDataList.get(i);
    		Message.printStatus ( 2, routine, "Station ID=\"" + prf.getID() + "\" Gage ID=\"" +
    			prf.getGageID() + "\"");
    	}

    	for ( int i = 0; i < prfGageDataListSize; i++ ) {
    		prf = (StateMod_PrfGageData)prfGageDataList.get(i);
    		id = prf.getNodeID();
    		if ( net.findNode(id) == null ){
        		message = "PFGage substitute node ID \"" + id + "\" is not in the network.";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                    status.addToLog(command_phase,
                        new CommandLogRecord( CommandStatusType.WARNING, message,
                            "Verify the proration factor gage identifier in the network."));
    		}
    		id = prf.getGageID();
    		if ( net.findNode(id) == null ){
    			message = "PFGage substitute gage ID \"" + id + "\" is not in the network.";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                    status.addToLog(command_phase,
                        new CommandLogRecord( CommandStatusType.WARNING, message,
                            "Verify the proration factor substitute gage identifier in the network."));
    		}
    	}

    	// Loop through the stream estimate stations and process a
    	// StateMod_StreamEstimate_Coefficients object for each station...

    	StateMod_StreamEstimate ses = null;
    	StateMod_StreamEstimate_Coefficients rib = null;
    	StateMod_StreamEstimate_Coefficients rib2;
    	double water; // The value of area*precip, as a double
    	HydrologyNode nodePt, nodeDownstreamFlow;
    	int dl = 12;
    	int iprfGageData;
    	int nupstreamFlowNodes;
    	List upstreamFlowNodes = null;
    	
    	// Create an adaptor to handle the StateMod prfGageList integration with the more generic CDSS
    	// HydrologyNodeNetwork.  This will be reused for lookups.
    	
    	UpstreamFlowNodeA upstreamFlowNodeA = new UpstreamFlowNodeA ( prfGageDataList );

    	for ( int i = 0; i < streamEstimateStationListSize; i++) {
    		ses = (StateMod_StreamEstimate)streamEstimateStationList.get(i);
    		id = ses.getID();
    		// Define stream estimate coefficients...
    		Message.printStatus ( 2, routine, "Creating empty stream estimate coefficients for " + id );
    		rib = new StateMod_StreamEstimate_Coefficients ();
    		rib.setID ( id );

    		// The following is actually printed to the file, not ID, but
    		// set both since general code uses ID...

    		rib.setFlowX ( id );

    		// Find the stream estimate station node in the network...

    		nodePt = net.findNode ( id );
    		if ( nodePt == null ) {
    			message = "The stream estimate station \"" + id +
    			"\" was not found in the network.  Skipping.";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                    status.addToLog(command_phase,
                        new CommandLogRecord( CommandStatusType.FAILURE, message,
                            "Verify the stream estimate station identifier in the network."));
    			continue;
    		}

    		// Figure out the values to go into the data structure.  Start
    		// by finding the closest downstream flow node while staying
    		// on the current reach or parent streams (do not follow
    		// smaller tributaries even if computationally downstream!)...

    		nodeDownstreamFlow = net.findDownstreamFlowNode(nodePt);
    		if ( nodeDownstreamFlow == null ) {
    			Message.printStatus ( 2, routine, "Node \"" + nodePt.getCommonID()
    				+ "\" doesn't have a downstream FLOW node");
    			iprfGageData = StateMod_PrfGageData.isSetprfTarget(id,prfGageDataList);
    			if ( iprfGageData < 0 ) {
    				// Not a setprfgage() node so we definitely have a problem...
    				message = "Node \"" + id + "\" is not a PFGage node so cannot continue.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                        status.addToLog(command_phase,
                            new CommandLogRecord( CommandStatusType.FAILURE, message,
                                "Verify the stream estimate station identifier in the network."));
    				throw new Exception ( message );
    			}
    			// Else can continue...
    		}

    		// First, figure out the upstream flow stations from the current baseflow node.
    		// This is the information for line 1 of the .rib file entry.

    		nupstreamFlowNodes = 0;

    		// Need because of a check in the code...
    		upstreamFlowNodes = new Vector(1,1);
    		upstreamFlowNodes = net.findUpstreamFlowNodes ( upstreamFlowNodes, nodePt, upstreamFlowNodeA, false);

    		if (upstreamFlowNodes != null) {
    			nupstreamFlowNodes = upstreamFlowNodes.size();
    		}

    		Message.printStatus(2, routine, "Found " + nupstreamFlowNodes +
    			" stream gage nodes upstream of stream estimate node \"" + nodePt.getCommonID() + "\"");

    		// Now add these to the list of upstream nodes...

    		HydrologyNode upstreamFlowNodes_i = null;
    		for ( int iup = 0; iup < nupstreamFlowNodes; iup++) {
    			upstreamFlowNodes_i = (HydrologyNode)upstreamFlowNodes.get(iup);
    			int N = rib.getN();
    			rib.setUpper(N, upstreamFlowNodes_i.getCommonID());
    			rib.setCoefn(N, 1.0);
    			if (Message.isDebugOn) {
    				Message.printDebug(dl, routine,
    					"For " + nodePt.getCommonID() + ", have upstream flow node [" + N
    					+ "] \"" + rib.getUpper(N) + "\"  Coefn: " + rib.getCoefn(N));
    			}
    		}

    		// Second, figure out the information for the second line of the .rib file.
    		// This deals with the flow stations upstream of the downstream node...

    		iprfGageData = StateMod_PrfGageData.isSetprfTarget ( id, prfGageDataList );
    		StateMod_PrfGageData prfGageData_found = null;
    		HydrologyNode prfGageData_Node = null;
    		HydrologyNode prfGageData_GageNode = null;
    		if ( iprfGageData >= 0 ) {
    			// Found a gage substitute...
    			//
    			// The proration factor is calculated only from the
    			// specified nodes and there is only one gain station...
    			//
    			// 2nd line in .rib has only the specified node...
    			prfGageData_found = (StateMod_PrfGageData)prfGageDataList.get(iprfGageData);
    			int M = rib.getM();
    			rib.setFlowm(M, prfGageData_found.getGageID());
    			if (Message.isDebugOn) {
    				Message.printDebug(dl, routine, "PFGage for " + prfGageData_found.getNodeID()
    					+ ":  set Flowm(" + M + ") to "	+ prfGageData_found.getGageID());
    			}
    			rib.setCoefm(M, 1.0);
    			// SAM.. rib.setM(M + 1);
    			// Calculate the proration factor to be the water at the baseflow station divided by
    			// the specified station.  First have to find the gage in the network.
    			prfGageData_GageNode = net.findNode ( prfGageData_found.getGageID() );
    			if ( prfGageData_GageNode == null ) {
    				message = "Can't find PFGage gage \"" + prfGageData_found.getGageID()
    					+ "\" in network - may result in errors.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                        status.addToLog(command_phase,
                            new CommandLogRecord( CommandStatusType.WARNING, message,
                                "Verify the PFGage identifier in the network."));
    				continue;
    			}
    			if ( prfGageData_GageNode.getWater() == 0.0 ) {
    				rib.setProratnf(0.0);
    				message ="Baseflow node " + id + ": proration coef. is 0.0 (caught divide by zero)!";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                        status.addToLog(command_phase,
                            new CommandLogRecord( CommandStatusType.WARNING, message,
                                "Verify that area*precipitation is non-zero."));
    			}
    			else {
    				prfGageData_Node = net.findNode ( prfGageData_found.getNodeID() );
    				if ( prfGageData_Node == null ) {
    					message = "Can't find PFGage node \"" + prfGageData_found.getNodeID()
    						+ "\" in network - may result in errors.";
                        Message.printWarning ( warning_level,
                            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                            status.addToLog(command_phase,
                                new CommandLogRecord( CommandStatusType.WARNING, message,
                                    "Verify the stream PFGage identifier in the network."));
    					continue;
    				}
    				rib.setProratnf( prfGageData_Node.getWater()/prfGageData_GageNode.getWater() );
    				Message.printStatus(2, routine, "Baseflow node " + id + ": proration coef. is "
    					+ rib.getProratnf() + " (" + StringUtil.formatString(prfGageData_Node.getWater(),"%.2f")
    					+ "/" + StringUtil.formatString(prfGageData_GageNode.getWater(),"%.2f") + ").");
    			}
    		}
    		else {
    			// The first gain flow station is always the downstream flow station...
    			Message.printStatus ( 2, routine, "Node \"" + id +
    				"\" has +1 gain FLOW gage (next downstream) \""
    				+ nodeDownstreamFlow.getCommonID() + "\"");

    			int M = rib.getM();
    			rib.setFlowm(M, nodeDownstreamFlow.getCommonID());
    			rib.setCoefm(M, 1.0);
    			// SAM... baseflow_tmp.setM(M + 1);
    			// Next, look for FLOW gages upstream of the downstream flow node to get the gain
    			// inflows.  Do so by traversing the current reach until a flow gage is
    			// found and then terminate the search.  While searching the current reach,
    			// also branch off on tribs until the first contributing FLOW station is found...
    			nupstreamFlowNodes = 0;

    			// Need because of a check in the code...
    			upstreamFlowNodes = new Vector(1,1);
    			upstreamFlowNodes = net.findUpstreamFlowNodes(
    				upstreamFlowNodes, nodeDownstreamFlow, upstreamFlowNodeA, false);

    			if (upstreamFlowNodes != null) {
    				nupstreamFlowNodes = upstreamFlowNodes.size();
    			}

    			Message.printStatus ( 2, routine, "Found " + nupstreamFlowNodes +
    				" FLOW nodes upstream of downstream FLOW node \""
    				+ nodeDownstreamFlow.getCommonID() + "\"");

    			// We did not specifically set the proration factor
    			// based on one area so calculate it from upstream areas...
    			//
    			// First, find the area for the downstream FLOW node...
    			water = nodeDownstreamFlow.getWater();
    			Message.printStatus ( 2, routine, "For \"" + id +
    				"\" proration factor, start with downstream flow node \""
    				+ nodeDownstreamFlow.getCommonID() + "\" water "
    				+ StringUtil.formatString(water,"%.2f"));

    			// Now add these to the list of gain nodes (2nd line in
    			// .rib)and adjust the water for the proration factor...
    			for ( int iup = 0; iup < nupstreamFlowNodes; iup++ ) {
    				upstreamFlowNodes_i = (HydrologyNode)upstreamFlowNodes.get(iup);

    				M = rib.getM();
    				rib.setFlowm(M,	upstreamFlowNodes_i.getCommonID());
    				rib.setCoefm(M, -1.0);
    				water -= upstreamFlowNodes_i.getWater();
    				Message.printStatus(2, routine, "For \"" + id
    					+ "\" proration factor, subtract upstream \""
    					+ upstreamFlowNodes_i.getCommonID() + "\" water "
    					+ StringUtil.formatString(upstreamFlowNodes_i.getWater(),"%.2f") +
    					", now have " + StringUtil.formatString(water,"%.2f"));
    			}

    			// Calculate the proration factor to be the water at the baseflow station divided
    			// by the gaining water between the two bounding gages...
    			if (water == 0.0) {
    				rib.setProratnf(0.0);
    				message = "Baseflow node " + id + ": proration coef. is 0.0 (caught divide by zero)!";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                        status.addToLog(command_phase,
                            new CommandLogRecord( CommandStatusType.WARNING, message,
                                "Verify that cumulative area*precipitation is non-zero."));
    			}
    			else {
    				rib.setProratnf( nodePt.getWater()/water);
    				Message.printStatus(2, routine, "Baseflow node \"" + id + "\": proration coef. is "
    					+ rib.getProratnf() + "(" + StringUtil.formatString(nodePt.getWater(),"%.2f")
    					+ "/" + StringUtil.formatString(water,"%.2f") + ").");
    			}
    		}

    		if ( rib.getProratnf() > 1.0) {
				message = "Proration factor for \"" + id + "\" is "
				+ StringUtil.formatString(rib.getProratnf(),"%.6f") + " (> 1.0).";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.WARNING, message,
                        "Verify network and area*precipition data for the basin."));
    		}
    		else if ( rib.getProratnf() < 0.0) {
    			message = "Proration factor for \"" + id + "\" is "
				+ StringUtil.formatString(rib.getProratnf(),"%.6f") + " (< 0.0).";
    			Message.printWarning(3, routine, message );
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.WARNING, message,
                        "Verify network and area*precipition data for the basin."));
    		}

    		processor.findAndAddSMStreamEstimateCoefficients ( rib, true );
    	}

    	// Now adjust the baseflow line 1's to account for the PFGage adjustments...

    	Message.printStatus(2, routine,
    		"Adjust the weights on the computed gaged flows because of PFGage data...");

    	int streamEstimateCoefficientsListSize = streamEstimateCoefficientsList.size();
    	for ( int irib = 0; irib < streamEstimateCoefficientsListSize; irib++) {
    		rib = (StateMod_StreamEstimate_Coefficients)streamEstimateCoefficientsList.get(irib);

    		Message.printStatus(2, routine, "Processing stream estimate station " + rib.getFlowX());

    		// Loop through the first line information to see if any of
    		// the gages are actually other structures...

    		int N = rib.getN();
    		for ( int i = 0; i < N; i++) {
    			iprfGageData = StateMod_PrfGageData.isSetprfTarget(rib.getUpper(i), prfGageDataList);
    			if (iprfGageData >= 0) {
    				// Then need to search for the node that is the target and replace the weight and
    				// station with the gage that it is based upon...
    				rib2 = StateMod_StreamEstimate_Coefficients.locateBaseNode(
    					streamEstimateCoefficientsList,rib.getUpper(i));

    				if ( rib2 == null ) {
    					message = "Working on \"" + rib.getFlowX() + "\".  Can't find PFGage node \""
    						+ rib.getUpper(i) + "\" in list.";
                        Message.printWarning ( warning_level,
                                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                                status.addToLog(command_phase,
                                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                                        "Verify the PFGage in the network."));
    					continue;
    				}
    				// Else we found a baseflow node.  Replace the current data with the found data...
    				Message.printStatus(2, routine, "Changing \"" + rib.getFlowX() + "\" pair " + (i + 1)
    					+ " gage data from " + StringUtil.formatString(rib.getCoefn(i),"%6.3f")
    					+ " " + rib.getUpper(i)	+ " to " +
    					StringUtil.formatString(rib2.getProratnf(),"%6.3f") + " " + rib2.getFlowm(0));
    				rib.setUpper(i, rib2.getFlowm(0));
    				rib.setCoefn(i, rib2.getProratnf());
    			}
    		}

    		// Loop through the second line information to see if any of
    		// the gages are actually other structures...

    		int M = rib.getM();
    		for ( int i = 0; i < M; i++ ) {
    			if (Message.isDebugOn) {
    				Message.printDebug(dl, routine, "Trying to get flowm " + i);
    			}

    			iprfGageData = StateMod_PrfGageData.isSetprfTarget( rib.getFlowm(i), prfGageDataList );

    			if (iprfGageData >= 0) {
    				// Then need to search for the node that is the target and replace the weight
    				// and station with the gage that it is based upon...
    				rib2 = 	StateMod_StreamEstimate_Coefficients.locateBaseNode(
    					streamEstimateCoefficientsList, rib.getFlowm(i));

    				if ( rib2 == null ) {
    					message = "Working on \"" + rib.getFlowX() + "\".  Can't find PFGage node \""
    						+ rib.getFlowm(i) + "\" in list.";
                        Message.printWarning ( warning_level,
                            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                            status.addToLog(command_phase,
                                new CommandLogRecord( CommandStatusType.FAILURE, message,
                                    "Verify the PFGage nide identifier in the network."));
    					continue;
    				}

    				// Else we found a baseflow node.  Replace the current data with the found data...
    				Message.printStatus(2, routine, "Changing \"" + rib.getFlowX() + "\" pair " +
    					(i + 1) + " gain data from " + StringUtil.formatString(rib.getCoefm(i),"%6.3f")
    					+ " " + rib.getFlowm(i) + " to " +
    					StringUtil.formatString(-1.0 * rib2.getProratnf(),"%6.3f") + " "
    					+ rib2.getFlowm(0));
    				rib.setFlowm(i, rib2.getFlowm(0));
    				rib.setCoefm(i, -1.0 * rib2.getProratnf());
    			}
    		}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error calculating stream estimate coefficients (" + e + ").";
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

	//String ID = parameters.getValue ( "ID" );
	
	StringBuffer b = new StringBuffer ();
	/*
	 if ( (ID != null) && (ID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"");
	}*/
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
