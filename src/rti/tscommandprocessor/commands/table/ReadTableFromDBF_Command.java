// ReadTableFromDBF_Command - This class initializes, checks, and runs the ReadTableFromDBF() command.

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

package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DbaseDataTable;
import RTi.Util.Table.TableField;

/**
This class initializes, checks, and runs the ReadTableFromDBF() command.
*/
public class ReadTableFromDBF_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
The table that is read.
*/
private DataTable __table = null;

/**
Constructor.
*/
public ReadTableFromDBF_Command ()
{	super();
	setCommandName ( "ReadTableFromDBF" );
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
{	String TableID = parameters.getValue ( "TableID" );
    String InputFile = parameters.getValue ( "InputFile" );
    String Top = parameters.getValue ( "Top" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	String working_dir = null;
	
	CommandProcessor processor = getCommandProcessor();
	
    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the table identifier." ) );
    }
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
	
	if ( (InputFile == null) || (InputFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
	}
	else if ( InputFile.indexOf("${") < 0 ) {
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, InputFile) );
            /* Don't check for existence during discovery.  Do it at runtime because file may be dynamically created.
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The input file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the input file exists - may be OK if created at run time." ) );
			}
			f = null;
			*/
		}
		catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + InputFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
		}
	}
    if ( (Top != null) && (Top.length() != 0) && !StringUtil.isInteger(Top)) {
        message = "The Top value (" + Top +") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the Top parameter as an integer." ) );
    }
 
	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<>(6);
    validList.add ( "TableID" );
    validList.add ( "InputFile" );
    validList.add ( "DoubleColumns" );
    validList.add ( "IntegerColumns" );
    validList.add ( "TextColumns" );
    validList.add ( "Top" );
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
	return (new ReadTableFromDBF_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
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

// Use base class parseCommand()

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
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommandInternal", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
	CommandProcessor processor = getCommandProcessor();
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
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
	String InputFile = parameters.getValue ( "InputFile" ); // Expanded below
	String DoubleColumns = parameters.getValue ( "DoubleColumns" );
	if ( (DoubleColumns != null) && (DoubleColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		DoubleColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, DoubleColumns);
	}
	String IntegerColumns = parameters.getValue ( "IntegerColumns" );
	if ( (IntegerColumns != null) && (IntegerColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		IntegerColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, IntegerColumns);
	}
	String TextColumns = parameters.getValue ( "TextColumns" );
	if ( (TextColumns != null) && (TextColumns.indexOf("${") >= 0) && (commandPhase == CommandPhaseType.RUN) ) {
		TextColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, TextColumns);
	}
    String Top = parameters.getValue ( "Top" );
    Integer top = 0;
    if ( (Top != null) && !Top.equals("") ) {
        top = Integer.parseInt(Top);
    }

	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
        	TSCommandProcessorUtil.expandParameterValue(processor,this,InputFile)));
	// Only warn during run mode because properties may be used or file may be created dynamically
	if ( commandPhase == CommandPhaseType.RUN ) {
	    if ( !IOUtil.fileExists(InputFile_full) ) {
		    message += "\nThe DBF file \"" + InputFile_full + "\" does not exist.";
		    ++warning_count;
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the DBF file exists." ) );
	    }
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

	DataTable table = null;
	try {
	    if ( commandPhase == CommandPhaseType.RUN ) {
	        table = new DbaseDataTable ( InputFile_full, true, false );
	        table.setTableID ( TableID );
	        // Remove records if smaller number is requested.
	        // TODO SAM 2013-08-25 update the above method to only read top - for now process after
	        if ( (top != null) && (top != 0) ) {
	            for ( int i = table.getNumberOfRecords() - 1; i >= top; i-- ) {
	                table.deleteRecord(i);
	            }
	        }
	        // Convert specified columns to double if specified.
	        if ( (DoubleColumns != null) && !DoubleColumns.isEmpty() ) {
	            // Split the parameter into separate strings.
	            String [] doubleColumns = DoubleColumns.split(",");
	            for ( int i = 0; i < doubleColumns.length; i++ ) {
	                // Convert * to Java style regular expression
	                doubleColumns[i] = doubleColumns[i].trim().replace("*", ".*");
	            }
	            // Get a list of specific table columns considering possible regex in parameter
	            // - must match case
	            // - check regex
	            List<String> doubleColumns2 = StringUtil.includeStrings(
	            	StringUtil.toList(table.getFieldNames()),
	            	StringUtil.toList(doubleColumns), false, true);
	            int [] fieldNum = new int[doubleColumns2.size()];
	            for ( int icol = 0; icol < doubleColumns2.size(); icol++ ) {
	          	    fieldNum[icol] = table.getFieldIndex(doubleColumns2.get(icol));
	            }
	            // Loop through the double column names.
	            for ( int i = 0; i < doubleColumns2.size(); i++ ) {
	            	try {
	            		// Set the precision so that table display shows a double
	                    table.changeFieldDataType(fieldNum[i], TableField.DATA_TYPE_DOUBLE, -1, 6);
	            	}
	            	catch ( Exception e ) {
		                Message.printWarning ( 3, routine, e );
		                message = "Error changing columnn \"" + doubleColumns2.get(i) + "\" to double (" + e + ").";
		                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Review original data to ensure that it can be converted to double." ) );
		                throw new CommandWarningException ( message );
	            	}
	            }
	        }
	        // Convert specified columns to integer if specified.
	        // - currently this can only be used for Double or Float columns
	        if ( (IntegerColumns != null) && !IntegerColumns.isEmpty() ) {
	            // Split the parameter into separate strings.
	            String [] integerColumns = IntegerColumns.split(",");
	            for ( int i = 0; i < integerColumns.length; i++ ) {
	                // Convert * to Java style regular expression
	                integerColumns[i] = integerColumns[i].trim().replace("*", ".*");
	            }
	            // Get a list of specific table columns considering possible regex in parameter
	            // - must match case
	            // - check regex
	            List<String> integerColumns2 = StringUtil.includeStrings(
	            	StringUtil.toList(table.getFieldNames()),
	            	StringUtil.toList(integerColumns), false, true);
	            int [] fieldNum = new int[integerColumns2.size()];
	            for ( int icol = 0; icol < integerColumns2.size(); icol++ ) {
	          	    fieldNum[icol] = table.getFieldIndex(integerColumns2.get(icol));
	            }
	            for ( int i = 0; i < integerColumns2.size(); i++ ) {
	            	try {
	                    table.changeFieldDataType(fieldNum[i], TableField.DATA_TYPE_INT, -1, -1);
	            	}
	            	catch ( Exception e ) {
		                Message.printWarning ( 3, routine, e );
		                message = "Error changing columnn \"" + integerColumns2.get(i) + "\" to integer (" + e + ").";
		                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Review original data to ensure that it can be converted to integer." ) );
		                throw new CommandWarningException ( message );
	            	}
	            }
	        }
	        // Convert specified columns to text if specified.
	        if ( (TextColumns != null) && !TextColumns.isEmpty() ) {
	            // Split the parameter into separate strings.
	            String [] textColumns = TextColumns.split(",");
	            for ( int i = 0; i < textColumns.length; i++ ) {
	                // Convert * to Java style regular expression
	                textColumns[i] = textColumns[i].trim().replace("*", ".*");
	            }
	            // Get a list of specific table columns considering possible regex in parameter
	            // - must match case
	            // - check regex
	            List<String> textColumns2 = StringUtil.includeStrings(
	            	StringUtil.toList(table.getFieldNames()),
	            	StringUtil.toList(textColumns), false, true);
	            int [] fieldNum = new int[textColumns2.size()];
	            for ( int icol = 0; icol < textColumns2.size(); icol++ ) {
	          	    fieldNum[icol] = table.getFieldIndex(textColumns2.get(icol));
	            }
	            for ( int i = 0; i < textColumns2.size(); i++ ) {
	            	try {
	                    table.changeFieldDataType(fieldNum[i], TableField.DATA_TYPE_STRING, -1, -1);
	            	}
	            	catch ( Exception e ) {
	                    Message.printWarning ( 3, routine, e );
	                    message = "Error changing columnn \"" + textColumns2.get(i) + "\" to text/string (" + e + ").";
	                    Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
                        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Review original data to ensure that it can be converted to text/string." ) );
	                    throw new CommandWarningException ( message );
	                }
	            }
	        }
	    }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error reading table from dBASE file \"" + InputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the file exists and is readable." ) );
		throw new CommandWarningException ( message );
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}
    
    // Set the table in the processor...
    
    if ( commandPhase == CommandPhaseType.RUN ) {
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
        table = new DataTable ();
        table.setTableID ( TableID );
        setDiscoveryTable ( table );
    }

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
	String InputFile = props.getValue( "InputFile" );
	String DoubleColumns = props.getValue("DoubleColumns");
	String IntegerColumns = props.getValue("IntegerColumns");
	String TextColumns = props.getValue("TextColumns");
	String Top = props.getValue( "Top" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (DoubleColumns != null) && (DoubleColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DoubleColumns=\"" + DoubleColumns + "\"" );
	}
	if ( (IntegerColumns != null) && (IntegerColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IntegerColumns=\"" + IntegerColumns + "\"" );
	}
	if ( (TextColumns != null) && (TextColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TextColumns=\"" + TextColumns + "\"" );
	}
    if ( (Top != null) && (Top.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Top=" + Top );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}