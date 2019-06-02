// SetCropCharacteristics_Command - This class initializes, checks, and runs the SetCropCharacteristics() command.

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

import java.util.List;
import java.util.Vector;

import DWR.StateCU.StateCU_CropCharacteristics;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

/**
<p>
This class initializes, checks, and runs the SetCropCharacteristics() command.
</p>
*/
public class SetCropCharacteristics_Command extends AbstractCommand implements Command
{
	
/**
Values for IfNotFound parameter.
*/
protected final String _Add = "Add";
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";
	
/**
Constructor.
*/
public SetCropCharacteristics_Command ()
{	super();
	setCommandName ( "SetCropCharacteristics" );
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
{	String routine = "SetCropCharacteristics_Command.checkCommandParameters";
	String CropType = parameters.getValue ( "CropType" );
	String PlantingMonth = parameters.getValue ( "PlantingMonth" );
	String PlantingDay = parameters.getValue ( "PlantingDay" );
	String HarvestMonth = parameters.getValue ( "HarvestMonth" );
	String HarvestDay = parameters.getValue ( "HarvestDay" );
	String DaysToFullCover = parameters.getValue ( "DaysToFullCover" );
	String LengthOfSeason = parameters.getValue ( "LengthOfSeason" );
	String LatestMoistureUseTemp = parameters.getValue ( "LatestMoistureUseTemp" );
	String EarliestMoistureUseTemp = parameters.getValue ( "EarliestMoistureUseTemp" );
	String MaxRootZoneDepth = parameters.getValue ( "MaxRootZoneDepth" );
	String MaxAppDepth = parameters.getValue ( "MaxAppDepth" );
	String SpringFrostFlag = parameters.getValue ( "SpringFrostFlag" );
	String FallFrostFlag = parameters.getValue ( "FallFrostFlag" );
	String DaysTo2ndCut = parameters.getValue ( "DaysTo2ndCut" );
	String DaysTo3rdCut = parameters.getValue ( "DaysTo3rdCut" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	String message;
	String warning = "";
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	if ( (CropType == null) || (CropType.length() == 0) ) {
		message = "A crop type or pattern must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the crop type to match." ) );
	}
	int max_PlantingDay = 0;
	int max_HarvestDay = 0;
	if ( (PlantingMonth != null) && !PlantingMonth.equals("") && (!StringUtil.isInteger(PlantingMonth) ||
		(StringUtil.atoi(PlantingMonth) < 1) || (StringUtil.atoi(PlantingMonth) > 12)) ) {
		message = "The planting month (" + PlantingMonth + ") is not valid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the planting month as 1-12." ) );
		max_PlantingDay = 0;
	}
	else {
		max_PlantingDay = TimeUtil.numDaysInMonth(StringUtil.atoi(PlantingMonth),1976);
	}
	
	if ( (PlantingDay != null) && !PlantingDay.equals("") && (!StringUtil.isInteger(PlantingDay) ||
		(StringUtil.atoi(PlantingDay) < 1) || (StringUtil.atoi(PlantingDay) > max_PlantingDay)) ) {
		message = "The planting day (" + PlantingDay + ") is not a valid number";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify a valid day number (1-" + max_PlantingDay) );
	}

	if ( (HarvestMonth != null) && !HarvestMonth.equals("") && (!StringUtil.isInteger(HarvestMonth) ||
		(StringUtil.atoi(HarvestMonth) < 1) || (StringUtil.atoi(HarvestMonth) > 12)) ) {
		message = "The harvest month (" + HarvestMonth + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify a valid month number (1-12).") );
		max_HarvestDay = 0;
	}
	else {
		max_HarvestDay = TimeUtil.numDaysInMonth(StringUtil.atoi(HarvestMonth), 1976 );
	}
	
	if ( (HarvestDay != null) && !HarvestDay.equals("") && (!StringUtil.isInteger(HarvestDay) ||
		(StringUtil.atoi(HarvestDay) < 1) || (StringUtil.atoi(HarvestDay) > max_HarvestDay)) ) {
		message = "The harvest day (" + HarvestDay + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify a valid day number (1-" + max_HarvestDay + ").") );
	}
	
	if ( (DaysToFullCover != null) && !DaysToFullCover.equals("") &&
		!StringUtil.isInteger(DaysToFullCover) ) {
		message = "The days to full cover (" + DaysToFullCover + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the number of days as an integer.") );
	}
	
	if ( (LengthOfSeason != null) && !LengthOfSeason.equals("") && !StringUtil.isInteger(LengthOfSeason) ) {
		message = "The length of season (" + LengthOfSeason + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the length of growing season as an integer.") );
	}
	
	if ( (EarliestMoistureUseTemp != null) && !EarliestMoistureUseTemp.equals("") &&
		!StringUtil.isDouble(EarliestMoistureUseTemp) ) {
		message = "The earliest moisture value (" + EarliestMoistureUseTemp + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the temperature as a number.") );
	}
	
	if ( (LatestMoistureUseTemp != null) && !LatestMoistureUseTemp.equals("") &&
		!StringUtil.isDouble(LatestMoistureUseTemp) ) {
		message = "The latest moisture value (" + LatestMoistureUseTemp + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the temperature as a number.") );
	}
	
	if ( (MaxRootZoneDepth != null) && !MaxRootZoneDepth.equals("") && !StringUtil.isDouble(MaxRootZoneDepth) ) {
		message = "The maximum root zone depth (" + MaxRootZoneDepth + ") is not a vaoid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the maximum root zone depth as a number.") );
	}
	
	if ( (MaxAppDepth != null) && !MaxAppDepth.equals("") && !StringUtil.isDouble(MaxAppDepth) ) {
		message = "The maximum application depth (" + MaxAppDepth + ") is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the maximum application depth as a number.") );
	}
	
	if ( (SpringFrostFlag != null) && !SpringFrostFlag.equals("") && !StringUtil.isInteger(SpringFrostFlag) ) {
		message = "The spring frost flag (" + SpringFrostFlag + "\" is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the spring frost flag as an integer.") );
	}
	
	if ( (FallFrostFlag != null) && !FallFrostFlag.equals("") && !StringUtil.isInteger(FallFrostFlag) ) {
		message = "The fall frost flag (" + FallFrostFlag + "\" is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the fall frost flag as an integer.") );
	}
	
	if ( (DaysTo2ndCut != null) && !DaysTo2ndCut.equals("") && !StringUtil.isInteger(DaysTo2ndCut) ) {
		message = "The days between first and second cuts (" + DaysTo2ndCut + "\" is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the number of days as an integer.") );
	}
	
	if ( (DaysTo3rdCut != null) && !DaysTo3rdCut.equals("") && !StringUtil.isInteger(DaysTo3rdCut) ) {
		message = "The days between second and third cuts (" + DaysTo3rdCut + "\" is not a valid number.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the number of days as an integer.") );
	}
	
	if ( (IfNotFound != null) && (IfNotFound.length() > 0) && !IfNotFound.equalsIgnoreCase(_Add) &&
		!IfNotFound.equalsIgnoreCase(_Ignore) && !IfNotFound.equalsIgnoreCase(_Fail) &&
		!IfNotFound.equalsIgnoreCase(_Warn) ) {
		message = "The IfNotFound value (" + IfNotFound + ") is invalid.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify IfNotFound as " + _Add + ", " + _Ignore + ", " + _Fail +
				", or " + _Warn + " (default).") );
	}

	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>(16);
    valid_Vector.add ( "CropType" );
	valid_Vector.add ( "PlantingMonth" );
    valid_Vector.add ( "PlantingDay" );
	valid_Vector.add ( "HarvestMonth" );
    valid_Vector.add ( "HarvestDay" );
	valid_Vector.add ( "DaysToFullCover" );
	valid_Vector.add ( "LengthOfSeason" );
	valid_Vector.add ( "LatestMoistureUseTemp" );
	valid_Vector.add ( "EarliestMoistureUseTemp" );
	valid_Vector.add ( "MaxRootZoneDepth" );
	valid_Vector.add ( "MaxAppDepth" );
	valid_Vector.add ( "SpringFrostFlag" );
	valid_Vector.add ( "FallFrostFlag" );
	valid_Vector.add ( "DaysTo2ndCut" );
	valid_Vector.add ( "DaysTo3rdCut" );
	valid_Vector.add ( "IfNotFound" );
	warning = StateDMICommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine, warning );
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
{	
	return (new SetCropCharacteristics_JDialog ( parent, this )).ok();
}

