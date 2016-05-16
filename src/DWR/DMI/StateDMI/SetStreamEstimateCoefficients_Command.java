package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_StreamEstimate_Coefficients;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the SetStreamEstimateCoefficients() command.  There is no
corresponding fill command; however, the code is written to allow this if necessary.
</p>
*/
public class SetStreamEstimateCoefficients_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Base data coefficients.
*/
private double [] __BaseData_coef = null;
/**
Base data identifiers.
*/
private String [] __BaseData_ID = null;

/**
Gain data coefficients.
*/
private double [] __GainData_coef = null;
/**
Gain data identifiers.
*/
private String [] __GainData_ID = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetStreamEstimateCoefficients_Command ()
{	super();
	setCommandName ( "SetStreamEstimateCoefficients" );
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
{	//String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String ProrationFactor = parameters.getValue ( "ProrationFactor" );
	String BaseData = parameters.getValue ( "BaseData" );
	String GainData = parameters.getValue ( "GainData" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID to process." ) );
	}
	
	if ( (ProrationFactor != null) && (ProrationFactor.length() > 0) &&
		!StringUtil.isDouble(ProrationFactor)) {
        message = "The proration factor (" + ProrationFactor + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the proration factor as a number" ) );
	}
	
	if ( (BaseData != null) && (BaseData.length() > 0) ) {
		List v = StringUtil.breakStringList ( BaseData, ",", 0 );
		if ( (v == null) || (v.size()%2 != 0) ) {
			message = "The base data is not specified in pairs.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specified the base data as Coeff,ID,Coeff,ID,... pairs" ) );
		}
		else {
			// Parse the data lists for use when running and check the coefficients.
			int size = v.size();
			__BaseData_ID = new String[size/2];
			__BaseData_coef = new double[size/2];
			String coef, id;
			for ( int i = 0; i < size; i += 2 ) {
				coef = ((String)v.get(i)).trim();
				id = ((String)v.get(i + 1)).trim();
				if ( !StringUtil.isDouble(coef) ) {
					message = "The base flow coefficient (" + coef + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specified the base data as Coeff,ID,Coeff,ID,... pairs" ) );
				}
				else {
					// Save the pair
					__BaseData_ID[i/2] = id;
					__BaseData_coef[i/2] = Double.parseDouble(coef);
				}
			}
		}
	}
	if ( (GainData != null) && (GainData.length() > 0) ) {
		List v = StringUtil.breakStringList ( GainData, ",", 0 );
		if ( (v == null) || (v.size()%2 != 0) ) {
			message = "The gain data is not specified in pairs.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specified the gain data as Coeff,ID,Coeff,ID,... pairs" ) );
		}
		else {
			// Parse the data lists for use when running and check the coefficients.
			int size = v.size();
			__GainData_ID = new String[size/2];
			__GainData_coef = new double[size/2];
			String coef, id;
			for ( int i = 0; i < size; i += 2 ) {
				coef = ((String)v.get(i)).trim();
				id = ((String)v.get(i + 1)).trim();
				if ( !StringUtil.isDouble(coef) ) {
					message = "The gain flow coefficient (" + coef + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specified the gain data as Coeff,ID,Coeff,ID,... pairs" ) );
				}
				else {
					// Save the pair
					__GainData_ID[i/2] = id;
					__GainData_coef[i/2] = Double.parseDouble(coef);
				}
			}
		}
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Add) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
						", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "ProrationFactor" );
	valid_Vector.add ( "BaseData" );
	valid_Vector.add ( "GainData" );
	valid_Vector.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
			warning );
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
{	// The command will be modified if changed...
	return (new SetStreamEstimateCoefficients_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String ProrationFactor = parameters.getValue ( "ProrationFactor" );
	String BaseData = parameters.getValue ( "BaseData" );
	String GainData = parameters.getValue ( "GainData" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List streamEstimateCoefficientsList = null;
    int streamEstimateCoefficientsListSize = 0;
    try {
   		streamEstimateCoefficientsList = (List)processor.getPropContents (
   			"StateMod_StreamEstimateCoefficients_List" );
   		streamEstimateCoefficientsListSize = streamEstimateCoefficientsList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting stream estimate coefficients data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	boolean fill_ProrationFactor = false;
    	double ProrationFactor_double = 0.0;
    	if ( ProrationFactor != null ) {
    		fill_ProrationFactor = true;
    		ProrationFactor_double = StringUtil.atod(ProrationFactor);
    	}

    	boolean fill_BaseData = false;
    	if ( BaseData != null ) {
    		fill_BaseData = true;
    	}
    	
       	boolean fill_GainData = false;
    	if ( GainData != null ) {
    		fill_GainData = true;
    	}
    	
    	// Use logic as per code pulled out of StateDMI_Processor
    	
    	StateMod_StreamEstimate_Coefficients rib = null;
    	String id;
    	int matchCount = 0;
    	for (int i = 0; i < streamEstimateCoefficientsListSize; i++) {
    		rib = (StateMod_StreamEstimate_Coefficients)streamEstimateCoefficientsList.get(i);
    		id = rib.getID();
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Have a match so reset the data...
    		if ( fill_ProrationFactor ) {
    			Message.printStatus ( 2, routine, "Setting " + id + " ProrationFactor -> " +
    				ProrationFactor );
    			rib.setProratnf ( ProrationFactor_double );
    		}
    		if ( fill_BaseData ) {
    			rib.setM ( 0 );	// Clear out first.
    			for ( int j = 0; j < __BaseData_coef.length; j++ ) {
    				Message.printStatus ( 2, routine, "Setting " + id + " FlowB(" + (j + 1) + " " +
    					__BaseData_ID[j] + ") -> " + __BaseData_coef[j] );
    				rib.setCoefn ( j, __BaseData_coef[j] );
    				rib.setUpper ( j, __BaseData_ID[j] );
    			}
    		}
    		if ( fill_GainData ) {
    			rib.setN ( 0 );	// Clear out first.
    			for ( int j = 0; j < __GainData_coef.length; j++ ) {
    				Message.printStatus ( 2, routine, "Setting " + id + " FlowG(" + (j + 1) + " " +
    					__GainData_ID[j] + ") -> " + __GainData_coef[j] );
    				rib.setFlowm ( j, __GainData_ID[j] );
    				rib.setCoefm ( j, __GainData_coef[j] );
    			}
    		}
    	}

    	// If nothing was matched and the idpattern does not contain a
    	// wildcard, add stream estimate coefficients at the end...

    	if ( matchCount == 0 ) {
    		if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
    			// Do nothing
    		}
    		else if ( (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add") ) {
    			rib = new StateMod_StreamEstimate_Coefficients();
    			id = ID;
    			rib.setID ( id );
    			rib.setFlowX ( id );
    			streamEstimateCoefficientsList.add ( rib);
    			Message.printStatus ( 2, routine, "Adding stream estimate coefficients " + id );
    			if ( fill_ProrationFactor ) {
    				Message.printStatus ( 2, routine, "Setting " + id + " ProrationFactor -> " +
    					ProrationFactor );
    					rib.setProratnf ( ProrationFactor_double );
    			}
    			if ( fill_BaseData ) {
    				rib.setN ( 0 );	// Clear out first.
    				rib.setN ( __BaseData_coef.length ); // Now set to new size.
    				for (int j = 0; j < __BaseData_coef.length;j++){
    					Message.printStatus ( 2, routine, "Setting " + id + " FlowB(" + (j + 1) +
    						" " + __BaseData_ID[j] + ") -> " + __BaseData_coef[j] );
    					rib.setCoefn ( j, __BaseData_coef[j] );
    					rib.setUpper ( j, __BaseData_ID[j] );
    				}
    			}
    			if ( fill_GainData ) {
    				rib.setM ( 0 );	// Clear out first.
    				rib.setM( __GainData_coef.length ); // Now set to new size
    				for (int j = 0; j < __GainData_coef.length;j++){
    					Message.printStatus ( 2, routine, "Setting " + id + " FlowG(" + (j + 1) +
    						" " + __GainData_ID[j] + ") -> " + __GainData_coef[j] );
    					rib.setFlowm ( j, __GainData_ID[j] );
    					rib.setCoefm ( j, __GainData_coef[j] );
    				}
    			}
    		}
    		else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Stream estimate coefficients station \"" + ID + "\" was not matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
    		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Stream estimate coefficients station \"" + ID + "\" was not matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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

	String ID = parameters.getValue ( "ID" );
	String ProrationFactor = parameters.getValue ( "ProrationFactor" );
	String BaseData = parameters.getValue ( "BaseData" );
	String GainData = parameters.getValue ( "GainData" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (ProrationFactor != null) && (ProrationFactor.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProrationFactor=" + ProrationFactor );
	}
	if ( (BaseData != null) && (BaseData.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "BaseData=\"" + BaseData + "\"" );
	}
	if ( (GainData != null) && (GainData.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GainData=\"" + GainData + "\"" );
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}