package DWR.DMI.StateDMI;

/**
Indicate how a well right is defined.
*/
public enum DefineWellRightHowType
{
    EARLIEST_DATE("EarliestDate"),
    LATEST_DATE("LatestDate"),
    RIGHT_IF_AVAILABLE("RightIfAvailable");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private DefineWellRightHowType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
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
    public static DefineWellRightHowType valueOfIgnoreCase(String name)
    {
    	DefineWellRightHowType [] values = values();
        for ( DefineWellRightHowType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}