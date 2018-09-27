package rti.tscommandprocessor.core;

/**
This class provides an enumeration of possible EnsembleList command parameter values,
which are used to select which time series ensembles are in a list to be processed.
*/
public enum EnsembleListType {
	
    /**
     * AllMatchingEnsemble indicates that all time series ensembles matching a pattern should be in the list.
     */
    ALL_MATCHING_ENSEMBLEID("AllMatchingEnsembleID"),
    
    /**
     * AllEnsemble indicates that all time series ensemble should be in the list. 
     */
    ALL_ENSEMBLE("AllEnsemble"),
	
    /**
     * FirstMatchingEnsembleID indicates that only the first matching time series ensemble should be in the list. 
     */
    FIRST_MATCHING_ENSEMBLEID("FirstMatchingEnsembleID"),
    
    /**
     * LastMatchingEnsembleID indicates that only the last matching time series should be in the list. 
     */
    LAST_MATCHING_ENSEMBLEID("LastMatchingEnsembleID"),
	
	/**
	 * SelectedTS indicates that all selected time series ensemble should be in the list. 
	 */
	SELECTED_ENSEMBLE("SelectedEnsemble"),
    
    /**
     * SpecifiedEnsembleID indicates that all specified time series should be in the list.
     * Specified time series are those that are explicitly included in a list.
     */
    SPECIFIED_EnsembleID("SpecifiedEnsembleID");
    
    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Construct a time series ensemble list type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private EnsembleListType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
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
	public static EnsembleListType valueOfIgnoreCase(String name)
	{
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values
	    for ( EnsembleListType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    } 
	    return null;
	}
}