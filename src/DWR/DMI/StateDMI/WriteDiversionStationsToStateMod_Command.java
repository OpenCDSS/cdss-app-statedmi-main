package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDiversionStationsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteDiversionStationsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDiversionStationsToStateMod_Command ()
{	super();
	setCommandName ( "WriteDiversionStationsToStateMod" );
}
	
}