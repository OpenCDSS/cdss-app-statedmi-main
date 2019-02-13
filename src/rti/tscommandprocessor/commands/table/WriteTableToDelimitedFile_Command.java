// WriteTableToDelimitedFile_Command - This class initializes, checks, and runs the WriteTableToDelimitedFile() command.

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

package rti.tscommandprocessor.commands.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the WriteTableToDelimitedFile() command.
*/
public class WriteTableToDelimitedFile_Command extends AbstractCommand implements Command, FileGenerator
{

/** 
Values for use with WriteHeaderComments parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
 * Values for the OutputSchemaFormat parameter.
 */
protected final String _JSONTableSchema = "JSONTableSchema";
protected final String _GoogleBigQuery = "GoogleBigQuery";

/**
Value to use for NaNValue.
*/
protected final String _Blank = "Blank";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Output schema file that is created by this command.
*/
private File __OutputSchemaFile_File = null;

/**
Constructor.
*/
public WriteTableToDelimitedFile_Command ()
{	super();
	setCommandName ( "WriteTableToDelimitedFile" );
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
	String TableID = parameters.getValue ( "TableID" );
	String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
	String AlwaysQuoteStrings = parameters.getValue ( "AlwaysQuoteStrings" );
	String OutputSchemaFile = parameters.getValue ( "OutputSchemaFile" );
	String OutputSchemaFormat = parameters.getValue ( "OutputSchemaFormat" );
	String warning = "";
	String routine = getCommandName() + ".checkCommandParameters";
	String message;

	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
    if ( (TableID == null) || TableID.isEmpty()) {
        message = "The table identifier for the table to write has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid table identifier." ) );
    }
	
	if ( (OutputFile == null) || OutputFile.isEmpty() ) {
		message = "The output file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an output file." ) );
	}
	else if ( OutputFile.indexOf("${") < 0 ) {
		// Can't check if output file is specified with ${Property}
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
				message = "The output file parent directory does " +
				"not exist: \"" + adjusted_path + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Create the output directory." ) );
			}
			f = null;
			f2 = null;
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
		// Also check schema file
		if ( (OutputSchemaFile != null) && (OutputSchemaFile.indexOf("${") < 0) ) {
			try {
	            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputSchemaFile));
				File f = new File ( adjusted_path );
				File f2 = new File ( f.getParent() );
				if ( !f2.exists() ) {
					message = "The output schema file parent directory does not exist: \"" + adjusted_path + "\".";
					warning += "\n" + message;
					status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Create the output directory." ) );
				}
			}
			catch ( Exception e ) {
				message = "The output schema file:\n" +
				"    \"" + OutputSchemaFile +
				"\"\ncannot be adjusted using the working directory:\n" +
				"    \"" + working_dir + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that output file and working directory paths are compatible." ) );
			}
		}
	}
	
    if ( (WriteHeaderComments != null) && !WriteHeaderComments.equals("") ) {
        if ( !WriteHeaderComments.equalsIgnoreCase(_False) && !WriteHeaderComments.equalsIgnoreCase(_True) ) {
            message = "The WriteHeaderComments parameter (" + WriteHeaderComments + ") must be " + _False +
            " or " + _True + ".";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the parameter as " + _False + " or " + _True + "."));
        }
    }
    
    if ( (AlwaysQuoteStrings != null) && !AlwaysQuoteStrings.equals("") ) {
        if ( !AlwaysQuoteStrings.equalsIgnoreCase(_False) && !AlwaysQuoteStrings.equalsIgnoreCase(_True) ) {
            message = "The AlwaysQuoteStrings parameter (" + AlwaysQuoteStrings + ") must be " + _False +
            " (default) or " + _True + ".";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the parameter as " + _False + " or " + _True + "."));
        }
    }
    
    if ( (OutputSchemaFormat != null) && !OutputSchemaFormat.equals("")
    	&& !OutputSchemaFormat.equalsIgnoreCase(_JSONTableSchema) && !OutputSchemaFormat.equalsIgnoreCase(_GoogleBigQuery)) {
        message = "The OutputSchemaFormat parameter (" + OutputSchemaFormat + ") must be " + _JSONTableSchema +
        " (default if blank) or " + _GoogleBigQuery + ").";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _False + " or " + _True + "."));
    }

	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(8);
	validList.add ( "OutputFile" );
	validList.add ( "TableID" );
	validList.add ( "WriteHeaderComments" );
	validList.add ( "AlwaysQuoteStrings" );
	validList.add ( "NewlineReplacement" );
	validList.add ( "NaNValue" );
	validList.add ( "OutputSchemaFile" );
	validList.add ( "OutputSchemaFormat" );
	warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
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
    List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
	return (new WriteTableToDelimitedFile_JDialog ( parent, this, tableIDChoices )).ok();
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
	if ( getOutputSchemaFile() != null ) {
		list.add ( getOutputSchemaFile() );
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
Return the output schema file generated by this file.  This method is used internally.
*/
private File getOutputSchemaFile ()
{
	return __OutputSchemaFile_File;
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	// Clear the output file
	
	setOutputFile ( null );
	setOutputSchemaFile ( null );
	
	// Check whether the processor wants output files to be created...

	CommandProcessor processor = getCommandProcessor();
	if ( !TSCommandProcessorUtil.getCreateOutput(processor) ) {
		Message.printStatus ( 2, routine, "Skipping \"" + toString() + "\" because output is not being created." );
	}

	PropList parameters = getCommandParameters();
	
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
	
	String OutputFile_full = null;

    // Get the table information  
    String OutputFile = parameters.getValue ( "OutputFile" );
    OutputFile_full = OutputFile;
    String TableID = parameters.getValue ( "TableID" );
    if ( (TableID != null) && !TableID.isEmpty() && (commandPhase == CommandPhaseType.RUN) ) {
    	// In discovery mode want lists of tables to include ${Property}
    	if ( TableID.indexOf("${") >= 0 ) {
    		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
    	}
    }
    String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
    boolean WriteHeaderComments_boolean = true;
    if ( (WriteHeaderComments != null) && WriteHeaderComments.equalsIgnoreCase(_False) ) {
        WriteHeaderComments_boolean = false;
    }
    String AlwaysQuoteStrings = parameters.getValue ( "AlwaysQuoteStrings" );
    boolean AlwaysQuoteStrings_boolean = false; // Default
    if ( (AlwaysQuoteStrings != null) && AlwaysQuoteStrings.equalsIgnoreCase(_True) ) {
        AlwaysQuoteStrings_boolean = true;
    }
    String NewlineReplacement = parameters.getValue ( "NewlineReplacement" );
    String newlineReplacement = NewlineReplacement;
    if ( (NewlineReplacement != null) && NewlineReplacement.equals("") ) {
        newlineReplacement = null; // User must use \s to indicate space
    }
    String NaNValue = parameters.getValue ( "NaNValue" );
    if ( NaNValue != null ) {
        if ( NaNValue.equals("") ) {
            NaNValue = null; // Will result in "NaN" in output
        }
        else if ( NaNValue.equals(_Blank) ) {
            NaNValue = "";
        }
    }
    String OutputSchemaFile = parameters.getValue ( "OutputSchemaFile" );
    String OutputSchemaFormat = parameters.getValue ( "OutputSchemaFormat" );
    String outputSchemaFormat = OutputSchemaFormat;
    if ( (outputSchemaFormat == null) || outputSchemaFormat.isEmpty() ) {
    	outputSchemaFormat = _JSONTableSchema;
    }

    PropList request_params = new PropList ( "" );
    request_params.set ( "TableID", TableID );
    CommandProcessorRequestResultsBean bean = null;
    try {
        bean = processor.processRequest( "GetTable", request_params);
    }
    catch ( Exception e ) {
        message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
        Message.printWarning(warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
    }
    PropList bean_PropList = bean.getResultsPropList();
    Object o_Table = bean_PropList.getContents ( "Table" );
    DataTable table = null;
    if ( o_Table == null ) {
        message = "Unable to find table to process using TableID=\"" + TableID + "\".";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that a table exists with the requested ID." ) );
    }
    else {
        table = (DataTable)o_Table;
    }
        
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings for command parameters.";
        Message.printWarning ( 2,
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine,message);
        throw new InvalidCommandParameterException ( message );
    }
        
    try {
    	// Now try to write...
    
        OutputFile_full = OutputFile;
    
		// Convert to an absolute path...
		OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            	TSCommandProcessorUtil.expandParameterValue(processor, this,OutputFile)) );
		String outputSchemaFile = null;
		if ( (OutputSchemaFile != null) && !OutputSchemaFile.isEmpty() ) {
			outputSchemaFile = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            	TSCommandProcessorUtil.expandParameterValue(processor, this,OutputSchemaFile)) );
		}
		Message.printStatus ( 2, routine, "Writing table to file \"" + OutputFile_full + "\"" );
		warning_count = writeTable ( table, OutputFile_full, WriteHeaderComments_boolean,
		    AlwaysQuoteStrings_boolean, StringUtil.literalToInternal(newlineReplacement), NaNValue,
		    outputSchemaFile, outputSchemaFormat,
		    warning_level, command_tag, warning_count );
		// Save the output file name...
		setOutputFile ( new File(OutputFile_full));
		if ( (outputSchemaFile != null) && !outputSchemaFile.isEmpty() ) {
			setOutputSchemaFile ( new File(outputSchemaFile));
		}
	}
	catch ( Exception e ) {
		message = "Unexpected error writing table to file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Check log file for details." ) );
		throw new CommandException ( message );
	}
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
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
Set the output schema file that is created by this command.  This is only used internally.
*/
private void setOutputSchemaFile ( File file )
{
	__OutputSchemaFile_File = file;
}

