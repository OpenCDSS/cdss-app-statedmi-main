package DWR.DMI.StateDMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import DWR.StateMod.StateMod_NodeNetwork;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

/**
Editor for PrintTextFile command.
*/
public class PrintNetwork_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private final String __AddWorkingDirectoryInput = "Add Working Directory (Input)";
private final String __RemoveWorkingDirectoryInput = "Remove Working Directory (Input)";

private final String __AddWorkingDirectoryOutput = "Add Working Directory (Output)";
private final String __RemoveWorkingDirectoryOutput = "Remove Working Directory (Output)";

private SimpleJButton __browse_JButton = null; // input file
private SimpleJButton __browse2_JButton = null; // output file
private SimpleJButton __path_JButton = null;
private SimpleJButton __path2_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private JTextField __InputFile_JTextField = null;
private SimpleJComboBox __PageLayout_JComboBox = null;
private SimpleJComboBox __PrinterName_JComboBox = null;
private SimpleJComboBox __PaperSize_JComboBox = null;
private SimpleJComboBox __PaperSource_JComboBox = null;
private SimpleJComboBox __Orientation_JComboBox = null;
private JTextField __MarginLeft_JTextField = null;
private JTextField __MarginRight_JTextField = null;
private JTextField __MarginTop_JTextField = null;
private JTextField __MarginBottom_JTextField = null;
private JTextField __OutputFile_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private SimpleJComboBox __ShowDialog_JComboBox = null;
private JTextField __IncludeNodes_JTextField = null;
private JTextField __Buffer_JTextField = null;
private JTextField __PrintExtent_JTextField = null;
private JTextArea __command_JTextArea = null;
private String __working_dir = null; // Working directory.
private boolean __error_wait = false;
private boolean __first_time = true;
private PrintNetwork_Command __command = null;
private boolean __ok = false; // Has user pressed OK to close the dialog.

// Network layout data
private List<PropList> __layouts = null;

//private PrinterJob __printerJob = null; // Used to fill out parameter choices

private PrintService [] __printServiceArray = null; // Used to fill out parameter choices

private PrintService __selectedPrintService = null; // The print service that is selected for printing

