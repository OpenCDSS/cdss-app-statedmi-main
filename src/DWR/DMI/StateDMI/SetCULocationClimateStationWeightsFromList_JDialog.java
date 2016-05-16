package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the SetCULocationClimateStationWeightsFromList() command.
*/
public class SetCULocationClimateStationWeightsFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __ListFile_JTextField = null;// List file
private SimpleJComboBox __IDCol_JComboBox = null;
private SimpleJComboBox __StationIDCol_JComboBox = null;
private SimpleJComboBox __Region1Col_JComboBox = null;
private SimpleJComboBox __Region2Col_JComboBox = null;
private SimpleJComboBox __TempWtCol_JComboBox = null;
private SimpleJComboBox __PrecWtCol_JComboBox = null;
private SimpleJComboBox __OrographicTempAdjCol_JComboBox = null;
private SimpleJComboBox __OrographicPrecAdjCol_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private SetCULocationClimateStationWeightsFromList_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCULocationClimateStationWeightsFromList_JDialog (JFrame parent, Command command)
{	super(parent, true);
	initialize (parent, command);
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
		fc.addChoosableFileFilter( new SimpleFileFilter("csv", "CU Location Climate Station Weights List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", "CU Location Climate Station Weights List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", "CU Location Climate Station Weights List File") );
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
		response (false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton ) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__ListFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __ListFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals("Remove Working Directory")) {
			try {
				__ListFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __ListFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, "SetCULocationClimateStationWeightsFromList_JDialog",
				"Error converting file to relative path.");
			}
		}
		refresh ();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList parameters = new PropList ( "" );
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String StationIDCol = __StationIDCol_JComboBox.getSelected();
	String Region1Col = __Region1Col_JComboBox.getSelected();
	String Region2Col = __Region2Col_JComboBox.getSelected();
	String TempWtCol = __TempWtCol_JComboBox.getSelected();
	String PrecWtCol = __PrecWtCol_JComboBox.getSelected();
	String OrographicTempAdjCol = __OrographicTempAdjCol_JComboBox.getSelected();
	String OrographicPrecAdjCol = __OrographicPrecAdjCol_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ListFile.length() > 0 ) {
		parameters.set ( "ListFile", ListFile );
	}
	if ( IDCol.length() > 0 ) {
		parameters.set ( "IDCol", IDCol );
	}
	if ( StationIDCol.length() > 0 ) {
		parameters.set ( "StationIDCol", StationIDCol );
	}
	if ( Region1Col.length() > 0 ) {
		parameters.set ( "Region1Col", Region1Col );
	}
    if ( Region2Col.length() > 0 ) {
        parameters.set ( "Region2Col", Region2Col );
    }
	if ( TempWtCol.length() > 0 ) {
		parameters.set ( "TempWtCol", TempWtCol );
	}
    if ( PrecWtCol.length() > 0 ) {
        parameters.set ( "PrecWtCol", PrecWtCol );
    }
    if ( OrographicTempAdjCol.length() > 0 ) {
        parameters.set ( "OrographicTempAdjCol", OrographicTempAdjCol );
    }
    if ( OrographicPrecAdjCol.length() > 0 ) {
        parameters.set ( "OrographicPrecAdjCol", OrographicPrecAdjCol );
    }
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
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
{	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String StationIDCol = __StationIDCol_JComboBox.getSelected();
	String Region1Col = __Region1Col_JComboBox.getSelected();
	String Region2Col = __Region2Col_JComboBox.getSelected();
	String TempWtCol = __TempWtCol_JComboBox.getSelected();
	String PrecWtCol = __PrecWtCol_JComboBox.getSelected();
	String OrographicTempAdjCol = __OrographicTempAdjCol_JComboBox.getSelected();
	String OrographicPrecAdjCol = __OrographicPrecAdjCol_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ListFile", ListFile );
	__command.setCommandParameter ( "IDCol", IDCol );
	__command.setCommandParameter ( "StationIDCol", StationIDCol );
	__command.setCommandParameter ( "Region1Col", Region1Col );
    __command.setCommandParameter ( "Region2Col", Region2Col );
	__command.setCommandParameter ( "TempWtCol", TempWtCol );
    __command.setCommandParameter ( "PrecWtCol", PrecWtCol );
    __command.setCommandParameter ( "OrographicTempAdjCol", OrographicTempAdjCol );
    __command.setCommandParameter ( "OrographicPrecAdjCol", OrographicPrecAdjCol );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__ListFile_JTextField = null;
	__browse_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__path_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, Command command)
{
	__command = (SetCULocationClimateStationWeightsFromList_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
    JGUIUtil.addComponent(paragraph, new JLabel (
        "This command sets CU location climate station weights from data in a delimited list file."),
        0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Each CU Location must be associated with one or more " +
		"precipitation and temperature stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The data from each station is weighted, and the weights should add to 1.0."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"There are two ways to match CU locations for processing:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "   1.  Specify the CU Location ID column - in this case weights will be set only for CU locations that " +
        "match the ID in the list file."), +
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "   2.  Specify the Region1 and Region2 columns - in this case weights will be set for CU locations " +
        "with matching Region1 and Region2 (e.g, County, HUC)."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns in the file should be delimited by commas."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The climate station weights are usually assigned after other"+
		" CU Location data have been filled, in particular if county is filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ListFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID column:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List column2_Vector = new Vector(100);
   	column2_Vector.add ( "" );
   	for ( int i = 1; i <= 100; i++ ) {
   		column2_Vector.add ( "" + i );
   	}
   	__IDCol_JComboBox = new SimpleJComboBox(false);
   	__IDCol_JComboBox.setData ( column2_Vector );
   	__IDCol_JComboBox.addItemListener (this);
   	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
   		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"If specified, Region1 and Region2 columns should not be specified."),
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);        

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Climate station ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List column_Vector = new Vector(100);
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
	}
	__StationIDCol_JComboBox = new SimpleJComboBox(false);
	__StationIDCol_JComboBox.setData ( column2_Vector );
	__StationIDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __StationIDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Required - indicate the climate station column."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST); 

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region 1 column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region1Col_JComboBox = new SimpleJComboBox(false);
	__Region1Col_JComboBox.setData ( column2_Vector );
	__Region1Col_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __Region1Col_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"If specified, the CU Location ID column should not be specified."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);        

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region 2 column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region2Col_JComboBox = new SimpleJComboBox(false);
	__Region2Col_JComboBox.setData ( column2_Vector );
	__Region2Col_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __Region2Col_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"If specified, the CU Location ID column should not be specified."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);        

    JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Temperature station weight column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__TempWtCol_JComboBox = new SimpleJComboBox(false);
	__TempWtCol_JComboBox.setData ( column2_Vector );
	__TempWtCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __TempWtCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - temperature station weight column."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST); 

   JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Precipitation station weight column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PrecWtCol_JComboBox = new SimpleJComboBox(false);
	__PrecWtCol_JComboBox.setData ( column2_Vector );
	__PrecWtCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __PrecWtCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - precipitation station weight column."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST); 

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Orographic temperature adjustment column:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OrographicTempAdjCol_JComboBox = new SimpleJComboBox(false);
    __OrographicTempAdjCol_JComboBox.setData ( column2_Vector );
    __OrographicTempAdjCol_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __OrographicTempAdjCol_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - orographic temperature adjustment column."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST); 
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Orographic precipitation adjustment column:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OrographicPrecAdjCol_JComboBox = new SimpleJComboBox(false);
    __OrographicPrecAdjCol_JComboBox.setData ( column2_Vector );
    __OrographicPrecAdjCol_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __OrographicPrecAdjCol_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - orographic precipitation adjustment column."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List IfNotFound_List = new Vector();
    IfNotFound_List.add("");
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
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

	if (__working_dir != null) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton("Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

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
{	String routine = "SetCULocationClimateStationWeightsFromList_JDialog.refresh";
	String ListFile = "";
	String IDCol = "";
	String StationIDCol = "";
	String Region1Col = "";
	String Region2Col = "";
	String TempWtCol = "";
	String PrecWtCol = "";
	String OrographicTempAdjCol = "";
	String OrographicPrecAdjCol = "";
	String IfNotFound = "";
	__error_wait = false;
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		ListFile = parameters.getValue ( "ListFile" );
		IDCol = parameters.getValue ( "IDCol" );
		StationIDCol = parameters.getValue ( "StationIDCol" );
		Region1Col = parameters.getValue ( "Region1Col" );
		Region2Col = parameters.getValue ( "Region2Col" );
		TempWtCol = parameters.getValue ( "TempWtCol" );
		PrecWtCol = parameters.getValue ( "PrecWtCol" );
		OrographicTempAdjCol = parameters.getValue ( "OrographicTempAdjCol" );
		OrographicPrecAdjCol = parameters.getValue ( "OrographicPrecAdjCol" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( IDCol == null ) {
			// Select default...
			__IDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDCol_JComboBox, IDCol, JGUIUtil.NONE, null, null ) ) {
				__IDCol_JComboBox.select(IDCol);
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid IDCol value \"" + IDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( StationIDCol == null ) {
			// Select default...
			__StationIDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__StationIDCol_JComboBox, StationIDCol, JGUIUtil.NONE, null, null ) ) {
				__StationIDCol_JComboBox.select(StationIDCol);
			}
			else {
				Message.printWarning ( 1, routine, "Existing command "+
				"references an invalid StationIDCol value \"" + StationIDCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region1Col == null ) {
			// Select default...
			__Region1Col_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region1Col_JComboBox, Region1Col, JGUIUtil.NONE, null, null ) ) {
				__Region1Col_JComboBox.select(Region1Col);
			}
			else {
				Message.printWarning ( 1, routine, "Existing command "+
				"references an invalid Region1Col value \"" +
				Region1Col + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region2Col == null ) {
			// Select default...
			__Region2Col_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region2Col_JComboBox, Region2Col, JGUIUtil.NONE, null, null ) ) {
				__Region2Col_JComboBox.select(Region2Col);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command "+
				"references an invalid Region2Col value \"" + Region2Col +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( TempWtCol == null ) {
			// Select default...
			__TempWtCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__TempWtCol_JComboBox, TempWtCol, JGUIUtil.NONE, null, null ) ) {
				__TempWtCol_JComboBox.select(TempWtCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command "+
				"references an invalid TempWtCol value \"" + TempWtCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PrecWtCol == null ) {
			// Select default...
			__PrecWtCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__PrecWtCol_JComboBox, PrecWtCol, JGUIUtil.NONE, null, null ) ) {
				__PrecWtCol_JComboBox.select(PrecWtCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command "+
				"references an invalid PrecWtCol value \"" + PrecWtCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( OrographicTempAdjCol == null ) {
			// Select default...
			__OrographicTempAdjCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OrographicTempAdjCol_JComboBox, OrographicTempAdjCol, JGUIUtil.NONE, null, null ) ) {
				__OrographicTempAdjCol_JComboBox.select(OrographicTempAdjCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command "+
				"references an invalid OrographicTempAdjCol value \"" +
				OrographicTempAdjCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( OrographicPrecAdjCol == null ) {
			// Select default...
			__OrographicPrecAdjCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OrographicPrecAdjCol_JComboBox, OrographicPrecAdjCol, JGUIUtil.NONE, null, null ) ) {
				__OrographicPrecAdjCol_JComboBox.select(OrographicPrecAdjCol);
			}
			else {
				Message.printWarning ( 2, routine, "Existing command "+
				"references an OrographicPrecAdjCol value \"" + OrographicPrecAdjCol +
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
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	IDCol = __IDCol_JComboBox.getSelected();
	StationIDCol = __StationIDCol_JComboBox.getSelected();
	Region1Col = __Region1Col_JComboBox.getSelected();
	Region2Col = __Region2Col_JComboBox.getSelected();
	TempWtCol = __TempWtCol_JComboBox.getSelected();
	PrecWtCol = __PrecWtCol_JComboBox.getSelected();
	OrographicTempAdjCol = __OrographicTempAdjCol_JComboBox.getSelected();
	OrographicPrecAdjCol = __OrographicPrecAdjCol_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "ListFile=" + ListFile );
	parameters.add ( "IDCol=" + IDCol );
	parameters.add ( "StationIDCol=" + StationIDCol );
    parameters.add ( "Region1Col=" + Region1Col );
    parameters.add ( "Region2Col=" + Region2Col );
    parameters.add ( "TempWtCol=" + TempWtCol );
    parameters.add ( "PrecWtCol=" + PrecWtCol );
    parameters.add ( "OrographicTempAdjCol=" + OrographicTempAdjCol );
    parameters.add ( "OrographicPrecAdjCol=" + OrographicPrecAdjCol );
    parameters.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString ( parameters ) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (ListFile);
		if (f.isAbsolute()) {
			__path_JButton.setText ("Remove Working Directory");
		}
		else {
			__path_JButton.setText ("Add Working Directory");
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