package DWR.DMI.StateDMI;

/**
This class initializes and runs the WritePenmanMonteithToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WritePenmanMonteithToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WritePenmanMonteithToStateCU_Command ()
{	super();
	setCommandName ( "WritePenmanMonteithToStateCU" );
}

}