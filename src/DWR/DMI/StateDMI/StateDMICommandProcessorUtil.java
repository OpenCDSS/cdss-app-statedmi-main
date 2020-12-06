// StateDMICommandProcessorUtil - This class contains static utility methods to support StateDMI_Processor.

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSEnsemble;
import RTi.TS.TSUtil_SortTimeSeries;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class contains static utility methods to support StateDMI_Processor.  These methods
are here to prevent the processor from getting to large and in some cases because code is being migrated.
*/
public abstract class StateDMICommandProcessorUtil
{

/**
PrintWriter for regression test results report.
*/
private static PrintWriter __regression_test_fp = null;
/**
Count of regression tests that fail (does not include disabled tests).
*/
private static int __regressionTestFailCount = 0;
/**
Count of regression tests that pass (does not include disabled tests).
*/
private static int __regressionTestPassCount = 0;
/**
Count of regression tests that are disabled (not included in pass/fail tests).
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
Count of output lines in regression output report.
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
    String enabled = "TRUE   ";
    if ( isEnabled ) {
	    if ( testPassFail.toUpperCase().indexOf("FAIL") >= 0 ) {
	        indicator = "*";
	        ++__regressionTestFailCount;
	    }
	    else {
	        ++__regressionTestPassCount;
	    }
    }
    else {
        ++__regressionTestDisabledCount;
        enabled = "FALSE  ";
        testPassFail = "    ";
    }
    String lineCount = StringUtil.formatString(__regressionTestLineCount,"%5d");
    //String runTime = "        ";
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
    		col = __regressionTestTable.getFieldIndex("Enabled");
    		rec.setFieldValue(col, enabled.trim());
    		col = __regressionTestTable.getFieldIndex("Run Time (ms)");
    		rec.setFieldValue(col, runTimeMs);
    		col = __regressionTestTable.getFieldIndex("Test Pass/Fail");
    		rec.setFieldValue(col, testPassFail.trim());
    		col = __regressionTestTable.getFieldIndex("Commands Expected Status");
    		rec.setFieldValue(col, expectedStatus.trim());
    		col = __regressionTestTable.getFieldIndex("Commands Actual Status");
    		rec.setFieldValue(col, ""+maxSeverity);
    		col = __regressionTestTable.getFieldIndex("Command File");
    		rec.setFieldValue(col, testCommandFile);
    		__regressionTestTable.addRecord(rec);
    	}
    	catch ( Exception e ) {
    		// Just ignore adding the test record to the table
    	}
    }
}

/**
Add a record to the regression test results report.  The report is a simple text file
that indicates whether a test passed.
@param processor CommandProcessor that is being run.
@param testPassFail whether the test was a success or failure (it is possible for the test to
be a successful even if the command file failed, if failure was expected)
@param expectedStatus the expected status (as a string)
@param maxSeverity the maximum severity from the command file that was run.
@param InputFile_full the full path to the command file that was run. 
*/
/* OLD version
public static void appendToRegressionTestReport(CommandProcessor processor, String testPassFail,
        String expectedStatus, CommandStatusType maxSeverity,
        String InputFile_full )
{
    ++__regressionTestLineCount;
    if ( __regression_test_fp != null ) {
        // FIXME SAM 2008-02-19 Would be useful to have command run time.
        String indicator = " ";
        if ( testPassFail.equalsIgnoreCase("FAIL") ) {
            indicator = "*";
            ++__regressionTestFailCount;
        }
        else {
            ++__regressionTestPassCount;
        }
        __regression_test_fp.println (
                StringUtil.formatString(__regressionTestLineCount,"%4d") + " " +
                indicator + StringUtil.formatString(testPassFail,"%-4.4s") + indicator + "  " +
                StringUtil.formatString(expectedStatus,"%-10.10s") + " " +
                StringUtil.formatString(maxSeverity,"%-10.10s") + " " + InputFile_full);
    }
}
*/

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
        wc += appendTimeSeriesToResultsList ( processor, command, (TS)tslist.get(i) );
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
Check that a requested table column is within the number of columns in a file, for example when reading
data from a list/delimited file.
@param param The name of the parameter,
@param reqCol1 The value of the parameter, as a String column number 1+.
@param reqCol0 The value of the parameter, as an integer column number 0+.  Pass in because
there may be special initialization to -1, etc. for missing.
@param numCol The number of columns available (1+).
@param status command status, for logging.
@param commandPhase command phase, for logging.
@param routine calling routine, for logging.
@param commandTag command tag, for logging.
@param warningLevel warning level, for logging.
@param warningCount count of warnings to this point, for logging
@return updated warning count if a problem is detected.
*/
protected static int checkColumnAgainstFileNumberOfColumns (
		String param, String reqCol1, int reqCol0, int numCol,
		CommandStatus status, CommandPhaseType commandPhase,
		String routine, String commandTag, int warningLevel, int warningCount )
throws CommandException
{
	if ( (reqCol0 >= 0) && (reqCol0 >= numCol) ) {
		String message = "Requested " + param + " column (" + reqCol1 +
		") is greater than the number of columns in the file (" + numCol + ").";
        Message.printWarning ( warningLevel,
            MessageUtil.formatMessageTag( commandTag, ++warningCount ), routine, message );
            status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,
                    "Specify an ID column <= " + numCol));
        throw new CommandException ( message );
	}
	return warningCount;
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
    int totalCount = getRegressionTestFailCount() + getRegressionTestPassCount() + getRegressionTestDisabledCount();
    __regression_test_fp.println ( "FAIL count     = " + getRegressionTestFailCount() +
        ", " + StringUtil.formatString(100.0*(double)getRegressionTestFailCount()/(double)totalCount,"%.3f")+ "%");
    __regression_test_fp.println ( "PASS count     = " + getRegressionTestPassCount() +
        ", " + StringUtil.formatString(100.0*(double)getRegressionTestPassCount()/(double)totalCount,"%.3f")+ "%");
    __regression_test_fp.println ( "Disabled count = " + getRegressionTestDisabledCount() +
    	", " + StringUtil.formatString(100.0*(double)getRegressionTestDisabledCount()/(double)totalCount,"%.3f")+ "%");
    __regression_test_fp.println ( "#--------------------------------" );
    __regression_test_fp.println ( "Total          = " + totalCount );
    
    __regression_test_fp.close();
}

