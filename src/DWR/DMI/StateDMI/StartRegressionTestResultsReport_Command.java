package DWR.DMI.StateDMI;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the StartRegressionTestResultsReport() command.
</p>
*/
public class StartRegressionTestResultsReport_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Strings used for parameter values, here and in the editor dialog.
*/
protected final String _Date = "Date";
protected final String _DateTime = "DateTime";

/**
Output (log) file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public StartRegressionTestResultsReport_Command ()
{	super();
	setCommandName ( "StartRegressionTestResultsReport" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag,int warning_level )
throws InvalidCommandParameterException
{	String routine = getCommandName() + "_checkCommandParameters";
	String OutputFile = parameters.getValue ( "OutputFile" );
	//String Suffix = parameters.getValue ( "Suffix" );
	String working_dir = null;
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	try { Object o = processor.getPropContents ( "WorkingDir" );
		// Working directory is available so use it...
		if ( o != null ) {
			working_dir = (String)o;
			if ( working_dir.equals("") ) {
				working_dir = null;	// Not available.
			}
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
		// A null output file means that the current file should be re-opened.
		if ( (OutputFile != null) && (working_dir != null) ) {
			String adjusted_path = IOUtil.adjustPath(working_dir,OutputFile);
			File f = new File(adjusted_path);
			File f2 = new File(f.getParent());
			if (!f2.exists()) {
				message = "The output file parent folder \"" + f.getParent() +
				"\" does not exist: \"" + adjusted_path + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the ouput file folder exists." ) );
			}
			f = null;
			f2 = null;
		}
	}
	catch ( Exception e ) {
		// Print a stack trace so the output shows up somewhere because the log file
		// may not be used.
		if ( Message.isDebugOn ) {
			e.printStackTrace();
		}
		message = "\nThe output file \"" + OutputFile +
		"\" cannot be adjusted to an absolute path using the working directory \"" +
		working_dir + "\".";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the path information is consistent." ) );
	}
    /*
	if (	(Suffix != null) && (Suffix.length() != 0) &&
		!Suffix.equalsIgnoreCase(_Date) &&
		!Suffix.equalsIgnoreCase(_DateTime) ) {
		message = "The suffix must be blank, \"" + _Date + "\", or \"" + _DateTime + "\".";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Change the suffix to an allowable value." ) );
	}
    */
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "Outputfile" );
	//valid_Vector.add ( "Suffix" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );
	
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine,
		warning );
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
{	// The command will be modified if changed...
	return (new StartRegressionTestResultsReport_JDialog ( parent, this )).ok();
}

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
private File getOutputFile ()
{
	return __OutputFile_File;
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommand ( int command_number )
throws CommandWarningException, CommandException
{	String routine = getClass().getName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	String OutputFile_full = null;	// File with path, used below and in final catch.
    String Suffix = null;   // For now do not allow a suffix
	try {
		String OutputFile = parameters.getValue ( "OutputFile" );
		//String Suffix = parameters.getValue ( "Suffix" );
		// Open a new log file.  Append the suffix if it has been specified.
		if ( (Suffix == null) || (Suffix.length() == 0) ) {
			// Make sure to do nothing below...
			Suffix = "";
		}
		else if ( Suffix.equalsIgnoreCase(_Date) ) {
			DateTime d = new DateTime (	DateTime.DATE_CURRENT );
			Suffix = "." +
				StringUtil.formatString(d.getYear(),"%04d") +
				StringUtil.formatString(d.getMonth(),"%02d") +
				StringUtil.formatString(d.getDay(),"%02d");
		}
		else if ( Suffix.equalsIgnoreCase(_DateTime) ) {
			DateTime d = new DateTime (	DateTime.DATE_CURRENT );
			// Make sure there is no space and can't use
			// colons here because Windows uses that for
			// drive letters...
		Suffix = "." + 
				StringUtil.formatString(d.getYear(),"%04d") +
				StringUtil.formatString(d.getMonth(),"%02d") +
				StringUtil.formatString(d.getDay(),"%02d") + "_" +
				StringUtil.formatString(d.getHour(),"%02d") +
				StringUtil.formatString(d.getMinute(),"%02d") +
				StringUtil.formatString(d.getSecond(),"%02d");
		}
		if ( Suffix.length() > 0 ) {
			String ext = IOUtil.getFileExtension (OutputFile );
			if ( ext == null ) {
			// Just append...
				OutputFile = OutputFile + Suffix;
			}
			else {	// Insert before the last extension...
				OutputFile = OutputFile.substring(0,OutputFile.length()-ext.length()-1)+ Suffix + "." + ext;
			}
		}
		OutputFile_full = IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile);
		StateDMICommandProcessorUtil.openNewRegressionTestReportFile ( OutputFile_full, false );
		setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Unexpected error (re)starting the regression test report file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Check the log file or command window for details." ) );
		throw new CommandException ( message );
	}
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}


/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
	__OutputFile_File = file;
}

// Can rely on base class for toString().

}
