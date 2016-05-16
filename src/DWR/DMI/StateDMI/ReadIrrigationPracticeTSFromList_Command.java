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
<p>
This class initializes, checks, and runs the ReadIrrigationPracticeTSFromList() command.
</p>
*/
public class ReadIrrigationPracticeTSFromList_Command 
extends AbstractCommand implements Command
{
	
// Possible values for parameters...

protected final String _False = "_False";
protected final String _True = "_True";

// IrrigationMethod possible data values...

protected final String _Flood = "Flood";
protected final String _Sprinkler = "Sprinkler";

// SupplyType possible data values...

protected final String _Ground = "Ground";
protected final String _Surface = "Surface";

/**
Constructor.
*/
public ReadIrrigationPracticeTSFromList_Command ()
{	super();
	setCommandName ( "ReadIrrigationPracticeTSFromList" );
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
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue ( "InputEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	String IDCol = parameters.getValue ( "IDCol" );
	/* TODO SAM 2007-10-08 Remove later if tests out.
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue ( "SprinklerAppEffMaxCol" );
	String AcresGWCol = parameters.getValue ( "AcresGWCol" );
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
	*/
	String AcresCol = parameters.getValue ( "AcresCol" );
	String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
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
    
	if ( (InputStart != null) && (InputStart.length() > 0) && !StringUtil.isInteger(InputStart) ) {
        message = "The input start year (" + InputStart + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid start year YYYY." ) );
	}
	if ( (InputEnd != null) && (InputEnd.length() > 0) && !StringUtil.isInteger(InputEnd) ) {
        message = "The input end year (" + InputEnd + ") is not a valid integer.";
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
	/*
	if ( (SurfaceDelEffMaxCol != null) && (SurfaceDelEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(SurfaceDelEffMaxCol) ) {
		warning += "\nThe surface water delivery max eff. column: \"" +
			SurfaceDelEffMaxCol + "\" is not a number.";
	}
	if ( (FloodAppEffMaxCol != null) && (FloodAppEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(FloodAppEffMaxCol) ) {
		warning += "\nThe flood application max eff column \"" +
			FloodAppEffMaxCol +	"\" is not a number.";
	}
	if ( (SprinklerAppEffMaxCol != null) && (SprinklerAppEffMaxCol.length() > 0) &&
		!StringUtil.isInteger(SprinklerAppEffMaxCol) ) {
		warning += "\nThe sprinkler application max eff. column: \"" +
			SprinklerAppEffMaxCol +	"\" is not a number.";
	}
	if ( (AcresGWCol != null) && (AcresGWCol.length() > 0) &&
		!StringUtil.isInteger(AcresGWCol) ) {
		warning += "\nThe acres irrigated by groundwater column: \"" +
			AcresGWCol + "\" is not a number.";
	}
	if ( (AcresSprinklerCol != null) && (AcresSprinklerCol.length() > 0) &&
		!StringUtil.isInteger(AcresSprinklerCol) ) {
		warning += "\nThe acres irrigated by sprinkler column: \"" +
			AcresSprinklerCol +	"\" is not a number.";
	}
	if ( (AcresSWFloodCol != null) && (AcresSWFloodCol.length() > 0) &&
			!StringUtil.isInteger(AcresSWFloodCol) ) {
			warning += "\nThe acres irrigated by surface water (flood) column: \"" +
				AcresSWFloodCol + "\" is not a number.";
	}
	if ( (AcresSWSprinklerCol != null) && (AcresSWSprinklerCol.length() > 0) &&
			!StringUtil.isInteger(AcresSWSprinklerCol) ) {
			warning += "\nThe acres irrigated by surface water (sprinkler) column: \"" +
				AcresSWSprinklerCol + "\" is not a number.";
	}
	if ( (AcresGWFloodCol != null) && (AcresGWFloodCol.length() > 0) &&
			!StringUtil.isInteger(AcresGWFloodCol) ) {
			warning += "\nThe acres irrigated by groundwater (flood) column: \"" +
				AcresGWFloodCol + "\" is not a number.";
	}
	if ( (AcresGWSprinklerCol != null) && (AcresGWSprinklerCol.length() > 0) &&
			!StringUtil.isInteger(AcresGWFloodCol) ) {
			warning += "\nThe acres irrigated by groundwater (sprinkler) column: \"" +
				AcresGWSprinklerCol + "\" is not a number.";
	}
	if ( (PumpingMaxCol != null) && (PumpingMaxCol.length() > 0) &&
		!StringUtil.isInteger(PumpingMaxCol) ) {
		warning += "\nThe maximum pumping column: \"" +
			PumpingMaxCol +	"\" is not a number.";
	}
	if ( (GWModeCol != null) && (GWModeCol.length() > 0) && !StringUtil.isInteger(GWModeCol) ) {
		warning += "\nThe groundwater mode column: \"" +
			GWModeCol + "\" is not a number.";
	}
	if ( (AcresTotalCol != null) && (AcresTotalCol.length() > 0) &&
		!StringUtil.isInteger(AcresTotalCol) ) {
		warning += "\nThe total acres column: \"" +
			AcresTotalCol +	"\" is not a number.";
	}
	*/
	
	if ( (AcresCol == null) || (AcresCol.length() == 0) ) {
		message = "The required acres column has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the acres column as an integer 1+." ) );
	}
	else if ( !StringUtil.isInteger(AcresCol) ) {
		message = "The acres column (" + AcresCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the acres column as an integer 1+." ) );
	}
	
	if ( (IrrigationMethodCol == null) || (IrrigationMethodCol.length() == 0) ) {
		message = "The required irrigation method column has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the irrigation method column as an integer 1+." ) );
	}
	else if ( !StringUtil.isInteger(IrrigationMethodCol) ) {
		message = "The irrigation method column (" + IrrigationMethodCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the irrigation method column as an integer 1+." ) );
	}
	
	if ( (SupplyTypeCol == null) || (SupplyTypeCol.length() == 0) ) {
		message = "The required supply type column has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the supply type column as an integer 1+." ) );
	}
	else if ( !StringUtil.isInteger(IrrigationMethodCol) ) {
		message = "The supply type column (" + IrrigationMethodCol + ") is not a valid integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the supply type column as an integer 1+." ) );
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "ListFile" );
	valid_Vector.add ( "InputStart" );
	valid_Vector.add ( "InputEnd" );
	valid_Vector.add ( "YearCol" );
	valid_Vector.add ( "IDCol" );
	valid_Vector.add ( "AcresCol" );
	valid_Vector.add ( "IrrigationMethodCol" );
	valid_Vector.add ( "SupplyTypeCol" );
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
	return (new ReadIrrigationPracticeTSFromList_JDialog ( parent, this )).ok();
}

