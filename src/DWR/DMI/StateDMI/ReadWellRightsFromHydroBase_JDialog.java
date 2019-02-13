// ReadWellRightsFromHydroBase_JDialog - Editor for the ReadWellRightsFromHydroBase() command.

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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the ReadWellRightsFromHydroBase() command.
*/
public class ReadWellRightsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false; // Indicate whether OK has been pressed
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __Approach_JComboBox = null;
private JTextField __ID_JTextField=null;
private JTextField __PermitIDPattern_JTextField=null;
private JTextField __PermitIDPreFormat_JTextField=null;
private SimpleJComboBox __IDFormat_JComboBox = null;
private JTextField __PermitIDPostFormat_JTextField=null;
private JTextField __Year_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextField __DecreeMin_JTextField=null;
private JTextField __DefaultAppropriationDate_JTextField = null;
private SimpleJComboBox __DefineRightHow_JComboBox = null;
private SimpleJComboBox __ReadWellRights_JComboBox = null;
private SimpleJComboBox __UseApex_JComboBox = null;
private SimpleJComboBox __OnOffDefault_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJComboBox __Optimization_JComboBox = null;
private ReadWellRightsFromHydroBase_Command __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadWellRightsFromHydroBase_JDialog ( JFrame parent,	ReadWellRightsFromHydroBase_Command command )
{	super(parent, true);
	initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response ( true );
		}
	}
}

