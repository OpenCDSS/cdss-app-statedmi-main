package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the ReadReservoirStationsFromNetwork() command.
</p>
*/
public class ReadReservoirStationsFromNetwork_Command extends ReadFromNetwork_Command
{
	
/**
Constructor.
*/
public ReadReservoirStationsFromNetwork_Command ()
{	super();
	setCommandName ( "ReadReservoirStationsFromNetwork" );
}

}