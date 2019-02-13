// ReadCropPatternTSFromDBF_JDialog - editor for ReadCropPatternTSFromDBF command

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
// readCropPatternTSFromDBF_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-03-02	Steven A. Malers, RTi	Initial version - copy and modify
//					readCULocationsFromList_JDialog.
// 2004-03-03	SAM, RTi		Add the CU Location ID as a parameter
//					to limit processing.
// 2004-03-04	SAM, RTi		* Update to take a constructor argument
//					  so that this dialog can also be used
//					  for reading sprinkler parcel data.
//					* For sprinklers, can disable some of
//					  the inputs but also add a property
//					  ParcelIDYear to indicate which year
//					  of parcel identifiers should be used.
//					  This is necessary in case additional
//					  years of irrigated lands are developed
//					  with different parcel numbers.
// 2004-03-11	SAM, RTi		* Add ExcludedCropTypes parameter to
//					  exclude crop types.
// 2004-03-14	SAM, RTi		* Add ProcessData parameter to allow
//					  relationships to be read but not
//					  create output.  This is used when
//					  processing the IPY files.
// 2004-05-25	SAM, RTi		Add a note about supplemental
//					information.
// 2005-01-27	SAM, RTi		Simplify the
//					readSprinklerParcelsFromList() command.
// 2005-02-27	SAM, RTi		Finish the above, for use on the Rio
//					Grande data set.  Rename the command to
//					readIrrigationPracticeTSSprinklerParcels
//					FromList().
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
import RTi.Util.Table.DbaseDataTable;

public class ReadCropPatternTSFromDBF_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private final String __CropPatternDBF = "CropPatternDBF";
private final String __SprinklerList = "SprinklerList";

private final String __True = "True";
private final String __False = "False";

private boolean		__error_wait = false;	// To track errors
private boolean		__first_time = true;	// Indicate first time display
private JTextArea	__command_JTextArea=null;// For command
private JTextField	__InputFile_JTextField = null;// List file
private JTextField	__ID_JTextField = null;// List file
private JTextField	__Div_JTextField = null;
private SimpleJComboBox	__YearCol_JComboBox = null;
private JTextField	__Year_JTextField = null;
private JTextField	__ParcelIDYear_JTextField = null;
private SimpleJComboBox	__ParcelIDCol_JComboBox = null;
private SimpleJComboBox	__AreaCol_JComboBox = null;
private JTextField	__AreaUnits_JTextField = null;
private SimpleJComboBox	__CropTypeCol_JComboBox = null;
private SimpleJComboBox	__IrrigTypeCol_JComboBox = null;
private JTextField	__DitchIDCols_JTextField = null;
private JTextField	__DitchCovCols_JTextField = null;
private JTextField	__WellIDCols_JTextField = null;
private SimpleJComboBox	__GWOnlyCol_JComboBox = null;
private JTextField	__ExcludedCropTypes_JTextField = null;
private SimpleJComboBox	__ProcessData_JComboBox = null;
private SimpleJButton	__cancel_JButton = null;
private SimpleJButton	__ok_JButton = null;	
private SimpleJButton	__browse_JButton = null;
private SimpleJButton	__path_JButton = null;
private String		__working_dir = null;	
private List		__command_Vector = null;
private String		__filetype = null;	// "SprinklerList" or
						// "CropPatternDBF"
