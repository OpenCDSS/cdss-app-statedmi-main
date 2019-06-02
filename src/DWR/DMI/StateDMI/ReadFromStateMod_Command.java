// ReadFromStateMod_Command - This class initializes, checks, and runs the Read*FromStateMod() commands.

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

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateCU.StateCU_Location;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DelayTable;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlow;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_NodeNetwork;
import DWR.StateMod.StateMod_OperationalRight;
import DWR.StateMod.StateMod_Plan;
import DWR.StateMod.StateMod_Plan_WellAugmentation;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_ReturnFlow;
import DWR.StateMod.StateMod_RiverNetworkNode;
import DWR.StateMod.StateMod_StreamEstimate;
import DWR.StateMod.StateMod_StreamEstimate_Coefficients;
import DWR.StateMod.StateMod_StreamGage;
import DWR.StateMod.StateMod_TS;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;
import RTi.TS.MonthTS;
import RTi.TS.TS;
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
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class initializes, checks, and runs the Read*FromStateMod() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the ReadDiversionStationsFromStateMod()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
*/
public abstract class ReadFromStateMod_Command extends AbstractCommand implements Command
{
	
/**
Flags for Ignore* and Append parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Input file that is read by this command.
*/
private File __InputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public ReadFromStateMod_Command ()
{	super();
	setCommandName ( "Read?FromStateMod" );
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
{	String routine = "ReadFromStateMod_Command.checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String Scale = parameters.getValue ( "Scale" );
	String Append = parameters.getValue ( "Append" );
	String ReadData = parameters.getValue ( "ReadData" );
	String IgnoreDWs = parameters.getValue ( "IgnoreDWs" );
	String IgnoreWells = parameters.getValue ( "IgnoreWells" );
	String IgnoreDiversions = parameters.getValue ( "IgnoreDiversions" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (InputFile == null) || (InputFile.length() == 0) ) {
		message = "The input file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an input file." ) );
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
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Software error - report to support." ) );
			}
	
		try {
			String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile));
			File f = new File ( adjustedPath );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The input file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the input directory." ) );
			}
			else if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The input file:\n" +
			"    \"" + InputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that input file and working directory paths are compatible." ) );
		}
	}
	
	if ( (this instanceof ReadDelayTablesMonthlyFromStateMod_Command) ||
		(this instanceof ReadDelayTablesDailyFromStateMod_Command)) {
		if ( (Scale != null) && (Scale.length() > 0) && !StringUtil.isDouble(Scale) ) {
			message = "The scale value (" + Scale + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the scale as a number.") );
		}
	}
	
	if ( (this instanceof ReadWellStationsFromStateMod_Command) ||
		(this instanceof ReadWellDemandTSMonthlyFromStateMod_Command) ) {
		if ( (IgnoreDWs != null) && (IgnoreDWs.length() > 0) &&
			!IgnoreDWs.equalsIgnoreCase(_False) && !IgnoreDWs.equalsIgnoreCase(_True) ) {
			message = "The IgnoreDWs value (" + IgnoreDWs + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IgnoreDWs as " + _False + " (default), or " + _True + ".") );
		}
		
		if ( (IgnoreWells != null) && (IgnoreWells.length() > 0) &&
			!IgnoreWells.equalsIgnoreCase(_False) && !IgnoreWells.equalsIgnoreCase(_True) ) {
			message = "The IgnoreWells value (" + IgnoreWells + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IgnoreWells as " + _False + " (default), or " + _True + ".") );
		}
	}
	
	if ( (this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command) ) {
		if ( (IgnoreDiversions != null) && (IgnoreDiversions.length() > 0) &&
			!IgnoreDiversions.equalsIgnoreCase(_False) && !IgnoreDiversions.equalsIgnoreCase(_True) ) {
			message = "The IgnoreDiversions value (" + IgnoreDiversions + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify IgnoreDiversions as " + _False + " (default), or " + _True + ".") );
		}
	}
	
	if ( this instanceof ReadWellRightsFromStateMod_Command ) {
		if ( (Append != null) && (Append.length() > 0) &&
			!Append.equalsIgnoreCase(_False) && !Append.equalsIgnoreCase(_True) ) {
			message = "The Append value (" + Append + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify Append as " + _False + ", or " + _True + " (default).") );
		}
	}
	
	if ( this instanceof ReadResponseFromStateMod_Command ) {
		if ( (ReadData != null) && (ReadData.length() > 0) &&
			!ReadData.equalsIgnoreCase(_False) && !ReadData.equalsIgnoreCase(_True) ) {
			message = "The ReadData value (" + ReadData + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify ReadData as " + _False + ", or " + _True + " (default).") );
		}
	}
    
	// Check for invalid parameters...
	Vector<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "InputFile" );
	if ( (this instanceof ReadDelayTablesMonthlyFromStateMod_Command) ||
		(this instanceof ReadDelayTablesDailyFromStateMod_Command) ){
		valid_Vector.add ( "Scale" );
	}
	if ( (this instanceof ReadWellStationsFromStateMod_Command) ||
		(this instanceof ReadWellDemandTSMonthlyFromStateMod_Command) ) {
		valid_Vector.add ( "IgnoreDWs" );
		valid_Vector.add ( "IgnoreWells" );
	}
	if ( (this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command) ) {
		valid_Vector.add ( "IgnoreDiversions" );
	}
	if ( this instanceof ReadWellRightsFromStateMod_Command ){
		valid_Vector.add ( "Append" );
	}
	if ( this instanceof ReadResponseFromStateMod_Command ){
		valid_Vector.add ( "ReadData" );
	}
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
	return (new ReadFromStateMod_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getInputFile ()
{
	return __InputFile_File;
}

/**
Read StateMod control file.
*/
private int readControlFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, StateMod_DataSet stateModDataSet )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readControlFromStateMod";

	// Reset the control information to initial values
	stateModDataSet.initializeControlData();
	// This reads control information in the existing data set
	stateModDataSet.readStateModControlFile(InputFile_full);
	
	return warning_count;
}