/**
Process the readIrrigationPracticeTSFromList().
@param command_tag Command tag used for messaging.
@param command Command to process.
*/
private int readIrrigationPracticeTSFromList(
		String command_tag,	int warningLevel, int warningCount, CommandStatus status,
		CommandPhaseType commandPhase,
		String ListFile_full,
		String ID, String idpattern_Java,
		String IDCol, int IDCol_int,
		String YearCol, int YearCol_int,
		String AcresCol, int AcresCol_int,
		String IrrigationMethodCol, int IrrigationMethodCol_int,
		String SupplyTypeCol, int SupplyTypeCol_int,
		int InputStart_int, int InputEnd_int,
		List CULocation_Vector,
		List CUIrrigationPracticeTS_Vector,
		List Supplemental_ParcelUseTS_Vector )
throws CommandException, Exception
{	String routine = "ReadIrrigationPracticeTSFromList_Command.readIrrigationPracticeTSFromList";
	String message;

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
	
	// Make sure that requested columns are available
	
	warningCount = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
		"YearCol", YearCol, YearCol_int, table.getNumberOfFields(),
		status, commandPhase, routine, command_tag, warningLevel, warningCount );
	warningCount = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
		"IDCol", IDCol, IDCol_int, table.getNumberOfFields(),
		status, commandPhase, routine, command_tag, warningLevel, warningCount );
	warningCount = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
		"AcresCol", AcresCol, AcresCol_int, table.getNumberOfFields(),
		status, commandPhase, routine, command_tag, warningLevel, warningCount );
	warningCount = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
		"IrrigationMethodCol", IrrigationMethodCol, IrrigationMethodCol_int, table.getNumberOfFields(),
		status, commandPhase, routine, command_tag, warningLevel, warningCount );
	warningCount = StateDMICommandProcessorUtil.checkColumnAgainstFileNumberOfColumns (
		"SupplyTypeCol", SupplyTypeCol, SupplyTypeCol_int, table.getNumberOfFields(),
		status, commandPhase, routine, command_tag, warningLevel, warningCount );

	// Do this the brute force way...

	// Loop through the list file records.  If the CU location is matched,
	// find the irrigation practice information for the location.

	//StateCU_IrrigationPracticeTS ipy;
	String Year_String = "";
	/*
	String SurfaceDelEffMax_String = "";
	String FloodAppEffMax_String = "";
	String SprinklerAppEffMax_String = "";
	String AcresGW_String = "";
	String AcresSprinkler_String = "";
	String AcresGWFlood_String = "";
	String AcresGWSprinkler_String = "";
	String AcresSWFlood_String = "";
	String AcresSWSprinkler_String = "";
	String PumpingMax_String = "";
	String GWMode_String = "";
	String AcresTotal_String = "";
	*/
	String Acres_String = "";
	String IrrigationMethod_String = "";
	String SupplyType_String = "";
	/*
	boolean fill_SurfaceDelEffMax = false;
	boolean fill_FloodAppEffMax = false;
	boolean fill_SprinklerAppEffMax = false;
	boolean fill_AcresGW = false;
	boolean fill_AcresSprinkler = false;
	boolean fill_AcresGWFlood = false;
	boolean fill_AcresGWSprinkler = false;
	boolean fill_AcresSWFlood = false;
	boolean fill_AcresSWSprinkler = false;
	boolean fill_PumpingMax = false;
	boolean fill_GWMode = false;
	boolean fill_AcresTotal = false;
	*/
	boolean fill_Acres = false;
	boolean fill_IrrigationMethod = false;
	boolean fill_SupplyType = false;
	TableRecord rec = null;
	int matchCount = 0;
	int year;
	int pos = 0; // Location of ID in data component Vector
	int Year_int; // Integer value of year from column in file
	String id;
	// Loop through the table and see if there are any matches for the CU Location ID for the record.
	// This will be relatively fast if no YearCol is given and/or if the ID=*.
	for ( int j = 0; j < tsize; j++ ) {
		rec = table.getRecord(j);
		id = (String)rec.getFieldValue(IDCol_int);
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
		/* FIXME SAM 2007-10-16 OK to comment here since checked below.
		// Find the StateCU_IrrigationPracticeTS instance to modify...
		if ( ProcessWhen_int == Now_int ) {
			pos = StateCU_Util.indexOf(CUIrrigationPracticeTS_Vector,id);
			if ( pos < 0 ) {
				Message.printWarning(2, MessageUtil.formatMessageTag(command_tag,
					++warning_count), routine,
					"List file location \"" + id +
					"\" does not match any CU locations." );
				continue;
			}
			ipy = (StateCU_IrrigationPracticeTS)
			CUIrrigationPracticeTS_Vector.elementAt(pos);
		}
		*/
		// OK to set the data...
		// Get the data values from the table one time...
		++matchCount;
		Year_int = -1;	// Indicate not to set for a specific year
		if ( YearCol != null ) {
			Year_String = (String)rec.getFieldValue( YearCol_int);
			if ( !StringUtil.isInteger( Year_String)) {
				message = "Year in list file (" + Year_String
				+ ") is not an integer.  Skipping record " + (j + 1) + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that year value in list file is a valid integer year." ) );
				continue;
			}
			else {
				// Convert for use below...
				Year_int = Integer.parseInt( Year_String );
			}
		}
		/*
		fill_SurfaceDelEffMax = false;
		if ( SurfaceDelEffMaxCol != null ) {
			SurfaceDelEffMax_String =
			(String)rec.getFieldValue(
			SurfaceDelEffMaxCol_int);
			fill_SurfaceDelEffMax = true;
			if ( !StringUtil.isDouble(
				SurfaceDelEffMax_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"SurfaceDelEffMax (" +
				SurfaceDelEffMax_String
				+ ") is not a number.");
				fill_SurfaceDelEffMax = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id +
					" SurfaceDelEffMax " + Year_int + " -> "+
					SurfaceDelEffMax_String );
					ipy.setCeff ( Year_int,
						StringUtil.atod(
						SurfaceDelEffMax_String));
				}
			}
		}
		fill_FloodAppEffMax = false;
		if ( FloodAppEffMaxCol != null ) {
			FloodAppEffMax_String =
			(String)rec.getFieldValue(
			FloodAppEffMaxCol_int);
			fill_FloodAppEffMax = true;
			if ( !StringUtil.isDouble(
				FloodAppEffMax_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"FloodAppEffMax (" +
				FloodAppEffMax_String
				+ ") is not a number.");
				fill_FloodAppEffMax = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " FloodAppEffMax " +
					Year_int + " -> "+
					FloodAppEffMax_String );
					ipy.setFeff ( Year_int,
					StringUtil.atod(
					FloodAppEffMax_String));
				}
				fill_FloodAppEffMax = true;
			}
		}
		fill_SprinklerAppEffMax = false;
		if ( SprinklerAppEffMaxCol != null ) {
			SprinklerAppEffMax_String =
			(String)rec.getFieldValue(
			SprinklerAppEffMaxCol_int);
			fill_SprinklerAppEffMax = true;
			if ( !StringUtil.isDouble(
				SprinklerAppEffMax_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"SprinklerAppEffMax (" +
				SprinklerAppEffMax_String
				+ ") is not a number.");
				fill_SprinklerAppEffMax = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " SprinklerAppEffMax " +
					Year_int + " -> "+ SprinklerAppEffMax_String );
					ipy.setSeff ( Year_int,
						StringUtil.atod(
						SprinklerAppEffMax_String));
				}
			}
		}
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
		fill_AcresGWFlood = false;
		if ( AcresGWFloodCol != null ) {
			AcresGWFlood_String = (String)rec.getFieldValue(
			AcresGWFloodCol_int);
			fill_AcresGWFlood = true;
			if ( !StringUtil.isDouble(
				AcresGWFlood_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"AcresGWFlood (" + AcresGWFlood_String
				+ ") is not a number.");
				fill_AcresGWFlood = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " AcresGWFlood " + Year_int +
					" -> " + AcresGWFlood_String );
					ipy.setAcswfl ( Year_int,
					StringUtil.atod(
					AcresGWFlood_String));
				}
			}
		}
		fill_AcresGWSprinkler = false;
		if ( AcresGWSprinklerCol != null ) {
			AcresGWSprinkler_String = (String)rec.getFieldValue(
			AcresGWSprinklerCol_int);
			fill_AcresGWSprinkler = true;
			if ( !StringUtil.isDouble(
				AcresGWSprinkler_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"AcresGWSprinkler (" + AcresGWSprinkler_String
				+ ") is not a number.");
				fill_AcresGWSprinkler = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				Message.printStatus ( 2, routine,
				"Setting " + ipy_id + " AcresGWSprinkler " + Year_int +
				" -> " + AcresGWSprinkler_String );
				ipy.setAcgwspr ( Year_int,
					StringUtil.atod(
					AcresGWSprinkler_String));
			}
		}
		fill_AcresSWFlood = false;
		if ( AcresSWFloodCol != null ) {
			AcresSWFlood_String = (String)rec.getFieldValue(
			AcresSWFloodCol_int);
			fill_AcresSWFlood = true;
			if ( !StringUtil.isDouble(
				AcresSWFlood_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"AcresSWFlood (" + AcresSWFlood_String
				+ ") is not a number.");
				fill_AcresSWFlood = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " AcresSWFlood " + Year_int +
					" -> " + AcresSWFlood_String );
					ipy.setAcgwfl ( Year_int,
					StringUtil.atod(
					AcresSWFlood_String));
				}
			}
		}
		fill_AcresSWSprinkler = false;
		if ( AcresSWSprinklerCol != null ) {
			AcresSWSprinkler_String = (String)rec.getFieldValue(
			AcresSWSprinklerCol_int);
			fill_AcresSWSprinkler = true;
			if ( !StringUtil.isDouble(
				AcresSWSprinkler_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"AcresSWSprinkler (" + AcresSWSprinkler_String
				+ ") is not a number.");
				fill_AcresSWSprinkler = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " AcresSWSprinkler " + Year_int +
					" -> " + AcresSWSprinkler_String );
					ipy.setAcswspr ( Year_int,
					StringUtil.atod(
					AcresSWSprinkler_String));
				}
			}
		}
		fill_PumpingMax = false;
		if ( PumpingMaxCol != null ) {
			PumpingMax_String = (String)rec.getFieldValue(
			PumpingMaxCol_int);
			fill_PumpingMax = true;
			if ( !StringUtil.isDouble(
				PumpingMax_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"PumpingMax (" + PumpingMax_String
				+ ") is not a number.");
				fill_PumpingMax = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " PumpingMax " +
					Year_int + " -> "+ PumpingMax_String );
					ipy.setMprate ( Year_int,
					StringUtil.atod( PumpingMax_String));
				}
			}
		}
		fill_GWMode = false;
		if ( GWModeCol != null ) {
			GWMode_String = (String)rec.getFieldValue(
			GWModeCol_int);
			fill_GWMode = true;
			if ( !StringUtil.isInteger( GWMode_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"GWMode (" + GWMode_String
				+ ") is not a number.");
				fill_GWMode = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " GWMode " + Year_int +
					" -> " + GWMode_String );
					ipy.setGmode ( Year_int,
					StringUtil.atoi( GWMode_String));
				}
			}
		}
		fill_AcresTotal = false;
		if ( AcresTotalCol != null ) {
			AcresTotal_String = (String)rec.getFieldValue(
			AcresTotalCol_int);
			fill_AcresTotal = true;
			if ( !StringUtil.isDouble(
				AcresTotal_String)) {
				Message.printWarning(2,
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine,
				"AcresTotal (" + AcresTotal_String
				+ ") is not a number.");
				fill_AcresTotal = false;
			}
			else if ( Year_int > 0 ) {
				// Set the data for the specified year...
				if ( ProcessWhen_int == Now_int ) {
					Message.printStatus ( 2, routine,
					"Setting " + ipy_id + " AcresTotal " +
					Year_int + " -> " + AcresTotal_String );
					ipy.setTacre ( Year_int,
					StringUtil.atod( AcresTotal_String));
				}
			}
		}
		*/
		fill_Acres = false;
		if ( AcresCol != null ) {
			Acres_String = (String)rec.getFieldValue(AcresCol_int);
			fill_Acres = true;
			if ( !StringUtil.isDouble(Acres_String)) {
				message = "AcresTotal (" + Acres_String + ") is not a number.";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the acres value in the list file is a valid number." ) );
				fill_Acres = false;
			}
		}
		fill_IrrigationMethod = false;
		if ( IrrigationMethodCol != null ) {
			IrrigationMethod_String = (String)rec.getFieldValue(IrrigationMethodCol_int);
			fill_IrrigationMethod = true;
			if ( !IrrigationMethod_String.equalsIgnoreCase(_Flood) &&
				!IrrigationMethod_String.equalsIgnoreCase(_Sprinkler) ) {
				message = "IrrigationMethod (" + IrrigationMethod_String + ") is not " +
				_Flood + " or " + _Sprinkler + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the irrigation method in the list file is a valid value." ) );
				fill_IrrigationMethod = false;
			}
		}
		fill_SupplyType = false;
		if ( SupplyTypeCol != null ) {
			SupplyType_String = (String)rec.getFieldValue(
			SupplyTypeCol_int);
			fill_SupplyType = true;
			if ( !SupplyType_String.equalsIgnoreCase(_Ground) && !SupplyType_String.equalsIgnoreCase(_Surface) ) {
				message = "SupplyType (" + SupplyType_String + ") is not " + _Ground + " or " + _Surface + ".";
				Message.printWarning(warningLevel,
					MessageUtil.formatMessageTag( command_tag, ++warningCount),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the supply type in the list file is a valid value." ) );
				fill_SupplyType = false;
			}
		}
		// Can only process if all values are specified.
		if ( !fill_Acres || !fill_IrrigationMethod || !fill_SupplyType ) {
			message = "Unable to set data becauase one or more values in list record are invalid.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See previous messages - correct the list file data." ) );
			continue;
		}
		if ( Year_int > 0 ) {
			// Process supplemental records using code similar to the
			// readIrrigationPracticeTSFromHydroBase command.
			StateDMI_HydroBase_ParcelUseTS hbputs = new StateDMI_HydroBase_ParcelUseTS();
			hbputs.setCal_year ( Year_int );
			hbputs.setLocationID ( id );
			if ( fill_Acres ) {
				hbputs.setArea ( StringUtil.atod(Acres_String) );
			}
			if ( fill_IrrigationMethod ) {
				hbputs.setIrrig_type ( IrrigationMethod_String );
			}
			if ( fill_SupplyType ) {
				hbputs.setSupply_type ( SupplyType_String );
			}
			Supplemental_ParcelUseTS_Vector.add ( hbputs );
		}
		// Set for the specified period if the year column was not specified.
		// This should only get executed once per location if the list file is set up properly.
		if ( YearCol == null ) {
			for ( year = InputStart_int; year <= InputEnd_int; year++ ){
				/*
				if ( fill_SurfaceDelEffMax ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" SurfaceDelEffMax " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						SurfaceDelEffMax_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setCeff ( year,
						StringUtil.atod(
						SurfaceDelEffMax_String));
					}
				}
				if ( fill_FloodAppEffMax ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" FloodAppEffMax " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						FloodAppEffMax_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setFeff ( year,
						StringUtil.atod(
						FloodAppEffMax_String));
					}
				}
				if ( fill_SprinklerAppEffMax ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" SprinklerAppEffMax " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						SprinklerAppEffMax_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setSeff ( year,
						StringUtil.atod(
						SprinklerAppEffMax_String));
					}
				}
				if ( fill_AcresGW ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresGW " + InputStart_int +
						" to " + InputEnd_int + " -> "+
						AcresGW_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setGacre ( year,
						StringUtil.atod(
						AcresGW_String));
					}
				}
				if ( fill_AcresSprinkler ) {
					if ( year ==InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresSprinkler " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						AcresSprinkler_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setSacre ( year,
						StringUtil.atod(
						AcresSprinkler_String));
					}
				}
				if ( fill_AcresGWFlood ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresGWFlood " + InputStart_int +
						" to " + InputEnd_int + " -> "+
						AcresGWFlood_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setAcgwfl ( year,
						StringUtil.atod(
						AcresGWFlood_String));
					}
				}
				if ( fill_AcresGWSprinkler) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresGWSprinkler " + InputStart_int +
						" to " + InputEnd_int + " -> "+
						AcresGWSprinkler_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setAcgwspr ( year,
						StringUtil.atod(
						AcresGWSprinkler_String));
					}
				}
				if ( fill_AcresSWFlood) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresSWFlood " + InputStart_int +
						" to " + InputEnd_int + " -> "+
						AcresSWFlood_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setAcswfl ( year,
								StringUtil.atod(
						AcresSWFlood_String));
					}
				}
				if ( fill_AcresSWSprinkler) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,
						routine, "Setting " + ipy_id +
						" AcresSWSprinkler " + InputStart_int +
						" to " + InputEnd_int + " -> "+
						AcresSWSprinkler_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setAcswspr ( year,
						StringUtil.atod(
						AcresSWSprinkler_String));
					}
				}
				if ( fill_PumpingMax ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,routine,
						"Setting " + ipy_id +
						" PumpingMax " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						PumpingMax_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setMprate ( year,
						StringUtil.atod(
						PumpingMax_String));
					}
				}
				if ( fill_GWMode ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,routine,
						"Setting " + ipy_id +" GWMode "+
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						GWMode_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setGmode ( year,
						StringUtil.atoi(
						GWMode_String));
					}
				}
				if ( fill_AcresTotal ) {
					if ( year == InputStart_int ) {
						Message.printStatus ( 2,routine,
						"Setting " + ipy_id +
						" AcresTotal " +
						InputStart_int + " to " +
						InputEnd_int + " -> "+
						AcresTotal_String );
					}
					if ( ProcessWhen_int == Now_int ) {
						ipy.setTacre ( year,
						StringUtil.atod(
						AcresTotal_String));
					}
				}
				*/
				// Save supplemental records to process similar to the
				// readIrrigationPracticeTSFromHydroBase command.
				StateDMI_HydroBase_ParcelUseTS hbputs = new StateDMI_HydroBase_ParcelUseTS();
				hbputs.setCal_year ( year );
				hbputs.setLocationID ( id );
				// Records are primitive so store total acres...
				if ( fill_Acres ) {
					hbputs.setArea ( StringUtil.atod(Acres_String) );
				}
				if ( fill_IrrigationMethod ) {
					hbputs.setIrrig_type ( IrrigationMethod_String );
				}
				if ( fill_SupplyType ) {
					hbputs.setSupply_type ( SupplyType_String );
				}
				// Indicate that the record has not yet been processed...
				hbputs.setHasBeenProcessed(false);
				Supplemental_ParcelUseTS_Vector.add ( hbputs );
			}
		}
	}
	// Now process the supplemental records that have been accumulated...
	int size = Supplemental_ParcelUseTS_Vector.size();
	StateDMI_HydroBase_ParcelUseTS hbputs;
	for ( int i = 0; i < size; i++ ) {
		hbputs = (StateDMI_HydroBase_ParcelUseTS)Supplemental_ParcelUseTS_Vector.get(i);
		// Only process the record if it has not already been processed.  This prevents
		// double-counting when more than one year of supplemental data are read.
		if ( hbputs.getHasBeenProcessed() ) {
			continue;
		}
		else {
			// Set to true even if errors may occur below.  In any case don't want to reprocess
			// the same record.
			hbputs.setHasBeenProcessed ( true );
		}
		// The part ID can be part of an aggregate/system or may be a main ID...
		String part_id = hbputs.getLocationID();
		// Figure out the main location to add to.
		StateCU_Location culoc = StateCU_Util.getLocationForPartID(CULocation_Vector,part_id);
		if ( culoc == null ) {
			message = "List file location \"" + part_id +
				"\" does not match any CU locations or ditch aggregate/system parts.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the list file location identifier is valid." ) );
			continue;
		}
		// Get the main ID...
		id = culoc.getID();
		// Get the irrigation practice time series.
		pos = StateCU_Util.indexOf(CUIrrigationPracticeTS_Vector,id);
		if ( pos < 0 ) {
			message = "List file location \"" + id +
				"\" does not match any irrigation practice TS.  " +
				"Verify that IPY time series were created using location list.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the list file location identifier is valid." ) );
			continue;
		}
		StateCU_IrrigationPracticeTS ipyts = (StateCU_IrrigationPracticeTS)CUIrrigationPracticeTS_Vector.get(pos);
		// The following method allows GW supply to be indicated with a boolean.
		// For here, just check the supply type.
		boolean has_gw_supply = false;
		if ( hbputs.getSupply_type().equalsIgnoreCase(_Ground)) {
			has_gw_supply = true;
		}
		// Now add to the main location...
		StateDMI_Util.processIrrigationPracticeTSParcel (
			id,
			ipyts,
			-1,		// Parcel ID is not specified
			"",		// Crop type is not needed
			hbputs.getArea(),
			hbputs.getIrrig_type(),
			hbputs.getCal_year(),
			has_gw_supply );
	}
	return warningCount;
}

