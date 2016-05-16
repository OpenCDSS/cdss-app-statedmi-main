//-----------------------------------------------------------------------------
// LegacyReturnFlow - read and hold data from the legacy "*.rtn" file,
//			originally used with watright and supported in StateDMI
// REVISIT SAM 2004-06-15 - need to replace with list?
//-----------------------------------------------------------------------------
// History:
//
// 19 Aug 1999	Steven A. Malers, RTi	Rename from rtninfo and make class more
//					functional.  Move read code from Utils
//					class.
// 23 Feb 2000	SAM, RTi		Add ability to look for return flow
//					even if the network is generated from
//					a list.  For example, the network may
//					be read but a list of wells specified
//					independently.  If not enabled, then the
//					return flow file is not read!
//					Overload constructor to take flag
//					indicating whether used for depletions.
// 15 Feb 2001	SAM, RTi		Update to not use SMRiverInfo
//					additional strings since this data is
//					gone.  Add checkAgainstNetwork().
// 2004-06-15	SAM, RTi		* Rename watright ReturnFlow to
//					  this LegacyReturnFlow class.
//					* Strip out all but the essential data
//					  members and the method to read the
//					  file since StateDMI only uses this
//					  class temporarily while data are read.
//					* Encapsulate the data and add get
//					  methods.
// 2005-06-01	SAM, RTi		* Print exceptions to log file at level
//					  3.
// 2006-04-03	SAM, RTi		* If end of file is reached
//					  unexpectedly, print the return flow
//					  ID in the message.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Data;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class reads and stores information from the return flow (*.rtn) file.
Data are public to simplify manipulation.
*/
public class LegacyReturnFlow extends StateMod_Data
{

/**
Identifiers for structures being returned to.
*/
private String[] __return_id = null;

/**
Delay table number to use for structure (default for all structures being
returned to).
*/
private String __default_table = "1";

/**
Default efficiency to use for the structure.
*/
private double __eff = 0.0;

/**
Return flow percentages for the structures being returned to (0 to 100).
*/
private double[] __return_percent = null;

/**
Return flow table for each structure (defaults to default_table).
*/
private String[] __return_table = null;

/**
Constructor.
@param id Identifier for structure of interest.
@param n Number of return flow points.
@param eff Default efficiency for structure.
@param default_table Default delay table number for structure.
*/
public LegacyReturnFlow ( String id, int n, double eff, String default_table )
{
	initialize ();
	__default_table = default_table;
	__eff = eff;
	setID ( id );
	__return_id = new String[n];
	__return_percent = new double[n];
	__return_table = new String[n];
	for ( int i = 0; i < n; i++ ) {
		__return_percent[i] = 0.0;
		__return_table[i] = default_table;
	}
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	__return_id = null;
	__return_percent = null;
	__return_table = null;
	super.finalize();
}

/**
Return the default return table ID for a location.
@return the default return table ID for a location.
*/
public String getDefaultReturnTable ()
{	return __default_table;
}

/**
Return the default efficiency for a location.
@return the default efficiency for a location.
*/
public double getEfficiency ()
{	return __eff;
}

/**
Return the number of returns/depletions.
@return the number of returns/depletions.
*/
public int getNumReturns ()
{	if ( __return_id == null ) {
		return 0;
	}
	else {	return __return_id.length;
	}
}

/**
Return the node identifier for a return/depletion.
@return the node identifier for a return/depletion.
*/
public String getReturnID ( int pos )
{	return __return_id[pos];
}

/**
Return the percent for a return/depletion.
@return the percent for a return/depletion.
*/
public double getReturnPercent ( int pos )
{	return __return_percent[pos];
}

/**
Return the return table ID for a return/depletion.
@return the return table ID for a return/depletion.
*/
public String getReturnTable ( int pos )
{	return __return_table[pos];
}

/**
Initialize data.  Default to diversion values (not wells).
*/
private void initialize ()
{	__default_table = "1";
	__eff = 0.0;
	__return_id = null;
	__return_percent = null;
	__return_table = null;
}

/**
Read a legacy return flow file, as originally used with the watright program.
@return a Vector of LegacyReturnFlow.
@param rtnfile Name of the return flow/depletion file.
@exception IOException if there is an error opening/reading the file.
*/
public static List readReturnFile ( String rtnfile )
throws IOException
{	BufferedReader	ifp = null;
	int		i, linenum = 0, num;
	String		id = "", line, message = "",
			routine = "LegacyReturnFlow.readReturnFile",
			table;
	double		eff = 0.0;
	LegacyReturnFlow	rinfo;
	List		list, rinfo_vec = new Vector();
	boolean		warning_known = false;	// Whether the code is handling
						// a known condition (false
						// means that a generic message
						// should be printed).

	try {	// Main try
	if ( rtnfile == null ) {
		message = "File name is null";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	ifp = new BufferedReader( new FileReader(
		IOUtil.getPathUsingWorkingDir(rtnfile) ) );

	while( true ){
		// Let an exception be thrown and caught in the main loop...
		if( (line = ifp.readLine()) == null ) {
			// End of file...
			break;
		}

		++linenum;
		line = line.trim();
		if( line.length() == 0 || (line.charAt(0) == '#') ||
			(line.charAt(0) == '\0') ||
			(line.charAt(0) == '\n') ) {
			// Comment or blank line...
			continue;
		}
		// Should be at first line that is not a comment or a tab'ed
		// over line from a zero value below.  Get the
		// following information:
		//	id	- id of diversion.
		//	n	- number of return flow nodes (also number of
		//		  informative lines following this one).
		//	eff	- default efficiency for structure.
		//	table	- default delay table for structure.
		list = StringUtil.breakStringList( line, " \t", 
			StringUtil.DELIM_SKIP_BLANKS );

		if ( list.size() < 4 ) {
			// Did not read in the correct number of arguments for
			// the line...
			message = list.size() +
			" return/depletion pattern values (should be >= 4) on "+
			"data line: \""+ line + "\".  Unable to continue";
			Message.printWarning ( 2, routine, message );
			warning_known = true;
			throw new IOException ( message );
		}
		id 	= (String)list.get(0);
		num	= StringUtil.atoi((String)list.get(1));
		eff	= StringUtil.atod((String)list.get(2));
		table	= (String)list.get(3);
		// Put a check in to see if the line has a % character.  For
		// now assume that no % is in the identifier...
		if ( line.indexOf( '%' ) != -1 ) {
			message = "Structure \"" + id + 
			"\" in the return/depletion file uses %.  " +
			"Please just use numbers.";
			Message.printWarning ( 2, routine, message );
			warning_known = true;
			throw new IOException ( message );
		}
		if ( num == 0 ) {
			// There are not supposed to be any return flow nodes
			// for this one although there may be some informative
			// lines -> if there is nothing in line[0] eat 
			// the line, return to the top of the loop
			Message.printWarning ( 2, routine, "\"" + id +
			"\" is in return/depletion file but has 0 return " +
			"nodes!" );
			continue;
		}

		// Allocate the new return information data repository and
		// construct with main data values...

		rinfo = new LegacyReturnFlow ( id, num, eff, table );

		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine,
			"\"" + rinfo.getID() + "\" has " +
			rinfo.getNumReturns() + 
			" return/depletion node(s), " + rinfo.__eff + 
			" % efficiency, and default table " +
			rinfo.__default_table
			+ "." );
		}

		// Now read each return flow structure line...
	
		for ( i = 0; i < num; i++ ) {
			// Each line should have an id and efficiency and
			// an optional table...
			try {	if( (line=ifp.readLine()) == null ) {
					// Do a simple message.  The full
					// message is printed below.
					message = "null line";
					throw new IOException ( message );
				}
			}
			catch ( IOException e ){
				Message.printWarning ( 3, routine, e );
				message = "Unexpected end of return/depletion "+
				"pattern file file (not enough data for " +
				"ID=\"" + id + "\"?, expecting " +
				num + " points).";
				Message.printWarning ( 2, routine, message );
				warning_known = true;
				throw new IOException ( message );
			}
			// Allow spaces at front and back...
			line = line.trim();
			++linenum;
			if (	(line.charAt(0) == '#') ||
				(line.charAt(0) == '\0') ||
				(line.charAt(0) == '\n') ) {
				// Comment or blank line...
				// Decrement because we still need to read
				// data...
				--i;
				continue;
			}
			// Now read the data...
			list = StringUtil.breakStringList( line, " \t",
				StringUtil.DELIM_SKIP_BLANKS );

			if( list.size() < 2 ){
				message = "ID line: \"" + line +
				"\" (line " + linenum + ") has bad format (" +
				"ID and percent expected).";
				Message.printWarning( 2, routine, message );
				warning_known = true;
				throw new IOException ( message );
			}
			rinfo.__return_id[i] =
				((String)list.get(0)).trim();
			// Get rid of the percent on the fly...
			rinfo.__return_percent[i] = StringUtil.atod(
				((String)list.get(1)).replace( '%', ' '));

			// If the list size is >= 3, also set the delay table
			// information...

			if ( list.size() >= 3 ) {
				rinfo.__return_table[i] =
				((String)list.get(2)).trim();
			}
			else {	// Default to structure default...
				rinfo.__return_table[i] = table;
			}

			if( Message.isDebugOn ) {
				Message.printDebug ( 10, routine,
				"	" + rinfo.__return_percent[i] + 
				" returns/depletions to " +
				rinfo.__return_id[i]  +
				" using delay table " +rinfo.__return_table[i]);
			}
		}

		rinfo_vec.add( rinfo );
	}
	try {	ifp.close();
	}
	catch( IOException e ){
	}

	return rinfo_vec;
	}
	catch ( Exception e ) {
		if ( warning_known ) {
			// Specific error so throw it...
			message =
			"Remaining data from \"" + rtnfile +
			"\" have not been read.";
			Message.printWarning ( 2, routine, message );
			throw new IOException ( e.getMessage() + " " + message);
		}
		else {	// Unknown cause for warning so print a generic
			// message...
			message =
			"General error reading return/depletion file \"" +
			rtnfile + "\" near line " + linenum + " last ID=\"" +
			id + "\"." + "  Remaining data have not been read.";
			Message.printWarning ( 2, routine, message );
			// REVISIT SAM 2006-04-03
			// Need to come up with a standard for exception
			// warnings.
			Message.printWarning ( 3, routine, e);
			throw new IOException ( message );
		}
	}
}

} // End of LegacyReturnFlow class
