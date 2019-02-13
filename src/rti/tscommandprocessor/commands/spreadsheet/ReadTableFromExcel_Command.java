// ReadTableFromExcel_Command - This class initializes, checks, and runs the ReadTableFromExcel() command, using Apache POI.

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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;

import rti.tscommandprocessor.commands.spreadsheet.ExcelColumnNameRowType;
import rti.tscommandprocessor.commands.spreadsheet.ExcelToolkit;
import rti.tscommandprocessor.commands.spreadsheet.ExcelUtil;
import rti.tscommandprocessor.commands.spreadsheet.WorkbookFileMetadata;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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
import RTi.Util.String.StringFilterList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeZoneDefaultType;

/**
This class initializes, checks, and runs the ReadTableFromExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class ReadTableFromExcel_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Possible values for ReadAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
The table that is read.
*/
private DataTable __table = null;

/**
The first data row from the range.  Save this because it is possible that comment lines
are skipped so not as easy as assuming the header line is before or at start of range.
*/
private int __firstDataRow = 0;

/**
Constructor.
*/
public ReadTableFromExcel_Command ()
{	super();
	setCommandName ( "ReadTableFromExcel" );
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
    String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
    String NumberPrecision = parameters.getValue ( "NumberPrecision" );
	String ReadAllAsText = parameters.getValue ( "ReadAllAsText" );
    String KeepOpen = parameters.getValue ( "KeepOpen" );
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
	else if ( InputFile.indexOf("${") < 0 ){
		/* Allow input file to not exist
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
		*/
	}
	
    if ( (ExcelColumnNames != null) && !ExcelColumnNames.isEmpty() ) {
    	ExcelColumnNameRowType t = ExcelColumnNameRowType.valueOfIgnoreCase(ExcelColumnNames);
    	if ( t == null ) {
	        message = "ExcelColumnNames is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "ExcelColumnNames must " + ExcelColumnNameRowType.COLUMN_N + " (default), " +
	            	ExcelColumnNameRowType.FIRST_ROW_IN_RANGE + ", or " + ExcelColumnNameRowType.ROW_BEFORE_RANGE) );
    	}
    }
    
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
	List<String> validList = new ArrayList<String>(18);
    validList.add ( "TableID" );
    validList.add ( "NumberPrecision" );
    validList.add ( "InputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "ExcelAddress" );
    validList.add ( "ExcelNamedRange" );
    validList.add ( "ExcelTableName" );
    validList.add ( "ExcelColumnNames" );
    validList.add ( "KeepOpen" );
    validList.add ( "ColumnIncludeFilters" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "Comment" );
    validList.add ( "ExcelDoubleColumns" );
    validList.add ( "ExcelIntegerColumns" );
    validList.add ( "ExcelDateTimeColumns" );
    validList.add ( "ExcelTextColumns" );
    validList.add ( "ReadAllAsText" );
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
	return (new ReadTableFromExcel_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
}

/**
Return the row (0+) of the first data row to process.
*/
private int getFirstDataRow ()
{
    return __firstDataRow;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
*/
@SuppressWarnings("rawtypes")
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
Read the table from an Excel worksheet.  The cells must be specified by a contiguous address block specified
by one of the parameters excelAddress, excelNamedRange, excelTableName.
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name
@param excelColumnNames indicate how to determine column names from the Excel worksheet
@param columnIncludeFilterList a map indicating patterns for column values, to include rows
@param columnExcludeFilterList a map indicating patterns for column values, to exclude rows
@param comment character that if at start of first column indicates row is a comment
@param excelIntegerColumns names of columns that should be treated as integers, or null if none
@param numberPrecision digits after decimal for floating point numbers (can't yet determine from Excel)
@param readAllAsText if True, treat all data as text values
@param problems list of problems encountered during read, for formatted logging in calling code
@return a DataTable with the Excel contents
*/
private DataTable readTableFromExcelFile ( String workbookFile, String sheetName, boolean keepOpen,
    String excelAddress, String excelNamedRange, String excelTableName, ExcelColumnNameRowType excelColumnNames,
    StringFilterList columnIncludeFilterList, StringFilterList columnExcludeFilterList,
    String comment,
    String [] excelDoubleColumns, String [] excelIntegerColumns, String [] excelDateTimeColumns, String [] excelTextColumns,
    int numberPrecision, boolean readAllAsText, List<String> problems )
throws FileNotFoundException, IOException
{   String routine = getClass().getSimpleName() + ".readTableFromExcelFile", message;
    DataTable table = new DataTable();
    if ( (comment != null) && (comment.trim().length() == 0) ) {
        // Set to null to simplify logic below
        comment = null;
    }

    ExcelToolkit tk = new ExcelToolkit();
    Workbook wb = null;
    InputStream inp = null;
    try {
        // See if an open workbook by the same name exists
        WorkbookFileMetadata wbMeta = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wbMeta != null ) {
        	wb = wbMeta.getWorkbook();
        }
        if ( wb == null ) {
            try {
                inp = new FileInputStream(workbookFile);
            }
            catch ( IOException e ) {
                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
                return null;
            }
            try {
                wb = WorkbookFactory.create(inp);
            }
            catch ( InvalidFormatException e ) {
                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
                return null;
            }
        }
        Sheet sheet = null;
        // TODO SAM 2013-02-22 In the future sheet may be determined from named address (e.g., named ranges
        // are global in workbook)
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
            sheet = wb.getSheetAt(0);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets" );
                return null;
            }
        }
        else {
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include worksheet named \"" + sheetName + "\"" );
                return null;
            }
        }
        // Get the contiguous block of data to process by evaluating user input
        AreaReference area = tk.getAreaReference ( wb, sheet, excelAddress, excelNamedRange, excelTableName );
        if ( area == null ) {
            problems.add ( "Unable to get worksheet area reference from address information." );
            return null;
        }
        Message.printStatus(2,routine,"Excel address block to read: " + area );
        // Create the table based on the first row of the area
        Object [] o = tk.createTableColumns ( table, wb, sheet, area, excelColumnNames, comment,
        	excelDoubleColumns, excelIntegerColumns, excelDateTimeColumns, excelTextColumns, numberPrecision, readAllAsText, problems );
        String [] columnNames = (String [])o[0];
        setFirstDataRow((Integer)o[1]);
        int [] tableColumnTypes = table.getFieldDataTypes();
        // Read the data from the area and transfer to the table.
        Row row;
        Cell cell;
        int rowStart = getFirstDataRow(); // Set in createTableColumns()
        int rowEnd = area.getLastCell().getRow();
        int colStart = area.getFirstCell().getCol();
        int colEnd = area.getLastCell().getCol();
        String numberFormat = "%." + numberPrecision + "f"; // Used to format numeric to text to avoid Java exponential notation
        Message.printStatus(2, routine, "Cell range is [" + rowStart + "][" + colStart + "] to [" + rowEnd + "][" + colEnd + "]");
        int cellType;
        int iRowOut = -1, iColOut;
        String cellValueString;
        Object cellValueObject = null; // Generic cell object for logging
        boolean cellValueBoolean;
        double cellValueDouble;
        Date cellValueDate;
        CellValue formulaCellValue = null; // Cell value after formula evaluation
        DateTime dt;
        boolean cellIsFormula; // Used to know when the evaluate cell formula to get output object
        boolean needToSkipRow = false; // Whether a row should be skipped when reading (depends on columnIncludeFilterList and columnExcludeFilterList)
        int nRowsToRead = rowEnd - rowStart + 1;
        for ( int iRow = rowStart; iRow <= rowEnd; iRow++ ) {
            row = sheet.getRow(iRow);
            if ( row == null ) {
                // Seems to happen at bottom of worksheets where there are extra junk rows
                continue;
            }
            if ( Message.isDebugOn ) {
                Message.printDebug(1, routine, "Processing row [" + iRow + "] end at [" + rowEnd + "]" );
            }
            int updateDelta = nRowsToRead/20;
            if ( updateDelta == 0 ) {
                updateDelta = 2;
            }
            if ( (iRow == rowStart) || (iRow == rowEnd) || (iRow%updateDelta == 0) ) {
                // Update the progress bar every 5%
                message = "Reading row " + (iRow - rowStart + 1) + " of " + nRowsToRead;
                notifyCommandProgressListeners ( (iRow - rowStart), nRowsToRead, (float)-1.0, message );
            }
            if ( (comment != null) && tk.isRowComment(sheet, iRow, comment) ) {
                // No need to process the row.
                continue;
            }
            // Increment after check for comments.
            // Other skipped rows are handled by removing the row and decrementing iRowOut
            ++iRowOut;
            needToSkipRow = false;
            iColOut = -1;
            for ( int iCol = colStart; iCol <= colEnd; iCol++ ) {
                ++iColOut;
                cell = row.getCell(iCol);
                if ( Message.isDebugOn ) {
                    Message.printDebug(1, routine, "Cell [" + iRow + "][" + iCol + "]= \"" + cell + "\" - types determined below" );
                }
                try {
                    if ( cell == null ) {
                    	// Handle null cells specifically - might cause row to be ignored
                        String cellValue = null;
                        if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                            cellValue = "";
                        }
                        table.setFieldValue(iRowOut, iColOut, cellValue, true);
                        if ( !needToSkipRow && (tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING) &&
                            !tk.cellMatchesIncludeFilter(columnNames[iCol - colStart], cellValue, columnIncludeFilterList, true, true) ) {
                            // Row was added but will remove at the end after all columns are processed
                        	// Only change from false to true to handle previous columns setting to true
                        	needToSkipRow = true;
                        }
                        if ( !needToSkipRow && (tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING) &&
                            tk.cellMatchesExcludeFilter(columnNames[iCol - colStart], cellValue, columnExcludeFilterList, true, true) ) {
                            // Row was added but will remove at the end after all columns are processed
                        	// Only change from false to true since include takes precedence
                        	// In other words, if the include has set to true then the row is already excluded so don't undo that setting.
                    		needToSkipRow = true;
                        }
                        continue;
                    }
                    // If here the cell is not null.
                    // First get the data using the type indicated for the cell.  Then translate to
                    // the appropriate type in the data table.  Handling at cell level is needed because
                    // the Excel worksheet might have cell values that are mixed type in the column.
                    // The checks are exhaustive, so list in the order that is most likely (string, double,
                    // boolean, blank, error, formula).
                    cellValueObject = null; // For try/catch
                    cellType = cell.getCellType();
                    if ( Message.isDebugOn ) {
                        Message.printDebug(1, routine, "Cell [" + iRow + "][" + iCol + "]= \"" + cell + "\" type=" +
                            cellType + " " + tk.lookupExcelCellType(cellType));
                    }
                    cellIsFormula = false;
                    if ( cellType == Cell.CELL_TYPE_FORMULA ) {
                        // Have to evaluate the cell and get the value as the result
                        cellIsFormula = true;
                        try {
                            FormulaEvaluator formulaEval = wb.getCreationHelper().createFormulaEvaluator();
                            formulaCellValue = formulaEval.evaluate(cell);
                            // Reset cellType for following code
                            cellType = formulaCellValue.getCellType();
                            if ( Message.isDebugOn ) {
                                Message.printDebug(1, routine, "Detected formula, new cellType=" + cellType +
                                    ", cell value=\"" + formulaCellValue + "\"" );
                            }
                        }
                        catch ( Exception e ) {
                            // Handle as an error in processing below.
                            problems.add ( "Error evaluating formula for row [" + iRow + "][" + iCol + "] \"" +
                                cell + "\" - setting to error cell type (" + e + ")");
                            cellType = Cell.CELL_TYPE_ERROR;
                        }
                    }
                    if ( cellType == Cell.CELL_TYPE_STRING ) {
                        if ( cellIsFormula ) {
                            cellValueString = formulaCellValue.getStringValue();
                        }
                        else {
                            cellValueString = cell.getStringCellValue();
                        }
                        cellValueObject = cellValueString; // For try/catch
                        if ( !needToSkipRow && !tk.cellMatchesIncludeFilter(columnNames[iCol - colStart], cellValueString, columnIncludeFilterList, true, true) ) {
                            // Add the row but will remove at the end after all columns are processed
                        	// Only change from false to true to handle previous columns setting to true
                        	needToSkipRow = true;
                        }
                        if ( !needToSkipRow && tk.cellMatchesExcludeFilter(columnNames[iCol - colStart], cellValueString, columnExcludeFilterList, true, true) ) {
                            // Add the row but will remove at the end after all columns are processed
                        	// Only change from false to true since include takes precedence
                        	// In other words, if the include has set to true then the row is already excluded so don't undo that setting.
                        	needToSkipRow = true;
                        }
                        if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                            // Just set
                            table.setFieldValue(iRowOut, iColOut, cellValueString, true);
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DOUBLE ) {
                            // Parse to the double
                            try {
                                table.setFieldValue(iRowOut, iColOut, new Double(cellValueString), true);
                            }
                            catch ( NumberFormatException e ) {
                                // Set to NaN
                                table.setFieldValue(iRowOut, iColOut, Double.NaN, true);
                            }
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_INT ) {
                            // Parse to the boolean
                            if ( cellValueString.equalsIgnoreCase("True") || cellValueString.equals("1") ) {
                                table.setFieldValue(iRowOut, iColOut, new Integer(1), true);
                            }
                            else {
                                // Set to null
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DATE ) {
                            // Try to parse to a date/time string
                        	// TODO SAM 2016-03-10 Need to understand better how to handle time zone
                        	// Format is like Sun Jan 01 00:00:00 MST 2012
                            try {
                                dt = DateTime.parse(cellValueString);
                                Date d = dt.getDate(TimeZoneDefaultType.LOCAL);
                                //Message.printStatus(2, routine, "Original Excel date string \"" + cellValueString + "\" DateTime=\"" + dt + "\" Date=\"" + d + "\"");
                                table.setFieldValue(iRowOut, iColOut, d, true);
                            }
                            catch ( Exception e ) {
                                // Set to null
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DATETIME ) {
                            // Try to parse to a date/time string
                            try {
                                dt = DateTime.parse(cellValueString);
                                //Message.printStatus(2, routine, "Original Excel date string \"" + cellValueString + "\" DateTime=\"" + dt + "\"");
                                table.setFieldValue(iRowOut, iColOut, dt, true);
                            }
                            catch ( Exception e ) {
                                // Set to null
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                        else {
                            // Other cell types don't translate
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else if ( cellType == Cell.CELL_TYPE_NUMERIC ) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            if ( cellIsFormula ) {
                                // TODO SAM 2013-02-25 Does not seem to method to return date 
                                cellValueDate = null;
                            }
                            else {
                                cellValueDate = cell.getDateCellValue();
                            }
                            cellValueObject = cellValueDate; // For try/catch
                            if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DATE ) {
                                // date to date
                                table.setFieldValue(iRowOut, iColOut, cellValueDate, true);
                            }
                            else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DATETIME ) {
                                // date to date/time
                                table.setFieldValue(iRowOut, iColOut, new DateTime(cellValueDate), true);
                            }
                            else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                                // date to string
                                try {
                                    dt = new DateTime ( cellValueDate );
                                    table.setFieldValue(iRowOut, iColOut, dt.toString(), true);
                                }
                                catch ( Exception e ) {
                                    table.setFieldValue(iRowOut, iColOut, null, true);
                                }
                            }
                            else {
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                        else {
                            // Floating point value
                            if ( cellIsFormula ) {
                                cellValueDouble = formulaCellValue.getNumberValue();
                            }
                            else {
                                cellValueDouble = cell.getNumericCellValue();
                            }
                            cellValueObject = cellValueDouble; // For try/catch
                            if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DOUBLE ) {
                                // Double to double
                                table.setFieldValue(iRowOut, iColOut, new Double(cellValueDouble), true);
                            }
                            else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                                // Double to string - have to format number because Java will use exponential notation
                                table.setFieldValue(iRowOut, iColOut, StringUtil.formatString(cellValueDouble,numberFormat), true);
                            }
                            else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_INT ) {
                                // Double to integer - use an offset to help make sure integer value is correct
                                if ( cellValueDouble >= 0.0 ) {
                                    table.setFieldValue(iRowOut, iColOut, new Integer((int)(cellValueDouble + .0001)), true);
                                }
                                else {
                                    table.setFieldValue(iRowOut, iColOut, new Integer((int)(cellValueDouble - .0001)), true);
                                }
                            }
                            else {
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                    }
                    else if ( cellType == Cell.CELL_TYPE_BOOLEAN ) {
                        if ( cellIsFormula ) {
                            cellValueBoolean = formulaCellValue.getBooleanValue();
                        }
                        else {
                            cellValueBoolean = cell.getBooleanCellValue();
                        }
                        cellValueObject = cellValueBoolean; // For try/catch
                        if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_INT ) {
                            table.setFieldValue(iRowOut, iColOut, cellValueBoolean, true);
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                            // Just set
                            table.setFieldValue(iRowOut, iColOut, "" + cellValueBoolean, true);
                        }
                        else if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_DOUBLE ) {
                            if ( cellValueBoolean ) {
                                table.setFieldValue(iRowOut, iColOut, new Double(1.0), true);
                            }
                            else {
                                table.setFieldValue(iRowOut, iColOut, new Double(0.0), true);
                            }
                        }
                        else {
                            // Not able to convert
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else if ( cellType == Cell.CELL_TYPE_BLANK ) {
                        // Null works for all object types.  If truly a blank string in text cell, use "" as text
                        if ( !needToSkipRow && !tk.cellMatchesIncludeFilter(columnNames[iColOut],"",columnIncludeFilterList, true, true) ) {
                            // Add the row but will remove at the end after all columns are processed
                        	// Only change from false to true to handle previous columns setting to true
                        	needToSkipRow = true;
                        }
                        if ( !needToSkipRow && tk.cellMatchesExcludeFilter(columnNames[iColOut],"",columnExcludeFilterList, true, true) ) {
                            // Add the row but will remove at the end after all columns are processed
                        	// Only change from false to true since include takes precedence
                        	// In other words, if the include has set to true then the row is already excluded so don't undo that setting.
                        	needToSkipRow = true;
                        }
                        cellValueObject = "blank"; // For try/catch
                        if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                            table.setFieldValue(iRowOut, iColOut, "", true);
                        }
                        else {
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else if ( cellType == Cell.CELL_TYPE_ERROR ) {
                        if ( tableColumnTypes[iColOut] == TableField.DATA_TYPE_STRING ) {
                            table.setFieldValue(iRowOut, iColOut, "", true);
                        }
                        else {
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                        cellValueObject = "error"; // For try/catch
                    }
                    else {
                        table.setFieldValue(iRowOut, iColOut, null, true);
                    }
                }
                catch ( Exception e ) {
                    problems.add ( "Error processing Excel [" + iRow + "][" + iCol + "] = " +
                        cellValueObject + " (as string) skipping cell (" + e + ")." );
                    Message.printWarning(3,routine,e);
                }
                if ( Message.isDebugOn ) {
                	Message.printDebug(1,routine,"At end of processing column \"" + columnNames[iColOut] + "\", needToSkipRow="+needToSkipRow);
                }
            } // end of column loop
            if ( Message.isDebugOn ) {
            	Message.printDebug(1,routine,"At end of processing row, needToSkipRow="+needToSkipRow);
            }
            if ( needToSkipRow ) {
                // Because columns are added individually, need to remove rows that were added but should not have because
                // an exclude filter was matched
                table.deleteRecord(iRowOut);
                --iRowOut; // Will be incremented for next row
                continue;
            }
        }
    }
    catch ( Exception e ) {
        problems.add ( "Error reading workbook \"" + workbookFile + "\" (" + e + ")." );
        Message.printWarning(3,routine,e);
    }
    finally {
        // If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,"r",wb);
        }
        else {
            if ( inp != null ) {
                inp.close();
            }
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
    return table;
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
{	String routine = getClass().getSimpleName() + ".runCommandInternal", message = "";
	int warning_level = 2;
	int log_level = 3; // Level for non-user messages for log file.
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
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
	String InputFile = parameters.getValue ( "InputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    if ( (Worksheet != null) && !Worksheet.isEmpty() && (commandPhase == CommandPhaseType.RUN) && Worksheet.indexOf("${") >= 0 ) {
    	Worksheet = TSCommandProcessorUtil.expandParameterValue(processor, this, Worksheet);
    }
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
	String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	ExcelColumnNameRowType excelColumnNames = ExcelColumnNameRowType.COLUMN_N; // Default
	if ( (ExcelColumnNames != null) && !ExcelColumnNames.isEmpty() ) {
	    excelColumnNames = ExcelColumnNameRowType.valueOfIgnoreCase(ExcelColumnNames);  
	}
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    StringFilterList columnIncludeFilterList = null;
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.length() > 0) && (ColumnIncludeFilters.indexOf(":") > 0) ) {
        columnIncludeFilterList = new StringFilterList();
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnIncludeFilters, ",", 0 );
        // Now break pairs and put in list
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            String tableColumn = parts[0].trim();
            String pattern = "";
            if ( parts.length > 1 ) {
                // Use upper-case to facilitate case-independent comparisons, and replace * globbing with internal Java notation
                pattern = parts[1].trim().replace("*", ".*");
            }
            columnIncludeFilterList.add(tableColumn, pattern );
        }
    }
    String ColumnExcludeFilters = parameters.getValue ( "ColumnExcludeFilters" );
    StringFilterList columnExcludeFilterList = null;
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.length() > 0) && (ColumnExcludeFilters.indexOf(":") > 0) ) {
        columnExcludeFilterList = new StringFilterList();
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnExcludeFilters, ",", 0 );
        // Now break pairs and put in hashtable
        // Case is checked when processing the filter
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            String tableColumn = parts[0].trim();
            String pattern = "";
            if ( parts.length > 1 ) {
                // Replace * globbing with internal Java notation
                pattern = parts[1].trim().replace("*", ".*");
            }
            columnExcludeFilterList.add(tableColumn, pattern );
        }
    }
	String Comment = parameters.getValue ( "Comment" );
	String comment = null;
	if ( (Comment != null) && Comment.length() > 0 ) {
	    comment = Comment;
	}
	String ExcelDoubleColumns = parameters.getValue ( "ExcelDoubleColumns" );
	String [] excelDoubleColumns = null;
	if ( (ExcelDoubleColumns != null) && !ExcelDoubleColumns.equals("") ) {
		excelDoubleColumns = ExcelDoubleColumns.split(",");
	    for ( int i = 0; i < excelDoubleColumns.length; i++ ) {
	    	excelDoubleColumns[i] = excelDoubleColumns[i].trim();
	    }
	}
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
    String ExcelTextColumns = parameters.getValue ( "ExcelTextColumns" );
    String [] excelTextColumns = null;
    if ( (ExcelTextColumns != null) && !ExcelTextColumns.equals("") ) {
    	excelTextColumns = ExcelTextColumns.split(",");
        for ( int i = 0; i < excelTextColumns.length; i++ ) {
        	excelTextColumns[i] = excelTextColumns[i].trim();
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
    String RowCountProperty = parameters.getValue ( "RowCountProperty" );
    if ( (RowCountProperty != null) && !RowCountProperty.isEmpty() && (commandPhase == CommandPhaseType.RUN) && RowCountProperty.indexOf("${") >= 0 ) {
    	RowCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, RowCountProperty);
    }
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase(_True) ) {
        keepOpen = true;
    }

	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
        	TSCommandProcessorUtil.expandParameterValue(processor,this,InputFile)) );
	if ( !IOUtil.fileExists(InputFile_full) ) {
		if ( commandPhase == CommandPhaseType.DISCOVERY ) {
			if ( InputFile_full.indexOf("${") < 0 ) {
				// File does not exist because it may be created during run - ${Property} is ok regardless
				// Do not increment the warning counter because the discovery table will not be set up below
				message += "\nThe Excel workbook file \"" + InputFile_full + "\" does not exist.";
		        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
		            message, "Verify that the Excel workbook file exists.  May be OK if created during processing." ) );
			}
		}
		else {
			message += "\nThe Excel workbook file \"" + InputFile_full + "\" does not exist.";
			++warning_count;
	        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
	            message, "Verify that the Excel workbook file exists." ) );
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

	DataTable table = null;
	List<String> problems = new Vector<String>();
	try {
	    if ( commandPhase == CommandPhaseType.RUN ) {
            table = readTableFromExcelFile ( InputFile_full, Worksheet, keepOpen,
                ExcelAddress, ExcelNamedRange, ExcelTableName, excelColumnNames,
                columnIncludeFilterList, columnExcludeFilterList, comment,
                excelDoubleColumns, excelIntegerColumns, excelDateTimeColumns, excelTextColumns,
                numberPrecision, readAllAsText, problems );
            for ( String problem: problems ) {
                Message.printWarning ( 3, routine, problem );
                message = "Error reading from Excel: " + problem;
                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Check the log file for exceptions." ) );
            }
            // Set the table identifier...
            if ( table == null ) {
                // Create an empty table to have something in output for user
                table = new DataTable();
            }
            table.setTableID ( TableID );
    	    // Set the property indicating the number of rows in the table
            if ( (RowCountProperty != null) && !RowCountProperty.isEmpty() ) {
                int rowCount = table.getNumberOfRecords();
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "PropertyName", RowCountProperty );
                request_params.setUsingObject ( "PropertyValue", new Integer(rowCount) );
                try {
                    processor.processRequest( "SetProperty", request_params);
                }
                catch ( Exception e ) {
                    message = "Error requesting SetProperty(Property=\"" + RowCountProperty + "\") from processor.";
                    Message.printWarning(log_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                    status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
                }
            }
	    }
	    else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
	        // Create an empty table.
	        table = new DataTable();
	        table.setTableID ( TableID );
	        setDiscoveryTable(table);
	    }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error reading table from Excel workbook file \"" + InputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
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
    
    if ( commandPhase == CommandPhaseType.RUN ) {
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
Set the first data row (0+).
*/
private void setFirstDataRow ( int row )
{
    __firstDataRow = row;
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
	String Worksheet = props.getValue( "Worksheet" );
	String ExcelAddress = props.getValue("ExcelAddress");
	String ExcelNamedRange = props.getValue("ExcelNamedRange");
	String ExcelTableName = props.getValue("ExcelTableName");
	String ExcelColumnNames = props.getValue("ExcelColumnNames");
	String ColumnIncludeFilters = props.getValue("ColumnIncludeFilters");
	String ColumnExcludeFilters = props.getValue("ColumnExcludeFilters");
	String Comment = props.getValue("Comment");
	String ExcelDoubleColumns = props.getValue("ExcelDoubleColumns");
	String ExcelIntegerColumns = props.getValue("ExcelIntegerColumns");
	String ExcelDateTimeColumns = props.getValue("ExcelDateTimeColumns");
	String ExcelTextColumns = props.getValue("ExcelTextColumns");
	String NumberPrecision = props.getValue("NumberPrecision");
	String ReadAllAsText = props.getValue("ReadAllAsText");
	String RowCountProperty = props.getValue( "RowCountProperty" );
	String KeepOpen = props.getValue("KeepOpen");
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (NumberPrecision != null) && (NumberPrecision.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NumberPrecision=" + NumberPrecision );
    }
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
	if ( (ExcelAddress != null) && (ExcelAddress.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelAddress=\"" + ExcelAddress + "\"" );
	}
	if ( (ExcelNamedRange != null) && (ExcelNamedRange.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelNamedRange=\"" + ExcelNamedRange + "\"" );
	}
	if ( (ExcelTableName != null) && (ExcelTableName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelTableName=\"" + ExcelTableName + "\"" );
	}
    if ( (ExcelColumnNames != null) && (ExcelColumnNames.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelColumnNames=" + ExcelColumnNames );
    }
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnIncludeFilters=\"" + ColumnIncludeFilters + "\"" );
    }
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnExcludeFilters=\"" + ColumnExcludeFilters + "\"" );
    }
    if ( (Comment != null) && (Comment.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Comment=\"" + Comment + "\"" );
    }
    if ( (ExcelDoubleColumns != null) && (ExcelDoubleColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelDoubleColumns=\"" + ExcelDoubleColumns + "\"" );
    }
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
    if ( (ExcelTextColumns != null) && (ExcelTextColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelTextColumns=\"" + ExcelTextColumns + "\"" );
    }
    if ( (ReadAllAsText != null) && (ReadAllAsText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ReadAllAsText=" + ReadAllAsText );
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
