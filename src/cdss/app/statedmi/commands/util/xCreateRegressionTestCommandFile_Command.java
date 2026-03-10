// CreateRegressionTestCommandFile_Command - This class initializes, checks, and runs the CreateRegressionTestCommandFile() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

//package rti.tscommandprocessor.commands.util;
package cdss.app.statedmi.commands.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandFile;
import RTi.Util.IO.CommandFileOrderType;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the CreateRegressionTestCommandFile() command.
*/
public class xCreateRegressionTestCommandFile_Command extends AbstractCommand
implements FileGenerator
{

/**
Data members used for parameter values.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public xCreateRegressionTestCommandFile_Command () {
	super();
	setCommandName ( "CreateRegressionTestCommandFile" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param commandTag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warningLevel The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String commandTag, int warningLevel )
throws InvalidCommandParameterException {
	String routine = getClass().getSimpleName() + ".checkCommandParameters";
    String SearchFolder = parameters.getValue ( "SearchFolder" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String SetupCommandFile = parameters.getValue ( "SetupCommandFile" );
	String EndCommandFile = parameters.getValue ( "EndCommandFile" );
	//String FilenamePattern = parameters.getValue ( "FilenamePattern" );
	String Append = parameters.getValue ( "Append" );
	String warning = "";
    String message;

    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);

	// The existence of the parent directories or files is not checked
	// because the files may be created dynamically after the command is edited.

	if ( (SearchFolder == null) || (SearchFolder.length() == 0) ) {
        message = "The search folder must be specified.";
		warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Specify the search folder."));
	}
    else if ( SearchFolder.indexOf("${") < 0 ) {
        String working_dir = null;
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
                // Working directory is available so use it.
                if ( o != null ) {
                    working_dir = (String)o;
                }
            }
            catch ( Exception e ) {
                message = "Error requesting WorkingDir from processor.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
            }

        try {
            //String adjusted_path =
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,SearchFolder)));
        }
        catch ( Exception e ) {
            message = "The search folder:\n" +
            "    \"" + SearchFolder +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that search folder and working directory paths are compatible." ) );
        }
    }

	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The output file must be specified.";
		warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Specify the output file name."));
	}
	else if ( OutputFile.indexOf("${") < 0 ) {
        String working_dir = null;
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Software error - report the problem to support." ) );
        }

        try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The output file parent directory does not exist for: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Create the output directory." ) );
            }
            f = null;
            f2 = null;
        }
        catch ( Exception e ) {
            message = "The output file:\n" +
            "    \"" + OutputFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that output file and working directory paths are compatible." ) );
        }
    }

    if ( (SetupCommandFile != null) && (SetupCommandFile.length() != 0) && (SetupCommandFile.indexOf("${") < 0) ) {
        String working_dir = null;
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
                // Working directory is available so use it.
                if ( o != null ) {
                    working_dir = (String)o;
                }
            }
            catch ( Exception e ) {
                message = "Error requesting WorkingDir from processor.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }

        try {
            //String adjusted_path =
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,SetupCommandFile)));
        }
        catch ( Exception e ) {
            message = "The setup command file:\n" +
            "    \"" + SetupCommandFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that setup command file and working directory paths are compatible." ) );
        }
    }

    if ( (EndCommandFile != null) && !EndCommandFile.isEmpty() && (EndCommandFile.indexOf("${") < 0) ) {
        String working_dir = null;
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
            // Working directory is available so use it.
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            Message.printWarning(3, routine, message );
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }

        try {
            //String adjusted_path =
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,EndCommandFile)));
        }
        catch ( Exception e ) {
            message = "The end command file:\n" +
            "    \"" + EndCommandFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that end command file and working directory paths are compatible." ) );
        }
    }

	if ( (Append != null) && !Append.equals("") ) {
		if ( !Append.equalsIgnoreCase(_False) && !Append.equalsIgnoreCase(_True) ) {
            message = "The Append parameter \"" + Append + "\" must be False or True.";
			warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the parameter as False or True."));
		}
	}

	// Check for invalid parameters.
	List<String> validList = new ArrayList<>(12);
	validList.add ( "SearchFolder" );
	validList.add ( "OutputFile" );
    validList.add ( "SetupCommandFile" );
    validList.add ( "TestResultsFile" );
    validList.add ( "EndCommandFile" );
    validList.add ( "FilenamePattern" );
	validList.add ( "Append" );
	validList.add ( "IncludeTestSuite" );
	validList.add ( "ExcludeTestSuite" );
	validList.add ( "IncludeOS" );
	validList.add ( "UseOrder" );
	validList.add ( "TestResultsTableID" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

    // Throw an InvalidCommandParameterException in case of errors.
    if ( warning.length() > 0 ) {
        Message.printWarning ( warningLevel,
            MessageUtil.formatMessageTag(
                commandTag, warningLevel ), warning );
        throw new InvalidCommandParameterException ( warning );
    }

    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Determine the expected status parameter by searching the command file for an "@expectedStatus" string.
@param filename Name of file to open to scan.
@return a string for the ExpectedStatus parameter or empty string if no expected status is
used for the command file (default expected status is success).
*/
private String determineExpectedStatusParameter ( CommandFile commandFile )
throws FileNotFoundException {
    // Default is success, which will not add an ExpectedStatus parameter.
	String expectedStatusParameter = "";
    CommandStatusType expectedStatus = commandFile.getExpectedStatus();
   	// Translate variations to the official name recognized by RunCommands().
    if ( expectedStatus == CommandStatusType.WARNING ) {
    	expectedStatusParameter = ",ExpectedStatus=\"Warning\"";
    }
    else if ( expectedStatus == CommandStatusType.FAILURE ) {
    	expectedStatusParameter = ",ExpectedStatus=\"Failure\"";
   	}
    return expectedStatusParameter;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent ) {
	// The command will be modified if changed.
	return (new xCreateRegressionTestCommandFile_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList () {
	List<File> list = new ArrayList<>();
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
    }
    return list;
}

// FIXME SAM 2008-07-31 Should separate out this method from the checks for tags to simplify logic of each.
/**
Visits all files and directories under the given directory and if
the file matches a valid commands file it is added to the test list.
All commands file that end with ".<product_name>" will be added to the list.
@param commandFileList List of command files that are matched, to be appended to.
@param path Folder in which to start searching for command files.
@param patterns Array of pattern to match when searching files, for example "test*.StateDMI", Java regular expressions.
@param includedTestSuites the test suites for test cases that should be included,
indicated by "@testSuite ABC" tags in the comments of command files, will be compared ignoring case
@param excludedTestSuites the test suites for test cases that
should be excluded, indicated by "@testSuite ABC" tags in the comments of command files, will be compared ignoring case
@param includedOS the operating systems for test cases that should be included,
indicated by "@os Windows", "@os linux", and "@os UNIX" tags in the comments of command files.
@throws IOException
 */
private void getMatchingFilenamesInTree ( List<String> commandFileList, File path, String[] patterns,
        String[] includedTestSuites, String[] excludedTestSuites, String[] includedOS )
throws IOException {
    String routine = getClass().getSimpleName() + ".getMatchingFilenamesInTree";
    // Determine if UNIX, linux, and Windows tests have been requested.
    // Check the OS only if the specific.
    boolean needToCheckForUnixOS = false;
    boolean needToCheckForWindowsOS = false;
    for ( int i = 0; i < includedOS.length; i++ ) {
        if ( includedOS[i].equalsIgnoreCase("UNIX") || includedOS[i].equalsIgnoreCase("linux")) {
            needToCheckForUnixOS = true;
  	    	if ( Message.isDebugOn ) {
  	    		Message.printStatus ( 2, routine, "Will only include tests that are for UNIX/Linux." );
  	    	}
        }
    }
    for ( int i = 0; i < includedOS.length; i++ ) {
        if ( includedOS[i].equalsIgnoreCase("Windows") ) {
            needToCheckForWindowsOS = true;
  	    	if ( Message.isDebugOn ) {
  	    		Message.printStatus ( 2, routine, "Will only include tests that are for Windows." );
  	    	}
        }
    }
    if (path.isDirectory()) {
        String[] children = path.list();
        for (int i = 0; i < children.length; i++) {
        	// Recursively call with full path using the directory and child name.
        	getMatchingFilenamesInTree(commandFileList,new File(path,children[i]), patterns,
    	        includedTestSuites, excludedTestSuites, includedOS );
        }
    }
    else {
        // Add to list if command file is valid.
        String pathName = path.getName();
    	// Do comparison on file name without directory.
    	for ( int i = 0; i < patterns.length; i++ ) {
    		String pattern = patterns[i];
    		if ( Message.isDebugOn ) {
    			Message.printStatus(2, "", "Checking path \"" + pathName + "\" against pattern \"" + pattern + "\"" );
    		}
    	    if( pathName.matches( pattern )
    		    // FIXME SAM 2007-10-15 Need to enable something like the following to make more robust.
    		    //&& isValidCommandsFile( dir )
    		    ) {
    	    	if ( Message.isDebugOn ) {
    	    		Message.printStatus(2, "", "    File matched." );
    	    	}
        	    // Exclude the command file if tag in the file indicates that it is not compatible with this command's parameters.
        	    boolean doAddForOS = false;
        	    List<Object> tagValues = TSCommandProcessorUtil.getTagValues ( path.toString(), "os" );
        	    if ( !needToCheckForUnixOS && !needToCheckForWindowsOS ) {
        	        // Not checking for OS so go ahead and add.
        	        doAddForOS = true;
        	    }
        	    if ( !doAddForOS && needToCheckForUnixOS ) {
                    boolean tagHasUNIX = false;
        	        // os tag needs to be blank or include "UNIX".
        	        for ( int ivalue = 0; ivalue < tagValues.size(); ivalue++ ) {
        	            Object o = tagValues.get(ivalue);
        	            if ( o instanceof String ) {
        	                String s = (String)o;
        	                if ( s.toUpperCase().matches("UNIX") || s.toUpperCase().matches("LINUX") ) {
        	                    tagHasUNIX = true;
        	                }
        	            }
        	        }
                    if ( (tagValues.size() == 0) || tagHasUNIX ) {
                        // Test is not OS-specific or test is for UNIX so include for UNIX.
                        doAddForOS = true;
                    }
         	    }
        	    if ( !doAddForOS && needToCheckForWindowsOS ) {
                    boolean tagHasWindows = false;
                    // os tag needs to be blank or include "Windows".
                    for ( int ivalue = 0; ivalue < tagValues.size(); ivalue++ ) {
                        Object o = tagValues.get(ivalue);
                        if ( o instanceof String ) {
                            String s = (String)o;
                            if ( s.toUpperCase().matches("WINDOWS") ) {
                                tagHasWindows = true;
                            }
                        }
                    }
                    if ( (tagValues.size() == 0) || tagHasWindows ) {
                        // Test is not OS-specific or test is for Windows so include for Windows.
                        doAddForOS = true;
                    }
        	    }
        	    // Check to see if the test suite has been specified and matches that in the file.
        	    boolean doAddForTestSuite = false;
        	    if ( (includedTestSuites.length == 0) && (excludedTestSuites.length == 0) ) {
        	    	// No filtering test suites have been specified so include all by default.
                    Message.printStatus(2, routine, "Excluding test because no filters for included or excluded tests.");
        	        doAddForTestSuite = true;
        	    }
        	    else {
        	        // Check to see if the test suites in the test match the requested test suites:
        	    	// - first get comment tag values for #@testSuite
        	        List<Object> tagValues2 = TSCommandProcessorUtil.getTagValues ( path.toString(), "testSuite" );
        	        if ( (tagValues2.size() == 0) && ((includedTestSuites.length == 0) || includedTestSuites[0].equals(".*")) ) {
        	            // Test suite is not specified for the test so it is always included initially:
        	        	// - use the Java regular expression ".*" rather than just "."
        	        	Message.printStatus(2, routine, "Initially including test because test has no @testSuite and no included suites: " + path);
        	            doAddForTestSuite = true;
        	        }
        	        // Check the tag values from the file against requested test suites:
        	        // - a test can be in more than one suite
        	        for ( Object tagValueObject : tagValues2 ) {
        	            if ( !(tagValueObject instanceof String) ) {
        	            	// Should not happen because suite name should always be a string.
        	                continue;
        	            }
        	            String tagValue = (String)tagValueObject;
        	            // First include test suites that are included (may use '*' to match all):
        	            // - if no suite(s) to include are specified, include all tests (handled above)
        	            // - if suite(s) to include are specified, only include if match below
        	            for ( String includedTestSuite : includedTestSuites ) {
        	                if ( tagValue.toUpperCase().matches(includedTestSuite.toUpperCase()) ) {
        	                    doAddForTestSuite = true;
        	                    Message.printStatus(2, routine, "Including test because included test suites matches @testSuite: " + path);
        	                    // Matched a pattern so break.
        	                    break;
        	                }
        	            }
        	            // After including test suites, exclude:
        	            // - first exclude specifically requested suites
        	            // - also exclude if 'nosuite' was requested (TODO smalers 2021-10-15 remove this code if not needed)
        	            for ( String excludedTestSuite : excludedTestSuites ) {
        	                if ( tagValue.toUpperCase().matches(excludedTestSuite.toUpperCase()) ) {
        	                    doAddForTestSuite = false;
        	                    Message.printStatus(2, routine, "Excluding test because test suite '" + excludedTestSuite + "' is excluded: " + path);
        	                    // Matched a pattern so break.
        	                    break;
        	                }
        	                // More general test to exclude tests that are not in a suite:
        	                // - this probably does not need to be used
        	                /*
        	                if ( (tagValues2.size() == 0) && excludedTestSuite.equalsIgnoreCase("nosuite") ) {
        	                	// Test has no test suite and such tests are being excluded as 'nosuite'.
        	                    Message.printStatus(2, routine, "Excluding test because test has no suite and 'nosuite' is excluded: " + path);
        	                	doAddForTestSuite = false;
        	                	break;
        	                }
        	                */
        	            }
        	            // Next remove test suites that are excluded.
        	            if ( !doAddForTestSuite ) {
        	            	// No need to keep searching since is excluded.
        	                break;
        	            }
        	        }
        	    }
        	    if ( doAddForOS && doAddForTestSuite ) {
        	        // Test is to be included for the OS and test suite.
        	        commandFileList.add(path.toString());
        	    }
    	    }
        }
    }
}

/**
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile () {
    return __OutputFile_File;
}

/**
Include the setup command file in the regression test command file.
@param out PrintWriter to write to.
@param includeCommandFile full path for setup command file.
@param label a short label that indicates the type of file being included ("setup" or "end")
@exception IOException if there is an error including the file.
*/
private void includeCommandFile ( PrintWriter out, String includeCommandFile, String label )
throws IOException {
    //String routine = getClass().getSimpleName() + ".includeSetupCommandFile";
    if ( includeCommandFile == null ) {
        return;
    }
    BufferedReader in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( includeCommandFile )) );
    out.println ( "#----------------" );
    out.println ( "# The following " + label + " commands were imported from:" );
    out.println ( "#   " + includeCommandFile );
    String line;
    while ( true ) {
        line = in.readLine();
        if ( line == null ) {
            break;
        }
        //Message.printStatus ( 2, routine, "Importing command: " + line );
        out.println ( line );
    }
    out.println ( "#----------------" );
    in.close();
}

