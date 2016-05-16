package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
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
This class initializes, checks, and runs the SetIrrigationPracticeTS() command.
</p>
*/
public class SetIrrigationPracticeTS_Command extends AbstractCommand implements Command
{
	
protected final String _True = "True";
protected final String _False = "False";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";
	
/**
Constructor.
*/
public SetIrrigationPracticeTS_Command ()
{	super();
	setCommandName ( "SetIrrigationPracticeTS" );
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
{	String routine = "SetIrrigationPracticeTS_Command.checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String SurfaceDelEffMax = parameters.getValue ("SurfaceDelEffMax");
	String FloodAppEffMax = parameters.getValue ("FloodAppEffMax");
	String SprinklerAppEffMax = parameters.getValue ("SprinklerAppEffMax");
	String AcresSWFlood = parameters.getValue ( "AcresSWFlood" );
	String AcresSWSprinkler = parameters.getValue ( "AcresSWSprinkler");
	String AcresGWFlood = parameters.getValue ( "AcresGWFlood");
	String AcresGWSprinkler = parameters.getValue ( "AcresGWSprinkler");
	String PumpingMax = parameters.getValue ("PumpingMax");
	String GWMode = parameters.getValue ( "GWMode" );
	String AcresTotal = parameters.getValue ( "AcresTotal");
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the identifier pattern to match." ) );
	}
	
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

	if ( (SurfaceDelEffMax != null) && !StringUtil.isDouble(SurfaceDelEffMax) ) {
		message = "The surface delivery efficiency maximum (" + SurfaceDelEffMax + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the surface delivery efficiency maximum as a number.") );
	}
	
	if ( (FloodAppEffMax != null) && !StringUtil.isDouble(FloodAppEffMax) ) {
		message = "The flood application efficiency maximum (" + FloodAppEffMax + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the flood application efficiency maximum as a number.") );
	}
	
	if ( (SprinklerAppEffMax != null) && !StringUtil.isDouble(SprinklerAppEffMax) ) {
		message = "The sprinkler application efficiency maximum (" + SprinklerAppEffMax + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the sprinkler application efficiency maximum as a number.") );
	}
	
	if ( (AcresSWFlood != null) && !StringUtil.isDouble(AcresSWFlood) ) {
		message = "The acres irrigated by surface water flood (" + AcresSWFlood + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify acres irrigated by surface water flood as a number.") );
	}
	
	if ( (AcresSWSprinkler != null) && !StringUtil.isDouble(AcresSWSprinkler) ) {
		message = "The acres irrigated by surface water sprinkler (" + AcresSWSprinkler + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the acres irrigated by surface water sprinkler as a number.") );
	}
	
	if ( (AcresGWFlood != null) && !StringUtil.isDouble(AcresGWFlood) ) {
		message = "The acres irrigated by ground water flood (" + AcresGWFlood + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify acres irrigated by ground water flood as a number.") );
	}
	
	if ( (AcresGWSprinkler != null) && !StringUtil.isDouble(AcresGWSprinkler) ) {
		message = "The acres irrigated by ground water sprinkler (" + AcresGWSprinkler + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the acres irrigated by ground water sprinkler as a number.") );
	}
	
	if ( (PumpingMax != null) && !StringUtil.isDouble(PumpingMax) ) {
		message = "The pumping maximum (" + PumpingMax + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify pumping maximum as a number.") );
	}
	
	if ( (GWMode != null) && !StringUtil.isDouble(GWMode) ) {
		message = "The groundwater mode (" + GWMode + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the groundwater mode as a number.") );
	}
	
	if ( (AcresTotal != null) && !StringUtil.isDouble(AcresTotal) ) {
		message = "The acres total (" + AcresTotal + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the acres total as a number.") );
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
	valid_Vector.add ( "SurfaceDelEffMax" );
	valid_Vector.add ( "FloodAppEffMax" );
	valid_Vector.add ( "SprinklerAppEffMax" );
	valid_Vector.add ( "AcresSWFlood" );
	valid_Vector.add ( "AcresSWSprinkler" );
	valid_Vector.add ( "AcresGWFlood" );
	valid_Vector.add ( "AcresGWSprinkler" );
	valid_Vector.add ( "PumpingMax" );
	valid_Vector.add ( "GWMode" );
	valid_Vector.add ( "AcresTotal" );
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
	return (new SetIrrigationPracticeTS_JDialog ( parent, this )).ok();
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
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String SetStart = parameters.getValue ( "SetStart" );
	int SetStart_int = -1;
	if ( StringUtil.isInteger(SetStart)) {
		SetStart_int = StringUtil.atoi(SetStart);
	}
	String SetEnd = parameters.getValue ( "SetEnd" );
	int SetEnd_int = -1;
	if ( StringUtil.isInteger(SetEnd)) {
		SetEnd_int = StringUtil.atoi(SetEnd);
	}
	String SurfaceDelEffMax = parameters.getValue ( "SurfaceDelEffMax" );
	String FloodAppEffMax = parameters.getValue ( "FloodAppEffMax" );
	String SprinklerAppEffMax = parameters.getValue ( "SprinklerAppEffMax" );
	String AcresSWFlood = parameters.getValue ( "AcresSWFlood" );
	String AcresSWSprinkler = parameters.getValue ( "AcresSWSprinkler" );
	String AcresGWFlood = parameters.getValue ( "AcresGWFlood" );
	String AcresGWSprinkler = parameters.getValue ( "AcresGWSprinkler" );
	String PumpingMax = parameters.getValue ( "PumpingMax" );
	String GWMode = parameters.getValue ( "GWMode" );
	String AcresTotal = parameters.getValue ( "AcresTotal" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
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
	
	// Get the CU locations to check groundwater supply
	
	List culocList = null;
	int culocListSize = 0;
	try {
		culocList = (List)processor.getPropContents( "StateCU_Location_List");
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor (" + e + ").";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( culocListSize == 0 ) {
		message = "CU location list is empty - will not be able to check for groundwater supply.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Read CU locations and set aggregate information before this command." ) );
	}
	
	// Get the output period

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
	
	// Set start and end are specified by user or come from output period
	if ( (SetStart_int < 0) && (OutputStart_DateTime != null) ) {
		SetStart_int = OutputStart_DateTime.getYear();
	}
	if ( (SetEnd_int < 0) && (OutputEnd_DateTime != null) ) {
		SetEnd_int = OutputEnd_DateTime.getYear();
	}
	
	// Make sure that the set start/end are not still missing
	
	if ( SetStart_int < 0 ) {
		message = "Set start (and output period) has not been specified - cannot set irrigation practice time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set start." ) );
	}
	if ( SetEnd_int < 0 ) {
		message = "Set end (and output period) has not been specified - cannot set irrigation practice time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set end." ) );
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
		boolean fill_SurfaceDelEffMax = false;
		double SurfaceDelEffMax_double = 0.0;
		if ( (SurfaceDelEffMax != null) && (SurfaceDelEffMax.length() > 0) ) {
			fill_SurfaceDelEffMax = true;
			SurfaceDelEffMax_double = Double.parseDouble ( SurfaceDelEffMax );
		}

		boolean fill_FloodAppEffMax = false;
		double FloodAppEffMax_double = 0.0;
		if ( (FloodAppEffMax != null) && (FloodAppEffMax.length() > 0) ) {
			fill_FloodAppEffMax = true;
			FloodAppEffMax_double = Double.parseDouble ( FloodAppEffMax );
		}

		boolean fill_SprinklerAppEffMax = false;
		double SprinklerAppEffMax_double = 0.0;
		if ( (SprinklerAppEffMax != null) && (SprinklerAppEffMax.length() > 0)){
			fill_SprinklerAppEffMax = true;
			SprinklerAppEffMax_double = Double.parseDouble ( SprinklerAppEffMax );
		}

		boolean fill_AcresSWFlood = false;
		double AcresSWFlood_double = -1.0;
		if ( (AcresSWFlood != null) && (AcresSWFlood.length() > 0) ) {
			fill_AcresSWFlood = true;
			AcresSWFlood_double = Double.parseDouble ( AcresSWFlood );
		}
		
		boolean fill_AcresSWSprinkler = false;
		double AcresSWSprinkler_double = -1.0;
		if ( (AcresSWSprinkler != null) && (AcresSWSprinkler.length() > 0) ) {
			fill_AcresSWSprinkler = true;
			AcresSWSprinkler_double = Double.parseDouble ( AcresSWSprinkler );
		}
		
		boolean fill_AcresGWFlood = false;
		double AcresGWFlood_double = -1.0;
		if ( (AcresGWFlood != null) && (AcresGWFlood.length() > 0) ) {
			fill_AcresGWFlood = true;
			AcresGWFlood_double = Double.parseDouble ( AcresGWFlood );
		}
		
		boolean fill_AcresGWSprinkler = false;
		double AcresGWSprinkler_double = -1.0;
		if ( (AcresGWSprinkler != null) && (AcresGWSprinkler.length() > 0) ) {
			fill_AcresGWSprinkler = true;
			AcresGWSprinkler_double = Double.parseDouble ( AcresGWSprinkler );
		}
		
		/* TODO SAM 2007-09-12 Remove if not used
		boolean fill_AcresGW = false;
		double AcresGW_double = 0.0;
		if ( (AcresGW != null) && (AcresGW.length() > 0) ) {
			fill_AcresGW = true;
			AcresGW_double = Double.parseDouble ( AcresGW );
		}

		boolean fill_AcresSprinkler = false;
		double AcresSprinkler_double = 0.0;
		if ( (AcresSprinkler != null) && (AcresSprinkler.length() > 0) ) {
			fill_AcresSprinkler = true;
			AcresSprinkler_double = Double.parseDouble ( AcresSprinkler );
		}
		*/

		boolean fill_PumpingMax = false;
		double PumpingMax_double = 0.0;
		if ( (PumpingMax != null) && (PumpingMax.length() > 0) ) {
			fill_PumpingMax = true;
			PumpingMax_double = Double.parseDouble ( PumpingMax );
		}

		boolean fill_GWMode = false;
		double GWMode_double = 0.0;
		if ( (GWMode != null) && (GWMode.length() > 0) ) {
			fill_GWMode = true;
			GWMode_double = Double.parseDouble ( GWMode );
		}

		boolean fill_AcresTotal = false;
		double AcresTotal_double = -1.0;
		if ( (AcresTotal != null) && (AcresTotal.length() > 0) ) {
			fill_AcresTotal = true;
			AcresTotal_double = Double.parseDouble ( AcresTotal );
		}
		/* TODO SAM evaluate how to warn.  For now set total first, then GW terms,
		 * then SW terms, adjusting as each value is set.
		// If any acreage term is specified, then all must be specified and agree with the total acreage...
		if ( (AcresSWFlood_double >= 0.0) || (AcresSWSprinkler_double >= 0.0) ||
				(AcresGWFlood_double >= 0.0) || (AcresGWSprinkler_double >= 0.0) ) {
			if ( AcresSWFlood_double < 0.0 ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"All acreage terms must be specified if any are set.  Check AcresSWFlood." );
			}
			if ( AcresSWSprinkler_double < 0.0 ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"All acreage terms must be specified if any are set.  Check AcresSWSprinkler." );
			}
			if ( AcresGWFlood_double < 0.0 ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"All acreage terms must be specified if any are set.  Check AcresGWFlood." );
			}
			if ( AcresGWSprinkler_double < 0.0 ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"All acreage terms must be specified if any are set.  Check AcresGWSprinkler." );
			}
			if ( AcresTotal_double < 0.0 ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"All acreage terms must be specified if any are set.  Check AcresTotal." );
			}
			if ( (int)(AcresSWFlood_double + AcresSWSprinkler_double +
				AcresGWFlood_double + AcresGWSprinkler_double) != (int)AcresTotal_double ) {
				Message.printWarning ( 2,
						formatMessageTag(command_tag,++warning_count), routine,
						"The acreage parts do not equal the total." );
			}
		}
		*/

		StateCU_IrrigationPracticeTS ipy = null;
		StateCU_Location culoc = null;	// Needed to check if GW-only.
		String ipy_id;
		int matchCount = 0;
		int year = 0;
		boolean is_gw_only = false;
		for (int i = 0; i < ipyListSize; i++) {
			ipy = (StateCU_IrrigationPracticeTS)ipyList.get(i);
			ipy_id = ipy.getID();
			if ( !ipy_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			
			// Get the CU location and determine if GW only...
			
			int pos = StateCU_Util.indexOf( culocList, ipy_id);
			if ( pos >= 0 ) {
				culoc = (StateCU_Location)culocList.get(pos);
				is_gw_only = culoc.hasGroundwaterOnlySupply();
			}

			// Have a match so reset the data...

			DateTime date = new DateTime(DateTime.PRECISION_YEAR);
			for ( year = SetStart_int; year <= SetEnd_int; year++ ) {
				date.setYear(year);
				if ( fill_SurfaceDelEffMax ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " SurfaceDelEffMax -> " + SurfaceDelEffMax );
					ipy.setCeff ( year, SurfaceDelEffMax_double );
				}
				if ( fill_FloodAppEffMax ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " FloodAppEffMax -> " + FloodAppEffMax );
					ipy.setFeff ( year, FloodAppEffMax_double );
				}
				if ( fill_SprinklerAppEffMax ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " SprinklerAppEffMax -> "+ SprinklerAppEffMax );
					ipy.setSeff ( year, SprinklerAppEffMax_double );
				}
				// Fill these in the order that agrees with the procedure.
				if ( fill_AcresTotal ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresTotal -> "+ AcresTotal );
					ipy.setTacre ( year, AcresTotal_double );
					// Cascade groundwater and surface water acres adjustments...
					ipy.adjustGroundwaterAcresToTotalAcres ( date, is_gw_only );
				}
				if ( fill_AcresGWSprinkler && fill_AcresGWFlood ) {
					// Set both to get a new GW total and then adjust based on the overall total.
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresGWSprinkler -> "+ AcresGWSprinkler );
					ipy.setAcgwspr ( year, AcresGWSprinkler_double );
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresGWFlood -> "+ AcresGWFlood );
					ipy.setAcgwfl ( year, AcresGWFlood_double );
					// Compute the new total...
					ipy.refreshAcgw ( year );
					Message.printStatus ( 2, routine,
							"Since AcresGWSprinkler and AcresGWFlood are set, recompute GWtotal for " +
							year + " " + ipy_id + " AcresGW -> " + ipy.getAcgw(year) );
					// Cascade groundwater and surface water acres adjustments...
					ipy.adjustGroundwaterAcresToTotalAcres ( date, is_gw_only );
				}
				else if ( fill_AcresGWSprinkler ) {
					// Set the one, recompute the total, and adjust back to total...
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresGWSprinkler -> "+ AcresGWSprinkler );
					ipy.setAcgwsprAndAdjust ( year, AcresGWSprinkler_double );
				}
				else if ( fill_AcresGWFlood ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresGWFlood -> "+ AcresGWFlood );
					ipy.setAcgwflAndAdjust ( year, AcresGWFlood_double );
				}
				if ( fill_AcresSWSprinkler && fill_AcresSWFlood) {
					// Set both terms and then adjust...
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresSWSprinkler -> "+ AcresSWSprinkler );
					ipy.setAcswspr ( year, AcresSWSprinkler_double );
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresSWFlood -> "+ AcresSWFlood );
					ipy.setAcswfl ( year, AcresSWFlood_double );
					// Compute the new total...
					ipy.refreshAcsw ( year );
					// Cascade surface water checks...
					ipy.adjustSurfaceWaterAcresToGroundwaterAndTotalAcres ( date, is_gw_only );
				}
				else if ( fill_AcresSWSprinkler ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresSWSprinkler -> "+ AcresSWSprinkler );
					ipy.setAcswsprAndAdjust ( year, AcresSWSprinkler_double );
				}
				else if ( fill_AcresSWFlood ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresSWFlood -> "+ AcresSWFlood );
					ipy.setAcswflAndAdjust ( year, AcresSWFlood_double );
				}
				/* Disable - confusing to support old model
				if ( fill_AcresGW ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresGW -> "+ AcresGW );
					ipy.setGacre ( year, AcresGW_double );
				}
				if ( fill_AcresSprinkler ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " AcresSprinkler -> "+
					AcresSprinkler );
					ipy.setSacre ( year, AcresSprinkler_double );
				}
				*/
				if ( fill_PumpingMax ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " PumpingMax -> "+ PumpingMax );
					ipy.setMprate ( year, PumpingMax_double );
				}
				if ( fill_GWMode ) {
					Message.printStatus ( 2, routine, "Setting " +
					year + " " + ipy_id + " GWMode -> "+ GWMode );
					ipy.setGmode ( year, (int)(GWMode_double + .1));
				}
			}
		}

		// If nothing was matched, take further action...

		if ( (matchCount == 0) && (ID.indexOf("*") < 0) ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Irrigation practice time series \"" + ID +
				"\" was not matched: warning and not setting irrigation practice time series.";
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
				"\" was not matched: failing and not setting irrigation practice time series.";
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
        message = "Unexpected error setting irrigation practice time series (" + e + ").";
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
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String SurfaceDelEffMax = parameters.getValue ( "SurfaceDelEffMax" );
	String FloodAppEffMax = parameters.getValue ( "FloodAppEffMax" );
	String SprinklerAppEffMax = parameters.getValue ( "SprinklerAppEffMax" );
	String AcresSWFlood = parameters.getValue ( "AcresSWFlood" );
	String AcresSWSprinkler = parameters.getValue ( "AcresSWSprinkler" );
	String AcresGWFlood = parameters.getValue ( "AcresGWFlood" );
	String AcresGWSprinkler = parameters.getValue ( "AcresGWSprinkler" );
	String PumpingMax = parameters.getValue ( "PumpingMax" );
	String GWMode = parameters.getValue ( "GWMode" );
	String AcresTotal = parameters.getValue ( "AcresTotal" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
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
	if ( (SurfaceDelEffMax != null) && (SurfaceDelEffMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SurfaceDelEffMax=" + SurfaceDelEffMax );
	}
	if ( (FloodAppEffMax != null) && (FloodAppEffMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FloodAppEffMax=" + FloodAppEffMax );
	}
	if ( (SprinklerAppEffMax != null) && (SprinklerAppEffMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SprinklerAppEffMax=" + SprinklerAppEffMax);
	}
	if ( (AcresSWFlood != null) && (AcresSWFlood.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSWFlood=" + AcresSWFlood );
	}
	if ( (AcresSWSprinkler != null) && (AcresSWSprinkler.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSWSprinkler=" + AcresSWSprinkler );
	}
	if ( (AcresGWFlood != null) && (AcresGWFlood.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGWFlood=" + AcresGWFlood );
	}
	if ( (AcresGWSprinkler != null) && (AcresGWSprinkler.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGWSprinkler=" + AcresGWSprinkler );
	}
	/* TODO SAM 2007-10-08 remove once tested - no longer supported
	if ( AcresGW.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGW=" + AcresGW );
	}
	if ( AcresSprinkler.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSprinkler=" + AcresSprinkler );
	}
	*/
	if ( (PumpingMax != null) && (PumpingMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PumpingMax=" + PumpingMax );
	}
	if ( (GWMode != null) && (GWMode.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GWMode=" + GWMode );
	}
	if ( (AcresTotal != null) && (AcresTotal.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresTotal=" + AcresTotal );
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