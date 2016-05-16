package rti.tscommandprocessor.commands.check;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

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
import java.io.File;
import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for WriteCheckFile() command.
*/
public class WriteCheckFile_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;    
private JTextField __OutputFile_JTextField = null;
private JTextField __Title_JTextField = null;
private SimpleJComboBox __WriteHeaderComments_JComboBox = null;
private JTextArea __command_JTextArea=null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;  
private WriteCheckFile_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public WriteCheckFile_JDialog (JFrame parent, WriteCheckFile_Command command )
{   super(parent, true);
    initialize (parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{   Object o = event.getSource();

    if ( o == __browse_JButton ) {
        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
        JFileChooser fc = null;
        if ( last_directory_selected != null ) {
            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
        }
        else {
            fc = JFileChooserFactory.createJFileChooser(__working_dir );
        }
        SimpleFileFilter sffHtml = null;
        fc.setDialogTitle("Specify .html Check File to Write");
        sffHtml = new SimpleFileFilter("html", "Check File as HTML");
        fc.addChoosableFileFilter(sffHtml);
        
        fc.setDialogTitle("Specify .csv Check File to Write");
        SimpleFileFilter sffCsv = new SimpleFileFilter("csv", "Check File as CSV");
        fc.addChoosableFileFilter(sffCsv);

        fc.setFileFilter(sffHtml);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String directory = fc.getSelectedFile().getParent();
            String path = fc.getSelectedFile().getPath(); 
            JGUIUtil.setLastFileDialogDirectory(directory);
            __OutputFile_JTextField.setText(path);
            refresh();
        }   
    }
    else if ( o == __cancel_JButton ) {
        response (false);
    }
    else if ( o == __ok_JButton ) {
        refresh ();
        checkInput ();
        if (!__error_wait) {
            response (true);
        }
    }
    else if ( o == __path_JButton) {
        if (__path_JButton.getText().equals("Add Working Directory")) {
            __OutputFile_JTextField.setText (
            IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText()));
        }
        else if (__path_JButton.getText().equals("Remove Working Directory")) {
            try {
                __OutputFile_JTextField.setText (
                IOUtil.toRelativePath (__working_dir,__OutputFile_JTextField.getText()));
            }
            catch (Exception e) {
                Message.printWarning (1, __command + "_JDialog",
                "Error converting file to relative path.");
            }
        }
        refresh ();
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
    
    // Put together a list of parameters to check...
    PropList props = new PropList ( "" );
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String Title = __Title_JTextField.getText().trim();
    String WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
    
    if (OutputFile.length() > 0) {
        props.set("OutputFile", OutputFile);
    }
    if (Title.length() > 0) {
        props.set("Title", Title);
    }
    if ( (WriteHeaderComments != null) && (WriteHeaderComments.length() > 0) ) {
        props.set ( "WriteHeaderComments", WriteHeaderComments );
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
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits()
{
    String OutputFile = __OutputFile_JTextField.getText().trim();
    String Title = __Title_JTextField.getText().trim();
	String WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
    __command.setCommandParameter("OutputFile", OutputFile);
    __command.setCommandParameter("Title", Title);
	__command.setCommandParameter ( "WriteHeaderComments", WriteHeaderComments );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, WriteCheckFile_Command command )
{   __command = command;
    CommandProcessor processor = __command.getCommandProcessor();
    __working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

    addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

    // Main panel...

    JPanel main_JPanel = new JPanel();
    main_JPanel.setLayout(new GridBagLayout());
    getContentPane().add ("Center", main_JPanel);
    int y = 0;

    // Main contents...

    // Now add the buttons...

    JPanel paragraph = new JPanel();
    paragraph.setLayout(new GridBagLayout());
    int yy = -1;
    JGUIUtil.addComponent(paragraph, new JLabel (
        "This command writes command warning/failure messages to a check file, as a summary of data/processing problems."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Command messages generated during processing will be included in output."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Use commands such as CheckTimeSeries() to perform additional checks on specific data object types."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Specify an \"html\" extension for the output file to generate a navigable HTML file, or \"csv\" to " +
        "create a comma-separated value file."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, paragraph,
        0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Check (output) file:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __OutputFile_JTextField = new JTextField (35);
    __OutputFile_JTextField.setToolTipText("Specify the output file, can use ${Property} notation.");
    __OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
        1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    __browse_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
        6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Title:"),
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Title_JTextField = new JTextField (35);
    __Title_JTextField.setToolTipText("Specify the title for the HTML output, can use ${Property} notation.");
    __Title_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __Title_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - title for HTML output file."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Write header comments?:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __WriteHeaderComments_JComboBox = new SimpleJComboBox ( false );
    List<String> writeHeaderCommentsList = new Vector();
    writeHeaderCommentsList.add("");
    writeHeaderCommentsList.add(__command._False);
    writeHeaderCommentsList.add(__command._True);
    __WriteHeaderComments_JComboBox.setData ( writeHeaderCommentsList );
    __WriteHeaderComments_JComboBox.select(0);
    __WriteHeaderComments_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __WriteHeaderComments_JComboBox,
        1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Optional - should header comments be written? (default=" + __command._True + ")."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
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
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    if (__working_dir != null) {
        // Add the button to allow conversion to/from relative path...
        __path_JButton = new SimpleJButton("Remove Working Directory", this);
        button_JPanel.add (__path_JButton);
    }
    __cancel_JButton = new SimpleJButton("Cancel", this);
    button_JPanel.add (__cancel_JButton);
    __ok_JButton = new SimpleJButton("OK", this);
    button_JPanel.add (__ok_JButton);

    setTitle ( "Edit " + __command.getCommandName() + "() Command" );
    // JDialogs do not need to be resizable...
    setResizable ( false );
    pack();
    JGUIUtil.center(this);
    refresh();  // Sets the __path_JButton status
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
{   String routine = getClass().getSimpleName() + ".refresh";
	String OutputFile = "";
	String Title = "";
	String WriteHeaderComments = "";
    PropList props = null;
    
    if (__first_time) {
        __first_time = false;
    
        // Get the properties from the command
        props = __command.getCommandParameters();
        OutputFile = props.getValue ( "OutputFile" );
        WriteHeaderComments = props.getValue ( "WriteHeaderComments" );
        Title = props.getValue ( "Title" );
        if ( OutputFile != null ) {
            __OutputFile_JTextField.setText ( OutputFile );
        }
        if ( Title != null ) {
            __Title_JTextField.setText ( Title );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__WriteHeaderComments_JComboBox, WriteHeaderComments, JGUIUtil.NONE, null, null ) ) {
            __WriteHeaderComments_JComboBox.select ( WriteHeaderComments );
        }
        else {
            if ( (WriteHeaderComments == null) || WriteHeaderComments.equals("") ) {
                // New command...select the default...
                if ( __WriteHeaderComments_JComboBox.getItemCount() > 0 ) {
                    __WriteHeaderComments_JComboBox.select ( 0 );
                }
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid "+
                  "WriteHeaderComments parameter \"" + WriteHeaderComments + "\".  Select a different value or Cancel." );
            }
        }
    }
    // Regardless, reset the command from the fields...
    props = new PropList(__command.getCommandName());
    OutputFile = __OutputFile_JTextField.getText().trim();
    Title = __Title_JTextField.getText().trim();
    WriteHeaderComments = __WriteHeaderComments_JComboBox.getSelected();
    props.add("OutputFile=" + OutputFile);
    props.add("Title=" + Title);
    if ( WriteHeaderComments != null ) {
    	props.add ( "WriteHeaderComments=" + WriteHeaderComments );
    }
    __command_JTextArea.setText( __command.toString(props) );
    // Check the path and determine what the label on the path button should be...
    if (__path_JButton != null) {
        __path_JButton.setEnabled (true);
        File f = new File (OutputFile);
        if (f.isAbsolute()) {
            __path_JButton.setText ("Remove Working Directory");
        }
        else {
            __path_JButton.setText ("Add Working Directory");
        }
    }
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
public void windowActivated(WindowEvent evt)    {}
public void windowClosed(WindowEvent evt)   {}
public void windowDeactivated(WindowEvent evt)  {}
public void windowDeiconified(WindowEvent evt)  {}
public void windowIconified(WindowEvent evt)    {}
public void windowOpened(WindowEvent evt)   {}

}