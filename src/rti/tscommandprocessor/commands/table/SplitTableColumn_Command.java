// SplitTableColumn_Command - This class initializes, checks, and runs the SplitTableRowIntoSequenceColumn() command.

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

/**
This class initializes, checks, and runs the SplitTableRowIntoSequenceColumn() command.
*/
public class SplitTableColumn_Command extends AbstractCommand implements Command
{

/**
Possible values for OutputColumnOrder parameter.
*/
protected final String _SequenceOrder = "SequenceOrder";
protected final String _UniqueValues = "UniqueValues";
protected final String _UniqueValuesSorted = "UniqueValuesSorted";

/**
Possible values for DeleteOriginalColumn parameter.
*/
protected final String _False = "False";
protected final String _True = "True";
    
/**
Constructor.
*/
public SplitTableColumn_Command ()
{	super();
	setCommandName ( "SplitTableColumn" );
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
	String ColumnToSplit = parameters.getValue ( "ColumnToSplit" );
	String Delimiter = parameters.getValue ( "Delimiter" );
	String OutputColumns = parameters.getValue ( "OutputColumns" );
	String OutputColumnOrder = parameters.getValue ( "OutputColumnOrder" );
	String DeleteOriginalColumn = parameters.getValue ( "DeleteOriginalColumn" );
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
    
    if ( (ColumnToSplit == null) || (ColumnToSplit.length() == 0) ) {
        message = "The column to split must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the column to split." ) );
    }
    
    if ( (Delimiter == null) || (Delimiter.length() == 0) ) {
        message = "The delimiter must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the delimiter to split the column." ) );
    }
    
    if ( (OutputColumns == null) || (OutputColumns.length() == 0) ) {
        message = "The output columns must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the measure increment as a number." ) );
    }

    if ( (OutputColumnOrder != null) && (OutputColumnOrder.length() != 0) && !OutputColumnOrder.equalsIgnoreCase(_SequenceOrder) &&
        !OutputColumnOrder.equalsIgnoreCase(_UniqueValues) && !OutputColumnOrder.equalsIgnoreCase(_UniqueValuesSorted)) {
        message = "The OutputColumnOrder parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify OutputColumnOrder as " + _SequenceOrder + " (default), " + _UniqueValues + ", or " + _UniqueValuesSorted) );
    }

    if ( (DeleteOriginalColumn != null) && (DeleteOriginalColumn.length() != 0) && !DeleteOriginalColumn.equalsIgnoreCase(_False) &&
        !DeleteOriginalColumn.equalsIgnoreCase(_True)) {
        message = "The DeleteOriginalColumn parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify DeleteOriginalColumn as " + _False + " (default) or " + _True) );
    }
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(7);
    validList.add ( "TableID" );
    validList.add ( "ColumnToSplit" );
    validList.add ( "Delimiter" );
    validList.add ( "OutputColumns" );
    validList.add ( "OutputColumnOrder" );
    validList.add ( "InsertBeforeColumn" );
    validList.add ( "DeleteOriginalColumn" );
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
	return (new SplitTableColumn_JDialog ( parent, this, tableIDChoices )).ok();
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
{	String routine = getClass().getSimpleName() + ".runCommandInternal", message = "";
	int warning_level = 2;
	//int log_level = 3; // Level for non-user messages for log file.
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    status.clearLog(command_phase);

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String TableID = parameters.getValue ( "TableID" );
    String ColumnToSplit = parameters.getValue ( "ColumnToSplit" );
    String Delimiter = parameters.getValue ( "Delimiter" );
    String OutputColumns = parameters.getValue ( "OutputColumns" );
    String OutputColumnOrder = parameters.getValue ( "OutputColumnOrder" );
    // TODO SAM 2015-02-06 consider an enum but booleans are fast for now
    boolean outOrderSeq = true; // Default
    boolean outOrderUnique = false;
    boolean outOrderUniqueSorted = false;
    if ( OutputColumnOrder != null ) {
    	if ( OutputColumnOrder.equalsIgnoreCase(_UniqueValues) ) {
    		outOrderSeq = false;
    		outOrderUnique = true;
    	}
    	else if ( OutputColumnOrder.equalsIgnoreCase(_UniqueValuesSorted) ) {
    		outOrderSeq = false;
    		outOrderUniqueSorted = true;
    	}
    }
    String InsertBeforeColumn = parameters.getValue ( "InsertBeforeColumn" );
    String DeleteOriginalColumn = parameters.getValue ( "DeleteOriginalColumn" );
    boolean deleteOriginalColumn = false;
    if ( (DeleteOriginalColumn != null) && DeleteOriginalColumn.equalsIgnoreCase(_True) ) {
    	deleteOriginalColumn = true;
    }
     
    // Get the table to process.

    DataTable table = null;
    if ( command_phase == CommandPhaseType.RUN ) {
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
		boolean okToProcess = true;
    	// Make sure that the table has the input column
		int splitCol = -1;
		try {
			splitCol = table.getFieldIndex(ColumnToSplit);
		}
		catch ( Exception e ) {
            message = "Table \"" + TableID + "\" does not contain column to split \"" + ColumnToSplit + "\"";
            Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the table contains the requested column to split \"" + ColumnToSplit + "\"" ) );
            okToProcess = false;
		}
	   	// Make sure that the table has the "insert before" column
		int insertBeforeColumnNum = -1;
		if ( (InsertBeforeColumn != null) && !InsertBeforeColumn.equals("") ) {
			try {
				insertBeforeColumnNum = table.getFieldIndex(InsertBeforeColumn);
			}
			catch ( Exception e ) {
	            message = "Table \"" + TableID + "\" does not contain column to insert before \"" + InsertBeforeColumn + "\"";
	            Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the table contains the requested column to insert before \"" + InsertBeforeColumn + "\"" ) );
	            okToProcess = false;
			}
		}
		
		if ( okToProcess ) {
			// Make sure that the input table is a string
			if ( table.getFieldDataType(splitCol) != TableField.DATA_TYPE_STRING ) {
	            message = "Table \"" + TableID + "\" column to split \"" + ColumnToSplit + "\" is not a string column";
	            Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the column to split is a string column.") );
	            okToProcess = false;
			}
		}

		int numOutputColumns = 0;
		int nrec = table.getNumberOfRecords();
		TableRecord rec;
		String val;
		List<String> outputColumns = new ArrayList<String>();
		int [] outputColumnNum = new int [0];
		List<String> uniqueValueList = new ArrayList<String>();
		if ( okToProcess ) {
			// Need to know how many columns maximum there are in the data because that impacts the column names
			// Loop through the table one time and split the data to determine unique values and maximum sequence length
			String parts[];
			String partTrimmed;
			int maxParts = 0;
			for ( int irec = 0; irec < nrec; irec++ ) {
				rec = table.getRecord(irec);
				val = rec.getFieldValueString(splitCol);
				if ( val == null ) {
					continue;
				}
				parts = val.split(Delimiter);
				if ( parts.length > maxParts ) {
					maxParts = parts.length;
				}
				// Check on unique values
				for ( int i = 0; i < parts.length; i++ ) {
					boolean found = false;
					partTrimmed = parts[i].trim();
					for ( int j = 0; j < uniqueValueList.size(); j++ ) {
						if ( partTrimmed.equalsIgnoreCase(uniqueValueList.get(j)) ) {
							found = true;
							break;
						}
					}
					if ( !found ) {
						uniqueValueList.add(partTrimmed);
					}
				}
			}
			// Now figure out the number of output columns
			if ( outOrderSeq ) {
				numOutputColumns = maxParts;
				Message.printStatus(2, routine, "Number of output columns set to " + numOutputColumns + " maximum data values in split column.");
			}
			else {
				numOutputColumns = uniqueValueList.size();
				Message.printStatus(2, routine, "Number of output columns set to " + numOutputColumns + " unique values.");
			}
			if ( outOrderUniqueSorted ) {
				uniqueValueList = StringUtil.sortStringList(uniqueValueList);
			}
		   
		    // Determine the output column names
		    parts = OutputColumns.split(",");
		    for ( int i = 0; i < parts.length; i++ ) {
		    	if ( parts[i].indexOf("[") >= 0 ) {
		    		// Handle [range] notation
		    	    int parenPos1 = parts[i].indexOf("[");
	                int parenPos2 = parts[i].indexOf("]");
		    		String prefix = "";
		    		if ( parenPos1 >= 1 ) {
		    			prefix = parts[i].substring(0,parenPos1);
		    		}
		    		String suffix = "";
		    		if ( parenPos2 < (parts[i].length() - 1) ) {
		    			suffix = parts[i].substring(parenPos2 + 1);
		    		}
	                if ( (parenPos1 >= 0) && (parenPos2 >= 0) ) {
	                    // Need to interpret slice of field numbers in file
	                    String slice = parts[i].substring((parenPos1 + 1),parenPos2);
	                    int [] sliceNumbers = StringUtil.parseIntegerSlice( slice, ":", 0, numOutputColumns );
	                    Message.printStatus(2, routine, "Got " + sliceNumbers.length + " columns from slice \"" + slice + "\"" );
	                    for ( int ipos = 0; ipos < sliceNumbers.length; ipos++ ) {
	                        // Positions from parameter parsing are 1+ so need to decrement to get 0+ indices
	                    	Message.printStatus(2, routine, "Adding output column from sequence \"" + prefix + sliceNumbers[ipos] + suffix + "\"");
	                        outputColumns.add ( prefix + sliceNumbers[ipos] + suffix );
	                    }
	                }
		    	}
		    	else {
		    		// Just add the column
		    		Message.printStatus(2, routine, "Adding specified output column \"" + parts[i] + "\"");
		    		outputColumns.add(parts[i]);
		    	}
		    }
		    // Make sure that there are at least enough output columns to receive the split values
		    if ( outOrderSeq ) {
			    if ( outputColumns.size() > maxParts ) {
			    	// OK to have extras because using sequence notation may result in extra numbered columns
			    	// TODO SAM 2015-02-07 figure out if can handle explicit columns and numbered columns precisely without extras
			    	for ( int i = outputColumns.size() - 1; i >= maxParts; i-- ) {
			    		outputColumns.remove(i);
			    	}
			    }
			    if ( outputColumns.size() < maxParts ) {
		            message = "Table \"" + TableID + "\" maximum number of parts in column to split (" + maxParts + ") does not equal the number of output columns (" +
		            	outputColumns.size() + ")";
		            Message.printWarning ( warning_level,
		            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
		            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Verify that the number of output columns is correct.") );
			    	okToProcess = false;
			    }
		    }
		    else if ( outOrderUnique || outOrderUniqueSorted ) {
			    if ( outputColumns.size() > uniqueValueList.size() ) {
			    	// OK to have extras because using sequence notation may result in extra numbered columns
			    	// TODO SAM 2015-02-07 figure out if can handle explicit columns and numbered columns precisely without extras
			    	for ( int i = outputColumns.size() - 1; i >= uniqueValueList.size(); i-- ) {
			    		outputColumns.remove(i);
			    	}
			    }
			    if ( outputColumns.size() < uniqueValueList.size() ) {
		            message = "Table \"" + TableID + "\" number of unique values (" + uniqueValueList.size() + ") does not equal the number of output columns (" +
		            	outputColumns.size() + ")";
		            Message.printWarning ( warning_level,
		            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
		            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Verify that the number of output columns is correct.") );
			    	okToProcess = false;
			    }
		    }
		}
		
		if ( okToProcess ) {
		    // Add the new output columns and save the column positions
		    outputColumnNum = new int[outputColumns.size()];
		    int icol = -1;
		    for ( String outputColumn : outputColumns ) {
		    	 ++icol;
		    	 if ( insertBeforeColumnNum < 0 ) {
		    		 table.addField( new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
		    	 }
		    	 else {
		    		 table.addField((insertBeforeColumnNum + icol), new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
		    	 }
		         try {
		             outputColumnNum[icol] = table.getFieldIndex(outputColumn);
		             Message.printStatus(2,routine,"Output column \"" + outputColumn + "\" column number = " + outputColumnNum[icol] );
		             if ( outOrderUnique || outOrderUniqueSorted ) {
		            	 Message.printStatus(2,routine,"Unique value[" + icol + "] = \"" + uniqueValueList.get(icol) + "\"");
		             }
		         }
		         catch ( Exception e2 ) {
		             // Should not happen.
		        	 okToProcess = false;
		         }
		    }
		    
		    // Recalculate the split column number because it may have been shifted from above
			try {
				splitCol = table.getFieldIndex(ColumnToSplit);
			}
			catch ( Exception e ) {
	            message = "Table \"" + TableID + "\" does not contain column to split \"" + ColumnToSplit + "\"";
	            Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
	            status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that the table contains the requested column to split \"" + ColumnToSplit + "\"" ) );
	            okToProcess = false;
			}
		}

		if ( okToProcess ) {
			String [] parts;
			String partTrimmed;
			// Loop through the table again, parse the data again and set the values in the output
			for ( int irec = 0; irec < nrec; irec++ ) {
				rec = table.getRecord(irec);
				val = rec.getFieldValueString(splitCol);
				if ( val == null ) {
					// Leave column values as is
					continue;
				}
				parts = val.split(Delimiter);
				for ( int i = 0; i < parts.length; i++ ) {
					partTrimmed = parts[i].trim();
					if ( outOrderSeq ) {
						// Insert the part in the order it is found
						rec.setFieldValue(outputColumnNum[i], partTrimmed);
					}
					else if ( outOrderUnique || outOrderUniqueSorted ) {
						// Loop through the unique values to match and then set in corresponding output column position
						for ( int j = 0; j < uniqueValueList.size(); j++ ) {
							if ( partTrimmed.equalsIgnoreCase(uniqueValueList.get(j)) ) {
								rec.setFieldValue(outputColumnNum[j], partTrimmed);
								break;
							}
						}
					}
				}
			}
			
			if ( deleteOriginalColumn ) {
				// Delete the original column
				table.deleteField(splitCol);
			}
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error splitting table column (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
    String ColumnToSplit = props.getValue( "ColumnToSplit" );
    String Delimiter = props.getValue( "Delimiter" );
	String OutputColumns = props.getValue( "OutputColumns" );
	String OutputColumnOrder = props.getValue( "OutputColumnOrder" );
    String InsertBeforeColumn = props.getValue( "InsertBeforeColumn" );
	String DeleteOriginalColumn = props.getValue( "DeleteOriginalColumn" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (ColumnToSplit != null) && (ColumnToSplit.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnToSplit=\"" + ColumnToSplit + "\"" );
    }
    if ( (Delimiter != null) && (Delimiter.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Delimiter=\"" + Delimiter + "\"" );
    }
	if ( (OutputColumns != null) && (OutputColumns.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputColumns=\"" + OutputColumns +"\"" );
	}
    if ( (OutputColumnOrder != null) && (OutputColumnOrder.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputColumnOrder=" + OutputColumnOrder );
    }
    if ( (InsertBeforeColumn != null) && (InsertBeforeColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "InsertBeforeColumn=\"" + InsertBeforeColumn + "\"" );
    }
    if ( (DeleteOriginalColumn != null) && (DeleteOriginalColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DeleteOriginalColumn=" + DeleteOriginalColumn );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
