package DWR.DMI.StateDMI;

/**
This class initializes, checks, and runs the SetPlanStation() command.
*/
public class SetPlanStation_Command extends FillAndSetPlanStation_Command
{

/**
Constructor.
*/
public SetPlanStation_Command ()
{	super();
	setCommandName ( "SetPlanStation" );
}

}