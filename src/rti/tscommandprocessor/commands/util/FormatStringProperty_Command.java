package rti.tscommandprocessor.commands.util;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
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
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the FormatStringProperty() command.
*/
public class FormatStringProperty_Command extends AbstractCommand implements CommandDiscoverable, ObjectListProvider
{
	
/**
Possible value for PropertyType.
*/
protected final String _DateTime = "DateTime";
protected final String _Double = "Double";
protected final String _Integer = "Integer";
protected final String _String = "String";
	
/**
Property set during discovery.
*/
private Prop __discovery_Prop = null;
    
/**
Constructor.
*/
public FormatStringProperty_Command ()
{   super();
    setCommandName ( "FormatStringProperty" );
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
{   String Format = parameters.getValue ( "Format" );
    String OutputProperty = parameters.getValue ( "OutputProperty" );
    String PropertyType = parameters.getValue ( "PropertyType" );
    String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (Format == null) || Format.equals("") ) {
        message = "The format must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a format to process input." ) );
    }

    if ( (OutputProperty == null) || OutputProperty.equals("") ) {
        message = "The output property must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide a property name for output." ) );
    }
    if ( (PropertyType != null) && !PropertyType.isEmpty() && !PropertyType.equalsIgnoreCase(_DateTime) &&
    	!PropertyType.equalsIgnoreCase(_Double) && !PropertyType.equalsIgnoreCase(_Integer) && !PropertyType.equalsIgnoreCase(_String) ) {
		message = "The property type is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the property type as " + _DateTime + ", " + _Double + ", " +
                	_Integer + ", or " + _String + " (default)." ) );
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(4);
    validList.add ( "InputProperties" );
    validList.add ( "Format" );
    validList.add ( "OutputProperty" );
    validList.add ( "PropertyType" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
    
    if ( warning.length() > 0 ) {
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag(command_tag,warning_level), warning );
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
{   return (new FormatStringProperty_JDialog ( parent, this )).ok();
}

/**
Format a string property using input from other properties.
@param processor command processor from which to get input property values
@param inputProperties the name of the first column to use as input
@param format the operator to execute for processing data
@param outputProperty the name of the output column
@param problems a list of strings indicating problems during processing
@return the formatted string property
*/
public String format ( StateDMI_Processor processor, String [] inputProperties, String format,
	String outputProperty, List<String> problems )
{   //String routine = getClass().getSimpleName() + ".format" ;

    // Loop through the records, get the input column objects, and format for output
    String outputVal = null;
    List<Object> values = new ArrayList<Object>();
    // Get the input values
    values.clear();
    for ( int iProp = 0; iProp < inputProperties.length; iProp++ ) {
        try {
            values.add(processor.getPropContents(inputProperties[iProp]));
        }
        catch ( Exception e ) {
            problems.add ( "Error getting property value for \"" + inputProperties[iProp] + "\" (" + e + ")." );
            values.clear();
            break;
        }
    }
    if ( inputProperties.length != values.size() ) {
        // Don't have the right number of values from the number of specified input properties
    	problems.add ( "Have " + inputProperties.length + " input properties but only found " +
    		values.size() + " corresponding values.  Cannot format string." );
        outputVal = null;
    }
    else {
        //Message.printStatus(2, routine, "format=\"" + format + "\"" );
        //for ( int i = 0; i < values.size(); i++ ) {
        //    Message.printStatus(2, routine, "value=\"" + values.get(i) + "\"" );
        //}
        outputVal = StringUtil.formatString(values,format);
    }
    return outputVal;
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
For this class Prop can be requested.
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{
    Prop discovery_Prop = getDiscoveryProp ();
    if ( discovery_Prop == null ) {
        return null;
    }
    Prop prop = new Prop();
    // Check for TS request or class that matches the data...
    if ( c == prop.getClass() ) {
        List<T> v = new Vector<T> (1);
        v.add ( (T)discovery_Prop );
        return v;
    }
    else {
        return null;
    }
}

// Parse command is in the base class

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
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
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
@param commandPhase The command phase that is being run (RUN or DISCOVERY).
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
public void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   String message, routine = getCommandName() + "_Command.runCommandInternal";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Level for non-use messages for log file.
    
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
    PropList parameters = getCommandParameters();
    
	if ( commandPhase == CommandPhaseType.DISCOVERY ) {
		setDiscoveryProp(null);
	}
    
    // Get the input parameters...
    
    String InputProperties = parameters.getValue ( "InputProperties" );
    String [] inputPropertyNames = new String[0];
    if ( (InputProperties != null) && !InputProperties.equals("") ) {
        if ( InputProperties.indexOf(",") < 0 ) {
            inputPropertyNames = new String[1];
            inputPropertyNames[0] = InputProperties.trim();
        }
        else {
            inputPropertyNames = InputProperties.split(",");
            for ( int i = 0; i < inputPropertyNames.length; i++ ) {
                inputPropertyNames[i] = inputPropertyNames[i].trim();
            }
        }
    }
    String Format = parameters.getValue ( "Format" );
    String OutputProperty = parameters.getValue ( "OutputProperty" );
    String PropertyType = parameters.getValue ( "PropertyType" );
    String propertyType = _String; // default
    if ( (PropertyType != null) && !PropertyType.isEmpty() ) {
    	propertyType = PropertyType;
    }
    
    if ( warning_count > 0 ) {
        // Input error...
        message = "Insufficient input to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
        Message.printWarning(3, routine, message );
        throw new CommandException ( message );
    }
    
    // Now process...

    List<String> problems = new ArrayList<String>();
    try {
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		String stringProp = format ( (StateDMI_Processor)processor, inputPropertyNames, Format, OutputProperty, problems );
    		// Replace "\n" in format with actual newline
    		if ( stringProp != null ) {
    			stringProp = stringProp.replace("\\n","\n");
    		}
    		// Create an output property of the requested type
    		Object propObject = null;
    		if ( propertyType.equalsIgnoreCase(_DateTime) ) {
    			propObject = DateTime.parse(stringProp);
    		}
    		else if ( propertyType.equalsIgnoreCase(_Double) ) {
    			propObject = new Double(stringProp);
    		}
    		else if ( propertyType.equalsIgnoreCase(_Integer) ) {
    			propObject = new Integer(stringProp);
    		}
    		else {
    			// Default
    			propObject = stringProp;
    		}
	    	// Set the new property in the processor
    	    PropList request_params = new PropList ( "" );
	    	request_params.setUsingObject ( "PropertyName", OutputProperty );
	    	request_params.setUsingObject ( "PropertyValue", propObject );
	    	try {
	            processor.processRequest( "SetProperty", request_params);
	    	}
	    	catch ( Exception e ) {
	    		message = "Error requesting SetProperty(Property=\"" + OutputProperty + "\") from processor.";
	    		Message.printWarning(log_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
	            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
	    	}
    	}
		else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
			// Set a property that will be listed for choices
			Object propertyObject = null;
    		if ( propertyType.equalsIgnoreCase(_DateTime) ) {
    			propertyObject = new DateTime(DateTime.DATE_CURRENT);
    		}
    		else if ( propertyType.equalsIgnoreCase(_Double) ) {
    			propertyObject = new Double(1.0);
    		}
    		else if ( propertyType.equalsIgnoreCase(_Integer) ) {
    			propertyObject = new Integer(1);
    		}
    		else {
    			propertyObject = "";
    		}
    		Prop prop = new Prop(OutputProperty, propertyObject, "");
            prop.setHowSet(Prop.SET_UNKNOWN);
    		setDiscoveryProp ( prop );
		}
    }
    catch ( Exception e ) {
        message = "Unexpected error formatting string (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
    finally {
	    int MaxWarnings_int = 500; // Limit the problems to 500 to prevent command overload
	    int problemsSize = problems.size();
	    int problemsSizeOutput = problemsSize;
	    String ProblemType = "FormatTableString";
	    if ( (MaxWarnings_int > 0) && (problemsSize > MaxWarnings_int) ) {
	        // Limit the warnings to the maximum
	        problemsSizeOutput = MaxWarnings_int;
	    }
	    if ( problemsSizeOutput < problemsSize ) {
	        message = "Performing string formatting had " + problemsSize + " warnings - only " + problemsSizeOutput + " are listed.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
	        // No recommendation since it is a user-defined check
	        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
	        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
	    }
	    for ( int iprob = 0; iprob < problemsSizeOutput; iprob++ ) {
	        message = problems.get(iprob);
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
	        // No recommendation since it is a user-defined check
	        // FIXME SAM 2009-04-23 Need to enable using the ProblemType in the log.
	        status.addToLog ( CommandPhaseType.RUN,new CommandLogRecord(CommandStatusType.WARNING, ProblemType, message, "" ) );
	    }
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
        throw new CommandWarningException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{   
    if ( parameters == null ) {
        return getCommandName() + "()";
    }
    
    String InputProperties = parameters.getValue( "InputProperties" );
    String Format = parameters.getValue( "Format" );
    String OutputProperty = parameters.getValue( "OutputProperty" );
    String PropertyType = parameters.getValue( "PropertyType" );
        
    StringBuffer b = new StringBuffer ();
    
    if ( (InputProperties != null) && (InputProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InputProperties=\"" + InputProperties + "\"" );
    }
    if ( (Format != null) && (Format.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Format=\"" + Format + "\"" );
    }
    if ( (OutputProperty != null) && (OutputProperty.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputProperty=\"" + OutputProperty + "\"" );
    }
    if ( (PropertyType != null) && (PropertyType.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PropertyType=" + PropertyType );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

}