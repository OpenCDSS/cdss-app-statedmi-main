// WriteTimeSeriesToExcel_JDialog - Editor for the WriteTimeSeriesToExcel command.

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;
import rti.tscommandprocessor.ui.CommandEditorUtil;

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

import RTi.TS.TSFormatSpecifiersJPanel;
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
import RTi.Util.Time.DateTimeFormatterSpecifiersJPanel;
import RTi.Util.Time.DateTimeFormatterType;

/**
Editor for the WriteTimeSeriesToExcel command.
*/
@SuppressWarnings("serial")
public class WriteTimeSeriesToExcel_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rele";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __TSList_JComboBox = null;
private JLabel __TSID_JLabel = null;
private SimpleJComboBox __TSID_JComboBox = null;
private JLabel __EnsembleID_JLabel = null;
private SimpleJComboBox __EnsembleID_JComboBox = null;
private JTextField __Precision_JTextField = null;
private JTextField __MissingValue_JTextField = null;
private JTextField __OutputStart_JTextField = null;
private JTextField __OutputEnd_JTextField = null;

private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __Append_JComboBox = null;
private JTextField __Worksheet_JTextField = null;
private JTabbedPane __excelSpace_JTabbedPane = null;
private JTextField __ExcelAddress_JTextField = null;
private JTextField __ExcelNamedRange_JTextField = null;
private JTextField __ExcelTableName_JTextField = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private JTextField __DateTimeColumn_JTextField = null;
private DateTimeFormatterSpecifiersJPanel __DateTimeFormat_JPanel = null;
private JTextField __DateColumn_JTextField = null;
private DateTimeFormatterSpecifiersJPanel __DateFormat_JPanel = null;
private JTextField __TimeColumn_JTextField = null;
private DateTimeFormatterSpecifiersJPanel __TimeFormat_JPanel = null;
private TSFormatSpecifiersJPanel __ValueColumns_JTextField = null;
private JTextField __Author_JTextField = null;
private TSFormatSpecifiersJPanel __ColumnComment_JTextField = null;
private JTextField __ColumnCommentWidth_JTextField = null;
private JTextField __ColumnCommentHeight_JTextField = null;
private TSFormatSpecifiersJPanel __ValueComment_JTextField = null;
private SimpleJComboBox __SkipValueCommentIfNoFlag_JComboBox = null;
private JTextField __CommentWidth_JTextField = null;
private JTextField __CommentHeight_JTextField = null;
private SimpleJComboBox __ColumnStyleTableID_JComboBox = null;
private SimpleJComboBox __ColumnConditionTableID_JComboBox = null;
private SimpleJComboBox __StyleTableID_JComboBox = null;
private SimpleJComboBox __ConditionTableID_JComboBox = null;
private JTextField __LegendWorksheet_JTextField = null;
private JTextField __LegendAddress_JTextField = null;

private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private WriteTimeSeriesToExcel_Command __command = null;
private boolean __ok = false;
//private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTimeSeriesToExcel_JDialog ( JFrame parent, WriteTimeSeriesToExcel_Command command, List<String> tableIDChoices )
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
				Message.printWarning ( 1,"WriteTimeSeriesToExcel_JDialog", "Error converting file to relative path." );
			}
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WriteTimeSeriesToExcel");
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
	/*
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnExcludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
        String dict = (new DictionaryJDialog ( __parent, true, ColumnExcludeFilters,
            "Edit ColumnExcludeFilters Parameter", null, "Table Column", "Pattern to exclude rows (* allowed)",10)).response();
        if ( dict != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( dict );
            refresh();
        }
    }
    */
}