/**
Check the input.  Currently does nothing.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	String Approach = __Approach_JComboBox.getSelected();
	String ID = __ID_JTextField.getText().trim();
	String PermitIDPattern = __PermitIDPattern_JTextField.getText().trim();
	String PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	String Year = __Year_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	String DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	String ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	String UseApex = __UseApex_JComboBox.getSelected();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	String Optimization = __Optimization_JComboBox.getSelected();
	__error_wait = false;
	
	PropList props = new PropList ( "" );
	if ( Approach.length() > 0 ) {
		props.set ( "Approach", Approach );
	}
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( PermitIDPattern.length() > 0 ) {
		props.set ( "PermitIDPattern", PermitIDPattern );
	}
	if ( PermitIDPreFormat.length() > 0 ) {
		props.set ( "PermitIDPreFormat", PermitIDPreFormat );
	}
	if ( IDFormat.length() > 0 ) {
		props.set ( "IDFormat", IDFormat );
	}
	if ( PermitIDPostFormat.length() > 0 ) {
		props.set ( "PermitIDPostFormat", PermitIDPostFormat );
	}
	if ( Year.length() > 0 ) {
		props.set ( "Year", Year );
	}
	if ( Div.length() > 0 ) {
		props.set ( "Div", Div );
	}
	if (DecreeMin.length() > 0 ) {
		props.set("DecreeMin", DecreeMin);
	}
	if ( DefaultAppropriationDate.length() > 0 ) {
		props.set ( "DefaultAppropriationDate", DefaultAppropriationDate );
	}
	if ( DefineRightHow.length() > 0 ) {
		props.set ( "DefineRightHow", DefineRightHow );
	}
	if ( ReadWellRights.length() > 0 ) {
		props.set ( "ReadWellRights", ReadWellRights );
	}
	if ( UseApex.length() > 0 ) {
		props.set ( "UseApex", UseApex );
	}
	if ( OnOffDefault.length() > 0 ) {
		props.set ( "OnOffDefault", OnOffDefault );
	}
	if ( Optimization.length() > 0 ) {
		props.set ( "Optimization", Optimization );
	}
	try {
		// This will warn the user
		__command.checkCommandParameters( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning will have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{
	String Approach = __Approach_JComboBox.getSelected();
	String ID = __ID_JTextField.getText().trim();
	String PermitIDPattern = __PermitIDPattern_JTextField.getText().trim();
	String PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	String Year = __Year_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	String DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	String ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	String UseApex = __UseApex_JComboBox.getSelected();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	String Optimization = __Optimization_JComboBox.getSelected();

	__command.setCommandParameter ( "Approach", Approach );
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "PermitIDPattern", PermitIDPattern );
	__command.setCommandParameter ( "PermitIDPreFormat", PermitIDPreFormat);
	__command.setCommandParameter ( "IDFormat", IDFormat);
	__command.setCommandParameter ( "PermitIDPostFormat", PermitIDPostFormat);
	__command.setCommandParameter ( "Year", Year );
	__command.setCommandParameter ( "Div", Div );
	__command.setCommandParameter ( "DecreeMin", DecreeMin);
	__command.setCommandParameter ( "DefaultAppropriationDate", DefaultAppropriationDate );
	__command.setCommandParameter ( "DefineRightHow", DefineRightHow );
	__command.setCommandParameter ( "ReadWellRights", ReadWellRights );
	__command.setCommandParameter ( "UseApex", UseApex );
	__command.setCommandParameter ( "OnOffDefault", OnOffDefault );
	__command.setCommandParameter ( "Optimization", Optimization );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, ReadWellRightsFromHydroBase_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads well rights from HydroBase, using the well station identifiers to find rights."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Well stations can be explicit or a collection of wells (aggregate or system)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Water right data can be determined from well rights (identified by WDID)"
		+ " or permits (identified by receipt number)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<html><b>A new simple approach is now the default - see the General tab below.</b></html>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for general parameters
    int yGeneral = -1;
    JPanel general_JPanel = new JPanel();
    general_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "General", general_JPanel );
    
   	JGUIUtil.addComponent(general_JPanel, new JLabel (
		"Well right processing using HydroBase can be complex due to right/permit/parcel/ditch relationships."),
		0, ++yGeneral, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(general_JPanel, new JLabel (
		"However, a Simple approach is now the default due to improvements in data and changes in modeling approach."),
		0, ++yGeneral, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(general_JPanel, new JLabel (
		"The older approach is referred to as Legacy.  Parameters are documented as applying to Simple, Legacy, or both approaches."),
		0, ++yGeneral, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(general_JPanel, new JLabel (
		"If the well rights are to be aggregated into water right classes, use the AggregateWellRights() command to reduce" +
		" the number (but retain decree sum) of rights in the model."),
		0, ++yGeneral, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(general_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++yGeneral, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
   	JGUIUtil.addComponent(general_JPanel, new JLabel ("Approach:"),
		0, ++yGeneral, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> approachList = new ArrayList<String>(3);
   	approachList.add ( "" );
   	approachList.add ( __command._Legacy );
   	approachList.add ( __command._Simple );
	__Approach_JComboBox = new SimpleJComboBox(false);
	__Approach_JComboBox.setData ( approachList );
	__Approach_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(general_JPanel, __Approach_JComboBox,
		1, yGeneral, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(general_JPanel, new JLabel (
		"Optional - approach to process well rights (default="+__command._Simple + ".)."),
		3, yGeneral, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
   	JGUIUtil.addComponent(general_JPanel, new JLabel ( "Optimization level:"),
		0, ++yGeneral, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List optimizationList = new ArrayList<String>(3);
	optimizationList.add ( "" );
	optimizationList.add ( __command._UseLessMemory );
	optimizationList.add ( __command._UseMoreMemory );
	__Optimization_JComboBox = new SimpleJComboBox(false);
	__Optimization_JComboBox.setData ( optimizationList );
	__Optimization_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(general_JPanel, __Optimization_JComboBox,
		1, yGeneral, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(general_JPanel, new JLabel (
		"Optional - optimize performance (default=" + __command._UseMoreMemory + ")."),
		3, yGeneral, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     
    // Panel for well station filter
    int yWell = -1;
    JPanel well_JPanel = new JPanel();
    well_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Well Stations", well_JPanel );
    
   	JGUIUtil.addComponent(well_JPanel, new JLabel (
		"Specify the station ID pattern to limit the wells that are processed."),
		0, ++yWell, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(well_JPanel, new JLabel (
		"<html>Specify the permit ID pattern to help indicate explicit wells that are permits - <b>used with Simple approach</b>.</html>"),
		0, ++yWell, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(well_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yWell, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(well_JPanel, new JLabel ( "Well station ID:"),
			0, ++yWell, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(well_JPanel, __ID_JTextField,
		1, yWell, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(well_JPanel, new JLabel (
		"Optional - well stations to read (use * for wildcard, default=*)."),
		3, yWell, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
	JGUIUtil.addComponent(well_JPanel, new JLabel ( "Well permit ID pattern:"),
			0, ++yWell, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PermitIDPattern_JTextField = new JTextField(10);
	__PermitIDPattern_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(well_JPanel, __PermitIDPattern_JTextField,
		1, yWell, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(well_JPanel, new JLabel (
		"Optional - pattern to indicate explicit well stations that are permit (use * for wildcard such as P:*)."),
		3, yWell, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    // Panel for explicit WDID
    int yExplicitWDID = -1;
    JPanel explicitWDID_JPanel = new JPanel();
    explicitWDID_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Explicit WDID", explicitWDID_JPanel );
   	
   	JGUIUtil.addComponent(explicitWDID_JPanel, new JLabel (
		"Well rights output by StateDMI can originate from structure well rights with WDID, or permits with receipt ID."),
		0, ++yExplicitWDID, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(explicitWDID_JPanel, new JLabel (
		"The following parameter indicates that when explicit wells are processed (not aggregate or system), the rights should be read directly "
		+ "using WDID rather than processing well/parcel data."),
		0, ++yExplicitWDID, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(explicitWDID_JPanel, new JLabel (
		"<html><b>This parameter is only used with Legacy approach and is ignored for Simple approach.</b></html>"),
		0, ++yExplicitWDID, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(explicitWDID_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yExplicitWDID, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
   	JGUIUtil.addComponent(explicitWDID_JPanel, new JLabel ("Read well rights?:"),
		0, ++yExplicitWDID, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List read_Vector = new Vector(3);
   	read_Vector.add ( "" );
	read_Vector.add ( __command._True );
	read_Vector.add ( __command._False );
	__ReadWellRights_JComboBox = new SimpleJComboBox(false);
	__ReadWellRights_JComboBox.setData ( read_Vector );
	__ReadWellRights_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(explicitWDID_JPanel, __ReadWellRights_JComboBox,
		1, yExplicitWDID, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(explicitWDID_JPanel, new JLabel (
		"Optional - read well rights rather than relying on well matching results (default=" +
		__command._True + ")."),
		3, yExplicitWDID, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    // Panel for well/parcel data
    int yWellParcel = -1;
    JPanel wellParcel_JPanel = new JPanel();
    wellParcel_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Well/Parcel", wellParcel_JPanel );
   	
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"Wells in HydroBase are associated with parcels using irrigated lands assessment spatial data layers as input."),
		0, ++yWellParcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"The well/parcel relationships require division and year as input to match HydroBase data."),
		0, ++yWellParcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"<html><b>Note that more recent versions of HydroBase use parcel identifiers that include water district (older did not).</b></html>"),
		0, ++yWellParcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"<html><b>These parameters are only used with Legacy approach and are ignored for Simple approach.</b></html>"),
		0, ++yWellParcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(wellParcel_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yWellParcel, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(wellParcel_JPanel, new JLabel ( "Water Division (Div):"),
		0, ++yWellParcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(wellParcel_JPanel, __Div_JTextField,
		1, yWellParcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"Required - water division for the parcels."),
		3, yWellParcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(wellParcel_JPanel, new JLabel ( "Year:"),
    	0, ++yWellParcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	__Year_JTextField = new JTextField(30);
	__Year_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(wellParcel_JPanel, __Year_JTextField,
		1, yWellParcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(wellParcel_JPanel, new JLabel (
		"Optional - year(s) for the parcels, separated by commas (default=all available)."),
		3, yWellParcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
   	// Panel for right/permit data
    int yRightPermit = -1;
    JPanel rightPermit_JPanel = new JPanel();
    rightPermit_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Well Right/Permit", rightPermit_JPanel );

    JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"Water rights are determined from cross-referenced well right and permit data, which have been matched with wells and parcels."),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"Cross-referenced data can be used as is, or well rights can be requeried to obtain individual net amount rights."),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"The following parameters indicate how to decide whether to use well right or permit when well/parcel data are used."),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"This is necessary because well rights (using WDID and decree) are matched with well permit (using receipt ID and yield) in HydroBase."),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"Water rights are desirable but are not always available."),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"<html><b>The DefineRightHow parameter is only used with Legacy approach and is ignored for Simple approach (right is always used if available).</b></html>"),
		0, ++yRightPermit, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(rightPermit_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yRightPermit, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel ("Define right how?:"),
		0, ++yRightPermit, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List define_Vector = new Vector(4);
   	define_Vector.add ( "" );
	define_Vector.add ( "" + DefineWellRightHowType.EARLIEST_DATE );
	define_Vector.add ( "" + DefineWellRightHowType.LATEST_DATE );
	define_Vector.add ( "" + DefineWellRightHowType.RIGHT_IF_AVAILABLE );
	__DefineRightHow_JComboBox = new SimpleJComboBox(false);
	__DefineRightHow_JComboBox.setData ( define_Vector );
	__DefineRightHow_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(rightPermit_JPanel, __DefineRightHow_JComboBox,
		1, yRightPermit, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"Optional - how to define right from HydroBase right/permit (default=" + DefineWellRightHowType.EARLIEST_DATE + ")."),
		3, yRightPermit, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(rightPermit_JPanel, new JLabel ( "Default appropriation date:"),
		0, ++yRightPermit, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DefaultAppropriationDate_JTextField = new JTextField("",10);
	__DefaultAppropriationDate_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(rightPermit_JPanel, __DefaultAppropriationDate_JTextField,
		1, yRightPermit, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(rightPermit_JPanel, new JLabel (
		"Optional - use if date is not available from right or permit (default=99999.99999 administration number as date)."),
		3, yRightPermit, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    // Panel for decree value
    int yDecree = -1;
    JPanel decree_JPanel = new JPanel();
    decree_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Decree Value", decree_JPanel );
   	
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"The decree value for water rights is taken from water right decree (explicit WDID) or well yield (if processing well/parcel data)."),
		0, ++yDecree, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"The cross-referenced well/parcel/right/permit data represent yield in gallons per minute (GPM)."),
		0, ++yDecree, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"Additionally, an alternate point/exchange (APEX) water right decree may be found with a decree or separate from a water right decree."),
		0, ++yDecree, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"The APEX (Alternate Point/Exchange) amount can optionally be added to the decree - refer to modeling guidelines."),
		0, ++yDecree, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"If APEX is used and a decree has an APEX amount (whether absolute or conditional right), then the decree is (net_abs+APEX)."),
		0, ++yDecree, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(decree_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yDecree, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(decree_JPanel, new JLabel ("Decree minimum:"),
		0, ++yDecree, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DecreeMin_JTextField = new JTextField(10);
	__DecreeMin_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(decree_JPanel, __DecreeMin_JTextField,
		1, yDecree, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"Optional - minimum decree to include (default = .0005 CFS)."),
		3, yDecree, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
   	JGUIUtil.addComponent(decree_JPanel, new JLabel ( "Use APEX?:"),
		0, ++yDecree, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List apex_Vector = new Vector(3);
   	apex_Vector.add ( "" );
	apex_Vector.add ( __command._True );
	apex_Vector.add ( __command._False );
	__UseApex_JComboBox = new SimpleJComboBox(false);
	__UseApex_JComboBox.setData ( apex_Vector );
	__UseApex_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(decree_JPanel, __UseApex_JComboBox,
		1, yDecree, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(decree_JPanel, new JLabel (
		"Optional - add APEX amount to right amount (default=False)."),
		3, yDecree, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    // Panel for right ID format
    int yId = -1;
    JPanel id_JPanel = new JPanel();
    id_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Right ID Format", id_JPanel );
   	
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"The water right ID in modeling (StateMod) is restricted to 12 characters."),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"HydroBase identifiers may be a water right WDID (7 digits) or permit receipt (8 digits and possibly characters)."),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"Because WDIDs and permit receipts may have the same numeric values, it may be necessary to add characters to identifiers."),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"The following parameters support flexibility in formatting well right identifiers to facilitate modeling."),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"If the rights will later be processed with MergeWellRights(), format the ID as:  " + __command._HydroBaseID ),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"If the rights will not be processed with MergeWellRights() or AggregateWellRights(), then using a format with station ID and NN or NNN count." ),
		0, ++yId, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(id_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yId, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(id_JPanel, new JLabel ( "Permit ID (pre)format (currently ignored):"),
		0, ++yId, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PermitIDPreFormat_JTextField = new JTextField("",10);
	__PermitIDPreFormat_JTextField.setToolTipText("Use %s to pass the receipt ID through, or for example %s:P for permit ID");
	__PermitIDPreFormat_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(id_JPanel, __PermitIDPreFormat_JTextField,
		1, yId, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"Optional - format for permit receipt BEFORE Right ID format (default= %s:P =legacy format)."),
		3, yId, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(id_JPanel, new JLabel ( "Right ID format:"),
		0, ++yId, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List formatChoices = new ArrayList<String>(6);
	formatChoices.add ( "" );
	formatChoices.add ( __command._HydroBaseID );
	formatChoices.add ( __command._StationIDW_NN );
	formatChoices.add ( __command._StationIDWNN );
	formatChoices.add ( __command._StationID_NN );
	formatChoices.add ( __command._StationIDNN );
	formatChoices.add ( __command._StationIDW_NNN );
	formatChoices.add ( __command._StationIDWNNN );
	formatChoices.add ( __command._StationID_NNN );
	formatChoices.add ( __command._StationIDNNN );
	formatChoices.add ( __command._StationIDW_AutoN );
	formatChoices.add ( __command._StationIDWAutoN );
	formatChoices.add ( __command._StationID_AutoN );
	formatChoices.add ( __command._StationIDAutoN );
	__IDFormat_JComboBox = new SimpleJComboBox(false);
	__IDFormat_JComboBox.setData ( formatChoices );
	__IDFormat_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(id_JPanel, __IDFormat_JComboBox,
		1, yId, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(id_JPanel, new JLabel (
		"Optional - format for right identifiers (default=" + __command._StationIDW_NN + ")."),
		3, yId, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(id_JPanel, new JLabel ( "Permit ID (post)format (currently ignored):"),
		0, ++yId, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
 	__PermitIDPostFormat_JTextField = new JTextField("",10);
	__PermitIDPostFormat_JTextField.setToolTipText("Use %s to pass the receipt ID through, or for example %s:P for legacy permit ID");
	__PermitIDPostFormat_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(id_JPanel, __PermitIDPostFormat_JTextField,
		1, yId, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(id_JPanel, new JLabel (
		"Optional - format for permit receipt AFTER Right ID format (default=%s=pass-through)."),
		3, yId, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    // Panel for on/off
    int yOnOff = -1;
    JPanel onOff_JPanel = new JPanel();
    onOff_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "StateMod On/Off", onOff_JPanel );
    
   	JGUIUtil.addComponent(onOff_JPanel, new JLabel (
		"StateMod well rights can be turned on or off in a year or use 1 to be on for the full period." ),
		0, ++yOnOff, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(onOff_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yOnOff, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(onOff_JPanel, new JLabel ( "OnOff default:"),
		0, ++yOnOff, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List onoffList = new ArrayList<String>(3);
	onoffList.add ( "" );
	onoffList.add ( __command._1 );
	onoffList.add ( __command._AppropriationDate );
	__OnOffDefault_JComboBox = new SimpleJComboBox(false);
	__OnOffDefault_JComboBox.setData ( onoffList );
	__OnOffDefault_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(onOff_JPanel, __OnOffDefault_JComboBox,
		1, yOnOff, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(onOff_JPanel, new JLabel (
		"Optional - default StateMod OnOff switch (default=" + __command._AppropriationDate + ")."),
		3, yOnOff, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (6,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
			
	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
    super.setVisible(true);
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e) {
	refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed (KeyEvent event) {
	int code = event.getKeyCode();

	if (code == KeyEvent.VK_ENTER) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (true);
		}
	}
	else {	// One of the combo boxes...
		refresh();
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = __command.getCommandName() + "_JDialog.refresh";
	String Approach = "";
	String ID = "";
	String PermitIDPattern = "";
	String PermitIDPreFormat = "";
	String IDFormat = "";
	String PermitIDPostFormat = "";
	String Year = "";
	String Div = "";
	String DecreeMin = "";
	String DefaultAppropriationDate = "";
	String DefineRightHow = "";
	String ReadWellRights = "";
	String UseApex = "";
	String OnOffDefault = "";
	String Optimization = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		Approach = props.getValue ( "Approach" );
		ID = props.getValue ( "ID" );
		PermitIDPattern = props.getValue ( "PermitIDPattern" );
		PermitIDPreFormat = props.getValue ( "PermitIDPreFormat" );
		IDFormat = props.getValue ( "IDFormat" );
		PermitIDPostFormat = props.getValue ( "PermitIDPostFormat" );
		Div = props.getValue ( "Div" );
		Year = props.getValue ( "Year" );
		if ( DecreeMin != null ) {
			__DecreeMin_JTextField.setText(DecreeMin);
		}
		DecreeMin = props.getValue ( "DecreeMin" );
		DefaultAppropriationDate = props.getValue ( "DefaultAppropriationDate" );
		DefineRightHow = props.getValue ( "DefineRightHow" );
		ReadWellRights = props.getValue ( "ReadWellRights" );
		UseApex = props.getValue ( "UseApex" );
		OnOffDefault = props.getValue ( "OnOffDefault" );
		Optimization = props.getValue ( "Optimization" );
		if ( Approach == null ) {
			// Select default...
			__Approach_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Approach_JComboBox, Approach, JGUIUtil.NONE, null, null ) ) {
				__Approach_JComboBox.select ( Approach );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid Approach value \"" +
					Approach + "\".  Select a different value or Cancel.");
			}
		}
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( PermitIDPreFormat != null ) {
			__PermitIDPreFormat_JTextField.setText(PermitIDPreFormat);
		}
		if ( IDFormat == null ) {
			// Select default...
			__IDFormat_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDFormat_JComboBox, IDFormat, JGUIUtil.NONE, null, null ) ) {
				__IDFormat_JComboBox.select ( IDFormat );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid IDFormat value \"" +
				IDFormat + "\".  Select a different value or Cancel.");
			}
		}
		if ( PermitIDPostFormat != null ) {
			__PermitIDPostFormat_JTextField.setText(PermitIDPostFormat);
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( DefaultAppropriationDate != null ) {
			__DefaultAppropriationDate_JTextField.setText( DefaultAppropriationDate);
		}
		if ( DefineRightHow == null ) {
			// Select default (True)...
			__DefineRightHow_JComboBox.select ( __command._True );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DefineRightHow_JComboBox, DefineRightHow, JGUIUtil.NONE, null, null ) ) {
				__DefineRightHow_JComboBox.select ( DefineRightHow );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid DefineRightHow " +
				"value \"" + DefineRightHow + "\".  Select a different value or Cancel.");
			}
		}
		if ( ReadWellRights == null ) {
			// Select default...
			__ReadWellRights_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__ReadWellRights_JComboBox, ReadWellRights, JGUIUtil.NONE, null, null ) ) {
				__ReadWellRights_JComboBox.select ( ReadWellRights );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid ReadWellRights " +
				"value \"" + ReadWellRights + "\".  Select a different value or Cancel.");
			}
		}
		if ( UseApex == null ) {
			// Select default...
			__UseApex_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__UseApex_JComboBox, UseApex, JGUIUtil.NONE, null, null ) ) {
				__UseApex_JComboBox.select ( UseApex );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid UseApex " +
				"value \"" + UseApex + "\".  Select a different value or Cancel.");
			}
		}
		if ( OnOffDefault == null ) {
			// Select default...
			__OnOffDefault_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OnOffDefault_JComboBox, OnOffDefault, JGUIUtil.NONE, null, null ) ) {
				__OnOffDefault_JComboBox.select ( OnOffDefault );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid OnOffDefault " +
				"value \"" + OnOffDefault + "\".  Select a different value or Cancel.");
			}
		}
		if ( Optimization == null ) {
			// Select default...
			__Optimization_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Optimization_JComboBox, Optimization, JGUIUtil.NONE, null, null ) ) {
				__Optimization_JComboBox.select ( Optimization );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid Optimization " +
				"value \"" + Optimization + "\".  Select a different value or Cancel.");
			}
		}
	}

	// Always get the value that is selected...

	Approach = __Approach_JComboBox.getSelected();
	ID = __ID_JTextField.getText().trim();
	PermitIDPattern = __PermitIDPattern_JTextField.getText().trim();
	PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	IDFormat = __IDFormat_JComboBox.getSelected();
	PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
	}
	DecreeMin = __DecreeMin_JTextField.getText().trim();
	DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	if ( __DefineRightHow_JComboBox != null ) {
		DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	}
	if ( __ReadWellRights_JComboBox != null ) {
		ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	}
	if ( __UseApex_JComboBox != null ) {
		UseApex = __UseApex_JComboBox.getSelected();
	}
	if ( __OnOffDefault_JComboBox != null ) {
		OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	}
	if ( __Optimization_JComboBox != null ) {
		Optimization = __Optimization_JComboBox.getSelected();
	}
	props = new PropList ( "");
	props.add ( "Approach=" + Approach );
	props.add ( "ID=" + ID );
	props.add ( "PermitIDPattern=" + PermitIDPattern );
	props.add ( "PermitIDPreFormat=" + PermitIDPreFormat);
	props.add ( "IDFormat=" + IDFormat);
	props.add ( "PermitIDPostFormat=" + PermitIDPostFormat);
	props.add ( "Year=" + Year );
	props.add ( "Div=" + Div );
	props.add ( "DecreeMin=" + DecreeMin );
	props.add ( "DefaultAppropriationDate=" + DefaultAppropriationDate );
	props.add ( "DefineRightHow=" + DefineRightHow );
	props.add ( "ReadWellRights=" + ReadWellRights );
	props.add ( "UseApex=" + UseApex );
	props.add ( "OnOffDefault=" + OnOffDefault );
	props.add ( "Optimization=" + Optimization );
	__command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	
	__ok = ok;	// Save to be returned by ok()
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			Message.printStatus(2, "", "error_wait=true");
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
