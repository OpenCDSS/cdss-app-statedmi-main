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
import java.util.List;

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
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
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
		processor.resetDataMatches ( processor.getStateCUCropPatternTSMatchList() );
		
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
		StateCU_SupplyFromSW swSupply;
		StateCU_SupplyFromGW gwSupply;
		boolean parcelHasSurfaceWaterSupply = false;

		// Years with data, used to set time series with crops in those years to zero.
		// - TODO smalers 2020-10-11 not sure about the following comment
		// - TODO SAM 2007-06-14 need to rework to require users to specify the years to read.
		int [] parcel_years = new int[100];
		for ( int i = 0; i < parcel_years.length; i++ ) {
			parcel_years[i] = -1;
		}

		for ( int i = 0; i < culocListSize; i++ ) {
			culoc = culocList.get(i);
			culoc_id = culoc.getID();
			
			// Filter on requested locations
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;

			try {
				// Loop through the parcel objects and add new StateCU_CropPatternTS instances.
				// - the parcel amounts are added based on each supply relationship,
				//   which either use ditch percent_irrig or parcel divided by number of wells.
	
				// Replace or add in the list.  Pass individual fields because may or may
				// not need to add a new StateCU_CropPatternTS or a time series in the object...
				
				for ( StateCU_Parcel parcel : culoc.getParcelList() ) {
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
					// Loop though the supplies associated with the parcel
					for ( StateCU_Supply supply : parcel.getSupplyList() ) {
						if ( supply instanceof StateCU_SupplyFromSW ) {
							swSupply = (StateCU_SupplyFromSW)supply;
							// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
							parcelSupplyArea = swSupply.getAreaIrrig();
						}
						else {
							// Groundwater data
							if ( !parcelHasSurfaceWaterSupply ) {
								// Groundwater only so get the area from the supply
								gwSupply = (StateCU_SupplyFromGW)supply;
								// Area for supply was previously calculated as (parcel area) / (number of wells)
								parcelSupplyArea = gwSupply.getAreaIrrig();
							}
						}
						// Now check to see if there is an existing value...
						val = yts.getDataValue ( temp_DateTime );
						if ( yts.isDataMissing(val) ) {
							// Value is missing so set...
							if ( Message.isDebugOn ) {
								Message.printDebug ( dl, "", "  Initializing " + culoc_id + " from parcelId=" + parcelId + " " +
								parcelYear + " " + parcelCrop + " to " + StringUtil.formatString(parcelSupplyArea,"%.4f") );
							}
							yts.setDataValue ( temp_DateTime, parcelSupplyArea );
						}
						else {
							// Value is not missing.  Need to either set or add to it...
							//__CUCropPatternTS_match_List.add ( id + "-" + parcelYear + "-" + parcelCrop );
							/*
							if ( replace == 0 ) {
								if ( Message.isDebugOn ) {
									Message.printDebug ( dl, "", "Replacing " + id + " from " + part_id + " " +
									parcelYear + " " + parcelCrop + " with " + StringUtil.formatString(parcelSupplyArea,"%.4f") );
								}
								yts.setDataValue ( temp_DateTime, parcelSupplyArea );
							do_store_parcel = false;
								// FIXME SAM 2007-05-18 Evaluate whether need to save observations.
							}
							else if ( replace == 1 ) {*/
								if ( Message.isDebugOn ) {
									Message.printDebug ( dl, "", "  Adding " + culoc_id + " from parcelId=" + parcelId + " " +
									parcelYear + " " + parcelCrop + " + " + parcelSupplyArea + " = " +
									StringUtil.formatString( (val + parcelSupplyArea), "%.4f") );
								}
								yts.setDataValue ( temp_DateTime, val + parcelSupplyArea );
								/*
							}*/
						}

						/* TODO smalers 2020-10-11 old logic that is too complicated, remove when tested out
						StateCU_CropPatternTS cds = processor.findAndAddCUCropPatternTSValue (
							culoc_id,
							partId, // for messages
							parcelYear,
							parcelIdInt,
							parcel.getCrop(),
							parcel.getArea(),
							OutputStart_DateTime, // Period in case new TS needs to be created
							OutputEnd_DateTime,
							units,
							replaceFlag ); // Always add since don't have to deal with supplemental parcel data here
						*/
						// Add to list of parcel years that are being read.
						// - Needed for below
						addToParcelYears ( parcelYear, parcel_years );
						// Save data for use in checks and filling (does not increment acreage)...
						/* TODO smalers 2020-10-11 experimental - leave out for now.
						addParcelToCropPatternTS ( cds,
							culoc_id,
							parcelYear,
							parcel.getCrop(),
							parcel.getArea(),	// Total for irrigation method
							units );
						*/
						// TODO smalers 2020-10-11 is the following needed
						//++crop_set_count;
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