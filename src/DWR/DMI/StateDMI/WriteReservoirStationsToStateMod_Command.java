package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirStationsToStateMod() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirStationsToStateMod_Command extends WriteToStateMod_Command
{
	
/**
Constructor.
*/
public WriteReservoirStationsToStateMod_Command ()
{	super();
	setCommandName ( "WriteReservoirStationsToStateMod" );
}
	
}