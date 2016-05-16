package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadDiversionStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadDiversionStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadDiversionStationsFromList_Command ()
{	super();
	setCommandName ( "ReadDiversionStationsFromList" );
}
	
}