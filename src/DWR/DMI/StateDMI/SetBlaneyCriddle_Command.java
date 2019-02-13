// SetBlaneyCriddle_Command - This class initializes, checks, and runs the SetBlaneyCriddle() command.

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

import DWR.StateCU.StateCU_BlaneyCriddle;

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
This class initializes, checks, and runs the SetBlaneyCriddle() command.
</p>
*/
public class SetBlaneyCriddle_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Flags for CurveType parameter:
*/
protected final String _CurveType_DAY = "Day - Perennial Crop";
protected final String _CurveType_DAY_short = "Day";
protected final String _CurveType_PERCENT = "Percent - Annual Crop";
protected final String _CurveType_PERCENT_short = "Percent";

/**
Flags for the BlaneyCriddleMethod parameter:
*/
protected final String _BLANEY_CRIDDLE_METHOD_0 = "0 - SCS Modified Blaney-Criddle";
protected final String _BLANEY_CRIDDLE_METHOD_1 = "1 - Original Blaney-Criddle";
protected final String _BLANEY_CRIDDLE_METHOD_2 = "2 - Modified Blaney-Criddle with elevation adjustment";
protected final String _BLANEY_CRIDDLE_METHOD_3 = "3 - Original Blaney-Criddle with elevation adjustment";
protected final String _BLANEY_CRIDDLE_METHOD_4 = "4 - Pochop Method";

/**
Coefficients as array, parsed on checkCommandParameters().
*/
private double[] __Coefficients_double = null;
	
