package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;

/**
This class initializes, checks, and runs the ReadPropertiesFromExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
// TODO SAM 2015-05-04 Evaluate whether to implement discoverable
public class ReadPropertiesFromExcel_Command extends AbstractCommand implements Command//, CommandDiscoverable, ObjectListProvider
{

/**
Possible values for KeepOpen parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Constructor.
*/
public ReadPropertiesFromExcel_Command ()
{	super();
	setCommandName ( "ReadPropertiesFromExcel" );
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
    String PropertyCellMap = parameters.getValue ( "PropertyCellMap" );
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
	
    if ( (PropertyCellMap == null) || (PropertyCellMap.length() == 0) ) {
        message = "The column to cell address map must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the column to cell address map." ) );
    }
    else {
        String [] pairs = PropertyCellMap.split(",");
        for ( int i = 0; i < pairs.length; i++ ) {
            String [] parts = pairs[i].split(":");
            if ( (parts.length == 1) || ((parts.length == 2) && parts[1].trim().equals("")) ) {
                message = "PropertyCellMap item \"" + pairs[i] + "\" cell address is blank.  Expecting Column:CellAddress.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the column to cell address map using Column:CellAddress,Column:CellAddress,..." ) );
            }
            else if ( parts.length != 2 ) {
                message = "PropertyCellMap item " + pairs[i] + "\" contains " + parts.length +
                    " items (delimiter is :).  Expecting Column:CellAddress.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the column to cell address map using Column:CellAddress,Column:CellAddress,..." ) );
            }
        }
    }
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(7);
    validList.add ( "InputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "KeepOpen" );
    validList.add ( "PropertyCellMap" );
    validList.add ( "BooleanProperties" );
    validList.add ( "DateTimeProperties" );
    validList.add ( "IntegerProperties" );
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
	return (new ReadPropertiesFromExcel_JDialog ( parent, this, tableIDChoices )).ok();
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
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
	int log_level = 3; // Level for non-user messages for log file.
    
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

	String InputFile = parameters.getValue ( "InputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase(_True) ) {
        keepOpen = true;
    }
    String PropertyCellMap = parameters.getValue ( "PropertyCellMap" );
    LinkedHashMap<String,String> propertyCellMap = new LinkedHashMap<String,String>();
    if ( (PropertyCellMap != null) && (PropertyCellMap.length() > 0) && (PropertyCellMap.indexOf(":") > 0) ) {
        // First break map pairs by comma
        String [] pairs = PropertyCellMap.split(",");
        // Now break pairs and put in hashtable
        String propertyName, address;
        for ( int i = 0; i < pairs.length; i++ ) {
            String [] parts = pairs[i].split(":");
            propertyName = parts[0].trim();
            address = "";
            if ( parts.length > 1 ) {
                address = parts[1].trim();
            }
            if ( address.equals("*") ) {
            	address = propertyName;
            }
            if ( propertyCellMap.get(propertyName) != null ) {
                message = "PropertyCellMap has duplicate property names \"" + propertyName + "\".";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Currently cannot specify the property name more than once." ) );
            }
            else {
                propertyCellMap.put(propertyName,address);
            }
        }
    }
    String BooleanProperties = parameters.getValue ( "BooleanProperties" );
    String [] booleanProperties = new String[0];
    if ( (BooleanProperties != null) && !BooleanProperties.equals("") ) {
    	booleanProperties = BooleanProperties.split(",");
        for ( int i = 0; i < booleanProperties.length; i++ ) {
        	booleanProperties[i] = booleanProperties[i].trim();
        }
    }
    String DateTimeProperties = parameters.getValue ( "DateTimeProperties" );
    String [] dateTimeProperties = new String[0];
    if ( (DateTimeProperties != null) && !DateTimeProperties.equals("") ) {
        dateTimeProperties = DateTimeProperties.split(",");
        for ( int i = 0; i < dateTimeProperties.length; i++ ) {
            dateTimeProperties[i] = dateTimeProperties[i].trim();
        }
    }
	String IntegerProperties = parameters.getValue ( "IntegerProperties" );
	String [] integerProperties = new String[0];
	if ( (IntegerProperties != null) && !IntegerProperties.equals("") ) {
	    integerProperties = IntegerProperties.split(",");
	    for ( int i = 0; i < integerProperties.length; i++ ) {
	        integerProperties[i] = integerProperties[i].trim();
	    }
	}

	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),InputFile) );
	if ( !IOUtil.fileExists(InputFile_full) ) {
		message += "\nThe Excel workbook file \"" + InputFile_full + "\" does not exist.";
		++warning_count;
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the Excel workbook file exists." ) );
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
        // First read the Excel cell values into a hashmap
        ExcelToolkit tk = new ExcelToolkit();
        HashMap<String,Object> propertyMap = tk.readNamedCells ( InputFile_full, Worksheet, keepOpen,
            propertyCellMap,
            booleanProperties, dateTimeProperties, integerProperties,
            problems );
        for ( String problem: problems ) {
            Message.printWarning ( 3, routine, problem );
            message = "Error reading properties from Excel: " + problem;
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.WARNING,
                message, "Check the log file for exceptions." ) );
        }
        // Transfer the objects to properties
        String propertyName;
        Object propertyObject;
        for ( Map.Entry<String,Object> entry: propertyMap.entrySet() ) {
            propertyName = entry.getKey();
            propertyObject = entry.getValue();
            // Set the property in the processor
           	PropList request_params = new PropList ( "" );
        	request_params.setUsingObject ( "PropertyName", propertyName );
        	request_params.setUsingObject ( "PropertyValue", propertyObject );
        	try {
        		if ( propertyObject != null ) {
        			// Null throws an exception
        			processor.processRequest( "SetProperty", request_params);
        		}
                // Set the 
                //if ( command_phase == CommandPhaseType.DISCOVERY ) {
                //    setDiscoveryProp ( new Prop(PropertyName,Property_Object,"" + Property_Object ) );
                //}
        	}
        	catch ( Exception e ) {
        		message = "Error making processor request SetProperty(Property=\"" + propertyName +
        			"\" - null properties are not allowed) ("+e+").";
        		Message.printWarning(log_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
        		Message.printWarning(2,routine,e);
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Check that property is not null." ) );
        	}
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error reading properties from Excel workbook file \"" + InputFile_full + "\" (" + e + ").";
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
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
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
	String PropertyCellMap = props.getValue("PropertyCellMap");
	String BooleanProperties = props.getValue("BooleanProperties");
	String DateTimeProperties = props.getValue("DateTimeProperties");
	String IntegerProperties = props.getValue("IntegerProperties");
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
    if ( (PropertyCellMap != null) && (PropertyCellMap.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PropertyCellMap=\"" + PropertyCellMap + "\"");
    }
    if ( (BooleanProperties != null) && (BooleanProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "BooleanProperties=\"" + BooleanProperties + "\"" );
    }
    if ( (DateTimeProperties != null) && (DateTimeProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DateTimeProperties=\"" + DateTimeProperties + "\"" );
    }
    if ( (IntegerProperties != null) && (IntegerProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IntegerProperties=\"" + IntegerProperties + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}