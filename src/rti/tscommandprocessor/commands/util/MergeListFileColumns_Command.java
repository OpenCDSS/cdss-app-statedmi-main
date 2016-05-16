package rti.tscommandprocessor.commands.util;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.List;
import java.util.Vector;

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
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;

/**
<p>
This class initializes, checks, and runs the MergeListFileColumns() command.
</p>
*/
public class MergeListFileColumns_Command extends AbstractCommand implements Command, FileGenerator
{

// Columns as integer array, filled in checkCommandParameters() and used in runCommand.
private int [] __Columns_intArray = new int[0];

/**
Format used for the merged columns, set up in checkCommandParameters().
*/
String __SimpleMergeFormat2 = "";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public MergeListFileColumns_Command ()
{	super();
	setCommandName ( "MergeListFileColumns" );
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
{	String ListFile = parameters.getValue ( "ListFile" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Columns = parameters.getValue ( "Columns" );
	String NewColumnName = parameters.getValue ( "NewColumnName" );
	String SimpleMergeFormat = parameters.getValue ( "SimpleMergeFormat" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	String working_dir = null;
	CommandProcessor processor = getCommandProcessor();
	
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
                message, "Report the problem to software support." ) );
	}
	
	if ( ListFile.length() == 0 ) {
        message = "The input (list) file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify an existing input file." ) );
	}
	else {
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, ListFile) );
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The input (list) file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the input file exists - may be OK if created at run time." ) );
			}
			f = null;
		}
		catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + ListFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that input file and working directory paths are compatible." ) );
		}
	}

	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory for the file does not exist, warn the user...
	if ( OutputFile.length() == 0 ) {
        message = "The output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify an output file." ) );
	}
	else {
        try {
        	String adjusted_path = IOUtil.adjustPath ( working_dir, OutputFile);
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
                message = "The parent directory for the output file does not exist:\n" +
                "    " + f.getParent() + ".";
				warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Create the output folder." ) );
			}
			f = null;
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

	if ( (Columns == null) || (Columns.length() == 0) ) {
        message = "One or more columns with values 1+ must be specified";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the columns to merge." ) );
	}
	else {
		// Check for integers...
		List<String> v = StringUtil.breakStringList ( Columns, ",", 0 );
		String token;
		if ( v == null ) {
            message = "One or more columns with value 1+ must be specified";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the columns to merge." ) );
		}
		else {
			int size = v.size();
			__Columns_intArray = new int[size];
			for ( int i = 0; i < size; i++ ) {
				token = v.get(i);
				if ( !StringUtil.isInteger(token) ) {
                    message = "Column \"" + token + "\" is not a number";
					warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the column as an integer >= 1." ) );
				}
				else {
					int column = Integer.parseInt(token);
					if ( column < 1 ) {
	                    message = "Column \"" + token + "\" is < 1";
						warning += "\n" + message;
	                    status.addToLog ( CommandPhaseType.INITIALIZATION,
	                        new CommandLogRecord(CommandStatusType.FAILURE,
	                            message, "Specify the column as an integer >= 1." ) );
					}
					else {
	                    // Decrement by one to make zero-referenced.
						__Columns_intArray[i] = column - 1;
					}
				}
			}
		}
	}
 
	if ( (NewColumnName == null) || (NewColumnName.length() == 0) ) {
        message = "A new column name must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the new column name." ) );
	}

	String token;
	__SimpleMergeFormat2 = "";	// Initial value.
	if ( (SimpleMergeFormat == null) || (SimpleMergeFormat.length() == 0) ) {
		// Add defaults (treat as strings)...
		for ( int i = 0; i < __Columns_intArray.length; i++ ) {
			__SimpleMergeFormat2 += "%s";
		}
	}
	else {
		List v = StringUtil.breakStringList (	SimpleMergeFormat, ",", 0 );
		int size = v.size();
		if ( size != __Columns_intArray.length ) {
			message = "The number of specifiers in the merge format (" + SimpleMergeFormat +
			") does not match the number of columns (" + __Columns_intArray.length;
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the number of format specifiers matches the columns to merge." ) );
		}
		else {
			for ( int i = 0; i < size; i++ ) {
				token = (String)v.get(i);
				if ( !StringUtil.isInteger(token) ) {
					message = "The format specifier \"" + token + "\" is not an integer";
					warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the format specifier as an integer." ) );
				}
				else {
					if ( token.startsWith("0") ) {
						// TODO SAM 2005-11-18 Need to enable 0 for %s in StringUtil. Assume integers...
						__SimpleMergeFormat2 += "%" + token + "d";
					}
					else {
                        __SimpleMergeFormat2 += "%" + token+ "." + token + "s";
					}
				}
			}
		}
	}
    
	//  Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ListFile" );
    valid_Vector.add ( "OutputFile" );
    valid_Vector.add ( "Columns" );
    valid_Vector.add ( "NewColumnName" );
    valid_Vector.add ( "SimpleMergeFormat" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );    

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new MergeListFileColumns_JDialog (
		parent, this,
		false	// Not runnable
		)).ok();
}

