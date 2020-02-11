// SetCropCharacteristics_JDialog - Editor for SetCropCharacteristics() command.

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

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Editor for SetCropCharacteristics() command.
*/
@SuppressWarnings("serial")
public class SetCropCharacteristics_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false;
private boolean __first_time = true;
private JTextArea __command_JTextArea=null;
private JTextField __CropType_JTextField = null;
private SimpleJComboBox __IfNotFound_JComboBox = null;
private JTextField __PlantingMonth_JTextField = null;
private JTextField __PlantingDay_JTextField = null;
private JTextField __HarvestMonth_JTextField = null;
private JTextField __HarvestDay_JTextField = null;
private JTextField __DaysToFullCover_JTextField = null;
private JTextField __LengthOfSeason_JTextField = null;
private SimpleJComboBox __SpringFrostFlag_JComboBox = null;
private JTextField __EarliestMoistureUseTemp_JTextField = null;
private SimpleJComboBox __FallFrostFlag_JComboBox = null;
private JTextField __LatestMoistureUseTemp_JTextField = null;
private JTextField __MaxRootZoneDepth_JTextField = null;
private JTextField __MaxAppDepth_JTextField = null;
private JTextField __DaysTo2ndCut_JTextField = null;
private JTextField __DaysTo3rdCut_JTextField = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private SetCropCharacteristics_Command __command = null;
private boolean __ok = false;

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetCropCharacteristics_JDialog (JFrame parent, SetCropCharacteristics_Command command ) {
	super(parent, true);
	initialize (parent, command);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
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
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput () {
	
	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String CropType = __CropType_JTextField.getText().trim();
	String PlantingMonth = __PlantingMonth_JTextField.getText().trim();
	String PlantingDay = __PlantingDay_JTextField.getText().trim();
	String HarvestMonth = __HarvestMonth_JTextField.getText().trim();
	String HarvestDay = __HarvestDay_JTextField.getText().trim();
	String DaysToFullCover = __DaysToFullCover_JTextField.getText().trim();
	String LengthOfSeason = __LengthOfSeason_JTextField.getText().trim();
	String EarliestMoistureUseTemp = __EarliestMoistureUseTemp_JTextField.getText().trim();
	String LatestMoistureUseTemp=__LatestMoistureUseTemp_JTextField.getText().trim();
	String MaxRootZoneDepth = __MaxRootZoneDepth_JTextField.getText().trim();
	String MaxAppDepth = __MaxAppDepth_JTextField.getText().trim();
	String DaysTo2ndCut = __DaysTo2ndCut_JTextField.getText().trim();
	String DaysTo3rdCut = __DaysTo3rdCut_JTextField.getText().trim();
	String SpringFrostFlag = StringUtil.getToken (__SpringFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( SpringFrostFlag == null ) {
		 SpringFrostFlag = "";
	}
	String FallFrostFlag = StringUtil.getToken ( __FallFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( FallFrostFlag == null ) {
		FallFrostFlag = "";
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();
	
	__error_wait = false;
	
	if (CropType.length() > 0) {
		props.set("CropType", CropType);
	}
	if (PlantingMonth.length() > 0) {
		props.set("PlantingMonth", PlantingMonth);
	}
	if (PlantingDay.length() > 0) {
		props.set("PlantingDay", PlantingDay);
	}
	if (HarvestMonth.length() > 0 ) {
		props.set("HarvestMonth", HarvestMonth);
	}
	if (HarvestDay.length() > 0 ) {
		props.set("HarvestDay", HarvestDay);
	}
	if (DaysToFullCover.length() > 0 ) {
		props.set("DaysToFullCover", DaysToFullCover);
	}
	if (LengthOfSeason.length() > 0 ) {
		props.set("LengthOfSeason", LengthOfSeason);
	}
	if (EarliestMoistureUseTemp.length() > 0 ) {
		props.set("EarliestMoistureUseTemp", EarliestMoistureUseTemp);
	}
	if (LatestMoistureUseTemp.length() > 0 ) {
		props.set("LatestMoistureUseTemp", LatestMoistureUseTemp);
	}
	if (MaxRootZoneDepth.length() > 0 ) {
		props.set("MaxRootZoneDepth", MaxRootZoneDepth);
	}
	if (MaxAppDepth.length() > 0 ) {
		props.set("MaxAppDepth", MaxAppDepth);
	}
	if (DaysTo2ndCut.length() > 0 ) {
		props.set("DaysTo2ndCut", DaysTo2ndCut);
	}
	if (DaysTo3rdCut.length() > 0 ) {
		props.set("DaysTo3rdCut", DaysTo3rdCut);
	}
	if (SpringFrostFlag.length() > 0 ) {
		props.set("SpringFrostFlag", SpringFrostFlag);
	}
	if (FallFrostFlag.length() > 0 ) {
		props.set("FallFrostFlag", FallFrostFlag);
	}
	if (IfNotFound.length() > 0 ) {
		props.set("IfNotFound", IfNotFound);
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
private void commitEdits()
{
	String CropType = __CropType_JTextField.getText().trim();
	String PlantingMonth = __PlantingMonth_JTextField.getText().trim();
	String PlantingDay = __PlantingDay_JTextField.getText().trim();
	String HarvestMonth = __HarvestMonth_JTextField.getText().trim();
	String HarvestDay = __HarvestDay_JTextField.getText().trim();
	String DaysToFullCover = __DaysToFullCover_JTextField.getText().trim();
	String LengthOfSeason = __LengthOfSeason_JTextField.getText().trim();
	String EarliestMoistureUseTemp = __EarliestMoistureUseTemp_JTextField.getText().trim();
	String LatestMoistureUseTemp=__LatestMoistureUseTemp_JTextField.getText().trim();
	String MaxRootZoneDepth = __MaxRootZoneDepth_JTextField.getText().trim();
	String MaxAppDepth = __MaxAppDepth_JTextField.getText().trim();
	String DaysTo2ndCut = __DaysTo2ndCut_JTextField.getText().trim();
	String DaysTo3rdCut = __DaysTo3rdCut_JTextField.getText().trim();
	String SpringFrostFlag = StringUtil.getToken (__SpringFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( SpringFrostFlag == null ) {
		 SpringFrostFlag = "";
	}
	String FallFrostFlag = StringUtil.getToken ( __FallFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( FallFrostFlag == null ) {
		FallFrostFlag = "";
	}
	String IfNotFound = __IfNotFound_JComboBox.getSelected();

	__command.setCommandParameter("CropType", CropType);
	__command.setCommandParameter("PlantingMonth", PlantingMonth);
	__command.setCommandParameter("PlantingDay", PlantingDay);
	__command.setCommandParameter("HarvestMonth", HarvestMonth);
	__command.setCommandParameter("HarvestDay", HarvestDay);
	__command.setCommandParameter("DaysToFullCover", DaysToFullCover);
	__command.setCommandParameter("LengthOfSeason", LengthOfSeason);
	__command.setCommandParameter("EarliestMoistureUseTemp", EarliestMoistureUseTemp);
	__command.setCommandParameter("LatestMoistureUseTemp", LatestMoistureUseTemp);
	__command.setCommandParameter("MaxRootZoneDepth", MaxRootZoneDepth);
	__command.setCommandParameter("MaxAppDepth", MaxAppDepth);
	__command.setCommandParameter("DaysTo2ndCut", DaysTo2ndCut);
	__command.setCommandParameter("DaysTo3rdCut", DaysTo3rdCut);
	__command.setCommandParameter("SpringFrostFlag", SpringFrostFlag);
	__command.setCommandParameter("FallFrostFlag", FallFrostFlag);
	__command.setCommandParameter("IfNotFound", IfNotFound);
}
	
/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Vector of String containing the command.
*/
private void initialize (JFrame parent, SetCropCharacteristics_Command command )
{	__command = command;

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
		"This command sets (edits) crop characteristics data, using the crop type (name) to look up the crop."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
		"The crop name can contain a * wildcard pattern to match one or more crops."),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"If the crop name does not contain a * wildcard pattern and does not match a crop name,"),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(paragraph, new JLabel (
		"the crop will be added if the \"If not found\" option is set to " + __command._Add + "." ),
		0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, ++y, 7, 1, 0, 1, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Crop type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__CropType_JTextField = new JTextField("*",10);
	__CropType_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __CropType_JTextField,
		1, y, 3, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Required - crop type to set (use * for wildcard)"),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Planting month:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PlantingMonth_JTextField = new JTextField (3);
	__PlantingMonth_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PlantingMonth_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Day:"),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__PlantingDay_JTextField = new JTextField (3);
	__PlantingDay_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __PlantingDay_JTextField,
		3, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Month: 1-12, Day: 1-31."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Harvest month:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__HarvestMonth_JTextField = new JTextField (3);
	__HarvestMonth_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __HarvestMonth_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Day:"),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__HarvestDay_JTextField = new JTextField (3);
	__HarvestDay_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __HarvestDay_JTextField,
		3, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Month: 1-12, Day: 1-31."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Days to full cover:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DaysToFullCover_JTextField = new JTextField (5);
	__DaysToFullCover_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __DaysToFullCover_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Days to full cover"),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Length of season:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LengthOfSeason_JTextField = new JTextField (5);
	__LengthOfSeason_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __LengthOfSeason_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Days from planting."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Earliest moisture use temperature:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__EarliestMoistureUseTemp_JTextField = new JTextField (5);
	__EarliestMoistureUseTemp_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __EarliestMoistureUseTemp_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Degrees F"),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Latest moisture use temperature:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__LatestMoistureUseTemp_JTextField = new JTextField (5);
	__LatestMoistureUseTemp_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __LatestMoistureUseTemp_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Degrees F"),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Maximum root zone depth:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__MaxRootZoneDepth_JTextField = new JTextField (5);
	__MaxRootZoneDepth_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __MaxRootZoneDepth_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Feet."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Maximum application depth:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__MaxAppDepth_JTextField = new JTextField (5);
	__MaxAppDepth_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel,__MaxAppDepth_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Inches."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    List<String> frost_Vector = new Vector<String>(3);
	frost_Vector.add ( "0 - Mean Temperature" );
	frost_Vector.add ( "1 - 28 F frost" );
	frost_Vector.add ( "2 - 32 F frost" );
    JGUIUtil.addComponent(main_JPanel,new JLabel ("Spring frost flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__SpringFrostFlag_JComboBox = new SimpleJComboBox( false );
	__SpringFrostFlag_JComboBox.setData( frost_Vector);
	__SpringFrostFlag_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __SpringFrostFlag_JComboBox,
		1, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Indicate how frost is handled."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Fall frost flag:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__FallFrostFlag_JComboBox = new SimpleJComboBox( false );
	__FallFrostFlag_JComboBox.setData( frost_Vector);
	__FallFrostFlag_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __FallFrostFlag_JComboBox,
		1, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Indicate how frost is handled."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Days to 2nd Cut:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DaysTo2ndCut_JTextField = new JTextField (5);
	__DaysTo2ndCut_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel,__DaysTo2ndCut_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Alfalfa only - days between 1st and 2nd cuts."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Days to 3rd Cut:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DaysTo3rdCut_JTextField = new JTextField (5);
	__DaysTo3rdCut_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel,__DaysTo3rdCut_JTextField,
		1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Alfalfa only - days between 2nd and 3rd cuts."),
		5, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("If not found:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IfNotFound_Vector = new Vector<String>(5);
    IfNotFound_Vector.add ( "" );
	IfNotFound_Vector.add ( __command._Add );
	IfNotFound_Vector.add ( __command._Ignore );
	IfNotFound_Vector.add ( __command._Warn );
	IfNotFound_Vector.add ( __command._Fail );
	__IfNotFound_JComboBox = new SimpleJComboBox(false);
	__IfNotFound_JComboBox.setData( IfNotFound_Vector );
	__IfNotFound_JComboBox.addItemListener (this);
    JGUIUtil.addComponent(main_JPanel, __IfNotFound_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - action if no match is found (default=" + __command._Warn + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (8,40);
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
{	String routine = "SetCropCharacteristics_JDialog.refresh";
	__error_wait = false;
	String CropType = "";
	String PlantingMonth = "";
	String PlantingDay = "";
	String HarvestMonth = "";
	String HarvestDay = "";
	String DaysToFullCover = "";
	String LengthOfSeason = "";
	String EarliestMoistureUseTemp = "";
	String LatestMoistureUseTemp = "";
	String MaxRootZoneDepth = "";
	String MaxAppDepth = "";
	String SpringFrostFlag = "";
	String FallFrostFlag = "";
	String DaysTo2ndCut = "";
	String DaysTo3rdCut = "";
	String IfNotFound = "";
	PropList props = null;
	
	if (__first_time) {
		__first_time = false;
	
		// Get the properties from the command
		props = __command.getCommandParameters();
		CropType = props.getValue ( "CropType" );
		PlantingMonth = props.getValue ( "PlantingMonth" );
		PlantingDay = props.getValue ( "PlantingDay" );
		HarvestMonth = props.getValue ( "HarvestMonth" );
		HarvestDay = props.getValue ( "HarvestDay" );
		DaysToFullCover = props.getValue ( "DaysToFullCover" );
		LengthOfSeason = props.getValue ( "LengthOfSeason" );
		EarliestMoistureUseTemp = props.getValue("EarliestMoistureUseTemp" );
		LatestMoistureUseTemp = props.getValue("LatestMoistureUseTemp");
		MaxRootZoneDepth = props.getValue ( "MaxRootZoneDepth" );
		MaxAppDepth = props.getValue ( "MaxAppDepth" );
		SpringFrostFlag = props.getValue ( "SpringFrostFlag" );
		FallFrostFlag = props.getValue ( "FallFrostFlag" );
		DaysTo2ndCut = props.getValue ( "DaysTo2ndCut" );
		DaysTo3rdCut = props.getValue ( "DaysTo3rdCut" );
		IfNotFound = props.getValue ( "IfNotFound" );

		if ( CropType != null ) {
			__CropType_JTextField.setText(CropType);
		}
		if ( PlantingMonth != null ) {
			__PlantingMonth_JTextField.setText(PlantingMonth);
		}
		if ( PlantingDay != null ) {
			__PlantingDay_JTextField.setText(PlantingDay);
		}
		if ( HarvestMonth != null ) {
			__HarvestMonth_JTextField.setText(HarvestMonth);
		}
		if ( HarvestDay != null ) {
			__HarvestDay_JTextField.setText(HarvestDay);
		}
		if ( DaysToFullCover != null ) {
			__DaysToFullCover_JTextField.setText(DaysToFullCover);
		}
		if ( LengthOfSeason != null ) {
			__LengthOfSeason_JTextField.setText(LengthOfSeason);
		}
		if ( EarliestMoistureUseTemp != null ) {
			__EarliestMoistureUseTemp_JTextField.setText(EarliestMoistureUseTemp);
		}
		if ( LatestMoistureUseTemp != null ) {
			__LatestMoistureUseTemp_JTextField.setText(LatestMoistureUseTemp);
		}
		if ( MaxRootZoneDepth != null ) {
			__MaxRootZoneDepth_JTextField.setText(MaxRootZoneDepth);
		}
		if ( MaxAppDepth != null ) {
			__MaxAppDepth_JTextField.setText(MaxAppDepth);
		}
		if ( SpringFrostFlag == null ) {
			// Select default...
			__SpringFrostFlag_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __SpringFrostFlag_JComboBox, true, " ", 0, 0, SpringFrostFlag, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nSpringFrostFlag " +
				"value \""+ SpringFrostFlag + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( FallFrostFlag == null ) {
			// Select default...
			__FallFrostFlag_JComboBox.select ( 0 );
		}
		else {
			try {
				JGUIUtil.selectTokenMatches ( __FallFrostFlag_JComboBox, true, " ", 0, 0, FallFrostFlag, null );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Existing command references an invalid\nFallFrostFlag value \""+ FallFrostFlag +
				"\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( DaysTo2ndCut != null ) {
			__DaysTo2ndCut_JTextField.setText(DaysTo2ndCut);
		}
		if ( DaysTo3rdCut != null ) {
			__DaysTo3rdCut_JTextField.setText(DaysTo3rdCut);
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

	// Always get from the dialog fields...

	CropType = __CropType_JTextField.getText().trim();
	PlantingMonth = __PlantingMonth_JTextField.getText().trim();
	PlantingDay = __PlantingDay_JTextField.getText().trim();
	HarvestMonth = __HarvestMonth_JTextField.getText().trim();
	HarvestDay = __HarvestDay_JTextField.getText().trim();
	DaysToFullCover = __DaysToFullCover_JTextField.getText().trim();
	LengthOfSeason = __LengthOfSeason_JTextField.getText().trim();
	EarliestMoistureUseTemp = __EarliestMoistureUseTemp_JTextField.getText().trim();
	LatestMoistureUseTemp=__LatestMoistureUseTemp_JTextField.getText().trim();
	MaxRootZoneDepth = __MaxRootZoneDepth_JTextField.getText().trim();
	MaxAppDepth = __MaxAppDepth_JTextField.getText().trim();
	DaysTo2ndCut = __DaysTo2ndCut_JTextField.getText().trim();
	DaysTo3rdCut = __DaysTo3rdCut_JTextField.getText().trim();
	SpringFrostFlag = StringUtil.getToken (__SpringFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( SpringFrostFlag == null ) {
		 SpringFrostFlag = "";
	}
	FallFrostFlag = StringUtil.getToken ( __FallFrostFlag_JComboBox.getSelected(), " -",
		StringUtil.DELIM_SKIP_BLANKS, 0 );
	if ( FallFrostFlag == null ) {
		FallFrostFlag = "";
	}
	IfNotFound = __IfNotFound_JComboBox.getSelected();
	props = new PropList(__command.getCommandName());
	props.add("CropType=" + CropType );
	props.add("PlantingMonth=" + PlantingMonth);
	props.add("PlantingDay=" + PlantingDay);
	props.add("HarvestMonth=" + HarvestMonth);
	props.add("HarvestDay=" + HarvestDay);
	props.add("DaysToFullCover=" + DaysToFullCover);
	props.add("LengthOfSeason=" + LengthOfSeason);
	props.add("EarliestMoistureUseTemp=" + EarliestMoistureUseTemp );
	props.add("LatestMoistureUseTemp=" + LatestMoistureUseTemp);
	props.add("MaxRootZoneDepth=" + MaxRootZoneDepth);
	props.add("MaxAppDepth=" + MaxAppDepth );
	props.add("DaysTo2ndCut=" + DaysTo2ndCut);
	props.add("DaysTo3rdCut=" + DaysTo3rdCut);
	props.add("SpringFrostFlag=" + SpringFrostFlag );
	props.add("FallFrostFlag=" + FallFrostFlag );
	props.add("IfNotFound=" + IfNotFound);
	__command_JTextArea.setText( __command.toString(props) );
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
public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt)	{}

}
