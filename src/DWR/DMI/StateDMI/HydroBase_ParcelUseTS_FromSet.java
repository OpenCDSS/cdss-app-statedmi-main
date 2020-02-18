// HydroBase_ParcelUseTS_FromSet - class to store data from the HydroBase parcel_use_ts table,
// but with added data for StateDMI as needed by SetCropPatternTS and SetCropPatternTSFromList commands.

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

/**
Class to store data from the HydroBase parcel_use_ts table, but with added
data for StateDMI, specifically the CULocation ID, necessary to relate to dataset.
The records are used as additional data when processing crop pattern time series
that are not in HydroBase.
*/
public class HydroBase_ParcelUseTS_FromSet extends HydroBase_ParcelUseTS
{
	/**
	 * The CU Location ID.
	 */
	private String locationId = "";

	/**
	Constructor.
	*/
	public HydroBase_ParcelUseTS_FromSet() {
		super();
	}

	/**
	Returns _location_id
	@return _location_id
	*/
	public String getLocationID() {
		return this.locationId;
	}

	/**
	Sets CU Location_ID.
	@param CU Location ID.
	*/
	public void setLocationID (String locationId ) {
		this.locationId = locationId;
	}

	/** 
	Return a string representation of this object.
	@return a string representation of this object.
	*/
	public String toString() {
		return "StateDMI_" + super.toString() + "\n" +
			"LocationID:  " + this.locationId + "\n}";
	}

}