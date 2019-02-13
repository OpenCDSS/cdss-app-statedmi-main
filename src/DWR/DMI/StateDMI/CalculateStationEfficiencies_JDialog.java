// CalculateStationEfficiencies_JDialog - Editor for CalculateDiversionStationEfficiencies() and CalculateWellStationEfficiencies() commands.

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
Editor for CalculateDiversionStationEfficiencies() and CalculateWellStationEfficiencies() commands.
*/
public class CalculateStationEfficiencies_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener,
ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private JTextField __ID_JTextField = null;
private JTextField __EffMin_JTextField = null;
private JTextField __EffMax_JTextField = null;
private JTextField __EffCalcStart_JTextField = null;
private JTextField __EffCalcEnd_JTextField = null;
private SimpleJComboBox __LEZeroInAverage_JComboBox = null;
private JTextField __EffReportFile_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private JTextArea __command_JTextArea = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private CalculateStationEfficiencies_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CalculateStationEfficiencies_JDialog (JFrame parent, CalculateStationEfficiencies_Command command )
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
		fc.setDialogTitle("Specify the Efficieny Report File to Write");
		SimpleFileFilter sff = null;
		sff = new SimpleFileFilter("txt", "StateDMI Efficiency Calculation Report File");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__EffReportFile_JTextField.setText(path);
			refresh();
		}	
	}
	else if ( o == __cancel_JButton ) {
		response (false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__EffReportFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __EffReportFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals(
			"Remove Working Directory")) {
			try {
				__EffReportFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __EffReportFile_JTextField.getText()));
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
	String ID = __ID_JTextField.getText().trim();
	String EffMin = __EffMin_JTextField.getText().trim();
	String EffMax = __EffMax_JTextField.getText().trim();
	String EffCalcStart = __EffCalcStart_JTextField.getText().trim();
	String EffCalcEnd = __EffCalcEnd_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String EffReportFile = __EffReportFile_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (ID.length() > 0) {
		props.set("ID", ID);
	}
	if (EffMin.length() > 0 ) {
		props.set("EffMin", EffMin);
	}
	if (EffMax.length() > 0 ) {
		props.set("Delimiter", EffMax);
	}
	if (EffCalcStart.length() > 0 ) {
		props.set("EffCalcStart", EffCalcStart);
	}
	if (EffCalcEnd.length() > 0 ) {
		props.set("Delimiter", EffCalcEnd);
	}
	if (LEZeroInAverage.length() > 0 ) {
		props.set("LEZeroInAverage", LEZeroInAverage);
	}
	if (EffReportFile.length() > 0 ) {
		props.set("EffReportFile", EffReportFile);
	}
    if ( IfNotFound.length() > 0 ) {
    	props.set ( "IfNotFound", IfNotFound );
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
	String ID = __ID_JTextField.getText().trim();
	String EffMin = __EffMin_JTextField.getText().trim();
	String EffMax = __EffMax_JTextField.getText().trim();
	String EffCalcStart = __EffCalcStart_JTextField.getText().trim();
	String EffCalcEnd = __EffCalcEnd_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String EffReportFile = __EffReportFile_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter("ID", ID);
	__command.setCommandParameter("EffMin", EffMin);
	__command.setCommandParameter("EffMax", EffMax);
	__command.setCommandParameter("EffCalcStart", EffCalcStart);
	__command.setCommandParameter("EffCalcEnd", EffCalcEnd);
	__command.setCommandParameter("LEZeroInAverage", LEZeroInAverage);
	__command.setCommandParameter("EffReportFile", EffReportFile);
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__LEZeroInAverage_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, CalculateStationEfficiencies_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	if ( __command instanceof CalculateDiversionStationEfficiencies_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command calculates monthly efficiencies for each diversion station that is defined." ),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Efficiencies are computed as the ratio of irrigation " +
		"(consumptive) water requirement divided by historical diversions." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof CalculateWellStationEfficiencies_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command calculates monthly efficiencies for each well station that is defined." ),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Efficiencies are computed as the ratio of irrigation (consumptive) water " +
		"requirement divided by historical pumping." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is expected that both sets of time series have been filled appropriately."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"If the efficiency report file is provided, details of the" +
		" efficiency calculations will be printed to the file."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	if ( __command instanceof CalculateDiversionStationEfficiencies_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if ( __command instanceof CalculateWellStationEfficiencies_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - stations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency min. (%):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffMin_JTextField = new JTextField(10);
	__EffMin_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffMin_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - minimum efficiency (default=no constraint)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency max. (%):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffMax_JTextField = new JTextField(10);
	__EffMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffMax_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - maximum efficiency (default=no constraint)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Calculation start date:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffCalcStart_JTextField = new JTextField(10);
	__EffCalcStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffCalcStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - start date for efficiency calculations (blank=all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Calculation end date:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffCalcEnd_JTextField = new JTextField(10);
	__EffCalcEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffCalcEnd_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - end date for efficiency calculations (blank=all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("<= zero values in average?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List le_Vector = new Vector(2);
	le_Vector.add ( "" );
	le_Vector.add ( __command._False );
	le_Vector.add ( __command._True );
	__LEZeroInAverage_JComboBox = new SimpleJComboBox(false);
	__LEZeroInAverage_JComboBox.setData( le_Vector );
	__LEZeroInAverage_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __LEZeroInAverage_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - are values <= zero used in averages (used later in filling)? (default=" + __command._True + ".)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Efficiency report file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffReportFile_JTextField = new JTextField (35);
	__EffReportFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffReportFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List if_not_found_Vector = new Vector();
    if_not_found_Vector.add ( "" );
	if_not_found_Vector.add ( __command._Ignore );
	if_not_found_Vector.add ( __command._Warn );
	if_not_found_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( if_not_found_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if no match is found (default="+__command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
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
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton("Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();
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
	String ID = "";
	String EffMin = "";
	String EffMax = "";
	String EffCalcStart = "";
	String EffCalcEnd = "";
	String LEZeroInAverage = "";
	String EffReportFile = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		EffMin = props.getValue ( "EffMin" );
		EffMax = props.getValue ( "EffMax" );
		EffCalcStart = props.getValue ( "EffCalcStart" );
		EffCalcEnd = props.getValue ( "EffCalcEnd" );
		LEZeroInAverage = props.getValue ( "LEZeroInAverage" );
		EffReportFile = props.getValue ( "EffReportFile" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( EffMin != null ) {
			__EffMin_JTextField.setText(EffMin);
		}
		if ( EffMax != null ) {
			__EffMax_JTextField.setText(EffMax);
		}
		if ( EffCalcStart != null ) {
			__EffCalcStart_JTextField.setText(EffCalcStart);
		}
		if ( EffCalcEnd != null ) {
			__EffCalcEnd_JTextField.setText(EffCalcEnd);
		}
		if ( LEZeroInAverage == null ) {
			// Select default...
			__LEZeroInAverage_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__LEZeroInAverage_JComboBox, LEZeroInAverage, JGUIUtil.NONE, null, null ) ) {
				__LEZeroInAverage_JComboBox.select ( LEZeroInAverage );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid\n" +
				"LEZeroInAverage value \""+ LEZeroInAverage + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( EffReportFile != null ) {
			__EffReportFile_JTextField.setText(EffReportFile);
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

	// Always get the value that is selected...
	props = new PropList (__command.toString());
	ID = __ID_JTextField.getText().trim();
	EffMin = __EffMin_JTextField.getText().trim();
	EffMax = __EffMax_JTextField.getText().trim();
	EffCalcStart = __EffCalcStart_JTextField.getText().trim();
	EffCalcEnd = __EffCalcEnd_JTextField.getText().trim();
	LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	EffReportFile = __EffReportFile_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add("ID=" + ID);
	props.add("EffMin=" + EffMin);
	props.add("EffMax=" + EffMax);
	props.add("EffCalcStart=" + EffCalcStart);
	props.add("EffCalcEnd=" + EffCalcEnd);
	props.add("LEZeroInAverage=" + LEZeroInAverage);
	props.add("EffReportFile=" + EffReportFile);
	props.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (EffReportFile);
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
