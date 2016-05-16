package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Structure;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSIdent;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;

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
This class initializes, checks, and runs the ReadDiversionHistoricalTSMonthlyFromHydroBase() command.
</p>
*/
public class ReadDiversionHistoricalTSMonthlyFromHydroBase_Command 
extends AbstractCommand implements Command
{
/**
Possible values for boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Read start and end (can be null) to use global output period.
*/
private DateTime __ReadStart_DateTime = null;
private DateTime __ReadEnd_DateTime = null;

/**
Constructor.
*/
public ReadDiversionHistoricalTSMonthlyFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadDiversionHistoricalTSMonthlyFromHydroBase" );
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
	// parameters
	String ID = parameters.getValue( "ID" ); 
	String ReadStart = parameters.getValue( "ReadStart" ); 
	String ReadEnd = parameters.getValue( "ReadEnd" ); 
	String AverageFillFlag = parameters.getValue( "AverageFillFlag" ); 
	String PatternFillFlag = parameters.getValue( "PatternFillFlag" ); 
	String FillUsingCIU = parameters.getValue( "FillUsingCIU" );
	String FillUsingCIUFlag = parameters.getValue( "FillUsingCIUFlag" );
	String IncludeExplicit = parameters.getValue( "IncludeExplicit" );
	String IncludeCollections = parameters.getValue( "IncludeCollections" );
	String LEZeroInAverage = parameters.getValue( "LEZeroInAverage" );
	String UseDiversionComments = parameters.getValue( "UseDiversionComments");
	String FillPatternOrder = parameters.getValue( "FillPatternOrder" );
	String FillAverageOrder = parameters.getValue( "FillAverageOrder" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}
	
	__ReadStart_DateTime = null;
	__ReadEnd_DateTime = null;
	if ( ReadStart != null && !ReadStart.equals("") &&
		!ReadStart.equalsIgnoreCase("OutputStart") &&
		!ReadStart.equalsIgnoreCase("OutputEnd") ) {
		try {
			__ReadStart_DateTime = DateTime.parse( ReadStart);
		}
		catch ( Exception e ) {
			message = "Read start date (" + ReadStart + ") is not a valid date.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid read start." ) );
		}
	}
	if ( ReadEnd != null && !ReadEnd.equals("") &&
		!ReadEnd.equalsIgnoreCase("OutputStart") &&
		!ReadEnd.equalsIgnoreCase("OutputEnd") ) {
		try {
			__ReadEnd_DateTime = DateTime.parse(ReadEnd);
		}
		catch ( Exception e ) {
			message = "Read end date (" + ReadEnd + ") is not a valid date.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid read end." ) );
		}
	}
	if ( (__ReadStart_DateTime != null) && (__ReadEnd_DateTime != null) &&
		__ReadStart_DateTime.greaterThan(__ReadEnd_DateTime) ) {
		message = "Read start date is later than the read end date.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify a valid read end." ) );
	}
	if ( (__ReadStart_DateTime != null) && (__ReadStart_DateTime.getPrecision() != DateTime.PRECISION_MONTH) ) {
		message = "Read start date precision must be monthly.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the read start to monthly precision." ) );
	}
	if ( (__ReadEnd_DateTime != null) && (__ReadEnd_DateTime.getPrecision() != DateTime.PRECISION_MONTH) ) {
		message = "Read end date precision must be monthly.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the read start to monthly precision." ) );
	}
	if ( IncludeExplicit != null && !IncludeExplicit.equals("") &&
		!(IncludeExplicit.equalsIgnoreCase(_True)) &&
		!(IncludeExplicit.equalsIgnoreCase(_False))) {
		message = "The include explicit parameter (" + IncludeExplicit + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeExplicit value as " + _False + " or " + _True + " (default)." ) );
	}
	if ( IncludeCollections != null && !IncludeCollections.equals("") &&
		!(IncludeCollections.equalsIgnoreCase(_True)) &&
		!(IncludeCollections.equalsIgnoreCase(_False))) {
		message = "The include collections parameter (" + IncludeCollections + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeCollections value as " + _False + " or " + _True + " (default)." ) );
	}
	if ( LEZeroInAverage != null && !LEZeroInAverage.equals("") &&
		!(LEZeroInAverage.equalsIgnoreCase(_True)) &&
		!(LEZeroInAverage.equalsIgnoreCase(_False))) {
		message = "The LEZeroInAverage parameter (" + LEZeroInAverage + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the LEZeroInAverage value as " + _False + " or " + _True + " (default)." ) );
	}
	if ( UseDiversionComments != null && !UseDiversionComments.equals("") &&
		!(UseDiversionComments.equalsIgnoreCase(_True)) &&
		!(UseDiversionComments.equalsIgnoreCase(_False))) {
		message = "The UseDiversionComments parameter (" + UseDiversionComments + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the UseDiversionComments value as " + _False + " (default) or " + _True + "." ) );
	}
	if ( FillUsingCIU != null && !FillUsingCIU.equals("") &&
		!(FillUsingCIU.equalsIgnoreCase(_True)) &&
		!(FillUsingCIU.equalsIgnoreCase(_False))) {
		message = "The fill using ciu parameter (" + FillUsingCIU + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the FillUsingCIU value as " + _False + " (default) or " + _True + "." ) );
	}
	if ( PatternFillFlag != null &&	(PatternFillFlag.length() > 0) &&
		!PatternFillFlag.equalsIgnoreCase("Auto") && (PatternFillFlag.length() != 1) ) {
		message = "The pattern fill flag must be \"Auto\" or a single character.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the PatternFillFlag value as Auto or a single character." ) );
	}
	if ( FillUsingCIUFlag != null && (FillUsingCIUFlag.length() > 0) &&
		!FillUsingCIUFlag.equalsIgnoreCase("Auto") && (FillUsingCIUFlag.length() != 1) ) {
		message = "The fill using ciu flag must be \"Auto\" or a single character.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the FillUsingCIUFlag value as Auto or a single character." ) );
	}
	if ( AverageFillFlag != null && (AverageFillFlag.length() > 0) &&
		(AverageFillFlag.length() != 1) ) {
		message = "The average fill flag must be \"Auto\" or a single character.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the AverageFillFlag value as Auto or a single character." ) );
	}
	if ( FillAverageOrder != null )	{
		if ( !StringUtil.isInteger(FillAverageOrder)) {
			message = "The fill average order (" + FillAverageOrder + ") is not a valid number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify FillAverageOrder as an integer 1 or 2." ) );
		}
		else {
			int value = StringUtil.atoi(FillAverageOrder);
			if( value != 1 && value != 2 ) {
				message = "The fill average order (" + FillAverageOrder + ") is not a valid number.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify FillAverageOrder as an integer 1 or 2." ) );
			}
		}
	}
	if ( FillPatternOrder != null )	{
		if ( !(StringUtil.isInteger(FillPatternOrder))) {
			message = "The fill pattern order (" + FillPatternOrder + ") is not a valid number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify FillPatternOrder as an integer 1 or 2." ) );
		}
		else {
			int value = StringUtil.atoi(FillPatternOrder);
			if( value != 1 && value != 2 ) {
				message = "The fill pattern order (" + FillPatternOrder + ") is not a valid number.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify FillPatternOrder as an integer 1 or 2." ) );
			}
		}
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "IncludeExplicit");
    valid_Vector.add ( "IncludeCollections" );
    valid_Vector.add ( "ReadStart" );
    valid_Vector.add ( "ReadEnd" );
    valid_Vector.add ( "AverageFillFlag" );
    valid_Vector.add ( "LEZeroInAverage" );
    valid_Vector.add ( "UseDiversionComments" );
    valid_Vector.add ( "PatternID" );
    valid_Vector.add ( "FillPatternOrder" );
    valid_Vector.add ( "PatternFillFlag" );
    valid_Vector.add ( "FillAverageOrder" );
    valid_Vector.add ( "AverageFillFlag" );	
    valid_Vector.add ( "FillUsingCIU" );
    valid_Vector.add ( "FillUsingCIUFlag" );
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
	return (new ReadDiversionHistoricalTSMonthlyFromHydroBase_JDialog ( parent, this )).ok();
}

