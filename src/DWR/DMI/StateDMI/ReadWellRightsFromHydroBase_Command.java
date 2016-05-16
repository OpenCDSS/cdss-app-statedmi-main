package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTSStructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_Util;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateMod.StateMod_Parcel;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;
import RTi.Util.Math.MathUtil;
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
import RTi.Util.Time.TimeZoneDefaultType;

/**
<p>
This class initializes, checks, and runs the ReadWellRightsFromHydroBase() command.
</p>
*/
public class ReadWellRightsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
//	 Possible values for parameters...

protected final String _False = "False";
protected final String _True = "True";

//Formats for well right identifiers...

protected final String _HydroBaseID = "HydroBaseID";
protected final String _StationIDW_NN = "StationIDW.NN";

private final int __HydroBaseID_int = 0;
private final int __StationIDW_NN_int = 1;

protected final String _1 = "1";
protected final String _AppropriationDate = "AppropriationDate";

/**
Optimization parameter values.
*/
protected final String _UseMoreMemory = "UseMoreMemory";
protected final String _UseLessMemory = "UseLessMemory";

private final int __AppropriationDate_int = 1000099;	// Special value

// Year parameter as list of years to process, or null.  Set in
// checkCommandParameters() for use in runCommand().
private int [] __Year_int = null;	// Default = process all years

/**
Constructor.
*/
public ReadWellRightsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadWellRightsFromHydroBase" );
}

/**
Add the HydroBase well rights for a location to the main list of StateMod rights.
@param hbwellr_Vector list of HydroBase_NetAmts to convert to StateMod rights.
*/
private void addHydroBaseRightsToStateModWellRights (
		String Loctype,
		String id,
		List hbwellr_Vector,
		String DecreeMin,
		double DecreeMin_double,
		int IDFormat_int,
		int parcel_year, String yearString,
		boolean is_collection,
		String collection_part_type,
		int OnOffDefault_int,
		List SMWellRight_Vector,
		List SMWellRight_match_Vector,
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = "ReadWellRightsFromHydroBase_Command.addHydroBaseRightsToStateModWellRights";
	String message;	// For messages
	HydroBase_NetAmts hbwellr = null;
	double decree;		// HydroBase decree
	String units;		// Units for decree
	double irtem;		// Admin number used by StateMod
	String decree_string;	// Decree as String formatted for output
	StateMod_WellRight wellr;	// StateMod right to add
	int wellr_count = 0;		// Count of rights for location
	HydroBase_AdministrationNumber admin_data;	// For on/off switch
	
	int nhbwellr = 0;
	if ( hbwellr_Vector != null ) {
		nhbwellr = hbwellr_Vector.size();
	}
	for ( int ir = 0; ir < nhbwellr; ir++ ) {
		hbwellr = (HydroBase_NetAmts)hbwellr_Vector.get(ir);
		// Processing absolute rights...
		decree = hbwellr.getNet_rate_abs();
		units = hbwellr.getUnit();
		if ( decree < DecreeMin_double ) {
			message = yearString + Loctype + " \"" + id + "\" has right with decree " + decree +
			" < the minimum (" + DecreeMin + ") skipping...";
			Message.printStatus ( 2, routine, message );
			continue;
		}
		if ( !units.equalsIgnoreCase("C") && !units.equalsIgnoreCase("CFS") ) {
			message = yearString + Loctype + " \"" + id + "\" has right with decree units \"" + units +
			"\" - only C and CFS are allowed - skipping...";
			// TODO SAM 2009-01-19 Evaluate whether warning or info
			//Message.printStatus ( 2, routine, message );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Report problem to support - possible error in HydroBase" +
						" or may need to update software." ) );
			continue;
		}
		irtem = hbwellr.getAdmin_no();

		// Format the decree according to StateMod precision and check for zero.
		
		decree_string = StringUtil.formatString(decree,"%.2f");
		if ( decree_string.equalsIgnoreCase("0.00") ) {
			message = yearString + "Well station \"" + id + "\" has right with decree of zero " +
			"after formatting for output...skipping.";
			Message.printStatus ( 2, routine, message );
		}
		else {
			// Add it...
			++wellr_count;
			wellr = new StateMod_WellRight ();
			wellr.setComment ( "" + parcel_year + " " +
				StringUtil.formatString(hbwellr.getParcelMatchClass(),"%4d") +
				" " + StringUtil.formatString(""+hbwellr.getParcelID(),"%6.6s"));
			wellr.setParcelYear( parcel_year );
			wellr.setParcelMatchClass ( hbwellr.getParcelMatchClass() );
			wellr.setParcelID ( "" + hbwellr.getParcelID() );
			if ( IDFormat_int == __HydroBaseID_int ) {
				// If the right is a water right, use the WDID padded to seven digits.  If it is
				// a well permit, use the permit receipt.  The appropriate identifier will have been set
				// in the helper code when processing the right/permit.
				wellr.setID ( hbwellr.getCommonID() );
				// Do a check to make sure that the ID is not going to overflow the field - this will
				// cause problems with well matching.
				if ( wellr.getID().length() > 12 ) {
					message = "Well right \"" + wellr.getID() + "\" is > 12 characters and will be " +
						"truncated on output - will cause problems later (e.g., in water right merging).";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Report problem to support - need to evaluate ID conventions." ) );
				}
			}
			else if ( IDFormat_int == __StationIDW_NN_int ) {
				// Use the old default ID formatting...
				if ( is_collection && collection_part_type.equalsIgnoreCase(
					StateMod_Well.COLLECTION_PART_TYPE_PARCEL) ) {
					// As per watright, do not use a "W" in the water right ID...
					wellr.setID ( id + "." + StringUtil.formatString(wellr_count, "%02d") );
				}
				else {
					// As per watright, use a "W" in the water right ID...
					wellr.setID ( id + "W." + StringUtil.formatString(wellr_count, "%02d") );
				}
			}
			else {
				// Fall through...
				wellr.setID ( hbwellr.getCommonID() );
			}
			wellr.setName ( hbwellr.getWr_name() );
			wellr.setCgoto ( id );
			wellr.setIrtem ( StringUtil.formatString(irtem,"%.5f"));
			wellr.setDcrdivw ( decree );
			if ( OnOffDefault_int == __AppropriationDate_int ) {
				// Convert the administration number to a year...
				try { admin_data = new HydroBase_AdministrationNumber ( irtem );
					wellr.setSwitch ( admin_data.getAppropriationDate().getYear() );
				}
				catch ( Exception e ) {
					message = yearString + "Error converting administration number " + irtem +
					" to date for right \"" + id + "\".  Setting on/off switch to 1.";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Report problem to support - possible error in HydroBase" +
								" or may need to update software." ) );
					wellr.setSwitch( 1 );
				}
			}
			else {
				// Use the default value for the administration number...
				wellr.setSwitch ( OnOffDefault_int );
			}
			Message.printStatus ( 2, routine, yearString + "Adding right ID=\"" + wellr.getID() + "\" decree=" +
				StringUtil.formatString(wellr.getDcrdivw(),"%.2f") + " AdminNum=" + wellr.getIrtem() +
				" StationID=" + wellr.getCgoto() );
			StateDMI_Util.findAndAddSMWellRight ( SMWellRight_Vector, SMWellRight_match_Vector, wellr,
				StateDMI_Util._IF_MATCH_APPEND );
			// TODO SAM 2007-05-24 Evaluate need.
			// The following seems to not be needed now that water rights are being handled more
			// explicitly
			//StateDMI_Util._IF_MATCH_MERGE );
		}
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
	//String IDFormat = parameters.getValue ( "IDFormat" );
	String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String DecreeMin = parameters.getValue ( "DecreeMin" );
	String DefaultAppropriationDate = parameters.getValue( "DefaultAppropriationDate" );
	String DefineRightHow = parameters.getValue( "DefineRightHow" );
	String ReadWellRights = parameters.getValue( "ReadWellRights" );
	String UseApex = parameters.getValue( "UseApex" );
	String OnOffDefault = parameters.getValue( "OnOffDefault" );
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
		boolean error_found = false;
		if ( size == 0 ) {
			__Year_int = null;
		}
		else {
			__Year_int = new int[size];
		}
		for ( int i = 0; i < size; i++ ) {
			String token = (String)v.get(i);
			if ( !StringUtil.isInteger(token) ) {
				error_found = true;
			}
			else {
				__Year_int[i] = Integer.parseInt(token);
			}
		}
		if (error_found ) {
			message = "The year list values (" + Year + ") are not all integers.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the year list as YYYY,YYYY, etc.." ) );
		}
	}
	
	if ( (Div == null) || (Div.length() == 0) ) {
		message = "The division must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division as an integer." ) );
	}
	else if ( !StringUtil.isInteger(Div) ) {
		message = "The division (" + Div + ") is not an integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division as an integer." ) );
	}
	
	if ( (DecreeMin != null) && !DecreeMin.equals("") && !StringUtil.isDouble(DecreeMin) ) {
		message = "The decree minumum (" + DecreeMin + ") is not a number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the decree minimum as a number." ) );
	}
	
	if ( (DefaultAppropriationDate != null) && (DefaultAppropriationDate.length() > 0) ) {
		try {
			DateTime.parse(DefaultAppropriationDate);
		}
		catch (Exception e) {
			message = "The default appropriation date (" + DefaultAppropriationDate +
			") is not a valid date.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the default appropriation date as YYYY-MM-DD or MM/DD/YYYY." ) );
		}
	}
	if ( (DefineRightHow != null) && (DefineRightHow.length() > 0) &&
		!DefineRightHow.equalsIgnoreCase("" + DefineWellRightHowType.EARLIEST_DATE) &&
		!DefineRightHow.equalsIgnoreCase("" + DefineWellRightHowType.LATEST_DATE) &&
		!DefineRightHow.equalsIgnoreCase("" + DefineWellRightHowType.RIGHT_IF_AVAILABLE) ) {
		message = "The DefineRightHow value (" + DefineRightHow + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify DefineRightHow as " + DefineWellRightHowType.EARLIEST_DATE + ", " +
				DefineWellRightHowType.LATEST_DATE +
				", or " + DefineWellRightHowType.RIGHT_IF_AVAILABLE + " (default).") );
	}
	if ( (ReadWellRights != null) && (ReadWellRights.length() > 0) &&
		!ReadWellRights.equalsIgnoreCase(_True) && !ReadWellRights.equalsIgnoreCase(_False) ) {
		message = "The ReadWellRights value (" + ReadWellRights + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify ReadWellRights as " + _False + " (default) or " + _True + ".") );
	}
	if ( (ReadWellRights != null) && (ReadWellRights.equalsIgnoreCase(_True) ||
		ReadWellRights.equals("")) &&
		!DefineRightHow.equalsIgnoreCase("" + DefineWellRightHowType.RIGHT_IF_AVAILABLE) ) {
		message = "ReadWellRights=True can only be specified when "+
		"DefineRightHow=" + DefineWellRightHowType.RIGHT_IF_AVAILABLE + " - well rights are expected to be found in HydroBase.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Verify the parameter values.") );
	}
	if ( (UseApex != null) && (UseApex.length() > 0) &&
		!UseApex.equalsIgnoreCase(_True) && !UseApex.equalsIgnoreCase(_False) ) {
		message = "The UseApex value (" + UseApex + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify UseApex as " + _False + " (default) or " + _True + ".") );
	}
	
	if ( (OnOffDefault != null) && (OnOffDefault.length() > 0) &&
		!OnOffDefault.equalsIgnoreCase(_AppropriationDate) && !StringUtil.isInteger(OnOffDefault)) {
		message = "The OnOffDefault value (" + OnOffDefault + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify OnOffDefault as an integer year or " + _AppropriationDate + " (default).") );
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
    valid_Vector.add ( "IDFormat" );
    valid_Vector.add ( "Div" );
    valid_Vector.add ( "Year" );
    valid_Vector.add ( "DecreeMin" );
    valid_Vector.add ( "DefaultAppropriationDate" );
    valid_Vector.add ( "DefineRightHow" );
    valid_Vector.add ( "ReadWellRights" );
    valid_Vector.add ( "UseApex" );
    valid_Vector.add ( "OnOffDefault" );
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
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ReadWellRightsFromHydroBase_JDialog ( parent, this )).ok();
}

