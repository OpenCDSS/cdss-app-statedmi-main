package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_PenmanMonteith;

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
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the Write*ToStateCU() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the WriteClimateStationsToStateCU()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class WriteToStateCU_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
Possible values for boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public WriteToStateCU_Command ()
{	super();
	setCommandName ( "Write?ToStateCU" );
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
	String Version = parameters.getValue ( "Version" );
	String Precision = parameters.getValue ( "Precision" );
	String AutoAdjust = parameters.getValue ( "AutoAdjust" );
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
	
	if ( this instanceof WriteCULocationsToStateCU_Command ||
		this instanceof WriteCropCharacteristicsToStateCU_Command ||
		this instanceof WriteBlaneyCriddleToStateCU_Command) {
		if ( (Version != null) && (Version.length() != 0) && !StringUtil.isInteger(Version) ) {
	        message = "The value for Version (" + Version + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the StateCU version as an integer." ) );
		}
	}
	
	if ( (this instanceof WriteBlaneyCriddleToStateCU_Command) ||
		(this instanceof WritePenmanMonteithToStateCU_Command) ) {
		if ( (Precision != null) && (Precision.length() != 0) && !StringUtil.isInteger(Precision) ) {
	        message = "The value for Precision (" + Precision + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the precision as an integer." ) );
		}
	}
	
	if ( this instanceof WriteCropCharacteristicsToStateCU_Command ) {
		if ( (AutoAdjust != null) && (AutoAdjust.length() != 0) &&
			!AutoAdjust.equals(_False) && !AutoAdjust.equals(_True) ) {
	        message = "The value for AutoAdjust (" + AutoAdjust + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify AutoAdjust as " + _False + " (default) or " + _True ) );
		}
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
	if ( this instanceof WriteCULocationsToStateCU_Command ||
		this instanceof WriteCropCharacteristicsToStateCU_Command ||
		this instanceof WriteBlaneyCriddleToStateCU_Command) {
		valid_Vector.add ( "Version" );
	}
	if ( (this instanceof WriteBlaneyCriddleToStateCU_Command) ||
		(this instanceof WritePenmanMonteithToStateCU_Command) ) {
		valid_Vector.add ( "Precision" );
	}
	if ( this instanceof WriteCropCharacteristicsToStateCU_Command ) {
		valid_Vector.add ( "AutoAdjust" );
	}
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
	return (new WriteToStateCU_JDialog ( parent, this )).ok();
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
    String Version = parameters.getValue ( "Version" );
    String Precision = parameters.getValue ( "Precision" );
    Integer Precision_Integer = null;
    if ( Precision != null ) {
    	Precision_Integer = Integer.parseInt(Precision);
    }
    String AutoAdjust = parameters.getValue ( "AutoAdjust" );
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
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
		if ( this instanceof WriteClimateStationsToStateCU_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateCU climate stations file \"" +
	        	OutputFile_full + "\"" );
			StateCU_ClimateStation.writeStateCUFile(OutputFile_prevFull, OutputFile_full,
				processor.getStateCUClimateStationList(), OutputComments_List );
		}
		else if ( this instanceof WriteCropCharacteristicsToStateCU_Command ) {
			Message.printStatus ( 2, routine, "Writing StateCU crop characteristics file \"" +
			        	OutputFile_full + "\"" );
			PropList writeProps = null;
			if ( Version != null ) {
				writeProps = new PropList ( "writeCropCharacteristicsToStateCU" );
				writeProps.add ( "Version=" + Version );
			}
			if ( AutoAdjust != null ) {
				if ( writeProps == null ) {
					writeProps = new PropList ( "writeCropCharacteristicsToStateCU" );
				}
				writeProps.add ( "AutoAdjust=" + AutoAdjust );
			}
			StateCU_CropCharacteristics.writeStateCUFile(OutputFile_prevFull, OutputFile_full,
				processor.getStateCUCropCharacteristicsList(), OutputComments_List, writeProps );
		}
		else if ( this instanceof WriteBlaneyCriddleToStateCU_Command ) {
			PropList writeProps = new PropList ( "writeBlaneyCriddleToStateCU" );
			if ( Precision != null ) {
				writeProps.set ( "Precision", Precision );
			}
			if ( Version != null) {
				writeProps.add( "Version=" + Version );
			}
			StateCU_BlaneyCriddle.writeStateCUFile(OutputFile_prevFull, OutputFile_full,
				processor.getStateCUBlaneyCriddleList(), OutputComments_List, writeProps );
		}
		else if ( this instanceof WritePenmanMonteithToStateCU_Command ) {
			PropList writeProps = new PropList ( "writePenmanMonteithToStateCU" );
			StateCU_PenmanMonteith.writeStateCUFile(OutputFile_prevFull, OutputFile_full,
				processor.getStateCUPenmanMonteithList(), OutputComments_List, Precision_Integer );
		}
		else if ( this instanceof WriteCULocationsToStateCU_Command ) {
			PropList writeProps = null;
			if ( Version != null ) {
				writeProps = new PropList ( "WriteCULocationsToStateCU" );
				writeProps.add ( "Version=" + Version );
			}
			StateCU_Location.writeStateCUFile(OutputFile_prevFull, OutputFile_full,
				processor.getStateCULocationList(), OutputComments_List, writeProps );
		}
    	// Set the filename for the FileGenerator interface
    	setOutputFile ( new File(OutputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE, message, "See log file for details." ) );
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
	String Version = parameters.getValue ( "Version" );
	String Precision = parameters.getValue ( "Precision" );
	String AutoAdjust = parameters.getValue ( "AutoAdjust" );
	String WriteHow = parameters.getValue ( "WriteHow" );
	String Delimiter = parameters.getValue ( "Delimiter" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (WriteHow != null) && (WriteHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteHow=" + WriteHow );
	}
	if ( (this instanceof WriteCULocationsToStateCU_Command) ||
		(this instanceof WriteCropCharacteristicsToStateCU_Command)	||
		(this instanceof WriteBlaneyCriddleToStateCU_Command)) {
		if ( (Version != null) && (Version.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Version=" + Version );
		}
	}
	if ( (this instanceof WriteBlaneyCriddleToStateCU_Command) ||
		(this instanceof WritePenmanMonteithToStateCU_Command) ) {
		if ( (Precision != null) && (Precision.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Precision=" + Precision );
		}
	}
	if ( this instanceof WriteCropCharacteristicsToStateCU_Command	) {
		if ( (AutoAdjust != null) && (AutoAdjust.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "AutoAdjust=" + AutoAdjust );
		}
	}
	if ( (Delimiter != null) && (Delimiter.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Delimiter=" + Delimiter );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}