package rti.tscommandprocessor.commands.spatial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.GIS.GeoView.ESRIShapefile;
import RTi.GIS.GeoView.GeographicProjection;
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
This class initializes, checks, and runs the WriteTableToShapefile() command.
*/
public class WriteTableToShapefile_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public WriteTableToShapefile_Command ()
{   super();
    setCommandName ( "WriteTableToShapefile" );
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
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
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
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(14);
    validList.add ( "TableID" );
    validList.add ( "OutputFile" );
    validList.add ( "LongitudeColumn" );
    validList.add ( "LatitudeColumn" );
    validList.add ( "ElevationColumn" );
    validList.add ( "WKTGeometryColumn" );
    validList.add ( "IncludeColumns" );
    validList.add ( "ExcludeColumns" );
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
    return (new WriteTableToShapefile_JDialog ( parent, this, tableIDChoices )).ok();
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
throws InvalidCommandParameterException,
CommandWarningException, CommandException
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
	if ( (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
    String OutputFile = parameters.getValue ( "OutputFile" );
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
    String ElevationColumn = parameters.getValue ( "ElevationColumn" );
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
    
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
        Message.printStatus ( 2, routine, "Writing shapefile \"" + OutputFile_full + "\"" );
        List<String> errors = new ArrayList<String>();
        writeTableToShapefile ( table, OutputFile_full, IncludeColumns, ExcludeColumns,
            LongitudeColumn, LatitudeColumn, ElevationColumn, WKTGeometryColumn, errors );
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
        message = "Unexpected error writing time series to shapefile \"" + OutputFile_full + "\" (" + e + ")";
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
    String LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
    String LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
    String ElevationColumn = parameters.getValue ( "ElevationColumn" );
    String WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
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
    return getCommandName() + "(" + b.toString() + ")";
}

/**
Write the table to a shapefile.
@param fout open PrintWriter to write to
@param table data table to write
@param errors list of error strings to be propagated to calling code
*/
private void writeTableToShapefile ( DataTable table, String outputFile, String includeColumns, String excludeColumns,
    String longitudeColumn, String latitudeColumn, String elevationColumn, String wktGeometryColumn, List<String> errors )
throws IOException
{   // Get the column numbers corresponding to the column names
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
    if ( errorCount > 0 ) {
        // Don't have needed input
        return;
    }

    int nRows = table.getNumberOfRecords();
    TableRecord rec;
    Object latitudeO, longitudeO, elevationO = null; // Can be double, int, or string
    WKTGeometryParser wktParser = null;
    String wkt = null;
    if ( doWkt ) {
        wktParser = new WKTGeometryParser();
    }
    List<GRShape> shapes = new ArrayList<GRShape>();
    GRShape shape = null; // Shape instances to add below
    GRPoint point = null;
    GRPointZM pointzm = null;
    Double x = 0.0, y = 0.0, z = 0.0;
    for ( int iRow = 0; iRow < nRows; iRow++ ) {
        try {
            rec = table.getRecord(iRow);
            longitudeO = null;
            latitudeO = null;
            if ( doPoint ) {
                // Table columns can be any type because objects are treated as strings below
                longitudeO = rec.getFieldValue(longitudeColNum);
                if ( longitudeO == null ) {
                	// Add an unknown shape type
                	shapes.add ( new GRShape() );
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
                	// Add an unknown shape type
                	shapes.add ( new GRShape() );
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
                	pointzm.x = x;
                	pointzm.y = y;
                	pointzm.z = z;
                    shapes.add ( pointzm );
                }
                else {
                	shape = point = new GRPoint ();
                	point.x = x;
                	point.y = y;
                    shapes.add ( point );
                }
            }
            if ( doWkt ) {
                // Extract shape from WKT
                wkt = rec.getFieldValueString(wktGeometryColNum);
                // Parse WKT string needs to extract coordinates
                //Message.printStatus(2, "", "Parsing \"" + wkt + "\"." );
                shape = wktParser.parseWKT(wkt);
                if ( shape == null ) {
                    //Message.printStatus(2, "", "Shape from \"" + wkt + "\" is null." );
                    shapes.add(new GRShape());
                }
                else {
                    shapes.add ( shape );
                }
            }
        }
        catch ( Exception e ) {
            errors.add("Error adding shape (" + e + ")." );
            Message.printWarning(3, "", e);
            shapes.add ( new GRShape() );
            continue;
        }
    }
    GeographicProjection gp = new GeographicProjection();
    ESRIShapefile.write(outputFile, table, shapes, gp, gp);
}

}