/**
Expand a parameter valueto recognize processor-level properties.  For example, a parameter value like
"${WorkingDir}/morepath" will be expanded to include the working directory.
@param processor the CommandProcessor that has a list of named properties.
@param command the command that is being processed (may be used later for context sensitive values).
@param parameterValue the parameter value being expanded.
*/
public static String expandParameterValue( CommandProcessor processor, Command command, String parameterValue )
{   String routine = "TSCommandProcessorUtil.expandParameterValue";
    if ( (parameterValue == null) || (parameterValue.length() == 0) ) {
        // Just return what was provided.
        return parameterValue;
    }
    // Else see if the parameter value can be expanded to replace $ symbolic references with other values
    // Search the parameter string for $ until all processor parameters have been resolved
    int searchPos = 0;  // Position in the "parameter_val" string to search for $ references
    int foundPos;       // Position when leading ${ is found
    int foundPosEnd;   // Position when ending } is found
    String foundProp = null;    // Whether a property is found that matches the $ symbol
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
        Message.printStatus ( 2, routine, "Found " + delimStart + " at position [" + foundPos + "]");
        // Get the name of the property
        foundProp = parameterValue.substring((foundPos+2),foundPosEnd);
        // Try to get the property from the processor
        // TODO SAM 2007-12-23 Evaluate whether to skip null.  For now show null in result.
        Object propval = null;
        String propvalString = null;
        try {
            propval = processor.getPropContents ( foundProp );
            propvalString = "" + propval;
        }
        catch ( Exception e ) {
            // Keep the original value
            propvalString = delimStart + propval + delimEnd;
        }
        StringBuffer b = new StringBuffer();
        // Append the start of the string
        if ( foundPos > 0 ) {
            b.append ( parameterValue.substring(0,foundPos) );
        }
        // Now append the value of the property...
        b.append ( propvalString );
        // Now append the end of the original string if anything is at the end...
        if ( parameterValue.length() > (foundPosEnd + 1) ) {
            b.append ( parameterValue.substring(foundPosEnd + 1) );
        }
        // Now reset the search position to finish evaluating whether to expand the string.
        parameterValue = b.toString();
        searchPos = foundPos + propvalString.length();   // Expanded so no need to consider delim*
        Message.printStatus( 2, routine, "Expanded property value is \"" + parameterValue +
                "\" searchpos is now " + searchPos + " in string \"" + parameterValue + "\"" );
    }
    return parameterValue;
}

