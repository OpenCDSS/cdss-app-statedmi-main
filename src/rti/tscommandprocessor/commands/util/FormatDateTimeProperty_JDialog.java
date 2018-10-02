package rti.tscommandprocessor.commands.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import DWR.DMI.StateDMI.StateDMI_Processor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormatterSpecifiersJPanel;
import RTi.Util.Time.DateTimeFormatterType;

@SuppressWarnings("serial")
public class FormatDateTimeProperty_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private FormatDateTimeProperty_Command __command = null;
private JTextArea __command_JTextArea = null;
private JTextField __PropertyName_JTextField = null;
private SimpleJComboBox __DateTimePropertyName_JComboBox = null;
//private SimpleJComboBox __FormatterType_JComboBox = null;
private DateTimeFormatterSpecifiersJPanel __Format_JPanel = null;
private SimpleJComboBox __PropertyType_JComboBox = null;
private boolean __error_wait = false; // Is there an error to be cleared up or Cancel?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether OK button has been pressed.

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FormatDateTimeProperty_JDialog ( JFrame parent, FormatDateTimeProperty_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "FormatDateTimeProperty");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

// Start event handlers for DocumentListener...

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void changedUpdate ( DocumentEvent e )
{   checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void insertUpdate ( DocumentEvent e )
{   checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void removeUpdate ( DocumentEvent e )
{   checkGUIState();
    refresh();
}

// ...End event handlers for DocumentListener

/**
Check the GUI state to make sure that appropriate components are enabled/disabled.
*/
private void checkGUIState ()
{
    // TODO SAM 2008-01-30 Anything to do?
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList parameters = new PropList ( "" );
	String PropertyName = __PropertyName_JTextField.getText().trim();
	String DateTimePropertyName = __DateTimePropertyName_JComboBox.getSelected();
    if ( DateTimePropertyName == null ) {
        DateTimePropertyName = "";
    }
    String FormatterType = __Format_JPanel.getSelectedFormatterType().trim();
	String Format = __Format_JPanel.getText().trim();
	String PropertyType = __PropertyType_JComboBox.getSelected();

	__error_wait = false;

	if ( PropertyName.length() > 0 ) {
	    parameters.set ( "PropertyName", PropertyName );
	}
    if ( DateTimePropertyName.length() > 0 ) {
        parameters.set ( "DateTimePropertyName", DateTimePropertyName );
    }
    if ( FormatterType.length() > 0 ) {
        parameters.set ( "FormatterType", FormatterType );
    }
	if ( Format.length() > 0 ) {
		parameters.set ( "Format", Format );
	}
    if ( PropertyType.length() > 0 ) {
        parameters.set ( "PropertyType", PropertyType );
    }

	try {
	    // This will warn the user...
		__command.checkCommandParameters ( parameters, null, 1 );
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
private void commitEdits ()
{	String PropertyName = __PropertyName_JTextField.getText().trim();
    String DateTimePropertyName = __DateTimePropertyName_JComboBox.getSelected();
    if ( DateTimePropertyName == null ) {
        DateTimePropertyName = "";
    }
    String FormatterType = __Format_JPanel.getSelectedFormatterType().trim(); 
	String Format = __Format_JPanel.getText().trim();
	String PropertyType = __PropertyType_JComboBox.getSelected();
	__command.setCommandParameter ( "PropertyName", PropertyName );
    __command.setCommandParameter ( "DateTimePropertyName", DateTimePropertyName );
    __command.setCommandParameter ( "FormatterType", FormatterType );
    __command.setCommandParameter ( "Format", Format );
    __command.setCommandParameter ( "PropertyType", PropertyType );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param command The command to edit.
*/
private void initialize ( JFrame parent, FormatDateTimeProperty_Command command )
{	__command = command;
    CommandProcessor processor = __command.getCommandProcessor();

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Format a date/time property to create a new string property." ), 
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The property can be referenced in parameters of some commands using ${Property} notation." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "For example, use the string property to create file names that include date/time information." ), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Property name:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PropertyName_JTextField = new JTextField ( 20 );
    __PropertyName_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __PropertyName_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Required - new property (do not use spaces $, { or } in name)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Date/time property name:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimePropertyName_JComboBox = new SimpleJComboBox(false);
    List<Prop> propertyList = TSCommandProcessorUtil.getDiscoveryPropFromCommandsBeforeCommand((StateDMI_Processor)processor,__command);
    List<String> propertyNameList = new ArrayList<String>();
    // Remove all but DateTime instances
    Object property;
    for ( Prop prop: propertyList ) {
        try {
        	// TODO SAM 2017-03-26 this was problematic because it used runtime data
            //property = processor.getPropContents(propertyNameList.get(i));
        	property = prop.getContents();
        }
        catch ( Exception e ) {
            property = null;
        }
        if ( (property != null) && (property instanceof DateTime) ) {
            propertyNameList.add(prop.getKey());
        }
    }
    __DateTimePropertyName_JComboBox.setData ( propertyNameList );
    __DateTimePropertyName_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __DateTimePropertyName_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Required - existing date/time property to format."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // TODO SAM 2012-04-10 Evaluate whether the formatter should just be the first part of the format, which
    // is supported by the panel
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Format:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Format_JPanel = new DateTimeFormatterSpecifiersJPanel ( 20, true, true, null, true, false );
	__Format_JPanel.addKeyListener ( this );
	__Format_JPanel.addFormatterTypeItemListener (this); // Respond to changes in formatter choice
	__Format_JPanel.getDocument().addDocumentListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Format_JPanel,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - format string for formatter."), 
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Property type:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PropertyType_JComboBox = new SimpleJComboBox ( false );
    List<String> typeChoices = new ArrayList<String>();
    typeChoices.add ( "" ); // Default is string
    typeChoices.add ( __command._DateTime );
    typeChoices.add ( __command._Double );
    typeChoices.add ( __command._Integer );
    typeChoices.add ( __command._String );
    __PropertyType_JComboBox.setData(typeChoices);
    __PropertyType_JComboBox.select ( __command._String );
    __PropertyType_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __PropertyType_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - output property type (default=" + __command._String + ")."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 55 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
    checkGUIState();
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
    pack();
    JGUIUtil.center( this );
	setResizable ( false );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{
    checkGUIState();
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else {
	    // Combo box...
		refresh();
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

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
{	String routine = getClass().getSimpleName() + ".refresh";
    String PropertyName = "";
    String DateTimePropertyName = "";
    String FormatterType = "";
	String Format = "";
    String PropertyType = "";
	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		PropertyName = props.getValue ( "PropertyName" );
		DateTimePropertyName = props.getValue ( "DateTimePropertyName" );
        FormatterType = props.getValue ( "FormatterType" );
		Format = props.getValue ( "Format" );
		PropertyType = props.getValue ( "PropertyType" );
	    if ( PropertyName != null ) {
	         __PropertyName_JTextField.setText ( PropertyName );
	    }
        if ( DateTimePropertyName == null ) {
            // Select default...
            if ( __DateTimePropertyName_JComboBox.getItemCount() == 0 ) {
                __DateTimePropertyName_JComboBox.select ( null );
            }
            else {
                __DateTimePropertyName_JComboBox.select ( 0 );
            }
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(
                __DateTimePropertyName_JComboBox, DateTimePropertyName, JGUIUtil.NONE, null, null ) ) {
                __DateTimePropertyName_JComboBox.select ( DateTimePropertyName );
            }
            else {
                Message.printWarning ( 1, routine, "Existing command " +
                "references an invalid\nDateTimePropertyName value \"" +
                DateTimePropertyName + "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( (FormatterType == null) || FormatterType.equals("") ) {
            // Select default...
            __Format_JPanel.selectFormatterType(null);
        }
        else {
            try {
                __Format_JPanel.selectFormatterType(DateTimeFormatterType.valueOfIgnoreCase(FormatterType));
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nFormatterType value \"" + FormatterType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
		if ( Format != null ) {
		    __Format_JPanel.setText ( Format );
		}
        if ( PropertyType == null ) {
            // Select default...
            __PropertyType_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __PropertyType_JComboBox,PropertyType, JGUIUtil.NONE, null, null ) ) {
                __PropertyType_JComboBox.select ( PropertyType );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nPropertyType value \"" + PropertyType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
	}
	// Regardless, reset the command from the fields...
	PropertyName = __PropertyName_JTextField.getText().trim();
	DateTimePropertyName = __DateTimePropertyName_JComboBox.getSelected();
    if ( DateTimePropertyName == null ) {
        DateTimePropertyName = "";
    }
    FormatterType = __Format_JPanel.getSelectedFormatterType().trim();
	Format = __Format_JPanel.getText().trim();
	PropertyType = __PropertyType_JComboBox.getSelected();
	props = new PropList ( __command.getCommandName() );
	props.add ( "PropertyName=" + PropertyName );
	props.add ( "DateTimePropertyName=" + DateTimePropertyName );
    props.add ( "FormatterType=" + FormatterType );
	props.add ( "Format=" + Format );
	props.add ( "PropertyType=" + PropertyType );
	__command_JTextArea.setText( __command.toString ( props ) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
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
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}