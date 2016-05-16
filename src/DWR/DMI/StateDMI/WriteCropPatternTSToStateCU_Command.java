package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_DataSet;

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
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the WriteCropPatternToStateCU() command.
*/
public class WriteCropPatternTSToStateCU_Command extends AbstractCommand implements Command, FileGenerator
{
	
/**
Values for Version parameter.
*/
protected final String _10 = "10";

/**
Values for boolean parameters.
*/
protected final String _True = "True";
protected final String _False = "False";

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
	
/**
Constructor.
*/
public WriteCropPatternTSToStateCU_Command ()
{	super();
	setCommandName ( "WriteCropPatternTSToStateCU" );
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
{	String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String Version = parameters.getValue ( "Version" );
	String WriteCropArea = parameters.getValue ( "WriteCropArea" );
	String WriteOnlyTotal = parameters.getValue ( "WriteOnlyTotal" );
	String WriteHow = parameters.getValue ( "WriteHow" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		message = "The output file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an output file." ) );
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
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the output directory." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The output file:\n" +
			"    \"" + OutputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}
	
	if ( (OutputStart != null) && (OutputStart.length() != 0) && !StringUtil.isInteger(OutputStart) ) {
        message = "The value for the output start (" + OutputStart + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output start as blank or an integer." ) );
	}
	
	if ( (OutputEnd != null) && (OutputEnd.length() != 0) && !StringUtil.isInteger(OutputEnd) ) {
        message = "The value for the output end (" + OutputEnd + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output end as blank or an integer." ) );
	}
	
	if ( (Version != null) && (Version.length() != 0) && !Version.equals(_10) ) {
        message = "The value for Version (" + Version + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify Version as blank (default) or " + _10 ) );
	}
	
	if ( (WriteCropArea != null) && (WriteCropArea.length() != 0) &&
		!WriteCropArea.equals(_False) && !WriteCropArea.equals(_True) ) {
        message = "The value for WriteCropArea (" + WriteCropArea + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteCropArea as " + _False + " or " + _True + " (default).") );
	}
	
	if ( (WriteOnlyTotal != null) && (WriteOnlyTotal.length() != 0) &&
		!WriteOnlyTotal.equals(_False) && !WriteOnlyTotal.equals(_True) ) {
        message = "The value for WriteOnlyTotal (" + WriteOnlyTotal + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteHow as " + _False + " (default) or " + _True ) );
	}
    
