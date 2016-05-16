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
Command editor dialog for simple Read*FromStateCU() commands, which share the same parameters.
*/
public class ReadFromStateCU_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private JTextField __InputFile_JTextField = null;
private SimpleJComboBox	__Version_JComboBox = null;
private SimpleJComboBox	__ReadDataFrom_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private ReadFromStateCU_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadFromStateCU_JDialog (JFrame parent, Command command )
{	super(parent, true);
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

		SimpleFileFilter sff = null;
		if ( __command instanceof ReadClimateStationsFromStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU Climate Stations File to Read");
			sff = new SimpleFileFilter("cli", "StateCU Climate Stations File");
		}
		else if ( __command instanceof ReadCropCharacteristicsFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Crop Characteristics File");
			sff = new SimpleFileFilter("cch", "StateCU Crop Characteristics File");
		}
		else if ( __command instanceof ReadBlaneyCriddleFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Blaney-Criddle Crop Coefficients File");
			sff = new SimpleFileFilter("kbc", "StateCU Blaney-Criddle Crop Coefficients File");
		}
		else if ( __command instanceof ReadPenmanMonteithFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Penman-Monteith Crop Coefficients File");
			sff = new SimpleFileFilter("kpm", "StateCU Penman-Monteith Crop Coefficients File");
		}
		else if ( __command instanceof ReadCULocationsFromStateCU_Command ) {
			fc.setDialogTitle("Specify StateCU CU Locations File to Read");
			sff = new SimpleFileFilter("str", "StateCU CU Locations File");
		}
		else if ( __command instanceof ReadCropPatternTSFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Crop Pattern Time Series File");
			sff = new SimpleFileFilter("cds", "StateCU Crop Pattern Time Series File");
		}
		else if ( __command instanceof ReadIrrigationPracticeTSFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Irrigation Practice Time Series File");
			sff = new SimpleFileFilter("ipy", "StateCU Irrigation Practice Time Series File");
		}
		else if ( __command instanceof ReadIrrigationWaterRequirementTSMonthlyFromStateCU_Command ) {
			fc.setDialogTitle("Select StateCU Irrigation (Consumptive) Water Requirement Time Series File");
			sff = new SimpleFileFilter("ddc", "StateCU Irrigation Water Requirement Time Series File");
		}
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__InputFile_JTextField.setText(path);
			refresh();
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
	else if ( o == __path_JButton) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals("Remove Working Directory")) {
			try {
				__InputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __InputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, __command + "_JDialog",
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String InputFile = __InputFile_JTextField.getText().trim();
	
	__error_wait = false;
	
	if (InputFile.length() > 0) {
		props.set("InputFile", InputFile);
	}
	if ( __Version_JComboBox != null ) {
		String Version = __Version_JComboBox.getSelected();
		if (Version.length() > 0 ) {
			props.set("Version", Version);
		}
	}
	if ( __ReadDataFrom_JComboBox != null ) {
		String ReadDataFrom = __ReadDataFrom_JComboBox.getSelected();
		if (ReadDataFrom.length() > 0 ) {
			props.set("ReadDataFrom", ReadDataFrom);
		}
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
private void commitEdits()
{
	String InputFile = __InputFile_JTextField.getText().trim();

	__command.setCommandParameter("InputFile", InputFile);
	if ( __Version_JComboBox != null ) {
		String Version = __Version_JComboBox.getSelected();
		__command.setCommandParameter("Version", Version);
	}
	if ( __ReadDataFrom_JComboBox != null ) {
		String ReadDataFrom = __ReadDataFrom_JComboBox.getSelected();
		__command.setCommandParameter("ReadDataFrom", ReadDataFrom);
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__InputFile_JTextField = null;
	__Version_JComboBox = null;
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
private void initialize (JFrame parent, Command command )
{	__command = (ReadFromStateCU_Command)command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	
	if ( __command instanceof ReadClimateStationsFromStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads the StateCU climate station data from a StateCU climate stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof ReadCropCharacteristicsFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
       		"This command reads crop characteristics from a StateCU crop characteristics file."),
       		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel (
    		"Crop characteristics define general parameters for a crop (e.g., growing season, root depth)."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadBlaneyCriddleFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command reads Blaney-Criddle crop coefficients from " +
    		"a StateCU Blaney-Criddle crop coefficients file."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"Blaney-Criddle crop coefficents estimate crop water" +
    		" requirements for each crop during the year, for standard conditions."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadPenmanMonteithFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command reads Penman-Monteith crop coefficients from " +
    		"a StateCU Penman-Monteith crop coefficients file."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"Penman-Monteith crop coefficents estimate crop water" +
    		" requirements for each crop during the year, for standard conditions."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"The ASCE Standardized Penman-Monteith equation is also supported."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadCULocationsFromStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads the StateCU CU locations data from a StateCU \"structure\" file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	else if ( __command instanceof ReadCropPatternTSFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command reads crop pattern time series" +
    		" from a StateCU crop pattern time series file."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
    		"Crop pattern time series for a CU Location are defined by " +
    		"year, crop type, and irrigated area."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
			"It is recommended that crop areas be read from the acreage values (the default) " +
			"because this will minimize roundoff errors."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
			"Reading the data from total and fraction should only be used for older files that " +
			"do not have individual crop acreage values."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadIrrigationPracticeTSFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command reads CU irrigation practice time series" +
        	" from a StateCU irrigation practice time series file."),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"StateCU irrigation practice time series are defined for each CU Location."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	/*
	else if ( __command instanceof ReadDelayTablesFromStateCU_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads StateCU delay tables from a StateCU file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	}
	*/
	else if ( __command instanceof ReadIrrigationWaterRequirementTSMonthlyFromStateCU_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command reads IWR or CU requirement time series " +
    		"(monthly) from a StateCU/StateMod time series file."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Consumptive water requirement time series (monthly) are " +
		"associated with diversion and well stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"For agricultural stations, the irrigation water requirement " +
		"(IWR) time series from the consumptive use model are equivalent."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the file be specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
        JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: "), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("    " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

    if ( __command instanceof ReadCropPatternTSFromStateCU_Command ||
    	__command instanceof ReadIrrigationPracticeTSFromStateCU_Command) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Version:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    List version_Vector = new Vector();
		version_Vector.add ( "" );
		version_Vector.add ( __command._10 );
		__Version_JComboBox = new SimpleJComboBox(false);
		__Version_JComboBox.setData ( version_Vector );
		__Version_JComboBox.addItemListener (this);
		JGUIUtil.addComponent(main_JPanel, __Version_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - StateCU program version (default=most current)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
	 
    if ( __command instanceof ReadCropPatternTSFromStateCU_Command ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Read data from:"),
        	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    List ReadDataFrom_Vector = new Vector();
	    ReadDataFrom_Vector.add ( "" );
	    ReadDataFrom_Vector.add ( __command._CropArea );
	    ReadDataFrom_Vector.add ( __command._TotalAreaAndCropFraction );
	    __ReadDataFrom_JComboBox = new SimpleJComboBox(false);
	    __ReadDataFrom_JComboBox.setData ( ReadDataFrom_Vector );
	    __ReadDataFrom_JComboBox.addItemListener (this);
	    JGUIUtil.addComponent(main_JPanel, __ReadDataFrom_JComboBox,
   		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
   		"Optional - how to read crop data (default=CropArea, total will be sum of crops)."),
   		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
        
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
		0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if (__working_dir != null) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton( "Remove Working Directory", this);
		button_JPanel.add (__path_JButton);
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable ( false );
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
			response (false);
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
{	String routine = __command.getCommandName() + "_JDialog.refresh";
	String InputFile = "";
	String Version = "";
	String ReadDataFrom = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
		Version = props.getValue ( "Version" );
		ReadDataFrom = props.getValue ( "ReadDataFrom" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText (InputFile);
		}
		if ( __Version_JComboBox != null ) {
			if ( Version == null ) {
				// Select default...
				__Version_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__Version_JComboBox, Version, JGUIUtil.NONE, null, null ) ) {
					__Version_JComboBox.select ( Version );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid Version value \"" + Version +
					"\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __ReadDataFrom_JComboBox != null ) {
			if ( ReadDataFrom == null ) {
				// Select default...
				__ReadDataFrom_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__ReadDataFrom_JComboBox, ReadDataFrom, JGUIUtil.NONE, null, null ) ) {
					__ReadDataFrom_JComboBox.select ( ReadDataFrom );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nReadDataFrom value \"" +
					ReadDataFrom + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	InputFile = __InputFile_JTextField.getText().trim();
	props.add("InputFile=" + InputFile);
	if ( __Version_JComboBox != null ) {
		Version = __Version_JComboBox.getSelected();
		props.add("Version=" + Version);
	}
	if ( __ReadDataFrom_JComboBox != null ) {
		ReadDataFrom = __ReadDataFrom_JComboBox.getSelected();
		props.add("ReadDataFrom=" + ReadDataFrom);
	}
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (InputFile);
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