/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
@param filetype "CropPatternDBF" if a DBF file associated with a shapefile.
"SprinklerList" if a text file associated with sprinkler acreage.
*/
public ReadCropPatternTSFromDBF_JDialog (JFrame parent, PropList props,
		List command, String filetype) {
	super(parent, true);
	initialize (parent, props, command, filetype );
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
		if ( __filetype.equals(__SprinklerList) ) {
			fc.setDialogTitle("Select Sprinkler Parcels List File");
			SimpleFileFilter sff = new SimpleFileFilter("txt", 
					"Sprinkler Parcels List File");
			fc.addChoosableFileFilter( sff );
			fc.setFileFilter(sff);
		}
		else {	fc.setDialogTitle("Select Irrigated Lands DBF File");
			SimpleFileFilter sff = new SimpleFileFilter("dbf", 
					"Irrigated Lands DBase File");
			fc.addChoosableFileFilter( sff );
			fc.setFileFilter(sff);
		}

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__InputFile_JTextField.setText(path);
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
				"readCropPatternTSFromDBF_JDialog",
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
{	String InputFile = __InputFile_JTextField.getText().trim();
	String warning = "";
	__error_wait = false;
	if ( __filetype.equals("CropPatternDBF") ) {
		String ID = __ID_JTextField.getText().trim();
		if ( ID.length() == 0 ) {
			warning +=
			"\nAn identifier or pattern must be specified." +
				"  Correct or Cancel.";
		}
	}
	else {	// Sprinkler parcels...
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
			InputFile );
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
		"    \"" + InputFile + "\".\n" +
		"Correct the file or Cancel.";
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( 1, "readCropPatternTSFromDBF_JDialog",
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
@param filetype "CropPatternDBF" if a DBF file associated with a shapefile.
"SprinklerList" if a text file associated with sprinkler acreage.
*/
private void initialize (JFrame parent, PropList props, List command,
				String filetype )
{	__command_Vector = command;
	__working_dir = props.getValue ("WorkingDir");
	__filetype = filetype;

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
	if ( __filetype.equals(__SprinklerList) ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads from a comma-delimited file a list of " +
		"parcels that are irrigated by sprinklers for the given year."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
		"The HydroBase data for the Parcel ID Year are then used" +
		" to determine the area served by sprinklers."),
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
	}
	else {	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads crop pattern data from a DBase (DBF) file,"+
		" typically one associated with an ESRI shapefile."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
		"It is expected that each record in the file describes a " +
		"parcel and provides information about its water supply." ),
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
		"Crop patterns defined with " +
		"setCropPatternTS(...,ProcessWhen=WithParcels,...) will also " +
		"be processed."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
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

	if ( __filetype.equals(__SprinklerList) ) {
        	JGUIUtil.addComponent(main_JPanel,new JLabel("Sprinkler file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else {	JGUIUtil.addComponent(main_JPanel, new JLabel ("DBF file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

	if ( __filetype.equals(__CropPatternDBF) ) {
        	JGUIUtil.addComponent(main_JPanel,
			new JLabel ("CU Location ID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ID_JTextField = new JTextField("*",10);
		__ID_JTextField.addKeyListener (this);
        	JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the locations to process (use * " +
			"for wildcard)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
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

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Year_JTextField = new JTextField(10);
	__Year_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __filetype.equals(__CropPatternDBF) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Data year: specify if not a column in the DBF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	else {	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Data year."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	// Data will be reset after selecting a file
	List column_Vector = new Vector();
	column_Vector.add ( "" );	// Not available

	// Don't trust getting the year out of the parcels file...
	if ( __filetype.equals(__CropPatternDBF) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel ("Year Column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__YearCol_JComboBox = new SimpleJComboBox(false);
		__YearCol_JComboBox.setData ( column_Vector );
		__YearCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __YearCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify if a column in the DBF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	if ( __filetype.equals(__SprinklerList) ) {
        	JGUIUtil.addComponent(main_JPanel,new JLabel("Parcel ID year:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ParcelIDYear_JTextField = new JTextField(10);
		__ParcelIDYear_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __ParcelIDYear_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the year for parcel IDs."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel ID Column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	// Data will be reset after selecting a file
	__ParcelIDCol_JComboBox = new SimpleJComboBox(false);
	__ParcelIDCol_JComboBox.setData ( column_Vector );
	__ParcelIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ParcelIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __filetype.equals(__CropPatternDBF) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel ("Area Column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AreaCol_JComboBox = new SimpleJComboBox(false);
		__AreaCol_JComboBox.setData ( column_Vector );
		__AreaCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __AreaCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel ("Area Units:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AreaUnits_JTextField = new JTextField(10);
		__AreaUnits_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __AreaUnits_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Default is ACRE."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Crop Type Column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__CropTypeCol_JComboBox = new SimpleJComboBox(false);
		__CropTypeCol_JComboBox.setData ( column_Vector );
		__CropTypeCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __CropTypeCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Irrig. Type Column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__IrrigTypeCol_JComboBox = new SimpleJComboBox(false);
		__IrrigTypeCol_JComboBox.setData ( column_Vector );
		__IrrigTypeCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __IrrigTypeCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Ditch ID Columns:"), 
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DitchIDCols_JTextField = new JTextField (40);
		JGUIUtil.addComponent(main_JPanel, __DitchIDCols_JTextField,
			1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Separate column names with commas."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Ditch Cov. Columns:"), 
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DitchCovCols_JTextField = new JTextField (40);
		JGUIUtil.addComponent(main_JPanel, __DitchCovCols_JTextField,
			1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Separate column names with commas."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel,
			new JLabel("Well ID Columns:"), 
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__WellIDCols_JTextField = new JTextField (40);
		JGUIUtil.addComponent(main_JPanel, __WellIDCols_JTextField,
			1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Separate column names with commas."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Groundwater Only Column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__GWOnlyCol_JComboBox = new SimpleJComboBox(false);
		__GWOnlyCol_JComboBox.setData ( column_Vector );
		__GWOnlyCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __GWOnlyCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Data values of \"yes\" indicate well-only parcel."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel,
			new JLabel("Excluded Crop Types:"), 
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ExcludedCropTypes_JTextField = new JTextField (40);
		JGUIUtil.addComponent(main_JPanel,
			__ExcludedCropTypes_JTextField,
			1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Separate with commas."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel ("Process Data:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ProcessData_JComboBox = new SimpleJComboBox(false);
		__ProcessData_JComboBox.add ( "" );
		__ProcessData_JComboBox.add ( __False );
		__ProcessData_JComboBox.add ( __True );
		__ProcessData_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __ProcessData_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Default is true.  See documentation."),
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

	if ( filetype.equals(__SprinklerList) ) {
		setTitle( "Edit " +
		"readIrrigationPracticeTSSprinklerParcelsFromList() Command");
	}
	else {	setTitle ( "Edit readCropPatternTSFromDBF() Command" ); 
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
Populate the JComboBoxes based on the columns that are available in the DBF
file.
@param file Full path to DBF file.
*/
private void populateJComboBoxes ( String file )
{	// Open the DBase table and only read the header, closing the file after
	// the read...
	List choices = new Vector();
	choices.add ( "" );
	DataTable table;
	try {	if ( __filetype.equals(__SprinklerList) ) {
			// Read the headings from the list file...
			PropList props = new PropList ("");
			props.set ( "Delimiter=," );
			props.set ( "CommentLineIndicator=#" );
			table = DataTable.parseFile ( file, props );
		}
		else {	// Read the headings from the Dbase file...
			table = new DbaseDataTable ( file, false, false);
		}
		String [] field_names = table.getFieldNames();
		for ( int i = 0; i < field_names.length; i++ ) {
			choices.add ( field_names[i] );
		}
		table = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 1,
		"readCropPatternTSFromDBF_JDialog.populateJComboBoxes",
		"Error getting field names from \"" + file + "\"" );
		choices.add ( "UNKNOWN" );
	}
	if ( __filetype.equals(__CropPatternDBF) ) {
		__YearCol_JComboBox.setData(choices);
		__YearCol_JComboBox.select(0);
		__CropTypeCol_JComboBox.setData(choices);
		__CropTypeCol_JComboBox.select(0);
		__IrrigTypeCol_JComboBox.setData(choices);
		__IrrigTypeCol_JComboBox.select(0);
		__GWOnlyCol_JComboBox.setData(choices);
		__GWOnlyCol_JComboBox.select(0);
		__AreaCol_JComboBox.setData(choices);
		__AreaCol_JComboBox.select(0);
	}
	// Always fill these choices...
	__ParcelIDCol_JComboBox.setData(choices);
	__ParcelIDCol_JComboBox.select(0);
}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
readCropPatternTSFromDBF(DBFFile="file",ParcelIDCol=X,...)
</pre>
*/
private void refresh ()
{	String routine = "readCropPatternTSFromDBF.refresh";
	String InputFile = "";
	String ID = "";
	String Div = "";
	String Year = "";
	String YearCol = "";
	String ParcelIDYear = "";
	String ParcelIDCol = "";
	String AreaCol = "";
	String AreaUnits = "";
	String CropTypeCol = "";
	String IrrigTypeCol = "";
	String DitchIDCols = "";
	String DitchCovCols = "";
	String WellIDCols = "";
	String GWOnlyCol = "";
	String ExcludedCropTypes = "";
	String ProcessData = "";
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
		if ( __filetype.equals(__CropPatternDBF) ) {
			InputFile = props.getValue ( "DBFFile" );
		}
		else {	InputFile = props.getValue ( "ListFile" );
		}
		ID = props.getValue ( "ID" );
		Div = props.getValue ( "Div" );
		Year = props.getValue ( "Year" );
		YearCol = props.getValue ( "YearCol" );
		ParcelIDYear = props.getValue ( "ParcelIDYear" );
		ParcelIDCol = props.getValue ( "ParcelIDCol" );
		AreaCol = props.getValue ( "AreaCol" );
		AreaUnits = props.getValue ( "AreaUnits" );
		CropTypeCol = props.getValue ( "CropTypeCol" );
		IrrigTypeCol = props.getValue ( "IrrigTypeCol" );
		DitchIDCols = props.getValue ( "DitchIDCols" );
		DitchCovCols = props.getValue ( "DitchCovCols" );
		WellIDCols = props.getValue ( "WellIDCols" );
		GWOnlyCol = props.getValue ( "GWOnlyCol" );
		ExcludedCropTypes = props.getValue ( "ExcludedCropTypes" );
		ProcessData = props.getValue ( "ProcessData" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText (InputFile);
			populateJComboBoxes (
				IOUtil.getPathUsingWorkingDir(InputFile) );
		}
		if ( __filetype.equals(__CropPatternDBF) && (ID != null) ) {
			__ID_JTextField.setText(ID);
		}
		if ( Div != null ) {
			__Div_JTextField.setText(Div);
		}
		if ( Year != null ) {
			__Year_JTextField.setText (Year);
		}
		if ( __filetype.equals(__CropPatternDBF) ) {
		if ( YearCol == null) {
			// Select default...
			__YearCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__YearCol_JComboBox,
				YearCol, JGUIUtil.NONE, null, null ) ) {
				__YearCol_JComboBox.select ( YearCol );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid YearCol value \"" +
				YearCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		}
		if ( __filetype.equals(__SprinklerList) ) {
		if ( ParcelIDYear != null ) {
			__ParcelIDYear_JTextField.setText (ParcelIDYear);
		}
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
		if ( __filetype.equals(__CropPatternDBF) ) {
		if ( AreaCol == null ) {
			// Select default...
			__AreaCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__AreaCol_JComboBox,
				AreaCol, JGUIUtil.NONE, null, null ) ) {
				__AreaCol_JComboBox.select ( AreaCol );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid AreaCol value \"" +
				AreaCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AreaUnits != null ) {
			__AreaUnits_JTextField.setText ( AreaUnits );
		}
		if ( CropTypeCol == null ) {
			// Select default...
			__CropTypeCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__CropTypeCol_JComboBox,
				CropTypeCol, JGUIUtil.NONE, null, null ) ) {
				__CropTypeCol_JComboBox.select ( CropTypeCol );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid CropTypeCol value \"" +
				CropTypeCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IrrigTypeCol == null ) {
			// Select default...
			__IrrigTypeCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__IrrigTypeCol_JComboBox,
				IrrigTypeCol, JGUIUtil.NONE, null, null ) ) {
				__IrrigTypeCol_JComboBox.select ( IrrigTypeCol);
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid IrrigTypeCol value \"" +
				IrrigTypeCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( DitchIDCols != null ) {
			__DitchIDCols_JTextField.setText (DitchIDCols);
		}
		if ( DitchCovCols != null ) {
			__DitchCovCols_JTextField.setText (DitchCovCols);
		}
		if ( WellIDCols != null ) {
			__WellIDCols_JTextField.setText (WellIDCols);
		}
		if ( GWOnlyCol == null ) {
			// Select default...
			__GWOnlyCol_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__GWOnlyCol_JComboBox,
				GWOnlyCol, JGUIUtil.NONE, null, null ) ) {
				__GWOnlyCol_JComboBox.select ( GWOnlyCol);
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid GWOnlyCol value \"" +
				GWOnlyCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if (	(__ExcludedCropTypes_JTextField != null) &&
			(ExcludedCropTypes != null) ) {
			__ExcludedCropTypes_JTextField.setText(
				ExcludedCropTypes);
		}
		if ( ProcessData == null ) {
			// Select default...
			__ProcessData_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__ProcessData_JComboBox,
				ProcessData, JGUIUtil.NONE, null, null ) ) {
				__ProcessData_JComboBox.select ( ProcessData);
			}
			else {	Message.printWarning ( 1, routine,
				"Existing readCropPatternTSFromDBF() " +
				"references an invalid ProcessData value \"" +
				ProcessData +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		}
	}
	// Regardless, reset the command from the fields...
	InputFile = __InputFile_JTextField.getText().trim();
	if ( __filetype.equals(__CropPatternDBF) ) {
		ID = __ID_JTextField.getText().trim();
	}
	Div = __Div_JTextField.getText().trim();
	Year = __Year_JTextField.getText().trim();
	if ( __filetype.equals(__CropPatternDBF) ) {
		YearCol = __YearCol_JComboBox.getSelected();
	}
	if ( __filetype.equals(__SprinklerList) ) {
		ParcelIDYear = __ParcelIDYear_JTextField.getText();
	}
	ParcelIDCol = __ParcelIDCol_JComboBox.getSelected();
	if ( __filetype.equals(__CropPatternDBF) ) {
		AreaCol = __AreaCol_JComboBox.getSelected();
		AreaUnits = __AreaUnits_JTextField.getText().trim();
		if ( __filetype.equals(__CropPatternDBF) ) {
			CropTypeCol = __CropTypeCol_JComboBox.getSelected();
			IrrigTypeCol = __IrrigTypeCol_JComboBox.getSelected();
		}
		DitchIDCols = __DitchIDCols_JTextField.getText().trim();
		DitchCovCols = __DitchCovCols_JTextField.getText().trim();
		WellIDCols = __WellIDCols_JTextField.getText().trim();
		GWOnlyCol = __GWOnlyCol_JComboBox.getSelected();
		ExcludedCropTypes =
			__ExcludedCropTypes_JTextField.getText().trim();
		ProcessData = __ProcessData_JComboBox.getSelected();
	}
	StringBuffer b = new StringBuffer ();
	if ( __filetype.equals(__CropPatternDBF) ) {
		if ( InputFile.length() > 0 ) {
			b.append ( "DBFFile=\"" + InputFile + "\"" );
		}
	}
	else {	if ( InputFile.length() > 0 ) {
			b.append ( "ListFile=\"" + InputFile + "\"" );
		}
	}
	if ( __filetype.equals(__CropPatternDBF) ) {
	if ( ID.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ID=\"" + ID + "\"" );
	}
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
	if ( __filetype.equals(__CropPatternDBF) ) {
	if ( YearCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "YearCol=\"" + YearCol + "\"" );
	}
	if ( AreaCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AreaCol=\"" + AreaCol + "\"" );
	}
	if ( AreaUnits.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AreaUnits=\"" + AreaUnits + "\"" );
	}
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
	if ( __filetype.equals(__CropPatternDBF) ) {
	if ( CropTypeCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "CropTypeCol=\"" + CropTypeCol + "\"" );
	}
	if ( IrrigTypeCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IrrigTypeCol=\"" + IrrigTypeCol + "\"" );
	}
	if ( DitchIDCols.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DitchIDCols=\"" + DitchIDCols + "\"" );
	}
	if ( DitchCovCols.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DitchCovCols=\"" + DitchCovCols + "\"" );
	}
	if ( WellIDCols.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "WellIDCols=\"" + WellIDCols + "\"" );
	}
	if ( GWOnlyCol.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GWOnlyCol=\"" + GWOnlyCol + "\"" );
	}
	if ( ExcludedCropTypes.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcludedCropTypes=\"" + ExcludedCropTypes + "\"" );
	}
	if ( ProcessData.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ProcessData=" + ProcessData );
	}
	}
	if ( __filetype.equals(__CropPatternDBF) ) {
		__command_JTextArea.setText("readCropPatternTSFromDBF(" +
			b.toString() + ")");
	}
	else {	__command_JTextArea.setText(
			"readIrrigationPracticeTSSprinklerParcelsFromList(" +
			b.toString() + ")");
	}
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

} // End readCropPatternTSFromDBF_JDialog
