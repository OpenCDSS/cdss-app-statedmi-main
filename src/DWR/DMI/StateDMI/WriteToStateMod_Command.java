package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

//import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DelayTable;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_OperationalRight;
import DWR.StateMod.StateMod_Plan;
import DWR.StateMod.StateMod_Plan_WellAugmentation;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_ReturnFlow;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
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
This class initializes, checks, and runs the Write*ToStateMod() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the WriteWellStationsToStateMod()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class WriteToStateMod_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
Values for WriteDataComments and other boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Flags for Interval parameter, used with delay table data.
*/
protected final String _Day = "Day";
protected final String _Month = "Month";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public WriteToStateMod_Command ()
{	super();
	setCommandName ( "Write?ToStateMod" );
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
	//String Version = parameters.getValue ( "Version" );  // Optional for some commands - currently string
	String WriteDataComments = parameters.getValue ( "WriteDataComments" );
	String Precision = parameters.getValue ( "Precision" );
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
	
	if ( (this instanceof WriteDelayTablesMonthlyToStateMod_Command) ||
		this instanceof WriteDelayTablesDailyToStateMod_Command) {
		if ( (Precision != null) && (Precision.length() > 0) &&
			!StringUtil.isInteger(Precision) ) {
			message = "The precision (" + Precision + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the precision as an integer.") );
		}
	}
	
	if ( this instanceof WriteWellRightsToStateMod_Command ) {
		if ( (WriteDataComments != null) && (WriteDataComments.length() != 0) &&
			!WriteDataComments.equals(_False) && !WriteDataComments.equals(_True) ) {
	        message = "The value for WriteDataComments (" + WriteDataComments + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify WriteDataComments as " + _False + " (default) or " + _True ) );
		}
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
	if ( (this instanceof WriteDelayTablesMonthlyToStateMod_Command) ||
		(this instanceof WriteDelayTablesDailyToStateMod_Command) ){
		valid_Vector.add ( "Precision" );
	}
	if ( this instanceof WriteWellRightsToStateMod_Command ) {
		valid_Vector.add ( "WriteDataComments" );
	}
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
	return (new WriteToStateMod_JDialog ( parent, this )).ok();
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
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    
    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
    //String Version = parameters.getValue ( "Version" );
    String Precision = parameters.getValue ( "Precision"); // For delay tables
    int Precision_int = 2; // Default
    if ( Precision != null ) {
    	Precision_int = Integer.parseInt(Precision);
    }
    String WriteDataComments = parameters.getValue ( "WriteDataComments" );
    if ( (WriteDataComments == null) || WriteDataComments.equals("") ) {
    	WriteDataComments = _False;	// Default
    }
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
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
    	setOutputFile ( new File(OutputFile_full) );
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
		if ( this instanceof WriteStreamGageStationsToStateMod_Command ) {
			// Check the stations (will be output later in check file)...
			// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_STREAMGAGE_STATIONS );
	        Message.printStatus ( 2, routine, "Writing StateMod stream gage stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_StreamGage.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_StreamGageStation_List"),
				OutputComments_List, true );
		}
		else if ( this instanceof WriteDelayTablesMonthlyToStateMod_Command ) {
			// The -1 below is the "interv" variable in the StateMod control file.  In this case
			// the default is to write delay tables with the ID, Nret, RetValues...
			// Check the stations (will be output later in check file)...
			// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY );
	        Message.printStatus ( 2, routine, "Writing StateMod delay tables (monthly) file \"" +
		       	OutputFile_full + "\"" );
			StateMod_DelayTable.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_DelayTableMonthly_List"),
				OutputComments_List, -1, Precision_int );
		}
		else if ( this instanceof WriteDelayTablesDailyToStateMod_Command ) {
			// The -1 below is the "interv" variable in the StateMod control file.  In this case
			// the default is to write delay tables with the ID, Nret, RetValues...
			// Check the stations (will be output later in check file)...
			// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DELAY_TABLES_DAILY );
			Message.printStatus ( 2, routine, "Writing StateMod delay tables (daily) file \"" +
			   	OutputFile_full + "\"" );
				StateMod_DelayTable.writeStateModFile(OutputFile_prevFull, OutputFile_full,
					(List)processor.getPropContents("StateMod_DelayTableDaily_List"),
					OutputComments_List, -1, Precision_int );
		}
    	else if ( this instanceof WriteDiversionStationsToStateMod_Command ) {
			// Check the diversion stations (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DIVERSION_STATIONS );
	        Message.printStatus ( 2, routine, "Writing StateMod diversion stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_Diversion.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_DiversionStation_List"), OutputComments_List );
		}
    	else if ( this instanceof WriteDiversionRightsToStateMod_Command ) {
			// Check the diversion rights (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_DIVERSION_RIGHTS );
	        Message.printStatus ( 2, routine, "Writing StateMod diversion rights file \"" +
	        	OutputFile_full + "\"" );
			StateMod_DiversionRight.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_DiversionRight_List"), OutputComments_List );
		}
    	else if ( this instanceof WriteReservoirStationsToStateMod_Command ) {
			// Check the reservoir stations (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_RESERVOIR_STATIONS );
	        Message.printStatus ( 2, routine, "Writing StateMod reservoir stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_Reservoir.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_ReservoirStation_List"), OutputComments_List );
		}
    	else if ( this instanceof WriteReservoirRightsToStateMod_Command ) {
			// Check the reservoir rights (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_RESERVOIR_RIGHTS );
	        Message.printStatus ( 2, routine, "Writing StateMod reservoir rights file \"" +
	        	OutputFile_full + "\"" );
			StateMod_ReservoirRight.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_ReservoirRight_List"), OutputComments_List );
		}
		else if ( this instanceof WriteReservoirReturnToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod reservoir return flow file \"" +
	        	OutputFile_full + "\"" );
			StateMod_ReturnFlow.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				"Reservoir", (List)processor.getPropContents("StateMod_ReservoirReturn_List"),
				OutputComments_List );
		}
    	else if ( this instanceof WriteInstreamFlowStationsToStateMod_Command ) {
			// Check the well stations (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_INSTREAM_STATIONS );
	        Message.printStatus ( 2, routine, "Writing StateMod instream flow stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_InstreamFlow.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_InstreamFlowStation_List"), OutputComments_List );
		}
    	else if ( this instanceof WriteInstreamFlowRightsToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod instream flow rights file \"" +
	        	OutputFile_full + "\"" );
			StateMod_InstreamFlowRight.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_InstreamFlowRight_List"), OutputComments_List );
		}
		else if ( this instanceof WritePlanStationsToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod plan stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_Plan.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_PlanStation_List"),
				OutputComments_List );
		}
		else if ( this instanceof WritePlanWellAugmentationToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod well augmentation plan data file \"" +
	        	OutputFile_full + "\"" );
			StateMod_Plan_WellAugmentation.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_PlanWellAugmentation_List"),
				OutputComments_List );
		}
		else if ( this instanceof WritePlanReturnToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod plan return flow file \"" +
	        	OutputFile_full + "\"" );
			StateMod_ReturnFlow.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				"Plan", (List)processor.getPropContents("StateMod_PlanReturn_List"),
				OutputComments_List );
		}
		else if ( this instanceof WriteStreamEstimateStationsToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod stream estimate stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_StreamEstimate.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_StreamEstimateStation_List"),
				OutputComments_List, true );
		}
		else if ( this instanceof WriteStreamEstimateCoefficientsToStateMod_Command ) {
			// Check the stations (will be output later in check file)...
			// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS );
	        Message.printStatus ( 2, routine, "Writing StateMod stream estimate coefficients file \"" +
	        	OutputFile_full + "\"" );
			StateMod_StreamEstimate_Coefficients.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_StreamEstimateCoefficients_List"),
				OutputComments_List );
		}
    	else if ( this instanceof WriteWellStationsToStateMod_Command ) {
			// Check the well stations (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_WELL_STATIONS );
	        Message.printStatus ( 2, routine, "Writing StateMod well stations file \"" +
	        	OutputFile_full + "\"" );
			StateMod_Well.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_WellStation_List"), OutputComments_List );
		}
    	else if ( this instanceof WriteWellRightsToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod well rights file \"" +
	        	OutputFile_full + "\"" );
	        PropList writeProps = new PropList("WriteWellRightsToStateMod");
	        if ( WriteDataComments != null ) {
	        	writeProps.set ( "WriteDataComments", "" + WriteDataComments );
	        }
			StateMod_WellRight.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_WellRight_List"), OutputComments_List,
				writeProps );
		}
       	else if ( this instanceof WriteNetworkToStateMod_Command ) {
			// Check the network (will be output later in check file)...
       		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_NETWORK );
	        Message.printStatus ( 2, routine, "Writing StateMod network file \"" + OutputFile_full + "\"" );
	        StateMod_NodeNetwork net = (StateMod_NodeNetwork)processor.getPropContents("StateMod_Network");
	        if ( net == null ) {
	        	message = "No StateMod generalized network is available to write.";
	            Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that a StateMod generalized network was read or created before this command." ) );
	        }
	        else {
	        	// FIXME SAM 2009-01-21 Need to add comments to header
	        	net.writeXML ( OutputFile_full ); //, OutputComments_List );
	        }
		}
    	else if ( this instanceof WriteRiverNetworkToStateMod_Command ) {
			// Check the river network (will be output later in check file)...
    		// FIXME SAM 2009-04-27 Remove when finished updating checks
			//processor.runStateModDataCheck( StateMod_DataSet.COMP_RIVER_NETWORK );
	        Message.printStatus ( 2, routine, "Writing StateMod river network file \"" +
	        	OutputFile_full + "\"" );
			StateMod_RiverNetworkNode.writeStateModFile(OutputFile_prevFull, OutputFile_full,
				(List)processor.getPropContents("StateMod_RiverNetworkNode_List"),
				OutputComments_List, true );
		}
    	else if ( this instanceof WriteOperationalRightsToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod operational rights file \"" +
	        	OutputFile_full + "\"" );
			// 2 is the file version (introduced for StateMod version 12 change)
			StateMod_OperationalRight.writeStateModFile(OutputFile_prevFull, OutputFile_full, 2,
				(List)processor.getPropContents("StateMod_OperationalRight_List"), OutputComments_List,
				(StateMod_DataSet)null );
		}
       	else if ( this instanceof WriteResponseToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod response file \"" + OutputFile_full + "\"" );
	        StateMod_DataSet dataSet = (StateMod_DataSet)processor.getPropContents("StateMod_DataSet");
	        if ( dataSet == null ) {
	        	message = "No StateMod data set is available to write the response file.";
	            Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Contact software support." ) );
	        }
	        else {
	        	StateMod_DataSet.writeStateModFile ( dataSet, OutputFile_prevFull,
	        		OutputFile_full, OutputComments_List );
	        }
		}
       	else if ( this instanceof WriteControlToStateMod_Command ) {
	        Message.printStatus ( 2, routine, "Writing StateMod control file \"" + OutputFile_full + "\"" );
	        StateMod_DataSet dataSet = (StateMod_DataSet)processor.getPropContents("StateMod_DataSet");
	        if ( dataSet == null ) {
	        	message = "No StateMod data set is available to write the control file.";
	            Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Contact software support." ) );
	        }
	        else {
	        	StateMod_DataSet.writeStateModControlFile ( dataSet, OutputFile_prevFull,
	        		OutputFile_full, OutputComments_List );
	        }
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
	String Precision = parameters.getValue ( "Precision" );
	String WriteHow = parameters.getValue ( "WriteHow" );
	String WriteDataComments = parameters.getValue ( "WriteDataComments" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (this instanceof WriteDelayTablesMonthlyToStateMod_Command) ||
		(this instanceof WriteDelayTablesDailyToStateMod_Command)) {
		if ( (Precision != null) && (Precision.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Precision=" + Precision );
		}
	}
	if ( this instanceof WriteWellRightsToStateMod_Command ) {
		if ( (WriteDataComments != null) && (WriteDataComments.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "WriteDataComments=" + WriteDataComments );
		}
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