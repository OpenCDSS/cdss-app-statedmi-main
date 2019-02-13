// StateModWellRightIdFormatType - This enumeration defines ways to format StateMod well right identifiers.

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
