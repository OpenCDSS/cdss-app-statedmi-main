package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;

import RTi.TS.MonthTS;
import RTi.TS.TSData;
import RTi.TS.TSUtil;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
<p>
This class initializes, checks, and runs the Limit*TSToRights() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the
LimitDiversionDemandTSMonthlyToRights() command extends this class in order to uniquely represent
the command, but much of the functionality is in the base class.
</p>
*/
public abstract class LimitTSToRights_Command extends AbstractCommand implements Command
{
	
/**
Flags for Ignore* and Append parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Input file that is read by this command.
*/
private File __InputFile_File = null;

/**
Free water appropriation date.
*/
private DateTime __FreeWaterAppropriationDate_DateTime = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public LimitTSToRights_Command ()
{	super();
	setCommandName ( "Limit?TSToRights" );
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
{	String routine = getClass().getName() + ".checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String ID = parameters.getValue ( "ID" );
	//String IgnoreID = parameters.getValue ( "IgnoreID" );
	String FreeWaterAppropriationDate = parameters.getValue ( "FreeWaterAppropriationDate" );
	String UseOnOffDate = parameters.getValue ( "UseOnOffDate" );
	String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth" );
	String LimitToCurrent = parameters.getValue ( "LimitToCurrent" );
	String SetFlag = parameters.getValue ( "SetFlag" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    String fileType = "diversion right"; // default
    if ( (this instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
    	(this instanceof LimitWellDemandTSMonthlyToRights_Command) ) {
    	fileType = "diversion right";
    }
    else if((this instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
    	(this instanceof LimitWellDemandTSMonthlyToRights_Command) ||
    	(this instanceof LimitIrrigationPracticeTSToRights_Command)) {
    	fileType = "well right";
    }
	
	if ( (InputFile != null) && (InputFile.length() != 0) ) {
		// Not required but must exist if being read
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
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile));
			File f = new File ( adjustedPath );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The input file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the input directory." ) );
			}
			else if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing " + fileType + " input file (may be OK if created during processing)." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The input file:\n" +
			"    \"" + InputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that " + fileType + " input file and working directory paths are compatible." ) );
		}
	}
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID to process." ) );
	}
	if ( (FreeWaterAppropriationDate != null) && !FreeWaterAppropriationDate.equals("")) {
		try {
			__FreeWaterAppropriationDate_DateTime = DateTime.parse(FreeWaterAppropriationDate);
		}
		catch ( Exception e ) {
            message = "The free water appropriation date \"" + FreeWaterAppropriationDate + "\" is not a valid date.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a date YYYY-MM-DD or MM/DD/YYYY." ) );
		}
	}
	
	if ( (UseOnOffDate != null) && (UseOnOffDate.length() > 0) &&
		!UseOnOffDate.equalsIgnoreCase(_False) && !UseOnOffDate.equalsIgnoreCase(_True) ) {
		message = "The UseOnOffDate value (" + UseOnOffDate + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify UseOnOffDate as " + _False + " (default), or " + _True + ".") );
	}
	
	if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
		if ( (NumberOfDaysInMonth != null) && (NumberOfDaysInMonth.length() > 0) &&
			!StringUtil.isDouble(NumberOfDaysInMonth) ) {
			message = "The NumberOfDaysInMonth value (" + NumberOfDaysInMonth + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the NumberOfDaysInMonth as a number.") );
		}
	}
	
	if ( (this instanceof LimitDiversionDemandTSMonthlyToRights_Command) ||
		(this instanceof LimitWellDemandTSMonthlyToRights_Command) ) {
		if ( (LimitToCurrent != null) && (LimitToCurrent.length() > 0) &&
			!LimitToCurrent.equalsIgnoreCase(_False) && !LimitToCurrent.equalsIgnoreCase(_True) ) {
			message = "The LimitToCurrent value (" + LimitToCurrent + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify LimitToCurrent as " + _False + " (default), or " + _True + ".") );
		}
	}
	
	if ( (SetFlag != null) && (SetFlag.length() > 0) && (SetFlag.length() != 1) ) {
		message = "The SetFlag value (" + SetFlag + ") must be 1 character.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify SetFlag as 1 character.") );
	}
   
	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "InputFile" );
	valid_Vector.add ( "ID" );
	if ( !(this instanceof LimitIrrigationPracticeTSToRights_Command) ) {
		valid_Vector.add ( "IgnoreID" );
	}
	valid_Vector.add ( "FreeWaterAppropriationDate" );
	valid_Vector.add ( "UseOnOffDate" );
	if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
		valid_Vector.add ( "NumberOfDaysInMonth" );
	}
	if ( (this instanceof LimitDiversionDemandTSMonthlyToRights_Command) ||
		(this instanceof LimitWellDemandTSMonthlyToRights_Command) ) {
		valid_Vector.add ( "LimitToCurrent" );
	}
	valid_Vector.add ( "SetFlag" );

    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ), warning );
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
	return (new LimitTSToRights_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getInputFile ()
{
	return __InputFile_File;
}

/**
Run method internal to this class, to handle running in discovery and run mode.
Limit (or set) the time series values to the water rights in effect at the time
of the value.  Diversion historical and demand time series can be limited to the
rights.  Well pumping can be set to the rights.  The process is as follows:
<ol>
<li>	When reading diversion time series from HydroBase or replacement
	files, if a LimitDiversionHistoricalTSMonthlyToRights() command is
	detected, then a backup copy of the time series is saved after the
	read.  (The backup does not occur when using the
	LimitDiversionDemandTSMonthlyToRights() command.)</li>
<li>	The rights file is read - rights do not need to be grouped (all are searched).</li>
<li>	The rights are converted to a time series of decrees as a step function.
	</li>
<li>	If limiting the time series (diversion historical and demand time
	series), the time series values are set to the
	rights if the values are greater than the rights step function.  If
	setting the time series (well maximum pumping time series), the values
	are simply set to the rights.</li>
<li>	For diversion historical and demand time series, if the original data
	are not missing, the values are set back to the
	original (using the backup copy).  This way, the algorithm is relatively
	clean filled values are reset.</li>
</ol>
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommand", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" );
    String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";
	}
	String ID_Java = StringUtil.replaceString(ID,"*",".*");
    String IgnoreID = parameters.getValue ( "IgnoreID" );
	List IgnoreID_Vector = null;
	int IgnoreID_Vector_size = 0;
	if ( IgnoreID != null ) {
		// Parse out the parts...
		IgnoreID_Vector = StringUtil.breakStringList (IgnoreID,", ", 0);
		if ( IgnoreID_Vector != null ) {
			IgnoreID_Vector_size = IgnoreID_Vector.size();
		}
		for ( int i = 0; i < IgnoreID_Vector_size; i++ ) {
			IgnoreID_Vector.set(i,
			StringUtil.replaceString( (String)IgnoreID_Vector.get(i),"*",".*") );
		}
	}
    // FreeWaterAppropriationDate parsed in checkCommandParameters()
    boolean LimitToCurrent_boolean = false; // default
    String LimitToCurrent = parameters.getValue ( "LimitToCurrent" );
    if ( (LimitToCurrent != null) && LimitToCurrent.equalsIgnoreCase(_True) ) {
    	LimitToCurrent_boolean = true;
    }
    boolean UseOnOffDate_boolean = false; // default
    String UseOnOffDate = parameters.getValue ( "UseOnOffDate" );
    if ( (UseOnOffDate != null) && UseOnOffDate.equalsIgnoreCase(_True) ) {
    	UseOnOffDate_boolean = true;
    }
    String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth"); // For IPY
    double NumberOfDaysInMonth_double = -1.0;
    if ( NumberOfDaysInMonth != null ) {
    	NumberOfDaysInMonth_double = Double.parseDouble(NumberOfDaysInMonth);
    }
    String SetFlag = parameters.getValue ( "SetFlag" );
    
    // Get the stations/locations being processed
    List stationList = null;
    String rightType = "";
    String stationType = "";
	boolean do_diversions = false;	// booleans to increase performance
	boolean do_wells = false;
	boolean do_maxpumping = false;
	int stationListSize = 0;
    try {
    	if ( (this instanceof LimitDiversionHistoricalTSMonthlyToRights_Command) ||
    		(this instanceof LimitDiversionDemandTSMonthlyToRights_Command)) {
    		stationList = (List)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		rightType = "diversion";
    		stationType = "diversion";
    		do_diversions = true;
    	}
    	else if ( (this instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
    		(this instanceof LimitWellDemandTSMonthlyToRights_Command)) {
    		stationList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
    		rightType = "well";
    		stationType = "well";
    		do_wells = true;
    	}
       	else if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
    		stationList = (List)processor.getPropContents ( "StateCU_Location_List" );
    		rightType = "well";
    		stationType = "CU location";
    		do_maxpumping = true;
    	}
    	stationListSize = stationList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting location list to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
 
    // Get the time series being processed

    List tsList = null;
	int tsListSize = 0;
	String tsType = null;
    try {
    	if ( this instanceof LimitDiversionHistoricalTSMonthlyToRights_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
    		tsType = "diversion historical time series (monthly)";
    	}
    	else if ( this instanceof LimitDiversionDemandTSMonthlyToRights_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_DiversionDemandTSMonthly_List" );
    		tsType = "diversion demand time series (monthly)";
    	}
    	else if ( this instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
    		tsType = "well historical pumping time series (monthly)";
    	}
    	else if ( this instanceof LimitWellDemandTSMonthlyToRights_Command ) {
    		tsList = (List)processor.getPropContents ( "StateMod_WellDemandTSMonthly_List" );
    		tsType = "well demand time series (monthly)";
    	}
       	else if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
       		tsList = (List)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List" );
       		tsType = "irrigation practice time series";
    	}
    	tsListSize = tsList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + tsType + " to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( tsListSize == 0 ) {
    	message = "No " + tsType + " are available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.WARNING,
                message, "Read " + tsType + " prior to this command." ) );
   }
    
    // If the rights input file is not specified, check that rights are available in memory
    List processorRightList = null;
    int processorRightListSize = 0;
    if ( InputFile == null ) {
        try {
        	if ( do_diversions ) {
        		processorRightList = (List)processor.getPropContents ( "StateMod_DiversionRight_List" );
        	}
        	else if ( do_wells ) {
        		processorRightList = (List)processor.getPropContents ( "StateMod_WellRight_List" );
        	}
        	processorRightListSize = processorRightList.size();
        }
        catch ( Exception e ) {
            Message.printWarning ( log_level, routine, e );
            message = "Error requesting " + rightType + " rights to process (" + e + ").";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report to software support.  See log file for details." ) );
        }
        if ( processorRightListSize == 0 ) {
        	 message = "No " + rightType + " rights are available.";
             Message.printWarning ( warning_level, 
             MessageUtil.formatMessageTag(command_tag, ++warning_count),
             routine, message );
             status.addToLog ( commandPhase,
                 new CommandLogRecord(CommandStatusType.FAILURE,
                     message, "Read " + rightType + " rights prior to this command or specify a rights file for this command." ) );
        }
    }
    
    // Get the flag that indicates if need to copy historical TS monthly...
    boolean need_diversion_ts_monthly_copy = false;
    try {
	    if ( this instanceof LimitDiversionHistoricalTSMonthlyToRights_Command ) {
	    	Boolean b = (Boolean)processor.getPropContents ( "NeedToCopyDiversionHistoricalTSMonthly" );
	    	need_diversion_ts_monthly_copy = b.booleanValue();
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting processor property NeedToCopyDiversionHistoricalTSMonthly (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Get the original time series copy..
    List diversionTSCopyList = null;
    int diversionTSCopyListSize = 0;
    if ( need_diversion_ts_monthly_copy ) {
	    try {
	    	diversionTSCopyList = (List)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly2_List" );
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting diversion historical time series copy from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
    }
    
    // Get the output start and end for use with time series commands
    DateTime OutputStart_DateTime = null;
    DateTime OutputEnd_DateTime = null;
    if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
	    try {
	    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart" );
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting output start from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
	    try {
	    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd" );
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting output end from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
	    if ( (OutputStart_DateTime == null) || (OutputEnd_DateTime == null) ) {
	        message = "Output period has not been specified.";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the output period with SetOutputPeriod() prior to this command." ) );
	    }
    }
    
    String InputFile_full = null;
    if ( InputFile != null ) {
    	InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
	  	if ( !IOUtil.fileExists(InputFile_full) ) {
			message = "Input file \"" + InputFile_full + "\" does not exist.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
		}
	  	else if ( !IOUtil.fileReadable(InputFile_full) ) {
			message = "Input file \"" + InputFile_full + "\" is not readable.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
		}
    }
  	
	// Set the initial date condition, which are used to force zero decree.

	HydroBase_AdministrationNumber admin_num_1800 = null;
	try {
		DateTime date1800 = new DateTime ( DateTime.PRECISION_DAY );
		date1800.setYear ( 1800 );
		date1800.setMonth ( 1 );
		date1800.setDay ( 1 );
		admin_num_1800 = new HydroBase_AdministrationNumber ( date1800 );
	}
	catch ( Exception e ) {
		message = "Unexpected error initializing admininistration number for 1800 (" + e + ")";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog(commandPhase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Contact software support."));
	}

	// Set the properties that are used when the time series are limited to rights...

	PropList enforce_props = new PropList ( "EnforceLimits" );
	enforce_props.set ( "LimitsScaleFactor=DaysInMonth" );
	boolean SetFlag_boolean = false;
	if ( SetFlag != null ) {
		enforce_props.set ( "SetFlag", SetFlag );
		SetFlag_boolean = true;
	}

	// Initialize the free water cutoff...

	double DOUBLE_FW_ADMIN_NUM_CUTOFF = 90000.00000;
	double INT_FW_ADMIN_NUM_CUTOFF = 90000;
	HydroBase_AdministrationNumber FW_ADMIN_NUM_CUTOFF = null;
					// Free water administration number cut-off value for checks.
	int julian_fw_cutoff = 0; // Julian appropriation date corresponding to the cutoff administration number.
	double double_fw_cutoff = 0.0;	// Double free water admin number.
	try {
		FW_ADMIN_NUM_CUTOFF = new HydroBase_AdministrationNumber ( DOUBLE_FW_ADMIN_NUM_CUTOFF );
		julian_fw_cutoff = FW_ADMIN_NUM_CUTOFF.getJulianAppropriationDate();
		double_fw_cutoff = (double)julian_fw_cutoff;
	}
	catch ( Exception e ) {
		message = "Unexpected error initializing free water cutoff data for admininistration number" +
			DOUBLE_FW_ADMIN_NUM_CUTOFF + " (" + e + ")";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog(commandPhase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Contact software support."));
	}

	// Initialize the free water default...

	double DOUBLE_FW_ADMIN_NUM_DEFAULT = 0.00000;
	HydroBase_AdministrationNumber FW_ADMIN_NUM_DEFAULT = null;
					// Default free water administration number when no
					// FreeWaterAppropriationDate and no senior right for a free water right.
	try {
		FW_ADMIN_NUM_DEFAULT = new HydroBase_AdministrationNumber ( DOUBLE_FW_ADMIN_NUM_DEFAULT );
	}
	catch ( Exception e ) {
		message = "Error initializing free water default data for admin. num. " + DOUBLE_FW_ADMIN_NUM_DEFAULT;
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog(commandPhase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Contact software support."));
	}
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
     	// Clear the filename
    	setInputFile ( null );
    	// FIXME SAM 2009-02-06 - sort a local copy of the rights here?
    	Message.printStatus ( 2, routine, "It is assumed that the water rights for a " + rightType +
    		" are grouped in the data file." );
    	
    	// Read the rights file...

    	List rightList = null;
    	if ( InputFile != null ) {
    		InputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
	    	
    		// If the InputFile is not specified, use the rights that are in memory.
    		if ( InputFile_full != null ) {
    			Message.printStatus ( 2, routine, "Rights for limit are being read from \"" + InputFile + "\"" );
    			if ( do_diversions ) {
    				rightList = StateMod_DiversionRight.readStateModFile(InputFile_full );
    			}
    			else if ( do_wells ) {
    	    		rightList = StateMod_WellRight.readStateModFile (InputFile_full );
    			}
    		}
    		else {
    			Message.printStatus ( 2, routine, "Rights for limit are the existing " + rightType + " rights in memory." );
    		}
    	}
    	else {
    		// Use what was read in previously
    		rightList = processorRightList;
    	}
    	int rightListSize = rightList.size();

    	// Make sure that data are available...

    	if ( rightListSize == 0 ) {
    		message = "No " + rightType + " rights have been read to process.";
    		Message.printWarning ( 2, command_tag, routine, message );
    		throw new Exception ( message );
    	}
    	
    	MonthTS pumping_ts = null;
    	if ( do_maxpumping ) {
			// Create a time series that will be used to limit the rights...
			pumping_ts = new MonthTS ();
			DateTime pumping_start_DateTime = new DateTime( DateTime.PRECISION_MONTH);
			pumping_start_DateTime.setYear ( OutputStart_DateTime.getYear());
			pumping_start_DateTime.setMonth( 1 );
			pumping_ts.setDate1 ( pumping_start_DateTime );
			DateTime pumping_end_DateTime = new DateTime( DateTime.PRECISION_MONTH);
			pumping_end_DateTime.setYear ( OutputEnd_DateTime.getYear());
			pumping_end_DateTime.setMonth( 12 );
			pumping_ts.setDate2 ( pumping_end_DateTime );
			pumping_ts.allocateDataSpace ();
    	}

		HydroBase_AdministrationNumber fwadminnum = null;
		if ( __FreeWaterAppropriationDate_DateTime != null ) {
			fwadminnum = new HydroBase_AdministrationNumber (__FreeWaterAppropriationDate_DateTime );
		}
		if ( fwadminnum != null ) {
			Message.printStatus ( 2, routine, "Free water (rights with administration number " +
			FW_ADMIN_NUM_CUTOFF.toString()+ ") appropriation dates will be reset to " + fwadminnum.toString() + " (" +
			fwadminnum.getAppropriationDate().toString() + ")." );
		}
		else {
			Message.printStatus ( 2, routine,
				"FreeWaterAppropriationDate has not been specified.  Free water rights will not be reset." );
		}

		// Put these here to increase performance.  Sizing to 5000 rights for
		// a diversion station should be enough for all cases.  5000 works for
		// wells in the Rio Grande but may need to be changed in the South
		// Platte.  Large values will likely never be exceeded if water rights
		// classes are used.  These arrays are reused for each station.

		// FIXME SAM 2009-02-06 - Evaluate array autosizing
		int RN = 5000; // Dimension for arrays.  Could make so dynamically resize if exceeded later.
		double [] decrees = new double[RN]; // Decrees for rights.
		double [] double_admin_nums = new double[RN]; // Administration numbers as floating point values.
		boolean [] is_fw = new boolean[RN]; // Indicates whether the right was originally a free water right.
		int [] julian_appro_dates = new int[RN]; // Appropriation dates as Julian integers.
		String [] rightids = new String[RN]; // Identifiers for water rights.

		StateMod_Diversion div = null; // Diversion to process
		StateMod_Well well = null; // Well to process
		StateCU_Location culoc = null; // CU Location to process
		MonthTS ts_i; // Reference to the monthly time series to process.
		TSData tsdata; // Single data point from a time series.
		String flag; // Data flag for a data point.
		MonthTS orig_ts; // Reference to the original diversion or well time series, as read from HydroBase or another source.
		StateCU_IrrigationPracticeTS ipy = null; // Irrigation practice time series to use when setting maximum pumping.
		int pos = 0; // Position of time series in array
		String id = null; // Diversion or CU location ID.
		int count = 0; // Count of rights for the time series' structure, including the initial condition right imposed here.
		List appro_dates = null; // Vector of appropriation dates associated with the rights.
		HydroBase_AdministrationNumber admin_num = null;
		double double_admin_num;
		boolean found_senior = false; // Used to track whether there is a non-free-water senior right.
		boolean found_fw = false; // Used to track whether there a free-water right.
		int whole; // Whole number part of the administration number.

		HydroBase_AdministrationNumber senior_admin_num = null; // Use this for the senior water right
						// associated with the time series structure.

		boolean fw_error = false; // Indicates if there is an error processing free water rights.

		int min_julian_appro_date = 99999;

		int j = 0; // Index used below.

		int ir = 0; // Position in the overall rights data.
		StateMod_DiversionRight	rights_i = null;// Reference to right in list
		StateMod_WellRight	wrights_i =null;// Reference to right in list
		boolean	ignore = false; // Indicates if one of the IgnoreID structures was found

		// This is only used with diversion time series...
		if ( need_diversion_ts_monthly_copy && (diversionTSCopyListSize == 0) ) {
			message = "There are no backup copies of diversion historical time " +
			"series (monthly) - reset to original data cannot occur.";
		    Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
	        status.addToLog(commandPhase,
	            new CommandLogRecord( CommandStatusType.FAILURE, message, "Contact software support."));
		}

		// Loop through the diversion stations, well stations, or CU Locations
		// and check the rights for each station...

		int rswitch = 0; // Switch value for right.
		String right_id = null; // Identifier for a water right.
		double decree_sign = 1.0; // Indicates whether decree is additive or subtractive
		String irtem = null; // Administration number from StateMod right object.
		double smdecree	= 0.0; // Decree from StateMod right object.
		double max_pumping = 0.0; // Maximum monthly pumping (within a year).
		int month = 0; // Month for iterator.
		DateTime month_DateTime = new DateTime ( DateTime.PRECISION_MONTH ); // Used for well processing.
		int year, year1, year2; // Used for well processing.
		// TODO SAM 2007-02-18 Evaluate whether cgoto needed.
		// String cgoto = null; // Location for the right.
		int matchCount = 0;
		for ( int i = 0; i < stationListSize; i++ ) {
			if ( do_diversions ) {
				div = (StateMod_Diversion)stationList.get(i);
				id = div.getID();
			}
			else if ( do_wells){
				well = (StateMod_Well)stationList.get(i);
				id = well.getID();
			}
			else if ( do_maxpumping ){
				culoc = (StateCU_Location)stationList.get(i);
				id = culoc.getID();
			}
			if ( !id.matches(ID_Java) ) {
				// Identifier does not match...
				continue;
			}
			// Check for IDs to ignore...
			ignore = false;
			for ( int iignore = 0; iignore < IgnoreID_Vector_size; iignore++ ) {
				if ( id.matches((String)IgnoreID_Vector.get(iignore))){
					// The ID matches one of the ignore structures so ignore processing the station.
					ignore = true;
					break;
				}
			}
			if ( ignore ) {
				continue;
			}

			// Get the time series to be processed...

			ts_i = null;
			orig_ts = null;
			if ( this instanceof LimitDiversionHistoricalTSMonthlyToRights_Command ){
				pos = TSUtil.indexOf( tsList, id,"Location",0);
				if ( pos < 0 ) {
					// No time series is available...
					Message.printStatus ( 2, routine,
					"No " + tsType + " is available for \"" + id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				ts_i = (MonthTS)tsList.get(pos);
				if ( ts_i == null ) {
					// No diversion historical time series is
					// available.  Don't include warning count in
					// tag...
					Message.printStatus ( 2, routine, "No diversion historical TS (monthly) is available for \""+
						id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}

				// Get the historical diversion time series, if available,
				// from the backup - this contains the raw data...

				orig_ts = null;	// Indicate no original time series.
				pos = TSUtil.indexOf( diversionTSCopyList,id,"Location",0);
				if ( pos >= 0 ) {
					orig_ts = (MonthTS)diversionTSCopyList.get(pos);
				}
			}
			else if ( this instanceof LimitDiversionDemandTSMonthlyToRights_Command ){
				pos = TSUtil.indexOf(tsList, id,"Location",0);
				if ( pos < 0 ) {
					// No diversion demand time series is available...
					Message.printStatus ( 2, routine,
					"No diversion demand TS (monthly) is available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				ts_i = (MonthTS)tsList.get(pos);
				if ( ts_i == null ) {
					// No diversion demand time series is available.
					Message.printStatus ( 2, routine,
					"No diversion demand TS (monthly) is available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				// The original time series is not used.
			}
			else if ( this instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command ) {
				pos = TSUtil.indexOf(tsList, id, "Location",0);
				if ( pos < 0 ) {
					// No well historical time series is available...
					Message.printStatus ( 2, routine,
					"No well historical pumping TS (monthly) is available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				ts_i = (MonthTS)tsList.get(pos);
				if ( ts_i == null ) {
					// No well historical time series is available.
					Message.printStatus ( 2, routine, "No well historical pumping TS (monthly) is " +
					"available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}

				// Get the historic well time series, if available, from the backup - this contains the raw data...

				orig_ts = null;	// Indicate no original time series.
				// TODO SAM 2005-10-05 Enable later if needed.
				//pos = TSUtil.indexOf(__SMWellHistoricalPumpingTSMonthly2_Vector,id,"Location",0);
				//if ( pos >= 0 ) {
					//orig_ts = (MonthTS)__SMDiversionTSMonthly2_Vector.get(pos);
				//}
			}
			else if ( this instanceof LimitWellDemandTSMonthlyToRights_Command ){
				pos = TSUtil.indexOf(tsList,id,"Location",0);
				if ( pos < 0 ) {
					// No well demand time series is available...
					Message.printStatus ( 2, routine, "No well demand TS (monthly) is " +
					"available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				ts_i = (MonthTS)tsList.get(pos);
				if ( ts_i == null ) {
					// No well demand time series is available.
					Message.printStatus ( 2, routine, "No well demand TS (monthly) is " +
					"available for \""+ id + "\".  Skipping rights check." );
					// Non fatal?
					continue;
				}
				// The original time series is not used.
			}
			else if ( do_maxpumping ) {
				// Get the irrigation practice time series (contains time series for each data type in the file)...
				pos = StateCU_Util.indexOf(tsList,id);
				if ( pos >= 0 ) {
					// Get the time series...
					ipy = (StateCU_IrrigationPracticeTS)tsList.get(pos);
				}
				if ( (pos < 0) || (ipy == null) ) {
					Message.printStatus ( 2, routine, "Unable to find irrigation practice time series for \""+ id +
					"\".  Skipping rights check." );
					continue;
				}
				// The original time series is not used.
			}
			
			// Time series was matched.
			++matchCount;

			// Initialize the data for each structure...

			count = 0;	// Counter for the rights in the structure
			found_senior = false;
			found_fw = false;
			admin_num = null;
			senior_admin_num = null;
			appro_dates = new Vector ( 10, 5 );

			// Get the rights for the specified time series, using the location identifier to match the rights.

			for ( ir = 0; ir < rightListSize; ir++  ) {
				// Handle each right type...
				if ( do_diversions ) {
					rights_i = (StateMod_DiversionRight)
					rightList.get(ir);
					if ( rights_i == null ) {
						continue;
					}
					if ( !rights_i.getCgoto().equalsIgnoreCase(id)) {
						// Right does not match the structure...
						continue;
					}
					// Else need some information...
   					// TODO SAM 2007-02-18 Evaluate whether cgoto needed.
					// cgoto = rights_i.getCgoto();
					rswitch = rights_i.getSwitch();
					right_id = rights_i.getID();
					irtem = rights_i.getIrtem();
					smdecree = rights_i.getDcrdiv();
				}
				else if ( do_wells ) {
					wrights_i = (StateMod_WellRight)rightList.get(ir);
					if ( wrights_i == null ) {
						continue;
					}
					if ( !wrights_i.getCgoto().equalsIgnoreCase(id)) {
						// Right does not match the structure...
						continue;
					}
					// Else need some information...
   					// TODO SAM 2007-02-18 Evaluate whether cgoto needed.
					//cgoto = wrights_i.getCgoto();
					rswitch = wrights_i.getSwitch();
					right_id = wrights_i.getID();
					irtem = wrights_i.getIrtem();
					smdecree = wrights_i.getDcrdivw();
				}

				decree_sign = 1.0;
				if ( rswitch == 0 ) {
					Message.printStatus ( 2, routine, "Ignoring right \"" + right_id + "\" because OnOff switch = 0." );
					continue;
				}
				else if (((rswitch > 1800) || (rswitch < -1800)) && UseOnOffDate_boolean ) {
					// Assume that a year has been specified.
					DateTime dt = new DateTime ( DateTime.PRECISION_DAY );
					if ( rswitch > 0 ) {
						// Use the decree in an additive way...
						dt.setYear ( rswitch );
						dt.setMonth ( 1 );
						dt.setDay ( 1 );
					}
					else {
						// Subtract the decree...
						decree_sign = -1.0;
						dt.setYear ( rswitch*-1 + 1 );
						dt.setMonth ( 1 );
						dt.setDay ( 1 );
					}
					Message.printStatus ( 2, routine, "Using appropriation date " +
						dt + " for right \"" + right_id + "\" because switch = " + rswitch );
					try {
						admin_num = new HydroBase_AdministrationNumber(dt);
						double_admin_num = (double)admin_num.getWhole();
					}
					catch ( Exception e ) {
						message = "Unexpected error converting " + dt + " to administration number for \"" + id + "\").  Skipping.";
						Message.printWarning ( warning_level,
				            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
				        Message.printWarning ( 3, routine, e );
				        status.addToLog(commandPhase,
				            new CommandLogRecord( CommandStatusType.WARNING, message,
				                "Check on/off value (" + rswitch + ") - contact software support if necessary."));
						continue;
					}
				}
				else {
					// Just operate on the administration number from the water right.
					// TODO SAM - comment from old demandts...
					// Get the number as a floating point number (is there any chance that there will be round-off here?)...
					double_admin_num = StringUtil.atod( irtem );
					// Now convert the administration number to a full data type to get the pieces.  The prior
					// adjudication date will not be available but we can always get the appropriation date.
					try {
						admin_num = new HydroBase_AdministrationNumber ( double_admin_num );
					}
					catch ( Exception e ) {
						message = "Unexpected error converting " + double_admin_num + " to dates (" + id + ").  Skipping.";
						Message.printWarning ( warning_level,
				            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
				        Message.printWarning ( 3, routine, e );
				        status.addToLog(commandPhase,
				            new CommandLogRecord( CommandStatusType.WARNING, message,
				                "Check water right administration number (" + irtem + ") - contact software support if necessary."));
						continue;
					}
					// Handle the sign...
					if ( rswitch < 0 ) {
						decree_sign = -1.0;
					}
				}
				// Get the decree for the right (adjusting for the negative year in the switch if necessary)...
				decrees[count] = smdecree*decree_sign;
				// Get the appropriation date for the right...
				// The appropriation date is stored as a Julian date number of days since datum so we can sort ignoring
				// the effects of the prior adjudication date on the administration number.  If the whole number is
				// greater than the free water cutoff, use the whole number here...
				whole = admin_num.getWhole();
				if ( whole >= INT_FW_ADMIN_NUM_CUTOFF ) {
					julian_appro_dates[count] = whole;
				}
				else {
					// Use the true appropriation date...
					julian_appro_dates[count] = admin_num.getJulianAppropriationDate();
				}
				rightids[count] = right_id;
				// Don't need?...
				double_admin_nums[count] = double_admin_num;
				is_fw[count] = false;
				if ( double_admin_num < double_fw_cutoff ) {
					// Have at least one senior right.  Note that this check only uses the administration
					// number.  We assume that large administration numbers are free water (conversely that small
					// numbers are read admin numbers with dates).
					found_senior = true;
				}
				else {
					// Found a free water right...
					found_fw = true;
					is_fw[count] = true;
				}
				// Also save the appropriation date.  This is used later when we limit the rights because the
				// appropriation date controls the "step-function"...
				appro_dates.add (admin_num.getAppropriationDate() );
				// Increment the counter...
				++count;
			}
			if ( count == 0 ) {
				// Non-fatal error.  Maybe the structure just does not have any rights?  All of them should...
				Message.printStatus ( 2, routine, "Did not find any rights for " + rightType+ " \"" + id + "\"");
				continue;
			}

			// Now determine the most senior water right.  This is used if have a free water right that needs
			// to be reset to have the same appropriation date of the senior right...

			senior_admin_num = null; // Use this for the senior water right associated with the time series structure.

			fw_error = false; // Indicates if there is an error processing free water rights.
			if ( found_fw ) {
				min_julian_appro_date = 99999;
					// Very large number.  The maximum we will ever have is 99999 since it is a 5-digit number.
				for ( int is = 0; is < count; is++ ) {
					if ( julian_appro_dates[is] < min_julian_appro_date ) {
						// Reset the minimum...
						min_julian_appro_date = julian_appro_dates[is];
					}
				}
				// Now create an administration number object for the senior right...
				try {
					senior_admin_num = new HydroBase_AdministrationNumber ( min_julian_appro_date );
				}
				catch ( Exception e ) {
					// This should not generally happen...
					message = "Unexpected error converting minimum appropriation date (julian) " + min_julian_appro_date +
					" to administration number.  Not changing free water rights.";
					Message.printWarning ( warning_level,
			            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
			        Message.printWarning ( 3, routine, e );
			        status.addToLog(commandPhase,
			            new CommandLogRecord( CommandStatusType.WARNING, message,
			                "Check water right administration numbers - contact software support if necessary."));
					fw_error = true;
				}
				if ( !fw_error ) {
					// Now process the free water rights, resetting the administration number to:
					//
					// 1) the most senior right if there are senior rights
					// 2) the FreeWaterAppropriationDate value if there are no senior rights
					//
					for ( int is = 0; is < count; is++ ) {
						if ( julian_appro_dates[is] >= julian_fw_cutoff ) {
							// This is a free water right so need to check for a senior right.
							// It was determined whether there was a senior right above...
							if ( found_senior ) {
								// Have a senior right...
								Message.printStatus ( 2, routine, "For \"" + id + "\", there IS a senior"+
								" right.  Resetting free water right \"" + rightids[is] +
								"\" to use senior appropriation date " + senior_admin_num.getAppropriationDate() );
								// Reset all the necessary values here...
								double_admin_nums[is] = senior_admin_num.getAdminNumber();
								julian_appro_dates[is] = senior_admin_num.getJulianAppropriationDate();
								appro_dates.set( is, senior_admin_num.getAppropriationDate() );
							}
							else {
								// There is no senior right.  Set to the free water value...
								if ( fwadminnum != null ) {
									message = "For \"" + id + "\", there IS NO senior right.  Resetting free water right \"" +
									rightids[is] + "\" to use FreeWaterAppropriationDate appropriation date " +
									fwadminnum.getAppropriationDate();
									Message.printStatus ( 2, routine, message );
									// Reset all the necessary values here...
									double_admin_nums[is] = fwadminnum.getAdminNumber();
									julian_appro_dates[is] = fwadminnum.getJulianAppropriationDate();
									appro_dates.set(is, fwadminnum.getAppropriationDate() );
								}
								else {
									message = "For \"" + id + "\", there IS NO senior right and FreeWaterAppropriationDate" +
									" has not been specified.  Resetting free water right \"" + rightids[is] +
									"\" to use default appropriation date " + FW_ADMIN_NUM_DEFAULT.getAppropriationDate();
									Message.printStatus ( 2, routine, message );
									// Reset all the necessary values here...
									double_admin_nums[is] = FW_ADMIN_NUM_DEFAULT.getAdminNumber();
									julian_appro_dates[is] = FW_ADMIN_NUM_DEFAULT.getJulianAppropriationDate();
									appro_dates.set(is,FW_ADMIN_NUM_DEFAULT.getAppropriationDate() );
								}
							}
						}
					}
				}
			}

			// Always add one date and value at the beginning to denote no structure (no rights are before 1849 so
			// this is a safe bet).  Below this is added at the end and then needs to be sorted later.

			appro_dates.add( admin_num_1800.getAppropriationDate() );
			decrees[count] = 0.0;
			julian_appro_dates[count] = admin_num_1800.getJulianAppropriationDate();
			double_admin_nums[count] = (double)julian_appro_dates[count];
			rightids[count] = "Init. Cond.";
			is_fw[count] = false;
			++count;

			// Now we have the decrees which may contain the results of free water rights resets.
			// Sort the decrees by appropriation date.
			// First copy to a new array only the data that apply to this structure.

			int [] sorted_julian_appro_dates = new int[count];
			double [] sorted_double_admin_nums = new double[count];
			String [] sorted_rightids = new String[count];
			double [] sorted_decrees = new double[count];
			boolean [] sorted_is_fw = new boolean[count];
			List sorted_appro_dates = new Vector(count);
			int [] sort_order = new int[count];

			for ( int is = 0; is < count; is++ ) {
				sorted_julian_appro_dates[is] = julian_appro_dates[is];
			}
			MathUtil.sort ( sorted_julian_appro_dates, MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING, sort_order, true );
			// Now the appropriation dates are sorted so sort the other data.
			// For displays, set the admin number back to the original value...
			for ( int is = 0; is < count; is++ ) {
				sorted_double_admin_nums[is] = double_admin_nums[sort_order[is]];
				sorted_decrees[is] = decrees[sort_order[is]];
				sorted_appro_dates.add ( appro_dates.get(sort_order[is]) );
				sorted_rightids[is] = rightids[sort_order[is]];
				sorted_is_fw[is] = is_fw[sort_order[is]];
			}
			// Now, the decrees are sorted so process to get cumulative values.
			// Leave the initial condition as is and loop through the others...
			double [] enforce_decrees = new double[count];
			// If we have gotten to here, we are guaranteed of at least one right...
			enforce_decrees[0] = sorted_decrees[0];	// Should be zero
			for ( int is = 1; is < count; is++ ) {
				enforce_decrees[is] = enforce_decrees[is - 1] + sorted_decrees[is];
				if ( enforce_decrees[is] < 0.0 ) {
					Message.printStatus ( 2, routine, "After adding right \"" + sorted_rightids[is] +
					"\" Total decree is < 0.0. Resetting to zero.");
					enforce_decrees[is] = 0.0;
				}
			}
			// Now output the rights so users can see what are found (do not output the zero initial condition)...
			Message.printStatus ( 2, routine, "Found " + (count - 1) + " rights for \"" + id +
			"\".  Decree limits after date sort...");
			// Multiply the decrees by 1.9835 to convert from CFS to ACFT.  For the messages,
			// also multiply by the days (this is handled in the enforce code).
			Message.printStatus ( 2, routine, "RightID      FW ApproDate  AdminNumber   CFS      " +
			"Total CFS AF->Jan" +
			"   Feb(28)      Mar       Apr       May       Jun       Jul" +
			"       Aug       Sep       Oct       Nov       Dec" );
			// Print out the rest in a loop...
			String is_fw_string = null;
			for ( j = 0; j < count; j++ ) {
				if ( sorted_is_fw[j] ) {
					is_fw_string = "Y ";
				}
				else {
					is_fw_string = "  ";
				}
				// Allow negatives on appropriation date since the first one will be negative...
				Message.printStatus ( 2, routine, StringUtil.formatString(sorted_rightids[j],"%-12.12s") +
				" " + is_fw_string + " " + ((DateTime)sorted_appro_dates.get(j)).toString() +
				StringUtil.formatString (sorted_double_admin_nums[j],"%12.5f ")+
				StringUtil.formatString ( sorted_decrees[j], "%10.2f")+
				StringUtil.formatString ( enforce_decrees[j], "%10.2f")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*28,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*30,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*30,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*30,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*30,"%9.0f ")+
				StringUtil.formatString(enforce_decrees[j]*1.9835*31,"%9.0f"));
			}

			// Now multiply the decrees by 1.9835 to convert from CFS to AF before enforcing limits...

			for ( j = 0; j < count; j++ ) {
				enforce_decrees[j] *= 1.9835;
			}

			// If only current rights are to be used, remove all but the last constraint...
			if ( LimitToCurrent_boolean ) {
				Message.printStatus ( 2, routine, "Using current conditions.  The last decree total from"+
				" above will be used to limit the time series for the full period." );
				double temp = enforce_decrees[count - 1];
				enforce_decrees = new double[1];
				enforce_decrees[0] = temp;
				// Use an early date...
				sorted_appro_dates.clear();
				sorted_appro_dates.add ( new DateTime(admin_num_1800.getAppropriationDate()) );
			}

			if ( do_diversions || do_wells ) {
				// Now enforce the limits...
				try {
					TSUtil.enforceLimits ( ts_i, sorted_appro_dates, enforce_decrees, enforce_props );
				}
				catch ( Exception e ) {
					message = "Unexpected error enforcing historical decrees for \"" + id +"\".";
					Message.printWarning ( warning_level,
			            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
			        Message.printWarning ( 3, routine, e );
			        status.addToLog(commandPhase,
			            new CommandLogRecord( CommandStatusType.WARNING, message,
			                "Contact software support."));
					continue;
				}
			}
			else if ( do_maxpumping ) {
				// Need to convert monthly limits to an annual time series and then set the IPY max pumping.
				// First use the pumping time series with large values and limit to the rights...
				TSUtil.setConstant ( pumping_ts, 99999999.0 );
				try {
					TSUtil.enforceLimits ( pumping_ts, sorted_appro_dates, enforce_decrees, enforce_props );
				}
				catch ( Exception e ) {
					message = "Unable to enforce historic decrees for \"" + id +"\".";
					Message.printWarning ( warning_level,
			            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
			        Message.printWarning ( 3, routine, e );
			        status.addToLog(commandPhase,
			            new CommandLogRecord( CommandStatusType.WARNING, message,
			                "Contact software support."));
					continue;
				}
				// Now loop through the years in the IPY time series and set each annual value to the maximum
				// monthly pumping for the 12 months in the year...

				year1 = OutputStart_DateTime.getYear();
				year2 = OutputEnd_DateTime.getYear();
				for ( year = year1; year <= year2; year++ ) {
					// Determine the maximum monthly pumping value that will occur in the year.  This rate does
					// not precisely take into account the fact that months have different days...
					month_DateTime.setYear ( year );
					month_DateTime.setMonth ( 1 );
					if ( NumberOfDaysInMonth_double > 0.0 ) {
						// Adjust the monthly values to the given number of days per month...
						max_pumping = pumping_ts.getDataValue ( month_DateTime )*(NumberOfDaysInMonth_double/31.0);
					}
					else {
						// Use values that reflect the specific number of days in months...
						max_pumping = pumping_ts.getDataValue ( month_DateTime );
					}
					for ( month = 2; month <= 12; month++ ) {
						month_DateTime.setMonth ( month );
						if ( NumberOfDaysInMonth_double > 0.0 ){
							// Adjust the monthly values to the given number of days per month...
							max_pumping = MathUtil.max ( max_pumping,
								pumping_ts.getDataValue(month_DateTime)*
								(NumberOfDaysInMonth_double/TimeUtil.numDaysInMonth(month, year) ) );
						}
						else {
							// Use values that reflect the specific number of days in months...
							max_pumping = MathUtil.max (max_pumping, pumping_ts.getDataValue(month_DateTime) );
						}
					}
					// Set the maximum monthly value for the year...
					ipy.setMprate ( year, max_pumping );
				}
			}

			// Now reset observed values in the time series back to the observed.

			if ( do_diversions || do_wells ) {
				if ( orig_ts == null ) {
					if ( do_diversions && need_diversion_ts_monthly_copy ) {
						// Only need to print this if processing diversion time series...
						Message.printStatus ( 2, routine, "No original diversion time series " +
						"for \"" + id+ "\" - not resetting constrained time series." );
					}
				}
				else {
					Message.printStatus ( 2, routine, "Resetting observed values in time series..." );
					DateTime date = new DateTime ( ts_i.getDate1());
					DateTime end = ts_i.getDate2();
					double original_value;
					int interval_base=orig_ts.getDataIntervalBase();
					int interval_mult=orig_ts.getDataIntervalMult();
					for ( ; date.lessThanOrEqualTo(end); date.addInterval(interval_base, interval_mult) ){
						original_value = orig_ts.getDataValue(date);
						if ( !orig_ts.isDataMissing(original_value) ) {
							// Data values was not originally missing so reset in to the original in the filled...
							if ( SetFlag_boolean ) {
								// Restore the original value and also clear the SetFlag, which is
								// only appropriate for filled data...
								tsdata = ts_i.getDataPoint(date, null);
								flag = tsdata.getDataFlag();
								if ( flag.indexOf(SetFlag) >=0 ) {
									// Remove it...
									flag=StringUtil.remove(flag, SetFlag);
									// Reset...
									ts_i.setDataValue( date, original_value, flag, 1 );
								}
								else {
									// Simpler/faster reset
									ts_i.setDataValue ( date, original_value);
								}
							}
							else {
								ts_i.setDataValue (date,original_value);
							}
						}
					}
				}
			}
		}
		// For now always warn if the location was not matched
		if ( matchCount == 0 ) {
	        message = "No time series were matched.";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( commandPhase,
	            new CommandLogRecord(CommandStatusType.WARNING,
	                message, "Verify that " + stationType +
	                " stations are available and that station and time series identifier locations match." ) );
		}
     	// Set the filename
		if ( InputFile_full != null ) {
			setInputFile ( new File(InputFile_full) );
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error limiting time series to rights (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the input file that is read by this command.  This is only used internally.
*/
protected void setInputFile ( File file )
{
	__InputFile_File = file;
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

	String InputFile = parameters.getValue ( "InputFile" );
	String ID = parameters.getValue ( "ID" );
	String IgnoreID = parameters.getValue ( "IgnoreID" );
	String FreeWaterAppropriationDate = parameters.getValue ( "FreeWaterAppropriationDate" );
	String UseOnOffDate = parameters.getValue ( "UseOnOffDate" );
	String NumberOfDaysInMonth = parameters.getValue ( "NumberOfDaysInMonth" );
	String SetFlag = parameters.getValue ( "SetFlag" );
	String LimitToCurrent = parameters.getValue ( "LimitToCurrent" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (ID != null) && (ID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( !(this instanceof LimitIrrigationPracticeTSToRights_Command) ) {
		if ( (IgnoreID != null) && (IgnoreID.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IgnoreID=\"" + IgnoreID + "\"" );
		}
	}
	if ( (FreeWaterAppropriationDate != null) && (FreeWaterAppropriationDate.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FreeWaterAppropriationDate=" + FreeWaterAppropriationDate );
	}
	if ( (UseOnOffDate != null) && (UseOnOffDate.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseOnOffDate=" + UseOnOffDate );
	}
	if ( this instanceof LimitIrrigationPracticeTSToRights_Command ) {
		if ( (NumberOfDaysInMonth != null) && (NumberOfDaysInMonth.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "NumberOfDaysInMonth=" + NumberOfDaysInMonth );
		}
	}
	if ( (this instanceof LimitDiversionDemandTSMonthlyToRights_Command) ||
		(this instanceof LimitWellDemandTSMonthlyToRights_Command) ) {
		if ( (LimitToCurrent != null) && (LimitToCurrent.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "LimitToCurrent=" + LimitToCurrent );
		}
	}
	if ( (SetFlag != null) && (SetFlag.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetFlag=\"" + SetFlag + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}