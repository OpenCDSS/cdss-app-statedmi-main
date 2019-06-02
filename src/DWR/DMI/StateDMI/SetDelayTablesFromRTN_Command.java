// SetDelayTablesFromRTN_Command - This class initializes, checks, and runs the SetDiversionStationDelayTablesFromRTN(),
// SetWellStationDelayTablesFromRTN(), and SetWellStationDepletionTablesFromRTN() commands.

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
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_ReturnFlow;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
<p>
This class initializes, checks, and runs the SetDiversionStationDelayTablesFromRTN(),
SetWellStationDelayTablesFromRTN(), and SetWellStationDepletionTablesFromRTN() commands.
It is an abstract base class that must be controlled via a derived class.  For example, the
SetDiversionStationDelayTablesFromRTN() command extends this class in order to uniquely represent
the command, but much of the functionality is in this base class.
</p>
*/
public abstract class SetDelayTablesFromRTN_Command extends AbstractCommand implements Command
{

/**
Values for SetEfficiency parameter.
*/
protected final String _False = "False";
protected final String _True = "True";
	
/**
Values for the IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public SetDelayTablesFromRTN_Command ()
{	super();
	setCommandName ( "Set?Station?TablesFromRTN" );
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
{	String routine = getClass().getName() + ".checkCommandParameters";
	String InputFile = parameters.getValue ( "InputFile" );
	String SetEfficiency = parameters.getValue ( "SetEfficiency" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (InputFile == null) || (InputFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
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
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify an existing input file." ) );
            }
    
        try {
            String adjustedPath = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, InputFile));
            if ( !IOUtil.fileExists(adjustedPath) ) {
                message = "The input file \"" + adjustedPath + "\" does not exist.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.WARNING, message,
                      	"Specify an existing input file (may be OK if created during processing)." ) );
            }
        }
        catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + InputFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
        }
    }
	
	if ( (SetEfficiency != null) && (SetEfficiency.length() > 0) &&
		!SetEfficiency.equalsIgnoreCase(_False) && !SetEfficiency.equalsIgnoreCase(_True) ) {
		message = "The SetEfficiency value (" + SetEfficiency + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify SetEfficiency as " + _False + " (default) or " + _True + ".") );
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
	List<String> valid_Vector = new Vector<String>(3);
	valid_Vector.add ( "InputFile" );
	valid_Vector.add ( "SetEfficiency" );
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
	return (new SetDelayTablesFromRTN_JDialog ( parent, this )).ok();
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

    PropList parameters = getCommandParameters();
    String InputFile = parameters.getValue ( "InputFile" );
    // TODO SAM 2009-01-24 Currently always a defaults
    String ID = parameters.getValue ( "ID" );
    if ( ID == null ) {
    	ID = "*";
    }
    String SetEfficiency = parameters.getValue ( "SetEfficiency" );
	if ( SetEfficiency == null ) {
		SetEfficiency = _False;	// Default
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    
    // Get the data needed for the command
	
    List<StateMod_Diversion> smdivList = null;
    List<StateMod_Well> smwellList = null;
    int stationListSize = 0;
    boolean doDiv = false;
    boolean doWell = false;
    boolean isReturns = true;
    String datatype = "returns";
    try {
    	if ( this instanceof SetDiversionStationDelayTablesFromRTN_Command ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		smdivList = dataList;
    		doDiv = true;
    		stationListSize = smdivList.size();
    	}
    	else if ( (this instanceof SetWellStationDelayTablesFromRTN_Command) ||
    		(this instanceof SetWellStationDepletionTablesFromRTN_Command)) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
    		smwellList = dataList;
    		doWell = true;
    		stationListSize = smwellList.size();
    		if ( this instanceof SetWellStationDepletionTablesFromRTN_Command ) {
    			datatype = "depletions";
    			isReturns = false;
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
    
	String InputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
            StateDMICommandProcessorUtil.expandParameterValue(processor,this,InputFile)));
	if ( !IOUtil.fileExists(InputFile_full) ) {
		message = "Input file \"" + InputFile_full + "\" does not exist.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
        status.addToLog(command_phase,
            new CommandLogRecord( CommandStatusType.FAILURE, message,
                "Verify that the file exists and is readable."));
	}
   
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	int matchCount = 0;
    	List<LegacyReturnFlow> returnList = null;

    	try {
    		returnList = LegacyReturnFlow.readReturnFile ( InputFile_full );
    	}
    	catch ( Exception e ) {
    		message = "Error reading return flow file \"" +	InputFile_full + "\" +(" + e + ").";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
            status.addToLog(command_phase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Verify that the file exists and is readable."));
    		throw new Exception ( message );
    	}

    	// Loop through the stations.  If a matching record is found in the
    	// return flow file, use the information.

    	StateMod_Diversion div = null;
    	StateMod_Well well = null;
    	StateMod_ReturnFlow smret = null;
    	LegacyReturnFlow ret = null; // Legacy return flow object read from input file.
    	List<StateMod_ReturnFlow> smret_Vector = null; // List of smret.
    	String id = null;// Diversion ID.
    	int iret, ieff;	// Loop counters for returns and efficiencies.
    	int nret; // Number of returns.
    	int pos; // Position within data from file, matching diversion.
    	for ( int i = 0; i < stationListSize; i++ ) {
    		if ( doDiv ) {
    			div = smdivList.get(i);
    			id = div.getID();
    		}
    		else if ( doWell ) {
    			well = smwellList.get(i);
    			id = well.getID();
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Find the return flow from the file...

    		pos = StateMod_Util.indexOf ( returnList, id );
    		if ( pos < 0 ) {
    			Message.printStatus ( 2, routine, "Cannot find station \"" + id +
    			"\" in the return flow file - not setting " + datatype + " for this station." );
    			continue;
    		}
    		ret = (LegacyReturnFlow)returnList.get(pos);

    		// Reset the data with the specified filled values.

    		Message.printStatus ( 2, routine, "Setting " + id + " " + datatype + " -> from return file" );

    		nret = ret.getNumReturns();
    		smret_Vector = new Vector<StateMod_ReturnFlow>();	// Do not reuse!
    		for ( iret = 0; iret < nret; iret++ ) {
    			if ( doDiv ) {
    				smret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_DIVERSION_STATIONS );
    			}
    			else if ( doWell ) {
    				smret = new StateMod_ReturnFlow ( StateMod_DataSet.COMP_WELL_STATIONS );
    			}
    			smret.setCrtnid ( ret.getReturnID(iret) );
    			smret.setPcttot ( ret.getReturnPercent(iret) );
    			if ( ret.getReturnTable(iret).length() > 0 ) {
    				smret.setIrtndl ( ret.getReturnTable(iret) );
    			}
    			else {
    				// Use the default...
    				smret.setIrtndl ( ret.getDefaultReturnTable() );
    			}
    			smret_Vector.add ( smret );
    		}
    		if ( doDiv ) {
    			div.setReturnFlow ( smret_Vector );
    		}
    		else {
    			if ( isReturns ) {
    				well.setReturnFlows ( smret_Vector );
    			}
    			else {
    				well.setDepletions ( smret_Vector );
    			}
    		}
    		// Set the efficiency...
    		if ( SetEfficiency.equalsIgnoreCase("true") ) {
    			// Set the annual and monthly values to the same...
    			if ( doDiv ) {
    				div.setDivefc ( ret.getEfficiency() );
    				for ( ieff = 0; ieff < 12; ieff++ ) {
    					div.setDiveff ( ieff, ret.getEfficiency() );
    				}
    			}
    			if ( doWell ) {
    				well.setDivefcw ( ret.getEfficiency() );
    				for ( ieff = 0; ieff < 12; ieff++ ) {
    					well.setDiveff ( ieff, ret.getEfficiency() );
    				}
    			}
    		}
    	}
		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "No identifiers were matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifiers are correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "No identifiers were matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifiers are correct." ) );
			}
		}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error setting delay tables (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
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

	String InputFile = parameters.getValue ( "InputFile" );
	String SetEfficiency = parameters.getValue ( "SetEfficiency" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"");
	}
	if ( (SetEfficiency != null) && (SetEfficiency.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetEfficiency=" + SetEfficiency );
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
