// FillAndSetCULocationsFromList_Command - This class initializes, checks,
// and runs the FillCULocationsFromList() and SetCULocationsFromList() commands.

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
import DWR.StateCU.StateCU_Util;

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
<p>
This class initializes, checks, and runs the FillCULocationsFromList() and
SetCULocationsFromList() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetCULocationsFromList()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
</p>
*/
public abstract class FillAndSetCULocationsFromList_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetCULocationsFromList_Command ()
{	super();
	setCommandName ( "?Fill?Set?CULocationsFromList" );
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
	String NameCol = parameters.getValue ( "NameCol" );
	String LatitudeCol = parameters.getValue ( "LatitudeCol" );
	String ElevationCol = parameters.getValue ( "ElevationCol" );
	String Region1Col = parameters.getValue ( "Region1Col" );
	String Region2Col = parameters.getValue ( "Region2Col" );
	String AWCCol = parameters.getValue ( "AWCCol" );
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
	List valid_Vector = new Vector();
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "NameCol" );
	valid_Vector.add ( "LatitudeCol" );
	valid_Vector.add ( "ElevationCol" );
	valid_Vector.add ( "Region1Col" );
	valid_Vector.add ( "Region2Col" );
	valid_Vector.add ( "AWCCol" );
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
	return (new FillAndSetCULocationsFromList_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
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
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    String ListFile = parameters.getValue ( "ListFile" );
    String IDCol = parameters.getValue ( "IDCol" );
    String NameCol = parameters.getValue ( "NameCol" );
    String LatitudeCol = parameters.getValue ( "LatitudeCol" );
    String ElevationCol = parameters.getValue ( "ElevationCol" );
    String Region1Col = parameters.getValue ( "Region1Col" );
    String Region2Col = parameters.getValue ( "Region2Col" );
    String AWCCol = parameters.getValue ( "AWCCol" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
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
    
    // Get the data needed for the command
    
    List culocList = null;
    try {
		culocList = (List)processor.getPropContents ( "StateCU_Location_List" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting CU location data to process (" + e + ").";
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
	String Action = "Filling ";	// For messages
	String action = "filling";	// For messages
	boolean doFill = true;
	if ( this instanceof SetCULocationsFromList_Command ) {
		doFill = false;
		Action = "Setting ";
		action = "setting";
	}
    try {
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
    	DataTable table = null;
    	int tsize = 0;
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
    	
    	// Remove all the elements for the Vector that tracks when identifiers
    	// are read from more than one main source (e.g., DDS, and STR).
    	// This is used to print a warning.
    	processor.resetDataMatches ( processor.getStateCULocationMatchList() );
    	
    	// Do this the brute force way because there are not that many records.

    	// Loop through the CU locations that are in memory...  For each one,
    	// search the table and add climate station information to the locations

    	StateCU_Location culoc;	// Instance
    	String culoc_id; // ID from instance
    	int size = 0;
    	if ( culocList != null ) {
    		size = culocList.size();
    	}
    	String id;
    	String latitude;
    	String elevation;
    	String region1;
    	String region2;
    	String name;
    	String awc;
    	TableRecord rec = null;
    	int matchCount = 0;
    	for (int i = 0; i < size; i++) {
    		culoc = (StateCU_Location)culocList.get(i);
    		culoc_id = culoc.getID();
    		// First check to see if it desired to update the location...
    		if ( !culoc_id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		Message.printStatus(2,routine, "Checking CU location \"" + culoc_id + "\" against list IDs." );
    		// Now loop through the table and see if there are any matches for the ID...
    		for (int j = 0; j < tsize; j++) {
    			rec = table.getRecord(j);
    			id = (String)rec.getFieldValue(IDCol_int);
    			if ( !id.equalsIgnoreCase(culoc_id) ) {
    				continue;
    			}
    			// Else, have a match...
    			Message.printStatus(2,routine, "CU location \"" + culoc_id + "\" matches list.  " +
    				Action + " using list data." );
    			if ( (LatitudeCol_int > 0) && (!doFill || // Setting...else
    				StateCU_Util.isMissing(	// filling
    				culoc.getLatitude()))) {//similar comments below
    				latitude = (String)rec.getFieldValue(LatitudeCol_int);
    				if ( !StringUtil.isDouble(latitude) ) {
    					message = "In list file for location \"" + culoc_id + "\" latitude (" +
    					latitude + ") is not a number.  Not " + action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + culoc_id + " latitude -> " + latitude );
    					culoc.setLatitude( StringUtil.atod(latitude) );
    				}
    			}
    			if ( (ElevationCol_int > 0) && (!doFill ||
    				StateCU_Util.isMissing(culoc.getElevation()))) {
    				elevation = (String)rec.getFieldValue(ElevationCol_int);
    				if ( !StringUtil.isDouble(elevation) ) {
    					message = "In list file for elevation \"" + culoc_id + "\" elevation (" +
    					elevation + ") is not a number.  Not " +action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + culoc_id + " elevation -> " + elevation );
    					culoc.setElevation(	StringUtil.atod(elevation) );
    				}
    			}
    			if ( (Region1Col_int > 0) && (!doFill || StateCU_Util.isMissing(culoc.getRegion1()))) {
    				region1 = (String)rec.getFieldValue(Region1Col_int);
    				Message.printStatus ( 2, routine, Action + culoc_id + " region1 -> " + region1 );
    				culoc.setRegion1( region1 );
    			}
    			if ( (Region2Col_int > 0) && (!doFill || StateCU_Util.isMissing(culoc.getRegion2()))) {
    				region2 = (String)rec.getFieldValue(Region2Col_int);
    				Message.printStatus ( 2, routine, Action + culoc_id + " region2 -> " + region2 );
    				culoc.setRegion2( region2 );
    			}
    			if ( (NameCol_int > 0) && (!doFill || StateCU_Util.isMissing(culoc.getName()))) {
    				name = (String)rec.getFieldValue(NameCol_int);
    				Message.printStatus ( 2, routine, Action + culoc_id + " name -> " + name );
    				culoc.setName( name );
    			}
    			if ( (AWCCol_int > 0) && (!doFill || StateCU_Util.isMissing(culoc.getAwc()))) {
    				awc = (String) rec.getFieldValue(AWCCol_int);
    				if ( !StringUtil.isDouble(awc) ) {
    					message= "In list file for AWC \"" + culoc_id + "\" awc (" + awc +
    					") is not a number.  Not " +action+".";
    					Message.printWarning ( warning_level,
			                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
		                status.addToLog(command_phase,
		                    new CommandLogRecord( CommandStatusType.FAILURE, message,
		                        "Verify the format of the file."));
    				}
    				else {
    					Message.printStatus ( 2, routine, Action + culoc_id + " AWC -> " + awc );
    					culoc.setAwc( StringUtil.atod(awc) );
    				}
    			}
    			++matchCount;
    		}
    	}

    	// If nothing was matched, take other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything.
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No CU locations were matched: warning and not " + action + ".";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No CU locations were matched: failing and not " + action + ".";
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
        message = "Unexpected error " + action + " CU location data (" + e + ").";
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
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
