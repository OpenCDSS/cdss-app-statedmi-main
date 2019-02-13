// WriteTimeSeriesToExcelBlock_Command - This class initializes, checks, and runs the WriteTimeSeriesToExcelBlock() command, using Apache POI.

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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
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
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormatterType;
import RTi.Util.Time.DateTimeRange;
import RTi.Util.Time.InvalidTimeIntervalException;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
This class initializes, checks, and runs the WriteTimeSeriesToExcelBlock() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class WriteTimeSeriesToExcelBlock_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Possible values for KeepOpen parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Values for MissingValue parameter.
*/
protected final String _Blank = "Blank";

/**
Values for LayoutBlock parameter.
*/
protected final String _Period = "Period";
protected final String _Year = "Year";

/**
Values to use with output properties.
*/
private int blockMinColumn = -1;
private int blockMinRow = -1;
private int blockMaxColumn = -1;
private int blockMaxRow = -1;

/**
Constructor.
*/
public WriteTimeSeriesToExcelBlock_Command ()
{	super();
	setCommandName ( "WriteTimeSeriesToExcelBlock" );
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
{	String Precision = parameters.getValue ( "Precision" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Append = parameters.getValue ( "Append" );
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
	String OutputYearType = parameters.getValue ( "OutputYearType" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
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
	else if ( OutputFile.indexOf("${") < 0 ) {
	    try {
	        String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, OutputFile) );
			File f = new File ( adjusted_path );
			if ( !f.getParentFile().exists() ) {
	            message = "The Excel output file folder does not exist for file \"" + adjusted_path + "\".";
	            warning += "\n" + message;
	            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the folder for the Excel output file exists - may be OK if created at run time." ) );
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
	
	if ( (Append != null) && !Append.equals("") ) {
		if ( !Append.equalsIgnoreCase(_False) && !Append.equalsIgnoreCase(_True) ) {
			message = "The Append parameter \"" + Append + "\" is invalid.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _False + " (default) or " + _True + "."));
		}
	}
	
	if ( ((ExcelAddress == null) || ExcelAddress.equals("")) && ((ExcelNamedRange == null) || ExcelNamedRange.equals("")) &&
		((ExcelTableName == null) || ExcelTableName.equals("")) ) {
		message = "The address for the output must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the output address, named range, or Excel table name."));
	}
	
	if ( Precision != null && !Precision.equals("") ) {
	    try {
	        Integer.parseInt(Precision);
	    }
	    catch ( NumberFormatException e ) {
	        message = "The output precision (" + Precision + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the output precision as a positive integer." ) );
	    }
	}
	
	if ( (OutputStart != null) && !OutputStart.isEmpty() && !OutputStart.startsWith("${")) {
		try {	DateTime datetime1 = DateTime.parse(OutputStart);
			if ( datetime1 == null ) {
				throw new Exception ("bad date");
			}
		}
		catch (Exception e) {
			message = "Output start date/time \"" + OutputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid output start date/time." ) );
		}
	}
	if ( (OutputEnd != null) && !OutputEnd.equals("") && !OutputEnd.startsWith("${")) {
		try {	DateTime datetime2 = DateTime.parse(OutputEnd);
			if ( datetime2 == null ) {
				throw new Exception ("bad date");
			}
		}
		catch (Exception e) {
			message = "Output end date/time \"" + OutputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify a valid output end date/time." ) );
		}
	}
	
	YearType outputYearType = null;
	if ( (OutputYearType != null) && !OutputYearType.equals("") ) {
        try {
            outputYearType = YearType.valueOfIgnoreCase(OutputYearType);
        }
        catch ( Exception e ) {
        	outputYearType = null;
        }
        if ( outputYearType == null ) {
            message = "The output year type (" + OutputYearType + ") is invalid.";
            warning += "\n" + message;
            StringBuffer b = new StringBuffer();
            List<YearType> values = YearType.getYearTypeChoices();
            for ( YearType t : values ) {
                if ( b.length() > 0 ) {
                    b.append ( ", " );
                }
                b.append ( t.toString() );
            }
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message, "Valid values are:  " + b.toString() + "."));
        }
	}
	
	if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
	    !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.equalsIgnoreCase("") ) {
	    message = "KeepOpen is invalid.";
	    warning += "\n" + message;
	    status.addToLog ( CommandPhaseType.INITIALIZATION,
	        new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "KeepOpen must be " + _False + " (default) or " + _True ) );
	}
	
	// TODO SAM 2005-11-18 Check the format.
	
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(26);
	validList.add ( "TSList" );
	validList.add ( "TSID" );
	validList.add ( "EnsembleID" );
	validList.add ( "MissingValue" );
	validList.add ( "Precision" );
	validList.add ( "OutputStart" );
	validList.add ( "OutputEnd" );
	validList.add ( "OutputFile" );
	validList.add ( "Append" );
	validList.add ( "Worksheet" );
	validList.add ( "ExcelAddress" );
	validList.add ( "ExcelNamedRange" );
	validList.add ( "ExcelTableName" );
	validList.add ( "KeepOpen" );
	validList.add ( "LayoutBlock" );
	validList.add ( "LayoutColumns" );
	validList.add ( "LayoutRows" );
	validList.add ( "OutputYearType" );
	validList.add ( "BlockMinColumnProperty" );
	validList.add ( "BlockMinRowProperty" );
	validList.add ( "BlockMaxColumnProperty" );
	validList.add ( "BlockMaxRowProperty" );
    validList.add ( "ConditionTableID" );
    validList.add ( "StyleTableID" );
    validList.add ( "LegendWorksheet" );
    validList.add ( "LegendAddress" );
	// TODO SAM 2015-03-10 need to add others for comments, statistics, etc.
	
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
	return (new WriteTimeSeriesToExcelBlock_JDialog ( parent, this, tableIDChoices )).ok();
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
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand",message = "";
	int warning_level = 2;
	int logLevel = 3; // Level for non-user messages for log file.
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
    
	// Check whether the processor wants output files to be created...

	if ( !TSCommandProcessorUtil.getCreateOutput(processor) ) {
			Message.printStatus ( 2, routine,
			"Skipping \"" + toString() + "\" because output is not being created." );
	}

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	
	String TSList = parameters.getValue ( "TSList" );
    if ( (TSList == null) || TSList.equals("") ) {
        TSList = TSListType.ALL_TS.toString();
    }
	String TSID = parameters.getValue ( "TSID" );
	if ( (TSID != null) && (TSID.indexOf("${") >= 0) ) {
		TSID = TSCommandProcessorUtil.expandParameterValue(processor, this, TSID);
	}
    String EnsembleID = parameters.getValue ( "EnsembleID" );
	if ( (EnsembleID != null) && (EnsembleID.indexOf("${") >= 0) ) {
		EnsembleID = TSCommandProcessorUtil.expandParameterValue(processor, this, EnsembleID);
	}
    String MissingValue = parameters.getValue ( "MissingValue" );
    if ( (MissingValue != null) && MissingValue.equals("") ) {
        // Set to null to indicate default internal value should be used
        MissingValue = null;
    }
	String Precision = parameters.getValue ( "Precision" );
	Integer precision = null; // default
	if ( (Precision != null) && !Precision.equals("") ) {
		try {
		    precision = Integer.parseInt(Precision);
		}
		catch ( NumberFormatException e ) {
		    precision = null;
		}
	}
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Append = parameters.getValue ( "Append" );
	boolean append = false;
	if ( (Append != null) && !Append.equals("")) {
	    if ( Append.equalsIgnoreCase(_True) ) {
	        append = true;
	    }
	}
	String Worksheet = parameters.getValue ( "Worksheet" );
	if ( (Worksheet != null) && (Worksheet.indexOf("${") >= 0) ) {
		Worksheet = TSCommandProcessorUtil.expandParameterValue(processor, this, Worksheet);
	}
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	boolean keepOpen = false;
	if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase(_True) ) {
		keepOpen = true;
	}
	String OutputYearType = parameters.getValue ( "OutputYearType" );
	YearType outputYearType = YearType.CALENDAR; // Default
	if ( (OutputYearType != null) && !OutputYearType.isEmpty() ) {
		outputYearType = YearType.valueOfIgnoreCase(OutputYearType);
	}
	String BlockMinColumnProperty = parameters.getValue ( "BlockMinColumnProperty" );
	String BlockMinRowProperty = parameters.getValue ( "BlockMinRowProperty" );
	String BlockMaxColumnProperty = parameters.getValue ( "BlockMaxColumnProperty" );
	String BlockMaxRowProperty = parameters.getValue ( "BlockMaxRowProperty" );
	
	// TODO SAM 2015-03-10 Need to rework the following
	String DateTimeColumn = parameters.getValue ( "DateTimeColumn" );
    String DateTimeFormatterType0 = parameters.getValue ( "DateTimeFormatterType" );
    if ( (DateTimeFormatterType0 == null) || DateTimeFormatterType0.equals("") ) {
        DateTimeFormatterType0 = "" + DateTimeFormatterType.C;
    }
    DateTimeFormatterType dateTimeFormatterType = DateTimeFormatterType.valueOfIgnoreCase(DateTimeFormatterType0);
    String DateTimeFormat = parameters.getValue ( "DateTimeFormat" );
	String DateColumn = parameters.getValue ( "DateColumn" );
    String DateFormatterType = parameters.getValue ( "DateFormatterType" );
    if ( (DateFormatterType == null) || DateFormatterType.equals("") ) {
        DateFormatterType = "" + DateTimeFormatterType.C;
    }
    DateTimeFormatterType dateFormatterType = DateTimeFormatterType.valueOfIgnoreCase(DateFormatterType);
    String DateFormat = parameters.getValue ( "DateFormat" );
	String TimeColumn = parameters.getValue ( "TimeColumn" );
    String TimeFormatterType = parameters.getValue ( "TimeFormatterType" );
    if ( (TimeFormatterType == null) || TimeFormatterType.equals("") ) {
        TimeFormatterType = "" + DateTimeFormatterType.C;
    }
    DateTimeFormatterType timeFormatterType = DateTimeFormatterType.valueOfIgnoreCase(TimeFormatterType);
    String TimeFormat = parameters.getValue ( "TimeFormat" );
    String ValueColumns = parameters.getValue ( "ValueColumns" );
    if ( (ValueColumns == null) || ValueColumns.equals("") ) {
        ValueColumns = "%L_%T";
    }
    String LayoutBlock = parameters.getValue ( "LayoutBlock" );
    String LayoutRows = parameters.getValue ( "LayoutRows" );
    String LayoutColumns = parameters.getValue ( "LayoutColumns" );
    TimeInterval layoutColumns = null;
    try {
    	layoutColumns = TimeInterval.parseInterval(LayoutColumns);
    }
    catch ( Exception e ) {
    	layoutColumns = null;
    }
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
    
	// Get the time series to process...
	PropList request_params = new PropList ( "" );
	request_params.set ( "TSList", TSList );
	request_params.set ( "TSID", TSID );
    request_params.set ( "EnsembleID", EnsembleID );
	CommandProcessorRequestResultsBean bean = null;
	try {
        bean = processor.processRequest( "GetTimeSeriesToProcess", request_params);
	}
	catch ( Exception e ) {
        message = "Error requesting GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\", TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\") from processor.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report problem to software support." ) );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
	if ( o_TSList == null ) {
        message = "Null TSToProcessList returned from processor for GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report problem to software support." ) );
	}
	@SuppressWarnings("unchecked")
	List<TS> tslist = (List<TS>)o_TSList;
	if ( tslist.size() == 0 ) {
        message = "No time series are available from processor GetTimeSeriesToProcess (TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
						message, "Confirm that time series are available (may be OK for partial run)." ) );
	}
	if ( tslist.size() != 1 ) {
        message = "Currently only the first time series will be processed.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.WARNING,
				message, "Specify a single time series for output." ) );
	}

	String OutputStart = parameters.getValue ( "OutputStart" );
	if ( (OutputStart == null) || OutputStart.isEmpty() ) {
		OutputStart = "${OutputStart}";
	}
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	if ( (OutputEnd == null) || OutputEnd.isEmpty() ) {
		OutputEnd = "${OutputEnd}";
	}
	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	try {
		OutputStart_DateTime = TSCommandProcessorUtil.getDateTime ( OutputStart, "OutputStart", processor,
			status, warning_level, command_tag );
	}
	catch ( InvalidCommandParameterException e ) {
		// Warning will have been added above...
		++warning_count;
	}
	try {
		OutputEnd_DateTime = TSCommandProcessorUtil.getDateTime ( OutputEnd, "OutputEnd", processor,
			status, warning_level, command_tag );
	}
	catch ( InvalidCommandParameterException e ) {
		// Warning will have been added above...
		++warning_count;
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
	    Object o_Table = bean_PropList.getContents ( "Table" );
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
	    Object o_Table = bean_PropList.getContents ( "Table" );
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

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the time series...

	List<String> problems = new Vector<String>();
	String OutputFile_full = null;
	try {
        OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
        Message.printStatus ( 2, routine, "Writing time series to Excel file \"" + OutputFile_full + "\"" );
        writeTimeSeries ( tslist,
        		precision, MissingValue, OutputStart_DateTime, OutputEnd_DateTime,
        		OutputFile_full, append, Worksheet, ExcelAddress, ExcelNamedRange, ExcelTableName, keepOpen,
        		DateTimeColumn, dateTimeFormatterType, DateTimeFormat,
        		DateColumn, dateFormatterType, DateFormat,
        		TimeColumn, timeFormatterType, TimeFormat,
        		ValueColumns,
        		LayoutBlock, layoutColumns, LayoutRows, outputYearType,
        		conditionTable, styleTable, LegendWorksheet, LegendAddress,
        	    problems, processor, status, commandPhase );
        for ( String problem: problems ) {
            Message.printWarning ( 3, routine, problem );
            message = "Error writing to Excel: " + problem;
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Check the log file for exceptions." ) );
        }
        // Set the table identifier...
        setOutputFile(new File(OutputFile_full));
        // Set the properties indicating the dimension of output
        if ( (BlockMinColumnProperty != null) && !BlockMinColumnProperty.equals("") ) {
            request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", BlockMinColumnProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(this.blockMinColumn) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + BlockMinColumnProperty + "\") from processor.";
                Message.printWarning(logLevel,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
        if ( (BlockMinRowProperty != null) && !BlockMinRowProperty.equals("") ) {
            request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", BlockMinRowProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(this.blockMinRow) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + BlockMinRowProperty + "\") from processor.";
                Message.printWarning(logLevel,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
        if ( (BlockMaxColumnProperty != null) && !BlockMaxColumnProperty.equals("") ) {
            request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", BlockMaxColumnProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(this.blockMaxColumn) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + BlockMaxColumnProperty + "\") from processor.";
                Message.printWarning(logLevel,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
        if ( (BlockMaxRowProperty != null) && !BlockMaxRowProperty.equals("") ) {
            request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", BlockMaxRowProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(this.blockMaxRow) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + BlockMaxRowProperty + "\") from processor.";
                Message.printWarning(logLevel,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error writing time series to Excel file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the folder to write exists and permissions allow writing." ) );
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
	String TSList = props.getValue ( "TSList" );
	String TSID = props.getValue ( "TSID" );
	String EnsembleID = props.getValue ( "EnsembleID" );
	String MissingValue = props.getValue("MissingValue");
	String Precision = props.getValue ( "Precision" );
	String OutputStart = props.getValue ( "OutputStart" );
	String OutputEnd = props.getValue ( "OutputEnd" );
	String OutputFile = props.getValue ( "OutputFile" );
	String Append = props.getValue ( "Append" );
	String Worksheet = props.getValue ( "Worksheet" );
	String ExcelAddress = props.getValue ( "ExcelAddress" );
	String ExcelNamedRange = props.getValue ( "ExcelNamedRange" );
	String ExcelTableName = props.getValue ( "ExcelTableName" );
	String KeepOpen = props.getValue ( "KeepOpen" );
	String LayoutBlock = props.getValue ( "LayoutBlock" );
	String LayoutColumns = props.getValue ( "LayoutColumns" );
	String LayoutRows = props.getValue ( "LayoutRows" );
	String OutputYearType = props.getValue ( "OutputYearType" );
	String BlockMinColumnProperty = props.getValue ( "BlockMinColumnProperty" );
	String BlockMinRowProperty = props.getValue ( "BlockMinRowProperty" );
	String BlockMaxColumnProperty = props.getValue ( "BlockMaxColumnProperty" );
	String BlockMaxRowProperty = props.getValue ( "BlockMaxRowProperty" );
	String ConditionTableID = props.getValue( "ConditionTableID" );
	String StyleTableID = props.getValue( "StyleTableID" );
	String LegendWorksheet = props.getValue( "LegendWorksheet" );
	String LegendAddress = props.getValue( "LegendAddress" );
	StringBuffer b = new StringBuffer ();
	if ( (TSList != null) && (TSList.length() > 0) ) {
	    if ( b.length() > 0 ) {
	        b.append ( "," );
	    }
	    b.append ( "TSList=" + TSList );
	}
	if ( (TSID != null) && (TSID.length() > 0) ) {
	    if ( b.length() > 0 ) {
	        b.append ( "," );
	    }
	    b.append ( "TSID=\"" + TSID + "\"" );
	}
	if ( (EnsembleID != null) && (EnsembleID.length() > 0) ) {
	    if ( b.length() > 0 ) {
	        b.append ( "," );
	    }
	    b.append ( "EnsembleID=\"" + EnsembleID + "\"" );
	}
    if ( (MissingValue != null) && (MissingValue.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MissingValue=" + MissingValue );
    }
	if ( (Precision != null) && (Precision.length() > 0) ) {
	    if ( b.length() > 0 ) {
	        b.append ( "," );
	    }
	    b.append ( "Precision=" + Precision );
	}
    if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputStart=\"" + OutputStart + "\"" );
    }
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputEnd=\"" + OutputEnd + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
    if ( (Append != null) && (Append.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Append=" + Append );
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
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (LayoutBlock != null) && !LayoutBlock.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LayoutBlock=" + LayoutBlock );
    }
    if ( (LayoutColumns != null) && !LayoutColumns.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LayoutColumns=" + LayoutColumns );
    }
    if ( (LayoutRows != null) && !LayoutRows.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LayoutRows=" + LayoutRows );
    }
    if ( (OutputYearType != null) && !OutputYearType.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputYearType=" + OutputYearType );
    }
    if ( (BlockMinColumnProperty != null) && !BlockMinColumnProperty.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "BlockMinColumnProperty=\"" + BlockMinColumnProperty + "\"" );
    }
    if ( (BlockMinRowProperty != null) && !BlockMinRowProperty.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "BlockMinRowProperty=\"" + BlockMinRowProperty + "\"" );
    }
    if ( (BlockMaxColumnProperty != null) && !BlockMaxColumnProperty.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "BlockMaxColumnProperty=\"" + BlockMaxColumnProperty + "\"" );
    }
    if ( (BlockMaxRowProperty != null) && !BlockMaxRowProperty.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "BlockMaxRowProperty=\"" + BlockMaxRowProperty + "\"" );
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
Read the table from an Excel worksheet.  The cells must be specified by a contiguous address block specified
by one of the parameters excelAddress, excelNamedRange, excelTableName.
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name
@param excelColumnNames indicate how to determine column names from the Excel worksheet
@param columnExcludeFiltersMap a map indicating patters for column values, to exclude rows
@param comment character that if at start of first column indicates row is a comment
@param excelIntegerColumns names of columns that should be treated as integers, or null if none
@param numberPrecision digits after decimal for floating point numbers (can't yet determine from Excel)
@param WriteAllAsText if True, treat all data as text values
@param problems list of problems encountered during read, for formatted logging in calling code
@return a DataTable with the Excel contents
*/
/*
private DataTable WriteTimeSeriesToExcelFile ( String workbookFile, String sheetName,
    String excelAddress, String excelNamedRange, String excelTableName, String excelColumnNames,
    Hashtable columnExcludeFiltersMap, String comment, String [] excelIntegerColumns, String [] excelDateTimeColumns,
    int numberPrecision, boolean WriteAllAsText, List<String> problems )
throws FileNotFoundException, IOException
{   String routine = "WriteTimeSeriesToExcel_Command.WriteTimeSeriesToExcelFile", message;
*/

/**
TODO SAM 2015-07-10 - move this to generic location if reused between classes
Write the legend to the Excel worksheet.
@param sheet the worksheet being written
@param styleManager the style manager containing conditions and styles
@param legendAddress the address to use for the legend position (upper left is used)
*/
private void writeLegend ( ExcelToolkit tk, Workbook wb, Sheet reqSheet, TimeSeriesConditionAndStyleManager styleManager,
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
		if ( (this.blockMaxRow < 0) || (this.blockMinColumn < 0) ) {
			return;
		}
		rowOut = this.blockMinRow;
		colOut = this.blockMaxColumn + 1;
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
Write a time series to the Excel file.
@param tslist list of time series to write
@param precision precision for output value (default is from data units, or 4)
@param missingValue requested missing value to output, or null to output time series missing value
@param outputStart start for output values
@param output End end for output values
@param workbookFile full path to output file
@param keepOpen indicate whether Excel file should be kept open after writing
@param dateTimeColumn name of column for date/time
@param dateTimeFormatterType formatter type for date/times
@param dateTimeFormat the format to use for date/times, when processed by the date/time formatter
@param valueColumns name(s) of column(s) for time series values using %L, ${ts:property}, ${property}
@param problems list of problems occurring in method
@param processor command processor
@param cs command status, for logging
@param commandPhase command phase, for logging
*/
private void writeTimeSeries ( List<TS> tslist,
	Integer precision, String missingValue, DateTime outputStart, DateTime outputEnd,
	String workbookFile, boolean append, String sheetName, String excelAddress, String excelNamedRange, String excelTableName, boolean keepOpen,
	String dateTimeColumn, DateTimeFormatterType dateTimeFormatterType, String dateTimeFormat,
	String dateColumn, DateTimeFormatterType dateFormatterType, String dateFormat,
	String timeColumn, DateTimeFormatterType timeFormatterType, String timeFormat,
	String valueColumns,
	String layoutBlock, TimeInterval layoutColumns, String layoutRows, YearType outputYearType,
	DataTable conditionTable, DataTable styleTable, String legendWorksheet, String legendAddress,
    List<String> problems, CommandProcessor processor, CommandStatus cs, CommandPhaseType commandPhase )
throws FileNotFoundException, IOException
{   String routine = getClass().getSimpleName() + ".writeTimeSeries", message;
    Workbook wb = null;
    InputStream inp = null;
    try {
    	// Create toolkit instance for useful Excel methods
    	ExcelToolkit tk = new ExcelToolkit();
        // See if an open workbook by the same name exists
        WorkbookFileMetadata wbMeta = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wbMeta != null ) {
        	wb = wbMeta.getWorkbook();
        }
        if ( wb == null ) {
            // Workbook is not open in memory so Open the file
        	if ( append ) {
	            try {
	                inp = new FileInputStream(workbookFile);
	            }
	            catch ( IOException e ) {
	                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
	                return;
	            }
	            try {
	                // Open the existing workbook...
	                wb = WorkbookFactory.create(inp);
	            }
	            catch ( InvalidFormatException e ) {
	                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
	                return;
	            }
	            finally {
	                // Close the file because will need to re-write it below and close
	                if ( !keepOpen ) {
	                    inp.close();
	                }
	            }
        	}
        	else {
        		// Create a new workbook
        		try {
        		    if ( workbookFile.toLowerCase().endsWith(".xls") ) {
        		        wb = new HSSFWorkbook();
        		    }
        		    else if ( workbookFile.toLowerCase().endsWith(".xlsx") ) {
        		        wb = new XSSFWorkbook();
        		    }
        		    else {
        		        message = "Unknown Excel file extension for \"" + workbookFile + "\"";
        		        Message.printWarning ( 3, routine, message );
        		        cs.addToLog(CommandPhaseType.RUN,
        		            new CommandLogRecord(CommandStatusType.FAILURE,
        		                message, "See the log file for details."));
        		    }
        		}
        		catch ( Exception e ) {
        		    problems.add ( "Error creating the Workbook object (" + e + ")." );
        		    return;
        		}
        	}
        }
        Sheet sheet = null;
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
        	try {
        		sheet = wb.getSheetAt(0);
        	}
        	catch ( Exception e ) {
        		sheet = null;
        	}
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets - cannot default to first sheet." );
                return;
            }
        }
        else {
        	// Get the sheet from the open workbook
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                // Create the worksheet
            	String sheetNameSafe = null;
            	try {
            		sheetNameSafe = WorkbookUtil.createSafeSheetName(sheetName);
	                sheet = wb.createSheet(sheetName);
                }
                catch ( Exception e ) {
                    problems.add("Error creating Excel worksheet \"" + sheetName + "\" (safe name: \"" +
                    	sheetNameSafe + "\") (" + e + ").");
                    return;
                }
            }
        }
        // Get the contiguous block of data to process by evaluating user input
        AreaReference area = tk.getAreaReference ( wb, sheet, excelAddress, excelNamedRange, excelTableName );
        int rowOutStart = 0, colOutStart = 0; // Position of upper-left start of output
        if ( area == null ) {
            problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
            colOutStart = area.getFirstCell().getCol();
            rowOutStart = area.getFirstCell().getRow();
        }
        else {
        	Message.printStatus(2,routine,"Excel address block to write: " + area);
        }
	    
        if ( (tslist == null) || (tslist.size() == 0) ) {
        	// Cannot continue because time series need to be accessed below
        	// Don't save a problem because it is OK to process zero time series in some cases.
            return;
        }
	    // Make sure the time series have the same interval
        // TODO SAM 2013-10-22 For now do not support irregular data - when do have to use TSIterator
        if ( !TSUtil.areIntervalsSame(tslist) ) {
            throw new InvalidTimeIntervalException("Time series time intervals are not the same.  Cannot write file.");
        }
        // Set up for writing time series data - determine output properties
        int [] tsPrecision = new int[tslist.size()];
        for ( int its = 0; its < tslist.size(); its++ ) {
            if ( precision == null ) {
            	// Get the precision from the data units
            	DataUnits units = null;
            	try {
            		units = DataUnits.lookupUnits(tslist.get(its).getDataUnits());
            	}
            	catch ( Exception e ) {
            		// Units not handled
            	}
            	if ( units != null ) {
            		tsPrecision[its] = units.getOutputPrecision();
            	}
            	else {
            		tsPrecision[its] = 4;
            	}
            }
        }
        String missingValueString = "";
        // Create a DateTimeFormatter to format the data values
        if ( dateTimeFormatterType == null ) {
            dateTimeFormatterType = DateTimeFormatterType.C;
        }
        if ( (dateTimeFormat != null) && dateTimeFormat.equals("") ) {
            // Set to null to simplify checks below
            dateTimeFormat = null;
        }
        // Determine the period of record for output
        // Loop through the specified period or if not specified the full overlapping period
        if ( (outputStart == null) || (outputEnd == null) ) {
            TSLimits limits = TSUtil.getPeriodFromTS(tslist, TSUtil.MAX_POR);
            if ( outputStart == null ) {
                outputStart = limits.getDate1();
            }
            if ( outputEnd == null ) {
                outputEnd = limits.getDate2();
            }
        }
        int intervalBase = -1;
        int intervalMult = -1;
        String [] missingValueStrings = new String[tslist.size()];
        boolean missingValueBlank = false;
        if ( (missingValue != null) && missingValue.equalsIgnoreCase("Blank") ) {
        	// Use a blank cell
        	missingValueBlank = true;
        }
        int its = -1;
        int blockMinColumn = Integer.MAX_VALUE;
        int blockMinRow = Integer.MAX_VALUE;
        int blockMaxColumn = Integer.MIN_VALUE;
        int blockMaxRow = Integer.MIN_VALUE;
        for ( TS ts : tslist ) {
            ++its;
            if ( its == 1 ) {
            	// Currently only process the first time series
            	// TODO SAM 2015-03-10 Need to figure out how to increment output block
            }
            if ( ts != null ) {
                intervalBase = ts.getDataIntervalBase();
                intervalMult = ts.getDataIntervalMult();
            }
            // Missing value can be output as a string so check
            if ( (missingValue == null) || missingValue.equals("") ) {
                // Use the time series value
            	if ( !StringUtil.isDouble(missingValue) ) {
            		// Missing value is a string
            		missingValueStrings[its] = missingValue;
            	}
            	else if ( Double.isNaN(ts.getMissing()) ) {
            		// Use string NaN
                    missingValueStrings[its] = "NaN";
                }
            	else {
            		missingValueStrings[its] = null; // Indicates to use the time series missing value or missingValueBlank
            	}
            }
            else {
                if ( missingValue.equalsIgnoreCase(_Blank) ) {
                    missingValueStrings[its] = "";
                }
                else {
                    missingValueStrings[its] = missingValue;
                }
            }
        }
        if ( (dateTimeColumn == null) || dateTimeColumn.equals("") ) {
            if ( intervalBase >= TimeInterval.DAY ) {
                dateTimeColumn = "Date";
            }
            else {
                dateTimeColumn = "DateTime";
            }
        }
        // Output the column headings
        // Process column metadata:
        //  1) determine column Excel data type
        //  2) write column names (if requested)
        //  3) indicate whether to auto-size column or set column specifically
        //  4) set up column styles/formatting for data values
        int dateTimeCol = -1;
        int dateCol = -1;
        int timeCol = -1;
        int numDateTimeCol = 0;
        if ( (dateTimeColumn != null) && !dateTimeColumn.equals("") ) {
        	dateTimeCol = colOutStart;
        	++numDateTimeCol;
        }
        if ( (dateColumn != null) && !dateColumn.equals("") ) {
        	dateCol = dateTimeCol + 1;
        	++numDateTimeCol;
        }
        if ( (timeColumn != null) && !timeColumn.equals("") ) {
        	timeCol = dateCol + 1;
        	++numDateTimeCol;
        }
        int cols = tslist.size() + numDateTimeCol; // Number of columns to output including date/time columns and time series values
        // Set the cell formats for output (will be used for the data rows).
        // All formats for the column headings are text.
        DataFormat [] cellFormats = new DataFormat[cols];
        DataFormat cellFormatHeader = wb.createDataFormat();
        CellStyle [] cellStyles = new CellStyle[cols];
        CellStyle cellStyleHeader = wb.createCellStyle();
        // Initialize styles corresponding to styleTable, newer approach to styling.
        // The styles in this table will be used by default with the above setting style information to the below.
        TimeSeriesConditionAndStyleManager styleManager = null;
        if ( (conditionTable != null) && (styleTable != null) ) {
        	styleManager = new TimeSeriesConditionAndStyleManager(tslist,conditionTable,styleTable,wb);
        }
        int [] cellTypes = new int[cols];
        int cellTypeHeader = Cell.CELL_TYPE_STRING;
        int col = 0;
        // TODO SAM 2015-02-17 Need to figure out how to store date/time in numeric - for now format a string
        List<String> columnNames = new ArrayList<String>();
        if ( dateTimeCol >= 0 ) {
        	cellFormats[col] = wb.createDataFormat();
            cellStyles[col] = wb.createCellStyle();
            cellTypes[col] = Cell.CELL_TYPE_STRING;
            columnNames.add(dateTimeColumn);
            ++col;
        }
        if ( dateCol >= 0 ) {
        	cellFormats[col] = wb.createDataFormat();
            cellStyles[col] = wb.createCellStyle();
            cellTypes[col] = Cell.CELL_TYPE_STRING;
            columnNames.add(dateColumn);
            ++col;
        }
        if ( timeCol >= 0 ) {
        	cellFormats[col] = wb.createDataFormat();
            cellStyles[col] = wb.createCellStyle();
            cellTypes[col] = Cell.CELL_TYPE_STRING;
            columnNames.add(timeColumn);
            ++col;
        }
        for ( col = numDateTimeCol, its = 0; col < cols; col++, its++ ) {
        	TS ts = tslist.get(its);
        	cellFormats[col] = wb.createDataFormat();
            cellStyles[col] = wb.createCellStyle();
            cellStyles[col].setDataFormat(cellFormats[col].getFormat(tk.createFormatForFloat(tsPrecision[its])));
            cellTypes[col] = Cell.CELL_TYPE_NUMERIC;
            columnNames.add(ts.formatExtendedLegend(valueColumns));
        }
        int rowOutColumnNames = rowOutStart;
        int colOut = colOutStart - 1;
        int rcCol = 0; // Excel R1C1 notation column
        int rcRow = 0; // Excel R1C1 notation row
        for ( String columnName: columnNames ) {
        	++colOut;
        	rcCol = colOut + 1;
        	if ( rcCol < blockMinColumn ) {
        		blockMinColumn = rcCol;
        	}
        	if ( rcRow > blockMaxColumn ) {
        		blockMaxColumn = rcCol;
        	}
            // 2. Write the column names
            // First try to get an existing cell for the heading
            // First try to get an existing row
        	try {
        		tk.setCellValue(sheet,rowOutColumnNames,colOut,columnName);
            }
            catch ( Exception e ) {
                // Log but let the output continue
                Message.printWarning(3, routine, "Unexpected error writing table heading at Excel row [" + rowOutColumnNames + "][" +
                    colOut + "] (" + e + ")." );
                Message.printWarning(3, routine, e);
            }
        }
        // Output the data rows
        for ( its = 0; its < tslist.size(); its++ ) {
        	writeTimeSeriesPeriod ( wb, sheet, tslist.get(its), outputStart, outputEnd, outputYearType, tk,
        		layoutBlock, layoutColumns, layoutRows,
        		styleManager, legendWorksheet, legendAddress,
        		problems );
        }
        if ( 1 == 2 ) {
        // Loop through date/time corresponding to each row in the output file
        double value;
        String valueString, dateTimeString = "", dateString = "", timeString = "";
        int row = rowOutStart;
        for ( DateTime date = new DateTime(outputStart); date.lessThanOrEqualTo(outputEnd); date.addInterval(intervalBase, intervalMult)) {
        	++row;
            // Output the date/time as per the format
            if ( dateTimeCol >= 0 ) {
                if ( dateTimeFormatterType == DateTimeFormatterType.C ) {
                    if ( dateTimeFormat == null ) {
                        // Just use the default
                        dateTimeString = date.toString();
                    }
                    else {
                        // Format according to the requested
                        dateTimeString = TimeUtil.formatDateTime(date, dateTimeFormat);
                    }
                }
            	try {
            		tk.setCellValue(sheet,row,dateTimeCol,dateTimeString);
                }
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing date/time at Excel row [" + row + "][" + col + "] (" + e + ")." );
                }
            }
            if ( dateCol >= 0 ) {
            	if ( dateFormatterType == DateTimeFormatterType.C ) {
                    if ( dateFormat == null ) {
                        // Just use the default
                        dateString = date.toString();
                    }
                    else {
                        // Format according to the requested
                        dateString = TimeUtil.formatDateTime(date, dateFormat);
                    }
                }
            	try {
            		tk.setCellValue(sheet,row,dateCol,dateString);
                }
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing date at Excel row [" + row + "][" + col + "] (" + e + ")." );
                }
            }
            if ( timeCol >= 0 ) {
            	if ( timeFormatterType == DateTimeFormatterType.C ) {
                    if ( timeFormat == null ) {
                        // Just use the default
                        timeString = date.toString();
                    }
                    else {
                        // Format according to the requested
                        timeString = TimeUtil.formatDateTime(date, timeFormat);
                    }
                }
            	try {
            		tk.setCellValue(sheet,row,timeCol,timeString);
                }
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing time at Excel row [" + row + "][" + col + "] (" + e + ")." );
                }
            }
            // Loop through the time series list and output each value
            its = -1;
            for ( TS ts : tslist ) {
                // Iterate through data in the time series and output each value according to the format.
                ++its;
                col = colOutStart + numDateTimeCol + its;
                TSData tsdata = new TSData();
                tsdata = ts.getDataPoint(date, tsdata);
                // First expand the line to replace time series properties
                value = tsdata.getDataValue();
            	try {
	                if ( ts.isDataMissing(value) ) {
	                	if ( missingValueBlank ) {
	                		// Set the cell value to blank
	                		tk.setCellBlank(sheet,row,col);
	                	}
	                	else if ( missingValueStrings[its] != null ) {
	                		// A string missing value is specified so set the cell value to the string
	                   		tk.setCellValue(sheet,row,col,missingValueStrings[its]);
	                    }
	                	else {
	                    	// Set the cell value to the numerical missing value
	                		tk.setCellValue(sheet,row,col,ts.getMissing(),cellStyles[col]);
	                	}
	                }
	                else {
	                    // Not missing so set to the numerical value
	                	tk.setCellValue(sheet,row,col,value,cellStyles[col]);
	                }
            	}
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing date at Excel row [" + row + "][" + col + "] (" + e + ")." );
                }
            }
        }
        // Now do post-data set operations
        // Set the column width
        // TODO SAM 2015-02-17 Need to enable a parameter control width similar to WriteTableToExcel
        colOut = colOutStart;
        for ( col = colOutStart; col < (colOutStart + cols); col++ ) {
        	String width = "Auto";
            if ( width != null ) {
                // Set the column width
                if ( width.equalsIgnoreCase("Auto") ) {
                    sheet.autoSizeColumn(col);
                    Message.printStatus(2,routine,"Setting column [" + col + "] width to auto.");
                }
                else {
                    // Set the column width to 1/256 of character width, max of 256*256 since 256 is max characters shown
                    try {
                        int w = Integer.parseInt(width.trim());
                        sheet.setColumnWidth(col, w);
                        Message.printStatus(2,routine,"Setting column [" + col + "] width to " + w + ".");
                    }
                    catch ( NumberFormatException e ) {
                        //problems.add ( "Column \"" + tableColumnName + "\" width \"" + width + "\" is not an integer." );
                    }
                }
            }
        }
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
Write a time series to Excel using a block for the entire period.
@param ts time series to write
@param outputStart output start
@param outputEnd output end
@param outputYearType output year type
@param tk Excel toolkit object
*/
private void writeTimeSeriesPeriod ( Workbook wb, Sheet sheet, TS ts, DateTime outputStart, DateTime outputEnd, YearType outputYearType,
	ExcelToolkit tk,
	String layoutBlock, TimeInterval layoutColumns, String layoutRows,
	TimeSeriesConditionAndStyleManager styleManager, String legendWorksheet, String legendAddress,
	List<String> problems )
{	String routine = getClass().getSimpleName() + ".writeTimeSeriesPeriod";
	DateTimeRange outputRange = TimeUtil.determineOutputYearTypeRange(outputStart, outputEnd, outputYearType);
	int yearStart = outputRange.getStart().getYear() - outputYearType.getStartYearOffset();
	int yearEnd = outputEnd.getYear();
	DateTime dateInYear = new DateTime();
	dateInYear.setMonth(1);
	dateInYear.setDay(1);
	TSData tsdata = new TSData();
	double value;
	boolean missingValueBlank = true;
	int rowOut = 0; // Output row in Excel, 0+
	int colOut = 0; // Output column in Excel, 0+
	int colOutMax = 0; // Used to set column widths for data values
    int blockMinColumn = Integer.MAX_VALUE; // These are used to help with legend positioning
    int blockMinRow = Integer.MAX_VALUE;
    int blockMaxColumn = Integer.MIN_VALUE;
    int blockMaxRow = Integer.MIN_VALUE;
    int rcCol = 0; // Excel R1C1 notation column
    int rcRow = 0; // Excel R1C1 notation row
	// The loop is in years that match the output year type
	Message.printStatus(2,routine,"Writing time series block");
	for ( int year = yearEnd; year >= yearStart; year-- ) {
		Message.printStatus(2,routine,"Processing year " + year );
		++rowOut;
    	rcRow = rowOut + 1;
    	if ( rcRow < blockMinRow ) {
    		blockMinRow = rcRow;
    	}
    	if ( rcRow > blockMaxRow ) {
    		blockMaxRow = rcRow;
    	}
    	// First write the date in column 0
		colOut = 0;
    	rcCol = colOut + 1;
    	if ( rcCol < blockMinColumn ) {
    		blockMinColumn = rcCol;
    	}
    	if ( rcCol > blockMaxColumn ) {
    		blockMaxColumn = rcCol;
    	}
		dateInYear.setYear(year); // TODO SAM 2015-03-10 make sure input is properly lined up with output year type
		dateInYear.setMonth(outputYearType.getStartMonth());
		dateInYear.setDay(1);
		DateTime dateInYearLast = new DateTime(dateInYear);
		dateInYearLast.setYear(dateInYearLast.getYear() - outputYearType.getStartYearOffset());
		dateInYearLast.setMonth(outputYearType.getEndMonth());
		dateInYearLast.setDay(TimeUtil.numDaysInMonth(outputYearType.getEndMonth(), year));
		tk.setCellValue(sheet,rowOut,colOut,"" + dateInYearLast.getYear());
		String flag;
		Cell cell;
		int intervalBase = layoutColumns.getBase();
		// Write the data values in the row, incrementing the date/time appropriately
		for ( ; dateInYear.lessThanOrEqualTo(dateInYearLast); dateInYear.addInterval(intervalBase,1) ) {
			++colOut;
			if ( colOut > colOutMax ) {
				colOutMax = colOut;
			}
        	rcCol = colOut + 1;
        	if ( rcCol < blockMinColumn ) {
        		blockMinColumn = rcCol;
        	}
        	if ( rcCol > blockMaxColumn ) {
        		blockMaxColumn = rcCol;
        	}
			tsdata = ts.getDataPoint(dateInYear, tsdata);
			// First expand the line to replace time series properties
			value = tsdata.getDataValue();
			flag = tsdata.getDataFlag();
			try {
				if ( ts.isDataMissing(value) ) {
					if ( missingValueBlank ) {
						// Set the cell value to blank
						cell = tk.setCellBlank(sheet,rowOut,colOut);
					}
					/*
					else if ( missingValueStrings[its] != null ) {
						// A string missing value is specified so set the cell value to the string
						tk.setCellValue(sheet,row,col,missingValueStrings[its]);
					}
					*/
					else {
						// Set the cell value to the numerical missing value
						cell = tk.setCellValue(sheet,rowOut,colOut,ts.getMissing(),null);//cellStyles[col]);
					}
                    if ( styleManager != null ) {
                    	// New-style...
                    	int its = 0;
                    	if ( styleManager != null ) {
                    		cell.setCellStyle(styleManager.getStyle(ts,its,value,flag));
                    	}
                    }
				}
				else {
					// Not missing so set to the numerical value
					cell = tk.setCellValue(sheet,rowOut,colOut,value,null);//cellStyles[col]);
					int its =  0;
					if ( styleManager != null ) {
						cell.setCellStyle(styleManager.getStyle(ts,its,value,flag));
					}
				}
				if ( (dateInYear.getMonth() == 2) && !dateInYear.isLeapYear() && (dateInYear.getDay() == 28)) {
					// Not a leap year but to make grid line up need to insert an extra cell for Feb 29
					// TODO SAM 2015-03-10 Need to handle style
					++colOut;
					cell = tk.setCellBlank(sheet, rowOut, colOut);
					int its = 0;
					if ( styleManager != null ) {
						//cell.setCellStyle(styleManager.getStyle(ts,its,value,flag));
					}
				}
				// Set the row height here
				// TODO SAM 2015-03-10 need to allow width and height to be set with a parameter
				if ( colOut == 1 ) { 
					Row sheetRow = sheet.getRow(rowOut);
					sheetRow.setHeight((short)100);
				}
			}
			catch ( Exception e ) {
				// Log but let the output continue
				Message.printWarning(3, routine, "Unexpected error writing data at Excel row [" + rowOut + "][" + colOut + "] (" + e + ")." );
				Message.printWarning(3, routine, e );
			}
		}
	}
    // Now do post-data set operations
    // Set the column width
    // TODO SAM 2015-02-17 Need to enable a parameter control width similar to WriteTableToExcel
	String width = "80"; // Works for daily
	if ( layoutColumns.getBase() == TimeInterval.MONTH ) {
		width = "960";
	}
    for ( colOut = 1; colOut <= colOutMax; colOut++ ) {
        if ( width != null ) {
            // Set the column width
            if ( width.equalsIgnoreCase("Auto") ) {
                sheet.autoSizeColumn(colOut);
                Message.printStatus(2,routine,"Setting column [" + colOut + "] width to auto.");
            }
            else {
                // Set the column width to 1/256 of character width, max of 256*256 since 256 is max characters shown
                try {
                    int w = Integer.parseInt(width.trim());
                    sheet.setColumnWidth(colOut, w);
                    Message.printStatus(2,routine,"Setting column [" + colOut + "] width to " + w + ".");
                }
                catch ( NumberFormatException e ) {
                    //problems.add ( "Column \"" + tableColumnName + "\" width \"" + width + "\" is not an integer." );
                }
            }
        }
    }
    // Set values used to position legend
    this.blockMinColumn = blockMinColumn;
    this.blockMinRow = blockMinRow;
    this.blockMaxColumn = blockMaxColumn;
    this.blockMaxRow = blockMaxRow;
    // Write the legend
    if ( (legendAddress != null) && !legendAddress.isEmpty() ) {
    	writeLegend ( tk, wb, sheet, styleManager, legendWorksheet, legendAddress, problems );
    }
}

}
