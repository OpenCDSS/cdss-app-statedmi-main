// ReadWellRightsFromHydroBase_JDialog - editor for ReadWellRightsFromHydroBase command

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

// ----------------------------------------------------------------------------
// readWellRightsFromHydroBase_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2004-07-07	Steven A. Malers, RTi	Initial version from
//					readDiversionRightsFromHydroBase.
// 2004-09-24	SAM, RTi		Add Div and Year properties.
// 2005-02-28	SAM, RTi		Add DefaultAppropriationDate parameter.
//					Add DefineRightHow parameter.
// 2005-03-07	SAM, RTi		Update to handle the
//					setIrrigationPracticeTSFromHydroBase()
//					command.
// 2005-03-10	SAM, RTi		Add OnOffDefault parameter.
// 2005-03-21	SAM, RTi		Finalize for IPY processing.
// 2005-06-29	SAM, RTi		Add DefineRightHow=LatestDate.
// 2005-10-09	SAM, RTi		Convert the command text field to a text
//					area.
// 2006-03-10	SAM, RTi		Fix note to the right of the year -
//					check was for well stations instead of
//					well rights.
// 2006-04-17	SAM, RTi		Add IDFormat parameter to allow control
//					of water right ID format.
// 2006-10-09	SAM, RTi		Add UseApex and ReadWellRights
//					parameters.
// 2006-10-24	SAM, RTi		Change default note for IDFormat to
//					StationIDW.NN since that is what is
//					actually the default.
// 2007-01-01	SAM, RTi		* Add check for ReadWellRights
//					  consistent with runtime checks.
//		SAM, RTi		* Add check for UseApex
//					  consistent with runtime checks.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// 2007-03-27	SAM, RTi		Add optional InputStart and InputEnd for processing
//					the IPY file.
// 2007-05-17	SAM, RTi		Change UseAPEX to false by default.
//					Require True/False for ReadWellRights and UseApex so it is
//					very clear to users how data are being processed.
// ----------------------------------------------------------------------------
// EndHeader

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
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

import DWR.StateCU.StateCU_DataSet;
import DWR.StateMod.StateMod_DataSet;

@SuppressWarnings("serial")
public class ReadWellRightsFromHydroBaseLegacy_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener,
ChangeListener {

// Possible values for parameters...

private final String __False = "False";
private final String __True = "True";

private final String __EarliestDate = "EarliestDate";
private final String __LatestDate = "LatestDate";
private final String __RightIfAvailable = "RightIfAvailable";

// Formats for well right identifiers...

private final String __HydroBaseID = "HydroBaseID";
private final String __StationIDW_NN = "StationIDW.NN";

private final String __1 = "1";
private final String __Year = "AppropriationDate";

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJComboBox __IDFormat_JComboBox = null;
private JTextField __ID_JTextField=null;
private JTextField __AdminNumClasses_JTextField=null;
private JTextField __Year_JTextField = null;
private JTextField __InputStart_JTextField = null;
private JTextField __InputEnd_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextField __DefaultAppropriationDate_JTextField = null;
private SimpleJComboBox __DefineRightHow_JComboBox = null;
private SimpleJComboBox __ReadWellRights_JComboBox = null;
private SimpleJComboBox __UseApex_JComboBox = null;
private SimpleJComboBox __OnOffDefault_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private List<String> __command_Vector = null;
private String __command = null;
private int __app_type;
private int __comp_type;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
@param app_type Application type.
@param comp_type Component type for the application.
*/
public ReadWellRightsFromHydroBaseLegacy_JDialog (	JFrame parent,
						PropList props,
						List<String> command,
						int app_type,
						int comp_type )
{	super(parent, true);
	initialize (parent, props, command, app_type, comp_type );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response (0);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadWellRightsFromHydroBaseLegacy");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (1);
		}
	}
}

