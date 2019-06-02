// FillStationsFromNetwork_JDialog - Editor for Fill*StationsFromNetwork() commands.

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

/**
Editor for Fill*StationsFromNetwork() commands.
*/
@SuppressWarnings("serial")
public class FillStationsFromNetwork_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextField __ID_JTextField=null;
private JTextArea __command_JTextArea=null;
private SimpleJComboBox __NameFormat_JComboBox = null;
private SimpleJComboBox __CommentFormat_JComboBox = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillStationsFromNetwork_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillStationsFromNetwork_JDialog ( JFrame parent, FillStationsFromNetwork_Command command )
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
		checkInput();
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
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__error_wait = false;
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
    if ( IfNotFound.length() > 0 ) {
        parameters.set ( "IfNotFound", IfNotFound );
    }
    if ( __NameFormat_JComboBox != null ) {
    	String NameFormat = (String)__NameFormat_JComboBox.getSelected();
    	if ( NameFormat.length() > 0 ) {
    		parameters.set ( "NameFormat", NameFormat );
    	}
    }
	if ( __CommentFormat_JComboBox != null ) {
		String CommentFormat = (String)__CommentFormat_JComboBox.getSelected();
		if ( CommentFormat.length() > 0 ) {
			parameters.set ( "CommentFormat", CommentFormat );
		}
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
{	String ID = __ID_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
    
    if ( __NameFormat_JComboBox != null ) {
    	String NameFormat = (String)__NameFormat_JComboBox.getSelected();
    	__command.setCommandParameter ( "NameFormat", NameFormat );
    }
	if ( __CommentFormat_JComboBox != null ) {
		String CommentFormat = (String)__CommentFormat_JComboBox.getSelected();
		__command.setCommandParameter ( "CommentFormat", CommentFormat );
	}
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__ID_JTextField = null;
	__NameFormat_JComboBox = null;
	__CommentFormat_JComboBox = null;
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
private void initialize ( JFrame parent, FillStationsFromNetwork_Command command )
{	__command = command;

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
	if ( __command instanceof FillStreamGageStationsFromNetwork_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills in missing data in stream gage stations " +
		"by using data from the network, matching the station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillDiversionStationsFromNetwork_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in diversion stations using " +
		"data from the network, matching the diversion station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillReservoirStationsFromNetwork_Command ) {
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in reservoir stations using " +
		"data from the network, matching the reservoir station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillInstreamFlowStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in instream flow stations using data from the network,"),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillWellStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills missing data in well stations using " +
		"data from the network, matching the well station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillStreamEstimateStationsFromNetwork_Command ){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills in missing data in stream estimate " +
		"stations by using data from the network, matching the station identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"Stream estimate stations are locations where flow is estimated (not historically measured)."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillRiverNetworkFromNetwork_Command){
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command fills in missing data in river network data by " +
		"using data from the network, matching the node identifiers."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"This command is useful if names for stations cannot be filled from a database or other source."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"The following values from the network are set if missing in a station:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( (__command instanceof FillStreamGageStationsFromNetwork_Command) ||
		(__command instanceof FillStreamEstimateStationsFromNetwork_Command)||
		(__command instanceof FillRiverNetworkFromNetwork_Command)){
       	JGUIUtil.addComponent(paragraph, new JLabel ( "   Name - pick a format to use:"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel ( "       StationName - 24 characters."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
        	"       StationName_NodeType - 20 characters + \"_FLO\"."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillDiversionStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("   Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillReservoirStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("   Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillInstreamFlowStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("   Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillWellStationsFromNetwork_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel ("   Name"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Station ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the stations to fill (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    if ( (__command instanceof FillStreamGageStationsFromNetwork_Command) ||
    	(__command instanceof FillStreamEstimateStationsFromNetwork_Command)||
    	(__command instanceof FillRiverNetworkFromNetwork_Command)){
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Name format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> formats = new Vector<String>(3);
        formats.add("");
        formats.add(__command._StationName);
        formats.add(__command._StationName_NodeType);
        __NameFormat_JComboBox = new SimpleJComboBox (false);
		__NameFormat_JComboBox.setData ( formats );
		__NameFormat_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(main_JPanel, __NameFormat_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for the name (default=" + __command._StationName + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	if ( __command instanceof FillRiverNetworkFromNetwork_Command ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ("Comment format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List<String> comments = new Vector<String>(2);
        comments.add("");
        comments.add(__command._StationID);
        __CommentFormat_JComboBox = new SimpleJComboBox (false);
        __CommentFormat_JComboBox.setData ( comments );
		__CommentFormat_JComboBox.addItemListener ( this );
        JGUIUtil.addComponent(main_JPanel, __CommentFormat_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for the comment (default=" + __command._StationID + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	
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
{	String routine = getClass().getName() + ".refresh";
	String ID = "";
	String NameFormat = "";
	String CommentFormat = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		NameFormat = props.getValue ( "NameFormat" );
		CommentFormat = props.getValue ( "CommentFormat" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __NameFormat_JComboBox != null ) {
			if ( NameFormat == null ) {
				// Select default...
				__NameFormat_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__NameFormat_JComboBox, NameFormat, JGUIUtil.NONE, null, null ) ) {
					__NameFormat_JComboBox.select ( NameFormat );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nNameFormat value \"" +
					NameFormat + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __CommentFormat_JComboBox != null ) {
			if ( CommentFormat == null ) {
				// Select default...
				__CommentFormat_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__CommentFormat_JComboBox, CommentFormat, JGUIUtil.NONE, null, null ) ) {
					__CommentFormat_JComboBox.select ( CommentFormat );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\nCommentFormat value \""+
					CommentFormat + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
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

	// Always get the value that is selected...
	props = new PropList ( __command.getCommandName() );
	ID = __ID_JTextField.getText().trim();
	props.add ( "ID=" + ID );
	if ( __NameFormat_JComboBox != null ) {
		NameFormat = (String)__NameFormat_JComboBox.getSelected();
		props.add ( "NameFormat=" + NameFormat );
	}
	if ( __CommentFormat_JComboBox != null ) {
		CommentFormat = (String)__CommentFormat_JComboBox.getSelected();
		props.add ( "CommentFormat=" + CommentFormat );
	}
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
