// SetTSConstant_JDialog - Editor for various Set*TSConstant() commands.

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
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for various Set*TSConstant() commands.
*/
public class SetTSConstant_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextField __ID_JTextField = null; 
private JTextField __MonthValues_JTextField = null;
private JTextField __Constant_JTextField = null;
private JTextField __SetStart_JTextField = null;
private JTextField __SetEnd_JTextField = null;
private SimpleJComboBox __RecalcLimits_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SetTSConstant_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetTSConstant_JDialog ( JFrame parent, SetTSConstant_Command command )
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
    String RecalcLimits = __RecalcLimits_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		if ( Constant.length() > 0 ) {
			parameters.set ( "Constant", Constant );
		}
	}
	if ( __MonthValues_JTextField != null ) {
		String MonthValues = __MonthValues_JTextField.getText().trim();
		if ( MonthValues.length() > 0 ) {
			parameters.set ( "MonthValues", MonthValues );
		}
	}
	if ( __SetStart_JTextField != null ) {
		String SetStart = __SetStart_JTextField.getText().trim();
		if ( SetStart.length() > 0 ) {
			parameters.set ( "SetStart", SetStart );
		}
	}
	if ( __SetEnd_JTextField != null ) {
		String SetEnd = __SetEnd_JTextField.getText().trim();
		if ( SetEnd.length() > 0 ) {
			parameters.set ( "SetEnd", SetEnd );
		}
	}
    if ( RecalcLimits.length() > 0 ) {
    	parameters.set( "RecalcLimits", RecalcLimits );
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
	String RecalcLimits = __RecalcLimits_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
    __command.setCommandParameter ( "RecalcLimits", RecalcLimits );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
	if ( __MonthValues_JTextField != null ) {
		String MonthValues = __MonthValues_JTextField.getText().trim();
		__command.setCommandParameter ( "MonthValues", MonthValues );
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		__command.setCommandParameter ( "Constant", Constant );
	}
	if ( __SetStart_JTextField != null ) {
		String SetStart = __SetStart_JTextField.getText().trim();
		__command.setCommandParameter ( "SetStart", SetStart );
	}
	if ( __SetEnd_JTextField != null ) {
		String SetEnd = __SetEnd_JTextField.getText().trim();
		__command.setCommandParameter ( "SetEnd", SetEnd );
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__MonthValues_JTextField = null;
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
private void initialize (JFrame parent, SetTSConstant_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("Center", main_JPanel);
	int y = 0;

	// Main contents...

	// Now add the buttons...

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
	if ( __command instanceof SetDiversionHistoricalTSMonthlyConstant_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets monthly diversion historical time series data to a constant."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetDiversionDemandTSMonthlyConstant_Command ){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets monthly diversion demand time series data to a constant."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetWellHistoricalPumpingTSMonthlyConstant_Command ){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets monthly well historical pumping time series data to a constant."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SetWellDemandTSMonthlyConstant_Command ){
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets monthly well demand time series data to a constant."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	if ( __command instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets an instream flow demand time series (average monthly) to constant values."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The instream flow station identifier is used to match the time series that is assigned."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);			
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Specify 12 monthly values (January through December) separated by spaces or commas."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	}
	else {
		// TODO SAM 2009-01-28 Evaluate whether parameter should allow recomputing.
		// Single value constants
		JGUIUtil.addComponent(paragraph, new JLabel (
		"The original data limits are recomputed as if the data are historical data.  The " +
		"time series will be created if it does not exist and IfNotFound=" + __command._Add + "."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}	
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (__command instanceof SetDiversionHistoricalTSMonthlyConstant_Command) ||
		(__command instanceof SetDiversionDemandTSMonthlyConstant_Command) ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if((__command instanceof SetWellHistoricalPumpingTSMonthlyConstant_Command)||
		(__command instanceof SetWellDemandTSMonthlyConstant_Command)) {
	   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if ( __command instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
       	JGUIUtil.addComponent(main_JPanel,
		new JLabel ( "Instream flow station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField (10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - identifier of station to process."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    if ( __command instanceof SetInstreamFlowDemandTSAverageMonthlyConstant_Command ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Monthly values"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__MonthValues_JTextField = new JTextField (30);
		__MonthValues_JTextField.addKeyListener (this);
	    JGUIUtil.addComponent(main_JPanel, __MonthValues_JTextField,
			1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required - monthly constant values for Jan-Dec."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
    else {
    	// Single constant and period is allowed  	
    	JGUIUtil.addComponent(main_JPanel, new JLabel("Constant:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Constant_JTextField = new JTextField (10);
		__Constant_JTextField.addKeyListener (this);
   		JGUIUtil.addComponent(main_JPanel, __Constant_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - constant value to use."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    	
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Set start:"),
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__SetStart_JTextField = new JTextField (10);
		__SetStart_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __SetStart_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - start date (default=output period)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Set end:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__SetEnd_JTextField = new JTextField (10);
		__SetEnd_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __SetEnd_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - end date (default=output period)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Recalculate limits:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __RecalcLimits_JComboBox = new SimpleJComboBox ( false );
    __RecalcLimits_JComboBox.addItem ( "" );
    __RecalcLimits_JComboBox.addItem ( __command._False );
    __RecalcLimits_JComboBox.addItem ( __command._True );
    __RecalcLimits_JComboBox.select ( 0 );
    __RecalcLimits_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __RecalcLimits_JComboBox,
    1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Recalculate original data limits after set (default=" +
    	__command._True + ")."), 
    3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List if_not_found_Vector = new Vector();
    if_not_found_Vector.add ( "" );
	if_not_found_Vector.add ( __command._Add );
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
		0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable ( false );
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
	String SetStart = "";
	String SetEnd = "";
	String Constant = "";
	String MonthValues = "";
    String RecalcLimits = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		SetStart = props.getValue ( "SetStart" );
		SetEnd = props.getValue ( "SetEnd" );
		Constant = props.getValue ( "Constant" );
		MonthValues = props.getValue ( "MonthValues" );
        RecalcLimits = props.getValue ( "RecalcLimits" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText (ID);
		}
		if ( (__SetStart_JTextField != null) && (SetStart != null) ) {
			__SetStart_JTextField.setText (SetStart);
		}
		if ( (__SetEnd_JTextField != null) && (SetEnd != null) ) {
			__SetEnd_JTextField.setText (SetEnd);
		}
		if ( (__Constant_JTextField != null) && (Constant != null) ) {
			__Constant_JTextField.setText (Constant);
		}
		if ( (__MonthValues_JTextField != null) && (MonthValues != null) ) {
			__MonthValues_JTextField.setText (MonthValues);
		}
        if ( RecalcLimits == null ) {
            // Select default...
            __RecalcLimits_JComboBox.select ( 0 );
        }
        else {
        	if ( JGUIUtil.isSimpleJComboBoxItem(
                __RecalcLimits_JComboBox, RecalcLimits, JGUIUtil.NONE, null, null )) {
                __RecalcLimits_JComboBox.select ( RecalcLimits );
            }
            else {
            	Message.printWarning ( 1, routine,
                "Existing command references an invalid\n" +
                "RecalcLimits value \"" + RecalcLimits +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
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
	// Regardless, reset the command from the fields...
	ID = __ID_JTextField.getText().trim();
    RecalcLimits = __RecalcLimits_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
	props.add ( "ID=" + ID );
    props.add ( "RecalcLimits=" + RecalcLimits);
	props.add ( "IfNotFound=" + IfNotFound );
	if ( __SetStart_JTextField != null ) {
		MonthValues = __SetStart_JTextField.getText().trim();
		props.add ( "SetStart=" + SetStart );
	}
	if ( __SetEnd_JTextField != null ) {
		MonthValues = __SetEnd_JTextField.getText().trim();
		props.add ( "SetEnd=" + SetEnd );
	}
	if ( __Constant_JTextField != null ) {
		MonthValues = __Constant_JTextField.getText().trim();
		props.add ( "Constant=" + Constant );
	}
	if ( __MonthValues_JTextField != null ) {
		MonthValues = __MonthValues_JTextField.getText().trim();
		props.add ( "MonthValues=" + MonthValues );
	}

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