/**
Process the ReadCULocationsFromStateMod() command.
*/
private int readCULocationsFromStateMod ( StateDMI_Processor processor,
	int warning_level, int warning_count, String command_tag,
	CommandStatus status, CommandPhaseType command_phase,
	String InputFile_full )
throws Exception
{	String routine = "ReadFromStateMod_Command.readCULocationsFromStateMod";
	String message = null;
    Message.printStatus ( 2, routine,
        	"Reading StateCU locations from StateMod file \"" + InputFile_full + "\"" );
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source. This is used to print a warning.
	processor.resetDataMatches ( processor.getStateCULocationMatchList() );

	List<StateMod_Diversion> smdivList = null;
	List<StateMod_Well> smwellList = null;
	boolean isdiv = false;
	int size = 0;
	if ( StateMod_Diversion.isStateModDiversionFile(InputFile_full) ) {
		smdivList = StateMod_Diversion.readStateModFile( InputFile_full );
		size = smdivList.size();
		isdiv = true;
	}
	else if ( StateMod_Well.isStateModWellFile(InputFile_full) ) {
		smwellList = StateMod_Well.readStateModFile( InputFile_full );
		size = smwellList.size();
		isdiv = false;
	}
	else {
		message = "Cannot determine whether \"" + InputFile_full +
			"\" is a diversion or well station file.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
        throw new CommandException ( message );
	}

	// Loop through the diversions and add new CULocation instances for each
	// diversion, checking the demsrc flag...

	StateCU_Location culoc;
	int demsrc;
	if ( isdiv ) {
		// Process diversions...
		StateMod_Diversion diversion;
		for (int i = 0; i < size; i++) {
			diversion = smdivList.get(i);
			// Only add if the demand source flag is 1, 2, 3, 4, or 8...
			demsrc = diversion.getDemsrc();
			if ( (demsrc != StateMod_Diversion.DEMSRC_GIS) &&
				(demsrc != StateMod_Diversion.DEMSRC_TIA) &&
				(demsrc!=StateMod_Diversion.DEMSRC_GIS_PRIMARY) &&
				(demsrc!=StateMod_Diversion.DEMSRC_TIA_PRIMARY) &&
				(demsrc != StateMod_Diversion.DEMSRC_USER) ) {
				Message.printStatus ( 2, routine,
				"Demand source (" + demsrc + ") for diversion \"" + diversion.getID() +
				"\" is not for crops.  Ignoring as a CU location." );
				continue;
			}
			culoc = new StateCU_Location();
			culoc.setID ( diversion.getID() );
			culoc.setName ( diversion.getName() );
			// Replace or add in the __CULocation_Vector...
			processor.findAndAddCULocation ( culoc, true );
		}
	}
	else {
		// wells...
		StateMod_Well well;
		for (int i = 0; i < size; i++) {
			well = smwellList.get(i);
			// Only add if the demand source flag is 1, 2, 3, 4, or 8...
			demsrc = well.getDemsrcw();
			if ( (demsrc != StateMod_Diversion.DEMSRC_GIS) &&
				(demsrc != StateMod_Diversion.DEMSRC_TIA) &&
				(demsrc!=StateMod_Diversion.DEMSRC_GIS_PRIMARY) &&
				(demsrc!=StateMod_Diversion.DEMSRC_TIA_PRIMARY) &&
				(demsrc != StateMod_Diversion.DEMSRC_USER) ) {
				Message.printStatus ( 2, routine,
				"Demand source (" + demsrc + ") for well \"" +
				well.getID() + "\" is not for crops.  Ignoring as a CULocation." );
				continue;
			}
			culoc = new StateCU_Location();
			culoc.setID ( well.getID() );
			culoc.setName ( well.getName() );
			// Replace or add in the __CULocation_Vector...
			processor.findAndAddCULocation ( culoc, true );
		}
	}

	// Warn about identifiers that have been replaced in the list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateCUClimateStationMatchList(), "CU Locations" );
	
	return warning_count;
}

/**
Read StateMod delay tables.
*/
private int readDelayTablesFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, String Scale, double Scale_double, int Interval_int )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readDelayTablesFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	if ( Interval_int == TimeInterval.DAY ) {
		processor.resetDataMatches ( processor.getStateModDelayTableMatchList(TimeInterval.DAY) );
	}
	else {
		processor.resetDataMatches ( processor.getStateModDelayTableMatchList(TimeInterval.MONTH) );
	}

	// Read the stream delay tables.  Always read as percent (-1 below) and force the user to
	// scale since the file itself gives no indication of whether it contains percent or fraction.

	List<StateMod_DelayTable> delayTableList = null;
	if ( Interval_int == TimeInterval.DAY ) {
		// False indicates daily.
		delayTableList = StateMod_DelayTable.readStateModFile ( InputFile_full, false, -1 );
	}
	else {
		// True indicates monthly
		delayTableList = StateMod_DelayTable.readStateModFile ( InputFile_full, true, -1 );
	}
	int delayTablesListSize = 0;
	if ( delayTableList != null ) {
		delayTablesListSize = delayTableList.size();
	}

	// Loop through the data and add new StateMod_DelayTable instances for each entry...

	StateMod_DelayTable dly;
	for (int i = 0; i < delayTablesListSize; i++) {
		dly = delayTableList.get(i);
		if ( Scale != null ) {
			// Scale the delay table values...
			dly.scale ( Scale_double );
		}
		// Replace or add in the __SMDelayTable_Vector...
		processor.findAndAddSMDelayTable ( dly, Interval_int, true );
	}

	// Warn about identifiers that have been replaced in the station list

	if ( Interval_int == TimeInterval.DAY ) {
		processor.warnAboutDataMatches ( this, true,
			processor.getStateModDelayTableMatchList(Interval_int), "Delay Tables (Daily)" );
	}
	else {
		processor.warnAboutDataMatches ( this, true,
			processor.getStateModDelayTableMatchList(Interval_int), "Delay Tables (Monthly)" );
	}
	return warning_count;
}

