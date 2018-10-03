package rti.tscommandprocessor.commands.spatial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import com.google.gson.Gson;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.GIS.GeoView.GeoJSONGeometryFormatter;
import RTi.GIS.GeoView.UnrecognizedGeometryException;
import RTi.GIS.GeoView.WKTGeometryParser;
import RTi.GR.GRPoint;
import RTi.GR.GRPointZM;
import RTi.GR.GRShape;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the WriteTableToGeoJSON() command.
*/
public class WriteTableToGeoJSON_Command extends AbstractCommand implements Command, FileGenerator
{
	
/**
Possible values for Append.
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
public WriteTableToGeoJSON_Command ()
{   super();
    setCommandName ( "WriteTableToGeoJSON" );
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
{   String TableID = parameters.getValue ( "TableID" );
    String OutputFile = parameters.getValue ( "OutputFile" );
    String Append = parameters.getValue ( "Append" );
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
    String IncludeBBox = parameters.getValue ( "IncludeBBox" );
    String IncludeFeatureBBox = parameters.getValue ( "IncludeFeatureBBox" );
    String warning = "";
    String routine = getCommandName() + ".checkCommandParameters";
    String message;

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }
    
    if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify an output file." ) );
    }
    else if ( OutputFile.indexOf ("${") < 0 ) {
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
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Software error - report the problem to support." ) );
        }

        try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                    TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The output file parent directory does not exist for: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Create the output directory." ) );
            }
            f = null;
            f2 = null;
        }
        catch ( Exception e ) {
            message = "The output file:\n" +
            "    \"" + OutputFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that output file and working directory paths are compatible." ) );
        }
    }
    
    if ( Append != null && !Append.equalsIgnoreCase(_True) && 
        !Append.equalsIgnoreCase(_False) && !Append.isEmpty() ) {
        message = "Append is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Append must be specified as " + _False + " (default) or " + _True ) );
    }
    
    if ( ((LongitudeColumn == null) || (LongitudeColumn.length() == 0)) &&
        ((WKTGeometryColumn == null) || (WKTGeometryColumn.length() == 0)) ) {
        message = "The longitude column OR WKT geometry column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify the longitude OR WKT geometry column." ) );
    }
    
    if ( ((LongitudeColumn != null) && (LongitudeColumn.length() != 0)) &&
        ((WKTGeometryColumn != null) && (WKTGeometryColumn.length() != 0)) ) {
        message = "The longitude column OR WKT geometry column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify the longitude OR WKT geometry column." ) );
    }
    
    if ( ((LatitudeColumn == null) || (LatitudeColumn.length() == 0)) &&
        ((WKTGeometryColumn == null) || (WKTGeometryColumn.length() == 0)) ) {
        message = "The latitude column OR WKT geometry column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify the latitude column OR WKT geometry column." ) );
    }
    
    if ( ((LatitudeColumn != null) && (LatitudeColumn.length() != 0)) &&
        ((WKTGeometryColumn != null) && (WKTGeometryColumn.length() != 0)) ) {
        message = "The latitude column OR WKT geometry column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify the latitude column OR WKT geometry column." ) );
    }
    
    if ( IncludeBBox != null && !IncludeBBox.equalsIgnoreCase(_True) && 
        !IncludeBBox.equalsIgnoreCase(_False) && !IncludeBBox.isEmpty() ) {
        message = "IncludeBBox is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "IncludeBBox must be specified as " + _False + " or " + _True + " (default)") );
    }
    
    if ( IncludeFeatureBBox != null && !IncludeFeatureBBox.equalsIgnoreCase(_True) && 
        !IncludeFeatureBBox.equalsIgnoreCase(_False) && !IncludeFeatureBBox.isEmpty() ) {
        message = "IncludeFeatureBBox is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "IncludeFeatureBBox must be specified as " + _False + " or " + _True + " (default)") );
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(15);
    validList.add ( "TableID" );
    validList.add ( "OutputFile" );
    validList.add ( "Append" );
    validList.add ( "LongitudeColumn" );
    validList.add ( "LatitudeColumn" );
    validList.add ( "ElevationColumn" );
    validList.add ( "WKTGeometryColumn" );
    validList.add ( "CRSText" );
    validList.add ( "IncludeBBox" );
    validList.add ( "IncludeFeatureBBox" );
    validList.add ( "IncludeColumns" );
    validList.add ( "ExcludeColumns" );
    validList.add ( "JavaScriptVar" );
    validList.add ( "PrependText" );
    validList.add ( "AppendText" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{   List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
    return (new WriteTableToGeoJSON_JDialog ( parent, this, tableIDChoices )).ok();
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
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   String routine = getClass().getSimpleName() + ".runCommand", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    
    // Clear the output file
    
    setOutputFile ( null );
    
    // Check whether the processor wants output files to be created...

    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandProcessor processor = getCommandProcessor();
    if ( !TSCommandProcessorUtil.getCreateOutput(processor) ) {
            Message.printStatus ( 2, routine,
            "Skipping \"" + toString() + "\" because output is not being created." );
    }
    
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

    PropList parameters = getCommandParameters();
    String TableID = parameters.getValue ( "TableID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
    String OutputFile = parameters.getValue ( "OutputFile" ); // Expanded below
    String Append = parameters.getValue ( "Append" );
    boolean append = false;
    if ( (Append != null) && Append.equalsIgnoreCase(_True) ) {
    	append = true;
    }
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (LongitudeColumn != null) && (LongitudeColumn.indexOf("${") >= 0) ) {
		LongitudeColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, LongitudeColumn);
	}
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (LatitudeColumn != null) && (LatitudeColumn.indexOf("${") >= 0) ) {
		LatitudeColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, LatitudeColumn);
	}
    String ElevationColumn = parameters.getValue ( "ElevationColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (ElevationColumn != null) && (ElevationColumn.indexOf("${") >= 0) ) {
		ElevationColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, ElevationColumn);
	}
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (WKTGeometryColumn != null) && (WKTGeometryColumn.indexOf("${") >= 0) ) {
		WKTGeometryColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, WKTGeometryColumn);
	}
    String CRSText = parameters.getValue ( "CRSText" );
	if ( (commandPhase == CommandPhaseType.RUN) && (CRSText != null) && (CRSText.indexOf("${") >= 0) ) {
		CRSText = TSCommandProcessorUtil.expandParameterValue(processor, this, CRSText);
	}
	if ( CRSText != null ) {
		// Replace literal \" with double quote for output
		CRSText = CRSText.replace("\\\"","\"");
		// Make sure there is a comma at the end
		if ( !CRSText.endsWith(",") ) {
			CRSText = CRSText + ",";
		}
	}
    String IncludeBBox = parameters.getValue ( "IncludeBBox" );
    boolean includeBBox = true; // Default
    if ( (IncludeBBox != null) && IncludeBBox.equalsIgnoreCase(_False) ) {
    	includeBBox = false;
    }
    String IncludeFeatureBBox = parameters.getValue ( "IncludeFeatureBBox" );
    boolean includeFeatureBBox = true; // Default
    if ( (IncludeFeatureBBox != null) && IncludeFeatureBBox.equalsIgnoreCase(_False) ) {
    	includeFeatureBBox = false;
    }
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
	if ( (commandPhase == CommandPhaseType.RUN) && (IncludeColumns != null) && (IncludeColumns.indexOf("${") >= 0) ) {
		IncludeColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, IncludeColumns);
	}
    String [] includeColumns = null;
    if ( (IncludeColumns != null) && !IncludeColumns.isEmpty() ) {
        // Use the provided columns
        includeColumns = IncludeColumns.split(",");
        for ( int i = 0; i < includeColumns.length; i++ ) {
            includeColumns[i] = includeColumns[i].trim();
        }
    }
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
	if ( (commandPhase == CommandPhaseType.RUN) && (ExcludeColumns != null) && (ExcludeColumns.indexOf("${") >= 0) ) {
		ExcludeColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, ExcludeColumns);
	}
    String [] excludeColumns = null;
    if ( (ExcludeColumns != null) && !ExcludeColumns.isEmpty() ) {
        // Use the provided columns
        excludeColumns = ExcludeColumns.split(",");
        for ( int i = 0; i < excludeColumns.length; i++ ) {
            excludeColumns[i] = excludeColumns[i].trim();
        }
    }
    String JavaScriptVar = parameters.getValue ( "JavaScriptVar" );
	if ( (commandPhase == CommandPhaseType.RUN) && (JavaScriptVar != null) && (JavaScriptVar.indexOf("${") >= 0) ) {
		JavaScriptVar = TSCommandProcessorUtil.expandParameterValue(processor, this, JavaScriptVar);
	}
    String PrependText = parameters.getValue ( "PrependText" );
	if ( (PrependText != null) && (PrependText.indexOf("${") >= 0) ) {
		PrependText = TSCommandProcessorUtil.expandParameterValue(processor, this, PrependText);
	}
    String AppendText = parameters.getValue ( "AppendText" );
	if ( (AppendText != null) && (AppendText.indexOf("${") >= 0) ) {
		AppendText = TSCommandProcessorUtil.expandParameterValue(processor, this, AppendText);
	}
    
    // Get the table to process.

    DataTable table = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
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
        }
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings for command parameters.";
        Message.printWarning ( 2,
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine,message);
        throw new InvalidCommandParameterException ( message );
    }

    String OutputFile_full = OutputFile;
    try {
        // Convert to an absolute path...
        OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
        IOUtil.enforceFileExtension(OutputFile_full, "shp");
        Message.printStatus ( 2, routine, "Writing GeoJSON file \"" + OutputFile_full + "\"" );
        List<String> errors = new ArrayList<String>();
        writeTableToGeoJSON ( table, OutputFile_full, append, includeColumns, excludeColumns,
            LongitudeColumn, LatitudeColumn, ElevationColumn, WKTGeometryColumn, CRSText, includeBBox, includeFeatureBBox,
            JavaScriptVar, PrependText, AppendText, errors );
        for ( String error : errors ) {
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, error );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    error, "Check log file for details." ) );
        }
        // Save the output file name...
        setOutputFile ( new File(OutputFile_full));
    }
    catch ( Exception e ) {
        message = "Unexpected error writing time series to GeoJSON file \"" + OutputFile_full + "\" (" + e + ")";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Check log file for details." ) );
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
{   if ( parameters == null ) {
        return getCommandName() + "()";
    }
    String TableID = parameters.getValue( "TableID" );
    String OutputFile = parameters.getValue ( "OutputFile" );
    String Append = parameters.getValue ( "Append" );
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
    String ElevationColumn = parameters.getValue ( "ElevationColumn" );
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
    String CRSText = parameters.getValue ( "CRSText" );
    String IncludeBBox = parameters.getValue ( "IncludeBBox" );
    String IncludeFeatureBBox = parameters.getValue ( "IncludeFeatureBBox" );
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
    String JavaScriptVar = parameters.getValue ( "JavaScriptVar" );
    String PrependText = parameters.getValue ( "PrependText" );
    String AppendText = parameters.getValue ( "AppendText" );
    StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
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
        b.append ( "Append=\"" + Append + "\"" );
    }
    if ( (LongitudeColumn != null) && (LongitudeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LongitudeColumn=\"" + LongitudeColumn + "\"" );
    }
    if ( (LatitudeColumn != null) && (LatitudeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "LatitudeColumn=\"" + LatitudeColumn + "\"" );
    }
    if ( (ElevationColumn != null) && (ElevationColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ElevationColumn=\"" + ElevationColumn + "\"" );
    }
    if ( (WKTGeometryColumn != null) && (WKTGeometryColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "WKTGeometryColumn=\"" + WKTGeometryColumn + "\"" );
    }
    if ( (CRSText != null) && (CRSText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CRSText=\"" + CRSText + "\"" );
    }
    if ( (IncludeBBox != null) && (IncludeBBox.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeBBox=" + IncludeBBox );
    }
    if ( (IncludeFeatureBBox != null) && (IncludeFeatureBBox.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeFeatureBBox=" + IncludeFeatureBBox );
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
    if ( (JavaScriptVar != null) && (JavaScriptVar.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "JavaScriptVar=\"" + JavaScriptVar + "\"" );
    }
    if ( (PrependText != null) && (PrependText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PrependText=\"" + PrependText + "\"" );
    }
    if ( (AppendText != null) && (AppendText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "AppendText=\"" + AppendText + "\"" );
    }
    return getCommandName() + "(" + b.toString() + ")";
}

// TODO SAM 2016-01-16 Need to evaluate GeoJSON Java toolkits once project is updated to use Maven.
/**
Write the table to a GeoJSON file.  This uses simple print statements.
@param fout open PrintWriter to write to
@param table data table to write
@param crsText text for full "crs" data in GeoJSON, to insert in the output
@param includeBBox whether or not to include "bbox" in output at main level
@param errors list of error strings to be propagated to calling code
*/
private void writeTableToGeoJSON ( DataTable table, String outputFile, boolean append, String [] includeColumns, String [] excludeColumns,
    String longitudeColumn, String latitudeColumn, String elevationColumn, String wktGeometryColumn, String crsText,
    boolean includeBBox, boolean includeFeatureBBox, String javaScriptVar,
    String prependText, String appendText, List<String> errors )
throws IOException
{   PrintWriter fout = null;
	if ( appendText == null ) {
		appendText = "";
	}
	try {
		// Open the output file
		FileOutputStream fos = new FileOutputStream ( outputFile, append );
		if ( includeColumns == null ) {
			includeColumns = new String[0]; // Simplifies error handling
		}
		if ( excludeColumns == null ) {
			excludeColumns = new String[0]; // Simplifies error handling
		}
	    fout = new PrintWriter ( fos );
	    // Indentations
	    String i1 = "  ";
	    String i2 = "    ";
	    String i3 = "      ";
	    String i4 = "        ";
		// Get the column numbers corresponding to the column names
	    int errorCount = 0;
	    boolean doPoint = false;
	    boolean doWkt = false;
	    boolean doElevation = false;
	    // WKT trumps point data
	    if ( (wktGeometryColumn != null) && !wktGeometryColumn.equals("") ) {
	        doWkt = true;
	    }
	    else {
	        // Rely on point data
	        if ( (latitudeColumn != null) && !latitudeColumn.equals("") &&
	            (longitudeColumn != null) && !longitudeColumn.equals("") ) {
	            doPoint = true;
	        }
	        if ( (elevationColumn != null) && !elevationColumn.equals("") ) {
	            doElevation = true;
	        }
	    }
	    int longitudeColNum = -1;
	    int latitudeColNum = -1;
	    if ( doPoint ) {
	        try {
	            longitudeColNum = table.getFieldIndex(longitudeColumn);
	            if ( longitudeColNum < 0 ) {
	                errors.add ( "Longitude column \"" + longitudeColumn + "\" not found in table.");
	                ++errorCount;
	            }
	        }
	        catch ( Exception e ) {
	            errors.add ( "Error determining longitude column number \"" + longitudeColumn + "\" (" + e + ").");
	        }
	        try {
	            latitudeColNum = table.getFieldIndex(latitudeColumn);
	            if ( latitudeColNum < 0 ) {
	                errors.add ( "Latitude column \"" + latitudeColumn + "\" not found in table.");
	                ++errorCount;
	            }
	        }
	        catch ( Exception e ) {
	            errors.add ( "Error determining latitude column number \"" + latitudeColumn + "\" (" + e + ").");
	        }
	    }
	    int elevationColNum = -1;
	    try {
	        if ( doElevation ) {
	            elevationColNum = table.getFieldIndex(elevationColumn);
	            if ( elevationColNum < 0 ) {
	                errors.add ( "Elevation column \"" + elevationColumn + "\" not found in table.");
	                ++errorCount;
	            }
	        }
	    }
	    catch ( Exception e ) {
	        errors.add ( "Error determining elevation column number \"" + elevationColumn + "\" (" + e + ").");
	    }
	    int wktGeometryColNum = -1;
	    if ( doWkt ) {
	        try {
	            wktGeometryColNum = table.getFieldIndex(wktGeometryColumn);
	            if ( wktGeometryColNum < 0 ) {
	                errors.add ( "WKT geometry column \"" + wktGeometryColumn + "\" not found in table.");
	                ++errorCount;
	            }
	        }
	        catch ( Exception e ) {
	            errors.add ( "Error determining WKT geometry column number \"" + wktGeometryColumn + "\" (" + e + ").");
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
	                errors.add("Table column to include in output \"" + includeColumns[i] + "\" does not exist in table.");
	                includeColumnNumbers[i] = -1;
	            }
	        	// Automatically ignore the WKT column because it is verbose
	        	if ( doWkt && includeColumns[i].equalsIgnoreCase(wktGeometryColumn) ) {
	        		includeColumnNumbers[i] = -1;
	        	}
	        }
	    }
	    else {
	        // Output all the columns (except WKT)
	        includeColumnNumbers = new int[table.getNumberOfFields()];
	        includeColumns = new String[table.getNumberOfFields()];
	        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
	            includeColumnNumbers[i] = i;
	            includeColumns[i] = table.getFieldName(i);
	            // Ignore the WKT column because it is verbose
	        	if ( doWkt && includeColumns[i].equalsIgnoreCase(wktGeometryColumn) ) {
	        		includeColumnNumbers[i] = -1;
	        	}
	        }
	    }
	    // Now remove output columns that are to be excluded.
	    // Do so by setting column numbers for excluded columns to -1
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
	                errors.add ( "Table column to exclude in output \"" + excludeColumns[i] + "\" does not exist in table." );
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
        // Need to get the column names for the included columns
        String [] includeColumns2 = new String[includeColumnNumbers2.length];
        for ( int iCol = 0; iCol < includeColumns2.length; iCol++ ) {
        	includeColumns2[iCol] = table.getFieldName(includeColumnNumbers[iCol]);
        }
	    if ( errorCount > 0 ) {
	        // Don't have needed input
	        return;
	    }
	    
