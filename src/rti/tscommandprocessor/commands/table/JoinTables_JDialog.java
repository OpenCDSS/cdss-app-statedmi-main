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
import java.util.Vector;

import RTi.Util.GUI.DictionaryJDialog;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTableJoinMethodType;
import RTi.Util.Table.HandleMultipleJoinMatchesHowType;

@SuppressWarnings("serial")
public class JoinTables_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private SimpleJComboBox __TableToJoinID_JComboBox = null;
private JTextArea __JoinColumns_JTextArea = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextArea __ColumnMap_JTextArea = null;
private JTextArea __ColumnFilters_JTextArea = null;
private SimpleJComboBox __JoinMethod_JComboBox = null;
private SimpleJComboBox __HandleMultipleJoinMatchesHow_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private JoinTables_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public JoinTables_JDialog ( JFrame parent, JoinTables_Command command, List<String> tableIDChoices )
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
		HelpViewer.getInstance().showHelp("command", "JoinTables");
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
        	"Column names in the table being joined can be changed to a new name in the main table.",
        	"Column Name in Join Table - the original column name (can use ${property})",
        	"Column Name in the Joined Table - the column name after joining (can use ${property})"
    		};
        String dict = (new DictionaryJDialog ( __parent, true, ColumnMap,
            "Edit ColumnMap Parameter", notes, "Column Name in Join Table", "Column Name in Joined Table",10)).response();
        if ( dict != null ) {
            __ColumnMap_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditJoinColumns") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String JoinColumns = __JoinColumns_JTextArea.getText().trim();
        String [] notes = {
        	"Specify the column name in the main table and the corresponding column in the table to be joined.",
    		"These columns will be used to match column values for the join.",
    		"The ${property} notation can be used to substitute processor properties."};
        String dict = (new DictionaryJDialog ( __parent, true, JoinColumns,
            "Edit JoinColumns Parameter", notes,
        	"Column Name in First Table", "Column Name in Join (Second) Table",10)).response();
        if ( dict != null ) {
            __JoinColumns_JTextArea.setText ( dict );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
        String [] notes = {
        	"Rows to join can be filtered.  This may be needed to ignore rows from one-to-many relationships.",
        	"Join Column Name - the original column name (can use ${property})",
        	"Column Value Filter Pattern - a string pattern to match to include rows, using * for wildcard  (can use ${property})"
    		};
        String columnFilters = (new DictionaryJDialog ( __parent, true, ColumnFilters, "Edit ColumnFilters Parameter",
            notes, "Join Column Name", "Column Value Filter Pattern",10)).response();
        if ( columnFilters != null ) {
            __ColumnFilters_JTextArea.setText ( columnFilters );
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
	String TableToJoinID = __TableToJoinID_JComboBox.getSelected();
	String JoinColumns = __JoinColumns_JTextArea.getText().trim().replace("\n"," ");
	String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	String ColumnMap = __ColumnMap_JTextArea.getText().trim().replace("\n"," ");
	String ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
	String JoinMethod = __JoinMethod_JComboBox.getSelected();
	String HandleMultipleJoinMatchesHow = __HandleMultipleJoinMatchesHow_JComboBox.getSelected();
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( TableToJoinID.length() > 0 ) {
        props.set ( "TableToJoinID", TableToJoinID );
    }
    if ( JoinColumns.length() > 0 ) {
        props.set ( "JoinColumns", JoinColumns );
    }
	if ( IncludeColumns.length() > 0 ) {
		props.set ( "IncludeColumns", IncludeColumns );
	}
    if ( ColumnMap.length() > 0 ) {
        props.set ( "ColumnMap", ColumnMap );
    }
    if ( ColumnFilters.length() > 0 ) {
        props.set ( "ColumnFilters", ColumnFilters );
    }
    if ( JoinMethod.length() > 0 ) {
        props.set ( "JoinMethod", JoinMethod );
    }
    if ( HandleMultipleJoinMatchesHow.length() > 0 ) {
        props.set ( "HandleMultipleJoinMatchesHow", HandleMultipleJoinMatchesHow );
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
    String TableToJoinID = __TableToJoinID_JComboBox.getSelected();
    String JoinColumns = __JoinColumns_JTextArea.getText().trim().replace("\n"," ");
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ColumnMap = __ColumnMap_JTextArea.getText().trim().replace("\n"," ");
    String ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
    String JoinMethod = __JoinMethod_JComboBox.getSelected();
    String HandleMultipleJoinMatchesHow = __HandleMultipleJoinMatchesHow_JComboBox.getSelected();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "TableToJoinID", TableToJoinID );
    __command.setCommandParameter ( "JoinColumns", JoinColumns );
	__command.setCommandParameter ( "IncludeColumns", IncludeColumns );
	__command.setCommandParameter ( "ColumnMap", ColumnMap );
	__command.setCommandParameter ( "ColumnFilters", ColumnFilters );
	__command.setCommandParameter ( "JoinMethod", JoinMethod );
	__command.setCommandParameter ( "HandleMultipleJoinMatchesHow", HandleMultipleJoinMatchesHow );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, JoinTables_Command command, List<String> tableIDChoices )
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
        "This command modifies a table by joining columns from a second table (the table to join)."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Specify 1+ join columns to match column values in each table."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Columns will be added to the original table.  Rows will be added only if JoinMethod=JoinAlways and there was not a match."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The rows being added from the second table can be filtered."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table ID for the table to modify or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to modify (first table)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table to join ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableToJoinID_JComboBox = new SimpleJComboBox ( 12, false );
    __TableToJoinID_JComboBox.setToolTipText("Specify the second table ID or use ${Property} notation");
    __TableToJoinID_JComboBox.setData ( tableIDChoices );
    __TableToJoinID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableToJoinID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to join to the first table."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Columns to join:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __JoinColumns_JTextArea = new JTextArea (3,35);
    __JoinColumns_JTextArea.setLineWrap ( true );
    __JoinColumns_JTextArea.setWrapStyleWord ( true );
    __JoinColumns_JTextArea.setToolTipText("Table1Column1:Table2Column1,Table1Column2:Table2Column2, can use ${Property}");
    __JoinColumns_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__JoinColumns_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - columns to match in each table."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditJoinColumns",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column names to copy:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField (10);
    __IncludeColumns_JTextField.setToolTipText("Specify the column names to copy, can use ${Property}");
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeColumns_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - names of columns in second table to copy, can use ${property} (default=copy all)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column map:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnMap_JTextArea = new JTextArea (6,35);
    __ColumnMap_JTextArea.setLineWrap ( true );
    __ColumnMap_JTextArea.setWrapStyleWord ( true );
    __ColumnMap_JTextArea.setToolTipText("OriginalColumn1:NewColumn1,OriginalColumn2:NewColumn2, can use ${Property}");
    __ColumnMap_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnMap_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - to change names (default=names are same)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnMap",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column filters:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnFilters_JTextArea = new JTextArea (3,35);
    __ColumnFilters_JTextArea.setLineWrap ( true );
    __ColumnFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnFilters_JTextArea.setToolTipText("JoinTableColumnName1:FilterPattern1,JoinTableColumnName2:FilterPattern2, can use ${Property}");
    __ColumnFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - filter rows to copy by matching column pattern."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Join method:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __JoinMethod_JComboBox = new SimpleJComboBox ( false );
    List<String> choices = new Vector<String>();
    choices.add("");
    choices.add("" + DataTableJoinMethodType.JOIN_ALWAYS);
    choices.add("" + DataTableJoinMethodType.JOIN_IF_IN_BOTH);
    __JoinMethod_JComboBox.setData ( choices );
    __JoinMethod_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __JoinMethod_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - join method (default=" + DataTableJoinMethodType.JOIN_IF_IN_BOTH + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Handle multiple matches how?:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __HandleMultipleJoinMatchesHow_JComboBox = new SimpleJComboBox ( false );
    List<String> choices2 = new ArrayList<String>();
    choices2.add("");
    choices2.add(""+HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS);
    choices2.add(""+HandleMultipleJoinMatchesHowType.USE_LAST_MATCH);
    __HandleMultipleJoinMatchesHow_JComboBox.setData ( choices2 );
    __HandleMultipleJoinMatchesHow_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __HandleMultipleJoinMatchesHow_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - how to handle multiple matches for row (default=" +
        HandleMultipleJoinMatchesHowType.USE_LAST_MATCH + ")."), 
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
{	String routine = getClass().getName() + ".refresh";
    String TableID = "";
    String TableToJoinID = "";
    String JoinColumns = "";
    String IncludeColumns = "";
    String ColumnMap = "";
    String ColumnFilters = "";
    String JoinMethod = "";
    String HandleMultipleJoinMatchesHow = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        TableToJoinID = props.getValue ( "TableToJoinID" );
        JoinColumns = props.getValue ( "JoinColumns" );
        IncludeColumns = props.getValue ( "IncludeColumns" );
        ColumnMap = props.getValue ( "ColumnMap" );
        ColumnFilters = props.getValue ( "ColumnFilters" );
        JoinMethod = props.getValue ( "JoinMethod" );
        HandleMultipleJoinMatchesHow = props.getValue ( "HandleMultipleJoinMatchesHow" );
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
        if ( TableToJoinID == null ) {
            // Select default...
            __TableToJoinID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableToJoinID_JComboBox, TableToJoinID, JGUIUtil.NONE, null, null ) ) {
                __TableToJoinID_JComboBox.select ( TableToJoinID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTableToJoinID value \"" + TableToJoinID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( JoinColumns != null ) {
            __JoinColumns_JTextArea.setText ( JoinColumns );
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
        if ( JoinMethod == null ) {
            // Select default...
            __JoinMethod_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __JoinMethod_JComboBox,JoinMethod, JGUIUtil.NONE, null, null ) ) {
                __JoinMethod_JComboBox.select ( JoinMethod );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nJoinMethod value \"" + JoinMethod +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( HandleMultipleJoinMatchesHow == null ) {
            // Select default...
            __HandleMultipleJoinMatchesHow_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __HandleMultipleJoinMatchesHow_JComboBox,HandleMultipleJoinMatchesHow, JGUIUtil.NONE, null, null ) ) {
                __HandleMultipleJoinMatchesHow_JComboBox.select ( HandleMultipleJoinMatchesHow );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nHandleMultipleJoinMatchesHow value \"" + HandleMultipleJoinMatchesHow +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
    TableToJoinID = __TableToJoinID_JComboBox.getSelected();
    JoinColumns = __JoinColumns_JTextArea.getText().trim();
	IncludeColumns = __IncludeColumns_JTextField.getText().trim();
	ColumnMap = __ColumnMap_JTextArea.getText().trim();
	ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
	JoinMethod = __JoinMethod_JComboBox.getSelected();
	HandleMultipleJoinMatchesHow = __HandleMultipleJoinMatchesHow_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "TableToJoinID=" + TableToJoinID );
    props.add ( "JoinColumns=" + JoinColumns );
	props.add ( "IncludeColumns=" + IncludeColumns );
	props.add ( "ColumnMap=" + ColumnMap );
	props.add ( "ColumnFilters=" + ColumnFilters );
	props.add ( "JoinMethod=" + JoinMethod );
	props.add ( "HandleMultipleJoinMatchesHow=" + HandleMultipleJoinMatchesHow );
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