/**
Read StateMod diversion demand time series (monthly).
*/
private int readDiversionDemandTSMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime )
throws Exception
{	String routine = "ReadFromStateMod_Command.readDiversionDemandTSMonthlyromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionDemandTSMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> ddmList = StateMod_TS.readTimeSeriesList (
		InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, null, true );

	int size = ddmList.size();
	Message.printStatus ( 2, routine, "Read " + size + " diversion demand monthly time series." );

	// Loop through the DDH file data and add new time series instances for each DDH entry...

	MonthTS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)ddmList.get(i);
		// Replace or add time series list...
		processor.findAndAddSMDemandTSMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModDiversionDemandTSMonthlyMatchList(),
		"Diversion Demand Time Series (Monthly)" );
	return warning_count;
}

/**
Read StateMod diversion historical time series (monthly).
*/
private int readDiversionHistoricalTSMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime )
throws Exception
{	String routine = "ReadFromStateMod_Command.readDiversionHistoricalTSMonthlyromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionHistoricalTSMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> ddhList = StateMod_TS.readTimeSeriesList (
		InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, null, true );

	int size = ddhList.size();
	Message.printStatus ( 2, routine, "Read " + size +
		" diversion historical monthly time series." );

	// Loop through the DDH file data and add new time series instances for each DDH entry...

	MonthTS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)ddhList.get(i);
		// Replace or add in the diversion time series list...
		processor.findAndAddSMDiversionTSMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModDiversionHistoricalTSMonthlyMatchList(),
		"Diversion Historical Time Series (Monthly)" );
	return warning_count;
}

/**
Read StateMod diversion rights.
*/
private int readDiversionRightsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readDiversionRightFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionRightMatchList() );

	// Read the diversion rights...

	List<StateMod_DiversionRight> diversionRightList = StateMod_DiversionRight.readStateModFile ( InputFile_full );
	int diversionRightListSize = 0;
	if ( diversionRightList != null ) {
		diversionRightListSize = diversionRightList.size();
	}

	// Loop through the rights and add new instances in the processor...

	StateMod_DiversionRight ddr;
	// Process rights...
	for (int i = 0; i < diversionRightListSize; i++) {
		ddr = (StateMod_DiversionRight)diversionRightList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMDiversionRight ( ddr, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModDiversionRightMatchList(), "Diversion Rights" );
	return warning_count;
}

/**
Read StateMod diversion stations.
*/
private int readDiversionStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readDiversionStationsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModDiversionStationMatchList() );

	// Read the reservoir stations...

	List<StateMod_Diversion> diversionStationList = StateMod_Diversion.readStateModFile ( InputFile_full );
	int diversionStationListSize = 0;
	if ( diversionStationList != null ) {
		diversionStationListSize = diversionStationList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_Diversion dds;
	// Process stations...
	for (int i = 0; i < diversionStationListSize; i++) {
		dds = diversionStationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMDiversion ( dds, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModDiversionStationMatchList(), "Diversion Stations" );
	return warning_count;
}

/**
Read StateMod instream flow demand time series (average monthly).
*/
private int readInstreamFlowDemandTSAverageMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase, String InputFile_full )
throws Exception
{	String routine = "ReadFromStateMod_Command.readInstreamFlowDemandTSAverageMonthlyFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModInstreamFlowDemandTSAverageMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> ifaList = StateMod_TS.readTimeSeriesList (
		InputFile_full, null, null, null, true );

	int size = ifaList.size();
	Message.printStatus ( 2, routine, "Read " + size + " instream flow demand average monthly time series." );

	// Loop through the file data and add new time series instances for each entry...

	MonthTS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)ifaList.get(i);
		// Replace or add time series list...
		processor.findAndAddSMInstreamFlowDemandTSAverageMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModInstreamFlowDemandTSAverageMonthlyMatchList(),
		"Instream Flow Demand Time Series (Average Monthly)" );
	return warning_count;
}