// Parse command is in the base class

/**
Method to execute the setIrrigationPracticeTSPumpingMaxUsingWellRights() command.
@param command_number Number of command in sequence.
@exception Exception if there is an error processing the command.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String message, routine = getCommandName() + "_Command.runCommand";
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);
	
	// Get the input parameters...
	
	PropList parameters = getCommandParameters();
	String CropType = parameters.getValue ( "CropType" );
	if ( CropType == null ) {
		CropType = "*"; // Default
	}
	String idpattern_Java = StringUtil.replaceString(CropType,"*",".*");
	String PlantingMonth = parameters.getValue ( "PlantingMonth" );
	String PlantingDay = parameters.getValue ( "PlantingDay" );
	String HarvestMonth = parameters.getValue ( "HarvestMonth" );
	String HarvestDay = parameters.getValue ( "HarvestDay" );
	String DaysToFullCover = parameters.getValue ( "DaysToFullCover" );
	String LengthOfSeason = parameters.getValue ( "LengthOfSeason" );
	String LatestMoistureUseTemp = parameters.getValue ( "LatestMoistureUseTemp" );
	String EarliestMoistureUseTemp = parameters.getValue ( "EarliestMoistureUseTemp" );
	String MaxRootZoneDepth = parameters.getValue ( "MaxRootZoneDepth" );
	String MaxAppDepth = parameters.getValue ( "MaxAppDepth" );
	String SpringFrostFlag = parameters.getValue ( "SpringFrostFlag" );
	String FallFrostFlag = parameters.getValue ( "FallFrostFlag" );
	String DaysTo2ndCut = parameters.getValue ( "DaysTo2ndCut" );
	String DaysTo3rdCut = parameters.getValue ( "DaysTo3rdCut" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	if ( IfNotFound == null ) {
		IfNotFound = _Warn; // Default
	}
		
	// Get the list of crop characteristics...
	
	List<StateCU_CropCharacteristics> cchList = null;
	int cchListSize = 0;
	try {
		@SuppressWarnings("unchecked")
		List<StateCU_CropCharacteristics> dataList = (List<StateCU_CropCharacteristics>)processor.getPropContents( "StateCU_CropCharacteristics_List");
		cchList = dataList;
		cchListSize = cchList.size();
	}
	catch ( Exception e ) {
		message = "Error requesting StateCU_CropCharacteristics_List from processor.";
		Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
			routine, message );
		status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Report problem to software support." ) );
	}
	
	if ( warning_count > 0 ) {
		// Input error...
		message = "Insufficient data to run command.";
        status.addToLog ( CommandPhaseType.RUN,
        new CommandLogRecord(CommandStatusType.FAILURE,
              message, "Check input to command." ) );
		Message.printWarning(3, routine, message );
		throw new CommandException ( message );
	}
	
	// Now process...
	
	try {
		// FIXME SAM 2009-02-16 Need to parse values once instead of in code below
		boolean fill_PlantingMonth = false;
		if ( (PlantingMonth != null) && !PlantingMonth.equals("")) {
			fill_PlantingMonth = true;
        }
		boolean fill_PlantingDay = false;
		if ( (PlantingDay != null) && !PlantingDay.equals("")) {
			fill_PlantingDay = true;
		}
		boolean fill_HarvestMonth = false;
		if ( (HarvestMonth != null) && !HarvestMonth.equals("")) {
			fill_HarvestMonth = true;
		}
		boolean fill_HarvestDay = false;
		if ( (HarvestDay != null) && !HarvestDay.equals("")) {
			fill_HarvestDay = true;
		}
		boolean fill_DaysToFullCover = false;
		if ( (DaysToFullCover != null) && !DaysToFullCover.equals("")) {
			fill_DaysToFullCover = true;
		}
		boolean fill_LengthOfSeason = false;
		if ( (LengthOfSeason != null) && !LengthOfSeason.equals("")) {
			fill_LengthOfSeason = true;
		}
		boolean fill_EarliestMoistureUseTemp = false;
		if ( (EarliestMoistureUseTemp != null) && !EarliestMoistureUseTemp.equals("")) {
			fill_EarliestMoistureUseTemp = true;
		}
		boolean fill_LatestMoistureUseTemp = false;
		if ( (LatestMoistureUseTemp != null) && !LatestMoistureUseTemp.equals("")) {
			fill_LatestMoistureUseTemp = true;
		}
		boolean fill_MaxRootZoneDepth = false;
		if ( (MaxRootZoneDepth != null) && !MaxRootZoneDepth.equals("")) {
			fill_MaxRootZoneDepth = true;
		}
		boolean fill_MaxAppDepth = false;
		if ( (MaxAppDepth != null) && !MaxAppDepth.equals("")) {
			fill_MaxAppDepth = true;
		}
		boolean fill_SpringFrostFlag = false;
		if ( (SpringFrostFlag != null) && !SpringFrostFlag.equals("")) {
			fill_SpringFrostFlag = true;
		}
		boolean fill_FallFrostFlag = false;
		if ( (FallFrostFlag != null) && !FallFrostFlag.equals("")) {
			fill_FallFrostFlag = true;
		}
		boolean fill_DaysTo2ndCut = false;
		boolean fill_DaysTo3rdCut = false;
		if ( (DaysTo2ndCut != null) && !DaysTo2ndCut.equals("")) {
			fill_DaysTo2ndCut = true;
		}
		if ( (DaysTo3rdCut != null) && !DaysTo3rdCut.equals("")) {
			fill_DaysTo3rdCut = true;
		}

		StateCU_CropCharacteristics cucc = null;
		String id;
		int matchCount = 0;
		for (int i = 0; i < cchListSize; i++) {
			cucc = (StateCU_CropCharacteristics)cchList.get(i);
			// Note that Name is used for the ID since internal ID was traditionally the number
			// TODO SAM 2009-05-08 Evaluate whether to treat ID and name the same always
			id = cucc.getName();
			if ( !id.matches(idpattern_Java) ) {
				// Identifier does not match...
				continue;
			}
			++matchCount;
			// Have a match so reset the data...
			if ( fill_PlantingMonth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " planting month -> "+PlantingMonth);
				cucc.setGdate1(Integer.parseInt(PlantingMonth) );
			}
			if ( fill_PlantingDay ) {
				Message.printStatus ( 2, routine, "Setting " + id + " planting day -> " + PlantingDay );
				cucc.setGdate2 (Integer.parseInt(PlantingDay) );
			}
			if ( fill_HarvestMonth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " harvest month -> "+ HarvestMonth);
				cucc.setGdate3(Integer.parseInt(HarvestMonth) );
			}
			if ( fill_HarvestDay ) {
				Message.printStatus ( 2, routine, "Setting " + id + " harvest day -> " + HarvestDay );
				cucc.setGdate4 (Integer.parseInt(HarvestDay) );
			}
			if ( fill_DaysToFullCover ) {
				Message.printStatus ( 2, routine, "Setting " + id + " days to full cover -> " + DaysToFullCover );
				cucc.setGdate5 (Integer.parseInt(DaysToFullCover) );
			}
			if ( fill_LengthOfSeason ) {
				Message.printStatus ( 2, routine, "Setting " + id + " length of season -> " + LengthOfSeason );
				cucc.setGdates (Integer.parseInt(LengthOfSeason) );
			}
			if ( fill_EarliestMoistureUseTemp ) {
				Message.printStatus ( 2, routine, "Setting " + id + " earliest moisture use temp -> " +
				EarliestMoistureUseTemp );
				cucc.setTmois1(Double.parseDouble(EarliestMoistureUseTemp));
			}
			if ( fill_LatestMoistureUseTemp ) {
				Message.printStatus ( 2, routine, "Setting " + id + " latest moisture use temp -> " +
				LatestMoistureUseTemp );
				cucc.setTmois2 (Double.parseDouble(LatestMoistureUseTemp));
			}
			if ( fill_MaxRootZoneDepth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " maximum root zone -> " + MaxRootZoneDepth );
				cucc.setFrx(Double.parseDouble(MaxRootZoneDepth) );
			}
			if ( fill_MaxAppDepth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " maximum application depth -> " +
				MaxAppDepth );
				cucc.setApd(Double.parseDouble( MaxAppDepth) );
			}
			if ( fill_SpringFrostFlag ) {
				Message.printStatus ( 2, routine, "Setting " + id + " spring frost flag -> " +
				SpringFrostFlag );
				cucc.setTflg1(Integer.parseInt( SpringFrostFlag) );
			}
			if ( fill_FallFrostFlag ) {
				Message.printStatus ( 2, routine, "Setting " + id + " fall frost flag -> " +
				FallFrostFlag );
				cucc.setTflg2(Integer.parseInt(FallFrostFlag) );
			}
			if ( fill_DaysTo2ndCut ) {
				Message.printStatus ( 2, routine, "Setting " + id + " days between 1st and 2nd cut -> " + DaysTo2ndCut );
				cucc.setCut2 ( Integer.parseInt(DaysTo2ndCut) );
			}
			if ( fill_DaysTo3rdCut ) {
				Message.printStatus ( 2, routine, "Setting " + id +
				" days between 2nd and 3rd cut -> " + DaysTo3rdCut );
				cucc.setCut3 ( Integer.parseInt(DaysTo3rdCut) );
			}
		}

		// If nothing was matched and the idpattern does not contain a
		// wildcard, add a StateCU_CropCharacteristics at the end...

		if ( (matchCount == 0) && (CropType.indexOf("*") < 0) && IfNotFound.equalsIgnoreCase("Add") ) {
			cucc = new StateCU_CropCharacteristics();
			id = CropType;
			cucc.setName ( id );
			Message.printStatus ( 2, routine, "Adding CU crop characteristics " + id );
			cchList.add ( cucc );
			// Indent is the same as above to simplify code maintenance...
			if ( fill_PlantingMonth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " planting month -> "+PlantingMonth);
				cucc.setGdate1(Integer.parseInt(PlantingMonth) );
			}
			if ( fill_PlantingDay ) {
				Message.printStatus ( 2, routine, "Setting " + id + " planting day -> " + PlantingDay );
				cucc.setGdate2 (Integer.parseInt(PlantingDay) );
			}
			if ( fill_HarvestMonth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " harvest month -> "+ HarvestMonth);
				cucc.setGdate3(Integer.parseInt(HarvestMonth) );
			}
			if ( fill_HarvestDay ) {
				Message.printStatus ( 2, routine, "Setting " + id + " harvest day -> " + HarvestDay );
				cucc.setGdate4 (Integer.parseInt(HarvestDay) );
			}
			if ( fill_DaysToFullCover ) {
				Message.printStatus ( 2, routine, "Setting " + id + " days to full cover -> " +
				DaysToFullCover );
				cucc.setGdate5 (Integer.parseInt(DaysToFullCover) );
			}
			if ( fill_LengthOfSeason ) {
				Message.printStatus ( 2, routine, "Setting " + id + " length of season -> " +
				LengthOfSeason );
				cucc.setGdates (Integer.parseInt(LengthOfSeason) );
			}
			if ( fill_EarliestMoistureUseTemp ) {
				Message.printStatus ( 2, routine, "Setting " + id + " earliest moisture use temp -> " +
				EarliestMoistureUseTemp );
				cucc.setTmois1(Double.parseDouble(EarliestMoistureUseTemp));
			}
			if ( fill_LatestMoistureUseTemp ) {
				Message.printStatus ( 2, routine,
				"Setting " + id + " latest moisture use temp -> " + LatestMoistureUseTemp );
				cucc.setTmois2(Double.parseDouble(LatestMoistureUseTemp) );
			}
			if ( fill_MaxRootZoneDepth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " maximum root zone -> " + MaxRootZoneDepth );
				cucc.setFrx(Double.parseDouble(MaxRootZoneDepth) );
			}
			if ( fill_MaxAppDepth ) {
				Message.printStatus ( 2, routine, "Setting " + id + " maximum application depth -> " + MaxAppDepth );
				cucc.setApd(Double.parseDouble(MaxAppDepth) );
			}
			if ( fill_SpringFrostFlag ) {
				Message.printStatus ( 2, routine, "Setting " + id + " spring frost flag -> " + SpringFrostFlag );
				cucc.setTflg1(Integer.parseInt( SpringFrostFlag) );
			}
			if ( fill_FallFrostFlag ) {
				Message.printStatus ( 2, routine, "Setting " + id + " fall frost flag -> "+FallFrostFlag);
				cucc.setTflg2(Integer.parseInt(FallFrostFlag) );
			}
			if ( fill_DaysTo2ndCut ) {
				Message.printStatus ( 2, routine,
				"Setting " + id + " days between 1st and 2nd cut -> " + DaysTo2ndCut );
				cucc.setCut2 ( Integer.parseInt(DaysTo2ndCut) );
			}
			if ( fill_DaysTo3rdCut ) {
				Message.printStatus ( 2, routine,
				"Setting " + id + " days between 2nd and 3rd cut -> " + DaysTo3rdCut );
				cucc.setCut3 ( Integer.parseInt(DaysTo3rdCut) );
			}
			// Increment the count
			++matchCount;
		}
		// If nothing was matched, perform other actions...

		if ( matchCount == 0 ) {
			if ( IfNotFound.equalsIgnoreCase(_Warn) ) {
				message = "Crop type \"" + CropType + "\" was not matched: warning and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.WARNING,
						message, "Verify that the identifier is correct." ) );
			}
			else if ( IfNotFound.equalsIgnoreCase(_Fail) ) {
				message = "Crop type \"" + CropType + "\" was not matched: failing and not setting.";
				Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Verify that the identifier is correct." ) );
			}
		}
	}
    catch ( Exception e ) {
        message = "Unexpected error setting crop characteristics data (" + e + ").";
        Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( CommandPhaseType.RUN,
			new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
	
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
            command_tag, ++warning_count),
            routine,message);
        throw new CommandWarningException ( message );
    }
	
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
	
	String CropType = parameters.getValue ( "CropType" );
	String PlantingMonth = parameters.getValue ( "PlantingMonth" );
	String PlantingDay = parameters.getValue ( "PlantingDay" );
	String HarvestMonth = parameters.getValue ( "HarvestMonth" );
	String HarvestDay = parameters.getValue ( "HarvestDay" );
	String DaysToFullCover = parameters.getValue ( "DaysToFullCover" );
	String LengthOfSeason = parameters.getValue ( "LengthOfSeason" );
	String LatestMoistureUseTemp = parameters.getValue ( "LatestMoistureUseTemp" );
	String EarliestMoistureUseTemp = parameters.getValue ( "EarliestMoistureUseTemp" );
	String MaxRootZoneDepth = parameters.getValue ( "MaxRootZoneDepth" );
	String MaxAppDepth = parameters.getValue ( "MaxAppDepth" );
	String SpringFrostFlag = parameters.getValue ( "SpringFrostFlag" );
	String FallFrostFlag = parameters.getValue ( "FallFrostFlag" );
	String DaysTo2ndCut = parameters.getValue ( "DaysTo2ndCut" );
	String DaysTo3rdCut = parameters.getValue ( "DaysTo3rdCut" );
	String IfNotFound = parameters.getValue ( "IfNotFound" );
	
	StringBuffer b = new StringBuffer ();

	if ( (CropType != null) && CropType.length() > 0 ) {
		b.append ( "CropType=\"" + CropType + "\"" );
	}
	if ( (PlantingMonth != null) && PlantingMonth.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PlantingMonth=" + PlantingMonth );
	}
	if ( (PlantingDay != null) && PlantingDay.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "PlantingDay=" + PlantingDay );
	}
	if ( (HarvestMonth != null) && HarvestMonth.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "HarvestMonth=" + HarvestMonth );
	}
	if ( (HarvestDay != null) && HarvestDay.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "HarvestDay=" + HarvestDay );
	}
	if ( (DaysToFullCover != null) && DaysToFullCover.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DaysToFullCover=" + DaysToFullCover );
	}
	if ( (LengthOfSeason != null) && LengthOfSeason.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LengthOfSeason=" + LengthOfSeason );
	}
	if ( (LatestMoistureUseTemp != null) && LatestMoistureUseTemp.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "LatestMoistureUseTemp=" + LatestMoistureUseTemp );
	}
	if ( (EarliestMoistureUseTemp != null) && EarliestMoistureUseTemp.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ("EarliestMoistureUseTemp=" + EarliestMoistureUseTemp);
	}
	if ( (MaxRootZoneDepth != null) && MaxRootZoneDepth.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MaxRootZoneDepth=" + MaxRootZoneDepth );
	}
	if ( (MaxAppDepth != null) && MaxAppDepth.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MaxAppDepth=" + MaxAppDepth );
	}
	if ( (SpringFrostFlag != null) && SpringFrostFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SpringFrostFlag=" + SpringFrostFlag );
	}
	if ( (FallFrostFlag != null) && FallFrostFlag.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FallFrostFlag=" + FallFrostFlag );
	}
	if ( (DaysTo2ndCut != null) && DaysTo2ndCut.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DaysTo2ndCut=" + DaysTo2ndCut );
	}
	if ( (DaysTo3rdCut != null) && DaysTo3rdCut.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "DaysTo3rdCut=" + DaysTo3rdCut );
	}
	if ( IfNotFound != null && IfNotFound.length() > 0 ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "IfNotFound=" + IfNotFound );
	}
	
	return getCommandName() + "(" + b.toString() + ")";
}

}
