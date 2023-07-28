// FillCropPatternTS_Command - This class initializes, checks, and runs the FillCropPatternTS*() commands.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2023 Colorado Department of Natural Resources

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
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;
import RTi.TS.TS;
import RTi.TS.TSUtil;
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
import RTi.Util.Time.TimeInterval;

/**
This class initializes, checks, and runs the FillCropPatternTS*() commands.
It is extended by the specific fill commands.
*/
public abstract class FillCropPatternTS_Command
extends AbstractCommand implements Command
{

/**
Values for fill direction.
*/
protected final String _Forward = "Forward";
protected final String _Backward = "Backward";

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
Constructor.
*/
public FillCropPatternTS_Command () {
	super();
	setCommandName ( "FillCropPatternTS?" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String routine = getClass().getSimpleName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String CropType = parameters.getValue ( "CropType" );
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String NormalizeTotals = parameters.getValue ( "NormalizeTotals" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String FillDirection = parameters.getValue ( "FillDirection" );
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
	String Constant = parameters.getValue ( "Constant" );
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
				message, "Specify the location identifier pattern to match." ) );
	}

	if ( (CropType == null) || (CropType.length() == 0) ) {
		message = "A crop type must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the crop type to match." ) );
	}

	if ( (IncludeSurfaceWaterSupply != null) && (IncludeSurfaceWaterSupply.length() > 0) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_True) ) {
		message = "The IncludeSurfaceWaterSupply value (" + IncludeSurfaceWaterSupply + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IncludeSurfaceWaterSupply as " + _False +
					" or " + _True + " (default=" + _True + ").") );
	}

	if ( (IncludeGroundwaterOnlySupply != null) && (IncludeGroundwaterOnlySupply.length() > 0) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_True) ) {
		message = "The IncludeGroundwaterOnlySupply value (" + IncludeGroundwaterOnlySupply + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IncludeGroundwaterOnlySupply as " + _False +
					" or " + _True + " (default=" + _True + ").") );
	}

	if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
		if ( (NormalizeTotals != null) && (NormalizeTotals.length() > 0) &&
			!NormalizeTotals.equalsIgnoreCase(_False) &&
			!NormalizeTotals.equalsIgnoreCase(_True) ) {
			message = "The NormalizeTotals value (" + NormalizeTotals + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify NormalizeTotals as " + _False + " or " + _True +
						" (default=" + _False + " for one crop or " + _True + " for multiple crops).") );
		}
	}

	if ( (FillStart != null) && (FillStart.length() > 0) && !StringUtil.isInteger(FillStart) ) {
		message = "The FillStart value (" + FillStart + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify FillStart as blank (fill all years) or a 4-digit integer year to start filling.") );
	}

	if ( (FillEnd != null) && (FillEnd.length() > 0) && !StringUtil.isInteger(FillEnd) ) {
		message = "The FillEnd value (" + FillEnd + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify FillEnd as blank (fill all years) or a 4-digit integer year to end filling.") );
	}

	if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
		if ( (ParcelYear == null) || (ParcelYear.length() == 0) ) {
			message = "The ParcelYear value must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify ParcelYear as a 4-digit integer year corresponding to well rights.") );
		}
		else if ( !StringUtil.isInteger(ParcelYear) ) {
			message = "The ParcelYear value (" + ParcelYear + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify ParcelYear as a 4-digit integer year corresponding to well rights.") );
		}
		if ( (IncludeSurfaceWaterSupply != null) && IncludeSurfaceWaterSupply.equalsIgnoreCase(_True)) {
			message = "IncludeSurfaceWaterSupply=True CANNOT be used when filling with rights.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IncludeSurfaceWaterSupply=False if filling with well rights.") );
		}
	}

	if ( this instanceof FillCropPatternTSInterpolate_Command ||
		this instanceof FillCropPatternTSRepeat_Command ) {
		if ( (FillDirection != null) && (FillDirection.length() > 0) &&
			!FillDirection.equalsIgnoreCase(_Backward) && !FillDirection.equalsIgnoreCase(_Forward) ) {
			message = "The FillDirection value (" + FillDirection + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify FillDirection as " + _Backward + " or " + _Forward +
					" (default=" + _Forward + ").") );
		}

		if ( (MaxIntervals != null) && (MaxIntervals.length() > 0) && !StringUtil.isInteger(MaxIntervals) ) {
			message = "The MaxIntervals value (" + MaxIntervals + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify MaxIntervals as blank (fill all gaps) or an integer for gap years to fill.") );
		}
	}

	if ( this instanceof FillCropPatternTSConstant_Command ) {
		if ( (Constant == null) || (Constant.length() == 0) ) {
			message = "The constant value has not been specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the contant value." ) );
		}
		else if ( !StringUtil.isDouble(Constant) ) {
			message = "The Constant value (" + Constant + ") is not a valid number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify Constant as a number to use for filling.") );
		}
	}

	/* TODO SAM 2009-01-03 Evaluate if needed - already done above?
	if (	(!fill_agstats || (fill_agstats && !NormalizeTotals_boolean))&&
			(CropType.indexOf(",") > 0) ) {
			// Comma-separated list of crop types is only valid for
			// filling with proration when NormalizeTotals=true...
			Message.printWarning ( 2,
			formatMessageTag(command_tag,++warning_count), routine,
			"CropType can contain commas only when prorating AgStats and "+
			"NormalizeTotals=True." );
			throw new Exception (
				"Bad NormalizeTotals in command \"" + command + "\"");
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

	// Check for invalid parameters.
	List<String> validList = new ArrayList<>();
    validList.add ( "ID" );
	validList.add ( "IncludeSurfaceWaterSupply" );
    validList.add ( "IncludeGroundwaterOnlySupply" );
	validList.add ( "CropType" );
	if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
		validList.add ( "NormalizeTotals" );
	}
	validList.add ( "FillStart" );
	validList.add ( "FillEnd" );
	if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
		validList.add ( "ParcelYear" );
	}
	if (this instanceof FillCropPatternTSProrateAgStats_Command ||
		this instanceof FillCropPatternTSRepeat_Command ) {
		validList.add ( "FillDirection" );
	}
	if ( (this instanceof FillCropPatternTSInterpolate_Command) ||
		(this instanceof FillCropPatternTSRepeat_Command) ) {
		validList.add ( "FillFlag" );
	}
	if ( (this instanceof FillCropPatternTSInterpolate_Command) ||
		(this instanceof FillCropPatternTSRepeat_Command) ) {
		validList.add ( "FillFlagDescription" );
	}
	if ( this instanceof FillCropPatternTSInterpolate_Command ||
		this instanceof FillCropPatternTSRepeat_Command ) {
		validList.add ( "MaxIntervals" );
	}
	if ( this instanceof FillCropPatternTSConstant_Command ) {
		validList.add ( "Constant" );
	}
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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent ) {
	// The command will be modified if changed (true indicates fill)>
	return (new FillCropPatternTS_JDialog ( parent, this )).ok();
}

// Parse command is in the base class>

/**
Fill the crop pattern time series using water rights.
Do this by getting the crop pattern data for the parcel year
(which has references to the parcels used to create the data)
and check those parcels for whether they have rights in a year.  If they do, include the parcels.
@param werYearTSList List of YearTS for the rights for parcels.
This will be used to look up a time series for the matching parcel.
@param cdsts The CropPatternTS that is having a component filled,
passed in because the stored parcel information is at this level.
@param cropName The crop time series being filled.
It is also passed in to avoid issues pulling out the crop name from a composite data type in "yts".
@param yts The specific crop time series being filled.
@param FillStart_DateTime The starting date/time for filling.
@param FillEnd_DateTime The ending date/time for filling.
@param ParcelYear_int the parcel year to use for parcel data.
*/
private int fillCropPatternTSUsingRights (
		List<TS> werYearTSList, StateCU_CropPatternTS cdsts, String cropName, YearTS yts,
		DateTime FillStart_DateTime, DateTime FillEnd_DateTime, int ParcelYear_int,
		int warning_level, int warning_count, String command_tag, CommandStatus status ) {
	String routine = getClass().getSimpleName() + ".fillCropPatternTSUsingRights";
	String tsid = yts.getIdentifier().toString();
	Message.printStatus( 2, routine, "Filling time series \"" +
		tsid + "\" " +
		FillStart_DateTime + " to " + FillEnd_DateTime + " by using " + ParcelYear_int +
		" year parcel data and rights associated with parcels.");
	// Get the parcels for the crop pattern TS for the year and crop in question.
	List<StateCU_Parcel> parcels = cdsts.getParcelListForYearAndCropName ( ParcelYear_int, cropName );
	int nparcel = 0;
	if ( parcels != null ) {
		nparcel = parcels.size();
	}
	Message.printStatus( 2, routine, "Have " + nparcel + " parcels for " + ParcelYear_int + ", " +
		cropName + " to check for non-zero decree." );
	// Loop through the period to be filled.
	StateCU_Parcel parcel = null; // Individual parcel to process.
	// Debugging...
	for ( int i = 0; i < nparcel; i++ ) {
		parcel = parcels.get(i);
		Message.printStatus ( 2, routine, parcel.toString() );
	}
	double parcelRightDecree; // Decree for a parcel at a point in time.
	double parcelArea; // Area to add for parcel..
	TS parcelRightTS = null; // Time series of rights for parcel.
	int pos = 0; // Position in right time series array.
	double ytsValue; // Area value of time series being filled.

	// First figure out if the time series is missing for a year.
	// If the original value was NOT missing, then filling can be skipped below.
	int nyears = FillEnd_DateTime.getYear() - FillStart_DateTime.getYear() + 1;
	boolean [] ismissing = new boolean[nyears];
	for ( int i = 0; i < nyears; i++ ) {
		ismissing[i] = false;
	}
	int iyear = 0;
	for ( DateTime date = new DateTime(FillStart_DateTime);
		date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1), iyear++ ) {
		ytsValue = yts.getDataValue ( date );
		if ( yts.isDataMissing(ytsValue)) {
			ismissing[iyear] = true;
		}
	}

	// Loop through the parcels and process those that have the matching crop.
	// If no parcels are available in the indicator year, then fill each year with zero.

	if ( nparcel == 0 ) {
		DateTime date = new DateTime(FillStart_DateTime);
		for ( iyear = 0;
		date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1), iyear++ ) {
			if ( ismissing[iyear]) {
				yts.setDataValue ( date, 0.0 );
			}
		}
	}
	for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
		parcel = (StateCU_Parcel)parcels.get(iparcel);
		// Get the time series of rights corresponding to the parcel.
		pos = TSUtil.indexOf ( werYearTSList, parcel.getID(), "Location", 1 );
		boolean parcel_always_off = false;
		if ( pos < 0 ) {
			// No time series for the parcel.
			String message = "For time series \"" + tsid +
			"\", no time series of rights is available for parcel " + parcel.getID() +
			".  Assuming zero supply - parcel treated as OFF.  HydroBase/model inconsistency (modeling " +
			"assumes well supply but parcel data loaded into HydroBase has none)?";
			Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify that parcel/supply relationships are consistent in GIS and aggregate lists." ) );
			parcel_always_off = true;
		}
		else {
			parcelRightTS = werYearTSList.get(pos);
			Message.printStatus ( 2, routine, "Using right time series for parcel " + parcel.getID() );
		}
		// Now loop through the years to fill.
		DateTime date = new DateTime(FillStart_DateTime);
		for ( iyear = 0;
			date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1), iyear++ ) {
			// If the original time series was NOT missing, don't fill.
			if ( !ismissing[iyear]) {
				continue;
			}
			// Check to see if the parcel rights in the year in question are // > 0.0.
			// If so, the parcel is on.  Since filling add a zero because it is treated like an observation.
			if ( parcel_always_off ) {
				parcelRightDecree = 0.0;
			}
			else {
				parcelRightDecree = parcelRightTS.getDataValue ( date );
			}
			if ( parcelRightDecree > 0.0 ) {
				parcelArea = parcel.getArea();
				Message.printStatus( 2, routine, "Parcel " + parcel.getID() + " is ON for " + cropName +
					" in " + date.getYear() + " Adding " + parcelArea );
			}
			else {
				parcelArea = 0.0;
				Message.printStatus( 2, routine, "Parcel " + parcel.getID() + " is OFF for " + cropName +
						" in " + date.getYear() );
			}
			ytsValue = yts.getDataValue ( date );
			if ( yts.isDataMissing(ytsValue) ) {
				// Just set the value.
				yts.setDataValue ( date, parcelArea );
			}
			else {
				// Increment the acreage.
				yts.setDataValue ( date, (ytsValue + parcelArea) );
			}
		}
	}
	return warning_count;
}

