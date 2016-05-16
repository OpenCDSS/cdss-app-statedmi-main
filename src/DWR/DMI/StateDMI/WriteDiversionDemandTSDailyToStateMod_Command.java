package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDiversionDemandTSDailyToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteDiversionDemandTSDailyToStateMod_Command extends WriteTSToStateMod_Command
{
	
/**
Constructor.
*/
public WriteDiversionDemandTSDailyToStateMod_Command ()
{	super();
	setCommandName ( "WriteDiversionDemandTSDailyToStateMod" );
}
	
}