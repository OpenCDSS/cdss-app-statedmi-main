package rti.tscommandprocessor.commands.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the ListFiles() command.
*/
public class ListFiles_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Data members used for Append parameter values.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
The table that is created.
*/
private DataTable __table = null;

/**
Constructor.
*/
public ListFiles_Command ()
{	super();
	setCommandName ( "ListFiles" );
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
{	String Folder = parameters.getValue ( "Folder" );
    String TableID = parameters.getValue ( "TableID" );
	String Append = parameters.getValue ( "Append" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the file to append is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (Folder == null) || (Folder.length() == 0) ) {
		message = "The folder must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the folder."));
	}
    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table ID must be specified.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table ID."));
    }
	if ( (Append != null) && !Append.equals("") ) {
		if ( !Append.equalsIgnoreCase(_False) && !Append.equalsIgnoreCase(_True) ) {
			message = "The Append parameter \"" + Append + "\" is invalid.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _False + " (default) or " + _True + "."));
		}
	}
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(5);
	validList.add ( "Folder" );
	validList.add ( "IncludeFiles" );
	validList.add ( "ExcludeFiles" );
	validList.add ( "TableID" );
	validList.add ( "Append" );
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
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
    List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
	return (new ListFiles_JDialog ( parent, this, tableIDChoices )).ok();
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
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(commandPhase);
	
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }
	
	String Folder = parameters.getValue ( "Folder" );
    // Replace the globbing notation with Java wildcarding
	String IncludeFiles = parameters.getValue ( "IncludeFiles" );
	String includePattern = null;
	if ( (IncludeFiles != null) && !IncludeFiles.equals("") ) {
	    includePattern = IncludeFiles.replace("*",".*");
	}
	String ExcludeFiles = parameters.getValue ( "ExcludeFiles" );
    String excludePattern = null;
    if ( (ExcludeFiles != null) && !ExcludeFiles.equals("") ) {
        excludePattern = ExcludeFiles.replace("*",".*");
    }
	String TableID = parameters.getValue ( "TableID" );
	String Append = parameters.getValue ( "Append" );
	boolean append = false;
	if ( (Append != null) && !Append.equals("")) {
	    if ( Append.equalsIgnoreCase(_True) ) {
	        append = true;
	    }
	}
	
    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( (TableID != null) && !TableID.equals("") ) {
        // Get the table to be updated/created
        request_params = new PropList ( "" );
        request_params.set ( "TableID", TableID );
        try {
            bean = processor.processRequest( "GetTable", request_params);
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table != null ) {
                // Found the table so no need to create it
                table = (DataTable)o_Table;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Process the files.  Each input file is opened to get information.
	
	try {
	    // Make sure the table has the columns that are needed
	    if ( commandPhase == CommandPhaseType.RUN ) {
	        int fileNameCol = -1;
	        int relPathCol = -1;
	        int absPathCol = -1;
    	    if ( (table == null) || !append ) {
    	        // The table needs to be created
    	        List<TableField> columnList = new ArrayList<TableField>();
    	        columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "FileName", -1) );
    	        columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "RelativePath", -1) );
    	        columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AbsolutePath", -1) );
                table = new DataTable( columnList );
                fileNameCol = table.getFieldIndex("FileName");
                relPathCol = table.getFieldIndex("RelativePath");
                absPathCol = table.getFieldIndex("AbsolutePath");
                table.setTableID ( TableID );
                Message.printStatus(2, routine, "Was not able to match existing table \"" + TableID + "\" so created new table.");
                // Set the table in the processor...
                request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", table );
                try {
                    processor.processRequest( "SetTable", request_params);
                }
                catch ( Exception e ) {
                    message = "Error requesting SetTable(Table=...) from processor.";
                    Message.printWarning(warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                    status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                           message, "Report problem to software support." ) );
                }
    	    }
    	    else {
    	        // Make sure that the needed columns exist - otherwise add them
    	        fileNameCol = table.getFieldIndex("FileName");
    	        if ( fileNameCol < 0 ) {
    	            fileNameCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "FileName", -1), "");
    	        }
    	        relPathCol = table.getFieldIndex("RelativePath");
                if ( relPathCol < 0 ) {
                    relPathCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "RelativePath", -1), "");
                }
    	        absPathCol = table.getFieldIndex("AbsolutePath");
                if ( absPathCol < 0 ) {
                    absPathCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "AbsolutePath", -1), "");
                }
    	    }
    	    // Get the list of all the files in the folder
    	    String workingDir = TSCommandProcessorUtil.getWorkingDir(processor);
    	    File folder = new File( IOUtil.verifyPathForOS(IOUtil.toAbsolutePath(workingDir,Folder) ) );
    	    File [] filePathList = folder.listFiles();
    	    boolean allowDuplicates = false;
    	    TableRecord rec = null;
        	for ( File filePath : filePathList ) {
        	    // See if the file matches the include and exclude patterns.  To do this split out the filename from the parent
        	    String file = filePath.getName();
        	    if ( (includePattern != null) && !file.matches(includePattern) ) {
        	        continue;
        	    }
                if ( (excludePattern != null) && file.matches(excludePattern) ) {
                    continue;
                }
                // If here add to the table
                String absPath = filePath.getAbsolutePath();
                String relPath = IOUtil.toRelativePath(workingDir, absPath);
                Message.printStatus(2, routine, "Matched file \"" + absPath + "\"" );
                // Try to get the record by matching the absolute path, which should be unique
                if ( !allowDuplicates ) {
                    // Try to match the TSID 
                    rec = table.getRecord ( absPathCol, absPath );
                }
                if ( rec == null ) {
                    // Create a new record
                    rec = table.addRecord(table.emptyRecord());
                }
                // Set the data in the record
                rec.setFieldValue(fileNameCol,filePath.getName());
                rec.setFieldValue(relPathCol,relPath);
                rec.setFieldValue(absPathCol,absPath);
        	}
	    }
	    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
	        if ( table == null ) {
	            // Did not find table so is being created in this command
	            // Create an empty table and set the ID
	            table = new DataTable();
	            table.setTableID ( TableID );
	        }
	        setDiscoveryTable ( table );
	    }
	}
	catch ( Exception e ) {
        message = "Unexpected error creating file list (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
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
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String Folder = parameters.getValue("Folder");
	String IncludeFiles = parameters.getValue("IncludeFiles");
	String ExcludeFiles = parameters.getValue("ExcludeFiles");
	String TableID = parameters.getValue("TableID");
	String Append = parameters.getValue("Append");
	StringBuffer b = new StringBuffer ();
	if ( (Folder != null) && (Folder.length() > 0) ) {
		b.append ( "Folder=\"" + Folder + "\"" );
	}
    if ( (IncludeFiles != null) && (IncludeFiles.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeFiles=\"" + IncludeFiles + "\"");
    }
    if ( (ExcludeFiles != null) && (ExcludeFiles.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeFiles=\"" + ExcludeFiles + "\"" );
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
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