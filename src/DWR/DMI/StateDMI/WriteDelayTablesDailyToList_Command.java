package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDelayTablesDailyToList() command.
Most functionality is implemented in the base class.
*/
public class WriteDelayTablesDailyToList_Command extends WriteToList_Command
{
	
/**
Constructor.
*/
public WriteDelayTablesDailyToList_Command ()
{	super();
	setCommandName ( "WriteDelayTablesDailyToList" );
}
	
}