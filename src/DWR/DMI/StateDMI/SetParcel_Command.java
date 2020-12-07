// SetParcel_Command - This class initializes, checks, and runs the SetParcel() command.

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

import DWR.StateCU.StateCU_Parcel;
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
This class initializes, checks, and runs the SetParcel() command.
*/
public class SetParcel_Command extends AbstractCommand implements Command
{
	
protected final String _Drip = "DRIP";
protected final String _Flood = "FLOOD";
protected final String _Sprinkler = "SPRINKLER";

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
public SetParcel_Command ()
{	super();
	setCommandName ( "SetParcel" );
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
	String Division = parameters.getValue ( "Division" );
	String District = parameters.getValue ( "District" );
	String CropType = parameters.getValue ( "CropType" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String Area = parameters.getValue ( "Area" );
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
	
	if ( (Division != null) && !Division.isEmpty() && !StringUtil.isInteger(Division)) {
		message = "The division is not an integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the division as an integer." ) );
	}
	
	if ( (District != null) && !District.isEmpty() && !StringUtil.isInteger(District)) {
		message = "The district is not an integer.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the district as an integer." ) );
	}
	
	// CropType not checked
	
	if ( (IrrigationMethod != null) && !IrrigationMethod.equalsIgnoreCase(_Drip) &&
		!IrrigationMethod.equalsIgnoreCase(_Flood) &&
		!IrrigationMethod.equalsIgnoreCase(_Sprinkler) ) {
		message = "The irrigation method (" + IrrigationMethod + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the irrigation method as " + _Drip + "," + _Flood + " or " + _Sprinkler + ".") );
	}

	if ( (Area != null) && !Area.isEmpty() && !StringUtil.isDouble(Area)) {
		message = "The area is not a number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the area as a number." ) );
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
		if ( (Division == null) || Division.isEmpty() ) {
			message = "The water division must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the water division." ) );
		}
		if ( (District == null) || District.isEmpty() ) {
			message = "The water district must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the water district." ) );
		}
		if ( (CropType == null) || CropType.isEmpty() ) {
			message = "The crop type must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop type." ) );
		}
		if ( (IrrigationMethod == null) || IrrigationMethod.isEmpty() ) {
			message = "An irrigation method must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the irrigation method." ) );
		}
		if ( (Area == null) || Area.isEmpty() ) {
			message = "The area must be specified.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the area." ) );
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
	List<String> validList = new ArrayList<>(9);
	validList.add ( "Year" );
    validList.add ( "ParcelID" );
	validList.add ( "Division" );
	validList.add ( "District" );
	validList.add ( "CropType" );
	validList.add ( "IrrigationMethod" );
	validList.add ( "Area" );
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
	return (new SetParcel_JDialog ( parent, this )).ok();
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
	String Division = parameters.getValue ( "Division" );
	int div = -1;
	if ( StringUtil.isInteger(Division)) {
		div = Integer.parseInt(Division);
	}
	String District = parameters.getValue ( "District" );
	int wd = -1;
	if ( StringUtil.isInteger(District)) {
		wd = Integer.parseInt(District);
	}
	String CropType = parameters.getValue ( "CropType" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String Area = parameters.getValue ( "Area" );
	double area = -1.0;
	if ( StringUtil.isDouble(Area)) {
		area = Double.parseDouble(Area);
	}
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
			// Create a new parcel
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Parcel for year " + year + " and parcel ID " + ParcelID +
					" not found: warning and not setting parcel data.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
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
				parcel = new StateCU_Parcel();
				// Set required data
				parcel.setYear(year);
				parcel.setID(ParcelID);
				parcel.setIdInt(Integer.parseInt(ParcelID));
				// Set optional data
				if ( div > 0 ) {
					parcel.setDiv(div);
				}
				if ( wd > 0 ) {
					parcel.setDiv(wd);
				}
				if ( (CropType != null) && !CropType.isEmpty() ) {
					parcel.setCrop(CropType);
				}
				if ( (IrrigationMethod != null) && !IrrigationMethod.isEmpty() ) {
					parcel.setIrrigationMethod(IrrigationMethod);
				}
				if ( area >= 0.0 ) {
					parcel.setArea(area);
				}
				if ( area >= 0.0 ) {
					parcel.setAreaUnits("acre");
				}
				List<String> parcelProblems = new ArrayList<>();
				processor.findAndAddCUParcel(parcel, false, routine, "    ", parcelProblems);
			}
		}
		else {
			// Parcel was found
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Parcel for year " + year + " and parcel ID " + ParcelID +
					" found: warning and not setting parcel data.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
								"  The parcel must exist before setting any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Parcel for year " + year + " and parcel ID " + ParcelID +
					" found: failing and not setting parcel data.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." +
								"  The parcel must exist before setting any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Set) ) {
				// Set optional data in the existing parcel.
				if ( div > 0 ) {
					parcel.setDiv(div);
				}
				if ( wd > 0 ) {
					parcel.setDiv(wd);
				}
				if ( (CropType != null) && !CropType.isEmpty() ) {
					parcel.setCrop(CropType);
				}
				if ( (IrrigationMethod != null) && !IrrigationMethod.isEmpty() ) {
					parcel.setIrrigationMethod(IrrigationMethod);
				}
				if ( area >= 0.0 ) {
					parcel.setArea(area);
				}
				if ( area >= 0.0 ) {
					parcel.setAreaUnits("acre");
				}
			}
			else if ( IfNotFound.equalsIgnoreCase(_Remove) ) {
				Message.printStatus(2, routine, "Removing existing parcel for year=" + year + " parcelID=" + ParcelID);
				parcelMap.remove(parcelKey);
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting parcel data (" + e + ").";
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
	String Division = parameters.getValue ( "Division" );
	String District = parameters.getValue ( "District" );
	String CropType = parameters.getValue ( "CropType" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String Area = parameters.getValue ( "Area" );
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
	if ( Division != null && Division.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Division=" + Division );
	}
	if ( District != null && District.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "District=" + District );
	}
	if ( CropType != null && CropType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropType=\"" + CropType + "\"");
	}
	if ( IrrigationMethod != null && IrrigationMethod.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigationMethod=" + IrrigationMethod );
	}
	if ( Area != null && Area.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Area=" + Area );
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