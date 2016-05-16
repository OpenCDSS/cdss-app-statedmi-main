package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirRightsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirRightsToList_Command extends WriteToList_Command
{

/**
Constructor.
*/
public WriteReservoirRightsToList_Command ()
{	super();
	setCommandName ( "WriteReservoirRightsToList" );
}
	
}
