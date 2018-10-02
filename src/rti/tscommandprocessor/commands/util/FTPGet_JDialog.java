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
import java.util.ArrayList;
import java.util.List;

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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

@SuppressWarnings("serial")
public class FTPGet_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private final String __AddWorkingDirectoryFile = "Abs";
private final String __RemoveWorkingDirectoryFile = "Rel";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private JTextField __RemoteSite_JTextField = null;
private JTextField __Login_JTextField = null;
private JTextField __Password_JTextField = null;
private JTextField __RemoteFolder_JTextField = null;
private JTextField __FilePattern_JTextField = null;
private JTextField __DestinationFolder_JTextField = null;
private SimpleJComboBox __TransferMode_JComboBox = null;
private JTextField __RetryCount_JTextField = null;
private JTextField __RetryWait_JTextField = null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null;	// Working directory.
private boolean __error_wait = false;
private boolean __first_time = true;
private FTPGet_Command __command = null;	// Command to edit
private boolean __ok = false; // Indicates whether OK pressed to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FTPGet_JDialog ( JFrame parent, Command command )
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
		else {
		    fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY );
		fc.setDialogTitle( "Select the Destination Folder");
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getPath(); 
            if (path == null || path.equals("")) {
                return;
            }
            if (path != null) {
				// Convert path to relative path by default.
				try {
					__DestinationFolder_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"FTPGet_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(path );
                refresh();
            }
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "FTPGet");
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
			__DestinationFolder_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__DestinationFolder_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectoryFile) ) {
			try {
                __DestinationFolder_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                        __DestinationFolder_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"FTPGet_JDialog",
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
	String RemoteSite = __RemoteSite_JTextField.getText().trim();
	String Login = __Login_JTextField.getText().trim();
	String Password = __Password_JTextField.getText().trim();
	String RemoteFolder = __RemoteFolder_JTextField.getText().trim();
	String FilePattern = __FilePattern_JTextField.getText().trim();
	String DestinationFolder = __DestinationFolder_JTextField.getText().trim();
	String TransferMode = __TransferMode_JComboBox.getSelected();
	String RetryCount = __RetryCount_JTextField.getText().trim();
	String RetryWait = __RetryWait_JTextField.getText().trim();
	__error_wait = false;
	if ( RemoteSite.length() > 0 ) {
		props.set ( "RemoteSite", RemoteSite );
	}
	if ( RemoteFolder.length() > 0 ) {
		props.set ( "RemoteFolder", RemoteFolder );
	}
    if ( Login.length() > 0 ) {
        props.set ( "Login", Login );
    }
    if ( Password.length() > 0 ) {
        props.set ( "Password", Password );
    }
    if ( FilePattern.length() > 0 ) {
        props.set ( "FilePattern", FilePattern );
    }
    if ( DestinationFolder.length() > 0 ) {
        props.set ( "DestinationFolder", DestinationFolder );
    }
    if ( TransferMode.length() > 0 ) {
        props.set ( "TransferMode", TransferMode );
    }
    if ( RetryCount.length() > 0 ) {
        props.set ( "RetryCount", RetryCount );
    }
    if ( RetryWait.length() > 0 ) {
        props.set ( "RetryWait", RetryWait );
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
{	String RemoteSite = __RemoteSite_JTextField.getText().trim();
    String RemoteFolder = __RemoteFolder_JTextField.getText().trim();
    String Login = __Login_JTextField.getText().trim();
    String Password = __Password_JTextField.getText().trim();
    String FilePattern = __FilePattern_JTextField.getText().trim();
    String DestinationFolder = __DestinationFolder_JTextField.getText().trim();
    String TransferMode = __TransferMode_JComboBox.getSelected();
    String RetryCount = __RetryCount_JTextField.getText().trim();
    String RetryWait = __RetryWait_JTextField.getText().trim();
	__command.setCommandParameter ( "RemoteSite", RemoteSite );
	__command.setCommandParameter ( "RemoteFolder", RemoteFolder );
	__command.setCommandParameter ( "Login", Login );
	__command.setCommandParameter ( "Password", Password );
	__command.setCommandParameter ( "FilePattern", FilePattern );
	__command.setCommandParameter ( "DestinationFolder", DestinationFolder );
	__command.setCommandParameter ( "TransferMode", TransferMode );
	__command.setCommandParameter ( "RetryCount", RetryCount );
	__command.setCommandParameter ( "RetryWait", RetryWait );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (FTPGet_Command)command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command uses file transfer protocol (FTP) to retrieve files from a remote site and " +
		"save on the local file system." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The destination folder can be specified using ${Property} notation to utilize global properties."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that the local folder name be relative to the working directory, which is:"),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("    " + __working_dir),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Remote site:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RemoteSite_JTextField = new JTextField ( 20 );
    __RemoteSite_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __RemoteSite_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - FTP site (can use ${Property})."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Login:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Login_JTextField = new JTextField ( 10 );
    __Login_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __Login_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - case-sensitive (default=anonymous)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Password:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Password_JTextField = new JTextField ( 10 );
    __Password_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __Password_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - case-sensitive (default=anonymous)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Remote folder:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RemoteFolder_JTextField = new JTextField ( 20 );
    __RemoteFolder_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __RemoteFolder_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional (default=/ root on FTP server, can use ${Property})." ), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("File pattern:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __FilePattern_JTextField = new JTextField ( 20 );
    __FilePattern_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __FilePattern_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional (default=*, can use ${Property})."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Destination folder:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DestinationFolder_JTextField = new JTextField ( 50 );
	__DestinationFolder_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __DestinationFolder_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for folder");
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFile,this);
	    JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Transfer mode:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TransferMode_JComboBox = new SimpleJComboBox ( false );
    List<String> modeChoices = new ArrayList<String>();
    modeChoices.add ( "" );   // Default
    modeChoices.add ( __command._ASCII );
    modeChoices.add ( __command._Binary );
    __TransferMode_JComboBox.setData(modeChoices);
    __TransferMode_JComboBox.select ( 0 );
    __TransferMode_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __TransferMode_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional (default=" + __command._Binary + ")."), 
            3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Retry count:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RetryCount_JTextField = new JTextField ( 20 );
    __RetryCount_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __RetryCount_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional (default=3)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Retry wait:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RetryWait_JTextField = new JTextField ( 20 );
    __RetryWait_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __RetryWait_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - seconds (default=3)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 60 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.addKeyListener ( this );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");
	
	// Refresh the contents...
    refresh ();

	setTitle ( "Edit " + __command.getCommandName() + "() command" );

    pack();
    JGUIUtil.center( this );
	// Dialogs do not need to be resizable...
	setResizable ( false );
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
{	String routine = "FTPGet_JDialog.refresh";
    String RemoteSite = "";
    String RemoteFolder = "";
    String Login = "";
    String Password = "";
    String FilePattern = "";
    String DestinationFolder = "";
    String TransferMode = "";
    String RetryCount = "";
    String RetryWait = "";
    PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
        parameters = __command.getCommandParameters();
        RemoteSite = parameters.getValue ( "RemoteSite" );
        RemoteFolder = parameters.getValue ( "RemoteFolder" );
        Login = parameters.getValue ( "Login" );
        Password = parameters.getValue ( "Password" );
        FilePattern = parameters.getValue ( "FilePattern" );
        DestinationFolder = parameters.getValue ( "DestinationFolder" );
        TransferMode = parameters.getValue ( "TransferMode" );
        RetryCount = parameters.getValue ( "RetryCount" );
        RetryWait = parameters.getValue ( "RetryWait" );
		if ( RemoteSite != null ) {
			__RemoteSite_JTextField.setText ( RemoteSite );
		}
        if ( RemoteFolder != null ) {
            __RemoteFolder_JTextField.setText ( RemoteFolder );
        }
        if ( Login != null ) {
            __Login_JTextField.setText ( Login );
        }
        if ( Password != null ) {
            __Password_JTextField.setText ( Password );
        }
        if ( FilePattern != null ) {
            __FilePattern_JTextField.setText ( FilePattern );
        }
        if ( DestinationFolder != null ) {
            __DestinationFolder_JTextField.setText ( DestinationFolder );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__TransferMode_JComboBox, TransferMode,JGUIUtil.NONE, null, null ) ) {
            __TransferMode_JComboBox.select ( TransferMode );
        }
        else {
            if ( (TransferMode == null) || TransferMode.equals("") ) {
                // New command...select the default...
                __TransferMode_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\n"+
                "TransferMode parameter \"" + TransferMode +
                "\".  Select a\nMissing value or Cancel." );
            }
        }
        if ( RetryCount != null ) {
            __RetryCount_JTextField.setText ( RetryCount );
        }
        if ( RetryWait != null ) {
            __RetryWait_JTextField.setText ( RetryWait );
        }
	}
	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	RemoteSite = __RemoteSite_JTextField.getText().trim();
    RemoteFolder = __RemoteFolder_JTextField.getText().trim();
    Login = __Login_JTextField.getText().trim();
    Password = __Password_JTextField.getText().trim();
    FilePattern = __FilePattern_JTextField.getText().trim();
    DestinationFolder = __DestinationFolder_JTextField.getText().trim();
    TransferMode = __TransferMode_JComboBox.getSelected();
    RetryCount = __RetryCount_JTextField.getText().trim();
    RetryWait = __RetryWait_JTextField.getText().trim();
	PropList props = new PropList ( __command.getCommandName() );
	props.add ( "RemoteSite=" + RemoteSite );
	props.add ( "RemoteFolder=" + RemoteFolder );
	props.add ( "Login=" + Login );
	props.add ( "Password=" + Password );
	props.add ( "FilePattern=" + FilePattern );
	props.add ( "DestinationFolder=" + DestinationFolder );
	props.add ( "TransferMode=" + TransferMode );
	props.add ( "RetryCount=" + RetryCount );
	props.add ( "RetryWait=" + RetryWait );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( DestinationFolder );
		if ( f.isAbsolute() ) {
			__path_JButton.setText (__RemoveWorkingDirectoryFile);
			__path_JButton.setToolTipText("Change path to relative to command file");
		}
		else {
            __path_JButton.setText (__AddWorkingDirectoryFile );
            __path_JButton.setToolTipText("Change path to absolute");
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

}
