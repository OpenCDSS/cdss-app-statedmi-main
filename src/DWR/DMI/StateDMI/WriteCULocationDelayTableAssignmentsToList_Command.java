package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteCULocationDelayTableAssignmentsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteCULocationDelayTableAssignmentsToList_Command extends WriteToList_Command
{

/**
Constructor.
*/
public WriteCULocationDelayTableAssignmentsToList_Command ()
{	super();
	setCommandName ( "WriteCULocationDelayTableAssignmentsToList" );
}

}