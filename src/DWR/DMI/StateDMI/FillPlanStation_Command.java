package DWR.DMI.StateDMI;

/**
This class initializes, checks, and runs the FillPlanStation() command.
*/
public class FillPlanStation_Command extends FillAndSetPlanStation_Command
{

/**
Constructor.
*/
public FillPlanStation_Command ()
{	super();
	setCommandName ( "FillPlanStation" );
}

}