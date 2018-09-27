package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringDictionary;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the SetPropertyFromTable() command.
*/
public class SetPropertyFromTable_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
Property set during discovery - only name will be available.
*/
private Prop __discovery_Prop = null;

/**
Constructor.
*/
public SetPropertyFromTable_Command ()
{	super();
	setCommandName ( "SetPropertyFromTable" );
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
{	String TableID = parameters.getValue ( "TableID" );
    String PropertyName = parameters.getValue ( "PropertyName" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }
    
    if ( (PropertyName == null) || (PropertyName.length() == 0) ) {
        message = "The property name must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the property name." ) );
    }
 
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(6);
    validList.add ( "TableID" );
    validList.add ( "Column" );
    validList.add ( "ColumnIncludeFilters" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "PropertyName" );
    validList.add ( "DefaultValue" );
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
{	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new SetPropertyFromTable_JDialog ( parent, this, tableIDChoices )).ok();
}

// TODO SAM 2015-02-25 may include this in utility code at some point
/**
Find the table rows that match the include and exclude filters.
@param columnIncludeFilters include filters to match column values
@param columnExcludeFilters include filters to match column values
*/
private List<TableRecord> findTableRecords ( DataTable table,
	StringDictionary columnIncludeFilters, StringDictionary columnExcludeFilters,
	List<String>errors )
{
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
                errors.add ( "ColumnIncludeFilters column \"" + key + "\" not found in table.");
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
                errors.add ( "ColumnExcludeFilters column \"" + key + "\" not found in table.");
            }
        }
    }
    // Loop through the table and match rows
    List<TableRecord> matchedRows = new ArrayList<TableRecord>();
    boolean filterMatches;
    int icol;
    Object o;
    String s;
    for ( int irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
        filterMatches = true; // Default is match all
        if ( columnIncludeFiltersNumbers.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches
            for ( icol = 0; icol < columnIncludeFiltersNumbers.length; icol++ ) {
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
                    filterMatches = false;
                }
            }
            if ( !filterMatches ) {
                // Skip the record.
                continue;
            }
        }
        if ( columnExcludeFiltersNumbers.length > 0 ) {
            int matchesCount = 0;
            // Filters can be done on any columns so loop through to see if row matches
            for ( icol = 0; icol < columnExcludeFiltersNumbers.length; icol++ ) {
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
                continue;
            }
        }
        // If here then the row should be included
        try {
        	//Message.printStatus(2,"","Matched table row [" + irow + "]");
        	matchedRows.add(table.getRecord(irow));
        }
        catch ( Exception e ) {
        	// Should not happen since row was accessed above.
        	errors.add("Error getting table data for row [" + irow + "] (" + e + ")." );
        }
    }
    return matchedRows;
}

/**
Return the property defined in discovery phase.
*/
private Prop getDiscoveryProp ()
{
    return __discovery_Prop;
}

