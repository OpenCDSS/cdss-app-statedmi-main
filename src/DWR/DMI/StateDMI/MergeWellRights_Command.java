// MergeWellRights_Command - This class initializes, checks, and runs the MergeWellRights() command.

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Right;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_WellRight;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the MergeWellRights() command.
*/
public class MergeWellRights_Command extends AbstractCommand implements Command, FileGenerator
{
	
/**
Values for boolean parameters.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
List of output files that are created by this command.
*/
private List<File> __OutputFile_List = null;

/**
Constructor.
*/
public MergeWellRights_Command ()
{	super();
	setCommandName ( "MergeWellRights" );
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
{	String routine = "MergeWellRights.checkCommandParameters";
	String OutputFile = parameters.getValue ( "OutputFile" );
	String IDFormat = parameters.getValue ( "IDFormat" );
	String MergeParcelYears = parameters.getValue ( "MergeParcelYears" );
	String SumDecrees = parameters.getValue ( "SumDecrees" );
	String message;
	String warning = "";
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (OutputFile != null) && (OutputFile.length() != 0) ) {
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
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Software error - report to support." ) );
		}
	
		try {
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Create the output directory." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The output file:\n" +
			"    \"" + OutputFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}
	
	if ( (IDFormat != null) && (IDFormat.length() > 0) &&
		!IDFormat.equalsIgnoreCase(""+StateModWellRightIdFormatType.RIGHTID_NN) ) {
		message = "The IDFormat value (" + IDFormat + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IDFormat as " + StateModWellRightIdFormatType.RIGHTID_NN +
				" or blank (default=no formatting).") );
	}

	if ( (MergeParcelYears != null) && (MergeParcelYears.length() > 0) && 
		!MergeParcelYears.equalsIgnoreCase(_False) && !MergeParcelYears.equalsIgnoreCase(_True) ) {
		message = "The MergeParcelYears value (" + MergeParcelYears + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify MergeParcelYears as " + _False + " (default) or " + _True + ".") );
	}

	if ( (SumDecrees != null) && (SumDecrees.length() > 0) && 
		!SumDecrees.equalsIgnoreCase(_False) && !SumDecrees.equalsIgnoreCase(_True) ) {
		message = "The SumDecrees value (" + SumDecrees + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify SumDecrees as " + _False + " (default) or " + _True + ".") );
	}
	
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(6);
	validList.add ( "OutputFile" );
	validList.add ( "MergeParcelYears" );
	validList.add ( "SumDecrees" );
    validList.add ( "PermitIDPreFormat" );
	validList.add ( "IDFormat" );
    validList.add ( "PermitIDPostFormat" );

	warning = StateDMICommandProcessorUtil.validateParameterNames ( validList, this, warning );

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
	return (new MergeWellRights_JDialog ( parent, this )).ok();
}

/**
Extract explicit rights from the list of well rights.  These have a parcel year of -999 (less than zero).
This will remove the explicit rights from the original list
*/
private List<StateMod_WellRight> extractExplicitRights ( List<StateMod_WellRight> smrightsOrig )
{	List<StateMod_WellRight> extractedRights = new Vector<StateMod_WellRight>();
	int size = smrightsOrig.size();
	StateMod_WellRight wer = null;
	for ( int i = 0; i < size; i++ ) {
		wer = smrightsOrig.get(i);
		if ( wer.getParcelYear() < 0 ) {
			extractedRights.add ( wer );
			// Also remove from the original list
			smrightsOrig.remove(i);
			i--;
			size--;
		}
	}
	return extractedRights;
}

/**
 * Format the well right IDs
 */