/**
Read StateMod instream flow rights.
*/
private int readInstreamFlowRightsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readInstreamFlowRightFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModInstreamFlowRightMatchList() );

	// Read the instream flow rights...

	List<StateMod_InstreamFlowRight> instreamFlowRightList = StateMod_InstreamFlowRight.readStateModFile ( InputFile_full );
	int instreamFlowRightListSize = 0;
	if ( instreamFlowRightList != null ) {
		instreamFlowRightListSize = instreamFlowRightList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_InstreamFlowRight isf;
	// Process stations...
	for (int i = 0; i < instreamFlowRightListSize; i++) {
		isf = (StateMod_InstreamFlowRight)instreamFlowRightList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMInstreamFlowRight ( isf, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModInstreamFlowRightMatchList(), "Intream Flow Rights" );
	return warning_count;
}

/**
Read StateMod instream flow stations.
*/
private int readInstreamFlowStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readInstreamFlowStationsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModInstreamFlowStationMatchList() );

	// Read the instream flow stations...

	List<StateMod_InstreamFlow> instreamFlowStationList = StateMod_InstreamFlow.readStateModFile ( InputFile_full );
	int instreamFlowStationListSize = 0;
	if ( instreamFlowStationList != null ) {
		instreamFlowStationListSize = instreamFlowStationList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_InstreamFlow isf;
	// Process stations...
	for (int i = 0; i < instreamFlowStationListSize; i++) {
		isf = (StateMod_InstreamFlow)instreamFlowStationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMInstreamFlow ( isf, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModInstreamFlowStationMatchList(), "Intream Flow Stations" );
	return warning_count;
}

/**
Read StateMod stream network.
*/
private int readNetworkFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	String routine = "ReadFromStateMod_Command.readNetworkFromStateMod";
	// Get the HydroBase DMI, needed by the network...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		String message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Verify that HydroBase is accessible." ) );
	}
	
	// Read the network..

	StateMod_NodeNetwork net = StateMod_NodeNetwork.readStateModNetworkFile(
			InputFile_full, new StateDMI_NodeDataProvider(hbdmi), true );
	
	// Set the network in the processor.
	
	processor.setPropContents ( "StateMod_Network", net );
	
	return warning_count;
}

/**
Read StateMod operational rights.
*/
private int readOperationalRightsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readOperationalRightFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModOperationalRightMatchList() );

	// Read the operational rights...

	List<StateMod_OperationalRight> operationalRightList = StateMod_OperationalRight.readStateModFile ( InputFile_full,
		(StateMod_DataSet)null );
	int operaionalRightListSize = 0;
	if ( operationalRightList != null ) {
		operaionalRightListSize = operationalRightList.size();
	}

	// Loop through the rights and add new instances in the processor...

	StateMod_OperationalRight ddr;
	// Process rights...
	for (int i = 0; i < operaionalRightListSize; i++) {
		ddr = (StateMod_OperationalRight)operationalRightList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMOperationalRight ( ddr, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModOperationalRightMatchList(), "Operational Rights" );
	return warning_count;
}

/**
Read StateMod plan return data.
*/
private int readPlanReturnFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readPlanReturnFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModPlanReturnMatchList() );

	// Read the plan return data...

	List<StateMod_ReturnFlow> planReturnList =
		StateMod_ReturnFlow.readStateModFile ( InputFile_full, StateMod_DataSet.COMP_PLAN_RETURN );
	int planReturnListSize = 0;
	if ( planReturnList != null ) {
		planReturnListSize = planReturnList.size();
	}

	// Loop through the data and add new instances in the processor...

	StateMod_ReturnFlow rf;
	// Process stations...
	for (int i = 0; i < planReturnListSize; i++) {
		rf = planReturnList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMPlanReturn ( rf, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true, processor.getStateModPlanReturnMatchList(), "Plan Return" );
	return warning_count;
}

/**
Read StateMod plan stations.
*/
private int readPlanStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readPlanStationsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModPlanStationMatchList() );

	// Read the plan stations...

	List<StateMod_Plan> planStationList = StateMod_Plan.readStateModFile ( InputFile_full );
	int planStationListSize = 0;
	if ( planStationList != null ) {
		planStationListSize = planStationList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_Plan plan;
	// Process stations...
	for (int i = 0; i < planStationListSize; i++) {
		plan = (StateMod_Plan)planStationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMPlan ( plan, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true, processor.getStateModPlanStationMatchList(), "Plan Stations" );
	return warning_count;
}

/**
Read StateMod well augmentation plan data.
*/
private int readPlanWellAugmentationFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readPlanWellAugmentationFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModPlanWellAugmentationMatchList() );

	// Read the plan well augmentation data...

	List<StateMod_Plan_WellAugmentation> planWellAugmentationList =
		StateMod_Plan_WellAugmentation.readStateModFile ( InputFile_full );
	int planWellAugmentationListSize = 0;
	if ( planWellAugmentationList != null ) {
		planWellAugmentationListSize = planWellAugmentationList.size();
	}

	// Loop through the data and add new instances in the processor...

	StateMod_Plan_WellAugmentation plan;
	// Process stations...
	for (int i = 0; i < planWellAugmentationListSize; i++) {
		plan = planWellAugmentationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMPlanWellAugmentation ( plan, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true, processor.getStateModPlanStationMatchList(), "Plan Well Augmentation" );
	return warning_count;
}

/**
Read StateMod reservoir return data.
*/
private int readReservoirReturnFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readReservoirReturnFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModReservoirReturnMatchList() );

	// Read the reservoir return data...

	List<StateMod_ReturnFlow> resReturnList =
		StateMod_ReturnFlow.readStateModFile ( InputFile_full, StateMod_DataSet.COMP_RESERVOIR_RETURN );
	int resReturnListSize = 0;
	if ( resReturnList != null ) {
		resReturnListSize = resReturnList.size();
	}

	// Loop through the data and add new instances in the processor...

	StateMod_ReturnFlow rf;
	// Process stations...
	for (int i = 0; i < resReturnListSize; i++) {
		rf = resReturnList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMReservoirReturn ( rf, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true, processor.getStateModReservoirReturnMatchList(), "Reservoir Return" );
	return warning_count;
}

/**
Read StateMod reservoir rights.
*/
private int readReservoirRightsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readReservoirRightFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModReservoirRightMatchList() );

	// Read the reservoir rights...

	List<StateMod_ReservoirRight> reservoirRightList = StateMod_ReservoirRight.readStateModFile ( InputFile_full );
	int reservoirRightListSize = 0;
	if ( reservoirRightList != null ) {
		reservoirRightListSize = reservoirRightList.size();
	}

	// Loop through the rights and add new instances in the processor...

	StateMod_ReservoirRight rer;
	// Process rights...
	for (int i = 0; i < reservoirRightListSize; i++) {
		rer = (StateMod_ReservoirRight)reservoirRightList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMReservoirRight ( rer, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModReservoirRightMatchList(), "Reservoir Rights" );
	return warning_count;
}

