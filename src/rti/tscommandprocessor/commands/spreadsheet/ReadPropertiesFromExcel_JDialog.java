// ReadPropertiesFromExcel_JDialog - Editor for the ReadPropertiesFromExcel command.

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

package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
import java.util.List;

import RTi.Util.GUI.DictionaryJDialog;
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
Editor for the ReadPropertiesFromExcel command.
*/
@SuppressWarnings("serial")
public class ReadPropertiesFromExcel_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

// Used for button labels...

private final String __AddWorkingDirectory = "Abs";
private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false; // To track errors
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTabbedPane __main_JTabbedPane = null;
private JTextField __InputFile_JTextField = null;
private JTextField __Worksheet_JTextField = null;
private JTextArea __PropertyCellMap_JTextArea = null;
private JTextField __BooleanProperties_JTextField = null;
private JTextField __IntegerProperties_JTextField = null;
private JTextField __DateTimeProperties_JTextField = null;
private SimpleJComboBox __KeepOpen_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadPropertiesFromExcel_Command __command = null;
private boolean __ok = false;
private JFrame __parent = null;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public ReadPropertiesFromExcel_JDialog ( JFrame parent, ReadPropertiesFromExcel_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
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
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
            fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select Excel File");
     	fc.addChoosableFileFilter(new SimpleFileFilter("xls", "Excel File"));
		SimpleFileFilter sff = new SimpleFileFilter("xlsx", "Excel File");
		fc.addChoosableFileFilter(sff);
     	fc.addChoosableFileFilter(new SimpleFileFilter("xlsm", "Excel File with macros enabled"));
		fc.setFileFilter(sff);

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
					__InputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadPropertiesFromExcel");
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
		if (__path_JButton.getText().equals( __AddWorkingDirectory)) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( __RemoveWorkingDirectory)) {
			try {
                __InputFile_JTextField.setText ( IOUtil.toRelativePath (__working_dir,
                        __InputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, 
				__command.getCommandName() + "_JDialog", "Error converting file to relative path.");
			}
		}
		refresh ();
	}
    else if ( event.getActionCommand().equalsIgnoreCase("EditPropertyCellMap") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String PropertyCellMap = __PropertyCellMap_JTextArea.getText().trim();
        String [] notes = {
            "Specify how property names map to Excel cell addresses:",
            "Property Name - property name to set",
            "Cell Address - the input Excel cell address, as named range, A1 notation, or * to match property name"
        };
        String columnFilters = (new DictionaryJDialog ( __parent, true, PropertyCellMap, "Edit PropertyCellMap Parameter",
            notes, "Property Name", "Cell Address",10)).response();
        if ( columnFilters != null ) {
            __PropertyCellMap_JTextArea.setText ( columnFilters );
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
	String InputFile = __InputFile_JTextField.getText().trim();
	String Worksheet = __Worksheet_JTextField.getText().trim();
	String KeepOpen = __KeepOpen_JComboBox.getSelected();
	String PropertyCellMap = __PropertyCellMap_JTextArea.getText().trim().replace("\n"," ");
	String BooleanProperties  = __BooleanProperties_JTextField.getText().trim();
	String DateTimeProperties  = __DateTimeProperties_JTextField.getText().trim();
	String IntegerProperties  = __IntegerProperties_JTextField.getText().trim();
	__error_wait = false;

	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
    if ( Worksheet.length() > 0 ) {
        props.set ( "Worksheet", Worksheet );
    }
    if ( KeepOpen.length() > 0 ) {
        props.set ( "KeepOpen", KeepOpen );
    }
    if ( PropertyCellMap.length() > 0 ) {
        props.set ( "PropertyCellMap", PropertyCellMap );
    }
    if ( BooleanProperties.length() > 0 ) {
        props.set ( "BooleanProperties", BooleanProperties );
    }
    if ( DateTimeProperties.length() > 0 ) {
        props.set ( "DateTimeProperties", DateTimeProperties );
    }
    if ( IntegerProperties.length() > 0 ) {
        props.set ( "IntegerProperties", IntegerProperties );
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
{
    String InputFile = __InputFile_JTextField.getText().trim();
    String Worksheet = __Worksheet_JTextField.getText().trim();
    String KeepOpen  = __KeepOpen_JComboBox.getSelected();
    String PropertyCellMap = __PropertyCellMap_JTextArea.getText().trim().replace("\n"," ");
	String BooleanProperties  = __BooleanProperties_JTextField.getText().trim();
	String DateTimeProperties  = __DateTimeProperties_JTextField.getText().trim();
	String IntegerProperties  = __IntegerProperties_JTextField.getText().trim();
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter ( "Worksheet", Worksheet );
	__command.setCommandParameter ( "KeepOpen", KeepOpen );
	__command.setCommandParameter ( "PropertyCellMap", PropertyCellMap );
	__command.setCommandParameter ( "BooleanProperties", BooleanProperties );
	__command.setCommandParameter ( "DateTimeProperties", DateTimeProperties );
	__command.setCommandParameter ( "IntegerProperties", IntegerProperties );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, ReadPropertiesFromExcel_Command command, List<String> tableIDChoices )
{	__command = command;
    __parent = parent;
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
    	"This command reads cells from a Microsoft Excel worksheet and sets processor properties."),
    	0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Currently only a list of specific cells are read.  In the future a block of properties may be read."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "The cell locations in Excel can be specified using a named range, or A1-style address."),
        0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the Excel file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);		
	if (__working_dir != null) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    // Panel for Excel parameters
    int yExcel = -1;
    JPanel excel_JPanel = new JPanel();
    excel_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel", excel_JPanel );
    
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Input (Excel workbook) file:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputFile_JTextField = new JTextField (45);
    __InputFile_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile_JPanel = new JPanel();
	InputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile_JPanel, __InputFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(excel_JPanel, InputFile_JPanel,
		1, yExcel, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(excel_JPanel, new JLabel ("Worksheet:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Worksheet_JTextField = new JTextField (30);
    __Worksheet_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(excel_JPanel, __Worksheet_JTextField,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel,
        new JLabel ("Optional - worksheet name (default=first sheet)."),
        3, yExcel, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(excel_JPanel, new JLabel( "Keep file open?:"),
        0, ++yExcel, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __KeepOpen_JComboBox = new SimpleJComboBox ( false );
    __KeepOpen_JComboBox.setPrototypeDisplayValue(__command._False + "MMMM"); // to fix some issues with layout of dynamic components
    __KeepOpen_JComboBox.add("");
    __KeepOpen_JComboBox.add(__command._False);
    __KeepOpen_JComboBox.add(__command._True);
    __KeepOpen_JComboBox.select ( 0 );
    __KeepOpen_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(excel_JPanel, __KeepOpen_JComboBox,
        1, yExcel, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excel_JPanel, new JLabel ( "Optional - keep Excel file open? (default=" + __command._False + ")."),
        3, yExcel, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     
    // Panel for Excel <-> Table parameters
    int yExcelTable = -1;
    JPanel excelTable_JPanel = new JPanel();
    excelTable_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Excel <-> Properties", excelTable_JPanel );
    
    JGUIUtil.addComponent(excelTable_JPanel, new JLabel (
        "The column cell map is Property:Excel in order to match the similar parameter in the WritePropertiesToExcel() command (future enhancement)."), 
        0, ++yExcelTable, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(excelTable_JPanel, new JLabel ("Property to cell address map:"),
        0, ++yExcelTable, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PropertyCellMap_JTextArea = new JTextArea (7,45);
    __PropertyCellMap_JTextArea.setLineWrap ( true );
    __PropertyCellMap_JTextArea.setWrapStyleWord ( true );
    __PropertyCellMap_JTextArea.setToolTipText("PropertyName1:ExcelAddress1,PropertyName2:ExcelAddress2");
    __PropertyCellMap_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(excelTable_JPanel, new JScrollPane(__PropertyCellMap_JTextArea),
        1, yExcelTable, 2, 2, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(excelTable_JPanel, new JLabel ("Required - indicate property to cell address mapping (default=none)."),
        3, yExcelTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(excelTable_JPanel, new SimpleJButton ("Edit","EditPropertyCellMap",this),
        3, ++yExcelTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for properties parameters
    int yProp = -1;
    JPanel prop_JPanel = new JPanel();
    prop_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Properties", prop_JPanel );
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel (
        "Property types in TSTool by default correspond to Excel cell type (Excel Number=Double property, Excel Text=String property, Excel Date=DateTime property)."), 
        0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(prop_JPanel, new JLabel (
   	    "Use the following parameters to explicitly set the property type, in particular to convert number to boolean or integer and strings to date/time."),
   	    0, ++yProp, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(prop_JPanel, new JLabel ("Boolean properties:"),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __BooleanProperties_JTextField = new JTextField (20);
    __BooleanProperties_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(prop_JPanel, __BooleanProperties_JTextField,
        1, yProp, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel,
        new JLabel ("Optional - properties that are booleans, separated by commas."),
        3, yProp, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(prop_JPanel, new JLabel ("Date/time properties:"),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimeProperties_JTextField = new JTextField (20);
    __DateTimeProperties_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(prop_JPanel, __DateTimeProperties_JTextField,
        1, yProp, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel,
        new JLabel ("Optional - properties that are date/times, separated by commas."),
        3, yProp, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(prop_JPanel, new JLabel ("Integer properties:"),
        0, ++yProp, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IntegerProperties_JTextField = new JTextField (20);
    __IntegerProperties_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(prop_JPanel, __IntegerProperties_JTextField,
        1, yProp, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(prop_JPanel,
        new JLabel ("Optional - properties that are integers, separated by commas."),
        3, yProp, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (6,80);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 8, 1, 1, .5, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

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
    String InputFile = "";
    String Worksheet = "";
    String KeepOpen = "";
    String PropertyCellMap = "";
	String BooleanProperties = "";
	String DateTimeProperties = "";
	String IntegerProperties = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		InputFile = props.getValue ( "InputFile" );
		Worksheet = props.getValue ( "Worksheet" );
	    KeepOpen = props.getValue ( "KeepOpen" );
	    PropertyCellMap = props.getValue ( "PropertyCellMap" );
		BooleanProperties = props.getValue ( "BooleanProperties" );
		DateTimeProperties = props.getValue ( "DateTimeProperties" );
		IntegerProperties = props.getValue ( "IntegerProperties" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
        if ( Worksheet != null ) {
            __Worksheet_JTextField.setText ( Worksheet );
        }
        if ( KeepOpen == null || KeepOpen.equals("") ) {
            // Select a default...
            __KeepOpen_JComboBox.select ( 0 );
        } 
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __KeepOpen_JComboBox, KeepOpen, JGUIUtil.NONE, null, null ) ) {
                __KeepOpen_JComboBox.select ( KeepOpen );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command references an invalid\nKeepOpen \"" +
                    KeepOpen + "\".  Select a different choice or Cancel." );
            }
        }
        if ( PropertyCellMap != null ) {
            __PropertyCellMap_JTextArea.setText ( PropertyCellMap );
        }
        if ( BooleanProperties != null ) {
            __BooleanProperties_JTextField.setText ( BooleanProperties );
        }
        if ( DateTimeProperties != null ) {
            __DateTimeProperties_JTextField.setText ( DateTimeProperties );
        }
        if ( IntegerProperties != null ) {
            __IntegerProperties_JTextField.setText ( IntegerProperties );
        }
	}
	// Regardless, reset the command from the fields...
	InputFile = __InputFile_JTextField.getText().trim();
	Worksheet = __Worksheet_JTextField.getText().trim();
	KeepOpen = __KeepOpen_JComboBox.getSelected();
	PropertyCellMap = __PropertyCellMap_JTextArea.getText().trim().replace("\n"," ");
	BooleanProperties = __BooleanProperties_JTextField.getText().trim();
	DateTimeProperties = __DateTimeProperties_JTextField.getText().trim();
	IntegerProperties = __IntegerProperties_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "InputFile=" + InputFile );
	props.add ( "Worksheet=" + Worksheet );
	props.add ( "KeepOpen=" + KeepOpen );
	props.add ( "PropertyCellMap=" + PropertyCellMap );
	props.add ( "BooleanProperties=" + BooleanProperties );
	props.add ( "DateTimeProperties=" + DateTimeProperties );
	props.add ( "IntegerProperties=" + IntegerProperties );
	__command_JTextArea.setText( __command.toString ( props ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		if ( (InputFile != null) && !InputFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( InputFile );
			if ( f.isAbsolute() ) {
				__path_JButton.setText ( __RemoveWorkingDirectory );
				__path_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
		    	__path_JButton.setText ( __AddWorkingDirectory );
		    	__path_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path_JButton.setEnabled(false);
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
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}
