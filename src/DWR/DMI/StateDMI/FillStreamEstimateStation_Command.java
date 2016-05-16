package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the FillStreamEstimateStation() command.
</p>
*/
public class FillStreamEstimateStation_Command extends FillAndSetStreamEstimateAndGageStation_Command
{
	
/**
Constructor.
*/
public FillStreamEstimateStation_Command ()
{	super();
	setCommandName ( "FillStreamEstimateStation" );
}

}