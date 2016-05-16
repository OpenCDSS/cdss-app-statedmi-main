package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadReservoirStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadReservoirStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadReservoirStationsFromList_Command ()
{	super();
	setCommandName ( "ReadReservoirStationsFromList" );
}
	
}