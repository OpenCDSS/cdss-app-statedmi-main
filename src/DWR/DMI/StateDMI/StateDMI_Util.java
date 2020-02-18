// StateDMI_Util - Utility methods for manipulating StateCU and StateMod data.

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts_CollectionPartIdType;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;
import DWR.StateMod.StateMod_Well_CollectionPartIdType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import DWR.StateMod.StateMod_Well_CollectionType;
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
 * Determine the ID type for a well station.  It could be a WDID or permit receipt.
 * This is needed because logic is more robust if the determination is made up front.
 * @param hdmi HydroBaseDMI for database queries
 * @param wellStationId well station ID to evaluate
 * @param permitIdPatter a Java String.matches() regex that if matched indicates a well permit
 * @return "WDID" or "Receipt"
 */
public static String determineWellStationIdType ( HydroBaseDMI hdmi, String wellStationId, String permitIdPattern )
throws Exception {
	// First evaluate whether the ID matches WDID form, including having WD in the proper range
	if ( (permitIdPattern != null) && !permitIdPattern.isEmpty() && wellStationId.matches(permitIdPattern) ) {
		// Clear-cut case
		return "Receipt";
	}
	// Else have to do more evaluation of the identifier
	if ( HydroBase_WaterDistrict.isWDID(wellStationId) ) {
		// Pattern matches WDID format - the above only checks if it parses
		int wdidParts[] = HydroBase_WaterDistrict.parseWDID(wellStationId);
		// See if the WDID is available as a water right
		List<HydroBase_NetAmts> hbNetAmtsList = hdmi.readNetAmtsList(-1, wdidParts[0], wdidParts[1], false, null, false);
		if ( hbNetAmtsList.size() == 0 ) {
			// No water right - see if it shows up in a well permit
			List<HydroBase_Wells> hbWellsList = hdmi.readWellsList(wellStationId, -1, -1);
			if ( hbWellsList.size() == 0 ) {
				// No permit so use "WDID" and generate warnings for no data later
				return "WDID";
			}
		}
		else {
			return "Receipt";
		}
		// Was not found above but return "WDID" since that is preferred
		// Warnings will result later because of no right/permit
		return "WDID";
	}
	else {
		// Clearly not a WDID so assume Receipt
		// If there is no data later then warnings will be shown
		return "Receipt";
	}
}

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
protected static void findAndAddSMWellRight ( List<StateMod_WellRight> SMWellRight_Vector,
		List<String> SMWellRight_match_Vector, StateMod_WellRight wellr, int ifMatch )
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
				wer = SMWellRight_Vector.get(i);
				if ( wellr.getID().equalsIgnoreCase( wer.getID())
					&& wellr.getCgoto().equalsIgnoreCase( wer.getCgoto())
					&& wellr.getIrtem().equalsIgnoreCase( wer.getIrtem())
					&& (wellr.getSwitch()==wellr.getSwitch())){
					pos2 = i; // found match.
					break;
				}
			}
			if ( pos2 >= 0 ) {
				wer = SMWellRight_Vector.get(pos2);
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
 * Lookup the HydroBase_NetAmts_CollectionPartIdType from 
 * a StateMod_Well_CollectionPartIdType.
 * @return the HydroBase_NetAmts_CollectionPartIdType matching 
 * a StateMod_Well_CollectionPartIdType, or null if no match.
 */
private static HydroBase_NetAmts_CollectionPartIdType lookupHydroBaseNetAmtsCollectionPartIdType(
	StateMod_Well_CollectionPartIdType stateModWellCollectionPartIdType ) {
	if ( stateModWellCollectionPartIdType == StateMod_Well_CollectionPartIdType.RECEIPT ) {
		return HydroBase_NetAmts_CollectionPartIdType.RECEIPT;
	}
	else if ( stateModWellCollectionPartIdType == StateMod_Well_CollectionPartIdType.WDID ) {
		return HydroBase_NetAmts_CollectionPartIdType.WDID;
	}
	else {
		return null;
	}
}

/**
 * Lookup the StateMod_Well_CollectionPartIdType
 * from the HydroBase_NetAmts_CollectionPartIdType.
 * @return the StateMod_Well_CollectionPartIdType matching
 * a HydroBase_NetAmts_CollectionPartIdType matching, or null if no match.
 */
public static StateMod_Well_CollectionPartIdType lookupWellCollectionPartIdType(
	HydroBase_NetAmts_CollectionPartIdType hydrobaseNetAmtsCollectionPartIdType ) {
	if ( hydrobaseNetAmtsCollectionPartIdType == HydroBase_NetAmts_CollectionPartIdType.RECEIPT ) {
		return StateMod_Well_CollectionPartIdType.RECEIPT;
	}
	else if ( hydrobaseNetAmtsCollectionPartIdType == HydroBase_NetAmts_CollectionPartIdType.WDID ) {
		return StateMod_Well_CollectionPartIdType.WDID;
	}
	else {
		return null;
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

// TODO smalers 2019-07-06 under development - may not be used
/**
Helper method to process parcel use time series from HydroBase, when reading from the
"wells" table.  A single wells/well_to_parcel object is processed, resulting in
HydroBase_ParcelUseTS objects being returned.
@param hdmi HydroBaseDMI instance for queries.
@param command_tag Tag to use when printing messages.
@param routine Routine to use when printing messages.
@param warning_count Warning count to use when printing messages.  Increments
in the warning count is handled by passing the updated value back to the calling code.
@param locId Well station identifier that is being processed.
@param parcelId parcel number (from original aggregation data).
@param ditchParcelFraction Fraction of a parcel's area that is irrigated by the ditch
being processed (<= 1.0).  A value != 1.0 indicates that more than one ditch irrigate a parcel.
@param hbwellrList The list to return right(s) in.  The right(s) are
transferred to the main list in the calling code.
@return the number of errors that were found during processing.
@exception Exception Pass errors to the calling code.
*/
protected static int readParcelUseTSFromHydroBaseWellsHelper (
	HydroBaseDMI hdmi,
	String commandTag,	int warningLevel, int warningCount, CommandStatus status,
	String routine,
	int parcelYear,
	String locId,
	HydroBase_Wells hbwellParcel,
	int parcelId,
	double ditchParcelFraction,
	DefineWellRightHowType defineWellRightHow,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	List<HydroBase_NetAmts> hbwellrList )
	throws Exception
{	String message; // For messages
	HydroBase_NetAmts hbwellr = null; // Single right from HydroBase
	HydroBase_AdministrationNumber permitAdminnum;
	boolean useRightDate = false; // Use the water right information
	boolean usePermitDate = false; // Use the water permit information
	List<HydroBase_NetAmts> hbwellrPartList = null; // List of rights from HydroBase
	Date rightDate = null;
	if ( hbwellParcel != null ) {
	     rightDate = hbwellParcel.getAppr_date(); // Appropriation date in hbwell_parcel, as Date
	}
	DateTime rightDateTime; // Appropriation date in hbwell_parcel, as DateTime
	Date permitDate = null;
	if ( hbwellParcel != null ) {
		permitDate = hbwellParcel.getPerm_date(); // Permit date in hbwell_parcel, as Date
	}
	DateTime permitDateTime; // Permit date in hbwell_parcel, as DateTime
	Date approDate = null; // The final appropriation date in the HydroBase object.
	double yield = 0.0; // Well yield from "wells" record.
	double yieldApex = 0.0; // Well yield_apex from "wells" record
	double decree = 0.0; // Final decree in HydroBase object.
	double apex = 0.0; // Net rate APEX (alternate point/ exchange) decree value.
	double adminNumber = 0.0; // Final administration number in HydroBase object.
	String rightName = ""; // Final right name in HydroBase object.
	String rightId = ""; // Final common identifier for right, either a WDID or permit information,
					// depending on the data used for the right.
	HydroBase_AdministrationNumber approAdminnum;
					// Administration number object used to convert from dates.
	// Clear out the list to return...
	hbwellrList.clear();
	// The initial decision about whether to use water right or water permit
	// data is determined based on the DefineRightHow flag in the
	// ReadWellRightsFromHydroBase() command...
	//
	// The following checks for hbwellParcel != null are used when dealing with parcel data.
	// If reading well right/permit data directly, use the object will be null.
	String rightDateReason = "";
	if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.EARLIEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// earliest of the well right appropriation date or permit date...
		if ( (rightDate != null) && (permitDate == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using right because no " +
			"corresponding permit date is available." );
			useRightDate = true;
			rightDateReason = "HaveRightDate/NoPermitDate";
		}
		else if ( (rightDate == null) && (permitDate != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using permit because no " +
			"corresponding right date is available." );
			usePermitDate = true;
			rightDateReason = "NoRightDate/HavePermitDate";
		}
		else if ( (rightDate == null) && (permitDate == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwellParcel.getWD() > 0) && (hbwellParcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwellParcel.getWD(), hbwellParcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because have WDID but no right or permit date." );
				useRightDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/HaveWDID";
			}
			else {
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				usePermitDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/NoWDID";
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permitDate.before(rightDate) ) {
				// Permit date is earliest...
				usePermitDate = true;
				rightDateReason = "PermitDate<RightDate";
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because date is earlier than right date." );
			}
			else {
				useRightDate = true;
				rightDateReason = "RightDate<PermitDate";
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because date is earlier than permit date." );
			}
		}
	}
	else if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.LATEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// latest of the well right appropriation date or permit date...
		if ( (rightDate != null) && (permitDate == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using right because no " +
			"corresponding permit date is available." );
			useRightDate = true;
			rightDateReason = "HaveRightDate/NoPermitDate";
		}
		else if ( (rightDate == null) && (permitDate != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using permit because no " +
			"corresponding right date is available." );
			usePermitDate = true;
			rightDateReason = "NoRightDate/HavePermitDate";
		}
		else if ( (rightDate == null) && (permitDate == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwellParcel.getWD() > 0) && (hbwellParcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwellParcel.getWD(), hbwellParcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because have WDID but no right or permit date." );
				useRightDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/HaveWDID";
			}
			else {
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				usePermitDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/NoWDID";
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permitDate.after(rightDate) ) {
				// Permit date is latest...
				usePermitDate = true;
				rightDateReason = "PermitDate>RightDate";
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because date is later than right date." );
			}
			else {
				useRightDate = true;
				rightDateReason = "RightDate>PermitDate";
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because date is later than permit date." );
			}
		}
	}
	// Above bas EARLIEST_DATE and LATEST_DATE
	// Below uses right if available and otherwise permit
	else if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.RIGHT_IF_AVAILABLE) ) {
		if ( (hbwellParcel.getWD()>0) && (hbwellParcel.getID() > 0) ){
			// Use the water right information...
			useRightDate = true;
			rightDateReason = "HaveWDID";
			if ( !readWellRights ) {
				Message.printStatus ( 2, routine, "Using right data because it is available." );
			}
		}
		else {
			// Use the permit information...
			usePermitDate = true;
			rightDateReason = "NoWDID";
			// TODO SAM 2016-05-29 but may not have permit date!
			Message.printStatus ( 2, routine, "Using permit because right is not available." );
		}
	}
	else if ( hbwellParcel != null ) {
		// DefineRightHow in calling code is not defined - should not get to this code...
		message = "DefineRightHow value (" + defineWellRightHow + ") is not handled";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify processing parameters - contact software support." ) );
		throw new IllegalArgumentException(message);
	}
	if ( ((hbwellParcel != null) && useRightDate && readWellRights ) || // Reading parcel/well data
		(hbwellParcel == null) && HydroBase_WaterDistrict.isWDID(locId) ) { // Reading an individual well WDID
		// TODO SAM 2009-03-29 Evaluate here - should use_right be checked before querying for rights?
		// Re-query the rights to get the basic data...
		// A well structure and a query is requested so query the water rights ...
		try {
			if ( hbwellParcel != null ) {
				// Get the WDID from the well/parcel supply information...
				// TODO SAM 2016-05-29 Could this lead to redundant well rights if WDID is used for supply for multiple parcels?
				hbwellrPartList = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, hbwellParcel.getWD(), hbwellParcel.getID(), false, null );
			}
			else {
				// Get the WDID from the passed in well station ID, which will be the WDID.
				int [] wdid_parts = new int[2];
				// Should parse since checked above.
				wdid_parts = HydroBase_WaterDistrict.parseWDID ( locId );
				hbwellrPartList = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
			}
			// Loop through the returned data and add to returned list.
			// Adjust the amounts by ditch coverage area, if appropriate.
			int nhbwellrPart = 0;
			if ( hbwellrPartList != null ) {
				nhbwellrPart = hbwellrPartList.size();
			}
			if ( (hbwellParcel != null) && nhbwellrPart == 0 ) {
				// Expecting data since the parcel match indicated so
				message = "No net amount rights data from HydroBase found for " +
				HydroBase_WaterDistrict.formWDID (hbwellParcel.getWD(), hbwellParcel.getID() ) +
				", " + hbwellParcel.getWell_name() + " from " + parcelYear + " parcel data.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well used for parcel supply has net amount " +
							"water rights (can change over time but HydroBase has only the latest total)." ) );
				return warningCount;
			}
			else {
				if ( hbwellParcel != null ) {
					Message.printStatus ( 2, routine, "Read " + nhbwellrPart + " well rights for " +
					HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID()));
				}
				else {
					Message.printStatus ( 2, routine, "Read " + nhbwellrPart + " well rights for \"" + locId + "\"");
				}
			}
			for ( int ihb = 0; ihb < nhbwellrPart; ihb++ ) {
				// Adjust the decree based on the amount of the well's yield that serves the parcel...
				hbwellr = hbwellrPartList.get(ihb);
				decree = hbwellr.getNet_rate_abs();
				apex = hbwellr.getNet_rate_apex();
				if ( useApex && (apex > 0.0) ) {
					if ( DMIUtil.isMissing(decree) ) {
						decree = apex;
					}
					else {
						decree += apex;
					}
				}
				if ( !DMIUtil.isMissing( decree ) ) {
					// Reset the decree to consider the APEX and adjusted for ditch and well percent...
					if ( hbwellParcel == null ) {
						hbwellr.setNet_rate_abs(decree * 1.0 * ditchParcelFraction);
					}
					else {
						hbwellr.setNet_rate_abs(decree * hbwellParcel.getPercent_yield() * ditchParcelFraction);
					}
					// Make sure the units are "C" for CFS so that aggregation will work...
					hbwellr.setUnit ( "C" );
				}
				// Set information used elsewhere.  The "common ID" is always the WDID.
				// The water right name was set in the query.
				if ( hbwellParcel == null ) {
					hbwellr.setCommonID ( locId );
				}
				else {
					// Set the well ID to the WDID.
					hbwellr.setCommonID (
					HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() ) );
				}
				// Set the water right class and parcel_id for help in other processing...
				if ( hbwellParcel != null ) {
					hbwellr.setParcelMatchClass ( hbwellParcel.get_Class() );
				}
				hbwellr.setParcelID ( parcelId );
				// Set extended data to help understand how well rights were determined
				if ( hbwellParcel != null ) {
					// The following was added for StateDMI 4.00.00 to clarify groundwater-only processing (and help explain other)
					hbwellr.setXApproDate(hbwellParcel.getAppr_date());
					hbwellr.setXFractionYield(hbwellParcel.getPercent_yield());
					hbwellr.setXDitchFraction(ditchParcelFraction);
					hbwellr.setXPermitDate(hbwellParcel.getPerm_date());
					hbwellr.setXPermitReceipt(hbwellParcel.getReceipt());
					hbwellr.setXYieldApexGPM(hbwellParcel.getYield_apex());
					hbwellr.setXYieldGPM(hbwellParcel.getYield());
					// Maybe set reason for date being chosen
				}
				// Now add the individual right to the list...
				hbwellrList.add ( hbwellr );
			}
			// Now return (results will be in hbwellr_Vector)...
			return warningCount;
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			if ( hbwellParcel != null ) {
				message = "Unexpected error getting net amount rights data from HydroBase for " +
				HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() ) +
				", " + hbwellParcel.getWell_name() + " (" + e + ").";
			}
			else {
				message = "Unexpected error getting net amount rights data from HydroBase for \"" + locId +
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
		// 2) Reading a well permit explicitly
		//
		// The well data produced by the well/parcel matching processing are used, which can result in some
		// aggregated information for WDIDs
		if ( hbwellParcel == null ) {
			message = "The explicit well ID \"" + locId + "\" contains characters and is therefore assumed to be a well " +
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
			yield = hbwellParcel.getYield(); // GPM
			yieldApex = hbwellParcel.getYield_apex(); // GPM
			if ( useApex ) {
				// Add the Apex to the yield.  If the yield is zero, then
				// the Apex will be the amount shown in the right.
				if ( yield < 0.0 ) {
					// Yield was missing...
					yield = yieldApex;
				}
				else {
					// Add the Apex...
					yield += yieldApex;
				}
			}
			if ( DMIUtil.isMissing( yield) ) {
				// No well yield so return...
				// Print a non-fatal warning (don't know if HydroBase data will always be good...
				// Non-fatal...
				if ( useApex ) {
					message = "Well yield for parcel \"" + parcelId +
					"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				}
				else {
					message = "Well yield+yield_apex for parcel \"" + parcelId +
					"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				}
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that data in HydroBase are complete." ) );
				return warningCount;
			}
			if ( DMIUtil.isMissing( hbwellParcel.getPercent_yield()) ) {
				// No well percent_yield so return...
				// Print a non-fatal warning (don't know if HydroBase
				// data will always be good...
				// Non-fatal...
				message = "Well percent yield for parcel \"" + parcelId +
				"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				return warningCount;
			}
			// Convert the yield from GPM to CFS and prorate the yield
			// according to the fraction provided to the parcel...
			decree = (yield + yieldApex)*.002228 // GPM to CFS
				*hbwellParcel.getPercent_yield()
				*ditchParcelFraction; // Will only be different from 1.0 when processing ditches
			if ( useRightDate ) {
				// Use the right name...
				/* TODO SAM	As per Ray Bennett just use name...
				right_name = HydroBase_WaterDistrict.formWDID (
					hbwell_parcel.getWD(), hbwell_parcel.getID() ) +
					", " + hbwell_parcel.getWell_name();
				*/
				rightName = hbwellParcel.getWell_name();
				if ( rightName.equals("") ) {
					rightName = HydroBase_WaterDistrict.formWDID (
					hbwellParcel.getWD(), hbwellParcel.getID() ) + ", " + hbwellParcel.getWell_name();
				}
				// Use the water right appropriation date (set at the top of this method)...
				if ( DMIUtil.isMissing( rightDate) ) {
					// No date so use the user-specified value or the default...
					adminNumber = defaultAdminNumber;
					approDate = defaultApproDate;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcelId +
					"\", year " + parcelYear + " right appropriation date is missing - using default " +
					approDate );
				}
				else {
					// Convert the right date to an administration number...
					rightDateTime = new DateTime( rightDate);
					approAdminnum = new HydroBase_AdministrationNumber( rightDateTime, null );
					adminNumber = approAdminnum.getAdminNumber();
					// Save the date in the appropriation date...
					approDate = rightDate;
				}
				// Use a 7-digit formatted WDID for rights...
				rightId = HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() );
				// Estimated wells need to be clearly identified because they are handled in the merge.
				if ( (hbwellParcel != null) && isParcelClassForEstimatedWell(hbwellParcel.get_Class()) ) {
					rightId = rightId + ":WE";
				}
			}
			else if ( usePermitDate ) {
				// Use the permit information for the name...
				/* TODO 2006-04-24 As per Ray Bennett, just use right name...
				right_name = "P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
				*/
				if ( hbwellParcel != null ) {
					rightName = hbwellParcel.getWell_name();
				}
				else {
					rightName = ""; // FIXME SAM 2009-02-24 Decide what to use
				}
				if ( rightName.equals("") ) {
					/* TODO SAM 2006-05-15 As per Ray Bennett discussion, use the receipt.
					right_name =
					"P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
					*/
					if ( hbwellParcel != null ) {
						rightName = hbwellParcel.getReceipt();
					}
					else {
						rightName = ""; // FIXME SAM 2009-02-24 Decide what to use
					}
				}
				// Use the permit date (set at the top of this method)...
				if ( DMIUtil.isMissing( permitDate) ) {
					// No date so use the user-specified value or the default...
					adminNumber = defaultAdminNumber;
					approDate = defaultApproDate;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcelId +
						"\", year " + parcelYear + " permit date is missing - using default " +
						approDate );
				}
				else {
					// Convert the permit date to an administration number...
					permitDateTime = new DateTime( permitDate);
					permitAdminnum = new HydroBase_AdministrationNumber( permitDateTime, null );
					adminNumber = permitAdminnum.getAdminNumber();
					// Save the date in the appropriation date...
					approDate = permitDate;
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
				if ( hbwellParcel != null ) {
					if ( isParcelClassForEstimatedWell(hbwellParcel.get_Class()) ) {
						rightId = hbwellParcel.getReceipt() + ":PE";
					}
					else {
						// Format the permit receipt based on modeler-specified format
						rightId = String.format(permitIdPreFormat, hbwellParcel.getReceipt());
					}
				}
				else {
					// FIXME SAM 2009-02-24 Need to read permit data
					// This may never be implemented because permits are not typically distributed with HydroBase
					// and modelers use the data where parcels/wells are spatially matched
					rightId = "";
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
	hbwellr.setCommonID ( rightId );
	hbwellr.setWr_name ( rightName );
	// Set the dates...
	hbwellr.setApro_date( approDate);
	hbwellr.setAdmin_no ( adminNumber );
	// Set the water right class and other data for help in other processing...
	if ( hbwellParcel != null ) {
		// The following has been around for awhile (RGDSS?)
		hbwellr.setParcelMatchClass ( hbwellParcel.get_Class() );
		// The following was added for StateDMI 4.00.00 to clarify groundwater-only processing (and help explain other)
		hbwellr.setXApproDate(hbwellParcel.getAppr_date());
		hbwellr.setXFractionYield(hbwellParcel.getPercent_yield());
		hbwellr.setXDitchFraction(ditchParcelFraction);
		hbwellr.setXPermitDate(hbwellParcel.getPerm_date());
		hbwellr.setXPermitReceipt(hbwellParcel.getReceipt());
		hbwellr.setXYieldApexGPM(hbwellParcel.getYield_apex());
		hbwellr.setXYieldGPM(hbwellParcel.getYield());
		// Maybe set reason for date being chosen
	}
	hbwellr.setParcelID ( parcelId );
	// Indicate that permit was read if necessary
	// TODO SAM 2016-05-18 Need to fix this if receipt part type was requested
	// - but needs to be consistent with above
	if ( usePermitDate ) {
		hbwellr.setCollectionIdPartType(
			lookupHydroBaseNetAmtsCollectionPartIdType(
				StateMod_Well_CollectionPartIdType.RECEIPT));
	}
	else {
		hbwellr.setCollectionIdPartType(
			lookupHydroBaseNetAmtsCollectionPartIdType(StateMod_Well_CollectionPartIdType.RECEIPT));
	}
	// Add to the return list and return the error count...
	hbwellrList.add ( hbwellr );
	return warningCount;
}

/**
Read the list of parcel years from HydroBase.
@param hdmi HydroBaseDMI instance for queries.
@param Div_int integer division to process.
@return an array of integer years for which parcel data exist in HydroBase.
*/
public static int [] readParcelYearListFromHydroBase ( HydroBaseDMI hdmi, int div )
throws Exception
{
	int [] divs = { div };
	return readParcelYearListFromHydroBase(hdmi, divs);
}

/**
Read the list of parcel years from HydroBase.
@param hdmi HydroBaseDMI instance for queries.
@param Div_int integer division to process.
@return an array of integer years for which parcel data exist in HydroBase.
*/
public static int [] readParcelYearListFromHydroBase ( HydroBaseDMI hdmi, int [] divs )
throws Exception
{	// TODO SAM 2007-05-25 Check HydroBase version for the following

	List<Integer> years = new ArrayList<>();
	for ( int idivs = 0; idivs < divs.length; idivs++ ) {
		List<Integer> v = hdmi.readParcelUseTSDistinctCalYearsList(divs[idivs]);
		if ( v != null ) {
			years.addAll(v);
		}
	}

	int [] yearsArray = new int[years.size()];
	for ( int i = 0; i < years.size(); i++ ) {
		yearsArray[i] = years.get(i).intValue();
	}
	
	return yearsArray;
}
	
/**
Helper method to process well rights from HydroBase, when reading from the
"wells" table.  A single wells/well_to_parcel object is processed, resulting in
one or more HydroBase_NetAmts objects being returned. This method handles
whether to use well rights or permits (receipt number for ID and potentially permit date) for the returned data.
@param hdmi HydroBaseDMI instance for queries.
@param command_tag Tag to use when printing messages.
@param routine Routine to use when printing messages.
@param warning_count Warning count to use when printing messages.  Increments
in the warning count is handled by passing the updated value back to the calling code.
@param locId Well station identifier that is being processed.
@param hbwellParcel wells/well_to_parcel join object, from HydroBase.  If null, read the well right/permit
data directly using the well station ID, rather than using the ID information from the well to parcel object.
@param parcelId parcel number (from original aggregation data).
@param ditchParcelFraction Fraction of a parcel's area that is irrigated by the ditch
being processed (<= 1.0).  A value != 1.0 indicates that more than one ditch irrigate a parcel.
@param defineWellRightHow _EarliestDate to define the StateMod right using the
earliest of the well right appropriation date and permit date;
_LatestDate to define the StateMod right using the
latest of the well right appropriation date and permit date;
_RightIfAvailable If true, define the StateMod right using
the well right appropriation date, if the well right is available.  Otherwise use the permit date.
@param readWellRights If true, query the water rights.  If false, use the data in the wells table.
@param useApex If true, add the APEX data values to the net amount right
value. If false, do not add APEX.
@param defaultAdminNumber Default administration number to use if a date
cannot be determined from the data.
@param permitIdPreFormat a format using String.format() specifier to format a permit receipt
(for example use "%s:P" to use legacy behavior to indicate permit).
@param hbwellrList The list to return right(s) in.  The right(s) are
transferred to the main list in the calling code.
@return the number of errors that were found during processing.
@exception Exception Pass errors to the calling code.
*/
protected static int readWellRightsFromHydroBaseWellParcelsHelper (
	HydroBaseDMI hdmi,
	String commandTag,	int warningLevel, int warningCount, CommandStatus status,
	String routine,
	int parcelYear,
	String locId,
	HydroBase_Wells hbwellParcel,
	int parcelId,
	double ditchParcelFraction,
	DefineWellRightHowType defineWellRightHow,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	List<HydroBase_NetAmts> hbwellrList )
	throws Exception
{	String message; // For messages
	HydroBase_NetAmts hbwellr = null; // Single right from HydroBase
	HydroBase_AdministrationNumber permitAdminnum;
	boolean useRightDate = false; // Use the water right information
	boolean usePermitDate = false; // Use the water permit information
	List<HydroBase_NetAmts> hbwellrPartList = null; // List of rights from HydroBase
	Date rightDate = null;
	if ( hbwellParcel != null ) {
	     rightDate = hbwellParcel.getAppr_date(); // Appropriation date in hbwell_parcel, as Date
	}
	DateTime rightDateTime; // Appropriation date in hbwell_parcel, as DateTime
	Date permitDate = null;
	if ( hbwellParcel != null ) {
		permitDate = hbwellParcel.getPerm_date(); // Permit date in hbwell_parcel, as Date
	}
	DateTime permitDateTime; // Permit date in hbwell_parcel, as DateTime
	Date approDate = null; // The final appropriation date in the HydroBase object.
	double yield = 0.0; // Well yield from "wells" record.
	double yieldApex = 0.0; // Well yield_apex from "wells" record
	double decree = 0.0; // Final decree in HydroBase object.
	double apex = 0.0; // Net rate APEX (alternate point/ exchange) decree value.
	double adminNumber = 0.0; // Final administration number in HydroBase object.
	String rightName = ""; // Final right name in HydroBase object.
	String rightId = ""; // Final common identifier for right, either a WDID or permit information,
					// depending on the data used for the right.
	HydroBase_AdministrationNumber approAdminnum;
					// Administration number object used to convert from dates.
	// Clear out the list to return...
	hbwellrList.clear();
	// The initial decision about whether to use water right or water permit
	// data is determined based on the DefineRightHow flag in the
	// ReadWellRightsFromHydroBase() command...
	//
	// The following checks for hbwellParcel != null are used when dealing with parcel data.
	// If reading well right/permit data directly, use the object will be null.
	String rightDateReason = "";
	if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.EARLIEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// earliest of the well right appropriation date or permit date...
		if ( (rightDate != null) && (permitDate == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using right because no " +
			"corresponding permit date is available." );
			useRightDate = true;
			rightDateReason = "HaveRightDate/NoPermitDate";
		}
		else if ( (rightDate == null) && (permitDate != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from earliest date:  using permit because no " +
			"corresponding right date is available." );
			usePermitDate = true;
			rightDateReason = "NoRightDate/HavePermitDate";
		}
		else if ( (rightDate == null) && (permitDate == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwellParcel.getWD() > 0) && (hbwellParcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwellParcel.getWD(), hbwellParcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because have WDID but no right or permit date." );
				useRightDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/HaveWDID";
			}
			else {
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				usePermitDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/NoWDID";
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permitDate.before(rightDate) ) {
				// Permit date is earliest...
				usePermitDate = true;
				rightDateReason = "PermitDate<RightDate";
				Message.printStatus ( 2, routine, "Rights from earliest date:  using permit " +
				"because date is earlier than right date." );
			}
			else {
				useRightDate = true;
				rightDateReason = "RightDate<PermitDate";
				Message.printStatus ( 2, routine, "Rights from earliest date:  using right " +
				"because date is earlier than permit date." );
			}
		}
	}
	else if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.LATEST_DATE) ) {
		// Determine whether to use the well or permit based on the
		// latest of the well right appropriation date or permit date...
		if ( (rightDate != null) && (permitDate == null) ) {
			// Only right appropriation date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using right because no " +
			"corresponding permit date is available." );
			useRightDate = true;
			rightDateReason = "HaveRightDate/NoPermitDate";
		}
		else if ( (rightDate == null) && (permitDate != null) ) {
			// Only permit date is available...
			Message.printStatus ( 2, routine, "Rights from latest date:  using permit because no " +
			"corresponding right date is available." );
			usePermitDate = true;
			rightDateReason = "NoRightDate/HavePermitDate";
		}
		else if ( (rightDate == null) && (permitDate == null) ) {
			// Don't have either date.  Use a right if the WDID is
			// available and permit otherwise (probably will not have WDID)...
			if ( (hbwellParcel.getWD() > 0) && (hbwellParcel.getID() > 0) ) {
				// Non-fatal...
				message = "Wells data has no dates but has WDID " +
				HydroBase_WaterDistrict.formWDID(hbwellParcel.getWD(), hbwellParcel.getID()) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because have WDID but no right or permit date." );
				useRightDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/HaveWDID";
			}
			else {
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because no right or permit date and don't have WDID." );
				usePermitDate = true;
				rightDateReason = "NoRightDate/NoPermitDate/NoWDID";
			}
			// TODO SAM 2005-03-03 does this make sense?
		}
		else {
			// Have both dates so compare the right and permit dates...
			if ( permitDate.after(rightDate) ) {
				// Permit date is latest...
				usePermitDate = true;
				rightDateReason = "PermitDate>RightDate";
				Message.printStatus ( 2, routine, "Rights from latest date:  using permit " +
				"because date is later than right date." );
			}
			else {
				useRightDate = true;
				rightDateReason = "RightDate>PermitDate";
				Message.printStatus ( 2, routine, "Rights from latest date:  using right " +
				"because date is later than permit date." );
			}
		}
	}
	// Above bas EARLIEST_DATE and LATEST_DATE
	// Below uses right if available and otherwise permit
	else if ( (hbwellParcel != null) && (defineWellRightHow == DefineWellRightHowType.RIGHT_IF_AVAILABLE) ) {
		if ( (hbwellParcel.getWD()>0) && (hbwellParcel.getID() > 0) ){
			// Use the water right information...
			useRightDate = true;
			rightDateReason = "HaveWDID";
			if ( !readWellRights ) {
				Message.printStatus ( 2, routine, "Using right data because it is available." );
			}
		}
		else {
			// Use the permit information...
			usePermitDate = true;
			rightDateReason = "NoWDID";
			// TODO SAM 2016-05-29 but may not have permit date!
			Message.printStatus ( 2, routine, "Using permit because right is not available." );
		}
	}
	else if ( hbwellParcel != null ) {
		// DefineRightHow in calling code is not defined - should not get to this code...
		message = "DefineRightHow value (" + defineWellRightHow + ") is not handled";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify processing parameters - contact software support." ) );
		throw new IllegalArgumentException(message);
	}
	if ( ((hbwellParcel != null) && useRightDate && readWellRights ) || // Reading parcel/well data
		(hbwellParcel == null) && HydroBase_WaterDistrict.isWDID(locId) ) { // Reading an individual well WDID
		// TODO SAM 2009-03-29 Evaluate here - should use_right be checked before querying for rights?
		// Re-query the rights to get the basic data...
		// A well structure and a query is requested so query the water rights ...
		try {
			if ( hbwellParcel != null ) {
				// Get the WDID from the well/parcel supply information...
				// TODO SAM 2016-05-29 Could this lead to redundant well rights if WDID is used for supply for multiple parcels?
				hbwellrPartList = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, hbwellParcel.getWD(), hbwellParcel.getID(), false, null );
			}
			else {
				// Get the WDID from the passed in well station ID, which will be the WDID.
				int [] wdid_parts = new int[2];
				// Should parse since checked above.
				wdid_parts = HydroBase_WaterDistrict.parseWDID ( locId );
				hbwellrPartList = hdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
			}
			// Loop through the returned data and add to returned list.
			// Adjust the amounts by ditch coverage area, if appropriate.
			int nhbwellrPart = 0;
			if ( hbwellrPartList != null ) {
				nhbwellrPart = hbwellrPartList.size();
			}
			if ( (hbwellParcel != null) && nhbwellrPart == 0 ) {
				// Expecting data since the parcel match indicated so
				message = "No net amount rights data from HydroBase found for " +
				HydroBase_WaterDistrict.formWDID (hbwellParcel.getWD(), hbwellParcel.getID() ) +
				", " + hbwellParcel.getWell_name() + " from " + parcelYear + " parcel data.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well used for parcel supply has net amount " +
							"water rights (can change over time but HydroBase has only the latest total)." ) );
				return warningCount;
			}
			else {
				if ( hbwellParcel != null ) {
					Message.printStatus ( 2, routine, "Read " + nhbwellrPart + " well rights for " +
					HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID()));
				}
				else {
					Message.printStatus ( 2, routine, "Read " + nhbwellrPart + " well rights for \"" + locId + "\"");
				}
			}
			for ( int ihb = 0; ihb < nhbwellrPart; ihb++ ) {
				// Adjust the decree based on the amount of the well's yield that serves the parcel...
				hbwellr = hbwellrPartList.get(ihb);
				decree = hbwellr.getNet_rate_abs();
				apex = hbwellr.getNet_rate_apex();
				if ( useApex && (apex > 0.0) ) {
					if ( DMIUtil.isMissing(decree) ) {
						decree = apex;
					}
					else {
						decree += apex;
					}
				}
				if ( !DMIUtil.isMissing( decree ) ) {
					// Reset the decree to consider the APEX and adjusted for ditch and well percent...
					if ( hbwellParcel == null ) {
						hbwellr.setNet_rate_abs(decree * 1.0 * ditchParcelFraction);
					}
					else {
						hbwellr.setNet_rate_abs(decree * hbwellParcel.getPercent_yield() * ditchParcelFraction);
					}
					// Make sure the units are "C" for CFS so that aggregation will work...
					hbwellr.setUnit ( "C" );
				}
				// Set information used elsewhere.  The "common ID" is always the WDID.
				// The water right name was set in the query.
				if ( hbwellParcel == null ) {
					hbwellr.setCommonID ( locId );
				}
				else {
					// Set the well ID to the WDID.
					hbwellr.setCommonID (
					HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() ) );
				}
				// Set the water right class and parcel_id for help in other processing...
				if ( hbwellParcel != null ) {
					hbwellr.setParcelMatchClass ( hbwellParcel.get_Class() );
				}
				hbwellr.setParcelID ( parcelId );
				// Set extended data to help understand how well rights were determined
				if ( hbwellParcel != null ) {
					// The following was added for StateDMI 4.00.00 to clarify groundwater-only processing (and help explain other)
					hbwellr.setXApproDate(hbwellParcel.getAppr_date());
					hbwellr.setXFractionYield(hbwellParcel.getPercent_yield());
					hbwellr.setXDitchFraction(ditchParcelFraction);
					hbwellr.setXPermitDate(hbwellParcel.getPerm_date());
					hbwellr.setXPermitReceipt(hbwellParcel.getReceipt());
					hbwellr.setXYieldApexGPM(hbwellParcel.getYield_apex());
					hbwellr.setXYieldGPM(hbwellParcel.getYield());
					// Maybe set reason for date being chosen
				}
				// Now add the individual right to the list...
				hbwellrList.add ( hbwellr );
			}
			// Now return (results will be in hbwellr_Vector)...
			return warningCount;
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			if ( hbwellParcel != null ) {
				message = "Unexpected error getting net amount rights data from HydroBase for " +
				HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() ) +
				", " + hbwellParcel.getWell_name() + " (" + e + ").";
			}
			else {
				message = "Unexpected error getting net amount rights data from HydroBase for \"" + locId +
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
		// 2) Reading a well permit explicitly
		//
		// The well data produced by the well/parcel matching processing are used, which can result in some
		// aggregated information for WDIDs
		if ( hbwellParcel == null ) {
			message = "The explicit well ID \"" + locId + "\" contains characters and is therefore assumed to be a well " +
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
			yield = hbwellParcel.getYield(); // GPM
			yieldApex = hbwellParcel.getYield_apex(); // GPM
			if ( useApex ) {
				// Add the Apex to the yield.  If the yield is zero, then
				// the Apex will be the amount shown in the right.
				if ( yield < 0.0 ) {
					// Yield was missing...
					yield = yieldApex;
				}
				else {
					// Add the Apex...
					yield += yieldApex;
				}
			}
			if ( DMIUtil.isMissing( yield) ) {
				// No well yield so return...
				// Print a non-fatal warning (don't know if HydroBase data will always be good...
				// Non-fatal...
				if ( useApex ) {
					message = "Well yield for parcel \"" + parcelId +
					"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				}
				else {
					message = "Well yield+yield_apex for parcel \"" + parcelId +
					"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				}
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that data in HydroBase are complete." ) );
				return warningCount;
			}
			if ( DMIUtil.isMissing( hbwellParcel.getPercent_yield()) ) {
				// No well percent_yield so return...
				// Print a non-fatal warning (don't know if HydroBase
				// data will always be good...
				// Non-fatal...
				message = "Well percent yield for parcel \"" + parcelId +
				"\", year " + parcelYear + " is missing.  Output may be incomplete.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that well/parcel data in HydroBase are complete." ) );
				return warningCount;
			}
			// Convert the yield from GPM to CFS and prorate the yield
			// according to the fraction provided to the parcel...
			decree = (yield + yieldApex)*.002228 // GPM to CFS
				*hbwellParcel.getPercent_yield()
				*ditchParcelFraction; // Will only be different from 1.0 when processing ditches
			if ( useRightDate ) {
				// Use the right name...
				/* TODO SAM	As per Ray Bennett just use name...
				right_name = HydroBase_WaterDistrict.formWDID (
					hbwell_parcel.getWD(), hbwell_parcel.getID() ) +
					", " + hbwell_parcel.getWell_name();
				*/
				rightName = hbwellParcel.getWell_name();
				if ( rightName.equals("") ) {
					rightName = HydroBase_WaterDistrict.formWDID (
					hbwellParcel.getWD(), hbwellParcel.getID() ) + ", " + hbwellParcel.getWell_name();
				}
				// Use the water right appropriation date (set at the top of this method)...
				if ( DMIUtil.isMissing( rightDate) ) {
					// No date so use the user-specified value or the default...
					adminNumber = defaultAdminNumber;
					approDate = defaultApproDate;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcelId +
					"\", year " + parcelYear + " right appropriation date is missing - using default " +
					approDate );
				}
				else {
					// Convert the right date to an administration number...
					rightDateTime = new DateTime( rightDate);
					approAdminnum = new HydroBase_AdministrationNumber( rightDateTime, null );
					adminNumber = approAdminnum.getAdminNumber();
					// Save the date in the appropriation date...
					approDate = rightDate;
				}
				// Use a 7-digit formatted WDID for rights...
				rightId = HydroBase_WaterDistrict.formWDID ( hbwellParcel.getWD(), hbwellParcel.getID() );
				// Estimated wells need to be clearly identified because they are handled in the merge.
				if ( (hbwellParcel != null) && isParcelClassForEstimatedWell(hbwellParcel.get_Class()) ) {
					rightId = rightId + ":WE";
				}
			}
			else if ( usePermitDate ) {
				// Use the permit information for the name...
				/* TODO 2006-04-24 As per Ray Bennett, just use right name...
				right_name = "P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
				*/
				if ( hbwellParcel != null ) {
					rightName = hbwellParcel.getWell_name();
				}
				else {
					rightName = ""; // FIXME SAM 2009-02-24 Decide what to use
				}
				if ( rightName.equals("") ) {
					/* TODO SAM 2006-05-15 As per Ray Bennett discussion, use the receipt.
					right_name =
					"P:" + hbwell_parcel.getPermitno() + "_" +
					hbwell_parcel.getPermitsuf() + "_" +
					hbwell_parcel.getPermitrpl();
					*/
					if ( hbwellParcel != null ) {
						rightName = hbwellParcel.getReceipt();
					}
					else {
						rightName = ""; // FIXME SAM 2009-02-24 Decide what to use
					}
				}
				// Use the permit date (set at the top of this method)...
				if ( DMIUtil.isMissing( permitDate) ) {
					// No date so use the user-specified value or the default...
					adminNumber = defaultAdminNumber;
					approDate = defaultApproDate;
					Message.printStatus ( 2, routine, "Using well/parcel data, parcel \"" + parcelId +
						"\", year " + parcelYear + " permit date is missing - using default " +
						approDate );
				}
				else {
					// Convert the permit date to an administration number...
					permitDateTime = new DateTime( permitDate);
					permitAdminnum = new HydroBase_AdministrationNumber( permitDateTime, null );
					adminNumber = permitAdminnum.getAdminNumber();
					// Save the date in the appropriation date...
					approDate = permitDate;
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
				if ( hbwellParcel != null ) {
					if ( isParcelClassForEstimatedWell(hbwellParcel.get_Class()) ) {
						rightId = hbwellParcel.getReceipt() + ":PE";
					}
					else {
						// Format the permit receipt based on modeler-specified format
						rightId = String.format(permitIdPreFormat, hbwellParcel.getReceipt());
					}
				}
				else {
					// FIXME SAM 2009-02-24 Need to read permit data
					// This may never be implemented because permits are not typically distributed with HydroBase
					// and modelers use the data where parcels/wells are spatially matched
					rightId = "";
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
	hbwellr.setCommonID ( rightId );
	hbwellr.setWr_name ( rightName );
	// Set the dates...
	hbwellr.setApro_date( approDate);
	hbwellr.setAdmin_no ( adminNumber );
	// Set the water right class and other data for help in other processing...
	if ( hbwellParcel != null ) {
		// The following has been around for awhile (RGDSS?)
		hbwellr.setParcelMatchClass ( hbwellParcel.get_Class() );
		// The following was added for StateDMI 4.00.00 to clarify groundwater-only processing (and help explain other)
		hbwellr.setXApproDate(hbwellParcel.getAppr_date());
		hbwellr.setXFractionYield(hbwellParcel.getPercent_yield());
		hbwellr.setXDitchFraction(ditchParcelFraction);
		hbwellr.setXPermitDate(hbwellParcel.getPerm_date());
		hbwellr.setXPermitReceipt(hbwellParcel.getReceipt());
		hbwellr.setXYieldApexGPM(hbwellParcel.getYield_apex());
		hbwellr.setXYieldGPM(hbwellParcel.getYield());
		// Maybe set reason for date being chosen
	}
	hbwellr.setParcelID ( parcelId );
	// Indicate that permit was read if necessary
	// TODO SAM 2016-05-18 Need to fix this if receipt part type was requested
	// - but needs to be consistent with above
	if ( usePermitDate ) {
		hbwellr.setCollectionIdPartType(
			lookupHydroBaseNetAmtsCollectionPartIdType(
				StateMod_Well_CollectionPartIdType.RECEIPT));
	}
	else {
		hbwellr.setCollectionIdPartType(
			lookupHydroBaseNetAmtsCollectionPartIdType(StateMod_Well_CollectionPartIdType.RECEIPT));
	}
	// Add to the return list and return the error count...
	hbwellrList.add ( hbwellr );
	return warningCount;
}

/**
Helper method to read well rights from HydroBase, when reading from the "wells" table.
A single well (WDID or receipt) is processed, resulting in one or more HydroBase_NetAmts objects being returned.
This method handles whether to use well rights or permits.
@param hdmi HydroBaseDMI instance for queries.
@param commandTag Tag to use when printing messages.
@param warningCount Warning count to use when printing messages.  Increments
in the warning count is handled by passing the updated value back to the calling code.
@param routine Routine to use when printing messages.
@param wellStationId Well station identifier that is being processed.
@param partId well part ID that is being processed, same as station ID or part in a collection.
@param partIdType the part ID type, either 
@param defineWellRightHow _EarliestDate to define the StateMod right using the
earliest of the well right appropriation date and permit date;
_LatestDate to define the StateMod right using the
latest of the well right appropriation date and permit date;
_RightIfAvailable If true, define the StateMod right using
the well right appropriation date, if the well right is available.  Otherwise use the permit date.
@param useApex If true, add the APEX data values to the net amount right
value. If false, do not add APEX.
@param defaultAdminNumber Default administration number to use if a date
cannot be determined from the data.
@param defaultApproDate default appropriation date corresponding to defaultAdminNumber
@param permitIdPreFormat a format using String.format() specifier to format a permit receipt
(for example use "%s:P" to use legacy behavior to indicate permit).
@param smWellRightList The list to return right(s) in.  The right(s) are
transferred to the main list in the calling code.
@return the number of errors that were found during processing.
@exception Exception Pass errors to the calling code.
*/
protected static int readWellRightsFromHydroBaseWellsHelper (
	HydroBaseDMI hdmi,
	String commandTag, int warningLevel, int warningCount, CommandStatus status,
	String routine,
	String wellStationId,
	StateMod_Well_CollectionType collectionType,
	StateMod_Well_CollectionPartType collectionPartType,
	String partId,
	StateMod_Well_CollectionPartIdType partIdType,
	DefineWellRightHowType defineWellRightHow,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	List<StateMod_WellRight> smWellRightList )
	throws Exception
{	String message; // For messages
	// Read rights corresponding to the part
	// First read from the HydroBase vw_CDSS_Wells view
	List<HydroBase_Wells> hbWellsList = null;
	int wdidParts[] = null;
	if ( partIdType == StateMod_Well_CollectionPartIdType.WDID ) {
		// Read rights for well structure WDID
		// Split the WDID into parts in case it is not always 7 digits
		wdidParts = HydroBase_WaterDistrict.parseWDID(partId,null);
		hbWellsList = hdmi.readWellsList(null, wdidParts[0], wdidParts[1]);
	}
	else if ( partIdType == StateMod_Well_CollectionPartIdType.RECEIPT ) {
		// Read rights for well permit receipt
		// Split the WDID into parts in case it is not always 7 digits
		hbWellsList = hdmi.readWellsList(partId, -1, -1);
	}
	else {
		// Bad input
		message = "Requested unknown part type \"" + partIdType + "\" for well station \"" + wellStationId + "\" partID \"" + partId + "\".";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify that specified well ID type is correct (WDID or Receipt)." ) );
	}
	// There should only be one entry for returned value, matching a single well
	if ( hbWellsList.size() == 0 ) {
		// No well was matched so input data is in error
		message = "Requested well " + partIdType + " for well station \"" + wellStationId + "\" partId \"" + partId + "\" matches no records in vw_CDSS_Wells";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that specified well ID is correct." ) );
	}
	else if ( hbWellsList.size() != 1 ) {
		// Expecting 1 record so HydroBase is in error (duplicate receipt or WDID)
		message = "Requested well " + partIdType + " for well station \"" + wellStationId + "\" partID \"" + partId + "\" matches "
			+ hbWellsList.size() + " records in vw_CDSS_Wells but expecting exactly 1 match";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Verify that HydroBase vw_CDSS_Wells table contents are correct - has duplicate permit/WDID matches." ) );
	}
	else {
		// Now have exactly one HydroBase_Wells object, which contains receipt/WDID cross-reference.
		// However, one of the parts may be missing.
		// Base what data are used by the part type that was requested.
		HydroBase_Wells hbWells = hbWellsList.get(0);
		if ( partIdType == StateMod_Well_CollectionPartIdType.RECEIPT ) {
			// Use the data directly
			message = "Requested well " + partIdType + " for well station \"" + wellStationId +
					"\" - RECEIPT is requested so using receipt data from Wells table";
			StateMod_WellRight smWellRight = new StateMod_WellRight();
			double yieldGPM = hbWells.getYield();
			double yieldApexGPM = hbWells.getYield_apex();
			Date permitDate = hbWells.getPerm_date();
			Date approDate = hbWells.getAppr_date();
			int wd = hbWells.getWD();
			int id = hbWells.getID();
			// Set as many data in the object as possible, including extended data for troubleshooting
			smWellRight.setCgoto(wellStationId);
			smWellRight.setCollectionPartId(partId);
			smWellRight.setCollectionPartIdType(partIdType);
			smWellRight.setCollectionPartType(collectionPartType);
			smWellRight.setCollectionType(collectionType);
			// Decree is the well yield converted from GPM to CFS, or zero if missing
			smWellRight.setDecree((yieldGPM < 0.0 || Double.isNaN(yieldGPM) ? 0.0 : yieldGPM*.002228));
			if ( yieldGPM <= 0 ) {
				message = "Requested well " + partIdType + " for well station \"" + wellStationId +
					"\" yield (" + yieldGPM + ") is missing - setting to zero";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that HydroBase vw_CDSS_Wells table contents are correct - using 0.0 for right." ) );
			}
			smWellRight.setID(hbWells.getReceipt());
			// For "irtem", convert permit date to administration number
			if ( permitDate == null ) {
				if ( defaultApproDate == null ) {
					message = "Requested well " + partIdType + " for well station \"" + wellStationId +
						"\" permit date is missing and no default appropriation date provided - cannot add permit";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Verify that HydroBase vw_CDSS_Wells table contents are correct - need to assign permit date." ) );
				}
				else {
					// Use the default appropriation date for permit date
					permitDate = defaultApproDate;
					smWellRight.setIrtem(String.format("%.5f",defaultAdminNumber));
					Message.printStatus(2,routine,"          Setting appropriation date for well to default " + String.format("%.5f",defaultAdminNumber) );
				}
			}
			else {
				// Have a permit date so use it
				DateTime dt = new DateTime(permitDate);
				HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber(dt);
				smWellRight.setIrtem(an.toString());
				smWellRight.setXPermitDateAdminNumber(an.toString());
			}
			// Parcel information left as missing since not used
			smWellRight.setXApproDate(approDate);
			if ( approDate != null ) {
				// Set appropriation date for FYI
				DateTime dt = new DateTime(approDate);
				HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber(dt);
				smWellRight.setXApproDateAdminNumber(an.toString());
			}
			smWellRight.setXPermitDate(permitDate);
			smWellRight.setXPermitReceipt(hbWells.getReceipt());
			if ( (wd > 0) && (id > 0) ) {
				smWellRight.setXWDID(HydroBase_WaterDistrict.formWDID(wd, id));	
			}
			smWellRight.setXYieldGPM(yieldGPM);
			if ( (yieldApexGPM >= 0.0) && !Double.isNaN(yieldApexGPM)) {
				smWellRight.setXYieldApexGPM(yieldApexGPM);
			}
			smWellRight.setName(hbWells.getWell_name());
			// Set extended data to cross-reference...
			smWellRight.setXApproDate(hbWells.getAppr_date());
			if ( hbWells.getWd_id() > 0 ) {
				smWellRight.setXWDID("" + hbWells.getWd_id());
			}
			// Warn if receipt was requested but right looks like it has WDID/right data - should adjust input
			if ( approDate != null ) {
				message = "    Requested well " + partIdType + " for well station \"" + wellStationId +
					"\" permit date is missing but have appropriation date from water right";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Recommend using the well WDID instead of receipt when defining well station." ) );
			}
			// Finally, add the right to the returned list
			smWellRightList.add(smWellRight);
		}
		else {
			// Requested a WDID so read from NetAmts using WDID, may return 0 or more records
			message = "    Requested well " + partIdType + " for well station \"" + wellStationId +
					"\" - WDID is requested so reading from HydroBase NetAmts table";
			boolean positiveNetRateAbs = false;
			boolean oldList = false;
			List<String> orderByList = null;
			List<HydroBase_NetAmts> hbNetAmtsList = hdmi.readNetAmtsList(-1, wdidParts[0], wdidParts[1],
				positiveNetRateAbs, orderByList, oldList );
			// Further filter rights
			// TODO SAM 2016-10-03 evaluate adding use criteria to the above read method
			HydroBase_NetAmts tmp;
			boolean doRemove = false;
			for ( int i = hbNetAmtsList.size() -1; i >= 0; i-- ) {
				doRemove = false;
				tmp = hbNetAmtsList.get(i);
				if ( tmp.getUse().toUpperCase().indexOf("IRR") < 0 ) {
					// Include rights only if use includes IRR, and this one does not
					doRemove = true;
				}
				else {
					// Also check whether absolute or APEX
					if ( useApex ) {
						if ( !tmp.getAbs().equalsIgnoreCase("Y") && !tmp.getApex().equalsIgnoreCase("Y") ) {
							// Neither abs or apex is Y so remove
							doRemove = true;
						}
					}
					else {
						// Not including APEX so only include absolute
						if ( !tmp.getAbs().equalsIgnoreCase("Y") ) {
							// Only include absolute rights
							doRemove = true;
						}
					}
				}
				//Message.printStatus(2,routine,"getUse()=" + tmp.getUse() + " getAbs()=" + tmp.getAbs() + " getApex()=" + tmp.getApex() + " doRemove="+doRemove );
				if ( doRemove ) {
					hbNetAmtsList.remove(i);
				}
			}
			// There should be at least one water right
			if ( hbNetAmtsList.size() == 0 ) {
				// No well was matched so input data is in error
				if ( useApex ) {
					message = "      Requested well " + partIdType + " for well station \"" + wellStationId
							+ "\" part ID \"" + partId + "\" matches no abs or APEX, IRR records in the NetAmts table";
				}
				else {
					message = "      Requested well " + partIdType + " for well station \"" + wellStationId
						+ "\" part ID \"" + partId + "\" matches no abs, IRR records in the NetAmts table";
				}
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that specified well WDID is correct - maybe water right is abandoned?" ) );
			}
			else {
				// Convert the NetAmts records into StateMod well rights
				Message.printStatus(2, routine, "      Requested well " + partIdType + " for well station \"" + wellStationId +
						"\" partId = \"" + partId + "\" - has " + hbNetAmtsList.size() + " rights from the NetAmts table:" );
				for ( HydroBase_NetAmts hbNetAmts : hbNetAmtsList ) {
					String unit = hbNetAmts.getUnit();
					double net_rate_apex = hbNetAmts.getNet_rate_apex();
					double net_rate_abs = hbNetAmts.getNet_rate_abs();
					Message.printStatus(2,routine,"        WDID = " + partId
						+ ", appro_date = " + hbNetAmts.getApro_date()
						+ ", admin_no = " + String.format("%.5f",hbNetAmts.getAdmin_no())
						+ ", net_rate_abs = " + net_rate_abs
						+ ", unit = " + unit
						+ ", net_rate_apex = " + hbNetAmts.getNet_rate_apex());
					if ( !unit.equalsIgnoreCase("C") ) {
						Message.printStatus(2,routine,"          Skipping because units are not C (cfs)");
						continue;
					}
					if ( (net_rate_abs < 0) || Double.isNaN(net_rate_abs) ) {
						Message.printStatus(2,routine,"          Setting net_rate_abs (" + net_rate_abs + ") to zero for computations.");
						net_rate_abs = 0.0;
					}
					// Use the data directly
					StateMod_WellRight smWellRight = new StateMod_WellRight();
					// Set as much data in the object as possible, including extended data for troubleshooting
					smWellRight.setCgoto(wellStationId);
					smWellRight.setCollectionPartId(partId);
					smWellRight.setCollectionPartIdType(partIdType);
					smWellRight.setCollectionPartType(collectionPartType);
					smWellRight.setCollectionType(collectionType);
					// Decree is the well yield converted from GPM to CFS, or zero if missing
					if ( useApex ) {
						// Add the apex to the decree
						if ( (net_rate_apex < 0.0) || Double.isNaN(net_rate_apex) ) {
							// Reset value to zero
							net_rate_apex = 0.0;
						}
						if ( net_rate_apex > 0.0 ) {
							Message.printStatus(2,routine,"          Adding net_rate_apex " + String.format("%.2f",net_rate_apex) + " to net_rate_abs ("
								+ String.format("%.2f",net_rate_abs) + ") as requested");
						}
					}
					else {
						// APEX not added to decree
						net_rate_apex = 0.0;
					}
					if ( (net_rate_abs + net_rate_apex) <= 0.0 ) {
						if ( useApex ) {
							Message.printStatus(2,routine,"          Skipping because net_rate_abs + net_rate_apex = 0" );
						}
						else {
							Message.printStatus(2,routine,"          Skipping because net_rate_abs = 0 (APEX has not been requested to be added)" );
						}
						continue;
					}
					smWellRight.setDecree(net_rate_abs + net_rate_apex);
					smWellRight.setID(partId);
					// Administration number out of the database
					smWellRight.setIrtem(String.format("%.5f",hbNetAmts.getAdmin_no()));
					// Set extended data directly relevant...
					smWellRight.setXApproDate(hbNetAmts.getApro_date()); // Same as main data
					smWellRight.setXApproDateAdminNumber(String.format("%.5f",hbNetAmts.getAdmin_no())); // Same as main data
					smWellRight.setXWDID(partId);
					smWellRight.setXUse(hbNetAmts.getUse());
					// Set extended data for permits for cross-check
					smWellRight.setXPermitReceipt(hbWells.getReceipt());
					smWellRight.setXYieldApexGPM(hbWells.getYield_apex());
					smWellRight.setXPermitDate(hbWells.getPerm_date());
					// Finally, add the right to the returned list
					smWellRight.setName(hbNetAmts.getWr_name());
					smWellRightList.add(smWellRight);
				}
			}
		}
	}
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
protected static void warnAboutDataMatches ( String command, boolean replace, List<String> match_Vector,
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
		id = match_Vector.get(i);
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
