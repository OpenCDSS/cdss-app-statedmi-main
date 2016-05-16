package DWR.DMI.StateDMI;

import DWR.StateMod.StateMod_DataSet;

import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;

/**
<p>
This class initializes, checks, and runs the ReadRiverNetworkFromStateMod() command.
</p>
*/
public class ReadRiverNetworkFromStateMod_Command extends ReadFromStateMod_Command
{
	
/**
Constructor.
*/
public ReadRiverNetworkFromStateMod_Command ()
{	super();
	setCommandName ( "ReadRiverNetworkFromStateMod" );
}

}