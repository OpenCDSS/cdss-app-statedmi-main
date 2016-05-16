// ----------------------------------------------------------------------------
// setWorkingDir_JDialog - editor for setWorkingDir()
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2002-09-16	J. Thomas Sapienza, RTi	Initial version from TSTool's
//					non-Swing version.
// 2002-09-20	JTS, RTi		FileDialog replaced with JFileChooser.
// 2002-09-25	JTS, RTi		Javadoc'd.
// 2002-10-03	JTS, RTi		Horizontal size reduced and help text
//					revised.
// 2002-10-07	JTS, RTi		Reincoporated use of the proplist.
// 2002-10-23	Steven A. Malers, RTi	Select only directories when browsing.
//					Update the comments to be more
//					informative.  Remove the choice - it is
//					not needed given the updates that have
//					occurred.
//					Use the new IOUtil.adjustPath() to
//					check the results of the command.
// 2004-02-19	SAM, RTi		Change GUIUtil to JGUIUtil.
// 2007-02-27	SAM, RTi		Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package DWR.DMI.StateDMI;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import java.io.File;
import java.util.List;

import RTi.Util.IO.PropList;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

public class SetWorkingDir_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
						
/**
true if waiting for an error to be cleared up before allowed proper
exit of a form
*/
private boolean		__errorWait = false;	

/**
Whether it's the first time refreshing the dialog
*/
private boolean		__firstTime = true;

/**
Browse for file
*/
private SimpleJButton	__browseJButton = null;

/**
working directory text field
*/
private JTextField	__dirJTextField = null;

/**
The text field to hold the command.
*/
private JTextField	__commandJTextField=null;

/**
Button to cancel out of the form.
*/
private SimpleJButton	__cancelJButton = null;

/**
Button to accept the entries on the form.
*/
private SimpleJButton	__okJButton = null;	

/**
Vector containing the command and parameters to be filled in on the form.
*/
private List		__commandVector = null;

