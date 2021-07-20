// ReadIrrigationPracticeTSFromParcels_Command - This class initializes, checks, and runs the ReadIrrigationPracticeTSFromParcels() command.

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
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateCU.StateCU_SupplyFromGW;
import DWR.StateCU.StateCU_SupplyFromSW;
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

/**
This class initializes, checks, and runs the ReadIrrigationPracticeTSFromParcels() command.
Irrigation practice acreage time series data will be completely defined for years with HydroBase data.
The results should agree with acreage totals read with ReadCropPatternTSFromParcels() command.
*/
public class ReadIrrigationPracticeTSFromParcels_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "False";
protected final String _True = "True";

// Year parameter as list of years to process, or null.  Set in
// checkCommandParameters() for use in runCommand().
private int [] __Year_int = null;	// Default = process all years

/**
Constructor.
*/
public ReadIrrigationPracticeTSFromParcels_Command ()
{	super();
	setCommandName ( "ReadIrrigationPracticeTSFromParcels" );
}

/**
Process a single parcel's single supply data and add to the irrigation practice
time series.  This method is called when processing actual parcels and user-supplied supplemental data.
@param debug whether to print additional debug information
@param doSetValues whether to set the data values, false to only set CDS location for each supply line
@param id Location identifier (the main ID, not the aggregate/system part).
@param ipyts StateCU_IrrigationPracticeTS to which parcel data are added.
@param parcel_year Calendar year for parcel data.
@param supply for the parcel
@param status CommandStatus for logging
@return the warning_count, incremented from calling code value
*/
private int processIrrigationPracticeTSParcelSupply (
		boolean debug,
		boolean doSetValues,
		StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts,
		StateCU_Parcel parcel,
		StateCU_Supply supply,
		CommandStatus status,
		CommandPhaseType commandPhase,
		int warningLevel,
		String command_tag,
		int warning_count) {
	String routine = getClass().getSimpleName() + ".processIrrigationPracticeTSParcelSupply";
	// Transfer the parcel data into the IrrigationPracticeTS

	String culocId = culoc.getID();
	String parcelId = parcel.getID();
	int parcelYear = parcel.getYear();
	String irrigMethod = parcel.getIrrigationMethod();

	StateCU_SupplyFromSW supplyFromSW = null;
	StateCU_SupplyFromGW supplyFromGW = null;
	boolean supplyIdIsIn = false; // Whether supply ID is in the CULocation
	double areaIrrigFromSupply = 0.0;
	
	boolean isHighEfficiency = false;
	// TODO SAM 2007-06-06 Evaluate whether high efficiency irrigation
	// methods should be specified as a command parameter
	if ( irrigMethod.equalsIgnoreCase("SPRINKLER") || irrigMethod.equalsIgnoreCase("DRIP") ) {
		isHighEfficiency = true;
	}
	
	// THE FOLLOWING LOGIC IS SIMILAR TO ReadCropPatternTSFromParcels() command so update there first and then make consistent here.
	// Total acres are incremented using ReadCropPatternTSFromParcels logic.
	// - the CDS code processes parcels in a loop and then supplies for each parcel,
	//   but for below the loops are in the calling code
	// - if surface parcel has surface water supply and CU Location matches, set the total
	// - else if GW only node set the total if the parcel supply is included in the CU location collection
	// - additional area values are set below for IPY parts and total is checked against overall total
	if ( parcel.hasSurfaceWaterSupply() && !culoc.isGroundwaterOnlySupplyModelNode() ) {
		// DIV or D&W 
		if ( debug ) {
			Message.printStatus(2, routine, "            CUloc " + culoc.getID() + " year " + parcelYear +
				" parcel ID " + parcel.getID() + " has SW supply, is a DIV or D&W.");
		}
		// CDS code loops on supply here but calling code contains the loop.
		if ( !supply.getIsModeled() ) {
			// Supply is not modeled (is not in dataset) so skip
			// - typically only impacts GW only node fractional areas but put here in case added for surface water also
			Message.printStatus( 2, routine, "           For location " + culocId + " year " + parcelYear +
				" parcelId=" + parcelId + " skipping supply ID=" + supply.getID() +
				" since supply is not in dataset.");
			if ( !doSetValues ) {
				// Set flag but don't process data.
				//if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
					// Not setting the data values - just set the associated CDS location.
					// CDS use is not set so set to help inform in parcels report
					supply.setIncludeInCdsType(IncludeParcelInCdsType.NOT_MODELED);
				//}
			}
		}
		else if ( supply.isSurfaceWater() ) {
			// May also have groundwater supply, but only count surface water in the total, similar to CDS.
			supplyFromSW = (StateCU_SupplyFromSW)supply;
			boolean doInclude = false;
			String notIncludeMessage = "";
			if ( culoc.isCollection() ) {
				// Collection should ensure that parcels don't show up in more than one collection.
				supplyIdIsIn = culoc.idIsIn(supplyFromSW.getWDID());
				if ( supplyIdIsIn ) {
					doInclude = true;
				}
				else {
					doInclude = false;
					notIncludeMessage = "because is a collection ID and SW supply ID " + supplyFromSW.getID() + " does not match collection part IDs";
				}
			}
			else {
				// Single DIV so only include if the surface supply ID matches the location ID
				supplyIdIsIn = culoc.idIsIn(supplyFromSW.getWDID());
				if ( supplyIdIsIn ) {
					doInclude = true;
				}
				else {
					doInclude = false;
					notIncludeMessage = "because is a single ID and SW supply ID " + supplyFromSW.getID() + " does not match location ID";
				}
			}
			if ( doInclude ) {
				if ( doSetValues ) {
					// Second pass is to set the data values.

					if ( debug ) {
						Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear +
							" parcel ID " + parcel.getID() + " has SW supply, is a DIV or D&W.");
					}
					areaIrrigFromSupply = supplyFromSW.getAreaIrrig();
					// Source supply ID must match the CU Location or collection part
					if ( ipyts.getTacre(parcelYear) < 0.0 ) {
						ipyts.setTacre ( parcelYear, areaIrrigFromSupply );
					}
					else {
						ipyts.setTacre ( parcelYear, (ipyts.getTacre(parcelYear) + areaIrrigFromSupply) );
					}
					Message.printStatus( 2, routine, "           For location " + culocId + " year " + parcelYear +
						" parcelId=" + parcelId + " added SW WDID=" + supplyFromSW.getWDID() +
						" area " + StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to total area, result = " +
						StringUtil.formatString(ipyts.getTacre(parcelYear),"%.2f") );
				}
				else {
					// First pass is to set the relationship.
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.YES ) {
						// Should not happen, should only be set to YES once
						String message = "CUloc " + culoc.getID() + " year " + parcelYear +
							" parcel ID " + parcel.getID() + " supply ID " + culoc.getID() +
							" area was previously added to IPY (location " + supply.getStateCULocationForCds().getID() + ").";
						Message.printWarning ( warningLevel, 
       						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, "    " + message );
      						status.addToLog ( commandPhase,
          						new CommandLogRecord(CommandStatusType.WARNING, message,
          							"Area should only be added once - ditch is included in more than one DIV or D&W model locations." ) );
					}
					//if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
						// CDS use is not set so set to help inform in parcels report
						supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
						// Need to set the following to allow D&W ditch fraction to be computed properly.
						supply.setStateCULocationForCds(culoc);
					//}
				}
			}
			else {
				if ( debug ) {
					Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
						" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
						"\" (" + notIncludeMessage + ")" );
				}
				// TODO smalers 2021-01-30 new code 
				// - supply is not included
				// - only set to know if unknown because don't want to reset
				if ( !doSetValues ) {
					// Only set to unknown in first loop.
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
						supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
					}
				}
			}
		}
		else if ( supply instanceof StateCU_SupplyFromGW ) {
			// Parcel also has groundwater supplies that are not counted since surface water is counted
			if ( debug ) {
				Message.printStatus(2, routine, "    Not adding IPY acreage for " + parcelYear +
					" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
					"\" GW supply " + supply.getID() + " (because parcel has SW supply)");
			}
			// Only set to know if unknown because don't want to reset
			if ( !doSetValues ) {
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
		if ( doSetValues ) {
			Message.printStatus(2, routine, "CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " +
				parcel.getID() + " has groundwater only for model, is WEL.");
			Message.printStatus(2, routine, "  Skipping parcel because has surface supply (will have been added to a D&W).");
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
		}
		else {
			// Match CDS logic.
			// Only set if unknown because don't want to reset.
			if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
				supply.setStateCULocationForCds(null);
				supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
			}
		}
	}
	//else if ( culoc.isGroundwaterOnlySupplyModelNode() ) {
	else if ( !parcel.hasSurfaceWaterSupply() && culoc.isGroundwaterOnlySupplyModelNode() ) {
		// WEL location with groundwater only supply
		Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " +
			parcel.getID() + " is GW only for model, is WEL.");
		if ( !supply.getIsModeled() ) {
			if ( doSetValues ) {
				// Second pass process data.
				// Supply is not modeled (is not in dataset) so skip
				// - typically only impacts GW only node fractional areas but put here in case added for surface water also
				Message.printStatus( 2, routine, "           For location " + culocId + " year " + parcelYear +
					" parcelId=" + parcelId + " skipping supply ID=" + supply.getID() +
					" since supply is not in dataset.");
			}
			else {
				// First pass to set relationships.
				if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
					// CDS use is not set so set to help inform in parcels report.
					supply.setIncludeInCdsType(IncludeParcelInCdsType.NOT_MODELED);
				}
			}
		}
		else if ( supply.isGroundWater() ) {
			// Groundwater only so get the area from the supply.
			supplyFromGW = (StateCU_SupplyFromGW)supply;
			if ( debug ) {
				Message.printStatus(2, routine, "    CUloc " + culoc.getID() + " year " + parcelYear + " parcel ID " + parcel.getID() +
					" supply WDID " + supplyFromGW.getWDID() + " receipt " + supplyFromGW.getReceipt() );
			}
			// OK to check this here because collection is done by specific parts
			supplyIdIsIn = culoc.idIsIn(supplyFromGW.getWDID(), supplyFromGW.getReceipt());
			if ( supplyIdIsIn ) {
				if ( debug ) {
					Message.printStatus(2, routine, "    GW supply WDID " + supplyFromGW.getWDID() +
						" RECEIPT '" + supplyFromGW.getReceipt() + "' is in CULoc" );
				}
				// This culoc is associated with the supply via single ditch or collection.
				// Area for supply was previously calculated as (parcel area) * (ditch percent_irrig)
				// - parcel_years is output and is the list of years with data, across all locations in the command
				if ( doSetValues ) {
					// Second pass is to set values.
					// Source supply ID must match the CU Location or collection part
					// - commingled wells would have resulted in surface supply case above
					if ( ipyts.getTacre(parcelYear) < 0.0 ) {
						ipyts.setTacre ( parcelYear, areaIrrigFromSupply );
					}
					else {
						ipyts.setTacre ( parcelYear, (ipyts.getTacre(parcelYear) + areaIrrigFromSupply) );
					}
					Message.printStatus( 2, routine, "           For location " + culocId + " year " + parcelYear +
						" parcelId=" + parcelId + " added GW WDID=" + supplyFromGW.getWDID() +
						" receipt=" + supplyFromGW.getReceipt() + " area " + StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to total area, result = " +
						StringUtil.formatString(ipyts.getTacre(parcelYear),"%.2f") );
				}
				else {
					// First pass is to set relationships.
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.YES ) {
						// Should not happen, should only be set to YES once
						String message = "CUloc " + culoc.getID() + " year " + parcelYear +
							" parcel ID " + parcel.getID() + " supply ID " + culoc.getID() +
							" area was previously added to IPY (location " + supply.getStateCULocationForCds().getID() + ").";
						Message.printWarning ( warningLevel, 
       						MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, "    " + message );
      						status.addToLog ( commandPhase,
          						new CommandLogRecord(CommandStatusType.WARNING, message,
          							"Area should only be added once - well is included in more than one WEL model locations." ) );
					}
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
						// CDS use is not set so set to help inform in parcels report
						supply.setIncludeInCdsType(IncludeParcelInCdsType.YES);
						// Need to set the following to allow D&W ditch fraction to be computed properly.
						supply.setStateCULocationForCds(culoc);
					}
				}
			}
			else {
				if ( doSetValues ) {
					if ( debug ) {
						Message.printStatus(2, routine, "    Not adding CDS acreage for " + parcelYear +
							" parcelID " + parcelId + " - CULoc \"" + culoc.getID() +
							"\" (because does not have parts matching GW supply WDID " + supplyFromGW.getWDID() +
							" RECEIPT '" + supplyFromGW.getReceipt() + "')" );
					}
				}
				else {
					// Only set to know if unknown because don't want to reset
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN ) {
						supply.setIncludeInCdsType(IncludeParcelInCdsType.NO);
					}
				}
			}
		}
	}
	else {
		// THIS CODE IS COPIED FROM ReadCropPatternTSFromParcels.
		// This should not happen due to location/parcel/supply relationship in the first place
		// This should not happen due to location/parcel/supply relationship in the first place.
		// - there is a lot of code below to figure out why this is a data issue
		String message = "CUloc " + culoc.getID() + " year " + parcelYear +
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
			for ( StateCU_Supply supply2 : parcel.getSupplyList() ) {
				if ( supply2 instanceof StateCU_SupplyFromSW ) {
					supplyFromSW = (StateCU_SupplyFromSW)supply2;
					// Any SW supply should have been added to DIV or D&W
					Message.printWarning(3, routine, "    SW supply WDID = " + supplyFromSW.getWDID() );
					if ( !supplyFromSW.getWDID().isEmpty() ) {
						Message.printWarning(3, routine, "      SW supply WDID is in CU Location " +
							culoc.getCollectionType() + "? = " +  culoc.idIsIn(supplyFromSW.getWDID()) );
					}
				}
				supply2.setIncludeInCdsType(IncludeParcelInCdsType.ERROR);
			}
		}

		Message.printWarning(3, routine, "  Parcel has GW supply = " + parcel.hasGroundWaterSupply() );
		Message.printWarning(3, routine, "  Parcel has " + parcel.getSupplyFromGWCount() + " GW supplies based on parcel data." );
		if ( parcel.getSupplyFromGWCount() > 0 ) {
			for ( StateCU_Supply supply2 : parcel.getSupplyList() ) {
				if ( supply2 instanceof StateCU_SupplyFromGW ) {
					supplyFromGW = (StateCU_SupplyFromGW)supply2;
					Message.printWarning(3, routine, "    GW supply WDID = " + supplyFromGW.getWDID() + " RECEIPT = '" + supplyFromGW.getReceipt() + "'" );
					// Check to see if the well supply is in the original aggregate list for this WEL node
					if ( !supplyFromGW.getWDID().isEmpty() ) {
						supplyIdIsIn = culoc.idIsIn(supplyFromGW.getWDID());
						if ( supplyIdIsIn ) {
							Message.printWarning(3, routine, "      WDID is in CU Location " +
								culoc.getCollectionType() + "? = " + supplyIdIsIn );
						}
						supplyIdIsIn = culoc.idIsIn(supplyFromGW.getReceipt());
						if ( supplyIdIsIn ) {
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
	
	if ( !doSetValues ) {
		// No need to do more in first pass for relationships.
		return warning_count;
	}
	
	// Next add to the IPY component time series based on irrigation method and supply (SW and/or GW).
	// - only set if the parcel fraction was included in the CDS above
	// - this logic is specific to IPY processing and does not have similar code in CDS command

	// Below here, setting values in the objects (second iteration).
	
	if ( !supply.getIsModeled() ) {
		Message.printStatus( 2, routine, "             For location " + culocId + " year " + parcelYear +
			" parcelId=" + parcelId + " supply is not modeled - skipping." );
	}
	else {
		// The parcel/supply fraction is included as a supply.
		// IPY tracks parcel fractions with GW supply (whether or not also SW supply)
		// and fractions with SW only supply.

		// First apply the dwFraction to the area:
		// - not needed for total area above but is needed below for GW area in D&W.
		if ( supply.isGroundWater() ) {
			supplyFromGW = (StateCU_SupplyFromGW)supply;
			areaIrrigFromSupply = supplyFromGW.getAreaIrrig();  // Need because GW acres are not added in D&W above
			Message.printStatus( 2, routine, "             For location " + culocId + " year " + parcelYear +
				" parcelId=" + parcelId + " GW supply is modeled - adding acreage supply parts." );
			// Supply is GW so can be D&W or GW-only node.
			boolean doAddGW = false;
			if ( parcel.hasSurfaceWaterSupply() && !culoc.isGroundwaterOnlySupplyModelNode() ) {
				// D&W GW supply - pacel/supply is not added to CDS total but GW part needs to be added below
				supplyIdIsIn = true; // Well supply from D&W is always included unless it was well-only, handled below.
				if ( supplyIdIsIn ) {
					// The additional fraction for groundwater acreage must be applied from D&W split.
					// - this was not included for default/CDS processing
					double dwFraction = parcel.getSupplyFromSWFraction(culoc.getID());
					areaIrrigFromSupply = areaIrrigFromSupply*dwFraction;
					//if ( debug ) {
						Message.printStatus( 2, routine, "               GW supply WDID=" + supplyFromGW.getWDID() + " receipt=" + supplyFromGW.getReceipt()
							+ " dwFraction=" + StringUtil.formatString(dwFraction,"%.3f") + " areaIrrigFromSupply (using D&W fraction)=" +
							StringUtil.formatString(areaIrrigFromSupply,"%.2f") ); 
					//}
					doAddGW = true;
				}
				else {
					Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
						" parcelId=" + parcelId + " GW supply is not in D&W." );
				}
			}
			else if ( culoc.isGroundwaterOnlySupplyModelNode() ) {
				// Always add if the supply ID is in the collection list
				// supplyIdIsIn was calculated above when processing total area
				if ( supplyIdIsIn ) {
					doAddGW = true;
				}
			}
			else {
				Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
					" parcelId=" + parcelId + " unhandled case for GW supply." );
			}
			if ( doAddGW ) {
				if ( isHighEfficiency ) {
					// Sprinkler or drip irrigation
					if ( ipyts.getAcgwspr(parcelYear) < 0.0 ) {
						// TODO smalers 2020-01-23 actually the number of ditches is already accounted for in areaIrrigFromSupply.
						// - therefore, don't multiply by dwFactor here
						//ipyts.setAcgwspr ( parcelYear, areaIrrigFromSupply*dwFactor );
						ipyts.setAcgwspr ( parcelYear, areaIrrigFromSupply );
					}
					else {
						// TODO smalers 2020-01-23 actually the number of ditches is already accounted for in areaIrrigFromSupply.
						// - therefore, don't multiply by dwFactor here
						//ipyts.setAcgwspr ( parcelYear, ipyts.getAcgwspr(parcelYear) + areaIrrigFromSupply*dwFactor );
						ipyts.setAcgwspr ( parcelYear, ipyts.getAcgwspr(parcelYear) + areaIrrigFromSupply );
					}
					Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
						" parcelId=" + parcelId +" added " +
						//StringUtil.formatString(areaIrrigFromSupply*dwFactor,"%.2f") + " to GW sprinkler area, result = " +
						StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to GW sprinkler area, result = " +
						StringUtil.formatString(ipyts.getAcgwspr(parcelYear),"%.2f") );
				}
				else {
					// Not high efficiency, therefore flood irrigation
					if ( ipyts.getAcgwfl(parcelYear) < 0.0 ) {
						// TODO smalers 2020-01-23 actually the number of ditches is already accounted for in areaIrrigFromSupply.
						// - therefore, don't multiply by dwFactor here
						//ipyts.setAcgwfl ( parcelYear, areaIrrigFromSupply*dwFactor );
						ipyts.setAcgwfl ( parcelYear, areaIrrigFromSupply );
					}
					else {
						// TODO smalers 2020-01-23 actually the number of ditches is already accounted for in areaIrrigFromSupply.
						// - therefore, don't multiply by dwFactor here
						//ipyts.setAcgwfl ( parcelYear, ipyts.getAcgwfl(parcelYear) + areaIrrigFromSupply*dwFactor );
						ipyts.setAcgwfl ( parcelYear, ipyts.getAcgwfl(parcelYear) + areaIrrigFromSupply );
					}
					Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
						" parcelId=" + parcelId +" added " +
						//StringUtil.formatString(areaIrrigFromSupply*dwFactor,"%.2f") + " to GW flood area, result = " +
						StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to GW flood area, result = " +
						StringUtil.formatString(ipyts.getAcgwfl(parcelYear),"%.2f") );
				}
			}
			else {
				Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
					" parcelId=" + parcelId + " not adding GW supply.  ERROR?" );
			}
		}
		else {
			// Surface water supply
			supplyFromSW = (StateCU_SupplyFromSW)supply;
			Message.printStatus( 2, routine, "             For location " + culocId + " year " + parcelYear +
				" parcelId=" + parcelId + " SW supply is modeled - adding acreage supply parts." );
			if ( !parcel.hasGroundWaterSupply() ) {
				// Parcel has surface water only supply so add to SW-only acres.
				Message.printStatus( 2, routine, "             For SW-only parcel, location " + culocId + " year " + parcelYear +
					" SW supply " + supplyFromSW.getWDID() + " supplyIdIsIn=" + supplyIdIsIn );
				// supplyIdIsIn was calculated above when processing total area
				if ( supplyIdIsIn ) {
					// Supply is associated with the single CU Location or a collection part so add to the CU Location
					if ( isHighEfficiency ) {
						// Sprinkler or drip irrigation
						if ( ipyts.getAcswspr(parcelYear) < 0.0 ) {
							ipyts.setAcswspr ( parcelYear, areaIrrigFromSupply );
						}
						else {
							ipyts.setAcswspr ( parcelYear, ipyts.getAcswspr(parcelYear) + areaIrrigFromSupply );
						}
						Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
							" parcelId=" + parcelId +" added " + StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to SW sprinkler area, result = " +
							StringUtil.formatString(ipyts.getAcswspr(parcelYear),"%.2f") );
					}
					else {
						// Flood irrigation
						if ( ipyts.getAcswfl(parcelYear) < 0.0 ) {
							ipyts.setAcswfl ( parcelYear, areaIrrigFromSupply );
						}
						else {
							ipyts.setAcswfl ( parcelYear, ipyts.getAcswfl(parcelYear) + areaIrrigFromSupply );
						}
						Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
							" parcelId=" + parcelId +" added " + StringUtil.formatString(areaIrrigFromSupply,"%.2f") + " to SW flood area, result = " +
							StringUtil.formatString(ipyts.getAcswfl(parcelYear),"%.2f") );
					}
				}
			}
			else {
				Message.printStatus( 2, routine, "               For location " + culocId + " year " + parcelYear +
					" parcelId=" + parcelId + " also has GW supply so not adding to SW-only area." );
			}
		}
	
		// Ensure that in a year that any data value is specified, no missing values will remain.
		// Zeros can be added to with other commands.  Also recompute the totals for surface water only, and for groundwater.
	
		if ( ipyts.getAcgwfl(parcelYear) < 0.0 ) {
			ipyts.setAcgwfl ( parcelYear, 0.0 );
		}
		if ( ipyts.getAcgwspr(parcelYear) < 0.0 ) {
			ipyts.setAcgwspr ( parcelYear, 0.0 );
		}
		// Refresh the GW acres
		ipyts.refreshAcgw(parcelYear);

		if ( ipyts.getAcswfl(parcelYear) < 0.0 ) {
			ipyts.setAcswfl ( parcelYear, 0.0 );
		}
		if ( ipyts.getAcswspr(parcelYear) < 0.0 ) {
			ipyts.setAcswspr ( parcelYear, 0.0 );
		}
		// Refresh the SW acres
		ipyts.refreshAcsw(parcelYear);
	
		// Add parcels to IPY time series for filling later.
		// - TODO smalers 2021-06-03 is this even needed anymore?
		ipyts.addParcel ( parcel );
	} // end modeled
	
	return warning_count;
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
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>();
    validList.add ( "DataStore" );
    validList.add ( "ID" );
    validList.add ( "Year" );
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
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ReadIrrigationPracticeTSFromParcels_JDialog ( parent, this )).ok();
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
Method to execute the readIrrigationPracticeTSFromParcels() command.
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
	
	// Get the list of CU locations...
	
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
        message = "The HydroBase version (" + hbdmi.getDatabaseVersion() + ") is invalid";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Confirm that the HydroBase version is >= 20200720." ) );
	}

	// Do not allow ReadCropPatternTSFromParcels and ReadIrrigationPracticeTSFromParcels commands
	// to be in the same file because they do some of the same bookkeeping.
	List<String> commandsToFind0 = new ArrayList<>();
	commandsToFind0.add("ReadCropPatternTSFromParcels");
	List<Command> commandList = StateDMICommandProcessorUtil.getCommandsAfterIndex(
		-1, processor, commandsToFind0, false);
	if ( commandList.size() > 0 ) {
		// Found a command.
		message = "Cannot use ReadCropPatternTSFromParcels and ReadIrrigationPracticeTSFromParcels "
				+ "in the same command file because they each update parcel tracking data.";
	   	Message.printWarning ( warningLevel,
           	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
   	   	status.addToLog ( commandPhase,
   	       	new CommandLogRecord(CommandStatusType.FAILURE,
   	           	message, "Fix the command file." ) );
		throw new CommandException ( message );
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
		
		// Reset all StateCU_Location CDS tracking for parcels.
		// - the assignments will be made as processing occurs
		// - there may be more than one ReadCropPatternTSFromParcels command but only clear for the first one.
		List<String> commandsToFind = new ArrayList<>();
		commandsToFind.add("ReadIrrigationPracticeTSFromParcels");
		commandList = StateDMICommandProcessorUtil.getCommandsBeforeIndex(
			processor.indexOf(this), processor, commandsToFind, true);
		if ( commandList.size() == 0 ) {
			// No commands before this one so this is the first.
			// Reset the CDS indicators in all supplies.
			Message.printStatus(2,routine,"First ReadIrrigationPraciceTSFromParcels command - setting all parcel supplies to CDS:"
				+ IncludeParcelInCdsType.UNKNOWN);
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
	
		// Year_int is set in checkCommandParameters().  If null, get the years by querying HydroBase...
		int [] parcelYears = null;
		if ( (__Year_int == null) || (__Year_int.length == 0)  ) {
			//parcelYears = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_int );
			parcelYears = StateDMI_Util.readParcelYearListFromParcels ( parcelMap );
			if ( parcelYears == null ) {
				message = "Cannot determine years of parcel data from parcels.";
		    	Message.printWarning ( warningLevel,
	            	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    	       	status.addToLog ( commandPhase,
    	           	new CommandLogRecord(CommandStatusType.FAILURE,
    	               	message, "Report to software support." ) );
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
		
		StateCU_Location culoc = null;	// StateCU location to process (used when do_ipy = true).
		String culocId = null; // Well ID for CU location
		
		int parcelYear = 0;	// Used to process parcel years
		String parcelId;
	
		// Loop through the locations...
		DateTime date = new DateTime ( DateTime.PRECISION_YEAR );
		//boolean debug = true;
		boolean debug = Message.isDebugOn;
		// Loop through 2 times.
		// - iloop=1 makes sure that all CDS locations are assigned, needed to properly calculate D&W fractional areas
		// - iloop=2 does the area calculations
		boolean doSetValues = false;
		for ( int iloop = 1; iloop <= 2; iloop++ ) {
		if ( iloop == 1 ) {
			Message.printStatus(2, routine, "First pass to ensure that CDS locID is set so D&W fraction can be computed in loop 2.");
			doSetValues = false; // Preliminary work
		}
		else if ( iloop == 2 ) {
			Message.printStatus(2, routine, "Second pass to process IPY acreage.");
			doSetValues = true; // Set the data values
		}
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
			// parts of a collection could theoretically vary by year,
			// (but current approach is that collections are constant across the period).
			YearTS yts = null; // Time series to manipulate.
			for ( int iParcelYear = 0;	iParcelYear < parcelYears.length;	iParcelYear++ ) {
				parcelYear = parcelYears[iParcelYear]; // Year to process
				
				if ( iloop == 2 ) {
					if ( culoc.getParcelList(parcelYear).size() == 0 ) {
						Message.printStatus ( 2, routine, "Processing location ID=" + culocId + " parcelYear=" + parcelYear +
							", has 0 irrigated parcels." );
					}
					else {
						Message.printStatus ( 2, routine, "Processing location ID=" + culocId + " parcelYear=" + parcelYear +
							", have " + culoc.getParcelList(parcelYear).size() + " parcels (not all supplies may be modeled)." );
					}
				
					// Set the values in the time series to zero for the parcel year if
					// missing.  Later, values will be added.  This will handled using the
					// ReadIrrigationPracticeTSFromList() command, where aggregate parts might be added incrementally.
					// Calls to ipyts refreshAcsw() and refreshAcgw() will
					// result in these subtotals being updated.
					// Fill commands can then be used for years other than observations.
				
					date.setYear ( parcelYear );
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
					// Data are refreshed below after processing.
				} // end iloop=2

				for ( StateCU_Parcel parcel : culoc.getParcelList(parcelYear) ) {
					parcelId = parcel.getID();
					if ( iloop == 2 ) {
						Message.printStatus ( 2, routine, "  Processing " + culocId + " year=" + parcelYear +
							" parcelId=" + parcelId +
							", parcel has GW supply=" + parcel.hasGroundWaterSupply() +
							", parcel has SW supply=" + parcel.hasSurfaceWaterSupply() +
							", culoc is GW only model node=" + culoc.isGroundwaterOnlySupplyModelNode() );
					}

					// Call utility code that has been used in earlier versions of StateDMI.
					
					//boolean has_gw_supply = parcel.hasGroundWaterSupply();
					for ( StateCU_Supply supply : parcel.getSupplyList() ) {
						warning_count = processIrrigationPracticeTSParcelSupply (
							debug,
							doSetValues, // Will be true if iloop=2
							culoc,
							ipyts,
							parcel,
							supply,
							status, // Applies to the supply, not parcel overall
							commandPhase,
							warningLevel,
							command_tag,
							warning_count
						);
					} // End parcel supplies
				} // End parcels
			
				// Once all the data have been processed, refresh and check the totals.
				if ( iloop == 2 ) {
					// Refresh the total acres and part totals.
					// - missing values were set to zero above so years with data should always have all values
					// - if there is a logic bust, the following comparison will catch it
					ipyts.refreshTacre(parcelYear, true, true);
				} // iloop=2
			} // End parcel year
		} // End location
		} // End iloop
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
	/*
	if ( Div != null && Div.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=\"" + Div + "\"" );
	}
	*/
	
	return getCommandName() + "(" + b.toString() + ")";
}

}