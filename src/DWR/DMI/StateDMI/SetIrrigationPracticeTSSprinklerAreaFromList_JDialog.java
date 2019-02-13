// SetIrrigationPracticeTSSprinklerAreaFromList_JDialog - editor for SetIrrigationPracticeTSSprinklerAreaFromList command

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
// setIrrigationPracticeTSSprinklerAreaFromList_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-03-02	Steven A. Malers, RTi	Initial version - copy and modify
//					readCropPatternTSFromDBF_JDialog.
// 2005-05-30	SAM, RTi		Add ParcelAreaCol parameter.
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
import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;

public class SetIrrigationPracticeTSSprinklerAreaFromList_JDialog
extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean		__error_wait = false;	// To track errors
private boolean		__first_time = true;	// Indicate first time display
private JTextArea	__command_JTextArea=null;// For command
private JTextField	__ListFile_JTextField = null;// List file
private JTextField	__ID_JTextField = null;// List file
private JTextField	__Div_JTextField = null;
private JTextField	__Year_JTextField = null;
private JTextField	__ParcelIDYear_JTextField = null;
private SimpleJComboBox	__ParcelIDCol_JComboBox = null;
private SimpleJComboBox	__ParcelAreaCol_JComboBox = null;
private SimpleJButton	__cancel_JButton = null;
private SimpleJButton	__ok_JButton = null;	
private SimpleJButton	__browse_JButton = null;
private SimpleJButton	__path_JButton = null;
private String		__working_dir = null;	
private List		__command_Vector = null;
private String		__command =
			"setIrrigationPracticeTSSprinklerAreaFromList";
