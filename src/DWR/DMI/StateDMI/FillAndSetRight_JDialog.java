// FillAndSetRight_JDialog - Editor for Fill*Right() and Set*Right() commands.

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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_ReservoirRight;

/**
Editor for Fill*Right() and Set*Right() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetRight_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __StationID_JTextField = null;
private JTextField __AdministrationNumber_JTextField = null;
private JTextField __Decree_JTextField = null;
private SimpleJComboBox __OnOff_JComboBox = null;
private SimpleJComboBox __AccountDist_JComboBox = null;
private SimpleJComboBox __RightType_JComboBox = null;
private SimpleJComboBox __FillType_JComboBox = null;
private JTextField __OpRightID_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJComboBox __IfFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private FillAndSetRight_Command __command = null;
private boolean __ok = false; // Has user pressed OK to close the dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetRight_JDialog ( JFrame parent, FillAndSetRight_Command command )
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
	String StationID = __StationID_JTextField.getText().trim();
	String AdministrationNumber = __AdministrationNumber_JTextField.getText().trim();
	String Decree = __Decree_JTextField.getText().trim();
	String OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}

	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( Name.length() > 0 ) {
		parameters.set ( "Name", Name );
	}
	if ( StationID.length() > 0 ) {
		parameters.set ( "StationID", StationID );
	}
    if ( AdministrationNumber.length() > 0 ) {
        parameters.set ( "AdministrationNumber", AdministrationNumber );
    }
    if ( Decree.length() > 0 ) {
        parameters.set ( "Decree", Decree );
    }
	if ( OnOff.length() > 0 ) {
		parameters.set ( "OnOff", OnOff );
	}
	if ( __AccountDist_JComboBox != null ) {
		String AccountDist = StringUtil.getToken(__AccountDist_JComboBox.getSelected(), " ", 0, 0);
		if ( AccountDist == null ) {
			AccountDist = "";
		}
		if ( AccountDist.length() > 0 ) {
			parameters.set ( "AccountDist", AccountDist );
		}
	}
	if ( __RightType_JComboBox != null ) {
		String RightType = StringUtil.getToken(__RightType_JComboBox.getSelected(), " ", 0, 0);
		if ( RightType == null ) {
			RightType = "";
		}
		if ( RightType.length() > 0 ) {
			parameters.set ( "RightType", RightType );
		}
	}
	if ( __FillType_JComboBox != null ) {
		String FillType = StringUtil.getToken(__FillType_JComboBox.getSelected(), " ", 0, 0);
		if ( FillType == null ) {
			FillType = "";
		}
		if ( FillType.length() > 0 ) {
			parameters.set ( "FillType", FillType );
		}
	}
	if ( __OpRightID_JTextField != null ) {
		String OpRightID = __OpRightID_JTextField.getText().trim();
		if ( OpRightID.length() > 0 ) {
			parameters.set ( "OpRightID", OpRightID );
		}
	}
	if ( __IfNotFound_JComboBox != null ) {
		String IfNotFound = __IfNotFound_JComboBox.getSelected();
		if ( IfNotFound.length() > 0 ) {
			parameters.set ( "IfNotFound", IfNotFound );
		}
	}
	if ( __IfFound_JComboBox != null ) {
		String IfFound = __IfFound_JComboBox.getSelected();
		if ( IfFound.length() > 0 ) {
			parameters.set ( "IfFound", IfFound );
		}
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
	String StationID = __StationID_JTextField.getText().trim();
	String AdministrationNumber = __AdministrationNumber_JTextField.getText().trim();
	String Decree = __Decree_JTextField.getText().trim();
	String OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "StationID", StationID );
    __command.setCommandParameter ( "AdministrationNumber", AdministrationNumber );
    __command.setCommandParameter ( "Decree", Decree );
	__command.setCommandParameter ( "OnOff", OnOff );
	if ( __AccountDist_JComboBox != null ) {
		String AccountDist = StringUtil.getToken(__AccountDist_JComboBox.getSelected(), " ", 0, 0);
		if ( AccountDist == null ) {
			AccountDist = "";
		}
		__command.setCommandParameter ( "AccountDist", AccountDist );
	}
	if ( __RightType_JComboBox != null ) {
		String RightType = StringUtil.getToken(__RightType_JComboBox.getSelected(), " ", 0, 0);
		if ( RightType == null ) {
			RightType = "";
		}
		__command.setCommandParameter ( "RightType", RightType );
	}
	if ( __FillType_JComboBox != null ) {
		String FillType = StringUtil.getToken(__FillType_JComboBox.getSelected(), " ", 0, 0);
		if ( FillType == null ) {
			FillType = "";
		}
		__command.setCommandParameter ( "FillType", FillType );
	}
	if ( __OpRightID_JTextField != null ) {
		String OpRightID = __OpRightID_JTextField.getText().trim();
		__command.setCommandParameter ( "OpRightID", OpRightID );
	}
	if ( __IfNotFound_JComboBox != null ) {
		String IfNotFound = __IfNotFound_JComboBox.getSelected();
		__command.setCommandParameter ( "IfNotFound", IfNotFound );
	}
	if ( __IfFound_JComboBox != null ) {
		String IfFound = __IfFound_JComboBox.getSelected();
		__command.setCommandParameter ( "IfFound", IfFound );
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__Name_JTextField = null;
	__OnOff_JComboBox = null;
	__IfFound_JComboBox = null;
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
private void initialize ( JFrame parent, FillAndSetRight_Command command )
{	__command = command;

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
	if ( __command instanceof FillDiversionRight_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in diversion right(s)," + 
		" using the diversion right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetDiversionRight_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in diversion right(s)," + 
		" using the diversion right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillReservoirRight_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in reservoir right(s)," + 
		" using the reservoir right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetReservoirRight_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in reservoir right(s)," + 
		" using the reservoir right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if( __command instanceof FillInstreamFlowRight_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in instream flow right(s)," + 
		" using the instream flow right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetInstreamFlowRight_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in instream flow right(s)," + 
		" using the instream flow right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillWellRight_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in well right(s)," + 
		" using the well right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetWellRight_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in well right(s)," + 
		" using the well right ID to look up the right."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The right ID can contain a * wildcard pattern to match one or more rights."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( (__command instanceof SetDiversionRight_Command)||
		(__command instanceof SetReservoirRight_Command) ||
		(__command instanceof SetInstreamFlowRight_Command) ||
		(__command instanceof SetWellRight_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"If the right ID does not contain a * wildcard pattern and does not match an ID,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"the right will be added if the \"If not found\" option is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"If the right ID does not contain a * wildcard pattern and does match an ID,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"the right will be reset if the \"If found\" option is set to " + __command._Set + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Right ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( (__command instanceof SetDiversionRight_Command)||
    	(__command instanceof SetReservoirRight_Command) ||
    	(__command instanceof SetInstreamFlowRight_Command) ||
    	(__command instanceof SetWellRight_Command) ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the right(s) to set (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
    else {
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the right(s) to fill (use * for wildcard)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Name_JTextField = new JTextField (20);
	__Name_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - up to 24 characters for StateMod."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__StationID_JTextField = new JTextField(10);
	__StationID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __StationID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( (__command instanceof SetDiversionRight_Command)||
    	(__command instanceof SetReservoirRight_Command) ||
    	(__command instanceof SetInstreamFlowRight_Command) ||
    	(__command instanceof SetWellRight_Command) ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - station ID or \"ID\" to match first part of right ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
    else {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - station identifier."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
		
    JGUIUtil.addComponent(main_JPanel, new JLabel("Administration number:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AdministrationNumber_JTextField = new JTextField (10);
	__AdministrationNumber_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AdministrationNumber_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - administration number (priority, smaller is more senior)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Decree amount:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Decree_JTextField = new JTextField(10);
	__Decree_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Decree_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( (__command instanceof SetReservoirRight_Command) ||
		(__command instanceof FillReservoirRight_Command) ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - decree amount, AF."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else {
		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - decree amount, CFS."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("On/Off:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OnOff_JComboBox = new SimpleJComboBox(true);
	List<String> idivsw = StateMod_Diversion.getIdivswChoices(true);
	idivsw.add ( 0, "" );	// Blank to indicate no change
	__OnOff_JComboBox.setData( idivsw );
	__OnOff_JComboBox.addItemListener (this);
	__OnOff_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OnOff_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate on/off, YYYY to start, -YYYY to end in year."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( (__command instanceof FillReservoirRight_Command) ||
		(__command instanceof SetReservoirRight_Command) ) {
		// Need to add some more fields...
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Account served by right:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AccountDist_JComboBox = new SimpleJComboBox(true);
		List<String> iresco = StateMod_ReservoirRight.getIrescoChoices(true);
		iresco.add ( 0, "" ); // Blank to indicate no change
		__AccountDist_JComboBox.setData( iresco );
		__AccountDist_JComboBox.addItemListener (this);
		__AccountDist_JComboBox.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __AccountDist_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - account(s) served by right."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Right type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__RightType_JComboBox = new SimpleJComboBox(true);
		List<String> ityrsr = StateMod_ReservoirRight.getItyrsrChoices( true);
		ityrsr.add ( 0, "" ); // Blank to indicate no change
		__RightType_JComboBox.setData( ityrsr );
		__RightType_JComboBox.addItemListener (this);
		__RightType_JComboBox.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __RightType_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - right type."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Fill type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__FillType_JComboBox = new SimpleJComboBox(true);
		List<String> n2fill = StateMod_ReservoirRight.getN2fillChoices( true);
		n2fill.add ( 0, "" ); // Blank to indicate no change
		__FillType_JComboBox.setData( n2fill );
		__FillType_JComboBox.addItemListener (this);
		__FillType_JComboBox.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __FillType_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - fill type."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Operational rightID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__OpRightID_JTextField = new JTextField (10);
		__OpRightID_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __OpRightID_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - specify only if right type is -1"),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	// For both fill and set...

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IfNotFound_List = new Vector<String>();
    IfNotFound_List.add ( "" );
    if ( (__command instanceof SetDiversionRight_Command)||
    	(__command instanceof SetReservoirRight_Command) ||
    	(__command instanceof SetInstreamFlowRight_Command) ||
    	(__command instanceof SetWellRight_Command) ) {
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

    if ( (__command instanceof SetDiversionRight_Command)||
    	(__command instanceof SetReservoirRight_Command) ||
    	(__command instanceof SetInstreamFlowRight_Command) ||
    	(__command instanceof SetWellRight_Command) ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ("If found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    List<String> IfFound_List = new Vector<String>();
	    IfFound_List.add ( "" );
	    IfFound_List.add ( __command._Set );
	    IfFound_List.add ( __command._Ignore );
	    IfFound_List.add ( __command._Warn );
	    IfFound_List.add ( __command._Fail );
	    __IfFound_JComboBox = new SimpleJComboBox(false);
	    __IfFound_JComboBox.setData( IfFound_List );
	    __IfFound_JComboBox.addItemListener (this);
	    JGUIUtil.addComponent(main_JPanel, __IfFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if match is found (default=" + __command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }

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
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	__error_wait = false;
	String routine = __command + "_JDialog.refresh";
	String ID = "*";
	String Name = "";
	String StationID = "";
	String AdministrationNumber = "";
	String Decree = "";
	String OnOff = "";
	String AccountDist = "";
	String RightType = "";
	String FillType = "";
	String OpRightID = "";
	String IfNotFound = "";
	String IfFound = "";
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		ID = parameters.getValue ( "ID" );
		Name = parameters.getValue ( "Name" );
		StationID = parameters.getValue ( "StationID" );
		AdministrationNumber = parameters.getValue ( "AdministrationNumber");
		Decree = parameters.getValue ( "Decree" );
		OnOff = parameters.getValue ( "OnOff" );
		AccountDist = parameters.getValue ( "AccountDist" );
		RightType = parameters.getValue ( "RightType" );
		FillType = parameters.getValue ( "FillType" );
		OpRightID = parameters.getValue ( "OpRightID" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		IfFound = parameters.getValue ( "IfFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( Name != null ) {
			__Name_JTextField.setText(Name);
		}
		if ( StationID != null ) {
			__StationID_JTextField.setText(
			StationID);
		}
		if ( AdministrationNumber != null ) {
			__AdministrationNumber_JTextField.setText(
			AdministrationNumber);
		}
		if ( Decree != null ) {
			__Decree_JTextField.setText(Decree);
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
		if ( __AccountDist_JComboBox != null ) {
			if ( AccountDist == null ) {
				// Select default...
				__AccountDist_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches ( __AccountDist_JComboBox,
						true, " ", 0, 0, AccountDist, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine,
					"Existing command references an unrecognized\n" +
					"AccountDist value \"" + AccountDist + "\".  Using the user value.");
					__AccountDist_JComboBox.setText ( AccountDist );
				}
			}
		}
		if ( __RightType_JComboBox != null ) {
			if ( RightType == null ) {
				// Select default...
				__RightType_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches ( __RightType_JComboBox,
						true, " ", 0, 0, RightType, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine,
					"Existing command references an unrecognized\n" +
					"RightType value \"" + RightType + "\".  Using the user value.");
					__RightType_JComboBox.setText ( RightType );
				}
			}
		}
		if ( __FillType_JComboBox != null ) {
			if ( FillType == null ) {
				// Select default...
				__FillType_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches ( __FillType_JComboBox,
						true, " ", 0, 0, FillType, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine,
					"Existing commnd references an unrecognized\n" +
					"FillType value \"" + FillType + "\".  Using the user value.");
					__FillType_JComboBox.setText ( FillType );
				}
			}
		}
		if ( __OpRightID_JTextField != null ) {
			if ( OpRightID != null ) {
				__OpRightID_JTextField.setText(OpRightID);
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
		if ( __IfFound_JComboBox != null ) {
			if ( IfFound == null ) {
				// Select default...
				__IfFound_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IfFound_JComboBox, IfFound, JGUIUtil.NONE, null, null ) ) {
					__IfFound_JComboBox.select ( IfFound );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nIfFound value \""+
					IfFound + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}

	parameters = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	parameters.add ( "ID=" + ID );
	Name = __Name_JTextField.getText().trim();
	parameters.add ( "Name=" + Name );
	StationID = __StationID_JTextField.getText().trim();
	parameters.add ( "StationID=" + StationID );
	AdministrationNumber = __AdministrationNumber_JTextField.getText().trim();
	parameters.add ( "AdministrationNumber=" + AdministrationNumber );
	Decree = __Decree_JTextField.getText().trim();
	parameters.add ( "Decree=" + Decree );
	OnOff = StringUtil.getToken(__OnOff_JComboBox.getSelected(), " ", 0, 0);
	parameters.add ( "OnOff=" + OnOff );
	if ( __AccountDist_JComboBox != null ) {
		AccountDist = StringUtil.getToken(__AccountDist_JComboBox.getSelected(), " ", 0, 0);
		parameters.add ( "AccountDist=" + AccountDist );
	}
	if ( __RightType_JComboBox != null ) {
		RightType = StringUtil.getToken(__RightType_JComboBox.getSelected(), " ", 0, 0);
		parameters.add ( "RightType=" + RightType );
	}
	if ( __FillType_JComboBox != null ) {
		FillType = StringUtil.getToken(__FillType_JComboBox.getSelected(), " ", 0, 0);
		parameters.add ( "FillType=" + FillType );
	}
	if ( __OpRightID_JTextField != null ) {
		OpRightID = __OpRightID_JTextField.getText().trim();
		parameters.add ( "OpRightID=" + OpRightID );
	}
	if ( __IfNotFound_JComboBox != null ) {
		IfNotFound = __IfNotFound_JComboBox.getSelected();
		parameters.add ( "IfNotFound=" + IfNotFound );
	}
	if ( __IfFound_JComboBox != null ) {
		IfFound = __IfFound_JComboBox.getSelected();
		parameters.add ( "IfFound=" + IfFound );
	}
	__command_JTextArea.setText( __command.toString ( parameters ) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
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
