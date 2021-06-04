// WriteIrrigationPracticeTSToStateCU_Command - This class initializes, checks, and runs the WriteIrrigationPracticeTSToStateCU() command.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;
import RTi.TS.YearTS;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
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
This class initializes, checks, and runs the WriteIrrigationPracticeTSToStateCU() command.
*/
public class WriteIrrigationPracticeTSToStateCU_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

//Version
protected final String _10 = "10";

// OneLocationPerFile...
protected final String _False = "False";
protected final String _True = "True";

/**
List of output files that are created by this command.
*/
private List<File> __OutputFile_List = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public WriteIrrigationPracticeTSToStateCU_Command ()
{	super();
	setCommandName ( "WriteIrrigationPracticeTSToStateCU" );
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
	String PrecisionForArea = parameters.getValue ( "PrecisionForArea" );
	String RecalculateTotal = parameters.getValue ( "RecalculateTotal" );
	String Version = parameters.getValue ( "Version" );
	String OneLocationPerFile = parameters.getValue ( "OneLocationPerFile" );
	String CheckData = parameters.getValue ( "CheckData" );
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
	
	if ( (OutputStart != null) && (OutputStart.length() != 0) && !StringUtil.isInteger(OutputStart) ) {
        message = "The value for the output start (" + OutputStart + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output start as blank or an integer." ) );
	}
	
	if ( (OutputEnd != null) && (OutputEnd.length() != 0) && !StringUtil.isInteger(OutputEnd) ) {
        message = "The value for the output end (" + OutputEnd + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output end as blank or an integer." ) );
	}

	if ( (RecalculateTotal != null) && !RecalculateTotal.isEmpty() ) {
		message = "The RecalculateTotal value (" + RecalculateTotal + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _False + "(default) or " + _True) );
	}

	if ( (RecalculateTotal != null) && !RecalculateTotal.isEmpty() ) {
		message = "The RecalculateTotal value (" + RecalculateTotal + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _False + "(default) or " + _True) );
	}
	
	if ( (Version != null) && (Version.length() != 0) && !Version.equals(_10) ) {
        message = "The value for Version (" + Version + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify Version as blank (default) or " + _10 ) );
	}
	
	if ( (PrecisionForArea != null) && (PrecisionForArea.length() != 0) &&
		!StringUtil.isInteger(PrecisionForArea) ) {
        message = "The value for PrecisionForArea (" + PrecisionForArea + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify PrecisionForArea as an integer." ) );
	}
	
	if ( (OneLocationPerFile != null) && (OneLocationPerFile.length() != 0) &&
		!OneLocationPerFile.equals(_False) && !OneLocationPerFile.equals(_True) ) {
        message = "The value for OneLocationPerFile (" + OneLocationPerFile + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify OneLocationPerFile as " + _False + " (default) or " + _True + ".") );
	}
	
	if ( (CheckData != null) && (CheckData.length() != 0) &&
		!CheckData.equals(_False) && !CheckData.equals(_True) ) {
        message = "The value for CheckData (" + CheckData + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify CheckData as " + _False + " or " + _True + " (default).") );
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
	List<String> validList = new ArrayList<>(8);
	validList.add ( "OutputFile" );
	validList.add ( "OutputStart" );
	validList.add ( "OutputEnd" );
	validList.add ( "PrecisionForArea" );
	validList.add ( "RecalculateTotal" );
	validList.add ( "Version" );
	validList.add ( "OneLocationPerFile" );
	validList.add ( "CheckData" );
	validList.add ( "WriteHow" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
Check the irrigation practice time series for integrity, mainly to make sure that
the acreage terms add up.  This method may be made public at some point if can
be used by StateDMI commands that read or write the file.
@param tslist List of StateCU_IrrigationPracticeTS to check.
@param culocList List of StateCU_Location to check.
@param status CommandStatus to append check warnings.
*/
private int checkIrrigationPracticeTS ( List<StateCU_IrrigationPracticeTS> tslist, List<StateCU_Location> culocList, CommandStatus status,
	String command_tag, int warning_level, int warning_count )
{	String routine = getClass().getSimpleName() + ".checkIrrigationPracticeTS";	
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	StateCU_IrrigationPracticeTS ipyts = null;
	YearTS AcresSWFlood_yts = null;
	YearTS AcresSWSprinkler_yts = null;
	YearTS AcresGWFlood_yts = null;
	YearTS AcresGWSprinkler_yts = null;
	YearTS AcresTotal_yts = null;
	double AcresSWFlood_double;
	double AcresSWSprinkler_double;
	double AcresGWFlood_double;
	double AcresGWSprinkler_double;
	double AcresTotal_double;
	double tolerance = 1.0;	// Check to nearest integer since acreage is written as whole number
	int precision = 0;
	// TODO SAM 2007-09-09 Need some utilities to help with checks
	// Need to intelligently compute the precision from the tolerance
	DateTime date_end;		// End of period for looping.
	String id;
	double calculated_total;	// Calculated total of parts.
	int ipyWarnCountMax = 50;
	int totalWarnCountMax = 1000;
	int ipyWarnCount; // warning count for specific IPY
	int totalWarnCount = 0; // warning count for all locations
	String message;
	int year;
	double ipyValue;
	String ipyValueString;
	String format1 = "%.1f";
	for ( int i = 0; i < size; i++ ) {
		ipyts = tslist.get(i);
		ipyWarnCount = 0;
		id = ipyts.getID();
		AcresSWFlood_yts = ipyts.getAcswflTS();
		AcresSWSprinkler_yts = ipyts.getAcswsprTS();
		AcresGWFlood_yts = ipyts.getAcgwflTS();
		AcresGWSprinkler_yts = ipyts.getAcgwsprTS();
		AcresTotal_yts = ipyts.getTacreTS();
		// Loop through the period and check the acreage.
		date_end = ipyts.getDate2();
		for ( DateTime date = new DateTime(ipyts.getDate1()); date.lessThanOrEqualTo(date_end); date.addYear(1) ) {
			year = date.getYear();
			AcresSWFlood_double = AcresSWFlood_yts.getDataValue(date);
			AcresSWSprinkler_double = AcresSWSprinkler_yts.getDataValue(date);
			AcresGWFlood_double = AcresGWFlood_yts.getDataValue(date);
			AcresGWSprinkler_double = AcresGWSprinkler_yts.getDataValue(date);
			AcresTotal_double = AcresTotal_yts.getDataValue(date);
			// Do not include missing values in total...
			calculated_total = -999.0;
			if ( AcresSWFlood_double >= 0 ) {
				if ( calculated_total < 0.0 ) {
					calculated_total = AcresSWFlood_double;
				}
				else {
					calculated_total += AcresSWFlood_double;
				}
			}
			if ( AcresSWSprinkler_double >= 0.0 ) {
				if ( calculated_total < 0.0 ) {
					calculated_total = AcresSWSprinkler_double;
				}
				else {
					calculated_total += AcresSWSprinkler_double;
				}
			}
			if ( AcresGWFlood_double >= 0.0 ) {
				if ( calculated_total < 0.0 ) {
					calculated_total = AcresGWFlood_double;
				}
				else {
					calculated_total += AcresGWFlood_double;
				}
			}
			if ( AcresGWSprinkler_double >= 0.0 ) {
				if ( calculated_total < 0.0 ) {
					calculated_total = AcresGWSprinkler_double;
				}
				else {
					calculated_total += AcresGWSprinkler_double;
				}
			}
			// Only check if both values are non-missing.  Otherwise there are a lot of nuisance warnings.
			if ( (calculated_total >= 0.0) && (AcresTotal_double >= 0.0) &&
				Math.abs(calculated_total - AcresTotal_double) > tolerance ) {
				++ipyWarnCount;
				++totalWarnCount;
				String format = "%." + precision + "f";
				// See if any set or fill commands were used that might cause data to be different from database.
				StateCU_Location culoc = null;
				int pos = StateCU_Util.indexOf(culocList, ipyts.getID());
				String setString = "";
				if ( pos >= 0 ) {
					culoc = culocList.get(pos);
					if ( culoc.hasSetIrrigationPracticeTSCommands(date.getYear()) ) {
						setString = "  Check SetIrrigationPracticeTS*() command data.";
					}
					if ( culoc.hasFillIrrigationPracticeTSCommands(date.getYear()) ) {
						setString += "  Check FillIrrigationPracticeTS*() command data.";
					}
				}
				message =
					"Location \"" + id + "\" acreage terms do not add to total in " +
					date.getYear() + ".  Total of parts = " +
					StringUtil.formatString(calculated_total,format) +
					" Total value in IPY = " + StringUtil.formatString(AcresTotal_double,format) +
					", difference = " + StringUtil.formatString((AcresTotal_double - calculated_total), format) + 
					".  AcSwFl=" + String.format(format, AcresSWFlood_double) +
					" AcSwSpr=" + String.format(format, AcresSWSprinkler_double) +
					" AcGwFl=" + String.format(format, AcresGWFlood_double) +
					" AcGwSpr=" + String.format(format, AcresGWSprinkler_double);
				Message.printWarning(3, routine, message);
				if ( (ipyWarnCount <= ipyWarnCountMax) && (totalWarnCount < totalWarnCountMax) ) {
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE, message,
							"Verify data processing." + setString ) );
				}
			}
			// The following are less critical checks and due to large numbers may overflow so limit the output.
			ipyValue = ipyts.getTacre(year);
			ipyValueString = String.format(format1, ipyValue);
			if ( ipyValue < 0.0 ) {
				// Value is missing.
				++ipyWarnCount;
				++totalWarnCount;
				message = "Location \"" + id + "\" IPY total area value is missing for " + date.getYear() +
					" IPY total area = " + ipyValueString +
					", warning counts = " + ipyWarnCount + "," + totalWarnCount;
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
				// Only create command warning if under max count so don't overload UI displays.
				if ( (ipyWarnCount <= ipyWarnCountMax) && (totalWarnCount < totalWarnCountMax) ) {
					// OK to display warning.
					status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING, message,
							"Verify that data processing considers data values and uses filling if necessary."));
						}
			}
			if ( totalWarnCount == totalWarnCountMax ) {
				// Notify about limiting the total number of warnings
				// - print this message once at the end of displayed warnings.
				// - don't need to print to log file - only for UI.
				message = "Maximum missing data warning count " + totalWarnCountMax + " has been reached for displayed warnings - see the log file for full list of warnings.";
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING, message,
						"Verify that data processing is correct."));
			}
			else if ( (ipyWarnCount == ipyWarnCountMax) && (totalWarnCount < totalWarnCountMax) ) {
				// Notify about limiting the number of warnings
				// - have not reached maximum total number yet
				// - print this message once at the end of displayed warnings.
				// - don't need to print to log file - only for UI.
				message = "Location \"" + id + "\" limited to " + ipyWarnCountMax + " displayed missing data warnings - see the log file for full list of warnings.";
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING, message,
						"Verify that data processing is correct."));
			}
		}
	}
	return warning_count;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new WriteIrrigationPracticeTSToStateCU_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
	List<File> outputFileList = getOutputFileList();
	if ( outputFileList == null ) {
		return new ArrayList<>();
	}
	else {
		return outputFileList;
	}
}

