// CheckIrrigatedLands_Command - This class initializes, checks, and runs the CheckIrrigatedLands() command.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2020 Colorado Department of Natural Resources

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.DMI.StateDMI.StateDMI_Processor;
import DWR.StateCU.StateCU_Supply;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;

/**
This class initializes, checks, and runs the CheckIrrigatedLands() command.
*/
public class CheckIrrigatedLands_Command extends AbstractCommand
{
    
	/**
	 * Used with IncludeParcelsWithNoSupply.
	 */
	public final String _False = "False";
	public final String _True = "True";

/**
Constructor.
*/
public CheckIrrigatedLands_Command ()
{	super();
	setCommandName ( "CheckIrrigatedLands" );
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
{	String TableID = parameters.getValue ( "TableID" );
	String IncludeParcelsWithNoSupply = parameters.getValue ( "IncludeParcelsWithNoSupply" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the table identifier." ) );
    }

	if ( (IncludeParcelsWithNoSupply != null) && (IncludeParcelsWithNoSupply.length() > 0) &&
		!IncludeParcelsWithNoSupply.equalsIgnoreCase(_False) && !IncludeParcelsWithNoSupply.equalsIgnoreCase(_True) ) {
		message = "The IncludeParcelsWithNoSupply value (" + IncludeParcelsWithNoSupply + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IncludeParcelsWithNoSupply as " + _False + " (default) or " + _True) );
	}
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(3);
    validList.add ( "TableID" );
    validList.add ( "ExcludeCrops" );
    validList.add ( "IncludeParcelsWithNoSupply" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );    

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (StateDMI_Processor)getCommandProcessor(), this);
    // The command will be modified if changed...
	return (new CheckIrrigatedLands_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
 * Evaluate whether a parcel crop type should be included.
 * For example, this can be used to skip 'NO_CROP' crop type.
 * @param cropType crop type to check
 * @param excludeCrops crop types to exclude
 * @return true if crop should be included, false if not
 */
private boolean includeCropType ( String cropType, String [] excludeCrops ) {
	for ( int i = 0; i < excludeCrops.length; i++ ) {
		if ( cropType.equalsIgnoreCase(excludeCrops[i]) ) {
			return false;
		}
	}
	return true;
}

// Use base class parseCommand()

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;

	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    Boolean clearStatus = new Boolean(true); // default
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();

    String TableID = parameters.getValue ( "TableID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TableID != null) && (TableID.indexOf("${") >= 0) ) {
		TableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TableID);
	}
    String ExcludeCrops = parameters.getValue ( "ExcludeCrops" );
	if ( (commandPhase == CommandPhaseType.RUN) && (ExcludeCrops != null) && (ExcludeCrops.indexOf("${") >= 0) ) {
		ExcludeCrops = TSCommandProcessorUtil.expandParameterValue(processor, this, ExcludeCrops);
	}
	String [] excludeCrops = new String[0];
	if ( (ExcludeCrops != null) && !ExcludeCrops.isEmpty() ) {
		excludeCrops = ExcludeCrops.split(",");
		for ( int i = 0; i < excludeCrops.length; i++ ) {
			excludeCrops[i] = excludeCrops[i].trim();
		}
	}
    String IncludeParcelsWithNoSupply = parameters.getValue ( "IncludeParcelsWithNoSupply" );
    boolean includeParcelsWithNoSupply = false; // default
	if ( (commandPhase == CommandPhaseType.RUN) && (IncludeParcelsWithNoSupply != null) && (IncludeParcelsWithNoSupply.indexOf("${") >= 0) ) {
		IncludeParcelsWithNoSupply = TSCommandProcessorUtil.expandParameterValue(processor, this, IncludeParcelsWithNoSupply);
	}
	if ( (IncludeParcelsWithNoSupply != null) && !IncludeParcelsWithNoSupply.isEmpty() ) {
		if ( IncludeParcelsWithNoSupply.equalsIgnoreCase(_True) ) {
			includeParcelsWithNoSupply = true;
		}
	}
    
    // Get the table to process.

	DataTable table = null;
    if ( commandPhase == CommandPhaseType.RUN ) {
        PropList request_params = null;
        CommandProcessorRequestResultsBean bean = null;
        if ( (TableID != null) && !TableID.equals("") ) {
            // Get the table to be removed, even though will not use it within the command
            request_params = new PropList ( "" );
            request_params.set ( "TableID", TableID );
            try {
                bean = processor.processRequest( "GetTable", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
                Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table == null ) {
                message = "Unable to find table to process using TableID=\"" + TableID + "\".";
                Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
                status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a table exists with the requested ID." ) );
            }
            else {
            	table = (DataTable)o_Table;
            }
        }
    }

	// Get the HydroBase DMI...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}

	int acresColumnNum = -1;
	int cropTypeColumnNum = -1;
	int districtColumnNum = -1;
	int divColumnNum = -1;
	int irrigTypeColumnNum = -1;
	int parcelIdColumnNum = -1;
	int yearColumnNum = -1;
   	try {
   		acresColumnNum = table.getFieldIndex("ACRES");
   		if ( table.getFieldDataType(acresColumnNum) != TableField.DATA_TYPE_DOUBLE ) {
   			message = "Column \"ACRES\" is not a double.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as a double precision number when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"ACRES\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"ACRES\" column." ) );
   	}
   	try {
   		yearColumnNum = table.getFieldIndex("CAL_YEAR");
   		if ( table.getFieldDataType(yearColumnNum) != TableField.DATA_TYPE_INT ) {
   			message = "Column \"CAL_YEAR\" is not an integer.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as an integer when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"CAL_YEAR\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"CAL_YEAR\" column." ) );
   	}
   	try {
   		cropTypeColumnNum = table.getFieldIndex("CROP_TYPE");
   		if ( table.getFieldDataType(cropTypeColumnNum) != TableField.DATA_TYPE_STRING ) {
   			message = "Column \"CROP_TYPE\" is not a string.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as text when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"CROP_TYPE\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"CROP_TYPE\" column." ) );
   	}
   	try {
   		divColumnNum = table.getFieldIndex("DIV");
   		if ( table.getFieldDataType(divColumnNum) != TableField.DATA_TYPE_INT ) {
   			message = "Column \"DIV\" is not an integer.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as an integer when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"DIV\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"DIV\" column." ) );
   	}
   	try {
   		districtColumnNum = table.getFieldIndex("DISTRICT");
   		if ( table.getFieldDataType(districtColumnNum) != TableField.DATA_TYPE_INT ) {
   			message = "Column \"DISTRICT\" is not an integer.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as an integer when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"DISTRICT\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"DIV\" column." ) );
   	}
   	try {
   		irrigTypeColumnNum = table.getFieldIndex("IRRIG_TYPE");
   		if ( table.getFieldDataType(irrigTypeColumnNum) != TableField.DATA_TYPE_STRING ) {
   			message = "Column \"IRRIG_TYPE\" is not a string.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as text when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"IRRIG_TYPE\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"IRRIG_TYPE\" column." ) );
   	}
   	try {
   		parcelIdColumnNum = table.getFieldIndex("PARCEL_ID");
   		if ( table.getFieldDataType(parcelIdColumnNum) != TableField.DATA_TYPE_INT ) {
   			message = "Column \"PARCEL_ID\" is not an integer.";
   				Message.printWarning(warning_level,
	   				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   			status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   				message, "Read the column as an integer when reading irrigated lands data table." ) );
   		}
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"PARCEL_ID\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"CAL_YEAR\" column." ) );
   	}

   	int columnNum = 0;
   	String columnName = null;

	// Loop through surface water supplies and determine the column numbers for surface supplies.
   	int [] swIdColNums = new int[0];
   	List<Integer> swIdColNumList = new ArrayList<Integer>();
	for ( int isw = 1; isw <= 100; isw++ ) {
		try {
			columnName = "SW_WDID" + isw;
			//Message.printStatus(2, routine, "Getting column number for \"" + columnName + "\"");
			columnNum = table.getFieldIndex(columnName);
			// Successfully looked up the column number so add to the list of column numbers.
			swIdColNumList.add(new Integer(columnNum));
			//Message.printStatus(2, routine, "column number for \"" + columnName + "\" is " + columnNum);
		}
		catch ( Exception e ) {
			// Ran out of surface water supply columns
			swIdColNums = new int[swIdColNumList.size()];
			Message.printStatus(2, routine, "Number of SW_WDID columns is " + swIdColNums.length);
			for ( int i = 0; i < swIdColNumList.size(); i++ ) {
				swIdColNums[i] = swIdColNumList.get(i);
			}
			break;
		}
	}

	// Loop through groundwater supplies and determine the column numbers for groundwater supplies.
   	int [] gwIdColNums = new int[0];
   	int [] gwIdTypeColNums = new int[0];
   	List<Integer> gwIdColNumList = new ArrayList<Integer>();
   	List<Integer> gwIdTypeColNumList = new ArrayList<Integer>();
	for ( int igw = 1; igw <= 100; igw++ ) {
		try {
			columnName = "GW_ID" + igw;
			//Message.printStatus(2, routine, "Getting column number for \"" + columnName + "\"");
			columnNum = table.getFieldIndex(columnName);
			// Successfully looked up the column number so add to the list of column numbers.
			gwIdColNumList.add(new Integer(columnNum));
			//Message.printStatus(2, routine, "column number for \"" + columnName + "\" is " + columnNum);

			String columnName2 = "GW_TYPE" + igw;
			//Message.printStatus(2, routine, "Getting column number for \"" + columnName2 + "\"");
			columnNum = table.getFieldIndex(columnName2);
			// Successfully looked up the column number so add to the list of column numbers.
			gwIdTypeColNumList.add(new Integer(columnNum));
			//Message.printStatus(2, routine, "column number for \"" + columnName2 + "\" is " + columnNum);
		}
		catch ( Exception e ) {
			// Ran out of groundwater supply columns
			gwIdColNums = new int[gwIdColNumList.size()];
			Message.printStatus(2, routine, "Number of GW_ID columns is " + gwIdColNums.length);
			for ( int i = 0; i < gwIdColNumList.size(); i++ ) {
				gwIdColNums[i] = gwIdColNumList.get(i);
			}

			gwIdTypeColNums = new int[gwIdTypeColNumList.size()];
			Message.printStatus(2, routine, "Number of GW_TYPE columns is " + gwIdTypeColNums.length);
			for ( int i = 0; i < gwIdTypeColNumList.size(); i++ ) {
				gwIdTypeColNums[i] = gwIdTypeColNumList.get(i);
			}
			break;
		}
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	try {
    	// Check the irrigated lands table.
	    PropList requestParams = new PropList ( "" );
	    requestParams.setUsingObject ( "TableID", TableID );
	    try {
	    	// Loop through the irrigated lands parcels.
	    	TableRecord rec = null;
	    	String wdid = null;
	    	String receipt = null;
	    	String gwType = null;
	    	int [] wdidParts;
	    	// Parcel data
	    	String cropType = null;
	    	int numParcelSWSupply = 0;
	    	int numParcelGWSupply = 0;
	    	
	    	// Just use Object since only using for messaging
	    	Object year;
	    	Object parcelId;
	    	HydroBase_StructureView hbstruct = null;
	    	HydroBase_Wells hbwells = null;
	    	List<HydroBase_Wells> hbwellsList = null;
	    	// HashMap to store structure views to increase performance
	    	HashMap<String,HydroBase_StructureView> hbstructMap = new HashMap<>();
	    	HashMap<String,HydroBase_Wells> hbwellsMap = new HashMap<>();
	    	for ( int irec = 0; irec < table.getNumberOfRecords(); irec++ ) {
	    		rec = table.getRecord(irec);

				cropType = rec.getFieldValueString(cropTypeColumnNum);
				if ( !includeCropType(cropType, excludeCrops) ) {
					// Don't want to include parcels with this the crop type
					// - will no see errors because parcel and supply output is omitted from report
					continue;
				}
				year = (Integer)table.getFieldValue(irec, yearColumnNum);
				parcelId = (Integer)table.getFieldValue(irec, parcelIdColumnNum);

				numParcelSWSupply = 0;
				numParcelGWSupply = 0;

	    		// Loop through surface water supplies
	    		for ( int isw = 0; isw < swIdColNums.length; isw++ ) {
	    			wdid = rec.getFieldValueString(swIdColNums[isw]);
	    			if ( (wdid != null) && !wdid.isEmpty() ) {
	    				// Have a surface water supply to check.
	    				// - read the structure
	    				++numParcelSWSupply;
	    				hbstruct = hbstructMap.get(wdid);
	    				if ( hbstruct == null ) {
	    					// Have not read the structure before so try to read
	    					wdidParts = HydroBase_WaterDistrict.parseWDID(wdid);
	    					hbstruct = hbdmi.readStructureViewForWDID(wdidParts[0], wdidParts[1]);
	    					if ( hbstruct == null ) {
	    						year = table.getFieldValue(irec, yearColumnNum);
	    						parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    						message = "Year " + year + " parcel ID " + parcelId + " SW supply WDID " + wdid +
	    							" is not found in the database.";
	        					Message.printWarning(warning_level,
	        						MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        					status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        						message, "Check the irrigated lands GIS data." ) );
	    						continue;
	    					}
	    					else {
	    						hbstructMap.put(wdid, hbstruct);
	    					}
	    				}
	    				// TODO smalers 2020-12-06 add check for duplicate supply like in ReadParcelsFromIrrigatedLands command
	    				if ( (hbstruct != null) && hbstruct.getStr_type().equals("W") ) {
	    					// Surface water supply but using a well WDID.  This is a data error.
	    					year = table.getFieldValue(irec, yearColumnNum);
	    					parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    					message = "Year " + year + " parcel ID " + parcelId + " SW supply WDID " + wdid +
	    						" is a well structure (structure type = " + hbstruct.getStr_type() + ") - expecting diviersion structure.";
	        				Message.printWarning(warning_level,
	        					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        				status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        					message, "Check the irrigated lands GIS data." ) );
	    				}
	    			}
	    		}
	    		// Loop through ground water supplies
	    		for ( int igw = 0; igw < gwIdColNums.length; igw++ ) {
	    			gwType = rec.getFieldValueString(gwIdTypeColNums[igw]);

	    			if ( (gwType != null) && gwType.equalsIgnoreCase("WDID") ) {
	    				++numParcelGWSupply;
	    				wdid = rec.getFieldValueString(gwIdColNums[igw]);
	    				if ( (wdid != null) && !wdid.isEmpty() ) {
	    					// Have a groundwater supply to check.
	    					// - read the structure
	    					wdidParts = HydroBase_WaterDistrict.parseWDID(wdid);
	    					hbstruct = hbstructMap.get(wdid);
	    					if ( hbstruct == null ) {
	    						// Have not read before so try to read
	    						hbstruct = hbdmi.readStructureViewForWDID(wdidParts[0], wdidParts[1]);
	    						if ( hbstruct == null ) {
	    							year = table.getFieldValue(irec, yearColumnNum);
	    							parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    							message = "Year " + year + " parcel ID " + parcelId + " GW supply WDID " + wdid +
	    								" is not found in the database).";
	        						Message.printWarning(warning_level,
	        							MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        						status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        							message, "Check the irrigated lands GIS data." ) );
	    							continue;
	    						}
	    						else {
	    							hbstructMap.put(wdid, hbstruct);
	    						}
	    					}
	    					// TODO smalers 2020-12-06 add check for duplicate supply like in ReadParcelsFromIrrigatedLands command
	    					if ( (hbstruct != null) && !hbstruct.getStr_type().equals("W") ) {
	    						// Groundwater supply but using a WDID that is not a well.  This is a data error.
	    						year = table.getFieldValue(irec, yearColumnNum);
	    						parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    						message = "Year " + year + " parcel ID " + parcelId + " GW supply WDID " + wdid +
	    							" is not a well structure (structure type is " + hbstruct.getStr_type() + ").";
	        					Message.printWarning(warning_level,
	        						MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        					status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        						message, "Check the irrigated lands GIS data." ) );
	    					}
	    				}
	    			}
	    			else if ( gwType.equalsIgnoreCase("Receipt") ) {
	    				// TODO smalers 2020-12-04 check for receipt in wells
	    				++numParcelGWSupply;
	    				receipt = rec.getFieldValueString(columnNum);
	    				if ( (receipt != null) && !receipt.isEmpty() ) {
	    					// Have a groundwater supply to check.
	    					// - read the well
	    					hbwells = hbwellsMap.get(receipt);
	    					if ( hbwells == null ) {
	    						// Have not read before so try to read
	    						hbwellsList = hbdmi.readWellsList(receipt, -1, -1);
	    						if ( (hbwellsList == null) || (hbwellsList.size() == 0) ) {
	    							// Groundwater supply but receipt is not found in the database.
	    							year = table.getFieldValue(irec, yearColumnNum);
	    							parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    							message = "Year " + year + " parcel ID " + parcelId + " GW supply receipt " + receipt +
	    								" is not found in the database.";
	        						Message.printWarning(warning_level,
	        							MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        						status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        							message, "Check the irrigated lands GIS data." ) );
	    						}
	    						else {
	    							hbwellsMap.put(receipt, hbwells);
	    						}
	    					}
	    					// TODO smalers 2020-12-06 add check for duplicate supply like in ReadParcelsFromIrrigatedLands command
	    				}
	    			}
	    		}

	    		// Add an error if there are no supplies for a parcel
	    		// - if NO_CROP was excluded then this is particularly an issue
	    		// - recompute the parcel data to make sure the counts are accurate
	    		if ( (numParcelSWSupply + numParcelGWSupply) == 0 ) {
	    			// Parcel does not have any supply
	    			if ( includeParcelsWithNoSupply ) {
	    				// Include but add an error.
	    				message = "Year " + year + " parcel ID " + parcelId + " has no supplies for included crops.";
						Message.printWarning(warning_level,
							MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
						status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Check the irrigated lands GIS data." ) );
	    			}
	    		}
	    	}
	    }
	    catch ( Exception e ) {
	        message = "Error checking irrigated lands table \"" + TableID + "\".";
	        Message.printWarning ( 3, routine, e );
	        Message.printWarning(warning_level,
	            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Report problem to software support." ) );
	    }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error checking irrigated lands table (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
		throw new CommandWarningException ( message );
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
    String ExcludeCrops = props.getValue( "ExcludeCrops" );
    String IncludeParcelsWithNoSupply = props.getValue( "IncludeParcelsWithNoSupply" );
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (ExcludeCrops != null) && (ExcludeCrops.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcludeCrops=\"" + ExcludeCrops + "\"" );
    }
    if ( (IncludeParcelsWithNoSupply != null) && (IncludeParcelsWithNoSupply.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeParcelsWithNoSupply=" + IncludeParcelsWithNoSupply );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}