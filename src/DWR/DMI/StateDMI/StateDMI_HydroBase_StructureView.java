// StateDMI_HydroBase_StructureView - This class extends the base class and provides a simple boolean tracking
// mechanism to verify that the data are used only once.

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

import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import RTi.DMI.DMIUtil;

/**
This class extends the base class and provides a simple boolean tracking
mechanism to verify that the data are used only once.  The data are used to
hold SetCropPatternTS() records provided by the modeler, to supplement the values in HydroBase.
*/
public class StateDMI_HydroBase_StructureView extends HydroBase_StructureView {

// Indicate whether the data have been processed.
private boolean __hasBeenProcessed = false;

// Location identifier, which can be used for a WDID or other
protected String _location_id = DMIUtil.MISSING_STRING;

/**
Constructor.
*/
public StateDMI_HydroBase_StructureView ()
{
	super();
	throw new RuntimeException(
		"Class StateDMI_HydroBase_StructureView should not be used.  Use HydroBase_StructureView_FromSet instead.  Need to fix code.");
}

/**
Returns _location_id
@return _location_id
*/
public String getLocationID() {
	return _location_id;
}

/**
Indicate whether the object has previously been processed.  For example, in
StateDMI there may be multiple ReadCropPatternTSFromHydroBase() commands that
indicate to process the data with parcels.  The data should only be processed
the first matching case and a warning printed if processing is attempted
again.  This might occur, for example, if the period in the set command overlaps
2+ years of data from HydroBase.
@return false if the data have not been processed, true if data have been processed.
*/
public boolean hasBeenProcessed ()
{	return __hasBeenProcessed;
}

/**
Sets _location_id
@param Location ID value to put into _location_id
*/
public void setLocationID(String location_id) {
	_location_id = location_id;
}

/**
Set whether the data have been processed.
@param hasBeenProcessed Indicate whether the data have been processed.
*/
public void setHasBeenProcessed ( boolean hasBeenProcessed )
{	__hasBeenProcessed = hasBeenProcessed;
}

}