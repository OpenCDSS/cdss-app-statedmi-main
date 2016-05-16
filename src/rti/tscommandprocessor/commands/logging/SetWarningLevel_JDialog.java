// ----------------------------------------------------------------------------
// setWarningLevel_JDialog - editor for setWarningLevel()
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 27 Mar 2001	Steven A. Malers, RTi	Initial version (copy and modify
//					setDebugLevel_Dialog).
// 2002-04-08	SAM, RTi		Clean up interface and add comments.
// 2003-12-06	SAM, RTi		Update to Swing.
// 2007-02-26	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package rti.tscommandprocessor.commands.logging;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;

/**
Editor dialog for the SetWarningLevel() command.
*/
public class SetWarningLevel_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton	__cancel_JButton = null,// Cancel Button
			__ok_JButton = null;	// Ok Button
private SetWarningLevel_Command		__command = null; // Command to edit
private JTextArea	__command_JTextArea = null;// Command as JTextArea
private JTextField	__LogFileLevel_JTextField = null,// Fields for warning levels
			__ScreenLevel_JTextField;
private boolean		__error_wait = false;	// Is there an error to be cleared up?
private boolean		__first_time = true;
private boolean     __ok = false;       // Indicates whether user pressed OK to close the dialog.

/**
setWarningLevel_JDialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetWarningLevel_JDialog ( JFrame parent, Command command )
{ 	super(parent, true);
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
{   // Put together a list of parameters to check...
    PropList props = new PropList ( "" );
    String ScreenLevel = __ScreenLevel_JTextField.getText().trim();
    String LogFileLevel = __LogFileLevel_JTextField.getText().trim();
    if ( ScreenLevel.length() > 0 ) {
        props.set ( "ScreenLevel", ScreenLevel );
    }
    if ( LogFileLevel.length() > 0 ) {
        props.set ( "LogFileLevel", LogFileLevel );
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
Commit the edits to the command.
*/
private void commitEdits ()
{   String ScreenLevel = __ScreenLevel_JTextField.getText().trim();
    String LogFileLevel = __LogFileLevel_JTextField.getText().trim();
    __command.setCommandParameter ( "ScreenLevel", ScreenLevel );
    __command.setCommandParameter ( "LogFileLevel", LogFileLevel );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__LogFileLevel_JTextField = null;
	__ScreenLevel_JTextField = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{   __command = (SetWarningLevel_Command)command;

	addWindowListener( this );

    Insets insetsNONE = new Insets(1,1,1,1);
    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Set the warning level for screen and/or log file warning messages."),
        0, y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Setting the warning level to a higher number prints more warning information."),
		0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Warning information is used for troubleshooting." ),
		0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Warning levels can be increased before and decreased after " +
		"specific commands to troublesheet the commands." ), 
		0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel("Screen warning level:"), 
		0, ++y, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ScreenLevel_JTextField = new JTextField ( 4 );
	__ScreenLevel_JTextField.setText( "1");
	__ScreenLevel_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ScreenLevel_JTextField,
		1, y, 1, 1, 1, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("0=none, 100=all, blank=no change."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Log file warning level:" ), 
		0, ++y, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LogFileLevel_JTextField = new JTextField ( 4 );
	__LogFileLevel_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __LogFileLevel_JTextField,
		1, y, 1, 1, 1, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("0=none, 100=all, blank=no change."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    __command_JTextArea = new JTextArea ( 4, 60 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

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

	setTitle ( "Edit " + __command.getCommandName() + "() command" );
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String ScreenLevel = "1";
	String LogFileLevel = "2";
	__error_wait = false;
	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		ScreenLevel = props.getValue( "ScreenLevel" );
		LogFileLevel = props.getValue( "LogFileLevel" );
		__ScreenLevel_JTextField.setText( ScreenLevel );
		__LogFileLevel_JTextField.setText( LogFileLevel );
	}
	// Regardless, reset the command from the fields...
	ScreenLevel = __ScreenLevel_JTextField.getText().trim();
	LogFileLevel = __LogFileLevel_JTextField.getText().trim();
    props = new PropList ( __command.getCommandName() );
    props.add ( "ScreenLevel=" + ScreenLevel );
    props.add ( "LogFileLevel=" + LogFileLevel );
    __command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
public void response ( boolean ok )
{   __ok = ok;
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

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
