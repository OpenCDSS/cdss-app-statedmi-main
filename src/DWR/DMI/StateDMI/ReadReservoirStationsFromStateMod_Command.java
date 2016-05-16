package DWR.DMI.StateDMI;

import DWR.StateMod.StateMod_DataSet;

import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;

/**
<p>
This class initializes, checks, and runs the ReadReservoirStationsFromStateMod() command.
</p>
*/
public class ReadReservoirStationsFromStateMod_Command extends ReadFromStateMod_Command
{
	
/**
Constructor.
*/
public ReadReservoirStationsFromStateMod_Command ()
{	super();
	setCommandName ( "ReadReservoirStationsFromStateMod" );
}

}