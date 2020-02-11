// FillAndSetReservoirStation_JDialog - Editor for FillReservoirStation() and SetReservoirStation() commands.

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

import javax.swing.BorderFactory;
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

import DWR.StateMod.StateMod_Reservoir;
import DWR.StateMod.StateMod_ReservoirAccount;

/**
Editor for FillReservoirStation() and SetReservoirStation() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetReservoirStation_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __RiverNodeID_JTextField = null;
private SimpleJComboBox __OnOff_JComboBox = null;
private SimpleJComboBox __OneFillRule_JComboBox = null;
private JTextField __DailyID_JTextField = null;
private JTextField __ContentMin_JTextField = null;
private JTextField __ContentMax_JTextField = null;
private JTextField __ReleaseMax_JTextField = null;
private JTextField __DeadStorage_JTextField = null;
// Start accounts...
private JTextField __AccountID_JTextField = null;
private JTextField __AccountName_JTextField = null;
private JTextField __AccountMax_JTextField = null;
private JTextField __AccountInitial_JTextField = null;
private SimpleJComboBox __AccountEvap_JComboBox = null;
private SimpleJComboBox __AccountOneFill_JComboBox = null;
// End accounts...
private JTextField __EvapStations_JTextField = null;
private JTextField __PrecipStations_JTextField = null;
private JTextArea __ContentAreaSeepage_JTextArea = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
// TODO SAM 2004-07-08 need to deal with now to set/override accounts
//private SimpleJComboBox __IfAccountNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private FillAndSetReservoirStation_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetReservoirStation_JDialog ( JFrame parent, FillAndSetReservoirStation_Command command )
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
	String Name = __Name_JTextField.getText().trim();
	String RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	String OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	String OneFillRule = StringUtil.getToken( __OneFillRule_JComboBox.getSelected(), " ", 0, 0 );
	if ( OneFillRule == null ) {
		OneFillRule = "";
	}
	String DailyID = __DailyID_JTextField.getText().trim();
	String ContentMin = __ContentMin_JTextField.getText().trim();
	String ContentMax = __ContentMax_JTextField.getText().trim();
	String ReleaseMax = __ReleaseMax_JTextField.getText().trim();
	String DeadStorage = __DeadStorage_JTextField.getText().trim();
	String AccountID = __AccountID_JTextField.getText().trim();
	String AccountName = __AccountName_JTextField.getText().trim();
	String AccountMax = __AccountMax_JTextField.getText().trim();
	String AccountInitial = __AccountInitial_JTextField.getText().trim();
	String AccountEvap = StringUtil.getToken( __AccountEvap_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountEvap == null ) {
		AccountEvap = "";
	}
	String AccountOneFill = StringUtil.getToken( __AccountOneFill_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountOneFill == null ) {
		AccountOneFill = "";
	}
	String EvapStations = __EvapStations_JTextField.getText().trim();
	String PrecipStations = __PrecipStations_JTextField.getText().trim();
	String ContentAreaSeepage = __ContentAreaSeepage_JTextArea.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( Name.length() > 0 ) {
		parameters.set ( "Name", Name );
	}
    if ( RiverNodeID.length() > 0 ) {
        parameters.set ( "RiverNodeID", RiverNodeID );
    }
    if ( OnOff.length() > 0 ) {
        parameters.set ( "OnOff", OnOff );
    }
    if ( OneFillRule.length() > 0 ) {
        parameters.set ( "OneFillRule", OneFillRule );
    }
    if ( DailyID.length() > 0 ) {
        parameters.set ( "DailyID", DailyID );
    }
    if ( ContentMin.length() > 0 ) {
        parameters.set ( "ContentMin", ContentMin );
    }
    if ( ContentMax.length() > 0 ) {
        parameters.set ( "ContentMax", ContentMax );
    }
    if ( ReleaseMax.length() > 0 ) {
        parameters.set ( "ReleaseMax", ReleaseMax );
    }
    if ( DeadStorage.length() > 0 ) {
        parameters.set ( "DeadStorage", DeadStorage );
    }
    if ( AccountID.length() > 0 ) {
        parameters.set ( "AccountID", AccountID );
    }
    if ( AccountName.length() > 0 ) {
        parameters.set ( "AccountName", AccountName );
    }
    if ( AccountMax.length() > 0 ) {
        parameters.set ( "AccountMax", AccountMax );
    }
    if ( AccountInitial.length() > 0 ) {
        parameters.set ( "AccountInitial", AccountInitial );
    }
    if ( AccountEvap.length() > 0 ) {
        parameters.set ( "AccountEvap", AccountEvap );
    }
    if ( AccountOneFill.length() > 0 ) {
        parameters.set ( "AccountOneFill", AccountOneFill );
    }
    if ( EvapStations.length() > 0 ) {
        parameters.set ( "EvapStations", EvapStations );
    }
    if ( PrecipStations.length() > 0 ) {
        parameters.set ( "PrecipStations", PrecipStations );
    }
    if ( ContentAreaSeepage.length() > 0 ) {
        parameters.set ( "ContentAreaSeepage", ContentAreaSeepage );
    }
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
	__error_wait = false;
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
	String Name = __Name_JTextField.getText().trim();
	String RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	String OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	String OneFillRule = StringUtil.getToken( __OneFillRule_JComboBox.getSelected(), " ", 0, 0 );
	if ( OneFillRule == null ) {
		OneFillRule = "";
	}
	String DailyID = __DailyID_JTextField.getText().trim();
	String ContentMin = __ContentMin_JTextField.getText().trim();
	String ContentMax = __ContentMax_JTextField.getText().trim();
	String ReleaseMax = __ReleaseMax_JTextField.getText().trim();
	String DeadStorage = __DeadStorage_JTextField.getText().trim();
	String AccountID = __AccountID_JTextField.getText().trim();
	String AccountName = __AccountName_JTextField.getText().trim();
	String AccountMax = __AccountMax_JTextField.getText().trim();
	String AccountInitial = __AccountInitial_JTextField.getText().trim();
	String AccountEvap = StringUtil.getToken( __AccountEvap_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountEvap == null ) {
		AccountEvap = "";
	}
	String AccountOneFill = StringUtil.getToken( __AccountOneFill_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountOneFill == null ) {
		AccountOneFill = "";
	}
	String EvapStations = __EvapStations_JTextField.getText().trim();
	String PrecipStations = __PrecipStations_JTextField.getText().trim();
	String ContentAreaSeepage = __ContentAreaSeepage_JTextArea.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "RiverNodeID", RiverNodeID );
    __command.setCommandParameter ( "OnOff", OnOff );
    __command.setCommandParameter ( "OneFillRule", OneFillRule );
    __command.setCommandParameter ( "DailyID", DailyID );
    __command.setCommandParameter ( "ContentMin", ContentMin );
    __command.setCommandParameter ( "ContentMax", ContentMax );
    __command.setCommandParameter ( "ReleaseMax", ReleaseMax );
    __command.setCommandParameter ( "DeadStorage", DeadStorage );
    __command.setCommandParameter ( "AccountID", AccountID );
    __command.setCommandParameter ( "AccountName", AccountName );
    __command.setCommandParameter ( "AccountMax", AccountMax );
    __command.setCommandParameter ( "AccountInitial", AccountInitial );
    __command.setCommandParameter ( "AccountEvap", AccountEvap );
    __command.setCommandParameter ( "AccountOneFill", AccountOneFill );
    __command.setCommandParameter ( "EvapStations", EvapStations );
    __command.setCommandParameter ( "PrecipStations", PrecipStations );
    __command.setCommandParameter ( "ContentAreaSeepage", ContentAreaSeepage );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, FillAndSetReservoirStation_Command command )
{	__command = command;

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
	if ( __command instanceof FillReservoirStation_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in reservoir station(s)," + 
		" using the reservoir station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetReservoirStation_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command edits data in reservoir station(s)," + 
		" using the reservoir station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The reservoir station ID can contain a * wildcard pattern to match one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __command instanceof SetReservoirStation_Command ) {
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"If the reservoir station ID does not contain a * wildcard pattern and does not match an ID,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"the location will be added if the \"If not found\" option is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Only one account can be edited with each command (use additional commands for acount 2+)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Reservoir station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - reservior stations to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Name_JTextField = new JTextField (20);
	__Name_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - up to 24 characters for StateMod."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("River node ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__RiverNodeID_JTextField = new JTextField(10);
	__RiverNodeID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __RiverNodeID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - river node identifier or \"ID\"."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("On/Off:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OnOff_JComboBox = new SimpleJComboBox();
	List<String> iressw = StateMod_Reservoir.getIresswChoices(true);
	iressw.add ( 0, "" );	// Blank to indicate no change
	__OnOff_JComboBox.setData( iressw );
	__OnOff_JComboBox.addItemListener (this);
	__OnOff_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OnOff_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - is reservoir station on/off in data set?"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("One fill rule:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OneFillRule_JComboBox = new SimpleJComboBox();
	List<String> rdate = StateMod_Reservoir.getRdateChoices(true);
	rdate.add ( 0, "" );	// Blank to indicate no change
	__OneFillRule_JComboBox.setData( rdate );
	__OneFillRule_JComboBox.addItemListener (this);
	__OneFillRule_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OneFillRule_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - one fill rule administration switch."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Daily ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DailyID_JTextField = new JTextField(10);
	__DailyID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DailyID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - daily identifier, \"ID\", or StateMod flag)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Content (minimum):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ContentMin_JTextField = new JTextField (10);
	__ContentMin_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ContentMin_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - minimum content, AF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Content (maximum):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ContentMax_JTextField = new JTextField (10);
	__ContentMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ContentMax_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - maximum content, AF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Release (maximum):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ReleaseMax_JTextField = new JTextField (10);
	__ReleaseMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ReleaseMax_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - maximum release, CFS."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Dead storage:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DeadStorage_JTextField = new JTextField (10);
	__DeadStorage_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DeadStorage_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - dead storage, AF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Put in a group panel for all the account information...

	JPanel account_JPanel = new JPanel();
	account_JPanel.setLayout(new GridBagLayout());
	account_JPanel.setBorder(BorderFactory.createTitledBorder(
		"Accounts (must use one command for each account - use multiple commands with only this section " +
		"complete if necessary)") );

    JGUIUtil.addComponent(account_JPanel, new JLabel ("Account ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountID_JTextField = new JTextField (10);
	__AccountID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel, __AccountID_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel (
		"Required if setting account - account number (position in data), 1+."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(account_JPanel, new JLabel ("Account name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountName_JTextField = new JTextField (10);
	__AccountName_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel, __AccountName_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel ( "Optional - account name."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(account_JPanel, new JLabel ("Maximum storage:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountMax_JTextField = new JTextField (10);
	__AccountMax_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel, __AccountMax_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel ( "Optional - account maximum storage (ACFT)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(account_JPanel, new JLabel ("Initial storage:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountInitial_JTextField = new JTextField (10);
	__AccountInitial_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel,__AccountInitial_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel ( "Optional - account initial storage (ACFT)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(account_JPanel, new JLabel ( "Evaporation distribution:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountEvap_JComboBox = new SimpleJComboBox();
	List<String> pcteva = StateMod_ReservoirAccount.getPctevaChoices(true);
	pcteva.add ( 0, "" );	// Blank to indicate no change
	__AccountEvap_JComboBox.setData( pcteva );
	__AccountEvap_JComboBox.addItemListener (this);
	__AccountEvap_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel, __AccountEvap_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel ( "Optional - evaporation distribution option."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(account_JPanel, new JLabel ( "One fill rule calculation:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AccountOneFill_JComboBox = new SimpleJComboBox();
	List<String> n2own = StateMod_ReservoirAccount.getN2ownChoices(true);
	n2own.add ( 0, "" );	// Blank to indicate no change
	__AccountOneFill_JComboBox.setData( n2own );
	__AccountOneFill_JComboBox.addItemListener (this);
	__AccountOneFill_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(account_JPanel, __AccountOneFill_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(account_JPanel, new JLabel (
		"Optional - ownership for one fill rule calculation."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, account_JPanel,
		0, y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Evaporation stations:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EvapStations_JTextField = new JTextField (20);
	__EvapStations_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EvapStations_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - ID1,%; ID2,%..."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel,new JLabel("Precipitation stations:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PrecipStations_JTextField = new JTextField (20);
	__PrecipStations_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PrecipStations_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - ID1,%; ID2,%..."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Content/Area/Seepage:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ContentAreaSeepage_JTextArea = new JTextArea(4,30);
	__ContentAreaSeepage_JTextArea.setLineWrap ( true );
	__ContentAreaSeepage_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ContentAreaSeepage_JTextArea),
		1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - use format:  Content,Area,Seepage"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "(one record per line)"),
		3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IfNotFound_List = new Vector<String>();
	IfNotFound_List.add ( "" );
	if ( __command instanceof SetReservoirStation_Command ) {
		IfNotFound_List.add ( __command._Add );
	}
	IfNotFound_List.add ( __command._Ignore );
	IfNotFound_List.add ( __command._Warn );
	IfNotFound_List.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
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
		// If in one of the text areas do not want to do the following...
		if ( (event.getSource() != __ContentAreaSeepage_JTextArea) ) {
			checkInput ();
			if (!__error_wait) {
				response (true);
			}
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
	String routine = "FillAndSetReservoirStation_JDialog.refresh";
	String ID = "";
	String Name = "";
	String RiverNodeID = "";
	String OnOff = "";
	String OneFillRule = "";
	String DailyID = "";
	String ContentMin = "";
	String ContentMax = "";
	String ReleaseMax = "";
	String DeadStorage = "";
	String AccountID = "";
	String AccountName = "";
	String AccountMax = "";
	String AccountInitial = "";
	String AccountEvap = "";
	String AccountOneFill = "";
	String EvapStations = "";
	String PrecipStations = "";
	String ContentAreaSeepage = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		Name = props.getValue ( "Name" );
		RiverNodeID = props.getValue ( "RiverNodeID" );
		OnOff = props.getValue ( "OnOff" );
		OneFillRule = props.getValue ( "OneFillRule" );
		DailyID = props.getValue ( "DailyID" );
		ContentMin = props.getValue ( "ContentMin" );
		ContentMax = props.getValue ( "ContentMax" );
		ReleaseMax = props.getValue ( "ReleaseMax" );
		DeadStorage = props.getValue ( "DeadStorage" );
		AccountID = props.getValue ( "AccountID" );
		AccountName = props.getValue ( "AccountName" );
		AccountMax = props.getValue ( "AccountMax" );
		AccountInitial = props.getValue ( "AccountInitial" );
		AccountEvap = props.getValue ( "AccountEvap" );
		AccountOneFill = props.getValue ( "AccountOneFill" );
		EvapStations = props.getValue ( "EvapStations" );
		PrecipStations = props.getValue ( "PrecipStations" );
		ContentAreaSeepage = props.getValue ( "ContentAreaSeepage" );
		IfNotFound = props.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( Name != null ) {
			__Name_JTextField.setText(Name);
		}
		if ( RiverNodeID != null ) {
			__RiverNodeID_JTextField.setText(RiverNodeID);
		}
		if ( OnOff == null ) {
			// Select default...
			__OnOff_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __OnOff_JComboBox, true, " ", 0, 0, OnOff, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"OnOff value \"" + OnOff + "\".  Using the user value.");
				__OnOff_JComboBox.setText ( OnOff );
			}
		}
		if ( OneFillRule == null ) {
			// Select default...
			__OneFillRule_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __OneFillRule_JComboBox, true, " ", 0, 0, OneFillRule, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"OneFillRule value \"" + OneFillRule + "\".  Using the user value.");
				__OneFillRule_JComboBox.setText ( OneFillRule );
			}
		}
		if ( DailyID != null ) {
			__DailyID_JTextField.setText(DailyID);
		}
		if ( ContentMin != null ) {
			__ContentMin_JTextField.setText(ContentMin);
		}
		if ( ContentMax != null ) {
			__ContentMax_JTextField.setText(ContentMax);
		}
		if ( ReleaseMax != null ) {
			__ReleaseMax_JTextField.setText(ReleaseMax);
		}
		if ( DeadStorage != null ) {
			__DeadStorage_JTextField.setText(DeadStorage);
		}
		if ( AccountID != null ) {
			__AccountID_JTextField.setText(AccountID);
		}
		if ( AccountName != null ) {
			__AccountName_JTextField.setText(AccountName);
		}
		if ( AccountMax != null ) {
			__AccountMax_JTextField.setText(AccountMax);
		}
		if ( AccountInitial != null ) {
			__AccountInitial_JTextField.setText(AccountInitial);
		}
		if ( AccountEvap == null ) {
			// Select default...
			__AccountEvap_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __AccountEvap_JComboBox, true, " ", 0, 0, AccountEvap, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"AccountEvap value \"" + AccountEvap + "\".  Using the user value.");
				__AccountEvap_JComboBox.setText ( AccountEvap );
			}
		}
		if ( AccountOneFill == null ) {
			// Select default...
			__AccountOneFill_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches (
					__AccountOneFill_JComboBox, true, " ", 0, 0, AccountOneFill, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"AccountOneFill value \"" + AccountOneFill + "\".  Using the user value.");
				__AccountOneFill_JComboBox.setText (
				AccountOneFill );
			}
		}
		if ( EvapStations != null ) {
			__EvapStations_JTextField.setText(EvapStations);
		}
		if ( PrecipStations != null ) {
			__PrecipStations_JTextField.setText(PrecipStations);
		}
		if ( ContentAreaSeepage != null ) {
			// Force 3 values per line - formatting in the standard way (this will throw away a
			// user's manual edits but that is hopefully not an issue)...
			StringBuffer ContentAreaSeepage_buffer = new StringBuffer ();
			List<String> v2 = StringUtil.breakStringList(
				ContentAreaSeepage, ",; \n", StringUtil.DELIM_SKIP_BLANKS);
			int size = 0;
			if ( v2 != null ) {
				size = v2.size();
			}
			for ( int i = 0; i < size; i++ ) {
				if ( (i%3) == 0 ) {
					if ( i != 0 ) {
						// Add a newline to force a line break in the display...
						ContentAreaSeepage_buffer.append ( '\n');
					}
				}
				else if ( ((i - 1)%3 == 0) || ((i - 2)%3 == 0)){
					// Add a comma to separate values...
					ContentAreaSeepage_buffer.append ( ',');
				}
				// Add the item...
				ContentAreaSeepage_buffer.
				append ( v2.get(i) );
			}
			ContentAreaSeepage = ContentAreaSeepage_buffer.toString();
			__ContentAreaSeepage_JTextArea.setText(ContentAreaSeepage);
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
	
	// Regardless, get the values from the interface to check in the command...

	ID = __ID_JTextField.getText().trim();
	Name = __Name_JTextField.getText().trim();
	RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	OneFillRule = StringUtil.getToken( __OneFillRule_JComboBox.getSelected(), " ", 0, 0 );
	if ( OneFillRule == null ) {
		OneFillRule = "";
	}
	DailyID = __DailyID_JTextField.getText().trim();
	ContentMin = __ContentMin_JTextField.getText().trim();
	ContentMax = __ContentMax_JTextField.getText().trim();
	ReleaseMax = __ReleaseMax_JTextField.getText().trim();
	DeadStorage = __DeadStorage_JTextField.getText().trim();
	AccountID = __AccountID_JTextField.getText().trim();
	AccountName = __AccountName_JTextField.getText().trim();
	AccountMax = __AccountMax_JTextField.getText().trim();
	AccountInitial = __AccountInitial_JTextField.getText().trim();
	AccountEvap = StringUtil.getToken( __AccountEvap_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountEvap == null ) {
		AccountEvap = "";
	}
	AccountOneFill = StringUtil.getToken( __AccountOneFill_JComboBox.getSelected(), " ", 0, 0 );
	if ( AccountOneFill == null ) {
		AccountOneFill = "";
	}
	EvapStations = __EvapStations_JTextField.getText().trim();
	PrecipStations = __PrecipStations_JTextField.getText().trim();
	ContentAreaSeepage = __ContentAreaSeepage_JTextArea.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();

	props = new PropList(__command.getCommandName());
	props.add("ID=" + ID);
	props.add( "Name=" + Name );
	props.add( "RiverNodeID=" + RiverNodeID );
	props.add( "OnOff=" + OnOff );
	props.add( "OneFillRule=" + OneFillRule);
	props.add( "DailyID=" + DailyID);
	props.add( "ContentMin=" + ContentMin );
	props.add( "ContentMax=" + ContentMax );
	props.add( "ReleaseMax=" + ReleaseMax );
	props.add( "DeadStorage=" + DeadStorage );
	props.add( "AccountID=" + AccountID );
	props.add( "AccountName=" + AccountName );
	props.add( "AccountMax=" + AccountMax );
	props.add( "AccountInitial=" + AccountInitial );
	props.add( "AccountEvap=" + AccountEvap );
	props.add( "AccountOneFill=" + AccountOneFill );
	props.add( "EvapStations=" + EvapStations );
	props.add( "PrecipStations=" + PrecipStations );
	props.add( "ContentAreaSeepage=" + ContentAreaSeepage);
	props.add( "IfNotFound=" + IfNotFound );
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
