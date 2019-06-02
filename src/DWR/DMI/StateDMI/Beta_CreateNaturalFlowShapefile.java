// Beta_CreateNaturalFlowShapefile - This code extracts shapefiles of StateMod baseflow nodes.

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_NodeNetwork;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.GIS.GeoView.ESRIShapefile;
import RTi.GIS.GeoView.GeoLayer;
import RTi.GR.GRPoint;

import cdss.domain.hydrology.network.HydrologyNode;
//import DWR.StateMod.StateMod_NodeNetwork;

/**
This code extracts shapefiles of StateMod baseflow nodes.  The code could be put into
commands in the Network Group when time allows.  Currently all of the files are relative to
SAM's computer.
*/
public class Beta_CreateNaturalFlowShapefile
{

/**
Constructor.
*/
public Beta_CreateNaturalFlowShapefile ()
{	String routine = "Beta_CreateNaturalFlowShapefile";
	String [] networks = {
			"J:\\cdss\\Data\\colorado_1_2007\\network\\cm2005.net",
			"J:\\cdss\\Data\\Gunnison_2_2007\\network\\gm2004.net",
			"J:\\cdss\\Data\\sj2004_20051101\\cdss\\data\\sj2004\\network\\sj2004.net",
			"J:\\cdss\\Data\\Yampa_2_2007\\network\\ym2004.net",
			"J:\\cdss\\Data\\whiteT\\makenet\\whiteT.net",
			};
	// Shapefiles and id attributes (
	String [] shapefiles = {
			"J:\\CDSS\\GIS\\Div4\\div4_flowstations.shp",
			"J:\\CDSS\\GIS\\Div5\\div5_flowstations.shp",
			"J:\\CDSS\\GIS\\Div6\\div6_flowstations.shp",
			"J:\\CDSS\\GIS\\Div7\\div7_flowstations.shp",
			"J:\\CDSS\\GIS\\CO\\CO_flowstations.shp", // For out of state locations
			"J:\\CDSS\\GIS\\Div4\\div4_diversions.shp",
			"J:\\CDSS\\GIS\\Div5\\div5_diversions.shp",
			"J:\\CDSS\\GIS\\Div6\\div6_diversions.shp",
			"J:\\CDSS\\GIS\\Div7\\div7_diversions.shp",
			"J:\\CDSS\\GIS\\Div4\\div4_reservoirs.shp",
			"J:\\CDSS\\GIS\\Div5\\div5_reservoirs.shp",
			"J:\\CDSS\\GIS\\Div6\\div6_reservoirs.shp",
			"J:\\CDSS\\GIS\\Div7\\div7_reservoirs.shp",
			};
	// Fields to search (station_id is streamflow and others are for structures)
	String [] idfields = { "station_id", "id_label_6", "id_label_7" };
	
	// Loop through the networks and extract the natural flow stations.
	
	List<HydrologyNode> allnfnodes = new Vector<HydrologyNode>();
	for ( int inet = 0; inet < networks.length; inet++ ) {
		StateMod_NodeNetwork net = null;
		try {
			net = StateMod_NodeNetwork.readStateModNetworkFile( networks[inet], null, true );
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "Unexpected error reading network file \"" +
					networks[inet] + "\"...skipping..." );
		}
		
		// Get the baseflow nodes...
		
		List<HydrologyNode> nfnodes = net.getBaseflowNodes();
		int size_nfnodes = nfnodes.size();
		Message.printStatus(2, routine, "Got " + size_nfnodes + " natural flow nodes" );
		
		// For each node try to find a matching identifier in the shapefiles and set as the
		// alternate location in the node.
		
		HydrologyNode node = null;
		// Array of in-memory layers and metadata
		GeoLayer [] layers = new GeoLayer[shapefiles.length]; // The shapefiles read into layers
		int [][] idcols = new int[shapefiles.length][]; // The columns in each shapefile to check
		String [][] attributes = new String[shapefiles.length][];  // The attributes in each shapefile to check
		int [] nidfields = new int[shapefiles.length];	// The number of ID fields to search per shapefile
		GeoLayer layer;
		GRPoint point;
		for ( int inode = 0; inode < size_nfnodes; inode++ ) {
			node = nfnodes.get(inode);
			allnfnodes.add ( node );
			String node_id = node.getCommonID();
			Message.printStatus(2, routine, "Searching for node \"" + node_id + "\"" );
			for ( int isf = 0; isf < shapefiles.length; isf++ ) {
				if ( layers[isf] == null ) {
					// Read it
					try {
						PropList props = new PropList ("");
						props.set ( "ReadAttributes=true");
						layers[isf] = ESRIShapefile.readLayer(shapefiles[isf], props );
					}
					catch ( IOException e ) {
						Message.printWarning ( 2, routine, "Error reading shapefile \"" + shapefiles[isf] + "\"" );
					}
					DataTable table = layers[isf].getAttributeTable();
					// Get the field names that are valid that match fields to search.
					// Save as integers to allow more rapid lookup.
					attributes[isf] = table.getFieldNames();
					idcols[isf] = new int[attributes.length];
					nidfields[isf] = 0;
					for ( int iattribute = 0; iattribute < attributes[isf].length; iattribute++ ) {
						Message.printStatus ( 2, routine, "Attribute [" + iattribute + "] = " +
								attributes[isf][iattribute] );
						for ( int i_idfields = 0; i_idfields < idfields.length; i_idfields++ ) {
							if ( attributes[isf][iattribute].equalsIgnoreCase(idfields[i_idfields])) {
								// Save matching ID columns
								// Use nidfields to shorten the array size and use for iteration below.
								attributes[isf][nidfields[isf]] = table.getFieldName(iattribute);
								Message.printStatus(2, routine, "Attribute " + attributes[isf][nidfields[isf]] +
										" is in column " + iattribute );
								idcols[isf][nidfields[isf]++] = iattribute;
							}
						}
					}
					Message.printStatus ( 2, routine, "Shapefile has " +
							layers[isf].getAttributeTable().getNumberOfRecords() + " records and " +
							nidfields[isf] + " attributes to search.");
				}
				// Now have data that can be searched.
				layer = layers[isf];
				DataTable table = layer.getAttributeTable();
				int nrec = table.getNumberOfRecords();
				TableRecord rec;
				String id = null;
				Object o;	// Field value as Object
				// Search the records...
				boolean found = false;	// Did not find model ID
				int col;
				for ( int irec = 0; irec < nrec; irec++ ) {
					try {
						rec = table.getRecord(irec);
					}
					catch ( Exception e ) {
						// Should not happen
						Message.printWarning(2,routine,e);
						continue;
					}
					// Search the fields for the ID...
					for ( int idfield = 0; idfield < nidfields[isf]; idfield++ ) {
						try {
							col = idcols[isf][idfield];
							o = rec.getFieldValue(col);
							if ( o == null ) {
								// No data.
								continue;
							}
							id = (String)o;
							// Print for troubleshooting to make sure correct data are being checked.
							//if ( inode < 5 ) {
							//	Message.printStatus(2, routine, "Attribute " + attributes[isf][idfield] + "=\"" +
							//		id + "\"" );
							//}
						}
						catch ( Exception e ) {
							// Should not happen.
							Message.printWarning(2,routine,e);
						}
						if ( node_id.equalsIgnoreCase(id) ) {
							// Found the node.
							point = (GRPoint)layer.getShape(irec);
							node.setDBX ( point.x );
							node.setDBY ( point.y );
							found = true;
							break;
						}
					}
					if ( found ) {
						// Break out of next loop.
						break;
					}
				}
			}
		}
	}
	
