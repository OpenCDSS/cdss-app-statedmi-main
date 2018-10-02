package rti.tscommandprocessor.core;

import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.StringBuffer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import DWR.DMI.StateDMI.StateDMI_Processor;
import RTi.TS.TS;
import RTi.TS.TSEnsemble;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil_SortTimeSeries;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessRunner;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import rti.tscommandprocessor.commands.table.TableTimeSeriesMath_Command;

/**
This class contains static utility methods to support TSCommandProcessor.  These methods
are here to prevent the processor from getting to large and in some cases because code is being migrated.
*/
public abstract class TSCommandProcessorUtil
{

/**
PrintWriter for regression test results report.
*/
private static PrintWriter __regression_test_fp = null;
/**
Count of regression tests that fail.
*/
private static int __regressionTestFailCount = 0;
/**
Count of regression tests that pass.
*/
private static int __regressionTestPassCount = 0;
/**
Count of regression tests that are disabled.
*/
private static int __regressionTestDisabledCount = 0;
/**
Table to contain regression test results.
*/
private static DataTable __regressionTestTable = null;

/**
Append a time series to the processor time series results list.
@param processor the CommandProcessor to use to get data.
@param command Command for which to get the working directory.
@param ts Time series to append.
@param return the number of warnings generated.
*/
public static int appendEnsembleToResultsEnsembleList ( CommandProcessor processor, Command command, TSEnsemble tsensemble )
{   String routine = "TSCommandProcessorUtil.appendEnsembleToResultsEnsembleList";
    PropList request_params = new PropList ( "" );
    request_params.setUsingObject ( "TSEnsemble", tsensemble );
    int warning_level = 3;
    int warning_count = 0;
    CommandStatus status = null;
    if ( command instanceof CommandStatusProvider ) {
        status = ((CommandStatusProvider)command).getCommandStatus();
    }
    //CommandProcessorRequestResultsBean bean = null;
    try { //bean =
        processor.processRequest( "AppendEnsemble", request_params );
    }
    catch ( Exception e ) {
        String message = "Error requesting AppendEnsemble(TSEnsemble=\"...\") from processor).";
        // This is a low-level warning that the user should not see.
        // A problem would indicate a software defect so return the warning count as a trigger.
        Message.printWarning(warning_level, routine, e);
        Message.printWarning(warning_level, routine, message );
        if ( status != null ) {
            status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(
                    CommandStatusType.FAILURE, message,
                    "Check the log file for details - report the problem to software support."));
        }
        ++warning_count;
    }
    return warning_count;
}

/**
Count of output lines in regression output report body (basically a count of the tests).
*/
private static int __regressionTestLineCount = 0;
/**
Add a record to the regression test results report and optionally results table.
The report is a simple text file that indicates whether a test passed.
The data table is a table maintained by the processor to report on test results.
@param processor CommandProcessor that is being run.
@param isEnabled whether the command file is enabled (it is useful to list all tests even if not
enabled in order to generate an inventory of disabled tests that need cleanup)
@param runTimeMs run time for the command in milliseconds
@param testPassFail whether the test was a success or failure (it is possible for the test to
be a successful even if the command file failed, if failure was expected)
@param expectedStatus the expected status (as a string)
@param maxSeverity the maximum severity from the command file that was run.
@param testCommandFile the full path to the command file that was run.
*/
public static void appendToRegressionTestReport(CommandProcessor processor, boolean isEnabled, long runTimeMs,
    String testPassFail, String expectedStatus, CommandStatusType maxSeverity,
    String testCommandFile )
{
    ++__regressionTestLineCount;
    String indicator = " ";
    if ( testPassFail.toUpperCase().indexOf("FAIL") >= 0 ) {
        indicator = "*";
        ++__regressionTestFailCount;
    }
    else {
        ++__regressionTestPassCount;
    }
    String lineCount = StringUtil.formatString(__regressionTestLineCount,"%5d");
    String enabled = "TRUE   ";
    //String runTime = "        ";
    if ( !isEnabled ) {
        ++__regressionTestDisabledCount;
        enabled = "FALSE  ";
        testPassFail = "    ";
    }
    //runTime = StringUtil.formatString(runTimeMs,"%7d");
    String delim = "|";
    if ( __regression_test_fp != null ) {
        __regression_test_fp.println (
            lineCount + delim +
            enabled + delim +
            // Moved the runTime to the table because in the report it makes it difficult to "diff" previous and current reports
            //runTime + delim +
            indicator + StringUtil.formatString(testPassFail,"%-4.4s") + indicator + delim +
            StringUtil.formatString(expectedStatus,"%-10.10s") + delim +
            StringUtil.formatString(maxSeverity,"%-10.10s") + " " + delim + testCommandFile);
    }
    if ( __regressionTestTable != null ) {
    	TableRecord rec = __regressionTestTable.emptyRecord();
    	// Look up the column numbers using the names from the table initialization - make sure they agree!
    	int col = -1;
    	try {
    		col = __regressionTestTable.getFieldIndex("Num");
    		rec.setFieldValue(col, new Integer(__regressionTestLineCount));
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Enabled");
    		rec.setFieldValue(col, enabled.trim());
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Run Time (ms)");
    		rec.setFieldValue(col, runTimeMs);
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Test Pass/Fail");
    		rec.setFieldValue(col, testPassFail.trim());
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Commands Expected Status");
    		rec.setFieldValue(col, expectedStatus.trim());
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Commands Actual Status");
    		rec.setFieldValue(col, ""+maxSeverity);
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		col = __regressionTestTable.getFieldIndex("Command File");
    		rec.setFieldValue(col, testCommandFile);
    	}
    	catch ( Exception e ) {
    		// Just ignore setting
    	}
    	try {
    		__regressionTestTable.addRecord(rec);
    	}
    	catch ( Exception e ) {
    		// Just ignore adding
    	}
    }
}

/**
Append a time series list to the processor time series results list.
Errors should not result and are logged in the log file and command status, indicating a software problem.
@param processor the CommandProcessor to use to get data.
@param command Command for which to get the working directory.
@param tslist List of time series to append.
@param return the number of warnings generated.
*/
public static int appendTimeSeriesListToResultsList ( CommandProcessor processor, Command command, List<TS> tslist )
{
    int wc = 0;
    int size = 0;
    if ( tslist != null ) {
        size = tslist.size();
    }
    for ( int i = 0; i < size; i++ ) {
        wc += appendTimeSeriesToResultsList ( processor, command, tslist.get(i) );
    }
    return wc;
}
	
/**
Append a time series to the processor time series results list.
@param processor the CommandProcessor to use to get data.
@param command Command for which to get the working directory.
@param ts Time series to append.
@param return the number of warnings generated.
*/
public static int appendTimeSeriesToResultsList ( CommandProcessor processor, Command command, TS ts )
{	String routine = "TSCommandProcessorUtil.appendTimeSeriesToResultsList";
	PropList request_params = new PropList ( "" );
	request_params.setUsingObject ( "TS", ts );
    int warning_level = 3;
    int warning_count = 0;
    CommandStatus status = null;
    if ( command instanceof CommandStatusProvider ) {
        status = ((CommandStatusProvider)command).getCommandStatus();
    }
	//CommandProcessorRequestResultsBean bean = null;
	try { //bean =
		processor.processRequest( "AppendTimeSeries", request_params );
	}
	catch ( Exception e ) {
		String message = "Error requesting AppendTimeSeries(TS=\"...\") from processor).";
        // This is a low-level warning that the user should not see.
        // A problem would indicate a software defect so return the warning count as a trigger.
		Message.printWarning(warning_level, routine, e);
		Message.printWarning(warning_level, routine, message );
        if ( status != null ) {
            status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(
                    CommandStatusType.FAILURE, message,
                    "Check the log file for details - report the problem to software support."));
        }
        ++warning_count;
	}
    return warning_count;
}

/**
Close the regression test report file.
*/
public static void closeRegressionTestReportFile ()
{
    if ( __regression_test_fp == null ) {
        return;
    }
    __regression_test_fp.println ( "#----+-------+-------+------+----------+-----------+------------------" +
    "---------------------------------------------------------------------------" );
    int totalRun = getRegressionTestFailCount() + getRegressionTestPassCount();
    __regression_test_fp.println ( "FAIL count     = " + getRegressionTestFailCount() +
        ", " + StringUtil.formatString(100.0*(double)getRegressionTestFailCount()/(double)totalRun,"%.3f")+ "%");
    __regression_test_fp.println ( "PASS count     = " + getRegressionTestPassCount() +
        ", " + StringUtil.formatString(100.0*(double)getRegressionTestPassCount()/(double)totalRun,"%.3f")+ "%");
    __regression_test_fp.println ( "Disabled count = " + getRegressionTestDisabledCount() );
    __regression_test_fp.println ( "#--------------------------------" );
    __regression_test_fp.println ( "Total          = " +
        (totalRun + getRegressionTestDisabledCount()) );
    
    __regression_test_fp.close();
    __regression_test_fp = null;
}

