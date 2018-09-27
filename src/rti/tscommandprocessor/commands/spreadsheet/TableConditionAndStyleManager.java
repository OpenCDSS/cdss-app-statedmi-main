package rti.tscommandprocessor.commands.spreadsheet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import RTi.GR.GRColor;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;

/**
Manager to handle styling of table cells when written to Excel.
*/
public class TableConditionAndStyleManager
{

/**
Data table with data (subset will be output but initialize cell styles for all columns).
*/
private DataTable dataTable = null;

/**
Column numbers 0+ that will be output.
*/
private int [] includeColumnNumbers = null;
	
/**
Data table with style data.
*/
private DataTable styleTable = null;

/**
Data table with condition data.
*/
private DataTable conditionTable = null;

/**
Column number for "Column" column in condition table.
*/
private int conditionTableColumnNum = -1;

/**
Column number for "Condition" column in condition table.
*/
private int conditionTableConditionNum = -1;

/**
Column number for "Display" column in condition table.
*/
private int conditionTableDisplayNum = -1;

/**
Column number for "StyleID" column in condition table.
*/
private int conditionTableStyleIDNum = -1;

/**
Excel styles corresponding to defaults for the columns, will be augmented by cell styles.
Column styles are those defined by legacy command parameters and formatTable entries where condition is blank.
*/
private CellStyle [] columnStyles = null;

/**
Excel data formats corresponding to defaults for the columns, will be augmented by cell styles.
Column styles are those defined by legacy command parameters and formatTable entries where condition is blank.
*/
private DataFormat [] columnDataFormats = null;

/**
Excel styles corresponding to the style table rows.
The key is either "col" (0+) table column being output, which is the default style for the column
or "col-StyleID", which is the style for the column matching the StyleID.
*/
private Hashtable<String,CellStyle> cellStyleHash = new Hashtable<String,CellStyle>();

/**
Boolean to track whether cells styles have been generated.
This is checked when cell styles are requested.
Prior to these requests some column-based styles may be set.
*/
private boolean cellStylesInitialized = false;

/**
Workbook used to create styles.
*/
private Workbook wb = null;

/**
Constructor.
*/
public TableConditionAndStyleManager ( DataTable dataTable, int [] includeColumnNumbers, DataTable conditionTable, DataTable styleTable, Workbook wb )
{
	this.dataTable = dataTable;
	this.includeColumnNumbers = includeColumnNumbers;
	this.styleTable = styleTable;
	this.conditionTable = conditionTable;
	this.wb = wb;
	try {
		// The "Column" column provides glob-style patterns to match for the conditions.
		this.conditionTableColumnNum = conditionTable.getFieldIndex("Column");
	}
	catch ( Exception e ) {
		throw new RuntimeException("Condition table does not include \"Column\" column (" + e + ")");
	}
	try {
		this.conditionTableConditionNum = conditionTable.getFieldIndex("Condition");
	}
	catch ( Exception e ) {
		throw new RuntimeException("Condition table does not include \"Condition\" column (" + e + ")");
	}
	try {
		this.conditionTableDisplayNum = conditionTable.getFieldIndex("Display");
	}
	catch ( Exception e ) {
		// OK, optional - use condition string by default
	}
	try {
		this.conditionTableStyleIDNum = conditionTable.getFieldIndex("StyleID");
	}
	catch ( Exception e ) {
		throw new RuntimeException("Condition table does not include \"StyleID\" column (" + e + ")");
	}
	// Create styles for each column in the table
	this.columnStyles = new CellStyle[this.includeColumnNumbers.length];
	this.columnDataFormats = new DataFormat[this.includeColumnNumbers.length];
	for ( int i = 0; i < this.columnStyles.length; i++ ) {
		// The following creates default styles that can be modified by calls to:
		// - setColumnDataFormat().
		this.columnStyles[i] = wb.createCellStyle();
		this.columnDataFormats[i] = wb.createDataFormat();
	}
	// Cell styles will be a copy of the column style but with version for each format,
	// as initialized in initializeCellStyles() - don't do here because other methods
	// may be called to provide additional defaults.
}

/**
Return the CellStyle to use for the specified StyleID.
@param col output column (0+)
@param styleID style ID to look up
*/
public CellStyle getCellStyleForStyleID ( int col, String styleID )
throws Exception
{
	List<String> columnNames = new ArrayList<String>(1);
	columnNames.add("StyleID");
	List<Object> columnValues = new ArrayList<Object>(1);
	columnValues.add(styleID);
	if ( (styleID == null) || styleID.isEmpty() ) {
		return this.cellStyleHash.get("" + col);
	}
	else {
		return this.cellStyleHash.get("" + col + "-" + styleID.toUpperCase() );
	}
}

/**
TODO SAM 2015-07-11 In future may have condition object.
Return the condition string for a row in the condition table.
@return the condition string for a row in the condition table
@param irec the record (row) in the condition table (0+)
*/
public String getConditionString (int irec)
{
	String c = "";
	try {
		c = (String)this.conditionTable.getFieldValue(irec, this.conditionTableConditionNum);
	}
	catch ( Exception e ) {
		// Swallow for now
	}
	return c;
}

/**
Return the condition table used with the manager.
@return the condition table used with the manager
*/
public DataTable getConditionTable ()
{
	return this.conditionTable;
}

/**
TODO SAM 2015-07-11 In future may have condition object.
Return the display string for a row in the condition table.
@return the condition string for a row in the condition table
@param irec the record (row) in the condition table (0+)
*/
public String getDisplayString (int irec)
{
	String c = "";
	try {
		if ( this.conditionTableDisplayNum < 0 ) {
			c = "";
		}
		else {
			c = (String)this.conditionTable.getFieldValue(irec, this.conditionTableDisplayNum);
		}
	}
	catch ( Exception e ) {
		// Swallow for now
	}
	return c;
}

/**
Get the style to use for the requested table column when the column type is DateTime.
Currently this returns the column style, not cell-specific styling.
@return the style to use for the requested table column when the column type is DateTime
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value DateTime value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, DateTime value )
{
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	try {
		// TODO SAM 2015-06-24 Need to enable specific lookup for date/time columns
		return getCellStyleForStyleID(col, null); // Fall-through
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
}

/**
Get the style to use for the requested table column when the column type is Double.
The format table is used to find rows with matching column.
@return the style to use for the requested table column when the column type is Double
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value Double value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, Double value )
{	String routine = getClass().getSimpleName() + ".getStyle(Double)";
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	// Look up the column name
	String columnName = this.dataTable.getFieldName(this.includeColumnNumbers[col]);
	// See if the column name matches any rows in the format table
	List<TableRecord> matchedRowList = new ArrayList<TableRecord>();
	for ( int cRow = 0; cRow < this.conditionTable.getNumberOfRecords(); cRow++ ) {
		try {
			TableRecord row = this.conditionTable.getRecord(cRow);
			String column = row.getFieldValueString(this.conditionTableColumnNum);
			column = column.replace("*", ".*");
			if ( columnName.matches(column)) {
				// Format table row matches
				matchedRowList.add(row);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	// If here then matched the column in the format table with the requested column
	// Get the condition
	try {
		if ( matchedRowList.size() == 0 ) {
			// Return the default column style
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName +
					"\" did not match any condition table rows - defaulting to column style");
			}
			return this.columnStyles[col];
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName + "\" matched condition row - evaluating conditions");
			}
		}
		boolean doValue = false; // Is formatting based on cell value?
		for ( TableRecord row : matchedRowList ) {
			String cond = row.getFieldValueString(this.conditionTableConditionNum);
			String styleID = row.getFieldValueString(this.conditionTableStyleIDNum);
			// Evaluate the condition - if true return the cell style for the matching StyleID
			// The condition consists of Value1 Cond Value2 [AND Value3 Cond Value4] etc
			// Split the condition
			String [] parts = cond.trim().split(" ");
			for ( int iPart = 0; iPart < parts.length; iPart++ ) {
				String sValue1 = parts[iPart].trim();
				if ( (iPart > 0) && sValue1.equalsIgnoreCase("and") ) {
					// Continue so that the next clause will be evaluated.
					// If at any time the entire condition is false, nothing will be returned
					continue;
				}
				if ( sValue1.equalsIgnoreCase("${tablecell:value}") ) {
					doValue = true;
				}
				// TODO SAM 2015-05-09 in the future add other cell properties to be check, such as comments/annotations
				if ( doValue ) {
					ConditionOperatorType oper = ConditionOperatorType.valueOfIgnoreCase(parts[++iPart].trim());
					String sValue2 = parts[++iPart].trim();
					boolean value2Missing = false; // whether checking for missing
					if ( sValue2.equalsIgnoreCase("missing") || sValue2.equalsIgnoreCase("null")) {
						value2Missing = true;
					}
					double value2 = 0.0;
					if ( !value2Missing ) {
						value2 = Double.parseDouble(sValue2);
					}
					if ( Message.isDebugOn ) {
						Message.printDebug(1,routine,"Checking value \"" + value + "\" operator " + oper + " value2 " + value2 + " value2missing " + value2Missing );
					}
					boolean valueMatch = false;
					if ( oper == ConditionOperatorType.EQUAL_TO ) {
						if ( value2Missing ) {
							if ( (value == null) || value.isNaN() ) {
								valueMatch = true;
							}
						}
						else if ( (value != null) && !value.isNaN() && (value == value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.NOT_EQUAL_TO ) {
						if ( value2Missing ) {
							if ( (value != null) && !value.isNaN() ) {
								valueMatch = true;
							}
						}
						else if ( (value != null) && !value.isNaN() && (value != value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN ) {
						if ( (value != null) && (value < value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value <= value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN ) {
						if ( (value != null) && (value > value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value >= value2) ) {
							valueMatch = true;
						}
					}
					if ( !valueMatch ) {
						// Can break out of condition
						break;
					}
					// Only finish if there are no more clauses to evaluate
					if ( ((iPart + 1) == parts.length) && valueMatch ) {
						// The format table row matched for the value
						// Lookup the style and return 
						if ( Message.isDebugOn ) {
							Message.printDebug(1,routine,"Setting cell style for value " + value + " to " + styleID );
						}
						CellStyle cs = getCellStyleForStyleID ( col, styleID );
						//Message.printStatus(2,routine,"Cell style fill foreground color is " + cs.getFillForegroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill background color is " + cs.getFillBackgroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill pattern is " + cs.getFillPattern());
						return cs;
					}
				}
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(3,"getStyle",e);
		throw new RuntimeException(e);
	}
	try {
		if ( Message.isDebugOn ) {
			Message.printDebug(1,routine,"Setting cell style for value " + value + " to column default" );
		}
		return getCellStyleForStyleID(col, null); // Fall-through
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
}

/**
Get the style to use for the requested table column when the column type is Float.
This method calls the Double version.
The format table is used to find rows with matching column.
@return the style to use for the requested table column when the column type is Float
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value Float value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, Float value )
{
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	// Use the Double version to do the work
	if ( value == null ) {
		return getStyle ( col, (Double)null );
	}
	else {
		return getStyle ( col, new Double(value) );
	}
}

/**
Get the style to use for the requested table column when the column type is Integer.
This method calls the Long version.
The format table is used to find rows with matching column.
@return the style to use for the requested table column when the column type is Integer
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value Integer value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, Integer value )
{
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	// Use the Long version to do the work
	if ( value == null ) {
		return getStyle ( col, (Long)null );
	}
	else {
		return getStyle ( col, new Long(value) );
	}
}

/**
Get the style to use for the requested table column when the column type is Long.
The format table is used to find rows with matching column.
@return the style to use for the requested table column when the column type is Long
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value Long value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, Long value )
{	String routine = getClass().getSimpleName() + ".getStyle(Long)";
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	// Look up the column name
	String columnName = this.dataTable.getFieldName(this.includeColumnNumbers[col]);
	// See if the column name matches any rows in the format table
	List<TableRecord> matchedRowList = new ArrayList<TableRecord>();
	for ( int cRow = 0; cRow < this.conditionTable.getNumberOfRecords(); cRow++ ) {
		try {
			TableRecord row = this.conditionTable.getRecord(cRow);
			String column = row.getFieldValueString(this.conditionTableColumnNum);
			column = column.replace("*", ".*");
			if ( columnName.matches(column)) {
				// Format table row matches
				matchedRowList.add(row);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	// If here then matched the column in the format table with the requested column
	// Get the condition
	try {
		if ( matchedRowList.size() == 0 ) {
			// Return the default column style
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName +
					"\" did not match any condition table rows - defaulting to column style");
			}
			return this.columnStyles[col];
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName + "\" matched condition row - evaluating conditions");
			}
		}
		boolean doValue = false; // Is formatting based on cell value?
		for ( TableRecord row : matchedRowList ) {
			String cond = row.getFieldValueString(this.conditionTableConditionNum);
			String styleID = row.getFieldValueString(this.conditionTableStyleIDNum);
			// Evaluate the condition - if true return the cell style for the matching StyleID
			// The condition consists of Value1 Cond Value2 [AND Value3 Cond Value4] etc
			// Split the condition
			String [] parts = cond.trim().split(" ");
			for ( int iPart = 0; iPart < parts.length; iPart++ ) {
				String sValue1 = parts[iPart].trim();
				if ( (iPart > 0) && sValue1.equalsIgnoreCase("and") ) {
					// Continue so that the next clause will be evaluated.
					// If at any time the entire condition is false, nothing will be returned
					continue;
				}
				if ( sValue1.equalsIgnoreCase("${tablecell:value}") ) {
					doValue = true;
				}
				// TODO SAM 2015-05-09 in the future add other cell properties to be check, such as comments/annotations
				if ( doValue ) {
					ConditionOperatorType oper = ConditionOperatorType.valueOfIgnoreCase(parts[++iPart].trim());
					String sValue2 = parts[++iPart].trim();
					boolean value2Missing = false; // whether checking for missing
					if ( sValue2.equalsIgnoreCase("missing") || sValue2.equalsIgnoreCase("null")) {
						value2Missing = true;
					}
					long value2 = 0;
					if ( !value2Missing ) {
						value2 = Long.parseLong(sValue2);
					}
					if ( Message.isDebugOn ) {
						Message.printDebug(1,routine,"Checking value \"" + value + "\" operator " + oper + " value2 " + value2 + " value2missing " + value2Missing );
					}
					boolean valueMatch = false;
					if ( oper == ConditionOperatorType.EQUAL_TO ) {
						if ( value2Missing ) {
							if ( value == null ) {
								valueMatch = true;
							}
						}
						else if ( (value != null) && (value == value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.NOT_EQUAL_TO ) {
						if ( value2Missing ) {
							if ( value != null ) {
								valueMatch = true;
							}
						}
						else if ( (value != null) && (value != value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN ) {
						if ( (value != null) && (value < value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value <= value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN ) {
						if ( (value != null) && (value > value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value >= value2) ) {
							valueMatch = true;
						}
					}
					if ( !valueMatch ) {
						// Can break out of condition
						break;
					}
					// Only finish if there are no more clauses to evaluate
					if ( ((iPart + 1) == parts.length) && valueMatch ) {
						// The format table row matched for the value
						// Lookup the style and return 
						if ( Message.isDebugOn ) {
							Message.printDebug(1,routine,"Setting cell style for value " + value + " to " + styleID );
						}
						CellStyle cs = getCellStyleForStyleID ( col, styleID );
						//Message.printStatus(2,routine,"Cell style fill foreground color is " + cs.getFillForegroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill background color is " + cs.getFillBackgroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill pattern is " + cs.getFillPattern());
						return cs;
					}
				}
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(3,"getStyle",e);
		throw new RuntimeException(e);
	}
	try {
		if ( Message.isDebugOn ) {
			Message.printDebug(1,routine,"Setting cell style for value " + value + " to column default" );
		}
		return getCellStyleForStyleID(col, null); // Fall-through
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
}

/**
Get the style to use for the requested table column when the column type is String.
The format table is used to find rows with matching column.
@return the style to use for the requested table column when the column type is String
@param col output column 0+ corresponding to this.includeColumnNumbers
@param value String value to compare against conditions to determine style for cell
*/
public CellStyle getStyle ( int col, String value )
{	String routine = getClass().getSimpleName() + ".getStyle";
	if ( !this.cellStylesInitialized ) {
		initializeCellStyles();
	}
	// Look up the column name
	String columnName = this.dataTable.getFieldName(this.includeColumnNumbers[col]);
	// See if the column name matches any rows in the format table
	List<TableRecord> matchedRowList = new ArrayList<TableRecord>();
	for ( int cRow = 0; cRow < this.conditionTable.getNumberOfRecords(); cRow++ ) {
		try {
			TableRecord row = this.conditionTable.getRecord(cRow);
			String column = row.getFieldValueString(this.conditionTableColumnNum);
			column = column.replace("*", ".*");
			if ( columnName.matches(column)) {
				// Format table row matches
				matchedRowList.add(row);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	// If here then matched the column in the format table with the requested column
	// Get the condition
	try {
		if ( matchedRowList.size() == 0 ) {
			// Return the default column style
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName +
					"\" did not match any condition table rows - defaulting to column style");
			}
			return this.columnStyles[col];
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Column name \"" + columnName + "\" matched condition row - evaluating conditions");
			}
		}
		boolean doValue = false; // Is formatting based on cell value?
		for ( TableRecord row : matchedRowList ) {
			String cond = row.getFieldValueString(this.conditionTableConditionNum);
			String styleID = row.getFieldValueString(this.conditionTableStyleIDNum);
			// Evaluate the condition - if true return the cell style for the matching StyleID
			// The condition consists of Value1 Cond Value2 [AND Value3 Cond Value4] etc
			// Split the condition
			String [] parts = cond.trim().split(" ");
			for ( int iPart = 0; iPart < parts.length; iPart++ ) {
				String sValue1 = parts[iPart].trim();
				if ( (iPart > 0) && sValue1.equalsIgnoreCase("and") ) {
					// Continue so that the next clause will be evaluated.
					// If at any time the entire condition is false, nothing will be returned
					continue;
				}
				if ( sValue1.equalsIgnoreCase("${tablecell:value}") ) {
					doValue = true;
				}
				// TODO SAM 2015-05-09 in the future add other cell properties to be check, such as comments/annotations
				if ( doValue ) {
					ConditionOperatorType oper = ConditionOperatorType.valueOfIgnoreCase(parts[++iPart].trim());
					String sValue2 = parts[++iPart].trim(); // Value to check against, e.g., "missing" or a string
					boolean value2Missing = false; // whether checking for missing
					if ( sValue2.equalsIgnoreCase("missing") || sValue2.equalsIgnoreCase("null")) {
						value2Missing = true; // Will check cell value for missing (null or empty string)
					}
					String value2 = sValue2;
					if ( Message.isDebugOn ) {
						Message.printDebug(1,routine,"Checking value \"" + value + "\" operator " + oper + " value2 " + value2 + " value2missing " + value2Missing );
					}
					boolean valueMatch = false;
					if ( oper == ConditionOperatorType.EQUAL_TO ) {
						if ( value2Missing ) {
							if ( (value == null) || value.isEmpty() ) {
								// Actual cell value is null or empty so it is missing
								valueMatch = true;
							}
						}
						else if ( (value != null) && value.equals(value2) ) {
							// String match is case-sensitive
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.NOT_EQUAL_TO ) {
						if ( value2Missing ) {
							if ( (value != null) && !value.isEmpty() ) {
								// String is not null or missing so it is not equal to "missing"
								valueMatch = true;
							}
						}
						else if ( (value != null) && !value.isEmpty() && (value != value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.CONTAINS ) {
						if ( value2Missing ) {
							if ( (value == null) || value.isEmpty() ) {
								// String "contains" missing, which is true
								valueMatch = true;
							}
						}
						else if ( (value != null) && (value.indexOf(value2) >= 0) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.STARTS_WITH ) {
						if ( value2Missing ) {
							if ( (value == null) || value.isEmpty() ) {
								// String is missing, so it starts with missing, which is true
								valueMatch = true;
							}
						}
						else if ( (value != null) && value.startsWith(value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.ENDS_WITH ) {
						if ( value2Missing ) {
							if ( (value == null) || value.isEmpty() ) {
								// String is missing, so it ends with missing, which is true
								valueMatch = true;
							}
						}
						else if ( (value != null) && value.endsWith(value2) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN ) {
						if ( (value != null) && (value.compareTo(value2) < 0) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.LESS_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value.compareTo(value2) <= 0) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN ) {
						if ( (value != null) && (value.compareTo(value2) > 0) ) {
							valueMatch = true;
						}
					}
					else if ( oper == ConditionOperatorType.GREATER_THAN_OR_EQUAL_TO ) {
						 if ( (value != null) && (value.compareTo(value2) >= 0) ) {
							valueMatch = true;
						}
					}
					if ( !valueMatch ) {
						// Can break out of condition
						break;
					}
					// Only finish if there are no more clauses to evaluate
					if ( ((iPart + 1) == parts.length) && valueMatch ) {
						// The format table row matched for the value
						// Lookup the style and return 
						if ( Message.isDebugOn ) {
							Message.printDebug(1,routine,"Setting cell style for value " + value + " to " + styleID );
						}
						CellStyle cs = getCellStyleForStyleID ( col, styleID );
						//Message.printStatus(2,routine,"Cell style fill foreground color is " + cs.getFillForegroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill background color is " + cs.getFillBackgroundColor());
	                    //Message.printStatus(2,routine,"Cell style fill pattern is " + cs.getFillPattern());
						return cs;
					}
				}
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(3,"getStyle",e);
		throw new RuntimeException(e);
	}
	try {
		if ( Message.isDebugOn ) {
			Message.printDebug(1,routine,"Setting cell style for value " + value + " to column default" );
		}
		return getCellStyleForStyleID(col, null); // Fall-through
	}
	catch ( Exception e ) {
		throw new RuntimeException ( e );
	}
}

/**
TODO SAM 2015-07-11 In future may have condition object.
Return the style ID for a row in the condition table.
@return the style ID for a row in the condition table.
@param irec the row (0+) in the condition table
*/
public String getStyleIDForCondition (int irec)
{
	String s = "";
	try {
		s = (String)this.conditionTable.getFieldValue(irec, this.conditionTableStyleIDNum);
	}
	catch ( Exception e ) {
		// Swallow for now
	}
	return s;
}

/**
Initialize the cell styles for combinations of formats and styles.
The output is a hashtable that includes styles for each column:
<ol>
<li> for default column style, hash key is the string column number (0+)</li>
<li> for specific style, hash key is string column number + "-" + style ID</li>
</ol>
*/
private void initializeCellStyles ()
{	String routine = getClass().getSimpleName() + ".initializeCellStyles";
	// By this point the this.columnStyles will have been configured so set the styles for the columns to these defaults.
	// Get the style table columns
	// "StyleID" - the string that identifies the style
	int styleIDColNum = -1;
	try {
		styleIDColNum = this.styleTable.getFieldIndex("StyleID");
	}
	catch ( Exception e ) {
		styleIDColNum = -1;
	}
	// "FillForegroundColor" - the fill color for the cell
	int fillFGColorColNum = -1;
	try {
		fillFGColorColNum = this.styleTable.getFieldIndex("FillForegroundColor");
	}
	catch ( Exception e ) {
		fillFGColorColNum = -1;
	}
	String key;
	for ( int iColStyle = 0; iColStyle < this.columnStyles.length; iColStyle++ ) {
		key = "" + this.includeColumnNumbers[iColStyle];
		this.cellStyleHash.put(key, this.columnStyles[iColStyle]);
		//Message.printStatus(2, routine, "Initialized column cell style \"" + key + "\"");
		// Next, loop through the styles in the style table and add for each column
		// TODO SAM 2015-06-09 Need to filter these down using format table so only have styles for specific columns.
		//   Currently extra styles are added that may not be used - could be a an issue with large tables
		TableRecord styleRow = null;
		String styleID = null;
		String fillFGColor = null;
		for ( int iStyle = 0; iStyle < styleTable.getNumberOfRecords(); iStyle++ ) {
			try {
				styleRow = this.styleTable.getRecord(iStyle);
				styleID = styleRow.getFieldValueString(styleIDColNum);
				if ( fillFGColorColNum >= 0 ) {
					fillFGColor = styleRow.getFieldValueString(fillFGColorColNum);
				}
			}
			catch ( Exception e ) {
				continue;
			}
			// Create a new style that is a copy of the column style
			XSSFCellStyle cs = (XSSFCellStyle)this.wb.createCellStyle();
			cs.cloneStyleFrom(this.columnStyles[iColStyle]);
			// Now set more specific styles based on information in the style table
			if ( fillFGColorColNum >= 0 ) {
				// Color may have various forms so parse using GRColor method
				// Apparently foreground default is black
				// Setting background does nothing for solid since foreground is used.
				GRColor c = GRColor.parseColor(fillFGColor);
				//Message.printStatus(2,routine,"Color is \"" + fillFGColor + "\" " + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
				cs.setFillForegroundColor(new XSSFColor(c));
				//cs.setFillBackgroundColor(new XSSFColor(c));
				//cs.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
				//cs.setFillBackgroundColor(IndexedColors.RED.getIndex());
				cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
				//Message.printStatus(2,routine,"Setting style fill foreground color for " + fillFGColor + " to " + c);
				if ( Message.isDebugOn ) {
					Message.printDebug(1,routine,"Setting style fill foreground color for " + fillFGColor + " to " +
						cs.getFillForegroundColor() + " fill pattern " + cs.getFillPattern());
				}
			}
			else {
				if ( Message.isDebugOn ) {
					Message.printDebug(1,routine,"No fill foreground for style \"" + styleID + "\"");
				}
			}
			key = "" + this.includeColumnNumbers[iColStyle] + "-" + styleID.toUpperCase();
			this.cellStyleHash.put(key, cs);
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Initialized cell style \"" + key + "\"");
			}
		}
	}
	this.cellStylesInitialized = true;
}

/**
Set the data format for a column.
@param col position (0+) in includeColumns array
@param format Excel format (e.g., "0" for integer)
*/
public void setColumnDataFormat(int col, String format)
{
	this.columnStyles[col].setDataFormat(this.columnDataFormats[col].getFormat(format));
}

}