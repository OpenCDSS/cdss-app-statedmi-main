// CompareStateCUFiles_JDialog - command editor dialog for simple Compare*Files() commands, which share the same parameters.

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

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Command editor dialog for simple Compare*Files() commands, which share the same parameters.
*/
@SuppressWarnings("serial")
public class CompareStateCUFiles_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse1_JButton = null;
private SimpleJButton __browse2_JButton = null;
private SimpleJButton __path1_JButton = null;
private SimpleJButton __path2_JButton = null;
private String __working_dir = null;	
private JTextField __InputFile1_JTextField = null;
private JTextField __InputFile2_JTextField = null;
private JTextField __Precision_JTextField = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private CompareStateCUFiles_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CompareStateCUFiles_JDialog (JFrame parent, Command command )
{	super(parent, true);
	initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __browse1_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}

		SimpleFileFilter sff = null;
		if ( __command instanceof CompareCropPatternTSFiles_Command ) {
			fc.setDialogTitle("Specify StateCU Crop Pattern TS File to Read");
			sff = new SimpleFileFilter("cds", "StateCU Crop Pattern TS File");
		}
		else if ( __command instanceof CompareIrrigationPracticeTSFiles_Command ) {
			fc.setDialogTitle("Specify StateCU Irrigation Practice TS File to Read");
			sff = new SimpleFileFilter("ipy", "StateCU Irrigation Practice TS File");
		}
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			
			if (filename == null || filename.equals("")) {
				return;
			}
			
			if (path != null) {
				// Convert path to relative path by default.
				try {
					__InputFile1_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CompareStateCUFiles_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}	
	}
	else if ( o == __browse2_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}

		SimpleFileFilter sff = null;
		if ( __command instanceof CompareCropPatternTSFiles_Command ) {
			fc.setDialogTitle("Specify StateCU Crop Pattern TS File to Read");
			sff = new SimpleFileFilter("cds", "StateCU Crop Pattern TS File");
		}
		else if ( __command instanceof CompareIrrigationPracticeTSFiles_Command ) {
			fc.setDialogTitle("Specify StateCU Irrigation Practice TS File to Read");
			sff = new SimpleFileFilter("ipy", "StateCU Irrigation Practice TS File");
		}
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			
			if (filename == null || filename.equals("")) {
				return;
			}
			
			if (path != null) {
				// Convert path to relative path by default.
				try {
					__InputFile2_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CompareStateCUFiles_JDialog", "Error converting file to relative path." );
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
	else if ( o == __path1_JButton ) {
		if ( __path1_JButton.getText().equals(__AddWorkingDirectory) ) {
			__InputFile1_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __InputFile1_JTextField.getText() ) );
		}
		else if ( __path1_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __InputFile1_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __InputFile1_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,__command + "_JDialog", "Error converting file to relative path." );
			}
		}
		refresh ();
	}
	else if ( o == __path2_JButton ) {
		if ( __path2_JButton.getText().equals(__AddWorkingDirectory) ) {
			__InputFile2_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __InputFile2_JTextField.getText() ) );
		}
		else if ( __path2_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __InputFile2_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __InputFile2_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,__command + "_JDialog", "Error converting file to relative path." );
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
	String InputFile1 = __InputFile1_JTextField.getText().trim();
	String InputFile2 = __InputFile2_JTextField.getText().trim();
	
	__error_wait = false;
	
	if (InputFile1.length() > 0) {
		props.set("InputFile1", InputFile1);
	}
	if (InputFile2.length() > 0) {
		props.set("InputFile2", InputFile2);
	}
	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		if (Precision.length() > 0) {
			props.set("Precision", Precision);
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
	String InputFile1 = __InputFile1_JTextField.getText().trim();
	String InputFile2 = __InputFile2_JTextField.getText().trim();

	__command.setCommandParameter("InputFile1", InputFile1);
	__command.setCommandParameter("InputFile2", InputFile2);

	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		__command.setCommandParameter("Precision", Precision);
	}
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, Command command )
{	__command = (CompareStateCUFiles_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
	
	if ( __command instanceof CompareCropPatternTSFiles_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
       		"This command compares two StateCU crop pattern time series files."),
       		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
       	JGUIUtil.addComponent(paragraph, new JLabel (
       		"See the log file for command output."),
       		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof CompareIrrigationPracticeTSFiles_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
       		"This command compares two StateCU irrigation practice time series files."),
       		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
       	JGUIUtil.addComponent(paragraph, new JLabel (
       		"See the log file for command output."),
       		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}

    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that files are specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: "), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("    " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "First input file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile1_JTextField = new JTextField (35);
	__InputFile1_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile1_JPanel = new JPanel();
	InputFile1_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile1_JPanel, __InputFile1_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse1_JButton = new SimpleJButton ( "...", this );
	__browse1_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile1_JPanel, __browse1_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path1_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile1_JPanel, __path1_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, InputFile1_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Second input file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile2_JTextField = new JTextField (35);
	__InputFile2_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile2_JPanel = new JPanel();
	InputFile2_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile2_JPanel, __InputFile2_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse2_JButton = new SimpleJButton ( "...", this );
	__browse2_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile2_JPanel, __browse2_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path2_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile2_JPanel, __path2_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, InputFile2_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	if ( command instanceof CompareCropPatternTSFiles_Command ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Precision:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Precision_JTextField = new JTextField (10);
		__Precision_JTextField.setToolTipText("Number of digits after decimal to compare crop area values");
		__Precision_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __Precision_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - number of digits to compare (default=3)."),
			3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else if ( command instanceof CompareIrrigationPracticeTSFiles_Command ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ("Precision:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Precision_JTextField = new JTextField (10);
		__Precision_JTextField.setToolTipText("Number of digits after decimal to compare irrigated area values.  The precision to compare efficiency values defaults to 2.");
		__Precision_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __Precision_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - number of digits to compare (default=3)."),
			3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
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
private void refresh () {
	String InputFile1 = "";
	String InputFile2 = "";
	String Precision = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile1 = props.getValue ( "InputFile1" );
		InputFile2 = props.getValue ( "InputFile2" );
		Precision = props.getValue ( "Precision" );
		if ( InputFile1 != null ) {
			__InputFile1_JTextField.setText (InputFile1);
		}
		if ( InputFile2 != null ) {
			__InputFile2_JTextField.setText (InputFile2);
		}
		if ( __Precision_JTextField != null ) {
			if ( Precision != null ) {
				__Precision_JTextField.setText (Precision);
			}
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	InputFile1 = __InputFile1_JTextField.getText().trim();
	props.add("InputFile1=" + InputFile1);
	InputFile2 = __InputFile2_JTextField.getText().trim();
	props.add("InputFile2=" + InputFile2);
	if ( __Precision_JTextField != null ) {
		Precision = __Precision_JTextField.getText().trim();
		props.add("Precision=" + Precision);
	}
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path1_JButton != null ) {
		if ( (InputFile1 != null) && !InputFile1.isEmpty() ) {
			__path1_JButton.setEnabled ( true );
			File f = new File ( InputFile1 );
			if ( f.isAbsolute() ) {
				__path1_JButton.setText ( __RemoveWorkingDirectory );
				__path1_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
		    	__path1_JButton.setText ( __AddWorkingDirectory );
		    	__path1_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path1_JButton.setEnabled(false);
		}	
	}
	if ( __path2_JButton != null ) {
		if ( (InputFile2 != null) && !InputFile2.isEmpty() ) {
			__path2_JButton.setEnabled ( true );
			File f = new File ( InputFile2 );
			if ( f.isAbsolute() ) {
				__path2_JButton.setText ( __RemoveWorkingDirectory );
				__path2_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
		    	__path2_JButton.setText ( __AddWorkingDirectory );
		    	__path2_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path2_JButton.setEnabled(false);
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