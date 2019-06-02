// SetWellStationAreaToCropPatternTS_Command - This class initializes, checks, and runs the SetWellStationAreaToCropPatternTS() command.

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

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Well;

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
This class initializes, checks, and runs the SetWellStationAreaToCropPatternTS() command.
</p>
*/
public class SetWellStationAreaToCropPatternTS_Command 
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
public SetWellStationAreaToCropPatternTS_Command ()
{	super();
	setCommandName ( "SetWellStationAreaToCropPatternTS" );
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
{	
	String ID = parameters.getValue ( "ID" );
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
                message, "Specify the ID to process." ) );
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
	/* TODO SAM Evaluate use
	if ( (IncludeSurfaceWaterSupply != null) &&
			IncludeSurfaceWaterSupply.equalsIgnoreCase(_True)) {
		warning += "\nIncludeSurfaceWaterSupply=True CANNOT currently be used when filling with rights.";
	}
	*/

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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new SetWellStationAreaToCropPatternTS_JDialog ( parent, this )).ok();
}

/**
Determine the area for a well station as the maximum area from the crop pattern
time series.  If no time series is found, the area is zero.
@param cds A StateCU_CropPatternTS to process.
@param id The location identifier.
@return the area, in ACRE.
*/
private double getAreaFromCropPatternTS ( StateCU_CropPatternTS cds, String id )
{			
	double area = 0, area_year;
	DateTime date1 = cds.getDate1();
	DateTime date2 = cds.getDate2();
	for ( int year = date1.getYear(); year <= date2.getYear(); year++ ) {
		area_year = cds.getTotalArea ( year );
		if ( area_year > area ) {
			area = area_year;
		}
	}
	return area;
}

//Use parent parseCommand

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_tag specfic tag or identifier for this command.
@param warningLevel The message warning level to write to the log file with.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int log_level = 3;  // Log level for non-user warnings
	
	CommandPhaseType command_phase = CommandPhaseType.RUN;
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	//String IncludeSurfaceWaterSupply = _parameters.getValue ( "IncludeSurfaceWaterSupply" );
	//String IncludeGroundwaterOnlySupply = _parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	/*
	boolean IncludeSurfaceWaterSupply_boolean = true;
	if ( IncludeSurfaceWaterSupply != null ) {
		if ( IncludeSurfaceWaterSupply.equalsIgnoreCase("true") ) {
			IncludeSurfaceWaterSupply_boolean = true;
		}
		else if ( IncludeSurfaceWaterSupply.equalsIgnoreCase("false") ) {
			IncludeSurfaceWaterSupply_boolean = false;
		}
	}
	boolean IncludeGroundwaterOnlySupply_boolean = true;
	if ( IncludeGroundwaterOnlySupply != null ) {
		if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase("true") ) {
			IncludeGroundwaterOnlySupply_boolean = true;
		}
		else if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase("false") ) {
			IncludeGroundwaterOnlySupply_boolean = false;
		}
	}
	*/
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of well stations...
	
	List<StateMod_Well> wellList = null;
	int wellListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents( "StateMod_WellStation_List");
		wellList = dataList;
		wellListSize = wellList.size();
	}
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting well station data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
	}
	
	// Get the crop pattern time series, which are needed to set the data...
	
	List<StateCU_CropPatternTS> cdsList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents( "StateCU_CropPatternTS_List");
		cdsList = dataList;
	}
	catch ( Exception e ) {
		message = "Unable to get crop pattern time series to set well station area (" + e + ").";
        Message.printWarning ( log_level, routine, e );
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
	}
	if ( cdsList.size() == 0 ) {
		message = "No crop pattern time series are available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that crop pattern time series have been read or set in a previous command." ) );
	}
	
	try {
		// Loop through well stations and set the area.
		StateMod_Well well = null;
		StateCU_CropPatternTS cds = null;
		String id;	// Location ID
		int matchCount = 0;
		for ( int i = 0; i < wellListSize; i++ ) {
			well =(StateMod_Well)wellList.get(i);
			id = well.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			int pos = StateCU_Util.indexOf ( cdsList, id );
			if ( pos < 0 ) {
				// No CDS to use for filling.
				continue;
			}
			cds = (StateCU_CropPatternTS)cdsList.get(pos);
			/*
			// Check whether location supply matches the supply types to be included...
			if (IncludeSurfaceWaterSupply_boolean || IncludeGroundwaterOnlySupply_boolean ) {
				// Need to get the CU location to check for GW-only...
				int pos = StateCU_Util.indexOf ( CULocation_Vector, id );
				if ( pos < 0 ) {
					Message.printWarning ( 2,
					MessageUtil.formatMessageTag(command_tag,++__run_warning_count),
					routine,
					"Could not find CU location matching \"" + id +
					"\" to determine water supply type.  Not filling." );
					continue;
				}
				culoc = (StateCU_Location)CULocation_Vector.elementAt(pos);
			}
			if ( IncludeSurfaceWaterSupply_boolean && IncludeGroundwaterOnlySupply_boolean ) {
				// Including both...
			}
			else {
				// Check that only one type is included so not everything is skipped.
				// TODO SAM 2007-07-12 Need to fix this
				if ( IncludeSurfaceWaterSupply_boolean && !culoc.hasSurfaceWaterSupply() ) {
					continue;
				}
				if ( IncludeGroundwaterOnlySupply_boolean && !culoc.hasGroundwaterOnlySupply() ) {
					continue;
				}
			}
			*/
			// Get the total of the rights for the location...
			double area = getAreaFromCropPatternTS ( cds, id );
			// Set the capacity based on the rights...
			Message.printStatus ( 2, routine, "Setting well station \"" + id + "\" area -> " + area );
			well.setAreaw(area);
			++matchCount;
		}
		if ( matchCount == 0 ) {
	    	if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
	    		message = "Well station \"" + ID + "\" was not matched: warning and not setting";
    			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
	    	}
	    	else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
	    		message = "Well station \"" + ID + "\" was not matched: failing and not setting";
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
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting well station area from crop pattern time series (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
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
	//String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	//String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
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
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