/**
Expand a string to recognize time series % formatting strings using TS.formatLegend()
and also ${Property} strings.  If a property string is not found, it will remain
without being replaced.
@param processor The processor that is being used.
@param ts Time series to be used for metadata string.
@param s String to expand.  The string can contain % format specifiers used with TS.
@param status CommandStatus to add messages to if problems occur.
*/
public static String expandTimeSeriesMetadataString ( CommandProcessor processor, TS ts, String s,
        CommandStatus status, CommandPhaseType command_phase )
{   String routine = "TSCommandProcessorUtil.expandTimeSeriesMetadataString";
    if ( s == null ) {
        return "";
    }
    // First expand using the % characters...
    String s2 = ts.formatLegend ( s );
    Message.printStatus(2, routine, "After formatLegend(), string is \"" + s2 + "\"" );
    // Now replace ${Property} strings with properties from the processor
    int start = 0;
    int pos2 = 0;
    while ( pos2 < s2.length() ) {
        int pos1 = s2.indexOf( "${", start );
        if ( pos1 >= 0 ) {
            // Find the end of the property
            pos2 = s2.indexOf( "}", pos1 );
            if ( pos2 > 0 ) {
                // Get the property...
                String propname = s2.substring(pos1+2,pos2);
                String propval_string = "";
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "PropertyName", propname );
                CommandProcessorRequestResultsBean bean = null;
                try {
                    bean = processor.processRequest( "GetProperty", request_params);
                }
                catch ( Exception e ) {
                    String message = "Error requesting GetProperty(Property=\"" + propname + "\") from processor.";
                    Message.printWarning ( 3,routine, message );
                    if ( status != null ) {
                        status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    }
                    start = pos2;
                    continue;
                }
                if ( bean == null ) {
                    String message =
                        "Unable to find property from processor using GetProperty(Property=\"" + propname + "\").";
                    Message.printWarning ( 3,routine, message );
                    if ( status != null ) {
                        status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message,
                                "Verify that the property name is valid - must match case." ) );
                    }
                    start = pos2;
                    continue;
                }
                PropList bean_PropList = bean.getResultsPropList();
                Object o_PropertyValue = bean_PropList.getContents ( "PropertyValue" );
                if ( o_PropertyValue == null ) {
                    String message =
                        "Null PropertyValue returned from processor for GetProperty(PropertyName=\"" + propname + "\").";
                    Message.printWarning ( 3, routine, message );
                    if ( status != null ) {
                        status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message,
                                "Verify that the property name is valid - must match case." ) );
                    }
                    start = pos2;
                    continue;
                }
                else {
                    propval_string = o_PropertyValue.toString();
                    start = pos2;
                }
                // Replace the string and continue to evaluate s2
                s2 = s2.substring ( 0, pos1 ) + propval_string + s2.substring (pos2 + 1);
            }
            else {
                // No closing character so march on...
                start = pos1 + 2;
                if ( start > s2.length() ) {
                    break;
                }
            }
        }
        else {
            // Done processing properties.
            break;
        }
    }
    return s2;
}
	