private PrintService __defaultPrintService = null; // The default print service for the computer

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public PrintNetwork_JDialog ( JFrame parent, PrintNetwork_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
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
		fc.setDialogTitle("Select StateMod Network File");
		SimpleFileFilter sff = new SimpleFileFilter("net", "StateMod Network File");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);
	
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath();
			__InputFile_JTextField.setText(path);
			JGUIUtil.setLastFileDialogDirectory(directory);
	        try {
	            // fill Page Layout options
	            readLayouts();
	            fillPageLayoutOptions();
	        }
	        catch (Exception ex) {
	            Message.printWarning (1, "PrintNetwork_JDialog", "Error processing network file " + path );
	        }
			refresh ();
		}
	}
	else if ( o == __browse2_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
		}
		else {
			fc = JFileChooserFactory.createJFileChooser(__working_dir );
		}
		SimpleFileFilter sff = null;
		fc.setDialogTitle("Specify Network Print File to Write");
		sff = new SimpleFileFilter("tif", "Tagged Image File Format");
		fc.addChoosableFileFilter(sff);
		fc.setFileFilter(sff);
	
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String path = fc.getSelectedFile().getPath(); 
			JGUIUtil.setLastFileDialogDirectory(directory);
			__OutputFile_JTextField.setText(path);
			refresh();
		}	
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path_JButton ) {
		if ( __path_JButton.getText().equals(__AddWorkingDirectoryInput) ) {
			__InputFile_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__InputFile_JTextField.getText() ) );
		}
		else if ( __path_JButton.getText().equals(__RemoveWorkingDirectoryInput) ) {
			try {
                __InputFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
                        __InputFile_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, "PrintTextFile_JDialog",
				"Error converting file name to relative path." );
			}
		}
		refresh ();
	}
	else if ( o == __path2_JButton ) {
		if (__path2_JButton.getText().equals(__AddWorkingDirectoryOutput)) {
			__OutputFile_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __OutputFile_JTextField.getText()));
		}
		else if (__path2_JButton.getText().equals(__RemoveWorkingDirectoryOutput)) {
			try {
				__OutputFile_JTextField.setText (
				IOUtil.toRelativePath (__working_dir, __OutputFile_JTextField.getText()));
			}
			catch (Exception e) {
				Message.printWarning (1, "PrintNetwork_JDialog", "Error converting file to relative path.");
			}
		}
		refresh ();
	}
	else {
	    // Choices...
		refresh();
	}
}

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
	PropList props = new PropList ( "" );
	String InputFile = __InputFile_JTextField.getText().trim();
	String PageLayout = __PageLayout_JComboBox.getSelected();
	String PrinterName = __PrinterName_JComboBox.getSelected();
	String PaperSize = __PaperSize_JComboBox.getSelected();
	String PaperSource = __PaperSource_JComboBox.getSelected();
	String Orientation = __Orientation_JComboBox.getSelected();
	String MarginLeft = __MarginLeft_JTextField.getText().trim();	String MarginRight = __MarginRight_JTextField.getText().trim();
	String MarginTop = __MarginTop_JTextField.getText().trim();
	String MarginBottom = __MarginBottom_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
	String ShowDialog = __ShowDialog_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	if ( InputFile.length() > 0 ) {
		props.set ( "InputFile", InputFile );
	}
	if (PageLayout.length() > 0) {
		props.set("PageLayout", PageLayout);
	}
    if ( PrinterName.length() > 0 ) {
        props.set ( "PrinterName", PrinterName );
    }
    if ( PaperSize.length() > 0 ) {
        props.set ( "PaperSize", getShortPaperSize(PaperSize) );
    }
    if ( PaperSource.length() > 0 ) {
        props.set ( "PaperSource", PaperSource );
    }
    if ( Orientation.length() > 0 ) {
        props.set ( "Orientation", Orientation );
    }
    if ( MarginLeft.length() > 0 ) {
        props.set ( "MarginLeft", MarginLeft );
    }
    if ( MarginRight.length() > 0 ) {
        props.set ( "MarginRight", MarginRight );
    }
    if ( MarginTop.length() > 0 ) {
        props.set ( "MarginTop", MarginTop );
    }
    if ( MarginBottom.length() > 0 ) {
        props.set ( "MarginBottom", MarginBottom );
    }
	if (OutputFile.length() > 0) {
		props.set("OutputFile", OutputFile);
	}
    if ( ShowDialog.length() > 0 ) {
        props.set ( "ShowDialog", ShowDialog );
    }
	if ( IfNotFound.length() > 0 ) {
		props.set ( "IfNotFound", IfNotFound );
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
private void commitEdits ()
{	String InputFile = __InputFile_JTextField.getText().trim();
	String PageLayout = __PageLayout_JComboBox.getSelected();
    String PrinterName = __PrinterName_JComboBox.getSelected();
    String PaperSize = __PaperSize_JComboBox.getSelected();
    String PaperSource = __PaperSource_JComboBox.getSelected();
    String Orientation = __Orientation_JComboBox.getSelected();
    String MarginLeft = __MarginLeft_JTextField.getText().trim();
    String MarginRight = __MarginRight_JTextField.getText().trim();
    String MarginTop = __MarginTop_JTextField.getText().trim();
    String MarginBottom = __MarginBottom_JTextField.getText().trim();
	String OutputFile = __OutputFile_JTextField.getText().trim();
    String ShowDialog = __ShowDialog_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "InputFile", InputFile );
	__command.setCommandParameter("PageLayout", PageLayout);
	__command.setCommandParameter ( "PrinterName", PrinterName );
	__command.setCommandParameter ( "PaperSize", getShortPaperSize(PaperSize) );
	__command.setCommandParameter ( "PaperSource", PaperSource );
	__command.setCommandParameter ( "Orientation", Orientation );
	__command.setCommandParameter ( "MarginLeft", MarginLeft );
	__command.setCommandParameter ( "MarginRight", MarginRight );
	__command.setCommandParameter ( "MarginTop", MarginTop );
	__command.setCommandParameter ( "MarginBottom", MarginBottom );
	__command.setCommandParameter ( "OutputFile", OutputFile );
	__command.setCommandParameter ( "ShowDialog", ShowDialog );
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
}

/**
Populate the network layout choices (called once network is read).
 */
