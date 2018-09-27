package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.awt.Color;
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

import RTi.Util.GUI.DictionaryJDialog;
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
Editor for the SetExcelCell command.
*/
@SuppressWarnings("serial")
public class SetExcelCell_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea = null;
private JTabbedPane __main_JTabbedPane = null;
private JTextField __Value_JTextField = null;
private JTextField __PropertyName_JTextField = null;
private JTextField __OutputFile_JTextField = null;
private JTextField __Worksheet_JTextField = null;
private JTabbedPane __excelSpace_JTabbedPane = null;
private JTextField __ExcelAddress_JTextField = null;
private JTextField __ExcelNamedRange_JTextField = null;
private JTextField __ExcelTableName_JTextField = null;
private SimpleJComboBox __ExcelColumnNames_JComboBox = null;
private JTextArea __ColumnExcludeFilters_JTextArea = null;
private JTextArea __ColumnNamedRanges_JTextArea = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextField __ExcludeColumns_JTextField = null;
private JTextField __Rows_JTextField = null;
private JTextField __Author_JTextField = null;
private JTextField __Comment_JTextField = null;
private JTextField __CommentWidth_JTextField = null;
private JTextField __CommentHeight_JTextField = null;
private JTextArea __ColumnWidths_JTextArea = null;
private JTextArea __ColumnCellTypes_JTextArea = null;
private JTextArea __ColumnDecimalPlaces_JTextArea = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private SetExcelCell_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public SetExcelCell_JDialog ( JFrame parent, SetExcelCell_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
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
		fc.setDialogTitle("Select Excel File");
     	fc.addChoosableFileFilter(new SimpleFileFilter("xls", "Excel File"));
		SimpleFileFilter sff = new SimpleFileFilter("xlsx", "Excel File");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			// Convert path to relative path by default.
			try {
				__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"SetExcelCell_JDialog", "Error converting file to relative path." );
			}
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "SetExcelCell");
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
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnCellTypes") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim();
        String [] notes = {
            "Each output column's cell type is by default set based on the table column type.",
            "Override by specifying the cell types below.",
            "Column Name - column name in the table or Default to set default cell type for all columns",
            "Cell type - " + __command._Auto + " to automatically set the cell type, or Text for text cell type"
        };
        String columnCellTypes = (new DictionaryJDialog ( __parent, true, ColumnCellTypes, "Edit ColumnCellTypes Parameter",
            notes, "Column Name", "Cell Type",10)).response();
        if ( columnCellTypes != null ) {
            __ColumnCellTypes_JTextArea.setText ( columnCellTypes );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnDecimalPlaces") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim();
        String [] notes = {
            "Number columns for floating-point values can have the number of decimal places specified.",
            "The default is the number for the table column, or if not known, 6.",
            "Column Name - column name in the table",
            "Decimal Places - specify the number of decimal places to show for floating point number columns"
        };
        String columnCellTypes = (new DictionaryJDialog ( __parent, true, ColumnDecimalPlaces, "Edit ColumnDecimalPlaces Parameter",
            notes, "Column Name", "Decimal Places",10)).response();
        if ( columnCellTypes != null ) {
            __ColumnDecimalPlaces_JTextArea.setText ( columnCellTypes );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnExcludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
        String [] notes = {
            "Exclude rows from the output by specifying a pattern to match for a column.",
            "Only string columns can be specified."
        };
        String dict = (new DictionaryJDialog ( __parent, true, ColumnExcludeFilters,
            "Edit ColumnExcludeFilters Parameter", notes, "Table Column", "Pattern to exclude rows (* allowed)",10)).response();
        if ( dict != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnNamedRanges") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim();
        String [] notes = {
            "A column's data cell range (ignoring the column heading) can be set to a named range.",
            "This is useful for using the column data with Excel data validation.",
            "Column Name - column name in the table",
            "Named Range - the name of the range"
        };
        String columnNamedRanges = (new DictionaryJDialog ( __parent, true, ColumnNamedRanges, "Edit ColumnNamedRanges Parameter",
            notes, "Column Name", "Named Range",10)).response();
        if ( columnNamedRanges != null ) {
            __ColumnNamedRanges_JTextArea.setText ( columnNamedRanges );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnWidths") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnWidths = __ColumnWidths_JTextArea.getText().trim();
        String [] notes = {
            "A column's width can be set to make the Excel output more readable.",
            "Column Name - one of the following:",
            "  column name in the table",
            "  Default, to set default width for all columns (can be reset by specific column information)",
            "  EmptyColumns, to set the width for empty columns (can be reset by specific column information)",
            "Width - " + __command._Auto + " to automatically size to data, or 1/256 of character width (max=256*256)"
        };
        String columnWidths = (new DictionaryJDialog ( __parent, true, ColumnWidths, "Edit ColumnWidths Parameter",
            notes, "Column Name", "Width",10)).response();
        if ( columnWidths != null ) {
            __ColumnWidths_JTextArea.setText ( columnWidths );
            refresh();
        }
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String Value = __Value_JTextField.getText().trim();
	String PropertyName = __PropertyName_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
	String Rows = __Rows_JTextField.getText().trim();
	String Author = __Author_JTextField.getText().trim();
	String Comment = __Comment_JTextField.getText().trim();
	String CommentWidth = __CommentWidth_JTextField.getText().trim();
	String CommentHeight = __CommentHeight_JTextField.getText().trim();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	String ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	String ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
	String ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
	String ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
	__error_wait = false;

    if ( Value.length() > 0 ) {
        props.set ( "Value", Value );
    }
    if ( PropertyName.length() > 0 ) {
        props.set ( "PropertyName", PropertyName );
    }
	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
    if ( Worksheet.length() > 0 ) {
        props.set ( "Worksheet", Worksheet );
    }
	if ( ExcelAddress.length() > 0 ) {
		props.set ( "ExcelAddress", ExcelAddress );
	}
	if ( ExcelNamedRange.length() > 0 ) {
		props.set ( "ExcelNamedRange", ExcelNamedRange );
	}
    if ( ExcelTableName.length() > 0 ) {
        props.set ( "ExcelTableName", ExcelTableName );
    }
    if ( ExcelColumnNames.length() > 0 ) {
        props.set ( "ExcelColumnNames", ExcelColumnNames );
    }
    if ( ColumnExcludeFilters.length() > 0 ) {
        props.set ( "ColumnExcludeFilters", ColumnExcludeFilters );
    }
    if ( ColumnNamedRanges.length() > 0 ) {
        props.set ( "ColumnNamedRanges", ColumnNamedRanges );
    }
    if ( KeepOpen.length() > 0 ) {
        props.set ( "KeepOpen", KeepOpen );
    }
    if ( IncludeColumns.length() > 0 ) {
        props.set ( "IncludeColumns", IncludeColumns );
    }
    if ( ExcludeColumns.length() > 0 ) {
        props.set ( "ExcludeColumns", ExcludeColumns );
    }
    if ( Rows.length() > 0 ) {
        props.set ( "Rows", Rows );
    }
    if ( Author.length() > 0 ) {
        props.set ( "Author", Author );
    }
    if ( Comment.length() > 0 ) {
        props.set ( "Comment", Comment );
    }
    if ( CommentWidth.length() > 0 ) {
        props.set ( "CommentWidth", CommentWidth );
    }
    if ( CommentHeight.length() > 0 ) {
        props.set ( "CommentHeight", CommentHeight );
    }
    if ( ColumnCellTypes.length() > 0 ) {
        props.set ( "ColumnCellTypes", ColumnCellTypes );
    }
    if ( ColumnWidths.length() > 0 ) {
        props.set ( "ColumnWidths", ColumnWidths );
    }
    if ( ColumnDecimalPlaces.length() > 0 ) {
        props.set ( "ColumnDecimalPlaces", ColumnDecimalPlaces );
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
{	String Value = __Value_JTextField.getText().trim();
    String PropertyName = __PropertyName_JTextField.getText().trim();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	//String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	//String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	String ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	String KeepOpen  = __KeepOpen_JComboBox.getSelected();
	String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
	String Rows = __Rows_JTextField.getText().trim();
	String Author = __Author_JTextField.getText().trim();
	String Comment = __Comment_JTextField.getText().trim();
	String CommentWidth = __CommentWidth_JTextField.getText().trim();
	String CommentHeight = __CommentHeight_JTextField.getText().trim();
    String ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
    String ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
    String ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
    __command.setCommandParameter ( "Value", Value );
    __command.setCommandParameter ( "PropertyName", PropertyName );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "ExcelAddress", ExcelAddress );
	__command.setCommandParameter ( "ExcelNamedRange", ExcelNamedRange );
	__command.setCommandParameter ( "ExcelTableName", ExcelTableName );
	__command.setCommandParameter ( "ExcelColumnNames", ExcelColumnNames );
	__command.setCommandParameter ( "ColumnExcludeFilters", ColumnExcludeFilters );
	//__command.setCommandParameter ( "ExcelIntegerColumns", ExcelIntegerColumns );
	//__command.setCommandParameter ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
	__command.setCommandParameter ( "ColumnNamedRanges", ColumnNamedRanges );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
    __command.setCommandParameter ( "IncludeColumns", IncludeColumns );
    __command.setCommandParameter ( "ExcludeColumns", ExcludeColumns );
    __command.setCommandParameter ( "Rows", Rows );
	__command.setCommandParameter ( "Author", Author );
	__command.setCommandParameter ( "Comment", Comment );
	__command.setCommandParameter ( "CommentWidth", CommentWidth );
	__command.setCommandParameter ( "CommentHeight", CommentHeight );
	__command.setCommandParameter ( "ColumnCellTypes", ColumnCellTypes );
	__command.setCommandParameter ( "ColumnWidths", ColumnWidths );
	__command.setCommandParameter ( "ColumnDecimalPlaces", ColumnDecimalPlaces );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, SetExcelCell_Command command, List<String> tableIDChoices )
{	__command = command;
    __parent = parent;
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
    	"This command sets a single the value, comments, and formatting for one or more cells in a Microsoft Excel workbook file (*.xls, *.xlsx).  " ),
    	0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    /*
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Column filters to exclude rows:"),
        0, ++yValue, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        */
    __ColumnExcludeFilters_JTextArea = new JTextArea (3,35);
    /*
    __ColumnExcludeFilters_JTextArea.setLineWrap ( true );
    __ColumnExcludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnExcludeFilters_JTextArea.setToolTipText("TableColumn:DatastoreColumn,TableColumn:DataStoreColumn");
    __ColumnExcludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(value_JPanel, new JScrollPane(__ColumnExcludeFilters_JTextArea),
        1, yValue, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Optional - column patterns to exclude rows (default=include all)."),
        3, yValue, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(value_JPanel, new SimpleJButton ("Edit","EditColumnExcludeFilters",this),
        3, ++yValue, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        */

    // Panel for Excel output parameters
    int yExcelFile = -1;
    JPanel excelFile_JPanel = new JPanel();
    excelFile_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel File", excelFile_JPanel );
    
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel (
        "The Excel file being modified must exist and be open - use other commands to create/open if necessary."),
        0, ++yExcelFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel (
        "It is recommended that the location of the Excel file be " +
        "specified using a path relative to the working directory."),
        0, ++yExcelFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    if (__working_dir != null) {
        JGUIUtil.addComponent(excelFile_JPanel, new JLabel (
        "The working directory is: " + __working_dir), 
        0, ++yExcelFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel ("Output (workbook) file:"),
		0, ++yExcelFile, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
	__OutputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(excelFile_JPanel, __OutputFile_JTextField,
		1, yExcelFile, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("...", this);
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(excelFile_JPanel, __browse_JButton,
		6, yExcelFile, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromFile,this);
	    JGUIUtil.addComponent(excelFile_JPanel, __path_JButton,
	    	7, yExcelFile, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
        
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcelFile, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelFile_JPanel, __Worksheet_JTextField,
        1, yExcelFile, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFile_JPanel,
        new JLabel ("Optional - worksheet name (default=first sheet)."),
        3, yExcelFile, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel address parameters
    int yExcelAddress = -1;
    JPanel excelAddress_JPanel = new JPanel();
    excelAddress_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Address", excelAddress_JPanel );

    JGUIUtil.addComponent(excelAddress_JPanel, new JLabel (
        "<html><b>Currently these parameters are not enabled.  Use the Excel Column/Row tab to specify cells to modify.</b></html>"),
        0, ++yExcelAddress, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelAddress_JPanel, new JLabel (
        "The cells to be modified can be specified using one of the standard Excel address methods below."),
        0, ++yExcelAddress, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelAddress_JPanel, new JLabel (
        "Alternately, use the Excel Column/Row tab."),
        0, ++yExcelAddress, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    __excelSpace_JTabbedPane = new JTabbedPane ();
    __excelSpace_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify the address for a contiguous block of cells the in Excel worksheet (upper left is start)" ));
    JGUIUtil.addComponent(excelAddress_JPanel, __excelSpace_JTabbedPane,
        0, ++yExcelAddress, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JPanel address_JPanel = new JPanel();
    address_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Excel Address", address_JPanel );
    int yAddress = -1;
        
    JGUIUtil.addComponent(address_JPanel, new JLabel ("Excel address:"),
        0, ++yAddress, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelAddress_JTextField = new JTextField (10);
    __ExcelAddress_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(address_JPanel, __ExcelAddress_JTextField,
        1, yAddress, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(address_JPanel, new JLabel ("Excel cell block address in format A1 or A1:B2."),
        3, yAddress, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JPanel range_JPanel = new JPanel();
    range_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Named Range", range_JPanel );
    int yRange = -1;
    
    JGUIUtil.addComponent(range_JPanel, new JLabel ("Excel named range:"),
        0, ++yRange, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelNamedRange_JTextField = new JTextField (10);
    __ExcelNamedRange_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(range_JPanel, __ExcelNamedRange_JTextField,
        1, yRange, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(range_JPanel, new JLabel ("Excel named range."),
        3, yRange, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JPanel tableAddress_JPanel = new JPanel();
    tableAddress_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Excel Table Name", tableAddress_JPanel );
    int yTableAddress = -1;
    
    JGUIUtil.addComponent(tableAddress_JPanel, new JLabel ("Excel table name:"),
        0, ++yTableAddress, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelTableName_JTextField = new JTextField (10);
    __ExcelTableName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(tableAddress_JPanel, __ExcelTableName_JTextField,
        1, yTableAddress, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tableAddress_JPanel, new JLabel ("Excel table name."),
        3, yTableAddress, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(tableAddress_JPanel, new JLabel( "Excel column names:"),
        0, ++yTableAddress, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelColumnNames_JComboBox = new SimpleJComboBox ( false );
    __ExcelColumnNames_JComboBox.add("");
    //__ExcelColumnNames_JComboBox.add(__command._ColumnN);
    __ExcelColumnNames_JComboBox.add(__command._FirstRowInRange);
    __ExcelColumnNames_JComboBox.add(__command._None);
    __ExcelColumnNames_JComboBox.add(__command._RowBeforeRange);
    __ExcelColumnNames_JComboBox.select ( 0 );
    __ExcelColumnNames_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(tableAddress_JPanel, __ExcelColumnNames_JComboBox,
        1, yTableAddress, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tableAddress_JPanel, new JLabel ( "Optional - how to define Excel column names (default=" +
        __command._None + ")."),
        3, yTableAddress, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    /*
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel ("Column named ranges:"),
        0, ++yExcelFile, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        */
    __ColumnNamedRanges_JTextArea = new JTextArea (3,35);
    /*
    __ColumnNamedRanges_JTextArea.setLineWrap ( true );
    __ColumnNamedRanges_JTextArea.setWrapStyleWord ( true );
    __ColumnNamedRanges_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnNamedRanges_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFile_JPanel, new JScrollPane(__ColumnNamedRanges_JTextArea),
        1, yExcelFile, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel ("Optional - indicate columns to set as named ranges (default=none)."),
        3, yExcelFile, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFile_JPanel, new SimpleJButton ("Edit","EditColumnNamedRanges",this),
        3, ++yExcelFile, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        */
    
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel( "Keep file open?:"),
        0, ++yExcelFile, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __KeepOpen_JComboBox = new SimpleJComboBox ( false );
    __KeepOpen_JComboBox.add("");
    __KeepOpen_JComboBox.add(__command._False);
    __KeepOpen_JComboBox.add(__command._True);
    __KeepOpen_JComboBox.select ( 0 );
    __KeepOpen_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excelFile_JPanel, __KeepOpen_JComboBox,
        1, yExcelFile, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFile_JPanel, new JLabel ( "Optional - keep Excel file open? (default=" + __command._False + ")."),
        3, yExcelFile, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for specifying output location using columns parameters
    int yExcelColumn = -1;
    JPanel excelColumn_JPanel = new JPanel();
    excelColumn_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Column/Row", excelColumn_JPanel );
    
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel (
        "The cells to be modified can be specified by matching existing column/row values in the worksheet."),
        0, ++yExcelColumn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel (
        "In the future, the Excel Address tab will be enabled."),
        0, ++yExcelColumn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Columns to include:"), 
        0, ++yExcelColumn, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField (10);
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelColumn_JPanel, __IncludeColumns_JTextField,
        1, yExcelColumn, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Optional - names of Excel columns to set (default=set all)."),
        3, yExcelColumn, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Columns to exclude:"), 
        0, ++yExcelColumn, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns_JTextField = new JTextField (10);
    __ExcludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelColumn_JPanel, __ExcludeColumns_JTextField,
        1, yExcelColumn, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Optional - names of Excel columns to NOT set (default=set all)."),
        3, yExcelColumn, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Rows:"), 
        0, ++yExcelColumn, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Rows_JTextField = new JTextField (10);
    __Rows_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelColumn_JPanel, __Rows_JTextField,
        1, yExcelColumn, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelColumn_JPanel, new JLabel ("Optional - row numbers (1+) to set (default=set all)."),
        3, yExcelColumn, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for value parameters
    int yValue = -1;
    JPanel value_JPanel = new JPanel();
    value_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Value to Set", value_JPanel );
    
    JGUIUtil.addComponent(value_JPanel, new JLabel (
        "<html><b>Currently these parameters are not enabled - only comments can be set in the Excel Comment tab</b></html>."),
        0, ++yValue, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel (
        "The value to set in cells can be specified directly or from a processor property."),
        0, ++yValue, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel (
        "Optionally, the command also will set a cell's comment (see Excel Comment tab) and format a cell (see Excel Formatting tab)."),
        0, ++yValue, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel (
        "Currently only text (string) data values can be processed - other data types will be added in the future."),
        0, ++yValue, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Value to set in cell:"), 
        0, ++yValue, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Value_JTextField = new JTextField (10);
    __Value_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(value_JPanel, __Value_JTextField,
        1, yValue, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Optional - value to set in cell."),
        3, yValue, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Property to set in cell:"), 
        0, ++yValue, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PropertyName_JTextField = new JTextField (10);
    __PropertyName_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(value_JPanel, __PropertyName_JTextField,
        1, yValue, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(value_JPanel, new JLabel ("Optional - name of property to set in cell."),
        3, yValue, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
 
    // Panel for Excel comments 
    int yExcelComment = -1;
    JPanel excelComment_JPanel = new JPanel();
    excelComment_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Comment", excelComment_JPanel );
    
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel (
		"Comments can be set for column headings and data cells."),
		0, ++yExcelComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel (
		"Cells to have comments set are identified by the Excel Addres tab or Excel Column/Row tab."),
		0, ++yExcelComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel (
		"Warning:  Using many comments can significantly increase the size of the Excel file."),
		0, ++yExcelComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel (
		"Format the comment using the following specifiers:"),
		0, ++yExcelComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel (
		"   ${property} for processor property"),
		0, ++yExcelComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel("Author:"),
        0, ++yExcelComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Author_JTextField = new JTextField(10);
    __Author_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelComment_JPanel, __Author_JTextField,
        1, yExcelComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel ("Optional - author for comments (default=none)."),
        3, yExcelComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel("Comment:"),
        0, ++yExcelComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Comment_JTextField = new JTextField(30);
    __Comment_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelComment_JPanel, __Comment_JTextField,
        1, yExcelComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel ("Optional - comment text for cell (default=none)."),
        3, yExcelComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel("Comment width (columns):"),
        0, ++yExcelComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CommentWidth_JTextField = new JTextField(10);
    __CommentWidth_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelComment_JPanel, __CommentWidth_JTextField,
        1, yExcelComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel ("Optional - comment width in columns (default=6)."),
        3, yExcelComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel("Comment height (rows):"),
        0, ++yExcelComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CommentHeight_JTextField = new JTextField(10);
    __CommentHeight_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(excelComment_JPanel, __CommentHeight_JTextField,
        1, yExcelComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelComment_JPanel, new JLabel ("Optional - number of rows for comment (default=lines of comment)."),
        3, yExcelComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    // Panel for Excel formatting 
    int yExcelFormat = -1;
    JPanel excelFormat_JPanel = new JPanel();
    excelFormat_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Formatting", excelFormat_JPanel );
    
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel (
        "Features to format the specified cell(s) will be added in the future."),
        0, ++yExcelFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    /*
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Column cell types:"),
        0, ++yExcelFormat, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        */
    __ColumnCellTypes_JTextArea = new JTextArea (3,35);
    /*
    __ColumnCellTypes_JTextArea.setLineWrap ( true );
    __ColumnCellTypes_JTextArea.setWrapStyleWord ( true );
    __ColumnCellTypes_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnCellTypes_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFormat_JPanel, new JScrollPane(__ColumnCellTypes_JTextArea),
        1, yExcelFormat, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Optional - indicate columns to set cell types (default=" +
        __command._Auto + ")."),
        3, yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFormat_JPanel, new SimpleJButton ("Edit","EditColumnCellTypes",this),
        3, ++yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Column widths:"),
        0, ++yExcelFormat, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        */
    __ColumnWidths_JTextArea = new JTextArea (3,35);
    /*
    __ColumnWidths_JTextArea.setLineWrap ( true );
    __ColumnWidths_JTextArea.setWrapStyleWord ( true );
    __ColumnWidths_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnWidths_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFormat_JPanel, new JScrollPane(__ColumnWidths_JTextArea),
        1, yExcelFormat, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Optional - indicate column widths (default=constant width)."),
        3, yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFormat_JPanel, new SimpleJButton ("Edit","EditColumnWidths",this),
        3, ++yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Column decimal places:"),
        0, ++yExcelFormat, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        */
    __ColumnDecimalPlaces_JTextArea = new JTextArea (3,35);
    /*
    __ColumnDecimalPlaces_JTextArea.setLineWrap ( true );
    __ColumnDecimalPlaces_JTextArea.setWrapStyleWord ( true );
    __ColumnDecimalPlaces_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnDecimalPlaces_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFormat_JPanel, new JScrollPane(__ColumnDecimalPlaces_JTextArea),
        1, yExcelFormat, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Optional - indicate number column decimal places (default=from table)."),
        3, yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFormat_JPanel, new SimpleJButton ("Edit","EditColumnDecimalPlaces",this),
        3, ++yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        */
    
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
{	String routine = getClass().getSimpleName() + ".refresh";
    String Value = "";
    String PropertyName = "";
    String OutputFile = "";
    String Worksheet = "";
	String ExcelAddress = "";
	String ExcelNamedRange = "";
	String ExcelTableName = "";
	String ExcelColumnNames = "";
	String ColumnExcludeFilters = "";
	String ExcelIntegerColumns = "";
	String ExcelDateTimeColumns = "";
	String NumberPrecision = "";
	String WriteAllAsText = "";
	String ColumnNamedRanges = "";
	String KeepOpen = "";
    String IncludeColumns = "";
    String ExcludeColumns = "";
    String Rows = "";
	String Author = "";
	String Comment = "";
	String CommentWidth = "";
	String CommentHeight = "";
	String ColumnCellTypes = "";
	String ColumnWidths = "";
	String ColumnDecimalPlaces = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        Value = props.getValue ( "Value" );
        PropertyName = props.getValue ( "PropertyName" );
		OutputFile = props.getValue ( "OutputFile" );
		Worksheet = props.getValue ( "Worksheet" );
		ExcelAddress = props.getValue ( "ExcelAddress" );
		ExcelNamedRange = props.getValue ( "ExcelNamedRange" );
		ExcelTableName = props.getValue ( "ExcelTableName" );
		ExcelColumnNames = props.getValue ( "ExcelColumnNames" );
		ColumnExcludeFilters = props.getValue ( "ColumnExcludeFilters" );
		ExcelIntegerColumns = props.getValue ( "ExcelIntegerColumns" );
		ExcelDateTimeColumns = props.getValue ( "ExcelDateTimeColumns" );
		NumberPrecision = props.getValue ( "NumberPrecision" );
		WriteAllAsText = props.getValue ( "WriteAllAsText" );
		ColumnNamedRanges = props.getValue ( "ColumnNamedRanges" );
		KeepOpen = props.getValue ( "KeepOpen" );
		IncludeColumns = props.getValue ( "IncludeColumns" );
	    ExcludeColumns = props.getValue ( "ExcludeColumns" );
	    Rows = props.getValue ( "Rows" );
		Author = props.getValue ( "Author" );
		Comment = props.getValue ( "Comment" );
		CommentWidth = props.getValue ( "CommentWidth" );
		CommentHeight = props.getValue ( "CommentHeight" );
		ColumnCellTypes = props.getValue ( "ColumnCellTypes" );
		ColumnWidths = props.getValue ( "ColumnWidths" );
		ColumnDecimalPlaces = props.getValue ( "ColumnDecimalPlaces" );
        if ( Value != null ) {
            __Value_JTextField.setText ( Value );
        }
        if ( PropertyName != null ) {
            __PropertyName_JTextField.setText ( PropertyName );
        }
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
        if ( Worksheet != null ) {
            __Worksheet_JTextField.setText ( Worksheet );
        }
		if ( (ExcelAddress != null) && !ExcelAddress.equals("") ) {
			__ExcelAddress_JTextField.setText ( ExcelAddress );
			// Also select the tab to be visible
			__excelSpace_JTabbedPane.setSelectedIndex(0);
		}
		if ( (ExcelNamedRange != null) && !ExcelNamedRange.equals("") ) {
			__ExcelNamedRange_JTextField.setText ( ExcelNamedRange );
			__excelSpace_JTabbedPane.setSelectedIndex(1);
		}
        if ( (ExcelTableName != null) && !ExcelTableName.equals("") ) {
            __ExcelTableName_JTextField.setText ( ExcelTableName );
            __excelSpace_JTabbedPane.setSelectedIndex(2);
        }
        if ( ExcelColumnNames == null || ExcelColumnNames.equals("") ) {
            // Select a default...
            __ExcelColumnNames_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ExcelColumnNames_JComboBox, ExcelColumnNames, JGUIUtil.NONE, null, null ) ) {
                __ExcelColumnNames_JComboBox.select ( ExcelColumnNames );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nExcelColumnNames \"" +
                    ExcelColumnNames + "\".  Select a different choice or Cancel." );
            }
        }
        if ( ColumnExcludeFilters != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( ColumnExcludeFilters );
        }
        if ( ColumnNamedRanges != null ) {
            __ColumnNamedRanges_JTextArea.setText ( ColumnNamedRanges );
        }
        if ( KeepOpen == null || KeepOpen.equals("") ) {
            // Select a default...
            __KeepOpen_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __KeepOpen_JComboBox, KeepOpen, JGUIUtil.NONE, null, null ) ) {
                __KeepOpen_JComboBox.select ( KeepOpen );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nKeepOpen \"" +
                    KeepOpen + "\".  Select a different choice or Cancel." );
            }
        }
        if ( IncludeColumns != null ) {
            __IncludeColumns_JTextField.setText ( IncludeColumns );
        }
        if ( ExcludeColumns != null ) {
            __ExcludeColumns_JTextField.setText ( ExcludeColumns );
        }
        if ( Rows != null ) {
            __Rows_JTextField.setText ( Rows );
        }
        if ( Author != null ) {
            __Author_JTextField.setText ( Author );
        }
        if ( Comment != null ) {
            __Comment_JTextField.setText ( Comment );
        }
        if ( CommentWidth != null ) {
            __CommentWidth_JTextField.setText ( CommentWidth );
        }
        if ( CommentHeight != null ) {
            __CommentHeight_JTextField.setText ( CommentHeight );
        }
        if ( ColumnCellTypes != null ) {
            __ColumnCellTypes_JTextArea.setText ( ColumnCellTypes );
        }
        if ( ColumnWidths != null ) {
            __ColumnWidths_JTextArea.setText ( ColumnWidths );
        }
        if ( ColumnDecimalPlaces != null ) {
            __ColumnDecimalPlaces_JTextArea.setText ( ColumnDecimalPlaces );
        }
	}
	// Regardless, reset the command from the fields...
	Value = __Value_JTextField.getText().trim();
	PropertyName = __PropertyName_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Worksheet = __Worksheet_JTextField.getText().trim();
	ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	ExcelColumnNames = __ExcelColumnNames_JComboBox.getSelected();
	ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
	ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
	Rows = __Rows_JTextField.getText().trim();
	Author = __Author_JTextField.getText().trim();
	Comment = __Comment_JTextField.getText().trim();
	CommentWidth = __CommentWidth_JTextField.getText().trim();
	CommentHeight = __CommentHeight_JTextField.getText().trim();
	ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
	ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
	ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
	props = new PropList ( __command.getCommandName() );
    props.add ( "Value=" + Value );
    props.add ( "PropertyName=" + PropertyName );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "ExcelAddress=" + ExcelAddress );
	props.add ( "ExcelNamedRange=" + ExcelNamedRange );
	props.add ( "ExcelTableName=" + ExcelTableName );
	props.add ( "ExcelColumnNames=" + ExcelColumnNames );
	props.add ( "ColumnExcludeFilters=" + ColumnExcludeFilters );
	props.add ( "ExcelIntegerColumns=" + ExcelIntegerColumns );
	props.add ( "ExcelDateTimeColumns=" + ExcelDateTimeColumns );
	props.add ( "NumberPrecision=" + NumberPrecision );
	props.add ( "WriteAllAsText=" + WriteAllAsText );
	props.add ( "ColumnNamedRanges=" + ColumnNamedRanges );
	props.add ( "KeepOpen=" + KeepOpen );
    props.add ( "IncludeColumns=" + IncludeColumns );
    props.add ( "ExcludeColumns=" + ExcludeColumns );
    props.add ( "Rows=" + Rows );
	props.add ( "Author=" + Author );
	props.add ( "Comment=" + Comment );
	props.add ( "CommentWidth=" + CommentWidth );
	props.add ( "CommentHeight=" + CommentHeight );
	props.add ( "ColumnCellTypes=" + ColumnCellTypes );
	props.add ( "ColumnWidths=" + ColumnWidths );
	props.add ( "ColumnDecimalPlaces=" + ColumnDecimalPlaces );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (OutputFile);
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
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}