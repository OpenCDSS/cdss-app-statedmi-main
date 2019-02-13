// StateDMI_NodeDataProvider 

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

import java.util.List;
import java.util.Vector;

import cdss.domain.hydrology.network.HydrologyNode;
import cdss.domain.hydrology.network.HydrologyNodeNetwork;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_Geoloc;
import DWR.DMI.HydroBaseDMI.HydroBase_Station;
import DWR.DMI.HydroBaseDMI.HydroBase_StationView;
import DWR.DMI.HydroBaseDMI.HydroBase_Structure;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import DWR.DMI.HydroBaseDMI.HydroBase_StructureWDWater;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_WellApplicationView;

import DWR.StateMod.StateMod_NodeDataProvider;
import DWR.StateMod.StateMod_NodeNetwork;

import RTi.GIS.GeoView.UTMProjection;
import RTi.GR.GRPoint;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

/**
 * This class is passed to 
 * @author sam
 *
 */
public class StateDMI_NodeDataProvider implements StateMod_NodeDataProvider
{
/**
 * HydroBaseDMI instance used to query data.
 */
HydroBaseDMI __dmi = null;

/**
 * Constructor.
 * @param dmi HydrobaseDMI to use to query for node description data.
 */
public StateDMI_NodeDataProvider ( HydroBaseDMI dmi )
{
	__dmi = dmi;
}

/**
Looks up the X and Y coordinates for a record in the geoloc table.
@param geoloc_num the geoloc_num of the record in the geoloc table.
@return a two-element double array, the first element of which is the X location
and the second element of which is the Y location.
*/
private double[] findGeolocCoordinates(int geoloc_num) {
	double[] coords = new double[2];
	coords[0] = -1;
	coords[1] = -1;

	if (__dmi == null) {
		return coords;
	}

	try {
		HydroBase_Geoloc g = __dmi.readGeolocForGeoloc_num(geoloc_num);
		if (g != null) {
			coords[0] = g.getUtm_x();
			coords[1] = g.getUtm_y();
		}
		return coords;
	}
	catch (Exception e) {
		Message.printWarning(2, "", e);
		return coords;
	}
}

/**
Format the WDID, accounting for padded zeros, etc., for StateMod files.
This is used instead of the code in HydroBase_WaterDistrict because it makes
a check for node type.
@param wd the wd
@param id the id
@param nodeType the type of the node.
@return the formatted WDID.
*/
private String formatWDID(int wd, int id, int nodeType) {
	String tempID = "" + id;
	String tempWD = "" + wd;

	return formatWDID(tempWD, tempID, nodeType); 
}

/**
Format the WDID, accounting for padded zeros, etc., for StateMod files.
This is used instead of the code in HydroBase_WaterDistrict because it makes
a check for node type.
@param wd the wd
@param id the id
@param nodeType the type of the node.
@return the formatted WDID.
*/
public String formatWDID(String wd, String id, int nodeType) {
	String routine = "StateDMI_NodeDataProvider.formatWDID";
	
	int dl = 10;
	int idFormatLen = 4;
	int idLen;
	int wdFormatLen = 2;
	String message;
	String wdid;	

	// Wells have a 5 digit id. For now, this is the only type that
	// has anything but a 4 digit id...
	if (nodeType == HydrologyNode.NODE_TYPE_WELL) {
		idFormatLen = 5;
	}

	if (Message.isDebugOn) {
		message = "WD: " + wd + " ID: " + id;
		Message.printDebug(dl, routine, message);  
	}

	wdid = wd;
	idLen = id.length();

	if (idLen > 5) {
		// Long identifiers are assumed to be used as is.
		wdid = id;
	}
	else {	
		// Prepend the WD to the identifier...
		for (int i = 0; i < wdFormatLen - wdid.length(); i++) {
			wdid = "0"+ wdid;
		}

		for (int i = 0; i < idFormatLen - idLen; i++) {
			wdid = wdid.concat("0");
		}
		wdid = wdid.concat(id);
	}

	if (Message.isDebugOn) {
		message = "Finished WDID: " +wdid;
		Message.printDebug(dl, routine, message);  
	}

	return wdid;
}

// FIXME SAM 2009-01-21 Can't this method be told if the node is expected to be a structure
// or a station... to make error handling more robust?
/**
Looks up the location of a structure in the database.
@param identifier the identifier of the structure.
@return a two-element array, the first element of which is the X location
and the second of which is the Y location.  If none can be found, both values will be -1.
*/
public double[] lookupNodeLocation( String identifier )
{
	String routine = "StateDMI_NodeDataProvider.lookupNodeLocation";
	double[] loc = new double[2];
	loc[0] = -999.00;
	loc[1] = -999.00;
	double lat; // Used if lat/long are in DB and UTM is not -> project to UTM
	double lon;

	String id = identifier;
	int index = id.indexOf(":");
	if (index > -1) {
		id = id.substring(index + 1);
	}

	HydroBase_StructureView structure = null;
	if ( HydroBase_WaterDistrict.isWDID(id) ) {
		// Try getting the location from the structure.
		try {
			int[] wdid = HydroBase_WaterDistrict.parseWDID(id);
			structure = __dmi.readStructureViewForWDID(wdid[0], wdid[1]);
		}
		catch (Exception e) {
			Message.printWarning(3, routine, "Error reading WDID data for \"" + id + "\"");
			Message.printWarning(3, routine, e);
			structure = null; // To skip next block of code
		}
	
		if (structure != null) {
			try {
				HydroBase_Geoloc geoloc = __dmi.readGeolocForGeoloc_num( structure.getGeoloc_num());
				loc[0] = geoloc.getUtm_x();
				loc[1] = geoloc.getUtm_y();
				lat = geoloc.getLatdecdeg();
				lon = geoloc.getLongdecdeg();
				projectIfNeeded ( id, loc, lat, lon );
			}
			catch (Exception e) {
				Message.printWarning(3, routine, "Error reading WDID data for \"" + id + "\"");
				Message.printWarning(3, routine, e);
			}
		}
	}

	if (loc[0] > -999.0 && loc[1] > -999.0) {
		// Found the information
		return loc;
	}
	
	try {
		HydroBase_StationView station = __dmi.readStationViewForStation_id(id);
		if (station == null) {
			Message.printWarning(3, routine, "Couldn't find UTM locations for identifier \"" + identifier +
			"\" - tried WDID and station ID but could not match location.");
			return loc;
		}

		HydroBase_Geoloc geoloc = __dmi.readGeolocForGeoloc_num(station.getGeoloc_num());
		loc[0] = geoloc.getUtm_x();
		loc[1] = geoloc.getUtm_y();
		lat = geoloc.getLatdecdeg();
		lon = geoloc.getLongdecdeg();
		projectIfNeeded ( id, loc, lat, lon );
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error reading station data.");
		Message.printWarning(2, routine, e);
	}		

	if (loc[0] > -999.0 && loc[1] > -999.0) {
		return loc;
	}
	
	Message.printWarning(3, routine, "Couldn't find UTM locations for identifier \"" + identifier +
		"\" - station coordinates are missing.");

	return loc;
}

/**
Project the lat/long to UTM if needed.  For some reason, HydroBase may have lat/long but not UTM.
@param id identifier of location with coordinates - for logging.
@param loc UTM x, and y coordinates - will be updated if missing and lat/long are not.
@param lat latitude
@param lon longitude
*/
private void projectIfNeeded ( String id, double [] loc, double lat, double lon )
{	String routine = getClass().getName() + ".projectIfNeeded";
	if ( (loc[0] <= -998.0) && (loc[1] <= -998.0) && (lat > 0.0) && (lon > -181.0) ) {
		// Have lat/long but not UTM - project it
		try {
			UTMProjection utmProjection = UTMProjection.parse ( "UTM,13,NAD83" );
			GRPoint pt = new GRPoint ( lon, lat );
			utmProjection.project(pt, true);
			loc[0] = pt.x;
			loc[1] = pt.y;
			Message.printStatus(2, routine, "UTM coordinates not in database for \"" + id +
				"\" but lat/long are - projected to UTM.");
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Unable to project (" + e + ")." );
		}
	}
}

/**
 * Set the descriptions on the nodes in the network.
 * @param network Network to update with node descriptions.
 * @param createFancyDescription true if formatted descriptions should be produced,
 * indicating node type and stream.
 * @param createOutputFiles true if output files are being created (legacy parameter to
 * improve performance?)
 */
public void setNodeDescriptions ( StateMod_NodeNetwork network, boolean createFancyDescription,
		boolean createOutputFiles )
{	String routine = "StateDMI_NodeDataProvider.setNodeDescriptions";
	double[] coords = null;
	HydroBase_Station station;
	HydroBase_StationView view;
	HydroBase_Structure structure;
	HydroBase_StructureWDWater wdwater;
	HydroBase_WellApplicationView well_applicationView;		
	int	dl = 15, 
		geoloc_num = 0,
		i = 0, 
		id = 0, 
		type = 0, 
		wd = 0;	
	String 	message,
		nodeType,
		stationName,
		streamName,
		userDesc,
		wdid;
	List	idList = null,
		permitList = new Vector(),
		statList = null,
		structList = null,
		waterList = null,
		wellApplications = null;

	Message.printStatus(2, routine, "Setting node names from HydroBase...");

	// Get the list of HydroBase_Stations and HydroBase_Structures 
	// present in the Network as obtained from the database.  First 
	// get stations (do this regardless of whether fancy descriptions 
	// are used since the query is generally fast and output can be 
	// easily formatted)...

	int [] nodeTypes = null;
	try {	
		// Get list of stations as strings...
		nodeTypes = new int[1];
		nodeTypes[0] = HydrologyNode.NODE_TYPE_FLOW;
		idList = network.getNodeIdentifiersByType(nodeTypes);
		// Now query to get the HydroBase_Station list...
		Message.printStatus(2, routine,	"Getting station information from the database...");
		StopWatch timer = new StopWatch();
		timer.start();
		if (__dmi != null) {
			statList = __dmi.readStationListForStation_idList(idList);
		}
		if (statList != null) {
			Message.printStatus(2, routine,
				"Query for " + statList.size()
				+ " stations took " + (int)timer.getSeconds() + " seconds.");
		}
		else {
			statList = new Vector();
		}
	}
	catch (Exception e) {
		message = "Errors finding stations in node network.  Can't set station descriptions.";
		Message.printWarning(2, routine, message);
		Message.printWarning(2, routine, e);
		throw new RuntimeException(message);
	}

	// Now get structures so we can fill in descriptions (do this regardless
	// of whether fancy descriptions are used).  Structure types ...

	try {	
		// Get list of structures as strings...
		nodeTypes = new int[5];
		nodeTypes[0] = HydrologyNode.NODE_TYPE_DIV;
		nodeTypes[1] = HydrologyNode.NODE_TYPE_RES;
		nodeTypes[2] = HydrologyNode.NODE_TYPE_ISF;
		nodeTypes[3] = HydrologyNode.NODE_TYPE_IMPORT;
		nodeTypes[4] = HydrologyNode.NODE_TYPE_DIV_AND_WELL;
		// Ground water wells only (NODE_TYPE_WELL) are treated
		// separately below...
		idList = network.getNodeIdentifiersByType(nodeTypes);
		// Now query to get the HydroBase_Structure list...
		Message.printStatus(2, routine,	"Getting structure information from the database...");
		StopWatch timer = new StopWatch();
		timer.start();
		if ( __dmi != null ) {
			structList = __dmi.readStructureListForWDIDs(idList);
		}
		timer.stop();
		if (structList != null) {
			Message.printStatus(2, routine,
				"Query for " + structList.size() 
				+ " structures took " + (int)timer.getSeconds() + " seconds.");
		}
		else {
			structList = new Vector();
		}
	}
	catch (Exception e) {
		message = "Errors finding structures in node network.  Can't set structure descriptions.";
		Message.printWarning(2, routine, message);
		Message.printWarning(2, routine, e);
		throw new RuntimeException(message);
	}

	// Now get stream information from the database for fancy descriptions
	// so that they can be used for ditches and other structures...
	if (createFancyDescription) {
		Message.printStatus(2, routine,	"Getting stream information from the database...");
		// Need to get streams...
		try {	
			StopWatch timer = new StopWatch();
			timer.start();
			if (__dmi != null) {
				waterList = __dmi.readStructureWDWaterListForStructureIDs(idList);
			}
			timer.stop();
			if (waterList != null) {
				Message.printStatus(2, routine,
					"Query for " + waterList.size() + " wdwaters took " 
					+ (int)timer.getSeconds() + " seconds.");
			}
			else {
				waterList = new Vector();
			}
		}
		catch (Exception e) {
			message = "Errors finding HydroBase_WDWater objects for node network.";
			Message.printWarning(2, routine, message);
			Message.printWarning(2, routine, e);
			throw new RuntimeException(message);
		}
	
		if (waterList == null) {
			message = "Could not find HydroBase_WDWater objects for node network.";
			Message.printWarning(2, routine, message);
			throw new RuntimeException(message);
		}
		if (waterList.size() == 0) {
			message = "Could not find HydroBase_WDWater objects in node network.";
			Message.printWarning(2, routine, message);
			throw new RuntimeException(message);
		}
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine,	"Setting fancy descriptions.");
		}
	}

	// Now get the ground water well only...
	/*
	REVISIT (JTS - 2004-03-15)
	wellList is not used by the rest of the method, so this was 
	commented out
	try {	// Get list of stations as strings...
		nodeTypes = new int[1];
		nodeTypes[0] = HydroBaseNode.NODE_TYPE_WELL;
		idList = getNodeIdentifiersByType(nodeTypes);
		// Now query to get the HydroBase_Wells list...
		Message.printStatus(2, routine,
		"Getting well information from the database...");
		StopWatch timer = new StopWatch();
		timer.start();
		wellList = HydroBaseDMIUtil.getStructureToWellFromIDs(
			__dmi, idList, HydroBaseDMIUtil.STRUCT_TO_WELL_WELL,
			true);
		if (wellList != null) {
			Message.printStatus(2, routine,
				"Query for " + wellList.size()
				+ " wells took " + (int)timer.getSeconds() 
				+ " seconds.");
		}
	}
	catch (Exception e) {
		message = "Errors finding wells in node network.  " +
		"Can't set well descriptions.";
	}
	*/

	// Process all the nodes in the network, setting the descriptions as
	// appropriate...

	Message.printStatus(2, routine, "Setting node descriptions...");

	boolean wdid_structure;
	HydrologyNode nodePt = null;
	int[]	wdidArray;
	int 	idot = 0,
		i_structList = -1,
		i_waterList = -1,
		i_stationList = -1,
		nstationList = 0,
		nstructList = 0,
		nwaterList = 0;
	String desc = "";

	boolean done = false;

	// TODO - reworked from a for loop while doing some debugging
	// should probably set it back as this might be confusing.
	nodePt = network.getMostUpstreamNode();
	boolean cont = false;
	while (!done) {
		if (cont) {
			cont = false;
			nodePt = HydrologyNodeNetwork.getDownstreamNode(nodePt, StateMod_NodeNetwork.POSITION_COMPUTATIONAL );
		}

		if (nodePt == null) {
			done = true;
			type = -1;
			userDesc = "";
			nodeType = "";
			streamName = "";
		}
		else {
			type = nodePt.getType();	
			userDesc = nodePt.getUserDescription();
			nodeType = HydrologyNode.getTypeString(type, 1); 
			streamName = "";
		}

		if (nodePt == null) {}
		else if (type == HydrologyNode.NODE_TYPE_BLANK) {
			nodePt.setDescription("BLANK NODE - PLOT ONLY");
		}
		else if (type == HydrologyNode.NODE_TYPE_CONFLUENCE) {
			nodePt.setDescription("CONFLUENCE - PLOT ONLY");
		}
		else if (type == HydrologyNode.NODE_TYPE_END) {
			if (userDesc.length() > 0) {
				// User-defined...
				nodePt.setDescription(userDesc);
			}
			else {	// Default...
				nodePt.setDescription("END");
			}
			// The end of the network, so done...
			done = true;
		}
		else if ((type == HydrologyNode.NODE_TYPE_RES) 
		    || (type == HydrologyNode.NODE_TYPE_DIV) 
		    || (type == HydrologyNode.NODE_TYPE_DIV_AND_WELL) 
		    || ((type == HydrologyNode.NODE_TYPE_WELL) 
		    && nodePt.getCommonID().charAt(0) != 'P' 
		    && StringUtil.isInteger(nodePt.getCommonID())) 
		    || (type == HydrologyNode.NODE_TYPE_ISF) 
		    || (type == HydrologyNode.NODE_TYPE_IMPORT)) {
			// First see if the id can be parsed out.  If not a
			// true structure, then just leave the description as
			// is(assume it was set by the user).  WELL nodes are
			// included if the ID is not a permit but is a number
			// only (in which case it is assumed to NOT be an
			// aggregation, etc.)...
			wdid_structure = true;
			idot = nodePt.getCommonID().indexOf('.');
			String wdid2parse = nodePt.getCommonID();
			if (idot >= 0) {
				// ID has a period (like old-style ISF)
				wdid2parse = nodePt.getCommonID().substring(0,idot);
			}
			try {
				wdidArray = HydroBase_WaterDistrict.parseWDID(wdid2parse);
			}
			catch (Exception e) {
				// TODO (JTS - 2004-03-24)
				// this is to handle aggregate 
				// diversion nodes (43_ADW3030, etc)
				// that break the routine
				Message.printStatus (2, routine,
				"Node ID \"" + nodePt.getCommonID() +
				"\" is not a WDID.  Skipping HydroBase query." );
				//Message.printWarning(2, routine, e);
				wdidArray = null;
			}
			
			if (wdidArray == null) {
				wdid_structure = false;
			}
			
			if (wdid_structure) {
				wd = wdidArray[0];
				id = wdidArray[1];
			}
			else {	
				wd = 0;
				id = 0;
			}
			
			// Use the description information from the database...
			if (createFancyDescription) {
				// Get the stream associated with the
				// structure.  The structure and wdwater list
				// should be in the same order.  As an item
				// is found, remove from the lists so that
				// subsequent searches are faster...
				// First find the structure...
				nstructList = structList.size();
				// Initialize to the current description (which
				// will be the user description if specified)...
				desc = nodePt.getDescription();
				i_structList = -1;
				geoloc_num = -1;
				for (i = 0; i < nstructList; i++) {
					structure = (HydroBase_StructureView)structList.get(i);
					if ((structure.getWD() == wd) && (structure.getID() == id)) {
						// Found the structure...
						i_structList = i;
						desc = structure.getStr_name();
						geoloc_num = structure
							.getGeoloc_num();
						// No need to keep searching...
						break;
					}
				}

				// Now find the HydroBase_StructureWDWater...
				streamName = "";
				i_waterList = -1;
				nwaterList = waterList.size();
				for (i = 0; i < nwaterList; i++) {
					wdwater = (HydroBase_StructureWDWater)
					waterList.get(i);
					if ((wdwater.getWD() == wd) && (wdwater.getID() == id)) {
						// Found the stream...
						i_waterList = i;
						streamName = wdwater.getStr_name();
						if (Message.isDebugOn) {
							Message.printDebug(1,
								routine, "wdwater for " + nodePt.getCommonID() + " is " + streamName);
						}
						break;
					}
				}
				if (i_waterList < 0) {
					if (Message.isDebugOn) {
						Message.printDebug(1, routine, "Did not find wdwater for " + nodePt.getCommonID());
					}
				}
				// Regardless of what we found, format the
				// output to be "fancy"...
				// Structures need to be identified in
				// terms of their "WDID"
				wdid = formatWDID(wd, id, type);
				if (Message.isDebugOn) {
					Message.printDebug(dl, 
						routine, "Structure WDID: " 
						+ StringUtil.atoi(wdid) + "  Node ID: " + nodePt.getCommonID());
				}
				// Set the node description using either the
				// existing user description or a stream name
				// from the db...
				if (userDesc.length() != 0) {
					// Use the user's description,
					// not that from the database and
					// ignore the stream...
					nodePt.setDescription(
						StringUtil.formatString(userDesc, "%-20.20s") + "_" 
						+ StringUtil.formatString(nodeType, "%-3.3s"));
				}
				else if (type == HydrologyNode.NODE_TYPE_ISF) {
					nodePt.setDescription(
						StringUtil.formatString(desc, "%-20.20s") 
						+ "_" + StringUtil.formatString(nodeType, "%-3.3s"));
				}
				else {	
					// Use the stream from the database as
					// the first 4 characters...
					nodePt.setDescription(
						StringUtil.formatString( streamName, "%-4.4s") 
						+ "_" +	StringUtil.formatString(desc, "%-15.15s") 
						+ "_" +	StringUtil.formatString(nodeType, "%-3.3s"));
				}
				coords = findGeolocCoordinates(geoloc_num);
				nodePt.setDBX(coords[0]);
				nodePt.setDBY(coords[1]);
				// Can now remove from the list
				// to speed searches for later structures but
				// only do so if not an ISF (because these are
				// reused when old-style WDID.xx notation is
				// used)...
				if ((i_structList >= 0) && (type != HydrologyNode.NODE_TYPE_ISF)) {
					structList.remove(	i_structList);
				}
				if ((i_waterList >= 0) && (type != HydrologyNode.NODE_TYPE_ISF)) {
					waterList.remove( i_waterList);
				}
			}
			else if ( (__dmi != null) && createOutputFiles) {
				// No fancy description.  Just use the
				// structure name for the description...
				// First find the structure...
				nstructList = structList.size();
				// Initialize to the current description (which
				// will be the user description if specified)...
				desc = nodePt.getDescription();
				i_structList = -1;
				geoloc_num = -1;
				for (i = 0; i < nstructList; i++) {
					structure = (HydroBase_StructureView)structList.get(i);
					if ((structure.getWD() == wd) && (structure.getID() == id)) {
						// Found the structure...
						i_structList = i;
						desc = structure.getStr_name();
						geoloc_num = structure.getGeoloc_num();
						break;
					}
				}

				// Regardless of what we found, format the output...
				wdid = formatWDID(wd, id, type);

				coords = findGeolocCoordinates(geoloc_num);
				nodePt.setDBX(coords[0]);
				nodePt.setDBY(coords[1]);

				if (Message.isDebugOn) {
					Message.printDebug(dl, routine,	"StructureNetID: " + StringUtil.atoi(wdid));
				}

				if (userDesc.length() > 0) {
					// User-defined...
					nodePt.setDescription(userDesc);
					cont = true;
					continue;
				}
				else {	
					// Use the description from above...
					nodePt.setDescription(desc);
				}
				// Can now remove from the list
				// to speed searches for later structures but
				// only do so if not an ISF (because these are
				// reused when old-style WDID.xx notation is
				// used)...
				if ((i_structList >= 0) && (type != HydrologyNode.NODE_TYPE_ISF)) {
					structList.remove( i_structList);
				}
			}
		}
		else if (type == HydrologyNode.NODE_TYPE_WELL) {
			// Already checked for a possible well as a structure
			// with a WDID above so this is either a well permit
			// with information in struct_to_well, in which case
			// the description is taken from the first matching
			// entry, or an aggregation, in which the description
			// will not be found.
			// First see if the id can be parsed out.  Identifiers
			// that start with P are assumed to be well permits.
			// Otherwise, then just leave the description as is
			// (assume it was set by the user)...
			// Use the description information from the database...

			// Get the description from the well_application
			// table (or other?).  Since there will not
			// typically be many WEL nodes, this should not
			// be that much of a hit...
			try {
			permitList.clear();
			permitList.add(nodePt.getCommonID());
			if ( __dmi != null) {
				wellApplications = __dmi.readWellApplicationListForPermitData( permitList);
			}
			if ((wellApplications == null) || (wellApplications.size() == 0)) {
				Message.printStatus(2, routine, "No well data for " + nodePt.getCommonID());
				desc = "";
				geoloc_num = -1;				
			}
			else {	
				// What came back has to be the permit...
				well_applicationView = (HydroBase_WellApplicationView)wellApplications.get(0);
				desc = well_applicationView.getWell_name();
				geoloc_num = well_applicationView.getGeoloc_num();
			}
			}
			catch (Exception e) {
				desc = "";
				geoloc_num = -1;
			}

			coords = findGeolocCoordinates(geoloc_num);
			nodePt.setDBX(coords[0]);
			nodePt.setDBY(coords[1]);
				
			if ( createFancyDescription || ((__dmi != null) && createOutputFiles)) {
				if (userDesc.length() != 0) {
					// Use the user's description,
					// not that from the database and
					// ignore the stream...
					nodePt.setDescription(
						StringUtil.formatString(userDesc, "%-20.20s") + "_" 
						+ StringUtil.formatString(nodeType, "%-3.3s"));
				}
				else {	
					// Use the name from the database as
					// the first 4 characters...
					nodePt.setDescription(
						StringUtil.formatString(
						desc, "%-20.20s") 
						+ "_" +	StringUtil.formatString(
						nodeType, "%-3.3s"));
				}
			}
		}
		else if ((type == HydrologyNode.NODE_TYPE_FLOW) 
		    || (type == HydrologyNode.NODE_TYPE_BASEFLOW) 
		    || (type == HydrologyNode.NODE_TYPE_OTHER)) {
			stationName = "";
			if (userDesc.length() > 0) {
				// User-defined...
				nodePt.setDescription(userDesc);
				stationName = userDesc;
			}
			else {	
				// Default...
				if (type == HydrologyNode.NODE_TYPE_BASEFLOW) {
					nodePt.setDescription("Baseflow Node");
				}
			}
			if ( createFancyDescription) {
				// Set the new description using the gage 
				// description and the node type...
				// FLOW From user code...
				// Find the Station...
				i_stationList = -1;
				nstationList = statList.size();
				// Search even if a user description has been
				// supplied to clean up list for other
				// searches...
				geoloc_num = -1;
				for (i = 0; i < nstationList; i++) {
					view = (HydroBase_StationView)statList.get(i);
					if (view.getStation_id().equalsIgnoreCase(nodePt.getCommonID())) {
						// Found the view...
						i_stationList = i;
						if (userDesc.length() <= 0) {
							// No user description...
							stationName = view.getStation_name();
							geoloc_num = view.getGeoloc_num();
						}
						if (Message.isDebugOn) {
							Message.printDebug(1, routine,
								"station for " + nodePt.getCommonID() + " is " + stationName);
						}
						// No need to search more...
						break;
					}
				}

				if (i_stationList < 0) {
					stationName = nodePt.getDescription();
				}

				coords = findGeolocCoordinates(geoloc_num);
				nodePt.setDBX(coords[0]);
				nodePt.setDBY(coords[1]);

				nodePt.setDescription(StringUtil.formatString(
					stationName, "%-20.20s") + "_" 
					+ StringUtil.formatString(nodeType, "%-3.3s"));
				// Now remove from the station list so searches
				// are faster...
				if (i_stationList >= 0) {
					statList.get(i_stationList);
				}
			}
			else if ( (__dmi != null) && createOutputFiles) {
				// Just use the station name...
				if (userDesc.length() > 0) {
					// User-defined...
					nodePt.setDescription(userDesc);
					cont = true;
					continue;
				}
				i_stationList = -1;
				nstationList = statList.size();
				if (__dmi.useStoredProcedures()) {
				for (i = 0; i < nstationList; i++) {
					view = (HydroBase_StationView)
					statList.get(i);
					if (view.getStation_id().equalsIgnoreCase( nodePt.getCommonID())) {
						// Found the view...
						i_stationList = i;
						if (userDesc.length() <= 0) {
							// No user description...
							nodePt.setDescription( view.getStation_name());
							geoloc_num = view.getGeoloc_num();
						}
						if (Message.isDebugOn) {
							Message.printDebug(1, routine, "station for " 
								+ nodePt.getCommonID() + " is " + stationName);
						}
						// No need to search more...
						break;
					}
				}
				}
				else {
				for (i = 0; i < nstationList; i++) {
					station = (HydroBase_Station)statList.get(i);
					if (station.getStation_id().equalsIgnoreCase( nodePt.getCommonID())) {
						// Found the station...
						i_stationList = i;
						if (userDesc.length() <= 0) {
							// No user description...
							nodePt.setDescription( station.getStation_name());
							geoloc_num = station.getGeoloc_num();
						}
						if (Message.isDebugOn) {
							Message.printDebug(1,
								routine, "station for " + nodePt.getCommonID() + " is " + stationName);
						}
						// No need to search more...
						break;
					}
				}
				}
				coords = findGeolocCoordinates(geoloc_num);
				nodePt.setDBX(coords[0]);
				nodePt.setDBY(coords[1]);
				// Can now remove from the list...
				if (i_stationList >= 0) {
					statList.remove(i_stationList);
				}
			}
		}
		else if (type == HydrologyNode.NODE_TYPE_XCONFLUENCE) {
			if (userDesc.length() > 0) {
				// User-defined...
				nodePt.setDescription(userDesc);
			}
			else {	// Default...
				nodePt.setDescription("XCONFLUENCE - PLOT ONLY");
			}
		}
		nodePt = HydrologyNodeNetwork.getDownstreamNode(nodePt, StateMod_NodeNetwork.POSITION_COMPUTATIONAL);
	}
	Message.printStatus(2, routine, "...done setting node names from HydroBase...");
}

}
