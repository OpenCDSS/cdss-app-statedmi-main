// ReadWellRightsFromHydroBase_Command - This class initializes, checks, and runs the ReadWellRightsFromHydroBase() command.

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
import DWR.StateMod.StateMod_Well_CollectionPartIdType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import DWR.StateMod.StateMod_Well_CollectionType;
import RTi.DMI.DMIUtil;
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
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeZoneDefaultType;

/**
This class initializes, checks, and runs the ReadWellRightsFromHydroBase() command.
*/
public class ReadWellRightsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
//	 Possible values for parameters...
protected final String _False = "False";
protected final String _True = "True";

//Possible values for Approach parameters...
protected final String _Legacy = "Legacy";
protected final String _Simple = "Simple";

//Formats for well right identifiers...

protected final String _HydroBaseID = "HydroBaseID";
protected final String _StationIDW_NN = "StationIDW.NN";
protected final String _StationID_NN = "StationID.NN";
protected final String _StationIDWNN = "StationIDWNN";
protected final String _StationIDNN = "StationIDNN";
protected final String _StationIDW_NNN = "StationIDW.NNN";
protected final String _StationID_NNN = "StationID.NNN";
protected final String _StationIDWNNN = "StationIDWNNN";
protected final String _StationIDNNN = "StationIDNNN";
protected final String _StationIDW_AutoN = "StationIDW.AutoN";
protected final String _StationID_AutoN = "StationID.AutoN";
protected final String _StationIDWAutoN = "StationIDWAutoN";
protected final String _StationIDAutoN = "StationIDAutoN";

