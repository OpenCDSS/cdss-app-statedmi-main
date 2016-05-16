package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteCropPatternTSToDateValue() command.
Most functionality is implemented in the base class.
*/
public class WriteCropPatternTSToDateValue_Command extends WriteToDateValue_Command
{
	
/**
Constructor.
*/
public WriteCropPatternTSToDateValue_Command ()
{	super();
	setCommandName ( "WriteCropPatternTSToDateValue" );
}

}