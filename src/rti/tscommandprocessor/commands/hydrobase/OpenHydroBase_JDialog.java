// ----------------------------------------------------------------------------
// openHydroBase_JDialog - editor for openHydroBase()
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-02-03	Steven A. Malers, RTi	Initial version (copy and modify
//					setOutputYearType_JDialog).
// 2005-04-12	SAM, RTi		* Add InputName and UseStoredProcedures
//					  parameters.
//					* Convert the command text field to a
//					  text area.
// 2005-06-08	SAM, RTi		* Convert to Command design.
// 2005-09-22	SAM, RTi		* Add DatabaseName parameter.
// 2005-09-26	J. Thomas Sapienza, RTi	* Added a new combo box to select 
//					  whether the settings are for Access
//					  or SQL Server.  This information is 
//					  not saved with the command, but 
//					  filters the available options on the
//					  form to be specific to each database
//					  type.
//					* Database Server and Database Name 
//					  are now stored in editable combo 
//					  boxes.
//					* When enter is pressed in the server
//					  name combo box, or the combo box
//					  loses focus, the server in the combo
//					  box is queried to determine the 
//					  HydroBase databases available on it.
//					* When the value in the "Use stored
//					  procedures?" combo box is changed,
//					  the server in the server name combo
//					  box is requeried to see the databases
//					  available.
// 2007-02-26	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package rti.tscommandprocessor.commands.hydrobase;

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
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.DMI.DMIUtil;
import RTi.DMI.GenericDMI;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class OpenHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

/**
Strings used to help the user understand the ODBC DSN parameter.
*/
private final String 
	__TYPE_ACCESS = "Access",
	__TYPE_SQLSERVER = "SQL Server";

/**
Default string that shows up if a server has no database that match HydroBase*.
*/
private final String __NO_DATABASES = "[No HydroBase databases available]";

/**
The String that the Databases should start with.
*/
private final String __HYDROBASE = "HydroBase";

/**
Used to ignore itemStateChanged() events when setting the list of databases that are found on a server.
*/
private boolean __ignoreItemStateChange = false;

/**
The combo box that holds the selected of whether the database is an Access or a SQL Server database.
*/
private SimpleJComboBox __databaseType_JComboBox = null;

/**
These Strings hold the original parameter values of the database server, the
database name, and whether stored procedures are used.  They are used because
events were trouncing over each other and the default values were not being
placed properly within combo boxes in the refresh() method.  Using these values,
the defaults are selected in the initialize() method.
*/
private String 
	__holdServer = null, 
	__holdName = null,
	__holdStoredProcedures = null;

private SimpleJButton	__cancel_JButton = null, // Cancel Button
			__ok_JButton = null; // Ok Button
private JTextArea	__command_JTextArea = null;	// Command as JTextArea
private SimpleJComboBox	__OdbcDsn_JComboBox = null;	// Field for OdbcDsn
private SimpleJComboBox __DatabaseServer_JComboBox=null; // Field for database server
private SimpleJComboBox __DatabaseName_JComboBox=null;
private SimpleJComboBox	__RunMode_JComboBox=null; // Field for run mode
private JTextField	__InputName_JTextField=null; // Field for InputName for DMI.
private SimpleJComboBox	__UseStoredProcedures_JComboBox=null;
							// Indicate whether
							// stored procedures
							// should be used for
							// HydroBaseDMI.
private boolean		__error_wait = false;
private boolean		__first_time = true;

//private List		__available_OdbcDsn = null;	// Available ODBC DSN to list.
private OpenHydroBase_Command __command = null;	// Command to edit
private boolean		__ok = false; // Indicates whether the user has pressed OK to close the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public OpenHydroBase_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize ( parent, command );
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
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if (o == __databaseType_JComboBox) {
		databaseTypeSelected(__databaseType_JComboBox.getSelected());
	}
	else {
	    // Choices...
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	PropList props = new PropList ( "" );
	String OdbcDsn = "";
	if (__OdbcDsn_JComboBox.isEnabled()) {
		OdbcDsn = __OdbcDsn_JComboBox.getSelected();
	}
	
	String DatabaseServer = "";
	if (__DatabaseServer_JComboBox.isEnabled()) {
		DatabaseServer=__DatabaseServer_JComboBox.getFieldText().trim();
	}

	String DatabaseName = "";
	if (__DatabaseName_JComboBox.isEnabled()) {
		DatabaseName = __DatabaseName_JComboBox.getFieldText().trim();
	}
	
	String RunMode = __RunMode_JComboBox.getSelected();
	String InputName = __InputName_JTextField.getText().trim();
	
	String UseStoredProcedures = "";
	if (__UseStoredProcedures_JComboBox.isEnabled()) {
		UseStoredProcedures = __UseStoredProcedures_JComboBox.getSelected();
	}
	__error_wait = false;
	if ( OdbcDsn.length() > 0 ) {
		props.set ( "OdbcDsn", OdbcDsn );
	}
	if ( DatabaseServer.length() > 0 ) {
		props.set ( "DatabaseServer", DatabaseServer );
	}
	if ( DatabaseName.length() > 0 ) {
		props.set ( "DatabaseName", DatabaseName );
	}
	if ( InputName.length() > 0 ) {
		props.set ( "InputName", InputName );
	}
	if ( UseStoredProcedures.length() > 0 ) {
		props.set ( "UseStoredProcedures", UseStoredProcedures );
	}
	if ( RunMode.length() > 0 ) {
		props.set ( "RunMode", RunMode );
	}
	try {
	    // This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
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
private void commitEdits() {
	String OdbcDsn = "";
	if (__OdbcDsn_JComboBox.isEnabled()) {
		OdbcDsn = __OdbcDsn_JComboBox.getSelected();
	}
	
	String DatabaseServer = "";
	if (__DatabaseServer_JComboBox.isEnabled()) {
		DatabaseServer=__DatabaseServer_JComboBox.getFieldText().trim();
	}

	String DatabaseName = "";
	if (__DatabaseName_JComboBox.isEnabled()) {
		DatabaseName = __DatabaseName_JComboBox.getFieldText().trim();
	}
	
	String RunMode = __RunMode_JComboBox.getSelected();
	String InputName = __InputName_JTextField.getText().trim();
	
	String UseStoredProcedures = "";
	if (__UseStoredProcedures_JComboBox.isEnabled()) {
		UseStoredProcedures = __UseStoredProcedures_JComboBox.getSelected();
	}

	__command.setCommandParameter ( "OdbcDsn", OdbcDsn );
	__command.setCommandParameter ( "DatabaseServer", DatabaseServer );
	__command.setCommandParameter ( "DatabaseName", DatabaseName );
	__command.setCommandParameter ( "RunMode", RunMode );
	__command.setCommandParameter ( "InputName", InputName );
	__command.setCommandParameter ( "UseStoredProcedures", UseStoredProcedures );
}

/**
Called when a database type is selected from the database type combo box,
this method enables or disables the components on the GUI that are not necessary
for the type of database selected.
@param databaseType the type of database selected (one of __TYPE_ACCESS or __TYPE_SQLSERVER).
*/
private void databaseTypeSelected(String databaseType) {
	if (databaseType.equals(__TYPE_ACCESS)) {
		__OdbcDsn_JComboBox.setEnabled(true);
		__DatabaseServer_JComboBox.setEnabled(false);
		__DatabaseServer_JComboBox.getJTextComponent().setEnabled(false);
		__DatabaseServer_JComboBox.getJTextComponent().setEditable(false);
		__DatabaseName_JComboBox.setEnabled(false);
		__DatabaseName_JComboBox.getJTextComponent().setEnabled(false);
		__DatabaseName_JComboBox.getJTextComponent().setEditable(false);			
		__UseStoredProcedures_JComboBox.setEnabled(false);
		__UseStoredProcedures_JComboBox.setEditable(false);
	}
	else {
		__OdbcDsn_JComboBox.setEnabled(false);
		__DatabaseServer_JComboBox.setEnabled(true);
		__DatabaseServer_JComboBox.getJTextComponent().setEnabled(true);
		__DatabaseServer_JComboBox.getJTextComponent().setEditable(true);
		__DatabaseName_JComboBox.setEnabled(true);
		__DatabaseName_JComboBox.getJTextComponent().setEnabled(true);
		__DatabaseName_JComboBox.getJTextComponent().setEditable(true);					
		__UseStoredProcedures_JComboBox.setEnabled(true);	
		__UseStoredProcedures_JComboBox.setEditable(true);	
	}

	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__OdbcDsn_JComboBox = null;
	__DatabaseServer_JComboBox = null;
	__DatabaseName_JComboBox = null;
	__InputName_JTextField = null;
	__RunMode_JComboBox = null;
	__UseStoredProcedures_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__databaseType_JComboBox = null;
	__holdServer = null;
	__holdName = null;
	__holdStoredProcedures = null;
	__serverNames = null;
	
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (OpenHydroBase_Command)command;

	readConfigurationFile();

	try {
	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "<html><b>This command will be phased out in the future as HydroBase datastore support is phased in.</b></html>" ),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command opens a connection to a HydroBase database," +
		" closing the previous connection with the same input name." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command is used, for example, when making connections to more "+
		"than one HydroBase database, or running in batch mode."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The RunMode can also be set to control whether the command " +
		"is run in batch mode, in interactive sessions, or both."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The connection can be made either by specifying a database server/name for SQL Server,"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"or an ODBC Data Source Name (DSN) for a Microsoft Access HydroBase database " +
		"(only for very old HydroBase databases)."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel("Database type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__databaseType_JComboBox = new SimpleJComboBox();
	__databaseType_JComboBox.add(__TYPE_ACCESS);
	__databaseType_JComboBox.add(__TYPE_SQLSERVER);
	// Default to SQL Server
	__databaseType_JComboBox.select ( __TYPE_SQLSERVER );
	__databaseType_JComboBox.addActionListener(this);
	JGUIUtil.addComponent(main_JPanel, __databaseType_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - indicates whether a database server/name or ODBC DSN is specified below."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Database server:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	String prototype = "[No HydroBase databases available]";
	__DatabaseServer_JComboBox = new SimpleJComboBox(__serverNames, true);
	__DatabaseServer_JComboBox.setPrototypeDisplayValue(prototype);
	__DatabaseServer_JComboBox.addTextFieldKeyListener(this);
	__DatabaseServer_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(main_JPanel, __DatabaseServer_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - when using SQL Server."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Database name:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DatabaseName_JComboBox = new SimpleJComboBox(true);
	__DatabaseName_JComboBox.setPrototypeDisplayValue(prototype);
	__DatabaseName_JComboBox.addTextFieldKeyListener(this);
	__DatabaseName_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(main_JPanel, __DatabaseName_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - when using SQL Server."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
   	/*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "ODBC DSN:" ),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OdbcDsn_JComboBox = new SimpleJComboBox ( false );
    // Get the data source names from the system...
    __available_OdbcDsn = DMIUtil.getDefinedOdbcDsn ( true );
    // Remove any that don't have "HydroBase" in them...
    int size = __available_OdbcDsn.size();
    String s = null;
    for (int i = 0; i < size; i++) {
        s = (String)__available_OdbcDsn.get(i);
        if (StringUtil.indexOfIgnoreCase(s, "HydroBase", 0) < 0) {
            __available_OdbcDsn.remove(i--);
            --size;
        }
    }
    // Always have a blank at the beginning...
    __available_OdbcDsn.add ( 0, "" );
    __OdbcDsn_JComboBox.setData ( __available_OdbcDsn );
    __OdbcDsn_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __OdbcDsn_JComboBox,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - only used with Microsoft Access."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        */

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input name:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputName_JTextField = new JTextField ( "", 20 );
	__InputName_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InputName_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - input name for connection, to be used with time series identifiers."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Use stored procedures?:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__UseStoredProcedures_JComboBox = new SimpleJComboBox ( false );
	__UseStoredProcedures_JComboBox.add ( "" );
	__UseStoredProcedures_JComboBox.add ( __command._True );
	__UseStoredProcedures_JComboBox.add ( __command._False );
	__UseStoredProcedures_JComboBox.select ( 0 );
	__UseStoredProcedures_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __UseStoredProcedures_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - default is true for SQL Server, ignored for Access."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Run Mode:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__RunMode_JComboBox = new SimpleJComboBox ( false );
	__RunMode_JComboBox.add ( "" );
	__RunMode_JComboBox.add ( __command._BatchOnly );
	__RunMode_JComboBox.add ( __command._GUIAndBatch );
	__RunMode_JComboBox.add ( __command._GUIOnly );
	__RunMode_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RunMode_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - default is GUI and batch mode."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (2,60);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel,
		new JScrollPane ( __command_JTextArea ),
		1, y, 6, 2, 1, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	// Determine whether the database is making a connection to an access database or not.
	PropList props = __command.getCommandParameters();
	String OdbcDsn = props.getValue("OdbcDsn");
	boolean isAccess = false;
	if (OdbcDsn != null) {
		isAccess = true;
	}
		
	// Enable/disable components on the GUI as appropriate for the
	// connection type that was determined from the parameters
	if (isAccess) {
		__databaseType_JComboBox.select(__TYPE_ACCESS);
	}
	else {
		__databaseType_JComboBox.select(__TYPE_SQLSERVER);
	}

	// Do an initial refresh to make sure the command at the bottom is filled out
	refresh();

	// Select the database server and name from the combo box
	if (!isAccess) {
		if (__holdServer != null) {
			__DatabaseServer_JComboBox.select(__holdServer);
		}
		
		// Note: the stored procedures combo box value is selected prior to the database name combo box value
		// because selecting in the stored procedure combo box will requery the database
		// names for the other combo box.  So do not change the order of the following:
		if (__holdStoredProcedures != null) {
			__UseStoredProcedures_JComboBox.select(	__holdStoredProcedures);
		}		
		if (__holdName != null) {
			__DatabaseName_JComboBox.select(__holdName);
		}
	}

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	y += 2;	// Text area had 2 rows.
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	button_JPanel.add (__cancel_JButton = new SimpleJButton("Cancel",this));
	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );

	setTitle ( "Edit " + __command.getCommandName() + "() command" );

	// Dialogs do not need to be resizable...
	setResizable ( true );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "", e );
	}
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged(ItemEvent event) {
	if (__ignoreItemStateChange) {
		return;
	}

	if ( event.getStateChange() != ItemEvent.SELECTED ) {
		return;
	}

	if (event.getSource() == __DatabaseServer_JComboBox || event.getSource() == __UseStoredProcedures_JComboBox) {
		// new server name entered, return pressed
		retrieveDatabaseNames (__DatabaseServer_JComboBox.getFieldText());	
	}

	refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String OdbcDsn = "";
	String DatabaseServer = "";
	String DatabaseName = "";
	String InputName = "";
	String UseStoredProcedures = "";
	String RunMode = "";
	PropList props = __command.getCommandParameters ();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		OdbcDsn = props.getValue ( "OdbcDsn" );
		DatabaseServer = props.getValue ( "DatabaseServer" );
		DatabaseName = props.getValue ( "DatabaseName" );
		InputName = props.getValue ( "InputName" );
		UseStoredProcedures = props.getValue ( "UseStoredProcedures" );
		RunMode = props.getValue ( "RunMode" );
		if ( OdbcDsn == null ) {
			// Select blank...
			__OdbcDsn_JComboBox.select ( 0 );
		}
		else {	
			if ( JGUIUtil.isSimpleJComboBoxItem( __OdbcDsn_JComboBox, OdbcDsn, JGUIUtil.NONE, null, null ) ) {
				__OdbcDsn_JComboBox.select ( OdbcDsn );
			}
			else {
			    Message.printWarning ( 1, "openHydroBase_JDialog.refresh",
				"Existing openHydroBase() references an invalid\nODBC DSN \"" + OdbcDsn +
				"\".  Select a different ODBC DSN or Cancel.");
				__error_wait = true;
			}
		}
		
		if ( DatabaseServer != null ) {
			__holdServer = DatabaseServer;
			if (!__DatabaseServer_JComboBox.contains(DatabaseServer)) {
			    __DatabaseServer_JComboBox.add(DatabaseServer);
			}
			__DatabaseServer_JComboBox.select(DatabaseServer);
			retrieveDatabaseNames(__DatabaseServer_JComboBox.getFieldText());	
		}
		else if (__defaultServerName != null) {
			if (!__DatabaseServer_JComboBox.contains(__defaultServerName)) {
			    	__DatabaseServer_JComboBox.add(__defaultServerName);
			}
			__DatabaseServer_JComboBox.select(__defaultServerName);
			retrieveDatabaseNames(__DatabaseServer_JComboBox.getFieldText());	
		}			    

		if ( DatabaseName != null ) {
			__holdName = DatabaseName;
			if (!__DatabaseName_JComboBox.contains(DatabaseName)) {
				__DatabaseName_JComboBox.add(DatabaseName);
			}
			__DatabaseName_JComboBox.select(DatabaseName);
		}
		if ( InputName != null ) {
			__InputName_JTextField.setText ( InputName );
		}
		if ( UseStoredProcedures == null ) {
			// Select default...
			__UseStoredProcedures_JComboBox.select ( 0 );
		}
		else {	
			if ( JGUIUtil.isSimpleJComboBoxItem( __UseStoredProcedures_JComboBox,
				UseStoredProcedures, JGUIUtil.NONE, null,null)){
				__holdStoredProcedures = UseStoredProcedures;
			}
			else {
			    Message.printWarning ( 1, "openHydroBase_JDialog.refresh",
				"Existing openHydroBase() references an invalid\nflag for using stored procedures \""
				+ UseStoredProcedures + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( RunMode == null ) {
			// Select default...
			__RunMode_JComboBox.select ( 0 );	// None
		}
		else {
		    if ( JGUIUtil.isSimpleJComboBoxItem( __RunMode_JComboBox, RunMode, JGUIUtil.NONE, null, null ) ) {
				__RunMode_JComboBox.select ( RunMode );
			}
			else {
			    Message.printWarning ( 1, "openHydroBase_JDialog.refresh",
				"Existing openHydroBase() references an invalid\nrun mode \"" + RunMode +
				"\".  Select a different run mode or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	if (__OdbcDsn_JComboBox.isEnabled()) {
		OdbcDsn = __OdbcDsn_JComboBox.getSelected();
	}
	if (__DatabaseServer_JComboBox.isEnabled()) {
		DatabaseServer = __DatabaseServer_JComboBox.getFieldText().trim();
	}
	if (__DatabaseName_JComboBox.isEnabled()) {
		DatabaseName = __DatabaseName_JComboBox.getFieldText().trim();
	}
	
	RunMode = __RunMode_JComboBox.getSelected();
	InputName = __InputName_JTextField.getText().trim();

	if (__UseStoredProcedures_JComboBox.isEnabled()) {
		UseStoredProcedures = __UseStoredProcedures_JComboBox.getSelected();
	}
	props = new PropList ( __command.getCommandName() );
	props.add ( "OdbcDsn=" + OdbcDsn );
	props.add ( "DatabaseServer=" + DatabaseServer );
	props.add ( "DatabaseName=" + DatabaseName );
	props.add ( "RunMode=" + RunMode );
	props.add ( "InputName=" + InputName );
	props.add ( "UseStoredProcedures=" + UseStoredProcedures );
	__command_JTextArea.setText( __command.toString ( props ) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
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
Checks a server with the given hostname for the databases running on it.
@param server the server to check.
*/
private void retrieveDatabaseNames(String server) {
	if (server.trim().equals("")) {
		return;
	}

	String[] unms = new String[3];
	String[] pws = new String[3];
	int[] ports = new int[3];

	// FIXME SAM 2009-04-12 Need to obfuscate this
	String s = __UseStoredProcedures_JComboBox.getSelected();
	if (s.trim().equals("") || s.trim().equalsIgnoreCase("true")) {
		//unms[0] = "cdss";
		//pws[0] = "cdss%tools";
		//ports[0] = 5758;
		
		unms[0] = "cdss";
		pws[0] = "cdss%tools";
		ports[0] = 21784;
		
		unms[2] = "cdss";	
		pws[2] = "cdss%tools";  
		ports[2] = 1433;	
	} 
	else {
		//unms[0] = "crdss";	
		//pws[0] = "crdss3nt";    
		//ports[0] = 5758;
		
		unms[0] = "crdss";	
		pws[0] = "crdss3nt";    
		ports[0] = 21784;

		unms[1] = "crdss";	
		pws[1] = "crdss3nt";    
		ports[1] = 1433;
	}

	GenericDMI dmi = null;

	for ( int i = 0; i < unms.length; i++) {
		try {
			dmi = new GenericDMI("SQLServer", server, "master", ports[i], unms[i], pws[i]);
			dmi.open();
		}
		catch (Exception e) {
			// If there is an exception then the DMI was not opened, so there is no need to close it here.
			dmi = null;
		}

		if (dmi != null) {
			break;
			// The DMI is closed below after database information is read from it.
		}
	}

	__ignoreItemStateChange = true;
	try {
		if (dmi == null) {
			// error -- throw an exception to hit the catch below.
			throw new Exception("");
		}
	
		Connection c = dmi.getConnection();
		DatabaseMetaData dmd = c.getMetaData();
		List v = DMIUtil.processResultSet(dmd.getCatalogs());
		dmi.close();
		__DatabaseName_JComboBox.removeAllItems();
		List v2 = null;
		int size = v.size();

		if (size == 0) {
			// No database found -- this should NEVER happen, but just in case ...
			v.add(__NO_DATABASES);
			__DatabaseName_JComboBox.add(__NO_DATABASES);
			return;
		}
		
		s = null;
		int count = 0;
		List v3 = new Vector();
		for (int i = 0; i < size; i++) {
			v2 = (List)v.get(i);
			s = (String)v2.get(0);
			s = s.trim();

			// Only add those database that start with "HydroBase"
			if (StringUtil.startsWithIgnoreCase(s, __HYDROBASE)) {
				v3.add(s);
				__DatabaseName_JComboBox.add(s);
				count++;
			}
		}

		if (count == 0) {
			// no databases were found that started with "HydroBase"
			v3.add(__NO_DATABASES);
			__DatabaseName_JComboBox.add(__NO_DATABASES);
		}
		else {
			if (__DatabaseName_JComboBox.contains(__HYDROBASE)) {
				__DatabaseName_JComboBox.select(__HYDROBASE);
			}
		}
	}
	catch (Exception e) {
		__DatabaseName_JComboBox.removeAllItems();
		__DatabaseName_JComboBox.add(__NO_DATABASES);
	}
	__ignoreItemStateChange = false;
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

List __serverNames = null;

/**
Reads the configuration file.
*/
private void readConfigurationFile() {
	PropList configurationProps = new PropList("Config");
	configurationProps.setPersistentName(
		IOUtil.getApplicationHomeDir() + File.separator + "system" + File.separator + "CDSS.cfg");
	try {
		configurationProps.readPersistent();
	}
	catch (Exception e) {
		// Ignore -- probably a file not found error, in which 
		// the result is the same as an empty file: an empty proplist.
	}

	String serverNames = configurationProps.getValue("HydroBase.ServerNames");
	if (serverNames == null) {
		__serverNames = new Vector();
		if (IOUtil.testing()) {
			__serverNames.add("hbserver");
		}
		else {
			__serverNames.add("greenmtn.state.co.us");
		}

		// if SQL Server is running locally (eg, MSDE), add the local machine to the list of serves that
		// can be connected to.
		if (!IOUtil.isPortOpen(21784)) {
			__serverNames.add("local");
		}
	}
	else {	
		__serverNames = StringUtil.breakStringList(serverNames, ",", 0);
	}

	int size = __serverNames.size();
	String s = null;
	for (int i = 0; i < size; i++) {
		s = (String)__serverNames.get(i);
		if (s.equalsIgnoreCase("local")) {
			s = IOUtil.getProgramHost();
		}
		s = s.toLowerCase().trim();
		__serverNames.set(i,s);
	}

	__serverNames = StringUtil.sortStringList(__serverNames);

	String defaultServerName = configurationProps.getValue("HydroBase.DefaultServerName");
	if (defaultServerName == null) {
		if (IOUtil.testing()) {
			__defaultServerName = "hbserver";
		}
		else {
			__defaultServerName = "greenmtn.state.co.us";
		}
	}
	else {
		__defaultServerName = defaultServerName;
	}

	String defaultDatabaseName = configurationProps.getValue("HydroBase.DefaultDatabaseName");
	if (defaultDatabaseName == null) {
		__defaultDatabaseName = __HYDROBASE;
	}
	else {
		__defaultDatabaseName = defaultDatabaseName;
	}
}

String __defaultServerName = null;
String __defaultDatabaseName = null;

}