// The FTP parts of this code were taken from the rti.ts.commands.FTPGet
// RiverTrak(R) Sentry workflow code.  Come changes were necessary to fit into
// the legacy code base.

// FIXME SAM 2008-06-25 Need to clean up exception handling and command status in runCommand().

package rti.tscommandprocessor.commands.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JFrame;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

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
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

/**
This class initializes, checks, and runs the FTPGet() command.
*/
public class FTPGet_Command extends AbstractCommand
implements Command
{
    
protected final String _ASCII = "ASCII";
protected final String _Binary = "Binary";

/**
Constructor.
*/
public FTPGet_Command ()
{	super();
	setCommandName ( "FTPGet" );
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
{	String RemoteSite = parameters.getValue ( "RemoteSite" );
	//String Login = parameters.getValue ( "Login" );
	//String Password = parameters.getValue ( "Password" );
	//String RemoteFolder = parameters.getValue ( "RemoteFolder" );
	//String FilePattern = parameters.getValue ( "FilePattern" );
	String DestinationFolder = parameters.getValue ( "DestinationFolder" );
	String TransferMode = parameters.getValue ( "TransferMode" );
	String RetryCount = parameters.getValue ( "RetryCount" );
	String RetryWait = parameters.getValue ( "RetryWait" );
	String warning = "";
	String message;

	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	CommandProcessor processor = getCommandProcessor();
	
	// The existence of the file to remove is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (RemoteSite == null) || (RemoteSite.length() == 0) ) {
		message = "The remote FTP site must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the remote FTP site."));
	}
    if ( (DestinationFolder == null) || (DestinationFolder.length() == 0) ) {
        message = "The destination folder must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify a destination folder (for example . to indicate working folder)." ) );
    }
    else {
        String working_dir = null;
        try { Object o = processor.getPropContents ( "WorkingDir" );
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Software error - report the problem to support." ) );
        }

        try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                    TSCommandProcessorUtil.expandParameterValue(processor,this,DestinationFolder)));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The destination parent folder does " +
                "not exist for: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Create the destination parent folder." ) );
            }
            f = null;
            f2 = null;
        }
        catch ( Exception e ) {
            message = "The destination folder:\n" +
            "    \"" + DestinationFolder +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the destination folder and working directory paths are compatible." ) );
        }
    }
    if ( (TransferMode != null) && (TransferMode.length() != 0) &&
            !TransferMode.equalsIgnoreCase(_ASCII) && !TransferMode.equalsIgnoreCase(_Binary) ) {
        message = "TransferMode (" + TransferMode + ") is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify as " + _ASCII + " or " + _Binary + " (the default if blank)."));
    }
    if ( (RetryCount != null) && (RetryCount.length() != 0) && !StringUtil.isInteger(RetryCount) ) {
        message = "The RetryCount (" + RetryCount + ") is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an integer."));
    }
    if ( (RetryWait != null) && (RetryWait.length() != 0) && !StringUtil.isInteger(RetryWait) ) {
        message = "The RetryWait (" + RetryWait + ") is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an integer number of seconds."));
    }

	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(9);
	validList.add ( "RemoteSite" );
	validList.add ( "Login" );
	validList.add ( "Password" );
	validList.add ( "RemoteFolder" );
	validList.add ( "FilePattern" );
	validList.add ( "DestinationFolder" );
	validList.add ( "TransferMode" );
	validList.add ( "RetryCount" );
	validList.add ( "RetryWait" );
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
	return (new FTPGet_JDialog ( parent, this )).ok();
}

