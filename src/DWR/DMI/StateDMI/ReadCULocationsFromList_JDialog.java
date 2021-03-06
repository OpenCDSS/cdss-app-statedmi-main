// ReadCULocationsFromList_JDialog - Editor for ReadCULocationsFromList() command.

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
import javax.swing.JFileChooser;
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

import java.io.File;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for ReadCULocationsFromList() command.
*/
@SuppressWarnings("serial")
public class ReadCULocationsFromList_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
	
	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

private boolean __error_wait = false;	// To track errors
private boolean __first_time = true;	// Indicate first time display
private JTextArea __command_JTextArea=null;// For command
private JTextField __ListFile_JTextField = null;// List file
private SimpleJComboBox __IDCol_JComboBox = null;
private SimpleJComboBox __NameCol_JComboBox = null;
private SimpleJComboBox __LatitudeCol_JComboBox = null;
private SimpleJComboBox __ElevationCol_JComboBox = null;
private SimpleJComboBox __Region1Col_JComboBox = null;
private SimpleJComboBox __Region2Col_JComboBox = null;
private SimpleJComboBox __AWCCol_JComboBox = null;
private JTextField __Top_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private SimpleJButton __help_JButton = null;	
private SimpleJButton __browse_JButton = null;
private SimpleJButton __path_JButton = null;
private String __working_dir = null;	
private ReadCULocationsFromList_Command __command = null;
private boolean __ok = false;  // Has OK been pressed to close dialog?