/**
Check the input.  Currently does nothing.
*/
private void checkInput ()
{	String routine = __command + "_JDialog.checkInput";
	String ID = __ID_JTextField.getText().trim();
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	String InputStart = "";
	if ( __InputStart_JTextField != null ) {
		InputStart = __InputStart_JTextField.getText().trim();
	}
	String InputEnd ="";
	if ( __InputEnd_JTextField != null ) {
		InputEnd = __InputEnd_JTextField.getText().trim();
	}
	String DefaultAppropriationDate =
		__DefaultAppropriationDate_JTextField.getText().trim();
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
	String warning = "";
	__error_wait = false;
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory for the file does not exist, warn the
	// user...
	if ( ID.length() == 0 ) {
		warning += "\nAn identifier or pattern must be specified." +
			"  Correct or Cancel.";
	}
	if (	(IDFormat.length() > 0) && (AdminNumClasses.length() > 0) ) {
		warning += "\nIDFormat=HydroBaseID can only be used when " +
			" water right classes are NOT specified.";
	}
	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
		String Year = __Year_JTextField.getText().trim();
		if ( Year.length() == 0 ) {
			warning += "\nThe year must be specified";
		}
		else if ( !StringUtil.isInteger(Year) ) {
			warning += "\nThe year is not an integer.";
		}
	}
	if (	(__app_type == StateDMI.APP_TYPE_STATECU) &&
			(__comp_type == StateCU_DataSet.
					COMP_IRRIGATION_PRACTICE_TS_YEARLY)) {
		if ( (InputStart.length() > 0) && !StringUtil.isInteger(InputStart) ) {
			warning += "\nThe starting year: \"" + InputStart +
				"\" is not a number.\n"+
				"Specify a number or Cancel.";
		}
		if ( (InputEnd.length() > 0) && !StringUtil.isInteger(InputEnd) ) {
			warning += "\nThe ending year: \"" + InputEnd +
				"\" is not a number.\n"+
				"Specify a number or Cancel.";
		}
	}
	String Div = __Div_JTextField.getText().trim();
	if ( Div.length() == 0 ) {
		warning += "\nThe division must be specified";
	}
	else if ( !StringUtil.isInteger(Div) ) {
		warning += "\nThe division is not an integer.";
	}
	if ( DefaultAppropriationDate.length() > 0 ) {
		try {	DateTime datetime1 = DateTime.parse(
			DefaultAppropriationDate);
			if ( datetime1 == null ) {
				throw new Exception ("bad date/time");
			}
		}
		catch (Exception e) {
			warning += "\nDefault appropriation date \"" +
			DefaultAppropriationDate +
			"\" is not a valid date.\n"+
			"Specify a valid date or Cancel.";
		}
	}
	if (	(ReadWellRights.equalsIgnoreCase(__True) ||
		ReadWellRights.equals("")) &&
		!DefineRightHow.equalsIgnoreCase(__RightIfAvailable) ) {
		warning += "\nReadWellRights=True can only be specified when "+
		"DefineRightHow=RightIfAvailable - well rights are expected " +
		"to be found in HydroBase.";
	}
	if (	(UseApex.equalsIgnoreCase(__True) ||
		UseApex.equals("") ) &&
		(!ReadWellRights.equalsIgnoreCase(__True) &&
		!ReadWellRights.equals("") ) ) {
		warning += "\nUseApex=True can only be specified when " +
		"ReadWellRights=True";
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( 1, routine, warning );
		__error_wait = true;
	}
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
	__IDFormat_JComboBox = null;
	__AdminNumClasses_JTextField = null;
	__Year_JTextField = null;
	__InputStart_JTextField = null;
	__InputEnd_JTextField = null;
	__Div_JTextField = null;
	__DefaultAppropriationDate_JTextField = null;
	__DefineRightHow_JComboBox = null;
	__ReadWellRights_JComboBox = null;
	__UseApex_JComboBox = null;
	__OnOffDefault_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command_Vector = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the 
