// FillAndSetPlanStation_Command - This class initializes, checks, and runs the FillPlanStation() and SetPlanStation()commands.

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

import DWR.StateMod.StateMod_Plan;
import DWR.StateMod.StateMod_Util;

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
This class initializes, checks, and runs the FillPlanStation() and SetPlanStation()commands.
The functionality is handled in one class due to the
close similarity between the commands.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetPlanStation()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
*/
public abstract class FillAndSetPlanStation_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Monthly efficiencies as doubles, from data check, to be used when running.
*/
private double[] __EffMonthly_double;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetPlanStation_Command ()
{	super();
	setCommandName ( "?Fill/Set?Plan?Station" );
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
	String OnOff = parameters.getValue ( "OnOff" );
	String InitialStorage = parameters.getValue ( "InitialStorage" );
	String FailureSwitch = parameters.getValue ( "FailureSwitch" );
	String PlanType = parameters.getValue ( "PlanType" );
	String ReturnType = parameters.getValue ( "ReturnType" );
	String EffType = parameters.getValue ( "EffType" );
	String EffMonthly = parameters.getValue ( "EffMonthly" );
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
                message, "Specify the ID to process." ) );
	}
	
	if ( (OnOff != null) && (OnOff.length() != 0) ) {
		if ( !StringUtil.isInteger(OnOff) ) {
	        message = "The on/off parameter (" + OnOff + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the on/off value as 0 or 1." ) );
		}
		else {
			int onoff = Integer.parseInt(OnOff);
			if ( onoff < 0 || onoff > 1 ) {
		        message = "The on/off parameter (" + OnOff + ") is invalid.";
		        warning += "\n" + message;
		        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the on/off value as 0 or 1." ) );
			}
		}
	}
	
	if ( (PlanType != null) && (PlanType.length() != 0) && !StringUtil.isInteger(PlanType) ) {
        message = "The demand type (" + PlanType + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the demand type as an integer." ) );
	}
	
	if ( (EffType != null) && (EffType.length() != 0) && !StringUtil.isInteger(EffType) ) {
        message = "The efficiency type (" + EffType + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the annual efficiency as an integer." ) );
	}
	
	if ( (EffType != null) && (EffType.length() >= 0) && !EffType.equals("1") &&
		(EffMonthly != null) && (EffMonthly.length() >= 0) ) {
        message = "The monthly efficiencies should only be specified when EffType=1.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify EffType=1 and monthly efficiencies as percent 0-100, " +
                	"or use a different EffType value." ) );
	}
	
	if ( (EffType != null) && (EffType.length() >= 0) && EffType.equals("1") &&
		((EffMonthly == null) || (EffMonthly.length() == 0)) ) {
        message = "The monthly efficiencies must be specified when EffType=1.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify EffType=1 and monthly efficiencies as percent 0-100, " +
                	"or use a different EffType value." ) );
	}
	
	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
		// Make sure 12 numbers are specified...
		List<String> tokens = StringUtil.breakStringList(EffMonthly, ", ", 0);
		int ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}
		if ( ntokens != 12 ) {
			message = "12 monthly efficiencies must be specified.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify 12 monthly efficiencies as percent 0-100." ) );
		}
		else {
			__EffMonthly_double = new double[12];
			for ( int i = 0; i < 12; i++ ) {
				String eff = (tokens.get(i)).trim();
				if ( !StringUtil.isDouble(eff) ) {
					message = "Monthly efficiency (" + tokens.get(i) + ") is invalid.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
		                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the monthly efficiencies as percent 0-100." ) );
				}
				else {
					__EffMonthly_double[i] = Double.parseDouble(eff);
				}
			}
		}
	}
	
	if ( (ReturnType != null) && (ReturnType.length() != 0) && !StringUtil.isInteger(ReturnType) ) {
        message = "The use type (" + ReturnType + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the use type as an integer." ) );
	}
	
	if ( (FailureSwitch != null) && (FailureSwitch.length() != 0) ) {
		if ( !StringUtil.isInteger(FailureSwitch) ) {
	        message = "The failure switch (" + FailureSwitch + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the failure switch as an integer 0 or 1." ) );
		}
		else {
			int failureSwitch = Integer.parseInt(FailureSwitch);
			if ( failureSwitch < 0 || failureSwitch > 1 ) {
		        message = "The failure switch (" + FailureSwitch + ") is invalid.";
		        warning += "\n" + message;
		        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the failure switch as 0 or 1." ) );
			}
		}
	}
	
	if ( (InitialStorage != null) && (InitialStorage.length() != 0) && !StringUtil.isDouble(InitialStorage) ) {
        message = "The initial storage (" + InitialStorage + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the initial storage as a number." ) );
	}
	
	if ( this instanceof SetPlanStation_Command ) {
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
	else {
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
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(13);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "RiverNodeID" );
	valid_Vector.add ( "OnOff" );
	valid_Vector.add ( "PlanType" );
	valid_Vector.add ( "EffType" );
	valid_Vector.add ( "EffMonthly" );
	valid_Vector.add ( "ReturnType" );
	valid_Vector.add ( "FailureSwitch" );
	valid_Vector.add ( "InitialStorage" );
	valid_Vector.add ( "SourceID" );
	valid_Vector.add ( "SourceAccount" );
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
	return (new FillAndSetPlanStation_JDialog ( parent, this )).ok();
}

