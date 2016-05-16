package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetReservoirStation() command.
</p>
*/
public class SetReservoirStation_Command extends FillAndSetReservoirStation_Command
{
	
/**
Constructor.
*/
public SetReservoirStation_Command ()
{	super();
	setCommandName ( "SetReservoirStation" );
}

}