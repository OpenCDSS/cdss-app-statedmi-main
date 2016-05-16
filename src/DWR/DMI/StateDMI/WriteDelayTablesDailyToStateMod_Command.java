package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDelayTablesDailyToStateMod_Command() command.
Most functionality is implemented in the base class.
*/
public class WriteDelayTablesDailyToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDelayTablesDailyToStateMod_Command ()
{	super();
	setCommandName ( "WriteDelayTablesDailyToStateMod_Command" );
}
	
}