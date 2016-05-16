package DWR.DMI.StateDMI;

/**
This class initializes and runs the WritePlanStationsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WritePlanStationsToStateMod_Command extends WriteToStateMod_Command
{

/**
Constructor.
*/
public WritePlanStationsToStateMod_Command ()
{	super();
	setCommandName ( "WritePlanStationsToStateMod" );
}
	
}