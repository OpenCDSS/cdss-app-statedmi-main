package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetInstreamFlowStation() command.
</p>
*/
public class SetInstreamFlowStation_Command extends FillAndSetInstreamFlowStation_Command
{
	
/**
Constructor.
*/
public SetInstreamFlowStation_Command ()
{	super();
	setCommandName ( "SetInstreamFlowStation" );
}

}