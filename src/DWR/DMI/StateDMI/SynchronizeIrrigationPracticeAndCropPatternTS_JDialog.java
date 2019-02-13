// SynchronizeIrrigationPracticeAndCropPatternTS_JDialog - editor for SynchronizeIrrigationPracticeAndCropPatternTS command

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

// ----------------------------------------------------------------------------
// synchronizeIrrigationPracticeAndCropPatternTS_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2005-02-24	Steven A. Malers, RTi	Initial version - copy and modify
//					createIrrigationPracticeTS
//					ForCULocations_JDialog.
// 2005-07-27	SAM, RTi		Add GWOnlyGWAcreage,
//					DivAndWellGWAcreage, SprinklerAcreage
//					parameters.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

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
import RTi.Util.String.StringUtil;

public class SynchronizeIrrigationPracticeAndCropPatternTS_JDialog
extends JDialog
implements ActionListener, KeyListener, WindowListener
{
	
// SynchronizeMethod options:
	
private final String __ProratePartsToCropPatternTotal = "ProratePartsToCropPatternTotal";
private final String __SpecificChecks = "SpecificChecks";

private final String __AdjustNone = "AdjustNone";
private final String __AdjustGWAcreageToCropPatternTotal =
		"AdjustGWAcreageToCropPatternTotal";
private final String __AdjustCropPatternTotalToGWAcreage =
		"AdjustCropPatternTotalToGWAcreage";

private final String __AdjustGWAcreageDownToCropPatternTotal =
		"AdjustGWAcreageDownToCropPatternTotal";

private final String __AdjustSprinklerAcreageDownToCropPatternTotal =
		"AdjustSprinklerAcreageDownToCropPatternTotal";

private boolean		__error_wait = false;
private boolean		__first_time = true;	
private JTextField	__ID_JTextField=null;
private SimpleJComboBox	__SynchronizeMethod_JComboBox = null;
private SimpleJComboBox	__GWOnlyGWAcreage_JComboBox = null;
private SimpleJComboBox	__DivAndWellGWAcreage_JComboBox = null;
private SimpleJComboBox	__SprinklerAcreage_JComboBox = null;
private JTextArea	__command_JTextArea=null;
private SimpleJButton	__cancel_JButton = null;
private SimpleJButton	__ok_JButton = null;	
private List		__command_Vector = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Command to parse.
*/
public SynchronizeIrrigationPracticeAndCropPatternTS_JDialog (JFrame parent,
					PropList props, List command)
{	super(parent, true);
	initialize (parent,
		"Edit SynchronizeIrrigationPracticeAndCropPatternTS() Command", 
		props, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response (0);
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response (1);
		}
	}
	else {	// Choices...
		refresh ();
	}
}

