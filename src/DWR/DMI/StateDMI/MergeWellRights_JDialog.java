package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the MergeWellRights() command.
*/
public class MergeWellRights_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false; // Indicate whether OK has been pressed
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;
private JTextField __OutputFile_JTextField = null;
private JTextField __PermitIDPreFormat_JTextField = null;
private SimpleJComboBox __IDFormat_JComboBox = null;
private JTextField __PermitIDPostFormat_JTextField = null;
private SimpleJComboBox __MergeParcelYears_JComboBox = null;
private SimpleJComboBox __SumDecrees_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private MergeWellRights_Command __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public MergeWellRights_JDialog ( JFrame parent, MergeWellRights_Command command )
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
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		SimpleFileFilter sff = null;
		fc.setDialogTitle("Specify Intermediate Rights Files");
		sff = new SimpleFileFilter("wer", "StateMod Well Rights File");
	
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);
	
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__OutputFile_JTextField.setText(path);
			refresh();
		}	
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response ( true );
		}
	}
	else if ( o == __path_JButton) {
		if (__path_JButton.getText().equals("Add Working Directory")) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals("Remove Working Directory")) {
			try {
				__OutputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir,__OutputFile_JTextField.getText()));
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
Check the input.  Currently does nothing.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	String MergeParcelYears = __MergeParcelYears_JComboBox.getSelected();
	String SumDecrees = __SumDecrees_JComboBox.getSelected();
	__error_wait = false;
	if (OutputFile.length() > 0) {
		props.set("OutputFile", OutputFile);
	}
	if ( PermitIDPreFormat.length() > 0 ) {
		props.set ( "PermitIDPreFormat", PermitIDPreFormat );
	}
	if ( IDFormat.length() > 0 ) {
		props.set ( "IDFormat", IDFormat );
	}
	if ( PermitIDPostFormat.length() > 0 ) {
		props.set ( "PermitIDPostFormat", PermitIDPostFormat );
	}
	if (MergeParcelYears.length() > 0) {
		props.set("MergeParcelYears", MergeParcelYears);
	}
	if (SumDecrees.length() > 0) {
		props.set("SumDecrees", SumDecrees);
	}
	try {
		// This will warn the user
		__command.checkCommandParameters( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning will have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String OutputFile = __OutputFile_JTextField.getText().trim();
	String PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	String MergeParcelYears = __MergeParcelYears_JComboBox.getSelected();
	String SumDecrees = __SumDecrees_JComboBox.getSelected();
	__command.setCommandParameter("OutputFile", OutputFile);
	__command.setCommandParameter("PermitIDPreFormat", PermitIDPreFormat);
	__command.setCommandParameter("IDFormat", IDFormat);
	__command.setCommandParameter("PermitIDPostFormat", PermitIDPostFormat);
	__command.setCommandParameter("MergeParcelYears", MergeParcelYears);
	__command.setCommandParameter("SumDecrees", SumDecrees);
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, MergeWellRights_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    JGUIUtil.addComponent(paragraph, new JLabel (
	"This command merges well water rights created using multiple years of irrigated parcel data (" +
	"output from the ReadWellRightsFromHydroBase() command)."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"The original water rights for multiple years are replaced by merged rights that apply for the " +
	"full period."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"Rights present in adjacent years with matching location ID, right ID, well type (class), " +
	"and administration number are retained in the result, using the earliest year for the parcel year."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"If rights do not match, the list of rights for a location ID and right ID with the " +
	"highest decree total are retained in the result (assuming increasing water use)."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"Therefore, rights found in only one year are retained in the result."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(paragraph, new JLabel (
		"The first two years of data are compared first and subsequent years of data are then " +
       	"compared to the result."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command does not aggregate rights spatially or by combining a range of administration numbers." +
		"  See the AggregateWellRights() command, which can be run after this command."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
	"Specify the output file the same as the final *wer to create a sequence of files showing intermediate " +
	"results including the rights for the first year, after merging the second year, etc."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (" "),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"A final step sums the decrees for rights with the same identifier, adminstration number, and on/off switch."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"Water rights that are parcel supply class 4 and 9 are not summed because they represent unique estimated wells)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (" "),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"Each step is controlled by optional parameters to allow evaluation of the processing steps."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Output file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (35);
	__OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Merge parcel years?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List MergeParcelYears_Vector = new Vector(2);
    MergeParcelYears_Vector.add ( "" );
    MergeParcelYears_Vector.add ( __command._False );
    MergeParcelYears_Vector.add ( __command._True );
	__MergeParcelYears_JComboBox = new SimpleJComboBox(false);
	__MergeParcelYears_JComboBox.setData ( MergeParcelYears_Vector );
	__MergeParcelYears_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel,__MergeParcelYears_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - merge well rights for multiple parcel years (default=" + __command._True + ")."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Sum right decrees?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List SumDecrees_Vector = new Vector(2);
    SumDecrees_Vector.add ( "" );
    SumDecrees_Vector.add ( __command._False );
    SumDecrees_Vector.add ( __command._True );
	__SumDecrees_JComboBox = new SimpleJComboBox(false);
	__SumDecrees_JComboBox.setData ( SumDecrees_Vector );
	__SumDecrees_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __SumDecrees_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional - at end, sum rights with same ID and admin number (default=" + __command._True + ")."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Permit ID (pre)format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PermitIDPreFormat_JTextField = new JTextField("",10);
	__PermitIDPreFormat_JTextField.setToolTipText("Use %s to pass the permit ID through, or for example %s:P for legacy permit ID");
	__PermitIDPreFormat_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PermitIDPreFormat_JTextField,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for permit BEFORE right ID format (default= %s:P =legacy format)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Right ID format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List format_Vector = new Vector(3);
	format_Vector.add ( "" );
	format_Vector.add ( "" + StateModWellRightIdFormatType.RIGHTID_NN );
	__IDFormat_JComboBox = new SimpleJComboBox(false);
	__IDFormat_JComboBox.setData ( format_Vector );
	__IDFormat_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __IDFormat_JComboBox,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for right identifiers (default=no additional formatting)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Permit ID (post)format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
 	__PermitIDPostFormat_JTextField = new JTextField("",10);
	__PermitIDPostFormat_JTextField.setToolTipText("Use %s to pass the permit ID through, or for example %s:P for legacy permit ID");
	__PermitIDPostFormat_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PermitIDPostFormat_JTextField,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for permit ID AFTER right ID format (default= %s =pass-through)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
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
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
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
	else {
		// One of the combo boxes...
		refresh();
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
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getSimpleName() + ".refresh";
	String OutputFile = "";
	String PermitIDPreFormat = "";
	String IDFormat = "";
	String PermitIDPostFormat = "";
	String MergeParcelYears = "";
	String SumDecrees = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		OutputFile = props.getValue ( "OutputFile" );
		PermitIDPreFormat = props.getValue ( "PermitIDPreFormat" );
		IDFormat = props.getValue ( "IDFormat" );
		PermitIDPostFormat = props.getValue ( "PermitIDPostFormat" );
		MergeParcelYears = props.getValue ( "MergeParcelYears" );
		SumDecrees = props.getValue ( "SumDecrees" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText(OutputFile);
		}
		if ( PermitIDPreFormat != null ) {
			__PermitIDPreFormat_JTextField.setText(PermitIDPreFormat);
		}
		if ( IDFormat == null ) {
			// Select default...
			__IDFormat_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDFormat_JComboBox, IDFormat, JGUIUtil.NONE, null, null ) ) {
				__IDFormat_JComboBox.select ( IDFormat );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid IDFormat value \"" +
				IDFormat + "\".  Select a different value or Cancel.");
			}
		}
		if ( PermitIDPostFormat != null ) {
			__PermitIDPostFormat_JTextField.setText(PermitIDPostFormat);
		}
		if ( __MergeParcelYears_JComboBox != null ) {
			if ( MergeParcelYears == null ) {
				// Select default...
				__MergeParcelYears_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__MergeParcelYears_JComboBox, MergeParcelYears, JGUIUtil.NONE, null, null)){
					__MergeParcelYears_JComboBox.select ( MergeParcelYears );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid MergeParcelYears " +
					"value \"" + MergeParcelYears + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __SumDecrees_JComboBox != null ) {
			if ( SumDecrees == null ) {
				// Select default...
				__SumDecrees_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__SumDecrees_JComboBox, SumDecrees, JGUIUtil.NONE, null, null)){
					__SumDecrees_JComboBox.select ( SumDecrees );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid SumDecrees " +
					"value \"" + SumDecrees + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}

	// Always get the value that is selected...

	props = new PropList(__command.getCommandName());
	OutputFile = __OutputFile_JTextField.getText().trim();
	PermitIDPreFormat = __PermitIDPreFormat_JTextField.getText().trim();
	IDFormat = __IDFormat_JComboBox.getSelected();
	PermitIDPostFormat = __PermitIDPostFormat_JTextField.getText().trim();
	MergeParcelYears =__MergeParcelYears_JComboBox.getSelected();
	SumDecrees =__SumDecrees_JComboBox.getSelected();
	props.add("OutputFile=" + OutputFile);
	props.add ( "PermitIDPreFormat=" + PermitIDPreFormat);
	props.add("IDFormat=" + IDFormat);
	props.add ( "PermitIDPostFormat=" + PermitIDPostFormat);
	props.add("MergeParcelYears=" + MergeParcelYears);
	props.add("SumDecrees=" + SumDecrees);
	__command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	
	__ok = ok;	// Save to be returned by ok()
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

public void stateChanged(ChangeEvent e) {
	refresh();
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