/**
Return the list of output files generated by this method.  This method is used internally.
*/
protected List<File> getOutputFileList ()
{
	return __OutputFile_List;
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
	// Check whether the processor wants output files to be created...

    CommandPhaseType command_phase = CommandPhaseType.RUN;
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	if ( !StateDMICommandProcessorUtil.getCreateOutput(processor) ) {
		Message.printStatus ( 2, routine,
		"Skipping \"" + toString() + "\" because output is not being created." );
	}
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    
    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String PrecisionForArea = parameters.getValue ( "PrecisionForArea" );
	int PrecisionForArea_int = 1;	// Default
	if ( PrecisionForArea != null ) {
		PrecisionForArea_int = Integer.parseInt(PrecisionForArea);
	}
	String RecalculateTotal = parameters.getValue ( "RecalculateTotal" );
	boolean recalculateTotal = true; // Default
	if ( (RecalculateTotal != null) && RecalculateTotal.equalsIgnoreCase(_False) ) {
		recalculateTotal = false;
	}
	String Version = parameters.getValue ( "Version" );
	String CheckData = parameters.getValue ( "CheckData" );
	boolean CheckData_boolean = true;
	if ( (CheckData != null) && CheckData.equalsIgnoreCase(_False) ) {
		CheckData_boolean = false;
	}
	String OneLocationPerFile = parameters.getValue ( "OneLocationPerFile" );
	boolean OneLocationPerFile_boolean = false; // default
	if ( (OneLocationPerFile != null) && OneLocationPerFile.equalsIgnoreCase(_True) ) {
		OneLocationPerFile_boolean = true;
	}
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
    }
    
    // Get the list of StateCU_IrrigationPracticeTS to output.
    
    List<StateCU_IrrigationPracticeTS> ipyList = null;
    try {
    	@SuppressWarnings("unchecked")
		List<StateCU_IrrigationPracticeTS> dataList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List" );
    	ipyList = dataList;
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting irrigation practice time series to output (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }

	// Get the list of CU locations...
	
	List<StateCU_Location> culocList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents ( "StateCU_Location_List");
		culocList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting CU location data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
    
	// Get the output period

	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	try {
		OutputStart_DateTime = (DateTime)processor.getPropContents("OutputStart");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputStart from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	try {
		OutputEnd_DateTime = (DateTime)processor.getPropContents("OutputEnd");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputEnd from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
    
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}

    String OutputFile_full = OutputFile;
    List<File> outputFileList = null;
    try {
        List<String> OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<String> outputCommentsList = (List<String>)o;
                OutputComments_List = outputCommentsList;
            }
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
            Message.printWarning(3, routine, message );
            Message.printWarning(3, routine, e );
        }
        
    	if ( OutputStart != null ) {
    		OutputStart_DateTime = new DateTime(DateTime.PRECISION_YEAR);
    		OutputStart_DateTime.setYear( Integer.parseInt(OutputStart));
    	}
    	if ( OutputEnd != null ) {
    		OutputEnd_DateTime = new DateTime(DateTime.PRECISION_YEAR);
    		OutputEnd_DateTime.setYear( Integer.parseInt(OutputEnd));
    	}
        
    	// Clear the filename for the FileGenerator interface
    	setOutputFileList ( null );
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
    	
    	// Check the Irrigation Practice TS data (output later in check file)
    	// FIXME SAM 2009-04-27 Remove when finished updating checks
    	//processor.runStateCUDataCheck( StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY );
    	
    	PropList write_props = new PropList ( "WriteIrrigationPracticeTSToStateCU" );
    	if ( Version != null ) {
    		write_props.add ( "Version=" + Version );
    	}
    	
    	write_props.add( "PrecisionForArea=" + PrecisionForArea_int );

    	if ( OneLocationPerFile_boolean ) {
    		int size = ipyList.size();
    		StateCU_IrrigationPracticeTS ipy = null;
    		List<StateCU_IrrigationPracticeTS> one = new ArrayList<>();
    		String id;
    		String OutputFile2;
    		outputFileList = new ArrayList<File>();
    		File f = new File(OutputFile_full);
    		String parent = f.getParent();
    		String file = f.getName();
    		for ( int i = 0; i < size; i++ ) {
    			ipy = ipyList.get(i);
    			id = ipy.getID();
				if ( recalculateTotal ) {
					// Refresh the total acres.  This recalculates the GW and SW-only parts and then overall total.
					ipy.refreshTacre ( true, true );
				}
    			one.clear();
    			one.add(ipy);
    			OutputFile2 = parent + File.pathSeparator + "ipy_" + id + "_" + file;
    			Message.printStatus ( 2, routine, "Writing single " + id +
    				" CU irrigation practice time series to file: \"" + OutputFile2 +"\"");
    			// Don't allow update.
    			// Note that the following recomputes totals by default for latest StateCU format but does not update the object.
    			StateCU_IrrigationPracticeTS.writeStateCUFile ( null, OutputFile2, one, OutputComments_List,
					OutputStart_DateTime, OutputEnd_DateTime, write_props );
    			outputFileList.add ( new File(OutputFile2) );
    		}
    	}
    	else {
    		// Write all the time series to one file...
    		Message.printStatus ( 2, routine,
    			"Writing " + ipyList.size() + " CU irrigation practice time series to file: \"" +
    			OutputFile_full +"\"");
			if ( recalculateTotal ) {
				// Refresh the total acres.  This recalculates the GW and SW-only parts and then overall total.
				for ( StateCU_IrrigationPracticeTS ipy : ipyList ) {
					ipy.refreshTacre ( true, true );
				}
			}
			// Note that the following recomputes totals by default for latest StateCU format but does not update the object.
    		StateCU_IrrigationPracticeTS.writeStateCUFile ( OutputFile_prevFull, OutputFile_full,
    			ipyList, OutputComments_List, OutputStart_DateTime, OutputEnd_DateTime, write_props );
    		// Create a list with a single file
    		outputFileList = new ArrayList<>(1);
    		outputFileList.add ( new File(OutputFile_full) );
    	}
   		// Set the filename(s) for the FileGenerator interface
    	setOutputFileList ( outputFileList );
    	// FIXME SAM 2009-02-16 Need to add to output file list...
    	if ( CheckData_boolean ) {
    		warning_count = checkIrrigationPracticeTS ( ipyList, culocList, status,
    			command_tag, warning_level, warning_count);
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
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
protected void setOutputFileList ( List<File> outputFileList )
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
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String PrecisionForArea = parameters.getValue ( "PrecisionForArea" );
	String RecalculateTotal = parameters.getValue ( "RecalculateTotal" );
	String Version = parameters.getValue ( "Version" );
	String OneLocationPerFile = parameters.getValue ( "OneLocationPerFile" );
	String CheckData = parameters.getValue ( "CheckData" );
	String WriteHow = parameters.getValue ( "WriteHow" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputStart=\"" + OutputStart + "\"" );
	}
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputEnd=\"" + OutputEnd + "\"" );
	}
	if ( (PrecisionForArea != null) && (PrecisionForArea.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PrecisionForArea=\"" + PrecisionForArea + "\"" );
	}
	if ( (RecalculateTotal != null) && (RecalculateTotal.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RecalculateTotal=" + RecalculateTotal );
	}
	if ( (Version != null) && (Version.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Version=\"" + Version + "\"" );
	}
	if ( (OneLocationPerFile != null) && (OneLocationPerFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OneLocationPerFile=" + OneLocationPerFile );
	}
	if ( (CheckData != null) && (CheckData.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CheckData=" + CheckData );
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
