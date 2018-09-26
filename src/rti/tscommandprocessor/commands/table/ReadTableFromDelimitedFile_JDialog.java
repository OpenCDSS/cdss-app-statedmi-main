package rti.tscommandprocessor.commands.table;

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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class ReadTableFromDelimitedFile_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rel";

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __TableID_JTextField = null;
private JTextField __InputFile_JTextField = null;
private JTextField __Delimiter_JTextField = null;
private JTextField __SkipLines_JTextField = null;
// FIXME SAM 2008-01-27 Enable later
//private JTextField __SkipColumns_JTextField = null;
private JTextField __HeaderLines_JTextField = null;
private JTextField __DateTimeColumns_JTextField = null;
private JTextField __DoubleColumns_JTextField = null;
private JTextField __IntegerColumns_JTextField = null;
private JTextField __TextColumns_JTextField = null;
private JTextField __Top_JTextField = null;
private JTextField __RowCountProperty_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadTableFromDelimitedFile_Command __command = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadTableFromDelimitedFile_JDialog ( JFrame parent, ReadTableFromDelimitedFile_Command command )
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
            fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select Delimited Table File");
        SimpleFileFilter sff = new SimpleFileFilter("csv", "Table File");
		fc.addChoosableFileFilter(sff);
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", "Table File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			// Convert path to relative path by default.
			try {
				__InputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"ReadTableFromDelimitedFile_JDialog", "Error converting file to relative path." );
			}
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadTableFromDelimitedFile");
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
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectoryFromFile)) {
			try {
                __InputFile_JTextField.setText ( IOUtil.toRelativePath (__working_dir,
                        __InputFile_JTextField.getText()));
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
    String TableID = __TableID_JTextField.getText().trim();
	String InputFile = __InputFile_JTextField.getText().trim();
	String Delimiter = __Delimiter_JTextField.getText().trim();
	String SkipLines = __SkipLines_JTextField.getText().trim();
	//String SkipColumns = __SkipColumns_JTextField.getText().trim();
	String HeaderLines = __HeaderLines_JTextField.getText().trim();
	String DateTimeColumns  = __DateTimeColumns_JTextField.getText().trim();
	String DoubleColumns  = __DoubleColumns_JTextField.getText().trim();
	String IntegerColumns  = __IntegerColumns_JTextField.getText().trim();
	String TextColumns  = __TextColumns_JTextField.getText().trim();
	String Top  = __Top_JTextField.getText().trim();
	String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
	if ( Delimiter.length() > 0 ) {
		props.set ( "Delimiter", Delimiter );
	}
	if ( SkipLines.length() > 0 ) {
		props.set ( "SkipLines", SkipLines );
	}
	//if ( SkipColumns.length() > 0 ) {
	//	props.set ( "Columns", SkipColumns );
	//}
	if ( HeaderLines.length() > 0 ) {
		props.set ( "HeaderLines", HeaderLines );
	}
    if ( DateTimeColumns.length() > 0 ) {
        props.set ( "DateTimeColumns", DateTimeColumns );
    }
    if ( DoubleColumns.length() > 0 ) {
        props.set ( "DoubleColumns", DoubleColumns );
    }
    if ( IntegerColumns.length() > 0 ) {
        props.set ( "IntegerColumns", IntegerColumns );
    }
    if ( TextColumns.length() > 0 ) {
        props.set ( "TextColumns", TextColumns );
    }
    if ( Top.length() > 0 ) {
        props.set ( "Top", Top );
    }
    if ( RowCountProperty.length() > 0 ) {
        props.set ( "RowCountProperty", RowCountProperty );
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
{	String TableID = __TableID_JTextField.getText().trim();
    String InputFile = __InputFile_JTextField.getText().trim();
    String Delimiter = __Delimiter_JTextField.getText().trim();
	String SkipLines = __SkipLines_JTextField.getText().trim();
	//String SkipColumns = __SkipColumns_JTextField.getText().trim();
	String HeaderLines = __HeaderLines_JTextField.getText().trim();
	String DateTimeColumns  = __DateTimeColumns_JTextField.getText().trim();
	String DoubleColumns  = __DoubleColumns_JTextField.getText().trim();
	String IntegerColumns  = __IntegerColumns_JTextField.getText().trim();
	String TextColumns  = __TextColumns_JTextField.getText().trim();
	String Top  = __Top_JTextField.getText().trim();
	String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter ( "Delimiter", Delimiter );
	__command.setCommandParameter ( "SkipLines", SkipLines );
	//__command.setCommandParameter ( "SkipColumns", SkipColumns );
	__command.setCommandParameter ( "HeaderLines", HeaderLines );
	__command.setCommandParameter ( "DateTimeColumns", DateTimeColumns );
	__command.setCommandParameter ( "DoubleColumns", DoubleColumns );
	__command.setCommandParameter ( "IntegerColumns", IntegerColumns );
	__command.setCommandParameter ( "TextColumns", TextColumns );
	__command.setCommandParameter ( "Top", Top );
	__command.setCommandParameter ( "RowCountProperty", RowCountProperty );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, ReadTableFromDelimitedFile_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads a table from a delimited file.  The table can then be used by other commands."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns in the file should be delimited by commas (default) or other character."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "An example data file is shown below (line and data row numbers are shown on the left for illustration):"),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "<html><pre>\n" +
        "   1     | # This is a comment\n" +
        "   2     | # This is another comment\n" +
        "   3     | # Double-quoted fields in the 1st non-comment line will be treated as headers (see also HeaderLines)\n" +
        "   4     | \"Header1\",\"Header2\",\"Header3\"\n" +
        "   5   1 | 1,1.0,1.5\n" +
        "   6   2 | 2,2.0,3.0\n" +
        "   7     | # Embedded comment will be skipped - the above data rows are 1-2 and the following data row is 3\n" +
        "   8   3 | 3,3.0,4.5</pre></html>"),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Lines in the file starting with # are treated as comments and are skipped during the read.  " +
        "Header lines and skipped lines are also not included as row data after the read."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Non-comment lines, once read, are called \"rows\" and are numbered 1+ for row-based processing."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the files be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);			
	if (__working_dir != null) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Table ID:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JTextField = new JTextField (20);
    __TableID_JTextField.setToolTipText("Specify the table ID or use ${Property} notation");
    __TableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __TableID_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - unique identifier for the table."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.setToolTipText("Specify the path to the file to read or use ${Property} notation");
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
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
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Delimiter:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Delimiter_JTextField = new JTextField (15);
    __Delimiter_JTextField.setToolTipText("Specify the delimiter character between columns or use ${Property} notation");
    __Delimiter_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Delimiter_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - delimiter, \\t=tab (default=comma ,)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("File lines to skip:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SkipLines_JTextField = new JTextField (10);
    __SkipLines_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SkipLines_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - comma-separated line numbers or ranges (e.g., 1,5-6)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        /*
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Columns to skip:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SkipColumns_JTextField = new JTextField (10);
	__SkipColumns_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __SkipColumns_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify as comma-separated numbers or ranges."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
		*/

    JGUIUtil.addComponent(main_JPanel, new JLabel ("File line containing column names:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__HeaderLines_JTextField = new JTextField (10);
	__HeaderLines_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __HeaderLines_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel,
   	    new JLabel ( "Optional - specify line number 1+ (default=first row if double quoted)." ),
		//"Specify as a range (e.g., \"5-7\")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Date/time columns:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimeColumns_JTextField = new JTextField (20);
    __DateTimeColumns_JTextField.setToolTipText("Specify column names containing date/time values, can use ${Property} notation");
    __DateTimeColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DateTimeColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ("Optional - columns that contain date/times, separated by commas."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Double precision (number) columns:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DoubleColumns_JTextField = new JTextField (20);
    __DoubleColumns_JTextField.setToolTipText("Specify column names containing floating point (double precision) values, can use ${Property} notation");
    __DoubleColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DoubleColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ("Optional - columns that contain numbers, separated by commas."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Integer columns:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IntegerColumns_JTextField = new JTextField (20);
    __IntegerColumns_JTextField.setToolTipText("Specify column names containing integer values, can use ${Property} notation");
    __IntegerColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __IntegerColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ("Optional - columns that contain integers, separated by commas."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Text columns:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TextColumns_JTextField = new JTextField (20);
    __TextColumns_JTextField.setToolTipText("Specify column names containing text values, can use ${Property} notation");
    __TextColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __TextColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ("Optional - columns that contain text, separated by commas."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Top N rows:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Top_JTextField = new JTextField (5);
    __Top_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Top_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel,
        new JLabel ("Optional - only process top N rows."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Row count property:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowCountProperty_JTextField = new JTextField ( "", 20 );
    __RowCountProperty_JTextField.setToolTipText("Specify the property name for the copied table row count, can use ${Property} notation");
    __RowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RowCountProperty_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - processor property to set as output table row count." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
{	String TableID = "";
    String InputFile = "";
    String Delimiter = "";
	String SkipLines = "";
	//String SkipColumns = "";
	String HeaderLines = "";
	String DateTimeColumns = "";
	String DoubleColumns = "";
	String IntegerColumns = "";
	String TextColumns = "";
	String Top = "";
    String RowCountProperty = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
		InputFile = props.getValue ( "InputFile" );
		Delimiter = props.getValue ( "Delimiter" );
		SkipLines = props.getValue ( "SkipLines" );
		//SkipColumns = props.getValue ( "SkipColumns" );
		HeaderLines = props.getValue ( "HeaderLines" );
		DateTimeColumns = props.getValue ( "DateTimeColumns" );
		DoubleColumns = props.getValue ( "DoubleColumns" );
		IntegerColumns = props.getValue ( "IntegerColumns" );
		TextColumns = props.getValue ( "TextColumns" );
		Top = props.getValue ( "Top" );
		RowCountProperty = props.getValue ( "RowCountProperty" );
        if ( TableID != null ) {
            __TableID_JTextField.setText ( TableID );
        }
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
		if ( Delimiter != null ) {
			__Delimiter_JTextField.setText ( Delimiter );
		}
		if ( SkipLines != null ) {
			__SkipLines_JTextField.setText ( SkipLines );
		}
		//if ( SkipColumns != null ) {
		//	__SkipColumns_JTextField.setText ( SkipColumns );
		//}
		if ( HeaderLines != null ) {
			__HeaderLines_JTextField.setText ( HeaderLines );
		}
        if ( DateTimeColumns != null ) {
            __DateTimeColumns_JTextField.setText ( DateTimeColumns );
        }
        if ( DoubleColumns != null ) {
            __DoubleColumns_JTextField.setText ( DoubleColumns );
        }
        if ( IntegerColumns != null ) {
            __IntegerColumns_JTextField.setText ( IntegerColumns );
        }
        if ( TextColumns != null ) {
            __TextColumns_JTextField.setText ( TextColumns );
        }
        if ( Top != null ) {
            __Top_JTextField.setText ( Top );
        }
        if ( RowCountProperty != null ) {
            __RowCountProperty_JTextField.setText ( RowCountProperty );
        }
	}
	// Regardless, reset the command from the fields...
    TableID = __TableID_JTextField.getText().trim();
	InputFile = __InputFile_JTextField.getText().trim();
	Delimiter = __Delimiter_JTextField.getText().trim();
	SkipLines = __SkipLines_JTextField.getText().trim();
	//SkipColumns = __SkipColumns_JTextField.getText().trim();
	HeaderLines = __HeaderLines_JTextField.getText().trim();
	DateTimeColumns = __DateTimeColumns_JTextField.getText().trim();
	DoubleColumns = __DoubleColumns_JTextField.getText().trim();
	IntegerColumns = __IntegerColumns_JTextField.getText().trim();
	TextColumns = __TextColumns_JTextField.getText().trim();
	Top = __Top_JTextField.getText().trim();
	RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "InputFile=" + InputFile );
	props.add ( "Delimiter=" + Delimiter );
	props.add ( "SkipLines=" + SkipLines );
	//props.add ( "SkipColumns=" + SkipColumns );
	props.add ( "HeaderLines=" + HeaderLines );
	props.add ( "DateTimeColumns=" + DateTimeColumns );
	props.add ( "DoubleColumns=" + DoubleColumns );
	props.add ( "IntegerColumns=" + IntegerColumns );
	props.add ( "TextColumns=" + TextColumns );
	props.add ( "Top=" + Top );
	props.add ( "RowCountProperty=" + RowCountProperty );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (InputFile);
		if (f.isAbsolute()) {
			__path_JButton.setText (__RemoveWorkingDirectoryFromFile);
			__path_JButton.setToolTipText("Change path to relative to command file");
		}
		else {
            __path_JButton.setText (__AddWorkingDirectoryToFile);
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
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}