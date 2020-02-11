// SetIrrigationPracticeTS_JDialog - Editor for SetIrrigationPracticeTS() command.

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

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Editor for SetIrrigationPracticeTS() command.
*/
@SuppressWarnings("serial")
public class SetIrrigationPracticeTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __SetStart_JTextField = null;
private JTextField __SetEnd_JTextField = null;
private JTextField __SurfaceDelEffMax_JTextField = null;
private JTextField __FloodAppEffMax_JTextField = null;
private JTextField __SprinklerAppEffMax_JTextField = null;
private JTextField __AcresSWFlood_JTextField = null;
private JTextField __AcresSWSprinkler_JTextField = null;
private JTextField __AcresGWFlood_JTextField = null;
private JTextField __AcresGWSprinkler_JTextField = null;
private JTextField __PumpingMax_JTextField = null;
private SimpleJComboBox __GWMode_JComboBox = null;
private JTextField __AcresTotal_JTextField = null;
//private JTextField __AcresGW_JTextField = null;
//private JTextField __AcresSW_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SetIrrigationPracticeTS_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetIrrigationPracticeTS_JDialog (JFrame parent, SetIrrigationPracticeTS_Command command) {
	super(parent, true);
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
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
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
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String SurfaceDelEffMax = __SurfaceDelEffMax_JTextField.getText().trim();
	String FloodAppEffMax = __FloodAppEffMax_JTextField.getText().trim();
	String SprinklerAppEffMax = __SprinklerAppEffMax_JTextField.getText().trim();
	String AcresSWFlood = __AcresSWFlood_JTextField.getText().trim();
	String AcresSWSprinkler = __AcresSWSprinkler_JTextField.getText().trim();
	String AcresGWFlood = __AcresGWFlood_JTextField.getText().trim();
	String AcresGWSprinkler = __AcresGWSprinkler_JTextField.getText().trim();
	String PumpingMax = __PumpingMax_JTextField.getText().trim();
	String GWMode = StringUtil.getToken( __GWMode_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( GWMode == null ) {
		GWMode = "";
	}
	String AcresTotal = __AcresTotal_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( SetStart.length() > 0 ) {
		parameters.set ( "SetStart", SetStart );
	}
	if ( SetEnd.length() > 0 ) {
		parameters.set ( "SetEnd", SetEnd );
	}
	if ( SurfaceDelEffMax.length() > 0 ) {
		parameters.set ( "SurfaceDelEffMax", SurfaceDelEffMax );
	}
	if ( FloodAppEffMax.length() > 0 ) {
		parameters.set ( "FloodAppEffMax", FloodAppEffMax );
	}
	if ( SprinklerAppEffMax.length() > 0 ) {
		parameters.set ( "SprinklerAppEffMax", SprinklerAppEffMax );
	}
	if ( AcresSWFlood.length() > 0 ) {
		parameters.set ( "AcresSWFlood", AcresSWFlood );
	}
	if ( AcresSWSprinkler.length() > 0 ) {
		parameters.set ( "AcresSWSprinkler", AcresSWSprinkler );
	}
	if ( AcresGWFlood.length() > 0 ) {
		parameters.set ( "AcresGWFlood", AcresGWFlood );
	}
	if ( AcresGWSprinkler.length() > 0 ) {
		parameters.set ( "AcresGWSprinkler", AcresGWSprinkler );
	}
	if ( PumpingMax.length() > 0 ) {
		parameters.set ( "PumpingMax", PumpingMax );
	}
	if ( GWMode.length() > 0 ) {
		parameters.set ( "GWMode", GWMode );
	}
	if ( AcresTotal.length() > 0 ) {
		parameters.set ( "AcresTotal", AcresTotal );
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
	String SetStart = __SetStart_JTextField.getText().trim();
	String SetEnd = __SetEnd_JTextField.getText().trim();
	String SurfaceDelEffMax = __SurfaceDelEffMax_JTextField.getText().trim();
	String FloodAppEffMax = __FloodAppEffMax_JTextField.getText().trim();
	String SprinklerAppEffMax = __SprinklerAppEffMax_JTextField.getText().trim();
	String AcresSWFlood = __AcresSWFlood_JTextField.getText().trim();
	String AcresSWSprinkler = __AcresSWSprinkler_JTextField.getText().trim();
	String AcresGWFlood = __AcresGWFlood_JTextField.getText().trim();
	String AcresGWSprinkler = __AcresGWSprinkler_JTextField.getText().trim();
	String PumpingMax = __PumpingMax_JTextField.getText().trim();
	String GWMode = StringUtil.getToken( __GWMode_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( GWMode == null ) {
		GWMode = "";
	}
	String AcresTotal = __AcresTotal_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "SetStart", SetStart );
	__command.setCommandParameter ( "SetEnd", SetEnd );
	__command.setCommandParameter ( "SurfaceDelEffMax", SurfaceDelEffMax );
	__command.setCommandParameter ( "FloodAppEffMax", FloodAppEffMax );
	__command.setCommandParameter ( "SprinklerAppEffMax", SprinklerAppEffMax );
	__command.setCommandParameter ( "AcresSWFlood", AcresSWFlood );
	__command.setCommandParameter ( "AcresSWSprinkler", AcresSWSprinkler );
	__command.setCommandParameter ( "AcresGWFlood", AcresGWFlood );
	__command.setCommandParameter ( "AcresGWSprinkler", AcresGWSprinkler );
	__command.setCommandParameter ( "PumpingMax", PumpingMax );
	__command.setCommandParameter ( "GWMode", GWMode );
	__command.setCommandParameter ( "AcresTotal", AcresTotal );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param app_PropList Properties from the application.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, SetIrrigationPracticeTS_Command command )
{	__command = command;

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
		"This command edits irrigation practice time series data," + 
		" using the CU Location ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The CU Location ID can contain a * wildcard pattern to match one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The previous irrigation practice data will be reset to new values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel ( "Blanks will result in no change to the data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The following can typically set with no restriction because they are usually a simple set, with no subsequent filling over time:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"   Efficiencies, groundwater mode, pumping maximum."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
   		"<HTML><B>When processing acreage, the following approaches should be used.<B></HTML>"),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>1. Supply supplemental acreage data that are not in HydroBase for a year with parcel data - " +
		"see SetIrrigationPracticeTSFromList().<B></HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>2. Override all acreage data that are in HydroBase - acreage pairs will be set and will be adjusted to previous " +
		"surface water only and groundwater total acres.<B></HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>3. Override one irrigation method acreage - the other term will be computed from the total.<B></HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>In all cases, acreage parts will be reduced to previous totals if necessary.<B></HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU Location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU Location(s) to fill  (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set start (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetStart_JTextField = new JTextField (10);
	__SetStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Optional - starting year to set data (default=set all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Set end (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SetEnd_JTextField = new JTextField (10);
	__SetEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SetEnd_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - ending year to set data (default=set all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Surface delivery efficiency maximum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SurfaceDelEffMax_JTextField = new JTextField (10);
	__SurfaceDelEffMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SurfaceDelEffMax_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - specify a number 0.0 to 1.0."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Flood application efficiency maximum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FloodAppEffMax_JTextField = new JTextField (10);
	__FloodAppEffMax_JTextField.addKeyListener (this);
     JGUIUtil.addComponent(main_JPanel, __FloodAppEffMax_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - specify a number 0.0 to 1.0."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Sprinkler application efficiency maximum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SprinklerAppEffMax_JTextField = new JTextField (10);
	__SprinklerAppEffMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SprinklerAppEffMax_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - specify a number 0.0 to 1.0."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Acres irrigated by surface water only (flood):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresSWFlood_JTextField = new JTextField (10);
	__AcresSWFlood_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AcresSWFlood_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Acres irrigated by surface water only (sprinkler):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresSWSprinkler_JTextField = new JTextField (10);
	__AcresSWSprinkler_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AcresSWSprinkler_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Acres irrigated by groundwater (flood):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresGWFlood_JTextField = new JTextField (10);
	__AcresGWFlood_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AcresGWFlood_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Acres irrigated by groundwater (sprinkler):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresGWSprinkler_JTextField = new JTextField (10);
	__AcresGWSprinkler_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AcresGWSprinkler_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        /* TODO SAM 2007-09-12 Remove if turns out that it is not needed.
        // Put after the others since they are obsolete
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Acres irrigated by groundwater:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresGW_JTextField = new JTextField (10);
	__AcresGW_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __AcresGW_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"<HTML><B>Used with StateCU version 10 or older.<B></HTML>"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Acres irrigated by sprinkler:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresSprinkler_JTextField = new JTextField (10);
	__AcresSprinkler_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __AcresSprinkler_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"<HTML><B>Used with StateCU version 10 or older.<B></HTML>"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
		*/
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Pumping maximum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PumpingMax_JTextField = new JTextField (10);
	__PumpingMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PumpingMax_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - ACFT per month."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Groundwater mode:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> gwmode_Vector = new Vector<String>(4);
	gwmode_Vector.add ( "" );
	gwmode_Vector.add ( "1 - Surface + GW used to maximize supply.");
	gwmode_Vector.add ( "2 - Surface water used first.");
	gwmode_Vector.add ( "3 - GW used first for sprinkler, surface for recharge");
	__GWMode_JComboBox = new SimpleJComboBox(false);
	__GWMode_JComboBox.setData ( gwmode_Vector );
	__GWMode_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __GWMode_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Acres irrigated, total:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AcresTotal_JTextField = new JTextField (10);
	__AcresTotal_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AcresTotal_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional."),
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
			
	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

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
public void keyPressed (KeyEvent event)
{	int code = event.getKeyCode();

	if (code == KeyEvent.VK_ENTER) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
}

public void keyReleased (KeyEvent event)
{	refresh();
}

public void keyTyped (KeyEvent event)
{
}

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
{	__error_wait = false;
	String routine = "setIrrigationPracticeTS_JDialog.refresh";
	String ID = "";
	String SetStart = "";
	String SetEnd = "";
	String SurfaceDelEffMax = "";
	String FloodAppEffMax = "";
	String SprinklerAppEffMax = "";
	String AcresSWFlood= "";
	String AcresSWSprinkler= "";
	String AcresGWFlood= "";
	String AcresGWSprinkler= "";
	String PumpingMax = "";
	String GWMode = "";
	String AcresTotal = "";
	//String AcresGW= "";
	//String AcresSW = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		SetStart = props.getValue ( "SetStart" );
		SetEnd = props.getValue ( "SetEnd" );
		SurfaceDelEffMax = props.getValue ( "SurfaceDelEffMax" );
		FloodAppEffMax = props.getValue ( "FloodAppEffMax" );
		SprinklerAppEffMax = props.getValue ( "SprinklerAppEffMax" );
		AcresSWFlood = props.getValue ( "AcresSWFlood" );
		AcresSWSprinkler = props.getValue ( "AcresSWSprinkler" );
		AcresGWFlood = props.getValue ( "AcresGWFlood" );
		AcresGWSprinkler = props.getValue ( "AcresGWSprinkler" );
		//AcresGW = props.getValue ( "AcresGW" );
		//AcresSW = props.getValue ( "AcresSW" );
		PumpingMax = props.getValue ( "PumpingMax" );
		GWMode = props.getValue ( "GWMode" );
		AcresTotal = props.getValue ( "AcresTotal" );
		IfNotFound = props.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( SetStart != null ) {
			__SetStart_JTextField.setText(SetStart);
		}
		if ( SetEnd != null ) {
			__SetEnd_JTextField.setText(SetEnd);
		}
		if ( SurfaceDelEffMax != null ) {
			__SurfaceDelEffMax_JTextField.setText(SurfaceDelEffMax);
		}
		if ( FloodAppEffMax != null ) {
			__FloodAppEffMax_JTextField.setText(FloodAppEffMax);
		}
		if ( SprinklerAppEffMax != null ) {
			__SprinklerAppEffMax_JTextField.setText(
			SprinklerAppEffMax);
		}
		if ( AcresSWFlood != null ) {
			__AcresSWFlood_JTextField.setText(AcresSWFlood);
		}
		if ( AcresSWSprinkler != null ) {
			__AcresSWSprinkler_JTextField.setText(AcresSWSprinkler);
		}
		if ( AcresGWFlood != null ) {
			__AcresGWFlood_JTextField.setText(AcresGWFlood);
		}
		if ( AcresGWSprinkler != null ) {
			__AcresGWSprinkler_JTextField.setText(AcresGWSprinkler);
		}
		/* TODO SAM 2007-09-12 Remove if not needed
		if ( AcresGW != null ) {
			__AcresGW_JTextField.setText(AcresGW);
		}
		if ( AcresSprinkler != null ) {
			__AcresSprinkler_JTextField.setText(AcresSprinkler);
		}
		*/
		if ( PumpingMax != null ) {
			__PumpingMax_JTextField.setText(PumpingMax);
		}
		if ( (GWMode == null) || GWMode.equals("") ) {
			// Select default...
			__GWMode_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches( __GWMode_JComboBox, true, " ", 0, 0, GWMode, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine, "Existing command references an invalid\nGWMode value \"" +
				GWMode + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AcresTotal != null ) {
			__AcresTotal_JTextField.setText(AcresTotal);
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

	ID = __ID_JTextField.getText().trim();
	SetStart = __SetStart_JTextField.getText().trim();
	SetEnd = __SetEnd_JTextField.getText().trim();
	SurfaceDelEffMax = __SurfaceDelEffMax_JTextField.getText().trim();
	FloodAppEffMax = __FloodAppEffMax_JTextField.getText().trim();
	SprinklerAppEffMax = __SprinklerAppEffMax_JTextField.getText().trim();
	AcresSWFlood = __AcresSWFlood_JTextField.getText().trim();
	AcresSWSprinkler = __AcresSWSprinkler_JTextField.getText().trim();
	AcresGWFlood = __AcresGWFlood_JTextField.getText().trim();
	AcresGWSprinkler = __AcresGWSprinkler_JTextField.getText().trim();
	/* TODO SAM 2007-09-12 Remove if not needed
	AcresGW = __AcresGW_JTextField.getText().trim();
	AcresSprinkler = __AcresSprinkler_JTextField.getText().trim();
	*/
	PumpingMax = __PumpingMax_JTextField.getText().trim();
	GWMode = StringUtil.getToken( __GWMode_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	AcresTotal = __AcresTotal_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	props = new PropList(__command.getCommandName());
	props.add("ID=" + ID);
	props.add("SetStart=" + SetStart);
	props.add("SetEnd=" + SetEnd);
	props.add("SurfaceDelEffMax=" + SurfaceDelEffMax);
	props.add("FloodAppEffMax=" + FloodAppEffMax);
	props.add("SprinklerAppEffMax=" + SprinklerAppEffMax);
	props.add("AcresSWFlood=" + AcresSWFlood);
	props.add("AcresSWSprinkler=" + AcresSWSprinkler);
	props.add("AcresGWFlood=" + AcresGWFlood);
	props.add("AcresGWSprinkler=" + AcresGWSprinkler);
	props.add("PumpingMax=" + PumpingMax);
	props.add("GWMode=" + GWMode);
	props.add("AcresTotal=" + AcresTotal);
	props.add("IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
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
	response (true);
}

// The following methods are all necessary because this class
// implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
