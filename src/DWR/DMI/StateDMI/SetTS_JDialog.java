// SetTS_JDialog - Editor for Set*TS() and Set*TSConstant() commands (except instream flow constant!).

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
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for Set*TS() and Set*TSConstant() commands (except instream flow constant!).
*/
@SuppressWarnings("serial")
public class SetTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private String __working_dir = null;
private JTextField __ID_JTextField = null;
private JTextField __TSID_JTextField = null;
private SimpleJComboBox __LEZeroInAverage_JComboBox = null;
private JTextArea __command_JTextArea=null;
//private JTextField __SetStart_JTextField = null;
//private JTextField __SetEnd_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SetTS_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetTS_JDialog ( JFrame parent, SetTS_Command command )
{	super(parent, true);
	initialize ( parent, command );
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
	String TSID = __TSID_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( TSID.length() > 0 ) {
		parameters.set ( "TSID", TSID );
	}
	if ( LEZeroInAverage.length() > 0 ) {
		parameters.set ( "LEZeroInAverage", LEZeroInAverage );
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
	String TSID = __TSID_JTextField.getText().trim();
	String LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "TSID", TSID );
	__command.setCommandParameter ( "LEZeroInAverage", LEZeroInAverage );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__TSID_JTextField = null;
	//__SetStart_JTextField = null;
	//__SetEnd_JTextField = null;
	__LEZeroInAverage_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, SetTS_Command command )
{	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

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

	String dataType = "DivTotal";
	if ( (__command instanceof SetDiversionHistoricalTSMonthly_Command) ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command sets a diversion historical time series " +
    		"(monthly) by reading the data from a file or HydroBase."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"The diversion station identifier is used to match the time series that is read."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	dataType = "DivTotal";
	}
	else if(  __command instanceof SetDiversionDemandTSMonthly_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command sets a diversion demand time series (monthly) "+
    		"by reading the data from a file or HydroBase."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"The diversion station identifier is used to match the time series that is read."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	dataType = "DemTotal";
	}
	else if ( __command instanceof SetWellHistoricalPumpingTSMonthly_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command sets a well historical pumping time series " +
			"(monthly) by reading the data from a file or HydroBase."),
			0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"The well station identifier is used to match the time series that is read."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	dataType = "PumpingHist";
	}
	else if ( __command instanceof SetWellDemandTSMonthly_Command ){
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"This command sets a well demand time series (monthly) by reading the data from a file" +
    		" or HydroBase."),
    		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"The well station identifier is used to match the time series that is read."),
    		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    	dataType = "WellDem";
	}
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"Time series identifiers follow the conventions used by TSTool and other CDSS software."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"For example, for a StateMod file:"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"  ID.." + dataType + ".Month~StateMod~..\\path\\to\\file"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"For example, for a DateValue file:"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"  ID.." + dataType + ".Month~DateValue~..\\path\\to\\file"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"For example, for HydroBase (use TSTool to determine the identifier):"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"  ID.DWR.DivTotal.Month~HydroBase"),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);	
    JGUIUtil.addComponent(paragraph, new JLabel (
	"It is recommended files be specified using a path relative to the working directory."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
    	JGUIUtil.addComponent(paragraph, new JLabel ("The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"If the period that is read is shorter than the output period,"+
	" it is extended to the output period with missing data."),
	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		

	// Add the notes...
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	if ( (__command instanceof SetDiversionHistoricalTSMonthly_Command) ||
		(__command instanceof SetDiversionDemandTSMonthly_Command) ) {
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Diversion station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	else if((__command instanceof SetWellHistoricalPumpingTSMonthly_Command)||
		(__command instanceof SetWellDemandTSMonthly_Command)) {
	   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Well station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__ID_JTextField = new JTextField (10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - stations to process."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Time series ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__TSID_JTextField = new JTextField (40);
	__TSID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __TSID_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "<= zero values in average?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> le_Vector = new Vector<String>(2);
	le_Vector.add ( "" );
	le_Vector.add ( __command._False );
	le_Vector.add ( __command._True );
	__LEZeroInAverage_JComboBox = new SimpleJComboBox(false);
	__LEZeroInAverage_JComboBox.setData( le_Vector );
	__LEZeroInAverage_JComboBox.addItemListener (this);
   	JGUIUtil.addComponent(main_JPanel, __LEZeroInAverage_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - are values <= zero used in averages? (default=" + __command._True + "; used later in filling)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> if_not_found_Vector = new Vector<String>(5);
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
	__command_JTextArea = new JTextArea (4,60);
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
	String TSID = "";
	String LEZeroInAverage = "";
	//String SetStart = "";
	//String SetEnd = "*";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		TSID = props.getValue ( "TSID" );
		LEZeroInAverage = props.getValue ( "LEZeroInAverage" );
		//SetStart = props.getValue ( "SetStart" );
		//SetEnd = props.getValue ( "SetEnd" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText (ID);
		}
		if ( (__TSID_JTextField != null) && (TSID != null) ) {
			__TSID_JTextField.setText (TSID);
		}
		if ( __LEZeroInAverage_JComboBox != null ) {
			if ( LEZeroInAverage == null ) {
				// Select default...
				__LEZeroInAverage_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__LEZeroInAverage_JComboBox,LEZeroInAverage, JGUIUtil.NONE, null, null ) ) {
					__LEZeroInAverage_JComboBox.select (LEZeroInAverage );
				}
				else {
					Message.printWarning ( 1, routine, "Existing command references an invalid\n" +
					"LEZeroInAverage value \"" + LEZeroInAverage +
					"\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		/*
		if ( (__SetStart_JTextField != null) && (SetStart != null) ) {
			__SetStart_JTextField.setText ( SetStart );
		}
		if ( (__SetEnd_JTextField != null) && (SetEnd != null) ) {
			__SetEnd_JTextField.setText ( SetEnd );
		}
		*/
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
	props = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	props.add ( "ID=" + ID );
	TSID = __TSID_JTextField.getText().trim();
	props.add ( "TSID=" + TSID );
	if ( __LEZeroInAverage_JComboBox != null ) {
		LEZeroInAverage = __LEZeroInAverage_JComboBox.getSelected();
		props.add ( "LEZeroInAverage=" + LEZeroInAverage );
	}
	/*
	if ( __SetStart_JTextField != null ) {
		SetStart = __SetStart_JTextField.getText().trim();
		props.add ( "SetStart=" + SetStart );
	}
	if ( __SetEnd_JTextField != null ) {
		SetEnd = __SetEnd_JTextField.getText().trim();
		props.add ( "SetEnd" + SetEnd );
	}*/
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
