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
public class CopyTable_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __NewTableID_JTextField = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextField __DistinctColumns_JTextField = null;
private JTextArea __ColumnMap_JTextArea = null;
private JTextArea __ColumnFilters_JTextArea = null;
private JTextArea __ColumnExcludeFilters_JTextArea = null;
private JTextField __RowCountProperty_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private CopyTable_Command __command = null;
private JFrame __parent = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public CopyTable_JDialog ( JFrame parent, CopyTable_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "CopyTable");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
		}
	}
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnMap") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnMap = __ColumnMap_JTextArea.getText().trim();
        String [] notes = {
            "Column names in the original table can be renamed in the copy by specifying information below."
        };
        String dict = (new DictionaryJDialog ( __parent, true, ColumnMap,
            "Edit ColumnMap Parameter", notes, "Original Column Name", "Column Name in New Table",10)).response();
        if ( dict != null ) {
            __ColumnMap_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
        String [] notes = {
            "Rows in the original table can be included in the copy by filtering on column values.",
            "All specified conditions must be true to include the row.",
            "Column Name - column name in the original table to filter, can include ${Property}",
            "Column Value Filter Pattern - a literal value to match (to include), or a pattern using * as a wildcard,",
            "   can include ${Property}"
        };
        String columnFilters = (new DictionaryJDialog ( __parent, true, ColumnFilters, "Edit ColumnFilters Parameter",
            notes, "Column Name", "Column Value Filter Pattern",10)).response();
        if ( columnFilters != null ) {
            __ColumnFilters_JTextArea.setText ( columnFilters );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnExcludeFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim();
        String [] notes = {
            "Rows in the original table can be excluded from the copy by filtering on column values.",
            "All specified conditions must be true to exclude the row.",
            "Column Name - column name in the original table to filter",
            "Column Value Filter Pattern - a literal value to match (to exclude), or a pattern using * as a wildcard",
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
    String NewTableID = __NewTableID_JTextField.getText().trim();
	String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	String DistinctColumns = __DistinctColumns_JTextField.getText().trim();
	String ColumnMap = __ColumnMap_JTextArea.getText().trim().replace("\n"," ");
	String ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
	String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
	String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( NewTableID.length() > 0 ) {
        props.set ( "NewTableID", NewTableID );
    }
	if ( IncludeColumns.length() > 0 ) {
		props.set ( "IncludeColumns", IncludeColumns );
	}
    if ( DistinctColumns.length() > 0 ) {
        props.set ( "DistinctColumns", DistinctColumns );
    }
    if ( ColumnMap.length() > 0 ) {
        props.set ( "ColumnMap", ColumnMap );
    }
    if ( ColumnFilters.length() > 0 ) {
        props.set ( "ColumnFilters", ColumnFilters );
    }
    if ( ColumnExcludeFilters.length() > 0 ) {
        props.set ( "ColumnExcludeFilters", ColumnExcludeFilters );
    }
    if ( RowCountProperty.length() > 0 ) {
        props.set ( "RowCountProperty", RowCountProperty );
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
    String NewTableID = __NewTableID_JTextField.getText().trim();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String DistinctColumns = __DistinctColumns_JTextField.getText().trim();
    String ColumnMap = __ColumnMap_JTextArea.getText().trim().replace("\n"," ");
    String ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
    String ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
    String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "NewTableID", NewTableID );
	__command.setCommandParameter ( "IncludeColumns", IncludeColumns );
	__command.setCommandParameter ( "DistinctColumns", DistinctColumns );
	__command.setCommandParameter ( "ColumnMap", ColumnMap );
	__command.setCommandParameter ( "ColumnFilters", ColumnFilters );
	__command.setCommandParameter ( "ColumnExcludeFilters", ColumnExcludeFilters );
	__command.setCommandParameter ( "RowCountProperty", RowCountProperty );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, CopyTable_Command command, List<String> tableIDChoices )
{	__command = command;
    __parent = parent;

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
        "This command creates a new table by copying another table."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "For example, single column tables can be created from a larger table to use as a list with a template."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - original table."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("New table ID:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __NewTableID_JTextField = new JTextField (10);
    __NewTableID_JTextField.setToolTipText("Specify the new table ID or use ${Property} notation");
    __NewTableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __NewTableID_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - unique identifier for the new table."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Include columns:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField (10);
    __IncludeColumns_JTextField.setToolTipText("Names of columns to include, separated by commas, can use ${Property} notation.");
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - names of columns to copy (default=copy all)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Distinct columns:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DistinctColumns_JTextField = new JTextField (10);
    __DistinctColumns_JTextField.setToolTipText("If specified, the output table will only include the distinct columns, " +
        "UNLESS IncludeColumns is specified, in which case those columns will be retained in output, can use ${Property} notation.");
    __DistinctColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DistinctColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - names of columns to filter distinct combinations (default=no distinct columns)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column map:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnMap_JTextArea = new JTextArea (6,35);
    __ColumnMap_JTextArea.setLineWrap ( true );
    __ColumnMap_JTextArea.setWrapStyleWord ( true );
    __ColumnMap_JTextArea.setToolTipText("OriginalColumn1:NewColumn1,OriginalColumn2:NewColumn2");
    __ColumnMap_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnMap_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - to change names (default=names are same)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnMap",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column include filters:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnFilters_JTextArea = new JTextArea (3,35);
    __ColumnFilters_JTextArea.setLineWrap ( true );
    __ColumnFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnFilters_JTextArea.setToolTipText("Specify table column filters ColumnName1:FilterPattern1,ColumnName2:FilterPattern2, can use ${Property} notation");
    __ColumnFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - filter rows to copy by matching column pattern (default=copy all rows)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column exclude filters:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnExcludeFilters_JTextArea = new JTextArea (3,35);
    __ColumnExcludeFilters_JTextArea.setLineWrap ( true );
    __ColumnExcludeFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnExcludeFilters_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnExcludeFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnExcludeFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - filter rows to exclude by matching column pattern (default=copy all rows)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnExcludeFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Row count property:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowCountProperty_JTextField = new JTextField ( "", 20 );
    __RowCountProperty_JTextField.setToolTipText("Specify the property name for the copied table row count, can use ${Property} notation");
    __RowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RowCountProperty_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - processor property to set as output table row count." ),
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
    String NewTableID = "";
    String IncludeColumns = "";
    String DistinctColumns = "";
    String ColumnMap = "";
    String ColumnFilters = "";
    String ColumnExcludeFilters = "";
    String RowCountProperty = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        NewTableID = props.getValue ( "NewTableID" );
        DistinctColumns = props.getValue ( "DistinctColumns" );
        IncludeColumns = props.getValue ( "IncludeColumns" );
        ColumnMap = props.getValue ( "ColumnMap" );
        ColumnFilters = props.getValue ( "ColumnFilters" );
        ColumnExcludeFilters = props.getValue ( "ColumnExcludeFilters" );
        RowCountProperty = props.getValue ( "RowCountProperty" );
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
        if ( NewTableID != null ) {
            __NewTableID_JTextField.setText ( NewTableID );
        }
        if ( DistinctColumns != null ) {
            __DistinctColumns_JTextField.setText ( DistinctColumns );
        }
		if ( IncludeColumns != null ) {
			__IncludeColumns_JTextField.setText ( IncludeColumns );
		}
        if ( ColumnMap != null ) {
            __ColumnMap_JTextArea.setText ( ColumnMap );
        }
        if ( ColumnFilters != null ) {
            __ColumnFilters_JTextArea.setText ( ColumnFilters );
        }
        if ( ColumnExcludeFilters != null ) {
            __ColumnExcludeFilters_JTextArea.setText ( ColumnExcludeFilters );
        }
        if ( RowCountProperty != null ) {
            __RowCountProperty_JTextField.setText ( RowCountProperty );
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
    NewTableID = __NewTableID_JTextField.getText().trim();
	IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	DistinctColumns = __DistinctColumns_JTextField.getText().trim();
	ColumnMap = __ColumnMap_JTextArea.getText().trim().replace("\n"," ");
	ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
	ColumnExcludeFilters = __ColumnExcludeFilters_JTextArea.getText().trim().replace("\n"," ");
	RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "NewTableID=" + NewTableID );
    props.add ( "DistinctColumns=" + DistinctColumns );
	props.add ( "IncludeColumns=" + IncludeColumns );
	props.add ( "ColumnMap=" + ColumnMap );
	props.add ( "ColumnFilters=" + ColumnFilters );
	props.add ( "ColumnExcludeFilters=" + ColumnExcludeFilters );
	props.add ( "RowCountProperty=" + RowCountProperty );
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