// SetIrrigationPracticeTSPumpingMaxUsingWellRights_Command - This class initializes, checks, and runs the SetIrrigationPracticeTSPumpingMaxUsingWellRights() command.

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

import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.TS.YearTS;
import RTi.Util.Math.MathUtil;
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
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
<p>
This class initializes, checks, and runs the SetIrrigationPracticeTSPumpingMaxUsingWellRights() command.
</p>
*/
public class SetIrrigationPracticeTSPumpingMaxUsingWellRights_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "False";
protected final String _True = "True";

// High efficiency irrigation types...

protected final String _SPRINKLER = "SPRINKLER";
protected final String _DRIP = "DRIP";

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public SetIrrigationPracticeTSPumpingMaxUsingWellRights_Command ()
{	super();
	setCommandName ( "SetIrrigationPracticeTSPumpingMaxUsingWellRights" );
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
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String FreeWaterAdministrationNumber = parameters.getValue ( "FreeWaterAdministrationNumber" );
	String FreeWaterAppropriationDate = parameters.getValue ( "FreeWaterAppropriationDate" );
	String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "A location identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}
	if ( (IncludeSurfaceWaterSupply != null) && (IncludeSurfaceWaterSupply.length() > 0) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_True) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) ) {
		message = "The IncludeSurfaceWaterSupply (" + IncludeSurfaceWaterSupply + ") value is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeSurfaceWaterSupply parameter as " + _False + " or " + _True +
				" (default)." ) );
	}
	if ( (IncludeGroundwaterOnlySupply != null) && (IncludeGroundwaterOnlySupply.length() > 0) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_True) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) ) {
		message = "The IncludeGroundwaterOnlySupply (" + IncludeGroundwaterOnlySupply + ") value is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeGroundwaterOnlySupply parameter as " + _False + " or " + _True +
				" (default)." ) );
	}
	/* TODO SAM Evaluate use
	if ( (IncludeSurfaceWaterSupply != null) &&
			IncludeSurfaceWaterSupply.equalsIgnoreCase(_True)) {
		warning += "\nIncludeSurfaceWaterSupply=True CANNOT currently be used when filling with rights.";
	}
	*/
	
	if ( (SetStart != null) && (SetStart.length() != 0) && !StringUtil.isInteger(SetStart)) {
		message = "The set start (" + SetStart + ") is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the set start as an integer year YYYY." ) );
	}
	
	if ( (SetEnd != null) && (SetEnd.length() != 0) && !StringUtil.isInteger(SetEnd)) {
		message = "The set end (" + SetEnd + ") is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the set end as an integer year YYYY." ) );
	}
	if ( (FreeWaterAdministrationNumber != null) && (FreeWaterAdministrationNumber.length() != 0) ) {
		if ( !StringUtil.isDouble(FreeWaterAdministrationNumber) ) {
			message = "The FreeWaterAdministrationNumber (" + FreeWaterAdministrationNumber +
			") is not a valid number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify FreeWaterAdministrationNumber as a number." ) );
		}
	}
	if ( (NumberOfDaysInMonth != null) && (NumberOfDaysInMonth.length() != 0) ) {
		if ( !StringUtil.isDouble(NumberOfDaysInMonth) ) {
			message = "The NumberOfDaysInMonth (" + NumberOfDaysInMonth + ") is not a valid number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the NumberOfDaysInMonth as a number." ) );
		}
	}
	if ( (FreeWaterAppropriationDate != null) && (FreeWaterAppropriationDate.length() != 0) ) {
		if ( !TimeUtil.isDateTime(FreeWaterAppropriationDate) ) {
			message = "The FreeWaterAppropriationDate (" + FreeWaterAppropriationDate +
			") is not a valid date.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the FreeWaterAppropriationDate as date YYYY-MM-DD or MM/DD/YYYY." ) );
		}
	}
	if ( (ParcelYear != null) && (ParcelYear.length() != 0) ) {
		if ( !StringUtil.isInteger(ParcelYear) ) {
			message = "The ParcelYear (" + ParcelYear + ") is not a valid integer.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the ParcelYear as an integer YYYY." ) );
		}
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
	List<String> valid_Vector = new Vector<String>(10);
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "IncludeSurfaceWaterSupply" );
	valid_Vector.add ( "IncludeGroundwaterOnlySupply" );
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
	valid_Vector.add ( "FreeWaterAdministrationNumber" );
	valid_Vector.add ( "FreeWaterAppropriationDate" );
	valid_Vector.add ( "NumberOfDaysInMonth" );
	valid_Vector.add ( "ParcelYear" );
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
	return (new SetIrrigationPracticeTSPumpingMaxUsingWellRights_JDialog ( parent, this )).ok();
}

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
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
		ID = "*";
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String FreeWaterAdministrationNumber = parameters.getValue ( "FreeWaterAdministrationNumber" );
	String FreeWaterAppropriationDate = parameters.getValue ( "FreeWaterAppropriationDate" );
	String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	// Set defaults...

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

	int ParcelYear_int = -1;
	if ( ParcelYear != null ) {
		ParcelYear_int = Integer.parseInt( ParcelYear );
	}
	
	double FreeWaterAdministrationNumber_double = 90000.0;
	if ( FreeWaterAdministrationNumber != null ) {
		FreeWaterAdministrationNumber_double = Double.parseDouble ( FreeWaterAdministrationNumber );
	}
	
	DateTime FreeWaterAppropriationDate_DateTime = null;
	if ( FreeWaterAppropriationDate != null ) {
		try {
			FreeWaterAppropriationDate_DateTime = DateTime.parse(FreeWaterAppropriationDate);
		}
		catch ( Exception e ) {
			// Should not happen since checked previously
		}
	}
	
	double NumberOfDaysInMonth_double = -1.0;
	if ( NumberOfDaysInMonth != null ) {
		NumberOfDaysInMonth_double = Double.parseDouble (NumberOfDaysInMonth );
	}
	
	// Get the irrigation practice time series to process.
	
	List<StateCU_IrrigationPracticeTS> ipyList = null;
	int ipyListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_IrrigationPracticeTS> dataList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
		ipyList = dataList;
		ipyListSize = ipyList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting irrigation practice time series data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( ipyListSize == 0 ) {
		message = "No irrigation practice time series are defined.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateIrrigationPracticeTSForCULocations() before this command." ) );
	}
	
	// Get the CU locations, which is where collection information is stored,
	// necessary to determine if groundwater only location...
	
	List<StateCU_Location> culocList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the well rights, which are needed to create the time series by parcel...
	
	List<StateMod_WellRight> werList = null;
	int werListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents( "StateMod_WellRight_List");
		werList = dataList;
		werListSize = werList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_WellRight_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( werListSize == 0 ) {
		message = "No well rights available to set irrigation practice time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Read well rights before using this command." ) );
	}
	
	// Get the period to fill (default if fill period is not specified)...
	
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
	// Set the output end precision to month for creating the
	// water right time series.  Include the ending month 12.
	DateTime OutputStart_DateTime2 = null;
	DateTime OutputEnd_DateTime2 = null;
	if ( OutputStart_DateTime != null ) {
		OutputStart_DateTime2 = new DateTime ( OutputStart_DateTime );
		OutputStart_DateTime2.setPrecision( DateTime.PRECISION_MONTH );
		OutputStart_DateTime2.setMonth ( 1 );
	}
	if ( OutputEnd_DateTime != null ) {
		OutputEnd_DateTime2 = new DateTime ( OutputEnd_DateTime );
		OutputEnd_DateTime2.setPrecision( DateTime.PRECISION_MONTH );
		OutputEnd_DateTime2.setMonth ( 12 );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		// Convert the well rights to monthly time series (because the value
		// in the IPY file is the maximum monthly pumping)...
		List<TS> smrights_MonthTS_Vector = null;
		try {
			smrights_MonthTS_Vector = StateMod_Util.createWaterRightTimeSeriesList (
				werList,
				TimeInterval.MONTH,
				0,	// Aggregate to location
				ParcelYear_int,	// Not required - depends on rights file
				false,	// Do not include data set totals
				OutputStart_DateTime2,
				OutputEnd_DateTime2,
				FreeWaterAdministrationNumber_double,
				StateMod_Util.UseSeniorRightAppropriationDate,
				FreeWaterAppropriationDate_DateTime,
				true );	// Do read the data (not just header)
			Message.printStatus ( 2, routine, "Created " + smrights_MonthTS_Vector.size()
				+ " location/right time series from well water rights for period " +
				OutputStart_DateTime2 + " to " + OutputEnd_DateTime2 );
		}
		catch ( Exception e ) {
			message = "Unable to convert well rights monthly time series.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that well rights include parcel/and year " + ParcelYear_int) );
			// Throw an exception here to break out
			throw new CommandException ( message );
		}
		
		// Loop through available irrigation practice time series and see if any need to be set.
		StateCU_IrrigationPracticeTS ipyts = null;
		StateCU_Location culoc = null;
		String id;	// Location ID
		int matchCount = 0;
		for ( int i = 0; i < ipyListSize; i++ ) {
			ipyts =(StateCU_IrrigationPracticeTS)ipyList.get(i);
			id = ipyts.getID();
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine, "Checking CULocation " + id + " against \"" +
				idpattern_Java + "\"" );
			}
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Check whether location supply matches the supply types to be included...
			if (IncludeSurfaceWaterSupply_boolean ||IncludeGroundwaterOnlySupply_boolean ) {
				// Need to get the CU location to check for GW-only...
				int pos = StateCU_Util.indexOf ( culocList, id );
				if ( pos < 0 ) {
					message = "Could not find CU location matching \"" + id +
					"\" to determine water supply type.  Not filling.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Verify that well rights include parcel/and year " + ParcelYear_int) );
					continue;
				}
				culoc = (StateCU_Location)culocList.get(pos);
			}
			if ( IncludeSurfaceWaterSupply_boolean && IncludeGroundwaterOnlySupply_boolean ) {
				// Including both...
			}
			else {
				// Check that only one type is included so not everything is skipped.
				// TODO SAM 2007-07-12 Need to fix this
				if ( IncludeSurfaceWaterSupply_boolean && !culoc.hasSurfaceWaterSupplyForModelNode() ) {
					continue;
				}
				if ( IncludeGroundwaterOnlySupply_boolean && !culoc.isGroundwaterOnlySupplyModelNode() ) {
					continue;
				}
			}
			// Set using rights, for the requested period.
			setIrrigationPracticeTSUsingRights (
				smrights_MonthTS_Vector,
				ipyts,
				SetStart_DateTime,
				SetEnd_DateTime,
				NumberOfDaysInMonth_double,
				ParcelYear_int );
			
			++matchCount;
		}
		
		// If nothing was matched, take further action...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Crop pattern time series \"" + ID +
				"\" was not matched: warning and not setting crop pattern time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
							"  The time series must be created before setting any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Crop pattern time series \"" + ID +
				"\" was not matched: failing and not setting crop pattern time series.";
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
Set the irrigation practice pumping max time series using water rights.  Do this by getting the
data for the parcel year (which has references to the parcels used to
create the data) and check those parcels for whether they have rights in a
year.  If they do, include the parcels.  If no parcels or rights, set to zero.
@param smrights_MonthTS_Vector Vector of YearTS for the rights for parcels.  This
will be used to look up a time series for the matching parcel.
@param cdsts The IrrigationPracticeTS that is having a component filled, passed in because
the stored parcel information is at this level.
@param datatype The time series data type being filled.
It is also passed in to avoid issues pulling out the data type from a composite
data type in "yts".  CURRENTLY IGNORED - ALWAYS FILLED.
@param SetStart_DateTime The starting date/time for filling.
@param SetEnd_DateTime The ending date/time for filling.
@param NumberOfDaysInMonth_double Number of days in a month, for converting from
CFS to AF/M.  If negative, use actual days per month
@param ParcelYear_int the parcel year to use for parcel data.
*/
private void setIrrigationPracticeTSUsingRights (
		List<TS> smrights_MonthTS_Vector,
		StateCU_IrrigationPracticeTS ipyts,
		DateTime SetStart_DateTime,
		DateTime SetEnd_DateTime,
		double NumberOfDaysInMonth_double,
		int ParcelYear_int )
{	String routine = "setIrrigationPracticeTSPumpingMaxUsingWellRights.setIrrigationPracticeTSUsingRights";
	if ( ParcelYear_int > 0 ) {
		Message.printStatus( 2, routine, "Setting irrigation practice pumping max time series for \"" +
		ipyts.getID() + "\" by using " + ParcelYear_int +
		" well water rights associated with location.");
	}
	else {
		Message.printStatus( 2, routine, "Setting irrigation practice pumping max time series for \"" +
		ipyts.getID() + "\" by using all well rights associated with location.");
	}
			
	// Get the component time series that will be processed...
	
	String id = ipyts.getID();
	YearTS Mprate_ts = ipyts.getMprateTS();
	
	// Get the water right time series for the location...
	
	int pos = TSUtil.indexOf ( smrights_MonthTS_Vector, id,	"Location", 1 );
	if ( pos < 0 ) {
		// No time series for the parcel.
		Message.printStatus( 2, routine,
			"No time series of rights available for location \"" + id +
			"\".  Setting pumping to zero for " + SetStart_DateTime + " to " + SetEnd_DateTime );
		// Loop by year...
		// Iterate through the full period by month...
		DateTime date = new DateTime(SetStart_DateTime);
		date.setPrecision( DateTime.PRECISION_YEAR );
		DateTime date2 = new DateTime(SetEnd_DateTime);
		date2.setPrecision( DateTime.PRECISION_YEAR );
		for ( ; date.lessThanOrEqualTo(date2); date.addYear(1) ) {
			Mprate_ts.setDataValue ( date, 0.0 );
		}
		return;
	}
	MonthTS right_ts = (MonthTS)smrights_MonthTS_Vector.get(pos);
	
	double mprate;
	double right_decree;
	double days_per_month = 0;
	double max_pumping = 0;
	int month, year;
	// Reset dates to use monthly iteration...
	DateTime date = new DateTime(SetStart_DateTime);
	date.setPrecision( DateTime.PRECISION_MONTH );
	date.setMonth( 1 );
	DateTime date2 = new DateTime(SetEnd_DateTime);
	date2.setPrecision( DateTime.PRECISION_MONTH );
	date2.setMonth( 12 );
	for ( ; date.lessThanOrEqualTo(date2); date.addMonth(1) ) {
		month = date.getMonth();
		year = date.getYear();
		mprate = Mprate_ts.getDataValue ( date );
		// The right at a point in time is CFS
		right_decree = right_ts.getDataValue ( date );
		if ( right_decree < 0.0 ) {
			right_decree = 0.0;
		}
		if ( NumberOfDaysInMonth_double > 0.0 ) {
			days_per_month = NumberOfDaysInMonth_double;
		}
		else {
			days_per_month = TimeUtil.numDaysInMonth(month, year );
		}
		// Calculate AF/M from CFS
		mprate = right_decree*1.9835*days_per_month;
		if ( date.getMonth() == 1 ) {
			// Initialize the maximum.
			max_pumping = mprate;
		}
		else {
			// Get the maximum of the previous max and current value...
			max_pumping = MathUtil.max ( max_pumping, mprate );
		}
		if ( month == 12 ) {
			// End of a year or end of the period.
			// Save the max pumping to the time series (only year will be used
			// to determine the data position)...
			Mprate_ts.setDataValue ( date, max_pumping );
			Message.printStatus ( 2, routine, "Setting " + id + " " + year + " pumping max to " +
				StringUtil.formatString ( max_pumping, "%.2f") );
		}
	}
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
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String FreeWaterAdministrationNumber = parameters.getValue ( "FreeWaterAdministrationNumber" );
	String FreeWaterAppropriationDate = parameters.getValue ( "FreeWaterAppropriationDate" );
	String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
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
	if ( FreeWaterAdministrationNumber != null && FreeWaterAdministrationNumber.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FreeWaterAdministrationNumber=" + FreeWaterAdministrationNumber );
	}
	if ( FreeWaterAppropriationDate != null && FreeWaterAppropriationDate.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FreeWaterAppropriationDate=" + FreeWaterAppropriationDate );
	}
	if ( NumberOfDaysInMonth != null && NumberOfDaysInMonth.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NumberOfDaysInMonth=" + NumberOfDaysInMonth );
	}
	if ( ParcelYear != null && ParcelYear.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelYear=" + ParcelYear );
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
