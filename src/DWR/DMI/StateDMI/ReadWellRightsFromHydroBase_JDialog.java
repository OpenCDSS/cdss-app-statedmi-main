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
Editor for the ReadWellRightsFromHydroBase() command.
*/
public class ReadWellRightsFromHydroBase_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false; // Indicate whether OK has been pressed
private SimpleJComboBox __IDFormat_JComboBox = null;
private JTextField __ID_JTextField=null;
private JTextField __Year_JTextField = null;
private JTextField __Div_JTextField = null;
private JTextField __DecreeMin_JTextField=null;
private JTextField __DefaultAppropriationDate_JTextField = null;
private SimpleJComboBox __DefineRightHow_JComboBox = null;
private SimpleJComboBox __ReadWellRights_JComboBox = null;
private SimpleJComboBox __UseApex_JComboBox = null;
private SimpleJComboBox __OnOffDefault_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJComboBox __Optimization_JComboBox = null;
private ReadWellRightsFromHydroBase_Command __command = null;

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadWellRightsFromHydroBase_JDialog ( JFrame parent,	ReadWellRightsFromHydroBase_Command command )
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
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String Year = __Year_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	String DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	String ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	String UseApex = __UseApex_JComboBox.getSelected();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	String Optimization = __Optimization_JComboBox.getSelected();
	__error_wait = false;
	
	PropList props = new PropList ( "" );
	if ( ID.length() > 0 ) {
		props.set ( "ID", ID );
	}
	if ( IDFormat.length() > 0 ) {
		props.set ( "IDFormat", IDFormat );
	}
	if ( Year.length() > 0 ) {
		props.set ( "Year", Year );
	}
	if ( Div.length() > 0 ) {
		props.set ( "Div", Div );
	}
	if (DecreeMin.length() > 0 ) {
		props.set("DecreeMin", DecreeMin);
	}
	if ( DefaultAppropriationDate.length() > 0 ) {
		props.set ( "DefaultAppropriationDate", DefaultAppropriationDate );
	}
	if ( DefineRightHow.length() > 0 ) {
		props.set ( "DefineRightHow", DefineRightHow );
	}
	if ( ReadWellRights.length() > 0 ) {
		props.set ( "ReadWellRights", ReadWellRights );
	}
	if ( UseApex.length() > 0 ) {
		props.set ( "UseApex", UseApex );
	}
	if ( OnOffDefault.length() > 0 ) {
		props.set ( "OnOffDefault", OnOffDefault );
	}
	if ( Optimization.length() > 0 ) {
		props.set ( "Optimization", Optimization );
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
	String IDFormat = __IDFormat_JComboBox.getSelected();
	String Year = __Year_JTextField.getText().trim();
	String Div = __Div_JTextField.getText().trim();
	String DecreeMin = __DecreeMin_JTextField.getText().trim();
	String DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	String DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	String ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	String UseApex = __UseApex_JComboBox.getSelected();
	String OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	String Optimization = __Optimization_JComboBox.getSelected();

	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "IDFormat", IDFormat);
	__command.setCommandParameter ( "Year", Year );
	__command.setCommandParameter ( "Div", Div );
	__command.setCommandParameter ( "DecreeMin", DecreeMin);
	__command.setCommandParameter ( "DefaultAppropriationDate", DefaultAppropriationDate );
	__command.setCommandParameter ( "DefineRightHow", DefineRightHow );
	__command.setCommandParameter ( "ReadWellRights", ReadWellRights );
	__command.setCommandParameter ( "UseApex", UseApex );
	__command.setCommandParameter ( "OnOffDefault", OnOffDefault );
	__command.setCommandParameter ( "Optimization", Optimization );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__ID_JTextField = null;
	__IDFormat_JComboBox = null;
	__Year_JTextField = null;
	__Div_JTextField = null;
	__DefaultAppropriationDate_JTextField = null;
	__DefineRightHow_JComboBox = null;
	__ReadWellRights_JComboBox = null;
	__UseApex_JComboBox = null;
	__OnOffDefault_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, ReadWellRightsFromHydroBase_Command command )
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
    JGUIUtil.addComponent(paragraph, new JLabel (
		"This command reads well rights from HydroBase, using "+
		"the well station identifiers to find rights."),
		0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Water rights are determined from summarized well right " +
		"and permit data, which have been matched with wells and parcels."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"Summary data can be used as is, or well rights can " +
		"be requeried to obtain individual net amount rights."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"Alternate point or exchange (APEX) decrees provide additional rights."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"If the well rights are to be aggregates, use the AggregateWellRights() command to reduce" +
		" the number (but not decree sum) of rights in the model."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"See also MergeWellRights() command, which minimizes duplicate rights due to multiple parcel years."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       
	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Well station ID:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - well stations to read (use * for wildcard)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Water Division (Div):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Div_JTextField = new JTextField(10);
	__Div_JTextField.addKeyListener (this);
   	JGUIUtil.addComponent(main_JPanel, __Div_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - water division for the parcels."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Year:"),
    	0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	__Year_JTextField = new JTextField(10);
	__Year_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Year_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - year(s) for the parcels, separated by commas (default=all available)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
   	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Decree minimum:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DecreeMin_JTextField = new JTextField(10);
	__DecreeMin_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DecreeMin_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - minimum decree to include (default = .0005 CFS)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Right ID format:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List format_Vector = new Vector(3);
	format_Vector.add ( "" );
	format_Vector.add ( __command._HydroBaseID );
	format_Vector.add ( __command._StationIDW_NN );
	__IDFormat_JComboBox = new SimpleJComboBox(false);
	__IDFormat_JComboBox.setData ( format_Vector );
	__IDFormat_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __IDFormat_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - format for right identifiers (default=" + __command._StationIDW_NN + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Default appropriation date:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DefaultAppropriationDate_JTextField = new JTextField("",10);
	__DefaultAppropriationDate_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DefaultAppropriationDate_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - use if date is not available from right or permit (default=99999.99999 admin. num.)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Define right how?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List define_Vector = new Vector(4);
   	define_Vector.add ( "" );
	define_Vector.add ( "" + DefineWellRightHowType.EARLIEST_DATE );
	define_Vector.add ( "" + DefineWellRightHowType.LATEST_DATE );
	define_Vector.add ( "" + DefineWellRightHowType.RIGHT_IF_AVAILABLE );
	__DefineRightHow_JComboBox = new SimpleJComboBox(false);
	__DefineRightHow_JComboBox.setData ( define_Vector );
	__DefineRightHow_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __DefineRightHow_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - how to define right from HydroBase right/permit (default=" + DefineWellRightHowType.EARLIEST_DATE + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ("Read well rights?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List read_Vector = new Vector(3);
   	read_Vector.add ( "" );
	read_Vector.add ( __command._True );
	read_Vector.add ( __command._False );
	__ReadWellRights_JComboBox = new SimpleJComboBox(false);
	__ReadWellRights_JComboBox.setData ( read_Vector );
	__ReadWellRights_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __ReadWellRights_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - read well rights rather than relying on well matching results (default=" +
		__command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Use Apex?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List apex_Vector = new Vector(3);
   	apex_Vector.add ( "" );
	apex_Vector.add ( __command._True );
	apex_Vector.add ( __command._False );
	__UseApex_JComboBox = new SimpleJComboBox(false);
	__UseApex_JComboBox.setData ( apex_Vector );
	__UseApex_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __UseApex_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - add APEX amount to right amount (default=False)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "OnOff default:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List onoff_Vector = new Vector(3);
	onoff_Vector.add ( "" );
	onoff_Vector.add ( __command._1 );
	onoff_Vector.add ( __command._AppropriationDate );
	__OnOffDefault_JComboBox = new SimpleJComboBox(false);
	__OnOffDefault_JComboBox.setData ( onoff_Vector );
	__OnOffDefault_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __OnOffDefault_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - default StateMod OnOff switch (default=" + __command._AppropriationDate + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
   	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optimization level:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	List Optimization_Vector = new Vector(3);
	Optimization_Vector.add ( "" );
	Optimization_Vector.add ( __command._UseLessMemory );
	Optimization_Vector.add ( __command._UseMoreMemory );
	__Optimization_JComboBox = new SimpleJComboBox(false);
	__Optimization_JComboBox.setData ( Optimization_Vector );
	__Optimization_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __Optimization_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - optimize performance (default=" + __command._UseMoreMemory + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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
{	String routine = __command.getCommandName() + "_JDialog.refresh";
	String ID = "";
	String IDFormat = "";
	String Year = "";
	String Div = "";
	String DecreeMin = "";
	String DefaultAppropriationDate = "";
	String DefineRightHow = "";
	String ReadWellRights = "";
	String UseApex = "";
	String OnOffDefault = "";
	String Optimization = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
		ID = props.getValue ( "ID" );
		IDFormat = props.getValue ( "IDFormat" );
		Div = props.getValue ( "Div" );
		Year = props.getValue ( "Year" );
		if ( DecreeMin != null ) {
			__DecreeMin_JTextField.setText(DecreeMin);
		}
		DecreeMin = props.getValue ( "DecreeMin" );
		DefaultAppropriationDate = props.getValue ( "DefaultAppropriationDate" );
		DefineRightHow = props.getValue ( "DefineRightHow" );
		ReadWellRights = props.getValue ( "ReadWellRights" );
		UseApex = props.getValue ( "UseApex" );
		OnOffDefault = props.getValue ( "OnOffDefault" );
		Optimization = props.getValue ( "Optimization" );
		if ( ID != null ) {
			__ID_JTextField.setText(ID);
		}
		if ( IDFormat == null ) {
			// Select default...
			__IDFormat_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDFormat_JComboBox, IDFormat, JGUIUtil.NONE, null, null ) ) {
				__IDFormat_JComboBox.select ( IDFormat );
			}
			else {
				Message.printWarning ( 1, routine, "Existing command references an invalid IDFormat value \"" +
				IDFormat + "\".  Select a different value or Cancel.");
			}
		}
		if ( (Year != null) && (__Year_JTextField != null) ) {
			__Year_JTextField.setText(Year);
		}
		if ( (Div != null) && (__Div_JTextField != null) ) {
			__Div_JTextField.setText(Div);
		}
		if ( DefaultAppropriationDate != null ) {
			__DefaultAppropriationDate_JTextField.setText( DefaultAppropriationDate);
		}
		if ( DefineRightHow == null ) {
			// Select default (True)...
			__DefineRightHow_JComboBox.select ( __command._True );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__DefineRightHow_JComboBox, DefineRightHow, JGUIUtil.NONE, null, null ) ) {
				__DefineRightHow_JComboBox.select ( DefineRightHow );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid DefineRightHow " +
				"value \"" + DefineRightHow + "\".  Select a different value or Cancel.");
			}
		}
		if ( ReadWellRights == null ) {
			// Select default...
			__ReadWellRights_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__ReadWellRights_JComboBox, ReadWellRights, JGUIUtil.NONE, null, null ) ) {
				__ReadWellRights_JComboBox.select ( ReadWellRights );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid ReadWellRights " +
				"value \"" + ReadWellRights + "\".  Select a different value or Cancel.");
			}
		}
		if ( UseApex == null ) {
			// Select default...
			__UseApex_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__UseApex_JComboBox, UseApex, JGUIUtil.NONE, null, null ) ) {
				__UseApex_JComboBox.select ( UseApex );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid UseApex " +
				"value \"" + UseApex + "\".  Select a different value or Cancel.");
			}
		}
		if ( OnOffDefault == null ) {
			// Select default...
			__OnOffDefault_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__OnOffDefault_JComboBox, OnOffDefault, JGUIUtil.NONE, null, null ) ) {
				__OnOffDefault_JComboBox.select ( OnOffDefault );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid OnOffDefault " +
				"value \"" + OnOffDefault + "\".  Select a different value or Cancel.");
			}
		}
		if ( Optimization == null ) {
			// Select default...
			__Optimization_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Optimization_JComboBox, Optimization, JGUIUtil.NONE, null, null ) ) {
				__Optimization_JComboBox.select ( Optimization );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid Optimization " +
				"value \"" + Optimization + "\".  Select a different value or Cancel.");
			}
		}
	}

	// Always get the value that is selected...

	ID = __ID_JTextField.getText().trim();
	IDFormat = __IDFormat_JComboBox.getSelected();
	if ( __Year_JTextField != null ) {
		Year = __Year_JTextField.getText().trim();
	}
	if ( __Div_JTextField != null ) {
		Div = __Div_JTextField.getText().trim();
	}
	DecreeMin = __DecreeMin_JTextField.getText().trim();
	DefaultAppropriationDate = __DefaultAppropriationDate_JTextField.getText().trim();
	if ( __DefineRightHow_JComboBox != null ) {
		DefineRightHow = __DefineRightHow_JComboBox.getSelected();
	}
	if ( __ReadWellRights_JComboBox != null ) {
		ReadWellRights = __ReadWellRights_JComboBox.getSelected();
	}
	if ( __UseApex_JComboBox != null ) {
		UseApex = __UseApex_JComboBox.getSelected();
	}
	if ( __OnOffDefault_JComboBox != null ) {
		OnOffDefault = __OnOffDefault_JComboBox.getSelected();
	}
	if ( __Optimization_JComboBox != null ) {
		Optimization = __Optimization_JComboBox.getSelected();
	}
	props = new PropList ( "");
	props.add ( "ID=" + ID );
	props.add ( "IDFormat=" + IDFormat);
	props.add ( "Year=" + Year );
	props.add ( "Div=" + Div );
	props.add ( "DecreeMin=" + DecreeMin );
	props.add ( "DefaultAppropriationDate=" + DefaultAppropriationDate );
	props.add ( "DefineRightHow=" + DefineRightHow );
	props.add ( "ReadWellRights=" + ReadWellRights );
	props.add ( "UseApex=" + UseApex );
	props.add ( "OnOffDefault=" + OnOffDefault );
	props.add ( "Optimization=" + Optimization );
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