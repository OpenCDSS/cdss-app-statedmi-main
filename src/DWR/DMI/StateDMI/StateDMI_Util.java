package DWR.DMI.StateDMI;

import java.util.Date;
import java.util.List;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;
import RTi.DMI.DMIUtil;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Utility methods for manipulating StateCU and StateMod data.  These
methods are being split out of StateDMI_Processor as commands are split into separate classes.
*/
public abstract class StateDMI_Util
{

//Used when inserting objects in lists...

/**
Warn if a data item is matched (do not change data).
*/
protected static final int _IF_MATCH_REPLACE = 1;
// TODO SAM 2007-02-18 Evaluate if needed
//private final int IF_MATCH_USE_OLD = 2; // Use old data item if a match occurs.

/**
Append/insert data even if a match is found.
*/
protected static final int _IF_MATCH_APPEND = 3;
protected static final int _IF_MATCH_MERGE = 4; // Merge data if records match.

/**
Warning level for informative warnings, but which likely will not result in data
issues (i.e., the user probably wanted the result).
*/
protected static final int _FYI_warning_level = 3;

/**
Add a StateMod_WellRight instance to the __SMWellRight_Vector.  If
an existing instance is found (checking the ID), it is optionally replaced
and added to the __SMWellRight_match_Vector
so that a warning can be printed using warnAboutDataMatches().
@param SMWellRight_Vector list of StateMod_WellRight to add to.
@param SMWellRight_match_Vector list of String identifiers indicating that
data was attempted to be reset for an ID that already had data.
@param divr StateMod_WellRight instance to be added.
@param ifMatch If IF_MATCH_REPLACE, an existing instance is replaced if found.
If IF_MATCH_USE_OLD, the original instance is used.  If IF_MATCH_APPEND, append the duplicate record.
*/
protected static void findAndAddSMWellRight ( List SMWellRight_Vector,
		List SMWellRight_match_Vector, StateMod_WellRight wellr, int ifMatch )
{	String id = wellr.getID(), routine = "StateDMI_Processor.findAndAddSMWellRight";

	int pos = StateMod_Util.indexOf( SMWellRight_Vector, id );
	boolean insert = false;	// Should record be inserted/appended
	StateMod_WellRight wer = null;
	if ( pos >= 0 ) {
		if ( ifMatch == _IF_MATCH_REPLACE ) {
			Message.printStatus ( 2, routine, "Replacing matched well right \"" + wellr.getID() + "\"." );
			SMWellRight_Vector.set ( pos, wellr );
			// The StateMod_WellRight is already in the list...
			SMWellRight_match_Vector.add(id);
		}
		else if ( ifMatch == _IF_MATCH_APPEND ) {
			insert = true;
		}
		else if ( ifMatch == _IF_MATCH_MERGE ) {
			// Determine whether the matched record has the same right ID, station ID, admin number,
			// and on/off switch.  If so, then add the data to the existing record.  If not,
			// append a new record.  Because only the right ID was previously matched, need to search
			// the data again, starting with the found position.
			int size = SMWellRight_Vector.size();
			int pos2 = -1;
			for ( int i = pos; i < size; i++ ) {
				wer = (StateMod_WellRight)
				SMWellRight_Vector.get(i);
				if ( wellr.getID().equalsIgnoreCase( wer.getID())
					&& wellr.getCgoto().equalsIgnoreCase( wer.getCgoto())
					&& wellr.getIrtem().equalsIgnoreCase( wer.getIrtem())
					&& (wellr.getSwitch()==wellr.getSwitch())){
					pos2 = i; // found match.
					break;
				}
			}
			if ( pos2 >= 0 ) {
				wer = (StateMod_WellRight)SMWellRight_Vector.get(pos2);
				wer.setDcrdivw ( wer.getDcrdivw() + wellr.getDcrdivw() );
				Message.printStatus ( 2, routine,
				"Incrementing decree for matched well right \"" + wellr.getID() + "\" to " +
				StringUtil.formatString(wer.getDcrdivw(),"%.2f"));
			}
			else {
				// Just add as a new right...
				insert = true;
			}
		}
		// Else leave old data.
	}
	else {
		insert = true;
	}
	if ( insert ) {
		// Add in sorted order...
		pos = StateMod_Util.findWaterRightInsertPosition( SMWellRight_Vector, wellr );
		if ( pos < 0 ) {
			// Insert at the end...
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, "Cannot determine insert position for right \""
						+ wellr.getID() + "\" adding at end." );
			}
			SMWellRight_Vector.add ( wellr );
		}
		else {
			// Do the insert at the given location...
			if ( Message.isDebugOn ) {
				Message.printStatus ( 2, routine, "Adding well right right \""
						+ wellr.getID() + "\" at position[" + pos + "]." );
			}
			SMWellRight_Vector.add ( pos, wellr );
		}
	}
}

