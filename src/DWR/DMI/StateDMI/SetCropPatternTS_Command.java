package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropPatternTS;
import RTi.TS.YearTS;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the SetCropPatternTS() command.
</p>
*/
public class SetCropPatternTS_Command extends AbstractCommand implements Command
{
	
/**
Flags for "ProcessWhen" property:
*/
protected final String _Now = "Now";
protected final String _WithParcels = "WithParcels";

protected final String _True = "True";
protected final String _False = "False";

protected final String _Sprinkler = "Sprinkler";
protected final String _Flood = "Flood";

protected final String _Surface = "Surface";
protected final String _Ground = "Ground";
	
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

private String [] __cropTypes = null;
private  double [] __areas = null;
	
/**
Constructor.
*/
public SetCropPatternTS_Command ()
{	super();
	setCommandName ( "SetCropPatternTS" );
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
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String CropPattern = parameters.getValue ( "CropPattern" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the identifier pattern to match." ) );
	}
	
	if ( (SetStart != null) && (SetStart.length() != 0) && !StringUtil.isInteger(SetStart)) {
		message = "The set start is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the set start as an integer year YYYY." ) );
	}
	
	if ( (SetEnd != null) && (SetEnd.length() != 0) && !StringUtil.isInteger(SetEnd)) {
		message = "The set end is not a valid year.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the set end as an integer year YYYY." ) );
	}
	
	if ( ((CropPattern == null) || (CropPattern.length() == 0)) &&
		((SetToMissing == null) || (SetToMissing.length() == 0)) ) {
		message = "The crop pattern or SetToMissing=" + _True + " must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the crop pattern or SetToMissing=" + _True + "." ) );
	}
	
	if ( ((CropPattern != null) && (CropPattern.length() > 0)) &&
		((SetToMissing != null) && (SetToMissing.length() > 0)) ) {
		message = "The crop pattern and SetToMissing=" + _True + " cannot both be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the crop pattern or SetToMissing=" + _True + "." ) );
	}
	
	// Parse the crop patterns - check the data and keep for use when running
	
	if ( (CropPattern != null) && (SetToMissing == null) ) {
		List tokens = StringUtil.breakStringList ( CropPattern,	", ", StringUtil.DELIM_SKIP_BLANKS );
		if ( tokens == null ) {
			message = "The crop pattern is not valid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern as Crop,Area,..." ) );
		}
		int size = tokens.size();
		if ( (size%2) != 0 ) {
			message = "The crop pattern is not valid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern (" + CropPattern + ") as Crop,Area,..." ) );
		}
		__cropTypes = new String[size/2];
		__areas = new double[size/2];
		for ( int i = 0; i < size; i++ ) {
			if ( i%2 == 0 ) {
				// Even, crop names....
				__cropTypes[i/2] = (String)tokens.get(i);
			}
			else {
				// Odd... area...
				if ( !StringUtil.isDouble( (String)tokens.get(i) ) ) {
					message = "CropPattern " + CropPattern + " value " + (String)tokens.get(i) +
					" is not a number.";
					warning += "\n" + message;
					status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the crop pattern area as a number." ) );
				}
				else {
					__areas[i/2] = StringUtil.atod((String)tokens.get(i) );
				}
			}
		}
	}
	
	if ( (IrrigationMethod != null) && !IrrigationMethod.equalsIgnoreCase(_Flood) &&
		!IrrigationMethod.equalsIgnoreCase(_Sprinkler) ) {
		message = "The irrigation method (" + IrrigationMethod + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the irrigation method as " + _Flood + " or " + _Sprinkler + ".") );
	}
	
	if ( (SupplyType != null) && !SupplyType.equalsIgnoreCase(_Ground) &&
		!SupplyType.equalsIgnoreCase(_Surface) ) {
		message = "The supply type (" + SupplyType + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the supply type as " + _Ground + " or " + _Surface + ".") );
	}
	
	if ( (SetToMissing != null) && (SetToMissing.length() > 0) &&
		!SetToMissing.equalsIgnoreCase(_False) && !SetToMissing.equalsIgnoreCase(_True) ) {
		message = "The SetToMissing value (" + SetToMissing + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify SetToMissing as " + _False + " (default) or " + _True + ".") );
	}
	
	if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels) &&
		((ID != null) && (ID.length() == 0) || (ID.indexOf("*") >= 0)) ) {
		message = "A wildcard ID cannot be specified with ProcessWhen=" + _WithParcels;
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Provide a single identifier to set the crop pattern.") );
	}
	if ( (ProcessWhen != null) && ProcessWhen.equalsIgnoreCase(_WithParcels) &&
		(SetToMissing != null) && SetToMissing.equalsIgnoreCase(_True) ) {
			message = "ProcessWhen=" + _WithParcels + " cannot be specified when SetToMissing=" + _True + ".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify crop parcel data (no missing allowed).") );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "ID" );
	valid_Vector.add ( "SetStart" );
	valid_Vector.add ( "SetEnd" );
	valid_Vector.add ( "CropPattern" );
	valid_Vector.add ( "IrrigationMethod" );
	valid_Vector.add ( "SupplyType" );
	valid_Vector.add ( "SetToMissing" );
	valid_Vector.add ( "ProcessWhen" );
	valid_Vector.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
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
{
	return (new SetCropPatternTS_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String Units = parameters.getValue ( "Units" );
	if ( (Units == null) || Units.equals("") ) {
		Units = "ACFT"; // Default
	}
	String SetStart = parameters.getValue ( "SetStart" );
	int SetStart_int = -1;
	if ( StringUtil.isInteger(SetStart)) {
		SetStart_int = StringUtil.atoi(SetStart);
	}
	String SetEnd = parameters.getValue ( "SetEnd" );
	int SetEnd_int = -1;
	if ( StringUtil.isInteger(SetEnd)) {
		SetEnd_int = StringUtil.atoi(SetEnd);
	}
	//String CropPattern = parameters.getValue ( "CropPattern" ); parsed in checkCommandParameters
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	boolean SetToMissing_boolean = false;
	if ( (SetToMissing == null) || SetToMissing.equals("") ) {
		SetToMissing = _False; // Default
	}
	else if ( SetToMissing.equalsIgnoreCase(_True) ) {
		SetToMissing_boolean = true;
	}
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	if ( (ProcessWhen == null) || ProcessWhen.equals("") ) {
		ProcessWhen = _Now; // Default
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of crop pattern time series...
	
	List<StateCU_CropPatternTS> cdsList = null;
	int cdsListSize = 0;
	try {
		cdsList = (List<StateCU_CropPatternTS>)processor.getPropContents( "StateCU_CropPatternTS_List");
		cdsListSize = cdsList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropPatternTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of supplemental parcel data...
	
	List<StateDMI_HydroBase_ParcelUseTS> supplementalParcelList = null;
	try {
		Object o = processor.getPropContents( "HydroBase_Supplemental_ParcelUseTS_List");
		supplementalParcelList = (List<StateDMI_HydroBase_ParcelUseTS>)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase_Supplemental_ParcelUseTS_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the output period

	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	try {
		OutputStart_DateTime = (DateTime)processor.getPropContents("OutputStart");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputStart from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	try {
		OutputEnd_DateTime = (DateTime)processor.getPropContents("OutputEnd");
	}
	catch ( Exception e ) {
		message = "Error requesting OutputEnd from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Set start and end are specified by user or come from output period
	if ( (SetStart_int < 0) && (OutputStart_DateTime != null) ) {
		SetStart_int = OutputStart_DateTime.getYear();
	}
	if ( (SetEnd_int < 0) && (OutputEnd_DateTime != null) ) {
		SetEnd_int = OutputEnd_DateTime.getYear();
	}
	
	// Make sure that the set start/end are not still missing
	
	if ( SetStart_int < 0 ) {
		message = "Set start (and output period) has not been specified - cannot set crop pattern time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set start." ) );
	}
	if ( SetEnd_int < 0 ) {
		message = "Set end (and output period) has not been specified - cannot set crop pattern time series.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Use the SetOutputPeriod() command prior to this command or specify set end." ) );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        		new CommandLogRecord(CommandStatusType.FAILURE,message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	int matchCount = 0;
	try {
		StateCU_CropPatternTS cds = null;
		String cds_id;
		int year = 0;
		List crop_names;	// From an existing CropPatternTS
		int ncrop_names = 0;
		double missing;	// Used to set data to missing
		YearTS ts = null;	// Crop time series to process.
		DateTime date = new DateTime(DateTime.PRECISION_YEAR);
		if ( ProcessWhen.equalsIgnoreCase("Now") ) {
			// Loop through the crop pattern data and try to find matching records...
			for (int i = 0; i < cdsListSize; i++) {
				cds = (StateCU_CropPatternTS)cdsList.get(i);
				cds_id = cds.getID();
				if ( !cds_id.matches(idpattern_Java) ) {
					// Identifier does not match...
					continue;
				}
				++matchCount;

				// Have a match so reset or save the data.

				// Reset the data.  First set the existing crop patterns for the location to zero.
				// This will ensure that any crops not mentioned in the command are set to zero for the
				// given years...

				crop_names = cds.getCropNames();
				ncrop_names = 0;
				if ( crop_names != null ) {
					ncrop_names = crop_names.size();
				}
				for ( int ic = 0; ic < ncrop_names; ic++ ) {
					ts = cds.getCropPatternTS ( (String)crop_names.get(ic) );
					missing = ts.getMissing();
					for ( year = SetStart_int; year <= SetEnd_int; year++ ) {
						date.setYear(year);
						if ( SetToMissing_boolean ) {
							// Set the crop value to missing...
							ts.setDataValue ( date, missing );
						}
						else {
							// Replace or add in the crop pattern list.  Pass
							// individual fields because we may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified
							(String)crop_names.get(ic),
							0.0,
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );
						}
					}
				}

				// Now reset to new data.  The number of crops does not
				// need to match the original...

				if ( !SetToMissing_boolean ) {
					for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
						for ( year = SetStart_int; year <= SetEnd_int; year++){
							// Replace or add in crop pattern time series list.  Pass
							// individual fields because we may or may not need to add a new
							// StateCU_CropPatternTS or a time series in the object...
							processor.findAndAddCUCropPatternTSValue (
							cds_id, cds_id,
							year,
							-1,	// Individual parcel ID not specified.
							__cropTypes[ic],
							__areas[ic],
							OutputStart_DateTime,
							OutputEnd_DateTime,
							Units, 0 );
						}
					}
				}

				// Refresh the contents to calculate total area.  If all time
				// series are missing, this will result in missing in the total.
				cds.refresh();
			}
		}
		else {
			// ProcessWhen=WithParcels
			// Save the data so that it can be processed later.  Add
			// a record for each year/crop/structure combination, as if a data
			// value had been read from HydroBase.  For each combination, print
			// a warning if an existing record is found.
			// Initialize the "has been processed" flag to false.  This will be
			// checked later to make sure the data are not used more than once.
			//int [] wdid_parts = new int[2];
			//StateDMI_HydroBase_StructureView sits, sits2;
			StateDMI_HydroBase_ParcelUseTS parcel, parcel2;
			int i2, size2;	// For loops.
			for ( int ic = 0; ic < __cropTypes.length; ic++ ) {
				for ( year = SetStart_int; year <= SetEnd_int; year++ ){
					parcel = new StateDMI_HydroBase_ParcelUseTS();
					parcel.setLocationID ( ID );
					parcel.setCal_year ( year );
					parcel.setLand_use ( __cropTypes[ic] );
					parcel.setArea ( __areas[ic] );
					parcel.setIrrig_type ( IrrigationMethod );
					parcel.setSupply_type ( SupplyType );
					// Check for duplicates and print a warning...
					size2 =	supplementalParcelList.size();
					for ( i2 = 0; i2 < size2; i2++ ) {
						parcel2 = (StateDMI_HydroBase_ParcelUseTS)supplementalParcelList.get(i2);
						if (	
							parcel2.getLocationID().equalsIgnoreCase(ID) &&
							(parcel2.getCal_year() == year) &&
							parcel2.getLand_use().equalsIgnoreCase(__cropTypes[ic]) &&
							parcel2.getIrrig_type().equalsIgnoreCase(IrrigationMethod) &&
							parcel2.getSupply_type().equalsIgnoreCase(SupplyType) ) {
							// Matching record, print warning...
							message = "Crop pattern matches existing user-supplied data.  Using again but needs checked.";
							Message.printWarning(warning_level,
								MessageUtil.formatMessageTag( command_tag, ++warning_count),
								routine, message );
							status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Check user-supplied data for duplicates." ) );
						}
					}
					// In any case, add to the list for later use...
					supplementalParcelList.add ( parcel );
					Message.printStatus ( 2, routine,
					"Location " + ID + " saving supplemental acreage data to use " +
					"later with ReadCropPatternTSFromHydroBase() for: year=" + parcel.getCal_year() +
					" crop=" + parcel.getLand_use() + " IrrigationMethod=" + parcel.getIrrig_type() +
					" SupplyType=" + parcel.getSupply_type() +
					" acres=" +	StringUtil.formatString(parcel.getArea(),"%.3f") );
				}
			}
		}

		// If nothing was matched, take further action...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Crop pattern time series \"" + ID +
				"\" was not matched: warning and not setting crop pattern time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." +
								"  The time series must be created before setting any data." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Crop pattern time series \"" + ID +
				"\" was not matched: failing and not setting crop pattern time series.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." +
								"  The time series must be created before setting any data." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting crop pattern time series (" + e + ").";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
        throw new CommandWarningException ( message );
    }
	
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
	String SetStart = parameters.getValue ( "SetStart" );
	String SetEnd = parameters.getValue ( "SetEnd" );
	String CropPattern = parameters.getValue ( "CropPattern" );
	String IrrigationMethod = parameters.getValue ( "IrrigationMethod" );
	String SupplyType = parameters.getValue ( "SupplyType" );
	String SetToMissing = parameters.getValue ( "SetToMissing" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( SetStart != null && SetStart.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetStart=" + SetStart );
	}
	if ( SetEnd != null && SetEnd.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEnd=" + SetEnd );
	}
	if ( CropPattern != null && CropPattern.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropPattern=\"" + CropPattern + "\"");
	}
	if ( IrrigationMethod != null && IrrigationMethod.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigationMethod=" + IrrigationMethod );
	}
	if ( SupplyType != null && SupplyType.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SupplyType=" + SupplyType );
	}
	if ( SetToMissing != null && SetToMissing.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetToMissing=" + SetToMissing );
	}
	if ( ProcessWhen != null && ProcessWhen.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessWhen=" + ProcessWhen );
	}
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}