package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the ReadStreamGageStationsFromNetwork() command.
</p>
*/
public class ReadStreamGageStationsFromNetwork_Command extends ReadFromNetwork_Command
{
	
/**
Constructor.
*/
public ReadStreamGageStationsFromNetwork_Command ()
{	super();
	setCommandName ( "ReadStreamGageStationsFromNetwork" );
}

}