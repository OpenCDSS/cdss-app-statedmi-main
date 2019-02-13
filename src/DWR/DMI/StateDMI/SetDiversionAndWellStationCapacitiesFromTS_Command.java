// SetDiversionAndWellStationCapacitiesFromTS_Command - This class initializes, checks, and runs the SetDiversionStationCapacitiesFromTS() and
// SetWellStationCapacitiesFromTS() commands.

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
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;
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
This class initializes, checks, and runs the SetDiversionStationCapacitiesFromTS() and
SetWellStationCapacitiesFromTS() commands.  The functionality is handled in one class due to the
close similarity between the commands and of diversions and wells in StateMod.  It is an abstract
base class that must be controlled via a derived class.  For example, the
SetDiversionStationCapacitiesFromTS() command extends this class in order to uniquely represent
the command, but much of the functionality is in this base class.
</p>
*/
public abstract class SetDiversionAndWellStationCapacitiesFromTS_Command extends AbstractCommand implements Command
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
public SetDiversionAndWellStationCapacitiesFromTS_Command ()
{	super();
	setCommandName ( "Set?StationCapacitiesFromTS" );
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
	
	if ( (ID == null) ||	(ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID to process." ) );
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
	return (new SetDiversionAndWellStationCapacitiesFromTS_JDialog ( parent, this )).ok();
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
		ID = "*";
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}

    // Get the data needed for the command
    
    List stationList = null;
    int stationListSize = 0;
    String stationType = null;
    String dataType = null;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // Used rather than instanceof to increase performance
    try {
    	if ( this instanceof SetDiversionStationCapacitiesFromTS_Command ) {
    		stationType = "diversion";
    		dataType = "historical";
    		stationList = (List)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		compType = StateMod_DataSet.COMP_DIVERSION_STATIONS;
    	}
    	else if ( this instanceof SetWellStationCapacitiesFromTS_Command ) {
    		stationType = "well";
    		dataType = "historical pumping";
    		stationList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
    		compType = StateMod_DataSet.COMP_WELL_STATIONS;
    	}
    	stationListSize = stationList.size();
    	Message.printStatus ( 2, routine, "Have " + stationListSize + " " + stationType + " stations.");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + stationType + " station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( stationListSize == 0 ) {
        message = "No " + stationType + " stations are available).";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.WARNING,
                message, "Verify that " + stationType +
                " stations have been read or defined prior to this command." ) );
    }
    
    // Get the time series
    
    List tsList = null;
    int tsListSize = 0;
    try {
    	if ( this instanceof SetDiversionStationCapacitiesFromTS_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
    	}
    	else if ( this instanceof SetWellStationCapacitiesFromTS_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
    	}
    	tsListSize = tsList.size();
    	Message.printStatus ( 2, routine, "Have " + tsListSize + " " + dataType + " time series.");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + stationType.toLowerCase() + " " + dataType +
        " time series to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( tsListSize == 0 ) {
    	message = "No " + stationType + " " + dataType + " time series are available to check capacities.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.WARNING,
                message, "Verify that the time series have been read or created with previous commands." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	StateMod_Diversion div = null;
    	StateMod_Well well = null;
    	MonthTS mts = null;
    	int pos = 0; // Position of time series in array
    	TSLimits limits = null; // Limits of data, to check capacity.
    	String id = null; // Diversion or well ID.
    	double maxdiv = 0.0; // Maximum diversion or well pumping from time series.
    	DateTime maxtime = null; // Date/time for maximum diversion or well pumping
    	double cap_plus = 0.0; // Diversion or well capacity + .01 for check within the output tolerance.
    	int days_in_month; // Number of days in the month.
    	double acft_to_cfs = 1.9835; // Hard-code conversion rather than relying on data units information.
    	int matchCount = 0;
    	for ( int i = 0; i < stationListSize; i++ ) {
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    			div = (StateMod_Diversion)stationList.get(i);
    			id = div.getID();
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			well = (StateMod_Well)stationList.get(i);
    			id = well.getID();
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Get the historical diversion time series...

			pos = TSUtil.indexOf( tsList, id, "Location", 0 );
			if ( pos < 0 ) {
				// No historical time series is available...
				message = "No " + stationType + " " + dataType + " TS (monthly) is available for \""+
				id + "\".  Skipping capacity check.";
				Message.printWarning ( warning_level, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count),
			        routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Verify that " + dataType + " time series are available for the station." ) );
		        // FIXME SAM 2009-01-20 May be OK if historical diversion specified some other way?
				continue;
			}
			mts = (MonthTS)tsList.get(pos);
			if ( mts == null ) {
				// No historical time series is available...
				message = "No " + stationType + " " + dataType + " TS (monthly) is available for \""+
				id + "\".  Skipping capacity check.";
				Message.printWarning ( warning_level, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count),
			        routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Verify that " + dataType + " time series are available for the station." ) );
		        // FIXME SAM 2009-01-20 May be OK if historical diversion specified some other way?
			}

    		limits = TSUtil.getDataLimits ( mts, null, null );
    		if ( limits == null ) {
				message = "Unable to get maximum value for " + stationType + " station: \"" + id+ "\"";
				Message.printWarning ( warning_level, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count),
			        routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Entire period might be missing - fill data prior to using this command." ) );
				continue;
    		}
    		maxdiv = limits.getMaxValue();
    		maxtime = limits.getMaxValueDate();
    		if ( maxtime == null ) {
    			// Totally missing time series.
				message = "Unable to get maximum value for " + stationType + " station: \"" + id + "\"";
				Message.printWarning ( warning_level, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count),
			        routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Entire period might be missing - fill data prior to using this command." ) );
    			continue;
    		}
    		days_in_month =	TimeUtil.numDaysInMonth ( maxtime.getMonth(), maxtime.getYear() );
    		// Convert to CFS...
    		maxdiv /= (acft_to_cfs*days_in_month);
    		// Check a tolerance here to avoid getting a message for every minor change.
    		// The following will not even show up in the precision in the file...
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    			cap_plus = div.getDivcap() + .01;
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			cap_plus = well.getDivcapw() + .01;
    		}
    		// Check tolerance.  Since output is typically printed to the .01, only need to
    		// reset if that value is exceeded...
    		if ( maxdiv >= cap_plus ) {
    			// Replace the existing value with the value from the time series...
    			if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    				Message.printStatus ( 2, routine, "Changing " + id + " (" + div.getName() +
    				") capacity from " + StringUtil.formatString(div.getDivcap(),"%.4f")+
    				" to historic max " + StringUtil.formatString(maxdiv,"%.4f") + " (max on " + maxtime + ")" );
    				div.setDivcap ( maxdiv );
    			}
    			else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    				Message.printStatus ( 2, routine, "Changing " + id + " (" + well.getName() +
    				") capacity from " + StringUtil.formatString(well.getDivcapw(),"%.4f")+
    				" to historic max " + StringUtil.formatString(maxdiv,"%.4f") + " (max on " + maxtime + ")" );
    				well.setDivcapw ( maxdiv );
    			}
    		}
    	}
    	if ( matchCount == 0 ) {
    		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
    			message = "Well \"" + ID + "\" was not matched: warning and not setting capacity.";
    			Message.printWarning(warning_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
    			status.addToLog ( CommandPhaseType.RUN,
    				new CommandLogRecord(CommandStatusType.WARNING,
    					message, "Verify that the identifier is correct and that a matching station is defined." ) );
    		}
    		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
    			message = "Well \"" + ID +	"\" was not matched: failing and not setting capacity.";
    			Message.printWarning(warning_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
    			status.addToLog ( CommandPhaseType.RUN,
    				new CommandLogRecord(CommandStatusType.FAILURE,
    					message, "Verify that the identifier is correct and that a matching station is defined." ) );
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
