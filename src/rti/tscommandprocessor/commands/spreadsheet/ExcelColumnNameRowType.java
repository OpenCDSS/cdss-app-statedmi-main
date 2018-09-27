package rti.tscommandprocessor.commands.spreadsheet;

/**
Excel column name row type, which indicates how to determine column names in an Excel range.
*/
public enum ExcelColumnNameRowType
{
/**
Column names are auto-generated sequential number.
*/
COLUMN_N("ColumnN"),
/**
Column names are determined from the first row in a range.
*/
FIRST_ROW_IN_RANGE("FirstRowInRange"),
/**
Column names are determined from the row before a range.
*/
ROW_BEFORE_RANGE("RowBeforeRange");

/**
The name that should be displayed when the best fit type is used in UIs and reports.
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private ExcelColumnNameRowType(String displayName) {
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
public static ExcelColumnNameRowType valueOfIgnoreCase(String name)
{
	if ( name == null ) {
		return null;
	}
    ExcelColumnNameRowType [] values = values();
    for ( ExcelColumnNameRowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}