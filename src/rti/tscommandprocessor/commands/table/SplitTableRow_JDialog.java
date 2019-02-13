// SplitTableRow_JDialog - editor for SplitTableRow column

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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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
import java.util.ArrayList;
import java.util.List;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class SplitTableRow_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTabbedPane __main_JTabbedPane = null;
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private SimpleJComboBox __DeleteOriginalRow_JComboBox = null;
private JTextField __TupleColumns_JTextField = null;
private JTextField __TupleDateTimes_JTextField = null;
private JTextField __NewTupleColumns_JTextField = null;
private JTextField __NewTupleDateTimeColumn_JTextField = null;
private JTextField __InsertBeforeColumn_JTextField = null;
private JTextField __MeasureStartColumn_JTextField = null;
private JTextField __MeasureEndColumn_JTextField = null;
private JTextField __MeasureIncrement_JTextField = null;
private JTextField __MinimumStartSegmentLength_JTextField = null;
private JTextField __MinimumEndSegmentLength_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SplitTableRow_Command __command = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public SplitTableRow_JDialog ( JFrame parent, SplitTableRow_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

    if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "SplitTableRow");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
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
	String TupleColumns = __TupleColumns_JTextField.getText().trim();
	String TupleDateTimes = __TupleDateTimes_JTextField.getText().trim();
	String NewTupleColumns = __NewTupleColumns_JTextField.getText().trim();
	String NewTupleDateTimeColumn = __NewTupleDateTimeColumn_JTextField.getText().trim();
	String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	String MeasureStartColumn = __MeasureStartColumn_JTextField.getText().trim();
    String MeasureEndColumn = __MeasureEndColumn_JTextField.getText().trim();
	String MeasureIncrement = __MeasureIncrement_JTextField.getText().trim();
	String MinimumStartSegmentLength = __MinimumStartSegmentLength_JTextField.getText().trim();
	String MinimumEndSegmentLength = __MinimumEndSegmentLength_JTextField.getText().trim();
	String DeleteOriginalRow = __DeleteOriginalRow_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( TupleColumns.length() > 0 ) {
        props.set ( "TupleColumns", TupleColumns );
    }
    if ( TupleDateTimes.length() > 0 ) {
        props.set ( "TupleDateTimes", TupleDateTimes );
    }
    if ( NewTupleColumns.length() > 0 ) {
        props.set ( "NewTupleColumns", NewTupleColumns );
    }
    if ( NewTupleDateTimeColumn.length() > 0 ) {
        props.set ( "NewTupleDateTimeColumn", NewTupleDateTimeColumn );
    }
    if ( InsertBeforeColumn.length() > 0 ) {
        props.set ( "InsertBeforeColumn", InsertBeforeColumn );
    }
    if ( MeasureStartColumn.length() > 0 ) {
        props.set ( "MeasureStartColumn", MeasureStartColumn );
    }
    if ( MeasureEndColumn.length() > 0 ) {
        props.set ( "MeasureEndColumn", MeasureEndColumn );
    }
	if ( MeasureIncrement.length() > 0 ) {
		props.set ( "MeasureIncrement", MeasureIncrement );
	}
    if ( MinimumStartSegmentLength.length() > 0 ) {
        props.set ( "MinimumStartSegmentLength", MinimumStartSegmentLength );
    }
    if ( MinimumEndSegmentLength.length() > 0 ) {
        props.set ( "MinimumEndSegmentLength", MinimumEndSegmentLength );
    }
    if ( DeleteOriginalRow.length() > 0 ) {
        props.set ( "DeleteOriginalRow", DeleteOriginalRow );
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
	String TupleColumns = __TupleColumns_JTextField.getText().trim();
	String TupleDateTimes = __TupleDateTimes_JTextField.getText().trim();
	String NewTupleColumns = __NewTupleColumns_JTextField.getText().trim();
	String NewTupleDateTimeColumn = __NewTupleDateTimeColumn_JTextField.getText().trim();
	String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	String MeasureStartColumn = __MeasureStartColumn_JTextField.getText().trim();
	String MeasureEndColumn = __MeasureEndColumn_JTextField.getText().trim();
	String MeasureIncrement = __MeasureIncrement_JTextField.getText().trim();
	String MinimumStartSegmentLength = __MinimumStartSegmentLength_JTextField.getText().trim();
	String MinimumEndSegmentLength = __MinimumEndSegmentLength_JTextField.getText().trim();
	String DeleteOriginalRow = __DeleteOriginalRow_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "TupleColumns", TupleColumns );
    __command.setCommandParameter ( "TupleDateTimes", TupleDateTimes );
    __command.setCommandParameter ( "NewTupleColumns", NewTupleColumns );
    __command.setCommandParameter ( "NewTupleDateTimeColumn", NewTupleDateTimeColumn );
    __command.setCommandParameter ( "InsertBeforeColumn", InsertBeforeColumn );
    __command.setCommandParameter ( "MeasureStartColumn", MeasureStartColumn );
	__command.setCommandParameter ( "MeasureEndColumn", MeasureEndColumn );
	__command.setCommandParameter ( "MeasureIncrement", MeasureIncrement );
	__command.setCommandParameter ( "MinimumStartSegmentLength", MinimumStartSegmentLength );
	__command.setCommandParameter ( "MinimumEndSegmentLength", MinimumEndSegmentLength );
	__command.setCommandParameter ( "DeleteOriginalRow", DeleteOriginalRow );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, SplitTableRow_Command command, List<String> tableIDChoices )
{	__command = command;

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
        "This command takes information from a single table row and creates a sequence of rows, depending on approach."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table ID or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to process."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Delete original row:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DeleteOriginalRow_JComboBox = new SimpleJComboBox ( 12, false );
    List<String> choices = new ArrayList<String>();
    choices.add("");
    choices.add("" + __command._False);
    choices.add("" + __command._True);
    __DeleteOriginalRow_JComboBox.setData ( choices );
    __DeleteOriginalRow_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DeleteOriginalRow_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - delete original row (default=" + __command._False + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for tuples
    int yTuple = -1;
    JPanel tuple_JPanel = new JPanel();
    tuple_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Column Tuples", tuple_JPanel );

   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "Tuples are groups of related columns that can be reorganized into more general column names."),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "For example, specify column tuples as Column1,ColumnA;Column2,ColumnB;Column3,ColumnC"),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "and specify new column tuples as NewColumn1,NewColumnA."),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "Column1, Column2, and Column3 values will then be aligned under new NewColumn1"),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "and ColumnA, ColumnB, and ColumnC values will be aligned under new NewColumnA."),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JLabel (
        "If transposing multiple columns into a single column the TupleColumns will list original "
        + "columns and the NewTupleColumns will list the single new column."),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(tuple_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yTuple, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Tuple columns:"), 
        0, ++yTuple, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TupleColumns_JTextField = new JTextField (35);
    __TupleColumns_JTextField.setToolTipText("Specify as Column1,ColumnA;Column2,ColumnB - can use ${Property}");
    __TupleColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(tuple_JPanel, __TupleColumns_JTextField,
        1, yTuple, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Required - names of columns in each input tuple."),
        3, yTuple, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Tuple date/times:"), 
        0, ++yTuple, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TupleDateTimes_JTextField = new JTextField (35);
    __TupleDateTimes_JTextField.setToolTipText("The date/times that correspond to each input tuple - can use ${Property}");
    __TupleDateTimes_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(tuple_JPanel, __TupleDateTimes_JTextField,
        1, yTuple, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Optional - date/times for each input tuple."),
        3, yTuple, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("New tuple columns:"), 
        0, ++yTuple, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NewTupleColumns_JTextField = new JTextField (35);
    __NewTupleColumns_JTextField.setToolTipText("Specify as NewColumn1,NewColumnA - can use ${Property}");
    __NewTupleColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(tuple_JPanel, __NewTupleColumns_JTextField,
        1, yTuple, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Required - names of columns in new general tuple."),
        3, yTuple, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("New tuple date/time column:"), 
        0, ++yTuple, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NewTupleDateTimeColumn_JTextField = new JTextField (20);
    __NewTupleDateTimeColumn_JTextField.setToolTipText("New date/time column that will be filled with TupleDateTimes values, can use ${Property}");
    __NewTupleDateTimeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(tuple_JPanel, __NewTupleDateTimeColumn_JTextField,
        1, yTuple, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ("Optional - name of general column to add for tuple date/time."),
        3, yTuple, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(tuple_JPanel, new JLabel("Column to insert before:"),
        0, ++yTuple, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertBeforeColumn_JTextField = new JTextField ( "", 20 );
    __InsertBeforeColumn_JTextField.setToolTipText("Specify the table column to insert before or use ${Property} notation");
    __InsertBeforeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(tuple_JPanel, __InsertBeforeColumn_JTextField,
        1, yTuple, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(tuple_JPanel, new JLabel ( "Optional - column to insert before (default=insert at end)." ),
        3, yTuple, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for measure
    int yMeasure = -1;
    JPanel measure_JPanel = new JPanel();
    measure_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Distance Measure", measure_JPanel );
    
   	JGUIUtil.addComponent(measure_JPanel, new JLabel (
        "Create the row sequence by using an input row with start and end measure, for example stream reach endpoint distances."),
        0, ++yMeasure, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(measure_JPanel, new JLabel (
        "The output will be a sequence of rows, each with start and end measures that are increments of the full reach length."),
        0, ++yMeasure, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(measure_JPanel, new JLabel (
        "The other column values from the original row are duplicated in the new rows."),
        0, ++yMeasure, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(measure_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yMeasure, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Measure start column:"), 
        0, ++yMeasure, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MeasureStartColumn_JTextField = new JTextField (20);
    __MeasureStartColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(measure_JPanel, __MeasureStartColumn_JTextField,
        1, yMeasure, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Required - name of column containing measure."),
        3, yMeasure, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Measure end column:"), 
        0, ++yMeasure, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MeasureEndColumn_JTextField = new JTextField (20);
    __MeasureEndColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(measure_JPanel, __MeasureEndColumn_JTextField,
        1, yMeasure, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Required - name of column containing measure."),
        3, yMeasure, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Measure increment:"), 
        0, ++yMeasure, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MeasureIncrement_JTextField = new JTextField (10);
    __MeasureIncrement_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(measure_JPanel, __MeasureIncrement_JTextField,
        1, yMeasure, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Required - measure increment."),
        3, yMeasure, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Minimum start segment length:"), 
        0, ++yMeasure, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MinimumStartSegmentLength_JTextField = new JTextField (10);
    __MinimumStartSegmentLength_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(measure_JPanel, __MinimumStartSegmentLength_JTextField,
        1, yMeasure, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Optional - minimum segment length (default=include start segment)."),
        3, yMeasure, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Minimum end segment length:"), 
        0, ++yMeasure, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MinimumEndSegmentLength_JTextField = new JTextField (10);
    __MinimumEndSegmentLength_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(measure_JPanel, __MinimumEndSegmentLength_JTextField,
        1, yMeasure, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(measure_JPanel, new JLabel ("Optional - minimum segment length (default=include end segment)."),
        3, yMeasure, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

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
    String TableID = "";
    String TupleColumns = "";
    String TupleDateTimes = "";
    String NewTupleColumns = "";
    String NewTupleDateTimeColumn = "";
    String InsertBeforeColumn = "";
    String MeasureStartColumn = "";
    String MeasureEndColumn = "";
    String MeasureIncrement = "";
    String MinimumStartSegmentLength = "";
    String MinimumEndSegmentLength = "";
    String DeleteOriginalRow = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        TupleColumns = props.getValue ( "TupleColumns" );
        TupleDateTimes = props.getValue ( "TupleDateTimes" );
        NewTupleColumns = props.getValue ( "NewTupleColumns" );
        NewTupleDateTimeColumn = props.getValue ( "NewTupleDateTimeColumn" );
        InsertBeforeColumn = props.getValue ( "InsertBeforeColumn" );
        MeasureEndColumn = props.getValue ( "MeasureEndColumn" );
        MeasureStartColumn = props.getValue ( "MeasureStartColumn" );
        MeasureIncrement = props.getValue ( "MeasureIncrement" );
        MinimumStartSegmentLength = props.getValue ( "MinimumStartSegmentLength" );
        MinimumEndSegmentLength = props.getValue ( "MinimumEndSegmentLength" );
        DeleteOriginalRow = props.getValue ( "DeleteOriginalRow" );
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
        if ( (TupleColumns != null) && !TupleColumns.isEmpty() ) {
            __TupleColumns_JTextField.setText ( TupleColumns );
            __main_JTabbedPane.setSelectedIndex(0);
        }
        if ( TupleDateTimes != null ) {
            __TupleDateTimes_JTextField.setText ( TupleDateTimes );
        }
        if ( NewTupleColumns != null ) {
            __NewTupleColumns_JTextField.setText ( NewTupleColumns );
        }
        if ( NewTupleDateTimeColumn != null ) {
            __NewTupleDateTimeColumn_JTextField.setText ( NewTupleDateTimeColumn );
        }
        if ( InsertBeforeColumn != null ) {
            __InsertBeforeColumn_JTextField.setText ( InsertBeforeColumn );
        }
        if ( (MeasureStartColumn != null) && !MeasureStartColumn.isEmpty() ) {
            __MeasureStartColumn_JTextField.setText ( MeasureStartColumn );
            __main_JTabbedPane.setSelectedIndex(1);
        }
        if ( MeasureEndColumn != null ) {
            __MeasureEndColumn_JTextField.setText ( MeasureEndColumn );
        }
		if ( MeasureIncrement != null ) {
			__MeasureIncrement_JTextField.setText ( MeasureIncrement );
		}
        if ( MinimumStartSegmentLength != null ) {
            __MinimumStartSegmentLength_JTextField.setText ( MinimumStartSegmentLength );
        }
        if ( MinimumEndSegmentLength != null ) {
            __MinimumEndSegmentLength_JTextField.setText ( MinimumEndSegmentLength );
        }
        if ( DeleteOriginalRow == null ) {
            // Select default...
            __DeleteOriginalRow_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __DeleteOriginalRow_JComboBox,DeleteOriginalRow, JGUIUtil.NONE, null, null ) ) {
                __DeleteOriginalRow_JComboBox.select ( DeleteOriginalRow );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nDeleteOriginalRow value \"" + DeleteOriginalRow +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	TupleColumns = __TupleColumns_JTextField.getText().trim();
	TupleDateTimes = __TupleDateTimes_JTextField.getText().trim();
	NewTupleColumns = __NewTupleColumns_JTextField.getText().trim();
	NewTupleDateTimeColumn = __NewTupleDateTimeColumn_JTextField.getText().trim();
	InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	MeasureStartColumn = __MeasureStartColumn_JTextField.getText().trim();
	MeasureEndColumn = __MeasureEndColumn_JTextField.getText().trim();
	MeasureIncrement = __MeasureIncrement_JTextField.getText().trim();
	MinimumStartSegmentLength = __MinimumStartSegmentLength_JTextField.getText().trim();
	MinimumEndSegmentLength = __MinimumEndSegmentLength_JTextField.getText().trim();
	DeleteOriginalRow = __DeleteOriginalRow_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "TupleColumns=" + TupleColumns );
    props.add ( "TupleDateTimes=" + TupleDateTimes );
    props.add ( "NewTupleColumns=" + NewTupleColumns );
    props.add ( "NewTupleDateTimeColumn=" + NewTupleDateTimeColumn );
    props.add ( "InsertBeforeColumn=" + InsertBeforeColumn );
	props.add ( "MeasureStartColumn=" + MeasureStartColumn );
    props.add ( "MeasureEndColumn=" + MeasureEndColumn );
	props.add ( "MeasureIncrement=" + MeasureIncrement );
	props.add ( "MinimumStartSegmentLength=" + MinimumStartSegmentLength );
	props.add ( "MinimumEndSegmentLength=" + MinimumEndSegmentLength );
	props.add ( "DeleteOriginalRow=" + DeleteOriginalRow );
	__command_JTextArea.setText( __command.toString ( props ) );
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
public void windowActivated(WindowEvent evt){}
public void windowClosed(WindowEvent evt){}
public void windowDeactivated(WindowEvent evt){}
public void windowDeiconified(WindowEvent evt){}
public void windowIconified(WindowEvent evt){}
public void windowOpened(WindowEvent evt){}

}
