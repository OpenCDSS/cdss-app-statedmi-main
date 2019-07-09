// StateDMI_HydroBase_ParcelUseTS - Class to store data from the HydroBase parcel_use_ts table,
// but with added data for StateDMI,in particular whether the source is ground or surface water.

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
		"Supply_type: " + _supply_type + "\n" +
		"LocationID:  " + _location_id + "\n}";
	// FIXME SAM 2007-06-10 Remove redundant bracket
}

}