private void formatWellRightIds ( List<StateMod_WellRight> smrights, String permitIDPreFormat,
	StateModWellRightIdFormatType stateModWellRightIdFormatType, String permitIDPostFormat ) {
	StateMod_WellRight smWellRight;
	String id;
	// Loop through each right and, if originating from a well permit, format as requested
	if ( (permitIDPreFormat != null) && !permitIDPreFormat.isEmpty() ) {
		for ( StateMod_WellRight smright : smrights ) {
			smWellRight = smright;
			id = smWellRight.getID();
			if ( smWellRight.getCollectionPartIdType().equalsIgnoreCase("Receipt")) {
				smWellRight.setID(String.format(permitIDPreFormat, id));
			}
		}
	}
	if ( stateModWellRightIdFormatType == StateModWellRightIdFormatType.RIGHTID_NN ) {
		// Loop through each right and add .01, .02, etc. on all unique well rights.
		// This is needed because well rights are often listed more than once with structures due to
		// wells serving multiple parcels, etc.
		// First create a hashmap of all the unique right IDs before formatting and set the key to an integer 0,
		// meaning no match found.
		HashMap<String,Integer> idmap = new HashMap<String,Integer>();
		for ( StateMod_Right smright : smrights ) {
			if ( idmap.get(smright.getIdentifier()) == null ) {
				// Not found so add to the list
				idmap.put(smright.getIdentifier(), new Integer(0) );
			}
		}
		// Now loop through again and assign the identifier
		Integer idCount;
		for ( StateMod_WellRight smright : smrights ) {
			smWellRight = smright;
			id = smWellRight.getID();
			idCount = idmap.get(id);
			// Initialized to 0, so next will be .01
			idCount = idCount + 1;
			smWellRight.setID(String.format("%s.%02d", id, idCount));
			// Update the count to increase the counter
			idmap.put(id, new Integer(idCount) );
		}
	}
	// Loop through each right and, if originating from a well permit, format as requested
	if ( (permitIDPostFormat != null) && !permitIDPostFormat.isEmpty() ) {
		for ( StateMod_WellRight smright : smrights ) {
			smWellRight = smright;
			id = smWellRight.getID();
			if ( smWellRight.getCollectionPartIdType().equalsIgnoreCase("Receipt")) {
				smWellRight.setID(String.format(permitIDPostFormat, id));
			}
		}
	}
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
	List<File> outputFileList = getOutputFileList();
	if ( outputFileList == null ) {
		return new Vector<File>();
	}
	else {
		return outputFileList;
	}
}

/**
Return the list of output files generated by this method.  This method is used internally.
*/
protected List<File> getOutputFileList ()
{
	return __OutputFile_List;
}

/**
Merge rights from multiple parcel years, as follows.  First, any rights that have parcel year < 0 (missing)
are extracted and not merged - these correspond with explicit wells.  Second, loop through the
years with parcel data, comparing two at a time.  For example, if rights are
available from parcels in 1998 and 2002, for each location, get the list of
rights from the 1998 and the list of rights from the 2002 data.  If the following is true:
<ol>
<li>	A well right in 1998 and 2002 has the same well ID.</li>
<li>	and has the same well ID (location)</li>
<li>	and has the same well type (class)</li>
<li>	and has the same administration number</li>
</ol>
they are a match and should be included for the location, where the decree is
the maximum of the two years.  Any that don't match are totaled and the year with the largest sum of
rights is included.  Third, if summing rights, add the rights that have the same identifier, and
administration number.
@param smrights_orig list of StateMod_WellRight, the original list with multiple
parcel years, before merging.
@return the merged list of StateMod_WaterRight.
*/
private List<StateMod_WellRight> mergeRightsFromMultipleParcelYears (
	List<StateMod_WellRight> smrights_orig, String OutputFile_full, List<String> OutputComments_List )