/**
Read a single parcel from HydroBase as a HydroBase_ParcelUseTS, given a
parcel identifier, year, and water division.
@param hdmi HydroBaseDMI instance for database queries.
@param id The identifier for the model location associated with the parcel.
@param parcel_id Parcel identifier.
@param parcel_year Parcel year.
@param Div_int water division as integer.
@param cacheHydroBase if true, then on first read all the data for a division will be read
*/
private HydroBase_ParcelUseTS readHydroBaseParcel (
		HydroBaseDMI hdmi,
		StateMod_Well well,
		String id,
		int parcel_id,
		int parcel_year,
		int Div_int,
		int warningLevel, int warningCount, String commandTag, CommandStatus status,
		boolean cacheHydroBase )
{	String routine = "readWellRightsFromHydroBase_Command.readHydroBaseParcel";
	List hbparcel_Vector = null;
	String message;
	try {
		// Call the version that caches results
		hbparcel_Vector = hdmi.readParcelUseTSList ( parcel_year, Div_int, parcel_id, cacheHydroBase );
	}
	catch ( Exception e ) {
		message = "Unexpected error getting parcel data from HydroBase for " + id +
			" parcel=" +parcel_id + " year=" + parcel_year + " division=" + Div_int +"(" + e + ").";
		Message.printWarning ( 3, routine, e );
		Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		return null;
	}

	int nhbparcel = 0;
	if ( hbparcel_Vector != null ) {
		nhbparcel =	hbparcel_Vector.size();
	}
	// Save parcel information on the well station so that data check output can be output later.
	// Should only be one parcel...
	if ( nhbparcel == 0 ) {
		return null;
	}
	
	if ( nhbparcel != 1 ) {
		message = "Error - multiple parcel data records exist in HydroBase for " + id +
			" parcel=" +parcel_id + " year=" + parcel_year + " division=" + Div_int;
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
			routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Possible error in HydroBase - non-unique data." ) );
		return null;
	}

	HydroBase_ParcelUseTS hbparcel = (HydroBase_ParcelUseTS)hbparcel_Vector.get(0);
	
	// Save information for checks to be done later...
	StateMod_Parcel sm_parcel = null;
	sm_parcel = new StateMod_Parcel();
	sm_parcel.setCrop (	hbparcel.getLand_use());
	sm_parcel.setArea ( hbparcel.getArea());
	sm_parcel.setYear ( hbparcel.getCal_year());
	well.getParcels().add( sm_parcel );
	return hbparcel;
}

