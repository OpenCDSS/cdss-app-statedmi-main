// HydroBase_GUI_IrrigatedAcresTool - This class is a GUI for displaying information about a structure's irrigated acres.

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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.DMI.HydroBaseDMI.HydroBase_CellRenderer;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTSStructureToParcel;
import DWR.DMI.HydroBaseDMI.HydroBase_Structure;
import DWR.DMI.HydroBaseDMI.HydroBase_TableModel_IrrigatedAcresTool;
import DWR.DMI.HydroBaseDMI.HydroBase_TableModel_Wells;
import DWR.DMI.HydroBaseDMI.HydroBase_WaterDistrict;
import DWR.DMI.HydroBaseDMI.HydroBase_Wells;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

import RTi.Util.IO.PropList;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractTableModel;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class is a GUI for displaying information about a structure's irrigated acres.
It is similar to HydroBase_GUI_IrrigatedAcres, however, this class understands data set and HydroBase
data and facilitates looking at lists of locations included in collections.  In particular, rather than
showing information for a single structure at the top, there are choices to select from the list
of diversions in the data set (to show the related parcels) and another section to display the wells related
to the parcels.  This gives a complete picture of the water supply for parcels.
*/
@SuppressWarnings("serial")
public class HydroBase_GUI_IrrigatedAcresTool extends JFrame implements ActionListener,
	ItemListener, KeyListener, WindowListener
{

private final String __CLASS = "HydroBase_GUI_IrrigatedAcres";

/**
Button labels.
*/
private final String __BUTTON_CLOSE = "Close";
private final String __BUTTON_GET_DIVERSION_DATA = "Get Parcel Data for Diversions/Ditches";
private final String __BUTTON_GET_PARCEL_DATA = "Get Parcel Data";
private final String __BUTTON_USE_PARCELS_FROM_ABOVE = "Use Parcels From Above";

/**
The DMI through which to connect to the database.
*/
private HydroBaseDMI __dmi;

/**
The list of diversion stations in the data set.
*/
private List<StateMod_Diversion> __diversionStationList;

/**
The list of well stations in the data set that are associated with ditches.
*/
private List<StateMod_Well> __wellsWithDitchStationList;

/**
The list of well stations in the data set that are associated with parcels (not ditches).
*/
private List<StateMod_Well> __wellsWithParcelStationList;

/**
Parcel year to filter diversion query.
*/
private JTextField __diversionParcelYearJTextField;

/**
GUI JTextfields to display data.
*/
private JTextField __statusJTextField;

/**
Text area for diversion stations.
*/
JTextArea __diversionStationList_JTextArea = null;

/**
Choice for diversion stations.
*/
SimpleJComboBox __diversionStation_JComboBox = null;

/**
Choice for wells that are associated with ditches.
*/
SimpleJComboBox __wellsWithDitches_JComboBox = null;

/**
The worksheet for diversions and associated parcels.
*/
private JWorksheet __divParcelWorksheet;

/**
Parcel year to filter well query.
*/
private JTextField __wellParcelYearJTextField;

/**
Division to filter well query.
*/
private JTextField __wellDivisionJTextField;

/**
Choice for wells associated with parcels.
*/
SimpleJComboBox __wellsWithParcels_JComboBox = null;

/**
Button to trigger querying diversion data.
*/
SimpleJButton __divGetData_JButton = null;

/**
The worksheet for wells and associated parcels.
*/
private JWorksheet __wellParcelWorksheet;

/**
Button to trigger querying well data.
*/
SimpleJButton __parcelGetData_JButton = null;

/**
Text area for parcels.
*/
JTextArea __parcelList_JTextArea = null;

/**
Button to transfer parcels associated with ditches to parcel ID text area.
*/
SimpleJButton __useDiversionParcels_JButton = null;

/**
Constructor.
@param dmi the dmi connection to use for communicating with the database.
@param diversionStationList list of diversion stations from latest StateDMI run.
@param wellStationList list of well stations from latest StateDMI run.
*/
public HydroBase_GUI_IrrigatedAcresTool ( HydroBaseDMI dmi, List<StateMod_Diversion> diversionStationList,
	List<StateMod_Well> wellStationList )
{
    __dmi = dmi;
    __diversionStationList = diversionStationList;
    __wellsWithDitchStationList = filterWellCollections(wellStationList, StateMod_Well.COLLECTION_PART_TYPE_DITCH);
    __wellsWithParcelStationList = filterWellCollections(wellStationList, StateMod_Well.COLLECTION_PART_TYPE_PARCEL);
    JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
    setupGUI ( __diversionStationList, __wellsWithDitchStationList, __wellsWithParcelStationList );
}

/**
Responds to action performed events.
@param e the ActionEvent that happened.
*/         
public void actionPerformed(ActionEvent e)
{
	//String routine = __CLASS + ".actionPerformed";
	String s = e.getActionCommand();

    if (s.equals(__BUTTON_CLOSE)) {
        closeClicked();
    }
    else if (s.equals(__BUTTON_GET_DIVERSION_DATA)) {
        submitDitchParcelQuery();
        checkGUIState();
    }
    else if (s.equals(__BUTTON_GET_PARCEL_DATA)) {
        submitParcelWellQuery();
    }
    else if (s.equals(__BUTTON_USE_PARCELS_FROM_ABOVE)) {
    	int div = getWellDivision();
    	int parcelYear = getWellParcelYear();
        transferParcelsFromDiversions(div, parcelYear);
        checkGUIState();
    }
}

/**
Check the GUI state and enable/disable features based on the state.
*/
private void checkGUIState()
{	// Enable diversion "get" button if diversion WDIDs are present
	String wdidText = __diversionStationList_JTextArea.getText().trim();
	if ( wdidText.equals("") ) {
		__divGetData_JButton.setEnabled(false);
	}
	else {
		__divGetData_JButton.setEnabled(true);
	}
	
	// Enable well/parcel "get" button if parcel IDs are present
	String parcelText = __parcelList_JTextArea.getText().trim();
	if ( parcelText.equals("") ) {
		__parcelGetData_JButton.setEnabled(false);
	}
	else {
		__parcelGetData_JButton.setEnabled(true);
	}
	// But disable if well division and parcel are not specified
	if ( getWellDivision() <= 0 ) {
		__parcelGetData_JButton.setEnabled(false);
	}
	if ( getWellParcelYear() <= 0 ) {
		__parcelGetData_JButton.setEnabled(false);
	}
	
	// Enable "Use Parcels from Above" if diversion parcel results are available
	
	if ( __divParcelWorksheet.getRowCount() > 0 ) {
		__useDiversionParcels_JButton.setEnabled(true);
	}
	else {
		__useDiversionParcels_JButton.setEnabled(false);
	}
	// But disable if well division and parcel are not specified
	if ( getWellDivision() <= 0 ) {
		__useDiversionParcels_JButton.setEnabled(false);
	}
	if ( getWellParcelYear() <= 0 ) {
		__useDiversionParcels_JButton.setEnabled(false);
	}
}

/**
Clear the text area that lists the diversion identifiers.
*/
private void clearDiversionText ()
{
	__diversionStationList_JTextArea.setText("");
}

/**
Clear the text area that lists the parcel identifiers.
*/
private void clearParcelText ()
{
	__parcelList_JTextArea.setText("");
}

/**
Closes the GUI.  
*/
private void closeClicked() {
	dispose();
}

/**
Filter the well information to include only well stations that are collections using ditches.
@param wellStationList list of well stations to filter.
@param collectionPartType part type to return in list.
*/
private List<StateMod_Well> filterWellCollections ( List<StateMod_Well> wellStationList, String collectionPartType )
{	List<StateMod_Well> list = new Vector<StateMod_Well>();
	for ( StateMod_Well well : wellStationList ) {
		if ( well.isCollection() &&
			well.getCollectionPartType().equalsIgnoreCase(collectionPartType)) {
			list.add ( well );
		}
	}
	return list;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__dmi = null;
	__statusJTextField = null;
	__divParcelWorksheet = null;
	super.finalize();
}

/**
Get the parcel year to use with diversions, from the parcel year field.
*/
private int getDiversionParcelYear ()
{
	String parcelYear = __diversionParcelYearJTextField.getText();
	int parcelYear_int = 0;
	if ( StringUtil.isInteger(parcelYear) ) {
		parcelYear_int = Integer.parseInt(parcelYear);
	}
	return parcelYear_int;
}

/**
Get the parcel year to use with wells, from the well division field.
*/
private int getWellDivision ()
{
	String well = __wellDivisionJTextField.getText();
	int well_int = 0;
	if ( StringUtil.isInteger(well) ) {
		well_int = Integer.parseInt(well);
	}
	return well_int;
}

/**
Get the parcel year to use with diversions, from the well parcel year field.
*/
private int getWellParcelYear ()
{
	String parcelYear = __wellParcelYearJTextField.getText();
	int parcelYear_int = 0;
	if ( StringUtil.isInteger(parcelYear) ) {
		parcelYear_int = Integer.parseInt(parcelYear);
	}
	return parcelYear_int;
}

/**
Handle item events in choices.
*/
public void itemStateChanged ( ItemEvent event )
{	String routine = __CLASS + ".itemStateChanged";
	Object source = event.getSource();
	if ( event.getStateChange() == ItemEvent.DESELECTED ) {
		return;
	}
	if ( source == __diversionStation_JComboBox ) {
		// Display the WDID in the text area or the list of collection parts
		String wdid = StringUtil.getToken(__diversionStation_JComboBox.getSelected(), "-", 0, 0 ).trim();
		if ( wdid != null ) {
			wdid = "";
		}
		wdid = wdid.trim();
		if ( wdid.equals("") ) {
			// Cleared the selection
			__diversionStationList_JTextArea.setText("");
			checkGUIState();
			return;
		}
		// Find the item in the list
		int pos = StateMod_Util.indexOf(__diversionStationList, wdid);
		if ( pos >= 0 ) {
			// Clear the text area and then fill with the diversion WDID(s)
			clearDiversionText();
			StateMod_Diversion div = __diversionStationList.get(pos);
			if ( div.isCollection() ) {
				// Display the collection parts as a delimited list.
				__diversionStationList_JTextArea.setText(StringUtil.toString(
					div.getCollectionPartIDs(0), ","));
			}
			else {
				// Just display the single ID
				__diversionStationList_JTextArea.setText(wdid);
			}
		}
		else {
			Message.printWarning(1, routine, "Unable to locate diversion \"" + wdid + "\" in well station list.");
		}
	}
	else if ( source == __wellsWithDitches_JComboBox ) {
		// Display the collection parts as a delimited list - will always be a list of WDIDs because the
		// well list has been filtered.
		String id = StringUtil.getToken(__wellsWithDitches_JComboBox.getSelected(), "-", 0, 0 );
		if ( id == null ) {
			id = "";
		}
		id = id.trim();
		if ( id.equals("") ) {
			// Cleared the selection
			__diversionStationList_JTextArea.setText("");
			checkGUIState();
			return;
		}
		// Find the item in the list
		int pos = StateMod_Util.indexOf(__wellsWithDitchStationList, id);
		if ( pos >= 0 ) {
			// Clear the text area and then fill with the diversion WDID(s)
			clearDiversionText();
			StateMod_Well well = __wellsWithDitchStationList.get(pos);
			__diversionStationList_JTextArea.setText(StringUtil.toString(
			well.getCollectionPartIDs(0), ","));
		}
		else {
			Message.printWarning(1, routine, "Unable to locate well \"" + id + "\" in well station list.");
		}
	}
	else if ( source == __wellsWithParcels_JComboBox ) {
		// Display the collection parts as a delimited list - will always be a list of parcel IDs because the
		// well list has been filtered.
		String id = StringUtil.getToken(__wellsWithParcels_JComboBox.getSelected(), "-", 0, 0 );
		if ( id == null ) {
			id = "";
		}
		id = id.trim();
		if ( id.equals("") ) {
			// Cleared the selection
			__parcelList_JTextArea.setText("");
			checkGUIState();
			return;
		}
		// Find the item in the list
		int pos = StateMod_Util.indexOf(__wellsWithParcelStationList, id);
		// Requires the parcel year...
		int parcelYear = getWellParcelYear();
		if ( parcelYear <= 0 ) {
			Message.printWarning ( 1, routine, "The parcel year must be specified.", this );
			return;
		}
 		if ( pos >= 0 ) {
			// Clear the text area and then fill with the parcel ID(s)
			clearParcelText();
			StateMod_Well well = __wellsWithParcelStationList.get(pos);
			__parcelList_JTextArea.setText(StringUtil.toString(well.getCollectionPartIDs(parcelYear), ","));
		}
		else {
			Message.printWarning(1, routine, "Unable to locate well \"" + id + "\" in well station list.");
		}
	}
	// Verify the state of the interface after actions are taken
	checkGUIState();
}

/**
Respond to KeyEvents.
*/
public void keyPressed (KeyEvent event)
{
	//int code = event.getKeyCode();
	checkGUIState();
}

public void keyReleased (KeyEvent event)
{
}

public void keyTyped (KeyEvent event)
{
}

/**
Sets up the GUI.
*/
private void setupGUI ( List<StateMod_Diversion>diversionStationList,
	List<StateMod_Well> wellsWithDitchStationList, List<StateMod_Well> wellsWithParcelStationList ) {
	String routine = __CLASS + ".setupGUI";
	addWindowListener(this);

    // objects used throughout the GUI layout
    Insets insetsTLNR = new Insets(2,2,0,2);
    GridBagLayout gbl = new GridBagLayout();

    // Main (center) JPanel, to hold most information, including diversion and well supply
    JPanel mainJPanel = new JPanel();
    mainJPanel.setLayout(gbl); // To facilitate resizing
    getContentPane().add("Center", mainJPanel);
    
    JPanel diversionMainJPanel = new JPanel(); // Diversion query information and ditch/parcel results
    diversionMainJPanel.setLayout(gbl);
    JGUIUtil.addComponent(mainJPanel, diversionMainJPanel, 
        	0, 0, 1, 1, 1.0, 1.0, insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    // Panel to hold diversion/ditch data
	JPanel diversionTopJPanel = new JPanel();
	diversionTopJPanel.setBorder(BorderFactory.createTitledBorder(
		"Ditch(es) identified using WDID(s) [pick from list or enter comma-separated values]"));
	diversionTopJPanel.setLayout(gbl);
	int mainY = 0;
    JGUIUtil.addComponent(diversionMainJPanel, diversionTopJPanel, 
    	0, 0, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// The following labels/choices indicate how to fill in the list of diversions
	// Multiple options are available and the list area is vertically parallel to the choices
	
	int divTopY = 0;
    
    JGUIUtil.addComponent(diversionTopJPanel, new JLabel("Ditch WDIDs for which to query parcels:"), 
    		2, divTopY, 4, 1, 1.0, 0.0, insetsTLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   
    ++divTopY;
    JGUIUtil.addComponent(diversionTopJPanel, new JLabel("Parcel year (to filter query):"), 
        	0, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
 	__diversionParcelYearJTextField = new JTextField ( 10 );
	__diversionParcelYearJTextField.setToolTipText("Year for parcel data, or blank for all.");
    JGUIUtil.addComponent(diversionTopJPanel, __diversionParcelYearJTextField, 
    		1, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    __diversionStationList_JTextArea = 	new JTextArea (4,40);
    __diversionStationList_JTextArea.setLineWrap ( true );
    __diversionStationList_JTextArea.setWrapStyleWord ( true );
    __diversionStationList_JTextArea.addKeyListener(this);
    JGUIUtil.addComponent(diversionTopJPanel, new JScrollPane(__diversionStationList_JTextArea), 
	2, divTopY, 4, 6, 1.0, 0.0, insetsTLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    ++divTopY;
    JGUIUtil.addComponent(diversionTopJPanel, new JLabel("Diversion stations:"), 
        0, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__diversionStation_JComboBox = new SimpleJComboBox ( 30, false );
	List<String> divList = StringUtil.sortStringList(StateMod_Util.createIdentifierList(diversionStationList, true));
	divList.add(0,"");
	__diversionStation_JComboBox.setData(divList);
	__diversionStation_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(diversionTopJPanel, __diversionStation_JComboBox, 
	1, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    ++divTopY;
    JGUIUtil.addComponent(diversionTopJPanel, new JLabel("Wells associated with ditches:"), 
		0, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    
	__wellsWithDitches_JComboBox = new SimpleJComboBox ( 30, false );
	List<String> wellList = StringUtil.sortStringList(StateMod_Util.createIdentifierList(wellsWithDitchStationList, true));
	wellList.add ( 0, "" );
	__wellsWithDitches_JComboBox.setData(wellList);
	__wellsWithDitches_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(diversionTopJPanel, __wellsWithDitches_JComboBox, 
	1, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    divTopY += 5;
	__divGetData_JButton = new SimpleJButton(__BUTTON_GET_DIVERSION_DATA, this);
	__divGetData_JButton.setToolTipText("Query HydroBase for ditch/parcel data.");
    JGUIUtil.addComponent(diversionTopJPanel, __divGetData_JButton, 
	5, divTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

    // JPanel for parcel results
    ++mainY;
    JPanel divParcelJPanel = new JPanel();
    divParcelJPanel.setLayout(new GridBagLayout());

	divParcelJPanel.setBorder(BorderFactory.createTitledBorder("Diversion(s) and Irrigated Parcels"));
	PropList p = new PropList("HydroBase_GUI_IrrigatedAcres.JWorksheet");
	p.add("JWorksheet.ShowRowHeader=true");
	p.add("JWorksheet.RowColumnPresent=true");
	p.add("JWorksheet.AllowCopy=true");	
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=ExcelSelection");

	int[] widths = null;
	JScrollWorksheet jsw = null;
	try {
		HydroBase_TableModel_IrrigatedAcresTool tm =
			new HydroBase_TableModel_IrrigatedAcresTool(new Vector<HydroBase_ParcelUseTSStructureToParcel>());
		HydroBase_CellRenderer cr = new HydroBase_CellRenderer(tm);
	
		jsw = new JScrollWorksheet(cr, tm, p);
		__divParcelWorksheet = jsw.getJWorksheet();

		widths = tm.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, p);
		__divParcelWorksheet = jsw.getJWorksheet();
	}
	__divParcelWorksheet.setPreferredScrollableViewportSize(null);
	__divParcelWorksheet.setHourglassJFrame(this);

    JGUIUtil.addComponent(divParcelJPanel, jsw,
    		0, 0, 1, 1, 1, 1, 
    		insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(diversionMainJPanel, divParcelJPanel, 
        	0, 1, 1, 1, 1.0, 1.0, insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    // Well data
    
    JPanel wellMainJPanel = new JPanel(); // Well query information and well/parcel results
    wellMainJPanel.setLayout(gbl);
    JGUIUtil.addComponent(mainJPanel, wellMainJPanel, 
        	0, 1, 1, 1, 1.0, 1.0, insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    // Panel to hold well/ditch data
	JPanel wellTopJPanel = new JPanel();
	wellTopJPanel.setBorder(BorderFactory.createTitledBorder(
		"Parcel(s) identified using parcel ID [pick from list or enter comma-separated values]"));
	wellTopJPanel.setLayout(gbl);
	mainY = 0;
    JGUIUtil.addComponent(wellMainJPanel, wellTopJPanel, 
    	0, mainY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	// The following labels/choices indicate how to fill in the list of wells
	// Multiple options are available and the list area is vertically parallel to the choices
	
	int wellTopY = 0;
	
    JGUIUtil.addComponent(wellTopJPanel, new JLabel("Division (required):"), 
        	0, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__wellDivisionJTextField = new JTextField ( 10 );
	__wellDivisionJTextField.addKeyListener(this);
	__wellDivisionJTextField.setToolTipText("Division for parcel data, required for parcel query.");
    JGUIUtil.addComponent(wellTopJPanel, __wellDivisionJTextField, 
    		1, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(wellTopJPanel, new JLabel("Parcel IDs to query for well data (unique for division, year):"), 
    		2, wellTopY, 4, 1, 1.0, 0.0, insetsTLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    ++wellTopY;
    JGUIUtil.addComponent(wellTopJPanel, new JLabel("Parcel year (required):"), 
        	0, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
 	__wellParcelYearJTextField = new JTextField ( 10 );
 	__wellParcelYearJTextField.addKeyListener(this);
 	__wellParcelYearJTextField.setToolTipText("Year corresponding to parcel IDs.");
    JGUIUtil.addComponent(wellTopJPanel, __wellParcelYearJTextField, 
    		1, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    __parcelList_JTextArea = new JTextArea (4,40);
    __parcelList_JTextArea.setLineWrap ( true );
    __parcelList_JTextArea.setWrapStyleWord ( true );
    __parcelList_JTextArea.addKeyListener(this);
    JGUIUtil.addComponent(wellTopJPanel, new JScrollPane(__parcelList_JTextArea), 
	2, wellTopY, 4, 6, 1.0, 0.0, insetsTLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    ++wellTopY;
    JGUIUtil.addComponent(wellTopJPanel, new JLabel(""), 
    	0, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST); 
	__useDiversionParcels_JButton = new SimpleJButton ( __BUTTON_USE_PARCELS_FROM_ABOVE, this );
	__useDiversionParcels_JButton.setToolTipText (
		"Use parcels from selected rows, or all rows if none are selected.  " +
		"Division and year must match specified values." );
    JGUIUtil.addComponent(wellTopJPanel, __useDiversionParcels_JButton, 
	1, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    ++wellTopY;
    JGUIUtil.addComponent(wellTopJPanel, new JLabel("Wells associated with parcels:"), 
		0, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    
	__wellsWithParcels_JComboBox = new SimpleJComboBox ( 30, false );
	List<String> wellList2 = StringUtil.sortStringList(StateMod_Util.createIdentifierList(wellsWithParcelStationList, true));
	wellList2.add ( 0, "" );
	__wellsWithParcels_JComboBox.setData(wellList2);
	__wellsWithParcels_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(wellTopJPanel, __wellsWithParcels_JComboBox, 
	1, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    wellTopY += 5;
	__parcelGetData_JButton = new SimpleJButton(__BUTTON_GET_PARCEL_DATA, this);
	__parcelGetData_JButton.setToolTipText("Query HydroBase for well/parcel data.");
    JGUIUtil.addComponent(wellTopJPanel, __parcelGetData_JButton, 
	5, wellTopY, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

    // JPanel for parcel results
    ++mainY;
    JPanel wellParcelJPanel = new JPanel();
    wellParcelJPanel.setLayout(new GridBagLayout());

	wellParcelJPanel.setBorder(BorderFactory.createTitledBorder("Parcels and associated wells"));
	PropList p2 = new PropList("HydroBase_GUI_IrrigatedAcres.JWorksheet");
	p2.add("JWorksheet.ShowRowHeader=true");
	p2.add("JWorksheet.RowColumnPresent=true");
	p2.add("JWorksheet.AllowCopy=true");	
	p2.add("JWorksheet.ShowPopupMenu=true");
	p2.add("JWorksheet.SelectionMode=ExcelSelection");

	int[] widths2 = null;
	JScrollWorksheet jsw2 = null;
	try {
		HydroBase_TableModel_Wells tm2 = new HydroBase_TableModel_Wells(new Vector<HydroBase_Wells>());
		HydroBase_CellRenderer cr = new HydroBase_CellRenderer(tm2);
	
		jsw2 = new JScrollWorksheet(cr, tm2, p2);
		__wellParcelWorksheet = jsw2.getJWorksheet();

		widths2 = tm2.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw2 = new JScrollWorksheet(0, 0, p2);
		__wellParcelWorksheet = jsw2.getJWorksheet();
	}
	__wellParcelWorksheet.setPreferredScrollableViewportSize(null);
	__wellParcelWorksheet.setHourglassJFrame(this);

    JGUIUtil.addComponent(wellParcelJPanel, jsw2,
    		0, 0, 1, 1, 1, 1, 
    		insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(wellMainJPanel, wellParcelJPanel, 
        	0, 1, 1, 1, 1.0, 1.0, insetsTLNR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    // South (bottom) JPanel for buttons and controls
    JPanel bottomJPanel = new JPanel();
    bottomJPanel.setLayout(new BorderLayout());
    getContentPane().add("South", bottomJPanel);

    // Bottom: South JPanel
    JPanel bottomSouthJPanel = new JPanel();
    bottomSouthJPanel.setLayout(new BorderLayout());
    bottomJPanel.add("South", bottomSouthJPanel);

    // Bottom: South: North JPanel
    JPanel bottomSouthNorthJPanel = new JPanel();
    bottomSouthNorthJPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    bottomSouthJPanel.add("North", bottomSouthNorthJPanel);

	SimpleJButton close = new SimpleJButton(__BUTTON_CLOSE, this);
	close.setToolTipText("Close the form.");
    bottomSouthNorthJPanel.add(close);

    // Bottom: South: South JPanel
    JPanel bottomSSJPanel = new JPanel();
    bottomSSJPanel.setLayout(gbl);
    bottomSouthJPanel.add("South", bottomSSJPanel);

    __statusJTextField = new JTextField();
    __statusJTextField.setEditable(false);
    JGUIUtil.addComponent(bottomSSJPanel, __statusJTextField, 
	0, 1, 10, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Check the GUI state to initialize appearance
    checkGUIState();
  
    // Frame settings
    setTitle("Irrigated Parcels and Ditch/Well Water Supply Data");
    pack();
	setSize(getWidth() + 350, getHeight() + 100);

    JGUIUtil.center(this);
    setVisible(true);

	if (widths != null) {
		__divParcelWorksheet.setColumnWidths(widths);
	}
	if (widths2 != null) {
		__wellParcelWorksheet.setColumnWidths(widths2);
	}
}

/**
Submits a query for the desired structure and displays the data in the GUI.
*/
private void submitDitchParcelQuery()
{	String routine = __CLASS + ".submitDitchParcelQuery";
	JGUIUtil.setWaitCursor(this, true);
	
	// Get the year for the query
	int parcelYear = getDiversionParcelYear();
	
	List<HydroBase_ParcelUseTSStructureToParcel> divParcel1List = null; // One WDID
	List<HydroBase_ParcelUseTSStructureToParcel> divParcelList = new Vector<HydroBase_ParcelUseTSStructureToParcel>(); // All WDID
	HydroBase_Structure struct = null;
	int wdidParts[];
	String text = __diversionStationList_JTextArea.getText().trim();
	if ( text.equals("") ) {
		return;
	}
	// Parse the text into the list of identifiers
	List<String> wdidList = StringUtil.breakStringList(text," ,\n", StringUtil.DELIM_SKIP_BLANKS );
	for ( String wdid : wdidList ) {
		try {
			// For each WDID in the ditch list, get the structure data and then the related parcel data (all years)...
			wdidParts = HydroBase_WaterDistrict.parseWDID(wdid);
			struct = __dmi.readStructureViewForWDID(wdidParts[0], wdidParts[1]);
			// StateDMI has two methods depending on what is available
			divParcel1List = __dmi.readParcelUseTSStructureToParcelListForStructure_numCal_year(
				struct.getStructure_num(), parcelYear );
			int size = 0;
			if ( divParcel1List != null ) {
				size = divParcel1List.size();
			}
			Message.printStatus ( 2, routine,
				"Got " + size + " parcels for ditch \"" + wdid + "\" struct_num=" + struct.getStructure_num() +
				" (before division check)." );
			// Add the list to the big list - could do in one step but need to set the structure WD, ID,
			// and name since not set during the query
			for ( HydroBase_ParcelUseTSStructureToParcel parcel : divParcel1List ) {
				parcel.setStructureWD ( wdidParts[0] );
				parcel.setStructureID( wdidParts[1] );
				parcel.setStructureName( struct.getStr_name() );
				divParcelList.add(parcel);
			}
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error getting parcel data for ditch \"" + wdid + "\" (" + e + ").");
			Message.printWarning(2, routine, e);
			// Continue to the next WDID...
		}
	}
	
	// Set the data in the worksheet...
	//__divParcelWorksheet.getTableModel().setNewData(divParcelList);
	//__divParcelWorksheet.invalidate(); // Cause redraw
	try {
		HydroBase_TableModel_IrrigatedAcresTool tm = new HydroBase_TableModel_IrrigatedAcresTool(divParcelList);
		HydroBase_CellRenderer cr = new HydroBase_CellRenderer(tm);
		__divParcelWorksheet.setCellRenderer(cr);
		__divParcelWorksheet.setModel(tm);
		__divParcelWorksheet.setColumnWidths(tm.getColumnWidths());
	}
	catch ( Exception e ) {
		Message.printWarning(2, routine, "Error displaying parcel data (" + e + ")." );
	}
	finally {
		JGUIUtil.setWaitCursor(this, false);
	}
}

/**
Submits a query for the desired parcel(s) and displays the data in the GUI.
*/
private void submitParcelWellQuery()
{	String routine = __CLASS + ".submitParcelWellQuery";
	JGUIUtil.setWaitCursor(this, true);
	
	// Get the year for the query
	int parcelYear = getWellParcelYear();
	int div = getWellDivision();
	if ( (div < 1) || (div > 7) ) {
		Message.printWarning ( 1, routine, "The division must be provided.", this );
		return;
	}
	
	List<HydroBase_Wells> wellParcel1List = null; // One parcel
	List<HydroBase_Wells> wellParcelList = new Vector<HydroBase_Wells>(); // All parcels
	String text = __parcelList_JTextArea.getText().trim();
	if ( text.equals("") ) {
		return;
	}
	// Parse the text into the list of identifiers
	List<String> idList = StringUtil.breakStringList(text," ,\n", StringUtil.DELIM_SKIP_BLANKS );
	for ( String id : idList ) {
		try {
			int idInt = Integer.parseInt(id);
			// For each ID in the parcel list, get the well/parcel data
			wellParcel1List = __dmi.readWellsWellToParcelList( idInt, parcelYear, div, true );
			// Add the list to the big list - could do in one step but need to set the structure WD, ID,
			// and name since not set during the query
			int size = 0;
			if ( wellParcel1List != null ) {
				size = wellParcel1List.size();
			}
			Message.printStatus ( 2, routine,
				"Got " + size + " parcel/well records for parcel \"" + idInt + "\"." );
			wellParcelList.addAll(wellParcel1List);
		}
		catch (Exception e) {
			Message.printWarning(3, routine, "Error getting parcel data for \"" + id + "\" (" + e + ").");
			Message.printWarning(3, routine, e);
			// Continue to the next WDID...
		}
	}
	
	// Set the data in the worksheet...
	try {
		HydroBase_TableModel_Wells tm = new HydroBase_TableModel_Wells(wellParcelList);
		HydroBase_CellRenderer cr = new HydroBase_CellRenderer(tm);
		__wellParcelWorksheet.setCellRenderer(cr);
		__wellParcelWorksheet.setModel(tm);
		__wellParcelWorksheet.setColumnWidths(tm.getColumnWidths());
	}
	catch ( Exception e ) {
		Message.printWarning(2, routine, "Error displaying parcel data (" + e + ")." );
	}
	finally {
		JGUIUtil.setWaitCursor(this, false);
	}
}

/**
Transfer the parcels from the diversion/parcel data to the well/parcel parcel ID list text area.
Filter by the division and parcel year if > 0.
*/
private void transferParcelsFromDiversions ( int div, int parcelYear )
{	@SuppressWarnings("unchecked")
	JWorksheet_AbstractTableModel<HydroBase_ParcelUseTSStructureToParcel> tm = __divParcelWorksheet.getTableModel();
	int [] selectedRows = __divParcelWorksheet.getSelectedRows();
	int rowCount = __divParcelWorksheet.getRowCount();
	StringBuffer b = new StringBuffer(); // to hold text list of parcel identifiers
	if ( selectedRows.length == 0 ) {
		// Process all rows.
		for ( int row = 0; row < rowCount; row++ ) {
			transferParcelFromDiversions ( tm, b, row, div, parcelYear );
		}
	}
	else {
		// Transfer the selected rows
		for ( int i = 0; i < selectedRows.length; i++ ) {
			transferParcelFromDiversions ( tm, b, selectedRows[i], div, parcelYear );
		}
	}
	__parcelList_JTextArea.setText(b.toString());
}

/**
Transfer a single parcel from the diversion/parcel data to the well/parcel parcel ID list text area.
Filter by the division and parcel year if > 0.
*/
private void transferParcelFromDiversions ( JWorksheet_AbstractTableModel<HydroBase_ParcelUseTSStructureToParcel> tm,
	StringBuffer b, int row, int div, int parcelYear )
{
	String tableYear = "" + tm.getValueAt(row, HydroBase_TableModel_IrrigatedAcresTool.COL_YEAR);
	String tableDiv = "" + tm.getValueAt(row, HydroBase_TableModel_IrrigatedAcresTool.COL_DIV);
	// If the division or year have been specified, only include the matching records.
	// Do string comparisons to avoid messy casting and other checks.
	if ( (div > 0) && !tableDiv.equals("" + div) ) {
		return;
	}
	if ( (parcelYear > 0) && !tableYear.equals("" + parcelYear) ) {
		return;
	}
	if ( b.length() > 0 ) {
		b.append(",");
	}
	b.append ( "" + tm.getValueAt(row, HydroBase_TableModel_IrrigatedAcresTool.COL_PARCEL_ID) );
}

public void windowActivated(WindowEvent e) {;}
public void windowClosed(WindowEvent e) {;}

/**
Closes the GUI.
@param e WindowEvent object
*/
public void windowClosing(WindowEvent e) {
	closeClicked();
}

public void windowDeactivated(WindowEvent e) {;}
public void windowDeiconified(WindowEvent e) {;}
public void windowIconified(WindowEvent e) {;}
public void windowOpened(WindowEvent e) {;}

}
