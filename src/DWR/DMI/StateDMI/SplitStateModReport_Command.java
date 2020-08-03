package DWR.DMI.StateDMI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
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
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;

/**
This class initializes, checks, and runs the SplitStateModReport() command.
*/
public class SplitStateModReport_Command extends AbstractCommand
implements Command, FileGenerator
{

/**
Data members used for parameter values.
*/
protected final String _Create = "Create";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

protected final String _False = "False";
protected final String _True = "True";

/**
Output files that are created by this command.
*/
private List<File> __outputFileList = null;

/**
Constructor.
*/
public SplitStateModReport_Command ()
{	super();
	setCommandName ( "SplitStateModReport" );
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
{	String ReportFile = parameters.getValue ( "ReportFile" );
    //String OutputFolder = parameters.getValue ( "OutputFolder" );
    String OutputFile = parameters.getValue ( "OutputFile" );
	String IfOutputFolderDoesNotExist = parameters.getValue ( "IfOutputFolderDoesNotExist" );
	String ListInResults = parameters.getValue ( "ListInResults" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the report file is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (ReportFile == null) || ReportFile.isEmpty() ) {
		message = "The report file must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the report file."));
	}
	if ( (IfOutputFolderDoesNotExist != null) && !IfOutputFolderDoesNotExist.equals("") ) {
		if ( !IfOutputFolderDoesNotExist.equalsIgnoreCase(_Create) && !IfOutputFolderDoesNotExist.equalsIgnoreCase(_Warn)
		    && !IfOutputFolderDoesNotExist.equalsIgnoreCase(_Fail) ) {
			message = "The IfOutputFolderDoesNotExist parameter \"" + IfOutputFolderDoesNotExist + "\" is invalid.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _Create + ", " + _Warn + " (default), or " +
					_Fail + "."));
		}
	}
	if ( (OutputFile == null) || OutputFile.isEmpty() ) {
		message = "The output file must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the output file."));
	}
    if ( (ListInResults != null) && !ListInResults.isEmpty() &&
        !ListInResults.equalsIgnoreCase(_True) &&
        !ListInResults.equalsIgnoreCase(_False) ) {
        message = "The ListInResults parameter \"" + ListInResults + "\" must be " + _True + " or " + _False + ".";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Correct the ListInResults parameter " + _True + " (default) or " + _False + "." ) );
    }
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(5);
	validList.add ( "ReportFile" );
	validList.add ( "OutputFolder" );
	validList.add ( "OutputFile" );
	validList.add ( "IfOutputFolderDoesNotExist" );
	validList.add ( "ListInResults" );
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
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new SplitStateModReport_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    if ( getOutputFileList() != null ) {
        return getOutputFileList();
    }
    else {
    	return new ArrayList<File>();
    }
}

/**
Return the output files generated by this file.  This method is used internally.
*/
private List<File> getOutputFileList ()
{
    return __outputFileList;
}

