// ReadCropPatternTSFromHydroBase_Command - This class initializes, checks, and runs the ReadCropPatternTSFromHydroBase() command.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Location_CollectionPartType;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Util;
import RTi.DMI.DMIUtil;
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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the ReadCropPatternTSFromHydroBase() command.
*/
public class ReadCropPatternTSFromHydroBase_Command
extends AbstractCommand implements Command
{

/**
Values for the DataFrom parameter.
*/
protected final String _Parcels = "Parcels";
protected final String _Summary = "Summary";

/**
Constructor.
*/
public ReadCropPatternTSFromHydroBase_Command () {
	super();
	setCommandName ( "ReadCropPatternTSFromHydroBase" );
}

/**
Add parcel data from HydroBase to a StateCU_CropPatternTS so that it can be used later for filling and data checks.
For example, the FillCropPatternTSUsingWellRights() command uses the data.
This method DOES NOT manage the crop pattern time series - it simply adds the parcel
data related to the crop pattern time series to a list.
@param cds StateCU_CropPatternTS instance to in which to store data.
@param parcel_id The identifier for the parcel.
@param year The year for the parcel.
@param land_use The land use (crop name) for the parcel.
@param area The area of the parcel.
@param units The area units for the parcel.
*/
private void addParcelToCropPatternTS ( StateCU_CropPatternTS cds, String parcel_id, int year,
	String land_use, double area, String units ) {

	StateCU_Parcel parcel = new StateCU_Parcel();
	parcel.setID ( parcel_id );
	parcel.setYear ( year );
	parcel.setArea ( area );
	parcel.setAreaUnits ( units );
	cds.addParcel ( parcel );
}

/**
Add to the unique list of parcel years that were processed.
*/
private void addToParcelYears ( int year, int [] parcel_years ) {
	boolean found = false;
	int insert_i = 0;
	for ( int i = 0; i < parcel_years.length; i++ ) {
		if ( parcel_years[i] < 0 ) {
			// No more data to search.
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
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue( "InputEnd" );
	String Div = parameters.getValue( "Div" );
	String DataFrom = parameters.getValue( "DataFrom" ); // Experimental.
	String AreaPrecision = parameters.getValue( "AreaPrecision" ); // Experimental.
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

	if ( (AreaPrecision != null) && (AreaPrecision.length() != 0) && !StringUtil.isInteger(AreaPrecision) ) {
		message = "The area precision is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division as an integer." ) );
	}

	if ( (DataFrom != null) && (DataFrom.length() > 0) &&
		!DataFrom.equalsIgnoreCase(_Parcels) && !DataFrom.equalsIgnoreCase(_Summary) ) {
		message = "The DataFrom value (" + DataFrom + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify DataFrom as " + _Parcels + " or " + _Summary + ".") );
	}

	// Check for invalid parameters.
	List<String> validList = new ArrayList<>(8);
    validList.add ( "ID" );
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
    validList.add ( "Div" );
	validList.add ( "SetFlag" );
	validList.add ( "SetFlagDescription" );
    validList.add ( "DataFrom" );
    validList.add ( "AreaPrecision" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent ) {
	// The command will be modified if changed.
	return (new ReadCropPatternTSFromHydroBase_JDialog ( parent, this )).ok();
}

/**
 * Check whether a location has corresponding SetCropPatternTS() command that matches the ID.
 */
private boolean locationHasSetCommand ( StateDMI_Processor processor, String culoc_id ) {
	String routine = getClass().getSimpleName() + ".locationHasSetCommand";
	boolean hasSetCommand = false;
	// Get SetCropPatternTS commands after this command.
	List<String> commandsToFind = new ArrayList<>();
	commandsToFind.add("SetCropPatternTS");
	commandsToFind.add("SetCropPatternTSFromList");
	List<Command> commandList = StateDMICommandProcessorUtil.getCommandsBeforeIndex(
		processor.indexOf(this), processor, commandsToFind, true);
	if ( commandList.size() == 0 ) {
		// Loop through.
		for ( Command command : commandList ) {
			String ID = command.getCommandParameters().getValue("ID");
			String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
			if ( command instanceof SetCropPatternTS_Command ) {
				if ( culoc_id.matches(idpattern_Java) ) {
					// Identifier matches.
					hasSetCommand = true;
					break;
				}
			}
			else if ( command instanceof SetCropPatternTS_Command ) {
				if ( culoc_id.matches(idpattern_Java) ) {
					// Identifier matches, but also need to read the list file to see if any location IDs match.
					String ListFile = command.getCommandParameters().getValue ( "ListFile" );
					String ListFile_full = IOUtil.verifyPathForOS(
	        			IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
                			StateDMICommandProcessorUtil.expandParameterValue(processor,this,ListFile)));
					String IDCol = command.getCommandParameters().getValue ( "IDCol" );
					int IDCol_int = -1;
					if ( IDCol != null ) {
						IDCol_int = Integer.parseInt ( IDCol ) - 1;
					}
					// Read the list file using the table.

					PropList props = new PropList ("");
					props.set ( "Delimiter=," );		// See existing prototype.
					props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this.
					props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
					try {
						DataTable table = DataTable.parseFile ( ListFile_full, props );
						TableRecord rec = null;
						String id;

						int tsize = 0;
						if ( table != null ) {
							tsize = table.getNumberOfRecords();
						}
						for ( int j = 0; j < tsize; j++ ) {
							rec = table.getRecord(j);
							id = (String)rec.getFieldValue(IDCol_int);

							if ( id.matches(idpattern_Java) ) {
								// Identifier matches.
								hasSetCommand = true;
								break;
							}
						}
					}
					catch ( Exception e ) {
						Message.printWarning(3, routine, "Error processing SetCropPatternTSFromList file to check if ID is matched.");
					}
				}
			}
		}
	}
	// Check whether the ID parameter of the command matches culoc_id.
	return hasSetCommand;
}

/**
Reset crop pattern time series to zero, used in cases where multiple readCropPatternTSFromHydroBase() commands are used.
@param cdsList list of StateCU_CropPatternTS being processed.
@param culoc_id Identifier for CU location to have its crop pattern time series reset.
@param cal_year_start The first calendar year to reset.
@param cal_year_end The last calendar year to reset.
*/
private void resetCropPatternTS ( StateDMI_Processor processor, List<StateCU_CropPatternTS> cdsList,
	DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime,
	String culoc_id, int cal_year_start, int cal_year_end ) {
	// Get the crop pattern time series for the location.
	// If none matches, return without changing anything (data will be added OK).
	StateCU_CropPatternTS cds = null;
	int pos = StateCU_Util.indexOf(cdsList,culoc_id);
	if ( pos >= 0 ) {
		// Get the time series.
		cds = (StateCU_CropPatternTS)cdsList.get(pos);
	}
	if ( cds == null ) {
		// No need to reset.
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
			// Replace or add in the list.
			// Pass individual fields because we may or may not need to add a new StateCU_CropPatternTS or a time series in the object.
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

/**
Method to execute the ReadCropPatternTSFromHydroBase() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;

	CommandPhaseType commandPhase = CommandPhaseType.RUN;
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
	String SetFlag = parameters.getValue ( "SetFlag" );
	String SetFlagDescription = parameters.getValue ( "SetFlagDescription" );

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

	// Get the supplemental crop pattern data specified with SetCropPatternTS() and SetCropPatternTSFromList() commands.

	/* TODO smalers 2020.
	List<StateDMI_HydroBase_ParcelUseTS> hydroBaseSupplementalParcelUseTSList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateDMI_HydroBase_ParcelUseTS> dataList =
			(List<StateDMI_HydroBase_ParcelUseTS>)processor.getPropContents ( "HydroBase_SupplementalParcelUseTS_List");
		hydroBaseSupplementalParcelUseTSList = dataList;
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

	// Get the HydroBase DMI.

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

    // Output period will be used if not specified with InputStart and InputEnd.

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
		// Input error.
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

		// Because the aggregates and processing well parcels cause some data
		// management complexities, process each structure individually.
		// This will be slower than if all structures are queried at once, but the logic is cleaner.

		StateCU_Location culoc = null; // CU Location that has crop pattern TS.
		HydroBase_StructureView h_cds; // HydroBase data.
		HydroBase_ParcelUseTS h_parcel; // HydroBase parcel data.
		String irrig_type; // Irrigation type.
		List<String> culoc_wdids = new ArrayList<>(100);
		String culoc_id = null;
		List<HydroBase_ParcelUseTS> crop_patterns;
		List<HydroBase_StructureView> crop_patterns_sv = null; // Crop pattern records from HydroBase for parcels.
		List<HydroBase_StructureView> crop_patterns2 = null; // Crop pattern records for individual parts of aggregates.
		int ih, hsize; // Counter and size for HydroBase records.
		String units = "ACRE"; // Units for area.
		int replace_flag = 0; // 0 to replace data in time series or 1 to add.
		List<String> collection_ids; // IDs that are part of an aggregate/system.
		int [] collection_ids_array; // collection_ids as an int array.
		int [] collection_years; // Years corresponding to the collection definitions.
		String part_id; // One ID from an aggregate/system.
		int collection_size; // Size of a collection.
		int [] wdid_parts = new int[2];	// WDID parts for parsing.
		boolean processing_ditches = true; // Whether ditches are being processed (false indicates wells and parcels).
		int crop_set_count = 0;	// The number of times that any crop value is set for a location, for this command.
		int [] parcel_years = new int[100];	// Data from HydroBase.
										// TODO SAM 2007-06-14 need to rework to require users to specify the years to read.
		for ( int i = 0; i < parcel_years.length; i++ ) {
			parcel_years[i] = -1;
		}

		// Convert supplemental ParcelUseTS to StructureIrrigSummaryTS:
		// - the supplemental data was read from ReadCropPatternTS and ReadCropPatternTSFromList commands

		List<HydroBase_StructureView_FromSet> hydroBaseStructureViewFromSetList =
			processor.convertHydroBaseParcelUseTSFromSetToHydroBaseStructureViewFromSet(hydroBaseParcelUseTSFromSetList);

		// Year is used when processing groundwater only.
		DateTime year_DateTime = new DateTime(DateTime.PRECISION_YEAR);

		// The divisions are needed for well collections specified with WDIDS and receipts.
		String divListString = "UNKNOWN";
		if ( (Div_array == null) || (Div_array.length == 0) ) {
			// Get the divisions from the list of structures:
			// - split WDIDs and then lookup division from WD
			Message.printStatus(2, routine, "Divisions were not specified as a command parameter.");
			Message.printStatus(2, routine, "Determining division list from CU location identifiers that are WDIDs (used to determine available parcel years).");
			int [] wdidParts;
			List<Integer> divList = new ArrayList<>(); // List of divisions determined from WDID list.
			List<HydroBase_WaterDistrict> wdList = hbdmi.getWaterDistricts(); // All water districts.
			for ( StateCU_Location culoc0 : culocList ) {
				String culocId = culoc0.getID();
				if ( HydroBase_WaterDistrict.isWDID(culocId)) {
					// CU Location is a WDID so split it into parts.
					wdidParts = HydroBase_WaterDistrict.parseWDID(culocId);
					// Get the water district object and the corresponding division .
					HydroBase_WaterDistrict wd = HydroBase_WaterDistrict.lookupWaterDistrict(wdList, wdidParts[0]);
					int wdDiv = wd.getDiv();
					// If not found in the division list, add it.
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
			if ( divList.size() > 0 ) {
				// Convert to an array for use in following code.
				Div_array = new int[divList.size()];
				for ( int i = 0; i < Div_array.length; i++ ) {
					Div_array[i] = divList.get(i).intValue();
					Message.printStatus(2, routine, "Will process division " + Div_array[i] + " for irrigated acreage.");
					// Format the string for logging.
					if ( i == 0 ) {
						divListString += ", ";
					}
					divListString += Div_array[i];
				}
			}
			else {
				Message.printStatus(2, routine, "Will process NO DIVISIONS for irrigated acreage (this is likly an error in input).");
			}
		}

		// Get the parcel years that are available for the divisions of interest.

		// Use the divisions that have been provided.
		int [] parcelYearsForDiv = null;
		try {
			Message.printStatus(2, routine, "Determining parcel years for divisions: " + divListString );
			// TODO smalers 2021-02-21 need to evaluate whether to enable exclusion for bad data:
			// - modelers used work-arounds
			int [] excludeYears = new int[0];
			parcelYearsForDiv = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_array, excludeYears );
			for ( int i = 0; i < parcelYearsForDiv.length; i++ ) {
				Message.printStatus(2, routine, "Will process parcel year: " + parcelYearsForDiv[i] );
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

		// Loop through locations.
		int matchCount = 0;
		List<String> partIdList = null; // List of aggregate/system parts.
		// List of aggregate/system parts ID types (will contain "WDID" or "Receipt").
		List<StateCU_Location_CollectionPartIdType> partIdTypeList = null;
		//String collectionType = null;
		// Parts used for collection.  Mainly need to key on StateMod_WellStation.
		StateCU_Location_CollectionPartType collectionPartType = null;
		boolean isCollection = false;
		for ( int i = 0; i < culocListSize; i++ ) {
			culoc = culocList.get(i);
			culoc_id = culoc.getID();
			isCollection = culoc.isCollection();
			collectionPartType = null;
			if ( !culoc_id.matches(idpattern_Java) ) {
				// Identifier does not match.
				continue;
			}
			if ( isCollection ) {
				collectionPartType = culoc.getCollectionPartType();
			}
			++matchCount;

			crop_patterns = null; // Initialized because checked below.
			// The number of times that a crop value is set for this location.
			// If zero and a set is about to occur,
			// reset the value of the time series to reflect multiple calls to the command (with filling in between).
			crop_set_count = 0;

			// Check to see if the location is a simple diversion node,
			// an aggregate/system diversion, or a well aggregate/diversion, and process accordingly.

			try {
				notifyCommandProgressListeners ( i, culocListSize, (float)((i + 1)/((float)culocListSize)*100.0),
					"Processing CU location " + i + " of " + culocListSize );
				if ( !isCollection ) {
					// TODO SAM 2016-10-03 This will be an issue if an explicit well because don't have a way to indicate whether a ditch or well.
					// If single diversion.
					processing_ditches = true;
					Message.printStatus ( 2, routine, "Processing single diversion or well \"" + culoc_id +
						"\" using data from HydroBase vw_CDSS_StructureIrrigSummaryTS (parcels are not processed)." );
					if ( HydroBase_WaterDistrict.isWDID(culoc_id)) {
						// Read the data from summary time series.
						try {
							// Parse out the WDID.
							HydroBase_WaterDistrict.parseWDID(culoc_id,wdid_parts);
						}
						catch ( Exception e ) {
							// Should not happen because isWDID was checked above.
						}
						// The following returns HydroBase_StructureView.
						crop_patterns_sv = hbdmi.readStructureIrrigSummaryTSList (
							null, // InputFilter.
							null, // Order by clauses.
							DMIUtil.MISSING_INT, // Structure num.
							wdid_parts[0], // WD.
							wdid_parts[1], // ID.
							null, // Structure name.
							null, // Land use.
							InputStart_DateTime, // Year 1.
							InputEnd_DateTime, // Year 2.
							false ); // Distinct.
					}
					// Add supplemental records (works with WDID or not so can add data for single well):
					// - only the single location ID is used in the list
					culoc_wdids.clear();
					culoc_wdids.add ( culoc_id );
					//boolean todo = true;
					//if ( todo ) {
					//	throw new Exception("Need to fix code.");
					crop_patterns_sv = processor.readCropPatternTSFromSetForWDIDList (
						crop_patterns_sv, culoc_wdids, InputStart_DateTime, InputEnd_DateTime,
						hydroBaseStructureViewFromSetList,
						status, command_tag, warningLevel, warning_count);
					if ( crop_patterns_sv.size() == 0 ) {
						// Look forward in commands to see if any SetCropPatternTS commands are found that match the ID of interest:
						// - this does NOT check for setting individual parcels, which seems to be a Rio Grande thing and is being phased out.
						if ( locationHasSetCommand(processor, culoc_id) ) {
							// OK.
						}
						else {
							message = "CU location \"" + culoc_id + "\" had no crop pattern data from HydroBase or set commands.";
							Message.printWarning ( warningLevel,
					        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
					       	status.addToLog ( commandPhase,
					           	new CommandLogRecord(CommandStatusType.WARNING,
					            	message, "May be OK if period is short, but likely an issue if full period has been specified." ) );
						}
					}
					//}
					// The results are processed below.
					replace_flag = 0;
				}

				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.DITCH) ) {
					processing_ditches = true;
					Message.printStatus ( 2, routine, "Processing diversion aggregate/system \"" + culoc_id + "\"" );
					// Aggregate/system diversion.
					// Put together a list of WDIDs from the current CU location.
					// Currently ditch aggregate/systems are not allowed to vary over time so request the aggregate information for year 0.

					collection_ids = culoc.getCollectionPartIDsForYear(0);
					collection_size = 0;
					if ( collection_ids != null ) {
						collection_size = collection_ids.size();
					}
					culoc_wdids.clear();
					// This will contain the records for all the collection parts.
					crop_patterns_sv = new ArrayList<>();
					for ( int j = 0; j < collection_size; j++ ) {
						part_id = collection_ids.get(j);
						try {
							// Parse out the WDID.
							HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
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

						// Read from HydroBase.

						crop_patterns2 = hbdmi.readStructureIrrigSummaryTSList (
							null, // InputFilter.
							null, // Order by clauses.
							DMIUtil.MISSING_INT, // Structure num.
							wdid_parts[0], // WD.
							wdid_parts[1], // ID.
							null, // Structure name.
							null, // Land use.
							InputStart_DateTime, // Year 1.
							InputEnd_DateTime, // Year 2.
							false ); // Distinct.

						// Add to the list.
						if ( crop_patterns2 != null ) {
							crop_patterns_sv.addAll ( crop_patterns2 );
						}

						// Add to the list of WDIDs for supplemental data.
						culoc_wdids.add ( part_id );
					}

					// Add supplemental records.
					crop_patterns_sv = processor.readCropPatternTSFromSetForWDIDList (
						crop_patterns_sv, culoc_wdids,
						InputStart_DateTime, InputEnd_DateTime,
						hydroBaseStructureViewFromSetList,
						status, command_tag, warningLevel, warning_count );

					// First find the matching CropPatternTS and clear out the existing contents.
					/*
					TODO - SAM 2004-05-18 - why is this done?
					Comment out for now.

					pos = StateCU_Util.indexOf (__CUCropPatternTS_List, culoc_id);
					if ( pos >= 0 ) {
						__CUCropPatternTS_List.get(pos).removeAllTS();
					}
					*/

					if ( crop_patterns_sv.size() == 0 ) {
						if ( locationHasSetCommand(processor, culoc_id) ) {
							// OK.
						}
						else {
							message = "CU location \"" + culoc_id + "\" had no crop pattern data from HydroBase or set commands.";
							Message.printWarning ( warningLevel,
					        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
					       	status.addToLog ( commandPhase,
					           	new CommandLogRecord(CommandStatusType.WARNING,
					            	message, "May be OK if period is short, but likely an issue if full period has been specified." ) );
						}
					}

					// Process the records below into the collection ID.

					replace_flag = 1;	// 1 means add.
				}

				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.PARCEL) ) {
					// Well aggregate/system (read the individual parcels).
					processing_ditches = false;
					Message.printStatus ( 2, routine, "Processing well aggregate/system \"" + culoc_id + "\"" );
					message = "Using parcels to specify groundwater-only aggregation/system is being phased out - "
						+ "should specify aggregation using part type \"Well\" with well WDID and well permit receipt.";
					Message.printWarning ( warningLevel,
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( commandPhase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Change aggregation/system data from parcel to well part type." ) );

					// Clear the existing time series contents.
					/*
					TODO - SAM 2004-05-18 - why is this done?
					Comment out for now

					pos = StateCU_Util.indexOf (__CUCropPatternTS_List, culoc_id);
					if ( pos >= 0 ) {
						__CUCropPatternTS_List.get(pos).removeAllTS();
					}
					*/

					// Put together a list of parcel IDs from the current CU location.
					// The aggregate/systems are allowed to vary over time so only read the parcels for the specific years.

					collection_years = culoc.getCollectionYears();
					if ( collection_years == null ) {
						return;
					}
					// Loop over available collection years and read if in the requested input period.
					for ( int iy = 0; iy < collection_years.length; iy++ ) {
						// Get the parcel IDs for the year of interest.
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
									// Should not return anything from HydroBase.
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
								culoc.getCollectionDiv(),// Division.
								collection_ids_array, // Parcel ids.
								null, // Land use.
								null, // Irrig type.
								year_DateTime, // Collection year.
								year_DateTime );
						}
						else {
							// Collection year is not in the requested input period so don't read.
							continue;
						}

						// Process the records below into the collection ID.

						replace_flag = 1; // 1 means add.
						hsize = 0;
						if ( crop_patterns != null ) {
							hsize = crop_patterns.size();
						}
						Message.printStatus ( 2, routine, "For location " + culoc_id + " year=" +
							year_DateTime.getYear() + ", processing " + hsize + " well/parcel records" );

						for ( ih = 0; ih < hsize; ih++) {
							h_parcel = crop_patterns.get(ih);
							// Filter out lands that are not irrigated.
							irrig_type = h_parcel.getIrrig_type();
							// TODO SAM 2004-03-01 - don't want to hard-code strings but need to handle
							// revisions in HydroBaseDMI - ref_irrig_type should indicate whether irrigated.
							if ( irrig_type.equalsIgnoreCase("NA") ) {
								// Does not irrigate.
								continue;
							}
							// Need the following when one read command, then filling, then another read.
							if ( crop_set_count == 0 ) {
								// Reset all crops to missing for the year to prevent double-counting.
								resetCropPatternTS ( processor, cdsList, OutputStart_DateTime, OutputEnd_DateTime,
									culoc_id, h_parcel.getCal_year(), h_parcel.getCal_year() );
							}
							// Replace or add in the list.  Pass individual fields because may or may not
							// need to add a new StateCU_CropPatternTS or a time series in the object.
							StateCU_CropPatternTS cds = processor.findAndAddCUCropPatternTSValue (
									culoc_id, "" +
									h_parcel.getParcel_id(),
									h_parcel.getCal_year(),
									h_parcel.getParcel_id(),
									h_parcel.getLand_use(),
									h_parcel.getArea(),
									OutputStart_DateTime, // Output in case.
									OutputEnd_DateTime,	// New TS is needed.
									units, replace_flag );
							addToParcelYears ( h_parcel.getCal_year(), parcel_years );
							// Save the data for checks and filling based on parcel information.
							addParcelToCropPatternTS ( cds,
									"" + h_parcel.getParcel_id(),
									h_parcel.getCal_year(),
									h_parcel.getLand_use(),
									h_parcel.getArea(),
									units );
							++crop_set_count;
						}
					}
				}

				else if ( isCollection && (collectionPartType == StateCU_Location_CollectionPartType.WELL) ) {
					processing_ditches = false;
					Message.printStatus ( 2, routine, "Processing well aggregate/system \"" + culoc_id +
						"\" using list of WDID/permit receipt for parts." );
					// TODO smalers 2020-01-24 this code is not fully functional given migration to new
					// ReadCropPatternTSFromParcels command:
					// - zero results will likely be returned
					message = "CU location \"" + culoc_id + "\" is attempting to process well-only location, "
							+ "which is not supported by StateDMI 5.x+.  Results will likely be empty or inaccurate.";
					Message.printWarning ( warningLevel,
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( commandPhase,
			            new CommandLogRecord(CommandStatusType.FAILURE, message,
			            	"Use the ReadCropPatternTSFromParcels command instead." ) );
					// The collection definitions are the same each year (same list of wells throughout the period).
					// If the period is provided then the start and end are known.
					// However, don't want to requery data every year because that will be slow.
					// Therefore, determine the unique calendar years available from parcel data
					// in the requested period and process those years.

					// The aggregate/system information spans all years whereas the parcel associations are by year.
					// Get the list of parts.
					int parcelYear = -1; // Parcel year is irrelevant.
					partIdList = culoc.getCollectionPartIDsForYear(parcelYear);
					partIdTypeList = culoc.getCollectionPartIDTypes(); // Will not vary by year.
					// Loop through the parcel years determined for divisions of interest.
					for ( int iy = 0; iy < parcelYearsForDiv.length; iy++ ) {
						if ( ((InputStart_DateTime == null) ||(parcelYearsForDiv[iy] >= InputStart_DateTime.getYear())) &&
							((InputEnd_DateTime == null) || (parcelYearsForDiv[iy] <= InputEnd_DateTime.getYear()))) {
							// Get the parcels that are associated with the wells and then use the same logic as if processing parcels.
							List<HydroBase_Wells> hbWellsList = null; // List of HydroBase vw_CDSS_WellsWellToParcel records for location for all years.
							List<Integer> parcelListForYear = new ArrayList<>();
							int year = parcelYearsForDiv[iy];
							int [] parcelYearArray = new int[1]; // For one year
							// Loop through the well identifiers and read the parcels associated with the wells.
							// Get the vw_CDSS_WellsWellToParcel records, which tie well WDID and permit number to parcel.
							// This will give the list of parcels to process.
							int iPart = -1;
							StateCU_Location_CollectionPartIdType partIdType; // Part type for specified ID, indicates whether WDID or receipt.
							for ( String partId : partIdList ) {
								++iPart;
								partIdType = partIdTypeList.get(iPart);
								Message.printStatus ( 2, routine, "  Processing part ID \"" + partId + "\" part type " + partIdType + "." );
								// Get the well.
								if ( partIdType == StateCU_Location_CollectionPartIdType.WDID ) {
									// Read well to parcel data for WDID.
									// Split the WDID into parts in case it is not always 7 digits.
									int [] wdidParts = HydroBase_WaterDistrict.parseWDID(partId,null);
									hbWellsList = hbdmi.readWellsWellToParcelList(-1, year, -1, null, wdidParts[0], wdidParts[1]);
								}
								else if ( partIdType == StateCU_Location_CollectionPartIdType.RECEIPT ) {
									// Read well to parcel data for permit receipt.
									hbWellsList = hbdmi.readWellsWellToParcelList(-1, year, -1, partId, -1, -1);
								}
								Message.printStatus ( 2, routine, "    Found " + hbWellsList.size() + " matching well/parcel records for year " + year );
								// Loop through the output period and extract the parcel lists from the previously read list, ensuring unique parcel list.
								for ( DateTime dt = new DateTime(InputStart_DateTime); dt.lessThanOrEqualTo(InputEnd_DateTime); dt.addYear(1) ) {
									crop_set_count = 0; // Number of sets in the period.
									year = dt.getYear();
									parcelYearArray[0] = year;
									parcelListForYear.clear();
									for ( HydroBase_Wells hbwell: hbWellsList ) {
										if ( hbwell.getCal_year() == year ) {
											// Make sure that the parcel was not already added to the parcel list.
											boolean found = false;
											for ( Integer parcelForYear: parcelListForYear ) {
												if ( parcelForYear.equals(hbwell.getParcel_id()) ) {
													found = true;
													break;
												}
											}
											if ( !found ) {
												parcelListForYear.add(hbwell.getParcel_id());
											}
										}
									}
									//Message.printStatus ( 2, routine, "      Found " + parcelListForYear.size() + " matching well/parcel records for year " + year + "." );
									// The following logic is copied from above when processing a parcel list for collection.
									if ( parcelListForYear.size() > 0 ) {
										Message.printStatus ( 2, routine, "      Found " + parcelListForYear.size() + " matching well/parcel records for year " + year + "." );
										collection_ids_array = new int[parcelListForYear.size()];
										for ( int iParcel = 0; iParcel < parcelListForYear.size(); iParcel++ ) {
											collection_ids_array[iParcel] = parcelListForYear.get(iParcel);
										}
										crop_patterns = hbdmi.readParcelUseTSListForParcelList(
												// Division is irrelevant in newer HydroBase:
												// - irrigation year with parcel_id is unique
												-1,
												//culoc.getCollectionDiv(), // Division.
												collection_ids_array, // Parcel ids.
												null, // Land use.
												null, // Irrig type.
												dt, // Collection year.
												dt );
									}
									// Process the records below into the collection ID.

									replace_flag = 1; // 1 means add.
									hsize = 0;
									if ( crop_patterns != null ) {
										hsize = crop_patterns.size();
									}
									if ( hsize > 0 ) {
										Message.printStatus ( 2, routine, "        For location " + culoc_id + " year=" +
											year + ", processing " + hsize + " well/parcel records" );
									}
									for ( ih = 0; ih < hsize; ih++) {
										h_parcel = crop_patterns.get(ih);
										// Filter out lands that are not irrigated.
										irrig_type = h_parcel.getIrrig_type();
										// TODO SAM 2004-03-01 - don't want to hard-code strings but need to handle
										// revisions in HydroBaseDMI - ref_irrig_type should indicate whether irrigated.
										if ( irrig_type.equalsIgnoreCase("NA") ) {
											// Does not irrigate.
											continue;
										}
										// Need the following when one read command, then filling, then another read.
										if ( crop_set_count == 0 ) {
											// Reset all crops to missing for the year to prevent double-counting.
											resetCropPatternTS ( processor, cdsList, OutputStart_DateTime, OutputEnd_DateTime,
												culoc_id, h_parcel.getCal_year(), h_parcel.getCal_year() );
										}
										// Replace or add in the list.  Pass individual fields because may or may not
										// need to add a new StateCU_CropPatternTS or a time series in the object.
										StateCU_CropPatternTS cds = processor.findAndAddCUCropPatternTSValue (
												culoc_id, "" +
												h_parcel.getParcel_id(),
												h_parcel.getCal_year(),
												h_parcel.getParcel_id(),
												h_parcel.getLand_use(),
												h_parcel.getArea(),
												OutputStart_DateTime, // Output in case.
												OutputEnd_DateTime,	// New TS is needed.
												units, replace_flag );
										addToParcelYears ( h_parcel.getCal_year(), parcelYearArray );
										// Save the data for checks and filling based on parcel information.
										addParcelToCropPatternTS ( cds,
												"" + h_parcel.getParcel_id(),
												h_parcel.getCal_year(),
												h_parcel.getLand_use(),
												h_parcel.getArea(),
												units );
										++crop_set_count;
									}
								}
							}
						}
					}
				}

				else if ( isCollection ) {
					message = "CU Location \"" + culoc_id + "\" collection type \"" + culoc.getCollectionType() + " - software does not yet handle.";
					Message.printWarning ( warningLevel,
				        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( commandPhase,
			            new CommandLogRecord(CommandStatusType.FAILURE, message,
			            	"Check the log file - report to software support if necessary." ) );
				}

				// If here, a list of HydroBase_StructureView objects is defined for the CU
				// Location and can be added to the StateCU_CropPatternTS data.
				// If an aggregate, the aggregation is done above.

				// Loop through the HydroBase objects and add new StateCU_CropPatternTS instances for each instance.

				hsize = 0;
				if ( crop_patterns_sv != null ) {
					hsize = crop_patterns_sv.size();
				}
				Message.printStatus ( 2, routine, "Processing " + hsize + " records" );

				if ( processing_ditches ) {
					for ( ih = 0; ih < hsize; ih++) {
						h_cds = crop_patterns_sv.get(ih);
						// Need the following when one read command, then filling, then another read.
						if ( crop_set_count == 0 ) {
							// Reset all crops to missing for the year to prevent double-counting...
							resetCropPatternTS ( processor, cdsList, OutputStart_DateTime, OutputEnd_DateTime,
								culoc_id, h_cds.getCal_year(), h_cds.getCal_year() );
						}
						// Replace or add in the list.  Pass individual fields because may or may
						// not need to add a new StateCU_CropPatternTS or a time series in the object.
						StateCU_CropPatternTS cds = processor.findAndAddCUCropPatternTSValue (
							culoc_id, "" +
							//h_cds.getStructure_id(),
							// TODO SAM 2005-05-26 what width on the WDID?
							HydroBase_WaterDistrict.formWDID(h_cds.getWD(),
							h_cds.getID()),
							h_cds.getCal_year(),
							-1,		// No individual parcel IDs for ditches.
							h_cds.getLand_use(),
							h_cds.getAcres_total(),	// Total for irrigation method.
							OutputStart_DateTime,  // Output in case.
							OutputEnd_DateTime,	// New TS needed.
							units, replace_flag );
						addToParcelYears ( h_cds.getCal_year(), parcel_years );
						// Save data for use in checks and filling (does not increment acreage).
						addParcelToCropPatternTS ( cds,
							"" + h_cds.getID(),
							h_cds.getCal_year(),
							h_cds.getLand_use(),
							h_cds.getAcres_total(),	// Total for irrigation method.
							units );
						++crop_set_count;
					}
				}
				// Else, well data was transferred above.
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

		// The above code edited individual values in time series.
		// Loop through now and make sure that the totals are up to date.

		int size = cdsList.size();
		StateCU_CropPatternTS cds2;
		StringBuffer parcel_years_string = new StringBuffer();
		for ( int iyear = 0; iyear < parcel_years.length; iyear++ ) {
			if ( parcel_years[iyear] < 0 ) {
				// Done processing years.
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
			// Finally, if a crop pattern value is set in any year,
			// assume that all other missing values should be treated as zero.
			// If all data are missing, including no crops, the total should be set to zero.
			// In other words, crop patterns for a year must include all crops
			// and filling should not occur in a year when data values have been set.
			for ( int iyear = 0; iyear < parcel_years.length; iyear++ ) {
				if ( parcel_years[iyear] < 0 ) {
					// Done processing years.
					break;
				}
				cds2.setCropAreasToZero (
					parcel_years[iyear], // Specific year to process.
					false );// Only set missing to zero (leave non-missing as is).
			}
			// Recalculate totals for the location.
			cds2.refresh ();
		}

		// Warn about identifiers that have been replaced in the __CUCropPatternTS_List.

		processor.warnAboutDataMatches ( this, true,
			processor.getStateCUCropPatternTSMatchList(), "CU Crop Pattern TS values" );

		/* TODO SAM 2004-03-12 - need to store data when read from HydroBase.
		// TODO - need to merge these for multiple years, using the div and year.
		// It may be possible that an old year is read from HydroBase and a new year from a draft DBF file.

		// Save the list of parcel use time series in case they are needed to process wells, etc.

		__CUParcelUseTS_List = parcelusets_List();
		*/
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
public String toString ( PropList parameters ) {
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String ID = parameters.getValue ( "ID" );
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue( "InputEnd" );
	String Div = parameters.getValue( "Div" );
	String SetFlag = parameters.getValue ( "SetFlag" );
	String SetFlagDescription = parameters.getValue ( "SetFlagDescription" );
	String DataFrom = parameters.getValue( "DataFrom" ); // Experimental.
	String AreaPrecision = parameters.getValue( "AreaPrecision" ); // Experimental.

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
	if ( SetFlag != null && SetFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetFlag=\"" + SetFlag + "\"" );
	}
	if ( SetFlagDescription != null && SetFlagDescription.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetFlagDescription=\"" + SetFlagDescription + "\"" );
	}
	if ( DataFrom != null && DataFrom.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DataFrom=" + DataFrom );
	}
	if ( AreaPrecision != null && AreaPrecision.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AreaPrecision=" + AreaPrecision );
	}

	return getCommandName() + "(" + b.toString() + ")";
}

}