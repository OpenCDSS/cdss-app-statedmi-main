package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteCULocationsToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteCULocationsToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteCULocationsToStateCU_Command ()
{	super();
	setCommandName ( "WriteCULocationsToStateCU" );
}}