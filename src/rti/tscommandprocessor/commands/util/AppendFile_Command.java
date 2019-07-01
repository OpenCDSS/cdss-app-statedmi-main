// AppendFile_Command - This class initializes, checks, and runs the AppendFile() command.

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;

/**
This class initializes, checks, and runs the AppendFile() command.
*/
public class AppendFile_Command extends AbstractCommand
implements Command, FileGenerator
{

/**
Data members used for parameter values.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public AppendFile_Command ()
{	super();
	setCommandName ( "AppendFile" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String InputFile = parameters.getValue ( "InputFile" );
    String OutputFile = parameters.getValue ( "OutputFile" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the file to append is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (InputFile == null) || InputFile.isEmpty() ) {
		message = "The input file must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the input file."));
	}
    if ( (OutputFile == null) || OutputFile.isEmpty() ) {
        message = "The output file must be specified.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output file."));
    }
	if ( (IfNotFound != null) && !IfNotFound.isEmpty() ) {
		if ( !IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Warn)
		    && !IfNotFound.equalsIgnoreCase(_Fail) ) {
			message = "The IfNotFound parameter \"" + IfNotFound + "\" is invalid.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _Ignore + ", " + _Warn + " (default), or " +
					_Fail + "."));
		}
	}
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(6);
	validList.add ( "InputFile" );
	validList.add ( "OutputFile" );
	validList.add ( "IncludeText" );
	validList.add ( "ExcludeText" );
	validList.add ( "Newline" );
	validList.add ( "IfNotFound" );
	warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
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
	return (new AppendFile_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    List<File> list = new ArrayList<File>(1);
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
    }
    return list;
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
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
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
		status.clearLog(CommandPhaseType.RUN);
	}
	
    // Clear the output file
    setOutputFile ( null );
	
	String InputFile = parameters.getValue ( "InputFile" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String IncludeText = parameters.getValue ( "IncludeText" );
	boolean doIncludeText = false;
	if ( (IncludeText != null) && !IncludeText.equals("") ) {
	    doIncludeText = true;
	}
    String ExcludeText = parameters.getValue ( "ExcludeText" );
    boolean doExcludeText = false;
    if ( (ExcludeText != null) && !ExcludeText.equals("") ) {
        doExcludeText = true;
    }
    String Newline = parameters.getValue ( "Newline" );
    String nl = System.getProperty("line.separator"); // Default is native computer newline
    if ( (Newline != null) && !Newline.isEmpty() ) {
    	// Replace literal string with internal representation
    	nl = nl.replace("\\r", "\r" );
    	nl = nl.replace("\\n", "\n" );
    }
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( (IfNotFound == null) || IfNotFound.equals("")) {
	    IfNotFound = _Warn; // Default
	}

	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
        	TSCommandProcessorUtil.expandParameterValue(processor,this,InputFile) ) );
	// Expand to a list of files...
	File f = new File(InputFile_full);
	String ext = null;
	List<File> fileList = new ArrayList<File>();
	if ( InputFile_full.indexOf("*") < 0 ) {
	    // Processing a single file
	    fileList.add(new File(InputFile_full));
	}
	else if ( f.getName().equals("*") ) {
	    // Process all files in folder
	    fileList = Arrays.asList(f.getParentFile().listFiles());
	}
	else if ( f.getName().startsWith("*.") ) {
	    // Process all files in the folder with the matching extension
	    ext = IOUtil.getFileExtension(f.getName());
	    // TODO SAM 2016-02-08 Need to enable parameter for case
	    fileList = IOUtil.getFilesMatchingPattern(f.getParent(),ext,false);
	}
	if ( fileList.size() == 0 ) {
	    message = "Unable to match any files using InputFile=\"" + InputFile + "\"";
	    if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the input file(s) exist(s) at the time the command is run."));
        }
        else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
                message, "Verify that the input file(s) exist(s) at the time the command is run."));
        }
	}
	for ( File file : fileList ) {
    	if ( !file.exists() ) {
            message = "Input file to append \"" + file + "\" does not exist.";
            if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the file exists at the time the command is run."));
            }
            else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
                    message, "Verify that the file exists at the time the command is run."));
            }
            else {
                Message.printStatus( 2, routine, message + "  Ignoring.");
            }
    	}
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Process the files.  Each input file is opened to scan the file.  The output file is opened once in append mode.

	String OutputFile_full = IOUtil.verifyPathForOS(
	    IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
	        TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile) ) );
	PrintWriter fout = null;
	try {
	    fout = new PrintWriter ( new FileOutputStream( OutputFile_full, true ) );
	}
	catch ( Exception e ) {
	    message = "Error opening the output file (" + e + ").";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
        throw new CommandException ( message );
	}

    String line;
    boolean includeLine;
    int fileCount = 0;
	for ( File file : fileList ) {
	    BufferedReader in = null;
	    message = "Processing file \"" + file.getName() + "\"";
	    notifyCommandProgressListeners ( fileCount++, fileList.size(), (float)-1.0, message );
	    try {
	        in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( file.getPath() )) );
	        // Read lines and check against the pattern to match.  Default is regex syntax
	        while( (line = in.readLine()) != null ) {
	            includeLine = true;
	            if ( doIncludeText ) {
	                if ( line.matches(IncludeText) ) {
	                    // OK to append to output
	                    includeLine = true;
	                }
	                else {
	                    includeLine = false;
	                }
	            }
                if ( doExcludeText ) {
                    if ( line.matches(ExcludeText) ) {
                        // Skip
                        includeLine = false;
                    }
                    else {
                        includeLine = true;
                    }
                }
	            if ( includeLine ) {
	                fout.write(line + nl);
	            }
	        }
	    }
        catch ( Exception e ) {
    		message = "Unexpected error appending file \"" + InputFile_full + "\" to \"" +
    		    OutputFile_full + "\" (" + e + ").";
    		Message.printWarning ( warning_level, 
    		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
    		Message.printWarning ( 3, routine, e );
    		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file for details."));
    		throw new CommandException ( message );
    	}
        finally {
            try {
                in.close();
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
	}
	
	// Close the output file
	fout.close();
    // Save the output file name...
    setOutputFile ( new File(OutputFile_full));
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
        throw new CommandWarningException ( message );
    }

	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
    __OutputFile_File = file;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String InputFile = parameters.getValue("InputFile");
	String OutputFile = parameters.getValue("OutputFile");
	String IncludeText = parameters.getValue("IncludeText");
	String ExcludeText = parameters.getValue("ExcludeText");
	String Newline = parameters.getValue("Newline");
	String IfNotFound = parameters.getValue("IfNotFound");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFile=\"" + OutputFile + "\"");
    }
    if ( (IncludeText != null) && (IncludeText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeText=\"" + IncludeText + "\"" );
    }
    if ( (ExcludeText != null) && (ExcludeText.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeText=\"" + ExcludeText + "\"" );
    }
    if ( (Newline != null) && (Newline.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Newline=\"" + Newline + "\"" );
    }
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
