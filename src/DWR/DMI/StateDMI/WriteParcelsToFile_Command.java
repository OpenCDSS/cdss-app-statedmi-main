// WriteParcelsToFile_Command - This class initializes, checks, and runs the Write*ParcelsToFile() commands.

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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import DWR.StateCU.StateCU_CropPatternTS;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Parcel;
import DWR.StateCU.StateCU_Supply;
import DWR.StateCU.StateCU_SupplyFromGW;
import DWR.StateCU.StateCU_SupplyFromSW;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.Command;
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
<p>
This class initializes, checks, and runs the Write*ParcelsToFile() commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the WriteCropPatternTSParcelsToFile()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
</p>
*/
public abstract class WriteParcelsToFile_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Possible values for the WriteHow parameter.
*/
protected final String _OverwriteFile = "OverwriteFile";
protected final String _UpdateFile = "UpdateFile";

/**
List of output files that are created by this command.
*/
private List<File> __OutputFile_List = null;
	
/**
Constructor.
*/
public WriteParcelsToFile_Command ()
{	super();
	setCommandName ( "Write?ParcelsToFile" );
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
{	String OutputFile = parameters.getValue ( "OutputFile" );
	String WriteHow = parameters.getValue ( "WriteHow" );
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
    
	if ( (WriteHow != null) && (WriteHow.length() != 0) &&
		!WriteHow.equals(_OverwriteFile) && !WriteHow.equals(_UpdateFile) ) {
        message = "The valie for WriteHow (" + WriteHow + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify WriteHow as " + _OverwriteFile + " (default) or " + _UpdateFile ) );
	}

	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(3);
	validList.add ( "OutputFile" );
	validList.add ( "WriteHow" );
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
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new WriteParcelsToFile_JDialog ( parent, this )).ok();
}

