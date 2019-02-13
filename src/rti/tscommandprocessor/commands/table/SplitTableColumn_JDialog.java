// SplitTableColumn_JDialog - editor for SplitTableColumn command

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
public class SplitTableColumn_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __ColumnToSplit_JTextField = null;
private JTextField __Delimiter_JTextField = null;
private JTextField __OutputColumns_JTextField = null;
private SimpleJComboBox __OutputColumnOrder_JComboBox = null;
private JTextField __InsertBeforeColumn_JTextField = null;
private SimpleJComboBox __DeleteOriginalColumn_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SplitTableColumn_Command __command = null;
//private JFrame __parent = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public SplitTableColumn_JDialog ( JFrame parent, SplitTableColumn_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "SplitTableColumn");
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
	String ColumnToSplit = __ColumnToSplit_JTextField.getText().trim();
    String Delimiter = __Delimiter_JTextField.getText().trim();
	String OutputColumns = __OutputColumns_JTextField.getText().trim();
	String OutputColumnOrder = __OutputColumnOrder_JComboBox.getSelected();
    String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	String DeleteOriginalColumn = __DeleteOriginalColumn_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( ColumnToSplit.length() > 0 ) {
        props.set ( "ColumnToSplit", ColumnToSplit );
    }
    if ( Delimiter.length() > 0 ) {
        props.set ( "Delimiter", Delimiter );
    }
	if ( OutputColumns.length() > 0 ) {
		props.set ( "OutputColumns", OutputColumns );
	}
    if ( OutputColumnOrder.length() > 0 ) {
        props.set ( "OutputColumnOrder", OutputColumnOrder );
    }
    if ( InsertBeforeColumn.length() > 0 ) {
    	props.set ( "InsertBeforeColumn", InsertBeforeColumn );
    }
    if ( DeleteOriginalColumn.length() > 0 ) {
        props.set ( "DeleteOriginalColumn", DeleteOriginalColumn );
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
	String ColumnToSplit = __ColumnToSplit_JTextField.getText().trim();
	String Delimiter = __Delimiter_JTextField.getText().trim();
	String OutputColumns = __OutputColumns_JTextField.getText().trim();
	String OutputColumnOrder = __OutputColumnOrder_JComboBox.getSelected();
    String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	String DeleteOriginalColumn = __DeleteOriginalColumn_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "ColumnToSplit", ColumnToSplit );
	__command.setCommandParameter ( "Delimiter", Delimiter );
	__command.setCommandParameter ( "OutputColumns", OutputColumns );
	__command.setCommandParameter ( "OutputColumnOrder", OutputColumnOrder );
	__command.setCommandParameter ( "InsertBeforeColumn", InsertBeforeColumn );
	__command.setCommandParameter ( "DeleteOriginalColumn", DeleteOriginalColumn );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, SplitTableColumn_Command command, List<String> tableIDChoices )
{	__command = command;
    //__parent = parent;

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
        "This command splits a table column containing delimited content into multiple columns."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The new column names can be specified explicitly or use the notation Abc[N:] or Abc[N:N] to specify a number sequence for the column names."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "For example, output columns \"ColumnA,Column[1:]\" will result in output columns ColumnA, Column1, Column2, ..."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Values that are split are transferred to the output columns according to the OutputColumnOrder parameter."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true );    // Allow edit
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - original table."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name of column to split:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnToSplit_JTextField = new JTextField (20);
    __ColumnToSplit_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ColumnToSplit_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - name of column to be split."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Delimiter:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Delimiter_JTextField = new JTextField (10);
    __Delimiter_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Delimiter_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - delimiter to split column contents."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Output column name(s):"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputColumns_JTextField = new JTextField (30);
    __OutputColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __OutputColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - name(s) of output columns."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output column order:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputColumnOrder_JComboBox = new SimpleJComboBox ( 12, false );
    List<String> orderChoices = new ArrayList<String>();
    orderChoices.add("");
    orderChoices.add("" + __command._SequenceOrder);
    orderChoices.add("" + __command._UniqueValues);
    orderChoices.add("" + __command._UniqueValuesSorted);
    __OutputColumnOrder_JComboBox.setData ( orderChoices );
    __OutputColumnOrder_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __OutputColumnOrder_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - output value order in output columns (default=" + __command._SequenceOrder + ")."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Insert before column:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertBeforeColumn_JTextField = new JTextField ( 30 );
    __InsertBeforeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InsertBeforeColumn_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Optional - column to insert before (default=at end)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Delete original column:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DeleteOriginalColumn_JComboBox = new SimpleJComboBox ( 12, false );
    List<String> choices = new ArrayList<String>();
    choices.add("");
    choices.add("" + __command._False);
    choices.add("" + __command._True);
    __DeleteOriginalColumn_JComboBox.setData ( choices );
    __DeleteOriginalColumn_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DeleteOriginalColumn_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - delete original column (default=" + __command._False + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
    String ColumnToSplit = "";
    String Delimiter = "";
    String OutputColumns = "";
    String OutputColumnOrder = "";
    String InsertBeforeColumn = "";
    String DeleteOriginalColumn = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        Delimiter = props.getValue ( "Delimiter" );
        ColumnToSplit = props.getValue ( "ColumnToSplit" );
        OutputColumns = props.getValue ( "OutputColumns" );
        OutputColumnOrder = props.getValue ( "OutputColumnOrder" );
		InsertBeforeColumn = props.getValue ( "InsertBeforeColumn" );
        DeleteOriginalColumn = props.getValue ( "DeleteOriginalColumn" );
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
        if ( ColumnToSplit != null ) {
            __ColumnToSplit_JTextField.setText ( ColumnToSplit );
        }
        if ( Delimiter != null ) {
            __Delimiter_JTextField.setText ( Delimiter );
        }
		if ( OutputColumns != null ) {
			__OutputColumns_JTextField.setText ( OutputColumns );
		}
        if ( OutputColumnOrder == null ) {
            // Select default...
            __OutputColumnOrder_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputColumnOrder_JComboBox,OutputColumnOrder, JGUIUtil.NONE, null, null ) ) {
                __OutputColumnOrder_JComboBox.select ( OutputColumnOrder );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nOutputColumnOrder value \"" + OutputColumnOrder +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( InsertBeforeColumn != null ) {
            __InsertBeforeColumn_JTextField.setText ( InsertBeforeColumn );
        }
        if ( DeleteOriginalColumn == null ) {
            // Select default...
            __DeleteOriginalColumn_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __DeleteOriginalColumn_JComboBox,DeleteOriginalColumn, JGUIUtil.NONE, null, null ) ) {
                __DeleteOriginalColumn_JComboBox.select ( DeleteOriginalColumn );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nDeleteOriginalColumn value \"" + DeleteOriginalColumn +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	ColumnToSplit = __ColumnToSplit_JTextField.getText().trim();
	Delimiter = __Delimiter_JTextField.getText().trim();
	OutputColumns = __OutputColumns_JTextField.getText().trim();
	OutputColumnOrder = __OutputColumnOrder_JComboBox.getSelected();
    InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText();
	DeleteOriginalColumn = __DeleteOriginalColumn_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "ColumnToSplit=" + ColumnToSplit );
    props.add ( "Delimiter=" + Delimiter );
	props.add ( "OutputColumns=" + OutputColumns );
	props.add ( "OutputColumnOrder=" + OutputColumnOrder );
    props.add ( "InsertBeforeColumn=" + InsertBeforeColumn );
	props.add ( "DeleteOriginalColumn=" + DeleteOriginalColumn );
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
