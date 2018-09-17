// FIXME SAM 2008-06-25 Need to clean up exception handling and command status in runCommand().

package rti.tscommandprocessor.commands.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
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

/**
This class initializes, checks, and runs the WebGet() command.
*/
public class WebGet_Command extends AbstractCommand implements Command, FileGenerator
{
    
/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;
    
/**
Constructor.
*/
public WebGet_Command ()
{	super();
	setCommandName ( "WebGet" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String URI = parameters.getValue ( "URI" );
	String LocalFile = parameters.getValue ( "LocalFile" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	CommandProcessor processor = getCommandProcessor();
	
	// The existence of the file to remove is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (URI == null) || (URI.length() == 0) ) {
		message = "The URI (Uniform Resource Identifier) must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the URI."));
	}
	// LocalFile is not required given that output property can be specified
    if ( (LocalFile != null) && !LocalFile.isEmpty() && (LocalFile.indexOf("${") < 0) ) {
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
                TSCommandProcessorUtil.expandParameterValue(processor,this,LocalFile)));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The local file parent directory does not exist for: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Create the output directory." ) );
            }
            f = null;
            f2 = null;
        }
        catch ( Exception e ) {
            message = "The local file:\n" +
            "    \"" + LocalFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that local file and working directory paths are compatible." ) );
        }
    }

	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(4);
	validList.add ( "URI" );
	validList.add ( "LocalFile" );
	validList.add ( "OutputProperty" );
	validList.add ( "ResponseCodeProperty" );
	warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
		throw new InvalidCommandParameterException ( warning );
	}
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new WebGet_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    List<File> list = new Vector<File>();
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
    }
    return list;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
    return __OutputFile_File;
}

