// SortTable_JDialog - editor for SetTable command

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
public class SortTable_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __SortColumns_JTextField = null;
private JTextArea __SortOrder_JTextArea = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SortTable_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public SortTable_JDialog ( JFrame parent, SortTable_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "SortTable");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
		}
	}
    else if ( event.getActionCommand().equalsIgnoreCase("EditSortOrder") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String SortOrder = __SortOrder_JTextArea.getText().trim();
        String SortColumns = __SortColumns_JTextField.getText().trim();
        String [] notes = {
            "Specify the sort order corresponding to columns to be sorted:",
            "    " + SortColumns,
            "The default sort order is " + __command._Ascending + ".",
            "Column Name - column name in the table to sort.",
            "Sort Order - Specify as " + __command._Ascending + " (default) or " + __command._Descending + "."
        };
        String sortOrder = (new DictionaryJDialog ( __parent, true, SortOrder, "Edit SortOrder Parameter",
            notes, "Column Name", "Sort Order",10)).response();
        if ( sortOrder != null ) {
            __SortOrder_JTextArea.setText ( sortOrder );
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
	String SortColumns = __SortColumns_JTextField.getText().trim();
	String SortOrder = __SortOrder_JTextArea.getText().trim().replace("\n"," ");
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
	if ( SortColumns.length() > 0 ) {
		props.set ( "SortColumns", SortColumns );
	}
    if ( SortOrder.length() > 0 ) {
        props.set ( "SortOrder", SortOrder );
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
    String SortColumns = __SortColumns_JTextField.getText().trim();
    String SortOrder = __SortOrder_JTextArea.getText().trim().replace("\n"," ");
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "SortColumns", SortColumns );
	__command.setCommandParameter ( "SortOrder", SortOrder );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, SortTable_Command command, List<String> tableIDChoices )
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
        "This command sorts the rows in a table by sorting values from one or more specified columns."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Sorting has been implemented for columns with data types:  integer, float, double, string (case is ignored), and date/time."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true );    // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table ID or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to sort."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name(s) of column(s) to sort:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SortColumns_JTextField = new JTextField (10);
    __SortColumns_JTextField.setToolTipText("Specify the columns to sort or use ${Property} notation");
    __SortColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __SortColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - separate by commas if more than one column."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Sort order:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SortOrder_JTextArea = new JTextArea (3,35);
    __SortOrder_JTextArea.setLineWrap ( true );
    __SortOrder_JTextArea.setWrapStyleWord ( true );
    __SortOrder_JTextArea.setToolTipText("ColumnName1:SortOrder1,ColumnName2:SortOrder2");
    __SortOrder_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__SortOrder_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - sort order for columns being sorted (default=" + __command._Ascending + ")."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditSortOrder",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
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
    String SortColumns = "";
    String SortOrder = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        SortColumns = props.getValue ( "SortColumns" );
        SortOrder = props.getValue ( "SortOrder" );
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
		if ( SortColumns != null ) {
			__SortColumns_JTextField.setText ( SortColumns );
		}
        if ( SortOrder != null ) {
            __SortOrder_JTextArea.setText ( SortOrder );
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	SortColumns = __SortColumns_JTextField.getText().trim();
	SortOrder = __SortOrder_JTextArea.getText().trim().replace("\n"," ");
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
	props.add ( "SortColumns=" + SortColumns );
	props.add ( "SortOrder=" + SortOrder );
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
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
