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
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.Table.DataTable;
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
    
	// Check for invalid parameters...
	List<String> valid_Vector = new ArrayList<>(1);
    valid_Vector.add ( "TableID" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );    

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

	int yearColumnNum = -1;
	int parcelIdColumnNum = -1;
   	try {
   		yearColumnNum = table.getFieldIndex("CAL_YEAR");
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"CAL_YEAR\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"CAL_YEAR\" column." ) );
   	}
   	try {
   		parcelIdColumnNum = table.getFieldIndex("PARCEL_ID");
   	}
   	catch ( Exception e ) {
		message = "Unable to find column \"PARCEL_ID\"";
	   	Message.printWarning(warning_level,
	   		MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	   	status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	   		message, "Check irrigated lands data table for \"CAL_YEAR\" column." ) );
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
	    	// Just use Object since only using for messaging
	    	Object year;
	    	Object parcelId;
	    	HydroBase_StructureView hbstruct = null;
	    	HydroBase_Wells hbwells = null;
	    	List<HydroBase_Wells> hbwellsList = null;
	    	String columnName;
	    	int columnNum;
	    	// HashMap to store structure views to increase performance
	    	HashMap<String,HydroBase_StructureView> hbstructMap = new HashMap<>();
	    	HashMap<String,HydroBase_Wells> hbwellsMap = new HashMap<>();
	    	for ( int irec = 0; irec < table.getNumberOfRecords(); irec++ ) {
	    		rec = table.getRecord(irec);
	    		// Loop through surface water supplies
	    		for ( int isw = 1; isw <= 9; isw++ ) {
	    			columnName = "SW_WDID" + isw;
	    			try {
	    				columnNum = table.getFieldIndex(columnName);
	    			}
	    			catch ( Exception e ) {
	    				// Should not happen
	    				continue;
	    			}
	    			wdid = rec.getFieldValueString(columnNum);
	    			if ( (wdid != null) && !wdid.isEmpty() ) {
	    				// Have a surface water supply to check.
	    				// - read the structure
	    				hbstruct = hbstructMap.get(wdid);
	    				if ( hbstruct == null ) {
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
	    				if ( hbstruct.getStr_type().equals("W") ) {
	    					// Surface water supply but using a well WDID.  This is a data error.
	    					year = table.getFieldValue(irec, yearColumnNum);
	    					parcelId = table.getFieldValue(irec, parcelIdColumnNum);
	    					message = "Year " + year + " parcel ID " + parcelId + " SW supply WDID " + wdid +
	    						" is a well structure (structure type = " + hbstruct.getStr_type() + ") - expecting ditch.";
	        				Message.printWarning(warning_level,
	        					MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
	        				status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
	        					message, "Check the irrigated lands GIS data." ) );
	    				}
	    			}
	    		}
	    		// Loop through ground water supplies
	    		for ( int igw = 1; igw <= 20; igw++ ) {
	    			columnName = "GW_TYPE" + igw;
	    			try {
	    				columnNum = table.getFieldIndex(columnName);
	    			}
	    			catch ( Exception e ) {
	    				// Should not happen
	    				continue;
	    			}
	    			gwType = rec.getFieldValueString(columnNum);
    				columnName = "GW_ID" + igw;
    				try {
    					columnNum = table.getFieldIndex(columnName);
    				}
    				catch ( Exception e ) {
    					// Should not happen
    					continue;
    				}
	    			if ( (gwType != null) && gwType.equalsIgnoreCase("WDID") ) {
	    				wdid = rec.getFieldValueString(columnNum);
	    				if ( (wdid != null) && !wdid.isEmpty() ) {
	    					// Have a groundwater supply to check.
	    					// - read the structure
	    					wdidParts = HydroBase_WaterDistrict.parseWDID(wdid);
	    					hbstruct = hbstructMap.get(wdid);
	    					if ( hbstruct == null ) {
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
	    					if ( !hbstruct.getStr_type().equals("W") ) {
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
	    				receipt = rec.getFieldValueString(columnNum);
	    				if ( (receipt != null) && !receipt.isEmpty() ) {
	    					// Have a groundwater supply to check.
	    					// - read the well
	    					hbwells = hbwellsMap.get(receipt);
	    					if ( hbwells == null ) {
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
	    				}
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
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}