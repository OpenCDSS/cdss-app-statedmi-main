// ReadCropPatternTSFromHydroBase_Command - This class initializes, checks, and runs the ReadCropPatternTSFromHydroBase() command.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTSStructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_LocationType;
import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Location_CollectionPartType;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_SupplyFromGW;
import DWR.StateCU.StateCU_SupplyFromSW;
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
import RTi.Util.Time.StopWatch;

/**
This class initializes, checks, and runs the ReadCropPatternTSFromHydroBase() command.
*/
public class ReadParcelsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Constructor.
*/
public ReadParcelsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadParcelsFromHydroBase" );
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
	String Div = parameters.getValue( "Div" );
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

	if ( (Div != null) && !Div.isEmpty() ) {
		String [] parts = Div.trim().split(",");
		StringBuilder b = new StringBuilder();
		for ( int i = 0; i < parts.length; i++ ) {
			if ( !StringUtil.isInteger(parts[i]) ) {
				if ( b.length() == 0 ) {
						b.append("The following division(s) are not valid: " + parts[i] );
				}
				else {
					b.append ( ", " + parts[i] );
				}
			}
		}
		if ( b.length() > 0 ) {
			warning += "\n" + b;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					b.toString(), "Specify the input end as an integer YYYY." ) );
		}
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(4);
    validList.add ( "ID" );
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
    validList.add ( "Div" );
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
	return (new ReadParcelsFromHydroBase_JDialog ( parent, this )).ok();
}

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String message, routine = getClass().getSimpleName() + ".runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
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
	String Div = parameters.getValue( "Div" );
	int [] Div_array = null;
	if ( (Div != null) && !Div.isEmpty() ) {
		if ( Div.indexOf(",") < 0 ) {
			Div_array = new int[1];
			Div_array[0] = Integer.parseInt(Div);
			Message.printStatus(2, routine, "Will process division " + Div_array[0] + " for irrigated acreage.");
		}
		else {
			String [] parts = Div.split(",");
			Div_array = new int[parts.length];
			for ( int i = 0; i < parts.length; i++ ) {
				Div_array[i] = Integer.parseInt(parts[i].trim());
				Message.printStatus(2, routine, "Will process division " + Div_array[i] + " for irrigated acreage.");
			}
		}
	}

	// Get the list of CU Locations
	
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

	/*
	Get the map of parcel data for newer StateDMI.
	*/
	/* TODO smalers 2020-11-08 remove after testing - parcels are manipulated with findAndAdd...:w
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
	*/
	
	/*
	Get the list of parcel data for newer StateDMI.
	*/
	/*
	List<HydroBase_ParcelUseTS_FromSet> hydroBaseParcelUseTSFromSetList = null;
	try {
		@SuppressWarnings("unchecked")
		List<HydroBase_ParcelUseTS_FromSet> dataList =
			(List<HydroBase_ParcelUseTS_FromSet>)processor.getPropContents ( "HydroBase_ParcelUseTS_FromSet_List");
		hydroBaseParcelUseTSFromSetList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting supplemental parcel use data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	*/
	
	/*
	List<HydroBase_ParcelUseTS_FromSet> hydroBaseParcelUseTSFromSetList = null;
	try {
		@SuppressWarnings("unchecked")
		List<HydroBase_ParcelUseTS_FromSet> dataList =
			(List<HydroBase_ParcelUseTS_FromSet>)processor.getPropContents ( "HydroBase_ParcelUseTS_FromSet_List");
		hydroBaseParcelUseTSFromSetList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting supplemental parcel use data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	*/
	
	// Get the HydroBase DMI...
	
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
	
	// Check that HydroBase version is at least 20200720 for this command.
	
	if ( !hbdmi.isDatabaseVersionAtLeast(HydroBaseDMI.VERSION_20200720) ) {
        message = "The HydroBase version (" + hbdmi.getDatabaseVersion() + ") is invalid";
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

		// Because the aggregates and processing well parcels cause some data
		// management complexities, process each structure individually.  This
		// will be slower than if all structures are queried at once, but the logic is cleaner...

		StateCU_Location culoc = null; // CU Location that has crop pattern TS
		HydroBase_StructureView h_cds; // HydroBase data
		HydroBase_ParcelUseTS h_parcel; // HydroBase parcel data
		String irrig_type; // Irrigation type
		List<String> culoc_wdids = new ArrayList<String>(100);
		String culoc_id = null;
		List<HydroBase_ParcelUseTS> crop_patterns;
		List<HydroBase_StructureView> crop_patterns2 = null; // Crop pattern records for individual parts of aggregates.
		int ih, hsize; // Counter and size for HydroBase records.
		String units = "ACRE"; // Units for area
		int replace_flag = 0; // 0 to replace data in time series or 1 to add
		List<String> collection_ids; // IDs that are part of an aggregate/system
		int [] collection_ids_array; // collection_ids as an int array.
		int [] collection_years; // Years corresponding to the collection definitions.
		String part_id; // One ID from an aggregate/system.
		int collection_size; // Size of a collection.
		int crop_set_count = 0;	// The number of times that any crop value is set for a location, for this command.
		int [] parcel_years = new int[100];	// Data from HydroBase
										// TODO SAM 2007-06-14 need to rework to require users to specify the years to read.
		for ( int i = 0; i < parcel_years.length; i++ ) {
			parcel_years[i] = -1;
		}
		
		// Convert supplemental ParcelUseTS to StructureIrrigSummaryTS
		// - the supplemental data was read from ReadCropPatternTS and ReadCropPatternTSFromList commands

		/*
		List<HydroBase_StructureView_FromSet> hydroBaseStructureViewFromSetList =
			processor.convertHydroBaseParcelUseTSFromSetToHydroBaseStructureViewFromSet(hydroBaseParcelUseTSFromSetList);
		*/

		// Year is used when processing groundwater only...
		DateTime year_DateTime = new DateTime(DateTime.PRECISION_YEAR);

		// The divisions are needed for well collections specified with WDIDS and receipts.
		if ( (Div_array == null) || (Div_array.length == 0) ) {
			// Get the divisions from the list of structures
			// - split WDIDs and then lookup division from WD
			Message.printStatus(2, routine, "Determining division list from CU location identifiers to determine years to process.");
			int [] wdidParts;
			List<Integer> divList = new ArrayList<>(); // List of divisions determined from WDID list
			List<HydroBase_WaterDistrict> wdList = hbdmi.getWaterDistricts(); // All water districts
			for ( StateCU_Location culoc0 : culocList ) {
				String culocId = culoc0.getID();
				if ( HydroBase_WaterDistrict.isWDID(culocId)) {
					// CU Location is a WDID so split it into parts
					wdidParts = HydroBase_WaterDistrict.parseWDID(culocId);
					// Get the water district object and the corresponding division 
					HydroBase_WaterDistrict wd = HydroBase_WaterDistrict.lookupWaterDistrict(wdList, wdidParts[0]);
					int wdDiv = wd.getDiv();
					// If not found in the division list, add it
					boolean found = false;
					for ( Integer idiv : divList ) {
						if ( idiv.equals(wdDiv) ) {
							found = true;
							break;
						}
					}
					if ( !found ) {
						divList.add(new Integer(wdDiv));
					}
				}
			}
			// Now convert to an array for use in following code
			Div_array = new int[divList.size()];
			for ( int i = 0; i < Div_array.length; i++ ) {
				Div_array[i] = divList.get(i).intValue();
				Message.printStatus(2, routine, "Will process division " + Div_array[i] + " for irrigated acreage.");
			}
		}
		String divListString = "";
		for ( int i = 0; i < Div_array.length; i++ ) {
			if ( i != 0 ) {
				divListString += ", ";
			}
			divListString += Div_array[i];
		}
		
		// Get the parcel years that are available for the divisions of interest

		// Use the divisions that have been provided
		int [] parcelYearsForDiv = null;
		try {
			Message.printStatus(2, routine, "Determining parcel years for divisions: " + divListString );
			parcelYearsForDiv = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_array );
			for ( int i = 0; i < parcelYearsForDiv.length; i++ ) {
				Message.printStatus(2, routine, "  Will process parcel year: " + parcelYearsForDiv[i] );
			}
		}
		catch ( Exception e ) {
			parcelYearsForDiv = null;
		}
		if ( parcelYearsForDiv == null ) {
			message = "Cannot determine years of parcel data from HydroBase for divisions: " + divListString;
	 		Message.printWarning ( warningLevel,
        		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        		status.addToLog ( commandPhase,
           			new CommandLogRecord(CommandStatusType.FAILURE,
               			message, "Report to software support." ) );
					throw new CommandException ( message );
		}
		
		// Loop through locations
		List<String> partIdList = null; // List of aggregate/system parts
		// List of aggregate/system parts ID types (will contain "WDID" or "Receipt")
		List<StateCU_Location_CollectionPartIdType> partIdTypeList = null;
		//String collectionType = null;
		// Parts used for collection.  Mainly need to key on StateMod_WellStation.
		StateCU_Location_CollectionPartType collectionPartType = null;
		boolean isCollection = false;
		StopWatch stopWatchForLoc = new StopWatch();
		int totalMs = 0; // total time to process
		// The following are used to allow estimating whether a StateCU node is DIV, D&W, or WEL.
		boolean culocHasSurfaceWaterSupply = false;
		boolean culocHasGroundWaterSupply = false;
		List<String> parcelProblems = new ArrayList<>();
		for ( int i = 0; i < culocListSize; i++ ) {
			stopWatchForLoc.clearAndStart();
			culoc = culocList.get(i);
			culoc_id = culoc.getID();
			isCollection = culoc.isCollection();
			collectionPartType = null;
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			if ( isCollection ) {
				collectionPartType = culoc.getCollectionPartType();
			}

			culocHasSurfaceWaterSupply = false;
			culocHasGroundWaterSupply = false;

			crop_patterns = null; // Initialized because checked below.
			crop_set_count = 0;	// The number of times that a crop value is
								// set for this location.  If zero and a set
								// is about to occur, reset the value of the
								// time series to reflect multiple calls
								// to the command (with filling in between).
			
			// Check to see if the location is a simple diversion node,
			// an aggregate/system diversion, or a well aggregate/diversion, and process accordingly.

			try {
				notifyCommandProgressListeners ( i, culocListSize, (float)((i + 1)/((float)culocListSize)*100.0),
					"Processing CU location " + i + " of " + culocListSize );
				if ( !isCollection ) {
					// TODO SAM 2016-10-03 This will be an issue if an explicit well because don't have a way
					// to indicate whether a ditch or well WDID in StateCU (can do that in StateMod).

					// If single diversion...
					Message.printStatus ( 2, routine, "Processing single diversion \"" + culoc_id +
						"\" using parcel/supply data from HydroBase vw_CDSS_ParcelUseTSStructureToParcel (all parcels are for single location)." );
					int [] wdidParts = new int[2];
					if ( HydroBase_WaterDistrict.isWDID(culoc_id)) {
						// Read the data from irrigated parcels...
						try {
							// Parse out the WDID...
							wdidParts = HydroBase_WaterDistrict.parseWDID ( culoc_id, wdidParts );
						}
						catch ( Exception e ) {
							// Should not happen since checked above.
							Message.printWarning ( 2, routine, "Error parsing WDID \"" + culoc_id + "\" - skipping location." );
							continue;
						}

						// Make sure that the diversion structure is not also a well structure in HydroBase.
						// - this results in confusing errors later so best to warn about now
						List<HydroBase_Wells> hbwellCheckList = hbdmi.readWellsWellToParcelList(
							-1, -1, -1, null, wdidParts[0], wdidParts[1]);
						if ( hbwellCheckList.size() > 0 ) {
							message = "CU location \"" + culoc_id +
							    "\" has HydroBase vw_CDSS_ParcelUseTS (as ditch) and vw_CDSS_WellsWellToParcel (as well) data records.";
							Message.printWarning ( warningLevel, 
					        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        	status.addToLog ( commandPhase,
				            	new CommandLogRecord(CommandStatusType.WARNING,
				                	message, "A WDID should only be associated with one of the HydroBase tables. "
				                		+ "Original GIS data may have inaccurate supply type.  Additional errors may result." ) );
						}
						
						// Loop through known years of data and process if in the requested period.
						int year;
						StateCU_Parcel parcel = null;
						StateCU_SupplyFromSW supply = null;
						HydroBase_StructureView structureView = null;
						StopWatch sw = new StopWatch();
						// Reset the structure view before processing years
						structureView = null;
						for ( int iy = 0; iy < parcelYearsForDiv.length; iy++ ) {
							year = parcelYearsForDiv[iy];

							if ( ((InputStart_DateTime == null) ||(parcelYearsForDiv[iy] >= InputStart_DateTime.getYear())) &&
								((InputEnd_DateTime == null) || (parcelYearsForDiv[iy] <= InputEnd_DateTime.getYear()))) {
								// Get HydroBase_ParcelUseTS for the structure
								// - this will be cached using WD, year, and WDID
								List<HydroBase_ParcelUseTSStructureToParcel> hbParcelUseTSStructList =
									hbdmi.readParcelUseTSStructureToParcelListForStructureWdidCalYear(wdidParts[0], wdidParts[1], year);
							    if ( hbParcelUseTSStructList.size() > 0 ) {
							    	Message.printStatus ( 2, routine, "  SW: Found " + hbParcelUseTSStructList.size() +
							    		" matching structure/parcel records (SW supply) for year " +
										year + ", wd=" + wdidParts[0] + ", id=" + wdidParts[1] );
							    }
								// Create a new parcel and related supply to track the information.
								// - if the parcel is a duplicate for the location, the parcel is added once and
								//   the supply is added as an additional supply (by called code)
								for ( HydroBase_ParcelUseTSStructureToParcel hbParcelUseTSStruct : hbParcelUseTSStructList ) {
									StateCU_Parcel savedParcel = processor.getParcel(year, "" + hbParcelUseTSStruct.getParcel_id());
									if ( savedParcel != null ) {
										// Existing parcel.  Add the supply but leave other information as is.
										parcel = savedParcel;
									}
									else {
										// New parcel
										parcel = new StateCU_Parcel();
										parcel.setStateCULocation(culoc);
										parcel.setYear(year);
										// Water district is set from 
										parcel.setDiv(hbParcelUseTSStruct.getDiv());
										parcel.setWDFromParcelID(hbParcelUseTSStruct.getParcel_id());
										parcel.setCrop(hbParcelUseTSStruct.getLand_use());
										parcel.setArea(hbParcelUseTSStruct.getArea());
										parcel.setAreaUnits("acre");
										parcel.setIrrigationMethod(hbParcelUseTSStruct.getIrrig_type());
										parcel.setID("" + hbParcelUseTSStruct.getParcel_id());
										// TODO smalers 2020-11-05 moved to supply data
										//parcel.setDataSource("HB-PUTS");
										parcelProblems.clear();
										processor.findAndAddCUParcel(parcel, false, routine, "    ", parcelProblems);
										if ( parcelProblems.size() > 0 ) {
											for ( String parcelProblem : parcelProblems ) {
												Message.printWarning ( warningLevel,
													MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, parcelProblem );
												status.addToLog ( commandPhase,
													new CommandLogRecord(CommandStatusType.WARNING,
														parcelProblem, "Report to software support." ) );
											}
										}
									}
									// Supply information information is joined
									// - only one supply source (this WDID)
									supply = new StateCU_SupplyFromSW();
									supply.setDataSource("HB-PUTS");
									// Fraction and area irrigated is from HydroBase
									// - values calculated from parcel area and number of diversions is calculated
									supply.setAreaIrrigFractionHydroBase(hbParcelUseTSStruct.getPercent_irrig());
									supply.setAreaIrrigHydroBase(hbParcelUseTSStruct.getArea()*hbParcelUseTSStruct.getPercent_irrig());
									if ( (structureView == null) ||
										(structureView.getStructure_num() != hbParcelUseTSStruct.getStructure_num()) ) {
										// Parcels are related to the same structure in the loop so only need to read once.
										// TODO smalers 2020-08-24 Why is this even done since the structure WDID is
										// the same as ParcelUseTS query above?
										// - could set the structure name
										sw.clearAndStart();
										structureView = hbdmi.readStructureViewForStructure_num(hbParcelUseTSStruct.getStructure_num());
										sw.stop();
										Message.printStatus ( 2, routine, "    Reading structure view for structure_num " +
											hbParcelUseTSStruct.getStructure_num() + " took " + sw.getMilliseconds() + " ms");
									}
									supply.setWDID(HydroBase_WaterDistrict.formWDID( structureView.getWD(), structureView.getID()) );
									supply.setID(HydroBase_WaterDistrict.formWDID( structureView.getWD(), structureView.getID()) );
									// The supply will only be added once
									parcel.addSupply(supply);
									// Add to the CULocation parcels
									if ( Message.isDebugOn ) {
										sw.clearAndStart();
									}

									// Check that surface water supply WDID is not a well.
									// - check structure type here
									// - querying wells may not find and is slow
									//List<HydroBase_Wells> hbwellCheckList2 = hbdmi.readWells(structureView.getWD(), structureView.getID());
									//if ( hbwellCheckList2.size() > 0 ) {
									if ( structureView.getStr_type().charAt(0) == 'W') {
										message = "CU location \"" + culoc_id + " year " + year + " parcel ID " + parcel.getID() +
											" SW supply WDID " + structureView.getWDID() + "\" is a well.";
										Message.printWarning ( warningLevel, 
					        				MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        				status.addToLog ( commandPhase,
				            				new CommandLogRecord(CommandStatusType.WARNING,
				                				message, "Original GIS data has GW supply in SW supply. Additional errors may result." ) );
									}

									// Additionally, process the wells associated with the parcel for groundwater supply
									// - this logic is the same as collection system below for wells
									// - TODO smalers 2020-02-17 this code is similar to well aggregation so could make code modular and reuse
									// - Multiple wells might irrigate the same parcel.
									//   However, the parcel is added once above and additional wells are added as supply for that parcel.
									// - The parcel ID is unique, especially with year and first 3 digits are used for top-level cache.

									StateCU_SupplyFromGW supplyFromGW = null;

									sw.clearAndStart();
									// The cache tree uses water district (of the well), then year for index
									List<HydroBase_Wells> hbWellsList = hbdmi.readWellsWellToParcelList(
										hbParcelUseTSStruct.getParcel_id(), year, -1, null, hbParcelUseTSStruct.getStructureWD(), -1);
									if ( hbWellsList.size() > 0 ) {
										Message.printStatus ( 2, routine, "        GW: Found " + hbWellsList.size() +
											" matching well/parcel records (GW suppply) for year " +
											year + ", parcel ID=" + hbParcelUseTSStruct.getParcel_id() );
									}
									sw.stop();
									// Uncomment for troubleshooting performance...
									// Message.printStatus ( 2, routine, "  Reading well parcels for parcel_id " +
									//	hbParcelUseTSStruct.getParcel_id() + " (" + parcelCount + " of " + parcelNum
									//		+ ") took " + sw.getMilliseconds() + " ms");
									// Create a new supply
									// - if the parcel is a duplicate for the location, the parcel is added once and
									//   the supply is added as an additional supply
									for ( HydroBase_Wells hbWell : hbWellsList ) {
										// Supply information information is joined
										// - only one supply 
										supplyFromGW = new StateCU_SupplyFromGW();
										supplyFromGW.setDataSource("HB-WTP");
										supplyFromGW.setCollectionPartType("WellInDitch");
										double areaIrrigFraction = 1.0; // Well initially assumed to irrigate entire parcel
										supplyFromGW.setAreaIrrigFraction(areaIrrigFraction);  // Well relationships don't use
										supplyFromGW.setAreaIrrig(hbParcelUseTSStruct.getArea()*areaIrrigFraction);
										// Do not include in CDS area since parcel area is already with ditch
										// - TODO smalers this is now checked in parcel recompute()
										// supplyFromGW.setIncludeInCdsArea(false);
										// Set whatever is available
										// TODO smalers 2020-08-24 this is not used and takes time to query.
										//sw.clearAndStart();
										//structureView = hbdmi.readStructureViewForStructure_num(hbWell.getStructure_num());
										//sw.stop();
										//Message.printStatus ( 2, routine, "  Reading structure view for structure_num " +
										//	hbWell.getStructure_num() + " took " + sw.getMilliseconds() + " ms");
										// Can't set generic ID because association is ditch -> parcel -> well, not requested collection part
										supplyFromGW.setWDID(HydroBase_WaterDistrict.formWDID( hbWell.getWD(), hbWell.getID()) );
										supplyFromGW.setReceipt(hbWell.getReceipt());
										parcel.addSupply(supplyFromGW);
									}
									
									// Add the parcel
									
									culoc.addParcel ( parcel );
									if ( Message.isDebugOn ) {
										sw.stop();
										Message.printStatus ( 2, routine, "  Adding parcel ID " + parcel.getID() +
											" to CU Location " + culoc_id + " took " + sw.getMilliseconds() + " ms");
									}

									// Set CU Location information to help set location type.

									if ( parcel.hasGroundWaterSupply() ) {
										culocHasGroundWaterSupply = true;
									}
									if ( parcel.hasSurfaceWaterSupply() ) {
										culocHasSurfaceWaterSupply = true;
									}
	
								} // end parcels
							} // end year in period of interest
				
						} // end parcel years with data
					} // end isWDID
				} // end !isCollection - single ditch
	
				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.DITCH) ) {
					Message.printStatus ( 2, routine, "Processing diversion aggregate/system \"" + culoc_id + "\" (may or may not have wells)" );
					// Aggregate/system diversion...
					// Put together a list of WDIDs from the current CU location.  Currently ditch
					// aggregate/systems are not allowed to vary over time so request the aggregate
					// information for year 0...
	
					collection_ids = culoc.getCollectionPartIDsForYear(0);
					collection_size = 0;
					if ( collection_ids != null ) {
						collection_size = collection_ids.size();
					}
					culoc_wdids.clear();
					// This will contain the records for all the collection parts...
					int [] wdidParts = new int[2];
					for ( int j = 0; j < collection_size; j++ ) {
						part_id = collection_ids.get(j);
						try {
							// Parse out the WDID for the collection part...
							wdidParts = HydroBase_WaterDistrict.parseWDID(part_id, wdidParts);
							message = "  Processing collection part \"" + part_id + "\".";
							Message.printStatus(2, routine, message);
						}
						catch ( Exception e ) {
							message = "CU location \"" + culoc_id + "\" part \"" + part_id + "\" is not a WDID.";
							Message.printWarning ( warningLevel, 
						        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
					        status.addToLog ( commandPhase,
					            new CommandLogRecord(CommandStatusType.FAILURE,
					                message, "Verify that the part is a valid WDID." ) );
							continue;
						}

						// Make sure that the diversion structure is not also a well structure in HydroBase.
						// - this results in confusing errors later so best to warn about now
						List<HydroBase_Wells> hbwellCheckList = hbdmi.readWellsWellToParcelList(
							-1, -1, -1, null, wdidParts[0], wdidParts[1]);
						if ( hbwellCheckList.size() > 0 ) {
							message = "CU location \"" + culoc_id + " part ID \"" + part_id + 
							    "\" has HydroBase vw_CDSS_ParcelUseTS (as ditch) and vw_CDSS_WellsWellToParcel (as well) data records.";
							Message.printWarning ( warningLevel, 
					        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        	status.addToLog ( commandPhase,
				            	new CommandLogRecord(CommandStatusType.WARNING,
				                	message, "A WDID should only be associated with one of the HydroBase tables. "
				                		+ "Original GIS data may have inaccurate supply type.  Additional errors may result." ) );
						}

						// Loop through known years of data and process if in the requested period
						int year;
						StateCU_Parcel parcel = null;
						StateCU_SupplyFromSW supplyFromSW = null;
						StateCU_SupplyFromGW supplyFromGW = null;
						HydroBase_StructureView structureView = null;
						StopWatch sw = new StopWatch();
						for ( int iy = 0; iy < parcelYearsForDiv.length; iy++ ) {
							year = parcelYearsForDiv[iy];

							if ( ((InputStart_DateTime == null) ||(parcelYearsForDiv[iy] >= InputStart_DateTime.getYear())) &&
								((InputEnd_DateTime == null) || (parcelYearsForDiv[iy] <= InputEnd_DateTime.getYear()))) {
								// Get HydroBase_ParcelUseTS for the structure
								// - this will used cached data if HydroBase >= 20200824
								// - the following prints out the read time
								// - this is cached using WD, year, and WDID
								List<HydroBase_ParcelUseTSStructureToParcel> hbParcelUseTSStructList =
									hbdmi.readParcelUseTSStructureToParcelListForStructureWdidCalYear(wdidParts[0], wdidParts[1], year);
								if ( hbParcelUseTSStructList.size() > 0 ) {
									Message.printStatus ( 2, routine, "    Found " + hbParcelUseTSStructList.size() +
										" matching structure/parcel records (SW supply) for year " +
										year + ", ditch partID=" + part_id + " wd=" + wdidParts[0] + ", id=" + wdidParts[1] );
								}
								// Create a new parcel to track the information
								// - If the parcel is a duplicate for the location due to one to many relationship,
								//   the parcel is added once and the supply is added as an additional supply
								//   This should not happen since the ditch will be the same for multiple parcels.
								for ( HydroBase_ParcelUseTSStructureToParcel hbParcelUseTSStruct : hbParcelUseTSStructList ) {
									StateCU_Parcel savedParcel = processor.getParcel(year, "" + hbParcelUseTSStruct.getParcel_id());
									if ( savedParcel != null ) {
										// Existing parcel.  Add the supply but leave other information as is.
										parcel = savedParcel;
									}
									else {
										parcel = new StateCU_Parcel();
										parcel.setStateCULocation(culoc);
										parcel.setYear(year);
										parcel.setDiv(hbParcelUseTSStruct.getDiv());
										parcel.setWDFromParcelID(hbParcelUseTSStruct.getParcel_id());
										parcel.setCrop(hbParcelUseTSStruct.getLand_use());
										parcel.setArea(hbParcelUseTSStruct.getArea());
										parcel.setAreaUnits("acre");
										parcel.setIrrigationMethod(hbParcelUseTSStruct.getIrrig_type());
										parcel.setID("" + hbParcelUseTSStruct.getParcel_id());
										// TODO smalers 2020-11-05 moved to supply
										//parcel.setDataSource("HB-PUTS");
										parcelProblems.clear();
										processor.findAndAddCUParcel(parcel, false, routine, "      ", parcelProblems);
										if ( parcelProblems.size() > 0 ) {
											for ( String parcelProblem : parcelProblems ) {
												Message.printWarning ( warningLevel,
													MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, parcelProblem );
												status.addToLog ( commandPhase,
													new CommandLogRecord(CommandStatusType.WARNING,
														parcelProblem, "Report to software support." ) );
											}
										}
									}
									// Supply information information is joined
									// - only one supply 
									supplyFromSW = new StateCU_SupplyFromSW();
									supplyFromSW.setDataSource("HB-PUTS");
									// Fraction and area irrigated is from HydroBase
									// - values calculated from parcel area and number of diversions is calculated
									supplyFromSW.setAreaIrrigFractionHydroBase(hbParcelUseTSStruct.getPercent_irrig());
									supplyFromSW.setAreaIrrigHydroBase(hbParcelUseTSStruct.getArea()*hbParcelUseTSStruct.getPercent_irrig());
									structureView = hbdmi.readStructureViewForStructure_num(hbParcelUseTSStruct.getStructure_num());
									// ID for the supply is the same as WDID
									supplyFromSW.setWDID(HydroBase_WaterDistrict.formWDID( structureView.getWD(), structureView.getID()) );
									supplyFromSW.setID(supplyFromSW.getWDID());
									parcel.addSupply(supplyFromSW);

									// Check that surface water supply WDID is not a well.
									// - could check structure type here but query wells to be more certain
									//List<HydroBase_Wells> hbwellCheckList2 = hbdmi.readWellsWellToParcelList(
									//	-1, -1, -1, null, structureView.getWD(), structureView.getID());
									//if ( hbwellCheckList2.size() > 0 ) {
									if ( structureView.getStr_type().charAt(0) == 'W') {
										message = "CU location \"" + culoc_id + " year " + year + " parcel ID " + parcel.getID() +
											" SW supply WDID " + structureView.getWDID() + "\" is a well.";
										Message.printWarning ( warningLevel, 
					        				MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        				status.addToLog ( commandPhase,
				            				new CommandLogRecord(CommandStatusType.WARNING,
				                				message, "Original GIS data has GW supply in SW supply. Additional errors may result." ) );
									}

									// Additionally, process the wells associated with the parcel for groundwater supply
									// - TODO smalers 2020-02-17 this code is similar to well aggregation so could make code modular and reuse
									// - Multiple wells might irrigate the same parcel.
									//   However, the parcel is added once above and additional wells are added as supply for that parcel.
									// - The parcel ID is unique, especially with year and first 3 digits are used for top-level cache.

									sw.clearAndStart();
									// The cache tree uses water district (of the well), then year for index
									List<HydroBase_Wells> hbWellsList = hbdmi.readWellsWellToParcelList(
										hbParcelUseTSStruct.getParcel_id(), year, -1, null, hbParcelUseTSStruct.getStructureWD(), -1);
									if ( hbWellsList.size() > 0 ) {
										Message.printStatus ( 2, routine, "      Found " + hbWellsList.size() +
											" matching well/parcel records (GW suppply) for year " +
											year + ", parcel ID=" + hbParcelUseTSStruct.getParcel_id() );
									}
									sw.stop();
									// Uncomment for troubleshooting performance...
									// Message.printStatus ( 2, routine, "  Reading well parcels for parcel_id " +
									//	hbParcelUseTSStruct.getParcel_id() + " (" + parcelCount + " of " + parcelNum
									//		+ ") took " + sw.getMilliseconds() + " ms");
									// Create a new supply
									// - if the parcel is a duplicate for the location, the parcel is added once and
									//   the supply is added as an additional supply
									for ( HydroBase_Wells hbWell : hbWellsList ) {
										// Supply information information is joined
										// - only one supply 
										supplyFromGW = new StateCU_SupplyFromGW();
										supplyFromGW.setDataSource("HB-WTP");
										supplyFromGW.setCollectionPartType("WellInDitch");
										double areaIrrigFraction = 1.0; // Well initially assumed to irrigate entire parcel
										supplyFromGW.setAreaIrrigFraction(areaIrrigFraction);  // Well relationships don't use
										supplyFromGW.setAreaIrrig(hbParcelUseTSStruct.getArea()*areaIrrigFraction);
										// Do not include in CDS area since parcel area is already with ditch
										// - TODO smalers this is now checked in parcel recompute()
										//supplyFromGW.setIncludeInCdsArea(false);
										// Set whatever is available
										// TODO smalers 2020-08-24 this is not used and takes time to query.
										//sw.clearAndStart();
										//structureView = hbdmi.readStructureViewForStructure_num(hbWell.getStructure_num());
										//sw.stop();
										//Message.printStatus ( 2, routine, "  Reading structure view for structure_num " +
										//	hbWell.getStructure_num() + " took " + sw.getMilliseconds() + " ms");
										// 
										// Can't set generic ID because association is ditch -> parcel -> well, not requested collection part
										supplyFromGW.setWDID(HydroBase_WaterDistrict.formWDID( hbWell.getWD(), hbWell.getID()) );
										supplyFromGW.setReceipt(hbWell.getReceipt());
										parcel.addSupply(supplyFromGW);
									}

									// Add to the CULocation parcels
									culoc.addParcel ( parcel );

									// Set CU Location information to help set location type .

									if ( parcel.hasGroundWaterSupply() ) {
										culocHasGroundWaterSupply = true;
									}
									if ( parcel.hasSurfaceWaterSupply() ) {
										culocHasSurfaceWaterSupply = true;
									}

								} // end parcels for collection part
							} // end in requested period

						} // end years with parcel data
					} // end collection parts

				} // end Ditch collection

				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.PARCEL) ) {
					// Well only collection defined with list of parcels.
					if ( 1 > 0 ) {
						message = "CU location \"" + culoc_id + "\" is a collection specified with parcels - not currently handled.  "
							+ "Specify collection using WDIDs and RECEIPTs";
						Message.printWarning ( warningLevel, 
						    MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
						        status.addToLog ( commandPhase,
						            new CommandLogRecord(CommandStatusType.FAILURE,
						                message, "Verify that the part is a valid WDID." ) );
				        continue;
					}

					Message.printStatus ( 2, routine, "Processing well aggregate/system \"" + culoc_id + "\"" );
					message = "Using parcels to specify groundwater-only aggregation/system is being phased out - "
						+ "should specify aggregation using part type \"Well\" with well WDID and well permit receipt.";
					Message.printWarning ( warningLevel, 
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( commandPhase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Change aggregation/system data from parcel to well part type." ) );
	
					// Put together a list of parcel IDs from the current CU location.
					// The aggregate/systems are allowed to vary over time so only read the parcels for
					// the specific years.
	
					collection_years = culoc.getCollectionYears();
					if ( collection_years == null ) {
						return;
					}
					// Loop over available collection years and read if in the requested input period.
					for ( int iy = 0; iy < collection_years.length; iy++ ) {
						// Get the parcel IDs for the year of interest...
						year_DateTime.setYear ( collection_years[iy] );
						collection_ids = culoc.getCollectionPartIDsForYear(collection_years[iy]);
						collection_size = 0;
						collection_ids_array = null;
						if ( collection_ids != null ) {
							collection_size = collection_ids.size();
							collection_ids_array = new int[collection_size];
							for ( int ic = 0; ic < collection_size; ic++ ) {
								part_id = collection_ids.get(ic);
								if ( !StringUtil.isInteger(part_id)) {
									message = "CU location \"" + culoc_id + "\" part ID \"" + part_id +
									"\" is not an integer.";
									Message.printWarning ( warningLevel, 
								        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
							        status.addToLog ( commandPhase,
							            new CommandLogRecord(CommandStatusType.FAILURE,
							                message, "Verify that the part is a valid WDID." ) );
									// Should not return anything from HydroBase...
									collection_ids_array[ic] = 0;
								}
								else {
									collection_ids_array[ic] = Integer.parseInt ( collection_ids.get(ic) );
								}
							}
						}
	
						if ( collection_ids_array == null ) {
							message = "CU location \"" + culoc_id + "\" has no aggregate/system parts.";
							Message.printWarning ( warningLevel, 
						        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
					        status.addToLog ( commandPhase,
					            new CommandLogRecord(CommandStatusType.FAILURE, message,
					            	"Verify that location has its aggregate parts specified correctly for year " + iy + "." ) );
							continue;
						}
	
						// Read from HydroBase for the year of interest but only if in the input period.
						// If no input period was specified, read all.

						StringBuilder b = new StringBuilder();
						for ( int bi = 0; bi < collection_ids_array.length; bi++ ) {
							if ( bi != 0 ) {
								b.append(",");
							}
							b.append(collection_ids_array[bi]);
						}
						if ( ((InputStart_DateTime == null) ||(collection_years[iy] >= InputStart_DateTime.getYear())) &&
							((InputEnd_DateTime == null) || (collection_years[iy] <= InputEnd_DateTime.getYear()))) {
							Message.printStatus(2, "XXX"+routine, "Calling hbdmi.readParcelUseTSListForParcelList(Div="+
								culoc.getCollectionDiv() + ", parcel_ids=["+ b.toString() +
								"], land use=null, irrig type=null, date1="+year_DateTime + ", date2=" + year_DateTime);
							crop_patterns = hbdmi.readParcelUseTSListForParcelList(
								culoc.getCollectionDiv(),// Division
								collection_ids_array, // parcel ids
								null, // land use
								null, // irrig type
								year_DateTime, // Collection year
								year_DateTime );
						}
						else {
							// Collection year is not in the requested input period so don't read...
							continue;
						}
	
						// Process the records below into the collection ID...
	
						replace_flag = 1; // 1 means add
						hsize = 0;
						if ( crop_patterns != null ) {
							hsize = crop_patterns.size();
						}
						Message.printStatus ( 2, routine, "For location " + culoc_id + " year=" +
							year_DateTime.getYear() + ", processing " + hsize + " well/parcel records" );
	
						for ( ih = 0; ih < hsize; ih++) {
							h_parcel = crop_patterns.get(ih);
							// Filter out lands that are not irrigated...
							irrig_type = h_parcel.getIrrig_type();
							// TODO SAM 2004-03-01 - don't want to hard-code strings but need to handle
							// revisions in HydroBaseDMI - ref_irrig_type should indicate whether irrigated
							if ( irrig_type.equalsIgnoreCase("NA") ) {
								// Does not irrigate...
								continue;
							}
							// Need the following when one read command, then filling, then another read.
							if ( crop_set_count == 0 ) {
								// Reset all crops to missing for the year to prevent double-counting...
								/* FIXME
								resetCropPatternTS ( processor, cdsList, OutputStart_DateTime, OutputEnd_DateTime,
									culoc_id, h_parcel.getCal_year(), h_parcel.getCal_year() );
									*/
							}
							// Replace or add in the list.  Pass individual fields because may or may not
							// need to add a new StateCU_CropPatternTS or a time series in the object...
							StateCU_CropPatternTS cds = processor.findAndAddCUCropPatternTSValue (
									culoc_id, "" +
									h_parcel.getParcel_id(),
									h_parcel.getCal_year(),
									h_parcel.getParcel_id(),
									h_parcel.getLand_use(),
									h_parcel.getArea(),
									OutputStart_DateTime, // Output in case
									OutputEnd_DateTime,	// new TS is needed
									units, replace_flag );
							/* TODO smalers 2020-08-25 not used in this command - need to refactor
							addToParcelYears ( h_parcel.getCal_year(), parcel_years );
							*/
							// Save the data for checks and filling based on parcel information
							/* TODO smalers 2020-08-25 not used in this command - need to refactor
							addParcelToCropPatternTS ( cds,
									"" + h_parcel.getParcel_id(),
									h_parcel.getCal_year(),
									h_parcel.getLand_use(),
									h_parcel.getArea(),
									units );
									*/
							++crop_set_count;

							// Set CU Location information to help set location type .

							/* TODO smalers 2020-10-11 enable if parcels are supported
							if ( parcel.hasGroundWaterSupply() ) {
								culocHasGroundWaterSupply = true;
							}
							if ( parcel.hasSurfaceWaterSupply() ) {
								culocHasSurfaceWaterSupply = true;
							}
							*/

						} // end parcels in collection
					} // end years in collection
				} // end Well collection specified with parcels

				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.WELL) ) {
					// Well only collection defined with list of well WDIDs and/or receipts.
					
					String collectionPartTypeString = "Well";
					Message.printStatus ( 2, routine, "Processing well aggregate/system \"" + culoc_id +
						"\" using list of WDID/permit receipt for parts (expect 1+ wells)." );
					// The collection definitions are the same each year (same list of wells throughout the period).
					// If the period is provided then the start and end are known.
					// However, don't want to requery data every year because that will be slow.
					// Therefore, determine the unique calendar years available from parcel data
					//in the requested period and process those years.
					
					// The aggregate/system information spans all years whereas the parcel associations are by year.
					// Get the list of parts.
					int parcelYear = -1; // Parcel year is irrelevant
					partIdList = culoc.getCollectionPartIDsForYear(parcelYear);
					partIdTypeList = culoc.getCollectionPartIDTypes(); // Will not vary by year
					// Loop through the parcel years determined for divisions of interest
					StateCU_Parcel parcel = null;
					StateCU_SupplyFromGW supplyFromGW = null;
					HydroBase_StructureView structureView = null;
					int wellParcelId;
					int wellDiv;
					String wellReceipt;
					int wellWD;
					int wellID;
					for ( int iy = 0; iy < parcelYearsForDiv.length; iy++ ) {
						if ( ((InputStart_DateTime == null) ||(parcelYearsForDiv[iy] >= InputStart_DateTime.getYear())) &&
							((InputEnd_DateTime == null) || (parcelYearsForDiv[iy] <= InputEnd_DateTime.getYear()))) {
							// Get the parcels that are associated with the wells and then use the same logic as if processing parcels
							List<HydroBase_Wells> hbWellsList = null; // List of HydroBase vw_CDSS_WellsWellToParcel records for location for all years
							List<Integer> parcelListForYear = new ArrayList<>();
							int year = parcelYearsForDiv[iy];
							int [] parcelYearArray = new int[1]; // For one year
							// Loop through the well identifiers and read the parcels associated with the wells
							// Get the vw_CDSS_WellsWellToParcel records, which tie well WDID and permit number to parcel.
							// This will give the list of parcels to process.
							int iPart = -1;
							StateCU_Location_CollectionPartIdType partIdType; // Part type for specified ID, indicates whether WDID or receipt
							int [] wdidParts = new int[2];
							for ( String partId : partIdList ) {
								++iPart;
								partIdType = partIdTypeList.get(iPart);
								Message.printStatus ( 2, routine, "  Processing part ID \"" + partId + "\" part type " + partIdType + "." );
								// Get the well associated with parcel
								// - only contains well to parcel (no parcel use)
								if ( partIdType == StateCU_Location_CollectionPartIdType.WDID ) {
									// Split the WDID into parts in case it is not always 7 digits
									wdidParts = HydroBase_WaterDistrict.parseWDID(partId,wdidParts);
									wellParcelId = -1; // Not used since querying well WDID
									wellDiv = -1; // Not used since querying well WDID
									wellReceipt = null; // Not used since querying well WDID
									hbWellsList = hbdmi.readWellsWellToParcelList(wellParcelId, year, wellDiv, wellReceipt,
										wdidParts[0], wdidParts[1]);
								    if ( hbWellsList.size() > 0 ) {
								    	Message.printStatus ( 2, routine, "    Found " + hbWellsList.size() +
								    		" matching well/parcel records (GW supply) for year " +
											year + ", wd=" + wdidParts[0] + ", id=" + wdidParts[1] );
								    }

								    // Make sure that the diversion structure is not also a well structure in HydroBase.
								    // - this results in confusing errors later so best to warn about now
								    if ( iy == 0 ) {
								    	List<HydroBase_ParcelUseTSStructureToParcel> hbditchCheckList =
									    	hbdmi.readParcelUseTSStructureToParcelListForStructureWdidCalYear(wdidParts[0], wdidParts[1], -1);
								    	if ( hbditchCheckList.size() > 0 ) {
									    	message = "CU location \"" + culoc_id + " part ID \"" + partId + 
										    	"\" has HydroBase vw_CDSS_ParcelUseTS (as ditch) and vw_CDSS_WellsWellToParcel (as well) data records.";
									    	Message.printWarning ( warningLevel, 
						        		    	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
					        		    	status.addToLog ( commandPhase,
					            		    	new CommandLogRecord(CommandStatusType.WARNING,
					                		    	message, "A WDID should only be associated with one of the HydroBase tables. "
					                		   			+ "Original GIS data may have inaccurate supply type.  Additional errors may result." ) );
								    	}
								    }
								}
								else if ( partIdType == StateCU_Location_CollectionPartIdType.RECEIPT ) {
									// Read well to parcel data for permit receipt
									wellParcelId = -1; // Not used since querying well WDID
									wellDiv = -1; // Not used since querying well WDID
									wellWD = culoc.getCollectionPartIDWD(partId); // Determined up front when collection information was specified
									wellID = -1;
									try {
										hbWellsList = hbdmi.readWellsWellToParcelList(wellParcelId, year, wellDiv, partId, wellWD, wellID);
									}
									catch ( Exception e ) {
										// Possible to have an error with bad receipt and no WD
										message = "Error getting well/parcel data for year " + year + ", receipt=" + partId + " (" + e + ").";
										Message.printWarning ( warningLevel, 
				        					MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, "    " + message );
			        					status.addToLog ( commandPhase,
			            					new CommandLogRecord(CommandStatusType.WARNING, message,
			            						"Check the log file.  Verify that the well/parcel is in HydroBase.  Report to software support if necessary." ) );
										Message.printWarning ( warningLevel, routine, e );
										// Create an empty list to handle below
										hbWellsList = new ArrayList<>();
									}
								    if ( hbWellsList.size() > 0 ) {
								    	Message.printStatus ( 2, routine, "    Found " + hbWellsList.size() +
								    		" matching well/parcel records (GW supply) for year " +
											year + ", receipt=" + partId );
								    }
								}
								// Create a new parcel to track the information
								// - if the parcel is a duplicate for the location, the parcel is added once and
								//   the supply is added as an additional supply
								for ( HydroBase_Wells hbWell : hbWellsList ) {
									// Get the ParcelUseTS
									// Get HydroBase_ParcelUseTS based on parcel ID, necessary to get the crop type and use
									// Prior to HydroBase 20200720
									//List<HydroBase_ParcelUseTS> hbParcelUseTSList =
									//	hbdmi.readParcelUseTSList(year, -1, hbWell.getParcel_id(), cacheHydroBase);
									List<HydroBase_ParcelUseTS> hbParcelUseTSList =
										hbdmi.readParcelUseTSList(year, -1, hbWell.getParcel_id() );
									// The following messge does not provide a lot of value
									//Message.printStatus(2, routine, "      Found " + hbParcelUseTSList.size() + " ParcelUseTS for parcel " +
									//	hbWell.getParcel_id() );
									for ( HydroBase_ParcelUseTS hbParcelUseTS : hbParcelUseTSList ) {
										// Now add the parcel and supply
										StateCU_Parcel savedParcel = processor.getParcel(year, "" + hbWell.getParcel_id());
										if ( savedParcel != null ) {
											// Existing parcel.  Add the supply but leave other information as is.
											parcel = savedParcel;
										}
										else {
											parcel = new StateCU_Parcel();
											parcel.setStateCULocation(culoc);
											parcel.setYear(year);
											parcel.setDiv(hbParcelUseTS.getDiv());
											parcel.setWDFromParcelID(hbWell.getParcel_id());
											// TODO smalers 2020-11-05 evaluate getting WD
											//parcel.setWD();
											parcel.setCrop(hbParcelUseTS.getLand_use());
											parcel.setArea(hbParcelUseTS.getArea());
											parcel.setAreaUnits("acre");
											parcel.setIrrigationMethod(hbParcelUseTS.getIrrig_type());
											parcel.setID("" + hbWell.getParcel_id());
											// TODO smalers 2020-11-05 moved to supply data
											//parcel.setDataSource("HB-WTP");
											parcelProblems.clear();
											processor.findAndAddCUParcel(parcel, false, routine, "      ", parcelProblems );
											if ( parcelProblems.size() > 0 ) {
												for ( String parcelProblem : parcelProblems ) {
													Message.printWarning ( warningLevel,
														MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, parcelProblem );
													status.addToLog ( commandPhase,
														new CommandLogRecord(CommandStatusType.WARNING,
															parcelProblem, "Report to software support." ) );
												}
											}
										}
										// Add supply information to the parcel.
										supplyFromGW = new StateCU_SupplyFromGW();
										supplyFromGW.setDataSource("HB-WTP");
										supplyFromGW.setCollectionPartType(collectionPartTypeString);
										supplyFromGW.setCollectionPartIdType(partIdType.toString());
										double areaIrrigFraction = 1.0;
										supplyFromGW.setAreaIrrigFraction(areaIrrigFraction);  // Well relationships don't use
										supplyFromGW.setAreaIrrig(hbParcelUseTS.getArea()*areaIrrigFraction);
										structureView = hbdmi.readStructureViewForStructure_num(hbWell.getStructure_num());
										// Can set the ID since a specific part ID is specified for the collection.
										supplyFromGW.setID(partId);
										if ( partIdType == StateCU_Location_CollectionPartIdType.WDID ) {
											supplyFromGW.setWDID(partId);
										}
										else if ( partIdType == StateCU_Location_CollectionPartIdType.RECEIPT ) {
											supplyFromGW.setReceipt(partId);
										}
										parcel.addSupply(supplyFromGW);
										// Add to the CULocation parcels
										culoc.addParcel ( parcel );

										if ( parcel.hasGroundWaterSupply() ) {
											culocHasGroundWaterSupply = true;
										}
										if ( parcel.hasSurfaceWaterSupply() ) {
											culocHasSurfaceWaterSupply = true;
										}

									} // end parcels
								} // end list of well/parcel
							} // end part id list
						} // end in period of interest
					} // end years with parcel data

				} // end well collection specified with Well IDs

				else if ( isCollection ) {
					message = "CU Location \"" + culoc_id + "\" collection type \"" + culoc.getCollectionType() + " - software does not yet handle.";
					Message.printWarning ( warningLevel, 
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( commandPhase,
			            new CommandLogRecord(CommandStatusType.FAILURE, message,
			            	"Check the log file - report to software support if necessary." ) );
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error processing parcel data for \"" + culoc_id + "\" (" + e + ").";
				Message.printWarning ( warningLevel, 
			        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( commandPhase,
		            new CommandLogRecord(CommandStatusType.FAILURE, message,
		            	"Check the log file - report to software support if necessary." ) );
			}
			stopWatchForLoc.stop();
			Message.printStatus ( 2, routine, "  Processed CU Location \"" + culoc_id +
				"\" parcel data in " + stopWatchForLoc.getMilliseconds() + " ms." );
			totalMs += stopWatchForLoc.getMilliseconds();
			
			// Based on the supply for the CU Location, estimate the node type DIV, D&W, WEL so that the parcel display report
			// can display.  This is useful to understand the data.
			
			//else if ( !culocHasSurfaceWaterSupply && culocHasGroundWaterSupply ) {
			if ( culoc.hasGroundwaterOnlySupply() ) {
				// Model nodes that are single wells are not supported - must be defined as a well aggregate.
				// Therefore ignore whether have any surface water supply
				// (should not have any because of definition of well-only node) and rely on the aggregate check.
				culoc.setLocationType(StateCU_LocationType.WELL);
			}
			// The remainder of these are based on checking the supplies associated with parcels.
			// - necessary because what looks like a diversion single node or
			//   aggregate/system may have also have wells after processing
			else if ( culocHasSurfaceWaterSupply && culocHasGroundWaterSupply ) {
				culoc.setLocationType(StateCU_LocationType.DIVERSION_AND_WELL);
			}
			else if ( culocHasSurfaceWaterSupply && !culocHasGroundWaterSupply ) {
				// Clearly a diversion node.
				culoc.setLocationType(StateCU_LocationType.DIVERSION);
			}
			//else if ( !culocHasSurfaceWaterSupply && !culocHasGroundWaterSupply ) {
			else {
				culoc.setLocationType(StateCU_LocationType.UNKNOWN);
			}
			
		} // end loop through CU Locations
		
		Message.printStatus ( 2, routine, "Processed " + culocListSize + " CU Locations in " +
			totalMs + " ms." );
	}
    catch ( Exception e ) {
        message = "Unexpected error CU Location parcels from HydroBase (" + e + ").";
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
	String Div = parameters.getValue( "Div" );
	
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
	if ( Div != null && Div.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=\"" + Div + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}