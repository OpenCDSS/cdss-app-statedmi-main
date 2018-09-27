package rti.tscommandprocessor.commands.table;

import javax.swing.JFrame;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the SplitTableRow() command.
*/
public class SplitTableRow_Command extends AbstractCommand implements Command
{

/**
Possible values for DeleteOriginalRow parameter.
*/
protected final String _False = "False";
protected final String _True = "True";
    
/**
Constructor.
*/
public SplitTableRow_Command ()
{	super();
	setCommandName ( "SplitTableRow" );
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
	String TupleColumns = parameters.getValue ( "TupleColumns" );
	String NewTupleColumns = parameters.getValue ( "NewTupleColumns" );
	String MeasureStartColumn = parameters.getValue ( "MeasureStartColumn" );
	String MeasureEndColumn = parameters.getValue ( "MeasureEndColumn" );
	String MeasureIncrement = parameters.getValue ( "MeasureIncrement" );
	String MinimumStartSegmentLength = parameters.getValue ( "MinimumStartSegmentLength" );
	String MinimumEndSegmentLength = parameters.getValue ( "MinimumEndSegmentLength" );
	String DeleteOriginalRow = parameters.getValue ( "DeleteOriginalRow" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }

    if ( ((TupleColumns == null) || TupleColumns.isEmpty()) &&
    	((MeasureStartColumn == null) || MeasureStartColumn.isEmpty()) ) {
    	message = "The tuple or measure parameters must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the tuple or measure parameters." ) );
    }
    else if ( (TupleColumns != null) && !TupleColumns.isEmpty() &&
    	(MeasureStartColumn != null) && !MeasureStartColumn.isEmpty() ) {
    	message = "The tuple or measure parameters must be specified (but not both).";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the tuple or measure parameters." ) );
    }
    else if ( (TupleColumns != null) && !TupleColumns.isEmpty() ) {
    	if ( (NewTupleColumns == null) || NewTupleColumns.isEmpty()) {
	        message = "The new tuple column(s) must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the new tuple column(s)." ) );
	    }
    }
    else if ( (MeasureStartColumn != null) && !MeasureStartColumn.isEmpty() ) {
	    if ( (MeasureEndColumn == null) || (MeasureEndColumn.length() == 0) ) {
	        message = "The measure end column must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the measure end column." ) );
	    }
	    
	    if ( (MeasureIncrement == null) || !StringUtil.isDouble(MeasureIncrement) ) {
	        message = "The measure increment must be specified as a number.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the measure increment as a number." ) );
	    }
	    else {
	    	double measureIncrement = Double.parseDouble(MeasureIncrement);
			// TODO SAM 2015-01-11 expand until the end values for iteration in runCommand()
	    	// so that they are evenly divisible by the increment, within a reasonable tolerance
			if ( measureIncrement > 1.0 ) {
				message = "The measure increment (" + MeasureIncrement + ") is > 1.0";
		        warning += "\n" + message;
		        status.addToLog ( CommandPhaseType.INITIALIZATION,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Specify the measure increment as < 1.0" ) );
			}
	    }
    }
    
    if ( (MinimumStartSegmentLength != null) && !MinimumStartSegmentLength.equals("") && !StringUtil.isDouble(MinimumStartSegmentLength)) {
		message = "The minimum start segment length (" + MinimumStartSegmentLength + ") is invalid";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum start segment length as a number." ) );
    }
    
    if ( (MinimumEndSegmentLength != null) && !MinimumEndSegmentLength.equals("") && !StringUtil.isDouble(MinimumEndSegmentLength)) {
		message = "The minimum end segment length (" + MinimumEndSegmentLength + ") is invalid";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum end segment length as a number" ) );
    }

    if ( (DeleteOriginalRow != null) && (DeleteOriginalRow.length() != 0) && !DeleteOriginalRow.equalsIgnoreCase(_False) &&
        !DeleteOriginalRow.equalsIgnoreCase(_True)) {
        message = "The DeleteOriginalRow parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify DeleteOriginalRow as " + _False + " (default) or " + _True) );
    }
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(12);
    validList.add ( "TableID" );
    validList.add ( "TupleColumns" );
    validList.add ( "TupleDateTimes" );
    validList.add ( "NewTupleColumns" );
    validList.add ( "NewTupleDateTimeColumn" );
    validList.add ( "InsertBeforeColumn" );
    validList.add ( "MeasureStartColumn" );
    validList.add ( "MeasureEndColumn" );
    validList.add ( "MeasureIncrement" );
    validList.add ( "MinimumStartSegmentLength" );
    validList.add ( "MinimumEndSegmentLength" );
    validList.add ( "DeleteOriginalRow" );
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
{	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new SplitTableRow_JDialog ( parent, this, tableIDChoices )).ok();
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
{	String routine = getClass().getSimpleName() + ".runCommandInternal",message = "";
	int warning_level = 2;
	//int log_level = 3; // Level for non-user messages for log file.
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
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

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
	// Do the new column tuples first because it controls the number
	String NewTupleColumns = parameters.getValue ( "NewTupleColumns" );
	if ( (commandPhase == CommandPhaseType.RUN) && (NewTupleColumns != null) && (NewTupleColumns.indexOf("${") >= 0) ) {
		NewTupleColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, NewTupleColumns);
	}
	String [] newTupleColumns = new String[0];
	if ( (NewTupleColumns != null) && !NewTupleColumns.isEmpty() ) {
		newTupleColumns = NewTupleColumns.split(","); // Single tuple
		for ( int i = 0; i < newTupleColumns.length; i++ ) {
			newTupleColumns[i] = newTupleColumns[i].trim();
		}
	}
	String NewTupleDateTimeColumn = parameters.getValue ( "NewTupleDateTimeColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (NewTupleDateTimeColumn != null) && (NewTupleDateTimeColumn.indexOf("${") >= 0) ) {
		NewTupleDateTimeColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, NewTupleDateTimeColumn);
	}
	String TupleColumns = parameters.getValue ( "TupleColumns" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TupleColumns != null) && (TupleColumns.indexOf("${") >= 0) ) {
		TupleColumns = TSCommandProcessorUtil.expandParameterValue(processor, this, TupleColumns);
	}
	String [][] tupleColumns = new String[0][0];
	// Limit to the number of new columns
	if ( (TupleColumns != null) && !TupleColumns.isEmpty() ) {
		String [] temp = TupleColumns.split(";");
		tupleColumns = new String [temp.length][newTupleColumns.length];
		for ( int i = 0; i < temp.length; i++ ) {
			tupleColumns[i] = temp[i].split(",");
			for ( int j = 0; j < tupleColumns[i].length; j++ ) {
				tupleColumns[i][j] = tupleColumns[i][j].trim();
			}
		}
	}
	String TupleDateTimes = parameters.getValue ( "TupleDateTimes" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TupleDateTimes != null) && (TupleDateTimes.indexOf("${") >= 0) ) {
		TupleDateTimes = TSCommandProcessorUtil.expandParameterValue(processor, this, TupleDateTimes);
	}
	String [] tupleDateTimeStrings = new String[0];
	DateTime [] tupleDateTimes = new DateTime[0];
	if ( (TupleDateTimes != null) && !TupleDateTimes.isEmpty() ) {
		tupleDateTimeStrings = TupleDateTimes.split(",");
		tupleDateTimes = new DateTime[tupleDateTimeStrings.length];
		for ( int i = 0; i < tupleDateTimeStrings.length; i++ ) {
			tupleDateTimeStrings[i] = tupleDateTimeStrings[i].trim();
			tupleDateTimes[i] = DateTime.parse(tupleDateTimeStrings[i]);
		}
	}
	String InsertBeforeColumn = parameters.getValue ( "InsertBeforeColumn" );
	if ( (commandPhase == CommandPhaseType.RUN) && (InsertBeforeColumn != null) && (InsertBeforeColumn.indexOf("${") >= 0) ) {
		InsertBeforeColumn = TSCommandProcessorUtil.expandParameterValue(processor, this, InsertBeforeColumn);
	}
    String MeasureStartColumn = parameters.getValue ( "MeasureStartColumn" );
    String MeasureEndColumn = parameters.getValue ( "MeasureEndColumn" );
    String MeasureIncrement = parameters.getValue ( "MeasureIncrement" );
    double measureIncrement = 0.0;
    if ( MeasureIncrement != null ) {
    	measureIncrement = Double.parseDouble(MeasureIncrement);
    }
    String MinimumStartSegmentLength = parameters.getValue ( "MinimumStartSegmentLength" );
    double minimumStartSegmentLength = 0.0; // Indicates no minimum length to include partial segment
    if ( MinimumStartSegmentLength != null ) {
    	minimumStartSegmentLength = Double.parseDouble(MinimumStartSegmentLength);
    }
    String MinimumEndSegmentLength = parameters.getValue ( "MinimumEndSegmentLength" );
    double minimumEndSegmentLength = 0.0; // Indicates no minimum length to include partial segment
    if ( MinimumEndSegmentLength != null ) {
    	minimumEndSegmentLength = Double.parseDouble(MinimumEndSegmentLength);
    }
    String DeleteOriginalRow = parameters.getValue ( "DeleteOriginalRow" );
    boolean deleteOriginalRow = false;
    if ( (DeleteOriginalRow != null) && DeleteOriginalRow.equalsIgnoreCase(_True) ) {
    	deleteOriginalRow = true;
    }
    
    // Get the table to process.

    DataTable table = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (TableID != null) && !TableID.equals("") ) {
            // Get the table to be updated
            request_params = new PropList ( "" );
            request_params.set ( "TableID", TableID );
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
        }
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	try {
		if ( (TupleColumns != null) && !TupleColumns.isEmpty() ) {
			// Process the tuples - first add the new columns so that lookup of input will be correct
			int [] newTupleColNum = new int[newTupleColumns.length];
			int [][] tupleColNum = null;
			int newTupleDateTimeColNum = -1;
			int insertBeforeColNum = -1;
			int oldCol = -1;
			boolean canProcess = true;
			boolean doSetDateTime = false;
			if ( tupleDateTimeStrings.length > 0 ) {
				doSetDateTime = true;
			}
			// If an insert column was requested, get the column number
			if ( (InsertBeforeColumn != null) && !InsertBeforeColumn.isEmpty() ) {
				try {
					insertBeforeColNum = table.getFieldIndex(InsertBeforeColumn);
				}
				catch ( Exception e ) {
					insertBeforeColNum = -1;
		            message = "Table \"" + TableID + "\" does not contain InsertBeforeColumn \"" + InsertBeforeColumn + "\" - adding at end";
		            Message.printWarning ( warning_level,
	    	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
    	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Verify that the table contains the requested column \"" + InsertBeforeColumn + "\"" ) );
				}
			}
			// Add the new date/time column if requested - put this before new tuple columns
			// TODO SAM 2016-05-08 may need a separate insert column to position the date
			if ( (NewTupleDateTimeColumn != null) && !NewTupleDateTimeColumn.isEmpty() ) {
				try {
					newTupleDateTimeColNum = table.getFieldIndex(NewTupleDateTimeColumn);
				}
				catch ( Exception e ) {
		            message = "Table \"" + TableID + "\" does not contain NewTupleDateTimeColumn \"" + NewTupleDateTimeColumn + "\" - adding";
		            Message.printStatus ( 2, routine, message );
		            // Add the column of type date/time
					try {
						newTupleDateTimeColNum = table.addField(insertBeforeColNum,new TableField(TableField.DATA_TYPE_DATETIME, NewTupleDateTimeColumn, -1, -1), null);
						if ( insertBeforeColNum >= 0 ) {
							// Increment so columns are added left to right
							++insertBeforeColNum;
						}
					}
					catch ( Exception e2 ) {
			            message = "Error adding column \"" + NewTupleDateTimeColumn + "\" - cannot process (" + e + ")";
			            Message.printWarning ( warning_level,
		    	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
			            Message.printWarning(3,routine, e2);
	    	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	    	                message, "Check the input" ) );
		    	        canProcess = false;
					}
				}
			}
			// Now add new tuple columns
			for ( int inew = 0; inew < newTupleColumns.length; inew++ ) {
				try {
					newTupleColNum[inew] = table.getFieldIndex(newTupleColumns[inew]);
				}
				catch ( Exception e ) {
		            message = "Table \"" + TableID + "\" does not contain NewTupleColumns \"" + newTupleColumns[inew] + "\" - adding";
		            Message.printStatus ( 2, routine, message );
		            // Add the column with the same type as the matched column of the first existing tuple
		            // Have to look up the column since it is normally only determined later
					try {
						oldCol = table.getFieldIndex(tupleColumns[0][inew]);
					}
					catch ( Exception e2 ) {
			            message = "Table \"" + TableID + "\" does not contain TupleColumns \"" + tupleColumns[0][inew] +
			            	"\" to determine NewTupleColumns column type - cannot process (" + e2 + ")";
			            Message.printWarning ( warning_level,
		    	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
			            Message.printWarning(3,routine, e2);
	    	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	    	                message, "Verify that the table contains the requested input column \"" + tupleColumns[0][0] + "\"" ) );
	    	            canProcess = false;
					}
					if ( canProcess ) {
						try {
							TableField oldField = table.getTableField(oldCol);
							newTupleColNum[inew] = table.addField(insertBeforeColNum,
								new TableField(oldField.getDataType(), newTupleColumns[inew], oldField.getWidth(), oldField.getPrecision()), null);
							if ( insertBeforeColNum >= 0 ) {
								// Increment so columns are added left to right
								++insertBeforeColNum;
							}
						}
						catch ( Exception e2 ) {
				            message = "Table \"" + TableID + "\" error adding NewTupleColumns \"" + newTupleColumns[inew] + "\" - cannot process (" + e2 + ")";
				            Message.printWarning ( warning_level,
			    	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
				            Message.printWarning(3,routine, e2);
		    	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
		    	                message, "Verify command parameters" ) );
		    	            canProcess = false;
						}
					}
				}
			}
			// All new columns have been added so get the columns for the old tuples
			tupleColNum = new int[tupleColumns.length][tupleColumns[0].length];
			for ( int ituple = 0; ituple < tupleColumns.length; ituple++ ) {
				for ( int ituplePart = 0; ituplePart < tupleColumns[ituple].length; ituplePart++ ) {
					try {
						tupleColNum[ituple][ituplePart] = table.getFieldIndex(tupleColumns[ituple][ituplePart]);
					}
					catch ( Exception e ) {
			            message = "Table \"" + TableID + "\" does not contain TupleColumns \"" + tupleColumns[ituple][ituplePart] + "\" - cannot process (" + e + ")";
			            Message.printWarning ( warning_level,
		    	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	    	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	    	                message, "Verify that the table contains the requested input column \"" + tupleColumns[ituple][ituplePart] + "\"" ) );
	    	            canProcess = false;
					}
				}
			}
			if ( canProcess ) {
				// If here can continue with processing
				int nrec = table.getNumberOfRecords();
				int [] rowsToDelete = new int[nrec]; // Rows to delete (may add functionality later that makes this more necessary)
				for ( int i = 0; i < nrec; i++ ) {
					rowsToDelete[i] = -1;
				}
				for ( int irec = 0; irec < nrec; irec++ ) {
					Message.printStatus(2, routine, "Processing record " + irec );
					// Loop through the tuples
					for ( int ituple = 0; ituple < tupleColumns.length; ituple++ ) {
						// Add a new row to the table for each tuple.
						// For now add at the end (TODO SAM 2016-05-09) evaluate whether to interleave (complicates iteration and deletes)
						TableRecord rec = table.getRecord(irec);
						TableRecord newRec = new TableRecord(rec);
						// Copy from the old tuples to new
						for ( int ituplePart = 0; ituplePart < tupleColumns[ituple].length; ituplePart++ ) {
							// Next set the values in the new record
							newRec.setFieldValue(newTupleColNum[ituplePart], rec.getFieldValue(tupleColNum[ituple][ituplePart]));
							if ( newTupleDateTimeColNum >= 0 ) {
								// Also set the date/time for the data
								newRec.setFieldValue(newTupleDateTimeColNum, tupleDateTimes[ituple]);
							}
						}
						// Clear out the original tuples to avoid confusion - have to do for all tuples
						/* TODO SAM 2016-05-09 Leave in for now for bread crumb trail - add parameter to delete
						for ( int itupleClear = 0; itupleClear < tupleColumns.length; itupleClear++ ) {
							for ( int ituplePartClear = 0; ituplePartClear < tupleColumns[ituple].length; ituplePartClear++ ) {
								newRec.setFieldValue(tupleColNum[itupleClear][ituplePartClear], null);
							}
						}
						*/
						// Now add the record to the table
						table.addRecord(newRec);
					}
					// Keep track of rows to delete
					if ( deleteOriginalRow ) {
						rowsToDelete[irec] = irec;
					}
				}
				// Now delete table rows, from largest to smallest
				for ( int irec = (nrec - 1); irec >= 0; irec-- ) {
					if ( rowsToDelete[irec] >= 0 ) {
						table.deleteRecord(irec);
					}
				}
			}
		}
		else if ( (MeasureStartColumn != null) && !MeasureStartColumn.isEmpty() ) {
	    	// Make sure that the table has the columns for the measures
			int startCol = -1;
			try {
				startCol = table.getFieldIndex(MeasureStartColumn);
			}
			catch ( Exception e ) {
	            message = "Table \"" + TableID + "\" does not contain measure start column \"" + MeasureStartColumn + "\"";
	            Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the table contains the requested start column \"" + MeasureStartColumn + "\"" ) );
			}
			int endCol = -1;
			try {
				endCol = table.getFieldIndex(MeasureEndColumn);
			}
			catch ( Exception e ) {
	            message = "Table \"" + TableID + "\" does not contain measure end column \"" + MeasureStartColumn + "\"";
	            Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the table contains the requested end column \"" + MeasureEndColumn + "\"" ) );
			}
			if ( (startCol >= 0) && (endCol >= 0) ) {
				// Loop through the table to process all of the rows
				int nrec = table.getNumberOfRecords();
				Double startVal, endVal;
				Object val;
				int numRowsAdded = 0;
				for ( int irec = 0; irec < nrec; irec++ ) {
					Message.printStatus(2, routine, "Processing record " + irec );
					numRowsAdded = 0;
					// Get the values for the start and end measure values
					val = table.getFieldValue(irec, startCol);
					if ( (val == null) || !(val instanceof Double) ) {
						continue;
					}
					startVal = (Double)val;
					val = table.getFieldValue(irec, endCol);
					if ( (val == null) || !(val instanceof Double) ) {
						continue;
					}
					endVal = (Double)val;
					// If the start and end are reversed, switch here
					if ( startVal > endVal ) {
						Double tmp = endVal;
						endVal = startVal;
						startVal = tmp;
					}
					// Find the closest integer points to the start and end
					double floor = Math.floor(startVal);
					double ceil = Math.ceil(endVal);
					// Insert new rows after the current row
					double segEnd;
					boolean addSeg = false;
					for ( double segStart = floor; segStart <= ceil; segStart += measureIncrement ) {
						addSeg = false;
						segEnd = segStart + measureIncrement;
						if ( segEnd <= startVal ) {
							// Right edge of first segment is right at boundary but not straddling yet
							continue;
						}
						else if ( segStart >= endVal ) {
							// Done with all segments - check whether need to delete original row and reposition iterator
							if ( deleteOriginalRow ) {
								// Delete the original row and set the row appropriately for the loop iterator
								table.deleteRecord(irec);
								Message.printStatus(2,routine,"Deleted original record " + irec );
								--irec;
								nrec = table.getNumberOfRecords();
								Message.printStatus(2,routine,"Reset record due to deletion, irec=" + irec + ", nrec=" + nrec );
							}
							// Position the record pointer so that the next original record is processed
							irec += numRowsAdded;
							Message.printStatus(2,routine,"Reset record due to " + numRowsAdded + " rows added previously, irec=" + irec );
							break;
						}
						else if ( (startVal >= segStart ) && (segEnd > startVal) ) {
							// First segment straddles start, check whether to include
							if ( (segEnd - startVal) >= minimumStartSegmentLength ) {
								addSeg = true;
							}
							else {
								Message.printStatus(2, routine, "Not adding segment because length " + (segEnd - startVal) + " < " + minimumStartSegmentLength );
							}
						}
						else if ( (segStart < endVal) && (segEnd >= endVal) ) {
							// Last segment straddles end, check whether to include
							if ( (endVal - segStart) >= minimumEndSegmentLength ) {
								addSeg = true;
							}
							else {
								Message.printStatus(2, routine, "Not adding segment because length " + (endVal - segStart) + " < " + minimumEndSegmentLength );
							}
						}
						else {
							// Segment fully in the reach so add
							addSeg = true;
						}
						if ( addSeg ) {
							// Add the segment
							// First copy the original row contents
							TableRecord newRec = new TableRecord(table.getRecord(irec));
							// Next set the values in the new record
							newRec.setFieldValue(startCol, new Double(segStart));
							newRec.setFieldValue(endCol, new Double(segEnd));
							int irecInsert = irec + numRowsAdded + 1;
							table.insertRecord(irecInsert, newRec, true);
							++numRowsAdded;
							nrec = table.getNumberOfRecords();
							Message.printStatus(2, routine, "Inserting segment record " + irecInsert + ", segStart=" + segStart + ", segEnd=" + segEnd +
								", numRowsAdded=" + numRowsAdded + ", nrec=" + nrec );
						}
					}
				}
			}
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error splitting table row (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
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
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
	String DeleteOriginalRow = props.getValue( "DeleteOriginalRow" );
    String TupleColumns = props.getValue( "TupleColumns" );
    String TupleDateTimes = props.getValue( "TupleDateTimes" );
    String NewTupleColumns = props.getValue( "NewTupleColumns" );
    String NewTupleDateTimeColumn = props.getValue( "NewTupleDateTimeColumn" );
    String InsertBeforeColumn = props.getValue( "InsertBeforeColumn" );
    String MeasureStartColumn = props.getValue( "MeasureStartColumn" );
    String MeasureEndColumn = props.getValue( "MeasureEndColumn" );
	String MeasureIncrement = props.getValue( "MeasureIncrement" );
	String MinimumStartSegmentLength = props.getValue( "MinimumStartSegmentLength" );
	String MinimumEndSegmentLength = props.getValue( "MinimumEndSegmentLength" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (TupleColumns != null) && (TupleColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TupleColumns=\"" + TupleColumns + "\"" );
    }
    if ( (TupleDateTimes != null) && (TupleDateTimes.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TupleDateTimes=\"" + TupleDateTimes + "\"" );
    }
    if ( (NewTupleColumns != null) && (NewTupleColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NewTupleColumns=\"" + NewTupleColumns + "\"" );
    }
    if ( (NewTupleDateTimeColumn != null) && (NewTupleDateTimeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "NewTupleDateTimeColumn=\"" + NewTupleDateTimeColumn + "\"" );
    }
    if ( (InsertBeforeColumn != null) && (InsertBeforeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InsertBeforeColumn=\"" + InsertBeforeColumn + "\"" );
    }
    if ( (MeasureStartColumn != null) && (MeasureStartColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MeasureStartColumn=\"" + MeasureStartColumn + "\"" );
    }
    if ( (MeasureEndColumn != null) && (MeasureEndColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MeasureEndColumn=\"" + MeasureEndColumn + "\"" );
    }
	if ( (MeasureIncrement != null) && (MeasureIncrement.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MeasureIncrement=" + MeasureIncrement );
	}
    if ( (MinimumStartSegmentLength != null) && (MinimumStartSegmentLength.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MinimumStartSegmentLength=\"" + MinimumStartSegmentLength + "\"" );
    }
    if ( (MinimumEndSegmentLength != null) && (MinimumEndSegmentLength.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MinimumEndSegmentLength=\"" + MinimumEndSegmentLength + "\"" );
    }
    if ( (DeleteOriginalRow != null) && (DeleteOriginalRow.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DeleteOriginalRow=\"" + DeleteOriginalRow + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}