/**
Run the command.
@param command_line Command number in sequence.
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
	
    // Clear the output file list
    setOutputFileList ( null );
	
	String ReportFile = parameters.getValue ( "ReportFile" ); // Expanded below
	String OutputFolder = parameters.getValue ( "OutputFolder" ); // Expanded below
	String OutputFile = parameters.getValue ( "OutputFile" ); // Expanded below
	String IfOutputFolderDoesNotExist = parameters.getValue ( "IfOutputFolderDoesNotExist" );
	if ( (IfOutputFolderDoesNotExist == null) || IfOutputFolderDoesNotExist.equals("")) {
	    IfOutputFolderDoesNotExist = _Warn; // Default
	}
	String ListInResults = parameters.getValue ( "ListInResults" );
	boolean listInResults = true;
	if ( (ListInResults != null) && ListInResults.equalsIgnoreCase(_False) ) {
	    listInResults = false;
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Process the report file.
    String ReportFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,ReportFile)));
	String OutputFolder_full = null;
	if ( (OutputFolder != null) && !OutputFolder.isEmpty() ) {
		OutputFolder_full = IOUtil.verifyPathForOS(
			IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
				TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFolder)));
	}
	else {
		// Default to the folder for the report file
		File f = new File(ReportFile_full);
		OutputFolder_full = f.getParent();
	}
	try {
		// Make sure that the output folder exists
	    File outputFolder = new File(OutputFolder_full);
	    if ( !outputFolder.exists() ) {
	        message = "Output folder \"" + OutputFolder + "\" does not exist.";
	        if ( IfOutputFolderDoesNotExist.equalsIgnoreCase(_Create) ) {
	        	outputFolder.mkdirs();
	        }
	        else if ( IfOutputFolderDoesNotExist.equalsIgnoreCase(_Fail) ) {
	            Message.printWarning ( warning_level,
	                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
	            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the output folder exists at the time the command is run or use IfOutputFolderDoesNotExist=Create."));
	        }
	        else if ( IfOutputFolderDoesNotExist.equalsIgnoreCase(_Warn) ) {
	            Message.printWarning ( warning_level,
	                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
	            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
	                message, "Verify that the output folder exists at the time the command is run or use IfOutputFolderDoesNotExist=Create."));
	        }
	    }
	    if ( outputFolder.exists() ) {
	    	// Can split the file
	    	File reportFile = new File(ReportFile_full);
	    	String reportFileExt = IOUtil.getFileExtension(ReportFile_full);
	    	List<File> outputFileList = new ArrayList<>();
	    	// Set the string that indicates the start of a section for a node.
	    	String sectionStartString = null; // Indicates start of data section for a node ID
	    	String nodeIdString = null; // Indicates the line with the node ID
	    	if ( reportFileExt.equalsIgnoreCase("xdd") ) {
	    		sectionStartString = "Diversion Summary";
	    		nodeIdString = "STRUCTURE ID (0 = total)  :";
	    	}
	    	else if ( reportFileExt.equalsIgnoreCase("xre") ) {
	    		sectionStartString = "Reservoir Summary";
	    		nodeIdString = "RESERVOIR ID              :";
	    	}
	    	else {
	    		message = "StateMod report file extension \"" + reportFileExt + "\" is not recognized.";
	            Message.printWarning ( warning_level,
	                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
	            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
	                message, "Make sure tht the file extension is correct.  May need to enhance software to support the file."));
	    	}
	    	if ( reportFile.exists() && (sectionStartString != null) ) {
	    		// File extension allowed the section start string to be defined.
	    		PrintWriter ofp = null;  // Will be used for multiple output files
	    		BufferedReader in = new BufferedReader(new FileReader(ReportFile_full));
	    		String iline;
	    		boolean inHeader = true;  // Header is lines with # comments
	    		boolean inSectionStart = false; // In the first lines of the section
	    		List<String> headerComments = new ArrayList<>();
	    		List<String> sectionStartLines = new ArrayList<>();
	    		String nodeId = "";
	    		// Previous nodeId when checking the sectionStartString
	    		// - reservoirs have repeated sections due to accounts so need to NOT restart the section
	    		String nodeIdPrev = "";
	    		int posId, posId2;
	    		int lineCount = 0;
	    		while ( (iline = in.readLine()) != null ) {
	    			++lineCount;
	    			if ( inHeader ) {
	    				if ( iline.startsWith("#") ) {
	    					// Still in the header
	    					headerComments.add(iline);
	    				}
	    				else {
	    					// Assume out of the header
	    					// - need to process the line below
	    					inHeader = false;
	    				}
	    			}
	    			if ( !inHeader ) {
	    				// In the body of the report
	    				if ( iline.indexOf(sectionStartString) >= 0 ) {
	    					// New node section.
	    					// Because the node ID is not yet known, need to keep a list of lines until the node ID is known.
	    					// - the output may end up in the same or a different output file
	    					Message.printStatus(2, routine, "Found section start at line " + lineCount);
	    					inSectionStart = true;
	    					sectionStartLines.clear();
	    					sectionStartLines.add(iline);
	    				}
	    				else if ( (posId=iline.indexOf(nodeIdString)) >= 0 ) {
	    					// In the main part of the node section
	    					Message.printStatus(2, routine, "Found node line " + lineCount);
	    					inSectionStart = false;
	    					// Extract the node ID, for example, from the xdd:
	    					//     STRUCTURE ID (0 = total)  : 5100546           265
	    					posId2 = posId + nodeIdString.length();
	    					// Node Id should be the first token, but may be surrounded with spaces
	    					// - get the end of the input line
	    					nodeId = iline.substring(posId2).trim();
	    					// See if a space follows the node ID
	    					posId = nodeId.indexOf(" ");
	    					if ( posId > 0 ) {
	    						// Additional space-delimited tokens at the end of the line so nodeId needs to be extracted
	    						nodeId = nodeId.substring(0,posId).trim();
	    					} // else, nodeId is fine as is
	    					if ( !nodeId.equals(nodeIdPrev) ) {
	    						// Close the old file and open the new file
	    						if ( ofp != null ) {
	    							ofp.close();
	    						}
	    						// Open the output file for the node.
	    						String OutputFile_full = OutputFolder_full + File.separator +
           							TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile).replace("NODEID",nodeId);
	    						Message.printStatus(2, routine, "Opening output file:  " + OutputFile_full);
	    						ofp = new PrintWriter ( new FileOutputStream(OutputFile_full) );
	    						// If requested, add the output file to output file list.
	    						if ( listInResults ) {
	    							outputFileList.add(new File(OutputFile_full));
	    						}
	    						// Add the file header to the output file
	    						for ( String headerComment : headerComments ) {
	    							ofp.println(headerComment);
	    						}
	    					}
	    					else {
	    						// Same node as previous section, typically because of a reservoir account.
	    						// Keep using the same output file.
	    					}
	    					// Set the node ID for the next iteration
	    					nodeIdPrev = nodeId;
	    					ofp.println();
	    					// Add the section start to the output file
	    					for ( String line : sectionStartLines ) {
	    						ofp.println(line);
	    					}
	    					// Add the line that was just read
	    					ofp.println(iline);
	    				}
	    				else if ( inSectionStart ) {
	    					// Still in the section start.
	    					// - append the line until the node ID line is found
	    					sectionStartLines.add(iline);
	    				}
	    				else {
	    					// Write the line to the file
	    					if ( ofp != null ) {
	    						ofp.println(iline);
	    					}
	    					else {
	    						// Typically blank lines before section start string is detected
	    					}
	    				}
	    			}

	    		}
	    		// Close remaining open files
				if ( in != null ) {
					in.close();
				}
				if ( ofp != null ) {
					ofp.close();
				}
	    	}
	    	// Save all the output files.
			if ( listInResults ) {
				setOutputFileList ( outputFileList );
			}
	    }
	}
    catch ( Exception e ) {
		message = "Unexpected error splitting StateMod report file \"" + ReportFile_full + "\" to folder \"" +
		    OutputFolder_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "See the log file for details."));
		throw new CommandException ( message );
	}
	
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
Set the output files that are created by this command.  This is only used internally.
*/
private void setOutputFileList ( List<File> fileList )
{
    __outputFileList = fileList;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String ReportFile = parameters.getValue("ReportFile");
	String OutputFolder = parameters.getValue("OutputFolder");
	String OutputFile = parameters.getValue("OutputFile");
	String IfOutputFolderDoesNotExist = parameters.getValue("IfOutputFolderDoesNotExist");
	String ListInResults = parameters.getValue("ListInResults");
	StringBuffer b = new StringBuffer ();
	if ( (ReportFile != null) && (ReportFile.length() > 0) ) {
		b.append ( "ReportFile=\"" + ReportFile + "\"" );
	}
    if ( (OutputFolder != null) && (OutputFolder.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFolder=\"" + OutputFolder + "\"");
    }
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFile=\"" + OutputFile + "\"");
    }
	if ( (IfOutputFolderDoesNotExist != null) && (IfOutputFolderDoesNotExist.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfOutputFolderDoesNotExist=" + IfOutputFolderDoesNotExist );
	}
    if ( (ListInResults != null) && (ListInResults.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append(",");
        }
        b.append ( "ListInResults=" + ListInResults );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}