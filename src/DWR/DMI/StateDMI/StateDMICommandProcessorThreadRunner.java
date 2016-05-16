package DWR.DMI.StateDMI;

import RTi.Util.IO.PropList;

/**
This class runs a StateDMI_Processor on a thread.  Its main purpose is to
help provide cancel/pause/feedback features for UI applications and to support the RunCommands() command.
Currently the run() method calls the StateDMI_Processor runCommands() method
to run all commands.  Any registered listeners on the command processor will be notified.
*/
public class StateDMICommandProcessorThreadRunner implements Runnable
{

/**
The TSCommandProcessor instance that is used to run the commands (no run parameters).
*/
private StateDMI_Processor __processor = null;

/**
The run request parameters used to run commands, when specifying run parameters.
*/
private PropList __requestParams = null;

/**
Construct a StateDMICommandProcessorThreadRunner using a StateDMI_Processor instance.
When run() is called, all commands will be run using the working directory from
the commands file that was originally read.
*/
public StateDMICommandProcessorThreadRunner ( StateDMI_Processor processor )
{
	__processor = processor;
}

/**
Construct a StateDMICommandProcessorThreadRunner using a StateDMI_Processor instance.
When run() is called, all commands will be run using properties as shown.
Properties are passed to the TSCommandProcessor.processRequest("RunCommands") method,
which recognizes parameters as per the following pseudocode:
<pre>
PropList request_params = new PropList ( "" );
request_params.setUsingObject ( "CommandList", Vector<Command> commands );
request_params.setUsingObject ( "InitialWorkingDir", String getInitialWorkingDir() );
request_params.setUsingObject ( "CreateOutput", new Boolean(create_output) );
</pre>
*/
public StateDMICommandProcessorThreadRunner ( StateDMI_Processor processor, PropList request_params )
{
	__processor = processor;
	__requestParams = request_params;
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
