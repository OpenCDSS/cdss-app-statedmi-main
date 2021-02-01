// SetCropPatternTS_Command - This class initializes, checks, and runs the SetCropPatternTS() command.

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

import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;
import RTi.TS.YearTS;
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
This class initializes, checks, and runs the SetCropPatternTS() command.
*/
public class SetCropPatternTS_Command extends AbstractCommand implements Command
{
	
/**
Flags for "ProcessWhen" property:
*/
protected final String _Now = "Now";
protected final String _WithParcels = "WithParcels";

protected final String _True = "True";
protected final String _False = "False";

protected final String _Sprinkler = "Sprinkler";
protected final String _Flood = "Flood";

protected final String _Surface = "Surface";
protected final String _Ground = "Ground";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

private String [] __cropTypes = null;
private  double [] __areas = null;

/**
Constructor.
*/
public SetCropPatternTS_Command ()
{	super();
	setCommandName ( "SetCropPatternTS" );
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
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String CropPattern = parameters.getValue ( "CropPattern" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
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
	
	if ( ((CropPattern == null) || (CropPattern.length() == 0)) &&
		((SetToMissing == null) || (SetToMissing.length() == 0)) ) {
		message = "The crop pattern or SetToMissing=" + _True + " must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the crop pattern or SetToMissing=" + _True + "." ) );
	}
	