// Use base class parseCommand

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "FTPGet_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	String RemoteSite = parameters.getValue ( "RemoteSite" );
	if ( RemoteSite != null ) {
	    RemoteSite = TSCommandProcessorUtil.expandParameterValue(processor,this,RemoteSite);
	}
    String Login = parameters.getValue ( "Login" );
    if ( (Login == null) || (Login.length() == 0) ) {
        Login = "anonymous";
    }
    String Password = parameters.getValue ( "Password" );
    if ( (Password == null) || (Password.length() == 0) ) {
        Password = "anonymous";
    }
    String RemoteFolder = parameters.getValue ( "RemoteFolder" );
    if ( RemoteFolder != null ) {
        RemoteFolder = TSCommandProcessorUtil.expandParameterValue(processor,this,RemoteFolder);
    }
    String FilePattern = parameters.getValue ( "FilePattern" );
    if ( FilePattern != null ) {
        FilePattern = TSCommandProcessorUtil.expandParameterValue(processor,this,FilePattern);
    }
    String DestinationFolder = parameters.getValue ( "DestinationFolder" );
    String TransferMode = parameters.getValue ( "TransferMode" );
    if ( (TransferMode == null) || (TransferMode.length() == 0) ) {
        TransferMode = _Binary;
    }
    int retryCount = 3; // Default
    String RetryCount = parameters.getValue ( "RetryCount" );
    if ( StringUtil.isInteger(RetryCount)) {
        retryCount = StringUtil.atoi(RetryCount);
    }
    int retryWait = 3;  // Default
    String RetryWait = parameters.getValue ( "RetryWait" );
    if ( StringUtil.isInteger(RetryWait)) {
        retryWait = StringUtil.atoi(RetryWait);
    }

	String DestinationFolder_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,DestinationFolder) ) );
    File file = new File ( DestinationFolder_full );
	if ( !file.exists() ) {
        message = "Destination folder \"" + DestinationFolder_full + "\" does not exist.";
        Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(
                        command_tag,++warning_count), routine, message );
        status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the destination folder exists at the time the command is run."));
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	boolean success = false;
	try {
        for ( int trycount = 1; trycount <= retryCount; trycount++ ) {
            try {
                run0 ( RemoteSite, Login, Password, RemoteFolder,
                        FilePattern, DestinationFolder_full, TransferMode, trycount );
                success = true;
            } catch (Exception ce) {
                message = "Error performing FTP get ";
                if (trycount <= retryCount) {
                    message += ", will try again in " + RetryWait + " seconds (" + ce + ")";
                }
                // Else let the "success" trigger a failure below.
                //logger.logger().log(Level.INFO, message, ce);
                else {
                    // Make sure to mark the status as failed since this is the last time.
                    message += " after " + RetryCount + " retries (" + ce + ")";
                    status.addToLog(CommandPhaseType.RUN,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "See the log file for details."));
                }
                Message.printWarning(3, routine, message);
                /*
                try {
                    retryPeriod.sleep();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                */
                // Sleep...
                TimeUtil.sleep(retryWait*1000);
            }
            if ( success ) {
                // Got the file so break out of the loop.
                Message.printStatus(2, routine, "Successfully performed file transfer." );
                break;
            }
        }
	}
	catch ( Exception e ) {
		message = "Unexpected error FTPing from \"" + RemoteSite + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file for details."));
		throw new CommandException ( message );
	}
    if (!success) {
        message = "Unable to FTP from \"" + RemoteSite + "\" after " + RetryCount + " tries.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        status.addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details."));
        throw new CommandException ( message );
    }

	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
    String RemoteSite = parameters.getValue ( "RemoteSite" );
    String Login = parameters.getValue ( "Login" );
    String Password = parameters.getValue ( "Password" );
    String RemoteFolder = parameters.getValue ( "RemoteFolder" );
    String FilePattern = parameters.getValue ( "FilePattern" );
    String DestinationFolder = parameters.getValue ( "DestinationFolder" );
    String TransferMode = parameters.getValue ( "TransferMode" );
    String RetryCount = parameters.getValue ( "RetryCount" );
    String RetryWait = parameters.getValue ( "RetryWait" );
	StringBuffer b = new StringBuffer ();
	if ( (RemoteSite != null) && (RemoteSite.length() > 0) ) {
		b.append ( "RemoteSite=\"" + RemoteSite + "\"" );
	}
	if ( (Login != null) && (Login.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Login=\"" + Login + "\"" );
	}
    if ( (Password != null) && (Password.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Password=\"" + Password + "\"" );
    }
    if ( (RemoteFolder != null) && (RemoteFolder.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RemoteFolder=\"" + RemoteFolder + "\"" );
    }
    if ( (FilePattern != null) && (FilePattern.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "FilePattern=\"" + FilePattern + "\"" );
    }
    if ( (DestinationFolder != null) && (DestinationFolder.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DestinationFolder=\"" + DestinationFolder + "\"" );
    }
    if ( (TransferMode != null) && (TransferMode.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TransferMode=" + TransferMode );
    }
    if ( (RetryCount != null) && (RetryCount.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RetryCount=" + RetryCount );
    }
    if ( (RetryWait != null) && (RetryWait.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "RetryWait=" + RetryWait );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

// Ian's code from Sentry follows, updated to work with TSCommandProcessor...

private void run0 ( String RemoteSite, String loginName, String password, String RemoteFolder,
            String FilePattern, String DestinationFolder, String TransferMode, int trycount )
{   String routine = "FTPGet_Command.run0";
    FTPClient ftp = new FTPClient();
    try {
        connect(ftp, RemoteSite, trycount );
        login(ftp, loginName, password, trycount );
        downloadFiles(ftp, RemoteFolder, FilePattern, DestinationFolder, TransferMode);
    } finally {
        try {
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        } catch (IOException ioe) {
            String message = "Error disconnecting from server.";
            Message.printWarning(3, routine, message);
            //logger.warn("Error disconnecting from server", ioe);
        }
    }
}

private void connect(FTPClient ftp, String remoteSite, int trycount )
{   String routine = "FTPGet_Command.connect";
    //final boolean verbal = logger.verbal();
    //if (verbal) {
        //logger.verbal("Connecting to server : " + remoteSite);
        Message.printStatus(2, routine, "Connecting to server:  " + remoteSite );
    //}
    try {
        ftp.connect(remoteSite);
    } catch (IOException ex) {
        String message = "Error connecting to server \"" + remoteSite + "\" (this is try "
        + trycount + ") : " + ex;
        getCommandStatus().addToLog(CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the server is accessible."));
        Message.printWarning(3, routine, message);
        throw new RuntimeException(message);
    }
    int reply = ftp.getReplyCode();
    if (FTPReply.isPositiveCompletion(reply)) {
        //if (verbal) {
            //logger.verbal("Connected to server");
            Message.printStatus(2, routine, "Connected to server.");
        //}
    } else {
        String message = "Unable to connect to server \"" + remoteSite + "\", FTP error code : " + reply +
            "(this is try " + trycount + ")";
        getCommandStatus().addToLog(CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the server is accessible."));
        Message.printWarning(3, routine, message);
        throw new RuntimeException();
    }
}

private void login(FTPClient ftp, String loginName, String password, int trycount )
{   String routine = "FTPGet_Command.login";
    //        if (loginName != null) {
    //final boolean verbal = logger.verbal();
    //if (verbal) {
        //logger.verbal("Logging in as " + loginName);
        Message.printStatus( 2, routine, "Logging in as \"" + loginName + "\"" );
    //}
    boolean loggedIn = false;
    if (loginName == null) {
        loginName = "anonymous";
    }
    if (password == null) {
        password = "anonymous";
    }
    try {
        loggedIn = ftp.login(loginName, password);
    } catch (IOException ex) {
        String message = "Error logging in to server (this is try "
        + trycount + ") : " + ex;
        getCommandStatus().addToLog(CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that the login information is correct."));
        Message.printWarning(3, routine, message);
        throw new RuntimeException( message, ex );
    }
    if (!loggedIn) {
        String message = "Error logging in to server (this is try "
            + trycount + ")";
            getCommandStatus().addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the login information is correct."));
            Message.printWarning(3, routine, message);
        throw new RuntimeException("Login failed");
    }
    //if (verbal) {
        //logger.verbal("Logged in");
        Message.printStatus( 2, routine, "Logged in." );
    //}
//        }
}

private void downloadFiles(FTPClient ftp, String remoteFolder, String FilePattern,
        String DestinationFolder, String TransferMode )
{   String routine = "FTPGet_Command.downloadFiles";
    File destinationFolder = new File(DestinationFolder);
    if (!destinationFolder.exists()) {
        boolean mkdirs = destinationFolder.mkdirs();
        if (!mkdirs) {
            String message = "Unable to create destination folder \"" + destinationFolder + "\".";
            getCommandStatus().addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the path to the folder is OK and write permissions are granted."));
                Message.printWarning(3, routine, message);
            throw new RuntimeException(message);
        }
    }
    //final boolean verbal = logger.verbal();
    //if (verbal) {
        //logger.verbal("downloading files from " + remoteFolder);
        Message.printStatus(2, routine, "Downloading files from " + remoteFolder);
    //}
    try {
        boolean cwdSuccess = ftp.changeWorkingDirectory(remoteFolder);
        if (!cwdSuccess) {
            //System.out.println(ftp.printWorkingDirectory());
            String message = "Remote directory \"" + remoteFolder + "\" does not exist.";
            getCommandStatus().addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the remove directory exists."));
                Message.printWarning(3, routine, message);
            throw new RuntimeException();
        }
    }
    catch (IOException ex) {
        String message = "Error changing CWD to \"" + remoteFolder + "\" (" + ex + ").";
        getCommandStatus().addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the remove directory exists."));
        throw new RuntimeException(message, ex);
    }
    FTPFile[] files = null;
    try {
        files = ftp.listFiles();
    } catch (IOException ex) {
        throw new RuntimeException("Error listing remote directory", ex);
    }
    ArrayList<String> matched = new ArrayList<String>();
    Glob filePattern = new Glob ( "*", false );
    if ( (FilePattern != null) && !FilePattern.equals("") ) {
        filePattern = new Glob(FilePattern,false);
    }
    Pattern pattern = filePattern.pattern();
    for (int i = 0; i < files.length; i++) {
        if (pattern.matcher(files[i].getName()).matches()) {
            matched.add(files[i].getName());
        }
    }
    //if (logger.info()) {
        //logger.info("matched " + matched.size() + " remote files");
        Message.printStatus(2, routine, "Matched " + matched.size() + " remote files." );
    //}
    int transferMode = TransferMode.equals(_ASCII) ? FTP.ASCII_FILE_TYPE : FTP.BINARY_FILE_TYPE;
    try {
        ftp.setFileTransferMode(transferMode);
    } catch (IOException ex) {
        ex.printStackTrace();
    }
    for ( int i = 0; i < matched.size(); i++ ) {
        String fileName = (String)matched.get(i);
        downloadFile(ftp, fileName, destinationFolder);
    }
}

private void downloadFile(FTPClient ftp, String file, File destinationFolder)
{   String routine = "FTPGet_Command.downloadFile";
    File dest = new File(destinationFolder, file);
    //boolean verbose = logger.verbose();
    OutputStream out = null;
    try {
        OutputStream base = new FileOutputStream(dest);
        //if (verbose) {
        //    base = wrapStream(base);
        //}
        out = new BufferedOutputStream(base);
    } catch (IOException ioe) {
        throw new RuntimeException("Error opening destination file " + dest, ioe);
    }
    try {
        ftp.retrieveFile(file, out);
        Message.printStatus( 2, routine, "Transferred file to \"" + dest + "\"" );
    } catch (IOException ex) {
        String message = "Error transfering file \"" + file + "\" (" + ex + ")";
        getCommandStatus().addToLog(CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Check the log file."));
        Message.printWarning(3, routine, message);
        throw new RuntimeException(message, ex);
    } finally {
        try {
            out.close();
        } catch (IOException ex) {
            String message = "Unable to close destination file \"" + dest + "\" (" + ex + ")";
            //logger.warn("Unable to close destination file " + dest, ex);
            Message.printWarning(3, routine, message);
        }
    }
}

/*
private OutputStream wrapStream(OutputStream base) {
    // @todo wrap with progress metering
    return base;
}
*/

}
