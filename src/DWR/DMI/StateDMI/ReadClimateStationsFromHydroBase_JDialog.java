// ReadClimateStationsFromHydroBase_JDialog - editor for ReadClimateStationsFromHydroBase command

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
// readClimateStationsFromHydroBase_JDialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2002-09-30	J. Thomas Sapienza, RTi	Initial version from 
//					readCULocations_JDialog.java
// 2003-02-20	Steven A. Malers	Rename from
//				readCUClimateWeightsFromHydroBase_JDialog.
// 2003-02-21	SAM, RTi		Final cleanup.
// 2003-05-09	SAM, RTi		Change readCUClimateStationWeights to
//					readClimateStations.
// 2005-10-10	SAM, RTi		Use a text area for the command.
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

import java.awt.Color;
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

import RTi.Util.GUI.GUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

@SuppressWarnings("serial")
public class ReadClimateStationsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener {
/**
True if there is an error that needs to be cleared up or cancelled.
*/
private boolean __errorWait = false;
/**
Whether it is the first time refreshing the dialog
*/
private boolean __firstTime = true;

/**
The text field to hold the command.
*/
private JTextArea __command_JTextArea=null;

private JTextField __region1JTextField = null; 
private JTextField __region2JTextField = null; 

/**
Button to cancel out of the form.
*/
private SimpleJButton __cancelJButton = null;
/**
Button to accept the entries on the form.
*/
private SimpleJButton __okJButton = null;	

private SimpleJButton __helpJButton = null;	

/**
List containing the command and parameters to be filled in on the form.
*/
private List<String> __commandVector = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param props Properties from the application.
@param command Time series command to parse.
*/
public ReadClimateStationsFromHydroBase_JDialog (JFrame parent,
							PropList props,
							List<String> command)
{	super(parent, true);
	initialize (parent,
		"Edit ReadClimateStationsFromHydroBase() Command", 
		props, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();
	Object o = event.getSource();

	if (s.equals("Cancel")) {
		response (0);
	}
	else if ( o == __helpJButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadClimateStationsFromHydroBase");
	}
	else if (s.equals("OK")) {
		refresh ();
		if (!__errorWait) {
			response (1);
		}
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__cancelJButton = null;
	__command_JTextArea = null;
	__commandVector = null;
	__okJButton = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the 
command.
*/
public List<String> getText () {
	if ((__commandVector != null) && ((__commandVector.size() == 0) ||
		__commandVector.get(0).equals(""))) {
		return null;
	}
	return __commandVector;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title JDialog title.
@param app_PropList Properties from the application.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, String title, PropList props, List<String> command)
{	__commandVector = command;

	addWindowListener(this);

        Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel mainJPanel = new JPanel();
	mainJPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", mainJPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
        GUIUtil.addComponent(paragraph, new JLabel (
		"This command reads the climate stations for CU " +
		"regions from HydroBase.  The regions should be consistent"),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        GUIUtil.addComponent(paragraph, new JLabel (
		"with those used in the CU Locations file."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        GUIUtil.addComponent(paragraph, new JLabel (
		"Although sub-sets of the HydroBase data can be read, it is " +
		"also OK to read the data for the entire state."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        GUIUtil.addComponent(paragraph, new JLabel (
		"Specify Region 1 as a county and Region 2 as a HUC or use " +
		"* wildcards to match multiple regions.  Use * to read all"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        GUIUtil.addComponent(paragraph, new JLabel (
		"available data."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	GUIUtil.addComponent(mainJPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

        GUIUtil.addComponent(mainJPanel, new JLabel ("Region 1:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__region1JTextField = new JTextField (10);
	__region1JTextField.addKeyListener (this);
        GUIUtil.addComponent(mainJPanel, __region1JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
		
        GUIUtil.addComponent(mainJPanel, new JLabel ("Region 2:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__region2JTextField = new JTextField (10);
	__region2JTextField.addKeyListener (this);
        GUIUtil.addComponent(mainJPanel, __region2JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        GUIUtil.addComponent(mainJPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	GUIUtil.addComponent(mainJPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel buttonJPanel = new JPanel();
	buttonJPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        GUIUtil.addComponent(mainJPanel, buttonJPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__okJButton = new SimpleJButton("OK", "OK", this);
	buttonJPanel.add (__okJButton);
	__cancelJButton = new SimpleJButton("Cancel", "Cancel", this);
	buttonJPanel.add (__cancelJButton);
	buttonJPanel.add ( __helpJButton = new SimpleJButton("Help", this) );
	__helpJButton.setToolTipText("Show command documentation in web browser");

	setBackground(Color.lightGray);
	if (title != null) {
		setTitle (title);
	}
	// JDialogs do not need to be resizable...
	setResizable (true);
        pack();
        GUIUtil.center(this);
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
		if (!__errorWait) {
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
readClimateStationWeightsFromHydroBase()
</pre>
*/
private void refresh () {
	String r1 = "";
	String r2 = "";
	__errorWait = false;
	if (__firstTime) {
		__firstTime = false;
		// Parse the incoming string and fill the fields...
		List<String> v = StringUtil.breakStringList (
			__commandVector.get(0).trim(),"() ,",
			StringUtil.DELIM_SKIP_BLANKS |
			StringUtil.DELIM_ALLOW_STRINGS);
		if ((v != null) && (v.size() == 3)) {
			// Second field is file...
			r1 = ((String)v.get(1)).trim();
			__region1JTextField.setText (r1);
			r2 = ((String)v.get(2)).trim();
			__region2JTextField.setText (r2);			
		}
		else {
			__region1JTextField.setText("*");
			__region2JTextField.setText("*");
		}
	}
	// Regardless, reset the command from the fields...
	r1 = __region1JTextField.getText();
	if ((r1 == null) || (r2.trim().length() == 0)) {
		r1 = "*";
	}
	r2 = __region2JTextField.getText();
	if ((r2 == null) || (r2.trim().length() == 0)) {
		r2 = "*";
	}
	__command_JTextArea.setText("readClimateStationsFromHydroBase(*, *)");
	__commandVector.clear();
	__commandVector.add (__command_JTextArea.getText());
	// Check the path and determine what the label on the path button should
	// be...
}

/**
Return the command as a Vector of String.
@return returns the command text or null if no command.
*/
public List<String> response (int status) {
	setVisible(false);
	dispose();
	if (status == 0) {
		// Cancel...
		__commandVector = null;
		return null;
	}
	else {	
		refresh();
		if (	(__commandVector.size() == 0) ||
			((String)__commandVector.get(0)).equals("")) {
			return null;
		}
		return __commandVector;
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

} // End readClimateStationsFromHydroBase_JDialog 
