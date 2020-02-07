// MergeListFileColumns_JDialog - merge contents of list file columns

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

// ----------------------------------------------------------------------------
// mergeListFileColumns_JDialog - merge contents of list file columns
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2005-11-18	Steven A. Malers, RTi	Initial version - copy and modify
//					readFromList_JDialog.
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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

@SuppressWarnings("serial")
public class MergeListFileColumns_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToListFile = "Add Working Directory To List File";
private final String __RemoveWorkingDirectoryFromListFile = "Remove Working Directory From List File";

private final String __AddWorkingDirectoryToOutputFile = "Add Working Directory To Output File";
private final String __RemoveWorkingDirectoryFromOutputFile = "Remove Working Directory From Output File";

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea = null;// For command
private JTextField __ListFile_JTextField = null;// List file
private JTextField __OutputFile_JTextField = null;// Output list file
private JTextField __Columns_JTextField = null;	// Columns to merge 
private JTextField __NewColumnName_JTextField = null;// New merged column
private JTextField __SimpleMergeFormat_JTextField = null;// Format widths for parts - integer for %s or 0N for zero-padded integers
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __browse2_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __path2_JButton = null;
private String __working_dir = null;	
private List<String> __command_Vector = null;
private String __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
*/
public MergeListFileColumns_JDialog ( JFrame parent, PropList props, List<String> command )
{	super(parent, true);
	initialize ( parent, props, command );
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
		fc.setDialogTitle("Select List File");
		fc.addChoosableFileFilter( new SimpleFileFilter("csv", 
			"List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", 
			"List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", 
			"List File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__ListFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __browse2_JButton ) {
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

		fc.setDialogTitle("Specify List File to Write");
		SimpleFileFilter sff = new SimpleFileFilter("txt", 
			"List File");
		fc.addChoosableFileFilter(sff);
		SimpleFileFilter cff = new SimpleFileFilter("csv", 
			"List File");
		fc.addChoosableFileFilter(cff);
		fc.setFileFilter(sff);

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__OutputFile_JTextField.setText(path);
			refresh();
		}	
	}
	else if ( o == __cancel_JButton ) {
		response (0);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "MergeListFileColumns");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (1);
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals(
			__AddWorkingDirectoryToListFile)) {
			__ListFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__ListFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals(
			__RemoveWorkingDirectoryFromListFile)) {
			try {	__ListFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir,
				__ListFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command + "_JDialog",
				"Error converting file to relative path.");
			}
		}
		refresh ();
	}
	else if ( o == __path2_JButton ) {
		if (__path2_JButton.getText().equals(
			__AddWorkingDirectoryToOutputFile)) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__OutputFile_JTextField.getText()));
		}
		else if (__path2_JButton.getText().equals(
			__RemoveWorkingDirectoryFromOutputFile)) {
			try {	__OutputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir,
				__OutputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command + "_JDialog",
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
{	String ListFile = __ListFile_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Columns = __Columns_JTextField.getText().trim();
	String NewColumnName = __NewColumnName_JTextField.getText().trim();
	// TODO SAM Evaluate use
	//String SimpleMergeFormat =
		//__SimpleMergeFormat_JTextField.getText().trim();
	String warning = "";
	__error_wait = false;
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	if ( ListFile.length() == 0 ) {
		warning += "\nA list file must be specified." +
			"  Correct or Cancel.";
	}
	else {	try {	String adjusted_path = IOUtil.adjustPath (
				__working_dir, ListFile);
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
				warning +=
				"\nThe list file does not exist:\n" +
				"    " + adjusted_path + "\n" +
		  		"Correct or Cancel.";
			}
			f = null;
		}
		catch ( Exception e ) {
			warning +=
			"\nThe working directory:\n" +
			"    \"" + __working_dir + "\"\ncannot be adjusted using:\n" +
			"    \"" + ListFile + "\".\n" +
			"Correct the file or Cancel.";
		}
	}
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory for the file does not exist, warn the
	// user...
	if ( OutputFile.length() == 0 ) {
		warning += "\nAn output file must be specified." +
			"  Correct or Cancel.";
	}
	else {	try {	String adjusted_path = IOUtil.adjustPath (
			__working_dir, OutputFile);
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				warning +=
				"\nThe directory does not exist:\n" +
				"    " + f.getParent() + "\n" +
		  		"Correct or Cancel.";
			}
			f = null;
		}
		catch ( Exception e ) {
			warning +=
			"\nThe working directory:\n" +
			"    \"" + __working_dir +
			"\"\ncannot be adjusted using:\n" +
			"    \"" + OutputFile + "\".\n" +
			"Correct the file or Cancel.";
		}
	}
	// Revisit SAM 2005-11-18
	// Check the columns for numbers
	if ( Columns.length() == 0 ) {
		warning += "\nColumns need to be specified." +
			"  Correct or Cancel.";
	}
	if ( NewColumnName.length() == 0 ) {
		warning += "\nA new column name needs to be specified." +
			"  Correct or Cancel.";
	}
	// Revisit SAM 2005-11-18
	// Check the format.
	if ( warning.length() > 0 ) {
		Message.printWarning (1, __command + "_JDialog", warning );
		__error_wait = true;
	}
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
	__command_Vector = null;
	__ok_JButton = null;
	__path_JButton = null;
	__path2_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the 
command.
*/
public List<String> getText () {
	if ((__command_Vector != null) && ((__command_Vector.size() == 0) ||
		__command_Vector.get(0).equals(""))) {
		return null;
	}
	return __command_Vector;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Vector of String containing the command.
*/
private void initialize ( JFrame parent, PropList props, List<String> command )
{	__command_Vector = command;
	__working_dir = props.getValue ("WorkingDir");
	__command = "mergeListFileColumns";
	String title = "Edit " + __command + "() Command";

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
	"This command merges columns in a list file, creating a new column" +
	" in a new list file."),
	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
	"This is useful, for example, when station identifiers need to be " +
	"created from data in multiple columns."),
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
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify as comma-separated numbers."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("NewColumnName:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NewColumnName_JTextField = new JTextField (10);
	__NewColumnName_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __NewColumnName_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"New column will be added at end."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Merge format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SimpleMergeFormat_JTextField = new JTextField (10);
	__SimpleMergeFormat_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __SimpleMergeFormat_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"For example 2,5 or 02,05 to pad with zeros."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

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
			__RemoveWorkingDirectoryFromListFile, this);
		button_JPanel.add (__path_JButton);
		__path2_JButton = new SimpleJButton(
			__RemoveWorkingDirectoryFromOutputFile, this);
		button_JPanel.add (__path2_JButton);
	}
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

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
mergeListFileColumns(ListFile="X",OutputFile="X",Columns="X,X",
NewColumnName="X",SimpleMergeFormat="X,X")
</pre>
*/
private void refresh ()
{	String routine = __command + ".refresh";
	String ListFile = "";
	String OutputFile = "";
	String Columns = "";
	String NewColumnName = "";
	String SimpleMergeFormat = "";
	__error_wait = false;
	if (__first_time) {
		__first_time = false;
		List<String> v = StringUtil.breakStringList (
			__command_Vector.get(0).trim(),"()",
			StringUtil.DELIM_SKIP_BLANKS );
		PropList props = null;
		if ( (v != null) && (v.size() > 1) ) {
			props = PropList.parse (
				(String)v.get(1), routine, "," );
		}
		else {	props = new PropList ( routine );
		}
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
			__SimpleMergeFormat_JTextField.setText ( SimpleMergeFormat );
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Columns = __Columns_JTextField.getText().trim();
	NewColumnName = __NewColumnName_JTextField.getText().trim();
	SimpleMergeFormat = __SimpleMergeFormat_JTextField.getText().trim();
	StringBuffer b = new StringBuffer ();
	if ( ListFile.length() > 0 ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( OutputFile.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( Columns.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Columns=\"" + Columns + "\"" );
	}
	if ( NewColumnName.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewColumnName=\"" + NewColumnName + "\"" );
	}
	if ( SimpleMergeFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SimpleMergeFormat=\"" + SimpleMergeFormat + "\"" );
	}
	__command_JTextArea.setText( __command + "(" + b.toString() + ")");
	__command_Vector.clear();
	__command_Vector.add (__command_JTextArea.getText());
	// Check the path and determine what the label on the path button should
	// be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (ListFile);
		if (f.isAbsolute()) {
			__path_JButton.setText (
			__RemoveWorkingDirectoryFromListFile);
		}
		else {	__path_JButton.setText (
			__AddWorkingDirectoryToListFile);
		}
	}
	if (__path2_JButton != null) {
		__path2_JButton.setEnabled (true);
		File f = new File (OutputFile);
		if (f.isAbsolute()) {
			__path2_JButton.setText (
			__RemoveWorkingDirectoryFromOutputFile);
		}
		else {	__path2_JButton.setText (
			__AddWorkingDirectoryToOutputFile);
		}
	}
}

/**
Return the command as a Vector of String.
@return returns the command text or null if no command.
*/
public List<String> response (int status) {
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

} // End mergeListFileColumns_JDialog
