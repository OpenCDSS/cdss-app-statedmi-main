// CopyPropertiesToTable_JDialog - editor for CopyPropertiesToTable command

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class CopyPropertiesToTable_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private CopyPropertiesToTable_Command __command = null;
private JTextArea __command_JTextArea = null;
private JTextField __IncludeProperties_JTextField = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __TableLookupColumn_JTextField = null;
private JTextField __TableLookupValue_JTextField = null; // Format for time series identifiers
private SimpleJComboBox __AllowDuplicates_JComboBox = null;
private JTextField __TableOutputColumns_JTextField = null;
private boolean __error_wait = false; // Is there an error to be cleared up or Cancel?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether OK button has been pressed.

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices choices for TableID value.
*/
public CopyPropertiesToTable_JDialog ( JFrame parent, CopyPropertiesToTable_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "CopyPropertiesToTable");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

// Start event handlers for DocumentListener...

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

// ...End event handlers for DocumentListener

/**
Check the GUI state to make sure that appropriate components are enabled/disabled.
*/
private void checkGUIState ()
{
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
    String IncludeProperties = __IncludeProperties_JTextField.getText().trim();
    String TableID = __TableID_JComboBox.getSelected();
    String TableLookupColumn = __TableLookupColumn_JTextField.getText().trim();
    String TableLookupValue = __TableLookupValue_JTextField.getText().trim();
    String AllowDuplicates = __AllowDuplicates_JComboBox.getSelected();
    String TableOutputColumns = __TableOutputColumns_JTextField.getText().trim();
	PropList parameters = new PropList ( "" );

	__error_wait = false;

    if ( IncludeProperties.length() > 0 ) {
        parameters.set ( "IncludeProperties", IncludeProperties );
    }
    if ( TableID.length() > 0 ) {
        parameters.set ( "TableID", TableID );
    }
    if ( TableLookupColumn.length() > 0 ) {
        parameters.set ( "TableLookupColumn", TableLookupColumn );
    }
    if ( TableLookupValue.length() > 0 ) {
        parameters.set ( "TableLookupValue", TableLookupValue );
    }
    if ( AllowDuplicates.length() > 0 ) {
        parameters.set ( "AllowDuplicates", AllowDuplicates );
    }
    if ( TableOutputColumns.length() > 0 ) {
        parameters.set ( "TableOutputColumns", TableOutputColumns );
    }

	try {
	    // This will warn the user...
		__command.checkCommandParameters ( parameters, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String IncludeProperties = __IncludeProperties_JTextField.getText().trim();
    String TableID = __TableID_JComboBox.getSelected();
    String TableLookupColumn = __TableLookupColumn_JTextField.getText().trim();
    String TableLookupValue = __TableLookupValue_JTextField.getText().trim();
    String AllowDuplicates = __AllowDuplicates_JComboBox.getSelected();
    String TableOutputColumns = __TableOutputColumns_JTextField.getText().trim();
    __command.setCommandParameter ( "IncludeProperties", IncludeProperties );
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "TableLookupColumn", TableLookupColumn );
    __command.setCommandParameter ( "TableLookupValue", TableLookupValue );
    __command.setCommandParameter ( "AllowDuplicates", AllowDuplicates );
    __command.setCommandParameter ( "TableOutputColumns", TableOutputColumns );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param command The command to edit.
@param tableIDChoices list of choices for table identifiers
*/
private void initialize ( JFrame parent, CopyPropertiesToTable_Command command, List<String> tableIDChoices )
{	__command = command;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"<html><b>This command is under development - do not use for production work.</b></html>" ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Copy processor properties to a table, which is useful for assembling output when looping through data processes." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
        "If the table does not exist, it will be created." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The table row is determined by matching the lookup value in the TableLookupColumn." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "If the lookup value is not matched or AllowDuplicates=True, a new row will be created for the properties." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST); 
	JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The table output columns will default to the property names.  Use * to match one property name " +
        "when specifying a list of column names." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
  
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Properties to include:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeProperties_JTextField = new JTextField ( 10 );
    __IncludeProperties_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeProperties_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - property names to copy (default=all)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
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
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to modify or create."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table lookup column:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableLookupColumn_JTextField = new JTextField ( 10 );
    __TableLookupColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __TableLookupColumn_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table column name for lookup values."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Lookup value:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableLookupValue_JTextField = new JTextField(10);
    __TableLookupValue_JTextField.addKeyListener ( this );
    __TableLookupValue_JTextField.getDocument().addDocumentListener ( this );
    JGUIUtil.addComponent(main_JPanel, __TableLookupValue_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - lookup value to match to find row."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Allow duplicates?:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AllowDuplicates_JComboBox = new SimpleJComboBox ( false );
    __AllowDuplicates_JComboBox.add ( "" );
    __AllowDuplicates_JComboBox.add ( __command._False );
    __AllowDuplicates_JComboBox.add ( __command._True );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __AllowDuplicates_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - allow multiple rows for same lookup value? (default=" + __command._False+ ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table output columns:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableOutputColumns_JTextField = new JTextField ( 10 );
    __TableOutputColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __TableOutputColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - column names to receive properties (default=property names)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 55 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
    checkGUIState();
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command" );
    pack();
    JGUIUtil.center( this );
	setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{	checkGUIState();
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else {
	    // Combo box...
		refresh();
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

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
    String IncludeProperties = "";
    String TableID = "";
    String TableLookupColumn = "";
    String TableLookupValue = "";
    String AllowDuplicates = "";
    String TableOutputColumns = "";

	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
        IncludeProperties = props.getValue ( "IncludeProperties" );
        TableID = props.getValue ( "TableID" );
        TableLookupColumn = props.getValue ( "TableLookupColumn" );
        TableLookupValue = props.getValue ( "TableLookupValue" );
        AllowDuplicates = props.getValue ( "AllowDuplicates" );
        TableOutputColumns = props.getValue ( "TableOutputColumns" );
        if ( IncludeProperties != null ) {
            __IncludeProperties_JTextField.setText ( IncludeProperties );
        }
        if ( TableID == null ) {
            // Select default...
            __TableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableID_JComboBox,TableID, JGUIUtil.NONE, null, null ) ) {
                __TableID_JComboBox.select ( TableID );
            }
            else {
                // Creating new table so add in the first position
                if ( __TableID_JComboBox.getItemCount() == 0 ) {
                    __TableID_JComboBox.add(TableID);
                }
                else {
                    __TableID_JComboBox.insert(TableID, 0);
                }
                __TableID_JComboBox.select(0);
            }
        }
        if ( TableLookupColumn != null ) {
            __TableLookupColumn_JTextField.setText ( TableLookupColumn );
        }
        if (TableLookupValue != null ) {
            __TableLookupValue_JTextField.setText(TableLookupValue);
        }
        if ( AllowDuplicates == null ) {
            // Select default...
            __AllowDuplicates_JComboBox.select ( "" );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(__AllowDuplicates_JComboBox,AllowDuplicates, JGUIUtil.NONE, null, null ) ) {
                __AllowDuplicates_JComboBox.select(AllowDuplicates);
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid AllowDuplicates value \"" +
                AllowDuplicates + "\".  Select a different run mode or Cancel.");
                __error_wait = true;
            }
        }
        if ( TableOutputColumns != null ) {
            __TableOutputColumns_JTextField.setText ( TableOutputColumns );
        }
	}
	// Regardless, reset the command from the fields...
    IncludeProperties = __IncludeProperties_JTextField.getText().trim();
	TableID = __TableID_JComboBox.getSelected();
    TableLookupColumn = __TableLookupColumn_JTextField.getText().trim();
    TableLookupValue = __TableLookupValue_JTextField.getText().trim();
    AllowDuplicates = __AllowDuplicates_JComboBox.getSelected();
    TableOutputColumns = __TableOutputColumns_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "IncludeProperties=" + IncludeProperties );
    props.add ( "TableID=" + TableID );
    props.add ( "TableLookupColumn=" + TableLookupColumn );
    props.add ( "TableLookupValue=" + TableLookupValue );
    props.add ( "AllowDuplicates=" + AllowDuplicates );
    props.add ( "TableOutputColumns=" + TableOutputColumns );
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
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
