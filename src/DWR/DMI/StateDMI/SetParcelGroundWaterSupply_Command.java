// SetParcelGroundWaterSupply_Command - This class initializes, checks, and runs the SetParcelGroundWaterSupply() command.

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

import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateCU.StateCU_SupplyFromGW;
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

/**
This class initializes, checks, and runs the SetParcelGroundWaterSupply() command.
*/
public class SetParcelGroundWaterSupply_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound and IfFound parameters.
*/
protected final String _Add = "Add";
protected final String _Set = "Set";
protected final String _Remove = "Remove";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public SetParcelGroundWaterSupply_Command ()
{	super();
	setCommandName ( "SetParcelGroundWaterSupply" );
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
	String Year = parameters.getValue ( "Year" );
	String ParcelID = parameters.getValue ( "ParcelID" );
	String WDID = parameters.getValue ( "WDID" );
	String Receipt = parameters.getValue ( "Receipt" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String IfFound = parameters.getValue ( "IfFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (Year == null) || Year.isEmpty() ) {
		message = "A year must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year." ) );
	}
	else if ( (Year != null) && !Year.isEmpty() && !StringUtil.isInteger(Year)) {
		message = "The year is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the year as an integer YYYY." ) );
	}

	if ( (ParcelID == null) || ParcelID.isEmpty() ) {
		message = "A parcel identifier must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parcel identifier." ) );
	}
	else if ( (ParcelID != null) && !ParcelID.isEmpty() && !StringUtil.isInteger(ParcelID)) {
		message = "The parcel ID is not valid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the parcel ID as an integer." ) );
	}

	if ( ((WDID == null) || WDID.isEmpty()) && ((Receipt == null) || Receipt.isEmpty()) ) {
		message = "WDID and Receipt are not specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify WDID or Receipt." ) );
	}

	if ( (WDID != null) && !WDID.isEmpty() && (Receipt != null) && !Receipt.isEmpty() ) {
		message = "WDID and Receipt are both specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify WDID or Receipt." ) );
	}
	
	if ( (WDID != null) && !WDID.isEmpty() && !StringUtil.isInteger(WDID)) {
		message = "The WDID is not an integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the WDID as an integer." ) );
	}
	
	if ( (IfNotFound != null) && !IfNotFound.isEmpty() &&
		!IfNotFound.equalsIgnoreCase(_Add) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	if ( (IfNotFound != null) && IfNotFound.equalsIgnoreCase(_Add) ) {
		// When adding, all optional parts are required (Year and ParcelID were specified above).
		if ( (WDID == null) || WDID.isEmpty() ) {
			message = "The WDID must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the WDID." ) );
		}
	}

	if ( (IfFound != null) && !IfFound.isEmpty() &&
		!IfFound.equalsIgnoreCase(_Remove) && !IfFound.equalsIgnoreCase(_Set) &&
		!IfFound.equalsIgnoreCase(_Ignore) &&
		!IfFound.equalsIgnoreCase(_Fail) && !IfFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfFound value (" + IfFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfFound as " + _Remove + ", " + _Set + ", " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(5);
	validList.add ( "Year" );
    validList.add ( "ParcelID" );
	validList.add ( "WDID" );
	validList.add ( "IfNotFound" );
	validList.add ( "IfFound" );
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
{
	return (new SetParcelGroundWaterSupply_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String Year = parameters.getValue ( "Year" );
	int year = -1;
	if ( StringUtil.isInteger(Year)) {
		year = Integer.parseInt(Year);
	}
	String ParcelID = parameters.getValue ( "ParcelID" );
	String WDID = parameters.getValue ( "WDID" );
	String Receipt = parameters.getValue ( "Receipt" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Add; // Default
	}
	String IfFound = parameters.getValue ( "IfFound" );
	if ( IfFound == null ) {
		IfFound = _Set; // Default
	}
		
	// Get the hash map of parcels...
	
	HashMap<String,StateCU_Parcel> parcelMap = null;
	try {
		@SuppressWarnings("unchecked")
		HashMap<String,StateCU_Parcel> dataMap = (HashMap<String,StateCU_Parcel>)processor.getPropContents( "StateCU_Parcel_List");
		parcelMap = dataMap;
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Parcel_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		Message.printWarning(warning_level,routine,e);
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		// Match the parcel based on the year and parcel ID.

		String parcelKey = Year + "-" + ParcelID;
		StateCU_Parcel parcel = parcelMap.get(parcelKey);

		if ( parcel == null ) {
			// Parcel must be matched.
			message = "Parcel for year " + year + " and parcel ID " + ParcelID +
				" not found: unable to set the supply.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the year and parcel identifier is correct." ) );

		}
		else {
			// Try to find the matching groundwater supply for the parcel.
			StateCU_SupplyFromGW supply = null;
			StateCU_SupplyFromGW supplyFromGW = null;
			for ( StateCU_Supply supply0 : parcel.getSupplyList() ) {
				if ( supply0 instanceof StateCU_SupplyFromGW ) {
					supplyFromGW = (StateCU_SupplyFromGW)supply0;
				}
				if ( ((WDID != null) && !WDID.isEmpty() && supplyFromGW.getWDID().equals(WDID)) ||
					((Receipt != null) && !Receipt.isEmpty() && supplyFromGW.getReceipt().equalsIgnoreCase(Receipt)) ) {
					supply = supplyFromGW;
					break;
				}
			}
		
			if ( supply == null ) {
				// Create a new supply
				if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
					message = "Supply for year " + year + ", parcel ID " + ParcelID +
						" and WDID " + WDID + " not found: warning and not setting supply data.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Verify that the data are correct." +
									"  The parcel must exist before setting any data." ) );
				}
				else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
					message = "Parcel for year " + year + " and parcel ID " + ParcelID +
						" not found: failing and not setting parcel data.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the identifier is correct." +
									"  The parcel must exist before setting any data." ) );
				}
				else if ( IfNotFound.equalsIgnoreCase(_Add) ) {
					supply = new StateCU_SupplyFromGW();
					// Set required data
					if ( (WDID != null) && !WDID.isEmpty()) {
						// Supply type is WDID
						Message.printStatus(2, routine, "Adding supply for year=" + Year + " parcel ID=" + ParcelID + " WDID=" + WDID);
						supply.setCollectionPartIdType(""+StateCU_Location_CollectionPartIdType.WDID);
						supply.setID(WDID);
						supply.setWDID(WDID);
					}
					else if ( (Receipt != null) && !Receipt.isEmpty()) {
						// Supply type is Receipt
						Message.printStatus(2, routine, "Adding supply for year=" + Year + " parcel ID=" + ParcelID + " receipt=" + Receipt);
						supply.setCollectionPartIdType(""+StateCU_Location_CollectionPartIdType.RECEIPT);
						supply.setID(Receipt);
						supply.setReceipt(Receipt);
					}
					parcel.addSupply(supply);
				}
			}
			else {
				// Parcel was found
				if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
					message = "Parcel for year " + year + ", parcel ID " + ParcelID +
						" and WDID " + WDID + " found: warning and not setting supply data.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Verify that the identifier is correct." +
									"  The parcel must exist before setting any data." ) );
				}
				else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
					message = "Parcel for year " + year + ", parcel ID " + ParcelID +
						" and WDID " + WDID + " found: failing and not setting supply data.";
					Message.printWarning(warning_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that the identifier is correct." +
									"  The parcel must exist before setting any data." ) );
				}
				else if ( IfNotFound.equalsIgnoreCase(_Remove) ) {
					// Remove the supply
					if ( (WDID != null) && !WDID.isEmpty()) {
						// Supply type is WDID
						Message.printStatus(2, routine, "Removing existing groundwater supply for year=" + year + " parcelID=" + ParcelID + " WDID=" + WDID);
						parcel.removeSupply(StateCU_Location_CollectionPartIdType.WDID, WDID);
					}
					else if ( (Receipt != null) && !Receipt.isEmpty()) {
						// Supply type is Receipt
						Message.printStatus(2, routine, "Removing existing groundwater supply for year=" + year + " parcelID=" + ParcelID + " Receipt=" + Receipt);
						parcel.removeSupply(StateCU_Location_CollectionPartIdType.RECEIPT, Receipt);
					}
				}
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting parcel supply data (" + e + ").";
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
	
	String Year = parameters.getValue ( "Year" );
	String ParcelID = parameters.getValue ( "ParcelID" );
	String WDID = parameters.getValue ( "WDID" );
	String Receipt = parameters.getValue ( "Receipt" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String IfFound = parameters.getValue ( "IfFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( Year != null && Year.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Year=" + Year );
	}
	if ( ParcelID != null && ParcelID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelID=\"" + ParcelID + "\"" );
	}
	if ( WDID != null && WDID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WDID=" + WDID );
	}
	if ( Receipt != null && Receipt.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Receipt=\"" + Receipt + "\"" );
	}
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	if ( IfFound != null && IfFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfFound=" + IfFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}