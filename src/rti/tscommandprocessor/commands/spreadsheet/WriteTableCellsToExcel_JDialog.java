// WriteTableCellsToExcel_JDialog - Editor for the WriteTableCellsToExcel command.

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
import javax.swing.JTabbedPane;
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
import java.util.ArrayList;
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
Editor for the WriteTableCellsToExcel command.
*/
@SuppressWarnings("serial")
public class WriteTableCellsToExcel_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __OutputFile_JTextField = null;
private JTextField __Worksheet_JTextField = null;
private JTextArea __ColumnIncludeFilters_JTextArea = null;
private JTextField __ExcelIntegerColumns_JTextField = null;
private JTextField __ExcelDateTimeColumns_JTextField = null;
private JTextField __NumberPrecision_JTextField = null;
private SimpleJComboBox __WriteAllAsText_JComboBox = null;
private JTextArea __ColumnCellMap_JTextArea = null;
// TODO SAM 2014-02-05 Evaluate whether this is even needed - for now rely on spreadsheet formatting
//private SimpleJComboBox __CellFormat_JComboBox = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private WriteTableCellsToExcel_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTableCellsToExcel_JDialog ( JFrame parent, WriteTableCellsToExcel_Command command, List<String> tableIDChoices )
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
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory );
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WriteTableCellsToExcel");
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
		if (__path_JButton.getText().equals( __AddWorkingDirectory)) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectory)) {
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
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnIncludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
        String [] notes = { "Specify filters for columns in order to match a single row.",
            "Table Column - name of column to filter",
            "Pattern to match - specify string to match, with * for wildcard"};
        String dict = (new DictionaryJDialog ( __parent, true, ColumnIncludeFilters,
            "Edit ColumnIncludeFilters Parameter", notes, "Table Column", "Pattern to match",10)).response();
        if ( dict != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnCellMap") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnCellMap = __ColumnCellMap_JTextArea.getText().trim();
        String [] notes = {
            "Specify how table column names map to Excel cell addresses:",
            "Column Name - column name in the table",
            "Cell Address - the output Excel cell address, as named range or A1 notation"
        };
        String columnFilters = (new DictionaryJDialog ( __parent, true, ColumnCellMap, "Edit ColumnCellMap Parameter",
            notes, "Column Name", "Cell Address",10)).response();
        if ( columnFilters != null ) {
            __ColumnCellMap_JTextArea.setText ( columnFilters );
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
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	//String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	//String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	//String NumberPrecision  = __NumberPrecision_JTextField.getText().trim();
	//String WriteAllAsText = __WriteAllAsText_JComboBox.getSelected();
	String ColumnCellMap = __ColumnCellMap_JTextArea.getText().trim().replace("\n"," ");
	//String CellFormat = __CellFormat_JComboBox.getSelected();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
    if ( Worksheet.length() > 0 ) {
        props.set ( "Worksheet", Worksheet );
    }
    if ( ColumnIncludeFilters.length() > 0 ) {
        props.set ( "ColumnIncludeFilters", ColumnIncludeFilters );
    }
    /*
    if ( ExcelIntegerColumns.length() > 0 ) {
        props.set ( "ExcelIntegerColumns", ExcelIntegerColumns );
    }
    if ( ExcelDateTimeColumns.length() > 0 ) {
        props.set ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
    }
    if ( NumberPrecision.length() > 0 ) {
        props.set ( "NumberPrecision", NumberPrecision );
    }
    if ( WriteAllAsText.length() > 0 ) {
        props.set ( "WriteAllAsText", WriteAllAsText );
    }*/
    if ( ColumnCellMap.length() > 0 ) {
        props.set ( "ColumnCellMap", ColumnCellMap );
    }
    //if ( CellFormat.length() > 0 ) {
    //    props.set ( "CellFormat", CellFormat );
    //}
    if ( KeepOpen.length() > 0 ) {
        props.set ( "KeepOpen", KeepOpen );
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
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String Worksheet = __Worksheet_JTextField.getText().trim();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	//String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	//String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	//String NumberPrecision  = __NumberPrecision_JTextField.getText().trim();
	//String WriteAllAsText  = __WriteAllAsText_JComboBox.getSelected();
	String ColumnCellMap = __ColumnCellMap_JTextArea.getText().trim().replace("\n"," ");
	//String CellFormat  = __CellFormat_JComboBox.getSelected();
	String KeepOpen  = __KeepOpen_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "ColumnIncludeFilters", ColumnIncludeFilters );
	//__command.setCommandParameter ( "ExcelIntegerColumns", ExcelIntegerColumns );
	//__command.setCommandParameter ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
	//__command.setCommandParameter ( "NumberPrecision", NumberPrecision );
	//__command.setCommandParameter ( "WriteAllAsText", WriteAllAsText );
	__command.setCommandParameter ( "ColumnCellMap", ColumnCellMap );
	//__command.setCommandParameter ( "CellFormat", CellFormat );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, WriteTableCellsToExcel_Command command, List<String> tableIDChoices )
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
    	"This command writes cells from a table row to specific cell in a Microsoft Excel worksheet."),
    	0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The Excel file (workbook) and worksheet must already exist.  See the NewExcelWorkbook() command."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Currently only a single table row can be output by matching with the column filter.  " +
        "If necessary use a template that loops over rows."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The cell locations in Excel can be specified using a named range, or A1-style address."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the Excel file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);		
	if (__working_dir != null) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
     
    // Panel for table parameters
    int yTable = -1;
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Table", table_JPanel );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Table ID:" ), 
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    __TableID_JComboBox.addKeyListener ( this );
    __TableID_JComboBox.getJTextComponent().addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(table_JPanel, __TableID_JComboBox,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel( "Required - table to write."), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Column filters to include rows:"),
        0, ++yTable, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnIncludeFilters_JTextArea = new JTextArea (3,45);
    __ColumnIncludeFilters_JTextArea.setLineWrap ( true );
    __ColumnIncludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnIncludeFilters_JTextArea.setToolTipText("TableColumn:DatastoreColumn,TableColumn:DataStoreColumn");
    __ColumnIncludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, new JScrollPane(__ColumnIncludeFilters_JTextArea),
        1, yTable, 2, 2, 1, .2, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - column patterns to include rows (default=include all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(table_JPanel, new SimpleJButton ("Edit","EditColumnIncludeFilters",this),
        3, ++yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Table <-> Excel parameters
    int yTableExcel = -1;
    JPanel tableExcel_JPanel = new JPanel();
    tableExcel_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Table <-> Excel", tableExcel_JPanel );
    
    JGUIUtil.addComponent(tableExcel_JPanel, new JLabel ("Column to cell address map:"),
        0, ++yTableExcel, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnCellMap_JTextArea = new JTextArea (7,45);
    __ColumnCellMap_JTextArea.setLineWrap ( true );
    __ColumnCellMap_JTextArea.setWrapStyleWord ( true );
    __ColumnCellMap_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnCellMap_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(tableExcel_JPanel, new JScrollPane(__ColumnCellMap_JTextArea),
        1, yTableExcel, 2, 2, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tableExcel_JPanel, new JLabel ("Required - indicate column to cell addres mapping (default=none)."),
        3, yTableExcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(tableExcel_JPanel, new SimpleJButton ("Edit","EditColumnCellMap",this),
        3, ++yTableExcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel parameters
    int yExcel = -1;
    JPanel excel_JPanel = new JPanel();
    excel_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel", excel_JPanel );
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Output (workbook) file:"),
		0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
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
	JGUIUtil.addComponent(excel_JPanel, OutputFile_JPanel,
		1, yExcel, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excel_JPanel, __Worksheet_JTextField,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel,
        new JLabel ("Optional - worksheet name (default=first sheet)."),
        3, yExcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    //JGUIUtil.addComponent(main_JPanel, new JLabel ("Excel integer columns:"),
    //    0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelIntegerColumns_JTextField = new JTextField (20);
    //__ExcelIntegerColumns_JTextField.addKeyListener (this);
    //JGUIUtil.addComponent(main_JPanel, __ExcelIntegerColumns_JTextField,
    //    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(main_JPanel,
    //    new JLabel ("Optional - columns that are integers, separated by commas."),
    //    3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    __ExcelIntegerColumns_JTextField.setVisible(false);
    
    //JGUIUtil.addComponent(main_JPanel, new JLabel ("Excel date/time columns:"),
    //    0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelDateTimeColumns_JTextField = new JTextField (20);
    //__ExcelDateTimeColumns_JTextField.addKeyListener (this);
    //JGUIUtil.addComponent(main_JPanel, __ExcelDateTimeColumns_JTextField,
    //    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(main_JPanel,
    //    new JLabel ("Optional - columns that are date/times, separated by commas."),
    //    3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    __ExcelDateTimeColumns_JTextField.setVisible(false);
    
    //JGUIUtil.addComponent(main_JPanel, new JLabel ("Number precision:"),
    //    0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NumberPrecision_JTextField = new JTextField (10);
    //__NumberPrecision_JTextField.addKeyListener (this);
    //JGUIUtil.addComponent(main_JPanel, __NumberPrecision_JTextField,
    //    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(main_JPanel,
    //    new JLabel ("Optional - precision for numbers (default=6)."),
    //    3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    __NumberPrecision_JTextField.setVisible(false);
    
    //JGUIUtil.addComponent(main_JPanel, new JLabel( "Write all as text?:"),
    //    0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __WriteAllAsText_JComboBox = new SimpleJComboBox ( false );
    //__WriteAllAsText_JComboBox.add("");
    //__WriteAllAsText_JComboBox.add(__command._False);
    //__WriteAllAsText_JComboBox.add(__command._True);
    //__WriteAllAsText_JComboBox.select ( 0 );
    //__WriteAllAsText_JComboBox.addItemListener ( this );
    //JGUIUtil.addComponent(main_JPanel, __WriteAllAsText_JComboBox,
    //    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - write all cells as text? (default=" + __command._False + ")."),
    //    3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    __WriteAllAsText_JComboBox.setVisible(false);
    
    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Cell format:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CellFormat_JComboBox = new SimpleJComboBox ( false );
    __CellFormat_JComboBox.add("");
    __CellFormat_JComboBox.add(__command._FromExcel);
    __CellFormat_JComboBox.add(__command._FromTable);
    __CellFormat_JComboBox.select ( 0 );
    __CellFormat_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __KeepOpen_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - CURRENTLY IGNORED - cell format to use (default=" + __command._FromExcel + ")."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        */

    JGUIUtil.addComponent(excel_JPanel, new JLabel( "Keep file open?:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __KeepOpen_JComboBox = new SimpleJComboBox ( false );
    __KeepOpen_JComboBox.setPrototypeDisplayValue(__command._False + "MMMM"); // to fix some issues with layout of dynamic components
    List<String> keepChoices = new ArrayList<String>();
    keepChoices.add("");
    keepChoices.add(__command._False);
    keepChoices.add(__command._True);
    __KeepOpen_JComboBox.setData(keepChoices);
    __KeepOpen_JComboBox.addItemListener ( this );
    __KeepOpen_JComboBox.select ( 0 );
    JGUIUtil.addComponent(excel_JPanel, __KeepOpen_JComboBox,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel, new JLabel ( "Optional - keep Excel file open? (default=" + __command._False + ")."),
        3, yExcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (6,80);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 8, 1, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

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
{	String routine = getClass().getName() + ".refresh";
    String TableID = "";
    String OutputFile = "";
    String Worksheet = "";
	String ColumnIncludeFilters = "";
	//String ExcelIntegerColumns = "";
	//String ExcelDateTimeColumns = "";
	//String NumberPrecision = "";
	//String WriteAllAsText = "";
	String ColumnCellMap = "";
	String CellFormat = "";
	String KeepOpen = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
		OutputFile = props.getValue ( "OutputFile" );
		Worksheet = props.getValue ( "Worksheet" );
		ColumnIncludeFilters = props.getValue ( "ColumnIncludeFilters" );
		//ExcelIntegerColumns = props.getValue ( "ExcelIntegerColumns" );
		//ExcelDateTimeColumns = props.getValue ( "ExcelDateTimeColumns" );
		//NumberPrecision = props.getValue ( "NumberPrecision" );
		//WriteAllAsText = props.getValue ( "WriteAllAsText" );
		ColumnCellMap = props.getValue ( "ColumnCellMap" );
		CellFormat = props.getValue ( "CellFormat" );
		KeepOpen = props.getValue ( "KeepOpen" );
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
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
        if ( Worksheet != null ) {
            __Worksheet_JTextField.setText ( Worksheet );
        }
        if ( ColumnIncludeFilters != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( ColumnIncludeFilters );
        }
        /*
        if ( ExcelIntegerColumns != null ) {
            __ExcelIntegerColumns_JTextField.setText ( ExcelIntegerColumns );
        }
        if ( ExcelDateTimeColumns != null ) {
            __ExcelDateTimeColumns_JTextField.setText ( ExcelDateTimeColumns );
        }
        if ( NumberPrecision != null ) {
            __NumberPrecision_JTextField.setText ( NumberPrecision );
        }
        if ( WriteAllAsText == null || WriteAllAsText.equals("") ) {
            // Select a default...
            __WriteAllAsText_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __WriteAllAsText_JComboBox, WriteAllAsText, JGUIUtil.NONE, null, null ) ) {
                __WriteAllAsText_JComboBox.select ( WriteAllAsText );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nWriteAllAsText \"" +
                    WriteAllAsText + "\".  Select a different choice or Cancel." );
            }
        }
        */
        if ( ColumnCellMap != null ) {
            __ColumnCellMap_JTextArea.setText ( ColumnCellMap );
        }
        /*
        if ( CellFormat == null || CellFormat.equals("") ) {
            // Select a default...
            __CellFormat_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __CellFormat_JComboBox, CellFormat, JGUIUtil.NONE, null, null ) ) {
                __CellFormat_JComboBox.select ( CellFormat );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nCellFormat \"" +
                    CellFormat + "\".  Select a different choice or Cancel." );
            }
        }
        */
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
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Worksheet = __Worksheet_JTextField.getText().trim();
	ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
	/*
	ExcelIntegerColumns = __ExcelIntegerColumns_JTextField.getText().trim();
	ExcelDateTimeColumns = __ExcelDateTimeColumns_JTextField.getText().trim();
	NumberPrecision = __NumberPrecision_JTextField.getText().trim();
	WriteAllAsText = __WriteAllAsText_JComboBox.getSelected();
	*/
	ColumnCellMap = __ColumnCellMap_JTextArea.getText().trim().replace("\n"," ");
	//CellFormat = __CellFormat_JComboBox.getSelected();
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "ColumnIncludeFilters=" + ColumnIncludeFilters );
	//props.add ( "ExcelIntegerColumns=" + ExcelIntegerColumns );
	//props.add ( "ExcelDateTimeColumns=" + ExcelDateTimeColumns );
	//props.add ( "NumberPrecision=" + NumberPrecision );
	//props.add ( "WriteAllAsText=" + WriteAllAsText );
	props.add ( "ColumnCellMap=" + ColumnCellMap );
	props.add ( "CellFormat=" + CellFormat );
	props.add ( "KeepOpen=" + KeepOpen );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
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