// TODO SAM 2016-09-28 Need to make these enum
private final int __HydroBaseID_int = 0;
private final int __StationIDW_NN_int = 1;
private final int __StationID_NN_int = 2;
private final int __StationIDWNN_int = 3;
private final int __StationIDNN_int = 4;
private final int __StationIDW_NNN_int = 5;
private final int __StationID_NNN_int = 6;
private final int __StationIDWNNN_int = 7;
private final int __StationIDNNN_int = 8;
private final int __StationIDW_AutoN_int = 9;
private final int __StationID_AutoN_int = 10;
private final int __StationIDWAutoN_int = 11;
private final int __StationIDAutoN_int = 12;

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
@param hbwellrList list of HydroBase_NetAmts to convert to StateMod rights.
*/
private void addHydroBaseRightsToStateModWellRights (
		String locType,
		String locId,
		List<HydroBase_NetAmts> hbwellrList,
		String DecreeMin,
		double DecreeMin_double,
		String PermitIDPreFormat,
		int IDFormat_int,
		String PermitIDPostFormat,
		int parcelYear, String yearString,
		boolean isCollection,
		StateMod_Well_CollectionType collectionType,
		StateMod_Well_CollectionPartType collectionPartType,
		int onOffDefault,
		List<StateMod_WellRight> SMWellRightList,
		List<String> SMWellRightMatchList,
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = getClass().getSimpleName() + ".addHydroBaseRightsToStateModWellRights";
	String message;	// For messages
	HydroBase_NetAmts hbwellr = null;
	double decree; // HydroBase decree
	String units; // Units for decree
	double irtem; // Admin number used by StateMod
	String decreeString; // Decree as String formatted for output
	StateMod_WellRight wellr; // StateMod right to add
	int wellrCount = 0; // Count of rights for location
	HydroBase_AdministrationNumber adminData; // For on/off switch
	
	int nhbwellr = 0;
	if ( hbwellrList != null ) {
		nhbwellr = hbwellrList.size();
	}
	for ( int ir = 0; ir < nhbwellr; ir++ ) {
		hbwellr = hbwellrList.get(ir);
		// Processing absolute rights...
		decree = hbwellr.getNet_rate_abs();
		units = hbwellr.getUnit();
		if ( decree < DecreeMin_double ) {
			message = yearString + locType + " \"" + locId + "\" has right with decree " + decree +
			" < the minimum (" + DecreeMin + ") skipping...";
			Message.printStatus ( 2, routine, message );
			continue;
		}
		if ( !units.equalsIgnoreCase("C") && !units.equalsIgnoreCase("CFS") ) {
			message = yearString + locType + " \"" + locId + "\" has right with decree units \"" + units +
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
		
		decreeString = StringUtil.formatString(decree,"%.2f");
		if ( decreeString.equalsIgnoreCase("0.00") ) {
			message = yearString + "Well station \"" + locId + "\" has right with decree of zero " +
			"after formatting for output...skipping.";
			Message.printStatus ( 2, routine, message );
		}
		else {
			// Add it...
			++wellrCount;
			wellr = new StateMod_WellRight ();
			wellr.setComment ( "" + parcelYear + " " +
				StringUtil.formatString(hbwellr.getParcelMatchClass(),"%4d") +
				" " + StringUtil.formatString(""+hbwellr.getParcelID(),"%6.6s"));
			wellr.setParcelYear( parcelYear );
			wellr.setParcelMatchClass ( hbwellr.getParcelMatchClass() );
			wellr.setParcelID ( "" + hbwellr.getParcelID() );
			// Set information useful for troubleshooting
			// Because net amount rights were read, parcel/well data are not set
			if ( isCollection ) {
				wellr.setCollectionType(collectionType);
				wellr.setCollectionPartType(collectionPartType);
				wellr.setCollectionPartId(hbwellr.getCommonID());
				// Translate the collection part ID type from HydroBase to StateMod_Well
				wellr.setCollectionPartIdType( StateDMI_Util.lookupWellCollectionPartIdType(
					hbwellr.getCollectionPartIdType()) );
			}
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
				if ( isCollection && (collectionPartType == StateMod_Well_CollectionPartType.PARCEL) ) {
					// As per watright, do not use a "W" in the water right ID...
					wellr.setID ( locId + "." + StringUtil.formatString(wellrCount, "%02d") );
				}
				else {
					// As per watright, use a "W" in the water right ID...
					wellr.setID ( locId + "W." + StringUtil.formatString(wellrCount, "%02d") );
				}
			}
			else {
				// Fall through...
				wellr.setID ( hbwellr.getCommonID() );
			}
			wellr.setName ( hbwellr.getWr_name() );
			wellr.setCgoto ( locId );
			wellr.setIrtem ( StringUtil.formatString(irtem,"%.5f"));
			wellr.setDcrdivw ( decree );
			if ( onOffDefault == __AppropriationDate_int ) {
				// Convert the administration number to a year...
				try { adminData = new HydroBase_AdministrationNumber ( irtem );
					wellr.setSwitch ( adminData.getAppropriationDate().getYear() );
				}
				catch ( Exception e ) {
					message = yearString + "Error converting administration number " + irtem +
					" to date for right \"" + locId + "\".  Setting on/off switch to 1.";
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
				wellr.setSwitch ( onOffDefault );
			}
			// Set additional data that are passed from low-level processing code,
			// used to help understand how well rights are formed from parcel/well data
			wellr.setXWDID(hbwellr.getWDID());
			wellr.setXUse(hbwellr.getUse());
			wellr.setXApproDate(hbwellr.getXApproDate());
			wellr.setXApproDateAdminNumber("");
			if ( hbwellr.getXApproDate() != null ) {
				DateTime dt = new DateTime(hbwellr.getXApproDate());
				try {
					HydroBase_AdministrationNumber adminNum = new HydroBase_AdministrationNumber(dt);
					wellr.setXApproDateAdminNumber(adminNum.toString());
				}
				catch ( Exception e ) {
					// Don't set the admin number
				}
			}
 			wellr.setXPermitReceipt(hbwellr.getXPermitReceipt());
			wellr.setXPermitDate(hbwellr.getXPermitDate());
			if ( hbwellr.getXPermitDate() != null ) {
				DateTime dt = new DateTime(hbwellr.getXPermitDate());
				try {
					HydroBase_AdministrationNumber adminNum = new HydroBase_AdministrationNumber(dt);
					wellr.setXPermitDateAdminNumber(adminNum.toString());
				}
				catch ( Exception e ) {
					// Don't set the admin number
				}
			}
			wellr.setXYieldGPM(hbwellr.getXYieldGPM());
			wellr.setXYieldApexGPM(hbwellr.getXYieldApexGPM());
			wellr.setXFractionYield(hbwellr.getXFractionYield());
			wellr.setXProratedYield(hbwellr.getXProratedYield());
			wellr.setXDitchFraction(hbwellr.getXDitchFraction());
			Message.printStatus ( 2, routine, yearString + "Adding right ID=\"" + wellr.getID() + "\" decree=" +
				StringUtil.formatString(wellr.getDcrdivw(),"%.2f") + " AdminNum=" + wellr.getIrtem() +
				" StationID=" + wellr.getCgoto() );
			StateDMI_Util.findAndAddSMWellRight ( SMWellRightList, SMWellRightMatchList, wellr,
				StateDMI_Util._IF_MATCH_APPEND );
			// TODO SAM 2007-05-24 Evaluate need.
			// The following seems to not be needed now that water rights are being handled more
			// explicitly
			//StateDMI_Util._IF_MATCH_MERGE );
		}
	}
}

/**
 * Add the list of StateMod_WellRight (from the simple approach) to the processor water right list.
 */
private void addStateModRightsToProcessorRightList ( List<StateMod_WellRight> smWellRightList,
	List<StateMod_WellRight> processorRightList,
	int onOffDefault,
	String permitIDPreFormat, int idFormat, String permitIDPostFormat,
	int warningLevel, int warningCount, String commandTag, CommandStatus status) {
	String routine = getClass().getSimpleName() + ".addStateModRightsToProcessorRightList";
	// TODO SAM 2016-06-12 Need to deal with formatting, etc.
	// Post-process the rights to set some additional information
	//boolean doPermitIDPreFormat = false;
	//if ( (permitIDPreFormat != null) && !permitIDPreFormat.isEmpty() ) {
		//doPermitIDPreFormat = true;
	//}
	boolean doPermitIDPostFormat = false;
	if ( (permitIDPostFormat != null) && !permitIDPostFormat.isEmpty() ) {
		doPermitIDPostFormat = true;
	}
	int count = 0;
	for ( StateMod_WellRight smWellRight : smWellRightList ) {
		++count;
		// Format the right ID
		StringBuilder idBuilder = new StringBuilder();
		// First pre-format the permit
		/*
		if ( !smWellRight.getXPermitReceipt().isEmpty() ) {
			if ( doPermitIDPreFormat ) {
				// Format the receipt
				idBuilder.append(String.format(permitIDPreFormat,smWellRight.getXPermitReceipt()));
			}
		}*/
		// Format the main part of the ID
		if ( idFormat == __HydroBaseID_int ) {
			// Just leave the ID as is
			idBuilder.append(smWellRight.getID());
		}
		else if ( idFormat == __StationIDNN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%s%02d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDWNN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%sW%02d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationID_NN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%s.%02d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDW_NN_int ) {
			// Format the well right identifier as the station ID + "W" + count
			idBuilder.append(String.format("%sW.%02d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDNNN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%s%03d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDWNNN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%sW%03d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationID_NNN_int ) {
			// Format the well right identifier as the station ID + count
			idBuilder.append(String.format("%s.%03d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDW_NNN_int ) {
			// Format the well right identifier as the station ID + "W" + count
			idBuilder.append(String.format("%sW.%03d",smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDAutoN_int ) {
			// Format the well right identifier as the station ID + count
			String format = "%s%0" + ((int)(Math.log10(smWellRightList.size())) + 1) + "d";
			idBuilder.append(String.format(format,smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDWAutoN_int ) {
			// Format the well right identifier as the station ID + count
			String format = "%sW%0" + ((int)(Math.log10(smWellRightList.size())) + 1) + "d";
			idBuilder.append(String.format(format,smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationID_AutoN_int ) {
			// Format the well right identifier as the station ID + count
			String format = "%s.%0" + ((int)(Math.log10(smWellRightList.size())) + 1) + "d";
			idBuilder.append(String.format(format,smWellRight.getCgoto(),count));
		}
		else if ( idFormat == __StationIDW_AutoN_int ) {
			// Format the well right identifier as the station ID + "W" + count
			String format = "%sW.%0" + ((int)(Math.log10(smWellRightList.size())) + 1) + "d";
			idBuilder.append(String.format(format,smWellRight.getCgoto(),count));
		}
		// Finally post-format the permit
		if ( doPermitIDPostFormat && !smWellRight.getXPermitReceipt().isEmpty() ) {
			// Replace with new formatted string
			String s = idBuilder.toString();
			idBuilder = new StringBuilder(String.format(permitIDPostFormat,s));
		}
		smWellRight.setID(idBuilder.toString());
		if ( smWellRight.getID().length() > 12 ) {
			String message = "Well right \"" + smWellRight.getID() + "\" is > 12 characters and will be " +
				"truncated on output - will cause problems later (e.g., in water right merging).";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Report problem to support - need to evaluate ID conventions." ) );
		}
		if ( onOffDefault == __AppropriationDate_int ) {
			// Convert the administration number to a year...
			String irtem = null;
			try {
				irtem = smWellRight.getIrtem();
				if ( !irtem.isEmpty() ) {
					double irtemDouble = Double.parseDouble(irtem);
					HydroBase_AdministrationNumber adminData = new HydroBase_AdministrationNumber ( irtemDouble );
					smWellRight.setSwitch ( adminData.getAppropriationDate().getYear() );
				}
			}
			catch ( Exception e ) {
				String message = "Error converting administration number " + irtem +
				" to date for right \"" + smWellRight.getID() + "\".  Setting on/off switch to 1.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Report problem to support - possible error in HydroBase" +
							" or may need to update software." ) );
				smWellRight.setSwitch( 1 );
			}
		}
		else {
			// Use the default value for the administration number...
			smWellRight.setSwitch ( onOffDefault );
		}
	}
	processorRightList.addAll(smWellRightList);
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
{	String routine = getClass().getSimpleName() + ".checkCommandParameters";
	String Approach = parameters.getValue ( "Approach" );
	//String ID = parameters.getValue ( "ID" );
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
	
	if ( (Approach != null) && !Approach.isEmpty() &&
		!Approach.equalsIgnoreCase(_Legacy) &&
		!Approach.equalsIgnoreCase(_Simple) ) {
		message = "The Approach value (" + Approach + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify Approach as " + _Legacy +
				" or " + _Simple + " (default).") );
	}
	
	/* OK to omit - default to *
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the identifier pattern to match." ) );
	}*/
	
	if ( (Year != null) && (Year.length() > 0) ) {
		List<String> v = StringUtil.breakStringList ( Year, ",", StringUtil.DELIM_SKIP_BLANKS );
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
			String token = v.get(i);
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
	List<String> validList = new ArrayList<String>(14);
	validList.add ( "Approach" );
    validList.add ( "ID" );
    validList.add ( "PermitIDPattern" );
    validList.add ( "PermitIDPreFormat" );
    validList.add ( "IDFormat" );
    validList.add ( "PermitIDPostFormat" );
    validList.add ( "Div" );
    validList.add ( "Year" );
    validList.add ( "DecreeMin" );
    validList.add ( "DefaultAppropriationDate" );
    validList.add ( "DefineRightHow" );
    validList.add ( "ReadWellRights" );
    validList.add ( "UseApex" );
    validList.add ( "OnOffDefault" );
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
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = "readWellRightsFromHydroBase_Command.readHydroBaseParcel";
	List <HydroBase_ParcelUseTS> hbparcel_Vector = null;
	String message;
	try {
		// Call the version that caches results
		hbparcel_Vector = hdmi.readParcelUseTSList ( parcel_year, Div_int, parcel_id );
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
@param parcelYear The calendar year for which the parcels apply.
@param String Div Division for data.
@param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
@param hbwellr_Vector A list of HydroBase_NetAmt to be appended to as rights are read for the parcel list.
@param cacheHydroBase if true, then on first read all the data for a division will be read
*/
private List<HydroBase_NetAmts> readHydroBaseWellRightsForDiversionWDIDList (
		HydroBaseDMI hdmi,
		StateMod_Well well,
		String id,
		String Loctype,
		StateMod_Well_CollectionType collectionType,
		List<String> wdids,
		int parcelYear, String yearString,
		int div,
		DefineWellRightHowType defineWellRightHow,
		boolean readWellRights,
		boolean useApex,
		double defaultAdminNumber,
		Date defaultApproDate,
		String permitIdPreFormat,
		int warningLevel, int warningCount, String commandTag, CommandStatus status, boolean cacheHydroBase )
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForDiversionWDIDList";
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
	String partId;	// single WDID
	int [] wdid_parts = new int[2];
	HydroBase_StructureView hbdiv = null;	// Individual ditch
	List<HydroBase_ParcelUseTSStructureToParcel> hbparcel_structure_Vector = null;//Structure/parcel join data
	List<HydroBase_NetAmts> hbwellrList = new Vector<HydroBase_NetAmts>();	// List of well rights for all parcels related to location
	for ( int iparts = 0; iparts < nwdids; iparts++ ) {
		partId = wdids.get(iparts);
		Message.printStatus ( 2, routine, yearString +
			"Processing well \"" + id + "\" ditch part \"" + partId + "\" (" + collectionType + ")." );
		try {
			// Parse out the WDID for the ditch...
			HydroBase_WaterDistrict.parseWDID(partId,wdid_parts);
		}
		catch ( Exception e ) {
			if ( (nwdids == 1) && !HydroBase_WaterDistrict.isWDID(partId)) {
				// The diversion is a single diversion that is not a WDID.
				// Therefore a warning is not needed...
				Message.printStatus ( 2,
					routine, yearString + "Well single ditch part \"" + id + "\" is not a WDID.  " +
						"Cannot read corresponding diversion structures from HydroBase." );
			}
			else {
				// Not a WDID - this is an error because valid structures are
				// expected as parts of an aggregate...
				message = yearString + "Well \"" + id + "\" (ditch part " + partId +
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
		if ( wdDiv != div ) {
			message = "Location " + id + " (part " + partId + ", " + collectionType +
				") has WDID in division " + wdDiv +
				", which is different from the requested division " + div +
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
				partId + ", " + collectionType + ") (" + e + ")";
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
				id + "\" part=\"" + partId + "\", " + collectionType + ".  Skipping.";
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
				hbdiv.getStructure_num(), parcelYear );
		}
		catch ( Exception e ) {
			message = yearString + "Unexpected error getting structure to parcel data from HydroBase for well \"" +
				id + "\" (ditch part " + partId + ") year=" + parcelYear + " (" + e + ").";
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
		
		Message.printStatus ( 2, routine, yearString + "Well \"" + id + "\" (ditch part " + partId +
			") year=" + parcelYear + " Div=" + div + " irrigates " + nparcel + " parcels" );

		// Put together a list of parcel identifiers...
		
		List<String> parcelIds = new Vector<String>(nparcel);
		double [] fractionIrrig = new double[nparcel];
		
		HydroBase_ParcelUseTSStructureToParcel hbparcel_structure;
		for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
			hbparcel_structure = hbparcel_structure_Vector.get(iparcel);
			parcelIds.add ( "" + hbparcel_structure.getParcel_id() );
			fractionIrrig[iparcel] = hbparcel_structure.getPercent_irrig();
		}
		// Loop through each parcel and append the HydroBase rights associated with each parcel
		List<HydroBase_NetAmts> hbwellr2List = readHydroBaseWellRightsForParcelList (
				hdmi,
				well,
				id,
				Loctype,
				parcelIds,
				fractionIrrig,	// fractionIrrig for each parcel in list
				partId,		// Ditch part that is being processed
				parcelYear, yearString,
				div,
				defineWellRightHow,
				readWellRights,
				useApex,
				defaultAdminNumber,
				defaultApproDate,
				permitIdPreFormat,
				warningLevel, warningCount, commandTag, status );
		int nwellr2 = 0;
		if ( hbwellr2List != null ) {
			nwellr2 = hbwellr2List.size();
			for ( int iwellr2 = 0; iwellr2 < nwellr2; iwellr2++ ) {
				hbwellrList.add ( hbwellr2List.get(iwellr2) );
			}
		}
	}
	return hbwellrList;
}

/**
Read well rights from HydroBase for a D&W station (explicit WDID, or collection of WDID).
 * @param hbdmi
 * @param wellStationId
 * @param is_collection
 * @param div
 * @param defineWellRightHow
 * @param readWellRights
 * @param useApex
 * @param defaultAdminNumber
 * @param defaultApproDate
 * @param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
 * @param warningLevel
 * @param warningCount
 * @param commandTag
 * @param status
 */
private List<StateMod_WellRight> readHydroBaseWellRightsForDWStationsSimple (
	HydroBaseDMI hbdmi,
	String wellStationId,
	boolean isCollection,
	StateMod_Well_CollectionType collectionType,
	StateMod_Well_CollectionPartType collectionPartType,
	List<String> partIdList,
	int div,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
	throws Exception
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForDWStationSimple";
	String message;
	List<StateMod_WellRight> smWellRightCombinedList = new ArrayList<StateMod_WellRight>(); // Rights that are read and returned
	String partId;
	for ( int iPart = 0; iPart < partIdList.size(); iPart++ ) {
		partId = partIdList.get(iPart);
		int wdidParts[] = null;
		try {
			wdidParts = HydroBase_WaterDistrict.parseWDID(partId);
		}
		catch ( Exception e ) {
			message = "Well station \"" +
				wellStationId + "\" part=\"" + partId + "\" is expected to be a WDID, but is not (" + e + ") - skipping.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the " + collectionType + " part identifiers are correct." ) );
			continue;
		}
		// Get the list of well/parcel/structure records for the parcels
		List<HydroBase_Wells> hbDivWellParcelList = hbdmi.readWellsWellToParcelWellToStructureList(
			-1, wdidParts[0], wdidParts[1], -1, -1);
		Message.printStatus(2,routine,"Well station \"" + wellStationId + "\" (diversion part " +
			partId + ") has " + hbDivWellParcelList.size() + " well/parcel/structure records (all years)" );
		// Process all the records to find unique receipt/wdid combinations - these are all synchronized
		List<String> uniqueReceiptList = new ArrayList<String>();
		List<String> uniqueWDIDList = new ArrayList<String>();
		List<HydroBase_Wells> hbDivWellParcelUniqueList = new ArrayList<HydroBase_Wells>();
		String receipt = null;
		String wdid = null;
		boolean foundMatch;
		for ( HydroBase_Wells hbDivWellParcel : hbDivWellParcelList ) {
			receipt = hbDivWellParcel.getReceipt();
			// TODO SAM 2016-06-12 Need to figure out using a WDID field directly - is it in stored procedure?
			wdid = "";
			if ( !DMIUtil.isMissing(hbDivWellParcel.getWD()) && (hbDivWellParcel.getWD() > 0) &&
				!DMIUtil.isMissing(hbDivWellParcel.getID()) && (hbDivWellParcel.getID() > 0) ) {
				wdid = HydroBase_WaterDistrict.formWDID(hbDivWellParcel.getWD(),hbDivWellParcel.getID());
			}
			foundMatch = false;
			for ( int i = 0; i < hbDivWellParcelUniqueList.size(); i++ ) {
				if ( receipt.equals(uniqueReceiptList.get(i)) && wdid.equals(uniqueWDIDList.get(i)) ) {
					foundMatch = true;
					break;
				}
			}
			if ( !foundMatch ) {
				// The well/parcel/structure list has a new combination so add to all the lists
				// - all lists have the same length
				// - uniqueReceiptList contains only receipts and empty strings
				// - uniqueWDIDList contains only WDIDs and empty strings
				// - hbDivWellParcelUniqueList is the original data but and may have WDID OR receipt OR WDID/receipt
				// This ensures that the supply don't get added more than once
				uniqueReceiptList.add(receipt);
				uniqueWDIDList.add(wdid);
				hbDivWellParcelUniqueList.add(hbDivWellParcel);
			}
		}
		// Now should have the list of permit/right to process
		Message.printStatus(2,routine,"Well station \"" + wellStationId + "\" (diversion part " +
			partId + ") has " + hbDivWellParcelUniqueList.size() + " well/parcel/structure records (all years) with unique WDID/receipt combinations:" );
		Message.printStatus(2,routine,"  Listing all rights immediately below and then will loop through each to process after the listing.  "
			+ "Can have WDID right (not permit), permit (no WDID), or matched WDID right and permit.");
		for ( int i = 0; i < hbDivWellParcelUniqueList.size(); i++ ) {
			Message.printStatus(2,routine,"  WDID = " + uniqueWDIDList.get(i) + ", receipt = \"" + uniqueReceiptList.get(i) +
				"\" yield (GPM) = " + String.format("%.2f",hbDivWellParcelUniqueList.get(i).getYield()) +
				", yield (CFS) = " + String.format("%.2f",hbDivWellParcelUniqueList.get(i).getYield()*.002228));
		}
		Message.printStatus(2,routine,"  Now attempt to process all of the rights listed above..."); 
		// Process the unique combinations of receipt/WDID
		for ( int i = 0; i < hbDivWellParcelUniqueList.size(); i++ ) {
			// Treat as if well WDID takes priority over receipt
			// This is the same approach as if reading an explicit well - it is a performance hit but at least code is shared
			// Read the rights for the specific well station by treating as a single D&W
			List<String> partIdList2 = new ArrayList<String>(1);
			List<StateMod_Well_CollectionPartIdType> partIdTypeList2 = new ArrayList<StateMod_Well_CollectionPartIdType>(1);
			List<StateMod_WellRight> smWellRightList = new ArrayList<StateMod_WellRight>();
			if ( !uniqueWDIDList.get(i).isEmpty() ) {
				// Have a WDID so try to get NetAmts using it - if returns nothing OK because well may have been abandoned.
				partIdList2.add(uniqueWDIDList.get(i));
				partIdTypeList2.add(StateMod_Well_CollectionPartIdType.WDID);
				smWellRightList = readHydroBaseWellRightsForWellStationsSimple (
					hbdmi,
					wellStationId,
					isCollection, // for the main well station
					collectionType,
					collectionPartType,
					partIdList2, // will have a single part, which is the well WDID
					partIdTypeList2, // will have single part, containing "WDID"
					-1, // division as integer, not used
					readWellRights, // TODO SAM 2016-06-11 need to figure out if used
					useApex, // TODO SAM 2016-06-11 need to figure out if used
					defaultAdminNumber, // TODO SAM 2016-06-11 need to figure out if used
					defaultApproDate, // TODO SAM 2016-06-11 need to figure out if used
					permitIdPreFormat, // used to format permit identifiers
					warningLevel, warningCount, commandTag, status ); // used for logging and error handling
			}
			else if ( !uniqueReceiptList.get(i).isEmpty() ) {
				// Well did not have a WDID but does have receipt
				// Try to read using the receipt - performance hit to reread but allows using same logic
				partIdList2 = new ArrayList<String>(1);
				partIdList2.add(uniqueReceiptList.get(i));
				partIdTypeList2 = new ArrayList<StateMod_Well_CollectionPartIdType>(1);
				partIdTypeList2.add(StateMod_Well_CollectionPartIdType.RECEIPT);
				smWellRightList = readHydroBaseWellRightsForWellStationsSimple (
					hbdmi,
					wellStationId,
					isCollection,
					collectionType,
					collectionPartType,
					partIdList2, // will have a single part, which is the well receipt
					partIdTypeList2, // will have single part, containing "Receipt"
					-1, // division as integer, not used
					readWellRights, // TODO SAM 2016-06-11 need to figure out if used
					useApex, // TODO SAM 2016-06-11 need to figure out if used
					defaultAdminNumber, // TODO SAM 2016-06-11 need to figure out if used
					defaultApproDate, // TODO SAM 2016-06-11 need to figure out if used
					permitIdPreFormat, // used to format permit identifiers
					warningLevel, warningCount, commandTag, status ); // used for logging and error handling
			}
			if ( smWellRightList.size() == 0 ) {
				// There was no matching receipt
				message = "  Well station \"" + wellStationId + "\" (diversion part " +
					partId + ") associated WDID " + uniqueWDIDList.get(i) + ", well receipt \"" + uniqueReceiptList.get(i)
					+ "\" resulted in no StateMod water rights";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify well/parcel associations since no WDID or receipt water right resulted.  Well may be abandoned?" ) );
			}
			else {
				// Add the water right from the receipt to the list
				smWellRightCombinedList.addAll(smWellRightList);
			}
		}
	}
	// Return the collective list of rights as HydroBase_NetAmt instances
	return smWellRightCombinedList;
}

/**
Read HydroBase well rights as HydroBase_NetAmts (sec=parcel, =class), for a parcel.
@param hdmi HydroBaseDMI instance to use for queries.
@param locType Location type being processed, for messages.
@param id Model location ID related to parcels.
@param parcelId ID for a single parcel for which to read wells.
@param fractionIrrig The fraction of the parcel irrigated by the ditch, or 1.0 if groundwater only supply.
@param partId WDID in a D&W aggregation.
@param iparcel Parcel count being processed (0 index), used when processing D&W (part_id != null).
@param nparcel Total number of parcels to process, used when processing D&W (part_id != null).
@param parcelYear Year for which to read data.
@param div Water division for parcel data.
@param hbwellrList The list of HydroBase_NetAmt containing wells associated with the parcel.
@param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
*/
private void readHydroBaseWellRightsForParcel (
		HydroBaseDMI hdmi,
		String locType,
		String id,
		int parcelId,
		double fractionIrrig,
		String partId,
		int iparcel,
		int nparcel,
		int parcelYear, String yearString,
		int div,
		DefineWellRightHowType defineWellRightHow,
		boolean readWellRights,
		boolean useApex,
		double defaultAdminNumber,
		Date defaultApproDate,
		String permitIdPreFormat,
		List<HydroBase_NetAmts> hbwellrList,
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForParcel";
	// First read the Wells - WellToParcel join to get the "holes in the ground"
	String message;

	List<HydroBase_Wells> hbwellParcelList = null;
	try {
		// Get the well/welltoparcels associated with the parcel...
		hbwellParcelList = hdmi.readWellsWellToParcelList( parcelId, parcelYear, div );
	}
	catch ( Exception e ) {
		if ( partId == null ) {
			// Processing groundwater only parcel...
			message = yearString + "Error getting well to parcel data from HydroBase for well " + id +
				" parcel=" +parcelId + " division=" + div + "(" + e + ").";
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
			" (part " + partId + ") parcel=" + parcelId + " Div=" + div + "(" + e + ").";
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
	int nwellParcel = 0;
	if ( hbwellParcelList != null ) {
		nwellParcel = hbwellParcelList.size();
	}
	if ( partId == null ) {
		// Groundwater only parcel
		Message.printStatus ( 2, routine, yearString + locType +	" \"" +id + "\" Div="
			+ div + " parcel " + parcelId + " has "+ nwellParcel + " well(s)." );
	}
	else {
		// WDID part of a D&W location
		if ( id.equals(partId) ) {
			// Explicit structure
			Message.printStatus ( 2, routine, yearString + "Diversion \"" +id + "\" (explicit, parcel " +
				(iparcel + 1) + " of " + nparcel+") " +
				" Div=" + div + " parcel " + parcelId +
				" has "+ nwellParcel + " well(s)." );
		}
		else {
			// D&W collection...
			Message.printStatus ( 2, routine, yearString + "Diversion \"" +id + "\" (part " +
			partId + ", parcel " + (iparcel + 1) + " of " + nparcel+") " +
			" Div=" + div + " parcel " + parcelId + " has "+ nwellParcel + " well(s)." );
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
	HydroBase_Wells hbwellParcel = null; // Used in iterator
	List<HydroBase_NetAmts> hbwellr2List = new ArrayList<HydroBase_NetAmts>(); // To return individual rights.
	HydroBase_NetAmts hbwellr = null; // To transfer rights in Vectors
	for ( int iwellParcel = 0; iwellParcel < nwellParcel; iwellParcel++ ) {
		// Process the single wells/well to parcel record, either using its
		// data directly or querying net amount rights.
		try {
			hbwellParcel = hbwellParcelList.get(iwellParcel);
			warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellParcelsHelper (
				hdmi,
				commandTag,	warningLevel, warningCount, status,
				routine,
				parcelYear,
				id,
				hbwellParcel,
				parcelId,
				fractionIrrig,	// Use for parcel/ditch overlap, 1.0 if GW only
				defineWellRightHow,
				readWellRights,
				useApex,
				defaultAdminNumber,
				defaultApproDate,
				permitIdPreFormat,
				hbwellr2List );
		}
		catch ( Exception e ) {
			message = yearString + "Error getting well rights data from HydroBase for " + id +
				" parcel=" +parcelId + " division=" + div + "(" + e + ")";
			Message.printWarning ( 3, routine, e );
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
			return;
		}
		// Transfer the resulting HydroBase rights into the big list...
		int irsize = hbwellr2List.size();
		for ( int ir = 0; ir < irsize; ir++ ) {
			hbwellr = hbwellr2List.get(ir);
			hbwellrList.add ( hbwellr );
			/* TODO SAM 2007-05-23 Evaluate how to add parcel/supply
			if ( hbwellr.getNet_rate_abs() >= DecreeMin_double ) {
				// Increment the count of wells for the parcel, to be used
				// in data checks...
				sm_parcel.setWellCount( sm_parcel.getWellCount()	+ 1 );
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
@param locType Location type for messages.
@param locId Location identifier.
@param parcelIds A list of parcel identifiers.
@param fractionIrrig the fraction of the parcel irrigated by a ditch
for the year.  This is only used when the calling code is processing
locations with surface and groundwater supply.  Specify null or an array
of 1.0 when processing groundwater only.
@param parcelYear The calendar year for which the parcels apply.
@param String Div Division for data.
@param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
@param hbwellr_Vector A list of HydroBase_NetAmt to be appended to as rights
are read for the parcel list.
*/
private List<HydroBase_NetAmts> readHydroBaseWellRightsForParcelList (
		HydroBaseDMI hdmi,
		StateMod_Well well,
		String locId,
		String locType,
		List<String> parcelIds,
		double [] fractionIrrig,
		String partId,
		int parcelYear, String yearString,
		int div,
		DefineWellRightHowType defineWellRightHow,
		boolean readWellRights,
		boolean useApex,
		double defaultAdminNumber,
		Date defaultApproDate,
		String permitIdPreFormat,
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = "readWellRightsFromHydroBase_Command.readHydroBaseWellRightsForParcelList";
	String message;
	// Loop through each parcel and append the HydroBase rights associated with each parcel
	
	int nparcel = 0;
	if ( parcelIds != null ) {
		nparcel = parcelIds.size();
	}
	Message.printStatus ( 2, routine, locType + " \"" + locId +
		yearString + " division=" + div + " irrigates " + nparcel + " parcels" );
	
	// Get the wells associated with the parcels.
	
	int parcelId;	// Specific parcel identifier to process
	HydroBase_ParcelUseTS hbwellParcel = null;
	List<HydroBase_NetAmts> hbwellrList = new ArrayList<HydroBase_NetAmts>();	// Rights for location
	double fractionIrrig2; // Fraction of parcel that is irrigated by ditch
	for ( int iparcel = 0; iparcel < nparcel; iparcel++ ) {
		parcelId = Integer.parseInt(parcelIds.get(iparcel) );
		// Get the HydroBase parcel data...
		hbwellParcel = readHydroBaseParcel ( hdmi, well, locId, parcelId,
			parcelYear, div, warningLevel, warningCount, commandTag, status );
		if ( hbwellParcel == null ) {
			// Should not happen but seems to be.
			message = yearString + "Unable to find parcel data in HydroBase for location \"" +
				well.getID() + " parcel_id=" + parcelId + ".  Skipping - check input parcel lists.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Confirm that model and HydroBase data are consistent." ) );
			continue;
		}
		// Now read the wells for the parcel (they will be added to the list)...
		if ( fractionIrrig == null ) {
			fractionIrrig2 = 1.0;
		}
		else {
			fractionIrrig2 = fractionIrrig[iparcel];
		}
		readHydroBaseWellRightsForParcel ( hdmi, locType, locId,
				hbwellParcel.getParcel_id(), 
				fractionIrrig2,
				partId,
				iparcel,
				nparcel,
				parcelYear, yearString, div,
				defineWellRightHow,
				readWellRights,
				useApex,
				defaultAdminNumber,
				defaultApproDate,
				permitIdPreFormat,
				hbwellrList,
				warningLevel, warningCount, commandTag, status );
	}
	return hbwellrList;
}

/**
Read well rights from HydroBase for a well station (explicit well, or in the future a well aggregated
by well IDs).
 * @param hdmi
 * @param well
 * @param id
 * @param is_collection
 * @param div
 * @param defineWellRightHow
 * @param readWellRights
 * @param useApex
 * @param defaultAdminNumber
 * @param defaultApproDate
 * @param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
 * @param warningLevel
 * @param warningCount
 * @param commandTag
 * @param status
 */
private List<HydroBase_NetAmts> readHydroBaseWellRightsForWellStation (
	HydroBaseDMI hdmi,
	String id,
	boolean isCollection,
	List<String> idParts,
	int div,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForWellStation";
	String message;
	List<HydroBase_NetAmts> hbwellr2List = new ArrayList<HydroBase_NetAmts>(); // Rights that are read and returned
	// Single well - get its water rights...
	HydroBase_Wells hbwellParcel = null;
	try {
		warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellParcelsHelper (
			hdmi,
			commandTag, warningLevel, warningCount, status,
			routine,
			-999, // No year since not dealing with parcels
			id,
			hbwellParcel,
			-999, // No parcel ID since not dealing with parcels
			1.0, // Assume that 100% of well right is to be used
			DefineWellRightHowType.RIGHT_IF_AVAILABLE, // Always use right if available
			true, // ReadWellRights_boolean, (always read because reading explicit well data, not from well/parcels)
			useApex,
			defaultAdminNumber,
			defaultApproDate,
			permitIdPreFormat,
			hbwellr2List );
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
	return hbwellr2List;
}

/**
Read well rights from HydroBase for a well station (explicit well, or a well aggregated by well IDs).
@param hdmi HydroBaseDMI database connection
@param locId location ID for well station
@param isCollection is the location a collection?
@param collectionType collection type for logging
@param parcelYears years to read parcels, used when parcels are involved
@param partIds list of part IDs for location
@param partIdTypes list of part ID types for location ("WDID" and "Receipt")
@param defineWellRightHow indicates how to define the well right (see documentation)
@param readWellRights should individual well rights be read or sum for location?
@param useApex should APEX rights be added to rights
@param defaultAdminNumber default admin number
@param defaultApproDate default appropriation date
@param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
@param warningLevel warning level for warning log messages
@param warningCount count of warnings, for logging
@param commandTag command tag number, for logging
@param status CommandStatus object for tracking warnings
*/
private List<HydroBase_NetAmts> readHydroBaseWellRightsForWellStationList (
	HydroBaseDMI hdmi,
	String locId,
	boolean isCollection,
	StateMod_Well_CollectionType collectionType,
	int [] parcelYears,
	List<String> partIds,
	List<StateMod_Well_CollectionPartIdType> partIdTypes,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForWellStationList";
	String message;
	// List of well rights to return for all well stations
	List<HydroBase_NetAmts> hbwellr3List = new ArrayList<HydroBase_NetAmts>(); // Rights that are read for the list of wells
	// Single well - get its water rights...
	HydroBase_Wells hbwellParcel = null;
	// Loop through the parts
	int partsSize = partIds.size();
	StopWatch swReceipt = new StopWatch();
	for ( int i = 0; i < partsSize; i++ ) {
		String idPart = partIds.get(i);
		StateMod_Well_CollectionPartIdType idPartType = null;
		if ( partIdTypes.size() == partIds.size() ) {
			idPartType = partIdTypes.get(i);
		}
		// There are two ways that well parts are defined:
		// 1) New way, where the part ID is either a WDID or permit receipt and partIdTypes indicates which of these it is
		// 2) Old way, where the part ID includes ":p" at the end to indicate a permit, or otherwise is assumed to be WDID.
		// The new way is more robust and easier to check so check that first and then default to the legacy code
		if ( idPartType != null ) {
			Message.printStatus(2,routine,"Reading well rights for " + collectionType + " well " + idPartType + " \"" + idPart + "\"" );
			if ( idPartType == StateMod_Well_CollectionPartIdType.WDID ) {
				// Read like an explicit well - do the same as legacy code did
				try {
					List<HydroBase_NetAmts> hbwellr2List = new ArrayList<HydroBase_NetAmts>(); // Rights that are read for this well
					warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellParcelsHelper (
						hdmi,
						commandTag, warningLevel, warningCount, status,
						routine,
						-999, // No year since not dealing with parcels
						idPart,
						hbwellParcel,
						-999, // No parcel ID since not dealing with parcels
						1.0, // Assume that 100% of well right is to be used
						DefineWellRightHowType.RIGHT_IF_AVAILABLE, // DefineRightHow_int, (always use right if available)
						true, // ReadWellRights_boolean, (always read)
						useApex,
						defaultAdminNumber,
						defaultApproDate,
						permitIdPreFormat,
						hbwellr2List );
					// Add the well rights to the main list
					hbwellr3List.addAll(hbwellr2List);
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine, e );
					message = "Unexpected error reading well rights for well station \"" + locId + "\" (" + e + ").";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "See the log file - report the problem to software support." ) );
				}
			}
			else if ( idPartType == StateMod_Well_CollectionPartIdType.RECEIPT ) {
				try {
					// Read the WellsWellToParcel join for the receipt
					// Do this for each year to be read
					for ( int iParcelYear = 0; iParcelYear < parcelYears.length; ++iParcelYear ) {
						swReceipt.clear();
						swReceipt.start();
						List<HydroBase_Wells> hbWellParcelList = hdmi.readWellsWellToParcelList( -1, parcelYears[iParcelYear], -1, idPart, -1, -1 );
						swReceipt.stop();
						Message.printStatus ( 2, routine, "" + parcelYears[iParcelYear] + " well receipt \""
							+ idPart + "\" have " + hbWellParcelList.size() + " rows - took " + swReceipt.getMilliseconds() + " ms");
						for ( HydroBase_Wells hbwellParcel1 : hbWellParcelList ) {
							// Translate the records into well rights - because receipt has been requested, always use receipt information
							List<HydroBase_NetAmts> hbwellr2List = new ArrayList<HydroBase_NetAmts>(); // Rights that are read for this well
							warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellParcelsHelper (
								hdmi,
								commandTag, warningLevel, warningCount, status,
								routine,
								parcelYears[iParcelYear], // Relevant
								idPart,
								hbwellParcel1,
								hbwellParcel1.getParcel_id(), // Parcel ID is relevant
								1.0, // Assume that 100% of well right is to be used
								DefineWellRightHowType.RIGHT_IF_AVAILABLE, // DefineRightHow_int, (always use right if available)
								true, // ReadWellRights_boolean, (always read)
								useApex,
								defaultAdminNumber,
								defaultApproDate,
								permitIdPreFormat,
								hbwellr2List );
							// Add the well rights to the main list
							hbwellr3List.addAll(hbwellr2List);
						}
					}
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine, e );
					message = "Unexpected error reading well permit (receipt) for well station \"" + locId + "\" (" + e + ").";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "See the log file - report the problem to software support." ) );
				}
			}
			else {
				// Don't know how to handle
				message = "Well id \"" + locId + "\" part \"" + idPart + "\" ID type is not "
						+ StateMod_Well_CollectionPartIdType.WDID + " or "
						+ StateMod_Well_CollectionPartIdType.RECEIPT + " - don't know how to read well rights";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report the problem to software support - feature needs to be implemented." ) );
			}
		}
		else {
			// Legacy code that did not actually supporting well rights from permits
			// TODO SAM 2016-05-17 Evaluate with WWG whether this should be disabled or made more robust
			// - need to know for sure the convention for identifiers
			if ( !HydroBase_WaterDistrict.isWDID(idPart)) {
				message = "Well id \"" + locId + "\" part \"" + idPart + "\" is not a WDID and features have not been " +
					"implemented to read well permits that are part of collections.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report the problem to software support - feature needs to be implemented." ) );
			}
			try {
				List<HydroBase_NetAmts> hbwellr2List = new ArrayList<HydroBase_NetAmts>(); // Rights that are read for this well
				warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellParcelsHelper (
					hdmi,
					commandTag, warningLevel, warningCount, status,
					routine,
					-999, // No year since not dealing with parcels
					idPart,
					hbwellParcel,
					-999, // No parcel ID since not dealing with parcels
					1.0, // Assume that 100% of well right is to be used
					DefineWellRightHowType.RIGHT_IF_AVAILABLE, // DefineRightHow_int, (always use right if available)
					true, // ReadWellRights_boolean, (always read)
					useApex,
					defaultAdminNumber,
					defaultApproDate,
					permitIdPreFormat,
					hbwellr2List );
				// Add the well rights to the main list
				hbwellr3List.addAll(hbwellr2List);
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, e );
				message = "Unexpected error reading well rights for well station \"" + locId + "\" (" + e + ").";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "See the log file - report the problem to software support." ) );
			}
		}
	}
	return hbwellr3List;
}

/**
Read well rights from HydroBase for a well station (explicit well, or collection).
Each part ID can be a WDID or receipt.
 * @param hdmi
 * @param well
 * @param wellStationId
 * @param is_collection
 * @param div
 * @param defineWellRightHow
 * @param readWellRights
 * @param useApex
 * @param defaultAdminNumber
 * @param defaultApproDate
 * @param permitIdPreFormat the String.format() specifier to apply to permit receipt after reading,
for example "%s:P" to mimic legacy behavior.
 * @param warningLevel
 * @param warningCount
 * @param commandTag
 * @param status
 */
private List<StateMod_WellRight> readHydroBaseWellRightsForWellStationsSimple (
	HydroBaseDMI hdmi,
	String wellStationId,
	boolean isCollection,
	StateMod_Well_CollectionType collectionType,
	StateMod_Well_CollectionPartType collectionPartType,
	List<String> partIdList,
	List<StateMod_Well_CollectionPartIdType> partIdTypeList,
	int div,
	boolean readWellRights,
	boolean useApex,
	double defaultAdminNumber,
	Date defaultApproDate,
	String permitIdPreFormat,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = getClass().getSimpleName() + ".readHydroBaseWellRightsForWellStationSimple";
	String message;
	List<StateMod_WellRight> smWellRightCombinedList = new ArrayList<StateMod_WellRight>(); // Rights that are read and returned
	String partId;
	StateMod_Well_CollectionPartIdType partIdType;
	for ( int iPart = 0; iPart < partIdList.size(); iPart++ ) {
		partId = partIdList.get(iPart);
		partIdType = partIdTypeList.get(iPart);
		// Get water rights for the single well...
		List<StateMod_WellRight> smWellRightList = new ArrayList<StateMod_WellRight>(); // Rights that are read and returned
		try {
			warningCount = StateDMI_Util.readWellRightsFromHydroBaseWellsHelper (
				hdmi,
				commandTag, warningLevel, warningCount, status,
				routine,
				wellStationId, // The station that is being processed, StateMod station ID, for logging
				collectionType,
				collectionPartType,
				partId, // The well that is being processed (well WDID or receipt)
				partIdType, // The well type that is being processed "WDID" or "Receipt"
				DefineWellRightHowType.RIGHT_IF_AVAILABLE, // Always use right if available
				useApex, // Read 
				defaultAdminNumber,
				defaultApproDate,
				permitIdPreFormat,
				smWellRightList );
			// Add the list of rights to the combined list
			smWellRightCombinedList.addAll(smWellRightList);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			message = "Unexpected error reading well rights for well station \"" + wellStationId + "\" part ID = \""
				+ partId + "\" part type = \"" + collectionPartType + "\" (" + e + ").";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file - report the problem to software support." ) );
		}
	}
	// Return the collective list of rights as HydroBase_NetAmt instances
	return smWellRightCombinedList;
}

/**
Method to execute the readWellRightsFromHydroBase() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	String commandTag = "" + command_number;
	int warningCount = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	boolean doSimpleApproach = true; // For now Simple is true and other (Legacy) is false
	String Simple = parameters.getValue ( "Simple" );
	if ( (Simple != null) && !Simple.isEmpty() && !Simple.equalsIgnoreCase(_Simple) ) {
		doSimpleApproach = false;
	}
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idPatternJava = StringUtil.replaceString(ID,"*",".*");
	//String PermitIDPattern = parameters.getValue ( "PermitIDPattern" );
	//String permitIdPattern = null;
	//if ( PermitIDPattern == null ) {
		//PermitIDPattern = ""; // Default - don't use parameter
		//permitIdPattern = StringUtil.replaceString(PermitIDPattern,"*",".*"); // Java regex
	//}
	String PermitIDPreFormat = parameters.getValue ( "PermitIDPreFormat" );
	if ( (PermitIDPreFormat == null) || PermitIDPreFormat.isEmpty() ) {
		PermitIDPreFormat = "%s:P";
	}
	String IDFormat = parameters.getValue ( "IDFormat" );
	if ( IDFormat == null ) {
		IDFormat = _StationIDW_NN;
	}
	int IDFormat_int = __HydroBaseID_int;
	if ( IDFormat.equalsIgnoreCase(_StationIDW_NN) ) {
		IDFormat_int = __StationIDW_NN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDWNN) ) {
		IDFormat_int = __StationIDWNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationID_NN) ) {
		IDFormat_int = __StationID_NN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDNN) ) {
		IDFormat_int = __StationIDNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDW_NNN) ) {
		IDFormat_int = __StationIDW_NNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDWNNN) ) {
		IDFormat_int = __StationIDWNNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationID_NNN) ) {
		IDFormat_int = __StationID_NNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDNNN) ) {
		IDFormat_int = __StationIDNNN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDW_AutoN) ) {
		IDFormat_int = __StationIDW_AutoN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDWAutoN) ) {
		IDFormat_int = __StationIDWAutoN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationID_AutoN) ) {
		IDFormat_int = __StationID_AutoN_int;
	}
	else if ( IDFormat.equalsIgnoreCase(_StationIDAutoN) ) {
		IDFormat_int = __StationIDAutoN_int;
	}
	String PermitIDPostFormat = parameters.getValue ( "PermitIDPostFormat" );
	if ( (PermitIDPostFormat == null) || PermitIDPostFormat.isEmpty() ) {
		PermitIDPostFormat = "%s";
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
	
	String locType = "Well station";	// Used with messages
	
	// List that tracks when identifiers when adding data cause a
	// conflict with previous data set.
	// TODO SAM 2007-05-23 Old code did not track conflicts between
	// this command and others that set well rights.
	List<String> SMWellRight_match_Vector = processor.getStateModWellRightMatchList();

	// Get the list of well stations...
	
	List<StateMod_Well> stationList = null;
	int stationListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List");
		stationList = dataList;
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting well station data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of well rights (probably empty)...
	
	List<StateMod_WellRight> processorRightList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List");
		processorRightList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting well right data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
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
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
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
	int [] parcelYears = null;
	if ( (__Year_int == null) || (__Year_int.length == 0)  ) {
		try {
			parcelYears = StateDMI_Util.readParcelYearListFromHydroBase ( hbdmi, Div_int );
		}
		catch ( Exception e ) {
			parcelYears = null;
		}
		if ( parcelYears == null ) {
			message = "Cannot determine years of parcel data from HydroBase.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
	else {
		// Use the years specified by the user...
		parcelYears = new int[__Year_int.length];
		System.arraycopy( __Year_int, 0, parcelYears, 0, __Year_int.length );
	}
	for ( int iparcel_year = 0; iparcel_year < parcelYears.length;	iparcel_year++ ) {
		Message.printStatus( 2, routine, "Will include division " + Div_int +
			" parcel data from " + parcelYears[iparcel_year] + " (if parcel data are needed in processing)");
	}
	if ( (parcelYears == null) || (parcelYears.length == 0) ) {
		message = "No parcel years have been specified or are available from HydroBase - " +
			" data retrieval will be limited to explicit/aggregate wells.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "May be OK if only processing explicit wells (not full dataset?)." ) );
		// Define one parcel year to make the following loop work
		parcelYears = new int[1];
		parcelYears[0] = -999;
	}
	
	// Set up the default administration number
	
	double defaultAdminNumber = 99999.99999; // Default
	Date defaultApproDate = null; // Matching default
	try {
		HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber ( defaultAdminNumber );
		DateTime dt = an.getAppropriationDate();
		// This goes to day precision so time zone is irrelevant
		defaultApproDate = dt.getDate(TimeZoneDefaultType.LOCAL);
	}
	catch ( Exception e ) {
		message = "Error converting default administration number " +
		"99999.99999 to a appropriation number (should not happen).";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Initialize the default appropriation date
	
	if ( DefaultAppropriationDate != null ) {
		try {
			DateTime dt = DateTime.parse(DefaultAppropriationDate);
				HydroBase_AdministrationNumber an = new HydroBase_AdministrationNumber ( dt );
			defaultAdminNumber = an.getAdminNumber();
			defaultApproDate = dt.getDate (TimeZoneDefaultType.LOCAL);
			Message.printStatus ( 2, routine, "If well right/permit does not have a date, then " +
			DefaultAppropriationDate + " (" +
			StringUtil.formatString(defaultAdminNumber,"%.5f")+ ") will be used as the default." );
		}
		catch ( Exception e ) {
			message = "Error converting date default appropriation date \"" + DefaultAppropriationDate +
			"\" to an administration number.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Should not normally be an issue - report problem to software support." ) );
		}
	}
	
	// Print to log...
	
	if ( defineWellRightHow == DefineWellRightHowType.EARLIEST_DATE ) {
		Message.printStatus ( 2, routine,
			"Rights will be defined using earliest of water right appropriation date and permit date (used with Approach=Legacy but not Approach=Simple)." );
	}
	else if ( defineWellRightHow == DefineWellRightHowType.LATEST_DATE ) {
		Message.printStatus ( 2, routine,
			"Rights will be defined using latest of water right appropriation date and permit date (used with Approach=Legacy but not Approach=Simple)." );
	}
	else if ( defineWellRightHow == DefineWellRightHowType.RIGHT_IF_AVAILABLE ){
		Message.printStatus ( 2, routine,
			"Rights will be defined using water right net amount appropriation date if available (used with Approach=Legacy but not Approach=Simple)." );
	}
	
	// Initialize ReadWellRights boolean to increase performance
	
	boolean readWellRights = true;	// Default
	if ( ReadWellRights != null ) {
		if ( ReadWellRights.equalsIgnoreCase("True") ) {
			Message.printStatus ( 2, routine, "Individual well rights will be read from HydroBase (not summed)." );
			readWellRights = true;
		}
		else if ( ReadWellRights.equalsIgnoreCase("False") ) {
			Message.printStatus ( 2, routine,
			"Individual well rights will NOT be read from HydroBase (totals for WDID will be used)." );
			readWellRights = false;
		}
	}
	
	// Initialize UseApex boolean to increase performance
	
	boolean useApex = false; // Default
	if ( UseApex != null ) {
		if ( UseApex.equalsIgnoreCase("True") ) {
			Message.printStatus ( 2, routine, "APEX amount will be added to well right amount." );
			useApex = true;
		}
		else if ( UseApex.equalsIgnoreCase("False") ) {
			Message.printStatus ( 2, routine, "APEX amount will not be added to well right amount." );
			useApex = false;
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
	
	if ( warningCount > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		// Data objects used throughout following code
		List<String> partIdList = null; // List of aggregate/system parts
		List<StateMod_Well_CollectionPartIdType> partIdTypeList = null; // List of aggregate/system parts ID types (will contain "WDID" or "Receipt")
		StateMod_Well_CollectionType collectionType = null;
		StateMod_Well_CollectionPartType collectionPartType = null; // Parts used for collection.  Mainly need to key on StateMod_WellStation.
		String wellStationId = null; // Well ID for StateMod or CU location ID for StateCU.
		boolean isCollection = false; // Indicate whether the well/location is
		boolean isSystem = false; // Is well station a system (each of which boolean will also be a collection).
		boolean isAggregate = false; // Is well station an aggregate (each of which will also be a collection).
		StateMod_Well well = null; // StateMod well station to process
		//int matchCount = 0; // FIXME SAM 2009-01-19 add more checks later when no matches
		List<HydroBase_NetAmts> hbwellrList = null; // List of rights from HydroBase for a single station (explicit or collection)
		if ( doSimpleApproach ) {
			// Simple approach where basically the full decree/yield for involved wells is used
			// rather than parcel/split data
			// A major difference between this and the legacy approach is that there is no leap on the parcel year
			Message.printStatus(2,routine,"Reading well rights using Simple approach (direct read of well rights and parcels and not parcel fractions");
			readWellRights = true; // TODO SAM 2016-09-29 need to remove from code below but for now keep since shared with legacy
			int parcelYear = -1; // Parcel year is irrelevant
			List<StateMod_WellRight> smWellRightList = new ArrayList<StateMod_WellRight>(); // The well rights for the well station
			for ( int i = 0; i < stationListSize; i++ ) {
				// Notify command progress listeners which station is being processed...
				notifyCommandProgressListeners ( i, stationListSize, (float)(((float)(i + 1)/(float)stationListSize)*100.0),
					"Processing well station " + i + " of " + stationListSize );
				// Use a well station for processing...
				well = stationList.get(i);
				wellStationId = well.getID();
				if ( !wellStationId.matches(idPatternJava) ) {
					// Identifier does not match...
					continue;
				}
				//++matchCount;
				// Clear out the parcels saved with the well...
				isCollection = false;
				isAggregate = false;
				isSystem = false;
				collectionType = null; // Default...
				collectionPartType = null; // Default...
				isCollection = well.isCollection();
				partIdList = null; // will be expanded below, used for collections and 1-value array for non-collection
				// The collection part list may vary by parcel year (although traditionally
				// D&W aggregation is constant for the period in CDSS modeling).
				// The collection type should not vary.
				if ( isCollection ) {
					collectionType = well.getCollectionType();
					collectionPartType = well.getCollectionPartType();
					if ( collectionType == StateMod_Well_CollectionType.AGGREGATE ) {
						isAggregate = true;
					}
					if ( collectionType == StateMod_Well_CollectionType.SYSTEM ) {
						isSystem = true;
					}
				}
				if ( collectionType == null ) {
					Message.printStatus(2,routine,"Processing well station \"" + well.getID() + "\" isCollection="
						+ isCollection + " collectionType=NA (explicit well)" );
				}
				else {
					Message.printStatus(2,routine,"Processing well station \"" + well.getID() + "\" isCollection="
						+ isCollection + " collectionType=" + collectionType + " collectionPartType=" + collectionPartType );
				}
				// Evaluate the station data to understand how to retrieve the data
				if ( isCollection ) {
					// An aggregate or system
					if ( well.getCollectionPartType() == StateMod_Well_CollectionPartType.DITCH ) {
						if ( well.getIdvcow2().equals("") ||
							well.getIdvcow2().equalsIgnoreCase("N/A") &&
							well.getIdvcow2().equalsIgnoreCase("NA") &&
							(collectionPartType == StateMod_Well_CollectionPartType.DITCH) ) {
							// Well stations says no associated diversion but collection type is ditch, so something is wrong
					        message = "Well " + collectionType + " \"" + wellStationId + "\" is " + collectionPartType +
					        " but the associated diversion is not set - data definition is incomplete - skipping well.";
					        Message.printWarning ( warningLevel, 
					                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
					        status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Verify that the associated diversion is set or use a part type other than " +
									StateMod_Well_CollectionPartType.DITCH + "." ) );
					        continue;
						}
						else {
							// Read the rights for the specific well station by treating as D&W
							partIdList = well.getCollectionPartIDs(parcelYear);
							partIdTypeList = well.getCollectionPartIDTypes(); // Not used here since processing diversion WDID
							smWellRightList = readHydroBaseWellRightsForDWStationsSimple (
								hbdmi,
								wellStationId,
								isCollection, // will be false
								collectionType,
								collectionPartType,
								partIdList, // will have a single part, which is the station ID
								Div_int, // division as integer
								readWellRights, // TODO SAM 2016-06-11 need to figure out if can remove
								useApex,
								defaultAdminNumber,
								defaultApproDate,
								PermitIDPreFormat, // used to format permit identifiers
								warningLevel, warningCount, commandTag, status ); // used for logging and error handling
						}
					}
					else if ( well.getCollectionPartType() == StateMod_Well_CollectionPartType.PARCEL ) {
						// This is not allowed for simple approach since trying to get away from processing parcels, merge rights, etc.
				        message = "Well " + collectionType + " \"" + wellStationId + "\" is " + collectionPartType +
				        ", which is not supported in Approach=Simple processing - skipping well.";
				        Message.printWarning ( warningLevel, 
				                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
				        status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Approach=Simple cannot be used with parcel aggregate or system" ) );
				        continue;
					}
					else if ( well.getCollectionPartType() == StateMod_Well_CollectionPartType.WELL ) {
						// Read the rights for the list of wells station (parcel year does not matter)
						partIdList = well.getCollectionPartIDs(parcelYear);
						partIdTypeList = well.getCollectionPartIDTypes(); // Will not vary by year
						smWellRightList = readHydroBaseWellRightsForWellStationsSimple (
							hbdmi,
							wellStationId,
							isCollection, // will be false
							collectionType,
							collectionPartType,
							partIdList, // the parts for the collection
							partIdTypeList, // the types for the parts
							Div_int, // division as integer
							readWellRights, // TODO SAM 2016-06-11 need to figure out if can remove
							useApex,
							defaultAdminNumber,
							defaultApproDate,
							PermitIDPreFormat, // used to format permit identifiers
							warningLevel, warningCount, commandTag, status ); // used for logging and error handling
					}
					else {
						// Collection type is not recognized
				        message = "Well " + collectionType + " \"" + wellStationId + "\" is " + collectionPartType +
				        ", which is not recognized - skipping well.";
				        Message.printWarning ( warningLevel, 
				                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
				        status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that collection type is Ditch or Well, or use explicit structure (not a collection)." ) );
				        continue;
					}
				}
				else {
					// Not a collection.
					// An explicit well structure - either a D&W (one well) or well specified with WDID or a permit/receipt
					if ( !well.getIdvcow2().isEmpty() &&
						!well.getIdvcow2().equalsIgnoreCase("N/A") &&
						!well.getIdvcow2().equalsIgnoreCase("NA") ) {
						// Well is a D&W associated with a diversion station
						Message.printStatus ( 2, routine, "Well \"" + wellStationId + "\" is explicitly modeled - " +
							"and is a D&W node (diversion and well) - getting rights for wells from ditch -> parcels -> wells." );
						// Read the rights for the specific well station by treating as a single D&W
						partIdList = new ArrayList<String>(1);
						partIdList.add(well.getIdvcow2());
						partIdTypeList = new ArrayList<StateMod_Well_CollectionPartIdType>(1);
						partIdTypeList.add(null); // Will be set later when know if WDID or receipt is used 
						collectionPartType = StateMod_Well_CollectionPartType.DITCH;
						smWellRightList = readHydroBaseWellRightsForDWStationsSimple (
							hbdmi,
							wellStationId,
							isCollection, // will be false
							collectionType,
							collectionPartType,
							partIdList, // will have a single part, which is the station ID
							Div_int, // division as integer
							readWellRights, // TODO SAM 2016-06-11 need to figure out if can remove
							useApex,
							defaultAdminNumber,
							defaultApproDate,
							PermitIDPreFormat, // used to format permit identifiers
							warningLevel, warningCount, commandTag, status ); // used for logging and error handling
					}
					else {
						message = "Well station \"" + wellStationId + "\" is explicitly modeled but StateDMI does not know whether the well is a WDID or permit - "
								+ "software features are not implemented to handle";
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "The work-around is to use a 1-well aggregate/system or use set commands." ) );
						/* TODO SAM 2016-10-03 Enable something like the following at some point but need to explicitly understand
						 * whether the well is a WDID or permit in order to get the data
						Message.printStatus ( 2, routine, "Well \"" + wellStationId + "\" is explicitly modeled - " +
							"getting water rights for the individual well." );
						// Read the rights for the specific well station by treating as a single part
						partIdList = new ArrayList<String>(1);
						partIdList.add(wellStationId);
						partIdTypeList = new ArrayList<String>(1);
						collectionPartType = "Well"; // Useful for troubleshooting
						// The part ID type can be either WDID or Receipt
						// If it has the form of a WDID, try to read that first.  If it does not have the form of a WDID assume a Receipt.
						partIdTypeList.add(StateDMI_Util.determineWellStationIdType(hbdmi,wellStationId,permitIdPattern));
						smWellRightList = readHydroBaseWellRightsForWellStationsSimple (
							hbdmi,
							wellStationId,
							isCollection, // will be false
							collectionType,
							collectionPartType,
							partIdList, // will have a single part, which is the station ID
							partIdTypeList, // will have single part, containing "WDID" or "Receipt"
							Div_int, // division as integer
							readWellRights, // TODO SAM 2016-06-11 need to figure out if can remove
							useApex,
							defaultAdminNumber,
							defaultApproDate,
							PermitIDPreFormat, // used to format permit identifiers
							warningLevel, warningCount, commandTag, status ); // used for logging and error handling
						if ( (smWellRightList.size() == 0) && !partIdTypeList.get(0).equalsIgnoreCase("Receipt") ) {
							// The above tried to read and did not find rights
							// If the above was not receipt, then try receipt - performance hit but allows using same logic
							List<String> partIdList2 = new ArrayList<String>(1);
							partIdList2.add(wellStationId);
							List<String> partIdTypeList2 = new ArrayList<String>(1);
							partIdTypeList2.add("Receipt");
							smWellRightList = readHydroBaseWellRightsForWellStationsSimple (
								hbdmi,
								wellStationId,
								false, // isCollection - false since a single well
								"", // collectionType - not used since not a collection at this point
								"", // collectionPartType - not used
								partIdList2, // will have a single part, which is the well receipt
								partIdTypeList2, // will have single part, containing "Receipt"
								-1, // division as integer, not used
								readWellRights, // TODO SAM 2016-06-11 need to figure out if can remove
								useApex,
								defaultAdminNumber,
								defaultApproDate,
								PermitIDPreFormat, // used to format permit identifiers
								warningLevel, warningCount, commandTag, status ); // used for logging and error handling
							if ( smWellRightList.size() == 0 ) {
								// There was no matching receipt
								message = "Well station \"" + wellStationId + "\" receipt \"" + wellStationId + "\" resulted in no StateMod water right";
								Message.printWarning(warningLevel,
									MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
								status.addToLog ( CommandPhaseType.RUN,
									new CommandLogRecord(CommandStatusType.FAILURE,
										message, "Verify that well station identifier has rights or permits." ) );
							}
						}
						*/
					}
				}
				// Remove duplicate rights for the structure.  Legacy logic would have retained splits of rights as duplicates.
				// Now if a right shows up more than once from above processing it should be removed
				if ( (smWellRightList != null) && (smWellRightList.size() > 0) ) {
					// Loop through all the rights to be added
					StateMod_WellRight right1, right2;
					String decree1Formatted, decree2Formatted;
					Message.printStatus ( 2, routine, "Well \"" + wellStationId + "\" checking for duplicate rights." );
					for ( int iright1 = 0; iright1 < smWellRightList.size(); iright1++ ) {
						// Loop through all the other rights.  If the others are a duplicate, delete them.
						// Loop backwards so loop counter still works
						right1 = smWellRightList.get(iright1);
						decree1Formatted = String.format("%.2f", right1.getDecree());
						for ( int iright2 = (smWellRightList.size() - 1); iright2 > iright1; iright2-- ) {
							// Loop through all the other rights.  If the others are a duplicate, delete them
							// - if right1 and right2 have WDIDs and the same, match the right
							//   - or if right1 and right2 have permit receipt and the same, match the right
							// - admin number also must match
							// - decree also must match to 2 digits
							right2 = smWellRightList.get(iright2);
							decree2Formatted = String.format("%.2f", right2.getDecree());
							if ( !right1.getXWDID().isEmpty() && !right2.getXWDID().isEmpty() ) {
								// Check for duplicate WDID right
								if ( right1.getXWDID().equalsIgnoreCase(right2.getXWDID()) && // WDID
									right1.getIrtem().equalsIgnoreCase(right2.getIrtem()) && // Admin number
									decree1Formatted.equals(decree2Formatted) ) { // Decree
									Message.printStatus ( 2, routine, "Well \"" + wellStationId + "\" removing duplicate right for"
										+ " structure ID \"" + right2.getCgoto() + "\" name=\"" + right2.getName() + "\" adminNum=" + right2.getIrtem() + " WDID=" + right1.getXWDID() + " decree=" + decree2Formatted );
									smWellRightList.remove(right2);
								}
							}
							else if ( !right1.getXPermitReceipt().isEmpty() && !right2.getXPermitReceipt().isEmpty() ) {
								// Check for duplicate permit
								if ( right1.getXPermitReceipt().equalsIgnoreCase(right2.getXPermitReceipt()) && // Receipt
									right1.getIrtem().equalsIgnoreCase(right2.getIrtem()) && // Admin number
									decree1Formatted.equals(decree2Formatted) ) { // Decree
									Message.printStatus ( 2, routine, "Well \"" + wellStationId + "\" removing duplicate permit for"
										+ " structure ID \"" + right2.getCgoto() + "\" name=\"" + right2.getName() + "\" adminNum=" + right2.getIrtem() + " permitRecipt=" + right1.getXPermitReceipt() + " decree=" + decree2Formatted );
									smWellRightList.remove(right2);
								}
							}
						}
					}
				}
				//
				if ( (smWellRightList.size() > 99) &&
					((IDFormat_int == __StationIDW_NN_int) || (IDFormat_int == __StationIDWNN_int) ||
					(IDFormat_int == __StationID_NN_int) || (IDFormat_int == __StationIDNN_int)) ) {
					// Number of rights will overflow the format so warn
					message = "Number of water rights for well station \"" + wellStationId + "\" is " + smWellRightList.size()
						+ " and will overflow the specified NN right ID format.";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a NNN right ID format." ) );
				}
				else if ( (smWellRightList.size() > 999) &&
					((IDFormat_int == __StationIDW_NNN_int) || (IDFormat_int == __StationIDWNNN_int) ||
					(IDFormat_int == __StationID_NNN_int) || (IDFormat_int == __StationIDNN_int)) ) {
					// Number of rights will overflow the format so warn
					message = "Number of water rights for well station \"" + wellStationId + "\" is " + smWellRightList.size()
						+ " and will overflow the specified NNN right ID format.";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount),routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify why there are so many well rights for a single well station or use an Auto format." ) );
				}
				// Add the rights that were read for the station
				addStateModRightsToProcessorRightList ( smWellRightList, processorRightList, OnOffDefault_int,
					PermitIDPreFormat, IDFormat_int, PermitIDPostFormat,
					warningLevel, warningCount, commandTag, status );
			}
		}
		else { // Legacy approach that is more complex
		List<HydroBase_NetAmts> hbwellrSortedList = new ArrayList<HydroBase_NetAmts>(100); // List of rights from HydroBase, after manual sort on admin number
		int nhbwellr = 0; // The number of rights read from HydroBase
		HydroBase_NetAmts hbwellr = null; // Single right from HydroBase
		int ir = 0; // Counter for rights in loop.
		double [] irtemArray = null; // Used to sort rights in a collection.
		int [] sortOrder = null; // Array used when sorting rights in a collection.
		
		int parcelYear = 0; // Used to process parcel years from HydroBase
		int parcelYear2 = 0; // Parcel year in output, may be reset if explicit wells (no parcels used)
		
		// Loop through the locations...
		for ( int i = 0; i < stationListSize; i++ ) {
			// Notify command progress listeners which station is being processed...
			notifyCommandProgressListeners ( i, stationListSize, (float)(((float)(i + 1)/(float)stationListSize)*100.0),
				"Processing well station " + i + " of " + stationListSize );
			// Use a well station for processing...
			well = stationList.get(i);
			wellStationId = well.getID();
			if ( !wellStationId.matches(idPatternJava) ) {
				// Identifier does not match...
				continue;
			}
			//++matchCount;
			// Clear out the parcels saved with the well...
			//well.getParcels().removeAllElements();
			isCollection = false;
			isAggregate = false;
			isSystem = false;
			collectionType = null;	// Default...
			collectionPartType = null;	// Default...
			isCollection = well.isCollection();
			if ( isCollection ) {
				collectionType = well.getCollectionType();
				collectionPartType = well.getCollectionPartType();
				if ( collectionType == StateMod_Well_CollectionType.AGGREGATE ) {
					isAggregate = true;
				}
				if ( collectionType == StateMod_Well_CollectionType.SYSTEM ) {
					isSystem = true;
				}
			}
			// Do a check for stations where idvcow2 indicates no ditch but collection part of Ditch is used
			if ( well.getIdvcow2().equals("") ||
				well.getIdvcow2().equalsIgnoreCase("N/A") &&
				well.getIdvcow2().equalsIgnoreCase("NA") &&
				(collectionPartType == StateMod_Well_CollectionPartType.DITCH) ) {
		        message = "Well " + collectionType + " \"" + wellStationId + "\" is " + collectionPartType +
		        " but the associated diversion is not set - data definition is incomplete - skipping well.";
		        Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
		        status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the associated diversion is set or use a part type other than " +
							StateMod_Well_CollectionPartType.DITCH + "." ) );
		        continue;
			}
			// Loop through the parcel years.  Put the loop here because the
			// parts of a collection could theoretically vary by year.
			// If processing a sprinkler snapshot list file, only one year will be processed.
			for ( int iParcelYear = 0;	iParcelYear < parcelYears.length; iParcelYear++ ) {
				// Year to process - will be -999 if only reading explicit wells
				parcelYear = parcelYears[iParcelYear];
				parcelYear2 = parcelYear;
				String yearString = "Parcel year " + parcelYears[iParcelYear] + ": ";
				
				Message.printStatus ( 2, routine, yearString + "Processing well station ID=" + wellStationId + " (" + (i + 1) +
					" of " + stationListSize + ") isCollection=" + isCollection + " collectionType="
					+ collectionType + " collectionPartType=" + well.getCollectionPartType() + " parcelYear=" + iParcelYear);
		
				hbwellrList = null;	// initialize for zero-length list check below
				if ( isCollection && (well.getCollectionPartType() == StateMod_Well_CollectionPartType.WELL) ) {
					// TODO SAM 2016-09-10 How to know whether to go through parcels (irrigation) or directly to net amounts (M&I wells)?
					// M&I could be an issue if the Well ID is actually a receipt because HydroBase is not distributed with
					// well permits and well/parcel data might not have a record for the well.
					if ( (iParcelYear == 0) || (iParcelYear == -999) ) {
						// StateMod well station that is a collection of wells - only read for first year since rights
						// will apply for the full period...
						Message.printStatus ( 2, routine, yearString + locType + " \"" + wellStationId +
						"\" is associated with a collection of wells - processing one time..." );
						// Aggregate or system, by well...
						partIdList = well.getCollectionPartIDs(parcelYear);
						partIdTypeList = well.getCollectionPartIDTypes(); // Will not vary by year
						try {
							hbwellrList = readHydroBaseWellRightsForWellStationList (
								hbdmi,
								wellStationId,
								isCollection,
								collectionType,
								parcelYears,
								partIdList,
								partIdTypeList,
								readWellRights,
								useApex,
								defaultAdminNumber,
								defaultApproDate,
								PermitIDPreFormat,
								warningLevel, warningCount, commandTag, status);
						}
						catch ( Exception e ) {
							message = yearString + "Unexpected error querying HydroBase (" + e + ").";
							Message.printWarning(3, routine, e);
							Message.printWarning(warningLevel,
								MessageUtil.formatMessageTag( commandTag, ++warningCount),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Report problem to software support." ) );
							hbwellrList = null;  // handled below
						}
					}
				}
				else if ( isCollection && (well.getCollectionPartType() == StateMod_Well_CollectionPartType.PARCEL) && (parcelYear > 0) ){
					// StateMod well station that is a collection (well-only
					// stations in StateMod MUST currently be collections)
					// OR a StateCU Location that is a collection of parcels
					// (and therefore a well-only location)...
					Message.printStatus ( 2, routine, yearString + locType + " \"" + wellStationId +
					"\" is associated with a collection of parcels..." );
					// Aggregate or system, by parcel...
					partIdList = well.getCollectionPartIDs(parcelYear);
					try {
						hbwellrList = readHydroBaseWellRightsForParcelList (
							hbdmi,
							well,
							wellStationId,
							locType,
							partIdList,
							null,		// No percent_yield for wells (1.0 always)
							null,		// No ditch aggregate part id
							//-1,			// No ditch aggregate part count
							//-1,			// No ditch aggregate number of parts
							parcelYear, yearString,
							Div_int,
							defineWellRightHow,
							readWellRights,
							useApex,
							defaultAdminNumber,
							defaultApproDate,
							PermitIDPreFormat,
							warningLevel, warningCount, commandTag, status );
					}
					catch ( Exception e ) {
						message = yearString + "Unexpected error querying HydroBase (" + e + ").";
						Message.printWarning(3, routine, e);
						Message.printWarning(warningLevel,
							MessageUtil.formatMessageTag( commandTag, ++warningCount),
							routine, message );
						status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report problem to software support." ) );
						hbwellrList = null;  // handled below
					}
				}
				else if ( !well.getIdvcow2().equals("") &&
					!well.getIdvcow2().equalsIgnoreCase("N/A") &&
					!well.getIdvcow2().equalsIgnoreCase("NA") && (parcelYear > 0) ) {
					// StateMod well that is associated with a diversion
					// and the diversion may or may not be an aggregate.
					Message.printStatus ( 2, routine, yearString + locType + " \"" + wellStationId + "\" is associated with a " +
						"diversion.  Determining associated parcels, and then wells..." );
					// Get the well station parts...
					if ( well.isCollection() ) {
						collectionType = well.getCollectionType();
						Message.printStatus ( 2, routine, "Well \"" + well.getID() + "\" is a " +
							collectionType + "...processing each part...");
						// Diversion aggregates are only set once (year is ignored)
						partIdList = well.getCollectionPartIDs ( 0 );
					}
					else {
						// To reuse code below, just use a single part...
						//collectionType = "(explicit)";
						// TODO SAM 2006-01-31
						//name = div.getName();
						//name = well.getName();
						partIdList = new Vector<String>();
						partIdList.add ( well.getID() );
						Message.printStatus ( 2, routine, yearString + "Well \"" + well.getID() +
							"\" is associated with a an explicit diversion (no aggregate/system specified)..." +
							"processing as system with one ditch part...");
					}
					hbwellrList = readHydroBaseWellRightsForDiversionWDIDList (
						hbdmi,
						well,
						wellStationId,
						locType,
						collectionType,
						partIdList,
						parcelYear, yearString,
						Div_int,
						defineWellRightHow,
						readWellRights,
						useApex,
						defaultAdminNumber,
						defaultApproDate,
						PermitIDPreFormat,
						warningLevel, warningCount, commandTag, status, cacheHydroBase );
				}
				else if ( !isCollection ) {
					if ( iParcelYear == 0 ) {
						// Single well - get its water rights.  Only do for the first year read because in this
						// case there is no relation to parcels or parcel years...
						if ( iParcelYear == 0 ) {
							Message.printStatus ( 2, routine, yearString + "Well \"" + wellStationId + "\" is explicitly modeled - " +
								"getting water rights for the individual well - processing once." );
							hbwellrList = readHydroBaseWellRightsForWellStation (
									hbdmi,
									wellStationId,
									isCollection,
									partIdList,
									Div_int,
									readWellRights,
									useApex,
									defaultAdminNumber,
									defaultApproDate,
									PermitIDPreFormat,
									warningLevel, warningCount, commandTag, status );
							// Reset the parcel year to be used in final water rights
							parcelYear2 = -999;
						}
					}
				}
				else {
					// Fall through case is not being considered somehow.  This may be an input error
					// bug checks should always alert users to this condition so that they can fix it.  Otherwise,
					// water rights may not be read.
					message = yearString + "unhandled configuration for well \"" + wellStationId + "\".";
					Message.printWarning(warningLevel,
						MessageUtil.formatMessageTag( commandTag, ++warningCount),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that definition of well station is correct for water right read." ) );
					hbwellrList = null; 
				}
		
				// This applies to well-only collection and D&W...
		
				if ( isCollection || !well.getIdvcow2().equals("") &&
					!well.getIdvcow2().equalsIgnoreCase("N/A") &&
					!well.getIdvcow2().equalsIgnoreCase("NA")) {
					// If a system or aggregate, sort the water rights by administration
					// number.  This will make sure that the insert order is by administration number.
					// For non-collection locations, the sort is done in the database query.
					nhbwellr = 0;
					if ( hbwellrList != null ) {
						nhbwellr = hbwellrList.size();
					}
					if ( (isAggregate || isSystem) && (nhbwellr > 0) ) {
						irtemArray = new double[nhbwellr];
						for ( ir = 0; ir < nhbwellr; ir++ ) {
							hbwellr = hbwellrList.get(ir);
							irtemArray[ir] = hbwellr.getAdmin_no();
						}
						sortOrder = new int[nhbwellr];
						MathUtil.sort ( irtemArray, MathUtil.SORT_QUICK,
							MathUtil.SORT_ASCENDING, sortOrder, true );
						hbwellrSortedList.clear();
						for ( ir = 0; ir < nhbwellr; ir++ ) {
							hbwellrSortedList.add ( hbwellrList.get(sortOrder[ir]) );
						}
						hbwellrList = hbwellrSortedList;
					}
				}
				// Still within a parcel year.
				//
				// Process the water rights from HydroBase_NetAmts to
				// StateMod_WellRight.  Save the parcel year information so that
				// merging of rights can occur later...
			
				nhbwellr = 0;
				if ( hbwellrList != null ) {
					nhbwellr = hbwellrList.size();
				}
				message = yearString + locType + " \"" + wellStationId + "\" has "
				+ nhbwellr + " HydroBase rights to format for output.";
				Message.printStatus ( 2, routine, message );
				
				addHydroBaseRightsToStateModWellRights ( 
					locType,
					wellStationId,
					hbwellrList,
					DecreeMin,
					DecreeMin_double,
					PermitIDPreFormat,
					IDFormat_int,
					PermitIDPostFormat,
					parcelYear2, // Will be zero if explicit well
					yearString,
					isCollection,
					collectionType,
					collectionPartType,
					OnOffDefault_int,
					processorRightList,
					SMWellRight_match_Vector,
					warningLevel, warningCount, commandTag, status );
			} // End parcel year
		} // End location
		
		// TODO SAM 2007-05-24 Evaluate how this works with multiple years.
		
		StateDMI_Util.warnAboutDataMatches ( toString(), true, SMWellRight_match_Vector, "Well Rights" );
	}
	} // End Legacy
    catch ( Exception e ) {
        message = "Unexpected error reading well rights from HydroBase (" + e + ").";
        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }

	if ( warningCount > 0 ) {
		message = "There were " + warningCount + " warnings processing the command.";
		Message.printWarning ( warningLevel,
		MessageUtil.formatMessageTag(commandTag, ++warningCount),routine,message);
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
	
	String Approach = parameters.getValue ( "Approach" );
	String ID = parameters.getValue ( "ID" );
	String PermitIDPattern = parameters.getValue ( "PermitIDPattern" );
	String PermitIDPreFormat = parameters.getValue ( "PermitIDPreFormat" );
	String IDFormat = parameters.getValue ( "IDFormat" );
	String PermitIDPostFormat = parameters.getValue ( "PermitIDPostFormat" );
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

	if ( Approach != null && !Approach.isEmpty() ) {
		b.append ( "Approach=\"" + Approach + "\"" );
	}
	if ( ID != null && ID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( PermitIDPattern != null && PermitIDPattern.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitIDPattern=\"" + PermitIDPattern + "\"" );
	}
	if ( PermitIDPreFormat != null && PermitIDPreFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitIDPreFormat=\"" + PermitIDPreFormat + "\"" );
	}
	if ( IDFormat != null && IDFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDFormat=\"" + IDFormat + "\"" );
	}
	if ( PermitIDPostFormat != null && PermitIDPostFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitIDPostFormat=\"" + PermitIDPostFormat + "\"" );
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
