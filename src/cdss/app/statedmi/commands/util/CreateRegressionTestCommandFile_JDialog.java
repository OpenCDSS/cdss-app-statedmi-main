// CreateRegressionTestCommandFile_JDialog - Editor for CreateRegressionTestCommandFile command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

//package rti.tscommandprocessor.commands.util;
package cdss.app.statedmi.commands.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import DWR.DMI.StateDMI.StateDMI_Processor;
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
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

/**
Editor for CreateRegressionTestCommandFile command.
*/
@SuppressWarnings("serial")
public class CreateRegressionTestCommandFile_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __browseSearchFolder_JButton = null;
private SimpleJButton __browseOutputFile_JButton = null;
private SimpleJButton __browseSetupCommandFile_JButton = null;
private SimpleJButton __browseTestResultsFile_JButton = null;
private SimpleJButton __browseEndCommandFile_JButton = null;
private SimpleJButton __pathSearchFolder_JButton = null;
private SimpleJButton __pathOutputFile_JButton = null;
private SimpleJButton __pathSetupCommandFile_JButton = null;
private SimpleJButton __pathTestResultsFile_JButton = null;
private SimpleJButton __pathEndCommandFile_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private JTextField __SearchFolder_JTextField = null;	// Top folder to start search.
private JTextField __FilenamePattern_JTextField = null;	// Pattern for file names.
private JTextField __OutputFile_JTextField = null;	// Resulting command file.
private JTextField __SetupCommandFile_JTextField = null; // Setup command file.
private JTextField __TestResultsFile_JTextField = null;
private JTextField __EndCommandFile_JTextField = null;
private SimpleJComboBox __Append_JComboBox = null;
private JTextField __IncludeTestSuite_JTextField = null;
private JTextField __ExcludeTestSuite_JTextField = null;
private JTextField __IncludeOS_JTextField = null;
private SimpleJComboBox __UseOrder_JComboBox = null;
private JTextField __TestResultsTableID_JTextField = null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private CreateRegressionTestCommandFile_Command __command = null;
private boolean __ok = false; // Indicates whether the user has pressed OK to close the dialog.

