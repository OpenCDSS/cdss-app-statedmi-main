package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteDelayTablesToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteDelayTablesToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteDelayTablesToStateCU_Command ()
{	super();
	setCommandName ( "WriteDelayTablesToStateCU" );
}

}