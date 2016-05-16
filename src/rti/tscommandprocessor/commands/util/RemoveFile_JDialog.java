package rti.tscommandprocessor.commands.util;

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
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

public class RemoveFile_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private final String __AddWorkingDirectoryFile = "Add Working Directory";
private final String __RemoveWorkingDirectoryFile = "Remove Working Directory";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private JTextField	__InputFile_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox =null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private RemoveFile_Command __command = null;
private boolean __ok = false; // Whether the user has pressed OK to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public RemoveFile_JDialog ( JFrame parent, RemoveFile_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {	fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setDialogTitle( "Select File to Remove");
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__InputFile_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectoryFile) ) {
			__InputFile_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectoryFile) ) {
			try {
                __InputFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                        __InputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"RemoveFile_JDialog",
				"Error converting file name to relative path." );
			}
		}
		refresh ();
	}
	else {	// Choices...
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String InputFile = __InputFile_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__error_wait = false;
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
	if ( IfNotFound.length() > 0 ) {
		props.set ( "IfNotFound", IfNotFound );
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
{	String InputFile = __InputFile_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, RemoveFile_Command command )
{	__command = command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command removes a file.  The file to be removed does not need to exist when editing this command." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that the file name be relative to the working directory, which is:"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    " + __working_dir),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("File to remove:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField ( 50 );
	__InputFile_JTextField.setToolTipText("Specify the file to remove or use ${Property} notation");
	__InputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", this );
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

   JGUIUtil.addComponent(main_JPanel, new JLabel ( "If not found?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox ( false );
	__IfNotFound_JComboBox.addItem ( "" );	// Default
	__IfNotFound_JComboBox.addItem ( __command._Ignore );
	__IfNotFound_JComboBox.addItem ( __command._Warn );
	__IfNotFound_JComboBox.addItem ( __command._Fail );
	__IfNotFound_JComboBox.select ( 0 );
	__IfNotFound_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Optional - action if file not found (default=" + __command._Warn + ")"), 
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 60 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.addKeyListener ( this );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if ( __working_dir != null ) {
		// Add the buttons to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton( __RemoveWorkingDirectoryFile,this);
		button_JPanel.add ( __path_JButton );
	}
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );

	setTitle ( "Edit " + __command.getCommandName() + "() command" );
	
	// Refresh the contents...
    refresh ();

	// Dialogs do not need to be resizable...
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
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = "RemoveFile_JDialog.refresh";
	String InputFile = "";
	String IfNotFound = "";
    PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
        parameters = __command.getCommandParameters();
		InputFile = parameters.getValue ( "InputFile" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
		if ( JGUIUtil.isSimpleJComboBoxItem(__IfNotFound_JComboBox, IfNotFound,JGUIUtil.NONE, null, null ) ) {
			__IfNotFound_JComboBox.select ( IfNotFound );
		}
		else {
            if ( (IfNotFound == null) ||	IfNotFound.equals("") ) {
				// New command...select the default...
				__IfNotFound_JComboBox.select ( 0 );
			}
			else {	// Bad user command...
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\n"+
				"IfNotFound parameter \"" +	IfNotFound +
				"\".  Select a\n value or Cancel." );
			}
		}
	}
	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	InputFile = __InputFile_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	PropList props = new PropList ( __command.getCommandName() );
	props.add ( "InputFile=" + InputFile );
	props.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( InputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText (__RemoveWorkingDirectoryFile);
		}
		else {
            __path_JButton.setText (__AddWorkingDirectoryFile );
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
public void response ( boolean ok )
{	__ok = ok;
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

} // end RemoveFile_JDialog
