// FillTS_Command - This class initializes, checks, and runs the Fill*TS*() commands.

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

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Util;

import RTi.TS.MonthTS;
import RTi.TS.StringMonthTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
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
<p>
This class initializes, checks, and runs the Fill*TS*() commands.
It is an abstract base class that must be controlled via a derived class.  For example,
the FillDiversionHistoricalTSMonthlyConstant() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
</p>
*/
public abstract class FillTS_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Values for FillFlag parameter.
*/
protected final String _Auto = "Auto";

/**
Start of set period.
*/
private DateTime __FillStart_DateTime = null;

/**
End of set period.
*/
private DateTime __FillEnd_DateTime = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillTS_Command ()
{	super();
	setCommandName ( "Fill?TS?" );
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
	String IncludeCollections = parameters.getValue ( "IncludeCollections" );
	String Constant = parameters.getValue ( "Constant" );
	String PatternID = parameters.getValue ( "PatternID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The station ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the station ID to process." ) );
	}
	
	if ( (this instanceof FillDiversionHistoricalTSMonthlyAverage_Command) || 
		(this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ) {
		if ( (IncludeCollections != null) && (IncludeCollections.length() > 0) && 
			!IncludeCollections.equalsIgnoreCase(_False) && !IncludeCollections.equalsIgnoreCase(_True) ) {
			message = "The IncludeCollections value (" + IncludeCollections + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IncludeCollections as " + _False + " (default) or " + _True + ".") );
		}
	}
	
	if ( (this instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(this instanceof FillWellDemandTSMonthlyConstant_Command) ) {
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
	
	if ( (this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(this instanceof FillWellDemandTSMonthlyPattern_Command) ) {
		if ( (PatternID == null) || (PatternID.length() == 0) ) {
	        message = "The pattern ID must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the pattern ID to use for filling." ) );
		}

		if ( (LEZeroInAverage != null) && (LEZeroInAverage.length() > 0) && 
			!LEZeroInAverage.equalsIgnoreCase(_False) && !LEZeroInAverage.equalsIgnoreCase(_True) ) {
			message = "The LEZeroInAverage value (" + LEZeroInAverage + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify LEZeroInAverage as " + _False + " or " + _True + " (default).") );
		}
	}
	
	if ( (FillStart != null) && !FillStart.equals("") && !FillStart.equalsIgnoreCase("OutputStart")){
		try {
			__FillStart_DateTime = DateTime.parse(FillStart);
		}
		catch ( Exception e ) {
            message = "The fill start date \"" + FillStart + "\" is not a valid date.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a valid date." ) );
		}
	}
	if ( (FillEnd != null) && !FillEnd.equals("") && !FillEnd.equalsIgnoreCase("OutputEnd") ) {
		try {
			__FillEnd_DateTime = DateTime.parse( FillEnd);
		}
		catch ( Exception e ) {
            message = "The fill end date \"" + FillStart + "\" is not a valid date.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a valid date." ) );
		}
	}
	
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		if ( (this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
			(this instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
			(this instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
			(this instanceof FillWellDemandTSMonthlyPattern_Command) ) {
			// Allow Auto
			if ( FillFlag.equalsIgnoreCase(_Auto) ) {
				//OK
			}
			else if ( FillFlag.length() != 1 ) {
				message = "The FillFlag value (" + FillFlag + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify FillFlag as Auto or 1 character (default=none).") );
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
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail + ", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "ID" );
	if ( (this instanceof FillDiversionHistoricalTSMonthlyAverage_Command) || 
		(this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ) {
		valid_Vector.add ( "IncludeCollections" );
	}
	if ( (this instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(this instanceof FillWellDemandTSMonthlyConstant_Command) ) {
		valid_Vector.add ( "Constant" );
	}
	if ( (this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(this instanceof FillWellDemandTSMonthlyPattern_Command) ) {
		valid_Vector.add ( "PatternID" );
		valid_Vector.add ( "LEZeroInAverage" );
	}
	valid_Vector.add ( "FillStart" );
	valid_Vector.add ( "FillEnd" );
	valid_Vector.add ( "FillFlag" );
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
	return (new FillTS_JDialog ( parent, this )).ok();
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
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IncludeCollections = parameters.getValue ( "IncludeCollections" );
	boolean IncludeCollections_boolean = true;
	if ( (IncludeCollections != null) && IncludeCollections.equalsIgnoreCase(_False) ) {
		IncludeCollections_boolean = false;
	}
	String Constant = parameters.getValue ( "Constant" );
	double Constant_double = 0.0;
	if ( StringUtil.isDouble(Constant) ) {
		Constant_double = Double.parseDouble(Constant);
	}
	String PatternID = parameters.getValue ( "PatternID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	boolean LEZeroInAverage_boolean = true;
	if ( (LEZeroInAverage != null) && LEZeroInAverage.equalsIgnoreCase(_False) ) {
		LEZeroInAverage_boolean = false;
	}
	String FillFlag = parameters.getValue ( "FillFlag" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	// SetStart and SetEnd processed in checkCommandParameters()
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}

	// Set up the properties for filling...

	PropList fill_props = new PropList ( "fill" );
	if ( FillFlag != null ) {
		fill_props.set ( "FillFlag", FillFlag );
	}
	if ( !LEZeroInAverage_boolean ) {
		// Ignore the values...
		fill_props.set ( "IgnoreLessThanOrEqualZero", "True" );
	}
	else {	// Do not ignore the values...
		fill_props.set ( "IgnoreLessThanOrEqualZero", "False" );
	}

    // Get the data needed for the command
    
    List<TS> tsList = null;
    int tsListSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN;
    String dataType = "";
    try {
    	if ( (this instanceof FillDiversionHistoricalTSMonthlyAverage_Command) ||
			(this instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
			(this instanceof FillDiversionHistoricalTSMonthlyPattern_Command)) {
    		@SuppressWarnings("unchecked")
			List<TS> dataList = (List<TS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
    		tsList = dataList;
    		dataType = "diversion historical";
    		compType = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
    	}
    	else if ( (this instanceof FillDiversionDemandTSMonthlyAverage_Command) ||
			(this instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
			(this instanceof FillDiversionDemandTSMonthlyPattern_Command) ) {
			@SuppressWarnings("unchecked")
			List<TS> dataList = (List<TS>)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
			tsList = dataList;
			dataType = "diversion demand";
			compType = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
		}
    	else if ( (this instanceof FillWellHistoricalPumpingTSMonthlyAverage_Command) ||
			(this instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
			(this instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command)){
			@SuppressWarnings("unchecked")
			List<TS> dataList = (List<TS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
			tsList = dataList;
			compType = StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY;
		}
    	else if ( (this instanceof FillWellDemandTSMonthlyAverage_Command) ||
			(this instanceof FillWellDemandTSMonthlyConstant_Command) ||
			(this instanceof FillWellDemandTSMonthlyPattern_Command) ){
			@SuppressWarnings("unchecked")
			List<TS> dataList = (List<TS>)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
			tsList = dataList;
			dataType = "well demand";
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
    
    // Get the diversion stations if needed to check collections
    List<StateMod_Diversion> divStationList = null;
    if ( !IncludeCollections_boolean && (compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) ) {
    	try {
    		@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		divStationList = dataList;
    	}
        catch ( Exception e ) {
            Message.printWarning ( log_level, routine, e );
            message = "Error requesting diversion station list to check for collections (" + e + ").";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report to software support.  See log file for details." ) );
        }
    }
    
    // Set some booleans to increase processing speed
    boolean fillAverage = false;
    boolean fillConstant = false;
    boolean fillPattern = false;
	if ( (this instanceof FillDiversionHistoricalTSMonthlyAverage_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyAverage_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyAverage_Command) ||
		(this instanceof FillWellDemandTSMonthlyAverage_Command) ) {
		fillAverage = true;
	}
	else if ( (this instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(this instanceof FillWellDemandTSMonthlyConstant_Command) ) {
		fillConstant = true;
	}
	else if ( (this instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(this instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(this instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(this instanceof FillWellDemandTSMonthlyPattern_Command) ) {
		fillPattern = true;
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
	DateTime FillStart_DateTime = OutputStart_DateTime;
	if ( __FillStart_DateTime != null ) {
		FillStart_DateTime = __FillStart_DateTime;
	}
	DateTime FillEnd_DateTime = OutputEnd_DateTime;
	if ( __FillEnd_DateTime != null ) {
		FillEnd_DateTime = __FillEnd_DateTime;
	}
	
    // Get the existing list of pattern time series - not used directly here but needed for check
	if ( fillPattern ) {
	    List<StringMonthTS> patternList = null;
	    int patternListSize = 0;
	    try {
	   		@SuppressWarnings("unchecked")
			List<StringMonthTS> dataList = (List<StringMonthTS>)processor.getPropContents ( "StateMod_PatternTSMonthly_List" );
	   		patternList = dataList;
	   		patternListSize = patternList.size();
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting pattern time series list from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
	    if ( patternListSize == 0 ) {
	        message = "No pattern time series have been read - will not be able to fill with pattern.";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that pattern time series have been read with ReadPatternFile() prior" +
	                	" to this command." ) );
	    }
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	TS ts = null;
    	String id;
    	int pos = 0; // Used when finding stations
    	StateMod_Diversion div = null; // StateMod diversion to process

    	// Loop through available objects and see if any need to be filled...
    	int matchCount = 0;
    	for ( int i = 0; i < tsListSize; i++ ) {
    		ts = (TS)tsList.get(i);
       		id = ts.getLocation();
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		if ( !IncludeCollections_boolean ) {
    			// Need to ignore diversion stations that are collections...
    			if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
    				pos = StateMod_Util.indexOf(divStationList,id);
    				if ( pos >= 0 ) {
    					div = (StateMod_Diversion)divStationList.get(pos);
    					if ( div.isCollection() ) {
    						Message.printStatus ( 2, routine,
    						"Not filling time series for \"" + id + "\" (it is a collection)." );
    						continue;
    					}
    				}
    			}
    		}
    		// Reset the data...
    		if ( fillAverage ) {
    			try {
	   				warning_count = processor.fillTSMonthlyAverage ( command_tag, warning_count, (MonthTS)ts,
	   				routine, id, null, dataType, FillStart_DateTime, FillEnd_DateTime, fill_props );
    			}
    			catch ( Exception e ) {
    				message = "Error filling " + id + " with monthly average.";
    		        Message.printWarning ( warning_level, 
		    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( CommandPhaseType.RUN,
	    	            new CommandLogRecord(CommandStatusType.WARNING,
	    	                message, "This is typically due to having no data in time series - " +
	    	                	"verify that original data are available because historical averages" +
	    	                	" are only computed from raw data (not filled data)." ) );
    			}
    		}
    		else if ( fillConstant ) {
    			Message.printStatus ( 2, routine, "Filling missing data in " + id +
    			" diversion TS with constant " + Constant );
    			TSUtil.fillConstant ( ts, FillStart_DateTime, FillEnd_DateTime, Constant_double, fill_props );
    		}
    		else if ( fillPattern ) {
    			try {
    				processor.fillTSPattern ( ts, routine, PatternID,
    				id, null, dataType, FillStart_DateTime, FillEnd_DateTime, fill_props,
    				warning_level, warning_count, command_tag, status );
    			}
    			catch ( Exception e ) {
    				message = "Error filling " + id + " with pattern.";
    		        Message.printWarning ( warning_level, 
		    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( CommandPhaseType.RUN,
	    	            new CommandLogRecord(CommandStatusType.WARNING,
	    	                message, "This is typically due to having no data in time series, or" +
	    	                	" trying to use a bad pattern ID - verify that data are available." ) );
    			}
    		}
    		// Increment the count
    		++matchCount;
    	}
    	if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct or use IfNotFound=" + _Ignore + "." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID +	"\" was not matched: failing and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is corrector use IfNotFound=" + _Ignore + "." ) );
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
	String IncludeCollections = parameters.getValue ( "IncludeCollections" );
	String Constant = parameters.getValue ( "Constant" );
	String PatternID = parameters.getValue ( "PatternID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (IncludeCollections != null) && (IncludeCollections.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IncludeCollections=" + IncludeCollections );
	}
	if ( (Constant != null) && (Constant.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Constant=" + Constant );
	}
	if ( (PatternID != null) && (PatternID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PatternID=\"" + PatternID + "\"" );
	}
	if ( (LEZeroInAverage != null) && (LEZeroInAverage.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LEZeroInAverage=" + LEZeroInAverage );
	}
	if ( (FillStart != null) && (FillStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillStart=\"" + FillStart + "\"" );
	}
	if ( (FillEnd != null) && (FillEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillEnd=\"" + FillEnd + "\"" );
	}
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillFlag=" + FillFlag );
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
