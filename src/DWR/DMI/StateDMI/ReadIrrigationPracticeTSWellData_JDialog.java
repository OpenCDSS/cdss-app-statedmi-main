// ReadIrrigationPracticeTSWellData_JDialog - editor for ReadIrrigationPracticeSWellData command

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
// readIrrigationPracticeTSWellData_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-03-12	Steven A. Malers, RTi	Initial version - copy and modify
//					readCULocationsFromList_JDialog.
// 2004-11-01	SAM, RTi		* Rename readIrrigationPracticeWellData
//					  FromList_JDialog to
//					  readIrrigationPracticeWellData_JDialog
//					* Allow reading from a list or
//					  HydroBase.
// 2005-02-27	SAM, RTi		* Rename class readIrrigationPracticeTS
//					  WellData_JDialog to be more consistent
//					  with other classes.
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

public class ReadIrrigationPracticeTSWellData_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean		__error_wait = false;	// To track errors
private boolean		__first_time = true;	// Indicate first time display
private JTextArea	__command_JTextArea=null;// For command
private JTextField	__ListFile_JTextField = null;// List file
private JTextField	__ParcelIDYear_JTextField = null;// Year for Parcel IDs
private JTextField	__Div_JTextField = null;// Division for Parcel IDs
private SimpleJComboBox	__ParcelIDCol_JComboBox = null;
private SimpleJComboBox	__YieldCol_JComboBox = null;
private SimpleJComboBox	__PermitDateCol_JComboBox = null;
private SimpleJComboBox	__AppropriationDateCol_JComboBox = null;
private SimpleJButton	__cancel_JButton = null;
private SimpleJButton	__ok_JButton = null;	
private SimpleJButton	__browse_JButton = null;
private SimpleJButton	__path_JButton = null;
private String		__working_dir = null;	
private List		__command_Vector = null;
private String		__command = null;
private boolean		__read_list = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
@param read_list true if reading a list file, false if reading from HydroBase.
*/
public ReadIrrigationPracticeTSWellData_JDialog (	JFrame parent,
							PropList props,
							List command,
							boolean read_list )
{	super(parent, true);
	initialize ( parent, props, command, read_list );
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
				"Irrigation Practice Well Data List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", 
				"Irrigation Practice Well Data List File" );
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", 
				"Irrigation Practice Well Data List File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__ListFile_JTextField.setText(path);
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
{	String warning = "";
	__error_wait = false;
	if ( __read_list ) {
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	String file = __ListFile_JTextField.getText().trim();
	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir, file);
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
		"    \"" + file + "\".\n" +
		"Correct the file or Cancel.";
	}
	}
	String Div =__Div_JTextField.getText().trim();
	if (	(Div.length() == 0) || !StringUtil.isInteger(Div) ) {
		warning +=
		"\nThe water division must be specified as an integer."
			+ "  Correct or Cancel.";
	}
	if ( __read_list ) {
	String ParcelIDYear =__ParcelIDYear_JTextField.getText().trim();
	if (	(ParcelIDYear.length() == 0) ||
		!StringUtil.isInteger(ParcelIDYear) ) {
		warning +=
		"\nThe parcel ID year must be specified as an integer."
			+ "  Correct or Cancel.";
	}
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
@param read_list true if reading a list file, false if reading from HydroBase.
*/
private void initialize (	JFrame parent, PropList props, List command,
				boolean read_list )
{	__command_Vector = command;
	__working_dir = props.getValue ("WorkingDir");
	__read_list = read_list;
	if ( __read_list ) {
		__command = "readIrrigationPracticeTSWellDataFromList";
	}
	else {	__command = "readIrrigationPracticeTSWellDataFromHydroBase";
	}
	String title = "Edit " + __command + "() command";

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
		"Irrigation practice data for areas that use groundwater " +
		"supply include acreage irrigated by groundwater,"),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"and the maximum pumping rate for wells."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __read_list ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads and processes well/parcel information" +
		" from a list file, to determine the above information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns should be delimited by commas (user-specified" +
		" delimiters will be added in the future)."),
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
	}
	else {	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads and processes well/parcel information" +
		" from HydroBase, to determine the above information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __read_list ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ListFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the water division for the data."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __read_list ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel ID year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ParcelIDYear_JTextField = new JTextField(10);
	__ParcelIDYear_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelIDYear_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Year for parcel IDs in the list file."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List column_Vector = new Vector(100);
	column_Vector.add ( "" );	// Not available
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
	}
	__ParcelIDCol_JComboBox = new SimpleJComboBox(false);
	__ParcelIDCol_JComboBox.setData ( column_Vector );
	__ParcelIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Yield column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__YieldCol_JComboBox = new SimpleJComboBox(false);
	__YieldCol_JComboBox.setData ( column_Vector );
	__YieldCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __YieldCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Prorated well yield for parcel (ACFT per month)." ), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Permit date column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PermitDateCol_JComboBox = new SimpleJComboBox(false);
	__PermitDateCol_JComboBox.setData ( column_Vector );
	__PermitDateCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __PermitDateCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Permit date if well has permit." ), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Appropriation date column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AppropriationDateCol_JComboBox = new SimpleJComboBox(false);
	__AppropriationDateCol_JComboBox.setData ( column_Vector );
	__AppropriationDateCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __AppropriationDateCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Appropriation date if well has right." ), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

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
readIrrigationPracticeTSWellDataFromList(ListFile="file",Div=X,ParcelIDYear=X,
ParcelIDCol=X,YieldCol=X,PermitDateCol=X,AppropriationDateCol=X)

