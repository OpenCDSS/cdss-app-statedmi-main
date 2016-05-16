package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetReservoirRight() command.
</p>
*/
public class SetReservoirRight_Command extends FillAndSetRight_Command
{
	
/**
Constructor.
*/
public SetReservoirRight_Command ()
{	super();
	setCommandName ( "SetReservoirRight" );
}

}