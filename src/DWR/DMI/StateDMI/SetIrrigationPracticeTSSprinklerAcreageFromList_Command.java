// SetIrrigationPracticeTSSprinklerAcreageFromList_Command - editor for SetIrrigationPracticeTSSprinklerAcreageFromList command

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

//------------------------------------------------------------------------------
// setIrrigationPracticeTSSprinklerAcreageFromList_Command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2007-06-19	Steven A. Malers, RTi	Initial version
//------------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;

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
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the SetIrrigationPracticeTSSprinklerAcreageFromList() command.
*/
public class SetIrrigationPracticeTSSprinklerAcreageFromList_Command 
extends AbstractCommand implements Command
{
	
/**
Constructor.
*/
public SetIrrigationPracticeTSSprinklerAcreageFromList_Command ()
{	super();
	setCommandName ( "SetIrrigationPracticeTSSprinklerAcreageFromList" );
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
	String ID = parameters.getValue ( "ID" );
	String ListFile = parameters.getValue ( "ListFile" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	String IDCol = parameters.getValue ( "IDCol" );
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a CU location ID pattern to process." ) );
	}

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
	if ( (SetStart != null) && (SetStart.length() > 0) && !StringUtil.isInteger(SetStart) ) {
        message = "The set start year (" + SetStart + ") is not a valid number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid start year YYYY." ) );
	}
	if ( (SetEnd != null) && (SetEnd.length() > 0) && !StringUtil.isInteger(SetEnd) ) {
        message = "The set end year (" + SetEnd + ") is not a valid number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid end year YYYY." ) );
	}
	if ( (YearCol != null) && (YearCol.length() > 0) && !StringUtil.isInteger(YearCol) ) {
		message = "\nThe year column (" + YearCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the Year column as an integer 1+." ) );
	}
	if ( (IDCol == null) ||	(IDCol.length() == 0) ) {
		message = "The required ID column has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID column as an integer 1+." ) );
	}
	else if ( !StringUtil.isInteger(IDCol) ) {
		message = "The ID column (" + IDCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ID column as an integer 1+." ) );
	}
	if ( (AcresSprinklerCol == null) ||	(AcresSprinklerCol.length() == 0) ) {
		message = "The required AcresSprinklerCol column has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the AcresSprinklerCol column as an integer 1+." ) );
	}
	else if ( !StringUtil.isInteger(AcresSprinklerCol) ) {
		message = "The AcresSprinklerCol column (" + AcresSprinklerCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the AcresSprinklerCol column as an integer 1+." ) );
	}
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(7);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
	valid_Vector.add ( "YearCol" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "AcresSprinklerCol" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, warning_level ), warning );
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
	return (new SetIrrigationPracticeTSSprinklerAcreageFromList_JDialog ( parent, this )).ok();
}

