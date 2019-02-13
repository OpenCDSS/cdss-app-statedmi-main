// SetExcelCell_Command - This class initializes, checks, and runs the SetExcelCell() command, using Apache POI.

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the SetExcelCell() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class SetExcelCell_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for ExcelColumnNames parameter.
*/
protected final String _FirstRowInRange = "FirstRowInRange";
protected final String _None = "None";
protected final String _RowBeforeRange = "RowBeforeRange";

/**
Possible values for WriteAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Possible values for ColumnWidths, CellTypes, ColumnDecimalPlaces parameter.
*/
protected final String _Auto = "Auto";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public SetExcelCell_Command ()
{	super();
	setCommandName ( "SetExcelCell" );
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
{	String OutputFile = parameters.getValue ( "OutputFile" );
    String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	String CommentWidth = parameters.getValue ( "CommentWidth" );
	String CommentHeight = parameters.getValue ( "CommentHeight" );
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
	
    if ( ExcelColumnNames != null &&
        !ExcelColumnNames.equalsIgnoreCase(_FirstRowInRange) && !ExcelColumnNames.equalsIgnoreCase(_None) &&
        !ExcelColumnNames.equalsIgnoreCase(_RowBeforeRange) && !ExcelColumnNames.equalsIgnoreCase("")) {
        message = "ExcelColumnNames is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "ExcelColumnNames must be " + _FirstRowInRange +
                ", " + _None + " (default), or " + _RowBeforeRange ) );
    }
    
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.equalsIgnoreCase("") ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }
    
    if ( (CommentWidth != null) && !CommentWidth.isEmpty() && !CommentWidth.equalsIgnoreCase(_Auto) && 
        !StringUtil.isInteger(CommentWidth) ) {
        message = "Comment width is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify comment width as the number of columns or " + _Auto + " (default)." ) );
    }
    
    if ( (CommentHeight != null) && !CommentHeight.isEmpty() && !CommentHeight.equalsIgnoreCase(_Auto) && 
        !StringUtil.isInteger(CommentHeight) ) {
        message = "Comment height is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify comment height as the number of rows or " + _Auto + " (default)." ) );
    }

	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(13);
    validList.add ( "Value" );
    validList.add ( "PropertyName" );
    validList.add ( "OutputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "ExcelAddress" );
    validList.add ( "ExcelNamedRange" );
    validList.add ( "ExcelTableName" );
    validList.add ( "ExcelColumnNames" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "NumberPrecision" );
    validList.add ( "WriteAllAsText" );
    validList.add ( "ColumnNamedRanges" );
    validList.add ( "KeepOpen" );
    validList.add ( "IncludeColumns" );
    validList.add ( "ExcludeColumns" );
    validList.add ( "Rows" );
    validList.add ( "Author" );
    validList.add ( "Comment" );
    validList.add ( "CommentWidth" );
    validList.add ( "CommentHeight" );
    validList.add ( "ColumnCellTypes" );
    validList.add ( "ColumnWidths" );
    validList.add ( "ColumnDecimalPlaces" );
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
	return (new SetExcelCell_JDialog ( parent, this, tableIDChoices )).ok();
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

/**
Parse the command string into a PropList of parameters.  Use this to translate old syntax to new.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{
    // First parse as usual
    super.parseCommand(command_string);
    PropList props = getCommandParameters();
    // Translate NumberPrecision=N to ColumnDecimalPlaces=Default:N
    String prop = props.getValue("NumberPrecision");
    if ( prop != null ) {
        String ColumnDecimalPlaces = props.getValue ( "ColumnDecimalPlaces" );
        StringDictionary columnDecimalPlaces = new StringDictionary(ColumnDecimalPlaces,":",",");
        LinkedHashMap<String,String> hm = columnDecimalPlaces.getLinkedHashMap();
        String prop2 = hm.get("Default");
        if ( prop2 == null ) {
            // Set the property
            hm.put("Default",prop2);
            props.set("ColumnDecimalPlaces",columnDecimalPlaces.toString());
        }
        props.unSet("NumberPrecision");
    }
    // WriteAllAsText=True|False to ColumnCellType=Default:Text
    prop = props.getValue("WriteAllAsText");
    if ( prop != null ) {
        String ColumnCellTypes = props.getValue ( "ColumnCellTypes" );
        StringDictionary columnCellTypes = new StringDictionary(ColumnCellTypes,":",",");
        LinkedHashMap<String,String> hm = columnCellTypes.getLinkedHashMap();
        String prop2 = hm.get("Default");
        if ( prop2 == null ) {
            // Set the property
            hm.put("Default","Text");
            props.set("ColumnCellTypes",columnCellTypes.toString());
        }
        props.unSet("NumberPrecision");
        props.unSet("WriteAllAsText");
    }
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException
{	String routine = getClass().getSimpleName() + ".runCommand",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    status.clearLog(commandPhase);
    
    // Clear the output file
    setOutputFile ( null );

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
	
	String Value = parameters.getValue ( "Value" );
	String PropertyName = parameters.getValue ( "PropertyName" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase("True") ) {
        keepOpen = true;
    }
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String [] includeColumns = new String[0];
    if ( (IncludeColumns != null) && !IncludeColumns.isEmpty() ) {
        // Use the provided columns
        includeColumns = IncludeColumns.split(",");
        for ( int i = 0; i < includeColumns.length; i++ ) {
            includeColumns[i] = TSCommandProcessorUtil.expandParameterValue(processor,this,includeColumns[i].trim());
        }
    }
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
    String [] excludeColumns = new String[0];
    if ( (ExcludeColumns != null) && !ExcludeColumns.isEmpty() ) {
        // Use the specified columns
        excludeColumns = ExcludeColumns.split(",");
        for ( int i = 0; i < excludeColumns.length; i++ ) {
            excludeColumns[i] = TSCommandProcessorUtil.expandParameterValue(processor,this,excludeColumns[i].trim());
        }
    }
    String Rows = parameters.getValue ( "Rows" );
    int [] rows = null;
    if ( (Rows != null) && !Rows.isEmpty() ) {
        // Use the specified rows
        String [] parts = Rows.split(",");
        rows = new int[parts.length];
        for ( int i = 0; i < parts.length; i++ ) {
        	try {
        		String part = parts[i].trim();
        		rows[i] = Integer.parseInt(part);
        		rows[i] = rows[i] - 1; // User-specified rows are 1+ but internally use 0+
        	}
        	catch ( NumberFormatException e ) {
        		rows[i] = -1;
        	}
        }
        // TODO SAM 2015-03-01 Clean up to remove -1 that could cause messages later
    }
    String Author = parameters.getValue ( "Author" );
    String Comment = parameters.getValue ( "Comment" );
    String CommentWidth = parameters.getValue ( "CommentWidth" );
    String CommentHeight = parameters.getValue ( "CommentHeight" );
    //String ColumnCellTypes = parameters.getValue ( "ColumnCellTypes" );
    //StringDictionary columnCellTypes = new StringDictionary(ColumnCellTypes,":",",");
    //String ColumnWidths = parameters.getValue ( "ColumnWidths" );
    //StringDictionary columnWidths = new StringDictionary(ColumnWidths,":",",");
    //String ColumnDecimalPlaces = parameters.getValue ( "ColumnDecimalPlaces" );
    //StringDictionary columnDecimalPlaces = new StringDictionary(ColumnDecimalPlaces,":",",");
	
	// Get the table to process

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
        setExcelCell ( (StateDMI_Processor)processor,
        	Value, PropertyName,
        	OutputFile_full, Worksheet,
            ExcelAddress, ExcelNamedRange, ExcelTableName, keepOpen,
            includeColumns, excludeColumns, rows,
            Author, Comment, CommentWidth, CommentHeight,
            problems );
        for ( String problem: problems ) {
            Message.printWarning ( 3, routine, problem );
            message = "Error setting Excel values: " + problem;
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Check the log file for exceptions." ) );
        }
        // Save the output file name...
        setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error setting Excel cell values for workbook file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check error messages and log file." ) );
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
Set cell values in an Excel worksheet using Excel address block or row/column matching information.
@param value the value to write to the cell
@param propertyName the name of the property whose value should be written to the cell
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name
@param keepOpen if True, the Excel workbook will be kept open in write mode and not written
@param includeColumns an array of Excel column names to include in the set
@param excludeColumns an array of Excel column names to exclude in the set
@param rows an array of row numbers (0+) to set
@param author the author to use when setting the comment
@param comment the comment string
@param comment comment width in columns
@param comment comment height in rows
@param problems list of problems encountered during read, for formatted logging in calling code
*/
private void setExcelCell ( StateDMI_Processor processor,
	String value, String propertyName,
	String workbookFile, String sheetName,
    String excelAddress, String excelNamedRange, String excelTableName, boolean keepOpen,
    String [] includeColumns, String [] excludeColumns, int [] rows,
    String author, String comment, String commentWidth, String commentHeight,
    List<String> problems )
throws FileNotFoundException, IOException
{   String routine = "SetExcelCell_Command.setExcelCell";
    
    Workbook wb = null;
    InputStream inp = null;
    boolean doSetComment = false;
    try {
    	// Set some booleans to make processing faster below
    	if ( (comment != null) && !comment.isEmpty() ) {
    		doSetComment = true;
    	}
    	// Create a toolkit for utility functions.
    	ExcelToolkit tk = new ExcelToolkit();
        // See if an open workbook by the same name exists
        WorkbookFileMetadata wbMeta = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wbMeta != null ) {
        	wb = wbMeta.getWorkbook();
        }
        if ( wb == null ) {
            // Workbook is not open in memory so Open the file
            try {
                inp = new FileInputStream(workbookFile);
            }
            catch ( IOException e ) {
                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            try {
                // Open an existing workbook if it exists...
                wb = WorkbookFactory.create(inp);
            }
            catch ( InvalidFormatException e ) {
                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            finally {
                // Close the sheet because will need to write it below and close
                if ( !keepOpen ) {
                    inp.close();
                }
            }
        }
        Sheet sheet = null;
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
            sheet = wb.getSheetAt(0);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets" );
                return;
            }
        }
        else {
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include worksheet named \"" + sheetName + "\"" );
                return;
            }
        }
        if ( ((excelAddress != null) && !excelAddress.isEmpty()) ||
        	((excelNamedRange != null) && !excelNamedRange.isEmpty()) ||
        	((excelTableName != null) && !excelTableName.isEmpty()) ) {
	        // Get the contiguous block of data to process by evaluating user input
        	/*
	        AreaReference area = getAreaReference ( wb, sheet, excelAddress, excelNamedRange, excelTableName );
	        if ( area == null ) {
	            problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
	            return;
	        }
	        Message.printStatus(2,routine,"Excel address block to write: " + area );
	        // Get the upper left row/column to write from the addresses
	        // Upper left, including column headings if included
	        int colOutStart = 0;
	        int rowOutStart = 0;
	        int rowOutColumnNames = 0;
	        if ( area != null ) {
	            colOutStart = area.getFirstCell().getCol();
	            rowOutStart = area.getFirstCell().getRow();
	            rowOutColumnNames = rowOutStart;
	        }
	        // Upper left, for first data row (assume no column headings and adjust accordingly below)
	        int colOutDataStart = colOutStart;
	        int rowOutDataStart = rowOutStart;
	        int colOutDataEnd = colOutDataStart + cols - 1; // 0-index
	        int rowOutDataEnd = rowOutDataStart + rows - 1; // 0-index
	        // Adjust the data locations based on whether column headings are in the block
	        boolean doWriteColumnNames = true;
	        if ( excelColumnNames.equalsIgnoreCase(_FirstRowInRange) ) {
	            ++rowOutDataStart;
	            ++rowOutDataEnd;
	        }
	        else if ( excelColumnNames.equalsIgnoreCase(_RowBeforeRange) ) {
	            --rowOutColumnNames;
	            // OK as is
	        }
	        else if ( excelColumnNames.equalsIgnoreCase(_None) ) {
	            // OK as is
	            doWriteColumnNames = false;
	        }
	        else {
	            problems.add ( "Unknown ExcelColumnNames value \"" + excelColumnNames +
	                "\" - assuming no column headings but may not be correct" );
	        }
	        */
        }
        else {
        	Message.printStatus(2,routine,"Processing comments" );
        	// Set using the column names and row numbers
        	// Determine which Excel columns to process
        	// TODO SAM 2015-03-01 Need to support column row specified relative to block range, as per some other commands.
        	int [] includeColumnsNum = new int[includeColumns.length];
        	for ( int icol = 0; icol < includeColumns.length; icol++ ) {
        		includeColumnsNum[icol] = tk.findColumn(wb,sheet,includeColumns[icol],0);
        		Message.printStatus(2,routine,"Column \"" + includeColumns[icol] + "\" found as number " + includeColumnsNum[icol] );
        	}
        	// Remove other columns by setting the column number to -1
        	for ( int icol = 0; icol < excludeColumns.length; icol++ ) {
        		int excludeColumnNum = tk.findColumn(wb,sheet,excludeColumns[icol],0);
        		if ( excludeColumnNum >= 0 ) {
        			for ( int jcol = 0; jcol < includeColumns.length; jcol++ ) {
        				if ( includeColumnsNum[jcol] == excludeColumnNum ) {
        					includeColumnsNum[jcol] = -1;
        					break;
        				}
        			}
        		}
        	}
        	Row row;
        	Cell cell;
        	int commentWidthInt = -1;
        	int commentHeightInt = -1;
        	if ( StringUtil.isInteger(commentWidth) ) {
        		commentWidthInt = Integer.parseInt(commentWidth);
        	}
        	if ( StringUtil.isInteger(commentHeight) ) {
        		commentHeightInt = Integer.parseInt(commentHeight);
        	}
        	for ( int icol = 0; icol < includeColumns.length; icol++ ) {
        		if ( includeColumnsNum[icol] < 0 ) {
        			continue;
        		}
        		for ( int irow = 0; irow < rows.length; irow++ ) {
        			Message.printStatus(2,routine,"Processing comment at icol " + icol + " irow " + irow );
            		if ( rows[icol] < 0 ) {
            			continue;
            		}
        			// Set the cell value or comment
        			// TODO SAM 2015-03-01 currently only cell can be set - need to enable setting values
        			if ( doSetComment ) {
        				// Get the heading row - it must already exist
        				row = sheet.getRow(rows[irow]);
        				if ( row == null ) {
        					continue;
        				}
        				// Get the cell - it must already exist
        				cell = row.getCell(includeColumnsNum[icol]);
        				if ( cell == null ) {
        					continue;
        				}
        				// Format the comment string here
        				// TODO SAM 2015-03-01 in the future may support properties like ${tablecol:columnname}
        				String commentString = TSCommandProcessorUtil.expandParameterValue(processor,this,comment);
        				Message.printStatus(2,routine,"Trying to set comment \"" + commentString + "\" at col " + includeColumnsNum[icol] + " row " + rows[irow] );
        				tk.setCellComment(wb, sheet, cell, commentString, author, commentWidthInt, commentHeightInt);
        			}
        		}
        	}
        }
    }
    catch ( Exception e ) {
        problems.add ( "Error setting cell(s) for workbook \"" + workbookFile + "\" (" + e + ")." );
        Message.printWarning(3,routine,e);
    }
    finally {
        // Now write the workbook and close.  If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,"w",wb);
        }
        else {
            // Close the workbook and remove from the cache
            wb.setForceFormulaRecalculation(true); // Will cause Excel to recalculate formulas when it opens
            FileOutputStream fout = new FileOutputStream(workbookFile);
            wb.write(fout);
            fout.close();
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String OutputFile = props.getValue( "OutputFile" );
	String Worksheet = props.getValue( "Worksheet" );
	String ExcelAddress = props.getValue("ExcelAddress");
	String ExcelNamedRange = props.getValue("ExcelNamedRange");
	String ExcelTableName = props.getValue("ExcelTableName");
	//String ExcelColumnNames = props.getValue("ExcelColumnNames");
	String KeepOpen = props.getValue("KeepOpen");
	String IncludeColumns = props.getValue("IncludeColumns");
	String ExcludeColumns = props.getValue("ExcludeColumns");
	String Rows = props.getValue("Rows");
    String Value = props.getValue( "Value" );
    String PropertyName = props.getValue( "PropertyName" );
	String Author = props.getValue("Author");
	String Comment = props.getValue("Comment");
	String CommentWidth = props.getValue("CommentWidth");
	String CommentHeight = props.getValue("CommentHeight");
	//String ColumnCellTypes = props.getValue("ColumnCellTypes");
	//String ColumnWidths = props.getValue("ColumnWidths");
	//String ColumnDecimalPlaces = props.getValue("ColumnDecimalPlaces");
	StringBuffer b = new StringBuffer ();
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
    //if ( (ExcelColumnNames != null) && (ExcelColumnNames.length() > 0) ) {
    //    if ( b.length() > 0 ) {
    //        b.append ( "," );
    //    }
    //    b.append ( "ExcelColumnNames=" + ExcelColumnNames );
    //}
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (IncludeColumns != null) && (IncludeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeColumns=\"" + IncludeColumns + "\"");
    }
    if ( (ExcludeColumns != null) && (ExcludeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeColumns=\"" + ExcludeColumns + "\"");
    }
    if ( (Rows != null) && (Rows.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Rows=\"" + Rows + "\"");
    }
    if ( (Value != null) && (Value.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Value=\"" + Value + "\"" );
    }
    if ( (PropertyName != null) && (PropertyName.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PropertyName=\"" + PropertyName + "\"" );
    }
    if ( (Author != null) && (Author.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Author=\"" + Author + "\"");
    }
    if ( (Comment != null) && (Comment.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Comment=\"" + Comment + "\"");
    }
    if ( (CommentWidth != null) && (CommentWidth.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CommentWidth=\"" + CommentWidth + "\"");
    }
    if ( (CommentHeight != null) && (CommentHeight.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CommentHeight=\"" + CommentHeight + "\"");
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
