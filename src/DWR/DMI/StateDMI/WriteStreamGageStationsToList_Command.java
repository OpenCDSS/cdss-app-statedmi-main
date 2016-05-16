package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteStreamGageStationsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteStreamGageStationsToList_Command extends WriteToList_Command
{

/**
Constructor.
*/
public WriteStreamGageStationsToList_Command ()
{	super();
	setCommandName ( "WriteStreamGageStationsToList" );
}

}
