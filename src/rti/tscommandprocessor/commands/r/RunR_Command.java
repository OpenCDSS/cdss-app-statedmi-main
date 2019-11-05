// RunR_Command - This class initializes, checks, and runs the RunPython() command.

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

package rti.tscommandprocessor.commands.r;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

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
This class initializes, checks, and runs the RunR() command.
*/
public class RunR_Command extends AbstractCommand implements Command
{
	
	/**
	 * Possible values for SpecifyWorkingDirHow parameter.
	 */
	protected final String _IncludedInArguments = "IncludedInArguments";
	// TODO smalers 2019-09-21 need to evaluate if this can be done gracefully 
	//protected final String _EnvironmentVariable = "EnvironmentVariable";

/**
Constructor.
*/
public RunR_Command ()
{	super();
	setCommandName ( "RunR" );
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
{	String ScriptFile = parameters.getValue ( "ScriptFile" );
	String SetwdHow = parameters.getValue ( "SetwdHow" );
	String warning = "";
    String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ScriptFile == null) || (ScriptFile.length() == 0) ) {
        message = "The script file must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the script file." ) );
	}
	else if ( ScriptFile.indexOf("${") < 0 ) {
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
                TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ScriptFile)) );
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
            message = "The script file \"" + ScriptFile +
            "\" cannot be adjusted to an absolute path using the working directory \"" +
            working_dir + "\" and processor properties.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that the R script to run and working directory paths are compatible." ) );
		}
	}

    if ( (SetwdHow != null) && !SetwdHow.equalsIgnoreCase(_IncludedInArguments) ) {
        message = "The SetwdHow parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify as " + _IncludedInArguments) );
    }
	
	// Check for invalid parameters...
    List<String> validList = new ArrayList<String>(5);
    validList.add ( "RProgram" );
    validList.add ( "ROptions" );
    //validList.add ( "PythonPath" );
	validList.add ( "ScriptFile" );
	validList.add ( "ScriptArguments" );
	validList.add ( "SetwdHow" );
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
	return (new RunR_JDialog ( parent, this )).ok();
}

