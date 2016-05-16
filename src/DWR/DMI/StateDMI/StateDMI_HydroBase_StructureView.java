package DWR.DMI.StateDMI;

import DWR.DMI.HydroBaseDMI.HydroBase_StructureView;
import RTi.DMI.DMIUtil;

/**
This class extends the base class and provides a simple boolean tracking
mechanism to verify that the data are used only once.  The data are used to
hold setCropPatternTS() records provided by the modeler, to supplement the
values in HydroBase.
*/
public class StateDMI_HydroBase_StructureView extends HydroBase_StructureView {

// Indicate whether the data have been processed.
private boolean __has_been_processed = false;

// Location identifier, which can be used for a WDID or other
protected String _location_id = DMIUtil.MISSING_STRING;

/**
Constructor.
*/
public StateDMI_HydroBase_StructureView ()
{	super();
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
StateDMI there may be multiple readCropPatternTSFromHydroBase() commands that
indicate to process the data with parcels.  The data should only be processed
the first matching case and a warning printed if processing is attempted
again.  This might occur, for example, if the period in the set command overlaps
2+ years of data from Hydrobase.
@return false if the data have not been processed, true if data have been
processed.
*/
public boolean hasBeenProcessed ()
{	return __has_been_processed;
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
@param has_been_processed Indicate whether the data have been processed.
*/
public void setHasBeenProcessed ( boolean has_been_processed )
{	__has_been_processed = has_been_processed;
}

}