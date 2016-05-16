package rti.tscommandprocessor.commands.util;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
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
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the CompareFiles() command.
*/
public class CompareFiles_Command extends AbstractCommand
implements Command
{

/**
Data members used for parameter values (these have been replaced with _Warn, etc. instead).
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Data members used for IfDifferent and IfSame parameters.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.
*/
public CompareFiles_Command ()
{	super();
	setCommandName ( "CompareFiles" );
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
{	String InputFile1 = parameters.getValue ( "InputFile1" );
	String InputFile2 = parameters.getValue ( "InputFile2" );
	String MatchCase = parameters.getValue ( "MatchCase" );
	String IgnoreWhitespace = parameters.getValue ( "IgnoreWhitespace" );
	String AllowedDiff = parameters.getValue ( "AllowedDiff" );
	String IfDifferent = parameters.getValue ( "IfDifferent" );
	String IfSame = parameters.getValue ( "IfSame" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the parent directories or files is not checked
	// because the files may be created dynamically after the command is edited.

	if ( (InputFile1 == null) || (InputFile1.length() == 0) ) {
		message = "The first input file to compare must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the first file name."));
	}
	if ( (InputFile2 == null) || (InputFile2.length() == 0) ) {
		message = "The second input file to compare must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the second file name."));
	}
    if ( (MatchCase != null) && !MatchCase.equals("") && !MatchCase.equalsIgnoreCase(_False) &&
        !MatchCase.equalsIgnoreCase(_True) ) {
        message = "The MatchCase parameter \"" + MatchCase + "\" is not a valid value.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _False + " or " + _True + " (default)"));
    }
	if ( (IgnoreWhitespace != null) && !IgnoreWhitespace.equals("") && !IgnoreWhitespace.equalsIgnoreCase(_False) &&
		!IgnoreWhitespace.equalsIgnoreCase(_True) ) {
		message = "The IgnoreWhitespace parameter \"" + IgnoreWhitespace + "\" is not a valid value.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the parameter as " + _False + " (default) or " + _True ));
	}
    if ( (AllowedDiff != null) && !AllowedDiff.equals("") && !StringUtil.isInteger(AllowedDiff) ) {
            message = "The number of allowed differences \"" + AllowedDiff + "\" is invalid.";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                     message, "Specify the parameter as an integer."));
    }
	if ( (IfDifferent != null) && !IfDifferent.equals("") && !IfDifferent.equalsIgnoreCase(_Ignore) &&
		!IfDifferent.equalsIgnoreCase(_Warn) && !IfDifferent.equalsIgnoreCase(_Fail) ) {
			message = "The IfDifferent parameter \"" + IfDifferent + "\" is not a valid value.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parameter as " + _Ignore + " (default), " +
						_Warn + ", or " + _Fail + "."));
	}
	if ( (IfSame != null) && !IfSame.equals("") && !IfSame.equalsIgnoreCase(_Ignore) &&
		!IfSame.equalsIgnoreCase(_Warn) && !IfSame.equalsIgnoreCase(_Fail) ) {
		message = "The IfSame parameter \"" + IfSame + "\" is not a valid value.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _Ignore + " (default), " +
					_Warn + ", or " + _Fail + "."));
	}
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(8);
	validList.add ( "InputFile1" );
	validList.add ( "InputFile2" );
	validList.add ( "CommentLineChar" );
	validList.add ( "MatchCase" );
	validList.add ( "IgnoreWhitespace" );
	validList.add ( "AllowedDiff" );
	validList.add ( "IfDifferent" );
	validList.add ( "IfSame" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	Prop prop = IOUtil.getProp("DiffProgram");
	String diffProgram = null;
	if ( prop != null ) {
		diffProgram = prop.getValue();
	}
	return (new CompareFiles_JDialog ( parent, this, diffProgram )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void parseCommand ( String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = "CompareFiles_Command.parseCommand", message;

	List<String> tokens = StringUtil.breakStringList ( command, "()", StringUtil.DELIM_SKIP_BLANKS );

	CommandStatus status = getCommandStatus();
	if ( (tokens == null) ) { //|| tokens.size() < 2 ) {}
		message = "Invalid syntax for \"" + command + "\".  Expecting CompareFiles(...).";
		Message.printWarning ( warning_level, routine, message);
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report the problem to support."));
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the command...
	if ( tokens.size() > 1 ) {
		try {
		    setCommandParameters ( PropList.parse ( Prop.SET_FROM_PERSISTENT, tokens.get(1), routine,"," ) );
		}
		catch ( Exception e ) {
			message = "Invalid syntax for \"" + command + "\".  Expecting CompareFiles(...).";
			Message.printWarning ( warning_level, routine, message);
			status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report the problem to support."));
			throw new InvalidCommandSyntaxException ( message );
		}
	}
	// Update old parameter names to new
	// Change WarnIfDifferent=True to IfDifferent=Warn
	// Change WarnIfSame=True to IfSame=Warn
	PropList props = getCommandParameters();
	String propValue = props.getValue ( "WarnIfDifferent" );
	if ( propValue != null ) {
		if ( propValue.equalsIgnoreCase(_True) ) {
			props.set("IfDifferent",_Warn);
		}
		props.unSet("WarnIfDifferent");
	}
	propValue = props.getValue ( "WarnIfSame" );
	if ( propValue != null ) {
		if ( propValue.equalsIgnoreCase(_True) ) {
			props.set("IfSame",_Warn);
		}
		props.unSet("WarnIfSame");
	}
}

/**
Read a line from a file.  Skip over comments until the next non-comment line is found.
@param in BufferedReader for open file to read.
@param CommentLineChar character at start of line that indicates comment line.
@param ignoreWhitespace if true, trim the lines.
@return the next line from the file, or null if at the end.
*/
private String readLine ( BufferedReader in, String CommentLineChar, boolean ignoreWhitespace )
{	String iline;
	int commentCount = 0;
	while ( true ) {
		// Read until a non-comment line is found
		try {
		    iline = in.readLine ();
		}
		catch ( Exception e ) {
			return null;
		}
		if ( iline == null ) {
			return null;
		}
		// check for comments
		else if ( (iline.length() > 0) && (CommentLineChar.indexOf(iline.charAt(0)) >= 0) ) {
			++commentCount;
			continue;
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printDebug (1, "", "Skipped " + commentCount + " comments before getting to data line" );
			}
			if ( ignoreWhitespace ) {
				return iline.trim();
			}
			else {
				return iline;
			}
		}
	}
}

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	int dl = 1;
	
	PropList parameters = getCommandParameters();
	
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
	
	String InputFile1 = parameters.getValue ( "InputFile1" );
	String InputFile2 = parameters.getValue ( "InputFile2" );
	String CommentLineChar = parameters.getValue ( "CommentLineChar" );
	String MatchCase = parameters.getValue ( "MatchCase" );
    boolean MatchCase_boolean = true; // Default
    if ( (MatchCase != null) && MatchCase.equalsIgnoreCase(_False)) {
        MatchCase_boolean = false;
    }
    String IgnoreWhitespace = parameters.getValue ( "IgnoreWhitespace" );
	boolean IgnoreWhitespace_boolean = false; // Default
	if ( (IgnoreWhitespace != null) && IgnoreWhitespace.equalsIgnoreCase(_True)) {
		IgnoreWhitespace_boolean = true;
	}
	String AllowedDiff = parameters.getValue ( "AllowedDiff" );
	int AllowedDiff_int = 0;
	if ( StringUtil.isInteger(AllowedDiff) ) {
	    AllowedDiff_int = Integer.parseInt(AllowedDiff);
	}
	if ( (CommentLineChar == null) || CommentLineChar.equals("") ) {
	    CommentLineChar = "#";
	}
	String IfDifferent = parameters.getValue ( "IfDifferent" );
	CommandStatusType IfDifferent_CommandStatusType = CommandStatusType.UNKNOWN;
	if ( IfDifferent == null ) {
		IfDifferent = _Ignore; // default
	}
	else {
		if ( !IfDifferent.equalsIgnoreCase(_Ignore) ) {
			IfDifferent_CommandStatusType = CommandStatusType.parse(IfDifferent);
		}
	}
	String IfSame = parameters.getValue ( "IfSame" );
	CommandStatusType IfSame_CommandStatusType = CommandStatusType.UNKNOWN;
	if ( IfSame == null ) {
		IfSame = _Ignore; // default
	}
	else {
		if ( !IfSame.equalsIgnoreCase(_Ignore) ) {
			IfSame_CommandStatusType = CommandStatusType.parse(IfSame);
		}
	}
	int diff_count = 0; // Number of lines that are different

	String InputFile1_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                    TSCommandProcessorUtil.expandParameterValue(processor, this, InputFile1) ) );
	String InputFile2_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                    TSCommandProcessorUtil.expandParameterValue(processor, this, InputFile2) ) );
	if ( !IOUtil.fileExists(InputFile1_full) ) {
		message = "First input file \"" + InputFile1_full + "\" does not exist.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the file exists at the time the command is run."));
	}
	if ( !IOUtil.fileExists(InputFile2_full) ) {
		message = "Second input file \"" + InputFile2_full + "\" does not exist.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the file exists at the time the command is run."));
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	int lineCountCompared = 0;
	try {
	    // Open the files...
		BufferedReader in1 = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(InputFile1_full)));
		BufferedReader in2 = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(InputFile2_full)));
		// Loop through the files, comparing non-comment lines...
		String iline1, iline2;
		while ( true ) {
			// The following will discard comments and only return non-comment lines
			// Therefore comparisons are made on chunks of non-comment lines.
			iline1 = readLine ( in1, CommentLineChar, IgnoreWhitespace_boolean );
			iline2 = readLine ( in2, CommentLineChar, IgnoreWhitespace_boolean );
			if ( (iline1 == null) && (iline2 == null) ) {
				// both are done at the same time...
				break;
			}
			// TODO SAM 2006-04-20 The following needs to handle comments at the end...
			if ( (iline1 == null) && (iline2 != null) ) {
				// First file is done (second is not) so files are different...
				++diff_count;
				break;
			}
			if ( (iline2 == null) && (iline1 != null) ) {
				// Second file is done (first is not) so files are different...
				++diff_count;
				break;
			}
			++lineCountCompared;
			if ( MatchCase_boolean ) {
    			if ( !iline1.equals(iline2) ) {
    				++diff_count;
    			}
			}
			else {
			    if ( !iline1.equalsIgnoreCase(iline2) ) {
                    ++diff_count;
                }
			}
			if ( Message.isDebugOn ) {
				Message.printDebug (dl,routine,"Compared:\n\"" + iline1 + "\"\n\"" + iline2 + "\"\nDiffCount=" +
						diff_count );
			}
		}
		in1.close();
		in2.close();
		if ( lineCountCompared == 0 ) {
			lineCountCompared = 1; // to avoid divide by zero below.
		}
		Message.printStatus ( 2, routine, "There are " + diff_count + " lines that are different, " +
			StringUtil.formatString(100.0*(double)diff_count/(double)lineCountCompared, "%.2f") +
			"% (compared " + lineCountCompared + " lines).");
	}
	catch ( Exception e ) {
		message = "Unexpected error comparing files (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file for details."));
		throw new CommandException ( message );
	}
	if ( (diff_count > AllowedDiff_int) && ((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
		(IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
		message = "" + diff_count + " lines were different, " +
			StringUtil.formatString(100.0*(double)diff_count/(double)lineCountCompared, "%.2f") +
			"% (compared " + lineCountCompared + " lines).";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag( command_tag,++warning_count),
		routine, message );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfDifferent_CommandStatusType,
					message, "Check files because difference is not expected.") );
		throw new CommandException ( message );
	}
	if ( (diff_count == 0) && ((IfSame_CommandStatusType == CommandStatusType.WARNING) ||
			(IfSame_CommandStatusType == CommandStatusType.FAILURE))) {
		message = "No lines were different (the files are the same).";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag( command_tag,++warning_count),
		routine, message );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(IfSame_CommandStatusType,
					message, "Check files because match is not expected.") );
		throw new CommandException ( message );
	}
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String InputFile1 = parameters.getValue("InputFile1");
	String InputFile2 = parameters.getValue("InputFile2");
	String CommentLineChar = parameters.getValue("CommentLineChar");
	String MatchCase = parameters.getValue("MatchCase");
	String IgnoreWhitespace = parameters.getValue("IgnoreWhitespace");
	String AllowedDiff = parameters.getValue("AllowedDiff");
	String IfDifferent = parameters.getValue("IfDifferent");
	String IfSame = parameters.getValue("IfSame");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile1 != null) && (InputFile1.length() > 0) ) {
		b.append ( "InputFile1=\"" + InputFile1 + "\"" );
	}
	if ( (InputFile2 != null) && (InputFile2.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile2=\"" + InputFile2 + "\"" );
	}
    if ( (CommentLineChar != null) && (CommentLineChar.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CommentLineChar=\"" + CommentLineChar + "\"" );
    }
    if ( (MatchCase != null) && (MatchCase.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MatchCase=" + MatchCase );
    }
    if ( (IgnoreWhitespace != null) && (IgnoreWhitespace.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IgnoreWhitespace=" + IgnoreWhitespace );
    }
    if ( (AllowedDiff != null) && (AllowedDiff.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "AllowedDiff=\"" + AllowedDiff + "\"" );
    }
	if ( (IfDifferent != null) && (IfDifferent.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfDifferent=" + IfDifferent );
	}
	if ( (IfSame != null) && (IfSame.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfSame=" + IfSame );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}