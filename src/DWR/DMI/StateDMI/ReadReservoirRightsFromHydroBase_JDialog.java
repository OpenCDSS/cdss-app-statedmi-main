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
Editor for ReadReservoirRightsFromHydroBase_Command.
*/
public class ReadReservoirRightsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextField __ID_JTextField=null;
private JTextField __DecreeMin_JTextField=null;
private JTextField __AdminNumClasses_JTextField=null;
private SimpleJComboBox __OnOffDefault_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private ReadReservoirRightsFromHydroBase_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadReservoirRightsFromHydroBase_JDialog ( JFrame parent,
	ReadReservoirRightsFromHydroBase_Command command)
{	super(parent, true);
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
private void checkInput ()
{
	String ID = __ID_JTextField.getText().trim();
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );

	if (ID.length() > 0 ) {
		props.set("ID", ID);
	}
	if (DecreeMin.length() > 0 ) {
		props.set("DecreeMin", DecreeMin);
	}
	if (AdminNumClasses.length() > 0 ) {
		props.set("AdminNumClasses", AdminNumClasses);
	}
	if (OnOffDefault.length() > 0 ) {
		props.set("OnOffDefault", OnOffDefault);
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
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();

	__command.setCommandParameter("ID", ID);
	__command.setCommandParameter("DecreeMin", DecreeMin);
	__command.setCommandParameter("AdminNumClasses", AdminNumClasses);
	__command.setCommandParameter("OnOffDefault", OnOffDefault);
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__DecreeMin_JTextField = null;
	__AdminNumClasses_JTextField = null;
	__OnOffDefault_JComboBox = null;
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
private void initialize (JFrame parent, ReadReservoirRightsFromHydroBase_Command command) {
	__command = command;

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
		"This command reads reservoir rights from HydroBase, using " +
		"the reservoir station identifiers to find rights."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Net absolute water rights are read by default."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"If the reservoir stations contain aggregates, use the " +
		"administration number classes to group rights"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"(indicate administration numbers separated by commas)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The output water right identifier is assigned as the" +
		" reservoir station identifer + \".\" + a three digit number."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Reservoir station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - reservoir stations to read (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Decree minimum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DecreeMin_JTextField = new JTextField(10);
	__DecreeMin_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DecreeMin_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - minimum decree to include (default = .0005)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Admin. number classes:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AdminNumClasses_JTextField = new JTextField("",10);
	__AdminNumClasses_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AdminNumClasses_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	/*
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify admin. numbers for aggregates."),
		5, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	*/

	JGUIUtil.addComponent(main_JPanel, new JLabel ( "OnOff default:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List onoff_Vector = new Vector(3);
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
{	String routine = __command + ".refresh";
	String ID = "";
	String DecreeMin = "";
	String AdminNumClasses = "";
	String OnOffDefault = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		DecreeMin = props.getValue ( "DecreeMin" );
		AdminNumClasses = props.getValue ( "AdminNumClasses" );
		OnOffDefault = props.getValue ( "OnOffDefault" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( DecreeMin != null ) {
			__DecreeMin_JTextField.setText(DecreeMin);
		}
		if ( AdminNumClasses != null ) {
			__AdminNumClasses_JTextField.setText(AdminNumClasses);
		}
		if ( OnOffDefault == null ) {
			// Select default...
			__OnOffDefault_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OnOffDefault_JComboBox,OnOffDefault, JGUIUtil.NONE, null, null ) ) {
				__OnOffDefault_JComboBox.select ( OnOffDefault );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid OnOffDefault " +
				"value \"" + OnOffDefault + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	// Always get the value that is selected...

	ID = __ID_JTextField.getText().trim();
	DecreeMin = __DecreeMin_JTextField.getText().trim();
	AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	props = new PropList(__command.getCommandName());
	props.add("ID=" + ID);
	props.add("DecreeMin=" + DecreeMin);
	props.add("AdminNumClasses=" + AdminNumClasses);
	props.add("OnOffDefault=" + OnOffDefault);
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