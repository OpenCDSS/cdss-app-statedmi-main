package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteWellRightsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteWellRightsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteWellRightsToStateMod_Command ()
{	super();
	setCommandName ( "WriteWellRightsToStateMod" );
}
	
}