/**
Constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
*/
public SetIrrigationPracticeTSSprinklerAreaFromList_JDialog ( JFrame parent,
								PropList props,
								List command)
{	super(parent, true);
	initialize (parent, props, command );
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
		fc.setDialogTitle("Select Sprinkler Parcels List File");
		SimpleFileFilter sff = new SimpleFileFilter("txt", 
				"Sprinkler Parcels List File");
		fc.addChoosableFileFilter( sff );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__ListFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
			populateJComboBoxes(path);
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
			__ListFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,
			__ListFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals(
			"Remove Working Directory")) {
			try {	__ListFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir,
				__ListFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, __command + "_JDialog",
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
	String warning = "";
	__error_wait = false;
	String ParcelIDYear =__ParcelIDYear_JTextField.getText().trim();
	if (	(ParcelIDYear.length() == 0) ||
		!StringUtil.isInteger(ParcelIDYear) ) {
		warning +=
		"\nThe parcel ID year must be specified as an integer."
			+ "  Correct or Cancel.";
	}
	String Year =__Year_JTextField.getText().trim();
	if (	(Year.length() == 0) || !StringUtil.isInteger(Year) ) {
		warning +=
		"\nThe year must be specified as an integer."
			+ "  Correct or Cancel.";
	}
	String Div =__Div_JTextField.getText().trim();
	if (	(Div.length() == 0) || !StringUtil.isInteger(Div) ) {
		warning +=
		"\nThe water division must be specified as an integer."
			+ "  Correct or Cancel.";
	}
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir,
			ListFile );
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
	if ( warning.length() > 0 ) {
		Message.printWarning ( 1, __command + "_JDialog.checkInput",
		warning );
		__error_wait = true;
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__ListFile_JTextField = null;
	__ID_JTextField = null;
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
private void initialize ( JFrame parent, PropList props, List command )
{	__command_Vector = command;
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
			"THIS COMMAND IS OBSOLETE AND IS USED FOR PHASE 4 RIO GRANDE WORK - " +
			"INSTEAD, SEE THE setIrrigationPracticeTSSprinklerAcreageFromList() COMMAND."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads from a comma-delimited file a list of " +
		"parcels that are irrigated by sprinklers for the given year."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
		"The HydroBase data for the Parcel ID are by default used"+
		" to determine the area served by sprinklers."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
		"If appropriate, the parcel area can be specified in the"+
		" list file (instead of HydroBase)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"It is expected that each record in the file lists a single " +
		"sprinkler-irrigated parcel." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Column positions should be specified using the column" +
		" names, not column numbers."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The column name choices will be refreshed after a file is" +
		" selected."),
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

       	JGUIUtil.addComponent(main_JPanel,new JLabel("Sprinkler list file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ListFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

       	JGUIUtil.addComponent(main_JPanel,new JLabel("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField ("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the locations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the water division for the data."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Year_JTextField = new JTextField(10);
	__Year_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Data year."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Data will be reset after selecting a file
	List column_Vector = new Vector();
	column_Vector.add ( "" );	// Not available

       	JGUIUtil.addComponent(main_JPanel,new JLabel("Parcel ID year:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ParcelIDYear_JTextField = new JTextField(10);
	__ParcelIDYear_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelIDYear_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the year for parcel IDs."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	// Data will be reset after selecting a file
	__ParcelIDCol_JComboBox = new SimpleJComboBox(false);
	__ParcelIDCol_JComboBox.setData ( column_Vector );
	__ParcelIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel area column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	// Data will be reset after selecting a file
	__ParcelAreaCol_JComboBox = new SimpleJComboBox(false);
	__ParcelAreaCol_JComboBox.setData ( column_Vector );
	__ParcelAreaCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelAreaCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Blank=get area from HydroBase using parcel ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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

	setTitle( "Edit " + __command + "() Command");
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
Populate the JComboBoxes based on the columns that are available in the input
file.
@param file Full path to input file.
*/
private void populateJComboBoxes ( String file )
{	// Open the DBase table and only read the header, closing the file after
	// the read...
	List choices = new Vector();
	choices.add ( "" );
	DataTable table;
	try {	// Read the headings from the list file...
		PropList props = new PropList ("");
		props.set ( "Delimiter=," );
		props.set ( "CommentLineIndicator=#" );
		table = DataTable.parseFile ( file, props );

		String [] field_names = table.getFieldNames();
		for ( int i = 0; i < field_names.length; i++ ) {
			choices.add ( field_names[i] );
		}
		table = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, __command +
		"_JDialog.populateJComboBoxes",
		"Error getting field names from \"" + file + "\"" );
		choices.add ( "UNKNOWN" );
	}
	// Always fill these choices...
	__ParcelIDCol_JComboBox.setData(choices);
	__ParcelIDCol_JComboBox.select(0);

	__ParcelAreaCol_JComboBox.setData(choices);
	__ParcelAreaCol_JComboBox.select(0);
}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
setIrrigationPracticeTSSprinklerAreaFromList(ListFile="file",Div=X,
Year=X,ParcelIDYear=X,ParcelIDCol=X,ParcelAreaCol=X)
</pre>
*/
private void refresh ()
{	String routine = __command + "_JDialog.refresh";
	String ListFile = "";
	String ID = "";
	String Div = "";
	String Year = "";
	// TODO SAM Evaluate use
	//String YearCol = "";
	String ParcelIDYear = "";
	String ParcelIDCol = "";
	String ParcelAreaCol = "";
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
		ListFile = props.getValue ( "ListFile" );
		ID = props.getValue ( "ID" );
		Div = props.getValue ( "Div" );
		Year = props.getValue ( "Year" );
		// TODO SAM Evaluate use
		//YearCol = props.getValue ( "YearCol" );
		ParcelIDYear = props.getValue ( "ParcelIDYear" );
		ParcelIDCol = props.getValue ( "ParcelIDCol" );
		ParcelAreaCol = props.getValue ( "ParcelAreaCol" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
			populateJComboBoxes (
				IOUtil.getPathUsingWorkingDir(ListFile) );
		}
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( Div != null ) {
			__Div_JTextField.setText(Div);
		}
		if ( Year != null ) {
			__Year_JTextField.setText (Year);
		}
		if ( ParcelIDYear != null ) {
			__ParcelIDYear_JTextField.setText (ParcelIDYear);
		}
		if ( ParcelIDCol == null ) {
			// Select default...
			__ParcelIDCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__ParcelIDCol_JComboBox,
				ParcelIDCol, JGUIUtil.NONE, null, null ) ) {
				__ParcelIDCol_JComboBox.select ( ParcelIDCol );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid ParcelIDCol value \"" +
				ParcelIDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ParcelAreaCol == null ) {
			// Select default...
			__ParcelAreaCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__ParcelAreaCol_JComboBox,
				ParcelAreaCol, JGUIUtil.NONE, null, null ) ) {
				__ParcelAreaCol_JComboBox.select (
				ParcelAreaCol );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid ParcelAreaCol value \"" +
				ParcelAreaCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	ID = __ID_JTextField.getText().trim();
	Div = __Div_JTextField.getText().trim();
	Year = __Year_JTextField.getText().trim();
	ParcelIDYear = __ParcelIDYear_JTextField.getText();
	ParcelIDCol = __ParcelIDCol_JComboBox.getSelected();
	ParcelAreaCol = __ParcelAreaCol_JComboBox.getSelected();
	StringBuffer b = new StringBuffer ();
	if ( ListFile.length() > 0 ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( ID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( Div.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=" + Div );
	}
	if ( Year.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Year=" + Year );
	}
	if ( (ParcelIDYear != null) && ParcelIDYear.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelIDYear=" + ParcelIDYear );
	}
	if ( ParcelIDCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelIDCol=\"" + ParcelIDCol + "\"");
	}
	if ( ParcelAreaCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelAreaCol=\"" + ParcelAreaCol + "\"");
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

}
