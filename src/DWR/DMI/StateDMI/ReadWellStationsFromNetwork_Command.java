package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the ReadWellStationsFromNetwork() command.
</p>
*/
public class ReadWellStationsFromNetwork_Command extends ReadFromNetwork_Command
{
	
/**
Constructor.
*/
public ReadWellStationsFromNetwork_Command ()
{	super();
	setCommandName ( "ReadWellStationsFromNetwork" );
}

}