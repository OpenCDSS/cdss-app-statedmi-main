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
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

@SuppressWarnings("serial")
public class WebGet_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private JTextArea __URI_JTextArea = null;
private JTextField __LocalFile_JTextField = null;
private JTextField __OutputProperty_JTextField = null;
private JTextField __ResponseCodeProperty_JTextField = null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private WebGet_Command __command = null;
private boolean __ok = false; // Indicates whether OK pressed to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public WebGet_JDialog ( JFrame parent, WebGet_Command command )
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
        fc.setDialogTitle("Select Local File to Write");
        SimpleFileFilter sff = new SimpleFileFilter("txt", "Text file");
        fc.addChoosableFileFilter(sff);
        sff = new SimpleFileFilter("txt", "Text File");
        fc.addChoosableFileFilter(sff);
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName(); 
            String path = fc.getSelectedFile().getPath(); 
    
            if (filename == null || filename.equals("")) {
                return;
            }
    
            if (path != null) {
				// Convert path to relative path by default.
				try {
					__LocalFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"FTPGet_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(directory );
                refresh();
            }
        }
    }
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WebGet");
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
			__LocalFile_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__LocalFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
                __LocalFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                        __LocalFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"WebGet_JDialog", "Error converting file name to relative path." );
			}
		}
		refresh ();
	}
	else {
	    // Choices...
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
	String URI = __URI_JTextArea.getText().trim();
	String LocalFile = __LocalFile_JTextField.getText().trim();
	String OutputProperty = __OutputProperty_JTextField.getText().trim();
	String ResponseCodeProperty = __ResponseCodeProperty_JTextField.getText().trim();
	__error_wait = false;
	if ( URI.length() > 0 ) {
		props.set ( "URI", URI );
	}
    if ( LocalFile.length() > 0 ) {
        props.set ( "LocalFile", LocalFile );
    }
    if ( OutputProperty.length() > 0 ) {
        props.set ( "OutputProperty", OutputProperty );
    }
    if ( ResponseCodeProperty.length() > 0 ) {
        props.set ( "ResponseCodeProperty", ResponseCodeProperty );
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
private void commitEdits ()
{	String URI = __URI_JTextArea.getText().trim();
    String LocalFile = __LocalFile_JTextField.getText().trim();
    String OutputProperty = __OutputProperty_JTextField.getText().trim();
    String ResponseCodeProperty = __ResponseCodeProperty_JTextField.getText().trim();
	__command.setCommandParameter ( "URI", URI );
	__command.setCommandParameter ( "LocalFile", LocalFile );
	__command.setCommandParameter ( "OutputProperty", OutputProperty );
	__command.setCommandParameter ( "ResponseCodeProperty", ResponseCodeProperty );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, WebGet_Command command )
{	__command = command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command retrieves content from the web using a Uniform Resource Identifier (URI) and" +
		" saves the content to a local file and/or a processor property." ),
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"It is recommended that the local file name is relative to the working directory, which is:"),
    		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"    " + __working_dir),
    		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("URI:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __URI_JTextArea = new JTextArea ( 6, 60 );
    __URI_JTextArea.setToolTipText("Specify the URL from which to read content, can use ${Property}.");
    __URI_JTextArea.setLineWrap ( true );
    __URI_JTextArea.setWrapStyleWord ( true );
    __URI_JTextArea.addKeyListener ( this );
    __URI_JTextArea.setEditable ( true );
        JGUIUtil.addComponent(main_JPanel, new JScrollPane(__URI_JTextArea),
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
      
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Local file:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LocalFile_JTextField = new JTextField ( 50 );
	__LocalFile_JTextField.setToolTipText("Specify the output file (will have same the contents as retrieved from URL), can use ${Property}.");
	__LocalFile_JTextField.addKeyListener ( this );
    // Output file layout fights back with other rows so put in its own panel
	JPanel LocalFile_JPanel = new JPanel();
	LocalFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(LocalFile_JPanel, __LocalFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(LocalFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(LocalFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, LocalFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output property:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputProperty_JTextField = new JTextField (10);
    __OutputProperty_JTextField.setToolTipText("Name of processor property to assign retrieved URI content.");
    __OutputProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __OutputProperty_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - property name for output (default=not set)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Response code property:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ResponseCodeProperty_JTextField = new JTextField (10);
    __ResponseCodeProperty_JTextField.setToolTipText("Name of processor property to assign retrieved URI content.");
    __ResponseCodeProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ResponseCodeProperty_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - property name for response code (default=not set)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 6, 60 );
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
{	//String routine = "WebGet_JDialog.refresh";
    String URI = "";
    String LocalFile = "";
    String OutputProperty = "";
    String ResponseCodeProperty = "";
    PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
        parameters = __command.getCommandParameters();
        URI = parameters.getValue ( "URI" );
        LocalFile = parameters.getValue ( "LocalFile" );
        OutputProperty = parameters.getValue ( "OutputProperty" );
        ResponseCodeProperty = parameters.getValue ( "ResponseCodeProperty" );
		if ( URI != null ) {
			__URI_JTextArea.setText ( URI );
		}
        if ( LocalFile != null ) {
            __LocalFile_JTextField.setText ( LocalFile );
        }
        if ( OutputProperty != null ) {
            __OutputProperty_JTextField.setText ( OutputProperty );
        }
        if ( ResponseCodeProperty != null ) {
            __ResponseCodeProperty_JTextField.setText ( ResponseCodeProperty );
        }
	}
	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	URI = __URI_JTextArea.getText().trim();
    LocalFile = __LocalFile_JTextField.getText().trim();
    OutputProperty = __OutputProperty_JTextField.getText().trim();
    ResponseCodeProperty = __ResponseCodeProperty_JTextField.getText().trim();
	PropList props = new PropList ( __command.getCommandName() );
	props.add ( "URI=" + URI );
	props.add ( "LocalFile=" + LocalFile );
	props.add ( "OutputProperty=" + OutputProperty );
	props.add ( "ResponseCodeProperty=" + ResponseCodeProperty );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		if ( (LocalFile != null) && !LocalFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( LocalFile );
			if ( f.isAbsolute() ) {
				__path_JButton.setText ( __RemoveWorkingDirectory );
				__path_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__path_JButton.setText ( __AddWorkingDirectory );
            	__path_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path_JButton.setEnabled(false);
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
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