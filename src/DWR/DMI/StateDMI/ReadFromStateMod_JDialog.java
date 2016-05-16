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
Command editor dialog for simple Read*FromStateMod() commands, which share the same parameters.
Some commands use additional paramters.  If too many special parameters are needed create separate command
and/or editor classes.
*/
public class ReadFromStateMod_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __InputFile_JTextField = null;// StateMod file
private JTextField __Scale_JTextField = null;// Delay tables only
private SimpleJComboBox __IgnoreWells_JComboBox = null; // Wells only
private SimpleJComboBox __IgnoreDiversions_JComboBox = null; // Wells only
private SimpleJComboBox __IgnoreDWs_JComboBox = null; // Wells only
private SimpleJComboBox __ReadData_JComboBox = null;
private SimpleJComboBox __Append_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadFromStateMod_Command __command = null; // Command being edited
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadFromStateMod_JDialog ( JFrame parent, Command command )
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
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		SimpleFileFilter sff = null;
		fc.setDialogTitle("Select StateMod File");
		if ( __command instanceof ReadCULocationsFromStateMod_Command ) {
			sff = new SimpleFileFilter("dds", "StateMod Direct Diversion Station File");
			fc.addChoosableFileFilter(sff);
			fc.addChoosableFileFilter( new SimpleFileFilter("wes", "StateMod Well Station File") );
			fc.setFileFilter(sff);
		}
		else if ( __command instanceof ReadStreamGageStationsFromStateMod_Command ) {
			sff = new SimpleFileFilter("ris", "StateMod Stream Gage Stations File");
		}
		else if ( __command instanceof ReadDelayTablesMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("dly", "StateMod Delay Tables File (Monthly)");
		}
		else if ( __command instanceof ReadDelayTablesMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("dld", "StateMod Delay Tables File (Daily)");
		}
		else if ( __command instanceof ReadDiversionStationsFromStateMod_Command ){
			sff = new SimpleFileFilter("dds", "StateMod Diversion Stations File");
		}
		else if ( __command instanceof ReadDiversionRightsFromStateMod_Command ){
			sff = new SimpleFileFilter("ddr", "StateMod Diversion Rights File");
		}
		else if ( __command instanceof ReadDiversionHistoricalTSMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("ddh", "StateMod Diversion Historical TS (Monthly) File");
		}
		else if ( __command instanceof ReadDiversionDemandTSMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("ddm", "StateMod Diversion Demand TS (Monthly) File");
		}
		else if ( __command instanceof ReadReservoirStationsFromStateMod_Command ) {
			sff = new SimpleFileFilter("res", "StateMod Reservoir Stations File");
		}
		else if ( __command instanceof ReadReservoirRightsFromStateMod_Command ){
			sff = new SimpleFileFilter("rer", "StateMod Reservoir Rights File");
		}
		else if ( __command instanceof ReadReservoirReturnFromStateMod_Command ) {
			sff = new SimpleFileFilter("rrf", "StateMod Reservoir Return File");
		}
		else if ( __command instanceof ReadInstreamFlowStationsFromStateMod_Command ){
			sff = new SimpleFileFilter("ifs", "StateMod Instream Flow Stations File");
		}
		else if ( __command instanceof ReadInstreamFlowRightsFromStateMod_Command ){
			sff = new SimpleFileFilter("ifr", "StateMod Instream Flow Rights File");
		}
		else if ( __command instanceof ReadInstreamFlowDemandTSAverageMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("ifa", "StateMod Instream Flow Demand TS (Average Monthly) File");
		}
		else if ( __command instanceof ReadWellStationsFromStateMod_Command ){
			sff = new SimpleFileFilter("wes", "StateMod Well Stations File");
		}
		else if ( __command instanceof ReadWellRightsFromStateMod_Command ){
			sff = new SimpleFileFilter("wer", "StateMod Well Rights File");
		}
		else if ( __command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateMod_Command ){
			sff = new SimpleFileFilter("weh", "StateMod Well Historical Pumping TS (Monthly) File");
		}
		else if ( __command instanceof ReadWellDemandTSMonthlyFromStateMod_Command ){
			// Demands could be read either from the weh or wem...
			fc.addChoosableFileFilter( new SimpleFileFilter("weh", "StateMod Well Historical Pumping TS (Monthly) File"));
			sff = new SimpleFileFilter("wem", "StateMod Well Demand TS (Monthly) File");
		}
		else if ( (__command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command) ){
			sff = new SimpleFileFilter("weh", "StateCU/StateMod Well Historical Pumping TS (Monthly) File");
		}
		else if ( __command instanceof ReadPlanStationsFromStateMod_Command ) {
			sff = new SimpleFileFilter("pln", "StateMod Plan Stations File");
		}
		else if ( __command instanceof ReadPlanWellAugmentationFromStateMod_Command ) {
			sff = new SimpleFileFilter("plw", "StateMod Well Augmentation Plan File");
		}
		else if ( __command instanceof ReadPlanReturnFromStateMod_Command ) {
			sff = new SimpleFileFilter("prf", "StateMod Plan Return File");
		}
		else if ( __command instanceof ReadStreamEstimateStationsFromStateMod_Command ) {
			sff = new SimpleFileFilter("ses", "StateMod Stream Estimate Stations File");
		}
		else if ( __command instanceof ReadStreamEstimateCoefficientsFromStateMod_Command ) {
			sff = new SimpleFileFilter("rib", "StateMod Stream Estimate Coefficients File");
		}
		else if ( __command instanceof ReadNetworkFromStateMod_Command ){
			fc.addChoosableFileFilter( new SimpleFileFilter("net", "StateMod Network File (Makenet format)"));
			sff = new SimpleFileFilter("net", "StateMod Network File (XML format)");
		}
		else if ( __command instanceof ReadRiverNetworkFromStateMod_Command ){
			sff = new SimpleFileFilter("rin", "StateMod River Network File");
		}
		else if ( __command instanceof ReadOperationalRightsFromStateMod_Command ) {
			sff = new SimpleFileFilter("opr", "StateMod Operational Rights File");
		}
		else if ( __command instanceof ReadResponseFromStateMod_Command ) {
			sff = new SimpleFileFilter("rsp", "StateMod Response File");
		}
		else if ( __command instanceof ReadControlFromStateMod_Command ) {
			sff = new SimpleFileFilter("ctl", "StateMod Control File");
		}
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__InputFile_JTextField.setText(path);
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
			__InputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __InputFile_JTextField.getText()));
		}
		else if (__path_JButton.getText().equals( "Remove Working Directory")) {
			try {
				__InputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __InputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, __command + "_JDialog", "Error converting file to relative path.");
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
	if ( __Scale_JTextField != null ) {
		String Scale = __Scale_JTextField.getText().trim();
		if (Scale.length() > 0 ) {
			props.set("Scale", Scale);
		}
	}
	if ( __IgnoreDiversions_JComboBox != null ) {
		String IgnoreDiversions = __IgnoreDiversions_JComboBox.getSelected();
		if (IgnoreDiversions.length() > 0 ) {
			props.set("IgnoreDiversions", IgnoreDiversions);
		}
	}
	if ( __IgnoreDWs_JComboBox != null ) {
		String IgnoreDWs = __IgnoreDWs_JComboBox.getSelected();
		if (IgnoreDWs.length() > 0 ) {
			props.set("IgnoreDWs", IgnoreDWs);
		}
	}
	if ( __IgnoreWells_JComboBox != null ) {
		String IgnoreWells = __IgnoreWells_JComboBox.getSelected();
		if (IgnoreWells.length() > 0 ) {
			props.set("IgnoreWells", IgnoreWells);
		}
	}
	if ( __ReadData_JComboBox != null ) {
		String ReadData = __ReadData_JComboBox.getSelected();
		if (ReadData.length() > 0 ) {
			props.set("ReadData", ReadData);
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
	if ( __Scale_JTextField != null ) {
		String Scale = __Scale_JTextField.getText().trim();
		__command.setCommandParameter("Scale", Scale);
	}
	if ( __IgnoreDWs_JComboBox != null ) {
		String IgnoreDWs = __IgnoreDWs_JComboBox.getSelected();
		__command.setCommandParameter("IgnoreDWs", IgnoreDWs);
	}
	if ( __IgnoreWells_JComboBox != null ) {
		String IgnoreWells = __IgnoreWells_JComboBox.getSelected();
		__command.setCommandParameter("IgnoreWells", IgnoreWells);
	}
	if ( __IgnoreDiversions_JComboBox != null ) {
		String IgnoreDiversions = __IgnoreDiversions_JComboBox.getSelected();
		__command.setCommandParameter("IgnoreDiversions", IgnoreDiversions);
	}
	if ( __ReadData_JComboBox != null ) {
		String ReadData = __ReadData_JComboBox.getSelected();
		__command.setCommandParameter("ReadData", ReadData);
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__InputFile_JTextField = null;
	__IgnoreDWs_JComboBox = null;
	__IgnoreWells_JComboBox = null;
	__Append_JComboBox = null;
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
@param command Command being edited.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (ReadFromStateMod_Command)command;
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
	if ( __command instanceof ReadCULocationsFromStateMod_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads CU locations from a StateMod direct " +
			"diversion station file or well station file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel (
			"A CU Location is a location where water requirement is estimated."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel (
			"Only the list of identifiers and names are read (latitude, " +
			"region1, etc., must be assigned with other commands)."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel (
			"Only diversions and wells with the following agricultural " +
			"demand source (demsrc) are read:"),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(paragraph, new JLabel (
			"1 (GIS), 2 (TIA), 3 (GIS primary), 4 (TIA primary), and 8 (user defined)."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else if ( __command instanceof ReadStreamGageStationsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads stream gage stations from a StateMod stream gage station file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"A stream gage is a location where flows have been measured " +
		"historically and time series are available in the data set."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof ReadDelayTablesMonthlyFromStateMod_Command) ||
		(__command instanceof ReadDelayTablesDailyFromStateMod_Command) ) {
		if ( __command instanceof ReadDelayTablesMonthlyFromStateMod_Command) {
			JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command reads monthly delay tables from a StateMod delay tables file."),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __command instanceof ReadDelayTablesDailyFromStateMod_Command) {
			JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command reads daily delay tables from a StateMod delay tables file."),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"Delay tables indicate how returns (or depletions) are distributed over time."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    	"These data are used in spatial delay/depletion assignments for diversions and wells."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"By default, is assumed that tables are read with values as percent (0 to 100)."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    		"If necessary, use the scale to multiply the values as they" +
    		" are read, to convert fracton (0-1) to percent (0-100)."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadDiversionStationsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads diversion stations from a StateMod direct diversion stations file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Diversion stations indicate locations where water is " +
		"diverted from a river, lake, or reservoir."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadDiversionRightsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads diversion rights from a StateMod direct diversion rights file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Diversion rights are associated with diversion stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadDiversionHistoricalTSMonthlyFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads diversion historical time series (monthly)"+
			" from a StateMod time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Diversion historical time series (monthly) are associated with diversion stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadDiversionDemandTSMonthlyFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads diversion demand time series (monthly)"+
			" from a StateMod time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Diversion demand time series (monthly) are associated with diversion stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}

	else if ( __command instanceof ReadReservoirStationsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads reservoir stations from a StateMod reservoir stations file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Reservoir stations indicate locations where water can be stored for use at a later date."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	}
	else if ( __command instanceof ReadReservoirRightsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads reservoir rights from a StateMod reservoir rights file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Reservoir rights are associated with reservoir stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadReservoirReturnFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads reservoir return data from a StateMod reservoir return file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The return data indicates how reservoir seepage is returned to the system."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadInstreamFlowStationsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads instream flow stations from a StateMod instream flow stations file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Instream flow stations indicate locations where surface " +
		"water flow is associated with an instream flow constraint."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadInstreamFlowRightsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads instream flow rights from a StateMod instream flow rights file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Instream flow rights are associated with instream flow stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadInstreamFlowDemandTSAverageMonthlyFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads instream flow demand time series (average monthly)"+
			" from a StateMod time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Instream flow demand time series (average monthly) are associated with instream flow stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellStationsFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command reads well stations from a StateMod well stations file."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Well stations indicate locations where ONLY groundwater supply is used to meet demand"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"and locations where groundwater supply supplements surface water (diversion) supply."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"(See also diversion stations that are supplemented by well pumping.)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellRightsFromStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads well rights from a StateMod " +
		"well rights file.  Well rights are associated with well stations."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads well historical pumping time series " +
			"(monthly) from a StateMod (or StateCU) well pumping time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Well historical pumping time series (monthly) are associated with well stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads well demand time series " +
			"(monthly) from a StateCU (or StateMod) well pumping time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Well demand time series (monthly) are associated with well stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadWellDemandTSMonthlyFromStateMod_Command){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads well demand time series " +
			"(monthly) from a StateMod well demand time series file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Well demand time series (monthly) are associated with well stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadPlanStationsFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads plan stations from a StateMod plan stations file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"A plan is an administrative model construct to control the delivery of water, " +
		"beyond basic allocation and operating rules."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadPlanWellAugmentationFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads well augmentation plan data from a StateMod well augmentation plan data file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"A plan is an administrative model construct to control the delivery of water, " +
		"beyond basic allocation and operating rules."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadPlanReturnFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads plan return data from a StateMod plan return file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The return data indicates how canal seepage is returned to the system."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadStreamEstimateStationsFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads stream estimate stations from a StateMod stream gage station file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"A stream estimate station is a location where flows are " +
		"estimated by prorating stream gage time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadStreamEstimateCoefficientsFromStateMod_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
       		"<HTML><B>This command is typically used for testing and minor changes to an existing" +
       		" stream estimate coefficients file.</B></HTML>"),
       		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
        	"<HTML><B>Normally, coefficients are calculated by reading stream estimate stations and " +
        	"then using the CalculateStreamEstimateCoefficients() command.</B></HTML>"),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads stream estimate coefficients from a StateMod stream estimate coefficients file."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The coefficients are associated with stream estimate stations, which are locations where flows are " +
		"estimated by prorating stream gage time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadNetworkFromStateMod_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads the generalized network from a StateMod " +
		"network file (old Makenet format or new XML format)."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The network can then be used, for example, to create the " +
		"river network specifically needed by StateMod."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The following data are read:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"  Identifier (ID)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"  River node identifier - set to the identifier."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"  River node name - set to the node description."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadRiverNetworkFromStateMod_Command ) {
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads the StateMod river network file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The StateMod river network contains river node identifiers, " +
		"names, and downstream node identifiers."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	}
	else if ( __command instanceof ReadOperationalRightsFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads operational rights from a StateMod operational rights file."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"An operational right (operating rule) defines custom rules to control the delivery of water, " +
		"beyond basic allocation."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadResponseFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads a StateMod response file, which contains a list of files in the data set."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"<html><b>The command is used to test reading/writing the response file - " +
			"do not use in full production.</b></html>."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof ReadControlFromStateMod_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads a StateMod control file, which contains data set controlling information."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(paragraph, new JLabel (
			"<html><b>The command is used to test reading/writing the control file - " +
			"do not use in full production.</b></html>."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
        JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
       	JGUIUtil.addComponent(paragraph, new JLabel ( "The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( __command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command){
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("StateCU file:"),
	    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("StateMod file:"),
	   		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__InputFile_JTextField = new JTextField (35);
	__InputFile_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ("Browse", this);
        JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
        
    if ( __command instanceof ReadDelayTablesMonthlyFromStateMod_Command ||
    	__command instanceof ReadDelayTablesDailyFromStateMod_Command) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Scale:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        __Scale_JTextField = new JTextField (10);
        __Scale_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Scale_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - scale to/from fraction/percent (default=no scale)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }

	if ( (__command instanceof ReadWellStationsFromStateMod_Command) ||
		(__command instanceof ReadWellDemandTSMonthlyFromStateMod_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Ignore diversion/well stations?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List ignore_Vector = new Vector(3);
		ignore_Vector.add ( "" );
		ignore_Vector.add ( __command._False );
		ignore_Vector.add ( __command._True );
		__IgnoreDWs_JComboBox = new SimpleJComboBox(false);
		__IgnoreDWs_JComboBox.setData( ignore_Vector );
		__IgnoreDWs_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __IgnoreDWs_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - ignore diversion/well stations in read (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

       	JGUIUtil.addComponent(main_JPanel, new JLabel (	"Ignore well stations?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		ignore_Vector = new Vector(3);
		ignore_Vector.add ( "" );
		ignore_Vector.add ( __command._False );
		ignore_Vector.add ( __command._True );
		__IgnoreWells_JComboBox = new SimpleJComboBox(false);
		__IgnoreWells_JComboBox.setData( ignore_Vector );
		__IgnoreWells_JComboBox.addItemListener (this);
        	JGUIUtil.addComponent(main_JPanel, __IgnoreWells_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - ignore well stations in read (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	if ( __command instanceof ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command ) {
      	JGUIUtil.addComponent(main_JPanel, new JLabel (	"Ignore diversion stations?:"),
  			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		List ignore_Vector = new Vector(3);
		ignore_Vector.add ( "" );
		ignore_Vector.add ( __command._False );
		ignore_Vector.add ( __command._True );
		__IgnoreDiversions_JComboBox = new SimpleJComboBox(false);
		__IgnoreDiversions_JComboBox.setData( ignore_Vector );
		__IgnoreDiversions_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __IgnoreDiversions_JComboBox,
  			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
  	   	JGUIUtil.addComponent(main_JPanel, new JLabel (
  			"Optional - ignore diversion stations in read (default=" + __command._False + ")."),
  			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	if ( __command instanceof ReadWellRightsFromStateMod_Command ){
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Append?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        	List Append_Vector = new Vector(3);
		Append_Vector.add ( "" );
		Append_Vector.add ( __command._False );
		Append_Vector.add ( __command._True );
		__Append_JComboBox = new SimpleJComboBox(false);
		__Append_JComboBox.setData( Append_Vector );
		__Append_JComboBox.addItemListener (this);
        	JGUIUtil.addComponent(main_JPanel, __Append_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - append to previously read/processed well rights (default=True)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	if ( __command instanceof ReadResponseFromStateMod_Command ){
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Read data?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> readDataChoices = new Vector(3);
		readDataChoices.add ( "" );
		readDataChoices.add ( __command._False );
		readDataChoices.add ( __command._True );
		__ReadData_JComboBox = new SimpleJComboBox(false);
		__ReadData_JComboBox.setData( readDataChoices );
		__ReadData_JComboBox.addItemListener (this);
        	JGUIUtil.addComponent(main_JPanel, __ReadData_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - read data in files (default=" + __command._False + ")"),
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
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

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
{	String routine = getClass().getName() + "_JDialog.refresh";
	String InputFile = "";
	String Scale = "";
	String IgnoreDWs = "";
	String IgnoreWells = "";
	String IgnoreDiversions = "";
	String Append = "";
	String ReadData = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		InputFile = props.getValue ( "InputFile" );
		Scale = props.getValue ( "Scale" );
		IgnoreDWs = props.getValue ( "IgnoreDWs" );
		IgnoreWells = props.getValue ( "IgnoreWells" );
		IgnoreDiversions = props.getValue ( "IgnoreDiversions" );
		Append = props.getValue ( "Append" );
		ReadData = props.getValue ( "ReadData" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
		}
		if ( __Scale_JTextField != null ) {
			if ( Scale != null ) {
				__Scale_JTextField.setText ( Scale );
			}
		}
		if ( __IgnoreDWs_JComboBox != null ) {
			if ( IgnoreDWs == null ) {
				// Select default...
				__IgnoreDWs_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IgnoreDWs_JComboBox, IgnoreDWs, JGUIUtil.NONE, null, null ) ) {
					__IgnoreDWs_JComboBox.select ( IgnoreDWs );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" +
					"IgnoreDWs value \"" + IgnoreDWs + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __IgnoreWells_JComboBox != null ) {
			if ( IgnoreWells == null ) {
				// Select default...
				__IgnoreWells_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IgnoreWells_JComboBox, IgnoreWells, JGUIUtil.NONE, null, null ) ) {
					__IgnoreWells_JComboBox.select (
					IgnoreWells );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" +
					"IgnoreWells value \"" + IgnoreWells + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __IgnoreDiversions_JComboBox != null ) {
			if ( IgnoreDiversions == null ) {
				// Select default...
				__IgnoreDiversions_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IgnoreDiversions_JComboBox, IgnoreDiversions, JGUIUtil.NONE, null, null ) ) {
					__IgnoreDiversions_JComboBox.select (
					IgnoreDiversions );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" +
					"IgnoreDiversions value \"" + IgnoreDiversions + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __Append_JComboBox != null ) {
			if ( Append == null ) {
				// Select default...
				__Append_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__Append_JComboBox, Append, JGUIUtil.NONE, null, null ) ) {
					__Append_JComboBox.select ( Append );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" +
					"Append value \"" + Append + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __ReadData_JComboBox != null ) {
			if ( ReadData == null ) {
				// Select default...
				__ReadData_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__ReadData_JComboBox, ReadData, JGUIUtil.NONE, null, null ) ) {
					__ReadData_JComboBox.select ( ReadData );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" +
					"ReadData value \"" + ReadData + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	InputFile = __InputFile_JTextField.getText().trim();
	props.add("InputFile=" + InputFile);
	if ( __Scale_JTextField != null ) {
		Scale = __Scale_JTextField.getText().trim();
		props.add("Scale=" + Scale);
	}
	if ( __IgnoreDWs_JComboBox != null ) {
		IgnoreDWs = __IgnoreDWs_JComboBox.getSelected();
		props.add("IgnoreDWs=" + IgnoreDWs);
	}
	if ( __IgnoreWells_JComboBox != null ) {
		IgnoreWells = __IgnoreWells_JComboBox.getSelected();
		props.add("IgnoreWells=" + IgnoreWells);
	}
	if ( __IgnoreDiversions_JComboBox != null ) {
		IgnoreDiversions = __IgnoreDiversions_JComboBox.getSelected();
		props.add("IgnoreDiversions=" + IgnoreDiversions);
	}
	if ( __Append_JComboBox != null ) {
		Append = __Append_JComboBox.getSelected();
		props.add("Append=" + Append);
	}
	if ( __ReadData_JComboBox != null ) {
		ReadData = __ReadData_JComboBox.getSelected();
		props.add("ReadData=" + ReadData);
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