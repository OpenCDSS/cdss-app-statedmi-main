package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
Editor for the FillNetworkFromHydroBase() command.
*/
public class FillNetworkFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __LocationEstimate_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillNetworkFromHydroBase_Command __command = null;
private boolean __ok = false;  // Has OK been pressed to close dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillNetworkFromHydroBase_JDialog (JFrame parent, FillNetworkFromHydroBase_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();

	if (s.equals("Cancel")) {
		response (false);
	}
	else if (s.equals("OK")) {
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String LocationEstimate = __LocationEstimate_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (LocationEstimate.length() > 0) {
		props.set("LocationEstimate", LocationEstimate);
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
private void commitEdits()
{
	String LocationEstimate = __LocationEstimate_JComboBox.getSelected();

	__command.setCommandParameter("LocationEstimate", LocationEstimate );
}
	
public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__LocationEstimate_JComboBox = null;
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
private void initialize ( JFrame parent, FillNetworkFromHydroBase_Command command )
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
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>This is experimental and is an attempt to determine geographic coordinates " +
		"for locations using HydroBase.</B></HTML>"),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>To fill node names, etc., use the Fill*FromNetwork() commands for each " +
		"station component.</B></HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
    	"This command fills missing location coordinates for network nodes, using data in HydroBase."),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"The generalized network data is used by StateDMI and StateMod GUI" + 
		" to represent the model network as a diagram."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This may be appropriate if a StateMod river network file has "+
		"been used to create the generalized network."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Location estimate:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LocationEstimate_JComboBox = new SimpleJComboBox();
	List choices = new Vector ( 2 );
	choices.add ( "" );
	choices.add ( __command._Interpolate );
	__LocationEstimate_JComboBox.setData ( choices );
	__LocationEstimate_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(main_JPanel, __LocationEstimate_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - how to estimate missing coordinates (default=" + __command._Interpolate + ")."),
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

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);

	setTitle("Edit " + __command.getCommandName() + "() Command");
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
		checkInput ();
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
{	String routine = getClass().getName() + ".refresh";
	String LocationEstimate = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;
		// Get the properties from the command
		props = __command.getCommandParameters();
		LocationEstimate = props.getValue ( "LocationEstimate" );
		if ( LocationEstimate == null ) {
			// Select default...
			__LocationEstimate_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__LocationEstimate_JComboBox, LocationEstimate, JGUIUtil.NONE, null, null ) ){
				__LocationEstimate_JComboBox.select ( LocationEstimate );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nLocationEstimate " +
				"value \"" + LocationEstimate + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	LocationEstimate = __LocationEstimate_JComboBox.getSelected();
	
	props = new PropList(__command.getCommandName());
	props.add("LocationEstimate=" + LocationEstimate);
	__command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
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