// FillAndSetDiversionAndWellStationsFromList_JDialog - Editor for the FillDiversionStationsFromList() and FillWellStationsFromList() commands.

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
Editor for the FillDiversionStationsFromList() and FillWellStationsFromList() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetDiversionAndWellStationsFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
// Common...
private JTextField __ListFile_JTextField = null;// List file
private SimpleJComboBox __IDCol_JComboBox = null;
private SimpleJComboBox __NameCol_JComboBox = null;
private SimpleJComboBox __Delim_JComboBox = null;
private SimpleJComboBox __MergeDelim_JComboBox = null;
// Diversion stations...
private SimpleJComboBox __RiverNodeIDCol_JComboBox = null;
private SimpleJComboBox __OnOffCol_JComboBox = null;
private SimpleJComboBox __CapacityCol_JComboBox = null;
private SimpleJComboBox __ReplaceResOptionCol_JComboBox = null;
private SimpleJComboBox __DailyIDCol_JComboBox = null;
private SimpleJComboBox __UserNameCol_JComboBox = null;
private SimpleJComboBox __DemandTypeCol_JComboBox = null;
private SimpleJComboBox __IrrigatedAcresCol_JComboBox = null;
private SimpleJComboBox __UseTypeCol_JComboBox = null;
private SimpleJComboBox __DemandSourceCol_JComboBox = null;
private SimpleJComboBox __EffAnnualCol_JComboBox = null;
private SimpleJComboBox __EffMonthlyCol_JComboBox = null;
// Well stations...
private SimpleJComboBox __DiversionIDCol_JComboBox = null;
private SimpleJComboBox __AdminNumShiftCol_JComboBox = null;