/**
Get the commands before the indicated index position.  Only the requested commands are returned.
Use this, for example, to get the setWorkingDir() commands above
the insert position for a readXXX() command, so the working directory can be
defined and used in the editor dialog.
@return List of commands (as list of Command instances) before the index that match the commands in
the neededCommandsList.  This will always return a non-null list, even if
no commands are in the list.
@param index The index in the command list before which to search for other commands.
@param processor A TSCommandProcessor with commands to search.
@param neededCommandsList List of commands (as String) that need to be processed
(e.g., "setWorkingDir").  Only the main command name should be defined.
@param lastOnly if true, only the last item before the requested index is returned.
If false, all matching commands above the point are returned in the order from top to bottom.
*/
public static List<Command> getCommandsBeforeIndex (
	int index,
	StateDMI_Processor processor,
	List<String> neededCommandsList,
	boolean lastOnly )
{	// Now search backwards matching commands for each of the requested commands...
	int size = 0;
	if ( neededCommandsList != null ) {
		size = neededCommandsList.size();
	}
	List<Command> foundCommands = new ArrayList<>();
	// Get the commands from the processor
	List<Command> commands = processor.getCommands();
	Command command;
	// Now loop up through the command list...
	for ( int ic = (index - 1); ic >= 0; ic-- ) {
		command = commands.get(ic);
		for ( String neededCommandString : neededCommandsList) {
			// TODO smalers 2020-12-05 old logic compared full string, but should just use command name
			// - remove the old code when tests out
			//if ( neededCommandString.regionMatches(true,0,command.toString().trim(),0, neededCommandString.length() ) ) {
			if ( neededCommandString.equalsIgnoreCase(command.getCommandName()) ) {
				foundCommands.add ( command );
				if ( lastOnly ) {
					// Don't need to search any more...
					break;
				}
			}
		}
	}
	// Reverse the commands so they are listed in the order of the list...
	size = foundCommands.size();
	if ( size <= 1 ) {
		// No need to sort
		return foundCommands;
	}
	List<Command> foundCommandsSorted = new ArrayList<>(size);
	for ( int i = (size - 1); i >= 0; i-- ) {
		foundCommandsSorted.add ( foundCommandsSorted.get(i));
	}
	return foundCommandsSorted;
}
	
/**
Get the commands above an index position.
@param processor The processor that is managing commands.
@param pos Index (0+) before which to get commands.  The command at the indicated
position is NOT included in the search.
*/
private static List<Command> getCommandsBeforeIndex ( StateDMI_Processor processor, int pos )
{	List<Command> commands = new ArrayList<>();
	int size = processor.size();
	if ( pos > size ) {
		pos = size;
	}
	for ( int i = 0; i < pos; i++ ) {
		commands.add ( processor.get(i));
	}
	return commands;
}

