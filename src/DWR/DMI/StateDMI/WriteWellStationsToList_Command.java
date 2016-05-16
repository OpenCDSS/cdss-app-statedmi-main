package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteWellStationsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteWellStationsToList_Command extends WriteToList_Command
{
	
/**
Constructor.
*/
public WriteWellStationsToList_Command ()
{	super();
	setCommandName ( "WriteWellStationsToList" );
}
	
}