readIrrigationPracticeTSWellDataFromHydroBase(Div=X)
</pre>
*/
private void refresh ()
{	String routine = __command + "_JDialog.refresh";
	String ListFile = "";
	String Div = "";
	String ParcelIDYear = "";
	String ParcelIDCol = "";
	String YieldCol = "";
	String PermitDateCol = "";
	String AppropriationDateCol = "";
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
		Div = props.getValue ( "Div" );
		ParcelIDYear = props.getValue ( "ParcelIDYear" );
		ParcelIDCol = props.getValue ( "ParcelIDCol" );
		YieldCol = props.getValue ( "YieldCol" );
		PermitDateCol = props.getValue ( "PermitDateCol" );
		AppropriationDateCol = props.getValue ( "AppropriationDateCol");
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( Div != null ) {
			__Div_JTextField.setText (Div);
		}
		if ( ParcelIDYear != null ) {
			__ParcelIDYear_JTextField.setText (ParcelIDYear);
		}
		if ( __read_list ) {
		if ( ParcelIDCol == null ) {
			// Select default...
			__ParcelIDCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__ParcelIDCol_JComboBox,
				ParcelIDCol, JGUIUtil.NONE, null, null ) ) {
				__ParcelIDCol_JComboBox.select ( ParcelIDCol );
			}
			else {	Message.printWarning ( 1, routine, "Existing " +
				__command + "() " +
				"references an ParcelIDCol value \"" +
				ParcelIDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( YieldCol == null ) {
			// Select default...
			__YieldCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__YieldCol_JComboBox,
				YieldCol, JGUIUtil.NONE, null, null ) ) {
				__YieldCol_JComboBox.select ( YieldCol );
			}
			else {	Message.printWarning ( 1, routine, "Existing " +
				__command + "() " +
				"references an YieldCol value \"" +
				YieldCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PermitDateCol == null ) {
			// Select default...
			__PermitDateCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__PermitDateCol_JComboBox,
				PermitDateCol, JGUIUtil.NONE, null, null ) ) {
				__PermitDateCol_JComboBox.select(PermitDateCol);
			}
			else {	Message.printWarning ( 1, routine, "Existing " +
				__command + "() " +
				"references an PermitDateCol value \"" +
				PermitDateCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AppropriationDateCol == null ) {
			// Select default...
			__AppropriationDateCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__AppropriationDateCol_JComboBox,
				AppropriationDateCol, JGUIUtil.NONE, null,
				null ) ) {
				__AppropriationDateCol_JComboBox.select(
				AppropriationDateCol);
			}
			else {	Message.printWarning ( 1, routine, "Existing " +
				__command + "() " +
				"references an AppropriationDateCol value \"" +
				AppropriationDateCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		}
	}
	// Regardless, reset the command from the fields...
	if ( __read_list ) {
		ListFile = __ListFile_JTextField.getText().trim();
	}
	Div = __Div_JTextField.getText().trim();
	if ( __read_list ) {
		ParcelIDYear = __ParcelIDYear_JTextField.getText().trim();
		ParcelIDCol = __ParcelIDCol_JComboBox.getSelected();
		YieldCol = __YieldCol_JComboBox.getSelected();
		PermitDateCol = __PermitDateCol_JComboBox.getSelected();
		AppropriationDateCol =
		__AppropriationDateCol_JComboBox.getSelected();
	}
	StringBuffer b = new StringBuffer ();
	if ( __read_list && (ListFile.length() > 0) ) {
		b.append ( "ListFile=\"" + ListFile + "\"" );
	}
	if ( Div.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=" + Div );
	}
	if ( __read_list && (ParcelIDYear.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelIDYear=" + ParcelIDYear );
	}
	if ( __read_list && (ParcelIDCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelIDCol=" + ParcelIDCol );
	}
	if ( __read_list && (YieldCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "YieldCol=" + YieldCol );
	}
	if ( __read_list && (PermitDateCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PermitDateCol=" + PermitDateCol );
	}
	if ( __read_list && (AppropriationDateCol.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AppropriationDateCol=" + AppropriationDateCol );
	}
	__command_JTextArea.setText(__command + "(" + b.toString() + ")");
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

} // End readIrrigationPracticeTSWellData_JDialog
