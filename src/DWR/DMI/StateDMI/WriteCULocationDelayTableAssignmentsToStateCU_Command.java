package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteCULocationDelayTableAssignmentsToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteCULocationDelayTableAssignmentsToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteCULocationDelayTableAssignmentsToStateCU_Command ()
{	super();
	setCommandName ( "WriteCULocationDelayTableAssignmentsToStateCU" );
}

}