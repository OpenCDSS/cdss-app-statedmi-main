// WriteTimeSeriesToExcelBlock_JDialog - Editor for the WriteTimeSeriesToExcelFormatted command.

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
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.YearType;

/**
Editor for the WriteTimeSeriesToExcelFormatted command.
*/
@SuppressWarnings("serial")
public class WriteTimeSeriesToExcelBlock_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __TSList_JComboBox = null;
private JLabel __TSID_JLabel = null;
private SimpleJComboBox __TSID_JComboBox = null;
private JLabel __EnsembleID_JLabel = null;
private SimpleJComboBox __EnsembleID_JComboBox = null;
private JTextField __MissingValue_JTextField = null;// Missing value for output
private JTextField __Precision_JTextField = null;
private JTextField __OutputStart_JTextField = null;
private JTextField __OutputEnd_JTextField = null;

private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __Append_JComboBox = null;
private JTextField __Worksheet_JTextField = null;
private JTabbedPane __main_JTabbedPane = null;
private JTabbedPane __excelSpace_JTabbedPane = null;
private JTextField __ExcelAddress_JTextField = null;
private JTextField __ExcelNamedRange_JTextField = null;
private JTextField __ExcelTableName_JTextField = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;

private SimpleJComboBox __LayoutBlock_JComboBox = null;
private SimpleJComboBox __LayoutRows_JComboBox = null;
private SimpleJComboBox __LayoutColumns_JComboBox = null;
private SimpleJComboBox __OutputYearType_JComboBox = null;
private JTextArea __ColumnLabels_JTextArea = null;
private JTextArea __RowLabels_JTextArea = null;
private JTextArea __ColumnStatistics_JTextArea = null;
private JTextArea __RowStatistics_JTextArea = null;
private JTextArea __ColumnStatisticFormulas_JTextArea = null;
private JTextArea __RowStatisticFormulas_JTextArea = null;

private JTextArea __ValueComment_JTextArea = null;

private SimpleJComboBox __StyleTableID_JComboBox = null;
private SimpleJComboBox __ConditionTableID_JComboBox = null;
private JTextField __BlockMinColumnProperty_JTextField = null;
private JTextField __BlockMinRowProperty_JTextField = null;
private JTextField __BlockMaxColumnProperty_JTextField = null;
private JTextField __BlockMaxRowProperty_JTextField = null;
private JTextField __LegendWorksheet_JTextField = null;
private JTextField __LegendAddress_JTextField = null;
//TODO SAM 2015-07-11 Enable notes that allow processor and time series properties
//private JTextField __NotesAddress_JTextField = null;
//private JTextField __Notes_JTextField = null;