/**
Read HydroBase well rights as HydroBase_NetAmts (sec=parcel, =class), for a list of diversion WDIDs.
The parcels are associated with a D&W location where groundwater supplements surface water supply.
@param hdmi HydroBaseDMI instance for database queries.
@param Loctype Location type for messages.
@param collectionType collection type (Aggregate or System), for messages.
@param id Location identifier.
@param parcel_ids A list of parcel identifiers.
@param parcel_year The calendar year for which the parcels apply.
@param String Div Division for data.
@param hbwellr_Vector A list of HydroBase_NetAmt to be appended to as rights are read for the parcel list.
@param cacheHydroBase if true, then on first read all the data for a division will be read
*/
private List readHydroBaseWellRightsForDiversionWDIDList (
		HydroBaseDMI hdmi,
		StateMod_Well well,
		String id,
		String Loctype,
		String collectionType,
		List wdids,
		int parcel_year, String yearString,
		int Div_int,
		DefineWellRightHowType defineWellRightHow,
		boolean ReadWellRights_boolean,
		boolean UseApex_boolean,
		double default_admin_number,
		Date default_appro_Date,
		int warningLevel, int warningCount, String commandTag, CommandStatus status, boolean cacheHydroBase )
{	String routine = "readWellRightsFromHydroBase_Command.readHydroBaseWellRightsForWDIDList";
	String message;
	// Loop through each location and for each WDID part get the parcel list.
	// Then call other methods to read the wells for the parcels, using the
	// same approach as for well-only supply parcels.

	int nwdids = 0;
	if ( wdids != null ) {
		nwdids = wdids.size();
	}
	Message.printStatus ( 2, routine, yearString + "Well \"" + id + "\" has " + nwdids + " ditch parts (" +
		collectionType + ").");
	String part_id;	// single WDID
	int [] wdid_parts = new int[2];
	HydroBase_StructureView hbdiv = null;	// Individual ditch
	List hbparcel_structure_Vector = null;//Structure/parcel join data
	List hbwellr_Vector = new Vector();	// List of well rights for all parcels related to location
	for ( int iparts = 0; iparts < nwdids; iparts++ ) {
		part_id = (String)wdids.get(iparts);
		Message.printStatus ( 2, routine, yearString +
			"Processing well \"" + id + "\" ditch part \"" + part_id + "\" (" + collectionType + ")." );
		try {
			// Parse out the WDID for the ditch...
			HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
		}
		catch ( Exception e ) {
			if ( (nwdids == 1) && !HydroBase_WaterDistrict.isWDID(part_id)) {
				// The diversion is a single diversion that is not a WDID.
				// Therefore a warning is not needed...
				Message.printStatus ( 2,
					routine, yearString + "Well single ditch part \"" + id + "\" is not a WDID.  " +
						"Cannot read corresponding diversion structures from HydroBase." );
			}
			else {
				// Not a WDID - this is an error because valid structures are
				// expected as parts of an aggregate...
				message = yearString + "Well \"" + id + "\" (ditch part " + part_id +
					", " + collectionType + ") is not a WDID.  Cannot read structure data from HydroBase.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Confirm that part ID is a ditch WDID." ) );
			}
			// In any case cannot process the data...
			continue;
		}
		// Verify that the division for the WDID is in the requested division.
		int wdDiv = HydroBase_Util.lookupDivisionForWaterDistrict(hdmi, wdid_parts[0]);
		if ( wdDiv != Div_int ) {
			message = "Location " + id + " (part " + part_id + ", " + collectionType +
				") has WDID in division " + wdDiv +
				", which is different from the requested division " + Div_int +
				" - results will not be correct - skipping location.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that all WDID identifiers used by the command are for the specified " +
						"division and that structures do not span divisions." ) );
			continue;
		}
		try {
			// Get the structure so the structure number can be retrieved...
			hbdiv = hdmi.readStructureViewForWDID( wdid_parts[0], wdid_parts[1] );
		}
		catch ( Exception e ) {
			message = yearString + "Error getting structure data from HydroBase for " + id + " (part " +
				part_id + ", " + collectionType + ") (" + e + ")";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			continue;
		}
		if ( hbdiv == null ) {
			message = yearString + "Unable to find structure in HydroBase for location \"" +
				id + "\" part=\"" + part_id + "\", " + collectionType + ".  Skipping.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the " + collectionType + " part identifiers are correct." ) );
			continue;
		}
		try {
			// Get the parcels that the ditch irrigates for the specific year...
			hbparcel_structure_Vector = hdmi.readParcelUseTSStructureToParcelListForStructure_numCal_year(
				hbdiv.getStructure_num(), parcel_year );
		}
		catch ( Exception e ) {
			message = yearString + "Unexpected error getting structure to parcel data from HydroBase for well \"" +
				id + "\" (ditch part " + part_id + ") year=" + parcel_year + " (" + e + ").";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			continue;
		}

		int nparcel = 0;
		if ( hbparcel_structure_Vector != null ) {
			nparcel = hbparcel_structure_Vector.size();
		}
		
		Message.printStatus ( 2, routine, yearString + "Well \"" + id + "\" (ditch part " + part_id +
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
		// Loop through each parcel and append the HydroBase rights associated with each parcel
		List hbwellr2_Vector = readHydroBaseWellRightsForParcelList (
				hdmi,
				well,
				id,
				Loctype,
				parcel_ids,
				percent_irrig,	// percent_irrig for each parcel in list
				part_id,		// Ditch part that is being processed
				parcel_year, yearString,
				Div_int,
				defineWellRightHow,
				ReadWellRights_boolean,
				UseApex_boolean,
				default_admin_number,
				default_appro_Date,
				warningLevel, warningCount, commandTag, status, cacheHydroBase );
		int nwellr2 = 0;
		if ( hbwellr2_Vector != null ) {
			nwellr2 = hbwellr2_Vector.size();
			for ( int iwellr2 = 0; iwellr2 < nwellr2; iwellr2++ ) {
				hbwellr_Vector.add ( (HydroBase_NetAmts)hbwellr2_Vector.get(iwellr2) );
			}
		}
	}
	return hbwellr_Vector;
}