// Use the base class parse().

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int commandNumber )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warningLevel = 2;
	String commandTag = "" + commandNumber;
	int warningCount = 0;

	PropList parameters = getCommandParameters();

    CommandProcessor processor = getCommandProcessor();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
	CommandStatus status = getCommandStatus();
    Boolean clearStatus = Boolean.TRUE; // Default.
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen.
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}

	String SearchFolder = parameters.getValue ( "SearchFolder" ); // Expanded below.
    String OutputFile = parameters.getValue ( "OutputFile" ); // Expanded below.
    String SetupCommandFile = parameters.getValue ( "SetupCommandFile" ); // Expanded below.
    String TestResultsFile = parameters.getValue ( "TestResultsFile" ); // Expanded below.
    String EndCommandFile = parameters.getValue ( "EndCommandFile" ); // Expanded below.
	String FilenamePattern = parameters.getValue ( "FilenamePattern" );
	String [] FilenamePattern_Java = new String[0];
	if ( FilenamePattern == null ) {
		// The default patterns are:
		//     "Test_*.StateDMI" where the . is literal, and want ignore case
		//     "test-*.statedmi" where the . is literal, and want ignore case
		// For Java string matching, need to replace * with .* and . with \...
		// The \\x2e means literal period, so as to not be confused with regex period.
		FilenamePattern_Java = new String[2];
		FilenamePattern_Java[0] = "^[tT][Ee][Ss][Tt]_.*\\x2e[Ss][Tt][Aa][Tt][Ee][Dd][Mm][Ii]";
		FilenamePattern_Java[1] = "^[tT][Ee][Ss][Tt]-.*\\x2e[Ss][Tt][Aa][Tt][Ee][Dd][Mm][Ii]";
	}
	else {
		String [] parts = FilenamePattern.split(",");
		FilenamePattern_Java = new String[parts.length];
		for ( int i = 0; i < parts.length; i++ ) {
			FilenamePattern_Java[i] = StringUtil.replaceString(parts[i].trim(),"*",".*");
		}
	}
	String Append = parameters.getValue ( "Append" );
	boolean append = true; // Default.
	if ( (Append != null) && Append.equalsIgnoreCase(_False)){
		append = false;
	}
	String IncludeTestSuite = parameters.getValue ( "IncludeTestSuite" );
	if ( (IncludeTestSuite == null) || IncludeTestSuite.equals("") ) {
	    IncludeTestSuite = "*"; // Default - include all test suites.
	}
	String ExcludeTestSuite = parameters.getValue ( "ExcludeTestSuite" );
	String IncludeOS = parameters.getValue ( "IncludeOS" );
    if ( (IncludeOS == null) || IncludeOS.equals("") ) {
        IncludeOS = "*"; // Default - include all OS.
    }
	String UseOrder = parameters.getValue ( "UseOrder" );
	boolean useOrder = true; // Default.
	if ( (UseOrder != null) && UseOrder.equalsIgnoreCase(_False)){
		useOrder = false;
	}
    String TestResultsTableID = parameters.getValue ( "TestResultsTableID" );
    // Get Java regular expression pattern to match.
    String IncludeTestSuitePattern = StringUtil.replaceString(IncludeTestSuite,"*",".*");
    String ExcludeTestSuitePattern = StringUtil.replaceString(ExcludeTestSuite,"*",".*");
    String IncludeOSPattern = StringUtil.replaceString(IncludeOS,"*",".*");

    // Break the SearchFolder parameter into parts.
    String [] searchFolderArray = StringUtil.toArray(StringUtil.breakStringList(SearchFolder,",",StringUtil.DELIM_DEFAULT),true);
	String [] SearchFolder_full = new String[searchFolderArray.length];
   	for ( int i = 0; i < searchFolderArray.length; i++ ) {
   		SearchFolder_full[i] = IOUtil.verifyPathForOS(
			IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,searchFolderArray[i])));
   		if ( !IOUtil.fileExists(SearchFolder_full[i]) ) {
			message = "The folder to search \"" + SearchFolder_full[i] + "\" does not exist.";
			Message.printWarning ( warningLevel,
			MessageUtil.formatMessageTag(commandTag,++warningCount), routine, message );
			status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
		 		"Verify that the folder exists at the time the command is run."));
	  	}
   	}
	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
	String SetupCommandFile_full = null;
	if ( (SetupCommandFile != null) && !SetupCommandFile.equals("") ) {
	    SetupCommandFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,SetupCommandFile)));
        if ( !IOUtil.fileExists(SetupCommandFile_full) ) {
            message = "The setup command file \"" + SetupCommandFile_full + "\" does not exist.";
            Message.printWarning ( warningLevel,
                MessageUtil.formatMessageTag(commandTag,++warningCount), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the file exists at the time the command is run."));
        }
	}
	// Do not expand to full because the path relative to the working directory because the file
	// is relative to the output command file, not current working files:
	// - just expand for property
	if ( (TestResultsFile != null) && !TestResultsFile.equals("") ) {
	    TestResultsFile = IOUtil.verifyPathForOS(
            TSCommandProcessorUtil.expandParameterValue(processor,this,TestResultsFile));
	}
	String EndCommandFile_full = null;
	if ( (EndCommandFile != null) && !EndCommandFile.equals("") ) {
	    EndCommandFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,EndCommandFile)));
        if ( !IOUtil.fileExists(EndCommandFile_full) ) {
            message = "The end command file \"" + EndCommandFile_full + "\" does not exist.";
            Message.printWarning ( warningLevel,
                MessageUtil.formatMessageTag(commandTag,++warningCount), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the file exists at the time the command is run."));
        }
	}

	if ( warningCount > 0 ) {
		message = "There were " + warningCount + " warnings about command parameters.";
		Message.printWarning ( warningLevel,
		MessageUtil.formatMessageTag(commandTag, ++warningCount),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	try {
	    // Get the list of files to run as test cases:
		// - multiple folders can be searched
		List<String> files = new ArrayList<>();
		List<CommandFile> commandFiles = new ArrayList<>();
        String [] includedTestSuitePatterns = StringUtil.toArray(StringUtil.breakStringList(IncludeTestSuitePattern,",",StringUtil.DELIM_DEFAULT),true);
        String [] excludedTestSuitePatterns = StringUtil.toArray(StringUtil.breakStringList(ExcludeTestSuitePattern,",",StringUtil.DELIM_DEFAULT),true);
        String [] includedOSPatterns = StringUtil.toArray(StringUtil.breakStringList(IncludeOSPattern,",",StringUtil.DELIM_DEFAULT),true);
        for ( String searchFolder : SearchFolder_full ) {
        	List<String> files0 = new ArrayList<>();
        	getMatchingFilenamesInTree ( files0, new File(searchFolder), FilenamePattern_Java,
            	includedTestSuitePatterns, excludedTestSuitePatterns, includedOSPatterns );
        	Message.printStatus(2, routine, "Found " + files.size() + " command files matching search criteria for folder: " + searchFolder);
        	// Sort the list because it may not be sorted, due to dates on files:
        	// - only sort within the search folder, not overall
        	// - some reordering may occur if @order annotations are present
        	files0 = StringUtil.sortStringList(files0);
        	files.addAll(files0);
        }
       	Message.printStatus(2, routine, "Found " + files.size() + " command files matching all search folders and criteria.");
        // Transfer the filenames into CommandFile objects for further processing.
        int expectedStatusCount = 0;
        int idCount = 0;
        int orderCount = 0;
        int testSuiteCount = 0;
        for ( String file : files ) {
        	// Create a new CommandFile instance using the absolute filename.
        	CommandFile commandFile = new CommandFile(file, true);
        	commandFiles.add(commandFile);
        	// Indicate whether any additional ordering needs to be done.
        	if ( !commandFile.getId().isEmpty() ) {
        		++idCount;
        	}
        	if ( !commandFile.getOrderId().isEmpty() ) {
        		++orderCount;
        	}
        	if ( !commandFile.getExpectedStatusString().isEmpty() ) {
        		++expectedStatusCount;
        	}
        	if ( !commandFile.getTestSuite().isEmpty() ) {
        		++testSuiteCount;
        	}
        }
        // Sort the command files based on 'order' annotation:
        // - only do if 'order' was detected above since is a slight performance hit
        if ( useOrder && (orderCount > 0) ) {
        	warningCount += sortBasedOnOrder(commandFiles, status, warningLevel, commandTag);
        }
		// Open the output file.
		PrintWriter out = new PrintWriter(new FileOutputStream(OutputFile_full, append));
		File OutputFile_full_File = new File(OutputFile_full);
		// Write a standard header to the file so that it is clear when the file was created.
		IOUtil.printCreatorHeader(out, "#", 120, 0 );
		// Include the setup command file if requested.
		//Message.printStatus ( 2, routine, "Adding commands from setup command file \"" + SetupCommandFile_full + "\"");
		includeCommandFile ( out, SetupCommandFile_full, "setup" );
		// Include the matching test cases
		out.println ( "#" );
		out.println ( "# The following " + commandFiles.size() + " test cases will be run to compare results with expected results.");
		out.println ( "# Individual log files are generally created for each test.");
		if ( IncludeTestSuite.equals("") ) {
		    out.println ( "# All test cases will be included.");
		}
		else {
		    out.println ( "# The following test suites from @testSuite comments are included: " + IncludeTestSuite );
		}
        if ( IncludeOS.equals("") ) {
            out.println ( "# Test cases for all operating systems will be included.");
        }
        else {
            out.println ( "# Test cases for @os comments are included: " + IncludeOS );
        }
        out.println ( "# Number of tests with 'expectedStatus' defined: " + expectedStatusCount );
        out.println ( "# Number of tests with 'id' defined: " + idCount );
        out.println ( "# Number of tests with 'order' defined: " + orderCount );
        out.println ( "# Number of tests with 'testSuite' defined: " + testSuiteCount );
        // FIXME SAM 2007-11-20 Disable this for now because it might interfere with the
        // individual logs for each command file regression test.
        String tableParam = "";
        if ( (TestResultsTableID != null) && !TestResultsTableID.isEmpty() ) {
        	tableParam = ",TestResultsTableID=\"" + TestResultsTableID + "\"";
        }
       	// Default to output filename that is the command file name appended with ".out.txt".
        String outputFile = OutputFile_full_File.getName() + ".out.txt";
        if ( (TestResultsFile != null) && !TestResultsFile.isEmpty() ) {
        	outputFile = TestResultsFile;
        }
		out.println ( "StartRegressionTestResultsReport(OutputFile=\"" + outputFile + "\"" + tableParam + ")");
		// Find the list of matching files.
		String commandFileToRun;
		for ( CommandFile commandFile: commandFiles ) {
			// The command files to run are relative to the commands file being created.
			commandFileToRun = IOUtil.toRelativePath ( OutputFile_full_File.getParent(), commandFile.getFilename() );
			// Determine if the command file has @expectedStatus in it.
			// If so, define an ExpectedStatus parameter for the command.
			out.println ( "RunCommands(InputFile=\"" + commandFileToRun + "\"" +
		        determineExpectedStatusParameter(commandFile) + ")");
		}
		// Include the end command file if requested.
		//Message.printStatus ( 2, routine, "Adding commands from end command file \"" + EndCommandFile_full + "\"");
		includeCommandFile ( out, EndCommandFile_full, "end" );
		out.close();
        // Save the output file name.
        setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Unexpected error creating regression command file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warningLevel,
	        MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "See the log file for details."));
		throw new CommandException ( message );
	}

    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file ) {
    __OutputFile_File = file;
}

