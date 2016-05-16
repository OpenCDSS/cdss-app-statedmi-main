package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteOperationalRightsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteOperationalRightsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteOperationalRightsToStateMod_Command ()
{	super();
	setCommandName ( "WriteOperationalRightsToStateMod" );
}
	
}