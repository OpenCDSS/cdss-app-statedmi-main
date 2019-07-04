// StateDMICommandFileRunner - This class allows a commands file to be be run.

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import RTi.Util.IO.Command;

/**
This class allows a commands file to be be run.  For example, it can be
used to make a batch run of a commands file.  An instance of StateDMI_Processor
is created to process the commands.
*/
public class StateDMICommandFileRunner
{

/**
The StateDMI_Processor instance that is used to run the commands.
*/
private StateDMI_Processor __processor = new StateDMI_Processor();

/**
Return the command processor used by the runner.
@return the command processor used by the runner
*/
public StateDMI_Processor getProcessor() {
    return __processor;
}

/**
Determine whether the command file is enabled.
This is used in the TSTool RunCommands() command to determine if a command file is enabled.
@return false if any comments have "@enabled False", otherwise true
*/
public boolean isCommandFileEnabled ()
{
    List<Command> commands = __processor.getCommands();
    String C;
    int pos;
    for ( Command command : commands ) {
        C = command.toString().toUpperCase();
        pos = C.indexOf("@ENABLED");
        if ( pos >= 0 ) {
            //Message.printStatus(2, "", "Detected tag: " + C);
            // Check the token following @enabled
            if ( C.length() > (pos + 8) ) {
                // Have trailing characters
                String [] parts = C.substring(pos).split(" ");
                if ( parts.length > 1 ) {
                    if ( parts[1].trim().equals("FALSE") ) {
                        //Message.printStatus(2, "", "Detected false");
                        return false;
                    }
                }
            }
        }
    }
    //Message.printStatus(2, "", "Did not detect false");
    return true;
}

/**
Read the commands from a file.
@param filename Name of command file to run, should be absolute.
*/
public void readCommandFile ( String path )
throws FileNotFoundException, IOException
{	__processor.readCommandFile (
		path,	// InitialWorkingDir will be set to commands file location
		true,	// Create GenericCommand instances for unknown commands
		false );	// Do not append the commands.
}

/**
Run the commands.
*/
public void runCommands ()
throws Exception
{
	__processor.runCommands(
			null,		// Subset of Command instances to run - just run all
			null );		// Properties to control run
}

}