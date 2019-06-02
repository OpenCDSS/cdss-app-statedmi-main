// FillAndSetRiverNetworkNode_Command - This class initializes, checks, and runs the FillRiverNetworkNode() and SetRiverNetworkNode() commands.

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

import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_Util;

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
This class initializes, checks, and runs the FillRiverNetworkNode() and SetRiverNetworkNode() commands.
The functionality is handled in one class due to the
close similarity between the commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetRiverNetworkNode()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
*/
public abstract class FillAndSetRiverNetworkNode_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetRiverNetworkNode_Command ()
{	super();
	setCommandName ( "?Fill/Set?RiverNetworkNode" );
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
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	String MaxRechargeLimit = parameters.getValue ( "MaxRechargeLimit" );
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
	
	if ( (DownstreamRiverNodeID != null) && (DownstreamRiverNodeID.length() > 0) &&
		((DownstreamRiverNodeID.indexOf(" ") >= 0) || (DownstreamRiverNodeID.indexOf("-") >= 0))) {
        message = "The DownstreamRiverNodeID (" + DownstreamRiverNodeID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the DownstreamRiverNodeID without spaces or dashes." ) );
	}
	
	if ( (MaxRechargeLimit != null) && (MaxRechargeLimit.length() > 0) && !StringUtil.isDouble(MaxRechargeLimit)) {
        message = "The MaxRechargeLimit (" + MaxRechargeLimit + ") is not a valid number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the MaxRechargeLimit is a number." ) );
	}

	if ( (this instanceof SetRiverNetworkNode_Command) || (this instanceof SetRiverNetworkNode_Command) ) {
		// Include the Add option
		if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
			!IfNotFound.equalsIgnoreCase(_Add) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
			message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
					", or " + _Warn + " (default).") );
		}
	}
	else {
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
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(5);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "DownstreamRiverNodeID" );
	valid_Vector.add ( "MaxRechargeLimit" );
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
	return (new FillAndSetRiverNetworkNode_JDialog ( parent, this )).ok();
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
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.
    // Trim strings because Ray Bennett has written FORTRAN programs to process command files, and there
    // are sometimes extra spaces in the parameter values - this causes IDs to not be matched, etc.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	ID = ID.trim();
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String Name = parameters.getValue ( "Name" );
	if ( Name != null ) {
		Name = Name.trim();
	}
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	if ( DownstreamRiverNodeID != null ) {
		DownstreamRiverNodeID = DownstreamRiverNodeID.trim();
	}
	String Comment = ""; // Currently no way to set.
	String MaxRechargeLimit = parameters.getValue ( "MaxRechargeLimit" );
	if ( MaxRechargeLimit != null ) {
		MaxRechargeLimit = MaxRechargeLimit.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();

    // Get the data needed for the command
    
    List<StateMod_RiverNetworkNode> riverNetworkNodeList = null;
    int riverNetworkNodeListSize = 0;
    try {
    	@SuppressWarnings("unchecked")
		List<StateMod_RiverNetworkNode> dataList = (List<StateMod_RiverNetworkNode>)processor.getPropContents ( "StateMod_RiverNetworkNode_List" );
    	riverNetworkNodeList = dataList;
    	riverNetworkNodeListSize = riverNetworkNodeList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting river network node data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
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
    	// Check the DownstreamRiverID supplied from the dialog
    	boolean fill_Name = false;
    	if ( Name != null ) {
    		fill_Name = true;
    	}
    	
		// Check the DownstreamRiverID supplied from the dialog
		boolean fill_DownstreamRiverNodeID = false;
		if ( DownstreamRiverNodeID != null ) {
			fill_DownstreamRiverNodeID = true;
		}
	
		// Check the MaxRechargeLimit supplied from the dialog
		boolean fill_MaxRechargeLimit = false;
		if ( MaxRechargeLimit != null ) {
			fill_MaxRechargeLimit = true;
		}
	
		boolean fill_Comment = false;
		if ( (Comment != null) && !Comment.equals("") ) {
			fill_Comment = true;
		}
	
		// Setup the RiverNetwork Object based on user input
		StateMod_RiverNetworkNode node = null;
		String id;
		String action = "Setting ";
		boolean fill = false; // Set
		if ( this instanceof FillRiverNetworkNode_Command ) {
			action = "Filling ";
			fill = true;
		}
		int matchCount = 0;
		for (int i = 0; i < riverNetworkNodeListSize; i++) {
			node = (StateMod_RiverNetworkNode)riverNetworkNodeList.get(i);
			id = node.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			if ( fill_Name && (!fill || StateMod_Util.isMissing(node.getName())) ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				node.setName ( Name );
			}
			if ( fill_DownstreamRiverNodeID && (!fill || StateMod_Util.isMissing(node.getCstadn())) ) {
				Message.printStatus ( 2, routine,
				"Setting " + id + " DownstreamRiverNodeID -> " + DownstreamRiverNodeID );
				node.setCstadn ( DownstreamRiverNodeID );
			}
			if ( fill_Comment && (!fill || StateMod_Util.isMissing(node.getComment())) ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Comment -> " + Comment );
				node.setComment ( Comment );
			}
			if ( fill_MaxRechargeLimit && (!fill || StateMod_Util.isMissing(node.getGwmaxr())) ) {
				Message.printStatus ( 2, routine, "Setting " + id + " MaxRechargeLimit -> " + MaxRechargeLimit );
				node.setGwmaxr ( MaxRechargeLimit );
			}
		}

		if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
			node = new StateMod_RiverNetworkNode();
			id = ID;
			node.setID ( id );
			riverNetworkNodeList.add ( node );
			Message.printStatus ( 2, routine, "Adding River Network Node " + id );
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				node.setName ( Name );
			}
			if ( fill_DownstreamRiverNodeID ) {
				Message.printStatus ( 2, routine,
				"Setting " + id + " DownstreamRiverNodeID -> " + DownstreamRiverNodeID );
				node.setCstadn ( DownstreamRiverNodeID );
			}
			if ( fill_Comment ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Comment -> " + Comment );
				node.setComment ( Comment );
			}
			if( fill_MaxRechargeLimit ) {
				Message.printStatus(2, routine, "Setting " + id + " MaxRechargeLimit -> " + MaxRechargeLimit);
				node.setGwmaxr( MaxRechargeLimit );
			}
			++matchCount;
		}
		if ( matchCount == 0 ) {
	    	if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
	    		message = "River network node \"" + ID + "\" was not matched: warning and not " +
	    		action.toLowerCase();
    			Message.printWarning(warning_level,
    					MessageUtil.formatMessageTag( command_tag, ++warning_count),
    					routine, message );
    				status.addToLog ( CommandPhaseType.RUN,
    					new CommandLogRecord(CommandStatusType.WARNING,
    						message, "Verify that the identifier is correct." ) );
	    	}
	    	else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
	    		message = "River network node \"" + ID + "\" was not matched: failing and not " +
	    		action.toLowerCase();
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
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing river network node data (" + e + ").";
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
	String Name = parameters.getValue ( "Name" );
	String DownstreamRiverNodeID = parameters.getValue ( "DownstreamRiverNodeID" );
	String MaxRechargeLimit = parameters.getValue ( "MaxRechargeLimit" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (Name != null) && (Name.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Name=\"" + Name + "\"" );
	}
	if ( (DownstreamRiverNodeID != null) && (DownstreamRiverNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DownstreamRiverNodeID=\"" + DownstreamRiverNodeID + "\"" );
	}
	if ( (MaxRechargeLimit != null) && (MaxRechargeLimit.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MaxRechargeLimit=" + MaxRechargeLimit );
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
