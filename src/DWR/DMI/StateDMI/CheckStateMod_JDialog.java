package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Check*() commands.
*/
public class CheckStateMod_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextField __ID_JTextField=null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private CheckStateMod_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public CheckStateMod_JDialog ( JFrame parent, CheckStateMod_Command command )
{	super(parent, true);
	initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response (false);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (true);
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList parameters = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
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
{	String ID = __ID_JTextField.getText().trim();
	__command.setCommandParameter ( "ID", ID );
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, CheckStateMod_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	String idLabel = "ID";
	String note = "identifiers";
	if ( __command instanceof CheckStreamGageStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks stream gage stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Daily ID is not a stream gage."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Stream gage station identifier:";
        note = "stream gage stations";
	}
	else if ( __command instanceof CheckDiversionStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks diversion stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   4) Daily ID is not a diversion (if diversion station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Diversion station identifier:";
        note = "diversion stations";
	}
	else if ( __command instanceof CheckDiversionRights_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks diversion rights, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Diversion station ID is not found (if diversion station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Diversion right identifier:";
        note = "diversion rights";
	}
	else if ( __command instanceof CheckDiversionHistoricalTSMonthly_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks diversion historical time series (monthly), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values (negatives)."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Diversion station ID is not found (if diversion station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Diversion station identifier:";
        note = "diversion historical time series (monthly)";
	}
	else if ( __command instanceof CheckDiversionDemandTSMonthly_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks diversion demand time series (monthly), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values (negatives)."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Diversion station ID is not found (if diversion station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Diversion station identifier:";
        note = "diversion demand time series (monthly)";
	}
	else if ( __command instanceof CheckReservoirStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks reservoir stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   4) Daily ID is not a reservoir (if reservoir station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Reservoir station identifier:";
        note = "reservoir stations";
	}
	else if ( __command instanceof CheckReservoirRights_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks reservoir rights, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Reservoir station ID is not found (if reservoir station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Reservoir right identifier:";
        note = "reservoir rights";
	}
	else if ( __command instanceof CheckInstreamFlowStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks instream flow stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   4) Downstream river network ID is not found in the network (if network node list is available)."),
               	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   5) Daily ID is not an instream flow station (if station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Instream flow station identifier:";
        note = "instream flow stations";
	}
	else if ( __command instanceof CheckInstreamFlowRights_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks instream flow rights, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Instream flow station ID is not found (if instream flow station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Instream flow right identifier:";
        note = "instream flow rights";
	}
	else if ( __command instanceof CheckInstreamFlowDemandTSAverageMonthly_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks instream flow demand time series (average monthly), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values (negatives)."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Instream flow station ID is not found (if instream flow station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Diversion station identifier:";
        note = "diversion historical time series (monthly)";
	}
	else if ( __command instanceof CheckWellStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks well stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   4) Daily ID is not a well (if well station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Well station identifier:";
        note = "well stations";
	}
	else if ( __command instanceof CheckWellRights_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks well rights, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Decree <= 0 after formatting for StateMod file"),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Well station capacity = sum of well rights (if well stations are available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Well station has well rights (if well stations are available)."),
            0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
    	"See also the CheckWellStations() command for useful checks."),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Well right identifier:";
        note = "well rights";
	}
	else if ( __command instanceof CheckWellHistoricalPumpingTSMonthly_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks well historical pumping time series (monthly), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values (negatives)."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Well station ID is not found (if well station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Well station identifier:";
        note = "well historical pumping time series (monthly)";
	}
	else if ( __command instanceof CheckWellDemandTSMonthly_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks well demand time series (monthly), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values (negatives)."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Well station ID is not found (if well station list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Well station identifier:";
        note = "well demand time series (monthly)";
	}
	else if ( __command instanceof CheckRiverNetwork_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks river network stations (nodes), generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Downstrem river network ID is not found in the network."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "River network node identifier:";
        note = "river network nodes";
	}
	if ( __command instanceof CheckStreamEstimateStations_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks stream estimate stations, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) River network ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) Daily ID is not a stream gage station (if the stream gage list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Stream estimate station identifier:";
        note = "stream estimate stations";
	}
	if ( __command instanceof CheckStreamEstimateCoefficients_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
        	"This command checks stream estimate coefficients, generating warnings for the follow conditions:"),
        	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   1) Missing (undefined) required values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   2) Invalid values."),
        	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ("   3) River node ID is not found in the network (if network node list is available)."),
           	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        idLabel = "Stream estimate station identifier:";
        note = "stream estimate coefficients";
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel (idLabel),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the " + note + " to check (use * for wildcard)."),
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
	   	"Optional - indicate action if no match is found (default=" + __command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

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

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();
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
{	String routine = __command + ".refresh";
	String ID = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
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

	// Always get the value that is selected...
	props = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	props.add ( "ID=" + ID );
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString ( props ) );
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