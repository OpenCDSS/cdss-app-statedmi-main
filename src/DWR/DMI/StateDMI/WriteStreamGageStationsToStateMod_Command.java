package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteStreamGageStationsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteStreamGageStationsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteStreamGageStationsToStateMod_Command ()
{	super();
	setCommandName ( "WriteStreamGageStationsToStateMod" );
}
	
}