// Sort_JDialog - Editor for Sort*() commands.

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

/**
Editor for Sort*() commands.
*/
@SuppressWarnings("serial")
public class Sort_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
// Values for Order, Order2
private String __Ascending = "Ascending";
private String __IDAscending = "IDAscending";
private String __LocationIDAscending = "LocationIDAscending";

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __Order_JComboBox = null;
private SimpleJComboBox __Order2_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private Sort_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public Sort_JDialog ( JFrame parent, Command command ) {
	super(parent, true);
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String Order = __Order_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (Order.length() > 0) {
		props.set("Order", Order);
	}
	if ( __Order2_JComboBox != null ) {
		String Order2 = __Order2_JComboBox.getSelected();
		if (Order2.length() > 0) {
			props.set("Order2", Order2);
		}
	}

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
private void commitEdits()
{
	String Order = __Order_JComboBox.getSelected();
	__command.setCommandParameter("Order", Order);
	
	if ( __Order2_JComboBox != null ) {
		String Order2 = __Order2_JComboBox.getSelected();
		__command.setCommandParameter("Order2", Order2);
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__command_JTextArea = null;
	__Order_JComboBox = null;
	__Order2_JComboBox = null;
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
{	__command = (Sort_Command)command;

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
	
	// StateMod data components...
	
	if ( __command instanceof SortStreamGageStations_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts stream gage stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortDiversionStations_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts diversion stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortDiversionRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts diversion rights by right identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortDiversionHistoricalTSMonthly_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts diversion historical time series (monthly) by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortDiversionDemandTSMonthly_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts diversion demand time series (monthly) by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortReservoirStations_Command ) {
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts reservoir stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortReservoirRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts reservoir rights by right identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortInstreamFlowStations_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts instream flow stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortInstreamFlowRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts instream flow rights by right identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortWellStations_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts well stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortWellRights_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts well rights by right and/or location identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortWellHistoricalPumpingTSMonthly_Command ){
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts well historical pumping time series (monthly) by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortWellDemandTSMonthly_Command){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts well demand time series (monthly) by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortStreamEstimateStations_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts stream estimate stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	
	// StateCU data components...

	else if ( __command instanceof SortClimateStations_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts CU climate stations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortCropPatternTS_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts crop pattern time series by CU location identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortIrrigationPracticeTS_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts irrigation practice time series by CU location identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortCULocations_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts CU locations by station identifier."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortBlaneyCriddle_Command ){
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts Blaney-Criddle data by crop name."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortPenmanMonteith_Command ){
      	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts Penman-Monteith data by crop name."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof SortCropCharacteristics_Command){
	   	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sorts Crop Characteristic data by crop name."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Sorting identifiers makes the order of data consistent in related files."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Order:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Order_JComboBox = new SimpleJComboBox(false);
	List<String> order_Vector = new Vector<String>(2);
	order_Vector.add ( "" );
	if ( __command instanceof SortWellRights_Command ) {
		order_Vector.add ( __IDAscending );
		order_Vector.add ( __LocationIDAscending );
	}
	else {
		// Leave the same as before until there is time to change
		order_Vector.add ( __Ascending );
	}
	__Order_JComboBox.setData ( order_Vector );
	__Order_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __Order_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __command instanceof SortWellRights_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - primary sort order (default=" + __IDAscending + ")."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else {
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
   			"Optional - primary sort order (default=" + __Ascending + ")."), 
   			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    if ( __command instanceof SortWellRights_Command ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Order2:"), 
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Order2_JComboBox = new SimpleJComboBox(false);
		List<String> Order2_Vector = new Vector<String>(3);
		Order2_Vector.add ( "" );
    	Order2_Vector.add ( __IDAscending );
    	Order2_Vector.add ( __LocationIDAscending );
   		__Order2_JComboBox.setData ( Order2_Vector );
    	__Order2_JComboBox.addItemListener (this);
    	JGUIUtil.addComponent(main_JPanel, __Order2_JComboBox,
    		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    		"Optional - secondary sort order (default=not used)."), 
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
public boolean ok() {
	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	__error_wait = false;
	String routine = getClass().getName() + ".refresh";
	String Order = "";
	String Order2 = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		Order = props.getValue ( "Order" );
		Order2 = props.getValue ( "Order2" );
		// Convert old default to new...
		if ( (Order != null) && Order.equalsIgnoreCase("Alphabetical") ) {
			Order = "Ascending";
		}
		// Display existing content...
		if ( Order == null ) {
			// Select default...
			__Order_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Order_JComboBox, Order, JGUIUtil.NONE, null, null ) ) {
				__Order_JComboBox.select ( Order );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid Order value \"" +
				Order + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		// Display existing content...
		if ( __Order2_JComboBox != null ) {
			if ( Order2 == null ) {
				// Select default...
				__Order2_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__Order2_JComboBox, Order2, JGUIUtil.NONE, null, null ) ) {
					__Order2_JComboBox.select ( Order2 );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid Order2 value \"" +
					Order2 + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
	}
	props = new PropList(__command.getCommandName());
	Order = __Order_JComboBox.getSelected();
	props.add("Order=" + Order);
	if ( __Order2_JComboBox != null ) {
		Order2 = __Order2_JComboBox.getSelected();
		props.add("Order2=" + Order2);
	}
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
