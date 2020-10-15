// TranslateCropPatternTS_Command - This class initializes, checks, and runs the TranslateCropPatternTS() command.

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

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropPatternTS;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the TranslateCropPatternTS() command.
*/
public class TranslateCropPatternTS_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public TranslateCropPatternTS_Command ()
{	super();
	setCommandName ( "TranslateCropPatternTS" );
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
{	String routine = getClass().getSimpleName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String OldCropType = parameters.getValue ( "OldCropType" );
	String NewCropType = parameters.getValue ( "NewCropType" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
    if ( (ListFile != null) && (ListFile.length() != 0) ) {
    	String working_dir = null;
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
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify an existing input file." ) );
            }
    
        try {
            String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, ListFile));
			if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing file (may be OK if created during processing)." ) );
			}
        }
        catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + ListFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
        }
		if ( (IDCol == null) ||	(IDCol.length() == 0) ) {
	        message = "The ID column must be specified if the list file is specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the ID column (1+) to read." ) );
		}
		else if ( !StringUtil.isInteger(IDCol) ) {
	        message = "The ID column (" + IDCol + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the ID column as a number >= 1." ) );
		}
	    if ( (ID != null) && (ID.length() != 0) ) {
	        message = "The location identifier should not be specified when the list file is specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Do not specify the location identifier." ) );
		}
    }
    else {
		if ( (IDCol != null) &&	(IDCol.length() != 0) ) {
	        message = "The ID column is not needed unless the list file is specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.WARNING,
                    message, "Specify the ID column (1+) to read." ) );
		}
	    if ( (ID == null) || (ID.length() == 0) ) {
	        message = "The location identifier must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify the location identifier." ) );
		}
    }
    
    if ( (OldCropType == null) || (OldCropType.length() == 0) ) {
        message = "The old crop type must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the old crop type to change." ) );
	}
    
    if ( (NewCropType == null) || (NewCropType.length() == 0) ) {
        message = "The new crop type must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the new crop type." ) );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> validList = new Vector<>(6);
	validList.add ( "ID" );
	validList.add ( "ListFile" );
	validList.add ( "IDCol" );
	validList.add ( "OldCropType" );
	validList.add ( "NewCropType" );
	validList.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ),
			warning );
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
	return (new TranslateCropPatternTS_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    String ListFile = parameters.getValue ( "ListFile" );
    String IDCol = parameters.getValue ( "IDCol" );
    String OldCropType = parameters.getValue ( "OldCropType" );
    String NewCropType = parameters.getValue ( "NewCropType" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	String Delim = null;
	if ( Delim == null ) {
		Delim = ",";	// Default
	}

    // Get columns, all zero offset
    
    int IDCol_int = -1;
    if ( IDCol != null ) {
        IDCol_int = Integer.parseInt(IDCol) - 1;
    }
    
    // Get the data needed for the command
    
    List<StateCU_CropPatternTS> cdsList = null;
    int cdsListSize = 0;
    try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents ( "StateCU_CropPatternTS_List" );
		cdsList = dataList;
		cdsListSize = cdsList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting crop pattern time series (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    String ListFile_full = ListFile;
    if ( ListFile_full != null ) {
	    ListFile_full = IOUtil.verifyPathForOS(
	        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
	
		if ( !IOUtil.fileExists(ListFile_full) ) {
			message = "List file \"" + ListFile_full + "\" does not exist.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
		}
		if ( !IOUtil.fileReadable(ListFile_full) ) {
			message = "List file \"" + ListFile_full + "\" is not readable.";
	        Message.printWarning ( warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
		}
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	DataTable table = null;
    	int tsize = 0;
    	if ( ListFile_full != null ) {
	        Message.printStatus ( 2, routine, "Reading CU locations list file \"" + ListFile_full + "\"" );
	
	    	// Read using the table...
	
	    	PropList props = new PropList ("");
	    	props.set ( "Delimiter=," );		// see existing prototype
	    	props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
	    	props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
	    	try {
	    		table = DataTable.parseFile ( ListFile_full, props );
	    		tsize = table.getNumberOfRecords();
	    	}
	    	catch ( Exception e ) {
	    		message = "Unexpected error parsing list file \"" + ListFile_full + "\".";
	            Message.printWarning ( warning_level,
	                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify the format of the file."));
	            Message.printWarning ( 3, routine, e );
	            throw new CommandException ( message );
	    	}
	    	
	    	// Make sure that requested columns are available
	    	
	    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	}
    	
    	int matchCount = 0;
    	StateCU_CropPatternTS cds = null;
    	String cds_id, id;
    	TableRecord rec = null;
    	boolean match = false;
    	for (int i = 0; i < cdsListSize; i++ ) {
    		cds = (StateCU_CropPatternTS)cdsList.get(i);
    		cds_id = cds.getID();
    		// If the list file was specified, only change matching
    		// locations.  Loop through the table and see if there are any matches for the ID...
    		if ( ListFile != null ) {
    			match = false;
    			for (int j = 0; j < tsize; j++) {
    				rec = table.getRecord(j);
    				id = (String)rec.getFieldValue(IDCol_int);
    				if ( id.equalsIgnoreCase(cds_id) ) {
    					match = true;
    					break;
    				}
    			}
    			if ( !match ) {
    				// No match so don't process...
    				continue;
    			}
    		}
    		else if ( ID != null ) {
    			// See if the ID matches that for the time series.
    			if ( !cds_id.matches(idpattern_Java) ) {
    				// Identifier does not match...
    				continue;
    			}
    		}
    		// If here it is OK to process the CU Location's crop pattern time series data...
    		++matchCount;
    		cds.translateCropName ( OldCropType, NewCropType );
    	}

    	// If nothing was matched, take other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No CU locations were matched: warning and not translating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No CU locations were matched: failing and not translating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifiers are correct." ) );
			}
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error tranlating crop pattern time series crop types (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String ID = parameters.getValue ( "ID" );
	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String OldCropType = parameters.getValue ( "OldCropType" );
	String NewCropType = parameters.getValue ( "NewCropType" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( (IDCol != null) && (IDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDCol=" + IDCol );
	}
	if ( (OldCropType != null) && (OldCropType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OldCropType=\"" + OldCropType + "\"" );
	}
	if ( (NewCropType != null) && (NewCropType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewCropType=\"" + NewCropType + "\"" );
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