/**
Read HydroBase well rights as HydroBase_NetAmts (sec=parcel, =class), for a parcel.
@param hdmi HydroBaseDMI instance to use for queries.
@param Loctype Location type being processed, for messages.
@param id Model location ID related to parcels.
@param parcel_id ID for a single parcel for which to read wells.
@param percent_irrig The fraction of the parcel irrigated by the ditch, or 1.0 if groundwater only supply.
@param part_id WDID in a D&W aggregation.
@param iparcel Parcel count being processed (0 index), used when processing D&W (part_id != null).
@param nparcel Total number of parcels to process, used when processing D&W (part_id != null).
@param parcel_year Year for which to read data.
@param Div_int Water division for parcel data.
@param hbwellr_Vector The list of HydroBase_NetAmt containing wells associated with the parcel.
@param cacheHydroBase if true, then on first read all the data for a division will be read
*/
private void readHydroBaseWellRightsForParcel (
		HydroBaseDMI hdmi,
		String Loctype,
		String id,
		int parcel_id,
		double percent_irrig,
		String part_id,
		int iparcel,
		int nparcel,
		int parcel_year, String yearString,
		int Div_int,
		DefineWellRightHowType defineWellRightHow,
		boolean ReadWellRights_boolean,
		boolean UseApex_boolean,
		double default_admin_number,
		Date default_appro_Date,
		List hbwellr_Vector,
		int warningLevel, int warningCount, String commandTag, CommandStatus status, boolean cacheHydroBase )
{	String routine = "readWellRightsFromHydroBase.readHydroBaseWellRightsForParcel";
	// First read the Wells - WellToParcel join to get the "holes in the ground"
	String message;

	List hbwell_parcel_Vector = null;
	try {
		// Get the well/welltoparcels associated with the parcel...
		hbwell_parcel_Vector = hdmi.readWellsWellToParcelList( parcel_id, parcel_year, Div_int, cacheHydroBase );
	}
	catch ( Exception e ) {
		if ( part_id == null ) {
			// Processing groundwater only parcel...
			message = yearString + "Error getting well to parcel data from HydroBase for well " + id +
				" parcel=" +parcel_id + " division=" + Div_int + "(" + e + ").";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
		else {
			// Processing WDID part of a D&W aggregation...
			message = yearString + "Error getting well to parcel data from HydroBase for " +id +
			" (part " + part_id + ") parcel=" + parcel_id + " Div=" + Div_int + "(" + e + ").";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
		return;
	}
	int nwell_parcel = 0;
	if ( hbwell_parcel_Vector != null ) {
		nwell_parcel = hbwell_parcel_Vector.size();
	}
	if ( part_id == null ) {
		// Groundwater only parcel
		Message.printStatus ( 2, routine, yearString + Loctype +	" \"" +id + "\" Div="
			+ Div_int + " parcel " + parcel_id + " has "+ nwell_parcel + " well(s)." );
	}
	else {
		// WDID part of a D&W location
		if ( id.equals(part_id) ) {
			// Explicit structure
			Message.printStatus ( 2, routine, yearString + "Diversion \"" +id + "\" (explicit, parcel " +
				(iparcel + 1) + " of " + nparcel+") " +
				" Div=" + Div_int + " parcel " + parcel_id +
				" has "+ nwell_parcel + " well(s)." );
		}
		else {
			// D&W collection...
			Message.printStatus ( 2, routine, yearString + "Diversion \"" +id + "\" (part " +
			part_id + ", parcel " + (iparcel + 1) + " of " + nparcel+") " +
			" Div=" + Div_int + " parcel " + parcel_id + " has "+ nwell_parcel + " well(s)." );
		}
	}
	/* TODO SAM 2006-04-24
	Change to only add to well count if non-zero yield (decree) below
	// Save the count of wells for the parcel to use with data checks.
	// sm_parcel will be OK as assigned from above...
	if ( do_wer && (sm_parcel != null) ) {
		sm_parcel.setWellCount (nwell_parcel);
	}
	*/
	HydroBase_Wells hbwell_parcel = null;	// Used in iterator
	List hbwellr2_Vector = new Vector();	// To return individual rights.
	HydroBase_NetAmts hbwellr = null;		// To transfer rights in Vectors
	for ( int iwell_parcel = 0; iwell_parcel < nwell_parcel; iwell_parcel++ ) {
		// Process the single wells/well to parcel record, either using its
		// data directly or querying net amount rights.
		try {
			hbwell_parcel = (HydroBase_Wells)hbwell_parcel_Vector.get(iwell_parcel);
			warningCount =	StateDMI_Util.readWellRightsFromHydroBaseHelper (
				hdmi,
				commandTag,	warningLevel, warningCount, status,
				routine,
				parcel_year,
				id,
				hbwell_parcel,
				parcel_id,
				percent_irrig,	// Use for parcel/ditch overlap, 1.0 if GW only
				defineWellRightHow,
				ReadWellRights_boolean,
				UseApex_boolean,
				default_admin_number,
				default_appro_Date,
				hbwellr2_Vector );
		}
		catch ( Exception e ) {
			message = yearString + "Error getting well rights data from HydroBase for " + id +
				" parcel=" +parcel_id + " division=" + Div_int + "(" + e + ")";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			return;
		}
		// Transfer the resulting HydroBase rights into the big list...
		int irsize = hbwellr2_Vector.size();
		for ( int ir = 0; ir < irsize; ir++ ) {
			hbwellr = (HydroBase_NetAmts)hbwellr2_Vector.get(ir);
			hbwellr_Vector.add ( hbwellr );
			/* TODO SAM 2007-05-23 Evaluate how to add parcel/supply
			if ( hbwellr.getNet_rate_abs() >= DecreeMin_double ) {
				// Increment the count of wells for the parcel, to be used
				// in data checks...
				sm_parcel.setWellCount(
				sm_parcel.getWellCount()
				+ 1 );
			}
			*/
		}
	}
}

/**
Read HydroBase well rights as HydroBase_NetAmts (sec=parcel, =class), for a
list of parcels.  The parcels can come from a groundwater-only aggregation
or a D&W location where groundwater supplements surface water supply.
@param hdmi HydroBaseDMI instance for database queries.
@param Loctype Location type for messages.
@param id Location identifier.
@param parcel_ids A list of parcel identifiers.
@param percent_irrig the fraction of the parcel irrigated by a ditch
for the year.  This is only used when the calling code is processing
locations with surface and groundwater supply.  Specify null or an array
of 1.0 when processing groundwater only.
@param parcel_year The calendar year for which the parcels apply.
@param String Div Division for data.
@param hbwellr_Vector A list of HydroBase_NetAmt to be appended to as rights
are read for the parcel list.
@param cacheHydroBase if true, then on first read all the data for a division will be read
*/
private List readHydroBaseWellRightsForParcelList (
		HydroBaseDMI hdmi,
		StateMod_Well well,
		String id,
		String Loctype,
		List parcel_ids,
		double [] percent_irrig,
		String part_id,
		int parcel_year, String yearString,
		int Div_int,
		DefineWellRightHowType defineWellRightHow,
		boolean ReadWellRights_boolean,
		boolean UseApex_boolean,
		double default_admin_number,
		Date default_appro_Date,
		int warningLevel, int warningCount, String commandTag, CommandStatus status,
		boolean cacheHydroBase )
{	String routine = "readWellRightsFromHydroBase_Command.readHydroBaseWellRightsForParcelList";
	String message;
	// Loop through each parcel and append the HydroBase rights associated with each parcel
	
	int nparcel = 0;
	if ( parcel_ids != null ) {
		nparcel = parcel_ids.size();
	}
	Message.printStatus ( 2, routine, Loctype + " \"" + id +
		yearString + " division=" + Div_int + " irrigates " + nparcel + " parcels" );
	
	// Get the wells associated with the parcels.
	
	int parcel_id;	// Specific parcel identifier to process
	HydroBase_ParcelUseTS hbwell_parcel = null;
	List hbwellr_Vector = new Vector();	// Rights for location
	double percent_irrig2;	// Percent of parcel that is irrigated by ditch
	for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
		parcel_id = StringUtil.atoi((String)parcel_ids.get(iparcel) );
		// Get the HydroBase parcel data...
		hbwell_parcel = readHydroBaseParcel ( hdmi, well, id, parcel_id,
			parcel_year, Div_int, warningLevel, warningCount, commandTag, status, cacheHydroBase );
		if ( hbwell_parcel == null ) {
			// Should not happen but seems to be.
			message = yearString + "Unable to find parcel data in HydroBase for location \"" +
				well.getID() + " parcel_id=" + parcel_id + ".  Skipping - check input parcel lists.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Confirm that model and HydroBase data are consistent." ) );
			continue;
					
		}
		// Now read the wells for the parcel (they will be added to the vector)...
		if ( percent_irrig == null ) {
			percent_irrig2 = 1.0;
		}
		else {
			percent_irrig2 = percent_irrig[iparcel];
		}
		readHydroBaseWellRightsForParcel ( hdmi, Loctype, id,
				hbwell_parcel.getParcel_id(), 
				percent_irrig2,
				part_id,
				iparcel,
				nparcel,
				parcel_year, yearString, Div_int,
				defineWellRightHow,
				ReadWellRights_boolean,
				UseApex_boolean,
				default_admin_number,
				default_appro_Date,
				hbwellr_Vector,
				warningLevel, warningCount, commandTag, status, cacheHydroBase );
	}
	return hbwellr_Vector;
}

