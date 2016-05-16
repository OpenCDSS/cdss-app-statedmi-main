package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the FillDiversionStation() command.
</p>
*/
public class FillDiversionStation_Command extends FillAndSetDiversionAndWellStation_Command
{
	
/**
Constructor.
*/
public FillDiversionStation_Command ()
{	super();
	setCommandName ( "FillDiversionStation" );
}

}