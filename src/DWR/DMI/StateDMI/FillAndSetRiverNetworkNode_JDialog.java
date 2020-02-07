// FillAndSetRiverNetworkNode_JDialog - Editor for SetRiverNetworkNode() and FillRiverNetworkNode() commands.

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

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for SetRiverNetworkNode() and FillRiverNetworkNode() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetRiverNetworkNode_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __DownstreamRiverNodeID_JTextField = null;
private JTextField __MaxRechargeLimit_JTextField = null;
//private JTextField __Comment_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private FillAndSetRiverNetworkNode_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetRiverNetworkNode_JDialog ( JFrame parent, FillAndSetRiverNetworkNode_Command command )
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
	String ID = __ID_JTextField.getText().trim();
	String Name = __Name_JTextField.getText().trim();
	String DownstreamRiverNodeID = __DownstreamRiverNodeID_JTextField.getText().trim();
	String MaxRechargeLimit = __MaxRechargeLimit_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( Name.length() > 0 ) {
		parameters.set ( "Name", Name );
	}
    if ( DownstreamRiverNodeID.length() > 0 ) {
        parameters.set ( "DownstreamRiverNodeID", DownstreamRiverNodeID );
    }
    if ( MaxRechargeLimit.length() > 0 ) {
        parameters.set ( "MaxRechargeLimit", MaxRechargeLimit );
    }
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
	__error_wait = false;
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
	String Name = __Name_JTextField.getText().trim();
	String DownstreamRiverNodeID = __DownstreamRiverNodeID_JTextField.getText().trim();
	String MaxRechargeLimit = __MaxRechargeLimit_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "DownstreamRiverNodeID", DownstreamRiverNodeID );
    __command.setCommandParameter ( "MaxRechargeLimit", MaxRechargeLimit );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__ID_JTextField = null;
	__Name_JTextField = null;
	__DownstreamRiverNodeID_JTextField = null;
	__MaxRechargeLimit_JTextField = null;
	//__Comment_JTextField = null;
	__IfNotFound_JComboBox = null;
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
private void initialize (JFrame parent, FillAndSetRiverNetworkNode_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	if ( __command instanceof FillRiverNetworkNode_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in a river network node," + 
		" using the river network node ID to look up the location."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetRiverNetworkNode_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in a river network node," + 
		" using the river network node ID to look up the location."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The river network node ID can contain a * wildcard pattern to match one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);
	
    if ( __command instanceof SetRiverNetworkNode_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"If the river network node ID does not contain a * wildcard pattern and does not match an ID,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"the river network node will be added if the \"If not found\" parameter" +
		" is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, 
		GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - river network node(s) to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__Name_JTextField = new JTextField (20);
	__Name_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - up to 24 characters for StateMod."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Downstream river node ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__DownstreamRiverNodeID_JTextField = new JTextField (10);
	__DownstreamRiverNodeID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DownstreamRiverNodeID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - up to 12 characters."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST );

        /**
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Comment:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
        GridBagConstraints.EAST);
    __Comment_JTextField = new JTextField (10);
    __Comment_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Comment_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
        GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel ( ""),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
        GridBagConstraints.WEST );
        **/
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Maximum recharge limit:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__MaxRechargeLimit_JTextField = new JTextField (10);
	__MaxRechargeLimit_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __MaxRechargeLimit_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, 
       	new JLabel ( "Optional - maximum recharge limit (CFS) if modeling groundwater."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
    List<String> if_not_found_Vector = new Vector<String>();
    if_not_found_Vector.add ( "" );
    if ( __command instanceof SetRiverNetworkNode_Command ) {
    	if_not_found_Vector.add ( __command._Add );
    }
    if_not_found_Vector.add ( __command._Ignore );
	if_not_found_Vector.add ( __command._Warn );
	if_not_found_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( if_not_found_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if no match is found (default="+ __command._Warn + ")"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, 
		GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,30);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, 
		GridBagConstraints.WEST);
			
	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, 
		GridBagConstraints.CENTER);

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
{	String routine = getClass().getName() + ".refresh";
	String ID = "";
	String DownstreamRiverNodeID = "";
	String MaxRechargeLimit = "";
	String Name = "";
	//String Comment = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		DownstreamRiverNodeID = props.getValue ( "DownstreamRiverNodeID" );
		MaxRechargeLimit = props.getValue ( "MaxRechargeLimit" );
		Name = props.getValue ( "Name" );
		//Comment = props.getValue( "Comment" );
		IfNotFound = props.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( DownstreamRiverNodeID != null ) {
			__DownstreamRiverNodeID_JTextField.setText(DownstreamRiverNodeID);
		}
		if ( MaxRechargeLimit != null ) {
			__MaxRechargeLimit_JTextField.setText(MaxRechargeLimit);
		}
		/**
		if( Comment != null ){
			__Comment_JTextField.setText(Comment);
		}
		**/
		if ( Name != null ) {
			__Name_JTextField.setText(Name);
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

	ID = __ID_JTextField.getText().trim();
	DownstreamRiverNodeID = __DownstreamRiverNodeID_JTextField.getText().trim();
	MaxRechargeLimit = __MaxRechargeLimit_JTextField.getText().trim();
	Name = __Name_JTextField.getText().trim();
	//Comment = __Comment_JTextField.getText().trim();
	//Comment = "";
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props = new PropList ( "");
	props.add ( "ID=" + ID );
	props.add ( "Name=" + Name );
	props.add ( "DownstreamRiverNodeID=" + DownstreamRiverNodeID );
	props.add ( "MaxRechargeLimit=" + MaxRechargeLimit );
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
