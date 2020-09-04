package DWR.DMI.StateDMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

@SuppressWarnings("serial")
public class SplitStateModReport_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __browseReport_JButton = null;
private SimpleJButton __pathReport_JButton = null;
private SimpleJButton __browseOutputFolder_JButton = null;
private SimpleJButton __pathOutputFolder_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private JTextField __ReportFile_JTextField = null;
private JTextField __OutputFolder_JTextField = null;
private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __IfOutputFolderDoesNotExist_JComboBox = null;
private SimpleJComboBox __ListInResults_JComboBox = null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private SplitStateModReport_Command __command = null;
private boolean __ok = false; // Whether the user has pressed OK to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SplitStateModReport_JDialog ( JFrame parent, SplitStateModReport_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browseReport_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setDialogTitle( "Select Statemod Report File");
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 

			SimpleFileFilter sff = new SimpleFileFilter("xwb", "StateMod Water Balance Report");
			fc.addChoosableFileFilter(sff);
			fc.setFileFilter(sff);
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				// Convert path to relative path by default.
				try {
					__ReportFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"SplitStateModReport_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
    if ( o == __browseOutputFolder_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY );
        fc.setDialogTitle( "Select Output Folder");
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filename = fc.getSelectedFile().getName(); 
            String path = fc.getSelectedFile().getPath(); 
    
            if (filename == null || filename.equals("")) {
                return;
            }
    
            if (path != null) {
				// Convert path to relative path by default.
				try {
					__OutputFolder_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"SplitStateModReport_JDialog", "Error converting folder to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(path);
                refresh();
            }
        }
    }
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "SplitStateModReport");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __pathReport_JButton ) {
		if ( __pathReport_JButton.getText().equals(__AddWorkingDirectory) ) {
			__ReportFile_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__ReportFile_JTextField.getText() ) );
		}
		else if ( __pathReport_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
                __ReportFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                        __ReportFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"SplitStateModReport_JDialog",
				"Error converting report file name to relative path." );
			}
		}
		refresh ();
	}
    else if ( o == __pathOutputFolder_JButton ) {
        if ( __pathOutputFolder_JButton.getText().equals(__AddWorkingDirectory) ) {
            __OutputFolder_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__OutputFolder_JTextField.getText() ) );
        }
        else if ( __pathOutputFolder_JButton.getText().equals(__RemoveWorkingDirectory) ) {
            try {
                __OutputFolder_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                    __OutputFolder_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1,"SplitStateModReport_JDialog",
                "Error converting output folder name to relative path." );
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
	String ReportFile = __ReportFile_JTextField.getText().trim();
	String OutputFolder = __OutputFolder_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String IfOutputFolderDoesNotExist = __IfOutputFolderDoesNotExist_JComboBox.getSelected();
	String ListInResults = __ListInResults_JComboBox.getSelected();
	__error_wait = false;
	if ( ReportFile.length() > 0 ) {
		props.set ( "ReportFile", ReportFile );
	}
    if ( OutputFolder.length() > 0 ) {
        props.set ( "OutputFolder", OutputFolder );
    }
    if ( OutputFile.length() > 0 ) {
        props.set ( "OutputFile", OutputFile );
    }
	if ( IfOutputFolderDoesNotExist.length() > 0 ) {
		props.set ( "IfOutputFolderDoesNotExist", IfOutputFolderDoesNotExist );
	}
    if ( ListInResults.length() > 0 ) {
        props.set ( "ListInResults", ListInResults );
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
{	String ReportFile = __ReportFile_JTextField.getText().trim();
    String OutputFolder = __OutputFolder_JTextField.getText().trim();
    String OutputFile = __OutputFile_JTextField.getText().trim();
	String IfOutputFolderDoesNotExist = __IfOutputFolderDoesNotExist_JComboBox.getSelected();
    String ListInResults = __ListInResults_JComboBox.getSelected();
	__command.setCommandParameter ( "ReportFile", ReportFile );
	__command.setCommandParameter ( "OutputFolder", OutputFolder );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "IfOutputFolderDoesNotExist", IfOutputFolderDoesNotExist );
	__command.setCommandParameter ( "ListInResults", ListInResults );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, SplitStateModReport_Command command )
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
    	"Split a StateMod report file (.xdd, .xre) into separate files, one file per model node." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"This is useful when publishing datasets on the web." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Filenames can use the notation ${Property} to use global processor properties." ),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that the file name be relative to the working directory, which is:"),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("    " + __working_dir),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Report file:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ReportFile_JTextField = new JTextField ( 40 );
	__ReportFile_JTextField.setToolTipText("Path to report file, can include ${Property}.");
	__ReportFile_JTextField.addKeyListener ( this );
    // Report file layout fights back with other rows so put in its own panel
	JPanel ReportFile_JPanel = new JPanel();
	ReportFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(ReportFile_JPanel, __ReportFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browseReport_JButton = new SimpleJButton ( "...", this );
	__browseReport_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(ReportFile_JPanel, __browseReport_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__pathReport_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(ReportFile_JPanel, __pathReport_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, ReportFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output folder:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputFolder_JTextField = new JTextField ( 40 );
    __OutputFolder_JTextField.setToolTipText("Path to output folder, can include ${Property}, default to report folder.");
    __OutputFolder_JTextField.addKeyListener ( this );
    // Output file layout fights back with other rows so put in its own panel
	JPanel OutputFolder_JPanel = new JPanel();
	OutputFolder_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(OutputFolder_JPanel, __OutputFolder_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browseOutputFolder_JButton = new SimpleJButton ( "...", this );
	__browseOutputFolder_JButton.setToolTipText("Browse for folder");
    JGUIUtil.addComponent(OutputFolder_JPanel, __browseOutputFolder_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__pathOutputFolder_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(OutputFolder_JPanel, __pathOutputFolder_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, OutputFolder_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

   JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField(30);
	__OutputFile_JTextField.setToolTipText("Output file in the output folder.  Use NODEID as placeholder for the model node ID.");
	__OutputFile_JTextField.addKeyListener ( this );
   JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Required - output file name (see tooltip)."), 
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   JGUIUtil.addComponent(main_JPanel, new JLabel ( "If output folder does not exist?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfOutputFolderDoesNotExist_JComboBox = new SimpleJComboBox ( false );
	List<String> notFoundChoices = new ArrayList<String>();
	notFoundChoices.add ( "" );	// Default
	notFoundChoices.add ( __command._Create );
	notFoundChoices.add ( __command._Warn );
	notFoundChoices.add ( __command._Fail );
	__IfOutputFolderDoesNotExist_JComboBox.setData(notFoundChoices);
	__IfOutputFolderDoesNotExist_JComboBox.select ( 0 );
	__IfOutputFolderDoesNotExist_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __IfOutputFolderDoesNotExist_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Optional - action if output folder does not exist (default=" + __command._Warn + ")."), 
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "List output in results?:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ListInResults_JComboBox = new SimpleJComboBox ( false );
    __ListInResults_JComboBox.add ( "" );
    __ListInResults_JComboBox.add ( __command._False );
    __ListInResults_JComboBox.add ( __command._True );
    __ListInResults_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ListInResults_JComboBox,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ( "Optional - list unzipped file(s) in results (default=" + __command._True + ")." ), 
        2, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 60 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.addKeyListener ( this );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 7, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " command" );
	
	// Refresh the contents...
    refresh ();

    pack();
    JGUIUtil.center( this );
	// Dialogs do not need to be resizable...
	setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{   refresh();
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
{	String routine = getClass().getSimpleName() + ".refresh";
	String ReportFile = "";
	String OutputFolder = "";
	String OutputFile = "";
	String IfOutputFolderDoesNotExist = "";
	String ListInResults = "";
    PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
        parameters = __command.getCommandParameters();
		ReportFile = parameters.getValue ( "ReportFile" );
		OutputFolder = parameters.getValue ( "OutputFolder" );
		OutputFile = parameters.getValue ( "OutputFile" );
		IfOutputFolderDoesNotExist = parameters.getValue ( "IfOutputFolderDoesNotExist" );
		ListInResults = parameters.getValue ( "ListInResults" );
		if ( ReportFile != null ) {
			__ReportFile_JTextField.setText ( ReportFile );
		}
        if ( OutputFolder != null ) {
            __OutputFolder_JTextField.setText ( OutputFolder );
        }
        if ( OutputFile != null ) {
            __OutputFile_JTextField.setText ( OutputFile );
        }
		if ( JGUIUtil.isSimpleJComboBoxItem(__IfOutputFolderDoesNotExist_JComboBox, IfOutputFolderDoesNotExist,JGUIUtil.NONE, null, null ) ) {
			__IfOutputFolderDoesNotExist_JComboBox.select ( IfOutputFolderDoesNotExist );
		}
		else {
            if ( (IfOutputFolderDoesNotExist == null) ||	IfOutputFolderDoesNotExist.equals("") ) {
				// New command...select the default...
				__IfOutputFolderDoesNotExist_JComboBox.select ( 0 );
			}
			else {	// Bad user command...
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\n"+
				"IfOutputFolderDoesNotExist parameter \"" +	IfOutputFolderDoesNotExist +
				"\".  Select a\n value or Cancel." );
			}
		}
        if ( (ListInResults == null) || (ListInResults.length() == 0) ) {
            // Select default...
            __ListInResults_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ListInResults_JComboBox,
                ListInResults, JGUIUtil.NONE, null, null ) ) {
                __ListInResults_JComboBox.select ( ListInResults );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "ListInResults \"" + ListInResults + "\" parameter.  Select a\ndifferent value or Cancel." );
            }
        }
	}
	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	ReportFile = __ReportFile_JTextField.getText().trim();
	OutputFolder = __OutputFolder_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	IfOutputFolderDoesNotExist = __IfOutputFolderDoesNotExist_JComboBox.getSelected();
	ListInResults = __ListInResults_JComboBox.getSelected();
	PropList props = new PropList ( __command.getCommandName() );
	props.add ( "ReportFile=" + ReportFile );
	props.add ( "OutputFolder=" + OutputFolder );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "IfOutputFolderDoesNotExist=" + IfOutputFolderDoesNotExist );
	props.add ( "ListInResults=" + ListInResults );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __pathReport_JButton != null ) {
		if ( (ReportFile != null) && !ReportFile.isEmpty() ) {
			__pathReport_JButton.setEnabled ( true );
			File f = new File ( ReportFile );
			if ( f.isAbsolute() ) {
				__pathReport_JButton.setText ( __RemoveWorkingDirectory );
				__pathReport_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
		    	__pathReport_JButton.setText ( __AddWorkingDirectory );
		    	__pathReport_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathReport_JButton.setEnabled(false);
		}
	}
    if ( __pathOutputFolder_JButton != null ) {
		if ( (OutputFolder != null) && !OutputFolder.isEmpty() ) {
			__pathOutputFolder_JButton.setEnabled ( true );
			File f = new File ( OutputFolder );
			if ( f.isAbsolute() ) {
				__pathOutputFolder_JButton.setText ( __RemoveWorkingDirectory );
				__pathOutputFolder_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathOutputFolder_JButton.setText ( __AddWorkingDirectory );
            	__pathOutputFolder_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathOutputFolder_JButton.setEnabled(false);
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