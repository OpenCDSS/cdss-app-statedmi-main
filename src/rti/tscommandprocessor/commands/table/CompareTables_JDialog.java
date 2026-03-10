// CompareTables_JDialog - Editor dialog for the CompareTables() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

package rti.tscommandprocessor.commands.table;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import RTi.Util.Table.DataTableComparerAnalysisType;

/**
Editor dialog for the CompareTables() command.
*/
@SuppressWarnings("serial")
public class CompareTables_JDialog extends JDialog
implements ActionListener, ChangeListener, ItemListener, KeyListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors.
private boolean __first_time = true; // Indicate first time display.
private JTextArea __command_JTextArea = null;
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __Table1ID_JComboBox = null;
private SimpleJComboBox __Table2ID_JComboBox = null;
private JTextField __CompareColumns1_JTextField = null;
private JTextField __ExcludeColumns1_JTextField = null;
private JTextField __MatchColumns1_JTextField = null;
private JTextField __CompareColumns2_JTextField = null;
private JTextField __ExcludeColumns2_JTextField = null;
private JTextField __MatchColumns2_JTextField = null;
private SimpleJComboBox __MatchColumnsHow_JComboBox = null;
private SimpleJComboBox __AnalysisMethod_JComboBox = null;
private JTextField __Precision_JTextField = null;
private JTextField __Tolerance_JTextField = null;
private JTextField __AllowedDiff_JTextField = null;
private SimpleJComboBox __IfDifferent_JComboBox = null;
private SimpleJComboBox __IfSame_JComboBox = null;
private JTextField __DiffTable1ID_JTextField = null;
private JTextField __DiffTable2ID_JTextField = null;
private JTextField __DiffTableID_JTextField = null;
private JTextField __RowNumberColumn_JTextField = null;
private JTextField __DiffFile1_JTextField = null;
private JTextField __DiffFile2_JTextField = null;
private JTextField __DiffFile_JTextField = null;
private SimpleJComboBox __OutputRows_JComboBox = null;
private JTextField __DiffRowCountProperty_JTextField = null;
private JTextField __DiffCellCountProperty_JTextField = null;
private JTextField __SameRowCountProperty_JTextField = null;
private JTextField __SameCellCountProperty_JTextField = null;
private SimpleJButton __browse1_JButton = null;
private SimpleJButton __browse2_JButton = null;
private SimpleJButton __browseDiff_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path1_JButton = null;
private SimpleJButton __path2_JButton = null;
private SimpleJButton __pathDiff_JButton = null;
private CompareTables_Command __command = null;
private String __working_dir = null; // Working directory.
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices table ID choices from the processor.
*/
public CompareTables_JDialog ( JFrame parent, CompareTables_Command command, List<String> tableIDChoices ) {
	super(parent, true);
	initialize ( parent, command, tableIDChoices );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	Object o = event.getSource();
	String routine = "CompareFiles_JDialog";

    if ( o == __browse1_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle("Select HTML File (table 1 differences) to Write");
        SimpleFileFilter sff_html = new SimpleFileFilter("html", "HTML File");
        fc.addChoosableFileFilter(sff_html);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName();
            String path = fc.getSelectedFile().getPath();

            if (filename == null || filename.equals("")) {
                return;
            }

            if (path != null) {
                if ( fc.getFileFilter() == sff_html ) {
                    // Enforce extension.
                    path = IOUtil.enforceFileExtension(path, "html");
                }
				// Convert path to relative path by default.
				try {
					__DiffFile1_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, routine, "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory( directory);
                refresh();
            }
        }
    }
    else if ( o == __browse2_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle("Select HTML File (table 2 differences) to Write");
        SimpleFileFilter sff_html = new SimpleFileFilter("html", "HTML File");
        fc.addChoosableFileFilter(sff_html);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName();
            String path = fc.getSelectedFile().getPath();

            if (filename == null || filename.equals("")) {
                return;
            }

            if (path != null) {
                if ( fc.getFileFilter() == sff_html ) {
                    // Enforce extension.
                    path = IOUtil.enforceFileExtension(path, "html");
                }
				// Convert path to relative path by default.
				try {
					__DiffFile2_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, routine, "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory( directory);
                refresh();
            }
        }
    }
    else if ( o == __browseDiff_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle("Select HTML File (differences) to Write");
        SimpleFileFilter sff_html = new SimpleFileFilter("html", "HTML File");
        fc.addChoosableFileFilter(sff_html);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName();
            String path = fc.getSelectedFile().getPath();

            if (filename == null || filename.equals("")) {
                return;
            }

            if (path != null) {
                if ( fc.getFileFilter() == sff_html ) {
                    // Enforce extension.
                    path = IOUtil.enforceFileExtension(path, "html");
                }
				// Convert path to relative path by default.
				try {
					__DiffFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, routine, "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "CompareTables");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited.
			response ( true );
		}
	}
    else if ( o == __path1_JButton ) {
        if ( __path1_JButton.getText().equals(__AddWorkingDirectory) ) {
            __DiffFile1_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir, __DiffFile1_JTextField.getText() ) );
        }
        else if ( __path1_JButton.getText().equals(__RemoveWorkingDirectory) ) {
            try {
                __DiffFile1_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir, __DiffFile1_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine, "Error converting file to relative path." );
            }
        }
        refresh ();
    }
    else if ( o == __path2_JButton ) {
        if ( __path2_JButton.getText().equals(__AddWorkingDirectory) ) {
            __DiffFile2_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir, __DiffFile2_JTextField.getText() ) );
        }
        else if ( __path2_JButton.getText().equals(__RemoveWorkingDirectory) ) {
            try {
                __DiffFile2_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir, __DiffFile2_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine, "Error converting file to relative path." );
            }
        }
        refresh ();
    }
    else if ( o == __pathDiff_JButton ) {
        if ( __pathDiff_JButton.getText().equals(__AddWorkingDirectory) ) {
            __DiffFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir, __DiffFile_JTextField.getText() ) );
        }
        else if ( __pathDiff_JButton.getText().equals(__RemoveWorkingDirectory) ) {
            try {
                __DiffFile_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir, __DiffFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine, "Error converting file to relative path." );
            }
        }
        refresh ();
    }
    else {
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
	String Table1ID = __Table1ID_JComboBox.getSelected();
	String Table2ID = __Table2ID_JComboBox.getSelected();
    String CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    String ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
    String MatchColumns1 = __MatchColumns1_JTextField.getText().trim();
	String CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
	String ExcludeColumns2 = __ExcludeColumns2_JTextField.getText().trim();
    String MatchColumns2 = __MatchColumns2_JTextField.getText().trim();
	String MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
	String AnalysisMethod = __AnalysisMethod_JComboBox.getSelected();
    String Precision = __Precision_JTextField.getText().trim();
    String Tolerance = __Tolerance_JTextField.getText().trim();
    String AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    String IfDifferent = __IfDifferent_JComboBox.getSelected();
    String IfSame = __IfSame_JComboBox.getSelected();
    // Changed in TSTool 14.9.3.
    //String NewTableID = __NewTableID_JTextField.getText().trim();
    //String NewTable2ID = __NewTable2ID_JTextField.getText().trim();
    String DiffTable1ID = __DiffTable1ID_JTextField.getText().trim();
    String DiffTable2ID = __DiffTable2ID_JTextField.getText().trim();
    String DiffTableID = __DiffTableID_JTextField.getText().trim();
    String RowNumberColumn = __RowNumberColumn_JTextField.getText().trim();
    //String OutputFile = __OutputFile_JTextField.getText().trim();
    //String OutputFile = __OutputFile2_JTextField.getText().trim();
    String DiffFile1 = __DiffFile1_JTextField.getText().trim();
    String DiffFile2 = __DiffFile2_JTextField.getText().trim();
    String DiffFile = __DiffFile_JTextField.getText().trim();
    String OutputRows = __OutputRows_JComboBox.getSelected();
	String DiffRowCountProperty = __DiffRowCountProperty_JTextField.getText().trim();
	String DiffCellCountProperty = __DiffCellCountProperty_JTextField.getText().trim();
	String SameRowCountProperty = __SameRowCountProperty_JTextField.getText().trim();
	String SameCellCountProperty = __SameCellCountProperty_JTextField.getText().trim();
	__error_wait = false;

    if ( Table1ID.length() > 0 ) {
        props.set ( "Table1ID", Table1ID );
    }
    if ( Table2ID.length() > 0 ) {
        props.set ( "Table2ID", Table2ID );
    }
    if ( CompareColumns1.length() > 0 ) {
        props.set ( "CompareColumns1", CompareColumns1 );
    }
    if ( ExcludeColumns1.length() > 0 ) {
        props.set ( "ExcludeColumns1", ExcludeColumns1 );
    }
    if ( MatchColumns1.length() > 0 ) {
        props.set ( "MatchColumns1", MatchColumns1 );
    }
	if ( CompareColumns2.length() > 0 ) {
		props.set ( "CompareColumns2", CompareColumns2 );
	}
	if ( ExcludeColumns2.length() > 0 ) {
		props.set ( "ExcludeColumns2", ExcludeColumns2 );
	}
    if ( MatchColumns2.length() > 0 ) {
        props.set ( "MatchColumns2", MatchColumns2 );
    }
    if ( MatchColumnsHow.length() > 0 ) {
        props.set ( "MatchColumnsHow", MatchColumnsHow );
    }
    if ( AnalysisMethod.length() > 0 ) {
        props.set ( "AnalysisMethod", AnalysisMethod );
    }
    if ( Precision.length() > 0 ) {
        props.set ( "Precision", Precision );
    }
    if ( Tolerance.length() > 0 ) {
        props.set ( "Tolerance", Tolerance );
    }
    if ( AllowedDiff.length() > 0 ) {
        props.set ( "AllowedDiff", AllowedDiff );
    }
    if ( IfDifferent.length() > 0 ) {
        props.set ( "IfDifferent", IfDifferent );
    }
    if ( IfSame.length() > 0 ) {
        props.set ( "IfSame", IfSame );
    }
    if ( DiffTable1ID.length() > 0 ) {
        props.set ( "DiffTable1ID", DiffTable1ID );
    }
    if ( DiffTable2ID.length() > 0 ) {
        props.set ( "DiffTable2ID", DiffTable2ID );
    }
    if ( DiffTableID.length() > 0 ) {
        props.set ( "DiffTableID", DiffTableID );
    }
    if ( RowNumberColumn.length() > 0 ) {
        props.set ( "RowNumberColumn", RowNumberColumn );
    }
    if ( DiffFile1.length() > 0 ) {
        props.set ( "DiffFile1", DiffFile1 );
    }
    if ( DiffFile2.length() > 0 ) {
        props.set ( "DiffFile2", DiffFile2 );
    }
    if ( DiffFile.length() > 0 ) {
        props.set ( "DiffFile", DiffFile );
    }
    if ( OutputRows.length() > 0 ) {
        props.set ( "OutputRows", OutputRows );
    }
    if ( DiffRowCountProperty.length() > 0 ) {
        props.set ( "DiffRowCountProperty", DiffRowCountProperty );
    }
    if ( DiffCellCountProperty.length() > 0 ) {
        props.set ( "DiffCellCountProperty", DiffCellCountProperty );
    }
    if ( SameRowCountProperty.length() > 0 ) {
        props.set ( "SameRowCountProperty", SameRowCountProperty );
    }
    if ( SameCellCountProperty.length() > 0 ) {
        props.set ( "SameCellCountProperty", SameCellCountProperty );
    }
	try {
	    // This will warn the user.
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
        Message.printWarning(2,"", e);
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.
In this case the command parameters have already been checked and no errors were detected.
*/
private void commitEdits () {
	String Table1ID = __Table1ID_JComboBox.getSelected();
    String Table2ID = __Table2ID_JComboBox.getSelected();
    String CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    String ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
    String MatchColumns1 = __MatchColumns1_JTextField.getText().trim();
    String CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
    String ExcludeColumns2 = __ExcludeColumns2_JTextField.getText().trim();
    String MatchColumns2 = __MatchColumns2_JTextField.getText().trim();
    String MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
    String AnalysisMethod = __AnalysisMethod_JComboBox.getSelected();
    String Precision = __Precision_JTextField.getText().trim();
    String Tolerance = __Tolerance_JTextField.getText().trim();
    String AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    String IfDifferent = __IfDifferent_JComboBox.getSelected();
    String IfSame = __IfSame_JComboBox.getSelected();
    String DiffTable1ID = __DiffTable1ID_JTextField.getText().trim();
    String DiffTable2ID = __DiffTable2ID_JTextField.getText().trim();
    String DiffTableID = __DiffTableID_JTextField.getText().trim();
    String RowNumberColumn = __RowNumberColumn_JTextField.getText().trim();
    String DiffFile1 = __DiffFile1_JTextField.getText().trim();
    String DiffFile2 = __DiffFile2_JTextField.getText().trim();
    String DiffFile = __DiffFile_JTextField.getText().trim();
    String OutputRows = __OutputRows_JComboBox.getSelected();
	String DiffRowCountProperty = __DiffRowCountProperty_JTextField.getText().trim();
	String DiffCellCountProperty = __DiffCellCountProperty_JTextField.getText().trim();
	String SameRowCountProperty = __SameRowCountProperty_JTextField.getText().trim();
	String SameCellCountProperty = __SameCellCountProperty_JTextField.getText().trim();
    __command.setCommandParameter ( "Table1ID", Table1ID );
    __command.setCommandParameter ( "Table2ID", Table2ID );
    __command.setCommandParameter ( "CompareColumns1", CompareColumns1 );
    __command.setCommandParameter ( "ExcludeColumns1", ExcludeColumns1 );
    __command.setCommandParameter ( "MatchColumns1", MatchColumns1 );
	__command.setCommandParameter ( "CompareColumns2", CompareColumns2 );
	__command.setCommandParameter ( "ExcludeColumns2", ExcludeColumns2 );
	__command.setCommandParameter ( "MatchColumns2", MatchColumns2 );
	__command.setCommandParameter ( "MatchColumnsHow", MatchColumnsHow );
    __command.setCommandParameter ( "AnalysisMethod", AnalysisMethod );
    __command.setCommandParameter ( "Precision", Precision );
	__command.setCommandParameter ( "Tolerance", Tolerance );
	__command.setCommandParameter ( "AllowedDiff", AllowedDiff );
    __command.setCommandParameter ( "IfDifferent", IfDifferent );
    __command.setCommandParameter ( "IfSame", IfSame );
	__command.setCommandParameter ( "DiffTable1ID", DiffTable1ID );
	__command.setCommandParameter ( "DiffTable2ID", DiffTable2ID );
	__command.setCommandParameter ( "DiffTableID", DiffTableID );
	__command.setCommandParameter ( "RowNumberColumn", RowNumberColumn );
	__command.setCommandParameter ( "DiffFile1", DiffFile1 );
	__command.setCommandParameter ( "DiffFile2", DiffFile2 );
	__command.setCommandParameter ( "DiffFile", DiffFile );
	__command.setCommandParameter ( "OutputRows", OutputRows );
	__command.setCommandParameter ( "DiffRowCountProperty", DiffRowCountProperty );
	__command.setCommandParameter ( "DiffCellCountProperty", DiffCellCountProperty );
	__command.setCommandParameter ( "SameRowCountProperty", SameRowCountProperty );
	__command.setCommandParameter ( "SameCellCountProperty", SameCellCountProperty );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, CompareTables_Command command, List<String> tableIDChoices ) {
	__command = command;
    CommandProcessor processor = __command.getCommandProcessor();
    __working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel.

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;

   	JGUIUtil.addComponent(paragraph, new JLabel (
        "This command compares two tables and optionally creates one or more difference table(s) and/or output file(s)."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
        "Tables to be compared should be sorted similarly to best highlight differences and avoid visual noise in output."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    __main_JTabbedPane = new JTabbedPane ();
    __main_JTabbedPane.addChangeListener(this);
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    // Panel for input parameters.
    int yInput = -1;
    JPanel input_JPanel = new JPanel();
    input_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Input", input_JPanel );

    JGUIUtil.addComponent(input_JPanel, new JLabel (
        "Specify the tables and columns."),
        0, ++yInput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "By default, all columns (and rows) from the specified tables are compared."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Specify the 'columns to compare' and 'columns to exclude' if specific columns should be compared."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Column order does not need to be the same in each table if MatchColumnsHow=" + this.__command._Name + "."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel (
        "If AnalysisMethod=Advanced, columns to match can be specified to allow searches for matching rows."),
        0, ++yInput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(input_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yInput, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(input_JPanel, new JLabel ( "Table1 ID:" ),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Table1ID_JComboBox = new SimpleJComboBox ( 25, true );    // Allow edit.
    __Table1ID_JComboBox.setToolTipText("The first table to compare, can use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table.
    __Table1ID_JComboBox.setData ( tableIDChoices );
    __Table1ID_JComboBox.addItemListener ( this );
    __Table1ID_JComboBox.getJTextComponent().addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(input_JPanel, __Table1ID_JComboBox,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel( "Required - first table to compare."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 1 columns to compare:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CompareColumns1_JTextField = new JTextField (10);
    __CompareColumns1_JTextField.setToolTipText(
    	"Comma-separated list of column names in the first table to compare, can use ${Property} notation");
    __CompareColumns1_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(input_JPanel, __CompareColumns1_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - default is to compare all table 1 columns."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 1 columns to exclude:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns1_JTextField = new JTextField (10);
    __ExcludeColumns1_JTextField.setToolTipText(
    	"Comma-separated list of column names in the first table to exclude, can use ${Property} notation");
    __ExcludeColumns1_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(input_JPanel, __ExcludeColumns1_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - default is to compare all table 1 columns."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 1 columns to match:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MatchColumns1_JTextField = new JTextField (10);
    __MatchColumns1_JTextField.setToolTipText(
    	"Column namess in the first table to match when searching for matching rows for advanced analysis, can use ${Property} notation.");
    __MatchColumns1_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(input_JPanel, __MatchColumns1_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - used with Advanced analysis method (default=CompareColumns1)."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ( "Table2 ID:" ),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Table2ID_JComboBox = new SimpleJComboBox ( 25, true );    // Allow edit.
    __Table2ID_JComboBox.setToolTipText("The second table to compare, can use ${Property} notation");
    __Table2ID_JComboBox.setData ( tableIDChoices );
    __Table2ID_JComboBox.addItemListener ( this );
    __Table2ID_JComboBox.getJTextComponent().addKeyListener ( this );
    //__Table2D_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(input_JPanel, __Table2ID_JComboBox,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel( "Required - second table to compare."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 2 columns to compare:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CompareColumns2_JTextField = new JTextField (10);
    __CompareColumns2_JTextField.setToolTipText(
    	"Comma-separated list of column names in the second table to compare, can use ${Property} notation");
    __CompareColumns2_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(input_JPanel, __CompareColumns2_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - default is to compare all table 2 columns."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 2 columns to exclude:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns2_JTextField = new JTextField (10);
    __ExcludeColumns2_JTextField.setToolTipText(
    	"Comma-separated list of column names in the second table to exclude, can use ${Property} notation");
    __ExcludeColumns2_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(input_JPanel, __ExcludeColumns2_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - default is to compare all table 2 columns."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ("Table 2 columns to match:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MatchColumns2_JTextField = new JTextField (10);
    __MatchColumns2_JTextField.setToolTipText(
    	"Column names in the second table to match when searching for matching rows for advanced analysis, can use ${Property} notation.");
    __MatchColumns2_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(input_JPanel, __MatchColumns2_JTextField,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel ("Optional - used with Advanced analysis method (default=MatchColumns1)."),
        3, yInput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(input_JPanel, new JLabel ( "Match columns how:"),
        0, ++yInput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MatchColumnsHow_JComboBox = new SimpleJComboBox ( false );
    __MatchColumnsHow_JComboBox.setToolTipText("If Name, then columns with matching names are compared.  If Order, column 1 is compared to column 1, etc.");
    List<String> matchChoices = new ArrayList<>();
    matchChoices.add ( "" ); // Default.
    matchChoices.add ( __command._Name );
    matchChoices.add ( __command._Order );
    __MatchColumnsHow_JComboBox.setData(matchChoices);
    __MatchColumnsHow_JComboBox.select ( 0 );
    __MatchColumnsHow_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(input_JPanel, __MatchColumnsHow_JComboBox,
        1, yInput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(input_JPanel, new JLabel(
        "Optional - how to match columns in tables (default=" + __command._Name + ")."),
        3, yInput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Panel for analysis parameters.
    int yAnalysis = -1;
    JPanel analysis_JPanel = new JPanel();
    analysis_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Analysis", analysis_JPanel );

    JGUIUtil.addComponent(analysis_JPanel, new JLabel (
        "If AnalysisMethod=Simple, the tables are expected to have the same number of rows so that cell values can be easily compared."),
        0, ++yAnalysis, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel (
        "If AnalysisMethod=Advanced, empty rows will be inserted in the difference tables to align matching rows."),
        0, ++yAnalysis, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel (
        "Tables should be sorted similarly before doing the comparison (e.g., use database SQL sort or the SortTable command)."),
        0, ++yAnalysis, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel (
        "The tables are compared by formatting cell values as strings, " +
        "which ensures that floating point numbers can be compared exactly."),
        0, ++yAnalysis, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel (
        "If necessary, specify the precision or tolerance for floating point number comparisons."),
        0, ++yAnalysis, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(analysis_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yAnalysis, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Analysis method:"),
        0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AnalysisMethod_JComboBox = new SimpleJComboBox ( false );
    List<String> analysisMethodChoices = new ArrayList<>();
    analysisMethodChoices.add ( "" ); // Default.
    analysisMethodChoices.add ( "" + DataTableComparerAnalysisType.ADVANCED );
    analysisMethodChoices.add ( "" + DataTableComparerAnalysisType.SIMPLE );
    __AnalysisMethod_JComboBox.setData(analysisMethodChoices);
    __AnalysisMethod_JComboBox.select ( 0 );
    __AnalysisMethod_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(analysis_JPanel, __AnalysisMethod_JComboBox,
        1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel(
        "Optional - analysis method (default=" + DataTableComparerAnalysisType.SIMPLE + ")."),
        3, yAnalysis, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Precision:" ),
        0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Precision_JTextField = new JTextField ( 5 );
    __Precision_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(analysis_JPanel, __Precision_JTextField,
        1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel(
        "Optional - digits after decimal to compare (default=use precision from table column)."),
        3, yAnalysis, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Tolerance:" ),
        0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Tolerance_JTextField = new JTextField ( 15 );
    __Tolerance_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(analysis_JPanel, __Tolerance_JTextField,
        1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel(
        "Optional - tolerance(s) to indicate difference (e.g., .01, .1, default=exact comparison)."),
        3, yAnalysis, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Allowed # of different values:"),
        0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AllowedDiff_JTextField = new JTextField ( 5 );
    __AllowedDiff_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(analysis_JPanel, __AllowedDiff_JTextField,
        1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(analysis_JPanel, new JLabel( "Optional - when checking for differences (default=0)"),
        3, yAnalysis, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

     JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Action if different:"),
         0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __IfDifferent_JComboBox = new SimpleJComboBox ( false );
     List<String> diffChoices = new ArrayList<>();
     diffChoices.add ( "" ); // Default.
     diffChoices.add ( __command._Ignore );
     diffChoices.add ( __command._Warn );
     diffChoices.add ( __command._Fail );
     __IfDifferent_JComboBox.setData(diffChoices);
     __IfDifferent_JComboBox.select ( 0 );
     __IfDifferent_JComboBox.addActionListener ( this );
     JGUIUtil.addComponent(analysis_JPanel, __IfDifferent_JComboBox,
         1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(analysis_JPanel, new JLabel(
         "Optional - action if tables are different (default=" + __command._Ignore + ")."),
         3, yAnalysis, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

     JGUIUtil.addComponent(analysis_JPanel, new JLabel ( "Action if same:"),
         0, ++yAnalysis, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __IfSame_JComboBox = new SimpleJComboBox ( false );
     List<String> sameChoices = new ArrayList<>();
     sameChoices.add ( "" );  // Default.
     sameChoices.add ( __command._Ignore );
     sameChoices.add ( __command._Warn );
     sameChoices.add ( __command._Fail );
     __IfSame_JComboBox.setData(sameChoices);
     __IfSame_JComboBox.select ( 0 );
     __IfSame_JComboBox.addActionListener ( this );
     JGUIUtil.addComponent(analysis_JPanel, __IfSame_JComboBox,
         1, yAnalysis, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(analysis_JPanel, new JLabel(
         "Optional - action if tables are the same (default=" + __command._Ignore + ")."),
         3, yAnalysis, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Panel for output parameters.
    int yOutput = -1;
    JPanel output_JPanel = new JPanel();
    output_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Output", output_JPanel );

    JGUIUtil.addComponent(output_JPanel, new JLabel (
        "If AnalysisMethod=Simple, the input tables are assumed to have rows aligned at the start."),
        0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel (
        "Therefore, a single output difference table and file can be created."),
        0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel (
        "If AnalysisMethod=Advanced, two difference tables and/or files can be created, "
        + "which may have blank rows inserted to align matched table rows."),
        0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel (
        "If AnalysisMethod=Advanced, a combined difference table can also be created, which overlaps difference tables 1 and 2."),
        0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel (
        "The table view will not color the differences, but the difference file(s) as HTML will be colored."),
        0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(output_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yOutput, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel ("Difference table (1) ID:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffTable1ID_JTextField = new JTextField (10);
    __DiffTable1ID_JTextField.setToolTipText("Name of difference table from table1 perspective, can use ${Property} notation.");
    __DiffTable1ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(output_JPanel, __DiffTable1ID_JTextField,
        1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ("Optional - unique identifier for the first difference table."),
        3, yOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(output_JPanel, new JLabel ("Difference table (2) ID:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffTable2ID_JTextField = new JTextField (10);
    __DiffTable2ID_JTextField.setToolTipText("Name of difference table from table2 perspective, can use ${Property} notation.");
    __DiffTable2ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(output_JPanel, __DiffTable2ID_JTextField,
        1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ("Optional - unique identifier for the second difference table."),
        3, yOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(output_JPanel, new JLabel ("Difference table (combined) ID:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffTableID_JTextField = new JTextField (10);
    __DiffTableID_JTextField.setToolTipText(
    	"Used with advanced analysis, name of difference table merging the above difference tables, can use ${Property} notation.");
    __DiffTableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(output_JPanel, __DiffTableID_JTextField,
        1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ("Optional - unique identifier for the final difference table."),
        3, yOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(output_JPanel, new JLabel("Row number column:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowNumberColumn_JTextField = new JTextField ( "", 20 );
    __RowNumberColumn_JTextField.setToolTipText("Row number column to add, can use ${Property} notation");
    __RowNumberColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(output_JPanel, __RowNumberColumn_JTextField,
        1, yOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Optional - row number column to add (default=no row number column)." ),
        3, yOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Difference file (1) to write:" ),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __DiffFile1_JTextField = new JTextField ( 50 );
     __DiffFile1_JTextField.setToolTipText (
    	"Optional output file for first difference table - specify .html extension, can use ${Property} notation." );
     __DiffFile1_JTextField.addKeyListener ( this );
     // Output file layout fights back with other rows so put in its own panel.
 	JPanel DiffFile1_JPanel = new JPanel();
 	DiffFile1_JPanel.setLayout(new GridBagLayout());
     JGUIUtil.addComponent(DiffFile1_JPanel, __DiffFile1_JTextField,
 		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
 	__browse1_JButton = new SimpleJButton ( "...", this );
 	__browse1_JButton.setToolTipText("Browse for file");
     JGUIUtil.addComponent(DiffFile1_JPanel, __browse1_JButton,
 		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	if ( __working_dir != null ) {
 		// Add the button to allow conversion to/from relative path.
 		__path1_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
 		JGUIUtil.addComponent(DiffFile1_JPanel, __path1_JButton,
 			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	}
 	JGUIUtil.addComponent(output_JPanel, DiffFile1_JPanel,
 		1, yOutput, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Difference file (2) to write:" ),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffFile2_JTextField = new JTextField ( 50 );
    __DiffFile2_JTextField.setToolTipText (
    	"Optional output file for second difference table - specify .html extension, can use ${Property} notation." );
    __DiffFile2_JTextField.addKeyListener ( this );
    // Output file layout fights back with other rows so put in its own panel.
    JPanel DiffFile2_JPanel = new JPanel();
    DiffFile2_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(DiffFile2_JPanel, __DiffFile2_JTextField,
 		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
 	__browse2_JButton = new SimpleJButton ( "...", this );
 	__browse2_JButton.setToolTipText("Browse for file");
     JGUIUtil.addComponent(DiffFile2_JPanel, __browse2_JButton,
 		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	if ( __working_dir != null ) {
 		// Add the button to allow conversion to/from relative path.
 		__path2_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
 		JGUIUtil.addComponent(DiffFile2_JPanel, __path2_JButton,
 			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	}
 	JGUIUtil.addComponent(output_JPanel, DiffFile2_JPanel,
 		1, yOutput, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Difference file (combined) to write:" ),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffFile_JTextField = new JTextField ( 50 );
    __DiffFile_JTextField.setToolTipText (
    	"Optional output file for combined difference table - specify .html extension, can use ${Property} notation." );
    __DiffFile_JTextField.addKeyListener ( this );
    // Output file layout fights back with other rows so put in its own panel.
    JPanel DiffFile_JPanel = new JPanel();
    DiffFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(DiffFile_JPanel, __DiffFile_JTextField,
 		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
 	__browseDiff_JButton = new SimpleJButton ( "...", this );
 	__browseDiff_JButton.setToolTipText("Browse for file");
     JGUIUtil.addComponent(DiffFile_JPanel, __browseDiff_JButton,
 		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	if ( __working_dir != null ) {
 		// Add the button to allow conversion to/from relative path.
 		__pathDiff_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
 		JGUIUtil.addComponent(DiffFile_JPanel, __pathDiff_JButton,
 			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	}
 	JGUIUtil.addComponent(output_JPanel, DiffFile_JPanel,
 		1, yOutput, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Output rows:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputRows_JComboBox = new SimpleJComboBox ( false );
    __OutputRows_JComboBox.setToolTipText("Indicate which rows to output.");
    List<String> outputChoices = new ArrayList<>();
    outputChoices.add ( "" );  // Default.
    outputChoices.add ( __command._All );
    outputChoices.add ( __command._Different );
    outputChoices.add ( __command._Same );
    __OutputRows_JComboBox.setData(outputChoices);
    __OutputRows_JComboBox.select ( 0 );
    __OutputRows_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(output_JPanel, __OutputRows_JComboBox,
        1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel(
        "Optional - rows to output (default=" + __command._All + ")."),
        3, yOutput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel("Different row count property:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffRowCountProperty_JTextField = new JTextField ( "", 20 );
    __DiffRowCountProperty_JTextField.setToolTipText("Specify the property name for the number of rows with differences, can use ${Property} notation");
    __DiffRowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(output_JPanel, __DiffRowCountProperty_JTextField,
        1, yOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Optional - processor property to set as count of different rows." ),
        3, yOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel("Different cell count property:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DiffCellCountProperty_JTextField = new JTextField ( "", 20 );
    __DiffCellCountProperty_JTextField.setToolTipText("Specify the property name for the number of cells with differences, can use ${Property} notation");
    __DiffCellCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(output_JPanel, __DiffCellCountProperty_JTextField,
        1, yOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Optional - processor property to set as count of different cells." ),
        3, yOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel("Same row count property:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SameRowCountProperty_JTextField = new JTextField ( "", 20 );
    __SameRowCountProperty_JTextField.setToolTipText("Specify the property name for the number of rows that are the same, can use ${Property} notation");
    __SameRowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(output_JPanel, __SameRowCountProperty_JTextField,
        1, yOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Optional - processor property to set as count of same rows." ),
        3, yOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(output_JPanel, new JLabel("Same cell count property:"),
        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SameCellCountProperty_JTextField = new JTextField ( "", 20 );
    __SameCellCountProperty_JTextField.setToolTipText("Specify the property name for the number of cells that are the same, can use ${Property} notation");
    __SameCellCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(output_JPanel, __SameCellCountProperty_JTextField,
        1, yOutput, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Optional - processor property to set as count of same cells." ),
        3, yOutput, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

     JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"),
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __command_JTextArea = new JTextArea (4,40);
     __command_JTextArea.setLineWrap ( true );
     __command_JTextArea.setWrapStyleWord ( true );
     __command_JTextArea.setEditable (false);
     JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
         1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents.
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
	refresh();	// Sets the __path_JButton status.
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

public void keyTyped (KeyEvent event) {
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok () {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh () {
	String routine = getClass().getSimpleName() + ".refresh";
    String Table1ID = "";
    String Table2ID = "";
    String CompareColumns1 = "";
    String ExcludeColumns1 = "";
    String MatchColumns1 = "";
    String CompareColumns2 = "";
    String ExcludeColumns2 = "";
    String MatchColumns2 = "";
    String MatchColumnsHow = "";
    String AnalysisMethod = "";
    String Precision = "";
    String Tolerance = "";
    String AllowedDiff = "";
    String IfDifferent = "";
    String IfSame = "";
    String DiffTable1ID = "";
    String DiffTable2ID = "";
    String DiffTableID = "";
    String RowNumberColumn = "";
    String DiffFile1 = "";
    String DiffFile2 = "";
    String DiffFile = "";
    String OutputRows = "";
    String DiffRowCountProperty = "";
    String DiffCellCountProperty = "";
    String SameRowCountProperty = "";
    String SameCellCountProperty = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        Table1ID = props.getValue ( "Table1ID" );
        Table2ID = props.getValue ( "Table2ID" );
        CompareColumns1 = props.getValue ( "CompareColumns1" );
        ExcludeColumns1 = props.getValue ( "ExcludeColumns1" );
        MatchColumns1 = props.getValue ( "MatchColumns1" );
        CompareColumns2 = props.getValue ( "CompareColumns2" );
        ExcludeColumns2 = props.getValue ( "ExcludeColumns2" );
        MatchColumns2 = props.getValue ( "MatchColumns2" );
        MatchColumnsHow = props.getValue ( "MatchColumnsHow" );
        AnalysisMethod = props.getValue ( "AnalysisMethod" );
        Precision = props.getValue ( "Precision" );
        Tolerance = props.getValue ( "Tolerance" );
        AllowedDiff = props.getValue ( "AllowedDiff" );
        IfDifferent = props.getValue ( "IfDifferent" );
        IfSame = props.getValue ( "IfSame" );
        DiffTable1ID = props.getValue ( "DiffTable1ID" );
        DiffTable2ID = props.getValue ( "DiffTable2ID" );
        DiffTableID = props.getValue ( "DiffTableID" );
        RowNumberColumn = props.getValue ( "RowNumberColumn" );
        DiffFile1 = props.getValue ( "DiffFile1" );
        DiffFile2 = props.getValue ( "DiffFile2" );
        DiffFile = props.getValue ( "DiffFile" );
        OutputRows = props.getValue ( "OutputRows" );
        DiffRowCountProperty = props.getValue ( "DiffRowCountProperty" );
        DiffCellCountProperty = props.getValue ( "DiffCellCountProperty" );
        SameRowCountProperty = props.getValue ( "SameRowCountProperty" );
        SameCellCountProperty = props.getValue ( "SameCellCountProperty" );
        if ( Table1ID == null ) {
            // Select default.
            __Table1ID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __Table1ID_JComboBox,Table1ID, JGUIUtil.NONE, null, null ) ) {
                __Table1ID_JComboBox.select ( Table1ID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTable1ID value \"" + Table1ID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( Table2ID == null ) {
            // Select default.
            __Table2ID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __Table2ID_JComboBox,Table2ID, JGUIUtil.NONE, null, null ) ) {
                __Table2ID_JComboBox.select ( Table2ID );
            }
            else {
                Message.printWarning ( 2, routine,
                "Existing command references an invalid\nTable2ID value \"" + Table2ID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( CompareColumns1 != null ) {
            __CompareColumns1_JTextField.setText ( CompareColumns1 );
        }
        if ( ExcludeColumns1 != null ) {
            __ExcludeColumns1_JTextField.setText ( ExcludeColumns1 );
        }
        if ( MatchColumns1 != null ) {
            __MatchColumns1_JTextField.setText ( MatchColumns1 );
        }
		if ( CompareColumns2 != null ) {
			__CompareColumns2_JTextField.setText ( CompareColumns2 );
		}
		if ( ExcludeColumns2 != null ) {
			__ExcludeColumns2_JTextField.setText ( ExcludeColumns2 );
		}
        if ( MatchColumns2 != null ) {
            __MatchColumns2_JTextField.setText ( MatchColumns2 );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__MatchColumnsHow_JComboBox, MatchColumnsHow, JGUIUtil.NONE, null, null ) ) {
            __MatchColumnsHow_JComboBox.select ( MatchColumnsHow );
        }
        else {
            if ( (MatchColumnsHow == null) || MatchColumnsHow.equals("") ) {
                // New command...select the default.
                __MatchColumnsHow_JComboBox.select ( 0 );
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "MatchColumnsHow parameter \"" + MatchColumnsHow + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__AnalysisMethod_JComboBox, AnalysisMethod, JGUIUtil.NONE, null, null ) ) {
            __AnalysisMethod_JComboBox.select ( AnalysisMethod );
        }
        else {
            if ( (AnalysisMethod == null) || AnalysisMethod.equals("") ) {
                // New command...select the default.
                __AnalysisMethod_JComboBox.select ( 0 );
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "AnalysisMethod parameter \"" + AnalysisMethod + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( Precision != null ) {
            __Precision_JTextField.setText ( Precision );
        }
        if ( Tolerance != null ) {
            __Tolerance_JTextField.setText ( Tolerance );
        }
        if ( AllowedDiff != null ) {
            __AllowedDiff_JTextField.setText ( AllowedDiff );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__IfDifferent_JComboBox, IfDifferent, JGUIUtil.NONE, null, null ) ) {
            __IfDifferent_JComboBox.select ( IfDifferent );
        }
        else {
            if ( (IfDifferent == null) || IfDifferent.equals("") ) {
                // New command...select the default.
                __IfDifferent_JComboBox.select ( 0 );
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "IfDifferent parameter \"" + IfDifferent + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem( __IfSame_JComboBox, IfSame, JGUIUtil.NONE, null, null ) ) {
            __IfSame_JComboBox.select ( IfSame );
        }
        else {
            if ( (IfSame == null) || IfSame.equals("") ) {
                // New command...select the default.
                __IfSame_JComboBox.select ( 0 );
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "IfSame parameter \"" + IfSame + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( DiffTable1ID != null ) {
            __DiffTable1ID_JTextField.setText ( DiffTable1ID );
        }
        if ( DiffTable2ID != null ) {
            __DiffTable2ID_JTextField.setText ( DiffTable2ID );
        }
        if ( DiffTableID != null ) {
            __DiffTableID_JTextField.setText ( DiffTableID );
        }
        if ( RowNumberColumn != null ) {
            __RowNumberColumn_JTextField.setText ( RowNumberColumn );
        }
        if ( DiffFile1 != null ) {
            __DiffFile1_JTextField.setText ( DiffFile1 );
        }
        if ( DiffFile2 != null ) {
            __DiffFile2_JTextField.setText ( DiffFile2 );
        }
        if ( DiffFile != null ) {
            __DiffFile_JTextField.setText ( DiffFile );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem( __OutputRows_JComboBox, OutputRows, JGUIUtil.NONE, null, null ) ) {
            __OutputRows_JComboBox.select ( OutputRows );
        }
        else {
            if ( (OutputRows == null) || OutputRows.equals("") ) {
                // New command...select the default.
                __OutputRows_JComboBox.select ( 0 );
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "OutputRows parameter \"" + OutputRows + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( DiffRowCountProperty != null ) {
            __DiffRowCountProperty_JTextField.setText ( DiffRowCountProperty );
        }
        if ( DiffCellCountProperty != null ) {
            __DiffCellCountProperty_JTextField.setText ( DiffCellCountProperty );
        }
        if ( SameRowCountProperty != null ) {
            __SameRowCountProperty_JTextField.setText ( SameRowCountProperty );
        }
        if ( SameCellCountProperty != null ) {
            __SameCellCountProperty_JTextField.setText ( SameCellCountProperty );
        }
	}
	// Regardless, reset the command from the fields.
	Table1ID = __Table1ID_JComboBox.getSelected();
	Table2ID = __Table2ID_JComboBox.getSelected();
    CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
    MatchColumns1 = __MatchColumns1_JTextField.getText().trim();
	CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
	ExcludeColumns2 = __ExcludeColumns2_JTextField.getText().trim();
    MatchColumns2 = __MatchColumns2_JTextField.getText().trim();
	MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
	AnalysisMethod = __AnalysisMethod_JComboBox.getSelected();
    Precision = __Precision_JTextField.getText().trim();
    Tolerance = __Tolerance_JTextField.getText().trim();
    AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    IfDifferent = __IfDifferent_JComboBox.getSelected();
    IfSame = __IfSame_JComboBox.getSelected();
    DiffTable1ID = __DiffTable1ID_JTextField.getText().trim();
    DiffTable2ID = __DiffTable2ID_JTextField.getText().trim();
    DiffTableID = __DiffTableID_JTextField.getText().trim();
    RowNumberColumn = __RowNumberColumn_JTextField.getText().trim();
    DiffFile1 = __DiffFile1_JTextField.getText().trim();
    DiffFile2 = __DiffFile2_JTextField.getText().trim();
    DiffFile = __DiffFile_JTextField.getText().trim();
    OutputRows = __OutputRows_JComboBox.getSelected();
	DiffRowCountProperty = __DiffRowCountProperty_JTextField.getText().trim();
	DiffCellCountProperty = __DiffCellCountProperty_JTextField.getText().trim();
	SameRowCountProperty = __SameRowCountProperty_JTextField.getText().trim();
	SameCellCountProperty = __SameCellCountProperty_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "Table1ID=" + Table1ID );
    props.add ( "Table2ID=" + Table2ID );
    props.add ( "CompareColumns1=" + CompareColumns1 );
    props.add ( "ExcludeColumns1=" + ExcludeColumns1 );
    props.add ( "MatchColumns1=" + MatchColumns1 );
	props.add ( "CompareColumns2=" + CompareColumns2 );
	props.add ( "ExcludeColumns2=" + ExcludeColumns2 );
    props.add ( "MatchColumns2=" + MatchColumns2 );
	props.add ( "MatchColumnsHow=" + MatchColumnsHow );
	props.add ( "AnalysisMethod=" + AnalysisMethod );
	props.add ( "Precision=" + Precision );
	props.add ( "Tolerance=" + Tolerance );
	props.add ( "AllowedDiff=" + AllowedDiff );
    props.add ( "IfDifferent=" + IfDifferent );
    props.add ( "IfSame=" + IfSame );
    props.add ( "DiffTable1ID=" + DiffTable1ID );
    props.add ( "DiffTable2ID=" + DiffTable2ID );
    props.add ( "DiffTableID=" + DiffTableID );
    props.add ( "RowNumberColumn=" + RowNumberColumn );
    props.add ( "DiffFile1=" + DiffFile1 );
    props.add ( "DiffFile2=" + DiffFile2 );
    props.add ( "DiffFile=" + DiffFile );
	props.add ( "OutputRows=" + OutputRows );
	props.add ( "DiffRowCountProperty=" + DiffRowCountProperty );
	props.add ( "DiffCellCountProperty=" + DiffCellCountProperty );
	props.add ( "SameRowCountProperty=" + SameRowCountProperty );
	props.add ( "SameCellCountProperty=" + SameCellCountProperty );
	__command_JTextArea.setText( __command.toString ( props ).trim() );
	// Check the path and determine what the label on the path button should be.
	if ( __path1_JButton != null ) {
		if ( (DiffFile1 != null) && !DiffFile1.isEmpty() ) {
			__path1_JButton.setEnabled ( true );
			File f = new File ( DiffFile1 );
			if ( f.isAbsolute() ) {
				__path1_JButton.setText ( __RemoveWorkingDirectory );
				__path1_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__path1_JButton.setText ( __AddWorkingDirectory );
            	__path1_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path1_JButton.setEnabled(false);
		}
	}
	if ( __path2_JButton != null ) {
		if ( (DiffFile2 != null) && !DiffFile2.isEmpty() ) {
			__path2_JButton.setEnabled ( true );
			File f = new File ( DiffFile2 );
			if ( f.isAbsolute() ) {
				__path2_JButton.setText ( __RemoveWorkingDirectory );
				__path2_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__path2_JButton.setText ( __AddWorkingDirectory );
            	__path2_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path2_JButton.setEnabled(false);
		}
	}
	if ( __pathDiff_JButton != null ) {
		if ( (DiffFile != null) && !DiffFile.isEmpty() ) {
			__pathDiff_JButton.setEnabled ( true );
			File f = new File ( DiffFile );
			if ( f.isAbsolute() ) {
				__pathDiff_JButton.setText ( __RemoveWorkingDirectory );
				__pathDiff_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
            	__pathDiff_JButton.setText ( __AddWorkingDirectory );
            	__pathDiff_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__pathDiff_JButton.setEnabled(false);
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
*/
private void response ( boolean ok ) {
	__ok = ok;	// Save to be returned by ok().
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
 * Handle JTabbedPane changes.
 * @param event ChangeEvent to handle
 */
public void stateChanged ( ChangeEvent event ) {
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing(WindowEvent event) {
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener.
public void windowActivated(WindowEvent evt) {
}

public void windowClosed(WindowEvent evt) {
}

public void windowDeactivated(WindowEvent evt) {
}

public void windowDeiconified(WindowEvent evt) {
}

public void windowIconified(WindowEvent evt) {
}

public void windowOpened(WindowEvent evt) {
}

}