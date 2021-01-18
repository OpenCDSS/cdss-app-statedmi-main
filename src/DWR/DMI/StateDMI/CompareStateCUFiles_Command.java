// CompareStateCUFiles_Command - this class initializes, checks, and runs the CompareStateCUFiles() commands.

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
import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Util;
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
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the StateCU Compare*Files() commands.
It is an abstract base class that must be controlled via a derived class.
For example, the CompareCropPatternTSFiles() command extends this class in order to uniquely represent the command,
but much of the functionality is in the base class.
*/
public abstract class CompareStateCUFiles_Command extends AbstractCommand implements Command
{
	
/**
First input file that is processed by this command.
*/
private File __InputFile1_File = null;

/**
Second input file that is processed by this command.
*/
private File __InputFile2_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public CompareStateCUFiles_Command ()
{	super();
	setCommandName ( "Compare?StateCUFiles" );
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
{	String routine = "CompareStateCUFiles_Command.checkCommandParameters";
	String InputFile1 = parameters.getValue ( "InputFile1" );
	String InputFile2 = parameters.getValue ( "InputFile2" );
	String Precision = parameters.getValue ( "Precision" );
	
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (InputFile1 == null) || (InputFile1.length() == 0) ) {
		message = "The first input file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the first input file." ) );
	}
	else if ( InputFile1.indexOf("${") < 0 ){
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
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile1));
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
			message = "The first input file:\n" +
			"    \"" + InputFile1 +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that input file and working directory paths are compatible." ) );
		}
	}

	if ( (InputFile2 == null) || (InputFile2.length() == 0) ) {
		message = "The first input file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the first input file." ) );
	}
	else if ( InputFile2.indexOf("${") < 0 ){
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
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile2));
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
			message = "The first input file:\n" +
			"    \"" + InputFile2 +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that input file and working directory paths are compatible." ) );
		}
	}

	if ( (this instanceof CompareCropPatternTSFiles_Command) ) {
		if ( (Precision != null) && !Precision.isEmpty() && !StringUtil.isInteger(Precision) ) {
	        message = "The value for Precision (" + Precision + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify Precision as an integer" ) );
		}
	}

	// Check for invalid parameters...
	List<String> validList = new ArrayList<>();
	validList.add ( "InputFile1" );
	validList.add ( "InputFile2" );
	if ( this instanceof CompareCropPatternTSFiles_Command ) {
		validList.add ( "Precision" );
	}
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
	return (new CompareStateCUFiles_JDialog ( parent, this )).ok();
}

/**
Return the first input file read by this command.  This method is used internally.
*/
protected File getInputFile1 ()
{
	return __InputFile1_File;
}

/**
Return the second input file read by this command.  This method is used internally.
*/
protected File getInputFile2 ()
{
	return __InputFile2_File;
}

