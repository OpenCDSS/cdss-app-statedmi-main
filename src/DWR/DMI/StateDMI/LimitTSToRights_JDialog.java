// LimitTSToRights_JDialog - Editor for Limit*TSToRights() commands.

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
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Limit*TSToRights() commands.
*/
@SuppressWarnings("serial")
public class LimitTSToRights_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private final String __True = "True"; // Used with
private final String __False = "False"; // LimitToCurrent

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextField __InputFile_JTextField = null;// StateMod file
private JTextField __ID_JTextField = null;	// IDs to process
private JTextField __FreeWaterAppropriationDate_JTextField = null;
private SimpleJComboBox __UseOnOffDate_JComboBox = null;
private JTextField __NumberOfDaysInMonth_JTextField = null;
private JTextField __IgnoreID_JTextField = null; // IDs to ignore
private SimpleJComboBox __LimitToCurrent_JComboBox = null;
private JTextField __SetFlag_JTextField = null;
private JTextArea __command_JTextArea=null;// For command
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private LimitTSToRights_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public LimitTSToRights_JDialog ( JFrame parent, LimitTSToRights_Command command )
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
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		SimpleFileFilter sff = null;
		fc.setDialogTitle("Select StateMod Rights File");
		if ( (__command instanceof LimitDiversionHistoricalTSMonthlyToRights_Command) ||
			(__command instanceof LimitDiversionDemandTSMonthlyToRights_Command) ) {	
			sff = new SimpleFileFilter("ddr", "StateMod Diversion Rights File");
		}
		else if((__command instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
			(__command instanceof LimitWellDemandTSMonthlyToRights_Command) ||
			(__command instanceof LimitIrrigationPracticeTSToRights_Command)) {
			sff = new SimpleFileFilter("wer", "StateMod Well Rights File");
		}
		// Might limit reservoir contents, etc. in the future...
		fc.addChoosableFileFilter(sff);
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
		response (false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( "Remove Working Directory")) {
			try {
				__InputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __InputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, __command + "_JDialog", "Error converting file to relative path.");
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
	
	String InputFile = __InputFile_JTextField.getText().trim();
	if (InputFile.length() > 0) {
		props.set("InputFile", InputFile);
	}
	String ID = __ID_JTextField.getText().trim();
	if (ID.length() > 0) {
		props.set("ID", ID);
	}
	if ( __IgnoreID_JTextField != null ) {
		String IgnoreID = __IgnoreID_JTextField.getText().trim();
		if (IgnoreID.length() > 0) {
			props.set("IgnoreID", IgnoreID);
		}
	}
	String FreeWaterAppropriationDate = __FreeWaterAppropriationDate_JTextField.getText().trim();
	if (FreeWaterAppropriationDate.length() > 0) {
		props.set("FreeWaterAppropriationDate", FreeWaterAppropriationDate);
	}
	if ( __UseOnOffDate_JComboBox != null ) {
		String UseOnOffDate = __UseOnOffDate_JComboBox.getSelected();
		if (UseOnOffDate.length() > 0) {
			props.set("UseOnOffDate", UseOnOffDate);
		}
	}
	if ( __NumberOfDaysInMonth_JTextField != null ) {
		String NumberOfDaysInMonth = __NumberOfDaysInMonth_JTextField.getText().trim();
		if (NumberOfDaysInMonth.length() > 0) {
			props.set("NumberOfDaysInMonth", NumberOfDaysInMonth);
		}
	}
	if ( __LimitToCurrent_JComboBox != null ) {
		String LimitToCurrent = __LimitToCurrent_JComboBox.getSelected();
		if (LimitToCurrent.length() > 0) {
			props.set("LimitToCurrent", LimitToCurrent);
		}
	}
	String SetFlag = __SetFlag_JTextField.getText().trim();
	if (SetFlag.length() > 0) {
		props.set("SetFlag", SetFlag);
	}
	
	__error_wait = false;
	
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
	String InputFile = __InputFile_JTextField.getText().trim();
	__command.setCommandParameter("InputFile", InputFile);
	String ID = __ID_JTextField.getText().trim();
	__command.setCommandParameter("ID", ID);
	if ( __IgnoreID_JTextField != null ) {
		String IgnoreID = __IgnoreID_JTextField.getText().trim();
		__command.setCommandParameter("IgnoreID", IgnoreID);
	}
	String FreeWaterAppropriationDate = __FreeWaterAppropriationDate_JTextField.getText().trim();
	__command.setCommandParameter("FreeWaterAppropriationDate", FreeWaterAppropriationDate);
	if ( __UseOnOffDate_JComboBox != null ) {
		String UseOnOffDate = __UseOnOffDate_JComboBox.getSelected();
		__command.setCommandParameter("UseOnOffDate", UseOnOffDate);
	}
	if ( __NumberOfDaysInMonth_JTextField != null ) {
		String NumberOfDaysInMonth = __NumberOfDaysInMonth_JTextField.getText().trim();
		__command.setCommandParameter("NumberOfDaysInMonth", NumberOfDaysInMonth);
	}
	if ( __LimitToCurrent_JComboBox != null ) {
		String LimitToCurrent = __LimitToCurrent_JComboBox.getSelected();
		__command.setCommandParameter("LimitToCurrent", LimitToCurrent);
	}
	String SetFlag = __SetFlag_JTextField.getText().trim();
	__command.setCommandParameter("SetFlag", SetFlag);
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__InputFile_JTextField = null;
	__IgnoreID_JTextField = null;
	__UseOnOffDate_JComboBox = null;
	__LimitToCurrent_JComboBox = null;
	__SetFlag_JTextField = null;
	__NumberOfDaysInMonth_JTextField = null;
	__browse_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__path_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, LimitTSToRights_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );
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
	if ( __command instanceof LimitDiversionHistoricalTSMonthlyToRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command limits diversion historical time series (monthly) to water rights for each diversion station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof LimitDiversionDemandTSMonthlyToRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command limits diversion demand time series (monthly) to water rights for each diversion station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command limits well historical pumping time series (monthly) to water rights for each well station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof LimitWellDemandTSMonthlyToRights_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command limits well demand time series (monthly) to water rights for each well station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof LimitIrrigationPracticeTSToRights_Command ) {
		// Message is for StateCU...
		JGUIUtil.addComponent(paragraph, new JLabel (
		"THIS COMMAND IS OBSOLETE AND IS USED ONLY FOR PHASE 4 RIO GRANDE WORK " +
		"- INSTEAD, SEE THE SetIrrigationPracticeTSPumpingMaxUsingWellRights() COMMAND."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets irrigation practice maximum monthly " +
		"pumping time series to water rights for each CU Location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	if ( (__command instanceof LimitDiversionHistoricalTSMonthlyToRights_Command) ||
		(__command instanceof LimitDiversionDemandTSMonthlyToRights_Command) ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
			"Diversion rights are specified by reading a StateMod diversion rights" +
			" file, or use rights in memory from previous commands."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
		(__command instanceof LimitWellDemandTSMonthlyToRights_Command) ||
		(__command instanceof LimitIrrigationPracticeTSToRights_Command) ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
			"Water rights are specified by reading a StateMod well rights" +
			" file, or use rights in memory from previous commands."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}

	if ( !(__command instanceof LimitIrrigationPracticeTSToRights_Command) ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Stations to ignore can be specified as one or more IDs or patterns, separated by commas."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the rights file be " +
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

    JGUIUtil.addComponent(main_JPanel, new JLabel ("StateMod rights file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

	if ( (__command instanceof LimitDiversionHistoricalTSMonthlyToRights_Command) ||
		(__command instanceof LimitDiversionDemandTSMonthlyToRights_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if((__command instanceof LimitWellHistoricalPumpingTSMonthlyToRights_Command) ||
		(__command instanceof LimitWellDemandTSMonthlyToRights_Command)){
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if ( __command instanceof LimitIrrigationPracticeTSToRights_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __command instanceof LimitIrrigationPracticeTSToRights_Command ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU locations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else {
		JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - stations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( !(__command instanceof LimitIrrigationPracticeTSToRights_Command)){
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Ignore ID(s):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__IgnoreID_JTextField = new JTextField(10);
		__IgnoreID_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __IgnoreID_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - stations to ignore (use * for wildcard)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Free water appropriation date:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FreeWaterAppropriationDate_JTextField = new JTextField(10);
	__FreeWaterAppropriationDate_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FreeWaterAppropriationDate_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - appropriation date for admin numbers >= 90000"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Use OnOff date?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> onoff_Vector = new Vector<String>(3);
	onoff_Vector.add ( "" );
	onoff_Vector.add ( __False );
	onoff_Vector.add ( __True );
	__UseOnOffDate_JComboBox = new SimpleJComboBox(false);
	__UseOnOffDate_JComboBox.setData ( onoff_Vector );
	__UseOnOffDate_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __UseOnOffDate_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - get date from OnOff when YYYY, -YYYY? (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __command instanceof LimitIrrigationPracticeTSToRights_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Number of days in month:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__NumberOfDaysInMonth_JTextField = new JTextField("",10);
		__NumberOfDaysInMonth_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __NumberOfDaysInMonth_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - use to normalize maximum pumping value (default=actual days)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	if ( (__command instanceof LimitDiversionDemandTSMonthlyToRights_Command) ||
		(__command instanceof LimitWellDemandTSMonthlyToRights_Command) ){
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Limit to current rights:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> LimitToCurrent_Vector = new Vector<String>(3);
		LimitToCurrent_Vector.add ( "" );
		LimitToCurrent_Vector.add ( __False );
		LimitToCurrent_Vector.add ( __True );
		__LimitToCurrent_JComboBox = new SimpleJComboBox(false);
		__LimitToCurrent_JComboBox.setData ( LimitToCurrent_Vector );
		__LimitToCurrent_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel, __LimitToCurrent_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - use " + __command._True + " to limit full period to current rights" +
		" (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel( "Set flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetFlag_JTextField = new JTextField (10);
	__SetFlag_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __SetFlag_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - 1-character flag to track reset values (default=none)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
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
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton("Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

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
{	String routine = __command + "_JDialog.refresh";
	String InputFile = "";
	String ID = "";
	String IgnoreID = "";
	String FreeWaterAppropriationDate = "";
	String UseOnOffDate = "";
	String NumberOfDaysInMonth = "";
	String LimitToCurrent = "";
	String SetFlag = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
		ID = props.getValue ( "ID" );
		IgnoreID = props.getValue ( "IgnoreID" );
		LimitToCurrent = props.getValue ( "LimitToCurrent" );
		FreeWaterAppropriationDate = props.getValue ( "FreeWaterAppropriationDate" );
		NumberOfDaysInMonth = props.getValue ( "NumberOfDaysInMonth" );
		UseOnOffDate = props.getValue ( "UseOnOffDate" );
		SetFlag = props.getValue ( "SetFlag" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
		if ( ID != null ) {
			__ID_JTextField.setText ( ID );
		}
		if ( IgnoreID != null ) {
			__IgnoreID_JTextField.setText ( IgnoreID );
		}
		if ( FreeWaterAppropriationDate != null ) {
			__FreeWaterAppropriationDate_JTextField.setText (
			FreeWaterAppropriationDate );
		}
		if ( __UseOnOffDate_JComboBox != null ) {
			if ( UseOnOffDate == null ) {
				// Select default...
				__UseOnOffDate_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__UseOnOffDate_JComboBox, UseOnOffDate, JGUIUtil.NONE, null, null ) ) {
					__UseOnOffDate_JComboBox.
					select ( UseOnOffDate );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an UseOnOffDate " +
					"value \"" + UseOnOffDate + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __NumberOfDaysInMonth_JTextField != null ) {
			if ( NumberOfDaysInMonth != null ) {
				__NumberOfDaysInMonth_JTextField.setText(
					NumberOfDaysInMonth );
			}
		}
		if ( __LimitToCurrent_JComboBox != null ) {
			if ( LimitToCurrent == null ) {
				// Select default...
				__LimitToCurrent_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__LimitToCurrent_JComboBox, LimitToCurrent, JGUIUtil.NONE, null, null ) ) {
					__LimitToCurrent_JComboBox.
					select ( LimitToCurrent );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing commnd references an LimitToCurrent " +
					"value \"" + LimitToCurrent + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( SetFlag != null ) {
			__SetFlag_JTextField.setText ( SetFlag );
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	InputFile = __InputFile_JTextField.getText().trim();
	props.add("InputFile=" + InputFile);
	ID = __ID_JTextField.getText().trim();
	props.add("ID=" + ID);
	if ( __IgnoreID_JTextField != null ) {
		IgnoreID = __IgnoreID_JTextField.getText().trim();
		props.add("IgnoreID=" + IgnoreID);
	}
	FreeWaterAppropriationDate = __FreeWaterAppropriationDate_JTextField.getText().trim();
	props.add("FreeWaterAppropriationDate=" + FreeWaterAppropriationDate);
	if ( __UseOnOffDate_JComboBox != null ) {
		UseOnOffDate = __UseOnOffDate_JComboBox.getSelected();
		props.add("UseOnOffDate=" + UseOnOffDate);
	}
	if ( __NumberOfDaysInMonth_JTextField != null ) {
		NumberOfDaysInMonth = __NumberOfDaysInMonth_JTextField.getText().trim();
		props.add("NumberOfDaysInMonth=" + NumberOfDaysInMonth);
	}
	if ( __LimitToCurrent_JComboBox != null ) {
		LimitToCurrent = __LimitToCurrent_JComboBox.getSelected();
		props.add("LimitToCurrent=" + LimitToCurrent);
	}
	SetFlag = __SetFlag_JTextField.getText().trim();
	props.add("SetFlag=" + SetFlag);
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( InputFile.length() == 0 ) {
		if (__path_JButton != null) {
			__path_JButton.setEnabled (false);
		}
	}
	else if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (InputFile);
		if (f.isAbsolute()) {
			__path_JButton.setText ("Remove Working Directory");
		}
		else {
			__path_JButton.setText ("Add Working Directory");
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
