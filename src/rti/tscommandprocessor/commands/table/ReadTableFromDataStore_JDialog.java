// ReadTableFromDataStore_JDialog - editor for ReadTableFromDataStore command

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

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import DWR.DMI.StateDMI.StateDMI_Processor;
import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.awt.Color;
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

import RTi.DMI.DMI;
import RTi.DMI.DMIUtil;
import RTi.DMI.DatabaseDataStore;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class ReadTableFromDataStore_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
    
private final String __RemoveWorkingDirectory = "Rel";
private final String __AddWorkingDirectory = "Abs";

private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __DataStore_JComboBox = null;
private JTextField __TableID_JTextField = null;
private JTextField __RowCountProperty_JTextField = null;
private JTabbedPane __sql_JTabbedPane = null;
private SimpleJComboBox __DataStoreCatalog_JComboBox = null;
private SimpleJComboBox __DataStoreSchema_JComboBox = null;
private SimpleJComboBox __DataStoreTable_JComboBox = null;
private JTextField __DataStoreColumns_JTextField = null;
private JTextField __OrderBy_JTextField = null;
private JTextField __Top_JTextField = null;
private JTextArea __Sql_JTextArea = null;
private JTextField __SqlFile_JTextField = null;
private SimpleJComboBox __DataStoreProcedure_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private ReadTableFromDataStore_Command __command = null;
private boolean __ok = false;
private String __working_dir = null;

private boolean __ignoreItemEvents = false; // Used to ignore cascading events when working with choices

private DatabaseDataStore __dataStore = null; // selected data store
private DMI __dmi = null; // DMI to do queries.

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadTableFromDataStore_JDialog ( JFrame parent, ReadTableFromDataStore_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

    if ( o == __browse_JButton ) {
        // Browse for the file to read...
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle( "Select SQL File");
        SimpleFileFilter sff = new SimpleFileFilter("sql","SQL File");
        fc.addChoosableFileFilter(sff);
        
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        if ( last_directory_selected != null ) {
            fc.setCurrentDirectory( new File(last_directory_selected));
        }
        else {
            fc.setCurrentDirectory(new File(__working_dir));
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName(); 
            String path = fc.getSelectedFile().getPath(); 
    
            if (filename == null || filename.equals("")) {
                return;
            }
    
            if (path != null) {
				// Convert path to relative path by default.
				try {
					__SqlFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"CompareFiles_JDialog", "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "ReadTableFromDataStore");
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
        if ( __path_JButton.getText().equals( __AddWorkingDirectory) ) {
            __SqlFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir,__SqlFile_JTextField.getText() ) );
        }
        else if ( __path_JButton.getText().equals( __RemoveWorkingDirectory) ) {
            try {
                __SqlFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __SqlFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, "ReadTableFromDataStore_JDialog", "Error converting file to relative path." );
            }
        }
        refresh ();
    }
}

/**
Refresh the schema choices in response to the currently selected datastore.
*/
private void actionPerformedDataStoreCatalogSelected ( )
{
    if ( __DataStoreCatalog_JComboBox.getSelected() == null ) {
        // Startup initialization
        return;
    }
    __dataStore = getSelectedDataStore();
    __dmi = ((DatabaseDataStore)__dataStore).getDMI();
    //Message.printStatus(2, "", "Selected data store " + __dataStore + " __dmi=" + __dmi );
    // Now populate the schema choices corresponding to the database
    populateDataStoreSchemaChoices ( __dmi );
}

/**
Refresh the database choices in response to the currently selected datastore.
*/
private void actionPerformedDataStoreSelected ( )
{
    if ( __DataStore_JComboBox.getSelected() == null ) {
        // Startup initialization
        return;
    }
    __dataStore = getSelectedDataStore();
    __dmi = ((DatabaseDataStore)__dataStore).getDMI();
    //Message.printStatus(2, "", "Selected data store " + __dataStore + " __dmi=" + __dmi );
    // Now populate the database choices corresponding to the datastore
    populateDataStoreCatalogChoices ( __dmi );
}

