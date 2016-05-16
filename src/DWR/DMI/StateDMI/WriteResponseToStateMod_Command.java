package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteResponseToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteResponseToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteResponseToStateMod_Command ()
{	super();
	setCommandName ( "WriteResponseToStateMod" );
}
	
}