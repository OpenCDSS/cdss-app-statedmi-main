package rti.tscommandprocessor.commands.check;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProfile;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the WriteCheckFile() command.  This command can be run from any CommandProcessor
because it depends only on command classes.
*/
public class WriteCheckFile_Command extends AbstractCommand implements Command, FileGenerator
{
	
/** 
Values for use with WriteHeaderComments parameter.
*/
protected final String _False = "False";
protected final String _True = "True";
    
/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
    
/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public WriteCheckFile_Command ()
{   super();
    setCommandName ( "WriteCheckFile" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{   String OutputFile = parameters.getValue ( "OutputFile" );
	String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
    String warning = "";
    String message;
    
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify an output file." ) );
    }
    else if ( OutputFile.indexOf("${") < 0 ){
        String working_dir = null;      
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
            // Working directory is available so use it...
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Software error - report to support." ) );
        }
    
        try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The output file parent directory does not exist: \"" + f2 + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
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
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that output file and working directory paths are compatible." ) );
        }
        
        // The output file must end in ".html" or ".csv" to indicate recognized formats
        
        if ( !StringUtil.endsWithIgnoreCase(OutputFile,".html") &&
            !StringUtil.endsWithIgnoreCase(OutputFile,".csv") ) {
            message = "The output file (" + OutputFile + ") has an invalid extension (not html or csv).";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the output file with an extension of html or csv." ) );
        }
    }
    
