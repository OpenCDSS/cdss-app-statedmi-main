// FillAndSetRight_Command - This class initializes, checks, and runs the Fill*Right() and Set*Right() commands.

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

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_DiversionRight;
import DWR.StateMod.StateMod_InstreamFlowRight;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;

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
<p>
This class initializes, checks, and runs the Fill*Right() and Set*Right() commands.
The functionality is handled in one class due to the close similarity between the commands.
It is an abstract base class that must be controlled via a derived class.  For example,
the SetDiversionRight() command extends this class in order to uniquely represent the command,
but much of the functionality is in this base class.
</p>
*/
public abstract class FillAndSetRight_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Additional values for the IfFound parameter.
*/
protected final String _Set = "Set";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetRight_Command ()
{	super();
	setCommandName ( "?Fill/Set?Right" );
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
{	//String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String AdministrationNumber = parameters.getValue ( "AdministrationNumber" );
	String Decree = parameters.getValue ( "Decree" );
	String OnOff = parameters.getValue ( "OnOff" );
	String AccountDist = parameters.getValue ( "AccountDist" ); // Reservoirs
	String RightType = parameters.getValue ( "RightType" ); // Reservoirs
	String FillType = parameters.getValue ( "FillType" ); // Reservoirs
	//String OpRightID = parameters.getValue ( "OpRightID" ); // Reservoirs
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String IfFound = parameters.getValue ( "IfFound" ); // Set
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The diversion right ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the diversion right ID to process." ) );
	}
	
	if ( (AdministrationNumber != null) && (AdministrationNumber.length() != 0) &&
		!StringUtil.isDouble(AdministrationNumber) ) {
        message = "The administration number (" + AdministrationNumber + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the administration number as a number NNNNN.NNNNN." ) );
	}
	
	if ( (Decree != null) && (Decree.length() != 0) && !StringUtil.isDouble(Decree) ) {
        message = "The decree (" + Decree + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the decree as a number." ) );
	}
	
	if ( (OnOff != null) && (OnOff.length() != 0) && !StringUtil.isInteger(OnOff) ) {
        message = "The on/off parameter (" + OnOff + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the on/off value as an integer." ) );
	}
	
	if ( (this instanceof FillReservoirRight_Command) || (this instanceof SetReservoirRight_Command) ) {
		if ( (AccountDist != null) && (AccountDist.length() != 0) && !StringUtil.isInteger(AccountDist) ) {
	        message = "The AccountDist parameter (" + AccountDist + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the AccountDist value as an integer." ) );
		}
		if ( (RightType != null) && (RightType.length() != 0) && !StringUtil.isInteger(RightType) ) {
	        message = "The RightType parameter (" + RightType + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the RightType value as an integer." ) );
		}
		if ( (FillType != null) && (FillType.length() != 0) && !StringUtil.isInteger(FillType) ) {
	        message = "The FillType parameter (" + FillType + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the FillType value as an integer." ) );
		}
	}

	if ( (this instanceof SetDiversionRight_Command)||
		(this instanceof SetReservoirRight_Command) ||
		(this instanceof SetInstreamFlowRight_Command) ||
		(this instanceof SetWellRight_Command) ) {
		// Include the Add option
		if ( (IfNotFound != null) && (IfNotFound.length() > 0) &&
			!IfNotFound.equalsIgnoreCase(_Add) &&
			!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
			!IfNotFound.equalsIgnoreCase(_Warn) ) {
			message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
							", or " + _Warn + " (default).") );
		}
	}
	else { // Fill
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
	}
	
	if ( (this instanceof SetDiversionRight_Command)||
		(this instanceof SetReservoirRight_Command) ||
		(this instanceof SetInstreamFlowRight_Command) ||
		(this instanceof SetWellRight_Command) ) {
		// Include the Set option
		if ( (IfFound != null) && (IfFound.length() > 0) &&
			!IfFound.equalsIgnoreCase(_Set) &&
			!IfFound.equalsIgnoreCase(_Ignore) && !IfFound.equalsIgnoreCase(_Fail) &&
			!IfFound.equalsIgnoreCase(_Warn) ) {
			message = "The IfFound value (" + IfFound + ") is invalid.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify IfFound as " + _Set + ", " + _Ignore + ", " + _Fail +
							", or " + _Warn + " (default).") );
		}
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "StationID" );
	valid_Vector.add ( "AdministrationNumber" );
	valid_Vector.add ( "Decree" );
	valid_Vector.add ( "OnOff" );
	if ( (this instanceof FillReservoirRight_Command) || (this instanceof SetReservoirRight_Command) ) {
		valid_Vector.add ( "AccountDist" );
		valid_Vector.add ( "RightType" );
		valid_Vector.add ( "FillType" );
		valid_Vector.add ( "OpRightID" );
	}
	valid_Vector.add ( "IfNotFound" );
	if ( (this instanceof SetDiversionRight_Command)||
		(this instanceof SetReservoirRight_Command) ||
		(this instanceof SetInstreamFlowRight_Command) ||
		(this instanceof SetWellRight_Command) ) {
		valid_Vector.add ( "IfFound" );
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
	return (new FillAndSetRight_JDialog ( parent, this )).ok();
}