/**
Read StateMod reservoir stations.
*/
private int readReservoirStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readReservoirStationsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModReservoirStationMatchList() );

	// Read the reservoir stations...

	List<StateMod_Reservoir> reservoirStationList = StateMod_Reservoir.readStateModFile ( InputFile_full );
	int reservoirStationListSize = 0;
	if ( reservoirStationList != null ) {
		reservoirStationListSize = reservoirStationList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_Reservoir res;
	// Process stations...
	for (int i = 0; i < reservoirStationListSize; i++) {
		res = (StateMod_Reservoir)reservoirStationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMReservoir ( res, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModReservoirStationMatchList(), "Reservoir Stations" );
	return warning_count;
}

/**
Read StateMod response file.
*/
private int readResponseFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, StateMod_DataSet stateModDataSet, boolean readData )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readResponseFromStateMod";

	stateModDataSet.readStateModFile( InputFile_full, readData,
		true, // do read time series
		false, // do not issue GUI warnings via interactive dialogs
		null ); // no need for parent frame
	
	// The data set is already in the processor so don't reset
	// TODO SAM 2011-01-17 Evaluate whether should still set to trigger other actions
	
	//processor.setPropContents ( "StateMod_DataSet", stateModDataSet );
	
	return warning_count;
}

/**
Read StateMod reservoir stations.
*/
private int readRiverNetworkFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readRiverNetworkFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModRiverNetworkNodeMatchList() );

	// Read the river network nodes...

	List<StateMod_RiverNetworkNode> riverNetworkNodeList = StateMod_RiverNetworkNode.readStateModFile ( InputFile_full );
	int riverNetworkNodeListSize = 0;
	if ( riverNetworkNodeList != null ) {
		riverNetworkNodeListSize = riverNetworkNodeList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_RiverNetworkNode node;
	// Process stations...
	for (int i = 0; i < riverNetworkNodeListSize; i++) {
		node = riverNetworkNodeList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMRiverNetworkNode ( node, true );
	}

	// Warn about identifiers that have been replaced in the list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModRiverNetworkNodeMatchList(), "Reservoir Network Nodes" );
	return warning_count;
}

/**
Read StateMod stream estimate coefficients.
*/
private int readStreamEstimateCoefficientsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readStreamEstimateCoefficientsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamEstimateCoefficientsMatchList() );

	// Read the stream estimate coefficients...

	List<StateMod_StreamEstimate_Coefficients> streamEstimateCoefficientList = StateMod_StreamEstimate_Coefficients.readStateModFile ( InputFile_full );
	int streamEstimateCoefficientsListSize = 0;
	if ( streamEstimateCoefficientList != null ) {
		streamEstimateCoefficientsListSize = streamEstimateCoefficientList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_StreamEstimate_Coefficients coeff;
	// Process stations...
	for (int i = 0; i < streamEstimateCoefficientsListSize; i++) {
		coeff = streamEstimateCoefficientList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMStreamEstimateCoefficients ( coeff, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModStreamEstimateCoefficientsMatchList(), "Stream Estimate Coefficients" );
	return warning_count;
}

/**
Read StateMod stream estimate stations.
*/
private int readStreamEstimateStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readStreamEstimateStationsFromStateMod";
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamEstimateStationMatchList() );

	// Read the stream estimate stations...

	List<StateMod_StreamEstimate> streamEstimateStationList = StateMod_StreamEstimate.readStateModFile ( InputFile_full );
	int streamEstimateStationListSize = 0;
	if ( streamEstimateStationList != null ) {
		streamEstimateStationListSize = streamEstimateStationList.size();
	}

	// Loop through the stations and add new instances in the processor...

	StateMod_StreamEstimate estimate;
	// Process stations...
	for (int i = 0; i < streamEstimateStationListSize; i++) {
		estimate = streamEstimateStationList.get(i);
		// Replace or add in the list...
		processor.findAndAddSMStreamEstimate ( estimate, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModStreamEstimateStationMatchList(), "Stream Estimate Stations" );
	return warning_count;
}

/**
Read StateMod stream gage stations.
*/
private int readStreamGageStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full )
throws Exception
{	//String routine = "ReadFromStateMod_Command.readStreamGageStationsFromStateMod";
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModStreamGageStationMatchList() );

	// Read the stream gage stations...

	List<StateMod_StreamGage> streamGageStationList = StateMod_StreamGage.readStateModFile ( InputFile_full );
	int streamGageStationListSize = 0;
	if ( streamGageStationList != null ) {
		streamGageStationListSize = streamGageStationList.size();
	}

	// Loop through the gages and add new instances in the processor...

	StateMod_StreamGage gage;
	// Process gages...
	for (int i = 0; i < streamGageStationListSize; i++) {
		gage = streamGageStationList.get(i);
		// Replace or add in the __CUStreamGage_Vector...
		processor.findAndAddSMStreamGage ( gage, true );
	}

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModStreamGageStationMatchList(), "Stream Gage Stations" );
	return warning_count;
}

/**
Read StateMod well demand time series (monthly).
*/
private int readWellDemandTSMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime,
		List<StateMod_Well> wellList, boolean IgnoreWells_boolean, boolean IgnoreDWs_boolean )
