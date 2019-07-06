// AggregateWellRights_Command - This class initializes, checks, and runs the AggregateWellRights() command.

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

import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;
import DWR.StateMod.StateMod_WellRight;
import DWR.StateMod.StateMod_Well_CollectionType;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the AggregateWellRights() command.
</p>
*/
public class AggregateWellRights_Command 
extends AbstractCommand implements Command
{
	
protected final String _1 = "1";
protected final String _AppropriationDate = "AppropriationDate";

private final int __AppropriationDate_int = 1000099;	// Special value

// Administration numbers as doubles...
private double [] __AdminNumClasses_double = new double[0];
	
/**
Constructor.
*/
public AggregateWellRights_Command ()
{	super();
	setCommandName ( "AggregateWellRights" );
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
	String AdminNumClasses = parameters.getValue( "AdminNumClasses" );
	String OnOffDefault = parameters.getValue( "OnOffDefault" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (OnOffDefault != null) && (OnOffDefault.length() > 0) &&
		!OnOffDefault.equalsIgnoreCase(_AppropriationDate) && !StringUtil.isInteger(OnOffDefault)) {
		message = "The OnOffDefault value (" + OnOffDefault + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify OnOffDefault as an integer year or " + _AppropriationDate + " (default).") );
	}
	
	if ( (AdminNumClasses != null) && !AdminNumClasses.equals("") ) {
		List<String> v = StringUtil.breakStringList ( AdminNumClasses, " ,", StringUtil.DELIM_SKIP_BLANKS );
		if ( (v == null) || (v.size() == 0) ) {
			message = "AdminNumClasses has zero values.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify AdminNumClasses as a list of comma-separated NNNNN.NNNNN values.") );
		}
		int nAdminNumClasses = v.size();
		__AdminNumClasses_double = new double[nAdminNumClasses];
		for ( int i = 0; i < nAdminNumClasses; i++ ) {
			String val = (String)v.get(i);
			if ( !StringUtil.isDouble(val) ) {
				message = "AdminNumClass value " + val + " is not a number.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify AdminNumClasses as a list of comma-separated NNNNN.NNNNN values.") );
			}
			else {
				__AdminNumClasses_double[i] = Double.parseDouble(val);
			}
		}
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(2);
    valid_Vector.add ( "AdminNumClasses" );
    valid_Vector.add ( "OnOffDefault" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
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
	return (new AggregateWellRights_JDialog ( parent, this )).ok();
}

