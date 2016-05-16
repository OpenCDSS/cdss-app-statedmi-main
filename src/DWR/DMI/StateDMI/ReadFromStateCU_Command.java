package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.io.File;
import java.util.List;
import java.util.Vector;

import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateCU.StateCU_BlaneyCriddle;
import DWR.StateCU.StateCU_ClimateStation;
import DWR.StateCU.StateCU_CropCharacteristics;
import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_IrrigationPracticeTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_PenmanMonteith;
import DWR.StateCU.StateCU_Util;
import DWR.StateMod.StateMod_TS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
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
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the Read*FromStateCU() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the ReadClimateStationsFromStateCU()
command extends this class in order to uniquely represent the command, but much of the functionality
is in the base class.
</p>
*/
public abstract class ReadFromStateCU_Command extends AbstractCommand implements Command
{
	
/**
Values for Version parameter.
*/
protected final String _10 = "10";

/**
Values for ReadDataFrom parameter, used with ReadCropPatternTSFromStateCU().
*/
protected final String _CropArea = "CropArea";
protected final String _TotalAreaAndCropFraction = "TotalAreaAndCropFraction";

/**
Input file that is read by this command.
*/
private File __InputFile_File = null;
	
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public ReadFromStateCU_Command ()
{	super();
	setCommandName ( "Read?FromStateCU" );
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
{	String routine = "ReadFromStateCU_Command.checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String Version = parameters.getValue ( "Version" );
	String ReadDataFrom = parameters.getValue ( "ReadDataFrom" );
	
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

	if ( (this instanceof ReadCropPatternTSFromStateCU_Command) ||
		(this instanceof ReadIrrigationPracticeTSFromStateCU_Command) ) {
		if ( (Version != null) && (Version.length() != 0) && !Version.equals(_10) ) {
	        message = "The value for Version (" + Version + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify Version as blank (default) or " + _10 ) );
		}
	}
	if ( this instanceof ReadCropPatternTSFromStateCU_Command ) {
		if ( (ReadDataFrom != null) && (ReadDataFrom.length() != 0) && !ReadDataFrom.equals(_TotalAreaAndCropFraction) ) {
	        message = "The value for ReadDataFrom (" + ReadDataFrom + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Specify ReadDataFrom as " + _CropArea + " (default) or " + _TotalAreaAndCropFraction ) );
		}
	}
    
	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "InputFile" );
	if ( (this instanceof ReadCropPatternTSFromStateCU_Command) ||
		(this instanceof ReadIrrigationPracticeTSFromStateCU_Command) ) {
		valid_Vector.add ( "Version" );
	}
	if ( this instanceof ReadCropPatternTSFromStateCU_Command ) {
		valid_Vector.add ( "ReadDataFrom" );
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
	return (new ReadFromStateCU_JDialog ( parent, this )).ok();
}

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getInputFile ()
{
	return __InputFile_File;
}

/**
Parse the command string into a PropList of parameters.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	String routine = "readStateMod_Command.parseCommand", message;
	int warning_level = 2;
    CommandStatus status = getCommandStatus();

    if ( (command_string.indexOf("=") > 0) || command_string.endsWith("()") ) {
		// New syntax...
		super.parseCommand ( command_string );
	}
	else {
		// Parse the old command...
		List tokens = StringUtil.breakStringList (command_string,
			"(,)", StringUtil.DELIM_ALLOW_STRINGS );
		if ( tokens.size() != 2 ) {
			message = "Invalid syntax for command.  Expecting ReadCULocationsFromStateCU(InputFile).";
			Message.printWarning ( warning_level, routine, message);
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Use the command editor to correct the command." ) );
			throw new InvalidCommandSyntaxException ( message );
		}
		String InputFile = ((String)tokens.get(1)).trim();
		PropList parameters = new PropList ( getCommandName() );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		if ( InputFile.length() > 0 ) {
			parameters.set ( "InputFile", InputFile );
		}
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
}

// The following is expected to be called by the derived classes.

/**
Check the crop pattern time series for integrity, mainly to make sure that
the acreage terms add up and, if specified, that the crop pattern total and
irrigation practice total match in years with non-missing data.
@param cds_tslist List of StateCU_CropPatternTS to check.
@param ipy_tslist List of StateCU_IrrigationPracticeTS to check against.
@param status CommandStatus to append check warnings.
*/
private void readCropPatternTSFromStateCU_checkCropPatternTS ( List cds_tslist, List ipy_tslist, CommandStatus status )
{	
	int cds_size = 0;
	if ( cds_tslist != null ) {
		cds_size = cds_tslist.size();
	}
	StateCU_CropPatternTS cds_ts = null;
	StateCU_IrrigationPracticeTS ipy_ts = null;
	YearTS ipy_total_yts = null;
	double cds_total, ipy_total;
	double tolerance = 1.0;	// Check to nearest integer since acreage may be written as whole number
	int precision = 0;
	// TODO SAM 2007-09-09 Need some utilities to help with checks
	// Need to intelligently compute the precision from the tolerance
	DateTime date_end;		// End of period for looping.
	String cds_id;
	int ipy_pos;
	for ( int i = 0; i < cds_size; i++ ) {
		// Get the crop pattern TS...
		cds_ts = (StateCU_CropPatternTS)cds_tslist.get(i);
		cds_id = cds_ts.getID();
		// Get the irrigation practice TS...
		ipy_pos = StateCU_Util.indexOf(	ipy_tslist,cds_id);
		if ( ipy_pos < 0 ) {
			// Nothing to check against
			ipy_ts = null;
		}
		else {
			ipy_ts = (StateCU_IrrigationPracticeTS)ipy_tslist.get(ipy_pos);
			ipy_total_yts = ipy_ts.getTacreTS();
		}
		// Loop through the period and check the acreage.
		date_end = cds_ts.getDate2();
		for ( DateTime date = new DateTime(cds_ts.getDate1());
			date.lessThanOrEqualTo(date_end); date.addYear(1) ) {
			// Get the totals to check...
			cds_total = cds_ts.getTotalArea ( date.getYear() );
			if ( ipy_ts == null ) {
				ipy_total = -999.00;
			}
			else {
				ipy_total = ipy_total_yts.getDataValue(date);
			}
			if ( ipy_total < 0 ) {
				// IPY is missing so don't do the check vs. CDS.
				continue;
			}
			if ( Math.abs(cds_total - ipy_total) > tolerance ) {
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						"Location \"" + cds_id + "\" CDS total acreage is not within 1 of IPY total in year " +
						date.getYear() + ".  CDS Total = " +
						StringUtil.formatString(cds_total,"%."+precision+"f") +
						" IPY total = " + StringUtil.formatString(ipy_total,"%."+precision+"f"),
						"Verify data processing." ) );
			}
		}
	}
}

