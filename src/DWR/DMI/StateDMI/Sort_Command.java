// Sort_Command - This class initializes, checks, and runs the Sort*() commands.

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

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_PenmanMonteith;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_Right_Comparator;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;
import RTi.TS.MonthTS;
import RTi.TS.TSUtil_SortTimeSeries;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the Sort*() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the SortCULocations()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class Sort_Command extends AbstractCommand implements Command
{

/**
Possible values for the Order parameter.
*/
protected final String _Ascending = "Ascending";
//protected final String _Descending = "Descending";

// Only used with well rights.
protected final String _IDAscending = "IDAscending";
protected final String _LocationIDAscending = "LocationIDAscending";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public Sort_Command ()
{	super();
	setCommandName ( "Sort?" );
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
{	String Order = parameters.getValue ( "Order" );
	String Order2 = parameters.getValue ( "Order2" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
    if ( this instanceof SortWellRights_Command ) {
		if ( (Order != null) && (Order.length() > 0) && !Order.equalsIgnoreCase(_IDAscending) &&
			!Order.equalsIgnoreCase(_LocationIDAscending)) {
			message = "The Order value (" + Order + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify Order as " + _IDAscending + " (default) or " +
					_LocationIDAscending + ".") );
		}
    }
    else {
		if ( (Order != null) && (Order.length() > 0) && !Order.equalsIgnoreCase(_Ascending) ) {
			message = "The Order value (" + Order + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify Order as " + _Ascending + " (default).") );
		}
    }
    
    if ( this instanceof SortWellRights_Command ) {
		if ( (Order2 != null) && (Order2.length() > 0) && !Order2.equalsIgnoreCase(_IDAscending) &&
			!Order2.equalsIgnoreCase(_LocationIDAscending)) {
			message = "The Order2 value (" + Order2 + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify Order2 as " + _IDAscending + " or " +
					_LocationIDAscending + " (default is no second order).") );
		}
    }
    
	// Check for invalid parameters...
	Vector<String> valid_Vector = new Vector<String>(2);
	valid_Vector.add ( "Order" );
	if ( this instanceof SortWellRights_Command ) {
		valid_Vector.add ( "Order2" );
	}
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
	return (new Sort_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param commandString A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String commandString )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	// First parse in the base class
    super.parseCommand( commandString);
    // Adjust parameter names
    String Order = getCommandParameters().getValue ( "Order" );
	if ( (Order != null) && Order.equalsIgnoreCase("Alphabetical") ) {
		// New default...
		Order = _Ascending;
		getCommandParameters().set ( "Order", Order );
	}
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
@param appType application type, either StateDMI.APP_TYPE_STATEMOD or StateDMI.APP_TYPE_STATECU.
The application type should be consistent with the component type.
@param compType Component type, from StateMod_DataSet or StateCU_DataSet, consistent with the appType.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommand", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    // Currently only Ascending order is supported
    String Order = parameters.getValue ( "Order" );
    if ( (Order == null) || (Order.length() == 0) ) {
    	// Default...
    	if ( this instanceof SortWellRights_Command ) {
    		Order = _IDAscending;
    	}
    	else {
    		Order = _Ascending;
    	}
    }
    String Order2 = parameters.getValue ( "Order2" );
    
    // Get the data needed for the command
    
    // List<? extends StateMod_Data> dataList = null;
    try {
    	//Object o = null;
    	if ( this instanceof SortBlaneyCriddle_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_BlaneyCriddle> dataList = (List<StateCU_BlaneyCriddle>)processor.getPropContents ( "StateCU_BlaneyCriddle_List" );
			List<StateCU_BlaneyCriddle> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
			processor.setPropContents( "StateCU_BlaneyCriddle_List", sortedDataList);
		}
    	else if ( this instanceof SortPenmanMonteith_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_PenmanMonteith> dataList = (List<StateCU_PenmanMonteith>)processor.getPropContents ( "StateCU_PenmanMonteith_List" );
			List<StateCU_PenmanMonteith> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
			processor.setPropContents( "StateCU_PenmanMonteith_List", sortedDataList);
		}
    	else if ( this instanceof SortClimateStations_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_ClimateStation> dataList = (List<StateCU_ClimateStation>)processor.getPropContents ( "StateCU_ClimateStation_List" );
			List<StateCU_ClimateStation> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
			processor.setPropContents( "StateCU_ClimateStation_List", sortedDataList);
		}
		else if( this instanceof SortCropCharacteristics_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_CropCharacteristics> dataList = (List<StateCU_CropCharacteristics>)processor.getPropContents ( "StateCU_CropCharacteristics_List" );
			List<StateCU_CropCharacteristics> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
  			processor.setPropContents( "StateCU_CropCharacteristics_List", sortedDataList);
	    }
		else if( this instanceof SortCropPatternTS_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents ( "StateCU_CropPatternTS_List" );
			List<StateCU_CropPatternTS> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
  			processor.setPropContents( "StateCU_CropPatternTS_List", sortedDataList);
	    }
		else if( this instanceof SortIrrigationPracticeTS_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_IrrigationPracticeTS> dataList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List" );
			List<StateCU_IrrigationPracticeTS> sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
  			processor.setPropContents( "StateCU_IrrigationPracticeTS_List", sortedDataList);
	    }
		else if ( this instanceof SortCULocations_Command ) {
			@SuppressWarnings("unchecked")
			List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents ( "StateCU_Location_List" );
			List<StateCU_Location>sortedDataList = StateCU_Util.sortStateCUDataList( dataList, false );
			processor.setPropContents( "StateCU_Location_List", sortedDataList);
		}
	    else if ( this instanceof SortDiversionStations_Command) {
	    	@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
			List<StateMod_Diversion> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_DiversionStation_List", sortedDataList);
	    }
	    else if ( this instanceof SortDiversionRights_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_DiversionRight> dataList = (List<StateMod_DiversionRight>)processor.getPropContents ( "StateMod_DiversionRight_List" );
			List<StateMod_DiversionRight> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_DiversionRight_List", sortedDataList);
    	}
    	else if ( this instanceof SortDiversionHistoricalTSMonthly_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
			// Sort by time series identifier.
			Message.printStatus ( 2, routine, "Sorting time series using the time series identifier..." );
			TSUtil_SortTimeSeries<MonthTS> tsu = new TSUtil_SortTimeSeries<MonthTS>(dataList,"TSID",null,null,1);
		    List<MonthTS> sortedDataList = tsu.sortTimeSeries();
			processor.setPropContents( "StateMod_DiversionHistoricalTSMonthly_List", sortedDataList);
    	}
    	else if ( this instanceof SortDiversionDemandTSMonthly_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
			// Sort by time series identifier.
			Message.printStatus ( 2, routine, "Sorting time series using the time series identifier..." );
			TSUtil_SortTimeSeries<MonthTS> tsu = new TSUtil_SortTimeSeries<MonthTS>(dataList,"TSID",null,null,1);
		    List<MonthTS> sortedDataList = tsu.sortTimeSeries();
			processor.setPropContents( "StateMod_DiversionDemandTSMonthly_List", sortedDataList);
    	}
    	else if ( this instanceof SortReservoirStations_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List" );
			List<StateMod_Reservoir> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_ReservoirStation_List", sortedDataList);
    	}
    	else if ( this instanceof SortReservoirRights_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_ReservoirRight> dataList = (List<StateMod_ReservoirRight>)processor.getPropContents ( "StateMod_ReservoirRight_List" );
			List<StateMod_ReservoirRight> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_ReservoirRight_List", sortedDataList);
    	}
    	else if ( this instanceof SortInstreamFlowStations_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlow> dataList = (List<StateMod_InstreamFlow>)processor.getPropContents ( "StateMod_InstreamFlowStation_List" );
			List<StateMod_InstreamFlow> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_InstreamFlowStation_List", sortedDataList);
    	}
    	else if ( this instanceof SortInstreamFlowRights_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlowRight> dataList = (List<StateMod_InstreamFlowRight>)processor.getPropContents ( "StateMod_InstreamFlowRight_List" );
			List<StateMod_InstreamFlowRight> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_InstreamFlowRight_List", sortedDataList);
    	}
    	else if ( this instanceof SortStreamEstimateStations_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate> dataList = (List<StateMod_StreamEstimate>)processor.getPropContents ( "StateMod_StreamEstimateStation_List" );
			List<StateMod_StreamEstimate> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_WellStation_List", sortedDataList);
    	}
    	else if ( this instanceof SortStreamGageStations_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_StreamGage> dataList = (List<StateMod_StreamGage>)processor.getPropContents ( "StateMod_StreamGageStation_List" );
			List<StateMod_StreamGage> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_StreamGageStation_List", sortedDataList);
    	}
    	else if ( this instanceof SortWellStations_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
			List<StateMod_Well> sortedDataList = StateMod_Util.sortStateMod_DataVector( dataList, false );
			processor.setPropContents( "StateMod_WellStation_List", sortedDataList);
    	}
    	else if ( this instanceof SortWellRights_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List" );
			StateMod_Right_Comparator<StateMod_WellRight> comparator = new StateMod_Right_Comparator<StateMod_WellRight>();
			if ( Order.equalsIgnoreCase(_IDAscending) ) {
				comparator.setOrder ( StateMod_Right_Comparator.IDAscending );
			}
			else if ( Order.equalsIgnoreCase(_LocationIDAscending) ) {
				comparator.setOrder ( StateMod_Right_Comparator.LocationIDAscending );
			}
			if ( (Order2 != null) && Order2.equalsIgnoreCase(_IDAscending) ) {
				comparator.setOrder2 ( StateMod_Right_Comparator.IDAscending );
			}
			else if ( (Order2 != null) && Order2.equalsIgnoreCase(_LocationIDAscending) ) {
				comparator.setOrder2 ( StateMod_Right_Comparator.LocationIDAscending );
			}
			List<StateMod_WellRight> sortedDataList = dataList;
			Collections.sort ( dataList, comparator );
			processor.setPropContents( "StateMod_WellRight_List", sortedDataList);
    	}
    	else if ( this instanceof SortWellHistoricalPumpingTSMonthly_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
			// Sort by time series identifier.
			Message.printStatus ( 2, routine, "Sorting time series using the time series identifier..." );
		    TSUtil_SortTimeSeries<MonthTS> tsu = new TSUtil_SortTimeSeries<MonthTS>(dataList,"TSID",null,null,1);
		    List<MonthTS>sortedDataList = tsu.sortTimeSeries();
			processor.setPropContents( "StateMod_WellHistoricalPumpingTSMonthly_List", sortedDataList);
    	}
    	else if ( this instanceof SortWellDemandTSMonthly_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
			// Sort by time series identifier.
			Message.printStatus ( 2, routine, "Sorting time series using the time series identifier..." );
		    TSUtil_SortTimeSeries<MonthTS> tsu = new TSUtil_SortTimeSeries<MonthTS>(dataList,"TSID",null,null,1);
		    List<MonthTS>sortedDataList = tsu.sortTimeSeries();
			processor.setPropContents( "StateMod_WellDemandTSMonthly_List", sortedDataList);
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting sorting data (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report to software support.  See log file for details." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        throw new InvalidCommandParameterException ( message );
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

	String Order = parameters.getValue ( "Order" );
	String Order2 = parameters.getValue ( "Order2" );

	StringBuffer b = new StringBuffer ();
	if ( (Order != null) && (Order.length() > 0) ) {
		b.append ( "Order=" + Order );
	}
	if ( this instanceof SortWellRights_Command ) {
		if ( (Order2 != null) && (Order2.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Order2=" + Order2 );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
