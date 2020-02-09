// RunR_JDialog - class to edit the RunR() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package rti.tscommandprocessor.commands.r;

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

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
Class to edit the RunR() command.
*/
@SuppressWarnings("serial")
public class RunR_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
    
private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null; // Convert between relative and absolute paths.
private RunR_Command __command = null;	// Command to edit
private String __working_dir = null; // Working directory.
private JTextField __RProgram_JTextField = null;
private JTextArea __ROptions_JTextArea = null;
//private JTextField __RPath_JTextField = null;
private JTextField __ScriptFile_JTextField = null;
private JTextArea __ScriptArguments_JTextArea=null;
private SimpleJComboBox __SetwdHow_JComboBox = null;
private JTextArea __command_JTextArea=null;// Command as TextArea
private boolean __error_wait = false;	// Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether OK was pressed when closing the dialog.

/**
Command constructor.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
public RunR_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
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
		fc.setDialogTitle( "Select R Script");
        SimpleFileFilter sff = new SimpleFileFilter("R", "R script");
        fc.addChoosableFileFilter(sff);
		
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
					__ScriptFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"RunR_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "RunR");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals( __AddWorkingDirectory) ) {
			__ScriptFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __ScriptFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals( __RemoveWorkingDirectory) ) {
			try {
                __ScriptFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __ScriptFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"RunR_JDialog", "Error converting file to relative path." );
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
	String RProgram = __RProgram_JTextField.getText().trim();
	String ROptions = __ROptions_JTextArea.getText().trim();
	//String PythonPath = __PythonPath_JTextField.getText().trim();
	String ScriptFile = __ScriptFile_JTextField.getText().trim();
	String ScriptArguments = __ScriptArguments_JTextArea.getText().trim();
	String SetwdHow = __SetwdHow_JComboBox.getSelected();
	__error_wait = false;
    if ( RProgram.length() > 0 ) {
        props.set ( "RProgram", RProgram );
    }
    if ( ROptions.length() > 0 ) {
        props.set ( "ROptions", ROptions );
    }
    //if ( PythonPath.length() > 0 ) {
        //props.set ( "PythonPath", PythonPath );
    //}
	if ( ScriptFile.length() > 0 ) {
		props.set ( "ScriptFile", ScriptFile );
	}
    if ( ScriptArguments.length() > 0 ) {
        props.set ( "ScriptArguments", ScriptArguments );
    }
    if ( SetwdHow.length() > 0 ) {
        props.set ( "SetwdHow", SetwdHow );
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
private void commitEdits ()
{	String RProgram = __RProgram_JTextField.getText().trim();
    String ROptions = __ROptions_JTextArea.getText().trim();
    //String PythonPath = __PythonPath_JTextField.getText().trim();
    String ScriptFile = __ScriptFile_JTextField.getText().trim();
    String ScriptArguments = __ScriptArguments_JTextArea.getText().trim();
	String SetwdHow = __SetwdHow_JComboBox.getSelected();
    __command.setCommandParameter ( "RProgram", RProgram );
    __command.setCommandParameter ( "ROptions", ROptions );
    //__command.setCommandParameter ( "PythonPath", PythonPath );
	__command.setCommandParameter ( "ScriptFile", ScriptFile );
	__command.setCommandParameter ( "ScriptArguments", ScriptArguments );
	__command.setCommandParameter ( "SetwdHow", SetwdHow );
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (RunR_Command)command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;
	String appName = IOUtil.getProgramName();

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Run an R script, by running an R program.  The Rscript program will be run by default if not specified." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "R scripts are useful for manipulating data outside of " + appName +
        "'s capabilities or as a cross-check for " + appName + " calculations."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify a full or relative path to the script file (relative to working directory)." ), 
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Strings with special meaning can be specified for any parameter and include:" ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "   \\\" - literal quote, needed to surround arguments that include spaces" ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "   ${InstallDirPortable} - the " + appName + " software installation directory, using all forward slashes" ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "   ${WorkingDirPortable} - the working directory (location of command file), using all forward slashes" ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "   ${Property} - other global properties" ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel("The working directory (portable format) is: " +
        	IOUtil.toPortablePath(__working_dir) ), 
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The working directory for the R script must be specified for the R script to use relative paths for files."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "See documentation for examples."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "R program:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RProgram_JTextField = new JTextField ( 40 );
    __RProgram_JTextField.setToolTipText("Specify the R program to run as full path, or blank to search PATH and default locations, can use ${Property}");
    __RProgram_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RProgram_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - program to run (default=find Rscript in PATH and standard locations)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "R program options:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ROptions_JTextArea = new JTextArea ( 4, 55 );
    __ROptions_JTextArea.setToolTipText("Specify the options for the R (Rscript) program, can use ${Property}");
    __ROptions_JTextArea.setLineWrap ( true );
    __ROptions_JTextArea.setWrapStyleWord ( true );
    __ROptions_JTextArea.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ROptions_JTextArea),
        1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Python path:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PythonPath_JTextField = new JTextField ( 40 );
    __PythonPath_JTextField.setToolTipText("Specify the path to add to Python path, can use ${Property}");
    __PythonPath_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __PythonPath_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - add to Python path, use : or ; to separate."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        */

    JGUIUtil.addComponent(main_JPanel, new JLabel ("R script to run:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ScriptFile_JTextField = new JTextField ( 40 );
	__ScriptFile_JTextField.setToolTipText("Specify the R script to run, can use ${Property}");
	__ScriptFile_JTextField.addKeyListener ( this );
    // Input file layout fights back with other rows so put in its own panel
	JPanel ScriptFile_JPanel = new JPanel();
	ScriptFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(ScriptFile_JPanel, __ScriptFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(ScriptFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(ScriptFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, ScriptFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "R script arguments:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ScriptArguments_JTextArea = new JTextArea ( 4, 55 );
    __ScriptArguments_JTextArea.setToolTipText("Specify the arguments for the R script, can use ${Property}");
    __ScriptArguments_JTextArea.setLineWrap ( true );
    __ScriptArguments_JTextArea.setWrapStyleWord ( true );
    __ScriptArguments_JTextArea.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ScriptArguments_JTextArea),
        1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "setwd() how?:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SetwdHow_JComboBox = new SimpleJComboBox ( false );
    __SetwdHow_JComboBox.setToolTipText("How will setwd() be specified?  Default is setwd() expression on command line.");
    List<String> interpreterChoices = new ArrayList<>();
    // Force setting on command line as default until other approach is determined
    interpreterChoices.add ( __command._IncludedInArguments );
    __SetwdHow_JComboBox.setData(interpreterChoices);
    __SetwdHow_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __SetwdHow_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - how setwd() is specified for for R script."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 55 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add (__cancel_JButton = new SimpleJButton("Cancel",this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command" );
    pack();
    JGUIUtil.center( this );
	refresh();	// Sets the __path_JButton status
	// Dialogs do not need to be resizable...
	setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

    refresh ();
	if ( code == KeyEvent.VK_ENTER ) {
		checkInput ();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{	refresh();
}

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
{	//String routine = __command.getCommandName() + "_JDialog.refresh";
    String RProgram = "";
    String ROptions = "";
    //String PythonPath = "";
    String ScriptFile = "";
    String ScriptArguments = "";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		props = __command.getCommandParameters();
	    RProgram = props.getValue ( "RProgram" );
	    ROptions = props.getValue ( "ROptions" );
	    //PythonPath = props.getValue ( "PythonPath" );
		ScriptFile = props.getValue ( "ScriptFile" );
		ScriptArguments = props.getValue ( "ScriptArguments" );
        if ( RProgram != null ) {
            __RProgram_JTextField.setText ( RProgram );
        }
        if ( ROptions != null ) {
            __ROptions_JTextArea.setText ( ROptions );
        }
        //if ( PythonPath != null ) {
        //    __PythonPath_JTextField.setText ( PythonPath );
        //}
		if ( ScriptFile != null ) {
			__ScriptFile_JTextField.setText ( ScriptFile );
		}
        if ( ScriptArguments != null ) {
            __ScriptArguments_JTextArea.setText ( ScriptArguments );
        }
	}
	// Regardless, reset the command from the fields...
	RProgram = __RProgram_JTextField.getText().trim();
	ROptions = __ROptions_JTextArea.getText().trim();
	//PythonPath = __PythonPath_JTextField.getText().trim();
	ScriptFile = __ScriptFile_JTextField.getText().trim();
	ScriptArguments = __ScriptArguments_JTextArea.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "RProgram=" + RProgram );
    props.add ( "ROptions=" + ROptions );
    //props.add ( "PythonPath=" + PythonPath );
	props.add ( "ScriptFile=" + ScriptFile );
	props.add ( "ScriptArguments=" + ScriptArguments );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		if ( (ScriptFile != null) && !ScriptFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( ScriptFile );
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
@param ok if false, then the edit is canceled.  If true, the edit is committed
and the dialog is closed.
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
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