/**
Merge rights from multiple parcel years, as follows.  Loop through the
years with parcel data, comparing two at a time.  For example, if rights are
available from parcels in 1998 and 2002, for each location, get the list of
rights from the 1998 and the list of rights from the 2002 data.  If the following is true:
<ol>
<li>	A well right in 1998 and 2002 has the same well ID.</li>
<li>	and has the same well ID (location)</li>
<li>	and has the same well type (class)</li>
<li>	and has the same administration number</li>
</ol>
They are a match and should be included for the location, where the decree is
the maximum of the two years.
Any that don't match are totalled and the year with the largest sum of rights is included.
@param smwells Vector of StateMod_Well, needed to determine whether a collection and to get
the well station name.
@param smrights_orig Vector of StateMod_WellRight, the original list with multiple
parcel years, before merging.
@return the merged list of StateMod_WaterRight.
*/
private List<StateMod_WellRight> aggregateWellRights ( List<StateMod_Well> smwells, List<StateMod_WellRight> smrights_orig, int OnOffDefault_int,
	int warningLevel, int warningCount, String commandTag, CommandStatus status)
{	String routine = "aggregateWellRights_command.aggregateWellRights";
	
	// Determine the unique list of well locations for the rights (independent of year)...
	
	List<String> smwells_loc = StateMod_Util.getWaterRightLocationList ( smrights_orig, -1 );
	
	int smwells_size = 0;
	if ( smwells_loc != null ) {
		smwells_size = smwells_loc.size();
	}
	String id_loc;
	List<StateMod_WellRight> smrights = new Vector<StateMod_WellRight>();	// Rights returned after the merge
	StateMod_Well well = null;	// Well for location
	// Loop through the well station locations, by identifier...
	for ( int iwell = 0; iwell < smwells_size; iwell++ ) {
		id_loc = smwells_loc.get(iwell);
		int pos = StateMod_Util.indexOf ( smwells, id_loc );
		well = null;	// Default - well station not found
		boolean gw_only = false;
		boolean is_aggregate = false;
		if ( pos >= 0 ) {
			// Found a matching well
			well = smwells.get(pos);
		}
		if ( well != null ) {
			// Determine whether aggregate/collection, etc.
			gw_only = well.hasGroundwaterOnlySupply();
			if ( well.getCollectionType() == StateMod_Well_CollectionType.AGGREGATE ) {
				is_aggregate = true;
			}
			// Also include all D&W to keep the number of rights down in the results
			if ( !well.getIdvcow2().equals("") && !well.getIdvcow2().equals("N/A") &&
				!well.getIdvcow2().equals("NA") ) {
				is_aggregate = true;
			}
		}
		Message.printStatus ( 2, routine, "Aggregating location " + id_loc + " well rights." );
		// Get all the rights at the location, for all years (although the data for multiple
		// years should have been merged previously).
		List<StateMod_WellRight> rights_loc = StateMod_Util.getWaterRightsForLocation ( smrights_orig, id_loc, -1 );
		// If no rights, no need to process...
		if ( rights_loc.size() == 0 ) {
			continue;
		}
		// Initialize the aggregate rights based on the number of classes that were
		// specified as parameters...
		if ( is_aggregate && (__AdminNumClasses_double.length > 0) ) {
			// Declare and initialize lists each time for a location...
			List<StateMod_WellRight> rights_aggregated_loc = new Vector<StateMod_WellRight>();// For a location
			initializeAggregateRights ( id_loc, well, gw_only, rights_aggregated_loc );
			double [] sum_decree_irtem = new double[__AdminNumClasses_double.length];
			double [] sum_decree = new double[__AdminNumClasses_double.length];
			int [] sum_count = new int[__AdminNumClasses_double.length];
			aggregateWellRightsForLocation ( rights_loc, rights_aggregated_loc,
					sum_decree_irtem, sum_decree, sum_count,
					warningLevel, warningCount, commandTag, status);
			List<StateMod_WellRight> smrights_nonzero =
					removeZeroDecreeAggregateRightsForLocation ( id_loc, rights_aggregated_loc,
					sum_decree_irtem, sum_decree, sum_count, OnOffDefault_int,
					warningLevel, warningCount, commandTag, status );
			// Now add the aggregate rights to the final results...
			int rights_aggregated_loc_size = smrights_nonzero.size();
			for ( int iright = 0; iright < rights_aggregated_loc_size; iright++ ) {
				smrights.add ( smrights_nonzero.get(iright) );
			}
		}
		else {
			// Just add the rights to the final list.
			int rights_loc_size = rights_loc.size();
			for ( int iright = 0; iright < rights_loc_size; iright++ ) {
				smrights.add ( rights_loc.get(iright) );
			}
		}
	}
	return smrights;
}

