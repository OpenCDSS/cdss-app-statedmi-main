package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirRightsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirRightsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteReservoirRightsToStateMod_Command ()
{	super();
	setCommandName ( "WriteReservoirRightsToStateMod" );
}
	
}