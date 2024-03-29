// WriteParcelsToFile_Command - This class initializes, checks, and runs the Write*ParcelsToFile() commands.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2023 Colorado Department of Natural Resources

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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DWR.StateCU.IncludeParcelInCdsType;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Parcel_Comparator;
import DWR.StateCU.StateCU_Supply;
import DWR.StateCU.StateCU_SupplyFromGW;
import DWR.StateCU.StateCU_SupplyFromSW;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the Write*ParcelsToFile() commands.
*/
public class WriteParcelsToFile_Command extends AbstractCommand implements FileGenerator
{

/**
Possible values for the Verbose parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
 *  File formats
 */
protected final String _ModelParcelSupply = "ModelParcelSupply";
protected final String _ParcelSupply = "ParcelSupply";

/**
List of output files that are created by this command.
*/
private List<File> __OutputFile_List = null;

/**
Constructor.
*/
public WriteParcelsToFile_Command () {
	super();
	//setCommandName ( "Write?ParcelsToFile" );
	setCommandName ( "WriteParcelsToFile" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Verbose = parameters.getValue ( "Verbose" );
	//String Delimiter = parameters.getValue ( "Delimiter" );
	String warning = "";
	String message;

    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		message = "The output file must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify an output file." ) );
	}
	else {
		String working_dir = null;
			try {
				Object o = processor.getPropContents ( "WorkingDir" );
				// Working directory is available so use it.
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
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the output directory." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The output file:\n" +
			"    \"" + OutputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}

	if ( (Verbose != null) && (Verbose.length() != 0) &&
		!Verbose.equals(_False) && !Verbose.equals(_True) ) {
        message = "The value for Verbose (" + Verbose + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify Value as " + _False + " (default) or " + _True ) );
	}

	// Check for invalid parameters.
	List<String> validList = new ArrayList<>(3);
	validList.add ( "OutputFile" );
	validList.add ( "FileFormat" );
	validList.add ( "Verbose" );
	validList.add ( "Delimiter" );
    warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
public boolean editCommand ( JFrame parent ) {
	// The command will be modified if changed.
	return (new WriteParcelsToFile_JDialog ( parent, this )).ok();
}

// Use parent parseCommand.

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList () {
	List<File> outputFileList = getOutputFileList();
	if ( outputFileList == null ) {
		return new ArrayList<>();
	}
	else {
		return outputFileList;
	}
}

/**
Return the list of output files generated by this method.  This method is used internally.
*/
protected List<File> getOutputFileList () {
	return __OutputFile_List;
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warningLevel = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings.
    CommandPhaseType commandPhase = CommandPhaseType.RUN;

	// Check whether the processor wants output files to be created.

    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	if ( !StateDMICommandProcessorUtil.getCreateOutput(processor) ) {
		Message.printStatus ( 2, routine,
		"Skipping \"" + toString() + "\" because output is not being created." );
	}

	CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(command_phase);

    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" );
    String FileFormat = parameters.getValue ( "FileFormat" );
    if ( (FileFormat == null) || FileFormat.equals("") ) {
    	FileFormat = _ModelParcelSupply; // Default.
    }
    String Verbose = parameters.getValue ( "Verbose" );
    boolean verbose = false; // Default.
    if ( (Verbose != null) && Verbose.equalsIgnoreCase(_True) ) {
    	verbose = true;
    }
    String Delimiter = parameters.getValue ( "Delimiter" );
    if ( (Delimiter == null) || Delimiter.equals("") ) {
    	Delimiter = ","; // Default.
    }

    String OutputFile_full = OutputFile;
    try {

    	/*
	   	Get the map of parcel data for newer StateDMI.
	   	*/
	   	HashMap<String,StateCU_Parcel> parcelMap = null;
	   	try {
		   	@SuppressWarnings("unchecked")
		   	HashMap<String,StateCU_Parcel> dataMap =
			   	(HashMap<String,StateCU_Parcel>)processor.getPropContents ( "StateCU_Parcel_List");
		   	parcelMap = dataMap;
	   	}
	   	catch ( Exception e ) {
		   	message = "Error requesting parcel data from processor.";
		   	Message.printWarning(warningLevel,
			   	MessageUtil.formatMessageTag( command_tag, ++warning_count),
			   	routine, message );
		   	status.addToLog ( CommandPhaseType.RUN,
			   	new CommandLogRecord(CommandStatusType.FAILURE,
				   	message, "Report problem to software support." ) );
	   	}
	   	if ( parcelMap == null ) {
		   	message = "Parcel list (map) is null.";
		   	Message.printWarning ( warningLevel,
			   	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		   	status.addToLog ( commandPhase,
			   	new CommandLogRecord(CommandStatusType.FAILURE,
				   	message, "Software logic problem - results will not be correct.") );
	   	}

        // Get the comments to add to the top of the file.

        List<String> OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them.
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<String> outputCommentsList = (List<String>)o;
                OutputComments_List = outputCommentsList;
            }
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
            Message.printWarning(3, routine, message );
            Message.printWarning(3, routine, e );
        }

    	// Clear the filename for the FileGenerator interface.
    	setOutputFileList ( null );
    	List<File> outputFileList = null; // If non-null below, then a method returned the list of files.
    	OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );

        // StateCU components.

        /* TODO smalers 2020-10-11 remove when tests out.
		if ( this instanceof WriteCropPatternTSParcelsToFile_Command ) {
			writeCropPatternTSParcelsToTextFile(OutputFile_full, Delimiter, update,
				processor.getStateCUCropPatternTSList(), processor.getStateCULocationList(), OutputComments_List );
		}
		else if ( this instanceof WriteCULocationParcelsToFile_Command ) {
			writeCULocationParcelsToTextFile(OutputFile_full, Delimiter, update,
				processor.getStateCULocationList(), processor.getStateCULocationList(), OutputComments_List );
		}
		*/

        // Translate the dictionary of parcels to a list:
        // - get from the map because extracting from the StateCU_Locations may result in redundancies.
        List<StateCU_Parcel> parcelList = new ArrayList<>();
		for ( Map.Entry<String, StateCU_Parcel> entry : parcelMap.entrySet() ) {
        	parcelList.add( entry.getValue() );
        }

        Message.printStatus ( 2, routine, "Writing " + parcelList.size() + " parcels to file \"" + OutputFile_full + "\"" );

        // Initial implementation is for parcels associated with StateCU locations.
		writeCULocationParcelsToTextFile( FileFormat, OutputFile_full, Delimiter, verbose,
			processor.getStateCULocationList(), parcelList, OutputComments_List );

    	// Set the filename(s) for the FileGenerator interface.
    	if ( outputFileList == null ) {
    		// Create a list with a single file.
    		outputFileList = new ArrayList<>(1);
    		outputFileList.add ( new File(OutputFile_full) );
    	} // Otherwise the write method returned the list of filenames for output files.
    	setOutputFileList ( outputFileList );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
        Message.printWarning ( warningLevel,
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
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFileList ( List<File> outputFileList ) {
	__OutputFile_List = outputFileList;
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters ) {
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String OutputFile = parameters.getValue ( "OutputFile" );
	String FileFormat = parameters.getValue ( "FileFormat" );
	String Verbose = parameters.getValue ( "Verbose" );
	String Delimiter = parameters.getValue ( "Delimiter" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (FileFormat != null) && (FileFormat.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FileFormat=" + FileFormat );
	}
	if ( (Verbose != null) && (Verbose.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Verbose=" + Verbose );
	}
	if ( (Delimiter != null) && (Delimiter.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Delimiter=" + Delimiter );
	}

	return getCommandName() + "(" + b.toString() + ")";
}

/**
 * Write the parcel data as a text file, using StateCU_Location as input.
 * @param fileFormat format for output file (_ModelParcelSupply or _ParcelSupply)
 * @param outputFileFull full path to output file
 * @param delimiter delimiter for output, not currently used because format is predefined
 * @param verbose output verbose format - used with ModelParcelSupply format
 * @param culocList list of StateCU_Location, when processing ModelParcelSupply
 * @param parcelList list of StateCU_Parcel, when processing ParcelSupply
 * @param outputCommentsList output comments for the top of the file
 */
private void writeCULocationParcelsToTextFile ( String fileFormat, String outputFileFull, String delimiter, boolean verbose,
	List<StateCU_Location> culocList, List<StateCU_Parcel> parcelList, List<String> outputCommentsList ) {

	// Always overwrite the file rather than update.
	boolean update = false;

	// Call the general write method.
	if ( fileFormat.equalsIgnoreCase(_ModelParcelSupply) ) {
		writeParcelsToModelParcelSupplyFile ( outputFileFull, delimiter, update, culocList, outputCommentsList, verbose );
	}
	else if ( fileFormat.equalsIgnoreCase(_ParcelSupply) ) {
		writeParcelsToParcelSupplyFile ( outputFileFull, delimiter, update, parcelList, outputCommentsList );
	}
}

/**
 * Write the parcel data as a text file, using StateCU_Parcel as input.
 * This is the most general method and can be used to write parcel data that was read in
 * using ReadCULocationParcelsFromHydroBase or ?? StateMod command ??
 * @param outputFileFull full path to output file
 * @param delimiter not currently used, may remove
 * @param update whether to update or overwrite the file, used when creating the file header
 * @param culocList list of StateCU_Location to output
 * @param outputCommentList additional comments for the file header
 * @param verbose whether to output verbose format - this result in:
 * <ul>
 * <li> parcels with surface water supply for groundwater only model nodes are not listed in output
 *      (they have CDS=NO anyhow)</li>
 * </ul>
 */
private void writeParcelsToModelParcelSupplyFile ( String outputFileFull, String delimiter, boolean update,
	List<StateCU_Location> culocList, List<String> outputCommentsList, boolean verbose ) {
	String routine = getClass().getSimpleName() + ".writeParcelsToModelParcelSupplyFile";

	List<String> newComments = new ArrayList<>();
	List<String> commentIndicators = new ArrayList<>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new ArrayList<>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	try {
		// Overwrite the file.
		out = IOUtil.processFileHeaders(
			null, outputFileFull,
			//IOUtil.getPathUsingWorkingDir(instrfile),
			//IOUtil.getPathUsingWorkingDir(outstrfile),
			newComments, commentIndicators, ignoredCommentIndicators, 0);

		String printLine = null;
		String cmnt = "#>";
		String format_1 =
			"%-12.12s %-4.4s %-11.11s %4d %-10.10s %4d %4d %-20.20s %10.3f %-4.4s %-10.10s                                     %-8.8s";

		// Surface water fields - space on the left to skip over above formatting.
		String format_2 = "                                                                                                        " +
			"%-8.8s %-8.8s %-12.12s %-4.4s          %-8.8s %3d %-8.8s %5.3f %5.3f %10.3f %-7.7s";

		// Groundwater fields - data source and include align with  surface water and then space over to groundwater columns.
		String format_3 = "                                                                                                        %-8.8s %-8.8s %-12.12s %-4.4s             " +
			"                                                  %-12.12s %-7.7s %-8.8s %-10.10s %4d %5.3f %5.5s %9.3f";

		// Size to largest size.
		List<Object> objectList = new ArrayList<>(8);

		out.println(cmnt);
		out.println(cmnt + " ***************************************************************************************************");
		out.println(cmnt + "  StateDMI Model / Parcel / Supply File - this is a diagnostics report");
		out.println(cmnt + "  - this report shows the relationships between a parcel and supplies for the parcel");
		out.println(cmnt + "  - the parcel data should match orginal GIS irrigated land and supply data");
		out.println(cmnt + "  - model data reflect model location identifiers and collections");
		out.println(cmnt + "  - the parcel/supply data can be used for *.cds, *.ipy, and *.wer files");
		out.println(cmnt);
		out.println(cmnt + "  Model ID - StateCU location and node type");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  LocId          :  CU Location ID surface water data");
		out.println(cmnt + "  LocType        :  Location type");
		out.println(cmnt + "                    - for StateMod, corresponds to network node type");
		out.println(cmnt + "                    - for StateCU, determined from supply for parcels");
		out.println(cmnt + "                    DIV - diversion");
		out.println(cmnt + "                    D&W - diversion & well");
		out.println(cmnt + "                    WEL - well");
		out.println(cmnt + "                    UNK - unknown, such as StateCU climate station dataset");
		out.println(cmnt + "  CollectionType :  Used if multiple data objects are combined under one model identifier");
		out.println(cmnt + "                    Single - single node (no collection)");
		out.println(cmnt + "                    Aggregate - aggregate physical properties (capacity, etc.) and water rights into classes");
		out.println(cmnt + "                    System - aggregate physical properties (capacity, etc.), maintain water rights");
		out.println(cmnt);
		out.println(cmnt + "  Parcel Data - GIS loaded into HydroBase");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  Year         :  Calendar year for parcel");
		out.println(cmnt + "  ParcelId     :  Parcel identifier");
		out.println(cmnt + "  Div          :  Water division (irrigated lands assessments are typically performed for a division)");
		out.println(cmnt + "  Dist         :  Water district");
		out.println(cmnt + "  Crop         :  Crop type for parcel (1 crop per parcel)");
		out.println(cmnt + "  ParcelArea   :  Parcel area for crop");
		out.println(cmnt + "  Units        :  Area units");
		out.println(cmnt + "  IrrigMeth    :  Irrigation method");
		out.println(cmnt);
		out.println(cmnt + "  Data Source/Use - whether or not the row of data is included in the CDS and IPY files");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  Include in CDS :  Indicates whether the parcel is included in CDS file acreage for the LocId.");
		out.println(cmnt + "                    This is set after running 'ReadCropPatternTSFromParcels' and");
		out.println(cmnt + "                    'ReadIrrigationPracticeTSFromParcels' commands.");
		out.println(cmnt + "                    CDS:YES = include parcel area in CDS and IPY files based on irrigated acreage parcel assignment");
		out.println(cmnt + "                    - YES if a parcel with surface water supply - area for parcel is always included");
		out.println(cmnt + "                    - YES if a parcel with only groundwater supply and model node is a WEL");
		out.println(cmnt + "                    - a parcel can have CDS:YES for surface water supplies or groundwater supplies, but not both");
		out.println(cmnt + "                    - IMPORTANT: a parcel/supply may show CDS:YES for surface water supply under GW-only model node");
		out.println(cmnt + "                      but this is only shown in verbose report mode - the supply is only included in the D&W node.");
		out.println(cmnt + "                    CDS:NO = do not include parcel area in CDS or IPY files based on irrigated acreage parcel assignment");
		out.println(cmnt + "                    - NO if GW supply but parcel also has surface water supply");
		out.println(cmnt + "                      - if GW only suppply WEL node, a D&W model node will include the area");
		out.println(cmnt + "                    CDS:NA = well supply that is NOT included in the dataset");
		out.println(cmnt + "                    - for example for WEL (well only) model node, a supply ID is not in the collection list.");
		out.println(cmnt + "                    - included in count that is used to compute area fraction, but not included in CDS area.");
		out.println(cmnt + "                    - not included in any IPY acreage.");
		out.println(cmnt + "                    CDS:ERR = error determining whether to include parcel area in CDS and IPY files (input error)");
		out.println(cmnt + "                    CDS:UNK = unknown whether to include parcel area in CDS and IPY files (should not happen)");
		out.println(cmnt + "                    - will have this value until crop pattern time series are processed by");
		out.println(cmnt + "                      ReadCropPatternTSFromParcels/SetCropPatternTSFromParcels (CDS file)");
		out.println(cmnt + "                      ReadIrrigationPracitceTSFromParcels/SetIrrigationPracticeTSFromParcels (IPY file)");
		out.println(cmnt + "                      commands for location");
		out.println(cmnt + "                    - a year may have this value if Read and Set commands do not overlap a year with irrigated lands");
		out.println(cmnt + "  DataSrc        :  Data source for the supply data");
		out.println(cmnt + "                    - typically from HydroBase but may enable user-supplied data");
		out.println(cmnt + "                    - may in the future be read directly from GIS or other files");
		out.println(cmnt + "                    HB-PUTS = HydroBase ParcelUseTS/Structure from vw_CDSS_ParcelUseTSStructureToParcel (diversions)" );
		out.println(cmnt + "                    HB-WTP = HydroBase Well/Parcel from vw_CDSS_WellsWellToParcel (wells)" );
		out.println(cmnt + "  CDS LocId      :  The StateCU location (or StateMod) ID where the parcel area is counted for CDS file. ");
		out.println(cmnt + "                    - DIV or D&W identifier if parcel has surface water supply. ");
		out.println(cmnt + "                    - WEL identifier if parcel has groundwater supply only. ");
		out.println(cmnt + "                    - ??? if the parcel has not been associated with a model location via ReadCropPatternTSFromParcels, ");
		out.println(cmnt + "                      such as when SetCropPatternTS*() commands are used to assign data at the end of processing.");
		out.println(cmnt + "                      When processing IPY, the ReadIrrigationPracticeTSFromParcels and SetIrrigationPracticeTS*() commands are used.");
		out.println(cmnt + "  CDS LocId Type :  Type node type for CDS LocId, to confirm data processing. ");
		out.println(cmnt + "                    - should match the model node location type");
		out.println(cmnt + "                    - ??? if the parcel has not been associated with a model location via ReadCropPatternTSFromParcels(), ");
		out.println(cmnt + "                      such as when SetCropPatternTS*() commands are used to assign data at the end of processing.");
		out.println(cmnt + "                      When processing IPY, the ReadIrrigationPracticeTSFromParcels and SetIrrigationPracticeTS*() commands are used.");
		out.println(cmnt + "  LocId has Set/Fill  :  Whether LocId has a SetCropPatternTS*() or FillCropPatternTS*() command");
		out.println(cmnt + "                         - SET:CDS if SetCropPatternTS*() command was used to set CDS acreage. ");
		out.println(cmnt + "                         - SET:IPY if SetIrrigationPracticeTS*() command was used to set IPY acreage. ");
		out.println(cmnt + "                         - FILL:CDS if FillCropPatternTS*() command was used to fill CDS acreage. ");
		out.println(cmnt + "                         - FILL:IPY if FillIrrigationPracticeTS*() command was used to fill IPY acreage. ");
		out.println(cmnt + "                         - set commands will override any parcel data in this file in output. ");
		out.println(cmnt + "                         - if CDS:UNK, LocId CDS data may be set by one or more StateDMI set or fill commands. ");
		out.println(cmnt);
		out.println(cmnt + "  SW Collection Data - surface water aggregate/system data");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  SW Collect WDID :  The WDID of the diversion collection part.");
		out.println(cmnt + "                     - If a WEL, blank (see the LocId for the location WDID).");
		out.println(cmnt + "                     - If a D&W, the collection part ID, mus be a WDID.");
		out.println(cmnt);
		out.println(cmnt + "  SW Supply Data - portion of parcel acreage associated with surface water supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  #Dit         :  Number of ditches that are associated with the parcel.");
		out.println(cmnt + "  WDID         :  Surface water supply ditch WDID.");
		out.println(cmnt + "  Irrig Frac   :  1/#Ditch = fraction of ParcelArea (from above) that is irrigated by the ditch (0.0 to 1.0).");
		out.println(cmnt + "  Irrig FracHB :  SWFrac from HydroBase, should match SWFrac.");
		out.println(cmnt + "  Irrig Area   :  ParcelArea * %Irrig = area irrigated by surface water supply for this ditch.");
		out.println(cmnt + "  HBError      :  Indicates whether the SWFrac computed from data is different than SWFracHB from HydroBase");
		out.println(cmnt + "                   ERROR - indicates that not all supplies in HydroBase parcel data are being modeled");
		out.println(cmnt + "                           due to different number of ditches associated with the parcel.");
		out.println(cmnt + "                           Checks are made to precision of 1 digit.");
		out.println(cmnt + "                           The CheckParcels() command can be used to check to a specified precision.");
		out.println(cmnt + "                           These errors need to be fixed to ensure the integrity of the dataset.");
		out.println(cmnt + "                   Blank indicates that model dataset and HydroBase data agree.");
		out.println(cmnt);
		out.println(cmnt + "  GW Collection Data - groundwater aggregate/system data");
		out.println(cmnt + "    - the collection part ID (WDID or receipt) is the same as the parcel supply ID");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  GWPart Type  :  Water supply part type (Well or Parcel, the latter being phased out).");
		out.println(cmnt + "                  WellInDitch = indicates a collection of ditches (D&W)");
		out.println(cmnt + "                  - associated wells are determined automatically based well -> parcel -> ditch relationship.");
		out.println(cmnt + "                  - the assoicated ditch is either the locId if single ditch or see previous SW supply line.");
		out.println(cmnt + "                  Well = indicates a collection of wells (WEL) specified using well identifiers.");
		out.println(cmnt + "                  Parcel = indicates a collection of wells (WEL) specified using parcel identifiers.");
		out.println(cmnt + "  GWPart IdType:  Water supply part ID type (WDID or RECEIPT).");
		out.println(cmnt + "                  If GWPartType=Well:");
		out.println(cmnt + "                     WDID - supply well has a WDID");
		out.println(cmnt + "                     RECEIPT - supply well has a well permit receipt for identifier.");
		out.println(cmnt + "                  If GWPartType=Parcel:");
		out.println(cmnt + "                     Parcel - parcel identifier, THIS APPROACH IS BEING PHASED OUT");
		out.println(cmnt + "  WDID         :  WDID for part if GWPartIdType=WDID.");
		out.println(cmnt + "  Receipt      :  Receipt for part if GWPartIdType=RECEIPT.");
		out.println(cmnt);
		out.println(cmnt + "  GW Supply Data - portion of parcel acreage associated with groundwater supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  #Wells       :  Number of wells that are associated with ParcelId.");
		out.println(cmnt + "  Irrig Frac   :  1/#Wells = fraction of ParcelArea (from above) that is irrigated by the well (0.0 to 1.0).");
		out.println(cmnt + "  D&W Frac     :  Sum of 'SW Supply Data' Irrig Frac for supplies where CDS LocId matches the model ID,");
		out.println(cmnt + "               :  applied when well supply is supplemental to ditch.");
		out.println(cmnt + "  Irrig Area   :  ParcelArea * Irrig Frac (* D&W Frac), zero if parcel has surface water supply for D&W node.");
		out.println(cmnt);
		out.println(cmnt + "-------- Model Id ---------|------------------------------- Parcel Data -----------------------------||----------- Data Source/Use ------------- ||            Collections use WDID Parts              |          WEL Collection Part ID is the same as GW Supply ID       |");
		out.println(cmnt + "                           |                                                                         || Include                    CDS    LocId  || SW     |-------------- SW Supply Data -------------|----------- GW Collection Data ---------|----- GW Supply Data -----|");
		out.println(cmnt + "           Loc  Collection |       Parcel                                    Parcel          Irrig   || in               CDS       LocId  has    || Collect|#     Ditch  Irrig Irrig   Irrig           |   GWPart    GWPart    Well     Well    |#    Irrig D&W     Irrig  |");
		out.println(cmnt + "  LocId    Type Type       |Year   ID        Div Dist        Crop            Area     Units  Method  || CDS?    DataSrc  LocId     Type   Fill/Set| WDID   |Dit   WDID   Frac  FracHB  Area     HBError|    Type     IdType    WDID     Receipt |Well Frac  Frac    Area   |");
		out.println(cmnt + "b--------exb--exb---------exb--exb--------exb--exb--exb------------------exb--------exb--exb--------exb------exb------exb----------exb--exb------exb------exb-exb------exb---exb---exb--------exb-----exb----------exb-----exb------exb--------exb--exb---exb---exb-------ex");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		StateCU_SupplyFromSW supplyFromSW = null;
		StateCU_SupplyFromGW supplyFromGW = null;

		// Loop through the locations.
		List<StateCU_Parcel> parcelListSorted = new ArrayList<>();
		StateCU_Parcel_Comparator parcelComparator = new StateCU_Parcel_Comparator(StateCU_Parcel_Comparator.YEAR_PARCELID);
		Message.printStatus(2, routine, "Processing " + culocList.size() + " CU locations.");
		for ( StateCU_Location culoc : culocList ) {

			// Loop through the parcels for the location:
			// - first sort to make sure the output is as expected
			parcelListSorted.clear();
			parcelListSorted.addAll(culoc.getParcelList());
			Collections.sort(parcelListSorted, parcelComparator);

			Message.printStatus(2, routine, "Processing CU location \"" + culoc.getID() + "\", number of parcels = " + parcelListSorted.size() );

			for ( StateCU_Parcel parcel : parcelListSorted ) {
				if (parcel == null) {
					continue;
				}
				// Make sure that the parcel's data is current:
				// - TODO smalers 2020-11-05 should happen automatically when values are requested
				parcel.recompute();

				// Skip the parcel output entirely if a groundwater only node and the parcel has surface water supply:
				// - in this case the parcel will have been counted with the D&W and not this WEL node
				if ( !verbose ) {
					// Not verbose so need to limit the output to only parcels
					if ( culoc.isGroundwaterOnlySupplyModelNode() && parcel.hasSurfaceWaterSupply() ) {
						continue;
					}
				}

				// line 1 - model node and parcel information.
				objectList.clear();
				// Model node.
				objectList.add(culoc.getID());
				objectList.add("" + culoc.getLocationType());
				if ( culoc.isCollection() ) {
					objectList.add("" + culoc.getCollectionType());
				}
				else {
					objectList.add("Single");
				}
				// Parcel.
				objectList.add(new Integer(parcel.getYear()));
				objectList.add(parcel.getID());
				objectList.add(parcel.getDiv());
				objectList.add(parcel.getWD());
				objectList.add(parcel.getCrop());
				objectList.add(new Double(parcel.getArea()));
				objectList.add(parcel.getAreaUnits());
				objectList.add(parcel.getIrrigationMethod());
				// Don't know whether CDS or IPY is being processed so check each:
				// - command checks allow only one of CDS or IPY processing from parcels
				// - handle separately so can troubleshoot which is set
				if ( culoc.hasSetCropPatternTSCommands(parcel.getYear()) ) {
					// A SetCropPatternTS command was used for the location to set area.
					//objectList.add("SET:YES");
					objectList.add("SET:CDS");
				}
				else if ( culoc.hasSetIrrigationPracticeTSCommands(parcel.getYear()) ) {
					// A SetCropPatternTS command was used for the location to set area.
					//objectList.add("SET:YES");
					objectList.add("SET:IPY");
				}
				else if ( culoc.hasFillCropPatternTSCommands(parcel.getYear()) ) {
					// A FillCropPatternTS command was used for the location to set area.
					objectList.add("FILL:CDS");
				}
				else if ( culoc.hasFillIrrigationPracticeTSCommands(parcel.getYear()) ) {
					// A FillIrrigationPracticeTS command was used for the location to set area.
					objectList.add("FILL:IPY");
				}
				else {
					objectList.add("");
				}

				printLine = StringUtil.formatString(objectList, format_1);
				out.println(printLine);

				// line 2 - surface supply information.

				for ( StateCU_Supply supply : parcel.getSupplyList() ) {
					if ( supply instanceof StateCU_SupplyFromSW ) {
						supplyFromSW = (StateCU_SupplyFromSW)supply;
						objectList.clear();
						objectList.add("CDS:" + supplyFromSW.getIncludeParcelInCdsType());
						objectList.add(supplyFromSW.getDataSource());
						if ( supplyFromSW.getStateCULocationForCds() == null ) {
					    	if ( supplyFromSW.getIncludeParcelInCdsType() == IncludeParcelInCdsType.NO ) {
					    		objectList.add("");
						   		objectList.add("");
					    	}
					    	else {
					    		objectList.add("???");
						   		objectList.add("???");
					    	}
						}
						else {
							objectList.add(supplyFromSW.getStateCULocationForCds().getID());
							objectList.add("" + supplyFromSW.getStateCULocationForCds().getLocationType());
						}
						//objectList.add(supplyFromSW.getCollectionPartType());
						//objectList.add(supplyFromSW.getCollectionPartIdType());
						if ( culoc.isCollection() ) {
							// Collection so output the collection part ID.
							objectList.add(supplyFromSW.getCollectionPartId());
						}
						else {
							// Single ditch so leave the collection WDID blank.
							objectList.add("");
						}
						objectList.add(new Integer(parcel.getSupplyFromSWCount()));
						objectList.add(supplyFromSW.getWDID());
						objectList.add(new Double(supplyFromSW.getAreaIrrigFraction()));
						objectList.add(new Double(supplyFromSW.getAreaIrrigFractionHydroBase()));
						objectList.add(new Double(supplyFromSW.getAreaIrrig()));
						objectList.add(supplyFromSW.getAreaIrrigFractionHydroBaseError());
						printLine = StringUtil.formatString(objectList, format_2);
						out.println(printLine);
					}
				}

				// line 3 - groundwater supply information:
				// - only output if is a parcel that is groundwater only because
				//   parcels with surface water are counted under D&W
				// - this is checked at the top of the loop

				for ( StateCU_Supply supply : parcel.getSupplyList() ) {
					if ( supply instanceof StateCU_SupplyFromGW ) {
						objectList.clear();
						supplyFromGW = (StateCU_SupplyFromGW)supply;
						//objectList.add(supply.getDataSource());
						objectList.add("CDS:" + supplyFromGW.getIncludeParcelInCdsType());
						objectList.add(supplyFromGW.getDataSource());
						if ( supplyFromGW.getStateCULocationForCds() == null ) {
						    if ( (supplyFromGW.getIncludeParcelInCdsType() == IncludeParcelInCdsType.NO) ||
						    	(supplyFromGW.getIncludeParcelInCdsType() == IncludeParcelInCdsType.NOT_MODELED) ) {
						    	// Case where D&W and parcel supply area is included in D&W location (not WEL location) or not modeled.
						    	objectList.add("");
							   	objectList.add("");
						    }
						    else {
						    	// Should not typically occur?
						    	objectList.add("???");
							   	objectList.add("???");
						    }
						}
						else {
							objectList.add(supplyFromGW.getStateCULocationForCds().getID());
							objectList.add("" + supplyFromGW.getStateCULocationForCds().getLocationType());
						}
						objectList.add(supplyFromGW.getCollectionPartType());
						objectList.add(supplyFromGW.getCollectionPartIdType());
						objectList.add(supplyFromGW.getWDID());
						objectList.add(supplyFromGW.getReceipt());
						objectList.add(new Integer(parcel.getSupplyFromGWCount()));
						objectList.add(new Double(1.0/parcel.getSupplyFromGWCount()));
						// DW fraction is used in IPY calculations.
						double dwFraction = 1.0;
						if ( parcel.getSupplyFromSWCount() > 0 ) {
							// Format here as a string because can be a blank string if not a D&W.
							//objectList.add(String.format("%5.3f", (1.0/parcel.getSupplyFromSWCount())));
							// TODO smalers 2021-02-28 ditch multiplier is not used since well supply is only once regardless of # of ditches.
							//objectList.add(String.format("%5.3f", 1.0));
							dwFraction = parcel.getSupplyFromSWFraction(culoc.getID());
							objectList.add(String.format("%5.3f",dwFraction));
						}
						else {
							objectList.add("");
						}
						// The area does not consider the D&W surface water split:
						// - multiply by the additional fraction
						objectList.add(new Double(supplyFromGW.getAreaIrrig()*dwFraction));
						printLine = StringUtil.formatString(objectList, format_3);
						out.println(printLine);
					}
					else if ( !(supply instanceof StateCU_SupplyFromSW) ) {
						// Not surface water or groundwater so an error:
						// - code error that will need to be fixed
						throw new RuntimeException("Supply type not handled - need to check code.");
					}
				}
			}
		}
	}
	catch (Exception e) {
		throw e;
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

/**
 * Write the parcel data to a "parcel supply" text file, using StateCU_Parcel as input.
 * This format closely matches the irrigated lands files.
 * @param outputFileFull full path to output file
 * @param delimiter not currently used, may remove
 * @param update whether to update or overwrite the file, used when creating the file header
 * @param parcelListOrig list of StateCU_Parcel to output, sort order is not assumed
 * @param outputCommentList additional comments for the file header
 */
private void writeParcelsToParcelSupplyFile ( String outputFileFull, String delimiter, boolean update,
	List<StateCU_Parcel> parcelListOrig, List<String> outputCommentsList ) {

	List<String> newComments = new ArrayList<>();
	List<String> commentIndicators = new ArrayList<>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new ArrayList<>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	try {
		// Overwrite the file.
		out = IOUtil.processFileHeaders(
			null, outputFileFull,
			//IOUtil.getPathUsingWorkingDir(instrfile),
			//IOUtil.getPathUsingWorkingDir(outstrfile),
			newComments, commentIndicators, ignoredCommentIndicators, 0);

		String printLine = null;
		String cmnt = "#>";
		String format_1 = "%6d %4d %4d %15.15s %-20.20s %10.3f %-4.4s %-10.10s"; // When no general parcel error.
		String format_1_error = format_1 + // When have a general parcel error.
			"                                                      " + // Skip surface supply data.
			"                                                                       %s"; // Skip groundwater supply and output general error.

		// Surface water fields - space on the left to skip over above formatting
		String format_2 = "                                                                                 " + // General parcel data.
			"%-10.10s %6d %6.3f %8.3f %11.3f %-7.7s";
		String format_2_error = format_2 +
			"                                                                     %s"; // Skip groundwater supply and output general error.

		// Groundwater fields - data source and include align with  surface water and then space over to groundwater columns.
		String format_3 = "                                                                                                                                       " + // Skip general parcel and surface supply data.
			"%-10.10s %-10.10s %-12.12s %6d %6.3f %6.6s %11.3f";
		String format_3_error = format_3 + " %s"; // Groundwater supply data and general error.

		// Size to largest size.
		List<Object> objectList = new ArrayList<>(8);

		out.println(cmnt);
		out.println(cmnt + " ***************************************************************************************************");
		out.println(cmnt + "  StateDMI Parcel / Supply File - this is a diagnostics report");
		out.println(cmnt + "  - this report lists each parcel and supplies for the parcel sorted by:");
		out.println(cmnt + "      year");
		out.println(cmnt + "      division");
		out.println(cmnt + "      district");
		out.println(cmnt + "      parcel ID");
		out.println(cmnt + "  - the parcel data should match orginal GIS irrigated land and supply data");
		out.println(cmnt + "  - model data reflect model location identifiers and collections");
		out.println(cmnt);
		out.println(cmnt + "  Parcel Data - GIS loaded into HydroBase, sorted by Year, Div, Dist, ParcelId");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  Year         :  Calendar year for parcel");
		out.println(cmnt + "  Div          :  Water division");
		out.println(cmnt + "  Dist         :  Water district - may be zero depending on how parcel data are read");
		out.println(cmnt + "                    Current HydroBase design includes WD in digits 2-3 of the parcel ID");
		out.println(cmnt + "  Parcel Id    :  Parcel identifier");
		out.println(cmnt + "  Crop         :  Crop type for parcel (1 crop per parcel)");
		out.println(cmnt + "  Parcel Area  :  Parcel area for crop");
		out.println(cmnt + "  Units        :  Area units");
		out.println(cmnt + "  Irrig Method :  Irrigation method");
		out.println(cmnt);
		out.println(cmnt + "  SW Supply Data - portion of parcel acreage associated with surface water supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  WDID         :  Water district identifier for the ditch.");
		out.println(cmnt + "  #Ditch       :  Number of ditches that are associated with the parcel.");
		out.println(cmnt + "  Irrig Frac   :  1/#Ditch = fraction of ParcelArea (from above) that is irrigated by the ditch (0.0 to 1.0).");
		out.println(cmnt + "  Irrig FracHB :  SWFrac from HydroBase, should match SWFrac if number of surface water supplies matches.");
		out.println(cmnt + "               :  Will be missing if parcels were read using ReadParcelsFromIrrigatedLands command.");
		out.println(cmnt + "  Irrig Area   :  ParcelArea * %Irrig = area irrigated by surface water supply for this ditch.");
		out.println(cmnt + "  HBError      :  Indicates whether the SWFrac computed from data is different than SWFracHB from HydroBase");
		out.println(cmnt + "                   ERROR - indicates that not all supplies in HydroBase parcel data are being modeled");
		out.println(cmnt + "                           due to different number of ditches associated with the parcel.");
		out.println(cmnt + "                           Checks are made to precision of 1 digit.");
		out.println(cmnt + "                           The CheckParcels() command can be used to check to a specified precision.");
		out.println(cmnt + "                           These errors need to be fixed to ensure the integrity of the dataset.");
		out.println(cmnt + "                   Blank indicates that model dataset and HydroBase data agree.");
		out.println(cmnt + "                   - Will also be blank if parcels were read using ReadParcelsFromIrrigatedLands command.");
		out.println(cmnt);
		out.println(cmnt + "  GW Supply Data - portion of parcel acreage associated with groundwater supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  IdType       :  Water supply part ID type (WDID or RECEIPT).");
		out.println(cmnt + "  WDID         :  WDID for supply well.");
		out.println(cmnt + "  RECEIPT      :  Well permit receipt.");
		out.println(cmnt + "  #Wells       :  Number of wells that are associated with ParcelId.");
		out.println(cmnt + "  Irrig Frac   :  1/#Wells = fraction of ParcelArea (from above) that is irrigated by the ditch (0.0 to 1.0).");
		out.println(cmnt + "                    - not in original data (calculated when loaded into HydroBase).");
		out.println(cmnt + "  D&W Frac     :  Same as surface water Irrig Frac, applied when well supply is supplemental to ditch.");
		out.println(cmnt + "  Irrig Area   :  ParcelArea/#Wells, zero if area is already assigned to surface water ID for D&W node.");
		out.println(cmnt);
		out.println(cmnt + "  Error        :  General error - for troubleshooting");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "               :  Will be used for parcel and supply report output lines if there are processing errors.");
		out.println(cmnt);
		out.println(cmnt + "----------------------------------- Parcel Data ------------------------------|-------------------- SW Suppply ---------------------|--------------------------- GW Supply Data ------------------------|-- Error --");
		out.println(cmnt + "                                                      Parcel          Irrig   |                   Irrig  Irrig      Irrig           |                                          Irrig   D&W     Irrig    |");
		out.println(cmnt + "Year  Div Dist   ParcelId              Crop           Area     Units  Method  |    WDID   #Ditch  Frac   FracHB     Area     HBError|  ID Type     WDID       Receipt   #Wells Frac    Frac    Area     |");
		out.println(cmnt + "b--exb--exb--exb-------------exb------------------exb--------exb--exb--------exb--------exb----exb----exb------exb---------exb-----exb--------exb--------exb----------exb----exb----exb----exb---------exb----------");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		// Create a new  parcel list and sort by year, division, district, and parcel ID:
		// - don't need to copy the parcel instances, just make a new list
		List<StateCU_Parcel> parcelList = new ArrayList<>();
		for ( StateCU_Parcel parcel : parcelListOrig ) {
			parcelList.add(parcel);
		}
		// Sort using the comparator.
		Collections.sort(parcelList, new StateCU_Parcel_Comparator(StateCU_Parcel_Comparator.YEAR_DIVISION_DISTRICT_PARCELID) );

		StateCU_SupplyFromSW supplyFromSW = null;
		StateCU_SupplyFromGW supplyFromGW = null;
		// Process all of the parcels.
		for ( StateCU_Parcel parcel : parcelList ) {
			if (parcel == null) {
				continue;
			}

			// line 1 - parcel information.
			objectList.clear();
			objectList.add(new Integer(parcel.getYear()));
			objectList.add(new Integer(parcel.getDiv()));
			objectList.add(new Integer(parcel.getWD()));
			objectList.add(new Integer(parcel.getID()));
			objectList.add(parcel.getCrop());
			objectList.add(new Double(parcel.getArea()));
			objectList.add(parcel.getAreaUnits());
			objectList.add(parcel.getIrrigationMethod());
			if ( parcel.getError().length() > 0 ) {
				// Output the parcel error at the end.
				objectList.add("ERROR: " + parcel.getError());
				printLine = StringUtil.formatString(objectList, format_1_error);
			}
			else {
				// No parcel error so don't output a bunch of whitespace.
				printLine = StringUtil.formatString(objectList, format_1);
			}
			out.println(printLine);

			// line 2+ - supply information:
			// - list surface water supplies first, then groundwater

			for ( StateCU_Supply supply : parcel.getSupplyList() ) {
				if ( supply instanceof StateCU_SupplyFromSW ) {
					supplyFromSW = (StateCU_SupplyFromSW)supply;
					objectList.clear();
					objectList.add(supplyFromSW.getWDID());
					objectList.add(new Integer(parcel.getSupplyFromSWCount()));
					objectList.add(new Double(supplyFromSW.getAreaIrrigFraction()));
					objectList.add(new Double(supplyFromSW.getAreaIrrigFractionHydroBase()));
					objectList.add(new Double(supplyFromSW.getAreaIrrig()));
					if ( supplyFromSW.getAreaIrrigFractionHydroBase() >= 0.0 ) {
						// Have HydroBase fraction from ReadParcelsFromHydroBase.
						objectList.add(supplyFromSW.getAreaIrrigFractionHydroBaseError());
					}
					else {
						// Do not have HydroBase fraction from ReadParcelsFromIrrigatedLands.
						objectList.add("");
					}
					// Only show as an error if HydroBase fraction is available:
					// - won't be available if parcels were read from irrigated lands using
					//   ReadParcelsFromIrrigatedLands command
					if ( supplyFromSW.getError().length() > 0 ) {
						// Output the parcel error at the end.
						objectList.add("ERROR: " + supplyFromSW.getError());
						printLine = StringUtil.formatString(objectList, format_2_error);
					}
					else {
						// No parcel error so don't output a bunch of whitespace.
						printLine = StringUtil.formatString(objectList, format_2);
					}
					out.println(printLine);
				}
			}
			for ( StateCU_Supply supply : parcel.getSupplyList() ) {
				if ( supply instanceof StateCU_SupplyFromGW ) {
					objectList.clear();
					supplyFromGW = (StateCU_SupplyFromGW)supply;
					objectList.add(supplyFromGW.getCollectionPartIdType());
					objectList.add(supplyFromGW.getWDID());
					objectList.add(supplyFromGW.getReceipt());
					objectList.add(new Integer(parcel.getSupplyFromGWCount()));
					objectList.add(new Double(1.0/parcel.getSupplyFromGWCount()));
					if ( parcel.getSupplyFromSWCount() > 0 ) {
						// Format here as a string because can be a blank string if not a D&W.
						objectList.add(String.format("%6.3f", (1.0/parcel.getSupplyFromSWCount())));
					}
					else {
						objectList.add("");
					}
					objectList.add(new Double(supplyFromGW.getAreaIrrig()));
					if ( supplyFromGW.getError().length() > 0 ) {
						// Output the parcel error at the end.
						objectList.add("ERROR: " + supplyFromGW.getError());
						printLine = StringUtil.formatString(objectList, format_3_error);
					}
					else {
						// No parcel error so don't output a bunch of whitespace.
						printLine = StringUtil.formatString(objectList, format_3);
					}
					out.println(printLine);
				}
			}
		}
	}
	catch (Exception e) {
		throw e;
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

}