/**
Return the list of data objects read by this object in discovery mode.
*/
public List getObjectList ( Class c )
{
    Prop discovery_Prop = getDiscoveryProp ();
    if ( discovery_Prop == null ) {
        return null;
    }
    Prop prop = new Prop();
    // Check for TS request or class that matches the data...
    if ( c == prop.getClass() ) {
        List<Prop> v = new ArrayList<Prop>(1);
        v.add ( discovery_Prop );
        return v;
    }
    else {
        return null;
    }
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
{	String routine = getClass().getSimpleName() + ".runCommandInternal", message = "";
	int warning_level = 2;
	int log_level = 3; // Level for non-user messages for log file.
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    Boolean clearStatus = new Boolean(true); // default
	CommandProcessor processor = getCommandProcessor();
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
        setDiscoveryProp ( null );
    }

	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) && TableID.indexOf("${") >= 0 ) {
   		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    }
    String Column = parameters.getValue ( "Column" );
    if ( (Column != null) && !Column.isEmpty() && (commandPhase == CommandPhaseType.RUN) && Column.indexOf("${") >= 0 ) {
   		Column = TSCommandProcessorUtil.expandParameterValue(processor, this, Column);
    }
    String ColumnIncludeFilters = parameters.getValue ( "ColumnIncludeFilters" );
    StringDictionary columnIncludeFilters = new StringDictionary(ColumnIncludeFilters,":",",");
    // Expand the filter information
    if ( (ColumnIncludeFilters != null) && (ColumnIncludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnIncludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            // Expand the key and the value (from original key)
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            // Remove the original unexpanded entry if a different key
            if ( !key.equals(key2) ) {
            	map.remove(key);
            }
        }
    }
    String ColumnExcludeFilters = parameters.getValue ( "ColumnExcludeFilters" );
    StringDictionary columnExcludeFilters = new StringDictionary(ColumnExcludeFilters,":",",");
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.indexOf("${") >= 0) ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            key = entry.getKey();
            // Expand the key and the value (from original key)
            String key2 = TSCommandProcessorUtil.expandParameterValue(processor,this,key);
            map.put(key2, TSCommandProcessorUtil.expandParameterValue(processor,this,map.get(key)));
            // Remove the original unexpanded entry if a different key
            if ( !key.equals(key2) ) {
            	map.remove(key);
            }
        }
    }
    String PropertyName = parameters.getValue ( "PropertyName" );
    if ( (PropertyName != null) && !PropertyName.isEmpty() && (commandPhase == CommandPhaseType.RUN) && PropertyName.indexOf("${") >= 0 ) {
    	PropertyName = TSCommandProcessorUtil.expandParameterValue(processor, this, PropertyName);
    }
    String DefaultValue = parameters.getValue ( "DefaultValue" );
    if ( (DefaultValue != null) && !DefaultValue.isEmpty() && (commandPhase == CommandPhaseType.RUN) && DefaultValue.indexOf("${") >= 0 ) {
    	DefaultValue = TSCommandProcessorUtil.expandParameterValue(processor, this, DefaultValue);
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

	try {
	    Prop prop = null;
	    if ( commandPhase == CommandPhaseType.RUN ) {
		    String propertyName = TSCommandProcessorUtil.expandParameterValue(processor,this,PropertyName);
	    	// Match 1+ rows so first match can be used 
	    	List<String> errors = new ArrayList<String>();
	        List<TableRecord> records = findTableRecords ( table, columnIncludeFilters, columnExcludeFilters, errors );
	        if ( records.size() <= 0 ) {
	        	// Set to the default value if specified
	        	String propValue = null;
	        	if ( (DefaultValue == null) || DefaultValue.isEmpty() ) {
	        		// Unset the property by setting to null
	        		propValue = null;
	        	}
	        	else if ( DefaultValue.equalsIgnoreCase("Blank") ) {
	        		propValue = "";
	        	}
	        	else if ( DefaultValue.equalsIgnoreCase("Null") ) {
	        		propValue = null;
	        	}
	        	else {
	        		propValue = DefaultValue;
	        	}
	        	if ( propValue == null ) {
	        		prop = new Prop(propertyName, propValue, "", Prop.SET_AT_RUNTIME_BY_USER);
	        	}
	        	else {
	        		prop = new Prop(propertyName, propValue, propValue, Prop.SET_AT_RUNTIME_BY_USER);
	        	}
	        }
	        else {
	        	// Have a matching record so set the property based on the column value
	        	int col = table.getFieldIndex(Column);
	        	if ( col < 0 ) {
		        	message = "Table with TableID=\"" + TableID + "\" does not contain Column=\"" +
		        		Column + "\". Can't set property.";
	                Message.printWarning(warning_level,
	                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Verify that the column exists in the table." ) );
	                prop = null;
	        	}
	        	else {
		        	Object o = records.get(0).getFieldValue(col);
		        	Message.printStatus(2,routine,"Column \"" + Column + "\" col=" + col + " value="+ o);
		        	if ( o == null ) {
		        		prop = new Prop(propertyName, o, "", Prop.SET_AT_RUNTIME_BY_USER);
		        	}
		        	else {
		        		prop = new Prop(propertyName, o, "" + o, Prop.SET_AT_RUNTIME_BY_USER);
		        	}
	        	}
	        }
	    	// Set the property in the processor
	        
	    	PropList request_params = new PropList ( "" );
	    	request_params.set ( "PropertyName", propertyName );
	    	if ( prop == null ) {
	    		request_params.setUsingObject ( "PropertyValue", null );
	    	}
	    	else {
	    		request_params.setUsingObject ( "PropertyValue", prop.getValue() );
	    	}
	    	try {
	            processor.processRequest( "SetProperty", request_params);
	    	}
	    	catch ( Exception e ) {
	    		message = "Error requesting SetProperty(Property=\"" + PropertyName + "\") from processor.";
	    		Message.printWarning(log_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
	            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
	    	}
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            // Create an empty property
            prop = new Prop();
            prop.setKey ( PropertyName ); // OK if property name includes ${} in discovery mode
            prop.setHowSet(Prop.SET_UNKNOWN);
            setDiscoveryProp ( prop );
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error setting property (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
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
Set the property defined in discovery phase.
@param prop Property set during discovery phase.
*/
private void setDiscoveryProp ( Prop prop )
{
    __discovery_Prop = prop;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
	String Column = props.getValue( "Column" );
	String ColumnIncludeFilters = props.getValue( "ColumnIncludeFilters" );
	String ColumnExcludeFilters = props.getValue( "ColumnExcludeFilters" );
    String PropertyName = props.getValue( "PropertyName" );
    String DefaultValue = props.getValue( "DefaultValue" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
	if ( (Column != null) && (Column.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Column=\"" + Column + "\"" );
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
    if ( (PropertyName != null) && (PropertyName.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PropertyName=\"" + PropertyName + "\"" );
    }
    if ( (DefaultValue != null) && (DefaultValue.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DefaultValue=\"" + DefaultValue + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}