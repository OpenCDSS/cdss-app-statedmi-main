// FillTS_JDialog - Editor for Fill*TS*() commands.

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
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import DWR.StateMod.StateMod_DataSet;

/**
Editor for Fill*TS*() commands.
*/
@SuppressWarnings("serial")
public class FillTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener,
ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private SimpleJComboBox __IncludeCollections_JComboBox = null;
private JTextField __FillStart_JTextField = null;
private JTextField __FillEnd_JTextField = null;
private JTextField __Constant_JTextField = null;
private JTextField __PatternID_JTextField = null;
private SimpleJComboBox __LEZeroInAverage_JComboBox = null;
private JTextField __FillFlag_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillTS_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillTS_JDialog (	JFrame parent, FillTS_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();

	if (s.equals("Cancel")) {
		response (false);
	}
	else if (s.equals("OK")) {
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
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( __IncludeCollections_JComboBox != null ) {
		String IncludeCollections = __IncludeCollections_JComboBox.getSelected();
	    if ( IncludeCollections.length() > 0 ) {
	        parameters.set ( "IncludeCollections", IncludeCollections );
	    }
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		if ( Constant.length() > 0 ) {
			parameters.set ( "Constant", Constant );
		}
	}
	if ( __PatternID_JTextField != null ) {
		String PatternID = __PatternID_JTextField.getText().trim();
		if ( PatternID.length() > 0 ) {
			parameters.set ( "PatternID", PatternID );
		}
	}
	if ( __LEZeroInAverage_JComboBox != null ) {
		String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	    if ( LEZeroInAverage.length() > 0 ) {
	        parameters.set ( "LEZeroInAverage", LEZeroInAverage );
	    }
	}
	if ( __FillStart_JTextField != null ) {
		String FillStart = __FillStart_JTextField.getText().trim();
		if ( FillStart.length() > 0 ) {
			parameters.set ( "FillStart", FillStart );
		}
	}
	if ( __FillEnd_JTextField != null ) {
		String FillEnd = __FillEnd_JTextField.getText().trim();
		if ( FillEnd.length() > 0 ) {
			parameters.set ( "FillEnd", FillEnd );
		}
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		if ( FillFlag.length() > 0 ) {
			parameters.set ( "FillFlag", FillFlag );
		}
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
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
	__command.setCommandParameter ( "ID", ID );
	if ( __IncludeCollections_JComboBox != null ) {
		String IncludeCollections = __IncludeCollections_JComboBox.getSelected();
	    __command.setCommandParameter ( "IncludeCollections", IncludeCollections );
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		__command.setCommandParameter ( "Constant", Constant );
	}
	if ( __PatternID_JTextField != null ) {
		String PatternID = __PatternID_JTextField.getText().trim();
		__command.setCommandParameter ( "PatternID", PatternID );
	}
	if ( __LEZeroInAverage_JComboBox != null ) {
		String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	    __command.setCommandParameter ( "LEZeroInAverage", LEZeroInAverage );
	}
	if ( __FillStart_JTextField != null ) {
		String FillStart = __FillStart_JTextField.getText().trim();
		__command.setCommandParameter ( "FillStart", FillStart );
	}
	if ( __FillEnd_JTextField != null ) {
		String FillEnd = __FillEnd_JTextField.getText().trim();
		__command.setCommandParameter ( "FillEnd", FillEnd );
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		__command.setCommandParameter ( "FillFlag", FillFlag );
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
public void stateChanged(ChangeEvent e)
{	refresh();
}

/**
Free memory for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__IncludeCollections_JComboBox = null;
	__Constant_JTextField = null;
	__FillStart_JTextField = null;
	__FillEnd_JTextField = null;
	__PatternID_JTextField = null;
	__LEZeroInAverage_JComboBox = null;
	__FillFlag_JTextField = null;
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
private void initialize ( JFrame parent, FillTS_Command command )
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
	int compType = StateMod_DataSet.COMP_UNKNOWN; // Simplify logic below
	if ( (__command instanceof FillDiversionHistoricalTSMonthlyAverage_Command) ||
		(__command instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(__command instanceof FillDiversionHistoricalTSMonthlyPattern_Command)) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in monthly diversion historical time series."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	compType = StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY;
	}
	/* FIXME SAM 2009-02-08 Enable later
	else if ( __comp_type == StateMod_DataSet.COMP_DIVERSION_TS_DAILY ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in monthly diversion " +
		"historical time series."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	*/
	else if ( (__command instanceof FillDiversionDemandTSMonthlyAverage_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyPattern_Command) ) {
   		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in monthly diversion demand time series."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   		compType = StateMod_DataSet.COMP_DEMAND_TS_MONTHLY;
	}
	else if ( (__command instanceof FillWellHistoricalPumpingTSMonthlyAverage_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command)){
   		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in well historical pumping (monthly) time series."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   		compType = StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY;
	}
	else if ( (__command instanceof FillWellDemandTSMonthlyAverage_Command) ||
		(__command instanceof FillWellDemandTSMonthlyConstant_Command) ||
		(__command instanceof FillWellDemandTSMonthlyPattern_Command) ){
   		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in monthly well demand time series."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   		compType = StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY;
	}
	if ( (__command instanceof FillDiversionHistoricalTSMonthlyAverage_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyAverage_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyAverage_Command) ||
		(__command instanceof FillWellDemandTSMonthlyAverage_Command) ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Missing values are replaced with monthly average - average " +
		"values are computed immediately after reading/calculating the data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(__command instanceof FillWellDemandTSMonthlyConstant_Command) ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Missing values are replaced with a constant value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( (__command instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(__command instanceof FillWellDemandTSMonthlyPattern_Command) ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Missing values are replaced with monthly average - average " +
		"values are computed by this command using the pattern data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"One or more ReadPatternFile() commands must be used before this command."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The station ID can contain a * wildcard pattern to match one or more time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Collections (diversion aggregates and systems) may have"+
		" already been filled during initial processing and can be skipped."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	if ( (compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) ||
		(compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) ||
		(compType == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY)||
		(compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY)){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The fill period can optionally be specified.  Only months in the output period can be filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	/*
	else if ( __comp_type == StateMod_DataSet.COMP_DIVERSION_TS_DAILY ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The fill period can optionally be specified.  Only days in the output period can be filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	*/

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) ||
		(compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if((compType == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY)||
		(compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY) ){
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( (compType == StateMod_DataSet.COMP_DIVERSION_TS_MONTHLY) ||
		(compType == StateMod_DataSet.COMP_DEMAND_TS_MONTHLY) ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the diversion stations to fill (use * for wildcard)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else if((compType == StateMod_DataSet.COMP_WELL_PUMPING_TS_MONTHLY)||
		(compType == StateMod_DataSet.COMP_WELL_DEMAND_TS_MONTHLY)){
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the well stations to fill (use * for wildcard)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( (__command instanceof FillDiversionHistoricalTSMonthlyAverage_Command) || 
		(__command instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Include collections:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> include_Vector = new Vector<String>(3);
		include_Vector.add ( "" );
		include_Vector.add ( __command._False );
		include_Vector.add ( __command._True );
		__IncludeCollections_JComboBox = new SimpleJComboBox(false);
		__IncludeCollections_JComboBox.setData ( include_Vector );
		__IncludeCollections_JComboBox.addItemListener(this);
		JGUIUtil.addComponent(main_JPanel,
		__IncludeCollections_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - default=" + __command._True + "."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill start:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillStart_JTextField = new JTextField (10);
	__FillStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillStart_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - start date or blank to fill all."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill end:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillEnd_JTextField = new JTextField (10);
	__FillEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillEnd_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - end date or blank to fill all."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( (__command instanceof FillDiversionHistoricalTSMonthlyConstant_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyConstant_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyConstant_Command) ||
		(__command instanceof FillWellDemandTSMonthlyConstant_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Constant:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Constant_JTextField = new JTextField (10);
		__Constant_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Constant_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - constant value to use for filling."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( (__command instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(__command instanceof FillWellDemandTSMonthlyPattern_Command) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Pattern identifier:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__PatternID_JTextField = new JTextField (10);
		__PatternID_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __PatternID_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - pattern ID to use for filling."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("<= zero values in average?:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> le_Vector = new Vector<String>(3);
		le_Vector.add ( "" );
		le_Vector.add ( __command._False );
		le_Vector.add ( __command._True );
		__LEZeroInAverage_JComboBox = new SimpleJComboBox(false);
		__LEZeroInAverage_JComboBox.setData( le_Vector );
		__LEZeroInAverage_JComboBox.addItemListener (this);
        JGUIUtil.addComponent(main_JPanel, __LEZeroInAverage_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - are values <= zero used in averages (default=" + __command._True + ")."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

   	JGUIUtil.addComponent(main_JPanel, new JLabel( "Fill flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillFlag_JTextField = new JTextField (10);
	__FillFlag_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillFlag_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( (__command instanceof FillDiversionHistoricalTSMonthlyPattern_Command) ||
		(__command instanceof FillDiversionDemandTSMonthlyPattern_Command) ||
		(__command instanceof FillWellHistoricalPumpingTSMonthlyPattern_Command) ||
		(__command instanceof FillWellDemandTSMonthlyPattern_Command) ) {
		// Allow Auto in addition to 1-character
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - 1-character flag to track filled values, or \"Auto\"."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else {
		JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - 1-character flag to track filled values."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> if_not_found_Vector = new Vector<String>(4);
    if_not_found_Vector.add ( "" );
	if_not_found_Vector.add ( __command._Ignore );
	if_not_found_Vector.add ( __command._Warn );
	if_not_found_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( if_not_found_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - indicate action if no match is found (default="+__command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel,
		new JScrollPane(__command_JTextArea),
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

	setTitle("Edit " + __command.getCommandName() + "() Command");
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
		checkInput ();
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
{	__error_wait = false;
	String routine = __command + "_JDialog.refresh";
	String ID = "*";
	String IncludeCollections = "";
	String FillStart = "";
	String FillEnd = "";
	String Constant = "";
	String PatternID = "";
	String LEZeroInAverage = "";
	String FillFlag = "";
	String IfNotFound = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		IncludeCollections = props.getValue ( "IncludeCollections" );
		FillStart = props.getValue ( "FillStart" );
		FillEnd = props.getValue ( "FillEnd" );
		Constant = props.getValue ( "Constant" );
		PatternID = props.getValue ( "PatternID" );
		LEZeroInAverage = props.getValue ( "LEZeroInAverage" );
		FillFlag = props.getValue ( "FillFlag" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText ( ID );
		}
		if ( __IncludeCollections_JComboBox != null ) {
			if ( IncludeCollections == null ) {
				// Select default...
				__IncludeCollections_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IncludeCollections_JComboBox, IncludeCollections, JGUIUtil.NONE, null, null)){
					__IncludeCollections_JComboBox.select ( IncludeCollections );
				}
				else {	Message.printWarning ( 1, routine,
					"Existing command references an invalid IncludeCollections value \"" +
					IncludeCollections + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( FillStart != null ) {
			__FillStart_JTextField.setText ( FillStart );
		}
		if ( FillEnd != null ) {
			__FillEnd_JTextField.setText ( FillEnd );
		}
		if ( (__Constant_JTextField != null) && (Constant != null) ) {
			__Constant_JTextField.setText ( Constant );
		}
		if ( (__PatternID_JTextField != null) && (PatternID != null) ) {
			__PatternID_JTextField.setText ( PatternID );
		}
		if ( __LEZeroInAverage_JComboBox != null ) {
			if ( LEZeroInAverage == null ) {
				// Select default...
				__LEZeroInAverage_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__LEZeroInAverage_JComboBox, LEZeroInAverage, JGUIUtil.NONE, null, null ) ) {
					__LEZeroInAverage_JComboBox.select (LEZeroInAverage );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n" + "LEZeroInAverage value \""+
					LEZeroInAverage + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( FillFlag != null ) {
			__FillFlag_JTextField.setText ( FillFlag );
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
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	ID = __ID_JTextField.getText().trim();
	props.add("ID=" + ID);
	if ( __IncludeCollections_JComboBox != null ) {
		IncludeCollections=__IncludeCollections_JComboBox.getSelected();
		props.add("IncludeCollections=" + IncludeCollections);
	}
	FillStart = __FillStart_JTextField.getText().trim();
	props.add("FillStart=" + FillStart);
	FillEnd = __FillEnd_JTextField.getText().trim();
	props.add("FillEnd=" + FillEnd);
	if ( __Constant_JTextField != null ) {
		Constant = __Constant_JTextField.getText().trim();
		props.add("Constant=" + Constant);
	}
	if ( __PatternID_JTextField != null ) {
		PatternID = __PatternID_JTextField.getText().trim();
		props.add("PatternID=" + PatternID);
	}
	if ( __LEZeroInAverage_JComboBox != null ) {
		LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
		props.add("LEZeroInAverage=" + LEZeroInAverage);
	}
	FillFlag = __FillFlag_JTextField.getText().trim();
	props.add("FillFlag=" + FillFlag);
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add ( "IfNotFound=" + IfNotFound );
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
