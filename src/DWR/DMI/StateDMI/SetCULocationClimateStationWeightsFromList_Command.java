// SetCULocationClimateStationWeightsFromList_Command - This class initializes, checks, and runs the SetCULocationClimateStationWeightsFromList() command.

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

import DWR.StateCU.StateCU_Location;

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
This class initializes, checks, and runs the SetCULocationClimateStationWeightsFromList() command.
*/
public class SetCULocationClimateStationWeightsFromList_Command extends AbstractCommand implements Command
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
public SetCULocationClimateStationWeightsFromList_Command ()
{	super();
	setCommandName ( "SetCULocationClimateStationWeightsFromList" );
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
{	String routine = getClass().getName() + ".checkCommandParameters";
	String ListFile = parameters.getValue ( "ListFile" );
	String IDCol = parameters.getValue ( "IDCol" );
	String StationIDCol = parameters.getValue ( "StationIDCol" );
	String Region1Col = parameters.getValue ( "Region1Col" );
	String Region2Col = parameters.getValue ( "Region2Col" );
	String TempWtCol = parameters.getValue ( "TempWtCol" );
	String PrecWtCol = parameters.getValue ( "PrecWtCol" );
	String OrographicTempAdjCol = parameters.getValue ( "OrographicTempAdjCol" );
	String OrographicPrecAdjCol = parameters.getValue ( "OrographicPrecAdjCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
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
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, ListFile));
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
	if ( ((IDCol != null) && (IDCol.length() > 0)) &&
			(((Region1Col != null) && (Region1Col.length() > 0)) ||
			((Region2Col != null) && (Region2Col.length() > 0))) ) {
        message = "Either the CU Location ID (alone) or Region1 and/or Region2 can be specified " +
		" to match locations.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID column (1+) OR Region1/Region2." ) );
	}
	
	if ( (IDCol != null) &&	(IDCol.length() != 0) && !StringUtil.isInteger(IDCol) ) {
        message = "The ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the ID column as a number >= 1." ) );
	}
	
	if ( (StationIDCol == null) ||(StationIDCol.length() == 0) ) {
        message = "The station ID column is required.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the station ID column as a number >= 1." ) );
	}
	else if ( !StringUtil.isInteger(StationIDCol) ) {
        message = "The station ID column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the station ID column as a number >= 1." ) );
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
	
	if ( (TempWtCol == null) || (TempWtCol.length() == 0) ) {
        message = "The temperature weight column is required.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the temperature weight column as a number >= 1." ) );
	}
	else if ( !StringUtil.isInteger(TempWtCol) ) {
        message = "The temperature weight column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the temperature weight column as a number >= 1." ) );
	}
	
	if ( (PrecWtCol == null) || (PrecWtCol.length() == 0) ) {
        message = "The precipitation weight column is required.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the precipitation weight column as a number >= 1." ) );
	}
	else if ( !StringUtil.isInteger(PrecWtCol) ) {
        message = "The precipitation weight column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the precipitation weight column as a number >= 1." ) );
	}
	
	if ( (OrographicTempAdjCol != null) && (OrographicTempAdjCol.length() != 0) && !StringUtil.isInteger(OrographicTempAdjCol) ) {
        message = "The OrographicTempAdjCol column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the OrographicTempAdjCol column as a number >= 1." ) );
	}
	
	if ( (OrographicPrecAdjCol != null) && (OrographicPrecAdjCol.length() != 0) && !StringUtil.isInteger(OrographicPrecAdjCol) ) {
        message = "The OrographicPrecAdjCol column is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the OrographicPrecAdjCol column as a number >= 1." ) );
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
	List<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "StationIDCol" );
	valid_Vector.add ( "Region1Col" );
	valid_Vector.add ( "Region2Col" );
	valid_Vector.add ( "TempWtCol" );
	valid_Vector.add ( "PrecWtCol" );
	valid_Vector.add ( "OrographicTempAdjCol" );
	valid_Vector.add ( "OrographicPrecAdjCol" );
	valid_Vector.add ( "IfNotFound" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
	return (new SetCULocationClimateStationWeightsFromList_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String ListFile = parameters.getValue ( "ListFile" );
    String Delim = parameters.getValue ( "Delim" );
    if ( (Delim == null) || Delim.equals("") ) {
    	Delim = ",";
    }
    String IDCol = parameters.getValue ( "IDCol" );
    String StationIDCol = parameters.getValue ( "StationIDCol" );
    String Region1Col = parameters.getValue ( "Region1Col" );
    String Region2Col = parameters.getValue ( "Region2Col" );
    String TempWtCol = parameters.getValue ( "TempWtCol" );
    String PrecWtCol = parameters.getValue ( "PrecWtCol" );
    String OrographicTempAdjCol = parameters.getValue ( "OrographicTempAdjCol" );
    String OrographicPrecAdjCol = parameters.getValue ( "OrographicPrecAdjCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}

    // Get columns, all zero offset
    
    int IDCol_int = -1;
    if ( IDCol != null ) {
        IDCol_int = Integer.parseInt(IDCol) - 1;
    }
    int StationIDCol_int = -1;
    if ( StationIDCol != null ) {
    	StationIDCol_int = Integer.parseInt(StationIDCol) - 1;
    }
    int Region1Col_int = -1;
    if ( Region1Col != null ) {
    	Region1Col_int = Integer.parseInt(Region1Col) - 1;
    }
    int Region2Col_int = -1;
    if ( Region2Col != null ) {
    	Region2Col_int = Integer.parseInt(Region2Col) - 1;
    }
    int TempWtCol_int = -1;
    if ( TempWtCol != null ) {
    	TempWtCol_int = Integer.parseInt(TempWtCol) - 1;
    }
    int PrecWtCol_int = -1;
    if ( PrecWtCol != null ) {
    	PrecWtCol_int = Integer.parseInt(PrecWtCol) - 1;
    }
    int OrographicTempAdjCol_int = -1;
    if ( OrographicTempAdjCol != null ) {
    	OrographicTempAdjCol_int = Integer.parseInt(OrographicTempAdjCol) - 1;
    }
    int OrographicPrecAdjCol_int = -1;
    if ( OrographicPrecAdjCol != null ) {
    	OrographicPrecAdjCol_int = Integer.parseInt(OrographicPrecAdjCol) - 1;
    }
    
    // Get the data needed for the command
    
    List<StateCU_Location> culocList = null;
    int culocListSize = 0;
    try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents ( "StateCU_Location_List" );
		culocList = dataList;
		if ( culocList != null ) {
			culocListSize = culocList.size();
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to sort (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    String ListFile_full = ListFile;
    try {
        ListFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),ListFile) );
        Message.printStatus ( 2, routine, "Reading CU locations climate station weights list file \"" +
        	ListFile_full + "\"" );
    
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
    	DataTable table = null;
    	int tsize = 0;
    	try {
    		table = DataTable.parseFile ( ListFile_full, props );
    		tsize = table.getNumberOfRecords();
         	Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
            		table.getNumberOfFields() + " fields" );
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
    			"StationIDCol", StationIDCol, StationIDCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"Region1Col", Region1Col, Region1Col_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"Region2Col", Region2Col, Region2Col_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"TempWtCol", TempWtCol, TempWtCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"PrecWtCol", PrecWtCol, PrecWtCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"OrographicTempAdjCol", OrographicTempAdjCol, OrographicTempAdjCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
    			"OrographicPrecAdjCol", OrographicPrecAdjCol, OrographicPrecAdjCol_int, table.getNumberOfFields(),
    			status, command_phase, routine, command_tag, warning_level, warning_count );
    	
    	// Remove all the elements for the Vector that tracks when identifiers
    	// are read from more than one main source (e.g., DDS, and STR).
    	// This is used to print a warning.
    	processor.resetDataMatches ( processor.getStateCULocationMatchList() );

    	// Do this the brute force way because there are not that many records.

    	// Loop through the CU locations that are in memory...  For each one,
    	// search the table and add climate station information to the
    	// locations

    	StateCU_Location culoc;
    	String id = null;	// CULoc id in the file
    	String culocID = null;	// CULoc ID for CU Location in memory.
    	String stationid;
    	String region1 = null;
    	String region2 = null;
    	String tempwt;
    	String precwt;
    	String ota;	// Orographic adjustments
    	String opa;
    	String culocRegion1;
    	String culocRegion2;
    	TableRecord rec = null;
    	boolean matchFound = false;	// Is record a match?
    	int matchCount = 0; // Match count for each culoc
    	int matchCountAll = 0; // Match count for all culoc
    	for (int i = 0; i < culocListSize; i++) {
    		matchCount = 0;	// Number of matching region1/region2
    		culoc = (StateCU_Location)culocList.get(i);
    		culocID = culoc.getID();
    		culocRegion1 = culoc.getRegion1();
    		culocRegion2 = culoc.getRegion2();
    		// Now loop through the table and see if there are any matches
    		// for Region1 and Region2 OR the CU location ID...
    		for (int j = 0; j < tsize; j++) {
    			rec = table.getRecord(j);
    			if ( IDCol_int >= 0 ) {
    				id = (String)rec.getFieldValue(IDCol_int);
    			}
    			if ( Region1Col_int >= 0 ) {
    				region1 = (String)rec.getFieldValue(Region1Col_int);
    			}
    			if ( Region2Col_int >= 0 ) {
    				region2 = (String)rec.getFieldValue(Region2Col_int);
    			}
    			matchFound = false;
    			if ( IDCol_int >= 0 ) {
    				// Try to match the ID.
    				if ( culocID.equalsIgnoreCase(id) ) {
    					matchFound = true;
    				}
    			}
    			else if ( (Region1Col_int >= 0) && (Region2Col_int >= 0) ) {
    				if ( region1.equalsIgnoreCase(culocRegion1) && region2.equalsIgnoreCase(culocRegion2) ){
    					matchFound = true;
    				}
    			}
    			else if ( Region1Col_int >= 0 ) {
    				if ( region1.equalsIgnoreCase(culocRegion1) ){
    					matchFound = true;
    				}
    			}
    			else if ( Region2Col_int >= 0 ) {
    				if ( region2.equalsIgnoreCase(culocRegion2) ){
    					matchFound = true;
    				}
    			}
    			if ( matchFound ) {
    				if ( matchCount == 0 ) {
    					// On the first match for the CU location, zero out what is defined for the
    					// location...
    					culoc.setNumClimateStations ( 0 );
    				}
    				stationid = (String)rec.getFieldValue(StationIDCol_int);
    				culoc.setClimateStationID ( stationid, matchCount );
    				// Required...
    				tempwt = (String)rec.getFieldValue(TempWtCol_int);
    				if ( StringUtil.isDouble(tempwt) ) {
    					culoc.setTemperatureStationWeight (
    					StringUtil.atod(tempwt), matchCount );
    				}
    				precwt = (String)rec.getFieldValue(PrecWtCol_int);
    				if ( StringUtil.isDouble(precwt) ) {
    					culoc.setPrecipitationStationWeight (
    					StringUtil.atod(precwt), matchCount );
    				}
    				// Optional...
    				String ota_message = "";
    				if ( OrographicTempAdjCol_int >= 0 ) {
    					ota = (String)rec.getFieldValue(OrographicTempAdjCol_int);
    					if ( StringUtil.isDouble(ota)) {
    						culoc.setOrographicTemperatureAdjustment ( StringUtil.atod(ota), matchCount );
    					}
    					ota_message = " ota=" + ota;
    				}
    				String opa_message = "";
    				if ( OrographicPrecAdjCol_int >= 0 ) {
    					opa = (String)rec.getFieldValue(OrographicPrecAdjCol_int);
    					if ( StringUtil.isDouble(opa)) {
    						culoc.setOrographicPrecipitationAdjustment ( StringUtil.atod(opa), matchCount );
    					}
    					opa_message = " opa=" + opa;
    				}
    				// Increment after setting
    				++matchCount;
    				++matchCountAll;
    				Message.printStatus ( 2, routine, "Setting " + culoc.getID() +
    					" climate station (" + matchCount + ") -> " + stationid +
    					" tempwt=" + tempwt + " precwt=" + precwt + ota_message + opa_message );
    			}
    		}
    	}

    	// If nothing was matched, take other actions...

		if ( matchCountAll == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No CU locations were matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No CU locations were matched: failing and not setting.";
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
        message = "Unexpected error setting data (" + e + ").";
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
	String StationIDCol = parameters.getValue ( "StationIDCol" );
	String Region1Col = parameters.getValue ( "Region1Col" );
	String Region2Col = parameters.getValue ( "Region2Col" );
	String TempWtCol = parameters.getValue ( "TempWtCol" );
	String PrecWtCol = parameters.getValue ( "PrecWtCol" );
	String OrographicTempAdjCol = parameters.getValue ( "OrographicTempAdjCol" );
	String OrographicPrecAdjCol = parameters.getValue ( "OrographicPrecAdjCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
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
	if ( (StationIDCol != null) && (StationIDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "StationIDCol=" + StationIDCol );
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
	if ( (TempWtCol != null) && (TempWtCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TempWtCol=" + TempWtCol );
	}
	if ( (PrecWtCol != null) && (PrecWtCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PrecWtCol=" + PrecWtCol );
	}
	if ( (OrographicTempAdjCol != null) && (OrographicTempAdjCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OrographicTempAdjCol=" + OrographicTempAdjCol  );
	}
	if ( (OrographicPrecAdjCol != null) && (OrographicPrecAdjCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OrographicPrecAdjCol=" + OrographicPrecAdjCol  );
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound  );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
