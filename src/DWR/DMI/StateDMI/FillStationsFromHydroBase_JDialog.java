// FillStationsFromHydroBase_JDialog - Editor for Fill*StationsFromHydroBase() commands.

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Fill*StationsFromHydroBase() commands.
*/
public class FillStationsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

// Used with DefineRightHow
private final String __EarliestDate = "EarliestDate";
private final String __LatestDate = "LatestDate";
private final String __RightIfAvailable = "RightIfAvailable";

/**
Used with CheckStructures
*/
private final String __False = "False";
private final String __True = "True";

/**
Used with NameFormat
*/
private final String __StationName = "StationName";
private final String __StationName_NodeType = "StationName_NodeType";

/**
Values for the IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextField __ID_JTextField=null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __NameFormat_JComboBox = null;
private SimpleJComboBox __CheckStructures_JComboBox = null;
private JTextField __Year_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextField __AdminNumClasses_JTextField=null;
private JTextField __DefaultAppropriationDate_JTextField = null;
private SimpleJComboBox __DefineRightHow_JComboBox = null;
private SimpleJComboBox __ReadWellRights_JComboBox = null;
private SimpleJComboBox __UseApex_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillStationsFromHydroBase_JDialog ( JFrame parent, Command command )
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
	String NameFormat = "";
	if ( __NameFormat_JComboBox != null ) {
		NameFormat = (String)__NameFormat_JComboBox.getSelected();
	}
	String CheckStructures = "";
	if ( __CheckStructures_JComboBox != null ) {
		CheckStructures = (String)__CheckStructures_JComboBox.getSelected();
	}
	String Year = "";
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
	}
	String Div = "";
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
	}
	String AdminNumClasses = "";
	if ( __AdminNumClasses_JTextField != null ) {
		AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	}
	String DefaultAppropriationDate = "";
	if ( __DefaultAppropriationDate_JTextField != null ) {
		DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	}
	String DefineRightHow = "";
	if ( __DefineRightHow_JComboBox != null ) {
		DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	}
	String ReadWellRights = "";
	if ( __ReadWellRights_JComboBox != null ) {
		ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	}
	String UseApex = "";
	if ( __UseApex_JComboBox != null ) {
		UseApex = __UseApex_JComboBox.getSelected();
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( NameFormat.length() > 0 ) {
		parameters.set ( "NameFormat", NameFormat );
	}
    if ( CheckStructures.length() > 0 ) {
        parameters.set ( "RiverNodeID", CheckStructures );
    }
	if ( Year.length() > 0 ) {
		parameters.set ( "Year", Year );
	}
    if ( Div.length() > 0 ) {
        parameters.set ( "Div", Div );
    }
    if ( AdminNumClasses.length() > 0 ) {
        parameters.set ( "AdminNumClasses", AdminNumClasses );
    }
    if ( DefaultAppropriationDate.length() > 0 ) {
        parameters.set ( "DefaultAppropriationDate", DefaultAppropriationDate );
    }
    if ( DefineRightHow.length() > 0 ) {
        parameters.set ( "DefineRightHow", DefineRightHow );
    }
    if ( ReadWellRights.length() > 0 ) {
        parameters.set ( "ReadWellRights", ReadWellRights );
    }
    if ( UseApex.length() > 0 ) {
        parameters.set ( "UseApex", UseApex );
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
	if ( __NameFormat_JComboBox != null ) {
		String NameFormat = (String)__NameFormat_JComboBox.getSelected();
		__command.setCommandParameter ( "NameFormat", NameFormat );
	}
	if ( __CheckStructures_JComboBox != null ) {
		String CheckStructures = (String)__CheckStructures_JComboBox.getSelected();
		__command.setCommandParameter ( "CheckStructures", CheckStructures );
	}
	if ( __Year_JTextField != null ) {
		String Year = __Year_JTextField.getText().trim();
		__command.setCommandParameter ( "Year", Year );
	}
	if ( __Div_JTextField != null ) {
		String Div = __Div_JTextField.getText().trim();
		__command.setCommandParameter ( "Div", Div );
	}
	if ( __AdminNumClasses_JTextField != null ) {
		String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
		__command.setCommandParameter ( "AdminNumClasses", AdminNumClasses );
	}
	if ( __DefaultAppropriationDate_JTextField != null ) {
		String DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
		__command.setCommandParameter ( "DefaultAppropriationDate", DefaultAppropriationDate );
	}
	if ( __DefineRightHow_JComboBox != null ) {
		String DefineRightHow = __DefineRightHow_JComboBox.getSelected();
		__command.setCommandParameter ( "DefineRightHow", DefineRightHow );
	}
	if ( __ReadWellRights_JComboBox != null ) {
		String ReadWellRights = __ReadWellRights_JComboBox.getSelected();
		__command.setCommandParameter ( "ReadWellRights", ReadWellRights );
	}
	if ( __UseApex_JComboBox != null ) {
		String UseApex = __UseApex_JComboBox.getSelected();
		__command.setCommandParameter ( "UseApex", UseApex );
	}
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
	__NameFormat_JComboBox = null;
	__CheckStructures_JComboBox = null;
	__Year_JTextField = null;
	__Div_JTextField = null;
	__AdminNumClasses_JTextField = null;
	__DefaultAppropriationDate_JTextField = null;
	__DefineRightHow_JComboBox = null;
	__ReadWellRights_JComboBox = null;
	__UseApex_JComboBox = null;
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
	if ( __command instanceof FillStreamGageStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in stream gage stations " +
		"by using data from HydroBase, matching the station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillDiversionStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in diversion stations using " +
		"data from HydroBase, matching the diversion station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillReservoirStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in reservoir stations using " +
		"data from HydroBase, matching the reservoir station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillInstreamFlowStationsFromHydroBase_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in instream flow stations using data from HydroBase,"),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	/*
	else if ( __comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in well stations using " +
		"data from HydroBase, matching the well station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	*/
	else if ( __command instanceof FillStreamEstimateStationsFromHydroBase_Command){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in stream estimate " +
		"stations by using data from HydroBase, matching the station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Stream estimate stations are locations other than stream " +
		"gages (diversions, reservoirs, etc.) where flow is estimated."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillRiverNetworkFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in river network data by " +
		"using data from HydroBase, matching the station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The following values from HydroBase are set if missing in a station:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( (__command instanceof FillStreamGageStationsFromHydroBase_Command) ||
		(__command instanceof FillStreamEstimateStationsFromHydroBase_Command)||
		(__command instanceof FillRiverNetworkFromHydroBase_Command)){
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Name - pick a format to use:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
		"         StationName - 24 characters."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
		"         StationName_NodeType - 20 characters + \"_FLO\"."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillDiversionStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Capacity (can be reset when historical time series are processed)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Demand source (checks whether GIS data are available)" ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     User name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Area (first available from GIS, diversion comments, structure TIA)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillReservoirStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Content/area/seepage"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof FillInstreamFlowStationsFromHydroBase_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	/*
	else if ( __comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"   Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"   Capacity (can be reset when historical time series are" +
		" processed)"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"   Demand source (checks whether GIS data are available)" ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"   Area (from parcels)" ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Well aggregates and wells associated with diversion stations "+
		"require the year and division for parcels."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	*/
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the stations to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (__command instanceof FillStreamGageStationsFromHydroBase_Command) ||
		(__command instanceof FillStreamEstimateStationsFromHydroBase_Command)||
		(__command instanceof FillRiverNetworkFromHydroBase_Command)){
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Name format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List formats = new Vector(3);
        formats.add("");
        formats.add(__StationName);
        formats.add(__StationName_NodeType);
        __NameFormat_JComboBox = new SimpleJComboBox (false);
        __NameFormat_JComboBox.setData ( formats );
		__NameFormat_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(main_JPanel, __NameFormat_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - pick the format for the name (default=" + __StationName + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	if ( __command instanceof FillStreamGageStationsFromHydroBase_Command ||
		__command instanceof FillStreamEstimateStationsFromHydroBase_Command ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Check structures?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List v = new Vector(1);
		v.add("");
		v.add(__False);
		v.add(__True);
		__CheckStructures_JComboBox = new SimpleJComboBox (false);
		__CheckStructures_JComboBox.setData ( v );
		__CheckStructures_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(main_JPanel, __CheckStructures_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - check structures in addition to stations (default=" + __False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	/*
	if ( __comp_type == StateMod_DataSet.COMP_WELL_STATIONS ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (	"Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Year_JTextField = new JTextField(10);
		__Year_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the year for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Water Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Div_JTextField = new JTextField(10);
		__Div_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the water division for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

		// REVISIT SAM 2005-10-10 Possibly add later if needed.
        //	JGUIUtil.addComponent(main_JPanel,
		//	new JLabel ("Admin. number classes:"),
		//	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		//__AdminNumClasses_JTextField = new JTextField("",50);
		//__AdminNumClasses_JTextField.addKeyListener (this);
        //	JGUIUtil.addComponent(main_JPanel, __AdminNumClasses_JTextField,
		//	1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Default appropriation date:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__DefaultAppropriationDate_JTextField = new JTextField("",10);
		__DefaultAppropriationDate_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel,
			__DefaultAppropriationDate_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Use if date is not available from right or permit."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Define right how?:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List define_Vector = new Vector(3);
		define_Vector.add ( "" );
		define_Vector.add ( __EarliestDate );
		define_Vector.add ( __LatestDate );
		define_Vector.add ( __RightIfAvailable );
		__DefineRightHow_JComboBox = new SimpleJComboBox(false);
		__DefineRightHow_JComboBox.setData ( define_Vector );
		__DefineRightHow_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel, __DefineRightHow_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Indicate how to define right (default is " +
			"EarliestDate)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Read well rights?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List read_Vector = new Vector(3);
		read_Vector.add ( "" );
		read_Vector.add ( __True );
		read_Vector.add ( __False );
		__ReadWellRights_JComboBox = new SimpleJComboBox(false);
		__ReadWellRights_JComboBox.setData ( read_Vector );
		__ReadWellRights_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel, __ReadWellRights_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
      	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Read well rights (default=True, False=use processed data)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Use Apex?:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List apex_Vector = new Vector(3);
		apex_Vector.add ( "" );
		apex_Vector.add ( __True );
		apex_Vector.add ( __False );
		__UseApex_JComboBox = new SimpleJComboBox(false);
		__UseApex_JComboBox.setData ( apex_Vector );
		__UseApex_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel, __UseApex_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Used when ReadWellRights=True.  Add APEX amount to right amount (default=True)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	*/
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List IfNotFound_List = new Vector();
    IfNotFound_List.add("");
	IfNotFound_List.add ( _Ignore );
	IfNotFound_List.add ( _Warn );
	IfNotFound_List.add ( _Fail );
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	   	"Optional - indicate action if no match is found (default=" + _Warn + ")."),
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
	String NameFormat = "";
	String CheckStructures = "";
	String Year = "";
	String Div = "";
	String AdminNumClasses = "";
	String DefaultAppropriationDate = "";
	String DefineRightHow = "";
	String ReadWellRights = "";
	String UseApex = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		NameFormat = props.getValue ( "NameFormat" );
		CheckStructures = props.getValue ( "CheckStructures" );
		Year = props.getValue ( "Year" );
		Div = props.getValue ( "Div" );
		AdminNumClasses = props.getValue ( "AdminNumClasses" );
		DefaultAppropriationDate = props.getValue (	"DefaultAppropriationDate" );
		DefineRightHow = props.getValue ( "DefineRightHow" );
		ReadWellRights = props.getValue ( "ReadWellRights" );
		UseApex = props.getValue ( "UseApex" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __NameFormat_JComboBox != null ) {
			if ( NameFormat == null ) {
				// Select default...
				__NameFormat_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__NameFormat_JComboBox, NameFormat, JGUIUtil.NONE, null, null ) ) {
					__NameFormat_JComboBox.select ( NameFormat );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nNameFormat value \"" +
					NameFormat + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __CheckStructures_JComboBox != null ) {
			if ( CheckStructures == null ) {
				// Select default...
				__CheckStructures_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__CheckStructures_JComboBox, CheckStructures, JGUIUtil.NONE, null, null ) ) {
					__CheckStructures_JComboBox.select ( CheckStructures );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nCheckStructures " +
					"value \"" + CheckStructures + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( (AdminNumClasses != null) && (__AdminNumClasses_JTextField != null) ) {
			__AdminNumClasses_JTextField.setText(AdminNumClasses);
		}
		if ( (DefaultAppropriationDate != null) && (__DefaultAppropriationDate_JTextField != null) ) {
			__DefaultAppropriationDate_JTextField.setText( DefaultAppropriationDate);
		}
		if ( __DefineRightHow_JComboBox != null ) {
			if ( DefineRightHow == null ) {
				// Select default...
				__DefineRightHow_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__DefineRightHow_JComboBox, DefineRightHow, JGUIUtil.NONE, null, null ) ) {
					__DefineRightHow_JComboBox.select ( DefineRightHow );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid DefineRightHow " +
					"value \"" + DefineRightHow + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __ReadWellRights_JComboBox != null ) {
			if ( ReadWellRights == null ) {
				// Select default...
				__ReadWellRights_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__ReadWellRights_JComboBox, ReadWellRights, JGUIUtil.NONE, null, null ) ) {
					__ReadWellRights_JComboBox.select ( ReadWellRights );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid ReadWellRights " +
					"value \"" + ReadWellRights + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __UseApex_JComboBox != null ) {
			if ( UseApex == null ) {
				// Select default...
				__UseApex_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__UseApex_JComboBox, UseApex, JGUIUtil.NONE, null, null ) ) {
					__UseApex_JComboBox.
					select ( UseApex );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid UseApex " +
					"value \"" + UseApex + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
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

	// Always get the value that is selected...
	props = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	props.add ( "ID=" + ID );
	if ( __NameFormat_JComboBox != null ) {
		NameFormat = (String)__NameFormat_JComboBox.getSelected();
		props.add ( "NameFormat=" + NameFormat );
	}
	if ( __CheckStructures_JComboBox != null ) {
		CheckStructures = (String)__CheckStructures_JComboBox.getSelected();
		props.add ( "CheckStructures=" + CheckStructures );
	}
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
		props.add ( "Year=" + Year );
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
		props.add ( "Div=" + Div );
	}
	if ( __AdminNumClasses_JTextField != null ) {
		AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
		props.add ( "AdminNumClasses=" + AdminNumClasses );
	}
	if ( __DefaultAppropriationDate_JTextField != null ) {
		DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
		props.add ( "DefaultAppropriationDate=" + DefaultAppropriationDate );
	}
	if ( __DefineRightHow_JComboBox != null ) {
		DefineRightHow = __DefineRightHow_JComboBox.getSelected();
		props.add ( "DefineRightHow=" + DefineRightHow );
	}
	if ( __ReadWellRights_JComboBox != null ) {
		ReadWellRights = __ReadWellRights_JComboBox.getSelected();
		props.add ( "ReadWellRights=" + ReadWellRights );
	}
	if ( __UseApex_JComboBox != null ) {
		UseApex = __UseApex_JComboBox.getSelected();
		props.add ( "UseApex=" + UseApex );
	}
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
