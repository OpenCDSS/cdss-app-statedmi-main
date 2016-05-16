//------------------------------------------------------------------------------
// openHydroBase_Command - handle the openHydroBase() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-06-08	Steven A. Malers, RTi	Initial version.  Copy and modify
//					compareTimeSeries_Command.
// 2005-09-22	SAM, RTi		Add the DatabaseName parameter.
// 2005-09-26	J. Thomas Sapienza, RTi	HydroBaseDMI instance for SQL Server
//					connections now iterates through a list
//					of possible ports to connect to, in 
//					order to support MSDE servers.
// 2007-02-11	SAM, RTi		Verify that code is consistent with the new
//					TSCommandProcessor.  Clean up code based on Eclipse
//					feedback.
//------------------------------------------------------------------------------
// EndHeader

package rti.tscommandprocessor.commands.hydrobase;

import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;

/**
<p>
This class initializes, checks, and runs the OpenHydroBase() command.
</p>
<p>The CommandProcessor must return the following properties:  HydroBaseDMIList.
</p>
*/
public class OpenHydroBase_Command extends AbstractCommand implements Command
{

/**
Data members used for parameter values.
*/
protected final String _BatchOnly = "BatchOnly";
protected final String _GUIAndBatch = "GUIAndBatch";
protected final String _GUIOnly = "GUIOnly";

protected final String _True = "True";
protected final String _False = "False";

/**
Constructor.
*/
public OpenHydroBase_Command ()
{	super();
	setCommandName ( "OpenHydroBase" );
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
{	String warning = "";
    String message;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    String OdbcDsn = parameters.getValue ( "OdbcDsn" );
	if ( OdbcDsn == null ) {
		OdbcDsn = "";
	}
	String DatabaseServer = parameters.getValue ( "DatabaseServer" );
	if ( DatabaseServer == null ) {
		DatabaseServer = "";
	}
	String DatabaseName = parameters.getValue ( "DatabaseName" );
	if ( DatabaseName == null ) {
		DatabaseName = "";
	}
	//TODO SAM 2007-02-11 Need to add checks for the following.
	//String RunMode = parameters.getValue ( "RunMode" );
	//String UseStoredProcedures = parameters.getValue("UseStoredProcedures");
	//String InputName = parameters.getValue ( "InputName" );

	if ( (OdbcDsn.equals("") && DatabaseServer.equals("")) ||
		(!OdbcDsn.equals("") && !DatabaseServer.equals("")) ) {
        message = "An ODBC DSN or Database Server must be specified (but not both).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ODBC DSN ... OR a databas server." ) );
	}

