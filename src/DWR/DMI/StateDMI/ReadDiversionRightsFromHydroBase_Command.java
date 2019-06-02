// ReadDiversionRightsFromHydroBase_Command - This class initializes, checks, and runs the ReadDiversionRightsFromHydroBase() command.

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

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_AdministrationNumber;
import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.StateDMI.dto.hydrobaserest.HydroBaseRestToolkit;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_DiversionRight;
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
import cdss.dmi.hydrobase.rest.ColoradoHydroBaseRestDataStore;
import cdss.dmi.hydrobase.rest.dao.WaterRightsNetAmount;
import riverside.datastore.DataStore;

/**
<p>
This class initializes, checks, and runs the ReadDiversionRightsFromHydroBase() command.
</p>
*/
public class ReadDiversionRightsFromHydroBase_Command 
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
public ReadDiversionRightsFromHydroBase_Command ()
{	super();
	setCommandName ( "ReadDiversionRightsFromHydroBase" );
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
	String Datastore = parameters.getValue("Datastore");
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
	
	if ( (Datastore == null) || Datastore.isEmpty() ) {
        message = "The datastore must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the datastore." ) );
	}

	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "DecreeMin" );
    valid_Vector.add ( "IgnoreUseType" );
    valid_Vector.add ( "AdminNumClasses" );
    valid_Vector.add ( "OnOffDefault" );
    valid_Vector.add ( "Datastore" );
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
{	return (new ReadDiversionRightsFromHydroBase_JDialog ( parent, this )).ok();
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
	String IgnoreUseType = parameters.getValue ( "IgnoreUseType" );
	String OnOffDefault = parameters.getValue ( "OnOffDefault" );
	if ( OnOffDefault == null ) {
		OnOffDefault = _AppropriationDate; // Default
	}
		
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
		
	// Get the list of diversion stations...
	
	List<StateMod_Diversion> stationList = null;
	int stationListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List");
		stationList = dataList;
		stationListSize = stationList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting diversion station data from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	String Datastore = parameters.getValue("Datastore");
	HydroBaseDMI hbdmi = null;
	ColoradoHydroBaseRestDataStore datastore = null;
	// If there is a datastore open it otherwise open hydrobase dmi
	if( Datastore != null && !Datastore.equals("")){ // Get the datastore
		switch(Datastore){
			case("ColoradoHydroBaseRest"):
				DataStore datastore0 = processor.getDataStoreForName(Datastore, ColoradoHydroBaseRestDataStore.class);
				if ( datastore0 != null){
					Message.printStatus(2, routine, "Selected data store is \"" + Datastore + "\"");
					datastore = (ColoradoHydroBaseRestDataStore)datastore0;
				}
				break;
			case("ColoradoWaterHBGuest"):
				//TODO @jurentie 08-06-2018 is not yet enabled. This acts as a placeholder for potential future datastores
				break;
		}
	}
	else{ // Get the HydroBase DMI...
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
	}
	
	// Process the IgnoreUseType parameter
	
	List<String> IgnoreUseType_Vector = null;
	String [] IgnoreUseType_pattern_Array = null;	// With Java wildcards
	int iignore; // Index for looping through use types to ignore.
	int IgnoreUseType_size = -1;
	if ( IgnoreUseType != null ) {
		IgnoreUseType_Vector = StringUtil.breakStringList(
			IgnoreUseType, ", ", StringUtil.DELIM_SKIP_BLANKS );
		IgnoreUseType_size = IgnoreUseType_Vector.size();
		// Save the separate use types, to be compared with database values below.
		if ( IgnoreUseType_size > 0 ) {
			IgnoreUseType_pattern_Array = new String[IgnoreUseType_size];
			for ( int i = 0; i < IgnoreUseType_size; i++ ) {
				IgnoreUseType_pattern_Array[i] = IgnoreUseType_Vector.get(i);
			}
		}
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

		StateMod_Diversion div = null;	// StateMod diversion station to process
		List<HydroBase_NetAmts> hbdivr_Vector = null;	// List of rights from HydroBase
		List<HydroBase_NetAmts> hbdivr_sorted_Vector = new Vector<HydroBase_NetAmts>(100);
						// List of rights from HydroBase, after manual sort on admin number
		List<HydroBase_NetAmts> hbdivr_part_Vector =null;// Vector of rights for an aggregate part.
		List<WaterRightsNetAmount> datastore_part_list = null; // List of objects returned from web services.
		int nhbdivr = 0; // The number of rights read from HydroBase
		int nhbdivr_part = 0; // The number of rights read from HydroBase, for a collection part
		//WaterRightsNetAmount dsdivr = null; // Used if using a datastore
		HydroBase_NetAmts hbdivr = null;// Single right from HydroBase
		StateMod_DiversionRight divr = null; // Single StateMod diversion right
		int divr_count = 0; // Count of diversion rights to add (accounts for some being ignored).
		int ir = 0; // Counter for rights in loop.
		List<String> parts = null;
		int psize = 0; // Number of parts in a collection
		int iparts = 0; // Index for iterating through parts
		String part_id = ""; // Identifier for a part in a collection
		int [] wdid_parts = new int[2]; // Parts when a WDID is parsed
		double decree = 0.0;
		String id;	// Diversion ID.
		boolean do_absolute = true;	// True for absolute rights, false for conditional
						// TODO SAM 2004-09-07 need to enable
						// parameter?  Seems like only absolute is used.
		boolean is_aggregate = false; // Indicate whether the diversion is an
		boolean is_system = false; // aggregate or system
		int ic = 0; // Loop counter for water right classes.
		double irtem = 0.0; // StateMod water right admin number.
		List<StateMod_DiversionRight> divr_agg_Vector = new Vector<StateMod_DiversionRight>(100); // References to the water rights for classes.
		HydroBase_AdministrationNumber admin_data;
						// Used to convert between administration number and appropriation date
		DateTime appro_date = null;	// Appropriation date for an administration number.
		double [] irtem_array = null; // Used to sort rights in a collection.
		int [] sort_order = null; // Array used when sorting rights in a collection.
		String units; // Units for a water right.
		String use_type; // Use type for a water right.
		String use_type_part; // 3-character use type part for a water right.
		int iuse; // Iterator counter for water right uses
		boolean ignore_right; // Indicate that right should be ignored
		for ( int i = 0; i < stationListSize; i++ ) {
			div = stationList.get(i);
			id = div.getID();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}

			// Defaults for each structure...

			decree = StateMod_Util.MISSING_DOUBLE;

			// Read the right from HydroBase.  If an aggregate, read each right and aggregate...

			is_aggregate = false;
			is_system = false;
			hbdivr_Vector = null; // Set to null so can re-use for aggregates
			if ( div.isCollection() ) {
				if ( div.getCollectionType().equalsIgnoreCase(
					StateMod_Diversion.COLLECTION_TYPE_AGGREGATE)) {
					is_aggregate = true;
				}
				if ( div.getCollectionType().equalsIgnoreCase(
					StateMod_Diversion.COLLECTION_TYPE_SYSTEM)) {
					is_system = true;
				}
			}

			if ( is_aggregate || is_system ) {
				// Aggregate or system.  Get the water rights for each diversion in the collection...
				psize = 0;
				parts = div.getCollectionPartIDs(0);
				if ( parts != null ) {
					psize = parts.size();
				}
				for ( iparts = 0; iparts < psize; iparts++ ) {
					part_id = parts.get(iparts);
					try {
						// Parse out the WDID...
						HydroBase_WaterDistrict.parseWDID(part_id,wdid_parts);
					}
					catch ( Exception e ) {
						// Not a WDID - this is an error because valid structures are expected as
						// parts of an aggregate...
						message = "The location \"" + id + "\" part \"" + part_id + "\" is not a WDID.";
				        Message.printWarning ( warning_level, 
				            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
				        status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the part identifier is a WDID and" +
									" that the " + div.getCollectionType() + " definition is correct." ) );
						continue;
					}
					try {
						// If using a datastore get the data from webservices and then convert to StateDMI POJO
						if ( datastore != null ) {
							datastore_part_list = datastore.getWaterRightsNetAmount(part_id);
							hbdivr_part_Vector = new ArrayList<HydroBase_NetAmts>();
							for(int j = 0; j < datastore_part_list.size(); j++){
								WaterRightsNetAmount waterRight = (WaterRightsNetAmount) datastore_part_list.get(j);
								hbdivr_part_Vector.add(HydroBaseRestToolkit.getInstance().toHydroBaseNetAmounts(waterRight));
							}
						}
						else{
							// Otherwise get data from StateDMI
							hbdivr_part_Vector = hbdmi.readNetAmtsList ( DMIUtil.MISSING_INT,
								wdid_parts[0], wdid_parts[1], false, null );
						}
						// Add to the main vector...
						if ( hbdivr_Vector == null ) {
							hbdivr_Vector = new Vector<HydroBase_NetAmts>(50);
						}
						nhbdivr_part = 0;
						if ( hbdivr_part_Vector != null ) {
							nhbdivr_part = hbdivr_part_Vector.size();
						}
						for ( int ihb = 0; ihb < nhbdivr_part; ihb++ ) {
							hbdivr_Vector.add ( hbdivr_part_Vector.get(ihb) );
						}
					}
					catch ( Exception e ) {
						message = "Error getting net amount rights data from HydroBase for " + id +
						" (part " + part_id + ")";
						Message.printWarning ( 3, routine, e );
				        Message.printWarning ( warning_level, 
				            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
				        status.addToLog ( CommandPhaseType.RUN,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify that the part identifier is a WDID and" +
									" that aggregate definition is correct." ) );
						// TODO SAM 2004-09-08 Is this an error or may some structures not have rights?
						continue;
					}
				}
				// If a system or aggregate without classes defined, sort the water rights
				// by administration number.  This will make sure that the insert order
				// is by administration number.
				nhbdivr = 0;
				divr_count = 0;
				if ( hbdivr_Vector != null ) {
					nhbdivr = hbdivr_Vector.size();
				}
				if ( ((is_aggregate && (nAdminNumClasses == 0)) || is_system) && (nhbdivr > 0) ) {
					irtem_array = new double[nhbdivr];
					for ( ir = 0; ir < nhbdivr; ir++ ) {
						hbdivr = (HydroBase_NetAmts)hbdivr_Vector.get(ir);
						irtem_array[ir] = hbdivr.getAdmin_no();
					}
					sort_order = new int[nhbdivr];
					MathUtil.sort ( irtem_array, MathUtil.SORT_QUICK,
						MathUtil.SORT_ASCENDING, sort_order, true );
					hbdivr_sorted_Vector.clear();
					for ( ir = 0; ir < nhbdivr; ir++ ) {
						hbdivr_sorted_Vector.add ( hbdivr_Vector.get( sort_order[ir]) );
					}
					hbdivr_Vector = hbdivr_sorted_Vector;
				}
			}
			else {
				// Single ditch - get its water rights...
				try {
					// Parse out the WDID...
					HydroBase_WaterDistrict.parseWDID(id,wdid_parts);
				}
				catch ( Exception e ) {
					// Not a WDID - non-fatal, just ignore the diversion...
					Message.printStatus ( 2, routine,
					"Skipping diversion \"" + id + "\" - does not appear to be a WDID." );
					continue;
				}
				try {
					// If using a datastore get the data from webservices and then convert to StateDMI POJO
					if ( datastore != null ) {
						datastore_part_list = datastore.getWaterRightsNetAmount(id);
						hbdivr_Vector = new ArrayList<HydroBase_NetAmts>();
						for(int j = 0; j < datastore_part_list.size(); j++){
							WaterRightsNetAmount waterRight = (WaterRightsNetAmount) datastore_part_list.get(j);
							hbdivr_Vector.add(HydroBaseRestToolkit.getInstance().toHydroBaseNetAmounts(waterRight));
						}
					}
					else{
						// Otherwise get data from StateDMI
						hbdivr_Vector = hbdmi.readNetAmtsList ( DMIUtil.MISSING_INT,
							wdid_parts[0], wdid_parts[1], false, null );
					}
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, command_tag, routine,
					"Error getting net amount rights from HydroBase for " + id );
					Message.printWarning ( 3, routine, e );
					// Not fatal because aggregates and other nodes
					// may not be in HydroBase...
					//++warning_count;
					// FIXME SAM 2009-01-19 Evaluate whether this is really a problem.
					continue;
				}
			}

			// Now process the water rights from HydroBase_NetAmts to StateMod_DiversionRight...

			nhbdivr = 0;
			divr_count = 0;
			if ( hbdivr_Vector != null ) {
				nhbdivr = hbdivr_Vector.size();
			}
			if ( is_aggregate && (nAdminNumClasses > 0) ) {
				// Create water rights for each class.  Remove the zero rights below.  This is easier
				// than trying to figure out what has to be created as rights are processed.
				divr_agg_Vector.clear();
				for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
					divr = new StateMod_DiversionRight ();
					divr.setID ( id + "." + StringUtil.formatString((ic + 1), "%02d") );
					divr.setName ( div.getName() );
					divr.setCgoto ( id );
					// Initialize...
					divr.setIrtem ( "0.00000" );
					divr.setDcrdiv ( 0.0 );
					divr.setSwitch ( OnOffDefault_int );
					// Keep a local list to optimize processing...
					divr_agg_Vector.add ( divr );
					sum_decree_irtem[ic] = 0.0;
					sum_decree[ic] = 0.0;
					sum_count[ic] = 0;
				}
			}
			for ( ir = 0; ir < nhbdivr; ir++ ) {
				hbdivr = (HydroBase_NetAmts)hbdivr_Vector.get(ir);
				if ( do_absolute ) {
					// Processing absolute rights...
					decree = hbdivr.getNet_rate_abs();
				}
				else {
					decree = hbdivr.getNet_rate_cond();
				}
				units = hbdivr.getUnit();
				// Ignore decrees that are smaller than the cutoff...
				if ( decree < DecreeMin_double ) {
					message = "Diversion station \"" + id + "\" has right with decree "
					+ decree + " < the minimum (" + DecreeMin + ") skipping...";
					Message.printStatus ( 2, routine, message );
					continue;
				}
				// Ignore if the use type is in the ignore list.
				if ( IgnoreUseType_size > 0 ) {
					ignore_right = false;
					// Get the HydroBase use type, which consists of 3-character use types all smashed
					// together into one string...
					use_type = hbdivr.getUse();
					if ( Message.isDebugOn ) {
						Message.printDebug ( 2, routine, "Use type = \"" + use_type + "\"");
					}
					// Loop through the 3-character parts of the use_type and compare each with
					// the use types to be ignored...
					int num_use_types = 0;
					if ( use_type.length() > 0 ) {
						num_use_types = use_type.length()/3;
					}
					for ( iuse = 0; iuse < num_use_types; iuse++ ) {
						use_type_part = use_type.substring ( iuse*3, (iuse*3 + 3) );
						for ( iignore = 0; iignore < IgnoreUseType_size; iignore++ ) {
							if ( Message.isDebugOn ) {
								Message.printDebug ( 2, routine, "Use type part= \""+
								use_type_part+"\""+ " ignore=\"" +
								IgnoreUseType_pattern_Array[iignore] + "\"" );
							}
							if ( (use_type.length() > 0) &&
								use_type_part.equalsIgnoreCase(IgnoreUseType_pattern_Array[iignore]) ){
								message = "Diversion station \"" + id + "\" has right with " +
								"use type \"" + use_type_part + "\" - ignoring...";
								Message.printStatus ( 2, routine, message );
								ignore_right = true;
								break;
							}
						}
						if ( ignore_right ) {
							break;
						}
					}
					if ( ignore_right ) {
						continue;
					}
				}
				if ( !units.equalsIgnoreCase("C") && !units.equalsIgnoreCase("CFS") ) {
					message = "Diversion station \"" + id + "\" has right with decree units \"" + units +
					"\" - only C and CFS is allowed - skipping.";
					//Message.printStatus ( 2, routine, message );
			        Message.printWarning ( warning_level, 
			            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
			        status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.WARNING,
							message, "Verify that the data are correct in HydroBase." ) );
					continue;
				}
				irtem = hbdivr.getAdmin_no();
				if ( is_aggregate && (nAdminNumClasses > 0) ) {
					// Accumulate the data using classes.  This logic follows the old watright logic in
					// concept.

					// Check the decree...
					for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
						if ( __AdminNumClasses_double[ic] >= irtem ) {
							// Found the water right...
							divr = (StateMod_DiversionRight)divr_agg_Vector.get ( ic );
							Message.printStatus ( 2,routine, "For aggregate right \"" + divr.getID() +
							"\", adding " + StringUtil.formatString(irtem,"%.5f") + " decree " + decree);
							divr.setDcrdiv ( divr.getDcrdiv() + decree );
							sum_decree[ic] += decree;
							// Weight the decree based on the appropriation date.
							// First convert the admin number to an appropriation date...
							admin_data = new HydroBase_AdministrationNumber ( irtem );
							appro_date = admin_data.getAppropriationDate();
							// Now use the appropriation date without the prior
							// adjudication date to get a new administration number...
							admin_data = new HydroBase_AdministrationNumber ( appro_date );
							sum_decree_irtem[ic] += decree*admin_data.getAdminNumber();
							++sum_count[ic];
							break;
						}
					}
				}
				else {
					// Anything but an aggregate so add each water right explicitly...
					++divr_count;
					divr = new StateMod_DiversionRight ();
					divr.setID ( id + "." + StringUtil.formatString(divr_count, "%02d") );
					divr.setName ( hbdivr.getWr_name() );
					divr.setCgoto ( id );
					divr.setIrtem (	StringUtil.formatString(irtem, "%.5f"));
					divr.setDcrdiv ( decree );
					divr.setSwitch ( 1 );
					if ( OnOffDefault_year ) {
						// Convert the administration number to a year...
						admin_data = new HydroBase_AdministrationNumber (irtem );
						appro_date = admin_data.getAppropriationDate();
						divr.setSwitch ( admin_data.getAppropriationDate().getYear() );
					}
					else {
						// Use the default value for the administration number...
						divr.setSwitch ( OnOffDefault_int );
					}
					processor.findAndAddSMDiversionRight ( divr, true );
				}
			}
			if ( is_aggregate && (nAdminNumClasses > 0) ) {
				// Post process the rights:
				//
				// 1. If there are no rights in a class, remove the right.
				// 2. Reset the admin number using:
				//
				//	irtem = int(sum_decree_irtem/sum_decree)
				Message.printStatus ( 2, routine, "Finalizing rights for \"" + id + "\"..." );
				for ( ic = 0; ic < nAdminNumClasses; ic++ ) {
					divr = (StateMod_DiversionRight)divr_agg_Vector.get(ic);
					if ( (sum_count[ic] > 0) && (sum_decree[ic] > 0.0) ) {
						// Make sure that irtem is handled as an integer below...
						irtem = sum_decree_irtem[ic]/sum_decree[ic];
						divr.setIrtem (	StringUtil.formatString((int)irtem, "%5d") + ".00000" );
						Message.printStatus ( 2, routine, "Decree-weighted administration number "
						+ "for " + divr.getID() + " class " +
						StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") +
						" is " + divr.getIrtem() + " decree=" + divr.getDcrdiv() );
						// Set the switch...
						if ( OnOffDefault_year ) {
							// Convert the administration number to a year.  The
							// starting value must be an integer...
							admin_data = new HydroBase_AdministrationNumber ((int)irtem );
							appro_date = admin_data.getAppropriationDate();
							divr.setSwitch ( admin_data.getAppropriationDate().getYear() );
						}
						else {
							// Use the default value for the administration number...
							divr.setSwitch ( OnOffDefault_int );
						}
						processor.findAndAddSMDiversionRight ( divr, true );
					}
					else {
						// Else no rights so don't add for the diversion
						Message.printStatus ( 2, routine, "No rights (or zero total) for right " +
						divr.getID() + " class " +
						StringUtil.formatString(__AdminNumClasses_double[ic],"%.5f") + "...not adding." );
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
	String Datastore = parameters.getValue ( "Datastore" );
		
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
	if ( Datastore != null && Datastore.length() > 0){
		if ( b.length() > 0) {
			b.append( "," );
		}
		b.append ( "Datastore=" + Datastore );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
