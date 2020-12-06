// ReadCropPatternTSFromParcels_Command - This class initializes, checks, and runs the ReadCropPatternTSFromHydroBase() command.

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateCU.IncludeParcelInCdsType;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateCU.StateCU_SupplyFromGW;
import DWR.StateCU.StateCU_SupplyFromSW;
import DWR.StateCU.StateCU_Util;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.TS.YearTS;
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
This class initializes, checks, and runs the ReadCropPatternTSFromParcels() command.
*/
public class ReadCropPatternTSFromParcels_Command 
extends AbstractCommand implements Command
{
	
/**
Constructor.
*/
public ReadCropPatternTSFromParcels_Command ()
{	super();
	setCommandName ( "ReadCropPatternTSFromParcels" );
}

/**
 * Add a parcel's area to the crop pattern area.
 * This is an internal method.
 * @param debug whether in debug mode, for troubleshooting
 * @param culoc the CU location being processed.
 * @param parcel the parcel being processed.
 * @param yts the crop pattern time series to add to.
 * @param temp_DateTime the DateTime at which to add the value, reused for optimization.
 * @param parcelYear the year for data (will be set in temp_DateTime).
 * @param parcelYears list of all years that have been processed, will update for the parcelYear.
 * @param parcelCrop the crop for the parcel
 * @param areaIrrig the area irrigated by a supply.
 */
private void addParcelArea ( boolean debug, StateCU_Location culoc, StateCU_Parcel parcel, YearTS yts, DateTime temp_DateTime,
	int parcelYear, int [] parcelYears, String parcelCrop, double areaIrrig ) {
	double val = yts.getDataValue ( temp_DateTime );
	int dl = 1;
	if ( yts.isDataMissing(val) ) {
		// Value is missing so set...
		if ( debug ) {
			Message.printDebug ( dl, "", "  Initializing " + culoc.getID() + " from parcelId=" + parcel.getID() + " " +
			parcelYear + " " + parcelCrop + " to " + StringUtil.formatString(areaIrrig,"%.3f") );
		}
		yts.setDataValue ( temp_DateTime, areaIrrig );
	}
	else {
		// Value is not missing.  Need to either set or add to it...
		if ( debug ) {
			Message.printDebug ( dl, "", "  Adding " + culoc.getID() + " from parcelId=" + parcel.getID() + " " +
				parcelYear + " " + parcelCrop + " + " + areaIrrig + " = " +
				StringUtil.formatString( (val + areaIrrig), "%.3f") );
		}
		yts.setDataValue ( temp_DateTime, val + areaIrrig );
	}
	addToParcelYears ( parcelYear, parcelYears );
}	

/**
Add parcel data from HydroBase to a StateCU_CropPatternTS so that it can be
used later for filling and data checks.  For example, the FillCropPatternTSUsingWellRights() command
uses the data.  This method DOES NOT manage the crop pattern time series - it simply adds the parcel
data related to the crop pattern time series to a list.
@param cds StateCU_CropPatternTS instance to in which to store data.
@param parcel_id The identifier for the parcel.
@param year The year for the parcel.
@param land_use The land use (crop name) for the parcel.
@param area The area of the parcel.
@param units The area units for the parcel.
*/
/* TODO smalers 2020-10-11 experimental - leave out for now to keep things simpler
private void addParcelToCropPatternTS ( StateCU_CropPatternTS cds, String parcel_id, int year,
	String land_use, double area, String units )
{
	StateCU_Parcel parcel = new StateCU_Parcel();
	parcel.setID ( parcel_id );
	parcel.setYear ( year );
	parcel.setArea ( area );
	parcel.setAreaUnits ( units );
	cds.addParcel ( parcel );
}
*/

/**
Add to the unique list of parcel years that were processed.
*/
private void addToParcelYears ( int year, int [] parcel_years )
{	boolean found = false;
	int insert_i = 0;
	for ( int i = 0; i < parcel_years.length; i++ ) {
		if ( parcel_years[i] < 0 ) {
			// No more data to search
			insert_i = i;
			break;
		}
		else if ( year == parcel_years[i] ) {
			found = true;
			break;
		}
	}
	if ( !found ) {
		parcel_years[insert_i] = year;
	}
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
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue( "InputEnd" );
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
	
	if ( (InputStart != null) && (InputStart.length() != 0) && !StringUtil.isInteger(InputStart) ) {
		message = "The input start is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the input start as an integer YYYY." ) );
	}
	
	if ( (InputEnd != null) && (InputEnd.length() != 0) && !StringUtil.isInteger(InputEnd) ) {
		message = "The input end is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the input end as an integer YYYY." ) );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(3);
    validList.add ( "ID" );
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ReadCropPatternTSFromParcels_JDialog ( parent, this )).ok();
}