	// Generate a delimited file with all the nodes and their coordinates.  Leave the coordinates blank
	// if not known.
	
	int size = allnfnodes.size();
	int missing_count = 0;
	int nonmissing_count = 0;
	int missing_model = 0;
	int nonmissing_flow = 0;
	int missing_flow = 0;
	int nonmissing_reservoirs = 0;
	int missing_reservoirs = 0;
	int nonmissing_diversions = 0;
	int missing_diversions = 0;
	int nonmissing_instream = 0;
	int missing_instream = 0;
	int nonmissing_imports = 0;
	int missing_imports = 0;
	String full_fname = "H:\\Home\\Beware\\StateMod_NFStations.csv";
	PrintWriter fp = null;
	try {
		fp = new PrintWriter ( new FileOutputStream ( full_fname ) );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "Unable to open output file \"" + full_fname + "\"" );
		return;
	}
	fp.println ( "COORDFLAG,NODETYPE,ID,NAME,X,Y" );
	for ( int i = 0; i < size; i++ ) {
		HydrologyNode node = (HydrologyNode)allnfnodes.get(i);
		if ( (node.getDBX() <= 0) || (node.getDBY() <= 0) ) {
			fp.println ( "None" + "," + HydrologyNode.getTypeString(node.getType(),HydrologyNode.ABBREVIATION) +
					"," + node.getCommonID() + ",\"" + node.getDescription() + "\",," );
			++missing_count;
			if ( node.getType() == HydrologyNode.NODE_TYPE_FLOW ) {
				++missing_flow;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_DIV ) {
				++missing_diversions;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_RES ) {
				++missing_reservoirs;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_ISF ) {
				++missing_instream;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_IMPORT ) {
				++missing_imports;
			}
			if ( !StringUtil.isDouble(node.getCommonID()) || node.getCommonID().startsWith("9") ) {
				++missing_model;
				;
			}
		}
		else {
			fp.println ( "InDB" + "," + HydrologyNode.getTypeString(node.getType(),HydrologyNode.ABBREVIATION) +
					"," + node.getCommonID() + ",\"" +
					node.getDescription() + "\"," +	node.getDBX() + "," + node.getDBY() );
			++nonmissing_count;
			if ( node.getType() == HydrologyNode.NODE_TYPE_FLOW ) {
				++nonmissing_flow;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_DIV ) {
				++nonmissing_diversions;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_RES ) {
				++nonmissing_reservoirs;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_ISF ) {
				++nonmissing_instream;
			}
			else if ( node.getType() == HydrologyNode.NODE_TYPE_IMPORT ) {
				++nonmissing_imports;
			}
		}
	}
	fp.close();
	Message.printStatus(2, routine, "Natural flow station locations nonmissing = " + nonmissing_count +
			" missing = " + missing_count );
	Message.printStatus(2, routine,	"Missing non-numerical ID stations (aggregates, 95+ IDs, etc.) = " + missing_model );
	Message.printStatus(2, routine,	"Flow stations = " + nonmissing_flow + " missing = " + missing_flow );
	Message.printStatus(2, routine,	"Diversions = " + nonmissing_diversions + " missing = " + missing_diversions );
	Message.printStatus(2, routine,	"Reservoirs = " + nonmissing_reservoirs + " missing = " + missing_reservoirs );
	Message.printStatus(2, routine,	"Instream flow = " + nonmissing_instream + " missing = " + missing_instream );
	Message.printStatus(2, routine,	"Imports = " + nonmissing_imports + " missing = " + missing_imports );

}

}
