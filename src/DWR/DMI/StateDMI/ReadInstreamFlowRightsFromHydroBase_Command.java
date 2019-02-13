// ReadInstreamFlowRightsFromHydroBase_Command - This class initializes, checks, and runs the ReadInstreamFlowRightsFromHydroBase() command.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;

import RTi.DMI.DMIUtil;
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
<p>
This class initializes, checks, and runs the ReadInstreamFlowRightsFromHydroBase() command.
</p>
*/
public class ReadInstreamFlowRightsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
 * 
 */
protected final String _1 = "1";
protected final String _AppropriationDate = "AppropriationDate";

/**
Constructor.
*/
public ReadInstreamFlowRightsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadInstreamFlowRightsFromHydroBase" );
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
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
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

	if ( (OnOffDefault != null) && (OnOffDefault.length() > 0) &&
		!OnOffDefault.equalsIgnoreCase(_AppropriationDate) && !StringUtil.isInteger(OnOffDefault)) {
		message = "The OnOffDefault value (" + OnOffDefault + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify OnOffDefault as an integer year or " + _AppropriationDate + " (default).") );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "OnOffDefault" );
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
{	return (new ReadInstreamFlowRightsFromHydroBase_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
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
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	if ( OnOffDefault == null ) {
		OnOffDefault = _AppropriationDate; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of instream flow stations...
	
	List stationList = null;
	int stationListSize = 0;
	try {
		stationList = (List)processor.getPropContents ( "StateMod_InstreamFlowStation_List");
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting data from processor.";
		Message.printWarning(warning_level,
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
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
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
	
	// Now process...
	
	try {
		int OnOffDefault_int = 1; // Will be reset or ignored.
		boolean OnOffDefault_year = true; // Is year used for switch?
		if ( OnOffDefault != null ) {
			if ( OnOffDefault.equalsIgnoreCase("AppropriationDate") ) {
				Message.printStatus ( 2, routine,
				"Water right OnOff switch will be set to the year of the appropriation date." );
				OnOffDefault_year = true;
			}
			else {
				OnOffDefault_year = false;
				OnOffDefault_int = StringUtil.atoi(OnOffDefault);
				Message.printStatus ( 2, routine, "Water right OnOff switch will be set to " + OnOffDefault );
			}
		}
		
		StateMod_InstreamFlow ifs = null; // StateMod station.
		StateMod_InstreamFlowRight ifr = null; // StateMod water right.
		String id; // Instream flow ID.
		List hb_rights;	// Water rights from HydroBase
		int nrights; // Number of rights for the station
		int iright; // Counter for rights for station
		int [] wdid_parts = new int[2]; // WDID parts
		HydroBase_NetAmts hb_right; // Water right from HydroBase
		HydroBase_AdministrationNumber admin_data = null;
		for ( int i = 0; i < stationListSize; i++ ) {
			ifs = (StateMod_InstreamFlow)stationList.get(i);
			id = ifs.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}

			// Read the rights from HydroBase.

			try {
				// Parse out the WDID...
				HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
			}
			catch ( Exception e ) {
				// Not a WDID - non-fatal, just ignore the instream flow location...
				Message.printStatus ( 2, routine,
					"Skipping instream flow station \"" + id + "\" - does not appear to be a WDID." );
					continue;
			}

			try {
				hb_rights = hbdmi.readNetAmtsList (
					DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, command_tag, routine,
				"Error getting water rights data from HydroBase for " + id );
				Message.printWarning ( 3, routine, e );
				// Not fatal because aggregates and other nodes may not be in HydroBase...
				//++warning_count;
				continue;
			}
			if ( (hb_rights == null) ||((nrights = hb_rights.size())== 0)){
				Message.printWarning ( 3, routine, "No water rights data from HydroBase for " + id );
				continue;
			}
			// Add the water rights that have been returned...
			for ( iright = 0; iright < nrights; iright++ ) {
				hb_right=(HydroBase_NetAmts)hb_rights.get(iright);
				ifr = new StateMod_InstreamFlowRight();
				ifr.setID ( id + "." + StringUtil.formatString((iright + 1),"%02d") );
				ifr.setName ( hb_right.getWr_name() );
				ifr.setCgoto ( id );
				ifr.setIrtem ( StringUtil.formatString(hb_right.getAdmin_no(), "%11.5f") );
				if ( OnOffDefault_year ) {
					// Convert the administration number to a year...
					admin_data = new HydroBase_AdministrationNumber ( hb_right.getAdmin_no() );
					ifr.setSwitch ( admin_data.getAppropriationDate().getYear() );
				}
				else {
					// Use the default value for the administration number...
					ifr.setSwitch ( OnOffDefault_int );
				}
				ifr.setDcrifr ( hb_right.getNet_rate_abs() );
				processor.findAndAddSMInstreamFlowRight ( ifr, true );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error reading instream flow rights from HydroBase (" + e + ").";
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
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( OnOffDefault != null && OnOffDefault.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffDefault=" + OnOffDefault );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
