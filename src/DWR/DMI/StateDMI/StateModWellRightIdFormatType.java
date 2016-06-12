package DWR.DMI.StateDMI;

/**
This enumeration defines ways to format StateMod well right identifiers.
*/
public enum StateModWellRightIdFormatType
{
    /**
    Right is that from the original source plus trailing .01, .02, .03 to uniquely identify.
    */
    RIGHTID_NN("RightID.NN");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a StateMod well right ID format type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateModWellRightIdFormatType(String displayName) {
        this.displayName = displayName;
    }

/**
Return the display name for the type.  This is usually similar to the
value but using appropriate mixed case.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@return the enumeration value given a string name (case-independent), or null if not matched.
*/
public static StateModWellRightIdFormatType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
	StateModWellRightIdFormatType [] values = values();
    for ( StateModWellRightIdFormatType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}