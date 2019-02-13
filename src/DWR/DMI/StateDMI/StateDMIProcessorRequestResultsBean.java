// StateDMIProcessorRequestResultsBean - object to hold results from processor request

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

import RTi.Util.IO.PropList;

public class StateDMIProcessorRequestResultsBean
implements RTi.Util.IO.CommandProcessorRequestResultsBean
{
	
// Data to return.
private PropList __props = new PropList("");

// Warning text, zero length if no warning.
private String __warning = "";

// Warning recommendation text, zero length if no warning.
private String __warning_recommendation = "";

/**
Return the results from a request.  This is guaranteed to be non-null,
but may be empty.
@return PropList of results.
*/
public PropList getResultsPropList()
{	return __props;
}

/**
Return the warning text when a request threw an exception or had
problems.  Return null if processing was successful.
@return Test suitable for a warning message.
*/
public String getWarningText()
{	return __warning;
}

/**
Return the warning text when a request threw an exception or had
problems.  Return null if processing was successful.
@return Test suitable for a warning message.
*/
public String getWarningRecommendationText()
{	return __warning_recommendation;
}

/**
Set the warning text when a request throws an exception or had
problems.
@param warning Text suitable for a warning message.
*/
public void setWarningText( String warning )
{	__warning = warning;
}

/**
Set the warning text when a request throww an exception or had
problems.
@param recommendation Text suitable for a warning message recommendation.
*/
public void setWarningRecommendationText( String recommendation )
{	__warning_recommendation = recommendation;
}

}