// The following is expected to be called by the derived classes.

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
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.
    // Trim strings because Ray Bennett has written FORTRAN programs to process command files, and there
    // are sometimes extra spaces in the parameter values - this causes IDs to not be matched, etc.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	ID = ID.trim();
	String Name = parameters.getValue ( "Name" );
	if ( Name != null ) {
		Name = Name.trim();
	}
	String StationID = parameters.getValue ( "StationID" );
	if ( StationID != null ) {
		StationID = StationID.trim();
	}
	String AdministrationNumber = parameters.getValue ( "AdministrationNumber" );
	if ( AdministrationNumber != null ) {
		AdministrationNumber = AdministrationNumber.trim();
	}
	String Decree = parameters.getValue ( "Decree" );
	if ( Decree != null ) {
		Decree = Decree.trim();
	}
	String OnOff = parameters.getValue ( "OnOff" );
	if ( OnOff != null ) {
		OnOff = OnOff.trim();
	}
	String AccountDist = parameters.getValue ( "AccountDist" );// Reservoirs
	if ( AccountDist != null ) {
		AccountDist = AccountDist.trim();
	}
	String RightType = parameters.getValue ( "RightType" );	// Reservoirs
	if ( RightType != null ) {
		RightType = RightType.trim();
	}
	String FillType = parameters.getValue ( "FillType" ); // Reservoirs
	if ( FillType != null ) {
		FillType = FillType.trim();
	}
	String OpRightID = parameters.getValue ( "OpRightID" ); // Reservoirs
	if ( OpRightID != null ) {
		OpRightID = OpRightID.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();
	String IfFound = parameters.getValue ( "IfFound" );
	if ( IfFound == null ) {
		if ( (this instanceof SetDiversionRight_Command)||
			(this instanceof SetReservoirRight_Command) ||
			(this instanceof SetInstreamFlowRight_Command) ||
			(this instanceof SetWellRight_Command) ) {
			IfFound = _Warn; // Default
		}
		else {
			IfFound = _Ignore; // Default for fill (parameter not used)
		}
	}
	IfFound = IfFound.trim();
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
	List<StateMod_DiversionRight> ddrList = null;
	List<StateMod_ReservoirRight> rerList = null;
	List<StateMod_InstreamFlowRight> ifrList = null;
	List<StateMod_WellRight> werList = null;
    int rightListSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // Integer to increase performance in loop below
    try {
    	if ( (this instanceof FillDiversionRight_Command) ||
    		(this instanceof SetDiversionRight_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_DiversionRight> dataList = (List<StateMod_DiversionRight>)processor.getPropContents ( "StateMod_DiversionRight_List" );
    		ddrList = dataList;
    		compType = StateMod_DataSet.COMP_DIVERSION_RIGHTS;
    		rightListSize = ddrList.size();
    	}
    	else if ( (this instanceof FillReservoirRight_Command) ||
        	(this instanceof SetReservoirRight_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_ReservoirRight> dataList = (List<StateMod_ReservoirRight>)processor.getPropContents ( "StateMod_ReservoirRight_List" );
    		rerList = dataList;
    		compType = StateMod_DataSet.COMP_RESERVOIR_RIGHTS;
    		rightListSize = rerList.size();
    	}
    	else if ( (this instanceof FillInstreamFlowRight_Command) ||
        	(this instanceof SetInstreamFlowRight_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlowRight> dataList = (List<StateMod_InstreamFlowRight>)processor.getPropContents ( "StateMod_InstreamFlowRight_List" );
    		ifrList = dataList;
    		compType = StateMod_DataSet.COMP_INSTREAM_RIGHTS;
    		rightListSize = ifrList.size();
    	}
    	else if ( (this instanceof FillWellRight_Command) ||
        	(this instanceof SetWellRight_Command) ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List" );
    		werList = dataList;
    		compType = StateMod_DataSet.COMP_WELL_RIGHTS;
    		rightListSize = werList.size();
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting water right data to process (" + e + ").";
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
    	boolean fill_Name = false;
    	if ( Name != null ) {
    		fill_Name = true;
    	}
    	boolean fill_StationID = false;
    	if ( StationID != null ) {
    		fill_StationID = true;
    	}
    	boolean fill_AdministrationNumber = false;
    	if ( AdministrationNumber != null ) {
    		fill_AdministrationNumber = true;
    	}

    	boolean fill_Decree = false;
    	double Decree_double = 0.0;
    	if ( Decree != null ) {
    		fill_Decree = true;
    		Decree_double = Double.parseDouble ( Decree );
    	}

    	boolean fill_OnOff = false;
    	int OnOff_int = 0;
    	if ( OnOff != null ) {
    		fill_OnOff = true;
    		OnOff_int = Integer.parseInt ( OnOff );
    	}

    	boolean fill_AccountDist = false;
    	int AccountDist_int = 0;
    	if ( AccountDist != null ) {
    		fill_AccountDist = true;
    		AccountDist_int = Integer.parseInt ( AccountDist );
    	}

    	boolean fill_RightType = false;
    	int RightType_int = 0;
    	if ( RightType != null ) {
    		fill_RightType = true;
    		RightType_int = Integer.parseInt ( RightType );
    	}

    	boolean fill_FillType = false;
    	int FillType_int = 0;
    	if ( FillType != null ) {
    		fill_FillType = true;
    		FillType_int = Integer.parseInt  ( FillType );
    	}

    	boolean fill_OpRightID = false;
    	if ( OpRightID != null ) {
    		fill_OpRightID = true;
    	}
    	
    	StateMod_DiversionRight ddr = null;
    	StateMod_ReservoirRight rer = null;
    	StateMod_InstreamFlowRight ifr = null;
    	StateMod_WellRight wer = null;
    	String id = "";
    	int matchCount = 0;
    	String action = "Setting ";
    	boolean fill = false; // To increase performance and simplify logic
    	if ( (this instanceof FillDiversionRight_Command)||
    		(this instanceof FillReservoirRight_Command) ||
    		(this instanceof FillInstreamFlowRight_Command) ||
    		(this instanceof FillWellRight_Command) ) {
    		action = "Filling ";
    		fill = true;
    	}
    	for (int i = 0; i < rightListSize; i++) {
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_RIGHTS ){
    			ddr = ddrList.get(i);
    			id = ddr.getID();
    		}
    		else if ( compType == StateMod_DataSet.COMP_RESERVOIR_RIGHTS ){
    			rer = rerList.get(i);
    			id = rer.getID();
    		}
    		else if ( compType == StateMod_DataSet.COMP_INSTREAM_RIGHTS ) {
    			ifr = ifrList.get(i);
    			id = ifr.getID();
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_RIGHTS ){
    			wer = werList.get(i);
    			id = wer.getID();
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// This diversion right does not match...
    			continue;
    		}
    		// Match was found...
    		++matchCount;
    		if ( !fill && IfFound.equalsIgnoreCase(_Ignore) ) {
    			// Don't do anything
    			continue;
    		}
    		else if ( !fill && IfFound.equalsIgnoreCase(_Warn) ) {
    			// Setting data and a match was found.  Warn but do not set...
    			message = "Right \"" + ID + "\" was matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
    			continue;
    		}
    		else if ( !fill && IfFound.equalsIgnoreCase(_Fail) ) {
    			// Setting data and a match was found.  Warn but do not set...
    			message = "Right \"" + ID + "\" was matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
    			continue;
    		}
    		// Have a match so reset the data...
    		// TODO SAM 2009-01-12 Need to use interface for rights and avoid duplicate code.
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_RIGHTS ) {
	    		if ( fill_Name && (!fill || StateMod_Util.isMissing(ddr.getName())) ) {
	    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
	    			ddr.setName ( Name );
	    		}
	    		if ( fill_StationID && (!fill || StateMod_Util.isMissing(ddr.getCgoto())) ) {
	    			Message.printStatus ( 2, routine, action + id + " StationID -> " + StationID );
	    			if ( StationID.equalsIgnoreCase("ID") ) {
	    				// Use the station ID for the right...
	    				ddr.setCgoto(StringUtil.getToken(id, ".", 0,0));
	    			}
	    			else {
	    				// Match the exact string...
	    				ddr.setCgoto ( StationID );
	    			}
	    		}
	    		if ( fill_AdministrationNumber && (!fill || StateMod_Util.isMissing(ddr.getIrtem())) ) {
	    			Message.printStatus ( 2, routine, action + id + " AdministrationNumber -> " +
	    				AdministrationNumber );
	    			ddr.setIrtem ( AdministrationNumber );
	    		}
	    		if ( fill_Decree && (!fill || StateMod_Util.isMissing(ddr.getDcrdiv())) ){
	    			Message.printStatus ( 2, routine, action + id + " Decree -> " + Decree);
	    			ddr.setDcrdiv ( Decree_double );
	    		}
	    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(ddr.getSwitch())) ) {
	    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
	    			ddr.setSwitch ( OnOff_int );
	    		}
    		}
    		else if (compType == StateMod_DataSet.COMP_RESERVOIR_RIGHTS ) {
	    		if ( fill_Name && (!fill || StateMod_Util.isMissing(rer.getName())) ) {
	    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
	    			rer.setName ( Name );
	    		}
	    		if ( fill_StationID && (!fill || StateMod_Util.isMissing(rer.getCgoto())) ) {
	    			Message.printStatus ( 2, routine, action + id + " StationID -> " + StationID );
	    			if ( StationID.equalsIgnoreCase("ID") ) {
	    				// Use the station ID for the right...
	    				rer.setCgoto(StringUtil.getToken(id, ".", 0,0));
	    			}
	    			else {
	    				// Match the exact string...
	    				rer.setCgoto ( StationID );
	    			}
	    		}
	    		if ( fill_AdministrationNumber && (!fill || StateMod_Util.isMissing(rer.getRtem())) ) {
	    			Message.printStatus ( 2, routine,
	    			action + id + " AdministrationNumber -> " + AdministrationNumber );
	    			rer.setRtem ( AdministrationNumber );
	    		}
	    		if ( fill_Decree && (!fill || StateMod_Util.isMissing(rer.getDcrres())) ){
	    			Message.printStatus ( 2, routine, action + id + " Decree -> " + Decree );
	    			rer.setDcrres ( Decree_double );
	    		}
	    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(rer.getSwitch())) ) {
	    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
	    			rer.setSwitch ( OnOff_int );
	    		}
	    		if ( fill_AccountDist && (!fill || StateMod_Util.isMissing(rer.getIresco())) ) {
	    			Message.printStatus ( 2, routine, action + id + " AccountDist -> " + AccountDist );
	    			rer.setIresco ( AccountDist_int );
	    		}
	    		if ( fill_RightType && (!fill || StateMod_Util.isMissing(rer.getItyrstr())) ) {
	    			Message.printStatus ( 2, routine, action + id + " RightType -> " + RightType );
	    			rer.setItyrstr ( RightType_int );
	    		}
	    		if ( fill_FillType && (!fill || StateMod_Util.isMissing(rer.getN2fill())) ) {
	    			Message.printStatus ( 2, routine, action + id + " FillType -> " + FillType );
	    			rer.setN2fill ( FillType_int );
	    		}
	    		if ( fill_OpRightID && (!fill || StateMod_Util.isMissing(rer.getCopid())) ) {
	    			Message.printStatus ( 2, routine, action + id + " OpRightID -> " + OpRightID );
	    			rer.setCopid ( OpRightID );
	    		}
    		}
    		else if ( compType == StateMod_DataSet.COMP_INSTREAM_RIGHTS ) {
	    		if ( fill_Name && (!fill || StateMod_Util.isMissing(ifr.getName())) ) {
	    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
	    			ifr.setName ( Name );
	    		}
	    		if ( fill_StationID && (!fill || StateMod_Util.isMissing(ifr.getCgoto())) ) {
	    			Message.printStatus ( 2, routine, action + id + " StationID -> " + StationID );
	    			if ( StationID.equalsIgnoreCase("ID") ) {
	    				// Use the station ID for the right...
	    				ifr.setCgoto(StringUtil.getToken(id, ".", 0,0));
	    			}
	    			else {
	    				// Match the exact string...
	    				ifr.setCgoto ( StationID );
	    			}
	    		}
	    		if ( fill_AdministrationNumber && (!fill || StateMod_Util.isMissing(ifr.getIrtem())) ) {
	    			Message.printStatus ( 2, routine,
	    			action + id + " AdministrationNumber -> " + AdministrationNumber );
	    			ifr.setIrtem ( AdministrationNumber );
	    		}
	    		if ( fill_Decree && (!fill || StateMod_Util.isMissing(ifr.getDcrifr())) ){
	    			Message.printStatus ( 2, routine, action + id + " Decree -> "+Decree);
	    			ifr.setDcrifr ( Decree_double );
	    		}
	    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(ifr.getSwitch())) ) {
	    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
	    			ifr.setSwitch ( OnOff_int );
	    		}
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_RIGHTS ) {
	    		if ( fill_Name && (!fill || StateMod_Util.isMissing(wer.getName())) ) {
	    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
	    			wer.setName ( Name );
	    		}
	    		if ( fill_StationID && (!fill || StateMod_Util.isMissing(wer.getCgoto())) ) {
	    			Message.printStatus ( 2, routine, action + id + " StationID -> " + StationID );
	    			if ( StationID.equalsIgnoreCase("ID") ) {
	    				// Use the station ID for the right...
	    				wer.setCgoto(StringUtil.getToken(id, ".", 0,0));
	    			}
	    			else {
	    				// Match the exact string...
	    				wer.setCgoto ( StationID );
	    			}
	    		}
	    		if ( fill_AdministrationNumber && (!fill || StateMod_Util.isMissing(wer.getIrtem())) ) {
	    			Message.printStatus ( 2, routine,
	    			action + id + " AdministrationNumber -> " + AdministrationNumber );
	    			wer.setIrtem ( AdministrationNumber );
	    		}
	    		if ( fill_Decree && (!fill || StateMod_Util.isMissing(wer.getDcrdivw())) ){
	    			Message.printStatus ( 2, routine, action + id + " Decree -> "+Decree);
	    			wer.setDcrdivw ( Decree_double );
	    		}
	    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(wer.getSwitch())) ) {
	    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
	    			wer.setSwitch ( OnOff_int );
	    		}
    		}
    	}

    	// If nothing was matched and the idpattern does not contain a
    	// wildcard, add a StateMod_DiversionRight at the end...

    	if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) ) {
    		if ( IfNotFound.equalsIgnoreCase("Add") ) {
    			id = ID;
    			int pos = 0;	// Insert position for right.
    			if ( compType == StateMod_DataSet.COMP_DIVERSION_RIGHTS ) {
	    			ddr = new StateMod_DiversionRight();
	    			ddr.setID ( id );
	    			Message.printStatus ( 2, routine, "Adding diversion right " + ID );
	    			if ( fill_Name ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
	    				ddr.setName ( Name );
	    			}
	    			if ( fill_StationID ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " StationID -> " + StationID);
	    				if ( StationID.equalsIgnoreCase("ID") ) {
	    					// Use the station ID for the right...
	    					ddr.setCgoto( StringUtil.getToken(id, ".", 0,0));
	    				}
	    				else {
	    					// Match the exact string...
	    					ddr.setCgoto ( StationID );
	    				}
	    			}
	    			if ( fill_AdministrationNumber ) {
	    				Message.printStatus ( 2, routine,
	    				"Setting " + id + " AdministrationNumber -> " + AdministrationNumber );
	    				ddr.setIrtem ( AdministrationNumber );
	    			}
	    			if ( fill_Decree ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Decree -> " + Decree );
	    				ddr.setDcrdiv ( Decree_double );
	    			}
	    			if ( fill_OnOff ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
	    				ddr.setSwitch ( OnOff_int );
	    			}
	    			pos = StateMod_Util.findWaterRightInsertPosition( ddrList, ddr );
	    			if ( pos < 0 ) {
	    				// Insert at the end...
	    				Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
	    				+ ddr.getID() + "\" adding at end." );
	    				ddrList.add ( ddr );
	    			}
	    			else {
	    				// Do the insert at the given location...
	    				ddrList.add ( pos, ddr );
	    			}
    			}
    			else if ( compType == StateMod_DataSet.COMP_RESERVOIR_RIGHTS ) {
	    			rer = new StateMod_ReservoirRight();
	    			rer.setID ( id );
	    			Message.printStatus ( 2, routine, "Adding reservoir right " + ID );
	    			if ( fill_Name ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
	    				rer.setName ( Name );
	    			}
	    			if ( fill_StationID ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " StationID -> " + StationID);
	    				if ( StationID.equalsIgnoreCase("ID") ) {
	    					// Use the station ID for the right...
	    					rer.setCgoto( StringUtil.getToken( id, ".", 0,0));
	    				}
	    				else {
	    					// Match the exact string...
	    					rer.setCgoto ( StationID );
	    				}
	    			}
	    			if ( fill_AdministrationNumber ) {
	    				Message.printStatus ( 2, routine,
	    				"Setting " + id + " AdministrationNumber -> " + AdministrationNumber );
	    				rer.setRtem ( AdministrationNumber );
	    			}
	    			if ( fill_Decree ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Decree -> " + Decree );
	    				rer.setDcrres ( Decree_double );
	    			}
	    			if ( fill_OnOff ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
	    				rer.setSwitch ( OnOff_int );
	    			}
	    			if ( fill_AccountDist ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " AccountDist -> " + AccountDist );
	    				rer.setIresco ( AccountDist_int );
	    			}
	    			if ( fill_RightType ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " RightType -> " + RightType);
	    				rer.setItyrstr ( RightType_int );
	    			}
	    			if ( fill_FillType ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " FillType -> " + FillType );
	    				rer.setN2fill ( FillType_int );
	    			}
	    			if ( fill_OpRightID ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " OpRightID -> " + OpRightID);
	    				rer.setCopid ( OpRightID );
	    			}
	    			pos = StateMod_Util.findWaterRightInsertPosition( rerList, rer );
	    			if ( pos < 0 ) {
	    				// Insert at the end...
	    				Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
	    				+ rer.getID() + "\" adding at end." );
	    				rerList.add ( rer );
	    			}
	    			else {
	    				// Do the insert at the given location...
	    				rerList.add (pos, rer );
	    			}
    			}
    			else if ( compType == StateMod_DataSet.COMP_INSTREAM_RIGHTS ){
	    			ifr = new StateMod_InstreamFlowRight();
	    			id = ID;
	    			ifr.setID ( id );
	    			Message.printStatus ( 2, routine, "Adding instream flow right " + ID );
	    			if ( fill_Name ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
	    				ifr.setName ( Name );
	    			}
	    			if ( fill_StationID ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " StationID -> " + StationID);
	    				if ( StationID.equalsIgnoreCase("ID") ) {
	    					// Use the station ID for the right...
	    					ifr.setCgoto( StringUtil.getToken(id, ".", 0,0));
	    				}
	    				else {
	    					// Match the exact string...
	    					ifr.setCgoto ( StationID );
	    				}
	    			}
	    			if ( fill_AdministrationNumber ) {
	    				Message.printStatus ( 2, routine,
	    				"Setting " + id + " AdministrationNumber -> " + AdministrationNumber );
	    				ifr.setIrtem ( AdministrationNumber );
	    			}
	    			if ( fill_Decree ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Decree -> " + Decree );
	    				ifr.setDcrifr ( Decree_double );
	    			}
	    			if ( fill_OnOff ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
	    				ifr.setSwitch ( OnOff_int );
	    			}
	    			pos = StateMod_Util.findWaterRightInsertPosition( ifrList, ifr );
	    			if ( pos < 0 ) {
	    				// Insert at the end...
	    				Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
	    				+ ifr.getID() + "\" adding at end." );
	    				ifrList.add ( ifr );
	    			}
	    			else {
	    				// Do the insert at the given location...
	    				ifrList.add(pos,ifr );
	    			}
    			}
    			else if ( compType == StateMod_DataSet.COMP_WELL_RIGHTS ) {
	    			wer = new StateMod_WellRight();
	    			wer.setID ( id );
	    			Message.printStatus ( 2, routine, "Adding well right " + ID );
	    			if ( fill_Name ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
	    				wer.setName ( Name );
	    			}
	    			if ( fill_StationID ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " StationID -> " + StationID);
	    				if ( StationID.equalsIgnoreCase("ID") ) {
	    					// Use the station ID for the right...
	    					wer.setCgoto( StringUtil.getToken(id, ".", 0,0));
	    				}
	    				else {
	    					// Match the exact string...
	    					wer.setCgoto ( StationID );
	    				}
	    			}
	    			if ( fill_AdministrationNumber ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " AdministrationNumber -> " +
	    				AdministrationNumber );
	    				wer.setIrtem ( AdministrationNumber );
	    			}
	    			if ( fill_Decree ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " Decree -> " + Decree );
	    				wer.setDcrdivw ( Decree_double );
	    			}
	    			if ( fill_OnOff ) {
	    				Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
	    				wer.setSwitch ( OnOff_int );
	    			}
	    			pos = StateMod_Util.findWaterRightInsertPosition(werList, wer );
	    			if ( pos < 0 ) {
	    				// Insert at the end...
	    				Message.printStatus ( 2, routine, "Cannot determine insert position for right \""
	    				+ wer.getID() + "\" adding at end." );
	    				werList.add ( wer );
	    			}
	    			else {
	    				// Do the insert at the given location...
	    				werList.add(pos,wer );
	    			}
    			}
    		}
    		else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
    			message = "Right \"" + ID + "\" was not matched: warning and not adding.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
    		}
    		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
    			message = "Right \"" + ID + "\" was not matched: failing and not adding.";
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
        message = "Unexpected error processing water right data (" + e + ").";
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
	String Name = parameters.getValue ( "Name" );
	String StationID = parameters.getValue ( "StationID" );
	String AdministrationNumber = parameters.getValue ( "AdministrationNumber" );
	String Decree = parameters.getValue ( "Decree" );
	String OnOff = parameters.getValue ( "OnOff" );
	String AccountDist = parameters.getValue ( "AccountDist" ); // Reservoirs
	String RightType = parameters.getValue ( "RightType" ); // Reservoirs
	String FillType = parameters.getValue ( "FillType" ); // Reservoirs
	String OpRightID = parameters.getValue ( "OpRightID" ); // Reservoirs
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String IfFound = parameters.getValue ( "IfFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (Name != null) && (Name.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Name=\"" + Name + "\"" );
	}
	if ( (StationID != null) && (StationID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "StationID=\"" + StationID + "\"" );
	}
	if ( (AdministrationNumber != null) && (AdministrationNumber.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AdministrationNumber=" + AdministrationNumber );
	}
	if ( (Decree != null) && (Decree.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Decree=" + Decree );
	}
	if ( (OnOff != null) && (OnOff.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOff=" + OnOff );
	}
	if ( (this instanceof FillReservoirRight_Command) || (this instanceof SetReservoirRight_Command) ) {
		if ( (AccountDist != null) && (AccountDist.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "AccountDist=\"" + AccountDist + "\"" );
		}
		if ( (RightType != null) && (RightType.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "RightType=" + RightType );
		}
		if ( (FillType != null) && (FillType.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "FillType=" + FillType );
		}
		if ( (OpRightID != null) && (OpRightID.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "OpRightID=\"" + OpRightID + "\"" );
		}
	}

	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	if ( (this instanceof SetDiversionRight_Command)||
		(this instanceof SetReservoirRight_Command) ||
		(this instanceof SetInstreamFlowRight_Command) ||
		(this instanceof SetWellRight_Command) ) {
		if ( (IfFound != null) && (IfFound.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "IfFound=" + IfFound );
		}
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
