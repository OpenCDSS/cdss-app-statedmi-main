package DWR.DMI.StateDMI;

/**
This class initializes and runs the SetCULocationClimateStationWeights() command.
Most functionality is implemented in the base class.
*/
public class SetCULocationClimateStationWeights_Command extends
FillAndSetCULocationClimateStationWeights_Command
{
	
/**
Constructor.
*/
public SetCULocationClimateStationWeights_Command ()
{	super();
	setCommandName ( "SetCULocationClimateStationWeights" );
}

}