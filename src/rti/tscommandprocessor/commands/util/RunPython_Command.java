// RunPython_Command - This class initializes, checks, and runs the RunPython() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package rti.tscommandprocessor.commands.util;

import java.io.File;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

/*
import org.python.util.PythonInterpreter; 
import org.python.core.PyException;
import org.python.core.PySyntaxError;
import org.python.core.PySystemState;
*/

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.PropList;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

/**
This class initializes, checks, and runs the RunPython() command.
*/
public class RunPython_Command extends AbstractCommand implements Command
{

/**
Interpreter parameter value indicating that command-line IronPython should be called to run script.
*/
protected String _IronPython = "IronPython";

/**
Interpreter parameter value indicating that command-line Jython should be called to run script.
*/
protected String _Jython = "Jython";

/**
Interpreter parameter value indicating that embedded Jython should be called to run script.
*/
protected String _JythonEmbedded = "JythonEmbedded";
    
/**
Interpreter parameter value indicating that command-line Python should be called to run script.
*/
protected String _Python = "Python";
    
/**
Indicate whether Jython has been initialized.
*/
private static boolean __Jython_initialized = false;

/**
Constructor.
*/
public RunPython_Command ()
{	super();
	setCommandName ( "RunPython" );
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
{	String InputFile = parameters.getValue ( "InputFile" );
    String Interpreter = parameters.getValue ( "Interpreter" );
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
	else if ( InputFile.indexOf("${") < 0 ) {
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
                    message, "Software error - report problem to support." ) );
		}
	
		try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath ( working_dir,
                TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, InputFile)) );
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
            "\" cannot be adjusted to an absolute path using the working directory \"" +
            working_dir + "\" and processor properties.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that command file to run and working directory paths are compatible." ) );
		}
	}
	
    if ( (Interpreter == null) || (Interpreter.length() == 0) ) {
        message = "The interpreter is not specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an interpreter to use, one of " + _Python + " or " + _Jython ) );
    }
    else if ( !Interpreter.equalsIgnoreCase(_Python) && !Interpreter.equalsIgnoreCase(_Jython) &&
            !Interpreter.equalsIgnoreCase(_JythonEmbedded) && !Interpreter.equalsIgnoreCase(_IronPython) ) {
        message = "The interpreter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an interpreter to use, one of " + _IronPython + ", " +
                        _Python + ", " + _Jython + ", or " + _JythonEmbedded ) );
    }

	// Check for invalid parameters...
    List<String> validList = new ArrayList<String>(5);
    validList.add ( "Interpreter" );
    validList.add ( "Program" );
    validList.add ( "PythonPath" );
	validList.add ( "Arguments" );
	validList.add ( "InputFile" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
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
	return (new RunPython_JDialog ( parent, this )).ok();
}

