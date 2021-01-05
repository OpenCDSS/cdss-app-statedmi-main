// SetParcel_JDialog - Editor for SetParcel() command.

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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for SetParcel() command.
*/
@SuppressWarnings("serial")
public class SetParcel_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __Year_JTextField = null;
private JTextField __ParcelID_JTextField = null;
private JTextField __Division_JTextField = null;
private JTextField __District_JTextField = null;
private JTextField __CropType_JTextField = null;
private SimpleJComboBox __IrrigationMethod_JComboBox = null;
private JTextField __Area_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJComboBox	__IfFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private SetParcel_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetParcel_JDialog (JFrame parent, SetParcel_Command command) {
	super(parent, true);
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
		checkInput ();
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
	String Year = __Year_JTextField.getText().trim();
	String ParcelID = __ParcelID_JTextField.getText().trim();
	String Division = __Division_JTextField.getText().trim();
	String District = __District_JTextField.getText().trim();
	String CropType = __CropType_JTextField.getText().trim();
	String IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	String Area = __Area_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	String IfFound = __IfFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( Year.length() > 0 ) {
		parameters.set ( "Year", Year );
	}
	if ( ParcelID.length() > 0 ) {
		parameters.set ( "ParcelID", ParcelID );
	}
	if ( Division.length() > 0 ) {
		parameters.set ( "Division", Division );
	}
	if ( District.length() > 0 ) {
		parameters.set ( "District", District );
	}
	if ( CropType.length() > 0 ) {
		parameters.set ( "CropType", CropType );
	}
	if ( IrrigationMethod.length() > 0 ) {
		parameters.set ( "IrrigationMethod", IrrigationMethod );
	}
	if ( Area.length() > 0 ) {
		parameters.set ( "Area", Area );
	}
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
    if ( IfFound.length() > 0 ) {
        parameters.set ( "IfFound", IfFound );
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
{	String Year = __Year_JTextField.getText().trim();
	String ParcelID = __ParcelID_JTextField.getText().trim();
	String Division = __Division_JTextField.getText().trim();
	String District = __District_JTextField.getText().trim();
	String CropType = __CropType_JTextField.getText().trim();
	String IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	String Area = __Area_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	String IfFound = __IfFound_JComboBox.getSelected();
	__command.setCommandParameter ( "Year", Year );
	__command.setCommandParameter ( "ParcelID", ParcelID );
	__command.setCommandParameter ( "Division", Division );
	__command.setCommandParameter ( "District", District );
	__command.setCommandParameter ( "CropType", CropType );
	__command.setCommandParameter ( "IrrigationMethod", IrrigationMethod );
	__command.setCommandParameter ( "Area", Area );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
    __command.setCommandParameter ( "IfFound", IfFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command command to edit.
*/
private void initialize (JFrame parent, SetParcel_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
   	JGUIUtil.addComponent(paragraph, new JLabel (
   		"This command adds, edits, and removes parcel data for a single parcel, which is uniquely identified using year and parcel ID."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   		"Parcel data are equivalent to data from irrigated lands spatial data layer."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   		"Parcel data should be edited after using the ReadParcelsFromHydroBase and ReadParcelsFromIrrigatedLands commands."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use SetParcelSurfaceWaterSupply and SetParcelGroundWaterSupply commands to edit supply data for a parcel."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Year_JTextField = new JTextField(10);
	__Year_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - year for the parcel."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ParcelID_JTextField = new JTextField(10);
	__ParcelID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ParcelID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - parcel ID to set."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Division:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Division_JTextField = new JTextField(10);
	__Division_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Division_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - water division for the parcel."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("District:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__District_JTextField = new JTextField(10);
	__District_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __District_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - water district for the parcel."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Crop type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropType_JTextField = new JTextField(20);
	__CropType_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CropType_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - crop type for the parcel."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Irrigation method:"),
       		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> irrigationMethodList = new ArrayList<>(4);
    irrigationMethodList.add ( "" );
    irrigationMethodList.add ( __command._Drip );
    irrigationMethodList.add ( __command._Flood );
    irrigationMethodList.add ( __command._Sprinkler );
    __IrrigationMethod_JComboBox = new SimpleJComboBox(false);
    __IrrigationMethod_JComboBox.setData ( irrigationMethodList );
    __IrrigationMethod_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IrrigationMethod_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - irrigation method for the parcel."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Area:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Area_JTextField = new JTextField(10);
	__Area_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Area_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - area for the parcel (acres)."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new Vector<String>(4);
    IfNotFound_List.add("");
	IfNotFound_List.add ( __command._Add );
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	   	"Optional - indicate action if no match is found (default=" + __command._Add + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfFound_List = new ArrayList<>(6);
    IfFound_List.add("");
	IfFound_List.add ( __command._Set );
	IfFound_List.add ( __command._Remove );
	IfFound_List.add ( __command._Ignore );
	IfFound_List.add ( __command._Warn );
	IfFound_List.add ( __command._Fail );
	__IfFound_JComboBox.setData( IfFound_List );
	__IfFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	   	"Optional - indicate action if match is found (default=" + __command._Set + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
			
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
public void keyPressed (KeyEvent event)
{	int code = event.getKeyCode();

	if (code == KeyEvent.VK_ENTER) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
}

public void keyReleased (KeyEvent event)
{	refresh();
}

public void keyTyped (KeyEvent event)
{
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
private void refresh ()
{	__error_wait = false;
	String routine = getClass().getSimpleName() + ".refresh";
	String Year = "";
	String ParcelID = "";
	String Division = "";
	String District = "";
	String CropType = "";
	String IrrigationMethod = "";
	String Area = "";
	String IfNotFound = "";
	String IfFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		Year = props.getValue ( "Year" );
		ParcelID = props.getValue ( "ParcelID" );
		Division = props.getValue ( "Division" );
		District = props.getValue ( "District" );
		CropType = props.getValue ( "CropType" );
		IrrigationMethod = props.getValue ( "IrrigationMethod" );
		Area = props.getValue ( "Area" );
		IfNotFound = props.getValue ( "IfNotFound" );
		IfFound = props.getValue ( "IfFound" );
		// Display existing content...
		if ( Year != null ) {
			__Year_JTextField.setText(Year);
		}
		if ( ParcelID != null ) {
			__ParcelID_JTextField.setText(ParcelID);
		}
		if ( Division != null ) {
			__Division_JTextField.setText(Division);
		}
		if ( District != null ) {
			__District_JTextField.setText(District);
		}
		if ( CropType != null ) {
			__CropType_JTextField.setText(CropType);
		}
		if ( IrrigationMethod == null ) {
			// Select default...
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
		if ( Area != null ) {
			__Area_JTextField.setText(Area);
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
		if ( IfFound == null ) {
			// Select default...
			__IfFound_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IfFound_JComboBox, IfFound, JGUIUtil.NONE, null, null ) ) {
				__IfFound_JComboBox.select ( IfFound );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfFound value \""+
				IfFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	Year = __Year_JTextField.getText().trim();
	ParcelID = __ParcelID_JTextField.getText().trim();
	Division = __Division_JTextField.getText().trim();
	District = __District_JTextField.getText().trim();
	CropType = __CropType_JTextField.getText().trim();
	IrrigationMethod = __IrrigationMethod_JComboBox.getSelected();
	Area = __Area_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	IfFound = __IfFound_JComboBox.getSelected();
	
	props = new PropList(__command.getCommandName());
	props.add("Year=" + Year);
	props.add("ParcelID=" + ParcelID);
	props.add("Division=" + Division);
	props.add("District=" + District);
	props.add("CropType=" + CropType);
	props.add("IrrigationMethod=" + IrrigationMethod);
	props.add("Area=" + Area);
	props.add("IfNotFound=" + IfNotFound );
	props.add("IfFound=" + IfFound );
	__command_JTextArea.setText( __command.toString(props) );
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