package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteWellDemandTSMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteWellDemandTSMonthlyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteWellDemandTSMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteWellDemandTSMonthlyToStateMod" );
}
	
}