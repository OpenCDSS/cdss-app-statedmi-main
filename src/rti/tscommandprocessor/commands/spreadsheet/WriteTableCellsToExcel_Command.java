// WriteTableCellsToExcel_Command - This class initializes, checks, and runs the WriteTableCellsToExcel() command, using Apache POI.

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

package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.JFrame;

import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
//import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;

/**
This class initializes, checks, and runs the WriteTableCellsToExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class WriteTableCellsToExcel_Command extends AbstractCommand implements Command
{

/**
Possible values for ExcelColumnNames parameter.
*/
protected final String _ColumnN = "ColumnN";
protected final String _FirstRowInRange = "FirstRowInRange";
protected final String _None = "None";
protected final String _RowBeforeRange = "RowBeforeRange";

/**
Possible values for WriteAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Possible values for CellFormat parameter.
*/
protected final String _FromExcel = "FromExcel";
protected final String _FromTable = "FromTable";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public WriteTableCellsToExcel_Command ()
{	super();
	setCommandName ( "WriteTableCellsToExcel" );
}

// TODO SAM 2013-08-12 This can be optimized to not have to check column name and do upper-case conversions
/**
Evaluate whether a cell value matches an exclude pattern.
@param columnName name of Excel column being checked
@param cellValue cell value as string, to check
@param filtersMap map of column 
@return true if the cell matches a filter
*/
@SuppressWarnings("unused")
private boolean cellMatchesFilter ( String columnName, String cellValue, Hashtable<String,String> filtersMap )
{
    if ( filtersMap == null ) {
        return false;
    }
    Enumeration<String> keys = filtersMap.keys();
    String key = null;
    // Compare as upper case to treat as case insensitive
    String cellValueUpper = null;
    if ( cellValue != null ) {
        cellValueUpper = cellValue.toUpperCase();
    }
    String columnNameUpper = columnName.toUpperCase();
    String pattern;
    while ( keys.hasMoreElements() ) {
        key = keys.nextElement();
        pattern = filtersMap.get(key);
        //Message.printStatus(2,"","Checking column \"" + columnNameUpper + "\" against key \"" + key +
        //    "\" for cell value \"" + cellValueUpper + "\" and pattern \"" + pattern + "\"" );
        if ( columnNameUpper.equals(key) ) {
            if ( ((cellValue == null) || (cellValue.length() == 0)) &&
                ((pattern == null) || (pattern.length() == 0)) ) {
                // Blank cell should be ignored
                return true;
            }
            else if ( (cellValueUpper != null) && cellValueUpper.matches(pattern) ) {
                return true;
            }
        }
    }
    return false;
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
    String OutputFile = parameters.getValue ( "OutputFile" );
    String ColumnCellMap = parameters.getValue ( "ColumnCellMap" );
    //String NumberPrecision = parameters.getValue ( "NumberPrecision" );
	//String WriteAllAsText = parameters.getValue ( "WriteAllAsText" );
    String CellFormat = parameters.getValue ( "CellFormat" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	CommandProcessor processor = getCommandProcessor();
	
    if ( (TableID == null) || (TableID.length() == 0) ) {
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
			//working_dir = (String)o;
		}
	}
	catch ( Exception e ) {
        message = "Error requesting WorkingDir from processor.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
	}

	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The Excel output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing Excel output file." ) );
	}
	/** TODO SAM 2014-01-12 Evaluate whether to only do this check at run-time
	else {
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, OutputFile) );
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The Excel output file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the Excel output file exists - may be OK if created at run time." ) );
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
	*/
	
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
	
	if ( WriteAllAsText != null && !WriteAllAsText.equalsIgnoreCase(_True) && 
        !WriteAllAsText.equalsIgnoreCase(_False) && !WriteAllAsText.equalsIgnoreCase("") ) {
        message = "WriteAllAsText is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "WriteAllAsText must be specified as " + _False + " (default) or " + _True ) );
    }
    */
	
    if ( CellFormat != null && !CellFormat.equalsIgnoreCase(_True) && 
        !CellFormat.equalsIgnoreCase(_False) && !CellFormat.equalsIgnoreCase("") ) {
        message = "CellFormat is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "CellFormat must be specified as " + _FromExcel + " (default) or " + _FromTable ) );
    }
    
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.equalsIgnoreCase("") ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }

	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(7);
    validList.add ( "TableID" );
    validList.add ( "OutputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "ColumnIncludeFilters" );
    //validList.add ( "Comment" );
    //validList.add ( "ExcelIntegerColumns" );
    //validList.add ( "ExcelDateTimeColumns" );
    //validList.add ( "NumberPrecision" );
    //validList.add ( "WriteAllAsText" );
    validList.add ( "ColumnCellMap" );
    validList.add ( "CellFormat" );
    validList.add ( "KeepOpen" );
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
	return (new WriteTableCellsToExcel_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Get the array of cell ranges based on one of the input address methods.
@param wb the Excel workbook object
@param sheet the sheet in the workbook, read in entirety if no other address information is given
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name, treated as named range
@return null if no area reference can be determined
*/
@SuppressWarnings("unused")
private AreaReference getAreaReference ( Workbook wb, Sheet sheet,
    String excelAddress, String excelNamedRange, String excelTableName )
{   String routine = "WriteTableToExcel_Command.getAreaReference";
    if ( (excelTableName != null) && (excelTableName.length() > 0) ) {
        // Table name takes precedence as range name
        excelNamedRange = excelTableName;
    }
    // If sheet is specified but excelAddress, String excelNamedRange, String excelTableName are not,
    // read the entire sheet
    if ( ((excelAddress == null) || (excelAddress.length() == 0)) &&
        ((excelNamedRange == null) || (excelNamedRange.length() == 0)) ) {
        // Examine the sheet for blank columns/cells.  POI provides methods for the rows...
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        Message.printStatus(2, routine, "Sheet firstRow=" + firstRow + ", lastRow=" + lastRow );
        // ...but have to iterate through the rows as per:
        //  http://stackoverflow.com/questions/2194284/how-to-get-the-last-column-index-reading-excel-file
        Row row;
        int firstCol = -1;
        int lastCol = -1;
        int cellNum; // Index of cell in row (not column number?)
        int col;
        for ( int iRow = firstRow; iRow <= lastRow; iRow++ ) {
            row = sheet.getRow(iRow);
            if ( row == null ) {
                // TODO SAM 2013-06-28 Sometimes this happens with extra rows at the end of a worksheet?
                continue;
            }
            cellNum = row.getFirstCellNum(); // Not sure what this returns if no columns.  Assume -1
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( firstCol < 0 ) {
                    firstCol = col;
                }
                else {
                    firstCol = Math.min(firstCol, col);
                }
            }
            cellNum = row.getLastCellNum() - 1; // NOTE -1, as per API docs
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( lastCol < 0 ) {
                    lastCol = col;
                }
                else {
                    lastCol = Math.max(lastCol, col);
                }
            }
            Message.printStatus(2, routine, "row " + iRow + ", firstCol=" + firstCol + ", lastCol=" + lastCol );
        }
        // Return null if the any of the row column limits were not determined
        if ( (firstRow < 0) || (firstCol < 0) || (lastRow < 0) || (lastCol < 0) ) {
            return null;
        }
        else {
            return new AreaReference(new CellReference(firstRow,firstCol), new CellReference(lastRow,lastCol));
        }
    }
    if ( (excelAddress != null) && (excelAddress.length() > 0) ) {
        return new AreaReference(excelAddress);
    }
    else if ( (excelNamedRange != null) && (excelNamedRange.length() > 0) ) {
        int namedCellIdx = wb.getNameIndex(excelNamedRange);
        if ( namedCellIdx < 0 ) {
            Message.printWarning(3, routine, "Unable to get Excel internal index for named range \"" +
                excelNamedRange + "\"" );
            return null;
        }
        Name aNamedCell = wb.getNameAt(namedCellIdx);

        // Retrieve the cell at the named range and test its contents
        // Will get back one AreaReference for C10, and
        //  another for D12 to D14
        AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
        // Can only handle one area
        if ( arefs.length != 1 ) {
            return null;
        }
        else {
            return arefs[0];
        }
    }
    else {
        return null;
    }
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    List<File> list = new Vector<File>();
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
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    status.clearLog(commandPhase);
    
    // Clear the output file
    setOutputFile ( null );

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String TableID = parameters.getValue ( "TableID" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    Hashtable<String,String> ColumnIncludeFiltersMap = null;
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.length() > 0) && (ColumnIncludeFilters.indexOf(":") > 0) ) {
        ColumnIncludeFiltersMap = new Hashtable<String,String>();
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
            ColumnIncludeFiltersMap.put(tableColumn, pattern );
        }
    }
    /*
	String ExcelIntegerColumns = parameters.getValue ( "ExcelIntegerColumns" );
	String [] excelIntegerColumns = null;
	if ( (ExcelIntegerColumns != null) && !ExcelIntegerColumns.equals("") ) {
	    excelIntegerColumns = ExcelIntegerColumns.split(",");
	    for ( int i = 0; i < excelIntegerColumns.length; i++ ) {
	        excelIntegerColumns[i] = excelIntegerColumns[i].trim();
	    }
	}
    String ExcelDateTimeColumns = parameters.getValue ( "ExcelDateTimeColumns" );
    String [] excelDateTimeColumns = null;
    if ( (ExcelDateTimeColumns != null) && !ExcelDateTimeColumns.equals("") ) {
        excelDateTimeColumns = ExcelDateTimeColumns.split(",");
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
	String WriteAllAsText = parameters.getValue ( "WriteAllAsText" );
	boolean writeAllAsText = false; // default
	if ( (WriteAllAsText != null) && WriteAllAsText.equalsIgnoreCase("True") ) {
	    writeAllAsText = true;
	}
	*/
    String ColumnCellMap = parameters.getValue ( "ColumnCellMap" );
    Hashtable<String,String> columnCellMap = new Hashtable<String,String>();
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
    String CellFormat = parameters.getValue ( "CellFormat" );
    boolean cellFormatExcel = true; // default
    if ( (CellFormat != null) && CellFormat.equalsIgnoreCase(_FromTable) ) {
        cellFormatExcel = false;
    }
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase(_True) ) {
        keepOpen = true;
    }
	
	// Get the table to process
	
    PropList request_params = new PropList ( "" );
    request_params.set ( "TableID", TableID );
    CommandProcessorRequestResultsBean bean = null;
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
    DataTable table = null;
    if ( o_Table == null ) {
        message = "Unable to find table to process using TableID=\"" + TableID + "\".";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that a table exists with the requested ID." ) );
    }
    else {
        table = (DataTable)o_Table;
    }

	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile) );
	if ( (ExcelUtil.getOpenWorkbook(OutputFile_full) == null) && !IOUtil.fileExists(OutputFile_full) ) {
		message += "\nThe Excel workbook file \"" + OutputFile_full + "\" is not open from a previous command and does not exist.";
		++warning_count;
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the Excel workbook file is open in memory or exists as a file." ) );
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
        // Check that named ranges match columns
	    if ( ColumnCellMap != null ) {
    	    Enumeration<String> keys = columnCellMap.keys();
            String key = null;
            while ( keys.hasMoreElements() ) {
                key = keys.nextElement(); // Column name
                // Find the table column
                if ( table.getFieldIndex(key) < 0 ) {
                    message += "\nThe column \"" + key + "\" to map to a cell address does not exist in the table.";
                    ++warning_count;
                    status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify the column name to be used for the cell address." ) );
                }
            }
	    }
	    ExcelToolkit tk = new ExcelToolkit();
        tk.writeTableCells ( table, OutputFile_full, Worksheet,
            ColumnIncludeFiltersMap,
            //comment, excelIntegerColumns, excelDateTimeColumns, numberPrecision, writeAllAsText,
            columnCellMap, cellFormatExcel, keepOpen, problems );
        for ( String problem: problems ) {
            Message.printWarning ( 3, routine, problem );
            message = "Error writing to Excel: " + problem;
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Check the log file for exceptions." ) );
        }
        // Save the output file name...
        setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error writing table to Excel workbook file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the file exists and is writeable." ) );
		throw new CommandWarningException ( message );
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
    String TableID = props.getValue( "TableID" );
	String OutputFile = props.getValue( "OutputFile" );
	String Worksheet = props.getValue( "Worksheet" );
	String ColumnIncludeFilters = props.getValue("ColumnIncludeFilters");
	//String ExcelIntegerColumns = props.getValue("ExcelIntegerColumns");
	//String ExcelDateTimeColumns = props.getValue("ExcelDateTimeColumns");
	//String NumberPrecision = props.getValue("NumberPrecision");
	//String WriteAllAsText = props.getValue("WriteAllAsText");
	String ColumnCellMap = props.getValue("ColumnCellMap");
	String CellFormat = props.getValue("CellFormat");
	String KeepOpen = props.getValue("KeepOpen");
	StringBuffer b = new StringBuffer ();
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
    if ( (ColumnCellMap != null) && (ColumnCellMap.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnCellMap=\"" + ColumnCellMap + "\"");
    }
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
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
    // TODO SAM 2014-03-01 Enable the following if needed
    /*
    if ( (ExcelIntegerColumns != null) && (ExcelIntegerColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelIntegerColumns=\"" + ExcelIntegerColumns + "\"" );
    }
    if ( (ExcelDateTimeColumns != null) && (ExcelDateTimeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelDateTimeColumns=\"" + ExcelDateTimeColumns + "\"" );
    }
    */
    /*
    if ( (NumberPrecision != null) && (NumberPrecision.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NumberPrecision=" + NumberPrecision );
    }
    if ( (WriteAllAsText != null) && (WriteAllAsText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "WriteAllAsText=" + WriteAllAsText );
    }
    */
    if ( (CellFormat != null) && (CellFormat.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CellFormat=" + CellFormat );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