private void fillPageLayoutOptions ( )
{
    String isDefault, id;
    if ( __layouts == null ){
        return;
    }
    __PageLayout_JComboBox.removeAll();
    for (PropList p : __layouts ) {
        id = p.getValue("ID");
        isDefault = p.getValue("IsDefault");
        __PageLayout_JComboBox.add(id);
        if ( isDefault != null && isDefault.equalsIgnoreCase("true"))
            __PageLayout_JComboBox.setSelectedItem(id);
    }
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__InputFile_JTextField = null;
	__IfNotFound_JComboBox = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Return the short page size, including only the string before the note.
*/
private String getShortPaperSize ( String longPaperSize )
{
    if ( (longPaperSize == null) || (longPaperSize.length() == 0) ) {
        return "";
    }
    else {
        int pos = longPaperSize.indexOf ( " " );
        if ( pos < 0 ) {
            // No note
            return longPaperSize;
        }
        else {
            // Has note
            return longPaperSize.substring(0,pos).trim();
        }
    }
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, PrintNetwork_Command command )
{	__command = command;
	CommandProcessor processor =__command.getCommandProcessor();
	
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command prints a StateMod XML network file.  If the network file is not specified, " +
		"the network read from previous commands will be processed."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The network can be printed directly to a printer or can be printed to a file, which is useful" +
		" for testing and reviewing drafts."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If the network file is not specified, it is assumed that the network "+
		"has already been read in the network editor or by another command."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If the network file is specified, it is read and will be used only for printing in this command."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The layout that is selected must use a paper size that is available on the selected printer."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Determining supported printer settings may take a few seconds." ),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The margins agree with the orientation (e.g., for letter size portrait orientation, " +
        "left margin is long edge; for landscape, left margin is for short edge)." ),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __working_dir != null ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"It is recommended that file names are relative to the working directory, which is:"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    " + __working_dir),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Network (input) file:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile_JTextField = new JTextField ( 50 );
	__InputFile_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse_JButton = new SimpleJButton ( "Browse", this );
    JGUIUtil.addComponent(main_JPanel, __browse_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Page layout:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
   	List<String> layoutList = new Vector(3);
   	// FIXME SAM 2009-03-06 Add list of layouts from network - need to evaluate discovery because
   	// network may not be available when editing command - therefore, allow editable choice below
	layoutList.add ( "" );
	__PageLayout_JComboBox = new SimpleJComboBox(true); // Allow edits
	__PageLayout_JComboBox.setData ( layoutList );
	__PageLayout_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(main_JPanel, __PageLayout_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - layout identifier as shown in the network editor."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Printer name:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    //this.__printerJob = PrinterJob.getPrinterJob();
    this.__printServiceArray = PrinterJob.lookupPrintServices();
    this.__defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    List<String> printerNames = new Vector();
    printerNames.add ( "" ); // Corresponds to default printer
    for ( int i = 0; i < this.__printServiceArray.length; i++ ) {
        printerNames.add (this.__printServiceArray[i].getName());
    }
    Collections.sort(printerNames);
    __PrinterName_JComboBox = new SimpleJComboBox ( false );
    __PrinterName_JComboBox.setData ( printerNames );
    __PrinterName_JComboBox.select ( 0 );
    __PrinterName_JComboBox.addItemListener ( this );
   JGUIUtil.addComponent(main_JPanel, __PrinterName_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - printer name (default=default printer)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Paper size:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PaperSize_JComboBox = new SimpleJComboBox ( 30, false );
    __PaperSize_JComboBox.addItem ( "" );  // Default
    // TODO SAM 2011-06-24 Get from a PrinterJob
    __PaperSize_JComboBox.select ( 0 );
    __PaperSize_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __PaperSize_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - paper size name (default=for network page layout)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);    
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Paper source (tray):"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PaperSource_JComboBox = new SimpleJComboBox ( false );
    __PaperSource_JComboBox.setEnabled(false); // TODO SAM 2011-06-24 For now disable
    __PaperSource_JComboBox.addItem ( "" );  // Default
    // TODO SAM 2011-06-24 Get from a PrinterJob
    __PaperSource_JComboBox.select ( 0 );
    __PaperSource_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __PaperSource_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - paper source (default=default source/tray)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Orientation:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Orientation_JComboBox = new SimpleJComboBox ( false );
    __Orientation_JComboBox.addItem ( "" ); // Default
    // TODO SAM 2011-06-25 Figure out the reverse orientations
    __Orientation_JComboBox.addItem ( PrintUtil.getOrientationAsString(PageFormat.LANDSCAPE) );
    __Orientation_JComboBox.addItem ( PrintUtil.getOrientationAsString(PageFormat.PORTRAIT) );
    // TODO SAM 2011-06-24 Get from a PrinterJob
    __Orientation_JComboBox.select ( 0 );
    __Orientation_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __Orientation_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - paper orientation (default=for network page layout)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Left margin:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MarginLeft_JTextField = new JTextField ( 10 );
    __MarginLeft_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __MarginLeft_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - left margin, inches (default=.75)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Right margin:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MarginRight_JTextField = new JTextField ( 10 );
    __MarginRight_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __MarginRight_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - right margin, inches (default=.75)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Top margin:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MarginTop_JTextField = new JTextField ( 10 );
    __MarginTop_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __MarginTop_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - top margin, inches (default=.75)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Bottom margin:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __MarginBottom_JTextField = new JTextField ( 10 );
    __MarginBottom_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __MarginBottom_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - bottom margin, inches (default=.75)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Print (output) file:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OutputFile_JTextField = new JTextField (35);
	__OutputFile_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __OutputFile_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse2_JButton = new SimpleJButton ("Browse", this);
    JGUIUtil.addComponent(main_JPanel, __browse2_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
	    "Optional - can use when printing to PDF or another file format."), 
	    3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Show dialog?:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ShowDialog_JComboBox = new SimpleJComboBox ( false );
    __ShowDialog_JComboBox.addItem ( "" );  // Default
    __ShowDialog_JComboBox.addItem ( __command._False );
    __ShowDialog_JComboBox.addItem ( __command._True );
    __ShowDialog_JComboBox.select ( 0 );
    __ShowDialog_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __ShowDialog_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Optional - show printer dialog? (default=" + __command._False + ")."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

   JGUIUtil.addComponent(main_JPanel, new JLabel ( "If not found?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox ( false );
	__IfNotFound_JComboBox.addItem ( "" );	// Default
	__IfNotFound_JComboBox.addItem ( __command._Ignore );
	__IfNotFound_JComboBox.addItem ( __command._Warn );
	__IfNotFound_JComboBox.addItem ( __command._Fail );
	__IfNotFound_JComboBox.select ( 0 );
	__IfNotFound_JComboBox.addActionListener ( this );
   JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
		"Optional - action if file not found (default=" + __command._Warn + ")."), 
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Nodes to include:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IncludeNodes_JTextField = new JTextField ( 10 );
    __IncludeNodes_JTextField.setEnabled ( false );
    __IncludeNodes_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeNodes_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Future enhancement - nodes defining print extent (default=print all)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Buffer:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Buffer_JTextField = new JTextField ( 10 );
    __Buffer_JTextField.setEnabled ( false );
    __Buffer_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Buffer_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Future enhancement - extra buffer around printed nodes (default=no buffer)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("PrintExtent:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __PrintExtent_JTextField = new JTextField ( 10 );
    __PrintExtent_JTextField.setEnabled ( false );
    __PrintExtent_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __PrintExtent_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Future enhancement - print area extent using node coordinates (default=print all)."), 
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 60 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.addKeyListener ( this );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if ( __working_dir != null ) {
		// Add the buttons to allow conversion to/from relative path...
		__path_JButton = new SimpleJButton( __RemoveWorkingDirectoryInput,this);
		button_JPanel.add ( __path_JButton );
		__path2_JButton = new SimpleJButton( __RemoveWorkingDirectoryOutput, this);
		button_JPanel.add (__path2_JButton);
	}
	button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
	button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );

	setTitle ( "Edit " + __command.getCommandName() + "() command" );
	
	// Refresh the contents...
    checkGUIState();
    refresh ();

	// Dialogs do not need to be resizable...
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{   //checkGUIState();
    Object source = e.getSource();
    if ( source == __PrinterName_JComboBox ) {
        // First get the print service instance that goes with the printer name
        String printerName = __PrinterName_JComboBox.getSelected().trim();
        if ( (printerName == null) || printerName.equals("") ) {
            // Use default printer
            this.__selectedPrintService = this.__defaultPrintService;
        }
        else {
            // Specific printer has been selected...
            for ( int i = 0; i < __printServiceArray.length; i++ ) {
                if ( printerName.equalsIgnoreCase(__printServiceArray[i].getName())) {
                    this.__selectedPrintService = __printServiceArray[i];
                    break;
                }
            }
        }
        Message.printStatus(2, "", "Printer for PrintTextFile choices:  " + this.__selectedPrintService.getName() );
        // Need to update other choices
        // Available page sizes...
        List<String> supportedMediaSizes = PrintUtil.getSupportedMediaSizeNames(this.__selectedPrintService,
            true, // Include notes
            true ); // Include dimensions );
        // Add a blank
        supportedMediaSizes.add(0, "");
        // Reset the available page sizes...
        this.__PaperSize_JComboBox.setData(supportedMediaSizes);
    }
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok ()
{	return __ok;
}

/**
Read the layouts from the network file, for use in the PageLayout choice.
*/
private void readLayouts ()
{	// Get the network file from the input file
	String routine = getClass().getName() + ".readLayouts";
	String InputFile = __InputFile_JTextField.getText().trim();
	CommandProcessor processor = __command.getCommandProcessor();
	String inputFileFull = IOUtil.verifyPathForOS(
		IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),InputFile ));
	if ( IOUtil.fileExists(inputFileFull)) {
		try {
			StateMod_NodeNetwork network = StateMod_NodeNetwork.readXMLNetworkFile(inputFileFull);
			__layouts = network.getLayoutList();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading layouts from network file \"" +
				inputFileFull + "\" (" + e + ").");
			Message.printWarning(3, routine, e );
		}
	}
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = "PrintNetwork_JDialog.refresh";
	String InputFile = "";
	String PageLayout = "";
	String PrinterName = "";
	String PaperSize = "";
	String PaperSource = "";
	String Orientation = "";
	String MarginLeft = "";
	String MarginRight = "";
	String MarginTop = "";
	String MarginBottom = "";
	String OutputFile = "";
	String ShowDialog = "";
	String IfNotFound = "";
    PropList parameters = null;
	if ( __first_time ) {
		__first_time = false;
        parameters = __command.getCommandParameters();
		InputFile = parameters.getValue ( "InputFile" );
		PageLayout = parameters.getValue ( "PageLayout" );
		PrinterName = parameters.getValue ( "PrinterName" );
		PaperSize = parameters.getValue ( "PaperSize" );
		PaperSource = parameters.getValue ( "PaperSource" );
		Orientation = parameters.getValue ( "Orientation" );
		MarginLeft = parameters.getValue ( "MarginLeft" );
		MarginRight = parameters.getValue ( "MarginRight" );
		MarginTop = parameters.getValue ( "MarginTop" );
		MarginBottom = parameters.getValue ( "MarginBottom" );
		OutputFile = parameters.getValue ( "OutputFile" );
		ShowDialog = parameters.getValue ( "ShowDialog" );
		IfNotFound = parameters.getValue ( "IfNotFound" );
		if ( InputFile != null ) {
			__InputFile_JTextField.setText ( InputFile );
			readLayouts();
		}
		if ( __PageLayout_JComboBox != null ) {
            fillPageLayoutOptions(); // From InputFile above
			if ( PageLayout == null ) {
				if ( __PageLayout_JComboBox.getItemCount() > 0 )
                    __PageLayout_JComboBox.setSelectedIndex(0);
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem( __PageLayout_JComboBox,
					PageLayout, JGUIUtil.NONE, null, null ) ) {
					__PageLayout_JComboBox.select ( PageLayout );
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid PageLayout value \"" +
					PageLayout + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
        if ( JGUIUtil.isSimpleJComboBoxItem(__PrinterName_JComboBox, PrinterName,JGUIUtil.NONE, null, null ) ) {
            __PrinterName_JComboBox.select ( PrinterName );
        }
        else {
            if ( (PrinterName == null) || PrinterName.equals("") ) {
                // New command...select the default...
                __PrinterName_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "PrinterName parameter \"" + PrinterName + "\".  Select a\n value or Cancel." );
            }
        }
        // Page size is handled differently because the choices contain notes that are not saved in the
        // command parameter.  Notes are after " - " but use space because - may occur in primary data
        try {
            JGUIUtil.selectTokenMatches(__PaperSize_JComboBox, true, " ", 0, 0, PaperSize, null );
        }
        catch ( Exception e ) {
            if ( (PaperSize == null) || PaperSize.equals("") ) {
                // New command...select the default...
                __PaperSize_JComboBox.select ( 0 );
            }
            else {
                // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "PaperSize parameter \"" + PaperSize + "\".  Select a\n value or Cancel." );
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__PaperSource_JComboBox, PaperSource,JGUIUtil.NONE, null, null ) ) {
            __PaperSource_JComboBox.select ( PaperSource );
        }
        else {
            if ( (PaperSource == null) || PaperSource.equals("") ) {
                // New command...select the default...
                __PaperSource_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "PaperSource parameter \"" + PaperSource + "\".  Select a\n value or Cancel." );
            }
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__Orientation_JComboBox, Orientation,JGUIUtil.NONE, null, null ) ) {
            __Orientation_JComboBox.select ( Orientation );
        }
        else {
            if ( (Orientation == null) || Orientation.equals("") ) {
                // New command...select the default...
                __Orientation_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "Orientation parameter \"" + Orientation + "\".  Select a\n value or Cancel." );
            }
        }
        if ( MarginLeft != null ) {
            __MarginLeft_JTextField.setText ( MarginLeft );
        }
        if ( MarginRight != null ) {
            __MarginRight_JTextField.setText ( MarginRight );
        }
        if ( MarginTop != null ) {
            __MarginTop_JTextField.setText ( MarginTop );
        }
        if ( MarginBottom != null ) {
            __MarginBottom_JTextField.setText ( MarginBottom );
        }
        if ( OutputFile != null ) {
			__OutputFile_JTextField.setText ( OutputFile );
        }
        if ( JGUIUtil.isSimpleJComboBoxItem(__ShowDialog_JComboBox, ShowDialog,JGUIUtil.NONE, null, null ) ) {
            __ShowDialog_JComboBox.select ( ShowDialog );
        }
        else {
            if ( (ShowDialog == null) || ShowDialog.equals("") ) {
                // New command...select the default...
                __ShowDialog_JComboBox.select ( 0 );
            }
            else {  // Bad user command...
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                "ShowDialog parameter \"" + ShowDialog + "\".  Select a\n value or Cancel." );
            }
        }
		if ( JGUIUtil.isSimpleJComboBoxItem(__IfNotFound_JComboBox, IfNotFound,JGUIUtil.NONE, null, null ) ) {
			__IfNotFound_JComboBox.select ( IfNotFound );
		}
		else {
            if ( (IfNotFound == null) || IfNotFound.equals("") ) {
				// New command...select the default...
				__IfNotFound_JComboBox.select ( 0 );
			}
			else {	// Bad user command...
				Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
				"IfNotFound parameter \"" +	IfNotFound + "\".  Select a\n value or Cancel." );
			}
		}
	}
	// Regardless, reset the command from the fields.  This is only  visible
	// information that has not been committed in the command.
	InputFile = __InputFile_JTextField.getText().trim();
	PageLayout = __PageLayout_JComboBox.getSelected();
    PrinterName = __PrinterName_JComboBox.getSelected();
    PaperSize = __PaperSize_JComboBox.getSelected();
    PaperSource = __PaperSource_JComboBox.getSelected();
    Orientation = __Orientation_JComboBox.getSelected();
    MarginLeft = __MarginLeft_JTextField.getText().trim();
    MarginRight = __MarginRight_JTextField.getText().trim();
    MarginTop = __MarginTop_JTextField.getText().trim();
    MarginBottom = __MarginBottom_JTextField.getText().trim();
	OutputFile = __OutputFile_JTextField.getText().trim();
    ShowDialog = __ShowDialog_JComboBox.getSelected();
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	PropList props = new PropList ( __command.getCommandName() );
	props.add ( "InputFile=" + InputFile );
	props.add ( "PageLayout=" + PageLayout );
	props.add ( "PrinterName=" + PrinterName );
	props.add ( "PaperSize=" + getShortPaperSize(PaperSize) );
	props.add ( "PaperSource=" + PaperSource );
	props.add ( "Orientation=" + Orientation );
	props.add ( "MarginLeft=" + MarginLeft );
	props.add ( "MarginRight=" + MarginRight );
	props.add ( "MarginTop=" + MarginTop );
	props.add ( "MarginBottom=" + MarginBottom );
	props.add ( "OutputFile=" + OutputFile );
	props.add ( "ShowDialog=" + ShowDialog );
	props.add ( "IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
	// Check the path and determine what the label on the path button should be...
	if ( __path_JButton != null ) {
		__path_JButton.setEnabled ( true );
		File f = new File ( InputFile );
		if ( f.isAbsolute() ) {
			__path_JButton.setText (__RemoveWorkingDirectoryInput);
		}
		else {
            __path_JButton.setText (__AddWorkingDirectoryInput );
		}
	}
	if ( __path2_JButton != null ) {
		__path2_JButton.setEnabled ( true );
		File f = new File ( OutputFile );
		if ( f.isAbsolute() ) {
			__path2_JButton.setText (__RemoveWorkingDirectoryOutput);
		}
		else {
            __path2_JButton.setText (__AddWorkingDirectoryOutput );
		}
	}
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed
and the dialog is closed.
*/
public void response ( boolean ok )
{	__ok = ok;
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
