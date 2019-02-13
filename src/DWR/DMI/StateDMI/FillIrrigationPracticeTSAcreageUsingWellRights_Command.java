// FillIrrigationPracticeTSAcreageUsingWellRights_Command - This class initializes, checks, and runs the FillIrrigationPracticeTSAcreageUsingWellRights() command.

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

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Util;

import RTi.TS.TSUtil;
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
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
<p>
This class initializes, checks, and runs the FillIrrigationPracticeTSAcreageUsingWellRights() command.
</p>
*/
public class FillIrrigationPracticeTSAcreageUsingWellRights_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "False";
protected final String _True = "True";

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

// High efficiency irrigation types...

protected final String _SPRINKLER = "SPRINKLER";
protected final String _DRIP = "DRIP";

/**
Constructor.
*/
public FillIrrigationPracticeTSAcreageUsingWellRights_Command ()
{	super();
	setCommandName ( "FillIrrigationPracticeTSAcreageUsingWellRights" );
}

/**
Fill the irrigation practice time series at a location using well water rights.
Do this by getting the
irrigation practice data for the parcel year (which has references to the parcels used to
create the data) and check those parcels for whether they have rights in a
year.  If they do, include the parcels.
@param smrights_YearTS_Vector Vector of YearTS for the rights for parcels.  This
will be used to look up a time series for the matching parcel.
@param ipyts The IrrigationPracticeTS that is having a component filled, passed in because
the stored parcel information is at this level.
@param cdsts The CropPatternTS that is checked to make sure that total acreage is OK.
@param datatype The time series data type being filled.
It is also passed in to avoid issues pulling out the data type from a composite
data type in "yts".  CURRENTLY IGNORED - ALWAYS FILLED.
@param FillStart_DateTime The starting date/time for filling.
@param FillEnd_DateTime The ending date/time for filling.
@param ParcelYear_int the parcel year to use for parcel data.
*/
private void fillIrrigationPracticeTSUsingRights (
		List smrights_YearTS_Vector,
		StateCU_IrrigationPracticeTS ipyts,
		StateCU_CropPatternTS cdsts,
		String datatype,
		DateTime FillStart_DateTime,
		DateTime FillEnd_DateTime,
		int ParcelYear_int,
		boolean has_gwonly_supply )
{	String routine = "fillIrrigationPracticeTSAcreageUsingWellRights.fillIrrigationPracticeTSUsingRights";
	Message.printStatus( 2, routine, "Filling irrigation practice acreage time series for \"" +
		ipyts.getID() + "\" by using " + ParcelYear_int +
		" year parcel data and rights associated with parcels.");
	// Get the parcels for the irrigation practice TS for the year in
	// question.  This will return HydroBase and user-supplied records.
	List parcels = ipyts.getParcelListForYear ( ParcelYear_int );
	int nparcel = 0;
	if ( parcels != null ) {
		nparcel = parcels.size();
	}
	Message.printStatus( 2, routine, "Have " + nparcel +
			" parcels for " + ParcelYear_int + " to check for non-zero decree." );
		StateCU_Parcel parcel = null;	// Individual parcel to process.
	// Debugging...
	/*for ( int i = 0; i < nparcel; i++ ) {
		parcel = (StateCU_Parcel)parcels.elementAt(i);
		Message.printStatus ( 2, routine, parcel.toString() );
	}*/
	YearTS parcel_right_ts = null;	// Time series of rights for parcel
	int pos = 0;			// Position in right time series array
	
	// Get the component time series that will be processed...
	
	String id = ipyts.getID();
	YearTS Tacre_ts = ipyts.getTacreTS();
	/* FIXME SAM 2007-10-18 Remove when code tests out.
	YearTS Sacre_ts = ipyts.getSacreTS();
	YearTS Gacre_ts = ipyts.getGacreTS();
	*/
	YearTS Acgwfl_ts = ipyts.getAcgwflTS();
	YearTS Acgwspr_ts = ipyts.getAcgwsprTS();
	YearTS Acswfl_ts = ipyts.getAcswflTS();
	YearTS Acswspr_ts = ipyts.getAcswsprTS();
	
	// First figure out if the time series is missing for a year.  If the original
	// value was NOT missing, then filling can be skipped below.
	
	int nyears = FillEnd_DateTime.getYear() - FillStart_DateTime.getYear() + 1;
	boolean [] ismissing_Tacre = new boolean[nyears];
	boolean [] ismissing_Sacre = new boolean[nyears];
	boolean [] ismissing_Gacre = new boolean[nyears];
	boolean [] ismissing_Acgwfl = new boolean[nyears];
	boolean [] ismissing_Acgwspr = new boolean[nyears];
	boolean [] ismissing_Acswfl = new boolean[nyears];
	boolean [] ismissing_Acswspr = new boolean[nyears];

	DateTime date = new DateTime(FillStart_DateTime);
	int year = 0; // year to process
	double old_value; // Old time series value to check
	for ( int iyear = 0;
		date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1), iyear++ ) {
		old_value = Tacre_ts.getDataValue ( date );
		if ( Tacre_ts.isDataMissing(old_value)) {
			ismissing_Tacre[iyear] = true;
		}
		else {
			ismissing_Tacre[iyear] = false;
		}
		/* FIXME SAM 2007-10-18 Remove when code tests out.
		old_value = Sacre_ts.getDataValue ( date );
		if ( Sacre_ts.isDataMissing(old_value)) {
			ismissing_Sacre[iyear] = true;
		}
		else {
			ismissing_Sacre[iyear] = false;
		}
		old_value = Gacre_ts.getDataValue ( date );
		if ( Gacre_ts.isDataMissing(old_value)) {
			ismissing_Gacre[iyear] = true;
		}
		else {
			ismissing_Gacre[iyear] = false;
		}
		*/
		old_value = Acgwfl_ts.getDataValue ( date );
		if ( Acgwfl_ts.isDataMissing(old_value)) {
			ismissing_Acgwfl[iyear] = true;
		}
		else {
			ismissing_Acgwfl[iyear] = false;
		}
		old_value = Acgwspr_ts.getDataValue ( date );
		if ( Acgwspr_ts.isDataMissing(old_value)) {
			ismissing_Acgwspr[iyear] = true;
		}
		else {
			ismissing_Acgwspr[iyear] = false;
		}
		old_value = Acswfl_ts.getDataValue ( date );
		if ( Acswfl_ts.isDataMissing(old_value)) {
			ismissing_Acswfl[iyear] = true;
		}
		else {
			ismissing_Acswfl[iyear] = false;
		}
		old_value = Acswspr_ts.getDataValue ( date );
		if ( Acswspr_ts.isDataMissing(old_value)) {
			ismissing_Acswspr[iyear] = true;
		}
		else {
			ismissing_Acswspr[iyear] = false;
		}
	}
	
	// If no parcels were irrigated in the indicator year, fill acreage with zero.  The results
	// SHOULD match the CDS total of zero but send through the same code for error handling.
	
	if ( nparcel == 0 ) {
		// Process one zero record to set all values to zero...
		Message.printStatus( 2, routine, "There are no parcels for " + ParcelYear_int +
			" - set all acreage to zero for set period." );
		processSingleParcelForPeriod ( FillStart_DateTime,
				FillEnd_DateTime,
				false,		// No parcel rights available
				null,		// Parcel time series
				id,
				"NoParcels",// Dummy parcel ID
				0.0,		// Decree
				false,		// Is high efficiency?
				false,		// has GW supply?
				true,		// All data are zero
				ipyts,
				Tacre_ts,
				/* FIXME SAM 2007-10-18 Remove when code tests out.
				Sacre_ts,
				Gacre_ts,
				*/
				Acgwfl_ts,
				Acgwspr_ts,
				Acswfl_ts,
				Acswspr_ts,
				ismissing_Tacre,
				ismissing_Sacre,
				ismissing_Gacre,
				ismissing_Acgwfl,
				ismissing_Acgwspr,
				ismissing_Acswfl,
				ismissing_Acswspr );
	}
	else {
		// Loop through the parcels...
	
		boolean has_gw_supply;
		boolean is_high_efficiency;
		String irrig_type;
		double parcel_area;		// Parcel area
		boolean rights_available = false;	// Indicates if rights are available for parcel
		for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
			parcel = (StateCU_Parcel)parcels.get(iparcel);
			parcel_area = parcel.getArea();
			irrig_type = parcel.getIrrigationMethod();
			is_high_efficiency = false;
			if ( irrig_type.equalsIgnoreCase(_SPRINKLER) ||
				irrig_type.equalsIgnoreCase(_DRIP) ) {
				is_high_efficiency = true;
			}
			has_gw_supply = parcel.hasGroundWaterSupply();
			if ( parcel_area < 0.0 ) {
				// No need to process - this should not happen...
				continue;
			}
			has_gw_supply = parcel.hasGroundWaterSupply();
			// Get the time series of rights corresponding to the parcel...
			pos = TSUtil.indexOf ( smrights_YearTS_Vector, parcel.getID(), "Location", 1 );
			if ( pos < 0 ) {
				// No time series for the parcel.
				Message.printStatus( 2, routine,
					"No time series of rights available for parcel \"" + parcel.getID() +
					"\".  Processing as if decree=0 for whole fill period." );
				rights_available = false;
			}
			else {
				rights_available = true;
				parcel_right_ts = (YearTS)smrights_YearTS_Vector.get(pos);
				Message.printStatus ( 2, routine, "Using right time series for parcel \"" + parcel.getID() + "\"");
			}
			// Now loop through the years to fill...

			processSingleParcelForPeriod ( FillStart_DateTime,
				FillEnd_DateTime,
				rights_available,
				parcel_right_ts,
				id,
				parcel.getID(),
				parcel_area,
				is_high_efficiency,
				has_gw_supply,
				false,				// Not all zero
				ipyts,
				Tacre_ts,
				/* FIXME SAM 2007-10-18 Remove when code tests out.
				Sacre_ts,
				Gacre_ts,
				*/
				Acgwfl_ts,
				Acgwspr_ts,
				Acswfl_ts,
				Acswspr_ts,
				ismissing_Tacre,
				ismissing_Sacre,
				ismissing_Gacre,
				ismissing_Acgwfl,
				ismissing_Acgwspr,
				ismissing_Acswfl,
				ismissing_Acswspr );

		} // End parcels for location
	} // Has parcels
	// TODO SAM 2007-09-11 Need CommandStatus
	// Now adjust the data to ensure acreage adds up.
	// Warnings to user do not occur through exceptions.
	// Need to check the file integrity after processing and
	// migrate to new command status design here.
	for ( date = new DateTime(FillStart_DateTime); date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1) ) {
		// Refresh the totals
		year = date.getYear();
		ipyts.refreshAcgw ( year );
		ipyts.refreshAcsw ( year );
		// Cascade the checks to the other terms...
		ipyts.adjustGroundwaterAcresToTotalAcres ( date, has_gwonly_supply );
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
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	//String DataType = parameters.getValue ( "DataType" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "A location identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}
	if ( (IncludeSurfaceWaterSupply != null) && (IncludeSurfaceWaterSupply.length() > 0) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_True) &&
		!IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) ) {
		message = "The IncludeSurfaceWaterSupply (" + IncludeSurfaceWaterSupply + ") value is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeSurfaceWaterSupply parameter as " + _False + " or " + _True +
				" (default)." ) );
	}
	if ( (IncludeGroundwaterOnlySupply != null) && (IncludeGroundwaterOnlySupply.length() > 0) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_True) &&
		!IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) ) {
		message = "The IncludeGroundwaterOnlySupply (" + IncludeGroundwaterOnlySupply + ") value is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the IncludeGroundwaterOnlySupply parameter as " + _False + " or " + _True +
				" (default)." ) );
	}
	/* Need to allow ditches to be filled.
	if ( (IncludeSurfaceWaterSupply != null) &&
			IncludeSurfaceWaterSupply.equalsIgnoreCase(_True)) {
		warning += "\nIncludeSurfaceWaterSupply=True CANNOT currently be used when filling with rights.";
	}
	*/
	
	if ( (FillStart != null) && (FillStart.length() != 0) && !StringUtil.isInteger(FillStart)) {
		message = "The fill start (" + FillStart + ") is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the fill start as an integer year YYYY." ) );
	}
	
	if ( (FillEnd != null) && (FillEnd.length() != 0) && !StringUtil.isInteger(FillEnd)) {
		message = "The fill end (" + FillEnd + ") is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the fill end as an integer year YYYY." ) );
	}
	if ( (ParcelYear == null) || (ParcelYear.length() == 0) ) {
		message = "The ParcelYear must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the ParcelYear as a year YYYY." ) );
	}
	else if ( !StringUtil.isDouble(ParcelYear) ) {
		message = "The ParcelYear (" + ParcelYear + ") is not a valid integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the ParcelYear as a year YYYY." ) );
	}

	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "IncludeSurfaceWaterSupply" );
	valid_Vector.add ( "IncludeGroundwaterOnlySupply" );
	valid_Vector.add ( "ParcelYear" );
	valid_Vector.add ( "FillStart" );
	valid_Vector.add ( "FillEnd" );
	valid_Vector.add ( "IfNotFound" );
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
	return (new FillIrrigationPracticeTSAcreageUsingWellRights_JDialog ( parent, this )).ok();
}