// TODO SAM 2016-03-24 May move this to class that focuses on UI
// and make generic so as to not hard-code TSTool documentation path
// Maybe have some configuration/hooks on the processor to define more properties like ${CommandDocRootURL}
/**
Display the command documentation.  This will use the default web browser.
@param command command to display documentation.
*/
public static void displayCommandDocumentation ( Command command ) {
	try {
		// TODO SAM 2016-03-23 This is a prototype of how to do interactive documentation - put in utility code
		String docURL = null;
		boolean isCommandPlugin = command.getIsCommandPlugin();
		if ( isCommandPlugin ) {
			// Envision that command documentation would be in:
			// $home/.tstool/plugin-command/CommandName/doc/CommandName.html or CommandName.pdf
			Prop prop = command.getCommandProcessor().getProp("UserHomeDirURL");
			if ( prop != null ) {
				docURL = prop.getValue() + "/plugin-command/" + command.getCommandName() + "/doc/" +
				command.getCommandName() + ".html";
			}
		}
		else {
			Prop prop = command.getCommandProcessor().getProp("InstallDirURL");
			if ( prop != null ) {
				docURL = prop.getValue() + "/doc/UserManual/html/TSTool-Vol2-CommandReference/" +
				command.getCommandName() + "/" + command.getCommandName() + ".html";
			}
		}
		if ( docURL != null ) {
			Desktop desktop = Desktop.getDesktop();
		    desktop.browse ( new URI(docURL) );
		}
	}
	catch ( Exception err ) {
		Message.printWarning(1,"","Error displaying documentation (" + err + ")");
	}
}

/**
Expand a string containing processor-level properties.  For example, a parameter value like
"${WorkingDir}/morepath" will be expanded to include the working directory.
The characters \" will be replaced by a literal quote (").  Properties that cannot be expanded will remain.
@param processor the CommandProcessor that has a list of named properties.
@param command the command that is being processed (may be used later for context sensitive values).
@param parameterValue the parameter value being expanded, containing literal substrings and optionally ${Property} properties.
*/
public static String expandParameterValue( CommandProcessor processor, Command command, String parameterValue )
{   String routine = "TSCommandProcessorUtil.expandParameterValue";
    if ( (parameterValue == null) || (parameterValue.length() == 0) ) {
        // Just return what was provided.
        return parameterValue;
    }
    // First replace escaped characters.
    // TODO SAM 2009-04-03 Evaluate this
    // Evaluate whether to write a general method for this - for now only handle // \" and \' replacement.
    parameterValue = parameterValue.replace("\\\"", "\"" );
    parameterValue = parameterValue.replace("\\'", "'" );
    // Else see if the parameter value can be expanded to replace ${} symbolic references with other values
    // Search the parameter string for $ until all processor parameters have been resolved
    int searchPos = 0; // Position in the "parameter_val" string to search for ${} references
    int foundPos; // Position when leading ${ is found
    int foundPosEnd; // Position when ending } is found
    String propname = null; // Whether a property is found that matches the $ symbol
    String delimStart = "${";
    String delimEnd = "}";
    while ( searchPos < parameterValue.length() ) {
        foundPos = parameterValue.indexOf(delimStart, searchPos);
        foundPosEnd = parameterValue.indexOf(delimEnd, (searchPos + delimStart.length()));
        if ( (foundPos < 0) && (foundPosEnd < 0)  ) {
            // No more $ property names, so return what we have.
            return parameterValue;
        }
        // Else found the delimiter so continue with the replacement
        //Message.printStatus ( 2, routine, "Found " + delimStart + " at position [" + foundPos + "]");
        // Get the name of the property
        propname = parameterValue.substring((foundPos+2),foundPosEnd);
        // Try to get the property from the processor
        // TODO SAM 2007-12-23 Evaluate whether to skip null.  For now show null in result.
        Object propval = null;
        String propvalString = "";
        try {
            propval = processor.getPropContents ( propname );
            // The following should work for all representations as long as the toString() does not truncate
            propvalString = "" + propval;
        }
        catch ( Exception e ) {
            // Keep the original literal value to alert user that property could not be expanded
            propvalString = delimStart + propname + delimEnd;
        }
        if ( propval == null ) {
            // Keep the original literal value to alert user that property could not be expanded
            propvalString = delimStart + propname + delimEnd;
        }
        // If here have a property
        StringBuffer b = new StringBuffer();
        // Append the start of the string
        if ( foundPos > 0 ) {
            b.append ( parameterValue.substring(0,foundPos) );
        }
        // Now append the value of the property.
        b.append ( propvalString );
        // Now append the end of the original string if anything is at the end...
        if ( parameterValue.length() > (foundPosEnd + 1) ) {
            b.append ( parameterValue.substring(foundPosEnd + 1) );
        }
        // Now reset the search position to finish evaluating whether to expand the string.
        parameterValue = b.toString();
        searchPos = foundPos + propvalString.length(); // Expanded so no need to consider delim*
        if ( Message.isDebugOn ) {
            Message.printDebug( 1, routine, "Expanded parameter value is \"" + parameterValue +
                "\" searchpos is now " + searchPos + " in string \"" + parameterValue + "\"" );
        }
    }
    return parameterValue;
}

/**
Expand a string using:
<ol>
<li> time series processor ${Property} strings</li>
<li> time series ensemble ${tsensemble:Property} strings</li>
</ol>
If a property string is not found, it will remain without being replaced.
@param processor The processor that is being used, if a ${property} needs to be expanded (if passed as null,
the processor property won't be expanded)
@param ensemble Time series ensemble to be used for metadata string.
@param s String to expand, which includes format specifiers and literal strings.
@param status CommandStatus to add messages to if problems occur, or null to ignore.
@param commandPhase command phase (for logging), can be null to ignore logging.
*/
public static String expandTimeSeriesEnsembleMetadataString ( CommandProcessor processor, TSEnsemble ensemble, String s,
    CommandStatus status, CommandPhaseType commandPhase )
{   String routine = "TSCommandProcessorUtil.expandTimeSeriesEnsembleMetadataString";
    if ( s == null ) {
        return "";
    }
    //Message.printStatus(2, routine, "After formatLegend(), string is \"" + s2 + "\"" );
    // Now replace ${tsensemble:Property} and ${Property} strings with properties from the processor
    // Put the most specific first so it is matched first
    String [] startStrings = { "${tsensemble:", "${" };
    int [] startStringsLength = { 13, 2 };
    String [] endStrings = { "}", "}" };
    boolean isTsProp = false;
    Object propO;
    // Loop through and expand the string, first by expanding the time series properties, which have a more specific
    // ${tsensemble: starting pattern and then the processor properties starting with ${
    for ( int ipat = 0; ipat < startStrings.length; ipat++ ) {
        int start = 0; // Start at the beginning of the string
        int pos2 = 0;
        isTsProp = false;
        if ( ipat == 0 ) {
            // Time series property corresponding to startStrings[0] for loop below.
            // The fundamental logic is the same but getting the property is different whether from TS or processor
            isTsProp = true;
        }
        while ( pos2 < s.length() ) {
            int pos1 = StringUtil.indexOfIgnoreCase(s, startStrings[ipat], start );
            if ( pos1 >= 0 ) {
                // Find the end of the property
                pos2 = s.indexOf( endStrings[ipat], pos1 );
                if ( pos2 > 0 ) {
                    // Get the property...
                    String propname = s.substring(pos1+startStringsLength[ipat],pos2);
                    //Message.printStatus(2, routine, "Property=\"" + propname + "\" isTSProp=" + isTsProp + " pos1=" + pos1 + " pos2=" + pos2 );
                    // By convention if the property is not found, keep the original string so can troubleshoot property issues
                    String propvalString = s.substring(pos1,(pos2 + 1));
                    if ( isTsProp ) {
                        // Get the property out of the time series
                        propO = ensemble.getProperty(propname);
                        if ( propO == null ) {
                            if ( status != null ) {
                                String message = "Time series ensemble \"" + ensemble.getEnsembleID() + "\" property=\"" +
                                propname + "\" has a null value.";
                                Message.printWarning ( 3,routine, message );
                                status.addToLog ( commandPhase,
                                    new CommandLogRecord(CommandStatusType.FAILURE,
                                        message, "Verify that the property is set for the time series ensemble." ) );
                            }
                        }
                        else {
                            // This handles conversion of integers to strings
                            propvalString = "" + propO;
                        }
                    }
                    else if ( processor != null ) {
                        // Not a time series property so this is a processor property
                        // Get the property from the processor properties
                        PropList request_params = new PropList ( "" );
                        request_params.set ( "PropertyName", propname );
                        CommandProcessorRequestResultsBean bean = null;
                        boolean processorError = false;
                        try {
                            bean = processor.processRequest( "GetProperty", request_params);
                        }
                        /* TODO SAM 2015-07-05 Need to evaluate whether error should be absorbed and ${property} remain unexpanded, as javadoc'ed
                        catch ( UnrecognizedRequestException e ) {
                        	// Property is not set - OK
                        	processorError = true;
                        }
                        */
                        catch ( Exception e ) {
                        	// Unexpected exception
                            if ( status != null ) {
                                String message = "Error requesting GetProperty(Property=\"" + propname + "\") from processor.";
                                Message.printWarning ( 3,routine, message );
                                status.addToLog ( commandPhase,
                                    new CommandLogRecord(CommandStatusType.FAILURE,
                                        message, "Report the problem to software support." ) );
                            }
                            processorError = true;
                        }
                        if ( !processorError ) {
                            if ( bean == null ) {
                                // Not an exception but the property was not found in the processor
                                if ( status != null ) {
                                    String message =
                                        "Unable to find property from processor using GetProperty(Property=\"" + propname + "\").";
                                    Message.printWarning ( 3,routine, message );
                                    status.addToLog ( commandPhase,
                                        new CommandLogRecord(CommandStatusType.FAILURE,
                                            message, "Verify that the property name is valid - must match case." ) );
                                }
                            }
                            else {
                                // Have a property, but still need to check for null value
                                // TODO SAM 2013-09-09 should this be represented as "null" in output?
                                PropList bean_PropList = bean.getResultsPropList();
                                Object o_PropertyValue = bean_PropList.getContents ( "PropertyValue" );
                                if ( o_PropertyValue == null ) {
                                    if ( status != null ) {
                                        String message =
                                            "Null PropertyValue returned from processor for GetProperty(PropertyName=\"" + propname + "\").";
                                        Message.printWarning ( 3, routine, message );
                                        status.addToLog ( commandPhase,
                                            new CommandLogRecord(CommandStatusType.FAILURE, message,
                                                "Verify that the property name is valid - must match case." ) );
                                    }
                                }
                                else {
                                    // This handles conversion of integers and dates to strings
                                    propvalString = "" + o_PropertyValue;
                                }
                            }
                        }
                    }
                    // Replace the string and continue to evaluate s2
                    s = s.substring ( 0, pos1 ) + propvalString + s.substring (pos2 + 1);
                    // Next search will be at the end of the expanded string (end delimiter will be skipped in any case)
                    start = pos1 + propvalString.length();
                }
                else {
                    // No closing character so leave the property string as is and march on...
                    start = pos1 + startStringsLength[ipat];
                    if ( start > s.length() ) {
                        break;
                    }
                }
            }
            else {
                // No more ${} property strings so done processing properties.
                // If checking time series properties will then check global properties in next loop
                break;
            }
        }
    }
    return s;
}

