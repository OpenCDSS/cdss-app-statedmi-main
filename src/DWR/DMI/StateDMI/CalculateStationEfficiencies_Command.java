// CalculateStationEfficiencies_Command - This class initializes, checks, and runs the Calculate*DemandTS*() commands.

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

import javax.swing.JFrame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import DWR.StateMod.StateMod_Data;
import DWR.StateMod.StateMod_DataSet;
import DWR.StateMod.StateMod_Diversion;
import DWR.StateMod.StateMod_Util;
import DWR.StateMod.StateMod_Well;

import RTi.TS.MonthTS;
import RTi.TS.MonthTotals;
import RTi.TS.TSUtil;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;

/**
This class initializes, checks, and runs the Calculate*DemandTS*() commands.
It is an abstract base class that must be controlled via a derived class.  For example,
the CalculateDiversionDemandTSMonthly() command extends this class in order to uniquely
represent the command, but much of the functionality is in this base class.
*/
public abstract class CalculateStationEfficiencies_Command extends AbstractCommand
implements Command, FileGenerator
{

/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Possible values for IgnoreLEZero parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

//Used with printEfficiencyReport...

private final int EFF_PRINT_STATION_ID	= 0x1; // Print the station ID in report
private final int EFF_PRINT_STDDEV	= 0x2; // Print the standard deviation in the report
private final int EFF_PRINT_BASIN_DATA	= 0x4; // Print the basin data in report

/**
Output start.
*/
private DateTime __EffCalcStart_DateTime = null;

/**
Output end.
*/
private DateTime __EffCalcEnd_DateTime = null;

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.  The command name should be set in the constructor of derived classes.
*/
public CalculateStationEfficiencies_Command ()
{	super();
	setCommandName ( "Calculate?StationEfficiencies" );
}

/* ----------------------------------------------------------------------------
** calculateEfficiencyDetail - calculate the detailed efficiency report values
** ----------------------------------------------------------------------------
** History:
**
** 05-26-95	Steven A. Malers, RTi	First version of routine.
** 05-30-95	SAM, RTi		Fix some warning messages to be at a
**					lower level.
** 15 May 96	SAM, RTi		Change some warning levels.
** 24 Feb 1997	Matthew J. Rutherford, RTi
**					Changed to variable length messages.
** 20 Jun 1997	SAM, RTi		Fix bug in debug message when printing
**					monthly efficiencies.
** 2004-09-01	SAM, RTi		Pull in code from demandts.
** ----------------------------------------------------------------------------
** Notes:	(1)	This routine takes as input a historic demand time
**			series and an irrigation demand time series and
**			calculates a matrix:
**
**			 	1  2  3  4  5  6  7  8  9  10  11  12  Total
**			year1
**			...
**			...
**
**			for the demands, diversions, and efficiencies.  The
**			standard deviations of the values in each column are
**			also returned.
** ----------------------------------------------------------------------------
** Variable		I/O	Description
**
** calendar		I	Indicates whether using water or calendar year.
** dem_all		O	The filled in demands matrix.
** demmonthdata		L	Monthly data for demands - one column.
** demts		I	Irrigation demand time series.
** div_all		O	The filled in diversions matrix.
** divmonthdata		L	Monthly data for historic diversions - one
**				column.
** divstaid		I	ID for diversion station.
** divts		I	Historic diversions time series.
** eff_all		O	The filled in efficiency matrix.
** effmonthdata		L	Data for one month, entire period of record.
** imon			L	Loop counter for months (column).
** iyear		L	Loop counter for years (row).
** months*		L	Order to process months.
** ndemmonthdata	L	Number of months of demands.
** ndivmonthdata	L	Number of months of historic diversions.
** routine		L	Name of this routine.
** sdevtotal		O	Standard deviations for the columns.
** year1, year2		L	Calendar years of period of record.
** yearoffset*		L	Offset to convert calendar year to water year
**				(time series are stored internally in calendar
**				years).
** ----------------------------------------------------------------------------
*/
// FIXME SAM 2009-02-10 Need to evaluate how to handle errors in this method.
/**
Calculate the details for the efficiency report, to output a high-detail report.
@param its Counter to display (1 offset).
*/
private int calculateEfficiencyDetail ( int its, MonthTS demts, MonthTS divts, String divstaid,
	YearType calendar, int year1, int year2, double dem_all[][], double div_all[][], double eff_all[][],
	double sdevtotal[], boolean eff_ignore_le_zero )
throws Exception
{	int imon, iyear;
	int MAXYEARS = 200;
	double [] demmonthdata = new double[MAXYEARS], divmonthdata = new double[MAXYEARS],
			effmonthdata = new double[MAXYEARS];
	String routine ="StateDMI_Processor.calculateEfficiencyDetail";
	int ndivmonthdata = 0;
	DateTime date1 = new DateTime ( DateTime.PRECISION_MONTH ),
			date2 = new DateTime ( DateTime.PRECISION_MONTH );

	//Message.printStatus ( 2, routine,
	//"Calculating detail for calendar years " + year1 + " to " + year2 );

	// Set the pointers for the calendar adjustments...

	int yearoffset1	[] = new int[12];
	int yearoffset2	[] = new int[12];
	int months[] = new int[12];

	if ( calendar == YearType.CALENDAR ) {
		// Calendar year;
		yearoffset1[0] = 0;	yearoffset2[0] = 0;	months[0] = 1;
		yearoffset1[1] = 0;	yearoffset2[1] = 0;	months[1] = 2;
		yearoffset1[2] = 0;	yearoffset2[2] = 0;	months[2] = 3;
		yearoffset1[3] = 0;	yearoffset2[3] = 0;	months[3] = 4;
		yearoffset1[4] = 0;	yearoffset2[4] = 0;	months[4] = 5;
		yearoffset1[5] = 0;	yearoffset2[5] = 0;	months[5] = 6;
		yearoffset1[6] = 0;	yearoffset2[6] = 0;	months[6] = 7;
		yearoffset1[7] = 0;	yearoffset2[7] = 0;	months[7] = 8;
		yearoffset1[8] = 0;	yearoffset2[8] = 0;	months[8] = 9;
		yearoffset1[9] = 0;	yearoffset2[9] = 0;	months[9] = 10;
		yearoffset1[10] = 0; yearoffset2[10] = 0; months[10] = 11;
		yearoffset1[11] = 0; yearoffset2[11] = 0; months[11] = 12;

	}
	else if ( calendar == YearType.WATER ) {
		yearoffset1[0] = 1;	yearoffset2[0] = 0;	months[0] = 10;
		yearoffset1[1] = 1;	yearoffset2[1] = 0;	months[1] = 11;
		yearoffset1[2] = 1;	yearoffset2[2] = 0;	months[2] = 12;
		yearoffset1[3] = 1;	yearoffset2[3] = 0;	months[3] = 1;
		yearoffset1[4] = 1;	yearoffset2[4] = 0;	months[4] = 2;
		yearoffset1[5] = 1;	yearoffset2[5] = 0;	months[5] = 3;
		yearoffset1[6] = 1;	yearoffset2[6] = 0;	months[6] = 4;
		yearoffset1[7] = 1;	yearoffset2[7] = 0;	months[7] = 5;
		yearoffset1[8] = 1;	yearoffset2[8] = 0;	months[8] = 6;
		yearoffset1[9] = 0;	yearoffset2[9] = -1; months[9] = 7;
		yearoffset1[10] = 0; yearoffset2[10] = -1; months[10] = 8;
		yearoffset1[11] = 0; yearoffset2[11] = -1; months[11] = 9;
	}
	else {
		throw new RuntimeException ( "Year type " + calendar + " is not supported." );
	}

	// Fill in the raw data values...

	for ( iyear = 0; iyear < MAXYEARS; iyear++ ) {
		dem_all[iyear][12] = 0.0;
		div_all[iyear][12] = 0.0;
	}
	for ( imon = 0; imon < 12; imon++ ) {
		ndivmonthdata = MAXYEARS;
		// Need to use right calendar.  The years coming in are
		// calendar but we want to print out in water year order...
		date1.setYear (	year1 + yearoffset1[months[imon] - 1] );
		date1.setMonth ( months[imon] );
		//date1.setMonth ( imon + months[imon] );
		date2.setYear (	year2 + yearoffset2[months[imon] - 1] );
		date2.setMonth ( months[imon] );
		// First get the diversion values for the month...
		try { divmonthdata = TSUtil.toArrayByMonth ( divts, date1, date2, months[imon] );
			ndivmonthdata = divmonthdata.length;
		}
		catch ( Exception e ) {
			if ( (imon == 0) && (divts == null) ) {
				// Only print one message...
				Message.printWarning ( 1, routine,
				"NULL time series [" + its + "] - error getting diversion " +
				"data for month " + months[imon] + " (period " + date1 + " to " + date2 + ")..." );
			}
			else {
				Message.printWarning ( 1, routine, divts.getLocation() +
				" Error getting diversion data for month " + months[imon] +
				" (period " + date1 + " to " + date2 + ")..." );
			}
			Message.printWarning ( 2, routine, e.getMessage() );
		}
		// Now get the demand values for the month...
		try {
			demmonthdata =	TSUtil.toArrayByMonth ( demts, date1, date2, months[imon] );
		}
		catch ( Exception e ) {
			if ( (imon == 0) && (demts == null) ) {
				// Only print one message...
				Message.printWarning ( 1, routine, "NULL time series [" + its + "] - error getting demand data" +
				" for month " + months[imon] + " (period " + date1 + " to " + date2 + ")..." );
			}
			else {
				Message.printWarning ( 1, routine, demts.getLocation() +
				" Error getting demand data for month " + months[imon] +
				" (period " + date1 + " to " + date2 + ")..." );
			}
			Message.printWarning ( 2, routine, e.getMessage() );
		}
		// Transfer a single month into the main array...
		for ( iyear = 0; iyear < ndivmonthdata; iyear++ ) {
			dem_all[iyear][imon] = demmonthdata[iyear];
			div_all[iyear][imon] = divmonthdata[iyear];
			if ( eff_ignore_le_zero ) {
				if ( (demmonthdata[iyear] <= 0.0) || (divmonthdata[iyear] <= 0.0) ) {
					// Ignore data if either is <= 0.0...
					continue;
				}
			}
			if ( dem_all[iyear][12] < 0.0 ) {
				// Need to initialize...
				dem_all[iyear][12] = demmonthdata[iyear];
			}
			else {
				dem_all[iyear][12] += demmonthdata[iyear];
			}
			if ( div_all[iyear][12] < 0.0 ) {
				div_all[iyear][12] = divmonthdata[iyear];
			}
			else {
				div_all[iyear][12] += divmonthdata[iyear];
			}
		}
	}
	// Now calculate efficiencies for all the years of data for each year
	// for the particular month.  Include the yearly values in the
	// calculations (but the yearly values do not include values that
	// are ignored because they are <= 0.0 - these were skipped above when
	// the monthly values were transferred)...
	for ( imon = 0; imon < 13; imon++ ) {
		for ( iyear = 0; iyear < ndivmonthdata; iyear++ ) {
			if ( div_all[iyear][imon] == 0.0 ) {
				eff_all[iyear][imon] = 0.0;
			}
			else {
				eff_all[iyear][imon] = 100.0*(dem_all[iyear][imon]/div_all[iyear][imon]);
			}
			// Print out the efficiency numbers...
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10, routine, StringUtil.formatString(divstaid, "%-10.10s") +
				" Efficiency [" + (year1 + iyear) + "][" + months[imon] + "] = " + eff_all[iyear][imon] );
			}
		}
	}
	// Now calculate the standard deviations for each column...
	double sdevtmp[] = new double[ndivmonthdata];
	int nsdevtmp = 0;
	for ( imon = 0; imon < 13; imon++ ) {
		// First pull out the vertical columns...
		for ( iyear = 0; iyear < ndivmonthdata; iyear++ ) {
			effmonthdata[iyear] = eff_all[iyear][imon];
		}
		try {
			// Loop through and discard any efficiency values <= zero...
			nsdevtmp = 0;
			for ( iyear = 0; iyear < ndivmonthdata; iyear++ ) {
				if ( effmonthdata[iyear] > 0.0 ) {
					sdevtmp[nsdevtmp] = effmonthdata[iyear];
					++nsdevtmp;
				}
			}
			sdevtotal[imon] = MathUtil.standardDeviation ( nsdevtmp, sdevtmp );
		}
		catch ( Exception e ) {
			Message.printWarning ( 20, routine, "\"" + divstaid + ":  Zero data for " +
			TimeUtil.monthAbbreviation(imon + 1) + " - not computing StdDev" );
		}
	}
	return ndivmonthdata;
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
{	//String routine = getClass().getName() + ".checkCommandParameters";
	String ID = parameters.getValue ( "ID" );
	String EffMin = parameters.getValue ( "EffMin" );
	String EffMax = parameters.getValue ( "EffMax" );
	String EffCalcStart = parameters.getValue ( "EffCalcStart" );
	String EffCalcEnd = parameters.getValue ( "EffCalcEnd" );
	String EffReportFile = parameters.getValue ( "EffReportFile" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String warning = "";
	String message;
	
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (ID == null) || (ID.length() == 0) ) {
        message = "The station ID must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the station ID to process." ) );
	}
	
	if ( (EffMax != null) && (EffMax.length() > 0) && !StringUtil.isDouble(EffMax)) {
        message = "The maximum efficiency (" + EffMax + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the maximum efficiency as a number." ) );
	}
	
	if ( (EffMin != null) && (EffMin.length() > 0) && !StringUtil.isDouble(EffMin)) {
        message = "The minimum efficiency (" + EffMin + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum efficiency as a number." ) );
	}
	
	if ( (EffCalcStart != null) && !EffCalcStart.equals("")) {
		try {
			__EffCalcStart_DateTime = DateTime.parse(EffCalcStart);
		}
		catch (Exception e) {
			message = "Efficiency calculation start date \"" + EffCalcStart + "\" is not a valid date.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid EffCalc start date." ) );
		}
	}
	if ( (EffCalcEnd != null) && !EffCalcEnd.equals("")) {
		try {
			__EffCalcEnd_DateTime = DateTime.parse(EffCalcEnd);
		}
		catch (Exception e) {
			message = "Efficiency calculation end date \"" + EffCalcEnd + "\" is not a valid date.";
			warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify a valid EffCalc end date." ) );
		}
	}
	
	if ( (EffReportFile != null) && (EffReportFile.length() > 0) ) {
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
			String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, EffReportFile));
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
			"    \"" + EffReportFile +
			"\"\ncannot be adjusted using the working directory:\n" +
			"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Verify that output file and working directory paths are compatible." ) );
		}
	}
	
	// Include the Add option
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Ignore) &&
		!IfNotFound.equalsIgnoreCase(_Fail) && !IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}
	
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(7);
	valid_Vector.add ( "ID" );
	valid_Vector.add ( "EffMin" );
	valid_Vector.add ( "EffMax" );
	valid_Vector.add ( "EffCalcStart" );
	valid_Vector.add ( "EffCalcEnd" );
	valid_Vector.add ( "EffReportFile" );
	valid_Vector.add ( "IfNotFound" );

    warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

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
{	// The command will be modified if changed...
	return (new CalculateStationEfficiencies_JDialog ( parent, this )).ok();
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
protected File getOutputFile ()
{
	return __OutputFile_File;
}

/**
Print the efficiency report header.
@param ofp Reference to open output file.
@param efficiency_low Lowest "ok" efficiency, percent.
@param efficiency_high Highest "ok" efficiency, percent.
@param calendar	Indicates the calendar being used for input and output ("Calendar" or "Water").
@param month1 Starting calendar month for data.
@param year1 Starting calendar year for data.
@param month2 Ending calendar month for data.
@param year2 Ending calendar year for data.
@param eff_round Increment in efficiencies, percent.
@param divsta list of StateMod_Diversion being processed or null if wells are being processed.
@param wellsta list of StateMod_Well being processed or null if diversions are being processed.
@param eff_ignore_le_zero Indicate whether values <= zero should be considered in average values.
*/
private void printEfficiencyReportHeader ( PrintWriter ofp, double efficiency_low,
	double efficiency_high, YearType calendar, int month1,
	int year1, int month2, int year2, double eff_round,
	List<StateMod_Diversion> divsta, List<StateMod_Well> wellsta, boolean eff_ignore_le_zero )
	// multistruct is list of MultiStructData
	// divsta is Vector of StateMod_Diversion
{	int ndivsta = 0;
	if ( divsta != null ) {
		ndivsta = divsta.size();
	}
	int nwellsta = 0;
	if ( wellsta != null ) {
		nwellsta = wellsta.size();
	}
	boolean is_div = true;	// True if diversions, false if wells.
	String station_type = "diversion";
	String tstype = "diversion";
	if ( ndivsta > 0 ) {
		is_div = true;
	}
	else if ( nwellsta > 0 ) {
		is_div = false;
		station_type = "well";
		tstype = "pumping";
	}

	String newline = System.getProperty ( "line.separator" );

	// TODO SAM 2004-09-01 what is this?
	//IOUtil.printCreatorHeader ( demandts.getDMI(), ofp, "#", 80, 0 );
	ofp.print (
	"# Efficiency calculation report (only applies to agricultural structures)." + newline + "#" + newline );
	IOUtil.printCreatorHeader ( ofp, "#", 80, 0 );
	ofp.print (
	"#" + newline +
	"# Efficiencies for the period are computed on a monthly basis as" + newline +
	"# the average IWR divided by the average " + tstype +"."+newline);
	if ( eff_ignore_le_zero ) {
		if ( is_div ) {
			ofp.print (
			"# An IWR or diversion <= 0.0 results in the month's data being ignored" + newline +
			"# (marked with X before values)." + newline );
		}
		else {
			ofp.print (
			"# An IWR or pumping <= 0.0 results in the month's data being ignored" + newline +
			"# (marked with X before values)." + newline );
		}
	}
	else {
		if ( is_div ) {
			ofp.print ( "# Zero IWR and diversions are included in averages." + newline );
		}
		else {
			ofp.print ( "# Zero IWR and pumping are included in averages." + newline );
		}
	}
	ofp.print ( "#" + newline +
		"# Period of record for efficiency calculations is:  " +
	month1 + "/" + year1 + " to " + month2 + "/" + year2 + newline +
	"#" + newline +
	"# Output is listed by " + station_type + " in the original " + station_type + " station order." + newline +
	"# Areas are acres taken from the " + station_type + " stations file." + newline +
	"# The rows of output for each " + station_type + newline +
	"# station are listed below (the \"S\" and \"M\" rows are shown only for" + newline +
	"# \"All\" year output)." + newline +
	"#" + newline +
	"#     1) Consumptive (irrigation) water requirement time series (leading ** indicates missing time" + newline +
	"#        series).  These lines are indicated by \"C\" (consumptive use" + newline +
	"#        water requirement) in the output (X indicates ignored)." + newline +
	"#     2) Historic " + tstype + " time series (leading ** indicates missing time" + newline +
	"#        series).  These lines are indicated by \"H\" (Historic " + tstype + ")" + newline +
	"#        in the output (X indicates ignored)." + newline +
	"#     3) Efficiency (leading *- indicates a total efficiency < " + efficiency_low + " (min)," + newline +
	"#        *+ indicates a total efficiency > " + efficiency_high + " (max), value of ******** indicates" + newline +
	"#        that the efficiency is zero or can't be computed)." +newline+
	"#        These lines are indicated by \"E\" (Efficiency) in the output." + newline +
	"#     4) Standard deviation of efficiency for period of record.  These lines" + newline +
	"#        are indicated by \"S\" (standard deviation) in the output." + newline +
	"#        Only efficiencies > 0.0 are analyzed." + newline +
	"#     5) Efficiency used in the model.  These lines are indicated by \"M\" in the output." + newline +
	/* TODO SAM 2004-09-01 - don't currently handle resets here since
	they are processed  in later commands.
	\"Mu\" indicates that the user has set the" +
	newline +
	"#        efficiency (using seteff()).  \"Mo\" indicates that the " +
	"efficiency" + newline +
	"#        has been taken from the previous version of the " +
	station_type +
	" station" + newline +
	"#        file.
	*/
	"#        If the total efficiency is negative, then the 12" + newline +
	"#        monthly values are used by the model.  If the total efficiency is" + newline +
	"#        positive, then the monthly efficiencies are the same as the total." + newline +
	"#        The model efficiences are rounded to the nearest " + eff_round + "%.  This line" + newline +
	"#        also shows a count of the structures and the structure name if" + newline +
	"#        available from the " + station_type + " stations file." + newline +
	"#" + newline );

/* TODO SAM 2004-09-01 - unclear if multistruct() is needed
	ofp.print (
	"# The following list describes irrigated parcels that are irrigated "+
	"by more" + newline +
	"# than one structure.  In this case, only the primary structure " +
	"should have" + newline +
	"# a demand time series.  The \"Dem Src\" field in the report below " +
	"indicates" + newline +
	"# the demand source (multistruct() relationships should involve types"+
	" 3, 4, 5):" + newline +
	"#" + newline +
	"# Dem Src        Description" + newline +
	"# -------------------------------------------------------------------"+
	"----" + newline +
	"#    1           Irrigated acreage from the GIS database." + newline +
	"#    2           Irrigated acreage from the TIA database." + newline +
	"#    3           Primary multistruct() structure with GIS acreage." +
	newline +
	"#    4           Primary multistruct() structure with TIS acreage." +
	newline +
	"#    5           Secondary multistruct() structure (no acreage " +
	"required)." + newline +
	"#    6           Municipal or industrial structure (no acreage " +
	"required)." + newline +
	"#    7           Carrier structure (no acreage required)." + newline +
	"#    8           User supplied acreage." + newline +
	"#   others       Not allowed!" + newline +
	"#" + newline +
	"# Note that the basin-wide efficiency shown at the bottom of this " +
	"report is" + newline +
	"# for demand source types 1, 2, 3, 4, and 8." + newline +
	"#" + newline );
	if ( nmultistruct <= 0 ) {
		ofp.print ( "#" + newline +
		"# No multistruct() combinations have been applied to this " +
		"data set." + newline +
		"#" + newline );
	}
	else {	ofp.print (
	"# " + nmultistruct + " multistruct() combinations have been applied " +
	"to this data set, as follows:" + newline +
	"#" + newline +
	"#           Primary/" + newline +
	"#  Number   Secondary  Structure (Description)" + newline +
	"#  -----------------------------------------------------------------"+
	"----" + newline );
		for ( i = 0; i < nmultistruct; i++ ) {
			// Find the structure so that we can print the
			// description...
			multistruct_i = (MultiStructData)
					multistruct.get(i);
			search_results =	searchForStructure (
					multistruct_i.getStructureID(0),
					divsta );
			if ( search_results == null ) {
				strname = "????";
			}
			else {	divstapt = search_results.getDiv();
				if ( divstapt == null ) {
					strname = "????";
				}
				else {	strname = divstapt.getName();
				}
			}
			ofp.print (
			"#    " +
			StringUtil.formatString ( (i + 1), "%3d" ) +
			"    Primary    " + multistruct_i.getStructureID(0) +
			" (" + strname + ")" );
			for ( j = 1; j < multistruct_i.getNumStruct(); j++ ) {
				search_results =	searchForStructure (
						multistruct_i.getStructureID(j),
						divsta );
				if ( search_results == null ) {
					strname = "????";
				}
				else {	divstapt = search_results.getDiv();
					if ( divstapt == null ) {
						strname = "????";
					}
					else {	strname = divstapt.getName();
					}
				}
				ofp.print (
				"#           Secondary  " +
				multistruct_i.getStructureID(j) + " (" +
				strname + ")" + newline );
			}
		}
		ofp.print ( "#" + newline );
	}
*/
	ofp.print ( "#EndHeader" + newline );
	ofp.print ( "#" + newline );
	if ( calendar == YearType.WATER ) {
		ofp.print (
"#   Struct    Area    Dem Water" + newline +
"#   ID        Acres   Src Year           Oct      Nov      Dec      Jan      Feb      Mar      Apr      May      Jun      Jul      Aug      Sep      Tot" +
			newline );
	}
	else if ( calendar == YearType.CALENDAR ) {
		ofp.print (
"#   Struct    Area    Dem Calendar" + newline +
"#   ID        Acres   Src Year           Jan      Feb      Mar      Apr      May      Jun      Jul      Aug      Sep      Oct      Nov      Dec      Tot" +
		newline );
	}
	ofp.print (
"#---------------------------------------------------------------------------------------------------------------------------------------------------------" +
	newline );
}

/* ----------------------------------------------------------------------------
** printEfficiencyReportItem - print a line of output in the -demands report
** ----------------------------------------------------------------------------
** History:
**
** 05-24-95	Steven A. Malers, RTi	Initial version of routine.
** 05-31-95	SAM, RTi		Change symbols next to lines of output
**					to "C", "H", "E", and "S".  Added model
**					efficiency to output.
** 07 Mar 96	SAM, RTi		Add the demand source to the output so
**					that we can better tell if the
**					structure is a multistruct or not.
**					Add the structure name to the model
**					line.
** 15 May 96	SAM, RTi		Update so that the basin-wide
**					efficiencies are noted to be only
**					from diversions with acreage.
** 19 Jun 1997	SAM, RTi		Update to use a mask to indicate what
**					should be printed - this is easier to
**					read than the separate flags.
** 2004-09-01	SAM, RTi		Pull in code from demandts.  Change as
**					little as possible.
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** carea	L	Character string to contain area OR blanks.
** cdemsrc	L	Character string to contain demand source OR blanks.
** cyear	L	Character string to contain year OR blanks.
** flag		I	Mask indiicating what should be printed:
**			PRINT_STATION_ID	indicates tht the first line
**						should have the station
**						information (1 = yes, 0 = no)
**			PRINT_STDDEV		indicates that the output should
**						contain the standard deviation
**						& model efficiency lines (1)
**			PRINT_BASIN_DATA	indicates that the line is the
**						final basin line (2).
** i		L	Loop counter.
** string	L	String for line of output.
** string2	L	String for single value in line of output.
** year		I	The year to be printed with the output (only applies
**			to detailed report).
** warn		L	Warning string.
** ----------------------------------------------------------------------------
*/

/**
Print one line of the demands report.
@param eff_ignore_le_zero If a monthl diversion or demand is zero, ignore the
month in calculation of the efficiency.  Still print, but put an X in front of the efficiency line.
*/
private void printEfficiencyReportItem ( PrintWriter repofp, int flag,
						int year0,
						StateMod_Data structure,
							// old...
							//StructureData
							//structure,
						int demts_ok, int divts_ok,
						int efficiency_ok,
						double dem[], double div[],
						double eff[],
						double stddev[],
						double eff_mod[],
						YearType calendar, int count,
						double basin_acres,
						boolean eff_ignore_le_zero )
{	String carea = "", cdemsrc = "", cyear = "", id,
		routine = "StateDMI.printEfficiencyReportItem",
		string, string2, warn, letter;
	String mflag = " ";
	int	i, year = year0;
	String format = "%8.1f";
	String newline = System.getProperty ( "line.separator" );
	String status[] = new String[12];

	for ( i = 0; i < 12; i++ ) {
		status[i] = " ";
	}

	if ( (flag & EFF_PRINT_STATION_ID) != 0 ) {
		id = structure.getID();
		if ( structure instanceof StateMod_Diversion ) {
			carea = StringUtil.formatString(((StateMod_Diversion)structure).getArea(),"%5.0f");
			cdemsrc = StringUtil.formatString (((StateMod_Diversion)structure).getDemsrc(), "%4d" );
		}
		else if ( structure instanceof StateMod_Well ) {
			carea = StringUtil.formatString(((StateMod_Well)structure).getAreaw(), "%5.0f");
			cdemsrc = StringUtil.formatString (((StateMod_Well)structure).getDemsrcw(), "%4d" );
		}
	}
	else {
		id = " ";
		carea = " ";
		cdemsrc = "    ";
	}
	if ( (flag & EFF_PRINT_BASIN_DATA) != 0 ) {
		id = "Basin Agri";
		carea = StringUtil.formatString ( basin_acres, "%.0f" );
	}
	if ( year > 0 ) {
		if ( calendar == YearType.WATER ) {
			year = year0 + 1;
		}
		cyear = StringUtil.formatString ( year, "%4d" );
	}
	else {
		cyear = "Avg";
	}

	if ( demts_ok == 1 ) {
		warn = "   ";
	}
	else {
		warn = "** ";
	}

	if ( count == -2 ) {
		// we have a area-weighted line
		letter = "EffxAc";
	}
	else {
		letter = "C";
		// Check to see if pairs should be ignored (this will also be
		// used below where diversions are printed out)...
		if ( eff_ignore_le_zero ) {
			for ( i = 0; i < 12; i++ ) {
				if ( (dem[i] <= 0.0) || (div[i] <= 0.0) ) {
					status[i] = "X";
				}
			}
		}
	}
	if ( dem == null ) {
		Message.printStatus ( 2, routine, "id is " + structure.getID());
		Message.printStatus ( 2, routine, "carea is " + carea );
		Message.printStatus ( 2, routine, "cdemsrc is " + cdemsrc );
		Message.printStatus ( 2, routine, "cyear is " + cyear );
		Message.printStatus ( 2, routine, "letter is " + letter );
		Message.printWarning ( 2, routine, "dem is null" );
	}
	// Don't want anything after the decimal...
	format = "%8.0f";
	repofp.print (
	warn + " " +
	StringUtil.formatString ( id, "%-10.10s" ) + " " +
	StringUtil.formatString ( carea, "%5.5s" ) + " " +
	StringUtil.formatString ( cdemsrc, "%4.4s" ) + " " +
	StringUtil.formatString ( cyear, "%4.4s" ) + " " +
	StringUtil.formatString ( letter, "%6.6s" ) +
	status[0] + StringUtil.formatString ( dem[0], format ) +
	status[1] + StringUtil.formatString ( dem[1], format ) +
	status[2] + StringUtil.formatString ( dem[2], format ) +
	status[3] + StringUtil.formatString ( dem[3], format ) +
	status[4] + StringUtil.formatString ( dem[4], format ) +
	status[5] + StringUtil.formatString ( dem[5], format ) +
	status[6] + StringUtil.formatString ( dem[6], format ) +
	status[7] + StringUtil.formatString ( dem[7], format ) +
	status[8] + StringUtil.formatString ( dem[8], format ) +
	status[9] + StringUtil.formatString ( dem[9], format ) +
	status[10] + StringUtil.formatString ( dem[10], format ) +
	status[11] + StringUtil.formatString ( dem[11], format ) +
	" " + StringUtil.formatString ( dem[12], format ) + newline );
	if ( divts_ok == 1 ) {
		warn = "   ";
	}
	else {
		warn = "** ";
	}

	if( count == -2 ) {
		letter = "TotAcres";
	}
	else {
		letter = "H";
	}

	repofp.print (
	warn + "                            " +
	StringUtil.formatString ( letter, "%6.6s" ) +
	status[0] + StringUtil.formatString ( div[0], format ) +
	status[1] + StringUtil.formatString ( div[1], format ) +
	status[2] + StringUtil.formatString ( div[2], format ) +
	status[3] + StringUtil.formatString ( div[3], format ) +
	status[4] + StringUtil.formatString ( div[4], format ) +
	status[5] + StringUtil.formatString ( div[5], format ) +
	status[6] + StringUtil.formatString ( div[6], format ) +
	status[7] + StringUtil.formatString ( div[7], format ) +
	status[8] + StringUtil.formatString ( div[8], format ) +
	status[9] + StringUtil.formatString ( div[9], format ) +
	status[10] + StringUtil.formatString ( div[10], format ) +
	status[11] + StringUtil.formatString ( div[11], format ) +
	" " + StringUtil.formatString ( div[12], format ) + newline );
	// Efficiency line...
	if ( efficiency_ok < 0 ) {
		warn = "*- ";
	}
	else if ( efficiency_ok == 0 ) {
		warn = "*+ ";
	}
	else {
		warn = "   ";
	}

	if ( count == -2 ) {
		letter = "WEff";
	}
	else {
		letter = "E";
	}

	string = warn + "                            " + StringUtil.formatString(letter,"%6.6s") + " ";
	for ( i = 0; i < 13; i++ ) {
		// Before 2000-10-25...
		//if ( eff[i] != 0.0 ) {}
		if ( eff[i] > 0.0 ) {
			string2 = StringUtil.formatString(eff[i], "%8.2f ");
			//string2 = StringUtil.formatString(eff[i], "%8.4f ");
		}
		else {
			string2 = "******** ";
		}
		string = string + string2;
	}
	repofp.print ( string + newline );
	if ( (flag & EFF_PRINT_STDDEV) != 0 ) {
		// Standard deviations...
		warn = "   ";
		string =
		warn + "                            " +
		StringUtil.formatString("S","%6.6s") + " ";
		for ( i = 0; i < 13; i++ ) {
			// Before 2000-10-25...
			//if ( eff[i] != 0.0 ) { }
			if ( eff[i] > 0.0 ) {
				string2 = StringUtil.formatString(stddev[i], "%8.2f " );
				//StringUtil.formatString(stddev[i], "%8.4f " );
			}
			else {
				string2 = "******** ";
			}
			string = string + string2;
		}
		repofp.print ( string + newline );
		// Efficiencies for model...
		// TODO SAM 2004-09-01 - needed?
		/*
		if ( structure.isEfficiencySet() ) {
			mflag = "u";
		}
		else if ( structure.isEfficiencyOld() ) {
			mflag = "o";
		}
		else {
		mflag = " ";
		}
		*/
		if ( count >= 0 ) {
			string = StringUtil.formatString ( count, "%-4d" ) + " " +
			StringUtil.formatString ( structure.getName(),"%25.25s" ) + " " +
			StringUtil.formatString ( "M", "%6.6s") +
			StringUtil.formatString ( mflag, "%1.1s" );
		}
		else {
			// Basin total...
			string = "     " + StringUtil.formatString ( "Basin Total", "%25.25s" )
			+ "  " + StringUtil.formatString ( "M", "%6.6s") + " ";
		}
		for ( i = 0; i < 13; i++ ) {
			string2 = StringUtil.formatString (	eff_mod[i], "%8.2f " );
			string = string + string2;
		}
		repofp.print ( string + newline );
	}
	repofp.print ( newline );
}

/**
Print the demands report showing efficiencies, etc. for each structure.  The
summary information comes from the StructureData Vector whereas detailed
information for each year are recomputed (carrying around takes a lot of memory.
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** acre_basin	L	total acreage of the basin
** acre_eff	L	acreage weighted efficiencies for each month
** acre_eff_bas	L	acreage for a structure x efficiency (total basin)
** calendar	L	Calendar to use for output.
** dem_all	L	Matrix of demands (years x months).
** demts_ok	L	Indicates if the demand time series exists.
** div_all	L	Matrix of diversions (years x months).
** divstaList	I	List of diversion stations.
** wellstaList	I	List of well stations.
** divts_ok	L	Indicates if the diversion time series exists.
** eff_all	L	Matrix of efficiencies (years x months).
** eff_high	I	Highest efficiency allowed for modeling.
** eff_low	I	Lowest efficiency allowed for modeling.
** eff_ok	L	Indicates if the total efficiency is in the accepted range.
** eff_round0	I	Value to round efficiencies to.
** iflag	L	Flag indicating what to print.
** ndivsta	I	The number of diversion stations.
** ndivmonthdata L	The number of months in the historic period (i.e., how many Januaries).
** month*	I	Beginning and ending months for output.
** outrepfile	L	Name of the output file.
** rep_detail	I	Detail desired for report.
** repofp	L	Pointer to output file.
** routine	L	Name of this routine.
** sdevtotal	L	Standard deviation of efficiencies for structure for total period of record.
** year*	I	Beginning and ending years for output.
** ----------------------------------------------------------------------------
*/
// FIXME SAM 2009-02-10 Evaluate how to handle errors
private void printEfficiencyReport ( String EffReportFile, List<StateMod_Diversion> divstaList,
	List<StateMod_Well> wellstaList, int month1, int year1,
	int month2, int year2, YearType calendar, boolean rep_high_detail, double eff_low, double eff_high,
	double eff_round0, boolean eff_create_stm, boolean eff_ignore_le_zero )
throws Exception
{	String outrepfile, routine = "StateDMI_Processor.printEfficiencyReport";
	String message;
	PrintWriter	repofp = null;
	int MAXYEARS=200;
	double[] acre_eff_bas = new double[13];
	double[] acre_basin = new double[13];
	double[] acre_eff = new double[13];
	double[][] dem_all = new double [MAXYEARS][13];
	double[] dem_basin = new double[13];
	double[][] div_all = new double[MAXYEARS][13];
	double[] div_basin = new double[13];
	double[][] eff_all = new double[MAXYEARS][13];
	double[] eff_basin = new double[13];
	double[] sdevtotal = new double[13];
	double[] dummy = new double[13];
	int demts_ok, divts_ok, eff_ok, i, j, ndivmonthdata = 0;
	int iflag;
	String newline = System.getProperty ( "line.separator" );
	int demsrc_i;
	StateMod_Diversion div_i = null;
	StateMod_Well well_i = null;
	DateTime date = new DateTime(DateTime.PRECISION_MONTH );

	// Determine whether a list of StateMod_Diversion or StateMod_Well have been passed...

	boolean is_div = true;
	if ( (divstaList != null) && (divstaList.size() > 0) ) {
		is_div = true;
	}
	else {
		is_div = false;
	}

	int nsta = 0;
	if ( divstaList != null ) {
		nsta = divstaList.size();
	}
	if ( wellstaList != null ) {
		nsta = wellstaList.size();
	}

	// Open the report file...

	outrepfile = IOUtil.getPathUsingWorkingDir ( EffReportFile );
	try {
		repofp = new PrintWriter ( new FileWriter(outrepfile) );
	}
	catch ( IOException e ) {
		message = "Unable to open efficiency report file \"" + outrepfile + "\"";
		Message.printWarning( 2, routine, message );
		throw new Exception ( message );
	}
	Message.printStatus( 2, routine, "Creating efficiency report file \"" + outrepfile +
		"\" for years " + year1 + " to " + year2 );

	// Print the header to the efficiency report...

	printEfficiencyReportHeader ( repofp, eff_low, eff_high, calendar, month1, year1, month2, year2,
		eff_round0, divstaList, wellstaList, eff_ignore_le_zero );

	double [] cwr_monthly = null; // Used to retrieve average values from
	double [] ddh_monthly = null; // StateMod_Diversion objects
	double [] eff_calc = null;
	double [] eff_stddev = null;
	double [] eff_model = null;

	// Loop on the structures and print the report...

	for ( i = 0; i < 13; i++ ) {
		dem_basin[i] = 0.0;
		div_basin[i] = 0.0;
		acre_basin[i] = 0.0;
		acre_eff_bas[i] = 0.0;
		acre_eff[i]	= 0.0;
	}
	for ( i = 0; i < nsta; i++ ) {
		if ( is_div ) {
			div_i = divstaList.get(i);
			demsrc_i = div_i.getDemsrc();
			cwr_monthly = div_i.getAverageMonthlyCWR();
			ddh_monthly = div_i.getAverageMonthlyHistoricalDiversions();
			eff_model = div_i.getModelEfficiencies();
			eff_calc = div_i.getCalculatedEfficiencies();
			eff_stddev = div_i.getCalculatedEfficiencyStddevs();
		}
		else {
			well_i = wellstaList.get(i);
			demsrc_i = well_i.getDemsrcw();
			cwr_monthly = well_i.getAverageMonthlyCWR();
			ddh_monthly = well_i.getAverageMonthlyHistoricalPumping();
			eff_model = well_i.getModelEfficiencies();
			eff_calc = well_i.getCalculatedEfficiencies();
			eff_stddev = well_i.getCalculatedEfficiencyStddevs();
		}
		// Do not process structures that have null data (probably not agricultural)...
		if ( (eff_model == null) || (eff_calc == null) ) {
			continue;
		}
		// Only print the efficiency information for primary irrigation structures...
		if ( (demsrc_i != StateMod_Diversion.DEMSRC_GIS) &&
			(demsrc_i != StateMod_Diversion.DEMSRC_TIA) &&
			(demsrc_i != StateMod_Diversion.DEMSRC_GIS_PRIMARY)&&
			(demsrc_i != StateMod_Diversion.DEMSRC_TIA_PRIMARY)&&
			(demsrc_i != StateMod_Diversion.DEMSRC_USER) ) {
			if ( is_div ) {
				Message.printStatus ( 2, routine, "Skipping \"" + div_i.getID() +
				"\"... a primary irrigation structure." );
			}
			else {
				Message.printStatus ( 2, routine, "Skipping \"" + well_i.getID() +
				"\"... a primary irrigation structure." );
			}
			continue;
		}
		// Set flags indicating whether the diversion and demand time series have valid values
		// (may get rid of this if we are interpreting the demand source)...
		if ( is_div ) {
			if ( div_i.getDiversionMonthTS() == null) {
				divts_ok = 0;
			}
			else {
				divts_ok = 1;
			}
			if ( div_i.getConsumptiveWaterRequirementMonthTS() == null) {
				demts_ok = 0;
			}
			else {
				demts_ok = 1;
			}
		}
		else {
			if ( well_i.getPumpingMonthTS() == null) {
				divts_ok = 0;
			}
			else {
				divts_ok = 1;
			}
			if ( well_i.getConsumptiveWaterRequirementMonthTS() == null) {
				demts_ok = 0;
			}
			else {
				demts_ok = 1;
			}
		}
		// Check the yearly total efficiencies to see if they are in the accepted range...
		eff_ok = 1;
		if ( eff_model[12] < eff_low ) {
			eff_ok = -1;
		}
		else if ( eff_model[12] > eff_high ) {
			eff_ok = 0;
		}
		if ( !rep_high_detail ) {
			// Low detail output.
			// Show totals with full labels (the first line does have the station name, etc.)...
			if ( is_div ) {
				printEfficiencyReportItem (
					repofp,
					EFF_PRINT_STATION_ID |
					EFF_PRINT_STDDEV, 0,
					div_i,
					demts_ok, divts_ok, eff_ok,
					cwr_monthly,
					ddh_monthly,
					eff_calc,
					eff_stddev,
					eff_model,
					calendar, (i + 1), 0.0,
					eff_ignore_le_zero );
			}
			else {
				printEfficiencyReportItem (
					repofp,
					EFF_PRINT_STATION_ID |
					EFF_PRINT_STDDEV, 0,
					well_i,
					demts_ok, divts_ok, eff_ok,
					cwr_monthly,
					ddh_monthly,
					eff_calc,
					eff_stddev,
					eff_model,
					calendar, (i + 1), 0.0,
					eff_ignore_le_zero );
			}
		}
		else {
			// High detail.
			// Pulled this out of ProcessDemands...
			//
			// Get the detail information so that we have
			// standard deviations.  The yearly values will then
			// be available if the high detail output is desired...
			//
			// Old, before 2000-10-24.  The demand time series in
			// some cases is set to the historic, but we need to
			// make sure the irrigation demand time series is used
			// here...
			//try {	ndivmonthdata = calculateDetail ( (i + 1),
			//		div_i.getDemandTS(), div_i.getHistTS(),
			//		div_i.getID(), calendar, year1, year2,
			//		dem_all, div_all, eff_all, sdevtotal );
			//}
			// New, as of 2000-10-24...  The irrigation demand
			// time series will have been set in the
			// computeAgEfficiencies() method...
			try {
				if ( is_div ) {
					ndivmonthdata = calculateEfficiencyDetail (
					(i + 1),
					div_i.getConsumptiveWaterRequirementMonthTS(),
					div_i.getDiversionMonthTS(),
					div_i.getID(), calendar, year1, year2,
					dem_all, div_all, eff_all, sdevtotal,
					eff_ignore_le_zero );
				}
				else {
					ndivmonthdata =
					calculateEfficiencyDetail (
					(i + 1),
					well_i.getConsumptiveWaterRequirementMonthTS(),
					well_i.getPumpingMonthTS(),
					well_i.getID(), calendar, year1, year2,
					dem_all, div_all, eff_all, sdevtotal,
					eff_ignore_le_zero );
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine, "Unable to calculate detail" );
			}
			// Print out the detailed output.  There is
			// no standard deviation because we are only
			// looking at one year at a time.  The model
			// efficiencies are NOT printed.  Just use the
			// structure[i] arrays but they will not get printed.
			date.setMonth ( month1 );
			date.setYear ( year1 );
			for ( j = 0; j < ndivmonthdata; j++ ) {
				// Print the line of output...
				if ( j == 0 ) {
					iflag = EFF_PRINT_STATION_ID;
				}
				else {
					iflag = 0;
				}
				if ( is_div ) {
					printEfficiencyReportItem (
						repofp, iflag, (year1 + j),
						div_i,
						1, 1, 1,
						dem_all[j],
						div_all[j],
						eff_all[j],
						eff_stddev,
						eff_model,
						calendar, (j + 1), 0.0,
						eff_ignore_le_zero );
				}
				else {
					printEfficiencyReportItem (
						repofp, iflag, (year1 + j),
						well_i,
						1, 1, 1,
						dem_all[j],
						div_all[j],
						eff_all[j],
						eff_stddev,
						eff_model,
						calendar, (j + 1), 0.0,
						eff_ignore_le_zero );
				}
			}
			// Print the summary for the diversion station,
			// including the average for the period...
			//
			// Don't need the labels.  Do need stddev.  This is
			// basically the same as the low-detail report output,
			// with no headers...
			if ( is_div ) {
				printEfficiencyReportItem (
					repofp, EFF_PRINT_STDDEV, 0,
					div_i,
					demts_ok, divts_ok, eff_ok,
					div_i.getAverageMonthlyCWR(),
					div_i.getAverageMonthlyHistoricalDiversions(),
					div_i.getCalculatedEfficiencies(),
					div_i.getCalculatedEfficiencyStddevs(),
					div_i.getModelEfficiencies(),
					calendar, (i + 1), 0.0,
					eff_ignore_le_zero );
			}
			else {
				printEfficiencyReportItem (
					repofp, EFF_PRINT_STDDEV, 0,
					well_i,
					demts_ok, divts_ok, eff_ok,
					well_i.getAverageMonthlyCWR(),
					well_i.getAverageMonthlyHistoricalPumping(),
					well_i.getCalculatedEfficiencies(),
					well_i.getCalculatedEfficiencyStddevs(),
					well_i.getModelEfficiencies(),
					calendar, (i + 1), 0.0,
					eff_ignore_le_zero );
			}
		}
		// Add the demands and diversions/pumping to the basin-wide
		// arrays if the structure is an agricultural structure (has acreage)...
		if ( (demsrc_i == StateMod_Diversion.DEMSRC_GIS) ||
			(demsrc_i == StateMod_Diversion.DEMSRC_TIA) ||
			(demsrc_i == StateMod_Diversion.DEMSRC_GIS_PRIMARY) ||
			(demsrc_i == StateMod_Diversion.DEMSRC_TIA_PRIMARY) ||
			(demsrc_i == StateMod_Diversion.DEMSRC_USER)){
			for ( j = 0; j < 13; j++ ) {
				if ( cwr_monthly[j] > 0.0 ) {
					dem_basin[j] += cwr_monthly[j];
				}
				if ( ddh_monthly[j] > 0.0 ) {
					div_basin[j] += ddh_monthly[j];
				}

				// Now do the Basin-Wide Acreage-weighted efficiencies.

				if ( is_div ) {
					if ( (eff_model[j]>0.0) && (div_i.getArea() > 0.0) ) {
						acre_eff_bas[j] += eff_model[j]*div_i.getArea();
						acre_basin[j] +=div_i.getArea();
					}

					if ( Message.isDebugOn ) {
						Message.printDebug( 5, routine, "[" + (i + 1) + "] month[" + (j + 1) +
							"] acre x eff = " + div_i.getArea()*eff_model[j] + " cumulative = " +
							acre_eff_bas[j] + " cum acres: " + acre_basin[j]);
					}
				}
				else {
					if ( (eff_model[j]>0.0) && (well_i.getAreaw() > 0.0) ) {
						acre_eff_bas[j] += eff_model[j]*well_i.getAreaw();
						acre_basin[j] += well_i.getAreaw();
					}

					if ( Message.isDebugOn ) {
						Message.printDebug( 5, routine, "[" + (i + 1) + "] month[" + (j + 1) +
							"] acre x eff = " + well_i.getAreaw()*eff_model[j] + " cumulative = " +
							acre_eff_bas[j] + " cum acres: " + acre_basin[j]);
					}
				}
			}
		}
	}

	// Calculate the basin-wide efficiencies and output...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Calculating basin-wide efficiencies..." );
	}
	for ( i = 0; i < 13; i++ ) {
		if ( div_basin[i] <= 0.0 ) {
			eff_basin[i] = 0.0;
		}
		else {
			eff_basin[i] = 100.0*(dem_basin[i]/div_basin[i]);
		}

		// Now for the acreage weighted
		if ( acre_basin[i] <= 0.0 ) {
			acre_eff[i] = 0.0;
		}
		else {
			acre_eff[i] = (double)(acre_eff_bas[i]/acre_basin[i]);
		}
	}

	// Print out a special header to offset these basin-wide calculations from the rest of the report

	repofp.print ( "Basin-wide efficiencies:" + newline + newline );

	if ( is_div ) {
		repofp.print (
		"        Efficiencies calculated using only demand " +
			"and historical diversions:" + newline + newline );
	}
	else {
		repofp.print (
		"        Efficiencies calculated using only demand " +
			"and historical pumping:" + newline + newline );
	}

	/* TODO SAM 2004-09-06 not exactly correct since calculated from
	demand/diversion
	repofp.print (
	newline +
	"        Efficiencies are average of " + newline +
	"        Agricultural Structures' Efficiencies" + newline + newline );
	*/

	printEfficiencyReportItem (
			repofp, EFF_PRINT_BASIN_DATA, 0, null,
			1, 1, 1,
			dem_basin, div_basin, eff_basin,
			dummy, dummy, calendar, -1,
			acre_basin[0], eff_ignore_le_zero);

	repofp.print (
	newline +
	"        Efficiencies calculated using area-weighted average of " +
		newline + "        agricultural structures' model efficiencies"+
		" (reflecting efficiency limits)." + newline +
		"Area is taken from the station file and does not "+
		"vary with time:" + newline + newline );

	printEfficiencyReportItem (
			repofp, EFF_PRINT_BASIN_DATA, 0, null,
			1, 1, 1, acre_eff_bas, acre_basin, acre_eff,
			dummy, dummy, calendar, -2, acre_basin[0],
			eff_ignore_le_zero );

	if ( repofp != null ) {
		repofp.flush();
		repofp.close();
	}
}