    if ( (WriteHeaderComments != null) && !WriteHeaderComments.equals("") ) {
        if ( !WriteHeaderComments.equalsIgnoreCase(_False) && !WriteHeaderComments.equalsIgnoreCase(_True) ) {
            message = "The WriteHeaderComments parameter (" + WriteHeaderComments + ") must be " + _False +
            " or " + _True + ".";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the parameter as " + _False + " or " + _True + "."));
        }
    }

    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(3);
    validList.add ( "OutputFile" );
    validList.add ( "Title" );
	validList.add ( "WriteHeaderComments" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

    // Throw an InvalidCommandParameterException in case of errors.
    if ( warning.length() > 0 ) {       
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag( command_tag, warning_level ), warning );
        throw new InvalidCommandParameterException ( warning );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{   // The command will be modified if changed...
    return (new WriteCheckFile_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List getGeneratedFileList ()
{
    List list = new Vector();
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
    }
    return list;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
protected File getOutputFile ()
{
    return __OutputFile_File;
}

/**
Writes the start tags for the HTML check file.
@param html HTMLWriter object.
@param title title for the document.
@throws Exception
*/
private void htmlHead( HTMLWriter html, String title ) throws Exception
{
    if ( html != null ) {
        html.headStart();
        html.title(title);
        htmlWriteStyles(html);
        html.headEnd();
    }
}

/**
Helper method to write the header section of the check file.
@param html HTMLWriter object.
@throws Exception
 */
private void htmlWriteCheckFileIntro( HTMLWriter html, String userTitle, CommandProcessor processor,
	List<String> notes )
throws Exception
{
    // proplist provides an anchor link for this section used from the table of contents
    //PropList header_prop = new PropList("header");
    //header_prop.add("name=header");
    
    html.heading(1, IOUtil.getProgramName() + " Check Results" );
    
    html.heading(2, userTitle );
    
    // Table of contents using same heading and section names as main content...
    
    html.heading(2, "Check File Table of Contents" );
    
    String environmentHeading = "Program/Environment Information";
    String environmentLink = "environment";
    String notesHeading = "Command File Comments";
    String notesLink = "notes";
    String summaryHeading = "Command Problem Summary";
    String summaryLink = "summary";
    String detailsHeading = "Command Details";
    String detailsLink = "commands";
    
    html.link("#"+environmentLink, environmentHeading );
    html.write ( " - generated at runtime");
    html.breakLine();
    html.link("#"+notesLink, notesHeading );
    html.write ( " - as written to output file headers");
    html.breakLine();
    html.link("#"+summaryLink, summaryHeading );
    html.write ( " - command run times and warning/failure counts");
    html.breakLine();
    html.link("#"+detailsLink, detailsHeading );
    html.write ( " - command problem summary");
    html.breakLine();
    
    // Environment section of the report...
    
    html.heading(2, environmentHeading, environmentLink );
    
    DateTime now = new DateTime(DateTime.DATE_CURRENT);
    Object o = processor.getPropContents("CommandFileName");
    String commandFileName = "Unknown";
    if ( o != null ) {
        commandFileName = "" + o;
    }
    String [] tableHeaders = { "Property", "Value" };
    html.paragraphStart();
    html.tableStart();
    html.tableRowStart();
    html.tableHeaders( tableHeaders );
    html.tableRowEnd();
    String [] tds = new String[2];
    tds[0] = "Command file";
    tds[1] = commandFileName;
    html.tableRow( tds );
    tds[0] = "Program";
    tds[1] = IOUtil.getProgramName() + " " + IOUtil.getProgramVersion();
    html.tableRow( tds );
    tds[0] = "User";
    tds[1] = IOUtil.getProgramUser();
    html.tableRow( tds );
    tds[0] = "Creation time";
    tds[1] = "" + now;
    html.tableRow( tds );
    tds[0] = "Computer";
    tds[1] = IOUtil.getProgramHost();
    html.tableRow( tds );
    tds[0] = "Processor initial working directory";
    tds[1] = "" + processor.getPropContents("InitialWorkingDir");
    html.tableRow( tds );
    StringBuffer b = new StringBuffer();
    b.append( IOUtil.getProgramName() );
    String [] args = IOUtil.getProgramArguments();
    for ( int i = 0; i < args.length; i++ ) {
        b.append ( " " + args[i] );
    }
    tds[0] = "Command line";
    tds[1] = b.toString();
    html.tableRow( tds );
    html.tableEnd();
    html.paragraphEnd();
    
    // Notes
    html.heading(2, notesHeading, notesLink );
    html.preStart();
    for ( String note : notes ) {
    	html.write(note + "\n");
    }
    html.preEnd();

    html.heading(2, summaryHeading, summaryLink );
    htmlWriteProblemCounts ( html );
    htmlWriteCommands( html, processor.getCommands(), true ); // false to write text, true for table
}

/**
Writes the commands from the processor in HTML using the <code> tag, with embedded anchors for each command so that
other content can link to it.  This will force the output to match the command files text exactly.
@param html HTMLWriter object.
@param writeTable indicates whether to write the commands as simple text or a table.
@throws Exception
*/
private void htmlWriteCommands( HTMLWriter html, List<Command> commands, boolean writeTable )
throws Exception
{
    String [] tableHeaders = { "#", "Time, sec.", "Warnings", "Failures", "Command" };
    html.paragraphStart();
    int size = commands.size();
    long totalTime = 0;
    int totalWarnings = 0;
    int totalFailures = 0;
    CommandProfile commandProfile;
    if ( writeTable ) {
        html.tableStart();
        html.tableRowStart();
        html.tableHeaders( tableHeaders );
        html.tableRowEnd();
        // loop through the data
        String [] td1 = new String[1]; // Cell data for single column
        PropList severityAttributes = null;
        PropList warningAttributes = new PropList("warning");
        warningAttributes.set( "class", CommandStatusType.WARNING.toString().toLowerCase());
        PropList failureAttributes = new PropList("failure");
        failureAttributes.set( "class", CommandStatusType.FAILURE.toString().toLowerCase());
        int commandsSize = commands.size();
        Command command;
        int countWarnings, countFailures;
        for ( int iCommand = 0; iCommand < commandsSize; iCommand++ ) {
            command = commands.get(iCommand);
            commandProfile = command.getCommandProfile(CommandPhaseType.RUN);
            html.tableRowStart();
            // Row (command) number...
            td1[0] = "" + (iCommand + 1);
            html.tableCells( td1, null );
            // Run time
            td1[0] = "" + StringUtil.formatString(((double)commandProfile.getRunTime())/1000.0,"%.3f");
            html.tableCells( td1, null );
            totalTime += commandProfile.getRunTime();
            // Number of warnings
            countWarnings = CommandStatusUtil.getSeverityCount ( command, CommandStatusType.WARNING, false );
            totalWarnings += countWarnings;
            td1[0] = "" + countWarnings;
            severityAttributes = null;
            if ( countWarnings > 0 ) {
                severityAttributes = warningAttributes;
            }
            html.tableCells( td1, severityAttributes );
            // Number of failures
            countFailures = CommandStatusUtil.getSeverityCount ( command, CommandStatusType.FAILURE, false );
            totalFailures += countFailures;
            td1[0] = "" + countFailures;
            severityAttributes = null;
            if ( countFailures > 0 ) {
                severityAttributes = failureAttributes;
            }
            html.tableCells( td1, severityAttributes );
            // Command, with anchor
            // Format the link manually because HTMLWriter does not have anything that will handle it
            html.tableCellStart();
            td1[0] = "<a name=\"c" + iCommand + "\">" + command + "</a>";
            html.write(td1[0]);
            html.tableCellEnd();
            html.tableRowEnd();
        }
        // Add the totals...
        td1[0] = ""; // Don't show as "total" because want to keep all cells in column as number
        html.tableCells( td1, null );
        td1[0] = "" + StringUtil.formatString(((double)totalTime)/1000.0,"%.3f");
        html.tableCells( td1, null );
        td1[0] = "" + totalWarnings;
        severityAttributes = null;
        if ( totalWarnings > 0 ) {
            severityAttributes = warningAttributes;
        }
        html.tableCells( td1, severityAttributes );
        td1[0] = "" + totalFailures;
        severityAttributes = null;
        if ( totalFailures > 0 ) {
            severityAttributes = failureAttributes;
        }
        html.tableCells( td1, severityAttributes );
        td1[0] = "";
        html.tableCells( td1, null );
        // End the table
        html.tableEnd();
    }
    else {
        boolean doPre = true; // If using "pre", the lines don't wrap, but have to embed link in hidden text
        for ( int i = 0; i < size; i++ ) {
            if ( doPre ) {
                // Wrap each command in "pre" to prevent wrapping
                html.anchorStart("c" + i );
                html.write(" ");
                html.anchorEnd();
                html.pre("" + commands.get(i));
            }
            else {
                // TODO SAM 2009-04-22 Evaluate use - use "code" and line breaks - problem is that commands wrap - keep code
                html.anchorStart("c" + i );
                html.code("" + commands.get(i));
                html.anchorEnd();
                html.breakLine();
            }
        }
        html.paragraphEnd();
    }
}

/**
Writes the log records to the check file.
@param html HTMLWriter object.
@param logRecordList list of log records to output.
@throws Exception
 */
private void htmlWriteCommandLogRecords( HTMLWriter html, List<CommandLogRecord> logRecordList,
    String [] tableHeaders ) throws Exception
{
    // Get the data from the model
    // proplist provides an anchor link for this section used
    // from the table of contents
    //PropList data_prop = new PropList( "Data " + index );
    //data_prop.add( "name=data" + index );
    // write the more component specific data
    
    String heading = "Command Problem Details";
    html.heading(2, heading, "commands" );
    htmlWriteProblemCounts ( html );
    if ( logRecordList.size() > 0 ) {
        // table start
        html.tableStart();
        html.tableRowStart();
        html.tableHeaders( tableHeaders );
        html.tableRowEnd();
        // loop through the data
        String [] td1 = new String[1]; // Cell data for single column
        //String [] tds = new String[tableHeaders.length - 1]; // Omit the severity since it is written separately
        String severity;
        PropList severityAttributes = new PropList("severity");
        int logRecCount = 0;
        for ( CommandLogRecord logRecord: logRecordList ) {
            html.tableRowStart();
            // Row (problem) number...
            ++logRecCount;
            td1[0] = "" + logRecCount;
            html.tableCells( td1, null );
            // Severity
            severity = logRecord.getSeverity().toString();
            td1[0] = severity;
            severityAttributes.set( "class", severity.toLowerCase());
            html.tableCells( td1, severityAttributes );
            // Write the severity cell separately since it has a class corresponding to the severity
            // Now write the remainder of the cells.  Make sure that the string has at lease a space because
            // empty cells can be formatted badly.
            // Problem type
            td1[0] = CommandStatusUtil.getCommandLogRecordDisplayName ( logRecord );
            html.tableCells( td1, null );
            // Command, with links to the command file
            CommandStatusProvider csp = logRecord.getCommandStatusProvider();
            String command = "";
            if ( csp != null ) {
                command = csp.toString();
            }
            // Format the link manually because HTMLWriter does not have anything that will handle it
            html.tableCellStart();
            td1[0] = "<a href=\"#c" + TSCommandProcessorUtil.indexOf(getCommandProcessor(),(Command)csp,0) +
                "\">" + command + "</a>";
            html.write(td1[0]);
            html.tableCellEnd();
            // Problem
            td1[0] = logRecord.getProblem();
            if ( td1[0].equals("") ) {
                td1[0] = " ";
            }
            html.tableCells( td1, null );
            // Recommendation
            td1[0] = logRecord.getRecommendation();
            if ( td1[0].equals("") ) {
                td1[0] = " ";
            }
            html.tableCells( td1, null );
            html.tableRowEnd();
        }
        html.tableEnd();
    }
}

/**
Write the command problem counts.
*/
private void htmlWriteProblemCounts ( HTMLWriter html )
throws Exception
{
	html.paragraphStart();
	html.write("Total number of failures: " +
	    CommandStatusUtil.getSeverityCount ( getCommandProcessor().getCommands(), CommandStatusType.FAILURE, false ));
	html.breakLine();
	html.write("Total number of commands with failures: " +
	    CommandStatusUtil.getSeverityCount ( getCommandProcessor().getCommands(), CommandStatusType.FAILURE, true ));
	html.breakLine();
	html.write("Total number of warnings: " +
	    CommandStatusUtil.getSeverityCount ( getCommandProcessor().getCommands(), CommandStatusType.WARNING, false ));
	html.breakLine();
	html.write("Total number of commands with warnings: " +
	    CommandStatusUtil.getSeverityCount ( getCommandProcessor().getCommands(), CommandStatusType.WARNING, true ));
	html.breakLine();
	html.paragraphEnd( );
}

/**
Inserts the style attributes for a check file.
@throws Exception
 */
private void htmlWriteStyles(HTMLWriter html)
throws Exception
{
    html.write("<style>\n"
        + "#titles { font-weight:bold; color:#303044 }\n"
        + "table { background-color:black; text-align:left; border:1; bordercolor:black; cellspacing:1; cellpadding:1 }\n"  
        + "th { background-color:#333366; text-align:center; vertical-align:bottom; color:white }\n"
        + "tr { valign:bottom }\n"
        + "td { background-color:white; text-align:left; vertical-align:bottom; }\n" 
        + "body { text-align:left; font-size:12pt; }\n"
        + "pre { font-size:12pt; margin: 0px }\n"
        + "p { font-size:12pt; }\n"
        + "/* The following controls formatting of severity column in tables */\n"
        + ".warning { background-color:yellow; }\n"
        + ".failure { background-color:red; }\n"
        + ".success { background-color:green; }\n"
        + ".unknown { background-color:white; }\n"
        + "</style>\n");
}

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getSimpleName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    // Check whether the processor wants output files to be created...

    CommandProcessor processor = getCommandProcessor();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    if ( !TSCommandProcessorUtil.getCreateOutput(processor) ) {
        Message.printStatus ( 2, routine, "Skipping \"" + toString() + "\" because output is not being created." );
        return;
    }
    
    CommandStatus status = getCommandStatus();
    Boolean clearStatus = new Boolean(true); // default
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}
    
    PropList parameters = getCommandParameters();
    String OutputFile = parameters.getValue ( "OutputFile" ); // Expanded below
    String Title = parameters.getValue ( "Title" );
	if ( (commandPhase == CommandPhaseType.RUN) && (Title != null) && (Title.indexOf("${") >= 0) ) {
		Title = TSCommandProcessorUtil.expandParameterValue(processor, this, Title);
	}
    String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );
    boolean writeHeaderComments = true;
    if ( (WriteHeaderComments != null) && WriteHeaderComments.equalsIgnoreCase(_False) ) {
    	writeHeaderComments = false;
    }

    String OutputFile_full = OutputFile;
    try {
        // Get the comments to add to the top of the file.

        List OutputComments_List = new ArrayList<String>();
        if ( writeHeaderComments ) {
	        try {
	            Object o = processor.getPropContents ( "OutputComments" );
	            // Comments are available so use them...
	            if ( o != null ) {
	                OutputComments_List = (List)o;
	            }
	        }
	        catch ( Exception e ) {
	            // Not fatal, but of use to developers.
	            message = "Error requesting OutputComments from processor (" + e + ") - not using.";
	            Message.printWarning(3, routine, message );
	            Message.printWarning(3, routine, e );
	        }
        }
        
        // Clear the filename for the FileGenerator interface
        setOutputFile ( null );
        OutputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            	TSCommandProcessorUtil.expandParameterValue(processor, this,OutputFile)) );
        
