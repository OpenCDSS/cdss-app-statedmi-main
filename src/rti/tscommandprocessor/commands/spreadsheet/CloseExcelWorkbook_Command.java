// CloseExcelWorkbook_Command - This class initializes, checks, and runs the CloseExcelWorkbook() command, using Apache POI.

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

import javax.swing.JFrame;

import org.apache.poi.ss.usermodel.Workbook;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;

/**
This class initializes, checks, and runs the CloseExcelWorkbook() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class CloseExcelWorkbook_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for WriteFile parameter.
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
public CloseExcelWorkbook_Command ()
{	super();
	setCommandName ( "CloseExcelWorkbook" );
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
	String WriteFile = parameters.getValue ( "WriteFile" );
	String RecalculateFormulasAtOpen = parameters.getValue ( "RecalculateFormulasAtOpen" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

    /*
	String working_dir = null;
	
	CommandProcessor processor = getCommandProcessor();
	
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
                        message, "Report the problem to software support." ) );
	}
	*/

	if ( (OutputFile == null) || OutputFile.isEmpty() ) {
        message = "The Excel output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing Excel output file." ) );
	}
	/** TODO SAM 2014-01-12 Evaluate whether to only do this check at run-time
	else if ( !OutputFile.indexOf("${") < 0 ) {
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, OutputFile) );
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The Excel output file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the Excel output file exists - may be OK if created at run time." ) );
			}
			f = null;
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
	*/
	
    if ( WriteFile != null && !WriteFile.equalsIgnoreCase(_True) && 
        !WriteFile.equalsIgnoreCase(_False) && !WriteFile.isEmpty() ) {
        message = "WriteFile is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "WriteFile must be specified as " + _False + " or " + _True ) );
    }
    
    if ( RecalculateFormulasAtOpen != null && !RecalculateFormulasAtOpen.equalsIgnoreCase(_True) && 
        !RecalculateFormulasAtOpen.equalsIgnoreCase(_False) && !RecalculateFormulasAtOpen.isEmpty() ) {
        message = "RecalculateFormulasAtOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "RecalculateFormulasAtOpen must be specified as " + _False + " or " + _True + " (default).") );
    }

	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(3);
    validList.add ( "OutputFile" );
    validList.add ( "NewOutputFile" );
    validList.add ( "WriteFile" );
    validList.add ( "RecalculateFormulasAtOpen" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new CloseExcelWorkbook_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    List<File> list = new ArrayList<File>();
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
throws InvalidCommandParameterException, CommandWarningException
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;

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
		status.clearLog(commandPhase);
	}
    // Set output file to null since may not be used...
    setOutputFile(null);
    
	PropList parameters = getCommandParameters();

	String OutputFile = parameters.getValue ( "OutputFile" );
	String NewOutputFile = parameters.getValue ( "NewOutputFile" );
	String WriteFile = parameters.getValue ( "WriteFile" );
	String RecalculateLimitsAtOpen = parameters.getValue ( "RecalculateLimitsAtOpen" );
	boolean recalcLimits = true;
	if ( (RecalculateLimitsAtOpen != null) && RecalculateLimitsAtOpen.equalsIgnoreCase("false") ) {
		recalcLimits = false;
	}

	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
        	TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)) );
	if ( ExcelUtil.getOpenWorkbook(OutputFile_full) == null ) {
		message += "\nThe Excel workbook file \"" + OutputFile_full + "\" is not open from a previous command.";
		++warning_count;
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the Excel workbook file is open in memory or exists as a file." ) );
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

	try {
		// Get the open workbook
	    Workbook wb = null;
        // See if an open workbook by the same name exists
        WorkbookFileMetadata wbMeta = ExcelUtil.getOpenWorkbook(OutputFile_full);
        if ( wbMeta != null ) {
        	wb = wbMeta.getWorkbook();
        }
        if ( wb != null ) {
            // Workbook is open in memory so close the workbook and remove from the cache
	    	String outputFileFull = OutputFile_full; // Default is to (re)write original file
	    	if ( (NewOutputFile != null) && !NewOutputFile.isEmpty() ) {
	    		// Set the output file to the new file provided by NewOutputFile
	    		outputFileFull = IOUtil.verifyPathForOS(
	    	        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
	    	        	TSCommandProcessorUtil.expandParameterValue(processor,this,NewOutputFile)) );
	    	}
	    	boolean doWrite = false;
	    	if ( (WriteFile != null) && !WriteFile.isEmpty() ) {
	    		// Use the specified value
	    		if ( WriteFile.equalsIgnoreCase("true") ) {
	    			doWrite = true;
	    		}
	    		else if ( WriteFile.equalsIgnoreCase("false") ) {
	    			doWrite = false;
	    		}
	    	}
	    	else {
	    		// Determine default depending on how the workbook was originally created or opened
	    		if ( wbMeta.getMode().equalsIgnoreCase("w") ) {
	    			// Excel file was opened to write or has been written to so default is to write on close
	    			doWrite = true;
	    		}
	    		else {
	    			// Excel file was opened to read and has not been written to so default is to not write on close
	    			doWrite = false;
	    		}
	    	}
	    	if ( doWrite ) {
	    		if ( recalcLimits ) {
	    			wb.setForceFormulaRecalculation(true); // Will cause Excel to recalculate formulas when it opens
	    		}
		        FileOutputStream fout = new FileOutputStream(outputFileFull);
		        setOutputFile(new File(outputFileFull));
		        wb.write(fout);
		        fout.close();
	    	}
	        ExcelUtil.removeOpenWorkbook(OutputFile_full);
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error closing Excel workbook file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the file exists and is writeable." ) );
		throw new CommandWarningException ( message );
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
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
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String OutputFile = props.getValue( "OutputFile" );
	String NewOutputFile = props.getValue( "NewOutputFile" );
	String WriteFile = props.getValue( "WriteFile" );
	String RecalculateLimitsAtOpen = props.getValue( "RecalculateLimitsAtOpen" );
	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (NewOutputFile != null) && (NewOutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewOutputFile=\"" + NewOutputFile + "\"" );
	}
	if ( (WriteFile != null) && (WriteFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteFile=" + WriteFile );
	}
	if ( (RecalculateLimitsAtOpen != null) && (RecalculateLimitsAtOpen.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RecalculateLimitsAtOpen=" + RecalculateLimitsAtOpen );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