/**
Method to execute the readIrrigationPracticeTSFromList() command.
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
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue ( "InputEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	int YearCol_int = -1;
	if ( YearCol != null ) {
		YearCol_int = StringUtil.atoi (YearCol) - 1;
	}
	String IDCol = parameters.getValue ( "IDCol" );
	int IDCol_int = StringUtil.atoi ( IDCol ) - 1;
	/*
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue ( "SprinklerAppEffMaxCol" );
	String AcresGWCol = parameters.getValue ( "AcresGWCol" );
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
	*/
	String AcresCol = parameters.getValue ( "AcresCol" );
	int AcresCol_int = -1;
	if ( AcresCol != null ) {
		AcresCol_int = StringUtil.atoi (AcresCol) - 1;
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
	/*
	int SurfaceDelEffMaxCol_int = -1;
	if ( SurfaceDelEffMaxCol != null ) {
		SurfaceDelEffMaxCol_int = StringUtil.atoi (SurfaceDelEffMaxCol) - 1;
	}

	int FloodAppEffMaxCol_int = -1;
	if ( FloodAppEffMaxCol != null ) {
		FloodAppEffMaxCol_int = StringUtil.atoi ( FloodAppEffMaxCol) - 1;
	}

	int SprinklerAppEffMaxCol_int = -1;
	if ( SprinklerAppEffMaxCol != null ) {
		SprinklerAppEffMaxCol_int = StringUtil.atoi(SprinklerAppEffMaxCol)-1;
	}

	int AcresGWCol_int = -1;
	if ( AcresGWCol != null ) {
		AcresGWCol_int = StringUtil.atoi (AcresGWCol) - 1;
	}

	int AcresSprinklerCol_int = -1;
	if ( AcresSprinklerCol != null ) {
		AcresSprinklerCol_int = StringUtil.atoi (AcresSprinklerCol) - 1;
	}
	
	int AcresGWFloodCol_int = -1;
	if ( AcresGWFloodCol != null ) {
		AcresGWFloodCol_int = StringUtil.atoi (AcresGWFloodCol) - 1;
	}
	
	int AcresGWSprinklerCol_int = -1;
	if ( AcresGWSprinklerCol != null ) {
		AcresGWSprinklerCol_int = StringUtil.atoi (AcresGWSprinklerCol) - 1;
	}
	
	int AcresSWFloodCol_int = -1;
	if ( AcresSWFloodCol != null ) {
		AcresSWFloodCol_int = StringUtil.atoi (AcresSWFloodCol) - 1;
	}
	
	int AcresSWSprinklerCol_int = -1;
	if ( AcresSWSprinklerCol != null ) {
		AcresSWSprinklerCol_int = StringUtil.atoi (AcresSWSprinklerCol) - 1;
	}

	int PumpingMaxCol_int = -1;
	if ( PumpingMaxCol != null ) {
		PumpingMaxCol_int = StringUtil.atoi (PumpingMaxCol) - 1;
	}

	int GWModeCol_int = -1;
	if ( GWModeCol != null ) {
		GWModeCol_int = StringUtil.atoi (GWModeCol) - 1;
	}

	int AcresTotalCol_int = -1;
	if ( AcresTotalCol != null ) {
		AcresTotalCol_int = StringUtil.atoi (AcresTotalCol) - 1;
	}
	*/

	String ListFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
            StateDMICommandProcessorUtil.expandParameterValue(processor,this,ListFile)));

	// Get the data needed for processing
	
	// Get the list of CU locations...
	
	List culocList = null;
	try {
		culocList = (List)processor.getPropContents ( "StateCU_Location_List");
	}
	catch ( Exception e ) {
		message = "Error requesting CU location data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the irrigation practice time series to process.
	
	List ipyList = null;
	int ipyListSize = 0;
	try {
		ipyList = (List)processor.getPropContents ( "StateCU_IrrigationPracticeTS_List");
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
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	int InputStart_int = 0, InputEnd_int = 0;
	if ( InputStart == null ) {
		if ( OutputStart_DateTime == null ) {
			message = "Input start and global OutputStart are not set.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Set the input start or use SetOutputPeriod() before this command." ) );
		}
		else {
			InputStart_int = OutputStart_DateTime.getYear();
		}
	}
	else {
		InputStart_int = Integer.parseInt(InputStart);
	}

	if ( InputEnd == null ) {
		if ( OutputEnd_DateTime == null ) {
			message = "Input end and global OutputEnd are not set.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Set the input start or use SetOutputPeriod() before this command." ) );
		}
		else {
			InputEnd_int = OutputEnd_DateTime.getYear();
		}
	}
	else {
		InputEnd_int = Integer.parseInt(InputEnd);
	}

	try {
		warning_count = readIrrigationPracticeTSFromList (
			command_tag, warningLevel, warning_count, status,
			commandPhase,
			ListFile_full,
			ID, idpattern_Java,
			IDCol, IDCol_int,
			YearCol, YearCol_int,
			AcresCol, AcresCol_int,
			IrrigationMethodCol, IrrigationMethodCol_int,
			SupplyTypeCol, SupplyTypeCol_int,
			InputStart_int, InputEnd_int,
			culocList,
			ipyList,
			hydroBaseSupplementalParcelUseTSList );
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting crop pattern time series data (" + e + ").";
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
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue ( "InputEnd" );
	String YearCol = parameters.getValue ( "YearCol" );
	/*
	String SurfaceDelEffMaxCol = parameters.getValue ( "SurfaceDelEffMaxCol" );
	String FloodAppEffMaxCol = parameters.getValue ( "FloodAppEffMaxCol" );
	String SprinklerAppEffMaxCol = parameters.getValue("SprinklerAppEffMaxCol");
	String AcresGWCol = parameters.getValue ( "AcresGWCol" );
	String AcresSprinklerCol = parameters.getValue ( "AcresSprinklerCol" );
	String AcresGWFloodCol = parameters.getValue ( "AcresGWFloodCol" );
	String AcresGWSprinklerCol = parameters.getValue ( "AcresGWSprinklerCol" );
	String AcresSWFloodCol = parameters.getValue ( "AcresSWFloodCol" );
	String AcresSWSprinklerCol = parameters.getValue ( "AcresSWSprinklerCol" );
	String PumpingMaxCol = parameters.getValue ( "PumpingMaxCol" );
	String GWModeCol = parameters.getValue ( "GWModeCol" );
	String AcresTotalCol = parameters.getValue ( "AcresTotalCol" );
	String ProcessWhen = parameters.getValue ( "ProcessWhen" );
	*/
	String AcresCol = parameters.getValue ( "AcresCol" );
	String IrrigationMethodCol = parameters.getValue ( "IrrigationMethodCol" );
	String SupplyTypeCol = parameters.getValue ( "SupplyTypeCol" );
	
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
	if ( (InputStart != null) && (InputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputStart=" + InputStart  );
	}
	if ( (InputEnd != null) && (InputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputEnd=" + InputEnd  );
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
	/*
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
	if ( (ProcessWhen != null) && (ProcessWhen.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessWhen=" + ProcessWhen );
	}
	*/
	if ( (AcresCol != null) && (AcresCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AcresCol=\"" + AcresCol + "\"" );
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
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
