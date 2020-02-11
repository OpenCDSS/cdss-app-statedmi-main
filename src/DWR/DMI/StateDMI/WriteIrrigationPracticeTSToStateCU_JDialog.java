// WriteIrrigationPracticeTSToStateCU_JDialog - editor for WriteIrrigationPracticeTSToStateCU command

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
// writeIrrigationPracticeTSToStateCU_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2003-03-06	Steven A. Malers, RTi	Copy and modify
//					writeCUTimeSeriesToDateValue_JDialog.
// 2003-05-09	SAM, RTi		Rename from
//					writeCUTimeSeriesToTSP_JDialog to
//					writeIrrigationPracticeTS
//					ToStateCU_JDialog.
// 2004-03-03	SAM, RTi		* Update to current standards.
// 2005-01-20	SAM, RTi		Change the default for WriteHow to
//					"OverwriteFile".
// 2005-10-10	SAM, RTi		Use a text area for the command.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

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

@SuppressWarnings("serial")
public class WriteIrrigationPracticeTSToStateCU_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener {
	
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private JTextField __OutputFile_JTextField = null;
private JTextField __OutputStart_JTextField = null;
private JTextField __OutputEnd_JTextField = null;
private JTextField __PrecisionForArea_JTextField = null;
private SimpleJComboBox __WriteHow_JComboBox = null;
private SimpleJComboBox __OneLocationPerFile_JComboBox = null;
private SimpleJComboBox __CheckData_JComboBox = null;
private SimpleJComboBox __Version_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private WriteIrrigationPracticeTSToStateCU_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to parse.
*/
public WriteIrrigationPracticeTSToStateCU_JDialog (JFrame parent,
	WriteIrrigationPracticeTSToStateCU_Command command)
{
	super(parent, true);
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
		fc.setDialogTitle( "Specify StateCU Irrigation Practice Time Series File to Write");
		
		SimpleFileFilter sff = new SimpleFileFilter("ipy", "StateCU Irrigation Practice File");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			if (path != null) {
				// Convert path to relative path by default.
				try {
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory );
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
	else if ( o == __path_JButton) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
			__OutputFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals( __RemoveWorkingDirectory) ) {
			try {
                __OutputFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
				__OutputFile_JTextField.getText() ) );
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
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String OutputStart = __OutputStart_JTextField.getText().trim();
	String OutputEnd = __OutputEnd_JTextField.getText().trim();
	String PrecisionForArea = __PrecisionForArea_JTextField.getText().trim();
	String Version = __Version_JComboBox.getSelected();
	String OneLocationPerFile = __OneLocationPerFile_JComboBox.getSelected();
	String CheckData = __CheckData_JComboBox.getSelected();
	String WriteHow = __WriteHow_JComboBox.getSelected();
	
	if (OutputFile.length() > 0) {
		props.set("OutputFile", OutputFile);
	}
	if (OutputStart.length() > 0) {
		props.set("OutputStart", OutputStart);
	}
	if (OutputEnd.length() > 0) {
		props.set("OutputEnd", OutputEnd);
	}
	if (PrecisionForArea.length() > 0) {
		props.set("PrecisionForArea", PrecisionForArea);
	}
	if (Version.length() > 0) {
		props.set("Version", Version);
	}
	if (OneLocationPerFile.length() > 0) {
		props.set("OneLocationPerFile", OneLocationPerFile);
	}
	if (CheckData.length() > 0) {
		props.set("CheckData", CheckData);
	}
	if (WriteHow.length() > 0 ) {
		props.set("WriteHow", WriteHow);
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
private void commitEdits()
{
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String OutputStart = __OutputStart_JTextField.getText().trim();
	String OutputEnd = __OutputEnd_JTextField.getText().trim();
	String PrecisionForArea = __PrecisionForArea_JTextField.getText().trim();
	String Version = __Version_JComboBox.getSelected();
	String OneLocationPerFile = __OneLocationPerFile_JComboBox.getSelected();
	String CheckData = __CheckData_JComboBox.getSelected();
	String WriteHow = __WriteHow_JComboBox.getSelected();

	__command.setCommandParameter("OutputFile", OutputFile);
	__command.setCommandParameter("OutputStart", OutputStart);
	__command.setCommandParameter("OutputEnd", OutputEnd);
	__command.setCommandParameter("PrecisionForArea", PrecisionForArea);
	__command.setCommandParameter("Version", Version);
	__command.setCommandParameter("OneLocationPerFile", OneLocationPerFile);
	__command.setCommandParameter("CheckData", CheckData);
	__command.setCommandParameter("WriteHow", WriteHow);
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, WriteIrrigationPracticeTSToStateCU_Command command ) {
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = -1;

	// Main contents...

	// Now add the buttons...

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes available irrigation practice time " +
		"series data to a StateCU irrigation practice time series file."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the file be specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel ( "The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The default value for \"Write how\" is OverwriteFile, which " +
		"will create a new file, overwriting an old file if it exists."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "StateCU irrigation practice file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (35);
	__OutputFile_JTextField.addKeyListener (this);
    // Output file layout fights back with other rows so put in its own panel
	JPanel OutputFile_JPanel = new JPanel();
	OutputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(OutputFile_JPanel, __OutputFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(OutputFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(OutputFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, OutputFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output start (year):"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputStart_JTextField = new JTextField (10);
    __OutputStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputStart_JTextField,
    	1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - output start year as 4-digits (default=output all)."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output end (year):"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputEnd_JTextField = new JTextField (10);
    __OutputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputEnd_JTextField,
    	1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - end year as 4-digits (default=output all)."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );        

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Precision for area:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PrecisionForArea_JTextField = new JTextField (10);
    __PrecisionForArea_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PrecisionForArea_JTextField,
    	1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - default=1 to minimize roundoff errors to .1 acre."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );            
            
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Version:"),
       	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> version_Vector = new Vector<String>(2);
    version_Vector.add ( "" );
    version_Vector.add ( __command._10 );
    __Version_JComboBox = new SimpleJComboBox(false);
    __Version_JComboBox.setData ( version_Vector );
    __Version_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __Version_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - default=most current."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
    JGUIUtil.addComponent(main_JPanel, new JLabel ("One location per file?:"),
       	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> OneLocationPerFile_Vector = new Vector<String>(3);
    OneLocationPerFile_Vector.add ( "" );
    OneLocationPerFile_Vector.add ( __command._False );
    OneLocationPerFile_Vector.add ( __command._True );
    __OneLocationPerFile_JComboBox = new SimpleJComboBox(false);
    __OneLocationPerFile_JComboBox.setData ( OneLocationPerFile_Vector );
    __OneLocationPerFile_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __OneLocationPerFile_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - separate files are useful for verification (default=" + __command._False + ")."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Check data?:"),
           	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> CheckData_Vector = new Vector<String>(3);
    CheckData_Vector.add ( "" );
    CheckData_Vector.add ( __command._False );
    CheckData_Vector.add ( __command._True );
    __CheckData_JComboBox = new SimpleJComboBox(false);
    __CheckData_JComboBox.setData ( CheckData_Vector );
    __CheckData_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __CheckData_JComboBox,
     	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
     	"Optional - check data for integrity (default=" + __command._True +
     	", use " + __command._False + " for intermediate output)."),
      	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );        
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Write how:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> write_how_Vector = new Vector<String>(3);
	write_how_Vector.add ( "" );
	write_how_Vector.add ( __command._OverwriteFile );
	write_how_Vector.add ( __command._UpdateFile );
	__WriteHow_JComboBox = new SimpleJComboBox(false);
	__WriteHow_JComboBox.setData ( write_how_Vector );
	__WriteHow_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __WriteHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

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
{	String routine = getClass().getName() + ".refresh";
	String OutputFile = "";
	String OutputStart = "";
	String OutputEnd = "";
	String PrecisionForArea = "";
	String WriteHow = "";
	String Version = "";
	String OneLocationPerFile = "";
	String CheckData = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		OutputFile = props.getValue ( "OutputFile" );
		OutputStart = props.getValue ( "OutputStart" );
		OutputEnd = props.getValue ( "OutputEnd" );
		PrecisionForArea = props.getValue ( "PrecisionForArea" );
		Version = props.getValue ( "Version" );
		OneLocationPerFile = props.getValue ( "OneLocationPerFile" );
		CheckData = props.getValue ( "CheckData" );
		WriteHow = props.getValue ( "WriteHow" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText (OutputFile);
		}
		if ( OutputStart != null ) {
			__OutputStart_JTextField.setText ( OutputStart );
		}
		if ( OutputEnd != null ) {
			__OutputEnd_JTextField.setText ( OutputEnd );
		}
		if ( PrecisionForArea != null ) {
			__PrecisionForArea_JTextField.setText ( PrecisionForArea );
		}
		if ( Version == null ) {
			// Select default...
			__Version_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Version_JComboBox, Version, JGUIUtil.NONE, null, null ) ) {
				__Version_JComboBox.select ( Version );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nVersion value \"" + Version +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( OneLocationPerFile == null ) {
			// Select default...
			__OneLocationPerFile_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OneLocationPerFile_JComboBox, OneLocationPerFile, JGUIUtil.NONE, null, null ) ) {
				__OneLocationPerFile_JComboBox.select ( OneLocationPerFile );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nOneLocationPerFile value \"" +
				OneLocationPerFile + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( CheckData == null ) {
			// Select default...
			__CheckData_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__CheckData_JComboBox, CheckData, JGUIUtil.NONE, null, null ) ) {
				__CheckData_JComboBox.select ( CheckData );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nCheckData value \"" +
				CheckData + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( WriteHow == null ) {
			// Select default...
			__WriteHow_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__WriteHow_JComboBox, WriteHow, JGUIUtil.NONE, null, null ) ) {
				__WriteHow_JComboBox.select ( WriteHow );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nWriteHow value \"" +
				WriteHow + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	OutputFile = __OutputFile_JTextField.getText().trim();
	OutputStart = __OutputStart_JTextField.getText().trim();
	OutputEnd = __OutputEnd_JTextField.getText().trim();
	PrecisionForArea = __PrecisionForArea_JTextField.getText().trim();
	Version = __Version_JComboBox.getSelected();
	OneLocationPerFile = __OneLocationPerFile_JComboBox.getSelected();
	CheckData = __CheckData_JComboBox.getSelected();
	WriteHow = __WriteHow_JComboBox.getSelected();

	props = new PropList(__command.getCommandName());
	props.add("OutputFile=" + OutputFile);
	props.add("OutputStart=" + OutputStart);
	props.add("OutputEnd=" + OutputEnd);
	props.add("PrecisionForArea=" + PrecisionForArea);
	props.add("Version=" + Version);
	props.add("OneLocationPerFile=" + OneLocationPerFile);
	props.add("CheckData=" + CheckData);
	props.add("WriteHow=" + WriteHow);
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		if ( (OutputFile != null) && !OutputFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( OutputFile );
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