/**
Get the maximum command status severity for the processor.  This is used, for example, when
determining an overall status for a runCommands() command.
@param processor Command processor to check status.
@return most severe command status from all commands in a processor.
*/
public static CommandStatusType getCommandStatusMaxSeverity ( StateDMI_Processor processor )
{
	int size = processor.size();
	Command command;
	CommandStatusType most_severe = CommandStatusType.UNKNOWN;
	CommandStatusType from_command;
	for ( int i = 0; i < size; i++ ) {
		command = processor.get(i);
		if ( command instanceof CommandStatusProvider ) {
			from_command = CommandStatusUtil.getHighestSeverity((CommandStatusProvider)command);
			//Message.printStatus (2,"", "Highest severity \"" + command.toString() + "\"=" + from_command.toString());
			most_severe = CommandStatusType.maxSeverity(most_severe,from_command);
		}
	}
	return most_severe;
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
Get a list of ensemble identifiers from a list of commands.  See documentation for
fully loaded method.  The output list is not sorted..
@param commands Commands to search.
@return list of table identifiers or an empty non-null Vector if nothing found.
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
        return new Vector<String>();
    }
	List<String> v = new Vector<String>( 10, 10 );
    boolean in_comment = false;
    String command_string = null;
    for ( Command command : commands ) {
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
        	ObjectListProvider olp = (ObjectListProvider)command;
            List<? extends TSEnsemble> list = olp.getObjectList ( TSEnsemble.class );
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
in the TSCommandProcessor.  This is used, for example, to provide a list of
identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a Vector of String containing the ensemble identifiers, or an empty Vector.
*/
public static List<String> getEnsembleIdentifiersFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{   String routine = "TSCommandProcessorUtil.getEnsembleIdentifiersFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    Message.printStatus ( 2, routine, "Position in list is " + pos + " for command:" + command );
    if ( pos < 0 ) {
        // Just return a blank list...
        return new Vector<String>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series identifiers from the commands...
    return getEnsembleIdentifiersFromCommands ( commands );
}

// TODO smalers 2019-05-29 should this return TS or StringMonthTS
/**
Return the pattern time series for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of
time identifiers to editor dialogs, using information determined during discovery.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series are needed.
@return a List of pattern time series.
*/
public static List<TS> getPatternTSListFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
throws Exception
{   //String routine = "TSCommandProcessorUtil.getPatternTSFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    //Message.printStatus ( 2, routine, "Position in list is " + pos + " for command:" + command );
    if ( pos < 0 ) {
        // Just return a blank list...
        return new Vector<TS>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series identifiers from the commands...
    return getPatternTSListFromCommands ( commands );
}

// TODO smalers 2019-05-29 should this return TS or StringMonthTS
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
protected static List<TS> getPatternTSListFromCommands ( List<Command> commands )
throws Exception
{   if ( commands == null ) {
        return new Vector<TS>();
    }
	List<TS> v = new Vector<TS>( 10, 10 );
    for ( Command command: commands ) {
        if ( (command != null) && (command instanceof ObjectListProvider) ) {
        	ObjectListProvider olp = (ObjectListProvider)commands;
            // Try to get the list of identifiers using the interface method.
            // TODO SAM 2007-12-07 Evaluate the automatic use of the alias.
            List<? extends TS> list = olp.getObjectList ( TS.class );
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
    TSUtil_SortTimeSeries<TS> tsu = new TSUtil_SortTimeSeries<TS>(v,"TSID",null,null,1);
    v = tsu.sortTimeSeries();
    return v;
}

/**
Return the list of property names available from the processor.
These properties can be requested using getPropContents().
@return the list of property names available from the processor.
*/
public static Collection<String> getPropertyNameList( CommandProcessor processor )
{
	// This could use reflection.
	if ( processor instanceof StateDMI_Processor ) {
		return ((StateDMI_Processor)processor).getPropertyNameList(true, true);
	}
	return new Vector<String>();
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

// FIXME SAM 2008-01-31 Need to sort the column names.
/**
Return the table column names, searching commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of
column names to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@param sort Indicates whether column names should be sorted (NOT YET IMPLEMENTED).
@return a Vector of String containing the ensemble identifiers, or an empty Vector.
*/
public static List<String> getTableColumnNamesFromCommandsBeforeCommand(
		StateDMI_Processor processor, Command command, String table_id, boolean sort )
{   String routine = "TSCommandProcessorUtil.getTableColumnNamesFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    Message.printStatus ( 2, routine, "Position in list is " + pos + " for command:" + command );
    // Loop backwards because tables may be modified and we want the column names from
    // the table as close previous to the command in question.
    DataTable table;
    for ( int i = (pos - 1); i >= 0; i-- ) {
        command = processor.get(i);
        if ( command instanceof ObjectListProvider ) {
            // Request table objects
        	ObjectListProvider olp = (ObjectListProvider)command;
            List<? extends DataTable> tables = olp.getObjectList(DataTable.class);
            int ntables = 0;
            if ( tables != null ) {
                ntables = tables.size();
            }
            for ( int it = 0; it < ntables; it++ ) {
                table = (DataTable)tables.get(it);
                if ( !table.getTableID().equalsIgnoreCase(table_id) ) {
                    continue;
                }
                // Found the table.  Get its column names.
                String [] field_names = table.getFieldNames();
                List<String> field_names_Vector = new Vector<String>();
                for ( int in = 0; in < field_names.length; in++ ) {
                    field_names_Vector.add ( field_names[in] );
                }
                return field_names_Vector;
            }
        }
    }
    // Nothing found...
    return new Vector<String>();
}

/**
Get a list of table identifiers from a list of commands.  See documentation for
fully loaded method.
@param commands Time series commands to search.
@return list of table identifiers or an empty non-null Vector if nothing
found.
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
@return list of table identifiers or an empty non-null Vector if nothing found.
*/
protected static List<String> getTableIdentifiersFromCommands ( List<Command> commands, boolean sort )
{   if ( commands == null ) {
        return new Vector<String>();
    }
	List<String> v = new Vector<String> ( 10, 10 );
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
        	ObjectListProvider olp = (ObjectListProvider)command;
            List<? extends DataTable> list = olp.getObjectList ( DataTable.class );
            String id;
            if ( list != null ) {
                int tablesize = list.size();
                DataTable table;
                for ( int its = 0; its < tablesize; its++ ) {
                    table = list.get(its);
                    id = table.getTableID();
                    if ( !id.equals("") ) {
                        v.add( id );
                    }
                }
            }
        }
    }
    return v;
}

/**
Return the table identifiers for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of
identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a Vector of String containing the table identifiers, or an empty Vector.
*/
public static List<String> getTableIdentifiersFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{   String routine = "TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand";
    // Get the position of the command in the list...
    int pos = processor.indexOf(command);
    Message.printStatus ( 2, routine, "Position in list is " + pos + " for command:" + command );
    if ( pos < 0 ) {
        // Just return a blank list...
        return new Vector<String>();
    }
    // Find the commands above the position...
    List<Command> commands = getCommandsBeforeIndex ( processor, pos );
    // Get the time series identifiers from the commands...
    return getTableIdentifiersFromCommands ( commands );
}

/**
Get values for a tag in command comments.  Tags are strings like "@tagName" or "@tagName value"
(without the quotes).
@param processor CommandProcessor to evaluate.
@param tag Tag to search for, without the leading "@".
@param defaultValue value to use when the tag has no value, can be null.
Default is null.
@return a list of tag values, which are either Strings for the value or the indicated
value for for tags with no value.  Return an empty list if the tag was not found.
*/
public static List<String> getTagValues ( CommandProcessor processor, String tag, String defaultValue )
{
	List<String> tagValues = new Vector<String>();
    // Loop through the commands and check comments for the special string
    List<Command> commandList = ((StateDMI_Processor)processor).getCommands();
    int size = commandList.size();
    Command c;
    String searchTag = "@" + tag;
    for ( int i = 0; i < size; i++ ) {
        c = commandList.get(i);
        String commandString = c.toString();
        if ( !commandString.trim().startsWith("#") ) {
            continue;
        }
        // Check the comment.
        int pos = StringUtil.indexOfIgnoreCase(commandString,searchTag,0);
        if ( pos >= 0 ) {
        	List<String> parts = StringUtil.breakStringList(
                commandString.substring(pos)," \t", StringUtil.DELIM_SKIP_BLANKS);
            if ( parts.size() == 1 ) {
                // No value to the tag so add the default value
                tagValues.add ( defaultValue );
            }
            else {
                // Add as a string - note that this value may contain multiple values separated by
                // commas or some other encoding.  The calling code needs to handle.
                tagValues.add ( parts.get(1).trim() );
            }
        }
    }
    return tagValues;
}

/**
Get values for a tag in command file comments.  Tags are strings like "@tagName" or "@tagName value"
(without the quotes).
@param processor CommandProcessor to evaluate.
@param tag Tag to search for, without the leading "@".
@param defaultValue value to use when the tag has no value, can be null.
@return a list of tag values, which are either Strings for the value or True if the tag has
no value.  Return an empty list if the tag was not found.
*/
public static List<String> getTagValues ( String commandFile, String tag, String defaultValue )
throws IOException, FileNotFoundException
{
	StateDMI_Processor processor = new StateDMI_Processor();
    processor.readCommandFile(commandFile, true, false);
    return getTagValues ( processor, tag, defaultValue );
}

/**
Get a list of identifiers from a list of commands.  See documentation for
fully loaded method.  The output list is not sorted and does NOT contain the
input type or name.
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
@return list of time series identifiers or an empty non-null Vector if nothing
found.
*/
protected static List<String> getTSIdentifiersFromCommands ( List<Command> commands, boolean sort )
{	// Return the identifiers without the input type and name.
	return getTSIdentifiersFromCommands ( commands, false, sort );
}

/**
Get a list of identifiers from a list of commands.
The returned strings are suitable for drop-down lists, etc.
Time series identifiers are determined as follows:
<ol>
<li>    Commands that implement ObjectListProvider have their getObjectList(TS) method called.
        The time series identifiers from the time series list are examined and those with alias
        will have the alias returned.  Otherwise, the full time series identifier is returned with or
        with input path as requested.</li>
</ol>
@param commands commands to search.
@param include_input If true, include the input type and name in the returned
values.  If false, only include the 5-part information.
@param sort Should output be sorted by identifier.
@return list of time series identifiers or an empty non-null list if nothing found.
*/
protected static List<String> getTSIdentifiersFromCommands ( List<Command> commands, boolean include_input, boolean sort )
{	if ( commands == null ) {
		return new Vector<String>();
	}
	List<String> v = new Vector<String>( 10, 10 );
	String commandString = null;
	boolean in_comment = false;
	for ( Command command : commands ) {
		if ( command == null ) {
			// Should not happen
			continue;
		}
		commandString = command.toString().trim();
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
        if ( command instanceof ObjectListProvider ) {
            // Try to get the list of identifiers using the interface method.
            // TODO SAM 2007-12-07 Evaluate the automatic use of the alias.
        	ObjectListProvider olp = (ObjectListProvider)command;
            List<? extends TS> list = olp.getObjectList ( TS.class );
            if ( list != null ) {
                int tssize = list.size();
                TS ts;
                for ( int its = 0; its < tssize; its++ ) {
                    ts = list.get(its);
                    if ( !ts.getAlias().equals("") ) {
                        // Use the alias if it is available.
                        v.add( ts.getAlias() );
                    }
                    else {
                        // Use the identifier.
                        v.add ( ts.getIdentifier().toString(include_input) );
                    }
                }
            }
        }
	}
	return v;
}

/**
Return the time series identifiers for commands before a specific command
in the TSCommandProcessor.  This is used, for example, to provide a list of
identifiers to editor dialogs.
@param processor a TSCommandProcessor that is managing commands.
@param command the command above which time series identifiers are needed.
@return a Vector of String containing the time series identifiers, or an empty
Vector.
*/
public static List<String> getTSIdentifiersNoInputFromCommandsBeforeCommand( StateDMI_Processor processor, Command command )
{	String routine = "TSCommandProcessorUtil.getTSIdentifiersNoInputFromCommandsBeforeCommand";
	// Get the position of the command in the list...
	int pos = processor.indexOf(command);
	Message.printStatus ( 2, routine, "Position in list is " + pos + " for command:" + command );
	if ( pos < 0 ) {
		// Just return a blank list...
		return new Vector<String>();
	}
    // Find the commands above the position...
	List<Command> commands = getCommandsBeforeIndex ( processor, pos );
	// Get the time series identifiers from the commands...
	return getTSIdentifiersFromCommands ( commands );
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
Evaluate whether a command appears to be a pure time series identifier (not a
command that uses a time series identifier).  The string is checked to see if
it has three "." and that any parentheses are after the first ".".
Some of these checks are needed for TSIDs
that have data types with () - this is the case with some input types (e.g.,
HydroBase agricultural statistics that have "(Dry)".
@param command Command to evaluate.
@return true if the command appears to be a pure TSID, false if not.
*/
protected static boolean isTSID ( String command )
{	int left_paren_pos = command.indexOf('(');
	int right_paren_pos = command.indexOf(')');
	int period_pos = command.indexOf('.');

	if ( command.startsWith( "TS " ) ) {
	    // TS Alias command
	    return false;
	}
    if ( command.trim().startsWith( "#" ) ) {
        // Comment
        return false;
    }
	if ((StringUtil.patternCount(command,".") >= 3) &&
			(((left_paren_pos < 0) &&	// Definitely not a
			(right_paren_pos < 0)) ||	// command.
			((left_paren_pos > 0) &&	// A TSID with ()
			(period_pos > 0) &&
			(left_paren_pos > period_pos))) ) {
		return true;
	}
	else {
	    return false;
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
	List<TS> tslist = new Vector<TS>();
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
		// Ignore.  Should not happen.
		warning_Vector = null;
	}
	if ( warning_Vector != null ) {
		int size = warning_Vector.size();
		StringBuffer b = new StringBuffer();
		for ( int i = 0; i < size; i++ ) {
			warning += "\n" + warning_Vector.get (i);
			b.append ( warning_Vector.get(i));
		}
		if ( command instanceof CommandStatusProvider ) { 
			CommandStatus status = ((CommandStatusProvider)command).getCommandStatus();
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.WARNING,
					b.toString(),
					"Specify only valid parameters - see documentation."));
		}
	}
	return warning;
}

}