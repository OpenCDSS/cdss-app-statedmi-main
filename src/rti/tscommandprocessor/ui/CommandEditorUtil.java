package rti.tscommandprocessor.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import rti.tscommandprocessor.core.EnsembleListType;
import rti.tscommandprocessor.core.TSListType;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;

/**
This class provides methods for manipulating UI components of command editors.
Its intent is to centralize commonly used UI code.
*/
public abstract class CommandEditorUtil
{

/**
Add the EnsembleID parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the EnsembleID component (typically can be enabled/disabled elsewhere).
@param choices Choices for the EnsembleID, as String.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addEnsembleIDToEditorDialogPanel ( ItemListener itemlistener, KeyListener keylistener,
    JPanel panel, JLabel label, SimpleJComboBox choices, List<String> EnsembleIDs, int y )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    JGUIUtil.addComponent(panel, label,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    int size = 0;
    if ( EnsembleIDs == null ) {
        EnsembleIDs = new Vector<String> ();
    }
    size = EnsembleIDs.size();
    // Blank for default (not used)
    if ( size > 0 ) {
        EnsembleIDs.add ( 0, "" );
    }
    else {
        EnsembleIDs.add ( "" );
    }
    choices.setData ( EnsembleIDs );
    if ( itemlistener != null ) {
        choices.addItemListener ( itemlistener );
    }
    if ( keylistener != null ) {
        choices.getEditor().getEditorComponent().addKeyListener ( keylistener );
    }
    JGUIUtil.addComponent(panel, choices,
        1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    return y;
}

/**
Add the EnsembleList parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addEnsembleListToEditorDialogPanel (
    ItemListener dialog, JPanel panel, SimpleJComboBox choices, int y )
{
    return addEnsembleListToEditorDialogPanel ( dialog, panel, null, choices, y, null );
}

// TODO SAM 2016-02-26 Enable these in code
/**
Add the EnsembleList parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the row.  If null a default will be provided.
@param y The GridBagLayout vertical position.
@param note to override default note.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addEnsembleListToEditorDialogPanel (
    ItemListener dialog, JPanel panel, JLabel label, SimpleJComboBox choices, int y, JLabel note )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    if ( label == null ) {
        label = new JLabel ("Ensemble list:");
    }
    JGUIUtil.addComponent(panel, label, 0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> ensembleListChoices = new ArrayList<String>();
    //ensembleListChoices.add ( "" );
    //ensembleListChoices.add ( EnsembleListType.ALL_MATCHING_ENSEMBLEID.toString() );
    //ensembleListChoices.add ( EnsembleListType.ALL_ENSEMBLE.toString() );
    ensembleListChoices.add ( EnsembleListType.FIRST_MATCHING_ENSEMBLEID.toString() );
    //ensembleListChoices.add ( EnsembleListType.LAST_MATCHING_ENSEMBLEID.toString() );
    //ensembleListChoices.add ( EnsembleListType.SELECTED_ENSEMBLE.toString() );
    
    choices.setData ( ensembleListChoices );
    choices.addItemListener (dialog);
    JGUIUtil.addComponent(panel, choices,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( note == null ) {
        // Use a default note
        note = new JLabel ( "Optional - indicates the time series ensembles to process." );
    }
    JGUIUtil.addComponent(panel, note,
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    return y;
}

/**
Add the TSID parameter components to a command dialog.  A "*" will automatically be added.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the TSID component (typically can be enabled/disabled elsewhere).
@param choices Choices for the TSID, as String.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSIDToEditorDialogPanel ( ItemListener itemlistener, KeyListener keylistener,
    JPanel panel, JLabel label, SimpleJComboBox choices, List<String> tsids, int y )
{
    return addTSIDToEditorDialogPanel ( itemlistener, keylistener, panel, label, choices, tsids, y, true );
}
    
/**
Add the TSID parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the TSID component (typically can be enabled/disabled elsewhere).
@param choices Choices for the TSID, as String.
@param tsids the time series identifiers to use for choices, as a list of String
@param y The GridBagLayout vertical position.
@param add_asterisk If true, a "*" will be added to the list.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSIDToEditorDialogPanel ( ItemListener itemlistener, KeyListener keylistener,
    JPanel panel, JLabel label, SimpleJComboBox choices, List<String> tsids, int y, boolean add_asterisk )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    JGUIUtil.addComponent(panel, label,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    int size = 0;
    if ( tsids == null ) {
        tsids = new Vector<String> ();
    }
    size = tsids.size();
    // Blank for default
    if ( size > 0 ) {
        tsids.add ( 0, "" );
    }
    else {
        tsids.add ( "" );
    }
    boolean asteriskFound = false;
    for ( int i = 0; i < size; i++ ) {
        if ( tsids.get(i).equals("*") ) {
            asteriskFound = true;
            break;
        }
    }
    if ( add_asterisk && !asteriskFound) {
        // Add a "*" to let all time series be filled (put at end), if not already found...
        tsids.add ( "*" );
    }
    choices.setData ( tsids );
    if ( itemlistener != null ) {
        choices.addItemListener ( itemlistener );
    }
    if ( keylistener != null ) {
        choices.getEditor().getEditorComponent().addKeyListener ( keylistener );
    }
    JGUIUtil.addComponent(panel, choices,
    1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    return y;
}

/**
Add the TSList parameter notes components to a command dialog.
The standard set of TSList values is described (does not include SpecifiedTSID).
It is assumed that GridBagLayout is used for the layout.
@param panel The JPanel to which the controls are being added.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSListNotesToEditorDialogPanel ( JPanel panel, int y )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.ALL_MATCHING_TSID + " - add all previous time series with matching identifiers."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.ALL_TS + " - add all previous time series."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.ENSEMBLE_ID + " - add all time series for the ensemble."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.FIRST_MATCHING_TSID +
        " - add the first time series (before this command) with matching identifier."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.LAST_MATCHING_TSID +
        " - add the last time series (before this command) with matching identifier."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel (
        "  " + TSListType.SELECTED_TS + " - add time series selected with SelectTimeSeries() commands"),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    return y;
}

/**
Add the TSList parameter notes components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param panel The JPanel to which the controls are being added.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSListNotesWithSpecifiedTSIDToEditorDialogPanel ( JPanel panel, int y )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    y = addTSListNotesToEditorDialogPanel ( panel, y );
    JGUIUtil.addComponent(panel, new JLabel (
    "  " + TSListType.SPECIFIED_TSID + " - add time series selected from the list below"),
    0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    return y;
}

/**
Add the TSList parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSListToEditorDialogPanel (
    ItemListener dialog, JPanel panel, SimpleJComboBox choices, int y )
{
    return addTSListToEditorDialogPanel ( dialog, panel, null, choices, y, null );
}

/**
Add the TSList parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the row.  If null a default will be provided.
@param y The GridBagLayout vertical position.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSListToEditorDialogPanel (
    ItemListener dialog, JPanel panel, JLabel label, SimpleJComboBox choices, int y )
{
    return addTSListToEditorDialogPanel ( dialog, panel, label, choices, y, null );
}

/**
Add the TSList parameter components to a command dialog.
It is assumed that GridBagLayout is used for the layout.
@param dialog The dialog that is being added to.
@param panel The JPanel to which the controls are being added.
@param label Label for the row.  If null a default will be provided.
@param y The GridBagLayout vertical position.
@param note to override default note.
@return Incremented y reflecting the addition of a new vertical component group.
*/
public static int addTSListToEditorDialogPanel (
    ItemListener dialog, JPanel panel, JLabel label, SimpleJComboBox choices, int y, JLabel note )
{
    Insets insetsTLBR = new Insets(2,2,2,2);
    if ( label == null ) {
        label = new JLabel ("TS list:");
    }
    JGUIUtil.addComponent(panel, label, 0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> TSList_Vector = new Vector<String>();
    TSList_Vector.add ( "" );
    TSList_Vector.add ( TSListType.ALL_MATCHING_TSID.toString() );
    TSList_Vector.add ( TSListType.ALL_TS.toString() );
    TSList_Vector.add ( TSListType.ENSEMBLE_ID.toString() );
    TSList_Vector.add ( TSListType.FIRST_MATCHING_TSID.toString() );
    TSList_Vector.add ( TSListType.LAST_MATCHING_TSID.toString() );
    TSList_Vector.add ( TSListType.SELECTED_TS.toString() );
    
    choices.setData ( TSList_Vector );
    choices.addItemListener (dialog);
    JGUIUtil.addComponent(panel, choices,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( note == null ) {
        // Use a default note
        note = new JLabel ( "Optional - indicates the time series to process (default=AllTS)." );
    }
    JGUIUtil.addComponent(panel, note,
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    return y;
}

}