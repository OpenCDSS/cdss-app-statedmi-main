package rti.tscommandprocessor.commands.util;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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

import java.io.File;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

public class MergeListFileColumns_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToListFile = "Add Working Directory To List File";
private final String __RemoveWorkingDirectoryFromListFile = "Remove Working Directory From List File";

private final String __AddWorkingDirectoryToOutputFile = "Add Working Directory To Output File";
private final String __RemoveWorkingDirectoryFromOutputFile = "Remove Working Directory From Output File";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JFrame __parent_JFrame = null; // Parent JFrame
private JTextArea __command_JTextArea=null; // For command
private JTextField __ListFile_JTextField = null; // List file
private JTextField __OutputFile_JTextField = null; // Output list file
private JTextField __Columns_JTextField = null; // Columns to merge 
private JTextField __NewColumnName_JTextField = null; // New merged column
private JTextField __SimpleMergeFormat_JTextField = null;
	// Format widths for parts - integer for %s or 0N for zero-padded integers
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __browse2_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __path2_JButton = null;
private String __working_dir = null;	
private MergeListFileColumns_Command __command = null;
private boolean __runnable = false;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param runnable If true, the command can be run from the dialog, as a Tool.
*/
public MergeListFileColumns_JDialog ( JFrame parent, MergeListFileColumns_Command command, boolean runnable )
{	super(parent, true);
	initialize ( parent, command, runnable );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();
	String routine = __command.getCommandName();

	if ( o == __browse_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select List File");
		SimpleFileFilter cff = new SimpleFileFilter("csv", "List File");
		fc.addChoosableFileFilter(cff);
		SimpleFileFilter sff = new SimpleFileFilter("lst", "List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", "List File") );
		fc.setFileFilter(cff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__ListFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __browse2_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}

		fc.setDialogTitle("Specify List File to Write");
		SimpleFileFilter sff = new SimpleFileFilter("txt", "List File");
		fc.addChoosableFileFilter(sff);
		SimpleFileFilter cff = new SimpleFileFilter("csv", "List File");
		fc.addChoosableFileFilter(cff);
		fc.setFileFilter(cff);

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__OutputFile_JTextField.setText(path);
			refresh();
		}	
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			if ( __runnable ) {
				// Run the command...
				try {
				    commitEdits ();
					JGUIUtil.setWaitCursor ( __parent_JFrame, true );
					__command.runCommand ( 0 );
					JGUIUtil.setWaitCursor(	__parent_JFrame, false );
				}
				catch ( Exception e ) {
					JGUIUtil.setWaitCursor(	__parent_JFrame, false );
					Message.printWarning ( 1,
					__command.getCommandName() + "_JDialog", "There was an error running the command.");
					Message.printWarning ( 3, routine, e );
				}
			}
			else {
			    // Command has been edited...
				response ( true );
			}
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals( __AddWorkingDirectoryToListFile)) {
			__ListFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __ListFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectoryFromListFile)) {
			try {
			    __ListFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __ListFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command.getCommandName() + "_JDialog", "Error converting file to relative path.");
			}
		}
		refresh ();
	}
	else if ( o == __path2_JButton ) {
		if (__path2_JButton.getText().equals( __AddWorkingDirectoryToOutputFile)) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText()));
		}
		else if (__path2_JButton.getText().equals( __RemoveWorkingDirectoryFromOutputFile)) {
			try {
			    __OutputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __OutputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command.getCommandName() + "_JDialog", "Error converting file to relative path.");
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
	String ListFile = __ListFile_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Columns = __Columns_JTextField.getText().trim();
	String NewColumnName = __NewColumnName_JTextField.getText().trim();
	String SimpleMergeFormat = __SimpleMergeFormat_JTextField.getText().trim();
	__error_wait = false;

	if ( ListFile.length() > 0 ) {
		props.set ( "ListFile", ListFile );
	}
	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
	if ( Columns.length() > 0 ) {
		props.set ( "Columns", Columns );
	}
	if ( NewColumnName.length() > 0 ) {
		props.set ( "NewColumnName", NewColumnName );
	}
	if ( SimpleMergeFormat.length() > 0 ) {
		props.set ( "SimpleMergeFormat", SimpleMergeFormat );
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
{	String ListFile = __ListFile_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Columns = __Columns_JTextField.getText().trim();
	String NewColumnName = __NewColumnName_JTextField.getText().trim();
	String SimpleMergeFormat = __SimpleMergeFormat_JTextField.getText().trim();
	__command.setCommandParameter ( "ListFile", ListFile );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "Columns", Columns );
	__command.setCommandParameter ( "NewColumnName", NewColumnName );
	__command.setCommandParameter ( "SimpleMergeFormat", SimpleMergeFormat);
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ListFile_JTextField = null;
	__OutputFile_JTextField = null;
	__browse_JButton = null;
	__browse2_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__path_JButton = null;
	__path2_JButton = null;
	__parent_JFrame = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
@param runnable If true, the command can be run from the dialog, as a Tool.
*/
private void initialize ( JFrame parent, MergeListFileColumns_Command command, boolean runnable )
{	__parent_JFrame = parent;
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (CommandProcessor)processor, __command );
	__runnable = runnable;

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
	String name = "command";
	if ( runnable ) {
		name = "tool";
	}
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"This " + name+" merges columns in a list file, creating a new column in a new list file."),
	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
	"This is useful, for example, when station identifiers need to be created from data in multiple columns."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns should be delimited by commas (user-specified" +
		" delimiters will be added in the future)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the files be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ListFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (35);
	__OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse2_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse2_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Columns:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Columns_JTextField = new JTextField (10);
	__Columns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Columns_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - specify as comma-separated numbers."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("NewColumnName:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NewColumnName_JTextField = new JTextField (10);
	__NewColumnName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __NewColumnName_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (	"Required - new column that will be added at end."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Merge format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SimpleMergeFormat_JTextField = new JTextField (10);
	__SimpleMergeFormat_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SimpleMergeFormat_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - (e.g., 2,5 or 02,05 to pad with zeros)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The above command can be copied for batch processing."),
		1, ++y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if (__working_dir != null) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromListFile, this);
		button_JPanel.add (__path_JButton);
		__path2_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromOutputFile, this);
		button_JPanel.add (__path2_JButton);
	}
	if ( runnable ) {
		__cancel_JButton = new SimpleJButton("Cancel", this);
		__cancel_JButton.setToolTipText ("Close window without running tool." );
		button_JPanel.add (__cancel_JButton);
		__ok_JButton = new SimpleJButton("Run Tool", this);
		button_JPanel.add (__ok_JButton);
		__ok_JButton.setToolTipText ("Run tool and keep window open." );
	}
	else {
	    __cancel_JButton = new SimpleJButton("Cancel", this);
		button_JPanel.add (__cancel_JButton);
		__cancel_JButton.setToolTipText ("Close window without saving changes." );
		__ok_JButton = new SimpleJButton("OK", this);
		button_JPanel.add (__ok_JButton);
		__ok_JButton.setToolTipText ("Close window and save changes to command." );
	}

	if ( runnable ) {
		setTitle ( "Merge List File Columns");
	}
	else {
	    setTitle ( "Edit " + __command.getCommandName() + "() Command");
	}
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();	// Sets the __path_JButton status
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
			response ( true );
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
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String ListFile = "";
	String OutputFile = "";
	String Columns = "";
	String NewColumnName = "";
	String SimpleMergeFormat = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		ListFile = props.getValue ( "ListFile" );
		OutputFile = props.getValue ( "OutputFile" );
		Columns = props.getValue ( "Columns" );
		NewColumnName = props.getValue ( "NewColumnName" );
		SimpleMergeFormat = props.getValue ( "SimpleMergeFormat" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText ( ListFile );
		}
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
		if ( Columns != null ) {
			__Columns_JTextField.setText ( Columns );
		}
		if ( NewColumnName != null ) {
			__NewColumnName_JTextField.setText ( NewColumnName );
		}
		if ( SimpleMergeFormat != null ) {
			__SimpleMergeFormat_JTextField.setText (
			SimpleMergeFormat );
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Columns = __Columns_JTextField.getText().trim();
	NewColumnName = __NewColumnName_JTextField.getText().trim();
	SimpleMergeFormat = __SimpleMergeFormat_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "ListFile=" + ListFile );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "Columns=" + Columns );
	props.add ( "NewColumnName=" + NewColumnName );
	props.add ( "SimpleMergeFormat=" + SimpleMergeFormat );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (ListFile);
		if (f.isAbsolute()) {
			__path_JButton.setText (
			__RemoveWorkingDirectoryFromListFile);
		}
		else {
		    __path_JButton.setText (
			__AddWorkingDirectoryToListFile);
		}
	}
	if (__path2_JButton != null) {
		__path2_JButton.setEnabled (true);
		File f = new File (OutputFile);
		if (f.isAbsolute()) {
			__path2_JButton.setText ( __RemoveWorkingDirectoryFromOutputFile);
		}
		else {
		    __path2_JButton.setText ( __AddWorkingDirectoryToOutputFile);
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
public void windowClosing(WindowEvent event) {
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}