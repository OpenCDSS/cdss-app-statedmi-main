package rti.tscommandprocessor.commands.check;

import java.io.File;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CommandProcessorEvent;
import RTi.Util.IO.CommandProcessorEventListener;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.MissingObjectEvent;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
 * Listener to handle CommandProcessorEvent events.  The listener queues the events that it
 * handles and generates output when finalizeOutput() is called.
 * @author sam
 *
 */
public class CheckFileCommandProcessorEventListener implements CommandProcessorEventListener
{
    
private List __eventList = new Vector(10);

private File __checkFile = null;

/**
 * Properties for table elements, including alignment, etc.
 */
private PropList __tableProps = new PropList ( "table" );

/**
 * Properties for TD table elements, including alignment, etc.
 */
private PropList __tdProps = new PropList ( "td" );

/**
 * Properties for title elements.
 */
private PropList __titleProps = new PropList ( "title" );

public CheckFileCommandProcessorEventListener ( File checkFile )
{
    __checkFile = checkFile;
}

/**
 * Add the event to the list that will be processed.
 */
private void addEvent ( CommandProcessorEvent event )
{
    __eventList.add ( event );
}
    
/**
Close all open tags and finish writing the html to the file.
@param html HTMLWriter object.
@throws Exception 
 */
private void endHTML( HTMLWriter html)  throws Exception
{
    if ( html != null ) {
        html.bodyEnd();
        html.htmlEnd();
        html.closeFile();
    }
}
    
/**
 * Finalize the output check file by performing final sorting of events and formatting
 * the output.
 */
public void finalizeOutput ()
throws Exception
{
    // For now to save memory and possibly performance, brute force search for specific
    // event types for formatting.
    
    initialize();
    HTMLWriter html = new HTMLWriter( __checkFile.getCanonicalPath(), "Check File", false );
    startHTML( html );
    writeTableOfContents( html );
    //writeHeader( html );
    //writeCommandFile( html );
    //writeRuntimeMessages( html );
    //writeDataChecks( html );
    writeMissingObjectEvents ( html, getMissingObjectEventList() );
    endHTML( html );
    
    //return __check_file;
}

/**
 * Get the list of MissingObjectEvent from the queued up list of events.
 */
private List getMissingObjectEventList ()
{
    List eventList = new Vector();
    Object event;
    for ( int i = 0; i < __eventList.size(); i++ ) {
       event = __eventList.get(i);
       if ( event instanceof MissingObjectEvent ) {
           eventList.add ( event );
       }
    }
    return eventList;
}

/**
 * Get the properties used for tables.
 */
private PropList getTableProps()
{
    return __tableProps;
}

/**
 * Get the properties used for table elements (TD).
 */
private PropList getTdProps()
{
    return __tdProps;
}

/**
 * Get the properties used for title elements.
 */
private PropList getTitleProps()
{
    return __tdProps;
}
    
/**
 * Handle the CommandProcessorEvent events generated during processing and format for output.
 */
public void handleCommandProcessorEvent ( CommandProcessorEvent event )
{   String routine = "CheckFileCommandProcessorEventListener.handleCommandProcessorEvent";
    Message.printStatus ( 2, routine, "Got event " + event );
    // Accumulate it...
    addEvent ( event );
}

/**
 * Initialize properties used in formatting.
 */
private void initialize ()
{
    __tdProps.add("valign=bottom");
    
    __tableProps.add("border=\"1\"");
    __tableProps.add("bordercolor=black");
    __tableProps.add("cellspacing=1");
    __tableProps.add("cellpadding=1");
    
    __titleProps.add( "id=titles" );
}

/**
Writes the start tags for the HTML check file.
@param html HTMLWriter object.
@throws Exception
*/
private void startHTML( HTMLWriter html ) throws Exception
{
    if ( html != null ) {
        html.htmlStart();
        writeCheckFileStyle(html);
        html.bodyStart();
    }
}

/**
Inserts the style attributes for a check file.
@throws Exception
 */
public void writeCheckFileStyle(HTMLWriter html) throws Exception
{
    html.write("<style>\n"
            + "#titles { font-weight:bold; color:#303044 }\n"
            + "table { background-color:black; text-align:left }\n"  
            + "th {background-color:#333366; text-align:center;"
            + " vertical-align:bottom; color:white }\n" 
            + "td {background-color:white; text-align:center;"
            + " vertical-align:bottom; }\n" 
            + "body { text-align:left; font-size:12; }\n"
            + "pre { font-size:12; }\n"
            + "p { font-size:12; }\n"
            + "</style>\n");
}

/**
Writes the HTML for the MissingObjectEvents.
@param html HTMLWriter object.
@param tableStart - List of properties for the HTML table. 
@param int index Current index of the data list.
@throws Exception
 */
private void writeMissingObjectEvents( HTMLWriter html, List missingObjectEventList )
throws Exception
{
    // Headers for the table.
    String [] headers = { "#", "ID of Missing Time Series", "Command" };
    // grab the data from the model
    ///Vector gen_data = new Vector(); 
    ///gen_data = gen_data_model.getData();
    // proplist provides an anchor link for this section used
    // from the table of contents
    ///PropList gen_prop = new PropList( "Gen" );
    ///gen_prop.add( "name=generic" + index );
    // start the generic data section
    ///html.paragraphStart( __title_prop );
    ///html.link( gen_prop, "", gen_data_model.getTitle() );
    if ( missingObjectEventList.size() == 0 ) {
        return;
    }
    // table start
    html.heading( 1, "Missing Time Series" );
    html.paragraph ( "The following time series were requested but could not be read." );
    html.tableStart( getTableProps() );
    html.tableRowStart();
    html.tableHeaders( headers );
    html.tableRowEnd();
    MissingObjectEvent event;
    String [] rowData = new String[3];
    PropList tdProps = getTdProps();
    for ( int i = 0; i < missingObjectEventList.size(); i++ ) {
        event = (MissingObjectEvent)missingObjectEventList.get(i);
        rowData[0] = "" + (i + 1);
        rowData[1] = event.getMissingObjectID();
        // toString() must return a "nice" string for this to work...
        rowData[2] = "" + event.getResource();
        html.tableRowStart();
        html.tableCells( rowData, tdProps );
        html.tableRowEnd();
    }
    html.tableEnd();
}

/**
Writes the HTML table of contents for the check file.
@param html HTMLWriter object.
@throws Exception
 */
private void writeTableOfContents( HTMLWriter html )  throws Exception
{
    if ( html == null ) {
        return;
    }
    // properties for the table of contents HTML table
    PropList tableStart = new PropList("Table");
    tableStart.add("border=\"1\"");
    tableStart.add("bordercolor=black");
    tableStart.add("cellspacing=1");
    tableStart.add("cellpadding=1");
    String [] data_table_header = {"Component", "Type of Check", "# Problems", "# Total Checks"};
    //html follows ...
    html.headerStart( 4, getTitleProps() );    // <h3> tag
    html.addText( "Table Of Contents" );
    html.headerEnd( 4 );
    html.link( "#header", "Header" );
    html.breakLine();
    html.link( "#command_file", "Command File" );
    html.breakLine();
   // html.link( "#run_msgs", "Runtime Messages (" + __run_msgs.size() + ")" );
   // html.breakLine();
    html.link( "#MissingObjectEvent", "Missing Time Series" );
    html.breakLine();
    /*
    // Table of contents data records (there may be many of these)
    // this is written as a table of components and there
    // general and specific data checks
    html.tableStart( tableStart );
    html.tableRowStart();
    html.tableHeaders( data_table_header );
    html.tableRowEnd();
    // Write out the data and links to data checks
    // as a table with links to missing and specific data checks
    for ( int i = 0; i < __spec_data.size(); i++ ) {
        // get the data models
        CheckFile_DataModel dm = ( CheckFile_DataModel )
        __spec_data.elementAt(i);
        CheckFile_DataModel dm_gen = ( CheckFile_DataModel )__gen_data.elementAt(i);
        // get the data needed for the TOC from the data models
        //String data_size = new Integer( 
        //      dm_gen.getDataSize() ).toString();
        String data_size = new Integer ( dm_gen.getTotalNumberProblems()).toString();
        String total_size = new Integer( dm_gen.getTotalChecked() ).toString();
        String[] toc_values = { dm.getTitle(), "Zero or Missing",
            data_size, total_size };
        // write the first data section (row)
        // this section has the general data check info and links
        writeTocDataSection( html, toc_values, i );
        data_size = new Integer( dm.getTotalNumberProblems()).toString();
        total_size = new Integer( dm.getTotalChecked() ).toString();
        String[] toc_values2 = { dm.getTitle(), data_size, total_size };
        // write the second data section (row)
        // this section has the specific data check info and links
        writeTocDataSection( html, toc_values2, i );
    }
    html.tableEnd();
    */
    html.horizontalRule();
}

}