/**
Fills the given time series with constant values based on the CIU flag.
@param ts Time series to fill.
@param hbdmi HydroBaseDMI connection.
@param FillUsingCIUFlag Flag used to fill valid values.
@param start Starting DateTime for calculation of the fill.
@param end Ending DateTime for calculation of the fill.
@param command_tag Command tag used for messaging.
@param warningLevel Warning level used for messaging.
@param ignore_lezero Indicates whether or not to ignore less than zero values when computing averages.
*/
private int fillUsingCIUFlag ( TS ts, HydroBaseDMI hbdmi,
		String FillUsingCIUFlag, DateTime start, DateTime end, 
		String command_tag, int warningLevel, int warningCount, CommandStatus status, int ignore_lezero )
{	String message = null;
	if ( ts == null || hbdmi == null ) {
		return warningCount;
	}		
	if ( command_tag == null ) {
		command_tag = "";
	}

	String routine = "readDiversionHistoricalTSMonthlyFromHydroBase.fillUsingCIUFlag";
	// get CIU flag value from HydroBase
	String TSID_Location_part = ts.getLocation();
	HydroBase_Structure struct = null;
	int [] wdid_parts = null;
	try {
		wdid_parts = HydroBase_WaterDistrict.parseWDID ( TSID_Location_part );
	}
	catch (Exception e1) {
 		message = "Couldn't parse WDID for TSID Location part: " + TSID_Location_part;
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Confirm that only WDIDs are used for identifiers" ) );
		return warningCount;
	}
	int wd = wdid_parts[0];
	int id = wdid_parts[1];
	try {
		struct = hbdmi.readStructureViewForWDID ( wd, id );
	} catch (Exception e1) {
		message = "Unexpected error reading structure data for WDID using wd: " + wd + " and id: " + id;
		Message.printWarning(3, routine, e1);
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Confirm that only WDIDs are used for identifiers" ) );
		return warningCount;
	}
	// HydroBase currently in use value
	String ciu = struct.getCiu();
	// set the fill value
	String fillValue = "0";
	String fillFlag = "";
	if( FillUsingCIUFlag.equals( "Auto" )) {
		fillFlag = ciu;
	}
	else if( FillUsingCIUFlag.length() == 1 ) {
		fillFlag = FillUsingCIUFlag;
	}
	
	// Based on CIU string, fill missing values with
	// flag value
	// H = "Historical structure"
	// I = "Inactive structure"
	if( ciu.equalsIgnoreCase( "H" ) || ciu.equalsIgnoreCase( "I" )) {
		// Recalculate TS Limits
		recalculateLimits( ts, start, end, ignore_lezero);
		// Fill missing data values at end of period with zeros
		try {
			// get the nearest data point from the end of the period
			TSData tmpTSData = TSUtil.findNearestDataPoint(ts, start, end, true);	//reverse
			if( tmpTSData != null) {
				PropList const_prop = HydroBase_Util.createFillConstantPropList(ts,
					fillValue, fillFlag, tmpTSData.getDate(), ts.getDate2());
				// fill time series with zeros from last non-missing value
				// until the end of the period.
				TSUtil.fillConstant(ts, start, end, 0, const_prop);
			}
		} catch (Exception e) {
			message = "Couldn't fill the given time series with constant value.";
			Message.printWarning(3, routine, e);
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check previous warnings and the log file." ) );
		}	
	}
	// N = "Non-existent structure"
	else if( ciu.equalsIgnoreCase( "N" )) {
		// Recalculate TS Limits
		recalculateLimits( ts, start, end, ignore_lezero );
		try {
			TSData tmpTSData = TSUtil.findNearestDataPoint(ts, start, end, false);
			if( tmpTSData != null) {
				// Create propList for fill command
				PropList const_prop = HydroBase_Util.createFillConstantPropList(ts,
					fillValue, fillFlag, ts.getDate1(), tmpTSData.getDate());
				// fill time series with zero's from first non-missing value
				// until the beginning of the period.
				TSUtil.fillConstant(ts, start, end, 0, const_prop);
			}
		}
		catch (Exception e) {
			message = "Couldn't fill the given time series with a constant value.";
			Message.printWarning(3, routine, e);
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check previous warnings and the log file." ) );
		}
	}
	return warningCount;
}

