package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteControlToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteControlToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteControlToStateMod_Command ()
{	super();
	setCommandName ( "WriteControlToStateMod" );
}
	
}