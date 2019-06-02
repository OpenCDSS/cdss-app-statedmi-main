// SetIrrigationPracticeTSFromList_Command - This class initializes, checks, and runs the SetIrrigationPracticeTSFromList() command.

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

import DWR.StateCU.StateCU_IrrigationPracticeTS;
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
<p>
This class initializes, checks, and runs the SetIrrigationPracticeTSFromList() command.
The command sets irrigation practice time series to the values in the list file, as
the command is processed.  It no longer specifies values for processing with HydroBase data.
</p>
*/
public class SetIrrigationPracticeTSFromList_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "_False";
protected final String _True = "_True";

// SupplyType possible data values...

protected final String _Ground = "Ground";
protected final String _Surface = "Surface";

/**
Constructor.
*/
public SetIrrigationPracticeTSFromList_Command ()
{	super();
	setCommandName ( "SetIrrigationPracticeTSFromList" );
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
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue ( "SprinklerAppEffMaxCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
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
        message = "The set start year (" + SetStart + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid start year YYYY." ) );
	}
	if ( (SetEnd != null) && (SetEnd.length() > 0) && !StringUtil.isInteger(SetEnd) ) {
        message = "The set end year (" + SetEnd + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid end year YYYY." ) );
	}

	if ( (YearCol != null) && !StringUtil.isInteger(YearCol) ) {
		message = "The year column (" + YearCol + ") is not a valid number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid year column as an integer." ) );
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
	if ( (SurfaceDelEffMaxCol != null) && (SurfaceDelEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(SurfaceDelEffMaxCol) ) {
		message = "The surface water delivery maximum efficiency column (" +
			SurfaceDelEffMaxCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (FloodAppEffMaxCol != null) && (FloodAppEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(FloodAppEffMaxCol) ) {
		message = "The flood application max eff column (" + FloodAppEffMaxCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (SprinklerAppEffMaxCol != null) && (SprinklerAppEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(SprinklerAppEffMaxCol) ) {
		message = "The sprinkler application max eff. column (" +SprinklerAppEffMaxCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (AcresSWFloodCol != null) && (AcresSWFloodCol.length() > 0) &&
		!StringUtil.isInteger(AcresSWFloodCol) ) {
		message = "\nThe acres irrigated by surface water (flood) column (" +
			AcresSWFloodCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (AcresSWSprinklerCol != null) && (AcresSWSprinklerCol.length() > 0) &&
		!StringUtil.isInteger(AcresSWSprinklerCol) ) {
		message = "The acres irrigated by surface water (sprinkler) column (" +
			AcresSWSprinklerCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (AcresGWFloodCol != null) && (AcresGWFloodCol.length() > 0) &&
		!StringUtil.isInteger(AcresGWFloodCol) ) {
		message = "The acres irrigated by groundwater (flood) column: \"" +
			AcresGWFloodCol + "\" is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (AcresGWSprinklerCol != null) && (AcresGWSprinklerCol.length() > 0) &&
		!StringUtil.isInteger(AcresGWFloodCol) ) {
		message = "The acres irrigated by groundwater (sprinkler) column (" +
			AcresGWSprinklerCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (PumpingMaxCol != null) && (PumpingMaxCol.length() > 0) &&
		!StringUtil.isInteger(PumpingMaxCol) ) {
		message = "The maximum pumping column (" + PumpingMaxCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (GWModeCol != null) && (GWModeCol.length() > 0) && !StringUtil.isInteger(GWModeCol) ) {
		message = "The groundwater mode column (" + GWModeCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	if ( (AcresTotalCol != null) && (AcresTotalCol.length() > 0) &&
		!StringUtil.isInteger(AcresTotalCol) ) {
		message = "The total acres column (" + AcresTotalCol + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid column as an integer." ) );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(20);
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
	valid_Vector.add ( "YearCol" );
	valid_Vector.add ( "SurfaceDelEffMaxCol" );
	valid_Vector.add ( "FloodAppEffMaxCol" );
	valid_Vector.add ( "SprinklerAppEffMaxCol" );
	valid_Vector.add ( "AcresGWFloodCol" );
	valid_Vector.add ( "AcresGWSprinklerCol" );
	valid_Vector.add ( "AcresSWFloodCol" );
	valid_Vector.add ( "AcresSWSprinklerCol" );
	valid_Vector.add ( "PumpingMaxCol" );
	valid_Vector.add ( "GWModeCol" );
	valid_Vector.add ( "AcresTotalCol" );
	valid_Vector.add ( "InputFile1" );
	valid_Vector.add ( "InputFile2" );
	valid_Vector.add ( "WarnIfDifferent" );
	valid_Vector.add ( "WarnIfSame" );
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
	return (new SetIrrigationPracticeTSFromList_JDialog ( parent, this )).ok();
}

/**
Method to execute the setIrrigationPracticeTSFromList() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	int log_level = 3;  // Log level for non-user warnings
	
	CommandPhaseType commandPhase = CommandPhaseType.RUN;
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
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue ( "SprinklerAppEffMaxCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
	//String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	//String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	//String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	
	// Get the irrigation practice time series to process.
	
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
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( ipyListSize == 0 ) {
		message = "No irrigation practice time series are defined.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Run CreateIrrigationPracticeTSForCULocations() before this command." ) );
	}
	
	// Get the supplemental crop pattern data specified with SetCropPatternTS() and
	// SetCropPatternTSFromList() commands...
	/*
	List hydroBaseSupplementalParcelUseTSList = null;
	try {
		hydroBaseSupplementalParcelUseTSList =
			(List)processor.getPropContents ( "HydroBase_SupplementalParcelUseTS_List");
	}
	catch ( Exception e ) {
		message = "Error requesting supplemental parcel use data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	*/
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
    // Output period
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
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
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
	
	// Get the period to set (default if fill period is not specified)...
	
	int SetStart_int = 0, SetEnd_int = 0;
	if ( SetStart == null ) {
		if ( OutputStart_DateTime == null ) {
			message = "Set start and global OutputStart are not set.";
	        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Use the SetStart parameter or SetOutputCommand()." ) );
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
	        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Use the SetEnd parameter or SetOutputCommand()." ) );
		}
		else {
			SetEnd_int = OutputEnd_DateTime.getYear();
		}
	}
	else {
		SetEnd_int = StringUtil.atoi(SetEnd);
	}
	
	try {
		String ListFile_full = IOUtil.verifyPathForOS(
	        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
                StateDMICommandProcessorUtil.expandParameterValue(processor,this,ListFile)));

		// The remaining columns are optional...
		
		int IDCol_int = StringUtil.atoi ( IDCol ) - 1;

		int YearCol_int = -1;
		if ( YearCol != null ) {
			YearCol_int = Integer.parseInt (YearCol) - 1;
		}
		
		int SurfaceDelEffMaxCol_int = -1;
		if ( SurfaceDelEffMaxCol != null ) {
			SurfaceDelEffMaxCol_int = Integer.parseInt (SurfaceDelEffMaxCol) - 1;
		}

		int FloodAppEffMaxCol_int = -1;
		if ( FloodAppEffMaxCol != null ) {
			FloodAppEffMaxCol_int = Integer.parseInt ( FloodAppEffMaxCol) - 1;
		}

		int SprinklerAppEffMaxCol_int = -1;
		if ( SprinklerAppEffMaxCol != null ) {
			SprinklerAppEffMaxCol_int = Integer.parseInt (SprinklerAppEffMaxCol)-1;
		}

		/*
		int AcresGWCol_int = -1;
		if ( AcresGWCol != null ) {
			AcresGWCol_int = Integer.parseInt (AcresGWCol) - 1;
		}

		int AcresSprinklerCol_int = -1;
		if ( AcresSprinklerCol != null ) {
			AcresSprinklerCol_int = Integer.parseInt (AcresSprinklerCol) - 1;
		}
		*/
		
		int AcresGWFloodCol_int = -1;
		if ( AcresGWFloodCol != null ) {
			AcresGWFloodCol_int = Integer.parseInt (AcresGWFloodCol) - 1;
		}
		
		int AcresGWSprinklerCol_int = -1;
		if ( AcresGWSprinklerCol != null ) {
			AcresGWSprinklerCol_int = Integer.parseInt (AcresGWSprinklerCol) - 1;
		}
		
		int AcresSWFloodCol_int = -1;
		if ( AcresSWFloodCol != null ) {
			AcresSWFloodCol_int = Integer.parseInt (AcresSWFloodCol) - 1;
		}
		
		int AcresSWSprinklerCol_int = -1;
		if ( AcresSWSprinklerCol != null ) {
			AcresSWSprinklerCol_int = Integer.parseInt (AcresSWSprinklerCol) - 1;
		}

		int PumpingMaxCol_int = -1;
		if ( PumpingMaxCol != null ) {
			PumpingMaxCol_int = Integer.parseInt (PumpingMaxCol) - 1;
		}

		int GWModeCol_int = -1;
		if ( GWModeCol != null ) {
			GWModeCol_int = Integer.parseInt (GWModeCol) - 1;
		}

		int AcresTotalCol_int = -1;
		if ( AcresTotalCol != null ) {
			AcresTotalCol_int = StringUtil.atoi (AcresTotalCol) - 1;
		}
		
		/*
		int Now_int = 0;
		int WithParcels_int = 1;
		int ProcessWhen_int = Now_int;
		if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels)){
			ProcessWhen_int = WithParcels_int;
		}
		*/
		
		/*
		int IrrigationMethodCol_int = -1;
		if ( IrrigationMethodCol != null ) {
			IrrigationMethodCol_int = Integer.parseInt (IrrigationMethodCol) - 1;
		}
		
		int SupplyTypeCol_int = -1;
		if ( SupplyTypeCol != null ) {
			SupplyTypeCol_int = Integer.parseInt (SupplyTypeCol) - 1;
		}
		*/

		// Read the list file using the table...

		PropList props = new PropList ("");
		props.set ( "Delimiter=," ); // see existing prototype
		props.set ( "CommentLineIndicator=#" );	// New - skip lines that start with this
		props.set ( "TrimStrings=True" );	// If true, trim strings after reading.
		DataTable table = DataTable.parseFile ( ListFile_full, props );

		int tsize = 0;
		if ( table != null ) {
			tsize = table.getNumberOfRecords();
		}

		Message.printStatus ( 2, "", "Table has " + tsize + " records and " +
			table.getNumberOfFields() + " fields" );

		// Do this the brute force way...

		// Loop through the list file records.  If the CU location is matched,
		// find the irrigation practice information for the location.

		StateCU_IrrigationPracticeTS ipy;
		String Year_String = "";
		String SurfaceDelEffMax_String = "";
		String FloodAppEffMax_String = "";
		String SprinklerAppEffMax_String = "";
		//String AcresGW_String = "";
		//String AcresSprinkler_String = "";
		String AcresGWFlood_String = "";
		String AcresGWSprinkler_String = "";
		String AcresSWFlood_String = "";
		String AcresSWSprinkler_String = "";
		String PumpingMax_String = "";
		String GWMode_String = "";
		String AcresTotal_String = "";
		//String IrrigationMethod_String = "";
		//String SupplyType_String = "";
		boolean fill_SurfaceDelEffMax = false;
		boolean fill_FloodAppEffMax = false;
		boolean fill_SprinklerAppEffMax = false;
		//boolean fill_AcresGW = false;
		//boolean fill_AcresSprinkler = false;
		boolean fill_AcresGWFlood = false;
		boolean fill_AcresGWSprinkler = false;
		boolean fill_AcresSWFlood = false;
		boolean fill_AcresSWSprinkler = false;
		boolean fill_PumpingMax = false;
		boolean fill_GWMode = false;
		boolean fill_AcresTotal = false;
		//boolean fill_IrrigationMethod = false;
		//boolean fill_SupplyType = false;
		TableRecord rec = null;
		// TODO smalers 2019-05-28 enable matchCount
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
			// Find the StateCU_IrrigationPracticeTS instance to modify...
			pos = StateCU_Util.indexOf(ipyList,id);
			if ( pos < 0 ) {
				message = "List file location \"" + id + "\" does not match any CU locations.";
		        Message.printWarning ( warningLevel, 
	                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
	            status.addToLog ( commandPhase,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Verify that the identifier matches a CU location identifier." +
	                    	"This command can only be used to set data at model locations (not parts of aggregate/systems)" ) );
				continue;
			}
			ipy = (StateCU_IrrigationPracticeTS)ipyList.get(pos);
			// OK to set the data...
			// Get the data values from the table one time...
			//++matchCount;
			ipy_id = id;
			Year_int = -1; // Indicate not to set for a specific year
			if ( YearCol != null ) {
				Year_String = (String)rec.getFieldValue( YearCol_int);
				if ( !StringUtil.isInteger( Year_String)) {
					 message = "Year in list file (" + Year_String
					 + ") is not an integer.  Skipping record " + (j + 1) + ".)";
					 Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the year in the list file is an integer." ) );
					continue;
				}
				else {
					// Convert for use below...
					Year_int = Integer.parseInt ( Year_String );
				}
			}
			fill_SurfaceDelEffMax = false;
			if ( SurfaceDelEffMaxCol != null ) {
				SurfaceDelEffMax_String = (String)rec.getFieldValue(SurfaceDelEffMaxCol_int);
				fill_SurfaceDelEffMax = true;
				if ( !StringUtil.isDouble(SurfaceDelEffMax_String)) {
					message = "SurfaceDelEffMax (" + SurfaceDelEffMax_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the efficiency value in the list file is a number." ) );
					fill_SurfaceDelEffMax = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id +
					" SurfaceDelEffMax " + Year_int + " -> " + SurfaceDelEffMax_String );
					ipy.setCeff ( Year_int, Double.parseDouble(SurfaceDelEffMax_String));
				}
			}
			fill_FloodAppEffMax = false;
			if ( FloodAppEffMaxCol != null ) {
				FloodAppEffMax_String = (String)rec.getFieldValue(FloodAppEffMaxCol_int);
				fill_FloodAppEffMax = true;
				if ( !StringUtil.isDouble(FloodAppEffMax_String)) {
					message = "FloodAppEffMax (" + FloodAppEffMax_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the efficiency value in the list file is a number." ) );
					fill_FloodAppEffMax = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " FloodAppEffMax " +
					Year_int + " -> "+ FloodAppEffMax_String );
					ipy.setFeff ( Year_int, Double.parseDouble(FloodAppEffMax_String));
					fill_FloodAppEffMax = true;
				}
			}
			fill_SprinklerAppEffMax = false;
			if ( SprinklerAppEffMaxCol != null ) {
				SprinklerAppEffMax_String = (String)rec.getFieldValue(SprinklerAppEffMaxCol_int);
				fill_SprinklerAppEffMax = true;
				if ( !StringUtil.isDouble(SprinklerAppEffMax_String)) {
					message = "SprinklerAppEffMax (" + SprinklerAppEffMax_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the efficiency value in the list file is a number." ) );
					fill_SprinklerAppEffMax = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " SprinklerAppEffMax " +
					Year_int + " -> "+ SprinklerAppEffMax_String );
					ipy.setSeff ( Year_int, Double.parseDouble(SprinklerAppEffMax_String));
				}
			}
			/*
			fill_AcresGW = false;
			if ( AcresGWCol != null ) {
				AcresGW_String = (String)rec.getFieldValue(
				AcresGWCol_int);
				fill_AcresGW = true;
				if ( !StringUtil.isDouble(
					AcresGW_String)) {
					Message.printWarning(2,
					MessageUtil.formatMessageTag(command_tag,
					++warning_count), routine,
					"AcresGW (" + AcresGW_String
					+ ") is not a number.");
					fill_AcresGW = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					if ( ProcessWhen_int == Now_int ) {
						Message.printStatus ( 2, routine,
						"Setting " + ipy_id + " AcresGW " + Year_int +
						" -> " + AcresGW_String );
						ipy.setGacre ( Year_int,
							StringUtil.atod(
							AcresGW_String));
					}
					fill_AcresGW = true;
				}
			}
			fill_AcresSprinkler = false;
			if ( AcresSprinklerCol != null ) {
				AcresSprinkler_String = (String)rec.getFieldValue(
				AcresSprinklerCol_int);
				fill_AcresSprinkler = true;
				if ( !StringUtil.isDouble(
					AcresSprinkler_String)) {
					Message.printWarning(2,
					MessageUtil.formatMessageTag(command_tag,
					++warning_count), routine,
					"AcresSprinkler (" + AcresSprinkler_String
					+ ") is not a number.");
					fill_AcresSprinkler = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					if ( ProcessWhen_int == Now_int ) {
						Message.printStatus ( 2, routine,
						"Setting " + ipy_id + " AcresSprinkler " +
						Year_int + " -> " + AcresSprinkler_String );
						ipy.setSacre ( Year_int,
						StringUtil.atod(
						AcresSprinkler_String));
					}
				}
			}
			*/
			fill_AcresGWFlood = false;
			if ( AcresGWFloodCol != null ) {
				AcresGWFlood_String = (String)rec.getFieldValue(AcresGWFloodCol_int);
				fill_AcresGWFlood = true;
				if ( !StringUtil.isDouble(AcresGWFlood_String)) {
					message = "AcresGWFlood (" + AcresGWFlood_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_AcresGWFlood = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresGWFlood " + Year_int +
						" -> " + AcresGWFlood_String );
					ipy.setAcgwfl ( Year_int, Double.parseDouble(AcresGWFlood_String));
				}
			}
			fill_AcresGWSprinkler = false;
			if ( AcresGWSprinklerCol != null ) {
				AcresGWSprinkler_String = (String)rec.getFieldValue(AcresGWSprinklerCol_int);
				fill_AcresGWSprinkler = true;
				if ( !StringUtil.isDouble(AcresGWSprinkler_String)) {
					message = "AcresGWSprinkler (" + AcresGWSprinkler_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_AcresGWSprinkler = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresGWSprinkler " + Year_int +
					" -> " + AcresGWSprinkler_String );
					ipy.setAcgwspr ( Year_int, Double.parseDouble(AcresGWSprinkler_String));
				}
			}
			fill_AcresSWFlood = false;
			if ( AcresSWFloodCol != null ) {
				AcresSWFlood_String = (String)rec.getFieldValue(AcresSWFloodCol_int);
				fill_AcresSWFlood = true;
				if ( !StringUtil.isDouble(AcresSWFlood_String)) {
					message = "AcresSWFlood (" + AcresSWFlood_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_AcresSWFlood = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresSWFlood " + Year_int +
						" -> " + AcresSWFlood_String );
					ipy.setAcswfl ( Year_int, Double.parseDouble(AcresSWFlood_String));
				}
			}
			fill_AcresSWSprinkler = false;
			if ( AcresSWSprinklerCol != null ) {
				AcresSWSprinkler_String = (String)rec.getFieldValue(AcresSWSprinklerCol_int);
				fill_AcresSWSprinkler = true;
				if ( !StringUtil.isDouble(AcresSWSprinkler_String)) {
					message = "AcresSWSprinkler (" + AcresSWSprinkler_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_AcresSWSprinkler = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresSWSprinkler " + Year_int +
						" -> " + AcresSWSprinkler_String );
					ipy.setAcswspr ( Year_int, Double.parseDouble(AcresSWSprinkler_String));
				}
			}
			fill_PumpingMax = false;
			if ( PumpingMaxCol != null ) {
				PumpingMax_String = (String)rec.getFieldValue(PumpingMaxCol_int);
				fill_PumpingMax = true;
				if ( !StringUtil.isDouble(PumpingMax_String)) {
					message = "PumpingMax (" + PumpingMax_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_PumpingMax = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine, "Setting " + ipy_id + " PumpingMax " +
					Year_int + " -> "+ PumpingMax_String );
					ipy.setMprate ( Year_int, Double.parseDouble( PumpingMax_String));
				}
			}
			fill_GWMode = false;
			if ( GWModeCol != null ) {
				GWMode_String = (String)rec.getFieldValue(GWModeCol_int);
				fill_GWMode = true;
				if ( !StringUtil.isInteger( GWMode_String)) {
					message = "GWMode (" + GWMode_String + ") is not an integer.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the mode value in the list file is an integer." ) );
					fill_GWMode = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine,
						"Setting " + ipy_id + " GWMode " + Year_int + " -> " + GWMode_String );
					ipy.setGmode ( Year_int, Integer.parseInt( GWMode_String));
				}
			}
			fill_AcresTotal = false;
			if ( AcresTotalCol != null ) {
				AcresTotal_String = (String)rec.getFieldValue(AcresTotalCol_int);
				fill_AcresTotal = true;
				if ( !StringUtil.isDouble(AcresTotal_String)) {
					message = "AcresTotal (" + AcresTotal_String + ") is not a number.";
					Message.printWarning ( warningLevel, 
		                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		            status.addToLog ( commandPhase,
		                new CommandLogRecord(CommandStatusType.FAILURE,
		                    message, "Verify that the acreage value in the list file is a number." ) );
					fill_AcresTotal = false;
				}
				else if ( Year_int > 0 ) {
					// Set the data for the specified year...
					Message.printStatus ( 2, routine,
						"Setting " + ipy_id + " AcresTotal " + Year_int + " -> " + AcresTotal_String );
					ipy.setTacre ( Year_int,Double.parseDouble( AcresTotal_String));
				}
				// Refresh the groundwater total and surface water total only if the parts are not
				// missing.  Do not cascade the adjustments because this set command depends on the user
				// setting information completely, or relying on later commands to fill
				// This is OK because the user is not able to set these values.
				ipy.refreshAcgw ( Year_int );
				ipy.refreshAcsw ( Year_int );
			}
			/*
			fill_IrrigationMethod = false;
			if ( IrrigationMethodCol != null ) {
				IrrigationMethod_String = (String)rec.getFieldValue(
				IrrigationMethodCol_int);
				fill_IrrigationMethod = true;
				// Data will be used below.
			}
			fill_SupplyType = false;
			if ( SupplyTypeCol != null ) {
				SupplyType_String = (String)rec.getFieldValue(
				SupplyTypeCol_int);
				fill_SupplyType = true;
				// Data will be used below.
			}
			*/
			/*
			if ( (ProcessWhen_int == WithParcels_int) && (Year_int > 0) ) {
				// Instead of setting the data, save supplemental records for
				// use with the readIrrigationPracticeTSFromHydroBase command.
				StateDMI_HydroBase_ParcelUseTS hbputs = new StateDMI_HydroBase_ParcelUseTS();
				hbputs.setCal_year ( Year_int );
				hbputs.setLocationID ( id );
				// Records are primitive so store total acres...
				// Not required
				//if ( fill_CropType ) {
				//	hbputs.setLand_use ( StringUtil.atod(CropType_String) );
				//}
				if ( fill_AcresTotal ) {
					hbputs.setArea ( StringUtil.atod(AcresTotal_String) );
				}
				if ( fill_IrrigationMethod ) {
					hbputs.setIrrig_type ( IrrigationMethod_String );
				}
				if ( fill_SupplyType ) {
					hbputs.setSupply_type ( SupplyType_String );
				}
				Supplemental_ParcelUseTS_Vector.addElement ( hbputs );
			}
			*/
			// Set for the specified period if the year column was not specified.
			// This should only get executed once per location if the list file is set up properly.
			if ( YearCol == null ) {
				for ( year = SetStart_int; year <= SetEnd_int; year++ ){
					if ( fill_SurfaceDelEffMax ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " SurfaceDelEffMax " +
								SetStart_int + " to " + SetEnd_int + " -> " + SurfaceDelEffMax_String );
						}
						ipy.setCeff ( year,	Double.parseDouble(SurfaceDelEffMax_String));
					}
					if ( fill_FloodAppEffMax ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " FloodAppEffMax " +
							SetStart_int + " to " + SetEnd_int + " -> " + FloodAppEffMax_String );
						}
						ipy.setFeff ( year, Double.parseDouble(FloodAppEffMax_String));
					}
					if ( fill_SprinklerAppEffMax ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " SprinklerAppEffMax " +
							SetStart_int + " to " + SetEnd_int + " -> "+ SprinklerAppEffMax_String );
						}
						ipy.setSeff ( year, Double.parseDouble(SprinklerAppEffMax_String));
					}
					/* FIXME SAM 2007-10-18 remove when code tests out.
					if ( fill_AcresGW ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2,
							routine, "Setting " + ipy_id +
							" AcresGW " + SetStart_int +
							" to " + SetEnd_int + " -> "+
							AcresGW_String );
						}
						//if ( ProcessWhen_int == Now_int ) {
							ipy.setGacre ( year,
							StringUtil.atod(
							AcresGW_String));
						//}
					}
					if ( fill_AcresSprinkler ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2,
							routine, "Setting " + ipy_id +
							" AcresSprinkler " +
							SetStart_int + " to " +
							SetEnd_int + " -> "+
							AcresSprinkler_String );
						}
						//if ( ProcessWhen_int == Now_int ) {
							ipy.setSacre ( year,
							StringUtil.atod(
							AcresSprinkler_String));
						//}
					}
					*/
					if ( fill_AcresGWFlood ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresGWFlood " +
								SetStart_int + " to " + SetEnd_int + " -> "+ AcresGWFlood_String );
						}
						ipy.setAcgwfl ( year, Double.parseDouble(AcresGWFlood_String));
					}
					if ( fill_AcresGWSprinkler) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresGWSprinkler " +
								SetStart_int + " to " + SetEnd_int + " -> "+ AcresGWSprinkler_String );
						}
						ipy.setAcgwspr ( year, Double.parseDouble(AcresGWSprinkler_String));
					}
					if ( fill_AcresSWFlood) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresSWFlood " +
								SetStart_int + " to " + SetEnd_int + " -> " + AcresSWFlood_String );
						}
						ipy.setAcswfl ( year, Double.parseDouble(AcresSWFlood_String));
					}
					if ( fill_AcresSWSprinkler) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2, routine, "Setting " + ipy_id + " AcresSWSprinkler " +
								SetStart_int + " to " + SetEnd_int + " -> " + AcresSWSprinkler_String );
						}
						ipy.setAcswspr ( year, Double.parseDouble(AcresSWSprinkler_String));
					}
					if ( fill_PumpingMax ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2,routine, "Setting " + ipy_id + " PumpingMax " +
							SetStart_int + " to " + SetEnd_int + " -> "+ PumpingMax_String );
						}
						ipy.setMprate ( year, Double.parseDouble(PumpingMax_String));
					}
					if ( fill_GWMode ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2,routine, "Setting " + ipy_id +" GWMode "+
							SetStart_int + " to " + 	SetEnd_int + " -> "+ GWMode_String );
						}
						ipy.setGmode ( year, Integer.parseInt(GWMode_String));
					}
					if ( fill_AcresTotal ) {
						if ( year == SetStart_int ) {
							Message.printStatus ( 2,routine, "Setting " + ipy_id + " AcresTotal " +
							SetStart_int + " to " + SetEnd_int + " -> "+ AcresTotal_String );
						}
						ipy.setTacre ( year, Double.parseDouble(AcresTotal_String));
					}
					/*
					if ( ProcessWhen_int == WithParcels_int ) {
						// Instead of setting the data, save supplemental records for
						// use with the readIrrigationPracticeTSFromHydroBase command.
						StateDMI_HydroBase_ParcelUseTS hbputs = new StateDMI_HydroBase_ParcelUseTS();
						hbputs.setCal_year ( year );
						hbputs.setLocationID ( id );
						// Records are primitive so store total acres...
						if ( fill_AcresTotal ) {
							hbputs.setArea ( StringUtil.atod(AcresTotal_String) );
						}
						if ( fill_IrrigationMethod ) {
							hbputs.setIrrig_type ( IrrigationMethod_String );
						}
						if ( fill_SupplyType ) {
							hbputs.setSupply_type ( SupplyType_String );
						}
						Supplemental_ParcelUseTS_Vector.addElement ( hbputs );
					}
					*/
					// Refresh the groundwater total and surface water total only if the parts are not
					// missing.  Do not cascade the adjustments because this set command depends on the user
					// setting information completely, or relying on later commands to fill
					// This is OK because the user is not able to set these values.
					ipy.refreshAcgw ( year );
					ipy.refreshAcsw ( year );
				}
			}
		}
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting irrigation practice time series data from list (" + e + ").";
        Message.printWarning ( warningLevel, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
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
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue("SprinklerAppEffMaxCol");
	//String AcresGWCol = parameters.getValue ( "AcresGWCol" );
	//String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
	//String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	//String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	//String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ListFile != null) && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( (ID != null) && (ID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (SetStart != null) && (SetStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetStart=" + SetStart  );
	}
	if ( (SetEnd != null) && (SetEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEnd=" + SetEnd  );
	}
	if ( (YearCol != null) && (YearCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "YearCol=" + YearCol  );
	}
	if ( (IDCol != null) && (IDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDCol=\"" + IDCol + "\"" );
	}
	if ( (SurfaceDelEffMaxCol != null) && (SurfaceDelEffMaxCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SurfaceDelEffMaxCol=\"" + SurfaceDelEffMaxCol+"\"");
	}
	if ( (FloodAppEffMaxCol != null) && (FloodAppEffMaxCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FloodAppEffMaxCol=\"" + FloodAppEffMaxCol + "\"" );
	}
	if ( (SprinklerAppEffMaxCol != null) && (SprinklerAppEffMaxCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append("SprinklerAppEffMaxCol=\""+SprinklerAppEffMaxCol+"\"");
	}
	if ( (AcresSWFloodCol != null) && (AcresSWFloodCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSWFloodCol=\"" + AcresSWFloodCol + "\"" );
	}
	if ( (AcresSWSprinklerCol != null) && (AcresSWSprinklerCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSWSprinklerCol=\"" + AcresSWSprinklerCol + "\"" );
	}
	if ( (AcresGWFloodCol != null) && (AcresGWFloodCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGWFloodCol=\"" + AcresGWFloodCol + "\"" );
	}
	if ( (AcresGWSprinklerCol != null) && (AcresGWSprinklerCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGWSprinklerCol=\"" + AcresGWSprinklerCol + "\"" );
	}
	/*
	if ( (AcresGWCol != null) && (AcresGWCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresGWCol=\"" + AcresGWCol + "\"" );
	}
	if ( (AcresSprinklerCol != null) && (AcresSprinklerCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresSprinklerCol=\"" + AcresSprinklerCol + "\"" );
	}
	*/
	if ( (PumpingMaxCol != null) && (PumpingMaxCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PumpingMaxCol=\"" + PumpingMaxCol + "\"" );
	}
	if ( (GWModeCol != null) && (GWModeCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GWModeCol=\"" + GWModeCol + "\"" );
	}
	if ( (AcresTotalCol != null) && (AcresTotalCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresTotalCol=\"" + AcresTotalCol + "\"" );
	}
	/*
	if ( (ProcessWhen != null) && (ProcessWhen.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessWhen=" + ProcessWhen );
	}
	if ( (IrrigationMethodCol != null) && (IrrigationMethodCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigationMethodCol=\"" + IrrigationMethodCol + "\"" );
	}
	if ( (SupplyTypeCol != null) && (SupplyTypeCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SupplyTypeCol=\"" + SupplyTypeCol + "\"" );
	}
	*/
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
