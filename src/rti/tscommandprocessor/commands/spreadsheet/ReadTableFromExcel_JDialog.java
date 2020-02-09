// ReadTableFromExcel_JDialog - Editor for the ReadTableFromExcel command.

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
import rti.tscommandprocessor.commands.spreadsheet.ExcelColumnNameRowType;
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
Editor for the ReadTableFromExcel command.
*/
@SuppressWarnings("serial")
public class ReadTableFromExcel_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea = null;
private JTabbedPane __main_JTabbedPane = null;
private JTextField __TableID_JTextField = null;
private JTextField __InputFile_JTextField = null;
private JTextField __Worksheet_JTextField = null;
private JTabbedPane __excelSpace_JTabbedPane = null;
private JTextField __ExcelAddress_JTextField = null;
private JTextField __ExcelNamedRange_JTextField = null;
private JTextField __ExcelTableName_JTextField = null;
private JTextField __Comment_JTextField = null;
private SimpleJComboBox __ExcelColumnNames_JComboBox = null;
private JTextArea __ColumnIncludeFilters_JTextArea = null;
private JTextArea __ColumnExcludeFilters_JTextArea = null;
private JTextField __ExcelDoubleColumns_JTextField = null;
private JTextField __ExcelIntegerColumns_JTextField = null;
private JTextField __ExcelDateTimeColumns_JTextField = null;
private JTextField __ExcelTextColumns_JTextField = null;
private JTextField __NumberPrecision_JTextField = null;
private SimpleJComboBox __ReadAllAsText_JComboBox = null;
private JTextField __RowCountProperty_JTextField = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadTableFromExcel_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadTableFromExcel_JDialog ( JFrame parent, ReadTableFromExcel_Command command )
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
		fc.setDialogTitle("Select Excel File");
     	fc.addChoosableFileFilter(new SimpleFileFilter("xls", "Excel File"));
		SimpleFileFilter sff = new SimpleFileFilter("xlsx", "Excel File");
		fc.addChoosableFileFilter(sff);
     	fc.addChoosableFileFilter(new SimpleFileFilter("xlsm", "Excel File with macros enabled"));
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
					__InputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "ReadTableFromExcel");
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
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectory)) {
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
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnIncludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
        String dict = (new DictionaryJDialog ( __parent, true, ColumnIncludeFilters,
            "Edit ColumnIncludeFilters Parameter", null, "Excel Column", "Pattern to include rows (* allowed)",10)).response();
        if ( dict != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnExcludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
        String dict = (new DictionaryJDialog ( __parent, true, ColumnExcludeFilters,
            "Edit ColumnExcludeFilters Parameter", null, "Excel Column", "Pattern to exclude rows (* allowed)",10)).response();
        if ( dict != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( dict );
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
    String TableID = __TableID_JTextField.getText().trim();
	String InputFile = __InputFile_JTextField.getText().trim();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String Comment = __Comment_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	String ExcelDoubleColumns  = __ExcelDoubleColumns_JTextField.getText().trim();
	String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	String ExcelTextColumns  = __ExcelTextColumns_JTextField.getText().trim();
	String NumberPrecision  = __NumberPrecision_JTextField.getText().trim();
	String ReadAllAsText  = __ReadAllAsText_JComboBox.getSelected();
	String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
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
    if (Comment.length() > 0) {
        props.set("Comment", Comment);
    }
    if ( ExcelDoubleColumns.length() > 0 ) {
        props.set ( "ExcelDoubleColumns", ExcelDoubleColumns );
    }
    if ( ExcelIntegerColumns.length() > 0 ) {
        props.set ( "ExcelIntegerColumns", ExcelIntegerColumns );
    }
    if ( ExcelDateTimeColumns.length() > 0 ) {
        props.set ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
    }
    if ( ExcelTextColumns.length() > 0 ) {
        props.set ( "ExcelTextColumns", ExcelTextColumns );
    }
    if ( NumberPrecision.length() > 0 ) {
        props.set ( "NumberPrecision", NumberPrecision );
    }
    if ( ReadAllAsText.length() > 0 ) {
        props.set ( "ReadAllAsText", ReadAllAsText );
    }
    if ( KeepOpen.length() > 0 ) {
        props.set ( "KeepOpen", KeepOpen );
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
    String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String ExcelColumnNames  = __ExcelColumnNames_JComboBox.getSelected();
	String ColumnIncludeFilters  = __ColumnIncludeFilters_JTextArea.getText().trim();
	String ColumnExcludeFilters  = __ColumnExcludeFilters_JTextArea.getText().trim();
	String Comment = __Comment_JTextField.getText().trim();
	String ExcelDoubleColumns  = __ExcelDoubleColumns_JTextField.getText().trim();
	String ExcelIntegerColumns  = __ExcelIntegerColumns_JTextField.getText().trim();
	String ExcelDateTimeColumns  = __ExcelDateTimeColumns_JTextField.getText().trim();
	String ExcelTextColumns  = __ExcelTextColumns_JTextField.getText().trim();
	String NumberPrecision  = __NumberPrecision_JTextField.getText().trim();
	String ReadAllAsText  = __ReadAllAsText_JComboBox.getSelected();
    String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
    String KeepOpen  = __KeepOpen_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "ExcelAddress", ExcelAddress );
	__command.setCommandParameter ( "ExcelNamedRange", ExcelNamedRange );
	__command.setCommandParameter ( "ExcelTableName", ExcelTableName );
	__command.setCommandParameter ( "ExcelColumnNames", ExcelColumnNames );
	__command.setCommandParameter ( "ColumnIncludeFilters", ColumnIncludeFilters );
	__command.setCommandParameter ( "ColumnExcludeFilters", ColumnExcludeFilters );
	__command.setCommandParameter ( "Comment", Comment );
	__command.setCommandParameter ( "ExcelDoubleColumns", ExcelDoubleColumns );
	__command.setCommandParameter ( "ExcelIntegerColumns", ExcelIntegerColumns );
	__command.setCommandParameter ( "ExcelDateTimeColumns", ExcelDateTimeColumns );
	__command.setCommandParameter ( "ExcelTextColumns", ExcelTextColumns );
	__command.setCommandParameter ( "NumberPrecision", NumberPrecision );
	__command.setCommandParameter ( "ReadAllAsText", ReadAllAsText );
	__command.setCommandParameter ( "RowCountProperty", RowCountProperty );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, ReadTableFromExcel_Command command )
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

   	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"This command reads a table from a worksheet in a Microsoft Excel workbook file (*.xls, *.xlsx, *.xlsm)."),
    	0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    // Panel for table parameters
    int yTable = -1;
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Table", table_JPanel );
    
   	JGUIUtil.addComponent(table_JPanel, new JLabel (
    	"The Excel worksheet contents will be used to create the new table specified below."),
    	0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(table_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yTable, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Table ID:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JTextField = new JTextField (30);
    __TableID_JTextField.setToolTipText("Select the table to write or use ${Property} notation");
    __TableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __TableID_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Required - unique identifier for the created table."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Number precision:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NumberPrecision_JTextField = new JTextField (10);
    __NumberPrecision_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __NumberPrecision_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel,
        new JLabel ("Optional - precision for numbers in table (default=6)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel parameters
    int yExcel = -1;
    JPanel excel_JPanel = new JPanel();
    excel_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel", excel_JPanel );

    JGUIUtil.addComponent(excel_JPanel, new JLabel (
		"It is recommended that the location of the Excel file is " +
		"specified using a path relative to the working directory."),
		0, ++yExcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(excel_JPanel, new JLabel ( ""),
		0, ++yExcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
    	JGUIUtil.addComponent(excel_JPanel, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yExcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Input (workbook) file:"),
		0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (45);
	__InputFile_JTextField.setToolTipText("Specify the path to the Excel file or use ${Property} notation");
	__InputFile_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile_JPanel = new JPanel();
	InputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile_JPanel, __InputFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(excel_JPanel, InputFile_JPanel,
		1, yExcel, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.setToolTipText("Specify the name of the worksheet or use ${Property} notation");
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excel_JPanel, __Worksheet_JTextField,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel,
        new JLabel ("Required (if not in address) - worksheet name (default=first sheet)."),
        3, yExcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel (
		"A contiguous block of cells must be specified using one of the address methods below."),
		0, ++yExcel, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        
    __excelSpace_JTabbedPane = new JTabbedPane ();
    __excelSpace_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify the address for a contiguous block of cells the in Excel worksheet" ));
    JGUIUtil.addComponent(excel_JPanel, __excelSpace_JTabbedPane,
        0, ++yExcel, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
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
    JGUIUtil.addComponent(address_JPanel, new JLabel ("Excel cell block address in format A1:B2 (default=entire sheet)."),
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
    
    JPanel extable_JPanel = new JPanel();
    extable_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Table Name", extable_JPanel );
    int yExTable = -1;
    
    JGUIUtil.addComponent(extable_JPanel, new JLabel ("Excel table name:"),
        0, ++yExTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelTableName_JTextField = new JTextField (10);
    __ExcelTableName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(extable_JPanel, __ExcelTableName_JTextField,
        1, yExTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(extable_JPanel, new JLabel ("Excel table name."),
        3, yExTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel( "Excel column names:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelColumnNames_JComboBox = new SimpleJComboBox ( false );
    __ExcelColumnNames_JComboBox.add("");
    __ExcelColumnNames_JComboBox.add(""+ExcelColumnNameRowType.COLUMN_N);
    __ExcelColumnNames_JComboBox.add(""+ExcelColumnNameRowType.FIRST_ROW_IN_RANGE);
    __ExcelColumnNames_JComboBox.add(""+ExcelColumnNameRowType.ROW_BEFORE_RANGE);
    __ExcelColumnNames_JComboBox.select ( 0 );
    __ExcelColumnNames_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excel_JPanel, __ExcelColumnNames_JComboBox,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel, new JLabel ( "Optional - how to define column names (default=" +
    	ExcelColumnNameRowType.COLUMN_N + ")."),
        3, yExcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel( "Keep file open?:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __KeepOpen_JComboBox = new SimpleJComboBox ( false );
    List<String> keepChoices = new ArrayList<String>();
    __KeepOpen_JComboBox.setPrototypeDisplayValue(__command._False + "MMMM"); // to fix some issues with layout of dynamic components
    keepChoices.add("");
    keepChoices.add(__command._False);
    keepChoices.add(__command._True);
    __KeepOpen_JComboBox.setData(keepChoices);
    __KeepOpen_JComboBox.select ( 0 );
    __KeepOpen_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excel_JPanel, __KeepOpen_JComboBox,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel, new JLabel ( "Optional - keep Excel file open? (default=" + __command._False + ")."),
        3, yExcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Panel for filter parameters
    int yFilter = -1;
    JPanel filter_JPanel = new JPanel();
    filter_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Filters", filter_JPanel );
    
    JGUIUtil.addComponent(filter_JPanel, new JLabel (
		"Specify the rows to include and exclude by providing filter information below."),
		0, ++yFilter, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(filter_JPanel, new JLabel (
		"Comments, if used, must be specified in the first Excel column in the input, which should contain text."),
		0, ++yFilter, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(filter_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++yFilter, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(filter_JPanel, new JLabel ("Column filters to include rows:"),
        0, ++yFilter, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnIncludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnIncludeFilters_JTextArea.setLineWrap ( true );
    __ColumnIncludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnIncludeFilters_JTextArea.setToolTipText("TableColumn1:DatastoreColumn1,TableColumn2:DataStoreColumn2");
    __ColumnIncludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(filter_JPanel, new JScrollPane(__ColumnIncludeFilters_JTextArea),
        1, yFilter, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(filter_JPanel, new JLabel ("Optional - column patterns to include rows (default=include all)."),
        3, yFilter, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(filter_JPanel, new SimpleJButton ("Edit","EditColumnIncludeFilters",this),
        3, ++yFilter, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(filter_JPanel, new JLabel ("Column filters to exclude rows:"),
        0, ++yFilter, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnExcludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnExcludeFilters_JTextArea.setLineWrap ( true );
    __ColumnExcludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnExcludeFilters_JTextArea.setToolTipText("TableColumn1:DatastoreColumn1,TableColumn2:DataStoreColumn2");
    __ColumnExcludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(filter_JPanel, new JScrollPane(__ColumnExcludeFilters_JTextArea),
        1, yFilter, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(filter_JPanel, new JLabel ("Optional - column patterns to exclude rows (default=include all)."),
        3, yFilter, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(filter_JPanel, new SimpleJButton ("Edit","EditColumnExcludeFilters",this),
        3, ++yFilter, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(filter_JPanel, new JLabel ("Comment string to ignore rows:"),
        0, ++yFilter, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Comment_JTextField = new JTextField (10);
    __Comment_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(filter_JPanel, __Comment_JTextField,
        1, yFilter, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(filter_JPanel, new JLabel (
        "Optional - string that indicates comment lines (default=none)."),
        3, yFilter, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel column type parameters
    int yType = -1;
    JPanel type_JPanel = new JPanel();
    type_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Column Types", type_JPanel );
    
    JGUIUtil.addComponent(type_JPanel, new JLabel (
		"An attempt will be made to determine table column types from Excel data (Number=Double column, Text=String column, Date=DateTime column)."),
		0, ++yType, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel, new JLabel (
		"However, the following parameters can be specifed to ensure proper handling of data types."),
		0, ++yType, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++yType, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(type_JPanel, new JLabel ("Excel double columns:"),
        0, ++yType, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelDoubleColumns_JTextField = new JTextField (20);
    __ExcelDoubleColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(type_JPanel, __ExcelDoubleColumns_JTextField,
        1, yType, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel,
        new JLabel ("Optional - columns that are double-precision numbers, separated by commas."),
        3, yType, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(type_JPanel, new JLabel ("Excel integer columns:"),
        0, ++yType, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelIntegerColumns_JTextField = new JTextField (20);
    __ExcelIntegerColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(type_JPanel, __ExcelIntegerColumns_JTextField,
        1, yType, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel,
        new JLabel ("Optional - columns that are integers, separated by commas."),
        3, yType, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(type_JPanel, new JLabel ("Excel date/time columns:"),
        0, ++yType, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelDateTimeColumns_JTextField = new JTextField (20);
    __ExcelDateTimeColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(type_JPanel, __ExcelDateTimeColumns_JTextField,
        1, yType, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel,
        new JLabel ("Optional - columns that are date/times, separated by commas."),
        3, yType, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(type_JPanel, new JLabel ("Excel text columns:"),
        0, ++yType, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelTextColumns_JTextField = new JTextField (20);
    __ExcelTextColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(type_JPanel, __ExcelTextColumns_JTextField,
        1, yType, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel,
        new JLabel ("Optional - columns that are text, separated by commas."),
        3, yType, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(type_JPanel, new JLabel( "Read all as text?:"),
        0, ++yType, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ReadAllAsText_JComboBox = new SimpleJComboBox ( false );
    __ReadAllAsText_JComboBox.add("");
    __ReadAllAsText_JComboBox.add(__command._False);
    __ReadAllAsText_JComboBox.add(__command._True);
    __ReadAllAsText_JComboBox.select ( 0 );
    __ReadAllAsText_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(type_JPanel, __ReadAllAsText_JComboBox,
        1, yType, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(type_JPanel, new JLabel ( "Optional - read all cells as text? (default=" + __command._False + ")."),
        3, yType, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Panel for Excel column type parameters
    int yProp = -1;
    JPanel prop_JPanel = new JPanel();
    prop_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Properties", prop_JPanel );
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel (
		"Use the following parameter to set a processor property for the table row count, which can then be used for error handling."),
		0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel("Row count property:"),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowCountProperty_JTextField = new JTextField ( "", 20 );
    __RowCountProperty_JTextField.setToolTipText("Specify the property name for the copied table row count, can use ${Property} notation");
    __RowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(prop_JPanel, __RowCountProperty_JTextField,
        1, yProp, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel, new JLabel ( "Optional - processor property to set as output table row count." ),
        3, yProp, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
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
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add (__cancel_JButton);
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
    String InputFile = "";
    String Worksheet = "";
	String ExcelAddress = "";
	String ExcelNamedRange = "";
	String ExcelTableName = "";
	String ExcelColumnNames = "";
	String ColumnIncludeFilters = "";
	String ColumnExcludeFilters = "";
    String Comment = "";
	String ExcelDoubleColumns = "";
	String ExcelIntegerColumns = "";
	String ExcelDateTimeColumns = "";
	String ExcelTextColumns = "";
	String NumberPrecision = "";
	String ReadAllAsText = "";
    String RowCountProperty = "";
    String KeepOpen = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
		InputFile = props.getValue ( "InputFile" );
		Worksheet = props.getValue ( "Worksheet" );
		ExcelAddress = props.getValue ( "ExcelAddress" );
		ExcelNamedRange = props.getValue ( "ExcelNamedRange" );
		ExcelTableName = props.getValue ( "ExcelTableName" );
		ExcelColumnNames = props.getValue ( "ExcelColumnNames" );
		ColumnIncludeFilters = props.getValue ( "ColumnIncludeFilters" );
		ColumnExcludeFilters = props.getValue ( "ColumnExcludeFilters" );
		Comment = props.getValue ( "Comment" );
		ExcelDoubleColumns = props.getValue ( "ExcelDoubleColumns" );
		ExcelIntegerColumns = props.getValue ( "ExcelIntegerColumns" );
		ExcelDateTimeColumns = props.getValue ( "ExcelDateTimeColumns" );
		ExcelTextColumns = props.getValue ( "ExcelTextColumns" );
		NumberPrecision = props.getValue ( "NumberPrecision" );
		ReadAllAsText = props.getValue ( "ReadAllAsText" );
        RowCountProperty = props.getValue ( "RowCountProperty" );
		KeepOpen = props.getValue ( "KeepOpen" );
        if ( TableID != null ) {
            __TableID_JTextField.setText ( TableID );
        }
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
        if ( Worksheet != null ) {
            __Worksheet_JTextField.setText ( Worksheet );
        }
		if ( (ExcelAddress != null) && !ExcelAddress.isEmpty() ) {
			__ExcelAddress_JTextField.setText ( ExcelAddress );
			// Also select the tab to be visible
			__excelSpace_JTabbedPane.setSelectedIndex(0);
		}
		if ( (ExcelNamedRange != null) && !ExcelNamedRange.isEmpty() ) {
			__ExcelNamedRange_JTextField.setText ( ExcelNamedRange );
			__excelSpace_JTabbedPane.setSelectedIndex(1);
		}
        if ( (ExcelTableName != null) && !ExcelTableName.isEmpty() ) {
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
        if ( Comment != null) {
            __Comment_JTextField.setText(Comment);
        }
        if ( ExcelDoubleColumns != null ) {
            __ExcelDoubleColumns_JTextField.setText ( ExcelDoubleColumns );
        }
        if ( ExcelIntegerColumns != null ) {
            __ExcelIntegerColumns_JTextField.setText ( ExcelIntegerColumns );
        }
        if ( ExcelDateTimeColumns != null ) {
            __ExcelDateTimeColumns_JTextField.setText ( ExcelDateTimeColumns );
        }
        if ( ExcelTextColumns != null ) {
            __ExcelTextColumns_JTextField.setText ( ExcelTextColumns );
        }
        if ( NumberPrecision != null ) {
            __NumberPrecision_JTextField.setText ( NumberPrecision );
        }
        if ( ReadAllAsText == null || ReadAllAsText.equals("") ) {
            // Select a default...
            __ReadAllAsText_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ReadAllAsText_JComboBox, ReadAllAsText, JGUIUtil.NONE, null, null ) ) {
                __ReadAllAsText_JComboBox.select ( ReadAllAsText );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nReadAllAsText \"" +
                    ReadAllAsText + "\".  Select a different choice or Cancel." );
            }
        }
        if ( RowCountProperty != null ) {
            __RowCountProperty_JTextField.setText ( RowCountProperty );
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
	}
	// Regardless, reset the command from the fields...
    TableID = __TableID_JTextField.getText().trim();
	InputFile = __InputFile_JTextField.getText().trim();
	Worksheet = __Worksheet_JTextField.getText().trim();
	ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	ExcelColumnNames = __ExcelColumnNames_JComboBox.getSelected();
	ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
	ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
	Comment = __Comment_JTextField.getText().trim();
	ExcelDoubleColumns = __ExcelDoubleColumns_JTextField.getText().trim();
	ExcelIntegerColumns = __ExcelIntegerColumns_JTextField.getText().trim();
	ExcelDateTimeColumns = __ExcelDateTimeColumns_JTextField.getText().trim();
	ExcelTextColumns = __ExcelTextColumns_JTextField.getText().trim();
	NumberPrecision = __NumberPrecision_JTextField.getText().trim();
	ReadAllAsText = __ReadAllAsText_JComboBox.getSelected();
	RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "InputFile=" + InputFile );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "ExcelAddress=" + ExcelAddress );
	props.add ( "ExcelNamedRange=" + ExcelNamedRange );
	props.add ( "ExcelTableName=" + ExcelTableName );
	props.add ( "ExcelColumnNames=" + ExcelColumnNames );
	props.add ( "ColumnIncludeFilters=" + ColumnIncludeFilters );
	props.add ( "ColumnExcludeFilters=" + ColumnExcludeFilters );
	props.add ( "Comment=" + Comment );
	props.add ( "ExcelDoubleColumns=" + ExcelDoubleColumns );
	props.add ( "ExcelIntegerColumns=" + ExcelIntegerColumns );
	props.add ( "ExcelDateTimeColumns=" + ExcelDateTimeColumns );
	props.add ( "ExcelTextColumns=" + ExcelTextColumns );
	props.add ( "NumberPrecision=" + NumberPrecision );
	props.add ( "ReadAllAsText=" + ReadAllAsText );
	props.add ( "RowCountProperty=" + RowCountProperty );
	props.add ( "KeepOpen=" + KeepOpen );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		if ( (InputFile != null) && !InputFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( InputFile );
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
