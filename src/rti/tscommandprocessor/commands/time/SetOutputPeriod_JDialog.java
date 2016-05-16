package rti.tscommandprocessor.commands.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.PropList;

public class SetOutputPeriod_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private JTextField __OutputStart_JTextField = null;
private JTextField __OutputEnd_JTextField = null;
private SetOutputPeriod_Command __command = null;
private JTextArea __command_JTextArea = null;
private boolean __error_wait = false; // Is there an error waiting to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether OK button has been pressed.

/**
SetOutputPeriod_JDialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetOutputPeriod_JDialog ( JFrame parent, SetOutputPeriod_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String OutputStart = __OutputStart_JTextField.getText().trim();
	String OutputEnd = __OutputEnd_JTextField.getText().trim();
	__error_wait = false;

	if ( OutputStart.length() > 0 ) {
		props.set ( "OutputStart", OutputStart );
	}
	if ( OutputEnd.length() > 0 ) {
		props.set ( "OutputEnd", OutputEnd );
	}
	try {	// This will warn the user...
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
private void commitEdits ()
{	String OutputStart = __OutputStart_JTextField.getText().trim();
	String OutputEnd = __OutputEnd_JTextField.getText().trim();
	__command.setCommandParameter ( "OutputStart", OutputStart );
	__command.setCommandParameter ( "OutputEnd", OutputEnd );
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param title Dialog title.
@param command Command to edit.
*/
private void initialize ( JFrame parent, SetOutputPeriod_Command command )
{	__command = command;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(7,2,7,2);
    Insets insetsMin = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

	// Main contents...

    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Set the global (default) output period for time series and output products."),
	    0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The time series period after reading typically will be extended to the output period by using the missing value."),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Use a SetOutputPeriod() command to guarantee a longer period when filling/extending data."),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the command at the top of commands when filling a specific period."),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Enter date/times to a precision appropriate for time series being processed.  For example:"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Year data:   YYYY"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Month data:   MM/YYYY or YYYY-MM"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Day data:     MM/DD/YYYY or YYYY-MM-DD"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Hour data:    MM/DD/YYYY HH or YYYY-MM-DD HH"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Minute data:  MM/DD/YYYY HH:mm or YYYY-MM-DD HH:mm"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Special values are also recognized (for all precisions):"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToYear = the current date to year precision"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute = the current date/time to minute precision"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute - 7Day = current date/time minus 7 days"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute + 7Day = current date/time plus 7 days"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    ${Property} = processor property as DateTime object or date/time string"),
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"See also the SetInputPeriod() command, which will constrain the period that is read."), 
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
		0, ++y, 7, 1, 0, 0, insetsMin, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output period start:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputStart_JTextField = new JTextField ( 20 );
	__OutputStart_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __OutputStart_JTextField,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Required"), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output period end:" ),
		0, ++y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputEnd_JTextField = new JTextField ( 20 );
	__OutputEnd_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __OutputEnd_JTextField,
		1, y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Required"), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 40 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add ( __ok_JButton );

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	
}

public void keyReleased ( KeyEvent event )
{	refresh();	
}

public void keyTyped ( KeyEvent event ) {;}

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
{	String OutputStart = "";
	String OutputEnd = "";
	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		OutputStart = props.getValue ( "OutputStart" );
		OutputEnd = props.getValue ( "OutputEnd" );
		if ( OutputStart != null ) {
			__OutputStart_JTextField.setText( OutputStart );
		}
		if ( OutputEnd != null ) {
			__OutputEnd_JTextField.setText( OutputEnd );
		}
	}
	// Regardless, reset the command from the fields...
	OutputStart = __OutputStart_JTextField.getText().trim();
	OutputEnd = __OutputEnd_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "OutputStart=" + OutputStart );
	props.add ( "OutputEnd=" + OutputEnd );
	__command_JTextArea.setText(__command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	__ok = ok;	// Save to be returned by ok()
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
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt )
{
}

public void windowClosed( WindowEvent evt )
{
}

public void windowDeactivated( WindowEvent evt )
{
}

public void windowDeiconified( WindowEvent evt )
{
}

public void windowIconified( WindowEvent evt )
{
}

public void windowOpened( WindowEvent evt )
{
}

}