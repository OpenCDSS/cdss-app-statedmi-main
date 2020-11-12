// ReadIrrigationPracticeTSFromParcels_JDialog - Editor for ReadIrrigationPracticeTSFromParcels() command.

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
//import riverside.datastore.DataStore;

/**
Editor for ReadIrrigationPracticeTSFromHydroBase() command.
*/
@SuppressWarnings("serial")
public class ReadIrrigationPracticeTSFromParcels_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false;	// Indicate whether OK has been pressed
// TODO smalers 2020-11-10 disable datastore for now - all data will be read from parcels rather than HydroBase
// - remove later if HydroBase is not needed 
//private SimpleJComboBox __DataStore_JComboBox = null;
private JTextField __ID_JTextField=null;
private JTextField __Year_JTextField = null;
// TODO smalers 2020-11-10 remove when tested out
//private JTextField __Div_JTextField = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private ReadIrrigationPracticeTSFromParcels_Command __command = null;
private StateDMI_Processor __statedmiProcessor = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to parse.
*/
public ReadIrrigationPracticeTSFromParcels_JDialog ( JFrame parent,
	ReadIrrigationPracticeTSFromParcels_Command command )
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
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response ( true );
		}
	}
}

/**
Check the input.  Currently does nothing.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	//String DataStore = __DataStore_JComboBox.getSelected();
	String ID = __ID_JTextField.getText().trim();
	String Year = __Year_JTextField.getText().trim();
	//String Div = __Div_JTextField.getText().trim();
	__error_wait = false;
	
	PropList props = new PropList ( "" );
	//if ( DataStore.length() > 0 ){
	//	props.set ( "Datastore", DataStore );
	//}
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( Year.length() > 0 ) {
		props.set ( "Year", Year );
	}
	//if ( Div.length() > 0 ) {
	//	props.set ( "Div", Div );
	//}
	try {
		// This will warn the user
		__command.checkCommandParameters( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning will have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	
	//String DataStore = __DataStore_JComboBox.getSelected();
	String ID = __ID_JTextField.getText().trim();
	String Year = __Year_JTextField.getText().trim();
	//String Div = __Div_JTextField.getText().trim();

	//__command.setCommandParameter ( "DataStore" , DataStore );
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "Year", Year );
	//__command.setCommandParameter ( "Div", Div );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, ReadIrrigationPracticeTSFromParcels_Command command )
{	__command = command;
    __statedmiProcessor = (StateDMI_Processor)__command.getCommandProcessor();

	addWindowListener(this);

    Insets insetsTLBR = new Insets(1,2,1,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = -1;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads irrigation practice acreage time series data from parcel data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(paragraph, new JLabel (
		"Acreage values are set only for years with data.  Fill commands must be " +
		"used to estimate missing data in other years."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    // Datastore ID options
	/*
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Datastore:"), 0, ++y, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<DataStore> DataStores = __statedmiProcessor.getDataStores();
    List<String> datastoreList = new ArrayList<String>();
    datastoreList.add("");
    for(int i = 0; i < DataStores.size(); i++){
    	datastoreList.add(DataStores.get(i).getName());
    }
    __DataStore_JComboBox = new SimpleJComboBox(false);
    __DataStore_JComboBox.setToolTipText("Specify HydroBase or ColoradoHydroBaseRest datastore, blank for direct database query");
    __DataStore_JComboBox.setData(datastoreList);
    __DataStore_JComboBox.select(0);
    __DataStore_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(main_JPanel, __DataStore_JComboBox,
    		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - datastore (default=direct HydroBase query)."),
    		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	*/
	
	JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU locations to read (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
   	/*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Water Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
   		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (	"Required - water division for the parcels."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	*/

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Year:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	__Year_JTextField = new JTextField(10);
		__Year_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - year(s) for the parcels, separated by commas (default=all available)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
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

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	// JDialogs do not need to be resizable...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
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
	else {	// One of the combo boxes...
		refresh();
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
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getSimpleName() + ".refresh";
	//String DataStore = "";
	String ID = "";
	String Year = "";
	//String Div = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		//DataStore = props.getValue ( "DataStore" );
		ID = props.getValue ( "ID" );
		Year = props.getValue ( "Year" );
		//Div = props.getValue ( "Div" );
		/*
		if ( DataStore == null ) {
			// Select default...
			__DataStore_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DataStore_JComboBox, DataStore, JGUIUtil.NONE, null, null ) ) {
				__DataStore_JComboBox.select (  DataStore );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid DataStore " +
				"value \"" + DataStore + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		*/
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		/*
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		*/
	}

	// Always get the value that is selected...

	//DataStore = __DataStore_JComboBox.getSelected();
	ID = __ID_JTextField.getText().trim();
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
	}
	/*
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
	}
	*/
	
	//props.add ( "DataStore=" + DataStore );
	props.add ( "ID=" + ID );
	props.add ( "Year=" + Year );
	//props.add ( "Div=" + Div );
	__command_JTextArea.setText( __command.toString(props) );
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
			Message.printStatus(2, "", "error_wait=true");
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
