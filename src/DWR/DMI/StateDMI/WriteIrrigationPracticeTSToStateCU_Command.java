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
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_IrrigationPracticeTS;

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
<p>
This class initializes, checks, and runs the WriteIrrigationPracticeTSToStateCU() command.
</p>
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
	Vector<String> valid_Vector = new Vector<String>(8);
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "OutputStart" );
	valid_Vector.add ( "OutputEnd" );
	valid_Vector.add ( "PrecisionForArea" );
	valid_Vector.add ( "Version" );
	valid_Vector.add ( "OneLocationPerFile" );
	valid_Vector.add ( "CheckData" );
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
Check the irrigation practice time series for integrity, mainly to make sure that
the acreage terms add up.  This method may be made public at some point if can
be used by StateDMI commands that read or write the file.
@param tslist List of StateCU_IrrigationPracticeTS to check.
@param status CommandStatus to append check warnings.
*/
private void checkIrrigationPracticeTS ( List<StateCU_IrrigationPracticeTS> tslist, CommandStatus status )
{	
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	StateCU_IrrigationPracticeTS ipy_ts = null;
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
	for ( int i = 0; i < size; i++ ) {
		ipy_ts = tslist.get(i);
		id = ipy_ts.getID();
		AcresSWFlood_yts = ipy_ts.getAcswflTS();
		AcresSWSprinkler_yts = ipy_ts.getAcswsprTS();
		AcresGWFlood_yts = ipy_ts.getAcgwflTS();
		AcresGWSprinkler_yts = ipy_ts.getAcgwsprTS();
		AcresTotal_yts = ipy_ts.getTacreTS();
		// Loop through the period and check the acreage.
		date_end = ipy_ts.getDate2();
		for ( DateTime date = new DateTime(ipy_ts.getDate1());
			date.lessThanOrEqualTo(date_end); date.addYear(1) ) {
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
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						"Location \"" + id + "\" acreage terms do not add to total in " +
						date.getYear() + ".  Total of parts = " +
						StringUtil.formatString(calculated_total,"%."+precision+"f") +
						" Total value in IPY = " + StringUtil.formatString(AcresTotal_double,"%."+precision+"f"),
						"Verify data processing." ) );
			}
		}
	}
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
		return new Vector<File>();
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
    String routine = getClass().getName() + ".runCommandInternal", message;
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
    
   // Get the data needed for the command
    
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
    		List<StateCU_IrrigationPracticeTS> one = new Vector<StateCU_IrrigationPracticeTS>();
    		String id;
    		String OutputFile2;
    		outputFileList = new Vector<File>();
    		File f = new File(OutputFile_full);
    		String parent = f.getParent();
    		String file = f.getName();
    		for ( int i = 0; i < size; i++ ) {
    			ipy = ipyList.get(i);
    			id = ipy.getID();
    			one.clear();
    			one.add(ipy);
    			/* TODO SAM 2007-10-18 Old was to put ID at end - makes it hard to select
    			 * sequence of files for viewing.

    			String ext = IOUtil.getFileExtension(OutputFile);
    			if ( ext == null ) {
    				OutputFile2 = OutputFile + "." + id + ".ipy";
    			}
    			else {
    				OutputFile2 = OutputFile.substring(0,OutputFile.length()- ext.length() - 1) + "." + id + ".ipy";
    			}
    			*/
    			OutputFile2 = parent + File.pathSeparator + "ipy_" + id + "_" + file;
    			Message.printStatus ( 2, routine, "Writing single " + id +
    				" CU irrigation practice time series to file: \"" + OutputFile2 +"\"");
    			// Don't allow update.
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
    		StateCU_IrrigationPracticeTS.writeStateCUFile ( OutputFile_prevFull, OutputFile_full,
    			ipyList, OutputComments_List, OutputStart_DateTime, OutputEnd_DateTime, write_props );
    	}
    	// Set the filename(s) for the FileGenerator interface
    	if ( outputFileList == null ) {
    		// Create a list with a single file
    		outputFileList = new Vector<File>(1);
    		outputFileList.add ( new File(OutputFile_full) );
    	} // Otherwise multiple files were written and the list added to above
    	setOutputFileList ( outputFileList );
    	// FIXME SAM 2009-02-16 Need to add to output file list...
    	if ( CheckData_boolean ) {
    		checkIrrigationPracticeTS ( ipyList, status );
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
