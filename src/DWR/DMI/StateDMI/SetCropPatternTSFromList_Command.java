// SetCropPatternTSFromList_Command - This class initializes, checks, and runs the SetCropPatternTSFromList() command.

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
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Util;

import RTi.TS.YearTS;
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
<p>
This class initializes, checks, and runs the SetCropPatternTSFromList() command.
</p>
*/
public class SetCropPatternTSFromList_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "_False";
protected final String _True = "_True";

// ProcessWhen parameter values...

protected final String _Now = "Now";
protected final String _WithParcels = "WithParcels";

// SupplyType possible data values...

protected final String _Ground = "Ground";
protected final String _Surface = "Surface";

// Working directory
String __WorkingDir = null;

/**
Constructor.
*/
public SetCropPatternTSFromList_Command ()
{	super();
	setCommandName ( "SetCropPatternTSFromList" );
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
	String CropTypeCol = parameters.getValue ( "CropTypeCol" );
	String AreaCol = parameters.getValue ( "AreaCol" );
	String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
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

	if ( (YearCol != null) && (YearCol.length() > 0) ) {
		if ( !StringUtil.isInteger(YearCol) ) {
			message = "The year column (" + YearCol + ") is not a valid number.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify a valid year column as an integer." ) );
		}
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
	
	if ( (CropTypeCol != null) && (CropTypeCol.length() != 0) && !StringUtil.isInteger(CropTypeCol)) {
		message = "The crop type column (" + CropTypeCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the crop type column as an integer 1+." ) );
	}
	
	if ( (AreaCol != null) && (AreaCol.length() != 0) && !StringUtil.isInteger(AreaCol) ) {
		message = "The area column (" + AreaCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the area column as an integer 1+." ) );
	}
	
	if ( (IrrigationMethodCol != null) && (IrrigationMethodCol.length() != 0) &&
		!StringUtil.isInteger(IrrigationMethodCol) ) {
		message = "The irrigation method column (" + IrrigationMethodCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the irrigation method column as an integer 1+." ) );
	}
	
	if ( (SupplyTypeCol != null) && (SupplyTypeCol.length() != 0) &&
		!StringUtil.isInteger(SupplyTypeCol) ) {
		message = "The supply type column (" + SupplyTypeCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the supply type column as an integer 1+." ) );
	}
	
	if ( (ProcessWhen != null) && (ProcessWhen.length() != 0) &&
		!ProcessWhen.equalsIgnoreCase(_Now) && !ProcessWhen.equalsIgnoreCase(_WithParcels) ) {
		message = "The Process when parameter (" + ProcessWhen + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ProcessWhen parameter as " + _Now + " (default) or " +
                _WithParcels + ".") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(11);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
	valid_Vector.add ( "YearCol" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "CropTypeCol" );
	valid_Vector.add ( "AreaCol" );
	valid_Vector.add ( "IrrigationMethodCol" );
	valid_Vector.add ( "SupplyTypeCol" );
	valid_Vector.add ( "ProcessWhen" );
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
	return (new SetCropPatternTSFromList_JDialog ( parent, this )).ok();
}

