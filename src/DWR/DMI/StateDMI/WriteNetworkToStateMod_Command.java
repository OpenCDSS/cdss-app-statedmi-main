package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteNetworkToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteNetworkToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteNetworkToStateMod_Command ()
{	super();
	setCommandName ( "WriteNetworkToStateMod" );
}
	
}