// FillAndSetDiversionAndWellStation_JDialog - Editor for FillDiversionStation(), SetDiversionStation(), FillWellStation() and SetWellStation() commands.

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
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Well;

/**
Editor for FillDiversionStation(), SetDiversionStation(), FillWellStation() and SetWellStation() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetDiversionAndWellStation_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __RiverNodeID_JTextField = null;
private SimpleJComboBox __OnOff_JComboBox = null;
private JTextField __Capacity_JTextField = null;
private SimpleJComboBox __ReplaceResOption_JComboBox = null;
private JTextField __DailyID_JTextField = null;
private SimpleJComboBox __AdminNumShift_JComboBox = null;	// For wells
private JTextField __UserName_JTextField = null;
private JTextField __DiversionID_JTextField = null;// For wells
private SimpleJComboBox __DemandType_JComboBox = null;
private JTextField __IrrigatedAcres_JTextField = null;
private SimpleJComboBox __UseType_JComboBox = null;
private SimpleJComboBox __DemandSource_JComboBox = null;
private JTextField __EffAnnual_JTextField = null;
private JTextField __EffMonthly_JTextField = null;
private JTextField __Returns_JTextField = null;
private JTextField __Depletions_JTextField = null;	// For wells
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private FillAndSetDiversionAndWellStation_Command __command = null;
private boolean __ok = false; // Has user pressed OK to close the dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetDiversionAndWellStation_JDialog ( JFrame parent, Command command )
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
	String Capacity = __Capacity_JTextField.getText().trim();
	String ReplaceResOption = "";
	if ( __ReplaceResOption_JComboBox != null ) {
		ReplaceResOption = StringUtil.getToken( __ReplaceResOption_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( ReplaceResOption == null ) {
		ReplaceResOption = "";
	}
	String DailyID = __DailyID_JTextField.getText().trim();
	String AdminNumShift = "";
	if ( __AdminNumShift_JComboBox != null ) {
		AdminNumShift = StringUtil.getToken( __AdminNumShift_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( AdminNumShift == null ) {
		AdminNumShift = "";
	}
	String DiversionID = "";
	if ( __DiversionID_JTextField != null ) {
		DiversionID = __DiversionID_JTextField.getText().trim();
	}
	String UserName = "";
	if ( __UserName_JTextField != null ) {
		UserName = __UserName_JTextField.getText().trim();
	}
	String DemandType = StringUtil.getToken(__DemandType_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandType == null ) {
		DemandType = "";
	}
	String IrrigatedAcres = __IrrigatedAcres_JTextField.getText().trim();
	String UseType = StringUtil.getToken(__UseType_JComboBox.getSelected(), " ", 0, 0 );
	if ( UseType == null ) {
		UseType = "";
	}
	String DemandSource = StringUtil.getToken(__DemandSource_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandSource == null ) {
		DemandSource = "";
	}
	String EffAnnual = __EffAnnual_JTextField.getText().trim();
	String EffMonthly = __EffMonthly_JTextField.getText().trim();
	String Returns = __Returns_JTextField.getText().trim();
	String Depletions = "";
	if ( __Depletions_JTextField != null ) {
		Depletions = __Depletions_JTextField.getText().trim();
	}
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
    if ( Capacity.length() > 0 ) {
        parameters.set ( "Capacity", Capacity );
    }
    if ( ReplaceResOption.length() > 0 ) {
        parameters.set ( "ReplaceResOption", ReplaceResOption );
    }
    if ( DailyID.length() > 0 ) {
        parameters.set ( "DailyID", DailyID );
    }
    if ( AdminNumShift.length() > 0 ) {
        parameters.set ( "AdminNumShift", AdminNumShift );
    }
    if ( DiversionID.length() > 0 ) {
        parameters.set ( "DiversionID", DiversionID );
    }
    if ( UserName.length() > 0 ) {
        parameters.set ( "UserName", UserName );
    }
    if ( DemandType.length() > 0 ) {
        parameters.set ( "DemandType", DemandType );
    }
    if ( IrrigatedAcres.length() > 0 ) {
        parameters.set ( "IrrigatedAcres", IrrigatedAcres );
    }
    if ( UseType.length() > 0 ) {
        parameters.set ( "UseType", UseType );
    }
    if ( DemandSource.length() > 0 ) {
        parameters.set ( "DemandSource", DemandSource );
    }
    if ( EffAnnual.length() > 0 ) {
        parameters.set ( "EffAnnual", EffAnnual );
    }
    if ( EffMonthly.length() > 0 ) {
        parameters.set ( "EffMonthly", EffMonthly );
    }
    if ( Returns.length() > 0 ) {
        parameters.set ( "Returns", Returns );
    }
    if ( Depletions.length() > 0 ) {
        parameters.set ( "Depletions", Depletions );
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
	String Capacity = __Capacity_JTextField.getText().trim();
	String ReplaceResOption = "";
	if ( __ReplaceResOption_JComboBox != null ) {
		ReplaceResOption = StringUtil.getToken( __ReplaceResOption_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( ReplaceResOption == null ) {
		ReplaceResOption = "";
	}
	String DailyID = __DailyID_JTextField.getText().trim();
	String AdminNumShift = "";
	if ( __AdminNumShift_JComboBox != null ) {
		AdminNumShift = StringUtil.getToken( __AdminNumShift_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( AdminNumShift == null ) {
		AdminNumShift = "";
	}
	String DiversionID = "";
	if ( __DiversionID_JTextField != null ) {
		DiversionID = __DiversionID_JTextField.getText().trim();
	}
	String UserName = "";
	if ( __UserName_JTextField != null ) {
		UserName = __UserName_JTextField.getText().trim();
	}
	String DemandType = StringUtil.getToken(__DemandType_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandType == null ) {
		DemandType = "";
	}
	String IrrigatedAcres = __IrrigatedAcres_JTextField.getText().trim();
	String UseType = StringUtil.getToken(__UseType_JComboBox.getSelected(), " ", 0, 0 );
	if ( UseType == null ) {
		UseType = "";
	}
	String DemandSource = StringUtil.getToken(__DemandSource_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandSource == null ) {
		DemandSource = "";
	}
	String EffAnnual = __EffAnnual_JTextField.getText().trim();
	String EffMonthly = __EffMonthly_JTextField.getText().trim();
	String Returns = __Returns_JTextField.getText().trim();
	String Depletions = "";
	if ( __Depletions_JTextField != null ) {
		Depletions = __Depletions_JTextField.getText().trim();
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "RiverNodeID", RiverNodeID );
	__command.setCommandParameter ( "OnOff", OnOff );
    __command.setCommandParameter ( "Capacity", Capacity );
	if ( __ReplaceResOption_JComboBox != null ) {
		__command.setCommandParameter ( "ReplaceResOption", ReplaceResOption );
	}
    __command.setCommandParameter ( "DailyID", DailyID );
    if ( __AdminNumShift_JComboBox != null ) {
    	 __command.setCommandParameter ( "AdminNumShift", AdminNumShift );
    }
    if ( __DiversionID_JTextField != null ) {
    	__command.setCommandParameter ( "DiversionID", DiversionID );
    }
    if ( __UserName_JTextField != null ) {
    	__command.setCommandParameter ( "UserName", UserName );
    }
    __command.setCommandParameter ( "DemandType", DemandType );
    __command.setCommandParameter ( "IrrigatedAcres", IrrigatedAcres );
    __command.setCommandParameter ( "UseType", UseType );
    __command.setCommandParameter ( "DemandSource", DemandSource );
    __command.setCommandParameter ( "EffAnnual", EffAnnual );
    __command.setCommandParameter ( "EffMonthly", EffMonthly );
    __command.setCommandParameter ( "Returns", Returns );
    if ( __Depletions_JTextField != null ) {
    	__command.setCommandParameter ( "Depletions", Depletions );
    }
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (FillAndSetDiversionAndWellStation_Command)command;

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
	if ( __command instanceof FillDiversionStation_Command ){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in diversion station(s)," + 
		" using the diversion station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if( __command instanceof SetDiversionStation_Command ){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in diversion station(s)," + 
		" using the diversion station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillWellStation_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in well station(s)," + 
		" using the well station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if(__command instanceof SetWellStation_Command){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in well station(s)," + 
		" using the well station ID to look up the location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The station ID can contain a * wildcard pattern to match one or more locations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( (__command instanceof SetDiversionStation_Command) ||
			(__command instanceof SetWellStation_Command)) {
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
	if ( (__command instanceof SetDiversionStation_Command) ||
			(__command instanceof FillDiversionStation_Command) ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Returns should be specified as triplets of location, percent, and return table ID:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof SetWellStation_Command) ||
			(__command instanceof FillWellStation_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Returns and depletions should be specified as triplets of " +
		"location, percent, and return table ID:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel ( "    08123456,50.0,1;08234567,50.0,2"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	if ( (__command instanceof SetDiversionStation_Command) ||
			(__command instanceof FillDiversionStation_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if ( (__command instanceof SetWellStation_Command) ||
			(__command instanceof FillWellStation_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - ID for stations to fill (use * for wildcard)"),
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
	List<String> idivsw = StateMod_Diversion.getIdivswChoices(true);
	idivsw.add ( 0, "" );	// Blank to indicate no change
	__OnOff_JComboBox.setData( idivsw );
	__OnOff_JComboBox.addItemListener (this);
	__OnOff_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OnOff_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (	"Optional - is station on/off in data set?"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Capacity:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Capacity_JTextField = new JTextField (10);
	__Capacity_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Capacity_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( (__command instanceof SetDiversionStation_Command) ||
		(__command instanceof FillDiversionStation_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - diversion capacity, CFS."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else if ( (__command instanceof SetWellStation_Command) ||
		(__command instanceof FillWellStation_Command) ) {
      	JGUIUtil.addComponent(main_JPanel, new JLabel (	"Optional - well capacity, CFS."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	if ( (__command instanceof SetDiversionStation_Command) ||
		(__command instanceof FillDiversionStation_Command) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Replacement Res. Option:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__ReplaceResOption_JComboBox = new SimpleJComboBox ();
		List<String> ireptype = StateMod_Diversion.getIreptypeChoices(true);
		ireptype.add ( 0, "" );
		__ReplaceResOption_JComboBox.setData ( ireptype );
		__ReplaceResOption_JComboBox.addItemListener (this);
		__ReplaceResOption_JComboBox.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ReplaceResOption_JComboBox,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - replacement reservoir option."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Daily ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DailyID_JTextField = new JTextField(10);
	__DailyID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DailyID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - the daily identifier, \"ID\", or StateMod flag)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( (__command instanceof SetWellStation_Command) ||
		(__command instanceof FillWellStation_Command) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Admin Num. Shift:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AdminNumShift_JComboBox = new SimpleJComboBox(true);
		List<String> AdminNumShift = StateMod_Well.getPrimaryChoices(true);
		AdminNumShift.add ( 0, "" ); // Blank to indicate no change
		__AdminNumShift_JComboBox.setData( AdminNumShift );
		__AdminNumShift_JComboBox.addItemListener (this);
		__AdminNumShift_JComboBox.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __AdminNumShift_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - shift for well station right administration number."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DiversionID_JTextField = new JTextField(10);
		__DiversionID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __DiversionID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - diversion station this well supplements (use \"ID\" to use "+
		"the well station ID)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( (__command instanceof SetDiversionStation_Command) ||
		(__command instanceof FillDiversionStation_Command) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("User name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__UserName_JTextField = new JTextField(10);
		__UserName_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __UserName_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - specify the user name."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Demand type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DemandType_JComboBox = new SimpleJComboBox();
	List<String> idvcom = null;
	if ( (__command instanceof SetDiversionStation_Command) ||
		(__command instanceof FillDiversionStation_Command)) {
		idvcom = StateMod_Diversion.getIdvcomChoices ( true );
	}
	else if ( (__command instanceof SetWellStation_Command) ||
		(__command instanceof FillWellStation_Command) ) {
		idvcom = StateMod_Well.getIdvcomwChoices ( true );
	}
	idvcom.add ( 0, "" );
	__DemandType_JComboBox.setData( idvcom );
	__DemandType_JComboBox.addItemListener (this);
	__DemandType_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DemandType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - monthly demand time series type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Irrigated acres:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IrrigatedAcres_JTextField = new JTextField(10);
	__IrrigatedAcres_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __IrrigatedAcres_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - typically for the most recent year."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Use type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__UseType_JComboBox = new SimpleJComboBox();
	List<String> irturn = StateMod_Diversion.getIrturnChoices ( true );
	irturn.add ( 0, "" );
	__UseType_JComboBox.setData( irturn );
	__UseType_JComboBox.addItemListener (this);
	__UseType_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __UseType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - water use type."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Demand source:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DemandSource_JComboBox = new SimpleJComboBox();
	List<String> demsrc = StateMod_Diversion.getDemsrcChoices ( true );
	demsrc.add ( 0, "" );
	__DemandSource_JComboBox.setData( demsrc );
	__DemandSource_JComboBox.addItemListener (this);
	__DemandSource_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DemandSource_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - water demand source."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiency (Annual):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffAnnual_JTextField = new JTextField(10);
	__EffAnnual_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffAnnual_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - annual efficiency, percent (ignore if setting monthly)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Efficiencies (Monthly):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EffMonthly_JTextField = new JTextField(10);
	__EffMonthly_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EffMonthly_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - percent, annual is recomputed as average."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Returns (optional):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Returns_JTextField = new JTextField (10);
	__Returns_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Returns_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	if ( (__command instanceof SetWellStation_Command) ||
		(__command instanceof FillWellStation_Command) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Depletions (optional):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Depletions_JTextField = new JTextField (10);
		__Depletions_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Depletions_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> if_not_found_Vector = new Vector<String>();
    if_not_found_Vector.add ( "" );
    if ((__command instanceof SetDiversionStation_Command) ||
		(__command instanceof SetWellStation_Command) ) {
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
	String ID = "";
	String Name = "";
	String RiverNodeID = "";
	String OnOff = "";
	String Capacity = "";
	String ReplaceResOption = "";
	String DailyID = "";
	String AdminNumShift = "";
	String DiversionID = "";
	String UserName = "";
	String DemandType = "";
	String IrrigatedAcres = "";
	String UseType = "";
	String DemandSource = "";
	String EffAnnual = "";
	String EffMonthly = "";
	String Returns = "";
	String Depletions = "";
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
		Capacity = parameters.getValue ( "Capacity" );
		ReplaceResOption = parameters.getValue ( "ReplaceResOption" );
		DailyID = parameters.getValue ( "DailyID" );
		AdminNumShift = parameters.getValue ( "AdminNumShift" );
		DiversionID = parameters.getValue ( "DiversionID" );
		UserName = parameters.getValue ( "UserName" );
		DemandType = parameters.getValue ( "DemandType" );
		IrrigatedAcres = parameters.getValue ( "IrrigatedAcres" );
		UseType = parameters.getValue ( "UseType" );
		DemandSource = parameters.getValue ( "DemandSource" );
		EffAnnual = parameters.getValue ( "EffAnnual" );
		EffMonthly = parameters.getValue ( "EffMonthly" );
		Returns = parameters.getValue ( "Returns" );
		Depletions = parameters.getValue ( "Depletions" );
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
		if ( Capacity != null ) {
			__Capacity_JTextField.setText(Capacity);
		}
		if ( __ReplaceResOption_JComboBox != null ) {
			if ( ReplaceResOption == null ) {
				// Select default...
				__ReplaceResOption_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches ( __ReplaceResOption_JComboBox,
						true, " ", 0, 0, ReplaceResOption, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine,
					"Existing command references an unrecognized\n" +
					"ReplaceResOption value \"" + ReplaceResOption + "\".  Using the user value.");
					__ReplaceResOption_JComboBox.setText (
					ReplaceResOption );
				}
			}
		}
		if ( DailyID != null ) {
			__DailyID_JTextField.setText(DailyID);
		}
		if ( __AdminNumShift_JComboBox != null ) {
			if ( AdminNumShift == null ) {
				// Select default...
				__AdminNumShift_JComboBox.select ( 0 );
			}
			else {
				try {
					JGUIUtil.selectTokenMatches (
						__AdminNumShift_JComboBox, true," ",0,0, AdminNumShift, null );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
					"AdminNumShift value \"" + AdminNumShift + "\".  Using the user value.");
					__AdminNumShift_JComboBox.setText (
					AdminNumShift );
				}
			}
		}
		if ( __DiversionID_JTextField != null ) {
			if ( DiversionID != null ) {
				__DiversionID_JTextField.setText(DiversionID);
			}
		}
		if ( __UserName_JTextField != null ) {
			if ( UserName != null ) {
				__UserName_JTextField.setText(UserName);
			}
		}
		if ( DemandType == null ) {
			// Select default...
			__DemandType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __DemandType_JComboBox, true, " ", 0, 0, DemandType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"DemandType value \"" + DemandType + "\".  Using the user value.");
				__DemandType_JComboBox.setText ( DemandType );
			}
		}
		if ( IrrigatedAcres != null ) {
			__IrrigatedAcres_JTextField.setText(IrrigatedAcres);
		}
		if ( UseType == null ) {
			// Select default...
			__UseType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __UseType_JComboBox, true, " ", 0, 0, UseType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"UseType value \"" + UseType + "\".  Using the user value.");
				__UseType_JComboBox.setText ( UseType );
			}
		}
		if ( DemandSource == null ) {
			// Select default...
			__DemandSource_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches (
					__DemandSource_JComboBox, true, " ", 0, 0, DemandSource, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"DemandSource value \"" + DemandSource + "\".  Using the user value.");
				__DemandSource_JComboBox.setText ( DemandSource );
			}
		}
		if ( EffAnnual != null ) {
			__EffAnnual_JTextField.setText(EffAnnual);
		}
		if ( EffMonthly != null ) {
			__EffMonthly_JTextField.setText(EffMonthly);
		}
		if ( Returns != null ) {
			__Returns_JTextField.setText(Returns);
		}
		if ( __Depletions_JTextField != null ) {
			if ( Depletions != null ) {
				__Depletions_JTextField.setText(Depletions);
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

	ID = __ID_JTextField.getText().trim();
	Name = __Name_JTextField.getText().trim();
	RiverNodeID = __RiverNodeID_JTextField.getText().trim();
	OnOff = StringUtil.getToken( __OnOff_JComboBox.getSelected(), " ", 0, 0 );
	if ( OnOff == null ) {
		OnOff = "";
	}
	Capacity = __Capacity_JTextField.getText().trim();
	if ( __ReplaceResOption_JComboBox != null ) {
		ReplaceResOption = StringUtil.getToken( __ReplaceResOption_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( ReplaceResOption == null ) {
		ReplaceResOption = "";
	}
	DailyID = __DailyID_JTextField.getText().trim();
	if ( __AdminNumShift_JComboBox != null ) {
		AdminNumShift = StringUtil.getToken( __AdminNumShift_JComboBox.getSelected(), " ", 0, 0 );
	}
	if ( AdminNumShift == null ) {
		AdminNumShift = "";
	}
	if ( __DiversionID_JTextField != null ) {
		DiversionID = __DiversionID_JTextField.getText().trim();
	}
	if ( __UserName_JTextField != null ) {
		UserName = __UserName_JTextField.getText().trim();
	}
	DemandType = StringUtil.getToken(__DemandType_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandType == null ) {
		DemandType = "";
	}
	IrrigatedAcres = __IrrigatedAcres_JTextField.getText().trim();
	UseType = StringUtil.getToken(__UseType_JComboBox.getSelected(), " ", 0, 0 );
	if ( UseType == null ) {
		UseType = "";
	}
	DemandSource = StringUtil.getToken(__DemandSource_JComboBox.getSelected(), " ", 0, 0 );
	if ( DemandSource == null ) {
		DemandSource = "";
	}
	EffAnnual = __EffAnnual_JTextField.getText().trim();
	EffMonthly = __EffMonthly_JTextField.getText().trim();
	Returns = __Returns_JTextField.getText().trim();
	if ( __Depletions_JTextField != null ) {
		Depletions = __Depletions_JTextField.getText().trim();
	}
	IfNotFound = __IfNotFound_JComboBox.getSelected();

	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "ID=" + ID );
	parameters.add ( "Name=" + Name );
    parameters.add ( "RiverNodeID=" + RiverNodeID );
    parameters.add ( "OnOff=" + OnOff );
    parameters.add ( "Capacity=" + Capacity );
    if ( __ReplaceResOption_JComboBox != null ) {
    	parameters.add ( "ReplaceResOption=" + ReplaceResOption );
    }
    parameters.add ( "DailyID=" + DailyID );
    if ( __AdminNumShift_JComboBox != null ) {
    	parameters.add ( "AdminNumShift=" + AdminNumShift );
    }
    if ( __DiversionID_JTextField != null ) {
    	parameters.add ( "DiversionID=" + DiversionID );
    }
    if ( __UserName_JTextField != null ) {
    	parameters.add ( "UserName=" + UserName );
    }
    parameters.add ( "DemandType=" + DemandType );
    parameters.add ( "IrrigatedAcres=" + IrrigatedAcres );
    parameters.add ( "UseType=" + UseType );
    parameters.add ( "DemandType=" + DemandType );
    parameters.add ( "DemandSource=" + DemandSource );
    parameters.add ( "EffAnnual=" + EffAnnual );
    parameters.add ( "EffMonthly=" + EffMonthly );
    parameters.add ( "Returns=" + Returns );
    if ( __Depletions_JTextField != null ) {
    	parameters.add ( "Depletions=" + Depletions );
    }
    parameters.add ( "IfNotFound=" + IfNotFound );
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
