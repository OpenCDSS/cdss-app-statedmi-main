// ----------------------------------------------------------------------------
// readAgStatsTSFromDateValue_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-03-01	Steven A. Malers, RTi	Initial version - copy and modify
//					readCULocationsFromList_JDialog.
// 2005-10-10	SAM, RTi		Use a text area for the command.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

import java.util.List;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class ReadAgStatsTSFromDateValue_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean		__error_wait = false;	// To track errors
private boolean		__first_time = true;	// Indicate first time display
private JTextArea	__command_JTextArea=null;// For command
private JTextField	__InputFile_JTextField = null;// List file
private SimpleJButton	__cancel_JButton = null;
private SimpleJButton	__ok_JButton = null;	
private SimpleJButton	__browse_JButton = null;
private SimpleJButton	__path_JButton = null;
private String		__working_dir = null;	
private List		__command_Vector = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
*/
public ReadAgStatsTSFromDateValue_JDialog ( JFrame parent, PropList props, List command) {
	super(parent, true);
	initialize (parent, "Edit ReadAgStatsTSFromDateValue() Command", 
		props, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
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
		fc.setDialogTitle("Select Ag Stats DateValue Time Series File");
		SimpleFileFilter sff = new SimpleFileFilter("dv", 
				"Agricultural Statistics DateValue File");
		fc.addChoosableFileFilter(sff);
		fc.addChoosableFileFilter(
				new SimpleFileFilter("txt", 
				"Agricultural Statistics DateValue File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__InputFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response (0);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (1);
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals(
			"Remove Working Directory")) {
			try {	__InputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir,
				__InputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				"readAgStatsTSFromDateValue_JDialog",
				"Error converting file to relative path.");
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
{	String file = __InputFile_JTextField.getText().trim();
	String warning = "";
	__error_wait = false;
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir, file);
		File f = new File ( adjusted_path );
		if ( !f.exists() ) {
			warning +=
			"\nThe file does not exist:\n" +
			"    " + adjusted_path + "\n" +
		  	"Correct or Cancel.";
		}
		f = null;
	}
	catch ( Exception e ) {
		warning +=
		"\nThe working directory:\n" +
		"    \"" + __working_dir + "\"\ncannot be adjusted using:\n" +
		"    \"" + file + "\".\n" +
		"Correct the file or Cancel.";
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( 1, "readAgStatsTSFromDateValue_JDialog",
		warning );
		__error_wait = true;
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__InputFile_JTextField = null;
	__browse_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command_Vector = null;
	__ok_JButton = null;
	__path_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the 
command.
*/
public List getText () {
	if ((__command_Vector != null) && ((__command_Vector.size() == 0) ||
		((String)__command_Vector.get(0)).equals(""))) {
		return null;
	}
	return __command_Vector;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title JDialog title.
@param app_PropList Properties from the application.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, String title, PropList props, 
		List command) {
	__command_Vector = command;
	__working_dir = props.getValue ("WorkingDir");

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
    	"<HTML><B>This command was used with Rio Grande Phase 4 work but is no longer " +
    	"used with standard procedures.</B><HTML>"),
    	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Annual county agricultural statistics, consisting of acreage" +
		" for crops, can be used to fill crop pattern time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads agricultural statistics time series from " +
		"a DateValue format file."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The location part of time series identifiers should be the " +
		"county, to match the region information used in CU " +
		"locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The data type part of the time series identifiers should "+
		"match the data types used in data and commands."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file be " +
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

        JGUIUtil.addComponent(main_JPanel, new JLabel(
		"AgStats DateValue file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

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

	if (__working_dir != null) {
		// Add the button to allow conversion to/from relative
		// path...
		__path_JButton = new SimpleJButton(
			"Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

	if (title != null) {
		setTitle (title);
	}
	// JDialogs do not need to be resizable...
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
			response (1);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
readAgStatsTSFromDateValue(InputFile="file")
</pre>
*/
private void refresh ()
{	String routine = "readAgStatsTSFromDateValue.refresh";
	String InputFile = "";
	__error_wait = false;
	if (__first_time) {
		__first_time = false;
		List v = StringUtil.breakStringList (
			((String)__command_Vector.get(0)).trim(),"()",
			StringUtil.DELIM_SKIP_BLANKS );
		PropList props = null;
		if ( (v != null) && (v.size() > 1) ) {
			props = PropList.parse (
				(String)v.get(1), routine, "," );
		}
		else {	props = new PropList ( routine );
		}
		InputFile = props.getValue ( "InputFile" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText (InputFile);
		}
	}
	// Regardless, reset the command from the fields...
	InputFile = __InputFile_JTextField.getText().trim();
	StringBuffer b = new StringBuffer ();
	if ( InputFile.length() > 0 ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	__command_JTextArea.setText("readAgStatsTSFromDateValue(" +
		b.toString() + ")");
	__command_Vector.clear();
	__command_Vector.add (__command_JTextArea.getText());
	// Check the path and determine what the label on the path button should
	// be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (InputFile);
		if (f.isAbsolute()) {
			__path_JButton.setText ("Remove Working Directory");
		}
		else {	__path_JButton.setText ("Add Working Directory");
		}
	}
}

/**
Return the command as a Vector of String.
@return returns the command text or null if no command.
*/
public List response (int status) {
	setVisible(false);
	dispose();
	if (status == 0) {
		// Cancel...
		__command_Vector = null;
		return null;
	}
	else {	refresh();
		if (	(__command_Vector.size() == 0) ||
			((String)__command_Vector.get(0)).equals("")) {
			return null;
		}
		return __command_Vector;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response (0);
}

// The following methods are all necessary because this class
// implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

} // End readAgStatsTSFromDateValue_JDialog
