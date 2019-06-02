// AggregateWellRights_JDialog - Editor for the AggregateWellRights() command.

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
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the AggregateWellRights() command.
*/
@SuppressWarnings("serial")
public class AggregateWellRights_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener,
ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false; // Indicate whether OK has been pressed
private JTextField __AdminNumClasses_JTextField=null;
private SimpleJComboBox __OnOffDefault_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private AggregateWellRights_Command __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public AggregateWellRights_JDialog ( JFrame parent, AggregateWellRights_Command command )
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
	String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();

	PropList props = new PropList ( "" );
	if ( AdminNumClasses.length() > 0 ) {
		props.set ( "AdminNumClasses", AdminNumClasses );
	}
	if ( OnOffDefault.length() > 0 ) {
		props.set ( "OnOffDefault", OnOffDefault );
	}
	__error_wait = false;
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
	String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	__command.setCommandParameter ( "AdminNumClasses", AdminNumClasses );
	__command.setCommandParameter ( "OnOffDefault", OnOffDefault );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__AdminNumClasses_JTextField = null;
	__OnOffDefault_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, AggregateWellRights_Command command )
{	__command = command;

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
	"This command aggregates well water rights, resulting in fewer water rights.  " +
	"This increases model performance."),
	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"Aggregation occurs by weighting decree and administration numbers at a location."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"Water right classes must be supplied as administration number (NNNNN.NNNNN) breaks."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"The resulting aggregate rights replace the original rights."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Admin. number classes:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AdminNumClasses_JTextField = new JTextField("",50);
	__AdminNumClasses_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AdminNumClasses_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - use NNNNN.NNNNN, separated by commas."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
	JGUIUtil.addComponent(main_JPanel, new JLabel ("OnOff default:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List<String> onoff_Vector = new Vector<String>(3);
	onoff_Vector.add ( "" );
	onoff_Vector.add ( __command._1 );
	onoff_Vector.add ( __command._AppropriationDate );
	__OnOffDefault_JComboBox = new SimpleJComboBox(false);
	__OnOffDefault_JComboBox.setData ( onoff_Vector );
	__OnOffDefault_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __OnOffDefault_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - default OnOff switch (default=" + __command._AppropriationDate + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
	else {
		// One of the combo boxes...
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
	String AdminNumClasses = "";
	String OnOffDefault = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		AdminNumClasses = props.getValue ( "AdminNumClasses" );
		OnOffDefault = props.getValue ( "OnOffDefault" );
		if ( AdminNumClasses != null ) {
			__AdminNumClasses_JTextField.setText(AdminNumClasses);
		}
		if ( OnOffDefault == null ) {
			// Select default...
			__OnOffDefault_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__OnOffDefault_JComboBox,
				OnOffDefault, JGUIUtil.NONE, null, null ) ) {
				__OnOffDefault_JComboBox.
				select ( OnOffDefault );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid OnOffDefault " +
				"value \"" + OnOffDefault +
				"\".  Select a different value or Cancel.");
			}
		}
	}

	// Always get the value that is selected...

	AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	if ( __OnOffDefault_JComboBox != null ) {
		OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	}
	
	props.add ( "AdminNumClasses=" + AdminNumClasses );
	props.add ( "OnOffDefault=" + OnOffDefault );
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
