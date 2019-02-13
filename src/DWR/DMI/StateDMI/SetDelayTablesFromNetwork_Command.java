// SetDelayTablesFromNetwork_Command - This class initializes, checks, and runs the SetDiversionStationDelayTablesFromNetwork() and
// SetWellStationDelayTablesFromNetwork() commands.

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

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_ReturnFlow;
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
This class initializes, checks, and runs the SetDiversionStationDelayTablesFromNetwork() and
SetWellStationDelayTablesFromNetwork() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the
SetDiversionStationDelayTablesFromNetwork() command extends this class in order to uniquely represent
the command, but much of the functionality is in this base class.
</p>
*/
public abstract class SetDelayTablesFromNetwork_Command extends AbstractCommand implements Command
{
	
/**
Values for the IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetDelayTablesFromNetwork_Command ()
{	super();
	setCommandName ( "Set?StationDelayTablesFromNetwork" );
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
	String DefaultTable = parameters.getValue ( "DefaultTable" );
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
	
	if ( (DefaultTable != null) && (DefaultTable.length() != 0) && !StringUtil.isInteger(DefaultTable)) {
        message = "The defualt table ID is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the default table ID as an integer." ) );
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
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "DefaultTable" );
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
	return (new SetDelayTablesFromNetwork_JDialog ( parent, this )).ok();
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
    String DefaultTable = parameters.getValue ( "DefaultTable" );
	if ( DefaultTable == null ) {
		DefaultTable = "1";
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    
    // Get the data needed for the command
	
    List stationList = null;
    int stationListSize = 0;
    try {
    	if ( this instanceof SetDiversionStationDelayTablesFromNetwork_Command ) {
    		stationList = (List)processor.getPropContents ( "StateMod_DiversionStation_List" );
    	}
    	else if ( this instanceof SetWellStationDelayTablesFromNetwork_Command ) {
    		stationList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
    	}
		stationListSize = stationList.size();
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
    	boolean do_well = false;
    	boolean do_div = false;
    	if ( this instanceof SetDiversionStationDelayTablesFromNetwork_Command ) {
    		do_div = true;
    	}
    	else if ( this instanceof SetWellStationDelayTablesFromNetwork_Command  ) {
    		do_well = true;
    	}
    	StateMod_Diversion div = null;
    	StateMod_Well well = null;
    	StateMod_ReturnFlow smret = null;
    	HydrologyNode node = null; // Node in the network
    	List smret_Vector = null; // List of smret.
    	String id = null; // Diversion or well ID.
    	for ( int i = 0; i < stationListSize; i++ ) {
    		if ( do_div ) {
    			div = (StateMod_Diversion)stationList.get(i);
    			id = div.getID();
    		}
    		else if ( do_well ) {
    			well = (StateMod_Well)stationList.get(i);
    			id = well.getID();
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Find the node in the network...

    		node = net.findNode ( id );
    		if ( node == null ) {
    			message = "Cannot find node \"" + id + "\" in the network - " +
    			"cannot determine return location.";
    	        Message.printWarning ( warning_level, 
	                MessageUtil.formatMessageTag(command_tag, ++warning_count),
	                routine, message );
	                status.addToLog ( command_phase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Verify that location \"" + id + "\" is a node in the network."));
    			continue;
    		}

    		// Now find the downstream node that is a real node (not a confluence, etc.)...

    		node = StateMod_NodeNetwork.findNextRealDownstreamNode ( node );
    		if ( node == null ) {
    			message = "Cannot find node downstream of \"" + id +
    			"\" in the network - cannot determine return location.";
    	        Message.printWarning ( warning_level, 
	                MessageUtil.formatMessageTag(command_tag, ++warning_count),
	                routine, message );
	                status.addToLog ( command_phase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Verify that location \"" + id + "\" is a node in the network."));
    			continue;
    		}

    		// Reset the data with the specified filled values.

    		Message.printStatus ( 2, routine, "Setting " + id + " Return -> 100% to " + node.getCommonID() );

    		if ( do_div ) {
    			smret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_DIVERSION_STATIONS );
    		}
    		else if ( do_well ) {
    			smret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_WELL_STATIONS );
    		}
    		smret.setCrtnid ( node.getCommonID() );
    		smret.setPcttot ( 100.0 );
    		smret.setIrtndl ( DefaultTable );
    		smret_Vector = new Vector();	// Do not reuse!
    		smret_Vector.add ( smret );
    		if ( do_div ) {
    			div.setReturnFlow ( smret_Vector );
    		}
    		else if ( do_well ) {
    			well.setReturnFlows ( smret_Vector );
    		}
    	}

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No identifiers were matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No identifiers were matched: failing and not setting.";
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
        message = "Unexpected error setting delay tables (" + e + ").";
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
	String DefaultTable = parameters.getValue ( "DefaultTable" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"");
	}
	if ( (DefaultTable != null) && (DefaultTable.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DefaultTable=" + DefaultTable );
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
