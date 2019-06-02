// CreateRiverNetworkFromNetwork_Command - This class initializes, checks, and runs the CreateRiverNetworkFromNetwork() command.

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

import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_RiverNetworkNode;
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
<p>
This class initializes, checks, and runs the CreateRiverNetworkFromNetwork() command.
</p>
*/
public class CreateRiverNetworkFromNetwork_Command 
extends AbstractCommand implements Command
{

/**
Constructor.
*/
public CreateRiverNetworkFromNetwork_Command ()
{	super();
	setCommandName ( "CreateRiverNetworkFromNetwork" );
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
{	String routine = "CreateRiverNetworkFromNetwork.checkCommandParameters";
	//String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();

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
	return (new CreateRiverNetworkFromNetwork_JDialog ( parent, this )).ok();
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
	
    CommandPhaseType command_phase = CommandPhaseType.RUN;
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters (none)...

	// Get the network
	
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
		// Create the river network. The node types will not be known because
		// the RIN does not explicitly carry the node types...

		List<StateMod_RiverNetworkNode> riverNetworkNodeList = net.createStateModRiverNetwork();

		// Always make sure that a non-null list is available...

		if ( riverNetworkNodeList == null ) {
			riverNetworkNodeList = new Vector<StateMod_RiverNetworkNode>();
	        message = "No river nodes were created from the network.";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the general network incldues 1+ nodes." ) );
		}
		else {
			Message.printStatus(2, routine, "Created " + riverNetworkNodeList +
				" river nodes from network.");
		}
		
		// Set in the processor
		
		processor.setPropContents ( "StateMod_RiverNetworkNode_List", riverNetworkNodeList );
	}
    catch ( Exception e ) {
        message = "Unexpected error creating river network from network (" + e + ").";
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
