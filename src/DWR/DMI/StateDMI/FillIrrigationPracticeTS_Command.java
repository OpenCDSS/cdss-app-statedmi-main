// FillIrrigationPracticeTS_Command - This class initializes, checks, and runs the FillCropPatternTS*() commands.

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

/**
<p>
This class initializes, checks, and runs the FillCropPatternTS*() commands.  It is extended by the
specific fill commands.
</p>
*/
public abstract class FillIrrigationPracticeTS_Command
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
public FillIrrigationPracticeTS_Command ()
{	super();
	setCommandName ( "FillCropPatternTS?" );
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
{	String routine = "FillAndSetCULocationClimateStationWeights_Command.checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillDirection = parameters.getValue ( "FillDirection" );
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
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
	
	if ( this instanceof FillIrrigationPracticeTSRepeat_Command ) {
		if ( (FillDirection != null) && (FillDirection.length() > 0) &&
			!FillDirection.equalsIgnoreCase(_Backward) && !FillDirection.equalsIgnoreCase(_Forward) ) {
			message = "The FillDirection value (" + FillDirection + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify FillDirection as " + _Backward + " or " + _Forward +
					" (default=" + _Forward + ").") );
		}
	}
		
	if ( (MaxIntervals != null) && (MaxIntervals.length() > 0) && !StringUtil.isInteger(MaxIntervals) ) {
		message = "The MaxIntervals value (" + MaxIntervals + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify MaxIntervals as blank (fill all gaps) or an integer for gap years to fill.") );
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
	List<String> valid_Vector = new Vector<String>(8);
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "FillStart" );
	valid_Vector.add ( "FillEnd" );
	valid_Vector.add ( "DataType" );
	if ( this instanceof FillIrrigationPracticeTSRepeat_Command ) {
		valid_Vector.add ( "FillDirection" );
		valid_Vector.add ( "FillFlag" );
	}
	valid_Vector.add ( "MaxIntervals" );
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
{	// The command will be modified if changed (true indicates fill)
	return (new FillIrrigationPracticeTS_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Run the command command.
@param command_number Number of command in sequence.
@param fill if True, then the fill command is being run.  If false, the set command is being run.
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
	String DataType = parameters.getValue ( "DataType" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillDirection = parameters.getValue ( "FillDirection" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	int FillDirection_int = 1; // Default (forward)
	if ( (FillDirection != null) && FillDirection.equalsIgnoreCase(_Backward) ) {
		FillDirection_int = -1;
	}
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
	int MaxIntervals_int = 0; // Default
	if ( StringUtil.isInteger(MaxIntervals)) {
		MaxIntervals_int = Integer.parseInt(MaxIntervals);
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	// Get the list of CU locations, needed to check whether surface or ground water...
	
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
		message = "No CU locations available to fill irrigation practice time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Read CU locations before using this command." ) );
	}
		
	// Get the list of crop pattern time series...
	
	List<StateCU_IrrigationPracticeTS> ipyList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_IrrigationPracticeTS> dataList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents( "StateCU_IrrigationPracticeTS_List");
		ipyList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_IrrigationPracticeTS_List from processor.";
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
	
	// Increase processing speed
	
	boolean fillInterpolate = false;
	boolean fillRepeat = false;
	if ( this instanceof FillIrrigationPracticeTSInterpolate_Command ) {
		fillInterpolate = true;
	}
	if ( this instanceof FillIrrigationPracticeTSRepeat_Command ) {
		fillRepeat = true;
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		DateTime FillStart_DateTime = OutputStart_DateTime;
		if ( FillStart != null ) {
			FillStart_DateTime = new DateTime(DateTime.PRECISION_YEAR);
			FillStart_DateTime.setYear(StringUtil.atoi(FillStart));
		}
		DateTime FillEnd_DateTime = OutputEnd_DateTime;
		if ( FillEnd != null ) {
			FillEnd_DateTime = new DateTime(DateTime.PRECISION_YEAR);
			FillEnd_DateTime.setYear(StringUtil.atoi(FillEnd));
		}
		
		StateCU_IrrigationPracticeTS ipyts = null;
		StateCU_Location culoc = null; // Needed to get is_gwonly_supply info.
		String id;
		int pos; // Position of item within list for lookups
		List<String> data_types = StateCU_IrrigationPracticeTS.getTimeSeriesDataTypes ( false, false );
		String data_type = null;
		int ndata_types = data_types.size();
		int idata_type = 0;
		YearTS yts = null;
		boolean has_gwonly_supply = false; // Does location have GW only supply?
		// Allocate array to indicate when data are missing in original data.
		boolean [] missing_years = new boolean[FillEnd_DateTime.getYear() - FillStart_DateTime.getYear() + 1];
		// Loop through available objects and see if any need to be filled...
		int matchCount = 0;
		for (int i = 0; i < culocListSize; i++) {
			has_gwonly_supply = false;
			ipyts =(StateCU_IrrigationPracticeTS)ipyList.get(i);
			id = ipyts.getID();
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine, "Checking CULocation " + id + " against \"" + idpattern_Java + "\"" );
			}
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			pos = StateCU_Util.indexOf(	culocList,id);
			if ( pos < 0 ) {
				message = "Location \"" + id +
					"\" not found in CU locations.  Cannot determine whether groundwater " +
					"only supply.  Assuming both ground and surface water supply";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Read CU locations prior to this command." ) );
				has_gwonly_supply = false;
			}
			else {
				culoc = (StateCU_Location)culocList.get(pos);
				if ( culoc.hasGroundwaterOnlySupply() ) {
					has_gwonly_supply = true;
				}
			}
			yts = null;
			boolean data_type_matched = false;
			for ( idata_type = 0; idata_type < ndata_types; idata_type++ ) {
				data_type = (String)data_types.get(idata_type);
				// Can match a specific time series type or CropArea-AllAcreageParts
				// to match any of the GW/SW Flood/Sprinkler components.  Be loose
				// with the strings because the types may change to high/low efficiency.
				if ( (DataType == null) ||	// Fill All
					DataType.equalsIgnoreCase(data_type) ) {
					// Data type matches so process the time series...
					yts = ipyts.getTimeSeries ( data_type );
					data_type_matched = true;
					// Get a list of years with missing...
					int im = 0;
					for ( DateTime date = new DateTime(FillStart_DateTime); date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1) ) {
						if ( yts.isDataMissing(yts.getDataValue(date))) {
							missing_years[im] = true;
						}
						else {
							missing_years[im] = false;
						}
						++im;
					}
					// Reset the data...
					if ( fillRepeat ) {
						Message.printStatus ( 2, routine,
						"Filling location \"" + id + "\" irrigation practice time series data type \"" +
						data_type + "\" using FillRepeat." );
						TSUtil.fillRepeat ( yts, FillStart_DateTime, FillEnd_DateTime, FillDirection_int,
							MaxIntervals_int, FillFlag );
					}
					else if ( fillInterpolate ) {
						Message.printStatus ( 2, routine,
							"Filling location \"" + id + "\" irrigation practice time series data type \"" +
							data_type + "\" using FillInterpolate." );
						TSUtil.fillInterpolate ( yts, FillStart_DateTime, FillEnd_DateTime,
							MaxIntervals_int, 0 );
					}
					// Check to see if acreages need to be adjusted.  Only need to do this in
					// years that were actually modified (otherwise runs slower and generates a bunch
					// of output).  The following will handle:
					//   CropArea-GroundWater
					//   CropArea-GroundWaterFlood
					//   CropArea-GroundWaterSprinkler
					if ( (DataType == null) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_GroundWater) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_GroundWaterFlood) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_GroundWaterSprinkler) ) {
						im = 0;
						for ( DateTime date = new DateTime(FillStart_DateTime); date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1) ) {
							if ( missing_years[im] && !yts.isDataMissing(yts.getDataValue(date) )) {
								// Adjust the groundwater acreage to agree with the total acres.
								// Only need to post-process if the value was originally missing but
								// was filled above.
								ipyts.adjustGroundwaterAcresToTotalAcres ( date, has_gwonly_supply );
							}
							++im;
						}
					}
					if ( (DataType == null) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_SurfaceWaterOnly) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_SurfaceWaterOnlyFlood) ||
						DataType.equalsIgnoreCase(StateCU_IrrigationPracticeTS.TSTYPE_CropArea_SurfaceWaterOnlySprinkler) ) {
						im = 0;
						for ( DateTime date = new DateTime(FillStart_DateTime); date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1) ) {
							if ( missing_years[im] && !yts.isDataMissing(yts.getDataValue(date) )) {
								// Adjust the surface water acreage to agree with the groundwater and total acres.
								// Only need to post-process if the value was originally missing but
								// was filled above.
								ipyts.adjustSurfaceWaterAcresToGroundwaterAndTotalAcres ( date, has_gwonly_supply );
							}
							++im;
						}
					}
					if ( !data_type_matched ) {
						message = "The data type \"" +
							data_type + "\" did not match recognized data types to fill - no filling occurred.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify that the identifier and data type are correct." ) );
					}
					else {
						++matchCount;
					}
				}
			}

			/* TODO SAM 2007-09-11 Remove if above checks out
			if ( AdjustAcreagePartsToTotal_boolean &&
					((data_type.length() == 0) ||
					(StringUtil.indexOfIgnoreCase(data_type,"CropArea",0)>=0)) ) {
				// Adjust the surface water acreage so that:
				// TotalAcres - GWacres = SWflood + SWsprinkler
				// 
				// Do the adjustments using integers since that is the precision
				// of output.
				double Tacre, Acgwfl, Acgwspr, Acswfl, Acswspr;
				int Tacre_int;
				int Acgwfl_int;
				int Acgwspr_int;
				int Acswfl_int;
				int Acswspr_int;
				double Acgwfl_new;	// New computed values.
				double Acgwspr_new;
				double Acswfl_new;
				double Acswspr_new;
				double sw_target, sw_actual;
				int sw_target_int;	// Target surface water acres
				int sw_actual_int;	// Actual surface water acres
				int year1 = datetime1.getYear();
				int year2 = datetime2.getYear();
				int tolerance = 0;	// Tolerance to compare acres, when integers
				for ( int year = year1; year <= year2; year++ ) {
					Tacre = ipyts.getTacre ( year );
					Tacre_int = (int)Tacre;
					Acgwfl = ipyts.getAcgwfl ( year );
					Acgwfl_int = (int)Acgwfl;
					Acgwspr = ipyts.getAcgwspr ( year );
					Acgwspr_int = (int)Acgwspr;
					Acswfl = ipyts.getAcswfl ( year );
					Acswfl_int = (int)Acswfl;
					Acswspr = ipyts.getAcswspr ( year );
					Acswspr_int = (int)Acswspr;
					sw_target = Tacre - Acgwfl - Acgwspr;
					sw_target_int = (int)sw_target;
					sw_actual = Acswfl + Acswspr;
					sw_actual_int = (int)sw_actual;
					if ( (Tacre_int < 0) || (Acgwfl_int < 0) ||
							(Acgwspr_int < 0) || (Acswfl_int < 0) ||
							(Acswspr_int < 0) ) {
						// Missing data so skip. Should not happen if other commands have
						// filled in other data.
						continue;
					}
					if ( Math.abs(sw_target_int - sw_actual_int) > tolerance ) {
						Message.printStatus ( 2,routine,
							"Location \"" + id + "\" " + year +
							" (Totalacres - GWacres) (" + sw_target_int + ") - SWacres (" + sw_actual_int + ") > " + tolerance + " (" +
							(sw_target_int - sw_actual_int) + ").  Attempting to adjust SWacres." );
						if ( sw_target_int < 0 ) {
							// The groundwater acres are more than the total.  Adjust the groundwater acres
							// down to the total.  This will mean that surface water target will be zero
							// below.
							Acgwfl_new = Tacre*Acgwfl/(Acgwfl + Acgwspr);
							ipyts.setAcgwfl(year,Acgwfl_new);
							Acgwspr_new = Tacre*Acgwspr/(Acgwfl + Acgwspr);
							ipyts.setAcgwspr(year,Acgwspr_new);
							Message.printStatus ( 2,routine,
									"Location \"" + id + "\" " + year + ":  Adjusted GW acres (" +
									(Acgwfl_int + Acgwspr_int) + ") to Total (" + Tacre_int + "). New Acgwfl="+
									StringUtil.formatString(Acgwfl_new,"%.3f") + " Acgwspr=" +
									StringUtil.formatString(Acgwspr_new,"%.3f") );
							// Reset the data as if it was the previous, for consideration below...
							Acgwfl = Acgwfl_new;
							Acgwfl_int = (int)Acgwfl;
							Acgwspr = Acgwspr_new;
							Acgwspr_int = (int)Acgwspr;
							sw_target = Tacre - Acgwfl - Acgwspr;
							sw_target_int = (int)sw_target;
							sw_actual = Acswfl + Acswspr;
							sw_actual_int = (int)sw_actual;
							if ( sw_target_int == sw_actual_int ) {
								// Done with adjustments because the groundwater = total and surface = 0
								continue;
							}
						}
						if ( sw_actual_int == 0 ) {
							Message.printStatus ( 2,routine,
							"Location \"" + id + "\" " + year + ":  Can't adjust SW acres to Total-GW because SW = zero.  Attempt to adjust GW..." );
							// Adjust the groundwater to be the total.
							// Total = GWflood + GWsprink
							if ( (Acgwfl_int + Acgwspr_int) == 0) {
								Message.printWarning ( 2,
										MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,
										"Location \"" + id + "\" " + year + ":  Can't adjust GW acres to Total (" + Tacre_int +
										") because GW = zero." );
								// TODO SAM 2007-06-19 Need to address issue
								// Need some default or user preference whether to assign flood or
								// sprinkler acreage (or a mixture) to the total
							}
							else {
								Acgwfl_new = Tacre*Acgwfl/(Acgwfl + Acgwspr);
								ipyts.setAcgwfl(year,Acgwfl_new);
								Acgwspr_new = Tacre*Acgwspr/(Acgwfl + Acgwspr);
								ipyts.setAcgwspr(year,Acgwspr_new);
								Message.printStatus ( 2,routine,
										"Location \"" + id + "\" " + year + ":  Adjusted GW acres (" +
										(Acgwfl_int + Acgwspr_int) + ") to Total (" + Tacre_int + "). New Acgwfl="+
										StringUtil.formatString(Acgwfl_new,"%.3f") + " Acgwspr=" +
										StringUtil.formatString(Acgwspr_new,"%.3f") );
							}
						}
						else {	// Just prorate the CDS using the fractions of the previou GW values.
							Acswfl_new = sw_target*Acswfl/sw_actual;
							ipyts.setAcswfl(year,Acswfl_new);
							Acswspr_new = sw_target*Acswspr/sw_actual;
							ipyts.setAcswspr(year,Acswspr_new);
							Message.printStatus ( 2,routine,
									"Location \"" + id + "\" " + year + ":  Adjusted SW acres (" +
									sw_actual_int + ") to Total-GW (" + sw_target_int + ").  New Acswfl="+
										StringUtil.formatString(Acswfl_new,"%.3f") + " Acswspr=" +
										StringUtil.formatString(Acswspr_new,"%.3f") );
						}
					}

				}
			}
			*/
		}
		
		// If nothing was matched, perform other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "CU location \"" + ID + "\" and data type \"" + DataType +
				"\" was not matched: warning and not filling.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "CU location \"" + ID + "\" and data type \"" + DataType +
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
        message = "Unexpected error filling irrigation practice time series (" + e + ").";
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
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	
	String ID = parameters.getValue ( "ID" );
	String DataType = parameters.getValue ( "DataType" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillDirection = parameters.getValue ( "FillDirection" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	String MaxIntervals = parameters.getValue ( "MaxIntervals" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (DataType != null) && (DataType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DataType=\"" + DataType + "\"" );
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
	if( this instanceof FillIrrigationPracticeTSRepeat_Command ) {
		if ( (FillDirection != null) && (FillDirection.length() >0)){
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "FillDirection=\"" + FillDirection + "\"" );
		}
		if ( this instanceof FillIrrigationPracticeTSRepeat_Command ) {
		  	if ( FillFlag != null && FillFlag.length() > 0 ) {
	    		if ( b.length() > 0 ) {
	    			b.append ( "," );
	    		}
	    		b.append ( "FillFlag=\"" + FillFlag + "\"" );
	    	}
		}
	}
	if ( (MaxIntervals != null) && (MaxIntervals.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MaxIntervals=\"" + MaxIntervals + "\"" );
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
