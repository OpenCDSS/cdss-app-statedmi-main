package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteBlaneyCriddleToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteBlaneyCriddleToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteBlaneyCriddleToStateCU_Command ()
{	super();
	setCommandName ( "WriteBlaneyCriddleToStateCU" );
}

}