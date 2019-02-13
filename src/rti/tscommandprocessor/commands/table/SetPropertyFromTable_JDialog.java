// SetPropertyFromTable_JDialog - editor for SetPropertyFromTable command

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
import java.util.List;

import RTi.Util.GUI.DictionaryJDialog;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class SetPropertyFromTable_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __Column_JTextField = null;
private JTextArea __ColumnIncludeFilters_JTextArea = null;
private JTextArea __ColumnExcludeFilters_JTextArea = null;
private JTextField __PropertyName_JTextField = null;
private JTextField __DefaultValue_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SetPropertyFromTable_Command __command = null;
private JFrame __parent = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public SetPropertyFromTable_JDialog ( JFrame parent, SetPropertyFromTable_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "SetPropertyFromTable");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
		}
	}
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnIncludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnFilters = __ColumnIncludeFilters_JTextArea.getText().trim();
        String [] notes = {
            "Match rows in the table by filtering on column values.",
            "All specified conditions must be true to include the row.",
            "Column Name - column name in the table to filter (can use ${property})",
            "Column Value Filter Pattern - a literal value to match (to include), or a pattern using * as a wildcard (can use ${property})"
        };
        String columnFilters = (new DictionaryJDialog ( __parent, true, ColumnFilters, "Edit ColumnFilters Parameter",
            notes, "Column Name", "Column Value Include Filter Pattern",10)).response();
        if ( columnFilters != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( columnFilters );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnExcludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
        String [] notes = {
            "Rows in the table can be excluded by filtering on column values.",
            "All specified conditions must be true to exclude the row.",
            "Column Name - column name in the table to filter (can use ${property})",
            "Column Value Filter Pattern - a literal value to match (to exclude), or a pattern using * as a wildcard (can use ${property})",
            "(specify blank to filter out columns with no values)"
        };
        String columnExcludeFilters = (new DictionaryJDialog ( __parent, true, ColumnExcludeFilters, "Edit ColumnExcludeFilters Parameter",
            notes, "Column Name", "Column Value Filter Pattern",10)).response();
        if ( columnExcludeFilters != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( columnExcludeFilters );
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
	String Column = __Column_JTextField.getText().trim();
	String ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim().replace("\n"," ");
	String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
    String PropertyName = __PropertyName_JTextField.getText().trim();
    String DefaultValue = __DefaultValue_JTextField.getText().trim();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
	if ( Column.length() > 0 ) {
		props.set ( "Column", Column );
	}
    if ( ColumnIncludeFilters.length() > 0 ) {
        props.set ( "ColumnIncludeFilters", ColumnIncludeFilters );
    }
    if ( ColumnExcludeFilters.length() > 0 ) {
        props.set ( "ColumnExcludeFilters", ColumnExcludeFilters );
    }
    if ( PropertyName.length() > 0 ) {
        props.set ( "PropertyName", PropertyName );
    }
    if ( DefaultValue.length() > 0 ) {
        props.set ( "DefaultValue", DefaultValue );
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
    String Column = __Column_JTextField.getText().trim();
    String ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim().replace("\n"," ");
    String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
    String PropertyName = __PropertyName_JTextField.getText().trim();
    String DefaultValue = __DefaultValue_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "Column", Column );
	__command.setCommandParameter ( "ColumnIncludeFilters", ColumnIncludeFilters );
	__command.setCommandParameter ( "ColumnExcludeFilters", ColumnExcludeFilters );
    __command.setCommandParameter ( "PropertyName", PropertyName );
    __command.setCommandParameter ( "DefaultValue", DefaultValue );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, SetPropertyFromTable_Command command, List<String> tableIDChoices )
{	__command = command;
    __parent = parent;

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
        "This command sets a processor property using a value from a table cell."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "This is useful when iteration uses a property value."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Using the filter parameters to match row(s) may result in multiple matches - " +
        "only the first match is used to set the property."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

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
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table that provides the property value."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column to supply property:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Column_JTextField = new JTextField (20);
    __Column_JTextField.setToolTipText("Specify the column to provide the property or use ${Property} notation");
    __Column_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Column_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - name of column that supplies the property value (can use ${property}."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column include filters:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnIncludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnIncludeFilters_JTextArea.setLineWrap ( true );
    __ColumnIncludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnIncludeFilters_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2, can use ${Property}.");
    __ColumnIncludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnIncludeFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - filter rows to include by matching column filter pattern (default=match all rows)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnIncludeFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column exclude filters:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnExcludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnExcludeFilters_JTextArea.setLineWrap ( true );
    __ColumnExcludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnExcludeFilters_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2, can use ${Property}");
    __ColumnExcludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnExcludeFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - filter rows to exclude by matching column filter pattern (default=match all rows)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnExcludeFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Property name:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PropertyName_JTextField = new JTextField (20);
    __PropertyName_JTextField.setToolTipText("Specify the property name to set or use ${Property} notation");
    __PropertyName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PropertyName_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - property name to set (can use ${property})."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Default value:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DefaultValue_JTextField = new JTextField (20);
    __DefaultValue_JTextField.setToolTipText("Specify the property default value or use ${Property} notation");
    __DefaultValue_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DefaultValue_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - Default value, ${property}, Blank, or Null (default=property not set)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

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
{	String routine = getClass().getName() + ".refresh";
    String TableID = "";
    String Column = "";
    String ColumnIncludeFilters = "";
    String ColumnExcludeFilters = "";
    String PropertyName = "";
    String DefaultValue = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        Column = props.getValue ( "Column" );
        ColumnIncludeFilters = props.getValue ( "ColumnIncludeFilters" );
        ColumnExcludeFilters = props.getValue ( "ColumnExcludeFilters" );
        PropertyName = props.getValue ( "PropertyName" );
        DefaultValue = props.getValue ( "DefaultValue" );
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
		if ( Column != null ) {
			__Column_JTextField.setText ( Column );
		}
        if ( ColumnIncludeFilters != null ) {
            __ColumnIncludeFilters_JTextArea.setText ( ColumnIncludeFilters );
        }
        if ( ColumnExcludeFilters != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( ColumnExcludeFilters );
        }
        if ( PropertyName != null ) {
            __PropertyName_JTextField.setText ( PropertyName );
        }
        if ( DefaultValue != null ) {
            __DefaultValue_JTextField.setText ( DefaultValue );
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	Column = __Column_JTextField.getText().trim();
	ColumnIncludeFilters = __ColumnIncludeFilters_JTextArea.getText().trim().replace("\n"," ");
	ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
    PropertyName = __PropertyName_JTextField.getText().trim();
    DefaultValue = __DefaultValue_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "Column=" + Column );
	props.add ( "ColumnIncludeFilters=" + ColumnIncludeFilters );
	props.add ( "ColumnExcludeFilters=" + ColumnExcludeFilters );
    props.add ( "PropertyName=" + PropertyName );
    props.add ( "DefaultValue=" + DefaultValue );
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
