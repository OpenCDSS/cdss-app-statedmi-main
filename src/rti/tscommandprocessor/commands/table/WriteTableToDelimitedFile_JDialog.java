// WriteTableToDelimitedFile_JDialog - editor for WriteTableToDelimitedFile command

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

package rti.tscommandprocessor.commands.table;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
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
public class WriteTableToDelimitedFile_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __browse_schema_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null;
private SimpleJButton __path_schema_JButton = null;
private WriteTableToDelimitedFile_Command __command = null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __WriteHeaderComments_JComboBox = null;
private SimpleJComboBox __AlwaysQuoteStrings_JComboBox = null;
private JTextField __NewlineReplacement_JTextField = null;
private JTextField __NaNValue_JTextField = null;
private JTextField __OutputSchemaFile_JTextField = null;
private SimpleJComboBox __OutputSchemaFormat_JComboBox = null;
private String __working_dir = null; // Working directory.
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether the user has pressed OK.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit
*/
public WriteTableToDelimitedFile_JDialog ( JFrame parent, WriteTableToDelimitedFile_Command command,
    List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
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
		    fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setDialogTitle("Select Delimited File to Write");
	    SimpleFileFilter sff_csv = new SimpleFileFilter("csv", "Comma Separated Value File");
	    fc.addChoosableFileFilter(sff_csv);
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
	
			if (path != null) {
				if ( fc.getFileFilter() == sff_csv ) {
					// Enforce extension...
					path = IOUtil.enforceFileExtension(path, "csv");
				}
				// Convert path to relative path by default.
				try {
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"WriteTableToDelimitedFile_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory( directory);
				refresh();
			}
		}
	}
	else if ( o == __browse_schema_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		fc.setDialogTitle("Select Schema File to Write");
	    SimpleFileFilter sff_csv = new SimpleFileFilter("json", "Table Schema File (JSON)");
	    fc.addChoosableFileFilter(sff_csv);
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
	
			if (path != null) {
				if ( fc.getFileFilter() == sff_csv ) {
					// Enforce extension...
					path = IOUtil.enforceFileExtension(path, "json");
				}

				// Convert path to relative path by default.
				try {
					__OutputSchemaFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"WriteTableToDelimitedFile_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory( directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WriteTableToDelimitedFile");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __OutputFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir, __OutputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,
				"WriteTableToDelimitedFile_JDialog", "Error converting file to relative path." );
			}
		}
		refresh ();
	}
	else if ( o == __path_schema_JButton ) {
		if ( __path_schema_JButton.getText().equals(__AddWorkingDirectory) ) {
			__OutputSchemaFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __OutputSchemaFile_JTextField.getText() ) );
		}
		else if ( __path_schema_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
				__OutputSchemaFile_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir, __OutputSchemaFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,
				"WriteTableToDelimitedFile_JDialog", "Error converting schema file to relative path." );
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
	PropList parameters = new PropList ( "" );
	String OutputFile = __OutputFile_JTextField.getText().trim();
    String TableID = __TableID_JComboBox.getSelected();
    String WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
    String AlwaysQuoteStrings = __AlwaysQuoteStrings_JComboBox.getSelected();
    String NewlineReplacement = __NewlineReplacement_JTextField.getText().trim();
    String NaNValue = __NaNValue_JTextField.getText().trim();
    String OutputSchemaFile = __OutputSchemaFile_JTextField.getText().trim();
    String OutputSchemaFormat = __OutputSchemaFormat_JComboBox.getSelected();

	__error_wait = false;
	
	if ( OutputFile.length() > 0 ) {
		parameters.set ( "OutputFile", OutputFile );
	}
    if ( (TableID != null) && TableID.length() > 0 ) {
        parameters.set ( "TableID", TableID );
    }
    if ( (WriteHeaderComments != null) && (WriteHeaderComments.length() > 0) ) {
        parameters.set ( "WriteHeaderComments", WriteHeaderComments );
    }
    if ( (AlwaysQuoteStrings != null) && (AlwaysQuoteStrings.length() > 0) ) {
        parameters.set ( "AlwaysQuoteStrings", AlwaysQuoteStrings );
    }
    if ( (NewlineReplacement != null) && (NewlineReplacement.length() > 0) ) {
        parameters.set ( "NewlineReplacement", NewlineReplacement );
    }
    if ( (NaNValue != null) && (NaNValue.length() > 0) ) {
        parameters.set ( "NaNValue", NaNValue );
    }
    if ( (OutputSchemaFile != null) && (OutputSchemaFile.length() > 0) ) {
        parameters.set ( "OutputSchemaFile", OutputSchemaFile );
    }
    if ( (OutputSchemaFormat != null) && (OutputSchemaFormat.length() > 0) ) {
        parameters.set ( "OutputSchemaFormat", OutputSchemaFormat );
    }
	try {
	    // This will warn the user...
		__command.checkCommandParameters ( parameters, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		Message.printWarning(2,"",e);
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{   String TableID = __TableID_JComboBox.getSelected();   
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
	String AlwaysQuoteStrings = __AlwaysQuoteStrings_JComboBox.getSelected();
	String NewlineReplacement = __NewlineReplacement_JTextField.getText().trim();
	String NaNValue = __NaNValue_JTextField.getText().trim();
    String OutputSchemaFile = __OutputSchemaFile_JTextField.getText().trim();
    String OutputSchemaFormat = __OutputSchemaFormat_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "WriteHeaderComments", WriteHeaderComments );
	__command.setCommandParameter ( "AlwaysQuoteStrings", AlwaysQuoteStrings );
	__command.setCommandParameter ( "NewlineReplacement", NewlineReplacement );
	__command.setCommandParameter ( "NaNValue", NaNValue );
	__command.setCommandParameter ( "OutputSchemaFile", OutputSchemaFile );
	__command.setCommandParameter ( "OutputSchemaFormat", OutputSchemaFormat );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, WriteTableToDelimitedFile_Command command, List<String> tableIDChoices )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Write a table to a delimited format file, which can be specified using a full or " +
		"relative path (relative to the working directory)."),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The delimiter is a comma, header comment lines start with #, " +
        "and column headings are the first non-comment line."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Double quote characters in data are replaced with \"\" (two double quotes).  Quotes inserted at start and end of line are single."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "A schema file can also be written to provide metadata about the delimited file columns, which can be used by other software."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	 if ( __working_dir != null ) {
     	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The working directory is: " + __working_dir ), 
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	 }
  	JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output file to write:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	 __OutputFile_JTextField = new JTextField ( 50 );
	 __OutputFile_JTextField.setToolTipText("Specify the path to the output file or use ${Property} notation");
	 __OutputFile_JTextField.addKeyListener ( this );
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
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table to write:" ), 
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __TableID_JComboBox = new SimpleJComboBox ( false ); // Don't allow edits
     __TableID_JComboBox.setToolTipText("Specify the table ID for statistic output or use ${Property} notation");
     __TableID_JComboBox.setData(tableIDChoices);
     __TableID_JComboBox.addItemListener ( this );
     JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
         1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - table identifier."),
     3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Write header comments?:"), 
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __WriteHeaderComments_JComboBox = new SimpleJComboBox ( false );
     List<String> writeHeaderCommentsList = new Vector<String>();
     writeHeaderCommentsList.add("");
     writeHeaderCommentsList.add(__command._False);
     writeHeaderCommentsList.add(__command._True);
     __WriteHeaderComments_JComboBox.setData ( writeHeaderCommentsList );
     __WriteHeaderComments_JComboBox.select(0);
     __WriteHeaderComments_JComboBox.addItemListener (this);
     JGUIUtil.addComponent(main_JPanel, __WriteHeaderComments_JComboBox,
         1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
         "Optional - should header comments be written? (default=" + __command._True + ")."),
         3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Always quote strings?:"), 
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __AlwaysQuoteStrings_JComboBox = new SimpleJComboBox ( false );
     List<String> alwaysQuoteStringsList = new Vector<String>();
     alwaysQuoteStringsList.add("");
     alwaysQuoteStringsList.add(__command._False);
     alwaysQuoteStringsList.add(__command._True);
     __AlwaysQuoteStrings_JComboBox.setData ( alwaysQuoteStringsList );
     __AlwaysQuoteStrings_JComboBox.select(0);
     __AlwaysQuoteStrings_JComboBox.addItemListener (this);
     JGUIUtil.addComponent(main_JPanel, __AlwaysQuoteStrings_JComboBox,
         1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
         "Optional - always quote strings? (default=" + __command._False + ", only quote if delimiter in string)."),
         3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Newline replacement:"),
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __NewlineReplacement_JTextField = new JTextField (10);
     __NewlineReplacement_JTextField.addKeyListener (this);
     JGUIUtil.addComponent(main_JPanel, __NewlineReplacement_JTextField,
         1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
         "Optional - replacement for newline character (use \\t for tab or \\s for space)."),
         3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ("NaN value:"),
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __NaNValue_JTextField = new JTextField (10);
     __NaNValue_JTextField.setToolTipText("Specify Blank to write a blank in the output");
     __NaNValue_JTextField.addKeyListener (this);
     JGUIUtil.addComponent(main_JPanel, __NaNValue_JTextField,
         1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
         "Optional - value to use for NaN (use " + __command._Blank + " to write a blank)."),
         3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output schema file to write:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	 __OutputSchemaFile_JTextField = new JTextField ( 50 );
	 __OutputSchemaFile_JTextField.setToolTipText("Specify the path to the output schema file or use ${Property} notation");
	 __OutputSchemaFile_JTextField.addKeyListener ( this );
	 // Output file layout fights back with other rows so put in its own panel
	 JPanel OutputSchemaFile_JPanel = new JPanel();
	 OutputSchemaFile_JPanel.setLayout(new GridBagLayout());
	 JGUIUtil.addComponent(OutputSchemaFile_JPanel, __OutputSchemaFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
    __browse_schema_JButton = new SimpleJButton ( "...", this );
	__browse_schema_JButton.setToolTipText("Browse for file");
	JGUIUtil.addComponent(OutputSchemaFile_JPanel, __browse_schema_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_schema_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(OutputSchemaFile_JPanel, __path_schema_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
	JGUIUtil.addComponent(main_JPanel, OutputSchemaFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Output schema format:"), 
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __OutputSchemaFormat_JComboBox = new SimpleJComboBox ( false );
     List<String> schemaFormatChoices = new ArrayList<String>();
     schemaFormatChoices.add("");
     schemaFormatChoices.add(__command._GoogleBigQuery);
     schemaFormatChoices.add(__command._JSONTableSchema);
     __OutputSchemaFormat_JComboBox.setData ( schemaFormatChoices );
     __OutputSchemaFormat_JComboBox.select(0);
     __OutputSchemaFormat_JComboBox.addItemListener (this);
     JGUIUtil.addComponent(main_JPanel, __OutputSchemaFormat_JComboBox,
         1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
         "Optional - schema format (default=" + __command._JSONTableSchema + ")."),
         3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
     
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __command_JTextArea = new JTextArea ( 4, 50 );
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

	__ok_JButton = new SimpleJButton("OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add ( __ok_JButton );
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");
	
    refresh();

	setTitle ( "Edit " + __command.getCommandName() + " Command" );
    pack();
    JGUIUtil.center( this );
	setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e)
{
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	// Only refresh if the event is in the file TextField...
	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

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
{	String routine = getClass().getSimpleName() + "_JDialog.refresh";
	String OutputFile = "";
    String TableID = "";
    String WriteHeaderComments = "";
    String AlwaysQuoteStrings = "";
    String NewlineReplacement = "";
    String NaNValue = "";
    String OutputSchemaFile = "";
    String OutputSchemaFormat = "";
	__error_wait = false;
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		OutputFile = parameters.getValue ( "OutputFile" );
        TableID = parameters.getValue ( "TableID" );
        WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
        AlwaysQuoteStrings = parameters.getValue ( "AlwaysQuoteStrings" );
        NewlineReplacement = parameters.getValue ( "NewlineReplacement" );
        NaNValue = parameters.getValue ( "NaNValue" );
        OutputSchemaFile = parameters.getValue ( "OutputSchemaFile" );
        OutputSchemaFormat = parameters.getValue ( "OutputSchemaFormat" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText (OutputFile);
		}
        if ( TableID == null ) {
            // Select default...
            if ( __TableID_JComboBox.getItemCount() > 0 ) {
                __TableID_JComboBox.select ( 0 );
            }
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableID_JComboBox,TableID, JGUIUtil.NONE, null, null ) ) {
                __TableID_JComboBox.select ( TableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTableID value \"" + TableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__WriteHeaderComments_JComboBox, WriteHeaderComments, JGUIUtil.NONE, null, null ) ) {
            __WriteHeaderComments_JComboBox.select ( WriteHeaderComments );
        }
        else {
            if ( (WriteHeaderComments == null) || WriteHeaderComments.equals("") ) {
                // New command...select the default...
                if ( __WriteHeaderComments_JComboBox.getItemCount() > 0 ) {
                    __WriteHeaderComments_JComboBox.select ( 0 );
                }
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid "+
                  "WriteHeaderComments parameter \"" + WriteHeaderComments + "\".  Select a different value or Cancel." );
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__AlwaysQuoteStrings_JComboBox, AlwaysQuoteStrings, JGUIUtil.NONE, null, null ) ) {
            __AlwaysQuoteStrings_JComboBox.select ( AlwaysQuoteStrings );
        }
        else {
            if ( (AlwaysQuoteStrings == null) || AlwaysQuoteStrings.equals("") ) {
                // New command...select the default...
                if ( __AlwaysQuoteStrings_JComboBox.getItemCount() > 0 ) {
                    __AlwaysQuoteStrings_JComboBox.select ( 0 );
                }
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid "+
                  "AlwaysQuoteStrings parameter \"" + AlwaysQuoteStrings + "\".  Select a different value or Cancel." );
            }
        }
        if (NewlineReplacement != null) {
            __NewlineReplacement_JTextField.setText(NewlineReplacement);
        }
        if (NaNValue != null) {
            __NaNValue_JTextField.setText(NaNValue);
        }
		if ( OutputSchemaFile != null ) {
			__OutputSchemaFile_JTextField.setText (OutputSchemaFile);
		}
	}
	// Regardless, reset the command from the fields...
	OutputFile = __OutputFile_JTextField.getText().trim();
    TableID = __TableID_JComboBox.getSelected();
    WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
    AlwaysQuoteStrings = __AlwaysQuoteStrings_JComboBox.getSelected();
    NewlineReplacement = __NewlineReplacement_JTextField.getText().trim();
    NaNValue = __NaNValue_JTextField.getText().trim();
    OutputSchemaFile = __OutputSchemaFile_JTextField.getText().trim();
    OutputSchemaFormat = __OutputSchemaFormat_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "OutputFile=" + OutputFile );
	if ( TableID != null ) {
	    parameters.add ( "TableID=" + TableID );
	}
    if ( WriteHeaderComments != null ) {
        parameters.add ( "WriteHeaderComments=" + WriteHeaderComments );
    }
    if ( AlwaysQuoteStrings != null ) {
        parameters.add ( "AlwaysQuoteStrings=" + AlwaysQuoteStrings );
    }
    parameters.add("NewlineReplacement=" + NewlineReplacement );
    parameters.add("NaNValue=" + NaNValue );
    parameters.add ( "OutputSchemaFile=" + OutputSchemaFile );
    if ( OutputSchemaFormat != null ) {
        parameters.add ( "OutputSchemaFormat=" + OutputSchemaFormat );
    }
	__command_JTextArea.setText( __command.toString ( parameters ) );
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
	if ( __path_schema_JButton != null ) {
		if ( (OutputSchemaFile != null) && !OutputSchemaFile.isEmpty() ) {
			__path_schema_JButton.setEnabled ( true );
			File f = new File ( OutputSchemaFile );
			if ( f.isAbsolute() ) {
				__path_schema_JButton.setText ( __RemoveWorkingDirectory );
				__path_schema_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__path_schema_JButton.setText ( __AddWorkingDirectory );
            	__path_schema_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path_schema_JButton.setEnabled(false);
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
