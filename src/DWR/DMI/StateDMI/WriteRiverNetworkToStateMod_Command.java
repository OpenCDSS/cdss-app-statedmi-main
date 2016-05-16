package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteRiverNetworkToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteRiverNetworkToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteRiverNetworkToStateMod_Command ()
{	super();
	setCommandName ( "WriteRiverNetworkToStateMod" );
}
	
}