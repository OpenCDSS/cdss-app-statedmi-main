package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
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
import RTi.Util.IO.CommandProcessorRequestResultsBean;
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
This class initializes, checks, and runs the ReadTableCellsFromExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class ReadTableCellsFromExcel_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Possible values for KeepOpen and ReadAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Possible values for IfTableRowNotFound parameter.
*/
protected final String _Append = "Append";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
The table that is read.
*/
private DataTable __table = null;

/**
Constructor.
*/
public ReadTableCellsFromExcel_Command ()
{	super();
	setCommandName ( "ReadTableCellsFromExcel" );
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
{	
    String InputFile = parameters.getValue ( "InputFile" );
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    String ColumnCellMap = parameters.getValue ( "ColumnCellMap" );
    String TableID = parameters.getValue ( "TableID" );
    String IfTableRowNotFound = parameters.getValue ( "IfTableRowNotFound" );
    //String NumberPrecision = parameters.getValue ( "NumberPrecision" );
	//String ReadAllAsText = parameters.getValue ( "ReadAllAsText" );
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
	
	if ( (InputFile == null) || (InputFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
	}
	else {
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
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.equalsIgnoreCase("") ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }
	
    if ( (ColumnCellMap == null) || (ColumnCellMap.length() == 0) ) {
        message = "The column to cell address map must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the column to cell address map." ) );
    }
    else {
        String [] pairs = ColumnCellMap.split(",");
        for ( int i = 0; i < pairs.length; i++ ) {
            String [] parts = pairs[i].split(":");
            if ( (parts.length == 1) || ((parts.length == 2) && parts[1].trim().equals("")) ) {
                message = "ColumnCellMap item \"" + pairs[i] + "\" cell address is blank.  Expecting Column:CellAddress.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the column to cell address map using Column:CellAddress,Column:CellAddress,..." ) );
            }
            else if ( parts.length != 2 ) {
                message = "ColumnCellMap item " + pairs[i] + "\" contains " + parts.length +
                    " items (delimiter is :).  Expecting Column:CellAddress.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the column to cell address map using Column:CellAddress,Column:CellAddress,..." ) );
            }
        }
    }
    
    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the table identifier." ) );
    }
    if ( (IfTableRowNotFound != null) && !IfTableRowNotFound.equalsIgnoreCase("") && 
        !IfTableRowNotFound.equalsIgnoreCase(_Append) && !IfTableRowNotFound.equalsIgnoreCase(_Ignore) &&
        !IfTableRowNotFound.equalsIgnoreCase(_Warn) && !IfTableRowNotFound.equalsIgnoreCase(_Fail)) {
        message = "IfTableRowNotFound is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "IfTableRowNotFound must be specified as " + _Append + " (default), " + _Ignore + ", " + _Warn + ", or " + _Fail ) );
    }
    /*
    if ( NumberPrecision != null && !NumberPrecision.equals("") ) {
        int numberPrecision = 0;
        boolean bad = false;
        try {
            numberPrecision = Integer.parseInt(NumberPrecision);
            if ( numberPrecision < 0 ) {
                bad = true;
            }
        }
        catch ( NumberFormatException e ) {
            bad = true;
        }
        if ( bad ) {
            message = "The NumberPrecision value (" + NumberPrecision + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify NumberPrecision as a positive integer." ) );
        }
    }
	
	if ( ReadAllAsText != null && !ReadAllAsText.equalsIgnoreCase(_True) && 
        !ReadAllAsText.equalsIgnoreCase(_False) && !ReadAllAsText.equalsIgnoreCase("") ) {
        message = "ReadAllAsText is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "ReadAllAsText must " + _False + " (default) or " + _True ) );
    }
    */
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(7);
    validList.add ( "InputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "KeepOpen" );
    validList.add ( "ColumnCellMap" );
    validList.add ( "TableID" );
    validList.add ( "ColumnIncludeFilters" );
    validList.add ( "IfTableRowNotFound" );
    //validList.add ( "ExcelIntegerCells" );
    //validList.add ( "ExcelDateTimeCells" );
    //validList.add ( "NumberPrecision" );
    //validList.add ( "ReadAllAsText" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
    List<String> tableIDChoices = TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
        (StateDMI_Processor)getCommandProcessor(), this);
	return (new ReadTableCellsFromExcel_JDialog ( parent, this, tableIDChoices )).ok();
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
private void runCommandInternal ( int command_number, CommandPhaseType command_phase )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "ReadTableCellsFromExcelFile_Command.runCommand",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(command_phase);
    if ( command_phase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

	String InputFile = parameters.getValue ( "InputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase(_True) ) {
        keepOpen = true;
    }
    String ColumnCellMap = parameters.getValue ( "ColumnCellMap" );
    //Hashtable columnCellMap = new Hashtable();
    LinkedHashMap<String,String> columnCellMap = new LinkedHashMap<String,String>();
    if ( (ColumnCellMap != null) && (ColumnCellMap.length() > 0) && (ColumnCellMap.indexOf(":") > 0) ) {
        // First break map pairs by comma
        String [] pairs = ColumnCellMap.split(",");
        // Now break pairs and put in hashtable
        String column, address;
        for ( int i = 0; i < pairs.length; i++ ) {
            String [] parts = pairs[i].split(":");
            column = parts[0].trim();
            address = "";
            if ( parts.length > 1 ) {
                address = parts[1].trim();
            }
            if ( columnCellMap.get(column) != null ) {
                message = "ColumnCellMap has duplicate column values \"" + column + "\".";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Currently cannot specify the column more than once." ) );
            }
            else {
                columnCellMap.put(column,address);
            }
        }
    }
    String TableID = parameters.getValue ( "TableID" );
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    Hashtable<String,String> columnIncludeFiltersMap = null;
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.length() > 0) && (ColumnIncludeFilters.indexOf(":") > 0) ) {
        columnIncludeFiltersMap = new Hashtable<String,String>();
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnIncludeFilters, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            String tableColumn = parts[0].trim().toUpperCase();
            String pattern = "";
            if ( parts.length > 1 ) {
                // Use upper-case to facilitate case-independent comparisons, and replace * globbing with internal Java notation
                pattern = parts[1].trim().toUpperCase().replace("*", ".*");
            }
            columnIncludeFiltersMap.put(tableColumn, pattern );
        }
    }
    String IfTableRowNotFound = parameters.getValue ( "IfTableRowNotFound" );
    if ( (IfTableRowNotFound == null) || IfTableRowNotFound.equals("") ) {
        IfTableRowNotFound = _Warn; // Default
    }
    /*
	String ExcelIntegerCells = parameters.getValue ( "ExcelIntegerCells" );
	String [] excelIntegerCells = null;
	if ( (ExcelIntegerCells != null) && !ExcelIntegerCells.equals("") ) {
	    excelIntegerCells = ExcelIntegerCells.split(",");
	    for ( int i = 0; i < excelIntegerCells.length; i++ ) {
	        excelIntegerCells[i] = excelIntegerCells[i].trim();
	    }
	}
    String ExcelDateTimeCells = parameters.getValue ( "ExcelDateTimeCells" );
    String [] excelDateTimeColumns = null;
    if ( (ExcelDateTimeCells != null) && !ExcelDateTimeCells.equals("") ) {
        excelDateTimeColumns = ExcelDateTimeCells.split(",");
        for ( int i = 0; i < excelDateTimeColumns.length; i++ ) {
            excelDateTimeColumns[i] = excelDateTimeColumns[i].trim();
        }
    }
	String NumberPrecision = parameters.getValue ( "NumberPrecision" );
	int numberPrecision = 6; // default
	try {
	    numberPrecision = Integer.parseInt(NumberPrecision);
	}
	catch ( NumberFormatException e ) {
	    numberPrecision = 6;
	}
	String ReadAllAsText = parameters.getValue ( "ReadAllAsText" );
	boolean readAllAsText = false;
	if ( (ReadAllAsText != null) && ReadAllAsText.equalsIgnoreCase("True") ) {
	    readAllAsText = true;
	}
	*/

	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),InputFile) );
	if ( !IOUtil.fileExists(InputFile_full) ) {
		message += "\nThe Excel workbook file \"" + InputFile_full + "\" does not exist.";
		++warning_count;
        status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the Excel workbook file exists." ) );
	}
	
    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( command_phase == CommandPhaseType.RUN ) {
        if ( (TableID != null) && !TableID.equals("") ) {
            // Get the table to be updated
            request_params = new PropList ( "" );
            request_params.set ( "TableID", TableID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table != null ) {
                // Will append to the table
                table = (DataTable)o_Table;
            }
        }
        // TODO SAM 2014-03-02 Make this work for discovery mode
        if ( table == null ) {
            message = "Table does not exist for TableID=\"" + TableID + "\" - cannot transfer data from Excel";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Make sure that the table is created before this command." ) ); 
        }
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

	List<String> problems = new ArrayList<String>();
	try {
	    if ( command_phase == CommandPhaseType.RUN ) {
	        // First read the Excel cell values
	        ExcelToolkit tk = new ExcelToolkit();
            tk.readTableCells ( InputFile_full, Worksheet, keepOpen,
                columnCellMap,
                table, columnIncludeFiltersMap, IfTableRowNotFound,
                //excelIntegerCells, excelDateTimeColumns, numberPrecision, readAllAsText,
                problems );
            boolean doWarning;
            for ( String problem: problems ) {
                // TODO SAM 2014-03-02 The start of string IfTableRowNotFoundWarning and IfTableRowNotFoundFailure
                // are returned to handle the flag - evaluate a cleaner way
                doWarning = false;
                if ( problem.startsWith("IfTableRowNotFoundWarning") ) {
                    // Generate a warning message instead of failure
                    doWarning = true;
                }
                Message.printWarning ( 3, routine, problem );
                message = "Error reading table cells from Excel: " + problem;
                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                if ( doWarning ) {
                    status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
                        message, "Check the log file for exceptions." ) );
                }
                else {
                    // Failure
                    status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Check the log file for exceptions." ) );
                }
            }   
            // Set the table identifier...
            if ( table == null ) {
                // Create an empty table to have something in output for user
                table = new DataTable();
                table.setTableID ( TableID );
            }
	    }
	    else if ( command_phase == CommandPhaseType.DISCOVERY ) {
	        // Create an empty table.
	        table = new DataTable();
	        table.setTableID ( TableID );
	    }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error reading table from Excel workbook file \"" + InputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the file exists and is readable." ) );
		throw new CommandWarningException ( message );
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}
    
    // Set the table in the processor...
    
    if ( command_phase == CommandPhaseType.RUN ) {
        request_params = new PropList ( "" );
        request_params.setUsingObject ( "Table", table );
        try {
            processor.processRequest( "SetTable", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting SetTable(Table=...) from processor.";
            Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                       message, "Report problem to software support." ) );
        }
    }
    // TODO SAM 2014-03-02 Currently the table must exist but evaluate whether to automatically create
    //else if ( command_phase == CommandPhaseType.DISCOVERY ) {
    //    setDiscoveryTable ( table );
    //}

    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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
	String InputFile = props.getValue( "InputFile" );
	String Worksheet = props.getValue( "Worksheet" );
	String KeepOpen = props.getValue("KeepOpen");
	String ColumnCellMap = props.getValue("ColumnCellMap");
    String TableID = props.getValue( "TableID" );
    String ColumnIncludeFilters = props.getValue( "ColumnIncludeFilters" );
    String IfTableRowNotFound = props.getValue( "IfTableRowNotFound" );
	//String ExcelIntegerCells = props.getValue("ExcelIntegerCells");
	//String ExcelDateTimeCells = props.getValue("ExcelDateTimeCells");
	//String NumberPrecision = props.getValue("NumberPrecision");
	//String ReadAllAsText = props.getValue("ReadAllAsText");
	StringBuffer b = new StringBuffer ();
    if ( (InputFile != null) && (InputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InputFile=\"" + InputFile + "\"" );
    }
    if ( (Worksheet != null) && (Worksheet.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Worksheet=\"" + Worksheet + "\"" );
    }
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (ColumnCellMap != null) && (ColumnCellMap.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnCellMap=\"" + ColumnCellMap + "\"");
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnIncludeFilters=\"" + ColumnIncludeFilters + "\"" );
    }
    if ( (IfTableRowNotFound != null) && (IfTableRowNotFound.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IfTableRowNotFound=" + IfTableRowNotFound );
    }
    /*
    if ( (ExcelIntegerCells != null) && (ExcelIntegerCells.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelIntegerCells=\"" + ExcelIntegerCells + "\"" );
    }
    if ( (ExcelDateTimeCells != null) && (ExcelDateTimeCells.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelDateTimeCells=\"" + ExcelDateTimeCells + "\"" );
    }
    if ( (NumberPrecision != null) && (NumberPrecision.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NumberPrecision=" + NumberPrecision );
    }
    if ( (ReadAllAsText != null) && (ReadAllAsText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ReadAllAsText=" + ReadAllAsText );
    }
    */
	return getCommandName() + "(" + b.toString() + ")";
}

}