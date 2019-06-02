// RunCommands_Command - This class initializes, checks, and runs the RunCommands() command.

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
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

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
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

// FIXME SAM 2008-07-15 Need to add ability to inherit the properties of the main processor

/**
<p>
This class initializes, checks, and runs the RunCommands() command.
</p>
*/
public class RunCommands_Command extends AbstractCommand implements Command
{

/**
ExpectedStatus parameter values.
*/
protected final String _Unknown = "Unknown";
protected final String _Success = "Success";
protected final String _Warning = "Warning";
protected final String _Failure = "Failure";

/**
ResetWorkflowProperties parameter values.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
ResetWorkflowProperties parameter values.
*/
private final String __FAIL = "FAIL";
private final String __PASS = "PASS";

/**
AppendResults parameter values.
*/
// FIXME SAM 2007-12-13 Need to enable AppendResults

/**
Constructor.
*/
public RunCommands_Command ()
{	super();
	setCommandName ( "RunCommands" );
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
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String InputFile = parameters.getValue ( "InputFile" );
    String ExpectedStatus = parameters.getValue ( "ExpectedStatus" );
    //String InheritParentWorkflowProperties = parameters.getValue ( "InheritParentWorkflowProperties" );
	String warning = "";
    String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (InputFile == null) || (InputFile.length() == 0) ) {
        message = "The input file must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an input file." ) );
	}
	else {	String working_dir = null;
	
			try { Object o = processor.getPropContents ( "WorkingDir" );
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
                                message, "Software error - report problem to support." ) );
			}
	
		try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath ( working_dir, InputFile));
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The input file does not exist: \"" + adjusted_path + "\".";
				warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the command file to run exists." ) );
            }
			f = null;
		}
		catch ( Exception e ) {
            message = "The input file \"" + InputFile +
            "\" ncannot be adjusted to an absolute path using the working directory \"" +
            working_dir + "\".";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that command file to run and working directory paths are compatible." ) );
		}
	}
	/*
    if ( (InheritParentWorkflowProperties != null) && (InheritParentWorkflowProperties.length() == 0) &&
            !InheritParentWorkflowProperties.equalsIgnoreCase(_False) &&
            !InheritParentWorkflowProperties.equalsIgnoreCase(_True) ) {
        message = "The InheritParentWorkflowProperties parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an expected status of " + _False + " (default if blank), " +
                        ", or " + _True) );
    }
    */
    
    if ( (ExpectedStatus != null) && (ExpectedStatus.length() == 0) &&
            !ExpectedStatus.equalsIgnoreCase(_Unknown) &&
            !ExpectedStatus.equalsIgnoreCase(_Success) &&
            !ExpectedStatus.equalsIgnoreCase(_Warning) &&
            !ExpectedStatus.equalsIgnoreCase(_Failure) ) {
        message = "The expected status is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an expected status of " + _Unknown + ", " + _Success + ", " +
                        _Warning + ", or " + _Failure) );
    }

	// Check for invalid parameters...
    List<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "InputFile" );
	//valid_Vector.add ( "InheritParentWorkflowProperties" );
    valid_Vector.add ( "ExpectedStatus" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new RunCommands_JDialog ( parent, this )).ok();
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "RunCommands_Command.runCommand", message;
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
	PropList parameters = getCommandParameters();

	String InputFile = parameters.getValue ( "InputFile" );
    String ExpectedStatus = parameters.getValue ( "ExpectedStatus" );
    //String InheritParentWorkflowProperties = parameters.getValue ( "InheritParentWorkflowProperties" );
	String AppendResults = parameters.getValue ( "AppendResults" );
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Get the working directory from the processor that is running the commands.

	String WorkingDir = StateDMICommandProcessorUtil.getWorkingDir(processor);
	String InputFile_full = null;
	try {
        InputFile_full = IOUtil.verifyPathForOS(IOUtil.adjustPath ( WorkingDir, InputFile) );
		Message.printStatus ( 2, routine,
		"Processing commands from file \"" + InputFile_full + "\" using command file runner.");
		
		StateDMICommandFileRunner runner = new StateDMICommandFileRunner ();
        // This will set the initial working directory of the runner to that of the command file...
		runner.readCommandFile(InputFile_full);
        // Set the database connection information...
        // FIXME SAM 2007-11-25 This needs to be generic "DataSource" objects.
        StateDMI_Processor runner_processor = runner.getProcessor();
        runner_processor.setPropContents("HydroBaseDMIList", processor.getPropContents("HydroBaseDMIList"));
        // FIXME SAM 2008-07-15 If inheriting properties from parent workflow, transfer here
        // similar to how results will be appended after running.
        // Run the commands.
		runner.runCommands();
		
		// Set the CommandStatus for this command to the most severe status of the
		// commands file that was just run.
		CommandStatusType maxSeverity = StateDMICommandProcessorUtil.getCommandStatusMaxSeverity((StateDMI_Processor)runner.getProcessor());
		String testPassFail = "????"; // Status for the test, which is not always the same as maxSeverity
		String expectedStatus = CommandStatusType.SUCCESS.toString(); // Expected status of running command file
		if ( ExpectedStatus != null ) {
		    expectedStatus = ExpectedStatus;
		}
		if ( ExpectedStatus != null ) {
		    if ( maxSeverity.toString().equalsIgnoreCase(ExpectedStatus) ) {
                // User has indicated an expected status and it matches the actual so consider this a success.
                // This should generally be used only when running a test that we expect to fail (e.g., run
                // obsolete command or testing handling of errors).
                status.addToLog(CommandPhaseType.RUN,
                        new CommandLogRecord(
                                CommandStatusType.SUCCESS,
                                "Severity is max of commands file that was run (may not be a problem) - matches expected so considered Success.",
                                "Additional status messages are omitted to allow test to be success - " +
                                "refer to log file if warning/failure."));
                // TODO SAM 2008-07-09 Need to evaluate how to append all the log messages but still
                // have a successful status that shows in the displays.
                // DO NOT append the messages from the command because their status will cause the
                // error displays to show problem indicators.
                testPassFail = __PASS;
		    }
		    else {
		        // User has specified an expected status and it does NOT match the actual status so this is a failure.
                status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(
                        CommandStatusType.SUCCESS,
                        "Severity is max of commands file that was run (may not be a problem) - does not match expected so considered Failure.",
                        "Check the command to confirm the expected status."));
                // TODO SAM 2008-07-09 Need to evaluate how to append all the log messages but still
                // have a successful status that shows in the displays.
                // DO NOT append the messages from the command because their status will cause the
                // error displays to show problem indicators.
                testPassFail = __FAIL;
		    }
        }
        else {
            status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(
						maxSeverity,
						"Severity is max of commands file that was run (may not be a problem).",
						"See additional status messages and refer to log file if warning/failure."));
            // Append the log records from the command file that was run.
            CommandStatusUtil.appendLogRecords ( status, (List)runner.getProcessor().getCommands() );
            if ( maxSeverity.greaterThanOrEqualTo(CommandStatusType.WARNING)) {
                testPassFail = __FAIL;
            }
            else {
                testPassFail = __PASS;
            }
        }

        // Also add a record to the regression report...
        // TODO SAM 2008-07-09 Evaluate whether to print the expected status, the actual status and the
        // test status.
        StateDMICommandProcessorUtil.appendToRegressionTestReport(processor,testPassFail,expectedStatus,maxSeverity,InputFile_full);

		// If it was requested to append the results to the calling processor, get
		// the results from the runner and do so...
		
		if ( (AppendResults != null) && AppendResults.equalsIgnoreCase("true")) {
			/* FIXME SAM 2008-11-19 Need to enable
			StateDMI_Processor processor2 = runner.getProcessor();
			Object o_tslist = processor2.getPropContents("TSResultsList");
			PropList request_params = new PropList ( "" );
			if ( o_tslist != null ) {
				Vector tslist = (Vector)o_tslist;
				int size = tslist.size();
				TS ts;
				for ( int i = 0; i < size; i++ ) {
					ts = (TS)tslist.elementAt(i);
					request_params.setUsingObject( "TS", ts );
					processor.processRequest( "AppendTimeSeries", request_params );
				}
			}
			*/
			status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(
                            CommandStatusType.FAILURE,
                            "Appending results is not currently enabled.",
                            "Do not try to append results."));
		}
		
		Message.printStatus ( 2, routine,"...done processing commands from file." );
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error processing command file \"" + InputFile + "\", full path=\"" +
		    InputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new CommandException ( message );
	}
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String InputFile = props.getValue("InputFile");
    String ExpectedStatus = props.getValue("ExpectedStatus");
    //String InheritParentWorkflowProperties = props.getValue("InheritParentWorkflowProperties");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	/*
    if ( (InheritParentWorkflowProperties != null) && (InheritParentWorkflowProperties.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InheritParentWorkflowProperties=" + InheritParentWorkflowProperties );
    }*/
    if ( (ExpectedStatus != null) && (ExpectedStatus.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExpectedStatus=" + ExpectedStatus );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