/**
Constructor.
*/
public SetBlaneyCriddle_Command ()
{	super();
	setCommandName ( "SetBlaneyCriddle" );
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
{	String routine = getClass().getName() + ".checkCommandParameters";
	String CropType = parameters.getValue ( "CropType" );
	String CurveType = parameters.getValue ( "CurveType" );
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
	String Coefficients = parameters.getValue ( "Coefficients" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (CropType == null) || (CropType.length() == 0) ) {
		message = "A crop type or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the crop type to match." ) );
	}
	
	if ( (CurveType == null) || (CurveType.length() == 0) ) {
		message = "A curve type must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the curve type as " + _CurveType_DAY_short + " or " +
				_CurveType_PERCENT_short + "." ) );
	}
	else if ( !CurveType.equalsIgnoreCase(_CurveType_DAY_short) &&
		!CurveType.equalsIgnoreCase(_CurveType_PERCENT_short) ) {
		message = "The curve type (" + CurveType + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the curve type as " + _CurveType_DAY_short + " or " +
				_CurveType_PERCENT_short + "." ) );
		
	}
	
	if ( (BlaneyCriddleMethod == null) || (BlaneyCriddleMethod.length() == 0) ) {
		message = "The Blaney-Criddle method must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the curve type as " + _CurveType_DAY + " or " + _CurveType_PERCENT + "." ) );
	}
	else if ( !StringUtil.isInteger(BlaneyCriddleMethod) ) {
		message = "The Blaney-Criddle method (" + BlaneyCriddleMethod + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the Blaney-Criddle method as an integer 0-4 (see StateCU documentation)." ) );
		
	}
	
	__Coefficients_double = null;
	if ( (Coefficients == null) || (Coefficients.length() == 0) ) {
		message = "The coefficients must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the coefficients as a sequence of comma-separated numbers." ) );
	}
	else {
		if ( (CurveType != null) && CurveType.equalsIgnoreCase(_CurveType_DAY_short) ) {
			// Verify that 25 values are specified
			List tokens = StringUtil.breakStringList ( Coefficients, ", ", StringUtil.DELIM_SKIP_BLANKS );
			if ( (tokens == null) || tokens.size() != 25) {
				message = "Curve type " + _CurveType_DAY_short + " requires 25 numbers.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the coefficients as 25 numbers separated by commas." ) );
			}
			else {
				__Coefficients_double = new double[25];
				for ( int i = 0; i < 25; i++ ) {
					String val = (String)tokens.get(i);
					if ( !StringUtil.isDouble(val) ) {
						message = "Coefficient value (" + val + ") is not a valid number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Specify the coefficients as 25 numbers separated by numbers." ) );
					}
					else {
						__Coefficients_double[i] = Double.parseDouble(val);
					}
				}
			}
		}
		else if ( (CurveType != null) && CurveType.equalsIgnoreCase(_CurveType_PERCENT_short) ) {
			// Verify that 21 values are specified
			List tokens = StringUtil.breakStringList ( Coefficients, ", ", StringUtil.DELIM_SKIP_BLANKS );
			if ( (tokens == null) || tokens.size() != 21) {
				message = "Curve type " + _CurveType_PERCENT_short + " requires 21 numbers.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the coefficients as 21 numbers separated by commas." ) );
			}
			else {
				__Coefficients_double = new double[21];
				for ( int i = 0; i < 21; i++ ) {
					String val = (String)tokens.get(i);
					if ( !StringUtil.isDouble(val) ) {
						message = "Coefficient value (" + val + ") is not a valid number.";
						warning += "\n" + message;
						status.addToLog ( CommandPhaseType.INITIALIZATION,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Specify the coefficients as 21 numbers separated by numbers." ) );
					}
					else {
						__Coefficients_double[i] = Double.parseDouble(val);
					}
				}
			}
		}
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Add) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "CropType" );
	valid_Vector.add ( "CurveType" );
    valid_Vector.add ( "BlaneyCriddleMethod" );
	valid_Vector.add ( "Coefficients" );
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
{	
	return (new SetBlaneyCriddle_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
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
	
	PropList parameters = getCommandParameters();
	String CropType = parameters.getValue ( "CropType" );
	if ( CropType == null ) {
		CropType = "*"; // Default
	}
	String CropType_pattern_Java = StringUtil.replaceString(CropType,"*",".*");
	String CurveType = parameters.getValue ( "CurveType" );
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of Blaney-Criddle objects...
	
	List kbcList = null;
	int kbcListSize = 0;
	try {
		kbcList = (List)processor.getPropContents( "StateCU_BlaneyCriddle_List");
		kbcListSize = kbcList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_BlaneyCriddle_List from processor.";
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
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		boolean fill_curve_type = false;
		if ( (CurveType != null) && !CurveType.equals("")) {
			fill_curve_type = true;
			// Curve type is verified above.
		}

		boolean fill_BlaneyCriddleMethod = false;
		int bcm_value = 0;
		if (BlaneyCriddleMethod != null && !BlaneyCriddleMethod.equals("")) {
			bcm_value = Integer.parseInt(BlaneyCriddleMethod);
			if ( bcm_value < 5 && bcm_value > -1 ) {
				fill_BlaneyCriddleMethod = true;
			}
		}

		StateCU_BlaneyCriddle cubc = null;
		String crop_name;
		int matchCount = 0;
		for (int i = 0; i < kbcListSize; i++) {
			cubc = (StateCU_BlaneyCriddle)kbcList.get(i);
			crop_name = cubc.getName();
			if ( !crop_name.matches(CropType_pattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			if ( fill_curve_type ) {
				Message.printStatus ( 2, routine, "Setting " + crop_name + " curve type -> "+ CurveType);
				// Don't allow editing if parts so create a new curve...
				cubc = new StateCU_BlaneyCriddle ( CurveType );
				cubc.setName ( crop_name );
				// The curve values must be defined and have been verified above...
				for ( int j = 0; j < __Coefficients_double.length; j++ ) {
					cubc.setCurveValue ( j,	__Coefficients_double[j] );
				}
				//__CUBlaneyCriddle_Vector.setget ( cubc, i );
			}

			if ( fill_BlaneyCriddleMethod ) {
				Message.printStatus ( 2, routine,
				"Setting " + crop_name + " Blaney-CriddleMethod -> " + BlaneyCriddleMethod);
				cubc.setKtsw ( bcm_value );
			}
			kbcList.set ( i, cubc );
		}

		// If nothing was matched and the crop_name_pattern does not contain a
		// wildcard, add a StateCU_BlaneyCriddle at the end...

		if ( (matchCount == 0) && (CropType.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase(_Add) ) {
			crop_name = CropType;
			cubc = new StateCU_BlaneyCriddle ( CurveType );
			cubc.setName ( crop_name );
			Message.printStatus ( 2, routine, "Adding Blaney-Criddle crop coefficients for " + crop_name );
			kbcList.add ( cubc );
			// Indent is the same as above to simplify code maintenance...
			if ( fill_curve_type ) {
				Message.printStatus ( 2, routine, "Setting " + crop_name + " curve type -> "+ CurveType);
				// The curve values must be defined and have been verified above...
				for ( int j = 0; j < __Coefficients_double.length; j++ ) {
					cubc.setCurveValue ( j, __Coefficients_double[j] );
				}
			}
			if ( fill_BlaneyCriddleMethod ) {
				Message.printStatus ( 2, routine,
				"Setting " + crop_name + " Blaney-Criddle Method -> " + BlaneyCriddleMethod );
				cubc.setKtsw( bcm_value );
			}
			// Increment so warnings are not shown below
			++matchCount;
		}
		// If nothing was matched, perform other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Crop type \"" + CropType + "\" was not matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Crop type \"" + CropType + "\" was not matched: failing and not setting.";
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
        message = "Unexpected error setting Blaney-Criddle coefficient data (" + e + ").";
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
	
	String CropType = parameters.getValue ( "CropType" );
	String CurveType = parameters.getValue ( "CurveType" );
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
	String Coefficients = parameters.getValue ( "Coefficients" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();

	if ( (CropType != null) && CropType.length() > 0 ) {
		b.append ( "CropType=\"" + CropType + "\"" );
	}
	if ( (CurveType != null) && CurveType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CurveType=" + CurveType );
	}
	if ( (BlaneyCriddleMethod != null) && BlaneyCriddleMethod.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append( "," );
		}
		b.append ( "BlaneyCriddleMethod=" + BlaneyCriddleMethod);
	}
	if ( (Coefficients != null) && Coefficients.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Coefficients=\"" + Coefficients + "\"" );
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
