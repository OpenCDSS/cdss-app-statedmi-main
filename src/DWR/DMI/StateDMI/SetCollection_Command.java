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
import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_Location;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_Well;
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
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "PartIDs" );
	valid_Vector.add ( "Year" );
	valid_Vector.add ( "Div" );
	valid_Vector.add ( "PartType" );
	valid_Vector.add ( "IfNotFound" );
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
	
	if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_AGGREGATE,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_AGGREGATE;
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_SYSTEM,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_SYSTEM;
	}
	else if ( StringUtil.indexOfIgnoreCase(
		getCommandName(), StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT,0) >= 0 ) {
		collectionType = StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT;
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
	}
    String IfNotFound = parameters.getValue ( "IfNotFound" );
    if ( (IfNotFound == null) || IfNotFound.equals("") ) {
    	IfNotFound = _Warn;
    }
    
    // Get the data needed for the command
    
    List culocList = null;
    List divList = null;
    List resList = null;
    List wellList = null;
    try {
    	Object o = null;
		if ( nodeType.equals(_Diversion) || nodeType.equals(_Well) ) {
			o = processor.getPropContents ( "StateCU_Location_List" );
			if ( o != null ) {
				culocList = (List)o;
			}
	    }
		if ( nodeType.equals(_Diversion) ) {
			o = processor.getPropContents ( "StateMod_DiversionStation_List" );
			if ( o != null ) {
				divList = (List)o;
			}
	    }
		if ( nodeType.equals(_Reservoir) ) {
			o = processor.getPropContents ( "StateMod_ReservoirStation_List" );
			if ( o != null ) {
				resList = (List)o;
			}
	    }
		if ( nodeType.equals(_Well) ) {
			o = processor.getPropContents ( "StateMod_WellStation_List" );
			if ( o != null ) {
				wellList = (List)o;
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
    		culoc = (StateCU_Location)culocList.get(i);
    		id = culoc.getID();
    		if ( !id.equalsIgnoreCase(ID) ) {
    			// Identifier does not match...
    			continue;
    		}
    		// Have a match so set the data...
    		List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    		List<String> partIdList = new ArrayList<String>();
    		List<String> partIdTypeList = new ArrayList<String>();
    		for ( String partId : tokens ) {
    			// Look for any IDs starting with p: and strip off.
    			if ( partId.startsWith("p:") ) {
    				// Assume a receipt number
    				partIdList.add(partId.substring(2));
    				partIdTypeList.add(StateMod_Well.COLLECTION_WELL_PART_ID_TYPE_RECEIPT);
    			}
    			else {
    				// Assume a WDID
    				partIdList.add(partId);
    				partIdTypeList.add(StateMod_Well.COLLECTION_WELL_PART_ID_TYPE_WDID);
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
    		if ( collectionType.equalsIgnoreCase( StateCU_Location.COLLECTION_TYPE_AGGREGATE) ) {
    			culoc.setCollectionType ( StateCU_Location.COLLECTION_TYPE_AGGREGATE );
    		}
    		else if ( collectionType.equalsIgnoreCase(StateCU_Location.COLLECTION_TYPE_SYSTEM) ) {
    			culoc.setCollectionType ( StateCU_Location.COLLECTION_TYPE_SYSTEM );
    		}
    		culoc.setCollectionPartType ( PartType );
    		if ( nodeType.equalsIgnoreCase(_Well) ) {
    			culoc.setCollectionDiv ( Div_int );
    			if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_PARCEL) ) {
    				culoc.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_DITCH) ) {
    				culoc.setCollectionPartIDs ( partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_WELL) ) {
    				culoc.setCollectionPartIDs ( partIdList, partIdTypeList );
    			}
    		}
    		else {
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
    			div = (StateMod_Diversion)divList.get(i);
    			id = div.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    			Message.printStatus ( 2, routine,
    				"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
    			if ( div.isCollection() ) {
        	        message = "Diversion \"" + id + "\" is already a collection (" +
    	        	div.getCollectionType() + ").";
	    	        Message.printWarning ( warning_level, 
	    	        MessageUtil.formatMessageTag(command_tag, ++warning_count),
	    	        routine, message );
	    	        status.addToLog ( command_phase,
    	            new CommandLogRecord(CommandStatusType.FAILURE,
    	                message, "Last collection information specified will apply." ) );
    			}
    			if ( collectionType.equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_AGGREGATE) ){
    				div.setCollectionType (	StateMod_Diversion.COLLECTION_TYPE_AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_SYSTEM) ){
    				div.setCollectionType (	StateMod_Diversion.COLLECTION_TYPE_SYSTEM );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT)){
    				div.setCollectionType (StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT);
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
    			res = (StateMod_Reservoir)resList.get(i);
    			id = res.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
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
    			if ( collectionType.equalsIgnoreCase(StateMod_Reservoir.COLLECTION_TYPE_AGGREGATE)){
    				res.setCollectionType (	StateMod_Reservoir.COLLECTION_TYPE_AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase(StateMod_Reservoir.COLLECTION_TYPE_SYSTEM)){
    				res.setCollectionType (	StateMod_Reservoir.COLLECTION_TYPE_SYSTEM );
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
    			well = (StateMod_Well)wellList.get(i);
    			id = well.getID();
    			if ( !id.equalsIgnoreCase(ID) ) {
    				// Identifier does not match...
    				continue;
    			}
    			// Have a match so set the data...
    			List<String> tokens = StringUtil.breakStringList ( PartIDs, ", ", StringUtil.DELIM_SKIP_BLANKS );
    			Message.printStatus ( 2, routine,
    				"Setting " + id + " " + collectionType + " parts (" + PartType + ") -> " + tokens );
        		List<String> partIdList = new ArrayList<String>();
        		List<String> partIdTypeList = new ArrayList<String>();
        		for ( String partId : tokens ) {
        			// Look for any IDs starting with p: and strip off.
        			if ( partId.startsWith("p:") ) {
        				// Assume a receipt number
        				partIdList.add(partId.substring(2));
        				partIdTypeList.add(StateMod_Well.COLLECTION_WELL_PART_ID_TYPE_RECEIPT);
        			}
        			else {
        				// Assume a WDID
        				partIdList.add(partId);
        				partIdTypeList.add(StateMod_Well.COLLECTION_WELL_PART_ID_TYPE_WDID);
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
    			if ( collectionType.equalsIgnoreCase( StateMod_Well.COLLECTION_TYPE_AGGREGATE) ) {
    				well.setCollectionType ( StateMod_Well.COLLECTION_TYPE_AGGREGATE );
    			}
    			else if(collectionType.equalsIgnoreCase( StateMod_Well.COLLECTION_TYPE_SYSTEM) ) {
    				well.setCollectionType ( StateMod_Well.COLLECTION_TYPE_SYSTEM );
    			}
    			well.setCollectionPartType ( PartType );
    			if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_PARCEL) ) {
    				well.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_DITCH) ) {
    				well.setCollectionPartIDsForYear ( Year_int, partIdList );
    			}
    			else if ( PartType.equalsIgnoreCase(StateMod_Well.COLLECTION_PART_TYPE_WELL) ) {
    				well.setCollectionPartIDs ( partIdList, partIdTypeList );
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
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