/**
Indicate if a well/parcel class is for an estimated well.  This is indicated with ":PE" for estimated
permit and ":WE" for estimated well WDID on the end of well identifiers.
*/
public static boolean isParcelClassForEstimatedWell ( int parcelClass )
{
	if ( (parcelClass == 4) || (parcelClass == 9) ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Process a single parcel's data and add to the irrigation practice
time series.  This method is called when processing actual parcels and user-supplied supplemental data.
@param id Location identifier (the main ID, not the aggregate/system part).
@param ipyts StateCU_IrrigationPracticeTS to which parcel data are added.
@param parcel_id the identifier for the parcel, for the given year.
@param crop The crop name, used for IPY as FYI only.
@param area The parcel area, acres.
@param irrig_method The irrigation method (e.g., "Flood" or "Sprinkler").  Sprinkler
and Drip are added to the high-efficiency "Sprinkler" acreage.
@param parcel_year Calendar year for parcel data.
@param has_gw_supply Indicates whether the parcel has groundwater supply (True) or is
surface water only supply (False).
*/
public static void processIrrigationPracticeTSParcel (
		String id,
		StateCU_IrrigationPracticeTS ipyts,
		int parcel_id,
		String crop,
		double area,
		String irrig_method,
		int parcel_year,
		boolean has_gw_supply )
{	String routine = "StateDMI_Util.processIrrigationPracticeTSParcel";
	// Transfer the parcel data into the IrrigationPracticeTS
	
	boolean is_high_efficiency = false;
	// TODO SAM 2007-06-06 Evaluate whether high efficiency irrigation
	// methods should be specified as a command parameter
	if ( irrig_method.equalsIgnoreCase("SPRINKLER") || irrig_method.equalsIgnoreCase("DRIP") ) {
		is_high_efficiency = true;
	}
	
	if ( area <= 0.0 ) {
		// No need to process...
		return;
	}
	
	// Total acres are always incremented.
	if ( ipyts.getTacre(parcel_year) < 0.0 ) {
		ipyts.setTacre ( parcel_year, area );
	}
	else {
		ipyts.setTacre ( parcel_year, ipyts.getTacre(parcel_year) + area );
	}
	Message.printStatus( 2, routine, "For location " + id + " year " + parcel_year +
		" parcel_id=" + parcel_id + " added " + StringUtil.formatString(area,"%.2f") + " to total area, result = " +
		StringUtil.formatString(ipyts.getTacre(parcel_year),"%.2f") );

	if ( has_gw_supply ) {
		// Has groundwater supply...
		if ( is_high_efficiency ) {
			if ( ipyts.getAcgwspr(parcel_year) < 0.0 ) {
				ipyts.setAcgwspr ( parcel_year, area );
			}
			else {
				ipyts.setAcgwspr ( parcel_year, ipyts.getAcgwspr(parcel_year) + area );
			}
			Message.printStatus( 2, routine, "For location " + id + " year " + parcel_year +
				" parcel_id=" + parcel_id +" added " + StringUtil.formatString(area,"%.2f") + " to GW sprinkler area, result = " +
				StringUtil.formatString(ipyts.getAcgwspr(parcel_year),"%.2f") );
		}
		else {
			if ( ipyts.getAcgwfl(parcel_year) < 0.0 ) {
				ipyts.setAcgwfl ( parcel_year, area );
			}
			else {
				ipyts.setAcgwfl ( parcel_year, ipyts.getAcgwfl(parcel_year) + area );
			}
			Message.printStatus( 2, routine, "For location " + id + " year " + parcel_year +
				" parcel_id=" + parcel_id +" added " + StringUtil.formatString(area,"%.2f") + " to GW flood area, result = " +
				StringUtil.formatString(ipyts.getAcgwfl(parcel_year),"%.2f") );
		}
	}
	else {
		// Has surface water only supply so add to SW acres...
		if ( is_high_efficiency ) {
			if ( ipyts.getAcswspr(parcel_year) < 0.0 ) {
				ipyts.setAcswspr ( parcel_year, area );
			}
			else {
				ipyts.setAcswspr ( parcel_year, ipyts.getAcswspr(parcel_year) + area );
			}
			Message.printStatus( 2, routine, "For location " + id + " year " + parcel_year +
				" parcel_id=" + parcel_id +" added " + StringUtil.formatString(area,"%.2f") + " to SW sprinkler area, result = " +
				StringUtil.formatString(ipyts.getAcswspr(parcel_year),"%.2f") );
		}
		else {
			if ( ipyts.getAcswfl(parcel_year) < 0.0 ) {
				ipyts.setAcswfl ( parcel_year, area );
			}
			else {
				ipyts.setAcswfl ( parcel_year, ipyts.getAcswfl(parcel_year) + area );
			}
			Message.printStatus( 2, routine, "For location " + id + " year " + parcel_year +
				" parcel_id=" + parcel_id +" added " + StringUtil.formatString(area,"%.2f") + " to SW flood area, result = " +
				StringUtil.formatString(ipyts.getAcswfl(parcel_year),"%.2f") );
		}
	}
	
	// Ensure that in a year that any data value is specified, no missing values will remain.  Zeros can be
	// added to with other commands.  Also recompute the totals by source.
	
	if ( ipyts.getAcgwfl(parcel_year) < 0.0 ) {
		ipyts.setAcgwfl ( parcel_year, 0.0 );
	}
	if ( ipyts.getAcgwspr(parcel_year) < 0.0 ) {
		ipyts.setAcgwspr ( parcel_year, 0.0 );
	}
	ipyts.refreshAcgw(parcel_year);
	if ( ipyts.getAcswfl(parcel_year) < 0.0 ) {
		ipyts.setAcswfl ( parcel_year, 0.0 );
	}
	if ( ipyts.getAcswspr(parcel_year) < 0.0 ) {
		ipyts.setAcswspr ( parcel_year, 0.0 );
	}
	ipyts.refreshAcsw(parcel_year);
	if ( ipyts.getTacre(parcel_year) < 0.0 ) {
		ipyts.setTacre ( parcel_year, 0.0 );
	}
	
	// Save information for checks and filling to be done later, for example when filling with water rights...
	StateCU_Parcel cu_parcel = null;
	cu_parcel = new StateCU_Parcel();
	cu_parcel.setID ( "" + parcel_id );
	cu_parcel.setCrop (	crop );
	cu_parcel.setArea ( area );	// IMPORTANT - this already includes percent_irrig - no need to adjust later
	cu_parcel.setYear ( parcel_year );
	cu_parcel.setIrrigationMethod ( irrig_method );
	// TODO SAM 2007-06-08 Evaluate whether all rights should be added...
	StateCU_Supply supply = new StateCU_Supply();
	supply.setIsGroundWater( has_gw_supply );
	// Add a single supply to the parcel
	cu_parcel.addSupply ( supply );
	// Add to parcels...
	ipyts.addParcel ( cu_parcel );
}

/**
Read the list of parcel years from HydroBase.
@param hdmi HydroBaseDMI instance for queries.
@param Div_int integer division to process.
@return an array of integer years for which parcel data exist in HydroBase.
*/
public static int [] readParcelYearListFromHydroBase ( HydroBaseDMI hdmi, int Div_int )
throws Exception
{	// TODO SAM 2007-05-25 Check HydroBase version for the following
	// If not found, read all and filter out the parcel year of interest
	List v = hdmi.readParcelUseTSDistinctCalYearsList(Div_int);
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	int [] years = new int[v.size()];
	for ( int i = 0; i < years.length; i++ ) {
		years[i] = ((Integer)v.get(i)).intValue();
	}
	
	/* TODO SAM 2007-05-23 Code should not be needed with current HydroBase
	 * but is needed with old HydroBase.

					// FIXME SAM 2007-03-23 This is a performance hit - need to fix.
					// TODO SAM 2004-09-21 - could do this
					// with new database queries but we have already
					// given Doug Stenzel a list of stored
					// procedures so do a little more work here
					// using the existing methods.
					// For each parcel, get the associated wells.
					// Before doing so, throw out duplicate parcels
					// and those not for the requested year...
					nparcel = 0;
					if ( hbparcel_structure_Vector != null ) {
						nparcel =
							hbparcel_structure_Vector.size();
					}
					for( iparcel = 0; iparcel < nparcel; iparcel++){
						hbparcel_structure =
							(HydroBase_ParcelUseTSStructureToParcel)
							hbparcel_structure_Vector.elementAt (
									iparcel );
						if ( hbparcel_structure.getCal_year() !=
							parcel_year ) {
							hbparcel_structure_Vector.
							removeElementAt(iparcel--);
							--nparcel;
						}
					}
					
					*/
	
	return years;
}
	
/**
Helper method to process well rights from HydroBase, when reading from the
"wells" table.  A single wells/well_to_parcel object is processed, resulting in
one or more HydroBase_NetAmts objects being returned. This method handles
whether to use well rights or permits for the returned data.
@param hdmi HydroBaseDMI instance for queries.
@param command_tag Tag to use when printing messages.
@param routine Routine to use when printing messages.
@param warning_count Warning count to use when printing messages.  Increments
in the warning count is handled by passing the updated value back to the calling code.
@param id Well station identifier that is being processed.
@param hbwell_parcel wells/well_to_parcel join object, from HydroBase.  If null, read the well right/permit
data directly using the well station ID, rather than using the ID information from the well to parcel object.
@param parcel_id parcel number (from original aggregation data).
@param parcel_fraction Fraction of a parcel's area that is irrigated by the ditch
being processed (<= 1.0).  A value != 1.0 indicates that more than one ditch irrigate a parcel.
@param defineWellRightHow _EarliestDate to define the StateMod right using the
earliest of the well right appropriation date and permit date;
_LatestDate to define the StateMod right using the
latest of the well right appropriation date and permit date;
_RightIfAvailable If true, define the StateMod right using
the well right appropriation date, if the well right is available.  Otherwise use the permit date.
@param ReadWellRights_boolean If true, query the water rights.  If false, use the data in the wells table.
@param UseApex_boolean If true, add the APEX data values to the net amount right
value. If false, do not add APEX.
@param default_admin_number Default administration number to use if a date
cannot be determined from the data.
@param hbwellr_Vector The list to return right(s) in.  The right(s) are
transferred to the main list in the calling code.
@return the number of errors that were found during processing.
@exception Exception Pass errors to the calling code.
*/
protected static int readWellRightsFromHydroBaseHelper (
	HydroBaseDMI hdmi,
	String commandTag,	int warningLevel, int warningCount, CommandStatus status,
	String routine,
	int parcel_year,
	String id,
	HydroBase_Wells hbwell_parcel,
	int parcel_id,
	double parcel_fraction,
	DefineWellRightHowType defineWellRightHow,
	boolean ReadWellRights_boolean,
	boolean UseApex_boolean,
	double default_admin_number,
	Date default_appro_Date,
	List hbwellr_Vector )
	throws Exception
{	String message; // For messages
	HydroBase_NetAmts hbwellr = null; // Single right from HydroBase
	HydroBase_AdministrationNumber permit_adminnum;
	boolean use_right = false;	// Use the water right information
	boolean use_permit = false;	// Use the water permit information
	List hbwellr_part_Vector = null; // List of rights from HydroBase
	Date right_Date = null;
	if ( hbwell_parcel != null ) {
	     right_Date = hbwell_parcel.getAppr_date(); // Appropriation date in hbwell_parcel, as Date
	}
	DateTime right_DateTime; // Appropriation date in hbwell_parcel, as DateTime
	Date permit_Date = null;
	if ( hbwell_parcel != null ) {
		permit_Date = hbwell_parcel.getPerm_date(); // Permit date in hbwell_parcel, as Date
	}
	DateTime permit_DateTime; // Permit date in hbwell_parcel, as DateTime
	Date appro_Date = null; // The final appropriation date in the HydroBase object.
	double yield = 0.0; // Well yield from "wells" record.
	double yield_apex = 0.0; // Well yield_apex from "wells" record
	double decree = 0.0; // Final decree in HydroBase object.
	double apex = 0.0; // Net rate APEX (alternate point/ exchange) decree value.
	double admin_number = 0.0; // Final administration number in HydroBase object.
	String right_name = ""; // Final right name in HydroBase object.
	String right_id = ""; // Final common identifier for right, either a WDID or permit information,
					// depending on the data used for the right.
	HydroBase_AdministrationNumber appro_adminnum;
					// Administration number object used to convert from dates.
	// Clear out the list to return...
	hbwellr_Vector.clear();
	// The initial decision about whether to use water right or water permit
	// data is determined based on the DefineRightHow flag in the
	// ReadWellRightsFromHydroBase() command...
	//
	// The following checks for hbwell_parcel != null are used when dealing with parcel data.
	// If reading well right/permit data directly, use the object will be null.
	if ( (hbwell_parcel != null) && (defineWellRightHow == DefineWellRightHowType.EARLIEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// earliest of the well right appropriation date or permit date...
		if ( (right_Date != null) && (permit_Date == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using right because no " +
			"corresponding permit date is available." );
			use_right = true;
		}
		else if ( (right_Date == null) && (permit_Date != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using permit because no " +
			"corresponding right date is available." );
			use_permit = true;
		}
		else if ( (right_Date == null) && (permit_Date == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwell_parcel.getWD() > 0) && (hbwell_parcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwell_parcel.getWD(), hbwell_parcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because have WDID but no right or permit date." );
				use_right = true;
			}
			else {
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				use_permit = true;
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permit_Date.before(right_Date) ) {
				// Permit date is earliest...
				use_permit = true;
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because date is earlier than right date." );
			}
			else {
				use_right = true;
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because date is earlier than permit date." );
			}
		}
	}
	else if ( (hbwell_parcel != null) && (defineWellRightHow == DefineWellRightHowType.LATEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// latest of the well right appropriation date or permit date...
		if ( (right_Date != null) && (permit_Date == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using right because no " +
			"corresponding permit date is available." );
			use_right = true;
		}
		else if ( (right_Date == null) && (permit_Date != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using permit because no " +
			"corresponding right date is available." );
			use_permit = true;
		}
		else if ( (right_Date == null) && (permit_Date == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwell_parcel.getWD() > 0) && (hbwell_parcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwell_parcel.getWD(), hbwell_parcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because have WDID but no right or permit date." );
				use_right = true;
			}
			else {
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				use_permit = true;
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permit_Date.after(right_Date) ) {
				// Permit date is latest...
				use_permit = true;
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because date is later than right date." );
			}
			else {
				use_right = true;
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because date is later than permit date." );
			}
		}
	}
	else if ( (hbwell_parcel != null) && (defineWellRightHow == DefineWellRightHowType.RIGHT_IF_AVAILABLE) ) {
		if ( (hbwell_parcel.getWD()>0) && (hbwell_parcel.getID() > 0) ){
			// Use the water right information...
			use_right = true;
			if ( !ReadWellRights_boolean ) {
				Message.printStatus ( 2, routine, "Using right data because it is available." );
			}
		}
		else {
			// Use the permit information...
			use_permit = true;
			Message.printStatus ( 2, routine, "Using permit because right is not available." );
		}
	}
	else if ( hbwell_parcel != null ){
		// DefineRightHow in calling code is not defined - should not get to this code...
		message = "DefineRightHow value (" + defineWellRightHow + ") is not handled";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify processing parameters - contact software support." ) );
		throw new IllegalArgumentException(message);
	}
	if ( ((hbwell_parcel != null) && use_right && ReadWellRights_boolean ) || // Reading parcel/well data
		(hbwell_parcel == null) && HydroBase_WaterDistrict.isWDID(id) ) { // Reading an individual well WDID
		// TODO SAM 2009-03-29 Evaluate here - should use_right be checked before querying for rights?
		// Re-query the rights to get the basic data...
		// A well structure and a query is requested so query the water rights ...
		try {
			if ( hbwell_parcel != null ) {
				// Get the WDID from the well/parcel supply information...
				hbwellr_part_Vector = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, hbwell_parcel.getWD(), hbwell_parcel.getID(), false, null );
			}
			else {
				// Get the WDID from the passed in well station ID.
				int [] wdid_parts = new int[2];
				// Should parse since checked above.
				wdid_parts = HydroBase_WaterDistrict.parseWDID ( id );
				hbwellr_part_Vector = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
			}
			// Loop through the returned data and add to returned
			// vector.  Adjust the amounts by ditch coverage area, if appropriate.
			int nhbwellr_part = 0;
			if ( hbwellr_part_Vector != null ) {
				nhbwellr_part = hbwellr_part_Vector.size();
			}
			if ( (hbwell_parcel != null) && nhbwellr_part == 0 ) {
				// Expecting data since the parcel match indicated so
				message = "No net amount rights data from HydroBase found for " +
				HydroBase_WaterDistrict.formWDID (hbwell_parcel.getWD(), hbwell_parcel.getID() ) +
				", " + hbwell_parcel.getWell_name() + " using " + parcel_year + " parcel data.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well used for parcel supply has net amount " +
							"water rights (can change over time but HydroBase has only the latest total)." ) );
				return warningCount;
			}
			else {
				if ( hbwell_parcel != null ) {
					Message.printStatus ( 2, routine, "Read " + nhbwellr_part + " well rights for " +
					HydroBase_WaterDistrict.formWDID ( hbwell_parcel.getWD(), hbwell_parcel.getID()));
				}
				else {
					Message.printStatus ( 2, routine, "Read " + nhbwellr_part + " well rights for \"" + id + "\"");
				}
			}
			for ( int ihb = 0; ihb < nhbwellr_part; ihb++ ) {
				// Adjust the decree based on the amount of the well's yield that serves the parcel...
				hbwellr = (HydroBase_NetAmts)hbwellr_part_Vector.get(ihb);
				decree = hbwellr.getNet_rate_abs();
				apex = hbwellr.getNet_rate_apex();
				if ( UseApex_boolean && (apex > 0.0) ) {
					if ( DMIUtil.isMissing(decree) ) {
						decree = apex;
					}
					else {
						decree += apex;
					}
				}
				if ( !DMIUtil.isMissing( decree ) ) {
					// Reset the decree to consider the APEX and adjusted for ditch and well percent...
					if ( hbwell_parcel == null ) {
						hbwellr.setNet_rate_abs(decree * 1.0 * parcel_fraction);
					}
					else {
						hbwellr.setNet_rate_abs(decree * hbwell_parcel.getPercent_yield() * parcel_fraction);
					}
					// Make sure the units are "C" for CFS so that aggregation will work...
					hbwellr.setUnit ( "C" );
				}
				// Set information used elsewhere.  The "common ID" is always the WDID.
				// The water right name was set in the query.
				if ( hbwell_parcel == null ) {
					hbwellr.setCommonID ( id );
				}
				else {
					// Set the well ID to the WDID.
					hbwellr.setCommonID (
					HydroBase_WaterDistrict.formWDID ( hbwell_parcel.getWD(), hbwell_parcel.getID() ) );
				}
				// Set the water right class and parcel_id for help in other processing...
				if ( hbwell_parcel != null ) {
					hbwellr.setParcelMatchClass ( hbwell_parcel.get_Class() );
				}
				hbwellr.setParcelID ( parcel_id );
				// Now add the individual right to the list...
				hbwellr_Vector.add ( hbwellr );
			}
			// Now return (results will be in hbwellr_Vector)...
			return warningCount;
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			if ( hbwell_parcel != null ) {
				message = "Unexpected error getting net amount rights data from HydroBase for " +
				HydroBase_WaterDistrict.formWDID ( hbwell_parcel.getWD(), hbwell_parcel.getID() ) +
				", " + hbwell_parcel.getWell_name() + " (" + e + ").";
			}
			else {
				message = "Unexpected error getting net amount rights data from HydroBase for \"" + id +
				"\" (" + e + ").";
			}
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file - report the problem to software support." ) );
			return warningCount;
		}
	}
	else {
		// This code is executed if:
		//
		// 1) Not requerying well rights (even if a WDID)
		// 2) Reading a well permit
		//
		// The well data produced by the well/parcel matching processing are used, which can result in some
		// aggregated information for WDIDs
		if ( hbwell_parcel == null ) {
			message = "The explicit well ID \"" + id + "\" contains characters and is therefore assumed to be a well " +
				"permit - however, the ability to read explicit permits is not implemented.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify the well ID in input.  " +
						"This may be OK if it is expected that the ID has no data in HydroBase." ) );
			// Warning count is returned below.
		}
		else {
			// Can process the well/parcel data
			// Use data from the wells table, which contains rights and permits...
			// FIXME SAM 2009-02-24 Need to evaluate when reading well permits directly
			yield = hbwell_parcel.getYield(); // GPM
			yield_apex = hbwell_parcel.getYield_apex(); // GPM
			if ( UseApex_boolean ) {
				// Add the Apex to the yield.  If the yield is zero, then
				// the Apex will be the amount shown in the right.
				if ( yield < 0.0 ) {
					// Yield was missing...
					yield = yield_apex;
				}
				else {
					// Add the Apex...
					yield += yield_apex;
				}
			}
			if ( DMIUtil.isMissing( yield) ) {
				// No well yield so return...
				// Print a non-fatal warning (don't know if HydroBase data will always be good...
				// Non-fatal...
				if ( UseApex_boolean ) {
					message = "Well yield for parcel \"" + parcel_id +
					"\", year " + parcel_year + " is missing.  Output may be incomplete.";
				}
				else {
					message = "Well yield+yield_apex for parcel \"" + parcel_id +
					"\", year " + parcel_year + " is missing.  Output may be incomplete.";
				}
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that data in HydroBase are complete." ) );
				return warningCount;
			}
			if ( DMIUtil.isMissing( hbwell_parcel.getPercent_yield()) ) {
				// No well percent_yield so return...
				// Print a non-fatal warning (don't know if HydroBase
				// data will always be good...
				// Non-fatal...
				message = "Well percent yield for parcel \"" + parcel_id +
				"\", year " + parcel_year + " is missing.  Output may be incomplete.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				return warningCount;
			}
			// Convert the yield from GPM to CFS and prorate the yield
			// according to the fraction provided to the parcel...
			decree = (yield + yield_apex)*.002228	// GPM to CFS
				*hbwell_parcel.getPercent_yield()
				*parcel_fraction;	// Will only be different from 1.0 when processing ditches
			if ( use_right ) {
				// Use the right name...
				/* TODO SAM	As per Ray Bennett just use name...
				right_name = HydroBase_WaterDistrict.formWDID (
					hbwell_parcel.getWD(), hbwell_parcel.getID() ) +
					", " + hbwell_parcel.getWell_name();
				*/
				right_name = hbwell_parcel.getWell_name();
				if ( right_name.equals("") ) {
					right_name = HydroBase_WaterDistrict.formWDID (
					hbwell_parcel.getWD(), hbwell_parcel.getID() ) + ", " + hbwell_parcel.getWell_name();
				}
				// Use the right appropriation date (set at the top of this method)...
				if ( DMIUtil.isMissing( right_Date) ) {
					// No date so use the user-specified value or the default...
					admin_number = default_admin_number;
					appro_Date = default_appro_Date;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcel_id +
					"\", year " + parcel_year + " right appropriation date is missing - using default " +
					appro_Date );
				}
				else {
					// Convert the right date to an administration number...
					right_DateTime = new DateTime( right_Date);
					appro_adminnum = new HydroBase_AdministrationNumber( right_DateTime, null );
					admin_number = appro_adminnum.getAdminNumber();
					// Save the date in the appropriation date...
					appro_Date = right_Date;
				}
				// Use a 7-digit formatted WDID for rights...
				right_id = HydroBase_WaterDistrict.formWDID ( hbwell_parcel.getWD(), hbwell_parcel.getID() );
				// Estimated wells need to be clearly identified because they are handled in the merge.
				if ( (hbwell_parcel != null) && isParcelClassForEstimatedWell(hbwell_parcel.get_Class()) ) {
					right_id = right_id + ":WE";
				}
			}
			else if ( use_permit ) {
				// Use the permit information for the name...
				/* TODO 2006-04-24 As per Ray Bennett, just use right name...
				right_name = "P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
				*/
				if ( hbwell_parcel != null ) {
					right_name = hbwell_parcel.getWell_name();
				}
				else {
					right_name = ""; // FIXME SAM 2009-02-24 Decide what to use
				}
				if ( right_name.equals("") ) {
					/* TODO SAM 2006-05-15 As per Ray Bennett discussion, use the receipt.
					right_name =
					"P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
					*/
					if ( hbwell_parcel != null ) {
						right_name = hbwell_parcel.getReceipt();
					}
					else {
						right_name = ""; // FIXME SAM 2009-02-24 Decide what to use
					}
				}
				// Use the permit date (set at the top of this method)...
				if ( DMIUtil.isMissing( permit_Date) ) {
					// No date so use the user-specified value or the default...
					admin_number = default_admin_number;
					appro_Date = default_appro_Date;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcel_id +
						"\", year " + parcel_year + " permit date is missing - using default " +
						appro_Date );
				}
				else {
					// Convert the permit date to an administration number...
					permit_DateTime = new DateTime( permit_Date);
					permit_adminnum = new HydroBase_AdministrationNumber( permit_DateTime, null );
					admin_number = permit_adminnum.getAdminNumber();
					// Save the date in the appropriation date...
					appro_Date = permit_Date;
				}
				/* TODO SAM 2006-05-15 As per Ray Bennett discussion, use the receipt number
				since this is used in the irrigated lands data.  Because
				the receipt could be confused with a WDID, suffix with ":P".
				// This should be unique enough given modelers' use of _ and - in identifiers.
				// Use the permit parts for the identifier...
				right_id = "P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
				*/
				if ( hbwell_parcel != null ) {
					if ( isParcelClassForEstimatedWell(hbwell_parcel.get_Class()) ) {
						right_id = hbwell_parcel.getReceipt() + ":PE";
					}
					else {
						// Just indicate that it is a permit.
						right_id = hbwell_parcel.getReceipt() + ":P";
					}
				}
				else {
					right_id = ""; // FIXME SAM 2009-02-24 Need to read permit data
				}
			}
		}
	}
	// Create a pseudo water right...
	hbwellr = new HydroBase_NetAmts();
	hbwellr.setNet_rate_abs ( decree );
	// Needed or the aggregation code will toss out...
	hbwellr.setUnit ( "C" );
	// TODO SAM 2004-09-27 need to set?
	//hbwellr.setWD ( 0 );
	//hbwellr.setID ( 0 );
	hbwellr.setCommonID ( right_id );
	hbwellr.setWr_name ( right_name );
	// Set the dates...
	hbwellr.setApro_date( appro_Date);
	hbwellr.setAdmin_no ( admin_number );
	// Set the water right class for help in other processing...
	if ( hbwell_parcel != null ) {
		hbwellr.setParcelMatchClass ( hbwell_parcel.get_Class() );
	}
	hbwellr.setParcelID ( parcel_id );
	// Add to the return list and return the error count...
	hbwellr_Vector.add ( hbwellr );
	return warningCount;
}

