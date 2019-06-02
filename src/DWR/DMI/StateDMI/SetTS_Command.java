// SetTS_Command - This class initializes, checks, and runs the Set*TS() commands, to set time series from data in a file.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_TS;

import RTi.TS.DateValueTS;
//import RTi.TS.DayTS;
import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSLimits;
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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the Set*TS() commands, to set time series from data in a file.
It is an abstract base class that must be controlled via a derived class.  For example,
the SetInstreamFlowDemandTSAverageMonthlyConstant() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
</p>
*/
public abstract class SetTS_Command extends AbstractCommand implements Command
{

/**
Values for LEZeroInAverage parameter.
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
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetTS_Command ()
{	super();
	setCommandName ( "Set?TS" );
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
	String TSID = parameters.getValue ( "TSID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
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
                        message, "Specify the station ID to set time series." ) );
	}
	
	if ( (TSID == null) || (TSID.length() == 0) ) {
        message = "The time series identifeir (TSID) must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the time series ID (TSID) indicating the file to read." ) );
	}
	
	if ( (LEZeroInAverage != null) && (LEZeroInAverage.length() > 0) &&
		!LEZeroInAverage.equalsIgnoreCase(_False) && !LEZeroInAverage.equalsIgnoreCase(_True) ) {
		message = "The LEZeroInAverage value (" + LEZeroInAverage + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify LEZeroInAverage as " + _False + " or " + _True + " (default).") );
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
	List<String> valid_Vector = new Vector<String>(4);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "TSID" );
	valid_Vector.add ( "LEZeroInAverage" );
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
	return (new SetTS_JDialog ( parent, this )).ok();
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
	String TSID = parameters.getValue ( "TSID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	int ignore_lezero_flag = 0; // Used by TSUtil code
	if ( LEZeroInAverage == null ) {
		LEZeroInAverage = _True; // Default
	}
	if ( LEZeroInAverage.equalsIgnoreCase(_False) ) {
		ignore_lezero_flag = TSLimits.IGNORE_LESS_THAN_OR_EQUAL_ZERO;
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
    // Get the data needed for the command
    
    List<MonthTS> tsList = null;
    int tsListSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // Check this instead of instanceof to increase performance
    try {
       	if ( this instanceof SetDiversionHistoricalTSMonthly_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
    		tsList = dataList;
    		compType = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
    	}
		else if ( this instanceof SetDiversionDemandTSMonthly_Command ){
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
			tsList = dataList;
			compType = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
		}
		else if ( this instanceof SetWellHistoricalPumpingTSMonthly_Command ) {
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
			tsList = dataList;
			compType = StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY;
		}
		else if ( this instanceof SetWellDemandTSMonthly_Command ){
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
			tsList = dataList;
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
	
	// Get the HydroBase DMI...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
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
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Read the time series...

    	TSIdent tsident = new TSIdent ( TSID );
    	String inputType = tsident.getInputType();
    	TS filets = null;	// Time series read from file.
    	// TODO SAM 2004-06-19 - maybe call a TSTool method?
    	if ( inputType.equalsIgnoreCase("DateValue") ) {
    		String inputNameFull = IOUtil.verifyPathForOS(
		        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
	                StateDMICommandProcessorUtil.expandParameterValue(processor,this,tsident.getInputName())));
    		filets = DateValueTS.readTimeSeries ( tsident.getIdentifier(), inputNameFull,
				OutputStart_DateTime, OutputEnd_DateTime, null, true );
    	}
    	else if ( inputType.equalsIgnoreCase("HydroBase") ) {
    		filets = hbdmi.readTimeSeries ( tsident.getIdentifier(), OutputStart_DateTime,
    			OutputEnd_DateTime, null, true, null );
    	}
    	else if ( inputType.equalsIgnoreCase("StateMod") ) {
    		String inputNameFull = IOUtil.verifyPathForOS(
		        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
	                StateDMICommandProcessorUtil.expandParameterValue(processor,this,tsident.getInputName())));
    		filets = StateMod_TS.readTimeSeries ( tsident.getIdentifier(), inputNameFull,
				OutputStart_DateTime, OutputEnd_DateTime, null, true );
    	}
    	else {
    		message = "TSID has unrecognized input type (" + inputType + ").";
    		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify input type as HydroBase, DateValue, or StateMod." ) );
    	}

    	if ( filets == null ) {
    		message = "Time series read from file using TSID \"" + TSID + "\" is null.";
    		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the time series file is accessible (try using " +
						"the TSTool software to check file)." ) );
    	}

    	// Extend the period if necessary...

    	DateTime date1 = new DateTime ( OutputStart_DateTime );
    	if ( filets.getDate1().lessThan(date1) ) {
    		date1 = new DateTime ( filets.getDate1() );
    	}
    	DateTime date2 = new DateTime ( OutputEnd_DateTime );
    	if ( filets.getDate2().greaterThan(date2) ) {
    		date2 = new DateTime ( filets.getDate2() );
    	}
    	// This will do nothing if the period is the same as the data...
    	filets.changePeriodOfRecord ( date1, date2 );

    	if ( inputType.equalsIgnoreCase("HydroBase") ) {
    		// By default, fill with diversion comments...
    		try {
	    		if ( (filets != null) && HydroBase_WaterDistrict.isWDID(tsident.getLocation()) ){
	    			HydroBase_Util.fillTSUsingDiversionComments(hbdmi, filets, null, null );
	    		}
    		}
    		catch ( Exception e ) {
    			// Time series may not be for diversion but should not get an error unless there
    			// is a serious problem.
    			message = "Error filling time series using diversion comments (" + e + ").";
        		Message.printWarning(warning_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
    			status.addToLog ( CommandPhaseType.RUN,
    				new CommandLogRecord(CommandStatusType.FAILURE,
    					message, "Contact software support." ) );
    		}
    	}

    	MonthTS ts = null;
    	String id;
    	// Loop through available objects and see if any need to be filled...
    	int matchCount = 0;
    	for ( int i = 0; i < tsListSize; i++ ) {
    		ts = tsList.get(i);
    		id = ts.getLocation();
    		// Check if the requested location ID matches the ID in the time series.  If so, have
    		// found an existing time series to set.
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			//Message.printStatus ( 2, routine,"ID does not match.  Skipping..." );
    			continue;
    		}
    		++matchCount;

    		// Change the location in the input time series file to that which was requested so that
    		// later searches will match the location.  This is done inside the loop so that the
    		// ID that is used will match the pattern.

    		filets.addToGenesis ( "Changed location part of TSID from \"" +
    			filets.getLocation() + "\" to \"" + id + "\"" );
    		filets.setLocation ( id );

    		// Reset the data by copying what was read...

    		if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
    			tsList.set(i,ts = (MonthTS)filets.clone());
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    		else if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
    			tsList.set(i,ts = (MonthTS)filets.clone());
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    			// Make a copy of the time series needed for limiting to rights later...
    			if ( needToCopyDiversionHistoricalTSMonthly.booleanValue() ) {
    				processor.findAndAddSMDiversionTSMonthly2 (	(MonthTS)ts.clone(), true );
    			}
    		}
    		else if (compType == StateMod_DataSet.COMP_DIVERSION_TS_DAILY){
    			//tsList.set(i,ts = (DayTS)filets.clone());
    			// TODO smalers 2019-05-28 daily is not processed above so comment out here
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY ) {
    			tsList.set(i,ts = (MonthTS)filets.clone());
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
    			tsList.set(i,ts = (MonthTS)filets.clone());
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    	}
    	if ( (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase(_Add) ) {
    		Message.printStatus ( 2, routine,
    			"Unable to find matching TS for: \"" + ID + "\".  Adding at end." );
    		String oldlocation = filets.getLocation();
    		if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
    			tsList.add(	ts = (MonthTS)filets.clone() );
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    		else if ( compType==StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY){
    			tsList.add( ts = (MonthTS)filets.clone() );
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    			// Make a copy of the time series needed for limiting to rights later...
    			if ( needToCopyDiversionHistoricalTSMonthly.booleanValue() ) {
    				processor.findAndAddSMDiversionTSMonthly2 ( (MonthTS)ts.clone(), true );
    			}
    		}
    		else if (compType == StateMod_DataSet.COMP_DIVERSION_TS_DAILY){
    			//tsList.add( ts = (DayTS)filets.clone() );
    			// TODO smalers 2019-05-28 daily is not processed above so comment out here
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY ) {
    			tsList.add( ts = (MonthTS)filets.clone() );
    			ts.setDataLimitsOriginal (new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
    			tsList.add( ts = (MonthTS)filets.clone() );
    			ts.setDataLimitsOriginal ( new MonthTSLimits((MonthTS)ts,
    				OutputStart_DateTime, OutputEnd_DateTime, ignore_lezero_flag ) );
    		}
    		// This normally would occur only if the ID is not a wildcard...
    		ts.addToGenesis ( "Changed location part of TSID from \"" +
    			oldlocation + "\" to \"" + ID + "\"" );
    		ts.setLocation ( ID );
    		// Increment match count so warnings below are not printed.
    		++matchCount;
    	}
    	if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched in the time series: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is corrector use IfNotFound=" + _Add + "." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID + "\" was not matched in the time series: failing and not setting.";
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
	String TSID = parameters.getValue ( "TSID" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	//String SetStart = parameters.getValue ( "SetStart" );
	//String SetEnd = parameters.getValue ( "SetEnd" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"");
	}
	if ( (LEZeroInAverage != null) && (LEZeroInAverage.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LEZeroInAverage=" + LEZeroInAverage );
	}
	/*
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
	*/
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