/**
Accumulate the data using classes.  This logic follows the old watright logic in concept.
@param rights_loc Rights at a location.
@param rights_aggregated_loc Aggregated rights at a location.
*/
private void aggregateWellRightsForLocation ( List<StateMod_WellRight> rights_loc, List<StateMod_WellRight> rights_aggregated_loc,
		double [] sum_decree_irtem, double [] sum_decree, int [] sum_count,
		int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = "aggregateWellRights_Command.aggregateWellRightsForLocation";
	// Initialize the statistics...
	
	for ( int ic = 0; ic < __AdminNumClasses_double.length; ic++ ) {
		sum_decree_irtem[ic] = 0.0;
		sum_decree[ic] = 0.0;
		sum_count[ic] = 0;
	}
	// Loop through the rights at the location...
	StateMod_WellRight wellr, wellr_single;
	double irtem;	// Individual well right admin number as double
	double decree;
	int rights_loc_size = rights_loc.size();
	boolean found_class;
	for ( int iright = 0; iright < rights_loc_size; iright++ ) {
		wellr_single = rights_loc.get ( iright );
		decree = wellr_single.getDecree();
		irtem = StringUtil.atod(wellr_single.getAdministrationNumber());
		found_class = false;
		// Compare the decree to classes...
		for ( int ic = 0; ic < __AdminNumClasses_double.length; ic++ ) {
			if ( __AdminNumClasses_double[ic] >= irtem ) {
				// Found the water right...
				wellr = (StateMod_WellRight)rights_aggregated_loc.get ( ic );
				Message.printStatus ( 2, routine, "For aggregate right \"" + wellr.getID() +
					"\", adding " +	StringUtil.formatString(irtem,"%.5f") + " decree " +
					StringUtil.formatString(decree,"%.2f") + " \"" + wellr.getName() + "\"");
					wellr.setDcrdivw (wellr.getDcrdivw() +decree );
				sum_decree[ic] += decree;
				// Weight the decree based on the appropriation date.
				// First convert the admin number to an appropriation date...
				DateTime appro_date = null;
				try {
					HydroBase_AdministrationNumber admin_data = new HydroBase_AdministrationNumber ( irtem );
					appro_date = admin_data.getAppropriationDate();
				}
				catch ( Exception e ) {
					String message = "Error converting administration number " + irtem +
					" to date.  Skipping right.";
			        Message.printWarning ( warningLevel, 
			            MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
			        Message.printWarning ( 3, routine, e );
			        status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Should not happen - contact support." ) );
					continue;
				}
				// Now use the appropriation date without the prior
				// adjudication date to get a new administration number...
				try {
					HydroBase_AdministrationNumber admin_data = new HydroBase_AdministrationNumber ( appro_date );
					sum_decree_irtem[ic] +=	decree*admin_data.getAdminNumber();
				}
				catch ( Exception e ) {
					String message = "Error converting appropration date " + appro_date +
					" to administration number.  Skipping right.";
			        Message.printWarning ( warningLevel, 
			            MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
			        Message.printWarning ( 3, routine, e );
			        status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Should not happen - contact support." ) );
					continue;
				}
				++sum_count[ic];
				found_class = true;
				break;
			}
		}
		if ( !found_class ) {
			String message = "Administration number " + irtem +
			" is > the maximum administration number in classes.  Skipping right.";
	        Message.printWarning ( warningLevel, 
	            MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
	        status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Add a large bounding administration number to classes (e.g., 99999.99999)." ) );
		}
	}
}

/**
Initialize aggregate rights.  Create empty (zero) decree rights for each
administration number class.  The classes will be added to as rights are
processed.  Resulting zero decrees can be removed later.
@param id_loc Location identifier for well station.
@param gw_only Indicates if the location is groundwater only supply - impacts formatting of the rights.
@param rights_aggregated_loc Aggregated rights at a location.  Should initially
be an empty Vector and will be filled in with new StateMod_WellRight for each class.
*/
private void initializeAggregateRights ( String id_loc, StateMod_Well well, boolean gw_only, List<StateMod_WellRight> rights_aggregated_loc )
{	StateMod_WellRight wellr;
	for ( int ic = 0; ic < __AdminNumClasses_double.length; ic++ ) {
		wellr = new StateMod_WellRight ();

		if ( gw_only ) {
			// As per watright, do not use a "W" in the water right ID...
			wellr.setID ( id_loc + "." + StringUtil.formatString( (ic + 1), "%02d") );
		}
		else {
			// As per watright, use a "W" in the water right ID...
			wellr.setID ( id_loc + "W." + StringUtil.formatString( (ic + 1), "%02d") );
		}
		if ( well != null ) {
			wellr.setName ( well.getName() );
		}
		else {
			wellr.setName( id_loc );
		}
		wellr.setCgoto ( id_loc );
		// Initialize...
		wellr.setIrtem ( "0.00000" );
		wellr.setDcrdivw ( 0.0 );
		// Will be set later (no need to set here)
		//wellr.setSwitch ( OnOffDefault_int );
		rights_aggregated_loc.add ( wellr );
	}
}

/**
Remove zero decree aggregate rights since they are not needed. The check is
done by formatting the decree to %.2f since that is the format for StateMod
files.
Also reset the admin number using:
<pre>
    irtem = int(sum_decree_irtem/sum_decree)
</pre>
Use an integer because the original administration numbers were integers and any
remainder might mistakenly be converted to an appropriation date.
@param rights_aggregated_loc Aggregated rights at a location.
*/
private List<StateMod_WellRight> removeZeroDecreeAggregateRightsForLocation ( String id_loc,
	List<StateMod_WellRight> rights_aggregated_loc,
	double [] sum_decree_irtem, double [] sum_decree, int [] sum_count,	int OnOffDefault_int,
	int warningLevel, int warningCount, String commandTag, CommandStatus status )
{	String routine = "aggregateWellRights_Command.removeZeroDecreeAggregateRightsForLocation";
	Message.printStatus ( 2, routine, "Finalizing rights for \"" + id_loc + "\"..." );
	StateMod_WellRight wellr;
	String decree_string;
	double irtem;
	// Loop through the aggregate rights, which should match in number the
	// water rights classes set up during initialization.  Only save ones that have non-zero decree.
	List<StateMod_WellRight> nonzero_rights = new Vector<StateMod_WellRight>();
	for ( int ic = 0; ic < __AdminNumClasses_double.length; ic++ ) {
		wellr = rights_aggregated_loc.get(ic);
		decree_string = StringUtil.formatString( sum_decree[ic], "%.2f");
		if ( decree_string.equalsIgnoreCase("0.00")){
			Message.printStatus ( 2, routine, "Decree for " + wellr.getID() +
				" class " + StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") +
				" is decree=" + wellr.getDcrdivw() + " which is 0.00 when formatted for " +
				"output." + "...skipping." );
		}
		else if ( sum_count[ic] > 0 ) {
			// Make sure that irtem is handled as an integer below...
			irtem = sum_decree_irtem[ic]/sum_decree[ic];
			wellr.setIrtem ( StringUtil.formatString((int)irtem, "%d") + ".00000" );
			Message.printStatus ( 2, routine, "Decree-weighted administration number "
				+ "for " + wellr.getID() + " class " +
				StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") +
				" is " + wellr.getIrtem() + " decree=" +
				StringUtil.formatString(wellr.getDcrdivw(),"%.2f") );
			// Set the switch...
			if ( OnOffDefault_int == __AppropriationDate_int ) {
				// Convert the administration number to a year.  The
				// starting value must be an integer...
				try { HydroBase_AdministrationNumber admin_data = new HydroBase_AdministrationNumber ( (int)irtem );
					DateTime appro_date = admin_data.getAppropriationDate();
					wellr.setSwitch ( appro_date.getYear() );
				}
				catch ( Exception e ) {
					// Should not happen...
					String message = "Error converting administration number " + irtem +
					" to date.  Setting OnO/Off switch to 1.";
					Message.printWarning ( warningLevel, 
			            MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
			        status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Should not happen - contact support." ) );
					wellr.setSwitch ( 1 );
				}
			}
			else {
				// Use the default value for the administration number...
				wellr.setSwitch ( OnOffDefault_int );
			}
			// Add to the list (this is the only place a nonzero right is added)...
			nonzero_rights.add ( wellr );
		}
		else {
			// Else no rights so don't add for the well...
			Message.printStatus ( 2, routine, "No rights (or zero total) for right " +
					wellr.getID() + " class " +
					StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") + "...not adding." );
		}
	}
	return nonzero_rights;
}

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	String commandTag = "" + command_number;
	int warningLevel = 2;
	int warningCount = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	// AdminNumClasses parsed in checkCommandParameters
	PropList parameters = getCommandParameters();
	String OnOffDefault = parameters.getValue( "OnOffDefault" );
	
	int OnOffDefault_int = __AppropriationDate_int;	// Default
	if ( OnOffDefault != null ) {
		if ( OnOffDefault.equalsIgnoreCase(_AppropriationDate) ) {
			Message.printStatus ( 2, routine,
				"Water right OnOff switch will be set to the year of the appropriation date." );
			OnOffDefault_int = __AppropriationDate_int;
		}
		else  {
			if ( StringUtil.isInteger(OnOffDefault) ) {
				OnOffDefault_int = StringUtil.atoi(OnOffDefault);
				Message.printStatus ( 2, routine,
					"Water right OnOff switch will be set to " + OnOffDefault );
			}
		}
	}

	// Set defaults...

	// Get the list of well stations...
	
	List<StateMod_Well> stationList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List");
		stationList = dataList;
		if ( stationList.size() == 0 ) {
			message = "No well stations are available for processing.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Read or create well stations associated with well rights before using this command." ) );
		}
	}
	catch ( Exception e ) {
		message = "Error requesting well station data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	// Get the list of well rights (probably empty)...
	
	List<StateMod_WellRight> rightList = null;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List");
		rightList = dataList;
		if ( rightList.size() == 0 ) {
			message = "No well rights are available for processing.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Read or create well rights before using this command." ) );
		}
	}
	catch ( Exception e ) {
		message = "Error requesting well right data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( commandTag, ++warningCount),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	if ( warningCount > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	try {
		// Aggregate the well rights...
		
		List<StateMod_WellRight> smrights = aggregateWellRights ( stationList, rightList, OnOffDefault_int,
				warningLevel, warningCount, commandTag, status);
		
		// Set the rights back in the processor...
		
		try {
			processor.setPropContents ( "StateMod_WellRight_List", smrights );
		}
		catch ( Exception e ) {
			message = "Unable to set well rights for processor.  Results of aggregation will not be evident.";
	        Message.printWarning ( warningLevel, 
	                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
	        Message.printWarning ( 3, routine, e );
	        status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Check log file for details." ) );
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error aggregating well rights (" + e + ").";
        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }

	if ( warningCount > 0 ) {
		message = "There were " + warningCount + " warnings processing the command.";
		Message.printWarning ( warningLevel,
		MessageUtil.formatMessageTag(commandTag, ++warningCount),routine,message);
		throw new CommandException ( message );
	}
	
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
	
	String AdminNumClasses = parameters.getValue ( "AdminNumClasses" );
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	
	StringBuffer b = new StringBuffer ();

	if ( AdminNumClasses != null && 
			AdminNumClasses.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AdminNumClasses=\"" + AdminNumClasses + "\"" );
	}
	if ( OnOffDefault != null && OnOffDefault.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffDefault=\"" + OnOffDefault + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
