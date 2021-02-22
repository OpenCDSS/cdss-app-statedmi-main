// SetCropPatternTSFromList_JDialog - Editor for the SetCropPatternTSFromList() command.

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

package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the SetCropPatternTSFromList() command.
*/
@SuppressWarnings("serial")
public class SetCropPatternTSFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private boolean __ok = false;	// Indicate whether OK has been pressed
private JTextArea __command_JTextArea=null;// For command
private JTextField __ListFile_JTextField = null;// List file
private JTextField __ID_JTextField = null;
private SimpleJComboBox __IDCol_JComboBox = null;
private JTextField __SetStart_JTextField = null;
private JTextField __SetEnd_JTextField = null;
private SimpleJComboBox __YearCol_JComboBox = null;
private SimpleJComboBox __CropTypeCol_JComboBox = null;
private SimpleJComboBox __AreaCol_JComboBox = null;
private SimpleJComboBox __IrrigationMethodCol_JComboBox = null;
private SimpleJComboBox __SupplyTypeCol_JComboBox = null;
private SimpleJComboBox __ProcessWhen_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private SetCropPatternTSFromList_Command __command = null;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCropPatternTSFromList_JDialog (JFrame parent, SetCropPatternTSFromList_Command command)
{	super(parent, true);
	initialize (parent, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
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
		fc.setDialogTitle("Select List File");
		fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Crop Pattern Time Series List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", "Crop Pattern Time Series List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Crop Pattern Time Series List File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName();
			String path = fc.getSelectedFile().getPath();
			
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				// Convert path to relative path by default.
				try {
					__ListFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
			__ListFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __ListFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __ListFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __ListFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
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
	String ID = __ID_JTextField.getText().trim();
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String YearCol = __YearCol_JComboBox.getSelected();
	String IDCol = __IDCol_JComboBox.getSelected();
	String CropTypeCol = __CropTypeCol_JComboBox.getSelected();
	String AreaCol = __AreaCol_JComboBox.getSelected();
	String ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	String IrrigationMethodCol = __IrrigationMethodCol_JComboBox.getSelected();
	String SupplyTypeCol = __SupplyTypeCol_JComboBox.getSelected();

	PropList props = new PropList ( "" );
	
	if ( ListFile.length() > 0 ) {
		props.set ( "ListFile", ListFile );
	}
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( SetStart.length() > 0 ) {
		props.set ( "SetStart", SetStart );
	}
	if ( SetEnd.length() > 0 ) {
		props.set ( "SetEnd", SetEnd );
	}
	if ( YearCol.length() > 0 ) {
		props.set ( "YearCol", YearCol );
	}
	if ( IDCol.length() > 0 ) {
		props.set ( "IDCol", IDCol );
	}
	if ( CropTypeCol.length() > 0 ) {
		props.set ( "CropTypeCol", CropTypeCol );
	}
	if ( AreaCol.length() > 0 ) {
		props.set ( "AreaCol", AreaCol );
	}
	if ( ProcessWhen.length() > 0 ) {
		props.set ( "ProcessWhen", ProcessWhen );
	}
	if ( IrrigationMethodCol.length() > 0 ) {
		props.set ( "IrrigationMethodCol", IrrigationMethodCol );
	}
	if ( SupplyTypeCol.length() > 0 ) {
		props.set ( "SupplyTypeCol", SupplyTypeCol );
	}
	__error_wait = false;
	try {
		// This will warn the user
		__command.checkCommandParameters( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning will have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	
	String ListFile = __ListFile_JTextField.getText().trim();
	String ID = __ID_JTextField.getText().trim();
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String YearCol = __YearCol_JComboBox.getSelected();
	String IDCol = __IDCol_JComboBox.getSelected();
	String CropTypeCol = __CropTypeCol_JComboBox.getSelected();
	String AreaCol = __AreaCol_JComboBox.getSelected();
	String ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	String IrrigationMethodCol = __IrrigationMethodCol_JComboBox.getSelected();
	String SupplyTypeCol = __SupplyTypeCol_JComboBox.getSelected();

	__command.setCommandParameter( "ListFile", ListFile );
	__command.setCommandParameter( "ID", ID );
	__command.setCommandParameter ( "SetStart", SetStart);
	__command.setCommandParameter ( "SetEnd", SetEnd );
	__command.setCommandParameter ( "YearCol", YearCol );
	__command.setCommandParameter ( "IDCol", IDCol );
	__command.setCommandParameter ( "CropTypeCol", CropTypeCol );
	__command.setCommandParameter ( "AreaCol", AreaCol );
	__command.setCommandParameter ( "ProcessWhen", ProcessWhen );
	__command.setCommandParameter ( "IrrigationMethodCol", IrrigationMethodCol );
	__command.setCommandParameter ( "SupplyTypeCol", SupplyTypeCol );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, SetCropPatternTSFromList_Command command) {
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);
	
    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets crop pattern time series data, using the CU Location ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
       	"If ProcessWhen=Now, supplied data will be applied when the command is processed."),
     	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The crop pattern data will be reset to new values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"All crops for a location and year are set to zero before setting new data for a year."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
       	"If ProcessWhen=WithParcels, " +
       	"data will supplement HydroBase data when ReadCropPatternTSFromHydroBase() is processed."),
       	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"A comma-delimited list file is used to supply data, with" +
		" values being set one of the following ways:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    1) If the set start and end years are specified and a year " +
		"column is not specified, the file values are applied to each year in the set period."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"    2) If a year column is specified, year and corresponding values "+
		"are read from the list file.  The set period limits the years that are processed."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The supply type is a legacy feature and is not used when ReadCropPatternTSFromParcels() is use."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile_JPanel = new JPanel();
	InputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile_JPanel, __ListFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, InputFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU Location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField ("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU Location(s) to set (use * for wildcard)."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set start (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetStart_JTextField = new JTextField (10);
	__SetStart_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __SetStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - starting year to set data (default=output period)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set end (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetEnd_JTextField = new JTextField (10);
	__SetEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetEnd_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - ending year to set data (default=output period)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    List<String> column_Vector = new Vector<String>(100);
    List<String> column2_Vector = new Vector<String>(101);
	column2_Vector.add ( "" );
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
		column2_Vector.add ( "" + i );
	}

    JGUIUtil.addComponent(main_JPanel,new JLabel("CU location ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IDCol_JComboBox = new SimpleJComboBox(false);
	__IDCol_JComboBox.setData ( column_Vector );
	__IDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - column in file for CU location ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Year column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__YearCol_JComboBox = new SimpleJComboBox(false);
	__YearCol_JComboBox.setData ( column2_Vector );
	__YearCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __YearCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column in file for year."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Crop type column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropTypeCol_JComboBox = new SimpleJComboBox(false);
	__CropTypeCol_JComboBox.setData ( column2_Vector );
	__CropTypeCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __CropTypeCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column in file for crop type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Area (ACRE):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AreaCol_JComboBox = new SimpleJComboBox(false);
	__AreaCol_JComboBox.setData ( column2_Vector );
	__AreaCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __AreaCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column in file for crop area."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Irrigation method column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IrrigationMethodCol_JComboBox = new SimpleJComboBox(false);
	__IrrigationMethodCol_JComboBox.setData ( column2_Vector );
	__IrrigationMethodCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IrrigationMethodCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column in file for irrigation method (column containg SPRINKLER, FLOOD)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Supply type column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SupplyTypeCol_JComboBox = new SimpleJComboBox(false);
	__SupplyTypeCol_JComboBox.setData ( column2_Vector );
	__SupplyTypeCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __SupplyTypeCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column in file for supply type (column containing " +
		__command._Ground + " or " + __command._Surface + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Process when?:"),
	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> ProcessWhen_Vector = new Vector<String>(3);
    ProcessWhen_Vector.add("");
    ProcessWhen_Vector.add(__command._Now);
	ProcessWhen_Vector.add(__command._WithParcels);
	__ProcessWhen_JComboBox = new SimpleJComboBox(false);
	__ProcessWhen_JComboBox.setData ( ProcessWhen_Vector );
	__ProcessWhen_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ProcessWhen_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Optional - when to process the data (default=" + __command._Now + ")."),
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

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
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
			response (true);
		}
	}
	else {
		// One of the combo boxes...
		refresh();
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
{	String routine = "setCropPatternTSFromList.refresh";
	String ListFile = "";
	String ID = "";
	String IDCol = "";
	String SetStart = "";
	String SetEnd = "";
	String YearCol = "";
	String CropTypeCol = "";
	String AreaCol = "";
	String ProcessWhen = "";
	String IrrigationMethodCol = "";
	String SupplyTypeCol = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		ListFile = props.getValue ( "ListFile" );
		ID = props.getValue ( "ID" );
		IDCol = props.getValue ( "IDCol" );
		SetStart = props.getValue ( "SetStart" );
		SetEnd = props.getValue ( "SetEnd" );
		YearCol = props.getValue ( "YearCol" );
		CropTypeCol = props.getValue ( "CropTypeCol" );
		AreaCol = props.getValue ( "AreaCol" );
		ProcessWhen = props.getValue ( "ProcessWhen" );
		IrrigationMethodCol = props.getValue ( "IrrigationMethodCol" );
		SupplyTypeCol = props.getValue ( "SupplyTypeCol" );
		if ( ID != null ) {
			__ID_JTextField.setText (ID);
		}
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( SetStart != null ) {
			__SetStart_JTextField.setText(SetStart);
		}
		if ( SetEnd != null ) {
			__SetEnd_JTextField.setText(SetEnd);
		}
		if ( YearCol == null ) {
			// Select default...
			__YearCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__YearCol_JComboBox, YearCol, JGUIUtil.NONE, null, null )) {
				__YearCol_JComboBox.select(YearCol);
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid YearCol " +
				"value \"" + YearCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IDCol == null ) {
			// Select default...
			__IDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDCol_JComboBox, IDCol, JGUIUtil.NONE, null, null )) {
				__IDCol_JComboBox.select(IDCol);
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid IDCol " +
				"value \"" + IDCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( CropTypeCol == null ) {
			// Select default...
			__CropTypeCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__CropTypeCol_JComboBox, CropTypeCol, JGUIUtil.NONE, null,null)){
				__CropTypeCol_JComboBox.select(CropTypeCol);
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid CropTypeCol " +
				"value \"" + CropTypeCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AreaCol == null ) {
			// Select default...
			__AreaCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__AreaCol_JComboBox, AreaCol, JGUIUtil.NONE, null, null )){
				__AreaCol_JComboBox.select(AreaCol);
			}
			else {	Message.printWarning ( 2, routine, "Existing command references an invalid AreaCol " +
				"value \""+ AreaCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ProcessWhen == null ) {
			// Select default...
			__ProcessWhen_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__ProcessWhen_JComboBox, ProcessWhen, JGUIUtil.NONE, null, null )){
				__ProcessWhen_JComboBox.select(	ProcessWhen);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an ProcessWhen value \"" +
				ProcessWhen + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IrrigationMethodCol == null ) {
			// Select default...
			__IrrigationMethodCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IrrigationMethodCol_JComboBox, IrrigationMethodCol, JGUIUtil.NONE, null, null )){
				__IrrigationMethodCol_JComboBox.select(	IrrigationMethodCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an IrrigationMethodCol value \"" +
				IrrigationMethodCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( SupplyTypeCol == null ) {
			// Select default...
			__SupplyTypeCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__SupplyTypeCol_JComboBox, SupplyTypeCol, JGUIUtil.NONE, null, null )){
				__SupplyTypeCol_JComboBox.select(SupplyTypeCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an SupplyTypeCol value \"" +
				SupplyTypeCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	ID = __ID_JTextField.getText().trim();
	SetStart = __SetStart_JTextField.getText().trim();
	SetEnd = __SetEnd_JTextField.getText().trim();
	YearCol = __YearCol_JComboBox.getSelected();
	IDCol = __IDCol_JComboBox.getSelected();
	CropTypeCol = __CropTypeCol_JComboBox.getSelected();
	AreaCol = __AreaCol_JComboBox.getSelected();
	ProcessWhen = __ProcessWhen_JComboBox.getSelected();
	IrrigationMethodCol = __IrrigationMethodCol_JComboBox.getSelected();
	SupplyTypeCol = __SupplyTypeCol_JComboBox.getSelected();
	
	props.add ( "ListFile=" + ListFile );
	props.add ( "ID=" + ID );
	props.add ( "SetStart=" + SetStart);
	props.add ( "SetEnd=" + SetEnd );
	props.add ( "YearCol=" + YearCol );
	props.add ( "IDCol=" + IDCol );
	props.add ( "CropTypeCol=" + CropTypeCol );
	props.add ( "AreaCol=" + AreaCol );
	props.add ( "ProcessWhen=" + ProcessWhen );
	props.add ( "IrrigationMethodCol=" + IrrigationMethodCol );
	props.add ( "SupplyTypeCol=" + SupplyTypeCol );
	
	__command_JTextArea.setText( __command.toString(props) );
	
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		if ( (ListFile != null) && !ListFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( ListFile );
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
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	
	__ok = ok;	// Save to be returned by ok()
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			Message.printStatus(2, "", "error_wait=true");
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
	response (false);
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
