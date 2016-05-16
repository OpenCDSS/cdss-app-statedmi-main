package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDelayTablesMonthlyToList() command.
Most functionality is implemented in the base class.
*/
public class WriteDelayTablesMonthlyToList_Command extends WriteToList_Command
{
	
/**
Constructor.
*/
public WriteDelayTablesMonthlyToList_Command ()
{	super();
	setCommandName ( "WriteDelayTablesMonthlyToList" );
}
	
}