/**
Command editor constructor
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public ReadCULocationsFromList_JDialog ( JFrame parent, ReadCULocationsFromList_Command command ) {
	super(parent, true);
	initialize (parent, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __browse_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle("Select List File");
		fc.addChoosableFileFilter( new SimpleFileFilter("csv", "CU Locations List File") );
		SimpleFileFilter sff = new SimpleFileFilter("lst", "CU Locations List File");
		fc.addChoosableFileFilter( sff );
		fc.addChoosableFileFilter( new SimpleFileFilter("txt", "CU Locations List File") );
		fc.setFileFilter(sff);

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName();
			String path = fc.getSelectedFile().getPath();
			
			if (filename == null || filename.equals("")) {
				return;
			}

			if (path != null) {
				// Convert path to relative path by default.
				try {
					__ListFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
				}
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response (false);
	}
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", __command.getCommandName());
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectory) ) {
			__ListFile_JTextField.setText ( IOUtil.toAbsolutePath(__working_dir, __ListFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectory) ) {
			try {
			    __ListFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir, __ListFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, __command.getCommandName() + "_JDialog", "Error converting file to relative path." );
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
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String LatitudeCol = __LatitudeCol_JComboBox.getSelected();
	String ElevationCol = __ElevationCol_JComboBox.getSelected();
	String Region1Col = __Region1Col_JComboBox.getSelected();
	String Region2Col = __Region2Col_JComboBox.getSelected();
	String AWCCol = __AWCCol_JComboBox.getSelected();
	String Top = __Top_JTextField.getText().trim();
	
	__error_wait = false;
	
	if (ListFile.length() > 0) {
		props.set("ListFile", ListFile);
	}
	if (IDCol.length() > 0 && !IDCol.equals("")) {
		props.set("IDCol", IDCol);
	}
	if (NameCol.length() > 0 && !NameCol.equals("")) {
		props.set("NameCol", NameCol);
	}
	if (LatitudeCol.length() > 0 && !LatitudeCol.equals("")) {
		props.set("LatitudeCol", LatitudeCol);
	}
	if (ElevationCol.length() > 0 && !ElevationCol.equals("")) {
		props.set("ElevationCol", ElevationCol);
	}
	if (Region1Col.length() > 0 && !Region1Col.equals("")) {
		props.set("Region1Col", Region1Col);
	}
	if (Region2Col.length() > 0 && !Region2Col.equals("")) {
		props.set("Region2Col", Region2Col);
	}
	if (AWCCol.length() > 0 && !AWCCol.equals("")) {
		props.set("AWCCol", AWCCol);
	}
	if (Top.length() > 0 && !Top.equals("")) {
		props.set("Top", Top);
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
private void commitEdits() {
	String ListFile = __ListFile_JTextField.getText().trim();
	String IDCol = __IDCol_JComboBox.getSelected();
	String NameCol = __NameCol_JComboBox.getSelected();
	String LatitudeCol = __LatitudeCol_JComboBox.getSelected();
	String ElevationCol = __ElevationCol_JComboBox.getSelected();
	String Region1Col = __Region1Col_JComboBox.getSelected();
	String Region2Col = __Region2Col_JComboBox.getSelected();
	String AWCCol = __AWCCol_JComboBox.getSelected();
	String Top = __Top_JTextField.getText().trim();

	__command.setCommandParameter("ListFile", ListFile);
	__command.setCommandParameter("IDCol", IDCol);
	__command.setCommandParameter("NameCol", NameCol);
	__command.setCommandParameter("LatitudeCol", LatitudeCol);
	__command.setCommandParameter("ElevationCol", ElevationCol);
	__command.setCommandParameter("Region1Col", Region1Col);
	__command.setCommandParameter("Region2Col", Region2Col);
	__command.setCommandParameter("AWCCol", AWCCol );
	__command.setCommandParameter("Top", Top );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize (JFrame parent, ReadCULocationsFromList_Command command) {
	__command = command;
	CommandProcessor processor = __command.getCommandProcessor();
	__working_dir = StateDMICommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

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
    	"This command reads CU locations from a list file containing columns of information."),
    	0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"A CU Location is a location where water requirement is estimated."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"Columns should be delimited by commas."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"It is recommended that the location of the file be " +
		"specified using a path relative to the working directory."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
    JGUIUtil.addComponent(paragraph, new JLabel ( ""),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);		
	if (__working_dir != null) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"The working directory is: " + __working_dir), 
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("List file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ListFile_JTextField = new JTextField (35);
	__ListFile_JTextField.addKeyListener (this);
    // Input file layout fights back with other rows so put in its own panel
	JPanel InputFile_JPanel = new JPanel();
	InputFile_JPanel.setLayout(new GridBagLayout());
    JGUIUtil.addComponent(InputFile_JPanel, __ListFile_JTextField,
		0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "...", this );
	__browse_JButton.setToolTipText("Browse for file");
    JGUIUtil.addComponent(InputFile_JPanel, __browse_JButton,
		1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton(	__RemoveWorkingDirectory,this);
		JGUIUtil.addComponent(InputFile_JPanel, __path_JButton,
			2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}
	JGUIUtil.addComponent(main_JPanel, InputFile_JPanel,
		1, y, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("ID column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> column_Vector = new Vector<String>(100);
	column_Vector.add ( "" );	// Not available
	for ( int i = 1; i <= 100; i++ ) {
		column_Vector.add ( "" + i );
	}
	__IDCol_JComboBox = new SimpleJComboBox(false);
	__IDCol_JComboBox.setData ( column_Vector );
	__IDCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __IDCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - column (1+) for station identifier."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Name column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__NameCol_JComboBox = new SimpleJComboBox(false);
	__NameCol_JComboBox.setData ( column_Vector );
	__NameCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __NameCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for station name."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Latitude column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LatitudeCol_JComboBox = new SimpleJComboBox(false);
	__LatitudeCol_JComboBox.setData ( column_Vector );
	__LatitudeCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __LatitudeCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for latitude."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Elevation column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ElevationCol_JComboBox = new SimpleJComboBox(false);
	__ElevationCol_JComboBox.setData ( column_Vector );
	__ElevationCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __ElevationCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for elevation."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region1 column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region1Col_JComboBox = new SimpleJComboBox(false);
	__Region1Col_JComboBox.setData ( column_Vector );
	__Region1Col_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __Region1Col_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - column (1+) for region 1 (e.g., county)."),
	3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);        

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Region2 column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Region2Col_JComboBox = new SimpleJComboBox(false);
	__Region2Col_JComboBox.setData ( column_Vector );
	__Region2Col_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __Region2Col_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for region 2 (e.g., HUC)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("AWC column:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AWCCol_JComboBox = new SimpleJComboBox(false);
	__AWCCol_JComboBox.setData ( column_Vector );
	__AWCCol_JComboBox.addItemListener (this);
	JGUIUtil.addComponent(main_JPanel, __AWCCol_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - column (1+) for AWC."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Top (limit rows):"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Top_JTextField = new JTextField(10);
	__Top_JTextField.addKeyListener (this);
	JGUIUtil.addComponent(main_JPanel, __Top_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - limit read to top N rows (default=read all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

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

	setTitle("Edit " + __command.getCommandName() + "() Command");
	// JDialogs do not need to be resize-able...
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();	// Sets the __path_JButton status
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
{	String routine = "readCULocationsFromList.refresh";
	String ListFile = "";
	String IDCol = "";
	String NameCol = "";
	String LatitudeCol = "";
	String ElevationCol = "";
	String Region1Col = "";
	String Region2Col = "";
	String AWCCol = "";
	String Top = "";
	PropList props = null;

	if (__first_time) {
		__first_time = false;

		// Get the properties from the command
		props = __command.getCommandParameters();
		ListFile = props.getValue ( "ListFile" );
		IDCol = props.getValue ( "IDCol" );
		NameCol = props.getValue ( "NameCol" );
		LatitudeCol = props.getValue ( "LatitudeCol" );
		ElevationCol = props.getValue ( "ElevationCol" );
		Region1Col = props.getValue ( "Region1Col" );
		Region2Col = props.getValue ( "Region2Col" );
		AWCCol = props.getValue ( "AWCCol" );
		Top = props.getValue ( "Top" );
		if ( ListFile != null ) {
			__ListFile_JTextField.setText (ListFile);
		}
		if ( IDCol == null ) {
			// Select default...
			__IDCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__IDCol_JComboBox, IDCol, JGUIUtil.NONE, null, null ) ) {
				__IDCol_JComboBox.select ( IDCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid IDCol value \"" +
				IDCol +	"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( NameCol == null ) {
			// Select default...
			__NameCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__NameCol_JComboBox, NameCol, JGUIUtil.NONE, null, null ) ) {
				__NameCol_JComboBox.select ( NameCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid NameCol value \"" +
				NameCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( LatitudeCol == null ) {
			// Select default...
			__LatitudeCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__LatitudeCol_JComboBox, LatitudeCol, JGUIUtil.NONE, null, null ) ) {
				__LatitudeCol_JComboBox.select ( LatitudeCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid LatitudeCol value \"" + LatitudeCol +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( ElevationCol == null ) {
			// Select default...
			__ElevationCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__ElevationCol_JComboBox, ElevationCol, JGUIUtil.NONE, null, null ) ) {
				__ElevationCol_JComboBox.select ( ElevationCol);
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid ElevationCol value \"" +
				ElevationCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region1Col == null ) {
			// Select default...
			__Region1Col_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region1Col_JComboBox, Region1Col, JGUIUtil.NONE, null, null ) ) {
				__Region1Col_JComboBox.select ( Region1Col );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid Region1Col value \"" +
				Region1Col + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Region2Col == null ) {
			// Select default...
			__Region2Col_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__Region2Col_JComboBox, Region2Col, JGUIUtil.NONE, null, null ) ) {
				__Region2Col_JComboBox.select ( Region2Col );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid Region2Col value \"" +
				Region2Col + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( AWCCol == null ) {
			// Select default...
			__AWCCol_JComboBox.select ( 0 );
		}
		else {
			if ( JGUIUtil.isSimpleJComboBoxItem(
				__AWCCol_JComboBox, AWCCol, JGUIUtil.NONE, null, null ) ) {
				__AWCCol_JComboBox.select ( AWCCol );
			}
			else {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid AWCCol value \"" +
				AWCCol + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( Top != null ) {
			__Top_JTextField.setText (Top);
		}
	}
	// Regardless, reset the command from the fields...
	ListFile = __ListFile_JTextField.getText().trim();
	IDCol = __IDCol_JComboBox.getSelected();
	NameCol = __NameCol_JComboBox.getSelected();
	LatitudeCol = __LatitudeCol_JComboBox.getSelected();
	ElevationCol = __ElevationCol_JComboBox.getSelected();
	Region1Col = __Region1Col_JComboBox.getSelected();
	Region2Col = __Region2Col_JComboBox.getSelected();
	AWCCol = __AWCCol_JComboBox.getSelected();
	Top = __Top_JTextField.getText().trim();
	props = new PropList(__command.getCommandName());
	props.add("ListFile=" + ListFile);
	props.add("IDCol=" + IDCol);
	props.add("NameCol=" + NameCol);
	props.add("LatitudeCol=" +	LatitudeCol);
	props.add("ElevationCol=" + ElevationCol);
	props.add("Region1Col=" + Region1Col);
	props.add("Region2Col=" + Region2Col);
	props.add("AWCCol=" + AWCCol);
	props.add("Top=" + Top);
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if (__path_JButton != null) {
		if ( (ListFile != null) && !ListFile.isEmpty() ) {
			__path_JButton.setEnabled ( true );
			File f = new File ( ListFile );
			if ( f.isAbsolute() ) {
				__path_JButton.setText ( __RemoveWorkingDirectory );
				__path_JButton.setToolTipText("Change path to relative to command file");
			}
			else {
		    	__path_JButton.setText ( __AddWorkingDirectory );
		    	__path_JButton.setToolTipText("Change path to absolute");
			}
		}
		else {
			__path_JButton.setEnabled(false);
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
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

// The following methods are all necessary because this class
// implements WindowListener
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
