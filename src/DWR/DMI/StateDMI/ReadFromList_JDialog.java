// ReadFromList_JDialog - Command editor for Read*FromList() for StateMod data.

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
import java.util.ArrayList;
import java.util.List;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Command editor for Read*FromList() for StateMod data.  These commands only read the basic
information (ID, Name) from lists, with the expectation that the remainder of data will be
filled from HydroBase.  This is unlike the StateCU commands, which read all the data from list files.
*/
@SuppressWarnings("serial")
public class ReadFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __ListFile_JTextField = null;// List file
private SimpleJComboBox __IDCol_JComboBox = null;
private SimpleJComboBox __NameCol_JComboBox = null;
private SimpleJComboBox __RiverNodeIDCol_JComboBox = null;
private SimpleJComboBox __DailyIDCol_JComboBox = null;
private SimpleJComboBox __DiversionIDCol_JComboBox = null; // Only when reading well stations
private JTextField __Top_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadFromList_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command command to edit.
*/
public ReadFromList_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize (parent, command );
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
		if ( __command instanceof ReadStreamGageStationsFromList_Command ) {
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Stream Gage Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Stream Gage Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Stream Gage Stations List File") );
			fc.setFileFilter(sff);
		}
		else if (__command instanceof ReadDiversionStationsFromList_Command){
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Diversion Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Diversion Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Diversion Stations List File") );
			fc.setFileFilter(sff);
		}
		else if (__command instanceof ReadReservoirStationsFromList_Command){
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Reservoir Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Reservoir Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Reservoir Stations List File") );
			fc.setFileFilter(sff);
		}
		else if (__command instanceof ReadInstreamFlowStationsFromList_Command){
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Instream Flow Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Instream Flow Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Instream Flow Stations List File") );
			fc.setFileFilter(sff);
		}
		else if (__command instanceof ReadWellStationsFromList_Command){
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Well Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Well Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Well Stations List File") );
			fc.setFileFilter(sff);
		}
		else if ( __command instanceof ReadStreamEstimateStationsFromList_Command ) {
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Stream Estimate Stations List File") );
			SimpleFileFilter sff = new SimpleFileFilter("lst", "Stream Estimate Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Stream Estimate Stations List File") );
			fc.setFileFilter(sff);
		}

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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String Top = __Top_JTextField.getText().trim();
	
	__error_wait = false;
	
	if (ListFile.length() > 0) {
		props.set("ListFile", ListFile);
	}
	if (IDCol.length() > 0 && !IDCol.equals("")) {
		props.set("IDCol", IDCol);
	}
	if (NameCol.length() > 0 && !NameCol.equals("")) {
		props.set("NameCol", NameCol);
	}
	if ( __command instanceof ReadStreamGageStationsFromList_Command ||
		__command instanceof ReadStreamEstimateStationsFromList_Command ) {
		String RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
		if (RiverNodeIDCol.length() > 0 && !RiverNodeIDCol.equals("")) {
			props.set("RiverNodeIDCol", RiverNodeIDCol);
		}
		String DailyIDCol = __DailyIDCol_JComboBox.getSelected();
		if (DailyIDCol.length() > 0 && !DailyIDCol.equals("")) {
			props.set("DailyIDCol", DailyIDCol);
		}
	}
	if ( __command instanceof ReadWellStationsFromList_Command ) {
		String DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
		if (DiversionIDCol.length() > 0 && !DiversionIDCol.equals("")) {
			props.set("DiversionIDCol", DiversionIDCol);
		}
	}
	if (Top.length() > 0 && !Top.equals("")) {
		props.set("Top", Top);
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
private void commitEdits() {
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String Top = __Top_JTextField.getText().trim();

	__command.setCommandParameter("ListFile", ListFile);
	__command.setCommandParameter("IDCol", IDCol);
	__command.setCommandParameter("NameCol", NameCol);
	if ( __command instanceof ReadStreamGageStationsFromList_Command ||
		__command instanceof ReadStreamEstimateStationsFromList_Command ) {
		String RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
		__command.setCommandParameter("RiverNodeIDCol", RiverNodeIDCol);
		String DailyIDCol = __DailyIDCol_JComboBox.getSelected();
		__command.setCommandParameter("DailyIDCol", DailyIDCol);
	}
	else if ( __command instanceof ReadWellStationsFromList_Command ) {
		String DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
		__command.setCommandParameter("DiversionIDCol", DiversionIDCol);
	}
	__command.setCommandParameter("Top", Top );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (ReadFromList_Command)command;
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
	if ( __command instanceof ReadStreamGageStationsFromList_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads stream gage stations from a list file containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Stream gage stations indicate locations where historical flow observations are available."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	}
	else if ( __command instanceof ReadDiversionStationsFromList_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads diversion stations from a list file containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Diversion stations indicate locations where water is diverted from a river, lake, or reservoir."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadReservoirStationsFromList_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads reservoir stations from a list file containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Reservoir stations indicate locations where water can be stored for use at a later date."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadInstreamFlowStationsFromList_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads instream flow stations from a list file " +
		"containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Instream flow stations indicate locations where surface " +
		"water flow can be associated with a minimum flow constraint."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellStationsFromList_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads well stations from a list file containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"A well station can be one of the following:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"  1) location where only groundwater supply is used to meet demand"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"  2) location where groundwater supply supplements surface water (diversion)" +
		" supply to meet demand."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"      In this case, see also diversion stations that are supplemented by well pumping -"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"      both diversion and well stations must be defined and the Diversion ID must be specified" +
		" for the well (see below)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadStreamEstimateStationsFromList_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads stream estimate stations from a list file containing columns of information."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"Stream estimate stations indicate locations where stream flow"+
		" is estimated (not historically measured)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns should be delimited by commas (user-specified delimiters will be added in the future)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Identifiers and names (and in some cases other information) can be read - most " +
		"subsequent commands only need a list of identifiers."),
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
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel ListFile_JPanel = new JPanel();
	ListFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(ListFile_JPanel, __ListFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(ListFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(ListFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, ListFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> column_Vector = new ArrayList<>(100);
	column_Vector.add ( "" );	// Not available
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
	}
	__IDCol_JComboBox = new SimpleJComboBox(false);
	__IDCol_JComboBox.setData ( column_Vector );
	__IDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Required - column (1+) for ID."),
	3, y, 4, 1, 1.0, 0.00, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NameCol_JComboBox = new SimpleJComboBox(false);
	__NameCol_JComboBox.setData ( column_Vector );
	__NameCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __NameCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - column (1+) for name."),
    	3, y, 4, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	
	if ( __command instanceof ReadStreamGageStationsFromList_Command ||
		__command instanceof ReadStreamEstimateStationsFromList_Command ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ("River node ID column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__RiverNodeIDCol_JComboBox = new SimpleJComboBox(false);
		__RiverNodeIDCol_JComboBox.setData ( column_Vector );
		__RiverNodeIDCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __RiverNodeIDCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"Optional - column (1+) for river network ID."),
    		3, y, 4, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
    	
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Daily ID column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DailyIDCol_JComboBox = new SimpleJComboBox(false);
		__DailyIDCol_JComboBox.setData ( column_Vector );
		__DailyIDCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __DailyIDCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"Optional - column (1+) for daily ID."),
    		3, y, 4, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	}
	
	if ( __command instanceof ReadWellStationsFromList_Command ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Diversion ID column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DiversionIDCol_JComboBox = new SimpleJComboBox(false);
		__DiversionIDCol_JComboBox.setData ( column_Vector );
		__DiversionIDCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __DiversionIDCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"Optional - column (1+) to link the well to a diversion location."),
    		3, y, 4, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	}
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Top (limit rows):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Top_JTextField = new JTextField(10);
	__Top_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Top_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - limit read to top N rows (default=read all)."),
		3, y, 4, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

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

	setTitle("Edit " + __command.getCommandName() + "() Command");
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
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = __command + ".refresh";
	String ListFile = "";
	String IDCol = "";
	String NameCol = "";
	String RiverNodeIDCol = "";
	String DailyIDCol = "";
	String DiversionIDCol = "";
	String Top = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command
		props = __command.getCommandParameters();
		ListFile = props.getValue ( "ListFile" );
		IDCol = props.getValue ( "IDCol" );
		NameCol = props.getValue ( "NameCol" );
		DailyIDCol = props.getValue ( "DailyIDCol" );
		RiverNodeIDCol = props.getValue ( "RiverNodeIDCol" );
		DiversionIDCol = props.getValue ( "DiversionIDCol" );
		Top = props.getValue ( "Top" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( IDCol == null ) {
			// Select default...
			__IDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDCol_JComboBox, IDCol, JGUIUtil.NONE, null, null ) ) {
				__IDCol_JComboBox.select ( IDCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid IDCol value \"" + IDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( NameCol == null ) {
			// Select default...
			__NameCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__NameCol_JComboBox, NameCol, JGUIUtil.NONE, null, null ) ) {
				__NameCol_JComboBox.select ( NameCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid NameCol value \"" +
				NameCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __RiverNodeIDCol_JComboBox != null ) {
			if ( RiverNodeIDCol == null ) {
				// Select default...
				__RiverNodeIDCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__RiverNodeIDCol_JComboBox, RiverNodeIDCol, JGUIUtil.NONE, null, null ) ) {
					__RiverNodeIDCol_JComboBox.select ( RiverNodeIDCol );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid RiverNodeIDCol value \"" +
					RiverNodeIDCol + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __DailyIDCol_JComboBox != null ) {
			if ( DailyIDCol == null ) {
				// Select default...
				__DailyIDCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__DailyIDCol_JComboBox, DailyIDCol, JGUIUtil.NONE, null, null ) ) {
					__DailyIDCol_JComboBox.select ( DailyIDCol );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid DailyIDCol value \"" +
					DailyIDCol + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __DiversionIDCol_JComboBox != null ) {
			if ( DiversionIDCol == null ) {
				// Select default...
				__DiversionIDCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__DiversionIDCol_JComboBox, DiversionIDCol, JGUIUtil.NONE, null, null ) ) {
					__DiversionIDCol_JComboBox.select ( DiversionIDCol );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid DiversionIDCol value \"" +
					DiversionIDCol + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( Top != null ) {
			__Top_JTextField.setText (Top);
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	IDCol = __IDCol_JComboBox.getSelected();
	NameCol = __NameCol_JComboBox.getSelected();
	Top = __Top_JTextField.getText().trim();
	if ( __RiverNodeIDCol_JComboBox != null ) {
		RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
	}
	if ( __DailyIDCol_JComboBox != null ) {
		DailyIDCol = __DailyIDCol_JComboBox.getSelected();
	}
	if ( __DiversionIDCol_JComboBox != null ) {
		DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
	}
	props = new PropList(__command.getCommandName());
	props.add("ListFile=" + ListFile);
	props.add("IDCol=" + IDCol);
	props.add("NameCol=" + NameCol);
	if ( __RiverNodeIDCol_JComboBox != null ) {
		props.add("RiverNodeIDCol=" + RiverNodeIDCol);
	}
	if ( __DailyIDCol_JComboBox != null ) {
		props.add("DailyIDCol=" + DailyIDCol);
	}
	if ( __DiversionIDCol_JComboBox != null ) {
		props.add("DiversionIDCol=" + DiversionIDCol);
	}
	props.add("Top=" + Top);
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		if ( (ListFile != null) && !ListFile.isEmpty() ) {
			__path_JButton.setEnabled (true);
			File f = new File (ListFile);
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
@param ok if false, then the edit is cancelled.  If true, the edit is committed and the dialog is closed.
*/
public void response ( boolean ok ) {
	__ok = ok;
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
