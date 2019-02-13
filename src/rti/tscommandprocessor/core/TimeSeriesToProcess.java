// TimeSeriesToProcess - A simple container class to hold results from the TSCommandProcessor.getTimeSeriesToProcess() request.

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

package rti.tscommandprocessor.core;

import java.util.List;
import java.util.Vector;

import RTi.TS.TS;

/**
A simple container class to hold results from the TSCommandProcessor.getTimeSeriesToProcess() request.
*/
public class TimeSeriesToProcess
{
    
/**
List of time series to be processed.
*/
List<TS> __tsList = new Vector<TS>();

/**
Array of time series positions (in the full time series list)
*/
int [] __tsPos = new int[0];

/**
List of error strings generated when determining the time series list (e.g., 5 time series expected
but only 4 matched.
*/
List<String>__errorList = new Vector<String>();

/**
Constructor.
@param tsList a non-null list of time series to process (may be zero length)
@param tsPos the positions of time series in the original time series list
@param errorList a list of string errors generated when determining the list of time series to process
(e.g., specific time series may have been requested but not all could be found)
*/
public TimeSeriesToProcess ( List<TS> tsList, int [] tsPos, List<String> errorList )
{
    __tsList = tsList;
    __tsPos = tsPos;
    __errorList = errorList;
}

/**
Return the list of errors from determining the time series list to process.
@return the list of errors from determining the time series list to process
*/
public List<String> getErrors ()
{
    return __errorList;
}

/**
Return the list of time series to process.
@return the list of time series to process
*/
public List<TS> getTimeSeriesList ()
{
    return __tsList;
}

/**
Return the positions of time series to process, in the main time series processor list.
@return the positions of time series to process, in the main time series processor list
*/
public int [] getTimeSeriesPositions ()
{
    return __tsPos;
}

}
