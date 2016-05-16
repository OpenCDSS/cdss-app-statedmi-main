package DWR.DMI.StateDMI;

/**
This class initializes, checks, and runs the ReadResponseFromStateMod() command.
*/
public class ReadResponseFromStateMod_Command extends ReadFromStateMod_Command
{

/**
Constructor.
*/
public ReadResponseFromStateMod_Command ()
{	super();
	setCommandName ( "ReadResponseFromStateMod" );
}

}