/**
Process a single parcel (actual or internally assigned as zero) for the full period.
@param FillStart_DateTime Fill start.
*/
private void processSingleParcelForPeriod (
		DateTime FillStart_DateTime,
		DateTime FillEnd_DateTime,
		boolean rights_available,
		YearTS parcel_right_ts,
		String id,
		String parcel_id,
		double parcel_area,
		boolean is_high_efficiency,
		boolean has_gw_supply,
		boolean all_zero,
		StateCU_IrrigationPracticeTS ipyts,
		YearTS Tacre_ts,
		/* FIXME SAM 2007-10-18 Remove when code tests out.
		YearTS Sacre_ts,
		YearTS Gacre_ts,
		*/
		YearTS Acgwfl_ts,
		YearTS Acgwspr_ts,
		YearTS Acswfl_ts,
		YearTS Acswspr_ts,
		boolean [] ismissing_Tacre,
		boolean [] ismissing_Sacre,
		boolean [] ismissing_Gacre,
		boolean [] ismissing_Acgwfl,
		boolean [] ismissing_Acgwspr,
		boolean [] ismissing_Acswfl,
		boolean [] ismissing_Acswspr )
{	String routine = "FillIrrigationPracticeTSAcreageUsingWellRights.processSingleParcelForPeriod";
	DateTime date = new DateTime(FillStart_DateTime);
	int year;
	double parcel_right_decree; // Value of right time series for parcel
	double parcel_area2; // Area that is actually processed
	double old_value; // Old time series value
	for ( int iyear = 0;
		date.lessThanOrEqualTo(FillEnd_DateTime); date.addYear(1), iyear++ ) {
		year = date.getYear();
		// Check to see if the parcel rights in the year in question are
		// > 0.0.  If so, the parcel is on, add its acreage to the appropriate
		// irrigation practice time series.
		if ( rights_available ) {
			parcel_right_decree = parcel_right_ts.getDataValue ( date );
		}
		else {
			parcel_right_decree = 0.0;
		}
		if ( parcel_right_decree <= 0.0 ) {
			// Parcel is off but want to fill with a zero since off is an observation...
			Message.printStatus( 2, routine, "Decree for parcel " + parcel_id + " is OFF in " + year +
				".  Filling with zero where missing.");
			parcel_area2 = 0.0;
		}
		else {
			Message.printStatus( 2, routine, "Decree for parcel " + parcel_id + " is ON for in " + year +
				" based on decree total " + StringUtil.formatString(parcel_right_decree,"%.2f") +
				".  Adding " + StringUtil.formatString(parcel_area,"%.2f") + " acres to IPY time series.");
			parcel_area2 = parcel_area;
		}
		// Only fill if the original time series value was missing.
		/* TODO SAM 2010-02-09 total is no longer set because CDS value is used
		if ( ismissing_Tacre[iyear]) {
			// Total acres are always incremented.
			old_value = Tacre_ts.getDataValue(date);
			if ( Tacre_ts.isDataMissing(old_value) ) {
				// Need to set...
				//Tacre_ts.setDataValue ( date, parcel_area2 );
				//Tacre_filled = parcel_area2;
			}
			else {
				// Increment...
				//ipyts.setTacre ( year, old_value + parcel_area2 );
				//Tacre_filled = old_value + parcel_area2;
			}
			Message.printStatus( 2, "", "For location " + id + " year " + year +
				" added " + StringUtil.formatString(parcel_area2,"%.2f") + " to total area, result = " +
				StringUtil.formatString(ipyts.getTacre(year),"%.2f") );
		}
		*/
		if ( !all_zero ) {
		// Old sprinkler acres are set based on irrigation type...
		/* FIXME SAM 2007-10-18 Remove when code tests out.
		if ( is_high_efficiency ) {
			old_value = Sacre_ts.getDataValue(date);
			if ( Sacre_ts.isDataMissing(old_value) ) {
				// Need to set...
				Sacre_ts.setDataValue ( date, parcel_area2 );
			}
			else {	// Increment...
				Sacre_ts.setDataValue ( date, old_value + parcel_area2 );
			}
			Message.printStatus( 2, "", "For location " + id + " year " + year +
					" added " + StringUtil.formatString(parcel_area2,"%.2f") + " to sprinkler area, result = " +
					StringUtil.formatString(Sacre_ts.getDataValue(date),"%.2f") );
		}
		*/
		if ( has_gw_supply ) {
			/* FIXME SAM 2007-10-18 Remove when code tests out.
			// Old groundwater acres are set based on whether well supplies parcel...
			old_value = Gacre_ts.getDataValue(date);
			if ( Gacre_ts.isDataMissing(old_value) ) {
				// Need to set...
				Gacre_ts.setDataValue ( date, parcel_area2 );
			}
			else {	// Increment...
				Gacre_ts.setDataValue ( date, old_value + parcel_area2 );
			}
			Message.printStatus( 2, "", "For location " + id + " year " + year +
					" added " + StringUtil.formatString(parcel_area2,"%.2f") + " to GW area, result = " +
					StringUtil.formatString(Gacre_ts.getDataValue(date),"%.2f") );
			*/
			if ( is_high_efficiency ) {
				old_value = Acgwspr_ts.getDataValue(date);
				if ( Acgwspr_ts.isDataMissing(old_value) ) {
					// Need to set...
					Acgwspr_ts.setDataValue ( date, parcel_area2 );
				}
				else {
					// Increment...
					Acgwspr_ts.setDataValue ( date, old_value + parcel_area2 );
				}
				Message.printStatus( 2, "", "For location " + id + " year " + year +
					" added " + StringUtil.formatString(parcel_area2,"%.2f") + " to GW sprinkler area, result = " +
					StringUtil.formatString(Acgwspr_ts.getDataValue(date),"%.2f") );
				
			}
			else {
				old_value = Acgwfl_ts.getDataValue(date);
				if ( Acgwfl_ts.isDataMissing(old_value) ) {
					// Need to set...
					Acgwfl_ts.setDataValue ( date, parcel_area2 );
				}
				else {
					// Increment...
					Acgwfl_ts.setDataValue ( date, old_value + parcel_area2 );
				}
				Message.printStatus( 2, "", "For location " + id + " year " + year +
					" added " + StringUtil.formatString(parcel_area2,"%.2f") + " to GW flood area, result = " +
					StringUtil.formatString(Acgwfl_ts.getDataValue(date),"%.2f") );
			}
		}
		} // End all_zero - the following is the fall-through
		// Make sure to fill all groundwater with zero if necessary because result of filling
		// should be no gaps.  The zeros will be incremented with subsequent parcel
		// additions.  Don't set the surface water to zero because that will
		// get filled without rights (e.g, interpolation).
		if ( Tacre_ts.isDataMissing(Tacre_ts.getDataValue(date)) ) {
			//Tacre_ts.setDataValue ( date, 0.0 );
			//Tacre_filled = 0.0;
		}
		/* FIXME SAM 2007-10-18 Remove when code tests out.
		if ( Sacre_ts.isDataMissing(Sacre_ts.getDataValue(date)) ) {
				//Sacre_ts.setDataValue ( date, 0.0 );
		}
		if ( Gacre_ts.isDataMissing(Gacre_ts.getDataValue(date)) ) {
			Gacre_ts.setDataValue ( date, 0.0 );
		}
		*/
		if ( Acgwfl_ts.isDataMissing(Acgwfl_ts.getDataValue(date)) ) {
			Acgwfl_ts.setDataValue ( date, 0.0 );
		}
		if ( Acgwspr_ts.isDataMissing(Acgwspr_ts.getDataValue(date)) ) {
			Acgwspr_ts.setDataValue ( date, 0.0 );
		}
		if ( Acswfl_ts.isDataMissing(Acswfl_ts.getDataValue(date)) ) {
			//Acswfl_ts.setDataValue ( date, 0.0 );
		}
		if ( Acswspr_ts.isDataMissing(Acswspr_ts.getDataValue(date)) ) {
			//Acswspr_ts.setDataValue ( date, 0.0 );
		}
		// Refresh the ground water total and surface water only totals will occur in calling code.
	} // End year
}