/**
Reset crop pattern time series to zero, used in cases where multiple
readCropPatternTSFromHydroBase() commands are used.
@param cdsList list of StateCU_CropPatternTS being processed.
@param culoc_id Identifier for CU location to have its crop pattern time series reset.
@param cal_year_start The first calendar year to reset.
@param cal_year_end The last calendar year to reset.
*/
/* TODO smalers 2020-10-11 not needed for simpler logic of this new command?
private void resetCropPatternTS ( StateDMI_Processor processor, List<StateCU_CropPatternTS> cdsList,
	DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime,
	String culoc_id, int cal_year_start, int cal_year_end )
{
	// Get the crop pattern time series for the location.  If none
	// matches, return without changing anything (data will be added OK).
	StateCU_CropPatternTS cds = null;
	int pos = StateCU_Util.indexOf(cdsList,culoc_id);
	if ( pos >= 0 ) {
		// Get the time series...
		cds = (StateCU_CropPatternTS)cdsList.get(pos);
	}
	if ( cds == null ) {
		// No need to reset...
		return;
	}
	List<String> crop_names = cds.getCropNames();
	int ncrop_names = 0;
	if ( crop_names != null ) {
		ncrop_names = crop_names.size();
	}
	int year = 0;
	String units = cds.getUnits();
	if ( units == null ) {
		units = "ACRE";
	}
	for ( int ic = 0; ic < ncrop_names; ic++ ) {
		for ( year = cal_year_start; year <= cal_year_end; year++ ){
			// Replace or add in the list.  Pass individual fields because we may or
			// may not need to add a new StateCU_CropPatternTS or a time series in the object...
			processor.findAndAddCUCropPatternTSValue (
				culoc_id, culoc_id,
				year,
				-1,
				(String)crop_names.get(ic),
				0.0,
				OutputStart_DateTime,
				OutputEnd_DateTime,
				units, 0 );
		}
	}
}
*/

