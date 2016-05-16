package rti.tscommandprocessor.commands.util;

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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.util.List;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.String.StringUtil;

/**
Editor for # comments.
*/
public class Comment_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private List<String> __commandList = null;
private JTextArea __command_JTextArea = null;
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether the user has pressed OK to close the dialog.

/**
Comment editor constructor.
@param parent JFrame class instantiating this class.
@param comments Comments to parse
*/
public Comment_JDialog ( JFrame parent, List<String> comments )
{	super(parent, true);
	initialize ( parent, comments );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		__commandList = null;
		response ( 0 );
	}
	else if ( o == __ok_JButton ) {
		refresh();
		response ( 1 );
	}
}

/**
Convert text area to comments.
*/
private void checkComments ()
{	// Reset the command from the fields...
	__commandList = JGUIUtil.toList( __command_JTextArea );
	// Make sure there is a # character at the front of each line...
	int size = 0;
	if ( __commandList != null ) {
		size = __commandList.size();
	}
	String s = null;
	for ( int i = 0; i < size; i++ ) {
		s = ((String)__commandList.get(i)).trim();
		if ( !s.startsWith("#") ) {
			// Replace with a new string that has the comment character...
			__commandList.remove(i);
			__commandList.add(i,("# " + s));
		}
		// Make sure there are no CTRL-M (^M) carriage returns in the string.
		// If so, remove because the CTRL-N (newline) is enough for other code
		// to handle.
		if ( s.indexOf("\015") > 0 ) {
			// Replace with a new string that does not have the carriage returns.
			String s2 = StringUtil.remove ( s, "\015" );
			__commandList.remove(i);
			__commandList.add(i,s2);
		}
	}
}

/**
Return the text for the command.
@return the text for the command or null if there is a problem with the command.
*/
public List getText ()
{	if ( __commandList == null ) {
		// Indicates a cancel...
		return null;
	}
	// Else, OK to process the comments...
	checkComments ();
	if ( (__commandList == null) || (__commandList.size() == 0) || ((String)__commandList.get(0)).equals("") ) {
		__commandList = null;
	}
	return __commandList;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param comments Comments to parse
*/
private void initialize ( JFrame parent, List<String> comments )
{	__commandList = comments;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(7,2,7,2);
    Insets insetsXLXX = new Insets(0,2,0,0);
    Insets insets2 = new Insets(2,2,2,2);
    Insets insetsXLBR = new Insets(0,2,7,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	// Main contents...

	// Now add the buttons...

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Enter one or more comments (leading # will be added automatically if not shown)." ), 
		0, y, 7, 1, 0, 0, insets2, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "See also the /* and */ commands for multi-line comments, which are useful for commenting out multiple commands." ), 
        0, ++y, 7, 1, 0, 0, insets2, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JTextArea ref_JTextArea = new JTextArea (2, 80);
    // Add a string buffer with reference positions (similar to UltraEdit Editor)
    //0        10        20
    //12345678901234567890...
    int n10 = 12; // number to repeat.  Want to make 20 but this causes layout issues.
    // TODO SAM 2007-04-22 REVISIT layout for n10=20
    StringBuffer b = new StringBuffer();
    b.append ( StringUtil.formatString(0,"%-9d"));
    for ( int i = 1; i < n10; i++ ) {
    	b.append( StringUtil.formatString(i*10,"%-10d"));
    }
    b.append ( "\n");
    b.append ( "1234567890");
    for ( int i = 1; i < n10; i++ ) {
    	b.append( "1234567890");
    }
    ref_JTextArea.setText( b.toString() );
	ref_JTextArea.setEditable (false);
	ref_JTextArea.setEnabled ( false );
	JGUIUtil.addComponent(main_JPanel, ref_JTextArea,
		1, ++y, 6, 1, 1, 1, insetsXLXX, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Comments:" ), 
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    __command_JTextArea = new JTextArea ( 10, 80 );
	__command_JTextArea.setEditable ( true );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 1, insetsXLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add ( __ok_JButton );

	setTitle ( "Edit # Comments" );
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	// Want user to press OK to continue (not enter because newlines may be allowed).
}

public void keyReleased ( KeyEvent event )
{	// Nothing to do	
}

public void keyTyped ( KeyEvent event )
{
}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	if ( __first_time ) {
		__first_time = false;
		// Fill the text area with the command text.  If a list with
		// one blank string, don't do the following because it results
		// in a blank line and the user must back up to edit the blank.
		if ( (__commandList != null) && (__commandList.size() > 0) &&
			(((String)__commandList.get(0)).length() > 0)){
			String text = StringUtil.toString(__commandList,	System.getProperty("line.separator") );
			if ( text.length() > 0 ) {
				__command_JTextArea.setText ( text );
			}
		}
	}
}

/**
Return the time series command as a Vector of String.
@param status 0 to cancel, 1 is OK.
@return returns the command text or null if no command.
*/
public List response ( int status )
{	setVisible( false );
	dispose();
	if ( status == 0 ) {
		// Cancel...
		__commandList = null;
		return null;
	}
	else {
	    __ok = true;
	    refresh();
		checkComments ();
		if ( (__commandList.size() == 0) || ((String)__commandList.get(0)).equals("") ) {
			return null;
		}
		return __commandList;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response ( 0 );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}