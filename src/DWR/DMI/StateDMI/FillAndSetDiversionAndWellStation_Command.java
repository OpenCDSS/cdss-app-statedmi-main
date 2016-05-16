package DWR.DMI.StateDMI;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_ReturnFlow;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.YearType;
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
This class initializes, checks, and runs the FillDiversionStation(), SetDiversionStation(),
FillWellStation(), and SetWellStation() commands.  The functionality is handled in one class due to the
close similarity between the commands and of diversions and wells in StateMod.  It is an abstract
base class that must be controlled via a derived class.  For example, the SetDiversionStation()
command extends this class in order to uniquely represent the command, but much of the functionality
is in this base class.
*/
public abstract class FillAndSetDiversionAndWellStation_Command extends AbstractCommand implements Command
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
Return node, percent, and table, from data check, to be used when running.
*/
private String[] __ReturnsNodeID;
private String[] __ReturnsTableID;
private double[] __ReturnsPercent_double;

/**
Depletion node, percent, and table, from data check, to be used when running.
*/
private String[] __DepletionsNodeID;
private String[] __DepletionsTableID;
private double[] __DepletionsPercent_double;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetDiversionAndWellStation_Command ()
{	super();
	setCommandName ( "?Fill/Set?Diversion/Well?Station" );
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
	String Capacity = parameters.getValue ( "Capacity" );
	String ReplaceResOption = parameters.getValue ( "ReplaceResOption" );
	String AdminNumShift = parameters.getValue ( "AdminNumShift" );
	// TODO SAM 2008-12-22 Need to add more specific validation for option parameters
	String DemandType = parameters.getValue ( "DemandType" );
	String IrrigatedAcres = parameters.getValue ( "IrrigatedAcres" );
	String UseType = parameters.getValue ( "UseType" );
	String DemandSource = parameters.getValue ( "DemandSource" );
	String EffAnnual = parameters.getValue ( "EffAnnual" );
	String EffMonthly = parameters.getValue ( "EffMonthly" );
	String Returns = parameters.getValue ( "Returns" );
	String Depletions = parameters.getValue ( "Depletions" );
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
	
	if ( (OnOff != null) && (OnOff.length() != 0) && !StringUtil.isInteger(OnOff) ) {
        message = "The on/off parameter (" + OnOff + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the on/off value as an integer." ) );
	}
	
	if ( (Capacity != null) && (Capacity.length() != 0) && !StringUtil.isDouble(Capacity) ) {
        message = "The capacity (" + Capacity + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the capacity as a number." ) );
	}
	
	if ( (this instanceof FillDiversionStation_Command) || (this instanceof SetDiversionStation_Command) ) {
		if ( (ReplaceResOption != null) && (ReplaceResOption.length() != 0) &&
			!StringUtil.isInteger(ReplaceResOption) ) {
	        message = "The replacement reservoir option (" + ReplaceResOption + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the replacement reservoir option as an integer." ) );
		}
	}
	
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		if ( (AdminNumShift != null) && (AdminNumShift.length() != 0) && !StringUtil.isDouble(AdminNumShift) ) {
	        message = "The administration number shift (" + AdminNumShift + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the administration number shift as a number." ) );
		}
	}
	
	if ( (DemandType != null) && (DemandType.length() != 0) && !StringUtil.isInteger(DemandType) ) {
        message = "The demand type (" + DemandType + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the demand type as an integer." ) );
	}
	
	if ( (IrrigatedAcres != null) && (IrrigatedAcres.length() != 0) && !StringUtil.isDouble(IrrigatedAcres) ) {
        message = "The irrigated acres (" + IrrigatedAcres + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the irrigated acres as a number." ) );
	}
	
	if ( (UseType != null) && (UseType.length() != 0) && !StringUtil.isInteger(UseType) ) {
        message = "The use type (" + UseType + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the use type as an integer." ) );
	}
	
	if ( (DemandSource != null) && (DemandSource.length() != 0) && !StringUtil.isInteger(DemandSource) ) {
        message = "The demand source (" + DemandSource + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the demand source as an integer." ) );
	}
	
	if ( (EffAnnual != null) && (EffAnnual.length() != 0) && !StringUtil.isDouble(EffAnnual) ) {
        message = "The annual efficiency (" + EffAnnual + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the annual efficiency as a percent 0-100." ) );
	}
	
	if ( (EffAnnual != null) && (EffAnnual.length() >= 0) &&
			(EffMonthly != null) && (EffMonthly.length() >= 0) ) {
        message = "The annual and monthly efficiencies cannot both be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the annual OR monthly efficiencies as percent 0-100." ) );
	}
	
	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
		// Make sure 12 numbers are specified...
		List tokens = StringUtil.breakStringList(EffMonthly, ", ", 0);
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
				String eff = ((String)tokens.get(i)).trim();
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
	
	if ( (Returns != null) && (Returns.length() > 0) ) {
		// Make sure that values are in pairs of three...
		List tokens = StringUtil.breakStringList(Returns, ",; ", StringUtil.DELIM_SKIP_BLANKS);
		if ( (tokens == null) || (tokens.size()%3 != 0) ) {
			message = "Return data must be specified as triplets of location ID, percent, and table ID.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE, message,
	                "Specify return data as triplets of location ID, percent, and table ID." ) );
		}
		else {
			int nreturn = tokens.size()/3;
			__ReturnsNodeID = new String[nreturn];
			__ReturnsTableID = new String[nreturn];
			__ReturnsPercent_double = new double[nreturn];
			for ( int i = 0; i < nreturn; i++ ) {
				// TODO SAM 2004-06-06 Need to check return location if a network is supplied...
				__ReturnsNodeID[i] = (String)tokens.get(i*3);
				// Check the percent...
				String val = ((String)tokens.get(i*3 + 1)).trim();
				if ( !StringUtil.isDouble(val ) ) {
					message = "Return percent (" + val + ") is invalid.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the return value as percent 0-100." ) );
				}
				else {
					__ReturnsPercent_double[i] = Double.parseDouble ( val );
				}

				// TODO SAM 2004-06-07 Need to check the return table identifier if it is supplied.
				val = (String)tokens.get(i*3 + 2);
				if ( !StringUtil.isInteger(val ) ) {
					message = "Return table identifier (" + val + ") is invalid.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the return table identifier as an integer." ) );
				}
				else {
					__ReturnsTableID[i] = val;
				}
			}
		}
	}
	
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		if ( (Depletions != null) && (Depletions.length() > 0) ) {
			// Make sure that values are in pairs of three...
			List tokens = StringUtil.breakStringList(Depletions, ",; ", StringUtil.DELIM_SKIP_BLANKS);
			if ( (tokens == null) || (tokens.size()%3 != 0) ) {
				message = "Depletion data must be specified as triplets of location ID, percent, and table ID.";
		        warning += "\n" + message;
		        status.addToLog ( CommandPhaseType.INITIALIZATION,
		            new CommandLogRecord(CommandStatusType.FAILURE, message,
		                "Specify depletion data as triplets of location ID, percent, and table ID." ) );
			}
			else {
				int nreturn = tokens.size()/3;
				__DepletionsNodeID = new String[nreturn];
				__DepletionsTableID = new String[nreturn];
				__DepletionsPercent_double = new double[nreturn];
				for ( int i = 0; i < nreturn; i++ ) {
					// TODO SAM 2004-06-06 Need to check depletion location if a network is supplied...
					__DepletionsNodeID[i] = (String)tokens.get(i*3);
					// Check the percent...
					String val = ((String)tokens.get(i*3 + 1)).trim();
					if ( !StringUtil.isDouble(val ) ) {
						message = "Depletion percent (" + val + ") is not an integer.";
				        warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify the depletion value as percent 0-100." ) );
					}
					else {
						__DepletionsPercent_double[i] = Double.parseDouble ( val );
					}
	
					// TODO SAM 2004-06-07 Need to check the depletion table identifier if it is supplied.
					val = (String)tokens.get(i*3 + 2);
					if ( !StringUtil.isInteger(val ) ) {
						message = "Depletion table identifier (" + val + ") is not an integer.";
				        warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify the depletion table identifier as an integer." ) );
					}
					else {
						__DepletionsTableID[i] = val;
					}
				}
			}
		}
	}

	if ( (this instanceof SetDiversionStation_Command) || (this instanceof SetWellStation_Command) ) {
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
	List valid_Vector = new Vector();
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "Name" );
	valid_Vector.add ( "RiverNodeID" );
	valid_Vector.add ( "OnOff" );
	valid_Vector.add ( "Capacity" );
	if ( (this instanceof FillDiversionStation_Command) || (this instanceof SetDiversionStation_Command) ) {
		valid_Vector.add ( "ReplaceResOption" );
	}
	valid_Vector.add ( "DailyID" );
	if ( (this instanceof FillDiversionStation_Command) || (this instanceof SetDiversionStation_Command) ) {
		valid_Vector.add ( "UserName" );	
	}
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		valid_Vector.add ( "AdminNumShift" );
		valid_Vector.add ( "DiversionID" );
	}
	valid_Vector.add ( "DemandType" );
	valid_Vector.add ( "IrrigatedAcres" );
	valid_Vector.add ( "UseType" );
	valid_Vector.add ( "DemandSource" );
	valid_Vector.add ( "EffAnnual" );
	valid_Vector.add ( "EffMonthly" );
	valid_Vector.add ( "Returns" );
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		valid_Vector.add ( "Depletions" );
	}
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
	return (new FillAndSetDiversionAndWellStation_JDialog ( parent, this )).ok();
}