	if ( ((CropPattern != null) && (CropPattern.length() > 0)) &&
		((SetToMissing != null) && (SetToMissing.length() > 0)) ) {
		message = "The crop pattern and SetToMissing=" + _True + " cannot both be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the crop pattern or SetToMissing=" + _True + "." ) );
	}
	
	// Parse the crop patterns - check the data and keep for use when running
	
	if ( (CropPattern != null) && (SetToMissing == null) ) {
		List<String> tokens = StringUtil.breakStringList ( CropPattern,	", ", StringUtil.DELIM_SKIP_BLANKS );
		if ( tokens == null ) {
			message = "The crop pattern is not valid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern as Crop,Area,..." ) );
		}
		int size = tokens.size();
		if ( (size%2) != 0 ) {
			message = "The crop pattern is not valid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern (" + CropPattern + ") as Crop,Area,..." ) );
		}
		__cropTypes = new String[size/2];
		__areas = new double[size/2];
		for ( int i = 0; i < size; i++ ) {
			if ( i%2 == 0 ) {
				// Even, crop names....
				__cropTypes[i/2] = tokens.get(i);
			}
			else {
				// Odd... area...
				if ( !StringUtil.isDouble( (String)tokens.get(i) ) ) {
					message = "CropPattern " + CropPattern + " value " + (String)tokens.get(i) +
					" is not a number.";
					warning += "\n" + message;
					status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern area as a number." ) );
				}
				else {
					__areas[i/2] = StringUtil.atod(tokens.get(i) );
				}
			}
		}
	}
	
	if ( (IrrigationMethod != null) && !IrrigationMethod.equalsIgnoreCase(_Flood) &&
		!IrrigationMethod.equalsIgnoreCase(_Sprinkler) ) {
		message = "The irrigation method (" + IrrigationMethod + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the irrigation method as " + _Flood + " or " + _Sprinkler + ".") );
	}
	
	// TODO smalers 2020-02-15 Only if StateDMI before 5.00.00
	String SupplyType = parameters.getValue ( "SupplyType" );
	if ( (SupplyType != null) && !SupplyType.equalsIgnoreCase(_Ground) &&
		!SupplyType.equalsIgnoreCase(_Surface) ) {
		message = "The supply type (" + SupplyType + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the supply type as " + _Ground + " or " + _Surface + ".") );
	}

	// TODO smalers 2021-01-18 need to get this to show up in command status.
	if ( (SupplyType != null) && !SupplyType.isEmpty() ) {
		message = "The SupplyType parameter (" + SupplyType + ") is not used for StateDMI version >= 5.00.00 - ignoring.";
		// Don't increment the warning because it will cause the command to not be run.
		//warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			// Show as info since if a warning it causes the command to not run.
			// - info does not seem to be processed properly - results in black dots in left gutter
			//new CommandLogRecord(CommandStatusType.WARNING,
			//new CommandLogRecord(CommandStatusType.INFO,
			new CommandLogRecord(CommandStatusType.SUCCESS,
				message, "Need to update the commands to latest syntax.") );
	}
	
	if ( (SetToMissing != null) && (SetToMissing.length() > 0) &&
		!SetToMissing.equalsIgnoreCase(_False) && !SetToMissing.equalsIgnoreCase(_True) ) {
		message = "The SetToMissing value (" + SetToMissing + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify SetToMissing as " + _False + " (default) or " + _True + ".") );
	}
	
	if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels) &&
		((ID != null) && (ID.length() == 0) || (ID.indexOf("*") >= 0)) ) {
		message = "A wildcard ID cannot be specified with ProcessWhen=" + _WithParcels;
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Provide a single identifier to set the crop pattern.") );
	}
	if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels) &&
		(SetToMissing != null) && SetToMissing.equalsIgnoreCase(_True) ) {
			message = "ProcessWhen=" + _WithParcels + " cannot be specified when SetToMissing=" + _True + ".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify crop parcel data (no missing allowed).") );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(9);
    validList.add ( "ID" );
	validList.add ( "SetStart" );
	validList.add ( "SetEnd" );
	validList.add ( "CropPattern" );
	validList.add ( "IrrigationMethod" );
	// TODO smalers 2020-02-15 Only if before StateDMI 5.00.00 - allow as valid but print warning above
	validList.add ( "SupplyType" );
	validList.add ( "SetToMissing" );
	validList.add ( "ProcessWhen" );
	validList.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
	return (new SetCropPatternTS_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the command.
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
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String Units = parameters.getValue ( "Units" );
	if ( (Units == null) || Units.equals("") ) {
		Units = "ACFT"; // Default
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
	//String CropPattern = parameters.getValue ( "CropPattern" ); parsed in checkCommandParameters
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	// TODO smalers 2020-02-15 not needed for StateDMI 5+
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	boolean SetToMissing_boolean = false;
	if ( (SetToMissing == null) || SetToMissing.equals("") ) {
		SetToMissing = _False; // Default
	}
	else if ( SetToMissing.equalsIgnoreCase(_True) ) {
		SetToMissing_boolean = true;
	}
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	if ( (ProcessWhen == null) || ProcessWhen.equals("") ) {
		ProcessWhen = _Now; // Default
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of crop pattern time series...
	
	List<StateCU_CropPatternTS> cdsList = null;
	int cdsListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents( "StateCU_CropPatternTS_List");
		cdsList = dataList;
		cdsListSize = cdsList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropPatternTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	// Get the list of CU locations, used to check the data ...
	
	List<StateCU_Location> culocList = null;
	int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of additional parcel data, in order to append to that list...
	
	/* TODO smalers 2020-02-15 the following is from StateDMI < 5.00.00
	List<StateDMI_HydroBase_ParcelUseTS> supplementalParcelList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateDMI_HydroBase_ParcelUseTS> dataList = (List<StateDMI_HydroBase_ParcelUseTS>)processor.getPropContents( "ParcelUseTS_FromSet_List");
		supplementalParcelList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting ParcelUseTS_FromSet_List from processor (" + e + ").";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	*/

	/*
	List<HydroBase_ParcelUseTS_FromSet> supplementalParcelList = null;
	try {
		@SuppressWarnings("unchecked")
		List<HydroBase_ParcelUseTS_FromSet> dataList =
		    (List<HydroBase_ParcelUseTS_FromSet>)processor.getPropContents( "HydroBase_ParcelUseTS_FromSet_List");
		supplementalParcelList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting ParcelUseTS_FromSet_List from processor (" + e + ").";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	*/
	
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
		message = "Set start (and output period) has not been specified - cannot set crop pattern time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set start." ) );
	}
	if ( SetEnd_int < 0 ) {
		message = "Set end (and output period) has not been specified - cannot set crop pattern time series.";
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
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	int matchCount = 0;
	try {
		StateCU_CropPatternTS cds = null;
		String cds_id;
		int year = 0;
		List<String> crop_names;	// From an existing CropPatternTS
		int ncrop_names = 0;
		double missing;	// Used to set data to missing
		YearTS ts = null;	// Crop time series to process.
		DateTime date = new DateTime(DateTime.PRECISION_YEAR);
		if ( ProcessWhen.equalsIgnoreCase(_Now) ) {
			// Loop through the crop pattern data and try to find matching records...
			for (int i = 0; i < cdsListSize; i++) {
				cds = cdsList.get(i);
				cds_id = cds.getID();
				// Get the StateCU_Location, used below to track which years have set commands.
				int culocPos = StateCU_Util.indexOf(culocList,cds_id);
				if ( !cds_id.matches(idpattern_Java) ) {
					// Identifier does not match...
					continue;
				}

				// Have a match so reset or save the data.
				++matchCount;

				// Reset the data.  First set the existing crop patterns for the location to zero.
				// This will ensure that any crops not mentioned in the command are set to zero for the given years...

				crop_names = cds.getCropNames();
				ncrop_names = 0;
				if ( crop_names != null ) {
					ncrop_names = crop_names.size();
				}
				for ( int ic = 0; ic < ncrop_names; ic++ ) {
					ts = cds.getCropPatternTS ( crop_names.get(ic) );
					missing = ts.getMissing();
					for ( year = SetStart_int; year <= SetEnd_int; year++ ) {
						date.setYear(year);
						if ( SetToMissing_boolean ) {
							// Set the crop value to missing...
							Message.printStatus(2, routine, "Setting \"" + cds_id + "\" year " + year + " crop \"" + crop_names.get(ic) + " to missing value " + missing );
							ts.setDataValue ( date, missing );
						}
						else {
							// Replace or add in the crop pattern list.
							// Pass individual fields because may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified
							crop_names.get(ic),
							0.0,
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );
						}

						// Indicate that location has a set command, used with parcel report file.
						if ( culocPos >= 0 ) {
							StateCU_Location culoc = culocList.get(culocPos);
							culoc.setHasSetCropPatternTSCommands(year);
						}
					}
				}

				// Now reset to new data.  The number of crops does not
				// need to match the original...

				if ( !SetToMissing_boolean ) {
					for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
						for ( year = SetStart_int; year <= SetEnd_int; year++){
							// Replace or add in crop pattern time series list.
							// Pass individual fields because may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified.
							__cropTypes[ic],
							__areas[ic],
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );

							// Indicate that location has a set command, used with parcel report file.
							if ( culocPos >= 0 ) {
								StateCU_Location culoc = culocList.get(culocPos);
								culoc.setHasSetCropPatternTSCommands(year);
							}
						}
					}
				}

				// Refresh the contents to calculate total area.  If all time
				// series are missing, this will result in missing in the total.
				cds.refresh();
			}
		}
		else {
			// As of version 5.00.00 this is not supported.
			message = "ProcessWhen=" + _WithParcels + " is no longer supported.";
        	status.addToLog ( CommandPhaseType.RUN,
       			new CommandLogRecord(CommandStatusType.FAILURE,message, "Use SetParcel to change parcel data." ) );
			Message.printWarning(3, routine, message );
			/*
			// ProcessWhen=WithParcels
			// Save the data so that it can be processed later when records are read from HydroBase.
			// Add a record for each year/crop/structure combination, as if a data
			// value had been read from HydroBase.  For each combination, print
			// a warning if an existing record is found.
			// Initialize the "has been processed" flag to false.  This will be
			// checked later to make sure the data are not used more than once.
			//int [] wdid_parts = new int[2];
			// TODO smalers 2020-02-15 Code before StateDMI 5.00.00 and generics mixed types unnecessarily
			//StateDMI_HydroBase_StructureView sits, sits2;
			//StateDMI_HydroBase_ParcelUseTS parcel, parcel2;
			HydroBase_ParcelUseTS_FromSet parcel, parcel2;
			int i2, size2;	// For loops.

			// Make sure that the identifier that is specified is found in locations or aggregate lists
			StateCU_Location culoc = null;
			for (int i = 0; i < culocListSize; i++) {
				culoc = culocList.get(i);
				String culoc_id = culoc.getID();
				if ( culoc_id.equalsIgnoreCase(ID) ) {
					// Identifier for command data matches a location
				    ++matchCount;
				    break;
				}
				// Also check if the ID is in a collection
				if ( culoc.isIdInCollection(ID) ) {
					// Identifier for command data matches a collection ID
				    ++matchCount;
				    break;
				}
			}

			for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
				for ( year = SetStart_int; year <= SetEnd_int; year++ ){
					parcel = new HydroBase_ParcelUseTS_FromSet();
					parcel.setLocationID ( ID );
					parcel.setCal_year ( year );
					parcel.setLand_use ( __cropTypes[ic] );
					parcel.setArea ( __areas[ic] );
					parcel.setIrrig_type ( IrrigationMethod );
					// TODO smalers 2020-02-15 SupplyType not used for StateDMI >= 5.00.00
					//parcel.setSupply_type ( SupplyType );
					// Check for duplicates and print a warning...
					size2 =	supplementalParcelList.size();
					for ( i2 = 0; i2 < size2; i2++ ) {
						parcel2 = supplementalParcelList.get(i2);
						if (	
							parcel2.getLocationID().equalsIgnoreCase(ID)
							&& (parcel2.getCal_year() == year)
							&& parcel2.getLand_use().equalsIgnoreCase(__cropTypes[ic])
							&& parcel2.getIrrig_type().equalsIgnoreCase(IrrigationMethod)
							) {
							// TODO smalers 2020-02-15 The following was used with StateDMI < 5.00.00
							//&& parcel2.getSupply_type().equalsIgnoreCase(SupplyType) ) {
							// Matching record, print warning...
							message = "Crop pattern matches existing user-supplied data.  Using again but needs checked.";
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Check user-supplied data for duplicates." ) );
						}
					}
					// In any case, add to the list for later use...
					supplementalParcelList.add ( parcel );
					Message.printStatus ( 2, routine,
					"Location " + ID + " saving supplemental acreage data to use " +
					"later with ReadCropPatternTSFromHydroBase() for: year=" + parcel.getCal_year() +
					" crop=" + parcel.getLand_use() + " IrrigationMethod=" + parcel.getIrrig_type() +
					// TODO smalers 2020-02-15 the following was used for StateDMI < 5.00.00
					//" SupplyType=" + parcel.getSupply_type() +
					" acres=" +	StringUtil.formatString(parcel.getArea(),"%.3f") );
				}
			}
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
		*/
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting crop pattern time series (" + e + ").";
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

// TODO smalers 2020-02-15 This version is used for StateDMI pre-5.00.00
/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
This version is for StateDMI older than 5.00.00 - implementing generics pointed out some confusing old code.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
/*
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
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String Units = parameters.getValue ( "Units" );
	if ( (Units == null) || Units.equals("") ) {
		Units = "ACFT"; // Default
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
	//String CropPattern = parameters.getValue ( "CropPattern" ); parsed in checkCommandParameters
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	boolean SetToMissing_boolean = false;
	if ( (SetToMissing == null) || SetToMissing.equals("") ) {
		SetToMissing = _False; // Default
	}
	else if ( SetToMissing.equalsIgnoreCase(_True) ) {
		SetToMissing_boolean = true;
	}
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	if ( (ProcessWhen == null) || ProcessWhen.equals("") ) {
		ProcessWhen = _Now; // Default
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of crop pattern time series...
	
	List<StateCU_CropPatternTS> cdsList = null;
	int cdsListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents( "StateCU_CropPatternTS_List");
		cdsList = dataList;
		cdsListSize = cdsList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropPatternTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	// Get the list of CU locations, used to check the data ...
	
	List<StateCU_Location> culocList = null;
	int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of additional parcel data, in order to append to that list...
	
	List<StateDMI_HydroBase_ParcelUseTS> supplementalParcelList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateDMI_HydroBase_ParcelUseTS> dataList = (List<StateDMI_HydroBase_ParcelUseTS>)processor.getPropContents( "ParcelUseTS_FromSet_List");
		supplementalParcelList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting ParcelUseTS_FromSet_List from processor (" + e + ").";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
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
		message = "Set start (and output period) has not been specified - cannot set crop pattern time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set start." ) );
	}
	if ( SetEnd_int < 0 ) {
		message = "Set end (and output period) has not been specified - cannot set crop pattern time series.";
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
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	int matchCount = 0;
	try {
		StateCU_CropPatternTS cds = null;
		String cds_id;
		int year = 0;
		List<String> crop_names;	// From an existing CropPatternTS
		int ncrop_names = 0;
		double missing;	// Used to set data to missing
		YearTS ts = null;	// Crop time series to process.
		DateTime date = new DateTime(DateTime.PRECISION_YEAR);
		if ( ProcessWhen.equalsIgnoreCase("Now") ) {
			// Loop through the crop pattern data and try to find matching records...
			for (int i = 0; i < cdsListSize; i++) {
				cds = cdsList.get(i);
				cds_id = cds.getID();
				if ( !cds_id.matches(idpattern_Java) ) {
					// Identifier does not match...
					continue;
				}
				++matchCount;

				// Have a match so reset or save the data.

				// Reset the data.  First set the existing crop patterns for the location to zero.
				// This will ensure that any crops not mentioned in the command are set to zero for the
				// given years...

				crop_names = cds.getCropNames();
				ncrop_names = 0;
				if ( crop_names != null ) {
					ncrop_names = crop_names.size();
				}
				for ( int ic = 0; ic < ncrop_names; ic++ ) {
					ts = cds.getCropPatternTS ( crop_names.get(ic) );
					missing = ts.getMissing();
					for ( year = SetStart_int; year <= SetEnd_int; year++ ) {
						date.setYear(year);
						if ( SetToMissing_boolean ) {
							// Set the crop value to missing...
							ts.setDataValue ( date, missing );
						}
						else {
							// Replace or add in the crop pattern list.
							// Pass individual fields because may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified
							crop_names.get(ic),
							0.0,
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );
						}
					}
				}

				// Now reset to new data.  The number of crops does not
				// need to match the original...

				if ( !SetToMissing_boolean ) {
					for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
						for ( year = SetStart_int; year <= SetEnd_int; year++){
							// Replace or add in crop pattern time series list.
							// Pass individual fields because may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified.
							__cropTypes[ic],
							__areas[ic],
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );
						}
					}
				}

				// Refresh the contents to calculate total area.  If all time
				// series are missing, this will result in missing in the total.
				cds.refresh();
			}
		}
		else {
			// ProcessWhen=WithParcels
			// Save the data so that it can be processed later when records are read from HydroBase.
			// Add a record for each year/crop/structure combination, as if a data
			// value had been read from HydroBase.  For each combination, print
			// a warning if an existing record is found.
			// Initialize the "has been processed" flag to false.  This will be
			// checked later to make sure the data are not used more than once.
			//int [] wdid_parts = new int[2];
			//StateDMI_HydroBase_StructureView sits, sits2;
			StateDMI_HydroBase_ParcelUseTS parcel, parcel2;
			int i2, size2;	// For loops.

			// Make sure that the identifier that is specified is found in locations or aggregate lists
			StateCU_Location culoc = null;
			for (int i = 0; i < culocListSize; i++) {
				culoc = culocList.get(i);
				String culoc_id = culoc.getID();
				if ( culoc_id.equalsIgnoreCase(ID) ) {
					// Identifier for command data matches a location
				    ++matchCount;
				    break;
				}
				// Also check if the ID is in a collection
				if ( culoc.isIdInCollection(ID) ) {
					// Identifier for command data matches a collection ID
				    ++matchCount;
				    break;
				}
			}

			for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
				for ( year = SetStart_int; year <= SetEnd_int; year++ ){
					parcel = new StateDMI_HydroBase_ParcelUseTS();
					parcel.setLocationID ( ID );
					parcel.setCal_year ( year );
					parcel.setLand_use ( __cropTypes[ic] );
					parcel.setArea ( __areas[ic] );
					parcel.setIrrig_type ( IrrigationMethod );
					parcel.setSupply_type ( SupplyType );
					// Check for duplicates and print a warning...
					size2 =	supplementalParcelList.size();
					for ( i2 = 0; i2 < size2; i2++ ) {
						parcel2 = supplementalParcelList.get(i2);
						if (	
							parcel2.getLocationID().equalsIgnoreCase(ID) &&
							(parcel2.getCal_year() == year) &&
							parcel2.getLand_use().equalsIgnoreCase(__cropTypes[ic]) &&
							parcel2.getIrrig_type().equalsIgnoreCase(IrrigationMethod) &&
							parcel2.getSupply_type().equalsIgnoreCase(SupplyType) ) {
							// Matching record, print warning...
							message = "Crop pattern matches existing user-supplied data.  Using again but needs checked.";
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Check user-supplied data for duplicates." ) );
						}
					}
					// In any case, add to the list for later use...
					supplementalParcelList.add ( parcel );
					Message.printStatus ( 2, routine,
					"Location " + ID + " saving supplemental acreage data to use " +
					"later with ReadCropPatternTSFromHydroBase() for: year=" + parcel.getCal_year() +
					" crop=" + parcel.getLand_use() + " IrrigationMethod=" + parcel.getIrrig_type() +
					" SupplyType=" + parcel.getSupply_type() +
					" acres=" +	StringUtil.formatString(parcel.getArea(),"%.3f") );
				}
			}
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
        message = "Unexpected error setting crop pattern time series (" + e + ").";
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
*/

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
	String CropPattern = parameters.getValue ( "CropPattern" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
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
	if ( CropPattern != null && CropPattern.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropPattern=\"" + CropPattern + "\"");
	}
	if ( IrrigationMethod != null && IrrigationMethod.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigationMethod=" + IrrigationMethod );
	}
	if ( SupplyType != null && SupplyType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SupplyType=" + SupplyType );
	}
	if ( SetToMissing != null && SetToMissing.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetToMissing=" + SetToMissing );
	}
	if ( ProcessWhen != null && ProcessWhen.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessWhen=" + ProcessWhen );
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
