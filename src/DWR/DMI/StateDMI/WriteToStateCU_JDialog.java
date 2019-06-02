// WriteToStateCU_JDialog - Command editor dialog for simple Write*ToStateCU() commands, which share the same parameters.

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

import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Command editor dialog for simple Write*ToStateCU() commands, which share the same parameters.
*/
@SuppressWarnings("serial")
public class WriteToStateCU_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private JTextField __OutputFile_JTextField = null;
private JTextField __Version_JTextField = null;
private JTextField __Precision_JTextField = null;
private SimpleJComboBox	__AutoAdjust_JComboBox = null; // For development to
//deal with non-standard issues in data (e.g., crop names that include "."
private SimpleJComboBox __WriteHow_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private WriteToStateCU_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public WriteToStateCU_JDialog (JFrame parent, Command command )
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
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}

		SimpleFileFilter sff = null;
		if ( __command instanceof WriteClimateStationsToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Climate Stations File to Write");
			sff = new SimpleFileFilter("cli", "StateCU Climate Stations File");
		}
		else if ( __command instanceof WriteCropCharacteristicsToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Crop Characteristics File to Write");
			sff = new SimpleFileFilter("cch", "StateCU Crop Characteristics File");
		}
		else if ( __command instanceof WriteBlaneyCriddleToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Blaney-Criddle Crop Coefficients File to Write");
			sff = new SimpleFileFilter("kbc", "StateCU Blaney-Criddle Crop Coefficients File");
		}
		else if ( __command instanceof WritePenmanMonteithToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Penman-Monteith Crop Coefficients File to Write");
			sff = new SimpleFileFilter("kpm", "StateCU Penman-Monteith Crop Coefficients File");
		}
		else if ( __command instanceof WriteCULocationDelayTableAssignmentsToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Delay Table Assignment File to Write");
			sff = new SimpleFileFilter("dla", "StateCU Delay Table Assignment File");
		}
		else if ( __command instanceof WriteCULocationsToStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU CU Locations (Structure) File to Write");
			sff = new SimpleFileFilter("str", "StateCU CU Locations (Structure) File");
		}
		fc.addChoosableFileFilter(sff);
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
		response (false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals("Remove Working Directory")) {
			try {
				__OutputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __OutputFile_JTextField.getText()));
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
	String OutputFile = __OutputFile_JTextField.getText().trim();

	String WriteHow = __WriteHow_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (OutputFile.length() > 0) {
		props.set("OutputFile", OutputFile);
	}
	if ( __Version_JTextField != null ) {
		String Version = __Version_JTextField.getText().trim();
		if (Version.length() > 0) {
			props.set("Version", Version);
		}
	}
	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		if (Precision.length() > 0) {
			props.set("Precision", Precision);
		}
	}
	if ( __AutoAdjust_JComboBox != null ) {
		String AutoAdjust = __AutoAdjust_JComboBox.getSelected();
		if (AutoAdjust.length() > 0) {
			props.set("AutoAdjust", AutoAdjust);
		}
	}
	if (WriteHow.length() > 0 ) {
		props.set("WriteHow", WriteHow);
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
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String WriteHow = __WriteHow_JComboBox.getSelected();

	__command.setCommandParameter("OutputFile", OutputFile);
	__command.setCommandParameter("WriteHow", WriteHow);
	
	if ( __Version_JTextField != null ) {
		String Version = __Version_JTextField.getText().trim();
		__command.setCommandParameter("Version", Version);
	}
	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		__command.setCommandParameter("Precision", Precision);
	}
	if ( __AutoAdjust_JComboBox != null ) {
		String AutoAdjust = __AutoAdjust_JComboBox.getSelected();
		__command.setCommandParameter("AutoAdjust", AutoAdjust);
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__OutputFile_JTextField = null;
	__Version_JTextField = null;
	__WriteHow_JComboBox = null;
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
private void initialize (JFrame parent, Command command )
{	__command = (WriteToStateCU_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	
	if ( __command instanceof WriteClimateStationsToStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU climate station data to a StateCU climate stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof WriteCropCharacteristicsToStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU crop characteristics data to a StateCU crop characteristics file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof WriteBlaneyCriddleToStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU Blaney-Criddle data to a StateCU Blaney-Criddle file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof WritePenmanMonteithToStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU Penman-Monteith data to a StateCU Penman-Monteith file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof WriteCULocationsToStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU CU locations data to StateCU locations (structure) file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof WriteCULocationDelayTableAssignmentsToStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateCU location monthly delay table assignment data to a StateCU file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the file be specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: "), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("    " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (35);
	__OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    
   if ( __command instanceof WriteCULocationsToStateCU_Command ||
		__command instanceof WriteCropCharacteristicsToStateCU_Command ||
		__command instanceof WriteBlaneyCriddleToStateCU_Command ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Version:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __Version_JTextField = new JTextField (35);
		__Version_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __Version_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - indicate StateCU version for format (default=latest)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   }
   
   if ( __command instanceof WriteBlaneyCriddleToStateCU_Command ||
		__command instanceof WritePenmanMonteithToStateCU_Command) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Precision:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __Precision_JTextField = new JTextField (35);
		__Precision_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __Precision_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - digits after decimal (default=3)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   }
   
   if ( __command instanceof WriteCropCharacteristicsToStateCU_Command ) {
	   JGUIUtil.addComponent(main_JPanel, new JLabel ( "Automatically adjust?:" ), 
	   		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	   	__AutoAdjust_JComboBox = new SimpleJComboBox ( false );
	   	__AutoAdjust_JComboBox.addItem ( "" );
	   	__AutoAdjust_JComboBox.addItem ( __command._False );
	   	__AutoAdjust_JComboBox.addItem ( __command._True );
	   	__AutoAdjust_JComboBox.addItemListener ( this );
	    JGUIUtil.addComponent(main_JPanel, __AutoAdjust_JComboBox,
	   		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	  	JGUIUtil.addComponent(main_JPanel, new JLabel (
	       	"Optional - remove trailing \".xxxx\" when Version=10 (default=" + __command._False + ")."),
	       	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   }

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Write how:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	Vector<String> write_how_Vector = new Vector<String>(3);
	write_how_Vector.addElement ( "" );
	write_how_Vector.addElement ( __command._OverwriteFile );
	write_how_Vector.addElement ( __command._UpdateFile );
	__WriteHow_JComboBox = new SimpleJComboBox(false);
	__WriteHow_JComboBox.setData ( write_how_Vector );
	__WriteHow_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __WriteHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate whether to overwrite/update (default=" + __command._OverwriteFile + ")."),
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
		0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if (__working_dir != null) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton( "Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable ( false );
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
			response (false);
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
{	String routine = __command.getCommandName() + "_JDialog.refresh";
	String OutputFile = "";
	String Version = "";
	String Precision = "";
	String AutoAdjust = "";
	String WriteHow = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		OutputFile = props.getValue ( "OutputFile" );
		Version = props.getValue ( "Version" );
		Precision = props.getValue ( "Precision" );
		AutoAdjust = props.getValue ( "AutoAdjust" );
		WriteHow = props.getValue ( "WriteHow" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText (OutputFile);
		}
		if ( __Version_JTextField != null ) {
			if ( Version != null ) {
				__Version_JTextField.setText (Version);
			}
		}
		if ( __Precision_JTextField != null ) {
			if ( Precision != null ) {
				__Precision_JTextField.setText (Precision);
			}
		}
		if ( WriteHow == null ) {
			// Select default...
			__WriteHow_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__WriteHow_JComboBox,WriteHow, JGUIUtil.NONE, null, null ) ) {
				__WriteHow_JComboBox.select ( WriteHow );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid WriteHow value \"" + WriteHow +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __AutoAdjust_JComboBox != null ) {
			if ( AutoAdjust == null ) {
				// Select default...
				__AutoAdjust_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__AutoAdjust_JComboBox,AutoAdjust, JGUIUtil.NONE, null, null ) ) {
					__AutoAdjust_JComboBox.select ( AutoAdjust );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid AutoAdjust value \"" + AutoAdjust +
					"\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	OutputFile = __OutputFile_JTextField.getText().trim();
	props.add("OutputFile=" + OutputFile);
	if ( __Version_JTextField != null ) {
		Version = __Version_JTextField.getText().trim();
		props.add("Version=" + Version);
	}
	if ( __Precision_JTextField != null ) {
		Precision = __Precision_JTextField.getText().trim();
		props.add("Precision=" + Precision);
	}
	if ( __AutoAdjust_JComboBox != null ) {
		AutoAdjust = __AutoAdjust_JComboBox.getSelected();
		props.add("AutoAdjust=" + AutoAdjust);
	}
	WriteHow = __WriteHow_JComboBox.getSelected();
	props.add("WriteHow=" + WriteHow);
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (OutputFile);
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