/**
Read well rights from HydroBase for a well station (explicit well, or in the future a well aggregated
by well IDs).
 * @param hdmi
 * @param well
 * @param id
 * @param is_collection
 * @param Div_int
 * @param defineWellRightHow
 * @param ReadWellRights_boolean
 * @param UseApex_boolean
 * @param default_admin_number
 * @param default_appro_Date
 * @param warningLevel
 * @param warningCount
 * @param commandTag
 * @param status
 */
private List readHydroBaseWellRightsForWellStation (
	HydroBaseDMI hdmi,
	String id,
	boolean isCollection,
	List parts,
	int Div_int,
	boolean ReadWellRights_boolean,
	boolean UseApex_boolean,
	double default_admin_number,
	Date default_appro_Date,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = getClass().getName() + ".readHydroBaseWellRightsForWellStation";
	String message;
	List hbwellr2_Vector = new Vector(); // Rights that are read and returned
	// Single well - get its water rights...
	HydroBase_Wells hbwell_parcel = null;
	try {
		warningCount = StateDMI_Util.readWellRightsFromHydroBaseHelper (
			hdmi,
			commandTag, warningLevel, warningCount, status,
			routine,
			-999, // No year since not dealing with parcels
			id,
			hbwell_parcel,
			-999, // No parcel ID since not dealing with parcels
			1.0, // Assume that 100% of well right is to be used
			DefineWellRightHowType.RIGHT_IF_AVAILABLE, // Always use right if available
			true, // ReadWellRights_boolean, (always read because reading explicit well data, not from well/parcels)
			UseApex_boolean,
			default_admin_number,
			default_appro_Date,
			hbwellr2_Vector );
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error reading well rights for well station \"" + id + "\" (" + e + ").";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "See the log file - report the problem to software support." ) );
	}
	return hbwellr2_Vector;
}

