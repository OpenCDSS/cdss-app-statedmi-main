package DWR.DMI.StateDMI;

/**
This class initializes and runs the FillCULocationClimateStationWeights() command.
Most functionality is implemented in the base class.
*/
public class FillCULocationClimateStationWeights_Command extends
FillAndSetCULocationClimateStationWeights_Command
{
	
/**
Constructor.
*/
public FillCULocationClimateStationWeights_Command ()
{	super();
	setCommandName ( "FillCULocationClimateStationWeights" );
}

}