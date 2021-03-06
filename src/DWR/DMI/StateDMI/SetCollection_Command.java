// SetCollection_Command - This class initializes, checks, and runs the Set*Aggregate/System() commands (set collection information).

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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateCU.StateCU_Location;
import DWR.StateCU.StateCU_Location_CollectionPartIdType;
import DWR.StateCU.StateCU_Location_CollectionPartType;
import DWR.StateCU.StateCU_Location_CollectionType;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Diversion_CollectionType;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_Reservoir_CollectionType;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_Well_CollectionPartIdType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import DWR.StateMod.StateMod_Well_CollectionType;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the Set*Aggregate/System() commands (set collection information).
It is an abstract base class that must be controlled via a derived class.  For example, the
SetDiversionAggregate() command extends this class in order to uniquely represent the command,
but much of the functionality is in this base class.
*/
public abstract class SetCollection_Command extends AbstractCommand implements Command
{
	
/**
Possible values for the PartType parameter.
*/
protected final String _Ditch = "Ditch";
protected final String _Parcel = "Parcel";

/**
Possible values for node type, used by the editor and internally for messages.
*/
protected final String _Well = "Well"; // Also a choice for PartType
protected final String _Diversion = "Diversion";
protected final String _Reservoir = "Reservoir";

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetCollection_Command ()
{	super();
	setCommandName ( "Set?Collection?" );
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
{	String ID = parameters.getValue ( "ID" );
	String PartIDs = parameters.getValue ( "PartIDs" );
	String Year = parameters.getValue ( "Year" );
	String Div = parameters.getValue ( "Div" );
	String PartType = parameters.getValue ( "PartType" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "The ID must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the ID for the collection.") );
	}
	else if ( ID.indexOf("*") >= 0 ) {
		message = "The ID (" + ID + ") cannot contain the wildcard (*) character.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the ID without the wildcard (*) character.") );
	}
	
	if ( (PartIDs == null) || (PartIDs.length() == 0) ) {
		message = "The PartIDs must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the part IDs for the collection.") );
	}
	
	if ( (StringUtil.indexOfIgnoreCase(getCommandName(),_Well,0) >= 0) ) {
		// For wells,  allow collections by ditch, parcel and well
		if ( (PartType != null) && (PartType.length() != 0) &&
			!PartType.equalsIgnoreCase(_Ditch) && !PartType.equalsIgnoreCase(_Parcel) &&
			!PartType.equalsIgnoreCase(_Well)) {
			message = "The part type (" + PartType + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the part type as " + _Ditch + ", " + _Parcel + " or " + _Well + ".") );
		}
	}
	else {
		// Diversions - only allow collection by ditch
		if ( (PartType != null) && (PartType.length() != 0) && !PartType.equalsIgnoreCase(_Ditch) ) {
			message = "The part type (" + PartType + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the part type as " + _Ditch + ".") );
		}
	}
	
	if ( (StringUtil.indexOfIgnoreCase(getCommandName(),_Well,0) >= 0) ) {
		// Node type is well
		if ( (PartType != null) && PartType.equalsIgnoreCase(_Parcel) ) {
			if ( (Year == null) || (Year.length() == 0) ) {
				message = "The year must be specified with part type of " + _Parcel;
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			else if ( !StringUtil.isInteger(Year)) {
				message = "The year (" + Year + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			if ( (Div == null) || (Div.length() == 0) ) {
				message = "The division must be specified with part type of " + _Parcel;
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the year as an integer.") );
			}
			else if ( !StringUtil.isInteger(Div)) {
				message = "The division (" + Div + ") is invalid.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the division as an integer.") );
			}
		}
	}
	else if ( (PartType != null) && !PartType.equals("") ) {
		// DO NOT specify the year or division
		if ( (Year != null) && (Year.length() > 0) ) {
			message = "The year should only be specified with part type of " + _Parcel;
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify the year with part type " + PartType) );
		}
		if ( (Div != null) && (Div.length() > 0) ) {
			message = "The division should only be specified with part type of " + _Parcel;
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Do not specify the division with part type " + PartType) );
		}
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<>(6);
	validList.add ( "ID" );
	validList.add ( "PartIDs" );
	validList.add ( "Year" );
	validList.add ( "Div" );
	validList.add ( "PartType" );
	validList.add ( "WellReceiptWaterDistrictMap" );
	validList.add ( "IfNotFound" );
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
	return (new SetCollection_JDialog ( parent, this )).ok();
}

