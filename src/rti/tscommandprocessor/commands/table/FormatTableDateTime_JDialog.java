// FormatTableDateTime_JDialog - editor for FormatTableDateTime command

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

package rti.tscommandprocessor.commands.table;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTimeFormatterSpecifiersJPanel;
import RTi.Util.Time.DateTimeFormatterType;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.YearType;

@SuppressWarnings("serial")
public class FormatTableDateTime_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private FormatTableDateTime_Command __command = null;
private JTextArea __command_JTextArea = null;
private JTabbedPane __main_JTabbedPane = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __InputColumn_JTextField = null;
private JTextField __IncrementStart_JTextField = null;
private SimpleJComboBox __IncrementBaseUnit_JComboBox = null;
private DateTimeFormatterSpecifiersJPanel __DateTimeFormat_JPanel = null;
private SimpleJComboBox __OutputYearType_JComboBox = null;
private SimpleJComboBox __OutputColumn_JComboBox = null;
private SimpleJComboBox __OutputType_JComboBox = null;
private JTextField __InsertBeforeColumn_JTextField = null;
private boolean __error_wait = false;
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether OK button has been pressed.

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices choices for TableID value.
*/
public FormatTableDateTime_JDialog ( JFrame parent, FormatTableDateTime_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
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
		HelpViewer.getInstance().showHelp("command", "FormatTableDateTime");
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

//Start event handlers for DocumentListener...

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
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
    PropList parameters = new PropList ( "" );
    String TableID = __TableID_JComboBox.getSelected();
    String InputColumn = __InputColumn_JTextField.getText().trim();
    String IncrementStart = __IncrementStart_JTextField.getText().trim();
    String IncrementBaseUnit = __IncrementBaseUnit_JComboBox.getSelected();
    String FormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
    String DateTimeFormat =__DateTimeFormat_JPanel.getText().trim();
    String OutputYearType = __OutputYearType_JComboBox.getSelected();
    String OutputColumn = __OutputColumn_JComboBox.getSelected();
    String OutputType = __OutputType_JComboBox.getSelected();
    String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();

	__error_wait = false;

    if ( TableID.length() > 0 ) {
        parameters.set ( "TableID", TableID );
    }
    if ( InputColumn.length() > 0 ) {
        parameters.set ( "InputColumn", InputColumn );
    }
    if ( IncrementStart.length() > 0 ) {
        parameters.set ( "IncrementStart", IncrementStart );
    }
    if ( IncrementBaseUnit.length() > 0 ) {
        parameters.set ( "IncrementBaseUnit", IncrementBaseUnit );
    }
    if ( FormatterType.length() > 0 ) {
        parameters.set ( "FormatterType", FormatterType );
    }
    if ( DateTimeFormat.length() > 0 ) {
        parameters.set ( "DateTimeFormat", DateTimeFormat );
    }
    if ( OutputYearType.length() > 0 ) {
        parameters.set ( "OutputYearType", OutputYearType );
    }
    if ( OutputColumn.length() > 0 ) {
        parameters.set ( "OutputColumn", OutputColumn );
    }
    if ( OutputType.length() > 0 ) {
        parameters.set ( "OutputType", OutputType );
    }
    if ( InsertBeforeColumn.length() > 0 ) {
        parameters.set ( "InsertBeforeColumn", InsertBeforeColumn );
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
{	String TableID = __TableID_JComboBox.getSelected();
    String InputColumn = __InputColumn_JTextField.getText().trim();
    String IncrementStart = __IncrementStart_JTextField.getText().trim();
    String IncrementBaseUnit = __IncrementBaseUnit_JComboBox.getSelected();
    String FormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
    String DateTimeFormat =__DateTimeFormat_JPanel.getText().trim();
    String OutputYearType = __OutputYearType_JComboBox.getSelected();
    String OutputColumn = __OutputColumn_JComboBox.getSelected();
    String OutputType = __OutputType_JComboBox.getSelected();
    String InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "InputColumn", InputColumn );
    __command.setCommandParameter ( "IncrementStart", IncrementStart );
    __command.setCommandParameter ( "IncrementBaseUnit", IncrementBaseUnit );
    __command.setCommandParameter ( "FormatterType", FormatterType );
    __command.setCommandParameter ( "DateTimeFormat", DateTimeFormat );
    __command.setCommandParameter ( "OutputYearType", OutputYearType );
    __command.setCommandParameter ( "OutputColumn", OutputColumn );
    __command.setCommandParameter ( "OutputType", OutputType );
    __command.setCommandParameter ( "InsertBeforeColumn", InsertBeforeColumn );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param command The command to edit.
@param tableIDChoices list of choices for table identifiers
*/
private void initialize ( JFrame parent, FormatTableDateTime_Command command, List<String> tableIDChoices )
{	__command = command;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Format the contents of a date/time table input column to create values in the table output column." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This is helpful when a specific output format is needed." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
     
    // Panel for input
    int yIn = -1;
    JPanel in_JPanel = new JPanel();
    in_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Input", in_JPanel );
    
	JGUIUtil.addComponent(in_JPanel, new JLabel (
		"For simple formatting, the input column must have a type of Date, DateTime, or string that can be parsed to a date/time object." ), 
		0, ++yIn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(in_JPanel, new JLabel (
		"If a string, several standard formats are automatically recognized such as ISO YYYY-MM-DD hh:mm:ss and MM/DD/YYYY hh:mm:ss." ), 
		0, ++yIn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(in_JPanel, new JLabel (
		"The input column may also contain an integer offset from a starting date/time (see the Increment tab)." ), 
		0, ++yIn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(in_JPanel, new JLabel (
		"Once parsed, the date/time parts are used to reformat into the output column (see Format and Output tabs)." ), 
		0, ++yIn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(in_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
	    0, ++yIn, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(in_JPanel, new JLabel ( "Table ID:" ), 
        0, ++yIn, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table ID to process or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(in_JPanel, __TableID_JComboBox,
        1, yIn, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(in_JPanel, new JLabel( "Required - table to process."), 
        3, yIn, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(in_JPanel, new JLabel ( "Input column" ), 
        0, ++yIn, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputColumn_JTextField = new JTextField ( 30 );
    __InputColumn_JTextField.setToolTipText("Specify the input column name or use ${Property} notation");
    __InputColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(in_JPanel, __InputColumn_JTextField,
        1, yIn, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(in_JPanel, new JLabel("Required - name of date/time column to process."), 
        3, yIn, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for increment
    int yInc = -1;
    JPanel inc_JPanel = new JPanel();
    inc_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Increment", inc_JPanel );
    
	JGUIUtil.addComponent(inc_JPanel, new JLabel (
		"If the input column contains date/time increments, the input column must contain integers (e.g., number of hours since start)." ), 
		0, ++yInc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(inc_JPanel, new JLabel (
		"Also specify the start date/time for time 0." ), 
		0, ++yInc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(inc_JPanel, new JLabel (
		"Increments will be added to the starting date/time using the increment base unit to define the magnitude of the increment." ), 
		0, ++yInc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(inc_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
		0, ++yInc, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(inc_JPanel, new JLabel ( "Increment start:" ), 
        0, ++yInc, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncrementStart_JTextField = new JTextField ( 15 );
    __IncrementStart_JTextField.setToolTipText("Specify the starting date/time or use ${Property} notation");
    __IncrementStart_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(inc_JPanel, __IncrementStart_JTextField,
        1, yInc, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(inc_JPanel, new JLabel("Optional - starting date/time for time 0."), 
        3, yInc, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(inc_JPanel, new JLabel ( "Increment time base unit:" ), 
        0, ++yInc, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncrementBaseUnit_JComboBox = new SimpleJComboBox ( false );
    List<String> choices0 = TimeInterval.getTimeIntervalBaseChoices(TimeInterval.MINUTE, TimeInterval.YEAR, 1, false);
    choices0.add(0,"");
    __IncrementBaseUnit_JComboBox.setData(choices0);
    __IncrementBaseUnit_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(inc_JPanel, __IncrementBaseUnit_JComboBox,
        1, yInc, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(inc_JPanel, new JLabel(
        "Optional - increment time base unit (e.g. " + TimeInterval.getName(TimeInterval.HOUR,0) + "."), 
        3, yInc, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel for format information
    int yFormat = -1;
    JPanel format_JPanel = new JPanel();
    format_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Format", format_JPanel );

	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"The format string indicates how to format date/time parts (parsed from the input) into the output." ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"Currently only C-language style format is recognized, although other formatters may be added in the future." ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"See command documentation and specifier choices for an explanation of format specifiers, for example:" ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"  %Y - will output the year part of the date/time as a 4-digit year padded with zeroes" ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"  %m - will output the month part of the date/time as a 2-digit month number padded with zeroes" ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JLabel (
		"  literal text will be used as specified, for example dashes, colons, spaces, and other formatting characters" ), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(format_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
		0, ++yFormat, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JLabel DateTimeFormat_JLabel = new JLabel ("Date/time format:");
    JGUIUtil.addComponent(format_JPanel, DateTimeFormat_JLabel,
        0, ++yFormat, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DateTimeFormat_JPanel = new DateTimeFormatterSpecifiersJPanel(20,true,true,null,true,true);
    __DateTimeFormat_JPanel.getTextField().setToolTipText("Specify the date/time format or use ${Property} notation");
    __DateTimeFormat_JPanel.addKeyListener (this);
    __DateTimeFormat_JPanel.addFormatterTypeItemListener (this); // Respond to changes in formatter choice
    __DateTimeFormat_JPanel.getDocument().addDocumentListener(this); // Respond to changes in text field contents
    JGUIUtil.addComponent(format_JPanel, __DateTimeFormat_JPanel,
        1, yFormat, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(format_JPanel, new JLabel (
        "Required - to specify output format."),
        3, yFormat, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    // Panel for output
    int yOut = -1;
    JPanel out_JPanel = new JPanel();
    out_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Output", out_JPanel );

	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "The output column will contain the result of formatting the input date/time.  The output type can be set to:" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "    DateTime - if the resulting formatted string can be parsed to a date/time (e.g., with less precision than original)" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "                    (note TSTool by default displays all DateTime objets using ISO YYYY-MM-DD, etc. notation in tables)" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "    Double - if the resulting formatted string can be parsed to a floating point number (e.g., year only)" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "    Integer - if the resulting formatted string can be parsed to an integer (e.g., year only)" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JLabel (
        "    String - if the resulting formatted string should be treated as a literal string" ), 
        0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(out_JPanel, new JSeparator (SwingConstants.HORIZONTAL), 
		0, ++yOut, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(out_JPanel, new JLabel ( "OutputYearType:" ), 
        0, ++yOut, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputYearType_JComboBox = new SimpleJComboBox ( false );
    List<String> choices = YearType.getYearTypeChoicesAsStrings();
    choices.add(0,"");
    __OutputYearType_JComboBox.setData(choices);
    __OutputYearType_JComboBox.addItemListener(this);
    JGUIUtil.addComponent(out_JPanel, __OutputYearType_JComboBox,
        1, yOut, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(out_JPanel, new JLabel(
        "Optional - year type to interpret ${dt:YearForTypeYear} (default=" + YearType.CALENDAR + ")."), 
        3, yOut, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(out_JPanel, new JLabel ( "Output column:" ), 
        0, ++yOut, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputColumn_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __OutputColumn_JComboBox.setToolTipText("Specify the output column name or use ${Property} notation");
    List<String> outputChoices = new ArrayList<String>();
    outputChoices.add("");
    __OutputColumn_JComboBox.setData ( outputChoices ); // TODO SAM 2010-09-13 Need to populate via discovery
    __OutputColumn_JComboBox.addItemListener ( this );
    __OutputColumn_JComboBox.addKeyListener ( this );
    JGUIUtil.addComponent(out_JPanel, __OutputColumn_JComboBox,
        1, yOut, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(out_JPanel, new JLabel("Required - output column name."), 
        3, yOut, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(out_JPanel, new JLabel ( "Output type:" ), 
        0, ++yOut, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputType_JComboBox = new SimpleJComboBox ( false );
    List<String> typeChoices = new ArrayList<String>();
    typeChoices.add ( "" );
    typeChoices.add ( __command._DateTime );
    typeChoices.add ( __command._Double );
    typeChoices.add ( __command._Integer );
    typeChoices.add ( __command._String );
    __OutputType_JComboBox.setData(typeChoices);
    __OutputType_JComboBox.select ( __command._String );
    __OutputType_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(out_JPanel, __OutputType_JComboBox,
        1, yOut, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(out_JPanel, new JLabel(
        "Optional - specify output column type (default=" + __command._String + ")."), 
        3, yOut, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(out_JPanel, new JLabel ( "Insert before column:" ), 
        0, ++yOut, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertBeforeColumn_JTextField = new JTextField ( 30 );
    __InsertBeforeColumn_JTextField.setToolTipText("Specify the column name to insert before or use ${Property} notation");
    __InsertBeforeColumn_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(out_JPanel, __InsertBeforeColumn_JTextField,
        1, yOut, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(out_JPanel, new JLabel("Optional - column to insert before (default=at end)."), 
        3, yOut, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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

	setTitle ( "Edit " + __command.getCommandName() + " Command" );
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
{	checkGUIState();
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
    String TableID = "";
    String InputColumn = "";
    String IncrementStart = "";
    String IncrementBaseUnit = "";
    String FormatterType = "";
    String DateTimeFormat = "";
    String OutputYearType = "";
    String OutputColumn = "";
    String OutputType = "";
    String InsertBeforeColumn = "";

	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
	    TableID = props.getValue ( "TableID" );
        InputColumn = props.getValue ( "InputColumn" );
        IncrementStart = props.getValue ( "IncrementStart" );
        IncrementBaseUnit = props.getValue ( "IncrementBaseUnit" );
        FormatterType = props.getValue ( "FormatterType" );
        DateTimeFormat = props.getValue ( "DateTimeFormat" );
        OutputYearType = props.getValue ( "OutputYearType" );
		OutputColumn = props.getValue ( "OutputColumn" );
		OutputType = props.getValue ( "OutputType" );
		InsertBeforeColumn = props.getValue ( "InsertBeforeColumn" );
        if ( TableID == null ) {
            // Select default...
            __TableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableID_JComboBox,TableID, JGUIUtil.NONE, null, null ) ) {
                __TableID_JComboBox.select ( TableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTableID value \"" + TableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( InputColumn != null ) {
            __InputColumn_JTextField.setText ( InputColumn );
        }
        if ( IncrementStart != null ) {
            __IncrementStart_JTextField.setText ( IncrementStart );
        }
        if ( IncrementBaseUnit == null ) {
            // Select default...
            __IncrementBaseUnit_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __IncrementBaseUnit_JComboBox,IncrementBaseUnit, JGUIUtil.NONE, null, null ) ) {
                __IncrementBaseUnit_JComboBox.select ( IncrementBaseUnit );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nIncrementBaseUnit value \"" + IncrementBaseUnit +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( (FormatterType == null) || FormatterType.equals("") ) {
            // Select default...
            __DateTimeFormat_JPanel.selectFormatterType(null);
        }
        else {
            try {
                __DateTimeFormat_JPanel.selectFormatterType(DateTimeFormatterType.valueOfIgnoreCase(FormatterType));
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nFormatterType value \"" + FormatterType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if (DateTimeFormat != null) {
            // The front part of the string may match a formatter type (e.g., "C:") in which case
            // only the latter part of the string should be displayed.
            int pos = DateTimeFormat.indexOf(":");
            if ( pos > 0 ) {
                try {
                    __DateTimeFormat_JPanel.selectFormatterType(
                        DateTimeFormatterType.valueOfIgnoreCase(DateTimeFormat.substring(0,pos)));
                    Message.printStatus(2, routine, "Selecting format \"" + DateTimeFormat.substring(0,pos) + "\"");
                    if ( DateTimeFormat.length() > pos ) {
                        __DateTimeFormat_JPanel.setText(DateTimeFormat.substring(pos + 1));
                    }
                }
                catch ( IllegalArgumentException e ) {
                    __DateTimeFormat_JPanel.setText(DateTimeFormat);
                }
            }
            else {
                __DateTimeFormat_JPanel.setText(DateTimeFormat);
            }
        }
        if ( OutputYearType == null ) {
            // Select default...
            __OutputYearType_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputYearType_JComboBox,OutputYearType, JGUIUtil.NONE, null, null ) ) {
                __OutputYearType_JComboBox.select ( OutputYearType );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nOutputYearType value \"" + OutputYearType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( OutputColumn == null ) {
            // Select default...
            __OutputColumn_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputColumn_JComboBox,OutputColumn, JGUIUtil.NONE, null, null ) ) {
                __OutputColumn_JComboBox.select ( OutputColumn );
            }
            else {
                // Just set the user-specified value
                __OutputColumn_JComboBox.setText( OutputColumn );
            }
        }
        if ( OutputType == null ) {
            // Select default...
            __OutputType_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputType_JComboBox,OutputType, JGUIUtil.NONE, null, null ) ) {
                __OutputType_JComboBox.select ( OutputType );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nOutputType value \"" + OutputType +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( InsertBeforeColumn != null ) {
            __InsertBeforeColumn_JTextField.setText ( InsertBeforeColumn );
        }
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
	InputColumn = __InputColumn_JTextField.getText();
	IncrementStart = __IncrementStart_JTextField.getText();
	IncrementBaseUnit = __IncrementBaseUnit_JComboBox.getSelected();
    FormatterType = __DateTimeFormat_JPanel.getSelectedFormatterType().trim();
	DateTimeFormat = __DateTimeFormat_JPanel.getText().trim();
	OutputYearType = __OutputYearType_JComboBox.getSelected();
    OutputColumn = __OutputColumn_JComboBox.getSelected();
    OutputType = __OutputType_JComboBox.getSelected();
    InsertBeforeColumn = __InsertBeforeColumn_JTextField.getText();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "InputColumn=" + InputColumn );
    props.add ( "IncrementStart=" + IncrementStart );
    props.add ( "IncrementBaseUnit=" + IncrementBaseUnit );
    props.add ( "FormatterType=" + FormatterType );
    props.add ( "DateTimeFormat=" + DateTimeFormat );
    props.add ( "OutputYearType=" + OutputYearType );
    props.add ( "OutputColumn=" + OutputColumn );
    props.add ( "OutputType=" + OutputType );
    props.add ( "InsertBeforeColumn=" + InsertBeforeColumn );
	__command_JTextArea.setText( __command.toString ( props ) );
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