/**
Expand a string using:
<ol>
<li> time series % formatting strings using TS.formatLegend()</li>
<li> time series processor ${Property} strings</li>
<li> time series  ${ts:Property} strings</li>
</ol>
If a property string is not found, it will remain without being replaced.
@param processor The processor that is being used, if a ${property} needs to be expanded (if passed as null,
the processor property won't be expanded)
@param ts Time series to be used for metadata string.
@param s String to expand, which includes format specifiers and literal strings.
@param status CommandStatus to add messages to if problems occur, or null to ignore.
@param commandPhase command phase (for logging), can be null to ignore logging.
*/
public static String expandTimeSeriesMetadataString ( CommandProcessor processor, TS ts, String s,
    CommandStatus status, CommandPhaseType commandPhase )
{   String routine = "TSCommandProcessorUtil.expandTimeSeriesMetadataString";
    if ( s == null ) {
        return "";
    }
    // First expand using the % characters...
    String s2 = ts.formatLegend ( s );
    // TODO SAM 2014-04-05 Remove the ${ts:Property} handling from this method since it is now in TS.formatLegend()
    //Message.printStatus(2, routine, "After formatLegend(), string is \"" + s2 + "\"" );
    // Now replace ${ts:Property} and ${Property} strings with properties from the processor
    // Put the most specific first so it is matched first
    String [] startStrings = { "${ts:", "${" };
    int [] startStringsLength = { 5, 2 };
    String [] endStrings = { "}", "}" };
    boolean isTsProp = false;
    Object propO;
    // Loop through and expand the string, first by expanding the time series properties, which have a more specific
    // ${ts: starting pattern and then the processor properties starting with ${
    for ( int ipat = 0; ipat < startStrings.length; ipat++ ) {
        int start = 0; // Start at the beginning of the string
        int pos2 = 0;
        isTsProp = false;
        if ( ipat == 0 ) {
            // Time series property corresponding to startStrings[0] for loop below.
            // The fundamental logic is the same but getting the property is different whether from TS or processor
            isTsProp = true;
        }
        while ( pos2 < s2.length() ) {
            int pos1 = StringUtil.indexOfIgnoreCase(s2, startStrings[ipat], start );
            if ( pos1 >= 0 ) {
                // Find the end of the property
                pos2 = s2.indexOf( endStrings[ipat], pos1 );
                if ( pos2 > 0 ) {
                    // Get the property...
                    String propname = s2.substring(pos1+startStringsLength[ipat],pos2);
                    //Message.printStatus(2, routine, "Property=\"" + propname + "\" isTSProp=" + isTsProp + " pos1=" + pos1 + " pos2=" + pos2 );
                    // By convention if the property is not found, keep the original string so can troubleshoot property issues
                    String propvalString = s2.substring(pos1,(pos2 + 1));
                    if ( isTsProp ) {
                        // Get the property out of the time series
                        propO = ts.getProperty(propname);
                        if ( propO == null ) {
                            if ( status != null ) {
                                String message = "Time series \"" + ts.getIdentifierString() + "\" property=\"" +
                                propname + "\" has a null value.";
                                Message.printWarning ( 3,routine, message );
                                status.addToLog ( commandPhase,
                                    new CommandLogRecord(CommandStatusType.FAILURE,
                                        message, "Verify that the property is set for the time series." ) );
                            }
                        }
                        else {
                            // This handles conversion of integers to strings
                            propvalString = "" + propO;
                        }
                    }
                    else if ( processor != null ) {
                        // Not a time series property so this is a processor property
                        // Get the property from the processor properties
                        PropList request_params = new PropList ( "" );
                        request_params.set ( "PropertyName", propname );
                        CommandProcessorRequestResultsBean bean = null;
                        boolean processorError = false;
                        try {
                            bean = processor.processRequest( "GetProperty", request_params);
                        }
                        /* TODO SAM 2015-07-05 Need to evaluate whether error should be absorbed and ${property} remain unexpanded, as javadoc'ed
                        catch ( UnrecognizedRequestException e ) {
                        	// Property is not set - OK
                        	processorError = true;
                        }
                        */
                        catch ( Exception e ) {
                        	// Unexpected exception
                            if ( status != null ) {
                                String message = "Error requesting GetProperty(Property=\"" + propname + "\") from processor.";
                                Message.printWarning ( 3,routine, message );
                                status.addToLog ( commandPhase,
                                    new CommandLogRecord(CommandStatusType.FAILURE,
                                        message, "Report the problem to software support." ) );
                            }
                            processorError = true;
                        }
                        if ( !processorError ) {
                            if ( bean == null ) {
                                // Not an exception but the property was not found in the processor
                                if ( status != null ) {
                                    String message =
                                        "Unable to find property from processor using GetProperty(Property=\"" + propname + "\").";
                                    Message.printWarning ( 3,routine, message );
                                    status.addToLog ( commandPhase,
                                        new CommandLogRecord(CommandStatusType.FAILURE,
                                            message, "Verify that the property name is valid - must match case." ) );
                                }
                            }
                            else {
                                // Have a property, but still need to check for null value
                                // TODO SAM 2013-09-09 should this be represented as "null" in output?
                                PropList bean_PropList = bean.getResultsPropList();
                                Object o_PropertyValue = bean_PropList.getContents ( "PropertyValue" );
                                if ( o_PropertyValue == null ) {
                                    if ( status != null ) {
                                        String message =
                                            "Null PropertyValue returned from processor for GetProperty(PropertyName=\"" + propname + "\").";
                                        Message.printWarning ( 3, routine, message );
                                        status.addToLog ( commandPhase,
                                            new CommandLogRecord(CommandStatusType.FAILURE, message,
                                                "Verify that the property name is valid - must match case." ) );
                                    }
                                }
                                else {
                                    // This handles conversion of integers and dates to strings
                                    propvalString = "" + o_PropertyValue;
                                }
                            }
                        }
                    }
                    // Replace the string and continue to evaluate s2
                    s2 = s2.substring ( 0, pos1 ) + propvalString + s2.substring (pos2 + 1);
                    // Next search will be at the end of the expanded string (end delimiter will be skipped in any case)
                    start = pos1 + propvalString.length();
                }
                else {
                    // No closing character so leave the property string as is and march on...
                    start = pos1 + startStringsLength[ipat];
                    if ( start > s2.length() ) {
                        break;
                    }
                }
            }
            else {
                // No more ${} property strings so done processing properties.
                // If checking time series properties will then check global properties in next loop
                break;
            }
        }
    }
    return s2;
}
	

/**
Get the commands above an index position.
@param processor The processor that is managing commands.
@param pos Index (0+) before which to get commands.  The command at the indicated
position is NOT included in the search.
*/
private static List<Command> getCommandsBeforeIndex ( StateDMI_Processor processor, int pos )
{	List<Command> commands = new ArrayList<Command>();
	int size = ( processor.size());
	if ( pos > size ) {
		pos = size;
	}
	for ( int i = 0; i < pos; i++ ) {
		commands.add ( processor.get(i));
	}
	return commands;
}

/**
Determine whether commands should create output by checking the CreateOutput parameter.
This is a processor level property.  If there is a problem, return true (create output).
@param processor the CommandProcessor to use to get data.
@return true if output should be created when processing commands, false if not.
*/
public static boolean getCreateOutput ( CommandProcessor processor )
{	String routine = "TSCommandProcessorUtil.getCreateOutput";
	try {
		Object o = processor.getPropContents ( "CreateOutput" );
		if ( o != null ) {
			return ((Boolean)o).booleanValue();
		}
	}
	catch ( Exception e ) {
		// Not fatal, but of use to developers.
		String message = "Error requesting CreateOutput from processor - will create output.";
		Message.printWarning(3, routine, message );
		Message.printWarning(3, routine, e );
	}
	return true;
}

