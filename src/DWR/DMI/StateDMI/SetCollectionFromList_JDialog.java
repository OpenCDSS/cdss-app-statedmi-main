// SetCollectionFromList_JDialog - Command editor dialog for Set*FromList() (set collections) commands.

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

package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Diversion_CollectionType;
import DWR.StateMod.StateMod_Well_CollectionPartIdType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Command editor dialog for Set*FromList() (set collections) commands.
*/
@SuppressWarnings("serial")
public class SetCollectionFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __ListFile_JTextField = null;// List file
private JTextField __Year_JTextField = null;
private SimpleJComboBox __PartType_JComboBox = null;
private JTextField __Div_JTextField = null;
private SimpleJComboBox __IDCol_JComboBox = null;
private SimpleJComboBox __NameCol_JComboBox = null;
private SimpleJComboBox __PartIDsCol_JComboBox = null;
private JTextField __PartIDTypeColumn_JTextField = null;
private SimpleJComboBox __PartsListedHow_JComboBox = null;
private SimpleJComboBox __PartIDsColMax_JComboBox = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private SetCollectionFromList_Command __command = null;
private boolean __ok = false;

// Used for button labels...

private final String __AddWorkingDirectoryToFile = "Abs";
private final String __RemoveWorkingDirectoryFromFile = "Rel";

/**
Type of collection:  "Aggregate", "System", or "MultiStruct" - see StateMod definitions.
*/
private String __collectionType;
/**
Node type:  "Diversion", "Reservoir", "Well".
*/
private String __nodeType;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCollectionFromList_JDialog ( JFrame parent, Command command ) {
	super(parent, true);
	initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select List File");
		fc.addChoosableFileFilter( new SimpleFileFilter("csv",
			__nodeType + " " + __collectionType + " List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", 
			__nodeType + " " + __collectionType + " List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", 
			__nodeType + " " + __collectionType + " List File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__ListFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
			refresh ();
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals(__AddWorkingDirectoryToFile)) {
			__ListFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __ListFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals(__RemoveWorkingDirectoryFromFile)) {
			try {
				__ListFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __ListFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				"SetCollectionFromList_JDialog", "Error converting file to relative path.");
			}
		}
		refresh ();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String PartIDsCol = __PartIDsCol_JComboBox.getSelected();
	String PartIDTypeColumn = __PartIDTypeColumn_JTextField.getText().trim();
	String PartsListedHow = __PartsListedHow_JComboBox.getSelected();
	String PartIDsColMax = __PartIDsColMax_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ListFile.length() > 0 ) {
		props.set("ListFile", ListFile);
	}
	if ( IDCol.length() > 0 ) {
		props.set("IDCol", IDCol);
	}
	if ( NameCol.length() > 0 ) {
		props.set("NameCol", NameCol);
	}
	if ( PartIDsCol.length() > 0 ) {
		props.set("PartIDsCol", PartIDsCol);
	}
	if ( PartIDTypeColumn.length() > 0 ) {
		props.set("PartIDTypeColumn", PartIDTypeColumn);
	}
	if ( PartsListedHow.length() > 0 ) {
		props.set("PartsListedHow", PartsListedHow);
	}
	if ( PartIDsColMax.length() > 0 ) {
		props.set("PartIDsColMax", PartIDsColMax);
	}
	if ( IfNotFound.length() > 0 ) {
		props.set("IfNotFound", IfNotFound);
	}
	
	if ( __Year_JTextField != null ) {
		String Year = __Year_JTextField.getText().trim();
		if ( Year.length() > 0 ) {
			props.set("Year", Year);
		}
	}
	if ( __Div_JTextField != null ) {
		String Div = __Div_JTextField.getText().trim();
		if ( Div.length() > 0 ) {
			props.set("Div", Div);
		}
	}
	if ( __PartType_JComboBox != null ) {
		String PartType = __PartType_JComboBox.getSelected();
		if ( PartType.length() > 0 ) {
			props.set("PartType", PartType);
		}
	}
	
	__error_wait = false;
	
	try {
		// This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	} 
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
		Message.printWarning(3, "CheckInput", e );
	}
}