	if ( (WriteHow != null) && (WriteHow.length() != 0) &&
		!WriteHow.equals(_OverwriteFile) && !WriteHow.equals(_UpdateFile) ) {
        message = "The value for WriteHow (" + WriteHow + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteHow as " + _OverwriteFile + " (default) or " + _UpdateFile ) );
	}

	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "OutputStart" );
	valid_Vector.add ( "OutputEnd" );
	valid_Vector.add ( "Version" );
	valid_Vector.add ( "WriteCropArea" );
	valid_Vector.add ( "WriteOnlyTotal" );
	valid_Vector.add ( "WriteHow" );
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
	return (new WriteCropPatternTSToStateCU_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the list of files that were created by this command.
*/
public List getGeneratedFileList ()
{
	List list = new Vector();
	if ( getOutputFile() != null ) {
		list.add ( getOutputFile() );
	}
	return list;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getOutputFile ()
{
	return __OutputFile_File;
}

// The following is expected to be called by the derived classes.

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
    
	// Check whether the processor wants output files to be created...

    CommandPhaseType command_phase = CommandPhaseType.RUN;
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	if ( !StateDMICommandProcessorUtil.getCreateOutput(processor) ) {
		Message.printStatus ( 2, routine,
		"Skipping \"" + toString() + "\" because output is not being created." );
	}
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    
    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
    String OutputStart = parameters.getValue ( "OutputStart" );
    String OutputEnd = parameters.getValue ( "OutputEnd" );
    String Version = parameters.getValue ( "Version" );
    String WriteCropArea = parameters.getValue ( "WriteCropArea" );
    if ( (WriteCropArea == null) || WriteCropArea.equals("") ) {
    	WriteCropArea = _True; // Default
    }
	String WriteOnlyTotal = parameters.getValue ( "WriteOnlyTotal" );
    if ( (WriteOnlyTotal == null) || WriteOnlyTotal.equals("") ) {
    	WriteOnlyTotal = _False; // Default
    }
    String RecomputeVersion10Acreage = parameters.getValue ( "RecomputeVersion10Acreage" );
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( (WriteHow != null) && WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
    }

    String OutputFile_full = OutputFile;
    try {
        // Get the comments to add to the top of the file.

        List OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
            if ( o != null ) {
                OutputComments_List = (List)o;
            }
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
            Message.printWarning(3, routine, message );
            Message.printWarning(3, routine, e );
        }
        
        // Get default dates from processor.
    	// Get the input needed to process the file...
    	DateTime OutputStart_DateTime= (DateTime)processor.getPropContents("OutputStart");
    	DateTime OutputEnd_DateTime = (DateTime)processor.getPropContents("OutputEnd");
    	if ( OutputStart != null ) {
    		OutputStart_DateTime = new DateTime(DateTime.PRECISION_YEAR);
    		OutputStart_DateTime.setYear( Integer.parseInt(OutputStart));
    	}
    	if ( OutputEnd != null ) {
    		OutputEnd_DateTime = new DateTime(DateTime.PRECISION_YEAR);
    		OutputEnd_DateTime.setYear( Integer.parseInt(OutputEnd));
    	}
        
    	// Clear the filename for the FileGenerator interface
    	setOutputFile ( new File(OutputFile_full) );
    	OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );
    	String OutputFile_prevFull = null;
    	// Set previous filename to indicate whether update should occur.
    	if ( update ) {
    		OutputFile_prevFull = OutputFile_full;
    	}
    	else {
    		OutputFile_prevFull = null;
    	}
    	
    	// Check the Crop Pattern TS data (output later in check file)
    	// FIXME SAM 2009-04-27 Remove when finished updating checks
    	//processor.runStateCUDataCheck( StateCU_DataSet.COMP_CROP_PATTERN_TS_YEARLY );
    	
    	PropList writeProps = new PropList ( "writeCropPatternTSToStateCU" );
    	writeProps.set ( "WriteCropArea", WriteCropArea );

    	if ( Version != null ) {
    		writeProps.add ( "Version=" + Version );
    	}
    	// TODO SAM 2008-12-29 This seems to always be null given legacy code - is it intended for
    	// testing only?
    	if ( RecomputeVersion10Acreage != null ) {
    		writeProps.add ( "RecomputeVersion10Acreage=" + RecomputeVersion10Acreage );
    	}

    	if ( WriteOnlyTotal != null ) {
    		// Only specify as a parameter if not null since default
    		// behavior should be OK...
    		writeProps.set ( "WriteOnlyTotal", WriteOnlyTotal );
    	}

		Message.printStatus ( 2, routine,
		"Writing (updating) CU Crop Pattern TS to file: \"" + OutputFile_full + "\"");
		StateCU_CropPatternTS.writeStateCUFile ( OutputFile_prevFull, OutputFile_full,
			(List)processor.getPropContents("StateCU_CropPatternTS_List"),
			OutputComments_List, OutputStart_DateTime, OutputEnd_DateTime, writeProps );

    	// Set the filename for the FileGenerator interface
    	setOutputFile ( new File(OutputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
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
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFile ( File file )
{
	__OutputFile_File = file;
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

	String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String WriteCropArea = parameters.getValue ( "WriteCropArea" );
	String WriteOnlyTotal = parameters.getValue ( "WriteOnlyTotal" );
	String Version = parameters.getValue ( "Version" );
	String WriteHow = parameters.getValue ( "WriteHow" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputStart=\"" + OutputStart + "\"");
	}
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputEnd=\"" + OutputEnd + "\"");
	}
	if ( (WriteCropArea != null) && (WriteCropArea.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteCropArea=" + WriteCropArea );
	}
	if ( (WriteOnlyTotal != null) && (WriteOnlyTotal.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteOnlyTotal=" + WriteOnlyTotal );
	}
	if ( (Version != null) && (Version.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Version=" + Version );
	}
	if ( (WriteHow != null) && (WriteHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteHow=" + WriteHow );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}