//Start event handlers for DocumentListener...

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void changedUpdate ( DocumentEvent e )
{   checkGUIState();
  refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void insertUpdate ( DocumentEvent e )
{   checkGUIState();
  refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void removeUpdate ( DocumentEvent e )
{   checkGUIState();
  refresh();
}

//...End event handlers for DocumentListener

/**
Check the GUI state to make sure that appropriate components are enabled/disabled.
*/
private void checkGUIState ()
{
    String TSList = __TSList_JComboBox.getSelected();
    if ( TSListType.ALL_MATCHING_TSID.equals(TSList) ||
        TSListType.FIRST_MATCHING_TSID.equals(TSList) ||
        TSListType.LAST_MATCHING_TSID.equals(TSList) ) {
        __TSID_JComboBox.setEnabled(true);
        __TSID_JLabel.setEnabled ( true );
    }
    else {
        __TSID_JComboBox.setEnabled(false);
        __TSID_JLabel.setEnabled ( false );
    }
    if ( TSListType.ENSEMBLE_ID.equals(TSList)) {
        __EnsembleID_JComboBox.setEnabled(true);
        __EnsembleID_JLabel.setEnabled ( true );
    }
    else {
        __EnsembleID_JComboBox.setEnabled(false);
        __EnsembleID_JLabel.setEnabled ( false );
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
    String TSList = __TSList_JComboBox.getSelected();
    String TSID = __TSID_JComboBox.getSelected();
    String EnsembleID = __EnsembleID_JComboBox.getSelected();
	String MissingValue = __MissingValue_JTextField.getText().trim();
	String Precision  = __Precision_JTextField.getText().trim();
    String OutputStart = __OutputStart_JTextField.getText().trim();
    String OutputEnd = __OutputEnd_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Append = __Append_JComboBox.getSelected();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	String DateTimeColumn = __DateTimeColumn_JTextField.getText().trim();
    String DateTimeFormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
    String DateTimeFormat = __DateTimeFormat_JPanel.getText().trim();
	String DateColumn = __DateColumn_JTextField.getText().trim();
    String DateFormatterType = __DateFormat_JPanel.getSelectedFormatterType().trim();
    String DateFormat = __DateFormat_JPanel.getText().trim();
	String TimeColumn = __TimeColumn_JTextField.getText().trim();
    String TimeFormatterType = __TimeFormat_JPanel.getSelectedFormatterType().trim();
    String TimeFormat = __TimeFormat_JPanel.getText().trim();
	String ValueColumns = __ValueColumns_JTextField.getText().trim();
	String Author = __Author_JTextField.getText().trim();
	String ColumnComment = __ColumnComment_JTextField.getText().trim();
	String ColumnCommentWidth = __CommentWidth_JTextField.getText().trim();
	String ColumnCommentHeight = __CommentHeight_JTextField.getText().trim();
	String ValueComment = __ValueComment_JTextField.getText().trim();
	String SkipValueCommentIfNoFlag = __SkipValueCommentIfNoFlag_JComboBox.getSelected();
	String CommentWidth = __CommentWidth_JTextField.getText().trim();
	String CommentHeight = __CommentHeight_JTextField.getText().trim();
	String ColumnStyleTableID = __ColumnStyleTableID_JComboBox.getSelected();
	String ColumnConditionTableID = __ColumnConditionTableID_JComboBox.getSelected();
	String StyleTableID = __StyleTableID_JComboBox.getSelected();
	String ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	String LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	String LegendAddress = __LegendAddress_JTextField.getText().trim();
	__error_wait = false;

    if ( TSList.length() > 0 ) {
        props.set ( "TSList", TSList );
    }
    if ( TSID.length() > 0 ) {
        props.set ( "TSID", TSID );
    }
    if ( EnsembleID.length() > 0 ) {
        props.set ( "EnsembleID", EnsembleID );
    }
    if ( MissingValue.length() > 0 ) {
        props.set ( "MissingValue", MissingValue );
    }
    if ( Precision.length() > 0 ) {
        props.set ( "Precision", Precision );
    }
    if ( OutputStart.length() > 0 ) {
        props.set ( "OutputStart", OutputStart );
    }
    if ( OutputEnd.length() > 0 ) {
        props.set ( "OutputEnd", OutputEnd );
    }
	if ( OutputFile.length() > 0 ) {
		props.set ( "OutputFile", OutputFile );
	}
	if ( Append.length() > 0 ) {
		props.set ( "Append", Append );
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
    if ( KeepOpen.length() > 0 ) {
        props.set ( "KeepOpen", KeepOpen );
    }
    if  ( DateTimeColumn.length() > 0 ) {
    	props.set("DateTimeColumn", DateTimeColumn);
    }
    if ( DateTimeFormatterType.length() > 0 ) {
    	props.set ( "DateTimeFormatterType", DateTimeFormatterType );
    }
    if ( DateTimeFormat.length() > 0 ) {
    	props.set ( "DateTimeFormat", DateTimeFormat );
    }
    if  ( DateColumn.length() > 0 ) {
    	props.set("DateColumn", DateColumn);
    }
    if ( DateFormatterType.length() > 0 ) {
    	props.set ( "DateFormatterType", DateFormatterType );
    }
    if ( DateFormat.length() > 0 ) {
    	props.set ( "DateFormat", DateFormat );
    }
    if  ( TimeColumn.length() > 0 ) {
    	props.set("TimeColumn", TimeColumn);
    }
    if ( TimeFormatterType.length() > 0 ) {
    	props.set ( "TimeFormatterType", TimeFormatterType );
    }
    if ( TimeFormat.length() > 0 ) {
    	props.set ( "TimeFormat", TimeFormat );
    }
    if  ( ValueColumns.length() > 0 ) {
    	props.set("ValueColumns", ValueColumns);
    }
    if  ( Author.length() > 0 ) {
    	props.set("Author", Author);
    }
    if  ( ColumnComment.length() > 0 ) {
    	props.set("ColumnComment", ColumnComment);
    }
    if ( ColumnCommentWidth.length() > 0 ) {
        props.set ( "ColumnCommentWidth", ColumnCommentWidth );
    }
    if ( ColumnCommentHeight.length() > 0 ) {
        props.set ( "ColumnCommentHeight", ColumnCommentHeight );
    }
    if  ( ValueComment.length() > 0 ) {
    	props.set("ValueComment", ValueComment);
    }
    if  ( SkipValueCommentIfNoFlag.length() > 0 ) {
    	props.set("SkipValueCommentIfNoFlag", SkipValueCommentIfNoFlag);
    }
    if ( CommentWidth.length() > 0 ) {
        props.set ( "CommentWidth", CommentWidth );
    }
    if ( CommentHeight.length() > 0 ) {
        props.set ( "CommentHeight", CommentHeight );
    }
    if ( ColumnStyleTableID.length() > 0 ) {
        props.set ( "ColumnStyleTableID", ColumnStyleTableID );
    }
    if ( ColumnConditionTableID.length() > 0 ) {
        props.set ( "ColumnConditionTableID", ColumnConditionTableID );
    }
    if ( StyleTableID.length() > 0 ) {
        props.set ( "StyleTableID", StyleTableID );
    }
    if ( ConditionTableID.length() > 0 ) {
        props.set ( "ConditionTableID", ConditionTableID );
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
{	String TSList = __TSList_JComboBox.getSelected();
	String TSID = __TSID_JComboBox.getSelected();
	String EnsembleID = __EnsembleID_JComboBox.getSelected();
	String MissingValue = __MissingValue_JTextField.getText().trim();
	String Precision  = __Precision_JTextField.getText().trim();
	String OutputStart = __OutputStart_JTextField.getText().trim();
	String OutputEnd = __OutputEnd_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
    String Append = __Append_JComboBox.getSelected();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String KeepOpen  = __KeepOpen_JComboBox.getSelected();
	String DateTimeColumn = __DateTimeColumn_JTextField.getText().trim();
	String DateTimeFormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
	String DateTimeFormat = __DateTimeFormat_JPanel.getText().trim();
	String DateColumn = __DateColumn_JTextField.getText().trim();
	String DateFormatterType = __DateFormat_JPanel.getSelectedFormatterType().trim();
	String DateFormat = __DateFormat_JPanel.getText().trim();
	String TimeColumn = __TimeColumn_JTextField.getText().trim();
	String TimeFormatterType = __TimeFormat_JPanel.getSelectedFormatterType().trim();
	String TimeFormat = __TimeFormat_JPanel.getText().trim();
	String ValueColumns = __ValueColumns_JTextField.getText().trim();
	String Author = __Author_JTextField.getText().trim();
	String ColumnComment = __ColumnComment_JTextField.getText().trim();
	String ColumnCommentWidth = __ColumnCommentWidth_JTextField.getText().trim();
	String ColumnCommentHeight = __ColumnCommentHeight_JTextField.getText().trim();
	String ValueComment = __ValueComment_JTextField.getText().trim();
	String SkipValueCommentIfNoFlag = __SkipValueCommentIfNoFlag_JComboBox.getSelected();
	String CommentWidth = __CommentWidth_JTextField.getText().trim();
	String CommentHeight = __CommentHeight_JTextField.getText().trim();
	String ColumnStyleTableID = __ColumnStyleTableID_JComboBox.getSelected();
	String ColumnConditionTableID = __ColumnConditionTableID_JComboBox.getSelected();
	String StyleTableID = __StyleTableID_JComboBox.getSelected();
	String ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	String LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	String LegendAddress = __LegendAddress_JTextField.getText().trim();
	__command.setCommandParameter ( "TSList", TSList );
    __command.setCommandParameter ( "TSID", TSID );
    __command.setCommandParameter ( "EnsembleID", EnsembleID );
	__command.setCommandParameter ( "MissingValue", MissingValue );
	__command.setCommandParameter ( "Precision", Precision );
    __command.setCommandParameter ( "OutputStart", OutputStart );
    __command.setCommandParameter ( "OutputEnd", OutputEnd );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "Append", Append );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "ExcelAddress", ExcelAddress );
	__command.setCommandParameter ( "ExcelNamedRange", ExcelNamedRange );
	__command.setCommandParameter ( "ExcelTableName", ExcelTableName );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
    __command.setCommandParameter ( "DateTimeColumn", DateTimeColumn );
    __command.setCommandParameter ( "DateTimeFormatterType", DateTimeFormatterType );
	__command.setCommandParameter ( "DateTimeFormat", DateTimeFormat );
	__command.setCommandParameter ( "DateColumn", DateColumn );
	__command.setCommandParameter ( "DateFormatterType", DateFormatterType );
	__command.setCommandParameter ( "DateFormat", DateFormat );
	__command.setCommandParameter ( "TimeColumn", TimeColumn );
	__command.setCommandParameter ( "TimeFormatterType", TimeFormatterType );
	__command.setCommandParameter ( "TimeFormat", TimeFormat );
	__command.setCommandParameter ( "ValueColumns", ValueColumns );
	__command.setCommandParameter ( "Author", Author );
	__command.setCommandParameter ( "ColumnComment", ColumnComment );
	__command.setCommandParameter ( "ColumnCommentWidth", ColumnCommentWidth );
	__command.setCommandParameter ( "ColumnCommentHeight", ColumnCommentHeight );
	__command.setCommandParameter ( "ValueComment", ValueComment );
	__command.setCommandParameter ( "SkipValueCommentIfNoFlag", SkipValueCommentIfNoFlag );
	__command.setCommandParameter ( "CommentWidth", CommentWidth );
	__command.setCommandParameter ( "CommentHeight", CommentHeight );
	__command.setCommandParameter ( "ColumnStyleTableID", ColumnStyleTableID );
	__command.setCommandParameter ( "ColumnConditionTableID", ColumnConditionTableID );
	__command.setCommandParameter ( "StyleTableID", StyleTableID );
	__command.setCommandParameter ( "ConditionTableID", ConditionTableID );
	__command.setCommandParameter ( "LegendWorksheet", LegendWorksheet );
	__command.setCommandParameter ( "LegendAddress", LegendAddress );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, WriteTimeSeriesToExcel_Command command, List<String> tableIDChoices )
{	__command = command;
    //__parent = parent;
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
    	"This command writes a list of time series to a worksheet in a Microsoft Excel workbook file (*.xls, *.xlsx)."),
    	0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Time series are written as a sequence of columns, for simple data transfer of large amounts of data."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for time series
    int yTs = -1;
    JPanel ts_JPanel = new JPanel();
    ts_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Time Series to Write", ts_JPanel );
    
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
		"Specify the time series to output.  Each time series will be output as a column."),
		0, ++yTs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(ts_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yTs, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __TSList_JComboBox = new SimpleJComboBox(false);
    yTs = CommandEditorUtil.addTSListToEditorDialogPanel ( this, ts_JPanel, __TSList_JComboBox, yTs );

    __TSID_JLabel = new JLabel ("TSID (for TSList=" + TSListType.ALL_MATCHING_TSID.toString() + "):");
    __TSID_JComboBox = new SimpleJComboBox ( true ); // Allow edits
    __TSID_JComboBox.setToolTipText("Select a time series TSID/alias from the list or specify with ${Property} notation");
    List<String> tsids = TSCommandProcessorUtil.getTSIdentifiersNoInputFromCommandsBeforeCommand(
        (StateDMI_Processor)__command.getCommandProcessor(), __command );
    yTs = CommandEditorUtil.addTSIDToEditorDialogPanel ( this, this, ts_JPanel, __TSID_JLabel, __TSID_JComboBox, tsids, yTs );
    
    __EnsembleID_JLabel = new JLabel ("EnsembleID (for TSList=" + TSListType.ENSEMBLE_ID.toString() + "):");
    __EnsembleID_JComboBox = new SimpleJComboBox ( true ); // Allow edits
    __EnsembleID_JComboBox.setToolTipText("Select an ensemble identifier from the list or specify with ${Property} notation");
    List<String> EnsembleIDs = TSCommandProcessorUtil.getEnsembleIdentifiersFromCommandsBeforeCommand(
        (StateDMI_Processor)__command.getCommandProcessor(), __command );
    yTs = CommandEditorUtil.addEnsembleIDToEditorDialogPanel (
        this, this, ts_JPanel, __EnsembleID_JLabel, __EnsembleID_JComboBox, EnsembleIDs, yTs );
    
    JGUIUtil.addComponent(ts_JPanel, new JLabel ( "Missing value:" ),
        0, ++yTs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MissingValue_JTextField = new JTextField ( "", 10 );
    __MissingValue_JTextField.setToolTipText("Specify " + __command._Blank + " to output a blank.");
    __MissingValue_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(ts_JPanel, __MissingValue_JTextField,
        1, yTs, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
        "Optional - value to write for missing data (default=initial missing value)."),
        3, yTs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(ts_JPanel, new JLabel ("Output precision:"),
        0, ++yTs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Precision_JTextField = new JTextField (10);
    __Precision_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(ts_JPanel, __Precision_JTextField,
        1, yTs, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel,
        new JLabel ("Optional - precision for data values (default=based on units)."),
        3, yTs, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(ts_JPanel, new JLabel ("Output start:"), 
        0, ++yTs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputStart_JTextField = new JTextField (20);
    __OutputStart_JTextField.setToolTipText("Specify the output start using a date/time string or ${Property} notation");
    __OutputStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(ts_JPanel, __OutputStart_JTextField,
        1, yTs, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
        "Optional - override the global output start (default=write all data)."),
        3, yTs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(ts_JPanel, new JLabel ( "Output end:"), 
        0, ++yTs, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputEnd_JTextField = new JTextField (20);
    __OutputEnd_JTextField.setToolTipText("Specify the output end using a date/time string or ${Property} notation");
    __OutputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(ts_JPanel, __OutputEnd_JTextField,
        1, yTs, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
        "Optional - override the global output end (default=write all data)."),
        3, yTs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel output (column location in output, etc.)
    int yExcelOutput = -1;
    JPanel excelOutput_JPanel = new JPanel();
    excelOutput_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Output", excelOutput_JPanel );
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"Time series will be output in a block of cells with the upper left indicated by the address information."),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"The worksheet will be created if it does not exist."),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"It is recommended that the location of the Excel file be " +
		"specified using a path relative to the working directory."),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( ""),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
    	JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(excelOutput_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yExcelOutput, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Output (workbook) file:"),
		0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
	__OutputFile_JTextField.setToolTipText("Specify the Excel workbook file or use ${Property} notation");
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
        
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Append?:"),
		0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Append_JComboBox = new SimpleJComboBox ( false );
	List<String> appendChoices = new ArrayList<String>();
	appendChoices.add ( "" );	// Default
	appendChoices.add ( __command._False );
	appendChoices.add ( __command._True );
	__Append_JComboBox.setData(appendChoices);
	__Append_JComboBox.select ( 0 );
	__Append_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __Append_JComboBox,
		1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel(
		"Optional - whether to append to Excel file (default=" + __command._False + " or " + __command._True + " if open)."), 
		3, yExcelOutput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.setToolTipText("Specify the Excel worksheet name or use ${Property} notation");
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, __Worksheet_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel,
        new JLabel ("Optional - worksheet name (default=first sheet if appending to existing)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    __excelSpace_JTabbedPane = new JTabbedPane ();
    __excelSpace_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify the address for the upper-left corner of a block of cells in the Excel worksheet" ));
    JGUIUtil.addComponent(excelOutput_JPanel, __excelSpace_JTabbedPane,
        0, ++yExcelOutput, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JPanel address_JPanel = new JPanel();
    address_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Excel Address", address_JPanel );
    int yAddress = -1;
        
    JGUIUtil.addComponent(address_JPanel, new JLabel ("Excel address:"),
        0, ++yAddress, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelAddress_JTextField = new JTextField (20);
    __ExcelAddress_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(address_JPanel, __ExcelAddress_JTextField,
        1, yAddress, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(address_JPanel, new JLabel ("Excel cell block address in format A1, A1:B2, etc."),
        3, yAddress, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JPanel range_JPanel = new JPanel();
    range_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Named Range", range_JPanel );
    int yRange = -1;
    
    JGUIUtil.addComponent(range_JPanel, new JLabel ("Excel named range:"),
        0, ++yRange, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelNamedRange_JTextField = new JTextField (20);
    __ExcelNamedRange_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(range_JPanel, __ExcelNamedRange_JTextField,
        1, yRange, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(range_JPanel, new JLabel ("Excel named range."),
        3, yRange, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Table Name", table_JPanel );
    int yTable = -1;
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Excel table name:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelTableName_JTextField = new JTextField (20);
    __ExcelTableName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __ExcelTableName_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Excel table name."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
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
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Date/time column:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimeColumn_JTextField = new JTextField (10);
    __DateTimeColumn_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, __DateTimeColumn_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "Optional - name for date/time column (default=Date or DateTime)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // TODO SAM 2012-04-10 Evaluate whether the formatter should just be the first part of the format, which
    // is supported by the panel
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Date/time format:" ), 
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimeFormat_JPanel = new DateTimeFormatterSpecifiersJPanel ( 20, true, true, null, true, false );
    __DateTimeFormat_JPanel.addKeyListener ( this );
    __DateTimeFormat_JPanel.addFormatterTypeItemListener (this); // Respond to changes in formatter choice
    __DateTimeFormat_JPanel.getDocument().addDocumentListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __DateTimeFormat_JPanel,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel( "Optional - format string for data date/time formatter (default=ISO)."), 
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Date column:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateColumn_JTextField = new JTextField (10);
    __DateColumn_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, __DateColumn_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "Optional - name for date column (default=use date/time column only)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // TODO SAM 2012-04-10 Evaluate whether the formatter should just be the first part of the format, which
    // is supported by the panel
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Date format:" ), 
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateFormat_JPanel = new DateTimeFormatterSpecifiersJPanel ( 20, true, true, null, true, false );
    __DateFormat_JPanel.addKeyListener ( this );
    __DateFormat_JPanel.addFormatterTypeItemListener (this); // Respond to changes in formatter choice
    __DateFormat_JPanel.getDocument().addDocumentListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __DateFormat_JPanel,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel( "Optional - format string for date formatter (default=ISO)."), 
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Time column:"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TimeColumn_JTextField = new JTextField (10);
    __TimeColumn_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excelOutput_JPanel, __TimeColumn_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
        "Optional - name for time column (default=use date/time column only)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // TODO SAM 2012-04-10 Evaluate whether the formatter should just be the first part of the format, which
    // is supported by the panel
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ( "Time format:" ), 
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TimeFormat_JPanel = new DateTimeFormatterSpecifiersJPanel ( 20, true, true, null, true, false );
    __TimeFormat_JPanel.addKeyListener ( this );
    __TimeFormat_JPanel.addFormatterTypeItemListener (this); // Respond to changes in formatter choice
    __TimeFormat_JPanel.getDocument().addDocumentListener ( this );
    JGUIUtil.addComponent(excelOutput_JPanel, __TimeFormat_JPanel,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel( "Optional - format string for time formatter (default=ISO)."), 
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel("Value column(s):"),
        0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ValueColumns_JTextField = new TSFormatSpecifiersJPanel(30);
    __ValueColumns_JTextField.setToolTipText("Use %L for location, %T for data type, %I for interval, etc., " +
    	"%{ts:property} for time series property, ${property} for processor property.");
    __ValueColumns_JTextField.addKeyListener ( this );
    __ValueColumns_JTextField.getDocument().addDocumentListener(this);
    JGUIUtil.addComponent(excelOutput_JPanel, __ValueColumns_JTextField,
        1, yExcelOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Optional - %L for location, ${ts:property} for property (default=%L_%T)."),
        3, yExcelOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel output (location in output, etc.)
    int yComments = -1;
    JPanel comment_JPanel = new JPanel();
    comment_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Column and Cell Comments", comment_JPanel );
    
    JGUIUtil.addComponent(comment_JPanel, new JLabel (
		"Comments can be added to column headings and data value cells."),
		0, ++yComments, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JLabel (
		"Warning:  Using many comments can significantly increase the size of the Excel file."),
		0, ++yComments, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yComments, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

    JGUIUtil.addComponent(comment_JPanel, new JLabel("Author:"),
        0, ++yComments, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Author_JTextField = new JTextField(10);
    __Author_JTextField.addKeyListener ( this );
    __Author_JTextField.getDocument().addDocumentListener(this);
    JGUIUtil.addComponent(comment_JPanel, __Author_JTextField,
        1, yComments, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JLabel ("Optional - author for comments (default=none)."),
        3, yComments, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JTabbedPane __comments_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(comment_JPanel, __comments_JTabbedPane,
        0, ++yComments, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for column comments
    int yColComment = -1;
    JPanel colComment_JPanel = new JPanel();
    colComment_JPanel.setLayout( new GridBagLayout() );
    __comments_JTabbedPane.addTab ( "Column Heading Comments", colComment_JPanel );

    JGUIUtil.addComponent(colComment_JPanel, new JLabel (
		"For column headings, format the comment using the following specifiers:"),
		0, ++yColComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel (
		"   %L for location, %T for data type, %I for interval, etc. (using the format choices)"),
		0, ++yColComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel (
		"   ${ts:property} for time series property"),
		0, ++yColComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel (
		"   ${property} for processor property"),
		0, ++yColComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
		0, ++yColComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(colComment_JPanel, new JLabel("Column comment:"),
        0, ++yColComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnComment_JTextField = new TSFormatSpecifiersJPanel(30);
    __ColumnComment_JTextField.setToolTipText("Use %L for location, %T for data type, %I for interval, etc., " +
    	"${ts:property} for time series property, ${property} for processor property.");
    __ColumnComment_JTextField.addKeyListener ( this );
    __ColumnComment_JTextField.getDocument().addDocumentListener(this);
    JGUIUtil.addComponent(colComment_JPanel, __ColumnComment_JTextField,
        1, yColComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel ("Optional - see above for formatting options."),
        3, yColComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(colComment_JPanel, new JLabel("Column comment width (columns):"),
        0, ++yColComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnCommentWidth_JTextField = new JTextField(10);
    __ColumnCommentWidth_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(colComment_JPanel, __ColumnCommentWidth_JTextField,
        1, yColComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel ("Optional - comment width in columns (default=6)."),
        3, yColComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(colComment_JPanel, new JLabel("Column comment height (rows):"),
        0, ++yColComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnCommentHeight_JTextField = new JTextField(10);
    __ColumnCommentHeight_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(colComment_JPanel, __ColumnCommentHeight_JTextField,
        1, yColComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colComment_JPanel, new JLabel ("Optional - number of rows for comment (default=lines of comment)."),
        3, yColComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for cell value comments
    int yValComment = -1;
    JPanel valComment_JPanel = new JPanel();
    valComment_JPanel.setLayout( new GridBagLayout() );
    __comments_JTabbedPane.addTab ( "Value Comments", valComment_JPanel );
    
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
		"For data cells, format the comment using the following specifiers:"),
		0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
		"   %L for location, %T for data type, %I for interval, etc. (using the format choices)"),
		0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
		"   ${ts:property} for time series property"),
		0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
		"   ${property} for processor property"),
		0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
    	"   ${tsdata:datetime} - for date/time associated with data value"),
    	0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
    	"   ${tsdata:value} - for data value"),
    	0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel (
    	"   ${tsdata:flag} - for flag associated with data value"),
    	0, ++yValComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(valComment_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yValComment, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(valComment_JPanel, new JLabel("Value comment:"),
        0, ++yValComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ValueComment_JTextField = new TSFormatSpecifiersJPanel(30);
    __ValueComment_JTextField.setToolTipText("Use %L for location, %T for data type, %I for interval, etc., " +
    	"${ts:property} for time series property, ${property} for processor property.");
    __ValueComment_JTextField.addKeyListener ( this );
    __ValueComment_JTextField.getDocument().addDocumentListener(this);
    JGUIUtil.addComponent(valComment_JPanel, __ValueComment_JTextField,
        1, yValComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel ("Optional - see above for formatting options."),
        3, yValComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(valComment_JPanel, new JLabel( "Skip value comment if no flag?:"),
        0, ++yValComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SkipValueCommentIfNoFlag_JComboBox = new SimpleJComboBox ( false );
    __SkipValueCommentIfNoFlag_JComboBox.add("");
    __SkipValueCommentIfNoFlag_JComboBox.add(__command._False);
    __SkipValueCommentIfNoFlag_JComboBox.add(__command._True);
    __SkipValueCommentIfNoFlag_JComboBox.select ( 0 );
    __SkipValueCommentIfNoFlag_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(valComment_JPanel, __SkipValueCommentIfNoFlag_JComboBox,
        1, yValComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel ( "Optional - skip comment if no flag? (default=" + __command._True + ")."),
        3, yValComment, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(valComment_JPanel, new JLabel("Comment width (columns):"),
        0, ++yValComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CommentWidth_JTextField = new JTextField(10);
    __CommentWidth_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(valComment_JPanel, __CommentWidth_JTextField,
        1, yValComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel ("Optional - comment width in columns (default=6)."),
        3, yValComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(valComment_JPanel, new JLabel("Comment height (rows):"),
        0, ++yValComment, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CommentHeight_JTextField = new JTextField(10);
    __CommentHeight_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(valComment_JPanel, __CommentHeight_JTextField,
        1, yValComment, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(valComment_JPanel, new JLabel ("Optional - number of rows for comment (default=lines of comment)."),
        3, yValComment, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for column style formatting 
    int yColStyle = -1;
    JPanel colStyle_JPanel = new JPanel();
    colStyle_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Column Style Formatting", colStyle_JPanel );

    JGUIUtil.addComponent(colStyle_JPanel, new JLabel (
        "The following parameters control how Excel column heading cells are formatted, using a general style formatting approach."),
        0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel (
        "Style-based formatting requires as input a condition table to indicate how to evaluate column heading contents for style formatting."),
        0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel (
        "The condition table can use ${ts:property} notation to evaluate time series properties."),
        0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel (
        "A style table indicates the style properties to format a cell, such as the fill foreground color."),
        0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel (
        "Refer to the command documentation for details."),
        0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(colStyle_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	    0, ++yColStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel ( "Column condition table ID:" ), 
        0, ++yColStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnConditionTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __ColumnConditionTableID_JComboBox.setToolTipText("Select the condition table or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to give user a blank entry to type over
    __ColumnConditionTableID_JComboBox.setData ( tableIDChoices );
    __ColumnConditionTableID_JComboBox.addItemListener ( this );
    __ColumnConditionTableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(colStyle_JPanel, __ColumnConditionTableID_JComboBox,
        1, yColStyle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel( "Required when using styles - conditions to determine styles."), 
        3, yColStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel ( "Column style table ID:" ), 
        0, ++yColStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnStyleTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __ColumnStyleTableID_JComboBox.setToolTipText("Select the style table or use ${Property} notation");
    __ColumnStyleTableID_JComboBox.setData ( tableIDChoices );
    __ColumnStyleTableID_JComboBox.addItemListener ( this );
    __ColumnStyleTableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(colStyle_JPanel, __ColumnStyleTableID_JComboBox,
        1, yColStyle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(colStyle_JPanel, new JLabel( "Required when using styles - style definitions."), 
        3, yColStyle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for cell style formatting 
    int yStyle = -1;
    JPanel style_JPanel = new JPanel();
    style_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Cell Style Formatting", style_JPanel );

    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "The following parameters control how Excel data value cells are formatted, using a general style formatting approach."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "Style-based formatting requires as input a condition table to indicate how to evaluate data value cell contents for style formatting."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "The condition table can use ${ts:property}, ${tsdata:value}, and ${tsdata:flag} notation to evaluate time series properties and data values."),
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
        "Optional - worksheet for legend (default=same as for time series)."),
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
    checkGUIState();
	refresh();	// Sets the __path_JButton status
	setResizable (false);
    super.setVisible(true);
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e) {
	checkGUIState();
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
    String TSList = "";
    String TSID = "";
    String EnsembleID = "";
    String MissingValue = "";
	String Precision = "";
    String OutputStart = "";
    String OutputEnd = "";
    String OutputFile = "";
	String Append = "";
    String Worksheet = "";
	String ExcelAddress = "";
	String ExcelNamedRange = "";
	String ExcelTableName = "";
	String KeepOpen = "";
	String DateTimeColumn = "";
    String dateTimeFormatterType = "";
    String DateTimeFormat = "";
	String DateColumn = "";
    String DateFormatterType = "";
    String DateFormat = "";
	String TimeColumn = "";
    String TimeFormatterType = "";
    String TimeFormat = "";
	String ValueColumns = "";
	String Author = "";
	String ColumnComment = "";
	String ColumnCommentWidth = "";
	String ColumnCommentHeight = "";
	String ValueComment = "";
	String SkipValueCommentIfNoFlag = "";
	String CommentWidth = "";
	String CommentHeight = "";
	String ColumnStyleTableID = "";
	String ColumnConditionTableID = "";
	String StyleTableID = "";
	String ConditionTableID = "";
	String LegendWorksheet = "";
	String LegendAddress = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
	    TSList = props.getValue ( "TSList" );
	    TSID = props.getValue ( "TSID" );
	    EnsembleID = props.getValue ( "EnsembleID" );
        MissingValue = props.getValue("MissingValue");
		Precision = props.getValue ( "Precision" );
        OutputStart = props.getValue ( "OutputStart" );
        OutputEnd = props.getValue ( "OutputEnd" );
		OutputFile = props.getValue ( "OutputFile" );
		Append = props.getValue ( "Append" );
		Worksheet = props.getValue ( "Worksheet" );
		ExcelAddress = props.getValue ( "ExcelAddress" );
		ExcelNamedRange = props.getValue ( "ExcelNamedRange" );
		ExcelTableName = props.getValue ( "ExcelTableName" );
		KeepOpen = props.getValue ( "KeepOpen" );
		DateTimeColumn = props.getValue ( "DateTimeColumn" );
	    dateTimeFormatterType = props.getValue ( "DateTimeFormatterType" );
	    DateTimeFormat = props.getValue ( "DateTimeFormat" );
		DateColumn = props.getValue ( "DateColumn" );
	    DateFormatterType = props.getValue ( "DateFormatterType" );
	    DateFormat = props.getValue ( "DateFormat" );
		TimeColumn = props.getValue ( "TimeColumn" );
	    TimeFormatterType = props.getValue ( "TimeFormatterType" );
	    TimeFormat = props.getValue ( "TimeFormat" );
		ValueColumns = props.getValue ( "ValueColumns" );
		Author = props.getValue ( "Author" );
		ColumnComment = props.getValue ( "ColumnComment" );
		ColumnCommentWidth = props.getValue ( "ColumnCommentWidth" );
		ColumnCommentHeight = props.getValue ( "ColumnCommentHeight" );
		ValueComment = props.getValue ( "ValueComment" );
		SkipValueCommentIfNoFlag = props.getValue ( "SkipValueCommentIfNoFlag" );
		CommentWidth = props.getValue ( "CommentWidth" );
		CommentHeight = props.getValue ( "CommentHeight" );
		ColumnStyleTableID = props.getValue ( "ColumnStyleTableID" );
		ColumnConditionTableID = props.getValue ( "ColumnConditionTableID" );
		StyleTableID = props.getValue ( "StyleTableID" );
		ConditionTableID = props.getValue ( "ConditionTableID" );
		LegendWorksheet = props.getValue ( "LegendWorksheet" );
		LegendAddress = props.getValue ( "LegendAddress" );
        if ( TSList == null ) {
            // Select default...
            __TSList_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TSList_JComboBox,TSList, JGUIUtil.NONE, null, null ) ) {
                __TSList_JComboBox.select ( TSList );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTSList value \"" + TSList +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem( __TSID_JComboBox, TSID, JGUIUtil.NONE, null, null ) ) {
                __TSID_JComboBox.select ( TSID );
        }
        else {
            // Automatically add to the list after the blank...
            if ( (TSID != null) && (TSID.length() > 0) ) {
                __TSID_JComboBox.insertItemAt ( TSID, 1 );
                // Select...
                __TSID_JComboBox.select ( TSID );
            }
            else {
                // Select the blank...
                __TSID_JComboBox.select ( 0 );
            }
        }
        if ( EnsembleID == null ) {
            // Select default...
            __EnsembleID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __EnsembleID_JComboBox,EnsembleID, JGUIUtil.NONE, null, null ) ) {
                __EnsembleID_JComboBox.select ( EnsembleID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nEnsembleID value \"" + EnsembleID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( MissingValue != null ) {
            __MissingValue_JTextField.setText ( MissingValue );
        }
        if ( Precision != null ) {
            __Precision_JTextField.setText ( Precision );
        }
        if ( OutputStart != null ) {
            __OutputStart_JTextField.setText (OutputStart);
        }
        if ( OutputEnd != null ) {
            __OutputEnd_JTextField.setText (OutputEnd);
        }
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
		}
		if ( JGUIUtil.isSimpleJComboBoxItem(__Append_JComboBox, Append,JGUIUtil.NONE, null, null ) ) {
			__Append_JComboBox.select ( Append );
		}
		else {
            if ( (Append == null) || Append.equals("") ) {
				// New command...select the default...
				__Append_JComboBox.select ( 0 );
			}
			else {	// Bad user command...
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nAppend parameter \"" +	Append + "\".  Select a\n value or Cancel." );
			}
		}
        if ( Worksheet != null ) {
            __Worksheet_JTextField.setText ( Worksheet );
        }
		if ( ExcelAddress != null ) {
			__ExcelAddress_JTextField.setText ( ExcelAddress );
			// Also select the tab to be visible
			__excelSpace_JTabbedPane.setSelectedIndex(0);
		}
		if ( ExcelNamedRange != null ) {
			__ExcelNamedRange_JTextField.setText ( ExcelNamedRange );
			__excelSpace_JTabbedPane.setSelectedIndex(1);
		}
        if ( ExcelTableName != null ) {
            __ExcelTableName_JTextField.setText ( ExcelTableName );
            __excelSpace_JTabbedPane.setSelectedIndex(2);
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
        if (DateTimeColumn != null) {
            __DateTimeColumn_JTextField.setText(DateTimeColumn);
        }
        if ( (dateTimeFormatterType == null) || dateTimeFormatterType.equals("") ) {
            // Select default...
            __DateTimeFormat_JPanel.selectFormatterType(null);
        }
        else {
            try {
                __DateTimeFormat_JPanel.selectFormatterType(DateTimeFormatterType.valueOfIgnoreCase(dateTimeFormatterType));
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nDateTimeFormatterType value \"" + dateTimeFormatterType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( DateTimeFormat != null ) {
            __DateTimeFormat_JPanel.setText ( DateTimeFormat );
        }
        if (DateColumn != null) {
            __DateColumn_JTextField.setText(DateColumn);
        }
        if ( (DateFormatterType == null) || DateFormatterType.equals("") ) {
            // Select default...
            __DateFormat_JPanel.selectFormatterType(null);
        }
        else {
            try {
                __DateFormat_JPanel.selectFormatterType(DateTimeFormatterType.valueOfIgnoreCase(DateFormatterType));
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nDateFormatterType value \"" + DateFormatterType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( DateFormat != null ) {
            __DateFormat_JPanel.setText ( DateFormat );
        }
        if (TimeColumn != null) {
            __TimeColumn_JTextField.setText(DateTimeColumn);
        }
        if ( (TimeFormatterType == null) || TimeFormatterType.equals("") ) {
            // Select default...
            __TimeFormat_JPanel.selectFormatterType(null);
        }
        else {
            try {
                __TimeFormat_JPanel.selectFormatterType(DateTimeFormatterType.valueOfIgnoreCase(TimeFormatterType));
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTimeFormatterType value \"" + TimeFormatterType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( TimeFormat != null ) {
            __TimeFormat_JPanel.setText ( TimeFormat );
        }
        if (ValueColumns != null) {
            __ValueColumns_JTextField.setText(ValueColumns);
        }
        if (Author != null) {
            __Author_JTextField.setText(Author);
        }
        if (ColumnComment != null) {
            __ColumnComment_JTextField.setText(ColumnComment);
        }
        if (ValueComment!= null) {
            __ValueComment_JTextField.setText(ValueComment);
        }
        if ( ColumnCommentWidth != null ) {
            __ColumnCommentWidth_JTextField.setText ( ColumnCommentWidth );
        }
        if ( ColumnCommentHeight != null ) {
            __ColumnCommentHeight_JTextField.setText ( ColumnCommentHeight );
        }
        if ( SkipValueCommentIfNoFlag == null || SkipValueCommentIfNoFlag.equals("") ) {
            // Select a default...
            __SkipValueCommentIfNoFlag_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __SkipValueCommentIfNoFlag_JComboBox, SkipValueCommentIfNoFlag, JGUIUtil.NONE, null, null ) ) {
                __SkipValueCommentIfNoFlag_JComboBox.select ( SkipValueCommentIfNoFlag );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nSkipValueCommentIfNoFlag \"" +
                    SkipValueCommentIfNoFlag + "\".  Select a different choice or Cancel." );
            }
        }
        if ( CommentWidth != null ) {
            __CommentWidth_JTextField.setText ( CommentWidth );
        }
        if ( CommentHeight != null ) {
            __CommentHeight_JTextField.setText ( CommentHeight );
        }
        if ( ColumnConditionTableID == null ) {
            // Select default...
        	__ColumnConditionTableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ColumnConditionTableID_JComboBox,ColumnConditionTableID, JGUIUtil.NONE, null, null ) ) {
                __ColumnConditionTableID_JComboBox.select ( ColumnConditionTableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nColumnConditionTableID value \"" + ColumnConditionTableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( ColumnStyleTableID == null ) {
            // Select default...
        	__ColumnStyleTableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ColumnStyleTableID_JComboBox,ColumnStyleTableID, JGUIUtil.NONE, null, null ) ) {
                __ColumnStyleTableID_JComboBox.select ( ColumnStyleTableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nColumnStyleTableID value \"" + ColumnStyleTableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
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
	TSList = __TSList_JComboBox.getSelected();
    TSID = __TSID_JComboBox.getSelected();
    EnsembleID = __EnsembleID_JComboBox.getSelected();
    MissingValue = __MissingValue_JTextField.getText().trim();
	Precision = __Precision_JTextField.getText().trim();
    OutputStart = __OutputStart_JTextField.getText().trim();
    OutputEnd = __OutputEnd_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
	Append = __Append_JComboBox.getSelected();
	Worksheet = __Worksheet_JTextField.getText().trim();
	ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	DateTimeColumn = __DateTimeColumn_JTextField.getText().trim();
    dateTimeFormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
    DateTimeFormat = __DateTimeFormat_JPanel.getText().trim();
	DateColumn = __DateColumn_JTextField.getText().trim();
    DateFormatterType = __DateFormat_JPanel.getSelectedFormatterType().trim();
    DateFormat = __DateFormat_JPanel.getText().trim();
	TimeColumn = __TimeColumn_JTextField.getText().trim();
    TimeFormatterType = __TimeFormat_JPanel.getSelectedFormatterType().trim();
    TimeFormat = __TimeFormat_JPanel.getText().trim();
	ValueColumns = __ValueColumns_JTextField.getText().trim();
	Author = __Author_JTextField.getText().trim();
	ColumnComment = __ColumnComment_JTextField.getText().trim();
	ColumnCommentWidth = __ColumnCommentWidth_JTextField.getText().trim();
	ColumnCommentHeight = __ColumnCommentHeight_JTextField.getText().trim();
	ValueComment = __ValueComment_JTextField.getText().trim();
	SkipValueCommentIfNoFlag = __SkipValueCommentIfNoFlag_JComboBox.getSelected();
	CommentWidth = __CommentWidth_JTextField.getText().trim();
	CommentHeight = __CommentHeight_JTextField.getText().trim();
	ColumnStyleTableID = __ColumnStyleTableID_JComboBox.getSelected();
	ColumnConditionTableID = __ColumnConditionTableID_JComboBox.getSelected();
	StyleTableID = __StyleTableID_JComboBox.getSelected();
	ConditionTableID = __ConditionTableID_JComboBox.getSelected();
	LegendWorksheet = __LegendWorksheet_JTextField.getText().trim();
	LegendAddress = __LegendAddress_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "TSList=" + TSList );
    props.add ( "TSID=" + TSID );
    props.add ( "EnsembleID=" + EnsembleID );
	props.add ( "MissingValue=" + MissingValue );
	props.add ( "Precision=" + Precision );
	props.add ( "OutputStart=" + OutputStart );
	props.add ( "OutputEnd=" + OutputEnd );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "Append=" + Append );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "ExcelAddress=" + ExcelAddress );
	props.add ( "ExcelNamedRange=" + ExcelNamedRange );
	props.add ( "ExcelTableName=" + ExcelTableName );
	props.add ( "KeepOpen=" + KeepOpen );
	props.add ( "DateTimeColumn=" + DateTimeColumn );
	props.add ( "DateTimeFormatterType=" + dateTimeFormatterType );
	props.add ( "DateTimeFormat=" + DateTimeFormat );
	props.add ( "DateColumn=" + DateColumn );
	props.add ( "DateFormatterType=" + DateFormatterType );
	props.add ( "DateFormat=" + DateFormat );
	props.add ( "TimeColumn=" + TimeColumn );
	props.add ( "TimeFormatterType=" + TimeFormatterType );
	props.add ( "TimeFormat=" + TimeFormat );
	props.add ( "ValueColumns=" + ValueColumns );
	props.add ( "Author=" + Author );
	props.add ( "ColumnComment=" + ColumnComment );
	props.add ( "ColumnCommentWidth=" + ColumnCommentWidth );
	props.add ( "ColumnCommentHeight=" + ColumnCommentHeight );
	props.add ( "ValueComment=" + ValueComment );
	props.add ( "SkipValueCommentIfNoFlag=" + SkipValueCommentIfNoFlag );
	props.add ( "CommentWidth=" + CommentWidth );
	props.add ( "CommentHeight=" + CommentHeight );
	props.add ( "ColumnStyleTableID=" + ColumnStyleTableID );
	props.add ( "ColumnConditionTableID=" + ColumnConditionTableID );
	props.add ( "StyleTableID=" + StyleTableID );
	props.add ( "ConditionTableID=" + ConditionTableID );
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
