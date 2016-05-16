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
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor dialog for FillClimateStation() and SetClimateStation() commands.
*/
public class FillAndSetClimateStation_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private SimpleJComboBox	__Region1_JComboBox = null;
private SimpleJComboBox	__Region2_JComboBox = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private JTextField __Latitude_JTextField = null;
private JTextField __Elevation_JTextField = null;
private JTextField __Name_JTextField = null;
private JTextField __HeightHumidityMeas_JTextField = null;
private JTextField __HeightWindMeas_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private Command __command = null;
private List<String> __Region1_List = null;
private List<String> __Region2_List = null;
private boolean __ok = false; // Has user pressed OK to close the dialog?

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillAndSetClimateStation_JDialog (JFrame parent, Command command,
	List Region1_List, List Region2_List ) {
	super(parent, true);
	initialize (parent, command, Region1_List, Region2_List );
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
	String Name = __Name_JTextField.getText().trim();
	String Latitude = __Latitude_JTextField.getText().trim();
	String Elevation = __Elevation_JTextField.getText().trim();
	String Region1 = __Region1_JComboBox.getSelected();
	String Region2 = __Region2_JComboBox.getSelected();
	String HeightHumidityMeas = __HeightHumidityMeas_JTextField.getText().trim();
	String HeightWindMeas = __HeightWindMeas_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( Name.length() > 0 ) {
		parameters.set ( "Name", Name );
	}
    if ( Latitude.length() > 0 ) {
        parameters.set ( "Latitude", Latitude );
    }
	if ( Elevation.length() > 0 ) {
		parameters.set ( "Elevation", Elevation );
	}
    if ( Region1.length() > 0 ) {
        parameters.set ( "Region1", Region1 );
    }
    if ( Region2.length() > 0 ) {
        parameters.set ( "Region2", Region2 );
    }
    if ( HeightHumidityMeas.length() > 0 ) {
        parameters.set ( "HeightHumidityMeas", HeightHumidityMeas );
    }
	if ( HeightWindMeas.length() > 0 ) {
		parameters.set ( "HeightWindMeas", HeightWindMeas );
	}
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
	try {
		// This will warn the user...
		if ( __command instanceof FillClimateStation_Command ) {
			((FillClimateStation_Command)__command).checkCommandParameters ( parameters, null, 1 );
		}
		else {
			((SetClimateStation_Command)__command).checkCommandParameters ( parameters, null, 1 );
		}
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
	String Latitude = __Latitude_JTextField.getText().trim();
	String Elevation = __Elevation_JTextField.getText().trim();
	String Region1 = __Region1_JComboBox.getSelected();
	String Region2 = __Region2_JComboBox.getSelected();
	String HeightHumidityMeas = __HeightHumidityMeas_JTextField.getText().trim();
	String HeightWindMeas = __HeightWindMeas_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "Latitude", Latitude );
	__command.setCommandParameter ( "Elevation", Elevation );
    __command.setCommandParameter ( "Region1", Region1 );
    __command.setCommandParameter ( "Region2", Region2 );
    __command.setCommandParameter ( "Latitude", Latitude );
	__command.setCommandParameter ( "HeightHumidityMeas", HeightHumidityMeas );
    __command.setCommandParameter ( "HeightWindMeas", HeightWindMeas );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}
	
