package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetStreamEstimateStation() command.
</p>
*/
public class SetStreamEstimateStation_Command extends FillAndSetStreamEstimateAndGageStation_Command
{
	
/**
Constructor.
*/
public SetStreamEstimateStation_Command ()
{	super();
	setCommandName ( "SetStreamEstimateStation" );
}

}