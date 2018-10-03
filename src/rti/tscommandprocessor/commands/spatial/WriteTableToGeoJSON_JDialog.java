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
Command editor dialog for the WriteTableToGeoJSON() command.
*/
@SuppressWarnings("serial")
public class WriteTableToGeoJSON_JDialog extends JDialog
implements ActionListener, KeyListener, ItemListener, WindowListener
{

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private SimpleJButton __cancel_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __path_JButton = null;
private WriteTableToGeoJSON_Command __command = null;
private String __working_dir = null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __Append_JComboBox = null;
private JTabbedPane __main_JTabbedPane = null;
private JTextField __LongitudeColumn_JTextField = null;
private JTextField __LatitudeColumn_JTextField = null;
private JTextField __ElevationColumn_JTextField = null;
private JTextField __WKTGeometryColumn_JTextField = null;
private JTextArea __CRSText_JTextArea = null;
private SimpleJComboBox __IncludeBBox_JComboBox = null;
private SimpleJComboBox __IncludeFeatureBBox_JComboBox = null;
private JTextField __IncludeColumns_JTextField = null;
private JTextField __ExcludeColumns_JTextField = null;
private JTextField __JavaScriptVar_JTextField = null;
private JTextField __PrependText_JTextField = null;
private JTextField __AppendText_JTextField = null;
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Has user pressed OK to close the dialog.

/**
Dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public WriteTableToGeoJSON_JDialog ( JFrame parent, WriteTableToGeoJSON_Command command, List<String> tableIDChoices )
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
        fc.setDialogTitle("Select GeoJSON File to Write");
        SimpleFileFilter sff = new SimpleFileFilter("json", "GeoJSON");
        fc.addChoosableFileFilter(sff);
        fc.addChoosableFileFilter (new SimpleFileFilter("js", "GeoJSON JavaScript"));
        
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
					Message.printWarning ( 1,"WriteTableToGeoJSON_JDialog", "Error converting file to relative path." );
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
		HelpViewer.getInstance().showHelp("command", "WriteTableToGeoJSON");
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
                Message.printWarning ( 1, "WriteTableToGeoJSON_JDialog", "Error converting file to relative path." );
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
    String Append = __Append_JComboBox.getSelected();
    String LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    String LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    String ElevationColumn = __ElevationColumn_JTextField.getText().trim();
    String WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText().trim();
	String CRSText = __CRSText_JTextArea.getText().trim();
	String IncludeBBox = __IncludeBBox_JComboBox.getSelected();
	String IncludeFeatureBBox = __IncludeFeatureBBox_JComboBox.getSelected();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ExcludeColumns = __IncludeColumns_JTextField.getText().trim();
    String JavaScriptVar = __JavaScriptVar_JTextField.getText().trim();
    String PrependText = __PrependText_JTextField.getText().trim();
    String AppendText = __AppendText_JTextField.getText().trim();

    __error_wait = false;

    if ( TableID.length() > 0 ) {
        parameters.set ( "TableID", TableID );
    }
    if ( OutputFile.length() > 0 ) {
        parameters.set ( "OutputFile", OutputFile );
    }
    if ( Append.length() > 0 ) {
        parameters.set ( "Append", Append );
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
    if ( CRSText.length() > 0 ) {
    	parameters.set ( "CRSText", CRSText );
    }
    if ( IncludeBBox.length() > 0 ) {
    	parameters.set ( "IncludeBBox", IncludeBBox );
    }
    if ( IncludeFeatureBBox.length() > 0 ) {
    	parameters.set ( "IncludeFeatureBBox", IncludeFeatureBBox );
    }
    if ( IncludeColumns.length() > 0 ) {
        parameters.set ( "IncludeColumns", IncludeColumns );
    }
    if ( ExcludeColumns.length() > 0 ) {
        parameters.set ( "ExcludeColumns", ExcludeColumns );
    }
    if ( JavaScriptVar.length() > 0 ) {
        parameters.set ( "JavaScriptVar", JavaScriptVar );
    }
    if ( PrependText.length() > 0 ) {
        parameters.set ( "PrependText", PrependText );
    }
    if ( AppendText.length() > 0 ) {
        parameters.set ( "AppendText", AppendText );
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
    String Append = __Append_JComboBox.getSelected();
    String LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    String LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    String ElevationColumn = __ElevationColumn_JTextField.getText();
    String WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText();
    String CRSText = __CRSText_JTextArea.getText().trim();
    if ( CRSText != null ) {
    	CRSText = CRSText.replace("\"", "\\\"");
    }
	String IncludeBBox = __IncludeBBox_JComboBox.getSelected();
	String IncludeFeatureBBox = __IncludeFeatureBBox_JComboBox.getSelected();
    String IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    String ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
    String JavaScriptVar = __JavaScriptVar_JTextField.getText().trim();
    String PrependText = __PrependText_JTextField.getText().trim();
    String AppendText = __AppendText_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "OutputFile", OutputFile );
    __command.setCommandParameter ( "Append", Append );
    __command.setCommandParameter ( "LongitudeColumn", LongitudeColumn );
    __command.setCommandParameter ( "LatitudeColumn", LatitudeColumn );
    __command.setCommandParameter ( "ElevationColumn", ElevationColumn );
    __command.setCommandParameter ( "WKTGeometryColumn", WKTGeometryColumn );
    __command.setCommandParameter ( "CRSText", CRSText );
    __command.setCommandParameter ( "IncludeBBox", IncludeBBox );
    __command.setCommandParameter ( "IncludeFeatureBBox", IncludeFeatureBBox );
    __command.setCommandParameter ( "IncludeColumns", IncludeColumns );
    __command.setCommandParameter ( "ExcludeColumns", ExcludeColumns );
    __command.setCommandParameter ( "JavaScriptVar", JavaScriptVar );
    __command.setCommandParameter ( "PrependText", PrependText );
    __command.setCommandParameter ( "AppendText", AppendText );
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
private void initialize ( JFrame parent, WriteTableToGeoJSON_Command command, List<String> tableIDChoices )
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
        "Write a table to a GeoJSON file, for use in spatial data processing and visualization." ),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Longitude, latitude, elevation, and other GeoJSON values are taken from table columns.  The working directory is:" ),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "  " + __working_dir ), 
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
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
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "GeoJSON file to write:" ), 
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
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Append?:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Append_JComboBox = new SimpleJComboBox ( false ); // Allow edit
    __Append_JComboBox.add ( "" );
    __Append_JComboBox.add ( __command._False );
    __Append_JComboBox.add ( __command._True );
    __Append_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Append_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Optional - append content to file? (default=" + __command._False + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
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
    __LongitudeColumn_JTextField.setToolTipText("Longitude column, can use ${Property}, longitude is negative if in the Western Hemisphere");
    __LongitudeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(point_JPanel, __LongitudeColumn_JTextField,
        1, yPoint, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Required - column containing longitude, decimal degrees."),
        3, yPoint, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Latitude (Y) column:" ),
        0, ++yPoint, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __LatitudeColumn_JTextField = new JTextField ( "", 20 );
    __LatitudeColumn_JTextField.setToolTipText("Latitude column, can use ${Property}, latitude is negative if in the Southern Hemisphere");
    __LatitudeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(point_JPanel, __LatitudeColumn_JTextField,
        1, yPoint, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Required - column containing latitude, decimal degrees."),
        3, yPoint, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(point_JPanel, new JLabel ( "Elevation (Z) column:" ),
        0, ++yPoint, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ElevationColumn_JTextField = new JTextField ( "", 20 );
    __ElevationColumn_JTextField.setToolTipText("Elevation column, can use ${Property}");
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
    
    // Panel for coordinate reference system (CRS)
    int ycrs = -1;
    JPanel crs_JPanel = new JPanel();
    crs_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Coordinate Reference System", crs_JPanel );
    
    JGUIUtil.addComponent(crs_JPanel, new JLabel (
        "Coordinate reference system text can be specified using one-line syntax similar to the following (in this case for geographic coordinates)."),
        0, ++ycrs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(crs_JPanel, new JLabel (
        "The double quotes in the text will automatically be replaced with \\s in the command parameter value to escape from normal command quotes."),
        0, ++ycrs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(crs_JPanel, new JLabel (
        "\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },"),
        0, ++ycrs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(crs_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++ycrs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(crs_JPanel, new JLabel ("CRS text:"), 
        0, ++ycrs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __CRSText_JTextArea = new JTextArea (6,30);
    __CRSText_JTextArea.setLineWrap ( true );
    __CRSText_JTextArea.setWrapStyleWord ( true );
    __CRSText_JTextArea.addKeyListener(this);
    JGUIUtil.addComponent(crs_JPanel, new JScrollPane(__CRSText_JTextArea),
        1, ycrs, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(crs_JPanel, new JLabel ( "Optional - CRS text to insert (default=no CRS data=geographic)."),
        3, ycrs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for bounding box
    int yBbox = -1;
    JPanel bbox_JPanel = new JPanel();
    bbox_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Bounding Box", bbox_JPanel );
    
    JGUIUtil.addComponent(bbox_JPanel, new JLabel (
        "The bounding box is by default output at the layer (FeatureCollection) and Feature level."),
        0, ++yBbox, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(bbox_JPanel, new JLabel (
        "This allows visualization software to quickly zoom to the bounding box."),
        0, ++yBbox, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(bbox_JPanel, new JLabel (
        "Parameters are provided to turn off this feature if necessary."),
        0, ++yBbox, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(bbox_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yBbox, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(bbox_JPanel, new JLabel ( "Include layer bounding box?:" ), 
        0, ++yBbox, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeBBox_JComboBox = new SimpleJComboBox ( false ); // Allow edit
    __IncludeBBox_JComboBox.add ( "" );
    __IncludeBBox_JComboBox.add ( __command._False );
    __IncludeBBox_JComboBox.add ( __command._True );
    __IncludeBBox_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(bbox_JPanel, __IncludeBBox_JComboBox,
        1, yBbox, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(bbox_JPanel, new JLabel( "Optional - include layer bbox? (default=" + __command._True + ")."), 
        3, yBbox, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(bbox_JPanel, new JLabel ( "Include feature bounding box?:" ), 
        0, ++yBbox, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeFeatureBBox_JComboBox = new SimpleJComboBox ( false ); // Allow edit
    __IncludeFeatureBBox_JComboBox.add ( "" );
    __IncludeFeatureBBox_JComboBox.add ( __command._False );
    __IncludeFeatureBBox_JComboBox.add ( __command._True );
    __IncludeFeatureBBox_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(bbox_JPanel, __IncludeFeatureBBox_JComboBox,
        1, yBbox, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(bbox_JPanel, new JLabel( "Optional - include feature bbox? (default=" + __command._True + ")."), 
        3, yBbox, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for properties
    int yProp = -1;
    JPanel prop_JPanel = new JPanel();
    prop_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Properties", prop_JPanel );
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel (
        "Specify columns to be output in the GeoJSON feature \"properties\" list."),
        0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(prop_JPanel, new JLabel ( "Include columns:" ),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeColumns_JTextField = new JTextField ( "", 30 );
    __IncludeColumns_JTextField.setToolTipText("Comma-separated names of table columns to include, can use ${Property}");
    __IncludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(prop_JPanel, __IncludeColumns_JTextField,
        1, yProp, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel, new JLabel ( "Optional - columns to include (default=include all)."),
        3, yProp, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel ( "Exclude columns:" ),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ExcludeColumns_JTextField = new JTextField ( "", 30 );
    __ExcludeColumns_JTextField.setToolTipText("Comma-separated names of table columns to exclude, can use ${Property}");
    __ExcludeColumns_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(prop_JPanel, __ExcludeColumns_JTextField,
        1, yProp, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel, new JLabel ( "Optional - columns to exclude (default=exclude none)."),
        3, yProp, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for JavaScript
    int yJs = -1;
    JPanel js_JPanel = new JPanel();
    js_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "JavaScript", js_JPanel );
    
    JGUIUtil.addComponent(js_JPanel, new JLabel (
        "The default is to output GeoJSon in a format similar to the following:"),
        0, ++yJs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(js_JPanel, new JLabel (
        "<html><pre>{\n" +
    	"  \"type\": \"FeatureCollection\",\n" +
    	"  \"features\": [\n" +
    	"    {\n" +
    	"      \"type\": \"Feature\",\n" +
    	"      \"properties\": {\n" +
      	"      }\n" +
      	"      \"geometry\": {\n" +
      	"        \"type\": \"Point\",\n" +
      	"        \"coordinates\": [-105.89194, 38.99333]\n" +
      	"      }\n" +
      	"    }, { repeat for each feature },...\n" +
      	"  ]\n" +
    	"}</pre></html>"),
        0, ++yJs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(js_JPanel, new JLabel (
        "The entire output will correspond to one JavaScript object."),
        0, ++yJs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(js_JPanel, new JLabel (
        "However, if a JavaScript variable is specified, the object will be assigned to a JavaScript variable.  "
        + "This allows direct use of the file in a website."),
        0, ++yJs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(js_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yJs, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(js_JPanel, new JLabel ( "JavaScript variable:" ),
        0, ++yJs, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __JavaScriptVar_JTextField = new JTextField ( "", 20 );
    __JavaScriptVar_JTextField.setToolTipText("JavaScript variable, can use ${Property}");
    __JavaScriptVar_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(js_JPanel, __JavaScriptVar_JTextField,
        1, yJs, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(js_JPanel, new JLabel ( "Optional - JavaScript variable for GeoJSON object (default=none)."),
        3, yJs, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for inserts
    int yInsert = -1;
    JPanel insert_JPanel = new JPanel();
    insert_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Text Inserts", insert_JPanel );
    
    JGUIUtil.addComponent(insert_JPanel, new JLabel (
        "Specify text to insert before and after the GeoJSON.  For example, use the following to initialize the object in an array:"),
        0, ++yInsert, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "prepend:  var stationData = []; stationData['Org1'] = "),
        0, ++yInsert, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "append:  ;"),
        0, ++yInsert, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(insert_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yInsert, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "Prepend text:" ),
        0, ++yInsert, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PrependText_JTextField = new JTextField ( "", 35 );
    __PrependText_JTextField.setToolTipText("Text to prepend before GeoJSON - can include ${Property}");
    __PrependText_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(insert_JPanel, __PrependText_JTextField,
        1, yInsert, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "Optional - text to prepend before GeoJSON object (default=none)."),
        3, yInsert, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "Append text:" ),
        0, ++yInsert, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AppendText_JTextField = new JTextField ( "", 35 );
    __AppendText_JTextField.setToolTipText("Text to append at end of GeoJSON - can include ${Property}");
    __AppendText_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(insert_JPanel, __AppendText_JTextField,
        1, yInsert, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(insert_JPanel, new JLabel ( "Optional - text to append after GeoJSON object (default=none)."),
        3, yInsert, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
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
    String Append = "";
    String LongitudeColumn = "";
    String LatitudeColumn = "";
    String ElevationColumn = "";
    String WKTGeometryColumn = "";
    String CRSText = "";
    String IncludeBBox = "";
    String IncludeFeatureBBox = "";
    String IncludeColumns = "";
    String ExcludeColumns = "";
    String JavaScriptVar = "";
    String PrependText = "";
    String AppendText = "";
    __error_wait = false;
    PropList parameters = null;
    if ( __first_time ) {
        __first_time = false;
        // Get the parameters from the command...
        parameters = __command.getCommandParameters();
        TableID = parameters.getValue ( "TableID" );
        OutputFile = parameters.getValue ( "OutputFile" );
        Append = parameters.getValue ( "Append" );
        LongitudeColumn = parameters.getValue ( "LongitudeColumn" );
        LatitudeColumn = parameters.getValue ( "LatitudeColumn" );
        ElevationColumn = parameters.getValue ( "ElevationColumn" );
        WKTGeometryColumn = parameters.getValue ( "WKTGeometryColumn" );
        CRSText = parameters.getValue ( "CRSText" );
        if ( CRSText != null ) {
        	CRSText = CRSText.replace("\\\"","\""); // For display, don't escape
        }
        IncludeBBox = parameters.getValue ( "IncludeBBox" );
        IncludeFeatureBBox = parameters.getValue ( "IncludeFeatureBBox" );
        IncludeColumns = parameters.getValue ( "IncludeColumns" );
        ExcludeColumns = parameters.getValue ( "ExcludeColumns" );
        JavaScriptVar = parameters.getValue ( "JavaScriptVar" );
        PrependText = parameters.getValue ( "PrependText" );
        AppendText = parameters.getValue ( "AppendText" );
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
        if ( Append == null ) {
            // Select default...
            __Append_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __Append_JComboBox, Append, JGUIUtil.NONE, null, null ) ) {
                __Append_JComboBox.select ( Append );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nAppend value \"" + Append +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
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
        if ( CRSText != null ) {
            __CRSText_JTextArea.setText (CRSText);
        }
        if ( IncludeBBox == null ) {
            // Select default...
            __IncludeBBox_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeBBox_JComboBox, IncludeBBox, JGUIUtil.NONE, null, null ) ) {
                __IncludeBBox_JComboBox.select ( IncludeBBox );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nIncludeBBox value \"" + IncludeBBox +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( IncludeFeatureBBox == null ) {
            // Select default...
            __IncludeFeatureBBox_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeFeatureBBox_JComboBox, IncludeFeatureBBox, JGUIUtil.NONE, null, null ) ) {
                __IncludeFeatureBBox_JComboBox.select ( IncludeFeatureBBox );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nIncludeFeatureBBox value \"" + IncludeFeatureBBox +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( IncludeColumns != null ) {
            __IncludeColumns_JTextField.setText (IncludeColumns);
        }
        if ( ExcludeColumns != null ) {
            __ExcludeColumns_JTextField.setText (ExcludeColumns);
        }
        if ( JavaScriptVar != null ) {
            __JavaScriptVar_JTextField.setText (JavaScriptVar);
        }
        if ( PrependText != null ) {
            __PrependText_JTextField.setText (PrependText);
        }
        if ( AppendText != null ) {
            __AppendText_JTextField.setText (AppendText);
        }
    }
    // Regardless, reset the command from the fields...
    TableID = __TableID_JComboBox.getSelected();
    OutputFile = __OutputFile_JTextField.getText().trim();
    Append = __Append_JComboBox.getSelected();
    LongitudeColumn = __LongitudeColumn_JTextField.getText().trim();
    LatitudeColumn = __LatitudeColumn_JTextField.getText().trim();
    ElevationColumn = __ElevationColumn_JTextField.getText().trim();
    WKTGeometryColumn = __WKTGeometryColumn_JTextField.getText().trim();
    CRSText = __CRSText_JTextArea.getText().trim().replace("\"", "\\\""); // Escape double quotes for internal
    IncludeBBox = __IncludeBBox_JComboBox.getSelected();
    IncludeFeatureBBox = __IncludeFeatureBBox_JComboBox.getSelected();
    IncludeColumns = __IncludeColumns_JTextField.getText().trim();
    ExcludeColumns = __ExcludeColumns_JTextField.getText().trim();
    JavaScriptVar = __JavaScriptVar_JTextField.getText().trim();
    PrependText = __PrependText_JTextField.getText().trim();
    AppendText = __AppendText_JTextField.getText().trim();
    parameters = new PropList ( __command.getCommandName() );
    parameters.add ( "TableID=" + TableID );
    parameters.add ( "OutputFile=" + OutputFile );
    parameters.add ( "Append=" + Append );
    parameters.add ( "LongitudeColumn=" + LongitudeColumn );
    parameters.add ( "LatitudeColumn=" + LatitudeColumn );
    parameters.add ( "ElevationColumn=" + ElevationColumn );
    parameters.add ( "WKTGeometryColumn=" + WKTGeometryColumn );
    parameters.add ( "CRSText=" + CRSText );
    parameters.add ( "IncludeBBox=" + IncludeBBox );
    parameters.add ( "IncludeFeatureBBox=" + IncludeFeatureBBox );
    parameters.add ( "IncludeColumns=" + IncludeColumns );
    parameters.add ( "ExcludeColumns=" + ExcludeColumns );
    parameters.add ( "JavaScriptVar=" + JavaScriptVar );
    parameters.add ( "PrependText=" + PrependText );
    parameters.add ( "AppendText=" + AppendText );
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