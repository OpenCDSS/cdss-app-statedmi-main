package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteClimateStationsToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteClimateStationsToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteClimateStationsToStateCU_Command ()
{	super();
	setCommandName ( "WriteClimateStationsToStateCU" );
}

}