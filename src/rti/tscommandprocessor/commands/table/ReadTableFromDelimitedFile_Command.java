package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;

/**
This class initializes, checks, and runs the ReadTableFromDelimitedFile() command.
*/
public class ReadTableFromDelimitedFile_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
The table that is read.
*/
private DataTable __table = null;

/**
Constructor.
*/
public ReadTableFromDelimitedFile_Command ()
{	super();
	setCommandName ( "ReadTableFromDelimitedFile" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String TableID = parameters.getValue ( "TableID" );
    String InputFile = parameters.getValue ( "InputFile" );
	//String SkipLines = parameters.getValue ( "SkipLines" );
	//String SkipColumns = parameters.getValue ( "SkipColumns" );
	//String HeaderLines = parameters.getValue ( "HeaderLines" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	String working_dir = null;
	
	CommandProcessor processor = getCommandProcessor();
	
    if ( (TableID == null) || TableID.isEmpty() ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the table identifier." ) );
    }
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
	
	if ( (InputFile == null) || InputFile.isEmpty() ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
	}
	else if ( InputFile.indexOf("${") < 0 ) {
		// Can only check if no property in path
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, InputFile) );
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The input file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the input file exists - may be OK if created at run time." ) );
			}
			f = null;
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

    /* FIXME add checks for columns
	if ( (Columns == null) || (Columns.length() == 0) ) {
        message = "One or more columns must be specified";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the columns to merge." ) );
	}
	else {	// Check for integers...
		Vector v = StringUtil.breakStringList ( Columns, ",", 0 );
		String token;
		if ( v == null ) {
            message = "One or more columns must be specified";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the columns to merge." ) );
		}
		else {	int size = v.size();
			__Columns_intArray = new int[size];
			for ( int i = 0; i < size; i++ ) {
				token = (String)v.elementAt(i);
				if ( !StringUtil.isInteger(token) ) {
                    message = "Column \"" + token + "\" is not a number";
					warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Specify the column as an integer >= 1." ) );
				}
				else {
                    // Decrement by one to make zero-referended.
					__Columns_intArray[i] = StringUtil.atoi(token) - 1;
				}
			}
		}
	}
    */
 
	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(11);
    validList.add ( "TableID" );
    validList.add ( "InputFile" );
    validList.add ( "Delimiter" );
    validList.add ( "SkipLines" );
    validList.add ( "SkipColumns" );
    validList.add ( "HeaderLines" );
    validList.add ( "DateTimeColumns" );
    validList.add ( "DoubleColumns" );
    validList.add ( "IntegerColumns" );
    validList.add ( "TextColumns" );
    validList.add ( "Top" );
    validList.add ( "RowCountProperty" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );    

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
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
	return (new ReadTableFromDelimitedFile_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
*/
public List getObjectList ( Class c )
{   DataTable table = getDiscoveryTable();
    List<DataTable> v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new Vector<DataTable>();
        v.add ( table );
    }
    return v;
}