/**
Return the string representation of the command.
@param parameters Command parameters as strings.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String OutputFile = parameters.getValue ( "OutputFile" );
	String TableID = parameters.getValue ( "TableID" );
	String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
	String AlwaysQuoteStrings = parameters.getValue ( "AlwaysQuoteStrings" );
	String NewlineReplacement = parameters.getValue ( "NewlineReplacement" );
	String NaNValue = parameters.getValue ( "NaNValue" );
	String OutputSchemaFile = parameters.getValue ( "OutputSchemaFile" );
	String OutputSchemaFormat = parameters.getValue ( "OutputSchemaFormat" );
	StringBuffer b = new StringBuffer ();
	if ( (TableID != null) && (TableID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TableID=\"" + TableID + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
    if ( (WriteHeaderComments != null) && (WriteHeaderComments.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "WriteHeaderComments=" + WriteHeaderComments );
    }
    if ( (AlwaysQuoteStrings != null) && (AlwaysQuoteStrings.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "AlwaysQuoteStrings=" + AlwaysQuoteStrings );
    }
    if ( (NewlineReplacement != null) && (NewlineReplacement.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NewlineReplacement=\"" + NewlineReplacement + "\"" );
    }
    if ( (NaNValue != null) && (NaNValue.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NaNValue=\"" + NaNValue + "\"" );
    }
	if ( (OutputSchemaFile != null) && (OutputSchemaFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputSchemaFile=\"" + OutputSchemaFile + "\"" );
	}
	if ( (OutputSchemaFormat != null) && (OutputSchemaFormat.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputSchemaFormat=\"" + OutputSchemaFormat + "\"" );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

/**
 * Write the table schema file using Google Big Query Schema.
 * See:  https://cloud.google.com/bigquery/docs/reference/rest/v2/tables
 * @table table to write
 * @param outputSchemaFile path/name of schema file to write
 */
