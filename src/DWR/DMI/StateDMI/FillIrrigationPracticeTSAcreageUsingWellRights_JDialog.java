// FillIrrigationPracticeTSAcreageUsingWellRights_JDialog - Editor for the FillIrrigationPracticeTSAcreageUsingWellRights command.

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

/**
Editor for the FillIrrigationPracticeTSAcreageUsingWellRights command.
*/
@SuppressWarnings("serial")
public class FillIrrigationPracticeTSAcreageUsingWellRights_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false;	// Indicate whether OK has been pressed
private JTextField __ID_JTextField=null;
private SimpleJComboBox __IncludeGroundwaterOnlySupply_JComboBox = null;
private SimpleJComboBox __IncludeSurfaceWaterSupply_JComboBox = null;
// TODO SAM 2007-07-05 Remove if not needed after use
//private SimpleJComboBox	__DataType_JComboBox = null;
private JTextField __FillStart_JTextField = null;
private JTextField __FillEnd_JTextField = null;
private JTextField __ParcelYear_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private FillIrrigationPracticeTSAcreageUsingWellRights_Command		__command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillIrrigationPracticeTSAcreageUsingWellRights_JDialog ( JFrame parent,
	FillIrrigationPracticeTSAcreageUsingWellRights_Command command )
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
	String ID = __ID_JTextField.getText().trim();
	String IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
	String IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
	//String DataType = __DataType_JComboBox.getSelected();
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String ParcelYear = __ParcelYear_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__error_wait = false;
	
	PropList props = new PropList ( "" );
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( IncludeSurfaceWaterSupply.length() > 0 ) {
		props.set ( "IncludeSurfaceWaterSupply", IncludeSurfaceWaterSupply );
	}
	if ( IncludeGroundwaterOnlySupply.length() > 0 ) {
		props.set ( "IncludeGroundwaterOnlySupply", IncludeGroundwaterOnlySupply );
	}
	//if ( DataType.length() > 0 ) {
	//	props.set ( "DataType", DataType );
	//}
	if ( FillStart.length() > 0 ) {
		props.set ( "FillStart", FillStart );
	}
	if ( FillEnd.length() > 0 ) {
		props.set ( "FillEnd", FillEnd );
	}
	if ( ParcelYear.length() > 0 ) {
		props.set ( "ParcelYear", ParcelYear );
	}
    if ( IfNotFound.length() > 0 ) {
        props.set ( "IfNotFound", IfNotFound );
    }
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
	String ID = __ID_JTextField.getText().trim();
	String IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
	String IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
	//String DataType = __DataType_JComboBox.getSelected();
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String ParcelYear = __ParcelYear_JTextField.getText().trim();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "IncludeSurfaceWaterSupply", IncludeSurfaceWaterSupply);
	__command.setCommandParameter ( "IncludeGroundwaterOnlySupply", IncludeGroundwaterOnlySupply );
	//__command.setCommandParameter ( "DataType", DataType );
	__command.setCommandParameter ( "FillStart", FillStart );
	__command.setCommandParameter ( "FillEnd", FillEnd );
	__command.setCommandParameter ( "ParcelYear", ParcelYear );
    __command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, FillIrrigationPracticeTSAcreageUsingWellRights_Command command )
{	__command = command;

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
		"This command fills missing acreage data in irrigation practice time series " +
		"using well water rights, which have been read with a previous command."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Parcels for the specified year are used during the fill period and are turned on " +
		"if corresponding well water rights were active in the year."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
  	JGUIUtil.addComponent(paragraph, new JLabel (
		"The CU Location ID can contain a * wildcard pattern to match one or more time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The fill period can optionally be specified.  Only years in the output period can be filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(main_JPanel, new JLabel ("CU Location ID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU locations to process (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel data year:"),
 		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
 	__ParcelYear_JTextField = new JTextField (10);
 	__ParcelYear_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ParcelYear_JTextField,
 		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
 		"Required - 4-digit year for parcel data to turn acreage on/off."),
 		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST ); 
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Include surface water supply?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    List<String> IncludeSurfaceWaterSupply_Vector = new Vector<String>();
	IncludeSurfaceWaterSupply_Vector.add ( "" );
	IncludeSurfaceWaterSupply_Vector.add ( __command._False );
	IncludeSurfaceWaterSupply_Vector.add ( __command._True );
	__IncludeSurfaceWaterSupply_JComboBox = new SimpleJComboBox(false);
	__IncludeSurfaceWaterSupply_JComboBox.setData ( IncludeSurfaceWaterSupply_Vector );
	__IncludeSurfaceWaterSupply_JComboBox.addTextFieldKeyListener (this);
	__IncludeSurfaceWaterSupply_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IncludeSurfaceWaterSupply_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
	"Optional - include locations with surface water supply? (default=" + __command._True + ")."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );  
    
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Include groundwater only supply?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List<String> IncludeGroundwaterOnlySupply_Vector = new Vector<String>();
	IncludeGroundwaterOnlySupply_Vector.add ( "" );
	IncludeGroundwaterOnlySupply_Vector.add ( __command._False );
	IncludeGroundwaterOnlySupply_Vector.add ( __command._True );
	__IncludeGroundwaterOnlySupply_JComboBox = new SimpleJComboBox(false);
	__IncludeGroundwaterOnlySupply_JComboBox.setData ( IncludeGroundwaterOnlySupply_Vector );
	__IncludeGroundwaterOnlySupply_JComboBox.addTextFieldKeyListener (this);
	__IncludeGroundwaterOnlySupply_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IncludeGroundwaterOnlySupply_JComboBox,
	1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - include locations with only groundwater supply? (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Data Type:"),
       			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
       		Vector datatype_Vector =
       			StateCU_IrrigationPracticeTS.getTimeSeriesDataTypes (
       			false, false );
       		// FIXME SAM 2007-06-08 Update method to filter types instead of doing here
       		int size = datatype_Vector.size();
       		for ( int i = 0; i < size; i++ ) {
       			if ( ((String)datatype_Vector.elementAt(i)).indexOf("CropArea") < 0 ) {
       				datatype_Vector.removeElementAt(i--);
       				--size;
       			}
       		}
       		datatype_Vector.insertElementAt ( "", 0 );
       		__DataType_JComboBox = new SimpleJComboBox(false);
       		__DataType_JComboBox.setData ( datatype_Vector );
       		__DataType_JComboBox.addTextFieldKeyListener (this);
       		__DataType_JComboBox.addItemListener (this);
       	       	JGUIUtil.addComponent(main_JPanel, __DataType_JComboBox,
       		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	       	JGUIUtil.addComponent(main_JPanel, new JLabel (
       			"Time series data type to fill (blank for all acreage time series)."),
       			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	*/
     JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill start (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillStart_JTextField = new JTextField (10);
	__FillStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillStart_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - start year as 4-digits (default=fill all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fill end (year):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FillEnd_JTextField = new JTextField (10);
	__FillEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __FillEnd_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - end year as 4-digits (default=fill all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );    
    
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

	setTitle ( 	"Edit " + __command.getCommandName() + "() Command" );
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
	else {
		// One of the combo boxes...
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
{	String routine = __command.getCommandName() + "_JDialog.refresh";
	String ID = "";
	String IncludeSurfaceWaterSupply = "";
	String IncludeGroundwaterOnlySupply = "";
	//String DataType = "";
	String FillStart = "";
	String FillEnd = "";
	String ParcelYear =	"";
	String IfNotFound = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		ID = props.getValue ( "ID" );
		IncludeSurfaceWaterSupply = props.getValue ( "IncludeSurfaceWaterSupply" );
		IncludeGroundwaterOnlySupply = props.getValue ( "IncludeGroundwaterOnlySupply" );
		//DataType = props.getValue ( "DataType" );
		FillStart = props.getValue ( "FillStart" );
		FillEnd = props.getValue ( "FillEnd" );
		ParcelYear = props.getValue ( "ParcelYear" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( __IncludeSurfaceWaterSupply_JComboBox != null ) {
			if ( IncludeSurfaceWaterSupply == null ) {
				// Select default...
				__IncludeSurfaceWaterSupply_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeSurfaceWaterSupply_JComboBox,
					IncludeSurfaceWaterSupply, JGUIUtil.NONE, null, null ) ) {
					__IncludeSurfaceWaterSupply_JComboBox.select(IncludeSurfaceWaterSupply);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid IncludeSurfaceWaterSupply value \""+
					IncludeSurfaceWaterSupply + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( __IncludeGroundwaterOnlySupply_JComboBox != null ) {
			if ( IncludeGroundwaterOnlySupply == null ) {
				// Select default...
				__IncludeGroundwaterOnlySupply_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeGroundwaterOnlySupply_JComboBox,
					IncludeGroundwaterOnlySupply, JGUIUtil.NONE, null, null ) ) {
					__IncludeGroundwaterOnlySupply_JComboBox.select(IncludeGroundwaterOnlySupply);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid IncludeGroundwaterOnlySupply "
					+ "value \""+ IncludeGroundwaterOnlySupply + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		/*
		if ( __DataType_JComboBox != null ) {
			if ( DataType == null ) {
				// Select default...
				__DataType_JComboBox.select ( 0 );
			}
			else {	if (	JGUIUtil.isSimpleJComboBoxItem(
					__DataType_JComboBox,
					DataType, JGUIUtil.NONE, null,
					null ) ) {
					__DataType_JComboBox.select(
					DataType);
				}
				else {	Message.printWarning ( 1, routine,
					"Existing " + __command + "() " +
					"references an invalid DataType " +
					"value \""+ DataType + "\".  Select a "+
					"different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		*/
		if ( FillStart != null ) {
			__FillStart_JTextField.setText ( FillStart );
		}
		if ( FillEnd != null ) {
			__FillEnd_JTextField.setText ( FillEnd );
		}
		if ( (__ParcelYear_JTextField != null) && (ParcelYear != null) ) {
			__ParcelYear_JTextField.setText ( ParcelYear );
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

	ID = __ID_JTextField.getText().trim();
	IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
	IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
	//DataType = __DataType_JComboBox.getSelected();
	FillStart = __FillStart_JTextField.getText().trim();
	FillEnd = __FillEnd_JTextField.getText().trim();
	ParcelYear = __ParcelYear_JTextField.getText().trim();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	props.add ( "ID=" + ID );
	props.add ( "IncludeSurfaceWaterSupply=" + IncludeSurfaceWaterSupply);
	props.add ( "IncludeGroundwaterOnlySupply=" + IncludeGroundwaterOnlySupply );
	//props.add ( "DataType=" + DataType );
	props.add ( "FillStart=" + FillStart );
	props.add ( "FillEnd=" + FillEnd );
	props.add ( "ParcelYear=" + ParcelYear );
	props.add("IfNotFound=" + IfNotFound );
	
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
