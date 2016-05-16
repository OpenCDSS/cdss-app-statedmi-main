package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_PenmanMonteith;

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
This class initializes, checks, and runs the SetPenmanMonteith() command.
</p>
*/
public class SetPenmanMonteith_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Coefficients as array, parsed on checkCommandParameters().
*/
private double[] __Coefficients_double = null;
	
/**
Constructor.
*/
public SetPenmanMonteith_Command ()
{	super();
	setCommandName ( "SetPenmanMonteith" );
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
	
	__Coefficients_double = null;
	if ( (Coefficients == null) || (Coefficients.length() == 0) ) {
		message = "The coefficients must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the coefficients as a sequence of comma-separated numbers: " +
					"percent,coeff,percent,coeff." ) );
	}
	else {
		// Verify that the correct number of values are specified
		List<String> tokens = StringUtil.breakStringList ( Coefficients, ", ", StringUtil.DELIM_SKIP_BLANKS );
		int nvals = StateCU_PenmanMonteith.getNGrowthStagesFromCropName(CropType)*
			StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
		if ( (tokens == null) || (tokens.size() != nvals) ) {
			message = "Crop type " + CropType + " requires " + nvals + " coefficients.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the coefficients as " + nvals + " numbers separated by commas." ) );
		}
		else {
			__Coefficients_double = new double[nvals];
			for ( int i = 0; i < nvals; i++ ) {
				String val = tokens.get(i);
				if ( !StringUtil.isDouble(val) ) {
					message = "Coefficient value (" + val + ") is not a valid number.";
					warning += "\n" + message;
					status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the coefficients as " + nvals + " numbers separated by numbers." ) );
				}
				else {
					__Coefficients_double[i] = Double.parseDouble(val);
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
	return (new SetPenmanMonteith_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setPenmanMonteith() command.
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
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of Penman-Monteith objects...
	
	List<StateCU_PenmanMonteith> kpmList = null;
	try {
		kpmList = (List<StateCU_PenmanMonteith>)processor.getPropContents( "StateCU_PenmanMonteith_List");
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_PenmanMonteith_List from processor.";
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
		String cropName;
		int matchCount = 0;
		for ( StateCU_PenmanMonteith kpm: kpmList) {
			cropName = kpm.getName();
			if ( !cropName.matches(CropType_pattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			int ncpgs = StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
			// The curve values must be defined and have been verified above...
			for ( int j = 0; j < __Coefficients_double.length; j++ ) {
				int igs = j/ncpgs;
				kpm.setCurveValue ( igs, (j - igs*ncpgs), __Coefficients_double[j] );
			}
		}

		// If nothing was matched and the crop_name_pattern does not contain a
		// wildcard, add a StateCU_PenmanMonteith at the end...

		if ( (matchCount == 0) && (CropType.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase(_Add) ) {
			cropName = CropType;
			StateCU_PenmanMonteith kpm = new StateCU_PenmanMonteith (
				StateCU_PenmanMonteith.getNGrowthStagesFromCropName(cropName) );
			kpm.setName ( cropName );
			int ncpgs = StateCU_PenmanMonteith.getNCoefficientsPerGrowthStage();
			Message.printStatus ( 2, routine, "Adding Penman-Monteith crop coefficients for " + cropName );
			Message.printStatus ( 2, routine, "Setting " + cropName + " curve values." );
			// The curve values must be defined and have been verified above...
			for ( int j = 0; j < __Coefficients_double.length; j++ ) {
				int igs = j/ncpgs;
				kpm.setCurveValue ( igs, (j - igs*ncpgs), __Coefficients_double[j] );
			}
			kpmList.add ( kpm );
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
        message = "Unexpected error setting Penman-Monteith coefficient data (" + e + ").";
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
	String Coefficients = parameters.getValue ( "Coefficients" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();

	if ( (CropType != null) && CropType.length() > 0 ) {
		b.append ( "CropType=\"" + CropType + "\"" );
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