/**
@param ts Time Series to recalculate.
@param date1 Beginning DateTime to use for recalculation.
@param date2 Ending DateTime to use for recalculation.
@param ignore_lezero_flag Indicates whether or not to ignore values less than zero.
*/
private void recalculateLimits (TS ts, DateTime date1, DateTime date2, int ignore_lezero_flag )
{
	String routine = "readDiversionHistoricalTSMonthlyFromHydroBase.recalculateLimits";
	try {
		ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts, date1, date2, ignore_lezero_flag ) );
	}
	catch ( Exception e ) {
		Message.printStatus ( 2, routine,
		"Unable to set original data limits for " + ts.getIdentifierString() + " - all missing?" );
	}
}

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getClass().getName() + ".runCommand";
	int warningLevel = 2;
	String command_tag = "" + command_number;
	String interval = "Month";
    int log_level = 3;  // Log level for non-user warnings
    int warningCount = 0;
	
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input needed to process the file...
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String ID_Java = StringUtil.replaceString(ID,"*",".*");
	String IncludeExplicit = parameters.getValue ( "IncludeExplicit" );
	if ( IncludeExplicit == null ) {
		IncludeExplicit = _True;
	}
	String IncludeCollections = parameters.getValue ( "IncludeCollections" );
	if ( IncludeCollections == null ) {
		IncludeCollections = _True;
	}
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	if ( LEZeroInAverage == null ) {
		LEZeroInAverage = _True;	// Default as per old demandts
	}
	String UseDiversionComments = parameters.getValue ( "UseDiversionComments" );
	if ( UseDiversionComments == null ) {
		UseDiversionComments = _True;
	}
	String ReadStart = parameters.getValue ( "ReadStart" );
	String ReadEnd = parameters.getValue ( "ReadEnd" );
	String PatternID = parameters.getValue ( "PatternID" );
	String FillPatternOrder = parameters.getValue ( "FillPatternOrder" );
	String FillAverageOrder = parameters.getValue ( "FillAverageOrder" );
	String AverageFillFlag = parameters.getValue ( "AverageFillFlag" );
	String PatternFillFlag = parameters.getValue ( "PatternFillFlag" );
	String FillUsingCIU = parameters.getValue ( "FillUsingCIU" );
	String FillUsingCIUFlag = parameters.getValue ( "FillUsingCIUFlag" );
	
   // Output period will be used if not specified with SetStart and SetEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    DateTime OutputEnd_DateTime = null;
    try {
    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputEnd (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	if ( OutputStart_DateTime == null ) {
        message = "The output start has not been specified.";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output start with SetOutputPeriod() prior to this command." ) );
	}
	if ( OutputEnd_DateTime == null ) {
        message = "The output end has not been specified.";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output end with SetOutputPeriod() prior to this command." ) );
	}

	// Initialize properties for filling...
	PropList average_fill_props = new PropList ( "fill" );
	average_fill_props.set ( "FillFlag", AverageFillFlag );
	PropList pattern_fill_props = new PropList ( "fill" );
	pattern_fill_props.set ( "FillFlag", PatternFillFlag );
	
	boolean IncludeExplicit_boolean = true;
	if ( IncludeExplicit.equalsIgnoreCase(_False) ) {
		IncludeExplicit_boolean = false;
	}
	boolean IncludeCollections_boolean = true;
	if ( IncludeCollections.equalsIgnoreCase(_False) ) {
		IncludeCollections_boolean = false;
	}
	int ignore_lezero_flag = 0;
	if ( LEZeroInAverage.equalsIgnoreCase(_False) ) {
		// Ignore the values...
		pattern_fill_props.set ( "IgnoreLessThanOrEqualZero", _True );
		ignore_lezero_flag = TSLimits.IGNORE_LESS_THAN_OR_EQUAL_ZERO;
	}
	else {
		// Do not ignore the values...
		pattern_fill_props.set ( "IgnoreLessThanOrEqualZero", _False );
	}
	
	DateTime ReadStart_DateTime = null, ReadEnd_DateTime = null;
	if ( ReadStart == null ) {
		// Use the output date/time (even if null)...
		ReadStart_DateTime = OutputStart_DateTime;
	}
	else {
		if ( ReadStart.equalsIgnoreCase( "OutputStart" ) &&	(OutputStart_DateTime != null) ) {
			ReadStart_DateTime = new DateTime(OutputStart_DateTime);
		}
		else {
			ReadStart_DateTime = new DateTime(__ReadStart_DateTime);
		}
	}
	if ( ReadEnd == null ) {
		// Use the output date/time (even if null)...
		ReadEnd_DateTime = OutputEnd_DateTime;
	}
	else {
		if ( ReadEnd.equalsIgnoreCase( "OutputEnd" ) && (OutputEnd_DateTime != null) ) {
			ReadEnd_DateTime = new DateTime(OutputEnd_DateTime);
		}
		else {
			ReadEnd_DateTime = new DateTime(__ReadEnd_DateTime);
		}
	}

	boolean fill_pattern = false;
	int FillPatternOrder_int = -1;
	if ( FillPatternOrder != null ) {
		FillPatternOrder_int = Integer.parseInt (FillPatternOrder);
		fill_pattern = true;
	}
	boolean fill_average = false;
	int FillAverageOrder_int = -1;
	if ( FillAverageOrder != null ) {
		FillAverageOrder_int = Integer.parseInt  (FillAverageOrder);
		fill_average = true;
	}
	
	// Get the list of diversion stations...
	
	List stationList = null;
	int stationListSize = 0;
	try {
		stationList = (List)processor.getPropContents ( "StateMod_DiversionStation_List");
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting diversion station data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the HydroBase DMI...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
    if ( warningCount > 0 ) {
        message = "There were " + warningCount + " warnings about command input.";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	int compType = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY; // No daily support yet
		StateMod_Diversion div = null;
		MonthTS ts = null; // Total time series, either for an explicit structure the sum for a collection.
		MonthTS sumts = null; // The sum of time series for an aggregate, without filling, used to
						// compute the original data limits so that they can be used later in other
						// fill commands.
		TSIdent tsident = null; 	// TSIDent for time series
		MonthTS pts = null; // Part time series, to add to ts.
		List parts = null;
		int psize = 0; // Number of parts in a collection
		int iparts = 0; // Index for iterating through parts
		int part_count = 0; // Counter (1+) of parts in a collection
		String part_id = ""; // Identifier for a part in a collection
		int [] wdid_parts = new int[2];	// Parts when a WDID is parsed
		String collection_type = null;
		boolean fillUsingCIU = false;
		String id;	// Diversion ID.
		boolean blank_ts_created; // Indicates if a blank time series needed to be added (in which case
						// some exceptions on limits can be avoided).
		boolean is_wdid; // Used to help manage messages for HydroBase and non-HydroBase structures.
		for ( int i = 0; i < stationListSize; i++ ) {
			ts = null;	// If null at the end a blank time series will be added.
			div = (StateMod_Diversion)stationList.get(i);
			id = div.getID();
			if ( !id.matches(ID_Java) ) {
				// Do not read...
				continue;
			}
	
			if ( !IncludeCollections_boolean && div.isCollection() ) {
				// Ignore diversion stations that are collections...
				// Messages could be excessive if multiple read
				// commands are used so don't print for now...
				//Message.printStatus ( 2, routine,
				//"Not reading time series for \"" + id +
				//"\" (it is a collection)." );
				continue;
			}
	
			if ( !IncludeExplicit_boolean && !div.isCollection() ) {
				// Ignore diversion stations are explicit...
				// Messages could be excessive if multiple read
				// commands are used so don't print for now...
				//Message.printStatus ( 2, routine,
				//"Not reading time series for \"" + id +
				//"\" (it is explicit)." );
				continue;
			}
	
			// Read the time series from HydroBase.  If an aggregate or
			// system, read each time series and aggregate...
	
			if ( div.isCollection() ) {
				// Aggregate or system...
				Message.printStatus ( 2, routine, "Reading diversion time series for diversion " +
				div.getCollectionType() + " \"" + id + "\"" );
				psize = 0;
				parts = div.getCollectionPartIDs(0);
				sumts = null;
				part_count = 0;	// The count of time series from parts.
						// Because errors could occur and skip "ipart" values, this is used to
						// verify that adding time series occurs only on the 2nd, 3rd... etc. time series.
				if ( parts != null ) {
					psize = parts.size();
				}
				boolean ts_initialized = false;	// If false, it is the first non-null time series in the
								// collection and will be initialized.  If true, it is added to the first TS.
				collection_type = div.getCollectionType();
				for ( iparts = 0; iparts < psize; iparts++ ) {
					part_id = (String)parts.get(iparts);
					Message.printStatus ( 2, routine, "Reading diversion time series for \"" + id +
					"\" (part " + (iparts + 1) + ": "+part_id +")");
					try {
						// Parse out the WDID...
						HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
					}
					catch ( Exception e ) {
						// Not a WDID - this is an error because
						// valid structures are expected as parts of an aggregate...
						message = "Location \"" + id + "\" (part " +
						(iparts + 1) + ": " + part_id + ") that is not a WDID.";
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warningCount),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that collection parts are WDIDs." ) );
						continue;
					}
					blank_ts_created = false;	// Reset below
					try {
						// Read the part...
						pts = (MonthTS)hbdmi.readTimeSeries( part_id + ".DWR.DivTotal." + interval,
							ReadStart_DateTime, ReadEnd_DateTime, null, true, null );
						// If the read start and end were specified, make sure the time series
						// has a period of the output period...
						if ( (pts != null) && (((ReadStart_DateTime != null) &&
							ReadStart_DateTime.greaterThan(OutputStart_DateTime)) ||
							((ReadEnd_DateTime != null) &&
							ReadEnd_DateTime.lessThan(OutputEnd_DateTime))) ) {
							pts.changePeriodOfRecord ( OutputStart_DateTime, OutputEnd_DateTime );
						}
					}
					catch ( Exception e ) {
						// It is possible that a diversion time series is not in HydroBase so make
						// non-fatal and add a time series with missing data.  Do so by setting to
						// null and handling below...
						pts = null;
						// Can be confusing to users so only show in debug mode...
						if ( Message.isDebugOn ) {
							Message.printWarning ( 3, routine, e);
						}
					}
					if ( pts == null ) {
						Message.printStatus ( 2, routine,
						"Could not read diversion time series data from HydroBase for " + id +
						" (part " + (iparts + 1) + ": " +
						part_id + ") - setting to missing.  May need to reset total." );
						// TODO SAM 2009-01-24 Evaluate whether a warning
						// Make non-fatal for now...
						//++warning_count;
						//continue;
						//
						// Create a missing time series so that
						// diversion comments can be found -
						// this may actually occur when no
						// monthly data are available...
						blank_ts_created = true;
						try {
							tsident = new TSIdent ( part_id, "DWR",
								StateMod_DataSet.lookupTimeSeriesDataType(
									StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY),"Month", "" );
						} catch (Exception e) {
							Message.printWarning(3, routine, e);
						}
						try {
							pts = (MonthTS)TSUtil.newTimeSeries ( tsident.toString(), true );
						} catch (Exception e) {
							Message.printWarning(3, routine, e);
						}
						try {
							pts.setIdentifier ( tsident );
						} catch (Exception e) {
							Message.printWarning(3, routine, e);
						}
						pts.setDescription ( part_id + " - no DivTotal in HydroBase" );
						pts.setDataUnits ( StateMod_DataSet.lookupTimeSeriesDataUnits(
							StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) );
						pts.setDataUnitsOriginal ( pts.getDataUnits() );
						pts.setDate1 ( OutputStart_DateTime );
						pts.setDate1Original( OutputStart_DateTime);
						pts.setDate2 ( OutputEnd_DateTime );
						pts.setDate2Original( OutputEnd_DateTime);
						pts.allocateDataSpace();
					}
					// By here we have a part to process...
					++part_count;
					// Fill with diversion comments...
					if ( (pts != null) && UseDiversionComments.equalsIgnoreCase(_True) ) {
						try {
							HydroBase_Util.fillTSUsingDiversionComments( hbdmi, pts, null, null );
						} catch (Exception e) {
							message = "Error filling time series with diversion comments (" + e + ").";
							Message.printWarning(3, routine, e);
							Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( command_tag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
						}		
					}
					// check for CIU flag
					if ( pts != null && FillUsingCIU != null && FillUsingCIU.equalsIgnoreCase( _True) ) {
						fillUsingCIU = true;
						warningCount = fillUsingCIUFlag( pts, hbdmi, 
							FillUsingCIUFlag, OutputStart_DateTime, OutputEnd_DateTime,
							command_tag, warningLevel, warningCount, status, ignore_lezero_flag );
					}
					
					// Set the original data limits on the part.
					// This is needed when doing filling on the part...
					if ( (fill_pattern || fill_average) && !fillUsingCIU ) {
						recalculateLimits(pts, OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag );
					}
	
					// Initialize the total time series if necessary...
	
					if ( !ts_initialized && (pts != null) ) {
						// Just use the time series that was returned...
						Message.printStatus ( 2, routine, "Initializing " + collection_type + " "+
						id + " time series to first part (" + part_id + ") time series." );
						ts = pts;
						ts_initialized = true;
						// Set the identifier and name to that of the collection...
						ts.getIdentifier().setLocation(id);
						ts.setDescription ( id + " Diversion " + collection_type );
	
						// Also create a copy for the sum that is used to calculate the original limits...
	
						sumts = (MonthTS)ts.clone();
					}
	
					// Add the part to the raw data total so that
					// averages can be computed based on the total of the original data...
					if ( (sumts != null) && (pts != null) && (part_count > 1) ) {
						// Add the part time series to the full time series...
						try {
							//Message.printStatus ( 2, routine, "Adding part \"" + part_id +
							//"\" diversion TS to unfilled " + "main TS \""+id + "\"" );
							sumts = (MonthTS)TSUtil.add ( sumts,pts);
						}
						catch ( Exception e ) {
							// Hide for now since similar error will be made for add on filled data.
							//Message.printWarning ( 2, routine, "Error adding diversion time "
							//+ "series for \"" + part_id + "\" to \"" + id + "\"." );
							//Message.printWarning ( 3, routine, e );
							//++warning_count;
						}
					}
	
					// If requested, fill the part before adding it
					// to the sum.  Do not process if a blank part was created because errors will occur.
	
					if ( (pts != null) && !blank_ts_created && (fill_pattern || fill_average) ) {
						// Need to fill with one or both in the requested order...
						for(int ifill = 1; ifill <= 2; ifill++){
							if ( ifill == FillPatternOrder_int ) {
								// Fill pattern.  This will throw an exception if the
								// pattern is not found.  Let the exception cause the command to stop.
								try {
									warningCount =
									processor.fillTSPattern ( pts, routine, PatternID, id, part_id,
									"diversion historical", OutputStart_DateTime, OutputEnd_DateTime,
									pattern_fill_props, warningLevel, warningCount, command_tag, status );
								}
								catch ( Exception e ) {
									// Message is printed in above method.
								}
							}
							else if(ifill == FillAverageOrder_int ){
								// Fill with monthly average...
								try {
									warningCount = processor.fillTSMonthlyAverage (
									command_tag, warningCount, (MonthTS)pts, routine,
									id, part_id, "diversion historical",
									OutputStart_DateTime, OutputEnd_DateTime, average_fill_props );
								}
								catch ( Exception e ) {
									// Message has already been printed in the fill method.
								}
							}
						}// End filling.
					}
	
					// Accumulate the data.  This logic follows the old watright logic.  Let
					// the all-missing time series go through because it will add to the history and
					// description...
	
					if ( (pts != null) && (part_count > 1) ) {
						// Add the part time series to the full time series...
						try {
							Message.printStatus ( 2, routine, "Adding part \"" +
							(iparts + 1) + ": " + part_id + "\" diversion TS to main TS \""+ id + "\"" );
							ts = (MonthTS)TSUtil.add ( ts, pts );
						}
						catch ( Exception e ) {
							message = "Error adding diversion time series for \"" + part_id +
							"\" to \"" + id + "\" (" + e + ").";
							Message.printWarning(3, routine, e);
							Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( command_tag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
						}
					}
				}
	
				// Calculate the limits on the collection's time series 
				// for use when filling with historical averages.  This does
				// include the zero's from CIU filling.
				if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
					recalculateLimits ( ts, OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag );
				}
			} // End collection
			else {
				// Single ditch...
				Message.printStatus ( 2, routine, "Reading diversion time series for \"" + id + "\"" );
				is_wdid = true;
				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - non-fatal, just ignore the diversion...
					Message.printStatus ( 2, routine, "Not reading diversion \"" + id +
					"\" - does not appear to be a WDID." );
					is_wdid = false;
				}
				if ( is_wdid ) {
					// WDID is valid so try to read...
					try {
						ts = (MonthTS)hbdmi.readTimeSeries ( id + ".DWR.DivTotal." +interval,
							ReadStart_DateTime, ReadEnd_DateTime, null, true, null );
						// If the read start and end were specified, make sure the time series
						// has a period of the output period...
						if ( (ts != null) && (((ReadStart_DateTime != null)&&
							ReadStart_DateTime.greaterThan(OutputStart_DateTime)) ||
							((ReadEnd_DateTime != null) &&
							ReadEnd_DateTime.lessThan(OutputEnd_DateTime))) ){
							ts.changePeriodOfRecord ( OutputStart_DateTime, OutputEnd_DateTime );
						}
					}
					catch ( Exception e ) {
						Message.printStatus ( 2, routine,
						"Unable to read diversion time series data from HydroBase for " + id );
						if ( Message.isDebugOn ) {
							Message.printWarning ( 3, routine, e );
						}
						// Not fatal because aggregates and other nodes may not be in HydroBase...
						//++warning_count;
						// TODO SAM 2009-01-24 Evaluate whether warning status log should be added
						ts = null;
					}
				}
	
				blank_ts_created = false;
				if ( ts == null ) {
					if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
						ts = new MonthTS ();
						ts.setDataUnits (  StateMod_DataSet.lookupTimeSeriesDataUnits(compType) );
						try {
							tsident = new TSIdent ( id, "DWR",
								StateMod_DataSet.lookupTimeSeriesDataType(compType), "Month", "" );
						} catch (Exception e) {
							message = "Unable to create time series identifier for \"" + ID + "\" (" + e + ").";
							Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( command_tag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
						}
					}
					/* TODO SAM 2011-01-16 Need to enable
					else {
						ts = new DayTS ();
						ts.setDataUnits ( StateMod_DataSet.lookupTimeSeriesDataUnits(compType) );
						try {
							tsident = new TSIdent ( id, "DWR",
								StateMod_DataSet.lookupTimeSeriesDataType(compType), "Day", "" );
						} catch (Exception e) {
							message = "Unable to create time series identifier for \"" + id + "\" (" + e + ").";
							Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( command_tag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
						}
					}
					*/
					try {
						ts.setIdentifier ( tsident );
					} catch (Exception e) {
						message = "Unable to set time series identifier for \"" + tsident + "\" (" + e + ").";
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warningCount),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
					}
					ts.setDate1 ( OutputStart_DateTime );
					ts.setDate1Original ( OutputStart_DateTime );
					ts.setDate2 ( OutputEnd_DateTime );
					ts.setDate2Original ( OutputEnd_DateTime );
					ts.allocateDataSpace ();
					Message.printStatus ( 2, routine, "No HydroBase diversion time series data " +
					"found for requested ID \"" + id + "\".  Adding an empty time series." );
					blank_ts_created = true;
				}
	
				// Will always have a time series by here...
				if ( is_wdid && UseDiversionComments.equalsIgnoreCase(_True)) {
					try {
						HydroBase_Util.fillTSUsingDiversionComments ( hbdmi, ts, null, null );
					} catch (Exception e) {
						message = "Error filling time series with diversion comments (" + e + ").";
						Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( command_tag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
					}
				}
				// Check for CIU flag
				if ( is_wdid && FillUsingCIU != null && FillUsingCIU.equalsIgnoreCase( "true" ) ) {
					warningCount = fillUsingCIUFlag( ts, hbdmi, 
						FillUsingCIUFlag, OutputStart_DateTime, OutputEnd_DateTime,
						command_tag, warningLevel, warningCount, status, ignore_lezero_flag );
				}
				// Calculate the limits for use when filling with historical averages...
	
				if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY && !fillUsingCIU ) {
					recalculateLimits ( ts, OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag );
				}
			} // End single ditch
	
			// TODO SAM 2005-08-13 Can this be removed?  Or are there cases that could still
			// cause the following to be executed?
			// Final fall-through.
			// If the time series is null, create a blank one to keep the
			// sequence the same with the diversion identifiers...
	
			if ( ts == null ) {
				if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
					ts = new MonthTS ();
					ts.setDataUnits (  StateMod_DataSet.lookupTimeSeriesDataUnits( compType ));
					try {
						tsident = new TSIdent ( id, "DWR",
							StateMod_DataSet.lookupTimeSeriesDataType(compType),"Month", "" );
					} catch (Exception e) {
						message = "Error creating time series identifier for \"" + id + "\" (" + e + ").";
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warningCount),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
					}
				}
				/* TODO SAM 2011-01-16 Need to finish
				else {
					ts = new DayTS ();
					ts.setDataUnits ( StateMod_DataSet.lookupTimeSeriesDataUnits(compType) );
					try {
						tsident = new TSIdent ( id, "DWR",
							StateMod_DataSet.lookupTimeSeriesDataType( compType), "Day", "" );
					} catch (Exception e) {
						message = "Couldn't find time series data type for " + id;
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warningCount),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
					}
				}
				*/
				try {
					ts.setIdentifier ( tsident );
				} catch (Exception e) {
					message = "Couldn't set time series identifier \"" + tsident + "\".";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( command_tag, ++warningCount),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Report problem to software support." ) );
				}
				ts.setDate1 ( OutputStart_DateTime );
				ts.setDate1Original ( OutputStart_DateTime );
				ts.setDate2 ( OutputEnd_DateTime );
				ts.setDate2Original ( OutputEnd_DateTime );
				ts.allocateDataSpace ();
				//if ( adding_blank ) {
				//	Message.printStatus ( 2, routine, "ID \"" + id + "\" not requested from " +
				//	"HydroBase.  Adding an empty diversion time series." );
				//}
				//else {	
					Message.printStatus ( 2, routine,
					"No diversion time series data found for requested ID \"" +
					id + "\".  Adding an empty time series." );
				//}
			}
	
			// Add the time series to the list...
	
			if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
				processor.findAndAddSMDiversionTSMonthly ( ts, true );
				// Save a backup copy used when constraining to water rights...
				if ( processor.__need_diversion_ts_monthly_copy ) {
					// If an explicit station, add the total...
					if ( !div.isCollection() ) {
						processor.findAndAddSMDiversionTSMonthly2 ( (MonthTS)ts.clone(), true );
					}
					else {
						// For a collection add the original sum (no need to clone since this is
						// the only place that the data will be used)...
						processor.findAndAddSMDiversionTSMonthly2 ( sumts, true );
					}
				}
			}
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading diversion historical time series (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warningCount),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                 message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	
	if ( props == null ) {
		return getCommandName() + "()";
	}
	String ID = props.getValue ( "ID" );
	String IncludeExplicit = props.getValue ( "IncludeExplicit" );
	String IncludeCollections = props.getValue ( "IncludeCollections" );
	String UseDiversionComments = props.getValue ( "UseDiversionComments");
	String ReadStart = props.getValue ( "ReadStart");
	String ReadEnd = props.getValue ( "ReadEnd");
	String LEZeroInAverage = props.getValue ( "LEZeroInAverage");
	String PatternID = props.getValue ( "PatternID");
	String FillPatternOrder = props.getValue ( "FillPatternOrder");
	String PatternFillFlag = props.getValue ( "PatternFillFlag");
	String FillAverageOrder = props.getValue ( "FillAverageOrder");
	String AverageFillFlag = props.getValue ( "AverageFillFlag");
	String FillUsingCIU = props.getValue( "FillUsingCIU" );
	String FillUsingCIUFlag = props.getValue( "FillUsingCIUFlag" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( IncludeExplicit != null && IncludeExplicit.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeExplicit=" + IncludeExplicit );
	}
	if ( IncludeCollections != null && 
		 IncludeCollections.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeCollections=" + IncludeCollections );
	}
	if ( LEZeroInAverage != null && LEZeroInAverage.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LEZeroInAverage=" + LEZeroInAverage );
	}
	if ( UseDiversionComments != null && UseDiversionComments.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseDiversionComments=" + UseDiversionComments );
	}
	if ( FillUsingCIU != null && FillUsingCIU.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillUsingCIU=\"" + FillUsingCIU + "\"" );
	}
	if ( FillUsingCIUFlag != null && FillUsingCIUFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillUsingCIUFlag=" + FillUsingCIUFlag );
	}
	if ( ReadStart != null && ReadStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReadStart=\"" + ReadStart + "\"" );
	}
	if ( ReadEnd != null && ReadEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReadEnd=\"" + ReadEnd + "\"" );
	}
	if ( PatternID != null && PatternID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PatternID=\"" + PatternID + "\"" );
	}
	if ( FillPatternOrder != null && FillPatternOrder.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillPatternOrder=" + FillPatternOrder );
	}
	if ( PatternFillFlag != null && PatternFillFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PatternFillFlag=\"" + PatternFillFlag + "\"" );
	}
	if ( FillAverageOrder != null && FillAverageOrder.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillAverageOrder=" + FillAverageOrder );
	}
	if ( AverageFillFlag != null && AverageFillFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AverageFillFlag=\"" + AverageFillFlag + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}