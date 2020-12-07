// ReadParcelsFromHydroBase_JDialog - Editor for ReadParcelsFromHydroBase() command.

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

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;

/**
Editor for ReadParcelsFromHydroBase() command.
*/
@SuppressWarnings("serial")
public class ReadParcelsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextField __ID_JTextField=null;
private JTextField __InputStart_JTextField = null;
private JTextField __InputEnd_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;
private ReadParcelsFromHydroBase_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Time series command to parse.
*/
public ReadParcelsFromHydroBase_JDialog (	JFrame parent, ReadParcelsFromHydroBase_Command command )
{	super(parent, true);
	initialize (parent, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();
	String s = event.getActionCommand();

	if (s.equals("Cancel")) {
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if (s.equals("OK")) {
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
{
	String ID = __ID_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );

	if (ID.length() > 0 ) {
		props.set("ID", ID);
	}
	if (InputStart.length() > 0 ) {
		props.set("InputStart", InputStart);
	}
	if (InputEnd.length() > 0 ) {
		props.set("InputEnd", InputEnd);
	}
	if ( Div.length() > 0 ) {
		props.set ( "Div", Div );
	}
	
	__error_wait = false;

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
private void commitEdits()
{
	String ID = __ID_JTextField.getText().trim();
	String InputStart = __InputStart_JTextField.getText().trim();
	String InputEnd = __InputEnd_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	__command.setCommandParameter("ID", ID);
	__command.setCommandParameter("InputStart", InputStart);
	__command.setCommandParameter("InputEnd", InputEnd);
	__command.setCommandParameter ( "Div", Div );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, ReadParcelsFromHydroBase_Command command)
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
		"This command reads from HydroBase the following data associated with CU Locations:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    - parcels for irrigated lands"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    - ditch and well water supply associated with the parcels"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The command processes collections and tracks other information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
    	"The resulting data can be used by other commands rather than rereading the same complex data."),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.setToolTipText("Pattern to match CU loction identifiers, * for wildcard.");
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - locations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input start (year):"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputStart_JTextField = new JTextField (10);
    __InputStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
       	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
       	"Optional - starting year to read data (blank for full period)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input end (year):"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputEnd_JTextField = new JTextField (10);
    __InputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
       	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
       	"Optional - ending year to read data (blank for full period)."),
       	3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Water division(s):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.setToolTipText("Divisions for irrigated acreage, separated by commas, "
		+ "used to determine years for data (default is determine divisions from location IDs).");
	__Div_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
   		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (	"Optional - water division(s) for the data."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
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

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (true);
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
{	//String routine = "ReadCULocationParcelsFromHydroBase_JDialog.refresh";
	__error_wait = false;
	String ID="";
	String InputStart = "";
	String InputEnd = "";
	String Div = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		Div = props.getValue ( "Div" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( InputStart != null ) {
			__InputStart_JTextField.setText(InputStart);
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText(InputEnd);
		}
		if ( Div != null ) {
			__Div_JTextField.setText(Div);
		}
	}

	// Always get the value that is selected...

	props = new PropList(__command.getCommandName());
	ID = __ID_JTextField.getText().trim();
	InputStart = __InputStart_JTextField.getText().trim();
	InputEnd = __InputEnd_JTextField.getText().trim();
	Div = __Div_JTextField.getText().trim();
	props.add("ID=" + ID);
	props.add("InputStart=" + InputStart);
	props.add("InputEnd=" + InputEnd);
	props.add("Div=" + Div);
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