	    // Output GeoJSON intro
	    if ( (prependText != null) && !prependText.isEmpty() ) {
	    	fout.print(prependText);
	    }
	    if ( (javaScriptVar != null) && !javaScriptVar.isEmpty() ) {
	    	fout.print( "var " + javaScriptVar + " = {\n" );
	    }
	    else {
	     	fout.print("{\n" );
	    }
	    fout.print( i1 + "\"type\": \"FeatureCollection\",\n");
	    if ( (crsText != null) && !crsText.isEmpty() ) {
	    	// TODO SAM 2016-07-19 Improve indentation when on multiple lines
	    	fout.print ( i1 + crsText + "\n");
	    }
	    GeoJSONGeometryFormatter geoJSONFormatter = new GeoJSONGeometryFormatter(2);
	    StringBuilder buffer = new StringBuilder(); // Need if "bbox" is requested to process all data
	    if ( !includeBBox ) {
	    	// Can print directly
	    	fout.print( i1 + "\"features\": [\n");
	    }
	
	    int nRows = table.getNumberOfRecords();
	    TableRecord rec;
	    Object latitudeO, longitudeO, elevationO = null; // Can be double, int, or string
	    WKTGeometryParser wktParser = null;
	    Gson gson = new Gson();
	    String wkt = null;
	    if ( doWkt ) {
	        wktParser = new WKTGeometryParser();
	    }
	    GRShape shape = null; // Shape instances to add below
	    GRPoint point = null;
	    GRPointZM pointzm = null;
	    Double x = 0.0, y = 0.0, z = 0.0;
	    int nRows0 = nRows - 1;
	    Object o = null; // Object from table
	    double xminAll = Double.MAX_VALUE;
	    double xmaxAll = -Double.MAX_VALUE;
	    double yminAll = Double.MAX_VALUE;
	    double ymaxAll = -Double.MAX_VALUE;
	    double zminAll = 0.0;
	    double zmaxAll = 0.0;
	    int dim = 2; // Dimension for shape
	    for ( int iRow = 0; iRow < nRows; iRow++ ) {
	        try {
	            rec = table.getRecord(iRow);
	            longitudeO = null;
	            latitudeO = null;
	            if ( doPoint ) {
	                // Table columns can be any type because objects are treated as strings below
	                longitudeO = rec.getFieldValue(longitudeColNum);
	                if ( longitudeO == null ) {
	                    continue;
	                }
	                else if ( longitudeO instanceof Double ) {
	                	x = (Double)longitudeO;
	                }
	                else if ( longitudeO instanceof Float ) {
	                	x = 0.0 + (Float)longitudeO;
	                }
	                else if ( latitudeO instanceof Integer ) {
	                	x = 0.0 + (Integer)latitudeO;
	                }
	                latitudeO = rec.getFieldValue(latitudeColNum);
	                if ( latitudeO == null ) {
	                    continue;
	                }
	                else if ( latitudeO instanceof Double ) {
	                	y = (Double)latitudeO;
	                }
	                else if ( latitudeO instanceof Float ) {
	                	y = 0.0 + (Float)latitudeO;
	                }
	                else if ( latitudeO instanceof Integer ) {
	                	y = 0.0 + (Integer)latitudeO;
	                }
	                if ( doElevation ) {
	                	dim = 3;
	                    elevationO = rec.getFieldValue(elevationColNum);
	                    if ( latitudeO instanceof Double ) {
	                    	z = (Double)elevationO;
	                    }
	                    else if ( latitudeO instanceof Float ) {
	                    	z = 0.0 + (Float)elevationO;
	                    }
	                    else if ( latitudeO instanceof Integer ) {
	                    	z = 0.0 + (Integer)elevationO;
	                    }
	                }
	                if ( elevationColNum >= 0 ) {
	                	shape = pointzm = new GRPointZM();
	                	pointzm.setXYZ(x,y,z);
	                }
	                else {
	                	shape = point = new GRPoint ();
	                	point.setXY(x,y);
	                }
	            }
	            if ( doWkt ) {
	                // Extract shape from WKT
	                wkt = rec.getFieldValueString(wktGeometryColNum);
	                // Parse WKT string needs to extract coordinates
	                //Message.printStatus(2, "", "Parsing \"" + wkt + "\"." );
	                try {
	                	shape = wktParser.parseWKT(wkt);
		                if ( shape == null ) {
		                    //Message.printStatus(2, "", "Shape from \"" + wkt + "\" is null." );
		                    continue;
		                }
	                }
	                catch ( UnrecognizedGeometryException ue ) {
	                	errors.add("Unrecognized WKT geometry type (" + ue + ") table row " + (iRow + 1) );
	                	continue;
	                }
	                catch ( Exception e ) {
	                	errors.add("Exception adding shape (" + e + ") table row " + (iRow + 1) );
	                	Message.printWarning(3,"",e);
	                	continue;
	                }
	            }
	            // If get to here it is OK to output the feature and table columns as related properties.
	            if ( includeBBox ) {
	            	buffer.append( i2 + "{\n");
	            	buffer.append( i3 + "\"type\": \"Feature\",\n");
	            	if ( includeFeatureBBox ) {
	        	    	if ( dim == 2 ) {
	        	    		buffer.append ( i3 + "\"bbox\": [" + shape.xmin + ", " + shape.ymin + ", " + shape.xmax + ", " + shape.ymax + "],\n" );
	        	    	}
	        	    	else {
	        	    		// TODO SAM 2016-07-20 - need to enable Z
	        	    		fout.print ( i3 + "\"bbox\": [" + shape.xmin + ", " + shape.ymin + ", 0" + // shape.zmin + ", "
	        	    				+ shape.xmax + ", " + shape.ymax + ", " // + shape.zmax
	        	    				+ "0],\n" );
	        	    	}
	        	    }
	            	buffer.append( i3 + "\"properties\": {\n");
	            }
	            else {
		    	    fout.print( i2 + "{\n");
		    	    fout.print( i3 + "\"type\": \"Feature\",\n");
		    	    if ( includeFeatureBBox ) {
	        	    	if ( dim == 2 ) {
	        	    		fout.print ( i3 + "\"bbox\": [" + shape.xmin + ", " + shape.ymin + ", " + shape.xmax + ", " + shape.ymax + "],\n" );
	        	    	}
	        	    	else {
	        	    		// TODO SAM 2016-07-20 - need to enable Z
	        	    		fout.print ( i3 + "\"bbox\": [" + shape.xmin + ", " + shape.ymin + ", " + // shape.zmin +
	        	    		"0, " + shape.xmax + ", " + shape.ymax + ", " +
	        	    		//shape.zmax +
	        	    		"0],\n" );
	        	    	}
	        	    }
		    	    fout.print( i3 + "\"properties\": {\n");
	            }
	    	    // Loop through the columns in the table and output as properties
	    	    // - Do not output WKT property but do output latitude and longitude
	    	    int iCol0 = includeColumnNumbers.length - 1;
	    	    for ( int iCol = 0; iCol < includeColumnNumbers.length; iCol++ ) {
	    	    	try {
	    	    		// Gson will properly output with quotes, etc.
	    	    		o = table.getFieldValue(iRow, includeColumnNumbers[iCol]);
	    	    		if ( includeBBox ) {
	    	    			buffer.append( i4 + "\"" + includeColumns2[iCol] + "\": " + gson.toJson(o) );
	    	    		}
	    	    		else {
	    	    			fout.print( i4 + "\"" + includeColumns2[iCol] + "\": " + gson.toJson(o) );
	    	    		}
			    	    if ( iCol != iCol0 ) {
			    	    	if ( includeBBox ) {
			    	    		buffer.append ( ",\n" );
		    	    		}
		    	    		else {
		    	    			fout.print ( ",\n" );
		    	    		}
			    	    }
			    	    else {
			    	    	if ( includeBBox ) {
			    	    		buffer.append ( "\n" );
		    	    		}
		    	    		else {
		    	    			fout.print ( "\n" );
		    	    		}
			    	    }
	    	    	}
	    	    	catch ( Exception e ) {
	    	    		continue;
	    	    	}
	    	    }
	    	    if ( includeBBox ) {
	    	    	buffer.append( i3 + "},\n");
	    		}
	    		else {
	    			fout.print( i3 + "},\n");
	    		}
	    	    // Output the geometry based on the shape type
	    	    if ( includeBBox ) {
	    	    	buffer.append( i3 + "\"geometry\": " + geoJSONFormatter.format(shape, true, i3) );
	    	    	if ( shape.xmax > xmaxAll ) {
	    	    		xmaxAll = shape.xmax;
	    	    	}
	    	    	if ( shape.xmin < xminAll ) {
	    	    		xminAll = shape.xmin;
	    	    	}
	    	    	if ( shape.ymax > ymaxAll ) {
	    	    		ymaxAll = shape.ymax;
	    	    	}
	    	    	if ( shape.ymin < yminAll ) {
	    	    		yminAll = shape.ymin;
	    	    	}
	    		}
	    		else {
	    			fout.print( i3 + "\"geometry\": " + geoJSONFormatter.format(shape, true, i3) );
	    		}
	    	    if ( iRow == nRows0 ) {
	    	    	if ( includeBBox ) {
	    	    		buffer.append( i2 + "}\n");
    	    		}
    	    		else {
    	    			fout.print( i2 + "}\n");
    	    		}
	    	    }
	    	    else {
	    	    	if ( includeBBox ) {
	    	    		buffer.append( i2 + "},\n");
    	    		}
    	    		else {
    	    			fout.print( i2 + "},\n");
    	    		}
	    	    }
	        }
	        catch ( Exception e ) {
	            errors.add("Error adding shape (" + e + ")." );
	            Message.printWarning(3, "", e);
	            continue;
	        }
	    }
	    if ( includeBBox ) {
		    // Had to buffer so finish printing
	    	if ( dim == 2 ) {
	    		fout.print ( i1 + "\"bbox\": [" + xminAll + ", " + yminAll + ", " + xmaxAll + ", " + ymaxAll + "],\n" );
	    	}
	    	else {
	    		fout.print ( i1 + "\"bbox\": [" + xminAll + ", " + yminAll + ", " + zminAll + ", " + xmaxAll + ", " + ymaxAll + ", " + zmaxAll + "],\n" );
	    	}
		    fout.print( i1 + "\"features\": [\n");
		    fout.print( i1 + buffer);
	    }
	    fout.print( i1 + "]\n"); // End features
	    if ( (javaScriptVar != null) && !javaScriptVar.isEmpty() ) {
	    	fout.print( "};" + appendText + "\n"); // End GeoJSON
	    }
	    else {
	    	fout.print( "}" + appendText + "\n"); // End GeoJSON
	    }
	}
	finally {
	    try {
	        fout.close();
	    }
	    catch ( Exception e ) {
	    }
	}
}

}