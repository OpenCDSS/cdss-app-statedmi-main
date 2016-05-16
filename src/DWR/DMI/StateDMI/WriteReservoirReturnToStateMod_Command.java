package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirReturnToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirReturnToStateMod_Command extends WriteToStateMod_Command
{

/**
Constructor.
*/
public WriteReservoirReturnToStateMod_Command ()
{	super();
	setCommandName ( "WriteReservoirReturnToStateMod" );
}
	
}