/**
Dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CreateRegressionTestCommandFile_JDialog ( JFrame parent, CreateRegressionTestCommandFile_Command command ) {
	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event ) {
	Object o = event.getSource();
	String routine = getClass().getSimpleName() + ".actionPerformed";

	if ( o == __browseSearchFolder_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY );
		fc.setDialogTitle( "Select Folder to Search For Command Files");

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String folder = fc.getSelectedFile().getName();
			String path = fc.getSelectedFile().getPath();

			if (folder == null || folder.equals("")) {
				return;
			}

			if (path != null) {
				// Convert path to relative path by default.
				try {
					if ( __SearchFolder_JTextField.getText().trim().length() == 0 ) {
						// Set the value.
						__SearchFolder_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
					}
					else {
						// Append to the existing folder list with comma delimiter.
						__SearchFolder_JTextField.setText(__SearchFolder_JTextField.getText() + "," + IOUtil.toRelativePath(__working_dir, path));
					}
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CreateRegressionTestCommandFile_JDialog", "Error converting file to relative path." );
				}
				File pathFolder = new File(path);
				if ( pathFolder.exists() ) {
					JGUIUtil.setLastFileDialogDirectory(path);
				}
				refresh();
			}
		}
	}
	else if ( o == __browseSetupCommandFile_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle( "Select Setup Command File to Include at Start");
        SimpleFileFilter sff = new SimpleFileFilter("StateDMI","StateDMI Command File");
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
					__SetupCommandFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CreateRegressionTestCommandFile_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(directory);
                refresh();
            }
        }
    }
	else if ( o == __browseTestResultsFile_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle( "Select Test Results File");
        SimpleFileFilter sff = new SimpleFileFilter("txt","test results report file");
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
					__TestResultsFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CreateRegressionTestCommandFile_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(directory);
                refresh();
            }
        }
    }
	else if ( o == __browseEndCommandFile_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle( "Select Command File to Include at End");
        SimpleFileFilter sff = new SimpleFileFilter("StateDMI","StateDMI Command File");
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
					__EndCommandFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CreateRegressionTestCommandFile_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(directory);
                refresh();
            }
        }
    }
	else if ( o == __browseOutputFile_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle( "Select Command File to Create");
        SimpleFileFilter sff = new SimpleFileFilter("StateDMI","StateDMI Command File");
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
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CreateRegressionTestCommandFile_JDialog", "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "CreateRegressionTestCommandFile");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __pathSearchFolder_JButton ) {
		if ( __pathSearchFolder_JButton.getText().equals( __AddWorkingDirectory) ) {
			__SearchFolder_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __SearchFolder_JTextField.getText() ) );
		}
		else if ( __pathSearchFolder_JButton.getText().equals( __RemoveWorkingDirectory) ) {
			try {
			    __SearchFolder_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir, __SearchFolder_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,routine,
				"Error converting folder name to relative path." );
			}
		}
		refresh ();
	}
	else if ( o == __pathOutputFile_JButton ) {
		if ( __pathOutputFile_JButton.getText().equals( __AddWorkingDirectory) ) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText() ) );
		}
		else if ( __pathOutputFile_JButton.getText().equals( __RemoveWorkingDirectory) ) {
			try {
			    __OutputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir,__OutputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,routine,"Error converting output file name to relative path." );
			}
		}
		refresh ();
	}
    else if ( o == __pathSetupCommandFile_JButton ) {
        if ( __pathSetupCommandFile_JButton.getText().equals( __AddWorkingDirectory) ) {
            __SetupCommandFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir,__SetupCommandFile_JTextField.getText() ) );
        }
        else if ( __pathSetupCommandFile_JButton.getText().equals( __RemoveWorkingDirectory) ) {
            try {
                __SetupCommandFile_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir,__SetupCommandFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1,routine,"Error converting setup command file name to relative path." );
            }
        }
        refresh ();
    }
    else if ( o == __pathTestResultsFile_JButton ) {
        if ( __pathTestResultsFile_JButton.getText().equals( __AddWorkingDirectory) ) {
            __TestResultsFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir,__TestResultsFile_JTextField.getText() ) );
        }
        else if ( __pathTestResultsFile_JButton.getText().equals( __RemoveWorkingDirectory) ) {
            try {
                __TestResultsFile_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir,__TestResultsFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1,routine,"Error converting test results file name to relative path." );
            }
        }
        refresh ();
    }
    else if ( o == __pathEndCommandFile_JButton ) {
        if ( __pathEndCommandFile_JButton.getText().equals( __AddWorkingDirectory) ) {
            __EndCommandFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir,__EndCommandFile_JTextField.getText() ) );
        }
        else if ( __pathEndCommandFile_JButton.getText().equals( __RemoveWorkingDirectory) ) {
            try {
                __EndCommandFile_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir,__EndCommandFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1,routine,"Error converting end command file name to relative path." );
            }
        }
        refresh ();
    }
	else {
	    // Choices.
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
This should be called before response() is allowed to complete.
*/
private void checkInput () {
	// Put together a list of parameters to check.
	PropList props = new PropList ( "" );
	String SearchFolder = __SearchFolder_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String SetupCommandFile = __SetupCommandFile_JTextField.getText().trim();
	String TestResultsFile = __TestResultsFile_JTextField.getText().trim();
	String EndCommandFile = __EndCommandFile_JTextField.getText().trim();
	String FilenamePattern = __FilenamePattern_JTextField.getText().trim();
	String Append = __Append_JComboBox.getSelected();
	String IncludeTestSuite = __IncludeTestSuite_JTextField.getText().trim();
	String ExcludeTestSuite = __ExcludeTestSuite_JTextField.getText().trim();
	String IncludeOS = __IncludeOS_JTextField.getText().trim();
	String UseOrder = __UseOrder_JComboBox.getSelected();
	String TestResultsTableID = __TestResultsTableID_JTextField.getText().trim();

	if ( SearchFolder.length() > 0 ) {
		props.set ( "SearchFolder", SearchFolder );
	}
	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
    if ( SetupCommandFile.length() > 0 ) {
        props.set ( "SetupCommandFile", SetupCommandFile );
    }
    if ( TestResultsFile.length() > 0 ) {
        props.set ( "TestResultsFile", TestResultsFile );
    }
    if ( EndCommandFile.length() > 0 ) {
        props.set ( "EndCommandFile", EndCommandFile );
    }
    if ( FilenamePattern.length() > 0 ) {
        props.set ( "FilenamePattern", FilenamePattern );
    }
	if ( Append.length() > 0 ) {
		props.set ( "Append", Append );
	}
    if ( IncludeTestSuite.length() > 0 ) {
        props.set ( "IncludeTestSuite", IncludeTestSuite );
    }
    if ( ExcludeTestSuite.length() > 0 ) {
        props.set ( "ExcludeTestSuite", ExcludeTestSuite );
    }
    if ( IncludeOS.length() > 0 ) {
        props.set ( "IncludeOS", IncludeOS );
    }
	if ( UseOrder.length() > 0 ) {
		props.set ( "UseOrder", UseOrder );
	}
	if ( TestResultsTableID.length() > 0 ) {
		props.set ( "TestResultsTableID", TestResultsTableID );
	}
	try {
	    // This will warn the user.
		__command.checkCommandParameters ( props, null, 1 );
		__error_wait = false;
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.
In this case the command parameters have already been checked and no errors were detected.
*/
private void commitEdits () {
	String SearchFolder = __SearchFolder_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText();
	String SetupCommandFile = __SetupCommandFile_JTextField.getText().trim();
	String TestResultsFile = __TestResultsFile_JTextField.getText().trim();
	String EndCommandFile = __EndCommandFile_JTextField.getText().trim();
	String FilenamePattern = __FilenamePattern_JTextField.getText().trim();
	String Append = __Append_JComboBox.getSelected();
	String IncludeTestSuite = __IncludeTestSuite_JTextField.getText().trim();
	String ExcludeTestSuite = __ExcludeTestSuite_JTextField.getText().trim();
	String IncludeOS = __IncludeOS_JTextField.getText().trim();
	String UseOrder = __UseOrder_JComboBox.getSelected();
	String TestResultsTableID = __TestResultsTableID_JTextField.getText().trim();
	__command.setCommandParameter ( "SearchFolder", SearchFolder );
    __command.setCommandParameter ( "OutputFile", OutputFile );
    __command.setCommandParameter ( "SetupCommandFile", SetupCommandFile );
    __command.setCommandParameter ( "TestResultsFile", TestResultsFile );
    __command.setCommandParameter ( "EndCommandFile", EndCommandFile );
	__command.setCommandParameter ( "FilenamePattern", FilenamePattern );
	__command.setCommandParameter ( "Append", Append );
	__command.setCommandParameter ( "IncludeTestSuite", IncludeTestSuite );
	__command.setCommandParameter ( "ExcludeTestSuite", ExcludeTestSuite );
	__command.setCommandParameter ( "IncludeOS", IncludeOS );
	__command.setCommandParameter ( "UseOrder", UseOrder );
	__command.setCommandParameter ( "TestResultsTableID", TestResultsTableID );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, CreateRegressionTestCommandFile_Command command ) {
	__command = command;
	CommandProcessor processor =__command.getCommandProcessor();

	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel.

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command creates a regression test command file, for use in software testing." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Test command files should follow documented standards." ),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"One or more top-level folders are specified and will be searched for command files matching the specified pattern(s)."),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"The resulting output command file will include RunCommands() commands for each matched file," +
    	" and can be independently loaded and run."),
    	0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Command files within top-level folder are sorted alphabetically to facilitate review.  Use appropriate filenames or #@order comment to control run order."),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "A \"setup\" command file can be inserted at the top of the generated command file, for example to initialize " +
        "database connections."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "An \"end\" command file can be inserted at the end of the generated command file, for example to process the summary table."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that file names are relative to the working directory, which is:"),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    " + __working_dir),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Folder(s) to search for StateDMI command files:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SearchFolder_JTextField = new JTextField ( 50 );
	__SearchFolder_JTextField.setToolTipText("Specify one or more top-level folders separated by commas to search for StateDMI command files, can use ${Property} notation");
	__SearchFolder_JTextField.addKeyListener ( this );
    // Search folder layout fights back with other rows so put in its own panel.
	JPanel SearchFolder_JPanel = new JPanel();
	SearchFolder_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(SearchFolder_JPanel, __SearchFolder_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browseSearchFolder_JButton = new SimpleJButton ( "...", this );
	__browseSearchFolder_JButton.setToolTipText("Browse for folder");
    JGUIUtil.addComponent(SearchFolder_JPanel, __browseSearchFolder_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path.
		__pathSearchFolder_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(SearchFolder_JPanel, __pathSearchFolder_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, SearchFolder_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command file to create:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField ( 50 );
	__OutputFile_JTextField.setToolTipText("Specify command file to create, can use ${Property} notation");
	__OutputFile_JTextField.addKeyListener ( this );
    // Output file layout fights back with other rows so put in its own panel.
	JPanel OutputFile_JPanel = new JPanel();
	OutputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(OutputFile_JPanel, __OutputFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browseOutputFile_JButton = new SimpleJButton ( "...", this );
	__browseOutputFile_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(OutputFile_JPanel, __browseOutputFile_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path.
		__pathOutputFile_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(OutputFile_JPanel, __pathOutputFile_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, OutputFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Setup command file:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SetupCommandFile_JTextField = new JTextField ( 50 );
    __SetupCommandFile_JTextField.setToolTipText("Specify the setup command file to prepend, can use ${Property} notation");
    __SetupCommandFile_JTextField.addKeyListener ( this );
    // Setup command file layout fights back with other rows so put in its own panel.
	JPanel SetupCommandFile_JPanel = new JPanel();
	SetupCommandFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(SetupCommandFile_JPanel, __SetupCommandFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browseSetupCommandFile_JButton = new SimpleJButton ( "...", this );
	__browseSetupCommandFile_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(SetupCommandFile_JPanel, __browseSetupCommandFile_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path.
		__pathSetupCommandFile_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(SetupCommandFile_JPanel, __pathSetupCommandFile_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, SetupCommandFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Test results file:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TestResultsFile_JTextField = new JTextField ( 50 );
    __TestResultsFile_JTextField.setToolTipText("Test results file for StartRegressionTestResultsReport(OutputFile=...), can use ${Property} notation");
    __TestResultsFile_JTextField.addKeyListener ( this );
    // Setup command file layout fights back with other rows so put in its own panel.
	JPanel TestResultsFile_JPanel = new JPanel();
	TestResultsFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(TestResultsFile_JPanel, __TestResultsFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browseTestResultsFile_JButton = new SimpleJButton ( "...", this );
	__browseTestResultsFile_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(TestResultsFile_JPanel, __browseTestResultsFile_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path.
		__pathTestResultsFile_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(TestResultsFile_JPanel, __pathTestResultsFile_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, TestResultsFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "End command file:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __EndCommandFile_JTextField = new JTextField ( 50 );
    __EndCommandFile_JTextField.setToolTipText("Specify the end command file to append, can use ${Property} notation");
    __EndCommandFile_JTextField.addKeyListener ( this );
    // End command file layout fights back with other rows so put in its own panel.
	JPanel EndCommandFile_JPanel = new JPanel();
	EndCommandFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(EndCommandFile_JPanel, __EndCommandFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__browseEndCommandFile_JButton = new SimpleJButton ( "...", this );
	__browseEndCommandFile_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(EndCommandFile_JPanel, __browseEndCommandFile_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path.
		__pathEndCommandFile_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(EndCommandFile_JPanel, __pathEndCommandFile_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, EndCommandFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command file name pattern(s):" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __FilenamePattern_JTextField = new JTextField ( 30 );
    __FilenamePattern_JTextField.setToolTipText("File pattern(s) to match, * is wildcard, multiple patterns separated by commas, ignore case.");
    __FilenamePattern_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __FilenamePattern_JTextField,
    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - file pattern(s) to match (default is \"Test_*.StateDMI\",\"test-*.statedmi\")."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Append to output?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Append_JComboBox = new SimpleJComboBox ( false );
	List<String> appendChoices = new ArrayList<>();
	appendChoices.add ( "" );	// Default.
	appendChoices.add ( __command._False );
	appendChoices.add ( __command._True );
	__Append_JComboBox.setData(appendChoices);
	__Append_JComboBox.select ( 0 );
	__Append_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Append_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Optional - append to command file? (default=" + __command._True + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Test suites to include:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeTestSuite_JTextField = new JTextField ( "", 30 );
    __IncludeTestSuite_JTextField.setToolTipText("Test suites to include, separated by commas.");
    __IncludeTestSuite_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeTestSuite_JTextField,
    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel( "Optional - check \"#@testSuite ABC\" comments for tests to include (default=*)."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Test suites to exclude:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeTestSuite_JTextField = new JTextField ( "", 30 );
    // TODO smalers 2021-10-15 'nosuite' is probably not needed so remove later.
    //__ExcludeTestSuite_JTextField.setToolTipText("Test suites to exclude, separated by commas. Use 'nosuite' to exclude tests that don't have #@testSuite.");
    __ExcludeTestSuite_JTextField.setToolTipText("Test suites to exclude, separated by commas.");
    __ExcludeTestSuite_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ExcludeTestSuite_JTextField,
    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel( "Optional - check \"#@testSuite ABC\" comments for tests to exclude (default=none)."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include tests for OS:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeOS_JTextField = new JTextField ( "", 30 );
    __IncludeOS_JTextField.setToolTipText("Operating systems to include test: Windows, UNIX, Linux");
    __IncludeOS_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeOS_JTextField,
    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel( "Optional - check \"#@os Windows|UNIX\" comments for tests to include (default=*)."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Use @order?"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__UseOrder_JComboBox = new SimpleJComboBox ( false );
	List<String> orderChoices = new ArrayList<>();
	orderChoices.add ( "" );	// Default.
	orderChoices.add ( __command._False );
	orderChoices.add ( __command._True );
	__UseOrder_JComboBox.setData(orderChoices);
	__UseOrder_JComboBox.select ( 0 );
	__UseOrder_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __UseOrder_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Optional - use @order for sorting tests? (default=" + __command._True + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Test results table ID:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TestResultsTableID_JTextField = new JTextField (10);
    __TestResultsTableID_JTextField.setToolTipText("Table identifier to save test results.");
    __TestResultsTableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __TestResultsTableID_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - identifier for table containing results."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 60 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.addKeyListener ( this );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents.
	refresh ();

	// Panel for buttons.
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel,
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " command" );

    pack();
    JGUIUtil.center( this );
	// Dialogs do not need to be resizable.
	setResizable ( false );
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event ) {
	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
	}
}

public void keyReleased ( KeyEvent event ) {
	refresh();
}

public void keyTyped ( KeyEvent event ) {
}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok () {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh () {
	String routine = getClass().getSimpleName() + ".refresh";
	String SearchFolder = "";
	String SetupCommandFile = "";
	String TestResultsFile = "";
	String EndCommandFile = "";
	String OutputFile = "";
	String FilenamePattern = "";
	String Append = "";
	String IncludeTestSuite = "*";
	String ExcludeTestSuite = "*";
	String IncludeOS = "*";
	String UseOrder = "";
	String TestResultsTableID = "";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		props = __command.getCommandParameters();
		SearchFolder = props.getValue ( "SearchFolder" );
		OutputFile = props.getValue ( "OutputFile" );
		SetupCommandFile = props.getValue ( "SetupCommandFile" );
		TestResultsFile = props.getValue ( "TestResultsFile" );
		EndCommandFile = props.getValue ( "EndCommandFile" );
	    FilenamePattern = props.getValue ( "FilenamePattern" );
		Append = props.getValue ( "Append" );
		IncludeTestSuite = props.getValue ( "IncludeTestSuite" );
		ExcludeTestSuite = props.getValue ( "ExcludeTestSuite" );
		IncludeOS = props.getValue ( "IncludeOS" );
		UseOrder = props.getValue ( "UseOrder" );
		TestResultsTableID = props.getValue ( "TestResultsTableID" );
		if ( SearchFolder != null ) {
			__SearchFolder_JTextField.setText ( SearchFolder );
		}
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
        if ( SetupCommandFile != null ) {
            __SetupCommandFile_JTextField.setText ( SetupCommandFile );
        }
        if ( TestResultsFile != null ) {
            __TestResultsFile_JTextField.setText ( TestResultsFile );
        }
        if ( EndCommandFile != null ) {
            __EndCommandFile_JTextField.setText ( EndCommandFile );
        }
        if ( FilenamePattern != null ) {
            __FilenamePattern_JTextField.setText ( FilenamePattern );
        }
		if ( JGUIUtil.isSimpleJComboBoxItem( __Append_JComboBox, Append, JGUIUtil.NONE, null, null ) ) {
			__Append_JComboBox.select ( Append );
		}
		else {
		    if ( (Append == null) || Append.equals("") ) {
				// New command...select the default.
				__Append_JComboBox.select ( 0 );
			}
			else {
			    // Bad user command.
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nAppend parameter \"" +
				Append + "\".  Select a\ndifferent value or Cancel." );
			}
		}
        if ( IncludeTestSuite != null ) {
            __IncludeTestSuite_JTextField.setText ( IncludeTestSuite );
        }
        if ( ExcludeTestSuite != null ) {
            __ExcludeTestSuite_JTextField.setText ( ExcludeTestSuite );
        }
        if ( IncludeOS != null ) {
            __IncludeOS_JTextField.setText ( IncludeOS );
        }
		if ( JGUIUtil.isSimpleJComboBoxItem( __UseOrder_JComboBox, UseOrder, JGUIUtil.NONE, null, null ) ) {
			__UseOrder_JComboBox.select ( UseOrder );
		}
		else {
		    if ( (UseOrder == null) || UseOrder.equals("") ) {
				// New command...select the default.
				__UseOrder_JComboBox.select ( 0 );
			}
			else {
			    // Bad user command...
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nUseOrder parameter \"" +
				UseOrder + "\".  Select a\ndifferent value or Cancel." );
			}
		}
		if ( TestResultsTableID != null ) {
			__TestResultsTableID_JTextField.setText ( TestResultsTableID );
		}
	}
	// Regardless, reset the command from the fields.
	// This is only  visible information that has not been committed in the command.
	SearchFolder = __SearchFolder_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	SetupCommandFile = __SetupCommandFile_JTextField.getText().trim();
	TestResultsFile = __TestResultsFile_JTextField.getText().trim();
	EndCommandFile = __EndCommandFile_JTextField.getText().trim();
	FilenamePattern = __FilenamePattern_JTextField.getText().trim();
	Append = __Append_JComboBox.getSelected();
	IncludeTestSuite = __IncludeTestSuite_JTextField.getText().trim();
	ExcludeTestSuite = __ExcludeTestSuite_JTextField.getText().trim();
	IncludeOS = __IncludeOS_JTextField.getText().trim();
	UseOrder = __UseOrder_JComboBox.getSelected();
	TestResultsTableID = __TestResultsTableID_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "SearchFolder=" + SearchFolder );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "SetupCommandFile=" + SetupCommandFile );
	props.add ( "TestResultsFile=" + TestResultsFile );
	props.add ( "EndCommandFile=" + EndCommandFile );
	props.add ( "FilenamePattern=" + FilenamePattern );
	props.add ( "Append=" + Append );
	props.add ( "IncludeTestSuite=" + IncludeTestSuite );
	props.add ( "ExcludeTestSuite=" + ExcludeTestSuite );
	props.add ( "IncludeOS=" + IncludeOS );
	props.add ( "UseOrder=" + UseOrder );
	props.add ( "TestResultsTableID=" + TestResultsTableID );
	__command_JTextArea.setText( __command.toString(props).trim() );
	// Check the path and determine what the label on the path button should be.
	if ( __pathSearchFolder_JButton != null ) {
		if ( (SearchFolder != null) && !SearchFolder.isEmpty() && (SearchFolder.indexOf(",") < 0) ) {
			__pathSearchFolder_JButton.setEnabled ( true );
			File f = new File ( SearchFolder );
			if ( f.isAbsolute() ) {
				__pathSearchFolder_JButton.setText ( __RemoveWorkingDirectory );
				__pathSearchFolder_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathSearchFolder_JButton.setText ( __AddWorkingDirectory );
            	__pathSearchFolder_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathSearchFolder_JButton.setEnabled(false);
		}
	}
	if ( __pathOutputFile_JButton != null ) {
		if ( (OutputFile != null) && !OutputFile.isEmpty() ) {
			__pathOutputFile_JButton.setEnabled ( true );
			File f = new File ( OutputFile );
			if ( f.isAbsolute() ) {
				__pathOutputFile_JButton.setText ( __RemoveWorkingDirectory );
				__pathOutputFile_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathOutputFile_JButton.setText ( __AddWorkingDirectory );
            	__pathOutputFile_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathOutputFile_JButton.setEnabled(false);
		}
	}
    if ( __pathSetupCommandFile_JButton != null ) {
		if ( (SetupCommandFile != null) && !SetupCommandFile.isEmpty() ) {
			__pathSetupCommandFile_JButton.setEnabled ( true );
			File f = new File ( SetupCommandFile );
			if ( f.isAbsolute() ) {
				__pathSetupCommandFile_JButton.setText ( __RemoveWorkingDirectory );
				__pathSetupCommandFile_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathSetupCommandFile_JButton.setText ( __AddWorkingDirectory );
            	__pathSetupCommandFile_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathSetupCommandFile_JButton.setEnabled(false);
		}
    }
    if ( __pathTestResultsFile_JButton != null ) {
		if ( (TestResultsFile != null) && !TestResultsFile.isEmpty() ) {
			__pathTestResultsFile_JButton.setEnabled ( true );
			File f = new File ( TestResultsFile );
			if ( f.isAbsolute() ) {
				__pathTestResultsFile_JButton.setText ( __RemoveWorkingDirectory );
				__pathTestResultsFile_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathTestResultsFile_JButton.setText ( __AddWorkingDirectory );
            	__pathTestResultsFile_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathTestResultsFile_JButton.setEnabled(false);
		}
    }
    if ( __pathEndCommandFile_JButton != null ) {
		if ( (EndCommandFile != null) && !EndCommandFile.isEmpty() ) {
			__pathEndCommandFile_JButton.setEnabled ( true );
			File f = new File ( EndCommandFile );
			if ( f.isAbsolute() ) {
				__pathEndCommandFile_JButton.setText ( __RemoveWorkingDirectory );
				__pathEndCommandFile_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathEndCommandFile_JButton.setText ( __AddWorkingDirectory );
            	__pathEndCommandFile_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathEndCommandFile_JButton.setEnabled(false);
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
		// Commit the changes.
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out.
			return;
		}
	}
	// Now close out.
	setVisible( false );
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event ) {
	response ( false );
}

public void windowActivated( WindowEvent evt ) {
}

public void windowClosed( WindowEvent evt ) {
}

public void windowDeactivated( WindowEvent evt ) {
}

public void windowDeiconified( WindowEvent evt ) {
}

public void windowIconified( WindowEvent evt ) {
}

public void windowOpened( WindowEvent evt ) {
}

}