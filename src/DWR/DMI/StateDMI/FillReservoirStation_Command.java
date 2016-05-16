package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the FillReservoirStation() command.
</p>
*/
public class FillReservoirStation_Command extends FillAndSetReservoirStation_Command
{
	
/**
Constructor.
*/
public FillReservoirStation_Command ()
{	super();
	setCommandName ( "FillReservoirStation" );
}

}