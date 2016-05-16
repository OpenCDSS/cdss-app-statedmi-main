package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDelayTablesMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteDelayTablesMonthlyToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDelayTablesMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteDelayTablesMonthlyToStateMod" );
}
	
}