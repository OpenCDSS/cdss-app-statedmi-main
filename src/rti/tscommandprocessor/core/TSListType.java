package rti.tscommandprocessor.core;

/**
This class provides an enumeration of possible TSList command parameter values.
*/
public enum TSListType {
	
    /**
     * AllMatchingTS indicates that all time series matching a pattern should be in the list.
     */
    ALL_MATCHING_TSID("AllMatchingTSID"),
    
    /**
     * AllTS indicates that all time series should be in the list. 
     */
    ALL_TS("AllTS"),
	
	/**
	 * EnsembleID indicates that all time series in the ensemble should be in the list.
	 */
	ENSEMBLE_ID("EnsembleID"),
	
    /**
     * FirstMatchingTSID indicates that only the first matching time series should be in the list. 
     */
    FIRST_MATCHING_TSID("FirstMatchingTSID"),
    
    /**
     * LastMatchingTSID indicates that only the last matching time series should be in the list. 
     */
    LAST_MATCHING_TSID("LastMatchingTSID"),
	
	/**
	 * SelectedTS indicates that all selected time series should be in the list. 
	 */
	SELECTED_TS("SelectedTS"),
    
    /**
     * SpecifiedTSID indicates that all specified time series should be in the list.
     * Specified time series are those that are explicitly included in a list.
     */
    SPECIFIED_TSID("SpecifiedTSID"),
    
    /**
     * SpecifiedTSID indicates that all specified time series should be in the list. 
     */
    TSPOSITION("TSPosition");
    
    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Construct a time series list type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TSListType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Equals method to match legacy TSListType class behavior, and to prevent common programming error
     * of using the equals method instead of ==.
     */
    public boolean equals ( String tsListType ) {
        if ( tsListType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }
	
    /**
     * Return the display name for the time series list type.  This is usually the same as the
     * value but using appropriate mixed case.
     * @return the display name.
     */
    @Override
    public String toString() {
        return displayName;
    }
	
	/**
	 * Return the enumeration value given a string name (case-independent).
	 * @return the enumeration value given a string name (case-independent), or null if not matched.
	 */
	public static TSListType valueOfIgnoreCase(String name)
	{
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values
	    for ( TSListType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    } 
	    return null;
	}
}