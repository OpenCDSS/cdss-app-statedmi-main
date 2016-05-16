//---------------------------------------------------------------------------
// StateDMIProcessor - class to process StateDMI commands
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 18 Sep 2002	J. Thomas Sapienza, RTi	Initial version.
// 25 Sep 2002	JTS, RTi		Javadoc'd
//---------------------------------------------------------------------------

package DWR.DMI.StateDMI;

import java.util.List;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.DefaultListModel;

/**
 * Class wraps around JList and provides functionality for dynamically 
 * changing the contents of JList.
 */
public class MutableJList extends JList {
/**
 * The List model that the list will use.
 */
private DefaultListModel __dlm;

/**
 * Whether to automatically update the list or not.
 */
boolean autoUpdate = true;

/**
 * Constructor.
 */
public MutableJList() {
	super();
	__dlm = new DefaultListModel();
	setModel(__dlm);
}	

/**
 * Adds an object to the JList in the last position.
 * 
 * @param o the object to add to the list.
 */
public void add(Object o) {
	__dlm.addElement(o);
	update();
}

/**
 * Adds an object to the JList in the given position.
 *
 * @param o the object to add to the list
 * @param pos the position in the list at which to put the object.
 */
public void add(Object o, int pos) {
	__dlm.add(pos, o);
	update();
}

/**
 * Returns autoUpdate
 *
 * @return autoUpdate
 */
public boolean getAutoupdate() {
	return autoUpdate;
}

/**
 * Returns the Object at the given position.
 *
 * @param pos the position at which to return the Object.
 * @return the Object at the given position.
 */
public Object getItem(int pos) {
	return __dlm.get(pos);
}

/**
 * Returns the number of items in the list.
 *
 * @return the number of items in the list.
 */
public int getItemCount() {
	return __dlm.size();
}

/**
 * Returns all the items in the list.
 *
 * @return all the items in the list.
 */
public List getItems() {
	List v = new Vector(__dlm.size());
	for (int i = 0; i < __dlm.size(); i++) {
		v.add(__dlm.get(i));
	}
	return v;
}

/**
 * Returns the number of items in the list.
 *
 * @return the number of items in the list.
 */
public int getListSize() {
	return __dlm.size();
}

/**
 * Returns only the selected items in the list.
 *
 * @return only the selected items in the list.
 */
public List getSelectedItems() {
	List v = new Vector(getSelectedSize());
	int[] indices = getSelectedIndices();
	for (int i = 0; i < indices.length; i++) {
		v.add(__dlm.get(indices[i]));
	}
	return v;
}

/**
 * Returns the index of the given object in the list.
 *
 * @param o the Object for which to search in the list.
 * @return the index of the given object in the list.
 */
public int indexOf(Object o) {
	return __dlm.indexOf(o);
}

/**
 * Returns the first index of the given object in the list, starting from a
 * certain point.
 *
 * @param o the Object for which to search in the list.
 * @param pos the position from which to start searching.
 * @return the index of the object in the list
 */
public int indexOf(Object o, int pos) {
	return __dlm.indexOf(o, pos);
}

/**
 * Removes the object at the given position.
 *
 * @param pos the position of the Object to be removed.
 */
public void remove(int pos) {
	__dlm.remove(pos);
	update();
}

/**
 * Removes a given Object from the list
 *
 * @param o the Object to be removed.
 */
public void remove(Object o) {
	__dlm.removeElement(o);
	update();
}

/**
 * Removes all objects form the list
 */
public void removeAll() {
	__dlm.removeAllElements();
	update();
}

/**
 * Sets the object at the given position.
 *
 * @param pos the position at which to set the object.
 * @param o the object to set in the position, overwriting the old object.
 */
public void set(int pos, Object o) {
	__dlm.set(pos, o);
	update();
}

/**
 * Selects a given row
 *
 * @param i the row to select
 */
public void select(int i) {
	setSelected(i);
}

/**
 * Returns the number of rows selected in the list.
 *
 * @return the number of rows selected in the list.
 */
public int getSelectedSize() {
	int[] indices = getSelectedIndices();
	return indices.length;
}

/**
 * Sets whether the list should be automatically updated any time it is changed
 * or not.
 *
 * @param update if true, the list will be auto updated every time it changes.
 */
public void setAutoUpdate(boolean update) {
	autoUpdate = update;
}

/**
 * Selects a given row
 *
 * @param i the row to select
 */
public void setSelected(int i) {
	setSelectedIndex(i);
}

/**
 * If autoupdate is true, update the list with the current list model
 */
public void update() {
	if (autoUpdate == true) {
		setModel(__dlm);
	}
}

/**
 * If autoupdate is true, update the list with the current list model.
 *
 * @param update autoupdate will be set to this value, so instead of calling
 * setAutoupdate(true) and then update(), both commands can be combined into
 * one by calling update(true);
 */
public void update(boolean update) {
	autoUpdate = update;
	if (autoUpdate == true) {	
		setModel(__dlm);
	}
}

}
