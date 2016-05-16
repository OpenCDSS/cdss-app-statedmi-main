package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_PenmanMonteith;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DelayTable;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.Time.TimeInterval;
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
<p>
This class initializes, checks, and runs the Write*ToList() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the WriteClimateStationsToList()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class WriteToList_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
List of output files that are created by this command.
*/
private List __OutputFile_List = null;
	
/**
Constructor.
*/
public WriteToList_Command ()
{	super();
	setCommandName ( "Write?ToList" );
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
	String WriteHow = parameters.getValue ( "WriteHow" );
	//String Delimiter = parameters.getValue ( "Delimiter" );
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
    
	if ( (WriteHow != null) && (WriteHow.length() != 0) &&
		!WriteHow.equals(_OverwriteFile) && !WriteHow.equals(_UpdateFile) ) {
        message = "The valie for WriteHow (" + WriteHow + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteHow as " + _OverwriteFile + " (default) or " + _UpdateFile ) );
	}

	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "WriteHow" );
	valid_Vector.add ( "Delimiter" );
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
	return (new WriteToList_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the list of files that were created by this command.
*/
public List getGeneratedFileList ()
{
	List outputFileList = getOutputFileList();
	if ( outputFileList == null ) {
		return new Vector();
	}
	else {
		return outputFileList;
	}
}

/**
Return the list of output files generated by this method.  This method is used internally.
*/
protected List getOutputFileList ()
{
	return __OutputFile_List;
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
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
	}
    
	CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(command_phase);

    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
    }
    String Delimiter = parameters.getValue ( "Delimiter" );
    if ( (Delimiter == null) || Delimiter.equals("") ) {
    	Delimiter = ","; // Default
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
    	setOutputFileList ( null );
    	List outputFileList = null; // If non-null below, then a method returned the list of files.
    	OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );
        Message.printStatus ( 2, routine, "Writing list file \"" + OutputFile_full + "\"" );

        // StateCU components
        
		if ( this instanceof WriteClimateStationsToList_Command ) {
			StateCU_ClimateStation.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCUClimateStationList(), OutputComments_List );
		}
		else if ( this instanceof WriteCropCharacteristicsToList_Command ){
			StateCU_CropCharacteristics.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCUCropCharacteristicsList(), OutputComments_List );
		}
		else if ( this instanceof WriteBlaneyCriddleToList_Command ) {
			StateCU_BlaneyCriddle.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCUBlaneyCriddleList(), OutputComments_List );
			
		}
		else if ( this instanceof WritePenmanMonteithToList_Command ) {
			StateCU_PenmanMonteith.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCUPenmanMonteithList(), OutputComments_List );
			
		}
		/*FIXME SAM 2008-12-24 Is this used?
		else if () {
			// Pass in the StateMod Monthly Delay Table component type because it is used to set field
			// header information and not to do anything substantial with the data, so it's safe to use.
			StateMod_DelayTable.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCUDelayTableMonthlyList(),StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY);
		}
		*/
		else if (this instanceof WriteCULocationsToList_Command) {
			outputFileList = StateCU_Location.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateCULocationList(), OutputComments_List );
		}

		// StateMod components
		
		else if (this instanceof WriteStreamGageStationsToList_Command) {
			StateMod_StreamGage.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModStreamGageStationList(), OutputComments_List );
		}
		else if (this instanceof WriteDelayTablesDailyToList_Command) {
			StateMod_DelayTable.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModDelayTableList(TimeInterval.DAY), OutputComments_List,
				StateMod_DataSet.COMP_DELAY_TABLES_DAILY );
		}
		else if (this instanceof WriteDelayTablesMonthlyToList_Command) {
			StateMod_DelayTable.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModDelayTableList(TimeInterval.MONTH), OutputComments_List,
				StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY);
		}
		else if (this instanceof WriteDiversionStationsToList_Command) {
			outputFileList = StateMod_Diversion.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModDiversionStationList(), OutputComments_List );
		}
		else if (this instanceof WriteDiversionRightsToList_Command) {
			StateMod_DiversionRight.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModDiversionRightList(), OutputComments_List );
		}
		else if (this instanceof WriteReservoirStationsToList_Command) {
			outputFileList = StateMod_Reservoir.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModReservoirStationList(), OutputComments_List );
		}
		else if (this instanceof WriteReservoirRightsToList_Command) {
			StateMod_ReservoirRight.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModReservoirRightList(), OutputComments_List );
		}
		else if (this instanceof WriteInstreamFlowStationsToList_Command) {
			StateMod_InstreamFlow.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModInstreamFlowStationList(), OutputComments_List );
		}
		else if (this instanceof WriteInstreamFlowRightsToList_Command) {
			StateMod_InstreamFlowRight.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModInstreamFlowRightList(), OutputComments_List );
		}
		else if (this instanceof WriteWellStationsToList_Command) {
			outputFileList = StateMod_Well.writeListFile(OutputFile_full, Delimiter, update,
					processor.getStateModWellStationList(), OutputComments_List );
		}
		else if (this instanceof WriteWellRightsToList_Command) {
			StateMod_WellRight.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModWellRightList(), OutputComments_List );
		}
		else if (this instanceof WriteStreamEstimateStationsToList_Command) {
			StateMod_StreamEstimate.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModStreamEstimateStationList(), OutputComments_List );
		}
		else if (this instanceof WriteStreamEstimateCoefficientsToList_Command) {
			StateMod_StreamEstimate_Coefficients.writeListFile( OutputFile_full, Delimiter, update,
				processor.getStateModStreamEstimateCoefficientsList(), OutputComments_List );
		}
		else if (this instanceof WriteNetworkToList_Command) {
			// TODO (JTS - 2005-04-14) where is this data in the processor?
			// this will need to write separate files for each node type
		}
		else if (this instanceof WriteRiverNetworkToList_Command) {
			StateMod_RiverNetworkNode.writeListFile ( OutputFile_full, Delimiter, update,
				processor.getStateModRiverNetworkNodeList(), OutputComments_List );
		}
		else if (this instanceof WriteRiverNetworkToList_Command) {
			StateMod_RiverNetworkNode.writeListFile(OutputFile_full, Delimiter, update,
				processor.getStateModRiverNetworkNodeList(), OutputComments_List );
		}
		else {
			// Command not supported - usually developer forgot to add when new component was added
			/*
	        message = "Ability to write list file has not been implemented for command " + getClass().getName();
	        Message.printWarning ( warning_level, 
	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Contact software support." ) );
	                */
		}

    	// Set the filename(s) for the FileGenerator interface
    	if ( outputFileList == null ) {
    		// Create a list with a single file
    		outputFileList = new Vector(1);
    		outputFileList.add ( new File(OutputFile_full) );
    	} // Otherwise the write method returned the list of filenames for output files
    	setOutputFileList ( outputFileList );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
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
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFileList ( List outputFileList )
{
	__OutputFile_List = outputFileList;
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
	String WriteHow = parameters.getValue ( "WriteHow" );
	String Delimiter = parameters.getValue ( "Delimiter" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (WriteHow != null) && (WriteHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteHow=" + WriteHow );
	}
	if ( (Delimiter != null) && (Delimiter.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Delimiter=" + Delimiter );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}