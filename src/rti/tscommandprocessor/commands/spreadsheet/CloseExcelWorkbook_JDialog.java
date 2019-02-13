// CloseExcelWorkbook_JDialog - Editor for the CloseExcelWorkbook command.

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

package rti.tscommandprocessor.commands.spreadsheet;

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

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
Editor for the CloseExcelWorkbook command.
*/
@SuppressWarnings("serial")
public class CloseExcelWorkbook_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rel";
private final String __AddWorkingDirectoryToNewFile = "Abs";
private final String __RemoveWorkingDirectoryFromNewFile = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea = null;
private JTextField __OutputFile_JTextField = null;
private JTextField __NewOutputFile_JTextField = null;
private SimpleJComboBox __WriteFile_JComboBox = null;
private SimpleJComboBox __RecalculateFormulasAtOpen_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __browseNew_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __pathNew_JButton = null;
private String __working_dir = null;	
private CloseExcelWorkbook_Command __command = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CloseExcelWorkbook_JDialog ( JFrame parent, CloseExcelWorkbook_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( (o == __browse_JButton) || (o == __browseNew_JButton) ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
            fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select Excel File");
     	fc.addChoosableFileFilter(new SimpleFileFilter("xls", "Excel File"));
		SimpleFileFilter sff = new SimpleFileFilter("xlsx", "Excel File");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			if ( o == __browseNew_JButton ) {
				// Convert path to relative path by default.
				try {
					__NewOutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CloseExcelWorkbook_JDialog", "Error converting file to relative path." );
				}
			}
			else {
				// Convert path to relative path by default.
				try {
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CloseExcelWorkbook_JDialog", "Error converting file to relative path." );
				}
			}
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "CloseExcelWorkbook");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals( __AddWorkingDirectoryToFile)) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectoryFromFile)) {
			try {
                __OutputFile_JTextField.setText ( IOUtil.toRelativePath (__working_dir,
                        __OutputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command.getCommandName() + "_JDialog", "Error converting file to relative path.");
			}
		}
		refresh ();
	}
	else if ( o == __pathNew_JButton ) {
		if (__pathNew_JButton.getText().equals( __AddWorkingDirectoryToNewFile)) {
			__NewOutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__NewOutputFile_JTextField.getText()));
		}
		else if (__pathNew_JButton.getText().equals( __RemoveWorkingDirectoryFromNewFile)) {
			try {
                __NewOutputFile_JTextField.setText ( IOUtil.toRelativePath (__working_dir,
                    __NewOutputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command.getCommandName() + "_JDialog", "Error converting file to relative path.");
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
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String NewOutputFile = __NewOutputFile_JTextField.getText().trim();
	String WriteFile = __WriteFile_JComboBox.getSelected();
	String RecalculateFormulasAtOpen = __RecalculateFormulasAtOpen_JComboBox.getSelected();
	__error_wait = false;

	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
	if ( NewOutputFile.length() > 0 ) {
		props.set ( "NewOutputFile", NewOutputFile );
	}
	if ( WriteFile.length() > 0 ) {
		props.set ( "WriteFile", WriteFile );
	}
	if ( RecalculateFormulasAtOpen.length() > 0 ) {
		props.set ( "RecalculateFormulasAtOpen", RecalculateFormulasAtOpen );
	}
	try {
	    // This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
        Message.printWarning(2,"", e);
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String OutputFile = __OutputFile_JTextField.getText().trim();
	String NewOutputFile = __NewOutputFile_JTextField.getText().trim();
	String WriteFile = __WriteFile_JComboBox.getSelected();
	String RecalculateFormulasAtOpen = __RecalculateFormulasAtOpen_JComboBox.getSelected();
    __command.setCommandParameter ( "OutputFile", OutputFile );
    __command.setCommandParameter ( "NewOutputFile", NewOutputFile );
    __command.setCommandParameter ( "WriteFile", WriteFile );
    __command.setCommandParameter ( "RecalculateFormulasAtOpen", RecalculateFormulasAtOpen );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, CloseExcelWorkbook_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

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
    	"This command closes a Microsoft Excel workbook and optionally writes the file (*.xls, *.xlsx)." ),
    	0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "A workbook object is created in memory by commands that read and write Excel." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "The workbook is kept open in memory after processing a read/write command if the KeepOpen=True command parameter is used." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "This allows subsequent commands to read/write the same Excel workbook without initialization overhead." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "If KeepOpen=False (default for most commmands), the read or write operation is performed and the file is closed." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "By default, if the workbook was originally opened for reading, then this CloseExcelWorkbook command will close without writing the file." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "If the workbook was originally opened for writing, then this CloseExcelWorkbook command will write the file and close the workbook." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
   	    "Use the WriteFile parameter to control whether to override defaults for writing the file." ),
   	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        "The working directory is: " + __working_dir), 
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	   	0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Original workbook file (required):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
	__OutputFile_JTextField.setToolTipText("Specify the path to the Excel file or use ${Property} notation - used to idetify workbook.");
	__OutputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("...", this);
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromFile,this);
	    JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("New output file (optional):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NewOutputFile_JTextField = new JTextField (45);
	__NewOutputFile_JTextField.setToolTipText("Specify the path to the new Excel file or use ${Property} notation - to avoid overwriting original input.");
	__NewOutputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __NewOutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browseNew_JButton = new SimpleJButton ("...", this);
	__browseNew_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(main_JPanel, __browseNew_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__pathNew_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromNewFile,this);
	    JGUIUtil.addComponent(main_JPanel, __pathNew_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
        
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Write file?:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __WriteFile_JComboBox = new SimpleJComboBox ( false );
    __WriteFile_JComboBox.add("");
    __WriteFile_JComboBox.add(__command._False);
    __WriteFile_JComboBox.add(__command._True);
    __WriteFile_JComboBox.select ( 0 );
    __WriteFile_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __WriteFile_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - write file? (default=see notes above)."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Recalculate formulas?:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RecalculateFormulasAtOpen_JComboBox = new SimpleJComboBox ( false );
    __RecalculateFormulasAtOpen_JComboBox.add("");
    __RecalculateFormulasAtOpen_JComboBox.add(__command._False);
    __RecalculateFormulasAtOpen_JComboBox.add(__command._True);
    __RecalculateFormulasAtOpen_JComboBox.select ( 0 );
    __RecalculateFormulasAtOpen_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RecalculateFormulasAtOpen_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - recalculate formulas when Excel opens? (default=" + __command._True + ")."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command");
    pack();
    JGUIUtil.center(this);
	refresh();	// Sets the __path_JButton status
	setResizable (false);
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
			response ( true );
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{   String routine = getClass().getSimpleName() + ".refresh";
	String OutputFile = "";
	String NewOutputFile = "";
	String WriteFile = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		OutputFile = props.getValue ( "OutputFile" );
		NewOutputFile = props.getValue ( "NewOutputFile" );
		WriteFile = props.getValue ( "WriteFile" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
		if ( NewOutputFile != null ) {
			__NewOutputFile_JTextField.setText ( NewOutputFile );
		}
        if ( WriteFile == null || WriteFile.equals("") ) {
            // Select a default...
            __WriteFile_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __WriteFile_JComboBox, WriteFile, JGUIUtil.NONE, null, null ) ) {
                __WriteFile_JComboBox.select ( WriteFile );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nWriteFile \"" +
                    WriteFile + "\".  Select a different choice or Cancel." );
            }
        }
 	}
	// Regardless, reset the command from the fields...
	OutputFile = __OutputFile_JTextField.getText().trim();
	NewOutputFile = __NewOutputFile_JTextField.getText().trim();
	WriteFile = __WriteFile_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "NewOutputFile=" + NewOutputFile );
	props.add ( "WriteFile=" + WriteFile );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (OutputFile);
		if (f.isAbsolute()) {
			__path_JButton.setText (__RemoveWorkingDirectoryFromFile);
		}
		else {
            __path_JButton.setText (__AddWorkingDirectoryToFile);
		}
	}
	if (__pathNew_JButton != null) {
		__pathNew_JButton.setEnabled (true);
		File f = new File (NewOutputFile);
		if (f.isAbsolute()) {
			__pathNew_JButton.setText (__RemoveWorkingDirectoryFromNewFile);
			__path_JButton.setToolTipText("Change path to relative to command file");
		}
		else {
            __pathNew_JButton.setText (__AddWorkingDirectoryToNewFile);
			__path_JButton.setToolTipText("Change path to absolute");
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
*/
private void response ( boolean ok )
{	__ok = ok;	// Save to be returned by ok()
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
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}
