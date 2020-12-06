// StateDMICommandFactory - This class instantiates Commands for StateDMI processing.

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

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandFactory;
import RTi.Util.IO.UnknownCommand;
import RTi.Util.IO.UnknownCommandException;
import RTi.Util.Message.Message;
import rti.tscommandprocessor.commands.check.WriteCheckFile_Command;
import rti.tscommandprocessor.commands.hydrobase.OpenHydroBase_Command;
import rti.tscommandprocessor.commands.logging.Message_Command;
import rti.tscommandprocessor.commands.logging.SetDebugLevel_Command;
import rti.tscommandprocessor.commands.logging.SetWarningLevel_Command;
import rti.tscommandprocessor.commands.logging.StartLog_Command;
import rti.tscommandprocessor.commands.r.RunR_Command;
import rti.tscommandprocessor.commands.spatial.WriteTableToGeoJSON_Command;
import rti.tscommandprocessor.commands.spatial.WriteTableToShapefile_Command;
import rti.tscommandprocessor.commands.spreadsheet.CloseExcelWorkbook_Command;
import rti.tscommandprocessor.commands.spreadsheet.NewExcelWorkbook_Command;
import rti.tscommandprocessor.commands.spreadsheet.ReadExcelWorkbook_Command;
import rti.tscommandprocessor.commands.spreadsheet.ReadPropertiesFromExcel_Command;
import rti.tscommandprocessor.commands.spreadsheet.ReadTableCellsFromExcel_Command;
import rti.tscommandprocessor.commands.spreadsheet.ReadTableFromExcel_Command;
import rti.tscommandprocessor.commands.spreadsheet.SetExcelCell_Command;
import rti.tscommandprocessor.commands.spreadsheet.SetExcelWorksheetViewProperties_Command;
import rti.tscommandprocessor.commands.spreadsheet.WriteTableCellsToExcel_Command;
import rti.tscommandprocessor.commands.spreadsheet.WriteTableToExcel_Command;
import rti.tscommandprocessor.commands.table.AppendTable_Command;
import rti.tscommandprocessor.commands.table.CompareTables_Command;
import rti.tscommandprocessor.commands.table.CopyPropertiesToTable_Command;
import rti.tscommandprocessor.commands.table.CopyTable_Command;
import rti.tscommandprocessor.commands.table.DeleteTableColumns_Command;
import rti.tscommandprocessor.commands.table.DeleteTableRows_Command;
import rti.tscommandprocessor.commands.table.FormatTableDateTime_Command;
import rti.tscommandprocessor.commands.table.FormatTableString_Command;
import rti.tscommandprocessor.commands.table.FreeTable_Command;
import rti.tscommandprocessor.commands.table.InsertTableColumn_Command;
import rti.tscommandprocessor.commands.table.InsertTableRow_Command;
import rti.tscommandprocessor.commands.table.JoinTables_Command;
import rti.tscommandprocessor.commands.table.ManipulateTableString_Command;
import rti.tscommandprocessor.commands.table.NewTable_Command;
import rti.tscommandprocessor.commands.table.ReadTableFromDBF_Command;
import rti.tscommandprocessor.commands.table.ReadTableFromDataStore_Command;
import rti.tscommandprocessor.commands.table.ReadTableFromDelimitedFile_Command;
import rti.tscommandprocessor.commands.table.ReadTableFromFixedFormatFile_Command;
import rti.tscommandprocessor.commands.table.SetPropertyFromTable_Command;
import rti.tscommandprocessor.commands.table.SetTableValues_Command;
import rti.tscommandprocessor.commands.table.SortTable_Command;
import rti.tscommandprocessor.commands.table.SplitTableColumn_Command;
import rti.tscommandprocessor.commands.table.SplitTableRow_Command;
import rti.tscommandprocessor.commands.table.TableMath_Command;
import rti.tscommandprocessor.commands.table.WriteTableToDelimitedFile_Command;
import rti.tscommandprocessor.commands.table.WriteTableToHTML_Command;
import rti.tscommandprocessor.commands.time.SetOutputPeriod_Command;
import rti.tscommandprocessor.commands.time.SetOutputYearType_Command;
import rti.tscommandprocessor.commands.util.AppendFile_Command;
import rti.tscommandprocessor.commands.util.CommentBlockEnd_Command;
import rti.tscommandprocessor.commands.util.CommentBlockStart_Command;
import rti.tscommandprocessor.commands.util.Comment_Command;
import rti.tscommandprocessor.commands.util.CompareFiles_Command;
import rti.tscommandprocessor.commands.util.CopyFile_Command;
import rti.tscommandprocessor.commands.util.Exit_Command;
import rti.tscommandprocessor.commands.util.FTPGet_Command;
import rti.tscommandprocessor.commands.util.FormatDateTimeProperty_Command;
import rti.tscommandprocessor.commands.util.FormatStringProperty_Command;
import rti.tscommandprocessor.commands.util.ListFiles_Command;
import rti.tscommandprocessor.commands.util.MergeListFileColumns_Command;
import rti.tscommandprocessor.commands.util.RemoveFile_Command;
import rti.tscommandprocessor.commands.util.RunProgram_Command;
import rti.tscommandprocessor.commands.util.RunPython_Command;
import rti.tscommandprocessor.commands.util.SetProperty_Command;
import rti.tscommandprocessor.commands.util.SetWorkingDir_Command;
import rti.tscommandprocessor.commands.util.UnzipFile_Command;
import rti.tscommandprocessor.commands.util.WebGet_Command;
import rti.tscommandprocessor.commands.util.WritePropertiesToFile_Command;
import RTi.Util.String.StringUtil;