/**
Read well rights from HydroBase for a well station (explicit well, or a well aggregated by well IDs).
@param hdmi
@param id
@param is_collection
@param defineWellRightHow
@param ReadWellRights_boolean
@param UseApex_boolean
@param default_admin_number
@param default_appro_Date
@param warningLevel
@param warningCount
@param commandTag
@param status
*/
private List readHydroBaseWellRightsForWellStationList (
	HydroBaseDMI hdmi,
	String id,
	boolean isCollection,
	List parts,
	boolean ReadWellRights_boolean,
	boolean UseApex_boolean,
	double default_admin_number,
	Date default_appro_Date,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = getClass().getName() + ".readHydroBaseWellRightsForWellStationList";
	String message;
	// List of well rights to return for all well stations
	List hbwellr3_Vector = new Vector(); // Rights that are read for the list of wells
	// Single well - get its water rights...
	HydroBase_Wells hbwell_parcel = null;
	// Loop through the parts
	int partsSize = parts.size();
	for ( int i = 0; i < partsSize; i++ ) {
		String idPart = (String)parts.get(i);
		if ( !HydroBase_WaterDistrict.isWDID(idPart)) {
			message = "Well id \"" + id + "\" part \"" + idPart + "\" is not a WDID and features have not been " +
				"implemented to read well permits that are part of collections.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report the problem to software support - feature needs to be implemented." ) );
		}
		try {
			List hbwellr2_Vector = new Vector(); // Rights that are read for this well
			warningCount = StateDMI_Util.readWellRightsFromHydroBaseHelper (
				hdmi,
				commandTag, warningLevel, warningCount, status,
				routine,
				-999, // No year since not dealing with parcels
				idPart,
				hbwell_parcel,
				-999, // No parcel ID since not dealing with parcels
				1.0, // Assume that 100% of well right is to be used
				DefineWellRightHowType.RIGHT_IF_AVAILABLE, // DefineRightHow_int, (always use right if available)
				true, // ReadWellRights_boolean, (always read)
				UseApex_boolean,
				default_admin_number,
				default_appro_Date,
				hbwellr2_Vector );
			// Add the well rights to the main list
			hbwellr3_Vector.addAll(hbwellr2_Vector);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			message = "Unexpected error reading well rights for well station \"" + id + "\" (" + e + ").";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file - report the problem to software support." ) );
		}
	}
	return hbwellr3_Vector;
}

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
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
	String IDFormat = parameters.getValue ( "IDFormat" );
	if ( IDFormat == null ) {
		IDFormat = _StationIDW_NN;
	}
	int IDFormat_int = __HydroBaseID_int;
	if ( IDFormat.equalsIgnoreCase(_StationIDW_NN) ) {
		IDFormat_int = __StationIDW_NN_int;
	}
	// Populated in checkCommandParameters
	//String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String DecreeMin = parameters.getValue( "DecreeMin" );
	if ( DecreeMin == null ) {
		DecreeMin = ".0005"; // Default
	}
	double DecreeMin_double = Double.parseDouble(DecreeMin);
	String DefaultAppropriationDate = parameters.getValue( "DefaultAppropriationDate" );
	String DefineRightHow = parameters.getValue( "DefineRightHow" );
	DefineWellRightHowType defineWellRightHow = DefineWellRightHowType.EARLIEST_DATE; // Legacy default
	if ( DefineRightHow != null ) {
		defineWellRightHow = DefineWellRightHowType.valueOfIgnoreCase(DefineRightHow);
	}
	String ReadWellRights = parameters.getValue( "ReadWellRights" );
	if ( ReadWellRights == null ) {
		ReadWellRights = _True; // Default
	}
	String UseApex = parameters.getValue( "UseApex" );
	if ( UseApex == null ) {
		UseApex = _False; // Default
	}
	String OnOffDefault = parameters.getValue( "OnOffDefault" );
	if ( OnOffDefault == null ) {
		OnOffDefault = _AppropriationDate;
	}
	String Optimization = parameters.getValue( "Optimization" );
	if ( Optimization == null ) {
		Optimization = _UseMoreMemory;
	}
	boolean cacheHydroBase = true;
	if ( !Optimization.equalsIgnoreCase(_UseMoreMemory) ) {
		cacheHydroBase = false;
	}
	
	String Loctype = "Well station";	// Used with messages
	
	// Vector that tracks when identifiers when adding data cause a
	// conflict with previous data set.
	// TODO SAM 2007-05-23 Old code did not track conflicts between
	// this command and others that set well rights.
	List SMWellRight_match_Vector = processor.getStateModWellRightMatchList();

	// Get the list of well stations...
	
	List stationList = null;
	int stationListSize = 0;
	try {
		stationList = (List)processor.getPropContents ( "StateMod_WellStation_List");
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting well station data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of well rights (probably empty)...
	
	List rightList = null;
	try {
		rightList = (List)processor.getPropContents ( "StateMod_WellRight_List");
	}
	catch ( Exception e ) {
		message = "Error requesting well right data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
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
	
	// Year and division are needed to uniquely identify the parcel
	// collections.  However, if processing StateCU irrigation practice
	// data, the year is not required (default to negative so it can be checked later).
	
	int Div_int = Integer.parseInt ( Div );
	
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
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	else {
		// Use the years specified by the user...
		parcel_years = new int[__Year_int.length];
		System.arraycopy( __Year_int, 0, parcel_years, 0, __Year_int.length );
	}
	for ( int iparcel_year = 0; iparcel_year < parcel_years.length;	iparcel_year++ ) {
		Message.printStatus( 2, routine, "Will include division " + Div_int +
			" parcel data from " + parcel_years[iparcel_year] );
	}
	if ( (parcel_years == null) || (parcel_years.length == 0) ) {
		message = "No parcel years have been specified or are available from HydroBase - " +
			" data retrieval will be limited to explicit/aggregate wells.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "May be OK if only processing explicit wells (not full dataset?)." ) );
		// Define one parcel year to make the following loop work
		parcel_years = new int[1];
		parcel_years[0] = -999;
	}
	
	// Set up the default administration number
	
	double default_admin_number = 99999.99999; // Default
	Date default_appro_Date = null; // Matching default
	try {
		HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber ( default_admin_number );
		DateTime dt = an.getAppropriationDate();
		// This goes to day precision so time zone is irrelevant
		default_appro_Date = dt.getDate(TimeZoneDefaultType.LOCAL);
	}
	catch ( Exception e ) {
		message = "Error converting default administration number " +
		"99999.99999 to a appropriation number (should not happen).";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Initialize the default appropriation date
	
	if ( DefaultAppropriationDate != null ) {
		try {
			DateTime dt = DateTime.parse(DefaultAppropriationDate);
				HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber ( dt );
			default_admin_number = an.getAdminNumber();
			default_appro_Date = dt.getDate (TimeZoneDefaultType.LOCAL);
			Message.printStatus ( 2, routine, "If well right/permit does not have a date, then " +
			DefaultAppropriationDate + " (" +
			StringUtil.formatString(default_admin_number,"%.5f")+ ") will be used." );
		}
		catch ( Exception e ) {
			message = "Error converting date default appropriation date \"" + DefaultAppropriationDate +
			"\" to an administration number.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Should not normally be an issue - report problem to software support." ) );
		}
	}
	
	// Print to log...
	
	if ( defineWellRightHow == DefineWellRightHowType.EARLIEST_DATE ) {
		Message.printStatus ( 2, routine,
			"Rights will be defined using earliest of water right appropriation date and permit date." );
	}
	else if ( defineWellRightHow == DefineWellRightHowType.LATEST_DATE ) {
		Message.printStatus ( 2, routine,
			"Rights will be defined using latest of water right appropriation date and permit date." );
	}
	else if ( defineWellRightHow == DefineWellRightHowType.RIGHT_IF_AVAILABLE ){
		Message.printStatus ( 2, routine,
			"Rights will be defined using water right net amount appropriation date if available." );
	}
	
	// Initialize ReadWellRights boolean to increase performance
	
	boolean ReadWellRights_boolean = true;	// Default
	if ( ReadWellRights != null ) {
		if ( ReadWellRights.equalsIgnoreCase("True") ) {
			Message.printStatus ( 2, routine, "Individual well rights will be read from HydroBase." );
			ReadWellRights_boolean = true;
		}
		else if ( ReadWellRights.equalsIgnoreCase("False") ) {
			Message.printStatus ( 2, routine,
			"Individual well rights will NOT be read from HydroBase (totals for WDID will be used)." );
			ReadWellRights_boolean = false;
		}
	}
	
	// Initialize UseApex boolean to increase performance
	
	boolean UseApex_boolean = false; // Default
	if ( UseApex != null ) {
		if ( UseApex.equalsIgnoreCase("True") ) {
			Message.printStatus ( 2, routine, "APEX amount will be added to well right amount." );
			UseApex_boolean = true;
		}
		else if ( UseApex.equalsIgnoreCase("False") ) {
			Message.printStatus ( 2, routine, "APEX amount will not be added to well right amount." );
			UseApex_boolean = false;
		}
	}
	
	int OnOffDefault_int = __AppropriationDate_int;	// Default
	if ( OnOffDefault != null ) {
		if ( OnOffDefault.equalsIgnoreCase(_AppropriationDate) ) {
			Message.printStatus ( 2, routine,
				"Water right OnOff switch will be set to the year of the appropriation date." );
			OnOffDefault_int = __AppropriationDate_int;
		}
		else  {
			if ( StringUtil.isInteger(OnOffDefault) ) {
				OnOffDefault_int = StringUtil.atoi(OnOffDefault);
				Message.printStatus ( 2, routine, "Water right OnOff switch will be set to " + OnOffDefault );
			}
		}
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		StateMod_Well well = null; // StateMod well station to process
		List hbwellr_Vector = null; // List of rights from HydroBase
		List hbwellr_sorted_Vector = new Vector(100); // List of rights from HydroBase, after manual sort on admin number
		int nhbwellr = 0; // The number of rights read from HydroBase
		HydroBase_NetAmts hbwellr = null; // Single right from HydroBase
		int ir = 0; // Counter for rights in loop.
		List parts = null;
		//TODO SAM 2007-02-18 Evaluate why name not used
		//String name = "";
		
		String collection_type = null;
		String collection_part_type = null;
			// Parts used for collection.  Mainly need to key on StateMod_WellStation.
		String id = null; // Well ID for StateMod or CU location ID for StateCU.
		boolean is_collection = false; // Indicate whether the well/location is
		boolean is_aggregate = false; // an aggregate or system (each of which
		boolean is_system = false;	// will also be a collection).
			// location through the multi-part code.
		double [] irtem_array = null; // Used to sort rights in a collection.
		int [] sort_order = null; // Array used when sorting rights in a collection.
		
		int parcelYear = 0; // Used to process parcel years from HydroBase
		int parcelYear2 = 0; // Parcel year in output, may be reset if explicit wells (no parcels used)
		
		// Loop through the locations...
		int matchCount = 0; // FIXME SAM 2009-01-19 add more checks later when no matches
		for ( int i = 0; i < stationListSize; i++ ) {
			// Notify command progress listeners which station is being processed...
			notifyCommandProgressListeners ( i, stationListSize, (float)(((float)(i + 1)/(float)stationListSize)*100.0),
				"Processing well station " + i + " of " + stationListSize );
			// Use a well station for processing...
			well = (StateMod_Well)stationList.get(i);
			id = well.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Clear out the parcels saved with the well...
			//well.getParcels().removeAllElements();
			is_collection = false;
			is_aggregate = false;
			is_system = false;
			collection_type = "";	// Default...
			collection_part_type = "";	// Default...
			is_collection = well.isCollection();
			// The collection part list may vary by parcel year (although traditionally
			// D&W aggregation is constant for the period in CDSS modeling).
			// The collection type should not vary.
			if ( is_collection ) {
				collection_type = well.getCollectionType();
				collection_part_type = well.getCollectionPartType();
				if ( collection_type.equalsIgnoreCase(StateMod_Well.COLLECTION_TYPE_AGGREGATE)) {
					is_aggregate = true;
				}
				if ( collection_type.equalsIgnoreCase(StateMod_Well.COLLECTION_TYPE_SYSTEM)) {
					is_system = true;
				}
			}
			// Do a check for stations where idvcow2 indicates no ditch but collection part of Ditch is used
			if ( well.getIdvcow2().equals("") ||
				well.getIdvcow2().equalsIgnoreCase("N/A") &&
				well.getIdvcow2().equalsIgnoreCase("NA") &&
				collection_part_type.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_DITCH) ) {
		        message = "Well " + collection_type + " \"" + id + "\" is " + collection_part_type +
		        " but the associated diversion is not set - data definition is incomplete - skipping well.";
		        Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		        status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the associated diversion is set or use a part type other than " +
							StateMod_Well.COLLECTION_PART_TYPE_DITCH + "." ) );
		        continue;
			}
			// Loop through the parcel years.  Put the loop here because the
			// parts of a collection could theoretically vary by year.
			// If processing a sprinkler snapshot list file, only one year will be processed.
			for ( int iparcel_year = 0;	iparcel_year < parcel_years.length;	iparcel_year++ ) {
				// Year to process - will be -999 if only reading explicit wells
				parcelYear = parcel_years[iparcel_year];
				parcelYear2 = parcelYear;
				String yearString = "Parcel year " + parcel_years[iparcel_year] + ": ";
				
				Message.printStatus ( 2, routine, yearString + " Processing well station ID=" + id + " (" + (i + 1) +
					" of " + stationListSize + ").");
		
				hbwellr_Vector = null;	// initialize for zero-length list check below
		
				if ( is_collection && well.getCollectionPartType().equalsIgnoreCase(
					StateMod_Well.COLLECTION_PART_TYPE_PARCEL) && (parcelYear > 0) ){
					// StateMod well station that is a collection (well-only
					// stations in StateMod MUST currently be collections)
					// OR a StateCU Location that is a collection of parcels
					// (and therefore a well-only location)...
					Message.printStatus ( 2, routine, yearString + Loctype + " \"" + id +
					"\" is associated with a collection of parcels..." );
					// Aggregate or system, by parcel...
					parts = well.getCollectionPartIDs(parcelYear);
					try {
						hbwellr_Vector = readHydroBaseWellRightsForParcelList (
							hbdmi,
							well,
							id,
							Loctype,
							parts,
							null,		// No percent_yield for wells (1.0 always)
							null,		// No ditch aggregate part id
							//-1,			// No ditch aggregate part count
							//-1,			// No ditch aggregate number of parts
							parcelYear, yearString,
							Div_int,
							defineWellRightHow,
							ReadWellRights_boolean,
							UseApex_boolean,
							default_admin_number,
							default_appro_Date,
							warningLevel, warning_count, command_tag, status, cacheHydroBase );
					}
					catch ( Exception e ) {
						message = yearString + "Unexpected error querying HydroBase (" + e + ").";
						Message.printWarning(3, routine, e);
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
						hbwellr_Vector = null;  // handled below
					}
				}
				else if ( !well.getIdvcow2().equals("") &&
					!well.getIdvcow2().equalsIgnoreCase("N/A") &&
					!well.getIdvcow2().equalsIgnoreCase("NA") && (parcelYear > 0) ) {
					// StateMod well that is associated with a diversion
					// and the diversion may or may not be an aggregate.
					Message.printStatus ( 2, routine, yearString + Loctype + " \"" + id + "\" is associated with a " +
						"diversion.  Determining associated parcels, and then wells..." );
					// Get the well station parts...
					if ( well.isCollection() ) {
						collection_type = well.getCollectionType();
						Message.printStatus ( 2, routine, "Well \"" + well.getID() + "\" is a " +
							collection_type + "...processing each part...");
						// Diversion aggregates are only set once (year is ignored)
						parts = well.getCollectionPartIDs ( 0 );
					}
					else {
						// To reuse code below, just use a single part...
						collection_type = "(explicit)";
						// TODO SAM 2006-01-31
						//name = div.getName();
						//name = well.getName();
						parts = new Vector();
						parts.add ( well.getID() );
						Message.printStatus ( 2, routine, yearString + "Well \"" + well.getID() +
							"\" is associated with a an explicit diversion (no aggregate/system specified)..." +
							"processing as system with one ditch part...");
					}
					hbwellr_Vector = readHydroBaseWellRightsForDiversionWDIDList (
						hbdmi,
						well,
						id,
						Loctype,
						collection_type,
						parts,
						parcelYear, yearString,
						Div_int,
						defineWellRightHow,
						ReadWellRights_boolean,
						UseApex_boolean,
						default_admin_number,
						default_appro_Date,
						warningLevel, warning_count, command_tag, status, cacheHydroBase );
				}
				else if ( is_collection &&
					well.getCollectionPartType().equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_WELL) &&
					(iparcel_year == 0) ){
					// StateMod well station that is a collection of wells - only read for first year since rights
					// will apply for the full period...
					Message.printStatus ( 2, routine, yearString + Loctype + " \"" + id +
					"\" is associated with a collection of wells - processing one time..." );
					// Aggregate or system, by parcel...
					parts = well.getCollectionPartIDs(parcelYear);
					try {
						hbwellr_Vector = readHydroBaseWellRightsForWellStationList (
							hbdmi,
							id,
							is_collection,
							parts,
							ReadWellRights_boolean,
							UseApex_boolean,
							default_admin_number,
							default_appro_Date,
							warningLevel, warning_count, command_tag, status);
						// Reset the parcel year to be used in final water rights
						parcelYear2 = -999;
					}
					catch ( Exception e ) {
						message = yearString + "Unexpected error querying HydroBase (" + e + ").";
						Message.printWarning(3, routine, e);
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( command_tag, ++warning_count),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
						hbwellr_Vector = null;  // handled below
					}
				}
				else if ( !is_collection ) {
					if ( iparcel_year == 0 ) {
						// Single well - get its water rights.  Only do for the first year read because in this
						// case there is no relation to parcels or parcel years...
						if ( iparcel_year == 0 ) {
							Message.printStatus ( 2, routine, yearString + "Well \"" + id + "\" is explicitly modeled - " +
								"getting water rights for the individual well - processing once." );
							hbwellr_Vector = readHydroBaseWellRightsForWellStation (
									hbdmi,
									id,
									is_collection,
									parts,
									Div_int,
									ReadWellRights_boolean,
									UseApex_boolean,
									default_admin_number,
									default_appro_Date,
									warningLevel, warning_count, command_tag, status );
							// Reset the parcel year to be used in final water rights
							parcelYear2 = -999;
						}
					}
				}
				else {
					// Fall through case is not being considered somehow.  This may be an input error
					// bug checks should always alert users to this condition so that they can fix it.  Otherwise,
					// water rights may not be read.
					message = yearString + "unhandled configuration for well \"" + id + "\".";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that definition of well station is correct for water right read." ) );
					hbwellr_Vector = null; 
				}
		
				// This applies to well-only collection and D&W...
		
				if ( is_collection || !well.getIdvcow2().equals("") &&
					!well.getIdvcow2().equalsIgnoreCase("N/A") &&
					!well.getIdvcow2().equalsIgnoreCase("NA")) {
					// If a system or aggregate, sort the water rights by administration
					// number.  This will make sure that the insert order is by administration number.
					// For non-collection locations, the sort is done in the database query.
					nhbwellr = 0;
					if ( hbwellr_Vector != null ) {
						nhbwellr = hbwellr_Vector.size();
					}
					if ( (is_aggregate || is_system) && (nhbwellr > 0) ) {
						irtem_array = new double[nhbwellr];
						for ( ir = 0; ir < nhbwellr; ir++ ) {
							hbwellr = (HydroBase_NetAmts)hbwellr_Vector.get(ir);
							irtem_array[ir] = hbwellr.getAdmin_no();
						}
						sort_order = new int[nhbwellr];
						MathUtil.sort ( irtem_array, MathUtil.SORT_QUICK,
							MathUtil.SORT_ASCENDING, sort_order, true );
						hbwellr_sorted_Vector.clear();
						for ( ir = 0; ir < nhbwellr; ir++ ) {
							hbwellr_sorted_Vector.add ( hbwellr_Vector.get(sort_order[ir]) );
						}
						hbwellr_Vector = hbwellr_sorted_Vector;
					}
				}
				// Still within a parcel year.
				//
				// Process the water rights from HydroBase_NetAmts to
				// StateMod_WellRight.  Save the parcel year information so that
				// merging of rights can occur later...
			
				nhbwellr = 0;
				if ( hbwellr_Vector != null ) {
					nhbwellr = hbwellr_Vector.size();
				}
				message = yearString + Loctype + " \"" + id + "\" has "
				+ nhbwellr + " HydroBase rights to format for output.";
				Message.printStatus ( 2, routine, message );
				
				addHydroBaseRightsToStateModWellRights ( 
					Loctype,
					id,
					hbwellr_Vector,
					DecreeMin,
					DecreeMin_double,
					IDFormat_int,
					parcelYear2, // Will be zero if explicit well
					yearString,
					is_collection,
					collection_part_type,
					OnOffDefault_int,
					rightList,
					SMWellRight_match_Vector,
					warningLevel, warning_count, command_tag, status );
			} // End parcel year
		} // End location
		
		// TODO SAM 2007-05-24 Evaluate how this works with multiple years.
		
		StateDMI_Util.warnAboutDataMatches ( toString(), true, SMWellRight_match_Vector, "Well Rights" );
	}
    catch ( Exception e ) {
        message = "Unexpected error reading well rights from HydroBase (" + e + ").";
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
	String IDFormat = parameters.getValue ( "IDFormat" );
	String Year = parameters.getValue( "Year" );
	String Div = parameters.getValue( "Div" );
	String DecreeMin = parameters.getValue ( "DecreeMin" );
	String DefaultAppropriationDate = parameters.getValue( "DefaultAppropriationDate" );
	String DefineRightHow = parameters.getValue( "DefineRightHow" );
	String ReadWellRights = parameters.getValue( "ReadWellRights" );
	String UseApex = parameters.getValue( "UseApex" );
	String OnOffDefault = parameters.getValue( "OnOffDefault" );
	String Optimization = parameters.getValue( "Optimization" );
	
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( IDFormat != null && IDFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDFormat=\"" + IDFormat + "\"" );
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
	if ( DecreeMin != null && DecreeMin.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DecreeMin=" + DecreeMin );
	}
	if ( DefaultAppropriationDate != null && DefaultAppropriationDate.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DefaultAppropriationDate=\"" + DefaultAppropriationDate + "\"" );
	}
	if ( DefineRightHow != null && DefineRightHow.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DefineRightHow=" + DefineRightHow );
	}
	if ( ReadWellRights != null && ReadWellRights.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReadWellRights=" + ReadWellRights );
	}
	if ( UseApex != null && UseApex.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseApex=" + UseApex );
	}
	if ( OnOffDefault != null && OnOffDefault.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffDefault=" + OnOffDefault );
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