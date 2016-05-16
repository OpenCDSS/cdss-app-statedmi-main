package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDiversionHistoricalTSMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteDiversionHistoricalTSMonthlyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDiversionHistoricalTSMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteDiversionHistoricalTSMonthlyToStateMod" );
}
	
}