/**
Refresh the table choices in response to the currently selected schema.
*/
private void actionPerformedDataStoreSchemaSelected ( )
{
    if ( __DataStoreSchema_JComboBox.getSelected() == null ) {
        // Startup initialization
        return;
    }
    __dataStore = getSelectedDataStore();
    __dmi = ((DatabaseDataStore)__dataStore).getDMI();
    //Message.printStatus(2, "", "Selected data store " + __dataStore + " __dmi=" + __dmi );
    // Now populate the table choices corresponding to the schema
    populateDataStoreTableChoices ( __dmi );
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
    String DataStore = __DataStore_JComboBox.getSelected();
    if ( (DataStore != null) && DataStore.length() > 0 ) {
        props.set ( "DataStore", DataStore );
        __dataStore = getSelectedDataStore();
        __dmi = ((DatabaseDataStore)__dataStore).getDMI();
    }
    else {
        props.set ( "DataStore", "" );
    }
    String DataStoreCatalog = __DataStoreCatalog_JComboBox.getSelected();
    String DataStoreSchema = __DataStoreSchema_JComboBox.getSelected();
	String DataStoreTable = __DataStoreTable_JComboBox.getSelected();
	String DataStoreColumns = __DataStoreColumns_JTextField.getText().trim();
	String OrderBy = __OrderBy_JTextField.getText().trim();
	String Top = __Top_JTextField.getText().trim();
	String Sql = __Sql_JTextArea.getText().trim();
	String SqlFile = __SqlFile_JTextField.getText().trim();
	String DataStoreProcedure = __DataStoreProcedure_JComboBox.getSelected();
    String TableID = __TableID_JTextField.getText().trim();
    String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	__error_wait = false;

    if ( DataStoreCatalog.length() > 0 ) {
        props.set ( "DataStoreCatalog", DataStoreCatalog );
    }
    if ( DataStoreSchema.length() > 0 ) {
        props.set ( "DataStoreSchema", DataStoreSchema );
    }
	if ( DataStoreTable.length() > 0 ) {
		props.set ( "DataStoreTable", DataStoreTable );
	}
    if ( DataStoreColumns.length() > 0 ) {
        props.set ( "DataStoreColumns", DataStoreColumns );
    }
    if ( OrderBy.length() > 0 ) {
        props.set ( "OrderBy", OrderBy );
    }
    if ( Top.length() > 0 ) {
        props.set ( "Top", Top );
    }
    if ( Sql.length() > 0 ) {
        props.set ( "Sql", Sql );
    }
    if ( SqlFile.length() > 0 ) {
        props.set ( "SqlFile", SqlFile );
    }
    if ( DataStoreProcedure.length() > 0 ) {
        props.set ( "DataStoreProcedure", DataStoreProcedure );
    }
    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
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
{	String DataStore = __DataStore_JComboBox.getSelected();
    String DataStoreCatalog = __DataStoreCatalog_JComboBox.getSelected();
    String DataStoreSchema = __DataStoreSchema_JComboBox.getSelected();
    String DataStoreTable = __DataStoreTable_JComboBox.getSelected();
    String DataStoreColumns = __DataStoreColumns_JTextField.getText().trim();
    String OrderBy = __OrderBy_JTextField.getText().trim();
    String Top = __Top_JTextField.getText().trim();
    String Sql = __Sql_JTextArea.getText().trim().replace('\n', ' ').replace('\t', ' ');
    String SqlFile = __SqlFile_JTextField.getText().trim();
    String DataStoreProcedure = __DataStoreProcedure_JComboBox.getSelected();
    String TableID = __TableID_JTextField.getText().trim();
    String RowCountProperty = __RowCountProperty_JTextField.getText().trim();
    __command.setCommandParameter ( "DataStore", DataStore );
    __command.setCommandParameter ( "DataStoreCatalog", DataStoreCatalog );
    __command.setCommandParameter ( "DataStoreSchema", DataStoreSchema );
	__command.setCommandParameter ( "DataStoreTable", DataStoreTable );
	__command.setCommandParameter ( "DataStoreColumns", DataStoreColumns );
	__command.setCommandParameter ( "OrderBy", OrderBy );
	__command.setCommandParameter ( "Top", Top );
	__command.setCommandParameter ( "Sql", Sql );
	__command.setCommandParameter ( "SqlFile", SqlFile );
	__command.setCommandParameter ( "DataStoreProcedure", DataStoreProcedure );
    __command.setCommandParameter ( "TableID", TableID );
	__command.setCommandParameter ( "RowCountProperty", RowCountProperty );
}

/**
Return the DMI that is currently being used for database interaction, based on the selected data store.
*/
private DMI getDMI ()
{
    return __dmi;
}

/**
Get the selected data store.
*/
private DatabaseDataStore getSelectedDataStore ()
{   String routine = getClass().getName() + ".getSelectedDataStore";
    String DataStore = __DataStore_JComboBox.getSelected();
    DatabaseDataStore dataStore = (DatabaseDataStore)((StateDMI_Processor)
        __command.getCommandProcessor()).getDataStoreForName(
        DataStore, DatabaseDataStore.class );
    if ( dataStore != null ) {
        Message.printStatus(2, routine, "Selected data store is \"" + dataStore.getName() + "\"." );
        // Make sure database connection is open
        dataStore.checkDatabaseConnection();
    }
    else {
        Message.printStatus(2, routine, "Cannot get data store for \"" + DataStore + "\"." );
    }
    return dataStore;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, ReadTableFromDataStore_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
    __working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (StateDMI_Processor)processor, __command );

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
        "This command reads a table from a database datastore table, view, or procedure."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The query can be specified in one of four ways:"),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "    1) Specify a single table or view, related columns, and sort order."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "    2) Specify a free form SQL select statement (allows joins and other SQL constructs " +
        "supported by the database software)."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "    3) Similar to 2; however, the SQL statement is read from a file, " +
        "which can be specified relative to the working directory."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        "        The working directory is: " + __working_dir ), 
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(paragraph, new JLabel (
        "    4) Specify a database procedure to run (under development)."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The resulting table columns will have data types based on the query results."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
            0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    // List available data stores of the correct type
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Datastore:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStore_JComboBox = new SimpleJComboBox ( false );
    StateDMI_Processor tsProcessor = (StateDMI_Processor)processor;
    List<DataStore> dataStoreList = tsProcessor.getDataStoresByType( DatabaseDataStore.class );
    List<String> datastoreChoices = new ArrayList<String>();
    for ( DataStore dataStore: dataStoreList ) {
    	datastoreChoices.add ( dataStore.getName() );
    }
    if ( dataStoreList.size() == 0 ) {
        // Add an empty item so users can at least bring up the editor
    	datastoreChoices.add ( "" );
    }
    __DataStore_JComboBox.setData(datastoreChoices);
    __DataStore_JComboBox.select ( 0 );
    __DataStore_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DataStore_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Required - data store containing data to read."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __sql_JTabbedPane = new JTabbedPane ();
    __sql_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Specify how to query the datastore" ));
    JGUIUtil.addComponent(main_JPanel, __sql_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for SQL via table choices
    int yTable = -1;
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout( new GridBagLayout() );
    __sql_JTabbedPane.addTab ( "Table and columns", table_JPanel );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "Specify the catalog and schema as non-blank only if the datastore connection is defined at a higher level and requires " +
        "additional information to locate the table."),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "Database catalog and schema choices currently do not cascade because database driver metadata features are limited or may " +
        "be disabled and not provide information."),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
 
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Datastore catalog (database):"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStoreCatalog_JComboBox = new SimpleJComboBox ( false );
    __DataStoreCatalog_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(table_JPanel, __DataStoreCatalog_JComboBox,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel(
        "<html>Optional - specify if needed for <b>[Database]</b>.[Schema].[Table].</html>"), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Datastore schema:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStoreSchema_JComboBox = new SimpleJComboBox ( false );
    __DataStoreSchema_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(table_JPanel, __DataStoreSchema_JComboBox,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel(
        "<html>Optional - specify if needed for [Database].<b>[Schema]</b>.[Table].</html>"), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Datastore table:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStoreTable_JComboBox = new SimpleJComboBox ( false );
    __DataStoreTable_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(table_JPanel, __DataStoreTable_JComboBox,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel("Required - database table/view to read."), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Datastore columns:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStoreColumns_JTextField = new JTextField (10);
    __DataStoreColumns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __DataStoreColumns_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - database table/view columns, separated by commas (default=all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Order by:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OrderBy_JTextField = new JTextField (10);
    __OrderBy_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __OrderBy_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - columns to sort by, separated by commas (default=no sort)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Top N rows:"),
        0, ++yTable, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Top_JTextField = new JTextField (10);
    __Top_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(table_JPanel, __Top_JTextField,
        1, yTable, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel ("Optional - return top N rows (default=return all)."),
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for SQL via SQL statement
    int ySql = -1;
    JPanel sql_JPanel = new JPanel();
    sql_JPanel.setLayout( new GridBagLayout() );
    __sql_JTabbedPane.addTab ( "SQL string", sql_JPanel );

    JGUIUtil.addComponent(sql_JPanel, new JLabel (
        "Specify SQL as a string. This is useful for simple SQL.  If newlines and formatting are needed, use an SQL file instead."),
        0, ++ySql, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(sql_JPanel, new JLabel (
        "SQL specified with ${property} notation will be updated to use processor property values before executing the query."),
        0, ++ySql, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(sql_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++ySql, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(sql_JPanel, new JLabel ("SQL String:"), 
        0, ++ySql, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Sql_JTextArea = new JTextArea (9,50);
    __Sql_JTextArea.setToolTipText("Specify the SQL string, can use ${Property} notation");
    __Sql_JTextArea.setLineWrap ( true );
    __Sql_JTextArea.setWrapStyleWord ( true );
    __Sql_JTextArea.addKeyListener(this);
    JGUIUtil.addComponent(sql_JPanel, new JScrollPane(__Sql_JTextArea),
        1, ySql, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for SQL via SQL file
    int yFile = -1;
    JPanel file_JPanel = new JPanel();
    file_JPanel.setLayout( new GridBagLayout() );
    __sql_JTabbedPane.addTab ( "SQL file", file_JPanel );
    
    JGUIUtil.addComponent(file_JPanel, new JLabel (
        "Specify SQL as a file. This is useful for complex SQL containing formatting such as comments and newlines."),
        0, ++yFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(file_JPanel, new JLabel (
        "SQL specified with ${property} notation will be updated to use processor property values before executing the query."),
        0, ++yFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(file_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yFile, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(file_JPanel, new JLabel ( "SQL file to read:" ), 
        0, ++yFile, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SqlFile_JTextField = new JTextField ( 50 );
    __SqlFile_JTextField.setToolTipText("Specify the SQL file or use ${Property} notation");
    __SqlFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(file_JPanel, __SqlFile_JTextField,
        1, yFile, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    __browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(file_JPanel, __browse_JButton,
        6, yFile, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectory,this);
	    JGUIUtil.addComponent(file_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
        
    // Panel for procedure
    int yProc = -1;
    JPanel proc_JPanel = new JPanel();
    proc_JPanel.setLayout( new GridBagLayout() );
    __sql_JTabbedPane.addTab ( "Procedure", proc_JPanel );

    JGUIUtil.addComponent(proc_JPanel, new JLabel (
        "Run a stored procedure to return results as a table."),
        0, ++yProc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(proc_JPanel, new JLabel (
        "Currently, only procedures that do not require parameters can be run."),
        0, ++yProc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(proc_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yProc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(proc_JPanel, new JLabel ( "Datastore procedure:"),
        0, ++yProc, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStoreProcedure_JComboBox = new SimpleJComboBox ( false );
    __DataStoreProcedure_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(proc_JPanel, __DataStoreProcedure_JComboBox,
        1, yProc, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(proc_JPanel, new JLabel("Optional - database procedure to run to generate results."), 
        3, yProc, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Table for results
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Table ID:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JTextField = new JTextField (10);
    __TableID_JTextField.setToolTipText("Specify the table ID or use ${Property} notation");
    __TableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __TableID_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - unique identifier for the output table."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Row count property:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RowCountProperty_JTextField = new JTextField ( "", 20 );
    __RowCountProperty_JTextField.setToolTipText("The property can be referenced in other commands using ${Property}.");
    __RowCountProperty_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RowCountProperty_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - processor property to set as output table row count." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (6,50);
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
	button_JPanel.add (__ok_JButton);
	__ok_JButton.setToolTipText("Save changes to command");
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command");
	//setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();
    super.setVisible(true);
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent event)
{
    if ( !__ignoreItemEvents ) {
        if ( (event.getSource() == __DataStore_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
            // User has selected a datastore.
            actionPerformedDataStoreSelected ();
        }
        else if ( (event.getSource() == __DataStoreCatalog_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
            // User has selected a catalog.
            actionPerformedDataStoreCatalogSelected ();
        }
        else if ( (event.getSource() == __DataStoreSchema_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
            // User has selected a schema.
            actionPerformedDataStoreSchemaSelected ();
        }
    }
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
Populate the database list based on the selected datastore.
@param dmi DMI to use when selecting database list
*/
@SuppressWarnings("unchecked")
private void populateDataStoreCatalogChoices ( DMI dmi )
{   String routine = getClass().getSimpleName() + ".populateDataStoreDatastoreChoices";
    List<String> catalogList = null;
    List<String> notIncluded = new ArrayList<String>();
    if ( dmi == null ) {
        catalogList = new ArrayList<String>();
    }
    else {
        // TODO SAM 2013-07-22 Improve this - it should only be shown when the master DB is used
        if ( (dmi.getDatabaseEngineType() == DMI.DBENGINE_SQLSERVER) ) {
            try {
                catalogList = DMIUtil.getDatabaseCatalogNames(dmi, true, notIncluded);
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine, "Error getting database catalog (" + e + ")." );
                Message.printWarning ( 3, routine, e );
                catalogList = null;
            }
        }
    }
    if ( catalogList == null ) {
        catalogList = new ArrayList<String>();
    }
    // Always add a blank option at the start to help with initialization
    catalogList.add ( 0, "" );
    __DataStoreCatalog_JComboBox.removeAll();
    for ( String catalog : catalogList ) {
        __DataStoreCatalog_JComboBox.add( catalog );
    }
    // Set large so that new catalog list from selected datastore does not foul up layout
    String longest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    __DataStoreCatalog_JComboBox.setPrototypeDisplayValue(longest);
    // Select first choice (may get reset from existing parameter values).
    __DataStoreCatalog_JComboBox.select ( null );
    if ( __DataStoreCatalog_JComboBox.getItemCount() > 0 ) {
        __DataStoreCatalog_JComboBox.select ( 0 );
    }
}

/**
Populate the procedure list based on the selected database.
@param dmi DMI to use when selecting procedure list
*/
@SuppressWarnings("unchecked")
private void populateDataStoreProcedureChoices ( DMI dmi )
{   String routine = getClass().getSimpleName() + "populateDataStoreProcedureChoices";
    List<String> procList = null;
    List<String> notIncluded = new ArrayList<String>(); // TODO SAM 2012-01-31 need to omit system procedures
    if ( dmi == null ) {
        procList = new ArrayList<String>();
    }
    else {
        try {
            procList = DMIUtil.getDatabaseProcedureNames(dmi, true, notIncluded);
        }
        catch ( Exception e ) {
            Message.printWarning ( 1, routine, "Error getting procedure list (" + e + ")." );
            Message.printWarning ( 3, routine, e );
            procList = null;
        }
    }
    if ( procList == null ) {
        procList = new ArrayList<String>();
    }
    // Always add a blank option at the start to help with initialization
    procList.add ( 0, "" );
    __DataStoreProcedure_JComboBox.removeAll();
    for ( String proc : procList ) {
        __DataStoreProcedure_JComboBox.add( proc );
    }
    // Set large so that new procedure list from selected datastore does not foul up layout
    String longest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    __DataStoreProcedure_JComboBox.setPrototypeDisplayValue(longest);
    // Select first choice (may get reset from existing parameter values).
    __DataStoreProcedure_JComboBox.select ( null );
    if ( __DataStoreProcedure_JComboBox.getItemCount() > 0 ) {
        __DataStoreProcedure_JComboBox.select ( 0 );
    }
}

/**
Populate the schema list based on the selected database.
@param dmi DMI to use when selecting schema list
*/
@SuppressWarnings("unchecked")
private void populateDataStoreSchemaChoices ( DMI dmi )
{   String routine = getClass().getName() + "populateDataStoreSchemaChoices";
    List<String> schemaList = null;
    List<String> notIncluded = new ArrayList<String>(); // TODO SAM 2012-01-31 need to omit system tables
    if ( dmi == null ) {
        schemaList = new ArrayList<String>();
    }
    else {
        // TODO SAM 2013-07-22 Improve this - it should only be shown when the master DB is used
        try {
            String catalog = __DataStoreCatalog_JComboBox.getSelected();
            if ( catalog.equals("") ) {
                catalog = null;
            }
            schemaList = DMIUtil.getDatabaseSchemaNames(dmi, catalog, true, notIncluded);
        }
        catch ( Exception e ) {
            Message.printWarning ( 1, routine, "Error getting database schemas (" + e + ")." );
            Message.printWarning ( 3, routine, e );
            schemaList = null;
        }
    }
    if ( schemaList == null ) {
        schemaList = new ArrayList<String>();
    }
    // Always add a blank option at the start to help with initialization
    schemaList.add ( 0, "" );
    __DataStoreSchema_JComboBox.removeAll();
    for ( String schema : schemaList ) {
        __DataStoreSchema_JComboBox.add( schema );
    }
    // Set large so that new database list from selected schema does not foul up layout
    String longest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    __DataStoreSchema_JComboBox.setPrototypeDisplayValue(longest);
    // Select first choice (may get reset from existing parameter values).
    __DataStoreSchema_JComboBox.select ( null );
    if ( __DataStoreSchema_JComboBox.getItemCount() > 0 ) {
        __DataStoreSchema_JComboBox.select ( 0 );
    }
}

/**
Populate the table list based on the selected database.
@param dmi DMI to use when selecting table list
*/
@SuppressWarnings("unchecked")
private void populateDataStoreTableChoices ( DMI dmi )
{   String routine = getClass().getName() + "populateDataStoreTableChoices";
    List<String> tableList = null;
    List<String> notIncluded = new ArrayList<String>(); // TODO SAM 2012-01-31 need to omit system tables
    if ( dmi == null ) {
        tableList = new ArrayList<String>();
    }
    else {
        try {
            String dbName = __DataStoreCatalog_JComboBox.getSelected();
            if ( dbName.equals("")) {
                dbName = null;
            }
            String schema = __DataStoreSchema_JComboBox.getSelected();
            if ( schema.equals("")) {
                schema = null;
            }
            tableList = DMIUtil.getDatabaseTableNames(dmi, dbName, schema, true, notIncluded);
        }
        catch ( Exception e ) {
            Message.printWarning ( 1, routine, "Error getting table list (" + e + ")." );
            Message.printWarning ( 3, routine, e );
            tableList = null;
        }
    }
    if ( tableList == null ) {
        tableList = new ArrayList<String>();
    }
    // Always add a blank option at the start to help with initialization
    tableList.add ( 0, "" );
    __DataStoreTable_JComboBox.removeAll();
    for ( String table : tableList ) {
        __DataStoreTable_JComboBox.add( table );
    }
    // Set large so that new table list from selected datastore does not foul up layout
    String longest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    __DataStoreTable_JComboBox.setPrototypeDisplayValue(longest);
    // Select first choice (may get reset from existing parameter values).
    __DataStoreTable_JComboBox.select ( null );
    if ( __DataStoreTable_JComboBox.getItemCount() > 0 ) {
        __DataStoreTable_JComboBox.select ( 0 );
    }
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getName() + ".refresh";
try{
    String DataStore = "";
    String DataStoreCatalog = "";
    String DataStoreSchema = "";
    String DataStoreTable = "";
    String DataStoreColumns = "";
    String OrderBy = "";
    String Top = "";
    String Sql = "";
    String SqlFile = "";
    String DataStoreProcedure = "";
    String TableID = "";
    String RowCountProperty = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		DataStore = props.getValue ( "DataStore" );
		DataStoreCatalog = props.getValue ( "DataStoreCatalog" );
		DataStoreSchema = props.getValue ( "DataStoreSchema" );
		DataStoreTable = props.getValue ( "DataStoreTable" );
		DataStoreColumns = props.getValue ( "DataStoreColumns" );
		OrderBy = props.getValue ( "OrderBy" );
		Top = props.getValue ( "Top" );
		Sql = props.getValue ( "Sql" );
		SqlFile = props.getValue ( "SqlFile" );
		DataStoreProcedure = props.getValue ( "DataStoreProcedure" );
		TableID = props.getValue ( "TableID" );
        RowCountProperty = props.getValue ( "RowCountProperty" );
        // The data store list is set up in initialize() but is selected here
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStore_JComboBox, DataStore, JGUIUtil.NONE, null, null ) ) {
            __DataStore_JComboBox.select ( null ); // To ensure that following causes an event
            __DataStore_JComboBox.select ( DataStore ); // This will trigger getting the DMI for use in the editor
        }
        else {
            if ( (DataStore == null) || DataStore.equals("") ) {
                // New command...select the default...
                __DataStore_JComboBox.select ( null ); // To ensure that following causes an event
                __DataStore_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStore parameter \"" + DataStore + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        // First populate the database choices...
        populateDataStoreCatalogChoices(getDMI() );
        // Now select what the command had previously (if specified)...
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStoreCatalog_JComboBox, DataStoreCatalog, JGUIUtil.NONE, null, null ) ) {
            __DataStoreCatalog_JComboBox.select ( DataStoreCatalog );
        }
        else {
            if ( (DataStoreCatalog == null) || DataStoreCatalog.equals("") ) {
                // New command...select the default...
                __DataStoreCatalog_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStoreCatalog parameter \"" + DataStoreCatalog + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        // First populate the database schema...
        populateDataStoreSchemaChoices(getDMI() );
        // Now select what the command had previously (if specified)...
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStoreSchema_JComboBox, DataStoreSchema, JGUIUtil.NONE, null, null ) ) {
            __DataStoreSchema_JComboBox.select ( DataStoreSchema );
        }
        else {
            if ( (DataStoreSchema == null) || DataStoreSchema.equals("") ) {
                // New command...select the default...
                __DataStoreSchema_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStoreSchema parameter \"" + DataStoreSchema + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        // First populate the table choices...
        populateDataStoreTableChoices(getDMI() );
        // Now select what the command had previously (if specified)...
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStoreTable_JComboBox, DataStoreTable, JGUIUtil.NONE, null, null ) ) {
            __DataStoreTable_JComboBox.select ( DataStoreTable );
            __sql_JTabbedPane.setSelectedIndex(0);
        }
        else {
            if ( (DataStoreTable == null) || DataStoreTable.equals("") ) {
                // New command...select the default...
                __DataStoreTable_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStoreTable parameter \"" + DataStoreTable + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( DataStoreColumns != null ) {
            __DataStoreColumns_JTextField.setText ( DataStoreColumns );
        }
        if ( OrderBy != null ) {
            __OrderBy_JTextField.setText ( OrderBy );
        }
        if ( Top != null ) {
            __Top_JTextField.setText ( Top );
        }
        if ( (Sql != null) && !Sql.equals("") ) {
            __Sql_JTextArea.setText ( Sql );
            __sql_JTabbedPane.setSelectedIndex(1);
        }
        if ( (SqlFile != null) && !SqlFile.equals("") ) {
            __SqlFile_JTextField.setText(SqlFile);
            __sql_JTabbedPane.setSelectedIndex(2);
        }
        // First populate the procedure choices...
        populateDataStoreProcedureChoices(getDMI() );
        // Now select what the command had previously (if specified)...
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStoreProcedure_JComboBox, DataStoreProcedure, JGUIUtil.NONE, null, null ) ) {
            __DataStoreProcedure_JComboBox.select ( DataStoreProcedure );
            if ( (DataStoreProcedure != null) && !DataStoreProcedure.equals("") ) {
                __sql_JTabbedPane.setSelectedIndex(3);
            }
        }
        else {
            if ( (DataStoreProcedure == null) || DataStoreProcedure.equals("") ) {
                // New command...select the default...
                __DataStoreProcedure_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStoreProcedure parameter \"" + DataStoreProcedure + "\".  Select a\ndifferent value or Cancel." );
            }
        }
        if ( TableID != null ) {
            __TableID_JTextField.setText ( TableID );
        }
        if ( RowCountProperty != null ) {
            __RowCountProperty_JTextField.setText ( RowCountProperty );
        }
	}
	// Regardless, reset the command from the fields...
    DataStore = __DataStore_JComboBox.getSelected();
    if ( DataStore == null ) {
        DataStore = "";
    }
    DataStoreCatalog = __DataStoreCatalog_JComboBox.getSelected();
    if ( DataStoreCatalog == null ) {
        DataStoreCatalog = "";
    }
    DataStoreSchema = __DataStoreSchema_JComboBox.getSelected();
    if ( DataStoreSchema == null ) {
        DataStoreSchema = "";
    }
    DataStoreTable = __DataStoreTable_JComboBox.getSelected();
    if ( DataStoreTable == null ) {
        DataStoreTable = "";
    }
	DataStoreColumns = __DataStoreColumns_JTextField.getText().trim();
	OrderBy = __OrderBy_JTextField.getText().trim();
	Top = __Top_JTextField.getText().trim();
	Sql = __Sql_JTextArea.getText().trim();
	SqlFile = __SqlFile_JTextField.getText().trim();
    DataStoreProcedure = __DataStoreProcedure_JComboBox.getSelected();
    if ( DataStoreProcedure == null ) {
        DataStoreProcedure = "";
    }
    TableID = __TableID_JTextField.getText().trim();
	RowCountProperty = __RowCountProperty_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "DataStore=" + DataStore );
	props.add ( "DataStoreCatalog=" + DataStoreCatalog );
	props.add ( "DataStoreSchema=" + DataStoreSchema );
	props.add ( "DataStoreTable=" + DataStoreTable );
	props.add ( "DataStoreColumns=" + DataStoreColumns );
	props.add ( "OrderBy=" + OrderBy );
	props.add ( "Top=" + Top );
	props.add ( "Sql=" + Sql );
	props.add ( "SqlFile=" + SqlFile);
	props.add ( "DataStoreProcedure=" + DataStoreProcedure );
    props.add ( "TableID=" + TableID );
	props.add ( "RowCountProperty=" + RowCountProperty );
    
	__command_JTextArea.setText( __command.toString ( props ) );
	// Refresh the Path text.
    refreshPathControl();
}
catch ( Exception e ) {
    Message.printWarning ( 3, routine, e );
}
}

/**
Refresh the PathControl text based on the contents of the input text field contents.
*/
private void refreshPathControl()
{
    String SqlFile = __SqlFile_JTextField.getText().trim();
    if ( (SqlFile == null) || (SqlFile.trim().length() == 0) ) {
        if ( __path_JButton != null ) {
            __path_JButton.setEnabled ( false );
        }
        return;
    }

    // Check the path and determine what the label on the path button should be...
    if ( __path_JButton != null ) {
        __path_JButton.setEnabled ( true );
        File f = new File ( SqlFile );
        if ( f.isAbsolute() ) {
            __path_JButton.setText( __RemoveWorkingDirectory );
			__path_JButton.setToolTipText("Change path to relative to command file");
        }
        else {
            __path_JButton.setText( __AddWorkingDirectory );
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