/**
Get the R program to use to execute the interpreter in command-line mode.
If the Program parameter is not specified, use defaults based on standard installation and assuming
that the program is in the path.
@param Program program to run:
If an absolute path use as is.
If blank, the R program may be in the PATH, but the R installer does not modify the PATH by default.
Therefore, search for Rscript.exe on windows and Rscript on Linux in the PATH folders.
If not found in the PATH, search in standard installation folders.
Return null if not found.
@return rProgram the name of the R program to run, either a simple filename if in the PATH,
or a path to executable (as provided, or found in the installation folder).
*/
private String getRProgram ( String Program )
{	String routine = getClass().getSimpleName() + ".getRProgram";
    String program = getCommandParameters().getValue("Program");
    if ( (program == null) || program.equals("") ) {
    	// First try to find the program in the PATH
    	String rProgram = "Rscript";
    	boolean isUnix = IOUtil.isUNIXMachine();
    	if ( !isUnix ) {
    		// Windows
    		rProgram = "Rscript.exe";
    	}
    	File f = IOUtil.findProgramInPath(rProgram);
    	if ( f != null ) {
    		// Found the program in the PATH
    		return f.getAbsolutePath();
    	}
    	else {
    		Message.printStatus(2, routine, "Did not find program \"" + rProgram + "\" in PATH.");
    	}
    	// If here, no program was found in the PATH so look in standard install locations.
    	if ( isUnix ) {
    		// Not yet supported
    		// - perhaps won't get here because it will probably be in the PATH in a standard location
    		return null;
    	}
    	else {
    		// Windows - something like:
    		//    C:\Program Files\R\R-3.6.1\i386\Rscript.exe
    		//    C:\Program Files\R\R-3.6.1\x64\Rscript.exe
    		// Focus on recent convention for R and can add other options later if necessary
    		// List the folders in "C:\Program Files\R"
    		List<File> rFolders = IOUtil.getFilesMatchingPattern("C:\\Program Files\\R", "*", false);
    		for ( File folder : rFolders ) {
    			// Try 64-bit version
    			String folderPath = folder.getAbsolutePath();
    			File f2 = new File(folderPath + "\\bin\\x64\\" + rProgram);
    			Message.printStatus(2, routine, "Checking for R executable:  " + f2.getAbsolutePath());
    			if ( f2.exists() && f2.canExecute() ) {
    				Message.printStatus(2, routine, "R executable found:  " + f2.getAbsolutePath());
    				return "\"" + f2.getAbsolutePath() + "\"";
    			}
    		}
    		// Could add 32-bit search or other path names that may have been used
    		Message.printStatus(2, routine, "Did not find program \"" + rProgram + "\" in typical install locations.");
    	}
    }
    return null;
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

	String RProgram = parameters.getValue ( "RProgram" );
	if ( (RProgram != null) && !RProgram.isEmpty() && (RProgram.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	    RProgram = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, RProgram);
	}
	String ROptions = parameters.getValue ( "ROptions" );
	if ( (ROptions != null) && !ROptions.isEmpty() && (ROptions.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	    ROptions = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ROptions);
	}
	String ScriptFile = parameters.getValue ( "ScriptFile" ); // Expanded below
	String ScriptArguments = parameters.getValue ( "ScriptArguments" );
	if ( (ScriptArguments != null) && !ScriptArguments.isEmpty() && (ScriptArguments.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	    ScriptArguments = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ScriptArguments);
	}
	String SetwdHow = parameters.getValue ( "SetwdHow" );
	//String PythonPath = parameters.getValue ( "PythonPath" );
	//if ( (PythonPath != null) && !PythonPath.isEmpty() && (PythonPath.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
	//	PythonPath = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, PythonPath);
	//}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Get the working directory from the processor that is running the commands.

	String WorkingDir = TSCommandProcessorUtil.getWorkingDir(processor);
	String ScriptFile_full = null;
	try {
		// TODO SAM 2016-09-18 Should this include the following like many other commands?
		//IOUtil.toAbsolutePath(
        ScriptFile_full = IOUtil.verifyPathForOS(IOUtil.adjustPath ( WorkingDir,
            TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, ScriptFile)));
        if ( !IOUtil.fileExists(ScriptFile_full) ) {
            message = "R script file \"" + ScriptFile_full + "\" does not exist.";
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify the location of the script file." ) );
        }
        
        // Parse out the user-specified python path
        //String [] pathParts = new String[0];
        //if ( (PythonPath != null) && !PythonPath.equals("") ) {
            //// Expand the path
            //PythonPath = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, PythonPath );
            //pathParts = PythonPath.split("[:;]");
        //}
        
		Message.printStatus ( 2, routine, "Processing R script file \"" + ScriptFile_full + "\" using R program.");

		String [] rOptions = StringUtil.toArray(StringUtil.breakStringList(ROptions, " ", StringUtil.DELIM_ALLOW_STRINGS));
		String [] scriptArgs = StringUtil.toArray(StringUtil.breakStringList(ScriptArguments, " ", StringUtil.DELIM_ALLOW_STRINGS));
		String rProgram = getRProgram(RProgram);
		if ( rProgram == null ) {
            message = "Unable to find R (Rscript) program to run in expected locations.";
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that R software is installed." ) );
		}
		else {
			warning_count = runR ( command_tag, warning_count, rProgram, rOptions,
				ScriptFile_full, scriptArgs, SetwdHow );
			Message.printStatus ( 2, routine,"...done processing R script." );
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error processing R file \"" + ScriptFile_full + "\" (" + e + ").";
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
Run the R script by making a system call to R.
@param command_tag for logging
@param warning_count count of warnings, for logging, also returned
@param rProgram the name of the R program to run, either a simple filename,
in which case the PATH will be used, or a path to an executable program.
By default Rscript/Rscript.exe is used.
@param rOptions command line options for the R program (not the script).
@param rfile R script to run, full path.
@param scriptArgs Arguments to pass to the R script (not the R program).
@param setwdHow indicates how to set the working directory (_IncludedInArguments),
if null the latter.  In the future an environment variable may be added.
@return warning_count total warning count
*/
private int runR ( String command_tag, int warning_count, String rProgram, String [] rOptions,
    String rFile, String [] scriptArgs, String setwdHow )
{   String routine = getClass().getSimpleName() + ".runR", message;
    // Length is +2 because of R program and R script
    int commandLineNum = 1 + rOptions.length + 1 + scriptArgs.length;
    String [] commandLineArray = new String[commandLineNum];
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
    int iArg = 0;
    // R program name is first command line parameter token, typically a variant of Rscript
    commandLineArray[iArg++] = rProgram;
    // R program options
    for ( int i = 0; i < rOptions.length; i++ ) {
        String opt = rOptions[i];
        if ( (opt != null) && !opt.isEmpty() && (opt.indexOf("${") >= 0) ) {
	        opt = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, opt);
	    }
        commandLineArray[iArg++] = opt;
    }
    // If requested (the default), include setwd()
    // - working directory for R is the script folder
    // - need to escape inner quotes around the folder
    // This does not work because Rscript either runs with -e or R script, but not both
    /*
   	if ( doSetwdExpr ) {
   		File f = new File(rFile);
   		if ( IOUtil.isUNIXMachine() ) {
   			commandLineArray[iArg++] = "-e 'setwd(\"" + IOUtil.toPosixPath(f.getParent()) + "\")'";
   		}
   		else {
   			commandLineArray[iArg++] = "-e \"setwd(\\\"" + IOUtil.toPosixPath(f.getParent()) + "\\\")\"";
   		}
   	}
   	*/
   	// R script to run
    commandLineArray[iArg++] = IOUtil.toPortablePath(rFile); // R script as full path
   	// R script arguments
    for ( int i = 0; i < scriptArgs.length; i++ ) {
        String arg = scriptArgs[i];
        if ( (arg != null) && !arg.isEmpty() && (arg.indexOf("${") >= 0) ) {
	        arg = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, arg);
	    }
        commandLineArray[iArg++] = arg;
    }
    int warning_level = 2;
    
    ProcessManager pm = new ProcessManager ( commandLineArray,
        0, // No timeout
        (String)null, // Exit status indicator
        false, // No command shell (just run directly) - otherwise the command hangs on the r prompt (?)
        (File)null); // Override working directory
    String commandLine = pm.getCommand();
    Message.printStatus ( 2, routine, "Running:  " + commandLine );
    
    // Add the requested path to the PYTHONPATH environment variable
    /*
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
    */
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