// Parent parseCommmand is used

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommand", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile1 = parameters.getValue ( "InputFile1" );
    String InputFile2 = parameters.getValue ( "InputFile2" );

    int precision = 3;
	if ( this instanceof CompareCropPatternTSFiles_Command ) {
		String Precision = parameters.getValue ( "Precision" );
		if ( (Precision != null) && !Precision.isEmpty() ) {
			precision = Integer.parseInt(Precision);
		}
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    String InputFile1_full = InputFile1;
    String InputFile2_full = InputFile2;
    try {
     	// Set the filenames
    	InputFile1_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile1) );
    	setInputFile1 ( new File(InputFile1_full) );
    	
       	if ( !IOUtil.fileExists(InputFile1_full) ) {
    		message = "First input file \"" + InputFile1_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(InputFile1_full) ) {
    		message = "First input file \"" + InputFile1_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}

    	InputFile2_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile2) );
    	setInputFile2 ( new File(InputFile2_full) );
    	
       	if ( !IOUtil.fileExists(InputFile2_full) ) {
    		message = "Second input file \"" + InputFile2_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(InputFile2_full) ) {
    		message = "Second input file \"" + InputFile2_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	
		if ( this instanceof CompareCropPatternTSFiles_Command ) {
	        Message.printStatus ( 2, routine, "Comparing StateCU CropPatternTS files \"" +
	        	InputFile1_full + "\" and \"" + InputFile2_full + "\"" );
		
			// Read the first file
			List<StateCU_CropPatternTS> cdsList1 = StateCU_CropPatternTS.readStateCUFile(InputFile1_full, null, null);

			// Read the second file
			List<StateCU_CropPatternTS> cdsList2 = StateCU_CropPatternTS.readStateCUFile(InputFile2_full, null, null);

			// Compare the two lists.

			StateCU_CropPatternTS cds2 = null;
			List<String> diffText = new ArrayList<>();
			for ( StateCU_CropPatternTS cds1 : cdsList1 ) {
				// Find the matching object in the second list
				int pos = StateCU_Util.indexOf(cdsList2, cds1.getID());
				if ( pos < 0 ) {
					cds2 = null;
				}
				else {
					cds2 = cdsList2.get(pos);
				}
				// The following handles nulls
				List<String> diffText1 = StateCU_CropPatternTS.compare(cds1, cds2, precision);
				diffText.addAll(diffText1);
			}
			
			if ( diffText.size() > 0 ) {
				message = "Have " + diffText.size() + " differences between \"" +
					InputFile1_full + "\" and \"" + InputFile2_full + "\"";
				Message.printWarning ( warning_level, 
					MessageUtil.formatMessageTag(command_tag, ++warning_count),
					routine, message );
				status.addToLog ( command_phase,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "See log file for details." ) );
				for ( String text : diffText ) {
					Message.printStatus(2, routine, text);
				}
			}
		}
		else if ( this instanceof CompareIrrigationPracticeTSFiles_Command ) {
	        Message.printStatus ( 2, routine, "Comparing StateCU IrrigationPracticeTS files \"" +
	        	InputFile1_full + "\" and \"" + InputFile2_full + "\"" );
		
			// Read the first file
			List<StateCU_IrrigationPracticeTS> ipyList1 = StateCU_IrrigationPracticeTS.readStateCUFile(InputFile1_full, null, null);

			// Read the second file
			List<StateCU_IrrigationPracticeTS> ipyList2 = StateCU_IrrigationPracticeTS.readStateCUFile(InputFile2_full, null, null);

			// Compare the two lists.

			StateCU_IrrigationPracticeTS ipy2 = null;
			List<String> diffText = new ArrayList<>();
			for ( StateCU_IrrigationPracticeTS ipy1 : ipyList1 ) {
				// Find the matching object in the second list
				int pos = StateCU_Util.indexOf(ipyList2, ipy1.getID());
				if ( pos < 0 ) {
					ipy2 = null;
				}
				else {
					ipy2 = ipyList2.get(pos);
				}
				// The following handles nulls
				List<String> diffText1 = StateCU_IrrigationPracticeTS.compare(ipy1, ipy2, precision);
				diffText.addAll(diffText1);
			}
			
			if ( diffText.size() > 0 ) {
				message = "Have " + diffText.size() + " differences between \"" +
					InputFile1_full + "\" and \"" + InputFile2_full + "\"";
				Message.printWarning ( warning_level, 
					MessageUtil.formatMessageTag(command_tag, ++warning_count),
					routine, message );
				status.addToLog ( command_phase,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "See log file for details." ) );
				for ( String text : diffText ) {
					Message.printStatus(2, routine, text);
				}
			}
		}

    	// Set the filename
    	setInputFile1 ( new File(InputFile1_full) );
    	setInputFile2 ( new File(InputFile2_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error comparing \"" + InputFile1_full + "\" and \"" + InputFile2_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
}

/**
Set the first input file that is read by this command.  This is only used internally.
*/
protected void setInputFile1 ( File file )
{
	__InputFile1_File = file;
}

/**
Set the second input file that is read by this command.  This is only used internally.
*/
protected void setInputFile2 ( File file )
{
	__InputFile2_File = file;
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

	String InputFile1 = parameters.getValue ( "InputFile1" );
	String InputFile2 = parameters.getValue ( "InputFile2" );
	String Precision = parameters.getValue ( "Precision" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile1 != null) && (InputFile1.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( ",");
			}
		b.append ( "InputFile1=\"" + InputFile1 + "\"" );
	}
	if ( (InputFile2 != null) && (InputFile2.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( ",");
			}
		b.append ( "InputFile2=\"" + InputFile2 + "\"" );
	}
	if ( this instanceof CompareCropPatternTSFiles_Command ) {
		if ( (Precision != null) && (Precision.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( ",");
			}
			b.append ( "Precision=" + Precision );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
