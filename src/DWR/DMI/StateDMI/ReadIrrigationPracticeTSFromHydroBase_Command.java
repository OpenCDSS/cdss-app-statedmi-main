// ReadIrrigationPracticeTSFromHydroBase_Command - This class initializes, checks, and runs the ReadIrrigationPracticeTSFromHydroBase() command.

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
import DWR.DMI.HydroBaseDMI.HydroBaseDataStore;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTSStructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Location_CollectionPartType;
import DWR.StateCU.StateCU_Location_CollectionType;
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
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import cdss.dmi.hydrobase.rest.ColoradoHydroBaseRestDataStore;
import riverside.datastore.DataStore;

/**
See also the ReadIrrigationPracticeTSFromParcels command, which is newer and should be used.

This class initializes, checks, and runs the ReadIrrigationPracticeTSFromHydroBase() command.
Irrigation practice acreage time series data will be completely defined for years with HydroBase data.
NO ADJUSTMENT of data occurs after the read - it is simply set in the results.
NO CHECK of total acreage is done since it is expected that the crop pattern time series
total acreage is set after reading from HydroBase.
*/
public class ReadIrrigationPracticeTSFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "False";
protected final String _True = "True";

// Possible values for SupplyType...

protected final String _Ground = "Ground";
protected final String _Surface = "Surface";

/**
Optimization parameter values.
*/
protected final String _UseMoreMemory = "UseMoreMemory";
protected final String _UseLessMemory = "UseLessMemory";

// Year parameter as list of years to process, or null.  Set in
// checkCommandParameters() for use in runCommand().
private int [] __Year_int = null;	// Default = process all years