/**
Method to execute the setIrrigationPracticeTSSprinklerAcreageFromList() command.
@param command_number Command number in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	int log_level = 3;  // Log level for non-user warnings
	
	CommandPhaseType command_phase = CommandPhaseType.RUN;
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	
	// Get the input needed to process the file...
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*";	// Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String ListFile = parameters.getValue ( "ListFile" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	String IDCol = parameters.getValue ( "IDCol" );
	int IDCol_int = StringUtil.atoi ( IDCol ) - 1;
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	
	// The remaining columns are optional...

	int YearCol_int = -1;
	if ( YearCol != null ) {
		YearCol_int = StringUtil.atoi (YearCol) - 1;
	}

	int AcresSprinklerCol_int = -1;
	if ( AcresSprinklerCol != null ) {
		AcresSprinklerCol_int = StringUtil.atoi (AcresSprinklerCol) - 1;
	}
	
	// Get irrigation practice time series

	List<StateCU_IrrigationPracticeTS> ipyList = null;
	int ipyListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_IrrigationPracticeTS> dataList = (List<StateCU_IrrigationPracticeTS>)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
		ipyList = dataList;
		ipyListSize = ipyList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting irrigation practice time series data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( ipyListSize == 0 ) {
		message = "No irrigation practice time series are defined.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateIrrigationPracticeTSForCULocations() before this command." ) );
	}

	// Get the list of CU locations, used to check the data.
	// - ok if zero length (may be the case if smaller command file rather than full IPY)
	
	List<StateCU_Location> culocList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_Location_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
    // Output period will be used if not specified with SetStart and SetEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    DateTime OutputEnd_DateTime = null;
    try {
    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputEnd (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	int SetStart_int = 0, SetEnd_int = 0;
	if ( SetStart == null ) {
		if ( OutputStart_DateTime == null ) {
			message = "Set start and global OutputStart are not set.";
		    Message.printWarning ( warning_level, 
    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Set the period in this command or use the SetOutputPeriod() command." ) );
		}
		else {
			SetStart_int = OutputStart_DateTime.getYear();
		}
	}
	else {
		SetStart_int = StringUtil.atoi(SetStart);
	}

	if ( SetEnd == null ) {
		if ( OutputEnd_DateTime == null ) {
			message = "Set end and global OutputEnd are not set.";
		    Message.printWarning ( warning_level, 
    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	        status.addToLog ( command_phase,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Set the period in this command or use the SetOutputPeriod() command." ) );
		}
		else {
			SetEnd_int = OutputEnd_DateTime.getYear();
		}
	}
	else {
		SetEnd_int = StringUtil.atoi(SetEnd);
	}
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        throw new InvalidCommandParameterException ( message );
    }
	
	// Process the data....
	
	try {
		String ListFile_full = IOUtil.verifyPathForOS(
	        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
                StateDMICommandProcessorUtil.expandParameterValue(processor,this,ListFile)));

		// Read the list file using the table...

		PropList props = new PropList ("");
		props.set ( "Delimiter=," ); // see existing prototype
		props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
		props.set ( "TrimStrings=True" ); // If true, trim strings after reading.
		DataTable table = DataTable.parseFile ( ListFile_full, props );

		int tsize = 0;
		if ( table != null ) {
			tsize = table.getNumberOfRecords();
		}

		Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
			table.getNumberOfFields() + " fields" );
		
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"YearCol", YearCol, YearCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"AcresSprinklerCol", AcresSprinklerCol, AcresSprinklerCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		
		// Put in to allow old code to be used without changing.  The behavior is to
		// process the data now.
		
		int Now_int =0;
		int ProcessWhen_int = Now_int;

		// Do this the brute force way...

		// Loop through the list file records.  If the CU location is matched,
		// find the irrigation practice information for the location.

		StateCU_IrrigationPracticeTS ipy;
		String Year_String = "";
		String AcresSprinkler_String = "";
		boolean fill_AcresSprinkler = false;
		TableRecord rec = null;
		// TODO smalers 2019-05-28 enable match count
		//int matchCount = 0;
		int year;
		int pos = 0; // Location of ID in data component Vector
		int Year_int; // Integer value of year from column in file
		String ipy_id, id;
		// Loop through the table and see if there are any matches for the
		// CU Location ID for the record.  This will be relatively fast if no
		// YearCol is given and/or if the ID=*.
		for ( int j = 0; j < tsize; j++ ) {
			rec = table.getRecord(j);
			id = (String)rec.getFieldValue(IDCol_int);
			ipy = null;	// Initialize to prevent compiler warnings
			if ( ID.equals("*") ) {
				;	// Will process.
			}
			else {
				// Match the ID in the record with the IDs to process.
				if ( !id.matches(idpattern_Java) ) {
					// Identifier does not match.  Do not process the record...
					continue;
				}
			}

			// Find the StateCU_IrrigationPracticeTS instance to modify.

			if ( ProcessWhen_int == Now_int ) {
				pos = StateCU_Util.indexOf(ipyList,id);
				if ( pos < 0 ) {
					message = "List file location \"" + id + "\" does not match any CU locations.";
				    Message.printWarning ( warning_level, 
		    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.WARNING,
			                message, "Verify that the locations in the list file match CU locations." ) );
					continue;
				}
				ipy = (StateCU_IrrigationPracticeTS)ipyList.get(pos);
			}

			// Get the StateCU_Location to track when a set command is used.
			int pos2 = StateCU_Util.indexOf(culocList,id);
			StateCU_Location culoc = null;
			if ( pos2 >= 0 ) {
				culoc = culocList.get(pos2);
			}

			// OK to set the data...
			// Get the data values from the table one time...
			//++matchCount;
			ipy_id = id;
			Year_int = -1; // Indicate not to set for a specific year
			if ( YearCol != null ) {
				Year_String = (String)rec.getFieldValue( YearCol_int);
				if ( !StringUtil.isInteger( Year_String)) {
					message = "Year in list file (" + Year_String
					+ ") is not an integer.  Skipping record " + (j + 1) + ".";
				    Message.printWarning ( warning_level, 
		    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Verify that the year in the list is an integer." ) );
					continue;
				}
				else {
					// Convert for use below...
					Year_int = Integer.parseInt ( Year_String );
				}
			}
			fill_AcresSprinkler = false;
			if ( AcresSprinklerCol != null ) {
				AcresSprinkler_String = (String)rec.getFieldValue(AcresSprinklerCol_int);
				fill_AcresSprinkler = true;
				if ( !StringUtil.isDouble(AcresSprinkler_String)) {
					message = "AcresSprinkler (" + AcresSprinkler_String + ") is not a number.";
				    Message.printWarning ( warning_level, 
		    	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Verify that the acres value in the list is a number." ) );
					fill_AcresSprinkler = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					if ( ProcessWhen_int == Now_int ) {
						Message.printStatus ( 2, routine,
						"Setting " + ipy_id + " AcresSprinkler " + Year_int + " -> " + AcresSprinkler_String );
						setSprinklerAcres ( culoc, ipy, Double.parseDouble(AcresSprinkler_String), Year_int,
							command_tag, warning_level, warning_count, status );
					}
				}
			}
			// Set for the specified period if the year column was not specified.
			// This should only get executed once per location if the list file is set up properly.
			if ( YearCol == null ) {
				for ( year = SetStart_int; year <= SetEnd_int; year++ ){
					if ( fill_AcresSprinkler ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresSprinkler " +
								SetStart_int + " to " + SetEnd_int + " -> " + AcresSprinkler_String );
						}
						if ( ProcessWhen_int == Now_int ) {
							warning_count = setSprinklerAcres ( culoc, ipy, Double.parseDouble(
								AcresSprinkler_String), year,
								command_tag, warning_level, warning_count, status );
						}
					}
				}
			}
		}
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting irrigation practice time series data (" + e + ").";
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
Check the irrigation practice acreage time series totals for the specified year.
This causes adjustments to be made so the acreage parts add to the total.
@param ipyts Irrigation practice time series object being modified.
@param Sacre_set the sprinkler acreage value being set.
@param year The year for which the sprinkler acreage is being set.
@param command_tag The command tag used for logging.
@return the count of warnings (incremented from original passed in value).
*/
private int setSprinklerAcres ( StateCU_Location culoc,
		StateCU_IrrigationPracticeTS ipyts, double Sacre_set, int year,
		String command_tag, int warning_level, int warning_count, CommandStatus status )
{	String routine = "setIrrigationPracticeTSSprinklerAcreageFromList.checkTotals";
	String message;
	String id = ipyts.getID();
	CommandPhaseType command_phase = CommandPhaseType.RUN;
	
	// Get the previous data before changes...
	
	double Acgw_prev = ipyts.getAcgw(year);
	if ( Acgw_prev < 0.0 ) {
		message = "Location \"" + id + "\" " + year + ":  GWtotal is missing." ;
	    Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Fill GWtotal before setting sprinkler acreage." ) );
		return warning_count;
	}
	// TODO SAM 2007-09-11 Evaluate whether it should be allowed to set GWtotal to sprinkler file if total is missing.

	// Reset the user-assigned sprinkler acreage to the GW total if necessary...
	// TODO SAM 2009-02-15 Evaluate whether a warning or status message
	double Sacre_set_adjusted = Sacre_set;
	if ( Sacre_set > Acgw_prev ) {
		message = "Location \"" + id + "\" " + year + ":  Reducing list file sprinkler acres to GW acres.";
	    Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.WARNING,
                message, "Verify that list file sprinkler acres are <= GW acres." ) );
		Sacre_set_adjusted = Acgw_prev;
	}
	
	// Set the groundwater acreage parts.  The total is already set so no need to call refreshAcgw().
	
	ipyts.setAcgwspr ( year, Sacre_set_adjusted );
	ipyts.setAcgwfl( year, (Acgw_prev - Sacre_set_adjusted) );

	// Indicate that a set command was used.
	if ( culoc != null ) {
		culoc.setHasSetIrrigationPracticeTSCommands(year);
	}
	
	// Now check the surface water...
	
	double Acsw_prev = ipyts.getAcsw(year);
	if ( Acsw_prev < 0.0 ) {
		message = "Location \"" + id + "\" " + year + ":  SWtotal is missing.";
	    Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Fill SWtotal before setting sprinkler." ) );
		return warning_count;
	}
	// TODO SAM 2007-09-11 Evaluate whether it should be allowed to set SWtotal to sprinkler if total is missing.

	// Surface sprinklers are the remainder after groundwater sprinklers are set, using the
	// original sprinkler acres from the file (not the adjusted limited by total groundwater)...
	
	double Acswspr = Sacre_set - ipyts.getAcgwspr(year);
	if ( Acswspr < 0.0 ) {
		// Do not allow negative surface water sprinkler.
		Acswspr = 0.0;
	}
	else if ( Acswspr > Acsw_prev ) {
		// Do not allow surface water sprinkler to be greater than the surface water total.
		Acswspr = Acsw_prev;
	}
	// Now set the components and refresh...
	ipyts.setAcswspr ( year, Acswspr );
	ipyts.setAcswfl ( year, (Acsw_prev - Acswspr) );
	ipyts.refreshAcsw ( year );
	
	/*
	double Acgwfl_prev = ipyts.getAcgwfl(year);
	if ( Acgwfl_prev < 0.0 ) {
		Message.printWarning ( 2,
				MessageUtil.formatMessageTag(command_tag, ++__run_warning_count),routine,
				"Location \"" + id + "\" " + year + ":  Setting GWflood = 0 since missing." );
		Acgwfl_prev = 0.0;
	}
	double Acgwspr_prev = ipyts.getAcgwspr(year);
	if ( Acgwspr_prev < 0.0 ) {
		Message.printWarning ( 2,
				MessageUtil.formatMessageTag(command_tag, ++__run_warning_count),routine,
				"Location \"" + id + "\" " + year + ":  Setting GWsprinkler = 0 since missing." );
		Acgwspr_prev = 0.0;
	}
	double Acgw_prev = Acgwfl_prev + Acgwspr_prev;
	
	double Acswfl_prev = ipyts.getAcswfl(year);
	if ( Acswfl_prev < 0.0 ) {
		Message.printWarning ( 2,
				MessageUtil.formatMessageTag(command_tag, ++__run_warning_count),routine,
				"Location \"" + id + "\" " + year + ":  Setting SWflood = 0 since missing." );
		Acswfl_prev = 0.0;
	}
	double Acswspr_prev = ipyts.getAcswspr(year);
	if ( Acswspr_prev < 0.0 ) {
		Message.printWarning ( 2,
				MessageUtil.formatMessageTag(command_tag, ++__run_warning_count),routine,
				"Location \"" + id + "\" " + year + ":  Setting SWsprinkler = 0 since missing." );
		Acswspr_prev = 0.0;
	}
	double Acsw_prev = Acswfl_prev + Acswspr_prev;
	
	// First set GW sprinkler acres to min(previous, set)
	double Acgwspr_min = Sacre_set;
	if ( (Acgwspr_prev >= 0.0) && (Acgwspr_prev < Acgwspr_min) ) {
		Acgwspr_min = Acgwspr_prev;
		Message.printStatus(2, routine, "For location " + id + " " + year +
				" setting GWsprinkler to previous GWsprinkler (" +
				StringUtil.formatString(Acgwspr_min,"%.3f") +
				") = minimum of previous and list file value." );
	}
	else {
		Message.printStatus(2, routine, "For location " + id + " " + year +
				" setting GWsprinkler to List_sprinkler (" +
				StringUtil.formatString(Acgwspr_min,"%.3f") +
				") = minimum of previous and list file value." );
	}
	ipyts.setAcgwspr( year, Acgwspr_min );
	double Acgwspr = Acgwspr_min;
	
	// Now set the surface water sprinkler as:
	//
	// SWsprinkler = min((List - GWsprinkler), SWprev)
	//
	// This prevents negative numbers.
	
	double Acswspr = Sacre_set - Acgwspr_min;
	if ( (Acswfl_prev + Acswspr_prev) < Acswspr ) {
		Acswspr = Acswfl_prev + Acswspr_prev;
		Message.printStatus(2, routine, "For location " + id + " " + year +
				" setting SWsprinkler to SWprevious (" +
				StringUtil.formatString(Acswspr,"%.3f") + ") previous=" +
				StringUtil.formatString(Acswspr_prev,"%.3f"));
	}
	else {
		Message.printStatus(2, routine, "For location " + id + " " + year +
				" setting SWsprinkler to List_sprinkler - GWsprinkler (" +
				StringUtil.formatString(Acswspr,"%.3f") + ") previous=" +
				StringUtil.formatString(Acswspr_prev,"%.3f"));
	}
	ipyts.setAcswspr( year, Acswspr );
	
	// Now set the GW flood as the groundwater total (previous) minus the
	// GW sprinkler.
	
	double Acgwfl = Acgw_prev - Acgwspr;
	ipyts.setAcgwfl( year, Acgwfl );
	Message.printStatus(2, routine, "For location " + id + " " + year +
			" setting GWflood to GW - GWsprinkler (" +
			StringUtil.formatString(Acgwfl,"%.3f") + ") previous=" +
			StringUtil.formatString(Acgwfl_prev,"%.3f"));
	
	// Now set the SW flood as the surfacewater total (previous) minus the
	// SW sprinkler
	
	double Acswfl = Acsw_prev - Acswspr;
	ipyts.setAcswfl( year, Acswfl );
	Message.printStatus(2, routine, "For location " + id + " " + year +
			" setting SWflood to SW - SWsprinkler (" +
			StringUtil.formatString(Acswfl,"%.3f") + ") previous=" +
			StringUtil.formatString(Acswfl_prev,"%.3f"));
	
	// Do a final check to make sure that the acreage values add up to the total...
	double Tacre_new = Acswfl + Acswspr + Acgwfl + Acgwspr;
	double tolerance = 0.1;
	if ( Math.abs(Tacre_new - Tacre) > tolerance ) {
		++warning_count;
		Message.printWarning(3, routine, "For location " + id +
				" the total acreage (" + StringUtil.formatString(Tacre,"%.3f") +
				") does not equal the sum of Acswfl (" + StringUtil.formatString(Acswfl,"%.3f") + ") + " +
				") does not equal the sum of Acswspr (" + StringUtil.formatString(Acswspr,"%.3f") + ") + " +
				") does not equal the sum of Acgwfl (" + StringUtil.formatString(Acgwfl,"%.3f") + ") + " +
				") does not equal the sum of Acgwfl (" + StringUtil.formatString(Acgwspr,"%.3f") + ")." );
	}
	*/
	return warning_count;
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
	String ID = parameters.getValue ( "ID" );
	String IDCol = parameters.getValue ( "IDCol" );
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	
	StringBuffer b = new StringBuffer ();
	if ( ListFile != null && ListFile.length() > 0 ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( ID != null && ID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( SetStart != null && SetStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetStart=" + SetStart  );
	}
	if ( SetEnd != null && SetEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEnd=" + SetEnd  );
	}
	if ( YearCol != null && YearCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "YearCol=" + YearCol  );
	}
	if ( IDCol != null && IDCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDCol=\"" + IDCol + "\"" );
	}
	if ( AcresSprinklerCol != null && AcresSprinklerCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSprinklerCol=\"" + AcresSprinklerCol + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