private void writeGoogleBigQueryTableSchema ( DataTable table, String outputSchemaFile ) throws IOException {
	PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter(outputSchemaFile)));
	// Brute force the output.
	// TODO SAM 2017-01-18 move to a general class later
	String nl = System.getProperty("line.separator");
	String i1 = "  ", i2 = "    ", i3 = "      ", i4 = "        ";
	out.print ( "{" + nl );
	out.print ( i1 + "\"schema\": {" + nl );
	out.print ( i2 + "\"fields\": [" + nl );
	String colName, colDescription, dataTypeSchema;
	int colType;
	int irow;
	for ( int icol = 0; icol < table.getNumberOfFields(); icol++ ) {
		colName = table.getFieldName(icol);
		// TODO sam 2017-01-18 need to enable
		//colDescription = table.getFieldDescription(icol);
		colType = table.getFieldDataType(icol);
		dataTypeSchema = "STRING"; // default
		if ( colType == TableField.DATA_TYPE_BOOLEAN ) {
			dataTypeSchema = "BOOLEAN";
		}
		else if ( colType == TableField.DATA_TYPE_DATE ) {
			dataTypeSchema = "DATETIME";
		}
		else if ( colType == TableField.DATA_TYPE_DATETIME ) {
			// Figure out the most precise date/time
			int precMin = DateTime.PRECISION_YEAR;
			Object o;
			DateTime dt;
			for ( irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
				try {
					o = table.getFieldValue(irow, icol);
					if ( o != null ) {
						dt = (DateTime)o;
						if ( dt.getPrecision() < precMin ) {
							precMin = dt.getPrecision();
						}
					}
				}
				catch ( Exception e ) {
					// Ignore
				}
			}
			if ( precMin >= DateTime.PRECISION_YEAR ) {
				// Only date
				dataTypeSchema = "DATE";
			}
			else {
				// Date/time
				dataTypeSchema = "DATETIME";
			}
		}
		else if ( colType == TableField.DATA_TYPE_DOUBLE ) {
			dataTypeSchema = "FLOAT";
		}
		else if ( colType == TableField.DATA_TYPE_FLOAT ) {
			dataTypeSchema = "FLOAT";
		}
		else if ( colType == TableField.DATA_TYPE_INT ) {
			dataTypeSchema = "INTEGER";
		}
		else if ( colType == TableField.DATA_TYPE_LONG ) {
			dataTypeSchema = "INTEGER";
		}
		else if ( colType == TableField.DATA_TYPE_SHORT ) {
			dataTypeSchema = "INTEGER";
		}
		if ( icol > 0 ) {
			out.print("," + nl);
		}
		out.print(i3 + "{" + nl);
		out.print(i4 + "\"name\": \"" + colName + "\"" );
		//out.print(i4 + "\"description\": \"" + colDescription + "\"" );
		out.print("," + nl + i4 + "\"type\": \"" + dataTypeSchema + "\"" );
		out.print(nl + i3 + "}");
	}
	out.print ( nl + i2 + "]" + nl + i1 + "}" + nl + "}");
	out.close();
}

