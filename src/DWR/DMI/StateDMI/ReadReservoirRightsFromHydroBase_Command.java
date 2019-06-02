// ReadReservoirRightsFromHydroBase_Command - This class initializes, checks, and runs the ReadReservoirRightsFromHydroBase() command.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirRight;
import DWR.StateMod.StateMod_Util;

import RTi.DMI.DMIUtil;
import RTi.Util.Math.MathUtil;
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
This class initializes, checks, and runs the ReadReservoirRightsFromHydroBase() command.
</p>
*/
public class ReadReservoirRightsFromHydroBase_Command 
extends AbstractCommand implements Command
{
	
/**
Standard values for OnOffDefault parameter, as well as YYYY accepted.
*/
protected final String _1 = "1";
protected final String _AppropriationDate = "AppropriationDate";

/**
Administration number classes as array of doubles.  Initialize to zero length to avoid checks on
null in processing code.
*/
private double [] __AdminNumClasses_double = new double[0];

/**
Constructor.
*/
public ReadReservoirRightsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadReservoirRightsFromHydroBase" );
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
	String ID = parameters.getValue ( "ID" );
	String DecreeMin = parameters.getValue ( "DecreeMin" );
	String AdminNumClasses = parameters.getValue ( "AdminNumClasses" );
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
		message = "An identifier or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the identifier pattern to match." ) );
	}
	
	if ( (DecreeMin != null) && !DecreeMin.equals("") && !StringUtil.isDouble(DecreeMin) ) {
		message = "The decree minumum (" + DecreeMin + ") is not a number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the decree minimum as a number." ) );
	}

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
			String val = v.get(i);
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
	List<String> valid_Vector = new Vector<String>();
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "DecreeMin" );
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
{	return (new ReadReservoirRightsFromHydroBase_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String DecreeMin = parameters.getValue ( "DecreeMin" );
	if ( DecreeMin == null ) {
		DecreeMin = ".0005"; // Default as per watright
	}
	double DecreeMin_double = Double.parseDouble(DecreeMin);
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	if ( OnOffDefault == null ) {
		OnOffDefault = _AppropriationDate; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of reservoir stations...
	
	List<StateMod_Reservoir> stationList = null;
	int stationListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Reservoir> dataList = (List<StateMod_Reservoir>)processor.getPropContents ( "StateMod_ReservoirStation_List");
		stationList = dataList;
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting reservoir station data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
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
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		int OnOffDefault_int = 1; // Will be reset or ignored.
		boolean OnOffDefault_year = true; // Is year used for switch?
		if ( OnOffDefault != null ) {
			if ( OnOffDefault.equalsIgnoreCase("AppropriationDate") ) {
				Message.printStatus ( 2, routine,
				"Water right OnOff switch will be set to the year of the appropriation date." );
				OnOffDefault_year = true;
			}
			else {
				OnOffDefault_year = false;
				OnOffDefault_int = StringUtil.atoi(OnOffDefault);
				Message.printStatus ( 2, routine, "Water right OnOff switch will be set to " + OnOffDefault );
			}
		}

		int nAdminNumClasses = __AdminNumClasses_double.length;
		double [] sum_decree_irtem = new double[nAdminNumClasses]; // Sum of decree*irtem for weighting
		double [] sum_decree = new double[nAdminNumClasses]; // Sum of decree*irtem for weighting
		int [] sum_count = new int[nAdminNumClasses]; // Count for sum.

		StateMod_Reservoir res = null; // StateMod reservoir station to process
		List<HydroBase_NetAmts> hbresr_Vector = null; // List of rights from HydroBase
		List<HydroBase_NetAmts> hbresr_sorted_Vector = new Vector<HydroBase_NetAmts>(100);
						// List of rights from HydroBase,after manual sort on admin number
		List<HydroBase_NetAmts> hbresr_part_Vector =null;// List of rights for an aggregate part.
		int nhbresr = 0; // The number of rights read from HydroBase
		int nhbresr_part = 0; // The number of rights read from HydroBase, for a collection part
		HydroBase_NetAmts hbresr = null; // Single right from HydroBase
		StateMod_ReservoirRight resr = null; // Single StateMod reservoir right
		int resr_count = 0; // Count of reservoir rights to add (accounts for some being ignored).
		int ir = 0; // Counter for rights in loop.
		List<String> parts = null;
		int psize = 0; // Number of parts in a collection
		int iparts = 0; // Index for iterating through parts
		String part_id = ""; // Identifier for a part in a collection
		int [] wdid_parts = new int[2];	// Parts when a WDID is parsed
		double decree = 0.0;
		String id;	// Reservoir ID.
		List<String> order_Vector = new Vector<String>(1);
		order_Vector.add ("net_amts.admin_no");
		order_Vector.add ("net_amts.order_no");
						// Sometimes a WDID will have rights
						// with the same admin number.  The
						// order number is the "tie-breaker" -
						// it is not shown in the StateMod
						// output but does control the order of
						// the rights.
		boolean do_absolute = true;	// True for absolute rights, false for conditional
						// TODO SAM 2004-09-07 need to enable
						// parameter?  Seems like only absolute is used.
		boolean is_aggregate = false; // Indicate whether the reservoir is an aggregate
		boolean is_system = false; // or system
		int ic = 0; // Loop counter for water right classes.
		double irtem = 0.0; // StateMod water right admin number.
		List<StateMod_ReservoirRight> resr_agg_Vector = new Vector<StateMod_ReservoirRight>(100); // References to the water rights for classes.
		HydroBase_AdministrationNumber admin_data;
						// Used to convert between administration number and appropriation date
		DateTime appro_date = null; // Appropriation date for an administration number.
		double [] irtem_array = null; // Used to sort rights in a collection.
		int [] sort_order = null; // Array used when sorting rights in a collection.
		//int matchCount = 0;
		for ( int i = 0; i < stationListSize; i++ ) {
			res = stationList.get(i);
			id = res.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			//++matchCount;
			// Defaults for each structure...

			decree = StateMod_Util.MISSING_DOUBLE;

			// Read the right from HydroBase.  If an aggregate, read each right and aggregate...

			is_aggregate = false;
			is_system = false;
			hbresr_Vector = null; // Set to null so can re-use for aggregates
			if ( res.isCollection() ) {
				if ( res.getCollectionType().equalsIgnoreCase(StateMod_Reservoir.COLLECTION_TYPE_AGGREGATE)) {
					is_aggregate = true;
				}
				if ( res.getCollectionType().equalsIgnoreCase(StateMod_Reservoir.COLLECTION_TYPE_SYSTEM)) {
					is_system = true;
				}
			}

			if ( is_aggregate || is_system ) {
				// Aggregate or system.  Get the water rights for each reservoir in the collection...
				psize = 0;
				parts = res.getCollectionPartIDs(0);
				if ( parts != null ) {
					psize = parts.size();
				}
				for ( iparts = 0; iparts < psize; iparts++ ) {
					part_id = (String)parts.get(iparts);
					try {
						// Parse out the WDID...
						HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
					}
					catch ( Exception e ) {
						// Not a WDID - this is an error because valid structures are expected as
						// parts of an aggregate...
						message = "Location \"" + id + "\" has part (" + part_id + ") that is not a WDID.";
				        Message.printWarning ( warning_level, 
				            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
				        status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Verify that the part identifier is a WDID and that aggregate definition is correct." ) );
						continue;
					}
					try {
						hbresr_part_Vector = hbdmi.readNetAmtsList (
							DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
						// Add to the main vector...
						if ( hbresr_Vector == null ) {
							hbresr_Vector = new Vector<HydroBase_NetAmts>(50);
						}
						nhbresr_part = 0;
						if ( hbresr_part_Vector != null ) {
							nhbresr_part = hbresr_part_Vector.size();
						}
						for ( int ihb = 0; ihb < nhbresr_part; ihb++ ) {
							hbresr_Vector.add ( hbresr_part_Vector.get(ihb) );
						}
					}
					catch ( Exception e ) {
						message = "Error getting net amount rights data from HydroBase for " + id + "(" +
						part_id + ")";
						Message.printWarning ( warning_level, 
						    MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
						Message.printWarning ( 3, routine, e );
					        status.addToLog ( CommandPhaseType.RUN,
								new CommandLogRecord(CommandStatusType.FAILURE,
									message, "Verify the identifier." ) );
						continue;
					}
				}
				// If a system or aggregate without classes defined, sort the water rights by
				// administration number.  This will make sure that the insert order
				// is by administration number.
				nhbresr = 0;
				resr_count = 0;
				if ( hbresr_Vector != null ) {
					nhbresr = hbresr_Vector.size();
				}
				if ( ((is_aggregate && (nAdminNumClasses == 0)) || is_system) && (nhbresr > 0) ) {
					irtem_array = new double[nhbresr];
					for ( ir = 0; ir < nhbresr; ir++ ) {
						hbresr = (HydroBase_NetAmts)hbresr_Vector.get(ir);
						irtem_array[ir] = hbresr.getAdmin_no();
					}
					sort_order = new int[nhbresr];
					MathUtil.sort ( irtem_array,
						MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING, sort_order, true );
					hbresr_sorted_Vector.clear();
					for ( ir = 0; ir < nhbresr; ir++ ) {
						hbresr_sorted_Vector.add ( hbresr_Vector.get( sort_order[ir]) );
					}
					hbresr_Vector = hbresr_sorted_Vector;
				}
			}
			else {
				// Single reservoir - get its water rights...
				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - non-fatal, just ignore the reservoir...
					Message.printStatus ( 2, routine,
					"Skipping reservoir \"" + id + "\" - does not appear to be a WDID." );
					continue;
				}
				try {
					hbresr_Vector =	hbdmi.readNetAmtsList (
							DMIUtil.MISSING_INT, wdid_parts[0], wdid_parts[1], false, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine,
					"Error getting net amount rights from HydroBase for " + id + " - may not be in HydroBase." );
					Message.printWarning ( 3, routine, e );
					// TODO SAM 2009-01-19 Evaluate how to handle better
					continue;
				}
			}

			// Now process the water rights from HydroBase_NetAmts to StateMod_ReservoirRight...

			nhbresr = 0;
			resr_count = 0;
			if ( hbresr_Vector != null ) {
				nhbresr = hbresr_Vector.size();
			}
			if ( is_aggregate && (nAdminNumClasses > 0) ) {
				// Create water rights for each class.  Remove the zero rights below.  This is easier
				// than trying to figure out what has to be created as rights are processed
				resr_agg_Vector.clear();
				for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
					resr = new StateMod_ReservoirRight ();
					resr.setID ( id + "." + StringUtil.formatString( (ic + 1), "%02d") );
					resr.setName ( res.getName() );
					resr.setCgoto ( id );
					// Initialize...
					resr.setRtem ( "0.00000" );
					resr.setDcrres ( 0.0 );
					resr.setSwitch ( OnOffDefault_int );
					// Keep a local list to optimize processing...
					resr_agg_Vector.add ( resr );
					sum_decree_irtem[ic] = 0.0;
					sum_decree[ic] = 0.0;
					sum_count[ic] = 0;
				}
			}
			for ( ir = 0; ir < nhbresr; ir++ ) {
				hbresr = (HydroBase_NetAmts)hbresr_Vector.get(ir);
				if ( do_absolute ) {
					// Processing absolute rights...
					decree = hbresr.getNet_vol_abs();
				}
				else {
					decree = hbresr.getNet_vol_cond();
				}
				if ( decree < DecreeMin_double ) {
					message = "Reservoir station \"" + id + "\" has right with decree "
					+ decree + " < the minimum (" + DecreeMin + ") skipping...";
					Message.printStatus ( 2, routine, message );
					continue;
				}
				irtem = hbresr.getAdmin_no();
				if ( is_aggregate && (nAdminNumClasses > 0) ) {
					// Accumulate the data using classes.  This logic follows the old watright logic in
					// concept.

					// Check the decree...
					for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
						if ( __AdminNumClasses_double[ic] >= irtem ) {
							// Found the water right...
							resr = (StateMod_ReservoirRight)resr_agg_Vector.get ( ic );
							Message.printStatus ( 2, routine, "For aggregate right \"" + resr.getID() +
							"\", adding " + StringUtil.formatString(irtem,"%.5f") + " decree " + decree);
							resr.setDcrres ( resr.getDcrres() + decree );
							sum_decree[ic] += decree;
							// Weight the decree based on the appropriation date.
							// First convert the admin number to an appropriation date...
							admin_data = new HydroBase_AdministrationNumber ( irtem );
							appro_date = admin_data.getAppropriationDate();
							// Now use the appropriation date without the prior adjudication date to
							// get a new administration number...
							admin_data = new HydroBase_AdministrationNumber ( appro_date );
							sum_decree_irtem[ic] += decree*admin_data.getAdminNumber();
							++sum_count[ic];
							break;
						}
					}
				}
				else {
					// Anything but an aggregate so add each water right explicitly...
					++resr_count;
					resr = new StateMod_ReservoirRight ();
					resr.setID ( id + "." + StringUtil.formatString(resr_count, "%02d") );
					resr.setName ( hbresr.getWr_name() );
					resr.setCgoto ( id );
					resr.setRtem ( StringUtil.formatString(irtem, "%.5f"));
					resr.setDcrres ( decree );
					if ( OnOffDefault_year ) {
						// Convert the administration number to a year...
						admin_data = new HydroBase_AdministrationNumber (irtem );
						appro_date = admin_data.getAppropriationDate();
						resr.setSwitch ( admin_data.getAppropriationDate().getYear() );
					}
					else {
						// Use the default value for the administration number...
						resr.setSwitch ( OnOffDefault_int );
					}
					processor.findAndAddSMReservoirRight ( resr, true );
				}
			}
			if ( is_aggregate && (nAdminNumClasses > 0) ) {
				// Post process the rights:
				//
				// 1. If there are no rights in a class, remove the
				//    right.
				// 2. Reset the admin number using:
				//
				//	irtem = int(sum_decree_irtem/sum_decree)
				Message.printStatus ( 2, routine, "Finalizing rights for \"" + id + "\"..." );
				for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
					resr = (StateMod_ReservoirRight)resr_agg_Vector.get(ic);
					if ( (sum_count[ic] > 0) && (sum_decree[ic] > 0.0) ) {
						irtem = sum_decree_irtem[ic]/sum_decree[ic];
						resr.setRtem ( StringUtil.formatString((int)irtem, "%d") + ".00000" );
						Message.printStatus ( 2, routine, "Decree-weighted administration number "
						+ "for " + resr.getID() + " class " +
						StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") +
						" is " + resr.getRtem() + " decree=" + resr.getDcrres() );
						if ( OnOffDefault_year ) {
							// Convert the administration number to a year.  The
							// starting value must be an integer...
							admin_data = new HydroBase_AdministrationNumber ( (int)irtem );
							appro_date = admin_data.getAppropriationDate();
							resr.setSwitch ( admin_data.getAppropriationDate().getYear() );
						}
						else {
							// Use the default value for the administration number...
							resr.setSwitch ( OnOffDefault_int );
						}
						processor.findAndAddSMReservoirRight (resr, true);
					}
					else {
						// Else no rights so don't add for the reservoir
						Message.printStatus ( 2, routine, "No rights (or zero total) for right " +
						resr.getID() + " class " + StringUtil.formatString(
						__AdminNumClasses_double[ic],"%.5f") + "...not adding." );
					}
				}
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error reading instream flow rights from HydroBase (" + e + ").";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
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
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{	
	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	
	String ID = parameters.getValue ( "ID" );
	String DecreeMin = parameters.getValue ( "DecreeMin" );
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	String AdminNumClasses = parameters.getValue ( "AdminNumClasses" );
		
	StringBuffer b = new StringBuffer ();

	if ( ID != null && ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( DecreeMin != null && DecreeMin.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DecreeMin=" + DecreeMin );
	}
	if ( OnOffDefault != null && OnOffDefault.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffDefault=" + OnOffDefault );
	}
	if ( AdminNumClasses != null && AdminNumClasses.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AdminNumClasses=\"" + AdminNumClasses  + "\"");
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