/**
Check the input.  Currently does nothing.
*/
private void checkInput ()
{	String routine =
		"synchronizeIrrigationPracticeAndCropPatternTS.checkInput";
	String ID = __ID_JTextField.getText().trim();
	String warning = "";
	__error_wait = false;
	if ( ID.length() == 0 ) {
		warning += "\nAn identifier or pattern must be specified." +
			"  Correct or Cancel.";
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( 1, routine, warning );
		__error_wait = true;
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__SynchronizeMethod_JComboBox = null;
	__GWOnlyGWAcreage_JComboBox = null;
	__DivAndWellGWAcreage_JComboBox = null;
	__SprinklerAcreage_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command_Vector = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the 
command.
*/
public List getText () {
	if ((__command_Vector != null) && ((__command_Vector.size() == 0) ||
		((String)__command_Vector.get(0)).equals(""))) {
		return null;
	}
	return __command_Vector;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title JDialog title.
@param app_PropList Properties from the application.
@param command Vector of String containing the command.
*/
private void initialize (	JFrame parent, String title, PropList props, 
		List command )
{	__command_Vector = command;

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
	JGUIUtil.addComponent(paragraph, new JLabel (
	"THIS COMMAND IS OBSOLETE AND IS USED FOR PHASE 4 RIO GRANDE WORK - " +
	"INSTEAD, SEE NEWER FILL/SET COMMANDS."),
	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command synchronizes the irrigation practice and crop "+
		"pattern time series acreage for the specified CU Locations." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
       	"This may be necessary due to setting components of the IPY acreage "+
       	"using different techniques (e.g., user-supplied, water rights)." ),
       	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
               	"Synchronization may be a simple proration of acreage columns to equal the " +
               	"crop pattern total, or a list of specific checks may be used." ),
               	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The total acres values from the crop pattern time series are" +
		" used to set the total acres in the irrigation practice time "+
		"series." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The irrigation practice groundwater and sprinkler acres" +
		" are also synchronized with the total acreage."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"This command should be used after processing irrigation " +
		"practice time series and reading the crop pattern time " +
		"series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
        "The synchronization method indicates whether acreage in the irrigation practice " +
        "file are shifted relative to each other or are simply prorated to the total."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
        JGUIUtil.addComponent(main_JPanel, new JLabel ("CU Location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify the locations for data (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Synchronize method:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List SynchronizeMethod_Vector = new Vector(5);
        SynchronizeMethod_Vector.add ( "" );
        SynchronizeMethod_Vector.add (__ProratePartsToCropPatternTotal);
        SynchronizeMethod_Vector.add (__SpecificChecks);
        __SynchronizeMethod_JComboBox = new SimpleJComboBox(false);
        __SynchronizeMethod_JComboBox.setData ( SynchronizeMethod_Vector );
        __SynchronizeMethod_JComboBox.addActionListener (this);
        JGUIUtil.addComponent(main_JPanel, __SynchronizeMethod_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Default is " + __SpecificChecks + "." ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Adjust groundwater-only GW acreage how?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List GWOnlyGWAcreage_Vector = new Vector(5);
	GWOnlyGWAcreage_Vector.add ( "" );
	GWOnlyGWAcreage_Vector.add (__AdjustCropPatternTotalToGWAcreage);
	GWOnlyGWAcreage_Vector.add (__AdjustGWAcreageDownToCropPatternTotal);
	GWOnlyGWAcreage_Vector.add (__AdjustGWAcreageToCropPatternTotal);
	GWOnlyGWAcreage_Vector.add (__AdjustNone );
	__GWOnlyGWAcreage_JComboBox = new SimpleJComboBox(false);
	__GWOnlyGWAcreage_JComboBox.setData ( GWOnlyGWAcreage_Vector );
	__GWOnlyGWAcreage_JComboBox.addActionListener (this);
	JGUIUtil.addComponent(main_JPanel, __GWOnlyGWAcreage_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Default is AdjustGWAcreageDownToCropPatternTotal." ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Adjust diversion+well GW acreage how?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List DivAndWellGWAcreage_Vector = new Vector();
	DivAndWellGWAcreage_Vector.add ( "" );
	DivAndWellGWAcreage_Vector.add (
		__AdjustGWAcreageDownToCropPatternTotal);
	DivAndWellGWAcreage_Vector.add (__AdjustNone );
	__DivAndWellGWAcreage_JComboBox = new SimpleJComboBox(false);
	__DivAndWellGWAcreage_JComboBox.setData ( DivAndWellGWAcreage_Vector );
	__DivAndWellGWAcreage_JComboBox.addActionListener (this);
	JGUIUtil.addComponent(main_JPanel, __DivAndWellGWAcreage_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Default is AdjustGWAcreageDownToCropPatternTotal." ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Adjust sprinkler acreage how?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List SprinklerAcreage_Vector = new Vector();
	SprinklerAcreage_Vector.add ( "" );
	SprinklerAcreage_Vector.add (
		__AdjustSprinklerAcreageDownToCropPatternTotal );
	SprinklerAcreage_Vector.add (__AdjustNone );
	__SprinklerAcreage_JComboBox = new SimpleJComboBox(false);
	__SprinklerAcreage_JComboBox.setData ( SprinklerAcreage_Vector );
	__SprinklerAcreage_JComboBox.addActionListener (this);
	JGUIUtil.addComponent(main_JPanel, __SprinklerAcreage_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Default is AdjustSprinklerAcreageDownToCropPatternTotal." ),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 50 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel,new JScrollPane(__command_JTextArea),
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

	if (title != null) {
		setTitle (title);
	}
	// JDialogs do not need to be resizable...
	setResizable (false);
        pack();
        JGUIUtil.center(this);
	refresh();
        super.setVisible(true);
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
			response (1);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
synchronizeIrrigationPracticeAndCropPatternTS(ID="x",GWOnlyGWAcreage=X,
DivAndWellGWAcreage=X,SprinklerAcreage=X)
</pre>
*/
private void refresh ()
{	String routine ="synchronizeIrrigationPracticeAndCropPatternTS.refresh";
	String ID = "";
	String SynchronizeMethod = "";
	String GWOnlyGWAcreage = "";
	String DivAndWellGWAcreage = "";
	String SprinklerAcreage = "";
	__error_wait = false;
	if (__first_time) {
		__first_time = false;
		// Parse the incoming string and fill the fields...
		List v = StringUtil.breakStringList (
			((String)__command_Vector.get(0)).trim(),"()",
			StringUtil.DELIM_SKIP_BLANKS );
		PropList props = null;
		if ( (v != null) && (v.size() > 1) ) {
			props = PropList.parse (
				(String)v.get(1), routine, "," );
		}
		else {	props = new PropList ( routine );
		}
		ID = props.getValue ( "ID" );
		SynchronizeMethod = props.getValue ( "SynchronizeMethod" );
		GWOnlyGWAcreage = props.getValue ( "GWOnlyGWAcreage" );
		DivAndWellGWAcreage = props.getValue ( "DivAndWellGWAcreage" );
		SprinklerAcreage = props.getValue ( "SprinklerAcreage" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( SynchronizeMethod == null ) {
			// Select default...
			__SynchronizeMethod_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__SynchronizeMethod_JComboBox,
				SynchronizeMethod, JGUIUtil.NONE, null, null ) ) {
				__SynchronizeMethod_JComboBox.select (
				SynchronizeMethod );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing synchronizeIrrigationPracticeANdCropPatternTS() " +
				"references an invalid\n" +
				"SynchronizeMethod value \""+ SynchronizeMethod +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( GWOnlyGWAcreage == null ) {
			// Select default...
			__GWOnlyGWAcreage_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__GWOnlyGWAcreage_JComboBox,
				GWOnlyGWAcreage, JGUIUtil.NONE, null, null ) ) {
				__GWOnlyGWAcreage_JComboBox.select (
				GWOnlyGWAcreage );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing synchronizeIrrigationPracticeANdCropPatternTS() " +
				"references an invalid\n" +
				"GWOnlyGWAcreage value \""+ GWOnlyGWAcreage +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( DivAndWellGWAcreage == null ) {
			// Select default...
			__DivAndWellGWAcreage_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__DivAndWellGWAcreage_JComboBox,
				DivAndWellGWAcreage, JGUIUtil.NONE, null,null)){
				__DivAndWellGWAcreage_JComboBox.select (
				DivAndWellGWAcreage );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing synchronizeIrrigationPracticeANdCropPatternTS() " +
				"references an invalid\n" +
				"DivAndWellGWAcreage value \""+
				DivAndWellGWAcreage +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( SprinklerAcreage == null ) {
			// Select default...
			__SprinklerAcreage_JComboBox.select ( 0 );
		}
		else {	if (	JGUIUtil.isSimpleJComboBoxItem(
				__SprinklerAcreage_JComboBox,
				SprinklerAcreage, JGUIUtil.NONE, null,null)){
				__SprinklerAcreage_JComboBox.select (
				SprinklerAcreage );
			}
			else {	Message.printWarning ( 1, routine,
				"Existing synchronizeIrrigationPracticeANdCropPatternTS() " +
				"references an invalid\n" +
				"SprinklerAcreage value \""+ SprinklerAcreage +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	}

	// Always get the value that is selected...

	ID = __ID_JTextField.getText().trim();
	SynchronizeMethod = __SynchronizeMethod_JComboBox.getSelected();
	GWOnlyGWAcreage = __GWOnlyGWAcreage_JComboBox.getSelected();
	DivAndWellGWAcreage = __DivAndWellGWAcreage_JComboBox.getSelected();
	SprinklerAcreage = __SprinklerAcreage_JComboBox.getSelected();
	
	StringBuffer b = new StringBuffer ();
	if ( ID.length() > 0 ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( SynchronizeMethod.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SynchronizeMethod=" + SynchronizeMethod );
	}
	if ( GWOnlyGWAcreage.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "GWOnlyGWAcreage=" + GWOnlyGWAcreage );
	}
	if ( DivAndWellGWAcreage.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DivAndWellGWAcreage=" + DivAndWellGWAcreage );
	}
	if ( SprinklerAcreage.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SprinklerAcreage=" + SprinklerAcreage );
	}
	__command_JTextArea.setText(
		"synchronizeIrrigationPracticeAndCropPatternTS(" +
		b.toString()+")");
	__command_Vector.clear();
	__command_Vector.add (__command_JTextArea.getText());
}

/**
Return the time series command as a Vector of String.
@return returns the command text or null if no command.
*/
public List response (int status) {
	setVisible(false);
	dispose();
	if (status == 0) {
		// Cancel...
		__command_Vector = null;
		return null;
	}
	else {	refresh();
		if (	(__command_Vector.size() == 0) ||
			((String)__command_Vector.get(0)).equals("")) {
			return null;
		}
		return __command_Vector;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object 
*/
public void windowClosing(WindowEvent event) {
	response (0);
}

// The following methods are all necessary because this class
// implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
