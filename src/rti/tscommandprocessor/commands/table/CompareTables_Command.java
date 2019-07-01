// CompareTables_Command - This class initializes, checks, and runs the CompareTables() command.

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

import DWR.DMI.StateDMI.StateDMI_Processor;
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
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableComparer;

/**
This class initializes, checks, and runs the CompareTables() command.
*/
public class CompareTables_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider, FileGenerator
{
    
/**
Possible values for IfDifferent and IfSame parameters.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Possible values for MatchColumnsHow parameters.
*/
protected final String _Name = "Name";
protected final String _Order = "Order";

/**
The comparison table that is created.
*/
private DataTable __table = null;

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public CompareTables_Command ()
{	super();
	setCommandName ( "CompareTables" );
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
{	String Table1ID = parameters.getValue ( "Table1ID" );
    String Table2ID = parameters.getValue ( "Table2ID" );
    String Precision = parameters.getValue ( "Precision" );
    String MatchColumnsHow = parameters.getValue ( "MatchColumnsHow" );
    String Tolerance = parameters.getValue ( "Tolerance" );
    String AllowedDiff = parameters.getValue ( "AllowedDiff" );
    String NewTableID = parameters.getValue ( "NewTableID" );
    String OutputFile = parameters.getValue ( "OutputFile" );
    String IfDifferent = parameters.getValue ( "IfDifferent" );
    String IfSame = parameters.getValue ( "IfSame" );
	String warning = "";
    String message;

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (Table1ID == null) || (Table1ID.length() == 0) ) {
        message = "The first table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the first table identifier." ) );
    }
    
    if ( (Table2ID == null) || (Table2ID.length() == 0) ) {
        message = "The second table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the second table identifier." ) );
    }
    
    if ( (Table1ID != null) && (Table1ID.length() != 0) && (NewTableID != null) && (NewTableID.length() != 0) &&
        Table1ID.equalsIgnoreCase(NewTableID) ) {
        message = "The first and new table identifiers are the same.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the new table identifier different from the first table identifier." ) );
    }
    
    if ( (Table2ID != null) && (Table2ID.length() != 0) && (NewTableID != null) && (NewTableID.length() != 0) &&
        Table2ID.equalsIgnoreCase(NewTableID) ) {
        message = "The second and new table identifiers are the same.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the new table identifier different from the second table identifier." ) );
    }
    
    if ( (MatchColumnsHow != null) && !MatchColumnsHow.equals("") && !MatchColumnsHow.equalsIgnoreCase(_Name) &&
        !MatchColumnsHow.equalsIgnoreCase(_Order) ) {
            message = "The MatchColumnsHow parameter \"" + MatchColumnsHow + "\" is not a valid value.";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the parameter as " + _Name + " (default) or " + _Order + "."));
    }
    
    if ( (Precision != null) && !Precision.equals("") ) {
        if ( !StringUtil.isInteger(Precision) ) {
            message = "The precision: \"" + Precision + "\" is not an integer.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the precision as an integer (or blank to not round)." ) );
            
        }
        if ( StringUtil.atoi(Precision) < 0 ) {
            message = "The precision: \"" + Precision + "\" must be >= 0.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the precision as an integer >= 0 (or blank to not round)." ) );
        }
    }
    
    if ( (Tolerance != null) && !Tolerance.equals("") ) {
        if ( !StringUtil.isDouble(Tolerance) ) {
            message = "The tolerance: \"" + Tolerance + "\" is not a number.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the tolerance as a number." ) );
            
        }
        if ( StringUtil.atod(Tolerance) < 0.0 ) {
            message = "The tolerance: \"" + Tolerance + "\" must be >= 0.0.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the tolerance as a number >= 0.0." ) );
        }
    }
    
    if ( (AllowedDiff != null) && !AllowedDiff.equals("") && !StringUtil.isInteger(AllowedDiff) ) {
        message = "The number of allowed differences \"" + AllowedDiff + "\" is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                 message, "Specify the parameter as an integer."));
    }
    
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        String working_dir = null;
        try { Object o = processor.getPropContents ( "WorkingDir" );
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
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The output file parent directory does not exist: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Create the output directory." ) );
            }
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
	List<String> valid_Vector = new Vector<String>();
    valid_Vector.add ( "Table1ID" );
    valid_Vector.add ( "Table2ID" );
    valid_Vector.add ( "CompareColumns1" );
    valid_Vector.add ( "ExcludeColumns1" );
    valid_Vector.add ( "CompareColumns2" );
    valid_Vector.add ( "MatchColumnsHow" );
    valid_Vector.add ( "Precision" );
    valid_Vector.add ( "Tolerance" );
    valid_Vector.add ( "AllowedDiff" );
    valid_Vector.add ( "NewTableID" );
    valid_Vector.add ( "OutputFile" );
    valid_Vector.add ( "IfDifferent" );
    valid_Vector.add ( "IfSame" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );    

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
{	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new CompareTables_JDialog ( parent, this, tableIDChoices )).ok();
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
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
    return __OutputFile_File;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{   DataTable table = getDiscoveryTable();
    List<T> v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new ArrayList<T>();
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
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "CompareTables_Command.runCommand",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
	
    // Clear the output file
    setOutputFile ( null );
    
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String Table1ID = parameters.getValue ( "Table1ID" );
    String Table2ID = parameters.getValue ( "Table2ID" );
    String CompareColumns1 = parameters.getValue ( "CompareColumns1" );
    String ExcludeColumns1 = parameters.getValue ( "ExcludeColumns1" );
    String CompareColumns2 = parameters.getValue ( "CompareColumns2" );
    String MatchColumnsHow = parameters.getValue ( "MatchColumnsHow" );
    boolean matchColumnsByName = true;
    if ( (MatchColumnsHow != null) && MatchColumnsHow.equalsIgnoreCase(_Order) ) {
        matchColumnsByName = false;
    }
    String Precision = parameters.getValue ( "Precision" );
    Integer Precision_Integer = null;
    if ( (Precision != null) && !Precision.equals("") ) {
        Precision_Integer = new Integer ( Precision );
    }
    String Tolerance = parameters.getValue ( "Tolerance" );
    Double Tolerance_Double = null;
    if ( (Tolerance != null) && !Tolerance.equals("") ) {
        Tolerance_Double = new Double ( Tolerance );
    }
    String AllowedDiff = parameters.getValue ( "AllowedDiff" );
    int AllowedDiff_int = 0;
    if ( StringUtil.isInteger(AllowedDiff) ) {
        AllowedDiff_int = Integer.parseInt(AllowedDiff);
    }
    String NewTableID = parameters.getValue ( "NewTableID" );
    String newTableID = Table1ID + "-" + Table2ID + "-comparison";
    if ( (NewTableID != null) && !NewTableID.equals("") ) {
        newTableID = NewTableID;
    }
    String OutputFile = parameters.getValue ( "OutputFile" );
    if ( (OutputFile != null) && OutputFile.equals("") ) {
        OutputFile = null; // Easier for checks below
    }
    String IfDifferent = parameters.getValue ( "IfDifferent" );
    CommandStatusType IfDifferent_CommandStatusType = CommandStatusType.UNKNOWN;
    if ( (IfDifferent == null) || IfDifferent.equals("") ) {
        IfDifferent = _Ignore; // default
    }
    else {
        if ( !IfDifferent.equalsIgnoreCase(_Ignore) ) {
            IfDifferent_CommandStatusType = CommandStatusType.parse(IfDifferent);
        }
    }
    String IfSame = parameters.getValue ( "IfSame" );
    CommandStatusType IfSame_CommandStatusType = CommandStatusType.UNKNOWN;
    if ( (IfSame == null) || IfSame.equals("") ) {
        IfSame = _Ignore; // default
    }
    else {
        if ( !IfSame.equalsIgnoreCase(_Ignore) ) {
            IfSame_CommandStatusType = CommandStatusType.parse(IfSame);
        }
    }
    
    String [] compareColumns1 = null;
    if ( (CompareColumns1 != null) && !CompareColumns1.equals("") ) {
        compareColumns1 = CompareColumns1.split(",");
        for ( int i = 0; i < compareColumns1.length; i++ ) {
            compareColumns1[i] = compareColumns1[i].trim();
        }
    }
    String [] excludeColumns1 = null;
    if ( (ExcludeColumns1 != null) && !ExcludeColumns1.equals("") ) {
        excludeColumns1 = ExcludeColumns1.split(",");
        for ( int i = 0; i < excludeColumns1.length; i++ ) {
            excludeColumns1[i] = excludeColumns1[i].trim();
        }
    }
    String [] compareColumns2 = null;
    if ( (CompareColumns2 != null) && !CompareColumns2.equals("") ) {
        compareColumns2 = CompareColumns2.split(",");
        for ( int i = 0; i < compareColumns2.length; i++ ) {
            compareColumns2[i] = compareColumns2[i].trim();
        }
    }
    
    // Get the table to process.

    DataTable table1 = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (Table1ID != null) && !Table1ID.equals("") ) {
            // Get the table to be updated
            request_params = new PropList ( "" );
            request_params.set ( "TableID", Table1ID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + Table1ID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + Table1ID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
                table1 = (DataTable)o_Table;
            }
        }
    }
    
    DataTable table2 = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (Table2ID != null) && !Table2ID.equals("") ) {
            // Get the table to be updated
            request_params = new PropList ( "" );
            request_params.set ( "TableID", Table2ID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + Table2ID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + Table2ID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
                table2 = (DataTable)o_Table;
            }
        }
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}
    int diffCount = 0; // For final warning check
    int tableCellCount = 0;
	try {
    	// Create the table...
	    String OutputFile_full = OutputFile;
	    if ( commandPhase == CommandPhaseType.RUN ) {
	        if ( OutputFile != null ) {
    	        OutputFile_full = IOUtil.verifyPathForOS(
    	            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile) );
	        }
	        DataTableComparer comparer = new DataTableComparer ( table1, 
	            StringUtil.toList(compareColumns1), StringUtil.toList(excludeColumns1),
	            table2, StringUtil.toList(compareColumns2),
	            matchColumnsByName, Precision_Integer, Tolerance_Double, newTableID );
	        comparer.compare ();
	        DataTable comparisonTable = comparer.getComparisonTable();
	        tableCellCount = comparisonTable.getNumberOfRecords()*comparisonTable.getNumberOfFields();
	        diffCount = comparer.getDifferenceCount();
	        
	        // If an output file is desired, write to it and save the name.
	        if ( OutputFile != null ) {
	            comparer.writeHtmlFile ( OutputFile_full );
	            setOutputFile ( new File(OutputFile_full));
	        }
            
            // Set the table in the processor if the user has specific a name (otherwise the table is used
	        // internally, for example to create the HTML file)...
            
	        if ( (comparisonTable != null) && (NewTableID != null) && !NewTableID.equals("") ) {
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", comparisonTable );
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
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            if ( (NewTableID != null) && !NewTableID.equals("") ) {
                // Create an empty table and set the ID
                DataTable comparisonTable = new DataTable();
                comparisonTable.setTableID ( NewTableID );
                setDiscoveryTable ( comparisonTable );
                Message.printStatus(2,routine,"Setting discovery table " + comparisonTable.getTableID() );
            }
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error comparing tables (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
	}
	int allowedDiffPositive = AllowedDiff_int;
	if ( AllowedDiff_int < 0 ) {
	    allowedDiffPositive = AllowedDiff_int;
	}
	if ( diffCount > 0 ) {
	    // Have some differences - action is as per user request
        message = "" + diffCount + " table values were different, " +
        StringUtil.formatString(100.0*(double)diffCount/(double)tableCellCount, "%.2f") +
        "% (compared " + tableCellCount + " values).";
        Message.printStatus ( 2, routine, message );
	    boolean needToNotify = false;
	    if ( (AllowedDiff_int < 0) && (diffCount > allowedDiffPositive) ) {
	        needToNotify = true; // Too many differences (not expected)
	    }
	    else if ( (AllowedDiff_int >= 0) && (diffCount != AllowedDiff_int) ) {
	        needToNotify = true; // Does not match exact number of differences
	    }
        if ( needToNotify &&
           ((IfDifferent_CommandStatusType == CommandStatusType.WARNING) ||
           (IfDifferent_CommandStatusType == CommandStatusType.FAILURE)) ) {
           // Have differences and need to warn
           Message.printWarning ( warning_level,
           MessageUtil.formatMessageTag( command_tag,++warning_count),
           routine, message );
           status.addToLog(CommandPhaseType.RUN,
               new CommandLogRecord(IfDifferent_CommandStatusType,
                   message, "Check files because difference is not expected.") );
           throw new CommandException ( message );
        }
    }
	else {
	    // No differences were reported
        message = "No table values were different (the tables are the same).";
        Message.printStatus ( 2, routine, message );
	    if ( (IfSame_CommandStatusType == CommandStatusType.WARNING) ||
	       (IfSame_CommandStatusType == CommandStatusType.FAILURE)) {
           Message.printWarning ( warning_level,
           MessageUtil.formatMessageTag( command_tag,++warning_count),
           routine, message );
           status.addToLog(CommandPhaseType.RUN,
               new CommandLogRecord(IfSame_CommandStatusType,
                   message, "Check files because match is not expected.") );
           throw new CommandException ( message );
	    }
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

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String Table1ID = props.getValue( "Table1ID" );
    String Table2ID = props.getValue( "Table2ID" );
	String CompareColumns1 = props.getValue( "CompareColumns1" );
	String ExcludeColumns1 = props.getValue( "ExcludeColumns1" );
	String CompareColumns2 = props.getValue( "CompareColumns2" );
	String MatchColumnsHow = props.getValue("MatchColumnsHow");
    String Precision = props.getValue("Precision");
    String Tolerance = props.getValue("Tolerance");
    String AllowedDiff = props.getValue("AllowedDiff");
    String NewTableID = props.getValue( "NewTableID" );
    String OutputFile = props.getValue ( "OutputFile" );
    String IfDifferent = props.getValue("IfDifferent");
    String IfSame = props.getValue("IfSame");
	StringBuffer b = new StringBuffer ();
    if ( (Table1ID != null) && (Table1ID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Table1ID=\"" + Table1ID + "\"" );
    }
    if ( (CompareColumns1 != null) && (CompareColumns1.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CompareColumns1=\"" + CompareColumns1 + "\"" );
    }
    if ( (ExcludeColumns1 != null) && (ExcludeColumns1.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeColumns1=\"" + ExcludeColumns1 + "\"" );
    }
    if ( (Table2ID != null) && (Table2ID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Table2ID=\"" + Table2ID + "\"" );
    }
    if ( (CompareColumns2 != null) && (CompareColumns2.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "CompareColumns2=\"" + CompareColumns2 + "\"" );
    }
    if ( (MatchColumnsHow != null) && (MatchColumnsHow.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MatchColumnsHow=" + MatchColumnsHow );
    }
    if ( (Precision != null) && (Precision.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Precision=" + Precision );
    }
    if ( (Tolerance != null) && (Tolerance.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Tolerance=" + Tolerance );
    }
    if ( (AllowedDiff != null) && (AllowedDiff.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "AllowedDiff=\"" + AllowedDiff + "\"" );
    }
    if ( (NewTableID != null) && (NewTableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NewTableID=\"" + NewTableID + "\"" );
    }
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFile=\"" + OutputFile + "\"" );
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
