// FillAndSetReservoirStation_Command - This class initializes, checks, and runs the FillReservoirStation() and SetReservoirStation(), commands.

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

import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirAccount;
import DWR.StateMod.StateMod_ReservoirAreaCap;
import DWR.StateMod.StateMod_ReservoirClimate;
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
<p>
This class initializes, checks, and runs the FillReservoirStation() and SetReservoirStation(),
commands.  It is an abstract base class that must be controlled via a derived class.  For example,
the SetReservoirStation() command extends this class in order to uniquely represent the command,
but much of the functionality is in this base class.
</p>
*/
public abstract class FillAndSetReservoirStation_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Data for the evaporation stations.
*/
private String [] __EvapStations_id = null;
private double [] __EvapStations_percent = null;

/**
Data for the precipitation stations.
*/
private String [] __PrecipStations_id = null;
private double [] __PrecipStations_percent = null;

/**
Content/area/seepage data.
*/
private double [] __ContentAreaSeepage_content = null;
private double [] __ContentAreaSeepage_area = null;
private double [] __ContentAreaSeepage_seepage = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public FillAndSetReservoirStation_Command ()
{	super();
	setCommandName ( "?Fill/Set?ReservoirStation" );
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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	String OnOff = parameters.getValue ( "OnOff" );
	String OneFillRule = parameters.getValue ( "OneFillRule" );
	String DailyID = parameters.getValue ( "DailyID" );
	String ContentMin = parameters.getValue ( "ContentMin" );
	String ContentMax = parameters.getValue ( "ContentMax" );
	String ReleaseMax = parameters.getValue ( "ReleaseMax" );
	String DeadStorage = parameters.getValue ( "DeadStorage" );
	String AccountID = parameters.getValue ( "AccountID" );
	//String AccountName = parameters.getValue ( "AccountName" );
	String AccountMax = parameters.getValue ( "AccountMax" );
	String AccountInitial = parameters.getValue ( "AccountInitial" );
	String AccountEvap = parameters.getValue ( "AccountEvap" );
	String AccountOneFill = parameters.getValue ( "AccountOneFill" );
	String EvapStations = parameters.getValue ( "EvapStations" );
	String PrecipStations = parameters.getValue ( "PrecipStations" );
	String ContentAreaSeepage = parameters.getValue ( "ContentAreaSeepage" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The instream flow station ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the instream flow station ID to process." ) );
	}
	
	if ( (RiverNodeID != null) && (RiverNodeID.length() > 0) &&
		((RiverNodeID.indexOf(" ") >= 0) || (RiverNodeID.indexOf("-") >= 0))) {
        message = "The RiverNodeID (" + RiverNodeID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the RiverNodeID without spaces or dashes." ) );
	}
	
	if ( (OnOff != null) && (OnOff.length() > 0) && !StringUtil.isInteger(OnOff) ) {
        message = "The OnOff value (" + OnOff + ") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the OnOff value as a number." ) );
	}
	
	if ( (OneFillRule != null) && (OneFillRule.length() > 0) && !StringUtil.isInteger(OneFillRule) ) {
        message = "The OneFillRule value (" + OneFillRule + ") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the OneFillRule value as a number." ) );
	}
	
	if ( (DailyID != null) && (DailyID.length() > 0) &&
		((DailyID.indexOf(" ") >= 0) || (DailyID.indexOf("-") >= 0))) {
        message = "The DailyID (" + DailyID + ") contains spaces or dashes.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the DailyID without spaces or dashes." ) );
	}
	
	if ( (ContentMin != null) && (ContentMin.length() > 0) && !StringUtil.isDouble(ContentMin) ) {
        message = "The content minimum (" + ContentMin + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the content minimum as a number." ) );
	}
	if ( (ContentMax != null) && (ContentMax.length() > 0) ) {
		if ( !StringUtil.isDouble(ContentMax) ) {
	        message = "The content maximum (" + ContentMax + ") is not a number.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the content maximum as a number." ) );
		}
		// Also check against the minimum
		else if ( StringUtil.isDouble(ContentMin) &&
			StringUtil.atod(ContentMin) > StringUtil.atod(ContentMax) ) {
			message = "The minimum content (" + ContentMin +
				") is greater than the maximum content (" + ContentMax + ").";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the content minimum <= the content maximum." ) );
		}
	}
	if ( (ReleaseMax != null) && (ReleaseMax.length() > 0) && !StringUtil.isDouble(ReleaseMax) ) {
		message = "The release maximum (" + ReleaseMax + ") is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the release maximum as a number." ) );
	}
	if ( (DeadStorage != null) && (DeadStorage.length() > 0) ) {
		if ( !StringUtil.isDouble(DeadStorage) ) {
			message = "The dead storage (" + DeadStorage + ") is not a number.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the dead storage as a number." ) );
		}
		else if( StringUtil.isDouble(ContentMax) &&
			StringUtil.atod(DeadStorage) > StringUtil.atod(ContentMax) ) {
			message = "The dead storage (" + DeadStorage +
				") is greater than the content maximum (" + ContentMax + ").";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the dead storage less <= the content maximum." ) );
		}
	}
	if ( (AccountID != null) && (AccountID.length() > 0) && !StringUtil.isInteger(AccountID) ) {
        message = "The AccountID (" + AccountID + ") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the AccountID as a number." ) );
	}
	if ( (AccountMax != null) && (AccountMax.length() > 0) && !StringUtil.isDouble(AccountMax) ) {
		message = "The account maximum storage (" + AccountMax + ") is not a number";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the account maximum as a number." ) );
	}
	if ( (AccountInitial != null) && (AccountInitial.length() > 0) ) {
		if ( !StringUtil.isDouble(AccountInitial) ) {
			message = "The account initial storage (" + AccountInitial + ") is not a number.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the account initial storage as a number." ) );
		}
		else if(StringUtil.isDouble(AccountMax) &&
			StringUtil.atod(AccountInitial) > StringUtil.atod(AccountMax) ) {
			message = "The account initial storage (" + AccountInitial +
				") is greater than the account maximum (" + AccountMax + ").";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the account initial storage <= the account maximum." ) );
		}
	}
	if ( (AccountEvap != null) && (AccountEvap.length() > 0) && !StringUtil.isInteger(AccountEvap) ) {
        message = "The AccountEvap value (" + AccountEvap + ") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the AccountEvap value as an integer." ) );
	}
	if ( (AccountOneFill != null) && (AccountOneFill.length() > 0) && !StringUtil.isInteger(AccountOneFill) ) {
        message = "The AccountOneFill value (" + AccountOneFill + ") is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the AccountOneFill value as an integer." ) );
	}
	if ( (EvapStations != null) && (EvapStations.length() > 0) ) {
		// Make sure values are in pairs of two...
		List tokens = StringUtil.breakStringList(EvapStations, " ,;",StringUtil.DELIM_SKIP_BLANKS);
		if ( (tokens == null) || (tokens.size()%2 != 0) ) {
			message = "Evaporation station data are not specified as pairs of ID,%.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the evaporation station data as pairs of ID,%." ) );
		}
		else {
			int nsta = tokens.size()/2;
			__EvapStations_id = new String[nsta];
			__EvapStations_percent = new double[nsta];
			String percent;
			String id;
			for ( int i = 0; i < nsta; i++ ) {
				// TODO SAM 2004-06-06 Need to check station ID if a list is supplied...
				// Check the percent...
				id = (String)tokens.get(i*2);
				percent = (String)tokens.get(i*2 + 1);
				if ( !StringUtil.isDouble(percent) ) {
					message = "Evaporation station " + id + " percent (" + percent + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the evaporation station data as pairs of ID,%." ) );
				}
				else {
					__EvapStations_id[i] = id;
					__EvapStations_percent[i] = Double.parseDouble(percent);
				}
			}
		}
	}
	if ( (PrecipStations != null) && (PrecipStations.length() > 0) ) {
		// Make sure values are in pairs of two...
		List tokens = StringUtil.breakStringList(PrecipStations, " ,;", StringUtil.DELIM_SKIP_BLANKS);
		if ( (tokens == null) || (tokens.size()%2 != 0) ) {
			message = "Precipitation station data are not specified as pairs of ID,%.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the precipitation station data as pairs of ID,%." ) );
		}
		else {
			int nsta = tokens.size()/2;
			String id, percent;
			__PrecipStations_id = new String[nsta];
			__PrecipStations_percent = new double[nsta];
			for ( int i = 0; i < nsta; i++ ) {
				// TODO SAM 2004-06-06 Need to check station ID if a list is supplied...
				// Check the percent...
				id = (String)tokens.get(i*2);
				percent = (String)tokens.get(i*2 + 1);
				percent = (String)tokens.get(i*2 + 1);
				if ( !StringUtil.isDouble(percent) ) {
					message = "Precipitation station " + (String)tokens.get(i*2) + " percent (" +
						percent + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the evaporation station data as pairs of ID,%." ) );
				}
				else {
					__PrecipStations_id[i] = id;
					__PrecipStations_percent[i] = Double.parseDouble(percent);
				}
			}
		}
	}
	if ( (ContentAreaSeepage != null) && (ContentAreaSeepage.length() > 0) ) {
		// Make values are in triplets...
		List tokens = StringUtil.breakStringList(ContentAreaSeepage, ",; \n",StringUtil.DELIM_SKIP_BLANKS);
		boolean badData = false; // Used specifically here
		if ( (tokens == null) || (tokens.size()%3 != 0) ) {
			message = "Content/Area/Seepage data are not specified as triplets of content,area,seepage.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Specify the content/area/seepage data as triplets of content,area,seepage." ) );
			badData = true;
		}
		else {
			int ndata = tokens.size()/3;
			__ContentAreaSeepage_content = new double[ndata];
			__ContentAreaSeepage_area = new double[ndata];
			__ContentAreaSeepage_seepage = new double[ndata];
			String val;
			for ( int i = 0; i < ndata; i++ ) {
				// Check the content...
				val = (String)tokens.get(i*3 );
				if ( !StringUtil.isDouble(val) ) {
					message = "Content (" + val + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the data as triplets of content,area,seepage numbers." ) );
					badData = true;
				}
				else {
					__ContentAreaSeepage_content[i] = StringUtil.atod(val);
				}
				// Check the area...
				val = (String)tokens.get(i*3 + 1);
				if ( !StringUtil.isDouble(val) ) {
					message = "Area (" + val + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the data as triplets of content,area,seepage numbers." ) );
					badData = true;
				}
				else {
					__ContentAreaSeepage_area[i] = StringUtil.atod(val);
				}
				// Check the seepage...
				val = (String)tokens.get(i*3 + 2);
				if ( !StringUtil.isDouble(val) ) {
					message = "Seepage (" + val + ") is not a number.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the data as triplets of content,area,seepage numbers." ) );
					badData = true;
				}
				else {
					__ContentAreaSeepage_seepage[i] = StringUtil.atod(val);
				}
			}
			if ( !badData ) {
				// Make sure that the values increase...
				for ( int i = 1; i < ndata; i++ ) {
					if ( __ContentAreaSeepage_content[i] < __ContentAreaSeepage_content[i - 1] ) {
						message = "Content(" + (i + 1) + "): " + __ContentAreaSeepage_content[i] + " < " +
							__ContentAreaSeepage_content[ i - 1] +	" from previous item.";
				        warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify each content value >= the previous value." ) );
					}
					if ( __ContentAreaSeepage_area[i] < __ContentAreaSeepage_area[i - 1] ) {
						message = "Area(" + (i + 1) + "): " + __ContentAreaSeepage_area[i] + " < " +
							__ContentAreaSeepage_area[ i - 1] + " from previous item.";
				        warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify each area value >= the previous value." ) );
					}
					if ( __ContentAreaSeepage_seepage[i] < __ContentAreaSeepage_seepage[i - 1] ) {
						message = "Seepage(" + (i + 1) + "): " + __ContentAreaSeepage_seepage[i] + " < " +
							__ContentAreaSeepage_seepage[ i - 1] + " from previous item.";
				        warning += "\n" + message;
				        status.addToLog ( CommandPhaseType.INITIALIZATION,
				            new CommandLogRecord(CommandStatusType.FAILURE,
				                message, "Specify each seepage value >= the previous value." ) );
					}
				}
			}
			if ( ndata > 0 ) {
				// Make sure that a zero record is specified...
				if ( (__ContentAreaSeepage_content[0] > 0.0) || (__ContentAreaSeepage_area[0] > 0.0) || (__ContentAreaSeepage_seepage[0] > 0.0) ) {
					message = "First content/area/seepage is not all zeros.";
			        warning += "\n" + message;
			        status.addToLog ( CommandPhaseType.INITIALIZATION,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Specify the first content/area/seepage record as all zeros." ) );
				}
			}
		}
	}

	if ( this instanceof SetReservoirStation_Command ) {
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
	valid_Vector.add ( "OneFillRule" );
	valid_Vector.add ( "DailyID" );
	valid_Vector.add ( "ContentMin" );
	valid_Vector.add ( "ContentMax" );
	valid_Vector.add ( "ReleaseMax" );
	valid_Vector.add ( "DeadStorage" );
	valid_Vector.add ( "AccountID" );
	valid_Vector.add ( "AccountName" );
	valid_Vector.add ( "AccountMax" );
	valid_Vector.add ( "AccountInitial" );
	valid_Vector.add ( "AccountEvap" );
	valid_Vector.add ( "AccountOneFill" );
	valid_Vector.add ( "EvapStations" );
	valid_Vector.add ( "PrecipStations" );
	valid_Vector.add ( "ContentAreaSeepage" );
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
	return (new FillAndSetReservoirStation_JDialog ( parent, this )).ok();
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
	String RiverNodeID = parameters.getValue ( "RiverNodeID" );
	if ( RiverNodeID != null ) {
		RiverNodeID = RiverNodeID.trim();
	}
	String OnOff = parameters.getValue ( "OnOff" );
	if ( OnOff != null ) {
		OnOff = OnOff.trim();
	}
	String OneFillRule = parameters.getValue ( "OneFillRule" );
	if ( OneFillRule != null ) {
		OneFillRule = OneFillRule.trim();
	}
	String DailyID = parameters.getValue ( "DailyID" );
	if ( DailyID != null ) {
		DailyID = DailyID.trim();
	}
	String ContentMin = parameters.getValue ( "ContentMin" );
	if ( ContentMin != null ) {
		ContentMin = ContentMin.trim();
	}
	String ContentMax = parameters.getValue ( "ContentMax" );
	if ( ContentMax != null ) {
		ContentMax = ContentMax.trim();
	}
	String ReleaseMax = parameters.getValue ( "ReleaseMax" );
	if ( ReleaseMax != null ) {
		ReleaseMax = ReleaseMax.trim();
	}
	String DeadStorage = parameters.getValue ( "DeadStorage" );
	if ( DeadStorage != null ) {
		DeadStorage = DeadStorage.trim();
	}
	String AccountID = parameters.getValue ( "AccountID" );
	if ( AccountID != null ) {
		AccountID = AccountID.trim();
	}
	String AccountName = parameters.getValue ( "AccountName" );
	if ( AccountName != null ) {
		AccountName = AccountName.trim();
	}
	String AccountMax = parameters.getValue ( "AccountMax" );
	if ( AccountMax != null ) {
		AccountMax = AccountMax.trim();
	}
	String AccountInitial = parameters.getValue ( "AccountInitial" );
	if ( AccountInitial != null ) {
		AccountInitial = AccountInitial.trim();
	}
	String AccountEvap = parameters.getValue ( "AccountEvap" );
	if ( AccountEvap != null ) {
		AccountEvap = AccountEvap.trim();
	}
	String AccountOneFill = parameters.getValue ( "AccountOneFill" );
	if ( AccountOneFill != null ) {
		AccountOneFill = AccountOneFill.trim();
	}
	String EvapStations = parameters.getValue ( "EvapStations" );
	if ( EvapStations != null ) {
		EvapStations = EvapStations.trim();
	}
	String PrecipStations = parameters.getValue ( "PrecipStations" );
	if ( PrecipStations != null ) {
		PrecipStations = PrecipStations.trim();
	}
	String ContentAreaSeepage = parameters.getValue ( "ContentAreaSeepage" );
	if ( ContentAreaSeepage != null ) {
		ContentAreaSeepage = ContentAreaSeepage.trim();
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
	IfNotFound = IfNotFound.trim();
	
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");

    // Get the data needed for the command
    
    List reservoirStationList = null;
    int reservoirStationListSize = 0;
    try {
    	reservoirStationList = (List)processor.getPropContents ( "StateMod_ReservoirStation_List" );
    	reservoirStationListSize = reservoirStationList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting reservoir station data to process (" + e + ").";
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
    		OnOff_int = Integer.parseInt ( OnOff );
    	}

    	boolean fill_OneFillRule = false;
    	int OneFillRule_int = 0;
    	if ( (OneFillRule != null) && (OneFillRule.length() > 0) ) {
    		fill_OneFillRule = true;
    		OneFillRule_int = Integer.parseInt ( OneFillRule );
    	}

    	boolean fill_DailyID = false;
    	if ( DailyID != null ) {
    		fill_DailyID = true;
    	}

    	boolean fill_ContentMin = false;
    	double ContentMin_double = 0.0;
    	if ( (ContentMin != null) && (ContentMin.length() > 0) ) {
    		fill_ContentMin = true;
    		ContentMin_double = Double.parseDouble ( ContentMin );
    	}

    	boolean fill_ContentMax = false;
    	double ContentMax_double = 0.0;
    	if ( (ContentMax != null) && (ContentMax.length() > 0) ) {
    		fill_ContentMax = true;
    		ContentMax_double = Double.parseDouble ( ContentMax );
    	}

    	boolean fill_ReleaseMax = false;
    	double ReleaseMax_double = 0.0;
    	if ( (ReleaseMax != null) && (ReleaseMax.length() > 0) ) {
    		fill_ReleaseMax = true;
    		ReleaseMax_double = Double.parseDouble ( ReleaseMax );
    	}

    	boolean fill_DeadStorage = false;
    	double DeadStorage_double = 0.0;
    	if ( (DeadStorage != null) && (DeadStorage.length() > 0) ) {
    		fill_DeadStorage = true;
    		DeadStorage_double = Double.parseDouble ( DeadStorage );
    	}

    	boolean fill_AccountID = false;
    	if ( (AccountID != null) && (AccountID.length() > 0) ) {
    		fill_AccountID = true;
    	}

    	boolean fill_AccountName = false;
    	if ( (AccountName != null) && (AccountName.length() > 0) ) {
    		fill_AccountName = true;
    	}

    	boolean fill_AccountMax = false;
    	double AccountMax_double = 0.0;
    	if ( (AccountMax != null) && (AccountMax.length() > 0) ) {
    		fill_AccountMax = true;
    		AccountMax_double = Double.parseDouble ( AccountMax );
    	}

    	boolean fill_AccountInitial = false;
    	double AccountInitial_double = 0.0;
    	if ( (AccountInitial != null) && (AccountInitial.length() > 0) ) {
    		fill_AccountInitial = true;
    		AccountInitial_double = Double.parseDouble(AccountInitial);
    	}

    	boolean fill_AccountEvap = false;
    	int AccountEvap_int = 0;
    	if ( (AccountEvap != null) && (AccountEvap.length() > 0) ) {
    		fill_AccountEvap = true;
    		AccountEvap_int = Integer.parseInt ( AccountEvap );
    	}

    	boolean fill_AccountOneFill = false;
    	int AccountOneFill_int = 0;
    	if ( (AccountOneFill != null) && (AccountOneFill.length() > 0) ) {
    		fill_AccountOneFill = true;
    		AccountOneFill_int = Integer.parseInt ( AccountOneFill );
    	}

    	boolean fill_EvapStations = false;
    	if ( (EvapStations != null) && (EvapStations.length() > 0) ) {
    		fill_EvapStations = true;
    	}

    	boolean fill_PrecipStations = false;
    	if ( (PrecipStations != null) && (PrecipStations.length() > 0) ) {
    		fill_PrecipStations = true;
    	}

    	boolean fill_ContentAreaSeepage = false;
    	if ( (ContentAreaSeepage != null) && (ContentAreaSeepage.length() > 0)){
    		fill_ContentAreaSeepage = true;
    	}
    	
    	// Use logic as per code pulled out of StateDMI_Processor
    	
    	StateMod_Reservoir res = null;
    	StateMod_ReservoirAccount account = null;
    	String id;
    	int matchCount = 0;
    	String action = "Setting ";
    	boolean fill = false;
    	if ( this instanceof FillReservoirStation_Command ) {
    		action = "Filling ";
    	}
    	List climate_Vector = null; // Used to set evaporation and precipitation station data.
    	for (int i = 0; i < reservoirStationListSize; i++) {
    		res = (StateMod_Reservoir)reservoirStationList.get(i);
    		id = res.getID();
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;
    		// Have a match so reset the data...
    		if ( fill_Name && (!fill || StateMod_Util.isMissing(res.getName())) ) {
    			Message.printStatus ( 2, routine, action + id + " Name -> " + Name );
    			res.setName ( Name );
    		}
    		if ( fill_RiverNodeID && (!fill || StateMod_Util.isMissing(res.getCgoto())) ) {
    			Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID );
    			if ( RiverNodeID.equalsIgnoreCase("ID") ) {
    				// Set the river node ID to the same as the normal identifier...
    				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + res.getID());
    				res.setCgoto ( res.getID() );
    			}
    			else {
    				// Set to the given value...
    				Message.printStatus ( 2, routine, action + id + " RiverNodeID -> " + RiverNodeID);
    				res.setCgoto ( RiverNodeID );
    			}
    		}
    		if ( fill_OnOff && (!fill || StateMod_Util.isMissing(res.getSwitch())) ) {
    			Message.printStatus ( 2, routine, action + id + " OnOff -> " + OnOff );
    			res.setSwitch ( OnOff_int );
    		}
    		if ( fill_OneFillRule && (!fill || StateMod_Util.isMissing(res.getRdate())) ){
    			Message.printStatus ( 2, routine, action + id + " OneFillRule -> " + OneFillRule);
    			res.setRdate ( OneFillRule_int );
    		}
    		if ( fill_DailyID && (!fill || StateMod_Util.isMissing(res.getCresdy())) ){
    			if ( DailyID.equalsIgnoreCase("ID") ) {
    				// Set the daily ID to the same as the normal identifier...
    				Message.printStatus ( 2, routine, action + id + " DailyID -> " + res.getID() );
    				res.setCresdy ( res.getID() );
    			}
    			else {
    				// Set to the given value...
    				Message.printStatus ( 2, routine, action + id + " DailyID -> " + DailyID );
    				res.setCresdy ( DailyID );
    			}
    		}
    		if ( fill_ContentMin && (!fill || StateMod_Util.isMissing(res.getVolmin())) ) {
    			Message.printStatus ( 2, routine, action + id + " ContentMin -> " + ContentMin );
    			res.setVolmin ( ContentMin_double );
    		}
    		if ( fill_ContentMax && (!fill || StateMod_Util.isMissing(res.getVolmax())) ) {
    			Message.printStatus ( 2, routine, action + id + " ContentMax -> " + ContentMax );
    			res.setVolmax ( ContentMax_double );
    		}
    		if ( fill_ReleaseMax && (!fill|| StateMod_Util.isMissing(res.getFlomax()))){
    			Message.printStatus ( 2, routine, action + id + " ReleaseMax -> " + ReleaseMax );
    			res.setFlomax ( ReleaseMax_double );
    		}
    		if ( fill_DeadStorage && (!fill|| StateMod_Util.isMissing(res.getDeadst()))){
    			Message.printStatus ( 2, routine, action + id + " DeadStorage -> " + DeadStorage );
    			res.setDeadst ( DeadStorage_double );
    		}
    		// TODO SAM 2004-09-13 only support setting accounts
    		// (not filling) since probably need multiple commands for each reservoir.
    		if ( fill_AccountID ) {
    			Message.printStatus ( 2, routine, "Setting " + id + " AccountID -> " + AccountID );
    			List accounts = null;
    			if ( AccountID.equalsIgnoreCase("1") ) {
    				// If the first account for the reservoir, remove the old accounts...
    				accounts = new Vector ();
    			}
    			else {
    				// Get the old accounts and edit...
    				accounts = res.getAccounts();
    			}
    			account = new StateMod_ReservoirAccount ();
    			account.setID ( AccountID );
    			if ( fill_AccountName ) {
    				Message.printStatus ( 2, routine, "Setting " + id + " AccountID " + AccountID
    				+ " AccountName -> " + AccountName );
    				account.setName(AccountName);
    			}
    			if ( fill_AccountMax ) {
    				Message.printStatus ( 2, routine,
    				"Setting " + id + " AccountID " + AccountID + " AccountMax -> " + AccountMax );
    				account.setOwnmax(AccountMax_double);
    			}
    			if ( fill_AccountInitial ) {
    				Message.printStatus ( 2, routine,
    				"Setting " + id + " AccountID " + AccountID + " AccountInitial -> " + AccountInitial );
    				account.setCurown( AccountInitial_double);
    			}
    			if ( fill_AccountEvap ) {
    				Message.printStatus ( 2, routine,
    				"Setting " + id + " AccountID " + AccountID + " AccountEvap -> " + AccountEvap );
    				account.setPcteva( AccountEvap_int);
    			}
    			if ( fill_AccountOneFill ) {
    				Message.printStatus ( 2, routine,
    				"Setting " + id + " AccountID " + AccountID + " AccountOneFill -> " + AccountOneFill);
    				account.setN2own( AccountOneFill_int );
    			}
    			accounts.add ( account );
    			res.setAccounts ( accounts );
    		}
    		climate_Vector = null;
    		if ( fill_EvapStations && (!fill || (
    			StateMod_ReservoirClimate.getNumEvap(res.getClimates()) == 0)) ) {
    			Message.printStatus ( 2, routine, action + id + " EvapStations -> " + EvapStations );
    			if ( climate_Vector == null ) {
    				climate_Vector = new Vector ( __EvapStations_percent.length );
    			}
    			StateMod_ReservoirClimate evap;
    			for ( int ievap = 0; ievap < __EvapStations_percent.length; ievap++ ) {
    				evap = new StateMod_ReservoirClimate ();
    				evap.setType ( StateMod_ReservoirClimate.CLIMATE_EVAP);
    				evap.setID ( __EvapStations_id[ievap] );
    				evap.setWeight ( __EvapStations_percent[ievap] );
    				climate_Vector.add ( evap );
    			}
    		}
    		if ( fill_PrecipStations && (!fill || (
    			StateMod_ReservoirClimate.getNumPrecip(res.getClimates()) == 0)) ) {
    			Message.printStatus ( 2, routine, action + id + " PrecipStations -> " + PrecipStations );
    			if ( climate_Vector == null ) {
    				climate_Vector = new Vector ( __PrecipStations_percent.length );
    			}
    			StateMod_ReservoirClimate precip;
    			for ( int iprecip = 0; iprecip < __PrecipStations_percent.length; iprecip++ ) {
    				precip = new StateMod_ReservoirClimate ();
    				precip.setType (StateMod_ReservoirClimate.CLIMATE_PTPX);
    				precip.setID(__PrecipStations_id[iprecip]);
    				precip.setWeight(__PrecipStations_percent[iprecip]);
    				climate_Vector.add ( precip );
    			}
    		}
    		if ( climate_Vector != null ) {
    			res.setClimates ( climate_Vector );
    		}
    		if ( fill_ContentAreaSeepage && (!fill || ( (res.getAreaCaps().size()) == 0)) ) {
    			Message.printStatus ( 2, routine, action + id + " ContentAreaSeepage -> " + ContentAreaSeepage );
    			StateMod_ReservoirAreaCap areacap;
    			List areacap_Vector = new Vector(__ContentAreaSeepage_content.length);
    			for ( int iareacap = 0; iareacap < __ContentAreaSeepage_content.length; iareacap++ ) {
    				areacap = new StateMod_ReservoirAreaCap ();
    				areacap.setConten(__ContentAreaSeepage_content[iareacap]);
    				areacap.setSurarea(__ContentAreaSeepage_area[iareacap]);
    				areacap.setSeepage(__ContentAreaSeepage_seepage[iareacap]);
    				areacap_Vector.add ( areacap );
    			}
    			res.setAreaCaps ( areacap_Vector );
    		}
    	}

    	if ( !fill && (matchCount == 0) && (ID.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add")) {
	    	// If nothing was matched for set command and the idpattern does not contain a
	    	// wildcard, add a StateMod_Reservoir at the end...
			res = new StateMod_Reservoir ( false );
			id = ID;
			res.setID ( id );
			reservoirStationList.add ( res );
			Message.printStatus ( 2, routine, "Adding reservoir station " + id );
			if ( fill_Name ) {
				Message.printStatus ( 2, routine, "Setting " + id + " Name -> " + Name );
				res.setName ( Name );
			}
			if ( fill_RiverNodeID ) {
				if ( RiverNodeID.equalsIgnoreCase("ID") ) {
					Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + res.getID() );
					res.setCgoto ( res.getID() );	
				}
				else {
					Message.printStatus ( 2, routine, "Setting " + id + " RiverNodeID -> " + RiverNodeID );
					res.setCgoto ( RiverNodeID );
				}
			}
			if ( fill_OnOff ) {
				Message.printStatus ( 2, routine, "Setting " + id + " OnOff -> " + OnOff );
				res.setSwitch ( OnOff_int );
			}
			if ( fill_OneFillRule ) {
				Message.printStatus ( 2, routine, "Setting " + id + " OneFillRule -> " + OneFillRule );
				res.setRdate ( OneFillRule_int );
			}
			if ( fill_DailyID ) {
				if ( DailyID.equalsIgnoreCase("ID") ) {
					// Set the daily ID to the same as the normal identifier...
					Message.printStatus ( 2, routine, action + id + " DailyID -> " + res.getID() );
					res.setCresdy ( res.getID() );
				}
				else {
					Message.printStatus ( 2, routine, "Setting " + id + " DailyID -> " + DailyID );
					res.setCresdy ( DailyID );
				}
			}
			if ( fill_ContentMin ) {
				Message.printStatus ( 2, routine, "Setting " + id + " ContentMin -> " + ContentMin );
				res.setVolmin ( ContentMin_double );
			}
			if ( fill_ContentMax ) {
				Message.printStatus ( 2, routine, "Setting " + id + " ContentMax -> " + ContentMax );
				res.setVolmax ( ContentMax_double );
			}
			if ( fill_ReleaseMax ) {
				Message.printStatus ( 2, routine, "Setting " + id + " ReleaseMax -> " + ReleaseMax );
				res.setFlomax ( ReleaseMax_double );
			}
			if ( fill_DeadStorage ) {
				Message.printStatus ( 2, routine, "Setting " + id + " DeadStorage -> " + DeadStorage );
				res.setDeadst ( DeadStorage_double );
			}
			if ( fill_AccountID ) {
				Message.printStatus ( 2, routine, "Setting " + id + " AccountID -> " + AccountID);
				List accounts = null;
				if ( AccountID.equalsIgnoreCase("1") ) {
					// If the first account for the reservoir, remove the old accounts...
					Message.printStatus ( 2, routine, "Setting " + id +
						" first clearing all accounts since setting the first account.");
					accounts = new Vector ();
				}
				else {
					// Get the old accounts and edit...
					accounts = res.getAccounts();
				}
				account = new StateMod_ReservoirAccount ();
				account.setID ( AccountID );
				if ( fill_AccountName ) {
					Message.printStatus ( 2, routine, "Setting " + id + " AccountID " +
					AccountID + " AccountName -> " + AccountName );
					account.setName(AccountName);
				}
				if ( fill_AccountMax ) {
					Message.printStatus ( 2, routine, "Setting " + id + " AccountID " +
					AccountID + " AccountMax -> " + AccountMax );
					account.setOwnmax(AccountMax_double);
				}
				if ( fill_AccountInitial ) {
					Message.printStatus ( 2, routine, "Setting " + id + " AccountID " +
					AccountID + " AccountInitial -> " + AccountInitial );
					account.setCurown(AccountInitial_double);
				}
				if ( fill_AccountEvap ) {
					Message.printStatus ( 2, routine, "Setting " + id + " AccountID " +
					AccountID + " AccountEvap -> " + AccountEvap );
					account.setPcteva( AccountEvap_int);
				}
				if ( fill_AccountOneFill ) {
					Message.printStatus ( 2, routine, "Setting " + id + " AccountID " +
					AccountID + " AccountOneFill -> "+AccountOneFill);
					account.setN2own( AccountOneFill_int );
				}
				// Set the accounts back.
				accounts.add ( account );
				res.setAccounts ( accounts );
			}
			climate_Vector = null;
			if ( fill_EvapStations ) {
				Message.printStatus ( 2, routine, "Setting " + id + " EvapStations -> " + EvapStations);
				StateMod_ReservoirClimate evap;
    			if ( climate_Vector == null ) {
    				climate_Vector = new Vector ( __EvapStations_percent.length );
    			}
				for ( int ievap = 0; ievap < __EvapStations_percent.length; ievap++ ) {
					evap = new StateMod_ReservoirClimate ();
					evap.setType ( StateMod_ReservoirClimate.CLIMATE_EVAP);
					evap.setID ( __EvapStations_id[ievap] );
					evap.setWeight ( __EvapStations_percent[ievap] );
					climate_Vector.add ( evap );
				}
			}
			if ( fill_PrecipStations ) {
				Message.printStatus ( 2, routine, "Setting " + id + " PrecipStations -> " + PrecipStations);
				StateMod_ReservoirClimate precip;
    			if ( climate_Vector == null ) {
    				climate_Vector = new Vector ( __PrecipStations_percent.length );
    			}
				for ( int iprecip = 0; iprecip < __PrecipStations_percent.length; iprecip++ ) {
					precip=new StateMod_ReservoirClimate ();
					precip.setType ( StateMod_ReservoirClimate.CLIMATE_PTPX);
					precip.setID ( __PrecipStations_id[iprecip] );
					precip.setWeight ( __PrecipStations_percent[iprecip] );
					climate_Vector.add ( precip );
				}
			}
			if ( climate_Vector != null ) {
				res.setClimates ( climate_Vector );
			}
			if ( fill_ContentAreaSeepage ) {
				Message.printStatus ( 2, routine, "Setting " + id + " ContentAreaSeepage -> " + ContentAreaSeepage);
				StateMod_ReservoirAreaCap areacap;
				List areacap_Vector = new Vector(__ContentAreaSeepage_content.length);
				for ( int iareacap = 0; iareacap < __ContentAreaSeepage_content.length; iareacap++ ) {
					areacap=new StateMod_ReservoirAreaCap();
					areacap.setConten( __ContentAreaSeepage_content[iareacap]);
					areacap.setSurarea( __ContentAreaSeepage_area[iareacap]);
					areacap.setSeepage( __ContentAreaSeepage_seepage[iareacap]);
					areacap_Vector.add ( areacap );
				}
				res.setAreaCaps ( areacap_Vector );
				// Increment matchCount so that the warnings below are not printed
				++matchCount;
			}
    	}
		if ( matchCount == 0 ) {
	    	if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
	    		message = "Reservoir station \"" + ID + "\" was not matched: warning and not " +
	    		action.toLowerCase();
    			Message.printWarning(warning_level,
    					MessageUtil.formatMessageTag( command_tag, ++warning_count),
    					routine, message );
    				status.addToLog ( CommandPhaseType.RUN,
    					new CommandLogRecord(CommandStatusType.WARNING,
    						message, "Verify that the identifier is correct." ) );
	    	}
	    	else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
	    		message = "Reservoir station \"" + ID + "\" was not matched: failing and not " +
	    		action.toLowerCase();
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
	String OneFillRule = parameters.getValue ( "OneFillRule" );
	String DailyID = parameters.getValue ( "DailyID" );
	String ContentMin = parameters.getValue ( "ContentMin" );
	String ContentMax = parameters.getValue ( "ContentMax" );
	String ReleaseMax = parameters.getValue ( "ReleaseMax" );
	String DeadStorage = parameters.getValue ( "DeadStorage" );
	String AccountID = parameters.getValue ( "AccountID" );
	String AccountName = parameters.getValue ( "AccountName" );
	String AccountMax = parameters.getValue ( "AccountMax" );
	String AccountInitial = parameters.getValue ( "AccountInitial" );
	String AccountEvap = parameters.getValue ( "AccountEvap" );
	String AccountOneFill = parameters.getValue ( "AccountOneFill" );
	String EvapStations = parameters.getValue ( "EvapStations" );
	String PrecipStations = parameters.getValue ( "PrecipStations" );
	String ContentAreaSeepage = parameters.getValue ( "ContentAreaSeepage" );
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
	if ( (OneFillRule != null) && (OneFillRule.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OneFillRule=" + OneFillRule );
	}
	if ( (DailyID != null) && (DailyID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DailyID=\"" + DailyID + "\"" );
	}
	if ( (ContentMin != null) && (ContentMin.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ContentMin=" + ContentMin );
	}
	if ( (ContentMax != null) && (ContentMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ContentMax=" + ContentMax );
	}
	if ( (ReleaseMax != null) && (ReleaseMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReleaseMax=" + ReleaseMax );
	}
	if ( (DeadStorage != null) && (DeadStorage.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DeadStorage=" + DeadStorage );
	}
	if ( (AccountID != null) && (AccountID.length() > 0) ) {
		AccountID.replace('\n',';');
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountID=" + AccountID );
	}
	if ( (AccountName != null) && (AccountName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountName=\"" + AccountName + "\"");
	}
	if ( (AccountMax != null) && (AccountMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountMax=" + AccountMax );
	}
	if ( (AccountInitial != null) && (AccountInitial.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountInitial=" + AccountInitial );
	}
	if ( (AccountEvap != null) && (AccountEvap.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountEvap=" + AccountEvap );
	}
	if ( (AccountOneFill != null) && (AccountOneFill.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AccountOneFill=" + AccountOneFill );
	}
	if ( (EvapStations != null) && (EvapStations.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EvapStations=\"" + EvapStations + "\"" );
	}
	if ( (PrecipStations != null) && (PrecipStations.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PrecipStations=\"" + PrecipStations + "\"" );
	}
	if ( (ContentAreaSeepage != null) && (ContentAreaSeepage.length() > 0) ) {
		// TODO SAM 2004-09-13 The following does not seem to work...
		//ContentAreaSeepage.replace('\n',';');
		// Reformat completely...
		StringBuffer ContentAreaSeepage_buffer = new StringBuffer ();
		List v2 = StringUtil.breakStringList( ContentAreaSeepage, ",; \n", StringUtil.DELIM_SKIP_BLANKS);
		int size = 0;
		if ( v2 != null ) {
			size = v2.size();
		}
		for ( int i = 0; i < size; i++ ) {
			if ( (i%3) == 0 ) {
				if ( i != 0 ) {
					// Add a "newline" (semi-colon)...
					ContentAreaSeepage_buffer.append ( ';' );
				}
			}
			else if ( ((i - 1)%3 == 0) || ((i - 2)%3 == 0)){
				// Add a comma to separate values...
				ContentAreaSeepage_buffer.append ( ',');
			}
			// Add the item...
			ContentAreaSeepage_buffer.
			append ( (String)v2.get(i) );
		}
		ContentAreaSeepage = ContentAreaSeepage_buffer.toString();
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ContentAreaSeepage=\"" + ContentAreaSeepage + "\"");
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