/**
Check the state of the UI given choices.  In particular, disable the year and division if not well collections.
*/
public void checkUIState()
{
	if ( __PartType_JComboBox != null ) {
		// Should only be visible for wells.
		String PartType = __PartType_JComboBox.getSelected();
		if ( __nodeType.equalsIgnoreCase(__command._Well) && PartType.equalsIgnoreCase(__command._Parcel)) {
			// Enable the fields
			__Year_JTextField.setEnabled(true);
			__Div_JTextField.setEnabled(true);
		}
		else {
			// Disable and clear the fields
			if ( __Year_JTextField != null ) {
				__Year_JTextField.setText("");
				__Year_JTextField.setEnabled(false);
			}
			if ( __Div_JTextField != null ) {
				__Div_JTextField.setText("");
				__Div_JTextField.setEnabled(false);
			}
		}
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits()
{	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String PartIDsCol = __PartIDsCol_JComboBox.getSelected();
	String PartIDTypeColumn = __PartIDTypeColumn_JTextField.getText().trim();
	String PartsListedHow = __PartsListedHow_JComboBox.getSelected();
	String PartIDsColMax = __PartIDsColMax_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__command.setCommandParameter("ListFile", ListFile);
	__command.setCommandParameter("IDCol", IDCol);
	__command.setCommandParameter("NameCol", NameCol);
	__command.setCommandParameter("PartIDsCol", PartIDsCol);
	__command.setCommandParameter("PartIDTypeColumn", PartIDTypeColumn);
	__command.setCommandParameter("PartsListedHow", PartsListedHow);
	__command.setCommandParameter("PartIDsColMax", PartIDsColMax);
	__command.setCommandParameter("IfNotFound", IfNotFound);
	
	if ( __Year_JTextField != null ) {
		String Year = __Year_JTextField.getText().trim();
		__command.setCommandParameter("Year", Year);
	}
	if ( __Div_JTextField != null ) {
		String Div = __Div_JTextField.getText().trim();
		__command.setCommandParameter("Div", Div);
	}
	if ( __PartType_JComboBox != null ) {
		String PartType = __PartType_JComboBox.getSelected();
		__command.setCommandParameter("PartType", PartType);
	}
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (SetCollectionFromList_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Diversion,0) >= 0 ) {
		__nodeType = __command._Diversion;
	}
	else if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Well,0) >= 0 ) {
		__nodeType = __command._Well;
	}
	else if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Reservoir,0) >= 0 ) {
		__nodeType = __command._Reservoir;
	}
	
	if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.AGGREGATE.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.AGGREGATE.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.SYSTEM.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.SYSTEM.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.MULTISTRUCT.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.MULTISTRUCT.toString();
	}

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
		"This command sets a " + __nodeType + " " + __collectionType +
		" location's information from a list file." ),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __collectionType.equalsIgnoreCase(StateMod_Diversion_CollectionType.MULTISTRUCT.toString()) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "A \"MultiStruct\" is used when demands are met using water from different tributaries." ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "Each diversion station is represented in the model network"),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "and the historical water rights and diversion time series are distinct for each diversion station." ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "However, the efficiencies are estimated using combined demand and historical diversion time series," ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "and total demands are used for the primary structure, with zero demands on the other structure(s)." ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "Operating rules are used to handle sharing diversion water."),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "The primary ID will receive all demands."),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    else {
    	if ( __command instanceof SetWellAggregateFromList_Command ) {
    		// Not really relevant for other station types.
    		JGUIUtil.addComponent(paragraph, new JLabel (
        	    "This command can be used with StateMod " + __nodeType + " stations and StateCU CU Locations."),
        	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	}
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "Each " + __collectionType + " is a location where individual parts are combined into a single feature."),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
		    "An \"Aggregate\" is used with Set" + __nodeType + "AggregateFromList() when water rights will be aggregated into classes." ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "A \"System\" is used with Set" + __nodeType + "SystemFromList() when individual water rights will be maintained." ),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		if ( __nodeType.equalsIgnoreCase(__command._Diversion) ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
        		"For example, multiple nearby or related ditches may be grouped as a single identifier."),
        		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Reservoir) ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
        	    "For example, multiple nearby or related reservoirs may be grouped as a single identifier."),
        	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
			JGUIUtil.addComponent(paragraph, new JLabel (
			    "For example, well-only groups of wells or parcels may be grouped as a single identifier."),
			    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
        	    "Wells associated with ditches are grouped by specifying ditch identifiers for the parts."),
        	    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			    "When grouping wells using parcels, specify parcel identifiers for the" +
			    " parts and indicate the year and water division for the parcel data."),
			    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			    "<html><b>Grouping wells using parcels is an older approach that is being phased out.  " +
			    "Indicate the year and water division for the parcel data.</b></html>"),
			    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
        JGUIUtil.addComponent(paragraph, new JSeparator (SwingConstants.HORIZONTAL),
            0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "<html><b>Collection type:</b>  " + __collectionType + "</html>"),
		    0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		if ( __nodeType.equalsIgnoreCase(__command._Diversion) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Ditch (part ID type assumed to be WDID)</html>"),
		        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Reservoir) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Reservoir (part ID type assumed to be WDID)</html>"),
		        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Ditch (part ID is ditch WDID), Parcel (part ID is parcel ID), "
		        + "or Well (part ID can be well permit or structure WDID)</html>"),
		        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
	}
    JGUIUtil.addComponent(paragraph, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel ( "Columns should be delimited by commas."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file is specified using a path relative to the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel ( "The working directory is: " + __working_dir), 
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ListFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("...", this);
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(__RemoveWorkingDirectoryFromFile,this);
	    JGUIUtil.addComponent(main_JPanel, __path_JButton,
	    	7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}
 
	if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( __collectionType + " part type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__PartType_JComboBox = new SimpleJComboBox(false);
		List<String> part_Vector = new ArrayList<String>(3);
		part_Vector.add ( StateMod_Well_CollectionPartType.DITCH.toString() );
		part_Vector.add ( StateMod_Well_CollectionPartType.PARCEL.toString() );
		part_Vector.add ( StateMod_Well_CollectionPartType.WELL.toString() );
		__PartType_JComboBox.setData(part_Vector);
		__PartType_JComboBox.select(0);
		__PartType_JComboBox.addItemListener (this);
    	JGUIUtil.addComponent(main_JPanel, __PartType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - the type of features being aggregated."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel ("Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Year_JTextField = new JTextField(10);
		__Year_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required if part type is " + __command._Parcel + " - year for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Water division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Div_JTextField = new JTextField(10);
		__Div_JTextField.addKeyListener (this);
    	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required if part type is " + __command._Parcel + " - water division for the parcels."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> column_Vector = new Vector<String>(100);
	column_Vector.add ( "" );	// Not available
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
	}
	__IDCol_JComboBox = new SimpleJComboBox(false);
	__IDCol_JComboBox.setData ( column_Vector );
	__IDCol_JComboBox.select (0);
	__IDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - column for the " + __nodeType + " " + __collectionType + " IDs."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NameCol_JComboBox = new SimpleJComboBox(false);
	__NameCol_JComboBox.setData ( column_Vector );
	__NameCol_JComboBox.select ( 0 );
	__NameCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __NameCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column for the " + __nodeType + " " + __collectionType + " name."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Part IDs column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PartIDsCol_JComboBox = new SimpleJComboBox(false);
	__PartIDsCol_JComboBox.setData ( column_Vector );
	__PartIDsCol_JComboBox.select (0);
	__PartIDsCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __PartIDsCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - first/only column for the part IDs."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
   	if ( (__command instanceof SetWellAggregateFromList_Command) || (__command instanceof SetWellSystemFromList_Command) ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Part ID type column:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__PartIDTypeColumn_JTextField = new JTextField(10);
		__PartIDTypeColumn_JTextField.setToolTipText("Column name or number for the part ID type - column value will be " +
			StateMod_Well_CollectionPartIdType.WDID + " or " + StateMod_Well_CollectionPartIdType.RECEIPT );
		__PartIDTypeColumn_JTextField.addKeyListener (this);
		JGUIUtil.addComponent(main_JPanel, __PartIDTypeColumn_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	   	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required for " + StateMod_Well_CollectionPartType.WELL + " " + __collectionType + " part type."), 
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	}
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Parts listed how:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> parts_Vector = new Vector<String>(2);
	parts_Vector.add ( __command._InColumn );
	parts_Vector.add ( __command._InRow );
	__PartsListedHow_JComboBox = new SimpleJComboBox(false);
	__PartsListedHow_JComboBox.setData ( parts_Vector );
	__PartsListedHow_JComboBox.select ( 0 );
	__PartsListedHow_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __PartsListedHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - are part IDs listed in row or column?"), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel("Part IDs column (max):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PartIDsColMax_JComboBox = new SimpleJComboBox(false);
	__PartIDsColMax_JComboBox.setData ( column_Vector );
	__PartIDsColMax_JComboBox.select ( 0 );
	__PartIDsColMax_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __PartIDsColMax_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - maximum column for part IDs if in row (default is use all)."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new Vector<String>(4);
    IfNotFound_List.add("");
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.select( 0 );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - indicate action if no ID match is found (default=" + __command._Warn + ")."),
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

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();	// Sets the __path_JButton status
    super.setVisible(true);
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e) {
	checkUIState();
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
			response (true);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = "SetCollectionFromList_JDialog.refresh";
	String ListFile = "";
	String Year = "";
	String Div = "";
	String PartType = "";
	String IDCol = "";
	String NameCol = "";
	String PartIDsCol = "";
	String PartIDTypeColumn = "";
	String PartIDsColMax = "";
	String PartsListedHow = "";
	String IfNotFound = "";
	PropList parameters = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		parameters = __command.getCommandParameters();
		ListFile = parameters.getValue ( "ListFile" );
		Year = parameters.getValue ( "Year" );
		Div = parameters.getValue ( "Div" );
		PartType = parameters.getValue ( "PartType" );
		IDCol = parameters.getValue ( "IDCol" );
		NameCol = parameters.getValue ( "NameCol" );
		PartIDsCol = parameters.getValue ( "PartIDsCol" );
		PartIDTypeColumn = parameters.getValue ( "PartIDTypeColumn" );
		PartsListedHow = parameters.getValue ( "PartsListedHow" );
		PartIDsColMax = parameters.getValue ( "PartIDsColMax" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( __PartType_JComboBox != null ) {
			if ( PartType == null ) {
				// Select default...
				__PartType_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__PartType_JComboBox, PartType, JGUIUtil.NONE, null, null ) ) {
					__PartType_JComboBox.select ( PartType );
				}
				else {	Message.printWarning ( 2, routine,
					"Existing command references an unrecognized\n" +
					"PartType value \"" + PartType +
					"\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( IDCol == null ) {
			// Select default...
			__IDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDCol_JComboBox, IDCol, JGUIUtil.NONE, null, null ) ) {
				__IDCol_JComboBox.select ( IDCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid IDCol value \"" + IDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( NameCol == null ) {
			// Select default...
			__NameCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__NameCol_JComboBox, NameCol, JGUIUtil.NONE, null, null ) ) {
				__NameCol_JComboBox.select ( NameCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid NameCol value \"" + NameCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PartIDsCol == null ) {
			// Select default...
			__PartIDsCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__PartIDsCol_JComboBox,	PartIDsCol, JGUIUtil.NONE, null, null ) ) {
				__PartIDsCol_JComboBox.select ( PartIDsCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid PartIDsCol value \"" +
				PartIDsCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PartIDTypeColumn != null ) {
			__PartIDTypeColumn_JTextField.setText (PartIDTypeColumn);
		}
		if ( PartsListedHow == null ) {
			// Select default...
			__PartsListedHow_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__PartsListedHow_JComboBox, PartsListedHow, JGUIUtil.NONE, null, null ) ) {
				__PartsListedHow_JComboBox.select (
				PartsListedHow );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid PartsListedHow value \""+
				PartsListedHow + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PartIDsColMax == null ) {
			// Select default...
			__PartIDsColMax_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__PartIDsColMax_JComboBox, PartIDsColMax, JGUIUtil.NONE, null, null)){
				__PartIDsColMax_JComboBox.select ( PartIDsColMax );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid PartIDsColMax value \"" + PartIDsColMax +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IfNotFound == null ) {
			// Select default...
			__IfNotFound_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IfNotFound_JComboBox, IfNotFound, JGUIUtil.NONE, null, null ) ) {
				__IfNotFound_JComboBox.select ( IfNotFound );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		// Enable and disable components based on the data
		checkUIState();
	}
	// Regardless, reset the command from the fields...
	parameters = new PropList(__command.getCommandName());
	ListFile = __ListFile_JTextField.getText().trim();
	parameters.add("ListFile=" + ListFile);
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
		parameters.add("Year=" + Year);
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
		parameters.add("Div=" + Div);
	}
	if ( __PartType_JComboBox != null ) {
		PartType = __PartType_JComboBox.getSelected();
		parameters.add("PartType=" + PartType);
	}
	IDCol = __IDCol_JComboBox.getSelected();
	NameCol = __NameCol_JComboBox.getSelected();
	PartIDsCol = __PartIDsCol_JComboBox.getSelected();
	PartIDsColMax = __PartIDsColMax_JComboBox.getSelected();
	PartsListedHow = __PartsListedHow_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters.add("IDCol=" + IDCol);
	parameters.add("NameCol=" + NameCol);
	parameters.add("PartIDsCol=" + PartIDsCol);
	parameters.add("PartIDsColMax=" + PartIDsColMax);
	parameters.add("PartsListedHow=" + PartsListedHow);
	parameters.add("IfNotFound=" + IfNotFound);
   	if ( (__command instanceof SetWellAggregateFromList_Command) || (__command instanceof SetWellSystemFromList_Command) ) {
   		PartIDTypeColumn = __PartIDTypeColumn_JTextField.getText().trim();
   		parameters.add("PartIDTypeColumn=" + PartIDTypeColumn);
   	}
	__command_JTextArea.setText( __command.toString(parameters) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (ListFile);
		if (f.isAbsolute()) {
			__path_JButton.setText (__RemoveWorkingDirectoryFromFile);
		}
		else {
			__path_JButton.setText (__AddWorkingDirectoryToFile);
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed and the dialog is closed.
*/
public void response ( boolean ok ) {
	__ok = ok;
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
	response (false);
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
