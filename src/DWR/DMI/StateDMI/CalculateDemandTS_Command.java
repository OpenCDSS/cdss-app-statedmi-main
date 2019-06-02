// CalculateDemandTS_Command - This class initializes, checks, and runs the Calculate*DemandTS*() commands.

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
import DWR.StateMod.StateMod_Well;

import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.TSUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
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
This class initializes, checks, and runs the Calculate*DemandTS*() commands.
It is an abstract base class that must be controlled via a derived class.  For example,
the CalculateDiversionDemandTSMonthly() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
*/
public abstract class CalculateDemandTS_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public CalculateDemandTS_Command ()
{	super();
	setCommandName ( "Calculate?DemandTS?" );
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
	
	// Include the Add option
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(2);
	valid_Vector.add ( "ID" );
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
	return (new CalculateDemandTS_JDialog ( parent, this )).ok();
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
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}

    // Get the data needed for the command
	
    // Set some booleans to increase processing speed
    boolean do_iwr = false;
    boolean do_max = false;
	if ( (this instanceof CalculateDiversionDemandTSMonthly_Command) ||
		(this instanceof CalculateWellDemandTSMonthly_Command) ) {
		do_iwr = true;
	}
	else if ( (this instanceof CalculateDiversionDemandTSMonthlyAsMax_Command) ||
		(this instanceof CalculateWellDemandTSMonthlyAsMax_Command) ) {
		do_max = true;
	}
	
	// Demand time series
    
    List<MonthTS> demandTSList = null;
    int compType = StateMod_DataSet.COMP_UNKNOWN;
    String dataType = "";
    String stationType = "";
    try {
    	if ( (this instanceof CalculateDiversionDemandTSMonthly_Command) ||
			(this instanceof CalculateDiversionDemandTSMonthlyAsMax_Command) ) {
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
			demandTSList = dataList;
			dataType = "diversion demand";
			stationType = "diversion";
			compType = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
		}
    	else if ( (this instanceof CalculateWellDemandTSMonthly_Command) ||
			(this instanceof CalculateWellDemandTSMonthlyAsMax_Command) ){
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
			demandTSList = dataList;
			dataType = "well demand";
			stationType = "well";
			compType = StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY;
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + dataType + " time series process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the historical time series if doing the maximum
    
    List<MonthTS> histTSList = null;
    //int histTSListSize = 0;
    if ( do_max ) {
	    try {
	    	if ( this instanceof CalculateDiversionDemandTSMonthlyAsMax_Command ) {
				@SuppressWarnings("unchecked")
				List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
				histTSList = dataList;
			}
	    	else if ( this instanceof CalculateWellDemandTSMonthlyAsMax_Command ) {
	    		@SuppressWarnings("unchecked")
				List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
	    		histTSList = dataList;
			}
	    	//histTSListSize = demandTSList.size();
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting " + stationType + " historical time series (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
    }
    
    // Get the consumptive water requirement time series if doing the IWR/Hist
    
    List<MonthTS> cwrTSList = null;
    int cwrTSListSize = 0;
    if ( do_iwr ) {
	    try {
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_ConsumptiveWaterRequirementTSMonthly_List" );
			cwrTSList = dataList;
	    	cwrTSListSize = cwrTSList.size();
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting consumptive water requirement time series (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
	    if ( cwrTSListSize == 0 ) {
	        message = "Consumptive water requirement (CWR, IWR) time series must be available to calculate demands.";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.WARNING,
	                message, "Read the time series with the ReadIrrigationWaterRequirementTSMonthlyFromStateCU() command." ) );
	    }
    }
    
    // Get the stations, used to perform ID checks
    List<StateMod_Diversion> divstaList = null;
    List<StateMod_Well> wellstaList = null;
    int stationListSize = 0;
	try {
		if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		divstaList = dataList;
    		stationListSize = divstaList.size();
    	}
		else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
    		wellstaList = dataList;
    		stationListSize = wellstaList.size();
    	}
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + stationType + " station list (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	// Need output year type to know order of efficiencies
    YearType outputYearType = null;
    try {
    	outputYearType = (YearType)processor.getPropContents ( "OutputYearType" );
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
    if ( OutputStart_DateTime == null ) {
        message = "OutputStart has not been specified.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Use SetOutputPeriod() prior to this command." ) );
    }
    else if ( OutputStart_DateTime.getPrecision() > TimeInterval.MONTH ) {
        message = "OutputStart precision needs to include the month.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Use SetOutputPeriod() with dates that include month, prior to this command." ) );
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
    if ( OutputEnd_DateTime == null ) {
        message = "OutputEnd has not been specified.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Use SetOutputPeriod() prior to this command." ) );
    }
    else if ( OutputEnd_DateTime.getPrecision() > TimeInterval.MONTH ) {
        message = "OutputEnd precision needs to include the month.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Use SetOutputPeriod() with dates that include month, prior to this command." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Set the months to get efficiencies out for the correct month.
    	// The numbers indicate for the requested month (zero index 0-11), the
    	// slot for the efficiency value.  For example, for water year, November
    	// (month [10] is in slot 1)

    	int months[] = { 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2 }; // For water year
    	// Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    	if ( outputYearType == YearType.WATER ) {
    		// Set above
    	}
    	else if ( outputYearType == YearType.NOV_TO_OCT ) {
    		months[0] = 2;
    		months[1] = 3;
    		months[2] = 4;
    		months[3] = 5;
    		months[4] = 6;
    		months[5] = 7;
    		months[6] = 8;
    		months[7] = 9;
    		months[8] = 10;
    		months[9] = 11;
    		months[10] = 0;
    		months[11] = 1;
    	}
    	else if ( outputYearType == YearType.CALENDAR ) {
    		// Calendar year...
    		months[0] = 0;
    		months[1] = 1;
    		months[2] = 2;
    		months[3] = 3;
    		months[4] = 4;
    		months[5] = 5;
    		months[6] = 6;
    		months[7] = 7;
    		months[8] = 8;
    		months[9] = 9;
    		months[10] = 10;
    		months[11] = 11;
    	}
    	else {
    		// Not recognized - should not happen
            message = "Year type (" + outputYearType + ") is not recognized";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
            status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Use the SetOutputYearType() command to set the year type." ) );
            throw new CommandException ( message );
    	}
    	
    	StateMod_Diversion div = null;
    	StateMod_Well well = null;
    	MonthTS iwr_ts = null, ddm_ts = null, ddh_ts = null;
    	int pos = 0; // Position of time series in array
    	String id = ""; // Diversion/well ID.
    	boolean do_zero; // Indicate whether zero demand time series should be set up.

    	int ignore_lezero_flag = 0;	// Indicates whether data values <= zero
    					// should be ignored when computing averages (which are later used in filling).
    	int matchCount = 0;
    	for ( int i = 0; i < stationListSize; i++ ) {
    		if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
    			div = divstaList.get(i);
    			id = div.getID();
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
    			well = wellstaList.get(i);
    			if ( well.getIdvcomw() != 1 ) {
    				// Can only process well-only where demand time
    				// series are supplied for calculations to make sense...
    				continue;
    			}
    			id = well.getID();
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		// Increment the count
    		++matchCount;
    		try {
    			// Catch major problems for a location...
	    		if ( do_iwr ) {
	    			// Calculating demands using IWR/EffAve...
	    			do_zero = false;
	    			pos = TSUtil.indexOf ( cwrTSList,id, "Location", 0 );
	    			if ( pos < 0 ) {
	    				// No CWR/IWR time series is available...
	    				message ="No CWR/IWR TS (monthly) available for \"" + id + "\".";
	    		        Message.printWarning ( warning_level, 
	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    		        status.addToLog ( command_phase,
	    		            new CommandLogRecord(CommandStatusType.WARNING,
	    		                message, "Using zero demand time series and continuing with processing.  " +
	    		                		"Verify that a time series is available for the identifier." ) );
	    				do_zero = true;
	    			}
	    			if ( !do_zero ) {
	    				// Try to get the time series...
	    				iwr_ts = (MonthTS)cwrTSList.get(pos);
	    				if ( iwr_ts == null ) {
	    					// No CWR/IWR time series is available...
	    					message = "No CWR/IWR TS (monthly) available for \""+ id + "\".";
		    		        Message.printWarning ( warning_level, 
	    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	    		        status.addToLog ( command_phase,
    	    		            new CommandLogRecord(CommandStatusType.WARNING,
    	    		                message, "Using zero demand time series and continuing with processing.  " +
    	    		                		"Verify that a time series is available for the identifier." ) );
	    					do_zero = true;
	    				}
	    			}
	    			// Always set up the header information the same way...
	    			ddm_ts = new MonthTS ();
	    			ddm_ts.setLocation ( id );
	    			ddm_ts.getIdentifier().setSource ( "StateDMI" );
	    			ddm_ts.setDate1 ( OutputStart_DateTime );
	    			ddm_ts.setDate1Original ( OutputStart_DateTime );
	    			ddm_ts.setDate2 ( OutputEnd_DateTime );
	    			ddm_ts.setDate2Original ( OutputEnd_DateTime );
	    			ddm_ts.getIdentifier().setInterval ( "Month" );
	    			ddm_ts.setDataType(	StateMod_DataSet.lookupTimeSeriesDataType (	compType ) );
	    			ddm_ts.setDataUnits( StateMod_DataSet.lookupTimeSeriesDataUnits( compType ) );
	    			ddm_ts.setDataUnitsOriginal( ddm_ts.getDataUnits() );
	    			ddm_ts.allocateDataSpace ();
	    			if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
	    				processor.findAndAddSMDemandTSMonthly ( ddm_ts, true );
	    			}
	    			else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
	    				processor.findAndAddSMWellDemandTSMonthly ( ddm_ts, true);
	    			}
	    			if ( do_zero ) {
	    				// Fill the demand time series with zeros...
	    				TSUtil.setConstant ( ddm_ts, 0.0 );
	    			}
	    			else {
	    				// Loop through the IWR and divide by the average monthly efficiencies.
	    				// If a diversion station that is a MultiStruct, it is assumed that the average
	    				// efficiency for the primary structure was previously calculated
	    				// using all the collection parts.  Therefore, no special logic is needed here.
	    				// The only logic is to set the part demand time series to zero (below).
	    				double iwr = 0.0;
	    				double ddm = 0.0;
	    				double eff = 0.0;
	    				for ( DateTime date = new DateTime(OutputStart_DateTime);
	    					date.lessThanOrEqualTo(OutputEnd_DateTime); date.addMonth(1) ) {
	    					if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
	    						eff = div.getDiveff(months[date.getMonth() -1]);
	    					}
	    					else if(compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
	    						eff = well.getDiveff(months[date.getMonth() -1]);
	    					}
	    					if ( eff == 0.0 ) {
	    						// The efficiency is zero, perhaps in a winter month.
	    						// If the IWR is zero, set the demand to zero...
	    						ddm_ts.setDataValue( date, 0.0 );
	    					}
	    					else {
	    						// Scale the IWR time series...
	    						iwr = iwr_ts.getDataValue ( date );
	    						if ( !iwr_ts.isDataMissing ( iwr ) ) {
	    							// Efficiencies are in percent...
	    							ddm = iwr/(eff*.01);
	    							ddm_ts.setDataValue(date, ddm);
	    						}
	    					}
	    				}
	    			}
	    			if ( (compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) && div.isCollection() &&
	    				div.getCollectionType().equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT)) {
	    				// This is the primary diversion station in a MultiStruct.  Set all of the secondary
	    				// demand time series to zero.
	    				List<String> partids = div.getCollectionPartIDs(0);
	    				int collection_size = 0;
	    				if ( partids != null ) {
	    					collection_size = partids.size();
	    				}
	    				String part_id;
	    				for ( int ic = 0; ic < collection_size; ic++ ) {
	    					part_id = partids.get(ic);
	    					pos = TSUtil.indexOf ( demandTSList, part_id, "Location", 0 );
	    					if ( pos < 0 ) {
	    						// No demand time series is available...
	    						message = "No diversion demand TS (monthly) is available for \""+
	    						id + "\" MultiStruct part \"" +	part_id +
	    						".  Normally would set to zero.";
		        		        Message.printWarning ( warning_level, 
		    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	    		        status.addToLog ( command_phase,
	    	    		            new CommandLogRecord(CommandStatusType.WARNING,
	    	    		                message, "Verify that diversion demand time series is available." ) );
	    						continue;
	    					}
	    					MonthTS ddm_ts2 = (MonthTS)demandTSList.get(pos);
	    					Message.printStatus ( 2, routine, "Diversion demand TS (monthly) for \""+ id +
	    						"\": setting MultiStruct part \"" + part_id + " demand time series to zero.");
	    					TSUtil.setConstant ( ddm_ts2, 0.0 );
	    				}
	    			}
	    		}
	    		else if ( do_max ) {
	    			// Calculating demand as max(demand,hist)...
	    			// Get the demand time series (this is the same whether
	    			// a distinct structure or a MultiStruct)...
	    			int pos_ddm = 0; // Position of the DDM time series.
	    			if ( compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY ) {
	    				pos_ddm = TSUtil.indexOf(demandTSList, id, "Location", 0 );
	    				if ( pos_ddm < 0 ) {
	    					// No demand time series is available...
	    					message = "No diversion demand TS (monthly) available for \""+ id +
	    					"\".  Unable to perform max().";
	        		        Message.printWarning ( warning_level, 
	    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	    		        status.addToLog ( command_phase,
    	    		            new CommandLogRecord(CommandStatusType.FAILURE,
    	    		                message, "Verify that diversion demand time series is available." ) );
	    					continue;
	    				}
	    				ddm_ts = (MonthTS)demandTSList.get(pos_ddm);
	    				// The historical diversion time series is needed for specific stations and
	    				// MultiStruct...
	    				pos = TSUtil.indexOf( histTSList,id, "Location", 0 );
	    				if ( pos < 0 ) {
	    					// No diversion time series is available...
	    					message = "No diversion historical TS (monthly) available for \""+
	    					id + "\".  Unable to perform max().";
	        		        Message.printWarning ( warning_level, 
	    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	    		        status.addToLog ( command_phase,
    	    		            new CommandLogRecord(CommandStatusType.FAILURE,
    	    		                message, "Verify that diversion historical time series is available." ) );
	    					continue;
	    				}
	    				ddh_ts = (MonthTS)histTSList.get(pos);
	    			}
	    			else if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
	    				pos_ddm = TSUtil.indexOf(demandTSList, id,"Location", 0 );
	    				if ( pos_ddm < 0 ) {
	    					// No demand time series is available...
	    					message = "No well demand TS (monthly) available for \""+ id +
	    					"\".  Unable to perform max().";
	          		        Message.printWarning ( warning_level, 
	    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	    		        status.addToLog ( command_phase,
    	    		            new CommandLogRecord(CommandStatusType.FAILURE,
    	    		                message, "Verify that well demand time series is available." ) );
	    					continue;
	    				}
	    				ddm_ts = (MonthTS)demandTSList.get(pos_ddm);
	    				// The historical pumping time series is needed for specific stations and
	    				// MultiStruct...
	    				pos = TSUtil.indexOf( histTSList, id, "Location", 0 );
	    				if ( pos < 0 ) {
	    					// No well time series is available...
	    					message = "No well historical pumping TS (monthly) available for \""+
	    					id + "\".  Unable to perform max().";
	        		        Message.printWarning ( warning_level, 
	    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	    		        status.addToLog ( command_phase,
    	    		            new CommandLogRecord(CommandStatusType.FAILURE,
    	    		                message, "Verify that well historical pumping time series is available." ) );
	    					continue;
	    				}
	    				ddh_ts = (MonthTS)histTSList.get(pos);
	    			}
	    			// Add to the historical diversion time series if a MultiStruct...
	    			if ( (compType == StateMod_DataSet.	COMP_DEMAND_TS_MONTHLY) && div.isCollection() &&
	    				div.getCollectionType().equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT)) {
	    				Message.printStatus ( 2, routine, "Adding diversion historical TS (monthly) "+
	    				"parts for MultiStruct \""+ id + "\"..." );
	    				// First clone the diversion time series so it is not changed...
	    				MonthTS ddh_ts2 = (MonthTS)ddh_ts.clone();
	    				// Now loop through the parts and add the historical time series for the parts...
	    				List<String> partids = div.getCollectionPartIDs(0);
	    				int collection_size = 0;
	    				if ( partids != null ) {
	    					collection_size = partids.size();
	    				}
	    				String part_id;
	    				for ( int ic = 0; ic < collection_size; ic++ ) {
	    					part_id = partids.get(ic);
	    					pos = TSUtil.indexOf ( histTSList, part_id, "Location", 0 );
	    					if ( pos < 0 ) {
	    						// No diversion time series is available...
	    						message = "No diversion historical TS (monthly) is available for \""+
	    						id + "\" MultiStruct part \"" + part_id + "\".";
	    						Message.printWarning ( warning_level, 
		    	    		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	    		        status.addToLog ( command_phase,
	    	    		            new CommandLogRecord(CommandStatusType.WARNING,
	    	    		                message, "Doing max() without this time series - " +
	    	    		                	"verify that a diversion historical time series is available." ) );
	    						continue;
	    					}
	    					// Add the part to the total...
	    					Message.printStatus ( 2, routine, "Diversion historical TS (monthly) for \""+
	    					id + "\": adding MultiStruct part \"" + part_id + "\" historical time series.");
	    					ddh_ts2 = (MonthTS)TSUtil.add ( ddh_ts2, (MonthTS)histTSList.get(pos) );
	    				}
	    				// Set the time series reference to point to the clone, for the following logic...
	    				ddh_ts = ddh_ts2;
	    			}
	    			// Calculate the new demand time series and replace in the list...
	    			ddm_ts = (MonthTS)TSUtil.max ( ddm_ts, ddh_ts );
    				demandTSList.set ( pos_ddm, ddm_ts );
	    		}
	    		// Reset the data limits so that later fill commands can use the current information...
	    		ddm_ts.setDataLimitsOriginal ( new MonthTSLimits(ddm_ts,
	    			ddm_ts.getDate1(), ddm_ts.getDate2(), ignore_lezero_flag ) );
    		} catch ( Exception e ) {
    	        Message.printWarning ( log_level, routine, e );
    	        message = "Unexpected error processing demand time series for \"" + id + "\" (" + e + ").";
    	        Message.printWarning ( warning_level, 
    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	        status.addToLog ( command_phase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "See log file for details." ) );
    		}
    	}
    	if ( matchCount == 0 ) {
    		String note = "";
    		if ( compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY ) {
    			note = "  Only well stations where demand type is monthly total demand are processed.";
    		}
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not calculating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct and stations have been read/set." + note ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID +	"\" was not matched: failing and not calculating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct and stations have been read/set." + note ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
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
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
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
