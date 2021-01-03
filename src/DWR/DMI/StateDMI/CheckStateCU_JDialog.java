// CheckStateCU_JDialog - Editor for Check*() commands, for StateCU data.

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

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Check*() commands, for StateCU data.
*/
@SuppressWarnings("serial")
public class CheckStateCU_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextField __ID_JTextField=null;
private JTextArea __command_JTextArea=null;
private JTextField __AreaPrecision_JTextField=null;
// TODO smalers 2020-11-07 not currently needed - enable if needed
//private SimpleJComboBox __DeepCheck_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private CheckStateCU_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CheckStateCU_JDialog ( JFrame parent, CheckStateCU_Command command )
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
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (true);
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList parameters = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	//String DeepCheck = __DeepCheck_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
    //if ( DeepCheck.length() > 0 ) {
    //    parameters.set ( "DeepCheck", DeepCheck );
    //}
	if ( __AreaPrecision_JTextField != null ) {
		String AreaPrecision = __AreaPrecision_JTextField.getText().trim();
		if ( AreaPrecision.length() > 0 ) {
			parameters.set ( "AreaPrecision", AreaPrecision );
		}
	}
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
	try {
		// This will warn the user...
		__command.checkCommandParameters ( parameters, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		Message.printWarning(2,"",e);
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String ID = __ID_JTextField.getText().trim();
	__command.setCommandParameter ( "ID", ID );
	//String DeepCheck = __DeepCheck_JComboBox.getSelected();
	//__command.setCommandParameter ( "DeepCheck", DeepCheck );
	if ( __AreaPrecision_JTextField != null ) {
		String AreaPrecision = __AreaPrecision_JTextField.getText().trim();
		__command.setCommandParameter ( "AreaPrecision", AreaPrecision );
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, CheckStateCU_Command command )
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
	String idLabel = "ID";
	String note = "identifiers";
	if ( __command instanceof CheckBlaneyCriddle_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU Blaney-Criddle crop coefficients."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
			"Currently no cross-checks are done with other StateCU components."),
	    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., day > 365)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Crop type (name):";
        note = "crops";
	}
	else if ( __command instanceof CheckPenmanMonteith_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU Penman-Monteith crop coefficients."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
			"Currently no cross-checks are done with other StateCU components."),
	    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., percent > 100)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Crop type (name):";
        note = "crops";
	}
	else if ( __command instanceof CheckClimateStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU climate station data."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"Currently no cross-checks are done with other StateCU components."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., latitude > 90 decrees)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Climate station identifier:";
        note = "climate stations";
	}
	else if ( __command instanceof CheckCropCharacteristics_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU crop characteristics."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
			"Currently no cross-checks are done with other StateCU components."),
	    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., month > 12)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Crop type (name):";
        note = "crops";
	}
	else if ( __command instanceof CheckCropPatternTS_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU crop pattern time series at CU locations."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"Currently no cross-checks are done with other StateCU components."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"Crop acreage in a year is each used to calculate the total acreage and fraction for crop - only" +
    		" the crop acreage and total are checked."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., negative acreage)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "CU location identifier:";
        note = "CU locations";
	}
	else if ( __command instanceof CheckCULocations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks CU Locations, generating warnings for the follow conditions:"),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   2) Invalid numerical values (e.g., negative climate station weights)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "CU location identifier:";
        note = "CU locations";
	}
	else if ( __command instanceof CheckIrrigationPracticeTS_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks StateCU irrigation practice time series at CU locations."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"The total acreage is cross-checked with the crop pattern time series total if available."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "Warnings are generated for the follow conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid numerical values (e.g., negative acreage)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Acreage parts do not add to the total."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   4) Irrigation practice total acreage and crop pattern total acreage do not agree."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "CU location identifier:";
        note = "CU locations";
	}
	else if ( __command instanceof CheckParcels_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks parcel data associate with model locations. Warnings are generated for the following conditions:"),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   1) Parcel has no surface water or groundwater supply for any year."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   2) Parcel is associated with more than one surface supply in a year and total of fractional areas is not equal to parcel area."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"      This indicates that not all HydroBase parcel supplies are included in the dataset, or there are errors in supply assignment."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   3) Parcel surface water supply irrigated area fraction computed from number of supplies does not match fraction from HydroBase."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"      This indicates that not all HydroBase parcel supplies are included in the dataset, or there are errors in supply assignment."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"   4) Check that parcels for only one division are being processed - otherwise there may be issues handling different snapshot years."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        //JGUIUtil.addComponent(paragraph, new JLabel ("   3) [Deep Check] Parcel ID is associated with more than one CU Location in a year."),
           	//0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "CU location identifier:";
        note = "CU locations for parcels";
	}
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel (idLabel),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the " + note + " to check (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __command instanceof CheckParcels_Command ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Area precision:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AreaPrecision_JTextField = new JTextField("",10);
		__AreaPrecision_JTextField.addKeyListener (this);
    	JGUIUtil.addComponent(main_JPanel, __AreaPrecision_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - precision for area comparisons (default=3)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    	/* TODO smalers 2020-11-07 currently not needed - enable later if necessary
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Deep check?:"),
				0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DeepCheck_JComboBox = new SimpleJComboBox(false);
    	List<String> DeepCheck_List = new ArrayList<>();
    	DeepCheck_List.add("");
		DeepCheck_List.add ( __command._False );
		DeepCheck_List.add ( __command._True );
		__DeepCheck_JComboBox.setData( DeepCheck_List );
		__DeepCheck_JComboBox.addItemListener (this);
    	JGUIUtil.addComponent(main_JPanel, __DeepCheck_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - do a deep check? (default=" + __command._False + ")."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    	 */
	}
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new ArrayList<>();
    IfNotFound_List.add("");
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	   	"Optional - indicate action if no match is found (default=" + __command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
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
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();
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
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = __command + ".refresh";
	String ID = "";
	//String DeepCheck = "";
	String AreaPrecision = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		//DeepCheck = props.getValue ( "DeepCheck" );
		AreaPrecision = props.getValue ( "AreaPrecision" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		/*
		if ( DeepCheck == null ) {
			// Select default...
			__DeepCheck_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DeepCheck_JComboBox, DeepCheck, JGUIUtil.NONE, null, null ) ) {
				__DeepCheck_JComboBox.select ( DeepCheck );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nDeepCheck value \""+
				DeepCheck + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		*/
		if ( __AreaPrecision_JTextField != null ) {
			if ( AreaPrecision != null ) {
				__AreaPrecision_JTextField.setText(AreaPrecision);
			}
		}
		if ( IfNotFound == null ) {
			// Select default...
			__IfNotFound_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IfNotFound_JComboBox, IfNotFound, JGUIUtil.NONE, null, null ) ) {
				__IfNotFound_JComboBox.select ( IfNotFound );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	// Always get the value that is selected...
	props = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	//DeepCheck = __DeepCheck_JComboBox.getSelected();
	props.add ( "ID=" + ID );
	if ( __AreaPrecision_JTextField != null ) {
		AreaPrecision = __AreaPrecision_JTextField.getText().trim();
		props.add ( "AreaPrecision=" + AreaPrecision );
	}
	//props.add ( "DeepCheck=" + DeepCheck );
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString ( props ) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed and the dialog is closed.
*/
public void response ( boolean ok ) {
	__ok = ok;
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response (false);
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}