/**
Method to execute the setCropPatternTSFromList() command.
@param command_number Number of command in sequence.
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
	int YearCol_int = -1;
	if ( YearCol != null ) {
		YearCol_int = Integer.parseInt (YearCol) - 1;
	}
	String IDCol = parameters.getValue ( "IDCol" );
	int IDCol_int = -1;
	if ( IDCol != null ) {
		IDCol_int = Integer.parseInt ( IDCol ) - 1;
	}
	String CropTypeCol = parameters.getValue ( "CropTypeCol" );
	int CropTypeCol_int = -1;
	if ( CropTypeCol != null ) {
		CropTypeCol_int = Integer.parseInt (CropTypeCol) - 1;
	}
	String AreaCol = parameters.getValue ( "AreaCol" );
	int AreaCol_int = -1;
	if ( AreaCol != null ) {
		AreaCol_int = Integer.parseInt ( AreaCol) - 1;
	}
	String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	int IrrigationMethodCol_int = -1;
	if ( IrrigationMethodCol != null ) {
		IrrigationMethodCol_int = StringUtil.atoi (IrrigationMethodCol) - 1;
	}
	String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	int SupplyTypeCol_int = -1;
	if ( SupplyTypeCol != null ) {
		SupplyTypeCol_int = StringUtil.atoi (SupplyTypeCol) - 1;
	}
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	int Now_int = 0;
	int WithParcels_int = 1;
	int ProcessWhen_int = Now_int;
	if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels)){
		ProcessWhen_int = WithParcels_int;
	}
	if ( ProcessWhen == null ) {
		ProcessWhen = _Now; // Default
	}
	
	// Get the time series to fill...

	List<StateCU_CropPatternTS> CUCropPatternTS_Vector = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropPatternTS> dataList = (List<StateCU_CropPatternTS>)processor.getPropContents( "StateCU_CropPatternTS_List");
		CUCropPatternTS_Vector = dataList;
	}
	catch ( Exception e ) {
		Message.printWarning ( log_level, routine, e );
		message = "Unable to get crop pattern time series.  Verify that data were read.";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
	}
	
	// Get the supplemental ParcelUseTS so that records can be added if 'ProcessWhen=WithParcels'...
	
	List<StateDMI_HydroBase_ParcelUseTS> Supplemental_ParcelUseTS_Vector = null;
	if ( ProcessWhen.equalsIgnoreCase(_WithParcels) ) {
		try {
			@SuppressWarnings("unchecked")
			List<StateDMI_HydroBase_ParcelUseTS> dataList = (List<StateDMI_HydroBase_ParcelUseTS>)processor.getPropContents( "HydroBase_SupplementalParcelUseTS_List");
			Supplemental_ParcelUseTS_Vector = dataList;
		}
		catch ( Exception e ) {
			Message.printWarning ( log_level, routine, e );
			message = "Unable to get supplemental ParcelUseTS data.  Software bug.";
        	Message.printWarning ( warning_level, 
            	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        	status.addToLog ( command_phase,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Report to software support.  See log file for details." ) );
		}
	}

	// Get the list of CU locations, used to check the data ...
	
	List<StateCU_Location> culocList = null;
	//int culocListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_Location> dataList = (List<StateCU_Location>)processor.getPropContents( "StateCU_Location_List");
		culocList = dataList;
		//culocListSize = culocList.size();
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
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the output start with SetOutputPeriod() prior to this command." ) );
		}
		else {
			SetStart_int = OutputStart_DateTime.getYear();
		}
	}
	else {
		SetStart_int = Integer.parseInt(SetStart);
	}

	if ( SetEnd == null ) {
		if ( OutputEnd_DateTime == null ) {
			message = "Set end and global OutputEnd are not set.";
	        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the output end with SetOutputPeriod() prior to this command." ) );
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
		props.set ( "Delimiter=," );		// see existing prototype
		props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
		props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
		DataTable table = DataTable.parseFile ( ListFile_full, props );

		int tsize = 0;
		if ( table != null ) {
			tsize = table.getNumberOfRecords();
		}

		Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
			table.getNumberOfFields() + " fields" );
		
		// Make sure that requested columns are available
    	
    	warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"YearCol", YearCol, YearCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"CropTypeCol", CropTypeCol, CropTypeCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"AreaCol", AreaCol, AreaCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"IrrigationMethodCol", IrrigationMethodCol, IrrigationMethodCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );
		warning_count = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
			"SupplyTypeCol", SupplyTypeCol, SupplyTypeCol_int, table.getNumberOfFields(),
			status, command_phase, routine, command_tag, warning_level, warning_count );

		// Do this the brute force way...
		
		// Create an array to indicate how many records have been processed for a location (may be
		// multiple records for a location in the list file).  The first encounter will trigger a zeroing
		// in the file.
		
		int [] CUCropPatternTS_encountered = new int[CUCropPatternTS_Vector.size()];
		for ( int i = 0; i < CUCropPatternTS_encountered.length; i++ ) {
			CUCropPatternTS_encountered[i] = 0;
		}

		// Loop through the list file records.  If the CU location is matched,
		// find the irrigation practice information for the location.

		StateCU_CropPatternTS cds;
		String Year_String = "";
		String CropType_String = "";
		String Area_String = "";
		String IrrigationMethod_String = "";
		String SupplyType_String = "";
		boolean fill_CropType = false;
		boolean fill_Area = false;
		boolean fill_IrrigationMethod = false;
		boolean fill_SupplyType = false;
		TableRecord rec = null;
		// TODO smalers 2019-05-28 enable matchCount
		//int matchCount = 0;
		int year, year1, year2; // Year for iterator, and bounds on iteration
		int cdsPos = 0; // Location of ID in CropPatternTS object
		int Year_int; // Integer value of year from column in file
		String cds_id, id;
		// Loop through the table and see if there are any matches for the
		// CU Location ID for the record.  This will be relatively fast if no
		// YearCol is given and/or if the ID=*.
		for ( int j = 0; j < tsize; j++ ) {
			rec = table.getRecord(j);
			id = (String)rec.getFieldValue(IDCol_int);
			cds = null;	// Initialize to prevent compiler warnings
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

			// Find the StateCU_IrrigationPracticeTS instance to modify...
			if ( ProcessWhen_int == Now_int ) {
				cdsPos = StateCU_Util.indexOf(CUCropPatternTS_Vector,id);
				if ( cdsPos < 0 ) {
					message = "List file location \"" + id +
						"\" does not match any CU locations.  Cannot set crop pattern.";
			        Message.printWarning ( warning_level, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( command_phase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that list file location identifiers match CU locations." ) );
					continue;
				}
				cds = (StateCU_CropPatternTS)CUCropPatternTS_Vector.get(cdsPos);
				// Increment the counter of records processed for the location
				++CUCropPatternTS_encountered[cdsPos];
			}
			// OK to set the data...
			// Get the data values from the table one time...
			//++matchCount;
			cds_id = id;
			// Also get the CU location, used later to set the StateCU_Location list of years with CDS set commands.
			int culocPos = StateCU_Util.indexOf(culocList,cds_id);

			Year_int = -1;	// Indicate not to set for a specific year
			if ( YearCol != null ) {
				Year_String = (String)rec.getFieldValue( YearCol_int);
				if ( !StringUtil.isInteger( Year_String)) {
					message = "Year in list file (" + Year_String
					+ ") column " + (YearCol + 1) + " is not a number.  Skipping record " + (j + 1) + ".";
			        Message.printWarning ( warning_level, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( command_phase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the year in the list file is an integer." ) );
					continue;
				}
				else {
					// Convert for use below...
					Year_int = Integer.parseInt ( Year_String );
				}
			}
			fill_CropType = false;
			if ( CropTypeCol != null ) {
				CropType_String = (String)rec.getFieldValue(CropTypeCol_int);
				fill_CropType = true;
			}
			fill_Area = false;
			if ( AreaCol != null ) {
				Area_String = (String)rec.getFieldValue(AreaCol_int);
				fill_Area = true;
				if ( !StringUtil.isDouble(Area_String)) {
					message = "Area (" + Area_String + ") in column " + (AreaCol_int + 1) +
					"is not a number.  Skipping record " + (j + 1) + ".";
			        Message.printWarning ( warning_level, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( command_phase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the area in the list file is a number." ) );
					fill_Area = false;
				}
			}
			if ( fill_CropType && fill_Area && (Year_int > 0) ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine, "Setting " + cds_id + " crop " + CropType_String +
					" area for year " + Year_int + " -> " + Area_String );
					setCropPatternTS ( cds, Year_int, Year_int, CropType_String, StringUtil.atod(Area_String),
						CUCropPatternTS_encountered[cdsPos] );
				}
			}
			fill_IrrigationMethod = false;
			if ( IrrigationMethodCol != null ) {
				IrrigationMethod_String = (String)rec.getFieldValue(IrrigationMethodCol_int);
				fill_IrrigationMethod = true;
				// Data will be used below.
			}
			fill_SupplyType = false;
			if ( SupplyTypeCol != null ) {
				SupplyType_String = (String)rec.getFieldValue(SupplyTypeCol_int);
				fill_SupplyType = true;
				// Data will be used below.
			}
			// If the crop and year were not provided, no need to set.
			if ( !fill_CropType || !fill_Area ) {
				continue;
			}
			// Now have the data for the record.  Set the data depending on whether
			// processing now and whether the year was specified in the record.
			if ( Year_int > 0 ) {
				// Record had the year so use it for start and end of set
				year1 = Year_int;
				year2 = Year_int;
			}
			else {
				// Set for the set (or output period)...
				year1 = SetStart_int;
				year2 = SetEnd_int;
			}
			for ( year = year1; year <= year2; year++ ){
				if ( ProcessWhen_int == WithParcels_int ) {
					// Instead of setting the data, save supplemental records for
					// use with the readCropPatternTSFromHydroBase command.
					StateDMI_HydroBase_ParcelUseTS hbputs = new StateDMI_HydroBase_ParcelUseTS();
					hbputs.setCal_year ( year );
					hbputs.setLocationID ( id );
					// Records are primitive so store total acres...
					hbputs.setArea ( StringUtil.atod(Area_String) );
					hbputs.setLand_use ( CropType_String );
					// Optional - may be used for troubleshooting
					if ( fill_IrrigationMethod ) {
						hbputs.setIrrig_type ( IrrigationMethod_String );
					}
					if ( fill_SupplyType ) {
						hbputs.setSupply_type ( SupplyType_String );
					}
					Supplemental_ParcelUseTS_Vector.add ( hbputs );
				}
				else {
					// Set the data now
					// Print the message once...
					if ( year == SetStart_int ) {
						Message.printStatus ( 2, routine, "Setting " + cds_id + " CropType " +
							CropType_String + " area " + SetStart_int + " to " + SetEnd_int + " -> "+
							Area_String );
					}
					setCropPatternTS (
						cds,
						year,	// Iterating from SetStart to SetEnd
						year,
						CropType_String,
						StringUtil.atod(Area_String),
						CUCropPatternTS_encountered[cdsPos] );
				} // End process now or with parcels

				// Indicate that location has a set command, used with parcel report file.
				// - OK to set extra years in a span
				if ( culocPos >= 0 ) {
					StateCU_Location culoc = culocList.get(culocPos);
					culoc.setHasSetCropPatternTSCommands(year);
				}

			} // End year loop
		}
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting crop pattern time series data (" + e + ").";
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
Set the crop pattern data for a single location.  The data are first set to zero and then to the new crop.
@param cds Crop pattern time series to set data.
@param setstart Starting year to set data.
@param setend Ending year to set data.
@param crop_type Crop type to set.
@param area Crop area to set.
*/
private void setCropPatternTS ( StateCU_CropPatternTS cds, int setstart, int setend, String crop_type,
	double area, int recordCount )
