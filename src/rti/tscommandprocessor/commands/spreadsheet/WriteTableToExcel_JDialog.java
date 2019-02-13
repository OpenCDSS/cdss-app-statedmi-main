// WriteTableToExcel_JDialog - Editor for the WriteTableToExcel command.

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
Editor for the WriteTableToExcel command.
*/
@SuppressWarnings("serial")
public class WriteTableToExcel_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea = null;
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextField __ExcludeColumns_JTextField = null;
private JTextField __OutputFile_JTextField = null;
private JTextField __Worksheet_JTextField = null;
private JTabbedPane __excelSpace_JTabbedPane = null;
private JTextField __ExcelAddress_JTextField = null;
private JTextField __ExcelNamedRange_JTextField = null;
private JTextField __ExcelTableName_JTextField = null;
private SimpleJComboBox __ExcelColumnNames_JComboBox = null;
private JTextArea __ColumnIncludeFilters_JTextArea = null;
private JTextArea __ColumnExcludeFilters_JTextArea = null;
private JTextArea __ColumnNamedRanges_JTextArea = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private JTextArea __ColumnWidths_JTextArea = null;
private JTextArea __ColumnCellTypes_JTextArea = null;
private JTextArea __ColumnDecimalPlaces_JTextArea = null;
private SimpleJComboBox __ConditionTableID_JComboBox = null;
private SimpleJComboBox __StyleTableID_JComboBox = null;
private JTextField __LegendWorksheet_JTextField = null;
private JTextField __LegendAddress_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private WriteTableToExcel_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTableToExcel_JDialog ( JFrame parent, WriteTableToExcel_Command command, List<String> tableIDChoices )
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
				Message.printWarning ( 1,"WriteTableToExcel_JDialog", "Error converting file to relative path." );
			}
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WriteTableToExcel");
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
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnIncludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
        String [] notes = {
            "Include rows from the output by specifying a pattern to match for a column.",
            "Only string columns can be specified."
        };
        String dict = (new DictionaryJDialog ( __parent, true, ColumnIncludeFilters,
            "Edit ColumnIncludeFilters Parameter", notes, "Table Column", "Pattern to include rows (* allowed)",10)).response();
        if ( dict != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( dict );
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
	String TableID = __TableID_JComboBox.getSelected();
	String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	String ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	String ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
	String ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
	String ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
	String ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	String StyleTableID = __StyleTableID_JComboBox.getSelected();
	String LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	String LegendAddress = __LegendAddress_JTextField.getText().trim();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( IncludeColumns.length() > 0 ) {
        props.set ( "IncludeColumns", IncludeColumns );
    }
    if ( ExcludeColumns.length() > 0 ) {
        props.set ( "ExcludeColumns", ExcludeColumns );
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
    if ( ColumnIncludeFilters.length() > 0 ) {
        props.set ( "ColumnIncludeFilters", ColumnIncludeFilters );
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
    if ( ColumnCellTypes.length() > 0 ) {
        props.set ( "ColumnCellTypes", ColumnCellTypes );
    }
    if ( ColumnWidths.length() > 0 ) {
        props.set ( "ColumnWidths", ColumnWidths );
    }
    if ( ColumnDecimalPlaces.length() > 0 ) {
        props.set ( "ColumnDecimalPlaces", ColumnDecimalPlaces );
    }
    if ( ConditionTableID.length() > 0 ) {
        props.set ( "ConditionTableID", ConditionTableID );
    }
    if ( StyleTableID.length() > 0 ) {
        props.set ( "StyleTableID", StyleTableID );
    }
    if ( LegendWorksheet.length() > 0 ) {
        props.set ( "LegendWorksheet", LegendWorksheet );
    }
    if ( LegendAddress.length() > 0 ) {
        props.set ( "LegendAddress", LegendAddress );
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
{	String TableID = __TableID_JComboBox.getSelected();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	//String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	//String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	String ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	String KeepOpen  = __KeepOpen_JComboBox.getSelected();
    String ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
    String ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
    String ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
	String ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	String StyleTableID = __StyleTableID_JComboBox.getSelected();
	String LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	String LegendAddress = __LegendAddress_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "IncludeColumns", IncludeColumns );
    __command.setCommandParameter ( "ExcludeColumns", ExcludeColumns );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "ExcelAddress", ExcelAddress );
	__command.setCommandParameter ( "ExcelNamedRange", ExcelNamedRange );
	__command.setCommandParameter ( "ExcelTableName", ExcelTableName );
	__command.setCommandParameter ( "ExcelColumnNames", ExcelColumnNames );
	__command.setCommandParameter ( "ColumnIncludeFilters", ColumnIncludeFilters );
	__command.setCommandParameter ( "ColumnExcludeFilters", ColumnExcludeFilters );
	//__command.setCommandParameter ( "ExcelIntegerColumns", ExcelIntegerColumns );
	//__command.setCommandParameter ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
	__command.setCommandParameter ( "ColumnNamedRanges", ColumnNamedRanges );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
	__command.setCommandParameter ( "ColumnCellTypes", ColumnCellTypes );
	__command.setCommandParameter ( "ColumnWidths", ColumnWidths );
	__command.setCommandParameter ( "ColumnDecimalPlaces", ColumnDecimalPlaces );
	__command.setCommandParameter ( "ConditionTableID", ConditionTableID );
	__command.setCommandParameter ( "StyleTableID", StyleTableID );
	__command.setCommandParameter ( "LegendWorksheet", LegendWorksheet );
	__command.setCommandParameter ( "LegendAddress", LegendAddress );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, WriteTableToExcel_Command command, List<String> tableIDChoices )
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
    	"This command writes a table to a worksheet in a Microsoft Excel workbook file (*.xls, *.xlsx).  " ),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
    	"Basic cell formatting can also be configured." ),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	    0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for table parameters
    int yTable = -1;
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Table to Write", table_JPanel );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "Table column data types will be set in Excel by default (see Excel Formatting tab for more options):"),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "   String column -> text cell"),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "   Integer column -> number cell with no decimal places"),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "   Floating point number column -> number cell with decimal places (see Excel Formatting tab for default)"),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(table_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
    	0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Table ID:" ), 
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __TableID_JComboBox.setToolTipText("Select the table to write or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to give user a blank entry to type over
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    __TableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(table_JPanel, __TableID_JComboBox,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel( "Required - table to write."), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Columns to include:"), 
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField (10);
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(table_JPanel, __IncludeColumns_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - names of columns to write (default=write all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Columns to exclude:"), 
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns_JTextField = new JTextField (10);
    __ExcludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(table_JPanel, __ExcludeColumns_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - names of columns to NOT write (default=write all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
 
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Column filters to include rows:"),
        0, ++yTable, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnIncludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnIncludeFilters_JTextArea.setLineWrap ( true );
    __ColumnIncludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnIncludeFilters_JTextArea.setToolTipText("TableColumn1:Pattern1,TableColumn2:Pattern2");
    __ColumnIncludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, new JScrollPane(__ColumnIncludeFilters_JTextArea),
        1, yTable, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - column patterns to include rows (default=include all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(table_JPanel, new SimpleJButton ("Edit","EditColumnIncludeFilters",this),
        3, ++yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Column filters to exclude rows:"),
        0, ++yTable, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnExcludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnExcludeFilters_JTextArea.setLineWrap ( true );
    __ColumnExcludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnExcludeFilters_JTextArea.setToolTipText("TableColumn1:Pattern1,TableColumn2:Pattern2");
    __ColumnExcludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, new JScrollPane(__ColumnExcludeFilters_JTextArea),
        1, yTable, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - column patterns to exclude rows (default=include all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(table_JPanel, new SimpleJButton ("Edit","EditColumnExcludeFilters",this),
        3, ++yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    // Panel for Excel output parameters
    int yExcelOutput = -1;
    JPanel excelOutput_JPanel = new JPanel();
    excelOutput_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Output", excelOutput_JPanel );
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "Currently the Excel file must exist - use the NewExcelWorkbook command if necessary to create the workbook file."),
        0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "It is recommended that the location of the Excel file be " +
        "specified using a path relative to the working directory."),
        0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    if (__working_dir != null) {
        JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "The working directory is: " + __working_dir), 
        0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "A contiguous block of Excel cells must be specified using one of the address methods below."),
        0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "Column names from the table will be written to Excel if ExcelColumnNames is specified as other than None."),
        0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(excelOutput_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
    	0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Output (workbook) file:"),
		0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
	__OutputFile_JTextField.setToolTipText("Specify the path to the Excel file or use ${Property} notation");
	__OutputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(excelOutput_JPanel, __OutputFile_JTextField,
		1, yExcelOutput, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("...", this);
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(excelOutput_JPanel, __browse_JButton,
		6, yExcelOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromFile,this);
	    JGUIUtil.addComponent(excelOutput_JPanel, __path_JButton,
	    	7, yExcelOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
        
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.setToolTipText("Specify the name of the worksheet or use ${Property} notation");
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, __Worksheet_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel,
        new JLabel ("Required (if not in address) - worksheet name (default=first sheet)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    __excelSpace_JTabbedPane = new JTabbedPane ();
    __excelSpace_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify the address for a contiguous block of cells the in Excel worksheet (upper left is start)" ));
    JGUIUtil.addComponent(excelOutput_JPanel, __excelSpace_JTabbedPane,
        0, ++yExcelOutput, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
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
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel( "Excel column names:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelColumnNames_JComboBox = new SimpleJComboBox ( false );
    __ExcelColumnNames_JComboBox.add("");
    //__ExcelColumnNames_JComboBox.add(__command._ColumnN);
    __ExcelColumnNames_JComboBox.add(__command._FirstRowInRange);
    __ExcelColumnNames_JComboBox.add(__command._None);
    __ExcelColumnNames_JComboBox.add(__command._RowBeforeRange);
    __ExcelColumnNames_JComboBox.select ( 0 );
    __ExcelColumnNames_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __ExcelColumnNames_JComboBox,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Optional - how to define Excel column names (default=" +
        __command._None + ")."),
        3, yExcelOutput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Column named ranges:"),
        0, ++yExcelOutput, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnNamedRanges_JTextArea = new JTextArea (3,35);
    __ColumnNamedRanges_JTextArea.setLineWrap ( true );
    __ColumnNamedRanges_JTextArea.setWrapStyleWord ( true );
    __ColumnNamedRanges_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnNamedRanges_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, new JScrollPane(__ColumnNamedRanges_JTextArea),
        1, yExcelOutput, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Optional - indicate columns to set as named ranges (default=none)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelOutput_JPanel, new SimpleJButton ("Edit","EditColumnNamedRanges",this),
        3, ++yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel( "Keep file open?:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __KeepOpen_JComboBox = new SimpleJComboBox ( false );
    __KeepOpen_JComboBox.add("");
    __KeepOpen_JComboBox.add(__command._False);
    __KeepOpen_JComboBox.add(__command._True);
    __KeepOpen_JComboBox.select ( 0 );
    __KeepOpen_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __KeepOpen_JComboBox,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Optional - keep Excel file open? (default=" + __command._False + ")."),
        3, yExcelOutput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for Excel formatting 
    int yExcelFormat = -1;
    JPanel excelFormat_JPanel = new JPanel();
    excelFormat_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Formatting", excelFormat_JPanel );

    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel (
        "The following parameters control how Excel cells are formatted.  See also the \"Style Formatting\" tab."),
        0, ++yExcelFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(excelFormat_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	    0, ++yExcelFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Column cell types:"),
        0, ++yExcelFormat, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnCellTypes_JTextArea = new JTextArea (3,35);
    __ColumnCellTypes_JTextArea.setLineWrap ( true );
    __ColumnCellTypes_JTextArea.setWrapStyleWord ( true );
    __ColumnCellTypes_JTextArea.setToolTipText("ColumnName1:CellType1,ColumnName2:CellType2");
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
    __ColumnWidths_JTextArea = new JTextArea (3,35);
    __ColumnWidths_JTextArea.setLineWrap ( true );
    __ColumnWidths_JTextArea.setWrapStyleWord ( true );
    __ColumnWidths_JTextArea.setToolTipText("ColumnName1:ColumnWidth1,ColumnName2:ColumnWidth2");
    __ColumnWidths_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFormat_JPanel, new JScrollPane(__ColumnWidths_JTextArea),
        1, yExcelFormat, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Optional - indicate column widths (default=constant width)."),
        3, yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFormat_JPanel, new SimpleJButton ("Edit","EditColumnWidths",this),
        3, ++yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Column decimal places:"),
        0, ++yExcelFormat, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnDecimalPlaces_JTextArea = new JTextArea (3,35);
    __ColumnDecimalPlaces_JTextArea.setLineWrap ( true );
    __ColumnDecimalPlaces_JTextArea.setWrapStyleWord ( true );
    __ColumnDecimalPlaces_JTextArea.setToolTipText("ColumnName1:DecimalPlaces1,ColumnName2:DecimalPlaces2");
    __ColumnDecimalPlaces_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelFormat_JPanel, new JScrollPane(__ColumnDecimalPlaces_JTextArea),
        1, yExcelFormat, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelFormat_JPanel, new JLabel ("Optional - indicate number column decimal places (default=from table)."),
        3, yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelFormat_JPanel, new SimpleJButton ("Edit","EditColumnDecimalPlaces",this),
        3, ++yExcelFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for style formatting 
    int yStyle = -1;
    JPanel style_JPanel = new JPanel();
    style_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Style Formatting", style_JPanel );

    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "The following parameters control how Excel cells are formatted, using a general style formatting approach."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "Style-based formatting requires as input a condition table to indicate how to evaluate cell contents for style formatting."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "A style table indicates the style properties to format a cell, such as the fill foreground color."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "Refer to the command documentation for details."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(style_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	    0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(style_JPanel, new JLabel ( "Condition table ID:" ), 
        0, ++yStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ConditionTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __ConditionTableID_JComboBox.setToolTipText("Select the condition table or use ${Property} notation");
    __ConditionTableID_JComboBox.setData ( tableIDChoices );
    __ConditionTableID_JComboBox.addItemListener ( this );
    __ConditionTableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(style_JPanel, __ConditionTableID_JComboBox,
        1, yStyle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel( "Required when using styles - conditions to determine styles."), 
        3, yStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(style_JPanel, new JLabel ( "Style table ID:" ), 
        0, ++yStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __StyleTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __StyleTableID_JComboBox.setToolTipText("Select the style table or use ${Property} notation");
    __StyleTableID_JComboBox.setData ( tableIDChoices );
    __StyleTableID_JComboBox.addItemListener ( this );
    __StyleTableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(style_JPanel, __StyleTableID_JComboBox,
        1, yStyle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel( "Required when using styles - style definitions."), 
        3, yStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(style_JPanel, new JLabel ( "Legend worksheet:"), 
        0, ++yStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LegendWorksheet_JTextField = new JTextField (40);
    __LegendWorksheet_JTextField.setToolTipText("Name of worksheet for legend or use ${Property}.");
    __LegendWorksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(style_JPanel, __LegendWorksheet_JTextField,
        1, yStyle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "Optional - worksheet for legend (default=same as for table)."),
        3, yStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(style_JPanel, new JLabel ( "Legend address:"), 
        0, ++yStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LegendAddress_JTextField = new JTextField (40);
    __LegendAddress_JTextField.setToolTipText("Address of upper-left cell for legend.  Use R[${Property}+1]C[${Property}+1] to position relative to data block.");
    __LegendAddress_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(style_JPanel, __LegendAddress_JTextField,
        1, yStyle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "Optional - upper-left address for legend (default=no legend)."),
        3, yStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
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
{	String routine = getClass().getSimpleName() + ".refresh";
    String TableID = "";
    String IncludeColumns = "";
    String ExcludeColumns = "";
    String OutputFile = "";
    String Worksheet = "";
	String ExcelAddress = "";
	String ExcelNamedRange = "";
	String ExcelTableName = "";
	String ExcelColumnNames = "";
	String ColumnIncludeFilters = "";
	String ColumnExcludeFilters = "";
	String ExcelIntegerColumns = "";
	String ExcelDateTimeColumns = "";
	String NumberPrecision = "";
	String WriteAllAsText = "";
	String ColumnNamedRanges = "";
	String KeepOpen = "";
	String ColumnCellTypes = "";
	String ColumnWidths = "";
	String ColumnDecimalPlaces = "";
	String ConditionTableID = "";
	String StyleTableID = "";
	String LegendWorksheet = "";
	String LegendAddress = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        IncludeColumns = props.getValue ( "IncludeColumns" );
        ExcludeColumns = props.getValue ( "ExcludeColumns" );
		OutputFile = props.getValue ( "OutputFile" );
		Worksheet = props.getValue ( "Worksheet" );
		ExcelAddress = props.getValue ( "ExcelAddress" );
		ExcelNamedRange = props.getValue ( "ExcelNamedRange" );
		ExcelTableName = props.getValue ( "ExcelTableName" );
		ExcelColumnNames = props.getValue ( "ExcelColumnNames" );
		ColumnIncludeFilters = props.getValue ( "ColumnIncludeFilters" );
		ColumnExcludeFilters = props.getValue ( "ColumnExcludeFilters" );
		ExcelIntegerColumns = props.getValue ( "ExcelIntegerColumns" );
		ExcelDateTimeColumns = props.getValue ( "ExcelDateTimeColumns" );
		NumberPrecision = props.getValue ( "NumberPrecision" );
		WriteAllAsText = props.getValue ( "WriteAllAsText" );
		ColumnNamedRanges = props.getValue ( "ColumnNamedRanges" );
		KeepOpen = props.getValue ( "KeepOpen" );
		ColumnCellTypes = props.getValue ( "ColumnCellTypes" );
		ColumnWidths = props.getValue ( "ColumnWidths" );
		ColumnDecimalPlaces = props.getValue ( "ColumnDecimalPlaces" );
		ConditionTableID = props.getValue ( "ConditionTableID" );
		StyleTableID = props.getValue ( "StyleTableID" );
		LegendWorksheet = props.getValue ( "LegendWorksheet" );
		LegendAddress = props.getValue ( "LegendAddress" );
        if ( TableID == null ) {
            // Select default...
            __TableID_JComboBox.select ( 0 );
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
        if ( IncludeColumns != null ) {
            __IncludeColumns_JTextField.setText ( IncludeColumns );
        }
        if ( ExcludeColumns != null ) {
            __ExcludeColumns_JTextField.setText ( ExcludeColumns );
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
        if ( ColumnIncludeFilters != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( ColumnIncludeFilters );
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
        if ( ColumnCellTypes != null ) {
            __ColumnCellTypes_JTextArea.setText ( ColumnCellTypes );
        }
        if ( ColumnWidths != null ) {
            __ColumnWidths_JTextArea.setText ( ColumnWidths );
        }
        if ( ColumnDecimalPlaces != null ) {
            __ColumnDecimalPlaces_JTextArea.setText ( ColumnDecimalPlaces );
        }
        if ( ConditionTableID == null ) {
            // Select default...
        	__ConditionTableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ConditionTableID_JComboBox,ConditionTableID, JGUIUtil.NONE, null, null ) ) {
                __ConditionTableID_JComboBox.select ( ConditionTableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nConditionTableID value \"" + ConditionTableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( StyleTableID == null ) {
            // Select default...
        	__StyleTableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __StyleTableID_JComboBox,StyleTableID, JGUIUtil.NONE, null, null ) ) {
                __StyleTableID_JComboBox.select ( StyleTableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nStyleTableID value \"" + StyleTableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
		if ( LegendWorksheet != null ) {
			__LegendWorksheet_JTextField.setText ( LegendWorksheet );
		}
		if ( LegendAddress != null ) {
			__LegendAddress_JTextField.setText ( LegendAddress );
		}
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Worksheet = __Worksheet_JTextField.getText().trim();
	ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	ExcelColumnNames = __ExcelColumnNames_JComboBox.getSelected();
	ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
	ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
	ColumnNamedRanges = __ColumnNamedRanges_JTextArea.getText().trim().replace("\n"," ");
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	ColumnCellTypes = __ColumnCellTypes_JTextArea.getText().trim().replace("\n"," ");
	ColumnWidths = __ColumnWidths_JTextArea.getText().trim().replace("\n"," ");
	ColumnDecimalPlaces = __ColumnDecimalPlaces_JTextArea.getText().trim().replace("\n"," ");
	ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	StyleTableID = __StyleTableID_JComboBox.getSelected();
	LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	LegendAddress = __LegendAddress_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "IncludeColumns=" + IncludeColumns );
    props.add ( "ExcludeColumns=" + ExcludeColumns );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "ExcelAddress=" + ExcelAddress );
	props.add ( "ExcelNamedRange=" + ExcelNamedRange );
	props.add ( "ExcelTableName=" + ExcelTableName );
	props.add ( "ExcelColumnNames=" + ExcelColumnNames );
	props.add ( "ColumnIncludeFilters=" + ColumnIncludeFilters );
	props.add ( "ColumnExcludeFilters=" + ColumnExcludeFilters );
	props.add ( "ExcelIntegerColumns=" + ExcelIntegerColumns );
	props.add ( "ExcelDateTimeColumns=" + ExcelDateTimeColumns );
	props.add ( "NumberPrecision=" + NumberPrecision );
	props.add ( "WriteAllAsText=" + WriteAllAsText );
	props.add ( "ColumnNamedRanges=" + ColumnNamedRanges );
	props.add ( "KeepOpen=" + KeepOpen );
	props.add ( "ColumnCellTypes=" + ColumnCellTypes );
	props.add ( "ColumnWidths=" + ColumnWidths );
	props.add ( "ColumnDecimalPlaces=" + ColumnDecimalPlaces );
	props.add ( "ConditionTableID=" + ConditionTableID );
	props.add ( "StyleTableID=" + StyleTableID );
	props.add ( "LegendWorksheet=" + LegendWorksheet );
	props.add ( "LegendAddress=" + LegendAddress );
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