/**
Method to execute the fillIrrigationPracticeTSAcreageUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String DataType = parameters.getValue ( "DataType" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	boolean IncludeSurfaceWaterSupply_boolean = true;
	if ( IncludeSurfaceWaterSupply == null ) {
		IncludeSurfaceWaterSupply = _True; // Default
	}
	if (  IncludeSurfaceWaterSupply.equalsIgnoreCase(_True) ) {
		IncludeSurfaceWaterSupply_boolean = true;
	}
	else if ( IncludeSurfaceWaterSupply.equalsIgnoreCase(_False) ) {
		IncludeSurfaceWaterSupply_boolean = false;
	}

	boolean IncludeGroundwaterOnlySupply_boolean = true;
	if ( IncludeGroundwaterOnlySupply == null ) {
		IncludeGroundwaterOnlySupply = _True; // Default
	}
	if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase(_True) ) {
		IncludeGroundwaterOnlySupply_boolean = true;
	}
	else if ( IncludeGroundwaterOnlySupply.equalsIgnoreCase(_False) ) {
		IncludeGroundwaterOnlySupply_boolean = false;
	}

	int ParcelYear_int = Integer.parseInt ( ParcelYear );
	
	// Get the time series to fill...
	
	List ipyList = null;
	int ipyListSize = 0;
	try {
		ipyList = (List)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
		ipyListSize = ipyList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting irrigation practice time series data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( ipyListSize == 0 ) {
		message = "No irrigation practice time series are defined.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateIrrigationPracticeTSForCULocations() before this command." ) );
	}
	
	// Get the crop pattern time series to check that GWflood + GWsprinkler equal to CDS total acres.
	
	List cdsList = null;
	int cdsListSize = 0;
	try {
		Object o = processor.getPropContents( "StateCU_CropPatternTS_List");
		cdsList = (List)o;
		cdsListSize = cdsList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropPatternTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( cdsListSize == 0 ) {
		message = "No crop pattern time series are defined - " +
			"needed to check that GWflood + GWsprinkler = CDS total.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Read the time series before this command." ) );
	}
	
	// Get the CU locations, which is where collection information is stored,
	// necessary to determine if groundwater only location...
	
	List culocList = null;
	int culocListSize = 0;
	try {
		culocList = (List)processor.getPropContents( "StateCU_Location_List");
		culocListSize = culocList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( culocListSize == 0 ) {
		message = "No CU locations are available defined - needed to check if groundwater only location.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Read CU locations before this command." ) );
	}
	
	// Get the well rights, which are needed to create the time series by parcel...
	
	List werList = null;
	int werListSize = 0;
	try {
		werList = (List)processor.getPropContents( "StateMod_WellRight_List");
		werListSize = werList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_WellRight_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( werListSize == 0 ) {
		message = "No well rights available to fill irrigation practice time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Read well rights before using this command." ) );
	}
	
	// Get the period to fill (default if fill period is not specified)...
	
	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	try {
		OutputStart_DateTime = (DateTime)processor.getPropContents("OutputStart");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputStart from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	try {
		OutputEnd_DateTime = (DateTime)processor.getPropContents("OutputEnd");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputEnd from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	DateTime FillStart_DateTime = null;
	if ( StringUtil.isInteger(FillStart) ) {
		FillStart_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
		FillStart_DateTime.setYear ( StringUtil.atoi(FillStart));
	}
	else if ( OutputStart_DateTime != null ) {
		FillStart_DateTime = new DateTime(OutputStart_DateTime);
		FillStart_DateTime.setPrecision ( DateTime.PRECISION_YEAR );
	}
	DateTime FillEnd_DateTime = null;
	if ( StringUtil.isInteger(FillEnd) ) {
		FillEnd_DateTime = new DateTime ( DateTime.PRECISION_YEAR );
		FillEnd_DateTime.setYear ( StringUtil.atoi(FillEnd));
	}
	else if ( OutputEnd_DateTime != null ) {
		FillEnd_DateTime = new DateTime(OutputEnd_DateTime);
		FillEnd_DateTime.setPrecision ( DateTime.PRECISION_YEAR );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		// Convert the well rights to annual time series...
		List smrights_YearTS_Vector = null;
		try {
			smrights_YearTS_Vector = StateMod_Util.createWaterRightTimeSeriesList (
				werList,
				TimeInterval.YEAR,
				1,	// Aggregate to parcel
				ParcelYear_int,	// Parcel year for rights
				false,	// Do not include data set totals
				OutputStart_DateTime,
				OutputEnd_DateTime,
				999999.0,	// No special treatment of junior rights
				null,
				null,
				true );	// Do read the data (not just header)
		Message.printStatus ( 2, routine, "Created " + smrights_YearTS_Vector.size()
				+ " parcel/right time series from well water rights." );
		//for ( int i = 0; i < smrights_YearTS_Vector.size(); i++ ) {
		//	Message.printStatus ( 2, routine, "TSID = " +
		//			((TS)smrights_YearTS_Vector.elementAt(i)).getIdentifier() );
		//}
		}
		catch ( Exception e ) {
			message = "Unable to convert well rights monthly time series (" + e + ").";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that well rights include parcel/and year " + ParcelYear_int ) );
			// Throw exception to break out since serious error.
			throw new CommandException ( message );
		}
		
		// Loop through available irrigation practice time series and see if any need
		// to be filled.  Process each IPY time series together due overlap...
		StateCU_IrrigationPracticeTS ipyts = null;
		StateCU_CropPatternTS cdsts = null;
		StateCU_Location culoc = null;
		String id;	// Location ID
		Message.printStatus ( 2, routine, "Filling irrigation practice time series for " + ipyListSize + " locations.");
		int matchCount = 0;
		for ( int i = 0; i < ipyListSize; i++ ) {
			ipyts =(StateCU_IrrigationPracticeTS)ipyList.get(i);
			id = ipyts.getID();
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine, "Checking CULocation " + id + " against \"" +
				idpattern_Java + "\"" );
			}
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			Message.printStatus ( 2, routine, "Processing data for location " + id );
			// Check whether location supply matches the supply types to be included...
			if (IncludeSurfaceWaterSupply_boolean || IncludeGroundwaterOnlySupply_boolean ) {
				// Need to get the CU location to check for GW-only...
				int pos = StateCU_Util.indexOf ( culocList, id );
				if ( pos < 0 ) {
					message = "Could not find CU location matching \"" + id +
					"\" to determine water supply type.  Not filling.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that CU location is defined for irrigation practice time series." ) );
					continue;
				}
				culoc = (StateCU_Location)culocList.get(pos);
			}
			if ( IncludeSurfaceWaterSupply_boolean && IncludeGroundwaterOnlySupply_boolean ) {
				// Including both...
			}
			else {
				// Check that only one type is included so not everything is skipped.
				// TODO SAM 2007-07-12 Need to fix this
				if ( IncludeSurfaceWaterSupply_boolean && !culoc.hasSurfaceWaterSupply() ) {
					continue;
				}
				if ( IncludeGroundwaterOnlySupply_boolean && !culoc.hasGroundwaterOnlySupply() ) {
					continue;
				}
			}
			int pos = StateCU_Util.indexOf ( cdsList, id );
			if ( pos < 0 ) {
				message = "Could not find crop pattern time series matching \"" + id +
				"\" to check total acres.  Results may not be accurate.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that crop pattern time series is defined for location." ) );
			}
			else {
				cdsts = (StateCU_CropPatternTS)cdsList.get(pos);
			}
			// Fill using rights, for the requested period.
			fillIrrigationPracticeTSUsingRights (
				smrights_YearTS_Vector,
				ipyts, cdsts, DataType,
				FillStart_DateTime,
				FillEnd_DateTime,
				ParcelYear_int,
				culoc.hasGroundwaterOnlySupply() );
			// Increment the count to indicate that some processing has occurred.
			++matchCount;
		}
		// If nothing was matched, take further action...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Irrigation practice time series \"" + ID +
				"\" was not matched: warning and not filling irrigation practice time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
							"  The time series must be created before filling any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Irrigation practice time series \"" + ID +
				"\" was not matched: failing and not filling irrigation practice time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." +
							"  The time series must be created before filling any data." ) );
			}
		}
	}
	catch ( Exception e ) {
	    message = "Unexpected error filling irrigation practice time series (" + e + ").";
	    Message.printWarning ( warning_level, 
	            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
	    Message.printWarning ( 3, routine, e );
	    status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
	    throw new CommandException ( message );
	}
	
	if ( warning_count > 0 ) {
	    message = "There were " + warning_count + " warnings processing the command.";
	    Message.printWarning ( warning_level,
	        MessageUtil.formatMessageTag(
	        command_tag, ++warning_count),
	        routine,message);
	    throw new CommandWarningException ( message );
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
	String IncludeSurfaceWaterSupply = parameters.getValue ( "IncludeSurfaceWaterSupply" );
	String IncludeGroundwaterOnlySupply = parameters.getValue ( "IncludeGroundwaterOnlySupply" );
	String DataType = parameters.getValue ( "DataType" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	
		StringBuffer b = new StringBuffer ();
	
		if ( ID != null && ID.length() > 0 ) {
			b.append ( "ID=\"" + ID + "\"" );
		}
		if ( IncludeSurfaceWaterSupply != null && IncludeSurfaceWaterSupply.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IncludeSurfaceWaterSupply=" + IncludeSurfaceWaterSupply );
		}
		if ( IncludeGroundwaterOnlySupply != null && IncludeGroundwaterOnlySupply.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IncludeGroundwaterOnlySupply=\"" + IncludeGroundwaterOnlySupply + "\"" );
		}
		if ( DataType != null && DataType.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "DataType=\"" + DataType + "\"" );
		}
		if ( FillStart != null && FillStart.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "FillStart=" + FillStart );
		}
		if ( FillEnd != null && FillEnd.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "FillEnd=" + FillEnd );
		}
		if ( ParcelYear != null && ParcelYear.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "ParcelYear=" + ParcelYear );
		}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
