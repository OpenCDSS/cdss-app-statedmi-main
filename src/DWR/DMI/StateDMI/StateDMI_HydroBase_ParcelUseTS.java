package DWR.DMI.StateDMI;

import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import RTi.DMI.DMIUtil;

/**
Class to store data from the HydroBase parcel_use_ts table, but with added
data for StateDMI, in particular whether the source is ground or surface water.
The records are used as supplemental data when processing irrigated practice
time series that are not in HydroBase.
*/
public class StateDMI_HydroBase_ParcelUseTS extends HydroBase_ParcelUseTS
{

protected String _supply_type = DMIUtil.MISSING_STRING;
protected String _location_id = DMIUtil.MISSING_STRING;

private boolean __has_been_processed = false;

/**
Constructor.
*/
public StateDMI_HydroBase_ParcelUseTS() {
	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize()
throws Throwable {
	_supply_type = null;
	_location_id = null;
	
	super.finalize();
}

/**
Returns whether the record has been processed.
@return true if the record has been processed.
*/
public boolean getHasBeenProcessed()
{
	return __has_been_processed;
}

/**
Returns _location_id
@return _location_id
*/
public String getLocationID() {
	return _location_id;
}

/**
Returns _supply_type
@return _supply_type
*/
public String getSupply_type() {
	return _supply_type;
}

/**
Sets whether object has been processed.  Set true and check to avoid double-counting.
@param Location ID value to put into _location_id
*/
public void setHasBeenProcessed(boolean has_been_processed) {
	__has_been_processed = has_been_processed;
}

/**
Sets _location_id
@param Location ID value to put into _location_id
*/
public void setLocationID(String location_id) {
	_location_id = location_id;
}

/**
Sets _supply_type
@param supply_type value to put into _supply_type
*/
public void setSupply_type(String supply_type) {
	_supply_type = supply_type;
}

/** 
Return a string representation of this object.
@return a string representation of this object.
*/
public String toString() {
	return "StateDMI_" + super.toString() + "\n" +
		"Supply_type:        " + _supply_type + "\n" +
		"LocationID:               " + _location_id + "\n}";
	// FIXME SAM 2007-06-10 Remove redundant bracket
}

}
