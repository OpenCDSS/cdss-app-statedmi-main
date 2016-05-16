package DWR.DMI.StateDMI;

/**
This class initializes and runs the WritePlanReturnToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WritePlanReturnToStateMod_Command extends WriteToStateMod_Command
{

/**
Constructor.
*/
public WritePlanReturnToStateMod_Command ()
{	super();
	setCommandName ( "WritePlanReturnToStateMod" );
}
	
}