/**
Method to execute the ReadCropPatternTSFromParcels() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;
	int dl = 1; // Debug level
	
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
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
	String InputStart = parameters.getValue( "InputStart" );
	int InputStart_int = -1;
	if ( InputStart != null ) {
		InputStart_int = Integer.parseInt(InputStart);
	}
	String InputEnd = parameters.getValue( "InputEnd" );
	int InputEnd_int = -1;
	if ( InputEnd != null ) {
		InputEnd_int = Integer.parseInt(InputEnd);
	}

	// Get the list of CU locations.
	
	List<StateCU_Location> culocList = null;
	int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents ( "StateCU_Location_List");
		culocList = dataList;
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting CU location data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	// Get the list of crop pattern time series.
	// - this would have been initialized with CreateCropPatternTSForCULocations
	
	List<StateCU_CropPatternTS> cdsList = null;
	int cdsListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents ( "StateCU_CropPatternTS_List");
		cdsList = dataList;
		cdsListSize = cdsList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting crop pattern time series data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( cdsListSize == 0 ) {
		message = "No crop pattern time series are defined.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateCropPatternTSForCULocations() before this command." ) );
	}

	/*
	Get the map of parcel data for newer StateDMI.
	*/
	HashMap<String,StateCU_Parcel> parcelMap = null;
	try {
		@SuppressWarnings("unchecked")
		HashMap<String,StateCU_Parcel> dataMap =
			(HashMap<String,StateCU_Parcel>)processor.getPropContents ( "StateCU_Parcel_List");
		parcelMap = dataMap;
	}
	catch ( Exception e ) {
		message = "Error requesting parcel data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( parcelMap == null ) {
		message = "Parcel list (map) is null.";
		Message.printWarning ( warningLevel, 
			MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		status.addToLog ( commandPhase,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Software logic problem - results will not be correct.") );
	}
	
    // Output period will be used if not specified with InputStart and InputEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
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
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	if ( OutputStart_DateTime == null ) {
        message = "The output start has not been specified.";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output start with SetOutputPeriod() prior to this command" +
                	" or specify the InputStart parameter." ) );
	}
	if ( OutputEnd_DateTime == null ) {
        message = "The Output end has not been specified.";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output end with SetOutputPeriod() prior to this command" +
                	" or specify the InputEnd parameter." ) );
	}

	// Get the HydroBase DMI...
	// - only used to check the HydroBase version since parcels contain all necessary data for CDS
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	// Check that HydroBase version is at least 20200720 for this command.
	
	if ( !hbdmi.isDatabaseVersionAtLeast(HydroBaseDMI.VERSION_20200720) ) {
        message = "This HydroBase version (" + hbdmi.getDatabaseVersion() + ") is invalid";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Confirm that the HydroBase version is >= 20200720." ) );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		// Remove all the elements for the list that tracks when identifiers
		// are read from more than one main source (e.g., CDS, HydroBase).
		// This is used to print a warning.
		// TODO smalers 2020-11-08 not needed
		//processor.resetDataMatches ( processor.getStateCUCropPatternTSMatchList() );
		
		// Reset all StateCU_Location CDS tracking for parcels.
		// - the assignments will be made as processing occurs
		// - there may be more than one ReadCropPatternTSFromParcels command but only clear for the first one.
		boolean firstCommand = false;
		List<String> commandsToFind = new ArrayList<>();
		commandsToFind.add("ReadCropPatternTSFromParcels");
		List<Command> commandList = StateDMICommandProcessorUtil.getCommandsBeforeIndex(
			processor.indexOf(this), processor, commandsToFind, true);
		if ( commandList.size() == 0 ) {
			// No commands before this one so this is the first.
			firstCommand = true;
		}
		if ( firstCommand ) {
			// Reset the CDS indicators in all supplies
			Message.printStatus(2,routine,"First ReadCropPatternTSFromParcels command - setting all parcel supplies to CDS:NO");
			for ( Map.Entry<String, StateCU_Parcel> entry : parcelMap.entrySet() ) {
				for ( StateCU_Supply supply : entry.getValue().getSupplyList() ) {
					supply.setStateCULocationForCds(null);
					supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
					supply.setIncludeInCdsError("");
				}
			}
		}
		
		DateTime InputStart_DateTime = null;
		DateTime InputEnd_DateTime = null;
		if ( (InputStart != null) ) {
			InputStart_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
			InputStart_DateTime.setYear ( InputStart_int );
		}
		else if ( OutputStart_DateTime != null ) {
			InputStart_DateTime = new DateTime(OutputStart_DateTime);
		}
		if ( (InputEnd != null) ) {
			InputEnd_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
			InputEnd_DateTime.setYear ( InputEnd_int );
		}
		else if ( OutputEnd_DateTime != null ) {
			InputEnd_DateTime = new DateTime(OutputEnd_DateTime);
		}

		// Loop through locations...
		// TODO smalers 2020-10-14 remove unused code once tests out
		int matchCount = 0;
		String culoc_id;
		StateCU_Location culoc;
		int parcelYear;
		int replaceFlag = 1; // Add the parcel acreage value to the CropPatternTS time series.
		String partId = "";
		String units = "ACRE";
		String parcelId;
		int parcelIdInt;
		String parcelCrop;
		double parcelSupplyArea; // Parcel area associated with supply
		StateCU_CropPatternTS cds = null;
		YearTS yts;
		DateTime temp_DateTime = new DateTime(DateTime.PRECISION_YEAR);
		StateCU_SupplyFromSW supplyFromSW;
		StateCU_SupplyFromGW supplyFromGW;
		boolean parcelHasSurfaceWaterSupply = false;

		// Years with data, used to set time series with crops in those years to zero.
		// - TODO smalers 2020-10-11 not sure about the following comment
		// - TODO SAM 2007-06-14 need to rework to require users to specify the years to read.
		int [] parcel_years = new int[100];
		for ( int i = 0; i < parcel_years.length; i++ ) {
			parcel_years[i] = -1;
		}

		boolean debug = Message.isDebugOn;
		for ( int i = 0; i < culocListSize; i++ ) {
			culoc = culocList.get(i);
			culoc_id = culoc.getID();
			
			// Filter on requested locations
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;

			if ( debug ) {
				Message.printStatus ( 2, routine, "Processing " + culoc_id );
			}

			try {
				// Loop through the parcel objects and add new StateCU_CropPatternTS instances.
				// - the parcel amounts are added based on each supply relationship,
				//   which either use ditch percent_irrig or parcel divided by number of wells.
	
				// Replace or add in the list.  Pass individual fields because may or may
				// not need to add a new StateCU_CropPatternTS or a time series in the object...
				
				for ( StateCU_Parcel parcel : culoc.getParcelList() ) {
					if ( debug ) {
						Message.printStatus ( 2, routine, "  Processing " + culoc_id + " parcelId=" + parcel.getID() );
					}
					parcelYear = parcel.getYear();
					parcelCrop = parcel.getCrop();
					if ( (InputStart != null) && (parcelYear < InputStart_int) ) {
						// Only process years that were requested.
						continue;
					}
					if ( (InputEnd != null) && (parcelYear > InputEnd_int) ) {
						// Only process years that were requested.
						continue;
					}
					// StateCU_Parcel uses string identifier for parcel because derived from StateCU_Data,
					// but need integer ID below.
					parcelId = parcel.getID();
					parcelIdInt = Integer.parseInt(parcelId);
					parcelHasSurfaceWaterSupply = parcel.hasSurfaceWaterSupply();
					parcelSupplyArea = 0.0;
					
					// Find the CropPatternTS matching the CU Location
					// - this should be fast since there are not a huge number of CU Locations
					int pos = StateCU_Util.indexOf ( cdsList, culoc_id );
					if ( pos < 0 ) {
						message = "No crop pattern time series is defined for location \"" + culoc_id + "\".";
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "This should not be the case if CreateCropPatternTSForCULocations() "
										+ "was run before this command.  Report the issue to software support." ) );
						// Can't continue processing
						continue;
					}
					else {
						cds = cdsList.get(pos);
					}
					
					// Add the parcel data.  Inline this code rather than putting in a function because unlike
					// legacy code this code won't be called for user-supplied parcel data
					// (will already have been set in parcels).

					// The StateCU_CropPatternTS is in the list.  Now check to see if the
					// crop is in the list of time series...
					yts = cds.getCropPatternTS ( parcelCrop );
					if ( yts == null ) {
						// Add the crop time series.
						// - will be added alphabetically by crop name
						yts = cds.addTS ( parcelCrop, true );
					}
					// Get the value in the time series for the parcel year
					// - used to check whether a value has been previously set
					temp_DateTime.setYear ( parcelYear );
					double val;
					
					boolean useTestLogic = false;
					if ( useTestLogic ) {
					// This is the new test logic.
					// DON'T THINK IT WORKS JUST RELYING ON PARCEL TOTAL.
					// Instead, fix errors in input data and then the splits should always work because there is no double counting.
						
					// No need to loop through supplies because that has already been done when processing the parcels.
					// The only case where a parcel's area would not be included is if the current model location is
					// WEL (groundwater only) and the parcel has surface water from another location.
					// Otherwise, the parcel area should be counted in this location.
					// The other location with surface water supply will pick up the CDS acreage when it is processed.
					
					// To be sure, check the supplies for this node to determine if it is a groundwater only node.
					if ( !culoc.hasSurfaceWaterSupply() ) {
						// This node has only groundwater supply.
						// Check whether the parcel being processed has surface water supply.
						if ( parcel.hasSurfaceWaterSupply() ) {
							// The parcel has surface water supply so don't add to this CU Location.
							continue;
						}
					}

					// Set the information used in reporting and troubleshooting.
					//parcel.setStateCULocationForCds(culoc);
					//parcel.setIncludeInCdsType(IncludeParcelInCdsType.YES);
					
					// The entire area is counted.
					// For example, if surface water supply is used for the parcel:
					// - if one surface water supply, then 100% of the area
					// - if two surface water supplies, then each irrigate 50% of the area, so still 100%
					// - similar logic for wells
					// - the acreage gets split more when when processing the IPY file

					// Now check to see if there is an existing value...
					val = yts.getDataValue ( temp_DateTime );
					double parcelArea = parcel.getArea();
					if ( yts.isDataMissing(val) ) {
						// Value is missing so set...
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "", "  Initializing " + culoc_id + " from parcelId=" + parcelId + " " +
							parcelYear + " " + parcelCrop + " to " + StringUtil.formatString(parcelArea,"%.3f") );
						}
						yts.setDataValue ( temp_DateTime, parcelArea );
					}
					else {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "", "  Adding " + culoc_id + " from parcelId=" + parcelId + " " +
							parcelYear + " " + parcelCrop + " + " + parcelArea + " = " +
							StringUtil.formatString( (val + parcelArea), "%.3f") );
						}
					}
					yts.setDataValue ( temp_DateTime, val + parcelArea );

					// Add to list of parcel years that are being read.
					// - Needed for below
					addToParcelYears ( parcelYear, parcel_years );
					}

					// TODO smalers 2020-11-08 old code is actually what needs to be working
					boolean useCurrentCode = true;
					if ( useCurrentCode ) {
					// Loop though the supplies associated with the parcel.
					//  - only assign supply acreage that was matched with this location.
					//  - the math has already been done for the irrigated acreage fraction.
					if ( parcel.hasSurfaceWaterSupply() ) {
						if ( debug ) {
							Message.printStatus(2, routine, "CUloc " + culoc.getID() + " year " + parcelYear +
								" parcel ID " + parcel.getID() + " has surface water supply");
						}
						// Only assign surface water supply acreage and do not assign any groundwater acreage
						for ( StateCU_Supply supply : parcel.getSupplyList() ) {
							if ( supply instanceof StateCU_SupplyFromSW ) {
								supplyFromSW = (StateCU_SupplyFromSW)supply;
								if ( culoc.idIsIn(supplyFromSW.getWDID()) ) {
									// This culoc is associated with the supply via single ditch or collection.
									// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
									if ( debug ) {
										Message.printStatus(2, routine, "SW supply " + supplyFromSW.getWDID() + " ID is in CULoc" );
									}
									addParcelArea ( debug, culoc, parcel, yts, temp_DateTime, parcelYear, parcel_years, parcelCrop, supplyFromSW.getAreaIrrig() );
									supply.setStateCULocationForCds(culoc);
									supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
								}
								else {
									// TODO smalers 2020-11-08 convert to debug or remove when tested
									if ( debug ) {
										Message.printStatus(2, routine, "Not adding CDS acreage for " + parcelYear +
											" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
											"\" does not have part types matching SW supply WDID " + supplyFromSW.getWDID() );
									}
								}
							}
						}
					}
					else if ( parcel.hasGroundWaterSupply() ) {
						// Groundwater data only (no surface water supply):
						// - groundwater associated with commingled lands will not assigned so no double-counting of groundwater
						// - assign the portion of the parcel attributed to the location
						Message.printStatus(2, routine, "CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " + parcel.getID() + " has groundwater only");
						for ( StateCU_Supply supply : parcel.getSupplyList() ) {
							if ( supply instanceof StateCU_SupplyFromGW ) {
								// Groundwater only so get the area from the supply
								supplyFromGW = (StateCU_SupplyFromGW)supply;
								if ( debug ) {
									Message.printStatus(2, routine, "CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " + parcel.getID() +
										" supply WDID " + supplyFromGW.getWDID() + " receipt " + supplyFromGW.getReceipt() );
								}
								if ( culoc.idIsIn(supplyFromGW.getWDID(), supplyFromGW.getReceipt()) ) {
									if ( debug ) {
										Message.printStatus(2, routine, "GW supply WDID " + supplyFromGW.getWDID() +
											" receipt " + supplyFromGW.getReceipt() + " is in CULoc" );
									}
									// This culoc is associated with the supply via single ditch or collection.
									// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
									addParcelArea ( debug, culoc, parcel, yts, temp_DateTime, parcelYear, parcel_years, parcelCrop, supplyFromGW.getAreaIrrig() );
									supply.setStateCULocationForCds(culoc);
									supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
								}
								else {
									// TODO smalers 2020-11-08 convert to debug or remove when tested
									if ( debug ) {
										Message.printStatus(2, routine, "Not adding CDS acreage for " + parcelYear +
											" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
											"\" does not have parts matching GW supply WDID " + supplyFromGW.getWDID() +
											" receipt \"" + supplyFromGW.getReceipt() + "\"" );
									}
								}
							}
						}
					}
					else {
						if ( debug ) {
							Message.printStatus(2, routine, "CUloc " + culoc.getID() + " year " + parcelYear +
								" parcel ID " + parcel.getID() + " does not have surface or groundwater supply");
						}
					}
					} // End useOldCode
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error processing crop pattern time series for \"" + culoc_id + "\" (" + e + ").";
				Message.printWarning ( warningLevel, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( commandPhase,
		            new CommandLogRecord(CommandStatusType.FAILURE, message,
		            	"Check the log file - report to software support if necessary." ) );
			}
		}

		// The above code edited individual values in time series.  Loop through
		// now and make sure that the totals are up to date...

		int size = cdsList.size();
		StateCU_CropPatternTS cds2;
		StringBuffer parcel_years_string = new StringBuffer();
		for ( int iyear = 0; iyear < parcel_years.length; iyear++ ) {
			if ( parcel_years[iyear] < 0 ) {
				// Done processing years...
				break;
			}
			if ( iyear != 0 ) {
				parcel_years_string.append ( ", ");
			}
			parcel_years_string.append ( parcel_years[iyear]);
		}
		Message.printStatus( 2, routine,
			"Crop data years that were processed are:  " + parcel_years_string.toString() );
		for (int i = 0; i < size; i++) {
			cds2 = cdsList.get(i);
			Message.printStatus( 2, routine,
				"Setting missing data to zero in data years for \"" + cds2.getID() + "\"." );
			// Finally, if a crop pattern value is set in any year, assume
			// that all other missing values should be treated as zero.  If all data
			// are missing, including no crops, the total should be set to zero.  In
			// other words, crop patterns for a year must include all crops
			// and filling should not occur in a year when data values have been set.
			for ( int iyear = 0; iyear < parcel_years.length; iyear++ ) {
				if ( parcel_years[iyear] < 0 ) {
					// Done processing years...
					break;
				}
				cds2.setCropAreasToZero (
					parcel_years[iyear], // Specific year to process
					false );// Only set missing to zero (leave non-missing as is)
			}
			// Recalculate totals for the location...
			cds2.refresh ();
		}
		
		// Warn about identifiers that have been replaced in the
		// __CUCropPatternTS_List...

		processor.warnAboutDataMatches ( this, true,
			processor.getStateCUCropPatternTSMatchList(), "CU Crop Pattern TS values" );
	}
    catch ( Exception e ) {
        message = "Unexpected error reading crop pattern time series from HydroBase (" + e + ").";
        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warningLevel,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
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
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue( "InputEnd" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( InputStart != null && InputStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputStart=\"" + InputStart + "\"" );
	}
	if ( InputEnd != null && InputEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputEnd=\"" + InputEnd + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}