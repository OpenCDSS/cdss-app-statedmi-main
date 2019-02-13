// ConditionOperatorType - Condition operator types, for example used in condition statements.

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

package rti.tscommandprocessor.commands.spreadsheet;

/**
Condition operator types, for example used in condition statements.
All apply to numbers, and additionally CONTAINS/STARTS_WITH/ENDS_WITH applies to strings.
It is up to code that uses the conditions to filter choices to appropriate values.
*/
public enum ConditionOperatorType
{
/**
Is value equal to.
*/
EQUAL_TO("=="),
/**
Is value not equal to.
*/
NOT_EQUAL_TO("!="),
/**
Is value less than.
*/
LESS_THAN("<"),
/**
Is value less than or equal to.
*/
LESS_THAN_OR_EQUAL_TO("<="),
/**
Is value greater than.
*/
GREATER_THAN(">"),
/**
Is value greater than or equal to.
*/
GREATER_THAN_OR_EQUAL_TO(">="),
/**
Does the value contain (for strings).
*/
CONTAINS("contains"),
/**
Does the value start with (for strings).
*/
STARTS_WITH("startswith"),
/**
Does the value end with (for strings).
*/
ENDS_WITH("endswith");

/**
The name that should be displayed when the best fit type is used in UIs and reports.
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private ConditionOperatorType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the statistic.  This is usually the same as the
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
public static ConditionOperatorType valueOfIgnoreCase(String name)
{
    ConditionOperatorType [] values = values();
    for ( ConditionOperatorType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
