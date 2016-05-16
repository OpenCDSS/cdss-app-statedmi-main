package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteCropCharacteristicsToStateCU() command.
Most functionality is implemented in the base class.
*/
public class WriteCropCharacteristicsToStateCU_Command extends WriteToStateCU_Command
{
	
/**
Constructor.
*/
public WriteCropCharacteristicsToStateCU_Command ()
{	super();
	setCommandName ( "WriteCropCharacteristicsToStateCU" );
}

}