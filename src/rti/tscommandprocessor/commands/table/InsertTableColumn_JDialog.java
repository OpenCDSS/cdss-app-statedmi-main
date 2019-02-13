// InsertTableColumn_JDialog - editor for InsertTableColumn command

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



//import RTi.Util.GUI.DictionaryJDialog;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTableFunctionType;
import RTi.Util.Table.TableColumnType;

@SuppressWarnings("serial")
public class InsertTableColumn_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __InsertColumn_JTextField = null;
private JTextField __InsertBeforeColumn_JTextField = null;
private SimpleJComboBox __ColumnType_JComboBox = null;
private JTextField __InitialValue_JTextField = null;
private SimpleJComboBox __InitialFunction_JComboBox = null;
private JTextField __ColumnWidth_JTextField = null;
private JTextField __ColumnPrecision_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private InsertTableColumn_Command __command = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public InsertTableColumn_JDialog ( JFrame parent, InsertTableColumn_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "InsertTableColumn");
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
	String InsertColumn = __InsertColumn_JTextField.getText().trim();
	String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
	String ColumnType = __ColumnType_JComboBox.getSelected();
	String InitialValue = __InitialValue_JTextField.getText().trim();
	String InitialFunction = __InitialFunction_JComboBox.getSelected();
	String ColumnWidth = __ColumnWidth_JTextField.getText().trim();
	String ColumnPrecision = __ColumnPrecision_JTextField.getText().trim();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( InsertColumn.length() > 0 ) {
        props.set ( "InsertColumn", InsertColumn );
    }
    if ( InsertBeforeColumn.length() > 0 ) {
        props.set ( "InsertBeforeColumn", InsertBeforeColumn );
    }
    if ( ColumnType.length() > 0 ) {
        props.set ( "ColumnType", ColumnType );
    }
    if ( InitialValue.length() > 0 ) {
        props.set ( "InitialValue", InitialValue );
    }
    if ( (InitialFunction != null) && (InitialFunction.length() > 0) ) {
        props.set ( "InitialFunction", InitialFunction );
    }
    if ( ColumnWidth.length() > 0 ) {
        props.set ( "ColumnWidth", ColumnWidth );
    }
    if ( ColumnPrecision.length() > 0 ) {
        props.set ( "ColumnPrecision", ColumnPrecision );
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
    String InsertColumn = __InsertColumn_JTextField.getText().trim();
    String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
    String ColumnType = __ColumnType_JComboBox.getSelected();
    String InitialValue = __InitialValue_JTextField.getText().trim();
	String InitialFunction = __InitialFunction_JComboBox.getSelected();
    String ColumnWidth = __ColumnWidth_JTextField.getText().trim();
    String ColumnPrecision = __ColumnPrecision_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "InsertColumn", InsertColumn );
    __command.setCommandParameter ( "InsertBeforeColumn", InsertBeforeColumn );
    __command.setCommandParameter ( "ColumnType", ColumnType );
    __command.setCommandParameter ( "InitialValue", InitialValue );
    __command.setCommandParameter ( "InitialFunction", InitialFunction );
    __command.setCommandParameter ( "ColumnWidth", ColumnWidth );
    __command.setCommandParameter ( "ColumnPrecision", ColumnPrecision );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, InsertTableColumn_Command command, List<String> tableIDChoices )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    
   	JGUIUtil.addComponent(paragraph, new JLabel (
        "This command inserts a new column into a table."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The column is initialized with blank (null) values unless an initial value or function is provided."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit because sometimes table is not available
    __TableID_JComboBox.setToolTipText("Specify the table identifier or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    __TableID_JComboBox.addKeyListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table for insert."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel("Insert column:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertColumn_JTextField = new JTextField ( "", 20 );
    __InsertColumn_JTextField.setToolTipText("Specify the table column to insert or use ${Property} notation");
    __InsertColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InsertColumn_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - name of column to insert." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Column to insert before:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertBeforeColumn_JTextField = new JTextField ( "", 20 );
    __InsertBeforeColumn_JTextField.setToolTipText("Specify the table column to insert before or use ${Property} notation");
    __InsertBeforeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InsertBeforeColumn_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column to insert before (default=insert at end)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Column type:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnType_JComboBox = new SimpleJComboBox ( 12, false );
    List<String> typeChoices = new ArrayList<String>();
    typeChoices.add("");
    typeChoices.add(TableColumnType.DateTime.toString());
    typeChoices.add(TableColumnType.DOUBLE.toString());
    typeChoices.add(TableColumnType.FLOAT.toString());
    typeChoices.add(TableColumnType.INT.toString());
    typeChoices.add(TableColumnType.LONG.toString());
    typeChoices.add(TableColumnType.SHORT.toString());
    typeChoices.add(TableColumnType.STRING.toString());
    __ColumnType_JComboBox.setData ( typeChoices );
    __ColumnType_JComboBox.addItemListener ( this );
    __ColumnType_JComboBox.setMaximumRowCount(typeChoices.size());
    JGUIUtil.addComponent(main_JPanel, __ColumnType_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - column type (default=" + TableColumnType.STRING + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Initial value:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InitialValue_JTextField = new JTextField ( "", 10 );
    __InitialValue_JTextField.setToolTipText("Specify the initial column value or use ${Property} notation");
    __InitialValue_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InitialValue_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - initial value (default=null)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Initial function:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InitialFunction_JComboBox = new SimpleJComboBox(false);
    List<DataTableFunctionType> functionTypes = __command.getFunctionChoices();
    __InitialFunction_JComboBox.add ( "" );
    for ( DataTableFunctionType functionType : functionTypes ) {
        __InitialFunction_JComboBox.add ( "" + functionType );
    }
    __InitialFunction_JComboBox.select ( 0 );
    __InitialFunction_JComboBox.addActionListener (this);
    JGUIUtil.addComponent(main_JPanel, __InitialFunction_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - function to initialize data (default=initial value)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel("Column width (if string):"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnWidth_JTextField = new JTextField ( "", 10 );
    __ColumnWidth_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ColumnWidth_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column width if string (default=dynamic)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Column precision:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnPrecision_JTextField = new JTextField ( "", 10 );
    __ColumnPrecision_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ColumnPrecision_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column precision if floating point (default=6)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
    String InsertColumn = "";
    String InsertBeforeColumn = "";
    String ColumnType = "";
    String InitialValue = "";
    String InitialFunction = "";
    String ColumnWidth = "";
    String ColumnPrecision = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        InsertColumn = props.getValue ( "InsertColumn" );
        InsertBeforeColumn = props.getValue ( "InsertBeforeColumn" );
        ColumnType = props.getValue ( "ColumnType" );
        InitialValue = props.getValue ( "InitialValue" );
        InitialFunction = props.getValue ( "InitialFunction" );
        ColumnWidth = props.getValue ( "ColumnWidth" );
        ColumnPrecision = props.getValue ( "ColumnPrecision" );
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
        if ( InsertColumn != null ) {
            __InsertColumn_JTextField.setText ( InsertColumn );
        }
        if ( InsertBeforeColumn != null ) {
            __InsertBeforeColumn_JTextField.setText ( InsertBeforeColumn );
        }
        if ( ColumnType == null ) {
            // Select default...
            __ColumnType_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __ColumnType_JComboBox,ColumnType, JGUIUtil.NONE, null, -1, null, true ) ) {
                __ColumnType_JComboBox.select ( ColumnType );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nColumnType value \"" + ColumnType +
                "\".  Select a different value or Cancel.");
                __ColumnType_JComboBox.select ( 0 );
                __error_wait = true;
            }
        }
        if ( InitialValue != null ) {
            __InitialValue_JTextField.setText ( InitialValue );
        }
        if ( InitialFunction == null ) {
            // Select default...
            __InitialFunction_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __InitialFunction_JComboBox, InitialFunction, JGUIUtil.NONE, null, null ) ) {
                __InitialFunction_JComboBox.select ( InitialFunction );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nInitialFunction value \"" +
                InitialFunction + "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( ColumnWidth != null ) {
            __ColumnWidth_JTextField.setText ( ColumnWidth );
        }
        if ( ColumnPrecision != null ) {
            __ColumnPrecision_JTextField.setText ( ColumnPrecision );
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
    InsertColumn = __InsertColumn_JTextField.getText().trim();
    InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
    ColumnType = __ColumnType_JComboBox.getSelected();
    InitialValue = __InitialValue_JTextField.getText().trim();
    InitialFunction = __InitialFunction_JComboBox.getSelected();
    ColumnWidth = __ColumnWidth_JTextField.getText().trim();
    ColumnPrecision = __ColumnPrecision_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "InsertColumn=" + InsertColumn );
    props.add ( "InsertBeforeColumn=" + InsertBeforeColumn );
    props.add ( "ColumnType=" + ColumnType );
    props.add ( "InitialValue=" + InitialValue );
    props.add ( "InitialFunction=" + InitialFunction );
    props.add ( "ColumnWidth=" + ColumnWidth );
    props.add ( "ColumnPrecision=" + ColumnPrecision );
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
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}