throws Exception
{	String routine = getClass().getSimpleName() + ".mergeRightsFromMultipleParcelYears";

	// Determine the unique years of parcel data in the rights.
	// If there is only one year of data then there is no reason to merge the rights.
	
	int [] parcel_years = StateMod_Util.getWaterRightParcelYearList ( smrights_orig );
	if ( parcel_years.length < 2 ) {
		Message.printStatus ( 2, routine, "Have " + parcel_years.length +
			" years of parcel data.  No need to merge - using the original rights." );
		return smrights_orig;
	}

	// Get the rights that have parcel year < 0 - these will not be processed during the merge
	// The explicit rights are removed from the list and are added at the end
	List<StateMod_WellRight> explicitRights = extractExplicitRights ( smrights_orig );
	Message.printStatus(2, routine, "Extracted " + explicitRights.size() +
		" explicit rights with missing parcel year that will NOT be merged across years.");
	
	// Determine the unique list of well locations for the rights...
	
	List<String> smwells = StateMod_Util.getWaterRightLocationList ( smrights_orig, -1 );
	
	// Loop through the structures...
	int smwells_size = 0;
	if ( smwells != null ) {
		smwells_size = smwells.size();
	}
	String id;
	List<StateMod_WellRight> smrights = new Vector<StateMod_WellRight>();	// Rights returned after the merge
	List<StateMod_WellRight> rights_loc_year1;	// All rights at a location for year 1
	List<StateMod_WellRight> rights_loc_year2;	// All rights at a location for year 2
	List<StateMod_WellRight> rights_year1;	// Rights from first parcel year (or previous merge), loc and right ID
	List<StateMod_WellRight> rights_year2;	// Rights from second parcel year.
	StateMod_WellRight right1, right2;	// Rights to compare.
	String year1_string = "";	// For messages.
	String year2_string = "";	// For messages
	List<StateMod_WellRight> smrights_prev = new Vector<StateMod_WellRight>();
	
	List<File> outputFileList = null;
	if ( OutputFile_full != null ) {
		outputFileList = new Vector<File>();
	}
	for ( int iyear = 0; iyear < (parcel_years.length - 1); iyear++ ) {
		// Loop through the parcel years, comparing the first with the second,
		// the results with the third, etc.
		if ( OutputFile_full != null ) {
			// Write out the initial year of data as the starting point
			File f = new File(OutputFile_full);
			String outputFileYear = f.getParent() + File.separator +
				"wer-merged-" + parcel_years[iyear] + "-" + f.getName();
			PropList writeProps = new PropList("");
	       	writeProps.set ( "WriteDataComments", "True" );
	       	// Use null for the ID to get all locations.
	       	List<StateMod_WellRight> year1Rights = StateMod_Util.getWaterRightsForLocation ( smrights_orig, null, parcel_years[iyear] );
			StateMod_WellRight.writeStateModFile(null, outputFileYear,
				year1Rights, OutputComments_List, writeProps );
			outputFileList.add(new File(outputFileYear));
		}
		// Create a new results list because processing more than 2 years requires multiple
		// transfers of data...
		smrights = new Vector<StateMod_WellRight>();
		// Loop through each location
		for ( int iwell = 0; iwell < smwells_size; iwell++ ) {
			id = smwells.get(iwell);
			Message.printStatus ( 2, routine, "Merging " + id + " well rights for parcel years " +
				parcel_years[iyear] + parcel_years[iyear + 1] );
			// Get the water rights for the early year to compare (or the results of the previous merge iteration)...
			if ( iyear == 0 ) {
				// Get the rights for the first parcel year being considered...
				rights_loc_year1 = StateMod_Util.getWaterRightsForLocation ( smrights_orig, id, parcel_years[iyear] );
				year1_string = "year " + parcel_years[iyear];
			}
			else {
				// Get the rights from the previous merge...
				rights_loc_year1 = StateMod_Util.getWaterRightsForLocation (
					smrights_prev, // New merged rights
					id, // location ID of interest
					-1 ); // Don't specify a year since it may vary in merged results.
				year1_string = "previous merge";
			}
			year2_string = "" + parcel_years[iyear + 1];
			// Get the water rights for the next year to compare...
			rights_loc_year2 = StateMod_Util.getWaterRightsForLocation (
				smrights_orig, // Get from original data
				id, // location ID of interest
				parcel_years[iyear + 1] ); // next year to compare
			// Initialize the "dirty" flag on all the rights.  Set this to false initially
			// and then true below if the right is matched.  This is used to know if after
			// processing each location/right ID combination there still needs to be some rights added.
			int rights_loc_year1_size = rights_loc_year1.size();
			for ( int iright1 = 0; iright1 < rights_loc_year1_size; iright1++ ) {
				right1 = (StateMod_WellRight)rights_loc_year1.get(iright1);
				right1.setDirty(false);
			}
			int rights_loc_year2_size = rights_loc_year2.size();
			for ( int iright2 = 0; iright2 < rights_loc_year2_size; iright2++ ) {
				right2 = (StateMod_WellRight)rights_loc_year2.get(iright2);
				right2.setDirty(false);
			}
			Message.printStatus ( 2, routine,
				"Location \"" + id + "\" has " + rights_loc_year1_size + " rights in " +
				year1_string + " to check for matches against " + rights_loc_year2_size +
				" rights in " + year2_string + ".  Will check by loc ID/right ID blocks.");
			// Now get data for well right IDs for the location.  This is a smaller
			// block of data to check.  No need to pass in location ID or year since
			// that was filtered on the first lookup.  Focus on getting the locations from
			// the first year.  Anything not matched in year 2 will be handled below.
			List<String> rightids_year1 = StateMod_Util.getWaterRightIdentifiersForLocation(
				rights_loc_year1, null, -1 );
			int rightids_year1_size = 0;
			if ( rightids_year1 != null ) {
				rightids_year1_size = rightids_year1.size();
			}
			Message.printStatus ( 2, routine,
				"Location \"" + id + "\" has " + rightids_year1_size + " right identifiers in " +
				year1_string + " to check for matches.");
			// Loop through the IDs and merge the location/right ID combination.
			// At the end merge any rights that were not matched.
			for ( int iright1_ids = 0; iright1_ids < rightids_year1_size; iright1_ids++ ) {
				// Now get the actual rights for year 1 and 2 for the location
				// and right ID (again, using the year 1 ID list)...
				rights_year1 = StateMod_Util.getWaterRightsForLocationAndRightIdentifier(
					rights_loc_year1, null, rightids_year1.get(iright1_ids), -1 );
				rights_year2 = StateMod_Util.getWaterRightsForLocationAndRightIdentifier(
					rights_loc_year2, null, rightids_year1.get(iright1_ids), -1 );
				Message.printStatus ( 2, routine, "Merging rights for loc \"" +
					id + "\" right id=\"" + rightids_year1.get(iright1_ids) +
					"\" from " + year1_string +
					" (" + rights_year1.size() + " rights) and " + year2_string +
					" (" + rights_year2.size() + " rights)." );
				// Initialize data used to compare rights for a location in two
				// different years.  The decree sums are included here because the comparison
				// loop below may omit some year 2 rights.
				double rights_year1_decreesum = 0.0;
				double rights_year2_decreesum = 0.0;
				int rights_year1_size = rights_year1.size();
				boolean [] rights_year1_matched = new boolean[rights_year1_size];
				for ( int iright1 = 0; iright1 < rights_year1_size; iright1++ ) {
					right1 = rights_year1.get(iright1);
					rights_year1_matched[iright1] = false;
					double right1_decree = right1.getDecree();
					if ( right1_decree > 0.0 ) {
						rights_year1_decreesum += right1_decree;
					}
				}
				int rights_year2_size = rights_year2.size();
				boolean [] rights_year2_matched = new boolean[rights_year2_size];
				for ( int iright2 = 0; iright2 < rights_year2_size; iright2++ ) {
					right2 = rights_year2.get(iright2);
					rights_year2_matched[iright2] = false;
					double right2_decree = right2.getDecree();
					if ( right2_decree > 0.0 ) {
						rights_year2_decreesum += right2_decree;
					}
				}
				// Loop through the rights in the first year and see if there is a match
				// in the second year...
				for ( int iright1 = 0; iright1 < rights_year1_size; iright1++ ) {
					right1 = rights_year1.get(iright1);
					String right1_adminnum_String = StringUtil.formatString(right1.getAdministrationNumber(),"%11.5s");
					// Search for right in second list...
					for ( int iright2 = 0; iright2 < rights_year2_size; iright2++ ) {
						right2 = rights_year2.get(iright2);
						String right2_adminnum_String = StringUtil.formatString(right2.getAdministrationNumber(),"%11.5s");
						if ( rights_year2_matched[iright2] ) {
							// Right was already matched so skip in comparison (this may not be valid until
							// decree is included in the comparison).
							continue;
						}
						// See note below about including decree in comparison.
						if ( right1.getIdentifier().equalsIgnoreCase(right2.getIdentifier()) &&
							right1.getLocationIdentifier().equalsIgnoreCase(right2.getLocationIdentifier()) &&
							(right1.getClass() == right2.getClass()) &&
							right1_adminnum_String.equals(right2_adminnum_String) ) { 
							// Also include decree to get an exact match? ) {
							// The rights match
							rights_year1_matched[iright1] = true;
							rights_year2_matched[iright2] = true;
							break;	// Only one match allowed in year 2 per year 1 right
						}
					}
				}
				// Determine whether all rights were matched.
				// Put the extra loops here because the above may get more complicated.
				boolean rights_year1_allmatched = true;
				boolean rights_year2_allmatched = true;
				for ( int iright1 = 0; iright1 < rights_year1_size; iright1++ ) {
					if ( !rights_year1_matched[iright1] ) {
						rights_year1_allmatched = false;
						break;
					}
				}
				for ( int iright2 = 0; iright2 < rights_year2_size; iright2++ ) {
					if ( !rights_year2_matched[iright2] ) {
						rights_year2_allmatched = false;
						break;
					}
				}
				// If all rights were matched, include those from the first year
				// (the first parcel year will show up in the extended data on the right
				// side of the rights file).
				if ( rights_year1_allmatched && rights_year2_allmatched ) {
					Message.printStatus ( 2, routine, "Rights are the same in both years.  Using rights from "
						+ year1_string );
					for ( int iright1 = 0; iright1 < rights_year1_size; iright1++ ) {
						right1 = (StateMod_WellRight)rights_year1.get(iright1);
						smrights.add ( right1 );
						right1.setDirty(true);	// For global check
					}
				}
				else {
					// Else if not all rights were matched.  Check the sum of the
					// right decrees and include the year that has the highest decree total.
					// TODO SAM 2007-05-30 Evaluate whether more sophistication is needed.
					if ( rights_year1_decreesum >= rights_year2_decreesum ) {
						// Add the first year's rights...
						if ( iyear == 0 ) {
							Message.printStatus ( 2, routine,
							"Rights are different.  Decree sum (" +
							StringUtil.formatString(rights_year1_decreesum,"%.2f") +
							") is equal or larger in " + year1_string + " than in " +
							year2_string + " (" +
							StringUtil.formatString(rights_year2_decreesum,"%.2f") +
							").  Using rights from " + year1_string );
						}
						for ( int iright1 = 0; iright1 < rights_year1_size; iright1++ ) {
							right1 = (StateMod_WellRight)rights_year1.get(iright1);
							smrights.add ( right1 );
							right1.setDirty(true);	// For global check
						}
					}
					else {
						// Add the second year's rights...
						Message.printStatus ( 2, routine,
						"Rights are different.  Decree sum (" +
						StringUtil.formatString(rights_year2_decreesum,"%.2f") +
						") is greater in " + parcel_years[iyear + 1] + " than in " +
						year1_string + " (" +
						StringUtil.formatString(rights_year2_decreesum,"%.2f") +
						").  Using rights from " + year2_string );
						for ( int iright2 = 0; iright2 < rights_year2_size; iright2++ ) {
							right2 = (StateMod_WellRight)rights_year2.get(iright2);
							smrights.add ( right2 );
							right2.setDirty(true);	// For global check
						}
					}
				}
			} // End location/id loop
			// Now if any rights in the two years were not matched, add them at the end...
			/* This should not happen since year 1 is the basis for checks and all
			right identifiers should be checked for the location.
			for ( int iright1 = 0; iright1 < rights_loc_year1_size; iright1++ ) {
				right1 = (StateMod_WellRight)rights_loc_year1.elementAt(iright1);
				if ( right1.isDirty() ) {
					Message.printStatus ( 2, routine, "Right from " + year1_string +
							" was not matched.  Adding " + right1.getIdentifier() + " " +
							right1.getAdministrationNumber() );
					smrights.addElement ( right1 );
				}
			}
			*/
			// Add year 2 rights that were not in the list for the location in year 1
			// (new rights not matched with any loc ID/right ID combination).
			for ( int iright2 = 0; iright2 < rights_loc_year2_size; iright2++ ) {
				right2 = rights_loc_year2.get(iright2);
				if ( !right2.isDirty() ) {
					// Was not included
					// Check the right identifier...
					String right2_id = right2.getIdentifier();
					boolean id_found = false;
					for ( int iid = 0; iid < rightids_year1_size; iid++ ) {
						if ( right2_id.equalsIgnoreCase((String)rightids_year1.get(iid)) ) {
							id_found = true;
							// ID matches one considered for year 1 for the location
							// so was not included because year 2 data were included instead.
							break;
						}
					}
					if ( !id_found ) {
						Message.printStatus ( 2, routine, "Right from location \"" + id + "\"" +
							year2_string + " was not matched (right ID not in " + year1_string +
							").  Adding " + right2.getIdentifier() + " " +
							right2.getAdministrationNumber() );
						smrights.add ( right2 );
					}
				}
			}
		} // End location loop
		// Save the results from this parcel year because a restart is needed for the next year...
		smrights_prev = smrights;
		// Save the intermediate result if requested
		if ( OutputFile_full != null ) {
			File f = new File(OutputFile_full);
			String outputFileYear = f.getParent() + File.separator +
				"wer-merged-" + parcel_years[iyear + 1] + "-" + f.getName();
			PropList writeProps = new PropList("");
	       	writeProps.set ( "WriteDataComments", "True" );
			StateMod_WellRight.writeStateModFile(null, outputFileYear,
				smrights, OutputComments_List, writeProps );
			outputFileList.add(new File(outputFileYear));
		}
	} // End parcel year loop
	// TODO SAM 2009-03-26 Evaluate whether these should be inserted after the structure (not just in a
	// block at the end of the list)
	// Add the rights that were manually set that do not have a parcel year.
	// These should have a -999 for the parcel year in the file.
	smrights.addAll( explicitRights );
	// Save the list of generated files.
	if ( outputFileList != null ) {
		setOutputFileList ( outputFileList );
	}
	return smrights;
}