/**
Constructor.
*/
public ReadIrrigationPracticeTSFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadIrrigationPracticeTSFromHydroBase" );
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
	String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String Optimization = parameters.getValue( "Optimization" );
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
	
	if ( (Year != null) && (Year.length() > 0) ) {
		List<String> v = StringUtil.breakStringList ( Year, ",", StringUtil.DELIM_SKIP_BLANKS );
		int size = 0;
		if ( v != null ) {
			size = v.size();
		}
		if ( size == 0 ) {
			__Year_int = null;
		}
		else {
			__Year_int = new int[size];
		}
		for ( int i = 0; i < size; i++ ) {
			String token = v.get(i);
			if ( !StringUtil.isInteger(token) ) {
				message = "Year (" + token + ") is not a valid integer.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer." ) );
			}
			else {
				__Year_int[i] = StringUtil.atoi(token);
			}
		}
	}
	
	if ( (Div == null) || (Div.length() == 0) ) {
		message = "The division must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division for data." ) );
	}
	else if ( !StringUtil.isInteger(Div) ) {
		message = "The division is not a valid integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division as an integer." ) );
	}
	
	if ( (Optimization != null) && (Optimization.length() > 0) &&
		!Optimization.equalsIgnoreCase(_UseLessMemory) && !Optimization.equalsIgnoreCase(_UseMoreMemory) ) {
		message = "The Optimization value (" + Optimization + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify Optimization as " + _UseLessMemory + " or " + _UseMoreMemory +
				" (default).") );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(5);
    validList.add ( "DataStore" );
    validList.add ( "ID" );
    validList.add ( "Year" );
    validList.add ( "Div" );
    validList.add ( "Optimization" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
		throw new InvalidCommandParameterException ( warning );
	}
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Determine whether a parcel has well supply by querying the HydroBase
WellsWellToParcel view.  It is assumed that if a record exists, then the
parcel has supply; however, a check of whether water rights, etc., are valid
is not done.  This is as per Ray Bennett instruction of 2007-06-06.
@param hdmi HydroBaseDMI instance for queries.
@param id Identifier for location being processed.
@param part_id Identifier for part identifier being processed (when a collection),
null if not a collection.
@param parcel_year Year for parcel data.
@param parcel_id Identifier for parcel.
@param Div_int Water division for data.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
@return true if the parcel has well supply, false if not.
*/
private boolean doesHydroBaseParcelHaveGroundWaterSupply (
		HydroBaseDMI hdmi, String id, String part_id,
		int parcel_year, int parcel_id, int Div_int,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = "ReadIrrigationPracticeTSFromHydroBase.doesParcelHaveGroundwaterSupply";
	String message;
	List<HydroBase_Wells> hbwellParcelList = null;
	try {
		// Get the well/welltoparcels associated with the parcel...
		hbwellParcelList = hdmi.readWellsWellToParcelList(parcel_id, parcel_year, Div_int );
	}
	catch ( Exception e ) {
		// Should not happen
		Message.printWarning ( 3, routine, e );
		if ( part_id == null ) {
			// Processing groundwater only parcel...
			message = "Unexpected error getting well to parcel data from HydroBase for " + id +
			" parcel=" +parcel_id + " year=" + parcel_year + " division=" + Div_int + "(" + e + ").";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
		else {
			// Processing WDID part of a D&W aggregation...
			message = "Error getting well to parcel data from HydroBase for " +id +
				" (part " + part_id + ") parcel=" + parcel_id + " year=" + parcel_year + " Div=" + Div_int;
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
		return false;
	}
	int nwell_parcel = 0;
	if ( hbwellParcelList != null ) {
		nwell_parcel = hbwellParcelList.size();
	}
	if ( nwell_parcel > 0 ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ReadIrrigationPracticeTSFromHydroBase_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.  
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = 
		getCommandName() + "_Command.parseCommand",
		message;

	List<String> tokens = StringUtil.breakStringList ( command, "()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name, diversion ID
		message = "Syntax error in \"" + command +
		"\".  Not enough tokens.";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the file...
	try {
		setCommandParameters ( PropList.parse ( Prop.SET_FROM_PERSISTENT,
			tokens.get(1), routine, "," ) );
	}
	catch ( Exception e ) {
		message = "Syntax error in \"" + command +
		"\".  Not enough tokens.";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
}

/**
Read a single parcel from HydroBase as a HydroBase_ParcelUseTS, given a
parcel identifier, year, and water division.  Process the resulting data into
the associated StateCU_IrrigationPracticeTS.
@param hbdmi HydroBaseDMI instance for database queries.
@param culoc The StateCU_Location being processed.
@param is_ditch Indicates if the CU location is a ditch.
@param ipyts The StateCU_IrrigationPracticeTS associated with the location.
@param id The identifier for the model location associated with the parcel.
@param part_id The part identifier when a collection part is being processed, null if not a collection.
@param parcel_id Parcel identifier.
@param parcel_year Parcel year.
@param Div_int water division as integer.
@param fraction_irrig Fraction (0 to 1) of the parcel irrigated by the location.
If a ditch this may be <= 1.  If a well, it will be 1.0.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForParcel (
		HydroBaseDMI hdmi,
		ColoradoHydroBaseRestDataStore datastore,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String part_id,
		int parcel_id,
		int parcel_year,
		int Div_int,
		double fraction_irrig,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getSimpleName() + ".readHydroBaseIrrigationPracticeTSForParcel";
	String message;
	List<HydroBase_ParcelUseTS> hbparcelList = null;
	try {
		// If datastore get the parcel use ts list from web services and
		// then convert to HydroBase_ParcelUseTS objects and add to hbparcelList.
		if( datastore != null ){
			// TODO smalers 2019-06-26 need to re-enable
			// - this does not work because the web service does not allow a direct query on year and parcel ID
			//hbparcelList = new ArrayList<HydroBase_ParcelUseTS>();
			//List<ParcelUseTimeSeries> hbrparcelList = null;
			//hbrparcelList = datastore.readParcelUseTSListForParcelId(part_id, parcel_id);
			//for(int i = 0; i < hbrparcelList.size(); i++){
			//	HydroBase_ParcelUseTS hbpTS = HydroBaseRestToolkit.getInstance().toHydroBaseParcelUseTS(hbrparcelList.get(i));
			//	hbparcelList.add(hbpTS);
			//}
			message = "Reading parcel use TS for parcel ID (ID=" + parcel_id + ") is not supported for web services.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Use a local database to process data." ) );
			return warning_count;
		}
		else {
			// Read from HydroBase database
			hbparcelList = hdmi.readParcelUseTSList (
					parcel_year,
					Div_int,
					parcel_id );
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error getting parcel data from HydroBase for " + id +
			" parcel=" +parcel_id + " year=" + parcel_year + " division=" + Div_int;
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
		return warning_count;
	}

	int nhbparcel = 0;
	if ( hbparcelList != null ) {
		nhbparcel =	hbparcelList.size();
	}
	// Save parcel information on the well station so that data check output can be output later.
	// Should only be one parcel...
	if ( nhbparcel == 0 ) {
		return warning_count;
	}
	
	if ( nhbparcel != 1 ) {
		// Should not happen
		message = "Multiple parcel data records exist in HydroBase for " + id +
				" parcel=" + parcel_id + " year=" + parcel_year + " division=" + Div_int;
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support - HydroBase data error." ) );
		return warning_count;
	}

	HydroBase_ParcelUseTS hbparcel = hbparcelList.get(0);
	
	double area = hbparcel.getArea()*fraction_irrig;	// Acres
	String crop = hbparcel.getLand_use();
	String irrig_method = hbparcel.getIrrig_type();
	// Determine whether the parcel has groundwater supply simply by trying to read the
	// WellsWellToParcel record...
	boolean has_gw_supply = doesHydroBaseParcelHaveGroundWaterSupply (
			hdmi, id, part_id, parcel_year, parcel_id, Div_int,
			warningLevel, warning_count, command_tag, status, cacheHydroBase );
	
	// Now process the individual data parts...
	
	StateDMI_Util.processIrrigationPracticeTSParcel (
			id,
			ipyts,
			parcel_id,
			crop,
			area,
			irrig_method,
			parcel_year,
			has_gw_supply );
	
	return warning_count;
}

/**
Read HydroBase irrigation practice time series data, for a
list of parcels.  The parcels can come from a groundwater-only aggregation
or a D&W location where groundwater supplements surface water supply.
@param hdmi HydroBaseDMI instance for database queries.
@param culoc StateCU_Location to process data.
@param Loctype Location type for messages.
@param id Location identifier.
@param parcel_ids A list of parcel identifiers.
@param fraction_irrig the fraction (0.0 to 1.0) of the parcel irrigated by a ditch
for the year.  This is only used when the calling code is processing
locations with surface and groundwater supply.  Specify null or an array
of 1.0 when processing groundwater only.
@param parcel_year The calendar year for which the parcels apply.
@param String Div Division for data.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForParcelList (
		HydroBaseDMI hdmi,
		ColoradoHydroBaseRestDataStore datastore,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String Loctype,
		List<String> parcel_ids,
		double [] fraction_irrig,
		String part_id,
		int parcel_year,
		int Div_int,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getSimpleName() + ".readHydroBaseIrrigationPracticeTSForParcelList";
	// Loop through each parcel and append the HydroBase rights associated with each parcel

	int nparcel = 0;
	if ( parcel_ids != null ) {
		nparcel = parcel_ids.size();
	}
	Message.printStatus ( 2, routine, Loctype + " \"" + id + "\" year=" + parcel_year + " division=" +
		Div_int + " irrigates " + nparcel + " parcels" );
	
	// Get the wells associated with the parcels.
	
	int parcel_id;	// Specific parcel identifier to process
	double fraction_irrig2;	// Fraction of parcel (0.0 to 1.0) that is irrigated by ditch, after defaulting if null
	// if datastore read the parcel use ts list
	if(datastore != null){
		datastore.readParcelUseTSList(part_id);
	}
	for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
		parcel_id = Integer.parseInt(parcel_ids.get(iparcel) );
		// Process the HydroBase parcel data (values will be added to the
		// irrigation practice time series)...
		if ( fraction_irrig == null ) {
			fraction_irrig2 = 1.0;
		}
		else {
			fraction_irrig2 = fraction_irrig[iparcel];
		}
		warning_count = readHydroBaseIrrigationPracticeTSForParcel (
				hdmi,
				datastore,
				culoc,
				ipyts,
				id,
				part_id,	// Will be null for gw-only parcel collections
				parcel_id,
				parcel_year,
				Div_int,
				fraction_irrig2,
				warningLevel, warning_count, command_tag, status, cacheHydroBase );
	}
	return warning_count;
}

/**
Read HydroBase irrigation practice TS for a
list of diversions (WDIDs and not).  The parcels are associated with
a D&W location where groundwater supplements surface water supply.
@param hdmi HydroBaseDMI instance for database queries.
@param Loctype Location type for messages.
@param id Location identifier.
@param parcel_ids A list of parcel identifiers.
@param parcel_year The calendar year for which the parcels apply.
@param Div_int Division for data.
@param HydroBase_Supplemental_ParcelUseTS_List A list of StateDMI_HydroBase_ParcelUseTS to be appended to as
data are read for the well list.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForDiversionList (
		HydroBaseDMI hdmi,
		ColoradoHydroBaseRestDataStore datastore,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String Loctype,
		List<String> wdids,
		int parcel_year,
		int Div_int,
		List<StateDMI_HydroBase_ParcelUseTS> HydroBase_Supplemental_ParcelUseTS_List,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getSimpleName() + ".readHydroBaseIrrigationPracticeTSForLocationList";
	String message;

	// Loop through each location and for each WDID part get the parcel list.
	// Then call other methods to read the wells for the parcels, using the
	// same approach as for well-only supply parcels.

	int nwdids = 0;
	if ( wdids != null ) {
		nwdids = wdids.size();
	}
	String part_id;	// single WDID
	int [] wdid_parts = new int[2];
	HydroBase_StructureView hbdiv = null;	// Individual ditch
	List<HydroBase_ParcelUseTSStructureToParcel> hbparcelStructureList = null;//Structure/parcel join data
	boolean have_structure_num = false;
	boolean is_wdid = false;	// Whether a WDID
	for ( int iparts = 0; iparts < nwdids; iparts++ ) {
		part_id = wdids.get(iparts);
		is_wdid = HydroBase_WaterDistrict.isWDID(part_id);
		if ( is_wdid ) {
			try {
				// Parse out the WDID...
				HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
			}
			catch ( Exception e ) {
				if ( (nwdids == 1) && !HydroBase_WaterDistrict.isWDID(part_id)) {
					// The diversion is a single diversion that is not a WDID.
					// Therefore a warning is not needed...
					Message.printStatus ( 2, routine, "Diversion \"" + id +
					"\" is not a WDID.  Cannot read wells from HydroBase." );
				}
				else {
					// Not a WDID - this is an error because valid structures are
					// expected as parts of an aggregate...
					message = "Diversion \"" + id + "\" (part " + part_id + ") is not a WDID.  Cannot " +
						"read parcel data from HydroBase.";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that aggregate part is a valid WDID." ) );
				}
				// In any case cannot process the data...
				continue;
			}
		}
		have_structure_num = false;
		if ( is_wdid ) {
			try {
				// Get the structure so the structure number can be retrieved...
				hbdiv = hdmi.readStructureViewForWDID( wdid_parts[0], wdid_parts[1] );// parts determined above
				have_structure_num = true;
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error getting structure data from HydroBase for " + id + " (part " +
					part_id + ")(" + e + ")";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report to software support." ) );
				have_structure_num = false;
			}
			// Verify that the division for the WDID is in the requested division.
			int wdDiv = HydroBase_Util.lookupDivisionForWaterDistrict(hdmi, wdid_parts[0]);
			if ( wdDiv != Div_int ) {
				message = "Location " + id + " (part " + part_id + ") has WDID in division " + wdDiv +
					", which is different from the requested division " + Div_int +
					" - results will not be correct - skipping location.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that all WDID identifiers used by the command are for the specified " +
							"division and that structures do not span divisions." ) );
				continue;
			}
			if ( hbdiv == null ) {
				message = "No structure data from HydroBase for " + id + " (part " + part_id + ").";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the structure has data in HydroBase before including as a part." ) );
				have_structure_num = false;
			}
		}
		if ( have_structure_num ) {
			try {
				// Get the parcels that the ditch irrigates for the specific year...
				hbparcelStructureList = hdmi.readParcelUseTSStructureToParcelListForStructure_numCal_year(
				hbdiv.getStructure_num(), parcel_year );
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error getting structure to parcel data from HydroBase for " + id +
					" (part " + part_id + ")";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report to software support." ) );
				continue;
			}
			
			int nparcel = 0;
			if ( hbparcelStructureList != null ) {
				nparcel = hbparcelStructureList.size();
			}
		
			Message.printStatus ( 2, routine, "Diversion \"" + id + "\" (part " + part_id +
				") year=" + parcel_year + " Div=" + Div_int + " irrigates " + nparcel + " parcels" );

			// Put together a list of parcel identifiers...
		
			List<String> parcel_ids = new ArrayList<String>(nparcel);
			double [] fraction_irrig = new double[nparcel];
		
			HydroBase_ParcelUseTSStructureToParcel hbparcel_structure;
			for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
				hbparcel_structure = hbparcelStructureList.get(iparcel);
				parcel_ids.add ( "" + hbparcel_structure.getParcel_id() );
				// percent_irrig in HydroBase is actually a fraction
				fraction_irrig[iparcel] = hbparcel_structure.getPercent_irrig();
			}
			// Loop through each parcel and append the HydroBase irrigation practice
			// data associated with each parcel
			warning_count = readHydroBaseIrrigationPracticeTSForParcelList (
				hdmi,
				datastore,
				culoc,
				ipyts,
				id,
				Loctype,
				parcel_ids,
				fraction_irrig,	// fraction_irrig for each parcel in list
				part_id,		// Ditch part that is being processed
				parcel_year,
				Div_int,
				warningLevel, warning_count, command_tag, status, cacheHydroBase );
		}
		
		// Now read the supplemental data provided with
		// commands like setIrrigationPracticeTSFromList()...
		/* FIXME SAM 2007-10-18 Need to convert to a check to make sure that
		 * readIrrigationPracticeTSFromList() does not duplicate data from
		 * HydroBase.
		readSupplementalParcelUseTSListForLocation (
			HydroBase_Supplemental_ParcelUseTS_List,
			culoc, ipyts, part_id, parcel_year );
			*/
	} // End loop on location part IDs.
	return warning_count;
}

/**
Read (find) supplemental data for a location, which are essentially data
that were not in HydroBase but need to be considered, and process the data.
@return the updated list of data, including the supplemental data.
@param id The identifier for the part.
*/
/*
private void readSupplementalParcelUseTSListForLocation (
		List HydroBase_Supplemental_ParcelUseTS_List,
		StateCU_Location culoc, StateCU_IrrigationPracticeTS ipyts,
		String id, int parcel_year )
{	String routine = "readIrrigationPracticeTSFromHydroBase_Command.readSupplementalParcelUseTSListForWDID";
	int size = HydroBase_Supplemental_ParcelUseTS_List.size();
	StateDMI_HydroBase_ParcelUseTS puts = null;
	boolean has_gw_supply = false;
	for ( int i = 0; i < size; i++ ) {
		puts = (StateDMI_HydroBase_ParcelUseTS)
		HydroBase_Supplemental_ParcelUseTS_List.elementAt(i);
		if ( puts.getLocationID().equalsIgnoreCase(id) &&
				(puts.getCal_year() == parcel_year) ) {
			// Found a matching parcel so process it...
			// Check the supply type provided by the user.
			has_gw_supply = false;
			if ( puts.getSupply_type().equalsIgnoreCase(_Ground)) {
				has_gw_supply = true;
			}
			Message.printStatus ( 2, routine,
					"Processing supplemental data for ID=" + id +
					" Year=" + parcel_year +
					// Not required for IPY processing...
					//" CropType=" + puts.getLand_use() +
					" IrrigationMethod=" + puts.getIrrig_type() +
					" Area=" + puts.getArea() +
					" HasGWSupply=" + has_gw_supply );
			StateDMI_Util.processIrrigationPracticeTSParcel (
					id,
					ipyts,
					-1,		// No parcel ID
					puts.getLand_use(),
					puts.getArea(),
					puts.getIrrig_type(),
					parcel_year,
					has_gw_supply );
		}
	}
}
*/

/**
Read HydroBase irrigation practice TS for a
list of wells (WDIDs and receipts).  The parcels are associated with
a well location where groundwater supply is the only supply
(otherwise a D&W via diversion code would be used).
This code is now similar to reading diversions given that the parts are often WDIDs.
@param hdmi HydroBaseDMI instance for direct database queries.
@param datastore datastore instance for web service queries.
@param culoc StateCU_Location object being processed
@param ipyts IrrigationPracticeTS to be filled with data for the specific year.
@param id StateCU_Location identifier for the location.
@param Loctype Location type for messages.
@param partIds A list of well identifiers (see also partIdTypes).
@param partIdTypes A list of well identifiers types to indicate how to interpret partIds, can be WDIDs or Receipt.
@param parcel_year The calendar year for which the parcels apply.
@param Div_int Division for data.
@param HydroBase_Supplemental_ParcelUseTS_List A list of StateDMI_HydroBase_ParcelUseTS to be appended to as
data are read for the well list.
@param warningLevel level to use for logging
@param warning_count count of warnings, for error handling
@param command_tag used for logging to allow navigation of messages
@param status command status to receive logging messages
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForWellList (
		HydroBaseDMI hdmi,
		ColoradoHydroBaseRestDataStore datastore,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String Loctype,
		List<String> partIds,
		List<StateCU_Location_CollectionPartIdType> partIdTypes,
		int parcel_year,
		int Div_int,
		List<StateDMI_HydroBase_ParcelUseTS> HydroBase_Supplemental_ParcelUseTS_List,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getSimpleName() + ".readHydroBaseIrrigationPracticeTSForWellList";
	String message;

	// Loop through each location and for each WDID part get the parcel list.
	// Then call other methods to read the wells for the parcels, using the
	// same approach as for well-only supply parcels.

	int nPartIds = 0;
	if ( partIds != null ) {
		nPartIds = partIds.size();
	}
	String partId; // single well ID
	int [] wdidParts = new int[2];
	HydroBase_StructureView hbwell = null; // Individual well
	List<HydroBase_ParcelUseTSStructureToParcel> hbparcelStructureList = null;//Structure/parcel join data
	boolean have_structure_num = false;
	boolean is_wdid = false;	// Whether a WDID
	StateCU_Location_CollectionPartIdType partIdType = null;
	for ( int iparts = 0; iparts < nPartIds; iparts++ ) {
		partId = partIds.get(iparts);
		partIdType = partIdTypes.get(iparts);
		// Size the parcels to be empty, will be filled in later based on WDID or Receipt data
		List<String> parcel_ids = new ArrayList<>();
		// Fraction of parcel irrigated by the source
		double [] fraction_irrig = new double[0];
		if ( partIdType == StateCU_Location_CollectionPartIdType.WDID ) {
			// Part type is supposed to be a WDID
			// - will try to parse the WDID below to confirm
			is_wdid = true;
		}
		// WDID is used for diversion structures to connect to parcels irrigated
		// - this goes through structure table
		// - for wells, need to go through the wells table
		// - therefore, disable the following code because it will always return no records
		// - leave the code in in case HydroBase does at some point switch to this
		boolean doWdidLikeDiversions = false; // See the next code block for what is typically done for wells.
		if ( is_wdid && doWdidLikeDiversions ) {
			try {
				// Parse out the WDID...
				HydroBase_WaterDistrict.parseWDID(partId,wdidParts);
			}
			catch ( Exception e ) {
				if ( (nPartIds == 1) && !HydroBase_WaterDistrict.isWDID(partId) ) {
					// The well is a single diversion that is not a WDID.
					// Therefore a warning is not needed...
					Message.printStatus ( 2, routine, "Well \"" + id +
					"\" is not a WDID.  Cannot read wells from HydroBase." );
				}
				else {
					// Not a WDID - this is an error because valid structures are
					// expected as parts of an aggregate...
					message = "Well \"" + id + "\" (part " + partId + ") is not a WDID.  Cannot " +
						"read parcel data from HydroBase.";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that aggregate part is a valid WDID." ) );
				}
				// In any case cannot process the data...
				continue;
			}
			have_structure_num = false;
			try {
				// Get the structure so the structure number can be retrieved...
				hbwell = hdmi.readStructureViewForWDID( wdidParts[0], wdidParts[1] );// parts determined above
				have_structure_num = true;
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error getting structure data from HydroBase for well " + id + " (part " +
					partId + ")(" + e + ")";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report to software support." ) );
				have_structure_num = false;
			}
			// Verify that the division for the WDID is in the requested division.
			int wdDiv = HydroBase_Util.lookupDivisionForWaterDistrict(hdmi, wdidParts[0]);
			if ( wdDiv != Div_int ) {
				message = "Location " + id + " (part " + partId + ") has WDID in division " + wdDiv +
					", which is different from the requested division " + Div_int +
					" - results will not be correct - skipping location.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that all WDID identifiers used by the command are for the specified " +
							"division and that structures do not span divisions." ) );
				continue;
			}
			if ( hbwell == null ) {
				message = "No structure data from HydroBase for well " + id + " (part " + partId + ").";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the structure has data in HydroBase before including as a part." ) );
				have_structure_num = false;
			}
		    if ( have_structure_num ) {
		    	// Have a structure number for the part so can continue
			    try {
				    // Get the parcels that the well irrigates for the specific year...
				    hbparcelStructureList = hdmi.readParcelUseTSStructureToParcelListForStructure_numCal_year(
				    hbwell.getStructure_num(), parcel_year );
			    }
			    catch ( Exception e ) {
				    Message.printWarning ( 3, routine, e );
				    message = "Unexpected error getting structure to parcel data from HydroBase for " + id +
					    " (part " + partId + ")";
				    Message.printWarning(warningLevel,
					    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				    status.addToLog ( CommandPhaseType.RUN,
					    new CommandLogRecord(CommandStatusType.FAILURE,
						    message, "Report to software support." ) );
				    continue;
			    }
			    
			    int nparcel = 0;
			    if ( hbparcelStructureList != null ) {
				    nparcel = hbparcelStructureList.size();
		    
			        Message.printStatus ( 2, routine, "Well \"" + id + "\" (part WDID " + partId +
				        ") year=" + parcel_year + " Division=" + Div_int + " irrigates " + nparcel + " parcels" );

			        // Put together a list of parcel identifiers...
		
			        parcel_ids = new ArrayList<String>(nparcel);
			        fraction_irrig = new double[nparcel];
		
			        HydroBase_ParcelUseTSStructureToParcel hbparcel_structure;
			        for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
				        hbparcel_structure = hbparcelStructureList.get(iparcel);
				        parcel_ids.add ( "" + hbparcel_structure.getParcel_id() );
				        fraction_irrig[iparcel] = hbparcel_structure.getPercent_irrig();
			        }
			    }
		    }
	    } // End is WDID
		else if ( is_wdid ) {
			// WDID - get the parcels irrigated for the well
			// - go through the wells rather than handling like diversion record
			try {
				// Parse out the WDID...
				HydroBase_WaterDistrict.parseWDID(partId,wdidParts);
			}
			catch ( Exception e ) {
				// Not a WDID - this is an error because valid structures are
				// expected as parts of an aggregate...
				message = "Well \"" + id + "\" (part " + partId + ") is not a WDID.  Cannot " +
					"read parcel data from HydroBase.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that aggregate part is a valid WDID." ) );
				// In any case cannot process the data...
				continue;
			}
			try {
				List<HydroBase_Wells> hbWellParcelList = hdmi.readWellsWellToParcelList( -1, parcel_year, -1, null, wdidParts[0], wdidParts[1] );
				int nparcel = 0;
				if ( hbWellParcelList != null ) {
					nparcel = hbWellParcelList.size();
				}
		        Message.printStatus ( 2, routine, "Well \"" + id + "\" (part WDID " + partId +
			        ") year=" + parcel_year + " Division=" + Div_int + " irrigates " + nparcel + " parcels" );
		        HydroBase_Wells hbwells;
		        fraction_irrig = new double[nparcel];
		        for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
			        hbwells = hbWellParcelList.get(iparcel);
			        parcel_ids.add ( "" + hbwells.getParcel_id() );
			        fraction_irrig[iparcel] = 1.0; // Well irrigates entire parcel
		        }
			}
			catch ( Exception e ) {
				    Message.printWarning ( 3, routine, e );
				    message = "Unexpected error getting well receipt to parcel data from HydroBase for " + id +
					    " (part " + partId + ")";
				    Message.printWarning(warningLevel,
					    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				    status.addToLog ( CommandPhaseType.RUN,
					    new CommandLogRecord(CommandStatusType.FAILURE,
						    message, "Report to software support." ) );
				    continue;
			}
		}
		else {
			// Receipt - get the parcels irrigated for the well
			try {
				List<HydroBase_Wells> hbWellParcelList = hdmi.readWellsWellToParcelList( -1, parcel_year, -1, partId, -1, -1 );
				int nparcel = 0;
				if ( hbWellParcelList != null ) {
					nparcel = hbWellParcelList.size();
				}
		        Message.printStatus ( 2, routine, "Well \"" + id + "\" (part receipt " + partId +
			        ") year=" + parcel_year + " Division=" + Div_int + " irrigates " + nparcel + " parcels" );
		        HydroBase_Wells hbwells;
		        fraction_irrig = new double[nparcel];
		        for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
			        hbwells = hbWellParcelList.get(iparcel);
			        parcel_ids.add ( "" + hbwells.getParcel_id() );
			        fraction_irrig[iparcel] = 1.0; // Well irrigates entire parcel
		        }
			}
			catch ( Exception e ) {
				    Message.printWarning ( 3, routine, e );
				    message = "Unexpected error getting well receipt to parcel data from HydroBase for " + id +
					    " (part " + partId + ")";
				    Message.printWarning(warningLevel,
					    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				    status.addToLog ( CommandPhaseType.RUN,
					    new CommandLogRecord(CommandStatusType.FAILURE,
						    message, "Report to software support." ) );
				    continue;
			}
		}

		// Whether the part is a WDID or a receipt, will have determined the list of parcel numbers to process.
		// Loop through each parcel and append the HydroBase irrigation practice data associated with each parcel.
		if ( parcel_ids.size() > 0 ) {
		    warning_count = readHydroBaseIrrigationPracticeTSForParcelList (
			    hdmi,
			    datastore,
			    culoc,
			    ipyts,
			    id,
			    Loctype,
			    parcel_ids,
			    fraction_irrig,	// fraction_irrig for each parcel in list
			    partId,		// well part that is being processed
			    parcel_year,
			    Div_int,
			    warningLevel, warning_count, command_tag, status, cacheHydroBase );
		}
		
		// Now read the supplemental data provided with
		// commands like setIrrigationPracticeTSFromList()...
		/* FIXME SAM 2007-10-18 Need to convert to a check to make sure that
		 * readIrrigationPracticeTSFromList() does not duplicate data from
		 * HydroBase.
		readSupplementalParcelUseTSListForLocation (
			HydroBase_Supplemental_ParcelUseTS_List,
			culoc, ipyts, part_id, parcel_year );
			*/
	} // End loop on location part IDs.
	return warning_count;
}

/**
Method to execute the readIrrigationPracticeTSFromHydroBase() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	// Get the input parameters...
	
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String ID_Java = StringUtil.replaceString(ID,"*",".*");
	// Year is populated in checkCommandParameters
	//String Year = _parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	int Div_int = Integer.parseInt ( Div );
	String Optimization = parameters.getValue( "Optimization" );
	if ( Optimization == null ) {
		Optimization = _UseMoreMemory;
	}
	boolean cacheHydroBase = true;
	if ( !Optimization.equalsIgnoreCase(_UseMoreMemory) ) {
		cacheHydroBase = false;
	}

	String locType = "CU location";	// Used with messages

	String DataStore = parameters.getValue("DataStore");
	HydroBaseDMI hbdmi = null;
	ColoradoHydroBaseRestDataStore hbRestDatastore = null;
	HydroBaseDataStore hbDatastore = null;
	// If there is a datastore open it otherwise open hydrobase dmi
	if ( (DataStore != null) && !DataStore.isEmpty() ) {
		// Get the datastore
		DataStore datastore0 = processor.getDataStoreForName(DataStore, DataStore.class);
		if ( datastore0 == null ) {
			message = "Error getting datastore \"" + DataStore + "\".";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check datastore configuration." ) );
		}
		else {
			Message.printStatus(2, routine, "Selected data store is \"" + DataStore + "\"");
			if ( datastore0 instanceof ColoradoHydroBaseRestDataStore ) {
				hbRestDatastore = (ColoradoHydroBaseRestDataStore)datastore0;
			}
			else if ( datastore0 instanceof HydroBaseDataStore ) {
				hbDatastore = (HydroBaseDataStore)datastore0;
				// Get the DMI for the datastore
				hbdmi = (HydroBaseDMI)hbDatastore.getDMI();
			}
		}
	}
	else {
		// Get the HydroBase DMI...
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
	}
	
	// Get the list of cu locations...
	
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
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( ipyListSize == 0 ) {
		message = "No irrigation practice time series are defined.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateIrrigationPracticeTSForCULocations() before this command." ) );
	}
	
	// Get the supplemental crop pattern data specified with SetCropPatternTS() and
	// SetCropPatternTSFromList() commands.
	// - this is used in the legacy approach but not with newer ReadIrrigationPracticeTSFromParcels() and related commands.
	
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
	
    // Output period
    
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

	if ( warning_count > 0 ) {
		// Input error...
		message = "Errors exist in input - unable to run the command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check command input." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
	
		// Year_int is set in checkCommandParameters().  If null, get the years by querying HydroBase...
		int [] parcelYears = null;
		if ( (__Year_int == null) || (__Year_int.length == 0)  ) {
			if ( hbdmi != null ) {
				// Direct database query
				try {
					int [] excludeYears = new int[0];
					parcelYears = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_int, excludeYears );
				}
				catch ( Exception e ) {
					parcelYears = null;
				}
				if ( parcelYears == null ) {
					message = "Cannot determine years of parcel data from HydroBase.";
			    	Message.printWarning ( warningLevel,
	    	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	        	status.addToLog ( commandPhase,
    	            	new CommandLogRecord(CommandStatusType.FAILURE,
    	                	message, "Report to software support.." ) );
					throw new CommandException ( message );
				}
			}
			else {
				message = "Cannot determine years of parcel data from web services - functionality is not implemented.";
			   	Message.printWarning ( warningLevel,
	    	       	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	       	status.addToLog ( commandPhase,
    	           	new CommandLogRecord(CommandStatusType.FAILURE,
    	               	message, "Must specify year for parcel use data when using web services." ) );
				throw new CommandException ( message );
			}
		}
		else {
			// Use the years specified by the user...
			parcelYears = new int[__Year_int.length];
			System.arraycopy( __Year_int, 0, parcelYears, 0, __Year_int.length );
		}
		// Remove parcel years that are not in the output period...
		// If the year is outside the output period, then do not process.
		if ( (OutputStart_DateTime != null) && (OutputEnd_DateTime != null) ) {
			int [] parcel_years2 = new int[parcelYears.length];
			int count = 0;
			for ( int i = 0; i < parcelYears.length; i++ ) {
				if ( (parcelYears[i] < OutputStart_DateTime.getYear()) ||
					(parcelYears[i] > OutputEnd_DateTime.getYear()) ) {
					Message.printStatus ( 2, routine, "Ignoring parcel year " +
						parcelYears[i] + ".  It is outside the output period " +
						OutputStart_DateTime.getYear() + " to " + OutputEnd_DateTime.getYear() );
				}
				else {
					parcel_years2[count++] = parcelYears[i];
				}
			}
			parcelYears = new int[count];
			System.arraycopy ( parcel_years2, 0, parcelYears, 0, count );
		}
		for ( int iparcel_year = 0; iparcel_year < parcelYears.length;	iparcel_year++ ) {
			Message.printStatus( 2, routine, "Will include division " + Div_int +
				" parcel data from " + parcelYears[iparcel_year] );
		}
		
		StateCU_Location culoc = null;	// StateCU location to process (used when do_ipy = true).
		List<String> parts = null;
		StateCU_Location_CollectionType collectionType = null;
		StateCU_Location_CollectionPartType collectionPartType = null;
		String culocId = null; // Well ID for CU location
		boolean isCollection = false;	// Indicate whether the well/location is a collection (aggregate or system)
		
		int parcelYear = 0;	// Used to process parcel years
	
		// Loop through the locations...
		DateTime date = new DateTime ( DateTime.PRECISION_YEAR );
		for ( int i = 0; i < culocListSize; i++ ) {
			// Use a CU Location for processing...
			culoc = culocList.get(i);
			culocId = culoc.getID();
			if ( !culocId.matches(ID_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Notify the processor of progress...
			notifyCommandProgressListeners ( i, culocListSize, (float)((i + 1)/((float)culocListSize)*100.0),
				"Processing CU location " + i + " of " + culocListSize );
			// Clear out the parcels saved with the well...
			//well.getParcels().removeAllElements();
			isCollection = culoc.isCollection();
			collectionType = null;	// Default...
			collectionPartType = null; 
			// The collection part list may vary by parcel year (although traditionally
			// D&W aggregation is constant for the period in CDSS modeling).
			// The collection type should not vary.
			if ( isCollection ) {
				collectionType = culoc.getCollectionType();
				collectionPartType = culoc.getCollectionPartType();
			}
			
			// Get the irrigation practice time series for the location...
			
			int pos = StateCU_Util.indexOf(	ipyList,culocId);
			StateCU_IrrigationPracticeTS ipyts = null;
			if ( pos >= 0 ) {
				// Get the time series...
				ipyts = ipyList.get(pos);
			}
			if ( (pos < 0) || (ipyts == null) ) {
				message = "Unable to find irrigation practice time series for \""+ culocId +
				"\".  Not setting data in time series.";
			    Message.printWarning ( warningLevel, 
	    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	        status.addToLog ( commandPhase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Verify that irrigation practice time series were created for all CU locations." ) );
				continue;
			}
			
			// Loop through the parcel years.  Put the loop here because the
			// parts of a collection could theoretically vary by year.
			YearTS yts = null; // Time series to manipulate.
			for ( int iParcelYear = 0;	iParcelYear < parcelYears.length;	iParcelYear++ ) {
				parcelYear = parcelYears[iParcelYear]; // Year to process
				
				Message.printStatus ( 2, routine, "Processing location ID=" + culocId + " parcelYear=" + parcelYear );
				
				// Set the values in the time series to zero for the parcel year if
				// missing.  Later, values will be added.  This will handled using the
				// readIrrigationPracticeTSFromList(), where aggregate parts might be added incrementally.
				// Calls to ipyts refreshAcsw() and refreshAcgw() will
				// result in these subtotals being updated.
				// Fill commands can then be used for years other than observations.
				
				// Old file format acreages...
				date.setYear ( parcelYear );
				// New file format acreages...
				yts = ipyts.getAcgwflTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				yts = ipyts.getAcgwsprTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				yts = ipyts.getAcswflTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				yts = ipyts.getAcswsprTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				// Also set the totals to zero (in case no data are
				// ever set in following code - acreage will be zero).
				// Recompute the total groundwater from the parts...
				ipyts.refreshAcgw(parcelYear);
				// Recompute the total groundwater from the parts...
				ipyts.refreshAcsw(parcelYear);
				// Used in old and new...
				yts = ipyts.getTacreTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
	
				if ( culoc.isGroundwaterOnlySupplyModelNode() && (collectionPartType == StateCU_Location_CollectionPartType.PARCEL) ) {
					// StateCU Location that is a collection of parcels (and therefore a well-only location).
					// This approach is being phased out other than Rio Grande (and may also be phased out for Rio Grande).
					Message.printStatus ( 2, routine, locType + " \"" + culocId + "\" is associated with a collection of parcels..." );
					// Aggregate or system, by parcel...
					parts = culoc.getCollectionPartIDsForYear(parcelYear);
					warning_count = readHydroBaseIrrigationPracticeTSForParcelList (
							hbdmi,
							hbRestDatastore,
							culoc,
							ipyts,
							culocId,
							locType,
							parts,
							null, // No percent_yield for wells (1.0 always)
							null, // No ditch aggregate part id
							parcelYear,
							Div_int,
							warningLevel, warning_count, command_tag, status, cacheHydroBase);
				}
				else if ( culoc.isGroundwaterOnlySupplyModelNode() && (collectionPartType == StateCU_Location_CollectionPartType.WELL) ) {
					// StateCU Location that is a collection of well (and therefore a well-only location) and the well
					// may or may not be an aggregate.
					// This approach is preferred to collection of parcels.
					Message.printStatus ( 2, routine, locType + " \"" + culocId +
						"\" is associated with a collection of well.  Determining associated parcel use..." );
					// Get the well station parts...
					List<StateCU_Location_CollectionPartIdType> partIdTypes = new ArrayList<StateCU_Location_CollectionPartIdType>();
					if ( culoc.isCollection() ) {
						collectionType = culoc.getCollectionType();
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() + "\" is a " +
							collectionType + "...processing each part...");
						// Well aggregates are only set once (year is ignored),
						// but logic allows year-specific collection to be implemented
						parts = culoc.getCollectionPartIDsForYear ( parcelYear );
						partIdTypes = culoc.getCollectionPartIDTypes ();
						// Uncomment for debugging...
						//for ( int ipart = 0; ipart < parts.size(); ++ipart ) {
						//	Message.printStatus(2,routine,"  For location " + culoc.getID() + " partId=" + parts.get(ipart) +
						//		" partIdType=" + partIdTypes.get(ipart));
						//}
					}
					else {
						// To reuse code below, just use a single part...
						//collectionType = "(explicit)";
						// TODO SAM 2006-01-31
						//name = div.getName();
						//name = well.getName();
						parts = new ArrayList<String>();
						parts.add ( culoc.getID() );
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() +
							"\" is associated with a an explicit well...processing as one part...");
					}
					warning_count = readHydroBaseIrrigationPracticeTSForWellList (
						hbdmi,
						hbRestDatastore,
						culoc, // The StateCU_Location object being processed
						ipyts, // IrrigationPracticeTS time series for the location
						culocId, // The location ID being processed
						locType, // Location type, used with messages
						parts, // Part identifiers
						partIdTypes, // Part identifier types (WDID or Receipt)
						parcelYear, // Parcel year being processed
						Div_int, // Division
						hydroBaseSupplementalParcelUseTSList, // Parcel use time series being read
						warningLevel, warning_count, command_tag, status, // Used for messaging/logging
						cacheHydroBase );
				}
				else {
					// CU location that is associated with a diversion
					// and the diversion may or may not be an aggregate.
					Message.printStatus ( 2, routine, locType + " \"" + culocId + "\" is associated with a " +
						"diversion.  Determining associated parcels..." );
					// Get the well station parts...
					if ( culoc.isCollection() ) {
						collectionType = culoc.getCollectionType();
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() + "\" is a " +
							collectionType + "...processing each part...");
						// Diversion aggregates are only set once (year is ignored),
						// but logic allows year-specific collection to be implemented
						parts = culoc.getCollectionPartIDsForYear ( parcelYear );
					}
					else {
						// To reuse code below, just use a single part...
						//collectionType = "(explicit)";
						// TODO SAM 2006-01-31
						//name = div.getName();
						//name = well.getName();
						parts = new ArrayList<String>();
						parts.add ( culoc.getID() );
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() +
							"\" is associated with a an explicit diversion...processing as one part...");
					}
					warning_count = readHydroBaseIrrigationPracticeTSForDiversionList (
						hbdmi,
						hbRestDatastore,
						culoc,
						ipyts,
						culocId,
						locType,
						parts,
						parcelYear,
						Div_int,
						hydroBaseSupplementalParcelUseTSList,
						warningLevel, warning_count, command_tag, status, cacheHydroBase );
				}
			} // End parcel year
		} // End location
	}
    catch ( Exception e ) {
        message = "Unexpected error reading irrigation practice time series from HydroBase (" + e + ").";
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
	
	String DataStore = parameters.getValue( "DataStore" );
	String ID = parameters.getValue ( "ID" );
	String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String Optimization = parameters.getValue( "Optimization" );
	
	StringBuffer b = new StringBuffer ();

	if (DataStore != null && DataStore.length() > 0){
		if ( b.length() > 0 ){
			b.append( "," );
		}
		b.append( "DataStore=" + DataStore );
	}
	if ( ID != null && ID.length() > 0 ) {
		if ( b.length() > 0 ){
			b.append( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( Year != null && Year.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Year=\"" + Year + "\"" );
	}
	if ( Div != null && Div.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=\"" + Div + "\"" );
	}
	if ( Optimization != null && Optimization.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Optimization=" + Optimization );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}