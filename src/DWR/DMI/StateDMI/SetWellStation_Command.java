package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the SetWellStation() command.
</p>
*/
public class SetWellStation_Command extends FillAndSetDiversionAndWellStation_Command
{
	
/**
Constructor.
*/
public SetWellStation_Command ()
{	super();
	setCommandName ( "SetWellStation" );
}

}