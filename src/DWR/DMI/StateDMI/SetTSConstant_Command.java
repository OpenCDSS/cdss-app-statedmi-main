package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_DataSet;

import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.YearType;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the SetInstreamFlowDemandTSAverageMonthlyConstant() command.
It is an abstract base class that must be controlled via a derived class.  For example,
the SetInstreamFlowDemandTSAverageMonthlyConstant() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
*/
public abstract class SetTSConstant_Command extends AbstractCommand implements Command
{
	
/**
Parameter values used with RecalcLimits.
*/
protected final String _False = "False";
protected final String _True = "True";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for MonthValues, created in checkCommandParamers() and used when running.
*/
private Double [] __MonthValues_double = null;

/**
Start of set period.
*/
private DateTime __SetStart_DateTime = null;

/**
End of set period.
*/
private DateTime __SetEnd_DateTime = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetTSConstant_Command ()
{	super();
	setCommandName ( "Set?TS?Constant" );
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
{	//String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String Constant = parameters.getValue ( "Constant" );
	String MonthValues = parameters.getValue ( "MonthValues" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String RecalcLimits = parameters.getValue ( "RecalcLimits" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The instream flow station ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the instream flow station ID to process." ) );
	}
	
	if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
		if ( (MonthValues == null) || (MonthValues.length() == 0) ) {
	        message = "The monthly values must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify 12 monthly (Jan-Dec) values for setting the time series." ) );
		}
		else {
			List v = StringUtil.breakStringList ( MonthValues,", ", StringUtil.DELIM_SKIP_BLANKS );
			if ( (v == null) || (v.size() != 12) ) {
				message = "12 monthly values must be specified.";
				warning += "\n" + message;
		        status.addToLog ( CommandPhaseType.INITIALIZATION,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Specify 12 monthly (Jan-Dec) values for setting the time series." ) );
			}
			else {
				String val;
				__MonthValues_double = new Double[12];
				for ( int i = 0; i < 12; i++ ) {
					val = (String)v.get(i);
					if ( !StringUtil.isDouble(val) ) {
						message = "Monthly value \"" + val + " is not a number.";
						warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify 12 monthly (Jan-Dec) values for setting the time series." ) );
					}
					else {
						__MonthValues_double[i] = Double.parseDouble(val);
					}
				}
			}
		}
	}
	else {
		// Constant value expected.
		if ( (Constant == null) || (Constant.length() == 0) ) {
	        message = "The constant value must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify value for setting the time series." ) );
		}
		else if ( !StringUtil.isDouble(Constant)){
	        message = "The constant value (" + Constant + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the constant value as a number." ) );
		}
	}
	
	if ( (SetStart != null) && !SetStart.equals("") && !SetStart.equalsIgnoreCase("OutputStart")){
		try {
			__SetStart_DateTime = DateTime.parse(SetStart);
		}
		catch ( Exception e ) {
            message = "The set start date/time \"" + SetStart + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid date/time or OutputStart." ) );
		}
	}
	if ( (SetEnd != null) && !SetEnd.equals("") && !SetEnd.equalsIgnoreCase("OutputEnd") ) {
		try {
			__SetEnd_DateTime = DateTime.parse( SetEnd);
		}
		catch ( Exception e ) {
            message = "The set end date/time \"" + SetStart + "\" is not a valid date/time.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid date/time or OutputEnd." ) );
		}
	}
	
    if ( (RecalcLimits != null) && !RecalcLimits.equals("") &&
        !RecalcLimits.equalsIgnoreCase( "true" ) && !RecalcLimits.equalsIgnoreCase("false") ) {
        message = "The RecalcLimits parameter must be blank, " + _False + " (default), or " + _True + ".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a 1-character fill flag or Auto." ) );
    }

	// Include the Add option
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
	valid_Vector.add ( "ID" );
	if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
		valid_Vector.add ( "MonthValues" );
	}
	else {
		valid_Vector.add ( "Constant" );
	}
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
    valid_Vector.add ( "RecalcLimits" );
	valid_Vector.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new SetTSConstant_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String MonthValues = parameters.getValue ( "MonthValues" );
	String Constant = parameters.getValue ( "Constant" );
	double Constant_double = 0.0;
	if ( StringUtil.isDouble(Constant) ) {
		Constant_double = Double.parseDouble(Constant);
	}
    String RecalcLimits = parameters.getValue ( "RecalcLimits" );
    boolean RecalcLimits_boolean = true;   // Default
    if ( (RecalcLimits != null) && RecalcLimits.equalsIgnoreCase("false") ) {
        RecalcLimits_boolean = false;
    }
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	// SetStart and SetEnd processed in checkCommandParameters()
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List tsList = null;
    int tsListSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN;
    try {
    	if ( this instanceof SetDiversionHistoricalTSMonthlyConstant_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
    		compType = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
    	}
		else if ( this instanceof SetDiversionDemandTSMonthlyConstant_Command ){
			tsList = (List)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
			compType = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
		}
		else if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
			tsList = (List)processor.getPropContents ( "StateMod_InstreamFlowDemandTSAverageMonthly_List" );
			compType = StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY;
		}
		else if ( this instanceof SetWellHistoricalPumpingTSMonthlyConstant_Command ) {
			tsList = (List)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
			compType = StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY;
		}
		else if ( this instanceof SetWellDemandTSMonthlyConstant_Command ){
			tsList = (List)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
			compType = StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY;
		}
    	tsListSize = tsList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Output period will be used if not specified with SetStart and SetEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
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
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	// Output year type is needed to make sure that the output period is OK.
	
	YearType outputYearType = YearType.CALENDAR;
    try {
	   outputYearType = (YearType)processor.getPropContents ( "OutputYearType");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputYearType (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	// Adjust for average monthly values...
	
	if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
		// Dates are year zero because average values, not a full time series
		OutputStart_DateTime = new DateTime(DateTime.PRECISION_MONTH|DateTime.DATE_ZERO);
		OutputEnd_DateTime = new DateTime(DateTime.PRECISION_MONTH|DateTime.DATE_ZERO);
		OutputEnd_DateTime.setMonth(12);
		if (outputYearType == YearType.WATER){
			OutputStart_DateTime.setMonth ( 10 );
			OutputEnd_DateTime.setYear ( 1 );
			OutputEnd_DateTime.setMonth ( 9 );
		}
		else if (outputYearType == YearType.NOV_TO_OCT){
			OutputStart_DateTime.setMonth ( 11 );
			OutputEnd_DateTime.setYear ( 1 );
			OutputEnd_DateTime.setMonth ( 10 );
		}
	}
	
	// Whether historical diversions need to be copied due to data filling...
	
	Boolean needToCopyDiversionHistoricalTSMonthly = null;
	try {
		Object o = processor.getPropContents( "NeedToCopyDiversionHistoricalTSMonthly");
		needToCopyDiversionHistoricalTSMonthly = (Boolean)o;
	}
	catch ( Exception e ) {
		message = "Error requesting NeedToCopyDiversionHistoricalTSMonthly property from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Get the set start and end.

    	DateTime SetStart_DateTime = null;
    	if ( __SetStart_DateTime != null ) {
    		SetStart_DateTime = new DateTime(__SetStart_DateTime);
    	}
    	else if ( OutputStart_DateTime != null ) {
    		SetStart_DateTime = new DateTime(OutputStart_DateTime);
    	}
    	if ( SetStart_DateTime != null ) {
    		SetStart_DateTime.setPrecision ( DateTime.PRECISION_MONTH );
    	}
    	DateTime SetEnd_DateTime = null;
    	if ( __SetEnd_DateTime != null ) {
    		SetEnd_DateTime = new DateTime(__SetEnd_DateTime);
    	}
    	else if ( OutputEnd_DateTime != null ) {
    		SetEnd_DateTime = new DateTime(OutputEnd_DateTime);
    	}
    	if ( SetEnd_DateTime != null ) {
    		SetEnd_DateTime.setPrecision ( DateTime.PRECISION_MONTH );
    	}

    	String id = "";
    	int matchCount = 0;
    	MonthTS ts = null;	// Time series to set/add
    	for ( int i = 0; i < tsListSize; i++ ) {
    		ts = (MonthTS)tsList.get(i);
    		id = ts.getLocation();
    		// Uncomment for debugging...
    		//Message.printStatus ( 2, routine, "Checking TS location \"" +
    			//id + "\" against pattern \"" + idpattern_Java + "\"");
    		if ( (idpattern_Java.indexOf("*") >= 0) && !id.matches(idpattern_Java) ) {
    			// Trying to match existing time series with a wildcard
    			// and the identifier does not match...
    			continue;
    		}
    		if ( id.matches(idpattern_Java) ) {
    			++matchCount;
    			// Just reset the existing time series...
    			if ( MonthValues != null ) {
    				// Set to monthly constant values...
    				Message.printStatus ( 2, routine, "Setting " + id + " (updating) TS -> " + MonthValues );
    				TSUtil.setConstantByMonth ( ts, __MonthValues_double );
    			}
    			else {
    				// Set to a single constant...
    				Message.printStatus ( 2, routine, "Setting " + id + " (updating) TS -> " + Constant +
    					" for " + SetStart_DateTime + " to " + SetEnd_DateTime );
    				TSUtil.setConstant ( ts, SetStart_DateTime, SetEnd_DateTime, Constant_double );
    			}
    			// Update the limits (default is true but can be set to false by the user).
    			if ( RecalcLimits_boolean ) {
    	    		// Calculate the original data limits since this is a new time series
    	    		int ignore_lezero_flag = 0; // Used by TSUtil code
    	  			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    	    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    	 			// Make a new copy of the time series needed for limiting to rights later...
    	  			if ( this instanceof SetDiversionHistoricalTSMonthlyConstant_Command) {
    	    			if ( needToCopyDiversionHistoricalTSMonthly.booleanValue() ) {
    	    				processor.findAndAddSMDiversionTSMonthly2 (	(MonthTS)ts.clone(), true );
    	    			}
    	  			}
    			}
    		}
    	}
    	boolean datesOk = true;
    	if ( (matchCount == 0) && (idpattern_Java.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase(_Add)) {
    		// Did not find the time series to edit and not a wild-card.  Add a new time series...
    		if ( OutputStart_DateTime == null ) {
    			message = "Cannot create new time series for " + ID + ".  Need to set output start.";
      	        Message.printWarning ( warning_level, 
	                MessageUtil.formatMessageTag(command_tag, ++warning_count),
	                routine, message );
	                status.addToLog ( command_phase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Use SetOutputPeriod() command prior to this command." ) );
    			datesOk = false;
    		}
    		if ( OutputEnd_DateTime == null ) {
    			message = "Cannot create new time series for " + ID + ".  Need to set output end.";
      	        Message.printWarning ( warning_level, 
	                MessageUtil.formatMessageTag(command_tag, ++warning_count),
	                routine, message );
	                status.addToLog ( command_phase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Use SetOutputPeriod() command prior to this command." ) );
    			datesOk = false;
    		}
    		if ( datesOk ) {
    			Message.printStatus(2, routine,
    				"The location was not matched.  Adding new time series for \"" + ID + "\"" );
	     		TSIdent tsident = null;
	    		tsident = new TSIdent (
	    			ID,
	    			"",
	    			StateMod_DataSet.lookupTimeSeriesDataType (
	    			compType ),
	    			"Month",
	    			"" );	// No scenario
	    		// Now add the time series...
	    		ts = new MonthTS();
	    		ts.setIdentifier ( tsident );
	    		ts.setDescription ( ID );
	    		// Create the time series to match the output period (not the set period)...
	    		DateTime date1 = new DateTime(OutputStart_DateTime);
	    		DateTime date2 = new DateTime(OutputEnd_DateTime);
	    		if ( compType == StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY ) {
	    			// Only 12 values in each time series...
	    			date1.setYear ( 0 );
	    			date2.setYear ( 0 );
    				if (outputYearType == YearType.WATER){
    					date1.setMonth ( 10 );
    					date2.setYear ( 1 );
    					date2.setMonth ( 9 );
    				}
    				else if (outputYearType == YearType.NOV_TO_OCT){
    					date1.setMonth ( 11 );
    					date2.setYear ( 1 );
    					date2.setMonth ( 10 );
    				}
	    		}
	    		ts.setDate1 ( date1 );
	    		ts.setDate2 ( date2 );
	    		ts.setDataUnits ( StateMod_DataSet.lookupTimeSeriesDataUnits ( compType));
	    		ts.allocateDataSpace ();
	    		if ( this instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
	    			TSUtil.setConstantByMonth ( ts, __MonthValues_double );
	    			tsList.add ( ts );
	    			Message.printStatus ( 2, routine, "Setting " + ID +
	    				" (new) instream demand TS -> " + MonthValues );
	    		}
	    		else if(this instanceof SetDiversionHistoricalTSMonthlyConstant_Command){
	    			TSUtil.setConstant ( ts, SetStart_DateTime, SetEnd_DateTime, Constant_double );
	    			tsList.add ( ts );
	    			Message.printStatus ( 2, routine,
	    				"Setting " + id + " (new) diversion historical TS -> " +Constant);
	    		}
	    		else if( this instanceof SetDiversionDemandTSMonthlyConstant_Command ){
	    			TSUtil.setConstant ( ts, SetStart_DateTime, SetEnd_DateTime, Constant_double );
	    			tsList.add ( ts );
	    			Message.printStatus ( 2, routine,
	    				"Setting " + id + " (new) diversion demand TS -> " +Constant);
	    		}
	    		else if(compType==	StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY){
	    			TSUtil.setConstant ( ts, SetStart_DateTime, SetEnd_DateTime, Constant_double );
	    			tsList.add(ts);
	    			Message.printStatus ( 2, routine,
	    				"Setting " + id + " (new) well historical pumping TS -> " + Constant);
	    		}
	    		else if( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ){
	    			TSUtil.setConstant ( ts, SetStart_DateTime, SetEnd_DateTime, Constant_double );
	    			tsList.add ( ts );
	    			Message.printStatus ( 2, routine,
	    				"Setting " + id + " (new) well demand TS -> " + Constant);
	    		}
	    		// Calculate the original data limits since this is a new time series
	    		int ignore_lezero_flag = 0; // Used by TSUtil code
	  			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
	    				OutputStart_DateTime, OutputEnd_DateTime,
	    				ignore_lezero_flag ) );
	 			// Make a copy of the time series needed for limiting to rights later...
	  			if(this instanceof SetDiversionHistoricalTSMonthlyConstant_Command){
	    			if ( needToCopyDiversionHistoricalTSMonthly.booleanValue() ) {
	    				processor.findAndAddSMDiversionTSMonthly2 (	(MonthTS)ts.clone(), true );
	    			}
	  			}
	    		// Increment to prevent warning at end...
	    		++matchCount;
    		}
    	}
    	if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct or use IfNotFound=" + _Add + "." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID +	"\" was not matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is corrector use IfNotFound=" + _Add + "." ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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
	String Constant = parameters.getValue ( "Constant" );
	String MonthValues = parameters.getValue ( "MonthValues" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String RecalcLimits = parameters.getValue ( "RecalcLimits" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (Constant != null) && (Constant.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Constant=" + Constant );
	}
	if ( (MonthValues != null) && (MonthValues.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MonthValues=\"" + MonthValues + "\"" );
	}
	if ( (SetStart != null) && (SetStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetStart=\"" + SetStart + "\"" );
	}
	if ( (SetEnd != null) && (SetEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEnd=\"" + SetEnd + "\"" );
	}
	if ( (RecalcLimits != null) && (RecalcLimits.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RecalcLimits=" + RecalcLimits );
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}