package DWR.DMI.StateDMI;

/**
<p>
This class initializes, checks, and runs the ReadWellHistoricalPumpingTSMonthlyFromStateCU() command.
It is equivalent to ReadWellHistoricalPumpingTSMonthlyFromStateMod().
</p>
*/
public class ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command extends ReadFromStateMod_Command
{
	
/**
Constructor.
*/
public ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command ()
{	super();
	setCommandName ( "ReadWellHistoricalPumpingTSMonthlyFromStateCU" );
}

}