/**
Process the diversion stations.
*/
private int processDiversionStations ( List divList, boolean fill, YearType outputYearType,
		int warningCount, int warningLevel, String commandTag, CommandStatus status,
		String ID, String idpattern_Java,
		String Name, boolean fill_Name,
		String RiverNodeID, boolean fill_RiverNodeID,
		String OnOff, int OnOff_int, boolean fill_OnOff,
		String Capacity, double Capacity_double, boolean fill_Capacity,
		String ReplaceResOption, int ReplaceResOption_int, boolean fill_ReplaceResOption,
		String DailyID, boolean fill_DailyID,
		String UserName, boolean fill_UserName,
		String AdminNumShift, double AdminNumShift_double, boolean fill_AdminNumShift,
		String DemandType, int DemandType_int, boolean fill_DemandType,
		String IrrigatedAcres, double IrrigatedAcres_double, boolean fill_IrrigatedAcres,
		String UseType, int UseType_int, boolean fill_UseType,
		String DemandSource, int DemandSource_int, boolean fill_DemandSource,
		String EffAnnual, double EffAnnual_double, boolean fill_EffAnnual,
		String EffMonthly, double [] EffMonthly_double, boolean fill_EffMonthly,
		String Returns, String [] ReturnsNodeID, double [] ReturnsPercent, String [] ReturnsTableID, boolean fill_Returns,
		String IfNotFound )
{	String routine = "FillAndSetDiversionAndWellStation_Command.processWellStations";
	StateMod_Diversion div = null;
	String id;
	int matchCount = 0;
	String action = "Setting ";
	if ( fill ) {
		action = "Filling ";
	}
	
	int divListSize = divList.size();
	for (int i = 0; i < divListSize; i++) {
		div = (StateMod_Diversion)divList.get(i);
		id = div.getID();
		if ( !id.matches(idpattern_Java) ) {
			// Identifier does not match...
			continue;
		}
		++matchCount;
		// Have a match so reset the data...
		if ( fill_Name && (!fill || StateMod_Util.isMissing(div.getName())) ) {
			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
			div.setName ( Name );
		}
		if ( fill_RiverNodeID && (!fill || StateMod_Util.isMissing(div.getCgoto())) ) {
			Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID );
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + div.getID());
				div.setCgoto ( div.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID);
				div.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(div.getSwitch())) ) {
			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
			div.setSwitch ( OnOff_int );
		}
		if ( fill_Capacity && (!fill || StateMod_Util.isMissing(div.getDivcap())) ) {
			Message.printStatus ( 2, routine,
			action + id + " Capacity -> " + Capacity );
			div.setDivcap ( Capacity_double );
		}
		if ( fill_ReplaceResOption && (!fill || StateMod_Util.isMissing(div.getIreptype())) ){
			Message.printStatus ( 2, routine, action + id + " ReplaceResOption -> "+ReplaceResOption);
			div.setIreptype ( ReplaceResOption_int );
		}
		if ( fill_DailyID && (!fill || StateMod_Util.isMissing(div.getCdividy())) ){
			if ( DailyID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + div.getID() );
				div.setCdividy ( div.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + DailyID );
				div.setCdividy ( DailyID );
			}
		}
		if ( fill_UserName && (!fill || StateMod_Util.isMissing(div.getUsername())) ){
			Message.printStatus ( 2, routine, action + id + " UserName -> " + UserName );
			div.setUsername ( UserName );
		}
		if ( fill_DemandType && (!fill || StateMod_Util.isMissing(div.getIdvcom())) ){
			Message.printStatus ( 2, routine, action + id + " DemandType -> " + DemandType );
			div.setIdvcom ( DemandType_int );
		}
		if ( fill_IrrigatedAcres && (!fill || StateMod_Util.isMissing(div.getArea())) ){
			Message.printStatus ( 2, routine, action + id + " IrrigatedAcres -> "+IrrigatedAcres);
			div.setArea ( IrrigatedAcres_double );
		}
		if ( fill_UseType && (!fill || StateMod_Util.isMissing(div.getIrturn())) ){
			Message.printStatus ( 2, routine, action + id + " UseType -> " + UseType );
			div.setIrturn ( UseType_int );
		}
		if ( fill_DemandSource && (!fill || StateMod_Util.isMissing(div.getDemsrc())) ){
			Message.printStatus ( 2, routine, action + id + " DemandSource -> " + DemandSource );
			div.setDemsrc ( DemandSource_int );
		}
		if ( fill_EffAnnual && (!fill || StateMod_Util.isMissing(div.getDivefc())) ) {
			Message.printStatus ( 2, routine, action + id + " EffAnnual -> " + EffAnnual);
			div.setDivefc ( EffAnnual_double );
			// Set all the monthly values to the same...
			for ( int ieff = 0; ieff < 12; ieff++ ) {
				div.setDiveff ( ieff, EffAnnual_double );
			}
		}
		if ( fill_EffMonthly && (!fill || StateMod_Util.isMissing(div.getDiveff(0))) ){
			Message.printStatus ( 2, routine, action + id + " EffMonthly -> " + EffMonthly);
			double total = 0.0;
			// Set the monthly efficiencies according to the
			// output year type (EffMonthly is always calendar)...
			if ( outputYearType == YearType.CALENDAR ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					div.setDiveff ( ieff, EffMonthly_double[ieff] );
					total += EffMonthly_double[ieff];
				}
			}
			else if ( outputYearType == YearType.WATER ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					if ( ieff < 9 ) {
						// January - September
						div.setDiveff ( (ieff + 3), EffMonthly_double[ieff] );
					}
					else {
						// October to December need to be at the start...
						div.setDiveff ( (ieff - 9), EffMonthly_double[ieff] );
					}
					total += EffMonthly_double[ieff];
				}
			}
			else if ( outputYearType == YearType.NOV_TO_OCT ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					if ( ieff < 10 ) {
						// January - October
						div.setDiveff ( (ieff + 2), EffMonthly_double[ieff] );
					}
					else {
						// November to December need to be at the start...
						div.setDiveff ( (ieff - 10), EffMonthly_double[ieff] );
					}
					total += EffMonthly_double[ieff];
				}
			}
			// Set the annual to the average as a negative number,
			// to indicate that monthly values will be used...
			div.setDivefc ( -total/12.0 );
		}
		if ( fill_Returns && (!fill || (div.getNrtn() == 0)) ) {
			Message.printStatus ( 2, routine, action + id + " Returns -> " + Returns);
			List returns = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < __ReturnsPercent_double.length; iret++ ) {
				ret = new StateMod_ReturnFlow (	StateMod_DataSet.COMP_DIVERSION_STATIONS);
				ret.setCrtnid ( ReturnsNodeID[iret] );
				ret.setPcttot ( ReturnsPercent[iret] );
				ret.setIrtndl ( ReturnsTableID[iret] );
				returns.add ( ret );
			}
			div.setReturnFlow ( returns );
		}
	}

	if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
    	// If nothing was matched for set command and the idpattern does not contain a
    	// wildcard, add a StateMod_Diversion at the end...
		div = new StateMod_Diversion ( false );
		id = ID;
		div.setID ( id );
		divList.add ( div );
		Message.printStatus ( 2, routine, "Adding diversion station " + id );
		if ( fill_Name ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
			div.setName ( Name );
		}
		if ( fill_RiverNodeID ) {
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + div.getID());
				div.setCgoto ( div.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID);
				div.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff ) {
			Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
			div.setSwitch ( OnOff_int );
		}
		if ( fill_Capacity ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Capacity -> " + Capacity );
			div.setDivcap ( Capacity_double );
		}
		if ( fill_ReplaceResOption ) {
			Message.printStatus ( 2, routine, "Setting " + id + " ReplaceResOption -> " + ReplaceResOption );
			div.setIreptype ( ReplaceResOption_int );
		}
		if ( fill_DailyID ) {
			if ( DailyID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + div.getID() );
				div.setCdividy ( div.getID() );
			}
			else {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				div.setCdividy ( DailyID );
			}
		}
		if ( fill_UserName ) {
			Message.printStatus ( 2, routine, "Setting " + id + " UserName -> " + UserName );
			div.setUsername ( UserName );
		}
		if ( fill_DemandType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " DemandType -> "+DemandType);
			div.setIdvcom ( DemandType_int );
		}
		if ( fill_IrrigatedAcres ) {
			Message.printStatus ( 2, routine, "Setting " + id + " IrrigatedAcres -> " + IrrigatedAcres);
			div.setArea ( IrrigatedAcres_double );
		}
		if ( fill_UseType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " UseType -> " + UseType );
			div.setIrturn ( UseType_int );
		}
		if ( fill_DemandSource ) {
			Message.printStatus ( 2, routine, "Setting " + id + " DemandSource -> " + DemandSource );
			div.setDemsrc ( DemandSource_int );
		}
		if ( fill_EffAnnual ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffAnnual -> " + EffAnnual);
			div.setDivefc ( EffAnnual_double );
			// Set all the monthly values to the same...
			for ( int ieff = 0; ieff < 12; ieff++ ) {
				div.setDiveff ( ieff, EffAnnual_double);
			}
		}
		if ( fill_EffMonthly ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffMonthly -> "+EffMonthly);
			double total = 0.0;
			if ( outputYearType == YearType.CALENDAR ) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					div.setDiveff ( ieff, EffMonthly_double[ieff] );
					total +=EffMonthly_double[ieff];
				}
			}
			else if (outputYearType == YearType.WATER ) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					if ( ieff < 9 ) {
						div.setDiveff ((ieff+3), EffMonthly_double[ieff] );
					}
					else {
						div.setDiveff ( (ieff - 9), EffMonthly_double[ieff] );
					}
					total +=EffMonthly_double[ieff];
				}
			}
			else if (outputYearType == YearType.NOV_TO_OCT ) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					if ( ieff < 10 ) {
						div.setDiveff ((ieff+2), EffMonthly_double[ieff] );
					}
					else {
						div.setDiveff ( (ieff - 10), EffMonthly_double[ieff] );
					}
					total +=EffMonthly_double[ieff];
				}
			}
			// Set the annual to the average as a negative number, to indicate that monthly values will
			// be used...
			div.setDivefc ( -total/12.0 );
		}
		if ( fill_Returns ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Returns -> " + Returns);
			List returns = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < ReturnsPercent.length; iret++ ) {
				ret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_DIVERSION_STATIONS);
				ret.setCrtnid ( ReturnsNodeID[iret] );
				ret.setPcttot ( ReturnsPercent[iret] );
				ret.setIrtndl ( ReturnsTableID[iret] );
				returns.add ( ret );
			}
			div.setReturnFlow ( returns );
		}
	}
	if ( matchCount == 0 ) {
		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
			String message = "Diversion \"" + ID + "\" was not matched: warning and not adding.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the identifier is correct." ) );
		}
		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
			String message = "Diversion \"" + ID +	"\" was not matched: failing and not adding.";
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

