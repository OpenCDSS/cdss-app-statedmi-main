package DWR.DMI.StateDMI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.File;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

public class RunCommands_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
    
private final String __AddWorkingDirectory = "Add Working Directory";
private final String __RemoveWorkingDirectory = "Remove Working Directory";

private SimpleJButton	__browse_JButton = null,// File browse button
			__cancel_JButton = null,// Cancel Button
			__ok_JButton = null,	// Ok Button
			__path_JButton = null;	// Convert between relative and absolute paths.
private RunCommands_Command __command = null;	// Command to edit
private JTextArea	__command_JTextArea=null;
private String		__working_dir = null;	// Working directory.
private JTextField	__InputFile_JTextField = null;
private SimpleJComboBox __ExpectedStatus_JComboBox =null;
// FIXME SAM 2008-07-15 Need to add option to inherit the properties of the calling processor
//private SimpleJComboBox __InheritParentWorkflowProperties_JComboBox =null;
private boolean		__error_wait = false;	// Is there an error waiting to be cleared up
private boolean		__first_time = true;
private boolean		__ok = false; // Indicates whether OK was pressed when closing the dialog.

/**
Command editor dialog constructor.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
public RunCommands_JDialog ( JFrame parent, Command command )
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
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
            fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle( "Select command file to run");
		
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
		if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
			__InputFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals( __RemoveWorkingDirectory) ) {
			try {
                __InputFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,	__InputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"RunCommands_JDialog",	"Error converting file to relative path." );
			}
		}
		refresh ();
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
    String ExpectedStatus = __ExpectedStatus_JComboBox.getSelected();
    //String InheritParentWorkflowProperties = __InheritParentWorkflowProperties_JComboBox.getSelected();
	__error_wait = false;
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
    if ( ExpectedStatus.length() > 0 ) {
        props.set ( "ExpectedStatus", ExpectedStatus );
    }
    /*
    if ( InheritParentWorkflowProperties.length() > 0 ) {
        props.set ( "ResetWorkflowProperties", InheritParentWorkflowProperties );
    }
    */
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
    String ExpectedStatus = __ExpectedStatus_JComboBox.getSelected();
    //String InheritParentWorkflowProperties = __InheritParentWorkflowProperties_JComboBox.getSelected();
	__command.setCommandParameter ( "InputFile", InputFile );
    __command.setCommandParameter ( "ExpectedStatus", ExpectedStatus );
    //__command.setCommandParameter ( "InheritParentWorkflowProperties", InheritParentWorkflowProperties );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__browse_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__InputFile_JTextField = null;
	__command = null;
	__ok_JButton = null;
	__path_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (RunCommands_Command)command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Read a command file and run the commands." ),
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Results are cleared before processing each command file." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    "The (success/warning/failure) status from each command file is used for the RunCommands() command." ),
    0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify a full or relative path (relative to working directory)." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command file to run:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField ( 50 );
	__InputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", this );
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Expected status:"),
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExpectedStatus_JComboBox = new SimpleJComboBox ( false );
    __ExpectedStatus_JComboBox.addItem ( "" );   // Default
    __ExpectedStatus_JComboBox.addItem ( __command._Unknown );
    __ExpectedStatus_JComboBox.addItem ( __command._Success );
    __ExpectedStatus_JComboBox.addItem ( __command._Warning );
    __ExpectedStatus_JComboBox.addItem ( __command._Failure );
    __ExpectedStatus_JComboBox.select ( 0 );
    __ExpectedStatus_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ExpectedStatus_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - use for testing (overall status=Success if matches this)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Inherit parent workflow properties?:"),
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InheritParentWorkflowProperties_JComboBox = new SimpleJComboBox ( false );
    __InheritParentWorkflowProperties_JComboBox.addItem ( "" );   // Default
    __InheritParentWorkflowProperties_JComboBox.addItem ( __command._False );
    __InheritParentWorkflowProperties_JComboBox.addItem ( __command._True );
    __InheritParentWorkflowProperties_JComboBox.select ( 0 );
    __InheritParentWorkflowProperties_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InheritParentWorkflowProperties_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "False (default) uses properties set by previous commands."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    */

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 55 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		button_JPanel.add ( __path_JButton );
	}
	button_JPanel.add (__cancel_JButton = new SimpleJButton("Cancel",this));
	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// Dialogs do not need to be resizable...
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
	refresh();	// Sets the __path_JButton status
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{	refresh();
}

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
{	String routine = "RunCommands_JDialog.refresh";
    String InputFile = "";
    String ExpectedStatus = "";
    //String InheritParentWorkflowProperties = "";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
        ExpectedStatus = props.getValue ( "ExpectedStatus" );
        //InheritParentWorkflowProperties = props.getValue ( "InheritParentWorkflowProperties" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
        if ( JGUIUtil.isSimpleJComboBoxItem(__ExpectedStatus_JComboBox, ExpectedStatus,JGUIUtil.NONE, null, null ) ) {
            __ExpectedStatus_JComboBox.select ( ExpectedStatus );
        }
        else {
            if ( (ExpectedStatus == null) || ExpectedStatus.equals("") ) {
                // New command...select the default...
                __ExpectedStatus_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\n"+
                "ExpectedStatus parameter \"" +
                ExpectedStatus +
                "\".  Select a\nMissing value or Cancel." );
            }
        }
        /*
        if ( JGUIUtil.isSimpleJComboBoxItem(__InheritParentWorkflowProperties_JComboBox, ExpectedStatus,JGUIUtil.NONE, null, null ) ) {
            __InheritParentWorkflowProperties_JComboBox.select ( ExpectedStatus );
        }
        else {
            if ( (ResetWorkflowProperties == null) || ResetWorkflowProperties.equals("") ) {
                // New command...select the default...
                __InheritParentWorkflowProperties_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\n"+
                "ResetWorkflowProperties parameter \"" +
                ResetWorkflowProperties +
                "\".  Select a\nMissing value or Cancel." );
            }
        }
        */
	}
	// Regardless, reset the command from the fields...
	InputFile = __InputFile_JTextField.getText().trim();
    ExpectedStatus = __ExpectedStatus_JComboBox.getSelected();
    //InheritParentWorkflowProperties = __InheritParentWorkflowProperties_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
	props.add ( "InputFile=" + InputFile );
    props.add ( "ExpectedStatus=" + ExpectedStatus );
    //props.add ( "InheritParentWorkflowProperties=" + InheritParentWorkflowProperties );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( InputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText ( __RemoveWorkingDirectory );
		}
		else {
            __path_JButton.setText ( __AddWorkingDirectory );
		}
	}
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

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

} // end runCommands_JDialog
