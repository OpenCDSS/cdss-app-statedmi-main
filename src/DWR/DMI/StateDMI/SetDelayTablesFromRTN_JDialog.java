// SetDelayTablesFromRTN_JDialog - Editor for SetDiversionStationDelayTablesFromRTN(), SetWellStationDelayTablesFromRTN(), and
// SetWellStationDepletionTablesFromRTN() commands.

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
Editor for SetDiversionStationDelayTablesFromRTN(), SetWellStationDelayTablesFromRTN(), and
SetWellStationDepletionTablesFromRTN() commands.
*/
public class SetDelayTablesFromRTN_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __InputFile_JTextField = null;// List file
private SimpleJComboBox __SetEfficiency_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private SetDelayTablesFromRTN_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetDelayTablesFromRTN_JDialog ( JFrame parent, SetDelayTablesFromRTN_Command command )
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
			fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		SimpleFileFilter sff = null;
		if ( (__command instanceof SetDiversionStationDelayTablesFromRTN_Command) ||
			(__command instanceof SetWellStationDelayTablesFromRTN_Command)) {
			fc.setDialogTitle("Select Return File");
			sff = new SimpleFileFilter("rtn", "Return flow file." );
		}
		else if ( __command instanceof SetWellStationDepletionTablesFromRTN_Command ){
			fc.setDialogTitle("Select Depletions File");
			sff = new SimpleFileFilter("rtn", "Depletions file." );
		}
		fc.addChoosableFileFilter( sff );
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
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if (InputFile.length() > 0) {
		props.set("InputFile", InputFile);
	}
	if (IfNotFound.length() > 0 && !IfNotFound.equals("")) {
		props.set("IfNotFound", IfNotFound);
	}
	if ( (__command instanceof SetDiversionStationDelayTablesFromRTN_Command) ||
		(__command instanceof SetWellStationDelayTablesFromRTN_Command) ) {
		String SetEfficiency = __SetEfficiency_JComboBox.getSelected();
		if (SetEfficiency.length() > 0 && !SetEfficiency.equals("")) {
			props.set("SetEfficiency", SetEfficiency);
		}
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
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter("IfNotFound", IfNotFound);

	if ( (__command instanceof SetDiversionStationDelayTablesFromRTN_Command) ||
		(__command instanceof SetWellStationDelayTablesFromRTN_Command) ) {
		String SetEfficiency = __SetEfficiency_JComboBox.getSelected();
		__command.setCommandParameter("SetEfficiency", SetEfficiency);
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
private void initialize ( JFrame parent, SetDelayTablesFromRTN_Command command )
{
	__command = command;
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
	if ( (__command instanceof SetDiversionStationDelayTablesFromRTN_Command) ||
		(__command instanceof SetWellStationDelayTablesFromRTN_Command)) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads and processes delay table information from an \"RTN\" format file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Delay (return flow) table data indicate the pattern by " +
		"which unused water is returned to the system."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		if ( __command instanceof SetDiversionStationDelayTablesFromRTN_Command ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"The file may contain default efficiency information for diversion stations."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if (__command instanceof SetWellStationDelayTablesFromRTN_Command) {
			JGUIUtil.addComponent(paragraph, new JLabel (
			"The file may contain default efficiency information for well stations."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This information can be used and can then be reset later " +
		"when average efficiencies are estimated from time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if (__command instanceof SetWellStationDepletionTablesFromRTN_Command) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads and processes depletion information" +
		" from an \"RTN\" format file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
		"Depletion table data indicate the pattern by " +
		"which well pumping affects surface water locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This file format has been used with CDSS modeling software" +
		" and is provided for backward compatibility."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"A delimited list file format may be supported in the future."),
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

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

	if ( (__command instanceof SetDiversionStationDelayTablesFromRTN_Command) ||
		(__command instanceof SetWellStationDelayTablesFromRTN_Command) ) {
		// Not needed for depletions
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Set efficiency?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List choice_Vector = new Vector(2);
        choice_Vector.add ( "" );
        choice_Vector.add ( __command._True );
        choice_Vector.add ( __command._False );
        __SetEfficiency_JComboBox = new SimpleJComboBox(false);
        __SetEfficiency_JComboBox.setData ( choice_Vector );
        __SetEfficiency_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __SetEfficiency_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - if " + __command._True + ", use default efficiency information in file (default=" +
			__command._False + ")." ), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List IfNotFound_List = new Vector();
	IfNotFound_List.add ( "" );
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if no match is found (default=" + __command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setEditable (false);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
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
	String InputFile = "";
	String SetEfficiency = "";
	String IfNotFound = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
		SetEfficiency = props.getValue ( "SetEfficiency" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText (InputFile);
		}
		if ( __SetEfficiency_JComboBox != null ) {
			if ( SetEfficiency == null ) {
				// Select default...
				__SetEfficiency_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__SetEfficiency_JComboBox, SetEfficiency, JGUIUtil.NONE, null, null ) ) {
					__SetEfficiency_JComboBox.select(SetEfficiency);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an SetEfficiency value \"" +
					SetEfficiency + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
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
	props = new PropList(__command.getCommandName());
	InputFile = __InputFile_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add("InputFile=" + InputFile);
	if ( __SetEfficiency_JComboBox != null ) {
		SetEfficiency = __SetEfficiency_JComboBox.getSelected();
		props.add("SetEfficiency=" + SetEfficiency);
	}
	props.add( "IfNotFound=" + IfNotFound );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
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
