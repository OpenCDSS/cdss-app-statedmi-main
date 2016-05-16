package DWR.DMI.StateDMI;

/**
This class initializes and runs the ReadInstreamFlowStationsFromList() command.
Most functionality is implemented in the base class.
*/
public class ReadInstreamFlowStationsFromList_Command extends ReadFromList_Command
{
	
/**
Constructor.
*/
public ReadInstreamFlowStationsFromList_Command ()
{	super();
	setCommandName ( "ReadInstreamFlowStationsFromList" );
}
	
}