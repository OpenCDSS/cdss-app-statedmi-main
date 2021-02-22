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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDivision;
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
	int parcelYear, List<Integer> parcelYears, String parcelCrop, double areaIrrig ) {
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
@param parcelYear parcel year being processed
@param parcelYears list of parcel years being processed.
*/
private void addToParcelYears ( int parcelYear, List<Integer> parcelYears )
{	boolean found = false;
	for ( Integer parcelYear0 : parcelYears ) {
		if ( parcelYear0.equals(parcelYear) ) {
			found = true;
			break;
		}
	}
	if ( !found ) {
		// Add a new parcel year
		parcelYears.add ( new Integer(parcelYear) );
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
 * Get the lists of parcel year for each division.
 * This can be used for modeling, for example to know which parcel years should
 * be set to zero acres when data are missing at a location.
 * Loop through all the data and get the distinct list of years with parcel data for each division.
 * @param startYear start of period to include, -1 to include all.
 * @param endYear end of period to include, -1 to include all.
 */
private HashMap<Integer,List<Integer>> getParcelYearMapForDivisions ( List<StateCU_Location> culocList, int startYear, int endYear ) throws Exception {
	HashMap<Integer,List<Integer>> parcelMap = new HashMap<>();
	// Create year lists for each division.
	@SuppressWarnings("unchecked")
	List<Integer> [] divParcelYears = new ArrayList[HydroBase_WaterDivision.getDivisionNumbers().length];
	for ( int i = 0; i < HydroBase_WaterDivision.getDivisionNumbers().length; i++ ) {
		// Create each list, to be populated below.
		divParcelYears[i] = new ArrayList<>();
	}
	int div;
	int year;
	List<Integer> yearList;
	boolean found;
	for ( StateCU_Location culoc : culocList ) {
		for ( StateCU_Parcel parcel : culoc.getParcelList() ) {
			div = parcel.getDiv();
			year = parcel.getYear();
			// If years are specified check before adding to the list
			if ( startYear >= 0 ) {
				if ( year < startYear ) {
					continue;
				}
			}
			if ( endYear >= 0 ) {
				if ( year > endYear ) {
					continue;
				}
			}
			yearList = divParcelYears[div - 1];
			found = false;
			for ( Integer iyear : yearList ) {
				if ( iyear.equals(year) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				yearList.add(new Integer(year));
			}
		}
	}
	// Add the division lists to the map.
	for ( int i = 0; i < HydroBase_WaterDivision.getDivisionNumbers().length; i++ ) {
		// Create each list and add the the map.
		// - sort the years
		Collections.sort(divParcelYears[i]);
		parcelMap.put(new Integer(i + 1), divParcelYears[i]);
	}
	return parcelMap;
}

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
	//int dl = 1; // Debug level
	
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
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents ( "StateCU_Location_List");
		culocList = dataList;
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
			Message.printStatus(2,routine,"First ReadCropPatternTSFromParcels command - setting all parcel supplies to CDS:UNK");
			for ( Map.Entry<String, StateCU_Parcel> entry : parcelMap.entrySet() ) {
				for ( StateCU_Supply supply : entry.getValue().getSupplyList() ) {
					supply.setStateCULocationForCds(null);
					// TODO smalers 2021-01-30 use UNK because some locations may use SetCropPatternTS so
					// report needs to not say NO outright
					//supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
					supply.setIncludeInCdsType(IncludeParcelInCdsType.UNKNOWN);
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
		
		// Get the snapshot years from the parcels by examining years from all parcels.
		int startYear = -1;
		int endYear = -1;
		if ( InputStart_DateTime != null ) {
			startYear = InputStart_DateTime.getYear();
		}
		if ( InputStart_DateTime != null ) {
			endYear = InputEnd_DateTime.getYear();
		}

		// Loop through locations...
		int parcelYear;
		//String units = "ACRE";
		String parcelId;
		String parcelCrop;
		StateCU_CropPatternTS cds = null;
		YearTS yts;
		DateTime temp_DateTime = new DateTime(DateTime.PRECISION_YEAR);
		StateCU_SupplyFromSW supplyFromSW;
		StateCU_SupplyFromGW supplyFromGW;

		// Years with data for this command, used to set time series missing values in those years to zero no crop acreage.
		// - this is across all locations processed by this command because it is assumed that
		//   irrigated lands assessment for a year will be for the whole division (or divisions)
		// - however, years determined for the ID pattern may not match the full dataset,
		//   which is in 'allParcelYears' checked below
		// - moreso, during automated small tests, the period for all IDs may also not contain all parcel years,
		//   so need to get the divisions from location IDs and get parcel years for that
		List<Integer> parcelYears = new ArrayList<>();
		
		boolean debug = Message.isDebugOn;
		// Set to true for troubleshooting
		debug = true;
		for ( StateCU_Location culoc : culocList ) {
			String culoc_id = culoc.getID();
			
			// Filter on requested locations
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}

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
						// - overwrite=true because adding no matter what in this case
						boolean overwrite = true;
						yts = cds.addTS ( parcelCrop, overwrite );
					}
					// Get the value in the time series for the parcel year
					// - used to check whether a value has been previously set
					temp_DateTime.setYear ( parcelYear );

					// Loop though the supplies associated with the parcel.
					//  - only assign supply acreage that was matched with this location.
					//  - the math has already been done for the irrigated acreage fraction.
					if ( parcel.hasSurfaceWaterSupply() && !culoc.isGroundwaterOnlySupplyModelNode() ) {
						// DIV or D&W node - parcel has surface water supply and is location is NOT WEL so OK to process.
						// - otherwise WEL can have surface water supply but don't want to count in WEL
						if ( debug ) {
							Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear +
								" parcel ID " + parcel.getID() + " has SW supply, is a DIV or D&W.");
						}
						// Only assign surface water supply acreage and do not assign any groundwater acreage
						for ( StateCU_Supply supply : parcel.getSupplyList() ) {
							if ( !supply.getIsModeled() ) {
								// Supply is not modeled (is not in dataset) so skip
								// - typically only impacts GW only node fractional areas but put here in case added for surface water also
								Message.printStatus( 2, routine, "    For location " + culoc.getID() + " year " + parcelYear +
									" parcelId=" + parcelId + " skipping supply ID=" + supply.getID() +
									" since supply is not in dataset.");
								supply.setIncludeInCdsType(IncludeParcelInCdsType.NOT_MODELED);
							}
							else if ( supply instanceof StateCU_SupplyFromSW ) {
								supplyFromSW = (StateCU_SupplyFromSW)supply;
								// TODO smalers 2021-01-27 the following logic seems wrong since it omits D&W parts
								// - the WDIDS for the collection may result in parcels that have supply ditches with WDIDs that don't match the collection WDID list
								// - maybe should include in single DIV for matching supply, and include in D&W regardless, and only include parcel once regardless
								boolean doInclude = false;
								String notIncludeMessage = "";
								if ( culoc.isCollection() ) {
									// Collection should ensure that parcels don't show up in more than one collection.
									if ( culoc.idIsIn(supplyFromSW.getWDID()) ) {
										doInclude = true;
									}
									else {
										doInclude = false;
										notIncludeMessage = "because is a collection ID and SW supply ID " + supplyFromSW.getID() + " does not match collection part IDs";
									}
								}
								else {
									// Single DIV so only include if the surface supply ID matches the location ID
									if ( culoc.idIsIn(supplyFromSW.getWDID()) ) {
										doInclude = true;
									}
									else {
										doInclude = false;
										notIncludeMessage = "because is a single ID and SW supply ID " + supplyFromSW.getID() + " does not match location ID";
									}
								}
								if ( doInclude ) {
									// This culoc is associated with the supply via single ditch or collection.
									// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
									if ( debug ) {
										Message.printStatus(2, routine, "    SW supply " + supplyFromSW.getWDID() + " ID is in CULoc" );
									}
									addParcelArea ( debug, culoc, parcel, yts, temp_DateTime, parcelYear, parcelYears, parcelCrop, supplyFromSW.getAreaIrrig() );
									if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.YES ) {
										// Should not happen, should only be set to YES once
										message = "CUloc " + culoc.getID() + " year " + parcelYear +
											" parcel ID " + parcel.getID() + " supply ID " + culoc.getID() +
											" area was previously added to CDS (location " + supply.getStateCULocationForCds().getID() + ").";
										Message.printWarning ( warningLevel, 
			        						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, "    " + message );
		        						status.addToLog ( commandPhase,
		            						new CommandLogRecord(CommandStatusType.WARNING, message,
		            							"Area should only be added once - ditch is included in more than one DIV or D&W model locations." ) );
									}
									supply.setStateCULocationForCds(culoc);
									supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
								}
								else {
									// TODO smalers 2020-11-08 convert to debug or remove when tested
									// TODO smalers 2021-01-30 old code that is not relevant?
									if ( debug ) {
										Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
											" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
											"\" (" + notIncludeMessage + ")" );
									}
									// TODO smalers 2021-01-30 new code 
									// - supply is not included
									// - only set to know if unknown because don't want to reset
									if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
										supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
									}
								}
							}
							else if ( supply instanceof StateCU_SupplyFromGW ) {
								// Parcel also has groundwater supplies that are not counted since surface water is counted
								if ( debug ) {
									Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
										" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
										"\" GW supply " + supply.getID() + " (because parcel has SW supply)");
								}
								// Only set to know if unknown because don't want to reset
								if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
									supply.setStateCULocationForCds(null);
									supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
								}
							}
						}
					}
					else if ( parcel.hasSurfaceWaterSupply() && culoc.isGroundwaterOnlySupplyModelNode() ) {
						// WEL location parcel that has surface water supply.
						// The acreage will have been added to the D&W node above, for the appropriate location.
						// No need to do anything.  The parcel will be skipped for the location.
						Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " +
							parcel.getID() + " is GW only for model, is WEL.");
						Message.printStatus(2, routine, "  Skipping parcel for CDS (because has SW supply and will have been added to a D&W).");
						// Set all supply relationships to not include in CDS
						for ( StateCU_Supply supply : parcel.getSupplyList() ) {
							if ( debug ) {
								if ( supply instanceof StateCU_SupplyFromGW ) {
									Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
										" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
										"\" GW supply " + supply.getID() + " (because parcel has SW supply)");
								}
								else if ( supply instanceof StateCU_SupplyFromSW ) {
									Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
										" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
										"\" SW supply " + supply.getID() + " (because parcel has SW supply)");
								}
							}
							// Only set to know if unknown because don't want to reset
							if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
								supply.setStateCULocationForCds(null);
								supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
							}
						}
					}
					else if ( !parcel.hasSurfaceWaterSupply() && culoc.isGroundwaterOnlySupplyModelNode() ) {
						// WEL location parcel with only surface water supply
						// - groundwater associated with commingled lands will not assigned so no double-counting of parcel
						// - assign the portion of the parcel attributed to the location
						Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " +
							parcel.getID() + " is GW only for model, is WEL.");
						for ( StateCU_Supply supply : parcel.getSupplyList() ) {
							if ( !supply.getIsModeled() ) {
								// Supply is not modeled (is not in dataset) so skip
								// - typically only impacts GW only node fractional areas
								Message.printStatus( 2, routine, "    For location " + culoc.getID() + " year " + parcelYear +
									" parcelId=" + parcelId + " skipping supply ID=" + supply.getID() +
									" since supply is not in dataset.");
								supply.setIncludeInCdsType(IncludeParcelInCdsType.NOT_MODELED);
							}
							else if ( supply instanceof StateCU_SupplyFromGW ) {
								// Groundwater only so get the area from the supply
								supplyFromGW = (StateCU_SupplyFromGW)supply;
								if ( debug ) {
									Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " + parcel.getID() +
										" supply WDID " + supplyFromGW.getWDID() + " receipt " + supplyFromGW.getReceipt() );
								}
								// OK to check this here because collection is done by specific parts
								if ( culoc.idIsIn(supplyFromGW.getWDID(), supplyFromGW.getReceipt()) ) {
									if ( debug ) {
										Message.printStatus(2, routine, "    GW supply WDID " + supplyFromGW.getWDID() +
											" RECEIPT '" + supplyFromGW.getReceipt() + "' is in CULoc" );
									}
									// This culoc is associated with the supply via single ditch or collection.
									// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
									// - parcel_years is output and is the list of years with data, across all locations in the command
									addParcelArea ( debug, culoc, parcel, yts, temp_DateTime, parcelYear, parcelYears, parcelCrop, supplyFromGW.getAreaIrrig() );
									if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.YES ) {
										// Should not happen, should only be set to YES once
										message = "CUloc " + culoc.getID() + " year " + parcelYear +
											" parcel ID " + parcel.getID() + " supply ID " + culoc.getID() +
											" area was previously added to CDS (location " + supply.getStateCULocationForCds().getID() + ").";
										Message.printWarning ( warningLevel, 
			        						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, "    " + message );
		        						status.addToLog ( commandPhase,
		            						new CommandLogRecord(CommandStatusType.WARNING, message,
		            							"Area should only be added once - well is included in more than one WEL model locations." ) );
									}
									supply.setStateCULocationForCds(culoc);
									supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
								}
								else {
									// TODO smalers 2020-11-08 convert to debug or remove when tested
									if ( debug ) {
										Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
											" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
											"\" (because does not have parts matching GW supply WDID " + supplyFromGW.getWDID() +
											" RECEIPT '" + supplyFromGW.getReceipt() + "')" );
									}
									// Only set to know if unknown because don't want to reset
									if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
										supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
									}
								}
							}
						}
					}
					else {
						// This should not happen due to location/parcel/supply relationship in the first place.
						// - there is a lot of code below to figure out why this is a data issue
						message = "CUloc " + culoc.getID() + " year " + parcelYear +
							" parcel ID " + parcel.getID() + " does not appear to be DIV, D&W, or WEL based on available data.";
						Message.printWarning ( warningLevel, 
			        		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        		status.addToLog ( commandPhase,
		            		new CommandLogRecord(CommandStatusType.WARNING, message,
		            			"This may be a HydroBase data load issue.  For example, GIS data error may cause a HydroBase data load error."
		            			+ "  Use SetParcel* commands to fix input data.  See log file for more information." ) );
		        		// Print more information to troubleshoot:
						Message.printWarning(3, routine, "  CU Location has GW only supply based on aggregation/system (WEL): " + culoc.isGroundwaterOnlySupplyModelNode() );
						Message.printWarning(3, routine, "  CU Location has SW supply (DIV or D&W because is not WEL): " + culoc.hasSurfaceWaterSupplyForModelNode() );
						Message.printWarning(3, routine, "  Parcel has SW supply = " + parcel.hasSurfaceWaterSupply() );
						Message.printWarning(3, routine, "  Parcel has " + parcel.getSupplyFromSWCount() + " SW supplies based on parcel data." );
						if ( parcel.getSupplyFromSWCount() > 0 ) {
							for ( StateCU_Supply supply : parcel.getSupplyList() ) {
								if ( supply instanceof StateCU_SupplyFromSW ) {
									supplyFromSW = (StateCU_SupplyFromSW)supply;
									// Any SW supply should have been added to DIV or D&W
									Message.printWarning(3, routine, "    SW supply WDID = " + supplyFromSW.getWDID() );
									if ( !supplyFromSW.getWDID().isEmpty() ) {
										Message.printWarning(3, routine, "      SW supply WDID is in CU Location " +
											culoc.getCollectionType() + "? = " +  culoc.idIsIn(supplyFromSW.getWDID()) );
									}
								}
								supply.setIncludeInCdsType(IncludeParcelInCdsType.ERROR);
							}
						}
						Message.printWarning(3, routine, "  Parcel has GW supply = " + parcel.hasGroundWaterSupply() );
						Message.printWarning(3, routine, "  Parcel has " + parcel.getSupplyFromGWCount() + " GW supplies based on parcel data." );
						if ( parcel.getSupplyFromGWCount() > 0 ) {
							for ( StateCU_Supply supply : parcel.getSupplyList() ) {
								if ( supply instanceof StateCU_SupplyFromGW ) {
									supplyFromGW = (StateCU_SupplyFromGW)supply;
									Message.printWarning(3, routine, "    GW supply WDID = " + supplyFromGW.getWDID() + " RECEIPT = '" + supplyFromGW.getReceipt() + "'" );
									// Check to see if the well supply is in the original aggregate list for this WEL node
									if ( !supplyFromGW.getWDID().isEmpty() ) {
										if ( culoc.idIsIn(supplyFromGW.getWDID()) ) {
											Message.printWarning(3, routine, "      WDID is in CU Location " +
												culoc.getCollectionType() + "? = " +  culoc.idIsIn(supplyFromGW.getWDID()) );
										}
										if ( culoc.idIsIn(supplyFromGW.getReceipt()) ) {
											Message.printWarning(3, routine, "      RECEIPT is in CU Location " +
												culoc.getCollectionType() + "? = " +  culoc.idIsIn(supplyFromGW.getReceipt()) );
										}
									}
								}
								supply.setIncludeInCdsType(IncludeParcelInCdsType.ERROR);
							}
						}
						if ( (parcel.getSupplyFromSWCount() + parcel.getSupplyFromGWCount()) == 0) {
							message = "  Parcel has no supply data - could be a HydroBase data load issue."
								+ "  Model configuration is not consistent with HydroBase data.";
							Message.printWarning(3, routine, message );
							Message.printWarning ( warningLevel, 
			        			MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        			status.addToLog ( commandPhase,
		            			new CommandLogRecord(CommandStatusType.WARNING, message,
		            				"Check original parcel data." ) );
						}
					}
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

		// The above code accumulated individual parcel/crop fractional areas into the crop pattern time series.
		// Loop through now and make sure that missing values in each data year are set to zero and
		// the totals for each data year are up to date.
		// Loop using the same logic as above to check CU locations
		// to make sure everything is based on CU locations.

		HashMap<Integer,List<Integer>> divParcelYears = getParcelYearMapForDivisions(culocList, startYear, endYear);
		// TODO smalers 2021-01-25 do not use the following for filling because HydroBase contains bad data for parcels in 2003,
		// which are not found in the in-memory parcel map.  Print out below as FYI.
		// - instead, get valid parcel years from the parcel data (ReadParcelsFromHydroBase has way to exclude bad years)
		int [] excludeYears = new int[0];
		HashMap<Integer,List<Integer>> divHydroBaseParcelYears =
			StateDMI_Util.readParcelYearMapForDivisions(hbdmi, startYear, endYear, excludeYears);
		StringBuffer [] divParcelYearsString = new StringBuffer[HydroBase_WaterDivision.getDivisionNumbers().length];
		StringBuffer [] divHydroBaseParcelYearsString = new StringBuffer[HydroBase_WaterDivision.getDivisionNumbers().length];
		// Create strings for each division to use in output below.
		for ( int i = 0; i < divParcelYearsString.length; i++ ) {
			int div = i + 1;
			// Format divisions from the in-memory parcel data.
			List<Integer> divYears = divParcelYears.get(new Integer(div));
			divParcelYearsString[i] = new StringBuffer();
			for ( Integer iyear : divYears ) {
				if ( divParcelYearsString[i].length() > 0 ) {
					divParcelYearsString[i].append ( ", ");
				}
				divParcelYearsString[i].append ( iyear.toString() );
			}
			divParcelYearsString[i].insert ( 0, "all years from location/parcel data " + div + " in output period: " );
			// Format divisions from the HydroBase data.
			divYears = divHydroBaseParcelYears.get(new Integer(div));
			divHydroBaseParcelYearsString[i] = new StringBuffer();
			for ( Integer iyear : divYears ) {
				if ( divHydroBaseParcelYearsString[i].length() > 0 ) {
					divHydroBaseParcelYearsString[i].append ( ", ");
				}
				divHydroBaseParcelYearsString[i].append ( iyear.toString() );
			}
			divHydroBaseParcelYearsString[i].insert ( 0, "all years from HydroBase division " + div + " in output period: " );
		}

		// All parcel years, but not split by division
		List<Integer> allParcelYears = StateCU_Location.getParcelYears ( culocList, startYear, endYear );
		Collections.sort(allParcelYears);
		StringBuffer allParcelYearsString = new StringBuffer();
		for ( Integer iyear : allParcelYears ) {
			if ( allParcelYearsString.length() > 0 ) {
				allParcelYearsString.append ( ", ");
			}
			allParcelYearsString.append ( iyear.toString() );
		}
		allParcelYearsString.insert ( 0, "all years from all locations: " );

		// Years for locations processed by this command.
		Collections.sort(parcelYears);
		StringBuffer parcelYearsString = new StringBuffer();
		for ( Integer iyear : parcelYears ) {
			if ( parcelYearsString.length() > 0 ) {
				parcelYearsString.append ( ", ");
			}
			parcelYearsString.append ( iyear.toString() );
		}
		parcelYearsString.insert ( 0, "all years from command ID parameter: " );

		Message.printStatus( 2, routine,
			"Parcel crop data years from ParcelUseTS for divisions in HydroBase for bounding period " + startYear + " to " + endYear + " are listed below.");
		for ( int i = 0; i < 7; i++ ) {
			Message.printStatus( 2, routine,
				"  Parcel crop data years for Division " + (i + 1) + " are: " + divHydroBaseParcelYearsString[i] );
		}
		Message.printStatus( 2, routine,
			"Parcel crop data years for divisions in location/parcel data for bounding period " + startYear + " to " + endYear + " are listed below.");
		for ( int i = 0; i < 7; i++ ) {
			Message.printStatus( 2, routine,
				"  Parcel crop data years for Division " + (i + 1) + " are: " + divParcelYearsString[i] );
		}
		Message.printStatus( 2, routine,
			"Parcel crop data years for all locations in dataset are: " + allParcelYearsString );
		Message.printStatus( 2, routine,
			"Parcel crop data years that were processed for location pattern " + ID + " are: " + parcelYearsString );

		List<Integer> parcelYearsToZero = null;
		String parcelYearsToZeroString = "";

		// All years with data in dataset are used.
		// This is the only behavior right now.
		parcelYearsToZeroString = "";
		parcelYearsToZero = null;

		List<HydroBase_WaterDistrict> waterDistrictList = hbdmi.getWaterDistricts();
		for ( StateCU_Location culoc : culocList ) {
			// Only process the locations of interest for the command.
			String culoc_id = culoc.getID();
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			int pos = StateCU_Util.indexOf ( cdsList, culoc_id );
			if ( pos >= 0 ) {
				// Have crop pattern time series to process
				cds = cdsList.get(pos);
				// Get the water district.
				int wd = StateDMI_Util.getWaterDistrictFromID(culoc_id);
				if ( wd < 0 ) {
					// Should not happen if nodes follow standard naming but could be a non-CDSS dataset.
					// Handle the case internally by using the location year list.
					parcelYearsToZero = allParcelYears;
					parcelYearsToZeroString = "can't determine WD, using " + allParcelYearsString;
				}
				else {
					// Get the division.
					int div = HydroBase_WaterDistrict.lookupWaterDivisionIdForDistrict(waterDistrictList, wd);
					// Get the parcel years for the division.
					parcelYearsToZero = divParcelYears.get(new Integer(div));
					parcelYearsToZeroString = divParcelYearsString[div - 1].toString();
				}
				Message.printStatus( 2, routine,
					"  Setting missing data to zero in years (" + parcelYearsToZeroString + ") for \"" + cds.getID() + "\"." );
				// If a crop pattern value is set in any year,
				// assume that all other missing values for crops in the year should be treated as zero.
				// If all data are missing, including no crops, the total should be set to zero.
				// In other words, crop patterns for a year must include all crops
				// and filling should not occur in a year when data values have been set.
				boolean setAllValuesToZero = false;
				for ( Integer iyear : parcelYearsToZero ) {
					cds.setCropAreasToZero (
						iyear, // Specific year to process
						setAllValuesToZero );
				}
				// Recalculate totals for the location for all years that have data.
				cds.refresh ();
			}
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