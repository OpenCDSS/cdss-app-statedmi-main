package DWR.DMI.StateDMI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.IO.AbstractCommand;
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
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

/**
<p>
This class initializes, checks, and runs the WriteProperty() command.
</p>
*/
public class WriteProperty_Command extends AbstractCommand implements Command, FileGenerator
{
	
/**
Values for Append parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public WriteProperty_Command ()
{	super();
	setCommandName ( "WriteProperty" );
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
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String OutputFile = parameters.getValue ( "OutputFile" );
	String PropertyName = parameters.getValue ( "PropertyName" );
	String Append = parameters.getValue ( "Append" );
	String warning = "";
	String routine = getCommandName() + ".checkCommandParameters";
	String message;

	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		message = "The output file: \"" + OutputFile + "\" must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify an output file." ) );
	}
	/* Don't check because ${} properties may be used
	 * TODO SAM 2008-07-31 Need to enable check in some form.
	else {
	    String working_dir = null;
		try {
		    Object o = processor.getPropContents ( "WorkingDir" );
			if ( o != null ) {
				working_dir = (String)o;
			}
		}
		catch ( Exception e ) {
			message = "Error requesting WorkingDir from processor.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Software error - report problem to support." ) );
		}
		// Expand for ${} properties...
		working_dir = StateDMICommandProcessorUtil.expandParameterValue(processor, this, working_dir);
		try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output parent directory does " +
				"not exist for the output file: \"" + adjusted_path + "\".";
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
			"\"\ncannot be adjusted to an absolute path using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that output file and working directory paths are compatible." ) );
		}
	}
	*/
		
	if ( (PropertyName == null) || (PropertyName.length() == 0) ) {
		message = "A property name must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify a property name." ) );
	}
	else {
		List valid_properties = new Vector(StateDMICommandProcessorUtil.getPropertyNameList(processor));
		int size = 0;
		if ( valid_properties != null ) {
			size = valid_properties.size();
		}
		boolean found = false;
		for ( int i = 0; i < size; i++ ) {
			if ( PropertyName.equalsIgnoreCase((String)valid_properties.get(i))) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			message = "The property name \"" + PropertyName + "\" is not valid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid property name (use command editor choice)." ) );
		}
	}
	
	if ( (Append != null) && (Append.length() != 0) && !Append.equalsIgnoreCase(_True) &&
			!Append.equalsIgnoreCase(_False)) {
		message="Append (" + Append + ") is not a valid value (True or False).";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify as true or false." ) );
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "PropertyName" );
	valid_Vector.add ( "Append" );
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
{	// The command will be modified if changed...
	return (new WriteProperty_JDialog ( parent, this )).ok();
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

// parseCommand is base class

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "WriteProperty_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	// Clear the output file
	
	setOutputFile ( null );
	
	// Check whether the processor wants output files to be created...

	CommandProcessor processor = getCommandProcessor();
	if ( !StateDMICommandProcessorUtil.getCreateOutput(processor) ) {
			Message.printStatus ( 2, routine,
			"Skipping \"" + toString() + "\" because output is not being created." );
	}

	PropList parameters = getCommandParameters();
	String OutputFile = parameters.getValue ( "OutputFile" );
	String PropertyName = parameters.getValue ( "PropertyName" );
	String Append = parameters.getValue ( "Append" );
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);

	// Get the property to output...
	
	Object Property_Object = null;
	try {
		Property_Object = processor.getPropContents ( PropertyName );
	}
	catch ( Exception e ) {
		message = "Error requesting property named \"" + PropertyName + "\" from processor.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Confirm that property name is valid - use the command editor." ) );
	}

	// Now try to write...

	String OutputFile_full = OutputFile;
	try {
		boolean Append_boolean = true;	// Default
		if ( (Append != null) && Append.equalsIgnoreCase(_False)) {
			Append_boolean = false;
		}
		// Convert to an absolute path...
		OutputFile_full = IOUtil.verifyPathForOS(
                IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
                		StateDMICommandProcessorUtil.expandParameterValue(processor, this, OutputFile) ) );
		// Open the file...
		PrintWriter fout = new PrintWriter ( new FileOutputStream ( OutputFile_full, Append_boolean ) );
		// Write the output (no output for now since it is mainly for testing)...
		fout.println ( PropertyName + "=\"" + Property_Object + "\"" );
		// Close the file...
		fout.close();
		// Save the output file name...
		setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Unexpected error writing property to file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE, message, "Check log file for details." ) );
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

/**
Return the string representation of the command.
@param parameters Command parameters as strings.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String OutputFile = parameters.getValue ( "OutputFile" );
	String PropertyName = parameters.getValue ( "PropertyName" );
	String Append = parameters.getValue ( "Append" );
	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (PropertyName != null) && (PropertyName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PropertyName=\"" + PropertyName + "\"" );
	}
	if ( (Append != null) && (Append.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Append=" + Append );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