private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private FillAndSetDiversionAndWellStationsFromList_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetDiversionAndWellStationsFromList_JDialog ( JFrame parent, FillAndSetDiversionAndWellStationsFromList_Command command )
{	super(parent, true);
	initialize ( parent, command );
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
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select List File");
		SimpleFileFilter sff = null;
		if ( (__command instanceof FillDiversionStationsFromList_Command) ||
			(__command instanceof SetDiversionStationsFromList_Command) ) {
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Diversion Stations List File") );
			sff = new SimpleFileFilter("lst", "Diversion Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Diversion Stations List File") );
		}
		else if((__command instanceof FillWellStationsFromList_Command) ||
			(__command instanceof SetWellStationsFromList_Command) ) {
			fc.addChoosableFileFilter( new SimpleFileFilter("csv", "Diversion Stations List File") );
			sff = new SimpleFileFilter("lst", "Diversion Stations List File");
			fc.addChoosableFileFilter( sff );
			fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Diversion Stations List File") );
		}
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String Delim = __Delim_JComboBox.getSelected();
	String MergeDelim = __MergeDelim_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
	String OnOffCol = __OnOffCol_JComboBox.getSelected();
	String CapacityCol = __CapacityCol_JComboBox.getSelected();
	String DailyIDCol = __DailyIDCol_JComboBox.getSelected();
	String DemandTypeCol = __DemandTypeCol_JComboBox.getSelected();
	String IrrigatedAcresCol = __IrrigatedAcresCol_JComboBox.getSelected();
	String UseTypeCol = __UseTypeCol_JComboBox.getSelected();
	String DemandSourceCol = __DemandSourceCol_JComboBox.getSelected();
	String EffAnnualCol = __EffAnnualCol_JComboBox.getSelected();
	String EffMonthlyCol = __EffMonthlyCol_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (ListFile.length() > 0) {
		props.set("ListFile", ListFile);
	}
	if (IDCol.length() > 0) {
		props.set("IDCol", IDCol);
	}
	if (Delim.length() > 0) {
		props.set("Delim", Delim);
	}
	if (MergeDelim.length() > 0) {
		props.set("MergeDelim", MergeDelim);
	}
	if (NameCol.length() > 0) {
		props.set("NameCol", NameCol);
	}
	if (RiverNodeIDCol.length() > 0) {
		props.set("RiverNodeIDCol", RiverNodeIDCol);
	}
	if (OnOffCol.length() > 0) {
		props.set("OnOffCol", OnOffCol);
	}
	if (CapacityCol.length() > 0) {
		props.set("CapacityCol", CapacityCol);
	}
	if (DailyIDCol.length() > 0) {
		props.set("DailyIDCol", DailyIDCol);
	}
	if (DemandTypeCol.length() > 0) {
		props.set("DemandTypeCol", DemandTypeCol);
	}
	if (IrrigatedAcresCol.length() > 0) {
		props.set("IrrigatedAcresCol", IrrigatedAcresCol);
	}
	if (UseTypeCol.length() > 0) {
		props.set("UseTypeCol", UseTypeCol);
	}
	if (DemandSourceCol.length() > 0) {
		props.set("DemandSourceCol", DemandSourceCol);
	}
	if (EffAnnualCol.length() > 0) {
		props.set("EffAnnualCol", EffAnnualCol);
	}
	if (EffMonthlyCol.length() > 0) {
		props.set("EffMonthlyCol", EffMonthlyCol);
	}
	if (IfNotFound.length() > 0) {
		props.set("IfNotFound", IfNotFound);
	}
	
	if ( __ReplaceResOptionCol_JComboBox != null ) {
		String ReplaceResOptionCol = __ReplaceResOptionCol_JComboBox.getSelected();
		if (ReplaceResOptionCol.length() > 0 ) {
			props.set("ReplaceResOptionCol", ReplaceResOptionCol);
		}
	}
	if ( __UserNameCol_JComboBox != null ) {
		String UserNameCol = __UserNameCol_JComboBox.getSelected();
		if (UserNameCol.length() > 0 ) {
			props.set("UserNameCol", UserNameCol);
		}
	}
	if ( __AdminNumShiftCol_JComboBox != null ) {
		String AdminNumShiftCol = __AdminNumShiftCol_JComboBox.getSelected();
		if (AdminNumShiftCol.length() > 0 ) {
			props.set("AdminNumShiftCol", AdminNumShiftCol);
		}
	}
	if ( __DiversionIDCol_JComboBox != null ) {
		String DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
		if (DiversionIDCol.length() > 0 ) {
			props.set("DiversionIDCol", DiversionIDCol);
		}
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
private void commitEdits()
{
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String Delim = __Delim_JComboBox.getSelected();
	String MergeDelim = __MergeDelim_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
	String OnOffCol = __OnOffCol_JComboBox.getSelected();
	String CapacityCol = __CapacityCol_JComboBox.getSelected();
	String DailyIDCol = __DailyIDCol_JComboBox.getSelected();
	String DemandTypeCol = __DemandTypeCol_JComboBox.getSelected();
	String IrrigatedAcresCol = __IrrigatedAcresCol_JComboBox.getSelected();
	String UseTypeCol = __UseTypeCol_JComboBox.getSelected();
	String DemandSourceCol = __DemandSourceCol_JComboBox.getSelected();
	String EffAnnualCol = __EffAnnualCol_JComboBox.getSelected();
	String EffMonthlyCol = __EffMonthlyCol_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter("ListFile", ListFile);
	__command.setCommandParameter("IDCol", IDCol);
	__command.setCommandParameter("Delim", Delim);
	__command.setCommandParameter("MergeDelim", MergeDelim);
	__command.setCommandParameter("NameCol", NameCol);
	__command.setCommandParameter("RiverNodeIDCol", RiverNodeIDCol);
	__command.setCommandParameter("OnOffCol", OnOffCol);
	__command.setCommandParameter("CapacityCol", CapacityCol);
	__command.setCommandParameter("DailyIDCol", DailyIDCol);
	__command.setCommandParameter("DemandTypeCol", DemandTypeCol);
	__command.setCommandParameter("IrrigatedAcresCol", IrrigatedAcresCol);
	__command.setCommandParameter("UseTypeCol", UseTypeCol);
	__command.setCommandParameter("DemandSourceCol", DemandSourceCol);
	__command.setCommandParameter("EffAnnualCol", EffAnnualCol);
	__command.setCommandParameter("EffMonthlyCol", EffMonthlyCol);
	__command.setCommandParameter("IfNotFound", IfNotFound);
	
	if ( __ReplaceResOptionCol_JComboBox != null ) {
		String ReplaceResOptionCol = __ReplaceResOptionCol_JComboBox.getSelected();
		__command.setCommandParameter("ReplaceResOptionCol", ReplaceResOptionCol);
	}
	if ( __UserNameCol_JComboBox != null ) {
		String UserNameCol = __UserNameCol_JComboBox.getSelected();
		__command.setCommandParameter("UserNameCol", UserNameCol);
	}
	if ( __AdminNumShiftCol_JComboBox != null ) {
		String AdminNumShiftCol = __AdminNumShiftCol_JComboBox.getSelected();
		__command.setCommandParameter("AdminNumShiftCol", AdminNumShiftCol);
	}
	if ( __DiversionIDCol_JComboBox != null ) {
		String DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
		__command.setCommandParameter("DiversionIDCol", DiversionIDCol);
	}
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, FillAndSetDiversionAndWellStationsFromList_Command command )
{	__command = (FillAndSetDiversionAndWellStationsFromList_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );
	String action = "";	// Command action
	if ( (__command instanceof FillDiversionStationsFromList_Command) ||
		(__command instanceof FillWellStationsFromList_Command)	) {
		action = "fills missing";
	}
	else {
		action = "edits/sets";
	}
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
	String idlabel = "ID column:";
	if ( (__command instanceof FillDiversionStationsFromList_Command) ||
		(__command instanceof SetDiversionStationsFromList_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command " + action + " data in diversion " +
		"stations, using the station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	idlabel = "Diversion stations ID column:";
	}
	else if((__command instanceof FillWellStationsFromList_Command) ||
		(__command instanceof SetWellStationsFromList_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command " + action + " data in well stations," +
		" using the station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	idlabel = "Well stations ID column:";
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Data are supplied by values in a delimited file."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	/* TODO SAM 2005-11-14
	Need to evaluate whether to support.
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The ID can contain a * wildcard pattern to match "+
		"one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	*/
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
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
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

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

	/* TODO SAM 2005-11-14 Does the ID refer to the in-memory contents or the file? ID=memory IDList=file?
        JGUIUtil.addComponent(main_JPanel,
		new JLabel ("ID(s) in file to process:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField ("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	*/

    JGUIUtil.addComponent(main_JPanel, new JLabel(idlabel),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> columnID_Vector = new Vector<String>(100);
    List<String> column_Vector = new Vector<String>(100);
	column_Vector.add ( "" );	// For no change.
	for ( int i = 1; i <= 100; i++ ) {
		columnID_Vector.add ( "" + i );
		column_Vector.add ( "" + i );
	}
	__IDCol_JComboBox = new SimpleJComboBox(false);
	__IDCol_JComboBox.setData ( columnID_Vector );
	__IDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - column (1+) for identifier."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NameCol_JComboBox = new SimpleJComboBox(false);
	__NameCol_JComboBox.setData ( column_Vector );
	__NameCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __NameCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for name."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("River node ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__RiverNodeIDCol_JComboBox = new SimpleJComboBox(false);
	__RiverNodeIDCol_JComboBox.setData ( column_Vector );
	__RiverNodeIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __RiverNodeIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for river node ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("On/Off column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OnOffCol_JComboBox = new SimpleJComboBox(false);
	__OnOffCol_JComboBox.setData ( column_Vector );
	__OnOffCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __OnOffCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for on/off switch."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent( main_JPanel,	new JLabel ("Capacity column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CapacityCol_JComboBox = new SimpleJComboBox(false);
	__CapacityCol_JComboBox.setData ( column_Vector );
	__CapacityCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __CapacityCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for capacity."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (__command instanceof FillDiversionStationsFromList_Command) ||
		(__command instanceof SetDiversionStationsFromList_Command) ) {
    	JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Replacement reservoir option column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ReplaceResOptionCol_JComboBox = new SimpleJComboBox(false);
		__ReplaceResOptionCol_JComboBox.setData ( column_Vector );
		__ReplaceResOptionCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __ReplaceResOptionCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for repl. res. option."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	if ( (__command instanceof FillWellStationsFromList_Command) ||
		(__command instanceof SetWellStationsFromList_Command) ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Administration number shift column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	__AdminNumShiftCol_JComboBox = new SimpleJComboBox(false);
    	__AdminNumShiftCol_JComboBox.setData ( column_Vector );
    	__AdminNumShiftCol_JComboBox.addItemListener (this);
    	JGUIUtil.addComponent(main_JPanel, __AdminNumShiftCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for admin. num. shift."),
    		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    	JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	__DiversionIDCol_JComboBox = new SimpleJComboBox(false);
    	__DiversionIDCol_JComboBox.setData ( column_Vector );
    	__DiversionIDCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __DiversionIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for diversion station ID."),
	    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Daily ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DailyIDCol_JComboBox = new SimpleJComboBox(false);
	__DailyIDCol_JComboBox.setData ( column_Vector );
	__DailyIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __DailyIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for daily ID."),
	   	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (__command instanceof FillDiversionStationsFromList_Command) ||
		(__command instanceof SetDiversionStationsFromList_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("User name column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__UserNameCol_JComboBox = new SimpleJComboBox(false);
		__UserNameCol_JComboBox.setData ( column_Vector );
		__UserNameCol_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __UserNameCol_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for user name."),
		   	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Demand type column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DemandTypeCol_JComboBox = new SimpleJComboBox(false);
	__DemandTypeCol_JComboBox.setData ( column_Vector );
	__DemandTypeCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __DemandTypeCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for demand type."),
	   	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Irrigated acres column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IrrigatedAcresCol_JComboBox = new SimpleJComboBox(false);
	__IrrigatedAcresCol_JComboBox.setData ( column_Vector );
	__IrrigatedAcresCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IrrigatedAcresCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for irrigated acres."),
	   	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Use type column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__UseTypeCol_JComboBox = new SimpleJComboBox(false);
	__UseTypeCol_JComboBox.setData ( column_Vector );
	__UseTypeCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __UseTypeCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for use type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Demand source column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DemandSourceCol_JComboBox = new SimpleJComboBox(false);
	__DemandSourceCol_JComboBox.setData ( column_Vector );
	__DemandSourceCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __DemandSourceCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for demand source."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency (annual) column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffAnnualCol_JComboBox = new SimpleJComboBox(false);
	__EffAnnualCol_JComboBox.setData ( column_Vector );
	__EffAnnualCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __EffAnnualCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for annual efficiency."),
	   	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency (monthly) column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffMonthlyCol_JComboBox = new SimpleJComboBox(false);
	__EffMonthlyCol_JComboBox.setData ( column_Vector );
	__EffMonthlyCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __EffMonthlyCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - first column of 12, listed Jan...Dec."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Delimiter:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> delim_Vector = new Vector<String>(5);
	delim_Vector.add ( "" );	// For default (comma)
	delim_Vector.add ( "," );
	delim_Vector.add ( __command._Space );
	delim_Vector.add ( __command._Tab );
	delim_Vector.add ( __command._Whitespace );
	__Delim_JComboBox = new SimpleJComboBox(true);	// Allow edit
	__Delim_JComboBox.setData ( delim_Vector );
	__Delim_JComboBox.addItemListener (this);
	__Delim_JComboBox.getJTextComponent().addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Delim_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - delimiter character(s) (default=\",\")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Merge delimiters:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> mergedelim_Vector = new Vector<String>(3);
	mergedelim_Vector.add ( "" );	// For default (true)
	mergedelim_Vector.add ( __command._False );
	mergedelim_Vector.add ( __command._True );
	__MergeDelim_JComboBox = new SimpleJComboBox(false);
	__MergeDelim_JComboBox.setData ( mergedelim_Vector );
	__MergeDelim_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __MergeDelim_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - treat consecutive delimiters as one (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new Vector<String>(4);
    IfNotFound_List.add("");
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - indicate action if no ID match is found (default=" + __command._Warn + ")."),
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
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
fillCULocationsFromList(ListFile="file",IDColumn=X,LatitudeCol=X,
ElevationCol=X,Region1Col=X,Region2Col=X,AWCCol=X)

fillDiversionStationsFromList(...)
fillWellStationsFromList(...)

Also set versions.
</pre>
*/
private void refresh ()
{	String routine = __command + ".refresh";
	// Shared...
	String ListFile = "";
	String IDCol = "";
	String NameCol = "";
	String Delim = "";
	String MergeDelim = "";
	// StateMod diversions...
	String RiverNodeIDCol = "";
	String OnOffCol = "";
	String CapacityCol = "";
	String ReplaceResOptionCol = "";
	String DailyIDCol = "";
	String UserNameCol = "";
	String DemandTypeCol = "";
	String IrrigatedAcresCol = "";
	String UseTypeCol = "";
	String DemandSourceCol = "";
	String EffAnnualCol = "";
	String EffMonthlyCol = "";
	// StateMod wells...
	String DiversionIDCol = "";
	String AdminNumShiftCol = "";
	String IfNotFound = "";
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		// General...
		ListFile = parameters.getValue ( "ListFile" );
		IDCol = parameters.getValue ( "IDCol" );
		NameCol = parameters.getValue ( "NameCol" );
		Delim = parameters.getValue ( "Delim" );
		MergeDelim = parameters.getValue ( "MergeDelim" );
		// Diversion stations...
		RiverNodeIDCol = parameters.getValue ( "RiverNodeIDCol" );
		OnOffCol = parameters.getValue ( "OnOffCol" );
		CapacityCol = parameters.getValue ( "CapacityCol" );
		ReplaceResOptionCol = parameters.getValue ( "ReplaceResOptionCol" );
		DailyIDCol = parameters.getValue ( "DailyIDCol" );
		UserNameCol = parameters.getValue ( "UserNameCol" );
		DemandTypeCol = parameters.getValue ( "DemandTypeCol" );
		IrrigatedAcresCol = parameters.getValue ( "IrrigatedAcresCol" );
		UseTypeCol = parameters.getValue ( "UseTypeCol" );
		DemandSourceCol = parameters.getValue ( "DemandSourceCol" );
		EffAnnualCol = parameters.getValue ( "EffAnnualCol" );
		EffMonthlyCol = parameters.getValue ( "EffMonthlyCol" );
		// StateMod wells...
		DiversionIDCol = parameters.getValue ( "DiversionIDCol" );
		AdminNumShiftCol = parameters.getValue ( "AdminNumShiftCol" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( NameCol == null ) {
			// Select default...
			__NameCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__NameCol_JComboBox, NameCol, JGUIUtil.NONE, null, null ) ) {
				__NameCol_JComboBox.select(NameCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid NameCol value \"" +
				NameCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( RiverNodeIDCol == null ) {
			// Select default...
			__RiverNodeIDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__RiverNodeIDCol_JComboBox, RiverNodeIDCol, JGUIUtil.NONE, null, null ) ) {
				__RiverNodeIDCol_JComboBox.select(RiverNodeIDCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an invalid " +
				"RiverNodeIDCol value \""+ RiverNodeIDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( OnOffCol == null ) {
			// Select default...
			__OnOffCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OnOffCol_JComboBox, OnOffCol, JGUIUtil.NONE, null, null ) ) {
				__OnOffCol_JComboBox.select(OnOffCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid OnOffCol value \"" +
				OnOffCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( CapacityCol == null ) {
			// Select default...
			__CapacityCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__CapacityCol_JComboBox, CapacityCol, JGUIUtil.NONE, null, null ) ) {
				__CapacityCol_JComboBox.select(CapacityCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid CapacityCol value \"" +
				CapacityCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __ReplaceResOptionCol_JComboBox != null ) {
			if ( ReplaceResOptionCol == null ) {
				// Select default...
				__ReplaceResOptionCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__ReplaceResOptionCol_JComboBox, ReplaceResOptionCol, JGUIUtil.NONE, null,null)){
					__ReplaceResOptionCol_JComboBox.select(	ReplaceResOptionCol);
				}
				else {
					Message.printWarning ( 2, routine, "Existing command references an " +
					"invalid ReplaceResOptionCol value \"" +
					ReplaceResOptionCol + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( DailyIDCol == null ) {
			// Select default...
			__DailyIDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DailyIDCol_JComboBox, DailyIDCol, JGUIUtil.NONE, null, null ) ) {
				__DailyIDCol_JComboBox.select(DailyIDCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid DailyIDCol value \"" +
				DailyIDCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __UserNameCol_JComboBox != null ) {
			if ( UserNameCol == null ) {
				// Select default...
				__UserNameCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__UserNameCol_JComboBox, UserNameCol, JGUIUtil.NONE, null, null ) ) {
					__UserNameCol_JComboBox.select(UserNameCol);
				}
				else {
					Message.printWarning ( 2, routine,
					"Existing command references an invalid UserNameCol value \"" +
					UserNameCol + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( DemandTypeCol == null ) {
			// Select default...
			__DemandTypeCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DemandTypeCol_JComboBox, DemandTypeCol, JGUIUtil.NONE, null, null ) ) {
				__DemandTypeCol_JComboBox.select(DemandTypeCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid DemandTypeCol value \""+
				DemandTypeCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IrrigatedAcresCol == null ) {
			// Select default...
			__IrrigatedAcresCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IrrigatedAcresCol_JComboBox, IrrigatedAcresCol, JGUIUtil.NONE, null, null )){
				__IrrigatedAcresCol_JComboBox.select(IrrigatedAcresCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an invalid " +
				"IrrigatedAcresCol value \""+IrrigatedAcresCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( UseTypeCol == null ) {
			// Select default...
			__UseTypeCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__UseTypeCol_JComboBox, UseTypeCol, JGUIUtil.NONE, null, null ) ) {
				__UseTypeCol_JComboBox.select(UseTypeCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid UseTypeCol value \""+
				UseTypeCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( DemandSourceCol == null ) {
			// Select default...
			__DemandSourceCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DemandSourceCol_JComboBox, DemandSourceCol, JGUIUtil.NONE, null, null ) ) {
				__DemandSourceCol_JComboBox.select(DemandSourceCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command references an invalid " +
				"DemandSourceCol value \""+	DemandSourceCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( EffAnnualCol == null ) {
			// Select default...
			__EffAnnualCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__EffAnnualCol_JComboBox, EffAnnualCol, JGUIUtil.NONE, null, null ) ) {
				__EffAnnualCol_JComboBox.select(EffAnnualCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid EffAnnualCol value \""+
				EffAnnualCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( EffMonthlyCol == null ) {
			// Select default...
			__EffMonthlyCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__EffMonthlyCol_JComboBox, EffMonthlyCol, JGUIUtil.NONE, null, null ) ) {
				__EffMonthlyCol_JComboBox.select(EffMonthlyCol);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid EffMonthlyCol value \""+
				EffMonthlyCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		// Well stations...
		if ( __AdminNumShiftCol_JComboBox != null ) {
			if ( AdminNumShiftCol == null ) {
				// Select default...
				__AdminNumShiftCol_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__AdminNumShiftCol_JComboBox, AdminNumShiftCol, JGUIUtil.NONE, null, null ) ){
					__AdminNumShiftCol_JComboBox.select(AdminNumShiftCol);
				}
				else {
					Message.printWarning ( 2, routine, "Existing command references an invalid " +
					"AdminNumShiftCol value \""+ AdminNumShiftCol +
					"\".  Select a different value or Cancel.");
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
					__DiversionIDCol_JComboBox,	DiversionIDCol, JGUIUtil.NONE, null, null ) ) {
					__DiversionIDCol_JComboBox.select(DiversionIDCol);
				}
				else {
					Message.printWarning ( 2, routine, "Existing command references an invalid " +
					"DiversionIDCol value \""+ DiversionIDCol +
					"\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		// General...
		if ( Delim == null ) {
			// Select default...
			__Delim_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(__Delim_JComboBox, Delim, JGUIUtil.NONE, null, null ) ) {
				__Delim_JComboBox.select(Delim);
			}
			else {
				// Add as first item...
				__Delim_JComboBox.insert(Delim,1);
			}
		}
		if ( MergeDelim == null ) {
			// Select default...
			__MergeDelim_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__MergeDelim_JComboBox, MergeDelim, JGUIUtil.NONE, null, null ) ) {
				__MergeDelim_JComboBox.select(MergeDelim);
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an invalid MergeDelim value \""+
				MergeDelim + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IfNotFound == null ) {
			// Select default...
			__IfNotFound_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IfNotFound_JComboBox, IfNotFound, JGUIUtil.NONE, null, null ) ) {
				__IfNotFound_JComboBox.select ( IfNotFound );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	IDCol = __IDCol_JComboBox.getSelected();
	Delim = __Delim_JComboBox.getSelected();
	MergeDelim = __MergeDelim_JComboBox.getSelected();
	NameCol = __NameCol_JComboBox.getSelected();
	RiverNodeIDCol = __RiverNodeIDCol_JComboBox.getSelected();
	OnOffCol = __OnOffCol_JComboBox.getSelected();
	CapacityCol = __CapacityCol_JComboBox.getSelected();
	if ( __ReplaceResOptionCol_JComboBox != null ) {
		ReplaceResOptionCol = __ReplaceResOptionCol_JComboBox.getSelected();
	}
	DailyIDCol = __DailyIDCol_JComboBox.getSelected();
	if ( __UserNameCol_JComboBox != null ) {
		UserNameCol = __UserNameCol_JComboBox.getSelected();
	}
	DemandTypeCol = __DemandTypeCol_JComboBox.getSelected();
	IrrigatedAcresCol = __IrrigatedAcresCol_JComboBox.getSelected();
	UseTypeCol = __UseTypeCol_JComboBox.getSelected();
	DemandSourceCol = __DemandSourceCol_JComboBox.getSelected();
	EffAnnualCol = __EffAnnualCol_JComboBox.getSelected();
	EffMonthlyCol = __EffMonthlyCol_JComboBox.getSelected();
	if ( __AdminNumShiftCol_JComboBox != null ) {
		AdminNumShiftCol = __AdminNumShiftCol_JComboBox.getSelected();
	}
	if ( __DiversionIDCol_JComboBox != null ) {
		DiversionIDCol = __DiversionIDCol_JComboBox.getSelected();
	}
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters = new PropList(__command.getCommandName());
	parameters.add("ListFile=" + ListFile);
	parameters.add("IDCol=" + IDCol);
	parameters.add("Delim=" + Delim);
	parameters.add("MergeDelim=" + MergeDelim);
	parameters.add("NameCol=" + NameCol);
	parameters.add("RiverNodeIDCol=" + RiverNodeIDCol);
	parameters.add("OnOffCol=" + OnOffCol);
	parameters.add("CapacityCol=" + CapacityCol);
	parameters.add("ReplaceResOptionCol=" + ReplaceResOptionCol);
	parameters.add("DailyIDCol=" + DailyIDCol);
	parameters.add("UserNameCol=" + UserNameCol);
	parameters.add("DemandTypeCol=" + DemandTypeCol);
	parameters.add("IrrigatedAcresCol=" + IrrigatedAcresCol);
	parameters.add("UseTypeCol=" + UseTypeCol);
	parameters.add("DemandSourceCol=" + DemandSourceCol);
	parameters.add("EffAnnualCol=" + EffAnnualCol);
	parameters.add("EffMonthlyCol=" + EffMonthlyCol);
	parameters.add("AdminNumShiftCol=" + AdminNumShiftCol);
	parameters.add("DiversionIDCol=" + DiversionIDCol);
	parameters.add("IfNotFound=" + IfNotFound);
	__command_JTextArea.setText( __command.toString(parameters) );
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

} // End fillFromList_JDialog
