// FillAndSetCULocationClimateStationWeights_Command - This class initializes, checks,
// and runs the FillCULocationClimateStationWeights() and SetCULocationClimateStationWeights() commands.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_Location;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
<p>
This class initializes, checks, and runs the FillCULocationClimateStationWeights() and
SetCULocationClimateStationWeights() commands.  It is extended by the
FillCULocationClimateStationWeights() and SetCULocationClimateStationWeights() commands.
</p>
*/
public abstract class FillAndSetCULocationClimateStationWeights_Command
extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for orographic adjustment parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Data from parsed Weights.
*/
String [] __Weights_stationIDs = null;
double [] __Weights_tempWts = null;
double [] __Weights_precWts = null;
double [] __Weights_ota = null;
double [] __Weights_opa = null;
	
/**
Constructor.
*/
public FillAndSetCULocationClimateStationWeights_Command ()
{	super();
	setCommandName ( "?CULoationStationWeights" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String routine = "FillAndSetCULocationClimateStationWeights_Command.checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String Weights = parameters.getValue ( "Weights" );
	String IncludeOrographicTempAdj = parameters.getValue ( "IncludeOrographicTempAdj" );
	boolean IncludeOrographicTempAdj_boolean = false;
	String IncludeOrographicPrecAdj = parameters.getValue ( "IncludeOrographicPrecAdj" );
	boolean IncludeOrographicPrecAdj_boolean = false;
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "\nAn identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the identifier pattern to match." ) );
	}
	
	if ( (IncludeOrographicTempAdj != null) && (IncludeOrographicTempAdj.length() > 0) &&
			!IncludeOrographicTempAdj.equalsIgnoreCase(_False) &&
			!IncludeOrographicTempAdj.equalsIgnoreCase(_True) ) {
		message = "The IncludeOrographicTempAdj value (" + IncludeOrographicTempAdj + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IncludeOrographicTempAdj as " + _False + 
						" (default if blank), or " + _True + " (default=" + _False + ").") );
	}
	else if ( (IncludeOrographicTempAdj != null) && IncludeOrographicTempAdj.equalsIgnoreCase(_True) ) {
		IncludeOrographicTempAdj_boolean = true;
	}
	
	if ( (IncludeOrographicPrecAdj != null) && (IncludeOrographicPrecAdj.length() > 0) &&
			!IncludeOrographicPrecAdj.equalsIgnoreCase(_False) &&
			!IncludeOrographicPrecAdj.equalsIgnoreCase(_True) ) {
		message = "The IncludeOrographicPrecAdj value (" + IncludeOrographicPrecAdj + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IncludeOrographicTempAdj as " + _False + 
						" (default if blank), or " + _True + " (default=" + _False + ").") );
	}
	else if ( (IncludeOrographicPrecAdj != null) && IncludeOrographicPrecAdj.equalsIgnoreCase(_True) ) {
		IncludeOrographicPrecAdj_boolean = true;
	}
	
	if ( (Weights == null) || (Weights.length() == 0) ) {
		message = "\nStation weights must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE, message,
				"Specify the station weights using StationID,TempWt,PrecWt[,OroTemp][,OroPrec]." ) );
	}
	else {
		// Check the individual values and save for use at run time.
		// Parse the weights into tokens and then split into station ID and weights lists...
		int sizeExpected = 3;	// ID, temp wt, prec wt [, oro temp] [, oro prec]
		if ( IncludeOrographicTempAdj_boolean ) {
			++sizeExpected;
		}
		if ( IncludeOrographicPrecAdj_boolean ) {
			++sizeExpected;
		}
		List<String> tokens = StringUtil.breakStringList ( Weights, ",;", 0 );
		int size = 0; // Number of tokens 
		if ( tokens == null ) {
			message = "Weights parameter (" + Weights + ") is not properly defined.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE, message,
					"Specify the station weights using StationID,TempWt,PrecWt[,OroTemp][,OroPrec]." ) );
			// Set size to zero to skip next checks
			size = 0;
		}
		else {
			size = tokens.size();
		}
		boolean sizeOk = true;
		if ( (size%sizeExpected) != 0 ) {
			sizeOk = false;
			if ( IncludeOrographicTempAdj_boolean && IncludeOrographicPrecAdj_boolean ) {
				message = "Weights " + Weights +
				" is not sets of " + sizeExpected + " for groups of StationID,TempWt,PrecWt,OroTemp,OroPrec,...";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Specify the station weights using StationID,TempWt,PrecWt,OroTemp,OroPrec,..." ) );
			}
			else if ( IncludeOrographicTempAdj_boolean ) {
				message = "Weights " + Weights +
				" is not sets of " + sizeExpected + " for groups of StationID,TempWt,PrecWt,OroTemp,...";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Specify the station weights using StationID,TempWt,PrecWt,OroTemp,..." ) );
			}
			else if ( IncludeOrographicPrecAdj_boolean ) {
				message = "Weights " + Weights +
				" is not sets of " + sizeExpected + " for groups of StationID,TempWt,PrecWt,OroPrec,...";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Specify the station weights using StationID,TempWt,PrecWt,OroPrec,..." ) );		
			}
			else {
				// Just normal weights
				message = "Weights " + Weights +
				" is not sets of " + sizeExpected + " for groups of StationID,TempWt,PrecWt,...";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Specify the station weights using StationID,TempWt,PrecWt,..." ) );	
			}
		}
		if ( sizeOk ) {
			// Check the individual values.
			__Weights_stationIDs = new String[size/sizeExpected];
			__Weights_tempWts = new double[size/sizeExpected];
			__Weights_precWts = new double[size/sizeExpected];
			__Weights_ota = new double[size/sizeExpected];
			__Weights_opa = new double[size/sizeExpected];
			// Loop through all the tokens...
			for ( int i = 0; i < size; i++ ) {
				if ( i%sizeExpected == 0 ) {
					// Station ids....
					__Weights_stationIDs[i/sizeExpected] = tokens.get(i);
				}
				else if ( (i)%sizeExpected == 1 ) {
					// Temperature weights...
					if ( !StringUtil.isDouble((String)tokens.get(i) ) ) {
						message = "Weights (" + Weights +
						") temperature weight value " + tokens.get(i) + " is not a number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE, message,
								"Specify the temperature station weights as a number" ) );	
					}
					__Weights_tempWts[i/sizeExpected] = StringUtil.atod( tokens.get(i) );
				}
				else if ( (i)%sizeExpected == 2 ) {
					// Precipitation weights...
					if ( !StringUtil.isDouble(tokens.get(i) ) ) {
						message = "Weights (" + Weights +
						") precipitation weight value " + tokens.get(i) + " is not a number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE, message,
								"Specify the precipitation station weights as a number" ) );	
					}
					__Weights_precWts[i/sizeExpected] = StringUtil.atod(tokens.get(i) );
				}
				else if ( ((i)%sizeExpected == 3) && IncludeOrographicTempAdj_boolean ) {
					// Orographic temperature weights...
					if ( !StringUtil.isDouble((String)tokens.get(i) ) ) {
						message = "Weights " + Weights +
						" orographic temperature adjustment value " + tokens.get(i) +
						" is not a number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE, message,
								"Specify the orographic temperature adjustment value as a number" ) );	
					}
					__Weights_ota[i/sizeExpected] = StringUtil.atod( tokens.get(i) );
				}
				else if ( (((i)%sizeExpected == 3) && !IncludeOrographicTempAdj_boolean) ||
					(((i)%sizeExpected == 4) && IncludeOrographicTempAdj_boolean &&
					IncludeOrographicPrecAdj_boolean) ) {
					// Orographic precipitation weights...
					if ( !StringUtil.isDouble((String)tokens.get(i) ) ) {
						message = "Weights " + Weights +
						" orographic precipitation adjustment value " + tokens.get(i) +
						" is not a number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE, message,
								"Specify the orographic precipitation adjustment value as a number" ) );	
					}
					__Weights_opa[i/sizeExpected] = StringUtil.atod(tokens.get(i) );
				}
			}
		}
	}

	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
						", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(5);
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "IncludeOrographicTempAdj" );
    valid_Vector.add ( "IncludeOrographicPrecAdj" );
	valid_Vector.add ( "Weights" );
	valid_Vector.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
		throw new InvalidCommandParameterException ( warning );
	}
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed (true indicates fill)
	return (new FillAndSetCULocationClimateStationWeights_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Run the command command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	String action = "Filling ";
	if ( this instanceof SetCULocationClimateStationWeights_Command ) {
		action = "Setting ";
	}
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	boolean IncludeOrographicTempAdj_boolean = false;
	String IncludeOrographicTempAdj = parameters.getValue ( "IncludeOrographicTempAdj" );
	if ( (IncludeOrographicTempAdj != null) && IncludeOrographicTempAdj.equalsIgnoreCase(_True) ) {
		IncludeOrographicTempAdj_boolean = true;
	}
	boolean IncludeOrographicPrecAdj_boolean = false;
	String IncludeOrographicPrecAdj = parameters.getValue ( "IncludeOrographicPrecAdj" );
	if ( (IncludeOrographicPrecAdj != null) && IncludeOrographicPrecAdj.equalsIgnoreCase(_True) ) {
		IncludeOrographicPrecAdj_boolean = true;
	}
	// Weights already parsed in checkCommandParameters
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of climate stations...
	
	List<StateCU_Location> culocList = null;
	int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		// Loop through the CU locations that are in memory.  For each one,
		// search the table and add climate station information to the locations

		StateCU_Location culoc;
		String culoc_id;
		int matchCount = 0;
		for (int i = 0; i < culocListSize; i++) {
			culoc = (StateCU_Location)culocList.get(i);
			culoc_id = culoc.getID();
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			if ( this instanceof FillCULocationClimateStationWeights_Command ) {
				// If the location already has stations defined, skip it...
				if ( culoc.getNumClimateStations() > 0 ) {
					continue;
				}
			}
			else {
				// Setting the data.  Zero out what is defined for the location...
				culoc.setNumClimateStations ( 0 );
			}
			// Now assign the new data...
			for ( int ista = 0; ista < __Weights_stationIDs.length; ista++ ) {
				culoc.setClimateStationID ( __Weights_stationIDs[ista], ista );
				culoc.setTemperatureStationWeight(__Weights_tempWts[ista], ista );
				culoc.setPrecipitationStationWeight(__Weights_precWts[ista],ista);
				if ( IncludeOrographicTempAdj_boolean ) {
					culoc.setOrographicTemperatureAdjustment(__Weights_ota[ista], ista);
				}
				if ( IncludeOrographicPrecAdj_boolean ) {
					culoc.setOrographicPrecipitationAdjustment(__Weights_opa[ista], ista);
				}
				if ( IncludeOrographicTempAdj_boolean && IncludeOrographicPrecAdj_boolean ) {
					Message.printStatus ( 2, routine, action + culoc_id +
						" climate station -> " + __Weights_stationIDs[ista] +
						" tempwt=" + __Weights_tempWts[ista] +
						" precwt=" + __Weights_precWts[ista] +
						" ota=" + __Weights_ota[ista] +
						" opa=" + __Weights_opa[ista] );
				}
				else if ( IncludeOrographicTempAdj_boolean ) {
					Message.printStatus ( 2, routine, action + culoc_id +
						" climate station -> " + __Weights_stationIDs[ista] +
						" tempwt=" + __Weights_tempWts[ista] +
						" precwt=" + __Weights_precWts[ista] +
						" ota=" + __Weights_ota[ista] );
				}
				else if ( IncludeOrographicPrecAdj_boolean ) {
					Message.printStatus ( 2, routine, action + culoc_id +
						" climate station -> " + __Weights_stationIDs[ista] +
						" tempwt=" + __Weights_tempWts[ista] +
						" precwt=" + __Weights_precWts[ista] +
						" opa=" + __Weights_opa[ista] );
				}
				else {
					Message.printStatus ( 2, routine, action + culoc_id +
					" climate station -> " + __Weights_stationIDs[ista] +
					" tempwt=" + __Weights_tempWts[ista] +
					" precwt=" + __Weights_precWts[ista] );
				}
			}
			++matchCount;
		}

		// If nothing was matched, perform other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "CU location \"" + ID + "\" was not matched: warning and not " +
				action.toLowerCase() + ".";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "CU location \"" + ID + "\" was not matched: failing and not " +
				action.toLowerCase() + ".";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error " + action.toLowerCase() + " CU location data (" + e + ").";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
        throw new CommandWarningException ( message );
    }
	
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	
	String ID = parameters.getValue ( "ID" );
	String IncludeOrographicTempAdj = parameters.getValue ( "IncludeOrographicTempAdj" );
	String IncludeOrographicPrecAdj = parameters.getValue ( "IncludeOrographicPrecAdj" );
	String Weights = parameters.getValue ( "Weights" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( IncludeOrographicTempAdj != null && IncludeOrographicTempAdj.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeOrographicTempAdj=" + IncludeOrographicTempAdj );
	}
	if ( IncludeOrographicPrecAdj != null && IncludeOrographicPrecAdj.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeOrographicPrecAdj=" + IncludeOrographicPrecAdj );
	}
	if ( Weights != null && Weights.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Weights=\"" + Weights + "\"" );
	}
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