/**
 * Sort the tests based on the '@order' annotation.
 * Search command files for '@order' annotation and if corresponding @id is found,
 * reorder the test.
 * @param commandFiles list of command files to process
 * @param warningCount number of warnings at start of call
 * @return warningCount number of warnings due to sorting
 */
private int sortBasedOnOrder(List<CommandFile> commandFiles, CommandStatus status, int warningLevel, String commandTag ) {
	String routine = getClass().getSimpleName() + ".sortBasedOnOrder";
	String orderId = null;
	CommandFileOrderType orderOperator = null;
	int warningCount = 0;
	// Loop indefinitely because must modify loop contents outside of loop or else have concurrency issue.
	int startingIndex = 0; // Starting index to process, needed to ensure progress occurs even if errors.
	CommandFile foundCommandFile = null;
	CommandFile commandFile = null;
	CommandFile commandFile2 = null; // Used for iteration.
	int iCommandFile = 0; // Index for command file matching Id.
	int iFoundCommandFile = 0; // Index for command file matching Id.
	int loopCount = 0;
	boolean needToProcessOrder = false; // Used to indicate that 'order' needs to be processed.
	while ( true ) {
		Message.printStatus(2, routine, "Processing tests for 'order' starting at index " +
			startingIndex + ", max index = " + (commandFiles.size() - 1) );
		++loopCount;
		if ( loopCount >= commandFiles.size()) {
			String message = "Checking @order has logic problem - reached maximum number of tests (" +
				commandFiles.size() + " without finishing reordering.";
			Message.printWarning ( warningLevel,
				MessageUtil.formatMessageTag(commandTag,++warningCount), routine, message );
				status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
				"Check code logic.  Contact support."));
			break;
		}
		foundCommandFile = null;
		needToProcessOrder = false;
		for ( iCommandFile = startingIndex; iCommandFile < commandFiles.size(); iCommandFile++ ) {
			commandFile = commandFiles.get(iCommandFile);
			orderOperator = commandFile.getOrderOperatorType(); // If not null will be valid due to parsing.
			orderId = commandFile.getOrderId(); // Could still be null or empty if not properly specified.
			if ( (orderOperator != null) && (orderId != null) && !orderId.isEmpty() ) {
				// Find a command file with matching 'Id', need to search all tests.
				// - matched Id will cause 'foundCommandFile' and 'ifoundCommandFile' to be set for use later
				needToProcessOrder = true;
				for ( iFoundCommandFile = 0; iFoundCommandFile < commandFiles.size(); iFoundCommandFile++ ) {
					commandFile2 = commandFiles.get(iFoundCommandFile);
					// Matched command file cannot be itself.
					if ( (iCommandFile != iFoundCommandFile) && (commandFile2.getId() != null) &&
						(commandFile2.getId().equalsIgnoreCase(orderId))) {
						foundCommandFile = commandFile2;
						break;
					}
				}
				if ( foundCommandFile != null ) {
					// Break out of the loop with non-null object so move can occur.
					// The next search index will depend on how the reorder occurred.
					break;
				}
				else {
					// Could not find the Id of interest.  Add a warning so 'Id' can be corrected.
					String message = "The @order command file identifier \"" + orderId + "\" was not found in other command files.";
					Message.printWarning ( warningLevel,
						MessageUtil.formatMessageTag(commandTag,++warningCount), routine, message );
						status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
						"Verify that '@order' with identifier \"" + orderId + "\" exists."));
					// Next search needs to start after current index to avoid the same error again.
					startingIndex = iCommandFile + 1;
					break;
				}
			}
		}
		if ( !needToProcessOrder ) {
			// No 'order' needs to be processed so can exit outer loop.
			break;
		}
		else if ( foundCommandFile != null ) {
			// Reorder the command.
			Message.printStatus(2, routine, "Reordering command from index " + iCommandFile + " to " + orderOperator +
				" " + orderId + " (index " + iFoundCommandFile + ")");
			if ( iFoundCommandFile < iCommandFile ) {
				if ( orderOperator == CommandFileOrderType.BEFORE ) {
					if ( iFoundCommandFile != (iCommandFile - 1) ) {
						// Required position is not already in place.
						// Move the current command to before the found command.
						commandFiles.add(iFoundCommandFile, commandFile);
						Message.printStatus(2, routine, "  Adding command from old index " + iCommandFile + " to before found index " + iFoundCommandFile );
						// Original position will be shifted by one.
						commandFiles.remove(iCommandFile + 1);
						Message.printStatus(2, routine, "  Removing command at old index " + (iCommandFile + 1) );
					}
				}
				else if ( orderOperator == CommandFileOrderType.AFTER ) {
					if ( iFoundCommandFile != (iCommandFile + 1) ) {
						// Required position is not already in place.
						// Move the current command to after the found command.
						commandFiles.add((iFoundCommandFile + 1), commandFile);
						Message.printStatus(2, routine, "  Adding command from old index " + iCommandFile + " to after found index " + (iFoundCommandFile + 1) );
						// Original position will be shifted by one.
						commandFiles.remove(iCommandFile + 1);
						Message.printStatus(2, routine, "  Removing command at old index " + (iCommandFile + 1) );
					}
				}
				// Starting index will be 'iFoundCommand' + 1 due to insert + 1 to process next,
				// regardless of whether moved to before or after.
				startingIndex = iFoundCommandFile + 2;
			}
			else if ( iFoundCommandFile > iCommandFile ) {
				if ( orderOperator == CommandFileOrderType.BEFORE ) {
					if ( iFoundCommandFile != (iCommandFile - 1) ) {
						// Required position is not already in place.
						// Move the current command to before the found command.
						commandFiles.add(iFoundCommandFile, commandFile);
						Message.printStatus(2, routine, "  Adding command from old index " + iCommandFile + " to before found index " + iFoundCommandFile );
						// Original position will be the same.
						commandFiles.remove(iCommandFile);
						Message.printStatus(2, routine, "  Removing command at old index " + iCommandFile );
					}
				}
				else if ( orderOperator == CommandFileOrderType.AFTER ) {
					if ( iFoundCommandFile != (iCommandFile + 1) ) {
						// Required position is not already in place.
						// Move the current command to after the found command.
						commandFiles.add((iFoundCommandFile + 1), commandFile);
						Message.printStatus(2, routine, "  Adding command from old index " + iCommandFile + " to after found index " + (iFoundCommandFile + 1) );
						// Original position will be the same.
						commandFiles.remove(iCommandFile);
						Message.printStatus(2, routine, "  Removing command at old index " + iCommandFile );
					}
				}
				// Starting index will be current command since current command is shifted later.
				// No need to change the value of 'startingIndex'.
			}
		}
		else {
			// Count find the command matching 'order' - warnings were handled above.
		}
		if ( startingIndex > commandFiles.size() ) {
			// Next starting index to process is after the last item so done processing:
			// - this should not be needed but do to avoid infinite loop in 'while'
			break;
		}
	}
	return warningCount;
}

/**
Return the string representation of the command.
@param parameters to include in the command
@return the string representation of the command
*/
public String toString ( PropList parameters ) {
	String [] parameterOrder = {
		"SearchFolder",
		"OutputFile",
		"SetupCommandFile",
		"TestResultsFile",
		"EndCommandFile",
		"FilenamePattern",
		"Append",
		"IncludeTestSuite",
		"ExcludeTestSuite",
		"IncludeOS",
		"UseOrder",
		"TestResultsTableID"
	};
	return this.toString(parameters, parameterOrder);
}

}