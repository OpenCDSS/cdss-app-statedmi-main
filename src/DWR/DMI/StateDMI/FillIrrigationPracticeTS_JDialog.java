// FillIrrigationPracticeTS_JDialog - Editor for FillIrrigationPracticeTSInterpolate() and FillIrrigationPracticeTSRepeat() commands.

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

import DWR.StateCU.StateCU_IrrigationPracticeTS;

/**
Editor for FillIrrigationPracticeTSInterpolate() and FillIrrigationPracticeTSRepeat() commands.
*/
@SuppressWarnings("serial")
public class FillIrrigationPracticeTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private SimpleJComboBox __DataType_JComboBox = null;
private JTextField __FillStart_JTextField = null;
private JTextField __FillEnd_JTextField = null;
private JTextField __MaxIntervals_JTextField = null;
private SimpleJComboBox __FillDirection_JComboBox = null;
private JTextField __FillFlag_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private FillIrrigationPracticeTS_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillIrrigationPracticeTS_JDialog ( JFrame parent, FillIrrigationPracticeTS_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();
	Object o = event.getSource();

	if (s.equals("Cancel")) {
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
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
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String DataType = __DataType_JComboBox.getSelected();
	String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( FillStart.length() > 0 ) {
		parameters.set ( "FillStart", FillStart );
	}
	if ( FillEnd.length() > 0 ) {
		parameters.set ( "FillEnd", FillEnd );
	}
	if ( DataType.length() > 0 ) {
		parameters.set ( "DataType", DataType );
	}
	if ( MaxIntervals.length() > 0 ) {
		parameters.set ( "MaxIntervals", MaxIntervals );
	}
	if ( IfNotFound.length() > 0 ) {
		parameters.set ( "IfNotFound", IfNotFound );
	}
	
	if ( __FillDirection_JComboBox != null ) {
		String FillDirection = __FillDirection_JComboBox.getSelected();
		if ( FillDirection.length() > 0 ) {
			parameters.set ( "FillDirection", FillDirection );
		}
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		if ( FillFlag.length() > 0 ) {
			parameters.set ( "FillFlag", FillFlag );
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
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String DataType = __DataType_JComboBox.getSelected();
	String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "FillStart", FillStart );
	__command.setCommandParameter ( "FillEnd", FillEnd );
	__command.setCommandParameter ( "DataType", DataType );
	__command.setCommandParameter ( "MaxIntervals", MaxIntervals );
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
	
	if ( __FillDirection_JComboBox != null ) {
		String FillDirection = __FillDirection_JComboBox.getSelected();
		__command.setCommandParameter ( "FillDirection", FillDirection );
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		__command.setCommandParameter ( "FillFlag", FillFlag );
	}
}
	
public void stateChanged(ChangeEvent e)
{	refresh();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, FillIrrigationPracticeTS_Command command )
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
	"This command fills missing data in irrigation practice time series," + 
	" using the CU Location ID, time series data type, and year"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __command instanceof FillIrrigationPracticeTSInterpolate_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are replaced by interpolating between known values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillIrrigationPracticeTSRepeat_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are replaced by repeating known values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Max Intervals indicates the maximum number of intervals to fill in a gap."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The CU Location ID can contain a * wildcard pattern to match one or more time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The fill period can optionally be specified.  Only years in the output period can be filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU Location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU Location(s) to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

  	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Data type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> datatype_Vector = StateCU_IrrigationPracticeTS.getTimeSeriesDataTypes ( false, false );
	datatype_Vector.add ( 0, "" );
	__DataType_JComboBox = new SimpleJComboBox(false);
	__DataType_JComboBox.setData ( datatype_Vector );
	__DataType_JComboBox.addTextFieldKeyListener (this);
	__DataType_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __DataType_JComboBox,
	1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - data type to fill (blank for all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill start (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillStart_JTextField = new JTextField (10);
	__FillStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillStart_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - start year as 4-digits (default=fill all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill end (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillEnd_JTextField = new JTextField (10);
	__FillEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillEnd_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - end year as 4-digits or blank to fill all (default=fill all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( __command instanceof FillIrrigationPracticeTSRepeat_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill direction:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       	List<String> direction_Vector = new Vector<String>();
       	direction_Vector.add ( "" );
		direction_Vector.add ( __command._Backward );
		direction_Vector.add ( __command._Forward );
		__FillDirection_JComboBox = new SimpleJComboBox(false);
		__FillDirection_JComboBox.setData ( direction_Vector );
		__FillDirection_JComboBox.addTextFieldKeyListener (this);
		__FillDirection_JComboBox.addItemListener (this);
       	JGUIUtil.addComponent(main_JPanel, __FillDirection_JComboBox,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel,
       		new JLabel ( "Optional - direction to process data (default=" + __command._Forward + ")."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	if ( __command instanceof FillIrrigationPracticeTSRepeat_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Fill flag:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__FillFlag_JTextField = new JTextField (10);
		__FillFlag_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __FillFlag_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel,
       		new JLabel ("Optional - string to flag filled values (default=no flag)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

   	JGUIUtil.addComponent(main_JPanel, new JLabel("Maximum intervals:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__MaxIntervals_JTextField = new JTextField (10);
	__MaxIntervals_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __MaxIntervals_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - max years to fill (default=all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new Vector<String>();
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
	__command_JTextArea = new JTextArea (4,40);
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
	String routine = "fillIrrigationPracticeTS_JDialog.refresh";
	String ID = "";
	String DataType = "";
	String FillStart = "";
	String FillEnd = "";
	String FillDirection = "";
	String FillFlag = "";
	String MaxIntervals = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		DataType = props.getValue ( "DataType" );
		FillStart = props.getValue ( "FillStart" );
		FillEnd = props.getValue ( "FillEnd" );
		FillDirection = props.getValue ( "FillDirection" );
		FillFlag = props.getValue ( "FillFlag" );
		MaxIntervals = props.getValue ( "MaxIntervals" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText ( ID );
		}
		if ( __DataType_JComboBox != null ) {
			if ( DataType == null ) {
				// Select default...
				__DataType_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__DataType_JComboBox, DataType, JGUIUtil.NONE, null, null ) ) {
					__DataType_JComboBox.select(DataType);
				}
				else {
					Message.printWarning ( 1, routine, "Existing command references an invalid DataType " +
					"value \""+ DataType + "\".  Select a different value or Cancel.");
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
		if ( __FillDirection_JComboBox != null ) {
			if ( FillDirection == null ) {
				// Select default...
				__FillDirection_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__FillDirection_JComboBox, FillDirection, JGUIUtil.NONE, null, null ) ) {
					__FillDirection_JComboBox.select(FillDirection);
				}
				else {
					Message.printWarning ( 1, routine, "Existing command references an invalid " +
					"FillDirection value \""+ FillDirection + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( (__FillFlag_JTextField != null) && (FillFlag != null) ) {
			__FillFlag_JTextField.setText ( FillFlag );
		}
		if ( (__MaxIntervals_JTextField != null) && (MaxIntervals != null) ) {
			__MaxIntervals_JTextField.setText ( MaxIntervals );
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
	DataType = __DataType_JComboBox.getSelected();
	props.add("DataType=" + DataType);
	FillStart = __FillStart_JTextField.getText().trim();
	props.add("FillStart=" + FillStart);
	FillEnd = __FillEnd_JTextField.getText().trim();
	props.add("FillEnd=" + FillEnd);
	if ( __FillDirection_JComboBox != null ) {
		FillDirection = __FillDirection_JComboBox.getSelected();
		props.add("FillDirection=" + FillDirection);
	}
	if ( __FillFlag_JTextField != null ) {
		FillFlag = __FillFlag_JTextField.getText().trim();
		props.add("FillFlag=" + FillFlag);
	}
	MaxIntervals = __MaxIntervals_JTextField.getText().trim();
	props.add("MaxIntervals=" + MaxIntervals);
	IfNotFound = __IfNotFound_JComboBox.getSelected();
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