/**
Get the Python program to use to execute the interpreter in command-line mode.
If the Program parameter is not specified, use defaults based on standard installation and assuming
that the program is in the path.
*/
private String getPythonProgram ( String interpreter )
{
    String program = getCommandParameters().getValue("Program");
    if ( (program == null) || program.equals("") ) {
        // Return the default for the interpreter
        if ( interpreter.equalsIgnoreCase(_IronPython) ) {
            return "ipy";
        }
        else if ( interpreter.equalsIgnoreCase(_Jython) ) {
            return "jython"; // Has been a *.bat so may need to specify program as jython.bat on cygwin
        }
        else if ( interpreter.equalsIgnoreCase(_Python) ) {
            return "python";
        }
        else {
            // Should not happen...
            throw new InvalidParameterException ( "Interpreter \"" + interpreter + "\" not supported." );
        }
    }
    else {
        // Return the program from the parameter, expanded for properties and special characters
        return TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, program);
    }
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	
	CommandProcessor processor = getCommandProcessor();
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
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

	String InputFile = parameters.getValue ( "InputFile" ); // Expanded below
	String Arguments = parameters.getValue ( "Arguments" );
	if ( (Arguments != null) && !Arguments.isEmpty() && (Arguments.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	    Arguments = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, Arguments);
	}
	String Interpreter = parameters.getValue ( "Interpreter" );
	if ( (Interpreter != null) && !Interpreter.isEmpty() && (Interpreter.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	    Interpreter = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, Interpreter);
	}
	String PythonPath = parameters.getValue ( "PythonPath" );
	if ( (PythonPath != null) && !PythonPath.isEmpty() && (PythonPath.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		PythonPath = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, PythonPath);
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Get the working directory from the processor that is running the commands.

	String WorkingDir = TSCommandProcessorUtil.getWorkingDir(processor);
	String InputFile_full = null;
	try {
		// TODO SAM 2016-09-18 Should this include the following like many other commands?
		//IOUtil.toAbsolutePath(
        InputFile_full = IOUtil.verifyPathForOS(IOUtil.adjustPath ( WorkingDir,
            TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, InputFile)));
        if ( !IOUtil.fileExists(InputFile_full) ) {
            message = "Python script file \"" + InputFile_full + "\" does not exist.";
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify the location of the script file." ) );
        }
        
        // Parse out the user-specified python path
        String [] pathParts = new String[0];
        if ( (PythonPath != null) && !PythonPath.equals("") ) {
            // Expand the path
            PythonPath = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, PythonPath );
            pathParts = PythonPath.split("[:;]");
        }
        
		Message.printStatus ( 2, routine,
		"Processing Python script file \"" + InputFile_full + "\" using " + Interpreter + " interpreter.");

		String [] args = StringUtil.toArray(StringUtil.breakStringList(Arguments, " ", StringUtil.DELIM_ALLOW_STRINGS));
        if ( Interpreter.equalsIgnoreCase(_JythonEmbedded) ) {
            runJythonEmbedded ( command_tag, InputFile_full, args, WorkingDir, pathParts );
            Message.printStatus ( 2, routine,"...done processing Python file." );
        }
        else {
		    warning_count = runPython ( command_tag, warning_count, getPythonProgram(Interpreter),
	            InputFile_full, args, WorkingDir, pathParts );
		    Message.printStatus ( 2, routine,"...done processing Python file." );
		}
	}
	/*
    catch ( PySyntaxError e ) {
        message = "Syntax error in Python file \"" + InputFile_full + "\" (" + e + ").";
        Message.printWarning ( 3, routine, e );
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify Python script.  See the log file for Python error and output messages."));
        throw new CommandException ( message );
    }
    catch ( PyException e ) {
        message = "Python error processing Python file \"" + InputFile_full + "\" (" + e + ").";
        Message.printWarning ( 3, routine, e );
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify Python script.  See the log file for Python error and output messages."));
        throw new CommandException ( message );
    }
    */
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error processing Python file \"" + InputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),	routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details." ) );
		throw new CommandException ( message );
	}
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Run the Python script using the Jython interpreter.
@param command_tag Command tag for logging.
@param pyfile Python file to run.
@param args List of command line arguments for script.
@param workingDir The working directory in which to run the script.
@param pathParts the folders to append to the normal path, to find library *.py files.
*/
private void runJythonEmbedded ( String command_tag, String pyfile, String [] args, String workingDir,
        String[] pathParts )
//throws PyException, Exception
throws Exception
{   String routine = "RunPython.runJythonEmbedded";
	Message.printWarning(3, routine, "Jython is not supported in StateDMI.");
	return;
	/*
    // Only need to do this once to initialize the default state.
    if ( !__Jython_initialized ) {
        // This passes to the interpreter the environment information from the application
        // Do not set the script name because that may cause confusion.  It will be set below
        // with the command line arguments.
        Properties postProperties = new Properties();
        // TODO SAM 2008-06-25 Change to use a folder in the user's home once TSTool
        // setup is reconfigured to have personal files during install.
        // Use a cachedir in a temporary directory that should be visible and write-able for the user.
        String cachedir = System.getProperty("java.io.tmpdir") +
            System.getProperty("file.separator") + System.getProperty("user.name") + "-jythoncache";
        Message.printStatus( 2, routine, "Initializing Jython interpreter with python.cachdir=\"" +
                cachedir + "\"" );
        postProperties.setProperty ( "python.cachedir", cachedir );
        PythonInterpreter.initialize(System.getProperties(), postProperties, null );
    }
    // Create a new system state for this particular script.
    // The general properties will be inherited from the system properties passed in above.
    // specify new script name and command line parameters based on this specific call.
    PySystemState state = new PySystemState();
    for ( int i = 0; i < pathParts.length; i++ ) {
        Message.printStatus(2, routine,
            "Adding the following to Python path for state: \"" + pathParts[i] + "\"" );
        state.path.add( pathParts[i] );
    }
    
    String [] argv = new String[1 + args.length];
    argv[0] = pyfile;   // Script name
    for ( int i = 1; i <= args.length; i++ ) {
        // Arguments to the script...
        argv[i] = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, args[i - 1] );
    }
    state.argv.addAll(Arrays.asList(argv));
    PythonInterpreter interp = new PythonInterpreter(null,state);

    // Tell Jython where to send its output so it can be captured and logged.
    // Use the same writer for output and error so that it is intermingled in the correct
    // order in output.
    StringWriter out = new StringWriter();
    interp.setOut( out );
    interp.setErr( out );
    // For troubleshooting
    interp.exec ( "import sys" );
    interp.exec ( "import os" );
    interp.exec ( "print '\"sys.argv\"=' + str(sys.argv)" );
    interp.exec ( "print '\"os.getcwd()\"=\"' + os.getcwd() + '\"'" );
    // Now execute the script.  An error will result in PyException being thrown and
    // caught in the calling code.
    try {
        interp.execfile ( pyfile );
    }
    catch ( Exception e ) {
        // Make sure to capture the Python output...
        Message.printStatus( 2, routine, "Output and error from the Jython script follows:" );
        Message.printStatus(2, routine, out.toString());
        // Rethrow the exception
        throw ( e );
    }
    Message.printStatus( 2, routine, "Output and error from the Jython script follows:" );
    Message.printStatus(2, routine, out.toString());
    */
}