/**
Edit and optionally the run command from the editor.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editRunnableCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new MergeListFileColumns_JDialog (
		parent, this,
		true	// Runnable
		)).ok();
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

// Use base class parseCommand()

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = "mergeListFileColumns_Command.runCommand",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    
	// Clear the output file
	
	setOutputFile ( null );

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

	String ListFile = parameters.getValue ( "ListFile" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String NewColumnName = parameters.getValue ( "NewColumnName" );

	String ListFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),ListFile) );
	if ( !IOUtil.fileExists(ListFile_full) ) {
		message += "\nThe list file \"" + ListFile_full + "\" does not exist.";
		++warning_count;
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the list file exists." ) );
	}

    String OutputFile_full = IOUtil.verifyPathForOS (
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile) );
	File f = new File(OutputFile_full);
	if ( !IOUtil.fileExists(f.getParent()) ) {
		message += "\nThe output file folder \"" + f.getParent() + "\" does not exist.";
		++warning_count;
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the output folder exists." ) );
	}

	if ((__Columns_intArray == null) || (__Columns_intArray.length == 0)) {
		message += "\nOne or more columns must be specified";
		++warning_count;
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify columns to merge." ) );
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

	DataTable table = null;
	PropList props = new PropList ( "DataTable" );
	props.set ( "Delimiter", "," );	// Default
	// TODO SAM 2005-11-18 Enable later.
	//if ( MergeDelim != null ) {
		//props.set ( "MergeDelimiters=" + MergeDelim );
	//}
	props.set ( "CommentLineIndicator=#" );	// Skip comment lines
	props.set ( "TrimInput=True" );		// Trim strings after reading.
	props.set ( "TrimStrings=True" );	// Trim strings after parsing
	try {
        table = DataTable.parseFile ( ListFile_full, props );
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unable to read list file \"" + ListFile_full + "\".";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the file exists and is readable." ) );
		throw new CommandWarningException ( message );
	}
	
	// Add a column to the table.  For now always treat as a string...

	table.addField ( new TableField(TableField.DATA_TYPE_STRING,NewColumnName), "" );

	// Loop through the table records, merge the columns and set in the new column...

	int size = table.getNumberOfRecords();
	String merged;	// Merged column string
	int mergedcol = table.getNumberOfFields() - 1;	// New at end
	List v = new Vector ( __Columns_intArray.length );
	TableRecord rec = null;
	String s;
	int j;
	for ( int i = 0; i < size; i++ ) {
		v.clear();
		try {
			rec = table.getRecord(i);
		}
		catch ( Exception e ) {
			message = "Error getting table record [" + i + "]";
			Message.printWarning ( 2,
			MessageUtil.formatMessageTag(command_tag,
			++warning_count),
			routine,message );
            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify the format of the list file." ) );
			continue;
		}
		for ( j = 0; j < __Columns_intArray.length; j++ ) {
			try {
                s = (String)rec.getFieldValue(__Columns_intArray[j]);
				v.add ( s );
			}
			catch ( Exception e ) {
				message = "Error getting table field [" + i + "][" + j + "]";
				Message.printWarning ( 2,
				MessageUtil.formatMessageTag(command_tag,++warning_count),
				routine,message );
                status.addToLog ( CommandPhaseType.RUN,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify the format of the list file - report the problem to software support." ) );
				continue;
			}
		}
		merged = StringUtil.formatString ( v, __SimpleMergeFormat2 );
		try {
			rec.setFieldValue ( mergedcol, merged );
		}
		catch ( Exception e ) {
			message = "Error modifying table record [" + i + "]";
			Message.printWarning ( 2,
			MessageUtil.formatMessageTag(command_tag,
			++warning_count),
			routine,message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                     message, "Report the problem to software support." ) );
			continue;
		}
	}

	// Write the new file...

	Message.printStatus ( 2, routine, "Writing list file \"" + OutputFile_full + "\"" );
	try {
		table.writeDelimitedFile ( OutputFile_full,
			",", // Delimiter
			true, // Write column names
			IOUtil.formatCreatorHeader ( "#", 80, false ), "", // Comments for header
			false, // Always quote strings
			null, // Do not replace newlines in strings
			null ); // Write NaN as is
		// TODO SAM 2005-11-18
		// Need a general IOUtil method to format the header strings
		// (and NOT also open the file).
		
		// Save the output file name...
		setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Unexpected error writing table file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
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
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
	__OutputFile_File = file;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String ListFile = props.getValue( "ListFile" );
	String OutputFile = props.getValue("OutputFile");
	String Columns = props.getValue("Columns");
	String NewColumnName = props.getValue("NewColumnName");
	String SimpleMergeFormat = props.getValue("SimpleMergeFormat");
	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (Columns != null) && (Columns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Columns=\"" + Columns + "\"" );
	}
	if ( (NewColumnName != null) && (NewColumnName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewColumnName=\"" + NewColumnName + "\"" );
	}
	if ( (SimpleMergeFormat != null) && (SimpleMergeFormat.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SimpleMergeFormat=\"" + SimpleMergeFormat + "\"" );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
