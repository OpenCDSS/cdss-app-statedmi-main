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
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Write*ToStateMod() commands.  See WriteTSToStateMod() for time series output.
*/
public class WriteToStateMod_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private JTextField __OutputFile_JTextField = null;
private JTextField __Precision_JTextField = null;
private SimpleJComboBox __WriteHow_JComboBox = null;
private SimpleJComboBox __WriteDataComments_JComboBox = null;
private SimpleJComboBox __WriteExtendedDataComments_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private WriteToStateMod_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public WriteToStateMod_JDialog (JFrame parent, WriteToStateMod_Command command )
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
		if ( __command instanceof WriteStreamGageStationsToStateMod_Command ){
			fc.setDialogTitle("Specify StateMod Stream Gage Stations File to Write");
			sff = new SimpleFileFilter("ris", "StateMod Stream Gage Stations File");
		}
		else if ( __command instanceof WriteDelayTablesMonthlyToStateMod_Command ) {
			fc.setDialogTitle("Specify the StateMod Delay Tables (Daily) File to Write");
			sff = new SimpleFileFilter("dld", "StateCU Delay Tables (Daily) File");
		}
		else if ( __command instanceof WriteDelayTablesMonthlyToStateMod_Command ) {
			fc.setDialogTitle("Specify the StateMod Delay Tables File (Monthly) to Write");
			sff = new SimpleFileFilter("dly", "StateCU Delay Tables (Monthly) File");
		}
		else if ( __command instanceof WriteDiversionStationsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Diversion Stations File to Write");
			sff = new SimpleFileFilter("dds", "StateMod Diversion Stations File");
		}
		else if ( __command instanceof WriteDiversionRightsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Diversion Rights File to Write");
			sff = new SimpleFileFilter("ddr", "StateMod Diversion Rights File");
		}
		else if ( __command instanceof WriteReservoirStationsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Reservoir Stations File to Write");
			sff = new SimpleFileFilter("res", "StateMod Reservoir Stations File");
		}
		else if ( __command instanceof WriteReservoirRightsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Reservoir Rights File to Write");
			sff = new SimpleFileFilter("rer", "StateMod Reservoir Rights File");
		}
		else if ( __command instanceof WriteReservoirReturnToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Reservoir Return Flow File to Write");
			sff = new SimpleFileFilter("rrf", "StateMod Reservoir Return Flow File");
		}
		else if ( __command instanceof WriteInstreamFlowStationsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Instream Flow Stations File to Write");
			sff = new SimpleFileFilter("ifs", "StateMod Instream Flow Stations File");
		}
		else if ( __command instanceof WriteInstreamFlowRightsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Instream Flow Rights File to Write");
			sff = new SimpleFileFilter("ifr", "StateMod Instream Flow Rights File");
		}
		else if ( __command instanceof WriteWellStationsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Well Stations File to Write");
			sff = new SimpleFileFilter("wes", "StateMod Well Stations File");
		}
		else if ( __command instanceof WriteWellRightsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Well Rights File to Write");
			sff = new SimpleFileFilter("wer", "StateMod Well Rights File");
		}
		else if ( __command instanceof WritePlanStationsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Plan Stations File to Write");
			sff = new SimpleFileFilter("pln", "StateMod Plan Stations File");
		}
		else if ( __command instanceof WritePlanWellAugmentationToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Well Augmentation Plan Data File to Write");
			sff = new SimpleFileFilter("plw", "StateMod Well Augmentation Plan Data File");
		}
		else if ( __command instanceof WritePlanReturnToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Plan Return Flow File to Write");
			sff = new SimpleFileFilter("prf", "StateMod Plan Return Flow File");
		}
		else if ( __command instanceof WriteStreamEstimateStationsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Stream Estimate Stations File to Write");
			sff = new SimpleFileFilter("ses", "StateMod Stream Estimate Stations File");
		}
		else if ( __command instanceof WriteStreamEstimateCoefficientsToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Stream Estimate Coefficients File to Write");
			sff = new SimpleFileFilter("rib", "StateMod Stream Estimate Coefficients File");
		}
		else if ( __command instanceof WriteNetworkToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod Network File to Write");
			sff = new SimpleFileFilter("net", "StateMod Network File (XML Format)");
		}
		else if ( __command instanceof WriteRiverNetworkToStateMod_Command ){
			fc.setDialogTitle( "Specify StateMod River Network File to Write");
			sff = new SimpleFileFilter("rin", "StateMod River Network File");
		}
		else if ( __command instanceof WriteOperationalRightsToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Operational Rights File to Write");
			sff = new SimpleFileFilter("opr", "StateMod Operational Rights File");
		}
		else if ( __command instanceof WriteResponseToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Response File to Write");
			sff = new SimpleFileFilter("rsp", "StateMod Response File");
		}
		else if ( __command instanceof WriteControlToStateMod_Command ) {
			fc.setDialogTitle( "Specify StateMod Control File to Write");
			sff = new SimpleFileFilter("ctl", "StateMod Control File");
		}
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
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String OutputFile = __OutputFile_JTextField.getText().trim();
	//String Version = "";
	//if ( __Version_JTextField != null ) {
	//	Version = __Version_JTextField.getText().trim();
	//}
	String WriteHow = __WriteHow_JComboBox.getSelected();
	
	if (OutputFile.length() > 0) {
		props.set("OutputFile", OutputFile);
	}
	//if (Version.length() > 0) {
	//	props.set("Version", Version);
	//}
	if ( __WriteDataComments_JComboBox != null ) {
		String WriteDataComments = __WriteDataComments_JComboBox.getSelected();
		props.set("WriteDataComments", WriteDataComments);
	}
	if ( __WriteExtendedDataComments_JComboBox != null ) {
		String WriteExtendedDataComments = __WriteExtendedDataComments_JComboBox.getSelected();
		props.set("WriteExtendedDataComments", WriteExtendedDataComments);
	}
	if (WriteHow.length() > 0 ) {
		props.set("WriteHow", WriteHow);
	}
	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		if (Precision.length() > 0 ) {
			props.set("Precision", Precision);
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
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits()
{
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String WriteHow = __WriteHow_JComboBox.getSelected();
	__command.setCommandParameter("OutputFile", OutputFile);
	__command.setCommandParameter("WriteHow", WriteHow);
	if ( __WriteDataComments_JComboBox != null ) {
		String WriteDataComments = __WriteDataComments_JComboBox.getSelected();
		__command.setCommandParameter("WriteDataComments", WriteDataComments);
	}
	if ( __WriteExtendedDataComments_JComboBox != null ) {
		String WriteExtendedDataComments = __WriteExtendedDataComments_JComboBox.getSelected();
		__command.setCommandParameter("WriteExtendedDataComments", WriteExtendedDataComments);
	}
	if ( __Precision_JTextField != null ) {
		String Precision = __Precision_JTextField.getText().trim();
		__command.setCommandParameter("Precision", Precision);
	}
	
	//if ( __Version_JTextField != null ) {
	//	String Version = __Version_JTextField.getText().trim();
	//	__command.setCommandParameter("Version", Version);
	//}
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, WriteToStateMod_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = 0;

	// Main contents...

	// Now add the buttons...

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	if ( __command instanceof WriteStreamGageStationsToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes stream gage stations data to a StateMod stream gage stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteDelayTablesMonthlyToStateMod_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
			"This command writes delay tables (monthly) to a StateMod delay tables file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof WriteDelayTablesDailyToStateMod_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
			"This command writes delay tables (daily) to a StateMod delay tables file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof WriteDiversionStationsToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes diversion stations data to a StateMod diversion stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteDiversionRightsToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes diversion rights data to a StateMod diversion rights file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteReservoirStationsToStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes reservoir stations data to a StateMod reservoir stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteReservoirRightsToStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes reservoir rights data to a StateMod reservoir rights file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteReservoirReturnToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes reservoir return flow (seepage) data to a StateMod reservoir return flow file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteInstreamFlowStationsToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes instream flow stations data to a StateMod instream flow stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteInstreamFlowRightsToStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes instream flow rights data to a StateMod instream flow rights file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteWellStationsToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes well stations data to a StateMod well stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteWellRightsToStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes well rights data to a StateMod well rights file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WritePlanStationsToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes plan stations data to a StateMod plan stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WritePlanWellAugmentationToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes well augmentation plan data to a StateMod well augmentation plan data file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WritePlanReturnToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes plan return flow data to a StateMod plan return flow file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteStreamEstimateStationsToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes stream estimate stations data to a StateMod stream estimate stations file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteStreamEstimateCoefficientsToStateMod_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes stream estimate coefficients data to a StateMod stream estimate coefficients file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteNetworkToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the generalized network data to a StateMod file (XML format)."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteRiverNetworkToStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the StateMod river network data to a StateMod river network file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteOperationalRightsToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes operational rights data to a StateMod operational rights file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteResponseToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes the list of data set files to a StateMod response file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof WriteControlToStateMod_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command writes data set controlling information to a StateMod control file."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the file be specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The default value for \"Write how\" is OverwriteFile, which " +
		"will create a new file, overwriting an old file if it exists."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "StateMod file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (50);
	__OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    
	if ( (__command instanceof WriteDelayTablesMonthlyToStateMod_Command) ||
		(__command instanceof WriteDelayTablesDailyToStateMod_Command) ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Precision:"),
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	__Precision_JTextField = new JTextField(10);
    	__Precision_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Precision_JTextField,
    		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"Optional - number of digits after decimal (default=2)."),
    		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Write how:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List write_how_Vector = new Vector();
	write_how_Vector.add ( "" );
	write_how_Vector.add ( __command._OverwriteFile );
	write_how_Vector.add ( __command._UpdateFile );
	__WriteHow_JComboBox = new SimpleJComboBox(false);
	__WriteHow_JComboBox.setData ( write_how_Vector );
	__WriteHow_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __WriteHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Optional (default=" + __command._OverwriteFile + ")." ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( __command instanceof WriteWellRightsToStateMod_Command ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Write data comments?:"),
        		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List WriteDataComments_Vector = new Vector();
       	WriteDataComments_Vector.add ( "" );
       	WriteDataComments_Vector.add ( __command._False );
       	WriteDataComments_Vector.add ( __command._True );
       	__WriteDataComments_JComboBox = new SimpleJComboBox(false);
       	__WriteDataComments_JComboBox.setData ( WriteDataComments_Vector );
       	__WriteDataComments_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __WriteDataComments_JComboBox,
       		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - write parcel year, well matching class, parcel ID (default=" + __command._False + ")" ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Write extended data comments?:"),
        		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	__WriteExtendedDataComments_JComboBox = new SimpleJComboBox(false);
       	__WriteExtendedDataComments_JComboBox.setData ( WriteDataComments_Vector );
       	__WriteExtendedDataComments_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __WriteExtendedDataComments_JComboBox,
       		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - write parcel, collection, raw data permit/right (default=" + __command._False + ")" ),
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
		__path_JButton = new SimpleJButton("Remove Working Directory", this);
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
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = __command + ".refresh";
	String OutputFile = "";
	String Precision = "";
	String WriteHow = "";
	String WriteDataComments = "";
	String WriteExtendedDataComments = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		OutputFile = props.getValue ( "OutputFile" );
		Precision = props.getValue ( "Precision" );
		WriteHow = props.getValue ( "WriteHow" );
		WriteDataComments = props.getValue ( "WriteDataComments" );
		WriteExtendedDataComments = props.getValue ( "WriteExtendedDataComments" );
		if ( OutputFile != null ) {
			__OutputFile_JTextField.setText (OutputFile);
		}
		if ( __Precision_JTextField != null ) {
			if ( Precision != null ) {
				__Precision_JTextField.setText (Precision);
			}
		}
		if ( WriteHow == null ) {
			// Select default...
			__WriteHow_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__WriteHow_JComboBox, WriteHow, JGUIUtil.NONE, null, null ) ) {
				__WriteHow_JComboBox.select ( WriteHow );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing command references an invalid\nWriteHow value \"" +
				WriteHow + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __WriteDataComments_JComboBox != null ) {
			if ( WriteDataComments == null ) {
				// Select default...
				__WriteDataComments_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__WriteDataComments_JComboBox, WriteDataComments, JGUIUtil.NONE, null, null ) ) {
					__WriteDataComments_JComboBox.select ( WriteDataComments );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nWriteDataComments value \"" +
					WriteDataComments + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __WriteExtendedDataComments_JComboBox != null ) {
			if ( WriteExtendedDataComments == null ) {
				// Select default...
				__WriteExtendedDataComments_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__WriteExtendedDataComments_JComboBox, WriteExtendedDataComments, JGUIUtil.NONE, null, null ) ) {
					__WriteExtendedDataComments_JComboBox.select ( WriteExtendedDataComments );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nWriteExtendedDataComments value \"" +
					WriteExtendedDataComments + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	OutputFile = __OutputFile_JTextField.getText().trim();
	props.add("OutputFile=" + OutputFile);
	//if ( __Version_JTextField != null ) {
	//	Version = __Version_JTextField.getText().trim();
	//}
	if ( __Precision_JTextField != null ) {
		Precision = __Precision_JTextField.getText().trim();
		props.add("Precision=" + Precision);
	}
	if ( __WriteDataComments_JComboBox != null ) {
		WriteDataComments = __WriteDataComments_JComboBox.getSelected();
		props.add("WriteDataComments=" + WriteDataComments);
	}
	if ( __WriteExtendedDataComments_JComboBox != null ) {
		WriteExtendedDataComments = __WriteExtendedDataComments_JComboBox.getSelected();
		props.add("WriteExtendedDataComments=" + WriteExtendedDataComments);
	}
	WriteHow = __WriteHow_JComboBox.getSelected();
	props.add("WriteHow=" + WriteHow);
	//if ( __Version_JTextField != null ) {
	//	props.add("Version=" + Version);
	//}
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		__path_JButton.setEnabled (true);
		File f = new File (OutputFile);
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
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
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