/**
Run the Python script by making a system call to Python.
@param pythonProgram the name of the python program to run, either a simple filename, in which case the PATH
will be used to find the program, or a full path to the program.
@param pyfile Python file to run.
@param args Arguments to pass to Python.
@param WorkingDir The working directory in which to run the script.
@param pathParts the folders to append to the normal path, to find library *.py files.
*/
private int runPython ( String command_tag, int warning_count, String pythonProgram, 
        String pyfile, String [] args, String WorkingDir, String[] pathParts )
{   String routine = "RynPython_Command.runPython", message;
    // Expand ${} parameters...
    String [] commandLineArray = new String[args.length + 2];
    /*
    // Use a command line because arguments are one string
    if ( pythonProgram.indexOf(" ") > 0 ) {
        // Program has spaces so surround the program name with quotes
        commandLine = "\"" + pythonProgram + "\" \"" + pyfile + "\" " + args;
    }
    else {
        commandLine = pythonProgram + " \"" + pyfile + "\" " + args;
    }
    */
    commandLineArray[0] = pythonProgram;
    commandLineArray[1] = pyfile;
    for ( int i = 0; i < args.length; i++ ) {
        commandLineArray[2+i] = args[i];
    }
    int warning_level = 2;
    
    ProcessManager pm = new ProcessManager ( commandLineArray,
        0, // No timeout
        (String)null, // Exit status indicator
        false, // No command shell (just run directly) - otherwise the command hangs on the python prompt
        (File)null); // Override working directory
    String commandLine = pm.getCommand();
    Message.printStatus ( 2, routine, "Running:  " + commandLine );
    
    // Add the requested path to the PYTHONPATH environment variable
    if ( pathParts.length > 0 ) {
        StringBuffer path = new StringBuffer();
        for ( int i = 0; i < pathParts.length; i++ ) {
            Message.printStatus(2, routine,
                "Adding the following to Python path: \"" + pathParts[i] + "\"" );
            // Put the delimiter in front because parts will be appended to the existing path
            path.append ( System.getProperty("path.separator") + pathParts[i] );
        }
        HashMap<String,String> envMap = new HashMap<String,String>();
        envMap.put ( "PYTHONPATH", "+" + path );
        pm.setEnvironment ( envMap );
    }
    pm.saveOutput ( true );
    pm.run();
    CommandStatus status = getCommandStatus();
    if ( pm.getExitStatus() == 996 ) {
        message = "Program \"" + commandLine + "\" timed out.  Full output may not be available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Check the log file and verify running the program on the command " +
                    		"line before running in TSTool."));
    }
    else if ( pm.getExitStatus() > 0 ) {
        message = "Program \"" + commandLine + "\" exited with status " + pm.getExitStatus() +
        ".  Full output may not be available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Check the log file and verify running the program on the command " +
                    		"line before running in TSTool."));
    }
    // Echo the output to the log file.
    List<String> output = pm.getOutputList();
    int size = 0;
    if ( output != null ) {
        size = output.size();
    }
    for ( int i = 0; i < size; i++ ) {
        Message.printStatus(2, routine, "Program output:  " + output.get(i));
    }
    return warning_count;
}

}