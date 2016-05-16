package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadStreamGageStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadStreamGageStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadStreamGageStationsFromList_Command ()
{	super();
	setCommandName ( "ReadStreamGageStationsFromList" );
}
	
}