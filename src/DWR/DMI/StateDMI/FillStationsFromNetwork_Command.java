// FillStationsFromNetwork_Command - This class initializes, checks, and runs the Fill*StationsFromNetwork() commands.

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

import DWR.StateMod.StateMod_Data;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

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
This class initializes, checks, and runs the Fill*StationsFromNetwork() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the
FillDiversionStationsFromNetwork() command extends this class in order to uniquely represent the
command, but much of the functionality is in this base class.
</p>
*/
public abstract class FillStationsFromNetwork_Command extends AbstractCommand implements Command
{
	
/**
Values for the NameFormat parameter.
*/
protected final String _StationName = "StationName";
protected final String _StationName_NodeType = "StationName_NodeType";

/**
Values for the CommentFormat parameter.
*/
protected final String _StationID = "StationID";
	
/**
Values for the IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillStationsFromNetwork_Command ()
{	super();
	setCommandName ( "?Fill?StationsFromNetwork" );
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
	String ID = parameters.getValue ( "ID" );
	String NameFormat = parameters.getValue ( "NameFormat" );
	String CommentFormat = parameters.getValue ( "CommentFormat" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID to process." ) );
	}
	
	if ( (this instanceof FillStreamGageStationsFromNetwork_Command) ||
		(this instanceof FillStreamEstimateStationsFromNetwork_Command)||
		(this instanceof FillRiverNetworkFromNetwork_Command)){
		if ( (NameFormat != null) && (NameFormat.length() > 0) &&
			!NameFormat.equalsIgnoreCase(_StationName) &&
			!NameFormat.equalsIgnoreCase(_StationName_NodeType) ) {
			message = "The NameFormat value (" + NameFormat + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify NameFormat as " + _StationName + " (default) or " +
					_StationName_NodeType + ".") );
		}
	}
	
	if ( this instanceof FillRiverNetworkFromNetwork_Command ) {
		if ( (CommentFormat != null) && (CommentFormat.length() > 0) &&
			!CommentFormat.equalsIgnoreCase(_StationID) ) {
			message = "The CommentFormat value (" + CommentFormat + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify CommentFormat as " + _StationID + " (default).") );
		}
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "ID" );
	if ( (this instanceof FillStreamGageStationsFromNetwork_Command) ||
		(this instanceof FillStreamEstimateStationsFromNetwork_Command)||
		(this instanceof FillRiverNetworkFromNetwork_Command)){
		valid_Vector.add ( "NameFormat" );
	}
	if ( this instanceof FillRiverNetworkFromNetwork_Command ) {
		valid_Vector.add ( "CommentFormat" );
	}
	valid_Vector.add ( "IfNotFound" );
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
	return (new FillStationsFromNetwork_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

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

    PropList parameters = getCommandParameters();
    String ID = parameters.getValue ( "ID" );
    if ( ID == null ) {
    	ID = "*";
    }
    String NameFormat = parameters.getValue ( "NameFormat" );
    if ( NameFormat == null ) {
    	NameFormat = _StationName;
    }
    String CommentFormat = parameters.getValue ( "CommentFormat" );
    if ( CommentFormat == null ) {
    	CommentFormat = _StationID;
    }
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    
    // Get the data needed for the command
	
    List<? extends StateMod_Data> list = null;
    int listSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // Used for loops to improve performance
    try {
    	if ( this instanceof FillStreamGageStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamGage> dataList = (List<StateMod_StreamGage>)processor.getPropContents ( "StateMod_StreamGageStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_STREAMGAGE_STATIONS;
    	}
      	else if ( this instanceof FillDiversionStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_DIVERSION_STATIONS;
    	}
      	else if ( this instanceof FillInstreamFlowStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlow> dataList = (List<StateMod_InstreamFlow>)processor.getPropContents ( "StateMod_InstreamFlowStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_INSTREAM_STATIONS;
    	}
    	else if ( this instanceof FillReservoirStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_RESERVOIR_STATIONS;
    	}
    	else if ( this instanceof FillStreamEstimateStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate> dataList = (List<StateMod_StreamEstimate>)processor.getPropContents ( "StateMod_StreamEstimateStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS;
    	}
    	else if ( this instanceof FillWellStationsFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_WELL_STATIONS;
    	}
    	else if ( this instanceof FillRiverNetworkFromNetwork_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_RiverNetworkNode> dataList = (List<StateMod_RiverNetworkNode>)processor.getPropContents ( "StateMod_RiverNetworkNode_List" );
    		list = dataList;
    		compType = StateMod_DataSet.COMP_RIVER_NETWORK;
    	}
		listSize = list.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to process (" + e + ").";
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
    	int matchCount = 0;
    	
    	HydrologyNode node = null;
    	StateMod_StreamGage ris = null;
    	StateMod_Diversion dds = null;
    	StateMod_Reservoir res = null;
    	StateMod_InstreamFlow ifs = null;
    	StateMod_Well wes = null;
    	StateMod_StreamEstimate ses = null;
    	StateMod_RiverNetworkNode rin = null;
    	StateMod_Data smdata = null; // To allow polymorphism
    	String id = null, name;
    	String net_name = null;	// Name returned from HydroBase
    	String node_type = null;// Node type used in formatting the name.
    	for ( int i = 0; i < listSize; i++ ) {
    		if ( compType == StateMod_DataSet.COMP_STREAMGAGE_STATIONS ) {
    			ris = (StateMod_StreamGage)list.get(i);
    			smdata = ris;
    		}
    		else if (compType == StateMod_DataSet.COMP_DIVERSION_STATIONS){
    			dds = (StateMod_Diversion)list.get(i);
    			smdata = dds;
    		}
    		else if (compType == StateMod_DataSet.COMP_RESERVOIR_STATIONS){
    			res = (StateMod_Reservoir)list.get(i);
    			smdata = res;
    		}
    		else if (compType == StateMod_DataSet.COMP_INSTREAM_STATIONS){
    			ifs = (StateMod_InstreamFlow)list.get(i);
    			smdata = ifs;
    		}
    		else if (compType == StateMod_DataSet.COMP_WELL_STATIONS){
    			wes = (StateMod_Well)list.get(i);
    			smdata = wes;
    		}
    		else if ( compType == StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS ) {
    			// Try reading from stream gages and structures...
    			ses = (StateMod_StreamEstimate)list.get(i);
    			smdata = ses;
    		}
    		else if ( compType == StateMod_DataSet.COMP_RIVER_NETWORK ) {
    			rin = (StateMod_RiverNetworkNode)list.get(i);
    			smdata = rin;
    		}
    		id = smdata.getID();
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}

    		// Find the node in the network...

    		node = net.findNode ( id );
    		if ( node == null ) {
    			// This should not normally happen...
    			Message.printStatus ( 2, routine, "No station data in network for \"" + id + "\"" );
    			continue;
    		}

    		// Set the data to use below...

    		net_name = node.getDescription();
    		// TODO SAM 2004-07-09 There is a chance that the node type
    		// in the network will not agree with the StateMod files - deal with this issue later.
    		node_type = HydrologyNode.getTypeString ( node.getType(), HydrologyNode.ABBREVIATION );

    		// Reset the data with the specified filled values.

    		if ( ((compType==StateMod_DataSet.COMP_STREAMGAGE_STATIONS)
    			&& StateMod_Util.isMissing(ris.getName())) ||
    			((compType==StateMod_DataSet.COMP_DIVERSION_STATIONS)
    			&& StateMod_Util.isMissing(dds.getName())) ||
    			((compType==StateMod_DataSet.COMP_RESERVOIR_STATIONS)
    			&& StateMod_Util.isMissing(res.getName())) ||
    			((compType==StateMod_DataSet.COMP_INSTREAM_STATIONS)
    			&& StateMod_Util.isMissing(ifs.getName())) ||
    			((compType==StateMod_DataSet.COMP_WELL_STATIONS)
    			&& StateMod_Util.isMissing(wes.getName())) ||
    			((compType==StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS)
    			&& StateMod_Util.isMissing(ses.getName())) ||
    			((compType==StateMod_DataSet.COMP_RIVER_NETWORK)
    			&& StateMod_Util.isMissing(rin.getName())) ) {
    			if ( NameFormat.equalsIgnoreCase( "StationName_NodeType") ) {
    				name = StringUtil.formatString(net_name, "%-20.20s") + "_" + node_type;
    			}
    			else {
    				// "StationName" - default
    				name = net_name;
    			}
    			Message.printStatus ( 2, routine, "Filling " + id + " Name -> " + name );
    			smdata.setName ( name );
    		}
    		if ( (compType == StateMod_DataSet.COMP_RIVER_NETWORK) &&
    			StateMod_Util.isMissing(rin.getComment()) && (CommentFormat != null) ) {
    			if ( CommentFormat.equalsIgnoreCase("StationID") ) {
    				rin.setComment ( rin.getID() );
    			}
    		}
    		++matchCount;
    	}
    	
    	// If nothing was matched, take other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No network identifiers were matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No network identifiers were matched: failing and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifiers are correct." ) );
			}
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error filling data (" + e + ").";
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

	String ID = parameters.getValue ( "ID" );
	String NameFormat = parameters.getValue ( "NameFormat" );
	String CommentFormat = parameters.getValue ( "CommentFormat" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"");
	}
    if ( (this instanceof FillStreamGageStationsFromNetwork_Command) ||
        (this instanceof FillStreamEstimateStationsFromNetwork_Command)||
        (this instanceof FillRiverNetworkFromNetwork_Command)){
		if ( (NameFormat != null) && (NameFormat.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "NameFormat=\"" + NameFormat + "\"");
		}
    }
	if ( this instanceof FillRiverNetworkFromNetwork_Command ) {
		if ( (CommentFormat != null) && (CommentFormat.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "CommentFormat=\"" + CommentFormat + "\"" );
		}
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
