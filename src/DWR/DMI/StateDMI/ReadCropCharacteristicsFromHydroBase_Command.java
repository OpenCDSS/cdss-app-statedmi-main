// ReadCropCharacteristicsFromHydroBase_Command - This class initializes, checks, and runs the ReadCropCharacteristicsFromHydroBase() command.

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
import DWR.DMI.HydroBaseDMI.HydroBase_Cropchar;
import DWR.StateCU.StateCU_CropCharacteristics;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
This class initializes, checks, and runs the ReadCropCharacteristicsFromHydroBase() command.
</p>
*/
public class ReadCropCharacteristicsFromHydroBase_Command 
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
public ReadCropCharacteristicsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadCropCharacteristicsFromHydroBase" );
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
	String CUMethod = parameters.getValue ( "CUMethod" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (CUMethod == null) || (CUMethod.length() == 0) ) {
		message = "A CU method must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the CU method to match." ) );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "CUMethod" );
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
	List CUMethod_List = new Vector();
	try {
		CUMethod_List = (List)processor.getPropContents("CUMethod_List");
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error getting CU method list - will not be listed in editor." );
	}
	return (new ReadCropCharacteristicsFromHydroBase_JDialog ( parent, this, CUMethod_List )).ok();
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
	String CUMethod = parameters.getValue ( "CUMethod" );
	
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
		// Remove all the elements for the Vector that tracks when identifiers
		// are read from more than one main source (e.g., CCH, HydroBase).
		// This is used to print a warning.
		processor.resetDataMatches ( processor.getStateCUCropCharacteristicsMatchList() );

		// Read from HydroBase...

		List cropchars = hbdmi.readCropcharListForMethodDesc(CUMethod,false);

		int size = 0;
		if ( cropchars != null ) {
			size = cropchars.size();
		}
		Message.printStatus ( 2, routine, "Read " + size + "HydroBase.cropchar records" );

		// Loop through the HydroBase objects and add new
		// StateCU_CropCharacteristics instances for each instance...

		StateCU_CropCharacteristics cch;
		HydroBase_Cropchar cropchar;
		String s = null;
		for (int i = 0; i < size; i++) {
			cropchar = (HydroBase_Cropchar)cropchars.get(i);
			cch = new StateCU_CropCharacteristics();
			cch.setName ( cropchar.getCropname() );
			cch.setID ( "" + cropchar.getIrrig_cropnum() );
			cch.setGdate1 ( cropchar.getPlantingmon() );
			cch.setGdate2 ( cropchar.getPlantingday() );
			cch.setGdate3 ( cropchar.getHarvestmon() );
			cch.setGdate4 ( cropchar.getHarvestday() );
			cch.setGdate5 ( cropchar.getDaystofullcover() );
			cch.setGdates ( cropchar.getLengthofseason() );
			cch.setTmois1 ( cropchar.getTempearlymoisture() );
			cch.setTmois2 ( cropchar.getTemplatemoisture() );
			cch.setMad ( cropchar.getMadlevel() );
			cch.setIrx ( cropchar.getInitialroot() );
			cch.setFrx ( cropchar.getMaxroot() );
			// HydroBase does not have AWC in the cropchar table - why would it!?
			cch.setApd ( cropchar.getMaxappdepth() );
			s = cropchar.getSpringfrostmethod();
			if ( s.indexOf("32") >= 0 ) {
				cch.setTflg1 ( 2 );
			}
			else if ( s.indexOf("28") >= 0 ) {
				cch.setTflg1 ( 1 );
			}
			else {
				// Assume default of "monthly-mean"...
				cch.setTflg1 ( 0 );
			}
			s = cropchar.getFallfrostmethod();
			if ( s.indexOf("32") >= 0 ) {
				cch.setTflg2 ( 2 );
			}
			else if ( s.indexOf("28") >= 0 ) {
				cch.setTflg2 ( 1 );
			}
			else {
				// Assume default of "monthly-mean"...
				cch.setTflg2 ( 0 );
			}
			cch.setCut2 ( cropchar.getDaysbetweencuts() );
			cch.setCut3 ( cropchar.getDaysbetweencuts() );
			// Replace or add in the __CUCropCharacteristics_Vector...
			processor.findAndAddCUCropCharacteristics ( cch, true );
		}

		// Warn about identifiers that have been replaced in the
		// __CUCropCharacteristics_Vector...

		processor.warnAboutDataMatches ( this, true,
			processor.getStateCUCropCharacteristicsMatchList(), "CU Crop Characteristics" );
	}
    catch ( Exception e ) {
        message = "Unexpected error reading crop characteristics from HydroBase (" + e + ").";
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
	
	String CUMethod = parameters.getValue ( "CUMethod" );
		
	StringBuffer b = new StringBuffer ();

	if ( CUMethod != null && CUMethod.length() > 0 ) {
		b.append ( "CUMethod=\"" + CUMethod + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