private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private WriteTimeSeriesToExcelBlock_Command __command = null;
private boolean __ok = false;
//private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTimeSeriesToExcelBlock_JDialog ( JFrame parent, WriteTimeSeriesToExcelBlock_Command command, List<String> tableIDChoices )
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

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
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
		HelpViewer.getInstance().showHelp("command", "WriteTimeSeriesToExcelBlock");
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
	String MissingValue = __MissingValue_JTextField.getText().trim();
	String Precision  = __Precision_JTextField.getText().trim();
    String OutputStart = __OutputStart_JTextField.getText().trim();
    String OutputEnd = __OutputEnd_JTextField.getText().trim();
    String EnsembleID = __EnsembleID_JComboBox.getSelected();
    
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String Append = __Append_JComboBox.getSelected();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String ExcelAddress = __ExcelAddress_JTextField.getText().trim();
	String ExcelNamedRange = __ExcelNamedRange_JTextField.getText().trim();
	String ExcelTableName = __ExcelTableName_JTextField.getText().trim();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	
	String LayoutBlock = __LayoutBlock_JComboBox.getSelected();
	String LayoutColumns = __LayoutColumns_JComboBox.getSelected();
	String LayoutRows = __LayoutRows_JComboBox.getSelected();
	String OutputYearType = __OutputYearType_JComboBox.getSelected();
	String BlockMinColumnProperty = __BlockMinColumnProperty_JTextField.getText().trim();
	String BlockMinRowProperty = __BlockMinRowProperty_JTextField.getText().trim();
	String BlockMaxColumnProperty = __BlockMaxColumnProperty_JTextField.getText().trim();
	String BlockMaxRowProperty = __BlockMaxRowProperty_JTextField.getText().trim();
	
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
    if ( LayoutBlock.length() > 0 ) {
        props.set ( "LayoutBlock", LayoutBlock );
    }
    if ( LayoutColumns.length() > 0 ) {
        props.set ( "LayoutColumns", LayoutColumns );
    }
    if ( LayoutRows.length() > 0 ) {
        props.set ( "LayoutRows", LayoutRows );
    }
    if ( OutputYearType.length() > 0 ) {
        props.set ( "OutputYearType", OutputYearType );
    }
    if ( BlockMinColumnProperty.length() > 0 ) {
        props.set ( "BlockMinColumnProperty", BlockMinColumnProperty );
    }
    if ( BlockMinRowProperty.length() > 0 ) {
        props.set ( "BlockMinRowProperty", BlockMinRowProperty );
    }
    if ( BlockMaxColumnProperty.length() > 0 ) {
        props.set ( "BlockMaxColumnProperty", BlockMaxColumnProperty );
    }
    if ( BlockMaxRowProperty.length() > 0 ) {
        props.set ( "BlockMaxRowProperty", BlockMaxRowProperty );
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
	String LayoutBlock = __LayoutBlock_JComboBox.getSelected();
	String LayoutColumns = __LayoutColumns_JComboBox.getSelected();
	String LayoutRows = __LayoutRows_JComboBox.getSelected();
	String OutputYearType = __OutputYearType_JComboBox.getSelected();
	String BlockMinColumnProperty = __BlockMinColumnProperty_JTextField.getText().trim();
	String BlockMinRowProperty = __BlockMinRowProperty_JTextField.getText().trim();
	String BlockMaxColumnProperty = __BlockMaxColumnProperty_JTextField.getText().trim();
	String BlockMaxRowProperty = __BlockMaxRowProperty_JTextField.getText().trim();
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
	__command.setCommandParameter ( "LayoutBlock", LayoutBlock );
	__command.setCommandParameter ( "LayoutColumns", LayoutColumns );
	__command.setCommandParameter ( "LayoutRows", LayoutRows );
	__command.setCommandParameter ( "OutputYearType", OutputYearType );
	__command.setCommandParameter ( "BlockMinColumnProperty", BlockMinColumnProperty );
	__command.setCommandParameter ( "BlockMinRowProperty", BlockMinRowProperty );
	__command.setCommandParameter ( "BlockMaxColumnProperty", BlockMaxColumnProperty );
	__command.setCommandParameter ( "BlockMaxRowProperty", BlockMaxRowProperty );
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
private void initialize ( JFrame parent, WriteTimeSeriesToExcelBlock_Command command, List<String> tableIDChoices )
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
    	"<html><b>This command is under development.  Currently only daily and monthly time series can "
    	+ "be processed and parameter support is limited until features are enabled.</b></html>"),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
    	"This command writes a list of time series to a worksheet in a Microsoft Excel workbook file (*.xls, *.xlsx), in blocks of rows and columns."),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"See the WriteTimeSeriesToExcel() command to output multiple time series as columns of data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for time series
    int yTs = -1;
    JPanel ts_JPanel = new JPanel();
    ts_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Time Series to Write", ts_JPanel );
    
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
		"Specify the time series to output.  Each time series will be positioned in the worksheet as per the \"Excel Output\" and \"Layout\" tabs."),
		0, ++yTs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
		"<html><b>Currently only the first time series is processed.</b></html>"),
		0, ++yTs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(ts_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yTs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __TSList_JComboBox = new SimpleJComboBox(false);
    yTs = CommandEditorUtil.addTSListToEditorDialogPanel ( this, ts_JPanel, __TSList_JComboBox, ++yTs );

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
    __MissingValue_JTextField.setToolTipText("Specify Blank to output a blank.");
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
        0, ++yTs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputEnd_JTextField = new JTextField (20);
    __OutputEnd_JTextField.setToolTipText("Specify the output end using a date/time string or ${Property} notation");
    __OutputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(ts_JPanel, __OutputEnd_JTextField,
        1, yTs, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(ts_JPanel, new JLabel (
        "Optional - override the global output end (default=write all data)."),
        3, yTs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Excel output (location in output, etc.)
    int yExcelOutput = -1;
    JPanel excelOutput_JPanel = new JPanel();
    excelOutput_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel Output", excelOutput_JPanel );

    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"<html><b>Currently a single time series can be output.  In the future features will be enabled to match each time series with a worksheet or subset.</b></html>"),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"Each time series will be output in a block of cells with the upper left indicated by the address information."),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel (
		"The worksheet will be created if it does not exist.  Time series properties can be used to specify the worksheet name using syntax ${Property}."),
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
	JGUIUtil.addComponent(excelOutput_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yExcelOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelOutput_JPanel, new JLabel ("Output (workbook) file:"),
		0, ++yExcelOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (45);
	__OutputFile_JTextField.setToolTipText("Specify the Excel workbook file or use ${Property} notation");
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
	JGUIUtil.addComponent(excelOutput_JPanel, OutputFile_JPanel,
		1, yExcelOutput, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        
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
        new JLabel ("Optional - worksheet name (default=first sheet)."),
        3, yExcelOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    __excelSpace_JTabbedPane = new JTabbedPane ();
    __excelSpace_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify the address for a contigous block of cells the in Excel worksheet" ));
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
    
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout(new GridBagLayout());
    __excelSpace_JTabbedPane.addTab ( "by Table Name", table_JPanel );
    int yTable = -1;
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Excel table name:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcelTableName_JTextField = new JTextField (10);
    __ExcelTableName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __ExcelTableName_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
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
   
    // Panel for Layout
    int yLayout = 0;
    JPanel layout_JPanel = new JPanel();
    layout_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Layout", layout_JPanel );
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
		"Each time series is formatted by specifying a layout block (chunk of data) and then the layout within that block of data by columns and rows."),
		0, ++yLayout, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
		"See the \"Style Formatting\" tab for features to format cells based on time series data values and flags."),
		0, ++yLayout, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
		"<html><b>February 29 of non-leap years in daily and smaller interval time series is set to blank (more options will be added later).</b></html>"),
		0, ++yLayout, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(layout_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yLayout, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel( "Layout block:"),
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LayoutBlock_JComboBox = new SimpleJComboBox ( false );
    __LayoutBlock_JComboBox.add(__command._Period);
    //__LayoutBlock_JComboBox.add(__command._Year); // TODO SAM 2015-03-10 Need to enable
    __LayoutBlock_JComboBox.select ( 0 );
    __LayoutBlock_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(layout_JPanel, __LayoutBlock_JComboBox,
        1, yLayout, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Required - output block content."),
        3, yLayout, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel( "Layout columns:"),
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LayoutColumns_JComboBox = new SimpleJComboBox ( false );
    //__LayoutColumns_JComboBox.add("" + TimeInterval.getName(TimeInterval.YEAR)); // TODO SAM 2015-03-10 Need to enable
    __LayoutColumns_JComboBox.add("" + TimeInterval.getName(TimeInterval.MONTH,1));
    __LayoutColumns_JComboBox.add("" + TimeInterval.getName(TimeInterval.DAY,1));
    __LayoutColumns_JComboBox.select ( 0 );
    __LayoutColumns_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(layout_JPanel, __LayoutColumns_JComboBox,
        1, yLayout, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Required - time slice of each column."),
        3, yLayout, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel( "Layout rows:"),
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LayoutRows_JComboBox = new SimpleJComboBox ( false );
    //__LayoutRows_JComboBox.add("YearAscending"); // TODO SAM 2015-03-10 need to enable
    __LayoutRows_JComboBox.add("YearDescending");
    //__LayoutRows_JComboBox.add("" + TimeInterval.getName(TimeInterval.MONTH)); // TODO SAM 2015-03-10 need to enable
    //__LayoutRows_JComboBox.add("" + TimeInterval.getName(TimeInterval.DAY)); // TODO SAM 2015-03-10 need to enable
    __LayoutRows_JComboBox.select ( 0 );
    __LayoutRows_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(layout_JPanel, __LayoutRows_JComboBox,
        1, yLayout, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Required - time slice of each row."),
        3, yLayout, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Output year type:" ), 
		0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputYearType_JComboBox = new SimpleJComboBox ( false );
	// Only include types that have been tested for all output.  More specific types may be included in
	// some commands where local handling is enabled.
	__OutputYearType_JComboBox.add ( "" );
    __OutputYearType_JComboBox.add ( "" + YearType.CALENDAR );
    __OutputYearType_JComboBox.add ( "" + YearType.NOV_TO_OCT );
    __OutputYearType_JComboBox.add ( "" + YearType.WATER );
	__OutputYearType_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(layout_JPanel, __OutputYearType_JComboBox,
		1, yLayout, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Optional - output year type (default=" + YearType.CALENDAR + ")."),
        3, yLayout, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Block minimum column property:"), 
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __BlockMinColumnProperty_JTextField = new JTextField (20);
    __BlockMinColumnProperty_JTextField.setToolTipText("Integer property will be set when command runs - use to position legend, etc.");
    __BlockMinColumnProperty_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(layout_JPanel, __BlockMinColumnProperty_JTextField,
        1, yLayout, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
        "Optional - minimum output column (default=no property set)."),
        3, yLayout, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Block minimum row property:"), 
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __BlockMinRowProperty_JTextField = new JTextField (20);
    __BlockMinRowProperty_JTextField.setToolTipText("Integer property will be set when command runs - use to position legend, etc.");
    __BlockMinRowProperty_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(layout_JPanel, __BlockMinRowProperty_JTextField,
        1, yLayout, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
        "Optional - minimum output row (default=no property set)."),
        3, yLayout, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Block maximum column property:"), 
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __BlockMaxColumnProperty_JTextField = new JTextField (20);
    __BlockMaxColumnProperty_JTextField.setToolTipText("Integer property will be set when command runs - use to position legend, etc.");
    __BlockMaxColumnProperty_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(layout_JPanel, __BlockMaxColumnProperty_JTextField,
        1, yLayout, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
        "Optional - maximum output column (default=no property set)."),
        3, yLayout, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(layout_JPanel, new JLabel ( "Block maximum row property:"), 
        0, ++yLayout, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __BlockMaxRowProperty_JTextField = new JTextField (20);
    __BlockMaxRowProperty_JTextField.setToolTipText("Integer property will be set when command runs - use to position legend, etc.");
    __BlockMaxRowProperty_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(layout_JPanel, __BlockMaxRowProperty_JTextField,
        1, yLayout, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(layout_JPanel, new JLabel (
        "Optional - maximum output row (default=no property set)."),
        3, yLayout, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    //TODO SAM 2015-07-10 Evaluate enabling - for now default
    //JGUIUtil.addComponent(layout_JPanel, new JLabel ("Column labels:"),
    //    0, ++yLayout, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnLabels_JTextArea = new JTextArea (3,35);
    __ColumnLabels_JTextArea.setEnabled(false); // TODO SAM 2015-03-10 need to enable
    __ColumnLabels_JTextArea.setLineWrap ( true );
    __ColumnLabels_JTextArea.setWrapStyleWord ( true );
    __ColumnLabels_JTextArea.setToolTipText("TableColumn:DatastoreColumn,TableColumn:DataStoreColumn");
    __ColumnLabels_JTextArea.addKeyListener (this);
    __ColumnLabels_JTextArea.setVisible(false);
    //JGUIUtil.addComponent(layout_JPanel, new JScrollPane(__ColumnLabels_JTextArea),
    //    1, yLayout, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(layout_JPanel, new JLabel ("Optional - format for column labels."),
    //    3, yLayout, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    //JGUIUtil.addComponent(layout_JPanel, new SimpleJButton ("Edit","EditLayoutColumnLabels",this),
    //    3, ++yLayout, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    //JGUIUtil.addComponent(layout_JPanel, new JLabel ("Row labels:"),
    //    0, ++yLayout, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowLabels_JTextArea = new JTextArea (3,35);
    __RowLabels_JTextArea.setEnabled(false); // TODO SAM 2015-03-10 need to enable
    __RowLabels_JTextArea.setLineWrap ( true );
    __RowLabels_JTextArea.setWrapStyleWord ( true );
    __RowLabels_JTextArea.setToolTipText("TableColumn:DatastoreColumn,TableColumn:DataStoreColumn");
    __RowLabels_JTextArea.addKeyListener (this);
    __RowLabels_JTextArea.setVisible(false);
    //JGUIUtil.addComponent(layout_JPanel, new JScrollPane(__RowLabels_JTextArea),
    //    1, yLayout, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    //JGUIUtil.addComponent(layout_JPanel, new JLabel ("Optional - format for row labels."),
    //    3, yLayout, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    //JGUIUtil.addComponent(layout_JPanel, new SimpleJButton ("Edit","EditLayoutRowLabels",this),
    //    3, ++yLayout, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for statistics
    int yStats = 0;
    JPanel stats_JPanel = new JPanel();
    stats_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Statistics", stats_JPanel );

    JGUIUtil.addComponent(stats_JPanel, new JLabel (
		"<html><b>Statistics features are currently not enabled.</b></html>"),
		0, ++yStats, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(stats_JPanel, new JLabel (
		"The block of data can optionally also have statistics on the right-most columns or bottom-most rows."),
		0, ++yStats, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(stats_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yStats, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Column statistics:"),
        0, ++yStats, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnStatistics_JTextArea = new JTextArea (3,35);
    __ColumnStatistics_JTextArea.setLineWrap ( true );
    __ColumnStatistics_JTextArea.setWrapStyleWord ( true );
    __ColumnStatistics_JTextArea.setToolTipText("Label1:Statistic1,Label2:Statistic2,...");
    __ColumnStatistics_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(stats_JPanel, new JScrollPane(__ColumnStatistics_JTextArea),
        1, yStats, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Optional - built-in statistics to include at bottom of each column."),
        3, yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(stats_JPanel, new SimpleJButton ("Edit","EditColumnStatistics",this),
        3, ++yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Column statistic formulas:"),
        0, ++yStats, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnStatisticFormulas_JTextArea = new JTextArea (3,35);
    __ColumnStatisticFormulas_JTextArea.setLineWrap ( true );
    __ColumnStatisticFormulas_JTextArea.setWrapStyleWord ( true );
    __ColumnStatisticFormulas_JTextArea.setToolTipText("Label1:Formula1,Label2:Formula2,...");
    __ColumnStatisticFormulas_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(stats_JPanel, new JScrollPane(__ColumnStatisticFormulas_JTextArea),
        1, yStats, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Optional - statistic formulas to include at bottom of each column."),
        3, yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(stats_JPanel, new SimpleJButton ("Edit","EditColumnStatisticFormulas",this),
        3, ++yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Row statistics:"),
        0, ++yStats, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowStatistics_JTextArea = new JTextArea (3,35);
    __RowStatistics_JTextArea.setLineWrap ( true );
    __RowStatistics_JTextArea.setWrapStyleWord ( true );
    __RowStatistics_JTextArea.setToolTipText("Label1:Statistic1,Label2:Statistic2,...");
    __RowStatistics_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(stats_JPanel, new JScrollPane(__RowStatistics_JTextArea),
        1, yStats, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Optional - built-in statistics to include to right of each row."),
        3, yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(stats_JPanel, new SimpleJButton ("Edit","EditColumnStatistics",this),
        3, ++yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Row statistic formulas:"),
        0, ++yStats, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowStatisticFormulas_JTextArea = new JTextArea (3,35);
    __RowStatisticFormulas_JTextArea.setLineWrap ( true );
    __RowStatisticFormulas_JTextArea.setWrapStyleWord ( true );
    __RowStatisticFormulas_JTextArea.setToolTipText("Label1:Formula1,Label2:Formula2,...");
    __RowStatisticFormulas_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(stats_JPanel, new JScrollPane(__RowStatisticFormulas_JTextArea),
        1, yStats, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(stats_JPanel, new JLabel ("Optional - statistic formulas to include to right of each row."),
        3, yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(stats_JPanel, new SimpleJButton ("Edit","EditRowStatisticFormulas",this),
        3, ++yStats, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for Data Flags
    int yComment = 0;
    JPanel comment_JPanel = new JPanel();
    comment_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Cell Comments", comment_JPanel );

    JGUIUtil.addComponent(comment_JPanel, new JLabel (
		"<html><b>Cell comment formatting based on time series values, data flags, and properties currently is not enabled.</b></html>"),
		0, ++yComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JLabel (
		"In the future features may be enabled similar to the WriteTimeSeriesToExcel() command."),
		0, ++yComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JLabel (
		"Use the \"Cell Style Formatting\" tab to format cells based on a condition evaluation."),
		0, ++yComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(comment_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yComment, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(comment_JPanel, new JLabel ("Data flag cell comment:"),
        0, ++yComment, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ValueComment_JTextArea = new JTextArea (3,35);
    __ValueComment_JTextArea.setLineWrap ( true );
    __ValueComment_JTextArea.setWrapStyleWord ( true );
    __ValueComment_JTextArea.setToolTipText("Flag1Pattern:Comment1,Flag2Pattern:Comment2...");
    __ValueComment_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(comment_JPanel, new JScrollPane(__ValueComment_JTextArea),
        1, yComment, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(comment_JPanel, new JLabel ("Optional - cell comment for data flag."),
        3, yComment, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(comment_JPanel, new SimpleJButton ("Edit","EditDataFlagCellComment",this),
        3, ++yComment, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
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
    JGUIUtil.addComponent(style_JPanel, new JLabel (
        "The position for the legend can be specified using A1 address, named range, or R[${Property}+N]C[${Property}+N], where N is an offset of the property value."),
        0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(style_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
   	    0, ++yStyle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(style_JPanel, new JLabel ( "Condition table ID:" ), 
        0, ++yStyle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ConditionTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit since table may not be available
    __ConditionTableID_JComboBox.setToolTipText("Select the condition table or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to give user a blank entry to type over
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
	String LayoutBlock = "";
	String LayoutColumns = "";
	String LayoutRows = "";
	String OutputYearType = "";
	String BlockMinColumnProperty = "";
	String BlockMinRowProperty = "";
	String BlockMaxColumnProperty = "";
	String BlockMaxRowProperty = "";
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
		LayoutBlock = props.getValue ( "LayoutBlock" );
		LayoutColumns = props.getValue ( "LayoutColumns" );
		LayoutRows = props.getValue ( "LayoutRows" );
        OutputYearType = props.getValue ( "OutputYearType" );
        BlockMinColumnProperty = props.getValue ( "BlockMinColumnProperty" );
        BlockMinRowProperty = props.getValue ( "BlockMinRowProperty" );
        BlockMaxColumnProperty = props.getValue ( "BlockMaxColumnProperty" );
        BlockMaxRowProperty = props.getValue ( "BlockMaxRowProperty" );
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
            if ( (Append == null) ||	Append.equals("") ) {
				// New command...select the default...
				__Append_JComboBox.select ( 0 );
			}
			else {	// Bad user command...
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\n"+
				"IfNotFound parameter \"" +	Append +
				"\".  Select a\n value or Cancel." );
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
        if ( LayoutBlock == null ) {
            // Select default...
            __LayoutBlock_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __LayoutBlock_JComboBox,LayoutBlock, JGUIUtil.NONE, null, null ) ) {
                __LayoutBlock_JComboBox.select ( LayoutBlock );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nLayoutBlock value \"" + LayoutBlock +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( LayoutColumns == null ) {
            // Select default...
            __LayoutColumns_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __LayoutColumns_JComboBox,LayoutColumns, JGUIUtil.NONE, null, null ) ) {
                __LayoutColumns_JComboBox.select ( LayoutColumns );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nLayoutColumns value \"" + LayoutColumns +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( LayoutRows == null ) {
            // Select default...
            __LayoutRows_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __LayoutRows_JComboBox,LayoutRows, JGUIUtil.NONE, null, null ) ) {
                __LayoutRows_JComboBox.select ( LayoutRows );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nLayoutRows value \"" + LayoutRows +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( OutputYearType == null ) {
            // Select default...
            __OutputYearType_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputYearType_JComboBox,OutputYearType, JGUIUtil.NONE, null, null ) ) {
                __OutputYearType_JComboBox.select ( OutputYearType );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nOutputYearType value \"" + OutputYearType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
		if ( BlockMinColumnProperty != null ) {
			__BlockMinColumnProperty_JTextField.setText ( BlockMinColumnProperty );
		}
		if ( BlockMinRowProperty != null ) {
			__BlockMinRowProperty_JTextField.setText ( BlockMinRowProperty );
		}
		if ( BlockMaxColumnProperty != null ) {
			__BlockMaxColumnProperty_JTextField.setText ( BlockMaxColumnProperty );
		}
		if ( BlockMaxRowProperty != null ) {
			__BlockMaxRowProperty_JTextField.setText ( BlockMaxRowProperty );
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
	LayoutBlock = __LayoutBlock_JComboBox.getSelected();
	LayoutColumns = __LayoutColumns_JComboBox.getSelected();
	LayoutRows = __LayoutRows_JComboBox.getSelected();
	OutputYearType = __OutputYearType_JComboBox.getSelected();
	BlockMinColumnProperty = __BlockMinColumnProperty_JTextField.getText().trim();
	BlockMinRowProperty = __BlockMinRowProperty_JTextField.getText().trim();
	BlockMaxColumnProperty = __BlockMaxColumnProperty_JTextField.getText().trim();
	BlockMaxRowProperty = __BlockMaxRowProperty_JTextField.getText().trim();
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
	props.add ( "LayoutBlock=" + LayoutBlock );
	props.add ( "LayoutColumns=" + LayoutColumns );
	props.add ( "LayoutRows=" + LayoutRows );
	props.add ( "OutputYearType=" + OutputYearType );
	props.add ( "BlockMinColumnProperty=" + BlockMinColumnProperty );
	props.add ( "BlockMinRowProperty=" + BlockMinRowProperty );
	props.add ( "BlockMaxColumnProperty=" + BlockMaxColumnProperty );
	props.add ( "BlockMaxRowProperty=" + BlockMaxRowProperty );
	props.add ( "StyleTableID=" + StyleTableID );
	props.add ( "ConditionTableID=" + ConditionTableID );
	props.add ( "LegendWorksheet=" + LegendWorksheet );
	props.add ( "LegendAddress=" + LegendAddress );
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