// Use parent parseCommand

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
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
protected List<File> getOutputFileList ()
{
	return __OutputFile_List;
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
    
	// Check whether the processor wants output files to be created...

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
    String WriteHow = parameters.getValue ( "WriteHow" );
    boolean update = false;
    if ( (WriteHow == null) || WriteHow.equals("") ) {
    	WriteHow = _OverwriteFile;	// Default
    }
    else if ( WriteHow.equalsIgnoreCase(_UpdateFile) ) {
    	update = true;
    }
    String Delimiter = parameters.getValue ( "Delimiter" );
    if ( (Delimiter == null) || Delimiter.equals("") ) {
    	Delimiter = ","; // Default
    }

    String OutputFile_full = OutputFile;
    try {
        // Get the comments to add to the top of the file.

        List<String> OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
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
        
    	// Clear the filename for the FileGenerator interface
    	setOutputFileList ( null );
    	List<File> outputFileList = null; // If non-null below, then a method returned the list of files.
    	OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );
        Message.printStatus ( 2, routine, "Writing list file \"" + OutputFile_full + "\"" );

        // StateCU components
        
		if ( this instanceof WriteCropPatternTSParcelsToFile_Command ) {
			writeCropPatternTSParcelsToTextFile(OutputFile_full, Delimiter, update,
				processor.getStateCUCropPatternTSList(), OutputComments_List );
		}
		else if ( this instanceof WriteCULocationParcelsToFile_Command ) {
			writeCULocationParcelsToTextFile(OutputFile_full, Delimiter, update,
				processor.getStateCULocationList(), OutputComments_List );
		}
			
    	// Set the filename(s) for the FileGenerator interface
    	if ( outputFileList == null ) {
    		// Create a list with a single file
    		outputFileList = new ArrayList<>(1);
    		outputFileList.add ( new File(OutputFile_full) );
    	} // Otherwise the write method returned the list of filenames for output files
    	setOutputFileList ( outputFileList );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
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
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFileList ( List<File> outputFileList )
{
	__OutputFile_List = outputFileList;
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

	String OutputFile = parameters.getValue ( "OutputFile" );
	String WriteHow = parameters.getValue ( "WriteHow" );
	String Delimiter = parameters.getValue ( "Delimiter" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (WriteHow != null) && (WriteHow.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WriteHow=" + WriteHow );
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
 */
private void writeCULocationParcelsToTextFile ( String outputFileFull, String delimiter, boolean update,
	List<StateCU_Location> culocationList, List<String> outputCommentsList ) {
	// Get the parcels for all the crop pattern TS
	List<StateCU_Parcel> parcelList = new ArrayList<>();
	for ( StateCU_Location cpts : culocationList ) {
		parcelList.addAll(cpts.getParcelList());
	}
	
	// Call the general write method
	writeParcelsToTextFile ( outputFileFull, delimiter, update, parcelList, outputCommentsList );
}

/**
 * Write the parcel data as a text file, using StateCU_CropPatternTS as input.
 */
private void writeCropPatternTSParcelsToTextFile ( String outputFileFull, String delimiter, boolean update,
	List<StateCU_CropPatternTS> cropPatternTSList, List<String> outputCommentsList ) {
	// Get the parcels for all the crop pattern TS
	List<StateCU_Parcel> parcelList = new ArrayList<>();
	for ( StateCU_CropPatternTS cpts : cropPatternTSList ) {
		parcelList.addAll(cpts.getParcelList());
	}
	
	// Call the general write method
	writeParcelsToTextFile ( outputFileFull, delimiter, update, parcelList, outputCommentsList );
}

/**
 * Write the parcel data as a text file, using StateCU_CropPatternTS as input.
 */
private void writeParcelsToTextFile ( String outputFileFull, String delimiter, boolean update,
	List<StateCU_Parcel> parcelList, List<String> outputCommentsList ) {

	List<String> newComments = new ArrayList<>();
	List<String> commentIndicators = new ArrayList<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new ArrayList<String>(1);
	ignoredCommentIndicators.add ( "#>");
	PrintWriter out = null;
	try {
		// Overwrite the file
		out = IOUtil.processFileHeaders(
			null, outputFileFull,
			//IOUtil.getPathUsingWorkingDir(instrfile),
			//IOUtil.getPathUsingWorkingDir(outstrfile),
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String printLine = null;
		String cmnt = "#>";
		String format_1 = "%-12.12s %-8.8s %4d %-20.20s %10.3f %-4.4s %-10.10s %15.15s %-10.10s";

		// Surface water fields - space on the left to skip over above formatting
		String format_2 = "                                                                                           " +
			"%-10.10s %-8.8s %-10.10s %-8.8s %-8.8s %8.3f %10.3f";

		// Groundwater fields - data source and include align with  surface water and then space over to groundwater columns
		String format_3 = "                                                                                           %-10.10s %-8.8s                                        " +
			"          %-12.12s %-10.10s %-8.8s %-10.10s %8d %10.3f";

		// Size to largest size
		List<Object> objectList = new ArrayList<>(8);
	
		out.println(cmnt);
		out.println(cmnt + " ***************************************************************************************************");
		out.println(cmnt + "  StateDMI CU Location Parcel File - this is a diagnostics report");
		out.println(cmnt + "  - this report shows the relationships between a parcel and supplies for the parcel.");
		out.println(cmnt + "  - the underlying data can be used for *.cds, *.ipy, and *.wer files.");
		out.println(cmnt);
		out.println(cmnt + "  Model ID - StateCU location and node type");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  LocId        :  CU Location ID surface water data");
		out.println(cmnt + "  LocType      :  Location type for StateMod");
		out.println(cmnt + "                  DIV - diversion");
		out.println(cmnt + "                  D&W - diversion & well");
		out.println(cmnt + "                  WEL - well");
		out.println(cmnt + "                  ??? - unknown because StateCU locations don't use node type");
		out.println(cmnt);
		out.println(cmnt + "  Parcel Data - GIS loaded into HydroBase");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  Year         :  Calendar year for parcel");
		out.println(cmnt + "  Crop         :  Crop type for parcel (1 crop per parcel)");
		out.println(cmnt + "  ParcelArea   :  Parcel area for crop");
		out.println(cmnt + "  Units        :  Area units");
		out.println(cmnt + "  IrrigMeth    :  Irrigation method");
		out.println(cmnt + "  ParcelId     :  Parcel identifier");
		out.println(cmnt);
		out.println(cmnt + "  Whether or not the row of data is included - need to expand this to indicate how included or not");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  DataSrc      :  Data source (typically from HydroBase but may enable user-supplied data).");
		out.println(cmnt + "                  HB-PUTS = HydroBase ParcelUseTS/Structure from vw_CDSS_ParcelUseTSStructureToParcel" );
		out.println(cmnt + "                  HB-WTP = HydroBase Well/Parcel from vw_CDSS_WellsWellToParcel" );
		out.println(cmnt + "  CDS?         :  Indicates whether the parcel is included in CDS file acreage.");
		out.println(cmnt + "                  CDS:YES = include in CDS area for the location");
		out.println(cmnt + "                  CDS:NO = do not include - for GW supply in D&W, will be included in the Div node acreage");
		out.println(cmnt);
		out.println(cmnt + "  SW Collection Data - surface water aggregate/system data");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  SWPartType   :  Surface water supply part type (only is allowed Ditch).");
		out.println(cmnt + "  SWPartIdType :  Surface water supply part ID type (only WDID is allowed).");
		out.println(cmnt);
		out.println(cmnt + "  SW Supply Data - portion of parcel acreage associated with surface water supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  %Irrig       :  Percent of ParcelArea (from above) that is irrigated by the ditch.");
		out.println(cmnt + "  SWIrrigArea  :  ParcelArea * %Irrig.");
		out.println(cmnt);
		out.println(cmnt + "  GW Collection Data - groundwater aggregate/system data");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  GWPartType   :  Water supply part type (Well or Parcel, the latter being phased out).");
		out.println(cmnt + "                  WellInDitch = indicates a collection of ditches, with associated wells determined automatically.");
		out.println(cmnt + "                  Well = indicates a collection of wells specified using well identifiers.");
		out.println(cmnt + "                  Parcel = indicates a collection of wells specified using parcel identifiers.");
		out.println(cmnt + "  GWPartIdType :  Water supply part ID type (WDID or RECEIPT).");
		out.println(cmnt + "                  If GWPartType=Well, WDID or RECEIPT.");
		out.println(cmnt + "                  If GWPartType=Parcel, Parcel.");
		out.println(cmnt + "  WDID         :  WDID of part if GWPartIdType=WDID.");
		out.println(cmnt + "  Receipt      :  Receipt of part if GWPartIdType=RECEIPT.");
		out.println(cmnt);
		out.println(cmnt + "  GW Supply Data - portion of parcel acreage associated with groundwater supply");
		out.println(cmnt + "  --------------------------------------------------------------------------------------------------");
		out.println(cmnt + "  #Wells       :  Number of wells that are associated with ParcelId.");
		out.println(cmnt + "  GWIrrigArea  :  ParcelArea/#Wells, zero if area is already assigned to surface water ID for D&W node.");
		out.println(cmnt);
		out.println(cmnt + "----- Model Id ----|----------------------------- Parcel Data --------------------------||                 ||---- SW Collection Data ----|- SW Suppply Data -|------------- GW Collection Data ----------|-- GW Supply Data -|");
		out.println(cmnt + "  LocId     LocType|Year        Crop          ParcelArea Units IrrigMeth    ParcelId    || DataSrc    CDS? ||SWPartType SWPartIdType WDID| %Irrig SWIrrigArea| GWPartType  GWPartIdType   WDID   Receipt | #Wells|GWIrrigArea|");
		out.println(cmnt + "b--------exb------exb--exb------------------exb--------exb--exb--------exb-------------exb--------exb------exb--------exb------exb------exb------exb--------exb----------exb--------exb------exb--------exb------exb--------e");
		out.println(cmnt + "EndHeader");
		out.println(cmnt);
	
		StateCU_SupplyFromSW supplyFromSW = null;
		StateCU_SupplyFromGW supplyFromGW = null;
		for ( StateCU_Parcel parcel : parcelList ) {
			if (parcel == null) {
				continue;
			}
	
			// line 1 - parcel information
			objectList.clear();
			objectList.add(parcel.getLocationId());
			objectList.add("???");
			objectList.add(new Integer(parcel.getYear()));
			objectList.add(parcel.getCrop());
			objectList.add(new Double(parcel.getArea()));
			objectList.add(parcel.getAreaUnits());
			objectList.add(parcel.getIrrigationMethod());
			objectList.add(parcel.getID());
			objectList.add(parcel.getDataSource());
			printLine = StringUtil.formatString(objectList, format_1);
			out.println(printLine);
			
			// line 2+ - supply information
			
			for ( StateCU_Supply supply : parcel.getSupplyList() ) {
				if ( supply instanceof StateCU_SupplyFromSW ) {
					supplyFromSW = (StateCU_SupplyFromSW)supply;
					objectList.clear();
					objectList.add(supplyFromSW.getDataSource());
					objectList.add("CDS:YES");
					objectList.add(supplyFromSW.getCollectionPartType());
					objectList.add(supplyFromSW.getCollectionPartIdType());
					objectList.add(supplyFromSW.getWDID());
					objectList.add(new Double(supplyFromSW.getAreaIrrigPercent()));
					objectList.add(new Double(supplyFromSW.getAreaIrrig()));
					printLine = StringUtil.formatString(objectList, format_2);
					out.println(printLine);
				}
				else if ( supply instanceof StateCU_SupplyFromGW ) {
					objectList.clear();
					supplyFromGW = (StateCU_SupplyFromGW)supply;
					objectList.add(supply.getDataSource());
					if ( supplyFromGW.getIncludeInCdsArea() ) {
						objectList.add("CDS:YES");
					}
					else {
						objectList.add("CDS:NO");
					}
					objectList.add(supplyFromGW.getCollectionPartType());
					objectList.add(supplyFromGW.getCollectionPartIdType());
					objectList.add(supplyFromGW.getWDID());
					objectList.add(supplyFromGW.getReceipt());
					objectList.add(new Integer(parcel.getSupplyFromGWCount()));
					objectList.add(new Double(supplyFromGW.getAreaIrrig()));
					printLine = StringUtil.formatString(objectList, format_3);
					out.println(printLine);
				}
				else {
					throw new RuntimeException("Supply type not handled - need to check code.");
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