package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_TS;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.YearType;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the WriteTS*ToStateMod() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the
WriteDiversionHistoricalTSMonthlyToStateMod()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
*/
public abstract class WriteTSToStateMod_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
Output start.
*/
private DateTime __OutputStart_DateTime = null;

/**
Output end.
*/
private DateTime __OutputEnd_DateTime = null;

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public WriteTSToStateMod_Command ()
{	super();
	setCommandName ( "Write?TS?ToStateMod" );
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
{	String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String OutputYearType = parameters.getValue ( "OutputYearType" );
	String Precision = parameters.getValue ( "Precision" );
	String MissingValue = parameters.getValue ( "MissingValue" );
	String WriteHow = parameters.getValue ( "WriteHow" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		message = "The output file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an output file." ) );
	}
	else {
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
						message, "Software error - report to support." ) );
			}
	
		try {
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
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
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}
	
	if ( (OutputStart != null) && !OutputStart.equals("")) {
		try {
			__OutputStart_DateTime = DateTime.parse(OutputStart);
		}
		catch (Exception e) {
			message = "Output start date/time \"" + OutputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid output start date/time." ) );
		}
	}
	if ( (OutputEnd != null) && !OutputEnd.equals("")) {
		try {
			__OutputEnd_DateTime = DateTime.parse(OutputEnd);
		}
		catch (Exception e) {
			message = "Output end date/time \"" + OutputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid output end date/time." ) );
		}
	}
	
	if ( (OutputYearType != null) && !OutputYearType.equals("") &&
		!OutputYearType.equalsIgnoreCase(""+YearType.CALENDAR) &
		!OutputYearType.equalsIgnoreCase(""+YearType.WATER) &&
		!OutputYearType.equalsIgnoreCase(""+YearType.NOV_TO_OCT)) {
		message = "The OutputYearType parameter \"" + OutputYearType + "\" must be " + YearType.CALENDAR +
		" (default), " + YearType.WATER + ", or " + YearType.NOV_TO_OCT + ".";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the parameter as " + YearType.CALENDAR + ", " + YearType.WATER + ", or " +
				YearType.NOV_TO_OCT + "."));
	}
	
	if ( (Precision != null) && (Precision.length() > 0) && !StringUtil.isInteger(Precision) ) {
		message = "The precision (" + Precision + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the precision as an integer.") );
	}
	
	if ( (MissingValue != null) && (MissingValue.length() > 0) && !StringUtil.isDouble(MissingValue) ) {
		message = "The missing value indicator (" + MissingValue + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the missing value indicator as a double.") );
	}
	
	if ( (WriteHow != null) && (WriteHow.length() != 0) &&
		!WriteHow.equals(_OverwriteFile) && !WriteHow.equals(_UpdateFile) ) {
        message = "The value for WriteHow (" + WriteHow + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteHow as " + _OverwriteFile + " (default) or " + _UpdateFile ) );
	}

	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "OutputStart" );
	valid_Vector.add ( "OutputEnd" );
	valid_Vector.add ( "OutputYearType" );
	valid_Vector.add ( "Precision" );
	valid_Vector.add ( "MissingValue" );
	valid_Vector.add ( "WriteHow" );
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
	return (new WriteTSToStateMod_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the list of files that were created by this command.
*/
public List getGeneratedFileList ()
{
	List list = new Vector();
	if ( getOutputFile() != null ) {
		list.add ( getOutputFile() );
	}
	return list;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getOutputFile ()
{
	return __OutputFile_File;
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
    
	// Check whether the processor wants output files to be created...

    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	if ( !StateDMICommandProcessorUtil.getCreateOutput(processor) ) {
		Message.printStatus ( 2, routine,
			"Skipping \"" + toString() + "\" because output is not being created." );
		return;
	}
    
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    
    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
    String OutputYearType = parameters.getValue ( "OutputYearType" );
    YearType outputYearType = null;
    if ( (OutputYearType != null) && !OutputYearType.equals("") ) {
    	outputYearType = YearType.valueOfIgnoreCase(OutputYearType);
    }
    String Precision = parameters.getValue ( "Precision");
    int Precision_int = 0; // Default for most except below
    if ( (this instanceof WriteDiversionDemandTSDailyToStateMod_Command) ||
    	(this instanceof WriteInstreamFlowDemandTSAverageMonthlyToStateMod_Command) ) {
    	Precision_int = -2;
    }
    if ( Precision != null ) {
    	Precision_int = Integer.parseInt(Precision);
    }
    String MissingValue = parameters.getValue ( "MissingValue");
    double MissingValue_double = -999; // Default
    if ( MissingValue != null ) {
    	MissingValue_double = Double.parseDouble(MissingValue);
    }
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
    }
    
   // Output period will be used if not specified with SetStart and SetEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    DateTime OutputEnd_DateTime = null;
    try {
    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputEnd (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	if ( __OutputStart_DateTime != null ) {
		OutputStart_DateTime = __OutputStart_DateTime;
	}
	if ( __OutputEnd_DateTime != null ) {
		OutputEnd_DateTime = __OutputEnd_DateTime;
	}
	
	// Output year type should be set
	
	if ( OutputYearType == null ) {
		// Try getting the global value
		try {
			outputYearType = (YearType)processor.getPropContents("OutputYearType");
		}
		catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting OutputYearType (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
		}
	}

    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    String OutputFile_full = OutputFile;
    try {
        // Get the comments to add to the top of the file.

        List OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
            if ( o != null ) {
                OutputComments_List = (List)o;
            }
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
            Message.printWarning(3, routine, message );
            Message.printWarning(3, routine, e );
        }
        
    	// Clear the filename for the FileGenerator interface
    	setOutputFile ( null );
    	OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );
    	String OutputFile_prevFull = null;
    	// Set previous filename to indicate whether update should occur.
    	if ( update ) {
    		OutputFile_prevFull = OutputFile_full;
    	}
    	else {
    		OutputFile_prevFull = null;
    	}
    	if ( this instanceof WriteDiversionHistoricalTSMonthlyToStateMod_Command ) {
			// Check the data (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY );
			OutputComments_List.add( 0, "" );
			OutputComments_List.add( 1, "StateMod diversion historical time series (monthly)." );
			OutputComments_List.add( 2, "" );
	        Message.printStatus ( 2, routine, "Writing StateMod diversion historical time series (monthly) file \"" +
	        	OutputFile_full + "\"" );
	        if ( Precision == null ) {
	        	Precision_int = 0; // Default for diversions
	        }
			StateMod_TS.writeTimeSeriesList ( OutputFile_prevFull, OutputFile_full,
				OutputComments_List, // Comments
				(List)processor.getPropContents("StateMod_DiversionHistoricalTSMonthly_List"),
				OutputStart_DateTime, OutputEnd_DateTime,
				outputYearType, MissingValue_double, Precision_int );
		}
    	else if ( this instanceof WriteDiversionDemandTSMonthlyToStateMod_Command ) {
			// Check the data (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DEMAND_TS_MONTHLY );
			OutputComments_List.add( 0, "" );
			OutputComments_List.add( 1, "StateMod diversion demand time series (monthly)." );
			OutputComments_List.add( 2, "" );
	        Message.printStatus ( 2, routine, "Writing StateMod diversion demand time series (monthly) file \"" +
	        	OutputFile_full + "\"" );
	        if ( Precision == null ) {
	        	Precision_int = 0; // Default for demands
	        }
			StateMod_TS.writeTimeSeriesList ( OutputFile_prevFull, OutputFile_full,
				OutputComments_List, // Comments
				(List)processor.getPropContents("StateMod_DiversionDemandTSMonthly_List"),
				OutputStart_DateTime, OutputEnd_DateTime,
				outputYearType, MissingValue_double, Precision_int );
		}
    	else if ( this instanceof WriteInstreamFlowDemandTSAverageMonthlyToStateMod_Command ) {
			// Check the data (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_INSTREAM_DEMAND_TS_AVERAGE_MONTHLY );
			OutputComments_List.add( 0, "" );
			OutputComments_List.add( 1, "StateMod instream flow demand time series (average monthly) - year 0 applies to entire period." );
			OutputComments_List.add( 2, "" );
	        Message.printStatus ( 2, routine, "Writing StateMod flow instream demand time series (average monthly) file \"" +
	        	OutputFile_full + "\"" );
			StateMod_TS.writeTimeSeriesList ( OutputFile_prevFull, OutputFile_full,
				OutputComments_List, // Comments
				(List)processor.getPropContents("StateMod_InstreamFlowDemandTSAverageMonthly_List"),
				OutputStart_DateTime, OutputEnd_DateTime,
				outputYearType, MissingValue_double, Precision_int );
		}
    	else if ( this instanceof WriteWellDemandTSMonthlyToStateMod_Command ) {
			// Check the data (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY );
			OutputComments_List.add( 0, "" );
			OutputComments_List.add( 1, "StateMod well demand time series (monthly)." );
			OutputComments_List.add( 2, "" );
	        Message.printStatus ( 2, routine, "Writing StateMod well demand time series (monthly) file \"" +
	        	OutputFile_full + "\"" );
	        if ( Precision == null ) {
	        	Precision_int = 0; // Default for demands
	        }
			StateMod_TS.writeTimeSeriesList ( OutputFile_prevFull, OutputFile_full,
				OutputComments_List, // Comments
				(List)processor.getPropContents("StateMod_WellDemandTSMonthly_List"),
				OutputStart_DateTime, OutputEnd_DateTime,
				outputYearType, MissingValue_double, Precision_int );
		}
    	else if ( this instanceof WriteWellHistoricalPumpingTSMonthlyToStateMod_Command ) {
			// Check the data (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY );
			OutputComments_List.add( 0, "" );
			OutputComments_List.add( 1, "StateMod well historical pumping time series (monthly)." );
			OutputComments_List.add( 2, "" );
	        Message.printStatus ( 2, routine, "Writing StateMod well historical pumping time series (monthly) file \"" +
	        	OutputFile_full + "\"" );
	        if ( Precision == null ) {
	        	Precision_int = 0; // Default for pumping
	        }
			StateMod_TS.writeTimeSeriesList ( OutputFile_prevFull, OutputFile_full,
				OutputComments_List, // Comments
				(List)processor.getPropContents("StateMod_WellHistoricalPumpingTSMonthly_List"),
				OutputStart_DateTime, OutputEnd_DateTime,
				outputYearType, MissingValue_double, Precision_int );
		}
    	else {
    		throw new RuntimeException ( "Don't know how to process " + this );
    	}

    	// Set the filename for the FileGenerator interface
    	setOutputFile ( new File(OutputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFile ( File file )
{
	__OutputFile_File = file;
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

	String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String OutputYearType = parameters.getValue("OutputYearType");
	String Precision = parameters.getValue ( "Precision" );
	String MissingValue = parameters.getValue ( "MissingValue" );
	String WriteHow = parameters.getValue ( "WriteHow" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputStart=\"" + OutputStart + "\"");
	}
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputEnd=\"" + OutputEnd + "\"");
	}
	if ( (OutputYearType != null) && (OutputYearType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputYearType=" + OutputYearType );
	}
	if ( (Precision != null) && (Precision.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Precision=" + Precision );
	}
	if ( (MissingValue != null) && (MissingValue.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MissingValue=" + MissingValue );
	}
	if ( (WriteHow != null) && (WriteHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteHow=" + WriteHow );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}