command.
*/
public List<String> getText () {
	if ((__command_Vector != null) && ((__command_Vector.size() == 0) ||
		__command_Vector.get(0).equals(""))) {
		return null;
	}
	return __command_Vector;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param app_PropList Properties from the application.
@param command Vector of String containing the command.
@param app_type Application type.
@param comp_type Component type for the application.
*/
private void initialize (	JFrame parent, PropList props, 
		List<String> command, int app_type, int comp_type )
{	__command_Vector = command;
	__app_type = app_type;
	__comp_type = comp_type;
	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
		__command = "readWellRightsFromHydroBase";
	}
	else if((__app_type == StateDMI.APP_TYPE_STATECU) &&
		(__comp_type == StateCU_DataSet.
		COMP_IRRIGATION_PRACTICE_TS_YEARLY) ) {
		__command = "setIrrigationPracticeTSFromHydroBase";
	}
	String title = "Edit " + __command + "() Command";

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
	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command reads well rights from HydroBase, using "+
			"the well station identifiers to find rights."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"Water rights are determined from derived well right " +
			"and permit data, which have been matched with wells " +
			"and parcels."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"Derived data can be used as is, or well rights can " +
			"be requeried to obtain specific rights and add" +
			"alternate point/exchange values to decrees"),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"(the derived data in HydroBase are summaries based " +
			"only on decrees, not adding APEX)."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"If the well stations contain aggregates, use the " +
			"administration number classes to group rights."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"The output water right identifier by default is " +
			"assigned as the well station identifer + \"W.\"" +
			" + a two digit number."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if((__app_type == StateDMI.APP_TYPE_STATECU) &&
		(__comp_type ==
		StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY) ) {
    		JGUIUtil.addComponent(paragraph, new JLabel (
    		"THIS COMMAND IS OBSOLETE AND IS USED ONLY FOR PHASE 4 RIO GRANDE WORK - " +
    		"INSTEAD, SEE THE readIrrigationPracticeTSFromHydroBase() COMMAND."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"This command sets irrigation practice time series " +
			"data, using data from HydroBase."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"Water rights are determined from derived well right " +
			"and permit data, which have been matched with wells " +
			"and parcels."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"Derived data can be used as is, or well rights can " +
			"be requeried to obtain specific rights and add" +
			"add alternate point/exchange values."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"The data are used to define groundwater acres, " +
			"sprinkler acres, and maximum monthly well pumping."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			"Use the administration number classes to aggregate" +
			" rights (e.g., to match StateMod rights data)."),
			0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Well aggregates and wells associated with diversion stations "+
		"require the year and division for parcels."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Specify administration number classes as administration "+
		"numbers separated by commas."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Well station ID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if((__app_type == StateDMI.APP_TYPE_STATECU) &&
		(__comp_type ==
		StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"CU location ID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the well stations to read (use * for " +
			"wildcard)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	else if((__app_type == StateDMI.APP_TYPE_STATECU) &&
		(__comp_type ==
		StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY) ) {
        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the CU locations to read (use * for " +
			"wildcard)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Right ID format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> format_Vector = new Vector<String>(3);
	format_Vector.add ( "" );
	format_Vector.add ( __HydroBaseID );
	format_Vector.add ( __StationIDW_NN );
	__IDFormat_JComboBox = new SimpleJComboBox(false);
	__IDFormat_JComboBox.setData ( format_Vector );
	__IDFormat_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __IDFormat_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Indicate format for right identifiers (blank=StationIDW.NN)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel,
		new JLabel ("Admin. number classes:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AdminNumClasses_JTextField = new JTextField("",50);
	__AdminNumClasses_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __AdminNumClasses_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
    			(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
    		JGUIUtil.addComponent(main_JPanel, new JLabel ( "Year:"),
    				0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    		__Year_JTextField = new JTextField(10);
			__Year_JTextField.addKeyListener (this);
        	JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Specify the year for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else if((__app_type == StateDMI.APP_TYPE_STATECU) &&
		(__comp_type == StateCU_DataSet.
		COMP_IRRIGATION_PRACTICE_TS_YEARLY) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Input start (year):"),
            	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
            __InputStart_JTextField = new JTextField (10);
            __InputStart_JTextField.addKeyListener (this);
            JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
            	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
                JGUIUtil.addComponent(main_JPanel, new JLabel (
            	"Starting year to read data (blank for full period)."),
            	3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

            JGUIUtil.addComponent(main_JPanel, new JLabel ("Input end (year):"),
            	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
            	__InputEnd_JTextField = new JTextField (10);
            	__InputEnd_JTextField.addKeyListener (this);
            JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
            	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
                JGUIUtil.addComponent(main_JPanel, new JLabel (
            	"Ending year to read data (blank for full period)."),
            	3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Water Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the water division for the parcels."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Default appropriation date:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DefaultAppropriationDate_JTextField = new JTextField("",10);
	__DefaultAppropriationDate_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel,
		__DefaultAppropriationDate_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Use if date is not available from right or permit."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Define right how?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> define_Vector = new Vector<String>(3);
	define_Vector.add ( __EarliestDate );
	define_Vector.add ( __LatestDate );
	define_Vector.add ( __RightIfAvailable );
	__DefineRightHow_JComboBox = new SimpleJComboBox(false);
	__DefineRightHow_JComboBox.setData ( define_Vector );
	__DefineRightHow_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __DefineRightHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Indicate how to define right (default is EarliestDate)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Read well rights?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> read_Vector = new Vector<String>(2);
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

       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Use Apex?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> apex_Vector = new Vector<String>(2);
	apex_Vector.add ( __True );
	apex_Vector.add ( __False );
	__UseApex_JComboBox = new SimpleJComboBox(false);
	__UseApex_JComboBox.setData ( apex_Vector );
	__UseApex_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __UseApex_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Used when ReadWellRights=True.  Add APEX amount to right " +
		"amount (default=True)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if (	(__app_type == StateDMI.APP_TYPE_STATEMOD) &&
		(__comp_type == StateMod_DataSet.COMP_WELL_RIGHTS) ) {
		JGUIUtil.addComponent(main_JPanel, new JLabel (
			"OnOff default:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		List<String> onoff_Vector = new Vector<String>(3);
		onoff_Vector.add ( "" );
		onoff_Vector.add ( __1 );
		onoff_Vector.add ( __Year );
		__OnOffDefault_JComboBox = new SimpleJComboBox(false);
		__OnOffDefault_JComboBox.setData ( onoff_Vector );
		__OnOffDefault_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel, __OnOffDefault_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       		JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Default OnOff switch (default is AppropriationDate)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (6,50);
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
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	if (title != null) {
		setTitle (title);
	}
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
			response (1);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
readWellRightsFromHydroBase(ID="X",IDFormat=X,AdminNumClasses="x,x,x",
Year=X,Div=X,DefaultAppropriationDate="X",DefineRightHow=X,ReadWellRights=X,
UseApex=X,OnOffDefault=X)

readIrrigationPracticeTSFromHydroBase(ID="X",IDFormat=X,
AdminNumClasses="x,x,x",Year=X,Div=X,DefaultAppropriationDate="X",
DefineRightHow=X,ReadWellRights=X,UseApex=X)
</pre>
*/
private void refresh ()
{	String routine = __command + ".refresh";
	String ID = "";
	String IDFormat = "";
	String AdminNumClasses = "";
	String Year = "";
	String InputStart = "";
	String InputEnd = "";
	String Div = "";
	String DefaultAppropriationDate = "";
	String DefineRightHow = "";
	String ReadWellRights = "";
	String UseApex = "";
	String OnOffDefault = "";
	__error_wait = false;
	if (__first_time) {
		__first_time = false;
		// Parse the incoming string and fill the fields...
		List<String> v = StringUtil.breakStringList (
			__command_Vector.get(0).trim(),"()",
			StringUtil.DELIM_SKIP_BLANKS );
		PropList props = null;
		if ( (v != null) && (v.size() > 1) ) {
			props = PropList.parse (
				(String)v.get(1), routine, "," );
		}
		else {	props = new PropList ( routine );
		}
		ID = props.getValue ( "ID" );
		IDFormat = props.getValue ( "IDFormat" );
		AdminNumClasses = props.getValue ( "AdminNumClasses" );
		Year = props.getValue ( "Year" );
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		Div = props.getValue ( "Div" );
		DefaultAppropriationDate = props.getValue (
			"DefaultAppropriationDate" );
		DefineRightHow = props.getValue ( "DefineRightHow" );
		ReadWellRights = props.getValue ( "ReadWellRights" );
		UseApex = props.getValue ( "UseApex" );
		OnOffDefault = props.getValue ( "OnOffDefault" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( IDFormat == null ) {
			// Select default...
			__IDFormat_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__IDFormat_JComboBox,
				IDFormat, JGUIUtil.NONE, null, null ) ) {
				__IDFormat_JComboBox.
				select ( IDFormat );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid IDFormat " +
				"value \"" + IDFormat +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AdminNumClasses != null ) {
			__AdminNumClasses_JTextField.setText(AdminNumClasses);
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( InputStart != null ) {
			__InputStart_JTextField.setText(InputStart);
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText(InputEnd);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( DefaultAppropriationDate != null ) {
			__DefaultAppropriationDate_JTextField.setText(
				DefaultAppropriationDate);
		}
		if ( DefineRightHow == null ) {
			// Select default (True)...
			__DefineRightHow_JComboBox.select ( __True );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__DefineRightHow_JComboBox,
				DefineRightHow, JGUIUtil.NONE, null, null ) ) {
				__DefineRightHow_JComboBox.
				select ( DefineRightHow );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid DefineRightHow " +
				"value \"" + DefineRightHow +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ReadWellRights == null ) {
			// Select default...
			__ReadWellRights_JComboBox.select ( __True );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__ReadWellRights_JComboBox,
				ReadWellRights, JGUIUtil.NONE, null, null ) ) {
				__ReadWellRights_JComboBox.
				select ( ReadWellRights );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid ReadWellRights " +
				"value \"" + ReadWellRights +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( UseApex == null ) {
			// Select default...
			__UseApex_JComboBox.select ( __False );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__UseApex_JComboBox,
				UseApex, JGUIUtil.NONE, null, null ) ) {
				__UseApex_JComboBox.
				select ( UseApex );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid UseApex " +
				"value \"" + UseApex +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( __OnOffDefault_JComboBox != null ) {
		if ( OnOffDefault == null ) {
			// Select default...
			__OnOffDefault_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__OnOffDefault_JComboBox,
				OnOffDefault, JGUIUtil.NONE, null, null ) ) {
				__OnOffDefault_JComboBox.
				select ( OnOffDefault );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing " + __command + "() " +
				"references an invalid OnOffDefault " +
				"value \"" + OnOffDefault +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		}
	}

	// Always get the value that is selected...

	ID = __ID_JTextField.getText().trim();
	IDFormat = __IDFormat_JComboBox.getSelected();
	AdminNumClasses = __AdminNumClasses_JTextField.getText().trim();
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
	}
	if ( __InputStart_JTextField != null ) {
		InputStart = __InputStart_JTextField.getText().trim();
	}
	if ( __InputEnd_JTextField != null ) {
		InputEnd = __InputEnd_JTextField.getText().trim();
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
	}
	DefaultAppropriationDate =
		__DefaultAppropriationDate_JTextField.getText().trim();
	if ( __DefineRightHow_JComboBox != null ) {
		DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	}
	if ( __ReadWellRights_JComboBox != null ) {
		ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	}
	if ( __UseApex_JComboBox != null ) {
		UseApex = __UseApex_JComboBox.getSelected();
	}
	if ( __OnOffDefault_JComboBox != null ) {
		OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	}
	
	StringBuffer b = new StringBuffer ();
	if ( ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( IDFormat.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IDFormat=" + IDFormat );
	}
	if ( AdminNumClasses.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AdminNumClasses=\"" + AdminNumClasses + "\"" );
	}
	if ( (Year != null) && (Year.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Year=" + Year );
	}
	if ( (InputStart != null) && (InputStart.length() > 0) ) {
		if ( InputStart.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "InputStart=" + InputStart  );
		}
	}
	if ( (InputEnd != null) && (InputEnd.length() > 0) ) {
		if ( InputEnd.length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append ( "," );
			}
			b.append ( "InputEnd=" + InputEnd  );
		}
	}
	if ( (Div != null) && (Div.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Div=" + Div );
	}
	if ( DefaultAppropriationDate.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DefaultAppropriationDate=\"" +
			DefaultAppropriationDate + "\"" );
	}
	if ( DefineRightHow.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DefineRightHow=" + DefineRightHow );
	}
	if ( ReadWellRights.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ReadWellRights=" + ReadWellRights );
	}
	if ( UseApex.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "UseApex=" + UseApex );
	}
	if ( (OnOffDefault != null) && (OnOffDefault.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OnOffDefault=" + OnOffDefault );
	}
	__command_JTextArea.setText(
		__command + "(" + b.toString() + ")" );
	__command_Vector.clear();
	__command_Vector.add (__command_JTextArea.getText());
}

/**
Return the time command as a Vector of String.
@return returns the command text or null if no command.
*/
public List<String> response (int status)
{	setVisible(false);
	dispose();
	if (status == 0) {
		// Cancel...
		__command_Vector = null;
		return null;
	}
	else {	refresh();
		if (	(__command_Vector.size() == 0) ||
			__command_Vector.get(0).equals("")) {
			return null;
		}
		return __command_Vector;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response (0);
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
