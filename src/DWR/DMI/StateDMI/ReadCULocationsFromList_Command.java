// ReadCULocationsFromList_Command - This class initializes, checks, and runs the ReadCULocationsFromList() command.

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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateCU.StateCU_Location;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
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
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class initializes, checks, and runs the ReadCULocationsFromList() command.
*/
public class ReadCULocationsFromList_Command 
extends AbstractCommand implements Command
{
	
/**
Constructor.
*/
public ReadCULocationsFromList_Command ()
{	super();
	setCommandName ( "ReadCULocationsFromList" );
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
	//String ID = parameters.getValue ( "ID" );
	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String LatitudeCol = parameters.getValue ( "LatitudeCol" );
	String ElevationCol = parameters.getValue ( "ElevationCol" );
	String Region1Col = parameters.getValue ( "Region1Col" );
	String Region2Col = parameters.getValue ( "Region2Col" );
	String AWCCol = parameters.getValue ( "AWCCol" );
	String Top = parameters.getValue ( "Top" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
    if ( (ListFile == null) || (ListFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
    }
    else {
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
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
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
    }
	if ( (IDCol == null) ||	(IDCol.length() == 0) ) {
        message = "The ID column must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column (1+) to read." ) );
	}
	else if ( !StringUtil.isInteger(IDCol) ) {
        message = "The ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column as a number >= 1." ) );
	}
	
	if ( (NameCol != null) && (NameCol.length() != 0) && !StringUtil.isInteger(NameCol) ) {
        message = "The name column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the name column as a number >= 1." ) );
	}
	
	if ( (LatitudeCol != null) && (LatitudeCol.length() != 0) && !StringUtil.isInteger(LatitudeCol) ) {
        message = "The latitude column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the latitude column as a number >= 1." ) );
	}
	
	if ( (ElevationCol != null) && (ElevationCol.length() != 0) && !StringUtil.isInteger(ElevationCol) ) {
        message = "The elevation column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the elevation column as a number >= 1." ) );
	}
	
	if ( (Region1Col != null) && (Region1Col.length() != 0) && !StringUtil.isInteger(Region1Col) ) {
        message = "The region 1 column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the region 1 column as a number >= 1." ) );
	}
	
	if ( (Region2Col != null) && (Region2Col.length() != 0) && !StringUtil.isInteger(Region2Col) ) {
        message = "The region 2 column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the region 2 column as a number >= 1." ) );
	}
	
	if ( (AWCCol != null) && (AWCCol.length() != 0) && !StringUtil.isInteger(AWCCol) ) {
        message = "The AWC column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the AWC column as a number >= 1." ) );
	}
	
	if ( (Top != null) && (Top.length() != 0) && !StringUtil.isInteger(Top) ) {
        message = "The Top parameter is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the Top parameter as a number >= 1." ) );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(9);
	validList.add ( "ListFile" );
	validList.add ( "IDCol" );
	validList.add ( "NameCol" );
	validList.add ( "LatitudeCol" );
	validList.add ( "ElevationCol" );
	validList.add ( "Region1Col" );
	validList.add ( "Region2Col" );
	validList.add ( "AWCCol" );
	validList.add ( "Top" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, warning_level ),
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
	return (new ReadCULocationsFromList_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

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
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
*/
private void runCommandInternal ( int command_number, CommandPhaseType command_phase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandStatus status = getCommandStatus();
    status.clearLog(command_phase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
    String IDCol = parameters.getValue ( "IDCol" );
    String NameCol = parameters.getValue ( "NameCol" );
    String LatitudeCol = parameters.getValue ( "LatitudeCol" );
    String ElevationCol = parameters.getValue ( "ElevationCol" );
    String Region1Col = parameters.getValue ( "Region1Col" );
    String Region2Col = parameters.getValue ( "Region2Col" );
    String AWCCol = parameters.getValue ( "AWCCol" );
    String Top = parameters.getValue ( "Top" );
    int top = -1;
    if ( (Top != null) && !Top.isEmpty() ) {
    	top = Integer.parseInt(Top);
    }

    // Get columns, all zero offset
    
    int IDCol_int = -1;
    if ( IDCol != null ) {
        IDCol_int = Integer.parseInt(IDCol) - 1;
    }
    int NameCol_int = -1;
    if ( NameCol != null ) {
    	NameCol_int = Integer.parseInt(NameCol) - 1;
    }
    int LatitudeCol_int = -1;
    if ( LatitudeCol != null ) {
    	LatitudeCol_int = Integer.parseInt(LatitudeCol) - 1;
    }
    int ElevationCol_int = -1;
    if ( ElevationCol != null ) {
    	ElevationCol_int = Integer.parseInt(ElevationCol) - 1;
    }
    int Region1Col_int = -1;
    if ( Region1Col != null ) {
    	Region1Col_int = Integer.parseInt(Region1Col) - 1;
    }
    int Region2Col_int = -1;
    if ( Region2Col != null ) {
    	Region2Col_int = Integer.parseInt(Region2Col) - 1;
    }
    int AWCCol_int = -1;
    if ( AWCCol != null ) {
    	AWCCol_int = Integer.parseInt(AWCCol) - 1;
    }

    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    // Now try to read...

    String ListFile_full = ListFile;
    try {
        boolean readData = true;
        if ( command_phase == CommandPhaseType.DISCOVERY ){
            readData = false;
        }
        ListFile_full = IOUtil.verifyPathForOS(
                IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
        Message.printStatus ( 2, routine, "Reading CU locations list file \"" + ListFile_full + "\"" );
    
    	if ( !IOUtil.fileExists(ListFile_full) ) {
    		message = "List file \"" + ListFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(ListFile_full) ) {
    		message = "List file \"" + ListFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}

    	// Read using the table...

    	PropList props = new PropList ("");
    	props.set ( "Delimiter=," );		// see existing prototype
    	props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
    	props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
    	if ( top > 0 ) {
    		props.set ( "Top=" + top );
    	}
    	DataTable table = null;
    	try {
    		table = DataTable.parseFile ( ListFile_full, props );
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
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"NameCol", NameCol, NameCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"LatitudeCol", LatitudeCol, LatitudeCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"ElevationCol", ElevationCol, ElevationCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"Region1Col", Region1Col, Region1Col_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"Region2Col", Region2Col, Region2Col_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"AWCCol", AWCCol, AWCCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	
    	// Remove all the elements for the list that tracks when identifiers
    	// are read from more than one main source (e.g., DDS, and STR).
    	// This is used to print a warning.
    	processor.resetDataMatches ( processor.getStateCULocationMatchList() );

    	// Loop through the records in the table.

    	int size = table.getNumberOfRecords();
    	StateCU_Location culoc;
    	String id;
    	TableRecord rec = null;
    	String data;
		boolean foundWdidLength = false;
		int wdidLength = 0;	// Length of WDIDs in data set
    	for (int i = 0; i < size; i++) {
    		culoc = new StateCU_Location();
    		rec = table.getRecord(i);
    		id = (String)rec.getFieldValue(IDCol_int);
    		if ( id.length() == 0 ) {	// Sometimes blank lines
    			continue;
    		}
    		culoc.setID ( id );
    		if ( NameCol != null ) {
    			culoc.setName ( (String)rec.getFieldValue(NameCol_int) );
    		}
    		if ( LatitudeCol != null ) {
    			data = (String)rec.getFieldValue(LatitudeCol_int);
    			if ( data.length() > 0 ) {
    				if ( StringUtil.isDouble(data) ) {
    					culoc.setLatitude( StringUtil.atod(data));
    				}
    				else {
    					message = "Latitude (" + data + ") for \"" + id + ") is not a number.";
    		            Message.printWarning ( warning_level,
    		                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
    		                status.addToLog(command_phase,
    		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
    		                        "Verify that the latitude is a number."));
    				}
    			}
    		}
    		if ( ElevationCol != null ) {
    			data = (String)rec.getFieldValue(ElevationCol_int);
    			if ( data.length() > 0 ) {
    				if ( StringUtil.isDouble(data) ) {
    					culoc.setElevation(StringUtil.atod(data));
    				}
    				else {
    					message = "Elevation (" + data + ") for \"" + id + ") is not a number.";
    		            Message.printWarning ( warning_level,
    		                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
    		                status.addToLog(command_phase,
    		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
    		                        "Verify that the elevation is a number."));
    				}
    			}
    		}
    		if ( Region1Col != null ) {
    			culoc.setRegion1( (String)rec.getFieldValue(Region1Col_int) );
    		}
    		if ( Region2Col != null ) {
    			culoc.setRegion2( (String)rec.getFieldValue(Region2Col_int) );
    		}
    		if ( AWCCol != null ) {
    			data = (String)rec.getFieldValue(AWCCol_int);
    			if ( data.length() > 0 ) {
    				if ( StringUtil.isDouble(data) ) {
    					culoc.setAwc( StringUtil.atod(data));
    				}
    				else {
    					message = "AWC (" + data + ") for \"" + id + ") is not a number.";
    		            Message.printWarning ( warning_level,
    		                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
    		                status.addToLog(command_phase,
    		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
    		                        "Verify that the AWC is a number."));
    				}
    			}
    		}
    		// Replace or add in the CU locations list...
    		processor.findAndAddCULocation ( culoc, true );
			// Assume numeric fields are WDIDs and reset the default width for WDIDs in output.
			// This is used to make identifiers consistent in the data sets.
			if ( !foundWdidLength ) {
				id = culoc.getID();
				if ( HydroBase_WaterDistrict.isWDID(id) && (id.length() == 6) || (id.length() == 7) ) {
					wdidLength = id.length();
					if ( !foundWdidLength ) {
						Message.printStatus ( 2, routine,
						"Reading CU locations - assuming WDID length should be "+ wdidLength +
						" as default for other processing.");
					}
					foundWdidLength = true;
				}
			}
    	}
    	
		// Set the default identifier length in the processor.
		if ( wdidLength > 0 ) {
			getCommandProcessor().setPropContents("WDIDLength", new Integer(wdidLength) );
		}

    	// Warn about identifiers that have been replaced in the CU locations list...

    	processor.warnAboutDataMatches ( this, true, processor.getStateCULocationMatchList(), "CU Locations" );
    }
    catch ( CommandException e ) {
    	// Just pass through
    	throw e;
    }
    catch ( FileNotFoundException e ) {
        message = "CU locations list file \"" + ListFile_full + "\" is not found or accessible.";
        Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                Message.printWarning ( 3, routine, e );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
        throw new CommandException ( message );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading CU locations list file \"" + ListFile_full + "\" (" + e + ").";
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

	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String NameCol = parameters.getValue ( "NameCol" );
	String LatitudeCol = parameters.getValue ( "LatitudeCol" );
	String ElevationCol = parameters.getValue ( "ElevationCol" );
	String Region1Col = parameters.getValue ( "Region1Col" );
	String Region2Col = parameters.getValue ( "Region2Col" );
	String AWCCol = parameters.getValue ( "AWCCol" );
	String Top = parameters.getValue ( "Top" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( (IDCol != null) && (IDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDCol=" + IDCol );
	}
	if ( (NameCol != null) && (NameCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NameCol=" + NameCol );
	}
	if ( (LatitudeCol != null) && (LatitudeCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LatitudeCol=" + LatitudeCol  );
	}
	if ( (ElevationCol != null) && (ElevationCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ElevationCol=" + ElevationCol  );
	}
	if ( (Region1Col != null) && (Region1Col.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region1Col=" + Region1Col );
	}
	if ( (Region2Col != null) && (Region2Col.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Region2Col=" + Region2Col );
	}
	if ( (AWCCol != null) && (AWCCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AWCCol=" + AWCCol );
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