        // Get the log records...
        
        List<CommandLogRecord> logRecordList =
            CommandStatusUtil.getLogRecordListFromCommands ( processor.getCommands(), CommandPhaseType.RUN );
 
        // Write the output file.
        
        if ( StringUtil.endsWithIgnoreCase(OutputFile,"csv") ) {
            writeListFile(OutputFile_full, ",", logRecordList, writeHeaderComments, OutputComments_List );
        }
        else if ( StringUtil.endsWithIgnoreCase(OutputFile,"html") ) {
            writeHtmlFile(IOUtil.getProgramName(), processor, OutputFile_full, ",", logRecordList,
            	OutputComments_List, Title );
        }

        // Set the filename for the FileGenerator interface
        setOutputFile ( new File(OutputFile_full) );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error writing file \"" + OutputFile_full + "\" (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
protected void setOutputFile ( File file )
{
    __OutputFile_File = file;
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{   
    if ( parameters == null ) {
        return getCommandName() + "()";
    }

    String OutputFile = parameters.getValue ( "OutputFile" );
    String Title = parameters.getValue ( "Title" );
    String WriteHeaderComments = parameters.getValue ( "WriteHeaderComments" );

    StringBuffer b = new StringBuffer ();
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        b.append ( "OutputFile=\"" + OutputFile + "\"" );
    }
    if ( (Title != null) && (Title.length() > 0) ) {
    	if ( b.length() > 0 ) {
    		b.append ( "," );
    	}
        b.append ( "Title=\"" + Title + "\"" );
    }
    if ( (WriteHeaderComments != null) && (WriteHeaderComments.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "WriteHeaderComments=" + WriteHeaderComments );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

/**
Writes a list of CommandLogRecord objects to an HTML file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".
@param appName application name that is writing the check file.
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param data the list of objects to write.  
@param newComments list of comments to add at the top of the file.
@param userTitle the output file title requested by the user
@throws Exception if an error occurs.
*/
public void writeHtmlFile( String appName, CommandProcessor processor, String filename, String delimiter,
    List<CommandLogRecord> data, List<String> newComments, String userTitle ) 
throws Exception {
    // Put together high-level properties for the report.
    
    // Column headings for log record table
    String[] names = {
        "#",
        "Severity",
        "Type",
        "Command",
        "Problem",
        "Recommendation" };
    
    List<String> commentIndicators = new Vector<String>(1);
    commentIndicators.add ( "#" );
    List<String> ignoredCommentIndicators = new Vector<String>(1);
    ignoredCommentIndicators.add ( "#>");
    
    // TODO SAM Need to get normal comment header info and fold into HTML file in nice format
    
    // Format some basic comments at the top of the file.  Do this to a copy of the
    // incoming comments so that they are not modified in the calling code.
    List<String> newComments2 = null;
    if ( newComments == null ) {
        newComments2 = new ArrayList<String>();
    }
    else {
        newComments2 = new ArrayList<String>(newComments);
    }
    newComments2.add(0,"");
    newComments2.add(1,appName + " check file containing all warning/failure messages from run.");
    newComments2.add(2,"");

    String htmlTitle = userTitle;
    if ( (userTitle == null) || (userTitle.length() == 0) ) {
    	htmlTitle = appName + " Check File (" + processor.getPropContents("CommandFileName") + ")";
    	userTitle = htmlTitle;
    }
    
    // Write the HTML file
    
    try {
        // Open the filename but do not automatically create the head section (that is done below)
        HTMLWriter html = new HTMLWriter( filename, htmlTitle, false );
        // Start the file and write the head section
        html.htmlStart();
        htmlHead(html,htmlTitle);
        // Start the body section
        html.bodyStart();
        // Write introduction information
        htmlWriteCheckFileIntro( html, userTitle, processor, newComments2 );
        // Write the main table that contains the problems
        htmlWriteCommandLogRecords( html, data, names );
        // Close the body section and file
        html.bodyEnd();
        html.htmlEnd();
        html.closeFile();
    }
    catch (Exception e) {
        throw e;
    }
    finally {
    }
}

/**
Writes a list of CommandLogRecord objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param data the list of objects to write.  
@param newComments list of comments to add at the top of the file.
@throws Exception if an error occurs.
*/
public void writeListFile(String filename, String delimiter, List<CommandLogRecord> data, boolean writeHeaderComments, List<String> newComments ) 
throws Exception {
    int size = 0;
    if (data != null) {
        size = data.size();
    }
    int fieldCount = 5;
    String[] names = {
        "Severity",
        "Type",
        "Command",
        "Problem",
        "Recommendation" };
    
    String[] formats = {
        "%s",
        "%s",
        "%s",
        "%s",
        "%s" };

    String oldFile = null;
    
    int j = 0;
    PrintWriter out = null;
    CommandLogRecord logRecord = null;
    List<String> commentIndicators = new ArrayList<String>(1);
    commentIndicators.add ( "#" );
    List<String> ignoredCommentIndicators = new ArrayList<String>(1);
    ignoredCommentIndicators.add ( "#>");
    String[] line = new String[fieldCount];
    StringBuffer buffer = new StringBuffer();
    
    try {
        // Add some basic comments at the top of the file.  Do this to a copy of the
        // incoming comments so that they are not modified in the calling code.
    	if ( writeHeaderComments ) {
	        List<String> newComments2 = null;
	        if ( newComments == null ) {
	            newComments2 = new ArrayList<String>();
	        }
	        else {
	            newComments2 = new ArrayList(newComments);
	        }
	        newComments2.add(0,"");
	        newComments2.add(1,IOUtil.getProgramName() + " check file containing all warning/failure messages from run.");
	        newComments2.add(2,"");
	        out = IOUtil.processFileHeaders( oldFile, filename, newComments2, commentIndicators, ignoredCommentIndicators, 0);
    	}
    	else {
    		// Just open the file and start writing below.
    		out = new PrintWriter(filename);
    	}

        for (int i = 0; i < fieldCount; i++) {
            if (i > 0) {
                buffer.append(delimiter);
            }
            buffer.append("\"" + names[i] + "\"");
        }

        out.println(buffer.toString());
        
        for (int i = 0; i < size; i++) {
            logRecord = (CommandLogRecord)data.get(i);
            
            line[0] = StringUtil.formatString(logRecord.getSeverity().toString(), formats[0]).trim();
            //String className = logRecord.getClass().getSimpleName();
            String type = CommandStatusUtil.getCommandLogRecordDisplayName ( logRecord );
            line[1] = StringUtil.formatString(type, formats[1]).trim();
            CommandStatusProvider csp = logRecord.getCommandStatusProvider();
            String command = "";
            if ( csp != null ) {
                command = csp.toString();
            }
            line[2] = StringUtil.formatStringForCsv(StringUtil.formatString(command, formats[2]).trim(),true);
            line[3] = StringUtil.formatStringForCsv(StringUtil.formatString(logRecord.getProblem(), formats[3]).trim(),true);
            line[4] = StringUtil.formatStringForCsv(StringUtil.formatString(logRecord.getRecommendation(), formats[3]).trim(),true);

            buffer = new StringBuffer();    
            for (j = 0; j < fieldCount; j++) {
                if ( j > 0 ) {
                    buffer.append(delimiter);
                }
                buffer.append(line[j]);
            }
            out.println(buffer.toString());
        }
    }
    catch (Exception e) {
        throw e;
    }
    finally {
        if (out != null) {
            out.flush();
            out.close();
        }
    }
}

}