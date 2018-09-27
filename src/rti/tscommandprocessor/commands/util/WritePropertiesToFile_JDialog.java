package rti.tscommandprocessor.commands.util;

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

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.io.File;
import java.util.List;
import java.util.Vector;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.FileWriteModeType;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.IO.PropertyFileFormatType;
import RTi.Util.Message.Message;

/**
Command editor dialog for the WritePropertiesToFile() command.
*/
@SuppressWarnings("serial")
public class WritePropertiesToFile_JDialog extends JDialog
implements ActionListener, KeyListener, ItemListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __cancel_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null;
private WritePropertiesToFile_Command __command = null;
private String __working_dir = null;
private JTextArea __command_JTextArea=null;
private JTextField __OutputFile_JTextField = null;
// TODO SAM 2012-07-27 Convert the following from a text field to a property selector/formatter,
// similar to TSFormatSpecifiersJPanel
private JTextField __IncludeProperties_JTextField = null;
private SimpleJComboBox	__WriteMode_JComboBox = null;
private SimpleJComboBox __FileFormat_JComboBox = null;
private SimpleJComboBox __SortOrder_JComboBox = null;
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether the user has pressed OK to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public WritePropertiesToFile_JDialog (	JFrame parent, WritePropertiesToFile_Command command )
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
		String last_directory_selected =
			JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(
				last_directory_selected );
		}
		else {	fc = JFileChooserFactory.createJFileChooser(
				__working_dir );
		}
		fc.setDialogTitle("Select Property File to Write");
		SimpleFileFilter sff = new SimpleFileFilter("txt", "Property File");
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
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"WritePropertiesToFile_JDialog", "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "WritePropertiesToFile");
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
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __OutputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir,__OutputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, "WritePropertiesToFile_JDialog",
				"Error converting file to relative path." );
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
	PropList parameters = new PropList ( "" );
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String IncludeProperties = __IncludeProperties_JTextField.getText().trim();
	String WriteMode = __WriteMode_JComboBox.getSelected();
	String FileFormat = __FileFormat_JComboBox.getSelected();
    String SortOrder = __SortOrder_JComboBox.getSelected();

	__error_wait = false;
	
	if ( OutputFile.length() > 0 ) {
		parameters.set ( "OutputFile", OutputFile );
	}
	if ( IncludeProperties.length() > 0 ) {
		parameters.set ( "IncludeProperties", IncludeProperties );
	}
	if ( WriteMode.length() > 0 ) {
		parameters.set ( "WriteMode", WriteMode );
	}
    if ( FileFormat.length() > 0 ) {
        parameters.set ( "FileFormat", FileFormat );
    }
    if (SortOrder.length() > 0) {
    	parameters.set("SortOrder", SortOrder);
    }
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
{	String OutputFile = __OutputFile_JTextField.getText().trim();
    String IncludeProperties = __IncludeProperties_JTextField.getText().trim();
    String WriteMode = __WriteMode_JComboBox.getSelected();
    String FileFormat = __FileFormat_JComboBox.getSelected();
    String SortOrder = __SortOrder_JComboBox.getSelected();
    __command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "IncludeProperties", IncludeProperties );
	__command.setCommandParameter ( "WriteMode", WriteMode );
	__command.setCommandParameter ( "FileFormat", FileFormat );
    __command.setCommandParameter ( "SortOrder", SortOrder );
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, WritePropertiesToFile_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(1,2,1,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Write one or more properties to a file.  Properties include build-in properties (e.g., OutputStart) and user-defined properties." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that the output file be relative to the current working directory." ), 
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ), 
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Property file to write:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField ( 40 );
	__OutputFile_JTextField.setToolTipText("Specify the file to output or use ${Property} notation");
	__OutputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectory,this);
	    JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Properties to write:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IncludeProperties_JTextField = new JTextField(20);
	__IncludeProperties_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __IncludeProperties_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - properties to write, separated by commas, *=wildcard (default=write all)."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Write mode:"),
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<FileWriteModeType> writeModeChoices = __command.getWriteModeChoices();
    List<String> writeModeChoicesS = new Vector<String>();
    writeModeChoicesS.add ( "" );
    for ( FileWriteModeType c : writeModeChoices ) {
        writeModeChoicesS.add ( "" + c );
    }
	__WriteMode_JComboBox = new SimpleJComboBox(false);
    __WriteMode_JComboBox.setData ( writeModeChoicesS );
   	__WriteMode_JComboBox.addItemListener (this);
   	JGUIUtil.addComponent(main_JPanel, __WriteMode_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - how to write file (default=" + FileWriteModeType.OVERWRITE + ")."),
		3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("File format:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __FileFormat_JComboBox = new SimpleJComboBox(false);
    List<PropertyFileFormatType> fileFormatChoices = __command.getFileFormatChoices();
    List<String> fileFormatChoicesS = new Vector<String>();
    fileFormatChoicesS.add ( "" );
    for ( PropertyFileFormatType c : fileFormatChoices ) {
        fileFormatChoicesS.add ( "" + c );
    }
    __FileFormat_JComboBox.setData (fileFormatChoicesS);
    __FileFormat_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __FileFormat_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - property file format (default=" + PropertyFileFormatType.NAME_TYPE_VALUE + ")."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Sort order:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SortOrder_JComboBox = new SimpleJComboBox ( false );
    __SortOrder_JComboBox.add ( "" );
    __SortOrder_JComboBox.add ( __command._Ascending );
    __SortOrder_JComboBox.add ( __command._Descending );
    __SortOrder_JComboBox.add ( __command._None );
    __SortOrder_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __SortOrder_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Optional - sort order (default=" + __command._None + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __command_JTextArea = new JTextArea ( 4, 50 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
    		1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", "OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add ( __ok_JButton );
	__cancel_JButton = new SimpleJButton("Cancel", "Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command" );
    pack();
    JGUIUtil.center( this );
	refresh();	// Sets the __path_JButton status
	setResizable ( false );
    super.setVisible( true );
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
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getSimpleName() + ".refresh";
	String OutputFile = "";
	String IncludeProperties = "";
	String WriteMode = "";
	String FileFormat = "";
    String SortOrder = "";
	__error_wait = false;
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		OutputFile = parameters.getValue ( "OutputFile" );
		IncludeProperties = parameters.getValue ( "IncludeProperties" );
		WriteMode = parameters.getValue ( "WriteMode" );
		FileFormat = parameters.getValue ( "FileFormat" );
		SortOrder = parameters.getValue("SortOrder");
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText (OutputFile);
		}
		if ( IncludeProperties != null ) {
            __IncludeProperties_JTextField.setText (IncludeProperties);
        }
		if ( WriteMode == null ) {
			// Select default...
			__WriteMode_JComboBox.select ( 0 );
		}
		else {
		    if ( JGUIUtil.isSimpleJComboBoxItem(__WriteMode_JComboBox,
				WriteMode, JGUIUtil.NONE, null, null ) ) {
				__WriteMode_JComboBox.select ( WriteMode );
			}
			else {
			    Message.printWarning ( 1, routine,
				"Existing command references an invalid\nWriteMode value \"" +
				WriteMode + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
        if ( FileFormat == null ) {
            // Select default...
            __FileFormat_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(__FileFormat_JComboBox,
                FileFormat, JGUIUtil.NONE, null, null ) ) {
                __FileFormat_JComboBox.select ( FileFormat );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nFileFormat value \"" +
                FileFormat + "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( SortOrder == null ) {
            // Select default...
            __SortOrder_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(
                __SortOrder_JComboBox, SortOrder, JGUIUtil.NONE, null, null )) {
                __SortOrder_JComboBox.select ( SortOrder );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\n" +
                "SortOrder value \"" + SortOrder + "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
	}
	// Regardless, reset the command from the fields...
	OutputFile = __OutputFile_JTextField.getText().trim();
	IncludeProperties = __IncludeProperties_JTextField.getText().trim();
	WriteMode = __WriteMode_JComboBox.getSelected();
	FileFormat = __FileFormat_JComboBox.getSelected();
	SortOrder = __SortOrder_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "OutputFile=" + OutputFile );
	parameters.add ( "IncludeProperties=" + IncludeProperties );
	parameters.add ( "WriteMode=" + WriteMode );
	parameters.add ( "FileFormat=" + FileFormat );
	parameters.add ( "SortOrder=" + SortOrder );
	__command_JTextArea.setText( __command.toString ( parameters ) );
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		if ( __path_JButton != null ) {
			__path_JButton.setEnabled ( false );
		}
	}
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( OutputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText ( __RemoveWorkingDirectory );
			__path_JButton.setToolTipText("Change path to relative to command file");
		}
		else {
		    __path_JButton.setText ( __AddWorkingDirectory );
            __path_JButton.setToolTipText("Change path to absolute");
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
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

}