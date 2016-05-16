package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteWellHistoricalPumpingTSMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteWellHistoricalPumpingTSMonthlyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteWellHistoricalPumpingTSMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteWellHistoricalPumpingTSMonthlyToStateMod" );
}
	
}