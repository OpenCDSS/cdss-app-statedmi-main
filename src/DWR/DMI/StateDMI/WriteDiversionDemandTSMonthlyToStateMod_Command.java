package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDiversionDemandTSMonthlyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteDiversionDemandTSMonthlyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDiversionDemandTSMonthlyToStateMod_Command ()
{	super();
	setCommandName ( "WriteDiversionDemandTSMonthlyToStateMod" );
}
	
}