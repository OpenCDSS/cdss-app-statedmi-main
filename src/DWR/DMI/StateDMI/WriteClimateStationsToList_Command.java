package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteClimateStationsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteClimateStationsToList_Command extends WriteToList_Command
{
	
/**
Constructor.
*/
public WriteClimateStationsToList_Command ()
{	super();
	setCommandName ( "WriteClimateStationsToList" );
}

}