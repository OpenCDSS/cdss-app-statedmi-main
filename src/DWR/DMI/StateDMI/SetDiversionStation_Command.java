package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetDiversionStation() command.
</p>
*/
public class SetDiversionStation_Command extends FillAndSetDiversionAndWellStation_Command
{
	
/**
Constructor.
*/
public SetDiversionStation_Command ()
{	super();
	setCommandName ( "SetDiversionStation" );
}

}