/**
Write a table to a delimited file.
@param table Table to write.
@param OutputFile name of file to write.
@param writeHeaderComments indicates whether header comments should be written (some software like Esri ArcGIS
do not handle comments)
@param alwaysQuoteStrings if true, then always surround strings with double quotes; if false strings will only
be quoted when they include the delimiter
@param newlineReplacement if non-null, string to replace newlines in strings when writing the file
@param NaNValue value to write for NaN (null will result in "NaN" being output).
@exception IOException if there is an error writing the file.
*/
private int writeTable ( DataTable table, String OutputFile, boolean writeHeaderComments,
	boolean alwaysQuoteStrings, String newlineReplacement, String NaNValue,
	String outputSchemaFile, String outputSchemaFormat,
	int warning_level, String command_tag, int warning_count )
throws IOException
{	String routine = getClass().getSimpleName() + ".writeTable";
	String message;

	// Clear the output file

	setOutputFile ( null );
	setOutputSchemaFile ( null );

	// Check whether the processor wants output files to be created...

	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();

    // Get the comments to add to the top of the file.

    List<String> outputCommentsList = new ArrayList<String>();
    if ( writeHeaderComments ) {
        // Get the comments to be written at the top of the file
        // Put the standard header at the top of the file
        outputCommentsList = IOUtil.formatCreatorHeader ( "", 80, false );
        // Additional comments to add
        try {
            Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<String> o2 = (List<String>)o;
            	outputCommentsList.addAll(o2);
            }
            // Also add internal comments specific to the table.
            outputCommentsList.addAll ( table.getComments() );
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor - not using.";
            Message.printDebug(10, routine, message );
        }
    }
	
	try {
		Message.printStatus ( 2, routine, "Writing table file \"" + OutputFile + "\"" );
		table.writeDelimitedFile(OutputFile, ",", true, outputCommentsList, "#", alwaysQuoteStrings,
		    newlineReplacement, NaNValue );
		if ( (outputSchemaFile != null) && !outputSchemaFile.isEmpty() ) {
			if ( outputSchemaFormat.equalsIgnoreCase(_GoogleBigQuery) ) {
				writeGoogleBigQueryTableSchema ( table, outputSchemaFile );
			}
			else {
				writeJSONTableSchema ( table, outputSchemaFile );
			}
		}
	}
	catch ( Exception e ) {
		message = "Unexpected error writing table to file \"" + OutputFile + "\" (" + e + ")";
		Message.printWarning ( warning_level, 
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Software error - report problem to support." ) );
	}
	return warning_count;
}