/**
This class instantiates Commands for StateDMI processing.  The full command name
is required, but parameters are not because parsing does not occur.
*/
public class StateDMICommandFactory implements CommandFactory
{
	
/**
Return a new command, based on the command name.  DO NOT create an
UnknownCommand if the command is not recognized.
@return a new command, based on the command name.
@param command_string The command string to process.
@throws UnknownCommandException if the command name is not recognized.
*/
public Command newCommand ( String command_string )
throws UnknownCommandException
{
	return newCommand ( command_string, false );
}

/**
Return a new command, based on the command name.
@return a new command, based on the command name.
@param commandString The command string to process.  The command string can
contain parameters but they are not parsed.  At a minimum, the command string needs
to be of the form "CommandName()" or "TS Alias = CommandName()".
@param createUnknownCommandIfNotRecognized If true and the command is
not recognized, create an UnknownCommand instance that holds the command string.
This is useful for code that is being migrated to the full command class design.
@throws UnknownCommandException if the command name is not recognized
(and createUnknownCommandIfNotRecognized=false).
*/
public Command newCommand ( String commandString, boolean createUnknownCommandIfNotRecognized )
throws UnknownCommandException
{	String routine = getClass().getSimpleName() + ".newCommand";
	commandString = commandString.trim();

	// Comment commands...
	
	if ( commandString.trim().startsWith("#") ) {
	    return new Comment_Command ();
	}
	else if ( commandString.startsWith("/*") ) {
	    return new CommentBlockStart_Command ();
	}
	else if ( commandString.startsWith("*/") ) {
	    return new CommentBlockEnd_Command ();
	}

	// "a" commands
	else if ( StringUtil.startsWithIgnoreCase( commandString, "AggregateWellRights") ){
		return new AggregateWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "AppendFile") ) {
        return new AppendFile_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "AppendNetwork") ){
		return new AppendNetwork_Command ();
	}
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "AppendTable") ) {
        return new AppendTable_Command ();
    }

	// "c" commands ...
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateDiversionDemandTSMonthlyAsMax") ) {
		return new CalculateDiversionDemandTSMonthlyAsMax_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateDiversionDemandTSMonthly") ) {
		return new CalculateDiversionDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateDiversionStationEfficiencies") ) {
		return new CalculateDiversionStationEfficiencies_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateStreamEstimateCoefficients") ) {
		return new CalculateStreamEstimateCoefficients_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateWellDemandTSMonthlyAsMax") ) {
		return new CalculateWellDemandTSMonthlyAsMax_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateWellDemandTSMonthly") ) {
		return new CalculateWellDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CalculateWellStationEfficiencies") ) {
		return new CalculateWellStationEfficiencies_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckBlaneyCriddle") ) {
		return new CheckBlaneyCriddle_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckClimateStations") ) {
		return new CheckClimateStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckCropCharacteristics") ) {
		return new CheckCropCharacteristics_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckCropPatternTS") ) {
		return new CheckCropPatternTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckCULocations") ) {
		return new CheckCULocations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckDiversionDemandTSMonthly") ) {
		return new CheckDiversionDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckDiversionHistoricalTSMonthly") ) {
		return new CheckDiversionHistoricalTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckDiversionRights") ) {
		return new CheckDiversionRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckDiversionStations") ) {
		return new CheckDiversionStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckInstreamFlowDemandTSAverageMonthly") ) {
		return new CheckInstreamFlowDemandTSAverageMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckInstreamFlowRights") ) {
		return new CheckInstreamFlowRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckInstreamFlowStations") ) {
		return new CheckInstreamFlowStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckIrrigationPracticeTS") ) {
		return new CheckIrrigationPracticeTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckIrrigatedLands") ) {
		return new CheckIrrigatedLands_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckParcels") ) {
		return new CheckParcels_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckPenmanMonteith") ) {
		return new CheckPenmanMonteith_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckReservoirRights") ) {
		return new CheckReservoirRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckReservoirStations") ) {
		return new CheckReservoirStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckRiverNetwork") ) {
		return new CheckRiverNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckStreamEstimateCoefficients") ) {
		return new CheckStreamEstimateCoefficients_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckStreamEstimateStations") ) {
		return new CheckStreamEstimateStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckStreamGageStations") ) {
		return new CheckStreamGageStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckWellDemandTSMonthly") ) {
		return new CheckWellDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckWellHistoricalPumpingTSMonthly") ) {
		return new CheckWellHistoricalPumpingTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckWellRights") ) {
		return new CheckWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CheckWellStations") ) {
		return new CheckWellStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "CloseExcelWorkbook") ) {
		return new CloseExcelWorkbook_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CompareFiles") ) {
		return new CompareFiles_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"CompareTables") ) {
		return new CompareTables_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "CopyFile") ) {
        return new CopyFile_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "CopyPropertiesToTable") ) {
        return new CopyPropertiesToTable_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "CopyTable") ) {
        return new CopyTable_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase(commandString,"CreateCropPatternTSForCULocations") ) {
		return new CreateCropPatternTSForCULocations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase(commandString,"CreateIrrigationPracticeTSForCULocations") ) {
		return new CreateIrrigationPracticeTSForCULocations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase(commandString,"CreateNetworkFromRiverNetwork") ) {
		return new CreateNetworkFromRiverNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase(commandString,"CreateRegressionTestCommandFile") ) {
		return new CreateRegressionTestCommandFile_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase(commandString,"CreateRiverNetworkFromNetwork") ) {
		return new CreateRiverNetworkFromNetwork_Command ();
	}
	
	// "d" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "DeleteTableColumns") ) {
        return new DeleteTableColumns_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "DeleteTableRows") ) {
        return new DeleteTableRows_Command ();
    }
	
	// "e" commands ...
	else if ( StringUtil.startsWithIgnoreCase(commandString,"Exit") ||
	    StringUtil.startsWithIgnoreCase(commandString,"Exit")) {
		return new Exit_Command ();
	}
	
	// "f" commands ...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillClimateStationsFromHydroBase") ) {
		return new FillClimateStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillClimateStation") ) {
		return new FillClimateStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCropPatternTSConstant") ) {
		return new FillCropPatternTSConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCropPatternTSInterpolate") ) {
		return new FillCropPatternTSInterpolate_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCropPatternTSProrateAgStats") ) {
		return new FillCropPatternTSProrateAgStats_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCropPatternTSRepeat") ) {
		return new FillCropPatternTSRepeat_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCropPatternTSUsingWellRights") ) {
		return new FillCropPatternTSUsingWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCULocationClimateStationWeights") ) {
		return new FillCULocationClimateStationWeights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCULocationsFromHydroBase") ) {
		return new FillCULocationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCULocationsFromList") ) {
		return new FillCULocationsFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillCULocation") ) {
		return new FillCULocation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionDemandTSMonthlyAverage") ) {
		return new FillDiversionDemandTSMonthlyAverage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionDemandTSMonthlyConstant") ) {
		return new FillDiversionDemandTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionDemandTSMonthlyPattern") ) {
		return new FillDiversionDemandTSMonthlyPattern_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionHistoricalTSMonthlyAverage") ) {
		return new FillDiversionHistoricalTSMonthlyAverage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionHistoricalTSMonthlyConstant") ) {
		return new FillDiversionHistoricalTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionHistoricalTSMonthlyPattern") ) {
		return new FillDiversionHistoricalTSMonthlyPattern_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionRight") ) {
		return new FillDiversionRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionStationsFromHydroBase") ) {
		return new FillDiversionStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionStationsFromNetwork") ) {
		return new FillDiversionStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillDiversionStation") ) {
		return new FillDiversionStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillInstreamFlowRight") ) {
		return new FillInstreamFlowRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillInstreamFlowStationsFromHydroBase") ) {
		return new FillInstreamFlowStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillInstreamFlowStationsFromNetwork") ) {
		return new FillInstreamFlowStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillInstreamFlowStation") ) {
		return new FillInstreamFlowStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillIrrigationPracticeTSInterpolate") ) {
		return new FillIrrigationPracticeTSInterpolate_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillIrrigationPracticeTSRepeat") ) {
		return new FillIrrigationPracticeTSRepeat_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillNetworkFromHydroBase") ) {
		return new FillNetworkFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "FillPlanStation")) {
		return new FillPlanStation_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillReservoirRight") ) {
		return new FillReservoirRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillReservoirStationsFromHydroBase") ) {
		return new FillReservoirStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillReservoirStationsFromNetwork") ) {
		return new FillReservoirStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillRiverNetworkFromHydroBase") ) {
		return new FillRiverNetworkFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillRiverNetworkFromNetwork") ) {
		return new FillRiverNetworkFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillRiverNetworkNode") ) {
		return new FillRiverNetworkNode_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillReservoirStation") ) {
		return new FillReservoirStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamEstimateStationsFromHydroBase") ) {
		return new FillStreamEstimateStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamEstimateStationsFromNetwork") ) {
		return new FillStreamEstimateStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamEstimateStation") ) {
		return new FillStreamEstimateStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamGageStationsFromHydroBase") ) {
		return new FillStreamGageStationsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamGageStationsFromNetwork") ) {
		return new FillStreamGageStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillStreamGageStation") ) {
		return new FillStreamGageStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellDemandTSMonthlyAverage") ) {
		return new FillWellDemandTSMonthlyAverage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellDemandTSMonthlyConstant") ) {
		return new FillWellDemandTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellDemandTSMonthlyPattern") ) {
		return new FillWellDemandTSMonthlyPattern_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellHistoricalPumpingTSMonthlyAverage") ) {
		return new FillWellHistoricalPumpingTSMonthlyAverage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellHistoricalPumpingTSMonthlyConstant") ) {
		return new FillWellHistoricalPumpingTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellHistoricalPumpingTSMonthlyPattern") ) {
		return new FillWellHistoricalPumpingTSMonthlyPattern_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellRight") ) {
		return new FillWellRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellStationsFromDiversionStations") ) {
		return new FillWellStationsFromDiversionStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellStationsFromNetwork") ) {
		return new FillWellStationsFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillWellStation") ) {
		return new FillWellStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"FillIrrigationPracticeTSAcreageUsingWellRights") ) {
		return new FillIrrigationPracticeTSAcreageUsingWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "FormatDateTimeProperty" ) ){
		return new FormatDateTimeProperty_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "FormatStringProperty" ) ){
		return new FormatStringProperty_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "FormatTableDateTime") ) {
        return new FormatTableDateTime_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "FormatTableString") ) {
        return new FormatTableString_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "FreeTable") ) {
        return new FreeTable_Command ();
    }
	else if (StringUtil.startsWithIgnoreCase( commandString, "FTPGet") ) {
		return new FTPGet_Command ();
	}
	
	// "i" commands...
	else if ( StringUtil.startsWithIgnoreCase( commandString, "InsertTableColumn") ) {
        return new InsertTableColumn_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "InsertTableRow") ) {
        return new InsertTableRow_Command ();
    }
	
	// "j" commands...

    else if ( StringUtil.startsWithIgnoreCase( commandString, "JoinTables") ) {
        return new JoinTables_Command ();
    }
	
	// "l" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString,"LimitDiversionDemandTSMonthlyToRights") ){
		return new LimitDiversionDemandTSMonthlyToRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"LimitDiversionHistoricalTSMonthlyToRights") ){
		return new LimitDiversionHistoricalTSMonthlyToRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"LimitIrrigationPracticeTSToRights") ){
		return new LimitIrrigationPracticeTSToRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"LimitWellDemandTSMonthlyToRights") ){
		return new LimitWellDemandTSMonthlyToRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"LimitWellHistoricalPumpingTSMonthlyToRights") ){
		return new LimitWellHistoricalPumpingTSMonthlyToRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ListFiles" ) ) {
		return new ListFiles_Command ();
	}

	// "m" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ManipulateTableString") ) {
        return new ManipulateTableString_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString,"MergeListFileColumns") ){
		return new MergeListFileColumns_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"MergeWellRights") ){
		return new MergeWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "Message") ) {
		return new Message_Command ();
	}
	
	// "n" commands...
	
	else if ( StringUtil.startsWithIgnoreCase(commandString,"NewTable") ) {
        return new NewTable_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "NewExcelWorkbook") ) {
        return new NewExcelWorkbook_Command ();
    }

	// "o" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString,"OpenHydroBase") ){
		return new OpenHydroBase_Command ();
	}
	
	// "p" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString,"PrintNetwork") ){
		return new PrintNetwork_Command ();
	}

	// "r" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadBlaneyCriddleFromHydroBase")) {
		return new ReadBlaneyCriddleFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadBlaneyCriddleFromStateCU")) {
		return new ReadBlaneyCriddleFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadClimateStationsFromList")) {
		return new ReadClimateStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadClimateStationsFromStateCU")) {
		return new ReadClimateStationsFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadControlFromStateMod")) {
		return new ReadControlFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropCharacteristicsFromHydroBase")) {
		return new ReadCropCharacteristicsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropCharacteristicsFromStateCU")) {
		return new ReadCropCharacteristicsFromStateCU_Command();
	}
	/* TODO SAM 2009-01-03 Evaluate if needed
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropPatternTSFromDBF")) {
		return new ReadCropPatternTSFromDBF_Command();
	}
	*/
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropPatternTSFromStateCU")) {
		return new ReadCropPatternTSFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropPatternTSFromHydroBase")) {
		return new ReadCropPatternTSFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCropPatternTSFromParcels")) {
		return new ReadCropPatternTSFromParcels_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadParcelsFromHydroBase")) {
		return new ReadParcelsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"ReadParcelsFromIrrigatedLands") ) {
		return new ReadParcelsFromIrrigatedLands_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCULocationsFromList")) {
		return new ReadCULocationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCULocationsFromStateCU")) {
		return new ReadCULocationsFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadCULocationsFromStateMod")) {
		return new ReadCULocationsFromStateMod_Command();
	}
	// Put this before the default monthly.  For legacy commands, the string Interval=Day would have
	// been in the command so can check for this to determine what command to instantiate
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDelayTablesDailyFromStateMod") ||
		(StringUtil.startsWithIgnoreCase( commandString, "ReadDelayTablesFromStateMod") &&
		StringUtil.indexOfIgnoreCase(commandString,"Interval=Day",0) > 0) ) {
		return new ReadDelayTablesDailyFromStateMod_Command();
	}
	// Default is monthly, but new commands created by the GUI should be the longer name
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDelayTablesFromStateMod") ||
		StringUtil.startsWithIgnoreCase( commandString, "ReadDelayTablesMonthlyFromStateMod")) {
		return new ReadDelayTablesMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionDemandTSMonthlyFromStateMod")) {
		return new ReadDiversionDemandTSMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionHistoricalTSMonthlyFromHydroBase")) {
		return new ReadDiversionHistoricalTSMonthlyFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionHistoricalTSMonthlyFromStateMod")) {
		return new ReadDiversionHistoricalTSMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionRightsFromHydroBase")) {
		return new ReadDiversionRightsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionRightsFromStateMod")) {
		return new ReadDiversionRightsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionStationsFromList")) {
		return new ReadDiversionStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionStationsFromNetwork")) {
		return new ReadDiversionStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadDiversionStationsFromStateMod")) {
		return new ReadDiversionStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadExcelWorkbook") ) {
        return new ReadExcelWorkbook_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowDemandTSAverageMonthlyFromStateMod")) {
		return new ReadInstreamFlowDemandTSAverageMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowRightsFromHydroBase")) {
		return new ReadInstreamFlowRightsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowRightsFromStateMod")) {
		return new ReadInstreamFlowRightsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowStationsFromList")) {
		return new ReadInstreamFlowStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowStationsFromNetwork")) {
		return new ReadInstreamFlowStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadInstreamFlowStationsFromStateMod")) {
		return new ReadInstreamFlowStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadIrrigationPracticeTSFromHydroBase")) {
		return new ReadIrrigationPracticeTSFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadIrrigationPracticeTSFromList")) {
		return new ReadIrrigationPracticeTSFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadIrrigationPracticeTSFromParcels")) {
		return new ReadIrrigationPracticeTSFromParcels_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadIrrigationPracticeTSFromStateCU")) {
		return new ReadIrrigationPracticeTSFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadIrrigationWaterRequirementTSMonthlyFromStateCU")) {
		return new ReadIrrigationWaterRequirementTSMonthlyFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadNetworkFromStateMod")) {
		return new ReadNetworkFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadOperationalRightsFromStateMod")) {
		return new ReadOperationalRightsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPatternFile")) {
		return new ReadPatternFile_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPenmanMonteithFromHydroBase")) {
		return new ReadPenmanMonteithFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPenmanMonteithFromStateCU")) {
		return new ReadPenmanMonteithFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPlanReturnFromStateMod")) {
		return new ReadPlanReturnFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPlanStationsFromStateMod")) {
		return new ReadPlanStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPlanWellAugmentationFromStateMod")) {
		return new ReadPlanWellAugmentationFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadPropertiesFromExcel") ) {
        return new ReadPropertiesFromExcel_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirReturnFromStateMod")) {
		return new ReadReservoirReturnFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirRightsFromHydroBase")) {
		return new ReadReservoirRightsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirRightsFromStateMod")) {
		return new ReadReservoirRightsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirStationsFromList")) {
		return new ReadReservoirStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirStationsFromNetwork")) {
		return new ReadReservoirStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadReservoirStationsFromStateMod")) {
		return new ReadReservoirStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadResponseFromStateMod")) {
		return new ReadResponseFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadRiverNetworkFromStateMod")) {
		return new ReadRiverNetworkFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamEstimateCoefficientsFromStateMod")) {
		return new ReadStreamEstimateCoefficientsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamEstimateStationsFromList")) {
		return new ReadStreamEstimateStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamEstimateStationsFromNetwork")) {
		return new ReadStreamEstimateStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamEstimateStationsFromStateMod")) {
		return new ReadStreamEstimateStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamGageStationsFromList")) {
		return new ReadStreamGageStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamGageStationsFromNetwork")) {
		return new ReadStreamGageStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadStreamGageStationsFromStateMod")) {
		return new ReadStreamGageStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableCellsFromExcel") ) {
        return new ReadTableCellsFromExcel_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromDataStore") ) {
        return new ReadTableFromDataStore_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromDBF") ) {
        return new ReadTableFromDBF_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromDelimitedFile") ) {
        return new ReadTableFromDelimitedFile_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromExcel") ) {
        return new ReadTableFromExcel_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromFixedFormatFile") ) {
        return new ReadTableFromFixedFormatFile_Command ();
    }
    /*else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadTableFromJSON") ) {
        return new ReadTableFromJSON_Command ();
    }
    else if ( commandName.equalsIgnoreCase("ReadTableFromXML") ) {
        return new ReadTableFromXML_Command ();
    }*/
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellDemandTSMonthlyFromStateMod")) {
		return new ReadWellDemandTSMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellHistoricalPumpingTSMonthlyFromStateCU")) {
		return new ReadWellHistoricalPumpingTSMonthlyFromStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellHistoricalPumpingTSMonthlyFromStateMod")) {
		return new ReadWellHistoricalPumpingTSMonthlyFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellRightsFromHydroBase")) {
		return new ReadWellRightsFromHydroBase_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellRightsFromStateMod")) {
		return new ReadWellRightsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellStationsFromList")) {
		return new ReadWellStationsFromList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellStationsFromNetwork")) {
		return new ReadWellStationsFromNetwork_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "ReadWellStationsFromStateMod")) {
		return new ReadWellStationsFromStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RemoveCropPatternTS")) {
		return new RemoveCropPatternTS_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RemoveFile")) {
		return new RemoveFile_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RunCommands")) {
		return new RunCommands_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RunProgram")) {
		return new RunProgram_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RunPython")) {
		return new RunPython_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "RunR")) {
		return new RunR_Command();
	}
	
	// "s" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetBlaneyCriddle") ) {
		return new SetBlaneyCriddle_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetClimateStation") ) {
		return new SetClimateStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCropCharacteristics") ) {
		return new SetCropCharacteristics_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCropPatternTSFromList") ) {
		return new SetCropPatternTSFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCropPatternTS") ) {
		return new SetCropPatternTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase(
		commandString,"SetCULocationClimateStationWeightsFromHydroBase") ) {
		return new SetCULocationClimateStationWeightsFromHydroBase_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCULocationClimateStationWeightsFromList") ) {
		return new SetCULocationClimateStationWeightsFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCULocationClimateStationWeights") ) {
		return new SetCULocationClimateStationWeights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCULocationsFromList") ) {
		return new SetCULocationsFromList_Command ();
	}
	// Put this after longer commands that start with the same string
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetCULocation") ) {
		return new SetCULocation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDebugLevel") ) {
		return new SetDebugLevel_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionAggregateFromList") ) {
		return new SetDiversionAggregateFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionAggregate") ) {
		return new SetDiversionAggregate_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionDemandTSMonthlyConstant") ) {
		return new SetDiversionDemandTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionDemandTSMonthly") ) {
		return new SetDiversionDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionHistoricalTSMonthlyConstant") ) {
		return new SetDiversionHistoricalTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionHistoricalTSMonthly") ) {
		return new SetDiversionHistoricalTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionRight") ) {
		return new SetDiversionRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionMultiStructFromList") ) {
		return new SetDiversionMultiStructFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionMultiStruct") ) {
		return new SetDiversionMultiStruct_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionRight") ) {
		return new SetDiversionRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionStationCapacitiesFromTS") ) {
		return new SetDiversionStationCapacitiesFromTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionStationDelayTablesFromNetwork") ) {
		return new SetDiversionStationDelayTablesFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionStationDelayTablesFromRTN") ) {
		return new SetDiversionStationDelayTablesFromRTN_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionStationsFromList") ) {
		return new SetDiversionStationsFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionStation") ) {
		return new SetDiversionStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionSystemFromList") ) {
		return new SetDiversionSystemFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetDiversionSystem") ) {
		return new SetDiversionSystem_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetExcelCell") ) {
        return new SetExcelCell_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetExcelWorksheetViewProperties") ) {
        return new SetExcelWorksheetViewProperties_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetInstreamFlowDemandTSAverageMonthlyConstant") ) {
		return new SetInstreamFlowDemandTSAverageMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetInstreamFlowDemandTSAverageMonthlyFromRights") ) {
		return new SetInstreamFlowDemandTSAverageMonthlyFromRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetInstreamFlowRight") ) {
		return new SetInstreamFlowRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetInstreamFlowStation") ) {
		return new SetInstreamFlowStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetIrrigationPracticeTSFromList") ) {
		return new SetIrrigationPracticeTSFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,
		"SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage") ) {
		return new SetIrrigationPracticeTSTotalAcreageToCropPatternTSTotalAcreage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetIrrigationPracticeTSPumpingMaxUsingWellRights") ) {
		return new SetIrrigationPracticeTSPumpingMaxUsingWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetIrrigationPracticeTSSprinklerAcreageFromList") ) {
		return new SetIrrigationPracticeTSSprinklerAcreageFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetIrrigationPracticeTS")) {
		return new SetIrrigationPracticeTS_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetTableValues") ) {
        return new SetTableValues_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetOutputPeriod") ) {
		return new SetOutputPeriod_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetOutputYearType") ) {
		return new SetOutputYearType_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetPenmanMonteith") ) {
		return new SetPenmanMonteith_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetPlanStation")) {
		return new SetPlanStation_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetPropertyFromTable") ) {
		// Put this before SetProperty to avoid ambiguity
        return new SetPropertyFromTable_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SetProperty" )) {
		return new SetProperty_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetReservoirAggregateFromList") ) {
		return new SetReservoirAggregateFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetReservoirAggregate") ) {
		return new SetReservoirAggregate_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetReservoirRight") ) {
		return new SetReservoirRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetReservoirStation") ) {
		return new SetReservoirStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetRiverNetworkNode") ) {
		return new SetRiverNetworkNode_Command ();
	}
	// Put the following before the shorter command below
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetStreamEstimateCoefficientsPFGage") ) {
		return new SetStreamEstimateCoefficientsPFGage_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetStreamEstimateCoefficients") ) {
		return new SetStreamEstimateCoefficients_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetStreamEstimateStation") ) {
		return new SetStreamEstimateStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetStreamGageStation") ) {
		return new SetStreamGageStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWarningLevel") ) {
		return new SetWarningLevel_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellAggregateFromList") ) {
		return new SetWellAggregateFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellAggregate") ) {
		return new SetWellAggregate_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellDemandTSMonthlyConstant") ) {
		return new SetWellDemandTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellDemandTSMonthly") ) {
		return new SetWellDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellHistoricalPumpingTSMonthlyConstant") ) {
		return new SetWellHistoricalPumpingTSMonthlyConstant_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellHistoricalPumpingTSMonthly") ) {
		return new SetWellHistoricalPumpingTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellRight") ) {
		return new SetWellRight_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationsFromList") ) {
		return new SetWellStationsFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationAreaToCropPatternTS") ) {
		return new SetWellStationAreaToCropPatternTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationCapacitiesFromTS") ) {
		return new SetWellStationCapacitiesFromTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationCapacityToWellRights") ) {
		return new SetWellStationCapacityToWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationDelayTablesFromNetwork") ) {
		return new SetWellStationDelayTablesFromNetwork_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationDelayTablesFromRTN") ) {
		return new SetWellStationDelayTablesFromRTN_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStationDepletionTablesFromRTN") ) {
		return new SetWellStationDepletionTablesFromRTN_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellStation") ) {
		return new SetWellStation_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellSystemFromList") ) {
		return new SetWellSystemFromList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWellSystem") ) {
		return new SetWellSystem_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SetWorkingDir") ) {
		return new SetWorkingDir_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortBlaneyCriddle") ) {
		return new SortBlaneyCriddle_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortClimateStations") ) {
		return new SortClimateStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortCropCharacteristics") ) {
		return new SortCropCharacteristics_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortCropPatternTS") ) {
		return new SortCropPatternTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortCULocations") ) {
		return new SortCULocations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortDiversionDemandTSMonthly") ) {
		return new SortDiversionDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortDiversionHistoricalTSMonthly") ) {
		return new SortDiversionHistoricalTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortDiversionRights") ) {
		return new SortDiversionRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortDiversionStations") ) {
		return new SortDiversionStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortInstreamFlowRights") ) {
		return new SortInstreamFlowRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortInstreamFlowStations") ) {
		return new SortInstreamFlowStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortIrrigationPracticeTS") ) {
		return new SortIrrigationPracticeTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortPenmanMonteith") ) {
		return new SortPenmanMonteith_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortReservoirRights") ) {
		return new SortReservoirRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortReservoirStations") ) {
		return new SortReservoirStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortStreamEstimateStations") ) {
		return new SortStreamEstimateStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortStreamGageStations") ) {
		return new SortStreamGageStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SortTable") ) {
        return new SortTable_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortWellDemandTSMonthly") ) {
		return new SortWellDemandTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortWellHistoricalPumpingTSMonthly") ) {
		return new SortWellHistoricalPumpingTSMonthly_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortWellRights") ) {
		return new SortWellRights_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"SortWellStations") ) {
		return new SortWellStations_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SplitStateModReport") ) {
        return new SplitStateModReport_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SplitTableColumn") ) {
        return new SplitTableColumn_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "SplitTableRow") ) {
        return new SplitTableRow_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString,"StartLog") ) {
		return new StartLog_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"StartRegressionTestResultsReport") ) {
		return new StartRegressionTestResultsReport_Command ();
	}
	
	// "t" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "TableMath") ) {
        return new TableMath_Command ();
    }
    /*else if ( StringUtil.startsWithIgnoreCase( commandString, "TableTimeSeriesMath") ) {
        return new TableTimeSeriesMath_Command ();
    }*/
	else if ( StringUtil.startsWithIgnoreCase( commandString,"TranslateBlaneyCriddle") ) {
		return new TranslateBlaneyCriddle_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"TranslateCropCharacteristics") ) {
		return new TranslateCropCharacteristics_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"TranslateCropPatternTS") ) {
		return new TranslateCropPatternTS_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"TranslatePenmanMonteith") ) {
		return new TranslatePenmanMonteith_Command ();
	}
	
	// "u" commands...
	
	else if ( StringUtil.startsWithIgnoreCase( commandString, "UnzipFile" ) ) {
		return new UnzipFile_Command ();
	}
	
	// "w" commands...
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WebGet") ) {
        return new WebGet_Command ();
    }
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteBlaneyCriddleToList")) {
		return new WriteBlaneyCriddleToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteBlaneyCriddleToStateCU")) {
		return new WriteBlaneyCriddleToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteCheckFile") ) {
		return new WriteCheckFile_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteClimateStationsToList") ) {
		return new WriteClimateStationsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteClimateStationsToStateCU")) {
		return new WriteClimateStationsToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteControlToStateMod")) {
		return new WriteControlToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCropCharacteristicsToList")) {
		return new WriteCropCharacteristicsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCropCharacteristicsToStateCU")) {
		return new WriteCropCharacteristicsToStateCU_Command();
	}
	/* TODO smalers 2020-10-11 enable if need for troubleshooting
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCropPatternTSParcelsToFile")) {
		return new WriteCropPatternTSParcelsToFile_Command();
	}
	*/
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCropPatternTSToDateValue")) {
		return new WriteCropPatternTSToDateValue_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCropPatternTSToStateCU")) {
		return new WriteCropPatternTSToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteParcelsToFile")) {
		return new WriteParcelsToFile_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCULocationsToList")) {
		return new WriteCULocationsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCULocationDelayTableAssignmentsToList")) {
		return new WriteCULocationDelayTableAssignmentsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCULocationDelayTableAssignmentsToStateCU")) {
		return new WriteCULocationDelayTableAssignmentsToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteCULocationsToStateCU")) {
		return new WriteCULocationsToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesDailyToList") || //New
		StringUtil.startsWithIgnoreCase( commandString, "WriteDailyDelayTablesToList") ) { // Old
		return new WriteDelayTablesDailyToList_Command();
	}
	// Put this before the default monthly.  For legacy commands, the string Interval=Day would have
	// been in the command so can check for this to determine what command to instantiate
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesDailyToStateMod") ||
		(StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesToStateMod") &&
		StringUtil.indexOfIgnoreCase(commandString,"Interval=Day",0) > 0) ) {
		return new WriteDelayTablesDailyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesMonthlyToList") || //New
		StringUtil.startsWithIgnoreCase( commandString, "WriteMonthlyDelayTablesToList") ) { //Old
		return new WriteDelayTablesMonthlyToList_Command();
	}
	
	// Default is monthly, but new commands created by the GUI should be the longer name
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesToStateMod") ||
		StringUtil.startsWithIgnoreCase( commandString, "WriteDelayTablesMonthlyToStateMod")) {
		return new WriteDelayTablesMonthlyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionDemandTSMonthlyToStateMod")) {
		return new WriteDiversionDemandTSMonthlyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionHistoricalTSMonthlyToStateMod")) {
		return new WriteDiversionHistoricalTSMonthlyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionRightsToList")) {
		return new WriteDiversionRightsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionRightsToStateMod")) {
		return new WriteDiversionRightsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionStationsToList")) {
		return new WriteDiversionStationsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteDiversionStationsToStateMod")) {
		return new WriteDiversionStationsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteInstreamFlowDemandTSAverageMonthlyToStateMod") ) {
		return new WriteInstreamFlowDemandTSAverageMonthlyToStateMod_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteInstreamFlowRightsToList")) {
		return new WriteInstreamFlowRightsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteInstreamFlowRightsToStateMod")) {
		return new WriteInstreamFlowRightsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteInstreamFlowStationsToList")) {
		return new WriteInstreamFlowStationsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteInstreamFlowStationsToStateMod")) {
		return new WriteInstreamFlowStationsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteIrrigationPracticeTSToDateValue")) {
		return new WriteIrrigationPracticeTSToDateValue_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteIrrigationPracticeTSToStateCU")) {
		return new WriteIrrigationPracticeTSToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteNetworkToStateMod")) {
		return new WriteNetworkToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteOperationalRightsToStateMod")) {
		return new WriteOperationalRightsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WritePenmanMonteithToList")) {
		return new WritePenmanMonteithToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WritePenmanMonteithToStateCU")) {
		return new WritePenmanMonteithToStateCU_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WritePlanReturnToStateMod")) {
		return new WritePlanReturnToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WritePlanStationsToStateMod")) {
		return new WritePlanStationsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WritePlanWellAugmentationToStateMod")) {
		return new WritePlanWellAugmentationToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteProperty")) {
		return new WriteProperty_Command();
	}
	else if (StringUtil.startsWithIgnoreCase( commandString , "WritePropertiesToFile") ){
		return new WritePropertiesToFile_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteRiverNetworkToList")) {
		return new WriteRiverNetworkToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteRiverNetworkToStateMod")) {
		return new WriteRiverNetworkToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteReservoirReturnToStateMod")) {
		return new WriteReservoirReturnToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteReservoirRightsToList")) {
		return new WriteReservoirRightsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteReservoirRightsToStateMod")) {
		return new WriteReservoirRightsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteReservoirStationsToList")) {
		return new WriteReservoirStationsToList_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteReservoirStationsToStateMod")) {
		return new WriteReservoirStationsToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteResponseToStateMod")) {
		return new WriteResponseToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamEstimateCoefficientsToList") ) {
		return new WriteStreamEstimateCoefficientsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamEstimateCoefficientsToStateMod") ) {
		return new WriteStreamEstimateCoefficientsToStateMod_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamEstimateStationsToList") ) {
		return new WriteStreamEstimateStationsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamEstimateStationsToStateMod") ) {
		return new WriteStreamEstimateStationsToStateMod_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamGageStationsToList") ) {
		return new WriteStreamGageStationsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteStreamGageStationsToStateMod") ) {
		return new WriteStreamGageStationsToStateMod_Command ();
	}
	/*else if ( StringUtil.startsWithIgnoreCase("WriteTableToDataStore") ) {
        return new WriteTableToDataStore_Command ();
    }*/
	else if (StringUtil.startsWithIgnoreCase( commandString, "WriteTableToGeoJSON") ) {
		return new WriteTableToGeoJSON_Command ();
	}
    else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteTableToDelimitedFile") ) {
        return new WriteTableToDelimitedFile_Command ();
    }
    else if (StringUtil.startsWithIgnoreCase( commandString, "WriteTableToShapefile" ) ) {
    	return new WriteTableToShapefile_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteTableToExcel") ) {
        return new WriteTableToExcel_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString,  "WriteTableCellsToExcel") ) {
        return new WriteTableCellsToExcel_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteTableToHTML") ) {
        return new WriteTableToHTML_Command ();
    }
    /*else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteTimeSeriesToExcel") ) {
        return new WriteTimeSeriesToExcel_Command ();
    }
    else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteTimeSeriesToExcelBlock") ) {
        return new WriteTimeSeriesToExcelBlock_Command ();
    }*/
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteWellDemandTSMonthlyToStateMod")) {
		return new WriteWellDemandTSMonthlyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString, "WriteWellHistoricalPumpingTSMonthlyToStateMod")) {
		return new WriteWellHistoricalPumpingTSMonthlyToStateMod_Command();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteWellRightsToList") ) {
		return new WriteWellRightsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteWellRightsToStateMod") ) {
		return new WriteWellRightsToStateMod_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteWellStationsToList") ) {
		return new WriteWellStationsToList_Command ();
	}
	else if ( StringUtil.startsWithIgnoreCase( commandString,"WriteWellStationsToStateMod") ) {
		return new WriteWellStationsToStateMod_Command ();
	}
	
	// Did not match a command...

	if ( createUnknownCommandIfNotRecognized ) {
		// Create an unknown command...
		Command c = new UnknownCommand ();
		c.setCommandString( commandString );
        Message.printStatus ( 2, routine, "Creating UnknownCommand for unknown command \"" +
                commandString + "\"" );
		return c;
	}
	else {
		// Throw an exception if the command is not recognized.
		throw new UnknownCommandException ( "Unknown command \"" + commandString + "\"" );
	}
}

}