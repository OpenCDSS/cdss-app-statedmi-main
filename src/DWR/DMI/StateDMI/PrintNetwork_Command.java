package DWR.DMI.StateDMI;

import java.awt.print.PageFormat;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import DWR.DMI.HydroBaseDMI.HydroBaseDMI;
import DWR.StateMod.StateMod_Network_JComponent;
import DWR.StateMod.StateMod_NodeNetwork;
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
import RTi.Util.IO.GraphicsPrinterJob;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the PrintNetwork() command.
*/
public class PrintNetwork_Command extends AbstractCommand
implements Command, FileGenerator
{

/**
Possible parameter values.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public PrintNetwork_Command ()
{	super();
	setCommandName ( "PrintNetwork" );
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
{	String InputFile = parameters.getValue ( "InputFile" );
	String PageLayout = parameters.getValue ( "PageLayout" );
    // TODO SAM 2011-06-25 Might be nice to verify these at load, but it can be slow and don't have time to code now
    // Parameters will be verified when editing and running
    //String PrinterName = parameters.getValue ( "PrinterName" );
    String PaperSize = parameters.getValue ( "PaperSize" );
    //String PaperSource = parameters.getValue ( "PaperSource" );
    String Orientation = parameters.getValue ( "Orientation" );
    String MarginLeft = parameters.getValue ( "MarginLeft" );
    String MarginRight = parameters.getValue ( "MarginRight" );
    String MarginTop = parameters.getValue ( "MarginTop" );
    String MarginBottom = parameters.getValue ( "MarginBottom" );
	String OutputFile = parameters.getValue ( "OutputFile" );
    String ShowDialog = parameters.getValue ( "ShowDialog" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;

    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the file to print is not checked during initialization
	// because files may be created dynamically at runtime.

	if ( (InputFile == null) || (InputFile.length() == 0) ) {
		message = "The input file (file to print) must be specified.";
		warning += "\n" + message;
		status.addToLog(CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the file to print."));
	}
	if ( (PageLayout == null) || (PageLayout.length() == 0) ) {
		message = "The page layout must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify a page layout as defined in the network." ) );
	}
	String landscape = PrintUtil.getOrientationAsString(PageFormat.LANDSCAPE);
	String portrait = PrintUtil.getOrientationAsString(PageFormat.PORTRAIT);
    if ( (Orientation != null) && !Orientation.equals("") ) {
        if ( !Orientation.equalsIgnoreCase(landscape) && !Orientation.equalsIgnoreCase(portrait) ) {
            message = "The Orientation parameter \"" + Orientation + "\" is invalid.";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the parameter as " + landscape + " or " + portrait + " (default)."));
        }
    }
    // All margins must be specified, or none at all
    int setCount = 0;
	if ( (MarginLeft != null) && !MarginLeft.equals("") ) {
	    ++setCount;
	    if ( !StringUtil.isDouble(MarginLeft) ) {
            message = "The left margin value (" + MarginLeft + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the left margin as a number." ) );
	    }
    }
    if ( (MarginRight != null) && !MarginRight.equals("") ) {
        ++setCount;
        if ( !StringUtil.isDouble(MarginRight) ) {
            message = "The right margin value (" + MarginRight + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the right margin as a number." ) );
        }
    }
    if ( (MarginTop != null) && !MarginTop.equals("") ) {
        ++setCount;
        if ( !StringUtil.isDouble(MarginTop) ) {
            message = "The top margin value (" + MarginTop + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the top margin as a number." ) );
        }
    }
    if ( (MarginBottom != null) && !MarginBottom.equals("") ) {
        ++setCount;
        if ( !StringUtil.isDouble(MarginBottom) ) {
            message = "The bottom margin value (" + MarginBottom + ") is invalid.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the bottom margin as a number." ) );
        }
    }
    if ( (setCount != 0) && (setCount != 4) ) {
        message = "All margins must be set (or none should be set).";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify all margins, or specify none." ) );
    }
    if ( (setCount != 0) && ((PaperSize == null) || (PaperSize.length() == 0)) ) {
        message = "Margins can only be set when the paper is specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify the paper size to set margins." ) );
    }
    
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
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
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Software error - report to support." ) );
		}
	
		try {
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does not exist: \"" + f2 + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
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
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}

    if ( (ShowDialog != null) && !ShowDialog.equals("") &&
        !ShowDialog.equalsIgnoreCase(_False) && !ShowDialog.equalsIgnoreCase(_True) ) {
        message = "The ShowDialog parameter \"" + ShowDialog + "\" is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the parameter as " + _False + " (default) or " + _True + "."));
    }
    if ( (IfNotFound != null) && !IfNotFound.equals("") ) {
        if ( !IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
            message = "The IfNotFound parameter \"" + IfNotFound + "\" is invalid.";
            warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify the parameter as " + _Ignore + " or (default) " + _Warn + "."));
        }
    }
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector();
	valid_Vector.add ( "InputFile" );
	valid_Vector.add ( "PageLayout" );
	valid_Vector.add ( "PrinterName" );
	valid_Vector.add ( "PaperSize" );
	valid_Vector.add ( "PaperSource" );
	valid_Vector.add ( "Orientation" );
	valid_Vector.add ( "MarginLeft" );
	valid_Vector.add ( "MarginRight" );
	valid_Vector.add ( "MarginTop" );
	valid_Vector.add ( "MarginBottom" );
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "ShowDialog" );
	valid_Vector.add ( "IfNotFound" );
	warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
	return (new PrintNetwork_JDialog ( parent, this )).ok();
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
private File getOutputFile ()
{
    return __OutputFile_File;
}

// Use parent parseCommand()

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "PrintNetwork_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	PropList parameters = getCommandParameters();
	
    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
    // Clear the output file
    setOutputFile ( null );
	
	String InputFile = parameters.getValue ( "InputFile" );
	String PageLayout = parameters.getValue ( "PageLayout" );
    String PrinterName = parameters.getValue ( "PrinterName" );
    String PaperSize = parameters.getValue ( "PaperSize" );
    String PaperSource = parameters.getValue ( "PaperSource" );
    String Orientation = parameters.getValue ( "Orientation" );
    String MarginLeft = parameters.getValue ( "MarginLeft" );
    double marginLeft = -1; // use layout
    if ( (MarginLeft != null) && !MarginLeft.equals("") ) {
        marginLeft = Double.parseDouble(MarginLeft);
    }
    String MarginRight = parameters.getValue ( "MarginRight" );
    double marginRight = -1; // use layout
    if ( (MarginRight != null) && !MarginRight.equals("") ) {
        marginRight = Double.parseDouble(MarginRight);
    }
    String MarginTop = parameters.getValue ( "MarginTop" );
    double marginTop = -1; // use layout
    if ( (MarginTop != null) && !MarginTop.equals("") ) {
        marginTop = Double.parseDouble(MarginTop);
    }
    String MarginBottom = parameters.getValue ( "MarginBottom" );
    double marginBottom = -1; // use layout
    if ( (MarginBottom != null) && !MarginBottom.equals("") ) {
        marginBottom = Double.parseDouble(MarginBottom);
    }
    String OutputFile = parameters.getValue ( "OutputFile" );
    String OutputFile_full = null;
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
    	OutputFile_full = TSCommandProcessorUtil.expandParameterValue(processor,this,
    	    IOUtil.verifyPathForOS(IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile )) );
    }
    String ShowDialog = parameters.getValue ( "ShowDialog" );
    boolean showDialog = false; // Default
    if ( (ShowDialog != null) && ShowDialog.equalsIgnoreCase("true") ) {
        showDialog = true;
    }
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( (IfNotFound == null) || IfNotFound.equals("")) {
	    IfNotFound = _Warn; // Default
	}
	
	// Get the HydroBase DMI...
	
	HydroBaseDMI hbdmi = null;
	try {
		Object o = processor.getPropContents( "HydroBaseDMI");
		hbdmi = (HydroBaseDMI)o;
	}
	catch ( Exception e ) {
		message = "Error requesting HydroBase connection from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Verify that HydroBase is accessible." ) );
	}

	String InputFile_full = TSCommandProcessorUtil.expandParameterValue(processor,this,
	    IOUtil.verifyPathForOS(IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),InputFile )) );
    File file = new File ( InputFile_full );
	if ( !file.exists() ) {
        message = "File to print \"" + InputFile_full + "\" does not exist.";
        if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the file exists at the time the command is run."));
        }
        else if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.WARNING,
                    message, "Verify that the file exists at the time the command is run."));
        }
        else {
            Message.printStatus( 2, routine, message + "  Ignoring.");
        }
	}
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	//StateMod_Network_JFrame networkJFrame = null;
    try {
       	// Read the network file
    	
    	StateMod_NodeNetwork net = null;
    	if ( InputFile_full != null ) {
    		// Read the network.  For now only support old Makenet format...
    		Message.printStatus(2, routine, "Reading network file \"" + InputFile_full + "\"" );
    		net = StateMod_NodeNetwork.readStateModNetworkFile(	InputFile_full,
    			new StateDMI_NodeDataProvider(hbdmi), true);
    	}
    	else {
    		// Get the network from memory and use it.
    		// TODO SAM 2004-04-12 For now try to get from the JFrame.  Later, may try to get from a data set.
    		net = (StateMod_NodeNetwork)processor.getPropContents ( "StateMod_Network" );
    		if ( net == null ) {
    			message = "No network file is given and no network appears to have been read previously.";
    			Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count ), routine, message );
                status.addToLog(CommandPhaseType.RUN,
                    new CommandLogRecord( CommandStatusType.FAILURE, message,
                        "Verify that the file exists and is readable."));
    			throw new Exception ( message );
    		}
    	}
    	// DO NOT DO THIS - this command really isn't intended for integrated processing
    	// Set back in the processor for use by later commands
    	//processor.setPropContents ( "StateMod_Network", net );
    	
    	// Create the JComponent, which is the Printable implementation...
    	
    	//networkJFrame = new StateMod_Network_JFrame(net);
    	//networkJFrame.setVisible(false); // Don't need user to see
    	// Get the printable object from the JFrame
    	// Create the network component using only the network.  Page setup will be determined from the layout
        StateMod_Network_JComponent networkPrintable = new StateMod_Network_JComponent(net,PageLayout);
        	//new StateMod_Network_JComponent(
            //networkJFrame, // TODO SAM 2011-07-04 Would be nice to not have to use this
            //.5);
        // TODO SAM 2011-07-05 This is a bit redundant with what goes on inside the networkPrintable, but
        // data are in different forms so do some work here
        List<PropList> layouts = net.getLayoutList();
        boolean found = false;
        String orientation = null;
        String paperSizeFromLayout = null;
        for (PropList p : layouts ) {
            String id = p.getValue("ID");
            if ( id.equalsIgnoreCase(PageLayout)) {
                orientation = p.getValue("PageOrientation");
                paperSizeFromLayout = p.getValue("PaperSize");
                found = true;
                break;
            }
        }
        if ( !found ) {
        	throw new CommandException (
        		"Layout \"" + PageLayout + "\" was not found in network.  Cannot initialize network.");
        }
        String paperSize = null;
        if ( paperSizeFromLayout != null ) {
        	// Convert from network notation to standard
        	paperSize = PrintUtil.lookupStandardMediaSize(paperSizeFromLayout);
        	Message.printStatus(2, routine, "Using paper size from layout:  \"" + paperSizeFromLayout +
        		"\", corresponding media size = \"" + paperSize + "\"");
        }
        double margin = networkPrintable.getMargin();
        if ( marginLeft < 0.0 ) {
        	marginLeft = margin;
        }
        if ( marginRight < 0.0 ) {
        	marginRight = margin;
        }
        if ( marginTop < 0.0 ) {
        	marginTop = margin;
        }
        if ( marginBottom < 0.0 ) {
        	marginBottom = margin;
        }
        
        // Allow printer name to be a system property so that it can be configured dynamically
        String printerName = null;
        if ( (PrinterName != null) && (PrinterName.length() > 0) ) {
            printerName = PrinterName;
        }
        if ( (PaperSize != null) && (PaperSize.length() > 0) ) {
        	// Override the size in the layout
        	paperSize = PaperSize;
        }
        if ( (Orientation != null) && (Orientation.length() > 0) ) {
        	// Override the size in the layout
        	orientation = Orientation;
        }
        // This sets up the network UI settings for printing the full network
        //networkPrintable.printNetworkSetup ();
        new GraphicsPrinterJob ( networkPrintable,
            InputFile_full, // Printer job name
            printerName,
            paperSize,
            PaperSource,
            orientation,
            margin,
            margin,
            margin,
            margin,
            OutputFile_full,
            showDialog );
        if ( IOUtil.fileExists(OutputFile_full) ) {
            // Save the output file name...
            setOutputFile ( new File(OutputFile_full));
        }
	}
	catch ( Exception e ) {
		if ( InputFile_full == null ) {
			message = "Unexpected error printing network (" + e + ").";
		}
		else {
			message = "Unexpected error printing network \"" + InputFile_full + "\" (" + e + ").";
		}
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "See the log file for details."));
		throw new CommandException ( message );
	}
	finally {
        // Close the hidden JFrame...
        //networkJFrame.close();
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
	String InputFile = parameters.getValue("InputFile");
    String PrinterName = parameters.getValue ( "PrinterName" );
    String PageLayout = parameters.getValue ( "PageLayout" );
    String PaperSize = parameters.getValue ( "PaperSize" );
    String PaperSource = parameters.getValue ( "PaperSource" );
    String Orientation = parameters.getValue ( "Orientation" );
    String MarginLeft = parameters.getValue ( "MarginLeft" );
    String MarginRight = parameters.getValue ( "MarginRight" );
    String MarginTop = parameters.getValue ( "MarginTop" );
    String MarginBottom = parameters.getValue ( "MarginBottom" );
    String ShowDialog = parameters.getValue ( "ShowDialog" );
    String OutputFile = parameters.getValue ( "OutputFile" );
	String IfNotFound = parameters.getValue("IfNotFound");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
    if ( (PageLayout != null) && (PageLayout.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PageLayout=\"" + PageLayout + "\"" );
    }
    if ( (PrinterName != null) && (PrinterName.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PrinterName=\"" + PrinterName + "\"" );
    }
    if ( (PaperSize != null) && (PaperSize.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PaperSize=\"" + PaperSize + "\"");
    }
    if ( (PaperSource != null) && (PaperSource.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "PaperSource=\"" + PaperSource + "\"");
    }
    if ( (Orientation != null) && (Orientation.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Orientation=" + Orientation );
    }
    if ( (MarginLeft != null) && (MarginLeft.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MarginLeft=" + MarginLeft );
    }
    if ( (MarginRight != null) && (MarginRight.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MarginRight=" + MarginRight );
    }
    if ( (MarginTop != null) && (MarginTop.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MarginTop=" + MarginTop );
    }
    if ( (MarginBottom != null) && (MarginBottom.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MarginBottom=" + MarginBottom );
    }
    if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "OutputFile=\"" + OutputFile + "\"" );
    }
    if ( (ShowDialog != null) && (ShowDialog.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ShowDialog=" + ShowDialog );
    }
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}