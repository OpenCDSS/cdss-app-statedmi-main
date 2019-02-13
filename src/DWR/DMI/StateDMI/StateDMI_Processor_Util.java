// StateDMI_Processor_Util - This class contains static utility methods to support TSCommandProcessor.

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

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class contains static utility methods to support TSCommandProcessor.  These methods
are here to prevent the processor from getting to large and in some cases because code is
being migrated.
*/
public class StateDMI_Processor_Util {
	
/**
Get the commands before the indicated index position.  Only the requested commands
are returned.  Use this, for example, to get the setWorkingDir() commands above
the insert position for a readXXX() command, so the working directory can be
defined and used in the editor dialog.
@return List of commands (as Vector of Command instances) before the index that match the commands in
the needed_commands_Vector.  This will always return a non-null Vector, even if
no commands are in the Vector.
@param index The index in the command list before which to search for other commands.
@param processor A TSCommandProcessor with commands to search.
@param needed_commands_String_Vector Vector of commands (as String) that need to be processed
(e.g., "setWorkingDir").  Only the main command name should be defined.
@param last_only if true, only the last item above the insert point
is returned.  If false, all matching commands above the point are returned in
the order from top to bottom.
*/
public static List getCommandsBeforeIndex (
	int index,
	StateDMI_Processor processor,
	List needed_commands_String_Vector,
	boolean last_only )
{	// Now search backwards matching commands for each of the requested
	// commands...
	int size = 0;
	if ( needed_commands_String_Vector != null ) {
		size = needed_commands_String_Vector.size();
	}
	String needed_command_string;
	List found_commands = new Vector();
	// Get the commands from the processor
	List commands = processor.getCommands();
	Command command;
	// Now loop up through the command list...
	for ( int ic = (index - 1); ic >= 0; ic-- ) {
		command = (Command)commands.get(ic);
		for ( int i = 0; i < size; i++ ) {
			needed_command_string = (String)needed_commands_String_Vector.get(i);
			//((String)_command_List.getItem(ic)).trim() );
			if (	needed_command_string.regionMatches(true,0,command.toString().trim(),0,
					needed_command_string.length() ) ) {
					found_commands.add ( command );
					if ( last_only ) {
						// Don't need to search any more...
						break;
					}
				}
			}
		}
		// Reverse the commands so they are listed in the order of the list...
		size = found_commands.size();
		if ( size <= 1 ) {
			return found_commands;
		}
		List found_commands_sorted = new Vector(size);
		for ( int i = size - 1; i >= 0; i-- ) {
			found_commands_sorted.add ( found_commands.get(i));
		}
		return found_commands_sorted;
}
	
/**
Get the commands above an index position.
@param processor The processor that is managing commands.
@param pos Index (0+) before which to get commands.  The command at the indicated
position is NOT included in the search.
*/
private static List getCommandsBeforeIndex ( StateDMI_Processor processor, int pos )
{	List commands = new Vector();
	int size = processor.size();
	if ( pos > size ) {
		pos = size;
	}
	for ( int i = 0; i < pos; i++ ) {
		commands.add ( processor.get(i));
	}
	return commands;
}

/**
Get the maximum command status severity for the processor.  This is used, for example, when
determining an overall status for a runCommands() command.
@param processor Command processor to check status.
@return most severe command status from all commands in a processor.
*/
public static CommandStatusType getCommandStatusMaxSeverity ( StateDMI_Processor processor )
{
	int size = processor.size();
	Command command;
	CommandStatusType most_severe = CommandStatusType.UNKNOWN;
	CommandStatusType from_command;
	for ( int i = 0; i < size; i++ ) {
		command = processor.get(i);
		if ( command instanceof CommandStatusProvider ) {
			from_command = CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command);
			//Message.printStatus (2,"", "Highest severity \"" + command.toString() + "\"=" + from_command.toString());
			most_severe = CommandStatusType.maxSeverity(most_severe,from_command);
		}
	}
	return most_severe;
}

/**
Get the working directory for a command (e.g., for editing).
@param processor the TSCommandProcessor to use to get data.
@param command Command for which to get the working directory.
@return The working directory in effect for a command.
*/
public static String getWorkingDirForCommand ( StateDMI_Processor processor, Command command )
{	String routine = "TSTool_JFrame.commandProcessor_GetWorkingDirForCommand";
	PropList request_params = new PropList ( "" );
	request_params.setUsingObject ( "Command", command );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		processor.processRequest( "GetWorkingDirForCommand", request_params );
		return bean.getResultsPropList().getValue("WorkingDir");
	}
	catch ( Exception e ) {
		String message = "Error requesting GetWorkingDirForCommand(Command=\"" + command +
		"\" from processor).";
		Message.printWarning(3, routine, e);
		Message.printWarning(3, routine, message );
	}
	return null;
}

}

