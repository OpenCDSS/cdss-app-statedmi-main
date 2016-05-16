package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the FillStreamGageStation() command.
</p>
*/
public class FillStreamGageStation_Command extends FillAndSetStreamEstimateAndGageStation_Command
{
	
/**
Constructor.
*/
public FillStreamGageStation_Command ()
{	super();
	setCommandName ( "FillStreamGageStation" );
}

}