private String __working_dir = null;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to parse.
@param data Additional data (currently not used).
*/
public SetWorkingDir_JDialog (JFrame parent, PropList props, 
		List command, List data) {
	// Call the full version with no title and ok JButton

	super(parent, true);
	initialize (parent, props, "Edit SetWorkingDir() Command", command,
		data);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	String s = event.getActionCommand();

	if (s.equals("Browse")) {
		JFileChooser fc = JFileChooserFactory.createJFileChooser(
			JGUIUtil.getLastFileDialogDirectory() );
		fc.setDialogTitle("Select the Working Directory" );
		fc.setFileSelectionMode ( JFileChooser.DIRECTORIES_ONLY );
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getPath();
			if (directory != null) {
				JGUIUtil.setLastFileDialogDirectory(directory);
			}
			__dirJTextField.setText (directory);
			refresh ();
		}
	}
	else if (s.equals("Cancel")) {
		response (0);
	}
	else if (s.equals("OK")) {
		refresh ();
		checkInput ();
		if (!__errorWait) {
			response (1);
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __errorWait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	String dir = __dirJTextField.getText();
	// Adjust the working directory that was passed in by the specified
	// directory.  If the directory does not exist, warn the user...
	try {	String adjusted_path = IOUtil.adjustPath ( __working_dir, dir );
		File f = new File ( adjusted_path );
		if ( !f.exists() ) {
			Message.printWarning ( 1, "setWorkingDir_JDialog",
			"The working directory does not exist:\n" +
			"    " + adjusted_path + "\n" +
		  	"Correct or Cancel." );
			__errorWait = true;
		}
		f = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "setWorkingDir_JDialog",
		"The working directory:\n" +
		"    \"" + __working_dir + "\"\ncannot be adjusted using:\n" +
		"    \"" + dir + "\".\n" +
		"Correct the directory or Cancel." );
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable {
	__browseJButton = null;
	__dirJTextField = null;
	__browseJButton = null;
	__cancelJButton = null;
	__commandJTextField = null;
	__commandVector = null;
	__okJButton = null;
	__working_dir = null;
	super.finalize ();
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the
command.
*/
public List getText () { 
	if (__commandVector == null) {
		return null;
	}
	if (	(__commandVector.size() == 0) ||
		((String)__commandVector.get(0)).equals("")) {
		return null;
	}
	return __commandVector;
}

/**
 * Instantiates the GUI components.
 * @param parent JFrame class instantiating this class.
 * @param title JDialog title.
 * @param command Vector of String containing the command.
 * @param data Additional data (not currently used).
 */
private void initialize (	JFrame parent, PropList props, String title, 
		List command, List data) {
	__commandVector = command;
	__working_dir = props.getValue ("WorkingDir");

	addWindowListener(this);

        Insets insetsTLBR = new Insets(1,2,1,2);

	JPanel mainJPanel = new JPanel();
	mainJPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", mainJPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());	
	int yy = 0;
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is the active directory for " +
		"StateDMI at the time a command is processed.  The working" + 
		" directory"),
		0, yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
       		JGUIUtil.addComponent(paragraph, new JLabel (
		"from startup, File...Set Working Directory, opening/saving " +
		"commands files, and previous setWorkingDir() commands is: "),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       		JGUIUtil.addComponent(paragraph, new JLabel (
		"    " + __working_dir ), 
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is prepended to relative paths found " +
		"in commands in order to find the files on the system."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"The setWorkingDir() command DOES NOT NEED TO BE USED if " +
		"input/output files can be located relative to a commands " +
		"file."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"If setWorkingDir() commands are used, use the following " +
		"guidelines:"),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        	JGUIUtil.addComponent(paragraph, new JLabel (""),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"   1) If the directory is specified as an absolute path, it " +
		"will reset the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"   2) If the directory is specified as a relative path, it " +
		"will append to the working directory."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"   3) If browsing for files for other commands, a path may " +
		"need to be edited to be relative (relative paths are " +
		"advised)."),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(paragraph, new JLabel (
		"   4) The working directory can be adjusted by specifying a" +
		" relative path like \"..\" and \"..\\somedir\"." ),
		0, ++yy, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(mainJPanel, paragraph,
		0, ++y, 8, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);	

        JGUIUtil.addComponent(mainJPanel, new JLabel ("Working Directory:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__dirJTextField = new JTextField (35);
	__dirJTextField.addKeyListener (this);
        JGUIUtil.addComponent(mainJPanel, __dirJTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browseJButton = new SimpleJButton ("Browse", "Browse", this);
        JGUIUtil.addComponent(mainJPanel, __browseJButton,
		7, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);

        JGUIUtil.addComponent(mainJPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__commandJTextField = new JTextField (40);
	__commandJTextField.setEditable (false);
	JGUIUtil.addComponent(mainJPanel, __commandJTextField,
		1, y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel buttonJPanel = new JPanel();
	buttonJPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(mainJPanel, buttonJPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__cancelJButton = new SimpleJButton("Cancel", "Cancel", this);
	buttonJPanel.add (__cancelJButton);
	__okJButton = new SimpleJButton("OK", "OK", this);
	buttonJPanel.add (__okJButton);

	if (title != null) {
		setTitle (title);
	}
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

	if ((code == KeyEvent.VK_ENTER) || (code == KeyEvent.VK_TAB)) {
		refresh ();
		checkInput();
		if (!__errorWait) {
			response (1);
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {;}

/**
Refresh the command from the other text field contents.  The command is
of the form:
<pre>
setWorkingDir("directory")
</pre>
*/
private void refresh () {
	String dir = "";
	__errorWait = false;
	if (__firstTime) {
		__firstTime = false;
		// Parse the incoming string and fill the fields...
		List v = StringUtil.breakStringList (
			((String)__commandVector.get(0)).trim(),"() ,",
			StringUtil.DELIM_SKIP_BLANKS |
			StringUtil.DELIM_ALLOW_STRINGS);
		if ((v != null) && (v.size() == 2)) {
			// Second field is directory...
			dir = ((String)v.get(1)).trim();
			__dirJTextField.setText (dir);
		}
	}
	// Regardless, reset the command from the fields...
	dir = __dirJTextField.getText().trim();
	__commandJTextField.setText("setWorkingDir(\"" + dir + "\")");
	__commandVector.clear();
	__commandVector.add (__commandJTextField.getText());
}

/**
Return the command as a Vector of String.
@return returns the command text or null if no command.
*/
public List response (int status) {
	setVisible(false);
	dispose();
	if (status == 0) {
		// Cancel...
		__commandVector = null;
		return null;
	}
	else {	refresh();
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

public void windowActivated(WindowEvent evt){;}
public void windowClosed(WindowEvent evt){;}
public void windowDeactivated(WindowEvent evt){;}
public void windowDeiconified(WindowEvent evt){;}
public void windowIconified(WindowEvent evt){;}
public void windowOpened(WindowEvent evt){;}

} // end setWorkingDir_JDialog
