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
Editor for FillCropPatternTS*() commands.
*/
public class FillCropPatternTS_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener, ChangeListener {

private boolean __error_wait = false;
private boolean __first_time = true;	
private JTextArea __command_JTextArea=null;
private JTextField __ID_JTextField = null;
private SimpleJComboBox __IncludeGroundwaterOnlySupply_JComboBox = null;
private SimpleJComboBox __IncludeSurfaceWaterSupply_JComboBox = null;
private JTextField __CropType_JTextField = null;
private SimpleJComboBox __NormalizeTotals_JComboBox = null;
private JTextField __FillStart_JTextField = null;
private JTextField __FillEnd_JTextField = null;
private JTextField __ParcelYear_JTextField = null;
private JTextField __MaxIntervals_JTextField = null;
private JTextField __Constant_JTextField = null;
private SimpleJComboBox __FillDirection_JComboBox = null;
private JTextField __FillFlag_JTextField = null;
private SimpleJComboBox	__IfNotFound_JComboBox = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;	
private FillCropPatternTS_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public FillCropPatternTS_JDialog ( JFrame parent, FillCropPatternTS_Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();

	if (s.equals("Cancel")) {
		response (false);
	}
	else if (s.equals("OK")) {
		refresh ();
		checkInput ();
		if (!__error_wait) {
			response (true);
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList parameters = new PropList ( "" );
	String ID = __ID_JTextField.getText().trim();
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String CropType = __CropType_JTextField.getText().trim();
	String IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
	String IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	if ( ID.length() > 0 ) {
		parameters.set ( "ID", ID );
	}
	if ( FillStart.length() > 0 ) {
		parameters.set ( "FillStart", FillStart );
	}
	if ( FillEnd.length() > 0 ) {
		parameters.set ( "FillEnd", FillEnd );
	}
	if ( CropType.length() > 0 ) {
		parameters.set ( "CropType", CropType );
	}
	if ( IncludeSurfaceWaterSupply.length() > 0 ) {
		parameters.set ( "IncludeSurfaceWaterSupply", IncludeSurfaceWaterSupply );
	}
	if ( IncludeGroundwaterOnlySupply.length() > 0 ) {
		parameters.set ( "IncludeGroundwaterOnlySupply", IncludeGroundwaterOnlySupply );
	}
	if ( IfNotFound.length() > 0 ) {
		parameters.set ( "IfNotFound", IfNotFound );
	}
	
	if ( __NormalizeTotals_JComboBox != null ) {
		String NormalizeTotals = __NormalizeTotals_JComboBox.getSelected();
		if ( NormalizeTotals.length() > 0 ) {
			parameters.set ( "NormalizeTotals", NormalizeTotals );
		}
	}
	if ( __ParcelYear_JTextField != null ) {
		String ParcelYear = __ParcelYear_JTextField.getText().trim();
		if ( ParcelYear.length() > 0 ) {
			parameters.set ( "ParcelYear", ParcelYear );
		}
	}
	if ( __FillDirection_JComboBox != null ) {
		String FillDirection = __FillDirection_JComboBox.getSelected();
		if ( FillDirection.length() > 0 ) {
			parameters.set ( "FillDirection", FillDirection );
		}
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		if ( FillFlag.length() > 0 ) {
			parameters.set ( "FillFlag", FillFlag );
		}
	}
	if ( __MaxIntervals_JTextField != null ) {
		String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
		if ( MaxIntervals.length() > 0 ) {
			parameters.set ( "MaxIntervals", MaxIntervals );
		}
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		if ( Constant.length() > 0 ) {
			parameters.set ( "Constant", Constant );
		}
	}
	__error_wait = false;
	
	try {
		// This will warn the user...
		__command.checkCommandParameters ( parameters, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		Message.printWarning(2,"",e);
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String ID = __ID_JTextField.getText().trim();
	String FillStart = __FillStart_JTextField.getText().trim();
	String FillEnd = __FillEnd_JTextField.getText().trim();
	String CropType = __CropType_JTextField.getText().trim();
	String IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
	String IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	__command.setCommandParameter ( "ID", ID );
	__command.setCommandParameter ( "FillStart", FillStart );
	__command.setCommandParameter ( "FillEnd", FillEnd );
	__command.setCommandParameter ( "CropType", CropType );
	__command.setCommandParameter ( "IncludeSurfaceWaterSupply", IncludeSurfaceWaterSupply );
	__command.setCommandParameter ( "IncludeGroundwaterOnlySupply", IncludeGroundwaterOnlySupply );
	__command.setCommandParameter ( "IfNotFound", IfNotFound );
	
	if ( __NormalizeTotals_JComboBox != null ) {
		String NormalizeTotals = __NormalizeTotals_JComboBox.getSelected();
		__command.setCommandParameter ( "NormalizeTotals", NormalizeTotals );
	}
	if ( __ParcelYear_JTextField != null ) {
		String ParcelYear = __ParcelYear_JTextField.getText().trim();
		__command.setCommandParameter ( "ParcelYear", ParcelYear );
	}
	if ( __FillDirection_JComboBox != null ) {
		String FillDirection = __FillDirection_JComboBox.getSelected();
		__command.setCommandParameter ( "FillDirection", FillDirection );
	}
	if ( __FillFlag_JTextField != null ) {
		String FillFlag = __FillFlag_JTextField.getText().trim();
		__command.setCommandParameter ( "FillFlag", FillFlag );
	}
	if ( __MaxIntervals_JTextField != null ) {
		String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
		__command.setCommandParameter ( "MaxIntervals", MaxIntervals );
	}
	if ( __Constant_JTextField != null ) {
		String Constant = __Constant_JTextField.getText().trim();
		__command.setCommandParameter ( "Constant", Constant );
	}
}
	
public void stateChanged(ChangeEvent e)
{	refresh();
}

/**
Free memory for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable {
	__ID_JTextField = null;
	__IncludeGroundwaterOnlySupply_JComboBox = null;
	__IncludeSurfaceWaterSupply_JComboBox = null;
	__CropType_JTextField = null;
	__NormalizeTotals_JComboBox = null;
	__FillStart_JTextField = null;
	__FillEnd_JTextField = null;
	__ParcelYear_JTextField = null;
	__FillDirection_JComboBox = null;
	__MaxIntervals_JTextField = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, FillCropPatternTS_Command command )
{	__command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = 0;
   	JGUIUtil.addComponent(paragraph, new JLabel (
	"This command fills missing data in crop pattern time series," + 
	" using the CU Location ID, crop type, and year"),
	0, yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	if ( __command instanceof FillCropPatternTSConstant_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are replaced with a constant value."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillCropPatternTSInterpolate_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are " +
		"replaced by interpolating between known crop area values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillCropPatternTSProrateAgStats_Command ) {
		JGUIUtil.addComponent(paragraph, new JLabel (
		"<HTML><B>This command was used with Rio Grande Phase 4 work but is no longer " +
		"used with standard procedures.</B><HTML>"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are " +
		"replaced by prorating known agricultural statistics values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"If NormalizeTotals=True, the prorated value will additionally"+
		" be adjusted by ratios of the total acres for the crop types "),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"(see the documentation for more information)." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillCropPatternTSRepeat_Command ) {
       	JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are replaced by repeating known values."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
	else if ( __command instanceof FillCropPatternTSUsingWellRights_Command ) {
   		JGUIUtil.addComponent(paragraph, new JLabel (
		"to uniquely identify time series.  Missing values are " +
		"replaced by using crop data at parcels for the specified parcel year"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   		JGUIUtil.addComponent(paragraph, new JLabel (
   		"only if at least one water right is available for the parcel."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   		JGUIUtil.addComponent(paragraph, new JLabel (
   		"The resulting parcels are added to give a total by crop for the year."),
   		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The CU Location ID and crop type can contain a * wildcard "+
		"pattern to match one or more time series."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The fill period can optionally be specified.  Only years in the output period can be filled."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("CU location ID:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ID_JTextField = new JTextField("*",10);
	__ID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __ID_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - CU location(s) to fill (use * for wildcard)"),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Crop type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropType_JTextField = new JTextField ("*",10);
	__CropType_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CropType_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - specify the crops to fill (use * for wildcard, or separate by commas)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
	if ( __command instanceof FillCropPatternTSConstant_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Constant:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__Constant_JTextField = new JTextField (10);
		__Constant_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __Constant_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Required - constant value to use for filling."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
    
    if ( __command instanceof FillCropPatternTSUsingWellRights_Command ) {
    	JGUIUtil.addComponent(main_JPanel, new JLabel ("Parcel data year:"),
    		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	__ParcelYear_JTextField = new JTextField (10);
    	__ParcelYear_JTextField.addKeyListener (this);
        JGUIUtil.addComponent(main_JPanel, __ParcelYear_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - 4-digit year for parcel data to turn acreage on/off."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    }
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include surface water supply?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List IncludeSurfaceWaterSupply_Vector = new Vector();
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
        
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include groundwater only supply?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List IncludeGroundwaterOnlySupply_Vector = new Vector();
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

	if ( __command instanceof FillCropPatternTSProrateAgStats_Command ) {
       JGUIUtil.addComponent(main_JPanel, new JLabel ("Normalize totals?:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
        List NormalizeTotals_Vector = new Vector();
		NormalizeTotals_Vector.add ( "" );
		NormalizeTotals_Vector.add ( __command._False );
		NormalizeTotals_Vector.add ( __command._True );
		__NormalizeTotals_JComboBox = new SimpleJComboBox(false);
		__NormalizeTotals_JComboBox.setData ( NormalizeTotals_Vector );
		__NormalizeTotals_JComboBox.addTextFieldKeyListener (this);
		__NormalizeTotals_JComboBox.addItemListener (this);
        JGUIUtil.addComponent(main_JPanel, __NormalizeTotals_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - default=" + __command._True + " for multiple crops, " +
			__command._False + " for one crop."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

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
		"Optional - end year as 4-digits or (default=fill all)."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	if ( __command instanceof FillCropPatternTSRepeat_Command ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Fill direction:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    	List direction_Vector = new Vector();
    	direction_Vector.add ( "" );
		direction_Vector.add ( __command._Backward );
		direction_Vector.add ( __command._Forward );
		__FillDirection_JComboBox = new SimpleJComboBox(false);
		__FillDirection_JComboBox.setData ( direction_Vector );
		__FillDirection_JComboBox.addTextFieldKeyListener (this);
		__FillDirection_JComboBox.addItemListener (this);
        JGUIUtil.addComponent(main_JPanel, __FillDirection_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - direction to process data (default=" + __command._Forward + ")."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	if ( __command instanceof FillCropPatternTSRepeat_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Fill flag:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__FillFlag_JTextField = new JTextField (10);
		__FillFlag_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __FillFlag_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - string to flag filled values (default=no flag)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	if ( __command instanceof FillCropPatternTSInterpolate_Command ||
		__command instanceof FillCropPatternTSRepeat_Command ) {
       	JGUIUtil.addComponent(main_JPanel, new JLabel("Maximum intervals:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__MaxIntervals_JTextField = new JTextField (10);
		__MaxIntervals_JTextField.addKeyListener (this);
       	JGUIUtil.addComponent(main_JPanel, __MaxIntervals_JTextField,
			1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
       	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"Optional - specify limit on intervals to fill (default=no limit)."),
			3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
    List IfNotFound_List = new Vector();
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
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel,new JScrollPane(__command_JTextArea),
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
		checkInput ();
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
{	__error_wait = false;
	String routine = "FillCropPatternTS_JDialog.refresh";
	String ID = "*";
	String IncludeGroundwaterOnlySupply = "";
	String IncludeSurfaceWaterSupply = "";
	String CropType = "*";
	String NormalizeTotals = "";
	String FillStart = "";
	String FillEnd = "";
	String ParcelYear = "";
	String FillDirection = "";
	String FillFlag = "";
	String MaxIntervals = "";
	String Constant = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		ID = props.getValue ( "ID" );
		IncludeGroundwaterOnlySupply = props.getValue ( "IncludeGroundwaterOnlySupply" );
		IncludeSurfaceWaterSupply = props.getValue ( "IncludeSurfaceWaterSupply" );
		CropType = props.getValue ( "CropType" );
		NormalizeTotals = props.getValue ( "NormalizeTotals" );
		FillStart = props.getValue ( "FillStart" );
		FillEnd = props.getValue ( "FillEnd" );
		ParcelYear = props.getValue ( "ParcelYear" );
		FillDirection = props.getValue ( "FillDirection" );
		FillFlag = props.getValue ( "FillFlag" );
		MaxIntervals = props.getValue ( "MaxIntervals" );
		Constant = props.getValue ( "Constant" );
		IfNotFound = props.getValue ( "IfNotFound" );
		if ( ID != null ) {
			__ID_JTextField.setText ( ID );
		}
		if ( __IncludeSurfaceWaterSupply_JComboBox != null ) {
			if ( IncludeSurfaceWaterSupply == null ) {
				// Select default...
				__IncludeSurfaceWaterSupply_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeSurfaceWaterSupply_JComboBox,
						IncludeSurfaceWaterSupply, JGUIUtil.NONE, null,	null ) ) {
					__IncludeSurfaceWaterSupply_JComboBox.select(
					IncludeSurfaceWaterSupply);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid IncludeSurfaceWaterSupply alue \""+
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
					"Existing command references an invalid IncludeGroundwaterOnlySupply value \""+
					IncludeGroundwaterOnlySupply + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( CropType != null ) {
			__CropType_JTextField.setText ( CropType );
		}
		if ( __NormalizeTotals_JComboBox != null ) {
			if ( NormalizeTotals == null ) {
				// Select default...
				__NormalizeTotals_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__NormalizeTotals_JComboBox, NormalizeTotals, JGUIUtil.NONE, null, null ) ) {
					__NormalizeTotals_JComboBox.select(NormalizeTotals);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid NormalizeTotals value \""+
					NormalizeTotals + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( FillStart != null ) {
			__FillStart_JTextField.setText ( FillStart );
		}
		if ( FillEnd != null ) {
			__FillEnd_JTextField.setText ( FillEnd );
		}
		if ( (__ParcelYear_JTextField != null) && (ParcelYear != null) ) {
			__ParcelYear_JTextField.setText ( ParcelYear );
		}
		if ( __FillDirection_JComboBox != null ) {
			if ( FillDirection == null ) {
				// Select default...
				__FillDirection_JComboBox.select ( 0 );
			}
			else {
				if ( JGUIUtil.isSimpleJComboBoxItem(
					__FillDirection_JComboBox, FillDirection, JGUIUtil.NONE, null, null ) ) {
					__FillDirection_JComboBox.select(FillDirection);
				}
				else {
					Message.printWarning ( 1, routine,
					"Existing command references an invalid FillDirection value \""+
					FillDirection + "\".  Select a different value or Cancel.");
					__error_wait = true;
				}
			}
		}
		if ( (__FillFlag_JTextField != null) && (FillFlag != null) ) {
			__FillFlag_JTextField.setText ( FillFlag );
		}
		if ( (__MaxIntervals_JTextField != null) && (MaxIntervals != null) ) {
			__MaxIntervals_JTextField.setText ( MaxIntervals );
		}
		if ( (__Constant_JTextField != null) && (Constant != null) ) {
			__Constant_JTextField.setText ( Constant );
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
	// Regardless, reset the command from the fields...
	props = new PropList(__command.getCommandName());
	ID = __ID_JTextField.getText().trim();
	props.add("ID=" + ID);
	if ( __IncludeSurfaceWaterSupply_JComboBox != null ) {
		IncludeSurfaceWaterSupply = __IncludeSurfaceWaterSupply_JComboBox.getSelected();
		props.add("IncludeSurfaceWaterSupply=" + IncludeSurfaceWaterSupply);
	}
	if ( __IncludeGroundwaterOnlySupply_JComboBox != null ) {
		IncludeGroundwaterOnlySupply = __IncludeGroundwaterOnlySupply_JComboBox.getSelected();
		props.add("IncludeGroundwaterOnlySupply=" + IncludeGroundwaterOnlySupply);
	}
	CropType = __CropType_JTextField.getText().trim();
	props.add("CropType=" + CropType);
	if ( __NormalizeTotals_JComboBox != null ) {
		NormalizeTotals = __NormalizeTotals_JComboBox.getSelected();
		props.add("NormalizeTotals=" + NormalizeTotals);
	}
	FillStart = __FillStart_JTextField.getText().trim();
	props.add("FillStart=" + FillStart);
	FillEnd = __FillEnd_JTextField.getText().trim();
	props.add("FillEnd=" + FillEnd);
	if ( __ParcelYear_JTextField != null ) {
		ParcelYear = __ParcelYear_JTextField.getText().trim();
		props.add("ParcelYear=" + ParcelYear);
	}
	if ( __FillDirection_JComboBox != null ) {
		FillDirection = __FillDirection_JComboBox.getSelected();
		props.add("FillDirection=" + FillDirection);
	}
	if ( __FillFlag_JTextField != null ) {
		FillFlag = __FillFlag_JTextField.getText().trim();
		props.add("FillFlag=" + FillFlag);
	}
	if ( __MaxIntervals_JTextField != null ) {
		MaxIntervals = __MaxIntervals_JTextField.getText().trim();
		props.add("MaxIntervals=" + MaxIntervals);
	}
	if ( __Constant_JTextField != null ) {
		Constant = __Constant_JTextField.getText().trim();
		props.add("Constant=" + Constant);
	}
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props.add("IfNotFound=" + IfNotFound );
	__command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
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
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}