/**
Run the command command.
@param command_number Number of command in sequence.
@param fill if True, then the fill command is being run.  If false, the set command is being run.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException {
	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;

	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);

	// Get the input parameters.

	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default.
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	boolean IncludeSurfaceWaterSupply_boolean = true; // Default.
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	if ( (IncludeSurfaceWaterSupply != null) && IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) ) {
		IncludeSurfaceWaterSupply_boolean = false;
	}
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	boolean IncludeGroundwaterOnlySupply_boolean = true; // Default.
	if ( (IncludeGroundwaterOnlySupply != null) && IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) ) {
		IncludeGroundwaterOnlySupply_boolean = false;
	}
	String CropType = parameters.getValue ( "CropType" );
	if ( CropType == null ) {
		CropType = "*";
	}
	String croppattern_Java = StringUtil.replaceString( CropType,"*",".*");
	String NormalizeTotals = parameters.getValue ( "NormalizeTotals" );//AgStats.
	// TODO SAM 2005-07-31 Could be more robust by looping through all crops to see how many crop type matches are found.
	List<String> cropTypeList = new ArrayList<>(); // List of crop types to process.
	int cropTypeList_size = 0;
	if ( NormalizeTotals == null ) { // Only used by ProrateAgStats.
		// Figure out the default.
		if ( CropType.indexOf("*") >= 0 ) {
			// Assume that more than one crop will be processed.
			NormalizeTotals = _True;
		}
		else if ( CropType.indexOf(",") > 0 ) {
			// Assume that more than one crop will be processed, and determine the list of crops.
			NormalizeTotals = _True;
			cropTypeList = StringUtil.breakStringList( CropType, ", ",StringUtil.DELIM_SKIP_BLANKS);
			cropTypeList_size = cropTypeList.size();
		}
		else {
			// Assume that one crop will be processed.
			NormalizeTotals = _False;
		}
	}
	boolean NormalizeTotals_boolean = false;
	if ( (NormalizeTotals != null) && NormalizeTotals.equalsIgnoreCase(_True) ) {
		NormalizeTotals_boolean = true;
	}
	else if ( (NormalizeTotals != null) && NormalizeTotals.equalsIgnoreCase(_False) ) {
		NormalizeTotals_boolean = false;
	}
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	int ParcelYear_int = -1;
	if ( StringUtil.isInteger(ParcelYear)) {
		ParcelYear_int = Integer.parseInt(ParcelYear);
	}
	String FillDirection = parameters.getValue ( "FillDirection" );
	int FillDirection_int = 1; // Default (forward).
	if ( (FillDirection != null) && FillDirection.equalsIgnoreCase(_Backward) ) {
		FillDirection_int = -1;
	}
	String FillFlag = parameters.getValue ( "FillFlag" );
	String FillFlagDescription = parameters.getValue ( "FillFlagDescription" );
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
	int MaxIntervals_int = 0; // Default.
	if ( StringUtil.isInteger(MaxIntervals)) {
		MaxIntervals_int = Integer.parseInt(MaxIntervals);
	}
	String Constant = parameters.getValue ( "Constant" );
	double Constant_double = -999.0;
	if ( StringUtil.isDouble(Constant)) {
		Constant_double = Double.parseDouble(Constant);
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default.
	}

	// Get the list of CU locations, needed to check whether surface or ground water.

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
	if ( culocListSize == 0 ) {
		message = "No CU locations available to fill crop patterns - needed to determine supply type.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Read CU locations before using this command." ) );
	}

	// Get the list of crop pattern time series.

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

	// Get well rights and period needed for filling with well rights.
	List<StateMod_WellRight> werList = null;
	int werListSize = 0;
	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
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
			message = "No well rights available to fill crop patterns.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Read well rights before using this command." ) );
		}

		// Get the output period.

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
		if ( OutputStart_DateTime == null ) {
			message = "The output start has not been specified.  It is needed to process well rights.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Use the SetOutputPeriod() command before this command." ) );
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
		if ( OutputEnd_DateTime == null ) {
			message = "The output end has not been specified.  It is needed to process well rights.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Use the SetOutputPeriod() command before this command." ) );
		}
	}

	if ( warning_count > 0 ) {
		// Input error.
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}

	// Now process.

	try {
		PropList fillProrate_PropList = new PropList ("fillProrate");
		// Use if needed.

		double [] observedTotal = null; // Total observed acres for the crops being processed, by year.
		boolean [] observedActual = null; // Which of the values in "observed_total" were actual values and not repeated.
		double [] proratedTotal = null;	// Total prorated acres for the crops being processed, by year.
		double [] agstatsTotal = null; // Total AgStats acres for the crops being processed, by year.
		double [] agstatsObservedTotal = null;// Total AgStats acres for the crops being processed,
					// using values for years where observations were found, by year.
		double observedTotalPrev; // Used with repeat-filling of arrays.
		double agstatsObservedTotalPrev;

		List<TS> werYearTSList = null;
		if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
			// Convert the well rights to annual time series.
			werYearTSList = StateMod_Util.createWaterRightTimeSeriesList (
				werList,
				TimeInterval.YEAR,
				1,	// Aggregate to parcel.
				ParcelYear_int,
				false,	// Do not include data set totals.
				OutputStart_DateTime,
				OutputEnd_DateTime,
				999999.0,
				null,
				null,
				true );	// Do read the data (not just header).
			Message.printStatus ( 2, routine, "Created " + werYearTSList.size()
				+ " parcel/right time series from well water rights." );
			//for ( int i = 0; i < smrights_YearTS_Vector.size(); i++ ) {
			//	Message.printStatus ( 2, routine, "TSID = " +
			//			((TS)smrights_YearTS_Vector.get(i)).getIdentifier() );
			//}
		}

		DateTime FillStart_DateTime = new DateTime(DateTime.PRECISION_YEAR);
		if ( FillStart != null ) {
			FillStart_DateTime.setYear(Integer.parseInt(FillStart));
		}
		else if ( OutputStart_DateTime != null ) {
			FillStart_DateTime.setYear(OutputStart_DateTime.getYear());
		}
		else {
			FillStart_DateTime = null; // Fill all.
		}
		DateTime FillEnd_DateTime = new DateTime(DateTime.PRECISION_YEAR);
		if ( FillEnd != null ) {
			FillEnd_DateTime.setYear(Integer.parseInt(FillEnd));
		}
		else if ( OutputEnd_DateTime != null ) {
			FillEnd_DateTime.setYear(OutputEnd_DateTime.getYear());
		}
		else {
			FillEnd_DateTime = null; // Fill all.
		}

		StateCU_CropPatternTS cupatts = null;
		StateCU_Location culoc = null;
		String id;
		List<String> cropNames = null;
		String cropName = null;
		int ncrops = 0;
		int icrop = 0;
		int pos = 0; // Position of related CULocation.
		String county = null; // County to be used for lookup.
		YearTS yts = null;
		int iyear = 0; // Integer counter for year position in data array.
		int dyear = 0; // Increment for loops on year.
		int year1 = 0, year2 = 0; // Initial and ending values for year.
		DateTime FillStart_DateTime2 = null; // Fill years, accounting for direction.
		DateTime date = null; // Used to iterate through period.
		double factor1, factor2; // Factors used to adjust a data value.
		double value = 0.0, value2 = 0.0; // Data value from time series.
		YearTS countyts = null; // AgStats time series.
		boolean didFill = false; // Did a time series get filled?

		// Set the information used to iterate for prorating AgStats.
		// The "2" dates are reversed from the original if necessary to allow generic for loops.
		if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
			if ( FillDirection_int > 0 ) {
				// Forward.
				FillStart_DateTime2 = new DateTime ( FillStart_DateTime );
				year1 = 0;
				// Use original dates.
				year2 = FillEnd_DateTime.getYear() - FillStart_DateTime.getYear();
				dyear = 1;	// Delta year.
				observedTotal = new double[year2 - year1 + 1];
				observedActual = new boolean[year2 - year1 + 1];
				proratedTotal = new double[year2 - year1 + 1];
				agstatsTotal = new double[year2 - year1 + 1];
				agstatsObservedTotal = new double[year2 - year1 + 1];
			}
			else {
				// Backward.
				FillStart_DateTime2 = new DateTime ( FillEnd_DateTime );
				year1 = FillEnd_DateTime.getYear() - FillStart_DateTime.getYear();
				year2 = 0;
				dyear = -1;	// Delta year.
				observedTotal = new double[year1 - year2 + 1];
				observedActual = new boolean[year1 - year2 + 1];
				proratedTotal = new double[year1 - year2 + 1];
				agstatsTotal = new double[year1 - year2 + 1];
				agstatsObservedTotal = new double[year1 - year2 + 1];
			}
		}

		// Loop through available crop pattern time series and see if any need to be filled.
		// Process each crop time series independently.
		int matchCount = 0; // Track how many IDs result in changes.
		for ( int i = 0; i < cdsListSize; i++ ) {
			cupatts = cdsList.get(i);
			id = cupatts.getID();
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine, "Checking CULocation " + id + " against \"" + idpattern_Java + "\"" );
			}
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match.
				continue;
			}
			// Check whether location supply matches the supply types to be included.
			if ( IncludeSurfaceWaterSupply_boolean || IncludeGroundwaterOnlySupply_boolean ) {
				// Need to get the CU location to check for GW-only.
				pos = StateCU_Util.indexOf ( culocList, id );
				if ( pos < 0 ) {
					message = "Could not find CU location matching \"" + id +
					"\" to determine water supply type.  Not filling.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the ID is correct and that CU locations have been read before this command." ) );
					continue;
				}
				culoc = culocList.get(pos);
			}
			if ( IncludeSurfaceWaterSupply_boolean && IncludeGroundwaterOnlySupply_boolean ) {
				// Including both.
			}
			else {
				// Check that only one type is included so not everything is skipped.
				// TODO SAM 2007-07-12 Need to fix this.
				if ( IncludeSurfaceWaterSupply_boolean && !culoc.hasSurfaceWaterSupplyForModelNode() ) {
					continue;
				}
				if ( IncludeGroundwaterOnlySupply_boolean && !culoc.isGroundwaterOnlySupplyModelNode() ) {
					continue;
				}
			}
			// Loop through and compare a regular expression on each crop type.
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine, "CULocation "+id+" crops being checked for matches...");
			}
			// Get the CU Location for this ID because we need the county in region 1.

			if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
				pos = StateCU_Util.indexOf ( culocList, id );
				if ( pos < 0 ) {
					message = "Could not find CU location matching \"" + id +
					"\" to determine county.  Not filling with Agstats.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the ID is correct and that CU locations have been read before this command." ) );
					continue;
				}
				culoc = culocList.get(pos);
				county = culoc.getRegion1();
				if ( county.length() == 0 ) {
					message = "CU location \"" + id +
					"\" does not have a county specified.  Not filling with AgStats.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the CU location has its county set/filled prior to this command." ) );
					continue;
				}
			}
			cropNames = cupatts.getCropNames();
			ncrops = cropNames.size();
			yts = null;
			didFill = false;
			if ( !(this instanceof FillCropPatternTSProrateAgStats_Command) ||
				((this instanceof FillCropPatternTSProrateAgStats_Command) && !NormalizeTotals_boolean) ) {
				// Process the time series one at a time.
				for ( icrop = 0; icrop < ncrops; icrop++ ) {
					cropName = cropNames.get(icrop);
					if ( cropName.matches(croppattern_Java) ) {
						// Crop matches.
						yts = cupatts.getCropPatternTS ( cropName );
						++matchCount;
						// Reset the data.
						if ( this instanceof FillCropPatternTSConstant_Command ) {
							int nfilled = TSUtil.fillConstant ( yts, FillStart_DateTime,
							FillEnd_DateTime, Constant_double, null );
							Message.printStatus ( 2, routine,
							"FillConstant " + id + "-" + cropName +
							" " + FillStart_DateTime + " to " + FillEnd_DateTime + " (" + nfilled + " values filled).");
							// Indicate that crop pattern time series have been filled for a year:
							// - used in StateDMI parcel report
							// - only set if not already indicated as a set
							// - this indicates if a fill may have occurred
							int yearStart = 0, yearEnd = 0;
							if ( FillStart_DateTime == null ) {
								yearStart = yts.getDate1().getYear();
							}
							else {
								yearStart = FillStart_DateTime.getYear();
							}
							if ( FillEnd_DateTime == null ) {
								yearEnd = yts.getDate2().getYear();
							}
							else {
								yearEnd = FillEnd_DateTime.getYear();
							}
							for ( int year = yearStart; year <= yearEnd; year++ ) {
								if ( !culoc.hasSetCropPatternTSCommands(year) ) {
									// Indicate that crop pattern time series have been filled for a year:
									// - used in StateDMI parcel report
									// - only set if not already indicated as a set
									// - this indicates if a fill may have occurred
									culoc.setHasFillCropPatternTSCommands(year);
								}
							}
						}
						else if ( this instanceof FillCropPatternTSRepeat_Command ) {
							int nfilled = TSUtil.fillRepeat ( yts, FillStart_DateTime,
							FillEnd_DateTime, FillDirection_int, MaxIntervals_int, FillFlag );
							Message.printStatus ( 2, routine,
							"FillRepeat " + id + "-" + cropName +
							" " + FillStart_DateTime + " to " + FillEnd_DateTime+ " (" + nfilled + " values filled)." );
							// Indicate that crop pattern time series have been filled for a year
							// - used in StateDMI parcel report
							// - only set if not already indicated as a set
							// - this indicates if a fill may have occurred
							int yearStart = 0, yearEnd = 0;
							if ( FillStart_DateTime == null ) {
								yearStart = yts.getDate1().getYear();
							}
							else {
								yearStart = FillStart_DateTime.getYear();
							}
							if ( FillEnd_DateTime == null ) {
								yearEnd = yts.getDate2().getYear();
							}
							else {
								yearEnd = FillEnd_DateTime.getYear();
							}
							for ( int year = yearStart; year <= yearEnd; year++ ) {
								if ( !culoc.hasSetCropPatternTSCommands(year) ) {
									// Indicate that crop pattern time series have been filled for a year:
									// - used in StateDMI parcel report
									// - only set if not already indicated as a set
									// - this indicates if a fill may have occurred
									culoc.setHasFillCropPatternTSCommands(year);
								}
							}
						}
						else if ( this instanceof FillCropPatternTSInterpolate_Command ) {
							int nfilled = TSUtil.fillInterpolate ( yts, FillStart_DateTime,
							FillEnd_DateTime, MaxIntervals_int, 0, FillFlag, FillFlagDescription );
							Message.printStatus ( 2, routine, "FillInterpolate " + id + "-" +
							cropName + " " + FillStart_DateTime + " to " + FillEnd_DateTime+ " (" + nfilled + " values filled)." );
							// Indicate that crop pattern time series have been filled for a year:
							// - used in StateDMI parcel report
							// - only set if not already indicated as a set
							// - this indicates if a fill may have occurred
							int yearStart = 0, yearEnd = 0;
							if ( FillStart_DateTime == null ) {
								yearStart = yts.getDate1().getYear();
							}
							else {
								yearStart = FillStart_DateTime.getYear();
							}
							if ( FillEnd_DateTime == null ) {
								yearEnd = yts.getDate2().getYear();
							}
							else {
								yearEnd = FillEnd_DateTime.getYear();
							}
							for ( int year = yearStart; year <= yearEnd; year++ ) {
								if ( !culoc.hasSetCropPatternTSCommands(year) ) {
									// Indicate that crop pattern time series have been filled for a year:
									// - used in StateDMI parcel report
									// - only set if not already indicated as a set
									// - this indicates if a fill may have occurred
									culoc.setHasFillCropPatternTSCommands(year);
								}
							}
						}
						else if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
							// Find a matching AgStats time series using the CU location county and data type (crop type).
							countyts = processor.findAgStatsTS ( county, cropName );
							if ( countyts == null ) {
								message = "Could not find AgStats time series matching county " +
								"\"" + county+"\" and crop \""+ cropName + "\" for ID=\"" + id + "\". Not filling with AgStats.";
								Message.printWarning(warning_level,
									MessageUtil.formatMessageTag( command_tag, ++warning_count),
									routine, message );
								status.addToLog ( CommandPhaseType.RUN,
									new CommandLogRecord(CommandStatusType.FAILURE,
										message, "Verify that the AgStats time series were read using a prior ReadAgStatsTSFromDateValue() command." ) );
								continue;
							}
							int nfilled = TSUtil.fillProrate ( yts, countyts, FillStart_DateTime,
								FillEnd_DateTime, fillProrate_PropList );
							Message.printStatus ( 2, routine, "FillProrateAgStats " + id + " - " +
							county + ", " + cropName + " (" + nfilled + " values filled)." );
							// Indicate that crop pattern time series have been filled for a year:
							// - used in StateDMI parcel report
							// - only set if not already indicated as a set
							// - this indicates if a fill may have occurred
							int yearStart = 0, yearEnd = 0;
							if ( FillStart_DateTime == null ) {
								yearStart = yts.getDate1().getYear();
							}
							else {
								yearStart = FillStart_DateTime.getYear();
							}
							if ( FillEnd_DateTime == null ) {
								yearEnd = yts.getDate2().getYear();
							}
							else {
								yearEnd = FillEnd_DateTime.getYear();
							}
							for ( int year = yearStart; year <= yearEnd; year++ ) {
								if ( !culoc.hasSetCropPatternTSCommands(year) ) {
									// Indicate that crop pattern time series have been filled for a year:
									// - used in StateDMI parcel report
									// - only set if not already indicated as a set
									// - this indicates if a fill may have occurred
									culoc.setHasFillCropPatternTSCommands(year);
								}
							}
						}
						else if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
							// Fill the specific crop using rights, for the requested period.
							warning_count = fillCropPatternTSUsingRights (
									werYearTSList,
									cupatts, cropName, yts,
									FillStart_DateTime,
									FillEnd_DateTime,
									ParcelYear_int,
									warning_level, warning_count, command_tag, status );
							// Indicate that crop pattern time series have been filled for a year:
							// - used in StateDMI parcel report
							// - only set if not already indicated as a set
							// - this indicates if a fill may have occurred
							int yearStart = 0, yearEnd = 0;
							if ( FillStart_DateTime == null ) {
								yearStart = yts.getDate1().getYear();
							}
							else {
								yearStart = FillStart_DateTime.getYear();
							}
							if ( FillEnd_DateTime == null ) {
								yearEnd = yts.getDate2().getYear();
							}
							else {
								yearEnd = FillEnd_DateTime.getYear();
							}
							for ( int year = yearStart; year <= yearEnd; year++ ) {
								if ( !culoc.hasSetCropPatternTSCommands(year) ) {
									// Indicate that crop pattern time series have been filled for a year:
									// - used in StateDMI parcel report
									// - only set if not already indicated as a set
									// - this indicates if a fill may have occurred
									culoc.setHasFillCropPatternTSCommands(year);
								}
							}
						}
						didFill = true;
					}
				}
				if ( (this instanceof FillCropPatternTSConstant_Command) && (ncrops == 0)) {
					// Need to set the total to zero.
					int year_start, year_end;
					if ( FillStart_DateTime != null ) {
						year_start = FillStart_DateTime.getYear();
					}
					else {
						year_start = cupatts.getDate1().getYear();
					}
					if ( FillEnd_DateTime != null ) {
						year_end = FillEnd_DateTime.getYear();
					}
					else {
						year_end = cupatts.getDate2().getYear();
					}
					Message.printStatus ( 2, routine, "FillConstant " + id + " No crops - set total to " +
						Constant + " " + year_start + " to " + year_end );
					for ( int year = year_start; year <= year_end; year++ ) {
						cupatts.setTotalArea ( year, Constant_double );
						// Indicate that crop pattern time series have been filled for a year:
						// - used in StateDMI parcel report
						// - only set if not already indicated as a set
						// - this indicates if a fill may have occurred
						if ( !culoc.hasSetCropPatternTSCommands(year) ) {
							culoc.setHasFillCropPatternTSCommands(year);
						}
					}
				}
			}
			else if ( (this instanceof FillCropPatternTSProrateAgStats_Command) && NormalizeTotals_boolean ) {
				// Need to process the crops in groups based on the list of crops in CropType
				// (specified with * or a comma-separated list of crops).
				// For example, the ALFALFA, POTATOES, and SMALL_GRAINS might be processed together.
				// Each crop time series is prorated and is then adjusted by a ratios involving the acres for all crops in the group,
				// thus reflecting the overall change in acreage in the basin over time.  The computation is:
				//
				// ProratedCrop_year =
				//	AgStatsCrop_year*(ObservedCrop_Obsyear/AgStatsCrop_Obsyear)
				//	*(ObervedAllCrop_Obsyear/ProratedAllCrop_Obsyear)
				//	*(AgStatsAllCrop_year/AgStatsAllCrop_Obsyear)
				//
				// Where:
				//
				//	"_year" is the year being filled
				//	"_Obsyear" is the (nearest) year with observations
				//
				// The values for all crops are managed as arrays sized to the period.
				// To optimize performance, the arrays may be set during other operations.
				// Arrays are set with the following values:
				//
				// ObservedAllCrop_Obsyear = observed_total[]
				//	* Use observations if available.
				//	* Repeat values in other years, in direction of fill.
				// ProratedAllCrop_Obsyear = prorated_total[]
				//	* Add values from simple proration (first term above).
				// AgStatsAllCrop_year = agstats_total[]
				//	* Add AgStats values for all crops.
				// AgStatsAllCrop_Obsyear = agstats_observed_total[]
				//	* Use AgStats values on years where observed_total[] values are known.
				//	* Repeat values in other years, in direction of fill.
				//
				// Loop through the crops to be filled.  The loop here is by requested crops rather than doing a match.
				// However, if the wildcard is used for CropType, do a preliminary loop to get a list of crops for this location.
				didFill = true;	// Always force refresh at end.
				if ( CropType.indexOf("*") >= 0 ) {
					// Wildcards are used.
					// Get all the crop types for the current CU location.
					cropTypeList = new ArrayList<>();
					cropNames = cupatts.getCropNames();
					ncrops = cropNames.size();
					// Loop through the crops and see which ones match - it is conceivable that the wildcard specifies a subset of crops.
					for ( icrop = 0; icrop < ncrops; icrop++ ) {
						cropName = cropNames.get(icrop);
						if ( cropName.matches(croppattern_Java) ) {
							cropTypeList.add (cropName );
						}
					}
					cropTypeList_size = cropTypeList.size();
				}
				// Initialize the arrays to zero.  Zero acreage is a valid value, although the ratios will not work if a divisor is zero).
				for ( date= new DateTime (FillStart_DateTime2),
					iyear = year1; iyear != (year2 + dyear); date.addYear(dyear), iyear += dyear ) {
					observedTotal[iyear] = 0;
					observedActual[iyear] = false;
					proratedTotal[iyear] = 0;
					agstatsTotal[iyear] = 0;
					agstatsObservedTotal[iyear] = 0;
				}
				// Prorate the crop using the AgStats.
				// This loop is used to fill in the values for "observed_total", "observed_actual", "prorated_total", and "agstats_total".
				for ( icrop = 0; icrop < cropTypeList_size; icrop++){
					cropName = cropTypeList.get( icrop );
					// Get the CASS totals for all years and for the years when observations exist.
					// Find a matching AgStats time series using the CU location county and time series data type (crop type).
					countyts = processor.findAgStatsTS ( county, cropName );
					if ( countyts == null ) {
						message = "Could not find AgStats time series matching county " +
						"\"" + county+"\" and crop \""+	cropName + "\". Not filling.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the AgStats time series were read using a prior " +
									"ReadAgStatsTSFromDateValue() command and that county and crop type are consistent." ) );
					}
					// Get the crop time series to fill.
					yts = cupatts.getCropPatternTS ( cropName );
					// Save the totals before filling the time series.
					for ( date= new DateTime(FillStart_DateTime2), iyear = year1;
						iyear != (year2 + dyear); date.addYear(dyear), iyear += dyear ) {
						if ( yts != null ) {
							value = yts.getDataValue(date);
							// Missing crop time series is OK because not all CU locations have the crop.
							if ( !yts.isDataMissing(value)){
								observedActual[iyear] = true;
								observedTotal[iyear] += value;
							}
						}
						if ( countyts != null ) {
							value2 = countyts.getDataValue(date);
							if ( !countyts.isDataMissing(value2)) {
								agstatsTotal[iyear] += value2;
							}
							agstatsObservedTotal[iyear] += value2;
						}
					}
					if ( (yts != null) && (countyts != null) ) {
						int nfilled = TSUtil.fillProrate ( yts, countyts,
							FillStart_DateTime, FillEnd_DateTime, fillProrate_PropList );
						Message.printStatus ( 2, routine,
						"FillProrateAgStats " + id + " - " + county + ", " + cropName + " (" + nfilled + " values filled).");
						// Add to the prorated totals for the crops.
						for ( date= new DateTime(FillStart_DateTime2), iyear = year1;
							iyear != (year2 + dyear); date.addYear(dyear), iyear += dyear ) {
							// Prorated totals for the crops.
							value = yts.getDataValue(date);
							if ( !yts.isDataMissing(value)){
								proratedTotal[iyear] += value;
							}
						}
					}
				}
				// Fill in the arrays for the period so that the remaining adjustments can be easily made.
				// Carry forward or backward so the nearest observed values are used.
				observedTotalPrev = -999.0;
				agstatsObservedTotalPrev = -999.0;
				for ( date= new DateTime (FillStart_DateTime2), iyear = year1;
					iyear != (year2 + dyear); date.addYear(dyear), iyear += dyear ) {
					value = observedTotal[iyear];
					// Agstats totals for the crops.
					value2 = agstatsObservedTotal[iyear];
					if ( observedActual[iyear] ) {
						// On years when crop value is known, want to use the same AgStats value.
						// Just leave the total and save the "prev" value for repeating below.
						observedTotalPrev = value;
						agstatsObservedTotalPrev = value2;
					}
					else {
						// Observed value is missing.
						if ( observedTotalPrev >= 0.0 ) {
							// Repeat the previous observation.
							observedTotal[iyear] = observedTotalPrev;
						}
						// Repeat the agstats that were used in the previous year when observations were available.
						if (agstatsObservedTotalPrev >= 0.0){
							agstatsObservedTotal[iyear] = agstatsObservedTotalPrev;
						}
					}
				}
				// Now adjust by the additional ratio, looping through the period.
				for ( date= new DateTime (FillStart_DateTime2), iyear = year1;
					iyear != (year2 + dyear); date.addYear(dyear), iyear += dyear ) {
					if ( observedActual[iyear] ) {
						// There was an actual observation so do not adjust.
						continue;
					}
					for ( icrop = 0; icrop < cropTypeList_size; icrop++ ) {
						cropName = cropTypeList.get( icrop );
						yts = cupatts.getCropPatternTS ( cropName );
						if ( yts == null ) {
							// No crop time series to adjust.
							continue;
						}
						factor1 = 1.0;
						if ( (observedTotal[iyear] > 0.0) && (proratedTotal[iyear] > 0.0)) {
							factor1 = observedTotal[iyear]/proratedTotal[iyear];
						}
						factor2 = 1.0;
						if ( (agstatsTotal[iyear] > 0.0) && (agstatsObservedTotal[iyear] > 0.0)) {
							factor2 = agstatsTotal[iyear]/agstatsObservedTotal[iyear];
						}
						value = yts.getDataValue(date);
						Message.printStatus ( 2, routine, id + " " + cropName + " " + date.getYear() +
						" value=" + StringUtil.formatString(value,"%.6f") +
						" observed_total=" + StringUtil.formatString(observedTotal[iyear],"%.6f") +
						" prorated_total=" + StringUtil.formatString(proratedTotal[iyear],"%.6f") +
						" agstats_total=" + StringUtil.formatString(agstatsTotal[iyear],"%.6f") +
						" agstats_observed_total=" + StringUtil.formatString(agstatsObservedTotal[iyear],"%.6f") +
						" factor1=" + StringUtil.formatString(factor1,"%.6f") +
						" factor2=" + StringUtil.formatString(factor2,"%.6f") );
						if ( !yts.isDataMissing(value) && ((factor1 != 1.0) || (factor2 != 1.0)) ) {
							// Only adjust if the time series value is not missing and a non-unit factor is being applied.
							yts.setDataValue ( date, value*factor1*factor2 );
							Message.printStatus ( 2, routine, "Adjusting " + id +" "+
							cropName + " " +date.getYear()+
							" " + StringUtil.formatString(value,"%.6f") + " by " +
							StringUtil.formatString (factor1,"%.6f") + "*" +
							StringUtil.formatString (factor2,"%.6f") +
							" = " + StringUtil.formatString(yts.getDataValue(date),"%.6f"));
						}
					}
				}
			}
			if ( didFill ) {
				// Refresh the contents to calculate total area.
				Message.printStatus ( 2, routine,"Recomputing crop total from individual crop type type series.");
				cupatts.refresh();
			}
		} // End loop on CropPatternTS

		// If nothing was matched, perform other actions.

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "CU location \"" + ID + "\" and crop type \"" + CropType +
				"\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "CU location \"" + ID + "\" and crop type \"" + CropType +
				"\" was not matched: failing and not filling.";
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
        message = "Unexpected error filling crop pattern time series (" + e + ").";
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
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters ) {
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String ID = parameters.getValue ( "ID" );
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeOrographicPrecAdj" );
	String CropType = parameters.getValue ( "CropType" );
	String NormalizeTotals = parameters.getValue ( "NormalizeTotals" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String FillDirection = parameters.getValue ( "FillDirection" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	String FillFlagDescription = parameters.getValue ( "FillFlagDescription" );
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
	String Constant = parameters.getValue ( "Constant" );
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
		b.append ( "IncludeGroundwaterOnlySupply=" + IncludeGroundwaterOnlySupply );
	}
	if ( CropType != null && CropType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropType=\"" + CropType + "\"" );
	}
	if ( this instanceof FillCropPatternTSProrateAgStats_Command ) {
		if ( NormalizeTotals != null && NormalizeTotals.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "NormalizeTotals=" + NormalizeTotals );
		}
	}
	if ( FillStart != null && FillStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillStart=" + FillStart );
	}
	if ( FillEnd != null && FillEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillEnd=" + FillEnd );
	}
    if ( this instanceof FillCropPatternTSUsingWellRights_Command ) {
    	if ( ParcelYear != null && ParcelYear.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "ParcelYear=" + ParcelYear );
    	}
    }
	if (this instanceof FillCropPatternTSProrateAgStats_Command ||
		this instanceof FillCropPatternTSRepeat_Command ) {
    	if ( FillDirection != null && FillDirection.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "FillDirection=" + FillDirection );
    	}
	}
	if ( (this instanceof FillCropPatternTSInterpolate_Command) ||
		(this instanceof FillCropPatternTSRepeat_Command) ) {
	  	if ( FillFlag != null && FillFlag.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "FillFlag=\"" + FillFlag + "\"" );
    	}
	}
	if ( (this instanceof FillCropPatternTSInterpolate_Command) ||
		(this instanceof FillCropPatternTSRepeat_Command) ) {
	  	if ( FillFlagDescription != null && FillFlagDescription.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "FillFlagDescription=\"" + FillFlagDescription + "\"" );
    	}
	}
	if ( (this instanceof FillCropPatternTSInterpolate_Command) ||
		(this instanceof FillCropPatternTSRepeat_Command) ) {
    	if ( MaxIntervals != null && MaxIntervals.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "MaxIntervals=" + MaxIntervals );
    	}
	}
	if ( this instanceof FillCropPatternTSConstant_Command ) {
	  	if ( Constant != null && Constant.length() > 0 ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "Constant=" + Constant );
    	}
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
