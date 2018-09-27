package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import riverside.datastore.DataStore;
import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.DMI.DMI;
import RTi.DMI.DMISelectStatement;
import RTi.DMI.DMIStoredProcedureData;
import RTi.DMI.DMIUtil;
import RTi.DMI.DatabaseDataStore;
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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.ResultSetToDataTableFactory;

/**
This class initializes, checks, and runs the ReadTableFromDataStore() command.
*/
public class ReadTableFromDataStore_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
The table that is read.
*/
private DataTable __table = null;

/**
Constructor.
*/
public ReadTableFromDataStore_Command ()
{	super();
	setCommandName ( "ReadTableFromDataStore" );
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
{   String DataStore = parameters.getValue ( "DataStore" );
    String DataStoreTable = parameters.getValue ( "DataStoreTable" );
    String Top = parameters.getValue ( "Top" );
    String Sql = parameters.getValue ( "Sql" );
    String SqlFile = parameters.getValue ( "SqlFile" );
    String DataStoreProcedure = parameters.getValue ( "DataStoreProcedure" );
    String TableID = parameters.getValue ( "TableID" );

	String warning = "";
    String message;

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (DataStore == null) || (DataStore.length() == 0) ) {
        message = "The data store must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the data store." ) );
    }
    int specCount = 0;
    if ( (Sql != null) && !Sql.equals("") ) {
        ++specCount;
    }
    if ( ((DataStoreTable != null) && (DataStoreTable.length() != 0)) ) {
        ++specCount;
    }
    if ( (SqlFile != null) && (SqlFile.length() != 0) ) {
        ++specCount;
    }
    if ( ((DataStoreProcedure != null) && (DataStoreProcedure.length() != 0)) ) {
        ++specCount;
    }
    if ( specCount == 0 ) {
        message = "The data store table, SQL statement, SQL file, or procedure must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the data store table, SQL statement, SQL file, or procedure." ) );
    }
    if ( specCount > 1 ) {
        message = "Only one of the data store table, SQL statement, SQL file, or procedure can be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the data store table, SQL statement, or SQL file." ) );
    }
    // Remove comments.  In general /* */ are the main comments supported because they are used with SQL Server
    // and Oracle and generally easy to deal with.
    String sqlNoComments = null;
    if ( (Sql != null) && !Sql.equals("") ) {
        sqlNoComments = DMIUtil.removeCommentsFromSql ( Sql ).trim();
        if ( !StringUtil.startsWithIgnoreCase(sqlNoComments, "select") ) {
            message = "The SQL statement must start with SELECT.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Update the SQL string to start with SELECT." ) );
        }
    }
    if ( (Top != null) && (Top.length() != 0) && !StringUtil.isInteger(Top)) {
        message = "The Top value (" + Top +") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the Top parameter as an integer." ) );
    }
    String SqlFile_full = null;
    if ( (SqlFile != null) && !SqlFile.isEmpty() && (SqlFile.indexOf("${") < 0) ) {
        String working_dir = null;
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
                        message, "Specify an existing SQL file." ) );
            }
    
        try {
            SqlFile_full = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,SqlFile)));
            File f = new File ( SqlFile_full );
            if ( !f.exists() ) {
                message = "The SQL file does not exist:  \"" + SqlFile_full + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the SQL file exists - may be OK if created at run time." ) );
            }
        }
        catch ( Exception e ) {
            message = "The SQL file:\n" + "    \"" + SqlFile +
            "\"\ncannot be adjusted using the working directory:\n" + "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                     message, "Verify that SQL file and working directory paths are compatible." ) );
        }
    }
    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The output table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output table identifier." ) );
    }
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(12);
    validList.add ( "DataStore" );
    validList.add ( "DataStoreCatalog" );
    validList.add ( "DataStoreSchema" );
    validList.add ( "DataStoreTable" );
    validList.add ( "DataStoreColumns" );
    validList.add ( "OrderBy" );
    validList.add ( "Top" );
    validList.add ( "Sql" );
    validList.add ( "SqlFile" );
    validList.add ( "DataStoreProcedure" );
    validList.add ( "TableID" );
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
	return (new ReadTableFromDataStore_JDialog ( parent, this )).ok();
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
	int log_level = 3; // Level for non-user messages for log file.
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
	CommandProcessor processor = getCommandProcessor();
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
		status.clearLog(CommandPhaseType.RUN);
	}
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String DataStore = parameters.getValue ( "DataStore" );
    String DataStoreCatalog = parameters.getValue ( "DataStoreCatalog" );
    if ( (DataStoreCatalog != null) && DataStoreCatalog.equals("") ) {
        DataStoreCatalog = null; // Simplifies logic below
    }
    String DataStoreSchema = parameters.getValue ( "DataStoreSchema" );
    if ( (DataStoreSchema != null) && DataStoreSchema.equals("") ) {
        DataStoreSchema = null; // Simplifies logic below
    }
    String DataStoreTable = parameters.getValue ( "DataStoreTable" );
    if ( (DataStoreTable != null) && DataStoreTable.equals("") ) {
        DataStoreTable = null; // Simplifies logic below
    }
    String DataStoreColumns = parameters.getValue ( "DataStoreColumns" );
    String OrderBy = parameters.getValue ( "OrderBy" );
    String Top = parameters.getValue ( "Top" );
    Integer top = 0;
    if ( (Top != null) && !Top.equals("") ) {
        top = Integer.parseInt(Top);
    }
    String Sql = parameters.getValue ( "Sql" );
    String SqlFile = parameters.getValue("SqlFile");
    String DataStoreProcedure = parameters.getValue("DataStoreProcedure");
    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String RowCountProperty = parameters.getValue ( "RowCountProperty" );
    
    // Find the data store to use...
    DataStore dataStore = ((StateDMI_Processor)processor).getDataStoreForName (
        DataStore, DatabaseDataStore.class );
    DMI dmi = null;
    if ( dataStore == null ) {
        message = "Could not get data store for name \"" + DataStore + "\" to query data.";
        Message.printWarning ( 2, routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that a database connection has been opened with name \"" +
                DataStore + "\"." ) );
    }
    else {
    	DatabaseDataStore dbds = (DatabaseDataStore)dataStore;
    	// Make sure database connection is open - may have timed out
    	dbds.checkDatabaseConnection();
        dmi = ((DatabaseDataStore)dataStore).getDMI();
    }
    
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}
	
    // Query the table and set in the processor...
    
    DataTable table = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        // Create the query.
        DMISelectStatement q = new DMISelectStatement(dmi);
        if ( DataStoreTable != null ) {
            StringBuffer dataStoreTable = new StringBuffer();
            if ( DataStoreCatalog != null ) {
                // Prepend the database to the table
                dataStoreTable.append(DMIUtil.escapeField(dmi,DataStoreCatalog));
            }
            if ( DataStoreSchema != null ) {
                // Prepend the database to the table
                if ( dataStoreTable.length() > 0 ) {
                    dataStoreTable.append(".");
                }
                dataStoreTable.append(DMIUtil.escapeField(dmi,DataStoreSchema));
            }
            if ( dataStoreTable.length() > 0 ) {
                dataStoreTable.append(".");
            }
            if ( DataStoreTable.indexOf('.') > 0 ) {
                // Table already has the parts so just add
                dataStoreTable.append(DMIUtil.escapeField(dmi,DataStoreTable));
            }
            else {
                // Assume it is a simple table name so escape
                dataStoreTable.append(DMIUtil.escapeField(dmi,DataStoreTable));
            }
            q.addTable(dataStoreTable.toString());
            // Always get the columns from the database to check parameters, to guard against SQL injection
            List<String> columns = null;
            try {
                columns = DMIUtil.getTableColumns(dmi,DataStoreTable);
            }
            catch ( Exception e ) {
                message = "Error getting table columns for table \"" + DataStoreTable + "\".";
                Message.printWarning ( 2, routine, message );
                status.addToLog ( commandPhase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the database for data store \"" + DataStore +
                        "\" is accessible.") );
            }
            // Get the columns to query
            if ( (DataStoreColumns != null) && !DataStoreColumns.equals("") ) {
                // Use the columns from the parameter
                String [] columnsReq = DataStoreColumns.split(",");
                for ( int i = 0; i < columnsReq.length; i++ ) {
                    if ( StringUtil.indexOf(columns,columnsReq[i]) < 0 ) {
                        message = "Database table/view does not contain columnn \"" + columnsReq[i] + "\".";
                        Message.printWarning ( 2, routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the database table/view contains column \"" + columnsReq[i] +
                                "\".") );
                    }
                    else {
                        q.addField(columnsReq[i].trim());
                    }
                }
            }
            else {
                // Use all the columns from the database
                //for ( String column: columns ) {
                //    q.addField(column);
                //}
                // This is simpler than adding the long list of columns.
                q.addField("*");
            }
            // Set the order by information to query
            if ( (OrderBy != null) && !OrderBy.equals("") ) {
                String [] columnsReq = OrderBy.split(",");
                for ( int i = 0; i < columnsReq.length; i++ ) {
                    // Check for table to guard against SQL injection
                    if ( StringUtil.indexOfIgnoreCase(columns,columnsReq[i]) < 0 ) {
                        message = "Database table/view does not contain columnn \"" + columnsReq[i] + "\".";
                        Message.printWarning ( 2, routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the database table/view contains column \"" + columnsReq[i] +
                                "\".") );
                    }
                    else {
                        q.addOrderByClause(columnsReq[i].trim());
                    }
                }
            }
            if ( (Top != null) && !Top.equals("") ) {
                q.setTop ( top );
            }
        }
        String queryString = "";
        // Execute the query as appropriate depending on how the query was specified
        ResultSet rs = null;
        try {
            if ( DataStoreTable != null ) {
                // Query using the statement that was built above
                queryString = q.toString();
                rs = dmi.dmiSelect(q);
                Message.printStatus(2, routine, "Executed query \"" + queryString + "\".");
            }
            else if ( (Sql != null) && !Sql.equals("") ) {
                // Query using the SQL string.  Expand first using ${Property} notation
                queryString = TSCommandProcessorUtil.expandParameterValue(processor, this, Sql);
                // Remove comments if Microsoft Access.  Otherwise leave because troubleshooting might be easier
                if ( dmi.getDatabaseEngineType() == DMI.DBENGINE_ACCESS ) {
                    queryString = DMIUtil.removeCommentsFromSql(queryString);
                }
                rs = dmi.dmiSelect(queryString);
                Message.printStatus(2, routine, "Executed query \"" + queryString + "\".");
            }
            else if ( (SqlFile != null) && !SqlFile.equals("") ) {
                // Query using the contents of the SQL file
                String SqlFile_full = SqlFile;
                SqlFile_full = IOUtil.verifyPathForOS(
                    IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                        TSCommandProcessorUtil.expandParameterValue(processor,this,SqlFile)));
                
                if ( !IOUtil.fileReadable(SqlFile_full) || !IOUtil.fileExists(SqlFile_full)) {
                    message = "SQL file \"" + SqlFile_full + "\" is not found or accessible.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                    status.addToLog(commandPhase,
                        new CommandLogRecord( CommandStatusType.FAILURE, message,
                            "Verify that the file exists and is readable."));
                    throw new CommandException ( message );
                }
                queryString = TSCommandProcessorUtil.expandParameterValue(processor, this,
                    StringUtil.toString(IOUtil.fileToStringList(SqlFile_full), " "));
                // Remove comments if Microsoft Access.  Otherwise leave because troubleshooting might be easier
                if ( dmi.getDatabaseEngineType() == DMI.DBENGINE_ACCESS ) {
                    queryString = DMIUtil.removeCommentsFromSql(queryString);
                }
                rs = dmi.dmiSelect(queryString);
                Message.printStatus(2, routine, "Executed query \"" + queryString + "\".");
            }
            else if ( (DataStoreProcedure != null) && !DataStoreProcedure.equals("") ) {
                // Run a stored procedure
                q.setStoredProcedureData(new DMIStoredProcedureData(dmi,DataStoreProcedure));
                rs = q.executeStoredProcedure();
                Message.printStatus(2, routine, "Executed query \"" + dmi.getLastQueryString() + "\".");
            }
            ResultSetToDataTableFactory factory = new ResultSetToDataTableFactory();
            String tableID = TSCommandProcessorUtil.expandParameterValue(processor,this,TableID);
            table = factory.createDataTable(dmi.getDatabaseEngineType(), rs, tableID);
            
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
        }
        catch ( Exception e ) {
            message = "Error querying data store \"" + DataStore + "\" using SQL \"" + queryString + " (" + e + ").";
            Message.printWarning ( 2, routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the database for data store \"" + DataStore +
                    "\" is appropriate for SQL statement: \"" + queryString + "\"." ) );
            Message.printWarning ( 3, routine, e );
        }
        finally {
            DMI.closeResultSet(rs);
        }
	    // Set the property indicating the number of rows in the table
        if ( (RowCountProperty != null) && !RowCountProperty.equals("") ) {
        	String rowCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, RowCountProperty);
            int rowCount = 0;
            if ( table != null ) {
                rowCount = table.getNumberOfRecords();
            }
            PropList request_params = new PropList ( "" );
            request_params.setUsingObject ( "PropertyName", rowCountProperty );
            request_params.setUsingObject ( "PropertyValue", new Integer(rowCount) );
            try {
                processor.processRequest( "SetProperty", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting SetProperty(Property=\"" + RowCountProperty + "\") from processor.";
                Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
            }
        }
    }
    else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        // TODO SAM 2012-01-31 Evaluate whether discover should create table with proper column names
        table = new DataTable ();
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
	String DataStore = props.getValue( "DataStore" );
	String DataStoreCatalog = props.getValue( "DataStoreCatalog" );
	String DataStoreSchema = props.getValue( "DataStoreSchema" );
	String DataStoreTable = props.getValue( "DataStoreTable" );
	String DataStoreColumns = props.getValue( "DataStoreColumns" );
	String OrderBy = props.getValue( "OrderBy" );
	String Top = props.getValue( "Top" );
	String Sql = props.getValue( "Sql" );
	String SqlFile = props.getValue( "SqlFile" );
	String DataStoreProcedure = props.getValue( "DataStoreProcedure" );
    String TableID = props.getValue( "TableID" );
    String RowCountProperty = props.getValue( "RowCountProperty" );
	StringBuffer b = new StringBuffer ();
    if ( (DataStore != null) && (DataStore.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStore=\"" + DataStore + "\"" );
    }
    if ( (DataStoreCatalog != null) && (DataStoreCatalog.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStoreCatalog=\"" + DataStoreCatalog + "\"" );
    }
    if ( (DataStoreSchema != null) && (DataStoreSchema.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStoreSchema=\"" + DataStoreSchema + "\"" );
    }
    if ( (DataStoreTable != null) && (DataStoreTable.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStoreTable=\"" + DataStoreTable + "\"" );
    }
    if ( (DataStoreColumns != null) && (DataStoreColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStoreColumns=\"" + DataStoreColumns + "\"" );
    }
    if ( (OrderBy != null) && (OrderBy.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OrderBy=\"" + OrderBy + "\"" );
    }
    if ( (Top != null) && (Top.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Top=" + Top );
    }
    if ( (Sql != null) && (Sql.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Sql=\"" + Sql + "\"" );
    }
    if ( (SqlFile != null) && (SqlFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "SqlFile=\"" + SqlFile + "\"" );
    }
    if ( (DataStoreProcedure != null) && (DataStoreProcedure.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStoreProcedure=\"" + DataStoreProcedure + "\"" );
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (RowCountProperty != null) && (RowCountProperty.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RowCountProperty=\"" + RowCountProperty +"\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}