// Use base class parseCommand()

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;

	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    Boolean clearStatus = new Boolean(true); // default
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
	if ( (TableID != null) && (TableID.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
	String InputFile = parameters.getValue ( "InputFile" ); // Expanded below
	String Delimiter = parameters.getValue ( "Delimiter" );
	if ( (Delimiter != null) && (Delimiter.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		Delimiter = TSCommandProcessorUtil.expandParameterValue(processor, this, Delimiter);
	}
	String delimiter = ","; // default
	if ( (Delimiter != null) && !Delimiter.isEmpty() ) {
		delimiter = Delimiter;
		delimiter = delimiter.replace("\\t", "\t");
	}
	//String SkipColumns = parameters.getValue ( "SkipColumns" );
	String SkipLines = parameters.getValue ( "SkipLines" );
	String HeaderLines = parameters.getValue ( "HeaderLines" );
	Message.printStatus( 2, routine, "parameter SkipLines=\"" + SkipLines + "\"");
	Message.printStatus( 2, routine, "parameter HeaderLines=\"" + HeaderLines + "\"");
	String DateTimeColumns = parameters.getValue ( "DateTimeColumns" );
	if ( (DateTimeColumns != null) && (DateTimeColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		DateTimeColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, DateTimeColumns);
	}
	String DoubleColumns = parameters.getValue ( "DoubleColumns" );
	if ( (DoubleColumns != null) && (DoubleColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		DoubleColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, DoubleColumns);
	}
	String IntegerColumns = parameters.getValue ( "IntegerColumns" );
	if ( (IntegerColumns != null) && (IntegerColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		IntegerColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, IntegerColumns);
	}
	String TextColumns = parameters.getValue ( "TextColumns" );
	if ( (TextColumns != null) && (TextColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		TextColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, TextColumns);
	}
	String Top = parameters.getValue ( "Top" );
    String RowCountProperty = parameters.getValue ( "RowCountProperty" );
    if ( (RowCountProperty != null) && !RowCountProperty.isEmpty() && (commandPhase == CommandPhaseType.RUN) && RowCountProperty.indexOf("${") >= 0 ) {
    	RowCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, RowCountProperty);
    }

    /* FIXME enable the code
 	if ((__Columns_intArray == null) || (__Columns_intArray.length == 0)) {
		message += "\nOne or more columns must be specified";
		++warning_count;
        status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify columns to merge." ) );
	}

    // FIXME SAM 2007-11-29 Need to move the checks to checkCommandParameters()
	String token;
	String SimpleMergeFormat2 = "";	// Initial value.
	if (	(SimpleMergeFormat == null) ||
		(SimpleMergeFormat.length() == 0) ) {
		// Add defaults (treat as strings)...
		for ( int i = 0; i < __Columns_intArray.length; i++ ) {
			SimpleMergeFormat2 += "%s";
		}
	}
	else {	Vector v = StringUtil.breakStringList (	SimpleMergeFormat, ",", 0 );
		int size = v.size();
		if ( size != __Columns_intArray.length ) {
			message +=
			"\nThe number of specifiers in the merge " +
			"format (" + SimpleMergeFormat +
			") does not match the number of columns (" +
			__Columns_intArray.length;
			++warning_count;
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that the number of format specifiers matches the columns to merge." ) );
		}
		else {	for ( int i = 0; i < size; i++ ) {
				token = (String)v.elementAt(i);
				if ( !StringUtil.isInteger(token) ) {
					message += "\nThe format specifier \""+	token +	"\" is not an integer";
					++warning_count;
                    status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Specify the format specifier as an integer." ) );
				}
				else {	if ( token.startsWith("0") ) {
						// TODO SAM 2005-11-18 Need to enable 0 for %s in StringUtil. Assume integers...
						SimpleMergeFormat2 += "%" + token + "d";
					}
					else {
                        SimpleMergeFormat2 += "%" + token+ "." + token + "s";
					}
				}
			}
		}
	}
    */

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

    if ( commandPhase == CommandPhaseType.RUN ) {
    	String InputFile_full = IOUtil.verifyPathForOS(
	        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
	        	TSCommandProcessorUtil.expandParameterValue(processor, this,InputFile)) );
		if ( !IOUtil.fileExists(InputFile_full) ) {
			message += "\nThe delimited table file \"" + InputFile_full + "\" does not exist.";
			++warning_count;
	        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Verify that the delimited table file exists." ) );
		}
    	DataTable table = null;
    	PropList props = new PropList ( "DataTable" );
    	props.set ( "Delimiter", delimiter );
    	props.set ( "CommentLineIndicator=#" );	// Skip comment lines
    	props.set ( "TrimInput=True" ); // Trim strings before parsing.
    	props.set ( "TrimStrings=True" ); // Trim strings after parsing
    	//props.set ( "ColumnDataTypes=Auto" ); // Automatically determine column data types
    	if ( (SkipLines != null) && !SkipLines.isEmpty() ) {
    	    props.set ( "SkipLines=" + StringUtil.convertNumberSequenceToZeroOffset(SkipLines) );
    	}
        if ( (HeaderLines != null) && !HeaderLines.isEmpty() ) {
            props.set ( "HeaderLines=" + StringUtil.convertNumberSequenceToZeroOffset(HeaderLines) );
        }
        if ( (DateTimeColumns != null) && !DateTimeColumns.isEmpty() ) {
            props.set ( "DateTimeColumns=" + DateTimeColumns);
        }
        if ( (DoubleColumns != null) && !DoubleColumns.isEmpty() ) {
            props.set ( "DoubleColumns=" + DoubleColumns);
        }
        if ( (IntegerColumns != null) && !IntegerColumns.isEmpty() ) {
            props.set ( "IntegerColumns=" + IntegerColumns);
        }
        if ( (TextColumns != null) && !TextColumns.isEmpty() ) {
            props.set ( "TextColumns=" + TextColumns);
        }
        if ( (Top != null) && !Top.isEmpty() ) {
            props.set ( "Top=" + Top);
        }
        Message.printStatus( 2, routine, "parameter zero index SkipLines=\"" + props.getValue("SkipLines") + "\"");
        Message.printStatus( 2, routine, "parameter zero index HeaderLines=\"" + props.getValue("HeaderLines") + "\"");
    	try {
    	    // Always try to read object data types
    	    props.set("ColumnDataTypes=Auto");
            table = DataTable.parseFile ( InputFile_full, props );
            
            // Set the table identifier...
            
            table.setTableID ( TableID );
    	}
    	catch ( Exception e ) {
    		Message.printWarning ( 3, routine, e );
    		message = "Unexpected error reading table from delimited file \"" + InputFile_full + "\" (" + e + ").";
    		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the file exists and is properly formatted - see the log file." ) );
    		throw new CommandWarningException ( message );
    	}
    	
        // Set the table in the processor...

        PropList request_params = new PropList ( "" );
        request_params.setUsingObject ( "Table", table );
        try {
            processor.processRequest( "SetTable", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetTable(Table=...) from processor.";
            Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
            status.addToLog ( commandPhase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                       message, "Report problem to software support." ) );
        }
        
	    // Set the property indicating the number of rows in the table
        if ( (RowCountProperty != null) && !RowCountProperty.isEmpty() ) {
            int rowCount = table.getNumberOfRecords();
            request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", RowCountProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(rowCount) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + RowCountProperty + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( commandPhase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
    }
    else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        // Create an empty table and set the ID
        DataTable table = new DataTable();
        table.setTableID ( TableID );
        setDiscoveryTable ( table );
    }
    
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
	String InputFile = props.getValue( "InputFile" );
	String Delimiter = props.getValue( "Delimiter" );
	String SkipLines = props.getValue("SkipLines");
	String SkipColumns = props.getValue("SkipColumns");
	String HeaderLines = props.getValue("HeaderLines");
	String DateTimeColumns = props.getValue("DateTimeColumns");
	String DoubleColumns = props.getValue("DoubleColumns");
	String IntegerColumns = props.getValue("IntegerColumns");
	String TextColumns = props.getValue("TextColumns");
	String Top = props.getValue("Top");
	String RowCountProperty = props.getValue( "RowCountProperty" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (Delimiter != null) && (Delimiter.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Delimiter=\"" + Delimiter + "\"" );
	}
	if ( (SkipLines != null) && (SkipLines.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SkipLines=\"" + SkipLines + "\"" );
	}
	if ( (SkipColumns != null) && (SkipColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SkipColumns=\"" + SkipColumns + "\"" );
	}
	if ( (HeaderLines != null) && (HeaderLines.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "HeaderLines=\"" + HeaderLines + "\"" );
	}
	if ( (DateTimeColumns != null) && (DateTimeColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DateTimeColumns=\"" + DateTimeColumns + "\"" );
	}
	if ( (DoubleColumns != null) && (DoubleColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DoubleColumns=\"" + DoubleColumns + "\"" );
	}
	if ( (IntegerColumns != null) && (IntegerColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IntegerColumns=\"" + IntegerColumns + "\"" );
	}
	if ( (TextColumns != null) && (TextColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TextColumns=\"" + TextColumns + "\"" );
	}
	if ( (Top != null) && (Top.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Top=" + Top );
	}
    if ( (RowCountProperty != null) && (RowCountProperty.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RowCountProperty=\"" + RowCountProperty + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}