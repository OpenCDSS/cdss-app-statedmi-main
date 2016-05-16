package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteWellStationsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteWellStationsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteWellStationsToStateMod_Command ()
{	super();
	setCommandName ( "WriteWellStationsToStateMod" );
}
	
}