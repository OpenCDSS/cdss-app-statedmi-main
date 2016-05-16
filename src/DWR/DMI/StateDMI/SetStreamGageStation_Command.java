package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetStreamGageStation() command.
</p>
*/
public class SetStreamGageStation_Command extends FillAndSetStreamEstimateAndGageStation_Command
{
	
/**
Constructor.
*/
public SetStreamGageStation_Command ()
{	super();
	setCommandName ( "SetStreamGageStation" );
}

}