// SetCollection_JDialog - Command editor for the Set*Aggregate() and Set*System() (set collection) commands.

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
import java.util.Vector;

import DWR.StateMod.StateMod_Diversion_CollectionType;
import DWR.StateMod.StateMod_Well_CollectionPartType;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Command editor for the Set*Aggregate() and Set*System() (set collection) commands.
*/
@SuppressWarnings("serial")
public class SetCollection_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private SimpleJComboBox __PartType_JComboBox = null;
private JTextField __Year_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextArea __PartIDs_JTextArea = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;
private SetCollection_Command __command = null;
private boolean __ok = false;

/**
Type of collection:  "Aggregate", "System", or "MultiStruct" - see StateMod definitions.
*/
private String __collectionType;
/**
Node type:  "Diversion", "Reservoir", "Well".
*/
private String __nodeType;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCollection_JDialog (JFrame parent, SetCollection_Command command ) {
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String PartIDs = __PartIDs_JTextArea.getText().trim().replace('\n',' ').replace('\r', ' ');;
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		props.set("ID", ID);
	}
	if ( PartIDs.length() > 0 ) {
		props.set("PartIDs", PartIDs);
	}
	if ( IfNotFound.length() > 0 ) {
		props.set("IfNotFound", IfNotFound);
	}
	if ( __PartType_JComboBox != null ) {
		String PartType = __PartType_JComboBox.getSelected();
		if ( PartType.length() > 0 ) {
			props.set("PartType", PartType);
		}
	}
	if ( __Year_JTextField != null ) {
		String Year = __Year_JTextField.getText().trim();
		if ( Year.length() > 0 ) {
			props.set("Year", Year);
		}
	}
	if ( __Div_JTextField != null ) {
		String Div = __Div_JTextField.getText().trim();
		if ( Div.length() > 0 ) {
			props.set("Div", Div);
		}
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
Check the state of the UI given choices.  In particular, disable the year and division if not well collections.
*/
public void checkUIState()
{
	if ( __PartType_JComboBox != null ) {
		// Should only be visible for wells.
		String PartType = __PartType_JComboBox.getSelected();
		if ( __nodeType.equalsIgnoreCase(__command._Well) && PartType.equalsIgnoreCase(__command._Parcel) ) {
			// Enable the fields
			__Year_JTextField.setEnabled(true);
			__Div_JTextField.setEnabled(true);
		}
		else {
			// Disable and clear the fields
			if ( __Year_JTextField != null ) {
				__Year_JTextField.setText("");
				__Year_JTextField.setEnabled(false);
			}
			if ( __Div_JTextField != null ) {
				__Div_JTextField.setText("");
				__Div_JTextField.setEnabled(false);
			}
		}
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits()
{	String ID = __ID_JTextField.getText().trim();
	String PartIDs = __PartIDs_JTextArea.getText().trim().replace('\n',' ').replace('\r', ' ');;
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__command.setCommandParameter("ID", ID);
	__command.setCommandParameter("PartIDs", PartIDs);
	__command.setCommandParameter("IfNotFound", IfNotFound);
	
	if ( __PartType_JComboBox != null ) {
		String PartType = __PartType_JComboBox.getSelected();
		__command.setCommandParameter("PartType", PartType);
	}
	if ( __Year_JTextField != null ) {
		String Year = __Year_JTextField.getText().trim();
		__command.setCommandParameter("Year", Year);
	}
	if ( __Div_JTextField != null ) {
		String Div = __Div_JTextField.getText().trim();
		__command.setCommandParameter("Div", Div);
	}
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, SetCollection_Command command )
{	__command = command;
	
	if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Diversion,0) >= 0 ) {
		__nodeType = __command._Diversion;
	}
	else if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Well,0) >= 0 ) {
		__nodeType = __command._Well;
	}
	else if ( StringUtil.indexOfIgnoreCase(__command.getCommandName(), __command._Reservoir,0) >= 0 ) {
		__nodeType = __command._Reservoir;
	}
	
	if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.AGGREGATE.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.AGGREGATE.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.SYSTEM.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.SYSTEM.toString();
	}
	else if ( StringUtil.indexOfIgnoreCase(
		__command.getCommandName(), StateMod_Diversion_CollectionType.MULTISTRUCT.toString(),0) >= 0 ) {
		__collectionType = StateMod_Diversion_CollectionType.MULTISTRUCT.toString();
	}

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
		"This command sets a " + __nodeType + " location's " + __collectionType + " collection type information." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __collectionType.equalsIgnoreCase(StateMod_Diversion_CollectionType.MULTISTRUCT.toString()) ) {
		// MultiStruct is specific to Diversion demand time series
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "A \"MultiStruct\" is used when demands are met using water from different tributaries and is ONLY used with demand time series." ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "Each diversion station is represented in the model network"),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "and the historical water rights and diversion time series are distinct for each diversion station." ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "However, the efficiencies are estimated using combined demand and historical diversion time series," ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "and total demands are used for the primary structure, with zero demands on the other structure(s)." ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "Operating rules are used to handle sharing diversion water."),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel ( "The primary ID will receive all demands."),
       		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JSeparator (SwingConstants.HORIZONTAL),
            0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "<html><b>Collection type:</b>  MultiStruct</html>"),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "<html><b>Part type:</b>  Ditch (part ID type assumed to be WDID)</html>"),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    else {
    	// Aggregate or System
    	JGUIUtil.addComponent(paragraph, new JLabel (
    		"Each " + __collectionType + " is a location where individual parts are combined into a single feature."),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		JGUIUtil.addComponent(paragraph, new JLabel (
		    "An \"Aggregate\" is used with Set" + __nodeType + "Aggregate() when water rights will be aggregated into classes." ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		    "A \"System\" is used with Set" + __nodeType + "System() when individual water rights will be maintained." ),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		if ( __nodeType.equalsIgnoreCase(__command._Diversion) ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
        		"For example, multiple nearby or related ditches may be grouped as a single identifier."),
			    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Reservoir) ) {
        	JGUIUtil.addComponent(paragraph, new JLabel (
			    "For example, multiple nearby or related reservoirs may be grouped as a single identifier."),
			    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
			JGUIUtil.addComponent(paragraph, new JLabel (
			    "For example, well-only parcels may be grouped as a single identifier."),
			    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
        	    "Wells associated with ditches are grouped by specifying ditch identifiers and apply for the full period."),
        	    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (
			    "<html><b>Grouping wells using parcels is an older approach that is being phased out.  " +
			    "Indicate the year and water division for the parcel data.</b></html>"),
			    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
        JGUIUtil.addComponent(paragraph, new JSeparator (SwingConstants.HORIZONTAL),
            0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		    "<html><b>Collection type:</b>  " + __collectionType + "</html>"),
		    0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		if ( __nodeType.equalsIgnoreCase(__command._Diversion) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Ditch (part ID type assumed to be WDID)</html>"),
		        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Reservoir) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Reservoir (part ID type assumed to be WDID)</html>"),
		        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
		else if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
            JGUIUtil.addComponent(paragraph, new JLabel (
		        "<html><b>Part type:</b>  Ditch (part ID is ditch WDID), Parcel (part ID is parcel ID), "
		        + "or Well (part ID can be well permit as p:Receipt or structure WDID)</html>"),
		        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
		}
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, new JLabel ( __nodeType + " " + __collectionType + " ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField(10);
	__ID_JTextField.setToolTipText(__nodeType + " identifier that is a MultiStruct");
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the " + __nodeType + " " + __collectionType +" ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( __nodeType.equalsIgnoreCase(__command._Well) ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel (	__collectionType + " part type:"),
   			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__PartType_JComboBox = new SimpleJComboBox(false);
		List<String> part_Vector = new ArrayList<String>(3);
		part_Vector.add ( "" + StateMod_Well_CollectionPartType.DITCH );
		part_Vector.add ( "" + StateMod_Well_CollectionPartType.PARCEL );
		part_Vector.add ( "" + StateMod_Well_CollectionPartType.WELL );
		__PartType_JComboBox.setData(part_Vector);
		__PartType_JComboBox.addItemListener (this);
        JGUIUtil.addComponent(main_JPanel, __PartType_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required - the type of features being aggregated."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	       	
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Year:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Year_JTextField = new JTextField(10);
		__Year_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required for part type " + __command._Parcel +
       		" - year for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

       	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Water division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Div_JTextField = new JTextField(10);
		__Div_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required for part type " + __command._Parcel + " - water division for the parcels."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Part IDs:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PartIDs_JTextArea = new JTextArea (10,30);
	__PartIDs_JTextArea.setToolTipText("Separate the part IDs by spaces or commas.");
	__PartIDs_JTextArea.setLineWrap ( true );
	__PartIDs_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel,
		new JScrollPane(__PartIDs_JTextArea),
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - up to 12 characters for each ID."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setToolTipText("Indicate how to handle " + __collectionType + " identifier that is not matched.");
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
    	"Optional - indicate action if no ID match is found (default=" + __command._Warn + ")."),
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
	checkUIState();
	refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed (KeyEvent event)
{	int code = event.getKeyCode();

	refresh ();
	if (code == KeyEvent.VK_ENTER) {
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
	String routine = "SetCollection_JDialog.refresh";
	String ID = "";
	String PartType = "";
	String Year = "";
	String Div = "";
	String PartIDs = "";
	String IfNotFound = "";
	PropList parameters = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		parameters = __command.getCommandParameters();
		ID = parameters.getValue ( "ID" );
		PartType = parameters.getValue ( "PartType" );
		Year = parameters.getValue ( "Year" );
		Div = parameters.getValue ( "Div" );
		PartIDs = parameters.getValue ( "PartIDs" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		// Display existing content...
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __PartType_JComboBox != null ) {
			if ( PartType == null ) {
				// Select default...
				__PartType_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__PartType_JComboBox, PartType, JGUIUtil.NONE, null, null ) ) {
					__PartType_JComboBox.select ( PartType );
				}
				else {
					Message.printWarning ( 2, routine,
					"Existing Set" + __nodeType +__collectionType+
					"() references an unrecognized\n" +
					"PartType value \"" + PartType + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( PartIDs != null ) {
			__PartIDs_JTextArea.setText(PartIDs);
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
			else {	Message.printWarning ( 1, routine,
				"Existing command references an invalid\nIfNotFound value \""+
				IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		// Make sure that the appropriate components are enabled
		checkUIState();
	}
	parameters = new PropList(__command.getCommandName());
	ID = __ID_JTextField.getText().trim();
	PartIDs = __PartIDs_JTextArea.getText().trim().replace('\n',' ').replace('\r', ' ');
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	parameters.add("ID=" + ID);
	parameters.add("PartIDs=" + PartIDs);
	parameters.add("IfNotFound=" + IfNotFound);
	if ( __PartType_JComboBox != null ) {
		PartType = __PartType_JComboBox.getSelected();
		parameters.add("PartType=" + PartType);
	}
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
		parameters.add("Year=" + Year);
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
		parameters.add("Div=" + Div);
	}
	__command_JTextArea.setText( __command.toString(parameters) );
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