/**
Process the well stations.
*/
private int processWellStations ( List wellList, boolean fill, YearType outputYearType,
		int warningCount, int warningLevel, String commandTag, CommandStatus status,
		String ID, String idpattern_Java,
		String Name, boolean fill_Name,
		String RiverNodeID, boolean fill_RiverNodeID,
		String OnOff, int OnOff_int, boolean fill_OnOff,
		String Capacity, double Capacity_double, boolean fill_Capacity,
		String DailyID, boolean fill_DailyID,
		String AdminNumShift, double AdminNumShift_double, boolean fill_AdminNumShift,
		String DiversionID, boolean fill_DiversionID,
		String DemandType, int DemandType_int, boolean fill_DemandType,
		String IrrigatedAcres, double IrrigatedAcres_double, boolean fill_IrrigatedAcres,
		String UseType, int UseType_int, boolean fill_UseType,
		String DemandSource, int DemandSource_int, boolean fill_DemandSource,
		String EffAnnual, double EffAnnual_double, boolean fill_EffAnnual,
		String EffMonthly, double [] EffMonthly_double, boolean fill_EffMonthly,
		String Returns, String [] ReturnsNodeID, double [] ReturnsPercent, String [] ReturnsTableID, boolean fill_Returns,
		String Depletions, String [] DepletionsNodeID, double [] DepletionsPercent, String [] DepletionsTableID, boolean fill_Depletions,
		String IfNotFound  )
{	String routine = "FillAndSetDiversionAndWellStation_Command.processWellStations";
	StateMod_Well well = null;
	String id;
	int matchCount = 0;
	String action = "Setting ";
	if ( fill ) {
		action = "Filling ";
	}
	int wellListSize = wellList.size();
	for (int i = 0; i < wellListSize; i++) {
		well = (StateMod_Well)wellList.get(i);
		id = well.getID();
		if ( !id.matches(idpattern_Java) ) {
			// Identifier does not match...
			continue;
		}
		++matchCount;
		// Have a match so reset the data...
		if ( fill_Name && (!fill || StateMod_Util.isMissing(well.getName())) ) {
			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
			well.setName ( Name );
		}
		if ( fill_RiverNodeID && (!fill || StateMod_Util.isMissing(well.getCgoto())) ) {
			Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID );
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " +well.getID());
				well.setCgoto ( well.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID);
				well.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(well.getSwitch())) ) {
			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
			well.setSwitch ( OnOff_int );
		}
		if ( fill_Capacity && (!fill || StateMod_Util.isMissing(well.getDivcapw())) ){
			Message.printStatus ( 2, routine, action + id + " Capacity -> " + Capacity );
			well.setDivcapw ( Capacity_double );
		}
		if ( fill_DailyID && (!fill || StateMod_Util.isMissing(well.getCdividyw()))){
			if ( DailyID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + well.getID() );
				well.setCdividyw ( well.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + DailyID );
				well.setCdividyw ( DailyID );
			}
		}
		if ( fill_AdminNumShift && (!fill || StateMod_Util.isMissing(well.getPrimary())) ){
			Message.printStatus ( 2, routine, action + id + " AdminNumShift -> " + AdminNumShift );
			well.setPrimary ( AdminNumShift_double );
		}
		if ( fill_DiversionID && (!fill || StateMod_Util.isMissing(well.getIdvcow2()))){
			if ( DiversionID.equalsIgnoreCase("ID") ) {
				// Set the diversion ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DiversionID -> "+well.getID() );
				well.setIdvcow2 ( well.getID() );
			}
			else {
				// Set to the given value...
				Message.printStatus ( 2, routine, action + id + " DiversionID -> " + DiversionID);
				well.setIdvcow2 ( DiversionID );
			}
		}
		if ( fill_DemandType && (!fill || StateMod_Util.isMissing(well.getIdvcomw())) ){
			Message.printStatus ( 2, routine, action + id + " DemandType -> " + DemandType );
			well.setIdvcomw ( DemandType_int );
		}
		if ( fill_IrrigatedAcres && (!fill || StateMod_Util.isMissing(well.getAreaw())) ){
			Message.printStatus ( 2, routine, action + id + " IrrigatedAcres -> "+IrrigatedAcres);
			well.setAreaw ( IrrigatedAcres_double );
		}
		if ( fill_UseType && (!fill || StateMod_Util.isMissing(well.getIrturnw())) ){
			Message.printStatus ( 2, routine, action + id + " UseType -> " + UseType );
			well.setIrturnw ( UseType_int );
		}
		if ( fill_DemandSource && (!fill || StateMod_Util.isMissing(well.getDemsrcw())) ){
			Message.printStatus ( 2, routine, action + id + " DemandSource -> " + DemandSource );
			well.setDemsrcw ( DemandSource_int );
		}
		if ( fill_EffAnnual && (!fill || StateMod_Util.isMissing(well.getDivefcw())) ){
			Message.printStatus ( 2, routine, action + id + " EffAnnual -> " + EffAnnual);
			well.setDivefcw ( EffAnnual_double );
			// Set all the monthly values to the same...
			for ( int ieff = 0; ieff < 12; ieff++ ) {
				well.setDiveff ( ieff, EffAnnual_double );
			}
		}
		if ( fill_EffMonthly && (!fill || StateMod_Util.isMissing(well.getDiveff(0)))){
			Message.printStatus ( 2, routine, action + id + " EffMonthly -> " + EffMonthly);
			double total = 0.0;
			// Set the monthly efficiencies according to the
			// output year type (EffMonthly is always calendar)...
			if ( outputYearType == YearType.CALENDAR ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					well.setDiveff ( ieff, EffMonthly_double[ieff] );
					total += EffMonthly_double[ieff];
				}
			}
			else if ( outputYearType == YearType.WATER ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					if ( ieff < 9 ) {
						well.setDiveff ( (ieff + 3), EffMonthly_double[ieff] );
					}
					else {
						well.setDiveff ( (ieff - 9), EffMonthly_double[ieff] );
					}
					total += EffMonthly_double[ieff];
				}
			}
			else if ( outputYearType == YearType.NOV_TO_OCT ) {
				for ( int ieff = 0; ieff < 12; ieff++ ) {
					if ( ieff < 10 ) {
						well.setDiveff ( (ieff + 2), EffMonthly_double[ieff] );
					}
					else {
						well.setDiveff ( (ieff - 10), EffMonthly_double[ieff] );
					}
					total += EffMonthly_double[ieff];
				}
			}
			// Set the annual to the average as a negative number,
			// to indicate that monthly values will be used...
			well.setDivefcw ( -total/12.0 );
		}
		if ( fill_Returns && (!fill || (well.getNrtnw() == 0)) ) {
			Message.printStatus ( 2, routine, action + id + " Returns -> " + Returns);
			List returns = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < ReturnsPercent.length; iret++ ) {
				ret = new StateMod_ReturnFlow (	StateMod_DataSet.COMP_WELL_STATIONS);
				ret.setCrtnid ( ReturnsNodeID[iret] );
				ret.setPcttot ( ReturnsPercent[iret] );
				ret.setIrtndl ( ReturnsTableID[iret] );
				returns.add ( ret );
			}
			well.setReturnFlows ( returns );
		}
		if ( fill_Depletions && (!fill || (well.getNrtnw2() == 0)) ) {
			Message.printStatus ( 2, routine, action + id + " Depletions -> " + Depletions);
			List depletions = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < DepletionsPercent.length; iret++ ) {
				ret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_WELL_STATIONS);
				ret.setCrtnid ( DepletionsNodeID[iret] );
				ret.setPcttot ( DepletionsPercent[iret] );
				ret.setIrtndl ( DepletionsTableID[iret] );
				depletions.add ( ret );
			}
			well.setDepletions ( depletions );
		}
	}

	if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
    	// If nothing was matched for set command and the idpattern does not contain a
    	// wildcard, add a StateMod_Well at the end...
		well = new StateMod_Well ( false );
		// TODO SAM 2005-10-12
		// This could be filled with a command but default here at the request of Ray Bennett.
		well.setIdvcow2 ( "NA" );
		id = ID;
		well.setID ( id );
		wellList.add ( well );
		Message.printStatus ( 2, routine, "Adding well station " + id );
		if ( fill_Name ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
			well.setName ( Name );
		}
		if ( fill_RiverNodeID ) {
			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
				// Set the river node ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + well.getID() );
				well.setCgoto ( well.getID() );
			}
			else {
				Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
				well.setCgoto ( RiverNodeID );
			}
		}
		if ( fill_OnOff ) {
			Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
			well.setSwitch ( OnOff_int );
		}
		if ( fill_Capacity ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Capacity -> " + Capacity );
			well.setDivcapw ( Capacity_double );
		}
		if ( fill_DailyID ) {
			if ( DailyID.equalsIgnoreCase("ID") ) {
				// Set the daily ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DailyID -> " + well.getID() );
				well.setCdividyw ( well.getID() );
			}
			else {
				Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
				well.setCdividyw ( DailyID );
			}
		}
		if ( fill_AdminNumShift ) {
			Message.printStatus ( 2, routine, "Setting " + id + " AdminNumShift -> " + AdminNumShift );
			well.setPrimary ( AdminNumShift_double );
		}
		if ( fill_DiversionID ) {
			if ( DiversionID.equalsIgnoreCase("ID") ) {
				// Set the diversion ID to the same as the normal identifier...
				Message.printStatus ( 2, routine, action + id + " DiversionID -> " + well.getID() );
				well.setIdvcow2 ( well.getID() );
			}
			else {
				Message.printStatus ( 2, routine, "Setting " + id + " DiversionID -> " + DiversionID );
				well.setIdvcow2 ( DiversionID );
			}
		}
		if ( fill_DemandType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " DemandType -> "+DemandType);
			well.setIdvcomw ( DemandType_int );
		}
		if ( fill_IrrigatedAcres ) {
			Message.printStatus ( 2, routine, "Setting " + id + " IrrigatedAcres -> " + IrrigatedAcres);
			well.setAreaw ( IrrigatedAcres_double );
		}
		if ( fill_UseType ) {
			Message.printStatus ( 2, routine, "Setting " + id + " UseType -> " + UseType );
			well.setIrturnw ( UseType_int );
		}
		if ( fill_DemandSource ) {
			Message.printStatus ( 2, routine, "Setting " + id + " DemandSource -> " + DemandSource );
			well.setDemsrcw ( DemandSource_int );
		}
		if ( fill_EffAnnual ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffAnnual -> " + EffAnnual);
			well.setDivefcw ( EffAnnual_double );
			// Set all the monthly values to the same...
			for ( int ieff = 0; ieff < 12; ieff++ ) {
				well.setDiveff( ieff,EffAnnual_double);
			}
		}
		if ( fill_EffMonthly ) {
			Message.printStatus ( 2, routine, "Setting " + id + " EffMonthly -> "+EffMonthly);
			double total = 0.0;
			if ( outputYearType == YearType.CALENDAR ) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					well.setDiveff ( ieff, EffMonthly_double[ieff] );
					total +=EffMonthly_double[ieff];
				}
			}
			else if (outputYearType == YearType.WATER ) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					if ( ieff < 9 ) {
						well.setDiveff((ieff+3), EffMonthly_double[ieff] );
					}
					else {
						well.setDiveff ( (ieff - 9), EffMonthly_double[ieff] );
					}
					total +=EffMonthly_double[ieff];
				}
			}
			else if (outputYearType == YearType.NOV_TO_OCT) {
				for ( int ieff = 0; ieff < 12; ieff++ ){
					if ( ieff < 10 ) {
						well.setDiveff((ieff+2), EffMonthly_double[ieff] );
					}
					else {
						well.setDiveff ( (ieff - 10), EffMonthly_double[ieff] );
					}
					total +=EffMonthly_double[ieff];
				}
			}
			// Set the annual to the average as a negative
			// number, to indicate that monthly values will be used...
			well.setDivefcw ( -total/12.0 );
		}
		if ( fill_Returns ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Returns -> " + Returns);
			List returns = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < ReturnsPercent.length; iret++ ) {
				ret = new StateMod_ReturnFlow (	StateMod_DataSet.COMP_WELL_STATIONS);
				ret.setCrtnid ( ReturnsNodeID[iret] );
				ret.setPcttot ( ReturnsPercent[iret] );
				ret.setIrtndl ( ReturnsTableID[iret] );
				returns.add ( ret );
			}
			well.setReturnFlows ( returns );
		}
		if ( fill_Depletions ) {
			Message.printStatus ( 2, routine, "Setting " + id + " Depletions -> "+Depletions);
			List depletions = new Vector ();
			StateMod_ReturnFlow ret;
			for ( int iret = 0; iret < DepletionsPercent.length; iret++ ) {
				ret = new StateMod_ReturnFlow (
				StateMod_DataSet.COMP_WELL_STATIONS);
				ret.setCrtnid(DepletionsNodeID[iret] );
				ret.setPcttot(DepletionsPercent[iret]);
				ret.setIrtndl(DepletionsTableID[iret]);
				depletions.add ( ret );
			}
			well.setDepletions ( depletions );
		}
		// Increment the match count so a warning is not printed below
		++matchCount;
	}
	if ( matchCount == 0 ) {
		if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
			String message = "Well \"" + ID + "\" was not matched: warning and not adding.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
					message, "Verify that the identifier is correct." ) );
		}
		else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
			String message = "Well \"" + ID +	"\" was not matched: failing and not adding.";
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
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.
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
	String Capacity = parameters.getValue ( "Capacity" );
	if ( Capacity != null ) {
		Capacity = Capacity.trim();
	}
	String ReplaceResOption = parameters.getValue ( "ReplaceResOption" );
	if ( ReplaceResOption != null ) {
		ReplaceResOption = ReplaceResOption.trim();
	}
	String DailyID = parameters.getValue ( "DailyID" );
	if ( DailyID != null ) {
		DailyID = DailyID.trim();
	}
	String UserName = parameters.getValue ( "UserName" );
	if ( UserName != null ) {
		UserName = UserName.trim();
	}
	String AdminNumShift = parameters.getValue ( "AdminNumShift" );// Wells
	if ( AdminNumShift != null ) {
		AdminNumShift = AdminNumShift.trim();
	}
	String DiversionID = parameters.getValue ( "DiversionID" );	// Wells
	if ( DiversionID != null ) {
		DiversionID = DiversionID.trim();
	}
	String DemandType = parameters.getValue ( "DemandType" );
	if ( DemandType != null ) {
		DemandType = DemandType.trim();
	}
	String IrrigatedAcres = parameters.getValue ( "IrrigatedAcres" );
	if ( IrrigatedAcres != null ) {
		IrrigatedAcres = IrrigatedAcres.trim();
	}
	String UseType = parameters.getValue ( "UseType" );
	if ( UseType != null ) {
		UseType = UseType.trim();
	}
	String DemandSource = parameters.getValue ( "DemandSource" );
	if ( DemandSource != null ) {
		DemandSource = DemandSource.trim();
	}
	String EffAnnual = parameters.getValue ( "EffAnnual" );
	if ( EffAnnual != null ) {
		EffAnnual = EffAnnual.trim();
	}
	String EffMonthly = parameters.getValue ( "EffMonthly" );
	if ( EffMonthly != null ) {
		EffMonthly = EffMonthly.trim();
	}
	String Returns = parameters.getValue ( "Returns" );
	if ( Returns != null ) {
		Returns = Returns.trim();
	}
	String Depletions = parameters.getValue ( "Depletions" );
	if ( Depletions != null ) {
		Depletions = Depletions.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();

    // Get the data needed for the command
    
    List divList = null;
    List wellList = null;
    try {
    	if ( (this instanceof FillDiversionStation_Command) ||
    		(this instanceof SetDiversionStation_Command) ) {
    		divList = (List)processor.getPropContents ( "StateMod_DiversionStation_List" );
    	}
    	else if ( (this instanceof FillWellStation_Command) ||
        		(this instanceof SetWellStation_Command) ) {
    		wellList = (List)processor.getPropContents ( "StateMod_WellStation_List" );
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
    
    YearType outputYearType = null;
    try {
    	outputYearType = (YearType)processor.getPropContents ( "OutputYearType" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputYearType (" + e + ").";
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

    	boolean fill_Capacity = false;
    	double Capacity_double = 0.0;
    	if ( (Capacity != null) && (Capacity.length() > 0) ) {
    		fill_Capacity = true;
    		Capacity_double = Double.parseDouble ( Capacity );
    	}
    	
    	boolean fill_ReplaceResOption = false;
    	int ReplaceResOption_int = 0;
    	if ( (ReplaceResOption != null) && (ReplaceResOption.length() > 0) ) {
    		fill_ReplaceResOption = true;
    		ReplaceResOption_int = Integer.parseInt(ReplaceResOption);
    	}

    	boolean fill_DailyID = false;
    	if ( DailyID != null ) {
    		fill_DailyID = true;
    	}
    	
    	boolean fill_UserName = false;
    	if ( UserName != null ) {
    		fill_UserName = true;
    	}

    	boolean fill_AdminNumShift = false;
    	double AdminNumShift_double = 0.0;
    	if ( (AdminNumShift != null) && (AdminNumShift.length() > 0) ) {
    		fill_AdminNumShift = true;
    		AdminNumShift_double = Double.parseDouble ( AdminNumShift);
    	}

    	boolean fill_DiversionID = false;
    	if ( DiversionID != null ) {
    		fill_DiversionID = true;
    	}

    	boolean fill_DemandType = false;
    	int DemandType_int = 0;
    	if ( (DemandType != null) && (DemandType.length() > 0) ) {
    		fill_DemandType = true;
    		DemandType_int = Integer.parseInt( DemandType );
    	}
    	boolean fill_IrrigatedAcres = false;
    	double IrrigatedAcres_double = 0.0;
    	if ( (IrrigatedAcres != null) && (IrrigatedAcres.length() > 0) ) {
    		fill_IrrigatedAcres = true;
    		IrrigatedAcres_double = Double.parseDouble(IrrigatedAcres);
    	}
    	boolean fill_UseType = false;
    	int UseType_int = 0;
    	if ( (UseType != null) && (UseType.length() > 0) ) {
    		fill_UseType = true;
    		UseType_int = Integer.parseInt ( UseType );
    	}
    	boolean fill_DemandSource = false;
    	int DemandSource_int = 0;
    	if ( (DemandSource != null) && (DemandSource.length() > 0) ) {
    		fill_DemandSource = true;
    		DemandSource_int = Integer.parseInt ( DemandSource );
    	}
    	boolean fill_EffAnnual = false;
    	double EffAnnual_double = 0.0;
    	if ( (EffAnnual != null) && (EffAnnual.length() > 0) ) {
    		fill_EffAnnual = true;
    		EffAnnual_double = Double.parseDouble(EffAnnual);
    	}

    	boolean fill_EffMonthly = false;
    	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
    		fill_EffMonthly = true;
    	}

    	boolean fill_Returns = false;
    	if ( (Returns != null) && (Returns.length() > 0) ) {
    		fill_Returns = true;
    	}

    	boolean fill_Depletions = false;
    	if ( (Depletions != null) && (Depletions.length() > 0) ) {
    		fill_Depletions = true;
    	}
    	
    	boolean fill = true;
    	if ( (this instanceof SetDiversionStation_Command) ||
    		(this instanceof SetWellStation_Command) ) {
    		fill = false;
    	}
    	
    	if ( (this instanceof FillDiversionStation_Command) ||
        		(this instanceof SetDiversionStation_Command) ) {
    		warning_count = processDiversionStations ( divList, fill, outputYearType,
    				warning_count, warning_level, command_tag, status,
    				ID, idpattern_Java,
    				Name, fill_Name,
    				RiverNodeID, fill_RiverNodeID,
    				OnOff, OnOff_int, fill_OnOff,
    				Capacity, Capacity_double, fill_Capacity,
    				ReplaceResOption, ReplaceResOption_int, fill_ReplaceResOption,
    				DailyID, fill_DailyID,
    				UserName, fill_UserName,
    				AdminNumShift, AdminNumShift_double, fill_AdminNumShift,
    				DemandType, DemandType_int, fill_DemandType,
    				IrrigatedAcres, IrrigatedAcres_double, fill_IrrigatedAcres,
    				UseType, UseType_int, fill_UseType,
    				DemandSource, DemandSource_int, fill_DemandSource,
    				EffAnnual, EffAnnual_double, fill_EffAnnual,
    				EffMonthly, __EffMonthly_double, fill_EffMonthly,
    				Returns, __ReturnsNodeID, __ReturnsPercent_double, __ReturnsTableID, fill_Returns,
    				IfNotFound  );
    		if (this instanceof SetWellStation_Command) {
    			Message.printStatus(2, routine, "After set command have " + wellList.size() + " wells.");
    		}
    	}
    	else if ( (this instanceof FillWellStation_Command) ||
        		(this instanceof SetWellStation_Command) ) {
    		warning_count = processWellStations ( wellList, fill, outputYearType,
    				warning_count, warning_level, command_tag, status,
    				ID, idpattern_Java,
    				Name, fill_Name,
    				RiverNodeID, fill_RiverNodeID,
    				OnOff, OnOff_int, fill_OnOff,
    				Capacity, Capacity_double, fill_Capacity,
    				DailyID, fill_DailyID,
    				AdminNumShift, AdminNumShift_double, fill_AdminNumShift,
    				DiversionID, fill_DiversionID,
    				DemandType, DemandType_int, fill_DemandType,
    				IrrigatedAcres, IrrigatedAcres_double, fill_IrrigatedAcres,
    				UseType, UseType_int, fill_UseType,
    				DemandSource, DemandSource_int, fill_DemandSource,
    				EffAnnual, EffAnnual_double, fill_EffAnnual,
    				EffMonthly, __EffMonthly_double, fill_EffMonthly,
    				Returns, __ReturnsNodeID, __ReturnsPercent_double, __ReturnsTableID, fill_Returns,
    				Depletions, __DepletionsNodeID, __DepletionsPercent_double, __DepletionsTableID, fill_Depletions,
    				IfNotFound  );
    		if (this instanceof SetWellStation_Command) {
    			Message.printStatus(2, routine, "After set command have " + wellList.size() + " wells.");
    		}
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
	String Capacity = parameters.getValue ( "Capacity" );
	String ReplaceResOption = parameters.getValue ( "ReplaceResOption" ); // Diversions
	String DailyID = parameters.getValue ( "DailyID" );
	String AdminNumShift = parameters.getValue ( "AdminNumShift" ); // Wells
	String DiversionID = parameters.getValue ( "DiversionID" ); // Wells
	String UserName = parameters.getValue ( "UserName" ); // Diversions
	String DemandType = parameters.getValue ( "DemandType" );
	String IrrigatedAcres = parameters.getValue ( "IrrigatedAcres" );
	String UseType = parameters.getValue ( "UseType" );
	String DemandSource = parameters.getValue ( "DemandSource" );
	String EffAnnual = parameters.getValue ( "EffAnnual" );
	String EffMonthly = parameters.getValue ( "EffMonthly" );
	String Returns = parameters.getValue ( "Returns" );
	String Depletions = parameters.getValue ( "Depletions" ); // Wells
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
	if ( (Capacity != null) && (Capacity.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Capacity=" + Capacity );
	}
	if ( (this instanceof FillDiversionStation_Command) || (this instanceof SetDiversionStation_Command) ) {
		if ( (ReplaceResOption != null) && (ReplaceResOption.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "ReplaceResOption=" + ReplaceResOption );
		}
	}
	if ( (DailyID != null) && (DailyID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DailyID=\"" + DailyID + "\"" );
	}
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		if ( (AdminNumShift != null) && (AdminNumShift.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "AdminNumShift=" + AdminNumShift );
		}
		if ( (DiversionID != null) && (DiversionID.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "DiversionID=\"" + DiversionID + "\"" );
		}
	}
	if ( (this instanceof FillDiversionStation_Command) || (this instanceof SetDiversionStation_Command) ) {
		if ( (UserName != null) && (UserName.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "UserName=\"" + UserName + "\"" );
		}
	}
	if ( (DemandType != null) && (DemandType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DemandType=" + DemandType );
	}
	if ( (IrrigatedAcres != null) && (IrrigatedAcres.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigatedAcres=" + IrrigatedAcres );
	}
	if ( (UseType != null) && (UseType.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseType=" + UseType );
	}
	if ( (DemandSource != null) && (DemandSource.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DemandSource=" + DemandSource );
	}
	if ( (EffAnnual != null) && (EffAnnual.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffAnnual=" + EffAnnual );
	}
	if ( (EffMonthly != null) && (EffMonthly.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffMonthly=\"" + EffMonthly + "\"" );
	}
	if ( (Returns != null) && (Returns.length() > 0	) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Returns=\"" + Returns + "\"" );
	}
	if ( (this instanceof FillWellStation_Command) || (this instanceof SetWellStation_Command) ) {
		if ( (Depletions != null) && (Depletions.length() > 0) ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "Depletions=\"" + Depletions + "\"" );
		}
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