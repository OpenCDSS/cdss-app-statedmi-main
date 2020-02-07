// ReadDiversionHistoricalTSFromHydroBase_JDialog - editor for ReadDiversionHistoricalTSFromHydroBase command

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
// readDiversionHistoricalTSFromHydroBase_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
// 2007-02-06	Kurt Tometich, RTi		Initial version for separation
//								of command from the StateDMI processor.
// 								REVISIT: need to move the Daily Diversion
//								code out of here and into a new command.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

@SuppressWarnings("serial")
public class ReadDiversionHistoricalTSMonthlyFromHydroBase_JDialog
extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false;		// Indicates whether OK button
										// has been pressed.
private JTextField __ID_JTextField=null;
private SimpleJComboBox __IncludeExplicit_JComboBox = null;
private SimpleJComboBox __IncludeCollections_JComboBox = null;
private SimpleJComboBox __LEZeroInAverage_JComboBox = null;
private SimpleJComboBox __UseDiversionComments_JComboBox = null;
private SimpleJComboBox __FillUsingCIU_JComboBox = null;
private JTextField __ReadStart_JTextField = null;
private JTextArea __command_JTextArea = null;	//Command as JTextField
private JTextField __ReadEnd_JTextField = null;
private JTextField __PatternID_JTextField = null;
private SimpleJComboBox __FillPatternOrder_JComboBox = null;
private JTextField __PatternFillFlag_JTextField = null;
private SimpleJComboBox __FillAverageOrder_JComboBox = null;
private JTextField __AverageFillFlag_JTextField = null;
private JTextField __FillUsingCIUFlag_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;