/**
Get a date/time property from the processor, recognizing normal date/time strings like "YYYY-MM-DD",
processor properties "${Property}", and special strings including:
<ol>
<li>	"OutputPeriod"</li>
</ol>
If an error occurs, the command log messages and status will be updated.
Additionally, if the parameter string is invalid an exception will be thrown
(calling code in command can then increment the command's warning count).
@param dtString date/time string to process.
@param parameterName name for parameter for messages.
@param processor command processor from which to retrieve the date.
@param status command status, to receive logging information.
@param int warningLevel level at which to log information.
@param commandTag string tag for logging.
@exception InvalidCommandParameterException if the parameter is not valid.
*/
public static DateTime getDateTime ( String dtString, String parameterName, CommandProcessor processor,
    CommandStatus status, int warningLevel, String commandTag )
throws InvalidCommandParameterException
{   String routine = "TSCommandProcessorUtil.getDateTime", message;
    DateTime dt = null;
    int logLevel = 3;
    int warningCount = 0; // only has local scope and limited meaning
    if ( (dtString == null) || dtString.equals("") ) {
        return null;
    }
    try {
        PropList request_params = new PropList ( "" );
        request_params.set ( "DateTime", dtString );
        CommandProcessorRequestResultsBean bean = null;
        try {
            bean = processor.processRequest( "DateTime", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting " + parameterName + " DateTime(DateTime=" + dtString + "\") from processor (" + e + ").";
            Message.printWarning(logLevel,
                MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
            throw new InvalidCommandParameterException ( message );
        }

        PropList bean_PropList = bean.getResultsPropList();
        Object prop_contents = bean_PropList.getContents ( "DateTime" );
        if ( prop_contents == null ) {
        	// Have to take special care for built-in properties that are allowed to be null
        	// See the similar handling in TSEngine.getDateTime()
        	// Newer code will focus on ${Property} whereas legacy uses the version without ${}
        	if ( dtString.equalsIgnoreCase("OutputStart") || dtString.equalsIgnoreCase("OutputEnd") ||
        		dtString.equalsIgnoreCase("${OutputStart}") || dtString.equalsIgnoreCase("${OutputEnd}") ||
        		dtString.equalsIgnoreCase("InputStart") || dtString.equalsIgnoreCase("InputEnd") ||
        		dtString.equalsIgnoreCase("${InputStart}") || dtString.equalsIgnoreCase("${InputEnd}") ) {
        		// OK to return null
        		return null;
        	}
        	else {
	            message = "Null value for " + parameterName + " DateTime(DateTime=" + dtString + "\") returned from processor.";
	            Message.printWarning(logLevel,
	                MessageUtil.formatMessageTag( commandTag, ++warningCount),
	                routine, message );
	            status.addToLog ( CommandPhaseType.RUN,
	                new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Specify a valid date/time string, or a recognized internal property such as ${OutputEnd}." ) );
	            throw new InvalidCommandParameterException ( message );
        	}
        }
        else {
            dt = (DateTime)prop_contents;
        }
    }
    catch ( Exception e ) {
        message = parameterName + " \"" + dtString + "\" is invalid.";
        Message.printWarning(warningLevel,
            MessageUtil.formatMessageTag( commandTag, ++warningCount),
            routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid date/time string, or a recognized internal property such as ${OutputEnd}." ) );
        throw new InvalidCommandParameterException ( message );
    }
    return dt;
}

/**
Get a list of TSEnsemble (ensemble) from a list of commands.  These ensemble are suitable for passing to code
run in discovery mode, which itself produces new ensembles or time series with identifiers that are dynamically
determined from the input time series.  Commands that implement ObjectListProvider have their
getObjectList(TS) method called and the returned time series are added to the list.
@param commands time series commands to search.
@param sort Should output time series be sorted by identifier - currently not enabled
@return list of time series or an empty non-null list if nothing found.
*/
protected static List<TSEnsemble> getDiscoveryEnsembleFromCommands ( List<Command> commands, boolean sort )
{   if ( commands == null ) {
        return new Vector();
    }
    List<TSEnsemble> tsEnsembleList = new Vector ();
    for ( Command command: commands ) {
        if ( (command != null) && (command instanceof ObjectListProvider) ) {
        	Object o = ((ObjectListProvider)command).getObjectList ( TSEnsemble.class );
        	List<TSEnsemble> list = null;
        	if ( o != null ) {
        		list = (List<TSEnsemble>)o;
        	}
            if ( list != null ) {
                for ( TSEnsemble tsEnsemble : list ) {
                    if ( tsEnsemble != null ) {
                        tsEnsembleList.add( tsEnsemble );
                    }
                }
            }
        }
    }
    /*
    if ( sort ) {
        TSUtil.sort(tsEnsembleList);
    }
    */
    return tsEnsembleList;
}

/**
Get a list of Prop (properties) from a list of commands.  These properties are suitable for passing to code
run in discovery mode.  Commands that implement ObjectListProvider have their getObjectList(Prop) method
called and the returned time series are added to the list.
@param commands commands to search.
@param sort Should properties be sorted by property name - currently not enabled
@return list of properties an empty non-null list if nothing found.
*/
protected static List<Prop> getDiscoveryPropFromCommands ( List<Command> commands, boolean sort )
{   if ( commands == null ) {
        return new ArrayList<Prop>();
    }
    List<Prop> proplist = new ArrayList<Prop>();
    for ( Command command: commands ) {
        if ( (command != null) && (command instanceof ObjectListProvider) ) {
        	//TODO sam 2017-03-17 figure out how to do generics but for now the old way works
			ObjectListProvider objectListProvider = (ObjectListProvider)command;
        	Object o = objectListProvider.getObjectList ( Prop.class );
            List<Prop> list = null;
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<Prop> list0 = (List<Prop>)o;
            	list = list0;
            }
            if ( list != null ) {
                int listsize = list.size();
                Prop prop;
                for ( int iprop = 0; iprop < listsize; iprop++ ) {
                    prop = list.get(iprop);
                    if ( prop != null ) {
                        proplist.add( prop );
                    }
                }
            }
        }
    }
    if ( sort ) {
    	Collections.sort(proplist);
    }
    return proplist;
}

/**
Return the properties for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of property names to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which properties are needed.
@return a list of time series (containing only information populated during discovery), or an empty list.
*/
public static List<Prop> getDiscoveryPropFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{   String routine = "getDiscoveryPropFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "Position in list is " + pos + " for command:" + command );
    }
    if ( pos < 0 ) {
        // Just return a blank list...
        return new Vector<Prop>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series from the commands (sort on the property names)...
    List<Prop> availableProp = getDiscoveryPropFromCommands ( commands, true );
    return availableProp;
}

/**
Get a list of ensemble identifiers from a list of commands.  See documentation for
fully loaded method.  The output list is not sorted..
@param commands Commands to search.
@return list of table identifiers or an empty non-null list if nothing found.
*/
private static List<String> getEnsembleIdentifiersFromCommands ( List<Command> commands )
{   // Default behavior...
    return getEnsembleIdentifiersFromCommands ( commands, false );
}

/**
Get a list of ensemble identifiers from a list of commands.  The returned strings are suitable for
drop-down lists, etc.  Ensemble identifiers are determined as follows:
Commands that implement ObjectListProvider have their getObjectList(TSEnsemble) method called.
The getEnsembleID() method on the TSEnsemble is then returned.
@param commands Commands to search.
@param sort Should output be sorted by identifier.
@return list of ensemble identifiers or an empty non-null Vector if nothing found.
*/
protected static List<String> getEnsembleIdentifiersFromCommands ( List<Command> commands, boolean sort )
{   if ( commands == null ) {
        return new Vector();
    }
    List<String> v = new Vector ( 10, 10 );
    int size = commands.size();
    boolean in_comment = false;
    Command command = null;
    String command_string = null;
    for ( int i = 0; i < size; i++ ) {
        command = commands.get(i);
        command_string = command.toString();
        if ( command_string.startsWith("/*") ) {
            in_comment = true;
            continue;
        }
        else if ( command_string.startsWith("*/") ) {
            in_comment = false;
            continue;
        }
        if ( in_comment ) {
            continue;
        }
        if ( command instanceof ObjectListProvider ) {
        	Object o = ((ObjectListProvider)command).getObjectList ( new TSEnsemble().getClass() );
            List<TSEnsemble> list = null;
            if ( o != null ) { 
            	list = (List<TSEnsemble>)o;
            }
            String id;
            if ( list != null ) {
                int listsize = list.size();
                TSEnsemble tsensemble;
                for ( int its = 0; its < listsize; its++ ) {
                    tsensemble = list.get(its);
                    id = tsensemble.getEnsembleID();
                    if ( (id != null) && !id.equals("") ) {
                        v.add( id );
                    }
                }
            }
        }
    }
    return v;
}

/**
Return the ensemble identifiers for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a list of String containing the ensemble identifiers, or an empty Vector.
*/
public static List<String> getEnsembleIdentifiersFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{   String routine = "TSCommandProcessorUtil.getEnsembleIdentifiersFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "Position in list is " + pos + " for command:" + command );
    }
    if ( pos < 0 ) {
        // Just return a blank list...
        return new ArrayList<String>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series identifiers from the commands...
    return getEnsembleIdentifiersFromCommands ( commands );
}

/**
Get a list of pattern time series from a list of commands.  The time series can be used to
extract identifiers for drop-down lists, etc.
Time series are determined as follows:
<ol>
<li>    Commands that implement ObjectListProvider have their getObjectList(TS) method called.
        The time series identifiers from the time series list are examined and those with alias
        will have the alias returned.  Otherwise, the full time series identifier is returned with or
        with input path as requested.</li>
</ol>
@param commands Commands to search.
@param List of pattern time series provided by commands.
*/
protected static List<TS> getPatternTSListFromCommands ( List commands )
{   if ( commands == null ) {
        return new Vector();
    }
    List<TS> v = new Vector ( 10, 10 );
    int size = commands.size();
    Object command_o = null;    // Command as object
    for ( int i = 0; i < size; i++ ) {
        command_o = commands.get(i);
        if ( (command_o != null) && (command_o instanceof ObjectListProvider) ) {
            // Try to get the list of identifiers using the interface method.
            // TODO SAM 2007-12-07 Evaluate the automatic use of the alias.
        	Object o = ((ObjectListProvider)command_o).getObjectList ( new TS().getClass() );
            List<TS> list = null;
            if ( o != null ) {
            	list = (List<TS>)o;
            }
            if ( list != null ) {
                int tssize = list.size();
                TS ts;
                for ( int its = 0; its < tssize; its++ ) {
                    ts = list.get(its);
                    v.add( ts );
                }
            }
        }
    }
    // Sort the time series by identifier...
    TSUtil_SortTimeSeries tsu = new TSUtil_SortTimeSeries(v, null, null, null, 1 );
    try {
        return tsu.sortTimeSeries();
    }
    catch ( Exception e ) {
        // Return original order below
    }
    return v;
}

/**
Return the regression test disabled count.
@return the regression test disabled count.
*/
private static int getRegressionTestDisabledCount ()
{
    return __regressionTestDisabledCount;
}

/**
Return the regression test fail count.
@return the regression test fail count.
*/
private static int getRegressionTestFailCount ()
{
    return __regressionTestFailCount;
}

/**
Return the regression test pass count.
@return the regression test pass count.
*/
private static int getRegressionTestPassCount ()
{
    return __regressionTestPassCount;
}

/**
Get the total run time for the commands.  This is used, for example, by the RunCommands() command.
@param commands list of commands to determine total run time.
@return total run time for all commands, in milliseconds.
*/
public static long getRunTimeTotal ( List<Command> commands )
{
    long runTimeTotal = 0;
    if ( commands == null ) {
        return runTimeTotal;
    }
    for ( Command command : commands ) {
        runTimeTotal += command.getCommandProfile(CommandPhaseType.RUN).getRunTime();
    }
    return runTimeTotal;
}

/**
Get a list of table identifiers from a list of commands.  See documentation for fully loaded method.
@param commands Time series commands to search.
@return list of table identifiers or an empty non-null Vector if nothing found.
*/
private static List<String> getTableIdentifiersFromCommands ( List<Command> commands )
{   // Default behavior...
    return getTableIdentifiersFromCommands ( commands, false );
}

/**
Get a list of table identifiers from a list of commands.  The returned strings are suitable for
drop-down lists, etc.  Table identifiers are determined as follows:
Commands that implement ObjectListProvider have their getObjectList(DataTable) method called.
The getTableID() method on the DataTable is then returned.
@param commands Commands to search.
@param sort Should output be sorted by identifier.
@return list of table identifiers or an empty non-null list if nothing found.
*/
protected static List<String> getTableIdentifiersFromCommands ( List<Command> commands, boolean sort )
{   if ( commands == null ) {
        return new ArrayList<String>();
    }
    List<String> tableIDList = new ArrayList<String> ( 10 );
    int size = commands.size();
    boolean in_comment = false;
    Command command = null;
    String commandString = null;
    String commandName = null;
    for ( int i = 0; i < size; i++ ) {
        command = commands.get(i);
        commandString = command.toString();
        commandName = command.getCommandName();
        if ( commandString.startsWith("/*") ) {
            in_comment = true;
            continue;
        }
        else if ( commandString.startsWith("*/") ) {
            in_comment = false;
            continue;
        }
        if ( in_comment ) {
            continue;
        }
        // Commands that provide a list of tables (so add to the list)
        if ( command instanceof ObjectListProvider ) {
        	Object o = ((ObjectListProvider)command).getObjectList ( new DataTable().getClass() );
            List<DataTable> list = null;
            if ( o != null ) {
            	list = (List<DataTable>)o;
            }
            String id;
            if ( list != null ) {
                int tablesize = list.size();
                DataTable table;
                for ( int its = 0; its < tablesize; its++ ) {
                    table = list.get(its);
                    id = table.getTableID();
                    if ( (id != null) && !id.isEmpty() ) {
                    	// Don't add if already in the list
                    	boolean found = false;
                    	for ( String tableID : tableIDList ) {
                    		if ( id.equalsIgnoreCase(tableID) ) {
                    			found = true;
                    			break;
                    		}
                    	}
                    	if ( !found ) {
                    		tableIDList.add( id );
                    	}
                    }
                }
            }
        }
        else if ( commandName.equalsIgnoreCase("FreeTable") ) {
            // Need to remove matching table identifiers that are in the list
            // (otherwise editing commands will show extra tables as of that point in the workflow, which will
            // be confusing and may lead to errors, e.g., if consistent units are expected but the units are
            // not consistent).
            // First get the matching tables for the FreeTable() command parameters
            PropList parameters = command.getCommandParameters();
            String TableID = parameters.getValue("TableID");
            for ( int iTable = 0; iTable < tableIDList.size(); iTable++ ) {
                if ( tableIDList.get(iTable).equalsIgnoreCase(TableID) ) {
                    //Message.printStatus(2,"", "Removing table " + TableID );
                    tableIDList.remove(iTable--);
                }
            }
        }
    }
    if ( sort ) {
        java.util.Collections.sort(tableIDList);
    }
    return tableIDList;
}

/**
Return the table identifiers for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a list of String containing the table identifiers, or an empty list.
*/
public static List<String> getTableIdentifiersFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{   String routine = "TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "Position in list is " + pos + " for command:" + command );
    }
    if ( pos < 0 ) {
        // Just return a blank list...
        return new ArrayList<String>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series identifiers from the commands...
    return getTableIdentifiersFromCommands ( commands );
}

/**
Get a list of time series identifiers from a list of commands.  See documentation for
fully loaded method.  The output list is not sorted and does NOT contain the input type or name.
@param commands Time series commands to search.
@return list of time series identifiers or an empty non-null list if nothing found.
*/
private static List<String> getTSIdentifiersFromCommands ( List<Command> commands )
{	// Default behavior...
	return getTSIdentifiersFromCommands ( commands, false, false );
}

/**
Get a list of identifiers from a list of commands.  See documentation for
fully loaded method.  The output list does NOT contain the input type or name.
@param commands Time series commands to search.
@param sort Should output be sorted by identifier.
@return list of time series identifiers or an empty non-null list if nothing found.
*/
protected static List<String> getTSIdentifiersFromCommands ( List<Command> commands, boolean sort )
{	// Return the identifiers without the input type and name.
	return getTSIdentifiersFromCommands ( commands, false, sort );
}

/**
Get a list of time series identifiers from a list of commands.
These strings are suitable for drop-down lists, etc.
Time series identifiers are determined as follows:
<ol>
<li>    Commands that implement ObjectListProvider have their getObjectList(TS) method called.
        The time series identifiers from the time series list are examined and those with alias
        will have the alias returned.  Otherwise, the full time series identifier is returned with or
        with input path as requested.</li>
<li>    Command strings that start with "TS ? = " have the alias (?) returned.</li>
<li>    Lines that are time series identifiers are returned, including the full path as requested.</li>
</ol>
@param commands commands to search, in order of first command to process to last.
@param include_input If true, include the input type and name in the returned
values.  If false, only include the 5-part TSID information.  If an alias is returned, it is not
impacted by this parameter.
@param sort Should output be sorted by identifier (currently ignored)
@return list of time series identifiers or an empty non-null list if nothing found.
*/
protected static List<String> getTSIdentifiersFromCommands ( List<Command> commands, boolean include_input, boolean sort )
{	if ( commands == null ) {
		return new ArrayList<String>();
	}
	List<String> tsidsFromCommands = new ArrayList<String>(); // The String TSID or alias
	List<TS> tsFromCommands = new ArrayList<TS>(); // The ts for available TS, used to store each TSIdent
	int size = commands.size();
	String commandString = null;
	List<String> tokens = null;
	boolean in_comment = false;
	Object command_o = null; // Command as object
	String commandName; // Command name
	Command command; // Command as Command instance
	for ( int i = 0; i < size; i++ ) {
		command_o = commands.get(i);
		command = null;
		commandName = ""; // Only care about instances of Free() commands below
		if ( command_o instanceof Command ) {
			commandString = command_o.toString().trim();
			command = (Command)command_o;
			commandName = command.getCommandName();
		}
		// TODO SAM 2017-03-17 seems like the following can be removed because
		// code update to generics does not indicate any use of string version
		else if ( command_o instanceof String ) {
			commandString = ((String)command_o).trim();
		}
		if ( (commandString == null) || commandString.startsWith("#") || (commandString.length() == 0) ) {
			// Make sure comments are ignored...
			continue;
		}
		if ( commandString.startsWith("/*") ) {
			in_comment = true;
			continue;
		}
		else if ( commandString.startsWith("*/") ) {
			in_comment = false;
			continue;
		}
		if ( in_comment ) {
			continue;
		}
        if ( (command_o != null) && (command_o instanceof ObjectListProvider) ) {
            // Try to get the list of identifiers using the interface method.
            // TODO SAM 2007-12-07 Evaluate the automatic use of the alias (takes priority over TSID) - probably good.
        	Object o = ((ObjectListProvider)command_o).getObjectList ( new TS().getClass() );
            List<TS> list = null;
            if ( o != null ) {
            	@SuppressWarnings("unchecked")
				List<TS> list0 = (List<TS>)o;
            	list = list0;
            }
            if ( list != null ) {
                int tssize = list.size();
                TS ts;
                for ( int its = 0; its < tssize; its++ ) {
                    ts = list.get(its);
                    if ( ts == null ) {
                    	// This should not happen and is symptomatic of a command not fully handling a
                    	// time series in discovery mode, more of an issue with ${property} use in parameters.
                    	// Log a message so the issue can be tracked down
                    	String routine = "TSCommandProcessorUtil.getTSIdentifiersFromCommands";
                    	Message.printWarning(3, routine, "Null time series in discovery mode - need to check code for command to improve handling: " + commandString);
                    	continue;
                    }
                    if ( !ts.getAlias().equals("") ) {
                        // Use the alias if it is available.
                        tsidsFromCommands.add( ts.getAlias() );
                    }
                    else {
                        // Use the identifier.
                        tsidsFromCommands.add ( ts.getIdentifier().toString(include_input) );
                    }
                    tsFromCommands.add( ts );
                }
            }
        }
		else if ( StringUtil.startsWithIgnoreCase(commandString,"TS ") ) {
		    // TODO SAM 2011-04-04 Remove this code after some period - TSTool version 10.00.00 removed the
		    // TS Alias syntax, which should be migrated as commands are parsed.
			// Use the alias...
			tokens = StringUtil.breakStringList( commandString.substring(3)," =", StringUtil.DELIM_SKIP_BLANKS);
			if ( (tokens != null) && (tokens.size() > 0) ) {
			    String alias = tokens.get(0);
				tsidsFromCommands.add ( alias );
				//+ " (alias)" );
				// Treat as an alias
				TS ts = new TS();
				TSIdent tsident = new TSIdent();
				tsident.setAlias(alias);
				try {
				    ts.setIdentifier(tsident);
				}
				catch ( Exception e ) {
				    // This code should be phased out so don't worry about this issue
				}
                tsFromCommands.add( ts );
			}
		}
		else if ( isTSID(commandString) ) {
			// Reasonably sure it is an identifier.  Only add the
			// 5-part TSID and not the trailing input type and name.
			int pos = commandString.indexOf("~");
			String tsid;
			if ( (pos < 0) || include_input ) {
				// Add the whole thing...
				tsid = commandString;
			}
			else {
			    // Add the part before the input fields...
				tsid = commandString.substring(0,pos);
			}
			tsidsFromCommands.add ( tsid );
			// For the purpose of handling TSIdents for Free(), treat as an alias
            TS ts = new TS();
			TSIdent tsident = null;
			try {
			    tsident = new TSIdent(tsid);
			}
			catch ( Exception e ) {
			    // Should not happen because isTSID() called at start of code block
			}
			// Also set the identifier as the alias...
            tsident.setAlias(tsid);
            try {
                ts.setIdentifier(tsident);
            }
            catch ( Exception e ) {
                // Should not happen because isTSID() called at start of code block
            }
            tsFromCommands.add( ts );
		}
		else if ( commandName.equalsIgnoreCase("Free") ) {
		    // Need to remove matching time series identifiers that are in the list
		    // (otherwise editing commands will show extra time series as of that point in the workflow, which will
		    // be confusing and may lead to errors, e.g., if consistent units are expected but the units are
		    // not consistent).
		    // First get the matching time series for the Free() command parameters
		    Command commandInst = (Command)command_o;
		    PropList parameters = commandInst.getCommandParameters();
		    // TODO SAM 2011-04-04 Need to get ensembles above command
		    List<TSEnsemble> ensemblesFromCommands = new ArrayList<TSEnsemble>();
		    TimeSeriesToProcess tsToProcess = getTSMatchingTSListParameters(tsFromCommands, ensemblesFromCommands,
	            parameters.getValue("TSList"), parameters.getValue("TSID"),
	            parameters.getValue("TSPosition"), parameters.getValue("EnsembleID") );
		    // Loop through the list of matching time series and remove identifiers at the matching positions
		    // (the time series list and identifier lists should match in position).
		    int [] pos = tsToProcess.getTimeSeriesPositions();
		    //Message.printStatus(2,"", "Detected Free() command, have " + pos.length + " time series to check." ); 
		    // Loop backwards so that position values don't need to be adjusted.
		    for ( int ipos = pos.length - 1; ipos >= 0; ipos--  ) {
	            //Message.printStatus(2,"", "Removing time series " + pos[ipos] + ": " + tsidsFromCommands.get(pos[ipos]));
		        tsFromCommands.remove(pos[ipos]);
		        tsidsFromCommands.remove(pos[ipos]);
		    }
		}
	}
	return tsidsFromCommands;
}

/**
Return the time series identifiers for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a list of String containing the time series identifiers, or an empty list.
*/
public static List<String> getTSIdentifiersNoInputFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{	String routine = "TSCommandProcessorUtil.getTSIdentifiersNoInputFromCommandsBeforeCommand";
	// Get the position of the command in the list...
	int pos = processor.indexOf(command);
	if ( Message.isDebugOn ) {
	    Message.printDebug ( 1, routine, "Position in list is " + pos + " for command:" + command );
	}
	if ( pos < 0 ) {
		// Just return a blank list...
		return new ArrayList<String>();
	}
    // Find the commands above the position...
	List<Command> commands = getCommandsBeforeIndex ( processor, pos );
	// Get the time series identifiers from the commands...
	return getTSIdentifiersFromCommands ( commands );
}

/**
Get time series that match the TSList and related input.  This method is used to evaluate the list of
time series from the time series processor, and the list of discovery time series extracted from commands
(that maintain their own discovery information).
@param tsCandidateList list of time series to check for matching time series identifier
@param ensembleCandidateList list of ensembles to check for matching ensemble identifier
@param TSList string value for TSList (should match TSListType enumeration) - if null or a blank string,
TSListType.ALL_TS will be used by default
@param TSPosition string value of TSPosition range notation
@param EnsembleID ensemble identifier to match
*/
public static TimeSeriesToProcess getTSMatchingTSListParameters ( List<TS> tsCandidateList,
    List<TSEnsemble> ensembleCandidateList, String TSList, String TSID, String TSPosition, String EnsembleID )
{   String routine = "TSCommandProcessorUtil.getTimeSeriesToProcess";
    if ( (TSList == null) || TSList.equals("") ) {
        // Default is to match all
        TSList = "" + TSListType.ALL_TS;
    }
    List<TS> tslist = new ArrayList<TS>(); // List of time series to process
    List<String> errorList = new ArrayList<String>(); // List of error messages finding time series
    if ( (tsCandidateList == null) || (tsCandidateList.size() == 0) ) {
        // Return an empty list
        return new TimeSeriesToProcess(tslist, new int[0], errorList);
    }
    int nts = tsCandidateList.size();
    // Positions of time series to process.
    // Size to match the full list but may not be filled initially - trim before returning.
    int [] tspos = new int[nts];
    // Loop through the time series in memory...
    int count = 0;
    TS ts = null;
    if ( Message.isDebugOn ) {
        Message.printDebug( 1, routine, "Getting list of time series to process using TSList=\"" + TSList +
            "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\", TSPosition=\"" + TSPosition + "\"" );
    }
    if ( TSList.equalsIgnoreCase(TSListType.FIRST_MATCHING_TSID.toString()) ) {
        // Search forward for the first single matching time series...
        for ( int its = 0; its < nts; its++ ) {
            try {
                ts = tsCandidateList.get ( its );
                if ( ts == null ) {
                    continue;
                }
            }
            catch ( Exception e ) {
                // Don't add...
                continue;
            }
            if ( TSID.indexOf("~") > 0 ) {
                // Include the input type...
                if (ts.getIdentifier().matches(TSID,true,true)){
                    tslist.add ( ts );
                    tspos[count++] = its;
                    // Only return the single index...
                    int [] tspos2 = new int[1];
                    tspos2[0] = tspos[0];
                    // Only want one match...
                    return new TimeSeriesToProcess(tslist, tspos2, errorList);
                }
            }
            else {
                // Just check the main information...
                if(ts.getIdentifier().matches(TSID,true,false)){
                    tslist.add ( ts );
                    tspos[count++] = its;
                    // Only return the single index...
                    int [] tspos2 = new int[1];
                    tspos2[0] = tspos[0];
                    // Only want one match...
                    return new TimeSeriesToProcess(tslist, tspos2, errorList);
                }
            }
        }
        // Return empty list since no match
        errorList.add("Unable to find first matched TSID \"" + TSID + "\"" );
        return new TimeSeriesToProcess(tslist, new int[0], errorList);
    }
    else if ( TSList.equalsIgnoreCase(TSListType.LAST_MATCHING_TSID.toString()) ) {
        // Search backwards for the last single matching time series...
        for ( int its = (nts - 1); its >= 0; its-- ) {
            try {
                ts = tsCandidateList.get ( its );
            }
            catch ( Exception e ) {
                // Don't add...
                continue;
            }
            if ( TSID.indexOf("~") > 0 ) {
                // Include the input type...
                if (ts.getIdentifier().matches(TSID,true,true)){
                    tslist.add ( ts );
                    tspos[count++] = its;
                    // Only return the single index...
                    int [] tspos2 = new int[1];
                    tspos2[0] = tspos[0];
                    // Only want one match...
                    return new TimeSeriesToProcess(tslist, tspos2, errorList);
                }
            }
            else {
                // Just check the main information...
                if(ts.getIdentifier().matches(TSID,true,false)){
                    tslist.add ( ts );
                    tspos[count++] = its;
                    // Only return the single index...
                    int [] tspos2 = new int[1];
                    tspos2[0] = tspos[0];
                    // Only want one match...
                    return new TimeSeriesToProcess(tslist, tspos2, errorList);
                }
            }
        }
        // Return empty list since no match
        errorList.add("Unable to find last matched TSID \"" + TSID + "\"" );
        return new TimeSeriesToProcess(tslist, new int[0], errorList);
    }
    else if ( TSList.equalsIgnoreCase(TSListType.ENSEMBLE_ID.toString()) ) {
        // Return a list of all time series in an ensemble
        // FIXME SAM 2009-10-10 Need to fix issue of index positions being found in ensemble but
        // then not matching the main TS list (in case where time series were copied to ensemble).
        // For now, do not allow time series to be copied to ensemble (e.g., in NewEnsemble() command).
        TSEnsemble ensemble = null;
        if ( ensembleCandidateList != null ) {
            for ( TSEnsemble tsensemble : ensembleCandidateList ) {
                if ( tsensemble.getEnsembleID().equalsIgnoreCase(EnsembleID) ) {
                    ensemble = tsensemble;
                    break;
                }
            }
        }
        if ( ensemble == null ) {
            errorList.add ("Unable to find ensemble \"" + EnsembleID + "\" to get time series.");
            return new TimeSeriesToProcess(tslist, new int[0], errorList);
        }
        else {
            int esize = ensemble.size();
            for ( int ie = 0; ie < esize; ie++ ) {
                // Set the time series instance (always what is included in the ensemble)...
                ts = ensemble.get (ie);
                tslist.add ( ts );
                // Figure out the index in the processor time series list by comparing the instance...
                TS ts2; // Time series in main list to compare against.
                boolean found = false;
                // Loop through the main list...
                for ( int its = 0; its < nts; its++ ) {
                    try {
                        ts2 = tsCandidateList.get ( its );
                    }
                    catch ( Exception e ) {
                        continue;
                    }
                    if ( ts == ts2 ) {
                        found = true;
                        tspos[count++] = its;
                        break;
                    }
                }
                if ( !found ) {
                    // This will happen when time series are copied to ensembles in order to protect the
                    // data from further modification.  Time series will then always need to be accessed via
                    // the ensemble.
                    Message.printStatus( 3, routine, "Unable to find ensemble \"" + EnsembleID +
                        "\" time series \"" + ts.getIdentifier() + "\" - setting index to -1.");
                    tspos[count++] = -1; // TODO SAM 2009-10-08 - will this impact other code?
                }
            }
        }
        // Trim down the "tspos" array to only include matches so that other
        // code does not mistakenly iterate through a longer array...
        int [] tspos2 = new int[count];
        for ( int i = 0; i < count; i++ ) {
            tspos2[i] = tspos[i];
        }
        return new TimeSeriesToProcess(tslist, tspos2, errorList);
    }
    else if ( TSList.equalsIgnoreCase(TSListType.SPECIFIED_TSID.toString()) ) {
        // Return a list of time series that match the provided identifiers.
        List<String> tsid_Vector = StringUtil.breakStringList ( TSID, ",", StringUtil.DELIM_SKIP_BLANKS );
        int size_tsid = 0;
        if ( tsid_Vector != null ) {
            size_tsid = tsid_Vector.size();
        }
        for ( int itsid = 0; itsid < size_tsid; itsid++ ) {
            String tsid = tsid_Vector.get(itsid);
            Message.printStatus( 2, routine, "Trying to match \"" + tsid + "\"" );
            // Loop through the available time series and see if any match..
            boolean found = false;
            for ( int its = 0; its < nts; its++ ) {
                try {
                    ts = tsCandidateList.get ( its );
                }
                catch ( Exception e ) {
                    // Don't add...
                    continue;
                }
                // Compare the requested TSID with that in the time series list...
                if ( tsid.indexOf("~") > 0 ) {
                    // Include the input type...
                    if (ts.getIdentifier().matches(tsid,true,true)){
                        //Message.printStatus( 2, routine,
                        //        "Matched using input with TSID=\"" + ts.getIdentifier() + "\"" +
                        //        " Alias=\"" + ts.getAlias() + "\"");
                        found = true;
                    }
                }
                else {
                    // Just check the main information...
                    if(ts.getIdentifier().matches(tsid,true,false)){
                        //Message.printStatus( 2, routine,
                        //        "Matched not using input with TSID=\"" + ts.getIdentifier() + "\"" +
                        //        " Alias=\"" + ts.getAlias() + "\"");
                        found = true;
                    }
                }
                if ( found ) {
                    // Add the time series and increment the count...
                    tslist.add ( ts );
                    tspos[count++] = its;
                    // Found the specific time series so break out of the list.
                    // FIXME SAM 2008-02-05 What if user has the same ID more than once?
                    break;
                }
            }
            if ( !found ) {
                // Did not find a specific time series, which is a problem
                errorList.add ( "Did not match requested (specified) time series \"" + tsid + "\"" );
            }
        }
        // Trim down the "tspos" array to only include matches so that other
        // code does not mistakenly iterate through a longer array...
        Message.printStatus( 2, routine, "Matched " + count + " time series." );
        int [] tspos2 = new int[count];
        for ( int i = 0; i < count; i++ ) {
            tspos2[i] = tspos[i];
        }
        return new TimeSeriesToProcess(tslist, tspos2, errorList);
    }
    else if ( TSList.equalsIgnoreCase(TSListType.TSPOSITION.toString()) ) {
        // Process the position string
        List<String> tokens = StringUtil.breakStringList ( TSPosition,",", StringUtil.DELIM_SKIP_BLANKS );
        int npos = 0;
        if ( tokens != null ) {
            npos = tokens.size();
        }
        int tsposStart, tsposEnd;
        for ( int i = 0; i < npos; i++ ) {
            String token = tokens.get(i);
            if ( token.indexOf("-") >= 0 ) {
                // Range...
                String posString = StringUtil.getToken(token, "-",0,0).trim();
                tsposStart = Integer.parseInt( posString ) - 1;
                posString = StringUtil.getToken(token, "-",0,1).trim();
                tsposEnd = Integer.parseInt( posString ) - 1;
            }
            else {
                // Single value.  Treat as a range of 1.
                tsposStart = Integer.parseInt(token) - 1;
                tsposEnd = tsposStart;
            }
            for ( int itspos = tsposStart; itspos <= tsposEnd; itspos++ ) {
                try {
                    tslist.add ( tsCandidateList.get(itspos) );
                }
                catch ( Exception e ) {
                    // Don't add
                    // FIXME SAM 2008-07-07 Evaluate whether exception needs to be thrown
                    // for out of range index.
                }
                tspos[count++] = itspos;
            }
        }
        // Trim down the "tspos" array to only include matches so that other
        // code does not mistakenly iterate through a longer array...
        int [] tspos2 = new int[count];
        for ( int i = 0; i < count; i++ ) {
            tspos2[i] = tspos[i];
        }
        return new TimeSeriesToProcess(tslist, tspos2, errorList);
    }
    else {
        // Else loop through all the time series from first to last and find matches.  This is for:
        // TSList = AllTS
        // TSList = SELECTED_TS
        // TSList = ALL_MATCHING_TSID
        boolean found = false;
        for ( int its = 0; its < nts; its++ ) {
            found = false;
            try {
                ts = tsCandidateList.get ( its );
            }
            catch ( Exception e ) {
                // Don't add...
                continue;
            }
            if ( TSList.equalsIgnoreCase(TSListType.ALL_TS.toString()) ) {
                found = true;
            }
            else if( TSList.equalsIgnoreCase(TSListType.SELECTED_TS.toString()) && ts.isSelected() ) {
                found = true;
            }
            else if ( TSList.equalsIgnoreCase(TSListType.ALL_MATCHING_TSID.toString()) ) {
                if ( TSID.indexOf("~") > 0 ) {
                    // Include the input type...
                    if (ts.getIdentifier().matches(TSID,true,true)){
                        found = true;
                    }
                }
                else {
                    // Just check the main information...
                    if(ts.getIdentifier().matches(TSID,true,false)){
                        found = true;
                    }
                }
            }
            if ( found ) {
                // Add the time series and increment the count...
                tslist.add ( ts );
                tspos[count++] = its;
            }
        }
        // Trim down the "tspos" array to only include matches so that other
        // code does not mistakenly iterate through a longer array...
        int [] tspos2 = new int[count];
        for ( int i = 0; i < count; i++ ) {
            tspos2[i] = tspos[i];
        }
        //Message.printStatus( 2, routine, tslist.toString() );
        return new TimeSeriesToProcess(tslist, tspos2, errorList);
    }
}

/**
Get the current working directory for the processor.
@param processor the CommandProcessor to use to get data.
@return The working directory in effect for a command.
*/
public static String getWorkingDir ( CommandProcessor processor )
{	String routine = "TSCommandProcessorUtil.getWorkingDir";
	try {
	    Object o = processor.getPropContents ( "WorkingDir" );
		if ( o != null ) {
			return (String)o;
		}
	}
	catch ( Exception e ) {
		// Not fatal, but of use to developers.
		String message = "Error requesting WorkingDir from processor.";
		Message.printWarning(3, routine, message );
	}
	return null;
}

/**
Get the working directory for a command (e.g., for editing).
@param processor the CommandProcessor to use to get data.
@param command Command for which to get the working directory.
@return The working directory in effect for a command.
*/
public static String getWorkingDirForCommand ( CommandProcessor processor, Command command )
{	String routine = "TSCommandProcessorUtil.commandProcessor_GetWorkingDirForCommand";
	PropList request_params = new PropList ( "" );
	request_params.setUsingObject ( "Command", command );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		processor.processRequest( "GetWorkingDirForCommand", request_params );
		return bean.getResultsPropList().getValue("WorkingDir");
	}
	catch ( Exception e ) {
		String message = "Error requesting GetWorkingDirForCommand(Command=\"" + command +
		"\" from processor).";
		Message.printWarning(3, routine, e);
		Message.printWarning(3, routine, message );
	}
	return null;
}

/**
Determine the index of a command in the processor.  A reference comparison occurs.
@param command A command to search for in the processor.
@param startIndex the starting index for processing.
@return the index (0+) of the matching command, or -1 if not found.
*/
public static int indexOf ( CommandProcessor processor, Command command, int startIndex )
{   List<Command> commands = processor.getCommands();
    int size = commands.size();
    Command c;
    for ( int i = startIndex; i < size; i++ ) {
        c = commands.get(i);
        if ( c == command ) {
            return i;
        }
    }
    return -1;
}

/**
Evaluate whether a command appears to be a pure time series identifier (not a
command that uses a time series identifier).  The string is checked to see if:
<ol>
<li>    it has at least three "."</li>
<li>    parentheses are allowed in any part due to various data sources requirements</li>
</ol>
Multi-line / * * / comment strings should not be passed to this method because it will not
know if the command is in a comment block.
@param command Command to evaluate.
@return true if the command appears to be a pure TSID, false if not.
*/
protected static boolean isTSID ( String command )
{	String commandTrimmed = command.trim();
    if ( commandTrimmed.startsWith( "TS " ) ) {
	    // TS Alias command
	    return false;
	}
    if ( commandTrimmed.startsWith( "#" ) || commandTrimmed.startsWith("/*") ||
        commandTrimmed.endsWith("*/") ) {
        // Comment
        return false;
    }
    // TODO SAM 2014-06-20 Will need to handle escaped periods at some point
	if ( StringUtil.patternCount(command,".") < 3 ) {
	    // Not enough periods
	    return false;
	}
	if ( commandTrimmed.endsWith(")") ) {
	    // This cuts out normal commands - TSIDs likely would not have ) at end in the scenario
	    return false;
	}
	return true;
}

/**
Kill any processes associated with the list of commands.  Any commands that implements the
ProcessRunner interface are checked.
@param commandList the list of commands to check.
*/
public static void killCommandProcesses ( List<Command>commandList )
{   String routine = "TSCommandProcessorUtil.killCommandProcesses";
    int size = 0;
    if ( commandList != null ) {
        // Use all commands...
        size = commandList.size();
    }
    Command command;
    for ( int i = 0; i < size; i++ ) {
        command = commandList.get(i);
        if ( command instanceof ProcessRunner ) {
            ProcessRunner pr = (ProcessRunner)command;
            List<Process> processList = pr.getProcessList();
            int processListSize = processList.size();
            for ( int iprocess = 0; iprocess < processListSize; iprocess++ ) {
                Process process = processList.get(iprocess);
                Message.printStatus ( 2, routine, "Destroying process for command: " + command.toString() );
                process.destroy();
            }
        }
    }
}

/**
Open a new regression test report file.
@param outputFile Full path to report file to open.
@param table data table to receive report results, or null if no table will be used.
@param append indicates whether the file should be opened in append mode.
*/
public static void openNewRegressionTestReportFile ( String outputFile, DataTable table, boolean append )
throws FileNotFoundException
{   // Initialize the report counts.
    __regressionTestLineCount = 0;
    __regressionTestFailCount = 0;
    __regressionTestPassCount = 0;
    // Save the table to be used for the regression summary
    __regressionTestTable = table;
    // Print the report headers.
    __regression_test_fp = new PrintWriter ( new FileOutputStream ( outputFile, append ) );
    IOUtil.printCreatorHeader ( __regression_test_fp, "#", 80, 0 );
    __regression_test_fp.println ( "#" );
    __regression_test_fp.println ( "# Command file regression test report from StartRegressionTestResultsReport() and RunCommands()" );
    __regression_test_fp.println ( "#" );
    __regression_test_fp.println ( "# Explanation of columns:" );
    __regression_test_fp.println ( "#" );
    __regression_test_fp.println ( "# Num: count of the tests" );
    __regression_test_fp.println ( "# Enabled: TRUE if test enabled or FALSE if \"#@enabled false\" in command file" );
    __regression_test_fp.println ( "# Run Time: run time in milliseconds" );
    __regression_test_fp.println ( "# Test Pass/Fail:" );
    __regression_test_fp.println ( "#    The test status below may be PASS or FAIL (or blank if disabled)." );
    __regression_test_fp.println ( "#    A test will pass if the command file actual status matches the expected status." );
    __regression_test_fp.println ( "#    Disabled tests are not run and do not count as PASS or FAIL." );
    __regression_test_fp.println ( "#    Search for *FAIL* to find failed tests." );
    __regression_test_fp.println ( "# Commands Expected Status:" );
    __regression_test_fp.println ( "#    Default is assumed to be SUCCESS." );
    __regression_test_fp.println ( "#    \"#@expectedStatus Warning|Failure\" comment in command file overrides default." );
    __regression_test_fp.println ( "# Commands Actual Status:" );
    __regression_test_fp.println ( "#    The most severe status (Success|Warning|Failure) for each command file." );
    __regression_test_fp.println ( "#" );
    __regression_test_fp.println ( "#    |       |Test  |Commands  |Commands   |" );
    __regression_test_fp.println ( "#    |       |Pass/ |Expected  |Actual     |" );
    __regression_test_fp.println ( "# Num|Enabled|Fail  |Status    |Status     |Command File" );
    __regression_test_fp.println ( "#----+-------+------+----------+-----------+------------------" +
    		"---------------------------------------------------------------------------" );
}

/**
Process a time series after reading.  This calls the command processor readTimeSeries2() method.
Command status messages will be added if problems arise but exceptions are not thrown.
*/
public static int processTimeSeriesAfterRead( CommandProcessor processor, Command command, TS ts )
{
    List<TS> tslist = new Vector();
    tslist.add ( ts );
    return processTimeSeriesListAfterRead ( processor, command, tslist );
}

/**
Process a list of time series after reading.  This calls the command processor readTimeSeries2() method.
Command status messages will be added if problems arise but exceptions are not thrown.
*/
public static int processTimeSeriesListAfterRead( CommandProcessor processor, Command command, List<TS> tslist )
{   int log_level = 3;
    int warning_count = 0;
    String routine = "TSCommandProcessorUtil.processTimeSeriesListAfterRead";
    PropList request_params = new PropList ( "" );
    request_params.setUsingObject ( "TSList", tslist );
    CommandStatus status = null;
    if ( command instanceof CommandStatusProvider ) {
        status = ((CommandStatusProvider)command).getCommandStatus();
    }
    try {
        processor.processRequest( "ReadTimeSeries2", request_params);
    }
    catch ( Exception e ) {
        String message = "Error post-processing time series after read using ReadTimeSeries2 processor request.";
        Message.printWarning(log_level, routine, e);
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
        ++warning_count;
    }
    return warning_count;
}

/**
Validate command parameter names and generate standard feedback.  A list of allowed parameter
names is provided.  If a name is not recognized, it is removed so as to prevent the user from continuing.
@param valid_Vector List of valid parameter names (others will be flagged as invalid).
@param command The command being checked.
@param warning A warning String that is receiving warning messages, for logging.  It
will be appended to if there are more issues.
@return the warning string, longer if invalid parameters are detected.
*/
public static String validateParameterNames ( List<String> valid_Vector, Command command, String warning )
{	if ( command == null ) {
		return warning;
	}
	PropList parameters = command.getCommandParameters();
	List<String> warning_Vector = null;
	try {
	    // Validate the properties and discard any that are invalid (a message will be generated)
	    // and will be displayed once.
	    warning_Vector = parameters.validatePropNames (	valid_Vector, null, null, "parameter", true );
	}
	catch ( Exception e ) {
		// Ignore.  Should not happen but print out just in case.
		warning_Vector = null;
		Message.printWarning ( 3, "TSCommandProcessorUtil.validateParameterNames",
		    "Error checking parameter names (" + e + ")." );
		Message.printWarning ( 3, "TSCommandProcessorUtil.validateParameterNames", e );
	}
	if ( (warning_Vector != null) && (warning_Vector.size() > 0) ) {
		int size = warning_Vector.size();
		StringBuffer b = new StringBuffer();
		for ( int i = 0; i < size; i++ ) {
			warning += "\n" + warning_Vector.get (i);
			b.append ( warning_Vector.get(i));
		}
		if ( command instanceof CommandStatusProvider ) { 
			CommandStatus status = ((CommandStatusProvider)command).getCommandStatus();
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.WARNING, b.toString(),
					"Specify only valid parameters - see documentation."));
		}
	}
	return warning; // Return the original warning string with additional warnings if generated
}

}