package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteInstreamFlowDemandTSAverageMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteInstreamFlowDemandTSAverageMonthlyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteInstreamFlowDemandTSAverageMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteInstreamFlowDemandTSAverageMonthlyToStateMod" );
}
	
}