// Use the base class parseCommand()

/**
Method to execute the readDiversionHistoricalTSMonthlyFromHydroBase() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	// Use to allow called methods to increment warning count.
	int warningLevel = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String OutputFile = parameters.getValue ( "OutputFile" );
	String PermitIDPreFormat = parameters.getValue ( "PermitIDPreFormat" );
	if ( (PermitIDPreFormat == null) || PermitIDPreFormat.isEmpty() ) {
		PermitIDPreFormat = "%s";
	}
	String IDFormat = parameters.getValue ( "IDFormat" );
	String PermitIDPostFormat = parameters.getValue ( "PermitIDPostFormat" );
	if ( (PermitIDPostFormat == null) || PermitIDPostFormat.isEmpty() ) {
		PermitIDPostFormat = "%s";
	}
	StateModWellRightIdFormatType stateModWellRightIdFormatType = null;
	if ( (IDFormat != null) && !IDFormat.isEmpty() ) {
		stateModWellRightIdFormatType = StateModWellRightIdFormatType.valueOfIgnoreCase(IDFormat);
	}
	String MergeParcelYears = parameters.getValue ( "MergeParcelYears" );
	boolean MergeParcelYears_boolean = true; // Default
	if ( (MergeParcelYears != null) && MergeParcelYears.equalsIgnoreCase(_False) ) {
		MergeParcelYears_boolean = false;
	}
	String SumDecrees = parameters.getValue ( "SumDecrees" );
	boolean SumDecrees_boolean = true; // Default
	if ( (SumDecrees != null) && SumDecrees.equalsIgnoreCase(_False) ) {
		SumDecrees_boolean = false;
	}

	// Get the list of well rights (probably empty)...
	
	List<StateMod_WellRight> rightList = null;
	int rightsListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateMod_WellRight> dataList = (List<StateMod_WellRight>)processor.getPropContents ( "StateMod_WellRight_List");
		rightList = dataList;
		rightsListSize = rightList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting well right data from processor.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	if ( rightsListSize == 0 ) {
		message = "No wells are available to merge.";
		Message.printWarning(warningLevel,
			MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Make sure well rights were read/processed with previous command(s)." ) );
	}
	
	try {
        List<String> OutputComments_List = null;
        try {
        	Object o = processor.getPropContents ( "OutputComments" );
            // Comments are available so use them...
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<String> outputCommentsList = (List<String>)o;
                OutputComments_List = outputCommentsList;
            }
        }
        catch ( Exception e ) {
            // Not fatal, but of use to developers.
            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
            Message.printWarning(3, routine, message );
            Message.printWarning(3, routine, e );
        }
		// Merge the data from multiple years...
    	setOutputFileList ( null );
    	String OutputFile_full = null;
    	if ( OutputFile != null ) {
    		OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),OutputFile) );
    	}
    	List<StateMod_WellRight> smrights = rightList;
    	if ( MergeParcelYears_boolean ) {
    		Message.printStatus( 2, routine, "Before merge (all years) has " + rightsListSize + " rights.");
    		smrights = mergeRightsFromMultipleParcelYears ( rightList, OutputFile_full, OutputComments_List );
    		Message.printStatus( 2, routine, "After merge (merged years) has " + smrights.size() + " rights.");
    	}
		
		// Optionally, also sum decrees if ID and admin number match.
		
		if ( SumDecrees_boolean ) {
			smrights = sumDecrees ( smrights );
		}
		
		// Format the well right identifiers
		formatWellRightIds ( smrights, PermitIDPreFormat, stateModWellRightIdFormatType, PermitIDPostFormat );
		
		// Set the results back in the processor because it is a new list.
		
		try {
			processor.setPropContents ( "StateMod_WellRight_List", smrights );
		}
		catch ( Exception e ) {
			message =  "Unable to set well rights for processor.  Results of processing will not be evident.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
			status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Report problem to software support." ) );
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error merging well rights (" + e + ").";
        Message.printWarning ( warningLevel, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFileList ( List<File> outputFileList )
{
	__OutputFile_List = outputFileList;
}

/**
Sum rights in the list that have the same ID and administration number.  This serves to remove rights that
for whatever purpose have multiple entries.  Rights with well match class 4 and 9 are always passed through
without changing because they are estimated wells and need to remain as unique representations.
@param smrights list of rights to be processed
@return a new list with degrees summed.
*/
private List<StateMod_WellRight> sumDecrees ( List<StateMod_WellRight> smrights )
{	String routine = getClass().getSimpleName() + ".sumDecrees";
	List<StateMod_WellRight> smAllRightsSummed = new Vector<StateMod_WellRight>();
	Message.printStatus(2,routine,"Number of rights before summing decrees = " + smrights.size());
	// Get the list of well structures
	List<String> smwellIDs = StateMod_Util.getWaterRightLocationList(smrights, -1);
	int smwellIDsSize = smwellIDs.size();
	Message.printStatus(2, routine, "Processing " + smwellIDsSize + " unique well locations.");
	String wellID; // Well ID
	StateMod_WellRight wer = null;
	StateMod_Right wer2 = null;
	for ( int iwell = 0; iwell < smwellIDsSize; iwell++ ) {
		// Get the well rights for the location...
		wellID = smwellIDs.get(iwell);
		List<String> smwellRightIDs = StateMod_Util.getWaterRightIdentifiersForLocation(smrights, wellID, -1 );
		// Get the water rights for the identifiers
		int smwellRightIDsSize = smwellRightIDs.size();
		Message.printStatus(2, routine, "Processing well ID \"" + wellID + "\"... checking " + smwellRightIDsSize +
			" rights to sum decrees.");
		for ( int irightID = 0; irightID < smwellRightIDsSize; irightID++ ) {
			String rightID = smwellRightIDs.get(irightID);
			List<StateMod_WellRight> smwellRights =
				StateMod_Util.getWaterRightsForLocationAndRightIdentifier(smrights, wellID, rightID, -1);
			// Merge the rights where admin number and on/off are the same.  Whatever is left gets added to
			// the main list and returned.  Check the current index each time because rights may be removed.
			Message.printStatus(2, routine, "Processing well ID \"" + wellID + "\" right ID \"" +
				rightID + "\" - starting with " + smwellRights.size() + " rights to check.");
			List<StateMod_WellRight> smwellRightsSummed = new Vector<StateMod_WellRight>();
			for ( int iright = 0; iright < smwellRights.size(); iright++ ) {
				// Get the next right
				wer = smwellRights.get(iright);
				// TODO SAM 2009-04-29 Evaluate how missing data might impact things.  There really should
				// not be missing decrees and if on/off or admin number are missing, it should pass through
				// OK.
				// Loop through all rights that have already been added.  If the admin number and on/off
				// are the same, add the decree to the found right.  If the admin number and on/off are
				// different, add right to the list.
				if ( wer.isEstimatedWell() ) {
					// Estimated well rights always are added to the final list and then remove from the
					// small list so that they don't get reprocessed
					smAllRightsSummed.add ( wer );
					smwellRightsSummed.remove( wer);
					Message.printStatus(2, routine, "Processing well ID \"" + wellID + "\" right ID \"" +
						rightID + "\" - passing through estimated well right." );
				}
				else {
					// Loop through existing rights that have been processed and see if they need to be
					// added to...
					boolean found = false;
					for ( int iright2 = 0; iright2 < smwellRightsSummed.size(); iright2++ ) {
						wer2 = smwellRightsSummed.get(iright2);
						if ( wer.getAdministrationNumber().equals(wer2.getAdministrationNumber()) &&
							(wer.getSwitch() == wer2.getSwitch()) ) {
							found = true;
							break;
						}
					}
					if ( found ) {
						// Had a match so add to the matched right
						Message.printStatus(2, routine, "Processing well ID \"" + wellID + "\" right ID \"" +
							rightID + "\" - adding to adminNum " + wer.getAdministrationNumber() +
							", decree " + wer.getDecree() + " to matching right." );
						wer2.setDecree ( wer2.getDecree() + wer.getDecree() );
					}
					else {
						// Did not have a match so add 
						Message.printStatus(2, routine, "Processing well ID \"" + wellID + "\" right ID \"" +
							rightID + "\" - adding right for adminNum " + wer.getAdministrationNumber() +
							", decree " + wer.getDecree() + " since no matching right." );
						smwellRightsSummed.add(wer);
					}
				}
			}
			Message.printStatus(2, routine, "Adding " + smwellRightsSummed.size() + " rights to final list." );
			smAllRightsSummed.addAll ( smwellRightsSummed );
		}
	}
	Message.printStatus(2,routine,"Number of rights after summing decrees = " + smAllRightsSummed.size());
	return smAllRightsSummed;
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
	
	String OutputFile = parameters.getValue ( "OutputFile" );
	String MergeParcelYears = parameters.getValue ( "MergeParcelYears" );
	String SumDecrees = parameters.getValue ( "SumDecrees" );
	String PermitIDPreFormat = parameters.getValue ( "PermitIDPreFormat" );
	String IDFormat = parameters.getValue ( "IDFormat" );
	String PermitIDPostFormat = parameters.getValue ( "PermitIDPostFormat" );

	StringBuffer b = new StringBuffer ();
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (MergeParcelYears != null) && (MergeParcelYears.length() > 0) ) {
		b.append ( "MergeParcelYears=" + MergeParcelYears );
	}
	if ( (SumDecrees != null) && (SumDecrees.length() > 0) ) {
		b.append ( "SumDecrees=" + SumDecrees );
	}
	if ( PermitIDPreFormat != null && PermitIDPreFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitIDPreFormat=\"" + PermitIDPreFormat + "\"" );
	}
	if ( IDFormat != null && IDFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDFormat=\"" + IDFormat + "\"" );
	}
	if ( PermitIDPostFormat != null && PermitIDPostFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitIDPostFormat=\"" + PermitIDPostFormat + "\"" );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
