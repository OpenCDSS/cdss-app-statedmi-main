// StartRegressionTestResultsReport_Command - This class initializes, checks, and runs the StartRegressionTestResultsReport() command.

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;

/**
This class initializes, checks, and runs the StartRegressionTestResultsReport() command.
*/
public class StartRegressionTestResultsReport_Command extends AbstractCommand implements Command, FileGenerator, CommandDiscoverable, ObjectListProvider
{

/**
Strings used for parameter values, here and in the editor dialog.
*/
protected final String _Date = "Date";
protected final String _DateTime = "DateTime";

/**
Output (log) file that is created by this command.
*/
private File __OutputFile_File = null;

/**
The table that is created.
*/
private DataTable __table = null;

/**
Constructor.
*/
public StartRegressionTestResultsReport_Command ()
{	super();
	setCommandName ( "StartRegressionTestResultsReport" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag,int warning_level )
throws InvalidCommandParameterException
{	String routine = getCommandName() + "_checkCommandParameters";
	String OutputFile = parameters.getValue ( "OutputFile" );
	String working_dir = null;
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	try { Object o = processor.getPropContents ( "WorkingDir" );
		// Working directory is available so use it...
		if ( o != null ) {
			working_dir = (String)o;
			if ( working_dir.equals("") ) {
				working_dir = null;	// Not available.
			}
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
		// A null output file means that the current file should be re-opened.
		if ( (OutputFile != null) && (working_dir != null) ) {
			String adjusted_path = IOUtil.adjustPath(working_dir,OutputFile);
			File f = new File(adjusted_path);
			File f2 = new File(f.getParent());
			if (!f2.exists()) {
				message = "The output file parent folder \"" + f.getParent() +
				"\" does not exist: \"" + adjusted_path + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the ouput file folder exists." ) );
			}
			f = null;
			f2 = null;
		}
	}
	catch ( Exception e ) {
		// Print a stack trace so the output shows up somewhere because the log file
		// may not be used.
		if ( Message.isDebugOn ) {
			e.printStackTrace();
		}
		message = "\nThe output file \"" + OutputFile +
		"\" cannot be adjusted to an absolute path using the working directory \"" +
		working_dir + "\".";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the path information is consistent." ) );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(2);
	validList.add ( "Outputfile" );
	validList.add ( "TestResultsTableID" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );
	
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine,
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
	
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new StartRegressionTestResultsReport_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
	List<File> list = new Vector<File>();
	if ( getOutputFile() != null ) {
		list.add ( getOutputFile() );
	}
	return list;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
The following classes can be requested:  DataTable
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{   DataTable table = getDiscoveryTable();
    List<T> v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new Vector<T>();
        v.add ( (T)table );
    }
    return v;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
	return __OutputFile_File;
}

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
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(commandPhase);
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	String OutputFile = parameters.getValue ( "OutputFile" );
	String TestResultsTableID = parameters.getValue ( "TestResultsTableID" );
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}
    
    // Now start the report file
	
	String OutputFile_full = null;	// File with path, used below and in final catch.
    DataTable table = null;
	try {
		// Create the table for output.  Make sure the columns agree with lookups in TSCommandProcessor.appendToRegressionTestReport()
		if ( (TestResultsTableID != null) && !TestResultsTableID.isEmpty() ) {
	        if ( commandPhase == CommandPhaseType.RUN ) {
	            // Create the table of appropriate type
	        	String [] columnNames = {
	        		"Num",
	        		"Enabled",
	        		"Run Time (ms)",
	        		"Test Pass/Fail",
	        		"Commands Expected Status",
	        		"Commands Actual Status",
	        		"Command File"	
	        	};
	        	int [] columnTypes = {
	        		TableField.DATA_TYPE_INT,
	        		TableField.DATA_TYPE_STRING,
	        		TableField.DATA_TYPE_LONG,
	        		TableField.DATA_TYPE_STRING,
	        		TableField.DATA_TYPE_STRING,
	        		TableField.DATA_TYPE_STRING,
	        		TableField.DATA_TYPE_STRING
	        	};
	        	List<TableField> columnList = new ArrayList<TableField>();
                for ( int i = 0; i < columnNames.length; i++ ) {
                    // No precision is necessary and specify the field width as -1 meaning it can grow
                    columnList.add ( new TableField(columnTypes[i], columnNames[i], -1) );
                }
	            table = new DataTable( columnList );
	            table.setTableID ( TestResultsTableID );
	            
	            // Set the table in the processor...
	            
	            PropList request_params = new PropList ( "" );
	            request_params.setUsingObject ( "Table", table );
	            try {
	                processor.processRequest( "SetTable", request_params);
	            }
	            catch ( Exception e ) {
	                message = "Error requesting SetTable(Table=...) from processor.";
	                Message.printWarning(warning_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
	                status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                           message, "Report problem to software support." ) );
	            }
	        }
	        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
	            // Create an empty table and set the ID
	            table = new DataTable();
	            table.setTableID ( TestResultsTableID );
	            setDiscoveryTable ( table );
	        }
		}	
		
		// Now process the report file and optionally the table
		if ( commandPhase == CommandPhaseType.RUN ) {
			OutputFile_full = IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile);
			StateDMICommandProcessorUtil.openNewRegressionTestReportFile ( OutputFile_full, table, false );
			setOutputFile ( new File(OutputFile_full));
		}
	}
	catch ( Exception e ) {
		message = "Unexpected error (re)starting the regression test report file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Check the log file or command window for details." ) );
		throw new CommandException ( message );
	}
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
	__OutputFile_File = file;
}

// Can rely on base class for toString().

}