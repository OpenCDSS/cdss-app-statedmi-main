// ReadBlaneyCriddleFromHydroBase_Command - This class initializes, checks, and runs the ReadBlaneyCriddleFromHydroBase() command.

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
import DWR.DMI.HydroBaseDMI.HydroBase_CUBlaneyCriddle;
import DWR.StateCU.StateCU_BlaneyCriddle;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
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
import RTi.Util.IO.PropList;

/**
<p>
This class initializes, checks, and runs the ReadBlaneyCriddleFromHydroBase() command.
</p>
*/
public class ReadBlaneyCriddleFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public ReadBlaneyCriddleFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadBlaneyCriddleFromHydroBase" );
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
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (BlaneyCriddleMethod == null) || (BlaneyCriddleMethod.length() == 0) ) {
		message = "An Blaney-Criddle method must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the Blaney-Criddle method to match." ) );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "BlaneyCriddleMethod" );
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
{	String routine = getClass().getName() + ".editCommand";
	CommandProcessor processor = getCommandProcessor();
	List BlaneyCriddleMethod_List = new Vector();
	try {
		BlaneyCriddleMethod_List = (List)processor.getPropContents("CUMethod_List");
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error getting Blaney-Criddle method list - will not be listed in editor." );
	}
	return (new ReadBlaneyCriddleFromHydroBase_JDialog ( parent, this, BlaneyCriddleMethod_List )).ok();
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
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
	
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
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		// Remove all the elements for the Vector that tracks when identifiers
		// are read from more than one main source (e.g., CCH, HydroBase).
		// This is used to print a warning.
		processor.resetDataMatches ( processor.getStateCUBlaneyCriddleMatchList() );

		// Read from HydroBase...

		List hbc_Vector = hbdmi.readCUBlaneyCriddleListForMethodDesc ( BlaneyCriddleMethod, false );

		int size = 0;
		if ( hbc_Vector != null ) {
			size = hbc_Vector.size();
		}

		// Loop through the HydroBase objects and add new StateCU_BlaneyCriddle
		// instances for each HydroBase object...

		StateCU_BlaneyCriddle cubc = null;
		HydroBase_CUBlaneyCriddle hbc;
		String last_method_desc = ""; // Need because we need to process multiple records for a Blaney-Criddle object
		String last_cropname = "";
		String method_desc, cropname;
		int nrec = 0;
		boolean added = false;
		for (int i = 0; i < size; i++) {
			hbc = (HydroBase_CUBlaneyCriddle)hbc_Vector.get(i);
			method_desc = hbc.getMethod_desc();
			cropname = hbc.getCropname();
			if ( !last_method_desc.equalsIgnoreCase(method_desc) || !last_cropname.equalsIgnoreCase(cropname) ) {
				// This is a new set of records...
				// First add the previous one if necessary...
				if ( !added && (cubc != null) ) {
					processor.findAndAddCUBlaneyCriddle ( cubc, true );
				}
				// Now start on the new instance...
				cubc = new StateCU_BlaneyCriddle (hbc.getCurve_type() );
				cubc.setName ( hbc.getCropname() );
				// TODO SAM 2007-03-04 Hard-coding is not ideal but this is not in HydroBase.
				// Set the ktsw value based on the CU method.  This is from Erin Wilson Jan 9, 2007 email...
				if ( StringUtil.startsWithIgnoreCase(method_desc,"BLANEY-CRIDDLE") ) {
					cubc.setKtsw(0);
				}
				else if ( StringUtil.startsWithIgnoreCase(method_desc,"ORIGINAL_BLANEY-CRIDDLE") ) {
					cubc.setKtsw(1);
				}
				else if ( StringUtil.startsWithIgnoreCase(method_desc,"BLANEY-CRIDDLE") &&
					(StringUtil.indexOfIgnoreCase(method_desc,"HIGH_ALT",0)>=0) ) {
					cubc.setKtsw(2);
				}
				else if ( StringUtil.startsWithIgnoreCase(method_desc,"ORIGINAL_BLANEY-CRIDDLE") &&
					(StringUtil.indexOfIgnoreCase(method_desc,"HIGH_ALT",0)>=0) ) {
					cubc.setKtsw(3);
				}
				else if ( StringUtil.startsWithIgnoreCase(method_desc,"POCHOP") ) {
					cubc.setKtsw(4);
				}
				nrec = 0;
				added = false;
			}
			cubc.setCurveValues ( nrec++, hbc.getCurve_value(), hbc.getCropgrowcoeff() );
			last_cropname = cropname;
			last_method_desc = method_desc;
		}
		// Need to add the last one...
		if ( !added && (cubc != null) ) {
			processor.findAndAddCUBlaneyCriddle ( cubc, true );
		}

		// Warn about identifiers that have been replaced in the processor list

		processor.warnAboutDataMatches ( this, true,
			processor.getStateCUBlaneyCriddleMatchList(), "CU Blaney-Criddle Crop Coefficients" );
	}
    catch ( Exception e ) {
        message = "Unexpected error reading Blaney-Criddle crop coefficients from HydroBase (" + e + ").";
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
	
	String BlaneyCriddleMethod = parameters.getValue ( "BlaneyCriddleMethod" );
		
	StringBuffer b = new StringBuffer ();

	if ( BlaneyCriddleMethod != null && BlaneyCriddleMethod.length() > 0 ) {
		b.append ( "BlaneyCriddleMethod=\"" + BlaneyCriddleMethod + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