throws Exception
{	String routine = "ReadFromStateMod_Command.readWellDemandTSMonthlyromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModWellDemandTSMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> wemList = StateMod_TS.readTimeSeriesList (
		InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, null, true );

	int size = wemList.size();
	Message.printStatus ( 2, routine, "Read " + size + " well demand monthly time series." );

	// Loop through the data add new time series instances for each WEM entry...

	StateMod_Well well = null;
	MonthTS ts;
	String id = null;
	int pos = 0;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)wemList.get(i);
		id = ts.getLocation();
		// Find the associated well...
		pos = StateMod_Util.indexOf( wellList,id );
		well = null;
		if ( pos >= 0 ) {
			well = (StateMod_Well)wellList.get(pos);
		}
		// Skip stations that are to be ignored...
		if ( well != null ) {
			if ( IgnoreWells_boolean && !well.hasAssociatedDiversion() ) {
				Message.printStatus ( 3, routine,
				"Ignoring demand TS for \"" + id + "\" - well-only stations are being ignored." );
				continue;
			}
			if ( IgnoreDWs_boolean && well.hasAssociatedDiversion() ) {
				Message.printStatus ( 3, routine,
				"Ignoring demand TS for \"" + id + "\" - D&W stations are being ignored." );
				continue;
			}
		}
		// Replace or add in the __SMWellDemandTSMonthly_Vector...
		processor.findAndAddSMWellDemandTSMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModWellDemandTSMonthlyMatchList(),
		"Well Demand Time Series (Monthly)" );
	return warning_count;
}

/**
Read StateMod well historical pumping time series (monthly).
*/
private int readWellHistoricalPumpingTSMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime,
		List<StateMod_Well> wellList, boolean IgnoreDiversions_boolean )
throws Exception
{	String routine = "ReadFromStateMod_Command.readWellHistoricalPumpingTSMonthlyromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModWellHistoricalPumpingTSMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> wehList = StateMod_TS.readTimeSeriesList (
		InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, null, true );

	int size = wehList.size();
	Message.printStatus ( 2, routine, "Read " + size + " well historical pumping monthly time series." );

	// Loop through the data add new time series instances for each WEH entry...

	MonthTS ts;
	String id = null;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)wehList.get(i);
		id = ts.getLocation();
		if ( IgnoreDiversions_boolean  ) {
			// If the ID is not in the well stations file, ignore the time series...
			if ( StateMod_Util.indexOf( wellList,id) < 0 ) {
				Message.printStatus ( 2, routine,
				"Ignoring historical pumping TS for \"" + id + "\" - it is not for a well station." );
				continue;
			}
		}
		// Replace or add in the __SMWellDemandTSMonthly_Vector...
		processor.findAndAddSMWellHistoricalPumpingTSMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModWellHistoricalPumpingTSMonthlyMatchList(),
		"Well Historical Pumping Time Series (Monthly)" );
	return warning_count;
}

/**
Read StateMod well rights.
*/
private int readWellRightsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, boolean Append_boolean )
throws Exception
{	String routine = "ReadFromStateMod_Command.readWellRightsFromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	List<String> matchList = processor.getStateModWellRightMatchList();
	processor.resetDataMatches ( matchList );

	// Read the well rights...

	List<StateMod_WellRight> wellRightList = StateMod_WellRight.readStateModFile ( InputFile_full );
	int wellRightListSize = 0;
	if ( wellRightList != null ) {
		wellRightListSize = wellRightList.size();
	}

	// Loop through the rights and add new instances in the processor...

	StateMod_WellRight wellr;
	List<StateMod_WellRight> processorRightList = processor.getStateModWellRightList();
	if ( !Append_boolean ) {
		// Clear the rights first...
		processorRightList.clear();
	}
	for ( int i = 0; i < wellRightListSize; i++ ) {
		wellr = wellRightList.get(i);
		StateDMI_Util.findAndAddSMWellRight ( processorRightList,
			matchList, wellr, StateDMI_Util._IF_MATCH_APPEND );
	}
	Message.printStatus( 2, routine, "Read " + wellRightListSize + " well rights." );

	// Warn about identifiers that have been replaced in the station list

	processor.warnAboutDataMatches ( this, true, matchList, "Well Rights" );
	return warning_count;
}

