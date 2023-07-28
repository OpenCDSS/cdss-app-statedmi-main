// SetCropPatternTS_JDialog - Editor for SetCropPatternTS() command.

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2023 Colorado Department of Natural Resources

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
import java.util.ArrayList;
import java.util.List;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for SetCropPatternTS() command.
*/
@SuppressWarnings("serial")
public class SetCropPatternTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __SetStart_JTextField = null;
private JTextField __SetEnd_JTextField = null;
private SimpleJComboBox __ProcessWhen_JComboBox = null;
private JTextField __CropPattern_JTextField = null;
private SimpleJComboBox __IrrigationMethod_JComboBox = null;
private SimpleJComboBox __SupplyType_JComboBox = null; // Only enabled if enableStateDMIPre050000=true.
private SimpleJComboBox __SetToMissing_JComboBox = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SetCropPatternTS_Command __command = null;
private boolean __ok = false;
private boolean enableStateDMIPre050000 = true; // Set to true for features < StateDMI 5.00.00.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCropPatternTS_JDialog (JFrame parent, SetCropPatternTS_Command command) {
	super(parent, true);
	initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
}

/**
Check the input.
If errors exist, warn the user and set the __error_wait flag to true.
This should be called before response() is allowed to complete.
*/
private void checkInput () {
	// Put together a list of parameters to check.
	PropList parameters = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String CropPattern = __CropPattern_JTextField.getText().trim();
	String IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	String SetToMissing = __SetToMissing_JComboBox.getSelected();
	String ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;

	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( SetStart.length() > 0 ) {
		parameters.set ( "SetStart", SetStart );
	}
	if ( SetEnd.length() > 0 ) {
		parameters.set ( "SetEnd", SetEnd );
	}
	if ( CropPattern.length() > 0 ) {
		parameters.set ( "CropPattern", CropPattern );
	}
	if ( IrrigationMethod.length() > 0 ) {
		parameters.set ( "IrrigationMethod", IrrigationMethod );
	}
	if ( this.enableStateDMIPre050000 ) {
		String SupplyType = __SupplyType_JComboBox.getSelected();
		if ( SupplyType.length() > 0 ) {
			parameters.set ( "SupplyType", SupplyType );
		}
	}
	if ( SetToMissing.length() > 0 ) {
		parameters.set ( "SetToMissing", SetToMissing );
	}
	if ( ProcessWhen.length() > 0 ) {
		parameters.set ( "ProcessWhen", ProcessWhen );
	}
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
	try {
		// This will warn the user.
		__command.checkCommandParameters ( parameters, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		Message.printWarning(2,"",e);
		__error_wait = true;
	}
}

/**
Commit the edits to the command.
In this case the command parameters have already been checked and no errors were detected.
*/
private void commitEdits () {
	String ID = __ID_JTextField.getText().trim();
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String CropPattern = __CropPattern_JTextField.getText().trim();
	String IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	String SetToMissing = __SetToMissing_JComboBox.getSelected();
	String ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "SetStart", SetStart );
	__command.setCommandParameter ( "SetEnd", SetEnd );
	__command.setCommandParameter ( "CropPattern", CropPattern );
	__command.setCommandParameter ( "IrrigationMethod", IrrigationMethod );
	if ( this.enableStateDMIPre050000 ) {
		String SupplyType = __SupplyType_JComboBox.getSelected();
		__command.setCommandParameter ( "SupplyType", SupplyType );
	}
	__command.setCommandParameter ( "SetToMissing", SetToMissing );
	__command.setCommandParameter ( "ProcessWhen", ProcessWhen );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command List of String containing the command.
*/
private void initialize (JFrame parent, SetCropPatternTS_Command command ) {
	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel.

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
   	JGUIUtil.addComponent(paragraph, new JLabel (
   		"This command sets (edits) crop pattern time series data using the CU Location ID to look up the location."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Crop patterns should be specified separated by commans using the format:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"     Crop1,Area1,Crop2,Area2"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"For example:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"     ALFALFA,300,POTATOES,150"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel ("If ProcessWhen=Now (the default):"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Previous crop patterns for matching CU " +
		"locations (created with CreateCropPatternTSForCULocations and other commands) are reset when the command is processed."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Acreage for other crops at the location and date (e.g., " +
		"from other commands) will be set to zero."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Therefore this command completely defines the crop " +
		"pattern for a location at a point in time."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Used in this way, the command is usually used AFTER " +
		"commands that process crop patterns from parcels,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    and changes are made to final CU Locations (not parts" +
		" in an aggregate/system)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel ("<html>If ProcessWhen=WithParcels (<b>This is being phased out - if possible, do not use</b>):"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    The crop patterns are processed by ReadCropPatternTSFromHydroBase() command as additional irrigated parcels data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Each crop/area/year triplet is treated as if it were" +
		" determined from parcels, to supplement later processing."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    Used in this way, the commands should be used BEFORE" +
		" commands that process crop patterns from parcels,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    and data can be defined for parts of an aggregate/system."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel ("<html>Also see the newer SetParcel*() commands.</html>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - the CU location(s) to fill (use * for wildcard)"),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set start (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetStart_JTextField = new JTextField (10);
	__SetStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - starting year to set data (blank for full period)."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set end (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetEnd_JTextField = new JTextField (10);
	__SetEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetEnd_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - ending year to set data (blank for full period)."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Crop pattern:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropPattern_JTextField = new JTextField (50);
	__CropPattern_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CropPattern_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set to missing:"),
   		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> SetToMissingList = new ArrayList<>(3);
    SetToMissingList.add ( "" );
    SetToMissingList.add ( __command._True );
    SetToMissingList.add ( __command._False );
    __SetToMissing_JComboBox = new SimpleJComboBox(false);
    __SetToMissing_JComboBox.setData ( SetToMissingList );
    __SetToMissing_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetToMissing_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - set data to missing (no crops) - can then be filled."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Process when:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> processWhenList = new ArrayList<>(3);
	processWhenList.add ( "" );
	processWhenList.add ( __command._Now );
	processWhenList.add ( __command._WithParcels );
	__ProcessWhen_JComboBox = new SimpleJComboBox(false);
	__ProcessWhen_JComboBox.setData ( processWhenList );
	__ProcessWhen_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ProcessWhen_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate when to process the data (default=" + __command._Now + ")."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new ArrayList<>(4);
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

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The following are used with StateDMI < 5.x.  Irrigation data are used when processing irrigation practice time series."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"For StateDMI >= 5.x, the SetParcel*() commands can be used."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Irrigation method:"),
       		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IrrigationMethodList = new ArrayList<>(3);
    IrrigationMethodList.add ( "" );
    IrrigationMethodList.add ( __command._Flood );
    IrrigationMethodList.add ( __command._Sprinkler );
    __IrrigationMethod_JComboBox = new SimpleJComboBox(false);
    __IrrigationMethod_JComboBox.setData ( IrrigationMethodList );
    __IrrigationMethod_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IrrigationMethod_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Used with StateDMI < 5.x - irrigation method for crops."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    if ( this.enableStateDMIPre050000 ) {
    	// TODO smalers 2021-01-18 not needed for crop pattern TS.
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Supply type:"),
       		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	List<String> SupplyTypeList = new ArrayList<>(3);
    	SupplyTypeList.add ( "" );
    	SupplyTypeList.add ( __command._Ground );
    	SupplyTypeList.add ( __command._Surface );
    	__SupplyType_JComboBox = new SimpleJComboBox(false);
    	__SupplyType_JComboBox.setToolTipText("Used with StateDMI < 5.x - see command documentation.");
    	__SupplyType_JComboBox.setData ( SupplyTypeList );
    	__SupplyType_JComboBox.addItemListener (this);
    	JGUIUtil.addComponent(main_JPanel, __SupplyType_JComboBox,
    		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
        	"Used with StateDMI < 5.x - supply type for crops."),
        	3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Panel for buttons.
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
	// JDialogs do not need to be resizable.
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
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {
}

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
private void refresh () {
	__error_wait = false;
	String routine = getClass().getSimpleName() + ".refresh";
	String ID = "";
	String SetStart = "";
	String SetEnd = "";
	String CropPattern = "";
	String IrrigationMethod = "";
	String SupplyType = "";
	String SetToMissing = "";
	String ProcessWhen = "";
	String IfNotFound = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command.
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		SetStart = props.getValue ( "SetStart" );
		SetEnd = props.getValue ( "SetEnd" );
		CropPattern = props.getValue ( "CropPattern" );
		IrrigationMethod = props.getValue ( "IrrigationMethod" );
		SupplyType = props.getValue ( "SupplyType" );
		SetToMissing = props.getValue ( "SetToMissing" );
		ProcessWhen = props.getValue ( "ProcessWhen" );
		IfNotFound = props.getValue ( "IfNotFound" );
		// Display existing content.
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( SetStart != null ) {
			__SetStart_JTextField.setText(SetStart);
		}
		if ( SetEnd != null ) {
			__SetEnd_JTextField.setText(SetEnd);
		}
		if ( CropPattern != null ) {
			__CropPattern_JTextField.setText(CropPattern);
		}
		if ( IrrigationMethod == null ) {
			// Select default.
			__IrrigationMethod_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IrrigationMethod_JComboBox, IrrigationMethod, JGUIUtil.NONE, null, null ) ) {
				__IrrigationMethod_JComboBox.select ( IrrigationMethod );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIrrigationMethod value \"" +
				IrrigationMethod + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( this.enableStateDMIPre050000 ) {
			if ( SupplyType == null ) {
				// Select default.
				__SupplyType_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__SupplyType_JComboBox, SupplyType, JGUIUtil.NONE, null, null ) ) {
					__SupplyType_JComboBox.select ( SupplyType );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nSupplyType value \"" +
					SupplyType + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( SetToMissing == null ) {
			// Select default.
			__SetToMissing_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__SetToMissing_JComboBox, SetToMissing, JGUIUtil.NONE, null, null ) ) {
				__SetToMissing_JComboBox.select ( SetToMissing );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nSetToMissing value \"" +
				SetToMissing + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ProcessWhen == null ) {
			// Select default.
			__ProcessWhen_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__ProcessWhen_JComboBox, ProcessWhen, JGUIUtil.NONE, null, null ) ) {
				__ProcessWhen_JComboBox.select ( ProcessWhen );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nProcessWhen value \"" +
				ProcessWhen + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IfNotFound == null ) {
			// Select default.
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

	ID = __ID_JTextField.getText().trim();
	SetStart = __SetStart_JTextField.getText().trim();
	SetEnd = __SetEnd_JTextField.getText().trim();
	CropPattern = __CropPattern_JTextField.getText().trim();
	IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	SetToMissing = __SetToMissing_JComboBox.getSelected();
	ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();

	props = new PropList(__command.getCommandName());
	props.add("ID=" + ID);
	props.add("SetStart=" + SetStart);
	props.add("SetEnd=" + SetEnd);
	props.add("CropPattern=" + CropPattern);
	props.add("IrrigationMethod=" + IrrigationMethod);
	if ( this.enableStateDMIPre050000 ) {
		SupplyType = __SupplyType_JComboBox.getSelected();
		props.add("SupplyType=" + SupplyType);
	}
	props.add("SetToMissing=" + SetToMissing);
	props.add("ProcessWhen=" + ProcessWhen);
	props.add("IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed and the dialog is closed.
*/
public void response ( boolean ok ) {
	__ok = ok;
	if ( ok ) {
		// Commit the changes.
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out.
			return;
		}
	}
	// Now close out.
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

// The following methods are all necessary because this class implements WindowListener.

public void windowActivated(WindowEvent evt) {
}

public void windowClosed(WindowEvent evt) {
}

public void windowDeactivated(WindowEvent evt) {
}

public void windowDeiconified(WindowEvent evt) {
}

public void windowIconified(WindowEvent evt) {
}

public void windowOpened(WindowEvent evt) {
}

}