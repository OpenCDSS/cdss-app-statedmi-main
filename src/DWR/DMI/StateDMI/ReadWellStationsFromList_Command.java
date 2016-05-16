package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadWellStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadWellStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadWellStationsFromList_Command ()
{	super();
	setCommandName ( "ReadWellStationsFromList" );
}
	
}