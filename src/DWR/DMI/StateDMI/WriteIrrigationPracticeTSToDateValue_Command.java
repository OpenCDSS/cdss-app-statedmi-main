package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteIrrigationPracticeTSToDateValue() command.
Most functionality is implemented in the base class.
*/
public class WriteIrrigationPracticeTSToDateValue_Command extends WriteToDateValue_Command
{
	
/**
Constructor.
*/
public WriteIrrigationPracticeTSToDateValue_Command ()
{	super();
	setCommandName ( "WriteIrrigationPracticeTSToDateValue" );
}

}