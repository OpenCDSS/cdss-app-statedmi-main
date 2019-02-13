// WriteTableToExcel_Command - This class initializes, checks, and runs the WriteTableToExcel() command, using Apache POI.

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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.commands.spreadsheet.ExcelToolkit;
import rti.tscommandprocessor.commands.spreadsheet.ExcelUtil;
import rti.tscommandprocessor.commands.spreadsheet.TableConditionAndStyleManager;
import rti.tscommandprocessor.commands.spreadsheet.WorkbookFileMetadata;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the WriteTableToExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class WriteTableToExcel_Command extends AbstractCommand implements Command, FileGenerator
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
Maximum row and column written.
*/
private int outputMinRow = 0;
private int outputMinColumn = 0;
private int outputMaxRow = 0;
private int outputMaxColumn = 0;

/**
Constructor.
*/
public WriteTableToExcel_Command ()
{	super();
	setCommandName ( "WriteTableToExcel" );
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
    String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	//String working_dir = null;
	
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

	if ( (OutputFile == null) || OutputFile.isEmpty() ) {
        message = "The Excel output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing Excel output file." ) );
	}
	/** TODO SAM 2014-01-12 Evaluate whether to only do this check at run-time
	else if ( OutputFile.indexOf("${") < 0 ) {
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
        !ExcelColumnNames.equalsIgnoreCase(_RowBeforeRange) && !ExcelColumnNames.isEmpty()) {
        message = "ExcelColumnNames is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "ExcelColumnNames must be " + _FirstRowInRange +
                ", " + _None + " (default), or " + _RowBeforeRange ) );
    }
    
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.isEmpty() ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }

	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(22);
    validList.add ( "TableID" );
    validList.add ( "IncludeColumns" );
    validList.add ( "ExcludeColumns" );
    validList.add ( "OutputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "ExcelAddress" );
    validList.add ( "ExcelNamedRange" );
    validList.add ( "ExcelTableName" );
    validList.add ( "ExcelColumnNames" );
    validList.add ( "ColumnIncludeFilters" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "NumberPrecision" );
    validList.add ( "WriteAllAsText" );
    validList.add ( "ColumnNamedRanges" );
    validList.add ( "KeepOpen" );
    validList.add ( "ColumnCellTypes" );
    validList.add ( "ColumnWidths" );
    validList.add ( "ColumnDecimalPlaces" );
    validList.add ( "ConditionTableID" );
    validList.add ( "StyleTableID" );
    validList.add ( "LegendWorksheet" );
    validList.add ( "LegendAddress" );
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
	return (new WriteTableToExcel_JDialog ( parent, this, tableIDChoices )).ok();
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

// TODO SAM 2015-02-28 Need to insert into a DataTable toolkit class
/**
Determine whether a table row is matched for output.
@param table table to check
@param irow row [0+] in table to check
@param columnIncludeFiltersNumbers column numbers to check for inclusion
@param columnIncludeFiltersGlobs glob-style wildcards to check for inclusion filter
@param columnExcludeFiltersNumbers column numbers to check for exclusion
@param columnExcludeFiltersGlobs glob-style wildcards to check for exclusion filter
@param errors list of errors to propagate to calling code
@return true if row should be included in output, false if not
*/
private boolean isTableRowIncluded ( DataTable table, int irow,
	int [] columnIncludeFiltersNumbers, String [] columnIncludeFiltersGlobs,
	int [] columnExcludeFiltersNumbers, String [] columnExcludeFiltersGlobs,
	List<String> errors )
{   boolean filterMatches = true; // Default is match
	Object o;
	String s;
    if ( columnIncludeFiltersNumbers.length > 0 ) {
        // Filters can be done on any columns so loop through to see if row matches
        for ( int icol = 0; icol < columnIncludeFiltersNumbers.length; icol++ ) {
            if ( columnIncludeFiltersNumbers[icol] < 0 ) {
                filterMatches = false;
                break;
            }
            try {
                o = table.getFieldValue(irow, columnIncludeFiltersNumbers[icol]);
                if ( o == null ) {
                    filterMatches = false;
                    break; // Don't include nulls when checking values
                }
                s = ("" + o).toUpperCase();
                if ( !s.matches(columnIncludeFiltersGlobs[icol]) ) {
                    // A filter did not match so don't copy the record
                    filterMatches = false;
                    break;
                }
            }
            catch ( Exception e ) {
                errors.add("Error getting table data for [" + irow + "][" +
                	columnIncludeFiltersNumbers[icol] + "] (" + e + ")." );
            }
        }
        if ( !filterMatches ) {
            // Skip the record.
            return false;
        }
    }
    if ( columnExcludeFiltersNumbers.length > 0 ) {
        int matchesCount = 0;
        // Filters can be done on any columns so loop through to see if row matches
        for ( int icol = 0; icol < columnExcludeFiltersNumbers.length; icol++ ) {
            if ( columnExcludeFiltersNumbers[icol] < 0 ) {
                // Can't do filter so don't try
                break;
            }
            try {
                o = table.getFieldValue(irow, columnExcludeFiltersNumbers[icol]);
                //Message.printStatus(2,"","Got cell object " + o );
                if ( o == null ) {
                	if ( columnExcludeFiltersGlobs[icol].isEmpty() ) {
                		// Trying to match blank cells
                		++matchesCount;
                	}
                	else { // Don't include nulls when checking values
                		break;
                	}
                }
                s = ("" + o).toUpperCase();
                //Message.printStatus(2,"","Comparing table value \"" + s + "\" with exclude filter \"" + columnExcludeFiltersGlobs[icol] + "\"");
                if ( s.matches(columnExcludeFiltersGlobs[icol]) ) {
                    // A filter matched so don't copy the record
                	//Message.printStatus(2,"","Exclude filter matches");
                    ++matchesCount;
                }
            }
            catch ( Exception e ) {
            	errors.add("Error getting table data for [" + irow + "][" +
                   	columnExcludeFiltersNumbers[icol] + "] (" + e + ")." );
            }
        }
        //Message.printStatus(2,"","matchesCount=" + matchesCount + " excludeFiltersLength=" +  columnExcludeFiltersNumbers.length );
        if ( matchesCount == columnExcludeFiltersNumbers.length ) {
            // Skip the record since all exclude filters were matched
        	//Message.printStatus(2,"","Skipping since all exclude filters matched");
            return false;
        }
    }
    return filterMatches;
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
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;

	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
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
    
    // Clear the output file
    setOutputFile ( null );

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String [] includeColumns = null;
    if ( (IncludeColumns != null) && !IncludeColumns.isEmpty() ) {
        // Use the provided columns
        includeColumns = IncludeColumns.split(",");
        for ( int i = 0; i < includeColumns.length; i++ ) {
            includeColumns[i] = includeColumns[i].trim();
        }
    }
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
    String [] excludeColumns = null;
    if ( (ExcludeColumns != null) && !ExcludeColumns.isEmpty() ) {
        // Use the provided columns
        excludeColumns = ExcludeColumns.split(",");
        for ( int i = 0; i < excludeColumns.length; i++ ) {
            excludeColumns[i] = excludeColumns[i].trim();
        }
    }
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    if ( (Worksheet != null) && !Worksheet.isEmpty() && (commandPhase == CommandPhaseType.RUN) && Worksheet.indexOf("${") >= 0 ) {
    	Worksheet = TSCommandProcessorUtil.expandParameterValue(processor, this, Worksheet);
    }
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
	String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	if ( (ExcelColumnNames == null) || ExcelColumnNames.equals("") ) {
	    ExcelColumnNames = _None; // Default
	}
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    StringDictionary columnIncludeFilters = new StringDictionary(ColumnIncludeFilters,":",",");
    // Expand the filter information
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnIncludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            map.remove(key);
        }
    }
    String ColumnExcludeFilters = parameters.getValue ( "ColumnExcludeFilters" );
    StringDictionary columnExcludeFilters = new StringDictionary(ColumnExcludeFilters,":",",");
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            map.remove(key);
        }
    }
    String ColumnNamedRanges = parameters.getValue ( "ColumnNamedRanges" );
    Hashtable<String,String> columnNamedRanges = new Hashtable<String,String>();
    if ( (ColumnNamedRanges != null) && (ColumnNamedRanges.length() > 0) && (ColumnNamedRanges.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnNamedRanges, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            columnNamedRanges.put(parts[0].trim(), parts[1].trim() );
        }
    }
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase("True") ) {
        keepOpen = true;
    }
    String ColumnCellTypes = parameters.getValue ( "ColumnCellTypes" );
    StringDictionary columnCellTypes = new StringDictionary(ColumnCellTypes,":",",");
    String ColumnWidths = parameters.getValue ( "ColumnWidths" );
    StringDictionary columnWidths = new StringDictionary(ColumnWidths,":",",");
    String ColumnDecimalPlaces = parameters.getValue ( "ColumnDecimalPlaces" );
    StringDictionary columnDecimalPlaces = new StringDictionary(ColumnDecimalPlaces,":",",");
    String ConditionTableID = parameters.getValue ( "ConditionTableID" );
    if ( (ConditionTableID != null) && !ConditionTableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && ConditionTableID.indexOf("${") >= 0 ) {
    	ConditionTableID = TSCommandProcessorUtil.expandParameterValue(processor, this, ConditionTableID);
    }
    String StyleTableID = parameters.getValue ( "StyleTableID" );
    if ( (StyleTableID != null) && !StyleTableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && StyleTableID.indexOf("${") >= 0 ) {
    	StyleTableID = TSCommandProcessorUtil.expandParameterValue(processor, this, StyleTableID);
    }
    // Don't expand because ${Property} is expected to be internal
    String LegendAddress = parameters.getValue ( "LegendAddress" );
    String LegendWorksheet = parameters.getValue ( "LegendWorksheet" );
    if ( (LegendWorksheet != null) && !LegendWorksheet.isEmpty() && (commandPhase == CommandPhaseType.RUN) && LegendWorksheet.indexOf("${") >= 0 ) {
    	LegendWorksheet = TSCommandProcessorUtil.expandParameterValue(processor, this, LegendWorksheet);
    }
	
	// Get the output table to process
	
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
    
	// Get the style table
	
    DataTable styleTable = null;
    if ( (StyleTableID != null) && !StyleTableID.isEmpty() ) {
	    request_params = new PropList ( "" );
	    request_params.set ( "TableID", StyleTableID );
	    try {
	        bean = processor.processRequest( "GetTable", request_params);
	    }
	    catch ( Exception e ) {
	        message = "Error requesting GetTable(TableID=\"" + StyleTableID + "\") from processor.";
	        Message.printWarning(warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Report problem to software support." ) );
	    }
	    bean_PropList = bean.getResultsPropList();
	    o_Table = bean_PropList.getContents ( "Table" );
	    if ( o_Table == null ) {
	        message = "Unable to find table to process using TableID=\"" + StyleTableID + "\".";
	        Message.printWarning ( warning_level,
	        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Verify that a table exists with the requested ID." ) );
	    }
	    else {
	        styleTable = (DataTable)o_Table;
	    }
    }
	    
	// Get the condition table
	
    DataTable conditionTable = null;
    if ( (ConditionTableID != null) && !ConditionTableID.isEmpty() ) {
	    request_params = new PropList ( "" );
	    request_params.set ( "TableID", ConditionTableID );
	    try {
	        bean = processor.processRequest( "GetTable", request_params);
	    }
	    catch ( Exception e ) {
	        message = "Error requesting GetTable(TableID=\"" + ConditionTableID + "\") from processor.";
	        Message.printWarning(warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Report problem to software support." ) );
	    }
	    bean_PropList = bean.getResultsPropList();
	    o_Table = bean_PropList.getContents ( "Table" );
	    if ( o_Table == null ) {
	        message = "Unable to find table to process using TableID=\"" + ConditionTableID + "\".";
	        Message.printWarning ( warning_level,
	        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Verify that a table exists with the requested ID." ) );
	    }
	    else {
	        conditionTable = (DataTable)o_Table;
	    }
    }
	    
    // Get the worksheet to write

	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
        	TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)) );
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
	    if ( columnNamedRanges != null ) {
    	    Enumeration<String> keys = columnNamedRanges.keys();
            String key = null;
            while ( keys.hasMoreElements() ) {
                key = keys.nextElement(); // Column name
                // Find the table column
                if ( table.getFieldIndex(key) < 0 ) {
                    message += "\nThe column \"" + key + "\" for a named range does not exist in the table.";
                    ++warning_count;
                    status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the column name to be used for the named range." ) );
                }
            }
	    }
	    int [] includeColumnNumbers = null;
	    if ( (includeColumns != null) && (includeColumns.length > 0) ) {
	        // Get the column numbers to output
	        includeColumnNumbers = new int[includeColumns.length];
	        for ( int i = 0; i < includeColumns.length; i++ ) {
	            try {
	                includeColumnNumbers[i] = table.getFieldIndex(includeColumns[i]);
	            }
	            catch ( Exception e ) {
	                message = "Table column to include in output \"" + includeColumns[i] + "\" does not exist in table.";
	                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
	                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Check the table column names." ) );
	                includeColumnNumbers[i] = -1;
	            }
	        }
	    }
	    else {
	        // Output all the columns
	        includeColumnNumbers = new int[table.getNumberOfFields()];
	        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
	            includeColumnNumbers[i] = i;
	        }
	    }
	    // Now remove output columns that are to be excluded.  Do so by setting column numbers for excluded columns to -1
	    if ( (excludeColumns != null) && (excludeColumns.length > 0) ) {
	        // Get the column numbers to exclude
	        for ( int i = 0; i < excludeColumns.length; i++ ) {
	            try {
	                int excludeColumnNumber = table.getFieldIndex(excludeColumns[i]);
	                // See if it exists in the array
	                for ( int j = 0; j < includeColumnNumbers.length; j++ ) {
	                	if ( includeColumnNumbers[j] == excludeColumnNumber ) {
	                		includeColumnNumbers[j] = -1;
	                	}
	                }
	            }
	            catch ( Exception e ) {
	                message = "Table column to exclude in output \"" + excludeColumns[i] + "\" does not exist in table.";
	                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
	                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Check the table column names." ) );
	                includeColumnNumbers[i] = -1;
	            }
	        }
	    }
	    // Finally, remove column numbers -1 so only valid columns that are requested are output
        int count = 0;
        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
            if ( includeColumnNumbers[i] >= 0 ) {
                ++count;
            }
        }
        int [] includeColumnNumbers2 = new int[count];
        count = 0;
        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
            if ( includeColumnNumbers[i] >= 0 ) {
                includeColumnNumbers2[count++] = includeColumnNumbers[i];
            }
        }
        includeColumnNumbers = includeColumnNumbers2;
        writeTableToExcelFile ( table, includeColumnNumbers, OutputFile_full, Worksheet,
            ExcelAddress, ExcelNamedRange, ExcelTableName, ExcelColumnNames,
            columnIncludeFilters, columnExcludeFilters,
            columnNamedRanges, keepOpen, columnCellTypes, columnWidths, columnDecimalPlaces,
            conditionTable, styleTable, LegendWorksheet, LegendAddress,
            problems );
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
    String IncludeColumns = props.getValue( "IncludeColumns" );
    String ExcludeColumns = props.getValue( "ExcludeColumns" );
	String OutputFile = props.getValue( "OutputFile" );
	String Worksheet = props.getValue( "Worksheet" );
	String ExcelAddress = props.getValue("ExcelAddress");
	String ExcelNamedRange = props.getValue("ExcelNamedRange");
	String ExcelTableName = props.getValue("ExcelTableName");
	String ExcelColumnNames = props.getValue("ExcelColumnNames");
	String ColumnIncludeFilters = props.getValue("ColumnIncludeFilters");
	String ColumnExcludeFilters = props.getValue("ColumnExcludeFilters");
	String ColumnNamedRanges = props.getValue("ColumnNamedRanges");
	String KeepOpen = props.getValue("KeepOpen");
	String ColumnCellTypes = props.getValue("ColumnCellTypes");
	String ColumnWidths = props.getValue("ColumnWidths");
	String ColumnDecimalPlaces = props.getValue("ColumnDecimalPlaces");
	String ConditionTableID = props.getValue( "ConditionTableID" );
	String StyleTableID = props.getValue( "StyleTableID" );
	String LegendWorksheet = props.getValue( "LegendWorksheet" );
	String LegendAddress = props.getValue( "LegendAddress" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (IncludeColumns != null) && (IncludeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeColumns=\"" + IncludeColumns + "\"" );
    }
    if ( (ExcludeColumns != null) && (ExcludeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeColumns=\"" + ExcludeColumns + "\"" );
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
    if ( (ColumnNamedRanges != null) && (ColumnNamedRanges.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnNamedRanges=\"" + ColumnNamedRanges + "\"");
    }
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (ColumnCellTypes != null) && (ColumnCellTypes.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnCellTypes=\"" + ColumnCellTypes + "\"");
    }
    if ( (ColumnWidths != null) && (ColumnWidths.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnWidths=\"" + ColumnWidths + "\"");
    }
    if ( (ColumnDecimalPlaces != null) && (ColumnDecimalPlaces.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnDecimalPlaces=\"" + ColumnDecimalPlaces + "\"");
    }
    if ( (ConditionTableID != null) && (ConditionTableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ConditionTableID=\"" + ConditionTableID + "\"" );
    }
    if ( (StyleTableID != null) && (StyleTableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "StyleTableID=\"" + StyleTableID + "\"" );
    }
    if ( (LegendWorksheet != null) && (LegendWorksheet.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LegendWorksheet=\"" + LegendWorksheet + "\"" );
    }
    if ( (LegendAddress != null) && (LegendAddress.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LegendAddress=\"" + LegendAddress + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

/**
TODO SAM 2015-07-10 - move this to generic location if reused between classes
Write the legend to the Excel worksheet.
@param sheet the worksheet being written
@param styleManager the style manager containing conditions and styles
@param legendAddress the address to use for the legend position (upper left is used)
*/
private void writeLegend ( ExcelToolkit tk, Workbook wb, Sheet reqSheet, TableConditionAndStyleManager styleManager,
	String legendWorksheet, String legendAddress,
	List<String> problems )
{
	// If the legend worksheet does not exist create it.
    Sheet sheet = reqSheet;
    boolean sheetGiven = false;
    if ( (legendWorksheet != null) && !legendWorksheet.isEmpty() ) {
    	sheet = wb.getSheet(legendWorksheet);
    	sheetGiven = true;
	    if ( sheet == null ) {
	        // Create the worksheet
	    	String sheetNameSafe = WorkbookUtil.createSafeSheetName(legendWorksheet);
	    	sheet = wb.createSheet(sheetNameSafe);
	    }
    }
	// Parse the legend address
    int rowOut = 0;
    int colOut = 0;
    if ( sheetGiven ) {
    	// Parse the address that is given - for now don't accept named range
    	AreaReference area = tk.getAreaReference ( wb, sheet, legendAddress, null, null );
        if ( area == null ) {
            problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
        }
        else {
            colOut = area.getFirstCell().getCol();
            rowOut = area.getFirstCell().getRow();
        }
    }
    else {
    	 // For now hard-code to the right of the block
		if ( (this.outputMaxRow < 0) || (this.outputMinColumn < 0) ) {
			return;
		}
		rowOut = this.outputMinRow;
		colOut = this.outputMaxColumn + 1;
		// Write the legend - only write legend information that is actually used
    }
	Cell cell;
    // Write legend header
    cell = tk.setCellValue(sheet,rowOut,colOut,"Color Legend");
	// Loop through the conditions
	DataTable ct = styleManager.getConditionTable();
	for ( int i = 0; i < ct.getNumberOfRecords(); i++ ) {
		++rowOut;
		try {
			// Write the condition string
			// TODO SAM 2015-07-11 evaluate how to make presentation-friendly
			String legendString = styleManager.getDisplayString(i);
			if ( legendString.isEmpty() ) {
				legendString = styleManager.getConditionString(i);
			}
			cell = tk.setCellValue(sheet,rowOut,colOut,legendString);
			// Write a cell with the format - blank string to force column size
			cell = tk.setCellValue(sheet,rowOut,(colOut + 1),"     ");
        	if ( styleManager != null ) {
        		// Get the cell style for the style ID.
        		// Use time series position 0 since styles are initialized for the single time series
        		int its = 0;
        		cell.setCellStyle(styleManager.getCellStyleForStyleID(its,styleManager.getStyleIDForCondition(i)));
        	}
		}
		catch ( Exception e ) {
			continue;
		}
	}
	// If the sheet was given, auto-size the column (don't do by default because raster plot by definition uses narrow columns)
	if ( sheetGiven ) {
		sheet.autoSizeColumn(colOut);
		sheet.setColumnWidth(colOut+1,256*4);
	}
}

/**
Read the table from an Excel worksheet.  The cells must be specified by a contiguous address block specified
by one of the parameters excelAddress, excelNamedRange, excelTableName.
@param table the table to output
@param includeColumnNumbers an array of table column numbers to output, guaranteed to be non null and filled with valid
columns
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name
@param excelColumnNames indicate how to determine column names from the Excel worksheet
@param columnExcludeFiltersMap a map indicating patters for column values, to exclude rows
@param columnNamedRanges column names and name range name to define
@param keepOpen if True, the Excel workbook will be kept open and not written
@param columnCellTypes column names and Excel cell types
@param columnWidths column names and widths (Auto to auto-size, or integer points)
@param columnDecimalPlaces column names and number of decimal places (used for floating point data)
@param conditionTable table containing condition data, to relate columns to styles and condition
@param styleTable table containing style data, for formatting
@param problems list of problems encountered during read, for formatted logging in calling code
*/
private void writeTableToExcelFile ( DataTable table, int [] includeColumnNumbers, String workbookFile, String sheetName,
    String excelAddress, String excelNamedRange, String excelTableName, String excelColumnNames,
    StringDictionary columnIncludeFilters, StringDictionary columnExcludeFilters,
    Hashtable<String,String> columnNamedRanges, boolean keepOpen,
    StringDictionary columnCellTypes, StringDictionary columnWidths,
    StringDictionary columnDecimalPlaces,
    DataTable conditionTable, DataTable styleTable, String legendWorksheet, String legendAddress,
    List<String> problems )
throws FileNotFoundException, IOException
{   String routine = getClass().getSimpleName() + ".writeTableToExcelFile";
    
    Workbook wb = null;
    InputStream inp = null;
    try {
        // Get include filter columns and glob-style regular expressions
        int [] columnIncludeFiltersNumbers = new int[0];
        String [] columnIncludeFiltersGlobs = null;
        if ( columnIncludeFilters != null ) {
            LinkedHashMap<String, String> map = columnIncludeFilters.getLinkedHashMap();
            columnIncludeFiltersNumbers = new int[map.size()];
            columnIncludeFiltersGlobs = new String[map.size()];
            int ikey = -1;
            String key = null;
            for ( Map.Entry<String,String> entry : map.entrySet() ) {
                ++ikey;
                columnIncludeFiltersNumbers[ikey] = -1;
                try {
                    key = entry.getKey();
                    columnIncludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                    columnIncludeFiltersGlobs[ikey] = map.get(key);
                    // Turn default globbing notation into internal Java regex notation
                    columnIncludeFiltersGlobs[ikey] = columnIncludeFiltersGlobs[ikey].replace("*", ".*").toUpperCase();
                }
                catch ( Exception e ) {
                    problems.add ( "ColumnIncludeFilters column \"" + key + "\" not found in table.");
                }
            }
        }
        // Get exclude filter columns and glob-style regular expressions
        int [] columnExcludeFiltersNumbers = new int[0];
        String [] columnExcludeFiltersGlobs = null;
        if ( columnExcludeFilters != null ) {
            LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
            columnExcludeFiltersNumbers = new int[map.size()];
            columnExcludeFiltersGlobs = new String[map.size()];
            int ikey = -1;
            String key = null;
            for ( Map.Entry<String,String> entry : map.entrySet() ) {
                ++ikey;
                columnExcludeFiltersNumbers[ikey] = -1;
                try {
                    key = entry.getKey();
                    columnExcludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                    columnExcludeFiltersGlobs[ikey] = map.get(key);
                    // Turn default globbing notation into internal Java regex notation
                    columnExcludeFiltersGlobs[ikey] = columnExcludeFiltersGlobs[ikey].replace("*", ".*").toUpperCase();
                    Message.printStatus(2,"","Exclude filter column \"" + key + "\" [" +
                    	columnExcludeFiltersNumbers[ikey] + "] glob \"" + columnExcludeFiltersGlobs[ikey] + "\"" );
                }
                catch ( Exception e ) {
                    problems.add ( "ColumnExcludeFilters column \"" + key + "\" not found in table.");
                }
            }
        }
    	// Create a toolkit for utility functions.
    	ExcelToolkit tk = new ExcelToolkit();
        // See if an open workbook by the same name exists
        WorkbookFileMetadata wbMeta = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wbMeta != null ) {
        	Message.printStatus(2, routine, "Writing to open workbook \"" + workbookFile + "\".");
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
            	Message.printStatus(2, routine, "Opening and writing to exixting workbook \"" + workbookFile + "\".");
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
        // TODO SAM 2013-02-22 In the future sheet may be determined from named address (e.g., named ranges
        // are global in workbook)
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
        // Get the contiguous block of data to process by evaluating user input
        AreaReference area = tk.getAreaReference ( wb, sheet, excelAddress, excelNamedRange, excelTableName );
        if ( area == null ) {
            problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
            return;
        }
        Message.printStatus(2,routine,"Excel address block to write: " + area );
        // Get the upper left row/column to write from the addresses
        int cols = includeColumnNumbers.length;
        int rows = table.getNumberOfRecords();
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
        // Process column metadata:
        //  1) determine column Excel data type
        //  2) write column names (if requested)
        //  3) indicate whether to auto-size column or set column specifically
        //  4) set up column styles/formatting for data values
        DataFormat [] columnCellFormats = new DataFormat[cols];
        CellStyle [] columnCellStyles = new CellStyle[cols];
        // Initialize styles corresponding to styleTable, newer approach to styling.
        // The styles in this table will be used by default with the above setting style information to the below.
        TableConditionAndStyleManager styleManager = null;
        if ( styleTable != null ) {
        	styleManager = new TableConditionAndStyleManager(table,includeColumnNumbers,conditionTable,styleTable,wb);
        }
        int tableFieldType;
        int precision;
        int colOut = colOutDataStart;
        if ( excelColumnNames.equalsIgnoreCase(_None) ) {
            doWriteColumnNames = false;
        }
        int [] excelColumnTypes = new int[includeColumnNumbers.length];
        // Get the default cell type for all columns if set
        String defaultCellType = columnCellTypes.get("Default");
        for ( int col = 0; col < includeColumnNumbers.length; col++, colOut++ ) {
            // 1. Determine the Excel output cell types for each column
            tableFieldType = table.getFieldDataType(includeColumnNumbers[col]);
            // If the user has specified a column type (even the default), then use it
            if ( defaultCellType != null ) {
                // Only "Text" is allowed
                if ( defaultCellType.equalsIgnoreCase("Text") ) {
                    excelColumnTypes[col] = Cell.CELL_TYPE_STRING;
                }
            }
            else {
                // Else set the type to something reasonable for the table column data type
            	if ( table.isColumnArray(tableFieldType) ) {
            		// Output will be formatted as string [ , , , ]
            		excelColumnTypes[col] = Cell.CELL_TYPE_STRING;
            	}
            	else if ( (tableFieldType == TableField.DATA_TYPE_DOUBLE) ||
                    (tableFieldType == TableField.DATA_TYPE_FLOAT) ||
                    (tableFieldType == TableField.DATA_TYPE_INT) ||
                    (tableFieldType == TableField.DATA_TYPE_LONG) ||
                    (tableFieldType == TableField.DATA_TYPE_SHORT)) {
                    excelColumnTypes[col] = Cell.CELL_TYPE_NUMERIC;
                }
                // TODO SAM 2015-05-03 Need to handle DATE and DATETIME
                else {
                    // Default is text
                    excelColumnTypes[col] = Cell.CELL_TYPE_STRING;
                }
            }
            // 2. Write the column names
            // First try to get an existing cell for the heading
            // First try to get an existing row
            Row wbRowColumnNames = sheet.getRow(rowOutColumnNames);
            // If it does not exist, create it
            if ( wbRowColumnNames == null ) {
                wbRowColumnNames = sheet.createRow(rowOutColumnNames);
            }
            Cell wbCell = wbRowColumnNames.getCell(colOut);
            String tableColumnName = table.getFieldName(includeColumnNumbers[col]);
            if ( wbCell == null ) {
                wbCell = wbRowColumnNames.createCell(colOut);
            }
            try {
                if ( doWriteColumnNames ) {
                    wbCell.setCellValue(tableColumnName);
                }
                Message.printStatus(2, routine, "Setting [" + rowOutColumnNames + "][" + col + "] = " + tableColumnName );
            }
            catch ( Exception e ) {
                // Log but let the output continue
                Message.printWarning(3, routine, "Unexpected error writing table heading at Excel row [" + rowOutColumnNames + "][" +
                    colOut + "] (" + e + ")." );
                Message.printWarning(3, routine, e);
            }
            // 3. Set the column width
            //    Actually, have to do this after the data have been set so see post-write section below
            // 4. Create the styles for the data values, including number of decimals (precision)
            columnCellFormats[col] = wb.createDataFormat();
            columnCellStyles[col] = wb.createCellStyle();
            if ( (tableFieldType == TableField.DATA_TYPE_FLOAT) || (tableFieldType == TableField.DATA_TYPE_DOUBLE) ) {
                precision = table.getFieldPrecision(includeColumnNumbers[col]);
                String numDec = columnDecimalPlaces.get(tableColumnName);
                if ( numDec != null ) {
                    try {
                        precision = Integer.parseInt(numDec.trim());
                    }
                    catch ( Exception e ) {
                        problems.add ( "Column \"" + tableColumnName + "\" number of decimals " + numDec + "\" is not an integer." );
                    }
                }
                else {
                    // Use the number of decimal places if specified
                    if ( precision < 0 ) {
                        precision = 6;
                    }
                }
                if ( styleManager == null ) {
                	// Old-style
                	columnCellStyles[col].setDataFormat(columnCellFormats[col].getFormat(tk.createFormatForFloat(precision)));
                }
                else {
                	// New-style...
                	styleManager.setColumnDataFormat(col,tk.createFormatForFloat(precision));
                }
            }
            else if ( (tableFieldType == TableField.DATA_TYPE_INT) || (tableFieldType == TableField.DATA_TYPE_LONG) ) {
            	if ( styleManager == null ) {
            		// Old-style...
            		columnCellStyles[col].setDataFormat(columnCellFormats[col].getFormat("0"));
            	}
            	else {
            		// New-style...
                   	styleManager.setColumnDataFormat(col,"0");
                }
            }
            // If named ranges are to be written, match the table columns and do it
            if ( columnNamedRanges != null ) {
                // Iterate through hashtable
                Enumeration<String> keys = columnNamedRanges.keys();
                String key = null;
                boolean found = false;
                String namedRange = null;
                while ( keys.hasMoreElements() ) {
                    key = keys.nextElement(); // Column name
                    // Find the table column
                    int namedRangeCol = table.getFieldIndex(key);
                    namedRange = columnNamedRanges.get(key); // Named range
                    if ( namedRangeCol == includeColumnNumbers[col] ) {
                        found = true;
                        break;
                    }
                }
                if ( found ) {
                    // Define a named range.  First try to retrieve
                    int nrid = wb.getNameIndex(namedRange);
                    Name nr;
                    if ( nrid >= 0 ) {
                        nr = wb.getNameAt(nrid);
                    }
                    else {
                        nr = wb.createName();
                        nr.setNameName(namedRange);
                    }
                    // Convert the 0-index row and column range to an Excel address range
                    CellReference ref1 = new CellReference(rowOutDataStart,colOut);
                    CellReference ref2 = new CellReference(rowOutDataEnd,colOut);
                    String reference = "'" + sheetName + "'!$" + ref1.getCellRefParts()[2] +"$" + ref1.getCellRefParts()[1] +
                        ":$" + ref2.getCellRefParts()[2] + "$" + ref2.getCellRefParts()[1];
                    nr.setRefersToFormula(reference);
                }
            }
        }
        // Write the table data
        Object fieldValue;
        Double fieldValueDouble;
        Float fieldValueFloat;
        Integer fieldValueInteger;
        Long fieldValueLong;
        String fieldValueString;
        String NaNValue = "";
        String cellString;
        int rowOut = rowOutDataStart - 1; // -1 because incremented at the top of the loop below
        this.outputMinRow = rowOut;
        this.outputMaxRow = rowOutDataEnd;
        this.outputMinColumn = colOut;
        this.outputMaxColumn = colOutDataEnd;
        Row wbRow;
        for ( int row = 0; (row < rows) && (rowOut <= rowOutDataEnd); row++) {
        	// Check whether the in-memory row should be written
        	if ( !isTableRowIncluded ( table, row,
        		columnIncludeFiltersNumbers, columnIncludeFiltersGlobs,
        		columnExcludeFiltersNumbers, columnExcludeFiltersGlobs,
        		problems) ) {
        		continue;
        	}
        	// The above is the only "continue" so increment the Excel row here
        	++rowOut;
            // First try to get an existing row
            wbRow = sheet.getRow(rowOut);
            // If it does not exist, create it
            if ( wbRow == null ) {
                wbRow = sheet.createRow(rowOut);
            }
            colOut = colOutDataStart;
            for ( int col = 0; (col < cols) && (colOut <= colOutDataEnd); col++, colOut++) {
                // First try to get an existing cell
                Cell wbCell = wbRow.getCell(colOut);
                if ( wbCell == null ) {
                    wbCell = wbRow.createCell(colOut);
                }
                try {
                    tableFieldType = table.getFieldDataType(includeColumnNumbers[col]);
                    precision = table.getFieldPrecision(includeColumnNumbers[col]);
                    fieldValue = table.getFieldValue(row,includeColumnNumbers[col]);
                    if ( fieldValue == null ) {
                        cellString = "";
                        wbCell.setCellValue(cellString);
                        if ( styleManager != null ) {
                        	// New-style...
                        	if ( (tableFieldType == TableField.DATA_TYPE_DOUBLE) ||
                        		(tableFieldType == TableField.DATA_TYPE_FLOAT) ) {
                        		wbCell.setCellStyle(styleManager.getStyle(col,(Double)null));
                        	}
                        	else if ( (tableFieldType == TableField.DATA_TYPE_INT) ||
                        		(tableFieldType == TableField.DATA_TYPE_LONG) ) {
                        		wbCell.setCellStyle(styleManager.getStyle(col,(Long)null));
                        	}
                        	else if ( (tableFieldType == TableField.DATA_TYPE_DATETIME) ) {
                        		wbCell.setCellStyle(styleManager.getStyle(col,(DateTime)null));
                        	}
                        	else if ( (tableFieldType == TableField.DATA_TYPE_STRING) ) {
                        		wbCell.setCellStyle(styleManager.getStyle(col,(String)null));
                        	}
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_FLOAT ) {
                        fieldValueFloat = (Float)fieldValue;
                    	// Set Excel cell value
                        if ( fieldValueFloat.isNaN() ) {
                            cellString = NaNValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if floating point
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueFloat);
                            }
                        }
                        // Set Excel cell style
                        if ( styleManager == null ) {
                        	// Old-style...
                        	wbCell.setCellStyle(columnCellStyles[col]);
                        }
                        else {
                        	// New-style...
                        	wbCell.setCellStyle(styleManager.getStyle(col,fieldValueFloat));
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_DOUBLE ) {
                    	// Set the Excel cell value
                        fieldValueDouble = (Double)fieldValue;
                        if ( fieldValueDouble.isNaN() ) {
                            cellString = NaNValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if string
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueDouble);
                            }
                        }
                        // Set Excel cell style
                        if ( styleManager == null ) {
                        	// Old-style...
                        	wbCell.setCellStyle(columnCellStyles[col]);
                        }
                        else {
                        	// New-style...
                        	wbCell.setCellStyle(styleManager.getStyle(col,fieldValueDouble));
                        }
                        //Message.printStatus(2,routine,"After double cell data set, cell style fill foreground color is " + wbCell.getCellStyle().getFillForegroundColor());
                        //Message.printStatus(2,routine,"After double cell data set, cell style fill background color is " + wbCell.getCellStyle().getFillBackgroundColor());
                        //Message.printStatus(2,routine,"After double cell data set, cell style fill pattern is " + wbCell.getCellStyle().getFillPattern());
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_INT ) {
                    	// Set the Excel cell value
                        fieldValueInteger = (Integer)fieldValue;
                        if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                            cellString = "" + fieldValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            wbCell.setCellValue(fieldValueInteger);
                        }
                        // Set the Excel cell style
                        if ( styleManager == null ) {
                        	// Old-style...
                        	wbCell.setCellStyle(columnCellStyles[col]);
                        }
                        else {
                        	// New-style...
                        	wbCell.setCellStyle(styleManager.getStyle(col,fieldValueInteger));
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_LONG ) {
                    	// Set the Excel cell value
                        fieldValueLong = (Long)fieldValue;
                        if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                            cellString = "" + fieldValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            wbCell.setCellValue(fieldValueLong);
                        }
                        // Now set the cell style
                        if ( styleManager == null ) {
                        	// Old-style...
                        	wbCell.setCellStyle(columnCellStyles[col]);
                        }
                        else {
                        	// New-style...
                        	wbCell.setCellStyle(styleManager.getStyle(col,fieldValueLong));
                        }
                    }
                    else if ( (tableFieldType == TableField.DATA_TYPE_STRING) || table.isColumnArray(tableFieldType) ) {
                    	// Set the Excel cell value
                    	if ( table.isColumnArray(tableFieldType) ) {
                    		// First format the data array as a string
                    		fieldValueString = table.formatArrayColumn(row, col);
                    	}
                    	else {
                    		fieldValueString = (String)fieldValue;
                    	}
                        if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                            cellString = fieldValueString;
                            wbCell.setCellValue(cellString);
                        }
                        // TODO SAM 2016-01-17 Could set as number, etc., in Excel
                        else {
                            wbCell.setCellValue(fieldValueString);
                        }
                        // Set the Excel cell style
                        if ( styleManager == null ) {
                        	// Old-style...
                        	wbCell.setCellStyle(columnCellStyles[col]);
                        }
                        else {
                        	// New-style...
                        	wbCell.setCellStyle(styleManager.getStyle(col,fieldValueString));
                        }
                    }
                    else {
                        // Use default formatting without styling
                        cellString = "" + fieldValue;
                        wbCell.setCellValue(cellString);
                    }
                    //Message.printStatus(2,routine,"After cell data set, cell style fill foreground color is " + wbCell.getCellStyle().getFillForegroundColor());
                    //Message.printStatus(2,routine,"After cell data set, cell style fill background color is " + wbCell.getCellStyle().getFillBackgroundColor());
                    //Message.printStatus(2,routine,"After cell data set, cell style fill pattern is " + wbCell.getCellStyle().getFillPattern());
                }
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing table [" + row + "][" +
                        includeColumnNumbers[col] + "] (" + e + ")." );
                    Message.printWarning(3, routine, e);
                }
            }
        }
        // Now do post-data set operations
        // Set the column width
        colOut = colOutDataStart;
        for ( int col = 0; col < includeColumnNumbers.length; col++, colOut++ ) {
            String tableColumnName = table.getFieldName(includeColumnNumbers[col]);
            String width = columnWidths.get(tableColumnName);
            if ( width == null ) {
                // Try getting the empty columns width - this overrides "Default"
                String width2 = columnWidths.get("EmptyColumns");
                if ( width2 != null ) {
                	// Need to check to see if the entire column is empty.
                	// If the entire column is empty
                	if ( table.isColumnEmpty(includeColumnNumbers[col]) ) {
                		// Column is not empty so OK to set the column width
                		width = width2;
                	}
                	// Else width=null still in effect so cascade to Default, etc. below
                }
            }
            if ( width == null ) {
                // Try getting the default width
                width = columnWidths.get("Default");
            }
            if ( width != null ) {
                // Set the column width
                if ( width.equalsIgnoreCase("Auto") ) {
                    sheet.autoSizeColumn(colOut);
                    Message.printStatus(2,routine,"Setting column \"" + tableColumnName + "\" [" + colOut + "] width to auto.");
                }
                else {
                    // Set the column width to 1/256 of character width, max of 256*256 since 256 is max characters shown
                    try {
                        int w = Integer.parseInt(width.trim());
                        sheet.setColumnWidth(colOut, w);
                        Message.printStatus(2,routine,"Setting column \"" + tableColumnName + "\" [" + colOut + "] width to " + w + ".");
                    }
                    catch ( NumberFormatException e ) {
                        problems.add ( "Column \"" + tableColumnName + "\" width \"" + width + "\" is not an integer." );
                    }
                }
            }
        }
        // Write the legend
        if ( (legendAddress != null) && !legendAddress.isEmpty() ) {
        	writeLegend ( tk, wb, sheet, styleManager, legendWorksheet, legendAddress, problems );
        }
    }
    catch ( Exception e ) {
        problems.add ( "Error writing to workbook \"" + workbookFile + "\" (" + e + ")." );
        Message.printWarning(3,routine,e);
    }
    finally {
        // Now write the workbook and close.  If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,"w",wb);
        }
        else {
            // Write the workbook, close the workbook, and remove from the open workbook cache
            wb.setForceFormulaRecalculation(true); // Will cause Excel to recalculate formulas when it opens
            FileOutputStream fout = new FileOutputStream(workbookFile);
            wb.write(fout);
            fout.close();
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
}

}