// The following is expected to be called by the derived classes.

/**
Run method internal to this class, to handle running in discovery and run mode.
@param command_number Command number 1+ from processor.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    String routine = getClass().getName() + ".runCommandInternal", message;
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Log level for non-user warnings
    
    CommandPhaseType command_phase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
    StateDMI_Processor processor = (StateDMI_Processor)getCommandProcessor();
    
    // Not all of these are used with diversions and/or wells but it is OK to request all.

    PropList parameters = getCommandParameters();
	String ID = parameters.getValue ( "ID" );
	if ( ID == null ) {
		ID = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
	String EffMin = parameters.getValue ( "EffMin" );
	String EffMax = parameters.getValue ( "EffMax" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	String EffReportFile = parameters.getValue ( "EffReportFile" );
	String EffReportFile_full = null;
	if ( EffReportFile != null ) {
		EffReportFile_full = IOUtil.verifyPathForOS(
		        IOUtil.toAbsolutePath(StateDMICommandProcessorUtil.getWorkingDir(processor),
		            StateDMICommandProcessorUtil.expandParameterValue(processor,this,EffReportFile)));
	}
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}

    // Get the data needed for the command
	
    // Get the historical time series
    
    List<MonthTS> histTSList = null;
    String stationType = "";
    int histTSListSize = 0;
    int compType = StateMod_DataSet.COMP_UNKNOWN; // station type being processed
    try {
    	if ( this instanceof CalculateDiversionStationEfficiencies_Command ) {
			@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_DiversionHistoricalTSMonthly_List" );
			histTSList = dataList;
			stationType = "diversion";
			compType = StateMod_DataSet.COMP_DIVERSION_STATIONS;
		}
    	else if ( this instanceof CalculateWellStationEfficiencies_Command ) {
    		@SuppressWarnings("unchecked")
			List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_WellHistoricalPumpingTSMonthly_List" );
    		histTSList = dataList;
    		stationType = "well";
    		compType = StateMod_DataSet.COMP_WELL_STATIONS;
		}
    	histTSListSize = histTSList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + stationType + " historical time series (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( histTSListSize == 0 ) {
        message = "Historical " + stationType + " time series are not available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Read or generate the time series with previous command(s)." ) );
    }
    
    // Get the consumptive water requirement time series if doing the IWR/Hist
    
    List<MonthTS> cwrTSList = null;
    int cwrTSListSize = 0;
    try {
		@SuppressWarnings("unchecked")
		List<MonthTS> dataList = (List<MonthTS>)processor.getPropContents ( "StateMod_ConsumptiveWaterRequirementTSMonthly_List" );
		cwrTSList = dataList;
    	cwrTSListSize = cwrTSList.size();
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting irrigation (consumptive) water requirement time series (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( cwrTSListSize == 0 ) {
        message = "Irrigation (consumptive) water requirement time series must be available to calculate demands.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Read the time series with the ReadIrrigationWaterRequirementTSMonthlyFromStateCU() command." ) );
    }
    
    // Get the stations, used to set efficiencies
    List<StateMod_Diversion> ddsList = null;
    List<StateMod_Well> wesList = null;
    int stationListSize = 0;
	try {
		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Diversion> dataList = (List<StateMod_Diversion>)processor.getPropContents ( "StateMod_DiversionStation_List" );
    		ddsList = dataList;
    		stationListSize = ddsList.size();
    	}
		else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    		@SuppressWarnings("unchecked")
			List<StateMod_Well> dataList = (List<StateMod_Well>)processor.getPropContents ( "StateMod_WellStation_List" );
    		wesList = dataList;
    		stationListSize = wesList.size();
    	}
	}
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting " + stationType + " station list (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    if ( stationListSize == 0 ) {
        message = "No " + stationType + " stations are available.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Read or set the " + stationType + " stations with previous command(s)." ) );
    }
	
	// Need output year type to know order of efficiencies
    YearType outputYearType = null;
    try {
    	outputYearType = (YearType)processor.getPropContents ( "OutputYearType" );
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputYearType (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    // Output period will be used if not specified with EffCalcStart and EffCalcEnd
    
    DateTime OutputStart_DateTime = null;
    try {
    	OutputStart_DateTime = (DateTime)processor.getPropContents ( "OutputStart");
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputStart (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    DateTime OutputEnd_DateTime = null;
    try {
    	OutputEnd_DateTime = (DateTime)processor.getPropContents ( "OutputEnd");
    }
	catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Error requesting OutputEnd (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report to software support.  See log file for details." ) );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings about command input.";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count),
        routine, message );
        throw new InvalidCommandParameterException ( message );
    }

    try {
    	// Clear the output file
    	setOutputFile ( null );
    	// Properties that control computations...
    	
    	DateTime EffCalcStart_DateTime = OutputStart_DateTime;
    	if ( __EffCalcStart_DateTime != null ) {
    		EffCalcStart_DateTime = __EffCalcStart_DateTime;
    	}
    	DateTime EffCalcEnd_DateTime = OutputEnd_DateTime;
    	if ( __EffCalcEnd_DateTime != null ) {
    		EffCalcEnd_DateTime = __EffCalcEnd_DateTime;
    	}

    	if ( LEZeroInAverage == null ) {
    		LEZeroInAverage = _True;
    	}
    	boolean eff_ignore_le_zero = false;
    	if ( (LEZeroInAverage != null) && LEZeroInAverage.equalsIgnoreCase(_False) ) {
    		eff_ignore_le_zero = true;
    	}
    	PropList tsprops = new PropList ( "totals" );
    	if ( eff_ignore_le_zero ) {
    		tsprops.set ( "CheckRefTS=true" );
    		tsprops.set ( "IgnoreLEZero=true" );
    		tsprops.set ( "IgnoreRefLEZero=true" );
    	}

    	boolean eff_always_enforce_limits = false;	// Old default

    	double EffMin_double = 0.0;
    	if ( EffMin != null ) {
    		EffMin_double = Double.parseDouble ( EffMin );
    	}

    	double EffMax_double = 100.0;
    	if ( EffMax != null ) {
    		EffMax_double = Double.parseDouble ( EffMax );
    	}

    	double EffPrecision_double = 1.0; // Round to nearest 1%

    	// Set the months (default here is water year)...

    	int months[] = { 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    	// year1 and year2 are calendar years so we have already adjusted for water year previously
    	if ( outputYearType == YearType.WATER ) {
    		// Set the dates to query the proper monthly totals...
    		/* TODO - what??? is this needed or not
    		date1.setYear ( year1 );
    		date1.setMonth ( 10 );
    		date2.setYear ( year2 );
    		date2.setMonth ( 9 );
    		*/
    	}
    	else if ( outputYearType == YearType.NOV_TO_OCT ) {
    		months[0] = 11;
    		months[1] = 12;
    		months[2] = 1;
    		months[3] = 2;
    		months[4] = 3;
    		months[5] = 4;
    		months[6] = 5;
    		months[7] = 6;
    		months[8] = 7;
    		months[9] = 8;
    		months[10] = 9;
    		months[11] = 10;
    		// Set the dates to query the proper monthly totals...
    		/* TODO - what??? is this needed or not
    		date1.setYear ( year1 );
    		date1.setMonth ( 11 );
    		date2.setYear ( year2 );
    		date2.setMonth ( 10 );
    		*/
    	}
    	else if ( outputYearType == YearType.CALENDAR ){
    		// Calendar year...
    		months[0] = 1;
    		months[1] = 2;
    		months[2] = 3;
    		months[3] = 4;
    		months[4] = 5;
    		months[5] = 6;
    		months[6] = 7;
    		months[7] = 8;
    		months[8] = 9;
    		months[9] = 10;
    		months[10] = 11;
    		months[11] = 12;
    		/* What???
    		date1.setYear ( year1 );
    		date1.setMonth ( 1 );
    		date2.setYear ( year2 );
    		date2.setMonth ( 12 );
    		*/
    	}
    	else {
    		throw new RuntimeException ( "Year type " + outputYearType + " is not recognized" );
    	}

    	StateMod_Diversion div = null;
    	StateMod_Well well = null;
    	MonthTS h_ts, iwr_ts;
    	int pos = 0; // Position of time series in array
    	MonthTotals totals = null; // Totals used in calculations.
    	String id = null; // Diversion ID.

    	// For the following arrays, all raw data are stored Jan - Dec, Annual
    	// The following are stored according to the year type:
    	//
    	//	monthly_demand
    	//	monthly_diversion	 or pumping
    	//	efficiency_computed
    	//	efficiency_model

    	double [] avgvals = new double[13];
    	double [] monthly_demand = null; // Total demand
    	double [] monthly_diversion = null; // Total div
    	double [] efficiency_computed = null; // Computed
    	double [] efficiency_model = null; // After adjustment
    	double [] efficiency_stddev = null; // Eff stddev

    	// Used with detailed efficiency report...
    	int MAXYEARS = 200;
    	double [][]	dem_all = new double[MAXYEARS][13];
    	double [][]	div_all = new double[MAXYEARS][13];
    	double [][]	eff_all = new double[MAXYEARS][13];
    	// ... end used with detailed efficiency report
    	int j; // Loop counter for months
    	int demsrc = 0; // Diversion station "demsrc" value.

    	// Used with multistruct...

    	StateMod_Diversion div_part = null; // Secondary diversion.
    	List<String> partids = null; // Secondary IDs.
    	int collection_size = 0; // Number of parts...
    	String part_id; // Secondary ID.
    	double multistruct_area = 0.0; // Total area for a multistruct.
    	if ( multistruct_area < 0.0 ) {
    		// TODO put in to prevent compiler warning - should the value be output?
    	}
    	int matchCount = 0; // IDs that match
    	for ( int i = 0; i < stationListSize; i++ ) {
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    			div = ddsList.get(i);
    			id = div.getID();
    			demsrc = div.getDemsrc();
    		}
    		else if (compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			well = wesList.get(i);
    			id = well.getID();
    			demsrc = well.getDemsrcw();
    			if ( well.getIdvcomw() != 1 ) {
    				// Can only process well-only where demand time
    				// series are supplied for calculations to
    				// make sense...
    				continue;
    			}
    		}
    		if ( !id.matches(idpattern_Java) ) {
    			// Identifier does not match...
    			continue;
    		}
    		++matchCount;

    		// As per old demandts code, only process diversions and wells that are agricultural...

    		if ( (demsrc == StateMod_Diversion.DEMSRC_GIS) ||
    			(demsrc == StateMod_Diversion.DEMSRC_TIA) ||
    			(demsrc == StateMod_Diversion.DEMSRC_GIS_PRIMARY)||
    			(demsrc == StateMod_Diversion.DEMSRC_TIA_PRIMARY)||
    			(demsrc == StateMod_Diversion.DEMSRC_USER) ) {
    			Message.printStatus ( 2, routine, "Calculating average efficiencies for agricultural " +
    			stationType + " station \"" + id + "\"..." );
    		}
    		else if ( demsrc == StateMod_Diversion.DEMSRC_GIS_SECONDARY ) {
    			if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    				Message.printStatus ( 2, routine,
    				"Average efficiencies for MultiStruct() secondary diversion station \"" + id +
    				"\" are estimated when the primary is processed.  Not estimating from time series.");
    			}
    			else if(compType==StateMod_DataSet.COMP_WELL_STATIONS){
    				Message.printStatus ( 2, routine,
    				"Well has demand source for seconary GIS - well station \"" + id +
    				"\" Efficiencies should be set manually.  Not estimating from time series." );
    			}
    			continue;
    		}
    		else if((demsrc == StateMod_Diversion.DEMSRC_MI_TRANSBASIN) ||
    			(demsrc == StateMod_Diversion.DEMSRC_CARRIER) ) {
    			Message.printStatus ( 2, routine,
    			"Average efficiencies for M&I and carrier " + stationType + " station \"" + id +
    			"\" should be set manually.  Not estimating from time series." );
    			continue;
    		}
    		else {
    			// Unknown demand source...
    			message = "Demand source for " + stationType + " station \"" + id +
    			"\" (" + demsrc + ") is unknown.  Not estimating efficiencies from time series.";
		        Message.printWarning ( warning_level, 
		        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Verify that the demand source is a recognized value." ) );
    			continue;
    		}

    		// Get the historic diversion or pumping time series and an irrigation demand time series...

    		h_ts = null;
    		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    			pos = TSUtil.indexOf( histTSList, id,"Location",0);
    			if ( pos < 0 ) {
    				// No historic diversion time series is available...
    				message = "No diversion historical TS (monthly) available for \""+ id +
    				"\".  Skipping efficiency calculation.";
    		        Message.printWarning ( warning_level, 
    		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.WARNING,
			                message, "Verify that a historical diversion time series is available." ) );
    				continue;
    			}
    			h_ts = (MonthTS)histTSList.get(pos);
    			div.setDiversionMonthTS ( h_ts );
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			pos = TSUtil.indexOf(histTSList,id,"Location",0);
    			if ( pos < 0 ) {
    				// No historic pumping time series is available...
    				message = "No well historical pumping TS (monthly) available for \""+ id +
    				"\".  Skipping efficiency calculation.";
    		        Message.printWarning ( warning_level, 
    		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.WARNING,
			                message, "Verify that a historical well pumping time series is available." ) );
    				continue;
    			}
    			h_ts = (MonthTS)histTSList.get(pos);
    			well.setPumpingMonthTS ( h_ts );
    		}
    		if ( h_ts == null ) {
    			// No historic diversion/pumping time series is available...
    			message = "No " + stationType + " historical TS (monthly) available for \""+
    			id + "\".  Skipping efficiency calculation.";
		        Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Verify that a historical time series is available." ) );
    			continue;
    		}
    		// Same whether diversion or well...
    		pos = TSUtil.indexOf( cwrTSList, id, "Location", 0 );
    		if ( pos < 0 ) {
    			// No diversion CWR/IWR time series is available...
    			message = "No IWR (CWR) time series (monthly) is available for \""+
    			id + "\".  Skipping efficiency calculation.";
		        Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Verify that an IWR (CWR) time series is available." ) );
    			continue;
    		}
    		iwr_ts = cwrTSList.get(pos);
    		if(compType==StateMod_DataSet.COMP_DIVERSION_STATIONS){
    			div.setConsumptiveWaterRequirementMonthTS ( iwr_ts );
    		}
    		else if(compType==StateMod_DataSet.COMP_WELL_STATIONS){
    			well.setConsumptiveWaterRequirementMonthTS ( iwr_ts );
    		}
    		if ( iwr_ts == null ) {
    			// No diversion CWR/IWR time series is available...
    			message = "No " + stationType + " CWR/IWR TS (monthly) available for \""+
    			id + "\".  Skipping efficiency calculation.";
		        Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.WARNING,
		                message, "Verify that an IWR (CWR) time series is available." ) );
    			continue;
    		}

    		// Add to the diversion historical and demand time series if a MultiStruct...

    		if ( (compType == StateMod_DataSet.COMP_DIVERSION_STATIONS) && div.isCollection() &&
    			div.getCollectionType().equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT)) {
    			// First clone the time series so they are not changed...
    			MonthTS ddh_ts2 = (MonthTS)h_ts.clone();
    			MonthTS iwr_ts2 = (MonthTS)iwr_ts.clone();
    			// Now loop through the parts and add the historical time series for the parts.
    			// Also add the demand time series and area.
    			multistruct_area = div.getArea();
    			partids = div.getCollectionPartIDs(0);
    			collection_size = 0;
    			if ( partids != null ) {
    				collection_size = partids.size();
    			}
    			for ( int ic = 0; ic < collection_size; ic++ ) {
    				part_id = partids.get(ic);
    				// Diversion (for area)...
    				pos = StateMod_Util.indexOf ( ddsList, part_id );
    				if ( pos < 0 ) {
    					message = "No diversion station found as MultiStruct part \"" +
    					part_id + "\" area for weighted average may be in error.";
    			        Message.printWarning ( warning_level, 
				        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        status.addToLog ( command_phase,
				            new CommandLogRecord(CommandStatusType.WARNING,
				                message, "Verify that MultiStruct part is defined and has area." ) );
    				}
    				else {
    					div_part = ddsList.get(pos);
    					if ( div.getArea() > 0.0 ) {
    						multistruct_area += div_part.getArea();
    					}
    				}
    				// Diversion historical time series...
    				pos = TSUtil.indexOf ( histTSList, part_id, "Location", 0 );
    				if ( pos < 0 ) {
    					// No diversion time series is available...
    					message = "No diversion historical TS (monthly) available for \""+
    					id + "\" MultiStruct part \"" + part_id + "\".  Results may be in error.";
    			        Message.printWarning ( warning_level, 
				        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        status.addToLog ( command_phase,
				            new CommandLogRecord(CommandStatusType.WARNING,
				                message, "Verify that MultiStruct part has a histoircal time series defined." ) );
    					continue;
    				}
    				else {
    					// Add the part to the total...
    					Message.printStatus ( 2, routine, "Diversion historical TS (monthly) for \""+
    					id + "\": adding MultiStruct part \"" + part_id + " historical time series." );
    					ddh_ts2 = (MonthTS)TSUtil.add ( ddh_ts2, (MonthTS)histTSList.get(pos) );
    				}
    				// IWR time series...
    				pos = TSUtil.indexOf ( cwrTSList, part_id, "Location", 0 );
    				if ( pos < 0 ) {
    					// No IWR time series is available...
    					message = "No diversion IWR/CWR TS (monthly) available for \""+ id + "\" MultiStruct part \"" +
    					part_id + "\".  Results may be in error.";
    					Message.printWarning ( warning_level, 
				        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        status.addToLog ( command_phase,
				            new CommandLogRecord(CommandStatusType.WARNING,
				                message, "Verify that MultiStruct part has an IWR (CWR) time series defined." ) );
    					continue;
    				}
    				else {
    					// Add the part to the total...
    					Message.printStatus ( 2, routine, "Diversion IWR (CWR) TS (monthly) for \""+
    					id + "\": adding MultiStruct part \"" + part_id + " demand time series." );
    					iwr_ts2 = (MonthTS)TSUtil.add ( iwr_ts2, (MonthTS)cwrTSList.get(pos) );
    				}
    			}
    			// Set the time series references to point to the clone, for the following logic...
    			h_ts = ddh_ts2;
    			iwr_ts = iwr_ts2;
    		}

    		// Have the historic diversions (or pumping) and CWR/IWR time series.
    		// Initialize the structure's efficiency data (monthly and annual)...
    		// Create a new array for data because we do not want the arrays
    		// to be reused (unique values need to be available later)...

    		monthly_demand = new double[13];
    		monthly_diversion = new double[13];
    		efficiency_model = new double[13];
    		efficiency_computed = new double[13];
    		efficiency_stddev = new double[13];
    		for ( j = 0; j < 13; j++ ) {
    			monthly_demand[j] = 0.0;
    			monthly_diversion[j] = 0.0;
    			efficiency_model[j] = 0.0;
    			efficiency_computed[j] = 0.0;
    			efficiency_stddev[j] = 0.0;
    		}

    		// Sum the results for each month for each time series.  Use averages for calculations.
    		try {
    			// Use the demand time series to control missing...
    			totals = TSUtil.getMonthTotals ( h_ts, EffCalcStart_DateTime,
    				EffCalcEnd_DateTime, iwr_ts, tsprops );
    		}
    		catch ( Exception e ) {
    			message = "Unable to get monthly average TS values for " + stationType + " \"" +
    			id + "\".  Skipping efficiency calculation";
				Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Verify that historical and demand time series have at least some data." ) );
    			continue;
    		}
    		//numtotal = totals.getNumTotal();
    		avgvals = totals.getAverages();
    		for ( j = 0; j < 12; j++ ) {
    			// Set the monthly value...
    			monthly_diversion[j] = avgvals[months[j] - 1];
    			// Set the annual value...
    			if ( avgvals[months[j] - 1] > 0.0 ) {
    				monthly_diversion[12] = monthly_diversion[12] + avgvals[months[j] - 1];
    			}
    		}
    		try {
    			// Use the historic time series to control missing...
    			totals = TSUtil.getMonthTotals ( iwr_ts, EffCalcStart_DateTime,
    				EffCalcEnd_DateTime, h_ts, tsprops );
    		}
    		catch ( Exception e ) {
    			message = "Unable to get monthly average TS values for \"" +
    			id + "\" CWR/IWR.  Skipping eff. calc.";
    			Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Verify that IWR (CWR) time series have at least some data." ) );
    			continue;
    		}
    		//numtotal = totals.getNumTotal();
    		avgvals = totals.getAverages();
    		for ( j = 0; j < 12; j++ ) {
    			// Monthly value, regardless of whether missing...
    			monthly_demand[j] = avgvals[months[j] - 1];
    			// Annual value (ignore missing)...
    			if ( avgvals[months[j] - 1] > 0.0 ) {
    				monthly_demand[12] = monthly_demand[12] + avgvals[months[j] - 1];
    			}
    		}
    		// Get the detail information so that have standard deviations.
    		// The yearly values will have to be regenerated
    		// in another routine if the high detail output is desired.  For
    		// the most part, the information in the following is not used
    		// (the demand and diversion totals from above are).

    		// TODO SAM 2004-09-06 - this is apparently needed
    		// ONLY to get the standard deviation of values for the
    		// full period, to store with the diversion station.
    		// This code could be optimized.
    		// StateDMI will either print no efficiency report or the high detail report
    		if ( EffReportFile != null ) {
    			try {
    				calculateEfficiencyDetail ( (i + 1),
					iwr_ts,
					h_ts,
					id,
					outputYearType,
					EffCalcStart_DateTime.getYear(),
					EffCalcEnd_DateTime.getYear(),
					dem_all, div_all, eff_all,
					efficiency_stddev,
					eff_ignore_le_zero );
    			}
    			catch ( Exception e ) {
    				Message.printWarning ( 3, routine, e );
    				message = "Unexpected error calculating efficiency detail for \"" + id + "\" (" + e + ").";
    				Message.printWarning ( warning_level, 
			        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Check the log file for details." ) );
    			}
    		}
    		// Calculate the efficiencies based on the entire period of
    		// record.  Note that we do not care which months have zero
    		// flows.  We are just using total numbers.  The annual values
    		// will in all likelihood not be equal to the average of the
    		// individual months because the months have different overall
    		// flows which weight the annual values differently.
    		for ( j = 0; j < 13; j++ ) {
    			if ( monthly_diversion[j] <= 0.0 ) {
    				efficiency_computed[j] = 0.0;
    			}
    			else {
    				efficiency_computed[j] = 100.0*(monthly_demand[j]/monthly_diversion[j]);
    			}
    			if ( j == 12 ) {
    				message = id + ":  Annual efficiency computed as " +
    				StringUtil.formatString(monthly_demand[j],"%.6f") + "/" +
    				StringUtil.formatString(monthly_diversion[j],"%.6f")+ "=" +
    				StringUtil.formatString(efficiency_computed[j],"%.6f");
    			}
    			else {
    				message = id + ":  Month " + (j + 1) + " efficiency computed as " +
    				StringUtil.formatString(monthly_demand[j],"%.6f") + "/" +
    				StringUtil.formatString(monthly_diversion[j],"%.6f")+ "=" +
    				StringUtil.formatString(efficiency_computed[j],"%.6f");
    			}
    			if ( Message.isDebugOn ) {
    				Message.printDebug ( 10, routine, message );
    			}
    		}
    		// Make sure that the efficiencies are reasonable for
    		// modeling purposes.  Currently, this means that the value
    		// must be between "eff_low" and "eff_high".  Only do this if
    		// we are calculating efficiencies from time series data (if
    		// from the file we will not even be at this point)...
    		//
    		// Start by copying the computed efficiencies to the model efficiencies...
    		for ( j = 0; j < 13; j++ ) {
    			efficiency_model[j] = efficiency_computed[j];
    		}
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( 10, routine, "Checking agricultural efficiency values for reasonableness..." );
    		}
    		for ( j = 0; j < 13; j++ ) {
    			if ( (monthly_demand[j] <= 0.0) && !eff_always_enforce_limits ) {
    				// OK to pass on zero demands as zero efficiency...
    				;
    			}
    			else if ( efficiency_model[j] < EffMin_double ) {
    				if ( j == 12 ) {
    					message = id + ":  Annual:  Changing efficiency from "+
    					StringUtil.formatString ( efficiency_model[j], "%.2f" ) + " to minimum:  " + EffMin;
    				}
    				else {
    					message = id + ":  Month " + (j + 1) + ":  Changing efficiency from " +
    					StringUtil.formatString (efficiency_model[j], "%.2f" ) + " to minimum:  " + EffMin;
    				}
    				Message.printStatus ( 2, routine, message );
    				efficiency_model[j] = EffMin_double;
    			}
    			else if ( efficiency_model[j] > EffMax_double ) {
    				if ( j == 12 ) {
    					message = id + ":  Annual:  Changing efficiency from "+
    					StringUtil.formatString (efficiency_model[j], "%.2f" ) + " to maximum:  " + EffMax;
    				}
    				else {
    					message = id + ":  Month " + (j + 1) + ":  Changing efficiency from " +
    					StringUtil.formatString (efficiency_model[j], "%.2f" ) + " to maximum:  " + EffMax;
    				}
    				Message.printStatus ( 2, routine, message );
    				efficiency_model[j] = EffMax_double;
    			}

    			// Now make sure that the efficiencies are set to even percent values...

    			try {
    				efficiency_model[j] = MathUtil.roundToPercent ( efficiency_model[j], EffPrecision_double, 0, 0 );
    			}
    			catch ( Exception e ) {
    				Message.printWarning ( 3, routine, e );
    				message = "Unexpected error rounding " + efficiency_model[j] + "(" + e + ")";
    				Message.printWarning ( warning_level, 
			        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
			        status.addToLog ( command_phase,
			            new CommandLogRecord(CommandStatusType.FAILURE,
			                message, "Report to software support" ) );
    			}
    		}
    		// Now we have the "structure" efficiencies but we need to store in the "diversion" or "well"
    		// data.  The arrays are already in the year type that should correspond to the
    		// diversion/well stations so just transfer across...

    		// Set the annual to negative, to indicate that monthly efficiencies will be used...

    		if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    			div.setDivefc ( -efficiency_model[12] );

    			// Now set the monthly efficiencies...

    			for ( j = 0; j < 12; j++ ) {
    				div.setDiveff ( j, efficiency_model[j] );
    			}

    			// Also save information that is used for the detailed report...

    			div.setAverageMonthlyCWR ( monthly_demand );
    			div.setAverageMonthlyHistoricalDiversions ( monthly_diversion );
    			div.setCalculatedEfficiencies ( efficiency_computed );
    			div.setCalculatedEfficiencyStddevs ( efficiency_stddev );
    			div.setModelEfficiencies ( efficiency_model );
    		}
    		else if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			well.setDivefcw ( -efficiency_model[12] );

    			// Now set the monthly efficiencies...

    			for ( j = 0; j < 12; j++ ) {
    				well.setDiveff ( j, efficiency_model[j] );
    			}

    			// Also save information that is used for the detailed report...

    			well.setAverageMonthlyCWR ( monthly_demand );
    			well.setAverageMonthlyHistoricalPumping ( monthly_diversion );
    			well.setCalculatedEfficiencies ( efficiency_computed );
    			well.setCalculatedEfficiencyStddevs(efficiency_stddev );
    			well.setModelEfficiencies ( efficiency_model );
    		}

    		// For MultiStruct, set the average efficiencies in the secondary parts.
    		// The part data will have been set above for the first part of the check...

    		if ( (compType ==StateMod_DataSet.COMP_DIVERSION_STATIONS)&& div.isCollection() &&
    			div.getCollectionType().equalsIgnoreCase(StateMod_Diversion.COLLECTION_TYPE_MULTISTRUCT)) {
    			for ( int ic = 0; ic < collection_size; ic++ ) {
    				part_id = partids.get(ic);
    				pos = StateMod_Util.indexOf ( wesList, part_id );
    				if ( pos < 0 ) {
    					message = "No diversion station found as MultiStruct part \"" +
    					part_id + "\" not setting average efficiencies for the part.";
    	   				Message.printWarning ( warning_level, 
				        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
				        status.addToLog ( command_phase,
				            new CommandLogRecord(CommandStatusType.WARNING,
				                message, "Verify that MultiStruct parts are valid identifiers." ) );
    					continue;
    				}
    				div_part = ddsList.get(pos);
    				div_part.setAverageMonthlyCWR (	(double [])monthly_demand.clone() );
    				div_part.setAverageMonthlyHistoricalDiversions ((double [])monthly_diversion.clone() );
    				div_part.setCalculatedEfficiencies ((double [])efficiency_computed.clone());
    				div_part.setCalculatedEfficiencyStddevs ((double [])efficiency_stddev.clone() );
    				div_part.setModelEfficiencies (	(double [])efficiency_model.clone() );
    				div_part.setDivefc ( div.getDivefc() );
    				for ( int ie = 0; ie < 12; ie++ ) {
    					div_part.setDiveff (ie,div.getDiveff(ie) );
    				}
    			}
    		}
    	}
    	if ( EffReportFile != null ) {
    		try {
    			Message.printStatus ( 2, routine, "Printing efficiency report file \"" + EffReportFile + "\"..." );
    			if ( compType == StateMod_DataSet.COMP_DIVERSION_STATIONS ) {
    				printEfficiencyReport (
    				EffReportFile_full,
    				ddsList,
    				null,
    				EffCalcStart_DateTime.getMonth(),
    				EffCalcStart_DateTime.getYear(),
    				EffCalcEnd_DateTime.getMonth(),
    				EffCalcEnd_DateTime.getYear(),
    				outputYearType,
    				true,	// rep_detail, Always do high detail when report is requested
    				EffMin_double,	// eff_low,
    				EffMax_double,	//eff_high,
    				1.0,		// eff_round0,
    				false,		// eff_create_stm,
    				eff_ignore_le_zero );
    			}
    			else if(compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    				printEfficiencyReport (
    				EffReportFile_full,
    				null,
    				wesList,
    				EffCalcStart_DateTime.getMonth(),
    				EffCalcStart_DateTime.getYear(),
    				EffCalcEnd_DateTime.getMonth(),
    				EffCalcEnd_DateTime.getYear(),
    				outputYearType,
    				true,	// rep_detail, Always do high detail when report is requested
    				EffMin_double,	// eff_low,
    				EffMax_double,	//eff_high,
    				1.0,		// eff_round0,
    				false,		// eff_create_stm,
    				eff_ignore_le_zero );
    			}
    			// Set the filename for the FileGenerator interface
    	    	setOutputFile ( new File(EffReportFile_full) );
    		}
    		catch ( Exception e ) {
    			Message.printWarning ( 3, routine, e );
    			message = "Error printing efficiency report file \"" + EffReportFile + "\" (" + e + ").";
   				Message.printWarning ( warning_level, 
		        	MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
		        status.addToLog ( command_phase,
		            new CommandLogRecord(CommandStatusType.FAILURE,
		                message, "Check the log file - if necessary, report to software support." ) );
    		}
    	}
    	if ( matchCount == 0 ) {
    		String note = "";
    		if ( compType == StateMod_DataSet.COMP_WELL_STATIONS ) {
    			note = "  Only well stations where demand type is monthly total demand are processed.";
    		}
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Identifier \"" + ID + "\" was not matched: warning and not calculating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct and stations have been read/set." + note ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Identifier \"" + ID +	"\" was not matched: failing and not calculating.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct and stations have been read/set." + note ) );
			}
    	}
    }
    catch ( Exception e ) {
        Message.printWarning ( log_level, routine, e );
        message = "Unexpected error processing data (" + e + ").";
        Message.printWarning ( warning_level, 
        MessageUtil.formatMessageTag(command_tag, ++warning_count), routine, message );
        status.addToLog ( command_phase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "See log file for details." ) );
        throw new CommandException ( message );
    }
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
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

	String ID = parameters.getValue ( "ID" );
	String EffMin = parameters.getValue ( "EffMin" );
	String EffMax = parameters.getValue ( "EffMax" );
	String EffCalcStart = parameters.getValue ( "EffCalcStart" );
	String EffCalcEnd = parameters.getValue ( "EffCalcEnd" );
	String LEZeroInAverage = parameters.getValue ( "LEZeroInAverage" );
	String EffReportFile = parameters.getValue ( "EffReportFile" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();
	if ( (ID != null) && (ID.length() > 0) ) {
		b.append ( "ID=\"" + ID + "\"" );
	}
	if ( (EffMin != null) && (EffMin.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffMin=" + EffMin );
	}
	if ( (EffMax != null) && (EffMax.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffMax=" + EffMax );
	}
	if ( (EffCalcStart != null) && (EffCalcStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffCalcStart=" + EffCalcStart );
	}
	if ( (EffCalcEnd != null) && (EffCalcEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffCalcEnd=" + EffCalcEnd );
	}
	if ( (LEZeroInAverage != null) && (LEZeroInAverage.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LEZeroInAverage=" + LEZeroInAverage );
	}
	if ( (EffReportFile != null) && (EffReportFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EffReportFile=\"" + EffReportFile + "\"" );
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