/**
 * Write the table schema file using JSON Table Schema.
 * See:  http://specs.frictionlessdata.io/json-table-schema/
 * @table table to write
 * @param outputSchemaFile path/name of schema file to write
 */
private void writeJSONTableSchema ( DataTable table, String outputSchemaFile ) throws IOException {
	PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter(outputSchemaFile)));
	// Brute force the output.
	// TODO SAM 2017-01-18 move to a general class later
	String nl = System.getProperty("line.separator");
	String i1 = "  ", i2 = "    ", i3 = "      ";
	out.print ( "{" + nl );
	out.print ( i1 + "\"fields\": [" + nl );
	String colName, colDescription, dataTypeSchema;
	int colType;
	int irow;
	TableField field;
	for ( int icol = 0; icol < table.getNumberOfFields(); icol++ ) {
		colName = table.getFieldName(icol);
		// TODO sam 2017-01-18 need to enable
		//colDescription = table.getFieldDescription(icol);
		colType = table.getFieldDataType(icol);
		dataTypeSchema = "string"; // default
		if ( colType == TableField.DATA_TYPE_BOOLEAN ) {
			dataTypeSchema = "boolean";
		}
		else if ( colType == TableField.DATA_TYPE_DATE ) {
			dataTypeSchema = "datetime";
		}
		else if ( colType == TableField.DATA_TYPE_DATETIME ) {
			// Figure out the most precise date/time
			int precMin = DateTime.PRECISION_YEAR;
			Object o;
			DateTime dt;
			for ( irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
				try {
					o = table.getFieldValue(irow, icol);
					if ( o != null ) {
						dt = (DateTime)o;
						if ( dt.getPrecision() < precMin ) {
							precMin = dt.getPrecision();
						}
					}
				}
				catch ( Exception e ) {
					// Ignore
				}
			}
			if ( precMin == DateTime.PRECISION_YEAR ) {
				// Only date
				dataTypeSchema = "gyear";
			}
			else if ( precMin == DateTime.PRECISION_MONTH ) {
				// Only date
				dataTypeSchema = "gyearmonth";
			}
			else if ( precMin == DateTime.PRECISION_DAY ) {
				// Only date
				dataTypeSchema = "date";
			}
			else {
				// Includes time
				dataTypeSchema = "datetime";
			}
		}
		else if ( colType == TableField.DATA_TYPE_DOUBLE ) {
			dataTypeSchema = "number";
		}
		else if ( colType == TableField.DATA_TYPE_FLOAT ) {
			dataTypeSchema = "number";
		}
		else if ( colType == TableField.DATA_TYPE_INT ) {
			dataTypeSchema = "integer";
		}
		else if ( colType == TableField.DATA_TYPE_LONG ) {
			dataTypeSchema = "integer";
		}
		else if ( colType == TableField.DATA_TYPE_SHORT ) {
			dataTypeSchema = "integer";
		}
		if ( icol > 0 ) {
			out.print("," + nl);
		}
		out.print(i2 + "{" + nl);
		out.print(i3 + "\"name\": \"" + colName + "\"" );
		//out.print(i3 + "\"description\": \"" + colDescription + "\"" );
		out.print("," + nl + i3 + "\"type\": \"" + dataTypeSchema + "\"" );
		out.print(nl + i2 + "}");
	}
	out.print ( nl + i1 + "]" + nl + "}");
	out.close();
}

}
