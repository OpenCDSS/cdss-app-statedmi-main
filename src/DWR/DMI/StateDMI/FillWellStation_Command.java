package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the FillWellStation() command.
</p>
*/
public class FillWellStation_Command extends FillAndSetDiversionAndWellStation_Command
{
	
/**
Constructor.
*/
public FillWellStation_Command ()
{	super();
	setCommandName ( "FillWellStation" );
}

}