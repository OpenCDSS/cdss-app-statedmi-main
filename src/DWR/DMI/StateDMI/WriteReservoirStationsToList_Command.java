package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirStationsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirStationsToList_Command extends WriteToList_Command
{

/**
Constructor.
*/
public WriteReservoirStationsToList_Command ()
{	super();
	setCommandName ( "WriteReservoirStationsToList" );
}

}
