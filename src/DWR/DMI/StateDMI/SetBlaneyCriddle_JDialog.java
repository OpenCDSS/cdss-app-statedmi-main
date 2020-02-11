// SetBlaneyCriddle_JDialog - Editor for SetBlaneyCriddle() command.

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
import RTi.Util.String.StringUtil;

/**
Editor for SetBlaneyCriddle() command.
*/
@SuppressWarnings("serial")
public class SetBlaneyCriddle_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea = null;
private JTextField __CropType_JTextField = null;
private SimpleJComboBox __CurveType_JComboBox = null;
private SimpleJComboBox __BlaneyCriddleMethod_JComboBox = null;
private JTextArea __Coefficients_JTextArea = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SetBlaneyCriddle_Command __command = null;
private boolean __ok = false;

/**
setBlaneyCriddle_JDialog constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetBlaneyCriddle_JDialog (JFrame parent, SetBlaneyCriddle_Command command) {
	super(parent, true);
	initialize (parent, command);
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
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String CropType = __CropType_JTextField.getText().trim();
	String CurveType = StringUtil.getToken ( __CurveType_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( CurveType == null) {
		CurveType = "";
	}
	String BlaneyCriddleMethod = StringUtil.getToken ( __BlaneyCriddleMethod_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( BlaneyCriddleMethod == null ) {
		BlaneyCriddleMethod = "";
	}
	String Coefficients = __Coefficients_JTextArea.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (CropType.length() > 0) {
		props.set("CropType", CropType);
	}
	if (CurveType.length() > 0) {
		props.set("CurveType", CurveType);
	}
	if (BlaneyCriddleMethod.length() > 0) {
		props.set("BlaneyCriddleMethod", BlaneyCriddleMethod);
	}
	if (Coefficients.length() > 0 ) {
		props.set("Coefficients", Coefficients);
	}
	if (IfNotFound.length() > 0 ) {
		props.set("IfNotFound", IfNotFound);
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
	String CropType = __CropType_JTextField.getText().trim();
	String CurveType = StringUtil.getToken ( __CurveType_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( CurveType == null) {
		CurveType = "";
	}
	String BlaneyCriddleMethod = StringUtil.getToken ( __BlaneyCriddleMethod_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( BlaneyCriddleMethod == null ) {
		BlaneyCriddleMethod = "";
	}
	String Coefficients = __Coefficients_JTextArea.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter("CropType", CropType);
	__command.setCommandParameter("CurveType", CurveType);
	__command.setCommandParameter("BlaneyCriddleMethod", BlaneyCriddleMethod);
	__command.setCommandParameter("Coefficients", Coefficients);
	__command.setCommandParameter("IfNotFound", IfNotFound);
}

public void stateChanged(ChangeEvent e) {
	refresh();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, SetBlaneyCriddle_Command command) {
	__command = command;
	
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
		"This command edits Blaney-Criddle crop coefficient data for a specified crop type (name)." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"If the crop coefficients are based on the day of year,"+
		" specify 25 values, for the month boundaries and midpoints."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"If the crop coefficients are based on the percent of "+
		"growing season, specify 21 values, for 0, 5, ..., 100%."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"If the crop type does not contain a * wildcard pattern and does not match existing data,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"a new curve will be added if the \"If not found\" option is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Single values in a curve cannot be set - the entire curve must be set."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Crop Type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropType_JTextField = new JTextField(10);
	__CropType_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CropType_JTextField,
		1, y, 3, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required - crop type (use * for wildcard)."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Curve Type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> CurveType_Vector = new Vector<String>(2);
	CurveType_Vector.add ( __command._CurveType_DAY );
	CurveType_Vector.add ( __command._CurveType_PERCENT );
	__CurveType_JComboBox = new SimpleJComboBox(false);
	__CurveType_JComboBox.setData ( CurveType_Vector );
	__CurveType_JComboBox.addTextFieldKeyListener (this);
	__CurveType_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __CurveType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - see comments above."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
   JGUIUtil.addComponent(main_JPanel, new JLabel ("Blaney-Criddle Method:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   List<String> BlaneyCriddleMethod_Vector = new Vector<String>(6);
   BlaneyCriddleMethod_Vector.add ( "" );
   BlaneyCriddleMethod_Vector.add ( __command._BLANEY_CRIDDLE_METHOD_0 );
   BlaneyCriddleMethod_Vector.add ( __command._BLANEY_CRIDDLE_METHOD_1 );
   BlaneyCriddleMethod_Vector.add ( __command._BLANEY_CRIDDLE_METHOD_2 );
   BlaneyCriddleMethod_Vector.add ( __command._BLANEY_CRIDDLE_METHOD_3 );
   BlaneyCriddleMethod_Vector.add ( __command._BLANEY_CRIDDLE_METHOD_4 );
   __BlaneyCriddleMethod_JComboBox = new SimpleJComboBox(false);
   __BlaneyCriddleMethod_JComboBox.setData ( BlaneyCriddleMethod_Vector );
   __BlaneyCriddleMethod_JComboBox.addTextFieldKeyListener (this);
   __BlaneyCriddleMethod_JComboBox.addItemListener (this);
      	JGUIUtil.addComponent(main_JPanel, __BlaneyCriddleMethod_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - Blaney-Criddle method to use."),
        5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Coefficients:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Coefficients_JTextArea = new JTextArea (3,40);
	__Coefficients_JTextArea.setLineWrap ( true );
	__Coefficients_JTextArea.setWrapStyleWord ( true );
	__Coefficients_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__Coefficients_JTextArea),
		1, y, 4, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - separate by commas."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IfNotFound_Vector = new Vector<String>(5);
    IfNotFound_Vector.add ( "" );
	IfNotFound_Vector.add ( __command._Add );
	IfNotFound_Vector.add ( __command._Ignore );
	IfNotFound_Vector.add ( __command._Warn );
	IfNotFound_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( IfNotFound_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - action if no match is found (default=" + __command._Warn + ")."),
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
{	String routine = "SetBlaneyCriddle_JDialog.refresh";
	String CropType = "";
	String CurveType = "";
	String BlaneyCriddleMethod = "";
	String Coefficients = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		CropType = props.getValue ( "CropType" );
		CurveType = props.getValue ( "CurveType" );
		BlaneyCriddleMethod = props.getValue( "BlaneyCriddleMethod" );
		Coefficients = props.getValue ( "Coefficients" );
		IfNotFound = props.getValue ( "IfNotFound" );
		// Display existing content...
		if ( CropType != null ) {
			__CropType_JTextField.setText(CropType);
		}
		if ( CurveType == null ) {
			// Select default...
			__CurveType_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __CurveType_JComboBox, true, " ", 0, 0, CurveType, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nCurveType value \""+ CurveType +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( BlaneyCriddleMethod == null ) {
			// Select default...
			__BlaneyCriddleMethod_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __BlaneyCriddleMethod_JComboBox, true, " ", 0, 0, BlaneyCriddleMethod, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nBlaneyCriddleMethod value \""+ BlaneyCriddleMethod +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Coefficients != null ) {
			__Coefficients_JTextArea.setText(Coefficients);
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
				Message.printWarning ( 2, routine, "Existing command references an unrecognized\n" +
				"IfNotFound value \"" + IfNotFound + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	CropType = __CropType_JTextField.getText().trim();
	CurveType = StringUtil.getToken ( __CurveType_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	BlaneyCriddleMethod = StringUtil.getToken ( __BlaneyCriddleMethod_JComboBox.getSelected(), " -", StringUtil.DELIM_SKIP_BLANKS, 0 );
	Coefficients = __Coefficients_JTextArea.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	props = new PropList(__command.getCommandName());
	props.add("CropType=" + CropType );
	props.add("CurveType=" + CurveType);
	props.add("BlaneyCriddleMethod=" + BlaneyCriddleMethod);
	props.add("Coefficients=" + Coefficients);
	props.add("IfNotFound=" + IfNotFound);
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