/**
Print a warning about key identifier/name matches that have occurred when adding CU locations.
@param command Command that was adding the StateCU_Location.
@param replace If true, an existing instance is replaced if found.  If false,
the original instance is used.  This flag should be consistent with how the
StateCU_Location were processed.
@param match_Vector list of strings containing the key id/name values that have matches.
@param data_type String to use in messages to identify the data object type (e.g., "CU Locations").
*/
protected static void warnAboutDataMatches ( String command, boolean replace, List match_Vector,
	String data_type )
{	int size = match_Vector.size();

	if (size == 0) {
		return;
	}

	StringBuffer matches = new StringBuffer ( (String)match_Vector.get(0) );
	String id;
	int maxwidth = 100;
	String nl = System.getProperty ( "line.separator" );
	for (int i = 1; i < size; i++) {
		matches.append ( ", " );
		// Limit to "maxwidth" characters per line...
		id = (String)match_Vector.get(i);
		// 2 is for the ", "
		if ( (matches.length()%maxwidth + (id.length() + 2)) >= maxwidth) {
			matches.append ( nl );
		}
		matches.append ( id );
	}

	// Warn at level 2 since this is a non-fatal error.  Later may add an
	// option to the read methods to give the choice of some behavior when matches are found...

	if ( replace ) {
		Message.printWarning ( _FYI_warning_level, "StateDMI_Util.warnAboutDataMatches",
		"The following " + data_type +
		" were already in memory and were overwritten\nwith new data from the \"" + command + "\" " +
		"command :\n" + matches.toString() );
	}
	else {
		Message.printWarning ( _FYI_warning_level, "StateDMI_Util.warnAboutDataMatches",
		"The following " + data_type +
		" were already in memory and were retained\ndespite new data from the \"" + command + "\" " +
		"command :\n" + matches.toString() );
	}
}

}