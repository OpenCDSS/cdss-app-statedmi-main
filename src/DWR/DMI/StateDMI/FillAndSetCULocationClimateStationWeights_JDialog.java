// FillAndSetCULocationClimateStationWeights_JDialog - Editor for the FillCULocationClimateStationWeights() and SetCULocationClimateStationWeights() commands.

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

/**
Editor for the FillCULocationClimateStationWeights() and SetCULocationClimateStationWeights() commands.
*/
@SuppressWarnings("serial")
public class FillAndSetCULocationClimateStationWeights_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private JTextField __Weights_JTextField = null;
private SimpleJComboBox __IncludeOrographicTempAdj_JComboBox = null;
private SimpleJComboBox __IncludeOrographicPrecAdj_JComboBox = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private FillAndSetCULocationClimateStationWeights_Command __command = null;
private boolean __ok = false; // Has user pressed OK to close the dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
command.  If false, the dialog will behave as if for the SetCULocationClimateStationWeights() command.
@param command Command to edit.
*/
public FillAndSetCULocationClimateStationWeights_JDialog (JFrame parent, Command command )
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
	String Weights = __Weights_JTextField.getText().trim();
	String IncludeOrographicTempAdj = __IncludeOrographicTempAdj_JComboBox.getSelected();
	String IncludeOrographicPrecAdj = __IncludeOrographicPrecAdj_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
    if ( IncludeOrographicTempAdj.length() > 0 ) {
        parameters.set ( "IncludeOrographicTempAdj", IncludeOrographicTempAdj );
    }
	if ( IncludeOrographicPrecAdj.length() > 0 ) {
		parameters.set ( "IncludeOrographicPrecAdj", IncludeOrographicPrecAdj );
	}
	if ( Weights.length() > 0 ) {
		parameters.set ( "Weights", Weights );
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
{
	String ID = __ID_JTextField.getText().trim();
	String Weights = __Weights_JTextField.getText().trim();
	String IncludeOrographicTempAdj = __IncludeOrographicTempAdj_JComboBox.getSelected();
	String IncludeOrographicPrecAdj = __IncludeOrographicPrecAdj_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
    __command.setCommandParameter ( "IncludeOrographicTempAdj", IncludeOrographicTempAdj );
	__command.setCommandParameter ( "IncludeOrographicPrecAdj", IncludeOrographicPrecAdj );
	__command.setCommandParameter ( "Weights", Weights );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param fill If true, the dialog will behave as if for the FillCULocationClimateStationWeights()
command.  If false, the dialog will behave as if for the SetCULocationClimateStationWeights() command.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (FillAndSetCULocationClimateStationWeights_Command)command;
	
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
	if ( command instanceof FillCULocationClimateStationWeights_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills the climate station identifiers and weights associated with a CU location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"Only locations with no previous station weights are changed."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command assigns the climate station identifiers and weights associated with a CU location."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The previous data will be reset to new values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Each CU location must be associated with one or more precipitation and temperature stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The data from each station is weighted, and the weights should add to 1.0."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(paragraph, new JLabel (
   		"The climate station weights should be specified using the format (, and ; are equivalent):"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"     StationID,TempWt,PrecWt;StationID,TempWt,PrecWt,..."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "For example:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "     2184,.7,.7,3951,.3,.3"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel ( "The weights are specified as a fraction 0.0 to 1.0."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "If orographic adjustment factors are included, insert the factors after the weights in " +
        "the order of temperature adjustment, precipitation adjustment,"),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "where the temperature adjustment is degrees F/1000 ft. of elevation and the precipitation " +
        "adjustment is a fraction (0.0 to 1.0)."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the CU Location(s) to process (use * for wildcard)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Include orographic temperature adjustment?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> oroList = new Vector<String>(3);
    oroList.add ( "" ); // Default
	oroList.add ( __command._False );
	oroList.add ( __command._True );
	__IncludeOrographicTempAdj_JComboBox = new SimpleJComboBox(false);
	__IncludeOrographicTempAdj_JComboBox.setData ( oroList );
	__IncludeOrographicTempAdj_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __IncludeOrographicTempAdj_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - include orographic temperature adjustment factor in data (default=False)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel (	"Include orographic precipitation adjustment?:"),
	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeOrographicPrecAdj_JComboBox = new SimpleJComboBox(false);
    __IncludeOrographicPrecAdj_JComboBox.setData ( oroList );
    __IncludeOrographicPrecAdj_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(main_JPanel, __IncludeOrographicPrecAdj_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Optional - include orographic precipitation adjustment factor in data (default=False)."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel("Climate station weights:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Weights_JTextField = new JTextField (50);
	__Weights_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __Weights_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List<String> IfNotFound_List = new Vector<String>(4);
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
	String routine = getClass().getName() + "_JDialog.refresh";
	String ID = "";
	String IncludeOrographicTempAdj = "";
	String IncludeOrographicPrecAdj = "";
	String Weights = "";
	String IfNotFound = "";
	__error_wait = false;
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		ID = parameters.getValue ( "ID" );
		IncludeOrographicTempAdj = parameters.getValue ( "IncludeOrographicTempAdj" );
		IncludeOrographicPrecAdj = parameters.getValue ( "IncludeOrographicPrecAdj" );
		Weights = parameters.getValue ( "Weights" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( IncludeOrographicTempAdj == null ) {
			// Select default...
			__IncludeOrographicTempAdj_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IncludeOrographicTempAdj_JComboBox, IncludeOrographicTempAdj, JGUIUtil.NONE, null, null ) ) {
				__IncludeOrographicTempAdj_JComboBox.select ( IncludeOrographicTempAdj );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIncludeOrographicTempAdj value \""+
				IncludeOrographicTempAdj + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IncludeOrographicPrecAdj == null ) {
			// Select default...
			__IncludeOrographicPrecAdj_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IncludeOrographicPrecAdj_JComboBox, IncludeOrographicPrecAdj, JGUIUtil.NONE, null, null ) ) {
				__IncludeOrographicPrecAdj_JComboBox.select ( IncludeOrographicPrecAdj );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIncludeOrographicPrecAdj value \""+
				IncludeOrographicPrecAdj + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Weights != null ) {
			__Weights_JTextField.setText(Weights);
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
				Message.printWarning ( 1, routine, "Existing " + __command + 
				"() references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}
	// Reset the command from the fields...
	ID = __ID_JTextField.getText().trim();
	IncludeOrographicTempAdj = __IncludeOrographicTempAdj_JComboBox.getSelected();
	IncludeOrographicPrecAdj = __IncludeOrographicPrecAdj_JComboBox.getSelected();
	Weights = __Weights_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "ID=" + ID );
	parameters.add ( "IncludeOrographicTempAdj=" + IncludeOrographicTempAdj );
    parameters.add ( "IncludeOrographicPrecAdj=" + IncludeOrographicPrecAdj );
    parameters.add ( "Weights=" + Weights );
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
