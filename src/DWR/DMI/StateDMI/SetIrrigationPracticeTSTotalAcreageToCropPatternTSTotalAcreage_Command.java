//------------------------------------------------------------------------------
// setIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage_Command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2007-06-14	Steven A. Malers, RTi	Initial version
//------------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Util;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage() command.
</p>
*/
public class SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage_Command 
extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";
	
/**
Constructor.
*/
public SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage_Command ()
{	super();
	setCommandName ( "SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage" );
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
	String ID = parameters.getValue ( "ID" );
	//String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	//String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a CU location ID pattern to process." ) );
	}
	/*
	if (	(IncludeSurfaceWaterSupply != null) &&
			(IncludeSurfaceWaterSupply.length() > 0) &&
			!IncludeSurfaceWaterSupply.equalsIgnoreCase(_True) &&
			!IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) ) {
			warning += "\nIncludeSurfaceWaterSupply must be set to True or False.";
	}
	if (	(IncludeGroundwaterOnlySupply != null) &&
			(IncludeGroundwaterOnlySupply.length() > 0) &&
			!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_True) &&
			!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) ) {
			warning += "\nIncludeGroundwaterOnlySupply must be set to True or False.";
	}
	*/
	if ( (SetStart != null) && (SetStart.length() != 0) && !StringUtil.isInteger(SetStart)) {
		message = "The set start is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the set start as an integer year YYYY." ) );
	}
	
	if ( (SetEnd != null) && (SetEnd.length() != 0) && !StringUtil.isInteger(SetEnd)) {
		message = "The set end is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the set end as an integer year YYYY." ) );
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
		List valid_Vector = new Vector();
	    valid_Vector.add ( "ID" );
		valid_Vector.add ( "SetStart" );
		valid_Vector.add ( "SetEnd" );
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
{	// The command will be modified if changed...
	return (new SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage_JDialog ( parent, this )).ok();
}

/**
Method to execute the fillIrrigationPracticeTSAcreageUsingWellRights() command.
@param command_number Command Number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters ();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	//String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	//String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	/*
	boolean IncludeSurfaceWaterSupply_boolean = true;
	if ( IncludeSurfaceWaterSupply.equalsIgnoreCase("true") ) {
		IncludeSurfaceWaterSupply_boolean = true;
	}
	else if ( IncludeSurfaceWaterSupply.equalsIgnoreCase("false") ) {
		IncludeSurfaceWaterSupply_boolean = false;
	}

	boolean IncludeGroundwaterOnlySupply_boolean = true;
	if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase("true") ) {
		IncludeGroundwaterOnlySupply_boolean = true;
	}
	else if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase("false") ) {
		IncludeGroundwaterOnlySupply_boolean = false;
	}
	*/
	
	// Get the list of irrigation practice time series...
	
	List ipyList = null;
	int ipyListSize = 0;
	try {
		ipyList = (List)processor.getPropContents( "StateCU_IrrigationPracticeTS_List");
		ipyListSize = ipyList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_IrrigationPracticeTS_List from processor (" + e + ").";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of crop pattern time series...
	
	List cdsList = null;
	try {
		cdsList = (List)processor.getPropContents( "StateCU_CropPatternTS_List");
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropPatternTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
		
	// Get the output period (default if fill period is not specified)...

	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	try {
		OutputStart_DateTime = (DateTime)processor.getPropContents("OutputStart");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputStart from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	try {
		OutputEnd_DateTime = (DateTime)processor.getPropContents("OutputEnd");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputEnd from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	DateTime SetStart_DateTime = null;
	if ( (SetStart != null) && StringUtil.isInteger(SetStart) ) {
		SetStart_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
		SetStart_DateTime.setYear ( StringUtil.atoi(SetStart));
	}
	else if ( OutputStart_DateTime != null ) {
		SetStart_DateTime = new DateTime(OutputStart_DateTime);
		SetStart_DateTime.setPrecision ( DateTime.PRECISION_YEAR );
	}
	DateTime SetEnd_DateTime = null;
	if ( (SetEnd != null) && StringUtil.isInteger(SetEnd) ) {
		SetEnd_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
		SetEnd_DateTime.setYear ( StringUtil.atoi(SetEnd));
	}
	else if ( OutputEnd_DateTime != null ) {
		SetEnd_DateTime = new DateTime(OutputEnd_DateTime);
		SetEnd_DateTime.setPrecision ( DateTime.PRECISION_YEAR );
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
	
	int matchCount = 0;
	try {

		// Loop through available irrigation practice time series and find the matching
		// crop pattern time series.
		StateCU_IrrigationPracticeTS ipyts = null;
		StateCU_CropPatternTS cdsts = null;
		String id;	// Location ID
		Message.printStatus ( 2, routine, "Setting irrigation practice time series for " + ipyListSize +
				" locations for requested years " + SetStart_DateTime + " to " +
				SetEnd_DateTime );
		DateTime date = null, date1 = null, date2 = null; // For iteration
		int year;
		for ( int i = 0; i < ipyListSize; i++ ) {
			ipyts =(StateCU_IrrigationPracticeTS)ipyList.get(i);
			id = ipyts.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			int pos = StateCU_Util.indexOf ( cdsList, id );
			if ( pos < 0 ) {
				message = "Could not find crop pattern time series matching \"" + id +
				"\" to determine total acres.  Not setting total acres in irrigation practice time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that crop pattern time series exists for location." ) );
				continue;
			}
			cdsts = (StateCU_CropPatternTS)cdsList.get(pos);
			// Loop based on the set period or if not specified, the period of the IPY time series
			if ( SetStart_DateTime == null ) {
				date1 = ipyts.getDate1();
			}
			else {
				date1 = SetStart_DateTime;
			}
			if ( SetEnd_DateTime == null ) {
				date2 = ipyts.getDate2();
			}
			else {
				date2 = SetEnd_DateTime;
			}
			for ( date = new DateTime(date1); date.lessThanOrEqualTo(date2); date.addYear(1)) {
				// Set the total in the IPY from the CDS.
				year = date.getYear();
				ipyts.setTacre ( year, cdsts.getTotalArea(year) );
			}
			++matchCount;
		}
		
		// If nothing was matched, take further action...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Irrigation practice time series \"" + ID +
				"\" was not matched: warning and not adjusting total acres.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
							"  The time series must be created before setting any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Irrigation practice time series \"" + ID +
				"\" was not matched: failing and not adjusting total acres.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." +
							"  The time series must be created before setting any data." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error irrigation practice time series total acreage (" + e + ").";
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
	/*
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	*/
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	/*
	if ( IncludeSurfaceWaterSupply != null && IncludeSurfaceWaterSupply.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeSurfaceWaterSupply=" + IncludeSurfaceWaterSupply );
	}
	if ( IncludeGroundwaterOnlySupply != null && IncludeGroundwaterOnlySupply.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeGroundwaterOnlySupply=\"" + IncludeGroundwaterOnlySupply + "\"" );
	}
	*/
	if ( SetStart != null && SetStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetStart=" + SetStart );
	}
	if ( SetEnd != null && SetEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEnd=" + SetEnd );
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
