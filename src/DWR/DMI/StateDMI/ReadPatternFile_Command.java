// ReadPatternFile_Command - This class initializes, checks, and runs the ReadPatternFile() command.

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
import java.util.List;
import java.util.Vector;

//import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_TS;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
<p>
This class initializes, checks, and runs the ReadPatternFile() command.
</p>
*/
public class ReadPatternFile_Command extends AbstractCommand implements Command
{
	
/**
Input file that is read by this command.
*/
private File __InputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public ReadPatternFile_Command ()
{	super();
	setCommandName ( "ReadPatternFile" );
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
	String InputFile = parameters.getValue ( "InputFile" );
	String warning = "";
	String message;
	
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
   
	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "InputFile" );
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
	return (new ReadPatternFile_JDialog ( parent, this )).ok();
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
    String routine = getClass().getName() + ".runCommand", message;
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
    
    // Get the existing list of pattern time series.
    List patternList = null;
    try {
   		patternList = (List)processor.getPropContents ( "StateMod_PatternTSMonthly_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting pattern time series list from processor (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
   	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
    	
   	if ( !IOUtil.fileExists(InputFile_full) ) {
		message = "Input file \"" + InputFile_full + "\" does not exist.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        status.addToLog(commandPhase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
            	"Verify that the file exists and is readable."));
	}
	if ( !IOUtil.fileReadable(InputFile_full) ) {
		message = "Input file \"" + InputFile_full + "\" is not readable.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        status.addToLog(commandPhase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Verify that the file exists and is readable."));
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
     	// Clear the filename
    	setInputFile ( null );
 
    	Message.printStatus ( 2, routine, "Reading monthly pattern time series from \"" + InputFile_full +
    	"\"." );
    	// Read the fill pattern file.  Since multiple options are allowed,
    	// create a temporary list and then append to the main list...
    	List fill_pattern_ts = StateMod_TS.readPatternTimeSeriesList( InputFile_full, true );
    	if ( fill_pattern_ts == null ) {
    		message = "No pattern time series read from \"" + InputFile_full + "\" - will not fill with patterns.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the pattern file exists and contains pattern strings."));
    	}

    	int listsize = fill_pattern_ts.size();
    	Message.printStatus ( 2, routine,
    	"Read " + listsize + " pattern time series from \""+ InputFile_full + "\"" );
    	for ( int j = 0; j < listsize; j++ ) {
    		patternList.add( fill_pattern_ts.get(j) );
    	}

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

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
