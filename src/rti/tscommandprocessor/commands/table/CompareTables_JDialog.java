// CompareTables_JDialog - Editor dialog for the CompareTables() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

/**
Editor dialog for the CompareTables() command.
*/
@SuppressWarnings("serial")
public class CompareTables_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
    
private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __Table1ID_JComboBox = null;
private SimpleJComboBox __Table2ID_JComboBox = null;
private JTextField __CompareColumns1_JTextField = null;
private JTextField __ExcludeColumns1_JTextField = null;
private JTextField __CompareColumns2_JTextField = null;
private SimpleJComboBox __MatchColumnsHow_JComboBox = null;
private JTextField __Precision_JTextField = null;
private JTextField __Tolerance_JTextField = null;
private JTextField __AllowedDiff_JTextField = null;
private JTextField __NewTableID_JTextField = null;
private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __IfDifferent_JComboBox = null;
private SimpleJComboBox __IfSame_JComboBox = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null;
private CompareTables_Command __command = null;
private String __working_dir = null; // Working directory.
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices table ID choices from the processor.
*/
public CompareTables_JDialog ( JFrame parent, CompareTables_Command command, List<String> tableIDChoices )
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
            fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        fc.setDialogTitle("Select HTML File to Write");
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
                    // Enforce extension...
                    path = IOUtil.enforceFileExtension(path, "html");
                }
				// Convert path to relative path by default.
				try {
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CompareTables_JDialog", "Error converting file to relative path." );
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
			// Command has been edited...
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
                "WriteTableToHTML_JDialog", "Error converting file to relative path." );
            }
        }
        refresh ();
    }
    else {
        refresh();
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String Table1ID = __Table1ID_JComboBox.getSelected();
	String Table2ID = __Table2ID_JComboBox.getSelected();
    String CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    String ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
	String CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
	String MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
    String Precision = __Precision_JTextField.getText().trim();
    String Tolerance = __Tolerance_JTextField.getText().trim();
    String AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    String NewTableID = __NewTableID_JTextField.getText().trim();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String IfDifferent = __IfDifferent_JComboBox.getSelected();
    String IfSame = __IfSame_JComboBox.getSelected();
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
	if ( CompareColumns2.length() > 0 ) {
		props.set ( "CompareColumns2", CompareColumns2 );
	}
    if ( MatchColumnsHow.length() > 0 ) {
        props.set ( "MatchColumnsHow", MatchColumnsHow );
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
    if ( NewTableID.length() > 0 ) {
        props.set ( "NewTableID", NewTableID );
    }
    if ( OutputFile.length() > 0 ) {
        props.set ( "OutputFile", OutputFile );
    }
    if ( IfDifferent.length() > 0 ) {
        props.set ( "IfDifferent", IfDifferent );
    }
    if ( IfSame.length() > 0 ) {
        props.set ( "IfSame", IfSame );
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
{	String Table1ID = __Table1ID_JComboBox.getSelected();
    String Table2ID = __Table2ID_JComboBox.getSelected();
    String CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    String ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
    String CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
    String MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
    String Precision = __Precision_JTextField.getText().trim();
    String Tolerance = __Tolerance_JTextField.getText().trim();
    String AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    String NewTableID = __NewTableID_JTextField.getText().trim();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String IfDifferent = __IfDifferent_JComboBox.getSelected();
    String IfSame = __IfSame_JComboBox.getSelected();
    __command.setCommandParameter ( "Table1ID", Table1ID );
    __command.setCommandParameter ( "Table2ID", Table2ID );
    __command.setCommandParameter ( "CompareColumns1", CompareColumns1 );
    __command.setCommandParameter ( "ExcludeColumns1", ExcludeColumns1 );
	__command.setCommandParameter ( "CompareColumns2", CompareColumns2 );
	__command.setCommandParameter ( "MatchColumnsHow", MatchColumnsHow );
    __command.setCommandParameter ( "Precision", Precision );
	__command.setCommandParameter ( "Tolerance", Tolerance );
	__command.setCommandParameter ( "AllowedDiff", AllowedDiff );
	__command.setCommandParameter ( "NewTableID", NewTableID );
	__command.setCommandParameter ( "OutputFile", OutputFile );
    __command.setCommandParameter ( "IfDifferent", IfDifferent );
    __command.setCommandParameter ( "IfSame", IfSame );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__CompareColumns2_JTextField = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, CompareTables_Command command, List<String> tableIDChoices )
{	__command = command;
    CommandProcessor processor = __command.getCommandProcessor();
    __working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

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
        "This command compares two tables and optionally creates a new comparison table."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "By default, all columns (and rows) from the specified tables are compared; however, the columns to " +
        "compare can be specified."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The table is compared by formatting cell values as strings.  If necessary, " +
        "specify precision and tolerance for floating point comparisons."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The results table, if written as HTML, indicates differences as colored cells " +
        "(the simple table view will not color the differences)."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table1 ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Table1ID_JComboBox = new SimpleJComboBox ( 12, true );    // Allow edit
    tableIDChoices.add(0,""); // Add blank to ignore table
    __Table1ID_JComboBox.setData ( tableIDChoices );
    __Table1ID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __Table1ID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - first table to compare."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Table 1 columns to compare:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CompareColumns1_JTextField = new JTextField (10);
    __CompareColumns1_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CompareColumns1_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - default is to compare all."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Table 1 columns to exclude:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns1_JTextField = new JTextField (10);
    __ExcludeColumns1_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ExcludeColumns1_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - default is to compare all."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table2 ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Table2ID_JComboBox = new SimpleJComboBox ( 12, true );    // Allow edit
    __Table2ID_JComboBox.setData ( tableIDChoices );
    __Table2ID_JComboBox.addItemListener ( this );
    //__Table2D_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __Table2ID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - second table to compare."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Table 2 columns to compare:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CompareColumns2_JTextField = new JTextField (10);
    __CompareColumns2_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __CompareColumns2_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - default is to compare all."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Match columns how:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MatchColumnsHow_JComboBox = new SimpleJComboBox ( false );
    List<String> matchChoices = new ArrayList<String>();
    matchChoices.add ( "" ); // Default
    matchChoices.add ( __command._Name );
    matchChoices.add ( __command._Order );
    __MatchColumnsHow_JComboBox.setData(matchChoices);
    __MatchColumnsHow_JComboBox.select ( 0 );
    __MatchColumnsHow_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __MatchColumnsHow_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - how to match columns in tables (default=" + __command._Name + ")."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Precision:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Precision_JTextField = new JTextField ( 5 );
    __Precision_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Precision_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - digits after decimal to compare (default=use precision from table column)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Tolerance:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Tolerance_JTextField = new JTextField ( 15 );
    __Tolerance_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Tolerance_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - tolerance(s) to indicate difference (e.g., .01, .1, default=exact comparison)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Allowed # of different values:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AllowedDiff_JTextField = new JTextField ( 5 );
    __AllowedDiff_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __AllowedDiff_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - when checking for differences (default=0)"), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("New table ID:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NewTableID_JTextField = new JTextField (10);
    __NewTableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __NewTableID_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - unique identifier for the comparison table."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output file to write:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __OutputFile_JTextField = new JTextField ( 50 );
     __OutputFile_JTextField.setToolTipText ( "Optional output file - specify .html extension." );
     __OutputFile_JTextField.addKeyListener ( this );
     JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
        1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
     __browse_JButton = new SimpleJButton ( "...", this );
 	 __browse_JButton.setToolTipText("Browse for file");
     JGUIUtil.addComponent(main_JPanel, __browse_JButton,
        6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
 	 if ( __working_dir != null ) {
	 	 // Add the button to allow conversion to/from relative path...
		 __path_JButton = new SimpleJButton(__RemoveWorkingDirectory,this);
	     JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	 7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	 }
     
     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Action if different:"),
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __IfDifferent_JComboBox = new SimpleJComboBox ( false );
     List<String> diffChoices = new ArrayList<String>();
     diffChoices.add ( "" ); // Default
     diffChoices.add ( __command._Ignore );
     diffChoices.add ( __command._Warn );
     diffChoices.add ( __command._Fail );
     __IfDifferent_JComboBox.setData(diffChoices);
     __IfDifferent_JComboBox.select ( 0 );
     __IfDifferent_JComboBox.addActionListener ( this );
     JGUIUtil.addComponent(main_JPanel, __IfDifferent_JComboBox,
         1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel(
         "Optional - action if tables are different (default=" + __command._Ignore + ")."), 
         3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Action if same:"),
         0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
     __IfSame_JComboBox = new SimpleJComboBox ( false );
     List<String> sameChoices = new ArrayList<String>();
     sameChoices.add ( "" );  // Default
     sameChoices.add ( __command._Ignore );
     sameChoices.add ( __command._Warn );
     sameChoices.add ( __command._Fail );
     __IfSame_JComboBox.setData(sameChoices);
     __IfSame_JComboBox.select ( 0 );
     __IfSame_JComboBox.addActionListener ( this );
     JGUIUtil.addComponent(main_JPanel, __IfSame_JComboBox,
         1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel(
         "Optional - action if tables are the same (default=" + __command._Ignore + ")."), 
         3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     
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
{	String routine = getClass().getName() + ".refresh";
    String Table1ID = "";
    String Table2ID = "";
    String CompareColumns1 = "";
    String ExcludeColumns1 = "";
    String CompareColumns2 = "";
    String MatchColumnsHow = "";
    String Precision = "";
    String Tolerance = "";
    String AllowedDiff = "";
    String NewTableID = "";
    String OutputFile = "";
    String IfDifferent = "";
    String IfSame = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        Table1ID = props.getValue ( "Table1ID" );
        Table2ID = props.getValue ( "Table2ID" );
        CompareColumns1 = props.getValue ( "CompareColumns1" );
        ExcludeColumns1 = props.getValue ( "ExcludeColumns1" );
        CompareColumns2 = props.getValue ( "CompareColumns2" );
        MatchColumnsHow = props.getValue ( "MatchColumnsHow" );
        Precision = props.getValue ( "Precision" );
        Tolerance = props.getValue ( "Tolerance" );
        AllowedDiff = props.getValue ( "AllowedDiff" );
        NewTableID = props.getValue ( "NewTableID" );
        OutputFile = props.getValue ( "OutputFile" );
        IfDifferent = props.getValue ( "IfDifferent" );
        IfSame = props.getValue ( "IfSame" );
        if ( Table1ID == null ) {
            // Select default...
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
            // Select default...
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
		if ( CompareColumns2 != null ) {
			__CompareColumns2_JTextField.setText ( CompareColumns2 );
		}
        if ( JGUIUtil.isSimpleJComboBoxItem(__MatchColumnsHow_JComboBox, MatchColumnsHow, JGUIUtil.NONE, null, null ) ) {
            __MatchColumnsHow_JComboBox.select ( MatchColumnsHow );
        }
        else {
            if ( (MatchColumnsHow == null) || MatchColumnsHow.equals("") ) {
                // New command...select the default...
                __MatchColumnsHow_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "MatchColumnsHow parameter \"" + MatchColumnsHow + "\".  Select a\ndifferent value or Cancel." );
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
        if ( NewTableID != null ) {
            __NewTableID_JTextField.setText ( NewTableID );
        }
        if ( OutputFile != null ) {
            __OutputFile_JTextField.setText (OutputFile);
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__IfDifferent_JComboBox, IfDifferent, JGUIUtil.NONE, null, null ) ) {
            __IfDifferent_JComboBox.select ( IfDifferent );
        }
        else {
            if ( (IfDifferent == null) || IfDifferent.equals("") ) {
                // New command...select the default...
                __IfDifferent_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "IfDifferent parameter \"" + IfDifferent + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if (    JGUIUtil.isSimpleJComboBoxItem(
            __IfSame_JComboBox, IfSame,
            JGUIUtil.NONE, null, null ) ) {
            __IfSame_JComboBox.select ( IfSame );
        }
        else {
            if ( (IfSame == null) || IfSame.equals("") ) {
                // New command...select the default...
                __IfSame_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "IfSame parameter \"" + IfSame + "\".  Select a\ndifferent value or Cancel." );
            }
        }
	}
	// Regardless, reset the command from the fields...
	Table1ID = __Table1ID_JComboBox.getSelected();
	Table2ID = __Table2ID_JComboBox.getSelected();
    CompareColumns1 = __CompareColumns1_JTextField.getText().trim();
    ExcludeColumns1 = __ExcludeColumns1_JTextField.getText().trim();
	CompareColumns2 = __CompareColumns2_JTextField.getText().trim();
	MatchColumnsHow = __MatchColumnsHow_JComboBox.getSelected();
    Precision = __Precision_JTextField.getText().trim();
    Tolerance = __Tolerance_JTextField.getText().trim();
    AllowedDiff = __AllowedDiff_JTextField.getText().trim();
    NewTableID = __NewTableID_JTextField.getText().trim();
    OutputFile = __OutputFile_JTextField.getText().trim();
    IfDifferent = __IfDifferent_JComboBox.getSelected();
    IfSame = __IfSame_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "Table1ID=" + Table1ID );
    props.add ( "Table2ID=" + Table2ID );
    props.add ( "CompareColumns1=" + CompareColumns1 );
    props.add ( "ExcludeColumns1=" + ExcludeColumns1 );
	props.add ( "CompareColumns2=" + CompareColumns2 );
	props.add ( "MatchColumnsHow=" + MatchColumnsHow );
	props.add ( "Precision=" + Precision );
	props.add ( "Tolerance=" + Tolerance );
	props.add ( "AllowedDiff=" + AllowedDiff );
    props.add ( "NewTableID=" + NewTableID );
    props.add ( "OutputFile=" + OutputFile );
    props.add ( "IfDifferent=" + IfDifferent );
    props.add ( "IfSame=" + IfSame );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( OutputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText (__RemoveWorkingDirectory);
			__path_JButton.setToolTipText("Change path to relative to command file");
		}
		else {
		    __path_JButton.setText (__AddWorkingDirectory );
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
