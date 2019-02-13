// StateDMI_Processor_ThreadRunner - This class runs a StateDMI_Processor on a thread.

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

import RTi.Util.IO.PropList;

/**
This class runs a StateDMI_Processor on a thread.  Its main purpose is to
help provide cancel/pause/feedback features for UI applications.
Currently the run() method calls the StateDMI_Processor runCommands() method
to run all commands.  Any registered listeners on the command processor will
be notified.
*/
public class StateDMI_Processor_ThreadRunner implements Runnable
{

/**
The StateDMI_Processor instance that is used to run the commands (no run parameters).
*/
private StateDMI_Processor __processor = null;

/**
The run request parameters used to run commands, when specifying run parameters.
*/
private PropList __requestParams = null;

/**
Construct a StateDMI_Processor_ThreadRunner using a StateDMI_Processor instance.
When run() is called, all commands will be run using the working directory from
the commands file that was originally read.
*/
public StateDMI_Processor_ThreadRunner ( StateDMI_Processor processor )
{
	__processor = processor;
}

/**
Construct a StateDMI_Processor_ThreadRunner using a StateDMI_Processor instance.
When run() is called, all commands will be run using properties as shown.
Properties are passed to the StateDMI_Processor.processRequest("RunCommands") method,
which recognizes parameters as per the following pseudocode:
<pre>
PropList request_params = new PropList ( "" );
request_params.setUsingObject ( "CommandList", Vector<Command> commands );
request_params.setUsingObject ( "CreateOutput", new Boolean(create_output) );
</pre>
*/
public StateDMI_Processor_ThreadRunner ( StateDMI_Processor processor, PropList requestParams )
{
	__processor = processor;
	__requestParams = requestParams;
}

/**
Run the commands in the current command processor.
*/
public void run ()
{
	try {
		if ( __requestParams == null ) {
			__processor.runCommands(
					null,		// Subset of Command instances to run - just run all
					null );		// Properties to control run
		}
		else {
			__processor.processRequest( "RunCommands", __requestParams );
		}
		__processor = null;
		__requestParams = null;
	}
	catch ( Exception e ) {
		// FIXME SAM 2007-10-10 Need to handle exception in run
	}
}

}