// Use base class parseCommand

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	int log_level = 3; // Level for non-user messages for log file.
	String command_tag = "" + command_number;
	int warning_count = 0;
	
    // Clear the output file
    
    setOutputFile ( null );
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
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
		status.clearLog(CommandPhaseType.RUN);
	}
	
	String URI = parameters.getValue ( "URI" );
	if ( URI != null ) {
	    URI = TSCommandProcessorUtil.expandParameterValue(processor,this,URI);
	    Message.printStatus(2, routine, "URI after expanding is \"" + URI + "\"");
	}
    String LocalFile = parameters.getValue ( "LocalFile" );
    boolean doOutputFile = false;
	if ( (LocalFile != null) && !LocalFile.isEmpty() ) {
		LocalFile = TSCommandProcessorUtil.expandParameterValue(processor,this,LocalFile);
		doOutputFile = true;
	}
	String LocalFile_full = LocalFile;
	if ( (LocalFile != null) && !LocalFile.isEmpty() ) {
		LocalFile_full = IOUtil.verifyPathForOS(
	        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),LocalFile) );
	}
	boolean doOutputProperty = false;
	String OutputProperty = parameters.getValue ( "OutputProperty" );
	if ( (OutputProperty != null) && !OutputProperty.isEmpty() ) {
		doOutputProperty = true;
	}
	boolean doResponseCodeProperty = false;
	String ResponseCodeProperty = parameters.getValue ( "ResponseCodeProperty" );
	if ( (ResponseCodeProperty != null) && !ResponseCodeProperty.isEmpty() ) {
		doResponseCodeProperty = true;
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	try {
	    FileOutputStream fos = null;
	    HttpURLConnection urlConnection = null;
	    InputStream is = null;
    	StringBuilder content = null;
    	if ( doOutputProperty ) {
    		content = new StringBuilder();
    	}
        try {
            // Some sites need cookie manager
            // (see http://stackoverflow.com/questions/11022934/getting-java-net-protocolexception-server-redirected-too-many-times-error)
            CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
            // Open the input stream...
            Message.printStatus(2,routine,"Reading URI \"" + URI + "\"" );
            URL url = new URL(URI);
            urlConnection = (HttpURLConnection)url.openConnection();
            is = urlConnection.getInputStream();
            BufferedInputStream isr = new BufferedInputStream(is);
            // Open the output file...
            if ( doOutputFile ) {
            	fos = new FileOutputStream( LocalFile_full );
            }
            // Output the characters to the local file...
            int numCharsRead;
            int arraySize = 8192; // 8K optimal
            byte[] byteArray = new byte[arraySize];
            int bytesRead = 0;
            while ((numCharsRead = isr.read(byteArray, 0, arraySize)) != -1) {
            	if ( doOutputFile ) {
            		fos.write(byteArray, 0, numCharsRead);
            	}
                if ( doOutputProperty ) {
                	// Also set the content in memory
                	if ( numCharsRead == byteArray.length ) {
                		content.append(new String(byteArray));
                	}
                	else {
                		byte [] byteArray2 = new byte[numCharsRead];
                		System.arraycopy(byteArray, 0, byteArray2, 0, numCharsRead);
                		content.append(new String(byteArray2));
                	}
                }
                bytesRead += numCharsRead;
            }
            // Save the output file name...
            Message.printStatus(2,routine,"Number of bytes read=" + bytesRead );
            if ( doOutputFile ) {
            	setOutputFile ( new File(LocalFile_full));
            }
            // If requested, also set as a property
            if ( doOutputProperty ) {
                PropList request_params = new PropList ( "" );
                request_params.setUsingObject ( "PropertyName", OutputProperty );
                request_params.setUsingObject ( "PropertyValue", content.toString() );
                try {
                    processor.processRequest( "SetProperty", request_params);
                }
                catch ( Exception e ) {
                    message = "Error requesting SetProperty(Property=\"" + OutputProperty + "\") from processor.";
                    Message.printWarning(log_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                    status.addToLog ( CommandPhaseType.RUN,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
                }
            }
        }
        catch (MalformedURLException e) {
            message = "URI \"" + URI + "\" is malformed (" + e + ")";
            Message.printWarning ( warning_level, 
                   MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            Message.printWarning ( 3, routine, e );
            status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details."));
        }
        catch (IOException e) {
        	StringBuilder sb = new StringBuilder("Error opening URI \"" + URI + "\" (" + e );
        	// Try reading error stream - may only work for some error numbers
            if ( urlConnection != null ) {
            	is = urlConnection.getErrorStream(); // close in finally
            	if ( is != null ) {
            		sb.append ( " " );
	            	BufferedReader br = new BufferedReader(new InputStreamReader(is));
	                // Output the lines to a StringBuilder to improve error handling...
	            	String s;
	                while ((s = br.readLine()) != null ) {
	                    sb.append(s);
	                }
            	}
            }
            sb.append ( ")" );
            Message.printWarning ( warning_level, 
                   MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, sb.toString() );
            Message.printWarning ( 3, routine, e );
            status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    sb.toString(), "See the log file for details."));
        }
        catch (Exception e) {
            message = "Unexpected error reading URI \"" + URI + "\" (" + e + ")";
            Message.printWarning ( warning_level, 
                   MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
            Message.printWarning ( 3, routine, e );
            status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details."));
        }
        finally {
            // Close the streams and connection
            if ( is != null ) {
            	try {
            		is.close();
            	}
            	catch ( IOException e ) {
            	}
            }
            if ( doOutputFile ) {
	            if ( fos != null ) {
	                fos.close();
	            }
            }
            if ( urlConnection != null ) {
            	urlConnection.disconnect();
            	int code = urlConnection.getResponseCode();
                // If requested, set response code as a property
                if ( doResponseCodeProperty ) {
                    PropList request_params = new PropList ( "" );
                    request_params.setUsingObject ( "PropertyName", ResponseCodeProperty );
                    request_params.setUsingObject ( "PropertyValue", new Integer(code) );
                    try {
                        processor.processRequest( "SetProperty", request_params);
                    }
                    catch ( Exception e ) {
                        message = "Error requesting SetProperty(Property=\"" + ResponseCodeProperty + "\") from processor.";
                        Message.printWarning(log_level,
                            MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                        status.addToLog ( CommandPhaseType.RUN,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    }
                }
            }
        }
	}
	catch ( Exception e ) {
		message = "Unexpected error getting resource from \"" + URI + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file for details."));
		throw new CommandException ( message );
	}

	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
    __OutputFile_File = file;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
    String URI = parameters.getValue ( "URI" );
    String LocalFile = parameters.getValue ( "LocalFile" );
    String OutputProperty = parameters.getValue ( "OutputProperty" );
    String ResponseCodeProperty = parameters.getValue ( "ResponseCodeProperty" );
	StringBuffer b = new StringBuffer ();
	if ( (URI != null) && (URI.length() > 0) ) {
		b.append ( "URI=\"" + URI + "\"" );
	}
	if ( (LocalFile != null) && (LocalFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LocalFile=\"" + LocalFile + "\"" );
	}
	if ( (OutputProperty != null) && (OutputProperty.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputProperty=\"" + OutputProperty + "\"" );
	}
	if ( (ResponseCodeProperty != null) && (ResponseCodeProperty.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ResponseCodeProperty=\"" + ResponseCodeProperty + "\"" );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}