/**
Read StateMod well stations.
*/
private int readWellStationsFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, boolean IgnoreDWs_boolean, boolean IgnoreWells_boolean )
throws Exception
{	String routine = "ReadFromStateMod_Command.readWellStationsFromStateMod";
	// Remove all the elements for the Vector that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModWellStationMatchList() );

	// Read the well stations...

	List<StateMod_Well> wellList = StateMod_Well.readStateModFile ( InputFile_full );
	int wellListSize = 0;
	if ( wellList != null ) {
		wellListSize = wellList.size();
	}

	/* FIXME SAM 2008-12-24 Not sure what this does since wells probably have not been read before?
	// Get the well stations from the processor, needed for checks below.
	List processorWellList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
	int processorWellListSize = 0;
	if ( processorWellList != null ) {
		processorWellListSize = processorWellList.size();
	}
	if ( IgnoreDWs_boolean && (processorWellListSize == 0) ) {
		message = "Using the IgnoreDWs parameter requires that the well stations have been read and/or created.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        	status.addToLog(command_phase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Use a command to read/create well stations before this command."));
	}
	if ( IgnoreWells_boolean && (wellListSize == 0) ) {
		message = "Using the IgnoreWells parameter requires that the well stations have been read and/or created.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        	status.addToLog(command_phase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Use a command to read/create well stations before this command."));
	}
	*/

	// Loop through the well stations and add to the list that is
	// being maintained.  There is no reason to manipulate the input.

	StateMod_Well well;
	for (int i = 0; i < wellListSize; i++) {
		well = (StateMod_Well)wellList.get(i);
		// Skip stations that are to be ignored...
		if ( IgnoreWells_boolean && !well.hasAssociatedDiversion() ) {
			Message.printStatus ( 3, routine,
			"Ignoring well station \"" + well.getID() + "\" - well-only stations are being ignored." );
			continue;
		}
		if ( IgnoreDWs_boolean && well.hasAssociatedDiversion() ) {
			Message.printStatus ( 3, routine,
			"Ignoring well station \"" + well.getID() + "\" - D&W stations are being ignored." );
			continue;
		}
		processor.findAndAddSMWell ( well, true );
	}

	// Warn about identifiers that have been replaced in the well list

	processor.warnAboutDataMatches ( this, true,
			processor.getStateModWellStationMatchList(), "Well Stations" );
	return warning_count;
}

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
    
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" );
    boolean IgnoreDWs_boolean = false; // For wells stations, Default
    String IgnoreDWs = parameters.getValue ( "IgnoreDWs" );
    if ( (IgnoreDWs != null) && IgnoreDWs.equalsIgnoreCase(_True) ) {
    	IgnoreDWs_boolean = true;
    }
    boolean IgnoreWells_boolean = false; // For wells stations, Default
    String IgnoreWells = parameters.getValue ( "IgnoreWells" );
    if ( (IgnoreWells != null) && IgnoreWells.equalsIgnoreCase(_True) ) {
    	IgnoreWells_boolean = true;
    }
    boolean IgnoreDiversions_boolean = false; // For well pumping from StateCU
    String IgnoreDiversions = parameters.getValue ( "IgnoreDiversions" );
    if ( (IgnoreDiversions != null) && IgnoreDiversions.equalsIgnoreCase(_True) ) {
    	IgnoreDiversions_boolean = true;
    }
    boolean Append_boolean = false; // For well rights, Default
    String Append = parameters.getValue ( "Append" );
    if ( (Append != null) && Append.equalsIgnoreCase(_True) ) {
    	Append_boolean = true;
    }
    boolean ReadData_boolean = false; // For response, Default
    String ReadData = parameters.getValue ( "ReadData" );
    if ( (ReadData != null) && ReadData.equalsIgnoreCase(_True) ) {
    	ReadData_boolean = true;
    }
    String Scale = parameters.getValue ( "Scale"); // For delay tables
    double Scale_double = -1;
    if ( Scale != null ) {
    	Scale_double = Double.parseDouble(Scale);
    }
    
    // If reading response or control, get the StateMod data set
    StateMod_DataSet stateModDataSet = null;
    if ( (this instanceof ReadResponseFromStateMod_Command) || (this instanceof ReadControlFromStateMod_Command) ) {
        try {
       		stateModDataSet = (StateMod_DataSet)processor.getPropContents ( "StateMod_DataSet" );
        }
        catch ( Exception e ) {
            Message.printWarning ( log_level, routine, e );
            message = "Error requesting StateMod data set - used to process response and control data  (" + e + ").";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report to software support.  See log file for details." ) );
        }
    }
    
    // Get well stations if reading demands or historical pumping.
    List<StateMod_Well> wellList = null;
    if ( (this instanceof ReadWellDemandTSMonthlyFromStateMod_Command &&
    	(IgnoreWells_boolean || IgnoreDWs_boolean)) ||
    	(this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command) ||
    	(this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateMod_Command)) {
        try {
       		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
       		wellList = dataList;
        }
        catch ( Exception e ) {
            Message.printWarning ( log_level, routine, e );
            message = "Error requesting well stations to process - needed to handle ignore parameters (" + e + ").";
            Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),
            routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report to software support.  See log file for details." ) );
        }
    }
    
    // Get the output start and end for use with time series commands
    DateTime OutputStart_DateTime = null;
    DateTime OutputEnd_DateTime = null;
    if ( this instanceof ReadDiversionHistoricalTSMonthlyFromStateMod_Command ) {
	    try {
	    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart" );
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting output start from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
	    try {
	    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd" );
	    }
	    catch ( Exception e ) {
	        Message.printWarning ( log_level, routine, e );
	        message = "Error requesting output end from processor (" + e + ").";
	        Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	        routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Report to software support.  See log file for details." ) );
	    }
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    String InputFile_full = InputFile;
    try {
     	// Clear the filename
    	setInputFile ( new File(InputFile_full) );
    	InputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),InputFile) );
    	
       	if ( !IOUtil.fileExists(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(commandPhase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(commandPhase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( this instanceof ReadCULocationsFromStateMod_Command ) {
    		warning_count = readCULocationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
		}
      	else if ( this instanceof ReadDelayTablesMonthlyFromStateMod_Command ) {
    		warning_count = readDelayTablesFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, Scale, Scale_double, TimeInterval.MONTH );
    	}
      	else if ( this instanceof ReadDelayTablesDailyFromStateMod_Command ) {
    		warning_count = readDelayTablesFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, Scale, Scale_double, TimeInterval.DAY );
    	}
      	else if ( this instanceof ReadStreamGageStationsFromStateMod_Command ) {
    		warning_count = readStreamGageStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadDiversionStationsFromStateMod_Command ) {
    		warning_count = readDiversionStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
	  	else if ( this instanceof ReadDiversionRightsFromStateMod_Command ) {
			warning_count = readDiversionRightsFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full );
		}
	  	else if ( this instanceof ReadDiversionDemandTSMonthlyFromStateMod_Command ) {
			warning_count = readDiversionDemandTSMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full, OutputStart_DateTime, OutputEnd_DateTime );
		}
	  	else if ( this instanceof ReadDiversionHistoricalTSMonthlyFromStateMod_Command ) {
			warning_count = readDiversionHistoricalTSMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full, OutputStart_DateTime, OutputEnd_DateTime );
		}
      	else if ( this instanceof ReadReservoirStationsFromStateMod_Command ) {
    		warning_count = readReservoirStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadReservoirRightsFromStateMod_Command ) {
    		warning_count = readReservoirRightsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadReservoirReturnFromStateMod_Command ) {
    		warning_count = readReservoirReturnFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadInstreamFlowStationsFromStateMod_Command ) {
    		warning_count = readInstreamFlowStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadInstreamFlowRightsFromStateMod_Command ) {
    		warning_count = readInstreamFlowRightsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
	  	else if ( this instanceof ReadInstreamFlowDemandTSAverageMonthlyFromStateMod_Command ) {
			warning_count = readInstreamFlowDemandTSAverageMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase, InputFile_full );
		}
	  	else if ( this instanceof ReadOperationalRightsFromStateMod_Command ) {
			warning_count = readOperationalRightsFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full );
		}
      	else if ( this instanceof ReadPlanReturnFromStateMod_Command ) {
    		warning_count = readPlanReturnFromStateMod ( processor, warning_level, warning_count,
    			command_tag, status, commandPhase, InputFile_full );
    	}
      	else if ( this instanceof ReadPlanStationsFromStateMod_Command ) {
    		warning_count = readPlanStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadPlanWellAugmentationFromStateMod_Command ) {
    		warning_count = readPlanWellAugmentationFromStateMod ( processor, warning_level, warning_count,
    			command_tag, status, commandPhase, InputFile_full );
    	}
      	else if ( this instanceof ReadStreamEstimateStationsFromStateMod_Command ) {
    		warning_count = readStreamEstimateStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadStreamEstimateCoefficientsFromStateMod_Command ) {
    		warning_count = readStreamEstimateCoefficientsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
      	else if ( this instanceof ReadRiverNetworkFromStateMod_Command ) {
    		warning_count = readRiverNetworkFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
    	else if ( this instanceof ReadNetworkFromStateMod_Command ) {
    		warning_count = readNetworkFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full );
    	}
    	else if ( this instanceof ReadWellStationsFromStateMod_Command ) {
    		warning_count = readWellStationsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, IgnoreDWs_boolean, IgnoreWells_boolean );
    	}
      	else if ( this instanceof ReadWellRightsFromStateMod_Command ) {
    		warning_count = readWellRightsFromStateMod ( processor,
    				warning_level, warning_count, command_tag,
    				status, commandPhase,
    				InputFile_full, Append_boolean );
    	}
	  	else if ( this instanceof ReadWellDemandTSMonthlyFromStateMod_Command ) {
			warning_count = readWellDemandTSMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full, OutputStart_DateTime, OutputEnd_DateTime,
					wellList, IgnoreWells_boolean, IgnoreDWs_boolean );
		}
	  	else if ( this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateMod_Command ) {
			warning_count = readWellHistoricalPumpingTSMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full, OutputStart_DateTime, OutputEnd_DateTime,
					wellList, false );
		}
	  	else if ( this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command ) {
			warning_count = readWellHistoricalPumpingTSMonthlyFromStateMod ( processor,
					warning_level, warning_count, command_tag,
					status, commandPhase,
					InputFile_full, OutputStart_DateTime, OutputEnd_DateTime,
					wellList, IgnoreDiversions_boolean );
		}
	  	else if ( this instanceof ReadResponseFromStateMod_Command ) {
			warning_count = readResponseFromStateMod ( processor,
				warning_level, warning_count, command_tag,
				status, commandPhase, InputFile_full, stateModDataSet, ReadData_boolean );
		}
	  	else if ( this instanceof ReadControlFromStateMod_Command ) {
			warning_count = readControlFromStateMod ( processor,
				warning_level, warning_count, command_tag,
				status, commandPhase, InputFile_full, stateModDataSet );
		}
    	else {
    		throw new RuntimeException ( "Don't know how to run " + this );
    	}
    	// Set the filename
    	setInputFile ( new File(InputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error reading file \"" + InputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
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
Set the input file that is read by this command.  This is only used internally.
*/
protected void setInputFile ( File file )
{
	__InputFile_File = file;
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

	String InputFile = parameters.getValue ( "InputFile" );
	String IgnoreDWs = parameters.getValue ( "IgnoreDWs" );
	String IgnoreWells = parameters.getValue ( "IgnoreWells" );
	String IgnoreDiversions = parameters.getValue ( "IgnoreDiversions" );
	String Scale = parameters.getValue ( "Scale" );
	String Append = parameters.getValue ( "Append" );
	String ReadData = parameters.getValue ( "ReadData" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( this instanceof ReadDelayTablesMonthlyFromStateMod_Command ) {
		if ( (Scale != null) && (Scale.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Scale=" + Scale );
		}
	}
	if ( (this instanceof ReadWellStationsFromStateMod_Command) ||
		(this instanceof ReadWellDemandTSMonthlyFromStateMod_Command) ) {
		if ( (IgnoreDWs != null) && (IgnoreDWs.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IgnoreDWs=\"" + IgnoreDWs + "\"" );
		}
		if ( (IgnoreWells != null) && (IgnoreWells.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IgnoreWells=\"" + IgnoreWells + "\"" );
		}
	}
	if ( (this instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command) ) {
		if ( (IgnoreDiversions != null) && (IgnoreDiversions.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IgnoreDiversions=\"" + IgnoreDiversions + "\"" );
		}
	}
	if ( this instanceof ReadWellRightsFromStateMod_Command ) {
		if ( (Append != null) && (Append.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Append=" + Append );
		}
	}
	if ( this instanceof ReadResponseFromStateMod_Command ) {
		if ( (ReadData != null) && (ReadData.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "ReadData=" + ReadData );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