// Use base class parse method

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
@param command_phase Command phase being executed (RUN or DISCOVERY).
*/
protected void runCommandInternal ( int command_number, CommandPhaseType command_phase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    String nodeType = null; // Diversion, Well, Reservoir
    String collectionType = null; // Aggregate, System, MultiStruct
	if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Diversion,0) >= 0 ) {
		nodeType = _Diversion;
	}
	else if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Well,0) >= 0 ) {
		nodeType = _Well;
	}
	else if ( StringUtil.indexOfIgnoreCase(getCommandName(), _Reservoir,0) >= 0 ) {
		nodeType = _Reservoir;
	}
	
	// Use string initially and later in this function
	if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.AGGREGATE.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.AGGREGATE.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.SYSTEM.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.SYSTEM.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion_CollectionType.MULTISTRUCT.toString(),0) >= 0 ) {
		collectionType = StateMod_Diversion_CollectionType.MULTISTRUCT.toString();
	}

    PropList parameters = getCommandParameters();
    String ID = parameters.getValue ( "ID" );
    String PartIDs = parameters.getValue ( "PartIDs" );
    String Year = parameters.getValue ( "Year" );
    int Year_int = 0; // Only used by wells
    if ( (Year != null) && !Year.equals("") ) {
    	Year_int = Integer.parseInt(Year);
    }
    String Div = parameters.getValue ( "Div" );
    int Div_int = 0;
    if ( (Div != null) && !Div.equals("") ) {
    	Div_int = Integer.parseInt(Div);
    }
    String PartType = parameters.getValue ( "PartType" );
	if ( nodeType.equalsIgnoreCase(_Diversion) ) {
		PartType = _Ditch;	// Default for diversions.
	}
	else if ( nodeType.equalsIgnoreCase(_Reservoir) ) {
		PartType = _Reservoir;	// Default for reservoirs.
	}
	else if ( nodeType.equalsIgnoreCase(_Well) ) {
		// Part type will have been set above (required).
		// - can be Well or Parcel
	}
    String WellReceiptWaterDistrictMap = parameters.getValue ( "WellReceiptWaterDistrictMap" );
    Hashtable<String,String> receiptWDMap = new Hashtable<String,String>();
    if ( (WellReceiptWaterDistrictMap != null) && (WellReceiptWaterDistrictMap.length() > 0) && (WellReceiptWaterDistrictMap.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(WellReceiptWaterDistrictMap, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            receiptWDMap.put(parts[0].trim(), parts[1].trim() );
        }
    }
    String IfNotFound = parameters.getValue ( "IfNotFound" );
    if ( (IfNotFound == null) || IfNotFound.equals("") ) {
    	IfNotFound = _Warn;
    }
    
    // Get the data needed for the command
    
    List<StateCU_Location> culocList = null;
    List<StateMod_Diversion> divList = null;
    List<StateMod_Reservoir> resList = null;
    List<StateMod_Well> wellList = null;
    try {
    	Object o = null;
		if ( nodeType.equals(_Diversion) || nodeType.equals(_Well) ) {
			o = processor.getPropContents ( "StateCU_Location_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateCU_Location> dataList = (List<StateCU_Location>)o;
				culocList = dataList;
			}
	    }
		if ( nodeType.equals(_Diversion) ) {
			o = processor.getPropContents ( "StateMod_DiversionStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)o;
				divList = dataList;
			}
	    }
		if ( nodeType.equals(_Reservoir) ) {
			o = processor.getPropContents ( "StateMod_ReservoirStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)o;
				resList = dataList;
			}
	    }
		if ( nodeType.equals(_Well) ) {
			o = processor.getPropContents ( "StateMod_WellStation_List" );
			if ( o != null ) {
				@SuppressWarnings("unchecked")
				List<StateMod_Well> dataList = (List<StateMod_Well>)o;
				wellList = dataList;
			}
	    }
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting data to process (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
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
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// This command applies to StateCU and StateMod data sets, each of which
    	// use the same "collection" commands.  In most (all?) cases, only one
    	// model's files well be processed.

    	// Process the StateCU data regardless because CU locations are not specifically defined
    	// as diversions or wells.  Therefore match any.  There can only be one match...

    	int size = 0;
    	if ( culocList != null ) {
    		size = culocList.size();
    	}
    	String id;
    	StateCU_Location culoc;
    	boolean matchFound = false;
    	for (int i = 0; i < size; i++) {
    		culoc = culocList.get(i);
    		id = culoc.getID();
    		if ( !id.equalsIgnoreCase(ID) ) {
    			// Identifier does not match...
    			continue;
    		}
    		// Have a match so set the data...
    		List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    		List<String> partIdList = new ArrayList<>();
    		List<StateCU_Location_CollectionPartIdType> partIdTypeList = new ArrayList<>();
    		List<Integer> partIdWDList = new ArrayList<>(); // Used to store WD for well receipt so WD cache lookups can occur later
    		for ( String partId : tokens ) {
    			if ( PartType.equalsIgnoreCase(_Parcel) ) {
    				// Part is the parcel ID.
   					partIdList.add(partId);
    			}
    			else {
    				// Look for any IDs starting with p: and strip off.
    				if ( partId.startsWith("p:") || partId.startsWith("P:")) {
    					// Assume a receipt number
    					partIdList.add(partId.substring(2));
    					partIdTypeList.add(StateCU_Location_CollectionPartIdType.RECEIPT);
   						// Look up the WD from command parameter
   						String wd = receiptWDMap.get(partId);
   						if ( wd != null ) {
    						partIdWDList.add(Integer.parseInt(wd));
   						}
   						else {
   							// Set the WD to -1, will be filled later through an additional database query
    						partIdWDList.add(new Integer(-1));
   						}
    				}
    				else {
    					// Assume a WDID
    					partIdList.add(partId);
    					partIdTypeList.add(StateCU_Location_CollectionPartIdType.WDID);
    					// Set the WD, mostly for information since used for Well part ID of receipt
    					try {
    						String wd = partId.substring(0,2);
    						partIdWDList.add(new Integer(wd));
    					}
    					catch ( NumberFormatException e ) {
    						// Not a WDID
    						message = "Part id \"" + partId + "\" is not an integer - cannot set WDID.";
	  						Message.printWarning(warning_level,
		  						MessageUtil.formatMessageTag( command_tag, ++warning_count),
		  						routine, message );
	  						status.addToLog ( CommandPhaseType.RUN,
		  						new CommandLogRecord(CommandStatusType.FAILURE,
			  						message, "Report problem to software support." ) );
	  						// Add a value to keep lists aligned.
    						partIdWDList.add(new Integer(-1));
    					}
    				}
    			}
    		}
    		Message.printStatus ( 2, routine,
    			"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
    		if ( culoc.isCollection() ) {
    	        message = "CU Location \"" + id + "\" is already a collection (" +
    	        	culoc.getCollectionType() + ").";
    	        Message.printWarning ( warning_level, 
    	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
    	        routine, message );
    	        status.addToLog ( command_phase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Last collection information specified will apply." ) );
    		}
    		// Set the collection type.
    		if ( collectionType.equalsIgnoreCase( StateCU_Location_CollectionType.AGGREGATE.toString()) ) {
    			culoc.setCollectionType ( StateCU_Location_CollectionType.AGGREGATE );
    		}
    		else if ( collectionType.equalsIgnoreCase(StateCU_Location_CollectionType.SYSTEM.toString()) ) {
    			culoc.setCollectionType ( StateCU_Location_CollectionType.SYSTEM );
    		}
    		// Get the enumeration from the part type string.
   			StateCU_Location_CollectionPartType collectionPartTypeForCuloc = StateCU_Location_CollectionPartType.valueOfIgnoreCase(PartType);
			if ( collectionPartTypeForCuloc == null ) {
   				message = "CU Location collection \"" + id + "\" part type \"" + PartType + "\" is invalid.";
   				Message.printWarning ( warning_level, 
   					MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
   				status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
   					message, "Verify that part type is valid." ) );
			}
    		culoc.setCollectionPartType ( collectionPartTypeForCuloc );
    		// Node type is determined from the command name.
    		if ( nodeType.equalsIgnoreCase(_Well) ) {
    			// SetWell...
    			culoc.setCollectionDiv ( Div_int );
    			if ( PartType.equalsIgnoreCase(StateCU_Location_CollectionPartType.PARCEL.toString()) ) {
    				Message.printStatus ( 2, routine,
    					"Setting " + id + " " + collectionType + " year " + Year_int + " parts (" + PartType + ") -> " + StringUtil.toString(partIdList,",") );
    				culoc.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateCU_Location_CollectionPartType.DITCH.toString()) ) {
    				culoc.setCollectionPartIDs ( partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateCU_Location_CollectionPartType.WELL.toString()) ) {
    				/* Should be OK - TODO smalers w019-07-05 remove this when tested out
    				List<StateCU_Location_CollectionPartIdType> partIdTypesForCuloc = new ArrayList<StateCU_Location_CollectionPartIdType>();
    				String partIdTypeForLoop;
    				for ( int iPart = 0; i < partIdTypeList.size(); i++ ) {
    					partIdTypeForLoop = partIdTypeList.get(iPart);
    					StateMod_Well_CollectionPartIdType partIdTypeForCuloc = StateMod_Well_CollectionPartIdType.valueOfIgnoreCase(partIdTypeForLoop);
    					if ( partIdTypeForCuloc == null ) {
    						message = "CU Location collection \"" + id + "\" part ID \"" + partIdList.get(iPart) +
    							"\" has invalid part ID type \"" + partIdTypeForLoop + "\".";
    						Message.printWarning ( warning_level, 
    							MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    						status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
    							message, "Verify that part ID type is valid." ) );
    					}
    				}
    				culoc.setCollectionPartIDs ( partIdList, partIdTypesForCuloc );
    				*/
    				culoc.setCollectionPartIDs ( partIdList, partIdTypeList, partIdWDList );
    			}
    		}
    		else {
    			// SetDiversion...
    			// - part type is always WDID
    			culoc.setCollectionPartIDs ( partIdList );
    		}
    		matchFound = true;
    		break;
    	}

    	// Process the StateMod data, depending on the node type.  There can only be one match.

    	if ( nodeType.equals ("Diversion") ) {
    		size = 0;
    		if ( divList != null ) {
    			size = divList.size();
    		}
    		StateMod_Diversion div;
    		for (int i = 0; i < size; i++) {
    			div = divList.get(i);
    			id = div.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    			Message.printStatus ( 2, routine,
    				"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
    			if ( div.isCollection() ) {
        	        message = "Diversion \"" + id + "\" is already a collection (" + div.getCollectionType() + ").";
	    	        Message.printWarning ( warning_level, 
	    	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( command_phase,
	    	        	new CommandLogRecord(CommandStatusType.FAILURE,
	    	        		message, "Last collection information specified will apply." ) );
    			}
    			if ( collectionType.equalsIgnoreCase(StateMod_Diversion_CollectionType.AGGREGATE.toString()) ){
    				div.setCollectionType (	StateMod_Diversion_CollectionType.AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Diversion_CollectionType.SYSTEM.toString()) ){
    				div.setCollectionType (	StateMod_Diversion_CollectionType.SYSTEM );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Diversion_CollectionType.MULTISTRUCT.toString())){
    				div.setCollectionType (StateMod_Diversion_CollectionType.MULTISTRUCT);
    			}
    			else {
        	        message = "Diversion \"" + id + "\" collection type (" + div.getCollectionType() + ") is invalid.";
	    	        Message.printWarning ( warning_level, 
	    	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( command_phase,
	    	        	new CommandLogRecord(CommandStatusType.FAILURE,
	    	        		message, "Last collection information specified will apply." ) );
	    	        continue;
    			}
    			div.setCollectionPartIDs ( tokens );
    			matchFound = true;
    			break;
    		}
    	}
    	else if ( nodeType.equals ("Reservoir") ) {
    		size = 0;
    		if ( resList != null ) {
    			size = resList.size();
    		}
    		StateMod_Reservoir res;
    		for (int i = 0; i < size; i++) {
    			res = resList.get(i);
    			id = res.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    			Message.printStatus ( 2, routine,
    				"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
    			if ( res.isCollection() ) {
        	        message = "Reservoir \"" + id + "\" is already a collection (" +
    	        	res.getCollectionType() + ").";
	    	        Message.printWarning ( warning_level, 
	    	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	    	        routine, message );
	    	        status.addToLog ( command_phase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Last collection information specified will apply." ) );
    			}
    			if ( collectionType.equalsIgnoreCase(StateMod_Reservoir_CollectionType.AGGREGATE.toString())){
    				res.setCollectionType (	StateMod_Reservoir_CollectionType.AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Reservoir_CollectionType.SYSTEM.toString())){
    				res.setCollectionType (	StateMod_Reservoir_CollectionType.SYSTEM );
    			}
    			else {
        	        message = "Reservoir \"" + id + "\" collection type (" + res.getCollectionType() + ") is invalid.";
	    	        Message.printWarning ( warning_level, 
	    	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( command_phase,
	    	        	new CommandLogRecord(CommandStatusType.FAILURE,
	    	        		message, "Last collection information specified will apply." ) );
	    	        continue;
    			}
    			res.setCollectionPartIDs ( tokens );
    			matchFound = true;
    			break;
    		}
    	}
    	else if ( nodeType.equals ("Well") ) {
    		size = 0;
    		if ( wellList != null ) {
    			size = wellList.size();
    		}
    		StateMod_Well well;
    		for (int i = 0; i < size; i++) {
    			well = wellList.get(i);
    			id = well.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    			Message.printStatus ( 2, routine,
    				"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
        		List<String> partIdList = new ArrayList<>();
        		List<StateMod_Well_CollectionPartIdType> partIdTypeList = new ArrayList<StateMod_Well_CollectionPartIdType>();
				List<Integer> partIdWDsForWell = new ArrayList<>(); // Used to store WD for receipt, for cached data lookups
        		for ( String partId : tokens ) {
        			if ( PartType.equalsIgnoreCase(_Parcel) ) {
    				 	// Part is the parcel ID.
   					 	partIdList.add(partId);
    			 	}
        			else {
        				// Look for any IDs starting with P: and strip off.
        				String partIdUpper = partId.toUpperCase();
        				if ( partIdUpper.startsWith("P:") || partIdUpper.startsWith("RECEIPT:") ) {
        					int offset = 0;
        					if ( partIdUpper.startsWith("P:") ) {
        						offset = 2;
        					}
        					else {
        						offset = 8;
        					}
        					// Assume a receipt number
        					String receipt = partId.substring(offset).trim();
        					partIdList.add(receipt);
        					partIdTypeList.add(StateMod_Well_CollectionPartIdType.RECEIPT);
        					// Need to determine the water district for the receipt for use later querying cached data
        					// - since the Set*FromList command is used for long lists, query the single receipt.
        					List<HydroBase_Wells> wellsForReceipt = hbdmi.readWellsList(receipt, -1, -1);
        					if ( wellsForReceipt.size() > 1 ) {
        						message = "Well \"" + id + "\" part \"" + receipt + "\" RECEIPT has more than one HydroBase record.";
        						Message.printWarning ( warning_level, 
        							MessageUtil.formatMessageTag(command_tag, ++warning_count),
        								routine, message );
        						status.addToLog ( command_phase,
        							new CommandLogRecord(CommandStatusType.WARNING,
        								message, "Last collection information specified will apply." ) );
        					}
        					else if ( wellsForReceipt.size() == 0 ) {
        						// No wells, see if there is a set.
   								String wd = receiptWDMap.get(partId);
   								if ( wd != null ) {
    								partIdWDsForWell.add(new Integer(wellsForReceipt.get(0).getWD()));
   								}
   								else {
   									// Set the WD to -1.
   									message = "Well \"" + id + "\" part \"" + receipt +
   										"\" RECEIPT has no well record in HydroBase.  Won't be able to read well from cached data using well WD.";
   									Message.printWarning ( warning_level, 
   										MessageUtil.formatMessageTag(command_tag, ++warning_count),
   											routine, message );
   									status.addToLog ( command_phase,
   										new CommandLogRecord(CommandStatusType.WARNING,
   											message, "Specify the WellReceiptWaterDistrictMap parameter." ) );
   									partIdWDsForWell.add(new Integer(-1));
   								}
        					}
        					else {
        						// Have a well record and can determine the WD for the receipt
    							partIdWDsForWell.add(new Integer(wellsForReceipt.get(0).getWD()));
        					}
        				}
        				else {
        					// Assume a WDID
        					partIdList.add(partId);
        					partIdTypeList.add(StateMod_Well_CollectionPartIdType.WDID);
        					// The WD for the part is based on the first 2 digits of the WDID
   							partIdWDsForWell.add(new Integer(partId.substring(0,2)));
        				}
        			}
        		}
    			if ( well.isCollection() ) {
        	        message = "Well \"" + id + "\" is already a collection (" +
    	        	well.getCollectionType() + ").";
	    	        Message.printWarning ( warning_level, 
	    	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	    	        routine, message );
	    	        status.addToLog ( command_phase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Last collection information specified will apply." ) );
    			}
    			if ( collectionType.equalsIgnoreCase(StateMod_Well_CollectionType.AGGREGATE.toString()) ) {
    				well.setCollectionType ( StateMod_Well_CollectionType.AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase( StateMod_Well_CollectionType.SYSTEM.toString()) ) {
    				well.setCollectionType ( StateMod_Well_CollectionType.SYSTEM );
    			}
    			else {
        	        message = "Well \"" + id + "\" collection type (" + well.getCollectionType() + ") is invalid.";
	    	        Message.printWarning ( warning_level, 
	    	        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
	    	        status.addToLog ( command_phase,
	    	        	new CommandLogRecord(CommandStatusType.FAILURE,
	    	        		message, "Last collection information specified will apply." ) );
	    	        continue;
    			}
    			StateMod_Well_CollectionPartType collectionPartTypeForWell = StateMod_Well_CollectionPartType.valueOfIgnoreCase(PartType);
 				if ( collectionPartTypeForWell == null ) {
    				message = "CU Location collection \"" + id + "\" part type \"" + PartType + "\" is invalid.";
    				Message.printWarning ( warning_level, 
    					MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    				status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
    					message, "Verify that part type is valid." ) );
 				}
    			well.setCollectionPartType ( collectionPartTypeForWell );
    			if ( collectionPartTypeForWell == StateMod_Well_CollectionPartType.PARCEL ) {
    				well.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( collectionPartTypeForWell == StateMod_Well_CollectionPartType.DITCH ) {
    				well.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( collectionPartTypeForWell == StateMod_Well_CollectionPartType.WELL ) {
    				well.setCollectionPartIDs ( partIdList, partIdTypeList, partIdWDsForWell );
    			}
    			else {
    				message = "CU Location collection \"" + id + "\" part type \"" + PartType + "\" is invalid.";
    				Message.printWarning ( warning_level, 
    					MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
    				status.addToLog ( command_phase, new CommandLogRecord(CommandStatusType.WARNING,
    					message, "Verify that part type is valid." ) );
    			}
    			well.setCollectionDiv ( Div_int );
    			matchFound = true;
    			break;
    		}
    	}
    	if ( !matchFound ) {
			if ( IfNotFound.equalsIgnoreCase(_Ignore) ) {
				// Don't do anything
			}
			else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "ID \"" + ID + "\" was not matched: warning and not setting " +
				collectionType + " information.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "ID \"" + ID + "\" was not matched: warning and not setting " +
				collectionType + " information.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting " + collectionType + " information (" + e + ").";
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
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}

	String ID = parameters.getValue ( "ID" );
	String Year = parameters.getValue ( "Year" );
	String Div = parameters.getValue ( "Div" );
	String PartType = parameters.getValue ( "PartType" );
	String PartIDs = parameters.getValue ( "PartIDs" );
	String WellReceiptWaterDistrictMap = parameters.getValue ( "WellReceiptWaterDistrictMap" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );

	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (Year != null) && (Year.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Year=" + Year );
	}
	if ( (Div != null) && (Div.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=" + Div );
	}
	if ( (PartType != null) && (PartType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartType=" + PartType );
	}
	if ( (PartIDs != null) && (PartIDs.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PartIDs=\"" + PartIDs + "\"" );
	}
	if ( (WellReceiptWaterDistrictMap != null) && (WellReceiptWaterDistrictMap.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WellReceiptWaterDistrictMap=\"" + WellReceiptWaterDistrictMap + "\"" );
	}
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