throws Exception
{
	if ( recordCount == 1 ) {
		// Zero the crops first so that set data are new values.
		setCropPatternTSToZero(cds, setstart, setend);
	}
	
	// Now reset to new data.  The number of crops does not
	// need to match the original.  First make sure the crop time series is available.
	
	YearTS yts = cds.getCropPatternTS ( crop_type );
	if ( yts == null ) {
		// Add the crop time series...
		cds.addTS ( crop_type, true );
	}

	for ( int year = setstart; year <= setend; year++){
		cds.setCropArea ( crop_type, year, area );
	}

	// Refresh the contents to calculate total area...

	cds.refresh();
}

/**
Set the crop pattern time series to zero - this is typically done the first time that a location is
encountered.
@param cds Crop pattern time series object to process.
@param setStart starting year to set to zero.
@param setEnd ending year to set to zero.
*/
private void setCropPatternTSToZero ( StateCU_CropPatternTS cds, int setStart, int setEnd )
throws Exception
{
	// Reset the data.
	// First set the existing crop patterns for the location
	// to zero, for the specified year(s).  This will ensure that any crops not
	// mentioned in the command are set to zero for the given years...

	List<String> crop_names = cds.getCropNames();
	int ncrop_names = 0;
	if ( crop_names != null ) {
		ncrop_names = crop_names.size();
	}
	for ( int ic = 0; ic < ncrop_names; ic++ ) {
		for ( int year = setStart; year <= setEnd; year++ ){
			cds.setCropArea( crop_names.get(ic), year, 0.0 );
		}
	}
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
	String CropTypeCol = parameters.getValue ( "CropTypeCol" );
	String AreaCol = parameters.getValue ( "AreaCol" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	
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
	if ( CropTypeCol != null && CropTypeCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropTypeCol=\"" + CropTypeCol+"\"");
	}
	if ( AreaCol != null && AreaCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AreaCol=\"" + AreaCol + "\"" );
	}
	if ( ProcessWhen != null && ProcessWhen.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessWhen=" + ProcessWhen );
	}
	if ( IrrigationMethodCol != null && IrrigationMethodCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigationMethodCol=\"" + IrrigationMethodCol + "\"" );
	}
	if ( SupplyTypeCol != null && SupplyTypeCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SupplyTypeCol=\"" + SupplyTypeCol + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
