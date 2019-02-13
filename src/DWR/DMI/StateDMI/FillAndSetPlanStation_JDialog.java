// FillAndSetPlanStation_JDialog - Editor for FillPlanStation() and SetPlanStation() commands.

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

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import DWR.StateMod.StateMod_Plan;

/**
Editor for FillPlanStation() and SetPlanStation() commands.
*/
public class FillAndSetPlanStation_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __RiverNodeID_JTextField = null;
private SimpleJComboBox __OnOff_JComboBox = null;
private SimpleJComboBox __PlanType_JComboBox = null;
private SimpleJComboBox __EffType_JComboBox = null;
private JTextField __EffMonthly_JTextField = null;
private SimpleJComboBox __ReturnType_JComboBox = null;
private SimpleJComboBox __FailureSwitch_JComboBox = null;
private JTextField __InitialStorage_JTextField = null;
private JTextField __SourceID_JTextField = null;
private JTextField __SourceAccount_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillAndSetPlanStation_Command __command = null;
private boolean __ok = false; // Has user pressed OK to close the dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetPlanStation_JDialog ( JFrame parent, Command command )
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
	String PlanType = StringUtil.getToken(__PlanType_JComboBox.getSelected(), " ", 0, 0 );
	if ( PlanType == null ) {
		PlanType = "";
	}
	String EffType = StringUtil.getToken(__EffType_JComboBox.getSelected(), " ", 0, 0 );
	if ( EffType == null ) {
		EffType = "";
	}
	String EffMonthly = __EffMonthly_JTextField.getText().trim();
	String ReturnType = StringUtil.getToken(__ReturnType_JComboBox.getSelected(), " ", 0, 0 );
	if ( ReturnType == null ) {
		ReturnType = "";
	}
	String FailureSwitch = "";
	if ( __FailureSwitch_JComboBox != null ) {
		FailureSwitch = StringUtil.getToken( __FailureSwitch_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( FailureSwitch == null ) {
		FailureSwitch = "";
	}
	String InitialStorage = __InitialStorage_JTextField.getText().trim();
	String SourceID = "";
	if ( __SourceID_JTextField != null ) {
		SourceID = __SourceID_JTextField.getText().trim();
	}
	String SourceAccount = __SourceAccount_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
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
    if ( PlanType.length() > 0 ) {
        parameters.set ( "PlanType", PlanType );
    }
    if ( EffType.length() > 0 ) {
        parameters.set ( "EffType", EffType );
    }
    if ( EffMonthly.length() > 0 ) {
        parameters.set ( "EffMonthly", EffMonthly );
    }
    if ( ReturnType.length() > 0 ) {
        parameters.set ( "ReturnType", ReturnType );
    }
    if ( FailureSwitch.length() > 0 ) {
        parameters.set ( "FailureSwitch", FailureSwitch );
    }
    if ( InitialStorage.length() > 0 ) {
        parameters.set ( "InitialStorage", InitialStorage );
    }
    if ( SourceAccount.length() > 0 ) {
        parameters.set ( "SourceAccount", SourceAccount );
    }
    if ( SourceID.length() > 0 ) {
        parameters.set ( "SourceID", SourceID );
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
	String Name = __Name_JTextField.getText().trim();
	String RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	String OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	String PlanType = StringUtil.getToken(__PlanType_JComboBox.getSelected(), " ", 0, 0 );
	if ( PlanType == null ) {
		PlanType = "";
	}
	String EffType = StringUtil.getToken(__EffType_JComboBox.getSelected(), " ", 0, 0 );
	if ( EffType == null ) {
		EffType = "";
	}
	String EffMonthly = __EffMonthly_JTextField.getText().trim();
	String ReturnType = StringUtil.getToken(__ReturnType_JComboBox.getSelected(), " ", 0, 0 );
	if ( ReturnType == null ) {
		ReturnType = "";
	}
	String FailureSwitch = "";
	if ( __FailureSwitch_JComboBox != null ) {
		FailureSwitch = StringUtil.getToken( __FailureSwitch_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( FailureSwitch == null ) {
		FailureSwitch = "";
	}
	String InitialStorage = __InitialStorage_JTextField.getText().trim();
	String SourceID = "";
	if ( __SourceID_JTextField != null ) {
		SourceID = __SourceID_JTextField.getText().trim();
	}
	String SourceAccount = __SourceAccount_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "RiverNodeID", RiverNodeID );
	__command.setCommandParameter ( "OnOff", OnOff );
    __command.setCommandParameter ( "PlanType", PlanType );
    __command.setCommandParameter ( "EffType", EffType );
    __command.setCommandParameter ( "EffMonthly", EffMonthly );
    __command.setCommandParameter ( "ReturnType", ReturnType );
	__command.setCommandParameter ( "FailureSwitch", FailureSwitch );
    __command.setCommandParameter ( "InitialStorage", InitialStorage );
    __command.setCommandParameter ( "SourceID", SourceID );
    __command.setCommandParameter ( "SourceAccount", SourceAccount );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__Name_JTextField = null;
	__OnOff_JComboBox = null;
	__FailureSwitch_JComboBox = null;
	__IfNotFound_JComboBox = null;
	__ID_JTextField = null;
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
private void initialize ( JFrame parent, Command command )
{	__command = (FillAndSetPlanStation_Command)command;

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
	if ( __command instanceof FillPlanStation_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in plan station(s)," + 
		" using the plan station ID to look up the location."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if( __command instanceof SetPlanStation_Command ){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in plan station(s)," + 
		" using the plan station ID to look up the location."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The station ID can contain a * wildcard pattern to match one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __command instanceof SetPlanStation_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"If the station ID does not contain a * wildcard pattern and does not match an ID, " +
		"the location will be added if the \"If not found\" parameter is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Monthly efficiencies should be separated by commas, with January first."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Plan station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - ID for stations to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Name_JTextField = new JTextField (20);
	__Name_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - up to 24 characters for StateMod."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("River node ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__RiverNodeID_JTextField = new JTextField(10);
	__RiverNodeID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __RiverNodeID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the river node identifier, or \"ID\" to use the station ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("On/Off:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OnOff_JComboBox = new SimpleJComboBox();
	List<String> onoff = StateMod_Plan.getPonChoices(true);
	onoff.add ( 0, "" );	// Blank to indicate no change
	__OnOff_JComboBox.setData( onoff );
	__OnOff_JComboBox.addItemListener (this);
	__OnOff_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OnOff_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (	"Optional - is station on/off in data set?"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Plan type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PlanType_JComboBox = new SimpleJComboBox();
	List<String> irturn = StateMod_Plan.getIPlnTypChoices ( true );
	irturn.add ( 0, "" );
	__PlanType_JComboBox.setData( irturn );
	__PlanType_JComboBox.setMaximumRowCount(irturn.size() + 1);
	__PlanType_JComboBox.addItemListener (this);
	__PlanType_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PlanType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - plan type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffType_JComboBox = new SimpleJComboBox();
	List<String> peffFlag = StateMod_Plan.getPeffFlagChoices ( true );
	peffFlag.add ( 0, "" );
	__EffType_JComboBox.setData( peffFlag );
	__EffType_JComboBox.addItemListener (this);
	__EffType_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - plan type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiencies (monthly):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffMonthly_JTextField = new JTextField(10);
	__EffMonthly_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffMonthly_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required if EffType=1 - monthly efficiencies, percent."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Return type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ReturnType_JComboBox = new SimpleJComboBox();
	List<String> iPrf = null;
	iPrf = StateMod_Plan.getIPrfChoices ( true );
	iPrf.add ( 0, "" );
	__ReturnType_JComboBox.setData( iPrf );
	__ReturnType_JComboBox.addItemListener (this);
	__ReturnType_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ReturnType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - return flow type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Failure switch:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FailureSwitch_JComboBox = new SimpleJComboBox ();
	List<String> ipfail = StateMod_Plan.getIPfailChoices(true);
	ipfail.add ( 0, "" );
	__FailureSwitch_JComboBox.setData ( ipfail );
	__FailureSwitch_JComboBox.addItemListener (this);
	__FailureSwitch_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FailureSwitch_JComboBox,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - failure switch."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Initial storage:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InitialStorage_JTextField = new JTextField (10);
	__InitialStorage_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InitialStorage_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - initial storage, ACFT."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Source ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SourceID_JTextField = new JTextField(10);
	__SourceID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SourceID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - source ID where plan water becomes available."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Source account:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SourceAccount_JTextField = new JTextField(10);
	__SourceAccount_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __SourceAccount_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the source account."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List if_not_found_Vector = new Vector();
    if_not_found_Vector.add ( "" );
    if ( __command instanceof SetPlanStation_Command ) {
    	if_not_found_Vector.add ( __command._Add );
    }
	if_not_found_Vector.add ( __command._Ignore );
	if_not_found_Vector.add ( __command._Warn );
	if_not_found_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( if_not_found_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if no match is found."),
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
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	__error_wait = false;
	String routine = __command + "_JDialog.refresh";
	String ID = "";
	String Name = "";
	String RiverNodeID = "";
	String OnOff = "";
	String PlanType = "";
	String EffType = "";
	String EffMonthly = "";
	String ReturnType = "";
	String FailureSwitch = "";
	String InitialStorage = "";
	String SourceID = "";
	String SourceAccount = "";
	String IfNotFound = "";
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		ID = parameters.getValue ( "ID" );
		Name = parameters.getValue ( "Name" );
		RiverNodeID = parameters.getValue ( "RiverNodeID" );
		OnOff = parameters.getValue ( "OnOff" );
		PlanType = parameters.getValue ( "PlanType" );
		EffType = parameters.getValue ( "EffType" );
		EffMonthly = parameters.getValue ( "EffMonthly" );
		ReturnType = parameters.getValue ( "ReturnType" );
		FailureSwitch = parameters.getValue ( "FailureSwitch" );
		InitialStorage = parameters.getValue ( "InitialStorage" );
		SourceID = parameters.getValue ( "SourceID" );
		SourceAccount = parameters.getValue ( "SourceAccount" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
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
				Message.printWarning ( 2, routine,
				"Existing command references an unrecognized\n" +
				"OnOff value \"" + OnOff + "\".  Using the user value.");
				__OnOff_JComboBox.setText ( OnOff );
			}
		}
		if ( PlanType == null ) {
			// Select default...
			__PlanType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __PlanType_JComboBox, true, " ", 0, 0, PlanType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"PlanType value \"" + PlanType + "\".  Using the user value.");
				__PlanType_JComboBox.setText ( PlanType );
			}
		}
		if ( EffType == null ) {
			// Select default...
			__EffType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __EffType_JComboBox, true, " ", 0, 0, EffType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"EffType value \"" + EffType + "\".  Using the user value.");
				__EffType_JComboBox.setText ( EffType );
			}
		}
		if ( EffMonthly != null ) {
			__EffMonthly_JTextField.setText(EffMonthly);
		}
		if ( ReturnType == null ) {
			// Select default...
			__ReturnType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __ReturnType_JComboBox, true, " ", 0, 0, ReturnType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"ReturnType value \"" + ReturnType + "\".  Using the user value.");
				__ReturnType_JComboBox.setText ( ReturnType );
			}
		}
		if ( __FailureSwitch_JComboBox != null ) {
			if ( FailureSwitch == null ) {
				// Select default...
				__FailureSwitch_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches ( __FailureSwitch_JComboBox,
						true, " ", 0, 0, FailureSwitch, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine,
					"Existing command references an unrecognized\n" +
					"FailureSwitch value \"" + FailureSwitch + "\".  Using the user value.");
					__FailureSwitch_JComboBox.setText (
					FailureSwitch );
				}
			}
		}
		if ( InitialStorage != null ) {
			__InitialStorage_JTextField.setText(InitialStorage);
		}
		if ( SourceID != null ) {
			__SourceID_JTextField.setText(SourceID);
		}
		if ( SourceAccount != null ) {
			__SourceAccount_JTextField.setText(SourceAccount);
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
	// Get the displayed values and updated the full command text
	ID = __ID_JTextField.getText().trim();
	Name = __Name_JTextField.getText().trim();
	RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	PlanType = StringUtil.getToken(__PlanType_JComboBox.getSelected(), " ", 0, 0 );
	if ( PlanType == null ) {
		PlanType = "";
	}
	EffType = StringUtil.getToken(__EffType_JComboBox.getSelected(), " ", 0, 0 );
	if ( EffType == null ) {
		EffType = "";
	}
	EffMonthly = __EffMonthly_JTextField.getText().trim();
	ReturnType = StringUtil.getToken(__ReturnType_JComboBox.getSelected(), " ", 0, 0 );
	if ( ReturnType == null ) {
		ReturnType = "";
	}
	if ( __FailureSwitch_JComboBox != null ) {
		FailureSwitch = StringUtil.getToken( __FailureSwitch_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( FailureSwitch == null ) {
		FailureSwitch = "";
	}
	InitialStorage = __InitialStorage_JTextField.getText().trim();
	SourceID = __SourceID_JTextField.getText().trim();
	SourceAccount = __SourceAccount_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();

	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "ID=" + ID );
	parameters.add ( "Name=" + Name );
    parameters.add ( "RiverNodeID=" + RiverNodeID );
    parameters.add ( "OnOff=" + OnOff );
    parameters.add ( "PlanType=" + PlanType );
    parameters.add ( "EffType=" + EffType );
    parameters.add ( "EffMonthly=" + EffMonthly );
    parameters.add ( "ReturnType=" + ReturnType );
    parameters.add ( "FailureSwitch=" + FailureSwitch );
    parameters.add ( "InitialStorage=" + InitialStorage );
    parameters.add ( "SourceID=" + SourceID );
    parameters.add ( "SourceAccount=" + SourceAccount );
    parameters.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString ( parameters ) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
and the dialog is closed.
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
