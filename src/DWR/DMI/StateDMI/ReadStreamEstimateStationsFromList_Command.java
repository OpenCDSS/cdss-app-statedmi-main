package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadStreamEstimateStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadStreamEstimateStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadStreamEstimateStationsFromList_Command ()
{	super();
	setCommandName ( "ReadStreamEstimateStationsFromList" );
}
	
}