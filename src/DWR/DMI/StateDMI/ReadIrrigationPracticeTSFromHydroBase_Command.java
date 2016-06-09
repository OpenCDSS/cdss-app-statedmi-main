package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTSStructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
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
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the ReadIrrigationPracticeTSFromHydroBase() command.
Irrigation practice acreage time series data will be completely defined for years with HydroBase data.
NO ADJUSTMENT of data occurs after the read - it is simply set in the results.
NO CHECK of total acreage is done since it is expected that the crop pattern time series
total acreage is set after reading from HydroBase.
</p>
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
		List v = StringUtil.breakStringList ( Year, ",", StringUtil.DELIM_SKIP_BLANKS );
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
			String token = (String)v.get(i);
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
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "Year" );
    valid_Vector.add ( "Div" );
    valid_Vector.add ( "Optimization" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
	List hbwell_parcel_Vector = null;
	try {
		// Get the well/welltoparcels associated with the parcel...
		hbwell_parcel_Vector = hdmi.readWellsWellToParcelList(parcel_id, parcel_year, Div_int, cacheHydroBase );
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
	if ( hbwell_parcel_Vector != null ) {
		nwell_parcel = hbwell_parcel_Vector.size();
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
	return (new ReadIrrigationPracticeTSFromHydroBase_JDialog 
			( parent, this )).ok();
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

	List tokens = StringUtil.breakStringList ( command,
			"()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name, diversion ID
		message = "Syntax error in \"" + command +
		"\".  Not enough tokens.";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the file...
	try {	setCommandParameters ( PropList.parse ( Prop.SET_FROM_PERSISTENT,
			(String)tokens.get(1), routine, "," ) );
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
@param percent_irrig Fraction (0 to 1) of the parcel irrigated by the location.
If a ditch this may be <= 1.  If a well, it will be 1.0.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForParcel (
		HydroBaseDMI hdmi,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String part_id,
		int parcel_id,
		int parcel_year,
		int Div_int,
		double percent_irrig,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getName() + ".readHydroBaseIrrigationPracticeTSForParcel";
	String message;
	List hbparcel_Vector = null;
	try {
		hbparcel_Vector = hdmi.readParcelUseTSList (
			parcel_year, Div_int,
			parcel_id,
			cacheHydroBase );
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
	if ( hbparcel_Vector != null ) {
		nhbparcel =	hbparcel_Vector.size();
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

	HydroBase_ParcelUseTS hbparcel = (HydroBase_ParcelUseTS)hbparcel_Vector.get(0);
	
	double area = hbparcel.getArea()*percent_irrig;	// Acres
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
@param percent_irrig the fraction of the parcel irrigated by a ditch
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
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String Loctype,
		List parcel_ids,
		double [] percent_irrig,
		String part_id,
		int parcel_year,
		int Div_int,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getName() + ".readHydroBaseIrrigationPracticeTSForParcelList";
	// Loop through each parcel and append the HydroBase rights associated with each parcel
	
	int nparcel = 0;
	if ( parcel_ids != null ) {
		nparcel = parcel_ids.size();
	}
	Message.printStatus ( 2, routine, Loctype + " \"" + id + "\" year=" + parcel_year + " division=" +
		Div_int + " irrigates " + nparcel + " parcels" );
	
	// Get the wells associated with the parcels.
	
	int parcel_id;	// Specific parcel identifier to process
	double percent_irrig2;	// Percent of parcel that is irrigated by ditch
	for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
		parcel_id = Integer.parseInt((String)parcel_ids.get(iparcel) );
		// Process the HydroBase parcel data (values will be added to the
		// irrigation practice time series)...
		if ( percent_irrig == null ) {
			percent_irrig2 = 1.0;
		}
		else {
			percent_irrig2 = percent_irrig[iparcel];
		}
		warning_count = readHydroBaseIrrigationPracticeTSForParcel (
				hdmi,
				culoc,
				ipyts,
				id,
				part_id,	// Will be null for gw-only parcel collections
				parcel_id,
				parcel_year,
				Div_int,
				percent_irrig2,
				warningLevel, warning_count, command_tag, status, cacheHydroBase );
	}
	return warning_count;
}

/**
Read HydroBase irrigation practice TS for a
list of locations (WDIDs and not).  The parcels are associated with
a D&W location where groundwater supplements surface water supply.
@param hdmi HydroBaseDMI instance for database queries.
@param Loctype Location type for messages.
@param id Location identifier.
@param parcel_ids A list of parcel identifiers.
@param parcel_year The calendar year for which the parcels apply.
@param Div_int Division for data.
@param HydroBase_Supplemental_ParcelUseTS_Vector A list of HydroBase_NetAmt to be appended to as rights
are read for the parcel list.
@param cacheHydroBase indicate whether HydroBase results should be queried up front in cache to improve
performance (but take more memory).
*/
private int readHydroBaseIrrigationPracticeTSForLocationList (
		HydroBaseDMI hdmi,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		String id,
		String Loctype,
		List wdids,
		int parcel_year,
		int Div_int,
		List HydroBase_Supplemental_ParcelUseTS_Vector,
		int warningLevel, int warning_count, String command_tag, CommandStatus status, boolean cacheHydroBase )
{	String routine = "readIrrigationPracticeTSFromHydroBase_Command.readHydroBaseIrrigationPracticeTSForWDIDList";
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
	List hbparcel_structure_Vector = null;//Structure/parcel join data
	boolean have_structure_num = false;
	boolean is_wdid = false;	// Whether a WDID
	for ( int iparts = 0; iparts < nwdids; iparts++ ) {
		part_id = (String)wdids.get(iparts);
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
				hbparcel_structure_Vector = hdmi.readParcelUseTSStructureToParcelListForStructure_numCal_year(
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
			if ( hbparcel_structure_Vector != null ) {
				nparcel = hbparcel_structure_Vector.size();
			}
		
			Message.printStatus ( 2, routine, "Diversion \"" + id + "\" (part " + part_id +
				") year=" + parcel_year + " Div=" + Div_int + " irrigates " + nparcel + " parcels" );

			// Put together a list of parcel identifiers...
		
			List parcel_ids = new Vector(nparcel);
			double [] percent_irrig = new double[nparcel];
		
			HydroBase_ParcelUseTSStructureToParcel hbparcel_structure;
			for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
				hbparcel_structure = (HydroBase_ParcelUseTSStructureToParcel)
				hbparcel_structure_Vector.get(iparcel);
				parcel_ids.add ( "" + hbparcel_structure.getParcel_id() );
				percent_irrig[iparcel] = hbparcel_structure.getPercent_irrig();
			}
			// Loop through each parcel and append the HydroBase irrigation practice
			// data associated with each parcel
			warning_count = readHydroBaseIrrigationPracticeTSForParcelList (
				hdmi,
				culoc,
				ipyts,
				id,
				Loctype,
				parcel_ids,
				percent_irrig,	// percent_irrig for each parcel in list
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
			HydroBase_Supplemental_ParcelUseTS_Vector,
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
		Vector HydroBase_Supplemental_ParcelUseTS_Vector,
		StateCU_Location culoc, StateCU_IrrigationPracticeTS ipyts,
		String id, int parcel_year )
{	String routine = "readIrrigationPracticeTSFromHydroBase_Command.readSupplementalParcelUseTSListForWDID";
	int size = HydroBase_Supplemental_ParcelUseTS_Vector.size();
	StateDMI_HydroBase_ParcelUseTS puts = null;
	boolean has_gw_supply = false;
	for ( int i = 0; i < size; i++ ) {
		puts = (StateDMI_HydroBase_ParcelUseTS)
		HydroBase_Supplemental_ParcelUseTS_Vector.elementAt(i);
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
	CommandProcessor processor = getCommandProcessor();
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

	String Loctype = "CU location";	// Used with messages
	
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
	
	// Get the list of cu locations...
	
	List culocList = null;
	int culocListSize = 0;
	try {
		culocList = (List)processor.getPropContents ( "StateCU_Location_List");
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
	
	List ipyList = null;
	int ipyListSize = 0;
	try {
		ipyList = (List)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
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
	// SetCropPatternTSFromList() commands...
	
	List hydroBaseSupplementalParcelUseTSList = null;
	try {
		hydroBaseSupplementalParcelUseTSList =
			(List)processor.getPropContents ( "HydroBase_SupplementalParcelUseTS_List");
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
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
	
		// Year_int is set in checkCommandParameters().  If null, get the years by querying HydroBase...
		int [] parcel_years = null;
		if ( (__Year_int == null) || (__Year_int.length == 0)  ) {
			try {
				parcel_years = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_int );
			}
			catch ( Exception e ) {
				parcel_years = null;
			}
			if ( parcel_years == null ) {
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
			// Use the years specified by the user...
			parcel_years = new int[__Year_int.length];
			System.arraycopy( __Year_int, 0, parcel_years, 0, __Year_int.length );
		}
		// Remove parcel years that are not in the output period...
		// If the year is outside the output period, then do not process.
		if ( (OutputStart_DateTime != null) && (OutputEnd_DateTime != null) ) {
			int [] parcel_years2 = new int[parcel_years.length];
			int count = 0;
			for ( int i = 0; i < parcel_years.length; i++ ) {
				if ( (parcel_years[i] < OutputStart_DateTime.getYear()) ||
					(parcel_years[i] > OutputEnd_DateTime.getYear()) ) {
					Message.printStatus ( 2, routine, "Ignoring parcel year " +
						parcel_years[i] + ".  It is outside the output period " +
						OutputStart_DateTime.getYear() + " to " + OutputEnd_DateTime.getYear() );
				}
				else {
					parcel_years2[count++] = parcel_years[i];
				}
			}
			parcel_years = new int[count];
			System.arraycopy ( parcel_years2, 0, parcel_years, 0, count );
		}
		for ( int iparcel_year = 0; iparcel_year < parcel_years.length;	iparcel_year++ ) {
			Message.printStatus( 2, routine, "Will include division " + Div_int +
				" parcel data from " + parcel_years[iparcel_year] );
		}
		
		StateCU_Location culoc = null;	// StateCU location to process (used when do_ipy = true).
		List parts = null;
		String collection_type = null;
		String id = null;		// Well ID for CU location
		boolean is_collection = false;	// Indicate whether the well/location is
		
		int parcel_year = 0;	// Used to process parcel years
	
		// Loop through the locations...
		DateTime date = new DateTime ( DateTime.PRECISION_YEAR );
		for ( int i = 0; i < culocListSize; i++ ) {
			// Use a CU Location for processing...
			culoc = (StateCU_Location)culocList.get(i);
			id = culoc.getID();
			if ( !id.matches(ID_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Notify the processor of progress...
			notifyCommandProgressListeners ( i, culocListSize, (float)((i + 1)/((float)culocListSize)*100.0),
				"Processing CU location " + i + " of " + culocListSize );
			// Clear out the parcels saved with the well...
			//well.getParcels().removeAllElements();
			is_collection = false;
			collection_type = "";	// Default...
			is_collection = culoc.isCollection();
			// The collection part list may vary by parcel year (although traditionally
			// D&W aggregation is constant for the period in CDSS modeling).
			// The collection type should not vary.
			if ( is_collection ) {
				collection_type = culoc.getCollectionType();
			}
			
			// Get the irrigation practice time series for the location...
			
			int pos = StateCU_Util.indexOf(	ipyList,id);
			StateCU_IrrigationPracticeTS ipyts = null;
			if ( pos >= 0 ) {
				// Get the time series...
				ipyts = (StateCU_IrrigationPracticeTS)ipyList.get(pos);
			}
			if ( (pos < 0) || (ipyts == null) ) {
				message = "Unable to find irrigation practice time series for \""+ id +
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
			for ( int iparcel_year = 0;	iparcel_year < parcel_years.length;	iparcel_year++ ) {
				parcel_year = parcel_years[iparcel_year]; // Year to process
				
				Message.printStatus ( 2, routine, "Processing location ID=" + id + " parcel_year=" + parcel_year );
				
				// Set the values in the time series to zero for the parcel year if
				// missing.  Later, values will be added.  This will handled using the
				// readIrrigationPracticeTSFromList(), where aggregate parts might be added incrementally.
				// Calls to ipyts refreshAcsw() and refreshAcgw() will
				// result in these subtotals being updated.
				// Fill commands can then be used for years other than observations.
				
				// Old file format acreages...
				date.setYear ( parcel_year );
				/* FIXME 2007-10-18 Remove when code tests out.
				yts = ipyts.getGacreTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				yts = ipyts.getSacreTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
				*/
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
				ipyts.refreshAcgw(parcel_year);
				// Recompute the total groundwater from the parts...
				ipyts.refreshAcsw(parcel_year);
				// Used in old and new...
				yts = ipyts.getTacreTS();
				if ( yts.getDataValue(date) < 0.0 ) {
					yts.setDataValue ( date, 0.0 );
				}
	
				if ( culoc.hasGroundwaterOnlySupply() ){
					// StateCU Location that is a collection of parcels
					// (and therefore a well-only location)...
					Message.printStatus ( 2, routine, Loctype + " \"" + id +
					"\" is associated with a collection of parcels..." );
					// Aggregate or system, by parcel...
					parts = culoc.getCollectionPartIDsForYear(parcel_year);
					warning_count = readHydroBaseIrrigationPracticeTSForParcelList (
							hbdmi,
							culoc,
							ipyts,
							id,
							Loctype,
							parts,
							null, // No percent_yield for wells (1.0 always)
							null, // No ditch aggregate part id
							parcel_year,
							Div_int,
							warningLevel, warning_count, command_tag, status, cacheHydroBase);
				}
				else {
					// CU location that is associated with a diversion
					// and the diversion may or may not be an aggregate.
					Message.printStatus ( 2, routine, Loctype + " \"" + id + "\" is associated with a " +
						"diversion.  Determining associated parcels..." );
					// Get the well station parts...
					if ( culoc.isCollection() ) {
						collection_type = culoc.getCollectionType();
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() + "\" is a " +
							collection_type + "...processing each part...");
						// Diversion aggregates are only set once (year is ignored)
						parts = culoc.getCollectionPartIDsForYear ( parcel_year );
					}
					else {
						// To reuse code below, just use a single part...
						collection_type = "(explicit)";
						// TODO SAM 2006-01-31
						//name = div.getName();
						//name = well.getName();
						parts = new Vector();
						parts.add ( culoc.getID() );
						Message.printStatus ( 2, routine, "Location \"" + culoc.getID() +
							"\" is associated with a an explicit diversion...processing as one part...");
					}
					warning_count = readHydroBaseIrrigationPracticeTSForLocationList (
						hbdmi,
						culoc,
						ipyts,
						id,
						Loctype,
						parts,
						parcel_year,
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
	
	String ID = parameters.getValue ( "ID" );
	String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String Optimization = parameters.getValue( "Optimization" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
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