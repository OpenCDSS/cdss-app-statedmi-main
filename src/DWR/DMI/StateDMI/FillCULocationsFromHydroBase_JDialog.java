// FillCULocationsFromHydroBase_JDialog - Editor for FillCULocationsFromHydroBase() command.

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
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for FillCULocationsFromHydroBase() command.
*/
public class FillCULocationsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener,
ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextField __ID_JTextField=null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __CULocType_JComboBox = null;
private SimpleJComboBox __Region1Type_JComboBox = null;
private SimpleJComboBox __Region2Type_JComboBox = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillCULocationsFromHydroBase_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillCULocationsFromHydroBase_JDialog (JFrame parent, Command command) {
	super(parent, true);
	initialize (parent, command);
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String CULocType = __CULocType_JComboBox.getSelected();
	String Region1Type = __Region1Type_JComboBox.getSelected();
	String Region2Type = __Region2Type_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (ID.length() > 0) {
		props.set("ID", ID);
	}
	if (CULocType.length() > 0 && !CULocType.equals("")) {
		props.set("CULocType", CULocType);
	}
	if (Region1Type.length() > 0 && !Region1Type.equals("")) {
		props.set("Region1Type", Region1Type);
	}
	if (Region2Type.length() > 0 && !Region2Type.equals("")) {
		props.set("Region2Type", Region2Type);
	}
	if (IfNotFound.length() > 0 && !IfNotFound.equals("")) {
		props.set("IfNotFound", IfNotFound);
	}

	try {
		// This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	} 
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits() {
	String ID = __ID_JTextField.getText().trim();
	String CULocType = __CULocType_JComboBox.getSelected();
	String Region1Type = __Region1Type_JComboBox.getSelected();
	String Region2Type = __Region2Type_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter("ID", ID);
	__command.setCommandParameter("CULocType", CULocType);
	__command.setCommandParameter("Region1Type", Region1Type);
	__command.setCommandParameter("Region2Type", Region2Type);
	__command.setCommandParameter("IfNotFound", IfNotFound);
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__ID_JTextField = null;
	__CULocType_JComboBox = null;
	__Region1Type_JComboBox = null;
	__Region2Type_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, Command command)
{
	__command = (FillCULocationsFromHydroBase_Command)command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in CU Locations by using " +
		"data from HydroBase, matching the CU Location identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Currently the CU Location type can only be a structure.  In" +
		" the future, counties, etc., may be supported."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The following values from HydroBase are set if missing in a CU Location:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"   Region1 (currently can only be a county)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"   Region2 (currently can only be a Hydrologic Unit Code, HUC)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"   Name (taken from the structure data)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"   Latitude (taken from structure location)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Specifying the types for the location, Region1, and Region2 "+
		"indicate how the data should be taken from HydroBase."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - the CU locations to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List loctype = new Vector(2);
    loctype.add("");
	loctype.add(__command._Structure);
	__CULocType_JComboBox = new SimpleJComboBox (false);
	__CULocType_JComboBox.setData ( loctype );
	__CULocType_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __CULocType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the location type for the data to be filled (default=" + __command._Structure + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region1 Type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List region1 = new Vector(2);
    region1.add("");
	region1.add(__command._County);
	__Region1Type_JComboBox = new SimpleJComboBox (false);
	__Region1Type_JComboBox.setData (region1);
	__Region1Type_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Region1Type_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the type of region associated with Region1 (default=" + __command._County + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region2 type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List region2 = new Vector(2);
    region2.add("");
	region2.add(__command._HUC);
	__Region2Type_JComboBox = new SimpleJComboBox (false);
	__Region2Type_JComboBox.setData (region2);	
	__Region2Type_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Region2Type_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the type of region associated with Region2 (default=" + __command._HUC + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List IfNotFound_List = new Vector();
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
{	String routine = "fillCULocationsFromHydroBase.refresh";
	String ID = "";
	String CULocType = "";
	String Region1Type = "";
	String Region2Type = "";
	String IfNotFound = "";
	PropList parameters = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		parameters = __command.getCommandParameters();
		ID = parameters.getValue ( "ID" );
		CULocType = parameters.getValue ( "CULocType" );
		Region1Type = parameters.getValue ( "Region1Type" );
		Region2Type = parameters.getValue ( "Region2Type" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( CULocType == null ) {
			// Select default...
			__CULocType_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__CULocType_JComboBox, CULocType, JGUIUtil.NONE, null, null ) ) {
				__CULocType_JComboBox.select ( CULocType );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nCULocType value \"" +
				CULocType + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region1Type == null ) {
			// Select default...
			__Region1Type_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region1Type_JComboBox, Region1Type, JGUIUtil.NONE, null, null ) ) {
				__Region1Type_JComboBox.select ( Region1Type );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nRegion1Type value \"" +
				Region1Type + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region2Type == null ) {
			// Select default...
			__Region2Type_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region2Type_JComboBox, Region2Type, JGUIUtil.NONE, null, null ) ) {
				__Region2Type_JComboBox.select ( Region2Type );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nRegion2Type value \"" +
				Region2Type + "\".  Select a different value or Cancel.");
				__error_wait = true;
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

	ID = __ID_JTextField.getText().trim();
	CULocType = (String)__CULocType_JComboBox.getSelected();
	Region1Type = (String)__Region1Type_JComboBox.getSelected();
	Region2Type = (String)__Region2Type_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add("ID=" + ID);
	parameters.add("CULocType=" + CULocType);
	parameters.add("Region1Type=" + Region1Type);
	parameters.add("Region2Type=" + Region2Type);
	parameters.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(parameters) );
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
 * Responds to WindowEvents.
 *
 * @param event WindowEvent object 
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