private ReadDiversionHistoricalTSMonthlyFromHydroBase_Command __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadDiversionHistoricalTSMonthlyFromHydroBase_JDialog ( JFrame parent,
	ReadDiversionHistoricalTSMonthlyFromHydroBase_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	
	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

/**
Check the input.
*/
private void checkInput ()
{	
	PropList props = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String IncludeExplicit = __IncludeExplicit_JComboBox.getSelected();
	String IncludeCollections = __IncludeCollections_JComboBox.getSelected();
	String ReadStart = __ReadStart_JTextField.getText().trim();
	String ReadEnd = __ReadEnd_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String UseDiversionComments = __UseDiversionComments_JComboBox.getSelected();
	String PatternID = __PatternID_JTextField.getText().trim();
	String FillPatternOrder=__FillPatternOrder_JComboBox.getSelected();
	String PatternFillFlag = __PatternFillFlag_JTextField.getText().trim();
	String FillAverageOrder=__FillAverageOrder_JComboBox.getSelected();
	String AverageFillFlag = __AverageFillFlag_JTextField.getText().trim();
	String FillUsingCIU = __FillUsingCIU_JComboBox.getSelected();
	String FillUsingCIUFlag = __FillUsingCIUFlag_JTextField.getText().trim();
	
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( IncludeExplicit.length() > 0 ) {
		props.set ( "IncludeExplicit", IncludeExplicit );
	}
	if ( IncludeCollections.length() > 0 ) {
		props.set ( "IncludeCollections", IncludeCollections );
	}
	if ( ReadStart.length() > 0 ) {
		props.set ( "ReadStart", ReadStart );
	}
	if ( ReadEnd.length() > 0 ) {
		props.set ( "ReadEnd", ReadEnd );
	}
	if ( LEZeroInAverage.length() > 0 ) {
		props.set ( "LEZeroInAverage", LEZeroInAverage );
	}
	if ( UseDiversionComments.length() > 0 ) {
		props.set ( "UseDiversionComments", UseDiversionComments );
	}
	if ( PatternID.length() > 0 ) {
		props.set ( "PatternID", PatternID );
	}
	if ( FillPatternOrder.length() > 0 ) {
		props.set ( "FillPatternOrder", FillPatternOrder );
	}
	if ( FillAverageOrder.length() > 0 ) {
		props.set ( "FillAverageOrder", FillAverageOrder );
	}
	if ( FillUsingCIU.length() > 0 ) {
		props.set( "FillUsingCIU", FillUsingCIU );
	}
	if ( FillUsingCIUFlag.length() > 0 ) {
		props.set( "FillUsingCIUFlag", FillUsingCIUFlag );
	}
	if ( AverageFillFlag.length() > 0 ) {
		props.set ( "AverageFillFlag", AverageFillFlag );
	}
	if ( PatternFillFlag.length() > 0 ) {
		props.set( "PatternFillFlag", PatternFillFlag );
	}
	__error_wait = false;
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
private void commitEdits ()
{	
	String ID = __ID_JTextField.getText().trim();
	String IncludeExplicit = __IncludeExplicit_JComboBox.getSelected();
	String IncludeCollections = __IncludeCollections_JComboBox.getSelected();
	String ReadStart = __ReadStart_JTextField.getText().trim();
	String ReadEnd = __ReadEnd_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String UseDiversionComments = __UseDiversionComments_JComboBox.getSelected();
	String PatternID = __PatternID_JTextField.getText().trim();
	String FillPatternOrder=__FillPatternOrder_JComboBox.getSelected();
	String PatternFillFlag = __PatternFillFlag_JTextField.getText().trim();
	String FillAverageOrder=__FillAverageOrder_JComboBox.getSelected();
	String AverageFillFlag = __AverageFillFlag_JTextField.getText().trim();
	String FillUsingCIU = __FillUsingCIU_JComboBox.getSelected();
	String FillUsingCIUFlag = __FillUsingCIUFlag_JTextField.getText().trim();
	
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "IncludeExplicit", IncludeExplicit);
	__command.setCommandParameter ( "IncludeCollections", IncludeCollections );
	__command.setCommandParameter ( "ReadStart", ReadStart );
	__command.setCommandParameter ( "ReadEnd", ReadEnd );
	__command.setCommandParameter ( "AverageFillFlag", AverageFillFlag );
	__command.setCommandParameter ( "LEZeroInAverage", LEZeroInAverage );
	__command.setCommandParameter ( "UseDiversionComments", UseDiversionComments );
	__command.setCommandParameter ( "PatternID", PatternID );
	__command.setCommandParameter ( "FillPatternOrder", FillPatternOrder );
	__command.setCommandParameter ( "PatternFillFlag", PatternFillFlag );
	__command.setCommandParameter ( "FillAverageOrder", FillAverageOrder );
	__command.setCommandParameter ( "AverageFillFlag", AverageFillFlag );	
	__command.setCommandParameter ( "FillUsingCIU", FillUsingCIU );
	__command.setCommandParameter ( "FillUsingCIUFlag", FillUsingCIUFlag );
}


/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__IncludeExplicit_JComboBox = null;
	__IncludeCollections_JComboBox = null;
	__LEZeroInAverage_JComboBox = null;
	__UseDiversionComments_JComboBox = null;
	__PatternID_JTextField = null;
	__FillPatternOrder_JComboBox = null;
	__command_JTextArea = null;
	__ok_JButton = null;
	__FillAverageOrder_JComboBox = null;
	__AverageFillFlag_JTextField = null;
	__PatternFillFlag_JTextField = null;
	__FillUsingCIUFlag_JTextField = null;
	__FillUsingCIU_JComboBox = null;
	__cancel_JButton = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command being edited.
*/
private void initialize (	JFrame parent, Command command )
{	
	__command = (ReadDiversionHistoricalTSMonthlyFromHydroBase_Command)command;
	
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
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads diversion historical monthly time series from HydroBase, using " +
		"the diversion station identifiers to find data."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"The available period is read if no period is provided."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Explicit diversion stations and collections (aggregates and" +
		" systems) can be included or can be skipped."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"If data are not found in HydroBase, a time series with " +
		"missing data is added for the station."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"For diversion aggregates/systems, the data are added.  Missing data are ignored."), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Each aggregate/system part time series can each be filled " +
		"using averages.  The filled time series are then added."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
    JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ID_JTextField = new JTextField("*",10);
    __ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - diversion stations to read (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include explicit:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> include_Vector = new Vector<String>(3);
   	include_Vector.add ( "" );
   	include_Vector.add ( __command._False );
   	include_Vector.add ( __command._True );
   	__IncludeExplicit_JComboBox = new SimpleJComboBox(false);
   	__IncludeExplicit_JComboBox.setData ( include_Vector );
   	__IncludeExplicit_JComboBox.addItemListener(this);
   	JGUIUtil.addComponent(main_JPanel,
		__IncludeExplicit_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - process explicit diversion stations? (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include collections:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	include_Vector = new Vector<String>(3);
	include_Vector.add ( "" );
	include_Vector.add ( __command._False );
	include_Vector.add ( __command._True );
	__IncludeCollections_JComboBox = new SimpleJComboBox(false);
	__IncludeCollections_JComboBox.setData ( include_Vector );
	__IncludeCollections_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __IncludeCollections_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - process aggregates/systems? (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
		"Optional - are values <= 0 used in averages (used in filling)? (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Use diversion comments:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> dc_Vector = new Vector<String>(3);
    dc_Vector.add ( "" );
	dc_Vector.add ( __command._False );
	dc_Vector.add ( __command._True );
	__UseDiversionComments_JComboBox = new SimpleJComboBox(false);
	__UseDiversionComments_JComboBox.setData( dc_Vector );
	__UseDiversionComments_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __UseDiversionComments_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - use diversion comments for more zero values? (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Fill using CIU:"), 
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillUsingCIU_JComboBox = new SimpleJComboBox ( false );
	__FillUsingCIU_JComboBox.addItem ( "" );
	__FillUsingCIU_JComboBox.addItem ( __command._False);
	__FillUsingCIU_JComboBox.addItem ( __command._True );
	__FillUsingCIU_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __FillUsingCIU_JComboBox,
    	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
    	"Optional - use currently in use CIU for more zeros? (default=" + __command._False + ")."), 
    	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
     JGUIUtil.addComponent(main_JPanel, new JLabel ( "Fill CIU flag:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __FillUsingCIUFlag_JTextField = new JTextField ( 5 );
    __FillUsingCIUFlag_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __FillUsingCIUFlag_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - 1-character (or \"Auto\") flag to indicate fill (default=none)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Period to read (optional):" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ReadStart_JTextField = new JTextField ( 15 );
	__ReadStart_JTextField.addKeyListener ( this );
	JGUIUtil.addComponent(main_JPanel, __ReadStart_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "to" ), 
		3, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__ReadEnd_JTextField = new JTextField ( 15 );
	__ReadEnd_JTextField.addKeyListener ( this );
	JGUIUtil.addComponent(main_JPanel, __ReadEnd_JTextField,
		4, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel("If filling aggregates/systems:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel("Pattern identifier:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PatternID_JTextField = new JTextField (10);
	__PatternID_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __PatternID_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required if filling with pattern - pattern ID to use for filling aggregates/systems."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill pattern order:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> order_Vector = new Vector<String>(3);
	order_Vector.add ( "" );
	order_Vector.add ( "1" );
	order_Vector.add ( "2" );
	__FillPatternOrder_JComboBox = new SimpleJComboBox(false);
	__FillPatternOrder_JComboBox.setData ( order_Vector );
	__FillPatternOrder_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __FillPatternOrder_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - order to use pattern filling (default=no fill)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel, new JLabel("Pattern fill flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PatternFillFlag_JTextField = new JTextField (10);
	__PatternFillFlag_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __PatternFillFlag_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - 1-character flag to use for filled values, or \"Auto\"."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill average order:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	order_Vector = new Vector<String>(3);
	order_Vector.add ( "" );
	order_Vector.add ( "1" );
	order_Vector.add ( "2" );
	__FillAverageOrder_JComboBox = new SimpleJComboBox(false);
	__FillAverageOrder_JComboBox.setData ( order_Vector );
	__FillAverageOrder_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __FillAverageOrder_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - order to use monthly average filling (default=no fill)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel, new JLabel("Average fill flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AverageFillFlag_JTextField = new JTextField (10);
	__AverageFillFlag_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __AverageFillFlag_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - 1-character flag to use for filled values."),
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
    button_JPanel.add ( __ok_JButton );
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
public void keyPressed (KeyEvent event) {
	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {;}

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
{	
	String routine = __command + "_JDialog.refresh";
	String ID = "";
	String IncludeExplicit = "";
	String IncludeCollections = "";
	String UseDiversionComments = "";
	String ReadStart = "";
	String ReadEnd = "";
	String LEZeroInAverage = "";
	String PatternID = "";
	String FillPatternOrder = "";
	String PatternFillFlag = "";
	String FillAverageOrder = "";
	String AverageFillFlag = "";
	String FillUsingCIU = "";
	String FillUsingCIUFlag = "";
	__error_wait = false;
	PropList props = __command.getCommandParameters();
	
	if (__first_time) {
		__first_time = false;
		// Parse the incoming string and fill the fields...
		ID = props.getValue ( "ID" );
		IncludeExplicit = props.getValue ( "IncludeExplicit" );
		IncludeCollections = props.getValue ( "IncludeCollections" );
		UseDiversionComments = props.getValue ( "UseDiversionComments");
		ReadStart = props.getValue ( "ReadStart");
		ReadEnd = props.getValue ( "ReadEnd");
		LEZeroInAverage = props.getValue ( "LEZeroInAverage");
		PatternID = props.getValue ( "PatternID");
		FillPatternOrder = props.getValue ( "FillPatternOrder");
		PatternFillFlag = props.getValue ( "PatternFillFlag");
		FillAverageOrder = props.getValue ( "FillAverageOrder");
		AverageFillFlag = props.getValue ( "AverageFillFlag");
		FillUsingCIU = props.getValue( "FillUsingCIU" );
		FillUsingCIUFlag = props.getValue( "FillUsingCIUFlag" );
		
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __IncludeExplicit_JComboBox != null ) {
			if ( IncludeExplicit == null ) {
				// Select default...
				__IncludeExplicit_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__IncludeExplicit_JComboBox,IncludeExplicit, JGUIUtil.NONE, null, null)){
					__IncludeExplicit_JComboBox.select ( IncludeExplicit );
				}
				else {
					Message.printWarning ( 1, routine, "Existing command references an IncludeExplicit " +
					"value \"" + IncludeExplicit + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
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
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an IncludeCollections " +
					"value \"" + IncludeCollections + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( LEZeroInAverage == null ) {
			// Select default...
			__LEZeroInAverage_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__LEZeroInAverage_JComboBox, LEZeroInAverage, JGUIUtil.NONE, null, null ) ) {
				__LEZeroInAverage_JComboBox.select ( LEZeroInAverage );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid LEZeroInAverage value \""+
				LEZeroInAverage + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( UseDiversionComments == null ) {
			// Select default...
			__UseDiversionComments_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__UseDiversionComments_JComboBox, UseDiversionComments, JGUIUtil.NONE, null, null ) ) {
				__UseDiversionComments_JComboBox.select ( UseDiversionComments );
			}
			else {
				Message.printWarning ( 1, routine, "Existing commnd references an invalid\n" +
				"UseDiversionComments value \""+
				UseDiversionComments + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ReadStart != null ) {
			__ReadStart_JTextField.setText(ReadStart);
		}
		if ( ReadEnd != null ) {
			__ReadEnd_JTextField.setText(ReadEnd);
		}
		if ( (__PatternID_JTextField != null) && (PatternID != null) ) {
			__PatternID_JTextField.setText ( PatternID );
		}
		if ( FillPatternOrder == null ) {
			// Select default...
			__FillPatternOrder_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__FillPatternOrder_JComboBox, FillPatternOrder, JGUIUtil.NONE, null, null ) ){
				__FillPatternOrder_JComboBox.select ( FillPatternOrder );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an FillPatternOrder value \"" +
				FillPatternOrder + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( PatternFillFlag != null ) {
			__PatternFillFlag_JTextField.setText(PatternFillFlag);
		}
		if ( FillAverageOrder == null ) {
			// Select default...
			__FillAverageOrder_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__FillAverageOrder_JComboBox, FillAverageOrder, JGUIUtil.NONE, null, null ) ){
				__FillAverageOrder_JComboBox.select ( FillAverageOrder );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an FillAverageOrder value \"" +
				FillAverageOrder + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AverageFillFlag != null ) {
			__AverageFillFlag_JTextField.setText(AverageFillFlag);
		}
		if ( FillUsingCIU == null ) {
			// Select default...
			__FillUsingCIU_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__FillUsingCIU_JComboBox, FillUsingCIU, JGUIUtil.NONE, null, null ) ) {
				__FillUsingCIU_JComboBox.select ( FillUsingCIU );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\n" + "FillUsingCIU value \""+
				FillUsingCIU + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( FillUsingCIUFlag != null ) {
			__FillUsingCIUFlag_JTextField.setText ( FillUsingCIUFlag );
		}
	}
	
	// Always get the value that is selected...
	ID = __ID_JTextField.getText().trim();
	if ( __IncludeExplicit_JComboBox != null ) {
		IncludeExplicit = __IncludeExplicit_JComboBox.getSelected();
	}
	else {
		IncludeExplicit = "True";	//default
	}
	if ( __IncludeCollections_JComboBox != null ) {
		IncludeCollections = __IncludeCollections_JComboBox.getSelected();
	}
	else {
		IncludeCollections = "True"; //default
	}
	LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	UseDiversionComments = __UseDiversionComments_JComboBox.getSelected();
	ReadStart = __ReadStart_JTextField.getText().trim();
	ReadEnd = __ReadEnd_JTextField.getText().trim();
	PatternID = __PatternID_JTextField.getText().trim();
	FillPatternOrder=__FillPatternOrder_JComboBox.getSelected();
	PatternFillFlag = __PatternFillFlag_JTextField.getText().trim();
	FillAverageOrder=__FillAverageOrder_JComboBox.getSelected();
	AverageFillFlag = __AverageFillFlag_JTextField.getText().trim();
	FillUsingCIU = __FillUsingCIU_JComboBox.getSelected();
	FillUsingCIUFlag = __FillUsingCIUFlag_JTextField.getText().trim();
	// Add params to the propList
	props = new PropList ( __command.getCommandName() );
	props.add ( "ID=" + ID );
	props.add ( "IncludeExplicit=" + IncludeExplicit );
	props.add ( "IncludeCollection=" + IncludeCollections );
	props.add ( "LEZeroInAverage=" + LEZeroInAverage );
	props.add ( "UseDiversionComments=" + UseDiversionComments );
	props.add ( "ReadStart=" + ReadStart );
	props.add ( "ReadEnd=" + ReadEnd );
	props.add ( "PatternID=" + PatternID);
	props.add ( "FillPatternOrder=" + FillPatternOrder );
	props.add ( "PatternFillFlag=" + PatternFillFlag );
	props.add ( "FillAverageOrder=" + FillAverageOrder );
	props.add ( "AverageFillFlag=" + AverageFillFlag );
	props.add ( "FillUsingCIU=" + FillUsingCIU );
	props.add ( "FillUsingCIUFlag=" + FillUsingCIUFlag );
	
	__command_JTextArea.setText( __command.toString( props ) );
	
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	
	__ok = ok;	// Save to be returned by ok()
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

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
