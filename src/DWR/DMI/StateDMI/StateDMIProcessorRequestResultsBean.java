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
