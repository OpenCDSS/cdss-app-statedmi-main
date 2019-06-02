// AppendNetwork_Command - This class initializes, checks, and runs the AppendNetwork() command.

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

import java.io.File;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_NodeNetwork_AppendHowType;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the AppendNetwork() command.
*/
public class AppendNetwork_Command extends AbstractCommand implements Command
{
	
/**
Input file that is read by this command.
*/
private File __InputFile_File = null;

/**
Indicates how the network should be appended.
*/
StateMod_NodeNetwork_AppendHowType __appendHow = null;

/**
ScaleXY
*/
private Double __scaleXY = null;

/**
ShiftX
*/
private Double __shiftX = null;

/**
ShiftY
*/
private Double __shiftY = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public AppendNetwork_Command ()
{	super();
	setCommandName ( "AppendNetwork" );
}

/**
Read StateMod stream network and append to existing network.
*/
private int appendNetwork ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, StateMod_NodeNetwork networkPrev,
		StateMod_NodeNetwork_AppendHowType appendHowType,
		String existingDownstreamNodeID, String appendedUpstreamNodeID,
		Double scaleXY, Double shiftX, Double shiftY )
throws Exception
{	String routine = "AppendNetwork_Command.appendNetwork";
	// Get the HydroBase DMI, needed by the network...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		String message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Verify that HydroBase is accessible." ) );
	}
	
	// Read the network...

	StateMod_NodeNetwork net = StateMod_NodeNetwork.readStateModNetworkFile(
		InputFile_full, new StateDMI_NodeDataProvider(hbdmi), true );
	
	// Append to the previous network if requested...
	
	if ( networkPrev != null ) {
		StateMod_NodeNetwork mergedNetwork = networkPrev.append ( net, appendHowType, existingDownstreamNodeID,
			appendedUpstreamNodeID, scaleXY, shiftX, shiftY );
		// Network will already be managed by StateDMI so no reason to set again
		processor.setPropContents ( "StateMod_Network", mergedNetwork );
	}
	else {
		// Set the new network in the processor.
		processor.setPropContents ( "StateMod_Network", net );
	}
	
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
{	String routine = "AppendNetwork_Command.checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String AppendHow = parameters.getValue ( "AppendHow" );
	String ExistingDownstreamNodeID = parameters.getValue ( "ExistingDownstreamNodeID" );
	String AppendedUpstreamNodeID = parameters.getValue ( "AppendedUpstreamNodeID" );
	String ScaleXY = parameters.getValue ( "ScaleXY" );
	String ShiftXY = parameters.getValue ( "ShiftXY" );
	String warning = "";
	String message;
	
    __scaleXY = null;
    __shiftX = null;
    __shiftY = null;
    __appendHow = null;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (InputFile == null) || (InputFile.length() == 0) ) {
		message = "The input file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an input file." ) );
	}
	else {
		String working_dir = null;		
			try {
				Object o = processor.getPropContents ( "WorkingDir" );
				// Working directory is available so use it...
				if ( o != null ) {
					working_dir = (String)o;
				}
			}
			catch ( Exception e ) {
				message = "Error requesting WorkingDir from processor.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Software error - report to support." ) );
			}
	
		try {
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile));
			File f = new File ( adjustedPath );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The input file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the input directory." ) );
			}
			else if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The input file:\n" +
			"    \"" + InputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that input file and working directory paths are compatible." ) );
		}
	}
	
	if ( (AppendHow == null) || (AppendHow.length() == 0) ) {
		message = "The AppendHow parameter must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the AppendHow parameter." ) );
	}
	else {
		__appendHow = StateMod_NodeNetwork_AppendHowType.valueOfIgnoreCase(AppendHow);
		if ( (__appendHow == null) ||
			((__appendHow != StateMod_NodeNetwork_AppendHowType.ADD_UPSTREAM_OF_DOWNSTREAM) &&
			(__appendHow != StateMod_NodeNetwork_AppendHowType.REPLACE_UPSTREAM_OF_DOWNSTREAM)) ) {
			message = "The AppendHow value (" + AppendHow + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify AppendHow as " +
					StateMod_NodeNetwork_AppendHowType.ADD_UPSTREAM_OF_DOWNSTREAM + " or " +
					StateMod_NodeNetwork_AppendHowType.REPLACE_UPSTREAM_OF_DOWNSTREAM + ".") );
		}
	}
	if ( (ExistingDownstreamNodeID == null) || (ExistingDownstreamNodeID.length() == 0) ) {
		message = "The ExistingDownstreamNodeID value is required.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify ExistingDownstreamNodeID as a node identifier in the existing network.") );
	}
	if ( (AppendedUpstreamNodeID == null) || (AppendedUpstreamNodeID.length() == 0) ) {
		message = "The AppendedUpstreamNodeID value is required.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify AppendedUpstreamNodeID as a node identifier in the appended network.") );
	}
		
	if ( (ScaleXY != null) && (ScaleXY.length() > 0) ) {
		if ( !StringUtil.isDouble(ScaleXY) ) {
			message = "The ScaleXY value (" + ScaleXY + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify ScaleXY as a number.") );
		}
		else {
			__scaleXY = new Double(ScaleXY);
		}
	}
	
	if ( (ShiftXY != null) && (ShiftXY.length() > 0) ) {
		// Expect X and Y values separated by comma
		String[] shiftParts = ShiftXY.split(",");
		if ( shiftParts.length != 2 ) {
			message = "The ShiftXY value (" + ShiftXY + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify ScaleXY as shiftX,shiftY numbers.") );
		}
		else {
			if ( !StringUtil.isDouble(shiftParts[0].trim()) ) {
				message = "The ShiftXY value for X (" + shiftParts[0].trim() + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify ScaleXY as shiftX,shiftY numbers.") );
			}
			else {
				__shiftX = new Double(shiftParts[0].trim());
			}
			if ( !StringUtil.isDouble(shiftParts[1].trim()) ) {
				message = "The ShiftXY value for Y (" + shiftParts[1].trim() + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify ScaleXY as shiftX,shiftY numbers.") );
			}
			else {
				__shiftY = new Double(shiftParts[1].trim());
			}
		}
	}
    
	// Check for invalid parameters...
	Vector<String> valid_Vector = new Vector<String>(6);
	valid_Vector.add ( "InputFile" );
	valid_Vector.add ( "AppendHow" );
	valid_Vector.add ( "ExistingDownstreamNodeID" );
	valid_Vector.add ( "AppendedUpstreamNodeID" );
	valid_Vector.add ( "ScaleXY" );
	valid_Vector.add ( "ShiftXY" );

    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new AppendNetwork_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getInputFile ()
{
	return __InputFile_File;
}

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" );
    String ExistingDownstreamNodeID = parameters.getValue ( "ExistingDownstreamNodeID");
    String AppendedUpstreamNodeID = parameters.getValue ( "AppendedUpstreamNodeID");
    
    // Get the network if appending
    StateMod_NodeNetwork network = null;
    if ( __appendHow != null ) {
        try {
       		network = (StateMod_NodeNetwork)processor.getPropContents ( "StateMod_Network" );
        }
        catch ( Exception e ) {
            Message.printWarning ( log_level, routine, e );
            message = "Error requesting network to process for append.";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report to software support.  See log file for details." ) );
        }
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    String InputFile_full = InputFile;
    try {
     	// Clear the filename
    	setInputFile ( new File(InputFile_full) );
    	InputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
    	
       	if ( !IOUtil.fileExists(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(commandPhase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(commandPhase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
		warning_count = appendNetwork ( processor, warning_level, warning_count, command_tag,
			status, commandPhase, InputFile_full, network, __appendHow,
			ExistingDownstreamNodeID, AppendedUpstreamNodeID, __scaleXY, __shiftX, __shiftY );
    	// Set the filename
    	setInputFile ( new File(InputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading file \"" + InputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the input file that is read by this command.  This is only used internally.
*/
protected void setInputFile ( File file )
{
	__InputFile_File = file;
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

	String InputFile = parameters.getValue ( "InputFile" );
	String AppendHow = parameters.getValue ( "AppendHow" );
	String ExistingDownstreamNodeID = parameters.getValue ( "ExistingDownstreamNodeID" );
	String AppendedUpstreamNodeID = parameters.getValue ( "AppendedUpstreamNodeID" );
	String ScaleXY = parameters.getValue ( "ScaleXY" );
	String ShiftXY = parameters.getValue ( "ShiftXY" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (AppendHow != null) && (AppendHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AppendHow=" + AppendHow );
	}
	if ( (ExistingDownstreamNodeID != null) && (ExistingDownstreamNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExistingDownstreamNodeID=\"" + ExistingDownstreamNodeID + "\"");
	}
	if ( (AppendedUpstreamNodeID != null) && (AppendedUpstreamNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AppendedUpstreamNodeID=\"" + AppendedUpstreamNodeID + "\"");
	}
	if ( (ScaleXY != null) && (ScaleXY.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ScaleXY=" + ScaleXY);
	}
	if ( (ShiftXY != null) && (ShiftXY.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ShiftXY=\"" + ShiftXY + "\"");
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