/**
Read StateMod diversion demand time series (monthly).
*/
private int readIrrigationWaterRequirementTSMonthlyFromStateMod ( StateDMI_Processor processor,
		int warning_level, int warning_count, String command_tag,
		CommandStatus status, CommandPhaseType command_phase,
		String InputFile_full, DateTime OutputStart_DateTime, DateTime OutputEnd_DateTime )
throws Exception
{	String routine = "ReadFromStateCU_Command.readIrrigationWaterRequirementTSMonthlyromStateMod";
	// Remove all the elements for the list that tracks when identifiers
	// are read from more than one main source...
	// This is used to print a warning.
	processor.resetDataMatches ( processor.getStateModConsumptiveWaterRequirementTSMonthlyMatchList() );

	// If an exception is thrown, let the calling code catch it...
	List<TS> cwrList = StateMod_TS.readTimeSeriesList (
		InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, null, true );

	int size = cwrList.size();
	Message.printStatus ( 2, routine, "Read " + size +
		" irrigation (consumptive) water requirement monthly time series." );

	// Loop through the DDH file data and add new time series instances for each DDH entry...

	MonthTS ts;
	for ( int i = 0; i < size; i++ ) {
		ts = (MonthTS)cwrList.get(i);
		// Replace or add time series list...
		processor.findAndAddSMConsumptiveWaterRequirementTSMonthly ( ts, true );
	}

	// Warn about identifiers that have been replaced in the time series list...

	processor.warnAboutDataMatches ( this, true,
		processor.getStateModConsumptiveWaterRequirementTSMonthlyMatchList(),
		"Irrigation (Consumptive) Water Requirement Time Series (Monthly)" );
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
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" );
    String Version = parameters.getValue ( "Version" ); // Used with crop pattern TS
    String ReadDataFrom = parameters.getValue ( "ReadDataFrom" ); // Used with crop pattern TS
    
    // Get the output start and end for use with time series commands
    DateTime OutputStart_DateTime = null;
    DateTime OutputEnd_DateTime = null;
    if ( (this instanceof ReadCropPatternTSFromStateCU_Command) ||
    	(this instanceof ReadIrrigationWaterRequirementTSMonthlyFromStateCU_Command) ) {
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
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	if ( !IOUtil.fileReadable(InputFile_full) ) {
    		message = "Input file \"" + InputFile_full + "\" is not readable.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(command_phase,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
            throw new CommandException ( message );
    	}
    	
		if ( this instanceof ReadClimateStationsFromStateCU_Command ) {
	        Message.printStatus ( 2, routine, "Reading StateCU climate stations file \"" + InputFile_full + "\"" );
	    	// Remove all the elements for the list that tracks when identifiers
	    	// are read from more than one main source. This is used to print a warning.
	    	processor.resetDataMatches ( processor.getStateCUClimateStationMatchList() );

	    	List cliList = StateCU_ClimateStation.readStateCUFile ( InputFile_full );

			int size = 0;
			if ( cliList != null ) {
				size = cliList.size();
			}

			// Loop through the data and add new StateCU_ClimateStation instances for each entry...

			StateCU_ClimateStation cli;
			for (int i = 0; i < size; i++) {
				cli = (StateCU_ClimateStation)cliList.get(i);
				// Replace or add in the list...
				processor.findAndAddCUClimateStation ( cli, true );
			}

			// Warn about identifiers that have been replaced in the list...

			processor.warnAboutDataMatches ( this, true,
				processor.getStateCUClimateStationMatchList(), "Climate Stations" );
		}
		else if (this instanceof ReadCropCharacteristicsFromStateCU_Command) {
			// Remove all the elements for the list that tracks when identifiers
			// are read from more than one main source (e.g., CCH, and HydroBase).
			// This is used to print a warning.
			processor.resetDataMatches ( processor.getStateCUCropCharacteristicsMatchList());
			List cchList = StateCU_CropCharacteristics.readStateCUFile ( InputFile_full );

			int size = cchList.size();
	
			// Loop through the CCH file data and add new
			// StateCU_CropCharacteristics instances for each CCH entry...
	
			StateCU_CropCharacteristics cch;
			for (int i = 0; i < size; i++) {
				cch = (StateCU_CropCharacteristics)cchList.get(i);
				// Replace or add in the __CUCropCharacteristics_Vector...
				processor.findAndAddCUCropCharacteristics ( cch, true );
			}
	
			// Warn about identifiers that have been replaced in the
			// __CUCropCharacteristics_Vector...
	
			processor.warnAboutDataMatches ( this, true, processor.getStateCUCropCharacteristicsMatchList(),
					"CU Crop Characteristics" );
		}
		else if (this instanceof ReadBlaneyCriddleFromStateCU_Command) {
			// Remove all the elements for the Vector that tracks when identifiers
			// are read from more than one main source (e.g., KBC, and HydroBase).
			// This is used to print a warning.
			processor.resetDataMatches ( processor.getStateCUBlaneyCriddleMatchList() );

			// If an exception is thrown, let the calling code catch it...
			List kbc_Vector = StateCU_BlaneyCriddle.readStateCUFile ( InputFile_full );

			int size = kbc_Vector.size();

			// Loop through the KBC file data and add new StateCU_BlaneyCriddle
			// instances for each KBC entry...

			StateCU_BlaneyCriddle kbc;
			for (int i = 0; i < size; i++) {
				kbc = (StateCU_BlaneyCriddle)kbc_Vector.get(i);
				// Replace or add in the __CUBlaneyCriddle_Vector...
				processor.findAndAddCUBlaneyCriddle ( kbc, true );
			}

			// Warn about identifiers that have been replaced in the
			// __CUBlaneyCriddle_Vector...

			processor.warnAboutDataMatches ( this, true, processor.getStateCUBlaneyCriddleMatchList(),
					"Blaney-Criddle Crop Coefficients" );
		}
		/*
		else if (compType == StateCU_DataSet.COMP_DELAY_TABLES_MONTHLY) {
			StateMod_DelayTable.writeStateModFile(OutputFile_prevFull, InputFile_full,
				processor.getStateCUDelayTableMonthlyList(),
				StringUtil.toArray(OutputComments_List), StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY);
			// passing in the StateMod Monthly Delay Table
			// component type because it is used to set field
			// header information and not to do anything substantial
			// with the data, so it's safe to use.
		}
		*/
		else if ( this instanceof ReadCULocationsFromStateCU_Command ) {
	        Message.printStatus ( 2, routine,
	        	"Reading StateCU locations (structure) file \"" + InputFile_full + "\"" );
	    	// Remove all the elements for the list that tracks when identifiers
	    	// are read from more than one main source. This is used to print a warning.
	    	processor.resetDataMatches ( processor.getStateCULocationMatchList() );

	    	List culocList = StateCU_Location.readStateCUFile ( InputFile_full );

			int size = 0;
			if ( culocList != null ) {
				size = culocList.size();
			}

			// Loop through the data and add new StateCU_Location instances for each entry...

			StateCU_Location culoc;
			boolean foundWdidLength = false;
			String id;
			int wdidLength = 0;	// Length of WDIDs in data set
			for (int i = 0; i < size; i++) {
				culoc = (StateCU_Location)culocList.get(i);
				// Replace or add in the list...
				processor.findAndAddCULocation ( culoc, true );
				// Assume numeric fields are WDIDs and reset the default width for WDIDs in output.
				// This is used to make identifiers consistent in the data sets.
				if ( !foundWdidLength ) {
					id = culoc.getID();
					if ( HydroBase_WaterDistrict.isWDID(id) && (id.length() == 6) || (id.length() == 7) ) {
						wdidLength = id.length();
						if ( !foundWdidLength ) {
							Message.printStatus ( 2, routine,
							"Reading STR - assuming WDID length should be "+ wdidLength +
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

			// Warn about identifiers that have been replaced in the list...

			processor.warnAboutDataMatches ( this, true,
				processor.getStateCUClimateStationMatchList(), "CU Locations" );
		}
		else if ( this instanceof ReadCropPatternTSFromStateCU_Command ) {
			// Remove all the elements for the Vector that tracks when identifiers
			// are read from more than one main source (e.g., CDS, and HydroBase).
			// This is used to print a warning.
			processor.resetDataMatches ( processor.getStateCUCropPatternTSMatchList() );
			PropList read_props = new PropList ( "CropPatternTS" );
			if ( Version != null ) {
				read_props.set ( "Version", Version );
			}
			if ( ReadDataFrom != null ) {
				read_props.set ( "ReadDataFrom", ReadDataFrom );
			}
			// If an exception is thrown, let the calling code catch it...
			List cdsList = StateCU_CropPatternTS.readStateCUFile (
				InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, read_props );

			int size = cdsList.size();
			Message.printStatus(2, routine, "Read " + size + " crop pattern time series.");

			// Loop through the CDS file data and add new StateCU_CropPatternTS
			// instances for each CDS entry...

			StateCU_CropPatternTS cds;
			for (int i = 0; i < size; i++) {
				cds = (StateCU_CropPatternTS)cdsList.get(i);
				// Replace or add in the __CUCropPatternTS_Vector...
				processor.findAndAddCUCropPatternTS ( cds, true );
			}
			
			boolean CheckData_boolean = true; // Always do the check but may make a parameter later
			if ( CheckData_boolean ) {
				readCropPatternTSFromStateCU_checkCropPatternTS (
					(List)processor.getPropContents("StateCU_CropPatternTS_List"),
					(List)processor.getPropContents("StateCU_IrrigationPracticeTS_List"), status );
			}

			// Warn about identifiers that have been replaced in the processor list...

			processor.warnAboutDataMatches ( this, true,
				processor.getStateCUCropPatternTSMatchList(), "CU Crop Patterns" );
		}
		else if ( this instanceof ReadIrrigationPracticeTSFromStateCU_Command ) {
			// Remove all the elements for the list that tracks when identifiers
			// are read from more than one main source (e.g., CDS, and HydroBase).
			// This is used to print a warning.
			processor.resetDataMatches ( processor.getStateCUIrrigationPracticeTSMatchList() );
			// If an exception is thrown, let the calling code catch it...
			PropList read_props = new PropList ( "IrrigationPracticeTS" );
			if ( Version != null ) {
				read_props.set ( "Version", Version );
			}
			List ipyList = StateCU_IrrigationPracticeTS.readStateCUFile (
				InputFile_full, OutputStart_DateTime, OutputEnd_DateTime, read_props );

			int size = ipyList.size();

			// Loop through the IPY file data and add new
			// StateCU_IrrigationPracticeTS instances for each IPY entry...

			StateCU_IrrigationPracticeTS ipy;
			for (int i = 0; i < size; i++) {
				ipy = (StateCU_IrrigationPracticeTS)ipyList.get(i);
				// Replace or add in the __CUIrrigationPracticeTS_Vector...
				processor.findAndAddCUIrrigationPracticeTS ( ipy, true );
			}

			// Warn about identifiers that have been replaced in the processor list...

			processor.warnAboutDataMatches ( this, true,
				processor.getStateCUIrrigationPracticeTSMatchList(), "CU Irrigation Practice Time Series" );
		}
	  	else if ( this instanceof ReadIrrigationWaterRequirementTSMonthlyFromStateCU_Command ) {
			warning_count = readIrrigationWaterRequirementTSMonthlyFromStateMod ( processor,
				warning_level, warning_count, command_tag,
				status, command_phase, InputFile_full, OutputStart_DateTime, OutputEnd_DateTime );
		}
		else if (this instanceof ReadPenmanMonteithFromStateCU_Command) {
			// Remove all the elements for the list that tracks when identifiers
			// are read from more than one main source (e.g., KPM, and HydroBase).
			// This is used to print a warning.
			processor.resetDataMatches ( processor.getStateCUPenmanMonteithMatchList() );

			// If an exception is thrown, let the calling code catch it...
			List<StateCU_PenmanMonteith> kpm_Vector = StateCU_PenmanMonteith.readStateCUFile ( InputFile_full );

			// Loop through the KPM file data and add new StateCU_PenmanMonteith instances for each KPM entry...

			for ( StateCU_PenmanMonteith kpm: kpm_Vector) {
				// Replace or add in the __CUPenmanMonteith_Vector...
				processor.findAndAddCUPenmanMonteith ( kpm, true );
			}

			// Warn about identifiers that have been replaced in the __CUPenmanMonteith_Vector...

			processor.warnAboutDataMatches ( this, true, processor.getStateCUPenmanMonteithMatchList(),
				"Penman-Monteith Crop Coefficients" );
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
        status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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
	String ReadDataFrom = parameters.getValue ( "ReadDataFrom" );
	String Version = parameters.getValue ( "Version" );

	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( this instanceof ReadCropPatternTSFromStateCU_Command ) {
		if ( (ReadDataFrom != null) && (ReadDataFrom.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( ",");
			}
			b.append ( "ReadDataFrom=" + ReadDataFrom  );
		}
	}

	if ( (this instanceof ReadCropPatternTSFromStateCU_Command) ||
		(this instanceof ReadIrrigationPracticeTSFromStateCU_Command) ) {
		if ( (Version != null) && (Version.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( ",");
			}
			b.append ( "Version=" + Version  );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}