/**
Process the plan stations.
*/
private int processPlanStations ( List<StateMod_Plan> planList, boolean fill,
		int warningCount, int warningLevel, String commandTag, CommandStatus status,
		String ID, String idpattern_Java,
		String Name, boolean fill_Name,
		String RiverNodeID, boolean fill_RiverNodeID,
		String OnOff, int OnOff_int, boolean fill_OnOff,
		String PlanType, int PlanType_int, boolean fill_PlanType,
		String EffType, int EffType_int, boolean fill_EffType,
		String EffMonthly, double [] EffMonthly_double, boolean fill_EffMonthly,
		String ReturnType, int ReturnType_int, boolean fill_ReturnType,
		String FailureSwitch, int FailureSwitch_int, boolean fill_FailureSwitch,
		String InitialStorage, double InitialStorage_double, boolean fill_InitialStorage,
		String SourceID, boolean fill_SourceID,
		String SourceAccount, boolean fill_SourceAccount,
		String IfNotFound )
{	String routine = "FillAndSetPlanStation_Command.processPlanStations";
	StateMod_Plan plan = null;
	String id;
	int matchCount = 0;
	String action = "Setting ";
	if ( fill ) {
		action = "Filling ";
	}
	
	int planListSize = planList.size();
	for (int i = 0; i < planListSize; i++) {
		plan = planList.get(i);
		id = plan.getID();
		if ( !id.matches(idpattern_Java) ) {
			// Identifier does not match...
			continue;
		}
		++matchCount;
		// Have a match so reset the data...
		if ( fill_Name && (!fill || StateMod_Util.isMissing(plan.getName())) ) {
			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
			plan.setName ( Name );
		}
		if ( fill_RiverNodeID && (!fill || StateMod_Util.isMissing(plan.getCgoto())) ) {
			Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID );
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + plan.getID());
				plan.setCgoto ( plan.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID);
				plan.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(plan.getSwitch())) ) {
			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
			plan.setSwitch ( OnOff_int );
		}
		if ( fill_PlanType && (!fill || StateMod_Util.isMissing(plan.getIPlnTyp())) ){
			Message.printStatus ( 2, routine, action + id + " PlanType -> " + PlanType );
			plan.setIPlnTyp ( PlanType_int );
		}
		if ( fill_EffType && (!fill || StateMod_Util.isMissing(plan.getPeffFlag())) ) {
			Message.printStatus ( 2, routine, action + id + " EffType -> " + EffType);
			plan.setPeffFlag ( EffType_int );
		}
		if ( fill_EffMonthly && (!fill || StateMod_Util.isMissing(plan.getPeff(0))) ){
			Message.printStatus ( 2, routine, action + id + " EffMonthly -> " + EffMonthly);
			// Always stored in calendar...
			for ( int ieff = 0; ieff < 12; ieff++ ) {
				plan.setPeff ( ieff, EffMonthly_double[ieff] );
			}
		}
		if ( fill_ReturnType && (!fill || StateMod_Util.isMissing(plan.getIPrf())) ){
			Message.printStatus ( 2, routine, action + id + " ReturnType -> " + ReturnType );
			plan.setIPrf ( ReturnType_int );
		}
		if ( fill_FailureSwitch && (!fill || StateMod_Util.isMissing(plan.getIPfail())) ){
			Message.printStatus ( 2, routine, action + id + " FailureSwitch -> " + FailureSwitch);
			plan.setIPfail ( FailureSwitch_int );
		}
		if ( fill_InitialStorage && (!fill || StateMod_Util.isMissing(plan.getPsto1())) ) {
			Message.printStatus ( 2, routine, action + id + " InitialStorage -> " + InitialStorage );
			plan.setPsto1 ( InitialStorage_double );
		}
		if ( fill_SourceID && (!fill || StateMod_Util.isMissing(plan.getPsource())) ){
			if ( SourceID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " SourceID -> " + plan.getID() );
				plan.setPsource ( plan.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " SourceID -> " + SourceID );
				plan.setPsource ( SourceID );
			}
		}
		if ( fill_SourceAccount && (!fill || StateMod_Util.isMissing(plan.getIPAcc())) ){
			Message.printStatus ( 2, routine, action + id + " SourceAccount -> " + SourceAccount );
			plan.setIPAcc ( SourceAccount );
		}
	}

	if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
    	// If nothing was matched for set command and the idpattern does not contain a
    	// wildcard, add a StateMod_Plan at the end...
		plan = new StateMod_Plan ( false );
		id = ID;
		plan.setID ( id );
		planList.add ( plan );
		Message.printStatus ( 2, routine, "Adding plan station " + id );
		if ( fill_Name ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
			plan.setName ( Name );
		}
		if ( fill_RiverNodeID ) {
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + plan.getID());
				plan.setCgoto ( plan.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID);
				plan.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff ) {
			Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
			plan.setSwitch ( OnOff_int );
		}
		if ( fill_PlanType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " PlanType -> " + PlanType);
			plan.setIPlnTyp ( PlanType_int );
		}
		if ( fill_EffType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffType -> " + EffType);
			plan.setPeffFlag ( EffType_int );
		}
		if ( fill_EffMonthly ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffMonthly -> " + EffMonthly);
			for ( int ieff = 0; ieff < 12; ieff++ ){
				plan.setPeff ( ieff, EffMonthly_double[ieff] );
			}
		}
		if ( fill_ReturnType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " ReturnType -> " + ReturnType );
			plan.setIPrf ( ReturnType_int );
		}
		if ( fill_FailureSwitch ) {
			Message.printStatus ( 2, routine, "Setting " + id + " FailureSwitch -> " + FailureSwitch );
			plan.setIPfail ( FailureSwitch_int );
		}
		if ( fill_InitialStorage ) {
			Message.printStatus ( 2, routine, "Setting " + id + " InitialStorage -> " + InitialStorage );
			plan.setPsto1( InitialStorage_double );
		}
		if ( fill_SourceID ) {
			if ( SourceID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " SourceID -> " + plan.getID() );
				plan.setPsource( plan.getID() );
			}
			else {
				Message.printStatus ( 2, routine, "Setting " + id + " SourceID -> " + SourceID );
				plan.setPsource ( SourceID );
			}
		}
		if ( fill_SourceAccount ) {
			Message.printStatus ( 2, routine, "Setting " + id + " SourceAccount -> " + SourceAccount );
			plan.setIPAcc ( SourceAccount );
		}
	}
	if ( matchCount == 0 ) {
		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
			String message = "Plan \"" + ID + "\" was not matched: warning and not adding.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the identifier is correct." ) );
		}
		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
			String message = "Plan \"" + ID +	"\" was not matched: failing and not adding.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that the identifier is correct." ) );
		}
	}
	return warningCount;
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
    
    // Trim strings because Ray Bennett has written FORTRAN programs to process command files, and there
    // are sometimes extra spaces in the parameter values - this causes IDs to not be matched, etc.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" ).trim();
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String Name = parameters.getValue ( "Name" );
	if ( Name != null ) {
		Name = Name.trim();
	}
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	if ( RiverNodeID != null ) {
		RiverNodeID = RiverNodeID.trim();
	}
	String OnOff = parameters.getValue ( "OnOff" );
	if ( OnOff != null ) {
		OnOff = OnOff.trim();
	}
	String PlanType = parameters.getValue ( "PlanType" );
	if ( PlanType != null ) {
		PlanType = PlanType.trim();
	}
	String EffType = parameters.getValue ( "EffType" );
	if ( EffType != null ) {
		EffType = EffType.trim();
	}
	String EffMonthly = parameters.getValue ( "EffMonthly" );
	if ( EffMonthly != null ) {
		EffMonthly = EffMonthly.trim();
	}
	String ReturnType = parameters.getValue ( "ReturnType" );
	if ( ReturnType != null ) {
		ReturnType = ReturnType.trim();
	}
	String FailureSwitch = parameters.getValue ( "FailureSwitch" );
	if ( FailureSwitch != null ) {
		FailureSwitch = FailureSwitch.trim();
	}
	String InitialStorage = parameters.getValue ( "InitialStorage" );
	if ( InitialStorage != null ) {
		InitialStorage = InitialStorage.trim();
	}
	String SourceID = parameters.getValue ( "SourceID" );
	if ( SourceID != null ) {
		SourceID = SourceID.trim();
	}
	String SourceAccount = parameters.getValue ( "SourceAccount" );
	if ( SourceAccount != null ) {
		SourceAccount = SourceAccount.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();

    // Get the data needed for the command
    
    List<StateMod_Plan> planList = null;
    try {
    	@SuppressWarnings("unchecked")
		List<StateMod_Plan> dataList = (List<StateMod_Plan>)processor.getPropContents ( "StateMod_PlanStation_List" );
    	planList = dataList;
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
    	boolean fill_Name = false;
    	if ( Name != null ) {
    		fill_Name = true;
    	}
    	boolean fill_RiverNodeID = false;
    	if ( RiverNodeID != null ) {
    		fill_RiverNodeID = true;
    	}
    	boolean fill_OnOff = false;
    	int OnOff_int = 0;
    	if ( (OnOff != null) && (OnOff.length() > 0) ) {
    		fill_OnOff = true;
    		OnOff_int = Integer.parseInt(OnOff);
    	}
    	
    	boolean fill_PlanType = false;
    	int PlanType_int = 0;
    	if ( (PlanType != null) && (PlanType.length() > 0) ) {
    		fill_PlanType = true;
    		PlanType_int = Integer.parseInt( PlanType );
    	}

    	boolean fill_EffType = false;
    	int EffType_int = 0;
    	if ( (EffType != null) && (EffType.length() > 0) ) {
    		fill_EffType = true;
    		EffType_int = Integer.parseInt(EffType);
    	}

    	boolean fill_EffMonthly = false;
    	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
    		fill_EffMonthly = true;
    	}
    	
    	boolean fill_ReturnType = false;
    	int ReturnType_int = 0;
    	if ( (ReturnType != null) && (ReturnType.length() > 0) ) {
    		fill_ReturnType = true;
    		ReturnType_int = Integer.parseInt ( ReturnType );
    	}
    	
    	boolean fill_FailureSwitch = false;
    	int FailureSwitch_int = 0;
    	if ( (FailureSwitch != null) && (FailureSwitch.length() > 0) ) {
    		fill_FailureSwitch = true;
    		FailureSwitch_int = Integer.parseInt(FailureSwitch);
    	}

    	boolean fill_InitialStorage = false;
    	double InitialStorage_double = 0.0;
    	if ( (InitialStorage != null) && (InitialStorage.length() > 0) ) {
    		fill_InitialStorage = true;
    		InitialStorage_double = Double.parseDouble ( InitialStorage );
    	}
    	
    	boolean fill_SourceID = false;
    	if ( SourceID != null ) {
    		fill_SourceID = true;
    	}
    	
    	boolean fill_SourceAccount = false;
    	if ( SourceAccount != null ) {
    		fill_SourceAccount = true;
    	}

    	boolean fill = true;
    	if ( this instanceof SetPlanStation_Command ) {
    		fill = false;
    	}
    	
		warning_count = processPlanStations ( planList, fill,
			warning_count, warning_level, command_tag, status,
			ID, idpattern_Java,
			Name, fill_Name,
			RiverNodeID, fill_RiverNodeID,
			OnOff, OnOff_int, fill_OnOff,
			PlanType, PlanType_int, fill_PlanType,
			EffType, EffType_int, fill_EffType,
			EffMonthly, __EffMonthly_double, fill_EffMonthly,
			ReturnType, ReturnType_int, fill_ReturnType,
			FailureSwitch, FailureSwitch_int, fill_FailureSwitch,
			InitialStorage, InitialStorage_double, fill_InitialStorage,
			SourceID, fill_SourceID,
			SourceAccount, fill_SourceAccount,
			IfNotFound  );
		if (this instanceof SetPlanStation_Command) {
			Message.printStatus(2, routine, "After set command have " + planList.size() + " plans.");
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	String OnOff = parameters.getValue ( "OnOff" );
	String PlanType = parameters.getValue ( "PlanType" );
	String EffType = parameters.getValue ( "EffType" );
	String EffMonthly = parameters.getValue ( "EffMonthly" );
	String ReturnType = parameters.getValue ( "ReturnType" );
	String FailureSwitch = parameters.getValue ( "FailureSwitch" );
	String InitialStorage = parameters.getValue ( "InitialStorage" );
	String SourceID = parameters.getValue ( "SourceID" );
	String SourceAccount = parameters.getValue ( "SourceAccount" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
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
	if ( (RiverNodeID != null) && (RiverNodeID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RiverNodeID=\"" + RiverNodeID + "\"" );
	}
	if ( (OnOff != null) && (OnOff.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOff=" + OnOff );
	}
	if ( (PlanType != null) && (PlanType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PlanType=" + PlanType );
	}
	if ( (EffType != null) && (EffType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffType=" + EffType );
	}
	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffMonthly=\"" + EffMonthly + "\"" );
	}
	if ( (ReturnType != null) && (ReturnType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReturnType=" + ReturnType );
	}
	if ( (FailureSwitch != null) && (FailureSwitch.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FailureSwitch=" + FailureSwitch );
	}
	if ( (InitialStorage != null) && (InitialStorage.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InitialStorage=" + InitialStorage );
	}
	if ( (SourceID != null) && (SourceID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SourceID=\"" + SourceID + "\"" );
	}
	if ( (SourceAccount != null) && (SourceAccount.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SourceAccount=\"" + SourceAccount + "\"" );
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
