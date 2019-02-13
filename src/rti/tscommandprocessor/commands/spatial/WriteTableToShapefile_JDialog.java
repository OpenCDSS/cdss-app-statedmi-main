// WriteTableToShapefile_JDialog - Command editor dialog for the WriteTableToShapefile() command.

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

package rti.tscommandprocessor.commands.spatial;

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

import javax.swing.JFileChooser;
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

import java.io.File;
import java.util.List;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Command editor dialog for the WriteTableToShapefile() command.
*/
@SuppressWarnings("serial")
public class WriteTableToShapefile_JDialog extends JDialog
implements ActionListener, KeyListener, ItemListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __cancel_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null;
private WriteTableToShapefile_Command __command = null;
private String __working_dir = null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __OutputFile_JTextField = null;
private JTabbedPane __main_JTabbedPane = null;
private JTextField __LongitudeColumn_JTextField = null;
private JTextField __LatitudeColumn_JTextField = null;
private JTextField __ElevationColumn_JTextField = null;
private JTextField __WKTGeometryColumn_JTextField = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextField __ExcludeColumns_JTextField = null;
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Has user pressed OK to close the dialog.

/**
Dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTableToShapefile_JDialog ( JFrame parent, WriteTableToShapefile_Command command, List<String> tableIDChoices )
{   super(parent, true);
    initialize ( parent, command, tableIDChoices );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{   Object o = event.getSource();

    if ( o == __browse_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser( __working_dir );
        }
        fc.setDialogTitle("Select Shapefile File to Write");
        SimpleFileFilter sff = new SimpleFileFilter("shp", "Shapefile");
        fc.addChoosableFileFilter(sff);
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String filename = fc.getSelectedFile().getName(); 
            String path = fc.getSelectedFile().getPath(); 
    
            if (filename == null || filename.equals("")) {
                return;
            }
    
            if (path != null) {
				// Convert path to relative path by default.
				try {
					__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1,"WriteTableToShapefile_JDialog", "Error converting file to relative path." );
				}
                JGUIUtil.setLastFileDialogDirectory(directory );
                refresh();
            }
        }
    }
    else if ( o == __cancel_JButton ) {
        response ( false );
    }
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "WriteTableToShapefile");
	}
    else if ( o == __ok_JButton ) {
        refresh ();
        checkInput();
        if ( !__error_wait ) {
            response ( true );
        }
    }
    else if ( o == __path_JButton ) {
        if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
            __OutputFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText() ) );
        }
        else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
            try {
                __OutputFile_JTextField.setText (
                IOUtil.toRelativePath ( __working_dir, __OutputFile_JTextField.getText() ) );
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, "WriteTableToShapefile_JDialog", "Error converting file to relative path." );
            }
        }
        refresh ();
    }
}

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
{   // Put together a list of parameters to check...
    PropList parameters = new PropList ( "" );
    String TableID = __TableID_JComboBox.getSelected();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    String LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    String ElevationColumn = __ElevationColumn_JTextField.getText().trim();
    String WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText().trim();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ExcludeColumns = __IncludeColumns_JTextField.getText().trim();

    __error_wait = false;

    if ( TableID.length() > 0 ) {
        parameters.set ( "TableID", TableID );
    }
    if ( OutputFile.length() > 0 ) {
        parameters.set ( "OutputFile", OutputFile );
    }
    if ( LongitudeColumn.length() > 0 ) {
        parameters.set ( "LongitudeColumn", LongitudeColumn );
    }
    if ( LatitudeColumn.length() > 0 ) {
        parameters.set ( "LatitudeColumn", LatitudeColumn );
    }
    if ( ElevationColumn.length() > 0 ) {
        parameters.set ( "ElevationColumn", ElevationColumn );
    }
    if ( WKTGeometryColumn.length() > 0 ) {
        parameters.set ( "WKTGeometryColumn", WKTGeometryColumn );
    }
    if ( IncludeColumns.length() > 0 ) {
        parameters.set ( "IncludeColumns", IncludeColumns );
    }
    if ( ExcludeColumns.length() > 0 ) {
        parameters.set ( "ExcludeColumns", ExcludeColumns );
    }
    try {
        // This will warn the user...
        __command.checkCommandParameters ( parameters, null, 1 );
    }
    catch ( Exception e ) {
        // The warning would have been printed in the check code.
        Message.printWarning(2,"",e);
        __error_wait = true;
    }
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{   String TableID = __TableID_JComboBox.getSelected();
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    String LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    String ElevationColumn = __ElevationColumn_JTextField.getText();
    String WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "OutputFile", OutputFile );
    __command.setCommandParameter ( "LongitudeColumn", LongitudeColumn );
    __command.setCommandParameter ( "LatitudeColumn", LatitudeColumn );
    __command.setCommandParameter ( "ElevationColumn", ElevationColumn );
    __command.setCommandParameter ( "WKTGeometryColumn", WKTGeometryColumn );
    __command.setCommandParameter ( "IncludeColumns", IncludeColumns );
    __command.setCommandParameter ( "ExcludeColumns", ExcludeColumns );
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
private void initialize ( JFrame parent, WriteTableToShapefile_Command command, List<String> tableIDChoices )
{   __command = command;
    CommandProcessor processor = __command.getCommandProcessor();
    __working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

    addWindowListener( this );

    Insets insetsTLBR = new Insets(1,2,1,2);

    JPanel main_JPanel = new JPanel();
    main_JPanel.setLayout( new GridBagLayout() );
    getContentPane().add ( "North", main_JPanel );
    int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Write a table to an Esri Shapefile, for use in spatial data processing and visualization." ),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Longitude, latitude, elevation, and other attributes are taken from table columns.  The working directory is:" ),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "  " + __working_dir ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The output filename can be specified using ${Property} notation to utilize global properties."),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table to output or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - table to output."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Shapefile to write:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputFile_JTextField = new JTextField ( 50 );
    __OutputFile_JTextField.setToolTipText("Specify the path to the output file or use ${Property} notation");
    __OutputFile_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
        1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    __browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
        6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectory,this);
	    JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
    
    __main_JTabbedPane = new JTabbedPane ();
    //__main_JTabbedPane.setBorder(
    //    BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
    //    "Specify SQL" ));
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
     
    // Panel for point data in separate columns
    int yPoint = -1;
    JPanel point_JPanel = new JPanel();
    point_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Point Data", point_JPanel );
    
    JGUIUtil.addComponent(point_JPanel, new JLabel (
        "If the data are for a point layer, then spatial information can be specified from separate table columns (below)."),
        0, ++yPoint, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel (
        "Otherwise, specify shape data using parameters in the Geometry Data tab."),
        0, ++yPoint, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yPoint, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Longitude (X) column:" ),
        0, ++yPoint, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LongitudeColumn_JTextField = new JTextField ( "", 20 );
    __LongitudeColumn_JTextField.setToolTipText("Longitude is negative if in the Western Hemisphere");
    __LongitudeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(point_JPanel, __LongitudeColumn_JTextField,
        1, yPoint, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Required - column containing longitude, decimal degrees."),
        3, yPoint, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Latitude (Y) column:" ),
        0, ++yPoint, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LatitudeColumn_JTextField = new JTextField ( "", 20 );
    __LatitudeColumn_JTextField.setToolTipText("Latitude is negative if in the Southern Hemisphere");
    __LatitudeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(point_JPanel, __LatitudeColumn_JTextField,
        1, yPoint, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Required - column containing latitude, decimal degrees."),
        3, yPoint, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Elevation (Z) column:" ),
        0, ++yPoint, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ElevationColumn_JTextField = new JTextField ( "", 20 );
    __ElevationColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(point_JPanel, __ElevationColumn_JTextField,
        1, yPoint, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Optional - column containing elevation."),
        3, yPoint, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for geometry data in WKT column
    int yGeom = -1;
    JPanel geom_JPanel = new JPanel();
    geom_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Geometry Data", geom_JPanel );
    
    JGUIUtil.addComponent(geom_JPanel, new JLabel (
        "Geometry (shape) data can be specified using Well Known Text (WKT) strings in a table column."),
        0, ++yGeom, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(geom_JPanel, new JLabel (
        "Currently only POINT and POLYGON geometry are recognized but support for other geometry types will be added in the future."),
        0, ++yGeom, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(geom_JPanel, new JLabel (
        "Coordinates in the WKT strings must be geographic (longitude and latitude decimal degrees)."),
        0, ++yGeom, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(geom_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yGeom, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(geom_JPanel, new JLabel ( "WKT geometry column:" ),
        0, ++yGeom, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __WKTGeometryColumn_JTextField = new JTextField ( "", 20 );
    __WKTGeometryColumn_JTextField.setToolTipText("Longitude is negative if in the Western Hemisphere");
    __WKTGeometryColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(geom_JPanel, __WKTGeometryColumn_JTextField,
        1, yGeom, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(geom_JPanel, new JLabel ( "Required for geometry data - column containing WKT strings."),
        3, yGeom, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for attributes
    int yAtt = -1;
    JPanel att_JPanel = new JPanel();
    att_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Attributes", att_JPanel );
    
    JGUIUtil.addComponent(att_JPanel, new JLabel (
        "Specify columns to be output in the attribute table (shapefile *.dbf file)."),
        0, ++yAtt, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(att_JPanel, new JLabel (
        "These parameters are envisioned for a future software update."),
        0, ++yAtt, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(att_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yAtt, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(att_JPanel, new JLabel ( "Include columns:" ),
        0, ++yAtt, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField ( "", 30 );
    __IncludeColumns_JTextField.setEnabled(false);
    __IncludeColumns_JTextField.setToolTipText("Names of table columns to include");
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(att_JPanel, __IncludeColumns_JTextField,
        1, yAtt, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(att_JPanel, new JLabel ( "Optional - columns to include (default=all)."),
        3, yAtt, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(att_JPanel, new JLabel ( "Exclude columns:" ),
        0, ++yAtt, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns_JTextField = new JTextField ( "", 30 );
    __ExcludeColumns_JTextField.setEnabled(false);
    __ExcludeColumns_JTextField.setToolTipText("Names of table columns to exclude");
    __ExcludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(att_JPanel, __ExcludeColumns_JTextField,
        1, yAtt, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(att_JPanel, new JLabel ( "Optional - columns to exclude (default=all)."),
        3, yAtt, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __command_JTextArea = new JTextArea ( 4, 50 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
            1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    // South Panel: North
    JPanel button_JPanel = new JPanel();
    button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
        0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    __ok_JButton = new SimpleJButton("OK", "OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
    button_JPanel.add ( __ok_JButton );
    __cancel_JButton = new SimpleJButton("Cancel", "Cancel", this);
    button_JPanel.add ( __cancel_JButton );
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

    setTitle ( "Edit " + __command.getCommandName() + " Command" );
    
    // Refresh the contents...
    checkGUIState();
    refresh ();
    
    pack();
    JGUIUtil.center( this );
    setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e)
{   checkGUIState();
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{   int code = event.getKeyCode();

    if ( code == KeyEvent.VK_ENTER ) {
        refresh ();
        checkInput();
        if ( !__error_wait ) {
            response ( true );
        }
    }
}

public void keyReleased ( KeyEvent event )
{   refresh();
}

public void keyTyped ( KeyEvent event )
{
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{   String routine = getClass().getSimpleName() + ".refresh";
    String TableID = "";
    String OutputFile = "";
    String LongitudeColumn = "";
    String LatitudeColumn = "";
    String ElevationColumn = "";
    String WKTGeometryColumn = "";
    String IncludeColumns = "";
    String ExcludeColumns = "";
    __error_wait = false;
    PropList parameters = null;
    if ( __first_time ) {
        __first_time = false;
        // Get the parameters from the command...
        parameters = __command.getCommandParameters();
        TableID = parameters.getValue ( "TableID" );
        OutputFile = parameters.getValue ( "OutputFile" );
        LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
        LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
        ElevationColumn = parameters.getValue ( "ElevationColumn" );
        WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
        IncludeColumns = parameters.getValue ( "IncludeColumns" );
        ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
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
        if ( OutputFile != null ) {
            __OutputFile_JTextField.setText (OutputFile);
        }
        if ( LongitudeColumn != null ) {
            __LongitudeColumn_JTextField.setText (LongitudeColumn);
        }
        if ( LatitudeColumn != null ) {
            __LatitudeColumn_JTextField.setText (LatitudeColumn);
        }
        if ( ElevationColumn != null ) {
            __ElevationColumn_JTextField.setText (ElevationColumn);
        }
        if ( WKTGeometryColumn != null ) {
            __WKTGeometryColumn_JTextField.setText (WKTGeometryColumn);
        }
        if ( IncludeColumns != null ) {
            __IncludeColumns_JTextField.setText (IncludeColumns);
        }
        if ( ExcludeColumns != null ) {
            __ExcludeColumns_JTextField.setText (ExcludeColumns);
        }
    }
    // Regardless, reset the command from the fields...
    TableID = __TableID_JComboBox.getSelected();
    OutputFile = __OutputFile_JTextField.getText().trim();
    LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    ElevationColumn = __ElevationColumn_JTextField.getText().trim();
    WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText().trim();
    IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
    parameters = new PropList ( __command.getCommandName() );
    parameters.add ( "TableID=" + TableID );
    parameters.add ( "OutputFile=" + OutputFile );
    parameters.add ( "LongitudeColumn=" + LongitudeColumn );
    parameters.add ( "LatitudeColumn=" + LatitudeColumn );
    parameters.add ( "ElevationColumn=" + ElevationColumn );
    parameters.add ( "WKTGeometryColumn=" + WKTGeometryColumn );
    parameters.add ( "IncludeColumns=" + IncludeColumns );
    parameters.add ( "ExcludeColumns=" + ExcludeColumns );
    __command_JTextArea.setText( __command.toString ( parameters ) );
    if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        if ( __path_JButton != null ) {
            __path_JButton.setEnabled ( false );
        }
    }
    if ( __path_JButton != null ) {
        __path_JButton.setEnabled ( true );
        File f = new File ( OutputFile );
        if ( f.isAbsolute() ) {
            __path_JButton.setText ( __RemoveWorkingDirectory );
			__path_JButton.setToolTipText("Change path to relative to command file");
        }
        else {
            __path_JButton.setText ( __AddWorkingDirectory );
			__path_JButton.setToolTipText("Change path to absolute");
        }
    }
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{   __ok = ok;  // Save to be returned by ok()
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
{   response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
