// NewExcelWorkbook_Command - This class initializes, checks, and runs the NewExcelWorkbook() command.

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

package rti.tscommandprocessor.commands.spreadsheet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
This class initializes, checks, and runs the NewExcelWorkbook() command.
*/
public class NewExcelWorkbook_Command extends AbstractCommand
implements Command, FileGenerator
{

/**
Data members used for IfFound parameter values.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Possible values for WriteAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public NewExcelWorkbook_Command ()
{	super();
	setCommandName ( "NewExcelWorkbook" );
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
{	String OutputFile = parameters.getValue ( "OutputFile" );
	String IfFound = parameters.getValue ( "IfFound" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the file to append is not checked during initialization
	// because files may be created dynamically at runtime.

    if ( (OutputFile == null) || OutputFile.isEmpty() ) {
        message = "The Excel workbook (output file) must be specified.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output file."));
    }
	if ( (IfFound != null) && !IfFound.isEmpty() ) {
		if ( !IfFound.equalsIgnoreCase(_Ignore) && !IfFound.equalsIgnoreCase(_Warn)
		    && !IfFound.equalsIgnoreCase(_Fail) ) {
			message = "The IfFound parameter \"" + IfFound + "\" is invalid.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the parameter as " + _Ignore + ", " + _Warn + " (default), or " +
					_Fail + "."));
		}
	}
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.isEmpty() ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(4);
	validList.add ( "OutputFile" );
	validList.add ( "Worksheets" );
	validList.add ( "IfFound" );
	validList.add ( "KeepOpen" );
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
	return (new NewExcelWorkbook_JDialog ( parent, this )).ok();
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
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
    return __OutputFile_File;
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
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
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
	
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Worksheets = parameters.getValue ( "Worksheets" );
	String [] worksheets = null;
	if ( (Worksheets != null) && !Worksheets.isEmpty() ) {
	    if ( (Worksheets != null) && !Worksheets.isEmpty() && (commandPhase == CommandPhaseType.RUN) && Worksheets.indexOf("${") >= 0 ) {
	    	Worksheets = TSCommandProcessorUtil.expandParameterValue(processor, this, Worksheets);
	    }
	    worksheets = Worksheets.trim().split(",");
	}
	String IfFound = parameters.getValue ( "IfFound" );
	if ( (IfFound == null) || IfFound.equals("")) {
	    IfFound = _Warn; // Default
	}
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase("True") ) {
        keepOpen = true;
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	// Create the workbook.

	String OutputFile_full = IOUtil.verifyPathForOS(
	    IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
	        TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)) );
	FileOutputStream fout = null;
    Workbook wb = null;
	try {
	    if ( OutputFile_full.toLowerCase().endsWith(".xls") ) {
	        wb = new HSSFWorkbook();
	    }
	    else if ( OutputFile_full.toLowerCase().endsWith(".xlsx") ) {
	        wb = new XSSFWorkbook();
	    }
	    else {
	        message = "Unknown Excel file extension for \"" + OutputFile_full + "\"";
	        Message.printWarning ( 3, routine, message );
	        status.addToLog(CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "See the log file for details."));
	    }
	}
	catch ( Exception e ) {
	    message = "Error creating the Workbook object (" + e + ").";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
	}

    try {
        if ( wb != null ) {
            // Create worksheets if requested
            if ( (worksheets != null) && (worksheets.length != 0) ) {
                String worksheet = null;
                for ( int i = 0; i < worksheets.length; i++ ) {
                    try {
                        worksheet = WorkbookUtil.createSafeSheetName(worksheets[i].trim());
                        wb.createSheet(worksheet);
                    }
                    catch ( Exception e ) {
                        message = "Error creating Excel worksheet \"" + worksheets[i] + "\" (safe name: \"" +
                            worksheet + "\") (" + e + ").";
                        Message.printWarning ( warning_level, 
                        MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
                        Message.printWarning ( 3, routine, e );
                        status.addToLog(CommandPhaseType.RUN,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "See the log file for details."));
                    }
                }
            }
            if ( keepOpen ) {
                // Save the open workbook for other commands to use
                ExcelUtil.setOpenWorkbook(OutputFile_full,"w",wb);
            }
            else {
                // Write the workbook and close
                fout = new FileOutputStream(OutputFile_full);
                wb.write(fout);
                ExcelUtil.removeOpenWorkbook(OutputFile_full);
            }
            // Save the output file name...
            setOutputFile ( new File(OutputFile_full));
        }
    }
    catch ( Exception e ) {
		message = "Unexpected error creating Excel workbook file \"" + OutputFile_full + "\" (" + e + ").";
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
            fout.close();
        }
        catch ( Exception e ) {
            // Should not happen
        }
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
	String OutputFile = parameters.getValue("OutputFile");
	String Worksheets = parameters.getValue("Worksheets");
	String IfFound = parameters.getValue("IfFound");
	String KeepOpen = parameters.getValue("KeepOpen");
	StringBuffer b = new StringBuffer ();
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFile=\"" + OutputFile + "\"");
    }
    if ( (Worksheets != null) && (Worksheets.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Worksheets=\"" + Worksheets + "\"" );
    }
	if ( (IfFound != null) && (IfFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfFound=" + IfFound );
	}
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