	if ( DatabaseServer.equals("") ) {
		if ( !DatabaseName.equals("") ) {
            message = "A database name can be specified only when the database server is specified.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Do not specify the database name when the ODBC DSN is specified." ) );
		}
	}
    
    // Check for invalid parameters...
	List valid_Vector = new Vector();
    valid_Vector.add ( "OdbcDsn" );
    valid_Vector.add ( "DatabaseServer" );
    valid_Vector.add ( "DatabaseName" );
    valid_Vector.add ( "RunMode" );
    valid_Vector.add ( "UseStoredProcedures" );
    valid_Vector.add ( "InputName" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
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
	return (new OpenHydroBase_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	String routine = "OpenHydroBase_Command.parseCommand", message;
	int warning_level = 2;
    
    CommandStatus status = getCommandStatus();
	
    List tokens = StringUtil.breakStringList ( command, "()", StringUtil.DELIM_SKIP_BLANKS );

	if ( (tokens == null) ) {
		message = "Invalid syntax for \"" + command + "\".  Expecting OpenHydroBase(...).";
		Message.printWarning ( warning_level, routine, message);
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify command syntax using command editor." ) );
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the command...
	if ( tokens.size() > 1 ) {
		try {
		    setCommandParameters ( PropList.parse ( Prop.SET_FROM_PERSISTENT,
				(String)tokens.get(1), routine,"," ) );
		}
		catch ( Exception e ) {
			message = "Syntax error in \"" + command + "\".  Expecting OpenHydroBase(...).";
			Message.printWarning ( warning_level, routine, message);
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify command syntax using command editor." ) );
			throw new InvalidCommandSyntaxException ( message );
		}
	}
}

/**
Run the command.
@param processor The CommandProcessor that is executing the command, which will
provide necessary data inputs and receive output(s).
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "openHydroBase_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);

	// Get the input needed to process the file...
	PropList parameters = getCommandParameters();
	String OdbcDsn = parameters.getValue ( "OdbcDsn" );
	String DatabaseServer = parameters.getValue ( "DatabaseServer" );
	String DatabaseName = parameters.getValue ( "DatabaseName" );
	String RunMode = parameters.getValue ( "RunMode" );
	String UseStoredProcedures = parameters.getValue ("UseStoredProcedures" );
	String InputName = parameters.getValue ( "InputName" );
	if ( RunMode == null ) {
		RunMode = _GUIAndBatch;
	}
	if ( (!IOUtil.isBatch() && RunMode.equalsIgnoreCase(_GUIOnly)) ||
		(IOUtil.isBatch() && RunMode.equalsIgnoreCase(_BatchOnly)) ||
		RunMode.equalsIgnoreCase(_GUIAndBatch) ) {
		// OK to to run...
		HydroBaseDMI hbdmi = null;
		try {
		if ( (OdbcDsn != null) && !OdbcDsn.equals("") ) {
			// Use Access.  Stored procedures are not an option...
			hbdmi = new HydroBaseDMI("Access", OdbcDsn, null, null);
			hbdmi.open();
		}
		else if ( (DatabaseServer != null) && !DatabaseServer.equals("")){
			boolean successful = false;
			
			try {
				if (DatabaseName != null && DatabaseName.equals("")) {
					DatabaseName = null;
				}
				
				PropList props = new PropList("");
				props.set ( "HydroBase.DatabaseServer", DatabaseServer );
				props.set ( "HydroBase.DatabaseName", DatabaseName );
				props.set ( "HydroBase.UseStoredProcedures", UseStoredProcedures );
				hbdmi = new HydroBaseDMI ( props );
				
				hbdmi.open();
				successful = true;
			}
			catch (Exception e) {
				Message.printWarning(3, routine, e);
				message = "Error opening HydroBase connection";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag(
					command_tag, warning_count), 
					routine, message);
			}
			
			if (!successful) {
                message = "Could not connect to a HydroBase database \"" + DatabaseName + "\" on server \"" +
                DatabaseServer + "\".";
                status.addToLog ( CommandPhaseType.RUN,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify the HydroBase database information." ) );
				throw new Exception( message );
			}
		}
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, e );
			message = "Unexpected error opening HydroBase connection (" + e + ")";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify the HydroBase database information." ) );
			throw new CommandException ( message );
		}
		// Set the input name for the connection.  This is used to allow
		// more than one HydroBaseDMI connection at the same time...
		if ( InputName != null ) {
			// If not set will be a blank string in the DMI.
			hbdmi.setInputName ( InputName );
		}
		// Set the HydroBaseDMI instance in the Vector of open
		// connections, passed back to the CommandProcessor...
		warning_count = setHydroBaseDMI ( hbdmi, false, warning_level, warning_count, command_tag );
		// Print a message to the log file...
		try {	String [] comments = hbdmi.getVersionComments();
			for ( int i = 0; i < comments.length; i++ ) {
				Message.printStatus ( 2, routine, comments[i] );
			}
		}
		catch ( Exception e ) {
			// Skip...
		}
	}
	else {
        message =
		"Not running \"" + getCommandString() +
		"\" because run mode is not compatible with RunMode=" + RunMode;
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.WARNING,
                        message, "Verify that command should not be run." ) );
		throw new InvalidCommandParameterException ( message );
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
		throw new CommandWarningException ( message );
	}
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set a HydroBaseDMI instance in the Vector that is being maintained for use.
The input name in the DMI is used to lookup the instance.  If a match is found,
the old instance is optionally closed and the new instance is set in the same
location.  If a match is not found, the new instance is added at the end.
@param hbdmi HydroBaseDMI to add to the list.  Null will be ignored.
@param close_old If an old DMI instance is matched, close the DMI instance if
true.  The main issue is that if something else is using the DMI instance (e.g.,
the TSTool GUI) it may be necessary to leave the old instance open.
@param warning_level Warning level for user-visible command messages.
@param warning_count The count of warnings generated in this command.
@param command_tag String used to tag messages
*/
private int setHydroBaseDMI ( HydroBaseDMI hbdmi, boolean close_old, int warning_level,
		int warning_count, String command_tag )
throws CommandException
{	String routine = getCommandName() + ".setHydroBaseDMI";

    CommandStatus status = getCommandStatus();
    
	String message;
	if ( hbdmi == null ) {
		return warning_count;
	}

	// Get the DMI instances from the processor...

	CommandProcessor processor = getCommandProcessor();
	List dmilist = null;
	try { Object o = processor.getPropContents ( "HydroBaseDMIList" );
			if ( o != null ) {
				dmilist = (List)o;
			}
	}
	catch ( Exception e ) {
		// Not fatal, but of use to developers.
		message = "Error requesting HydroBaseDMIList from processor - starting new list.";
		routine = getCommandName() + ".setHydroBaseDMI";
		Message.printWarning(3, routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.WARNING,
                        message, "Report the problem to software support." ) );
	}
	
	int size = 0;
	if ( dmilist == null ) {
		dmilist = new Vector();
	}
	size = dmilist.size();
	HydroBaseDMI hbdmi2 = null;
	String input_name = hbdmi.getInputName();
	for ( int i = 0; i < size; i++ ) {
		hbdmi2 = (HydroBaseDMI)dmilist.get(i);
		if ( hbdmi2.getInputName().equalsIgnoreCase(input_name)){
			// The input name of the current instance matches that of the instance in the Vector.
			// Replace the instance in the Vector by the new instance...
			if ( close_old ) {
				try {	hbdmi2.close();
				}
				catch ( Exception e ) {
					// Probably can ignore.
				}
			}
			dmilist.set ( i, hbdmi );
			try {
			    processor.setPropContents ( "HydroBaseDMIList",dmilist );
			}
			catch ( Exception e ){
				message = "Cannot set updated HydroBaseDMI list.";
				Message.printWarning ( warning_level,
					MessageUtil.formatMessageTag(
					command_tag, ++warning_count),
					routine,message);
                status.addToLog ( CommandPhaseType.RUN,
                        new CommandLogRecord(CommandStatusType.WARNING,
                                message, "Report the problem to software support." ) );
				throw new CommandException ( message );
			}
			return warning_count;
		}
	}
	// Add a new instance to the Vector...
	dmilist.add ( hbdmi );
	// Set back in the processor...
	try { processor.setPropContents ( "HydroBaseDMIList", dmilist );
	}
	catch ( Exception e ){
		message = "Cannot set updated HydroBaseDMI list.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.WARNING,
                        message, "Report the problem to software support." ) );
		throw new CommandException ( message );
	}
	return warning_count;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String OdbcDsn = props.getValue ( "OdbcDsn" );
	String DatabaseServer = props.getValue ( "DatabaseServer" );
	String DatabaseName = props.getValue ( "DatabaseName" );
	String RunMode = props.getValue ( "RunMode" );
	String UseStoredProcedures = props.getValue ( "UseStoredProcedures" );
	String InputName = props.getValue ( "InputName" );

	StringBuffer b = new StringBuffer ();
	if ( (OdbcDsn != null) && (OdbcDsn.length() > 0) ) {
		b.append ( "OdbcDsn=\"" + OdbcDsn + "\"" );
	}
	if ( (DatabaseServer != null) && (DatabaseServer.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DatabaseServer=\"" + DatabaseServer + "\"" );
	}
	if ( (DatabaseName != null) && (DatabaseName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DatabaseName=\"" + DatabaseName + "\"" );
	}
	if ( (RunMode != null) && (RunMode.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "RunMode=" + RunMode );
	}
	if (	(UseStoredProcedures != null) &&
		(UseStoredProcedures.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseStoredProcedures=" + UseStoredProcedures );
	}
	if ( (InputName != null) && (InputName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputName=\"" + InputName + "\"" );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