/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__Name_JTextField = null;
	__Region1_JComboBox = null;
	__Region2_JComboBox = null;
	__IfNotFound_JComboBox = null;
	__Latitude_JTextField = null;
	__Elevation_JTextField = null;
	__ID_JTextField = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__ok_JButton = null;
	__Region1_List = null;
	__Region2_List = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, Command command, List Region1_List, List Region2_List )
{	__command = command;
	__Region1_List = new Vector ( Region1_List );
	__Region1_List.add ( 0, "" );
	__Region2_List = new Vector ( Region2_List );
	__Region2_List.add ( 0, "" );

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
	if ( __command instanceof FillClimateStation_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in climate station(s)," + 
		" using the climate station ID to look up the station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"This command sets (edits) data in climate station(s)," + 
		" using the climate station ID to look up the station."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The climate station ID can contain a * wildcard pattern to match one or more stations."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( ! (__command instanceof FillClimateStation_Command) ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"If the climate station ID does not contain a * wildcard pattern and does not match an ID,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"the location will be added if the \"IfNotFound\" parameter is set to " +
		((SetClimateStation_Command)__command)._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Use blanks in the any field to indicate no change to the existing value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Climate station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - climate station(s) to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Latitude:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Latitude_JTextField = new JTextField (10);
	__Latitude_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Latitude_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - decimal degrees."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Elevation:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Elevation_JTextField = new JTextField (10);
	__Elevation_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Elevation_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - feet."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region 1:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region1_JComboBox = new SimpleJComboBox(10,true);
	__Region1_JComboBox.setData(__Region1_List);
	__Region1_JComboBox.addItemListener (this);
	__Region1_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Region1_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - primary region for the climate station (typically county)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region 2:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region2_JComboBox = new SimpleJComboBox ( 10, true );
	__Region2_JComboBox.setData ( __Region2_List );
	__Region2_JComboBox.addItemListener (this);
	__Region2_JComboBox.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Region2_JComboBox,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - secondary region for the climate station (traditionally HUC or blank)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Name_JTextField = new JTextField (10);
	__Name_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - up to 28 characters for StateCU."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Height of humidy/temp. meas.:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__HeightHumidityMeas_JTextField = new JTextField (10);
	__HeightHumidityMeas_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __HeightHumidityMeas_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - height of humidity/temperature measurements (feet)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Height of wind meas.:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__HeightWindMeas_JTextField = new JTextField (10);
	__HeightWindMeas_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __HeightWindMeas_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - height of wind measurements (feet)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List IfNotFound_List = new Vector();
    IfNotFound_List.add("");
	if ( __command instanceof FillClimateStation_Command ) {
		IfNotFound_List.add ( ((FillClimateStation_Command)__command)._Ignore );
		IfNotFound_List.add ( ((FillClimateStation_Command)__command)._Warn );
		IfNotFound_List.add ( ((FillClimateStation_Command)__command)._Fail );
	}
	else {
		IfNotFound_List.add ( ((SetClimateStation_Command)__command)._Add );
		IfNotFound_List.add ( ((SetClimateStation_Command)__command)._Ignore );
		IfNotFound_List.add ( ((SetClimateStation_Command)__command)._Warn );
		IfNotFound_List.add ( ((SetClimateStation_Command)__command)._Fail );
	}
	__IfNotFound_JComboBox.setData( IfNotFound_List );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __command instanceof FillClimateStation_Command ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
    	   	"Optional - indicate action if no match is found (default=" +
    		((FillClimateStation_Command)__command)._Warn + ")."),
    		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
    else {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
	    	"Optional - indicate action if no match is found (default=" +
			((SetClimateStation_Command)__command)._Warn + ")."),
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
@return true if the edits were committed, false if the user canceled.
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
	String Name = "";
	String Latitude = "";
	String Elevation = "";
	String Region1 = "";
	String Region2 = "";
	String HeightHumidityMeas = "";
	String HeightWindMeas = "";
	String IfNotFound = "";
	__error_wait = false;
	PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		parameters = __command.getCommandParameters();
		Name = parameters.getValue ( "Name" );
		ID = parameters.getValue ( "ID" );
		Latitude = parameters.getValue ( "Latitude" );
		Elevation = parameters.getValue ( "Elevation" );
		Region1 = parameters.getValue ( "Region1" );
		Region2 = parameters.getValue ( "Region2" );
		HeightHumidityMeas = parameters.getValue ( "HeightHumidityMeas" );
		HeightWindMeas = parameters.getValue ( "HeightWindMeas" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( Name != null ) {
			__Name_JTextField.setText(Name);
		}
		if ( Latitude != null ) {
			__Latitude_JTextField.setText(Latitude);
		}
		if ( Elevation != null ) {
			__Elevation_JTextField.setText(Elevation);
		}
		if ( Region1 == null ) {
			// Select default...
			__Region1_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region1_JComboBox, Region1, JGUIUtil.NONE, null, null ) ) {
				__Region1_JComboBox.select ( Region1 );
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an unrecognized\n" +
				"Region1 value \"" + Region1 + "\".  Using the user value.");
				__Region1_JComboBox.setText ( Region1 );
			}
		}
		if ( Region2 == null ) {
			// Select default...
			__Region2_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region2_JComboBox, Region2, JGUIUtil.NONE, null, null ) ) {
				__Region2_JComboBox.select ( Region2 );
			}
			else {
				Message.printWarning ( 2, routine,
				"Existing command references an unrecognized\n" +
				"Region2 value \"" + Region2 + "\".  Using the user value.");
				__Region2_JComboBox.setText ( Region2 );
			}
		}
		if ( HeightHumidityMeas != null ) {
			__HeightHumidityMeas_JTextField.setText(HeightHumidityMeas);
		}
		if ( HeightWindMeas != null ) {
			__HeightWindMeas_JTextField.setText(HeightWindMeas);
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
	// Reset the command from the fields...
	ID = __ID_JTextField.getText().trim();
	Name = __Name_JTextField.getText().trim();
	Latitude = __Latitude_JTextField.getText().trim();
	Elevation = __Elevation_JTextField.getText().trim();
	Region1 = __Region1_JComboBox.getSelected();
	Region2 = __Region2_JComboBox.getSelected();
	HeightHumidityMeas = __HeightHumidityMeas_JTextField.getText().trim();
	HeightWindMeas = __HeightWindMeas_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters = new PropList ( __command.getCommandName() );
	parameters.add ( "ID=" + ID );
	parameters.add ( "Name=" + Name );
    parameters.add ( "Latitude=" + Latitude );
    parameters.add ( "Elevation=" + Elevation );
    parameters.add ( "Region1=" + Region1 );
    parameters.add ( "Region2=" + Region2 );
    parameters.add ( "HeightHumidityMeas=" + HeightHumidityMeas );
    parameters.add ( "HeightWindMeas=" + HeightWindMeas );
    parameters.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString ( parameters ) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
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